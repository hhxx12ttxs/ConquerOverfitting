/** This class is designed to create mazes for dungeoncrawler
 *  @author Daniel Th端men   4523176 Gruppe 7a
 *  @author Philipp Bartsch 4569839 Gruppe 7a
 */
class RecursiveBacktracker implements MazeGenerator {
    
    /** CONSTANT */
    private static final int NONE  = 0;
    /** CONSTANT */
    private static final int NORTH = 1;
    /** CONSTANT */
    private static final int EAST  = 2;
    /** CONSTANT */
    private static final int SOUTH = 4;
    /** CONSTANT */
    private static final int WEST  = 8;

    /** attribute to save generation time, may be useful for optimisation */
    private long time = 0;  

    /** method to handle the overall generation process
     * @param width width of the maze to be generated
     * @param height height of the maze to be generated
     * @throws IllegalArgumentException in case the
     *      dimensions are fucked up
     * @return simple char array that contains a map..
     */
    public char[][] generate(int width, int height) 
        throws IllegalArgumentException {
        if (height <= 2 || width <= 2) {
            throw new IllegalArgumentException("min. size 3x3");
        }   
        
        int pos = (int) (Math.random() * width * height);

        /* initialize map */
        int[] map = new int[width * height];
        for ( int i = 0; i < map.length; i++ ) {
            map[i] = NONE;
        }

        // calling recursive function here
        long timeBeg = System.currentTimeMillis();
    
        // This may lead to an error...
        map = this.recurBcktrck(map, pos, width);
        
        long timeFin = System.currentTimeMillis();

        this.time = timeFin - timeBeg;

        // printMap(map, width); // uncomment to view raw data
        
        char[][] charmap = this.mapToChar(map, width, height);
        return charmap;
    }

    /** getter for time(in millis) needed to generate raw mapdata
     * @return time
     */
    public long getTime() {
        return this.time;
    }

    /** method to get a char map out of the raw map data
     * @param map raw mapdata
     * @param width width of map
     * @param height height of map
     * @return char-map, ready to be used
     */
    private char[][] mapToChar(int[] map, int width, int height) {
        int mx = width * 2 + 1;
        int my = height * 2 + 1;

        int x, y;
        int directions;

        char[][] cmap = new char[mx][my];


        for (int i = 0; i < my; i++) {
            for (int j = 0; j < mx; j++) {
                cmap[j][i] = WALL;
            }
        }

        int j = 0;
        for (int i = 0; i < map.length; i++ ) {
            x = ((i % width) * 2 + 1);
            y = (j * 2 + 1);
            cmap[x][y] = this.directionsToChar(map[i]);
        
            if (this.containsDirection(map[i], NORTH) ) {
                cmap[x][y - 1] = this.getNormalChar();
            }
            
            if (this.containsDirection(map[i], EAST) ) {
                cmap[x + 1][y] = this.getNormalChar();
            }

            if (this.containsDirection(map[i], SOUTH) ) {
                cmap[x][y + 1] = this.getNormalChar();
            }

            if (this.containsDirection(map[i], WEST) ) {
                cmap[x - 1][y] = this.getNormalChar();
            }


            if ((i + 1) % width == 0) {
                j++;
            }
        }


        cmap[mx - 2][my - 2] = GOAL;
        cmap[1][1] = START;

        return cmap;
    }

    /** simple randomization for 'normal fields' 
     * @return a char, that represents a specific type of field
     */
    private char getNormalChar() {
        if (Math.random() < 0.031337) {
            return SMITHY;
        } else {
            return FREE;
        }
    }

    /** simple randomization for fields, where 3 ways are joinded
     * @param directions directions in which paths go out
     * @return a char, that represents a specific type of field
     */
    private char directionsToChar(int directions) {
        if (   this.containsDirection(directions, WEST + SOUTH + EAST) 
            || this.containsDirection(directions, NORTH + SOUTH + WEST)
            || this.containsDirection(directions, NORTH + EAST + WEST)
            || this.containsDirection(directions, NORTH + EAST + SOUTH)) {
            
            if (Math.random() < 0.85) {
                return BATTLE;
            } else {
                return WELL;
            }
        }
        
        return FREE;
    }

    /** recursive method, that is supposed to generate a maze, in which 
     * all fields are connected to each other.
     * @param map raw mapdata
     * @param pos current position on map
     * @param width width of map
     * @return raw mapdata with maze
     */
    private int[] recurBcktrck(int[] map, int pos, int width) {
        

        int freeDirections = this.getFreeDirections(map, pos, width); 
        if (freeDirections != NONE) {
            //System.out.println("FREE DIRECTIONS AVAILABLE");
            
            int direction = this.getOneDirection(freeDirections);
            
            //System.out.println("direction:" + direction);

            int newpos = this.moveDirection(pos, direction, width);

            //System.out.println("next position: " + newpos);

            // add direction to current
            map[pos] = this.addDirectionToDirections(
                map[pos], 
                direction);
            // add direction where we came from to new position
            map[newpos] = this.addDirectionToDirections(
                map[newpos], 
                this.reverseDirection(direction));

            // repeat until all free fields are gone
            while (this.getFreeDirections(map, newpos, width) != NONE) {
                this.recurBcktrck(map, newpos, width);
            }

        } else {
            // never happens
            System.out.println("WE NEED TO GO BACK");
        }

        return map;
    }

