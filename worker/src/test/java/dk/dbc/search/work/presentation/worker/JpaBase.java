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
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import dk.dbc.commons.testcontainers.postgres.DBCPostgreSQLContainer;
import dk.dbc.corepo.queue.QueueJob;
import dk.dbc.pgqueue.supplier.PreparedQueueSupplier;
import dk.dbc.pgqueue.supplier.QueueSupplier;
import dk.dbc.search.work.presentation.api.jpa.JsonSchemaVersion;
import dk.dbc.search.work.presentation.database.DatabaseMigrator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Testcontainers
public class JpaBase extends dk.dbc.search.work.presentation.api.jpa.JpaBase<BeanFactory> {

    private static final Logger log = LoggerFactory.getLogger(JpaBase.class);

    @Container
    static DBCPostgreSQLContainer coPg = new DBCPostgreSQLContainer();

    static WireMockServer wms = new WireMockServer(options()
            .dynamicPort()
            .stubCorsEnabled(true)
            .usingFilesUnderClasspath("wiremock")
            .notifier(new Slf4jNotifier(false))
            .gzipDisabled(true));

    @BeforeAll
    public static void setUpEntityCorepoDataSource() throws SQLException {
        DatabaseMigrator.migrate(wpPg.datasource());
        dk.dbc.corepo.access.DatabaseMigrator.migrate(coPg.datasource());
        wms.start();
    }

    @AfterAll
    public static void shutdownWiremock() {
        wms.stop();
    }

    @BeforeEach
    public void corepoDB() throws SQLException {
        try (Connection connection = coPg.createConnection() ;
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE queue");
        } catch (SQLException ex) {
            log.error("Could not clean corepo queue: {}", ex.getMessage());
            log.debug("Could not clean corepo queue: ", ex);
        }
    }

    @BeforeEach
    public void workPresentationDB() throws SQLException {
        try (Connection connection = wpPg.createConnection() ;
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE workObjectV" + JsonSchemaVersion.VERSION);
            stmt.executeUpdate("TRUNCATE workContainsV" + JsonSchemaVersion.VERSION);
            stmt.executeUpdate("TRUNCATE cachev" + JsonSchemaVersion.VERSION);
        } catch (SQLException ex) {
            log.error("Could not clean corepo queue: {}", ex.getMessage());
            log.debug("Could not clean corepo queue: ", ex);
        }
    }

    public void queue(String... pids) throws SQLException {
        try (Connection connection = coPg.createConnection()) {
            PreparedQueueSupplier<QueueJob> supplier = new QueueSupplier<>(QueueJob.STORAGE_ABSTRACTION).preparedSupplier(connection);
            for (String pid : pids) {
                supplier.enqueue("queue", new QueueJob(pid, "track", false));
            }
        }
    }

    @Override
    public BeanFactory createBeanFactory(Map<String, String> env, EntityManager em, EntityManagerFactory emf) {
        return new BeanFactory(env, em, emf, coPg.datasource(), wms);
    }

    public void waitForQueue(int seconds) throws SQLException, InterruptedException {
        Instant timeout = Instant.now().plusSeconds(seconds);
        for (;;) {
            try (Connection connection = coPg.createConnection() ;
                 Statement stmt = connection.createStatement() ;
                 ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM queue")) {
                if (resultSet.next()) {
                    int rows = resultSet.getInt(1);
                    if (rows == 0)
                        return;
                }
                Duration between = Duration.between(Instant.now(), timeout);
                if (between.isNegative())
                    Assertions.fail("Timedout waiting for queue to drain");
                Thread.sleep(100L);
            }
        }
    }

}
