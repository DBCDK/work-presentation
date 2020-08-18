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

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class RelsSysTest extends Base {

    @Test
    public void testPrimaryManifestation() throws Exception {
        System.out.println("testPrimaryManifestation");
        withtResource("manifestation-primary.xml", is -> {
                  RelsSys relsSys = new RelsSys(is);
                  System.out.println("relsSys = " + relsSys);
                  assertThat(relsSys.isPrimary(), is(true));
                  assertThat(relsSys.getParent(), is("unit:2"));
                  assertThat(relsSys.getChildren(), empty());
              });
    }

    @Test
    public void testNonPrimaryManifestation() throws Exception {
        System.out.println("testNonPrimaryManifestation");
        withtResource("manifestation-non-primary.xml", is -> {
                  RelsSys relsSys = new RelsSys(is);
                  System.out.println("relsSys = " + relsSys);
                  assertThat(relsSys.isPrimary(), is(false));
                  assertThat(relsSys.getParent(), is("unit:2"));
                  assertThat(relsSys.getChildren(), empty());
              });
    }

    @Test
    public void testPrimaryUnit() throws Exception {
        System.out.println("testPrimaryUnit");
        withtResource("unit-primary.xml", is -> {
                  RelsSys relsSys = new RelsSys(is);
                  System.out.println("relsSys = " + relsSys);
                  assertThat(relsSys.isPrimary(), is(true));
                  assertThat(relsSys.getParent(), is("work:1"));
                  assertThat(relsSys.getChildren(), containsInAnyOrder("870970-basis:01430351", "300840-katalog:100643898", "850020-katalog:0023681", "800010-katalog:99122618592105763", "800010-katalog:99122875633105763", "800010-katalog:99122768539405763", "800010-katalog:99122502410405763"));
              });
    }

    @Test
    public void testNotPrimaryUnit() throws Exception {
        System.out.println("testNotPrimaryUnit");
        withtResource("unit-not-primary.xml", is -> {
                  RelsSys relsSys = new RelsSys(is);
                  System.out.println("relsSys = " + relsSys);
                  assertThat(relsSys.isPrimary(), is(false));
                  assertThat(relsSys.getParent(), is("work:1"));
                  assertThat(relsSys.getChildren(), containsInAnyOrder("870970-basis:04852222"));
              });

    }

    @Test
    public void testWork() throws Exception {
        System.out.println("testWork");
        withtResource("work.xml", is -> {
                  RelsSys relsSys = new RelsSys(is);
                  System.out.println("relsSys = " + relsSys);
                  assertThat(relsSys.isPrimary(), is(true));
                  assertThat(relsSys.getParent(), nullValue());
                  assertThat(relsSys.getChildren(), containsInAnyOrder("unit:2", "unit:331669", "unit:9991306"));
              });
    }

}
