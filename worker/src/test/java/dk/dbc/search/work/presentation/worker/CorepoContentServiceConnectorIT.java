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

import dk.dbc.search.work.presentation.worker.corepo.DataStreamMetaData;
import dk.dbc.search.work.presentation.worker.corepo.ObjectMetaData;
import dk.dbc.search.work.presentation.worker.corepo.RelsSys;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class CorepoContentServiceConnectorIT extends JpaBase {

    @Test
    public void testObjectMetaData() throws Exception {
        System.out.println("testObjectMetaData");
        withConfigEnv().jpaWithBeans(beanFactory -> {
            CorepoContentServiceConnector bean = beanFactory.getCorepoContentService();
            ObjectMetaData metaData = bean.objectMetaData("work:19");
            System.out.println("metaData = " + metaData);
            assertThat(metaData.isActive(), is(true));
            assertThat(metaData.getCreated().toString(), is("2015-07-17T08:55:08.022Z"));
        });
    }

    @Test
    public void testDatastreamContent() throws Exception {
        System.out.println("testdataStreamContent");
        withConfigEnv().jpaWithBeans(beanFactory -> {
            CorepoContentServiceConnector bean = beanFactory.getCorepoContentService();
            String commonData = bean.datastreamContent("870970-basis:22476319", "commonData");
            System.out.println("commonData = " + commonData);
            assertThat(commonData, containsString("Udviklingsværkstedet for Naturformidling"));
        });
    }

    @Test
    public void testDatastreamMetaData() throws Exception {
        System.out.println("testDatastreamMetaData");
        withConfigEnv().jpaWithBeans(beanFactory -> {
            CorepoContentServiceConnector bean = beanFactory.getCorepoContentService();
            DataStreamMetaData dataStream = bean.datastreamMetaData("870970-basis:22476319", "commonData");
            System.out.println("dataStream = " + dataStream);
            assertThat(dataStream.isActive(), is(true));
            assertThat(dataStream.getCreated().toString(), is("2018-11-24T22:58:36.175Z"));
        });
    }

    @Test
    public void testRelsSys() throws Exception {
        System.out.println("testRelsSys");
        withConfigEnv().jpaWithBeans(beanFactory -> {
            CorepoContentServiceConnector bean = beanFactory.getCorepoContentService();
            RelsSys relsSys = bean.relsSys("work:19");
            System.out.println("relsSys = " + relsSys);
            assertThat(relsSys.getChildren(), containsInAnyOrder("unit:20"));
        });
    }

}
