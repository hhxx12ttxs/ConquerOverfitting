 import java.util.*;

public class Maze {

  // FILE NAME: Maze.java
  // WRITTEN BY: Cassie Hoef and Polina Soshnin 
  // WHEN: October 18, 2012 

  // PURPOSE: new class to implement maze search using a Stack to store
  // the current path as the search proceeds

  // MODIFICATION HISTORY: Modified March 9, 2013

  
  int[][] mazeArr;       // current pattern
  int[][] visitArr;      // update pattern using 1s to signal visited 0s
  Stack<Place> storeMaze; //stores the current path of the 
  

  public Maze(int[][] maze) {
    mazeArr=maze;
    storeMaze= new Stack<Place>();
    visitArr=maze;
    
  }
  // *** insert code to search the maze from start to goal locations
    
  /**
   *searchMaze takes the starting indices and ending indices of a 2D
   *array and traverses them. If there is a complete path of 0s, it returns
   *a printout of that path with 8s replacing the zeros.
   *
   *This method starts checking 
   * 
   */
  public void searchMaze(int startrow, int startcol, int endrow, int endcol) { 
    
    Place currentLoc= new Place(startrow,startcol); //starting place
    
    //Place currentLoc= startLoc;//current place
    Place endLoc= new Place(endrow,endcol);//ending place
    storeMaze.push(currentLoc); //push first location
    
    /*The while loop lasts as long as the current location is not the same as the end 
     location and the stack is not empty. In the while loop, we check each direction for zeros 
     and if they are zeros, then we move forward through the maze, setting the next location 
     to our current location. Every visited location gets a value of 1. When the current location
     is surrounded by ones, it pops the stack and goes backwards through visited locations until 
     either another zero is found or the stack is empty.*/
    
    while((!currentLoc.equals(endLoc))&&(!storeMaze.empty())) {
      //for each true condition of checkBlah, the visitArr gets a value of one at the current location
      //it then gets the next closes zero as it's current value
      //as you can tell from our comments, we used many printlns to debug
      if(checkEast(startrow, startcol, endcol)){
        
        //System.out.println("East is true");
        //visit array gets our current loc and sets it to one in array
        visitArr[currentLoc.getRow()][currentLoc.getCol()] = 1;
        startcol++;
        currentLoc=new Place(startrow, startcol);
        // System.out.println(currentLoc);
        storeMaze.push(currentLoc);      
        
      }
      else if(checkSouth(startrow, startcol, endrow)){
        // System.out.println("south is true");
        visitArr[currentLoc.getRow()][currentLoc.getCol()] = 1;
        startrow++;
        currentLoc=new Place(startrow, startcol);
        //System.out.println(currentLoc);
        storeMaze.push(currentLoc);
      }
      else if(checkWest(startrow, startcol)){
        //System.out.println("west is true");
        visitArr[currentLoc.getRow()][currentLoc.getCol()] = 1;
        startcol--;
        currentLoc=new Place(startrow, startcol);
        //System.out.println(currentLoc);
        storeMaze.push(currentLoc);
      }
      else if(checkNorth(startrow, startcol)){
        //System.out.println("north is true");
        visitArr[currentLoc.getRow()][currentLoc.getCol()] = 1;
        startrow--;
        currentLoc=new Place(startrow, startcol);
        //System.out.println(currentLoc);
        storeMaze.push(currentLoc);
      }
      
      else {
        //We pop the current location off the stack, and give current location the previous location
        //We update startrow and startcol so that they are consistant with the values in the other conditionals
        // System.out.println("popped");
        storeMaze.pop();      
        visitArr[currentLoc.getRow()][currentLoc.getCol()] = 1;      
        
        if(!storeMaze.empty()) {
          currentLoc=storeMaze.peek();
          startrow= currentLoc.getRow();
          startcol= currentLoc.getCol();   
        }
        
      }}
    if(storeMaze.empty()) {
      System.out.println("You're lost forever in the maze!");
    }
    if(endLoc.equals(currentLoc)){
      
      showPath(storeMaze);
    }           
  }   
  //list of helper methods
  //each boolean checks one direction for zeros
  public boolean checkEast(int i, int j, int endcol ) {    
    if((j+1)<=endcol){
      if (visitArr[i][j+1]==0) {    
        return true;
      }              
    }
    return false;
  }
  
  public boolean checkSouth(int i, int j, int endrow) {
    //System.out.println("checking south right now");
    //System.out.println(endrow);
    //System.out.println(i);
    //System.out.println(j);
    if((i+1)<=endrow){
      if (visitArr[i+1][j]==0) {
        return true;
      }            
    }
    return false; 
  } 
  public boolean checkWest(int i, int j) {
    
    if((j-1)>=0) {
      if  (visitArr[i][j-1]==0) { 
        return true;            
      } 
    } 
    return false; 
    
  }
  public boolean checkNorth(int i, int j) {      
    if((i-1)>=0){
      if (visitArr[i-1][j]==0) {
        return true;
      }}
    return false;      
  }
  
  public static void printArray (int[][] arr) {
    // prints the contents of a 2D square array, with labels on the
    // rows and columns
    int i, j; 
    System.out.println("array contents:");
    System.out.print("col");
    for (i = 0; i < arr.length; i++)
      System.out.print("  " + i);
    System.out.println();
    System.out.println("row");
    for (i = 0; i < arr.length; i++) {
      System.out.print(" " + i + "   ");
      for (j = 0; j < arr.length; j++)
        System.out.print(arr[i][j] + "  ");
      System.out.println();
    }
  }
  
  public void showPath (Stack<Place> stk) {
    // prints path locations on the input stack, and displays final path 
    // superimposed on the maze array
    int size = mazeArr.length;
    int[][] display = new int[size][size];
    int i, j;
    for (i = 0; i < size; i++)    // copy mazeArr into display array
      for (j = 0; j < size; j++)
      display[i][j] = mazeArr[i][j];
    Stack<Place> tempStack = new Stack<Place>();
    while (!stk.empty()) {   // flip stack contents onto tempStack
      tempStack.push(stk.pop());
    }
    Place P;  // print path and show path in display array
    System.out.println("Path on the Stack from start to goal: ");
    while (!tempStack.empty()) {
      P = tempStack.pop();
      System.out.println(P);
      display[P.getRow()][P.getCol()] = 8;     
      stk.push(P);      // restores the original stack
    }
    System.out.println("final path:");
    printArray(display);
  }
  
}


