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
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pojo to represent a CorepoContentService response for the rels-sys stream of
 * an object
 * <p>
 * This makes the object/unit/work child/parent/primary uniform - it is supplied
 * with different names from each type of record
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class RelsSys {

    private static final Logger log = LoggerFactory.getLogger(RelsSys.class);

    private final String parent;
    private final List<String> children;
    private final boolean primary;
    private final String id;

    public RelsSys(InputStream is) {
        SimpleFieldsWithId fields = new SimpleFieldsWithId(is);
        this.id = fields.getId();
        if (id == null) {
            throw new IllegalStateException("Rels-Sys doesn't contain id of record");
        }
        log.trace("fields = {}", fields);
        if (id.startsWith("unit:")) {
            this.parent = fields.getFirst("isMemberOfWork");
            this.children = fields.getList("hasMemberOfUnit");
            this.primary = fields.getFirst("isPrimaryUnitObjectFor") != null;
        } else if (id.startsWith("work:")) {
            this.parent = null;
            this.children = fields.getList("hasMemberOfWork");
            this.primary = true;
        } else {
            this.parent = fields.getFirst("isMemberOfUnit");
            this.children = Collections.EMPTY_LIST;
            this.primary = fields.getFirst("isPrimaryBibObjectFor") != null;
        }
    }

    public String getId() {
        return id;
    }

    public String getParent() {
        return parent;
    }

    public List<String> getChildren() {
        return children;
    }

    public boolean isPrimary() {
        return primary;
    }

    @Override
    public String toString() {
        return "RelsSysUnit{" + "id=" + id + ", parent=" + parent + ", children=" + children + ", primary=" + primary + '}';
    }

    private static class SimpleFieldsWithId extends SimpleFields {

        private String id;

        public SimpleFieldsWithId(InputStream in) {
            super(in);
        }

        @Override
        public void element(String uri, String localName, Map<String, String> attributes, String characters) {
            if ("Description".equals(localName)) {
                String about = attributes.get("about");
                if (about != null) {
                    id = about.substring(about.indexOf('/') + 1);
                }
            }
            super.element(uri, localName, attributes, characters);
        }

        public String getId() {
            return id;
        }

    }

}
