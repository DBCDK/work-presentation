/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-javascript
 *
 * work-presentation-javascript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-javascript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.javascript;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.dbc.jscommon.JsCommonPaths;
import dk.dbc.jslib.ClasspathSchemeHandler;
import dk.dbc.jslib.Environment;
import dk.dbc.jslib.FileSchemeHandler;
import dk.dbc.jslib.ModuleHandler;
import dk.dbc.jslib.SchemeURI;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.script.Bindings;
import jdk.nashorn.api.scripting.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JavascriptCacheObjectBuilder {

    private static final Logger log = LoggerFactory.getLogger(JavascriptCacheObjectBuilder.class);
    private static final ObjectMapper O = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    public static final String DEFAULT_SCRIPT_FILE = "BuildCacheObject.js";
    public static final String DEFAULT_METHOD = "buildManifestationInformation";
    public static final List<String> DEFAULT_SEARCH_PATH = Collections.unmodifiableList(Arrays.asList("classpath:javascript".split("\\s+")));

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String scriptFileName;
        private String methodName;
        private ClassLoader classLoader;
        private final LinkedList<String> searchPaths;

        private Builder() {
            this.searchPaths = new LinkedList<>();
        }

        /**
         * Set the script file name
         *
         * @param scriptFileName name of script
         * @return self for chaining
         */
        public Builder scriptFileName(String scriptFileName) {
            this.scriptFileName = setOnce("scriptFileName", this.scriptFileName, scriptFileName);
            return this;
        }

        /**
         * Set the method to call to make ManifestationInformation objects
         *
         * @param methodName name of the JavaScript method
         * @return self for chaining
         */
        public Builder manifestationInformationMethod(String methodName) {
            this.methodName = setOnce("manifestationInformationMethod", this.methodName, methodName);
            return this;
        }

        /**
         * Set the classloader for "classpath:" type search paths
         *
         * @param classLoader Classloader object
         * @return self for chaining
         */
        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = setOnce("classLoader", this.classLoader, classLoader);
            return this;
        }

        /**
         * Add a single path element to the end of the search paths
         *
         * @param path classpath or file path
         * @return self for chaining
         */
        public Builder appendPath(String path) {
            searchPaths.add(path);
            return this;
        }

        /**
         * Add a single path element to the beginning of the search paths
         *
         * @param path classpath or file path
         * @return self for chaining
         */
        public Builder prependPath(String path) {
            searchPaths.add(0, path);
            return this;
        }

        /**
         * Add multiple path elements to the end of the search paths
         *
         * @param paths classpath or file paths
         * @return self for chaining
         */
        public Builder appendPaths(List<String> paths) {
            searchPaths.addAll(paths);
            return this;
        }

        /**
         * Add multiple path elements to the beginning of the search paths
         *
         * @param paths classpath or file paths
         * @return self for chaining
         */
        public Builder prependPaths(List<String> paths) {
            searchPaths.addAll(0, paths);
            return this;
        }

        /**
         * Build an environment builder
         *
         * @return Javascript environment supplier
         */
        public Supplier<JavascriptCacheObjectBuilder> build() {
            String scriptFile = or(this.scriptFileName, DEFAULT_SCRIPT_FILE);
            String method = or(this.methodName, DEFAULT_METHOD);
            ClassLoader cl = or(this.classLoader, JavascriptCacheObjectBuilder.class.getClassLoader());
            if (searchPaths.isEmpty())
                searchPaths.addAll(DEFAULT_SEARCH_PATH);

            return () -> {
                log.info("Building JavaScript Environment");
                try {
                    return new JavascriptCacheObjectBuilder(scriptFile, method, searchPaths, cl);
                } catch (Exception ex) {
                    log.error("Error building JavaScript Environment: {}", ex.getMessage());
                    log.debug("Error building JavaScript Environment: ", ex);
                    throw new RuntimeException(ex);
                }
            };
        }

        private static <T> T setOnce(String field, T oldValue, T newValue) {
            if (newValue == null) {
                throw new IllegalArgumentException("Cannot set field: " + field + " to null");
            }
            if (oldValue != null) {
                throw new IllegalStateException("Field: " + field + " cannot be set to " + newValue + ", it is already set to: " + oldValue);
            }
            return newValue;
        }

        private static <T> T or(T... ts) {
            for (T t : ts) {
                if (t != null)
                    return t;
            }
            return null;
        }
    }

    private final String methodName;
    private final Environment environment;

    private JavascriptCacheObjectBuilder(String scriptFile, String methodName, List<String> searchPaths, ClassLoader classLoader) throws Exception {
        this.methodName = methodName;
        ModuleHandler moduleHandler = new ModuleHandler();
        moduleHandler.registerHandler("file", new FileSchemeHandler("/"));
        moduleHandler.registerHandler("classpath", new ClasspathSchemeHandler(classLoader));
        searchPaths.forEach(searchPath -> {
            if (!searchPath.endsWith(":") && !searchPath.endsWith("/")) {
                searchPath += "/";
            }
            moduleHandler.addSearchPath(new SchemeURI(searchPath));
        });
        searchPaths.forEach(searchPath -> {
            if (!searchPath.endsWith(":") && !searchPath.endsWith("/")) {
                searchPath += "/";
            }
            JsCommonPaths.registerPaths(searchPath, moduleHandler);
        });
        this.environment = new Environment();
        environment.registerUseFunction(moduleHandler);
        try (InputStreamReader is = new InputStreamReader(findFile(searchPaths, scriptFile, classLoader), UTF_8)) {
            environment.eval(is, scriptFile);
        }
    }

    /**
     * Call a JavaScript method to make a ManifestationInformation object
     *
     * @param manifestationId Id to set in result object (and used for logging)
     * @param xmlObjects      map of stream name to xml for the object
     * @return Manifestation Information as the JavaScript defines it
     * @throws Exception If JavaScript fail, or the return type cannot be
     *                   converted to the Java Object
     */
    public ManifestationInformation extractManifestationInformation(String manifestationId, Map<String, String> xmlObjects) throws Exception {
        Object obj = environment.callMethod(methodName, new Object[] {manifestationId, xmlObjects});
        JsonNode json = javaScriptObjectToJson(obj);
        log.trace("json = {}", json);
        return O.readValue(O.treeAsTokens(json),
                           ManifestationInformation.class);
    }

    /**
     * Convert a JSObject result type into JSON.
     *
     * @param object Value extracted from JavaScript environment
     * @return either null, ObjectNOde or ArrayNode
     */
    private JsonNode javaScriptObjectToJson(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof JSObject && ( (JSObject) object ).isArray()) {
            JSObject obj = (JSObject) object;
            ArrayNode array = O.createArrayNode();
            obj.values().forEach(value -> {
                if (value == null) {
                    array.addNull();
                } else {
                    switch (value.getClass().getCanonicalName()) {
                        case "java.lang.String":
                            array.add((String) value);
                            break;
                        case "java.lang.Double":
                            array.add((double) value);
                            break;
                        case "java.lang.Integer":
                            array.add((int) value);
                            break;
                        case "java.lang.Boolean":
                            array.add((boolean) value);
                            break;
                        default:
                            JsonNode v1 = javaScriptObjectToJson(value);
                            if (v1 != null) {
                                array.add(v1);
                            } else {
                                throw new IllegalStateException("Cannot convert array value of " + value.getClass().getCanonicalName() + " to json type");
                            }
                    }
                }
            });
            return array;
        } else if (object instanceof Bindings) {
            Bindings bindings = (Bindings) object;
            ObjectNode json = O.createObjectNode();
            bindings.forEach((key, value) -> {
                if (value == null) {
                    json.putNull(key);
                } else {
                    switch (value.getClass().getCanonicalName()) {
                        case "java.lang.String":
                            json.put(key, (String) value);
                            break;
                        case "java.lang.Double":
                            json.put(key, (double) value);
                            break;
                        case "java.lang.Integer":
                            json.put(key, (int) value);
                            break;
                        case "java.lang.Boolean":
                            json.put(key, (boolean) value);
                            break;
                        default:
                            JsonNode v1 = javaScriptObjectToJson(value);
                            if (v1 != null) {
                                json.set(key, v1);
                            } else {
                                throw new IllegalStateException("Cannot convert map value of " + value.getClass().getCanonicalName() + " for " + key + " to json type");
                            }
                    }
                }
            });
            return json;
        } else {
            throw new IllegalStateException("Cannot convert JavaScript value of " + object.getClass().getCanonicalName() + " to json type");
        }
    }

    /**
     * Locate a file in a search-path
     *
     * @param searchPaths list of file: or classpath: type search locations
     * @param fileName    name of file to find
     * @param cl          classloader for classpath type search path
     * @return InputStream for the file
     * @throws FileNotFoundException If the file isn't found
     */
    private static InputStream findFile(List<String> searchPaths, String fileName, ClassLoader cl) throws FileNotFoundException {
        for (String searchPath : searchPaths) {
            SchemeURI schemeURI = new SchemeURI(searchPath);
            String path = schemeURI.getPath();
            if (!path.endsWith("/"))
                path += "/";
            path += fileName;

            if (schemeURI.getScheme().equals("classpath")) {
                InputStream is = cl.getResourceAsStream(path);
                if (is != null) {
                    return is;
                }
            } else if (schemeURI.getScheme().equals("file")) {
                File file = new File(path);
                if (file.isFile() && file.canRead()) {
                    return new FileInputStream(file);
                }
            }
        }
        log.error("Cannot locate {} in searchpath {}", fileName, searchPaths);
        throw new FileNotFoundException("Cannot locate '" + fileName + "'");
    }
}
