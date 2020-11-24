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

import dk.dbc.search.work.presentation.worker.corepo.RelsExtType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.Objects;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class RelationTree extends HashMap<String, ObjectTree> {

    private static final long serialVersionUID = 0xBE9CEC397287462FL;

    private final RelsExtType type;

    public RelationTree(RelsExtType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || getClass() != obj.getClass())
            return false;
        final RelationTree other = (RelationTree) obj;
        return this.type == other.type;
    }

    @Override
    public String toString() {
        return "RelationTree{" + "type=" + type + ", " + super.toString() + '}';
    }
}
