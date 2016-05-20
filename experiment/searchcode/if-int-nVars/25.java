/*
 * Copyright (c) 2007 BUSINESS OBJECTS SOFTWARE LIMITED
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *  
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *  
 *     * Neither the name of Business Objects nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


/*
 * FreeVariableFinder.java
 * Creation date: (February 6, 2001)
 * By: Bo Ilic
 */
package org.openquark.cal.compiler;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openquark.cal.util.ArrayStack;
import org.openquark.cal.util.Graph;
import org.openquark.cal.util.Vertex;
import org.openquark.cal.util.VertexBuilder;
import org.openquark.cal.util.VertexBuilderList;



/**
 * Used for finding free variables in global function, lambda definition or local
 * function definitions. Free variables, in general, are the variables upon which
 * the given expression or definition actually depends on.
 * <p>
 * For each function in a module, an identifier appearing within its defining 
 * body expression is a:
 * <ul>
 * <li>(non built-in, non foreign) function defined within the same module
 * <li>(reference to a) function defined within a different module
 * <li>built-in function 
 * <li>class method name
 * <li>local function defined using a let
 * <li>pattern variable i.e. an argument variable, a variable from a case pattern binding, or
 *     a lambda bound variable.
 * <li>data constructor defined in the same module
 * <li>data constructor defined within a different module
 * <li>foreign function.
 * </ul>
 * <p>
 * This class is responsible for augmenting the parse tree of unqualified functions and
 * data constructors to explicitly fill in the module name, so that further analysis can assume
 * that all identifier names are fully qualified.
 * <p>
 * It is also responsible for reporting an error if an identifier does not exist in one of the 
 * categories above.
 * <p>
 * It isolates the non built-in, non foreign defined within the same module,
 * which is needed for dependency analysis. 
 * <p>
 * It augments data constructor field selection expression nodes with the qualified var which would 
 * appear in the rhs of the alt for a corresponding case expression.
 * <p>
 * It converts unused pattern variables in case expressions to wildcard (_) patterns.
 * This is primarily an optimization for the runtime.
 * <p>
 * It checks and desugars local pattern match declarations (e.g. let (x, y, z) = foo; in ...) via the helper class
 * {@link LocalPatternMatchDeclarationChecker}.
 * <p>
 * It provides module unique names for all the local bound variables appearing in the function's definition.
 * For example, f x = 2.0 + x would be converted to f f$x$1 = 2.0 + f$x$1. This makes latter compiler passes,
 * in particular Johnsson style lambda lifting much easier.
 * <p>
 * In some ways this class could more generally be called the "StaticAnalyzer" because it detects certain
 * simple "static" errors that can be reported in the first pass of a the parse-tree, such as
 * repeated symbol definitions.
 * <p>
 * Creation date: (February 6, 2001)
 * @author Bo Ilic
 */
class FreeVariableFinder {

    private final CALCompiler compiler;

    private final ModuleTypeInfo currentModuleTypeInfo;
        
    /** Names of all functions defined in the current module at the top level, not including built-ins and foreign functions.*/
    private final Set<String> topLevelFunctionNamesSet;
   
    /**
     *  If true, then resolve unqualified names that occur in the parse tree, or report an error if this can not be done.
     *  Also, do some extra error checking on the correctness of the parse trees.
     *  Since free variable finding is done on the first full traversal of the parse tree of function
     *  definitions, this class is a convenient place to put certain error checking.
     */
    private boolean firstPass;
   
    /** 
     * The names of all possible dependee var names from the current module.
     * The sets returned by the public methods of this class will be a subset of this set.
     */
    private Collection<String> possibleDependeeNamesSet;

    /** 
     * The names of built-in and foreign functions will never be returned as a dependee (unless cloaked by
     * scoping). Here are some additional names that should not be returned.  These names are all
     * assumed to belong to the current module.
     */
    private Set<String> ignorableFunctionNamesSet;        
     
    /**
     * Statistics on the number of pattern vars that are unused and then converted to
     * wildcard pattern variables (_). 
     */
    private static final boolean GENERATE_CONVERTED_PATTERN_VAR_STATISTICS = false;
    private int nUnusedPatternVars;
    private static int totalNUnusedPatternVars;
    
    /**
     * A stack for the use of bound variables. Its special feature is that it keeps track of how
     * often a given bound variable is used in its defining expression.
     * 
     * It is also used to rename the bound variables in a function's definition so that they are 
     * unique within a given module.
     * 
     * @author Bo Ilic
     */
    private static final class BoundVariablesStack {
        
        private final ArrayStack<BoundVariable> stack;
        
        /**
         * name of the top-level function in which we are doing symbol resolution.
         * null if we are working on a subexpression of the function's definition, and
         * don't want to assign unique names to the bound variables.
         */
        private final String topLevelFunctionName;
        
        /**
         * every time a BoundVariable is pushed onto the BoundVariableStack, the varCount is
         * incremented by 1. This allows us to assign a unique int to each bound variable within
         * a function, and thus have an unambiguous name for each local symbol defined within
         * the function
         */        
        private int varCount;
        
        private static final class BoundVariable {
        
            private final ParseTreeNode varNameNode;
            private int useCount;
            
            /**
             * name to prepend to the variable's name             
             */ 
            private final String prefix;
            private final int varNumber;
        
            private BoundVariable(ParseTreeNode varNameNode, int varNumber, String prefix) {
                this.varNameNode = varNameNode;
                this.varNumber = varNumber;
                this.prefix = prefix;                
            }
            
            private BoundVariable(String varName) {
                this (new ParseTreeNode(CALTreeParserTokenTypes.VAR_ID, varName), -1, null);
            }
        
            @Override
            public boolean equals(Object other) {
                return getVarName().equals(((BoundVariable)other).getVarName());
            }
        
            @Override
            public int hashCode() {
                return getVarName().hashCode();
            }
        
            int getUseCount() {
                return useCount;
            }
        
            void incrementUseCount() {
                ++useCount;
            }
            
            String getVarName () {
                return varNameNode.getText();
            }
            
            int getVarNumber () {
                return varNumber;
            }
            
            String getUniqueName() {
                if (prefix != null) {
                    return prefix + "$" + getVarName() + "$" + varNumber;
                }
                
                //names have already been made unique by an earlier pass.
                return getVarName();
            }
            
            @Override
            public String toString() {
                return getUniqueName();                    
            }
        }      
        
        BoundVariablesStack(String topLevelFunctionName) {
            stack = ArrayStack.make();          
            this.topLevelFunctionName = topLevelFunctionName;
        }
        
