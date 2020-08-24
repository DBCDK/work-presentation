/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-worker
 *
 * work-presentation-worker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-worker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker.corepo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects all elements with only text into a map from tags localName to the
 * text value(s) (a list for repeated tags)
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
class SimpleFields extends ElementHandler {

    private final Map<String, List<String>> fields;

    SimpleFields(InputStream is) {
        this.fields = new HashMap<>();
        parse(is);
    }

    @Override
    public void element(String uri, String localName, Map<String, String> attributes, String characters) {
        if (characters != null)
            fields.computeIfAbsent(localName, k -> new ArrayList<>()).add(characters);
    }

    public String getFirst(String key) {
        List<String> list = fields.get(key);
        if (list == null)
            return null;
        return list.get(0);
    }

    public List<String> getList(String key) {
        return Collections.unmodifiableList(fields.getOrDefault(key, Collections.EMPTY_LIST));
    }

    public Map<String, List<String>> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public String toString() {
        return "RelsSys{" + "fields=" + fields + '}';
    }

}
