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

package org.adroitlogic.ultraesb.admin;

import org.adroitlogic.ultraesb.jmx.JMXConstants;
import org.adroitlogic.ultraesb.jmx.core.SequenceManagementMXBean;
import org.adroitlogic.ultraesb.jmx.view.SequenceView;

import javax.management.MalformedObjectNameException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * Administration service for the managing sequences
 */
@Path("sequences")
@Produces(MediaType.APPLICATION_JSON)
public class SequenceAdmin extends AbstractAdmin {

    @GET
    @Path("getSequences")
    public List<SequenceView> getSequences() {
        logger.debug("Getting the sequences list from the JMX connection");

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            return bean.getSequences();
        } catch (Exception e) {
            handleException(e);
        }
        return Collections.emptyList();
    }

    @GET
    @Path("getSequence/{id}")
    public SequenceView getEndpoint(@PathParam("id") String id) {
        logger.debug("Getting the sequence with id : {} from the JMX connection", id);

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            return bean.getSequence(id);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @PUT
    @Path("updateSequence")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateSequence(SequenceView view) {
        logger.debug("Updating the sequence with id : {} using the JMX connection", view.getId());

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            bean.updateSequence(view);
            auditLogger.info("updateSequence() with the id : {} - current state : {}", view.getId(), view);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("startSequence/{id}")
    public void startSequence(@PathParam("id") String id) {
        logger.debug("Starting the sequence with id : {} using the JMX connection", id);

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            bean.start(id);
            auditLogger.info("startSequence() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("stopSequence/{id}")
    public void stopSequence(@PathParam("id") String id) {
        logger.debug("Stop the sequence with id : {} using the JMX connection", id);

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            bean.stop(id);
            auditLogger.info("stopSequence() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("enableDebugSequence/{id}")
    public void enableDebugSequence(@PathParam("id") String id) {
        logger.debug("Enable debug the sequence with id : {} using the JMX connection", id);

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            bean.enableDebug(id);
            auditLogger.info("enableDebugSequence() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("disableDebugSequence/{id}")
    public void disableDebugSequence(@PathParam("id") String id) {
        logger.debug("Disable debug the sequence with id : {} using the JMX connection", id);

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            bean.disableDebug(id);
            auditLogger.info("disableDebugSequence() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("resetStatisticsSequence/{id}")
    public void resetStatisticsSequence(@PathParam("id") String id) {
        logger.debug("Reset statistics the sequence with id : {} using the JMX connection", id);

        try {
            SequenceManagementMXBean bean = getSequenceMXBeanProxy();
            bean.resetStatistics(id);
            auditLogger.info("resetStatisticsSequence() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private SequenceManagementMXBean getSequenceMXBeanProxy() throws MalformedObjectNameException {
        return getMXBeanProxy(JMXConstants.MXBEAN_NAME_SEQUENCES, SequenceManagementMXBean.class);
    }

}

