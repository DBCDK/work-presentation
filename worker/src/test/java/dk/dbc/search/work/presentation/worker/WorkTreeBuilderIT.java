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

import dk.dbc.corepo.access.CORepoProvider;
import dk.dbc.opensearch.commons.repository.IRepositoryDAO;
import dk.dbc.opensearch.commons.repository.IRepositoryIdentifier;
import dk.dbc.opensearch.commons.repository.RepositoryException;
import dk.dbc.opensearch.commons.repository.RepositoryProvider;
import dk.dbc.opensearch.commons.repository.RepositoryStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkTreeBuilderIT extends JpaBaseWithCorepo {

    private static final Logger log = LoggerFactory.getLogger(WorkTreeBuilderIT.class);
    protected static IRepositoryDAO dao;

    @BeforeAll
    public static void setUpCorepoDao() throws SQLException, RepositoryException {
        RepositoryProvider provider = new CORepoProvider("IntegrationTest", corepoDataSource);
        dao = provider.getRepository();
    }

    @AfterEach
    public void cleanupCorepo() throws SQLException {
        try {
            dao.rollback();
        } catch (RepositoryException ex) {
            log.error("Could not clean corepo queue: {}", ex.getMessage());
            log.debug("Could not clean corepo queue: ", ex);
        }

    }


//    @Test
//    public void notAWork() throws Exception {
//        BeanFactory beanFactory = new BeanFactory(null, dataSource, corepoDataSource);
//        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();
//
//        Assertions.assertThrows(RepositoryException.class, () -> {
//            builder.process(dataSource, "unit:1");
//        });
//        log.info("Done");
//    }

    @Test
    public void isWork() throws Exception {
        BeanFactory beanFactory = new BeanFactory(null, dataSource, corepoDataSource);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();

        IRepositoryIdentifier work = dao.createWorkIdentifier();
        dao.createObjectWithData(work, IRepositoryDAO.State.ACTIVE, "", new RepositoryStream[]{});
//        log.info("Work {}", work);
//        Assertions.assertThrows(RepositoryException.class, () -> {
//            builder.process(dataSource, "work:1");
//        });

        log.info("Done");
    }
}
