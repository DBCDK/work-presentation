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
package dk.dbc.search.work.presentation.worker;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Cache object to deduplicate calls to corepo-content-service, when multiple
 * units in the same work are referred by the same relations.
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
class CorepoCache {

    private final ConcurrentHashMap<URI, Object> objects;

    CorepoCache() {
        this.objects = new ConcurrentHashMap<>();
    }

    // As Map::computeIfAbsent
    byte[] computeIfAbsent(URI key, Function<URI, byte[]> mappingFunction) {
        Object value = objects.computeIfAbsent(key, k -> {
                                           try {
                                               return mappingFunction.apply(k);
                                           } catch (RuntimeException ex) {
                                               return ex;
                                           }
                                       });
        if (value instanceof byte[])
            return (byte[]) value;
        if (value instanceof RuntimeException)
            throw (RuntimeException) value;
        throw new IllegalStateException("This cannot happen");
    }

    // As Map::clear
    void clear() {
        objects.clear();
    }

}
