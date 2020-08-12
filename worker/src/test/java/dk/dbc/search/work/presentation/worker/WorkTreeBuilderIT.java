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
import dk.dbc.opensearch.commons.repository.Repositorydentifier;
import java.sql.SQLException;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkTreeBuilderIT extends JpaBaseWithCorepo {

    private static final Logger log = LoggerFactory.getLogger(WorkTreeBuilderIT.class);

    public static String contentServiceUrl = System.getenv("COREPO_CONTENT_SERVICE_URL");

    @BeforeAll
    public static void setUpCorepoDao() throws SQLException, RepositoryException {
        RepositoryProvider provider = new CORepoProvider("IntegrationTest", corepoDataSource);
    }

    @Test
    public void notAWork() throws Exception {
        BeanFactory beanFactory = new BeanFactory(null, dataSource, null);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();

        Assertions.assertThrows(RepositoryException.class, () -> {
            builder.process(dataSource, "unit:1");
        });
        log.info("Done");
    }

    @Test
    public void notExistsWork() throws Exception {
        BeanFactory beanFactory = new BeanFactory(null, dataSource, null);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();

        Assertions.assertThrows(WebApplicationException.class, () -> {
            builder.process(dataSource, "work:3");
        });
        log.info("Done");
    }

    @Test
    public void isWorkDeleted() throws Exception {
        BeanFactory beanFactory = new BeanFactory(null, dataSource, null);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();

        builder.process(dataSource, "work:1571079");
        // TODO: find a work with state = DELETED. Output expected empty work with no units

        log.info("Done");
    }

    @Test
    public void isWorkWithSmallTree() throws Exception {
        // http://corepo-introspect-service.cisterne.svc.cloud.dbc.dk/?tab=Tree%20Views&tree=work:19
        BeanFactory beanFactory = new BeanFactory(null, dataSource, null);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();
        String work = "work:19";

        builder.process(dataSource, work);

        log.info("Done");

    }
//    @Test
//    public void isWorkWithMediumTree() throws Exception {
//        // http://corepo-introspect-service.cisterne.svc.cloud.dbc.dk/?tab=Tree%20Views&tree=work:62
//        BeanFactory beanFactory = new BeanFactory(null, dataSource, null);
//        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();
//        String work = "work:62";
//
//        builder.process(dataSource, work);
//
//        log.info("Done");
//
//    }
//    @Test
//    public void isWorkWithLargeTree() throws Exception {
//        // "lykke-per" http://corepo-introspect-service.cisterne.svc.cloud.dbc.dk/?tab=Tree%20Views&tree=work:15
//        BeanFactory beanFactory = new BeanFactory(null, dataSource, null);
//        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();
//        String work = "work:15";
//
//        builder.process(dataSource, work);
//
//        log.info("Done");
//
//    }


}
