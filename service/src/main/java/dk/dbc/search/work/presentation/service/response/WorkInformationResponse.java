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

import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Class wrapping response - This cannot be in the api module, since the @Schema
 * annotations cannot be found by OpenAPI.
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
@Schema(hidden = true, name = WorkInformationResponse.NAME)
public class WorkInformationResponse {

    public static final String NAME = "work";

    @Schema(example = "work-of:some-manifestation:id")
    public String workId;

    @Schema(example = "Necronomicon")
    public String title;

    @Schema(example = "Necronomicon: Book of the Dead")
    public String fullTitle;

    @Schema(example = "H. P. Lovecraft", implementation = String.class)
    public List<String> creators;

    @Schema(example = "Fictional book")
    public String description;

    @Schema(example = "grimoire", implementation = String.class)
    public Set<String> subjects;

    @Schema(implementation = ManifestationInformationResponse.Array.class)
    public Set<ManifestationInformationResponse> records;

    public static WorkInformationResponse from(WorkInformation wi) {
        WorkInformationResponse wir = new WorkInformationResponse();
        wir.workId = wi.workId;
        wir.title = wi.title;
        wir.fullTitle = wi.fullTitle;
        wir.creators = wi.creators;
        wir.description = wi.description;
        wir.subjects = wi.subjects;
        // wir.records is computed in FilterResult.processWork()
        return wir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WorkInformationResponse that = (WorkInformationResponse) o;
        return Objects.equals(workId, that.workId) &&
               Objects.equals(title, that.title) &&
               Objects.equals(fullTitle, that.fullTitle) &&
               Objects.equals(creators, that.creators) &&
               Objects.equals(description, that.description) &&
               Objects.equals(subjects, that.subjects) &&
               Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, title, fullTitle, creators, description, subjects, records);
    }

    @Override
    public String toString() {
        return "WorkInformationResponse{" + "workId=" + workId + '}';
    }
}