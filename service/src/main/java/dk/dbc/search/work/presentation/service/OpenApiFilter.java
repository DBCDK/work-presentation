/*
 * Copyright (C) 2021 DBC A/S (http://dbc.dk/)
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
package dk.dbc.search.work.presentation.service;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.servers.Server;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class OpenApiFilter implements OASFilter {

    public OpenApiFilter() {
        System.out.println("OpenApiFilter()");
    }

    @Override
    public Server filterServer(Server server) {
        String url = server.getUrl();
        System.out.println("url = " + url);
        return server.url("http://fool.me/");
    }
}
