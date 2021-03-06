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
package dk.dbc.search.work.presentation.service.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data structure for returning a response to the user
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
@Schema(name = WorkPresentationResponse.NAME)
public class WorkPresentationResponse {

    public static final String NAME = "answer";

    @Schema(required = true, ref = WorkInformationResponse.NAME)
    public WorkInformationResponse work;

    @Schema(example = "some-uuid")
    public String trackingId;

    @Override
    public String toString() {
        return "WorkPresentationResponse{" + work + '}';
    }
}
