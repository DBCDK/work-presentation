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

import dk.dbc.search.work.presentation.api.jpa.WorkObjectEntity;
import dk.dbc.search.work.presentation.worker.corepo.DataStreamMetaData;
import dk.dbc.search.work.presentation.worker.corepo.DataStreams;
import dk.dbc.search.work.presentation.worker.corepo.ObjectMetaData;
import dk.dbc.search.work.presentation.worker.corepo.RelsExt;
import dk.dbc.search.work.presentation.worker.corepo.RelsSys;
import dk.dbc.search.work.presentation.worker.tree.WorkTree;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class PresentationObjectBuilderIT extends JpaBase {

    @Test
    public void testWork() throws Exception {
        System.out.println("testWork");

        System.out.println("  Process a work");
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    PresentationObjectBuilder bean = beanFactory.getPresentationObjectBuilder();
                    bean.process("work:62", "track-me");
                });

        System.out.println("  Verify that the parts are in the database");
        assertThat(countWorkObjectEntries(), is(1));
        assertThat(countCacheEntries(), is(5));
        assertThat(countWorkContainsEntries(), is(5));
        jpa(em -> {
            WorkObjectEntity work = WorkObjectEntity.fromCorepoWorkId(em, "work:62");
            System.out.println("work = " + work);
            System.out.println("work = " + work.getContent());
            assertThat(work.getModified(), not(nullValue()));
            assertThat(work.getModified().toInstant().toString(), is("2020-06-17T19:32:07.853Z"));
        });

        System.out.println("  Process same work with a fake deleted record");
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    beanFactory.withWorkTreeBuilder(new WorkTreeBuilder() {
                        @Override
                        public WorkTree buildTree(String corepoWorkId) {
                            return new WorkTree(corepoWorkId, Instant.now());
                        }
                    });
                    PresentationObjectBuilder bean = beanFactory.getPresentationObjectBuilder();
                    bean.process("work:62", "track-me");
                });

        System.out.println("  Verify that the parts are purged from the database");
        assertThat(countWorkObjectEntries(), is(0));
        assertThat(countCacheEntries(), is(0));
        assertThat(countWorkContainsEntries(), is(0));
    }

    @Test
    public void testADeletedWork() throws Exception {
        System.out.println("testADeletedWork");

        System.out.println("  Process a work with a fake deleted record on an empty database");
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    beanFactory.withWorkTreeBuilder(new WorkTreeBuilder() {
                        @Override
                        public WorkTree buildTree(String corepoWorkId) {
                            return new WorkTree(corepoWorkId, Instant.now());
                        }
                    });
                    PresentationObjectBuilder bean = beanFactory.getPresentationObjectBuilder();
                    bean.process("work:62", "track-me");
                });

        System.out.println("  Verify that nothing has appeared in the database");
        assertThat(countWorkObjectEntries(), is(0));
        assertThat(countCacheEntries(), is(0));
        assertThat(countWorkContainsEntries(), is(0));
    }

    @Test
    public void testNewOwner() throws Exception {
        System.out.println("testNewOwner");

        // work:62 limited to one unit
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    CorepoContentServiceConnector real = beanFactory.getCorepoContentService();
                    CorepoContentServiceConnector mock = new CorepoContentServiceConnector() {

                        @Override
                        public RelsSys relsSys(String id) {
                            switch (id) {
                                case "work:62":
                                    return new RelsSys(null, Arrays.asList("unit:7892641"), true, id);
                                case "unit:7892641":
                                    return new RelsSys("work:62", Arrays.asList("830520-katalog:000025251"), true, id);
                                case "830520-katalog:000025251":
                                    return new RelsSys("unit:7892641", Arrays.asList(), true, id);
                                default:
                                    throw new AssertionError();
                            }
                        }

                        @Override
                        public String datastreamContent(String id, String stream) {
                            return real.datastreamContent(id, stream);
                        }

                        @Override
                        public DataStreams datastreams(String id) {
                            return real.datastreams(id);
                        }

                        @Override
                        public DataStreamMetaData datastreamMetaData(String id, String stream) {
                            return real.datastreamMetaData(id, stream);
                        }

                        @Override
                        public ObjectMetaData objectMetaData(String id) {
                            return real.objectMetaData(id);
                        }

                        @Override
                        public RelsExt relsExt(String id) {
                            return real.relsExt(id);
                        }
                    };
                    beanFactory.withCorepoContentServiceConnector(mock);

                    PresentationObjectBuilder bean = beanFactory.getPresentationObjectBuilder();
                    bean.process("work:62", "track-me");
                });

        jpa(em -> {
            assertThat(WorkObjectEntity.readOnlyFrom(em, "work-of:830520-katalog:000025251"), notNullValue());

        });

        // Full work:62
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    PresentationObjectBuilder bean = beanFactory.getPresentationObjectBuilder();
                    bean.process("work:62", "track-me");
                });
        jpa(em -> {
            assertThat(WorkObjectEntity.readOnlyFrom(em, "work-of:830520-katalog:000025251"), nullValue());
            assertThat(WorkObjectEntity.readOnlyFrom(em, "work-of:870970-basis:00010529"), notNullValue());
        });
    }

    @Test
    public void testStealManifestation() throws Exception {
        System.out.println("testStealManifestation");

        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    PresentationObjectBuilder bean = beanFactory.getPresentationObjectBuilder();
                    bean.process("work:62", "track-me");
                });

        // work:1062 has one of work:62's manifestations
        withConfigEnv()
                .jpaWithBeans(beanFactory -> {
                    CorepoContentServiceConnector real = beanFactory.getCorepoContentService();
                    CorepoContentServiceConnector mock = new CorepoContentServiceConnector() {

                        @Override
                        public RelsSys relsSys(String id) {
                            switch (id) {
                                case "work:1062":
                                    return new RelsSys(null, Arrays.asList("unit:1062"), true, id);
                                case "unit:1062":
                                    return new RelsSys("work:1062", Arrays.asList("830520-katalog:000025251"), true, id);
                                case "830520-katalog:000025251":
                                    return new RelsSys("unit:1062", Arrays.asList(), true, id);
                                default:
                                    throw new AssertionError();
                            }
                        }

                        @Override
                        public String datastreamContent(String id, String stream) {
                            switch (id) {
                                case "work:1062":
                                    id = "work:62";
                                    break;
                                case "unit:1062":
                                    id = "unit:7892641";
                                    break;
                                default:
                                    break;
                            }
                            return real.datastreamContent(id, stream);
                        }

                        @Override
                        public DataStreams datastreams(String id) {
                            switch (id) {
                                case "work:1062":
                                    id = "work:62";
                                    break;
                                case "unit:1062":
                                    id = "unit:7892641";
                                    break;
                                default:
                                    break;
                            }
                            return real.datastreams(id);
                        }

                        @Override
                        public DataStreamMetaData datastreamMetaData(String id, String stream) {
                            switch (id) {
                                case "work:1062":
                                    id = "work:62";
                                    break;
                                case "unit:1062":
                                    id = "unit:7892641";
                                    break;
                                default:
                                    break;
                            }
                            return real.datastreamMetaData(id, stream);
                        }

                        @Override
                        public ObjectMetaData objectMetaData(String id) {
                            switch (id) {
                                case "work:1062":
                                    id = "work:62";
                                    break;
                                case "unit:1062":
                                    id = "unit:7892641";
                                    break;
                                default:
                                    break;
                            }
                            return real.objectMetaData(id);
                        }

                        @Override
                        public RelsExt relsExt(String id) {
                            switch (id) {
                                case "work:1062":
                                    id = "work:62";
                                    break;
                                case "unit:1062":
                                    id = "unit:7892641";
                                    break;
                                default:
                                    break;
                            }
                            return real.relsExt(id);
                        }
                    };
                    beanFactory.withCorepoContentServiceConnector(mock);

                    PresentationObjectBuilder bean = beanFactory.getPresentationObjectBuilder();
                    bean.process("work:1062", "track-me");
                });

    }

}
