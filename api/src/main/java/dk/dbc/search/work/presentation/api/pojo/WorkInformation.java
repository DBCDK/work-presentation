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

    public Set<TypedValue> creators;

    public String description;

    public Set<TypedValue> subjects;

    @JsonProperty("dbUnits")
    // Unit -> Manifestations
    public Map<String, Set<ManifestationInformation>> dbUnitInformation;

    @JsonProperty("dbRelUnits")
    // Unit -> Relation-Type -> Manifestations
    public Map<String, Map<String, Set<ManifestationInformation>>> relUnitTypeInformation;

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
               Objects.equals(creators, that.creators) &&
               Objects.equals(description, that.description) &&
               Objects.equals(subjects, that.subjects) &&
               Objects.equals(dbUnitInformation, that.dbUnitInformation) &&
               Objects.equals(relUnitTypeInformation, that.relUnitTypeInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, title, fullTitle, creators, description, subjects, dbUnitInformation, relUnitTypeInformation);
    }

    @Override
    public String toString() {
        return "WorkInformation{" +
               "workId='" + workId + '\'' +
               ", title='" + title + '\'' +
               ", fullTitle='" + fullTitle + '\'' +
               ", creators='" + creators + '\'' +
               ", description='" + description + '\'' +
               ", subjects='" + subjects + '\'' +
               ", dbUnitInformation=" + dbUnitInformation +
               ", relUnitTypeInformation=" + relUnitTypeInformation +
               '}';
    }
}
