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

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class SolrTest {

    @Test
    public void testSliceCollection() throws Exception {
        System.out.println("testSliceCollection");
        Stream<String> in = Stream.of("1", "2", "3", "4", "5", "6", "7");
        List<List<String>> slices = Solr.sliceCollection(in, 3);
        assertThat(slices, contains(contains("1", "2", "3"),
                                    contains("4", "5", "6"),
                                    contains("7")));
    }
}
