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
import dk.dbc.search.work.presentation.api.pojo.WorkInformation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.postgresql.util.PGobject;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.LockModeType;
import javax.persistence.NamedQuery;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Entity
@Table(name = "workObjectV" + JsonSchemaVersion.VERSION)
@NamedQuery(
        name = "withCorepoWorkId",
        query = "SELECT r FROM WorkObjectEntity r WHERE r.corepoWorkId = :corepoWorkId"
)
public class WorkObjectEntity implements Serializable {

    private static final long serialVersionUID = 0x6d07e1639b2ced36L;

    @Version
    int version;

    @Id
    @Column(updatable = false, nullable = false)
    private String persistentWorkId;

    @Column(nullable = false)
    private String corepoWorkId;

    @Column(nullable = false)
    private Timestamp modified;

    @Column(nullable = false)
    @Convert(converter = WorkObjectEntity.JsonConverter.class)
    private WorkInformation content;

    @Transient
    transient boolean persist;

    @Transient
    transient EntityManager em;

    public static WorkObjectEntity from(EntityManager em, String persistentWorkId) {
        WorkObjectEntity entity = em.find(WorkObjectEntity.class,
                                          persistentWorkId,
                                          LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        if (entity == null) {
            entity = new WorkObjectEntity(persistentWorkId);
        }
        entity.em = em;
        return entity;
    }

    public static WorkObjectEntity readOnlyFrom(EntityManager em, String persistentWorkId) {
        WorkObjectEntity entity = em.find(WorkObjectEntity.class,
                                          persistentWorkId,
                                          LockModeType.NONE);
        if (entity != null)
            em.detach(entity);
        return entity;
    }

    public static WorkObjectEntity fromCorepoWorkId(EntityManager em, String corepoWorkId) {
        WorkObjectEntity entity = em.createNamedQuery("withCorepoWorkId", WorkObjectEntity.class)
                .setParameter("corepoWorkId", corepoWorkId)
                .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                .setMaxResults(1)
                .getResultStream()
                .findAny()
                .orElse(null);
        if (entity != null)
            entity.em = em;
        return entity;
    }

    public static WorkObjectEntity readOnlyFromCorepoWorkId(EntityManager em, String corepoWorkId) {
        WorkObjectEntity entity = em.createNamedQuery("withCorepoWorkId", WorkObjectEntity.class)
                .setParameter("corepoWorkId", corepoWorkId)
                .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                .setMaxResults(1)
                .getResultStream()
                .findAny()
                .orElse(null);
        if (entity != null)
            em.detach(entity);
        return entity;
    }

    protected WorkObjectEntity() {
        this.persist = false;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public WorkObjectEntity(String persistentWorkId, String corepoWorkId, Timestamp modified, WorkInformation content) {
        this.persistentWorkId = persistentWorkId;
        this.corepoWorkId = corepoWorkId;
        this.modified = modified;
        this.content = content;
        this.version = 0;
        this.persist = true;
    }

    private WorkObjectEntity(String persistentWorkId) {
        this.persistentWorkId = persistentWorkId;
        this.persist = true;
    }

    public void save() {
        if (persist) {
            em.persist(this);
        } else {
            em.merge(this);
        }
        persist = false;
    }

    public void delete() {
        if (!persist) {
            em.remove(this);
            em.flush();
        }
        persist = true;
    }

    public String getPersistentWorkId() {
        return persistentWorkId;
    }

    public void setPersistentWorkId(String persistentWorkId) {
        this.persistentWorkId = persistentWorkId;
    }

    public String getCorepoWorkId() {
        return corepoWorkId;
    }

    public void setCorepoWorkId(String corepoWorkId) {
        this.corepoWorkId = corepoWorkId;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Timestamp getModified() {
        return modified;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setModified(Timestamp modified) {
        this.modified = modified;
    }

    public WorkInformation getContent() {
        return content;
    }

    public void setContent(WorkInformation content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.persistentWorkId);
        hash = 47 * hash + Objects.hashCode(this.corepoWorkId);
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
        final WorkObjectEntity other = (WorkObjectEntity) obj;
        return this.version == other.version &&
               Objects.equals(this.persistentWorkId, other.persistentWorkId) &&
               Objects.equals(this.corepoWorkId, other.corepoWorkId) &&
               Objects.equals(this.content, other.content) &&
               Objects.equals(this.modified, other.modified);
    }

    @Override
    public String toString() {
        return "RecordEntity{" + "persistentWorkId=" + persistentWorkId + ", corepoWorkId=" + corepoWorkId + ", modified=" + modified + ", version=" + version + '}';
    }

    @Converter
    public static class JsonConverter implements AttributeConverter<WorkInformation, PGobject> {

        private static final ObjectMapper O = new ObjectMapper();

        @Override
        public PGobject convertToDatabaseColumn(WorkInformation workInformation) throws IllegalStateException {
            try {
                final PGobject res = new PGobject();
                res.setType("jsonb");
                if (workInformation == null) {
                    res.setValue(null);
                } else {
                    res.setValue(O.writeValueAsString(workInformation));
                }
                return res;
            } catch (SQLException | JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public WorkInformation convertToEntityAttribute(PGobject pgObject) {
            if (pgObject == null) {
                return null;
            }
            try {
                return O.readValue(pgObject.getValue(), WorkInformation.class);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
