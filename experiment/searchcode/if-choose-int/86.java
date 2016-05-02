package reuo.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import reuo.AccountResponse;
import reuo.client.Server.Shard;

/**
 * An encapsulation class for PacketHandlers that handle accounts.
 * @author Kristopher Ives
 */
public class AccountHandler implements Iterable<PacketHandler>{
	Client cl;
	ArrayList<PacketHandler> handlers = new ArrayList<PacketHandler>();
	
	/**
	 * Instantiates a AccountHandler for a Client
	 * @param cl the client
	 */
	public AccountHandler(Client cl){
		this.cl = cl;
		
		/* Add the supported PacketHandlers */
		handlers.add(new ShardListHandler());
		//handlers.add(new AccountFailedHandler());
		handlers.add(new RedirectHandler());
	}
	
	/**
	 * Gets an iteration of all the PacketHandlers implemented
	 */
	public Iterator<PacketHandler> iterator(){
		return(handlers.iterator());
	}
	
	/**
	 * A Request to login an account on the login server. The AccountResponse enumeration
	 * provides the different responses this may have.
	 * @author Kristopher Ives
	 */
	public static class LoginRequest extends Request<AccountResponse>{
		Client cl = null;
		AccountResponse response = null;
		ByteBuffer buffer = ByteBuffer.allocate(1 + 30 + 30 + 1);
		
		/**
		 * Instantiates a LoginRequest for an account using a client
		 * @param cl the client
		 * @param user the account username
		 * @param pass the account password
		 * @throws IllegalArgumentException if the username or password are longer than
		 * 30 characters (the maximum) or the character encoding is invalid
		 */
		public LoginRequest(Client cl, String user, String pass) throws IllegalArgumentException{
			super(AccountResponse.class);
			
			this.cl = cl;
			
			if(user.length() > 30 || pass.length() > 30){
				throw(new IllegalArgumentException("Account username or password is too long"));
			}
			
			buffer.clear();
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			
			/* The packet header */
			buffer.put((byte)0x80);
			
			try{
				buffer.put(user.getBytes("ASCII"));
				buffer.position(1 + 30);
				buffer.put(pass.getBytes("ASCII"));
			}catch(UnsupportedEncodingException e){
				throw(new IllegalArgumentException("Account username or password contain unsupported characters"));
			}
			
			/* This is a terminator (although it's useless) */
			buffer.put((byte)0x00);
		}
		
		public ByteBuffer getData(){
			return(buffer);
		}
		
		@Override
		public AccountResponse getResponse(){
			return(response);
		}
		
		@Override
		public boolean isCancelled(){
			return(buffer == null);
		}
		
		@Override
		public boolean isResolved(){
			return(response != null);
		}
		
		@Override
		public void cancel() throws IllegalStateException{
			if(!isOpen()){
				throw(notOpenException);
			}
			
			/* Mark the request as cancelled and inform the client of the
			 * cancel */
			cl.cancelSend(this, buffer);
			buffer = null;
		}
		
		@Override
		public void resolve(AccountResponse response) throws IllegalStateException{
			if(!isOpen()){
				throw(notOpenException);
			}
			
			this.response = response;
			
			for(RequestListener listener : cl.getListeners(RequestListener.class)){
				listener.requestResolved(cl, this);
			}
			
			cl.removeRequest(this);
			
			/* If the login worked we can now create a shard request */
			switch(response){
			case LOGGED_IN:
				ShardRequest request = new ShardRequest(cl);
				
				cl.addRequest(request);
				break;
			}
			
			
		}
	}
	
	public static class RedirectHandler extends PacketHandler{
		ByteBuffer buffer = ByteBuffer.allocate(10);
		byte[] ip = new byte[4];
		
		public RedirectHandler(){
			super(0x8C);
			
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		}
		
		@Override
		public void handle(Client cl, ByteChannel channel) throws IOException{
			buffer.rewind();
			channel.read(buffer);
			buffer.clear();
			
			ip[0] = buffer.get();
			ip[1] = buffer.get();
			ip[2] = buffer.get();
			ip[3] = buffer.get();
			
			/* The port is stored in network byte order */
			buffer.order(ByteOrder.BIG_ENDIAN);
			int port = buffer.getShort();
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			
			int key = buffer.getInt();
			
			/* Construct the new redirected address */
			InetSocketAddress redirect = new InetSocketAddress(
				InetAddress.getByAddress(ip), port);
			
			cl.channel.close();
			cl.connect(redirect);
			
			/*
			ByteBuffer keyBuffer = ByteBuffer.allocate(4);
			keyBuffer.order(ByteOrder.LITTLE_ENDIAN);
			keyBuffer.clear();
			keyBuffer.putInt(cl.key);
			
			cl.send(keyBuffer);
			*/
		}
		
	}
	
