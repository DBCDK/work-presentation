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
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class TypedValueTest {
    private final String not_spec = "not_specified";

    @Test
    public void testEmptyList() throws Exception {
        System.out.println("testEmptyList");
        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList());
        assertThat(set, is(empty()));
    }

    @Test
    public void testMultipleDifferentValues() throws Exception {
        System.out.println("testMultipleDifferentValues");

        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", "abc"),
                TypedValue.with(null, "def")));

        assertThat(set, containsInAnyOrder(
                TypedValue.with(not_spec, "abc"),
                TypedValue.with(not_spec, "def"))
        );
    }

    @Test
    public void testDifferentTypes() throws Exception {
        System.out.println("testDifferentTypes");
        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("1", "abc"),
                TypedValue.with("a", "def"),
                TypedValue.with("", "ghi"),
                TypedValue.with(null, "jkl")));

        assertThat(set, containsInAnyOrder(
                TypedValue.with("1", "abc"),
                TypedValue.with("a", "def"),
                TypedValue.with(not_spec, "ghi"),
                TypedValue.with(not_spec, "jki"))
        );
    }

    @Test
    public void testRemoveDuplicates() throws Exception {
        System.out.println("testRemoveDuplicates");
        Set<TypedValue> set = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", "abc"),
                TypedValue.with("", "abc")));

        assertThat(set, contains(
                TypedValue.with(not_spec, "abc")));

        assertThat(set.size(), equalTo(1));
    }

    @Test
    public void testUseCapitalized() throws Exception {
        System.out.println("testUseCapitalized");

        Set<TypedValue> set1 = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", "abc"),
                TypedValue.with("", "Abc")));

        assertThat(set1, contains(
                TypedValue.with(not_spec, "Abc")));

        Set<TypedValue> set2 = TypedValue.distinctSet(Arrays.asList(
                TypedValue.with("", "Abc"),
                TypedValue.with("", "abc")));

        assertThat(set2, contains(
                TypedValue.with(not_spec, "Abc")));
    }
}
