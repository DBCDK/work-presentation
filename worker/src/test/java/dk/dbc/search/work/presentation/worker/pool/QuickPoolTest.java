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
 * along call this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker.pool;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class QuickPoolTest {

    @Test
    public void testPool() throws Exception {
        System.out.println("testPool");
        assertTimeout(Duration.ofMillis(1_000L), () -> {
                  QuickPool<Integer> pool = new QuickPool<>(new AtomicInteger(0)::incrementAndGet);
                  pool.setBlockWhenExhausted(true);
                  pool.setMaxTotal(3);

                  ConcurrentHashMap<Integer, AtomicInteger> objectCalls = new ConcurrentHashMap<>();

                  ExecutorService ex = Executors.newFixedThreadPool(8);
                  for (int n = 0 ; n < 12 ; n++) {
                      ex.submit(() -> {
                          try {
                              pool.valueExec(i -> {
                                  objectCalls.computeIfAbsent(i, x -> new AtomicInteger(0)).incrementAndGet();
                                  Thread.sleep(10);
                                  if (i == 2) // Object 2 we don't like
                                      throw new BadObjectException();
                                  return null;
                              })
                                      .value();
                          } catch (BadObjectException e) {
                          }
                      });
                  }
                  ex.shutdown();
                  ex.awaitTermination(1, TimeUnit.SECONDS);

                  System.out.println("map = " + objectCalls);
                  assertThat("number of pool object generated", objectCalls.size(), is(4));
                  assertThat("Object 2 only used once", objectCalls.get(2).get(), is(1));
                  int calls = objectCalls.values().stream().mapToInt(AtomicInteger::get).sum();
                  assertThat("Number of registered calls", calls, is(12));
              });
    }

}