    /** method to reverse a direction
     * @param direction direction to be reversed
     * @return the other way 'round...
     */
    private int reverseDirection(int direction) {
        switch (direction) {
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case SOUTH:
                return NORTH;
            case WEST:
                return EAST;
            default:
                return NONE;
        }
    }


    /** method to choose one of the given directions by random
     * @param directions directions to be choosen from
     * @return the one and only choosen direction
     */
    private int getOneDirection(int directions) {
        int[] dirarray = new int[4];

        int c = 0;

        if (this.containsDirection(directions, NORTH) ) {
            dirarray[c] = NORTH;
            c++;
        }

        if (this.containsDirection(directions, EAST) ) {
            dirarray[c] = EAST;
            c++;
        }

        if (this.containsDirection(directions, SOUTH) ) {
            dirarray[c] = SOUTH;
            c++;
        }

        if (this.containsDirection(directions, WEST) ) {
            dirarray[c] = WEST;
            c++;
        }

        if (c > 0) {
            int rnd = (int) (Math.random() * c);
            return dirarray[rnd];
        } else {
            return 0;
        }
    }

    /** checking whether or not the direction is contained in directions
     * @param directions base directions
     * @param direction direction to check for
     * @return wether or not direction is in directions
     * */
    private boolean containsDirection(int directions, int direction) {
        
        if ((direction & directions) == direction) {  // bitwise
            return true;
        } else {
            return false;
        }
    }

    /** adding a direction to possibly multiple directions represented
     * as one integer, while not adding one direction multiple times
     * @param directions base directions that may be extended by direction
     * @param direction direction to be added to directions
     * @return directions after they have been added
     */
    private int addDirectionToDirections(int directions, int direction) {
        if (!this.containsDirection(directions, direction) ) {
            return directions + direction;
        } else {
            return directions;
        }
    }


    /** calculates directions of free fields next to the specified one
     * @param map raw mapdata
     * @param pos current position on map
     * @param width width of map
     * @return freedirections added in one int
     */
    private int getFreeDirections(int[] map, int pos, int width) {
        
        // uncomment to see which position gets a query and
        // what results are returned
        //System.out.print("getFreeNeighbours() - pos: " + pos);
        
        int directions = 0;
        
        // NORTH
        if (pos - width > 0 && map[pos - width] == 0) {
            //System.out.print(" - NORTH");
            directions += NORTH;
        }
        
        // EAST
        if (pos % width < (width - 1) && map[pos + 1] == 0) {
            //System.out.print(" - EAST");
            directions += EAST;
        }

        // SOUTH
        if (pos + width < map.length && map[pos + width] == 0) {
            //System.out.print(" - SOUTH");
            directions += SOUTH;
        }

        // WEST
        if ( pos % width > 0 && map[pos - 1] == 0 ) {
            //System.out.print(" - WEST");
            directions += WEST;
        }
        
        //System.out.println("");

        return directions;
    }

    /** method to move in a specific  direction
     * @param pos old position
     * @param direction what direction to shift
     * @param width widht of map
     * @return position modified by direction
     */
    private int moveDirection(int pos, int direction, int width) {

        switch(direction) {
            case NORTH:
                return pos - width;
            case EAST:
                return pos + 1;
            case SOUTH:
                return pos + width;
            case WEST:
                return pos - 1;
            default:
                // this is supposed to never happen
                return pos;
        }
    }

    /** debugging function that is capable of printing a raw map
     * @param map raw mapdata
     * @param width width of map
     */
    private void printMap(int[] map, int width) {
        for (int i = 0; i < map.length; i++ ) {
            System.out.print("\t" + map[i]);
            if ( (i + 1) % width == 0 ) {
                System.out.println("");
            }
        }
        System.out.println("");
    }

    /** main function for debugging only
     * @param args commandline parameters
     */
    public static void main(String[] args) {
        MazeGenerator gen = new RecursiveBacktracker();
        char[][] map = gen.generate(14, 11);
        

        for (int i = 0; i < map[0].length; i++) {
            for (int j = 0; j < map.length; j++) {
                System.out.print(map[j][i]);
            }
            System.out.println("");
        }

        System.out.println("\nraw mapdata generated in: " 
            + gen.getTime() + "ms");

    }
}

