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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Entity
@Table(name = "workContains")
@NamedQuery(
        name = "allWithCorepoWorkId",
        query = "SELECT w FROM WorkContainsEntity w WHERE w.corepoWorkId = :corepoWorkId"
)
public class WorkContainsEntity implements Serializable {

    private static final long serialVersionUID = 0x1d74b2313c990594L;

    @Version
    int version;

    @EmbeddedId
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final WorkContainsKey pk;

    @Column(updatable = false, nullable = false)
    private String corepoWorkId;

    @Column(updatable = false, nullable = false)
    private String manifestationId;

    @Transient
    transient boolean persist;

    @Transient
    transient EntityManager em;

    public static WorkContainsEntity from(EntityManager em, String corepoWorkId, String manifestationId) {
        WorkContainsEntity entity = em.find(WorkContainsEntity.class,
                                            new WorkContainsKey(corepoWorkId, manifestationId),
                                            LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        if (entity == null) {
            entity = new WorkContainsEntity(corepoWorkId, manifestationId);
        }
        entity.em = em;
        return entity;
    }

    /**
     * Produce all WorkContainsEntities that have a given corepoWorkId
     *
     * @param em           EntityManager that fetches WorkContainsEntities
     * @param corepoWorkId The common work id for all the records
     * @return a list of WorkContainsEntity
     */
    public static List<WorkContainsEntity> listFrom(EntityManager em, String corepoWorkId) {
        List<WorkContainsEntity> works = em.createNamedQuery("allWithCorepoWorkId", WorkContainsEntity.class)
                .setParameter("corepoWorkId", corepoWorkId)
                .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                .getResultList();
        works.forEach(w -> {
            w.em = em;
            w.persist = false;
        });
        return works;
    }

    /**
     * Ensure that the database only contains the entries from the is after
     * commit
     *
     * @param em           EntityManager that stores WorkContainsEntities
     * @param corepoWorkId The common work id for all the records, required if
     *                     works is an empty list
     * @param works        The elements that comprises this work
     */
    public static void updateToList(EntityManager em, String corepoWorkId, Collection<WorkContainsEntity> works) {
        HashSet<WorkContainsEntity> existing = new HashSet<>(listFrom(em, corepoWorkId));
        works.forEach(w -> {
            existing.remove(w);
            w.save();
        });
        existing.forEach(w -> {
            w.delete();
        });
    }

    protected WorkContainsEntity() {
        this.pk = new WorkContainsKey();
        this.persist = false;
    }

    private WorkContainsEntity(String corepoWorkId, String manifestationId) {
        this.pk = new WorkContainsKey(corepoWorkId, manifestationId);
        this.corepoWorkId = corepoWorkId;
        this.manifestationId = manifestationId;
        this.persist = true;
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

    public String getCorepoWorkId() {
        return corepoWorkId;
    }

    public void setCorepoWorkId(String corepoWorkId) {
        this.corepoWorkId = corepoWorkId;
    }

    public String getManifestationId() {
        return manifestationId;
    }

    public void setManifestationId(String manifestationId) {
        this.manifestationId = manifestationId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.corepoWorkId);
        hash = 47 * hash + Objects.hashCode(this.manifestationId);
        hash = 47 * hash + this.version;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final WorkContainsEntity other = (WorkContainsEntity) obj;
        return this.version == other.version &&
               Objects.equals(this.corepoWorkId, other.corepoWorkId) &&
               Objects.equals(this.manifestationId, other.manifestationId);
    }

    @Override
    public String toString() {
        return "WorkContainsEntity{" + "version=" + version + ", corepoWorkId=" + corepoWorkId + ", manifestationId=" + manifestationId + '}';
    }
}
