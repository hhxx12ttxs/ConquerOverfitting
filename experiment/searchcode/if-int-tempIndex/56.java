import java.util.ArrayList;

/**
 * Thread that fetches and creates the map and players' locations from the
 * history of messages from the server, freeing up the input thread to continue
 * listening for messages.
 */
public class MapBuilder extends Thread
{
  private ClientOutputThread output;
  private ClientGUI gui;
  private ArrayList<String> history;
  private LODCharacter character;
  
  /**
   * Constructor.
   * @param out       the output thread to use to communicate witht the server.
   * @param gui       the gui to call when the map has been built.
   * @param history   the server message history.
   * @param character the character to store the map and locations in.
   */
  public MapBuilder(ClientOutputThread out, ClientGUI gui,
                    ArrayList<String> history, LODCharacter character)
  {
    this.output = out;
    this.gui = gui;
    this.history = history;
    this.character = character;
  }
  
  /**
   * Called to start building a map.
   */
  public void run()
  {
    this.fetchAndCreateMap();
    this.gui.updateMap();
  }

  /**
   * Send the server a LOOK code, wait for the response and then construct a 
   * local copy of the map that can be seen to store in the LODCharacter.
   */
  public void fetchAndCreateMap()
  {
    this.output.sendString("LOOK");
    // Wait for the look reply to arrive
    try
    {
      Thread.sleep(300);
    }
    catch (InterruptedException e)
    {
      System.out.println("WARNING: Could not sleep to wait for lookreply.");
    }
    // Build and store map
    String [][] localMap = this.buildMap();
    while (localMap == null)
    {
      localMap = this.buildMap();
    }
    this.character.setLocalMap(localMap);
    // Add player positions from render hints
    int [][] positions = this.getPlayerPositions();
    if (positions != null)
    {
      this.character.setPlayerRelativePositions(positions);
    }
  }
  
  /**
   * Return the map array created by looking through the communication history.
   * @return the map array of the tiles encoded as Strings.
   */
  public String [][] buildMap()
  {
    String [][] localMap;
    ArrayList<String[]> tempMap = new ArrayList<String[]>(0);
    int i;
    boolean lookReplyFound = false;
    for (i = this.history.size() - 1; i >= 0; i--)
    {
      if (this.history.get(i).equals("LOOKREPLY"))
      { 
        lookReplyFound = true;
        break;
      }
    }
    if (lookReplyFound == false)
    {
      return null;
    }
    // Look through the history to find each line from the LOOKREPLY
    for (int j = i+1; j < this.history.size() - 1; j++)
    {
      String line = this.history.get(j);
      // A blank line signals the end of the LOOKREPLY
      if (line.equals(""))
      {
        break;
      }
      // Ignore keep alive signals in the history
      else if (line.equals("PONG"))
      {
        continue;
      }
      // Otherwise add the line to the map array
      else
      { 
        String [] tempArray = line.split("");
        String [] lineArray = new String [tempArray.length-1];
        // Remove the first character which is blank
        for (int k = 1; k < tempArray.length; k++)
        {
          lineArray[k-1] = tempArray[k];
        }        
        tempMap.add(lineArray);
      }
    }
    localMap = new String [tempMap.size()][];
    int tempIndex = 0;
    for (String [] line : tempMap)
    {
      localMap[tempIndex] = line;
      tempIndex++;
    }
    return localMap; 
  } 
  
  /**
   * Search the communications history and parse the positions of players on the
   * local map from the render hints.
   * @return an array of pairs of relative coordinates.
   */
  public int [][] getPlayerPositions()
  {    
    int [][] positions = null;
    int numberOfHints = 0;
    int i;
    boolean renderHintFound = false;
    for (i = this.history.size() - 1; i >= 0; i--)
    {
      String line = this.history.get(i);
      if (line.indexOf("RENDERHINT ") == 0)
      { 
        renderHintFound = true;
        try
        {
          numberOfHints = Integer.parseInt(line.substring(11, line.length()));
          positions = new int [numberOfHints][];
        }
        catch (NumberFormatException e)
        {
          System.out.println("ERROR: Recieved malformed RENDERHINT.");
          return null;
        }
        break;
      }
    }
    if (renderHintFound == false)
    {
      return null;
    }
    int hintsToGo = numberOfHints;
    while (hintsToGo > 0)
    {
      i++;
      String line = this.history.get(i);
      if (line.equals(""))
      {
        break;
      }
      else if (line.equals("PONG"))
      {
        continue;
      }
      else
      { 
        String [] hint = line.split(" "); // Split the line by whitespace
        int x;
        int y;
        try
        {
          x = Integer.parseInt(hint[0]);
          y = Integer.parseInt(hint[1]);
          // Handle the different types of hints - right now, just other players
          if (hint[2].equals("PLAYER"))
          {
            positions[numberOfHints-hintsToGo] = new int [] {x, y};
          }
          hintsToGo--;
        }
        catch (NumberFormatException e)
        {
          System.out.println("ERROR: Recieved malformed RENDERHINT.");
        } 
      }
    }
    return positions; 
  }
}

