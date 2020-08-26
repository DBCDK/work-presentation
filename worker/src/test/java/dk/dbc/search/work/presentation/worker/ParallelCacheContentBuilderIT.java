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

import dk.dbc.search.work.presentation.javascript.JavascriptCacheObjectBuilder;
import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import dk.dbc.search.work.presentation.worker.tree.ObjectTree;
import dk.dbc.search.work.presentation.worker.tree.UnitTree;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ParallelCacheContentBuilderIT extends JpaBase {

    @Test
    public void testPartsGetsDeleted() throws Exception {
        System.out.println("testPartsGetsDeleted");

        System.out.println("  Build a tree");
        WorkTree workTree = new WorkTree("work:1", Instant.now());
        UnitTree unit1 = new UnitTree(true, Instant.now());
        workTree.put("unit:1", unit1);
        ObjectTree obj1 = new ObjectTree(true, Instant.now());
        unit1.put("obj:1", obj1);
        obj1.put("100000", new CacheContentBuilderMock("obj:1", "localData.100000", Instant.now(), false));
        obj1.put("100001", new CacheContentBuilderMock("obj:1", "localData.100001", Instant.now(), false));
        ObjectTree obj2 = new ObjectTree(false, Instant.now());
        unit1.put("obj:2", obj2);
        obj2.put("200000", new CacheContentBuilderMock("obj:2", "localData.200000", Instant.now(), false));
        obj2.put("200001", new CacheContentBuilderMock("obj:2", "localData.200001", Instant.now(), false));
        UnitTree unit2 = new UnitTree(false, Instant.now());
        workTree.put("unit:2", unit2);
        ObjectTree obj3 = new ObjectTree(false, Instant.now());
        unit2.put("obj:3", obj3);
        obj3.put("300000", new CacheContentBuilderMock("obj:3", "localData.300000", Instant.now(), false));
        workTree.prettyPrint(System.out::println);

        System.out.println("  Save the full tree");
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    ParallelCacheContentBuilder pccb = beanFactory.getParallelCacheContentBuilder();
                    pccb.updateCache(workTree);
                    pccb.updateWorkContains(workTree);
                });

        System.out.println("  Verify tree is in database");
        jpa(em -> {
            assertThat(CacheEntity.from(em, "100000:1").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "100001:1").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "200000:2").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "200001:2").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "300000:3").getContent(), not(nullValue()));
        });

        System.out.println("  Delete datastream from tree");
        obj2.put("200001", new CacheContentBuilderMock("obj:2", "localData.200001", Instant.now(), true));

        System.out.println("  Save the tree with a deleted datastream");
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    ParallelCacheContentBuilder pccb = beanFactory.getParallelCacheContentBuilder();
                    pccb.updateCache(workTree);
                    pccb.updateWorkContains(workTree);
                });

        System.out.println("  Verify tree is in database without datastream");
        jpa(em -> {
            assertThat(CacheEntity.from(em, "100000:1").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "100001:1").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "200000:2").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "200001:2").getContent(), nullValue()); // No datastream in database
            assertThat(CacheEntity.from(em, "300000:3").getContent(), not(nullValue()));
        });

        System.out.println("  Delete unit:2 from tree");
        workTree.remove("unit:2");

        System.out.println("  Save the tree with a deleted datastream");
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    ParallelCacheContentBuilder pccb = beanFactory.getParallelCacheContentBuilder();
                    pccb.updateCache(workTree);
                    pccb.updateWorkContains(workTree);
                });

        System.out.println("  Verify tree is in database without unit");
        jpa(em -> {
            assertThat(CacheEntity.from(em, "100000:1").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "100001:1").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "200000:2").getContent(), not(nullValue()));
            assertThat(CacheEntity.from(em, "200001:2").getContent(), nullValue());
            assertThat(CacheEntity.from(em, "300000:3").getContent(), nullValue()); // No unit in database
        });

        System.out.println("  Delete the entire tree");
        WorkTree deletedWorkTree = new WorkTree("work:1", Instant.now());

        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    ParallelCacheContentBuilder pccb = beanFactory.getParallelCacheContentBuilder();
                    pccb.updateCache(deletedWorkTree);
                    pccb.updateWorkContains(deletedWorkTree);
                });

        System.out.println("  Verify tree is gone in the database");
        jpa(em -> {
            assertThat(CacheEntity.from(em, "100000:1").getContent(), nullValue());
            assertThat(CacheEntity.from(em, "100001:1").getContent(), nullValue());
            assertThat(CacheEntity.from(em, "200000:2").getContent(), nullValue());
            assertThat(CacheEntity.from(em, "200001:2").getContent(), nullValue());
            assertThat(CacheEntity.from(em, "300000:3").getContent(), nullValue());
        });
    }

    private static class CacheContentBuilderMock extends CacheContentBuilder {

        public CacheContentBuilderMock(String string, String string1, Instant instnt, boolean bln) {
            super(string, string1, instnt, bln);
        }

        @Override
        public ManifestationInformation generateContent(CorepoContentServiceConnector corepoContentService, JavascriptCacheObjectBuilder js) throws Exception {
            ManifestationInformation mi = new ManifestationInformation();
            mi.manifestationId = getManifestationId();
            return mi;
        }
    }
}
