/*
 * Copyright (C) 2021 DBC A/S (http://dbc.dk/)
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
package dk.dbc.search.work.presentation.service;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class NaturalSortTest {

    @Test
    public void testNumbers() throws Exception {
        System.out.println("testNumbers");
        List<String> actual = Stream.of("a8b", "a8a", "a6b2", "a6b3", "a12d", "4")
                .sorted(new NaturalSort())
                .collect(toList());

        assertThat(actual, contains("4", "a6b2", "a6b3", "a8a", "a8b", "a12d"));
    }

    @Test
    public void testDecimals() throws Exception {
        System.out.println("testDecimals");
        List<String> actual = Stream.of("1.234", "1.2", "1.1.1")
                .sorted(new NaturalSort())
                .collect(toList());

        assertThat(actual, contains("1.1.1", "1.2", "1.234"));
    }

    @Test
    public void testZeroPrefix() throws Exception {
        System.out.println("testZeroPrefix");
        List<String> actual = Stream.of("004", "1")
                .sorted(new NaturalSort())
                .collect(toList());

        assertThat(actual, contains("1", "004"));
    }
}
