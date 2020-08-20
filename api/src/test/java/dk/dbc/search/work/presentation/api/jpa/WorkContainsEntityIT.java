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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkContainsEntityIT extends JpaBase {

    @Test
    public void saveMultiple() throws Exception {
        System.out.println("saveMultiple");
        assertTimeout(Duration.ofMillis(500L), () -> {
                  jpa(em -> {
                      // Make work a
                      Stream.of("870970-basis:25912233", "710100-katalog:25912233", "766500-katalog:25912233")
                              .forEach(m -> {
                                  WorkContainsEntity wce = WorkContainsEntity.from(em, "870970-basis:25912233", m);
                                  wce.save();
                              });
                      // Make work b
                      Stream.of("870970-basis:23645564", "710100-katalog:23645564")
                              .forEach(m -> {
                                  WorkContainsEntity wce = WorkContainsEntity.from(em, "870970-basis:23645564", m);
                                  wce.save();
                              });
                  });
              });

        assertTimeout(Duration.ofMillis(500L), () -> {
                  System.out.println("fetch list, remove one(766500), add one(911116), update list");
                  flushAndEvict();
                  List<WorkContainsEntity> works = jpa(em -> {
                      return new ArrayList<WorkContainsEntity>(WorkContainsEntity.listFrom(em, "870970-basis:25912233"));
                  });

                  List<String> manifestationIds = works.stream().map(WorkContainsEntity::getManifestationId).collect(toList());
                  System.out.println("manifestationIds = " + manifestationIds);
                  assertThat(manifestationIds, containsInAnyOrder("710100-katalog:25912233", "766500-katalog:25912233", "870970-basis:25912233"));

                  for (Iterator<WorkContainsEntity> iterator = works.iterator() ; iterator.hasNext() ;) {
                      WorkContainsEntity next = iterator.next();
                      if (next.getManifestationId().equals("766500-katalog:25912233"))
                          iterator.remove();
                  }
                  WorkContainsEntity extra = jpa(em -> {
                      WorkContainsEntity wce = WorkContainsEntity.from(em, "870970-basis:25912233", "911116-katalog:25912233");
                      return wce;
                  });

                  works.add(extra);
                  manifestationIds = works.stream().map(WorkContainsEntity::getManifestationId).collect(toList());
                  System.out.println("list = " + manifestationIds);

                  jpa(em -> {
                      WorkContainsEntity.updateToList(em, "870970-basis:25912233", works);
                  });

              });
        assertTimeout(Duration.ofMillis(500L), () -> {
                  System.out.println("validate update list");
                  flushAndEvict();
                  List<WorkContainsEntity> works = jpa(em -> {
                      return new ArrayList<WorkContainsEntity>(WorkContainsEntity.listFrom(em, "870970-basis:25912233"));
                  });

                  List<String> manifestationIds = works.stream().map(WorkContainsEntity::getManifestationId).collect(toList());
                  System.out.println("manifestationIds = " + manifestationIds);
                  assertThat(manifestationIds, containsInAnyOrder("710100-katalog:25912233", "870970-basis:25912233", "911116-katalog:25912233"));
              });
    }
}
