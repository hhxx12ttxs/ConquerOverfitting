package maize;

/** Creates a more twisty maze by using a fully random Depth First Search.
	* @see DFSMazeFactory
	*/
public class FullDFSMazeFactory extends DFSMazeFactory {

	/** Default constructor.
		*/
	public FullDFSMazeFactory(){
		super();
	}

  /** Makes a path in one direction for one square.
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

		/* Visit next room */
		this.setVisited(x + xoffset, y + yoffset);

    /* Set two rooms neighbours, n, e, s, w */
    this.setNeighbour(x, y-2);
    this.setNeighbour(x+2, y);
    this.setNeighbour(x, y+2);
    this.setNeighbour(x-2, y);
		
		x+=xoffset;
		y+=yoffset;

    this.setNeighbour(x, y-2);
    this.setNeighbour(x+2, y);
    this.setNeighbour(x, y+2);
    this.setNeighbour(x-2, y);
  }

}

