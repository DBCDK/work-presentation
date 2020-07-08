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

import dk.dbc.search.work.presentation.api.jpa.RecordEntity;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class DummyIT extends JpaBaseWithCorepo {

    @Test
    public void testDUMMY() throws Exception {
        System.out.println("testDUMMY");

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
