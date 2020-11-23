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
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class TypedRelation implements Serializable {

    private static final long serialVersionUID = 0x045EC0BE89AF5B34L;

    private final RelsExtType type;
    private final String unit;

    public TypedRelation(RelsExtType type, String unit) {
        this.type = type;
        this.unit = unit;
    }

    public RelsExtType getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final TypedRelation other = (TypedRelation) obj;
        return this.type == other.type &&
               Objects.equals(this.unit, other.unit);
    }

    @Override
    public String toString() {
        return "TypedRelation{" + "type=" + type + ", relationUnit=" + unit + '}';
    }
}
