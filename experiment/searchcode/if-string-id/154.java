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

package org.adroitlogic.ultraesb.core.mgt;

import org.adroitlogic.ultraesb.jmx.core.TransportManagementMXBean;
import org.adroitlogic.ultraesb.jmx.view.TransportView;
import org.adroitlogic.ultraesb.transport.TransportListener;
import org.adroitlogic.ultraesb.transport.TransportSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.adroitlogic.ultraesb.api.ConfigurationEvent.ElementType;

/**
 * @author asankha
 */
public class TransportManagement extends ConfigurationElementManagement implements TransportManagementMXBean {

    private final Map<String, TransportListener> listeners = new HashMap<String, TransportListener>();
    private final Map<String, TransportSender>   senders   = new HashMap<String, TransportSender>();
    public static final Logger logger = LoggerFactory.getLogger(TransportManagement.class);

    public void shutdown() {
        for (TransportListener listener : new ArrayList<TransportListener>(listeners.values())) {
            unregisterListener(listener);
        }
        for (TransportSender sender : new ArrayList<TransportSender>(senders.values())) {
            unregisterSender(sender);
        }
    }

    public void registerListener(TransportListener listener) {
        assertInitialized();
        listeners.put(listener.getId(), listener);
        elementRegistered(ElementType.ELEMENT_TYPE_TRANSPORT_LISTENER, listener);
    }

    public void registerSender(TransportSender sender) {
        assertInitialized();
        senders.put(sender.getId(), sender);
        elementRegistered(ElementType.ELEMENT_TYPE_TRANSPORT_SENDER, sender);
    }

    public void unregisterListener(TransportListener listener) {
        assertInitialized();
        listeners.remove(listener.getId());
        elementUnregistered(ElementType.ELEMENT_TYPE_TRANSPORT_LISTENER, listener);
    }

    public void unregisterSender(TransportSender sender) {
        assertInitialized();
        senders.remove(sender.getId());
        elementUnregistered(ElementType.ELEMENT_TYPE_TRANSPORT_SENDER, sender);
    }

    public List<TransportView> getTransportListeners() {
        List<TransportView> values = new ArrayList<TransportView>(listeners.size());
        for (TransportListener tl : listeners.values()) {
            values.add(tl.toDetailedView());
        }
        return values;
    }

    public List<TransportView> getTransportSenders() {
        List<TransportView> values = new ArrayList<TransportView>(senders.size());
        for (TransportSender ts : senders.values()) {
            values.add(ts.toDetailedView());
        }
        return values;
    }

    public TransportView getTransportListener(String id) {
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            return tl.toDetailedView();
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public TransportView getTransportSender(String id) {
        TransportSender ts = senders.get(id);
        if (ts != null) {
            return ts.toDetailedView();
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }

    public void startListener(String id) {
        logger.info("Starting the listener with id : {}, of the running instance", id);
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            tl.start();
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public void startSender(String id) {
        logger.info("Starting the sender with id : {}, of the running instance", id);
        TransportSender ts = senders.get(id);
        if (ts != null) {
            ts.start();
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }

    public void stopListener(String id) {
        logger.info("Stopping the listener with id : {}, of the running instance", id);
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            tl.stop();
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public void stopSender(String id) {
        logger.info("Stopping the sender with id : {}, of the running instance", id);
        TransportSender ts = senders.get(id);
        if (ts != null) {
            ts.stop();
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }

    public void pauseListener(String id) {
        logger.info("Pausing the listener with id : {}, of the running instance", id);
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            tl.pause();
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public void pauseSender(String id) {
        logger.info("Pausing the sender with id : {}, of the running instance", id);
        TransportSender ts = senders.get(id);
        if (ts != null) {
            ts.pause();
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }

    public void resumeListener(String id) {
        logger.info("Resuming the listener with id : {}, of the running instance", id);
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            tl.resume();
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public void resumeSender(String id) {
        logger.info("Resuming the sender with id : {}, of the running instance", id);
        TransportSender ts = senders.get(id);
        if (ts != null) {
            ts.resume();
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }

    public void listenerMaintenanceShutdown(String id, long delay) {
        logger.info("Maintenance shutdown the listener with id : {}, and delay : {}, of the running instance", id, delay);
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            tl.maintenanceShutdown(delay);
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public void senderMaintenanceShutdown(String id, long delay) {
        logger.info("Maintenance shutdown the sender with id : {}, and delay : {}, of the running instance", id, delay);
        TransportSender ts = senders.get(id);
        if (ts != null) {
            ts.maintenanceShutdown(delay);
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }

    public void resetListenerStatistics(String id) {
        logger.info("Resetting statistics of the listener with id : {}, of the running instance", id);
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            tl.resetStatistics();
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public void resetSenderStatistics(String id) {
        logger.info("Resetting statistics of the sender with id : {}, of the running instance", id);
        TransportSender ts = senders.get(id);
        if (ts != null) {
            ts.resetStatistics();
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }

    public void setListenerDebugOn(String id, boolean debugOn) {
        logger.info("Changing debug level of the listener with id : {}, into : {}, of the running instance", id,
            debugOn ? "on" : "off");
        TransportListener tl = listeners.get(id);
        if (tl != null) {
            tl.setDebugOn(debugOn);
        } else {
            throw new IllegalArgumentException("Invalid listener : " + id);
        }
    }

    public void setSenderDebugOn(String id, boolean debugOn) {
        logger.info("Changing debug level of the sender with id : {}, into : {}, of the running instance", id,
            debugOn ? "on" : "off");
        TransportSender ts = senders.get(id);
        if (ts != null) {
            ts.setDebugOn(debugOn);
        } else {
            throw new IllegalArgumentException("Invalid sender : " + id);
        }
    }
}

