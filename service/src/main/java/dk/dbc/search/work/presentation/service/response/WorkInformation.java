package dk.dbc.search.work.presentation.service.response;

import java.io.Serializable;
import java.util.List;

public class WorkInformation implements Serializable {
    public String workId;
    public String title;
    public String creator;
    public String description;
    public String subject;
    public List<UnitInformation> dbUnitInformationList;
    public List<ManifestationInformation> manifestationInformationList;
}
