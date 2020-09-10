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

import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
@Schema(name = ManifestationInformationResponse.NAME)
public class ManifestationInformationResponse {

    public static final String NAME = "manifestation";

    // Ugly hack: https://github.com/eclipse/microprofile-open-api/issues/425
    @Schema(name = ManifestationInformationResponse.Array.NAME, type = SchemaType.ARRAY, ref = ManifestationInformationResponse.NAME, hidden = true)
    public static class Array {

        public static final String NAME = ManifestationInformationResponse.NAME + "_wrapper";
    }

    @Schema(example = "id-enti:fier")
    public String id;

    @Schema(example = "Necronomicon")
    public String title;

    @Schema(example = "Necronomicon: Book of the Dead")
    public String fullTitle;

    @Schema(example = "H. P. Lovecraft", implementation = String.class)
    public List<String> creators;

    @Schema(example = "Fictional book")
    public String description;

    @Schema(example = "grimoire", implementation = String.class)
    public List<String> subjects;

    @Schema(example = "ether", implementation = String.class)
    public List<String> types;

    public static ManifestationInformationResponse from(ManifestationInformation mi) {
        ManifestationInformationResponse mir = new ManifestationInformationResponse();
        mir.id = mi.manifestationId;
        mir.title = mi.title;
        mir.fullTitle = mi.fullTitle;
        mir.creators = mi.creators;
        mir.description = mi.description;
        mir.subjects = mi.subjects;
        mir.types = mi.materialTypes;
        return mir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ManifestationInformationResponse that = (ManifestationInformationResponse) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(title, that.title) &&
               Objects.equals(fullTitle, that.fullTitle) &&
               Objects.equals(creators, that.creators) &&
               Objects.equals(description, that.description) &&
               Objects.equals(subjects, that.subjects) &&
               Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, fullTitle, creators, description, subjects, types);
    }

}
