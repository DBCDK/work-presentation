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

import dk.dbc.search.work.presentation.api.pojo.GroupInformation;
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
public class GroupInformationResponse implements Comparable<GroupInformationResponse> {

    public static final String NAME = "group";

    // Ugly hack: https://github.com/eclipse/microprofile-open-api/issues/425
    @Schema(name = GroupInformationResponse.Array.NAME, type = SchemaType.ARRAY, ref = GroupInformationResponse.NAME, hidden = true)
    public static class Array {

        public static final String NAME = GroupInformationResponse.NAME + "_list";
    }

    @Schema(example = "true", implementation = boolean.class, description = "Primary for the work")
    public boolean primary;

    @Schema(example = "0,2,7", implementation = int.class, description = "indexes (starting with 0) in " + WorkInformationResponse.NAME + "/relations")
    public int[] relations;

    @Schema(implementation = ManifestationInformationResponse.Array.class)
    public Set<ManifestationInformationResponse> records;

    public static GroupInformationResponse from(GroupInformation gi) {
        GroupInformationResponse gir = new GroupInformationResponse();
        gir.primary = gi.primary;
        // gir.records is computed in FilterResult.processWork()
        return gir;
    }

    @Override
    public int compareTo(GroupInformationResponse o) {
        // TODO compare records?
        return Boolean.compare(o.primary, primary );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupInformationResponse that = (GroupInformationResponse) o;
        return primary == that.primary &&
               Objects.equals(records, that.records) &&
               Arrays.equals(relations, that.relations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records) + Boolean.hashCode(primary);
    }

    @Override
    public String toString() {
        return "GroupInformationResponse{" + "records=" + records + ", primary=" + primary + '}';
    }
}
