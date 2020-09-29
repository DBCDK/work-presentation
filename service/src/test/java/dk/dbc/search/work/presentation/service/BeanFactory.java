/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-service
 *
 * work-presentation-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.service;

import dk.dbc.search.work.presentation.service.solr.Solr;
import dk.dbc.search.work.presentation.service.vipcore.ProfileService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

    private final EntityManager em;
    private final DataSource dataSource;
    private final Config config;

    private final Bean<WorkPresentationBean> wpb = new Bean<>(new WorkPresentationBean(), this::setupWorkPresentationBean);
    private final Bean<FilterResult> fr = new Bean<>(new FilterResult(), this::setupFilterResult);
    private final Bean<ProfileService> ps = new Bean<>(new ProfileService(), this::setupProfileService);
    private final Bean<Solr> solr = new Bean<>(new Solr(), this::setupSolr);

    public BeanFactory(Map<String, String> envs, EntityManager em, DataSource dataSource) {
        this.em = em;
        this.dataSource = dataSource;
        this.config = makeConfig(envs);
    }

    @Override
    public void close() throws Exception {
    }

    private static Config makeConfig(Map<String, String> envs) {
        Map<String, String> env = new HashMap<>();
        env.putAll(config("COREPO_SOLR_URL=" + System.getenv("COREPO_SOLR_URL"),
                          "VIP_CORE_URL=" + System.getenv("VIP_CORE_URL"),
                          "SOLR_APPID=" + "WorkPresentationIntegrationTest",
                          "SYSTEM_NAME=test")); // Default settings
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

    public Config getConfig() {
        return config;
    }

    public FilterResult getFilterResult() {
        return fr.get();
    }

    private void setupFilterResult(FilterResult bean) {
        bean.solr = getSolr();
    }

    public ProfileService getProfileService() {
        return ps.get();
    }

    private void setupProfileService(ProfileService bean) {
        bean.config = config;
    }

    public Solr getSolr() {
        return solr.get();
    }

    public BeanFactory setSolr(Solr solr) {
        this.solr.set(solr);
        return this;
    }

    private void setupSolr(Solr bean) {
        bean.config = config;
        bean.profileService = getProfileService();
    }

    public WorkPresentationBean getWorkPresentationBean() {
        return wpb.get();
    }

    private void setupWorkPresentationBean(WorkPresentationBean bean) {
        bean.em = em;
        bean.filterResult = getFilterResult();
    }

    public static Map<String, String> config(String... envs) {
        return Arrays.stream(envs)
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
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
