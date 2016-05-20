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

package org.adroitlogic.ultraesb.clustering.mgt;

import org.adroitlogic.ultraesb.clustering.ClusterManager;
import org.adroitlogic.ultraesb.clustering.commands.DebugCommand;
import org.adroitlogic.ultraesb.clustering.commands.ResetStatisticsCommand;
import org.adroitlogic.ultraesb.clustering.commands.StartCommand;
import org.adroitlogic.ultraesb.clustering.commands.StopCommand;
import org.adroitlogic.ultraesb.jmx.clustering.ClusterArtifactManagementMXBean;
import org.adroitlogic.ultraesb.jmx.JMXConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sampath
 * @since 1.4.0
 */
public class ClusterArtifactManagement implements ClusterArtifactManagementMXBean {

    private ClusterManager clusterManager;
    JMXConstants.ArtifactType type;
    private static final Logger logger = LoggerFactory.getLogger(ClusterArtifactManagement.class);

    public ClusterArtifactManagement(ClusterManager clusterManager, JMXConstants.ArtifactType type) {
        this.clusterManager = clusterManager;
        this.type = type;
    }

    @Override
    public String stop(String id) {
        logger.info("Publishing stop command of type : {}, for artifact id : {}", type, id);
        StopCommand command = new StopCommand(type, id);
        return clusterManager.publishControlCommand(command);
    }

    @Override
    public String start(String id) {
        logger.info("Publishing start command of type : {}, for artifact id : {}", type, id);
        StartCommand command = new StartCommand(type, id);
        return clusterManager.publishControlCommand(command);
    }

    @Override
    public String enableDebug(String id) {
        logger.info("Publishing enable debug command of type : {}, for artifact id : {}", type, id);
        DebugCommand command = new DebugCommand(type, id, DebugCommand.DebugAction.ENABLE);
        return clusterManager.publishControlCommand(command);
    }

    @Override
    public String disableDebug(String id) {
        logger.info("Publishing disable debug command of type : {}, for artifact id : {}", type, id);
        DebugCommand command = new DebugCommand(type, id, DebugCommand.DebugAction.DISABLE);
        return clusterManager.publishControlCommand(command);
    }

    @Override
    public String resetStatistics(String id) {
        logger.info("Publishing reset statistics command of type : {}, for artifact id : {}", type, id);
        ResetStatisticsCommand command = new ResetStatisticsCommand(type, id);
        return clusterManager.publishControlCommand(command);
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}

