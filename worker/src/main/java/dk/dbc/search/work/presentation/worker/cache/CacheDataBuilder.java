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
package dk.dbc.search.work.presentation.worker.cache;

import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Data structure for information about cache entries
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class CacheDataBuilder {

    private final String corepoId;
    private final String localStream;
    private final boolean deleted;
    private final String manifestationId;
    private final Timestamp modified;

    public CacheDataBuilder(String corepoId, String localStream, Instant modified, boolean deleted) {
        this(corepoId, localStream, Timestamp.from(modified), deleted);
    }

    public CacheDataBuilder(String corepoId, String localStream, Timestamp modified, boolean deleted) {
        this.corepoId = corepoId;
        this.localStream = localStream;
        this.modified = modified;
        this.deleted = deleted;
        this.manifestationId = localStream.substring(localStream.indexOf('.') + 1) +
                               ":" +
                               corepoId.substring(corepoId.indexOf(':') + 1);
    }

    public String getManifestationId() {
        return manifestationId;
    }

    public Timestamp getModified() {
        return modified;
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Produces information about this manifestation for storing in the cache
     *
     * @return Manifestation Object
     */
    public ManifestationInformation generateContent() {
        ManifestationInformation result = new ManifestationInformation();
        result.manifestationId = manifestationId;
        return result;
    }
}
