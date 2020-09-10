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

import dk.dbc.search.work.presentation.service.response.WorkPresentationResponse;
import dk.dbc.commons.mdc.GenerateTrackingId;
import dk.dbc.commons.mdc.LogAs;
import dk.dbc.search.work.presentation.api.jpa.RecordEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkContainsEntity;
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import dk.dbc.search.work.presentation.service.response.WorkInformationResponse;
import java.util.Collections;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The endpoint for /api/work-presentation
 * <p>
 * This produces service output of the type {@link WorkInformation}
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
@Path("work-presentation")
@OpenAPIDefinition(
        info = @Info(
                title = "Work Presentation Service",
                version = "1.0",
                description = "This service allows for fetching of entire work structures.",
                contact = @Contact(url = "mailto:dbc@dbc.dk")))
public class WorkPresentationBean {

    private static final Logger log = LoggerFactory.getLogger(WorkPresentationBean.class);

    private static final String WORK_OF = "work-of:";
    private static final int WORK_OF_LEN = WORK_OF.length();

    @PersistenceContext(unitName = "workPresentation_PU")
    public EntityManager em;

    @Inject
    public FilterResult filterResult;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(reusable = true)
    @Operation(
            summary = "Retrieve a work structure",
            description = "This operation produces a work structure, for a given identifier." +
                          " The work structure contains metadata from a selected manifestation," +
                          " and all the manifestations that this work covers." +
                          " These are not ordered/grouped by anything.")
    @APIResponses({
        @APIResponse(name = "Success",
                     responseCode = "200",
                     description = "A work with the given workId has been located",
                     content = @Content(
                             mediaType = MediaType.APPLICATION_JSON,
                             schema = @Schema(ref = WorkPresentationResponse.NAME))),
        @APIResponse(name = "Content Moved",
                     responseCode = "301",
                     description = "If a workId has been updated this will redirect to the correct workId"),
        @APIResponse(name = "Not Found",
                     responseCode = "404",
                     description = "If a workId never existed")
    })
    @Parameters({
        @Parameter(name = "workId",
                   description = "The identifier for the requested work. Typically 'work-of:...'",
                   required = true),
        @Parameter(name = "trackingId",
                   description = "Useful for tracking a request in log files")
    })
    public Response get(@LogAs("workId") @QueryParam("workId") String workId,
                        @LogAs("trackingId") @GenerateTrackingId @QueryParam("trackingId") String trackingId,
                        @Context UriInfo uriInfo) {
        try {
            WorkPresentationResponse resp = new WorkPresentationResponse();
            resp.trackingId = trackingId;
            resp.work = processRequest(workId);
            return Response.ok(resp, MediaType.APPLICATION_JSON)
                    .build();
        } catch (NewWorkIdException ex) {
            log.info("Redirected: {} -> {}", workId, ex.getWorkId());
            UriBuilder ub = UriBuilder.fromUri(uriInfo.getAbsolutePath());
            uriInfo.getQueryParameters().forEach((key, values) -> {
                if ("workId".equals(key))
                    values = Collections.singletonList(ex.getWorkId());
                ub.queryParam(key, values.toArray());
            });
            return Response.status(Response.Status.MOVED_PERMANENTLY)
                    .location(ub.build()).build();
        } catch (NotFoundException ex) {
            log.info("Not found: {}", workId);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Internal exception: {}", ex.getMessage());
            log.debug("Internal exception: ", ex);
            return Response.serverError().build();
        }
    }

    /**
     * Find a work
     *
     * @param workId which identified to get from the database
     * @return WorkInformation for the work
     * @throws NewWorkIdException if the work had become part of another
     * @throws NotFoundException  if the work couldn't be found
     */
    WorkInformationResponse processRequest(String workId) {
        RecordEntity work = RecordEntity.readOnlyFrom(em, workId);
        if (work != null) {
            return filterResult.processWork(work.getContent());
        }
        if (workId.startsWith(WORK_OF)) {
            WorkContainsEntity wc = WorkContainsEntity.readOnlyFrom(em, workId.substring(WORK_OF_LEN));
            if (wc == null)
                throw new NotFoundException();
            work = RecordEntity.readOnlyFromCorepoWorkId(em, wc.getCorepoWorkId());
            if (work == null)
                throw new NotFoundException();
            throw new NewWorkIdException(work.getPersistentWorkId());
        }
        throw new NotFoundException();
    }
}
