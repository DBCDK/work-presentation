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

import dk.dbc.search.work.presentation.worker.tree.UnitTree;
import dk.dbc.search.work.presentation.worker.tree.ObjectTree;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import dk.dbc.search.work.presentation.worker.corepo.DataStreamMetaData;
import dk.dbc.search.work.presentation.worker.corepo.ObjectMetaData;
import dk.dbc.search.work.presentation.worker.corepo.RelsExt;
import dk.dbc.search.work.presentation.worker.corepo.RelsSys;
import dk.dbc.search.work.presentation.worker.tree.RelationTree;
import dk.dbc.search.work.presentation.worker.tree.TypedRelation;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract the structure of the work/units/records for a CORepo work
 *
 * @author Thomas Pii (thp@dbc.dk)
 */
@Stateless
public class WorkTreeBuilder {

    private static final Logger log = LoggerFactory.getLogger(WorkTreeBuilder.class);

    @Inject
    CorepoContentServiceConnector contentService;

    @PersistenceContext(unitName = "workPresentation_PU")
    EntityManager em;

    /**
     * This builds a work structure
     *
     * @param work "work:*" id (not validated)
     * @return a tree representation of the work
     */
    @Timed(reusable = true)
    public WorkTree buildTree(String work) {
        ObjectMetaData workMetaData = contentService.objectMetaData(work);
        log.trace("work = {}", workMetaData);
        WorkTree tree = new WorkTree(work, workMetaData.getModified());
        if (workMetaData.isActive()) {
            buildWorkTree(tree, work);
        }
        return tree;
    }

    private void buildWorkTree(WorkTree workTree, String work) throws IllegalStateException {
        RelsSys workRelsSys = contentService.relsSys(work);
        log.trace("workRelsSys = {}", workRelsSys);
        workRelsSys.getChildren()
                .forEach(unit -> workTree.put(unit, buildUnitTree(unit)));
        HashSet<TypedRelation> relationUnits = new HashSet<>();
        workTree.values().forEach(unit -> relationUnits.addAll(unit.getRelations()));
        relationUnits.forEach(tr -> {
            RelationTree relTree = buildRelationTree(tr);
            workTree.addRelation(tr, relTree);
        });
    }

    private UnitTree buildUnitTree(String unit) throws IllegalStateException {
        ObjectMetaData unitMetaData = contentService.objectMetaData(unit);
        if (!unitMetaData.isActive()) {
            throw new IllegalStateException("Unit: " + unit + " is deleted but part of rels-sys");
        }
        RelsSys unitRelsSys = contentService.relsSys(unit);
        Instant unitTs = unitMetaData.getModified();
        UnitTree unitTree = new UnitTree(unitTs);

        unitRelsSys.getChildren()
                .forEach(object -> unitTree.put(object, buildObjectTree(object)));

        RelsExt unitRelsExt = contentService.relsExt(unit);
        if (unitRelsExt != null) {
            unitRelsExt.forEach((type, ids) -> {
                if (type.isPresentable()) {
                    ids.stream()
                            .filter(s -> s.startsWith("unit:")) // This can be removed when corepo has been cleaned up for bad relations
                            .filter(contentService::objectIsActive) // This is a hack, because some relations haven't been removed when they were deleted
                            .forEach(r -> unitTree.addRelation(type, r));
                }
            });
        }
        return unitTree;
    }

    private ObjectTree buildObjectTree(String object) throws IllegalStateException {
        ObjectMetaData objectMetaData = contentService.objectMetaData(object);
        if (!objectMetaData.isActive()) {
            throw new IllegalStateException("Object: " + object + " is deleted but part of rels-sys");
        }
        RelsSys objectRelsSys = contentService.relsSys(object);
        ObjectTree objectTree = new ObjectTree(objectRelsSys.isPrimary(), objectMetaData.getModified());

        Map<String, DataStreamMetaData> streamMetaDatas = contentService.datastreams(object).getStreams()
                .stream()
                .collect(Collectors.toMap(stream -> stream,
                                          stream -> contentService.datastreamMetaData(object, stream)));

        Stream.Builder<Instant> timestamps = Stream.builder();
        timestamps.accept(objectMetaData.getModified());
        DataStreamMetaData dc = streamMetaDatas.get("DC");
        if (dc != null) {
            timestamps.accept(dc.getCreated());
        }
        DataStreamMetaData commonData = streamMetaDatas.get("commonData");
        if (commonData == null) {
            throw new IllegalStateException("Object: " + object + " has no commonData stream");
        }
        timestamps.accept(commonData.getCreated());
        Instant sharedDataModified = timestamps.build()
                .reduce(WorkTreeBuilder::latestOf)
                .orElse(Instant.MIN);

        contentService.datastreams(object).getStreams().forEach(stream -> {
            if (stream.startsWith(CacheContentBuilder.LOCAL_DATA)) {
                DataStreamMetaData streamMetaData = streamMetaDatas.get(stream);
                Instant streamModified = latestOf(sharedDataModified, streamMetaData.getCreated());
                objectTree.put(stream, new CacheContentBuilder(object, stream, streamModified, !streamMetaData.isActive()));
            }
        });
        String objectDefaultStream = CacheContentBuilder.LOCAL_DATA + object.substring(0, object.indexOf(':'));
        if (!objectTree.containsKey(objectDefaultStream)) {
            log.info("object: {} has no localData stream for owner", object);
            objectTree.put("commonData", new CacheContentBuilder(object, sharedDataModified, false));
        }
        if(objectTree.values().stream().allMatch(ccb -> ccb.isDeleted())) {
            throw new IllegalStateException("Object: " + object + " is not deleted but has no live datastreams");
        }

        return objectTree;
    }

    private RelationTree buildRelationTree(TypedRelation entry) throws IllegalStateException {
        String unit = entry.getUnit();
        ObjectMetaData unitMetaData = contentService.objectMetaData(unit);
        if (!unitMetaData.isActive()) {
            throw new IllegalStateException("Unit: " + unit + " is deleted but part of rels-sys");
        }

        RelsSys unitRelsSys = contentService.relsSys(unit);
        RelationTree relsTree = new RelationTree(entry.getType());

        unitRelsSys.getChildren()
                .forEach(object -> relsTree.put(object, buildObjectTree(object)));

        return relsTree;
    }

    private static Instant latestOf(Instant first, Instant second) {
        return first.isAfter(second) ? first : second;
    }
}
