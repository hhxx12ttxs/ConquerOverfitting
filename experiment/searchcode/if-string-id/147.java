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
 * You should have received a copy of the GNU Affero General Public License along with this program (See LICENSE.AGPL).
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

package org.adroitlogic.ultraesb.jmx.core;

import org.adroitlogic.ultraesb.jmx.view.TransportView;

import java.util.List;

/**
 * @author asankha
 */
public interface TransportManagementMXBean extends GenericMXBean {

    public List<TransportView> getTransportListeners();

    public List<TransportView> getTransportSenders();

    public TransportView getTransportListener(String id);

    public TransportView getTransportSender(String id);

    public void startListener(String id);

    public void startSender(String id);

    public void stopListener(String id);

    public void stopSender(String id);

    public void pauseListener(String id);

    public void pauseSender(String id);

    public void resumeListener(String id);

    public void resumeSender(String id);

    public void listenerMaintenanceShutdown(String id, long delay);

    public void senderMaintenanceShutdown(String id, long delay);

    public void resetListenerStatistics(String id);

    public void resetSenderStatistics(String id);

    public void setSenderDebugOn(String id, boolean debugOn);

    public void setListenerDebugOn(String id, boolean debugOn);
}

