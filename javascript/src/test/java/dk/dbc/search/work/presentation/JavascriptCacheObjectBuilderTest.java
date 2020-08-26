/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
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
package dk.dbc.search.work.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class JavascriptCacheObjectBuilderTest {

    private static Supplier<JavascriptCacheObjectBuilder> jsSupplier;
    private static final ObjectMapper O = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    @BeforeAll
    public static void setUpClass() {
        jsSupplier = JavascriptCacheObjectBuilder.builder()
                .classLoader(JavascriptCacheObjectBuilderTest.class.getClassLoader())
                .build();
    }

    @ParameterizedTest
    @MethodSource("testParameters")
    public void testExtractManifestationInformation(Path dir) throws Exception {
        System.out.println("testExtractManifestationInformation");
        String manifestationId = dir.getFileName().toString();
        System.out.println(" `-- " + manifestationId);
        assertTimeout(Duration.ofSeconds(10L), () -> {
                  HashMap<String, String> xmlObjects = new HashMap<>();
                  ManifestationInformation expected = null;
                  for (File file : dir.toFile().listFiles()) {
                      String name = file.getName();
                      if (name.equals("actual.json")) {
                      } else if (name.equals("expected.json")) {
                          expected = O.readValue(file, ManifestationInformation.class);
                      } else if (name.toLowerCase(Locale.ROOT).endsWith(".xml")) {
                          name = name.substring(0, name.length() - 4);
                          String content = FileUtils.readFileToString(file, UTF_8);
                          xmlObjects.put(name, content);
                      } else {
                          fail("Don't know what to do with: " + file);
                      }
                  }
                  JavascriptCacheObjectBuilder js = jsSupplier.get();
                  ManifestationInformation information = js.extractManifestationInformation(manifestationId, xmlObjects);
                  System.out.println("information = " + information);
                  try (FileOutputStream os = new FileOutputStream(dir.resolve("actual.json").toFile())) {
                      O.writeValue(os, information);
                  }
                  assertThat(information, is(expected));
              });
    }

    private static Stream<Arguments> testParameters() {
        return Stream.of(new File(
                JavascriptCacheObjectBuilderTest.class.getClassLoader()
                        .getResource(".")
                        .getPath())
                .toPath()
                .resolve("accept-test")
                .toFile()
                .listFiles())
                .filter(File::isDirectory)
                .map(File::toPath)
                .map(p -> Arguments.of(p));
    }
}
