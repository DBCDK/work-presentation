/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-api
 *
 * work-presentation-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.api.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import org.postgresql.util.PGobject;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Entity
@Table(name = "cache")
public class CacheEntity implements Serializable {

    private static final long serialVersionUID = 0x8b861b42b964b770L;

    @Version
    int version;

    @Id
    @Column(updatable = false, nullable = false)
    private String manifestationId;

    @Column(nullable = false)
    private Timestamp modified;

    @Column(nullable = false)
    @Convert(converter = CacheEntity.JsonConverter.class)
    private ManifestationInformation content;

    @Transient
    transient boolean persist;

    @Transient
    transient EntityManager em;

    public static CacheEntity from(EntityManager em, String manifestationId) {
        CacheEntity entity = em.find(CacheEntity.class, manifestationId, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        if (entity == null) {
            entity = new CacheEntity(manifestationId);
        }
        entity.em = em;
        return entity;
    }

    protected CacheEntity() {
        this.persist = false;
    }

    private CacheEntity(String manifestationId) {
        this.manifestationId = manifestationId;
        this.content = null;
        this.persist = true;
    }

    public String getManifestationId() {
        return manifestationId;
    }

    public void setManifestationId(String manifestationId) {
        this.manifestationId = manifestationId;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Timestamp getModified() {
        return modified;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setModified(Timestamp modified) {
        this.modified = modified;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ManifestationInformation getContent() {
        return content;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setContent(ManifestationInformation content) {
        this.content = content;
    }

    public void save() {
        if (persist) {
            em.persist(this);
        } else {
            em.refresh(this);
        }
        persist = false;
    }

    public void delete() {
        if (!persist) {
            em.remove(this);
        }
        persist = true;
    }

    public void detach() {
        em.detach(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.manifestationId);
        hash = 47 * hash + Objects.hashCode(this.modified);
        hash = 47 * hash + Objects.hashCode(this.content);
        hash = 47 * hash + this.version;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final CacheEntity other = (CacheEntity) obj;
        return this.version == other.version &&
               Objects.equals(this.manifestationId, other.manifestationId) &&
               Objects.equals(this.content, other.content) &&
               Objects.equals(this.modified, other.modified);
    }

    @Override
    public String toString() {
        return "CacheEntity{" + "version=" + version + ", manifestationId=" + manifestationId + ", modified=" + modified + '}';
    }

    @Converter
    public static class JsonConverter implements AttributeConverter<ManifestationInformation, PGobject> {

        private static final ObjectMapper O = new ObjectMapper();

        @Override
        public PGobject convertToDatabaseColumn(ManifestationInformation content) throws IllegalStateException {
            try {
                final PGobject pgObject = new PGobject();
                pgObject.setType("jsonb");
                if (content == null) {
                    pgObject.setValue(null);
                } else {
                    pgObject.setValue(O.writeValueAsString(content));
                }
                return pgObject;
            } catch (SQLException | JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public ManifestationInformation convertToEntityAttribute(PGobject pgObject) {
            if (pgObject == null)
                return null;
            try {
                return O.readValue(pgObject.getValue(), ManifestationInformation.class);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