        void push(ParseTreeNode varNameNode) {
            stack.push(new BoundVariable(varNameNode, ++varCount, topLevelFunctionName));            
        }

        void popN(int n, boolean updateWithUniqueNames) {
            if (updateWithUniqueNames) {
                for (int i = 0; i < n; ++i) {
                    BoundVariable boundVar = stack.pop();
                    boundVar.varNameNode.setText(boundVar.getUniqueName());
                }               
                return;
            }
            
            stack.popN(n);
        }
        
        /**
         * Determine if the stack contains the given varName.
         * @param varName
         * @param incrementUseCount if the stack contains varName, and this flag is true, then
         *   increment the use count of varName. Note: it is the most recently pushed varName whose
         *    use count will be incremented.
         * @return BoundVar non-null if the BoundVariables stack contains a varName with this name. 
         */
        BoundVariable contains(String varName, boolean incrementUseCount) {
            int index = stack.lastIndexOf(new BoundVariable(varName));
            if (index == -1) {
                return null;
            }
            
            BoundVariable var = stack.get(index);
            if (incrementUseCount) {
                var.incrementUseCount();
            }
                       
            return var;
        }
        
        /**         
         * @param varName
         * @return the use count (>= 0) if varName is in the stack and -1 if not. The use count is for
         *     the last added symbol in the stack.
         */
        int getUseCount(String varName) {
            int index = stack.lastIndexOf(new BoundVariable(varName));
            if (index == -1) {
                return -1;
            }
            return stack.get(index).getUseCount();            
        }
                            
        /**         
         * @param nElements number of elements to get from the top of the stack
         * @return (String Set) the var names in the stack, with the uniqueness transform applied.         
         */
        Set<String> getUniqueVarNamesSet(int nElements) { 
            if (topLevelFunctionName == null) {
                //should only be doing this if we are disambiguating names
                throw new IllegalStateException();
            }
            Set<String> varNamesSet = new HashSet<String>();            
            for (int i = stack.size() - nElements, n = stack.size(); i < n; ++i) {
                varNamesSet.add(stack.get(i).getUniqueName());           
            }
            return varNamesSet;           
        }
        
        Set<String> getUniqueVarNamesSet() { 
            return getUniqueVarNamesSet(stack.size());           
        }
        
        @Override
        public String toString() {
            if (stack.size() == 0) {
                return "<empty stack>";
            }
            
            StringBuilder result = new StringBuilder();
            for (int i = stack.size() - 1; i >= 0; --i) {
                result.append(i).append(' ').append(stack.get(i).toString()).append('\n');
            }
            return result.toString();
        }
    }
    
    /**
     * FreeVariableFinder constructor comment.
     *
     * @param compiler    
     * @param topLevelFunctionNamesSet names of all functions defined in the current module at the top level, not including built-in or foreign functions.
     * @param currentModuleTypeInfo
     */
    FreeVariableFinder(CALCompiler compiler, Set<String> topLevelFunctionNamesSet, ModuleTypeInfo currentModuleTypeInfo) {
    
        if (compiler == null ||           
            topLevelFunctionNamesSet == null ||
            currentModuleTypeInfo == null) {
                
            throw new NullPointerException();
        }

        this.compiler = compiler;        
        this.topLevelFunctionNamesSet = topLevelFunctionNamesSet;
        this.currentModuleTypeInfo = currentModuleTypeInfo;
    }
    
    /**
     * Perform dependency analysis to divide up the top-level functions defined in a module into
     * a topologically ordered set of strongly connected components.
     * 
     * This function does the first full traversal of all function definition expression trees.
     * It does certain static checking and updates the parse-trees as a side effect.
     * -all symbols that are not fully qualified are fully qualified, or an error if the symbol
     *  doesn't exist or is not visible.
     * -static checks that certain symbols such as case pattern variables are not duplicated
     *  e.g. "case xs of x : x" will give an error
     * -local dependency order transformations of let blocks
     * 
     * Creation date: (1/18/01 6:11:23 PM)
     * @param functionNameToDefinitionNodeMap
     * @return Graph 
     */
    Graph<String> performFunctionDependencyAnalysis(Map<String, ParseTreeNode> functionNameToDefinitionNodeMap) {
      
        //Makes the dependency graph for the top level functions defined in a module.
        VertexBuilderList<String> vertexBuilderList = new VertexBuilderList<String>();
                        
        for (final Map.Entry<String, ParseTreeNode> entry : functionNameToDefinitionNodeMap.entrySet()) {
           
            String functionName = entry.getKey();
            ParseTreeNode functionParseTree = entry.getValue();
            Set<String> freeVariablesSet = findDependeeFunctionNames(functionParseTree);

            vertexBuilderList.add(new VertexBuilder<String>(functionName, freeVariablesSet));
        }
        
        if (FreeVariableFinder.GENERATE_CONVERTED_PATTERN_VAR_STATISTICS) {
            FreeVariableFinder.totalNUnusedPatternVars += nUnusedPatternVars;
            
            System.out.println("module " + currentModuleTypeInfo.getModuleName());
            System.out.println("number of unused pattern vars converted = " + nUnusedPatternVars);
            System.out.println("total unused pattern vars converted =" + FreeVariableFinder.totalNUnusedPatternVars);
        }        
       
        // should never fail. It is a redundant check since makeSCDependencyGraph should throw an exception otherwise.
        if (!vertexBuilderList.makesValidGraph(topLevelFunctionNamesSet)) {
            throw new IllegalStateException("Internal coding error during dependency analysis."); 
        }
        
        Graph<String> g = new Graph<String>(vertexBuilderList);

        g = g.calculateStronglyConnectedComponents();

        //Previously we gave an error if there were no functions defined within the module.
        //However, we might want to have a module consisting only of data declarations so this
        //is now OK.

        return g;
    }        
    
    /**
     * Returns the names of the top level non built-in functions defined within the current
     * module upon which the definition of the function defined by functionParseTree depends.
     * As a side effect, unqualified names occurring in functionParseTree are augmented with their correct
     * qualification.
     *
     * Creation date: (2/6/01 9:16:32 AM)
     * @return Set of global non built-in or foreign function names upon which the function definition depends
     *      ordered by their first occurrence in the expression text.     
     * @param functionParseTree parse tree of the function definition
     */
    private Set<String> findDependeeFunctionNames(ParseTreeNode functionParseTree) {

        functionParseTree.verifyType(CALTreeParserTokenTypes.TOP_LEVEL_FUNCTION_DEFN);
       
        initState(true, Collections.<String>emptySet(), topLevelFunctionNamesSet);
        
        ParseTreeNode functionNameNode = functionParseTree.getChild(2);        

        ParseTreeNode paramListNode = functionNameNode.nextSibling();
        paramListNode.verifyType(CALTreeParserTokenTypes.FUNCTION_PARAM_LIST);

        BoundVariablesStack boundVariablesStack = new BoundVariablesStack(functionNameNode.getText());
        Set<String> freeVariablesSet = new LinkedHashSet<String>();

        findFreeVariablesInBoundExpression(freeVariablesSet, boundVariablesStack, paramListNode);

        return freeVariablesSet;
    }

