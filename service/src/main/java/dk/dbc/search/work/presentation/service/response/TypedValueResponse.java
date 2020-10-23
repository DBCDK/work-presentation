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

import dk.dbc.search.work.presentation.api.pojo.TypedValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
@Schema(name = TypedValueResponse.NAME)
public class TypedValueResponse {

    public static final String NAME = "typedvalue";

    // Ugly hack: https://github.com/eclipse/microprofile-open-api/issues/425
    @Schema(name = TypedValueResponse.Array.NAME, type = SchemaType.ARRAY, ref = TypedValueResponse.NAME, hidden = true)
    public static class Array {

        public static final String NAME = TypedValueResponse.NAME + "_list";
    }

    // SAHU: added required = true
    @Schema(example = "classifier", required = true)
    public String type;

    @Schema(example = "Value", required = true)
    public String value;

    public static TypedValueResponse from(TypedValue tv) {
        TypedValueResponse ret = new TypedValueResponse();
        ret.type = tv.type;
        ret.value = tv.value;
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TypedValueResponse that = (TypedValueResponse) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "TypedValueResponse{" +
               "type=" + type +
               ", value=" + value +
               '}';
    }
}
