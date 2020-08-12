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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;
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

    private UriBuilder corepoContentService;
    private Client httpClient;
    private String[] queues;
    private boolean queueDeduplicate;
    private int threads;

    public Config() {
        this(System.getenv());
    }

    public Config(Map<String, String> env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        log.info("Reading/verifying configuration");
        this.corepoContentService = UriBuilder.fromPath(getOrFail("COREPO_CONTENT_SERVICE_URL"));
        this.queues = Arrays.stream(getOrFail("QUEUES").split("[\\s,]+"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        this.queueDeduplicate = Boolean.parseBoolean(getOrDefault("QUEUE_DEDUPLICATE", "true"));
        this.threads = Integer.max(1, Integer.parseInt(getOrDefault("THREADS", "5")));
        String userAgent = getOrDefault("USER_AGENT", "WorkPresentationWorker/1.0");
        log.debug("Using: {} as HttpUserAgent", userAgent);
        this.httpClient = clientBuilder()
                .register((ClientRequestFilter) (ClientRequestContext context) ->
                        context.getHeaders().putSingle("User-Agent", userAgent)
                )
                .build();
        this.corepoContentService = UriBuilder.fromPath(getOrFail("COREPO_CONTENT_SERVICE_URL"));
    }

    public UriBuilder getCorepoContentService() {
        return corepoContentService.clone();
    }

    public Client getHttpClient() {
        return httpClient;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public String[] getQueues() {
        return queues;
    }

    public boolean hasQueueDeduplicate() {
        return queueDeduplicate;
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

    /**
     * Create a clientBuilder (useful for unittesting with specific
     * implementation)
     *
     * @return Http client builder
     */
    protected ClientBuilder clientBuilder() {
        return ClientBuilder.newBuilder();
    }
}
