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
import org.junit.jupiter.api.function.ThrowingConsumer;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class Base {

    protected void withtResource(String file, ThrowingConsumer<InputStream> scope) throws Exception {
        String resource = getClass().getSimpleName().replaceAll("(IT|Test)$", "/" + file);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (is == null)
                throw new IllegalArgumentException("Cannot find resource: " + resource);
            scope.accept(is);
        } catch (Throwable t) {
            throw new Exception("Error processing with: " + resource, t);
        }
    }

}
