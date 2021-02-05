/*
 * Copyright (C) 2021 DBC A/S (http://dbc.dk/)
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public abstract class JavascriptBuilder<T> {

    private static final Logger log = LoggerFactory.getLogger(JavascriptBuilder.class);

    public static final List<String> DEFAULT_SEARCH_PATH = Collections.unmodifiableList(Arrays.asList("classpath:javascript".split("\\s+")));

    protected abstract T create(String scriptFile, String methodName, List<String> searchPaths, ClassLoader classLoader) throws Exception;

    protected abstract String getDefaultScriptFile();

    protected abstract String getDefaultMethod();

    private String scriptFileName;
    private String methodName;
    private ClassLoader classLoader;
    private final LinkedList<String> searchPaths;

    protected JavascriptBuilder() {
        this.searchPaths = new LinkedList<>();
    }

    /**
     * Set the script file name
     *
     * @param scriptFileName name of script
     * @return self for chaining
     */
    public JavascriptBuilder<T> scriptFileName(String scriptFileName) {
        this.scriptFileName = setOnce("scriptFileName", this.scriptFileName, scriptFileName);
        return this;
    }

    /**
     * Set the method to call to make ManifestationInformation objects
     *
     * @param methodName name of the JavaScript method
     * @return self for chaining
     */
    public JavascriptBuilder<T> manifestationInformationMethod(String methodName) {
        this.methodName = setOnce("manifestationInformationMethod", this.methodName, methodName);
        return this;
    }

    /**
     * Set the classloader for "classpath:" type search paths
     *
     * @param classLoader Classloader object
     * @return self for chaining
     */
    public JavascriptBuilder<T> classLoader(ClassLoader classLoader) {
        this.classLoader = setOnce("classLoader", this.classLoader, classLoader);
        return this;
    }

    /**
     * Add a single path element to the end of the search paths
     *
     * @param path classpath or file path
     * @return self for chaining
     */
    public JavascriptBuilder<T> appendPath(String path) {
        searchPaths.add(path);
        return this;
    }

    /**
     * Add a single path element to the beginning of the search paths
     *
     * @param path classpath or file path
     * @return self for chaining
     */
    public JavascriptBuilder<T> prependPath(String path) {
        searchPaths.add(0, path);
        return this;
    }

    /**
     * Add multiple path elements to the end of the search paths
     *
     * @param paths classpath or file paths
     * @return self for chaining
     */
    public JavascriptBuilder<T> appendPaths(List<String> paths) {
        searchPaths.addAll(paths);
        return this;
    }

    /**
     * Add multiple path elements to the beginning of the search paths
     *
     * @param paths classpath or file paths
     * @return self for chaining
     */
    public JavascriptBuilder<T> prependPaths(List<String> paths) {
        searchPaths.addAll(0, paths);
        return this;
    }

    /**
     * Build an environment builder
     *
     * @return Javascript environment supplier
     */
    public Supplier<T> build() {
        String scriptFile = or(this.scriptFileName, getDefaultScriptFile());
        String method = or(this.methodName, getDefaultMethod());
        ClassLoader cl = or(this.classLoader, JavascriptCacheObjectBuilder.class.getClassLoader());
        if (searchPaths.isEmpty())
            searchPaths.addAll(DEFAULT_SEARCH_PATH);

        return () -> {
            log.info("Building JavaScript Environment");
            try {
                return create(scriptFile, method, searchPaths, cl);
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
