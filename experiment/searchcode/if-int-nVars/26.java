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
 * RenamedIdentifierFinder.java
 * Creation date: (June 22, 2004)
 * By: Iulian Radu
 */
package org.openquark.cal.compiler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openquark.cal.compiler.SourceIdentifier.Category;
import org.openquark.cal.util.ArrayStack;

/**
 * Traverses the specified AST with the purpose of renaming a specified identifier. 
 * 
 * This class is deprecated; it's only used by the GemCutter's code gem editing 
 * capabilities, and we'd like to remove it entirely at some point in the near
 * future.  For renaming toplevel elements (functions, classes, type constructors, 
 * and data constructors), this class has been superseded by the IdentifierRenamer
 * class.
 * 
 * This class still contains logical for "collateral damage" handling (where conflicting
 * bindings are renamed to be unique), but that no longer reflects the way that we handle
 * binding conflicts.
 * 
 * @author Iulian Radu
 */
final class RenamedIdentifierFinder extends SourceIdentifierFinder<SourceModification, String> {
    
    
    /**
     * Finds occurrences of identifiers which, when renamed, will conflict with locally 
     * bound variable names.
     * 
     * ex: within the expression "let r = 1.0; in s + r", identifier "s" will cause
     *     a conflict when renamed to "r"
     * 
     * @author Iulian Radu
     */
    private static final class RenameConflictFinder extends SourceIdentifierFinder<SourceIdentifier, String> {
        
        /** Category of identifier being renamed */ 
        private final SourceIdentifier.Category renameCategory;
        
        /** Old name of the identifier */
        private final QualifiedName renameOldName;
        
        /** New name of the identifier */
        private final QualifiedName renameNewName;
        
        /** Constructor */
        RenameConflictFinder(ModuleName currentModuleName, QualifiedName oldName, QualifiedName newName, SourceIdentifier.Category category) {
            setCurrentModuleName(currentModuleName);
            this.renameOldName = oldName;
            this.renameNewName = newName;
            this.renameCategory = category;
        }

        /**
         * Adds the current identifier to the list if it is to be renamed and its new name conflicts
         * with a locally bound variable.               
         */
        @Override
        void visitFunctionOrClassMethodNameNode(List<SourceIdentifier> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode qualifiedNode) {

            qualifiedNode.verifyType(CALTreeParserTokenTypes.QUALIFIED_VAR);
            ParseTreeNode moduleNameNode = qualifiedNode.firstChild();
            visitModuleNameNode(identifierList, moduleNameNode);
            ModuleName rawModuleName = ModuleNameUtilities.getModuleNameOrNullFromParseTree(moduleNameNode);
            ModuleName moduleName = getModuleNameResolver().resolve(rawModuleName).getResolvedModuleName();
            ParseTreeNode varNameNode = moduleNameNode.nextSibling();
            String varName = varNameNode.getText();

            if (
                ((moduleName == null) || (moduleName.equals(getCurrentModuleName()))) &&
                boundVariablesStack.contains(varName)) {

                // Variable is pattern bound (i.e. an argument variable, a lambda bound 
                // variable or a variable bound by a case alternative). Its module is the current module.
                return;
            }
            
            if ( varName.equals(renameOldName.getUnqualifiedName()) && (renameCategory == SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD) &&
                boundVariablesStack.contains(renameNewName)) {

                // We are renaming this variable, but we are renaming it to something which
                // already exists on the stack (bound by argument, let, lambda, or case). 
            
                // So mark this as impossible to change
                ModuleName minimallyQualifiedModuleName = getModuleNameResolver().getMinimallyQualifiedModuleName(moduleName);
                identifierList.add(new SourceIdentifier.Qualifiable(varName, varNameNode.getSourceRange(), SourceIdentifier.Category.LOCAL_VARIABLE, rawModuleName, moduleName, minimallyQualifiedModuleName, moduleNameNode.getAssemblySourceRange()));
            }
            
            return;
        }
        
