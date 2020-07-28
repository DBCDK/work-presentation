/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-api
 *
 * work-presentation-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.api.jpa;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Embeddable
public class WorkContainsKey implements Serializable {

    private static final long serialVersionUID = 0xd8d58d111e49fff2L;

    @Column(updatable = false, insertable = false, nullable = false)
    private String corepoWorkId;

    @Column(updatable = false, insertable = false, nullable = false)
    private String manifestationId;

    protected WorkContainsKey() {
    }

    public WorkContainsKey(String corepoWorkId, String manifestationId) {
        this.corepoWorkId = corepoWorkId;
        this.manifestationId = manifestationId;
    }

    public String getCorepoWorkId() {
        return corepoWorkId;
    }

    public String getManifestationId() {
        return manifestationId;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.corepoWorkId);
        hash = 67 * hash + Objects.hashCode(this.manifestationId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final WorkContainsKey other = (WorkContainsKey) obj;
        return Objects.equals(this.corepoWorkId, other.corepoWorkId) &&
               Objects.equals(this.manifestationId, other.manifestationId);
    }

    @Override
    public String toString() {
        return "CacheKey{" + "corepoWorkId=" + corepoWorkId + ", manifestationId=" + manifestationId + '}';
    }
}
