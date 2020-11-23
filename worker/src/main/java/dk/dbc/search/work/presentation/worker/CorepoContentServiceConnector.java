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
package dk.dbc.search.work.presentation.worker;

import dk.dbc.search.work.presentation.worker.corepo.DataStreamMetaData;
import dk.dbc.search.work.presentation.worker.corepo.DataStreams;
import dk.dbc.search.work.presentation.worker.corepo.ObjectMetaData;
import dk.dbc.search.work.presentation.worker.corepo.RelsExt;
import dk.dbc.search.work.presentation.worker.corepo.RelsSys;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class CorepoContentServiceConnector {

    private static final Logger log = LoggerFactory.getLogger(CorepoContentServiceConnector.class);

    @Inject
    public Config config;

    /**
     * Fetch a RELS-SYS stream from corepo-content-service
     *
     * @param id the repository-id
     * @return parsed RELS-SYS
     */
    @Timed(reusable = true)
    public RelsSys relsSys(String id) {
        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{id}/datastreams/RELS-SYS/content")
                .build(id);
        log.debug("Fetching {} to RelsSys object", id);
        return callUrl(uri, RelsSys::new, false);
    }

    /**
     * Fetch a RELS-EXT stream from corepo-content-service
     *
     * @param id the repository-id
     * @return parsed RELS-EXT or null if not found
     */
    @Timed(reusable = true)
    public RelsExt relsExt(String id) {
        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{id}/datastreams/RELS-EXT/content")
                .build(id);
        log.debug("Fetching {} to RelsExt object", id);
        return callUrl(uri, RelsExt::new, true);
    }

    /**
     * Fetch a metadata of an object (work/unit/obj) from corepo-content-service
     * <p>
     * Timestamps and active/deleted
     *
     * @param id the repository-id
     * @return parsed Metadata
     */
    @Timed(reusable = true)
    public ObjectMetaData objectMetaData(String id) {
        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{id}")
                .build(id);
        log.debug("Fetching {} to ObjectMetaData object", id);
        return callUrl(uri, ObjectMetaData::new, false);
    }

    /**
     * Fetch a metadata of a datastream from corepo-content-service
     * <p>
     * Timestamp and active/deleted
     *
     * @param id     the repository-id
     * @param stream the stream of the repositoryId
     * @return parsed Metadata
     */
    @Timed(reusable = true)
    public DataStreamMetaData datastreamMetaData(String id, String stream) {
        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{id}/datastreams/{stream}")
                .build(id, stream);
        log.debug("Fetching {}/{} to DataStreamMetaData object", id, stream);
        return callUrl(uri, DataStreamMetaData::new, false);
    }

    /**
     * Fetch a list of a datastream for an object (work/unit/obj) from
     * corepo-content-service
     *
     * @param id the repository-id
     * @return list of stream names
     */
    @Timed(reusable = true)
    public DataStreams datastreams(String id) {
        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{id}/datastreams")
                .build(id);
        log.debug("Fetching {} to DataStreams object", id);
        return callUrl(uri, DataStreams::new, false);
    }

    /**
     * Fetch a content of a datastream from corepo-content-service
     *
     * @param id     the repository-id
     * @param stream the stream of the repositoryId
     * @return XML content as a String
     */
    @Timed(reusable = true)
    public String datastreamContent(String id, String stream) {
        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{id}/datastreams/{stream}/content")
                .build(id, stream);
        log.debug("Fetching {}/{} to String", id, stream);
        return callUrl(uri, is -> IOUtils.toString(is, UTF_8), false);
    }

    @FunctionalInterface
    private interface Callback<R> {

        R call(InputStream is) throws IOException;
    }

    private <R> R callUrl(URI uri, Callback<R> callback, boolean nullOn404) {
        try (InputStream is = config.getHttpClient()
                .target(uri)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(InputStream.class)) {
            return callback.call(is);
        } catch (WebApplicationException | IOException ex) {
            if (ex instanceof NotFoundException && nullOn404) {
                return null;
            }
            log.error("Error requesting {}: {}", uri, ex.getMessage());
            log.debug("Error requesting {}: ", uri, ex);
            throw new RuntimeException("Error requesting: " + uri + ": " + ex.getMessage(), ex);
        }
    }
}
