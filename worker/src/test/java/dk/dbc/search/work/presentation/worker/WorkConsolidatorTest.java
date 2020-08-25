/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-worker
 *
 * work-presentation-worker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-worker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import dk.dbc.search.work.presentation.worker.tree.ObjectTree;
import dk.dbc.search.work.presentation.worker.tree.UnitTree;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkConsolidatorTest {

    private static final ObjectMapper O = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    @ParameterizedTest
    @MethodSource("tests")
    public void testBuildWorkInformation(File directory) throws Exception {
        System.out.println("testBuildWorkInformation: " + directory.getName());
        Path dir = directory.toPath();
        Source source = readSource(dir.resolve("source.json").toFile());
        WorkTree workTree = workTreeFrom(source);
        workTree.prettyPrint(System.out::println);

        WorkConsolidator workConsolidator = new WorkConsolidator() {
            @Override
            ManifestationInformation getCacheContentFor(String id) {
                return source.getManifestationInformation(id);
            }
        };

        WorkInformation actual = workConsolidator.buildWorkInformation(workTree);

        WorkInformation expected = O.readValue(dir.resolve("expected.json").toFile(), WorkInformation.class);

        if (!expected.equals(actual)) {
            System.out.println("Actual:");
            O.writeValue(System.out, actual);
            System.out.println("");
            System.out.println("Expected:");
            O.writeValue(System.out, expected);
            System.out.println("");

            O.writeValue(dir.resolve("actual.json").toFile(), actual);
        }

        assertThat(actual, is(expected));
    }

    Source readSource(File sourceFile) throws IOException {
        Source source = O.readValue(sourceFile, Source.class);
        source.units.values()
                .forEach(u -> u.values()
                        .forEach(obj -> {
                            obj.forEach((mId, m) -> {
                                m.manifestationId = mId;
                            });
                        }));
        return source;
    }

    public WorkTree workTreeFrom(Source source) {
        WorkTree workTree = new WorkTree("work:-1", Instant.now());

        source.units.forEach((unitId, unit) -> {
            boolean primaryUnit = unit.values().stream()
                    .map(Map::keySet)
                    .flatMap(Collection::stream)
                    .anyMatch(k -> source.primary.equals(k));
            UnitTree unitTree = new UnitTree(primaryUnit, Instant.now());
            workTree.put(unitId, unitTree);
            unit.forEach((objId, obj) -> {
                boolean primaryObj = obj.keySet().contains(source.primary);
                ObjectTree objectTree = new ObjectTree(primaryObj, Instant.now());
                unitTree.put(objId, objectTree);
                obj.forEach((maniId, mani) -> {
                    objectTree.put(maniId, new CacheContentBuilder("000000-na:0", "localData.000000", Instant.now(), false) {
                               @Override
                               public String toString() {
                                   return mani.toString();
                               }
                           });
                });
            });
        });
        return workTree;
    }

    public static class Source {

        public String primary;
        public TreeMap<String, TreeMap<String, TreeMap<String, ManifestationInformation>>> units;

        public ManifestationInformation getManifestationInformation(String id) {
            return units.values().stream()
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .filter(e -> e.getKey().equals(id))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Trying to get content for id: " + id));
        }

        @Override
        public String toString() {
            return "Source{" + "primary=" + primary + ", units=" + units + '}';
        }
    }

    private static Stream<Arguments> tests() {
        URL testResource = WorkConsolidatorTest.class.getClassLoader()
                .getResource("WorkConsolidator");
        File[] dirs = new File(testResource.getFile()).listFiles(dir -> {
            return dir.isDirectory();
        });
        return Stream.of(dirs).map(Arguments::of);
    }

}
