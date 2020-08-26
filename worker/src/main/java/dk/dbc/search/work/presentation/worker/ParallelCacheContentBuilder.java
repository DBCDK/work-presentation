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

import dk.dbc.search.work.presentation.javascript.JavascriptCacheObjectBuilder;
import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkContainsEntity;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.worker.pool.QuickPool;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Singleton
@Lock(LockType.READ)
public class ParallelCacheContentBuilder {

    private static final Logger log = LoggerFactory.getLogger(ParallelCacheContentBuilder.class);

    @PersistenceContext(unitName = "workPresentation_PU")
    public EntityManager em;

    @Resource(type = ManagedExecutorService.class)
    public ExecutorService executor;

    @Inject
    Config config;

    @Inject
    CorepoContentServiceConnector corepoContentService;

    QuickPool<JavascriptCacheObjectBuilder> jsWorkers;

    @PostConstruct
    public void init() {
        jsWorkers = new QuickPool<>(JavascriptCacheObjectBuilder.builder()
                .build());
        jsWorkers.setMaxTotal(config.getJsPoolSize());
    }

    /**
     * Deletes all cache entries for a corepoWorkId, in case the object is
     * deleted
     * <p>
     * This requires WorkContains to contain all the data from before the work
     * is deleted
     *
     * @param corepoWorkId Identifier of work to be deleted
     */
    @Timed(reusable = true)
    public void deleteCacheForCorepoWorkId(String corepoWorkId) {
        Set<String> manifestationsInDatabase = getManifestationIdsFromDatabase(corepoWorkId);
        manifestationsInDatabase.forEach(manifestationId -> {
            CacheEntity.from(em, manifestationId).delete();
        });
    }

    /**
     * Save all the parts of the work to cache
     * <p>
     * This checks all the cache objects, pruning those not needed, and
     * (re)building new/updated in parallel
     *
     * @param tree The structure of the entire work
     */
    @Timed(reusable = true)
    public void updateCache(WorkTree tree) {
        ArrayList<Future<Runnable>> parallelBuild = new ArrayList<>();

        // Flatten into all builders
        List<CacheContentBuilder> builders = tree.extractActiveCacheContentBuilders();
        // Extract active manifestationId
        Set<String> activeManifestationIds = tree.extractManifestationIds();

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
                    Callable<Runnable> buildJob = parallelJob(cacheObj, dataBuilder);
                    Future<Runnable> jobExecution = executor.submit(buildJob);
                    parallelBuild.add(jobExecution);
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

    /**
     * Save the manifestation list for the work
     *
     * @param tree The structure of the entire work
     */
    public void updateWorkContains(WorkTree tree) {
        String corepoWorkId = tree.getCorepoWorkId();
        Set<String> activeManifestationIds = tree.extractManifestationIds();
        List<WorkContainsEntity> workContains = activeManifestationIds.stream()
                .map(m -> WorkContainsEntity.from(em, corepoWorkId, m))
                .collect(Collectors.toList());
        WorkContainsEntity.updateToList(em, corepoWorkId, workContains);
    }

    private Set<String> getManifestationIdsFromDatabase(String corepoWorkId) {
        return WorkContainsEntity.listFrom(em, corepoWorkId)
                .stream()
                .map(WorkContainsEntity::getManifestationId)
                .collect(Collectors.toSet());
    }

    /**
     * Build a job that computes the content of a cache entity
     * <p>
     * The result is a chain of code executions, the first is to be run in an
     * executor-service, the second, which interacts with the EmtityManager is
     * to be run in the original transaction.
     *
     * @param cacheObj    The entity that needs content
     * @param dataBuilder The content producer
     * @return Something that is given to an ExecutorService
     */
    private Callable<Runnable> parallelJob(CacheEntity cacheObj, CacheContentBuilder dataBuilder) {
        String manifestationId = cacheObj.getManifestationId();
        log.info("Upserting manifestation: {}", manifestationId);
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return () -> {
            try {
                MDC.setContextMap(mdc == null ? Collections.EMPTY_MAP : mdc);
                log.info("Generating content for: {}", manifestationId);
                ManifestationInformation mi = jsWorkers
                        .valueExec(js -> dataBuilder.generateContent(corepoContentService, js))
                        .value();
                cacheObj.setContent(mi);
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
