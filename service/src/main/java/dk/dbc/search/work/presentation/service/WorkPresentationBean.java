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

import dk.dbc.commons.mdc.GenerateTrackingId;
import dk.dbc.commons.mdc.LogAs;
import dk.dbc.search.work.presentation.api.jpa.WorkContainsEntity;
import dk.dbc.search.work.presentation.api.jpa.WorkObjectEntity;
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import dk.dbc.search.work.presentation.service.response.ErrorCode;
import dk.dbc.search.work.presentation.service.response.ErrorResponse;
import dk.dbc.search.work.presentation.service.response.WorkInformationResponse;
import dk.dbc.search.work.presentation.service.response.WorkPresentationResponse;
import dk.dbc.search.work.presentation.service.vipcore.NoSuchProfileException;
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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

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
    @Path("getPersistentWorkId")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(
            summary = "Retrieve a persistent work id",
            description = "This operation produces a persistent work-id, given a " +
                    "corepo work-id."
    )
    @APIResponses({
            //        https://github.com/payara/Payara/issues/4955
            @APIResponse(name = "Success",
                    responseCode = "200",
                    description = "A persistent work id",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = String.class)
                    )),
            @APIResponse(name = "Bad Request",
                    responseCode = "400",
                    description = "If a request has parameters that are somehow invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(ref = ErrorResponse.NAME)
                    )),
            @APIResponse(name = "Not Found",
                    responseCode = "404",
                    description = "If a corepo work id does not exist",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(ref = ErrorResponse.NAME)
                    ))
    })
    @Parameters({
            @Parameter(name = "corepoWorkId",
                    description = "Id of the work in corepo. Typically 'work:...'",
                    required = true),
            @Parameter(name = "trackingId",
                    description = "Useful for tracking a request in log files")
    })
    public Response getPersistentWorkId(
            @LogAs("corepoWorkId") @QueryParam("corepoWorkId") String corepoWorkId,
            @LogAs("trackingId") @GenerateTrackingId @QueryParam("trackingId") String trackingId) {
        if (corepoWorkId == null || corepoWorkId.isEmpty()) {
            final String missingVariables = "Required parameter corepoWorkId is missing";
            log.info("Bad request: {}", missingVariables);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ErrorCode.MISSING_PARAMETERS, missingVariables, trackingId))
                    .build();
        }
        final WorkObjectEntity work = WorkObjectEntity.readOnlyFromCorepoWorkId(em, corepoWorkId);
        if (work == null) {
            log.warn("corepo work-id not found: {}", corepoWorkId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(ErrorCode.NOT_FOUND_ERROR, String.format("corepo work-id not found {}", corepoWorkId), trackingId))
                    .build();
        }
        return Response.status(Response.Status.OK).entity(work.getPersistentWorkId()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Operation(
            summary = "Retrieve a work structure",
            description = "This operation produces a work structure, for a given identifier." +
                          " The work structure contains metadata from a selected manifestation," +
                          " and all the manifestations that this work covers." +
                          " These are not ordered/grouped by anything.")
    @APIResponses({
        //        https://github.com/payara/Payara/issues/4955
        @APIResponse(name = "Success",
                     responseCode = "200",
                     description = "A work with the given workId has been located",
                     content = @Content(
                             mediaType = MediaType.APPLICATION_JSON,
                             schema = @Schema(ref = WorkPresentationResponse.NAME))),
        @APIResponse(name = "Bad Request",
                     responseCode = "400",
                     description = "If a request has parameters that are somehow invalid",
                     content = @Content(
                             mediaType = MediaType.APPLICATION_JSON,
                             schema = @Schema(ref = ErrorResponse.NAME)
                     )),
        @APIResponse(name = "Content Moved",
                     responseCode = "301",
                     description = "If a workId has been updated this will redirect to the correct workId",
                     content = @Content(
                             mediaType = MediaType.APPLICATION_JSON,
                             schema = @Schema(ref = ErrorResponse.NAME)
                     )),
        @APIResponse(name = "Forbidden",
                     responseCode = "403",
                     description = "Your profile doesn't have access to this workId",
                     content = @Content(
                             mediaType = MediaType.APPLICATION_JSON,
                             schema = @Schema(ref = ErrorResponse.NAME)
                     )),
        @APIResponse(name = "Not Found",
                     responseCode = "404",
                     description = "If a workId never existed",
                     content = @Content(
                             mediaType = MediaType.APPLICATION_JSON,
                             schema = @Schema(ref = ErrorResponse.NAME)
                     ))
    })
    @Parameters({
        @Parameter(name = "workId",
                   description = "The identifier for the requested work. Typically 'work-of:...'",
                   required = true),
        @Parameter(name = "agencyId",
                   description = "The agency that requested work. 6 digits",
                   required = true),
        @Parameter(name = "profile",
                   description = "The name of the search profile the agency uses",
                   required = true),
        @Parameter(name = "includeRelations",
                   description = "Include references to related records (this will slow down the request)"),
        @Parameter(name = "trackingId",
                   description = "Useful for tracking a request in log files")
    })
    public Response get(@LogAs("workId") @QueryParam("workId") String workId,
                        @QueryParam("agencyId") String agencyId,
                        @QueryParam("profile") String profile,
                        @QueryParam("includeRelations") @DefaultValue("false") boolean includeRelations,
                        @LogAs("trackingId") @GenerateTrackingId @QueryParam("trackingId") String trackingId,
                        @Context UriInfo uriInfo) {
        LinkedList<String> missing = new LinkedList<>();
        if (workId == null || workId.isEmpty())
            missing.add("workId");
        if (agencyId == null || agencyId.isEmpty())
            missing.add("agencyId");
        if (profile == null || profile.isEmpty())
            missing.add("profile");

        if (!missing.isEmpty()) {
            String variables = missing.removeLast();
            if (missing.isEmpty()) {
                variables = String.format(Locale.ROOT, "Required parameter %s is missing", variables);
            } else {
                variables = String.format(Locale.ROOT, "Required parameters %s and %s are missing", String.join(", ", missing), variables);
            }
            log.info("Bad request: {}", variables);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(ErrorCode.MISSING_PARAMETERS, variables, trackingId))
                    .build();
        }
        try {
            WorkPresentationResponse resp = new WorkPresentationResponse();
            resp.trackingId = trackingId;
            resp.work = ExceptionSafe.wrap(() -> processRequest(workId, agencyId, profile, includeRelations, trackingId))
                    .raise(NewWorkIdException.class)
                    .raise(NotFoundException.class)
                    .raise(NoSuchProfileException.class)
                    .raise(WebApplicationException.class)
                    .get();
            log.info("WorkId: {} for: {}/{} {} relations", workId, agencyId, profile, includeRelations ? "with" : "without");
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
        } catch (ForbiddenException ex) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(ErrorCode.PROFILE_ERROR, ex.getMessage(), trackingId))
                    .build();
        } catch (NotFoundException ex) {
            log.info("Not found: {}", workId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(ErrorCode.NOT_FOUND_ERROR, ex.getMessage(), trackingId))
                    .build();
        } catch (NoSuchProfileException ex) {
            log.warn("Profile not found: {}", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ErrorCode.PROFILE_ERROR, ex.getMessage(), trackingId))
                    .build();
        } catch (RuntimeException ex) {
            log.error("Internal exception: {}", ex.getMessage());
            log.debug("Internal exception: ", ex);
            return Response.serverError().build();
        }
    }

    /**
     * Find a work and filter it
     *
     * @param workId           Which identified to get from the database
     * @param agencyId         The 1st part of the filter specification
     * @param profile          The 2nd part of the filter specification
     * @param includeRelations If relations should be included in the answer
     * @param trackingId       The tracking id for the request
     * @return WorkInformation for the work
     * @throws NewWorkIdException if the work had become part of another
     * @throws NotFoundException  if the work couldn't be found
     */
    WorkInformationResponse processRequest(String workId, String agencyId, String profile, boolean includeRelations, String trackingId) {
        WorkObjectEntity work = WorkObjectEntity.readOnlyFrom(em, workId);
        if (work != null) {
            return filterResult.processWork(work.getCorepoWorkId(), work.getContent(), agencyId, profile, includeRelations, trackingId);
        }
        if (workId.startsWith(WORK_OF)) {
            WorkContainsEntity wc = WorkContainsEntity.readOnlyFrom(em, workId.substring(WORK_OF_LEN));
            if (wc == null)
                throw new NotFoundException("No Such Work");
            work = WorkObjectEntity.readOnlyFromCorepoWorkId(em, wc.getCorepoWorkId());
            if (work == null)
                throw new NotFoundException();
            throw new NewWorkIdException(work.getPersistentWorkId());
        }
        throw new NotFoundException("No Such Work");
    }
}
