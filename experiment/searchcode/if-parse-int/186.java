  /*
   *
   * Begin of Skeleton
   *
   */

  /* ****************************************************************
    Java Skeleton Parser for matrix tables

    This is not a sample program, but rather the parser skeleton
    to be included in the generated code.
    Modify at your own risk.

    Copyright (c), 1985-2012 Jaime Garza
  ***************************************************************** */

  private static final int ERROR_FAIL = 0;
  private static final int ERROR_RE_ATTEMPT = 1;

  private int    stateStack[] = new int[STACK_DEPTH];
  int            state;
  StackElement   lexicalValue;
  int            lexicalToken;
  int            errorCount;
  int            errorFlag;
  boolean        verbose = false;

  /**
   * Change the verbose flag
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Obtain the verbose flag
   */
  public boolean isVerbose() {
    return this.verbose;
  }

  /**
   * This routine maps a state and a token to a new state on the action table
   * @param state is the current state
   * @param symbol is the given symbol to find (if not found, defa will be used
   */
  private int parserAction(int state, int symbol) {
    int index = getTokenIndex(symbol);
    return parsingTable[state][index];
  }

  /**
   * This routine maps a origin state to a destination state
   * using the symbol position
   * @param state is the current state
   *@param non terminal that causes the transition
   */
  private int parserGoto(int state, int symbol) {
    int index = symbol;
    return parsingTable[state][index];
  }

  /**
   * This routine prints the contents of the parsing stack
   */
  private void parserPrintStack() {
    int i;

    System.out.print("States: [");
    for(i=0;i<=stackTop;i++) {
      System.out.print(stateStack[i] + " ");
    }
    System.out.println("<--Top Of Stack (" + stackTop + ")");
    System.out.print("Values: [");
    for(i=0;i<=stackTop;i++) {
      System.out.print("|" + (stack[i] != null ? stack[i].toString() : "(nothing)") + "| ");
    }
    System.out.println("<--Top Of Stack (" + stackTop + ")\n");
  }

  /**
   * Does a shift operation.  Puts a new state on the top of the stack
   * @param sym is the symbol causing the shift
   * @param state is the current state
   */
  private int parserShift(int sym, int state) {
    if(stackTop >= STACK_DEPTH-1) {
      return 0;
    }

    stateStack[++stackTop] = state;
    stack[stackTop] = lexicalValue;
    this.state = state;
    if (isVerbose()) {
      System.out.println("Shift to " + state + " with " + sym);
      parserPrintStack();
    }
    return 1;
  }

  /**
   * Recognizes a rule an removes all its elements from the stack
   * @param sym is the symbol causing the shift
   * @param rule is the number of rule being used
   */
  int parserReduce(int sym, int rule) {
    if (isVerbose()) {
      System.out.println("Reduce on rule " + rule + " with symbol " + sym);
    }
    if(generateCode(rule) == false) {
      return 0;
    }
    stackTop -= grammarTable[rule].reductions;
    stateStack[stackTop+1] =
        parserGoto(stateStack[stackTop], grammarTable[rule].symbol);
    state = stateStack[++stackTop];
    if (isVerbose()) {
        parserPrintStack();
    }
    return 1;
  }

  /**
   * Get the error message for a state
   */
  private String getErrorMessage() {
    int msgIndex = parsingError[state];
    if (msgIndex >= 0) {
      return errorTable[msgIndex];
    } else {
      return "Syntax error on state " + state + " with token " + getTokenName(lexicalToken);
    }
  }

  /**
   * Recover from a syntax error removing stack states/symbols, and removing
   * input tokens.  The array StxRecover contains the tokens that bound
   * the error
   */
  private int parserRecover() {
    int i, acc;

    switch(errorFlag) {
      case 0: // 1st error
        if(parserError(state, lexicalToken, stackTop, getErrorMessage()) == 0) {
          return 0;
        }
        errorCount++;
        // continues and goes into 1 and 2.  No break on purpose

      case 1:
      case 2: // three attempts are made before dropping the current token
        errorFlag = 3; // Remove token

        while(stackTop > 0) {
          // Look if the state on the stack's top has a transition with one of
          // the recovering elements in StxRecoverTable
          for (i=0; i<RECOVERS; i++) {
            if((acc = parserAction(state, recoverTable[i])) > 0) {
              // valid shift
              return parserShift(recoverTable[i], acc);
            }
          }
          if (isVerbose()) {
            System.out.println("Recuperate removing state " + state + " and going to state " +
                            stack[stackTop-1]);
          }
          state = stateStack[--stackTop];
        }
        stackTop = 0;
        return 0;

      case 3: // I need to drop the current token
        if (isVerbose()) {
          System.out.println("Recuperate removing symbol " + lexicalToken);
        }
        if(lexicalToken == 0) { // end of file
          return 0;
        }
        lexicalToken = parserElement(false);
        return 1;
    }
    // should never reach
    System.err.println("ASSERTION FAILED ON PARSER");
    Exception e = new Exception();
    e.printStackTrace(System.err);
    return 0;
  }

  /**
   * initialize the parser.  Caller (or constructor) must call it
   */
  public void init() {
    stackTop = 0;
    stateStack[0] = 0;
    stack[0] = null;
    state = 0;
  }
  
  public static final int ACCEPTED = 1;
  public static final int SHIFTED = 2;
  public static final int PARSING_ERROR = 3;
  public static final int INTERNAL_ERROR = 4;
  
  /**
   * send and parse one token.  main routine of the scanner driven recognizer
   */
  public int parse(int symbol, StackElement value) {
    int action;
    lexicalToken = getTokenIndex(symbol);
    lexicalValue = value;

    if (isVerbose()) {
      System.out.println("Starting to parse symbol " + symbol + "(" + lexicalToken + ":" + lexicalValue.toString() + ")");
      parserPrintStack();
    }

    while(2 != 1) { // forever with break and return below
      action = parserAction(state, symbol);
      if (isVerbose()) {
        System.out.println("Action: " + action);
      }
      if(action == ACCEPT) {
        if (isVerbose()) {
          System.out.println("Program Accepted");
        }
        return ACCEPTED;
      }

      if(action > 0) {
        if(parserShift(lexicalToken, action) == 0) {
          return INTERNAL_ERROR;
        }
        return SHIFTED;
      } else if(action < 0) {
        if(parserReduce(lexicalToken, -action) == 0) {
          return INTERNAL_ERROR;
        }
      } else if(action == 0) {
        return PARSING_ERROR;
      }
    }
  }
  
  /**
   * give me the available actions that can be taken.  I am also returning reduces.
   */
  public int[] getValidTokens() {
    int count = 0;
    for (int i = 0; i < TOKENS; i++) {
      if(parsingTable[state][i] != 0) {
        count ++;
      }
    }

    int actions[] = new int[count];
    int index = 0;
    for(int i = 0; i < TOKENS; i++) {
      if (parsingTable[state][i] != 0) {
        actions[index++] = tokenDefs[i].token;
      }
    }
    return actions;
  }

  /**
   * @returns the current lextical value
   */
  public StackElement getResult() {
    return stack[stackTop];
  }

  /**
   * @param token is the number of the token
   * @returns the name of a token, given the token number
   */
  public String getTokenName(int token) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].token == token) {
        return tokenDefs[i].name;
        }
    }
    if (token < 256) {
      return "\'" + (char) token + "\'";
    } else {
      return "UNKNOWN TOKEN";
    }
  }

  /**
   * @param token is the number of the token
   * @returns the name of a token, given the token number
   */
  public int getTokenIndex(int token) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].token == token) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Perform a round of tokenization and dump the results
   */
  public void dumpTokens() {
    lexicalToken = parserElement(true);
    while (lexicalToken != 0) {
      System.out.println("Token: " + getTokenName(lexicalToken) + "(" + lexicalToken + "):" + lexicalValue.toString());
      lexicalToken = parserElement(false);
    }
  }

  int findReservedWord(String word) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].reserved && tokenDefs[i].name.equals(word)) {
        return tokenDefs[i].token;
      }
    }
    return -1;
  }

  int findReservedWordIgnoreCase(String word) {
    for (int i = 0; i < tokenDefs.length; i++) {
      if (tokenDefs[i].reserved && tokenDefs[i].name.equalsIgnoreCase(word)) {
        return tokenDefs[i].token;
      }
    }
    return -1;
  }

  private static final int REGEX_MATCHED = 0;
  private static final int REGEX_NONE = 1;
  private static final int REGEX_TOOMANY = 2;

  private class RegexpMatch {
    int index;
    String matched;
    int error;

    public RegexpMatch(int index, String matched, int error) {
      this.index = index;
      this.matched = matched;
      this.error = error;
    }
    
    public String toString() {
      return "{index:" + index + ",matched:\"" + matched + "\",error:" + error + "}";
    }
  }

  /*private RegexpMatch matchRegExp() {
    String s = "";

    int candidates[] = new int[tokenDefs.length];

    for (int i = 0; i < candidates.length; i++) {
      candidates[i] = 1;
    }

    s += currentChar;

    // search which regular expressions match the first char
    int count = 0;
    int index = -1;
    int previousCount;
    int previousIndex;

    do {
      previousCount = count;
      previousIndex = index;
      count = 0;
      index = -1;
      for (int i = 0; i < tokenDefs.length; i++) {
        if (candidates[i] == 1 && tokenDefs[i].regex != null && tokenDefs[i].regex.length() > 0) {
          if (s.toString().matches(tokenDefs[i].regex)) {
            index = i;
            count++;
          } else {
            candidates[i] = -1;
          }
        }
      }

      if (count > 0) {
        s += currentChar;
        getNextChar(false);
      }
    } while (count > 0);

    // restore last try
    count = previousCount;
    index = previousIndex;
    s = s.substring(0, s.length()-2);
    // currentChar is OK now as I went one back internally to this function

    // see what happened
    if (count == 0) {
      // none matches
      return new RegexpMatch(-1, "", REGEX_NONE);
    } else if (count == 1) {
      return new RegexpMatch(index, s, REGEX_MATCHED);
    } else {
      return new RegexpMatch(-1, s, REGEX_TOOMANY);
    }
  }*/

/*
   *
   * End of packed skeleton for java
   *
   */


