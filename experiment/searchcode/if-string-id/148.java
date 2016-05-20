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
import org.adroitlogic.ultraesb.jmx.core.ProxyServiceManagementMXBean;
import org.adroitlogic.ultraesb.jmx.view.ProxyServiceView;

import javax.management.MalformedObjectNameException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * Administration service for the proxy services
 */
@Path("proxyservices")
@Produces(MediaType.APPLICATION_JSON)
public class ProxyServiceAdmin extends AbstractAdmin {

    @GET
    @Path("getServices")
    public List<ProxyServiceView> getProxyServiceViews() {
        logger.debug("Getting the proxy services list from the JMX connection");

        try {
            ProxyServiceManagementMXBean bean = getProxyServiceMXBeanProxy();
            return bean.getProxyServices();
        } catch (Exception e) {
            handleException(e);
        }

        return Collections.emptyList();
    }

    @GET
    @Path("getService/{proxyId}")
    public ProxyServiceView getProxyServiceViews(@PathParam("proxyId") String proxyId) {
        logger.debug("Getting the proxy service with id :{} from the JMX connection" , proxyId);

        try {
            ProxyServiceManagementMXBean bean = getProxyServiceMXBeanProxy();
            return bean.getProxyService(proxyId);
        } catch (Exception e) {
            handleException(e);
        }

        return null;
    }

    @POST
    @Path("startProxyService/{id}")
    public void startProxySrevice(@PathParam("id") String id) {
        logger.debug("Starting the proxy service with id : {} using the JMX connection", id);

        try {
            ProxyServiceManagementMXBean bean = getProxyServiceMXBeanProxy();
            bean.start(id);
            auditLogger.info("startProxyService() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("stopProxyService/{id}")
    public void stopProxyService(@PathParam("id") String id) {
        logger.debug("Stop the proxy service with id : {} using the JMX connection", id);

        try {
            ProxyServiceManagementMXBean bean = getProxyServiceMXBeanProxy();
            bean.stop(id);
            auditLogger.info("stopProxyService() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("enableProxyServiceDebug/{id}")
    public void enableProxyServiceDebug(@PathParam("id") String id) {
        logger.debug("Enable debug for the proxy service with id : {} using the JMX connection", id);

        try {
            ProxyServiceManagementMXBean bean = getProxyServiceMXBeanProxy();
            bean.enableDebug(id);
            auditLogger.info("enableProxyServiceDebug() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @POST
    @Path("disableProxyServiceDebug/{id}")
    public void disableProxyServiceDebug(@PathParam("id") String id) {
        logger.debug("Disable debug for the proxy service with id : {} using the JMX connection", id);

        try {
            ProxyServiceManagementMXBean bean = getProxyServiceMXBeanProxy();
            bean.disableDebug(id);
            auditLogger.info("disableProxyServiceDebug() with the id : {}", id);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private ProxyServiceManagementMXBean getProxyServiceMXBeanProxy() throws MalformedObjectNameException {
        return getMXBeanProxy(JMXConstants.MXBEAN_NAME_PROXY_SERVICES, ProxyServiceManagementMXBean.class);
    }
}

