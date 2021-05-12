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
import java.util.Arrays;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Thomas Pii (thp@dbc.dk)
 */
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
@Schema(name = GroupInformationResponse.NAME)
public class GroupInformationResponse {

    public static final String NAME = "group";

    // Ugly hack: https://github.com/eclipse/microprofile-open-api/issues/425
    @Schema(name = GroupInformationResponse.Array.NAME, type = SchemaType.ARRAY, ref = GroupInformationResponse.NAME, hidden = true)
    public static class Array {

        public static final String NAME = GroupInformationResponse.NAME + "_list";
    }

    @Schema(example = "0,2,7", implementation = int.class, description = "indexes (starting with 0) in " + WorkInformationResponse.NAME + "/relations")
    public int[] relations;

    @Schema(implementation = ManifestationInformationResponse.Array.class, ref = ManifestationInformationResponse.NAME)
    public Set<ManifestationInformationResponse> records;

    public static GroupInformationResponse with(Set<ManifestationInformationResponse> records) {
        GroupInformationResponse gir = new GroupInformationResponse();
        gir.records = records;
        return gir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupInformationResponse that = (GroupInformationResponse) o;
        return Objects.equals(records, that.records) &&
               Arrays.equals(relations, that.relations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records) * 13 + Arrays.hashCode(relations);
    }

    @Override
    public String toString() {
        return "GroupInformationResponse{" + "records=" + records + ", relations=" + Arrays.toString(relations) + '}';
    }
}
