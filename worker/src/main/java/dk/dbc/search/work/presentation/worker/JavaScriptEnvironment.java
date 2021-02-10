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

import dk.dbc.search.work.presentation.api.pojo.ManifestationInformation;
import dk.dbc.search.work.presentation.javascript.JavascriptCacheObjectBuilder;
import dk.dbc.search.work.presentation.javascript.JavascriptWorkOwnerSelector;
import dk.dbc.search.work.presentation.worker.pool.QuickPool;
import dk.dbc.search.work.presentation.worker.tree.CacheContentBuilder;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class JavaScriptEnvironment {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptEnvironment.class);

    QuickPool<JavascriptCacheObjectBuilder> jsWorkers;
    QuickPool<JavascriptWorkOwnerSelector> jsOwnerSelectors;

    @Inject
    Config config;

    @Inject
    CorepoContentServiceConnector corepoContentService;

    @PostConstruct
    public void init() {
        try {
            jsWorkers = new QuickPool<>(JavascriptCacheObjectBuilder.builder()
                    .build());
            jsWorkers.setMinIdle(0);
            jsWorkers.setMaxIdle(config.getJsPoolSize());
            jsWorkers.setMaxTotal(config.getJsPoolSize());
            jsWorkers.addObjects(config.getJsPoolSize());

            int jsOwnerSelectorsCount = 1 + ( config.getThreads() / 5 ); // seldom run in parallel
            jsOwnerSelectors = new QuickPool<>(JavascriptWorkOwnerSelector.builder()
                    .build());
            jsOwnerSelectors.setMinIdle(0);
            jsOwnerSelectors.setMaxIdle(jsOwnerSelectorsCount);
            jsOwnerSelectors.setMaxTotal(jsOwnerSelectorsCount);
            jsOwnerSelectors.addObjects(jsOwnerSelectorsCount);
        } catch (Exception ex) {
            log.error("Error building JavaScript environments: {}", ex.getMessage());
            log.debug("Error building JavaScript environments: ", ex);
            throw new EJBException("Error building JavaScript environments");
        }
    }

    /**
     * Call the JavaScript environment to build a cache document
     *
     * @param dataBuilder document builder.
     * @return The content to store in the database
     */
    @Timed(reusable = true)
    public ManifestationInformation cacheBuild(CacheContentBuilder dataBuilder) {
        try {
            return jsWorkers
                    .exec(js -> dataBuilder.generateContent(corepoContentService, js))
                    .raise(Exception.class)
                    .value();
        } catch (Exception ex) {
            log.error("Error building content for: {}: {}", dataBuilder.getManifestationId(), ex.getMessage());
            log.debug("Error building content for: {}: ", dataBuilder.getManifestationId(), ex);

            throw new EJBException("Error building content for: " + dataBuilder.getManifestationId());
        }
    }

    /**
     * Call the JavaScript to find the owner of a work
     *
     * @param potentialOwners map of manifestationId to
     *                        ManifestationInformation, for the primary in the
     *                        units
     * @param corepoWorkId            Id for exception
     * @return manifestaionId of most relevant
     */
    @Timed(reusable = true)
    public String getOwnerId(HashMap<String, ManifestationInformation> potentialOwners, String corepoWorkId) {
        try {
            return jsOwnerSelectors.exec(js -> jwos.selectOwner(potentialOwners))
                    .raise(Exception.class)
                    .value();
        } catch (Exception ex) {
            log.error("Error computing owner of work: {}", ex.getMessage());
            log.debug("Error computing owner of work: ", ex);
            throw new EJBException("Error finding owner for work: " + corepoWorkId);
        }
    }

}
