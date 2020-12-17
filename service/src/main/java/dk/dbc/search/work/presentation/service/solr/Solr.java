/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-service
 *
 * work-presentation-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.service.solr;

import dk.dbc.search.work.presentation.service.Config;
import dk.dbc.search.work.presentation.service.vipcore.ProfileService;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dk.dbc.search.work.presentation.service.vipcore.ProfileService.ProfileDomain.*;
import static java.util.Collections.EMPTY_SET;

/**
 * Accessing SolR for profile validation of access to manifestations
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Singleton
@Lock(LockType.READ)
public class Solr {

    private static final Logger log = LoggerFactory.getLogger(Solr.class);

    private static final String WORK_ID_FIELD = "rec.persistentWorkId";
    private static final String MANIFESTATION_ID_FIELD = "rec.manifestationId";

    private static final Pattern ZK = Pattern.compile("zk://([^/]*)(/.*)?/([^/]*)");

    @Inject
    public Config config;

    @Inject
    public ProfileService profileService;

    /**
     * Extract known manifestations for a workId from SolR given a profile
     *
     * @param workId                    The work-id to find manifestations for
     * @param agencyId                  The agency part of the profile
     * @param profile                   The symbolic name of the profile
     * @param maxExpectedManifestations How many manifestations one would expect
     *                                  in the work
     * @param trackingId                Tracking
     * @return collection of visible manifestations
     */
    @CacheResult(cacheName = "solr-manifestations",
                 exceptionCacheName = "solr-manifestations-error")
    @Timed(reusable = true)
    public Set<String> getAccessibleManifestations(@CacheKey String workId, @CacheKey String agencyId, @CacheKey String profile, int maxExpectedManifestations, String trackingId) {
        String filterQuery = profileService.filterQueryFor(SEARCH, agencyId, profile, trackingId);
        String queryString = "{!terms f=\"" + WORK_ID_FIELD + "\"}" + workId;
        Set<String> manifestationIds = new HashSet<>();
        pullSolrManifestations(queryString, filterQuery, maxExpectedManifestations, manifestationIds,
                               "Error requesting manifstationIds from solr for: " + workId);
        return manifestationIds;
    }

    /**
     * Filter manifestationIds of relations to those visible by a given profile
     *
     * @param relationIds collection of manifestationIds
     * @param agencyId    The agency part of the profile
     * @param profile     The symbolic name of the profile
     * @param trackingId  Tracking
     * @return collection of visible manifestations
     */
    @CacheResult(cacheName = "solr-relations",
                 exceptionCacheName = "solr-relations-error")
    @Timed(reusable = true)
    public Set<String> getAccessibleRelations(@CacheKey Set<String> relationIds, @CacheKey String agencyId, @CacheKey String profile, String trackingId) {
        log.info("getAccessibleRelations({})", relationIds);
        if (relationIds.isEmpty())
            return EMPTY_SET;

        String filterQuery = profileService.filterQueryFor(PRESENT, agencyId, profile, trackingId);
        Set<String> manifestationIds = new HashSet<>();
        Iterator<String> relations = relationIds.iterator();

        int max = config.getSolrQuerySize() - filterQuery.length();
        log.debug("max = {}", max);

        while (relations.hasNext()) {
            StringBuilder query = new StringBuilder("{!terms f=\"" + MANIFESTATION_ID_FIELD + "\"}")
                    .append(relations.next());
            int count = 1;
            while (relations.hasNext() && query.length() < max) {
                query.append(",")
                        .append(relations.next());
                count++;
            }
            log.debug("count = {}, query = {}", count, query.toString());
            pullSolrManifestations(query.toString(), filterQuery, count, manifestationIds,
                                   "Error requesting relationIds from solr");
        }
        return manifestationIds;
    }

    private void pullSolrManifestations(String queryString, String filterQuery, int maxExpectedManifestations, Set<String> manifestationIds, String logMessage) throws InternalServerErrorException {
        SolrClient solrClient = config.getSolrClient();
        int requestedRows = 16 + maxExpectedManifestations + maxExpectedManifestations / 16;  // Room in resultset for unexpected manifestations
        try {
            SolrCallback callback = new SolrCallback(manifestationIds);
            SolrQuery query = new SolrQuery(queryString);
            query.addFilterQuery(filterQuery);
            query.add("appId", config.getAppId());
            query.setFields(MANIFESTATION_ID_FIELD);
            int start = 0;
            for (;;) {
                query.setStart(start);
                query.setRows(requestedRows);
                log.debug("query = {}", query);
                solrClient.queryAndStreamResponse(query, callback);
                int rowCount = (int) callback.getRowCount();
                int extraRows = rowCount - start - requestedRows;
                if (extraRows <= 0) {
                    log.debug("Got: {} rows starting at: {}", rowCount - start, start);
                    log.debug("manifestationIds.size() = {}", manifestationIds.size());
                    return;
                }
                log.warn("Got: {} more rows than expected for request starting at: {} expected no more than: {}", extraRows, start, requestedRows);
                log.debug("manifestationIds.size() = {}", manifestationIds.size());
                start += requestedRows; // Move starting point in resultset beyond those seen
                requestedRows = extraRows + 16; // Estimate how many rows to fetch in next loop
            }
        } catch (SolrServerException | IOException ex) {
            log.error(logMessage + ": {}", ex.getMessage());
            log.debug(logMessage, ex);
            throw new InternalServerErrorException();
        }
    }

    /**
     * Make a SolrClient
     *
     * @param solrUrl http or zk url to a SolR
     * @return client
     */
    public static SolrClient makeSolrClient(String solrUrl) {
        Matcher zkMatcher = ZK.matcher(solrUrl);
        if (zkMatcher.matches()) {
            Optional<String> zkChroot = Optional.empty();
            if (zkMatcher.group(2) != null) {
                zkChroot = Optional.of(zkMatcher.group(2));
            }
            List<String> zkHosts = Arrays.asList(zkMatcher.group(1).split(","));
            CloudSolrClient solrClient = new CloudSolrClient.Builder(zkHosts, zkChroot)
                    .build();

            solrClient.setDefaultCollection(zkMatcher.group(3));

            return solrClient;
        } else {
            return new HttpSolrClient.Builder(solrUrl)
                    .build();
        }
    }

    private static class SolrCallback extends StreamingResponseCallback {

        private final Set<String> manifestationIds;
        private long rowCount;

        public SolrCallback(Set<String> manifestationIds) {
            this.manifestationIds = manifestationIds;
        }

        @Override
        public void streamSolrDocument(SolrDocument sd) {
            sd.getFieldValues(MANIFESTATION_ID_FIELD)
                    .stream()
                    .map(String::valueOf)
                    .forEach(manifestationIds::add);
        }

        @Override
        public void streamDocListInfo(long rowCount, long firstRow, Float maxScore) {
            log.debug("rowCount = {}, firstRow = {}, maxScore = {}", rowCount, firstRow, maxScore);
            this.rowCount = rowCount;
        }

        public long getRowCount() {
            return rowCount;
        }
    }

}