	/**
	 * This Request is raised when the client is to select a Shard to connect
	 * to.
	 * @author Kristopher Ives
	 */
	public static class ShardRequest extends Request<Shard>{
		Client cl = null;
		Shard response = null;
		
		public ShardRequest(Client cl){
			super(Shard.class);
			
			this.cl = cl;
		}

		@Override
		public Shard getResponse(){
			return(response);
		}
		
		@Override
		public boolean isCancelled(){
			return(false);
		}
		
		@Override
		public boolean isResolved(){
			return(response == null);
		}
		
		@Override
		public void cancel() throws IllegalStateException{
			/* TODO: support cancelling of ShardRequest */
		}

		@Override
		public void resolve(Shard response) throws IllegalStateException{
			this.response = response;
			
			ByteBuffer buffer = ByteBuffer.allocate(3);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.clear();
			
			buffer.put((byte)0xA0);
			buffer.putShort((byte)response.id);
			
			cl.send(buffer);
			
			for(RequestListener listener : cl.getListeners(RequestListener.class)){
				listener.requestResolved(cl, this);
			}
		}
	}
	
	static Map<Integer, AccountResponse> loginResponses = new HashMap<Integer, AccountResponse>();
	
	/**
	 * Handles a packet sent from the login Server to list the Shards the client
	 * can connect to.
	 * @author Kristopher Ives
	 */
	public class ShardListHandler extends PacketHandler{
		ByteBuffer header = ByteBuffer.allocate(2 + 1 + 2);
		ByteBuffer serverBuffer = ByteBuffer.allocate(2 + 32 + 1 + 1 + 4);
		byte[] serverName = new byte[32];
		byte[] ip = new byte[4];
		
		public ShardListHandler(){
			super(0xA8);
			
			header.order(ByteOrder.BIG_ENDIAN);
			serverBuffer.order(ByteOrder.BIG_ENDIAN);
		}
		
		@Override
		public void handle(Client cl, ByteChannel channel) throws IOException{
			header.rewind();
			channel.read(header);
			header.clear();
			
			int packetSize = (int)header.getShort() & 0xFFFF;
			int flags = (int)header.get() & 0xFF;
			int serverCount = header.getShort() & 0xFFFF;
			
			ArrayList<Shard> shards = new ArrayList<Shard>(serverCount);
			
			for(int i=0; i < serverCount; i++){
				/* Restore the buffer */
				serverBuffer.rewind();
				channel.read(serverBuffer);
				serverBuffer.clear();
				
				Shard shard = cl.server.new Shard();
				shard.id = serverBuffer.getShort() & 0xFFFF;
				
				/* Server name in ASCII */
				serverBuffer.get(serverName);
				shard.name = new String(serverName);
				
				/* The current load of the server */
				shard.load = serverBuffer.get() & 0xFF;
				
				/* TODO: Support timezones */
				serverBuffer.get();
				
				ip[3] = serverBuffer.get();
				ip[2] = serverBuffer.get();
				ip[1] = serverBuffer.get();
				ip[0] = serverBuffer.get();
				shard.address = InetAddress.getByAddress(ip);
				
				shards.add(shard);
			}
			
			/* This list of Shards replaces the old list for this server */
			cl.server.shards = shards;
			
			/* When the shard list is received it means the login request was
			 * successful */
			
			for(Request<AccountResponse> request : cl.getRequests(LoginRequest.class)){
				request.resolve(AccountResponse.LOGGED_IN);
			}
		}
	}

	
	/**
	 * Generates a login-request packet
	 * @param user the account username (must be 30 characters or less)
	 * @param pass the account password (must be 30 characters or less)
	 * @return the packet data
	 * @throws IllegalArgumentException if either username or password are invalid as described
	 */
	public static ByteBuffer request(String user, String pass) throws IllegalArgumentException{
		if(user.length() > 30 || pass.length() > 30){
			throw(new IllegalArgumentException("Account username or password is too long"));
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(1 + 30 + 30 + 1);
		buffer.clear();
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		/* Packet header */
		buffer.put((byte)0x80);
		
		try{
			buffer.put(user.getBytes("ASCII"));
			buffer.position(1 + 30);
			buffer.put(pass.getBytes("ASCII"));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		
		/* This is a terminator (although it's useless) */
		buffer.put((byte)0x00);
		
		return(buffer);
	}
	
	/**
	 * Generates a Shard selection packet.
	 * @param id the shard to choose
	 * @return the packet data
	 */
	public static ByteBuffer choose(int id){
		ByteBuffer buffer = ByteBuffer.allocate(3);
		buffer.clear();
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.put((byte)0xA0);
		buffer.putShort((short)id);
		
		return(buffer);
	}	
}
