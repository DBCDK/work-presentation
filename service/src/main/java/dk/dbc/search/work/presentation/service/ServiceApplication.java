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
package dk.dbc.search.work.presentation.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@ApplicationPath("api")
public class ServiceApplication extends Application {

    private static final Set<Class<?>> CLASSES = new HashSet<Class<?>>(Arrays.asList(
            WorkPresentationBean.class
    ));

    @Override
    public Set<Class<?>> getClasses() {
        return CLASSES;
    }

    @Override
    public Set<Object> getSingletons() {
        HashSet<Object> singletons = new HashSet<>();
        singletons.addAll(super.getSingletons());
        singletons.add(new CorsFilter());
        return singletons;
    }
}
