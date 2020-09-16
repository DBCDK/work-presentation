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
package dk.dbc.search.work.presentation.service.solr;

import dk.dbc.search.work.presentation.service.JpaBase;
import dk.dbc.search.work.presentation.service.vipcore.NoSuchProfileException;
import java.time.Duration;
import java.util.Set;
import org.eclipse.persistence.jpa.jpql.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * This tests ProfileService too
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class SolrIT extends JpaBase {

    private static final String WORK_ID = "work-of:870970-basis:00010529";

    @Test
    public void testProfileOk() throws Exception {
        System.out.println("testProfileOk");
        assertTimeout(Duration.ofMillis(2_500L), () -> {
                  withConfigEnv().jpaWithBeans(bf -> {
                      Solr solr = bf.getSolr();

                      Set<String> set1 = solr.getAccessibleManifestations(WORK_ID, "190102", "danbib", 10, "unittest");
                      assertThat(set1, containsInAnyOrder("830520-katalog:000025251", "870970-basis:00010529", "800010-katalog:99122054413305763"));
                      Set<String> set2 = solr.getAccessibleManifestations(WORK_ID, "710100", "opac", 10, "unittest");
                      assertThat(set2, containsInAnyOrder("710100-katalog:00010529", "870970-basis:00010529"));
                  });
              });
    }

    @Test
    public void testProfileNoSuchAgency() throws Exception {
        System.out.println("testProfileNoSuchAgency");
        String msg = assertTimeoutThrows(2_500L, NoSuchProfileException.class, () -> {
                                     withConfigEnv().jpaWithBeans(bf -> {
                                         Solr solr = bf.getSolr();
                                         solr.getAccessibleManifestations(WORK_ID, "000000", "test", 10, "unittest");
                                     });
                                 });
        System.out.println("msg = " + msg);
    }

    @Test
    public void testProfileNoSuchProfile() throws Exception {
        System.out.println("testProfileNoSuchProfile");
        String msg = assertTimeoutThrows(2_500L, NoSuchProfileException.class, () -> {
                                     withConfigEnv().jpaWithBeans(bf -> {
                                         Solr solr = bf.getSolr();
                                         solr.getAccessibleManifestations(WORK_ID, "100200", "not-really-a-profile", 10, "unittest");
                                     });
                                 });
        System.out.println("msg = " + msg);
    }

    private static <T extends Exception> String assertTimeoutThrows(long ms, Class<T> t, Executable ex) {
        return assertThrows(t, () -> assertTimeout(Duration.ofMillis(ms), ex))
                .getMessage();
    }
}
