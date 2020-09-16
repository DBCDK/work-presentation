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
import dk.dbc.search.work.presentation.api.jpa.RecordEntity;
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
            WorkPresentationBean wpb = bf.getWorkPresentationBean();
            try {
                WorkInformationResponse actual = wpb.processRequest(args.workId, "190102", "danbib", "track-me");
                System.out.println("actual = " + actual);
                WorkInformationResponse expected = O.readValue(dir.resolve("expected.json").toFile(), WorkInformationResponse.class);
                O.writeValue(dir.resolve("actual.json").toFile(), actual);
                if (!expected.equals(actual)) {
                    System.out.println("Actual:");
                    O.writeValue(System.out, actual);
                    System.out.println();
                    System.out.println("Expected:");
                    O.writeValue(System.out, expected);
                    System.out.println();
                }
                assertThat(actual, is(expected));
            } catch (Exception ex) {
                System.out.println("  Exception:" + ex.getClass().getName() + ", " + ex.getMessage());
                log.error("Exception: {}", ex.getMessage());
                log.debug("Exception: ", ex);
                ExpectedError expected = O.readValue(dir.resolve("expected.json").toFile(), ExpectedError.class);
                assertThat(ex.getClass().getName(), is(expected.exception));
                if (expected.regexp != null)
                    assertThat(ex.getMessage(), matchesPattern(expected.regexp));
            }
        });
    }

    private void setupTestData(Path dir) throws IOException {
        WorkInformation work = O.readValue(dir.resolve("source.json").toFile(), WorkInformation.class);
        System.out.println("work = " + work);

        jpa(em -> {
            RecordEntity record = RecordEntity.from(em, work.workId);
            record.setContent(work);
            record.setCorepoWorkId("work:0");
            record.setModified(Timestamp.from(Instant.now()));
            record.save();

            work.dbUnitInformation.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(m -> m.manifestationId)
                    .forEach(mId -> WorkContainsEntity.from(em, "work:0", mId).save());
        });
    }

    private static Stream<Arguments> tests() {
        URL url = WorkPresentationBeanIT.class.getClassLoader().getResource("WorkPresentationBean");
        System.out.println("url = " + url);
        return Stream.of(new File(url.getPath()).listFiles(File::isDirectory))
                .map(File::toPath)
                .map(Arguments::of);
    }

    public static class CallArguments {

        public String workId;
        public Set<String> accessibleManifestations;
    }

    public static class ExpectedError {

        public String exception;
        public String regexp;
    }

}
