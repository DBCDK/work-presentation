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

import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This produces all the fields a presentation request possibly can result in.
 * <p>
 * The presentation request then filters this.
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class PresentationObjectBuilder {

    private static final Logger log = LoggerFactory.getLogger(PresentationObjectBuilder.class);

    @Inject
    WorkTreeBuilder workTreeBuilder;

    @Inject
    ParallelCacheContentBuilder parallelCacheContentBuilder;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Timed
    public void process(String pid) {
        if (!pid.startsWith("work:")) {
            log.info("Skipping job: {}", pid);
            return;
        }
        log.info("Processing job: {}", pid);

        WorkTree tree = workTreeBuilder.buildTree(pid);
        tree.prettyPrint(log::trace);
        parallelCacheContentBuilder.updateCache(tree); // Needs to be before .updateWorkContains(tree)
        parallelCacheContentBuilder.updateWorkContains(tree);
    }

}
