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
import dk.dbc.search.work.presentation.service.response.GroupInformationResponse;
import dk.dbc.search.work.presentation.service.response.WorkInformationResponse;
import dk.dbc.search.work.presentation.service.solr.Solr;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.*;

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
     * Filter what is visible to the user
     * <p>
     * Flatten the unit to manifestation tree
     *
     * @param corepoWorkId     The work id as needed to limit access to its
     *                         parts
     * @param work             The work as stored in the database
     * @param agencyId         The 1st part of the filter specification
     * @param profile          The 2nd part of the filter specification
     * @param includeRelations If relations should be included in the answer
     * @param trackingId       The tracking id for the request
     * @return the work as presented to the user
     */
    @Timed
    public WorkInformationResponse processWork(String corepoWorkId, WorkInformation work, String agencyId, String profile, boolean includeRelations, String trackingId) {
        log.debug("work = {}", work);

        WorkInformationResponse wir = WorkInformationResponse.from(work);

        int manifestationCount = work.dbUnitInformation.values()
                .stream()
                .mapToInt(Set::size)
                .sum();
        Set<String> visibleManifestations = solr.getAccessibleManifestations(corepoWorkId, agencyId, profile, manifestationCount, trackingId);

        // Filtered manifests
        Map<String, GroupInformationResponse> groupInformation =
                work.dbUnitInformation.entrySet().stream()
                        .map(e -> new AbstractMap.SimpleEntry<>(
                                e.getKey(),
                                e.getValue().stream()
                                        .filter(m -> visibleManifestations.contains(m.manifestationId))
                                        .map(ManifestationInformationResponse::from)
                                        .collect(toSet())))
                        .filter(e -> !e.getValue().isEmpty())
                        .collect(toMap(Map.Entry::getKey, e -> GroupInformationResponse.with(e.getValue())));

        if (groupInformation.isEmpty()) {
            throw new ForbiddenException("No manifestations visible to profile");
        }

        if (includeRelations) {
            Set<String> possibleRelations = work.dbRelUnitInformation.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(r -> r.manifestationId)
                    .collect(toSet());
            Set<String> accessibleRelations = solr.getAccessibleRelations(possibleRelations, agencyId, profile, trackingId);

            RelationIndexComputer relationIndexes = new RelationIndexComputer(accessibleRelations, work.dbRelUnitInformation);
            groupInformation.forEach((unitId, group) -> {
                int[] indexes = relationIndexes.unitRelationIndexes(unitId);
                log.debug("Adding relations for unit {}, relations = {}", unitId, indexes);
                group.relations = indexes;
            });

            wir.relations = relationIndexes.getRelationList();
        }

        Stream<String> ownerStream = Stream.empty();
        if (work.ownerUnitId != null && groupInformation.containsKey(work.ownerUnitId)) {
            ownerStream = Stream.of(work.ownerUnitId);
        }

        List<GroupInformationResponse> groups = Stream.concat(
                ownerStream,
                groupInformation.keySet().stream()
                        .filter(unit -> !unit.equals(work.ownerUnitId))
                        .sorted(new NaturalSort()))
                .map(groupInformation::get)
                .collect(toList());

        wir.groups = groups;
        log.debug("wir = {}", wir);
        log.debug("groups = {}", groups);

        return wir;
    }
}
