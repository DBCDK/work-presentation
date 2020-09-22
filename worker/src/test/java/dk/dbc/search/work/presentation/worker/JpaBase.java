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

import dk.dbc.corepo.queue.QueueJob;
import dk.dbc.pgqueue.PreparedQueueSupplier;
import dk.dbc.pgqueue.QueueSupplier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JpaBase extends dk.dbc.search.work.presentation.api.jpa.JpaBase<BeanFactory> {

    private static final Logger log = LoggerFactory.getLogger(JpaBase.class);

    protected static PGSimpleDataSource corepoDataSource;

    @BeforeAll
    public static void setUpEntityCorepoDataSource() throws SQLException {
        corepoDataSource = getDataSource("corepo");
        try (Connection connection = dataSource.getConnection() ;
             Statement stmt = connection.createStatement()) {
            boolean hasCorepoDb;
            try (ResultSet resultSet = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'corepo'")) {
                hasCorepoDb = resultSet.next();
            }
            if (!hasCorepoDb)
                stmt.executeUpdate("CREATE DATABASE corepo");
        }
        dk.dbc.corepo.access.DatabaseMigrator.migrate(getDataSource("corepo"));
    }

    @BeforeEach
    public void corepo() throws SQLException {
        try (Connection connection = corepoDataSource.getConnection() ;
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE queue");
        } catch (SQLException ex) {
            log.error("Could not clean corepo queue: {}", ex.getMessage());
            log.debug("Could not clean corepo queue: ", ex);
        }
    }

    public void queue(String... pids) throws SQLException {
        try (Connection connection = corepoDataSource.getConnection()) {
            PreparedQueueSupplier<QueueJob> supplier = new QueueSupplier<>(QueueJob.STORAGE_ABSTRACTION).preparedSupplier(connection);
            for (String pid : pids) {
                supplier.enqueue("queue", new QueueJob(pid, "track", false));
            }
        }
    }

    @Override
    public BeanFactory createBeanFactory(Map<String, String> env, EntityManager em) {
        return new BeanFactory(env, em, corepoDataSource);
    }

    public void waitForQueue(int seconds) throws SQLException, InterruptedException {
        Instant timeout = Instant.now().plusSeconds(seconds);
        for (;;) {
            try (Connection connection = corepoDataSource.getConnection() ;
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
