/*
 * AdroitLogic UltraESB Enterprise Service Bus
 *
 * Copyright (c) 2010-2012 AdroitLogic Private Ltd. (http://adroitlogic.org). All Rights Reserved.
 *
 * GNU Affero General Public License Usage
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program (See LICENSE-AGPL.TXT).
 * If not, see http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Commercial Usage
 *
 * Licensees holding valid UltraESB Commercial licenses may use this file in accordance with the UltraESB Commercial
 * License Agreement provided with the Software or, alternatively, in accordance with the terms contained in a written
 * agreement between you and AdroitLogic.
 *
 * If you are unsure which license is appropriate for your use, or have questions regarding the use of this file,
 * please contact AdroitLogic at info@adroitlogic.com
 */

package org.adroitlogic.ultraesb.admin.to;

/**
 * @author sampath
 * @since 1.5.0
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ServerURLInfo {

    private String id;
    private String jmxURL;
    private String jmxUsername;
    private String jmxPassword;

    public ServerURLInfo() {}

    public ServerURLInfo(String id, String jmxURL) {
        this.id = id;
        this.jmxURL = jmxURL;
    }

    public ServerURLInfo(String id, String jmxURL, String jmxUsername) {
        this.id = id;
        this.jmxURL = jmxURL;
        this.jmxUsername = jmxUsername;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJmxURL() {
        return jmxURL;
    }

    public void setJmxURL(String jmxURL) {
        this.jmxURL = jmxURL;
    }

    public String getJmxUsername() {
        return jmxUsername;
    }

    public void setJmxUsername(String jmxUsername) {
        this.jmxUsername = jmxUsername;
    }

    public String getJmxPassword() {
        return jmxPassword;
    }

    public void setJmxPassword(String jmxPassword) {
        this.jmxPassword = jmxPassword;
    }
}

