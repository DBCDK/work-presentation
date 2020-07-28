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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import static java.time.Duration.ofSeconds;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class DatabaseMigratorIT {

    private PGSimpleDataSource dataSource;

    @BeforeEach
    public void setUp() {
        dataSource = getDataSource();
    }

    @Test
    public void testMigrate() throws Exception {
        System.out.println("migrate");
        HashSet<String> migrated = assertTimeout(
                ofSeconds(30), () -> {
            return DatabaseMigrator.migrate(dataSource);
        });
        System.out.println("migrated = " + migrated);
        Integer version = null;
        try (Connection connection = dataSource.getConnection() ;
             Statement stmt = connection.createStatement() ;
             ResultSet resultSet = stmt.executeQuery("SELECT version FROM schema_version ORDER BY installed_rank DESC LIMIT 1")) {
            if (resultSet.next()) {
                version = resultSet.getInt(1);
                System.out.println("version = " + version);
            }
        }
        assertThat(version, is(1));
    }

    private static PGSimpleDataSource getDataSource() {
        PGSimpleDataSource datasource = new PGSimpleDataSource();

        datasource.setServerNames(new String[] {"localhost"});
        String postgresqlPort = System.getProperty("postgresql.port");
        if (postgresqlPort != null && postgresqlPort.length() > 1) {
            datasource.setDatabaseName("workpresentation");
            datasource.setPortNumbers(new int[] {Integer.parseInt(System.getProperty("postgresql.port", "5432"))});
        } else {
            datasource.setDatabaseName(System.getProperty("user.name"));
            datasource.setPortNumbers(new int[] {5432});
        }
        datasource.setUser(System.getProperty("user.name"));
        datasource.setPassword(System.getProperty("user.name"));
        return datasource;
    }

}
