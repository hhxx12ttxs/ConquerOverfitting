package builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import exceptions.IdentifierAlreadyInUseException;
import exceptions.InvalidOutputModuleException;
import exceptions.InvalidStatementException;
import gui.GUI;

import view.OutputModule;
import view.MarkVirtual;
import view.Virtual;

/**
 * Stores and executes a Dancer Program.
 */
public class DancerProgram
{
  public Map<String, Macro> macros;
  public Map<String, DancerStatement> colors;
  public OutputModule outputModule;
  private IDGenerator idGenerator;
  private DancerStatementList program;
  private Map<Long, DancerStatement> statementIDs;
  private boolean isThreadSleeping;
  private long sleepingThreadID;
  private Object sync;
  public Virtual world;
  
  /**
   * Creates an empty program.
   */
  public DancerProgram()
  {
    sync = new Object();
    macros = new HashMap<String, Macro>();
    outputModule = null;
    idGenerator = new IDGenerator();
    program = new DancerStatementList(this, idGenerator, sync);
    statementIDs = new HashMap<Long, DancerStatement>();
    colors = new HashMap<String, DancerStatement>();
    outputModule = null;
    isThreadSleeping = false;
    sleepingThreadID = 0;
  }

  /**
   * Decides whether to output to Finch or Virtual.
   * 
   * @param method Chooses what output module to use when running 
   *  commands. Either “Finch” or “Virtual”
   * @throws InvalidOutputModule if method is not “Finch”, “Virtual”
   */
  public void outputMethod(OutputModule outputModule) throws InvalidOutputModuleException
  {
    this.outputModule = outputModule;
  }

  /**
   * Runs through the entire program.
   */
  public void run(InstructorProgram ip)
  {
    System.out.println("Running program. DP. run.");
    for (DancerStatement statement : program)
    {
      System.out.println("Evaluating statement.");
      statement.eval(false);
    }
    if (outputModule instanceof MarkVirtual)
    {
      ((MarkVirtual)outputModule).done(ip);
    }
  }
  
  /**
   * Steps one more step through the program.
   *
   * TODO: throw exception
   * @throws EndOfProgram if at end of program
   */
  public boolean step() //throws EndOfProgramException
  {
    if (isThreadSleeping)
    {
      // Wake up sleeping statement
      synchronized (sync)
      {
        sync.notifyAll();
      }
    }
    else
    {
      Thread stepper = new Thread(new DancerStepper(program));
      stepper.start();
      isThreadSleeping = true;
    }
    try {
      Thread.sleep(1*1000);
    } catch (InterruptedException e) {
    }
    return true;
  }

  /**
   * Tells whether or not it is possible to take another step in the 
   * Program. IE When step() gets to the end of the program, there 
   * are no more steps.
   * TODO: Implement this
   */
  public boolean hasSteps()
  {
    return true;
  }
  
  public OutputModule getOutputModule()
  {
    return outputModule;
  }
  
  /*
  public GUI getGUI()
  {
    return gui;
  }
  */
  
  public void helper(boolean value)
  {
    GUI.helper(value);
  }
  
  public void setProgram(ArrayList<String> programList) throws IdentifierAlreadyInUseException, InvalidStatementException
  {
    int index = 0;
    DancerStatementAndNextIndex dsani = null;
    while (index < programList.size())
    {
      dsani = addStatement(programList, index);
      program.appendStatement(dsani.dancerStatement);
      index = dsani.nextIndex;
    }
  }
  
  private DancerStatementAndNextIndex addStatement(ArrayList<String> program, int startIndex) throws IdentifierAlreadyInUseException, InvalidStatementException
  {
    String statement = program.get(startIndex);
    String firstWordInStatement = null;
    int spaceIndex = statement.indexOf(" ");
    if (spaceIndex == -1)
      firstWordInStatement = statement;
    else
      firstWordInStatement = statement.substring(0, spaceIndex);
    if (firstWordInStatement.equals("while"))
      return addWhile(program, startIndex);
    else if (firstWordInStatement.equals("if"))
      return addIf(program, startIndex);
    else if (firstWordInStatement.equals("repeat"))
      return addRepeat(program, startIndex);
    else if (firstWordInStatement.equals("define"))
      return addDefine(program, startIndex);
    else if (firstWordInStatement.equals("set"))
      return addSet(program, startIndex);
    else if (firstWordInStatement.equals("go_forward") || firstWordInStatement.equals("go_backward") || firstWordInStatement.equals("rotate_left") || firstWordInStatement.equals("rotate_right"))
      return addMovementCommand(program, startIndex);
    else if (firstWordInStatement.equals("perform"))
      return addPerform(program, startIndex);
    else if (macros.containsKey(firstWordInStatement))
      return addMacro(program, startIndex);
    else
      throw new InvalidStatementException(firstWordInStatement + " is not a good thing.");
  }
  
