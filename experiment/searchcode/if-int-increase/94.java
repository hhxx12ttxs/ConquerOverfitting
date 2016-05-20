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

package org.adroitlogic.ultraesb.api.transport.tcp;

/**
 * TCP transport constants
 * NOTE: This is an abstract transport. Please inquire from AdroitLogic for further details on how concrete
 * transports maybe built over TCP/S
 *
 * @author asankha
 */
public class TCPConstants {

    private TCPConstants() {}

    // transport headers
    public static final String CLIENT_DN                    = "ultra.tcp.client_dn";
    public static final String CLIENT_CERTS                 = "ultra.tcp.client_certs";
    // session variable keys
    public static final String REQUEST_SIZE                 = "ultra.tcp.request_size";

    public static class ErrorCodes {

        public static final int LST_IO_ERR_SND              = 101000;
        public static final int LST_IO_ERR_RCV              = 101001; // io exception
        public static final int LST_PROTO_ERR_RCV           = 101002; // protocol violation
        public static final int LST_TIMEOUT                 = 101003; // listener connection timeout
        public static final int SND_IO_ERR_SND              = 101500;
        public static final int SND_IO_ERR_RCV              = 101501;
        public static final int SND_CONNECTION_FAILED       = 101503;
        public static final int SND_CONNECTION_TIMEOUT      = 101504;
        public static final int SND_CONNECTION_CLOSED       = 101505;
        public static final int SND_PROTOCOL_VIOLATION      = 101506;
        public static final int SND_CONNECT_CANCEL          = 101507;
        public static final int SND_CONNECT_TIMEOUT         = 101508;
        public static final int SND_CONNECT_FAILED          = 101509;
        public static final int SND_RESPONSE_REJECTED       = 101510;
    }

    public static class TuningParameters {

        /**
         * Defines the IO threads to be used for dedicated NIO operations.
         * Typically the number of CPU cores
         */
        public static final String SND_IO_THREADS           = "ultra.tcp.snd_io_threads";

        /**
         * Defines the IO threads to be used for dedicated NIO operations.
         * Typically the number of CPU cores
         */
        public static final String LST_IO_THREADS           = "ultra.tcp.lst_io_threads";

        /**
         * Defines the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds,
         * which is the timeout for waiting for data  or, put differently,
         * a maximum period inactivity between two consecutive data packets).
         * A timeout value of zero is interpreted as an infinite timeout.
         * <p>
         * This parameter expects a value of type {@link Integer}.
         * </p>
         * @see java.net.SocketOptions#SO_TIMEOUT
         */
        public static final String SO_TIMEOUT               = "ultra.tcp.socket_timeout";

        /**
         * Determines whether Nagle's algorithm is to be used. The Nagle's algorithm
         * tries to conserve bandwidth by minimizing the number of segments that are
         * sent. When applications wish to decrease network latency and increase
         * performance, they can disable Nagle's algorithm (that is enable
         * TCP_NODELAY). Data will be sent earlier, at the cost of an increase
         * in bandwidth consumption.
         * <p>
         * This parameter expects a value of type {@link Boolean}.
         * </p>
         * @see java.net.SocketOptions#TCP_NODELAY
         */
        public static final String TCP_NODELAY              = "ultra.tcp.tcp_nodelay";

        /**
         * Determines the size of the internal socket buffer used to buffer data
         * while receiving / transmitting HTTP messages.
         * <p>
         * This parameter expects a value of type {@link Integer}.
         * </p>
         */
        public static final String SOCKET_BUFFER_SIZE       = "ultra.tcp.socket_buffer_size";

        /**
         * Sets SO_LINGER with the specified linger time in seconds. The maximum
         * timeout value is platform specific. Value <code>0</code> implies that
         * the option is disabled. Value <code>-1</code> implies that the JRE
         * default is used. The setting only affects the socket close operation.
         * <p>
         * This parameter expects a value of type {@link Integer}.
         * </p>
         * @see java.net.SocketOptions#SO_LINGER
         */
        public static final String SO_LINGER                = "ultra.tcp.socket_linger";

        /**
         * Determines the timeout in milliseconds until a connection is established.
         * A timeout value of zero is interpreted as an infinite timeout.
         * <p>
         * This parameter expects a value of type {@link Integer}.
         * </p>
         */
        public static final String CONNECTION_TIMEOUT       = "ultra.tcp.connection_timeout";

        /**
         * Determines whether stale connection check is to be used. The stale
         * connection check can cause up to 30 millisecond overhead per request and
         * should be used only when appropriate. For performance critical
         * operations this check should be disabled.
         * <p>
         * This parameter expects a value of type {@link Boolean}.
         * </p>
         */
        public static final String STALE_CONNECTION_CHECK   = "ultra.tcp.stale_connection_check";
    }
}

