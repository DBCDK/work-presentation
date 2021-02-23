/*
 * Copyright (C) 2021 DBC A/S (http://dbc.dk/)
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
package dk.dbc.search.work.presentation.service;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that implements a comparator for strings, that sorts in numeric order ie 9 before 12.
 *
 * this uses .xxx as decimal numbers not version numbers.
 *
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class NaturalSort implements Comparator<String>, Serializable {

    private static final long serialVersionUID = 0x5EF4DBC337BA3502L;

    private final HashMap<String, String> sortKeys;
    private static final Pattern REGEX = Pattern.compile("0*(0|[1-9]\\d*)((?:\\.\\d+)*)");

    public NaturalSort() {
        this.sortKeys = new HashMap<>();
    }

    @Override
    public int compare(String o1, String o2) {
        return sortKey(o1).compareTo(sortKey(o2));
    }

    private String sortKey(String key) {
        return sortKeys.computeIfAbsent(key, NaturalSort::sortKeyBuilder);
    }

    private static String sortKeyBuilder(String key) {
        StringBuilder alphaNumKey = new StringBuilder();
        Matcher matcher = REGEX.matcher(key);
        int end = 0;
        while (matcher.find()) {
            if (end != matcher.start()) {
                alphaNumKey.append(key.substring(end, matcher.start()));
            }
            String number = matcher.group(1);
            alphaNumKey.append('0')
                    .append((char)('@' + number.length()))
                    .append(number)
                    .append(matcher.group(2));
            end = matcher.end();
        }
        if (end != key.length()) {
            alphaNumKey.append(key.substring(end));
        }
        return alphaNumKey.toString();
    }
}
