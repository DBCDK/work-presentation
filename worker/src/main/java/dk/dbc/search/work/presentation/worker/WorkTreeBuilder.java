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

import dk.dbc.opensearch.commons.repository.IRepositoryIdentifier;
import dk.dbc.opensearch.commons.repository.ISysRelationsStream;
import dk.dbc.opensearch.commons.repository.RepositoryException;
import dk.dbc.opensearch.commons.repository.RepositoryStream;
import dk.dbc.opensearch.commons.repository.Repositorydentifier;
import java.io.IOException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Extract the structure of the work/units/records for a CORepo work
 *
 * @author Thomas Pii (thp@dbc.dk)
 */
@Stateless
public class WorkTreeBuilder {

    private static final Logger log = LoggerFactory.getLogger(WorkTreeBuilder.class);

    @Inject
    ContentService contentService;

    /**
     * Process a work object and extract structure for units and records
     * @param corpeoSource CORepo database
     * @param pidStr The identifier for the work object to process
     * @throws RepositoryException In case database layer thrown an error or if called for an object that is not a work object TODO: Use RepositoryException or a different exception
     */
    public void process(DataSource corpeoSource, String pidStr) throws RepositoryException {
        log.trace("Entering WorkTreeBuilder.process");
        try {
            log.info("Processing work: {}", pidStr);
            
            // Process members of work
            IRepositoryIdentifier workPid = new Repositorydentifier(pidStr);
            if (workPid.getObjectType() != IRepositoryIdentifier.ObjectType.WORK) {
                throw new RepositoryException("Not a work: " + workPid);
            }
            ContentService.MetaData workData = contentService.getObjectMetaData(workPid);
            log.debug("Work: {}, state {}, modified {}", pidStr, workData.state, workData.modified);

            ISysRelationsStream workRelations = contentService.getRelations(workPid);
            IRepositoryIdentifier primaryUnit = workRelations.getPrimaryUnitForWork();
            IRepositoryIdentifier[] units = workRelations.getMembersOfWork();
            log.debug("{} Found primaryUnit {} and members {}", workPid, primaryUnit, units);

            for (IRepositoryIdentifier unit : units) {
                // Process members of unit
                ISysRelationsStream unitRelations = contentService.getRelations(unit);
                IRepositoryIdentifier primaryRecord = unitRelations.getPrimaryMemberOfUnit();
                IRepositoryIdentifier[] records = unitRelations.getMembersOfUnit();
                log.debug("{} - {} Found primaryUnit {} and members {}", workPid, unit, primaryRecord, records);
                for (IRepositoryIdentifier record : records) {
                    RepositoryStream[] datastreams = contentService.getDatastreams(record);
                    log.debug("{} - {} - {} Found {} streams", workPid, unit, record, datastreams.length);
                    log.trace("{} - {} - {} Found streams {}", workPid, unit, record, datastreams);
                    for (RepositoryStream datastream : datastreams) {
                        log.trace("{} - {} - {} - {}, ", workPid, unit, record, datastream );
                        // Process meta data
                    }
                }
            }
        } catch (SAXException|ParserConfigurationException|IOException ex) {
            log.error("Error extracting tree from repository", ex);
        } finally {
            log.trace("Leaving WorkTreeBuilder.process {}", pidStr);
        }
    }
}
