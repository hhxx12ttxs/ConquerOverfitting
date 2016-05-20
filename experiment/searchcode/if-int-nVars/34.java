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
 * SourceIdentifierFinder.java
 * Creation date: (February 23, 2004)
 * By: Iulian Radu
 */
package org.openquark.cal.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openquark.cal.compiler.CompilerMessage.AbortCompilation;
import org.openquark.cal.util.ArrayStack;


/**
 * Used for finding identifiers in module sources, global functions, lambda definitions, 
 * local function definitions, import and instance declarations for the purposes of various
 * pre-compilation services, such as auto-qualification for code gems and ICE command line expressions,
 * and renaming.
 * 
 * This superclass is responsible for traversing the expression parse trees and invoking its abstract 
 * methods on each identifier reference found. The class also keeps track of any local variable
 * bindings, and identifiers bound to modules via "import using" statements.
 * 
 * Any errors encountered while performing visitation will be logged to a message logger specific to this finder.
 * This logger is available by calling getLogger().
 * 
 * Note that the visitation code is largely copied from FreeVariableFinder. This code has more of 
 * a tendency to get out of date since it is only used for code gems and ICE command line expressions.
 * So, if there is a bug, please update from the corresponding places in FreeVariableFinder.
 * 
 * <P>Creation date: (February 23, 2004)
 * 
 * @param <I> identifier type. The type of the identifier being found e.g. SourceIdentifier, SourceModification
 * @param <V> variable type. The type of the variables being places on the bound variables stack e.g. String, SourceIdentifier
 * 
 * @author Iulian Radu
 */
abstract class SourceIdentifierFinder<I, V> {

    /** Logger for parse error messages */
    private final CompilerMessageLogger logger;
    
    /** Name of the module containing the expressions checked */
    private ModuleName currentModuleName;
    
    /** A Set (of ModuleNames) of the names of modules imported by the current module. */
    private final Set<ModuleName> importedModuleNames = new HashSet<ModuleName>();
    
    /** The module name resolver corresponding to the module containing the expressions to be checked. */
    private ModuleNameResolver moduleNameResolver;
    
    // Mappings (String entity name -> ModuleName module name) of entities imported from 
    // other modules via "import using" statements
    
    private final Map<String, ModuleName> usingFunctionOrClassMethodMap = new HashMap<String, ModuleName>();     
    private final Map<String, ModuleName> usingDataConstructorMap = new HashMap<String, ModuleName>();    
    private final Map<String, ModuleName> usingTypeConstructorMap = new HashMap<String, ModuleName>();      
    private final Map<String, ModuleName> usingTypeClassMap       = new HashMap<String, ModuleName>();
    
    /**
     * Constructor for a SourceIdentifierFinder.
     */
    SourceIdentifierFinder() {
        // Abort finding on fatal.  TODO: Note in javadoc.
        this.logger = new MessageLogger(true);
    }
    
    ModuleName getCurrentModuleName() {
        return currentModuleName;
    }
    
    void setCurrentModuleName(ModuleName currentModuleName) {
        this.currentModuleName = currentModuleName;        
    }
       
    /**
     * @return the logger used to log errors during visitation.
     */
    CompilerMessageLogger getLogger() {
        return logger;
    }
    
    /** @return true if the given name is the name of an imported module. */
    boolean isModuleImported(ModuleName moduleName) {
        return importedModuleNames.contains(moduleName);
    }
    
    ModuleName getModuleForImportedFunctionOrClassMethod(String unqualifiedName) {
        return usingFunctionOrClassMethodMap.get(unqualifiedName);
    }
    
    ModuleName getModuleForImportedDataConstructor(String unqualifiedName) {
        return usingDataConstructorMap.get(unqualifiedName);
    }
    
    ModuleName getModuleForImportedTypeConstructor(String unqualifiedName) {
        return usingTypeConstructorMap.get(unqualifiedName);
    }
    
    ModuleName getModuleForImportedTypeClass(String unqualifiedName) {
        return usingTypeClassMap.get(unqualifiedName);
    }
    
    /**
     * @return the module name resolver corresponding to the module containing the expressions to be checked.
     */
    ModuleNameResolver getModuleNameResolver() {
        return moduleNameResolver;
    }
    
    /**
     * Sets the module name resolver corresponding to the module containing the expressions to be checked.
     * @param moduleNameResolver the module name resolver.
     */
    void setModuleNameResolver(ModuleNameResolver moduleNameResolver) {
        this.moduleNameResolver = moduleNameResolver;
    }

    /**
     * Traverse the passed code expression and return the list of identifiers that occur within it. 
     * The list is ordered as dictated by the getIdentifierListComparator().
     * 
     * This method is intended to be the entry point for traversing parsed code expressions.
     * 
     * @param exprNode ParseTree node defining the code expression
     * @param moduleName name of the module this expression belongs to
     * @param moduleNameResolver the module name resolver for the module to which this expression belongs.
     * @return List of identifiers encountered in the expression. 
     */
    List<I> findIdentifiersInCodeExpression(ParseTreeNode exprNode, ModuleName moduleName, ModuleNameResolver moduleNameResolver) {

        ArrayStack<V> boundVariablesStack = ArrayStack.make();
        List<I> identifierList = new ArrayList<I>();
        setCurrentModuleName(moduleName);
        setModuleNameResolver(moduleNameResolver);
        
        findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);
        
        //it may not be necessary to sort, but it is safe
        Collections.sort(identifierList, getIdentifierListComparator());
        
