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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@ApplicationScoped
@Startup
@Lock(LockType.READ)
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private final Map<String, String> env;
    private String[] queues;
    private int threads;

    public Config() {
        this.env = System.getenv();
    }

    public Config(Map<String, String> env) {
        this.env = env;
        configure();
    }

    @PostConstruct
    public void init() {
        configure();
    }

    private void configure() {
        log.info("Reading/verifying configuration");
        this.queues = Arrays.stream(getOrFail("QUEUES").split("[\\s,]+"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        this.threads = Integer.max(1, Integer.parseInt(getOrDefault("THREADS", "5")));
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public String[] getQueues() {
        return queues;
    }

    public int getThreads() {
        return threads;
    }

    private String getOrFail(String var) {
        String value = env.get(var);
        if (value == null)
            throw new EJBException("Missing required configuration: " + var);
        return value;
    }

    private String getOrDefault(String var, String defaultValue) {
        return env.getOrDefault(var, defaultValue);
    }
}
