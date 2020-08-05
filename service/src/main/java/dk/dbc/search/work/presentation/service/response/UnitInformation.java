package dk.dbc.search.work.presentation.service.response;

import java.io.Serializable;
import java.util.List;

public class UnitInformation implements Serializable {
    private static final long serialVersionUID = 7482723494555578796L;

    public String unitId;
    public List<ManifestationInformation> manifestations;
}
