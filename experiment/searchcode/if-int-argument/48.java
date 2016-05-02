package com.mtoolkit.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import com.mlogger.Loggers;
import com.mtoolkit.util.CharsetUtil;

/**
 * Application service monitor.
 * For start the application service and monitor thread, we can specified
 * the execute argument like "-c start" or "-c stop -p 8611". 
 * Similarly, the stop command argument like "-c stop" or "-c stop -p 8611". 
 * 
 * @author 	<a href="mailto:xishizhang@gmail.com">ZhangShixi</a>
 * @version 1.0, 2/1/2012
 * @since 	JDK1.5
 */
public class ServiceMonitor {
	
	/** target service */
	private Service _service;
	/** execute arguments */
	private String[] _arguments;
	/** execute command */
	private String _command;
	/** monitor port */
	private int _monitorPort = DEFAULT_PARAM_MONITOR_PORT;
	
	/** boot command argument name */
	public static final String CMD_PARAM_BOOT_COMMOND = "-c";
	/** monitor port argument name */
	public static final String CMD_PARAM_MONITOR_PORT = "-p";

	/** start command value */
	public static final String CMD_BOOT_SERVICE_START = "start";
	/** stop command value */
	public static final String CMD_BOOT_SERVICE_STOP  = "stop";
	
	/** default monitor thread listening port */
	public static final int DEFAULT_PARAM_MONITOR_PORT 	   = 8611;
	/** default stop command timeout value */
	public static final int DEFAULT_STOP_COMMOND_TIMEOUT   = 1000;
	/** local host address */
	public static final String LOCAL_HOST_IP_ADDRESS 	   = "127.0.0.1";
	/** service monitor thread name */
	public static final String SERVICE_MONITOR_THREAD_NAME = "ServiceMonitor";
	
	/** logger */
	private static final Loggers LOGGER = Loggers.getLoggers(ServiceMonitor.class);
	
	// ---- constructors ---------------------------------------------------------
	/**
	 * Creates a ServiceMonitor instance with target service and execute arguments.
	 * 
	 * @param  service target service.
	 * @param  arguments execute arguments. 
	 * 
	 * @throws NullPointerException 
	 * 		   if {@code service} or {@code arguments} is null.
	 * @throws IllegalArgumentException 
	 * 		   if command argument is null or not one of the "start" and "stop".
	 */
	public ServiceMonitor(Service service, String[] arguments) {
		if (service == null) {
			throw new NullPointerException("service");
		}
		parseArgs(arguments);

		_service   = service;
		_arguments = arguments;
	}
	
	// ---- public methods -------------------------------------------------------
	/**
	 * Executes command to start/stop the target service and monitor thread. 
	 * 
	 * @throws Exception 
	 * 		   if any exceptions occurs whiling start/stop the target service 
	 * 		   and service monitor thread.
	 */
	public void startup() throws Exception {
		if (CMD_BOOT_SERVICE_START.equals(_command)) {
			startService(_arguments);
			startMonitor(_monitorPort);
		} else if (CMD_BOOT_SERVICE_STOP.equals(_command)) {
			stopMonitor(_monitorPort);
		}
		
		LOGGER.info("{0} {1}ed with arguments {2}.", 
				_service.getClass().getName(), _command, Arrays.toString(_arguments));
	}

	/**
	 * Shutdown the target service and monitor thread.
	 * 
	 * @throws Exception  
	 * 		   if any exceptions occurs whiling stop the target service 
	 * 		   and service monitor thread.
	 */
	public void shutdown() throws Exception {
		stopMonitor(_monitorPort);
		stopService();
		
		LOGGER.info("{0} stoped.", _service.getClass().getName());
	}
	
	// ---- private methods ------------------------------------------------------
	private void parseArgs(String[] arguments) {
		if (arguments == null) {
			throw new NullPointerException("arguments");
		}
		
		String argument = null;
		String command  = null;
		String portStr 	= null;
		
		for (int i = 0; i< arguments.length - 1; i++) {
			argument = arguments[i];
			if (CMD_PARAM_BOOT_COMMOND.equals(argument)) {
				command = arguments[++i];
			} else if (CMD_PARAM_MONITOR_PORT.equals(argument)) {
				portStr = arguments[++i];
			}
		}
		
		if (command == null || !(CMD_BOOT_SERVICE_START.equals(command) || 
								 CMD_BOOT_SERVICE_STOP.equals(command))) {
			throw new IllegalArgumentException("Illegal commond: " + command);
		} else {
			_command = command;
		}
		if (portStr != null) {
			_monitorPort = Integer.parseInt(portStr);
		}
	}
	
	private void startService(String[] args) throws Exception {
		_service.startup(args);
		
		LOGGER.debug("{0} started.", _service.getClass().getName());
	}

	private void stopService() throws Exception {
		_service.shutdown();
		
		LOGGER.debug("{0} stoped.", _service.getClass().getName());
	}

	private void startMonitor(int port) throws IOException {
		new Thread(new MonitorThread(port, _service), SERVICE_MONITOR_THREAD_NAME).start();
		
		LOGGER.debug("Service monitor thread started listening on port {0}.", port);
	}
	
	private void stopMonitor(int port) throws IOException {
		Socket socket = new Socket();
		socket.setReuseAddress(true);
		socket.connect(new InetSocketAddress(LOCAL_HOST_IP_ADDRESS, port));
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), CharsetUtil.UTF_8));
			writer.write(CMD_BOOT_SERVICE_STOP);
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
		}
		
		LOGGER.debug("Service monitor thread stoped.");
	}

	// ---- inner classes --------------------------------------------------------
	/**
	 * Service monitor thread.
	 */
	private class MonitorThread implements Runnable {

	    private Service _runningService;
		private ServerSocket _serverSocket;;
		
		public MonitorThread(int port, Service runningService) throws IOException {
		    _runningService = runningService;
			_serverSocket = new ServerSocket();
			_serverSocket.setReuseAddress(true);
			_serverSocket.bind(new InetSocketAddress(LOCAL_HOST_IP_ADDRESS, port));
		}
		
		@Override
		public void run() {
			Socket socket = null;
			boolean stop = false;
			
			while(true) {
				try {
					socket = _serverSocket.accept();
					
					if(stop = isStop(socket)) {
					    try {
					        _runningService.shutdown();
                        } catch (Exception e) {
                            LOGGER.error("Shutdown service [{0}] error", e, _runningService);
                        }
						break;
					}
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				} finally {
					try {
						if (socket != null) {
							socket.close();
						}
						if (stop) {
							_serverSocket.close();
						}
					} catch (IOException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		}
		
		private boolean isStop(Socket socket) throws IOException {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream(), CharsetUtil.UTF_8));
				String command = reader.readLine();

				return CMD_BOOT_SERVICE_STOP.equals(command);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}
	}
	
}

