/*
 * This is a program that converts mathematical expressions from postfix to infix
 * and vice versa.
 */
package com.kurtronaldmueller.expressionManager;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class ExpressionManager
{
    Stack<Character> symbolStack;
    ArrayList<Operator> operators = new ArrayList<Operator>();
    
    /**
     * Default constructor. Creates the operator ArrayList to assst in evaulting a postfix expression.
     */
    public ExpressionManager() {
        operators.add( new Operator( "openBrace",        '{', 0 ) );
        operators.add( new Operator( "openBracket",      '[', 0 ) );
        operators.add( new Operator( "openParenthesis",  '(', 0 ) );
        operators.add( new Operator( "addition",         '+', 1 ) );
        operators.add( new Operator( "subtraction",      '-', 1 ) );
        operators.add( new Operator( "multiplication",   '*', 2 ) );
        operators.add( new Operator( "division",         '/', 2 ) );
        operators.add( new Operator( "modulus",          '%', 2 ) );
        operators.add( new Operator( "closeBrace",       '}', 3 ) );
        operators.add( new Operator( "closeBracket",     ']', 3 ) );
        operators.add( new Operator( "closeParenthesis", ')', 3 ) );
    }

    
    /**
     * Checks to see if the mathematical passed in is balanced, meaning
     * that the opening symbols '{', '(', & '[' are matched to the
     * corresponding closing symbols of '}', ')', & ']'.
     * 
     * @param expression The mathematical expression passed in.
     * @return True if the expression is balanced.  If the expression
     * is not balanced, the program ends.
     */
    public boolean hasBalancedSymbols( String expression )
    {
        /**
         * This stack stores the symbols ''{', '(', & '['. 
         * 
         * While reading through each character in the expression, the
         * symbol stack:
         * 1. Pushes when the current character is one of the aforementioned.
         * 2. Pops if:
         *    
         *    Current Stack Char | Current Expression Char
         *             '{'                    '}'
         *             '('                    ')'
         *             '['                    ']'
         * 
         */
        symbolStack = new Stack<Character>();
        int i = 0;
        
        // while the stack has content in it & the expression passed-in still has
        // characters in it
        while( symbolStack.size() >= 0 && i < expression.length() )
        {
            // if the current char in the expression is {, (, or [, push it
            // automatically on to the symbol stack
            if( expression.charAt( i ) == '{' || 
                expression.charAt( i ) == '(' ||
                expression.charAt( i ) == '[' )
                symbolStack.push( expression.charAt( i ) );
            
            // if the current char in the expression is }, ), or ], then
            // compare then the current char in the expression with the current
            // char in the stack
            else if( expression.charAt( i ) == '}' || expression.charAt( i ) == ')' ||
                     expression.charAt( i ) == ']' )
            {
                try
                {
                    compareSymbols( symbolStack.peek(), expression.charAt( i ) );
                    symbolStack.pop();
                }
                catch( EmptyStackException e ) // thrown when the symbol stack is empty,
                {                              // which means the equation is unbalanced
                    System.out.println("The expression is invalid.");
                    System.exit( 0 );
                }
                catch( MismatchingSymbolsException e ) // thrown when the current symbol in the 
                {                                      // stack does not correspond to the current
                                                       // symbol in the expression
                    System.out.println( e.getMessage() );
                    System.exit( 0 );
                }
            }
            
            i++;
        }
        
        // after the while loop, if the symbol stack is not empty, this
        // means that there are opening symbols that have yet to be closed
        if(!symbolStack.isEmpty()) {
            System.out.println( "The equation is not balanced!" );
            System.exit( 0 );
        }
        
        System.out.println( "Equation is balanced!" );
        return true;
    }
    

    /**
     * Compares two symbols to see if they correspond to each other (e.g. '{' & '}', '(' & ')' )
     * 
     * @param openingSymbol The opening symbol - {, (, or [.
     * @param closingSymbol the closing symbol - }, ), or ].
     * @throws MismatchingSymbolsException Thrown when the symbols do not correspond.
     */
    public void compareSymbols( char openingSymbol, char closingSymbol ) throws MismatchingSymbolsException
    {
        
        if( ( openingSymbol == '{' && closingSymbol != '}' ) ||
            ( openingSymbol == '(' && closingSymbol != ')' ) ||
            ( openingSymbol == '[' && closingSymbol != ']' ) )
            throw new MismatchingSymbolsException();
    }
    
    
    /**
     * Returns true if the character passed in is a symbol.
     * 
     * @param currentChar The character being compared.
     * @return True if the character is a symbol.
     */
    public boolean isSymbol( char currentChar ) {
        return( currentChar == '{' || currentChar == ']' || currentChar == '(' ||
                currentChar == '}' || currentChar == '[' || currentChar == ')' );
    }
    
    
    /**
     * Convert the mathematical expression to a stack of type string where
     * stack.pop() removes the the characters from left-to-right in the expression.
     * 
     * @precondition Mathematical expression is balanced.
     * @param expression The mathematical expression to be converted into Stack<String>
     * @return Returns a clone of the Stack<String>
     */
    @SuppressWarnings("unchecked")
    public Stack<String> toStack( String expression )
    {
        StringBuilder exprString    = new StringBuilder( expression ); // converted to StringBuilder in order to manipulate string
        StringBuilder currentString = new StringBuilder();             // holds current characters yet to be pushed into the stack
        Stack<String> exprStack     = new Stack<String>();             // holds the converted expression
        
       
        /**
         * This while loop works from the right end of the mathematical expression to
         * the left.
         * 
         * The algorithm is as follows:
         * 
         * While the expression string's length is greater than 0 (there are still
         * some characters left yet to be analyzed):
         * 1. Insert the character at the end of the mathematical expression into the
         *    first index of the current string.
         * 2. Delete the character at the end of the mathematical expression.
         * 3. If the first character in the current string is either a space, an
         *    operator, or a symbol, then:
         *    a. if the current string's length is greater than 1
         *       - push characters from index 1 to the end of the current string on to
         *         the expression stack.
         *    b. if the character at index 0 in the current string is not a space:
         *       - push the index 0 character on to the stack
         *    c. clear the current string
         */
        do
        {
            currentString.insert( 0, exprString.charAt( exprString.length() - 1 ) );
            exprString.deleteCharAt( exprString.length() - 1 );

            if( currentString.charAt( 0 ) == ' ' || isOperator( currentString.charAt( 0 ) ) ||
                    isSymbol( currentString.charAt( 0 ) ) ) {
                
                if( currentString.length() > 1 )       exprStack.push( currentString.substring( 1 ) );
                if( currentString.charAt( 0 ) != ' ' ) exprStack.push( currentString.substring( 0, 1 ) );
                
                currentString = new StringBuilder();
            }
            
        } while( exprString.length() > 0 );
        
        // if there are any left over chars in the current string,
        // push them onto the stack
        if( currentString.length() >= 0 )
            exprStack.push( currentString.toString() );

        // return a clone of the stack
        return ( Stack<String> ) exprStack.clone();
    }

    
    /**
     * Returns true if the character is an opening bracket (i.e. [, (, {)
     * Please note that brackets are the actual names for these characters,
     * even though in the U.S. the () chars are called parentheses.
     * 
     * @param character The character being compared.
     * @return True if the character is an opening bracket.
     */
    public boolean isOpeningBracket( char character ) {
        return( character == '{' || 
                character == '[' || 
                character == '(' );
    }

    
    /**
     * Returns true if the character is an closing bracket (i.e. ], ), })
     * 
     * @param character The character being compared.
     * @return True if the character is a closing bracket.
     */
    public boolean isClosingBracket( char character ) { 
        return( character == '}' || 
                character == ']' || 
                character == ')' );
    }

    
    /**
     * Performs the specified operation on the left & right operand.
     * 
     * @param leftOperand  The left operand.
     * @param operator     The specified operator.
     * @param rightOperand The right operand.
     * @return             Returns the result of the operation.
     */
    public double performOperation( String leftOperand, char operator, String rightOperand )
    {
        Double left  = new Double( leftOperand );
        Double right = new Double( rightOperand );
        
        switch( operator )
        {
        case '+': return left + right;
        case '-': return left - right;
        case '*': return left * right;
        case '/': return left / right;
        case '%': return left % right;
        }
        
        return 0.0; // should not get here
    }
    
    /**
     * Check the expression to see if it is valid.
     * 
     * @param expression The expression to be checked.
     * @throws IllegalExpressionException Thrown when the expression is illegal
     *         (i.e. amount of numbers - amount of operators != 1)
     */
    public void checkExpression( String expression )
                                    throws IllegalExpressionException
    {
        // convert the expression to a stack
        Stack<String> exprStack = this.toStack( expression );
        
        int operatorCount = 0; // the amount of operators
        int numberCount   = 0; // the amount of numbers
        
        StringBuilder currentString = new StringBuilder();
        
        while( ! exprStack.isEmpty() ) {
            currentString = new StringBuilder( exprStack.pop() );
            
            /*
             * If the current string is an operator, increment the operator count.
             * If the current string is a number, increment the number count.
             */
            if( isOperator( currentString.charAt( 0 ) ) )
                operatorCount++;
            else if( ! isOpeningBracket( currentString.charAt( 0 ) ) &&
                     ! isClosingBracket( currentString.charAt( 0 ) ) )
                numberCount++;
        }
        
        // the numbers in a valid mathematical expression will always equal the
        // operator count + 1
        if(numberCount - operatorCount != 1)
            throw new IllegalExpressionException();
    }
    
    /**
     * Evaluates a postfix expression and returns the result.
     * 
     * @param postfixExpression The postfix expression.
     * @return Returns the result.
     */
    public double evalPostfixExpression( String postfixExpression )
    {    
        try
        {
            // check to see if the expression is valid
            checkExpression( postfixExpression );
        }
        catch( IllegalExpressionException e )
        {
            System.out.println( postfixExpression + " is an illegal expression." );
            return 0.0;
        }
        
        Double result = new Double( 0.0 ); // the result that will be eventually returned
        
        Stack<String> exprStack     = this.toStack( postfixExpression );
        Stack<String> holder        = new Stack<String>(); // the holder stack
        
        StringBuilder currentString = new StringBuilder(); // the current string
        StringBuilder leftOperand   = new StringBuilder(); // the left operand
        StringBuilder rightOperand  = new StringBuilder(); // the right operand
        
        char currentOperator = 0; // the current operator
        
        // while the expression stack is not empty
        while( ! exprStack.isEmpty() )
        {
            // the current string in the expression stack
            // pop the element from the expression stacka nd add it to the current string
            currentString = new StringBuilder( exprStack.pop() ); 
            
            // if the first character in the current string is not a n operator,
            // push the current string onto the holder stack
            if( ! isOperator( currentString.charAt( 0 ) ) ) {
                holder.push( currentString.toString() );
            }
            else
            {
                currentOperator = currentString.charAt(0);           // the current operator
                rightOperand    = new StringBuilder( holder.pop() ); // the right operand
                leftOperand     = new StringBuilder( holder.pop() ); // the left operand
                
                // result of the current operation
                result = performOperation( leftOperand.toString(), currentOperator, rightOperand.toString() );
                
                // push the result on the holder stack
                holder.push( result.toString() );
            }
        }

        return result; // return the result
    }
    
    
    /**
     * Converts a postfix expression to its infix expression.
     * 
     * @precondition The postfix expression is valid.
     * @param postfixExpression The postfix expression that will be converted.
     * @return A string of the infix expression.
     */
    public String convertToInfix(String postfixExpression)
    {
        try
        {
            // check to see if the expression is valid
            checkExpression( postfixExpression );
        }
        catch(IllegalExpressionException e)
        {
            System.out.println( postfixExpression + " is an illegal expression." );
            return null;
        }
        
        Stack<String> exprStack = this.toStack( postfixExpression ); // convert the postfix expression to a stack
        Stack<String> holder    = new Stack<String>();             // holds the converted postfix expression
        
        StringBuilder currentString = new StringBuilder(); // the current string being examined
        StringBuilder ifExpression  = new StringBuilder(); // the final infix expression
        StringBuilder previousExpr  = new StringBuilder();
        
        char previousOperator = ')'; // if the precedence of the last operator is lower, expression is surrounded with parenthesis
        char currentOperator  = 0;
        
        /**
         * While loop algorithm
         * 
         * While the expression stack is not empty:
         * 1. Assign the top of the stack to the current string.
         * 2. If the first character in the current string is an operator:
         *    a. push the current string onto the holder stack
         * 3. Else, if the first character in the current string is not an operator:
         *    a. Store the current operator into a holder variable.
         *    b. Pop the top of the holder stack and hold it in the a variable
         *       for the previous expression.
         *    c. Pop the top of the holder stack again and insert it into the
         *       beginning of the current string.
         *    d. If the currently stored operator has a higher precedence than the
         *       previously stored operator, add a bracket at the beginning and end
         *       of the previous expression.
         *    e. Append the previous expression to the current string.
         *    f. The currently stored operator now becomes the previously stored
         *       operator.
         *    g. Push the current string to the holder stack.
         */
        while( ! exprStack.isEmpty() )
        {
            currentString = new StringBuilder( exprStack.pop() );
            
            if( ! isOperator( currentString.charAt( 0 ) ) )
                holder.push( currentString.toString() );
            else
            {
                currentOperator = currentString.charAt( 0 );
                previousExpr    = new StringBuilder( holder.pop() );
                
                currentString.insert( 0, holder.pop() + " " );
                
                if( ! hasHigherPrecedence( previousOperator, currentOperator ) ) {
                    previousExpr.append( " " + ')' );
                    previousExpr.insert( 0, '(' + " " );
                }
                                
                currentString.append( " " + previousExpr );                
                holder.push( currentString.toString() );
                
                previousOperator = currentOperator;
            }
        }
        
        /**
         * while the holder stack is not empty, insert it into the beginning of the
         * infix expression string
         */
        while( ! holder.isEmpty() )
            ifExpression.insert( 0, holder.pop() );

        return ifExpression.toString();
    }
    
    
    /**
     * Converts an infix expression to a postfix expression.
     * 
     * @precondition The expression being passed in is actually an infix expression.
     * @param infixExpression The infix expression.
     * @return The string of the postfix expression.
     */
    public String convertToPostfix( String infixExpression )
    {
        this.hasBalancedSymbols( infixExpression );
        
        try
        {
            checkExpression( infixExpression );
        }
        catch( IllegalExpressionException e )
        {
            System.out.println( infixExpression + " is an illegal expression." );
            return null;
        }
        
        Stack<String>  exprStack    = this.toStack( infixExpression ); // retrieves the stack of the infix expression
        Stack<Character> opStack    = new Stack<Character>();          // stores the operators of the infix expression

        StringBuilder currentString = new StringBuilder();            // stores the current string returned by exprStack.peek()
        StringBuilder pfExpression  = new StringBuilder();            // stores the postfix expression
        
        while( ! exprStack.isEmpty() )
        {
            // assign the top of the expression stack to the current string
            // and then pop it off
            currentString = new StringBuilder( exprStack.peek() );
            exprStack.pop();
    
            
            /**
             * if the current string's first character is a closing bracket,
             * 1. then while the operator stack's size is greater than 0 and
             *    current string at the top of the operator stack is not an 
             *    opening bracket:
             *    a. append whatever is in the operating stack to the postfix
             *       expression
             *    b. pop the top of the operator stack
             * 2. pop the operator stack (i.e. the opening bracket)
             */
            if( isClosingBracket( currentString.charAt( 0 ) ) )
            {
                while( opStack.size() > 0 && ! isOpeningBracket( opStack.peek() ) )
                {
                    pfExpression.append( opStack.peek() + " " );
                    opStack.pop();
                }

                opStack.pop();
            }
            
            /**
             * If the current string's first character is an opening bracket,
             * simply push it on the operator stack
             */
            else if( isOpeningBracket( currentString.charAt( 0 ) ) )
                 opStack.push( currentString.charAt( 0 ) );
            
            /**
             * If the current string's first charactar is an operator,
             * then:
             * 1. while the operator stack's size is greater than 0 AND
             *    the top of the operator stack has greater precedence over the
             *    current string's first character:
             *    a. append the top of the operator stack to the postfix expression
             *    b. pop the top of the operator stack
             * 2. push the first character of the current string on top of the operator
             *    stack
             */
            else if( isOperator( currentString.charAt( 0 ) ) )
            {   
                while( opStack.size() > 0 && 
                       this.hasHigherPrecedence( opStack.peek(), currentString.charAt( 0 ) ) )
                {
                    pfExpression.append( opStack.peek() + " " );
                    opStack.pop();
                }
                
                opStack.push( currentString.charAt( 0 ) );
            }
            else // simply append it to the postfix expression
                pfExpression.append( currentString + " " );
        }
        
        /**
         * while there are still operators left in the operator stack
         * 1. append the top of the stock to the postfix expression
         * 2. pop the top of the operator stack
         */
        while( opStack.size() > 0 )
        {
            pfExpression.append( opStack.peek() + " " );
            opStack.pop();
        }
        
        return pfExpression.toString();
    }
    
    /**
     * Compares a character and tells if the character is an operator.
     * 
     * @param currentChar The character being examined.
     * @return True if the current character is an operator.  False if is not.
     */
    public boolean isOperator( char currentChar )
    {

        return currentChar == '+' || currentChar == '-' || currentChar == '/' ||
               currentChar == '*' || currentChar == '%';
    }
    
    
    /**
     * Compares two operators and tells is the stack operator has higher
     * precedence than the string operator.
     * 
     * @precondition Assume both chars being passed in are operators.
     * @param stackOperator The current operator in the stack.
     * @param stringOperator The current operator in the pre-conversion string.
     * @return Returns true if the stack operator is greater than or equal to the string operator.
     */
    public boolean hasHigherPrecedence( char stackOperator, char stringOperator )
    {
        int i = 0;
        int precedenceStackOp  = -1;
        int precedenceStringOp = -1;
        
        // while precedences for both chars have yet to be assigned and
        // i is less than the ArrayList size
        while( ( precedenceStackOp == -1 || 
                precedenceStringOp == -1 ) && 
                i < operators.size() ) 
        {                           
            if( operators.get( i ).getSymbol() == stackOperator )
                precedenceStackOp = operators.get( i ).getPrecedence();
            
            if( operators.get( i ).getSymbol() == stringOperator )
                precedenceStringOp = operators.get( i ).getPrecedence();
            
            i++;
        }
        
        return precedenceStackOp >= precedenceStringOp;
    }
    
    
    /**
     * Get the precedence of the character.
     * 
     * @param character The character being passed in.
     * @return Returns the precedence level of the characters.
     */
    public int getPrecedence( char character ) {
        
        int precedence = -1;
        int i = 0;
        
        while( precedence == -1 && i < operators.size() )
        {
            if( operators.get(i).getSymbol() == character )
                precedence = operators.get(i).getPrecedence();
            
            i++;
        }
        
        return precedence;
    }
}
