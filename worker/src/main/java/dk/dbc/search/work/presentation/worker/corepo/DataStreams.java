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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class DataStreams {

    private final Set<String> streams;

    public DataStreams(InputStream is) {
        Handler handler = new Handler(is);
        this.streams = Collections.unmodifiableSet(handler.streams);
    }

    public Set<String> getStreams() {
        return streams;
    }

    @Override
    public String toString() {
        return "DataStreams{" + "streams=" + streams + '}';
    }

    private static class Handler extends ElementHandler {

        private final HashSet<String> streams;

        private Handler(InputStream is) {
            this.streams = new HashSet<>();
            parse(is);
        }

        @Override
        public void element(String uri, String localName, Map<String, String> attributes, String characters) {
            if ("datastream".equals(localName)) {
                String dsid = attributes.get("dsid");
                if (dsid != null)
                    streams.add(dsid);
            }
        }
    }
}