        @Override
        void visitCALDocArgNameNode(List<SourceIdentifier> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode argNameNode) {}
        @Override
        void visitDataConsDefnNameNode(List<SourceIdentifier> identifierList, ParseTreeNode dataConsNameNode) {}
        @Override
        void visitTypeConsDefnNameNode(List<SourceIdentifier> identifierList, ParseTreeNode typeConsNameNode) {}        
        @Override
        void visitClassDefnNameNode(List<SourceIdentifier> identifierList, ParseTreeNode classNameNode) {}
        @Override
        void visitClassNameNode(List<SourceIdentifier> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitInstanceMethodNameNode(List<SourceIdentifier> identifierList, ParseTreeNode instanceMethodNameNode, ParseTreeNode qualifiedNameNode) {}
        @Override
        void visitDataConsNameNode(List<SourceIdentifier> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitTypeConsNameNode(List<SourceIdentifier> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitFunctionOrClassMethodDefinitionNameNode(List<SourceIdentifier> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitUnqualifiedFunctionOrClassMethodNameNode(List<SourceIdentifier> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitUnqualifiedClassNameNode(List<SourceIdentifier> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitUnqualifiedDataConsNameNode(List<SourceIdentifier> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitUnqualifiedTypeConsNameNode(List<SourceIdentifier> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitModuleNameNode(List<SourceIdentifier> identifierList, ParseTreeNode moduleNameNode) {}
        @Override
        void visitCALDocCrossReferenceWithoutContextConsNameNode(List<SourceIdentifier> identifierList, ParseTreeNode refNode) {}
        
        /** The identifier list contains SourceIdentifier objects */
        @Override
        Comparator<SourceIdentifier> getIdentifierListComparator() {
            return SourceIdentifier.compareByStartPosition;
        }

        @Override
        void pushLocalVar(ArrayStack<String> boundVariablesStack, ParseTreeNode varNameNode, List<SourceIdentifier> identifierList) {        
            boundVariablesStack.push(varNameNode.getText());      
        }        
    }
    
    /**
     * Finds occurrences of CALDoc "@arg" tags that need to be renamed.
     * 
     * For example, in the module M, if M.x is renamed to M.a, then the let expression (in the same module M):
     * 
     * let
     *    /**
     *     * @arg a the argument.
     *     * /
     *    localFunction a = a + x;
     * in
     *    []
     * 
     * ...would need to be modified to:
     * 
     * let
     *    /**
     *     * @arg a2 the argument.
     *     * /
     *    localFunction a2 = a2 + a;
     * in
     *    []
     * 
     * @author Joseph Wong
     */
    private static final class CALDocArgNameRenamingFinder extends SourceIdentifierFinder<SourceModification, String> {
        /** Category of identifier being renamed */ 
        private final SourceIdentifier.Category renameCategory;
        
        /** Old name of the identifier */
        private final QualifiedName renameOldName;
        
        /** New name of the identifier */
        private final QualifiedName renameNewName;
        
        /** Constructor */
        CALDocArgNameRenamingFinder(ModuleName currentModuleName, QualifiedName oldName, QualifiedName newName, SourceIdentifier.Category category) {
            setCurrentModuleName(currentModuleName);
            this.renameOldName = oldName;
            this.renameNewName = newName;
            this.renameCategory = category;
        }
        
        /**
         * Adds a renaming (original to new name) if this node refers the SC or Class Method being renamed.
         * 
         * Note: Local bindings are not checked because the conflict resolution algorithms ensures
         * no conflicts occur (progressing outwards).
         */
        @Override
        void visitCALDocArgNameNode(List<SourceModification> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode argNameNode) {
            argNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID, CALTreeParserTokenTypes.ORDINAL_FIELD_NAME);
            
            if (argNameNode.getType() == CALTreeParserTokenTypes.ORDINAL_FIELD_NAME) {
                return;
            }
            
            String varName = argNameNode.getText();
            
            if (varName.equals(renameOldName.getUnqualifiedName()) &&
                renameCategory == SourceIdentifier.Category.TOP_LEVEL_FUNCTION_OR_CLASS_METHOD &&
                renameOldName.getModuleName().equals(getCurrentModuleName())) {
                
                identifierList.add(new SourceModification.ReplaceText(varName, renameNewName.getUnqualifiedName(), argNameNode.getSourcePosition()));
            }
        }        
        
        @Override
        void visitFunctionOrClassMethodNameNode(List<SourceModification> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode qualifiedNode) {}
        @Override
        void visitDataConsDefnNameNode(List<SourceModification> identifierList, ParseTreeNode dataConsNameNode) {}
        @Override
        void visitTypeConsDefnNameNode(List<SourceModification> identifierList, ParseTreeNode typeConsNameNode) {}        
        @Override
        void visitClassDefnNameNode(List<SourceModification> identifierList, ParseTreeNode classNameNode) {}
        @Override
        void visitClassNameNode(List<SourceModification> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitInstanceMethodNameNode(List<SourceModification> identifierList, ParseTreeNode instanceMethodNameNode, ParseTreeNode qualifiedNameNode) {}
        @Override
        void visitDataConsNameNode(List<SourceModification> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitTypeConsNameNode(List<SourceModification> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitFunctionOrClassMethodDefinitionNameNode(List<SourceModification> identifierList, ParseTreeNode qualifiedNode) {}
        @Override
        void visitUnqualifiedFunctionOrClassMethodNameNode(List<SourceModification> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitUnqualifiedClassNameNode(List<SourceModification> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitUnqualifiedDataConsNameNode(List<SourceModification> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitUnqualifiedTypeConsNameNode(List<SourceModification> identifierList, ParseTreeNode unqualifiedNameNode, ParseTreeNode moduleNameNode) {}
        @Override
        void visitModuleNameNode(List<SourceModification> identifierList, ParseTreeNode moduleNameNode) {}
        @Override
        void visitCALDocCrossReferenceWithoutContextConsNameNode(List<SourceModification> identifierList, ParseTreeNode refNode) {}
        
        /** The identifier list contains SourceIdentifier objects */
        @Override
        Comparator<SourceModification> getIdentifierListComparator() {
            return SourceModification.compareByPosition;
        }
        
        @Override
        void pushLocalVar(ArrayStack<String> boundVariablesStack, ParseTreeNode varNameNode, List<SourceModification> identifierList) {        
            boundVariablesStack.push(varNameNode.getText());      
        }        
    }
    
    /** Original name of identifier to rename */
    private final QualifiedName renameOldName;
    
    /** New name of identifier to rename*/
    private final QualifiedName renameNewName;
    
    /** Category of identifier renamed */
    private final SourceIdentifier.Category renameCategory;

    /** QualificationMap for use if we are working with a code gem. */
    private final CodeQualificationMap qualificationMap;
    
    /** Flag to indicate that a conflicting name has been found (so that we only report it once) */
    private boolean conflictingNameFound = false;
    
    /** 
     * Constructs a RenamedIdentifierFinder
     * @param oldName The original name of the entity to rename
     * @param newName The name to rename 
     * @param category The category of the entity being renamed
     */
    RenamedIdentifierFinder(QualifiedName oldName, QualifiedName newName, SourceIdentifier.Category category) {
        
        if (oldName == null || newName == null || category == null) {
            throw new NullPointerException();
        }
        this.renameOldName = oldName;
        this.renameNewName = newName;
        this.renameCategory = category;
        this.qualificationMap = null;
    }
    
    /** 
     * Constructs a RenamedIdentifierFinder
     * @param oldName The original name of the entity to rename
     * @param newName The name to rename 
     * @param category The category of the entity being renamed
     * @param qualificationMap The qualification map to use if one exists, or null if it does not. 
     */
    RenamedIdentifierFinder(QualifiedName oldName, QualifiedName newName, SourceIdentifier.Category category, CodeQualificationMap qualificationMap) {
        if (oldName == null || newName == null || category == null) {
            throw new NullPointerException();
        }
        this.renameOldName = oldName;
        this.renameNewName = newName;
        this.renameCategory = category;
        this.qualificationMap = qualificationMap;
        
    }

    /**
     * Collect renamings within a Let declaration. 
     * A form of the conflict resolution mechanism described above is implemented in this method. 
     * 
     */
    @Override
    void findIdentifiersInLet(List<SourceModification> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode parseTree) {

        // : Renaming S to R, resolving any conflicts that R may have with local vars
        ModuleName mS = renameOldName.getModuleName();
        String S = renameOldName.getUnqualifiedName(), R = renameNewName.getUnqualifiedName();
        
        ParseTreeNode defnListNode = parseTree.firstChild();
        defnListNode.verifyType(CALTreeParserTokenTypes.LET_DEFN_LIST);
    
        // Check that S is not on the stack; if it is, then it is a local binding so we don't care about 
        // it and so we have nothing to do in this let.
        if (mS.equals(getCurrentModuleName()) && boundVariablesStack.contains(S)) {
            findIdentifiersInCALDocCommentsForLocalDefnsAndNestedExprs(identifierList, boundVariablesStack, defnListNode);
            return;
        }
        
        // First, collect all the optional CALDoc nodes (for later checking)
        Map<String, ParseTreeNode> funcNamesToOptionalCALDocNodes = new HashMap<String, ParseTreeNode>();
        
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
        
        // Look at local binding declarations, to know the variables defined at this level
        
        int nLocalSCs = 0;
        ParseTreeNode rLocalDeclNameNode = null;
        ParseTreeNode rTypeDecl = null;
        int rStackPos = -1;
        for (final ParseTreeNode defnNode : defnListNode) {

            switch (defnNode.getType()) {
                case (CALTreeParserTokenTypes.LET_DEFN): 
                {
                    ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                    optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                    // we defer the checking of the CALDoc to "Part 1" below, as part of findIdentifiersInLambda
                    
                    ParseTreeNode localSCNameNode = optionalCALDocNode.nextSibling();
                    localSCNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                    String scName = localSCNameNode.getText();
                    
                    // scName is a bound variable for all declarations in the 'let' and for the expression following the 'in'.
                    boundVariablesStack.push(scName);
                    nLocalSCs++;
                    
                    // If this is defining local variable R, renaming of S to R will
                    // clash if S is present. So remember this location, for future use.
                    // Note: There is at most one TYPE_DECLARATION and LET_DEFN per variable
                    if (scName.equals(R)) {
                        rLocalDeclNameNode = localSCNameNode;
                        rStackPos = boundVariablesStack.size() - 1;
                    }
                    break;
                }
            
                case (CALTreeParserTokenTypes.LET_DEFN_TYPE_DECLARATION):
                {
                    // Check potential clash with variable named R again
                    ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                    optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                    // we defer the checking of the CALDoc to "Part 1" below, as part of findIdentifiersInLambda
                    
                    ParseTreeNode typeDeclNode = optionalCALDocNode.nextSibling();
                    typeDeclNode.verifyType(CALTreeParserTokenTypes.TYPE_DECLARATION);
                     
                    ParseTreeNode localSCNameNode = typeDeclNode.firstChild();
                    localSCNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                    String scName = localSCNameNode.getText();
                    if (scName.equals(R)) {
                        rTypeDecl = localSCNameNode;
                    }
                    
                    findIdentifiersInTypeDecl(identifierList, typeDeclNode);
                    break;
                }
                
                case CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL:
                {
                    // A local pattern match declaration can declare one or more locally bound variables
                    // We will loop through the pattern to process each one
                    
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
                                        
                                        final String scName = patternVarNode.getText();
                                        
                                        // scName is a bound variable for all declarations in the 'let' and for the expression following the 'in'.
                                        boundVariablesStack.push(scName);
                                        nLocalSCs++;
                                        
                                        // If this is defining local variable R, renaming of S to R will
                                        // clash if S is present. So remember this location, for future use.
                                        // Note: There is at most one TYPE_DECLARATION and LET_DEFN per variable
                                        if (scName.equals(R)) {
                                            rLocalDeclNameNode = patternVarNode;
                                            rStackPos = boundVariablesStack.size() - 1;
                                        }
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
                                        
                                        final String scName = patternVarNode.getText();
                                        
                                        // scName is a bound variable for all declarations in the 'let' and for the expression following the 'in'.
                                        boundVariablesStack.push(scName);
                                        nLocalSCs++;
                                        
                                        // If this is defining local variable R, renaming of S to R will
                                        // clash if S is present. So remember this location, for future use.
                                        // Note: There is at most one TYPE_DECLARATION and LET_DEFN per variable
                                        if (scName.equals(R)) {
                                            rLocalDeclNameNode = patternVarNode;
                                            rStackPos = boundVariablesStack.size() - 1;
                                        }
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
                                    
                                    final String scName = patternVarNode.getText();
                                    
                                    // scName is a bound variable for all declarations in the 'let' and for the expression following the 'in'.
                                    boundVariablesStack.push(scName);
                                    nLocalSCs++;
                                    
                                    // If this is defining local variable R, renaming of S to R will
                                    // clash if S is present. So remember this location, for future use.
                                    // Note: There is at most one TYPE_DECLARATION and LET_DEFN per variable
                                    if (scName.equals(R)) {
                                        rLocalDeclNameNode = patternVarNode;
                                        rStackPos = boundVariablesStack.size() - 1;
                                    }
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
                                    
                                    final String scName = patternVarNode.getText();
                                    
                                    // scName is a bound variable for all declarations in the 'let' and for the expression following the 'in'.
                                    boundVariablesStack.push(scName);
                                    nLocalSCs++;
                                    
                                    // If this is defining local variable R, renaming of S to R will
                                    // clash if S is present. So remember this location, for future use.
                                    // Note: There is at most one TYPE_DECLARATION and LET_DEFN per variable
                                    if (scName.equals(R)) {
                                        rLocalDeclNameNode = patternVarNode;
                                        rStackPos = boundVariablesStack.size() - 1;
                                    }
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
                    // Unexpected node type
                    defnListNode.unexpectedParseTreeNode();
                    break;
                }
            }
        }
        
        // If S has just been bound, then any references to S will be to the local variable,
        // so there is nothing for us to do here, beside performing finding the renamings for the CALDoc 
        // comments which were previously skipped over.
        if (mS.equals(getCurrentModuleName()) && boundVariablesStack.contains(S)) {
            findIdentifiersInCALDocCommentsForLocalDefnsAndNestedExprs(identifierList, boundVariablesStack, defnListNode);
            return;
        }
        
        // Part 1: Find replacements at lower levels
        
        // Look in body of local variable declarations, and let declaration, and collect S-to-R replacements
        // (and other renamings if conflicts occur inside)
        
        int oldIdentifierCount = identifierList.size();
        for (final ParseTreeNode defnNode : defnListNode) {

            if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN) {
                ParseTreeNode localFunctionNameNode = defnNode.getChild(1);
                localFunctionNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                String scName = localFunctionNameNode.getText();
                
                ParseTreeNode varListNode = localFunctionNameNode.nextSibling();
                
                // get the optional CALDoc node stored in the map.
                // this may be the CALDoc associated with the corresponding function type declaration, or if
                // there is none, the one associated with this function definition itself.
                ParseTreeNode optionalCALDocNodeFromMap = funcNamesToOptionalCALDocNodes.get(scName);
                    
                // here, we not only process the body of the local definition, but also its CALDoc comment,
                // since its @arg tags may be affected by the renaming of the parameters.
                findIdentifiersInLambda(identifierList, boundVariablesStack, varListNode, varListNode.nextSibling(), optionalCALDocNodeFromMap);
                
            } else if (defnNode.getType() == CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL) {
                
                final ParseTreeNode patternMatchPatternNode = defnNode.firstChild();
                final ParseTreeNode patternMatchExprNode = patternMatchPatternNode.nextSibling();
                
                findIdentifiersInLambda(identifierList, boundVariablesStack, null, patternMatchExprNode);
            }
        }
        ParseTreeNode exprNode = defnListNode.nextSibling();
        findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);

        // Part 2: Resolve conflicts at this level
        
        if (!(mS.equals(getCurrentModuleName()) || mS.equals(getModuleForImportedFunctionOrClassMethod(S))) || (rLocalDeclNameNode == null) || (oldIdentifierCount == identifierList.size())) {
            // We do not try to resolve conflicts if either:
            // 1. S cannot appear in unqualified form, and not thus conflicting with local variables
            // 2. the list of identifiers has not changed, thus no occurrences of S
            //    have been found and renamed
            // 3. R is not defined at our level so we have no conflicts to resolve here
            
            boundVariablesStack.popN(nLocalSCs); 
            return;
        }
        
        // Will rename the local variable R by appending a suffix
        
        int pad = 2;
        String R2 = "";
        
        // Repeat the following section until we find a variable name which
        // does not cause a conflict with others at this or inner levels
        boolean stillLooking = true; 
        while (stillLooking) {
            R2 = rLocalDeclNameNode.getText() + pad++;
            // Make sure we don't chose a name conflicting with something already defined
            // in this or outer scope
            while (boundVariablesStack.contains(R2)) {
                R2 = rLocalDeclNameNode.getText() + pad++;
            }
            
            // Now make sure this choice doesn't cause conflicts down the road (ie: renaming R to R2
            // will not conflict R2 with local variables in inner scopes)
            
            // Change to RenameConflict finding mode (R-R2)
            RenameConflictFinder rr2ConflictFinder = 
                new RenameConflictFinder(getCurrentModuleName(), QualifiedName.make(getCurrentModuleName(), R), QualifiedName.make(getCurrentModuleName(), R2), renameCategory);
            
            // Declare new stack because otherwise RCF will think conflicting thoughts
            // Note: Bound variables at this and upper levels have already been checked 
            //       not to contain R2
            ArrayStack<String> newBoundVariablesStack = ArrayStack.make();  
            List<SourceIdentifier> conflictingIdentifiers = new ArrayList<SourceIdentifier>();
            
            for (final ParseTreeNode defnNode : defnListNode) {
                
                if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN) {
                    ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                    optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                    
                    ParseTreeNode localFunctionNameNode = optionalCALDocNode.nextSibling();
                    ParseTreeNode varListNode = localFunctionNameNode.nextSibling();
                    rr2ConflictFinder.findIdentifiersInLambda(conflictingIdentifiers, newBoundVariablesStack, varListNode, varListNode.nextSibling());
                    
                } else if (defnNode.getType() == CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL) {
                    
                    defnNode.verifyType(CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL);

                    final ParseTreeNode patternMatchPatternNode = defnNode.firstChild();
                    final ParseTreeNode patternMatchExprNode = patternMatchPatternNode.nextSibling();

                    rr2ConflictFinder.findIdentifiersInLambda(conflictingIdentifiers, newBoundVariablesStack, null, patternMatchExprNode);
                }
            }
            rr2ConflictFinder.findIdentifiersInExpr(conflictingIdentifiers, newBoundVariablesStack, exprNode);
            getLogger().logMessages(rr2ConflictFinder.getLogger());
            
            // Continue looking for a new variable name until there are no conflicts
            stillLooking = !conflictingIdentifiers.isEmpty();
        }
        
        // Now, R2 is a non-conflicting name, so replace all occurrences of R with R2

        // Rename declaration
        identifierList.add(new SourceModification.ReplaceText(rLocalDeclNameNode.getText(), R2, rLocalDeclNameNode.getSourcePosition()));
        if (rTypeDecl != null) {
            identifierList.add(new SourceModification.ReplaceText(rTypeDecl.getText(), R2, rTypeDecl.getSourcePosition()));
        }
        
        // Rename R to R2 on the bindings stack 
        //   Note: (1) R2 does not exist in local vars already because we have checked
        //         (2) R2 as a local var from this or upper levels is not referenced from below because (1)
        //         (3) renaming R to R2 does not clash below with any local variable, because we checked
        boundVariablesStack.set(rStackPos, R2);
        
        // TODO: See if this can be improved to less hackish
        // Now, on the stack there may be other occurrences of R, which will not allow R-R2 renaming to
        // occur (since we check local bindings). These occurrences will be renamed as we move out, 
        // but for now we will rename them to R2 (this does nothing since we already have R2 on the stack)
        List<Integer> stackSpotsRenamed = new ArrayList<Integer>();
        for (int i = 0, n = boundVariablesStack.size(); i < n; i++) {
            if ((boundVariablesStack.get(i)).equals(R)) {
                stackSpotsRenamed.add(Integer.valueOf(i));
                boundVariablesStack.set(i, R2);
            }
        }
                
        // Rename R-R2 in lower levels
        // (Note: this should not go through any Part 2s, since there are no conflicts)
        RenamedIdentifierFinder rr2RenamingFinder = new RenamedIdentifierFinder(QualifiedName.make(getCurrentModuleName(), R), QualifiedName.make(getCurrentModuleName(), R2), renameCategory);
        rr2RenamingFinder.setCurrentModuleName(getCurrentModuleName());
        for (final ParseTreeNode defnNode : defnListNode) {
            if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN) {
                ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                
                ParseTreeNode localFunctionNameNode = optionalCALDocNode.nextSibling();
                ParseTreeNode varListNode = localFunctionNameNode.nextSibling();
                rr2RenamingFinder.findIdentifiersInLambda(identifierList, boundVariablesStack, varListNode, varListNode.nextSibling());
                
            } else if (defnNode.getType() == CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL) {
                
                defnNode.verifyType(CALTreeParserTokenTypes.LET_PATTERN_MATCH_DECL);

                final ParseTreeNode patternMatchPatternNode = defnNode.firstChild();
                final ParseTreeNode patternMatchExprNode = patternMatchPatternNode.nextSibling();

                rr2RenamingFinder.findIdentifiersInLambda(identifierList, boundVariablesStack, null, patternMatchExprNode);
            }
        }
        rr2RenamingFinder.findIdentifiersInExpr(identifierList, boundVariablesStack, exprNode);
        getLogger().logMessages(rr2RenamingFinder.getLogger());
        
        // Put back Rs on the stack
        for (int i = 0, n = stackSpotsRenamed.size(); i < n; i++) {
            boundVariablesStack.set(i, R);
        }
        boundVariablesStack.popN(nLocalSCs);
    }

    /**
     * Collect renamings within a CALDoc comment associated with a local definition. 
     */
    private void findIdentifiersInCALDocCommentAssociatedWithLocalDefn(List<SourceModification> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode defnNode) {
        ParseTreeNode optionalCALDocNode = defnNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        
        ParseTreeNode localFunctionNameNode = optionalCALDocNode.nextSibling();
        ParseTreeNode varListNode = localFunctionNameNode.nextSibling();
        
        int nVars = 0;
        if (varListNode != null) {
            for (final ParseTreeNode patternVarNode : varListNode) {
    
                switch (patternVarNode.getType()) {
    
                    case CALTreeParserTokenTypes.VAR_ID:
                    case CALTreeParserTokenTypes.LAZY_PARAM:
                    case CALTreeParserTokenTypes.STRICT_PARAM:                
                    {
                        String varName = patternVarNode.getText();
                        ++nVars;
                        
                        // varName is now a bound variable for the body of the
                        // lambda
                        boundVariablesStack.push(varName);
                        break;
                    }
                    
                    case CALTreeParserTokenTypes.UNDERSCORE:
                        break;
                        
                    default :
                        // Unexpected type
                        boundVariablesStack.popN(nVars);
                        patternVarNode.unexpectedParseTreeNode();                        
                        return;
                }
            }
        }
        
        findIdentifiersInCALDocComment(identifierList, boundVariablesStack, optionalCALDocNode);
        boundVariablesStack.popN(nVars);
    }

    /**
     * Collect renamings within the CALDoc comments associated with a local
     * function definitions and type declarations, as well as any CALDoc
     * comments contained in the defining expressions of these local functions.
     */
    void findIdentifiersInCALDocCommentsForLocalDefnsAndNestedExprs(List<SourceModification> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode defnListNode) {
        
        Map<String, ParseTreeNode> funcNamesToDefnNodes = new HashMap<String, ParseTreeNode>();
        
        // First pass: gather the function definition nodes
        for (final ParseTreeNode defnNode : defnListNode) {
            
            if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN) {
                ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                
                ParseTreeNode localSCNameNode = optionalCALDocNode.nextSibling();
                localSCNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                String scName = localSCNameNode.getText();
                
                funcNamesToDefnNodes.put(scName, defnNode);
            }
        }
        
        // Second pass: perform the checks on CALDocs and also check into the defining expressions
        for (final ParseTreeNode defnNode : defnListNode) {
        
            defnNode.verifyType(CALTreeParserTokenTypes.LET_DEFN, CALTreeParserTokenTypes.LET_DEFN_TYPE_DECLARATION);
            
            if (defnNode.getType() == CALTreeParserTokenTypes.LET_DEFN) {
                // check the CALDoc
                findIdentifiersInCALDocCommentAssociatedWithLocalDefn(identifierList, boundVariablesStack, defnNode);

                // check the bound expression for nested let blocks with CALDoc comments
                ParseTreeNode inExprNode = defnNode.getChild(3);
                findIdentifiersInExpr(identifierList, boundVariablesStack, inExprNode);
                
            } else { // must be a LET_DEFN_TYPE_DECLARATION
                
                ParseTreeNode optionalCALDocNode = defnNode.firstChild();
                optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
                
                ParseTreeNode typeDeclNode = optionalCALDocNode.nextSibling();
                typeDeclNode.verifyType(CALTreeParserTokenTypes.TYPE_DECLARATION);
                 
                ParseTreeNode localSCNameNode = typeDeclNode.firstChild();
                localSCNameNode.verifyType(CALTreeParserTokenTypes.VAR_ID);
                String scName = localSCNameNode.getText();
                
                ParseTreeNode funcDefnNode = funcNamesToDefnNodes.get(scName);
                
                if (funcDefnNode != null) {
                    // check the CALDoc with the function's definition node
                    findIdentifiersInCALDocCommentAssociatedWithLocalDefn(identifierList, boundVariablesStack, funcDefnNode);
                } else {
                    getLogger().logMessage(new CompilerMessage(defnNode, new MessageKind.Error.DefinitionMissing(scName)));
                }
            }
        }
    }

    /**
     * Collect renamings within an SC declaration. There can be no conflicts here.
     */
    @Override
    void findIdentifiersInFunctionDeclaration(List<SourceModification> identifierList, ParseTreeNode scTopLevelTypeDeclarationNode) {

        scTopLevelTypeDeclarationNode.verifyType(CALTreeParserTokenTypes.TOP_LEVEL_TYPE_DECLARATION);
                  
        ParseTreeNode optionalCALDocNode = scTopLevelTypeDeclarationNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        findIdentifiersInCALDocComment(identifierList, optionalCALDocNode);
        
        ParseTreeNode scTypeDeclarationNode = optionalCALDocNode.nextSibling();
        ParseTreeNode scNameNode = scTypeDeclarationNode.firstChild();
        visitFunctionOrClassMethodDefinitionNameNode(identifierList, scNameNode);
        
        ParseTreeNode typeSignatureNode = scNameNode.nextSibling();
        findIdentifiersInTypeSignature(identifierList, typeSignatureNode);
    }
    
    /**
     * Collect renamings within a SC definition. 
     * A form of the conflict resolution mechanism described above is implemented in this method.    
     */
    @Override
    void findIdentifiersInFunctionDefinition(List<SourceModification> identifierList, ParseTreeNode scNode) {
    
        // : Renaming S to R, in SC definition
        ModuleName mS = renameOldName.getModuleName();
        String S = renameOldName.getUnqualifiedName(), R = renameNewName.getUnqualifiedName();
        
        scNode.verifyType(CALTreeParserTokenTypes.TOP_LEVEL_FUNCTION_DEFN);
        
        ParseTreeNode optionalCALDocNode = scNode.firstChild();
        optionalCALDocNode.verifyType(CALTreeParserTokenTypes.OPTIONAL_CALDOC_COMMENT);
        findIdentifiersInCALDocComment(identifierList, optionalCALDocNode);
        
        ParseTreeNode accessModifierNode = optionalCALDocNode.nextSibling();
        
        ParseTreeNode scNameNode = accessModifierNode.nextSibling();
        visitFunctionOrClassMethodDefinitionNameNode(identifierList, scNameNode);
        
        // Find local argument bindings
        
        ParseTreeNode conflictingArgNode = null;
        int conflictingArgPos = -1;
        ArrayStack<String> namedArgumentsStack = ArrayStack.make();
        ParseTreeNode paramListNode = accessModifierNode.nextSibling().nextSibling();
        paramListNode.verifyType(CALTreeParserTokenTypes.FUNCTION_PARAM_LIST);
        for (final ParseTreeNode varNode : paramListNode) {

            varNode.verifyType(CALTreeParserTokenTypes.LAZY_PARAM, CALTreeParserTokenTypes.STRICT_PARAM);
            String varName = varNode.getText();
            namedArgumentsStack.push(varName);
            
            // If an argument has the name R, it will conflict with the rename if the SC definition
            // contains S; thus, keep track of this argument.
            if (varName.equals(R)) {
                conflictingArgNode = varNode;
                conflictingArgPos = namedArgumentsStack.size() - 1;
            }
        }
        
        // S has just been defined as a sc argument, so will ignore any renamings
        if (mS.equals(getCurrentModuleName()) && namedArgumentsStack.contains(S)) {
            return;
        }
        
        // Part 1 : Collect renamings (S-R, and local conflict resolutions) from the SC declaration
         
        int oldIdentifierCount = identifierList.size();
        findIdentifiersInExpr(identifierList, namedArgumentsStack, paramListNode.nextSibling());
        
        // Part 2 : Resolve conflicts between R and local arguments
        
        if (!(mS.equals(getCurrentModuleName()) || mS.equals(getModuleForImportedFunctionOrClassMethod(S))) || (conflictingArgNode == null) || (oldIdentifierCount == identifierList.size())) {
            // We do not try to resolve conflicts if either:
            // 1. S cannot appear in unqualified form, and not conflicting with local variables
            // 2. the list of identifiers has not changed, thus no occurrences of S
            //    have been found and renamed
            // 3. R is not defined at our level so we have no conflicts to resolve here
            return;
        }
        
        // Will repeat the following section until we find a variable name which
        // does not cause a conflict with others at this or inner levels
        boolean keepSearching = true;
        int pad = 2;
        String R2 = "";
        while (keepSearching) {
            R2 = conflictingArgNode.getText() + pad++;
            
            // Make sure we don't chose a name conflicting with an argument defined
            // in this or outer scope
            while (namedArgumentsStack.contains(R2)) {
                R2 = conflictingArgNode.getText() + pad++;
            }
            
            // Now make sure this choice doesn't cause conflicts down the road (ie: renaming R to R2
            // will not conflict R2 with local variables in inner scopes)
            
            // Change mode to RenameConflictFinder (renaming R to R2)
            RenameConflictFinder rr2ConflictFinder = new RenameConflictFinder(getCurrentModuleName(), QualifiedName.make(getCurrentModuleName(), R), QualifiedName.make(getCurrentModuleName(), R2), renameCategory);
            ArrayStack<String> newBoundVariablesStack = ArrayStack.make();
            List<SourceIdentifier> conflictingIdentifiers = new ArrayList<SourceIdentifier>();
            rr2ConflictFinder.findIdentifiersInExpr(conflictingIdentifiers, newBoundVariablesStack, paramListNode.nextSibling());
            getLogger().logMessages(rr2ConflictFinder.getLogger());
            
            // Continue looking for a new variable name until there are no conflicts
            keepSearching = !conflictingIdentifiers.isEmpty();
        } 
        
        // Now, R2 is a non-conflicting name, so replace all occurrences of R with R2

        // Mark argument definition to be renamed (R-R2)
        identifierList.add(new SourceModification.ReplaceText(conflictingArgNode.getText(), R2 ,conflictingArgNode.getSourcePosition())); 
        namedArgumentsStack.set(conflictingArgPos, R2);
                
        // Change mode to RF, R-R2
        // (Note: this should not go through any Part 2s, since there are no conflicts)
        RenamedIdentifierFinder rr2RenamingFinder = new RenamedIdentifierFinder(QualifiedName.make(getCurrentModuleName(), R), QualifiedName.make(getCurrentModuleName(), R2), renameCategory);
        rr2RenamingFinder.setCurrentModuleName(getCurrentModuleName());
        rr2RenamingFinder.findIdentifiersInExpr(identifierList, namedArgumentsStack, paramListNode.nextSibling());
        getLogger().logMessages(rr2RenamingFinder.getLogger());
    }

    /**
     * Collect renamings within a Lambda declaration. 
     * A form of the conflict resolution mechanism described above is implemented in this method.
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param patternVarListNode
     * @param boundExprNode
     * @param optionalCALDocNodeForLetDefn
     */
    @Override
    void findIdentifiersInLambda(List<SourceModification> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode patternVarListNode, ParseTreeNode boundExprNode, ParseTreeNode optionalCALDocNodeForLetDefn) {

        // : Renaming S to R, resolving any conflicts that R may have with local vars
        ModuleName mS = renameOldName.getModuleName();
        String S = renameOldName.getUnqualifiedName(), R = renameNewName.getUnqualifiedName();
        
        ParseTreeNode conflictingVarNode = null;
        int conflictingVarPos = -1;
        int nVars = 0;
        if (patternVarListNode != null) {
            for (final ParseTreeNode patternVarNode : patternVarListNode) {
                
                switch (patternVarNode.getType()) {
                    
                    case CALTreeParserTokenTypes.VAR_ID:
                    case CALTreeParserTokenTypes.LAZY_PARAM:
                    case CALTreeParserTokenTypes.STRICT_PARAM:                
                    {
                        String varName = patternVarNode.getText();
                        ++nVars;
                        
                        // varName is now a bound variable for the body of the
                        // lambda
                        boundVariablesStack.push(varName);
                        
                        if (varName.equals(R)) {
                            // This variable is a potential conflict if we find any S in the body,
                            // so keep it in case we need to rename it
                            conflictingVarNode = patternVarNode;
                            conflictingVarPos = boundVariablesStack.size() - 1;
                        }
                        
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
        
        // If S was already on the stack, or has just been defined as a lambda argument, will ignore any renamings
        if (mS.equals(getCurrentModuleName()) && boundVariablesStack.contains(S)) {
            // check the CALDoc
            if (optionalCALDocNodeForLetDefn != null) {
                findIdentifiersInCALDocComment(identifierList, boundVariablesStack, optionalCALDocNodeForLetDefn);
            }
            // and also check the bound expression for nested let blocks with CALDoc comments
            findIdentifiersInExpr(identifierList, boundVariablesStack, boundExprNode);
            boundVariablesStack.popN(nVars);
            return;
        }

        // Part 1: Find replacements at lower levels
        
        int oldIdentifierListSize = identifierList.size();
        findIdentifiersInExpr(identifierList, boundVariablesStack, boundExprNode);
        
        // Part 2: Resolve conflicts at this level
        
        if (!(mS.equals(getCurrentModuleName()) || mS.equals(getModuleForImportedFunctionOrClassMethod(S))) || (conflictingVarNode == null) || (oldIdentifierListSize == identifierList.size())) {
            // We do not try to resolve conflicts if either:
            // 1. S cannot appear in unqualified form and not conflicting with local variables
            // 2. the list of identifiers has not changed, thus no occurrences of S
            //    have been found and renamed
            // 3. R is not defined at our level so we have no conflicts to resolve here
            
            if (optionalCALDocNodeForLetDefn != null) {
                // just check the CALDoc, since the bound expression has already been traversed
                findIdentifiersInCALDocComment(identifierList, boundVariablesStack, optionalCALDocNodeForLetDefn);
            }
            boundVariablesStack.popN(nVars); 
            return;
        }
        
        // Will rename the local variable R by appending a suffix
        
        int pad = 2;
        String R2 = "";
        
        // Will repeat the following section until we find a variable name which
        // does not cause a conflict with others at this or inner levels
        boolean stillLooking = true; 
        while (stillLooking) {
            R2 = conflictingVarNode.getText() + pad++;
            // Make sure we don't chose a name conflicting with something already defined
            // in this or outer scope
            while (boundVariablesStack.contains(R2)) {
                R2 = conflictingVarNode.getText() + pad++;
            }
            
            // Now make sure this choice doesn't cause conflicts down the road (ie: renaming R to R2
            // will not conflict R2 with local variables in inner scopes)
            
            // Change to RenameConflict finding mode (R-R2)
            RenameConflictFinder rr2ConflictFinder = new RenameConflictFinder(getCurrentModuleName(), QualifiedName.make(getCurrentModuleName(), R), QualifiedName.make(getCurrentModuleName(), R2), renameCategory);
            ArrayStack<String> newBoundVariablesStack = ArrayStack.make();  
            List<SourceIdentifier> conflictingIdentifiers = new ArrayList<SourceIdentifier>();
            rr2ConflictFinder.findIdentifiersInExpr(conflictingIdentifiers, newBoundVariablesStack, boundExprNode);
            getLogger().logMessages(rr2ConflictFinder.getLogger());
            
            // Continue looking for a new variable name until there are no conflicts
            stillLooking = !conflictingIdentifiers.isEmpty();
        }
        
        // Now, R2 is a non-conflicting name, so replace all occurrences of R with R2

        identifierList.add(new SourceModification.ReplaceText(conflictingVarNode.getText(), R2, conflictingVarNode.getSourcePosition()));
        boundVariablesStack.set(conflictingVarPos, R2);
        
        // TODO: See if this can be improved to less hackish
        // Now, on the stack there may be other occurrences of R, which will not allow R-R2 renaming to
        // occur (since we check local bindings). These occurrences will be renamed as we move out, 
        // but for now we will rename them to R2 (this does nothing since we already have R2 on the stack)
        List<Integer> stackSpotsRenamed = new ArrayList<Integer>();
        for (int i = 0, n = boundVariablesStack.size(); i < n; i++) {
            if ((boundVariablesStack.get(i)).equals(R)) {
                stackSpotsRenamed.add(Integer.valueOf(i));
                boundVariablesStack.set(i, R2);
            }
        }
                
        // Change mode to FindRenamings (R-R2)
        // (Note: this should not go through any Part 2s, since there are no conflicts) 
        RenamedIdentifierFinder rr2RenamingFinder = new RenamedIdentifierFinder(QualifiedName.make(getCurrentModuleName(), R), QualifiedName.make(getCurrentModuleName(), R2), renameCategory);
        rr2RenamingFinder.setCurrentModuleName(getCurrentModuleName());
        rr2RenamingFinder.findIdentifiersInExpr(identifierList, boundVariablesStack, boundExprNode);
        getLogger().logMessages(rr2RenamingFinder.getLogger());
        
        // Make the appropriate R-R2 renamings in the CALDoc for the let definition which is being
        // checked here (pretending to be a lambda).
        if (optionalCALDocNodeForLetDefn != null) {
            CALDocArgNameRenamingFinder caldocRR2RenamingFinder = new CALDocArgNameRenamingFinder(getCurrentModuleName(), QualifiedName.make(getCurrentModuleName(), R), QualifiedName.make(getCurrentModuleName(), R2), renameCategory);
            caldocRR2RenamingFinder.findIdentifiersInCALDocComment(identifierList, boundVariablesStack, optionalCALDocNodeForLetDefn);
            getLogger().logMessages(caldocRR2RenamingFinder.getLogger());
        }
        
        // Put back Rs on the stack
        for (int i = 0, n = stackSpotsRenamed.size(); i < n; i++) {
            boundVariablesStack.set(i, R);
        }
        
        boundVariablesStack.popN(nVars);
        return;
    }
    
    /**
     * Finds identifiers in a case expression containing a field binding pattern.
     * A form of the conflict resolution mechanism described above is implemented in this method,
     * because local variables may be bound to field bindings
     * 
     * @param identifierList
     * @param boundVariablesStack
     * @param fieldBindingVarAssignmentListNode
     * @param boundExprNode
     * @param basePatternVarNameNode if non-null, the parse tree node for the named variable forming the base pattern for the field binding.   
     */
    @Override
    void findIdentifiersInFieldBindingCase(List<SourceModification> identifierList, ArrayStack<String> boundVariablesStack, ParseTreeNode fieldBindingVarAssignmentListNode, ParseTreeNode boundExprNode, ParseTreeNode basePatternVarNameNode) {
        // : Renaming S to R, resolving any conflicts that R may have with local vars
        ModuleName mS = renameOldName.getModuleName();
 
