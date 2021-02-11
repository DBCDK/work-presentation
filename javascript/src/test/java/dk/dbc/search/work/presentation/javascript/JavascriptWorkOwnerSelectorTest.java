/*
 * Copyright (C) 2021 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-javascript
 *
 * work-presentation-javascript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-javascript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.javascript;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import java.io.InputStream;
import java.util.HashMap;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JavascriptWorkOwnerSelectorTest {

    protected static final ObjectMapper O = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    @Test
    public void testOwnerMatch() throws Exception {
        System.out.println("testOwnerMatch");

        try(InputStream is = getClass().getClassLoader().getResourceAsStream("select-work-870970-basis-25912233.json")) {
            HashMap<String, ManifestationInformation> potentialOwners = O.readValue(is, new TypeReference<HashMap<String, ManifestationInformation>>(){});
            Supplier<JavascriptWorkOwnerSelector> supplier = JavascriptWorkOwnerSelector.builder().build();
            JavascriptWorkOwnerSelector workOwnerSelector = supplier.get();
            String owner = workOwnerSelector.selectOwner(potentialOwners);
            assertThat(owner, is("870970-basis:25912233"));
        }
    }
    @Test
    public void testOwnerMatch2() throws Exception {
        System.out.println("testOwnerMatch2");

        try(InputStream is = getClass().getClassLoader().getResourceAsStream("select-work-800010-katalog-99122034426005763__1.json")) {
            HashMap<String, ManifestationInformation> potentialOwners = O.readValue(is, new TypeReference<HashMap<String, ManifestationInformation>>(){});
            Supplier<JavascriptWorkOwnerSelector> supplier = JavascriptWorkOwnerSelector.builder().build();
            JavascriptWorkOwnerSelector workOwnerSelector = supplier.get();
            String owner = workOwnerSelector.selectOwner(potentialOwners);
            assertThat(owner, is("800010-katalog:99122034426005763__1"));
        }
    }
}
