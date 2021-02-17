package dk.dbc.search.work.presentation.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.Objects;

@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupInformation implements Serializable {

    private static final long serialVersionUID = 0x644CAC76C6A06754L; // TODO

    public String groupId;

    public boolean primary;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupInformation that = (GroupInformation) o;
        return Objects.equals(groupId, that.groupId );
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId) + Boolean.hashCode(primary);
    }

    @Override
    public String toString() {
        return "GroupInformation{" +
               "groupId='" + groupId + '\'' +
               ", primary='" + primary + '\'' +
               '}';
    }
}
