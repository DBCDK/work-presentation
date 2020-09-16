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
    private Client httpClient;
    private UriBuilder vipCore;

    public Config() {
        this(System.getenv());
    }

    public Config(Map<String, String> env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        log.info("Reading/verifying configuration");
        String userAgent = getOrDefault("USER_AGENT", "WorkPresentationService/1.0");
        log.debug("Using: {} as HttpUserAgent", userAgent);
        this.httpClient = clientBuilder()
                .register((ClientRequestFilter) (ClientRequestContext context) ->
                        context.getHeaders().putSingle("User-Agent", userAgent)
                )
                .build();
        this.vipCore = UriBuilder.fromPath(getOrFail("VIP_CORE_URL"));
    }

    public Client getHttpClient() {
        return httpClient;
    }

    public Client getVipCoreHttpClient(String trackingId) {
        return getHttpClient()
                .register((ClientRequestFilter) (ClientRequestContext context) ->
                        context.getHeaders().putSingle("X-DBCTrackingId", trackingId));
    }

    public UriBuilder getVipCore() {
        return vipCore.clone();
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
