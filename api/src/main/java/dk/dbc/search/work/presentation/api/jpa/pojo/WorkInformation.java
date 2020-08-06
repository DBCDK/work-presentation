package dk.dbc.search.work.presentation.api.jpa.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WorkInformation implements Serializable {

    private static final long serialVersionUID = -5483372029181835470L;

    public String workId;

    public String title;

    public String creator;

    public String description;

    public String subject;

    @JsonProperty("dbUnits")
    public Map<String, List<ManifestationInformation>> dbUnitInformation;

    @JsonProperty("records")
    public List<ManifestationInformation> manifestationInformationList;

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
                Objects.equals(creator, that.creator) &&
                Objects.equals(description, that.description) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(dbUnitInformation, that.dbUnitInformation) &&
                Objects.equals(manifestationInformationList, that.manifestationInformationList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, title, creator, description, subject, dbUnitInformation, manifestationInformationList);
    }

    @Override
    public String toString() {
        return "WorkInformation{" +
                "workId='" + workId + '\'' +
                ", title='" + title + '\'' +
                ", creator='" + creator + '\'' +
                ", description='" + description + '\'' +
                ", subject='" + subject + '\'' +
                ", dbUnitInformation=" + dbUnitInformation +
                ", manifestationInformationList=" + manifestationInformationList +
                '}';
    }

    /**
     * We do not want to expose the unit information in the service, so this method should
     * be called before sending WorkInformation objects as responses in a service.
     * @param workInformation the work information we wish to return
     * @return the input object, but with the unit information set to null.
     */
    public static WorkInformation prepareResponse(WorkInformation workInformation) {
        workInformation.dbUnitInformation = null;
        return workInformation;
    }
}
