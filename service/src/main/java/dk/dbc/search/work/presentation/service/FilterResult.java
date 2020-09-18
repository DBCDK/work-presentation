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
package dk.dbc.search.work.presentation.service;

import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import dk.dbc.search.work.presentation.service.response.ManifestationInformationResponse;
import dk.dbc.search.work.presentation.service.response.WorkInformationResponse;
import dk.dbc.search.work.presentation.service.solr.Solr;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class FilterResult {

    private static final Logger log = LoggerFactory.getLogger(FilterResult.class);

    @Inject
    public Solr solr;

    /**
     * Prepare/rewrite work information to presentation format
     * <p>
     * Tasks:
     * <p>
     * Filter what is visible to the user (TODO)
     * <p>
     * Flatten the unit to manifestation tree
     *
     * @param work       the work as stored in the database
     * @param agencyId   the 1st part of the filter specification
     * @param profile    The 2nd part of the filter specification
     * @param trackingId The tracking id for the request
     * @return the work as presented to the user
     */
    public WorkInformationResponse processWork(WorkInformation work, String agencyId, String profile, String trackingId) {
        log.debug("work = {}", work);
        int manifestationCount = work.dbUnitInformation.values()
                .stream()
                .mapToInt(Set::size)
                .sum();
        Set<String> visibleManifestations = solr.getAccessibleManifestations(work.workId, agencyId, profile, manifestationCount, trackingId);

        // Filtered manifests
        Map<String, Set<ManifestationInformationResponse>> dbUnitInformation =
                work.dbUnitInformation.entrySet().stream()
                        .map(e -> new AbstractMap.SimpleEntry<>(
                                e.getKey(),
                                e.getValue().stream()
                                        .filter(m -> visibleManifestations.contains(m.manifestationId))
                                        .map(ManifestationInformationResponse::from)
                                        .collect(Collectors.toSet())))
                        .filter(e -> !e.getValue().isEmpty())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.debug("dbUnitInformation = {}", dbUnitInformation);

        WorkInformationResponse wir = WorkInformationResponse.from(work);
        // Flatten the manifestations
        wir.records = dbUnitInformation.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        return wir;
    }
}
