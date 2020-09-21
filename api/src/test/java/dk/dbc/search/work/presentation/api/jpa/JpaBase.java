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
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;

/**
 * Transaction oriented helper for integration-tests
 * <p>
 * Override the method
 * {@link #createBeanFactory(java.util.Map, javax.persistence.EntityManager)} to
 * make beans for calls to .withConfigEnv(...).jpaWithBeans(beanFactory -> {})
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 * @param <BF> a beanFactory
 */
public class JpaBase<BF> {

    private static final Logger log = LoggerFactory.getLogger(JpaBase.class);

    protected static PGSimpleDataSource dataSource;
    private static HashMap<String, String> entityManagerProperties;
    private static EntityManagerFactory entityManagerFactory;

    @FunctionalInterface
    public interface JpaVoidExecution {

        public void execute(EntityManager em) throws Exception;
    }

    @FunctionalInterface
    public interface JpaBeanVoidExecution<BF> {

        public void execute(BF beanFactory) throws Exception;
    }

    public void jpa(JpaVoidExecution ex) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
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
        } finally {
            entityManager.close();
            entityManagerFactory.getCache().evictAll();
        }
    }

    @BeforeAll
    public static void setUpEntityManagerAndDataSource() {
        log.info("setUpEntityManagerAndDataSource");

        dataSource = getDataSource(null);

        DatabaseMigrator.migrate(dataSource);

        try (Connection connection = dataSource.getConnection() ;
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE records");
            stmt.executeUpdate("TRUNCATE workContains");
            stmt.executeUpdate("TRUNCATE cache");
        } catch (SQLException ex) {
            log.error("Could not clean database: {}", ex.getMessage());
            log.debug("Could not clean database: ", ex);
        }

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

    public WithEnv withConfigEnv(String... envs) {
        Map<String, String> env = Arrays.stream(envs)
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
        return new WithEnv(env);
    }

    public BF createBeanFactory(Map<String, String> env, EntityManager em) {
        return null;
    }

    public class WithEnv {

        private final Map<String, String> env;

        public WithEnv(Map<String, String> env) {
            this.env = env;
        }

        public void jpaWithBeans(JpaBeanVoidExecution<BF> execution) {
            jpa(em -> {
                execution.execute(createBeanFactory(env, em));
            });
        }
    }

    protected static PGSimpleDataSource getDataSource(String databaseName) throws NumberFormatException {
        String testPort = System.getProperty("postgresql.port");
        PGSimpleDataSource ds = new PGSimpleDataSource() {
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
            ds.setServerNames(new String[] {"localhost"});
            if (databaseName == null)
                databaseName = "workpresentation";
            ds.setUser(userName);
            ds.setPassword(userName);
            ds.setPortNumbers(new int[] {Integer.parseUnsignedInt(testPort)});
        } else {
            Map<String, String> env = System.getenv();
            ds.setUser(env.getOrDefault("PGUSER", userName));
            ds.setPassword(env.getOrDefault("PGPASSWORD", userName));
            ds.setServerNames(new String[] {env.getOrDefault("PGHOST", "localhost")});
            ds.setPortNumbers(new int[] {Integer.parseUnsignedInt(env.getOrDefault("PGPORT", "5432"))});
            if (databaseName == null)
                databaseName = env.getOrDefault("PGDATABASE", userName);
        }
        ds.setDatabaseName(databaseName);
        return ds;
    }

}
