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
package dk.dbc.search.work.presentation.worker;

import dk.dbc.search.work.presentation.api.jpa.CacheEntity;
import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.MDC;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class AsyncCacheContentBuilder {

    @Inject
    JavaScriptEnvironment jsEnv;

    @PersistenceContext(unitName = "workPresentation_PU")
    EntityManager em;

    /**
     * Acquire cache content, asynchronously and in its own transaction
     * <p>
     * This allows for saving cache entries outside the master transaction.
     *
     * @param dataBuilder The content provider
     * @param mdc         the log mdc values
     * @param delete      if the databuilder is tagged as deleted, should we
     *                    delete from the database
     * @return null if deleted otherwise the content from the cache
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Timed
    public Future<ManifestationInformation> getFromCache(CacheContentBuilder dataBuilder, Map<String, String> mdc, boolean delete) {
        try {
            MDC.setContextMap(mdc == null ? Collections.EMPTY_MAP : mdc);
            String manifestationId = dataBuilder.getManifestationId();
            CacheEntity cacheObj = CacheEntity.detachedFrom(em, manifestationId); // null if none existant

            if (delete && dataBuilder.isDeleted()) {
                if (cacheObj != null) { // delete entry in database
                    CacheEntity.from(em, manifestationId).delete();
                }
                return new AsyncResult<>(null);
            }
            if (cacheObj == null ||
                cacheObj.getModified().before(dataBuilder.getModified())) {
                ManifestationInformation content = jsEnv.cacheBuild(dataBuilder);
                cacheObj = CacheEntity.from(em, manifestationId);
                cacheObj.setContent(content);
                cacheObj.setModified(dataBuilder.getModified());
                cacheObj.save();
                return new AsyncResult<>(content);
            }
            return new AsyncResult<>(cacheObj.getContent());
        } finally {
            MDC.clear();
        }
    }
}
