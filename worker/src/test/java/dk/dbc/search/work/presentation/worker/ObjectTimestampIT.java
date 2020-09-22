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

import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkObjectEntity;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import java.sql.Timestamp;
import java.time.Instant;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ObjectTimestampIT extends JpaBase {

    @Test
    public void testRecordModificationTime() throws Exception {
        System.out.println("testRecordModificationTime");

        jpa(em -> {
            WorkObjectEntity work = WorkObjectEntity.from(em, "work-of:1");
            work.setContent(new WorkInformation());
            work.setCorepoWorkId("work:1");
            work.setModified(Timestamp.from(Instant.parse("2020-01-02T12:34:56.789Z")));
            work.save();
        });

        withConfigEnv()
                .jpaWithBeans(beanfactory -> {
                    ObjectTimestamp bean = beanfactory.getObjectTimestamp();
                    String timestamp = bean.getTimestamp("work-of:1");
                    assertThat(timestamp, is("2020-01-02T12:34:56.789Z"));
                });
    }

    @Test
    public void testCacheModificationTime() throws Exception {
        System.out.println("testCacheModificationTime");

        jpa(em -> {
            WorkObjectEntity work = WorkObjectEntity.from(em, "work-of:1");
            work.setContent(new WorkInformation());
            work.setCorepoWorkId("work:1");
            work.setModified(Timestamp.from(Instant.parse("2020-01-02T12:34:56.789Z")));
            work.save();

            CacheEntity cache = CacheEntity.from(em, "1");
            cache.setContent(new ManifestationInformation());
            cache.setModified(Timestamp.from(Instant.parse("1970-12-31T23:59:59.909Z")));
            cache.save();
        });

        withConfigEnv()
                .jpaWithBeans(beanfactory -> {
                    ObjectTimestamp bean = beanfactory.getObjectTimestamp();
                    String timestamp = bean.getTimestamp("1");
                    assertThat(timestamp, is("1970-12-31T23:59:59.909Z"));
                });
    }

    @Test
    public void testNotFound() throws Exception {
        System.out.println("testNotFound");
        NotFoundException ex = assertThrows(
                NotFoundException.class, () -> {
            withConfigEnv()
                    .jpaWithBeans(beanfactory -> {
                        ObjectTimestamp bean = beanfactory.getObjectTimestamp();
                        bean.getTimestamp("1");
                    });
        });
        assertThat(ex, is(notNullValue()));
    }
}
