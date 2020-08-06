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
import dk.dbc.opensearch.commons.repository.ISysRelationsStream;
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
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    public void corepo() throws SQLException {
        try (Connection connection = corepoDataSource.getConnection() ;
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE records CASCADE");
        } catch (SQLException ex) {
            log.error("Could not clean corepo queue: {}", ex.getMessage());
            log.debug("Could not clean corepo queue: ", ex);
        }
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

    @Test
    public void notAWork() throws Exception {
        BeanFactory beanFactory = new BeanFactory(null, dataSource, corepoDataSource);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();
        builder.init();

        Assertions.assertThrows(RepositoryException.class, () -> {
            builder.process(dataSource, "unit:1");
        });
        builder.destroy();
        log.info("Done");
    }

    @Test
    public void isWorkDeleted() throws Exception {
        BeanFactory beanFactory = new BeanFactory(null, dataSource, corepoDataSource);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();
        builder.init();

        IRepositoryIdentifier work = dao.createWorkIdentifier();
        // TODO: DC datastream ?
        dao.createObjectWithData(work, IRepositoryDAO.State.DELETED, "A delete work", new RepositoryStream[]{ });
        dao.commit();
        log.info("Work {}", work);
        builder.process(dataSource, "work:1");
        builder.destroy();
        log.info("Done");
    }

    private byte[] makeDCStream(IRepositoryIdentifier id) {
        String str = "<oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n" +
                "  <dc:identifier>" + id + "</dc:identifier>\n" +
                "</oai_dc:dc>";
        return str.getBytes();
    }

    @Test
    public void isWorkWithOneRecord() throws Exception {
        BeanFactory beanFactory = new BeanFactory(null, dataSource, corepoDataSource);
        WorkTreeBuilder builder = beanFactory.getWorkTreeBuilder();
        builder.init();

        IRepositoryIdentifier work = dao.createWorkIdentifier();
        RepositoryStream work_dc = new RepositoryStream("DC", makeDCStream(work), IRepositoryDAO.State.ACTIVE, work);
        dao.createObjectWithData(work, IRepositoryDAO.State.ACTIVE, "", new RepositoryStream[]{ work_dc });

        IRepositoryIdentifier unit = dao.createUnitIdentifier();
        RepositoryStream unit_dc = new RepositoryStream("DC", makeDCStream(unit), IRepositoryDAO.State.ACTIVE, unit);
        dao.createObjectWithData(unit, IRepositoryDAO.State.ACTIVE, "", new RepositoryStream[]{ unit_dc });

        IRepositoryIdentifier record = dao.createIdentifier("870970-basis:1");
        RepositoryStream record_dc = new RepositoryStream("dc", makeDCStream(record), IRepositoryDAO.State.ACTIVE, record);
        RepositoryStream record_common = new RepositoryStream("commonData", new byte[]{}, IRepositoryDAO.State.ACTIVE, record);
        dao.createObjectWithData(record, IRepositoryDAO.State.ACTIVE, "", new RepositoryStream[]{ record_dc, record_common });

        ISysRelationsStream workRelation = dao.getSysRelationsStream(work);
        ISysRelationsStream unitRelation = dao.getSysRelationsStream(unit);
        ISysRelationsStream recordRelation = dao.getSysRelationsStream(record);

        workRelation.addHasMemberOfWork(unit);
        workRelation.addHasPrimaryUnit(unit);

        unitRelation.addIsMemberOfWork(work);
        unitRelation.addIsPrimaryUnit(work);
        unitRelation.addHasMemberOfUnit(record);
        unitRelation.addHasPrimaryBibObject(record);

        recordRelation.addIsMemberOfUnit(unit);
        recordRelation.addIsPrimaryBibObject(unit);
        dao.commit();

        log.info("Work {}", work);
        builder.process(dataSource, work.toString());

        builder.destroy();
        log.info("Done");
    }
}
