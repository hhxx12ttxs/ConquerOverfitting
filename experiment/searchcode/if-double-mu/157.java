package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import antlr.PingLexer;
import antlr.PingParser;
import pingball.*;

/**
 * The server class
 * 
 * TSA(encapsulation/threadsafe datatype): Our thread safety argument is based off
 * encapsulation: The only time that the boards get updated is via one thread,
 * the UpdateThread, which when the server is created, only one instance of the
 * UpdateThread thread is initialized and executed. No other thread used can
 * mutate the board, only receive and place onto the queue. In terms of
 * threadsafety for transporting the data of what is to be done on the board, we
 * use a threadsafe datatype, a blockingqueue, to add commands to the queue.
 * This blockingqueue is used execute any sort of message passing between the
 * update thread and the server.
 */
public class PingballServer {

    private List<Board> boards = new ArrayList<Board>();
    private BlockingQueue<String> messages = new LinkedBlockingQueue<String>();
    private ServerSocket serverSocket;
    private Map<String, ArrayList<String>> boardConnections = new HashMap<String, ArrayList<String>>();
    private Map<String, Board> boardNames = new HashMap<String, Board>();

    /**
     * Constructor for PingballServer: prints IP address
     * 
     * @param port
     *            the port to listen to
     */
    public PingballServer(int port) {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a message to the messages Queue
     * 
     * @param s
     *            the message to add to the Queue
     */
    public synchronized void putMessage(String s) {
        messages.add(s);
    }

    /**
     * Accessor method for the Queue
     * 
     * @return the messages Queue
     */
    public BlockingQueue<String> getQueue() {
        return messages;
    }

    /**
     * Accessor method for the boards list
     * 
     * @return the list of boards
     */
    public List<Board> getBoards() {
        return boards;
    }

    /**
     * Accessor method for the Map from board name to Board
     * 
     * @return the Map of boardNames
     */
    public Map<String, Board> getNames() {
        return boardNames;
    }

    /**
     * The Map from board name to its connected Board's names
     * 
     * @return the Map of boardConnections
     */
    public Map<String, ArrayList<String>> getConnections() {
        return boardConnections;
    }

    /**
     * Start a PingballServer using the given arguments
     * 
     * Usage: PingballServer [--port PORT]
     * 
     * PORT is an optional integer in the range 0 to 65535 inclusive, specifying
     * the port where the server should listen for incoming connections. The
     * default port is 10987.
     * 
     **/
    public static void main(String[] args) throws ClassNotFoundException,
            IOException {
        int port = 10987;
        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while (!arguments.isEmpty()) {
                String tag = arguments.remove();
                try {
                    if (tag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > 65535) {
                            throw new IllegalArgumentException("port " + port
                                    + " out of range");
                        }
                    }
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(
                            "unable to parse number for " + tag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: NewServer [--port PORT]");
            return;
        }

        PingballServer server = new PingballServer(port);
        server.serve();

    }

    /**
     * Constantly listens for new connections. If a new PingballClient connects,
     * read the file name from the socket and create the board. Add the board to
     * the list of boards and the boardConnections map (starting with no
     * connections)
     * 
     * Finally, create a new thread to write out the board to this client, and
     * start it
     * 
     * @throws IOException
     */
    public void serve() throws IOException {
        Runnable listener = new ConnectBoardsThread(this);
        new Thread(listener).start();

        Runnable updater = new UpdateThread(this);
        new Thread(updater).start();
        while (true) {
            // block until a client connects
            Socket socket = serverSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            String file = in.readLine();
            Board board = null;

            board = createBoard(file);
            boards.add(board);
            boardNames.put(board.getName(), board);
            boardConnections.put(board.getName(),
                    new ArrayList<String>(Arrays.asList("", "", "", "")));

            // handle the client
            Runnable client = (Runnable) new ClientThread(socket, board);

            new Thread(client).start();

        }
    }

    /**
     * Create the board from a file
     * 
     * @param file
     *            the file name
     * @return the board built from the file
     * @throws RecognitionException
     * @throws IOException
     */
    public static Board createBoard(String file) {
        Board board = null;
        ANTLRFileStream in;
        try {
            in = new ANTLRFileStream(file);

            PingLexer lexer = new PingLexer(in);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PingParser parser = new PingParser(tokens);
            parser.parse();

            double gravity = 25;
            double mu = 0.025;
            double mu2 = 0.025;
            int xPos = 0;
            int yPos = 0;

            Map<String, String> boardArgs = parser.getVals().remove(0)
                    .get("board");
            if (boardArgs.containsKey("gravity"))
                gravity = Double.parseDouble(boardArgs.get("gravity"));
            if (boardArgs.containsKey("friction1"))
                mu = Double.parseDouble(boardArgs.get("friction1"));
            if (boardArgs.containsKey("friction2"))
                mu2 = Double.parseDouble(boardArgs.get("friction2"));
            if (boardArgs.containsKey("name")){
                board = new Board(boardArgs.get("name"), gravity, mu, mu2);
            }
            
            else{
                board = new Board("", gravity, mu, mu2);
            }
            
           
            for (HashMap<String, Map<String, String>> item : parser.getVals()) {
                for (String gadget : item.keySet()) {
                    String name = "";
                    if (item.get(gadget).containsKey("name")){
                        name = item.get(gadget).get("name");
                    }

                    if (gadget.equals("ball")) {
                        double x = Double
                                .parseDouble(item.get(gadget).get("x"));
                        double y = Double
                                .parseDouble(item.get(gadget).get("y"));
                        double xVel = Double.parseDouble(item.get(gadget).get(
                                "xVelocity"));
                        double yVel = Double.parseDouble(item.get(gadget).get(
                                "yVelocity"));
                        board.addBall(new Ball(name, x,
                                y, xVel, yVel));
                    }

                    else if (gadget.equals("fire")) {
                        Gadget trigger = board.gadgetNames.get(item.get(gadget)
                                .get("trigger"));
                        Gadget action = board.gadgetNames.get(item.get(gadget)
                                .get("action"));
                        trigger.addTrigger(action);
                    }

                    else {
                        xPos = Integer.parseInt(item.get(gadget).get("x"));
                        yPos = Integer.parseInt(item.get(gadget).get("y"));
                    }

                    if (gadget.equals("triangleBumper")) {
                        int orientation = Integer.parseInt(item.get(gadget)
                                .get("orientation"));
                        board.addGadget(new TriangleBumper(name, xPos, yPos, orientation));
                    } else if (gadget.equals("circleBumper")) {
                        board.addGadget(new CircleBumper(name, xPos, yPos));
                    }

                    else if (gadget.equals("squareBumper")) {
                        board.addGadget(new SquareBumper(name, xPos, yPos));
                    }

                    else if (gadget.equals("absorber")) {
                        int height = Integer.parseInt(item.get(gadget).get(
                                "height"));
                        int width = Integer.parseInt(item.get(gadget).get(
                                "width"));
                        board.addGadget(new Absorber(name, xPos, yPos, width, height));
                    }

                    else if (gadget.equals("leftFlipper")) {
                        int orientation = Integer.parseInt(item.get(gadget)
                                .get("orientation"));
                        board.addGadget(new LeftFlipper(name, xPos, yPos, orientation));
                    }

                    else if (gadget.equals("rightFlipper")) {
                        int orientation = Integer.parseInt(item.get(gadget)
                                .get("orientation"));
                        board.addGadget(new RightFlipper(name, xPos, yPos, orientation));
                    }
                }
            }

        } catch (IOException | RecognitionException e) {
            e.printStackTrace();
        }

        return board;

    }

}

