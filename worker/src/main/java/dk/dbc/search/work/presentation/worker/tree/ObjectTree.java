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
package dk.dbc.search.work.presentation.worker.tree;

import java.time.Instant;
import java.util.HashMap;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ObjectTree extends HashMap<String, CacheContentBuilder> {

    private static final long serialVersionUID = 0x4976FB455503CDEEL;

    private final boolean primary;
    private final Instant modified;

    public ObjectTree(boolean primary, Instant modified) {
        this.primary = primary;
        this.modified = modified;
    }

    public boolean isPrimary() {
        return primary;
    }

    public Instant getModified() {
        return modified;
    }

    @Override
    public String toString() {
        return "ObjectTree{" + "primary=" + primary + ", modified=" + modified + ", " + super.toString() + '}';
    }
}
