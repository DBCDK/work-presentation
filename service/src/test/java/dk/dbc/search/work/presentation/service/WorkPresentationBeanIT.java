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
package dk.dbc.search.work.presentation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dk.dbc.search.work.presentation.api.jpa.WorkObjectEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkContainsEntity;
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import dk.dbc.search.work.presentation.service.response.WorkInformationResponse;
import dk.dbc.search.work.presentation.service.solr.Solr;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkPresentationBeanIT extends JpaBase {

    private static final Logger log = LoggerFactory.getLogger(WorkPresentationBeanIT.class);

    private static final ObjectMapper O = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    @ParameterizedTest
    @MethodSource("tests")
    public void testCase(Path dir) throws Exception {
        System.out.println("testCase: " + dir.getFileName());
        setupTestData(dir);
        CallArguments args = O.readValue(dir.resolve("arguments.json").toFile(), CallArguments.class);
        withConfigEnv().jpaWithBeans(bf -> {
            bf.setSolr(new Solr() {
                @Override
                public Set<String> getAccessibleManifestations(String workId, String agencyId, String profile, int maxExpectedManifestations, String trackingId) {
                    return args.accessibleManifestations;
                }
                @Override
                    public Set<String> getAccessibleRelations(Set<String> relationIds, String agencyId, String profile, String trackingId) {
                        return args.accessibleRelations;
                    }
            });
            WorkPresentationBean wpb = bf.getWorkPresentationBean();
            try {
                WorkInformationResponse actual = wpb.processRequest(args.workId, "190102", "danbib", args.includeRelations, "track-me");
                System.out.println("actual = " + actual);
                O.writeValue(dir.resolve("actual.json").toFile(), actual);
                WorkInformationResponse expected;
                try {
                    expected = O.readValue(dir.resolve("expected.json").toFile(), WorkInformationResponse.class);
                } catch (IOException ioex) {
                    log.error("Exceprion parsing expected as response: {}", ioex.getMessage());
                    log.debug("Exceprion parsing expected as response: ", ioex);
                    fail("Could not parse expected.json as response");
                    return; // Keep ide quiet about dereferencing null-pointer
                }
                if (!expected.equals(actual)) {
                    System.out.println("Actual:");
                    O.writeValue(System.out, actual);
                    System.out.println();
                    System.out.println("Expected:");
                    O.writeValue(System.out, expected);
                    System.out.println();
                }
                assertThat(actual, is(expected));
            } catch (IOException | RuntimeException ex) {
                System.out.println("  Exception:" + ex.getClass().getName() + ", " + ex.getMessage());
                log.error("Exception: {}", ex.getMessage());
                log.debug("Exception: ", ex);
                ExpectedError expected;
                try {
                    expected = O.readValue(dir.resolve("expected.json").toFile(), ExpectedError.class);
                } catch (IOException ioex) {
                    log.error("Exception parsing expected as error: {}", ioex.getMessage());
                    log.debug("Exception parsing expected as error: ", ioex);
                    fail("Could not parse expected.json as error");
                    return; // Keep ide quiet about dereferencing null-pointer
                }
                assertThat(ex.getClass().getName(), is(expected.exception));
                if (expected.regexp != null)
                    assertThat(ex.getMessage(), matchesPattern(expected.regexp));
            }
        });
    }

    private void setupTestData(Path dir) throws IOException {
        WorkInformation workInformation = O.readValue(dir.resolve("source.json").toFile(), WorkInformation.class);
        System.out.println("workInformation = " + workInformation);

        jpa(em -> {
            WorkObjectEntity work = WorkObjectEntity.from(em, workInformation.workId);
            work.setContent(workInformation);
            work.setCorepoWorkId("work:0");
            work.setModified(Timestamp.from(Instant.now()));
            work.save();

            workInformation.dbUnitInformation.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(m -> m.manifestationId)
                    .forEach(mId -> WorkContainsEntity.from(em, "work:0", mId).save());
        });
    }

    private static Stream<Arguments> tests() {
        URL url = WorkPresentationBeanIT.class.getClassLoader().getResource("WorkPresentationBean");
        return Stream.of(new File(url.getPath()).listFiles(File::isDirectory))
                .map(File::toPath)
                .map(Arguments::of);
    }

    public static class CallArguments {

        public String workId;
        public boolean includeRelations;
        public Set<String> accessibleManifestations;
        public Set<String> accessibleRelations;
    }

    public static class ExpectedError {

        public String exception;
        public String regexp;
    }

}
