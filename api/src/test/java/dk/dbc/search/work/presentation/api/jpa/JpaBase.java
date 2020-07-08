/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-api
 *
 * work-presentation-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.api.jpa;

import dk.dbc.search.work.presentation.database.DatabaseMigrator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JpaBase {

    private static final Logger log = LoggerFactory.getLogger(JpaBase.class);

    protected static PGSimpleDataSource dataSource;
    private static HashMap<String, String> entityManagerProperties;
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @FunctionalInterface
    public static interface JpaVoidExecution {

        public void execute(EntityManager em) throws Exception;
    }

    @FunctionalInterface
    public static interface JpaExecution<T extends Object> {

        public T execute(EntityManager em) throws Exception;
    }

    public void jpa(JpaVoidExecution ex) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            ex.execute(entityManager);
            transaction.commit();
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
    }

    public <T> T jpa(JpaExecution<T> ex) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            T t = ex.execute(entityManager);
            transaction.commit();
            return t;
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
    }

    public void flushAndEvict() {
        jpa(em -> {
            em.flush();
            em.clear();
        });
        entityManagerFactory.getCache().evictAll();
    }

    @BeforeAll
    public static void setUpEntityManagerAndDataSource() {
        log.info("setUpEntityManagerAndDataSource");

        String testPort = System.getProperty("postgresql.port");
        dataSource = new PGSimpleDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return setLogging(super.getConnection());
            }

            @Override
            public Connection getConnection(String user, String password) throws SQLException {
                return setLogging(super.getConnection(user, password));
            }

            private Connection setLogging(Connection connection) {
                try (PreparedStatement stmt = connection.prepareStatement("SET log_statement = 'all';")) {
                    stmt.execute();
                } catch (SQLException ex) {
                    log.warn("Cannot set logging: {}", ex.getMessage());
                    log.debug("Cannot set logging:", ex);
                }
                return connection;
            }
        };

        String userName = System.getProperty("user.name");
        if (testPort != null) {
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("workpresentation");
            dataSource.setUser(userName);
            dataSource.setPassword(userName);
            dataSource.setPortNumber(Integer.parseUnsignedInt(testPort));
        } else {
            Map<String, String> env = System.getenv();
            dataSource.setUser(env.getOrDefault("PGUSER", userName));
            dataSource.setPassword(env.getOrDefault("PGPASSWORD", userName));
            dataSource.setServerName(env.getOrDefault("PGHOST", "localhost"));
            dataSource.setPortNumber(Integer.parseUnsignedInt(env.getOrDefault("PGPORT", "5432")));
            dataSource.setDatabaseName(env.getOrDefault("PGDATABASE", userName));
        }

        DatabaseMigrator.migrate(dataSource);

        entityManagerProperties = new HashMap<String, String>() {
            {
                put(JDBC_USER, dataSource.getUser());
                put(JDBC_PASSWORD, dataSource.getPassword());
                put(JDBC_URL, dataSource.getUrl());
                put(JDBC_DRIVER, "org.postgresql.Driver");
                put("eclipselink.logging.level", "FINE");
            }
        };
        entityManagerFactory = Persistence.createEntityManagerFactory("workPresentationTest_PU", entityManagerProperties);
    }

    @BeforeEach
    public void setUpEntityManager() {
        log.info("setUpEntityManager");
        entityManagerFactory.getCache().evictAll();
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterEach
    public void tearDownEntityManager() {
        log.info("tearDownEntityManager");
        entityManager.close();
        entityManager = null;
    }

    @AfterAll
    public static void tearEntityManagerAndDataSource() {
    }
}
