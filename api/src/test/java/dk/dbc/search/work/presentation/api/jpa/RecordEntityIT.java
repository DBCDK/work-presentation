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

import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.api.pojo.TypedValue;
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class RecordEntityIT extends JpaBase<AutoCloseable> {

    public RecordEntityIT() {
    }

    @Test
    public void testSaveLoad() throws Exception {
        System.out.println("testSaveLoad");

        ManifestationInformation mi = new ManifestationInformation();
        mi.manifestationId = "c";
        mi.title = "mTitle";
        mi.materialTypes = Arrays.asList("book");
        Set<ManifestationInformation> ml = Collections.singleton(mi);

        WorkInformation wi = new WorkInformation();
        wi.workId = "a";
        wi.title = "title";
        TypedValue creator = new TypedValue();
        creator.type = "aut";
        creator.value = "hans andersen";
        wi.creators = Collections.singleton(creator);
        TypedValue typedValue = new TypedValue();
        typedValue.type = null;
        typedValue.value = "emne";
        wi.subjects = Collections.singleton(typedValue);
        wi.description = "beskrivelse";
        Map<String, Set<ManifestationInformation>> unitInfo = new HashMap<>();
        unitInfo.put("unitId", ml);
        wi.dbUnitInformation = unitInfo;


        WorkObjectEntity oldEntity = new WorkObjectEntity("a", "b", Timestamp.from(Instant.now()), wi);
        jpa(em -> {
            em.persist(oldEntity);
        });

        jpa(em -> {
            WorkObjectEntity newEntity = WorkObjectEntity.from(em, "a");
            System.out.println("newEntity = " + newEntity);
            System.out.println("oldEntity = " + oldEntity);

            assertThat(newEntity, is(oldEntity));
        });

    }

    @Test
    public void testSaveAndDelete() throws Exception {
        System.out.println("testSaveAndDelete");

        jpa(em -> {
            WorkObjectEntity rec = WorkObjectEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(true)); // NEW
            rec.setContent(new WorkInformation());
            rec.setCorepoWorkId("any");
            rec.setModified(Timestamp.from(Instant.now()));
            rec.save();
        });
        jpa(em -> {
            WorkObjectEntity rec = WorkObjectEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(false)); // FROM DB
            rec.save();
        });
        jpa(em -> {
            WorkObjectEntity rec = WorkObjectEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(false)); // FROM DB
            rec.delete();
        });
        jpa(em -> {
            WorkObjectEntity rec = WorkObjectEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(true)); // NEW
        });
    }

    @Override
    public AutoCloseable createBeanFactory(Map<String, String> env, EntityManager em, EntityManagerFactory emf) {
        return () -> {
        };
    }

}
