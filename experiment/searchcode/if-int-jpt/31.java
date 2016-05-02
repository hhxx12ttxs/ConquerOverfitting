import edu.neu.ccs.gui.*;
import edu.neu.ccs.*;
import java.util.HashMap;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.event.*;

/** A graph traverser that is used to draw city graphs.
    This implementation uses JPT and paintables to create the graph (or at least,
    It uses this as an alternative to the draw teachpack because adding
    paintables to a window is much closer to the design
    philosophy used here, instead of having one unified draw method.
    @author Ian Helmke, Dan King
*/
public class GraphDrawer implements IGraphTraverser<String, City, Route> {
  
  /** The height of the panel that the map will be drawn on. */
  public final int HEIGHT = 800;
  /** The width of the panel that the map will be drawn on. */
  public final int WIDTH = 1280;

  /** The panel that this map will be drawn on. */
  public BufferedPanel panel = new BufferedPanel(WIDTH, HEIGHT);

  private MouseActionAdapter adapter = panel.getMouseActionAdapter();

  private MouseAction mouseAction = new MouseAction(){
      public void mouseActionPerformed(MouseEvent e) {
        handleButtonDown(e);
      }
    };
  /** A table associating the shapes in this image with nodes on the graph. */ 
  private HashMap<Paintable, INode<City, Route>> nodeTable =
    new HashMap<Paintable, INode<City, Route>>();

  /** A table associating the routes (lines) in this image with links on the graph. */
  private HashMap<Paintable, ILink<Route, City>> linkTable =
    new HashMap<Paintable, ILink<Route, City>>();

  /** The minimum longitude of this graph. */
  private final int xMin;
  /** The maximum longitude of this graph. */
  private final int xMax;
  /** The minimum latitude of this graph. */
  private final int yMin;
  /** The maximum latitude of this graph. */
  private final int yMax;

  /** The scale of the x axis of the representation of this graph. */
  private final double xScale;
  /** The scale of the y axis of the representation of this graph. */
  private final double yScale;

  /** A variable storing the last item clicked, so that we know where to
      start creating a route from.
  */
  private ShapePaintable itemClicked;

  private MultiLineTextPaintable currentDirections = new MultiLineTextPaintable("No Directions");

  /** The constructor. */
  public GraphDrawer(int xMin, int xMax, int yMin, int yMax, String name, boolean longlat) {

    this.xMin = xMin;
    this.xMax = xMax;
    this.yMin = yMin;
    this.yMax = yMax;
   
    this.xScale = this.WIDTH / (this.xMax - this.xMin);
    this.yScale = this.HEIGHT / (this.yMax - this.yMin);
   
    this.panel.frame(name);

    this.adapter.addMousePressedAction(mouseAction);

    this.panel.addPaintable(currentDirections);
    currentDirections.move(50, 50);
    currentDirections.setFillPaint(Color.BLACK);
     
  }
  
  /** Accepts a graph object. This does not do anything right now; in the future,
      it may draw city specific information.
      @param graph The graph currently being visited.
  */
  public void acceptGraph(Graph<String, City, Route> graph){
    panel.show();
  }
  
  /** Accepts a node object (in this case, a city), and draws it onto
      the BufferedPanel window.
      @param node The node currently being visited.
  */
  public void acceptNode(INode<City, Route> node){
    double xPos = (node.getInfo().getPosition().x - xMin) * xScale;
    double yPos = (node.getInfo().getPosition().y - yMin) * yScale;
    XCircle circle = new XCircle(xPos, yPos, 6);
    Paintable city = new ShapePaintable(circle, PaintMode.FILL, Color.RED);

    nodeTable.put(city, node);
    
    panel.addPaintable(city);
    panel.repaint();
  }

  /** Accepts a link object and draws it onto the BufferedPanel window.
      @param link The link currently being visited.
      @param origin The node that this link originated from.
  */
  public void acceptLink(ILink<Route, City> link, INode<City, Route> origin){
    double xPosOrigin = (origin.getInfo().getPosition().x - xMin) * xScale;
    double yPosOrigin = (origin.getInfo().getPosition().y - yMin) * yScale;

    double xPosDest = (link.getDestination().getInfo().getPosition().x - xMin) * xScale;
    double yPosDest = (link.getDestination().getInfo().getPosition().y - yMin) * yScale;

    XLine2D routelink = new XLine2D(xPosOrigin,
                                    yPosOrigin,
                                    xPosDest,
                                    yPosDest);
    Stroke routeStroke = new BasicStroke(6);
    Paintable route = new ShapePaintable(routelink, PaintMode.DRAW, Color.GREEN, Color.GREEN);

    linkTable.put(route, link);

    panel.appendPaintable(route);
    panel.repaint();
  }

  /** Handles a mouse button press.
      @param e The information about the mouse button pressed.
  */
  public void handleButtonDown(MouseEvent e) {
    ShapePaintable p = (ShapePaintable)panel.getPaintableSequence().hits(e.getX(), e.getY());
    
    if (p == null) return;
    
    if (itemClicked != null) {
      //p.setFillPaint(Color.BLUE);
      itemClicked.setFillPaint(Color.RED);

      TraceStrategy<City, Route, String> t = new DirectionMaker();

      INode<City, Route> origin = nodeTable.get(itemClicked);
      INode<City, Route> destination = nodeTable.get(p);


      PathRoute<City, Route> path = 
        new PathRoute(Algorithms.<City, Route>DFS(origin, destination).getValue());
      String result = path.<String>tracePath(t);
      currentDirections.setString(result);
      
      itemClicked = null;
      panel.repaint();
      return;
    }
    p.setFillPaint(Color.BLUE);
    panel.repaint();
    itemClicked = p;
  }


}

