package maize;

import java.util.*;
import java.awt.*;

/**This factory produces mazes using the Depth First Search algorithm, albeit randomised.
  * Described at http://en.wikipedia.org/wiki/Depth-first_search 
  */
public class DFSMazeFactory implements MazeFactory {

  /** Maze data. */
  protected boolean[][] mazedata = null;

  /** Maze width. */
  protected int width = 0;

  /** Maze height. */
  protected int height = 0;

  /** Visitor data */
  protected boolean[][] visited = null;

  /** Neighbour data */
  protected boolean[][] neighbour = null;

  /** Start point */
  protected Point start = null;

  /** Visited count */
  protected Vector<Integer> notNeighbour = null;

  /** To visit count */
  protected Vector<Integer> neighbourV = null;

  /** Default constructor. */
  public DFSMazeFactory() { }

  /** Public method for getting the maze.
    *
    * @param    width       Width of the maze.
    * @param    height      Width of the maze.
    *
    * @return         Maze object.
    */
  public Maze getMaze(int width, int height){

    /* Check for sane values, otherwise use defaults. */
    if(width < 7 || height < 7){

      this.width = 7;
      this.height = 7;
    } 
		if ( (width >> 1 << 1) == width ){

      this.width = width+1;
    } 
		if ( (height >> 1 << 1) == height ){

      this.height = height+1;
    }
		
    this.width = (this.width == 0)? width : this.width;
    this.height = (this.height == 0)? height : this.height;

    this.start = new Point();
    
    /* Build maze */
    this.buildMazeData();

    /* Encapsulate */
    return new Maze(this.mazedata, this.width, this.height, this.start.x, this.start.y, width-3, height-3);

  }

  /** Main logic loop for Maze building.
    * Returns a 2D array of booleans.
    */
  protected void buildMazeData(){

    this.buildBlankMaze();
    this.buildVisitorNeighbour();

    /* Initial conditions */
    int r = notNeighbour.get((int)(Math.random() * notNeighbour.size()));
    this.notNeighbour.removeElement(r);
    this.start.x = r / this.width;
    this.start.y = r % this.width;

    this.setVisited(this.start.x, this.start.y);
    
    /* Set neighbours */
    this.setNeighbour(this.start.x, this.start.y-2);
    this.setNeighbour(this.start.x+2, this.start.y);
    this.setNeighbour(this.start.x, this.start.y+2);
    this.setNeighbour(this.start.x-2, this.start.y);

    /* Build maze */
    while(!this.choosePath());


  }

  /** Creates a blank maze with rooms and walls.
    */
  protected void buildBlankMaze(){

    this.mazedata = new boolean[this.width][this.height];

    /**Walls go on even squares, rooms on odd.*/
    for(int x = 0; x < this.width; x++){

      for(int y = 0; y < this.height; y++){

        if((x>>1<<1) == x || (y>>1<<1) == y){

          this.mazedata[x][y] = true;
        } else {

          this.mazedata[x][y] = false;
        }
      }
    }
  }


  /** Sets the visited and neighbour sets.
    */
  protected void buildVisitorNeighbour(){

    this.visited = new boolean[(this.width-1)/2+1][(this.height-1)/2+1];
    this.neighbour = new boolean[(this.width-1)/2+1][(this.height-1)/2+1];
    this.notNeighbour = new Vector<Integer>();
    this.neighbourV = new Vector<Integer>();

    /*Adds all the rooms to the notNeighbour list */
    for( int i = 0; i< this.width; i++){
      for( int j = 0; j< this.height; j++){
        if((i>>1<<1) != i && (j>>1<<1) != j){
          notNeighbour.add(i*this.width + j);
        }
      }
    }
  }

  /** Has the room been visited?.
    *
    * @param  x     X coord.
    * @param  y     Y coord.
    *
    * @return       true/false
    */
  protected boolean isVisited(int x, int y){

    if(x <= 0 || y <= 0 || x > this.width-1 || y > this.height-1){
      return true;
    } else if(x == 1 && y ==1) {
      return this.visited[0][0];
    } else if(x == 1) {
      return this.visited[0][(y-1)/2];
    } else if(y == 1) {
      return this.visited[(x-1)/2][0];
    } else {
      return this.visited[(x-1)/2][(y-1)/2];
    }
  }

  /** Check is in map.
    *
    * @param  x     X coord.
    * @param  y     Y coord.
    *
    * @return       true/false
    */
  protected boolean isMap(int x, int y){

    if(x <= 0 || y <= 0 || x > this.width-1 || y > this.height-1){
      return false;
    } else {
      return true;
    }
  }

