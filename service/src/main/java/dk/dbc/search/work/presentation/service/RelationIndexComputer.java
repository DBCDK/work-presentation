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

import dk.dbc.search.work.presentation.api.pojo.RelationInformation;
import dk.dbc.search.work.presentation.service.response.RelationInformationResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class RelationIndexComputer {

    private static final Logger log = LoggerFactory.getLogger(RelationIndexComputer.class);
    private static final int[] NO_RELATION_LIST = new int[0];

    private final List<RelationInformationResponse> relationList;
    private final Map<String, int[]> unitRelationIndexes;

    public RelationIndexComputer(Map<String, Set<RelationInformation>> unitRelations) {
        relationList = unitRelations.values().stream()
                .flatMap(Collection::stream)
                .map(RelationInformationResponse::from)
                .collect(Collectors.toSet()) //uniq
                .stream()
                .sorted()
                .collect(Collectors.toList());

        int i = 0;
        Map<RelationInformationResponse, Integer> indexes = new HashMap<>();
        for (RelationInformationResponse relInfo : relationList) {
            indexes.put(relInfo, i++);
        }

        log.trace("relationList = {}", relationList);
        log.trace("indexes = {}", indexes);
        log.trace("unitRelations = {}", unitRelations);

        unitRelationIndexes = unitRelations.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          e -> e.getValue().stream()
                                                  .map(RelationInformationResponse::from)
                                                  .mapToInt(indexes::get)
                                                  .sorted()
                                                  .toArray()));
    } //uniq

    public List<RelationInformationResponse> getRelationList() {
        return relationList;
    }

    public int[] unitRelationIndexes(String unitId) {
        return unitRelationIndexes.getOrDefault(unitId, NO_RELATION_LIST);
    }
}
