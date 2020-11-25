package dk.dbc.search.work.presentation.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;
import java.util.function.Function;

@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelationInformation extends ManifestationInformation {

    private static final long serialVersionUID = 0x644CAC76C6A06754L;

    public String type;

    public static RelationInformation from(String type, ManifestationInformation mani) {
        RelationInformation res = new RelationInformation();
        res.type = type;
        res.manifestationId = mani.manifestationId;
        res.title = mani.title;
        res.fullTitle = mani.fullTitle;
        res.creators = mani.creators;
        res.description = mani.description;
        res.subjects = mani.subjects;
        res.materialTypes = mani.materialTypes;
        return res;
    }

    public static Function<ManifestationInformation, RelationInformation> mapperWith(String type) {
        return mani -> from(type, mani);
    }

    @Override
    public RelationInformation onlyPresentationFields() {
        RelationInformation res = new RelationInformation();
        res.type = type;
        res.manifestationId = this.manifestationId;
        res.materialTypes = this.materialTypes;
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) // Checks class too
            return true;
        RelationInformation that = (RelationInformation) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }

    @Override
    public String toString() {
        String parent = super.toString();
        return "RelationInformation{" +
               "type='" + type + "\', " +
               parent.substring(parent.indexOf('{') + 1) +
               '}';
    }

}
