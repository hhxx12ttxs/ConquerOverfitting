/*
 * Calculator
 * 
 * Version 1.0
 *
 * 05/01/2011
 * 
 * Created by Ashley Heath
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

public class CalculatorObject
{
  private double priorityIncrement_ = 10;
  private String assignmentSymbol_ = "=";
  private String[] numbers_ = {"0","1","2","3","4","5","6","7","8","9"};
  private String[] punctuation_ = {"(",")","."};
  private ArrayList<Operation> avaiableOperatorsList_ = new ArrayList<Operation>();
  private ArrayList<String> allowedInputs_ = new ArrayList<String>();
  private Map<String, String> assignedSymbols_ = new HashMap<String, String>();

  /*
   * Initialize the calculator object. 
   * Adds the numbers and punctuation symbols to the list of allowed
   * inputs.
   */
  public CalculatorObject()
  {
    allowedInputs_.addAll(Arrays.asList(numbers_));
    allowedInputs_.addAll(Arrays.asList(punctuation_));
  }

  /*
   * Initialize the operators to be used by the calculator object. 
   * Adds the defined operators to the list of avaiable operators and
   * adds the relevant operator symbol to the list of allowed inputs.
   */
  public void initializeOperators()
  {
    /*OPERATORS:
    //Name____________Priority_
    //Addition           1
    //Subtraction         3
    //Multiplication     2
    //Division           2
    */
    //Add these operators to the list of avaiable operators
    avaiableOperatorsList_.add(new Addition());
    avaiableOperatorsList_.add(new Subtraction());
    avaiableOperatorsList_.add(new Multiplication());
    avaiableOperatorsList_.add(new Division());
    //Add the operator symbols to the list of allowed inputs
    int avaiableOperatorsListSize = avaiableOperatorsList_.size();
    for (int i = 0; i<avaiableOperatorsListSize; i++)
    {
      Operation tmpOperator = avaiableOperatorsList_.get(i);
      String newAllowedInput = tmpOperator.getOperatorSymbol();
      allowedInputs_.add(newAllowedInput);
    }
  }

  /*
   * Return true if the input string is a valid assignment string.
   *
   * @param  inputString The input fed to the calculator.
   * @return true if the input string is a valid assignment string,
   *         else return false.
   */
  private boolean isAssignment(String inputString)
  {
    int inputStringLength = inputString.length();
    if (inputStringLength>1)
    {
      String firstChar = Character.toString(inputString.charAt(0));
      String secondChar = Character.toString(inputString.charAt(1));
      int allowedInputsSize = allowedInputs_.size();
      String[] tmpArray = new String[allowedInputsSize];
      String[] allowedCharacters = allowedInputs_.toArray(tmpArray);
      boolean firstInAllowed = Functions.aInB(firstChar, allowedCharacters);
      boolean secondIsEquals = secondChar.equals(assignmentSymbol_);
      if (firstInAllowed==false && secondIsEquals)
      {
        return true;
      }
    }
    return false;
  }

  /*
   * Return true if the input string contains symbol to
   * be assigned once only.
   *
   * @param  inputString The input fed to the calculator.
   * @return true if the the input string contains the symbol to
   *         be assigned once only, else return false.
   */
  private boolean assignmentSymbolOccursOnce(String inputString)
  {
    int inputStringLength = inputString.length();
    if (inputStringLength>1)
    {
      String assignmentSymbol = Character.toString(inputString.charAt(0));
      String equationString = inputString.substring(2);
      int equationStringLength = equationString.length();
      for (int i =0; i<equationStringLength; i++)
      {
        String testCharacter = Character.toString(equationString.charAt(i));
        if (testCharacter.equals(assignmentSymbol))
        {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /*
   * Returns the operator that uses the given operator symbol.
   * If no such match is found returns null.
   *
   * @param  givenOperatorSymbol The input fed to the calculator.
   * @return Operation that matches the given symbol, else if
   *         there is no matching symbol return null.
   */
  private Operation matchOperatorSymbol(String givenSymbol)
  {
    int avaiableOperatorsListSize =  avaiableOperatorsList_.size();
    for (int i = 0; i<avaiableOperatorsListSize; i++)
    {
      Operation testOperator = avaiableOperatorsList_.get(i);
      String testSymbol = testOperator.getOperatorSymbol();
      boolean symbolMatchesOperator = givenSymbol.equals(testSymbol);
      if (symbolMatchesOperator)
      {
        return testOperator;
      }
    }
    return null;
  }
  
  /*
   * Inserts any stored calculations into the inputString.
   *
   * @param inputString The calculation string.
   * @returns modifiedString The modifiedString.
   */
  private String insertAssignedEquations(String inputString)
  {
    String modifiedString = inputString;
    Set keySet = assignedSymbols_.keySet();
    String[] tmpArray = new String[keySet.size()];
    String[] keys = (String[])keySet.toArray(tmpArray);
    for (int i = 0; i < keys.length; i++)
    {
      modifiedString = modifiedString.replaceAll(keys[i], assignedSymbols_.get(keys[i]));
    }
    return modifiedString;
  }
  
  /*
   * Checks if the string is a valid calculation and 
   * returns the string "Valid." if it is, else it 
   * returns a string with the appropriate error
   * message.
   *
   * @param inputString The inputString.
   */
  public String validateInputString(String inputString)
  {
    boolean numberFound = false;
    if (inputString.equals(""))//Empty string
    {
      return new String("No input.");
    }
    //Check for assignment
    if (isAssignment(inputString)==true)
    {
      if (assignmentSymbolOccursOnce(inputString)==true)
      {
        if (inputString.length() > 2)
        {
          String assignmentSymbol = Character.toString(inputString.charAt(0));
          inputString = inputString.substring(2);
          assignedSymbols_.put(assignmentSymbol, inputString);
          allowedInputs_.add(assignmentSymbol);
        }
        else
        {
          return new String("You need to assign something.");
        }
      }
      else
      {
        return new String("Cannot reference the assignment symbol in the equation you are assigning to it.");
      }      
    }
    inputString = insertAssignedEquations(inputString);
    //Are all the characters in the string valid inputs?
    String[] tmpArray = new String[allowedInputs_.size()];
    String[] allowedCharacters = allowedInputs_.toArray(tmpArray);
    for (int i = 0; i < inputString.length(); i++)
    {
      String currentChar = Character.toString(inputString.charAt(i));
      if (Functions.aInB(currentChar, allowedCharacters) == false)
      {
        return new String("Invalid Character: " + "\"" + currentChar + "\"");
      }
    }

    //Are all the operators and operands in a valid format
    boolean dealingWithANumber = false;
    int decimalPoints = 0;
    int openBracketCount = 0;
    int closedBracketCount = 0;
    for (int i = 0; i < inputString.length(); i++)
    {
      String currentChar = Character.toString(inputString.charAt(i));
      //If a number
      if (Functions.aInB(currentChar, numbers_)==true)
      {
        dealingWithANumber = true;
        numberFound = true;
      }
      //If a decimal point
      else if (currentChar.equals("."))
      {
        decimalPoints += 1;
        if (decimalPoints > 1 && dealingWithANumber == true)
        {
          return new String("Too many decimal points.");
        }
        else if (dealingWithANumber == false)
        {
          return new String("Decimal points encountered outside of a number.");
        }
      }
      else
      {
        decimalPoints = 0;        
        //If an open bracket
        if (currentChar.equals("("))
        {
          openBracketCount += 1;
          if (i != 0 && matchOperatorSymbol(Character.toString(inputString.charAt(i-1))) == null)
          {
            return new String("No operator found before bracket.");
          }
        }
        //IF A CLOSED BRACKET
        else if (currentChar.equals(")"))
        {
          closedBracketCount += 1;
          if (i+1 != inputString.length())  
          {
            String nextCharacter = Character.toString(inputString.charAt(i+1));
            if (matchOperatorSymbol(nextCharacter) == null
               && nextCharacter.equals(")") == false)
            {
              return new String("No operator found after bracket.");
            }
          }
          if (!dealingWithANumber)
          {
            return new String("No operand found inside brackets.");
          }        
        }
        //If an operator
        else
        {
          dealingWithANumber = false;
          if (matchOperatorSymbol(currentChar) != null)
          {
            Operation operation = matchOperatorSymbol(currentChar);
            int numberOfOperandsNeeded = operation.getNumberOfOperandsRequired();
            //If an unary operator
            if (numberOfOperandsNeeded == 1)
            {
              //If this is the last character
              if (i == inputString.length()-1)
              {
                return new String("Too few operands.");
              }
              //If the next character is not a number or a unary operator
              else if (Functions.aInB(Character.toString(inputString.charAt(i+1)), numbers_) == false
                      && Character.toString(inputString.charAt(i+1)).equals("(")==false
                      && operatorIsUnary(Character.toString(inputString.charAt(i+1))) == false)
              {
                return new String("Too few operands.");
              }    
            }
            //If a binary operator
            else if (numberOfOperandsNeeded == 2)
            {
              //If this is the first or last character
              if (i == 0 || i == inputString.length()-1)
              {
                return new String("Too few operands.");
              }
              //If the next character is not a number or a unary operator
              else if (Functions.aInB(Character.toString(inputString.charAt(i+1)), numbers_) == false
                      && Character.toString(inputString.charAt(i+1)).equals("(")==false
                      && operatorIsUnary(Character.toString(inputString.charAt(i+1))) == false)
              {
                return new String("Too few operands.");

              }
              //If the previous character is not a number
              else if (Functions.aInB(Character.toString(inputString.charAt(i-1)), numbers_) == false
                      && Character.toString(inputString.charAt(i-1)).equals(")")==false)
              {
                return new String("Too few operands.");
              }
            }
          }
        }
      }
    }
    //Do the number of brackets add up?
    if (openBracketCount != closedBracketCount)
    {
      return new String("Brackets do not pair up.");
    }
    if (numberFound == false)
    {
      return new String("No operands found.");
    }
    return new String("Valid.");
  }

  /*
   * Returns true if the operator symbol provided matches a unary
   * operator, else returns false.
   */
  private boolean operatorIsUnary(String operatorSymbolToCheck)
  {
    for (int x = 0; x < avaiableOperatorsList_.size(); x++)
    {
      Operation operator = avaiableOperatorsList_.get(x);
      String operatorSymbol = operator.getOperatorSymbol();
      if (operatorSymbol.equals(operatorSymbolToCheck)
         && operator.getNumberOfOperandsRequired() == 1)
      {
        return true;
      }
    }
    return false;
  }

  /*
   * Returns an ArrayList containing the operator list and the 
   * operand list.
   */
  private ArrayList getOperatorAndOperandLists(String inputString)
  {
    boolean addedNumberLast = false;
    if (isAssignment(inputString)==true)
    {
      inputString = inputString.substring(2);
    }
    inputString = insertAssignedEquations(inputString);
    String priorityIncreaser = "(";
    String priorityDecreaser = ")";

    ArrayList<Operation> operatorList = new ArrayList<Operation>();
    ArrayList<Operand> operandList = new ArrayList<Operand>();
    String currentNumberString = "";
    double priorityIncrement = 10;
    double priorityLevel = 0;
    for (int i = 0; i < inputString.length(); i++)
    {
      String currentCharacter = Character.toString(inputString.charAt(i));
      //Is it a number?
      if (Functions.stringIsInt(currentCharacter))
      {
        currentNumberString += currentCharacter;
      }
      //Is it a decimal point?
      else if (currentCharacter.equals("."))
      {
        int index = currentNumberString.indexOf(".");
        if (index==-1)
        {
          currentNumberString += currentCharacter;
        }
      }
      //If not a number or a decimal point then we have finished a number
      else if (currentNumberString.length() > 0)
      {
        double completeNumber = Double.parseDouble(currentNumberString);
        Operand newOperand = new Operand(completeNumber, priorityLevel);
        operandList.add(newOperand);
        currentNumberString = "";
        addedNumberLast = true;
      }
      //WAS IT THE LAST THING IN THE INPUT STRING
      if (i==inputString.length()-1
         && currentNumberString.equals("")==false)
      {
        double completeNumber = Double.parseDouble(currentNumberString);
        Operand newOperand = new Operand(completeNumber, priorityLevel);
        operandList.add(newOperand);
        currentNumberString = "";
      }
      //Is it a bracket?
      if (currentCharacter.equals(priorityIncreaser))
      {
        priorityLevel += priorityIncrement;
      }
      else if (currentCharacter.equals(priorityDecreaser))
      {
        priorityLevel -= priorityIncrement;
      }
      //Is it an operator?
      for (int x = 0; x < avaiableOperatorsList_.size(); x++)
      {
        Operation operator = avaiableOperatorsList_.get(x).clone();
        String operatorSymbol = operator.getOperatorSymbol();
        if (operatorSymbol.equals(currentCharacter))
        {
          operator.setPriorityIncrease(priorityLevel);
          operatorList.add(operator);
          if (operator.getNumberOfOperandsRequired() == 1)
          {
            operandList.add(new NullOperand());
          }
          break;
        }
      }
      if (addedNumberLast)
      {
        addedNumberLast = false;
      }
    }
    ArrayList<ArrayList> returnContents = new ArrayList<ArrayList>(2);
    returnContents.add(0, operatorList);
    returnContents.add(1, operandList);
    return returnContents;
  }
  
  /*
   * Returns the index of the operator with the highest priority in
   * the operator list.
   *
   * @param operatorList The list of operators in the order they 
   *        occur.
   * @returns indexOfHighestPriority The index of the operator with
   *        the highest priority.
   */
  private int getIndexOfHighestPriority(ArrayList<Operation> operatorList)
  {
    double highestPriority = -1;
    int indexOfHighestPriority = -1;
    for (int i = 0; i < operatorList.size(); i++)
    {
      Operation operation = operatorList.get(i);
      double priority = operation.getPriorityPlusIncrease();
      if (priority > highestPriority)
      {
        highestPriority = priority;
        indexOfHighestPriority = i;
      }
    }
    return indexOfHighestPriority;
  }

  /*
   * Returns the result of the calculation as a double.
   *
   * @param inputString The calculation to be performed presented as
   *        a string.
   */
  public double getAnswer(String inputString) throws OperationException
  {
    ArrayList<ArrayList> lists = getOperatorAndOperandLists(inputString);
    ArrayList<Operation> operatorList = lists.get(0);
    ArrayList<Operand> operandList = lists.get(1);
    while (operatorList.size() != 0)
    {

      //FIND THE OPERATOR WITH THE HIGHEST PRIORITY
      int operatorIndex = this.getIndexOfHighestPriority(operatorList);
      Operation operation = operatorList.get(operatorIndex);
      int operandIndex = operatorIndex;
      operandIndex = operation.getOperandIndex(operatorIndex, operandIndex, operatorList, operandList);
      if (operation.safeToOperate(operatorIndex, operatorIndex, operatorList, operandList))
      {
        ArrayList<ArrayList> operatorAndOperandList = operation.operate(operatorIndex, operandIndex, operatorList, operandList);
        operatorList = operatorAndOperandList.get(0);
        operandList = operatorAndOperandList.get(1);
      }
      else
      {
        String errorMessage = operation.getErrorMessage();
        throw new OperationException(errorMessage);
      }
    }
    Operand lastOperand = operandList.get(0);
    double answer = lastOperand.getOperandValue();
    return answer;
  }
  
  /*
   * Returns a string with either the result of the calculation
   * or the error message it produced.
   */
  public String getResult(String inputString)
  {
    //Catch null input that occurs when Ctrl-D is pressed
    boolean inputIsNull = inputString == null;
    if (inputIsNull)
    {
      return new String("Null Input (Use Ctrl-C to exit).");
    }
    String strippedString = Functions.stripSpacesAndTabs(inputString);
    String validityString = validateInputString(strippedString);
    if (validityString.equals("Valid."))
    {
      try
      {
        double answer = getAnswer(strippedString);
        if (answer == -0.0)
        {
          answer = 0;
        }
        return Double.toString(answer);
      }
      catch (OperationException opException)
      {
        return opException.getErrorMessage();
      }
    }
    else
    {
      return validityString;
    }
  }
}

