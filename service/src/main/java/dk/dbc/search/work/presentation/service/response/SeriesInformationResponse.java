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

import dk.dbc.search.work.presentation.api.pojo.SeriesInformation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
@Schema(description = "If the work is part of a series, this information is included",
        name = SeriesInformationResponse.NAME)
public class SeriesInformationResponse {

    public static final String NAME = "seriesinformation";

    @Schema(example = "Den store djævlekrig", required = true)
    public String title;

    @Schema(example = "1", required = false)
    public String instalment;

    public static SeriesInformationResponse from(SeriesInformation sr) {
        SeriesInformationResponse ret = new SeriesInformationResponse();
        ret.title = sr.title;
        ret.instalment = sr.instalment;
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SeriesInformationResponse that = (SeriesInformationResponse) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(instalment, that.instalment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, instalment);
    }

    @Override
    public String toString() {
        return "SeriesInformationResponse{" +
               "title=" + title +
               ", instalment=" + instalment +
               '}';
    }
}
