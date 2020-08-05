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

import dk.dbc.corepo.access.ee.CORepoProviderEE;
import dk.dbc.opensearch.commons.repository.IRepositoryDAO;
import dk.dbc.opensearch.commons.repository.IRepositoryIdentifier;
import dk.dbc.opensearch.commons.repository.ISysRelationsStream;
import dk.dbc.opensearch.commons.repository.RepositoryException;
import dk.dbc.opensearch.commons.repository.RepositoryStream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This produces all the fields a presentation request possibly can result in.
 *
 * The presentation request then filters this.
 *
 * @author Thomas Pii (thp@dbc.dk)
 */
@Stateless
public class WorkTreeBuilder {

    private static final Logger log = LoggerFactory.getLogger(WorkTreeBuilder.class);

    @Resource(lookup = "jdbc/corepo")
    DataSource dataSource;

    private CORepoProviderEE daoProvider = new CORepoProviderEE("corepo");

    public void process(DataSource corpeoSource, String pidStr) throws RepositoryException {
        log.trace("Entering WorkTreeBuilder.process");
        try(IRepositoryDAO dao = daoProvider.getRepository(dataSource)) {

            IRepositoryIdentifier workPid = dao.createIdentifier(pidStr);

            if (workPid.getObjectType() != IRepositoryIdentifier.ObjectType.WORK) {
                throw new RepositoryException("Not a work: " + workPid);
            }
            IRepositoryDAO.State state = dao.getObjectState(workPid);
            log.info("Processing work: {}, state {}", workPid, state);
            // Process members of work
            ISysRelationsStream workRelations = dao.getSysRelationsStream(workPid);
            IRepositoryIdentifier primaryUnit = workRelations.getPrimaryUnitForWork();
            IRepositoryIdentifier[] units = workRelations.getMembersOfWork();
            for (IRepositoryIdentifier unit : units) {
                // Process members of unit
                ISysRelationsStream unitRelations = dao.getSysRelationsStream(unit);
                IRepositoryIdentifier primaryRecord = unitRelations.getPrimaryMemberOfUnit();
                IRepositoryIdentifier[] records = unitRelations.getMembersOfUnit();
                for (IRepositoryIdentifier record : records) {
                    // TODO getDatastreams includeds stream content. Add a metadata method that only has metadata?
                    RepositoryStream[] datastreams = dao.getDatastreams(record);
                    for (RepositoryStream datastream : datastreams) {
                        // Process meta data
                    }
                }
            }
        } finally {
            log.trace("Leaving WorkTreeBuilder.process");
        }
    }
}
