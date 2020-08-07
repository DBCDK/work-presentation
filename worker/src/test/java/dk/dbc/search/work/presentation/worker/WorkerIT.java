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

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkerIT extends JpaBaseWithCorepo {

    @Test
    public void testWork() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        jpa(em -> {
            BeanFactory beanFactory = new BeanFactory(em, dataSource, corepoDataSource, "QUEUE_DEDUPLICATE=true");
            beanFactory.setPresentationObjectBuilder(new PresentationObjectBuilder() {
                @Override
                public void process(String pid) {
                    counter.incrementAndGet();
                }
            });
            Worker worker = beanFactory.getWorker();
            queue("work:1", "work:2",
                  "work:1", "work:2",
                  "work:1", "work:2",
                  "work:1", "work:2",
                  "work:1", "work:2");
            worker.init();
            waitForQueue(10);
            worker.destroy();
        });

        assertThat(counter.get(), is(2));

    }

    @Test
    public void testWorkNoDedup() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        jpa(em -> {
            BeanFactory beanFactory = new BeanFactory(em, dataSource, corepoDataSource, "QUEUE_DEDUPLICATE=false");
            beanFactory.setPresentationObjectBuilder(new PresentationObjectBuilder() {
                @Override
                public void process(String pid) {
                    counter.incrementAndGet();
                }
            });
            Worker worker = beanFactory.getWorker();
            queue("work:1", "work:2",
                  "work:1", "work:2",
                  "work:1", "work:2",
                  "work:1", "work:2",
                  "work:1", "work:2");
            worker.init();
            waitForQueue(10);
            worker.destroy();
        });
        assertThat(counter.get(), is(10));
    }

}
