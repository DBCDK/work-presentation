/*
 * Copyright (C) 2020 DBC A/S (http://dbc.dk/)
 *
 * This is part of work-presentation-service
 *
 * work-presentation-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * work-presentation-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.search.work.presentation.worker;

import java.io.InputStream;
import java.net.URI;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ClientIT {

    private static final Logger log = LoggerFactory.getLogger(ClientIT.class);

    // PROF OF CONCEPT - when deleting remember to delete testdata
    @Test
    public void testClient() throws Exception {

        BeanFactory bf = new BeanFactory(null, null, null);
        Config config = bf.getConfig();

        Client client = config.getHttpClient();
        String pid = "870970-basis:25912233";
        String stream = "RELS-SYS";

        URI uri = config.getCorepoContentService()
                .path("/rest/objects/{pid}/datastreams/{stream}/content")
                .build(pid, stream);

        try (InputStream is = client.target(uri)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(InputStream.class)) {
            // Do something with "is" - like parse it or whatever
        } catch (WebApplicationException ex) {
            log.error("Error getting datastream {}/{}: {}", pid, stream, ex.getMessage());
            log.debug("Error getting datastream {}/{}: ", pid, stream, ex);
        }
    }
}
