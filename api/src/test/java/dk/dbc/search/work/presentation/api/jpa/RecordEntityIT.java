/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-api
 *
 * work-presentation-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.api.jpa;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class RecordEntityIT extends JpaBase {

    public RecordEntityIT() {
    }

    @Test
    public void testSaveLoad() throws Exception {
        System.out.println("testSaveLoad");

        RecordEntity oldEntity = new RecordEntity("a", "b", Timestamp.from(Instant.now()), new HashMap<String, String>() {
                                              {
                                                  put("a", "123");
                                              }
                                          });

        jpa(em -> {
            em.persist(oldEntity);
        });
        flushAndEvict();
        RecordEntity newEntity = jpa(em -> {
            return RecordEntity.from(em, "a");
        });

        System.out.println("newEntity = " + newEntity);

        assertThat(newEntity, is(oldEntity));
    }
}
