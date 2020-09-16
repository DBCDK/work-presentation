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
package dk.dbc.search.work.presentation.service.vipcore;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dbc.search.work.presentation.service.Config;
import dk.dbc.vipcore.marshallers.ProfileServiceResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

/**
 * Caching interface to vip-cores profileservice endpoint
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ProfileService {

    private static final ObjectMapper O = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    @Inject
    public Config config;

    /**
     * Fetch SolR-filterquery that represents a profile
     *
     * @param agencyId   Owner of the profile
     * @param profile    Name of the profile
     * @param trackingId For logging in vip-core
     * @return filter query
     * @throws NoSuchProfileException  if a non-existing profile was requested
     * @throws WebApplicationException in case of communication errors with
     *                                 vip-core
     */
    @CacheResult(cacheName = "vip-core",
                 exceptionCacheName = "vip-core-error")
    public String filterQueryFor(@CacheKey String agencyId, @CacheKey String profile, String trackingId) {
        URI uri = config.getVipCore()
                .path("api/profileservice/search/{agencyId}/{profile}")
                .build(agencyId, profile);
        try (InputStream is = config.getVipCoreHttpClient(trackingId)
                .target(uri)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(InputStream.class)) {
            ProfileServiceResponse resp = O.readValue(is, ProfileServiceResponse.class);
            if (resp.getError() != null) {
                switch (resp.getError()) {
                    case AGENCY_NOT_FOUND:
                    case PROFILE_NOT_FOUND:
                        throw new NoSuchProfileException(agencyId, profile);
                    default:
                        throw new BadRequestException("Error: " + resp.getError().value());
                }
            }
            return resp.getFilterQuery();
        } catch (IOException ex) {
            throw new BadRequestException(ex);
        }
    }
}
