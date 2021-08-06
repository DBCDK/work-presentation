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
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JavascriptCacheObjectBuilder extends AbstractJavascript {

    private static final Logger log = LoggerFactory.getLogger(JavascriptCacheObjectBuilder.class);
    private static final ObjectMapper O = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends JavascriptBuilder<JavascriptCacheObjectBuilder> {

        @Override
        protected JavascriptCacheObjectBuilder create(String scriptFile, String methodName, List<String> searchPaths, ClassLoader classLoader) throws Exception {
            return new JavascriptCacheObjectBuilder(scriptFile, methodName, searchPaths, classLoader);
        }

        @Override
        protected String getDefaultScriptFile() {
            return "BuildCacheObject.js";
        }

        @Override
        protected String getDefaultMethod() {
            return "buildManifestationInformation";
        }
    }

    private JavascriptCacheObjectBuilder(String scriptFile, String methodName, List<String> searchPaths, ClassLoader classLoader) throws Exception {
        super(scriptFile, methodName, searchPaths, classLoader);
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
        String oldManifestationId = MDC.get("manifestationId");
        try {
            MDC.put("manifestationId", manifestationId);
            Object json = environment.callMethod(methodName, new Object[] {manifestationId, xmlObjects});
            log.trace("json = {}", json);
            return O.readValue(String.valueOf(json), ManifestationInformation.class);
        } finally {
            if (oldManifestationId == null) {
                MDC.remove("manifestationId");
            } else {
                MDC.put("manifestationId", oldManifestationId);
            }
        }
    }
}
