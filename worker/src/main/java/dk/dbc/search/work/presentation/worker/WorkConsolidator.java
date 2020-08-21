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
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.stream.Stream;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.InternalServerErrorException;
import org.eclipse.microprofile.metrics.annotation.Timed;

/**
 *
 * This class assembles all the cached parts into a consolidated work response,
 * prime for filtering in the web-service
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class WorkConsolidator {

    @PersistenceContext(unitName = "workPresentation_PU")
    public EntityManager em;

    /**
     * Remove a work record from the database
     *
     * @param corepoWorkId corepo-work-id of the work
     */
    @Timed
    public void deleteWork(String corepoWorkId) {
        RecordEntity.fromCorepoWorkId(em, corepoWorkId)
                .ifPresent(RecordEntity::delete);
    }

    /**
     * Save a to the database
     *
     * @param corepoWorkId corepo-work-id of the work
     * @param tree         The structure of the entire work
     * @param content      The record content
     */
    @Timed
    public void saveWork(String corepoWorkId, WorkTree tree, WorkInformation content) {
        RecordEntity record = RecordEntity.from(em, tree.getPersistentWorkId());
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
                .orElseThrow(() -> new InternalServerErrorException("Cound not extrace modified from tree of " + corepoWorkId));
        record.setModified(Timestamp.from(modified));
        record.setContent(content);
        record.save();
    }

    /**
     * Consolidate all manifestations into a work-record
     * <p>
     * This contains at sum of all the information that can be given from the
     * web-service. Which will then filter the content before presentation.
     *
     * @param tree The structure of the entire work
     * @return Work record
     */
    @Timed
    public WorkInformation buildWorkInformation(WorkTree tree) {
        WorkInformation work = new WorkInformation();
        ManifestationInformation primary = CacheEntity.from(em, tree.getPrimaryManifestationId())
                .getContent();
        work.creator = primary.creator;
        work.description = primary.description;
        work.fullTitle = primary.fullTitle;
        work.subjects = primary.subjects; // TODO accumulate all menifestations subjects
        work.title = primary.title;
        work.workId = tree.getPersistentWorkId();
        return work;
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
}