  /**
   * Adds a while statement to the program
   * @return The index in program after "endwhile"
   * @throws IdentifierAlreadyInUseException 
   * @throws InvalidStatementException 
   */
  private DancerStatementAndNextIndex addWhile(ArrayList<String> program, int startIndex) throws IdentifierAlreadyInUseException, InvalidStatementException
  {
    DancerStatementAndNextIndex dsani = null;
    While theWhile = new While(this, idGenerator, sync);
    // Work on the condition line
    String[] whileLine = program.get(startIndex).split(" ");
    if (whileLine.length == 2)
    {
      theWhile.setPrimTest(generatePrimTest(whileLine[1]));
    }
    else
    {
      if (whileLine[2].equals("and"))
      {
        And theCondition = new And(this);
        theCondition.setLeft(generatePrimTest(whileLine[1]));
        theCondition.setRight(generatePrimTest(whileLine[3]));
        theWhile.setPrimTest(theCondition);
      }
      else //if(whileLine[2].equals("or"))
      {
        Or theCondition = new Or(this);
        theCondition.setLeft(generatePrimTest(whileLine[1]));
        theCondition.setRight(generatePrimTest(whileLine[3]));
        theWhile.setPrimTest(theCondition);
      }
    }
    // Work on the body
    int index = startIndex + 1;
    while (!program.get(index).equals("endWhile"))
    {
      DancerStatementAndNextIndex tempDSANI = addStatement(program, index);
      index = tempDSANI.nextIndex;
      theWhile.appendStatement(tempDSANI.dancerStatement);
    }
    index++;
    dsani = new DancerStatementAndNextIndex(theWhile, index);
    return dsani;
  }
  
  /**
   * Adds an if statement to the program
   * @return The index in program after "end_if"
   * @throws IdentifierAlreadyInUseException 
   * @throws InvalidStatementException 
   */
  private DancerStatementAndNextIndex addIf(ArrayList<String> program, int startIndex) throws IdentifierAlreadyInUseException, InvalidStatementException
  {
    DancerStatementAndNextIndex dsani = null;
    If theIf = new If(this, idGenerator, sync);
    // Work on the condition line
    String[] ifLine = program.get(startIndex).split(" ");
    if (ifLine.length == 2)
    {
      theIf.setPrimTest(generatePrimTest(ifLine[1]));
    }
    else
    {
      if (ifLine[2].equals("and"))
      {
        And theCondition = new And(this);
        theCondition.setLeft(generatePrimTest(ifLine[1]));
        theCondition.setRight(generatePrimTest(ifLine[3]));
        theIf.setPrimTest(theCondition);
      }
      else //if (ifLine[2].equals("or"))
      {
        Or theCondition = new Or(this);
        theCondition.setLeft(generatePrimTest(ifLine[1]));
        theCondition.setRight(generatePrimTest(ifLine[3]));
        theIf.setPrimTest(theCondition);
      }
    }
    // Work on the body
    int index = startIndex + 1;
    while (!program.get(index).equals("endIf") && !program.get(index).equals("else"))
    {
      DancerStatementAndNextIndex tempDSANI = addStatement(program, index);
      index = tempDSANI.nextIndex;
      theIf.appendStatement(tempDSANI.dancerStatement);
    }
    if (program.get(index).equals("else"))
    {
      Else theElse = new Else(this, idGenerator, sync);
      index++;
      while (!program.get(index).equals("endElse"))
      {
        DancerStatementAndNextIndex tempDSANI = addStatement(program, index);
        index = tempDSANI.nextIndex;
        theElse.appendStatement(tempDSANI.dancerStatement);
      }
      theIf.setElse(theElse);
      index++;
    }
    // index is now at endif
    index++;
    dsani = new DancerStatementAndNextIndex(theIf, index);
    return dsani;
  }
  
  /**
   * Adds a repeat statement to the program
   * @return The index in program after "endRepeat"
   * @throws IdentifierAlreadyInUseException 
   * @throws InvalidStatementException 
   */
  private DancerStatementAndNextIndex addRepeat(ArrayList<String> program, int startIndex) throws IdentifierAlreadyInUseException, InvalidStatementException
  {
    DancerStatementAndNextIndex dsani = null;
    Repeat theRepeat = new Repeat(this, idGenerator, sync);
    int timesToRepeat;
    // Work on the first line
    String[] repeatLine = program.get(startIndex).split(" ");
    timesToRepeat = Integer.parseInt(repeatLine[1]);
    theRepeat.setTimesToRepeat(timesToRepeat);
    // Work on the body
    int index = startIndex + 1;
    while (!program.get(index).equals("endRepeat"))
    {
      DancerStatementAndNextIndex tempDSANI = addStatement(program, index);
      index = tempDSANI.nextIndex;
      theRepeat.appendStatement(tempDSANI.dancerStatement);
    }
    index++;
    dsani = new DancerStatementAndNextIndex(theRepeat, index);
    return dsani;
  }
  
