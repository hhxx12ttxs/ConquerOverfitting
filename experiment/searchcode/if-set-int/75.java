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

package org.adroitlogic.ultraesb.transport.http;

import org.adroitlogic.ultraesb.ServerManager;
import org.adroitlogic.ultraesb.Util;
import org.adroitlogic.ultraesb.api.FileCache;
import org.adroitlogic.ultraesb.api.MessageFile;
import org.adroitlogic.ultraesb.api.transport.http.HttpConstants;
import org.adroitlogic.ultraesb.core.ConfigurationImpl;
import org.adroitlogic.ultraesb.core.MessageImpl;
import org.adroitlogic.ultraesb.core.ProxyService;
import org.adroitlogic.ultraesb.jmx.view.TransportView;
import org.adroitlogic.ultraesb.transport.base.AbstractTransportListener;
import org.adroitlogic.ultraesb.transport.base.ManagementSupport;
import org.adroitlogic.ultraesb.transport.base.MetricsCollector;
import org.adroitlogic.ultraesb.transport.http.compress.ResponseGzipCompress;
import org.adroitlogic.ultraesb.transport.http.logging.LoggingUtils;
import org.adroitlogic.ultraesb.transport.http.util.ServerConnectionDebug;
import org.adroitlogic.ultraesb.transport.http.util.UltraResponseConnControl;
import org.apache.http.HttpException;
import org.apache.http.HttpInetConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultServerIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.ExceptionEvent;
import org.apache.http.impl.nio.reactor.IOSessionImpl;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.NHttpServerIOTarget;
import org.apache.http.nio.NHttpServiceHandler;
import org.apache.http.nio.protocol.AsyncNHttpServiceHandler;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.NHttpRequestHandler;
import org.apache.http.nio.protocol.NHttpRequestHandlerResolver;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>The HTTP transport listener</p>
 *
 * <p>Sets the following message properties in addition to setting the request URI as the destination</p>
 * <ul>
 * <li>HttpConstants.METHOD - the HTTP method i.e. POST, GET, HEAD, PUT, DELETE, OPTIONS, TRACE</li>
 * <li>HttpNIOListener.USERNAME - if HTTP basic or digest authentication was used, the user name</li>
 * <li>HttpNIOListener.USERROLES - if HTTP basic or digest authentication was used, the user roles</li>
 * <li>HttpConstants.QUERY_STRING - the HTTP query string</li>
 * </ul>
 * @author asankha
 */
public class HttpNIOListener extends AbstractTransportListener implements ManagementSupport {

    /** @exclude */
    public static final String REQUEST_FILE        = "ultra.http.request_file";
    /** @exclude */
    public static final String SERVICE_TRP_PROPS   = "ultra.http.service_trp_props";
    /** @exclude */
    public static final String SERVICE_ID          = "ultra.http.service_id";
    /** @exclude */
    public static final String REQUEST_MESSAGE     = "ultra.http.request_message";
    /** @exclude */
    public static final String RESPONSE_MESSAGE    = "ultra.http.response_message";
    /** @exclude */
    public static final String IOSESSION           = "ultra.internal.iosession";
    /** @exclude */
    public static final String CONNECTION_COUNTED  = "ultra.internal.conn_counted";

    private DefaultListeningIOReactor ioReactor;
    /** @exclude */
    protected int port = 80;
    private String bindAddress;
    private RequestHandler requestHandler = null;
    private final FileCache fileCache;
    private String defaultContextPath = "/service/";
    private List<RequestFilter> requestFilters;
    /** Is connection debugging enabled - false by default */
    private boolean enableConnectionDebug = false;
    /** The HTTP headers used during connection debug : or 'none' or 'all' as a single element */
    private Set<String> connectionDebugHeaders;

