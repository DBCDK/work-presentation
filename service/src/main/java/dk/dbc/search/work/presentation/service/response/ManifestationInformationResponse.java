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
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
@Schema(name = ManifestationInformationResponse.NAME)
public class ManifestationInformationResponse implements Comparable<ManifestationInformationResponse> {

    public static final String NAME = "manifestation";

    // Ugly hack: https://github.com/eclipse/microprofile-open-api/issues/425
    @Schema(name = ManifestationInformationResponse.Array.NAME, type = SchemaType.ARRAY, ref = ManifestationInformationResponse.NAME, hidden = true)
    public static class Array {

        public static final String NAME = ManifestationInformationResponse.NAME + "_list";
    }

    @Schema(example = "id-enti:fier")
    public String id;

    @Schema(example = "ether", implementation = String.class)
    public List<String> types;

    // TODO: Move to GroupInformationResponse
//    @Schema(example = "0,2,7", implementation = int.class, description = "indexes (starting with 0) in " + WorkInformationResponse.NAME + "/relations")
//    public int[] relations;

    public static ManifestationInformationResponse from(ManifestationInformation mi) {
        ManifestationInformationResponse mir = new ManifestationInformationResponse();
        mir.id = mi.manifestationId;
        mir.types = mi.materialTypes;
        return mir;
    }

    @Override
    public int compareTo(ManifestationInformationResponse o) {
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ManifestationInformationResponse that = (ManifestationInformationResponse) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(types, that.types);
               //Arrays.equals(relations, that.relations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, types);
    }

    @Override
    public String toString() {
        return "ManifestationInformationResponse{" + "id=" + id + '}';
    }
}
