/*
 * Copyright (C) 2021 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-api
 *
 * work-presentation-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeriesInformation implements Serializable {

    public String title;

    public String instalment;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SeriesInformation that = (SeriesInformation) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(instalment, that.instalment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, instalment);
    }

    @Override
    public String toString() {
        return "SeriesInformation{" +
               "title='" + title + '\'' +
               ", instalment='" + instalment + '\'' +
               '}';
    }
}
