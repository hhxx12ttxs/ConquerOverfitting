/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.core;

import controller.game.Game;
import java.awt.Color;
import model.core.tree.TreeNode;
import model.core.tree.TreeRoot;
import model.core.tree.TriangleTree;
import variables.ZoomStatic;
import view.EngineWindow;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import model.core.buildings.Building;
import model.core.events.ZoomChangeEvent;
import model.core.external.Saveable;
import model.core.moveable.Moveable;
import model.core.particles.ParticleEffect;
import model.core.particles.ParticleEngine;

/**
 *
 * @author Wouter
 */
public abstract class Grid extends Drawable implements Saveable {

  private Point drawOffset = new Point(0, 0);
  private boolean drawTree = false, drawGrid = false;
  private Square[][] squares;
  private int gridSizeX = 0, gridSizeY = 0;
  private TriangleTree tree = null;
  private int currentZoomLevel = 0;

  // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
  public boolean isDrawingGrid() {
    return drawGrid;
  }

  public void setDrawGrid(boolean drawGrid) {
    this.drawGrid = drawGrid;
  }

  public boolean getDrawTree() {
    return drawTree;
  }

public int getCurrentZoomLevel() {
    return this.currentZoomLevel;
  }

  public void setDrawTree(boolean drawTree) {
    this.drawTree = drawTree;
  }

  public TriangleTree getTree() {
    return tree;
  }

  public void setTree(TriangleTree tree) {
    this.tree = tree;
  }

  /**
   * Gets the draw offset of this grid.
   * @return The point containing the point.
   */
  public Point getDrawOffset() {
    return drawOffset;
  }

  public void setDrawOffset(Point drawOffset) {
    this.drawOffset = drawOffset;
    this.onOffsetChange();
  }

  public int getGridSizeX() {
    return gridSizeX;
  }

  protected void setGridSizeX(int gridSizeX) {
    this.gridSizeX = gridSizeX;
  }

  public int getGridSizeY() {
    return gridSizeY;
  }

  protected void setGridSizeY(int gridSizeY) {
    this.gridSizeY = gridSizeY;
  }

  public Square[][] getSquares() {
    return squares;
  }

  protected void setSquares(Square[][] newSquares) {
    this.squares = newSquares;
  }

  /**
   * Gets the square at the given point.
   * @param point The point (grid location!).
   * @return The Square at that location. Null if the point is out of bounds.
   */
  public Square getSquareFromPoint(Point point) {
    if (point == null || point.x < 0 || point.x >= this.getGridSizeX()
            || point.y < 0 || point.y >= this.getGridSizeY()) {
      return null;
    } else return this.getSquares()[point.x][point.y];
  }

  /**
   * Sets the current zoom level, as defined in ZoomStatic
   * @param zoomLevel The new zoomlevel.
   * @see ZoomStatic
   */
  public void setCurrentZoomLevel(int zoomLevel) {
    double offsetModifierWidth = 1, offsetModifierHeight = 1;

    switch (zoomLevel) {
      case ZoomStatic.ZOOM_SMALL:
        offsetModifierWidth = (double) ZoomStatic.ZOOM_SMALL_WIDTH / (double) PlayfieldSquare.WIDTH;
        offsetModifierHeight = (double) ZoomStatic.ZOOM_SMALL_HEIGHT / (double) PlayfieldSquare.HEIGHT;

        PlayfieldSquare.WIDTH = ZoomStatic.ZOOM_SMALL_WIDTH;
        PlayfieldSquare.HEIGHT = ZoomStatic.ZOOM_SMALL_HEIGHT;
        break;
      case ZoomStatic.ZOOM_MEDIUM:
        offsetModifierWidth = (double) ZoomStatic.ZOOM_MEDIUM_WIDTH / (double) PlayfieldSquare.WIDTH;
        offsetModifierHeight = (double) ZoomStatic.ZOOM_MEDIUM_HEIGHT / (double) PlayfieldSquare.HEIGHT;

        PlayfieldSquare.WIDTH = ZoomStatic.ZOOM_MEDIUM_WIDTH;
        PlayfieldSquare.HEIGHT = ZoomStatic.ZOOM_MEDIUM_HEIGHT;
        break;
      case ZoomStatic.ZOOM_LARGE:
        offsetModifierWidth = (double) ZoomStatic.ZOOM_LARGE_WIDTH / (double) PlayfieldSquare.WIDTH;
        offsetModifierHeight = (double) ZoomStatic.ZOOM_LARGE_HEIGHT / (double) PlayfieldSquare.HEIGHT;

        PlayfieldSquare.WIDTH = ZoomStatic.ZOOM_LARGE_WIDTH;
        PlayfieldSquare.HEIGHT = ZoomStatic.ZOOM_LARGE_HEIGHT;
        break;
    }

    this.setDrawOffset(new Point((int) (this.getDrawOffset().x * offsetModifierWidth),
            (int) (this.getDrawOffset().y * offsetModifierHeight)));
    int old = this.currentZoomLevel;
    this.currentZoomLevel = zoomLevel;
    ZoomStatic.CURRENT_ZOOM_LEVEL = zoomLevel;
    // System.out.println(offsetModifierWidth + ", " + offsetModifierHeight);
    onZoomChange(new ZoomChangeEvent(old, zoomLevel));
  }
  // </editor-fold>

