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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
@Liveness
public class DatabaseHealthCheck implements HealthCheck {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthCheck.class);

    @Resource(lookup = "jdbc/work-presentation")
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        log.debug("check database connection");
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("database");
        try (Connection connection = dataSource.getConnection() ;
             Statement stmt = connection.createStatement() ;
             ResultSet resultSet = stmt.executeQuery("SELECT 1")) {
            if (resultSet.next()) {
                builder.up();
            } else {
                throw new SQLException("No rows in `SELECT 1`");
            }
        } catch (SQLException ex) {
            log.error("Status SQL: {}", ex.getMessage());
            log.debug("Status SQL: ", ex);
            builder.down().withData("exception", ex.getMessage());
        }
        return builder.build();
    }

}