    /**
     * Resolve unqualified identifiers within a function definition.
     * This method can be used on an adjunct to an existing module.
     * @param functionParseTree parse tree of the function definition
     */
    void resolveUnqualifiedIdentifiers(ParseTreeNode functionParseTree) {

        functionParseTree.verifyType(CALTreeParserTokenTypes.TOP_LEVEL_FUNCTION_DEFN);

        Set<String> dependeeNamesSet = new HashSet<String>();
        int nFunctions = currentModuleTypeInfo.getNFunctions();
        for (int i = 0; i < nFunctions; i++) {
            Function function = currentModuleTypeInfo.getNthFunction(i);
            dependeeNamesSet.add(function.getName().getUnqualifiedName());
        }
        topLevelFunctionNamesSet.addAll(dependeeNamesSet);
        
        // Add the name from the function itself, in case it's recursive.
        ParseTreeNode varNode = functionParseTree.getChild(2);
        varNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
        String functionName = varNode.getText();
        topLevelFunctionNamesSet.add(functionName);
                 
        initState(true, Collections.<String>emptySet(), topLevelFunctionNamesSet);

        ParseTreeNode paramListNode = functionParseTree.getChild(3);
        paramListNode.verifyType(CALTreeParserTokenTypes.FUNCTION_PARAM_LIST);

        BoundVariablesStack boundVariablesStack = new BoundVariablesStack(functionName);
        Set<String> freeVariablesSet = new HashSet<String>();

        // called for its side effect of resolving unqualified identifiers.
        findFreeVariablesInBoundExpression(freeVariablesSet, boundVariablesStack, paramListNode);
    }

    /**
     * Returns the names of the lambda bound variables, local function names (i.e. not
     * defined within a let or letrec) and function argument names that occur free
     * in the parseTree defining the given lambda or local function definition. Note that normally,
     * the returned set will not contain the names of top level functions. However, because of variable
     * hiding, it may. e.g.
     * x = 2;
     * y = 3;
     * f y = 2 + \z -> x + y + z;
     * Then only y is free in \z -> x + y + z, in the sense of this method. 
     *
     * Creation date: (2/6/01 1:42:51 PM)    
     * @param possibleDependeeNamesSet Set names of the function argument and lambda bound
     *        variables encountered so far in the parse of the global functions that this lambda or local
     *      let definition is a part of. The set returned by this function will be a subset of
     *      this set.
     * @param paramListNode variables for the lambda definition or local let definition.
     * @return Set the free variables encountered ordered by their first occurrence in the expression text. 
     */
    Set<String> findFreeNamesInLambdaExpr(Collection<String> possibleDependeeNamesSet, ParseTreeNode paramListNode) {

        paramListNode.verifyType(CALTreeParserTokenTypes.FUNCTION_PARAM_LIST);

        initState(false, topLevelFunctionNamesSet, possibleDependeeNamesSet);

        BoundVariablesStack boundVariablesStack = new BoundVariablesStack(null);
        Set<String> freeVariablesSet = new LinkedHashSet<String>();

        findFreeVariablesInBoundExpression(freeVariablesSet, boundVariablesStack, paramListNode);

        return freeVariablesSet;
    }
    
    Set<String> findFreeNamesInExpr(Collection<String> possibleDependeeNamesSet, ParseTreeNode exprNode) {

        initState(false, topLevelFunctionNamesSet, possibleDependeeNamesSet);

        BoundVariablesStack boundVariablesStack = new BoundVariablesStack(null);
        Set<String> freeVariablesSet = new LinkedHashSet<String>();

        findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, exprNode);

