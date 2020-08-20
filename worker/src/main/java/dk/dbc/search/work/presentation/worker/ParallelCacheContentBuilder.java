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
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class ParallelCacheContentBuilder {

    private static final Logger log = LoggerFactory.getLogger(ParallelCacheContentBuilder.class);

    @PersistenceContext(unitName = "workPresentation_PU")
    public EntityManager em;

    @Resource(type = ManagedExecutorService.class)
    public ExecutorService executor;

    /**
     * This requires WorkContains to contain all the data from before the work
     * is
     * deleted
     *
     * @param corepoWorkId Identifier of work to be deleted
     */
    public void deleteCacheForCorepoWorkId(String corepoWorkId) {
        Set<String> manifestationsInDatabase = getManifestationIdsFromDatabase(corepoWorkId);
        manifestationsInDatabase.forEach(manifestationId -> {
            CacheEntity.from(em, manifestationId).delete();
        });
    }

    public void updateCache(WorkTree tree) {
        ArrayList<Future<Runnable>> parallelBuild = new ArrayList<>();

        // Flatten into all builders
        List<CacheContentBuilder> builders = extractActiveCacheContentBuilders(tree);
        // Extract active manifestationId
        Set<String> activeManifestationIds = extractManifestationIds(builders);

        log.debug("activeManifestationIds: {}", activeManifestationIds);

        // All manifestationIds in database for this work
        Set<String> manifestationsInDatabase = getManifestationIdsFromDatabase(tree.getCorepoWorkId());
        log.debug("manifestationsInDatabase: {}", manifestationsInDatabase);

        // Remove those that are active and delete the rest
        manifestationsInDatabase.removeAll(activeManifestationIds);
        log.debug("deletedManifestationIds: {}", manifestationsInDatabase);
        manifestationsInDatabase.forEach(manifestationId -> {
            CacheEntity.from(em, manifestationId).delete();
        });

        builders.forEach(dataBuilder -> {
            if (!dataBuilder.isDeleted()) {
                CacheEntity cacheObj = CacheEntity.from(em, dataBuilder.getManifestationId());
                if (cacheObj.getModified() == null ||
                    cacheObj.getModified().before(dataBuilder.getModified())) {
                    parallelBuild.add(executor.submit(parallelJob(cacheObj, dataBuilder)));
                }
            }
        });
        parallelBuild.forEach(future -> {
            try {
                future.get().run();
            } catch (InterruptedException | ExecutionException ex) {
                log.error("Error building manifestation: {}", ex.getMessage());
                log.debug("Error building manifestation: ", ex);
                throw new RuntimeException("Error building manifestation: " + ex.getMessage(), ex);
            }
        });
    }

    public void updateWorkContains(WorkTree tree) {
        String corepoWorkId = tree.getCorepoWorkId();
        List<CacheContentBuilder> activeCacheContentBuilders = extractActiveCacheContentBuilders(tree);
        Set<String> activeManifestationIds = extractManifestationIds(activeCacheContentBuilders);
        List<WorkContainsEntity> workContains = activeManifestationIds.stream()
                .map(m -> WorkContainsEntity.from(em, corepoWorkId, m))
                .collect(Collectors.toList());
        WorkContainsEntity.updateToList(em, corepoWorkId, workContains);
    }

    private List<CacheContentBuilder> extractActiveCacheContentBuilders(WorkTree tree) {
        return tree.values().stream()
                .flatMap(u -> u.values().stream())
                .flatMap(o -> o.values().stream())
                .filter(builder -> !builder.isDeleted())
                .collect(Collectors.toList());
    }

    private Set<String> extractManifestationIds(List<CacheContentBuilder> builders) {
        return builders.stream()
                .map(CacheContentBuilder::getManifestationId)
                .collect(Collectors.toSet());
    }

    private Set<String> getManifestationIdsFromDatabase(String corepoWorkId) {
        return WorkContainsEntity.listFrom(em, corepoWorkId)
                .stream()
                .map(WorkContainsEntity::getManifestationId)
                .collect(Collectors.toSet());
    }

    private Callable<Runnable> parallelJob(CacheEntity cacheObj, CacheContentBuilder dataBuilder) {
        String manifestationId = cacheObj.getManifestationId();
        log.info("Upserting manifestation: {}", manifestationId);
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (mdc == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(mdc);
                }
                log.info("Generating content for: {}", manifestationId);
                cacheObj.setContent(dataBuilder.generateContent());
                cacheObj.setModified(dataBuilder.getModified());

                return () -> { // This will run in the original thread
                    log.info("Saving content for: {}", manifestationId);
                    cacheObj.save();
                };
            } finally {
                MDC.clear();
            }
        };
    }

}