    private boolean replaceOriginServer = true;
    /** @exclude */
    protected String originatingTransport = "http";
    /** @exclude */
    protected boolean isHttps = false;
    private boolean zeroCopyEnabled = false;
    /** Is response compression (even if supported by the client) is disabled */
    private boolean noCompression = false;
    /** The minimum size of the response to trigger compression, if supported by the client - default 2K bytes */
    private long compressionMinSize = 2048L;
    /** Should the engine continue execution (true) or shutdown and restart on a runtime exception */
    private boolean continueOnRuntimeExceptions = true;
    /** Should the engine continue execution (true) or shutdown and restart on a checked exception */
    private boolean continueOnCheckedExceptions = false;
    /** Maximum number of auto restart attempts in a row until shutdown is initiated */
    private int autoRestartAttempts = 3;
    /** Pending auto restart attempts until shutdown is initiated */
    private int pendingRestartAttempts = autoRestartAttempts;
    /** Currently open connections */
    private final AtomicInteger openConnections = new AtomicInteger(0);
    /** Start to pause reactor at this limit - 4096 by default - in reality this limit maybe surpassed */
    private int stopNewConnectionsAt = 4096;
    /** The number of connections at which normal operations should resume - default 3072 */
    private int resumeNewConnectionsAt = 3073;
    /** Maximum payload size to accept - 1M by default*/
    private long maxPayloadSize = 1048576L;
    /** Keep-alive persitant connections with clients */
    private final Map<InetAddress, Set<NHttpConnection>> connections = new HashMap<InetAddress, Set<NHttpConnection>>();

    /**
     * The HTTP transport listen port - default is 80
     * @param port HTTP listen port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * The bind address to use, on a multiple network interface node
     * @param bindAddress bind address to listen
     */
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * One or more request filters - to support ?wsdl, ?xsd etc on service endpoints
     * @see RequestFilter
     * @param requestFilters a list of service filters to attach to this listener
     */
    public void setRequestFilters(List<RequestFilter> requestFilters) {
        this.requestFilters = requestFilters;
    }

    /**
     * Sets the list of HTTP headers to be dumped on a connection failure, when connection debugging is enabled
     * @param connectionDebugHeaders a set of HTTP header names, or a single entry 'none' or 'all'
     */
    public void setConnectionDebugHeaders(Set<String> connectionDebugHeaders) {
        this.connectionDebugHeaders = connectionDebugHeaders;
    }

    /**
     * Replace the origin server header for proxied requests, to UltraESB
     * @param replaceOriginServer false disables the replacing (Default is true)
     */
    public void setReplaceOriginServer(boolean replaceOriginServer) {
        this.replaceOriginServer = replaceOriginServer;
    }

    /**
     * Debug connection information on errors
     * @param enableConnectionDebug enable when true
     */
    public void setEnableConnectionDebug(boolean enableConnectionDebug) {
        this.enableConnectionDebug = enableConnectionDebug;
    }

    /**
     * Use Zero-Copy when reading requests
     * @param zeroCopyEnabled true uses Zero-Copy reads (default)
     */
    public void setZeroCopyEnabled(boolean zeroCopyEnabled) {
        this.zeroCopyEnabled = zeroCopyEnabled;
    }

    /**
     * Set the upper bound on the open connections at a given time, after which the transport will reject new connections
     * @param stopNewConnectionsAt maximum open connections
     */
    public void setStopNewConnectionsAt(int stopNewConnectionsAt) {
        this.stopNewConnectionsAt = stopNewConnectionsAt;
    }

    /**
     * Bound at which to resume normal operations from maintenance mode when open connections increased
     * @param resumeNewConnectionsAt bound at which to resume normal operations after hitting stopNewConnectionsAt
     */
    public void setResumeNewConnectionsAt(int resumeNewConnectionsAt) {
        this.resumeNewConnectionsAt = resumeNewConnectionsAt;
    }

    /**
     * The maximum payload size to accept - defaults to 1M
     * @param maxPayloadSize maximum payload size to accept
     */
    public void setMaxPayloadSize(long maxPayloadSize) {
        this.maxPayloadSize = maxPayloadSize;
    }

    /**
     * Default constructor, with a reference to the FileCache to use
     * @param fileCache the file cache to use
     */
    public HttpNIOListener(FileCache fileCache) {
        this.fileCache = fileCache;
    }

    /** @exclude */
    public String getOriginatingTransportName() {
        return originatingTransport;
    }

