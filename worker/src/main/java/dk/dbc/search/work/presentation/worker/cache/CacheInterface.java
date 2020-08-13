/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-worker
 *
 * work-presentation-worker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-worker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker.cache;

import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import java.sql.Timestamp;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class CacheInterface {

    @PersistenceContext(unitName = "workPresentation_PU")
    EntityManager em;

    /**
     * Given a CacheDataBuilder update the cache if it is necessary.
     *
     * @param dataBuilder Object that can build a ManifestationInformation
     *                    object
     */
    public void updateCache(CacheDataBuilder dataBuilder) {
        String manifestationId = dataBuilder.getManifestationId();
        Timestamp modified = dataBuilder.getModified();
        CacheEntity cacheObj = CacheEntity.from(em, manifestationId);
        if (dataBuilder.isDeleted()) {
            cacheObj.delete();
        } else {
            if (cacheObj.getModified() == null ||
                cacheObj.getModified().before(modified)) {
                cacheObj.setModified(modified);
                cacheObj.setContent(dataBuilder.generateContent());
                cacheObj.save();
            }
        }
    }
}
