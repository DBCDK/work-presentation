package dk.dbc.search.work.presentation.service.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkInformation implements Serializable {

    private static final long serialVersionUID = -5483372029181835470L;

    public String workId;
    public String title;
    public String creator;
    public String description;
    public String subject;
    public List<UnitInformation> dbUnitInformationList;
    public List<ManifestationInformation> manifestationInformationList;

    private static final ObjectMapper O = new ObjectMapper();

    public Map<String, String> toPropMap() throws JsonProcessingException {
        Map<String, String> res = new HashMap<>();
        res.put("workId", workId);
        res.put("title", title);
        res.put("creator", creator);
        res.put("description", description);
        res.put("subject", subject);
        res.put("dbUnitInformationList", O.writeValueAsString(dbUnitInformationList));
        res.put("manifestations", O.writeValueAsString(manifestationInformationList));
        return res;
    }

}
