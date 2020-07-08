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
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@ApplicationScoped
@Startup
@Lock(LockType.READ)
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private final Map<String, String> env;

    public Config() {
        this.env = System.getenv();
    }

    public Config(String... strs) {
        env = Arrays.stream(strs)
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
        configure();
    }

    @PostConstruct
    public void init() {
        configure();
    }

    private void configure() {
        log.info("Reading/verifying configuration");
    }

//    private String get(String var) {
//        String value = env.get(var);
//        if (value == null)
//            throw new EJBException("Missing required configuration: " + var);
//        return value;
//    }
//
//    private String get(String var, String defaultValue) {
//        return env.getOrDefault(var, defaultValue);
//    }

}
