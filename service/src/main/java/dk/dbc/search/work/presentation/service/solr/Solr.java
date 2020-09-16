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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class Solr {

    private static final Logger log = LoggerFactory.getLogger(Solr.class);

    private static final String WORK_ID_FIELD = "rec.persistentWorkId";
    private static final String MANIFESTATION_ID = "rec.manifestationId";

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
    @Timed(reusable = true)
    public Set<String> getAccessibleManifestations(String workId, String agencyId, String profile, int maxExpectedManifestations, String trackingId) {
        String fileterQuery = profileService.filterQueryFor(agencyId, profile, trackingId);
        SolrClient solrClient = config.getSolrClient();
        int requestedRows = 16 + maxExpectedManifestations + maxExpectedManifestations / 16;  // Room in resultset for unexpected manifestations
        try {
            HashSet<String> manifestationIds = new HashSet<>();
            SolrCallback callback = new SolrCallback(manifestationIds);
            SolrQuery query = new SolrQuery(WORK_ID_FIELD + ":" + ClientUtils.escapeQueryChars(workId));
            query.addFilterQuery(fileterQuery);
            query.add("appId", config.getAppId());
            query.setFields(MANIFESTATION_ID);
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
                    return manifestationIds;
                }
                log.warn("Got: {} more rows than expected for request starting at: {} expteced no more that: {}", extraRows, start, requestedRows);
                log.debug("manifestationIds.size() = {}", manifestationIds.size());
                start += requestedRows; // Move starting point in resultset beyond those seen
                requestedRows = extraRows + 16; // Estimate how many rows to fetch in next loop
            }
        } catch (SolrServerException | IOException ex) {
            log.error("Error requesting manifstationIds from solr for: {}: {}", workId, ex.getMessage());
            log.debug("Error requesting manifstationIds from solr for: {}: ", workId, ex);
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

        private final HashSet<String> manifestationIds;
        private long rowCount;

        public SolrCallback(HashSet<String> manifestationIds) {
            this.manifestationIds = manifestationIds;
        }

        @Override
        public void streamSolrDocument(SolrDocument sd) {
            sd.getFieldValues(MANIFESTATION_ID)
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