  /** Puts the room into the visited set.
    * Removes from the neighbour set. 
    * Mutually exclusive.
    *
    * @param  x     X coord
    * @param  y     Y coord
    */
  protected void setVisited(int x, int y){

    int i, j;

    if(x == 1 && y ==1) {
      i = 0;
      j = 0;
    } else if(x == 1) {
      i = 0;
      j = (y-1)/2;
    } else if(y == 1) {
      i = (x-1)/2;
      j = 0;
    } else {
      i = (x-1)/2;
      j = (y-1)/2;
    }
    this.neighbour[i][j] = false;
    this.visited[i][j] = true;


  }

  /** Sets a room to be a neighbour and checks if it has been visited. 
    *
    * @param  x     x Coord.
    * @param  y     y Coord.
    */
  protected void setNeighbour(int x, int y){

    if(x < 0 || y < 0 || x >= this.width || y >= this.height)
      return;

    int i, j;

    if(x == 1 && y ==1) {
      i = 0;
      j = 0;
    } else if(x == 1) {
      i = 0;
      j = (y-1)/2;
    } else if(y == 1) {
      i = (x-1)/2;
      j = 0;
    } else {
      i = (x-1)/2;
      j = (y-1)/2;
    }

    /* Can only be a neighbour if not been visited */
    if(!visited[i][j]){
      neighbour[i][j] = true;

      /* Adds to the neighbour list */
      this.neighbourV.add(x*this.width + y);
      this.notNeighbour.removeElement(x*this.width + y);
    }
  }

  /** Keeps making a path in one direction until it hits a visited square (or wall).
    * Recursive, shall add new neighbours to choose from as it unrolls.
    * 
    * @param  x             Current X
    * @param  y             Current Y
    * @param  xoffset       X offset for next room on path
    * @param  yoffset       Y offset for next room on path
    */
  protected void makePath(int x, int y, int xoffset, int yoffset){

    /* Set this room as visited. */
    this.setVisited(x, y);

    /* End condition, is next room visited? */
    if(this.isVisited(x + xoffset, y + yoffset))
        return;

    /* Normal use case */

    /* Next room is not visited, so destory wall to it */
    int wallx = x + (xoffset/2);
    int wally = y + (yoffset/2);
    this.mazedata[wallx][wally] = false;

    /* Recurse */
    this.makePath(x + xoffset, y + yoffset, xoffset, yoffset);

    /* Set neighbours, n, e, s, w */
    this.setNeighbour(x, y-2);
    this.setNeighbour(x+2, y);
    this.setNeighbour(x, y+2);
    this.setNeighbour(x-2, y);
  }

  /** Chooses a new direction and start point to make a path.
    * The iterative bit.
    * Also checks if the maze is complete.
    *
    * @return         If all rooms are visited.
    */
  protected boolean choosePath(){

    /* Check maze is done */
    if(neighbourV.size() == 0){
      return true;
    }

    /* Get neighbour square*/
    int nx, ny;
      
    int r = neighbourV.get((int)(Math.random() * neighbourV.size()));
    this.neighbourV.removeElement(r);
    nx = r / this.width;
    ny = r % this.width;

    /* Find the room it is neighbouring, and from that, a direction to put the path. */
    boolean dirFound = false;
    int vx = 0, vy = 0, xoffset = 0, yoffset = 0;

    /* Go in the order, north, east, south, west. 
       TODO, must be a better way than this. */
    if(isVisited(nx, ny - 2) && isMap(nx, ny-2)){
      vx = nx;
      vy = ny-2;
      xoffset = 0;
      yoffset = 2;
      dirFound = (vx < 0 || vy < 0 || vx > this.width || vy > this.height) ? false : true;
    } 
    if (isVisited(nx + 2, ny) && isMap(nx +2, ny) && !dirFound){
      vx = nx+2;
      vy = ny;
      xoffset = -2;
      yoffset = 0;
      dirFound = (vx < 0 || vy < 0 || vx > this.width || vy > this.height) ? false : true;
    } 
    if (isVisited(nx, ny + 2) && isMap(nx, ny+2) && !dirFound){
      vx = nx;
      vy = ny+2;
      xoffset = 0;
      yoffset = -2;
      dirFound = (vx < 0 || vy < 0 || vx > this.width || vy > this.height) ? false : true;
    }
    if (isVisited(nx - 2, ny) && isMap(nx-2, ny) && !dirFound){
      vx = nx-2;
      vy = ny;
      xoffset = 2;
      yoffset = 0;
      dirFound = (vx < 0 || vy < 0 || vx > this.width || vy > this.height) ? false : true;
    }
    if (isVisited(nx+2, ny) && isVisited(nx, ny-2) && isVisited(nx, ny+2) && isVisited(nx-2, ny) ){
      //setVisited(nx, ny);
    }

    //Make the path
    this.makePath(vx, vy, xoffset, yoffset);

    return false;
  }

}

