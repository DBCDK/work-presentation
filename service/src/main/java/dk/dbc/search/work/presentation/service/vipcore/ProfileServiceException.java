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
package dk.dbc.search.work.presentation.service.vipcore;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class ProfileServiceException extends RuntimeException {

    private static final long serialVersionUID = 0xFF6A69FFEB4BF3B3L;

    private final String agencyId;
    private final String profileName;
    private final String uri;
    private final String message;

    public ProfileServiceException(String agencyId, String profileName, String uri, String message) {
        this.agencyId = agencyId;
        this.profileName = profileName;
        this.uri = uri;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "Cannot get profile for: " + agencyId + "/" + profileName + " via: " + uri + " cause: " + message;
    }

}
