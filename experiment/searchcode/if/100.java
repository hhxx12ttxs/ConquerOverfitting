package logger.sd.server;

import java.io.DataOutputStream;
import java.net.Socket;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>ConnectionTest</code> contains tests for the class
 * <code>{@link Connection}</code>.
 * 
 * @generatedBy CodePro at 12/10/10 10:40
 * @author Wendell
 * @version $Revision: 1.0 $
 */
public class ConnectionTest {

	private static final ServerManager serverManager;
	private static final ServerConfiguration config;

	private static LoggingServer server;
	private Socket cliente;
	private static String host;
	private static int port;

	static {
		config = new ServerConfiguration();
		config.setPort("7080");
		serverManager = new ServerManager(config);
		serverManager.createLogServer();
		server = serverManager.getServer();
		host = "localhost";
		port = config.getPort();
		serverManager.startServer();
	}

	/**
	 * Run the Connection(Socket) constructor test.
	 * 
	 * @throws Exception0
	 */
	@Test
	public void testConnection() throws Exception {
		System.out.println("Testando new Connection(clienteSocket, server)");

		cliente = new Socket(host, port);
		Connection result = new Connection(cliente, server);

		assertNotNull(result);
	}

	/**
	 * Run the void run() method test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRun() throws Exception {
		System.out.println("Testando Conncetion.run()");

		cliente = new Socket(host, port);

		if (cliente != null && cliente.isConnected()) {
			final Connection fixture = new Connection(cliente, server);

			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					fixture.run();
				}
			});

			t.start();

			DataOutputStream saidaParaServidor = new DataOutputStream(
					cliente.getOutputStream());

			saidaParaServidor.writeBytes("Teste\n");

		}
	}

	/**
	 * Launch the test.
	 * 
	 * @param args
	 *            the command line arguments
	 * 
	 * @generatedBy CodePro at 12/10/10 10:40
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ConnectionTest.class);
	}
}
