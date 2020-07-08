/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-database
 *
 * work-presentation-database is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-database is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.database;

import java.util.HashSet;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class DatabaseMigrator {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrator.class);

    public static HashSet<String> migrate(DataSource dataSource) {
        HashSet<String> migrates = new HashSet<>();
        FluentConfiguration flywayConfigure = Flyway.configure()
                .table("schema_version")
                .dataSource(dataSource);

        final Flyway flyway = flywayConfigure.load();
        for (MigrationInfo i : flyway.info().applied()) {
            log.info("db task {} : {} from file '{}' (applied)", i.getVersion(), i.getDescription(), i.getScript());
        }
        for (MigrationInfo i : flyway.info().pending()) {
            migrates.add(i.getVersion().getVersion());
            log.info("db task {} : {} from file '{}' (pending)", i.getVersion(), i.getDescription(), i.getScript());
        }
        flyway.migrate();
        return migrates;
    }

}
