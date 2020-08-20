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

import java.time.Duration;
import javax.ejb.embeddable.EJBContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class PresentationObjectBuilderIT extends JpaBase {

    public PresentationObjectBuilderIT() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of process method, of class PresentationObjectBuilder.
     */
    @Test
    public void testProcess() throws Exception {
        System.out.println("process");
        String pid = "";
        EJBContainer container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        PresentationObjectBuilder instance = (PresentationObjectBuilder)container.getContext().lookup("java:global/classes/PresentationObjectBuilder");
        instance.process(pid);
        container.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
