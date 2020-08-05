package dk.dbc.search.work.presentation.service.response;

import java.io.Serializable;
import java.util.List;

public class WorkPresentationResponse implements Serializable {
    public String trackingId;
    public String workId;
    public String title;
    public String creator;
    public String description;
    public String subject;
    public List<ManifestationInformation> manifestations;

    public WorkPresentationResponse(String trackingId, WorkInformation workInformation) {
        this.trackingId = trackingId;
        this.manifestations = workInformation.manifestationInformationList;
        this.workId = workInformation.workId;
        this.title = workInformation.title;
        this.creator = workInformation.creator;
        this.description = workInformation.description;
        this.subject = workInformation.subject;
    }
}
