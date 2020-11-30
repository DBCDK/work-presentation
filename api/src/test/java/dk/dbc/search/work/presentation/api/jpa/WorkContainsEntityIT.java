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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkContainsEntityIT extends JpaBase<AutoCloseable> {

    @Test
    public void saveMultiple() throws Exception {
        System.out.println("saveMultiple");
        String corepoWorkId = "870970-basis:25912233";

        System.out.println(" - Make a work");
        jpa(em -> {
            List<WorkContainsEntity> works = Stream.of(corepoWorkId, "710100-katalog:25912233", "766500-katalog:25912233")
                    .map(m -> WorkContainsEntity.from(em, corepoWorkId, m))
                    .collect(toList());
            System.out.println("works = " + works);
            WorkContainsEntity.updateToList(em, corepoWorkId, works);
        });

        System.out.println(" - Verify work from database");
        jpa(em -> {
            List<WorkContainsEntity> works = WorkContainsEntity.listFrom(em, "870970-basis:25912233");
            List<String> manifestationIds = works.stream().map(WorkContainsEntity::getManifestationId).collect(toList());
            System.out.println("manifestationIds = " + manifestationIds);
            assertThat(manifestationIds, containsInAnyOrder("710100-katalog:25912233", "766500-katalog:25912233", "870970-basis:25912233"));
        });

        System.out.println(" - Make a work with different content");
        jpa(em -> {
            List<WorkContainsEntity> works = Stream.of(corepoWorkId, "710100-katalog:25912233", "911116-katalog:25912233")
                    .map(m -> WorkContainsEntity.from(em, corepoWorkId, m))
                    .collect(toList());
            System.out.println("works = " + works);
            WorkContainsEntity.updateToList(em, corepoWorkId, works);
        });

        System.out.println(" - Verify work from database");
        jpa(em -> {
            List<WorkContainsEntity> works = WorkContainsEntity.listFrom(em, "870970-basis:25912233");
            List<String> manifestationIds = works.stream().map(WorkContainsEntity::getManifestationId).collect(toList());
            System.out.println("manifestationIds = " + manifestationIds);
            assertThat(manifestationIds, containsInAnyOrder("710100-katalog:25912233", "870970-basis:25912233", "911116-katalog:25912233"));
        });
    }

    @Override
    public AutoCloseable createBeanFactory(Map<String, String> env, EntityManager em, EntityManagerFactory emf) {
        return () -> {
        };
    }

}