  /**
   * Called when the offset has changed (user has dragged the grid).
   */
  public void onOffsetChange() {
    for (int i = 0; i < this.getGridSizeX(); i++) {
      for (int j = 0; j < this.getGridSizeY(); j++) {
        squares[i][j].calculateDrawCornerPoints();
      }
    }
  }

  /**
   * Called when the zoom changes, so certain calculations are done again.
   */
  private void onZoomChange(ZoomChangeEvent event) {
    long calculateStartMS = System.currentTimeMillis();
    for (int i = 0; i < this.getGridSizeX(); i++) {
      for (int j = 0; j < this.getGridSizeY(); j++) {
        this.getSquares()[i][j].calculatePoints();
        this.getSquares()[i][j].calculateDrawCornerPoints();
      }
    }
    long calculateTime = System.currentTimeMillis() - calculateStartMS;

    long drawableObjectStartMS = System.currentTimeMillis();
    Game game = EngineWindow.getInstance().getGame();
    LinkedList<Moveable> moveable = game.getMoveableObjects();
    for (int i = 0; i < moveable.size(); i++) {
      moveable.get(i).onZoomChange(event);
    }

    LinkedList<Building> buildings = game.getBuildingList();
    for (int i = 0; i < buildings.size(); i++) {
      buildings.get(i).onZoomChange(event);
    }

    LinkedList<ParticleEffect> effects = ParticleEngine.getInstance().getParticleEffects();
    for (int i = 0; i < effects.size(); i++) {
      effects.get(i).onZoomChange(event);
    }

    long drawableObjectTime = System.currentTimeMillis() - drawableObjectStartMS;

    long treeStartMS = System.currentTimeMillis();
    constructTree();
    long treeTime = System.currentTimeMillis() - treeStartMS;

    System.out.println("Changing zoom; Re-calculate time: " + calculateTime
            + ", Re-tree time: " + treeTime
            + ", Re-object time: " + drawableObjectTime);
  }

  /**
   * Re-constructs the entire tree!
   */
  public void constructTree() {
    TreeNode[] rootChildren = new TreeNode[2];
    Point[] points = new Point[3];
    // Top point
    points[0] = this.getSquares()[0][0].getCornerPoints()[0];
    // Left point
    points[1] = this.getSquares()[0][this.getGridSizeY() - 1].getCornerPoints()[1];
    // Right point
    points[2] = this.getSquares()[this.getGridSizeX() - 1][0].getCornerPoints()[2];

    rootChildren[0] = new TreeNode(null, new Triangle(points));


    Point[] points2 = new Point[3];
    // Bottom point
    points2[0] = this.getSquares()[this.getGridSizeX() - 1][this.getGridSizeY() - 1].getCornerPoints()[3];
    // Left point
    points2[1] = this.getSquares()[0][this.getGridSizeY() - 1].getCornerPoints()[1];
    // Right point
    points2[2] = this.getSquares()[this.getGridSizeX() - 1][0].getCornerPoints()[2];

    rootChildren[1] = new TreeNode(null, new Triangle(points2));

    this.setTree(new TriangleTree(this, new TreeRoot(rootChildren)));
    this.getTree().construct(this.getTree().getTreeRoot().getChildren());
    this.getTree().fillTree();
  }

