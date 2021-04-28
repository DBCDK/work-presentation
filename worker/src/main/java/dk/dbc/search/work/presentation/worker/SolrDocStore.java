/*
 * Copyright (C) 2021 DBC A/S (http://dbc.dk/)
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class SolrDocStore {

    private static final Logger log = LoggerFactory.getLogger(SolrDocStore.class);

    private static final ObjectMapper O = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Inject
    Config config;

    public void queue(String workId, String trackingId) {
        UriBuilder ub = config.getSolrDocStoreQueue();
        if (ub == null) {
            return;
        }
        URI uri = ub
                .build(workId, trackingId);
        log.info("Requeueing by url: {}", uri);

        try (InputStream is = config.getHttpClient()
                .target(uri)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(InputStream.class)) {
            ResponseDTO resp = O.readValue(is, ResponseDTO.class);
            if (!resp.ok) {
                log.error("Error queueing {}: {}", workId, resp.text);
            }
        } catch (IOException ex) {
            log.error("Error processing requeue response from solr-doc-store for url: {}: {}", uri, ex.getMessage());
            log.debug("Error processing requeue response from solr-doc-store for url: {}: ", uri, ex);
            throw new EJBException("Error processing requeue response from solr-doc-store", ex);
        } catch (NotFoundException ex) {
            log.info("Solr-doc-store doesn't know about {}, probably hasn't reached there yet");
        } catch (WebApplicationException ex) {
            log.error("Error getting requeue response from solr-doc-store for url: {}: {}", uri, ex.getMessage());
            log.debug("Error getting requeue response from solr-doc-store for url: {}: ", uri, ex);
            throw new EJBException("Error getting requeue response from solr-doc-store", ex);
        }
    }

    public static class ResponseDTO {

        public boolean ok;
        public String text;
    }

}
