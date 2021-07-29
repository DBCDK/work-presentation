package dk.dbc.search.work.presentation.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManifestationInformation implements Serializable {

    private static final long serialVersionUID = 0x644CAC76C6A06754L;

    @JsonProperty("pid")
    public String manifestationId;

    public String title;

    public String fullTitle;

    // Optional NULL is not at series
    public SeriesInformation series;

    public List<TypedValue> creators;

    public String description;

    public List<TypedValue> subjects;

    @JsonProperty("types")
    public List<String> materialTypes;

    public List<String> workTypes;

    @JsonProperty()
    public Map<String, String> priorityKeys;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ManifestationInformation that = (ManifestationInformation) o;
        return Objects.equals(manifestationId, that.manifestationId) &&
               Objects.equals(title, that.title) &&
               Objects.equals(fullTitle, that.fullTitle) &&
               Objects.equals(series, that.series) &&
               Objects.equals(creators, that.creators) &&
               Objects.equals(description, that.description) &&
               Objects.equals(subjects, that.subjects) &&
               Objects.equals(materialTypes, that.materialTypes) &&
               Objects.equals(workTypes, that.workTypes) &&
               Objects.equals(priorityKeys, that.priorityKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manifestationId, title, fullTitle, series, creators, description, subjects, materialTypes, workTypes, priorityKeys);
    }

    @Override
    public String toString() {
        return "ManifestationInformation{" +
               "manifestationId='" + manifestationId + '\'' +
               ", title='" + title + '\'' +
               ", fullTitle='" + fullTitle + '\'' +
               ", series=" + series +
               ", creators=" + creators +
               ", description='" + description + '\'' +
               ", subjects=" + subjects +
               ", types=" + materialTypes +
               ", workTypes=" + workTypes +
               ", priorityKeys=" + priorityKeys +
               '}';
    }

    public ManifestationInformation onlyPresentationFields() {
        ManifestationInformation res = new ManifestationInformation();
        res.manifestationId = this.manifestationId;
        res.materialTypes = this.materialTypes;
        res.workTypes = this.workTypes;
        return res;
    }
}
