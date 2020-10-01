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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.EJBException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ConfigTest {

    @Test
    public void testPostpone() throws Exception {
        System.out.println("testPostpone");

        long threeSeconds = 3L * 1000L;
        long sixMinutes = 6L * 60L * 1000L;

        Config config = new Config();
        config.computePostponeParameters("3s-6m");
        // List of random numbers in range
        List<Long> longs = Stream.generate(config::postponeDuration)
                .limit(25)
                .collect(Collectors.toList());

        long min = longs.stream()
                .mapToLong(l -> l)
                .min()
                .orElseThrow(() -> new IllegalStateException());
        assertThat(min, greaterThanOrEqualTo(threeSeconds));
        assertThat(min, lessThan(sixMinutes)); // Cannot be exactly 6m (25 times the same is unlikely)

        long max = longs.stream()
                .mapToLong(l -> l)
                .max()
                .orElseThrow(() -> new IllegalStateException());
        assertThat(max, greaterThan(threeSeconds)); // Cannot be exactly 3s (25 times the same is unlikely)
        assertThat(max, lessThanOrEqualTo(sixMinutes));
    }

    @Test
    public void testMS() throws Exception {
        System.out.println("testMS");
        String message;

        message = assertThrows(EJBException.class, () -> {
                           Config.ms("32");
                       }).getMessage();
        assertThat(message, containsString("not of format"));

        message = assertThrows(EJBException.class, () -> {
                           Config.ms("sec");
                       }).getMessage();
        assertThat(message, containsString("not of format"));

        message = assertThrows(EJBException.class, () -> {
                           Config.ms("2 doh");
                       }).getMessage();
        assertThat(message, containsString("unknown unit"));

        assertThat(Config.ms("0ms"), is(0L));
        assertThat(Config.ms("1ms"), is(1L));
        assertThat(Config.ms("123ms"), is(123L));
        assertThat(Config.ms("123milli"), is(123L));
        assertThat(Config.ms("123 millis"), is(123L));
        assertThat(Config.ms("1 second"), is(1000L));
        assertThat(Config.ms("123 seconds"), is(123000L));
        assertThat(Config.ms("123 secs"), is(123000L));
        assertThat(Config.ms("1 min"), is(60000L));
        assertThat(Config.ms("5 minutes"), is(300000L));
    }

}
