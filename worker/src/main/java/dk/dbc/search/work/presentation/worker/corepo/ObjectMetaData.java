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
 * Pojo to represent a CorepoContentService response for the metadata of an
 * object
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ObjectMetaData {

    private final String id;
    private final Instant created;
    private final Instant modified;
    private final boolean active;

    public ObjectMetaData(InputStream is) {
        Handler handler = new Handler(is);
        if (handler.id == null)
            throw new IllegalArgumentException("Object data is not complete - missing pid");
        if (handler.created == null)
            throw new IllegalArgumentException("Object data is not complete - missing created");
        if (handler.modified == null)
            throw new IllegalArgumentException("Object data is not complete - missing modified");
        if (handler.active == null)
            throw new IllegalArgumentException("Object data is not complete - missing active");
        this.id = handler.id;
        this.created = handler.created;
        this.modified = handler.modified;
        this.active = handler.active;
    }

    public String getId() {
        return id;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getModified() {
        return modified;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "ObjectMetaData{" + "id=" + id + ", created=" + created + ", modified=" + modified + ", active=" + active + '}';
    }

    private static class Handler extends ElementHandler {

        String id = null;
        Instant created = null;
        Instant modified = null;
        Boolean active = null;

        private Handler(InputStream is) {
            parse(is);
        }

        @Override
        public void element(String uri, String localName, Map<String, String> attributes, String characters) {
            switch (localName) {
                case "objectProfile":
                    id = attributes.get("pid");
                    break;
                case "objCreateDate":
                    created = parseTimeStamp(characters);
                    break;
                case "objLastModDate":
                    modified = parseTimeStamp(characters);
                    break;
                case "objState":
                    active = "A".equals(characters);
                    break;
                default:
                    break;
            }
        }
    }
}
