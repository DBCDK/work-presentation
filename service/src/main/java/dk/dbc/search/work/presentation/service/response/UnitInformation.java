package dk.dbc.search.work.presentation.service.response;

import java.io.Serializable;
import java.util.List;

public class UnitInformation implements Serializable {
    public String unitId;
    public List<ManifestationInformation> manifestations;
}
