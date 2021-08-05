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
import dk.dbc.search.work.presentation.worker.corepo.RelsExtType;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import dk.dbc.search.work.presentation.worker.tree.ObjectTree;
import dk.dbc.search.work.presentation.worker.tree.RelationTree;
import dk.dbc.search.work.presentation.worker.tree.TypedRelation;
import dk.dbc.search.work.presentation.worker.tree.UnitTree;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.ejb.AsyncResult;
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
            void setWorkContains(WorkTree tree) {
            }

            @Override
            protected String findOwnerOfWork(WorkTree tree, Map<String, ManifestationInformation> manifestationCache, String corepoWorkId) {
                return findPotentialOwners(tree, manifestationCache).keySet().stream().sorted().findFirst().orElse("ANY");
            }
        };
        workConsolidator.asyncCacheContentBuilder = new AsyncCacheContentBuilder() {
            @Override
            public Future<ManifestationInformation> getFromCache(CacheContentBuilder dataBuilder, Map<String, String> mdc, boolean delete) {
                ManifestationInformation manifestationInformation = source.getManifestationInformation(dataBuilder.getManifestationId());
                System.out.println("manifestationInformation = " + manifestationInformation);
                return new AsyncResult<>(manifestationInformation);
            }
        };

        WorkInformation actual = workConsolidator.buildWorkInformation(workTree, "corepo:any");

        WorkInformation expected = O.readValue(dir.resolve("expected.json").toFile(), WorkInformation.class);

        System.out.println("Actual:");
        O.writeValue(System.out, actual);
        System.out.println("");
        if (!expected.equals(actual)) {
            System.out.println("Expected:");
            O.writeValue(System.out, expected);
            System.out.println("");

            O.writeValue(dir.resolve("actual.json").toFile(), actual);
        }

        assertThat("testBuildWorkInformation: " + directory.getName(), actual, is(expected));
    }

    Source readSource(File sourceFile) throws IOException {
        Source source = O.readValue(sourceFile, Source.class);
        source.units.values()
                .forEach(u -> u
                        .forEach((objId, obj) -> {
                            obj.forEach((stream, m) -> {
                                String mId = manifestationIdOf(objId, stream);
                                m.manifestationId = mId;
                            });
                        }));
        if (source.relations != null) {
            source.relations.values()
                    .forEach(r -> r.values()
                            .forEach(u -> u
                                    .forEach((objId, obj) -> {
                                        obj.forEach((stream, m) -> {
                                            String mId = manifestationIdOf(objId, stream);
                                            m.manifestationId = mId;
                                        });
                                    })));
        }
        return source;
    }

    String manifestationIdOf(String objId, String stream) {
        return stream.substring(CacheContentBuilder.LOCAL_DATA_LEN) + ":" + objId.substring(objId.indexOf(':') + 1);
    }

    // Any object whose id ends in 0 is primary
    public WorkTree workTreeFrom(Source source) {
        WorkTree workTree = new WorkTree("work:-1", Instant.now());

        source.units.forEach((unitId, unit) -> {
            UnitTree unitTree = new UnitTree(Instant.now());
            workTree.put(unitId, unitTree);
            unit.forEach((objId, obj) -> {
                boolean primaryObj = objId.endsWith("0");
                ObjectTree objectTree = new ObjectTree(primaryObj, Instant.now());
                unitTree.put(objId, objectTree);
                obj.forEach((localStream, mani) -> {
                    CacheContentBuilder builder = new CacheContentBuilder(objId, localStream, Instant.now(), false) {
                        @Override
                        public String toString() {
                            return mani.toString();
                        }
                    };
                    objectTree.put(builder.getManifestationId(), builder);
                });
            });
            if (source.relationMap != null) {
                source.relationMap.getOrDefault(unitId, Collections.EMPTY_LIST).forEach(new Consumer<String>() {
                    @Override
                    public void accept(String relUnitId) {
                        source.relations.get(relUnitId)
                                .keySet()
                                .forEach(type -> {
                                    unitTree.addRelation(RelsExtType.from(type), relUnitId);
                                });
                    }
                });
            }
        });
        if (source.relations != null) {
            source.relations.forEach((unitId, unit) -> {
                unit.forEach((type, relations) -> {
                    RelsExtType relType = RelsExtType.from(type);
                    TypedRelation key = new TypedRelation(relType, unitId);
                    RelationTree relationTree = new RelationTree(relType);
                    workTree.addRelation(key, relationTree);
                    relations.forEach((objId, obj) -> {
                        boolean primaryObj = objectHasPrimary(obj, objId, source.primary);
                        ObjectTree objectTree = new ObjectTree(primaryObj, Instant.now());
                        relationTree.put(objId, objectTree);
                        obj.forEach((localStream, mani) -> {
                            CacheContentBuilder builder = new CacheContentBuilder(objId, localStream, Instant.now(), false) {
                                @Override
                                public String toString() {
                                    return mani.toString();
                                }
                            };
                            objectTree.put(builder.getManifestationId(), builder);
                        });
                    });
                });
            });
        }
        return workTree;
    }

    boolean objectHasPrimary(TreeMap<String, ManifestationInformation> obj, String objId, String primary) {
        return obj.keySet().stream()
                .map(stream -> manifestationIdOf(objId, stream))
                .anyMatch(primary::equals);
    }

    public static class Source {

        public String primary;
        //             Unit            Object          Stream
        public TreeMap<String, TreeMap<String, TreeMap<String, ManifestationInformation>>> units;
        //             RelationUnit    RelationType    Object          Stream
        public TreeMap<String, TreeMap<String, TreeMap<String, TreeMap<String, ManifestationInformation>>>> relations;
        //             Unit    RelationUnit
        public TreeMap<String, List<String>> relationMap;

        public ManifestationInformation getManifestationInformation(String id) {
            Stream<ManifestationInformation> unitStream = units.values().stream() // Unit
                    .map(Map::values)
                    .flatMap(Collection::stream) //Object
                    .map(Map::values)
                    .flatMap(Collection::stream); // ManifestationInformation

            Stream<ManifestationInformation> relStream = Stream.empty();
            if (relations != null) {
                relStream = relations.values().stream() // Unit
                        .map(Map::values)
                        .flatMap(Collection::stream) //RelationType
                        .map(Map::values)
                        .flatMap(Collection::stream) //Object
                        .map(Map::values)
                        .flatMap(Collection::stream); // ManifestationInformation
            }
            return Stream.concat(unitStream, relStream)
                    .filter(m -> m.manifestationId.equals(id))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Trying to get content for id: " + id));
        }

        @Override
        public String toString() {
            return "Source{" + "primary=" + primary + ", units=" + units + ", relations=" + relations + '}';
        }
    }

    private static Stream<Arguments> tests() {
        URL testResource = WorkConsolidatorTest.class.getClassLoader()
                .getResource("WorkConsolidator");
        File[] dirs = new File(testResource.getFile()).listFiles(dir -> {
            return dir.isDirectory();// && dir.getName().equals("include-relations");
        });
        return Stream.of(dirs).map(Arguments::of);
    }

}
