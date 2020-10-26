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
package dk.dbc.search.work.presentation.api.pojo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class TypedValueTest {
    private final String fallbackType = "not_specified";

    @Test
    public void testEmptyList() throws Exception {
        System.out.println("testEmptyList");
        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList(), fallbackType);
        assertThat(set, is(empty()));
    }

    @Test
    public void testMultipleDifferentValues() throws Exception {
        System.out.println("testMultipleDifferentValues");

        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", fallbackType,"abc"),
                TypedValue.with(null, fallbackType, "def")
        ), fallbackType);

        assertThat(set, containsInAnyOrder(
                   TypedValue.with(fallbackType, fallbackType, "abc"),
                   TypedValue.with(fallbackType, fallbackType, "def"))
        );
    }

    @Test
    public void testDifferentTypes() throws Exception {
        System.out.println("testDifferentTypes");
        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("1", fallbackType, "abc"),
                TypedValue.with("a", fallbackType, "def")
        ), fallbackType);

        assertThat(set, containsInAnyOrder(
                   TypedValue.with("1", fallbackType, "abc"),
                   TypedValue.with("a", fallbackType, "def")));
    }

    @Test
    public void testRemoveDuplicates() throws Exception {
        System.out.println("testRemoveDuplicates");
        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", fallbackType, "abc"),
                TypedValue.with("", fallbackType, "abc")
        ), fallbackType);

        assertThat(set, contains(
                   TypedValue.with(fallbackType, fallbackType,"abc")));
        assertThat(set, contains(
                   TypedValue.with(fallbackType, fallbackType, "abc")));
    }

    @Test
    public void testUseCapitalized() throws Exception {
        System.out.println("testUseCapitalized");

        Set<TypedValue> set1 = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", fallbackType, "abc"),
                TypedValue.with("", fallbackType, "Abc")
        ), fallbackType);

        assertThat(set1, contains(
                   TypedValue.with(fallbackType, fallbackType, "Abc")));

        Set<TypedValue> set2 = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", fallbackType, "Abc"),
                TypedValue.with("", fallbackType, "abc")
        ), fallbackType);

        assertThat(set2, contains(
                   TypedValue.with(fallbackType, fallbackType, "Abc")));
    }

    @Test
    public void testFallbackType() throws Exception {
        System.out.println("testFallbackType");

        Set<TypedValue> set1 = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", fallbackType, "abc"),
                TypedValue.with(null, fallbackType, "def")
        ), fallbackType);

        assertThat(set1, containsInAnyOrder(
                TypedValue.with(fallbackType, fallbackType, "abc"),
                TypedValue.with(fallbackType, fallbackType, "def"))
        );

        Set<TypedValue> set2 = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", "", "abc"),
                TypedValue.with(null, "", "def")
        ), "");

        assertThat(set2, containsInAnyOrder(
                TypedValue.with("", "", "abc"),
                TypedValue.with("", "", "def"))
        );
    }
}
