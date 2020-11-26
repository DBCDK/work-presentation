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

import dk.dbc.search.work.presentation.api.pojo.RelationInformation;
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
@Schema(name = RelationInformationResponse.NAME)
public class RelationInformationResponse implements Comparable<RelationInformationResponse> {

    public static final String NAME = "relation";

    // Ugly hack: https://github.com/eclipse/microprofile-open-api/issues/425
    @Schema(name = RelationInformationResponse.Array.NAME, type = SchemaType.ARRAY, ref = RelationInformationResponse.NAME, hidden = true)
    public static class Array {

        public static final String NAME = RelationInformationResponse.NAME + "_list";
    }

    @Schema(example = "review")
    public String type;

    @Schema(example = "id-enti:fier")
    public String id;

    @Schema(example = "ether", implementation = String.class)
    public List<String> types;

    public static RelationInformationResponse from(RelationInformation ri) {
        RelationInformationResponse rir = new RelationInformationResponse();
        rir.type = ri.type;
        rir.id = ri.manifestationId;
        rir.types = ri.materialTypes;
        return rir;
    }

    @Override
    public int compareTo(RelationInformationResponse o) {
        int ret = this.id.compareTo(o.id);
        if (ret == 0)
            ret = this.type.compareTo(o.type);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RelationInformationResponse that = (RelationInformationResponse) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(id, that.id) &&
               Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return "RelationInformationResponse{" + "type=" + type + ", id=" + id + ", types=" + types + '}';
    }
}
