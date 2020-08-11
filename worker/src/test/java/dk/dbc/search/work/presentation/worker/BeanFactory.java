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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.JerseyClientBuilder;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class BeanFactory {

    private final EntityManager em;
    private final DataSource wpDataSource;
    private final DataSource coDataSource;
    private final Config config;
    private final Bean<PresentationObjectBuilder> presentationObjectBuilder = new Bean<>(this::makePresentationObjectBuilder);
    private final Bean<Worker> worker = new Bean<>(this::makeWorker);

    public BeanFactory(EntityManager em, DataSource wpDataSource, DataSource coDataSource, String... envs) {
        this.em = em;
        this.wpDataSource = wpDataSource;
        this.coDataSource = coDataSource;
        this.config = makeConfig(envs);
    }

    private static Config makeConfig(String... envs) {
        Map<String, String> env = new HashMap<>();
        env.putAll(config("COREPO_CONTENT_SERVICE_URL=" + System.getenv("COREPO_CONTENT_SERVICE_URL"),
                          "SYSTEM_NAME=test",
                          "THREADS=1",
                          "QUEUES=queue",
                          "QUEUE_DEDUPLICATION=true")); // Default settings
        env.putAll(config(envs));
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

    public Config getConfig() {
        return config;
    }

    public PresentationObjectBuilder getPresentationObjectBuilder() {
        return presentationObjectBuilder.get();
    }

    public void setPresentationObjectBuilder(PresentationObjectBuilder pob) {
        presentationObjectBuilder.set(preparePresentationObjectBuilder(pob));
    }

    private PresentationObjectBuilder makePresentationObjectBuilder() {
        return preparePresentationObjectBuilder(new PresentationObjectBuilder());
    }

    private PresentationObjectBuilder preparePresentationObjectBuilder(PresentationObjectBuilder pob) {
        return pob;
    }

    public Worker getWorker() {
        return worker.get();
    }

    private Worker makeWorker() {
        Worker workerBean = new Worker();
        workerBean.config = config;
        workerBean.executor = Executors.newCachedThreadPool();
        workerBean.dataSource = coDataSource;
        workerBean.metrics = null;
        workerBean.presentationObjectBuilder = presentationObjectBuilder.get();
        return workerBean;
    }

    private static class Bean<T> {

        private final Supplier<T> supplier;
        private T that;

        public Bean(Supplier<T> supplier) {
            this.supplier = supplier;
            this.that = null;
        }

        private T get() {
            if (that == null)
                that = supplier.get();
            return that;
        }

        private void set(T t) {
            that = t;
        }
    }
}
