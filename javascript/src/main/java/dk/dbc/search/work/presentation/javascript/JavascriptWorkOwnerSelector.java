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

import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JavascriptWorkOwnerSelector extends AbstractJavascript {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends JavascriptBuilder<JavascriptWorkOwnerSelector> {

        @Override
        protected JavascriptWorkOwnerSelector create(String scriptFile, String methodName, List<String> searchPaths, ClassLoader classLoader) throws Exception {
            return new JavascriptWorkOwnerSelector(scriptFile, methodName, searchPaths, classLoader);
        }

        @Override
        protected String getDefaultScriptFile() {
            return "SelectWorkOwner.js";
        }

        @Override
        protected String getDefaultMethod() {
            return "selectWorkOwner";
        }
    }

    private JavascriptWorkOwnerSelector(String scriptFile, String methodName, List<String> searchPaths, ClassLoader classLoader) throws Exception {
        super(scriptFile, methodName, searchPaths, classLoader);
    }

    public String selectOwner(Map<String, ManifestationInformation> map) throws Exception {
        Object obj = environment.callMethod(methodName, new Object[] {map});
        return String.valueOf(obj);
    }
}