        return identifierList;
    }
    
    /**
     * Traverse the parsed module source and return the list of identifiers that occur within it. 
     * The list is ordered as dictated by the getIdentifierListComparator().
     * 
     * This method is intended to be the entry point for traversing parsed module sources.
     * 
     * @param moduleDefNode ParseTree node defining the code expression
     * @return List of identifiers encountered in the expression. 
     */
    List<I> findIdentifiersInModule(ParseTreeNode moduleDefNode) {

        List<I> identifierList = new ArrayList<I>();
        setCurrentModuleName(ModuleNameUtilities.getModuleNameFromParseTree(moduleDefNode.getChild(1)));
        
        findIdentifiersInModule(identifierList, moduleDefNode);
        
        // The retrieved identifiers most likely do not appear in order, since parse
        // trees within the module are inspected by category
        Collections.sort(identifierList, getIdentifierListComparator());
        
        return identifierList;
    }
    
    private void findIdentifiersInModule(List<I> identifierList, ParseTreeNode moduleDefnNode) {
        moduleDefnNode.verifyType(CALTreeParserTokenTypes.MODULE_DEFN);

        ParseTreeNode optionalCALDocNode = moduleDefnNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        findIdentifiersInCALDocComment(identifierList, optionalCALDocNode);
        
        ParseTreeNode moduleNameNode = optionalCALDocNode.nextSibling();
        visitModuleNameNode(identifierList, moduleNameNode);
        
        // Check 'import' declarations
        
        ParseTreeNode importDeclarationListNode = moduleNameNode.nextSibling();
        findIdentifiersInImportDeclarations(identifierList, importDeclarationListNode);
        
        //skip over friends. 
        ParseTreeNode friendDeclarationListNode = importDeclarationListNode.nextSibling();
        friendDeclarationListNode.verifyType(CALTreeParserTokenTypes.FRIEND_DECLARATION_LIST);
        
        // Categorize parse trees found in this module for easy access
        
        ParseTreeNode outerDefnListNode = friendDeclarationListNode.nextSibling();
        outerDefnListNode.verifyType(CALTreeParserTokenTypes.OUTER_DEFN_LIST);
        ModuleLevelParseTrees moduleLevelParseTrees = new ModuleLevelParseTrees(moduleDefnNode, outerDefnListNode);
        
        // Check SC declarations and definitions
                        
        for (final ParseTreeNode node : moduleLevelParseTrees.getFunctionTypeDeclarationNodes()) {
            findIdentifiersInFunctionDeclaration(identifierList, node);
        }
               
        for (final ParseTreeNode node : moduleLevelParseTrees.getFunctionDefnNodes()) {
            findIdentifiersInFunctionDefinition(identifierList, node);
        }
        
        // Check type class definitions
             
        for (final ParseTreeNode node : moduleLevelParseTrees.getTypeClassDefnNodes()) {
            findIdentifiersInTypeClassDefinition(identifierList, node);
        }
        
        // Check class instance declarations
                
        for (final ParseTreeNode node : moduleLevelParseTrees.getInstanceDefnNodes()) {
            findIdentifiersInInstanceDefinition(identifierList, node);
        }
        
        // Check data declarations       
        for (final ParseTreeNode node : moduleLevelParseTrees.getDataDeclarationNodes()) {
            findIdentifiersInDataDeclaration(identifierList, node);
        }
        
        // Check foreign SC declarations
               
        for (final ParseTreeNode node : moduleLevelParseTrees.getForeignFunctionDefnNodes()) {
            findIdentifiersInForeignFunctionDeclaration(identifierList, node);
        }
                
        for (final ParseTreeNode node : moduleLevelParseTrees.getForeignDataDeclarationNodes()) {
            findIdentifiersInForeignDataDeclaration(identifierList, node);
        }
        
        return;
    }
    
    /**
     * Find the identifiers occurring in an expression Node. 
     * 
     * For example in: f x = y + x + (let y = 2 in y) the first occurrence of y
     * is the only identifier returned by the AllIdentifierFinder.
     * 
     * @param identifierList
     *            The identifiers encountered while traversing the parse
     *            tree.
     * @param boundVariablesStack
     *            The function is not dependent on bound variables
     *            appearing in its definition. These are its argument
     *            variables, or variables introduced in internal let
     *            declarations or binder variables in a lambda declaration.
     *            This stack varies depending on where we are in the
     *            definition. The same variable name can occur more than once
     *            because of scoping.
     * @param parseTree
     *            expression parse tree 
     */
    void findIdentifiersInExpr(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode parseTree) {

        int nodeType = parseTree.getType();
        
        switch (nodeType) {

            case CALTreeParserTokenTypes.LITERAL_let :
            {
                findIdentifiersInLet(identifierList, boundVariablesStack, parseTree);
                return;
            }

            case CALTreeParserTokenTypes.LAMBDA_DEFN :
            {
                ParseTreeNode paramListNode = parseTree.firstChild();
                paramListNode.verifyType(CALTreeParserTokenTypes.FUNCTION_PARAM_LIST);

                findIdentifiersInLambda(identifierList, boundVariablesStack, paramListNode, paramListNode.nextSibling());
                return;
            }

            case CALTreeParserTokenTypes.LITERAL_case :
            {
                findIdentifiersInCase(identifierList, boundVariablesStack, parseTree);
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
                findIdentifiersInChildExpressions(identifierList, boundVariablesStack, parseTree);
                return;
            }

            case CALTreeParserTokenTypes.APPLICATION :
            {
                for (final ParseTreeNode exprNode : parseTree) {

                    findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);
                }

                return;
            }

            // function names, class method names and variables
            case CALTreeParserTokenTypes.QUALIFIED_VAR :
            {
                visitFunctionOrClassMethodNameNode(identifierList, boundVariablesStack, parseTree);
                return;
            }

            //data constructors
            case CALTreeParserTokenTypes.QUALIFIED_CONS :
            {
                visitDataConsNameNode(identifierList, parseTree);
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
                findIdentifiersInChildExpressions(identifierList, boundVariablesStack, parseTree);
                return;
            }

            //A list data value
            case CALTreeParserTokenTypes.LIST_CONSTRUCTOR :
                findIdentifiersInChildExpressions(identifierList, boundVariablesStack, parseTree);
                return;

            case CALTreeParserTokenTypes.RECORD_CONSTRUCTOR:
            {
                ParseTreeNode baseRecordNode = parseTree.firstChild();
                baseRecordNode.verifyType(CALTreeParserTokenTypes.BASE_RECORD);
                ParseTreeNode baseRecordExprNode = baseRecordNode.firstChild();
                if (baseRecordExprNode != null) {
                    findIdentifiersInExpr(identifierList, boundVariablesStack, baseRecordExprNode);            
                }
                
                ParseTreeNode fieldModificationListNode = baseRecordNode.nextSibling();
                fieldModificationListNode.verifyType(CALTreeParserTokenTypes.FIELD_MODIFICATION_LIST);
                
                for (final ParseTreeNode fieldModificationNode : fieldModificationListNode) {
                    
                    fieldModificationNode.verifyType(CALTreeParserTokenTypes.FIELD_EXTENSION,
                        CALTreeParserTokenTypes.FIELD_VALUE_UPDATE);
                                             
                    findIdentifiersInExpr(identifierList, boundVariablesStack, fieldModificationNode.getChild(1));         
                }
                
                return;
            }
            
            case CALTreeParserTokenTypes.SELECT_RECORD_FIELD:
            {
                ParseTreeNode exprNode = parseTree.firstChild();
                findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);
                return;
            }
            
            case CALTreeParserTokenTypes.SELECT_DATA_CONSTRUCTOR_FIELD:
            {
                // Simulate a case expr where
                // expr.DCName.fieldName    is converted to   case expr of DCName {fieldName} -> fieldName;
                
                // The expression.
                ParseTreeNode exprNode = parseTree.firstChild();
                findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);
                
                // The name of the DC.
                ParseTreeNode dcNameNode = exprNode.nextSibling();
                dcNameNode.verifyType(CALTreeParserTokenTypes.QUALIFIED_CONS);
                visitDataConsNameNode(identifierList, dcNameNode);
                
                // The name of the field.
                ParseTreeNode fieldNameNode = dcNameNode.nextSibling();
                
                // If it's a textual (not an ordinal) field, simulate the case as above.
                if (fieldNameNode.getType() == CALTreeParserTokenTypes.VAR_ID) {
                    
                    String calSourceForm = fieldNameNode.getText();
                    SourcePosition fieldNameSourcePosition = fieldNameNode.getSourcePosition();
                    ParseTreeNode patternVarNode = new ParseTreeNode(CALTreeParserTokenTypes.VAR_ID, calSourceForm, fieldNameSourcePosition);
                    
                    visitLocalVarDeclarationNode(identifierList, boundVariablesStack, patternVarNode, false);
                    
                    // varName is now a bound variable for the body of the lambda
                    pushLocalVar(boundVariablesStack, patternVarNode, identifierList);
                    
                    // The bound expr node is a node of type QUALIFIED_VAR with the same name as the pattern var (~punning).
                    // ie. moduleName is unspecified, unqualified name is the pattern var name.
                    ParseTreeNode qualifiedVarNode = ParseTreeNode.makeUnqualifiedVarNode(calSourceForm, fieldNameSourcePosition);
                    
                    // Find in the expr node..
                    findIdentifiersInExpr(identifierList, boundVariablesStack, qualifiedVarNode);
                    
                    // pop varName..
                    boundVariablesStack.pop();
                }

                return;
            }
            
            case CALTreeParserTokenTypes.EXPRESSION_TYPE_SIGNATURE:
            {
                ParseTreeNode exprNode = parseTree.firstChild();
                findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);
                
                ParseTreeNode typeSignatureNode = exprNode.nextSibling();
                typeSignatureNode.verifyType(CALTreeParserTokenTypes.TYPE_SIGNATURE);
                
                ParseTreeNode contextListNode = typeSignatureNode.firstChild();
                ParseTreeNode declarationNode = contextListNode.nextSibling();
                
                findIdentifiersInDeclaredTypeContext(identifierList, contextListNode);
                findIdentifiersInDeclaredTypeExpr(identifierList, declarationNode);                
                                
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
     * A helper function to find identifiers in case expressions.
     * 
     * @param identifierList 
     * @param boundVariablesStack 
     * @param parseTree 
     *  
     */
    void findIdentifiersInCase(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode parseTree) {

        ParseTreeNode exprNode = parseTree.firstChild();
        findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);

        ParseTreeNode altListNode = exprNode.nextSibling();
        altListNode.verifyType(CALTreeParserTokenTypes.ALT_LIST);

        for (final ParseTreeNode altNode : altListNode) {

            altNode.verifyType(CALTreeParserTokenTypes.ALT);
            ParseTreeNode patternNode = altNode.firstChild();
            int nodeKind = patternNode.getType();
            switch (nodeKind) {

                case CALTreeParserTokenTypes.PATTERN_CONSTRUCTOR :
                {
                    ParseTreeNode dataConsNameListNode = patternNode.firstChild();
                    ParseTreeNode argBindingsNode = dataConsNameListNode.nextSibling();
                    
                    for (final ParseTreeNode qualifiedConsNode : dataConsNameListNode) {
                        
                        visitDataConsNameNode(identifierList, qualifiedConsNode);
                    }
                    
                    switch (argBindingsNode.getType()) {
                        
                        case CALTreeParserTokenTypes.PATTERN_VAR_LIST :
                        {
                            // positional notation
                            findIdentifiersInLambda(identifierList, boundVariablesStack, argBindingsNode, patternNode.nextSibling());
                            break;
                        }
                        case CALTreeParserTokenTypes.FIELD_BINDING_VAR_ASSIGNMENT_LIST :
                        {
                            // matching notation
                            findIdentifiersInFieldBindingCase(identifierList, boundVariablesStack, argBindingsNode, patternNode.nextSibling(), null);
                            break;
                        }
                        default :
                        {                            
                            patternNode.unexpectedParseTreeNode();
                            return;
                        }
                    }
                    break;
                }

                case CALTreeParserTokenTypes.LIST_CONSTRUCTOR :         // null list constructor []
                case CALTreeParserTokenTypes.VIRTUAL_UNIT_DATA_CONSTRUCTOR: // Unit constructor ()
                case CALTreeParserTokenTypes.UNDERSCORE :               // _
                case CALTreeParserTokenTypes.INT_PATTERN :
                case CALTreeParserTokenTypes.CHAR_PATTERN :
                {
                    findIdentifiersInLambda(identifierList, boundVariablesStack, null, patternNode.nextSibling());
                    break;
                }
                
                case CALTreeParserTokenTypes.TUPLE_CONSTRUCTOR :
                case CALTreeParserTokenTypes.COLON :
                {
                    findIdentifiersInLambda(identifierList, boundVariablesStack, patternNode, patternNode.nextSibling());
                    break;
                }
                    
                case CALTreeParserTokenTypes.RECORD_PATTERN:
                {
                    findIdentifiersInRecordCase(identifierList, boundVariablesStack, patternNode);
                    break;                       
                }

                default :
                {                   
                    patternNode.unexpectedParseTreeNode();
                    return;
                }
            }
        }
        
        return;
    }

    /**
     * Finds identifiers in case expression containing a record pattern
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param patternNode
     */
    void findIdentifiersInRecordCase(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode patternNode) {
        ParseTreeNode baseRecordPatternNode = patternNode.firstChild();
        baseRecordPatternNode.verifyType(CALTreeParserTokenTypes.BASE_RECORD_PATTERN);
        
        // Find the name of the base record variable
        ParseTreeNode baseRecordPatternVarNode = baseRecordPatternNode.firstChild();
        ParseTreeNode basePatternVarNameNode = null;
        if (baseRecordPatternVarNode != null) 
        {                    
            switch (baseRecordPatternVarNode.getType())
            {
                case CALTreeParserTokenTypes.VAR_ID:
                    basePatternVarNameNode = baseRecordPatternVarNode;
                    break;
                    
                case CALTreeParserTokenTypes.UNDERSCORE:                              
                    break;
                    
                default:
                {                                                
                    baseRecordPatternVarNode.unexpectedParseTreeNode();
                    return;
                }                            
            }
        }                                                           
       
        ParseTreeNode fieldBindingVarAssignmentListNode = baseRecordPatternNode.nextSibling();
        
        findIdentifiersInFieldBindingCase(identifierList, boundVariablesStack, fieldBindingVarAssignmentListNode, patternNode.nextSibling(), basePatternVarNameNode);
    }

    /**
     * Finds identifiers in a case expression containing a field binding pattern.
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param fieldBindingVarAssignmentListNode
     * @param boundExprNode
     * @param basePatternVarNameNode if non-null, the parse tree node for the named variable forming the base pattern for the field binding.
     */
    void findIdentifiersInFieldBindingCase(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode fieldBindingVarAssignmentListNode, ParseTreeNode boundExprNode, ParseTreeNode basePatternVarNameNode) {
        // Set pattern variables for punned fields
        unpunPunnedFields(fieldBindingVarAssignmentListNode);

        int nVars;
        if (basePatternVarNameNode != null) {
            nVars = 1;
            visitLocalVarDeclarationNode(identifierList, boundVariablesStack, basePatternVarNameNode, false);
            pushLocalVar(boundVariablesStack, basePatternVarNameNode, identifierList);                                                                       
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
                    ++nVars;
                    visitLocalVarDeclarationNode(identifierList, boundVariablesStack, patternVarNode, false);
                    pushLocalVar(boundVariablesStack, patternVarNode, identifierList);
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
        
        findIdentifiersInExpr(identifierList, boundVariablesStack, boundExprNode);
        boundVariablesStack.popN(nVars);
    }

    /**
     * Patch up the parse tree so that in subsequent analysis we can assume that punning doesn't occur.
     * 
     * In the case of textual field names, punning means: fieldName ---> fieldName = fieldName
     * In the case of numeric field names, punning means: fieldName ---> fieldName = _
     * This is because something like #2 is a valid numeric field name but not a valid CAL variable name.
     * 
     * @param fieldBindingVarAssignmentListNode the parse tree node for the list of field binding var assignments.
     */
    static void unpunPunnedFields(ParseTreeNode fieldBindingVarAssignmentListNode) {
        fieldBindingVarAssignmentListNode.verifyType(CALTreeParserTokenTypes.FIELD_BINDING_VAR_ASSIGNMENT_LIST);
        for (final ParseTreeNode fieldBindingVarAssignmentNode : fieldBindingVarAssignmentListNode) {
            
            fieldBindingVarAssignmentNode.verifyType(CALTreeParserTokenTypes.FIELD_BINDING_VAR_ASSIGNMENT);
            ParseTreeNode fieldNameNode = fieldBindingVarAssignmentNode.firstChild();
            fieldNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID, CALTreeParserTokenTypes.ORDINAL_FIELD_NAME);
            
            ParseTreeNode patternVarNode = fieldNameNode.nextSibling();
            if (patternVarNode == null) {
                
                if (fieldNameNode.getType() == CALTreeParserTokenTypes.VAR_ID) {
                    //textual field names
                    patternVarNode = new ParseTreeNode();
                    patternVarNode.copyContentsFrom(fieldNameNode);
                } else {
                    //numeric field names
                    patternVarNode = new ParseTreeNode(CALTreeParserTokenTypes.UNDERSCORE, "_");
                }
                
                fieldNameNode.setNextSibling(patternVarNode);
            }                                                                                   
        }                                                                               
    }
    
    /**
     * A helper function that finds the names in each of the child expressions of parseTree.
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param parseTree
     * 
     */
    void findIdentifiersInChildExpressions(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode parseTree) {

        for (final ParseTreeNode exprNode : parseTree) {

            findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);
        }
        return;
    }

    /**
     * Helper function to find names in lambda expressions.
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param patternVarListNode if non-null, the parent node of the parse tree nodes for pattern vars.
     * @param boundExprNode 
     *  
     */
    final void findIdentifiersInLambda(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode patternVarListNode, ParseTreeNode boundExprNode) {
        findIdentifiersInLambda(identifierList, boundVariablesStack, patternVarListNode, boundExprNode, null);
    }
    
    /**
     * Helper function to find names in lambda expressions and local function definitions.
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param patternVarListNode if non-null, the parent node of the parse tree nodes for pattern vars.
     * @param boundExprNode
     * @param optionalCALDocNodeForLetDefn
     */
    void findIdentifiersInLambda(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode patternVarListNode, ParseTreeNode boundExprNode, ParseTreeNode optionalCALDocNodeForLetDefn) {

        int nVars = 0;
        
        if (patternVarListNode != null) {
            
            for (final ParseTreeNode patternVarNode : patternVarListNode) {
                
                switch (patternVarNode.getType()) {
                    
                    case CALTreeParserTokenTypes.VAR_ID:
                    case CALTreeParserTokenTypes.LAZY_PARAM:
                    case CALTreeParserTokenTypes.STRICT_PARAM:                
                    {
                        ++nVars;
                        
                        // varName is now a bound variable for the body of the
                        // lambda
                        visitLocalVarDeclarationNode(identifierList, boundVariablesStack, patternVarNode, false);
                        pushLocalVar(boundVariablesStack, patternVarNode, identifierList);
                        break;
                    }
                    
                    case CALTreeParserTokenTypes.UNDERSCORE :
                        break;
                        
                    default :
                    {
                        // Unexpected type
                        boundVariablesStack.popN(nVars);                        
                        patternVarNode.unexpectedParseTreeNode();
                        return;
                    }
                }
            }
        }

        // if there is a CALDoc comment that needs to be checked with the parameters in scope, do it now
        if (optionalCALDocNodeForLetDefn != null) {
            findIdentifiersInCALDocComment(identifierList, boundVariablesStack, optionalCALDocNodeForLetDefn);
        }
        
        findIdentifiersInExpr(identifierList, boundVariablesStack, boundExprNode);
        
        boundVariablesStack.popN(nVars);
        return;
    }

    /**
     * A helper function for finding identifiers in Let expressions.
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param parseTree
     */
    void findIdentifiersInLet(List<I> identifierList, ArrayStack<V> boundVariablesStack, ParseTreeNode parseTree) {

        ParseTreeNode defnListNode = parseTree.firstChild();
        defnListNode.verifyType(CALTreeParserTokenTypes.LET_DEFN_LIST);
        int nLocalFunctions = 0;
        
        List<ParseTreeNode> typeDeclNodes = new ArrayList<ParseTreeNode>();
        List<ParseTreeNode> letDefnAndLocalPatternMatchDeclNodes = new ArrayList<ParseTreeNode>();
        
        // First, collect all the optional CALDoc nodes (for later checking)
        Map/*String, ParseTreeNode*/<String, ParseTreeNode> funcNamesToOptionalCALDocNodes = new HashMap<String, ParseTreeNode>();
        
        for (final ParseTreeNode defnNode : defnListNode) {
            
            defnNode.verifyType(CALTreeParserTokenTypes.LET_DEFN, CALTreeParserTokenTypes.LET_DEFN_TYPE_DECLARATION, CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL);
            
            if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN) {
                ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                
                ParseTreeNode localSCNameNode = optionalCALDocNode.nextSibling();
                localSCNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                String scName = localSCNameNode.getText();
                
                if (optionalCALDocNode.firstChild() != null) {
                    funcNamesToOptionalCALDocNodes.put(scName, optionalCALDocNode);
                }
                
            } else if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN_TYPE_DECLARATION) {
                
                ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                
                ParseTreeNode typeDeclNode = optionalCALDocNode.nextSibling();
                typeDeclNode.verifyType(CALTreeParserTokenTypes.TYPE_DECLARATION);
                 
                ParseTreeNode localSCNameNode = typeDeclNode.firstChild();
                localSCNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                String scName = localSCNameNode.getText();
                
                if (optionalCALDocNode.firstChild() != null) {
                    funcNamesToOptionalCALDocNodes.put(scName, optionalCALDocNode);
                }
                
            } else { // must be a LET_PATTERN_MATCH_DECL
                // there is no CALDoc associated with a local pattern mathc declaration
            }
        }
        
        // Then, collect all the local variable bindings
    
        for (final ParseTreeNode defnNode : defnListNode) {

            switch (defnNode.getType()) {
                case (CALTreeParserTokenTypes.LET_DEFN): 
                {
                    ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                    optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                    
                    ParseTreeNode localFunctionNameNode = optionalCALDocNode.nextSibling();
                    localFunctionNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                    ++nLocalFunctions;
                    visitLocalVarDeclarationNode(identifierList, boundVariablesStack, localFunctionNameNode, false);
    
                    // functionName is a bound variable for all declarations in the 'let'
                    // and for the expression following the 'in'.
                    pushLocalVar(boundVariablesStack, localFunctionNameNode, identifierList);
                    
                    letDefnAndLocalPatternMatchDeclNodes.add(defnNode);
                    break;
                }
            
                case (CALTreeParserTokenTypes.LET_DEFN_TYPE_DECLARATION):
                {
                    ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                    optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                    
                    ParseTreeNode typeDeclNode = optionalCALDocNode.nextSibling();
                    typeDeclNode.verifyType(CALTreeParserTokenTypes.TYPE_DECLARATION);
                     
                    typeDeclNodes.add(typeDeclNode); 
                    break;
                }
                
                case CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL:
                {
                    // A local pattern match declaration can declare one or more locally bound variables
                    // We will loop through the pattern to process each one
                    
                    letDefnAndLocalPatternMatchDeclNodes.add(defnNode);
                    
                    final ParseTreeNode patternMatchPatternNode = defnNode.firstChild();

                    switch (patternMatchPatternNode.getType()) {
                        case CALTreeParserTokenTypes.PATTERN_CONSTRUCTOR:
                        {
                            // a data cons pattern
                            // e.g. let Cons x y = foo; ...
                            
                            ParseTreeNode dcNameListNode = patternMatchPatternNode.firstChild();
                            dcNameListNode.verifyType(CALTreeParserTokenTypes.DATA_CONSTRUCTOR_NAME_LIST, CALTreeParserTokenTypes.DATA_CONSTRUCTOR_NAME_SINGLETON);
                            
                            ParseTreeNode dcArgBindingsNode = dcNameListNode.nextSibling();  
                            
                            final ParseTreeNode dcNameNode = dcNameListNode.firstChild();
    
                            visitDataConsNameNode(identifierList, dcNameNode);

                            switch (dcArgBindingsNode.getType()) {
                            case CALTreeParserTokenTypes.PATTERN_VAR_LIST:
                                // a data cons pattern with positional patterns
                                // e.g. let Cons x y = foo; ...
                                
                                for (final ParseTreeNode patternVarNode : dcArgBindingsNode) {

                                    if (patternVarNode.getType() == CALTreeParserTokenTypes.VAR_ID) {
                                        
                                        ++nLocalFunctions;
                                        visitLocalVarDeclarationNode(identifierList, boundVariablesStack, patternVarNode, false);

                                        // the pattern variable is a bound variable for all declarations in the 'let'
                                        // and for the expression following the 'in'.
                                        pushLocalVar(boundVariablesStack, patternVarNode, identifierList);
                                    }
                                }
                                break;
    
                            case CALTreeParserTokenTypes.FIELD_BINDING_VAR_ASSIGNMENT_LIST:
                                // a data cons pattern with field-pattern pairs
                                // e.g. let Cons {head=x, tail} = foo; ...
                                
                                // Set pattern variables for punned fields
                                unpunPunnedFields(dcArgBindingsNode);
                                
                                for (final ParseTreeNode fieldBindingVarAssignmentNode : dcArgBindingsNode) {

                                    final ParseTreeNode fieldNameNode = fieldBindingVarAssignmentNode.firstChild();
                                    final ParseTreeNode patternVarNode = fieldNameNode.nextSibling();

                                    if (patternVarNode.getType() == CALTreeParserTokenTypes.VAR_ID) {
                                        
                                        ++nLocalFunctions;
                                        visitLocalVarDeclarationNode(identifierList, boundVariablesStack, patternVarNode, false);

                                        // the pattern variable is a bound variable for all declarations in the 'let'
                                        // and for the expression following the 'in'.
                                        pushLocalVar(boundVariablesStack, patternVarNode, identifierList);
                                    }
                                }
                                break;
    
                            default:
                                dcArgBindingsNode.unexpectedParseTreeNode();
                                break;
                            }
                            
                            break;
                        }
    
                        case CALTreeParserTokenTypes.COLON:
                        case CALTreeParserTokenTypes.TUPLE_CONSTRUCTOR:
                        {
                            // a list cons pattern and a tuple pattern can be treated in a similar way, because
                            // in both cases the node's children is the list of patterns
                            
                            // list cons pattern, e.g. let a:b = foo; ...
                            // tuple pattern, e.g. let (a, b, c) = foo; ...
                            
                            for (final ParseTreeNode patternVarNode : patternMatchPatternNode) {

                                if (patternVarNode.getType() == CALTreeParserTokenTypes.VAR_ID) {
                                    
                                    ++nLocalFunctions;
                                    visitLocalVarDeclarationNode(identifierList, boundVariablesStack, patternVarNode, false);

                                    // the pattern variable is a bound variable for all declarations in the 'let'
                                    // and for the expression following the 'in'.
                                    pushLocalVar(boundVariablesStack, patternVarNode, identifierList);
                                }
                            }
                            break;
                        }
    
                        case CALTreeParserTokenTypes.RECORD_PATTERN:
                        {
                            // a record pattern
                            // e.g. let {_ | a, b=y} = foo; ...
                            
                            final ParseTreeNode baseRecordPatternNode = patternMatchPatternNode.firstChild();
                            baseRecordPatternNode.verifyType(CALTreeParserTokenTypes.BASE_RECORD_PATTERN);
                            
                            final ParseTreeNode fieldBindingVarAssignmentListNode = baseRecordPatternNode.nextSibling();                    
                            
                            // Set pattern variables for punned fields
                            unpunPunnedFields(fieldBindingVarAssignmentListNode);
                            
                            for (final ParseTreeNode fieldBindingVarAssignmentNode : fieldBindingVarAssignmentListNode) {

                                final ParseTreeNode fieldNameNode = fieldBindingVarAssignmentNode.firstChild();
                                final ParseTreeNode patternVarNode = fieldNameNode.nextSibling();

                                if (patternVarNode.getType() == CALTreeParserTokenTypes.VAR_ID) {
                                    
                                    ++nLocalFunctions;
                                    visitLocalVarDeclarationNode(identifierList, boundVariablesStack, patternVarNode, false);

                                    // the pattern variable is a bound variable for all declarations in the 'let'
                                    // and for the expression following the 'in'.
                                    pushLocalVar(boundVariablesStack, patternVarNode, identifierList);
                                }
                            }
                            break;
                        }
    
                        default:
                        {
                            patternMatchPatternNode.unexpectedParseTreeNode();
                            break;
                        }
                    }
                    break;
                }
                
                default:
                {
                    defnListNode.unexpectedParseTreeNode();
                    break;
                }
            }
        }
        
        // Then scan the type declarations
        
        for (final ParseTreeNode defnNode : typeDeclNodes) {

            ParseTreeNode localSCNameNode = defnNode.firstChild();

            localSCNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
            visitLocalVarDeclarationNode(identifierList, boundVariablesStack, localSCNameNode, true);
        
            findIdentifiersInTypeDecl(identifierList, defnNode);
        }
        
        // Now visit the definitions of each variable 

        for (final ParseTreeNode defnNode : letDefnAndLocalPatternMatchDeclNodes) {

            if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN) {
            
                ParseTreeNode localFunctionNameNode = defnNode.getChild(1);
                localFunctionNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                String scName = localFunctionNameNode.getText();

                ParseTreeNode varListNode = localFunctionNameNode.nextSibling();

                // get the optional CALDoc node stored in the map.
                // this may be the CALDoc associated with the corresponding function type declaration, or if
                // there is none, the one associated with this function definition itself.
                ParseTreeNode optionalCALDocNodeFromMap = funcNamesToOptionalCALDocNodes.get(scName);

                findIdentifiersInLambda(identifierList, boundVariablesStack, varListNode, varListNode.nextSibling(), optionalCALDocNodeFromMap);
                
            } else {
                
                defnNode.verifyType(CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL);

                final ParseTreeNode patternMatchPatternNode = defnNode.firstChild();
                final ParseTreeNode patternMatchExprNode = patternMatchPatternNode.nextSibling();
                
                findIdentifiersInLambda(identifierList, boundVariablesStack, null, patternMatchExprNode);
            }
        }

        ParseTreeNode exprNode = defnListNode.nextSibling();
        findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);

        boundVariablesStack.popN(nLocalFunctions);
        return;
    }
    
    /**
     * Determines the identifiers in a parseTree describing a type expression. 
     * 
     * @param identifierList
     * @param parseTree 
     */
    void findIdentifiersInDeclaredTypeExpr(List<I> identifierList, ParseTreeNode parseTree) {
    
        switch (parseTree.getType()) {
            case CALTreeParserTokenTypes.FUNCTION_TYPE_CONSTRUCTOR :
            {
                ParseTreeNode domainNode = parseTree.firstChild();
                findIdentifiersInDeclaredTypeExpr(identifierList, domainNode);
                findIdentifiersInDeclaredTypeExpr(identifierList, domainNode.nextSibling());
                return;
            }

            case CALTreeParserTokenTypes.TUPLE_TYPE_CONSTRUCTOR :
            {               
                if (parseTree.hasNoChildren()) {
                    return;
                }

                if (parseTree.hasExactlyOneChild()) {
                    // the type (t) is equivalent to the type t.
                    findIdentifiersInDeclaredTypeExpr(identifierList, parseTree.firstChild());
                    return;
                }

                for (final ParseTreeNode componentNode : parseTree) {

                    findIdentifiersInDeclaredTypeExpr(identifierList, componentNode);
                }
                return;
            }

            case CALTreeParserTokenTypes.LIST_TYPE_CONSTRUCTOR :
            {
                findIdentifiersInDeclaredTypeExpr(identifierList, parseTree.firstChild());
                return;
            }
    
            case CALTreeParserTokenTypes.TYPE_APPLICATION :
            {
                for (final ParseTreeNode argNode : parseTree) {
                    findIdentifiersInDeclaredTypeExpr(identifierList, argNode);
                }

                return;
            }
    
            case CALTreeParserTokenTypes.QUALIFIED_CONS :
            {
                visitTypeConsNameNode(identifierList, parseTree);
                return;
            }
    
            case CALTreeParserTokenTypes.VAR_ID :
            {
                return;
            }
                
            case CALTreeParserTokenTypes.RECORD_TYPE_CONSTRUCTOR :
            {
                ParseTreeNode recordVarNode = parseTree.firstChild();
                recordVarNode.verifyType(CALTreeParserTokenTypes.RECORD_VAR);
                
                ParseTreeNode fieldTypeAssignmentListNode = recordVarNode.nextSibling();
                fieldTypeAssignmentListNode.verifyType(CALTreeParserTokenTypes.FIELD_TYPE_ASSIGNMENT_LIST);
                
                for (final ParseTreeNode fieldTypeAssignmentNode : fieldTypeAssignmentListNode) {
                         
                    fieldTypeAssignmentNode.verifyType(CALTreeParserTokenTypes.FIELD_TYPE_ASSIGNMENT);
                    ParseTreeNode fieldNameNode = fieldTypeAssignmentNode.firstChild();
                    
                    ParseTreeNode typeNode = fieldNameNode.nextSibling();
                    findIdentifiersInDeclaredTypeExpr(identifierList, typeNode);                   
                }
                                
                return;           
            }
    
            default :
            {                
                parseTree.unexpectedParseTreeNode();
            }
        }
    }
    
    /**
     * Finds identifiers in a type declaration.
     * 
     * @param identifierList
     * @param typeDeclarationNode
     * 
     */
    void findIdentifiersInTypeDecl(List<I> identifierList, ParseTreeNode typeDeclarationNode) {

        typeDeclarationNode.verifyType(CALTreeParserTokenTypes.TYPE_DECLARATION);

        ParseTreeNode functionNameNode = typeDeclarationNode.firstChild();
        functionNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
        ParseTreeNode typeSignatureNode = functionNameNode.nextSibling();
        findIdentifiersInTypeSignature(identifierList, typeSignatureNode);        
        
        return;
    }
    
    /**
     * Finds identifiers froma a class context node.
     * 
     * The context specifies the type variables that are qualified by type class constraints.
     * 
     * @param identifierList
     * @param contextListNode
     */
    void findIdentifiersInClassContext(List<I> identifierList, ParseTreeNode contextListNode) {
        contextListNode.verifyType(CALTreeParserTokenTypes.CLASS_CONTEXT_LIST, CALTreeParserTokenTypes.CLASS_CONTEXT_SINGLETON, CALTreeParserTokenTypes.CLASS_CONTEXT_NOTHING);
        
        for (final ParseTreeNode contextNode : contextListNode) {
            contextNode.verifyType(CALTreeParserTokenTypes.CLASS_CONTEXT);
            ParseTreeNode typeClassNameNode = contextNode.firstChild();
            typeClassNameNode.verifyType(CALTreeParserTokenTypes.QUALIFIED_CONS);
            visitClassNameNode(identifierList, typeClassNameNode);
        }
    }
    
    /**
     * Finds identifiers from a type context node.
     *  
     * The context specifies
     * a. the type variables that are qualified by type class constraints.
     * b. the row variables that have lacks fields constraints.
     *
     * @param identifierList
     * @param contextListNode
     */
    void findIdentifiersInDeclaredTypeContext(List<I> identifierList, ParseTreeNode contextListNode) {
        
        contextListNode.verifyType(CALTreeParserTokenTypes.TYPE_CONTEXT_LIST, CALTreeParserTokenTypes.TYPE_CONTEXT_NOTHING, CALTreeParserTokenTypes.TYPE_CONTEXT_SINGLETON);
       
        for (final ParseTreeNode contextNode : contextListNode) {

            switch (contextNode.getType())
            { 
                case CALTreeParserTokenTypes.CLASS_CONTEXT :
                {
                    ParseTreeNode typeClassNameNode = contextNode.firstChild();
                    typeClassNameNode.verifyType(CALTreeParserTokenTypes.QUALIFIED_CONS);
                    visitClassNameNode(identifierList, typeClassNameNode);
                    break;
                }
                    
                case CALTreeParserTokenTypes.LACKS_FIELD_CONTEXT:
                {                   
                   break;
                }
               
                default:
                {                    
                    contextNode.unexpectedParseTreeNode();
                }
            }
        }
        
        return;
    }
    
    void findIdentifiersInFunctionDefinition(List<I> identifierList, ParseTreeNode functionNode) {
        functionNode.verifyType(CALTreeParserTokenTypes.TOP_LEVEL_FUNCTION_DEFN);

        ParseTreeNode optionalCALDocNode = functionNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        findIdentifiersInCALDocComment(identifierList, optionalCALDocNode);
        
        ParseTreeNode accessModifierNode = optionalCALDocNode.nextSibling();
        
        ParseTreeNode functionNameNode = accessModifierNode.nextSibling();
        visitFunctionOrClassMethodDefinitionNameNode(identifierList, functionNameNode);
                
        ParseTreeNode paramListNode = accessModifierNode.nextSibling().nextSibling();
        paramListNode.verifyType(CALTreeParserTokenTypes.FUNCTION_PARAM_LIST);
        
        ArrayStack<V> namedArgumentsStack = ArrayStack.<V>make();        
        for (final ParseTreeNode varNode : paramListNode) {

            varNode.verifyType(CALTreeParserTokenTypes.LAZY_PARAM, CALTreeParserTokenTypes.STRICT_PARAM);
            //todoBI 
            //todo-jowong this doesn't work for the AllIdentifierFinder, but this method is never called in that case
            //since the AllIndentifier finder happens to only be used for code-gems, and code gems do not have
            //top level functions.
            pushLocalVar(namedArgumentsStack, varNode, null);          
        }
                
        findIdentifiersInExpr(identifierList, namedArgumentsStack, paramListNode.nextSibling());
    }
    
    void findIdentifiersInFunctionDeclaration(List<I> identifierList, ParseTreeNode topLevelTypeDeclarationNode) {
        topLevelTypeDeclarationNode.verifyType(CALTreeParserTokenTypes.TOP_LEVEL_TYPE_DECLARATION);
                  
        ParseTreeNode optionalCALDocNode = topLevelTypeDeclarationNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        findIdentifiersInCALDocComment(identifierList, optionalCALDocNode);
        
        ParseTreeNode functionTypeDeclarationNode = optionalCALDocNode.nextSibling();
        ParseTreeNode functionNameNode = functionTypeDeclarationNode.firstChild();
        visitFunctionOrClassMethodDefinitionNameNode(identifierList, functionNameNode);
        
        ParseTreeNode typeSignatureNode = functionNameNode.nextSibling();
        findIdentifiersInTypeSignature(identifierList, typeSignatureNode);
    }
    
    void findIdentifiersInTypeSignature(List<I> identifierList, ParseTreeNode typeSignatureNode) {
        typeSignatureNode.verifyType(CALTreeParserTokenTypes.TYPE_SIGNATURE);
        
        ParseTreeNode contextListNode = typeSignatureNode.firstChild();
        ParseTreeNode declarationNode = contextListNode.nextSibling();
        
        findIdentifiersInDeclaredTypeContext(identifierList, contextListNode);
        findIdentifiersInDeclaredTypeExpr(identifierList, declarationNode);
    }
    
    void findIdentifiersInDataDeclaration(List<I> identifierList, ParseTreeNode dataDeclarationNode) {
        dataDeclarationNode.verifyType(CALTreeParserTokenTypes.DATA_DECLARATION);
        
        ParseTreeNode optionalCALDocNode = dataDeclarationNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        findIdentifiersInCALDocComment(identifierList, optionalCALDocNode);
        
        ParseTreeNode accessModifierNode = optionalCALDocNode.nextSibling();
        ParseTreeNode typeConsNameNode = accessModifierNode.nextSibling();
        visitTypeConsDefnNameNode(identifierList, typeConsNameNode);
        
        ParseTreeNode typeConsParamListNode = typeConsNameNode.nextSibling();
        ParseTreeNode dataConsDefnListNode = typeConsParamListNode.nextSibling();
        dataConsDefnListNode.verifyType(CALTreeParserTokenTypes.DATA_CONSTRUCTOR_DEFN_LIST);
        
        for (final ParseTreeNode dataConsDefnNode : dataConsDefnListNode) {
            
            dataConsDefnNode.verifyType(CALTreeParserTokenTypes.DATA_CONSTRUCTOR_DEFN);
            
            ParseTreeNode dataConsOptionalCALDocNode = dataConsDefnNode.firstChild();
            dataConsOptionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
            findIdentifiersInCALDocComment(identifierList, dataConsOptionalCALDocNode);

            ParseTreeNode dataConsAccessModifierNode = dataConsOptionalCALDocNode.nextSibling();
            ParseTreeNode dataConsNameNode = dataConsAccessModifierNode.nextSibling();
            visitDataConsDefnNameNode(identifierList, dataConsNameNode);
            
            ParseTreeNode dataConsArgListNode = dataConsNameNode.nextSibling();
            for(ParseTreeNode dataConsArgNode = dataConsArgListNode.firstChild();
                dataConsArgNode != null;
                dataConsArgNode = dataConsArgNode.nextSibling()) {
                
                dataConsArgNode.verifyType(CALTreeParserTokenTypes.DATA_CONSTRUCTOR_NAMED_ARG);
                
                // Get the arg name node.
                ParseTreeNode dataConsArgNameNode = dataConsArgNode.firstChild();
                
                // the arg name doesn't become an identifier..
                
                // Get the type node.
                ParseTreeNode maybePlingTypeExprNode = dataConsArgNameNode.nextSibling();
                
                ParseTreeNode dataConsArgTypeNode;
                if (maybePlingTypeExprNode.getType() == CALTreeParserTokenTypes.STRICT_ARG) {
                    dataConsArgTypeNode = maybePlingTypeExprNode.firstChild();
                } else {
                    dataConsArgTypeNode = maybePlingTypeExprNode;                        
                }

                findIdentifiersInDeclaredTypeExpr(identifierList, dataConsArgTypeNode);                
            }
        }
        
        ParseTreeNode derivingClauseNode = dataConsDefnListNode.nextSibling();
        findIndentifiersInDerivingClauseNode(identifierList, derivingClauseNode);
    }
    
    void findIdentifiersInTypeClassDefinition(List<I> identifierList, ParseTreeNode typeClassNode) {
        typeClassNode.verifyType(CALTreeParserTokenTypes.TYPE_CLASS_DEFN);
        
        ParseTreeNode optionalCALDocNode = typeClassNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        findIdentifiersInCALDocComment(identifierList, optionalCALDocNode);
        
        ParseTreeNode accessModifierNode = optionalCALDocNode.nextSibling();
        ParseTreeNode classContextListNode = accessModifierNode.nextSibling();       
        ParseTreeNode typeClassNameNode = classContextListNode.nextSibling();
        findIdentifiersInClassContext(identifierList, classContextListNode);
        visitClassDefnNameNode(identifierList, typeClassNameNode);
        
        ParseTreeNode typeVarNode = typeClassNameNode.nextSibling();
        typeVarNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
        
        ParseTreeNode classMethodListNode = typeVarNode.nextSibling();
        classMethodListNode.verifyType(CALTreeParserTokenTypes.CLASS_METHOD_LIST);
        for (final ParseTreeNode classMethodNode : classMethodListNode) {
            
            classMethodNode.verifyType(CALTreeParserTokenTypes.CLASS_METHOD);
            
            ParseTreeNode classMethodOptionalCALDocNode = classMethodNode.firstChild();
            classMethodOptionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
            findIdentifiersInCALDocComment(identifierList, classMethodOptionalCALDocNode);

            ParseTreeNode classMethodAccessModifierNode = classMethodOptionalCALDocNode.nextSibling();
         