  /**
   * Draws the grid of this grid (the black lines!)
   * @param g The Graphics to draw on.
   */
  public void drawGrid(Graphics g) {
    g.setColor(Color.BLACK);
    for (int i = 0; i < this.getGridSizeX(); i++) {
      g.drawLine(this.squares[i][0].getDrawPixelX(),
              this.squares[i][0].getDrawPixelY(),
              this.squares[i][this.getGridSizeY() - 1].getDrawCornerPoints()[1].x,
              this.squares[i][this.getGridSizeY() - 1].getDrawCornerPoints()[1].y);
    }

    for (int i = 0; i < this.getGridSizeY(); i++) {
      g.drawLine(this.squares[0][i].getDrawPixelX(),
              this.squares[0][i].getDrawPixelY(),
              this.squares[this.getGridSizeX() - 1][i].getDrawCornerPoints()[2].x,
              this.squares[this.getGridSizeX() - 1][i].getDrawCornerPoints()[2].y);
    }

    g.drawLine(this.squares[0][this.getGridSizeY() - 1].getDrawCornerPoints()[1].x,
            this.squares[0][this.getGridSizeY() - 1].getDrawCornerPoints()[1].y,
            this.squares[this.getGridSizeX() - 1][this.getGridSizeY() - 1].getDrawCornerPoints()[1].x,
            this.squares[this.getGridSizeX() - 1][this.getGridSizeY() - 1].getDrawCornerPoints()[1].y);
    
    g.drawLine(this.squares[this.getGridSizeX() - 1][0].getDrawCornerPoints()[2].x,
            this.squares[this.getGridSizeX() - 1][0].getDrawCornerPoints()[2].y,
            this.squares[this.getGridSizeX() - 1][this.getGridSizeY() - 1].getDrawCornerPoints()[2].x,
            this.squares[this.getGridSizeX() - 1][this.getGridSizeY() - 1].getDrawCornerPoints()[2].y);
  }

