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

import dk.dbc.commons.testcontainers.postgres.DBCPostgreSQLContainer;
import dk.dbc.search.work.presentation.database.DatabaseMigrator;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Table;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Transaction oriented helper for integration-tests
 * <p>
 * Implement the method
 * {@link #createBeanFactory(java.util.Map, javax.persistence.EntityManager, javax.persistence.EntityManagerFactory)} to
 * make beans for calls to .withConfigEnv(...).jpaWithBeans(beanFactory -> {})
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 * @param <BF> a beanFactory as produced by
 *             {@link #createBeanFactory(java.util.Map, javax.persistence.EntityManager, javax.persistence.EntityManagerFactory)}
 */
@Testcontainers
public abstract class JpaBase<BF extends AutoCloseable> {

    @Container
    public static DBCPostgreSQLContainer wpPg = new DBCPostgreSQLContainer();

    private static final Logger log = LoggerFactory.getLogger(JpaBase.class);

    public static final String CACHE_TABLE_NAME = CacheEntity.class.getAnnotation(Table.class).name();
    public static final String WORK_CONTAINS_TABLE_NAME = WorkContainsEntity.class.getAnnotation(Table.class).name();
    public static final String WORK_OBJECT_TABLE_NAME = WorkObjectEntity.class.getAnnotation(Table.class).name();

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

        DatabaseMigrator.migrate(wpPg.datasource());

        try (Connection connection = wpPg.createConnection() ;
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE " + WORK_OBJECT_TABLE_NAME);
            stmt.executeUpdate("TRUNCATE " + WORK_CONTAINS_TABLE_NAME);
            stmt.executeUpdate("TRUNCATE " + CACHE_TABLE_NAME);
        } catch (SQLException ex) {
            log.error("Could not clean database: {}", ex.getMessage());
            log.debug("Could not clean database: ", ex);
        }
        entityManagerFactory = Persistence.createEntityManagerFactory("workPresentationTest_PU", wpPg.entityManagerProperties());
    }

    public WithEnv withConfigEnv(String... envs) {
        Map<String, String> env = Arrays.stream(envs)
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
        return new WithEnv(env);
    }

    public abstract BF createBeanFactory(Map<String, String> env, EntityManager em, EntityManagerFactory emf);

    public class WithEnv {

        private final Map<String, String> env;

        public WithEnv(Map<String, String> env) {
            this.env = env;
        }

        public void jpaWithBeans(JpaBeanVoidExecution<BF> execution) {
            jpa(em -> {
                try (BF bf = createBeanFactory(env, em, entityManagerFactory)) {
                    execution.execute(bf);
                }
            });
        }
    }

    public int countCacheEntries() {
        AtomicInteger i = new AtomicInteger();
        jpa(em -> {
            i.set((int) (long) (Long) em.createNativeQuery("SELECT COUNT(1) FROM " + CACHE_TABLE_NAME).getSingleResult());
        });
        return i.get();
    }

    public int countWorkContainsEntries() {
        AtomicInteger i = new AtomicInteger();
        jpa(em -> {
            i.set((int) (long) (Long) em.createNativeQuery("SELECT COUNT(1) FROM " + WORK_CONTAINS_TABLE_NAME).getSingleResult());
        });
        return i.get();
    }

    public int countWorkObjectEntries() {
        AtomicInteger i = new AtomicInteger();
        jpa(em -> {
            i.set((int) (long) (Long) em.createNativeQuery("SELECT COUNT(1) FROM " + WORK_OBJECT_TABLE_NAME).getSingleResult());
        });
        return i.get();
    }
}
