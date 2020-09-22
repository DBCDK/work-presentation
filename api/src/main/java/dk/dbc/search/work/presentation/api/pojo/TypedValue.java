/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
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
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * A value with a declared type
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypedValue implements Serializable {

    private static final long serialVersionUID = 0x138510F9E2C42638L;

    public String type;
    public String value;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TypedValue that = (TypedValue) o;
        return Objects.equals(type, that.type) &&
               Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "TypedValue{" +
               "type='" + type + '\'' +
               ", value='" + value + '\'' +
               '}';
    }

    /**
     * Construct a distinct set of typed values from a collection
     * <p>
     * The set is case insensitive, prioritizing words with capitalization over
     * lowercase.
     *
     * @param typedValues collection of values with duplicates
     * @return collection of values without duplicates
     */
    public static Set<TypedValue> distinctSet(Collection<TypedValue> typedValues) {
        HashMap<String, HashMap<String, String>> completeCollection = new HashMap<>();
        typedValues.forEach(typedValue -> {
            String safeType = typedValue.type == null ? "" : typedValue.type;
            HashMap<String, String> values = completeCollection.computeIfAbsent(safeType, s -> new HashMap<>());
            String normalized = Normalizer.normalize(typedValue.value, Normalizer.Form.NFC);
            String key = normalized.toLowerCase(Locale.ROOT);
            values.compute(key, (k, v) ->
                           v != null && // a value exists and
                           !v.equals(k) ? // It is not lowercase (as the key is)
                           v : // use existing value
                           normalized);
        });
        HashSet<TypedValue> ret = new HashSet<>();
        completeCollection.forEach((type, valueMap) -> {
            valueMap.values().forEach(value -> {
                TypedValue typedValue = with(type, value);
                ret.add(typedValue);
            });

        });

        return ret;
    }

    static TypedValue with(String type, String value) {
        TypedValue typedValue = new TypedValue();
        typedValue.type = type.isEmpty() ? "" : type;
        typedValue.value = value;
        return typedValue;
    }
}
