package chessminion2009;

import chessminion.messages.GameStatusWrapper;
import chessminion.messages.MessageWrapper;
import javax.swing.event.MouseInputListener;
import anim.*;
import chessminion.entities.ChessGame;
import chessminion.entities.ChessGame.GameEndStatus;
import objects.*;
import objects.Image;
import containers.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import chessminion.gameinfo.*;
import chessminion.gameinfo.ChessPiece.PieceColor;
import chessminion.gameinfo.ChessPiece.PieceType;
import chessminion.messages.ChatMessageWrapper;
import chessminion.sessions.GameControllerBean.MoveResult;
import chessminion.sessions.GameControllerRemote;
import java.awt.geom.AffineTransform;
import java.io.InputStream;
import javax.ejb.EJBException;
import javax.naming.InitialContext;

public class ChessBoard extends JApplet
        implements Runnable, MouseInputListener, ActionListener, SubscriberInterface {

    /**
     *
     */
    private static final long serialVersionUID = 4372481633567165906L;
    static int /*default protection */ BOX_SIZE = 70;
    private JMSSubscriber sub;
    JFrame frame = null;
    ChatBox chatBox;
    JPanel mainPane;
    Layers layers = new Layers();
    Render render;
    Screen screen;
    Mouse mouse;
    Image[][] pieces = new Image[8][8];
    Square[][] boxes = new Square[8][8];
    private GameControllerRemote gameSession;
    PieceColor myColor;
    int boxSize = BOX_SIZE;

    public void setGameSession(GameControllerRemote gameSession) {
        this.gameSession = gameSession;
    }

    public void run() {
        try {
            ChessGame g = gameSession.getGame();
            myColor = gameSession.getMyColor();
            drawBoard();
            if (null != myColor) {
                System.err.println("We are playing in game " + g.getId() + " as " + myColor);
            } else {
                System.err.println("We are observing game " + g.getId());
            }
            placePieces(g.getCurrentBoard());
            waitForMove();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitForMove() {
        int row = 0, col, row1, col1;
        for (; row >= 0;) {
            System.out.println("waiting for click1");
            mouse.waitFor("mouse click", null);
            row = (int) Math.floor(mouse.getX() / boxSize);
            col = (int) Math.floor(mouse.getY() / boxSize);
            if(row>7 || col >7) continue;
            System.out.println("Clicked1 on" + row + ":" + col);
            this.setHighlightColor(row, col);
            repaint();
            if (pieces[row][col] != null) {
                System.out.println("waiting for click2");
                mouse.waitFor("mouse click", null);
                row1 = (int) Math.floor(mouse.getX() / boxSize);
                col1 = (int) Math.floor(mouse.getY() / boxSize);

                System.out.println("Clicked2 on" + row1 + ":" + col1);
                if ((row1 == row) && (col == col1)) {
                    this.setDefaultColor(row, col);
                    repaint();
                    continue;
                }else if(row1 <=7 && col1 <= 7){
                    MoveResult result;
                    Point src = gamePointFromDisplayCoords(row, col);
                    Point dst = gamePointFromDisplayCoords(row1, col1);
                    try {
                        result = gameSession.sendMove(src,dst);
                        switch(result) {
                            case MOVE_OK:
                                System.err.println("Move accepted!");
                                break;
                            case MOVE_PROMOTES:
                                String typeString = (String)JOptionPane.showInputDialog(
                                    frame,
                                    "Promote pawn to:\n",
                                    "Promotion Required",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    new Object[]{"Queen","Knight","Rook","Bishop"},
                                    "Queen");
                                System.err.println("User selected " + typeString + " for promotion");
                                PieceType promotionChoice = null;
                                if ("Queen".equals(typeString)) {
                                    promotionChoice = PieceType.QUEEN;
                                } else if ("Knight".equals(typeString)) {
                                    promotionChoice = PieceType.KNIGHT;
                                } else if ("Rook".endsWith(typeString)) {
                                    promotionChoice = PieceType.ROOK;
                                } else if ("Bishop".equals(typeString)) {
                                    promotionChoice = PieceType.BISHOP;
                                } else {
                                    // Well, I don't know how you pulled that off--congratulations
                                    // I'll make it a Queen anyway
                                    promotionChoice = PieceType.QUEEN;
                                }
                                result = gameSession.sendMove(src, dst, promotionChoice);
                                if (MoveResult.MOVE_OK != result) {
                                    chatBox.update("Move rejected after promotion: " + result);
                                    System.err.println("Move rejected after promotion: " + result);
                                }
                                break;
                            case MOVE_ILLEGAL:
                                chatBox.update("That move is not legal.");
                                //nobreak;
                            case MOVE_INVALID:
                                System.err.println("Move rejected: " + result);
                                break;
                            case GAME_OVER:
                                chatBox.update("This game is over (" + gameSession.getGame().getStatus() + ")");
                                break;
                            case NOT_PLAYING:
                                chatBox.update("You are not a player in this game.");
                                break;
                        }
                    } catch (EJBException e) {
                        Throwable t = e.getCausedByException();
                        if (t instanceof java.rmi.AccessException) {
                            chatBox.update("You do not have permission to execute this command.");
                        } else {
                            chatBox.update("Error sending move.");
                        }
                    }
                }
            }
            this.setDefaultColor(row, col);
            repaint();

        }
        System.err.println("end of waitformove method");
    }

    public void movePiece(int rowSrc, int colSrc, int rowDst, int colDst, double delay) {
        try {
            Image img = pieces[rowSrc][colSrc];
            double totalFrames = (delay * render.fps);
            for (int i = 1; i <= totalFrames; i++) {
                img.move(rowDst * boxSize, colDst * boxSize, i, totalFrames, "linear");
                Thread.sleep(render.timeBtFrames);
            }
            pieces[rowDst][colDst] = pieces[rowSrc][colSrc];
            pieces[rowSrc][colSrc] = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("done with movePiece");
    }

    public void update(MessageWrapper message) {
        // this should be checked upstream, right?
        if (message instanceof GameStatusWrapper) {
            GameStatusWrapper gsw = (GameStatusWrapper) message;
            placePieces(gsw.getBoard());
            if (gsw.getGameState() != GameEndStatus.INCOMPLETE) {
                chatBox.update("Game ended: " + gsw.getGameState());
            }

        } else if (message instanceof ChatMessageWrapper) {
            //System.out.println(((ChatMessageWrapper) message).getMessage().format());
            ChatMessageWrapper cmw = (ChatMessageWrapper) message;
            chatBox.update(cmw.getMessage().format());
        }
    }

    public void placePieces(chessminion.gameinfo.ChessBoard gsm) {
        ChessPiece[][] cp = gsm.getBoard();
        layers.removeLayer(2);
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                final ChessPiece thisPiece = pieceFromDisplayCoords(cp, col, row);
                if (thisPiece != null) {
                    pieces[col][row] = new Image();
                    pieces[col][row].setImageStream(getImageStream(thisPiece));
                    pieces[col][row].setWidth(boxSize);
                    pieces[col][row].setHeight(boxSize);
                    
                    pieces[col][row].setX(col * boxSize);
                    pieces[col][row].setY(row * boxSize);

                    layers.add(pieces[col][row], 2);
                }
            }
        }
        repaint();
        System.err.println("Done with placePieces");
    }

    void drawBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boxes[row][col] = new Square();
                boxes[row][col].setWidth(boxSize);
                setDefaultColor(row, col);
                boxes[row][col].setX(row * boxSize);
                boxes[row][col].setY(col * boxSize);
                layers.add(boxes[row][col], 1);
            }
        }

    }

    public boolean initJFrame() {
        try {
            frame = new JFrame("Chessminions2009");
            //goFullScreen();
            //frame.createBufferStrategy(2);
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            //frame.setContentPane((Container) this);
            frame.setVisible(true);
            screen = new Screen(frame);
            mouse = new Mouse(frame);
            render = new Render(24, this);
            addMouseListener(this);
            addMouseMotionListener(this);
            mainPane = new JPanel(new BorderLayout());
            mainPane.add((Container) this, BorderLayout.CENTER);
            chatBox = new ChatBox(gameSession);
            mainPane.add(chatBox, BorderLayout.PAGE_END);
            frame.add(mainPane);
            screen.setWidth(8 * boxSize);
            //screen.setHeight(8*boxSize+)
            //render.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ChessBoard cb = new ChessBoard();
        cb.startGame(452L);
    }

    public void startGame(long gameID) {
        Thread t1 = new Thread(this);
        try {
            InitialContext context = RemoteServer.getContext();
            this.sub = new JMSSubscriber();
            GameControllerRemote gc = (GameControllerRemote) context.lookup("chessminion.sessions.GameControllerRemote");
            System.err.println("Got gamesession " + gc);
            gc.setGame(gameID);
            this.setGameSession(gc);
            this.sub.subscribeLocal(gameID, this);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }

        if (this.initJFrame() == false) {
            System.out.println("Error while initializing...returning");
            return;
        }

        try {
            t1.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        layers.render(g2d);
    }

    public void goFullScreen() {
        GraphicsDevice device;
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.isFullScreenSupported()) {
            device.setFullScreenWindow(frame);/*
            if (device.isDisplayChangeSupported()) {
            device.setDisplayMode(new DisplayMode(1280, 800, 32, // bitDepth
            // - 8 bits
            // 256
            // colors
            DisplayMode.REFRESH_RATE_UNKNOWN));

            } else {
            System.err.println("Change display mode not supported");
            }*/
        } else {
            System.err.println("Full screen not supported");
        }
    }

    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        synchronized (mouse.move) {
            mouse.lastMove = e;
            mouse.move.notifyAll();
        }
    }

    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

        synchronized (mouse.click) {
            mouse.lastClick = e;
            mouse.click.notifyAll();
        }
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        synchronized (mouse.press) {
            mouse.lastPress = e;
            mouse.press.notifyAll();
        }
    }

    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        synchronized (mouse.release) {
            mouse.lastRelease = e;
            mouse.release.notifyAll();
        }
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Point gamePointFromDisplayCoords(int row, int col) {
        if (myColor == PieceColor.WHITE) { // some day, we shall restore the rotation
            return new Point(row, 7 - col);
        } else {
            return new Point(7 - row, col);
        }
    }
    
    private InputStream getImageStream(ChessPiece cp) {
        return ChessBoard.class.getClassLoader().getResourceAsStream("images/"  + cp.toString() + ".png");
    }

    private ChessPiece pieceFromDisplayCoords(ChessPiece[][] cp, int col, int row) {
        if (PieceColor.WHITE == myColor) {
                return cp[col][7 - row];
        } else if (PieceColor.BLACK == myColor) {
                return cp[7 - col][row];
        } else {
                double x = col - 3.5;
                double y = 3.5 - row;
                int x_t = (int) (y + 3.5);
                int y_t = (int) (-x + 3.5);
                System.err.println(
                        String.format("Got board position (%d,%d) for coordinate (%d,%d)",
                        new Object[]{x_t,y_t,col,row})
                );
                /* input vector (x,y)
                 *
                 *  Rotation Matrix:
                 *
                 *  [ 0, 1]
                 *  [-1, 0]
                 *
                 */
                return cp[x_t][y_t];
        }
    }

    public class Piece {

        public Image img;
        PieceType piecetype = PieceType.PAWN;
        public Point pos;

        public void move(Point pos, int delay) throws Exception {
            this.pos = pos;
            double totalFrames3 = (delay * render.fps);
            for (int i3 = 1; i3 <= totalFrames3; i3++) {
                img.move(pos.x * boxSize, pos.y * boxSize, i3, totalFrames3, "linear");
                Thread.sleep(render.timeBtFrames);
            }
        }
    }

    public void setDefaultColor(int row, int col) {
        if ((row + col) % 2 == 0 ^ null == myColor) {
            boxes[row][col].setColor("White");
        } else {
            boxes[row][col].setColor("Gray");
        }
    }

    public void setHighlightColor(int row, int col) {
        boxes[row][col].setColor("Blue");
    }
}


