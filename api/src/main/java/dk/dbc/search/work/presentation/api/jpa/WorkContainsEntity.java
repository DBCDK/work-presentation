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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Entity
@Table(name = "workContains")
public class WorkContainsEntity implements Serializable {

    private static final long serialVersionUID = 0x1d74b2313c990594L;

    @Version
    int version;

    @EmbeddedId
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final WorkContainsKey pk;

    @Column(updatable = false, nullable = false)
    private String corepoWorkId;

    @Column(nullable = false)
    private String unitId;

    @Column(updatable = false, nullable = false)
    private String manifestationId;

    @Transient
    transient boolean persist;

    @Transient
    transient EntityManager em;

    public static WorkContainsEntity from(EntityManager em, String corepoWorkId, String manifestationId) {
        WorkContainsEntity entity = em.find(WorkContainsEntity.class, em, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        if (entity == null) {
            entity = new WorkContainsEntity(corepoWorkId, manifestationId);
        }
        entity.em = em;
        return entity;
    }

    public WorkContainsEntity() {
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
        em.detach(this);
    }

    public String getCorepoWorkId() {
        return corepoWorkId;
    }

    public void setCorepoWorkId(String corepoWorkId) {
        this.corepoWorkId = corepoWorkId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getManifestationId() {
        return manifestationId;
    }

    public void setManifestationId(String manifestationId) {
        this.manifestationId = manifestationId;
    }

}
