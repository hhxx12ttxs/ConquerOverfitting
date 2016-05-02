package net;

import java.io.*;
import java.net.*;
import java.util.*;

import model.*;

public class TockServer extends Thread implements TockConstants{
	
	public static int SERVER_PORT = 36963;
	private ServerSocket 		serverSocket;
	private ObjectInputStream 	incoming;
	private ObjectOutputStream 	outgoing;
	private boolean 			online;
	
	private ArrayList<TockServerHandler> handlers; // all of the games that are going on
	
	public TockServer() {
		online = false;
		handlers = new ArrayList<TockServerHandler>();
	}
	
	//Attempt to bring the server online.
	public boolean goOnline() {
		online = false;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			online = true;
			System.out.println("SERVER: Tock Server Online");
			start();
		} catch(IOException e) {
			System.out.println("SERVER: Error Getting Server Online");
		}
		return online;
	}
	
	//Do what is necessary to shut down the server connection
	public boolean goOffline() {
		try {
			if (online) {
				serverSocket.close();
				online = false;
				System.out.println("SERVER: Tock Server Offline");
			}
		} catch(IOException e) {
			System.out.println("SERVER: Error Going Offline");
		}
		return online;
	}
	
	//Try disconnecting from the client
	private boolean closeClientConnection(Socket s) {
		try {
			if (s != null) s.close();
			return true;
		} catch (IOException e) {
			System.out.println("SERVER: Error During Client Disconnect");
			return false;
		}
	}
	
	//Accept incoming messages from clients forever
	public void run() {
		//Accept messages forever
		while (online) {
			Socket socket = null;
			try {
				//Wait for an incoming message
				socket = serverSocket.accept();
			} catch(IOException e) {
				System.out.println("SERVER: Error Contacting Client");
			}
			try {
				//Make object streams for the socket
				incoming = new ObjectInputStream(socket.getInputStream());
				outgoing = new ObjectOutputStream(socket.getOutputStream());
				
				//Now handle the message
				try {
					TockPacket packet;
					packet = (TockPacket)incoming.readObject();
					if (DEBUG_NETWORK) System.out.println("SERVER: Received: " + packet);
					
					if (packet != null) {
						int gameNum = packet.getGameNum();
						//--- Check if we are playing a game or not
						if (-1 == gameNum) {
							if (packet.getID() == TockPacket.CREATE_GAME) handleCreateGame(packet);
							else if (packet.getID() == TockPacket.JOIN_GAME) handleJoinGame(packet);
							else if (packet.getID() == TockPacket.LIST_GAMES) handleListGames(packet);
						} else {
							//--- Sanity check on game num
							if (gameNum >= handlers.size()) {
								System.out.println("SERVER: Error, invalid game number.");
							} else {
								if (packet.getID() == TockPacket.CHAT) handleChat(packet, gameNum);
								else if (packet.getID() == TockPacket.MOVE) handleMove(packet, gameNum);
								else if (packet.getID() == TockPacket.REGISTER) handleRegister(packet, gameNum);
								else if (packet.getID() == TockPacket.EXIT) handleExit(packet, gameNum);
								else if (packet.getID() == TockPacket.SIT) handleSit(packet, gameNum);
								else if (packet.getID() == TockPacket.STAND) handleStand(packet, gameNum);
								else if (packet.getID() == TockPacket.UPDATE) handleUpdate(packet, gameNum);
								else System.out.println("SERVER: Error, Invalid packet");
							}
						}
					} else System.out.println("SERVER: Error, Invalid Client Message Command");
				} catch(ClassNotFoundException e) {
					System.out.println("SERVER: Error in Client Message Data");
				}
			} catch(IOException e) {
				System.out.println("SERVER: Error Receiving Client Message");
			} finally {
				//Now close the connection to this client
				if (DEBUG_NETWORK) System.out.println("SERVER: Closing Client Connection");
				closeClientConnection(socket);
			}
		}
	}
	
	private void handleChat(TockPacket packet, int gameNum) {
		handlers.get(gameNum).handleChat(packet);
		try {
			outgoing.flush();
		} catch (IOException e) {
			System.out.println("SERVER: Error Sending Packet Response");
		}
	}
	
	private void handleMove(TockPacket packet, int gameNum) {
		handlers.get(gameNum).handleMove(packet);
		try {
			outgoing.flush();
		} catch (IOException e) {
			System.out.println("SERVER: Error Sending Packet Response");
		}
	}
	
	private void handleRegister(TockPacket packet, int gameNum) {
		handlers.get(gameNum).handleRegister(packet);
		try {
			outgoing.flush();
		} catch (IOException e) {
			System.out.println("SERVER: Error Sending Packet Response");
		}
	}
	
	private void handleExit(TockPacket packet, int gameNum) {
		handlers.get(gameNum).handleExit(packet);
		try {
			outgoing.flush();
		} catch  (IOException e) {
			System.out.println("SERVER: Error Sending Packet Response");
		}
	}
	
	private void handleSit(TockPacket packet, int gameNum) {
		handlers.get(gameNum).handleSit(packet);
		try {
			outgoing.flush();
		} catch (IOException e) {
			System.out.println("SERVER: Error Sending Packet Response");
		}
	}
	
	private void handleStand(TockPacket packet, int gameNum) {
		handlers.get(gameNum).handleStand(packet);
		try {
			outgoing.flush();
		} catch (IOException e) {
			System.out.println("SERVER: Error Sending Packet Response");
		}
	}
	
	private void handleUpdate(TockPacket packet, int gameNum) {
		String sender = packet.getSender();
		try {
			//--- Get the game and chat
			TockGame game = handlers.get(gameNum).getGame();
			TockChat chat = handlers.get(gameNum).getChat();
			
			//--- Build the update packet...
			UpdatePacket update = new UpdatePacket(game.getBoard(),	game.getHand(sender), chat.getUpdate(sender), 
					game.getUsers(), game.getLastCard(), game.getDeckRemaining(), game.isPlaying());
			
			//--- ...and send it
			outgoing.writeObject(update);
			outgoing.flush();
			
		} catch (IOException e) {
			System.out.println("SERVER: Error Sending Packet Response");
		}
	}
	
	private void handleCreateGame(TockPacket packet) {
		int numOfPlayers = packet.getCard();
		String nameOfGame = packet.getMessage();
		try {
			int num = 0;
			
			//--- Check the name
			for (int i=0; i<handlers.size(); i++) {
				if (handlers.get(i).getName().equals(nameOfGame)) num = CREATE_GAME_ERROR_NAME;
			}
			
			//--- If still ok, check the number of players
			if (num == 0) {
				if ((numOfPlayers < 1) || (numOfPlayers > 4)) num = CREATE_GAME_ERROR_PLAYERS;
			}
			
			//--- If still ok, check the server load
			if (num == 0) {
				if (handlers.size() >= MAX_GAMES) num = CREATE_GAME_ERROR_FULL;
			}
			
			//--- Done our checks
			if (num == 0) {
				//--- Create the new game
				TockServerHandler newGame = new TockServerHandler(numOfPlayers, nameOfGame);
				
				//--- Add it to the list of games and return the id of the game
				handlers.add(newGame);
			}
			Integer numToReturn = new Integer(num);
			outgoing.writeObject(numToReturn);
			outgoing.flush();			
		} catch (IOException e) {
			System.out.println("SERVER: Error returning game number");
		}
	}
	
	private void handleJoinGame(TockPacket packet) {
		String nameOfGame = packet.getMessage();
		try {
			//--- Find the game
			int index = -1;
			for (int i=0; i<handlers.size(); i++) {
				if (handlers.get(i).getName().equals(nameOfGame)) {
					index = i;
				}
			}
			
			//--- Return the id of the gam
			Integer num = new Integer(index);
			outgoing.writeObject(num);
			outgoing.flush();
		} catch (IOException e) {
			System.out.println("SERVER: Error returning game number");
		}
	}
	
	private void handleListGames(TockPacket packet) {
		try {
			//--- List the games
			ArrayList<String> gamesList = new ArrayList<String>();
			for (int i=0; i<handlers.size(); i++) {
				gamesList.add(handlers.get(i).getName() + ":" + handlers.get(i).getGame().getNumOfPlayers());
			}
			
			//--- Return the list of games
			outgoing.writeObject(gamesList);
			outgoing.flush();
		} catch (IOException e) {
			System.out.println("SERVER: Error returning game list");
		} 
	}
				
	
	public static void main (String args[]) {
		TockServer server = new TockServer();
		server.goOnline();
	}
}

