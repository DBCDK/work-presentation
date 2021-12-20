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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import java.sql.SQLException;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static dk.dbc.search.work.presentation.api.jpa.JpaBase.wpPg;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JpaBase extends dk.dbc.search.work.presentation.api.jpa.JpaBase<BeanFactory> {

    static WireMockServer wms = new WireMockServer(options()
            .dynamicPort()
            .stubCorsEnabled(true)
            .usingFilesUnderClasspath("wiremock")
            .notifier(new Slf4jNotifier(false))
            .gzipDisabled(true));

    @BeforeAll
    public static void setUpEntityCorepoDataSource() throws SQLException {
        wms.start();
    }

    @AfterAll
    public static void shutdownWiremock() {
        wms.stop();
    }

    @Override
    public BeanFactory createBeanFactory(Map<String, String> env, EntityManager em, EntityManagerFactory emf) {
        return new BeanFactory(env, em, wpPg.datasource(), wms);
    }
}
