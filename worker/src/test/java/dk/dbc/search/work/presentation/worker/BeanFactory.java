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

import com.github.tomakehurst.wiremock.WireMockServer;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;
import javax.ws.rs.client.ClientBuilder;
import org.eclipse.microprofile.metrics.Counter;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class BeanFactory implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(BeanFactory.class);

    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource corepoDataSource;
    private final Config config;
    private final Bean<AsyncCacheContentBuilder> asyncCacheContentBuilder = new Bean<>(new AsyncCacheContentBuilderMock(), this::setupAsyncCacheContentBuilder);
    private final Bean<CorepoContentServiceConnector> corepoContentService = new Bean<>(new CorepoContentServiceConnector(), this::setupCorepoContentService);
    private final Bean<JavaScriptEnvironment> javaScriptEnvironment = new Bean<>(new JavaScriptEnvironment(), this::setupJavaScriptEnvironment);
    private final Bean<ObjectTimestamp> objectTimestamp = new Bean<>(new ObjectTimestamp(), this::setupObjectTimestamp);
    private final Bean<PresentationObjectBuilder> presentationObjectBuilder = new Bean<>(new PresentationObjectBuilder(), this::setupPresentationObjectBuilder);
    private final Bean<WorkConsolidator> workConsolidator = new Bean<>(new WorkConsolidator(), this::setupWorkConsolidator);
    private final Bean<Worker> worker = new Bean<>(new Worker(), this::setupWorker);
    private final Bean<WorkTreeBuilder> workTreeBuilder = new Bean<>(new WorkTreeBuilder(), this::setupWorkTreeBuilder);

    public BeanFactory(Map<String, String> envs, EntityManager em, EntityManagerFactory emf, DataSource corepoDataSource, WireMockServer wms) {
        this.entityManager = em;
        this.entityManagerFactory = emf;
        this.corepoDataSource = corepoDataSource;
        this.config = makeConfig(envs, wms);
    }

    @Override
    public void close() {
    }

    private static Config makeConfig(Map<String, String> envs, WireMockServer wms) {
        Map<String, String> env = new HashMap<>();
        env.putAll(config("COREPO_CONTENT_SERVICE_URL=" + wms.url("/corepo-content-service"),
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

    public AsyncCacheContentBuilder getAsyncCacheContentBuilder() {
        return asyncCacheContentBuilder.get();
    }

    private void setupAsyncCacheContentBuilder(AsyncCacheContentBuilder bean) {
        bean.em = entityManager;
        bean.jsEnv = getJavaScriptEnvironment();
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

    public JavaScriptEnvironment getJavaScriptEnvironment() {
        return javaScriptEnvironment.get();
    }

    private void setupJavaScriptEnvironment(JavaScriptEnvironment bean) {
        bean.config = config;
        bean.corepoContentService = getCorepoContentService();
        bean.init();
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
        bean.workConsolidator = getWorkConsolidator();
        bean.workTreeBuilder = getWorkTreeBuilder();
        bean.corepoContent = getCorepoContentService();
        bean.successes = new MockCounter();
    }

    public BeanFactory withPresentationObjectBuilder(PresentationObjectBuilder pob) {
        presentationObjectBuilder.set(pob);
        return this;
    }

    public WorkConsolidator getWorkConsolidator() {
        return workConsolidator.get();
    }

    private void setupWorkConsolidator(WorkConsolidator bean) {
        bean.em = entityManager;
        bean.asyncCacheContentBuilder = getAsyncCacheContentBuilder();
        bean.jsEnv = getJavaScriptEnvironment();
    }

    public Worker getWorker() {
        return worker.get();
    }

    private void setupWorker(Worker bean) {
        bean.config = config;
        bean.executor = Executors.newCachedThreadPool();
        bean.dataSource = corepoDataSource;
        bean.metrics = null;
        bean.presentationObjectBuilder = getPresentationObjectBuilder();
    }

    public BeanFactory withWorkTreeBuilder(WorkTreeBuilder bean) {
        workTreeBuilder.set(bean);
        return this;
    }

    public WorkTreeBuilder getWorkTreeBuilder() {
        return workTreeBuilder.get();
    }

    private void setupWorkTreeBuilder(WorkTreeBuilder bean) {
        bean.contentService = getCorepoContentService();
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

    /**
     * This wraps a function in a transaction
     * <p>
     * This is intended to help running things that are annotated with
     * {@code
     * @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     * }
     *
     * @param <R>   Return value type
     * @param scope the function
     * @return value from the scope
     */
    <R> R newTransaction(Function<EntityManager, R> scope) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                R ret = scope.apply(em);
                transaction.commit();
                return ret;
            } catch (Exception err) {
                if (transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (Exception e) {
                        log.error("Error rolling back transaction: {}", e.getMessage());
                        log.debug("Error rolling back transaction: ", e);
                    }
                }
                if (err instanceof RuntimeException)
                    throw (RuntimeException) err;
                throw new RuntimeException(err);
            }
        } finally {
            em.close();
            entityManagerFactory.getCache().evictAll();
        }
    }

    class AsyncCacheContentBuilderMock extends AsyncCacheContentBuilder {

        @Override
        public Future<ManifestationInformation> getFromCache(CacheContentBuilder dataBuilder, Map<String, String> mdc, boolean delete) {
            return newTransaction(em -> {
                this.em = em;
                return super.getFromCache(dataBuilder, mdc, delete);
            });
        }
    }

    private static class MockCounter implements Counter {

        private long cnt;

        public MockCounter() {
            this.cnt = 0;
        }

        @Override
        public void inc() {
            cnt++;
        }

        @Override
        public void inc(long n) {
            cnt += n;
        }

        @Override
        public long getCount() {
            return cnt;
        }
    }
}