  @Override
  public void draw(Graphics g) {
    long squareStartMS = System.currentTimeMillis();
    // Draw squares + their textures
    // <editor-fold defaultstate="collapsed" desc="Draw method that isn't any faster">
    /*for (int i = 0; i < this.getTree().getLeafList().size(); i++) {
    TreeNode node = this.getTree().getLeafList().get(i);
    int pointsOffScreen = 0;
    Triangle drawTriangle = node.getTriangle().getDrawTriangle();
    for (int j = 0; j < drawTriangle.getPoints().length; j++ ) {
    if (!EngineWindow.getInstance().getCanvas().getVisibleRect().contains(drawTriangle.getPoints()[j])) {
    pointsOffScreen++;
    }
    }
    // No points on the screen
    if (pointsOffScreen == drawTriangle.getPoints().length) {
    continue; //, no need to draw the child squares
    } else {
    LinkedList<SquarePoint> squares = node.getUnderlyingSquares();
    for( int j = 0; j < squares.size(); j++ ){
    squares.get(j).getSquare().draw(g);
    }
    }
    }*/
    // </editor-fold>
    for (int i = 0; i < this.getGridSizeX(); i++) {
      for (int j = 0; j < this.getGridSizeY(); j++) {
        this.getSquares()[i][j].draw(g);
      }
    }

    long squareTime = System.currentTimeMillis() - squareStartMS;

    long gridStartMS = System.currentTimeMillis();
    if (this.isDrawingGrid()) this.drawGrid(g);
    long gridTime = System.currentTimeMillis() - gridStartMS;

    long squareBorderStartMS = System.currentTimeMillis();
    for (int i = 0; i < this.getGridSizeX(); i++) {
      for (int j = 0; j < this.getGridSizeY(); j++) {
        Square s = this.getSquares()[i][j];
        if (s.isSelected()) s.drawBorder(g, Color.GREEN);
        else if (s.isHighlighted() || s.getFramesLeft() > 0) s.drawBorder(g, Color.ORANGE);
        // Frames left to be highlighted, minus one!
        s.substractedFramesLeft();
      }
    }
    long squareBorder = System.currentTimeMillis() - squareBorderStartMS;

    long treeStartMS = System.currentTimeMillis();
    if (this.getDrawTree()) {
      this.getTree().draw(g);
    }
    long treeTime = System.currentTimeMillis() - treeStartMS;

    Game game = EngineWindow.getInstance().getGame();
    long mouseStartMS = System.currentTimeMillis();
    Square selectedSquare = game.getSelectedSquare();
    if (selectedSquare != null) selectedSquare.drawBorder(g, Color.BLUE);
    long mouseTime = System.currentTimeMillis() - mouseStartMS;

    // <editor-fold defaultstate="collapsed" desc="Old draw code">
    /*long buildingStartMS = System.currentTimeMillis();
    for (int i = 0; i < this.getGridSizeX(); i++) {
    for (int j = 0; j < this.getGridSizeY(); j++) {
    Square s = this.getSquares()[j][i];
    if (s.getBuilding() != null) {
    // Draw the square again, so the lines are proper
    s.draw(g);
    if (s.isBuildingParent()) {
    // Draw the object
    s.getBuilding().draw(g);
    }
    }
    }
    }
    long buildingTime = System.currentTimeMillis() - buildingStartMS;
    
    long moveableStartMS = System.currentTimeMillis();
    LinkedList<Moveable> list = EngineWindow.getInstance().getGame().getMoveableObjects();
    
    
    
    for (int i = 0; i < list.size(); i++) {
    list.get(i).draw(g);
    }
    
    long moveableTime = System.currentTimeMillis() - moveableStartMS;*/




    /*// Draw particles!
    ParticleEngine engine = ParticleEngine.getInstance();
    int count = 0, particleCount = 0;
    long start = System.currentTimeMillis();
    for (int i = 0; i < engine.getParticleEffects().size(); i++) {
    engine.getParticleEffects().get(i).draw(g);
    count++;
    particleCount += engine.getParticleEffects().get(i).getParticleList().size();
    }
    
    if (EngineWindow.getInstance().getGame().getTotalFrames() % 300 == 0) {
    System.out.println("Drawn " + count + " particle effects with " + particleCount + " particles, in "
    + (System.currentTimeMillis() - start) + "ms");
    }*/
    // </editor-fold>

    long drawableObjectStartMS = System.currentTimeMillis();
    ArrayList<DrawableObject> drawableObjectList = new ArrayList<DrawableObject>();

    drawableObjectList.addAll(game.getBuildingList());
    drawableObjectList.addAll(game.getMoveableObjects());
    ParticleEngine engine = ParticleEngine.getInstance();

    for (ParticleEffect effect : engine.getParticleEffects()) {
      try {
        drawableObjectList.addAll(effect.getParticleList());
      } catch (ArrayIndexOutOfBoundsException e) {
        // Sometimes a particle is removed from the list, when the function
        // already grabbed the previous length, causing this exception,
        // ignore it
      }
    }

    // Remove preview buildings
    LinkedList<DrawableObject> toRemove = new LinkedList<DrawableObject>();
    for (DrawableObject o : drawableObjectList) {
      if (o instanceof Building) {
        Building b = (Building) o;
        if (b.getSquare() == null) toRemove.add(o);
      }
    }
    drawableObjectList.removeAll(toRemove);


    drawableObjectList = DrawableObject.sortByDrawIndex(drawableObjectList);

    int count = drawableObjectList.size();
    for (DrawableObject dr : drawableObjectList) {
      if (dr == null) continue;
      dr.draw(g);
    }

    // <editor-fold defaultstate="collapsed" desc="Debug code">
    /*if (EngineWindow.getInstance().getGame().getTotalFrames() % 100 == 0) {
    System.out.println("Old list: ");
    for (int i = 0; i < drawableObjectList.size(); i++) {
    System.out.println(i + ": " + drawableObjectList.get(i).getDrawPixelLocation().y);
    }
    }*/
    /*if (EngineWindow.getInstance().getGame().getTotalFrames() % 100 == 0) {
    System.out.println("--------------\nNew list: ");
    for (int i = 0; i < drawableObjectList.size(); i++) {
    System.out.println(i + ": " + drawableObjectList.get(i).getDrawPixelLocation().y);
    }
    }*/
    // </editor-fold>

    long drawableObjectTime = System.currentTimeMillis() - drawableObjectStartMS;




    if (game.getTotalFrames() % 3000 == 0) {
      System.out.println("Grid: " + gridTime
              + ", Square: " + squareTime
              + ", SquareBorder: " + squareBorder
              + ", Tree: " + treeTime
              + ", Mouse: " + mouseTime
              + ", DrawableObject: " + drawableObjectTime + " (" + count + " objects)");
      // System.out.println("Leaf size: " + this.getTree().getLeafList().size());
    }
  }

  /**
   * Initializes the squares.
   */
  public abstract void initSquares();
}

