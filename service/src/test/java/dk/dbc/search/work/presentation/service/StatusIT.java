/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-service
 *
 * work-presentation-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.service;

import dk.dbc.search.work.presentation.api.jpa.JpaBase;
import java.time.Duration;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class StatusIT extends JpaBase {

    private String user;

    @BeforeEach
    public void saveUser() {
        PGSimpleDataSource ds = (PGSimpleDataSource) dataSource;
        user = ds.getUser();
    }

    @AfterEach
    public void restoreUser() {
        PGSimpleDataSource ds = (PGSimpleDataSource) dataSource;
        ds.setUser(user);
    }

    @Test
    public void testStatusOk() throws Exception {
        System.out.println("testStatusOk");
        assertTimeout(Duration.ofMillis(500), () -> {
                  jpa(em -> {
                      BeanFactory beanFactory = new BeanFactory(em, dataSource);
                      Status status = beanFactory.getStatus();
                      Response resp = status.status();
                      System.out.println("resp = " + resp);
                      assertThat(resp.getStatus(), is(200));
                  });
              });
    }

    @Test
    public void testStatusNotOk() throws Exception {
        System.out.println("testStatusNotOk");
        assertTimeout(Duration.ofMillis(500), () -> {
                  jpa(em -> {
                      ( (PGSimpleDataSource) dataSource ).setUser("*SoNotValidUser*");
                      BeanFactory beanFactory = new BeanFactory(em, dataSource);
                      Status status = beanFactory.getStatus();
                      Response resp = status.status();
                      System.out.println("resp = " + resp);
                      assertThat(resp.getStatus(), not(is(200)));
                  });
              });
    }
}