        return freeVariablesSet;
    }
    
    /**
     * Finds the free variables used in expressions of the form x1,x2,...xn -> e where x1,.., xn are
     * bound variables over the expression's body.
     *
     * Creation date: (9/12/00 10:14:54 AM)
     * @param freeVariablesSet
     * @param boundVariablesStack
     * @param parseTree parent of x1,x2,...xn and preceding sibling of e
     */
    private void findFreeVariablesInBoundExpression(Set<String> freeVariablesSet, BoundVariablesStack boundVariablesStack, ParseTreeNode parseTree) {
    
        Set<String> varNamesSet = (firstPass ? new HashSet<String>() : null);

        int nVars = 0;              

        for (final ParseTreeNode patternVarNode : parseTree) {
                 
            patternVarNode.verifyType(CALTreeParserTokenTypes.LAZY_PARAM, CALTreeParserTokenTypes.STRICT_PARAM);                            
            String varName = patternVarNode.getText();

            if (firstPass && !varNamesSet.add(varName)) {
                // Repeated variable {varName} used in binding.
                compiler.logMessage(new CompilerMessage(patternVarNode, new MessageKind.Error.RepeatedVariableUsedInBinding(varName)));
            }
            
            ++nVars;

            // varName is now a bound variable for the body of the lambda
            boundVariablesStack.push(patternVarNode);                                              
        }

        findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, parseTree.nextSibling());

        boundVariablesStack.popN(nVars, firstPass);        
    }
    
    /**
     * Finds the free variables used in expressions of the form x1,x2,...xn -> e where x1,.., xn are
     * bound variables over the expression's body as defined by a case pattern.
     * For example: Cons x xs -> (length xs + 1, x)  
     * <p>
     * Case patterns are special at the moment because case pattern variables are allowed to be wildcards.
     * <p>
     * Creation date: (9/12/00 10:14:54 AM)
     * @param freeVariablesSet
     * @param boundVariablesStack
     * @param patternVarListNode the parent of the variables x1, x2, ..., xn (can include the wildcard pattern _)
     * @param boundExprNode the expression e   
     */
    private void findFreeVariablesInCasePatternVarListExpression(
        Set<String> freeVariablesSet,
        BoundVariablesStack boundVariablesStack, 
        ParseTreeNode patternVarListNode,
        ParseTreeNode boundExprNode) {
                
        Set<String> varNamesSet = (firstPass ? new HashSet<String>() : null);        
        int nVars = 0;

        for (final ParseTreeNode patternVarNode : patternVarListNode) {
            
            switch (patternVarNode.getType()) {
                
                case CALTreeParserTokenTypes.VAR_ID:
                {           
                    String varName = patternVarNode.getText();
        
                    if (firstPass && !varNamesSet.add(varName)) {
                        // Repeated variable {varName} used in binding.
                        compiler.logMessage(new CompilerMessage(patternVarNode, new MessageKind.Error.RepeatedVariableUsedInBinding(varName)));
                    }
                    
                    ++nVars;

                    // varName is now a bound variable for the body of the lambda
                    boundVariablesStack.push(patternVarNode);
                    
                    break;
                }
                
                case CALTreeParserTokenTypes.UNDERSCORE:
                    break;
                    
                default:
                {
                    patternVarNode.unexpectedParseTreeNode();                   
                    return;
                }
            }


        }

        findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, boundExprNode);
               
        if (firstPass) {     
            //now we convert unused pattern bound variables to wildcards.
            //This saves time in later compilation stages, but it is primarily an optimization
            //put in for the runtime.
            //For example:
            //MyDataCons x y z w -> y
            //is converted to
            //MyDataCons _ y _ _ -> y                                   
            for (final ParseTreeNode patternVarNode : patternVarListNode) {
                                                                          
                convertUnusedVar(boundVariablesStack, patternVarNode);                                                                              
            }
        }        

        boundVariablesStack.popN(nVars, firstPass);
    }
    
    /**
     * Find the free variables occurring in a case pattern expression where the argument bindings are
     * provided using field bindings (ie. matching notation).
     * 
     * Also, if firstPass is true, unused field bindings are removed and the parse tree is patched up so that 
     * punned field names are unpunned.
     *
     * @param freeVariablesSet
     * @param boundVariablesStack
     * @param dcArgBindingsNode the parent node of the arg bindings.
     * @param boundExprNode
     * @param patternDataConstructors (Set of DataConstructor objects) The data constructors used in this particular pattern in declaration order.      
     */
    private void findFreeVariablesInCasePatternMatchingExpression(
        Set<String> freeVariablesSet,
        BoundVariablesStack boundVariablesStack,
        ParseTreeNode dcArgBindingsNode,
        ParseTreeNode boundExprNode,
        Set<DataConstructor> patternDataConstructors) {
        
        if (firstPass) {
            // Perform the first pass verification and patching-up on the parse tree for the list of field bindings.
            firstPassProcessFieldBindingVarAssignmentListNode(dcArgBindingsNode, null, patternDataConstructors);
        } else {
            // Just verify the type.
            dcArgBindingsNode.verifyType(CALTreeParserTokenTypes.FIELD_BINDING_VAR_ASSIGNMENT_LIST);
        }
        
        Set<String> varNamesSet = (firstPass ? new HashSet<String>() : null);
        
        int nVars = 0;
        
        for (final ParseTreeNode fieldBindingVarAssignmentNode : dcArgBindingsNode) {
            
            ParseTreeNode fieldNameNode = fieldBindingVarAssignmentNode.firstChild();
            ParseTreeNode patternVarNode = fieldNameNode.nextSibling();
            
            switch (patternVarNode.getType()) {
                
                case CALTreeParserTokenTypes.VAR_ID:
                {           
                    String varName = patternVarNode.getText();
                    
                    if (firstPass && !varNamesSet.add(varName)) {
                        // Repeated variable {varName} used in binding.
                        compiler.logMessage(new CompilerMessage(patternVarNode, new MessageKind.Error.RepeatedVariableUsedInBinding(varName)));
                    }
                    
                    ++nVars;
                    
                    // varName is now a bound variable for the body of the lambda
                    boundVariablesStack.push(patternVarNode);
                    
                    break;
                }
                
                case CALTreeParserTokenTypes.UNDERSCORE:
                    break;
                    
                default:
                {
                    patternVarNode.unexpectedParseTreeNode();                   
                    return;
                }
            }
        }
        
        findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, boundExprNode);
        
        if (firstPass) {
            // Now convert pattern vars for unused field bindings to wildcards.
            //This saves some work and simplifies assumptions in later compilation stages, 
            //For example:
            //MyDataCons {x=x, y=y, z=z, w=w} -> y
            //is converted to
            //MyDataCons {x=_, y=y, z=_, w=_} -> y
            
            for (final ParseTreeNode fieldBindingVarAssignmentNode : dcArgBindingsNode) {

                ParseTreeNode fieldNameNode = fieldBindingVarAssignmentNode.firstChild();
                ParseTreeNode patternVarNode = fieldNameNode.nextSibling();
                
                if (patternVarNode.getType() == CALTreeParserTokenTypes.VAR_ID) {  
                    
                    if (boundVariablesStack.getUseCount(patternVarNode.getText()) == 0) {
                        // An unused var.
                        convertUnusedVar(boundVariablesStack, patternVarNode);
                        
                        if (FreeVariableFinder.GENERATE_CONVERTED_PATTERN_VAR_STATISTICS) {
                            ++nUnusedPatternVars;
                        }
                    } 
                }      
            }
        }        
        
        boundVariablesStack.popN(nVars, firstPass);
    }

    /**
     * A helper function for findFreeVariablesInCase.
     * Finds the free vars in a record pattern expression.
     * 
     * @param freeVariablesSet
     * @param boundVariablesStack
     * @param patternNode
     * @return false iff an unexpected parse tree node were encountered.
     */
    private boolean findFreeVarsInRecordPatternExpression(Set<String> freeVariablesSet, BoundVariablesStack boundVariablesStack, ParseTreeNode patternNode) {
        
        ParseTreeNode baseRecordPatternNode = patternNode.firstChild();
        baseRecordPatternNode.verifyType(CALTreeParserTokenTypes.BASE_RECORD_PATTERN);
        
        ParseTreeNode baseRecordPatternVarNode = baseRecordPatternNode.firstChild();
        String baseRecordVarName = null;
        if (baseRecordPatternVarNode != null) 
        {                    
            switch (baseRecordPatternVarNode.getType())
            {
                case CALTreeParserTokenTypes.VAR_ID:
                    baseRecordVarName = baseRecordPatternVarNode.getText();
                    break;
                    
                case CALTreeParserTokenTypes.UNDERSCORE:                              
                    break;
                    
                default:
                {  
                    baseRecordPatternVarNode.unexpectedParseTreeNode();                             
                    return false;
                }                            
            }
        }                                                           
       
        ParseTreeNode fieldBindingVarAssignmentListNode = baseRecordPatternNode.nextSibling();                    
        
        if (firstPass) {
            // Perform the first pass verification and patching-up on the parse tree for a list of field bindings.            
            firstPassProcessFieldBindingVarAssignmentListNode(fieldBindingVarAssignmentListNode, baseRecordVarName, Collections.<DataConstructor>emptySet());
        }
        
        int nVars;
        if (baseRecordVarName != null) {
            nVars = 1;
            boundVariablesStack.push(baseRecordPatternVarNode);                                                                       
        } else {
            nVars = 0;
        }

        for (final ParseTreeNode fieldBindingVarAssignmentNode : fieldBindingVarAssignmentListNode) {
                 
            ParseTreeNode fieldNameNode = fieldBindingVarAssignmentNode.firstChild();  
            ParseTreeNode patternVarNode = fieldNameNode.nextSibling();
            
            switch (patternVarNode.getType())
            {
                
                case CALTreeParserTokenTypes.VAR_ID:   
                {                     
                    //String patternVar = patternVarNode.getText();                        
                                        
                    ++nVars;                                                    
                    boundVariablesStack.push(patternVarNode);
                    break;
                }
                
                                               
                case CALTreeParserTokenTypes.UNDERSCORE:
                    break;
                        
                default:
                {
                    patternVarNode.unexpectedParseTreeNode();                               
                    return false;                                                              
                }
            }                                                                                          
        }

        findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, patternNode.nextSibling());
           
        if (firstPass) {
            //now we convert unused pattern bound variables to wildcards.
            //This saves time in latter compilation stages, but it is primarily an optimization
            //put in for the runtime.
            //For example:
            //case r of {s | field1 = f1, field2 = f2, field3 = f3} -> e
            //is converted to {_ | field1 = _, field2 = f2, field3 = _}
            //if only f2 is used in e and s, f1 and f3 are not used. 
                               
            if (baseRecordPatternVarNode != null) {
                convertUnusedVar(boundVariablesStack, baseRecordPatternVarNode);                                                                               
            }  
                              
            for (final ParseTreeNode fieldBindingVarAssignmentNode : fieldBindingVarAssignmentListNode) {
                                            
                ParseTreeNode patternVarNode = fieldBindingVarAssignmentNode.getChild(1);                       
                convertUnusedVar(boundVariablesStack, patternVarNode);                                                                              
            }   
        }
                            
        boundVariablesStack.popN(nVars, firstPass);
        
        return true;
    }

    /**
     * A helper function that finds the free variables in each of the child expressions of parseTree.
     *
     * Creation date: (2/28/01 11:31:54 AM)
     * @param freeVariablesSet 
     * @param boundVariablesStack 
     * @param parseTree
     */
    private void findFreeVariablesInChildExpressions(Set<String> freeVariablesSet, BoundVariablesStack boundVariablesStack, ParseTreeNode parseTree) {

        for (final ParseTreeNode exprNode : parseTree) {

            findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, exprNode);
        }
    }
    
    /**
     * Find the free variables occurring in an expression Node. Intuitively, the free variables
     * are the subset of possibleDependeeNamesSet on which the parseTree actually depends on.
     *
     * For example in: 
     * f x = y + x + (let y = 2 in y)
     * the first occurence of y is the only free variable in the defining expression of f.
     *
     * Creation date: (8/31/00 12:26:07 PM)
     * @param freeVariablesSet The free variables encountered while traversing the parse tree.
     * @param boundVariablesStack The function is not dependent on bound variables appearing in
     *      its definition. These are its argument variables, or variables introduced in internal let
     *      declarations or binder variables in a lambda declaration. This stack varies depending on where 
     *      we are in the definition. The same variable name can occur more than once because of scoping.
     * @param parseTree expression parse tree
     */
    private void findFreeVariablesInExpr(Set<String> freeVariablesSet, BoundVariablesStack boundVariablesStack, ParseTreeNode parseTree) {

        int nodeType = parseTree.getType();

        switch (nodeType) {

            case CALTreeParserTokenTypes.LITERAL_let :
            case CALTreeParserTokenTypes.VIRTUAL_LET_NONREC:
            case CALTreeParserTokenTypes.VIRTUAL_LET_REC:            
            {
                findFreeVariablesInLet(freeVariablesSet, boundVariablesStack, parseTree);               
                return;
            }

            case CALTreeParserTokenTypes.LAMBDA_DEFN :
            {
                ParseTreeNode paramListNode = parseTree.firstChild();
                paramListNode.verifyType(CALTreeParserTokenTypes.FUNCTION_PARAM_LIST);

                findFreeVariablesInBoundExpression(freeVariablesSet, boundVariablesStack, paramListNode);

                return;
            }

            case CALTreeParserTokenTypes.LITERAL_case :
            case CALTreeParserTokenTypes.VIRTUAL_DATA_CONSTRUCTOR_CASE:
            case CALTreeParserTokenTypes.VIRTUAL_RECORD_CASE:
            case CALTreeParserTokenTypes.VIRTUAL_TUPLE_CASE:
            {
                findFreeVariablesInCase(freeVariablesSet, boundVariablesStack, parseTree);                
                return;
            }

            case CALTreeParserTokenTypes.SELECT_DATA_CONSTRUCTOR_FIELD:
            {
                findFreeVariablesInDataConsFieldSelection(freeVariablesSet, boundVariablesStack, parseTree);
                return;
            }
        
            case CALTreeParserTokenTypes.LITERAL_if :
            case CALTreeParserTokenTypes.BARBAR :
            case CALTreeParserTokenTypes.AMPERSANDAMPERSAND :
            case CALTreeParserTokenTypes.PLUSPLUS : 
            case CALTreeParserTokenTypes.LESS_THAN :
            case CALTreeParserTokenTypes.LESS_THAN_OR_EQUALS :
            case CALTreeParserTokenTypes.EQUALSEQUALS :
            case CALTreeParserTokenTypes.NOT_EQUALS :
            case CALTreeParserTokenTypes.GREATER_THAN_OR_EQUALS :
            case CALTreeParserTokenTypes.GREATER_THAN :
            case CALTreeParserTokenTypes.PLUS :
            case CALTreeParserTokenTypes.MINUS :
            case CALTreeParserTokenTypes.ASTERISK :
            case CALTreeParserTokenTypes.SOLIDUS :
            case CALTreeParserTokenTypes.PERCENT:
            case CALTreeParserTokenTypes.COLON :
            case CALTreeParserTokenTypes.UNARY_MINUS:
            case CALTreeParserTokenTypes.POUND:
            case CALTreeParserTokenTypes.DOLLAR:
            case CALTreeParserTokenTypes.BACKQUOTE:
            {
                findFreeVariablesInChildExpressions(freeVariablesSet, boundVariablesStack, parseTree);
                return;
            }

            case CALTreeParserTokenTypes.APPLICATION :
            {
                for (final ParseTreeNode exprNode : parseTree) {

                    findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, exprNode);
                }

                return;
            }

            // function names, class method names and variables
            case CALTreeParserTokenTypes.QUALIFIED_VAR :

                findFreeVariablesInQualifiedVar(freeVariablesSet, boundVariablesStack, parseTree);
                return;

            //data constructors
            case CALTreeParserTokenTypes.QUALIFIED_CONS :
            {
                if (firstPass) {
                    resolveDataConsName(parseTree);
                }
                return;
            }

            // literals
            case CALTreeParserTokenTypes.INTEGER_LITERAL :
            case CALTreeParserTokenTypes.FLOAT_LITERAL :
            case CALTreeParserTokenTypes.CHAR_LITERAL :
            case CALTreeParserTokenTypes.STRING_LITERAL :
                return;

            //A parenthesized expression, a tuple or the trivial type
            case CALTreeParserTokenTypes.TUPLE_CONSTRUCTOR :
            {                                           
                findFreeVariablesInChildExpressions(freeVariablesSet, boundVariablesStack, parseTree);
                return;
            }
            
            //A list data value    
            case CALTreeParserTokenTypes.LIST_CONSTRUCTOR :
                findFreeVariablesInChildExpressions(freeVariablesSet, boundVariablesStack, parseTree);
                return;
                
            case CALTreeParserTokenTypes.RECORD_CONSTRUCTOR:
            {
                ParseTreeNode baseRecordNode = parseTree.firstChild();
                baseRecordNode.verifyType(CALTreeParserTokenTypes.BASE_RECORD);
                ParseTreeNode baseRecordExprNode = baseRecordNode.firstChild();
                if (baseRecordExprNode != null) {
                    findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, baseRecordExprNode);            
                }
                
                ParseTreeNode fieldModificationListNode = baseRecordNode.nextSibling();
                fieldModificationListNode.verifyType(CALTreeParserTokenTypes.FIELD_MODIFICATION_LIST);
                
                if (firstPass) {
                    
                    //check that there are no duplicate field names e.g. {colour = "red", height = 2.0, colour = "blue"}
                    //is not allowed at this stage because of the duplicate field name colour.
                    
                    //if this is a record literal rather than a record extension, we cannot have a field update
                    //i.e. {colour := "red", height = 2.0} is a static error flagged at this point, whereas
                    //{{} | colour := "red", height = 2.0} will be a type-check error flagged at a latter stage of
                    //compilation.
                    
                    Set<FieldName> fieldNamesSet = new HashSet<FieldName>();
                    
                    for (final ParseTreeNode fieldModificationNode : fieldModificationListNode) {
                    
                        fieldModificationNode.verifyType(CALTreeParserTokenTypes.FIELD_EXTENSION, CALTreeParserTokenTypes.FIELD_VALUE_UPDATE);
                        
                        if (fieldModificationNode.getType() == CALTreeParserTokenTypes.FIELD_VALUE_UPDATE && baseRecordExprNode == null) {
                            //"The field value update operator ':=' can not be used in a record literal value."
                            compiler.logMessage(new CompilerMessage(fieldModificationNode,
                                new MessageKind.Error.FieldValueUpdateOperatorUsedInRecordLiteralValue()));
                        }
                        
                        ParseTreeNode fieldNameNode = fieldModificationNode.firstChild(); 
                        FieldName fieldName = compiler.getTypeChecker().getFieldName(fieldNameNode);                        
                                           
                        if (!fieldNamesSet.add(fieldName)) {
                            // Repeated field name {fieldName.getCalSourceForm()} in record literal value. 
                            compiler.logMessage(new CompilerMessage(fieldNameNode, new MessageKind.Error.RepeatedFieldNameInRecordLiteralValue(fieldName)));
                        }                              
                    }
                    
                }
                
                for (final ParseTreeNode fieldModificationNode : fieldModificationListNode) {
                                             
                    findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, fieldModificationNode.getChild(1));         
                }
                
                return;
            }
            
            case CALTreeParserTokenTypes.SELECT_RECORD_FIELD:
            case CALTreeParserTokenTypes.EXPRESSION_TYPE_SIGNATURE:                
            {
                ParseTreeNode exprNode = parseTree.firstChild();
                findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, exprNode);
                return;
            }
            
            default :
            {
                parseTree.unexpectedParseTreeNode();                            
                return;
            }
        }
    }

    /**
     * Find the free variables occurring in a data constructor field selection expression. 
     * 
     * Also, if firstPass is true, and the expression is a data constructor field selection (dot operator - selection from a 
     * data constructor valued-expression, eg. [1.0].Cons.head), the parse tree for the select node is augmented by addition 
     * of a child node for the qualified var which would appear in the rhs of the alt for a corresponding case expression.
     * The transformation which makes use of this additional node comes in a later in the compilation stage.
     *
     * @param freeVariablesSet
     * @param boundVariablesStack 
     * @param selectNode parse tree for the select expression.
     */
    private void findFreeVariablesInDataConsFieldSelection(Set<String> freeVariablesSet, BoundVariablesStack boundVariablesStack, ParseTreeNode selectNode) {
        
        selectNode.verifyType(CALTreeParserTokenTypes.SELECT_DATA_CONSTRUCTOR_FIELD);
        
        ParseTreeNode exprNode = selectNode.firstChild();
        
        // Find in the expr from which to select.
        findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, exprNode);
        
        ParseTreeNode dcNameNode = exprNode.nextSibling();
        dcNameNode.verifyType(CALTreeParserTokenTypes.QUALIFIED_CONS);
        
        ParseTreeNode fieldNameNode = dcNameNode.nextSibling();
        fieldNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID, CALTreeParserTokenTypes.ORDINAL_FIELD_NAME);
        
        DataConstructor dataConstructor = resolveDataConsName(dcNameNode);
        FieldName fieldName = compiler.getTypeChecker().getFieldName(fieldNameNode);
        
        // Check that the field name exists.
        boolean fieldNameExists = (dataConstructor.getFieldIndex(fieldName) > -1);
        
        if (!fieldNameExists) {
            MessageKind messageKind;
            if (dataConstructor.getArity() == 0) {
                messageKind = new MessageKind.Error.ZeroFieldDataConstructorFieldReference(dataConstructor, fieldName);
            } else {
                messageKind = new MessageKind.Error.UnknownDataConstructorField(dataConstructor, fieldName);
            }
            compiler.logMessage(new CompilerMessage(fieldNameNode, messageKind));
        }

        // Simulate finding in the bound part of a case expr.
        String patternVarName;
        if (fieldNameNode.getType() == CALTreeParserTokenTypes.VAR_ID) {
            // textual field.
            patternVarName = fieldNameNode.getText();
        
        } else {
            // ordinal field.
            
            /* 
             * Don't convert the field name to a valid pattern var if the field name is not the name of a field in the data constructor.
             * This is to guard against subsequent analysis turning up errors against an identifier which doesn't appear in the code.
             * For instance, 
             *   "[1.0].Cons.#1" 
             * 
             *  eventually is transformed to 
             *   case [1.0] of 
             *   Cons {#1=field1} -> field1;
             *   
             * Since #1 is not a field of Cons, the field1 pattern var (on the left) is not bound to it.
             * Further analysis would attempt to bind the field1 identifier (on the right) to something else.
             * This might result in an error using the undefined identifier field1 (if it's not defined), 
             * or an ambiguous reference error (if there were multiple field1's visible).
             */
            if (fieldNameExists) {
                patternVarName = "field" + ((FieldName.Ordinal)fieldName).getOrdinal();
            } else {
                patternVarName = fieldNameNode.getText();
            }
        }
        
        SourcePosition fieldNameSourcePosition = fieldNameNode.getSourcePosition();
        
        ParseTreeNode qualifiedVarNode;
        if (firstPass) {
            // The bound expr node is a node of type QUALIFIED_VAR with the same name as the pattern var.
            // ie. moduleName is unspecified, unqualified name is the pattern var name.
            qualifiedVarNode = ParseTreeNode.makeUnqualifiedVarNode(patternVarName, fieldNameSourcePosition);
            
            //
            // Set the qualified var node for the alt expression as the next sibling.
            // selectNode is "augmented" with this node, and children become: expr qualifiedCons fieldName qualifiedVar
            //
            fieldNameNode.setNextSibling(qualifiedVarNode);
            
        } else {
            qualifiedVarNode = fieldNameNode.nextSibling();
        }
        ParseTreeNode varIdNodeCopy = new ParseTreeNode();
        varIdNodeCopy.copyContentsFrom(qualifiedVarNode.getChild(1));
        
        // Find in the qualified var.
        boundVariablesStack.push(varIdNodeCopy);
        findFreeVariablesInQualifiedVar(freeVariablesSet, boundVariablesStack, qualifiedVarNode);
        boundVariablesStack.popN(1, firstPass);
    }
    
    /**
     * A helper function for findFreeVariables. 
     * 
     * If firstPass is true:
     * It also checks that there are no duplicate patterns in the case expression. Haskell allows for repeated patterns. 
     * The reason for this is that their pattern matching syntax is more complicated, allowing for guards and nested patterns, 
     * and so it becomes more difficult to enforce the constraint that patterns should not overlap. So in Haskell, patterns 
     * are matched sequentially, with the first pattern that matches being taken. In CAL, we can't have duplicate patterns,
     * and the wildcard pattern must be last if present. This hopefully will give users the right intuition 
     * as to how the pattern matching syntax works.
     * 
     * @param freeVariablesSet
     * @param boundVariablesStack
     * @param caseExprNode
     */
    private void findFreeVariablesInCase (Set<String> freeVariablesSet, BoundVariablesStack boundVariablesStack, ParseTreeNode caseExprNode) {
       
        if (firstPass) {
            caseExprNode.verifyType(CALTreeParserTokenTypes.LITERAL_case);
        } else {
            caseExprNode.verifyType(CALTreeParserTokenTypes.VIRTUAL_DATA_CONSTRUCTOR_CASE,
                CALTreeParserTokenTypes.VIRTUAL_RECORD_CASE,
                CALTreeParserTokenTypes.VIRTUAL_TUPLE_CASE);
        }
        
        ParseTreeNode exprNode = caseExprNode.firstChild();
        findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, exprNode);

        ParseTreeNode altListNode = exprNode.nextSibling();
        altListNode.verifyType(CALTreeParserTokenTypes.ALT_LIST);
                        
        final Set<Object> consNamesSet;
        if (firstPass) {
            consNamesSet = new HashSet<Object>();
            //assume the data constructor case, and correct that assumption later if incorrect
            caseExprNode.setType(CALTreeParserTokenTypes.VIRTUAL_DATA_CONSTRUCTOR_CASE);
        } else {
            consNamesSet = null;
        }
        
        for (final ParseTreeNode altNode : altListNode) {

            altNode.verifyType(CALTreeParserTokenTypes.ALT);

            ParseTreeNode patternNode = altNode.firstChild();

            int nodeKind = patternNode.getType();
            switch (nodeKind) {
                
                case CALTreeParserTokenTypes.PATTERN_CONSTRUCTOR :
                {
                    ParseTreeNode dcNameListNode = patternNode.firstChild();
                    dcNameListNode.verifyType(CALTreeParserTokenTypes.DATA_CONSTRUCTOR_NAME_LIST, CALTreeParserTokenTypes.DATA_CONSTRUCTOR_NAME_SINGLETON);
                    
                    ParseTreeNode dcArgBindingsNode = dcNameListNode.nextSibling();  
                    
                    //(Set of DataConstructor objects) The data constructors used in this particular pattern in declaration order.                   
                    final Set<DataConstructor> patternDataConstructors = firstPass ? new LinkedHashSet<DataConstructor>() : null;
                    
                    if (firstPass) {
                        
                        final boolean checkArity = dcArgBindingsNode.getType() == CALTreeParserTokenTypes.PATTERN_VAR_LIST;
                        final int impliedArity = checkArity ? dcArgBindingsNode.getNumberOfChildren() : -1;
                                                                        
                        for (final ParseTreeNode dcNameNode : dcNameListNode) {
                            
                            DataConstructor dataConstructor = resolveDataConsName(dcNameNode);
                            QualifiedName dataConsName = dataConstructor.getName();
                            if (!consNamesSet.add(dataConsName)) {
                                // Repeated pattern {dataConstructor} in case expression. 
                                compiler.logMessage(new CompilerMessage(patternNode, new MessageKind.Error.RepeatedPatternInCaseExpression(dataConsName.getQualifiedName())));
                                break;                                           
                            }
                                                                                   
                            //we check that the implied arity is correct for pattern var list based case unpackings.
                            //we do this here in order to provide a reasonable error position (based on dcNameNode) in the case
                            //that something is wrong. This is because the patternVarList may be empty, in which case it will not
                            //have a source position.
                            if (checkArity) {
                                if (dataConstructor.getArity() != impliedArity) {
                                    // Check that the number of variables expected by the data constructor corresponds to the number actually supplied in the pattern.                            
                                    //"The data constructor {0} must have exactly {1} pattern argument(s) in its case alternative."
                                    compiler.logMessage(new CompilerMessage(dcNameNode, 
                                        new MessageKind.Error.ConstructorMustHaveExactlyNArgsInPattern(dataConstructor)));
                                 }
                            } else {
                                 patternDataConstructors.add(dataConstructor);
                            }                            
                        }                                                                                
                    }
                                                           
                    switch (dcArgBindingsNode.getType()) {
                        case CALTreeParserTokenTypes.PATTERN_VAR_LIST:
                        {
                            findFreeVariablesInCasePatternVarListExpression(freeVariablesSet, boundVariablesStack, dcArgBindingsNode, patternNode.nextSibling());
                            break;
                        }
                        case CALTreeParserTokenTypes.FIELD_BINDING_VAR_ASSIGNMENT_LIST:
                        {                   
                            findFreeVariablesInCasePatternMatchingExpression(freeVariablesSet, boundVariablesStack, dcArgBindingsNode, patternNode.nextSibling(), patternDataConstructors);
                            break;                        
                        }
                        default:
                        {
                            dcArgBindingsNode.unexpectedParseTreeNode();
                            return;
                        }
                    }
                    
                    break;
                }
                
                case CALTreeParserTokenTypes.INT_PATTERN :
                {
                    ParseTreeNode intListNode = patternNode.firstChild();
                    intListNode.verifyType(CALTreeParserTokenTypes.MAYBE_MINUS_INT_LIST);
                    
                    if (firstPass) {
                        
                        for (final ParseTreeNode maybeMinusIntNode : intListNode) {

                            final boolean minus;
                            final ParseTreeNode intLiteralNode;
                            
                            if (maybeMinusIntNode.getType() == CALTreeParserTokenTypes.MINUS) {
                                minus = true;
                                intLiteralNode = maybeMinusIntNode.firstChild();
                            } else {
                                minus = false;
                                intLiteralNode = maybeMinusIntNode;
                            }
                            
                            intLiteralNode.verifyType(CALTreeParserTokenTypes.INTEGER_LITERAL);
                            
                            String symbolText = intLiteralNode.getText();
                            if (minus) {
                                symbolText = "-" + symbolText;
                            }

                            Integer integerValue;
                            try {
                                integerValue = Integer.valueOf(symbolText);
                                
                            } catch (NumberFormatException nfe) {
                                boolean parseableAsBigInteger = true;
                                try {
                                    new BigInteger(symbolText);
                                
                                } catch (NumberFormatException nfe2) {
                                    parseableAsBigInteger = false;
                                }
                                
                                if (parseableAsBigInteger) {
                                    // {symbolText} is outside of range for the Int type.  The valid range is -2147483648 to 2147483647 (inclusive).
                                    compiler.logMessage(new CompilerMessage(maybeMinusIntNode, new MessageKind.Error.IntLiteralOutOfRange(symbolText)));
                                    break;
                                } else {
                                    // Unable to parse {symbolText} to an integer literal.
                                    compiler.logMessage(new CompilerMessage(maybeMinusIntNode, new MessageKind.Error.UnableToParseToIntegerLiteral(symbolText)));
                                    break;  
                                }
                            }
                            
                            // Set the data on the maybeMinusIntNode to be the Integer object.
                            maybeMinusIntNode.setIntegerValueForMaybeMinusIntLiteral(integerValue);
                            
                            // Check against the Integer, not the symbol, to catch adding both -0 and 0.
                            if (!consNamesSet.add(integerValue)) {
                                // Repeated pattern value {value} in case expression.
                                compiler.logMessage(new CompilerMessage(maybeMinusIntNode, new MessageKind.Error.RepeatedPatternValueInCaseExpression(symbolText)));
                                break;                                           
                            }
                        }
                    }
                    
                    ParseTreeNode boundExprNode = patternNode.nextSibling();
                    findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, boundExprNode);

                    break;
                }
                
                case CALTreeParserTokenTypes.CHAR_PATTERN :
                {
                    ParseTreeNode charListNode = patternNode.firstChild();
                    charListNode.verifyType(CALTreeParserTokenTypes.CHAR_LIST);
                    
                    if (firstPass) {
                        
                        for (final ParseTreeNode charLiteralNode : charListNode) {

                            charLiteralNode.verifyType(CALTreeParserTokenTypes.CHAR_LITERAL);
                            
                            String symbolText = charLiteralNode.getText();
                            Character charValue = null;
                            try {
                                char c = StringEncoder.unencodeChar(symbolText);
                                charValue = Character.valueOf(c);
                                
                            } catch (IllegalArgumentException e) {
                                // The encoded character did not have a valid format. 
                                // This should never happen for nodes created by the parser.

                                // Unable to parse {symbolText} to a character literal.
                                compiler.logMessage(new CompilerMessage(charLiteralNode, new MessageKind.Error.UnableToParseToCharacterLiteral(symbolText)));
                            }

                            // Set the data on the charLiteralNode to be the Character object.
                            charLiteralNode.setCharacterValueForCharLiteral(charValue);
                            
                            // Check against the Character, not the symbol, to catch adding unescaped and escaped forms..
                            if (!consNamesSet.add(charValue)) {
                                // Repeated pattern value {value} in case expression.
                                compiler.logMessage(new CompilerMessage(charLiteralNode, new MessageKind.Error.RepeatedPatternValueInCaseExpression(symbolText)));
                                break;                                           
                            }
                        }
                    }
                    
                    ParseTreeNode boundExprNode = patternNode.nextSibling();
                    findFreeVariablesInExpr(freeVariablesSet, boundVariablesStack, boundExprNode);

                    break;
                }
                
                case CALTreeParserTokenTypes.LIST_CONSTRUCTOR :
                case CALTreeParserTokenTypes.COLON :
                case CALTreeParserTokenTypes.UNDERSCORE :                
                {
          