    private int getProperty(String name, int def) {
        if (properties == null) return def;
        String val = properties.get(name);
        if (val != null && Integer.valueOf(val) > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using NIO HTTP/S tuning parameter : " + name + " = " + val);
            }
            return Integer.valueOf(val);
        }
        return def;
    }

    private boolean getProperty(String name, boolean def) {
        if (properties == null) return def;
        String val = properties.get(name);

        if (val != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using NIO HTTP/S tuning parameter : " + name + " = " + val);
            }
            return Boolean.parseBoolean(val);
        }
        return def;
    }

    /** @exclude */
    public void init(String serverName) {
        requestHandler = new RequestHandler(metrics, zeroCopyEnabled, maxPayloadSize,
            getOriginatingTransportName(), fileCache, defaultContextPath, requestFilters,
            replaceOriginServer, enableConnectionDebug, connectionDebugHeaders);
    }
    
    /** @exclude */
    public void start() {

        HttpParams params = new BasicHttpParams();
        params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                getProperty(CoreConnectionPNames.SO_TIMEOUT, 120000))
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
                getProperty(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8*1024))
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK,
                getProperty(CoreConnectionPNames.STALE_CONNECTION_CHECK, false))
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY,
                getProperty(CoreConnectionPNames.TCP_NODELAY, true));
        if (replaceOriginServer) {
            params.setParameter(CoreProtocolPNames.ORIGIN_SERVER, ConfigurationImpl.PRODUCT);
        }

        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        if (!noCompression) {
            httpproc.addInterceptor(new ResponseGzipCompress(fileCache, zeroCopyEnabled, compressionMinSize));
        }
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new UltraResponseConnControl());

        ServerHandler serviceHandler = new ServerHandler(
            httpproc,
            new DefaultHttpResponseFactory(),
            new DefaultConnectionReuseStrategy(),
            params);

        serviceHandler.setHandlerResolver(new NHttpRequestHandlerResolver() {
            public NHttpRequestHandler lookup(String requestURI) {
                return requestHandler;
            }
        });

        // reset open connections
        openConnections.set(0);
        // Provide an event logger
        serviceHandler.setEventListener(
            new EventLogger(metrics, this, openConnections,
                stopNewConnectionsAt, resumeNewConnectionsAt, enableConnectionDebug, connectionDebugHeaders));

        final IOEventDispatch ioEventDispatch = getIOEventDispatch(serviceHandler, params);
        try {
            ioReactor = new DefaultListeningIOReactor(
                getProperty("http.lst_io_threads", Runtime.getRuntime().availableProcessors()),
                new DefaultThreadFactory(), params);

            ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
                public boolean handle(IOException ioException) {
                    logger.warn("System may be unstable: IOReactor encountered a checked exception : " +
                        ioException.getMessage(), ioException);
                    if (ioException instanceof BindException) {
                        return false;
                    }
                    return continueOnCheckedExceptions;
                }

                public boolean handle(RuntimeException runtimeException) {
                    logger.warn("System may be unstable: IOReactor encountered a runtime exception : " +
                        runtimeException.getMessage(), runtimeException);
                    if (runtimeException instanceof UnsupportedOperationException) {
                        // Unsupported operations considered OK to ignore
                        return true;
                    }
                    return continueOnRuntimeExceptions;
                }
            });
        } catch (IOReactorException e) {
            logger.error("Error starting IOReactor for the " + (isHttps ? "HTTPS" : "HTTP") + " Listener : " + id, e);
            return;
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                startIOReactor(ioEventDispatch);
            }
        }, (isHttps ? "HttpsNIOListener" : "HttpNIOListener") + "-" + id);
        t.start();
    }

    private void startIOReactor(final IOEventDispatch ioEventDispatch) {
        logger.info("Starting NIO Listener : " + id + " on port : " + port + " ...");
        boolean attemptAutoRestart = true;

        try {
            if (bindAddress == null) {
                ioReactor.listen(new InetSocketAddress(port));
            } else {
                ioReactor.listen(new InetSocketAddress(InetAddress.getByName(bindAddress), port));
            }
            state = State.STARTED;
            ioReactor.execute(ioEventDispatch);
            attemptAutoRestart = false;
            pendingRestartAttempts = autoRestartAttempts;

        } catch (InterruptedIOException e) {
            logger.error("NIO Listener : " + id + " on port : " + port + " was interrupted", e);
        } catch (IOException e) {
            logger.error("NIO Listener : " + id + " on port : " + port +
                " encountered an I/O error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("NIO Listener : " + id + " on port : " + port +
                " encountered an unexpected error: " + e.getMessage(), e);
        }
        logger.info("NIO Listener : " + id + " on port : " + port + " stopped");

        state = State.STOPPED;
        if (attemptAutoRestart && pendingRestartAttempts-- > 0) {
            logger.info("Listening IOReactor encountered a fatal exception. Attempting a re-start of the reactor");

            if (ioReactor.getAuditLog() != null) {
                logger.error("Possible causes leading to the Listening IOReactor shutdown are : ");
                for (ExceptionEvent e : ioReactor.getAuditLog()) {
                    Throwable t = e.getCause();
                    logger.error("At time : " + e.getTimestamp() + " Error : " + t.getMessage(), t);
                }
            }

            try {
                logger.info("Attempt shutdown of existing IOReactor..");
                ioReactor.shutdown(10000);
            } catch (IOException ignore) {}

            logger.info("Restarting Listening IOReactor in 10 seconds.. ");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignore) {}

            start();
        } else if (attemptAutoRestart) {
            logger.error("FATAL - Unable to restart NIO listener - requesting server shutdown");
            ServerManager.getInstance().shutdown();
            ServerManager.getInstance().markAsFailed();
        }
    }

    /** @exclude */
    public void pause() {
        if (state != State.STARTED) return;
        try {
            ioReactor.pause();
            state = State.PAUSED;

            // forcefully close idle connections and mark others for discarding after use
            synchronized (connections) {
                for (Set<NHttpConnection> set : connections.values()) {
                    for (NHttpConnection conn: set) {
                        if (conn.getContext().getAttribute(HttpConstants.CONNECTION_IN_USE) == Boolean.TRUE) {
                            conn.getContext().setAttribute(HttpConstants.DISCARD_AFTER_USE, Boolean.TRUE);
                        } else {
                            try { conn.close(); } catch (Exception ignore) {}
                        }
                    }
                }
            }

            logger.info((isHttps ? "HTTPS" : "HTTP") + " Listener " + id + " paused");
        } catch (IOException e) {
            logger.error("Error pausing IOReactor for the " + (isHttps ? "HTTPS" : "HTTP") + " Listener : " + id, e);
        }
    }

    /** @exclude */
    public void resume() {
        if (state != State.PAUSED && state != State.DISABLED) return;
        try {
            ioReactor.resume();
            state = State.STARTED;
            logger.info((isHttps ? "HTTPS" : "HTTP") + " Listener " + id + " resumed");
        } catch (IOException e) {
            logger.error("Error resuming IOReactor for the " + (isHttps ? "HTTPS" : "HTTP") + " Listener : " + id, e);
        }
    }

    /** @exclude */
    public void stop() {
        if (state == State.STOPPED) return;
        try {
            ioReactor.shutdown();
            state = State.STOPPED;
            logger.info((isHttps ? "HTTPS" : "HTTP") + " Listener " + id + " stopped");

            String jmxName = "org.adroitlogic.ultraesb:Type=Transport,ConnectorName=listener-" + id;
            Util.unregisterMBean(jmxName);
        } catch (IOException e) {
            logger.error("Error stopping IOReactor for the " + (isHttps ? "HTTPS" : "HTTP") + " Listener : " + id, e);
        }
    }

    /** @exclude */
    public void maintenanceShutdown(long millis) {
        if (state != State.STARTED) return;
        try {
            long start = System.currentTimeMillis();
            ioReactor.pause();
            state = State.PAUSED;
            ioReactor.shutdown(millis);
            state = State.STOPPED;
            logger.info((isHttps ? "HTTPS" : "HTTP") + " Listener " + id + " shutdown in" +
                (System.currentTimeMillis() - start) / 1000 + "s");
        } catch (IOException e) {
            logger.error("Error shutting down IOReactor for the " + (isHttps ? "HTTPS" : "HTTP") + " Listener : " + id, e);
        }
    }

    /** @exclude */
    public void registerProxyService(ProxyService ps, String uriPattern, Map<String, String> properties) {
        requestHandler.registerProxyService(ps, uriPattern, properties);
    }

    /** @exclude */
    public void unregisterProxyService(ProxyService ps) {
        requestHandler.unregisterProxyService(ps);
    }

    static class EventLogger implements EventListener {

        private static final Logger logger = LoggerFactory.getLogger(EventLogger.class);
        private final MetricsCollector metrics;
        private final AtomicInteger openConnections;
        private final int stopNewConnectionsAt;
        private final int resumeNewConnectionsAt;
        private final HttpNIOListener listener;
        private final Map<InetAddress, Set<NHttpConnection>> connections;
        private final boolean enableConnectionDebug;
        private final Set<String> connectionDebugHeaders;

        EventLogger(MetricsCollector metrics, HttpNIOListener listener, AtomicInteger openConnections,
            int stopNewConnectionsAt, int resumeNewConnectionsAt, boolean enableConnectionDebug, Set<String> connectionDebugHeaders) {
            this.metrics = metrics;
            this.listener = listener;
            this.connections = listener.connections;
            this.openConnections = openConnections;
            this.stopNewConnectionsAt = stopNewConnectionsAt;
            this.resumeNewConnectionsAt = resumeNewConnectionsAt;
            this.enableConnectionDebug = enableConnectionDebug;
            this.connectionDebugHeaders = connectionDebugHeaders;
        }

        public void connectionOpen(final NHttpConnection conn) {

            // count new connection
            final int openConnections = this.openConnections.incrementAndGet();
            logger.debug("Connection open: {} Total : {}", conn, openConnections);

            if (openConnections >= stopNewConnectionsAt && State.STARTED == listener.state) {
                synchronized (this) {
                    if (State.STARTED == listener.state) {
                        listener.pause();
                        listener.setState(State.DISABLED);
                        logger.warn("Enter maintenance mode as open connections reached : {}", openConnections);
                    }
                }
            }
            conn.getContext().setAttribute(HttpConstants.CONNECTION_EST_TIME_MILLIS, System.currentTimeMillis());
            conn.getContext().setAttribute(CONNECTION_COUNTED, Boolean.TRUE);

            if (conn instanceof HttpInetConnection) {
                HttpInetConnection inetConn = (HttpInetConnection) conn;

                InetAddress remoteAddr = inetConn.getRemoteAddress();
                if (remoteAddr != null) {
                    conn.getContext().setAttribute(HttpConstants.REMOTE_ADDRESS,
                        remoteAddr.getHostAddress() != null ? remoteAddr.getHostAddress() : remoteAddr);
                }
                conn.getContext().setAttribute(HttpConstants.REMOTE_PORT, inetConn.getRemotePort());

                InetAddress localAddr = inetConn.getLocalAddress();
                if (localAddr != null) {
                    conn.getContext().setAttribute(HttpConstants.LOCAL_ADDRESS,
                        localAddr.getHostAddress() != null ? localAddr.getHostAddress() : localAddr);
                }
                conn.getContext().setAttribute(HttpConstants.LOCAL_PORT, inetConn.getLocalPort());

                // add connection to list of open connections
                synchronized (connections) {
                    Set<NHttpConnection> set = connections.get(remoteAddr);
                    if (set == null) {
                        set = new HashSet<NHttpConnection>();
                        connections.put(remoteAddr, set);
                    }
                    set.add(conn);
                }
            }
        }

        public void connectionTimeout(final NHttpConnection conn) {
            logger.debug("Connection timeout: {}", conn);
            decrementCountIfApplicable(conn);

            if (conn.getContext().removeAttribute(HttpConstants.CONNECTION_IN_USE) != null) {
                logger.debug("Connection timed out: {} socket timeout was : {}", conn, conn.getSocketTimeout());
                notifyFailure(conn, HttpConstants.ErrorCodes.LST_TIMEOUT);
            } else if (enableConnectionDebug && conn.getMetrics().getReceivedBytesCount() > 0) {
                ServerConnectionDebug.dumpPrematureRequestInfo(
                    conn, connectionDebugHeaders, HttpConstants.ErrorCodes.LST_TIMEOUT);
            }
            releasePartialReads(conn);
        }

        public void connectionClosed(final NHttpConnection conn) {
            logger.debug("Connection closed: {}", conn);
            decrementCountIfApplicable(conn);

            if (conn.getContext().removeAttribute(HttpConstants.CONNECTION_IN_USE) != null) {
                notifyFailure(conn, HttpConstants.ErrorCodes.LST_CONNECTION_CLOSED);
            } else if (enableConnectionDebug && conn.getMetrics().getReceivedBytesCount() > 0) {
                ServerConnectionDebug.dumpPrematureRequestInfo(
                    conn, connectionDebugHeaders, HttpConstants.ErrorCodes.LST_CONNECTION_CLOSED);
            }
            releasePartialReads(conn);
        }

        public void fatalIOException(final IOException ex, final NHttpConnection conn) {
            decrementCountIfApplicable(conn);
            if (conn.getContext().removeAttribute(HttpConstants.CONNECTION_IN_USE) != null) {
                logger.debug("I/O error: {} - {}", conn, ex.getMessage());
                notifyFailure(conn,
                    conn.getContext().getAttribute(HttpConstants.RECEIVE_COMPLETE) != null
                        ? HttpConstants.ErrorCodes.LST_IO_ERR_SND : HttpConstants.ErrorCodes.LST_IO_ERROR);
            } else if (enableConnectionDebug && conn.getMetrics().getReceivedBytesCount() > 0) {
                logger.debug("Connection Exception (possibly reset by peer after timeout) : {}", conn);
                ServerConnectionDebug.dumpPrematureRequestInfo(
                    conn, connectionDebugHeaders, HttpConstants.ErrorCodes.LST_IO_ERROR);
            }
            releasePartialReads(conn);
        }

        public void fatalProtocolException(final HttpException ex, final NHttpConnection conn) {
            decrementCountIfApplicable(conn);
            if (logger.isDebugEnabled()) {
                logger.warn("HTTP error: {}", conn, ex);
            } else {
                logger.warn("HTTP error: {} - {}", conn, ex.getMessage());
            }
            notifyFailure(conn, HttpConstants.ErrorCodes.LST_HTTP_ERROR);
            releasePartialReads(conn);
        }

        private void notifyFailure(NHttpConnection conn, int errorCode) {
            boolean requestMessage = false;
            MessageImpl message = (MessageImpl) conn.getContext().getAttribute(HttpNIOListener.RESPONSE_MESSAGE);
            if (message == null) {
                requestMessage = true;
                message = (MessageImpl) conn.getContext().getAttribute(HttpNIOListener.REQUEST_MESSAGE);
            }

            // mark end processing of endpoint
            if (message != null) {

                // debug session information
                IOSession ioSession = (IOSession) conn.getContext().getAttribute(IOSESSION);
                if (ioSession != null && ioSession instanceof IOSessionImpl) {
                    ServerConnectionDebug.recordFailure(message, (IOSessionImpl) ioSession);
                }

                ServerConnectionDebug.recordFailure(message, errorCode);
                ServerConnectionDebug.dump(message);
                conn.getMetrics().reset();

                if (!requestMessage) {
                    message.getLastEndpoint().notifyFailure(message, errorCode,
                        conn.getContext().getAttribute(HttpConstants.RECEIVE_COMPLETE) == null);
                }
            } else if (enableConnectionDebug) {
                ServerConnectionDebug.dumpPrematureRequestInfo(conn, connectionDebugHeaders, errorCode);
            }

            if (conn.getContext().getAttribute(HttpNIOListener.RESPONSE_MESSAGE) != null) {
                metrics.reportSendFaultWithErrorCode(errorCode);
            } else {
                metrics.reportReceiveFaultWithErrorCode(errorCode);
            }
        }

        private void releasePartialReads(NHttpConnection conn) {
            // if any partial content has been read, release the associated file
            if (conn.getContext().getAttribute(HttpConstants.RECEIVE_COMPLETE) == null) {
                MessageFile mf = (MessageFile) conn.getContext().removeAttribute(REQUEST_FILE);
                if (mf != null) {
                    mf.release();
                }
            }
        }

        private void decrementCountIfApplicable(NHttpConnection conn) {

            if (conn.getContext().removeAttribute(CONNECTION_COUNTED) != null) {
                final int openConnections = this.openConnections.decrementAndGet();

                // remove reference to open connection
                if (conn instanceof HttpInetConnection) {
                    final HttpInetConnection inetConn = (HttpInetConnection) conn;
                    final InetAddress remoteAddr = inetConn.getRemoteAddress();

                    synchronized (connections) {
                        Set<NHttpConnection> set = connections.get(remoteAddr);
                        if (set != null) {
                            set.remove(conn);
                        }
                    }
                }

                if (openConnections < resumeNewConnectionsAt && listener.state == State.DISABLED) {
                    synchronized (this) {
                        if (listener.state == State.DISABLED) {
                            logger.info("Resuming normal operations as open connections reached : {}", openConnections);
                            listener.resume();
                        }
                    }
                }
            }
        }
    }

    /** @exclude */
    protected IOEventDispatch getIOEventDispatch(AsyncNHttpServiceHandler serviceHandler, HttpParams params) {
        return new DirectServerIOEventDispatch(serviceHandler, params);
    }

    static class DirectServerIOEventDispatch extends DefaultServerIOEventDispatch {

        public DirectServerIOEventDispatch(NHttpServiceHandler handler, HttpParams params) {
            super(LoggingUtils.decorate(handler), params);
        }

        @Override
        protected ByteBufferAllocator createByteBufferAllocator() {
            return new HeapByteBufferAllocator();
        }

        @Override
        protected NHttpServerIOTarget createConnection(IOSession session) {
            session = LoggingUtils.decorate(session, "server");
            return LoggingUtils.createServerConnection(
                    session,
                    createHttpRequestFactory(),
                    this.allocator,
                    this.params);
        }

        @Override
        public void disconnected(IOSession session) {
            NHttpServerIOTarget conn = (NHttpServerIOTarget) session.getAttribute(ExecutionContext.HTTP_CONNECTION);
            if (conn != null) {
                conn.getContext().setAttribute(IOSESSION, session);
                this.handler.closed(conn);
            }
        }

        @Override
        public void timeout(IOSession session) {
            NHttpServerIOTarget conn = (NHttpServerIOTarget) session.getAttribute(ExecutionContext.HTTP_CONNECTION);
            if (conn == null) {
                throw new IllegalStateException("HTTP connection is null");
            }
            conn.getContext().setAttribute(IOSESSION, session);
            this.handler.timeout(conn);
        }

        @Override
        public void connected(IOSession session) {
            NHttpServerIOTarget conn = createConnection(session);
            session.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
            conn.getContext().setAttribute(IOSESSION, session);
            this.handler.connected(conn);
        }
    }

    /**
     * Set the default context path for services exposed over HTTP
     * @param defaultContextPath the context path to use (default '/service/')
     */
    public void setDefaultContextPath(String defaultContextPath) {
        this.defaultContextPath = defaultContextPath;
    }

    /**
     * Turn off use of Gzip compression even if the request contains an Accept-Encoding header
     * @param noCompression true fully disables all use of response compression
     */
    public void setNoCompression(boolean noCompression) {
        this.noCompression = noCompression;
    }

    /**
     * Set the minimum size of the response entity to enable compression when supported by the client (default 2Kbytes)
     * @param compressionMinSize size in bytes
     */
    public void setCompressionMinSize(long compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    /**
     * Control behavior of engine on runtime exceptions (WARNING: It maybe dangerous to ignore runtime exceptions)
     * @param continueOnRuntimeExceptions if true, ignores runtime exceptions
     */
    public void setContinueOnRuntimeExceptions(boolean continueOnRuntimeExceptions) {
        this.continueOnRuntimeExceptions = continueOnRuntimeExceptions;
    }

    /**
     * Control behavior of engine on checked exceptions
     * @param continueOnCheckedExceptions if true, ignores checked exceptions
     */
    public void setContinueOnCheckedExceptions(boolean continueOnCheckedExceptions) {
        this.continueOnCheckedExceptions = continueOnCheckedExceptions;
    }

    /**
     * Extension point for additional attributes
     */
    public void toDetailedViewExt(TransportView view) {
        Map<String, Integer> openConnectionStats = new HashMap<String, Integer>();
        for (Map.Entry<InetAddress, Set<NHttpConnection>> entry : connections.entrySet()) {
            openConnectionStats.put(entry.getKey().getHostAddress(), entry.getValue().size());
        }
        view.setOpenConnectionStats(openConnectionStats);
        view.setOpenConnectionsCount(openConnections.get());
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private static volatile int COUNT = 0;

        public Thread newThread(final Runnable r) {
            return new Thread(r, "L-I/O-dispatcher-" + (++COUNT));
        }
    }
}

