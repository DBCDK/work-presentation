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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class WorkTreeBuilderIT extends JpaBase {

    private static final Logger log = LoggerFactory.getLogger(WorkTreeBuilderIT.class);

    @Test
    public void testAWork() throws Exception {
        System.out.println("testAWork");
        withConfigEnv()
                .jpaWithBeans(bf -> {
                    WorkTreeBuilder workTreeBuilder = bf.getWorkTreeBuilder();
                    WorkTree tree = workTreeBuilder.buildTree("work:62");
                    tree.prettyPrint(System.out::println);
                    assertThat(tree.getPersistentWorkId(), is("work-of-870970-basis:00010529"));
                });
    }

    @Test
    public void testWorkDoesNotExist() throws Exception {
        System.out.println("testWorkDoesNotExist");
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> {
        withConfigEnv()
                .jpaWithBeans(bf -> {
                    WorkTreeBuilder workTreeBuilder = bf.getWorkTreeBuilder();
                    WorkTree tree = workTreeBuilder.buildTree("work:3");
                    tree.prettyPrint(System.out::println);
                });
        });
        assertThat(ex.getMessage(), containsString("HTTP 404 Not Found"));
        assertThat(ex.getMessage(), containsString("/rest/objects/work:3"));
    }

    @Test
    public void testWorkDeleted() throws Exception {
        System.out.println("testWorkDeleted");
        withConfigEnv()
                .jpaWithBeans(bf -> {
                    WorkTreeBuilder workTreeBuilder = bf.getWorkTreeBuilder();
                    WorkTree tree = workTreeBuilder.buildTree("work:27827958");
                    tree.prettyPrint(System.out::println);
                    assertThat(tree.getPersistentWorkId(), nullValue());
                });
    }

}
