package dk.dbc.search.work.presentation.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkInformation implements Serializable {

    private static final long serialVersionUID = 0x3B56D3505757B617L;

    public String workId;

    public String title;

    public String fullTitle;

    public SeriesInformation series;

    public Set<TypedValue> creators;

    public String description;

    public Set<TypedValue> subjects;

    @JsonProperty("dbUnits")
    // Unit -> Manifestations
    public Map<String, Set<ManifestationInformation>> dbUnitInformation;

    public String ownerUnitId;

    @JsonProperty("dbRelUnits")
    // Unit -> Relations
    public Map<String, Set<RelationInformation>> dbRelUnitInformation;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WorkInformation that = (WorkInformation) o;
        return Objects.equals(workId, that.workId) &&
               Objects.equals(title, that.title) &&
               Objects.equals(fullTitle, that.fullTitle) &&
               Objects.equals(series, that.series) &&
               Objects.equals(creators, that.creators) &&
               Objects.equals(description, that.description) &&
               Objects.equals(subjects, that.subjects) &&
               Objects.equals(dbUnitInformation, that.dbUnitInformation) &&
               Objects.equals(dbRelUnitInformation, that.dbRelUnitInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, title, fullTitle, series, creators, description, subjects, dbUnitInformation, dbRelUnitInformation);
    }

    @Override
    public String toString() {
        return "WorkInformation{" +
               "workId='" + workId + '\'' +
               ", title='" + title + '\'' +
               ", fullTitle='" + fullTitle + '\'' +
               ", series=" + series +
               ", creators='" + creators + '\'' +
               ", description='" + description + '\'' +
               ", subjects='" + subjects + '\'' +
               ", dbUnitInformation=" + dbUnitInformation +
               ", relUnitTypeInformation=" + dbRelUnitInformation +
               '}';
    }
}
