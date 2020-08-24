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

import dk.dbc.search.work.presentation.JavascriptCacheObjectBuilder;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.worker.CorepoContentServiceConnector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;

/**
 * Data structure for information about cache entries
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class CacheContentBuilder {

    public static final String LOCAL_DATA = "localData.";
    private static final int LOCAL_DATA_LEN = LOCAL_DATA.length();

    private final String corepoId;
    private final String localStream;
    private final boolean deleted;
    private final String manifestationId;
    private final Instant modified;

    public CacheContentBuilder(String corepoId, String localStream, Instant modified, boolean deleted) {
        if (!localStream.startsWith(LOCAL_DATA)) {
            throw new IllegalStateException("Trying to build a CacheDataBuilder for stream: " + localStream);
        }
        this.corepoId = corepoId;
        this.localStream = localStream;
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
        return Timestamp.from(modified);
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Produces information about this manifestation for storing in the cache
     *
     * @param corepoContentService Where to extract data streams from
     * @param js                   the JavaScript abstraction
     * @return Manifestation Object
     * @throws Exception Threwn by JavaScript engine
     */
    public ManifestationInformation generateContent(CorepoContentServiceConnector corepoContentService, JavascriptCacheObjectBuilder js) throws Exception {
        HashMap<String, String> dataStreams = new HashMap<String, String>() {
            {
                put("localData", corepoContentService.datastreamContent(corepoId, localStream));
                put("commonData", corepoContentService.datastreamContent(corepoId, "commonData"));
                put("DC", corepoContentService.datastreamContent(corepoId, "DC"));
            }
        };
        return js.extractManifestationInformation(manifestationId, dataStreams);
    }

    @Override
    public String toString() {
        return "CacheContentBuilder{" + "manifestationId=" + manifestationId + ", corepoId=" + corepoId + ", localStream=" + localStream + ", deleted=" + deleted + ", modified=" + modified + '}';
    }

}
