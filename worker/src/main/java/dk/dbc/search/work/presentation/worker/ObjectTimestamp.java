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

import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import dk.dbc.search.work.presentation.api.jpa.RecordEntity;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get metadata about objects in records or cache table
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
@Path("object")
@OpenAPIDefinition(
        info = @Info(title = "object info",
                     description = "Get information about the objects uin the database",
                     version = "1.0"))
public class ObjectTimestamp {

    private static final Logger log = LoggerFactory.getLogger(ObjectTimestamp.class);

    @PersistenceContext(unitName = "workPresentation_PU")
    EntityManager em;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{id}")
    @Operation(
            summary = "find timestamp for update of an id",
            description = "Retrieve modification time for an object (record/cache-entry)")
    @Parameter(
            name = "id",
            description = "identifier for an object",
            content = @Content(
                    examples = {
                        @ExampleObject()
                    }),
            required = true)
    @APIResponses({
        @APIResponse(
                name = "record found",
                description = "when a record exists for the given id",
                content = @Content(
                        mediaType = "text/plain",
                        examples = {
                            @ExampleObject(
                                    name = "modification time",
                                    summary = "timestamp",
                                    description = "last time the object was modified according to the database as an ISO instant text",
                                    value = "2020-01-01T12:34:56.789Z"
                            )})),
        @APIResponse(
                name = "not found",
                description = "when no record by that name exists",
                responseCode = "404 NOT FOUND"
        )
    })
    public String getTimestamp(@PathParam("id") String id) {
        log.info("Checking timestamp of: {}", id);
        RecordEntity record = RecordEntity.readOnlyFrom(em, id);
        if (record != null) {
            return record.getModified().toInstant().toString();
        }
        CacheEntity cache = CacheEntity.detachedFrom(em, id);
        if (cache != null) {
            return cache.getModified().toInstant().toString();
        }
        throw new NotFoundException();
    }

}
