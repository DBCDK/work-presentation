package dk.dbc.search.work.presentation.api.jpa.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class ManifestationInformation implements Serializable {
    private static final long serialVersionUID = 8070506196810189178L;

    @JsonProperty("pid")
    public String manifestationId;
    @JsonProperty("title")
    public String title;
    @JsonProperty("type")
    public String materialType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManifestationInformation that = (ManifestationInformation) o;
        return Objects.equals(manifestationId, that.manifestationId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(materialType, that.materialType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manifestationId, title, materialType);
    }

    @Override
    public String toString() {
        return "ManifestationInformation{" + "manifestationId: " + manifestationId + ", title: " + title + ", materialType: " + materialType + "}";
    }
}
