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

import dk.dbc.search.work.presentation.database.DatabaseMigrator;
import java.util.HashSet;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Singleton
@Startup
public class DatabaseMigrate {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrate.class);

    @Resource(lookup = "jdbc/work-presentation")
    DataSource dataSource;

    @PostConstruct
    public void init() {
        log.info("Migrating database");

        try {
            HashSet<String> migrations = DatabaseMigrator.migrate(dataSource);
            log.info("Migrations applied: {}", migrations);
        } catch (RuntimeException e) {
            log.error("Error migrating database: {}", e.getMessage());
            log.debug("Error migrating database: ", e);
            throw new EJBException("Error migrating database: ");
        }
    }
}
