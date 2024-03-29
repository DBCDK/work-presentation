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
import dk.dbc.pgqueue.consumer.FatalQueueError;
import dk.dbc.pgqueue.consumer.JobMetaData;
import dk.dbc.pgqueue.consumer.PostponedNonFatalQueueError;
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
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Singleton
@Startup
@Lock(LockType.READ)
@Liveness
public class Worker implements HealthCheck {

    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    @Resource(lookup = "jdbc/corepo")
    DataSource dataSource;

    @Resource(type = ManagedExecutorService.class)
    ExecutorService executor;

    @Inject
    Config config;

    @Inject
    PresentationObjectBuilder presentationObjectBuilder;

    @Inject
    MetricRegistry metrics;

    private QueueWorker worker;

    @PostConstruct
    public void init() {
        log.info("Staring worker");

        worker = QueueWorker.builder(QueueJob.STORAGE_ABSTRACTION)
                .skipDuplicateJobs(config.hasQueueDeduplicate() ? new QueueJob.DeduplicateAbstraction(250) : null, true, true)
                .consume(config.getQueues())
                .dataSource(dataSource)
                .fromEnvWithDefaults()
                .executor(executor)
                .metricRegistryMicroProfile(metrics)
                .build(config.getThreads(), this::processJob);
        worker.start();
    }

    @PreDestroy
    public void destroy() {
        worker.stop();
    }

    @Override
    public HealthCheckResponse call() {
        List<String> hungThreads = worker.hungThreads();
        return HealthCheckResponse.named("queue-worker")
                .status(hungThreads.isEmpty())
                .withData("hung-threads", String.join(", ", hungThreads))
                .build();
    }

    public void processJob(Connection connection, QueueJob job, JobMetaData metaData) throws FatalQueueError, PostponedNonFatalQueueError {
        try {
            presentationObjectBuilder.processJob(connection, job, metaData);
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause();
            FatalQueueError fex = findCauseOfType(cause, FatalQueueError.class);
            if (fex != null) {
                throw fex;
            }
            PersistenceException pex = findCauseOfType(cause, PersistenceException.class);
            if (pex != null) {
                long postpone = config.postponeDuration();
                log.error("PersistenceException, postponing: {}: {}", postpone, pex.getMessage());
                log.debug("PersistenceException, postponing: {}: ", postpone, pex);
                throw new PostponedNonFatalQueueError(postpone, cause);
            }
            DatabaseException dex = findCauseOfType(cause, DatabaseException.class);
            if (dex != null) {
                long postpone = config.postponeDuration();
                log.error("DatabaseException, postponing: {}: {}", postpone, dex.getMessage());
                log.debug("DatabaseException, postponing: {}: ", postpone, dex);
                throw new PostponedNonFatalQueueError(postpone, cause);
            }
            throw ex;
        }
    }

    private static <T extends Exception> T findCauseOfType(Throwable t, Class<T> c) {
        while (t != null) {
            if (c.isAssignableFrom(t.getClass()))
                return (T) t;
            t = t.getCause();
        }
        return null;
    }
}
