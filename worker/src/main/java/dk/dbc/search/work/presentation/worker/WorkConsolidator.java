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
import dk.dbc.search.work.presentation.api.jpa.RecordEntity;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
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
import java.text.Normalizer;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        RecordEntity entity = RecordEntity.fromCorepoWorkId(em, corepoWorkId);
        if (entity != null)
            entity.delete();
    }

    /**
     * Save a work record to the database
     * <p>
     * The work is represented by a {@link RecordEntity}
     *
     * @param corepoWorkId corepo-work-id of the work
     * @param tree         The structure of the entire work
     * @param content      The record content
     */
    @Timed(reusable = true)
    public void saveWork(String corepoWorkId, WorkTree tree, WorkInformation content) {
        String persistentWorkId = tree.getPersistentWorkId();
        RecordEntity record = RecordEntity.from(em, persistentWorkId);
        RecordEntity recordByWorkId = RecordEntity.fromCorepoWorkId(em, corepoWorkId);
        log.debug("record = {}, recordByWorkId = {}", record, recordByWorkId);
        if (recordByWorkId != null && !recordByWorkId.getPersistentWorkId().equals(persistentWorkId)) {
            log.info("Moved from persistent-work-id: {} to {}", recordByWorkId.getPersistentWorkId(), persistentWorkId);
            recordByWorkId.delete();
        }
        record.setCorepoWorkId(corepoWorkId);
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
        record.setModified(Timestamp.from(modified));
        record.setContent(content);
        record.save();
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
        work.creators = primary.creators;
        work.description = primary.description;
        work.fullTitle = primary.fullTitle;
        work.title = primary.title;

        // Map into unit->manifestations
        work.dbUnitInformation = new HashMap<>();
        tree.forEach((unitId, unit) -> {
            Set<ManifestationInformation> manifestations = unit.values().stream() // All ObjectTree from a unit
                    .map(ObjectTree::values) // Find manifestationIds
                    .flatMap(Collection::stream) // as a stream of manifestations
                    .filter(m -> !m.isDeleted()) // only those not deleted
                    .map(CacheContentBuilder::getManifestationId)
                    .map(this::getCacheContentFor) // Lookup manifestation data
                    .collect(Collectors.toSet());
            work.dbUnitInformation.put(unitId, manifestations);
        });

        // Collect all subjects
        work.subjects = work.dbUnitInformation.values()
                .stream() // Stream of Set<ManifestationInformation>
                .filter(WorkConsolidator::notNull)
                .flatMap(Collection::stream) // as a stream of ManifestationInformation
                .filter(WorkConsolidator::notNull)
                .map(m -> m.subjects) // as a stream of Set<String>
                .filter(WorkConsolidator::notNull)
                .flatMap(Collection::stream) // as a stream of String
                .collect(Collectors.toSet());
        // Make them distinct (ignoring case)
        work.subjects = noCaseSet(work.subjects);

        tree.forEach((unitId, unit) -> {
            Set<ManifestationInformation> manifestations = work.dbUnitInformation.get(unitId)
                .stream()
                .map(ManifestationInformation::onlyPresentationFields)
                .collect(Collectors.toSet());
            work.dbUnitInformation.put(unitId, manifestations);
        });

        return work;
    }

    /**
     * Convert a collection into a set
     * <p>
     * Prefer the 1st value that is not lowercase
     *
     * @param subjects collection of strings
     * @return set of strings (case insensitive compare)
     */
    static Set<String> noCaseSet(Collection<String> subjects) {
        HashMap<String, String> collector = new HashMap<>();
        subjects.forEach(subject -> {
            String normalized = Normalizer.normalize(subject, Normalizer.Form.NFC);
            String key = normalized.toLowerCase(Locale.ROOT);
            collector.compute(key, (k, v) ->
                              v != null && // a value exists and
                              !v.equals(k) ? // It is not lowercase (as the key is)
                              v : // use existing value
                              normalized); // use new value
        });
        return new HashSet<>(collector.values());
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
            throw new IllegalStateException("Got null content for: " + id);
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
}
