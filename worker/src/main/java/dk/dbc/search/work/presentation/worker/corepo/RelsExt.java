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
import java.util.AbstractMap;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Pojo to represent a CorepoContentService response for the Rels-Ext stream on
 * a unit
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class RelsExt {

    private final EnumMap<RelsExtType, List<String>> map;

    public RelsExt(InputStream is) {
        SimpleFields fields = new SimpleFields(is);
        Map<RelsExtType, List<String>> tempMap = fields.getFields()
                .entrySet()
                .stream()
                .map(e -> new AbstractMap.SimpleEntry<>(RelsExtType.from(e.getKey()), Collections.unmodifiableList(e.getValue())))
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (tempMap.isEmpty()) {
            this.map = new EnumMap<>(RelsExtType.class);
        } else {
            this.map = new EnumMap<>(tempMap);
        }
    }

    public List<String> get(RelsExtType key) {
        return map.getOrDefault(key, Collections.EMPTY_LIST);
    }

    public void forEach(BiConsumer<RelsExtType, List<String>> action) {
        map.entrySet().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    public String toString() {
        return "RelsExt{" + "map=" + map + '}';
    }
}