  private DancerStatementAndNextIndex addDefine(ArrayList<String> program, int startIndex) throws IdentifierAlreadyInUseException, InvalidStatementException
  {
    DancerStatementAndNextIndex dsani = null;
    Define theDefine = new Define(this, idGenerator, sync);
    // Work on the first line
    String[] repeatLine = program.get(startIndex).split(" ");
    if (macros.containsKey(repeatLine[1]))
      throw new exceptions.IdentifierAlreadyInUseException(repeatLine[1] + " is already an identifier.");
    // Work on the body and enter the new macro into the macro set
    Macro theMacro = new Macro(this, idGenerator, sync);
    int index = startIndex + 1;
    while (!program.get(index).equals("endDefine"))
    {
      DancerStatementAndNextIndex tempDSANI = addStatement(program, index);
      index = tempDSANI.nextIndex;
      theMacro.appendStatement(tempDSANI.dancerStatement);
    }
    macros.put(repeatLine[1], theMacro);
    index++;
    dsani = new DancerStatementAndNextIndex(theDefine, index);
    return dsani;
  }
  
  private DancerStatementAndNextIndex addSet(ArrayList<String> program, int startIndex) throws IdentifierAlreadyInUseException, InvalidStatementException
  {
    DancerStatementAndNextIndex dsani = null;
    Set theSet = new Set(this, idGenerator, sync);
    // Work on the first line
    String[] repeatLine = program.get(startIndex).split(" ");
    if (colors.containsKey(repeatLine[1]))
      throw new exceptions.IdentifierAlreadyInUseException(repeatLine[1] + " is already set.");
    // Work on the body and enter the new macro into the macro set
    DancerStatementAndNextIndex tempDSANI = addStatement(program, startIndex + 1);
    DancerStatement theDancerStatement = tempDSANI.dancerStatement;
    int index = tempDSANI.nextIndex;
    colors.put(repeatLine[1], theDancerStatement);
    index++;
    dsani = new DancerStatementAndNextIndex(theSet, index);
    return dsani;
  }
  
  private DancerStatementAndNextIndex addMovementCommand(ArrayList<String> program, int startIndex)
  {
    DancerStatementAndNextIndex dsani = null;
    MovementCommand theMovementCommand = new MovementCommand(this, idGenerator, sync);
    // Make the MovementComand
    String[] repeatLine = program.get(startIndex).split(" ");
    theMovementCommand.setCommand(repeatLine[0]);
    // Output it
    int index = startIndex + 1;
    dsani = new DancerStatementAndNextIndex(theMovementCommand, index);
    return dsani;
  }
  
  private DancerStatementAndNextIndex addPerform(ArrayList<String> program, int startIndex)
  {
    DancerStatementAndNextIndex dsani = null;
    Perform thePerform = new Perform(this, idGenerator, sync);
    // Make the MovementComand
    String[] performLine = program.get(startIndex).split(" ");
    // Handle the simple perform specific_color case
    if (!performLine[1].equals("instructors_colors"))
      thePerform.getColors().add(performLine[1]);
    // Handle the instructors_colors case
    else //if (performLine[1].equals("instructors_colors"))
    {
      for (String color : outputModule.getColors())
        thePerform.getColors().add(color);
    }
    // Output it
    int index = startIndex + 1;
    dsani = new DancerStatementAndNextIndex(thePerform, index);
    return dsani;
  }
  
  private DancerStatementAndNextIndex addMacro(ArrayList<String> program, int startIndex)
  {
    DancerStatementAndNextIndex dsani = null;
    Macro theMacro;
    // Make the Macro, well, we already have it
    String[] macroLine = program.get(startIndex).split(" ");
    theMacro = macros.get(macroLine[0]);    
    // Output it
    int index = startIndex + 1;
    dsani = new DancerStatementAndNextIndex(theMacro, index);
    return dsani;
  }
  
  private PrimTest generatePrimTest(String primTestType)
  {
    if (primTestType.equals("is_beak_up"))
      return new IsBeakUp(this);
    else if (primTestType.equals("is_beak_not_up"))
      return new IsBeakNotUp(this);
    else if (primTestType.equals("is_obstacle_on_left"))
      return new IsObstacleOnLeft(this);
    else if (primTestType.equals("is_obstacle_not_on_left"))
      return new IsObstacleNotOnLeft(this);
    else if (primTestType.equals("is_obstacle_on_right"))
      return new IsObstacleOnRight(this);
    else //if (primTestType.equals("is_obstacle_not_on_right"))
      return new IsObstacleNotOnRight(this);
  }
}
