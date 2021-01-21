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

import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkContainsEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkObjectEntity;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.api.pojo.RelationInformation;
import dk.dbc.search.work.presentation.api.pojo.SeriesInformation;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.ejb.EJBException;
import javax.inject.Inject;
import org.slf4j.MDC;

import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.*;

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

    @Inject
    AsyncCacheContentBuilder asyncCacheContentBuilder;

    /**
     * Remove a work record from the database
     *
     * @param corepoWorkId corepo-work-id of the work
     */
    @Timed(reusable = true)
    public void deleteWork(String corepoWorkId) {
        WorkObjectEntity work = WorkObjectEntity.fromCorepoWorkId(em, corepoWorkId);
        if (work != null) {
            List<WorkContainsEntity> oldWorkContainsList = WorkContainsEntity.listFrom(em, corepoWorkId);
            oldWorkContainsList
                    .stream()
                    .map(WorkContainsEntity::getManifestationId)
                    .forEach(manifestationId -> {
                        CacheEntity.from(em, manifestationId).delete();
                    });
            WorkContainsEntity.updateToList(em, corepoWorkId, EMPTY_LIST);
            work.delete();
        }
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

        setWorkContains(tree);

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

        Map<String, ManifestationInformation> manifestationCache = buildManifestationCache(tree);

        work.workId = tree.getPersistentWorkId();

        // Copy from owner
        String ownerId = tree.getPrimaryManifestationId();
        ManifestationInformation primary = manifestationCache.get(ownerId);
        if (primary == null) {
            throw new IllegalStateException("primary: " + ownerId + " could not be resolved");
        }
        work.creators = TypedValue.distinctSet(primary.creators);
        work.description = primary.description;
        work.fullTitle = primary.fullTitle;
        work.series = null;
        work.title = primary.title;

        // Map into unit->manifestations and unit->relationType->relationManifestation
        work.dbUnitInformation = new HashMap<>();
        work.dbRelUnitInformation = new HashMap<>();

        HashMap<SeriesInformation, AtomicInteger> allSI = new HashMap<>();

        Set<TypedValue> subjects = new HashSet<>();
        tree.forEach((unitId, unit) -> {
            List<ManifestationInformation> fullManifestations = unit.values().stream() // All ObjectTree from a unit
                    .map(ObjectTree::values) // Find manifestationIds
                    .flatMap(Collection::stream) // as a stream of manifestation references
                    .map(CacheContentBuilder::getManifestationId)
                    .map(manifestationCache::get) // Lookup manifestation data
                    .filter(notNull()) // Not deleted
                    .collect(toList());

            fullManifestations.forEach(m -> {
                if (m.series != null)
                    allSI.computeIfAbsent(m.series, k -> new AtomicInteger()).incrementAndGet();
            });

            fullManifestations.stream()
                    .map(m -> m.subjects) // as a stream of Set<String>
                    .filter(notNull())
                    .flatMap(Collection::stream) // as a stream of String
                    .forEach(subjects::add);

            Set<ManifestationInformation> manifestations = fullManifestations.stream()
                    .map(ManifestationInformation::onlyPresentationFields)
                    .collect(toSet());
            work.dbUnitInformation.put(unitId, manifestations);

            HashSet<RelationInformation> relationsForUnit = new HashSet<>();
            unit.getRelations().forEach(tr -> {
                tree.getRelations().get(tr).values().stream()
                        .map(ObjectTree::values) // Find manifestationIds
                        .flatMap(Collection::stream) // as a stream of manifestation references
                        .map(CacheContentBuilder::getManifestationId)
                        .map(manifestationCache::get) // Lookup manifestation data
                        .filter(notNull()) // Not deleted
                        .map(RelationInformation.mapperWith(tr.getType().getName()))
                        .map(RelationInformation::onlyPresentationFields)
                        .forEach(relationsForUnit::add);
            });

            work.dbRelUnitInformation.put(unitId, relationsForUnit);
        });

        work.series = findSeriesInformation(allSI, primary.series);
        work.subjects = TypedValue.distinctSet(subjects);

        return work;
    }

    /**
     * Find series information
     *
     * @param allSI Map of series-information -> number-of-instances
     * @param primary which to prioritize in case of equal usage
     * @return a series-information for the work
     */
    private SeriesInformation findSeriesInformation(HashMap<SeriesInformation, AtomicInteger> allSI, SeriesInformation primary) {
        // Set series information
        if (allSI.isEmpty()) {
            return null;
        } else {
            log.debug("allSI = {}", allSI);
            List<Map.Entry<SeriesInformation, AtomicInteger>> inOrder = allSI.entrySet().stream()
                    .sorted((l, r) -> Integer.compare(r.getValue().get(), l.getValue().get()))
                    .collect(toList());
            int highValue = inOrder.get(0).getValue().get();
            List<SeriesInformation> relevant = inOrder.stream()
                    .filter(e -> e.getValue().get() == highValue)
                    .map(Map.Entry::getKey)
                    .collect(toList());
            log.debug("relevant = {}", relevant);
            if (relevant.size() > 1 && relevant.contains(primary)) {
                log.debug("using from primary = {}", primary);
                return primary;
            } else {
                SeriesInformation ret = relevant.get(0);
                log.debug("work.series = {}", ret);
                return ret;
            }
        }
    }

    /**
     * Construct a cache, with all the documents used by this tree
     * <p>
     * This tries to build all cache objects, even if one fails, it keeps going
     * on, failing at the very end. Trying to put every object into the cache,
     * so that, during next run cache build collision errors are less likely
     *
     * @param tree the tree structure
     * @return map of manifestation-id to content
     */
    Map<String, ManifestationInformation> buildManifestationCache(WorkTree tree) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();

        ManifestationCollection manifestationCollection = new ManifestationCollection();

        // All manifestations as future ManiInfo (delete should be removed from cache)
        tree.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(cacheContentBuilder -> asyncCacheContentBuilder.getFromCache(cacheContentBuilder, mdc, true))
                .collect(toList())
                .forEach(manifestationCollection::include);

        // All distinct relations as future ManiInfo (deletes should be retained in cache)
        tree.getRelations().values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(toMap(CacheContentBuilder::getManifestationId, c -> c, (t1, t2) -> t1)) // Distinct by manifestationId
                .values()
                .stream()
                .map(cacheContentBuilder -> asyncCacheContentBuilder.getFromCache(cacheContentBuilder, mdc, false))
                .collect(toList())
                .forEach(manifestationCollection::include);

        return manifestationCollection.getManifestations();
    }

    /**
     * Given a tree, set the workcontains list, and remove orphaned cache
     * entries
     *
     * @param tree current work description
     */
    void setWorkContains(WorkTree tree) {
        String corepoWorkId = tree.getCorepoWorkId();
        Set<String> activeManifestationIds = tree.extractManifestationIds();

        WorkContainsEntity.listFrom(em, corepoWorkId).stream()
                .map(WorkContainsEntity::getManifestationId)
                .filter(m -> !activeManifestationIds.contains(m))
                .forEach(m -> CacheEntity.from(em, m).delete());

        List<WorkContainsEntity> workContains = activeManifestationIds.stream()
                .map(m -> WorkContainsEntity.from(em, corepoWorkId, m))
                .collect(toList());
        WorkContainsEntity.updateToList(em, corepoWorkId, workContains);
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

    private static <T> Predicate<T> notNull() {
        return o -> o != null;
    }

    private static class ManifestationCollection {

        private final HashMap<String, ManifestationInformation> manifestations = new HashMap<>();
        private EJBException exception = null;

        /**
         * Extract manifestation from future and store the result (optionally an
         * exception)
         *
         * @param future Where to get the manifestation
         */
        private void include(Future<ManifestationInformation> future) {
            try {
                ManifestationInformation maniInfo = future.get();
                if (maniInfo != null) {
                    manifestations.put(maniInfo.manifestationId, maniInfo);
                }
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof EJBException) {
                    exception = (EJBException) ex.getCause();
                } else {
                    exception = new EJBException("Could not build cache entry", ex);
                }
            } catch (InterruptedException ex) {
                exception = new EJBException("Could not build cache entry", ex);
            }
        }

        /**
         * Get the manifestations or throw an exception
         * <p>
         * If a manifestation resulted in an exception that is raised
         *
         * @return manifestations by id
         */
        public HashMap<String, ManifestationInformation> getManifestations() {
            if (exception != null)
                throw exception;
            return manifestations;
        }
    }
}
