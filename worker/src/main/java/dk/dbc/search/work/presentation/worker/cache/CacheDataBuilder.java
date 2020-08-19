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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Data structure for information about cache entries
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class CacheDataBuilder {

    public static final String LOCAL_DATA = "localData.";
    private static final int LOCAL_DATA_LEN = LOCAL_DATA.length();

//    private final String corepoId;
//    private final String localStream;
    private final boolean deleted;
    private final String manifestationId;
    private final Timestamp modified;

    public CacheDataBuilder(String corepoId, String localStream, Instant modified, boolean deleted) {
        this(corepoId, localStream, Timestamp.from(modified), deleted);
    }

    public CacheDataBuilder(String corepoId, String localStream, Timestamp modified, boolean deleted) {
        if (!localStream.startsWith(LOCAL_DATA)) {
            throw new IllegalStateException("Trying to build a CacheDataBuilder for stream: " + localStream);
        }
//        this.corepoId = corepoId;
//        this.localStream = localStream;
        this.modified = modified;
        this.deleted = deleted;
        this.manifestationId = localStream.substring(LOCAL_DATA_LEN) +
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

    @Override
    public String toString() {
        return "CacheDataBuilder{" + "manifestationId=" + manifestationId + ", deleted=" + deleted + ", modified=" + modified + '}';
    }
}
