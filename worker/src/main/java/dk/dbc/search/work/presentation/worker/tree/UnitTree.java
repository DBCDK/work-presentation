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
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * Pojo to represent the unit level of a work tree, when extracting from corepo
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class UnitTree extends HashMap<String, ObjectTree> {

    private static final long serialVersionUID = 0xD3F195C554AB12ACL;

    private final boolean primary;
    private final Instant modified;
    private final HashSet<TypedRelation> relations;

    public UnitTree(boolean primary, Instant modified) {
        this.primary = primary;
        this.modified = modified;
        this.relations = new HashSet<>();
    }

    public boolean isPrimary() {
        return primary;
    }

    public Instant getModified() {
        return modified;
    }

    public HashSet<TypedRelation> getRelations() {
        return relations;
    }

    public void addRelation(RelsExtType type, String relationUnitId) {
        relations.add(new TypedRelation(type, relationUnitId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), primary, modified, relations);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || getClass() != obj.getClass())
            return false;
        final UnitTree other = (UnitTree) obj;
        return this.primary == other.primary &&
               Objects.equals(this.modified, other.modified) &&
               Objects.equals(this.relations, other.relations);
    }

    @Override
    public String toString() {
        return "UnitTree{" + "primary=" + primary + ", modified=" + modified + ", " + super.toString() + ", relations=" + relations + '}';
    }
}
