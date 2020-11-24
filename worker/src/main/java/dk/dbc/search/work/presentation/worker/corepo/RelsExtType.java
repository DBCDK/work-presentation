/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-worker
 *
 * work-presentation-worker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-worker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker.corepo;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Known external relations (from addi-service)
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public enum RelsExtType {

    CONTINUED_IN("continuedIn"),
    CONTINUES("continues"),
    DISCUSSED_IN("discussedIn"),
    DISCUSSES("discusses"),
    HAS_ADAPTATION("hasAdaptation", "adaptation-of"),
    HAS_ANALYSIS("hasAnalysis", "analysis-of"),
    HAS_CREATOR_DESCRIPTION("hasCreatorDescription", "creator"),
    HAS_DESCRIPTION_FROM_PUBLISHER("hasDescriptionFromPublisher", "description"),
    HAS_MANUSCRIPT("hasManuscript", "manuscript"),
    HAS_REVIEW("hasReview", "review"),
    HAS_SOUNDTRACK("hasSoundtrack", "soundtrack"),
    IS_ADAPTATION_OF("isAdaptationOf"),
    IS_ANALYSIS_OF("isAnalysisOf"),
    IS_CREATOR_DESCRIPTION("isCreatorDescription"),
    IS_DESCRIPTION_FROM_PUBLISHER_OF("isDescriptionFromPublisherOf"),
    IS_MANUSCRIPT_OF("isManuscriptOf"),
    IS_REVIEW_OF("isReviewOf"),
    IS_SOUNDTRACK_OF_GAME("isSoundtrackOfGame"),
    IS_SOUNDTRACK_OF_MOVIE("isSoundtrackOfMovie");

    private static final Map<String, RelsExtType> LOOKUP = EnumSet.allOf(RelsExtType.class)
            .stream()
            .collect(Collectors.toMap(e -> e.text, e -> e));
    private final String text;
    private final String name;
    private final boolean presentable;

    RelsExtType(String text, String presentationName) {
        this.text = text;
        this.name = presentationName;
        this.presentable = true;
    }

    RelsExtType(String text) {
        this.text = text;
        this.name = text;
        this.presentable = true;
    }

    public String getName() {
        return name;
    }

    public static RelsExtType from(String text) {
        return LOOKUP.get(text);
    }

    public boolean isPresentable() {
        return presentable;
    }
}
