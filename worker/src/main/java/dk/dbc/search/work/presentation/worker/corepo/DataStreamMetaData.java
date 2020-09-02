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
import java.time.Instant;
import java.util.Map;

/**
 * Pojo to represent a CorepoContentService response for the metadata of a
 * datastream
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class DataStreamMetaData {

    private final String id;
    private final Instant created;
    private final boolean active;

    public DataStreamMetaData(InputStream is) {
        Handler handler = new Handler(is);
        if (handler.id == null)
            throw new IllegalArgumentException("Datastream data is not complete - missing pid");
        if (handler.created == null)
            throw new IllegalArgumentException("Datastream data is not complete - missing created");
        if (handler.active == null)
            throw new IllegalArgumentException("Datastream data is not complete - missing active");
        this.id = handler.id;
        this.created = handler.created;
        this.active = handler.active;
    }

    public String getId() {
        return id;
    }

    // This is name created for legacy reasons, but is modified
    public Instant getCreated() {
        return created;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "DataStreamMetaData{" + "id=" + id + ", created=" + created + ", active=" + active + '}';
    }

    private static class Handler extends ElementHandler {

        String id = null;
        Instant created = null;
        Boolean active = null;

        private Handler(InputStream is) {
            parse(is);
        }

        @Override
        public void element(String uri, String localName, Map<String, String> attributes, String characters) {
            switch (localName) {
                case "datastreamProfile":
                    id = attributes.get("pid");
                    break;
                case "dsCreateDate":
                    created = parseTimeStamp(characters);
                    break;
                case "dsState":
                    active = "A".equals(characters);
                    break;
                default:
                    break;
            }
        }
    }
}
