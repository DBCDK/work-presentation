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
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ManifestationInformation mi = new ManifestationInformation();
        mi.manifestationId = "c";
        mi.title = "mTitle";
        mi.materialType = Arrays.asList("book");
        List<ManifestationInformation> ml = Arrays.asList(mi);

        WorkInformation wi = new WorkInformation();
        wi.workId = "a";
        wi.title = "title";
        wi.creators = Arrays.asList("hans andersen");
        wi.subjects = Arrays.asList("emne");
        wi.description = "beskrivelse";
        wi.manifestationInformationList = ml;
        Map<String, List<ManifestationInformation>> unitInfo = new HashMap<String, List<ManifestationInformation>>();
        unitInfo.put("unitId", ml);
        wi.dbUnitInformation = unitInfo;

        RecordEntity oldEntity = new RecordEntity("a", "b", Timestamp.from(Instant.now()), wi );

        jpa(em -> {
            em.persist(oldEntity);
        });
        flushAndEvict();
        RecordEntity newEntity = jpa(em -> {
            return RecordEntity.from(em, "a");
        });

        System.out.println("newEntity = " + newEntity);
        System.out.println("oldEntity = " + oldEntity);

        assertThat(newEntity, is(oldEntity));
    }

    @Test
    public void testSaveAndDelete() throws Exception {
        System.out.println("testSaveAndDelete");

        flushAndEvict();
        jpa(em -> {
            RecordEntity rec = RecordEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(true)); // NEW
            rec.setContent(new WorkInformation());
            rec.setCorepoWorkId("any");
            rec.setModified(Timestamp.from(Instant.now()));
            rec.save();
        });
        flushAndEvict();
        jpa(em -> {
            RecordEntity rec = RecordEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(false)); // FROM DB
            rec.save();
        });
        flushAndEvict();
        jpa(em -> {
            RecordEntity rec = RecordEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(false)); // FROM DB
            rec.delete();
        });
        flushAndEvict();
        jpa(em -> {
            RecordEntity rec = RecordEntity.from(em, "work-of-x");
            assertThat(rec.persist, is(true)); // NEW
        });
    }

}
