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
package dk.dbc.search.work.presentation.worker.corepo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class RelsExtTest extends Base {

    @Test
    public void testRelsExt() throws Exception {
        System.out.println("testRelsExt");
        withResource("content.xml", is -> {
                  RelsExt relsExt = new RelsExt(is);
                  System.out.println("relsExt = " + relsExt);
                  assertThat(relsExt.get(RelsExtType.HAS_REVIEW), not(empty()));
                  assertThat(relsExt.get(RelsExtType.HAS_DESCRIPTION_FROM_PUBLISHER), contains("unit:30729947"));
                  assertThat(relsExt.get(RelsExtType.IS_REVIEW_OF), empty());
              });
    }
}
