package dk.dbc.search.work.presentation.api.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
public class ManifestationInformation implements Serializable {
    private static final long serialVersionUID = 8070506196810189178L;

    @JsonProperty("pid")
    public String manifestationId;

    public String title;

    public String fullTitle;

    public List<String> creators;

    public String description;

    @JsonProperty("subjects")
    public List<String> subjects;

    @JsonProperty("types")
    public List<String> materialTypes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManifestationInformation that = (ManifestationInformation) o;
        return Objects.equals(manifestationId, that.manifestationId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(fullTitle, that.fullTitle) &&
                Objects.equals(creators, that.creators) &&
                Objects.equals(description, that.description) &&
                Objects.equals(subjects, that.subjects) &&
                Objects.equals(materialTypes, that.materialTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manifestationId, title, fullTitle, creators, description, subjects, materialTypes);
    }

    @Override
    public String toString() {
        return "ManifestationInformation{" +
               "manifestationId='" + manifestationId + '\'' +
               ", title='" + title + '\'' +
               ", fullTitle='" + fullTitle + '\'' +
               ", creators='" + creators + '\'' +
               ", description='" + description + '\'' +
               ", subjects='" + subjects + '\'' +
               ", type='" + materialTypes + '\'' +
               '}';
    }
}
