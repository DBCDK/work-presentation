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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.JerseyClientBuilder;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class BeanFactory implements AutoCloseable {

    private final EntityManager entityManager;
    private final DataSource corepoDataSource;
    private final Config config;
    private final Bean<ParallelCacheContentBuilder> parallelCacheContentBuilder = new Bean<>(new ParallelCacheContentBuilder(), this::setupParallelCacheContentBuilder);
    private final Bean<CorepoContentServiceConnector> corepoContentService = new Bean<>(new CorepoContentServiceConnector(), this::setupCorepoContentService);
    private final Bean<ObjectTimestamp> objectTimestamp = new Bean<>(new ObjectTimestamp(), this::setupObjectTimestamp);
    private final Bean<PresentationObjectBuilder> presentationObjectBuilder = new Bean<>(new PresentationObjectBuilder(), this::setupPresentationObjectBuilder);
    private final Bean<WorkConsolidator> workConsolidator = new Bean<>(new WorkConsolidator(), this::setupWorkConsolidator);
    private final Bean<Worker> worker = new Bean<>(new Worker(), this::setupWorker);
    private final Bean<WorkTreeBuilder> workTreeBuilder = new Bean<>(new WorkTreeBuilder(), this::setupWorkTreeBuilder);
    private final ArrayList<Runnable> cleanup = new ArrayList<>();

    public BeanFactory(Map<String, String> envs, EntityManager em, DataSource corepoDataSource) {
        this.entityManager = em;
        this.corepoDataSource = corepoDataSource;
        this.config = makeConfig(envs);
    }

    @Override
    public void close() throws Exception {
        cleanup.forEach(Runnable::run);
    }

    private static Config makeConfig(Map<String, String> envs) {
        Map<String, String> env = new HashMap<>();
        env.putAll(config("COREPO_CONTENT_SERVICE_URL=" + System.getenv("COREPO_CONTENT_SERVICE_URL"),
                          "JPA_POSTPONE=5s-10s",
                          "JS_POOL_SIZE=2",
                          "SYSTEM_NAME=test",
                          "THREADS=1",
                          "QUEUES=queue",
                          "QUEUE_DEDUPLICATION=true")); // Default settings
        env.putAll(envs);
        Config config = new Config(env) {
            @Override
            protected ClientBuilder clientBuilder() {
                return JerseyClientBuilder.newBuilder();
            }
        };
        config.init();
        return config;
    }

    private static Map<String, String> config(String... envs) {
        return Arrays.stream(envs)
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

    Config getConfig() {
        return config;
    }

    public BeanFactory withPresentationObjectBuilder(PresentationObjectBuilder pob) {
        presentationObjectBuilder.set(pob);
        return this;
    }

    public ParallelCacheContentBuilder getParallelCacheContentBuilder() {
        return parallelCacheContentBuilder.get();
    }

    private void setupParallelCacheContentBuilder(ParallelCacheContentBuilder bean) {
        bean.em = entityManager;
        bean.config = config;
        bean.corepoContentService = corepoContentService.get();
        bean.init();
        cleanup.add(bean::destroy);
    }

    public BeanFactory withCorepoContentServiceConnector(CorepoContentServiceConnector ccsc) {
        corepoContentService.set(ccsc);
        return this;
    }

    public CorepoContentServiceConnector getCorepoContentService() {
        return corepoContentService.get();
    }

    private void setupCorepoContentService(CorepoContentServiceConnector bean) {
        bean.config = config;
    }

    public ObjectTimestamp getObjectTimestamp() {
        return objectTimestamp.get();
    }

    public void setupObjectTimestamp(ObjectTimestamp bean) {
        bean.em = entityManager;
    }


    public PresentationObjectBuilder getPresentationObjectBuilder() {
        return presentationObjectBuilder.get();
    }

    private void setupPresentationObjectBuilder(PresentationObjectBuilder bean) {
        bean.parallelCacheContentBuilder = parallelCacheContentBuilder.get();
        bean.workConsolidator = workConsolidator.get();
        bean.workTreeBuilder = workTreeBuilder.get();
    }

    public WorkConsolidator getWorkConsolidator() {
        return workConsolidator.get();
    }

    private void setupWorkConsolidator(WorkConsolidator bean) {
        bean.em = entityManager;
    }

    public Worker getWorker() {
        return worker.get();
    }

    private void setupWorker(Worker bean) {
        bean.config = config;
        bean.executor = Executors.newCachedThreadPool();
        bean.dataSource = corepoDataSource;
        bean.metrics = null;
        bean.presentationObjectBuilder = presentationObjectBuilder.get();
    }

    public BeanFactory withWorkTreeBuilder(WorkTreeBuilder bean) {
        workTreeBuilder.set(bean);
        return this;
    }

    public WorkTreeBuilder getWorkTreeBuilder() {
        return workTreeBuilder.get();
    }

    private void setupWorkTreeBuilder(WorkTreeBuilder bean) {
        bean.contentService = corepoContentService.get();
        bean.em = entityManager;
    }

    private static class Bean<T> {

        private final Consumer<T> setup;
        private T that;
        private boolean callSetup;

        public Bean(T that, Consumer<T> setup) {
            this.setup = setup;
            this.that = that;
            this.callSetup = true;
        }

        private T get() {
            if (callSetup) {
                // This needs to be set before .accept()
                // If two classes has mutual injection, you'll end up with
                // infinite loop and stack overflow
                callSetup = false;
                setup.accept(that);
            }
            return that;
        }

        private void set(T t) {
            that = t;
            callSetup = true;
        }
    }

}
