package master;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Worker {
	private Socket connection;
	private String address;
	private int port;
	
	private int load;
	private long lastChange;

	private BufferedWriter out;
	private BufferedReader in;
	
	public Worker(Socket connection, String address, int port) throws IOException {
		this.connection = connection;
		this.address = address;
		this.port = port;
		this.out = new BufferedWriter(new OutputStreamWriter(this.connection.getOutputStream()));
		this.in = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
	}
	
	public boolean isAlive() {
		return connection.isConnected() && !connection.isClosed();
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getRootUrl() {
		return "http://" + this.address + ":" + this.port + "/";
	}
	
	public int getLoad() {
		return this.load;
	}
	
	public long getLastChange() {
		return this.lastChange;
	}
	
	public void refreshLoad() throws IOException {
		this.out.write("load\n");
		this.out.flush();
		String result = this.in.readLine();
		if(result == null) {
			throw new IOException("Connection lost");
		}
		try {
			int prevload = load;
			this.load = Integer.parseInt(result);
			if (prevload != this.load)
				this.lastChange = System.currentTimeMillis();
		} catch(NumberFormatException nfx) {
			System.out.println("Got malformed load");
		}
	}
	public boolean shutdown() throws IOException {
		this.out.write("shutdown\n");
		this.out.flush();
		String result = this.in.readLine();
		if(result == null) {
			throw new IOException("Connection lost");
		}
		if("denied".equals(result))
			return false;
		else if("failed".equals(result))
			return false;
		else if("ok".equals(result))
			return true;
		else
			throw new IOException("Bad shutdown response: " + result);
	}
}

