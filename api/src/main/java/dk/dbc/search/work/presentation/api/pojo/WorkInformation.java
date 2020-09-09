package dk.dbc.search.work.presentation.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkInformation implements Serializable {

    private static final long serialVersionUID = -5483372029181835470L;

    public String workId;

    public String title;

    public String fullTitle;

    public List<String> creators;

    public String description;

    public Set<String> subjects;

    @JsonProperty("dbUnits")
    public Map<String, Set<ManifestationInformation>> dbUnitInformation;

    public WorkInformation() {
    }

    // used for integration test
    public WorkInformation(String workId) {
        this.workId = workId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkInformation that = (WorkInformation) o;
        return Objects.equals(workId, that.workId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(fullTitle, that.fullTitle) &&
                Objects.equals(creators, that.creators) &&
                Objects.equals(description, that.description) &&
                Objects.equals(subjects, that.subjects) &&
                Objects.equals(dbUnitInformation, that.dbUnitInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, title, fullTitle, creators, description, subjects, dbUnitInformation);
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
                '}';
    }
}
