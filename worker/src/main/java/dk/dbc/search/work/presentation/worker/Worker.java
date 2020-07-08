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

import dk.dbc.corepo.queue.QueueJob;
import dk.dbc.log.LogWith;
import dk.dbc.pgqueue.consumer.JobMetaData;
import dk.dbc.pgqueue.consumer.QueueWorker;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class Worker {

    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    @Resource(lookup = "jdbc/workpresentation")
    DataSource dataSource;

    @Resource(type = ManagedExecutorService.class)
    ExecutorService executor;

    @Inject
    Config config;

    @Inject
    Builder builder;

    @Inject
    MetricRegistry metrics;

    private QueueWorker worker;

    @PostConstruct
    public void init() {
        log.info("Staring worker");

        worker = QueueWorker.builder(QueueJob.STORAGE_ABSTRACTION)
                .skipDuplicateJobs(QueueJob.DEDUPLICATE_ABSTRACTION)
                .consume(config.getQueues())
                .dataSource(dataSource)
                .fromEnvWithDefaults()
                .executor(executor)
                .metricRegistryMicroProfile(metrics)
                .build(config.getThreads(), this::work);
        worker.start();
    }

    @PreDestroy
    public void destroy() {
        worker.stop();
    }

    public List<String> hungThreads() {
        return worker.hungThreads();
    }

    public void work(Connection connection, QueueJob job, JobMetaData metaData) {
        try (LogWith logWith = LogWith.track(job.getTrackingId())
                .pid(job.getPid());) {

            builder.process(job.getPid());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
