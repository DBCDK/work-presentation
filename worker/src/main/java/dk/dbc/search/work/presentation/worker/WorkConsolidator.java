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

import dk.dbc.search.work.presentation.worker.tree.NoCacheObjectException;
import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkObjectEntity;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.api.pojo.TypedValue;
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import dk.dbc.search.work.presentation.worker.tree.ObjectTree;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.InternalServerErrorException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 *
 * This class assembles all the cached parts into a consolidated work response,
 * prime for filtering in the web-service
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class WorkConsolidator {

    private static final Logger log = LoggerFactory.getLogger(WorkConsolidator.class);

    @PersistenceContext(unitName = "workPresentation_PU")
    public EntityManager em;

    /**
     * Remove a work record from the database
     *
     * @param corepoWorkId corepo-work-id of the work
     */
    @Timed(reusable = true)
    public void deleteWork(String corepoWorkId) {
        WorkObjectEntity work = WorkObjectEntity.fromCorepoWorkId(em, corepoWorkId);
        if (work != null)
            work.delete();
    }

    /**
     * Save a work record to the database
     * <p>
     * The work is represented by a {@link WorkObjectEntity}
     *
     * @param corepoWorkId corepo-work-id of the work
     * @param tree         The structure of the entire work
     * @param content      The record content
     */
    @Timed(reusable = true)
    public void saveWork(String corepoWorkId, WorkTree tree, WorkInformation content) {
        String persistentWorkId = tree.getPersistentWorkId();
        WorkObjectEntity work = WorkObjectEntity.from(em, persistentWorkId);
        WorkObjectEntity workByWorkId = WorkObjectEntity.fromCorepoWorkId(em, corepoWorkId);
        log.debug("record = {}, recordByWorkId = {}", work, workByWorkId);
        if (workByWorkId != null && !workByWorkId.getPersistentWorkId().equals(persistentWorkId)) {
            log.info("Moved from persistent-work-id: {} to {}", workByWorkId.getPersistentWorkId(), persistentWorkId);
            workByWorkId.delete();
        }
        work.setCorepoWorkId(corepoWorkId);
        Stream.Builder<Instant> builder = Stream.builder();
        builder.accept(tree.getModified());
        tree.values().forEach(unitTree -> {
            builder.accept(unitTree.getModified());
            unitTree.values().forEach(objTree -> {
                builder.accept(objTree.getModified());
            });
        });
        Instant modified = builder.build().max(WorkConsolidator::instantCmp)
                .orElseThrow(() -> new InternalServerErrorException("Could not extract modified from tree of " + corepoWorkId));
        work.setModified(Timestamp.from(modified));
        work.setContent(content);
        work.save();
    }

    /**
     * Consolidate all manifestations into a work-record
     * <p>
     * This contains the sum of all the information that can be given from the
     * web-service. Which will then filter the content before presentation.
     *
     * @param tree The structure of the entire work
     * @return Work record
     */
    @Timed(reusable = true)
    public WorkInformation buildWorkInformation(WorkTree tree) {
        WorkInformation work = new WorkInformation();

        work.workId = tree.getPersistentWorkId();

        // Copy from owner
        String ownerId = tree.getPrimaryManifestationId();
        ManifestationInformation primary = getCacheContentFor(ownerId);
        work.creators = TypedValue.distinctSet(primary.creators);
        work.description = primary.description;
        work.fullTitle = primary.fullTitle;
        work.title = primary.title;

        // Map into unit->manifestations and unit->relationType->relationManifestation
        work.dbUnitInformation = new HashMap<>();
        work.relUnitTypeInformation = new HashMap<>();

        Set<TypedValue> subjects = new HashSet<>();
        tree.forEach((unitId, unit) -> {
            List<ManifestationInformation> fullManifestations = unit.values().stream() // All ObjectTree from a unit
                    .map(ObjectTree::values) // Find manifestationIds
                    .flatMap(Collection::stream) // as a stream of manifestation references
                    .filter(not(CacheContentBuilder::isDeleted)) // only those not deleted
                    .map(CacheContentBuilder::getManifestationId)
                    .map(this::getCacheContentFor)
                    .collect(toList()); // Lookup manifestation data
            fullManifestations.stream()
                .map(m -> m.subjects) // as a stream of Set<String>
                .filter(WorkConsolidator::notNull)
                .flatMap(Collection::stream) // as a stream of String
                    .forEach(subjects::add);

            Set<ManifestationInformation> manifestations = fullManifestations.stream()
                    .map(ManifestationInformation::onlyPresentationFields)
                    .collect(Collectors.toSet());
            work.dbUnitInformation.put(unitId, manifestations);

            HashMap<String, Set<ManifestationInformation>> relationsForUnit = new HashMap<>();
            unit.getRelations().forEach(tr -> {
                Set<ManifestationInformation> relationManifestation = relationsForUnit.computeIfAbsent(tr.getType().getName(), t -> new HashSet<>());
                tree.getRelations().get(tr).values().stream()
                        .map(ObjectTree::values) // Find manifestationIds
                        .flatMap(Collection::stream) // as a stream of manifestation references
                        .filter(not(CacheContentBuilder::isDeleted)) // only those not deleted
                        .map(CacheContentBuilder::getManifestationId)
                        .map(this::getCacheContentFor) // Lookup manifestation data
                        .map(ManifestationInformation::onlyRelationPresentationFields)
                        .forEach(relationManifestation::add);
            });

            work.relUnitTypeInformation.put(unitId, relationsForUnit);

        });

        work.subjects = TypedValue.distinctSet(subjects);

        return work;
    }

    /**
     * Cache fetching (mockable)
     *
     * @param id manifestation id, to fetch cached data for
     * @return ManifestationInformation
     */
    ManifestationInformation getCacheContentFor(String id) {
        ManifestationInformation content = CacheEntity.from(em, id).getContent();
        if (content == null)
            throw new NoCacheObjectException(id);
        return content;
    }

    private static int instantCmp(Instant a, Instant b) {
        int epochDiff = truncateLongToInt(a.getEpochSecond() - b.getEpochSecond());
        if (epochDiff != 0)
            return epochDiff;
        return a.getNano() - b.getNano();
    }

    private static int truncateLongToInt(long l) {
        if (l < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        if (l > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int) l;
    }

    private static boolean notNull(Object o) {
        return o != null;
    }

    private static <T> Predicate<T> not(Predicate<T> p) {
        return t -> !p.test(t);
    }
}
