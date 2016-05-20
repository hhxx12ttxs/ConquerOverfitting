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
import org.adroitlogic.ultraesb.jmx.core.EndpointManagementMXBean;
import org.adroitlogic.ultraesb.jmx.view.AddressView;
import org.adroitlogic.ultraesb.jmx.view.EndpointView;

import javax.management.MalformedObjectNameException;
import javax.naming.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * Administration service for the managing endpoints
 */
@Path("endpoints")
@Produces(MediaType.APPLICATION_JSON)
public class EndpointAdmin extends AbstractAdmin {

    @GET
    @Path("getEndpoints")
    public List<EndpointView> getEndpoints() {
        logger.debug("Getting the endpoints list from the JMX connection");

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            return bean.getEndpoints();
        } catch (Exception e) {
            handleException(e);
        }
        return Collections.emptyList();
    }

    @GET
    @Path("getAddresses/{id}")
    public List<AddressView> getAddresses(@PathParam("id") String id) {
        logger.debug("Getting the addresses list from the JMX connection for endpoint : {}", id);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            return bean.getAddresses(id);
        } catch (Exception e) {
            handleException(e);
        }
        return Collections.emptyList();
    }

    @GET
    @Path("getEndpoint/{id}")
    public EndpointView getEndpoint(@PathParam("id") String id) {
        logger.debug("Getting the endpoint with id : {} from the JMX connection", id);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            return bean.getEndpoint(id);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @GET
    @Path("getAddress/{epId}/{addrId}")
    public AddressView getAddress(@PathParam("epId") String epId,
                                  @PathParam("addrId") String addrId) {
        logger.debug("Getting the address with id : {} within endpoint : {} from the JMX connection", addrId, epId);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            return bean.getAddress(epId, addrId);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @PUT
    @Path("updateEndpoint")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateEndpoint(EndpointView view) {
        logger.debug("Updating the endpoint with id : {} using the JMX connection", view.getId());

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.updateEndpoint(view);
            auditLogger.info("updateEndpoint() with the id : {} - current state :{}", view.getId(), view);

        } catch (Exception e) {
            handleException(e);
        }
    }

    @PUT
    @Path("updateAddress/{epId}/{addrId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateAddress(@PathParam("epId") String epId, @PathParam("addrId") String addrId, AddressView view) {

        logger.debug("Updating the address : {} of endpoint : {}", addrId, epId);
        logger.debug("Weight set to : {}", view.getWeight());

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.updateAddress(epId, addrId, view);
            auditLogger.info("updateEndpointAddress() with the id : {} - current state : {}", epId, view);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("addAddress/{epId}/{addrId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addAddress(@PathParam("epId") String epId, @PathParam("addrId") String addrId, AddressView view) {

        logger.debug("Adding the address : {} of endpoint : {}", addrId, epId);
        logger.debug("Weight set to : {}", view.getWeight());

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.addAddress(epId, addrId, view);
            auditLogger.info("addAddress() : {} of the endpoint id : {}", addrId, epId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("startEndpoint/{id}")
    public void startEndpoint(@PathParam("id") String id) throws AuthenticationException {
        logger.debug("Starting the endpoint with id : {} using the JMX connection", id);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.start(id);
            auditLogger.info("startEndpoint() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("stopEndpoint/{id}")
    public void stopEndpoint(@PathParam("id") String id) throws AuthenticationException {
        logger.debug("Stopped the endpoint with id : {} using the JMX connection", id);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.stop(id);
            auditLogger.info("stopEndpoint() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("enableEndpointDebug/{id}")
    public void enableEndpointDebug(@PathParam("id") String id) {
        logger.debug("Debugging the endpoint with id : {} using the JMX connection", id);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.enableDebug(id);
            auditLogger.info("enableEndpointDebug() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("disableEndpointDebug/{id}")
    public void disableEndpointDebug(@PathParam("id") String id) {
        logger.debug("Debugging the endpoint with id : {} using the JMX connection", id);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.disableDebug(id);
            auditLogger.info("disableEndpointDebug() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("resetEndpoint/{id}")
    public void resetEndpoint(@PathParam("id") String id) {
        logger.debug("Reset the endpoint with id : {} using the JMX connection", id);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.resetStatistics(id);
            auditLogger.info("resetEndpoint() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("resetAddressStatistics/{epId}/{addrId}")
    public void resetAddressAddressStatistics(@PathParam("epId") String epId, @PathParam("addrId") String addrId) {
        logger.debug("Reset the address : {} of endpoint : {} using the JMX connection", epId, addrId);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.resetAddressStatistics(epId, addrId);
            auditLogger.info("resetAddressStatistics() : {} of the endpoint id : {}", addrId, epId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("startAddress/{epId}/{addrId}")
    public void startAddress(@PathParam("epId") String epId, @PathParam("addrId") String addrId) {
        logger.debug("Start the address : {} of endpoint : {} using the JMX connection", addrId, epId);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.startAddress(epId, addrId);
            auditLogger.info("startAddress() : {} of the endpoint id : {}", addrId, epId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("stopAddress/{epId}/{addrId}")
    public void stopAddress(@PathParam("epId") String epId, @PathParam("addrId") String addrId) {
        logger.debug("Stop the address : {} of endpoint : {} using the JMX connection", addrId, epId);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.stopAddress(epId, addrId);
            auditLogger.info("stopAddress() : {} of the endpoint id : {}", addrId, epId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @DELETE
    @Path("deleteAddress/{epId}/{addrId}")
    public void deleteAddress(@PathParam("epId") String epId, @PathParam("addrId") String addrId) {
        logger.debug("Deleted the address : {} of endpoint : {} using the JMX connection", addrId, epId);

        try {
            EndpointManagementMXBean bean = getEndpointMXBeanProxy();
            bean.deleteAddress(epId, addrId);
            auditLogger.info("deleteEndpointAddress() : {} of the endpoint id : {}", addrId , epId);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private EndpointManagementMXBean getEndpointMXBeanProxy() throws MalformedObjectNameException {
        return getMXBeanProxy(JMXConstants.MXBEAN_NAME_ENDPOINTS, EndpointManagementMXBean.class);
    }
}

