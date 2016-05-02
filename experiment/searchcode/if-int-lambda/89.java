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
 * SourceDependencyFinder.java
 * Creation date: (Mar 24, 2005)
 * By: Jawright
 */
package org.openquark.cal.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openquark.cal.compiler.SourceModel.Expr;
import org.openquark.cal.compiler.SourceModel.FieldPattern;
import org.openquark.cal.compiler.SourceModel.FunctionDefn;
import org.openquark.cal.compiler.SourceModel.FunctionTypeDeclaration;
import org.openquark.cal.compiler.SourceModel.Import;
import org.openquark.cal.compiler.SourceModel.InstanceDefn;
import org.openquark.cal.compiler.SourceModel.LocalDefn;
import org.openquark.cal.compiler.SourceModel.ModuleDefn;
import org.openquark.cal.compiler.SourceModel.Name;
import org.openquark.cal.compiler.SourceModel.Parameter;
import org.openquark.cal.compiler.SourceModel.Pattern;
import org.openquark.cal.compiler.SourceModel.SourceElement;
import org.openquark.cal.compiler.SourceModel.TopLevelSourceElement;
import org.openquark.cal.compiler.SourceModel.TypeClassDefn;
import org.openquark.cal.compiler.SourceModel.TypeExprDefn;
import org.openquark.cal.compiler.SourceModel.Expr.Application;
import org.openquark.cal.compiler.SourceModel.Expr.BinaryOp;
import org.openquark.cal.compiler.SourceModel.Expr.DataCons;
import org.openquark.cal.compiler.SourceModel.Expr.Lambda;
import org.openquark.cal.compiler.SourceModel.Expr.Let;
import org.openquark.cal.compiler.SourceModel.Expr.Parenthesized;
import org.openquark.cal.compiler.SourceModel.Expr.SelectDataConsField;
import org.openquark.cal.compiler.SourceModel.Expr.UnaryOp;
import org.openquark.cal.compiler.SourceModel.Expr.Unit;
import org.openquark.cal.compiler.SourceModel.Expr.Var;
import org.openquark.cal.compiler.SourceModel.Expr.BinaryOp.Apply;
import org.openquark.cal.compiler.SourceModel.Expr.BinaryOp.BackquotedOperator;
import org.openquark.cal.compiler.SourceModel.Expr.BinaryOp.Compose;
import org.openquark.cal.compiler.SourceModel.Expr.Case.Alt.UnpackDataCons;
import org.openquark.cal.compiler.SourceModel.Expr.Case.Alt.UnpackListCons;
import org.openquark.cal.compiler.SourceModel.Expr.Case.Alt.UnpackListNil;
import org.openquark.cal.compiler.SourceModel.Expr.Case.Alt.UnpackUnit;
import org.openquark.cal.compiler.SourceModel.Expr.UnaryOp.Negate;
import org.openquark.cal.compiler.SourceModel.FunctionDefn.Algebraic;
import org.openquark.cal.compiler.SourceModel.FunctionDefn.Foreign;
import org.openquark.cal.compiler.SourceModel.FunctionDefn.Primitive;
import org.openquark.cal.compiler.SourceModel.Import.UsingItem;
import org.openquark.cal.compiler.SourceModel.InstanceDefn.InstanceMethod;
import org.openquark.cal.compiler.SourceModel.InstanceDefn.InstanceTypeCons.TypeCons;
import org.openquark.cal.compiler.SourceModel.LocalDefn.Function.Definition;
import org.openquark.cal.compiler.SourceModel.TypeClassDefn.ClassMethodDefn;
import org.openquark.cal.compiler.SourceModel.TypeConstructorDefn.AlgebraicType;
import org.openquark.cal.compiler.SourceModel.TypeConstructorDefn.ForeignType;
import org.openquark.cal.compiler.SourceModel.TypeConstructorDefn.AlgebraicType.DataConsDefn;
import org.openquark.cal.filter.AcceptAllQualifiedNamesFilter;
import org.openquark.cal.filter.QualifiedNameFilter;
import org.openquark.cal.module.Cal.Core.CAL_Prelude;
import org.openquark.cal.util.ArrayStack;
import org.openquark.util.Pair;


/**
 * Provides methods for finding various metrics associated with CAL modules.
 * 
 * Currently implemented metrics:
 * 
 *    - Reference frequency: Each gem is given an associated reference frequency count, which
 *                           represents the number of times that the gem is referred to in the
 *                           body of other functions.  The raw data collected are the number of
 *                           times each function refers to its dependees (referencees?).  These
 *                           raw data data are aggregated by the ModuleSourceMetrics and 
 *                           WorkspaceSourceMetricsManager classes into the workspace-level
 *                           reference frequency counts that are provided to clients.
 *    
 *    - Compositional frequency: Each pair of gems (A,B) is given an associated frequency count which
 *                               represents the number of times the output of B is passed directly as
 *                               an argument of A.
 *      
 *    - Lint warnings: The source is scanned for potential optimization issues and a list of
 *                     warning objects is returned to the client.
 * 
 *    - Reference and application locations: The source is scanned for references to or applications of
 *                                           a specified QualifiedName, and a list of SourcePositions is
 *                                           returned.   
 * 
 * Creation date: (Mar 24, 2005)
 * @author Jawright
 */
abstract class SourceMetricFinder {

    /** This class should never be instantiated */
    private SourceMetricFinder() {
    }

    /**
     * Provides functionality that is common to most of the SourceModelTraversers in 
     * the SourceMetricFinder class. 
     * 
     * Creation date: (Jun 27, 2005)
     * @author Jawright
     */
    private static abstract class MetricVisitor extends BindingTrackingSourceModelTraverser<Void> {

        /** The name of the module being processed */
        private final ModuleName currentModule;
        
        /** Information about the module being processed */
        private final ModuleTypeInfo moduleTypeInfo;
        
        private MetricVisitor(ModuleTypeInfo moduleTypeInfo) {
            
            if (moduleTypeInfo == null) {
                throw new NullPointerException("MetricVisitor needs a non-null moduleTypeInfo to operate upon");
            }

            this.moduleTypeInfo = moduleTypeInfo;
            this.currentModule = moduleTypeInfo.getModuleName();
        }
        
        /**
         * Resolves the given module name in the context of the module being processed based on module name resolution rules
         * and returns the name of the module the given name resolves to. If the original name
         * is not unambiguously resolvable, then the original name is returned.
         * 
         * @param moduleName the module name to be resolved. Cannot be null.
         * @return the name of the module the original name resolves to. If the original name
         *         is not unambiguously resolvable, then the original name is returned.
         */
        private ModuleName resolveModuleName(Name.Module moduleName) {
            ModuleNameResolver.ResolutionResult resolution = moduleTypeInfo.getModuleNameResolver().resolve(SourceModel.Name.Module.toModuleName(moduleName));
            return resolution.getResolvedModuleName();
        }
        
        /**
         * Check whether a variable reference counts as a reference to a dependee
         * @param varName The variable reference to check
         * @return true if varName refers to a dependee, false otherwise
         */
        private boolean isDependeeReference(Name.Function varName) {
            
            // References to the current function don't count (ie, a function
            // cannot be a dependee of itself).  All unqualified references to
            // the current function name are disqualified, since they either refer
            // to the current function or to a lexically-scoped variable, neither of
            // which counts as a dependee.
            String currentFunctionName = getCurrentFunction();
            if(currentFunctionName != null &&
               currentFunctionName.equals(varName.getUnqualifiedName()) &&
               (varName.getModuleName() == null || resolveModuleName(varName.getModuleName()).equals(currentModule))) {
                return false;
            }
            
            // All other explicitly-qualified references always count as dependees
            // (because they are guaranteed to be toplevel functional agents)
            if(varName.getModuleName() != null) {
                return true;
            }            
            
            // Unqualified references that match lexcially-scoped variables are not dependees
            if(isBound(varName.getUnqualifiedName())) {
                return false;
            }
            
            // Unqualified references that do not match lexically-scoped
            // variables count as dependees.
            return true;
        }
        
        /**
         * Looks up the named functional agent in the relevant ModuleTypeInfo object and returns it.
         * @param agentName The name (qualified or not) of the functional agent to look up.
         * @return The FunctionalAgent for the functional agent specified by entityName
         */
        private FunctionalAgent getFunctionalAgent(Name.Qualifiable agentName) {
            
            if(!(agentName instanceof Name.DataCons || agentName instanceof Name.Function)) {
                return null;
            }
            
            // Explicitly qualified, non-imported agent
            if (agentName.getModuleName() != null && resolveModuleName(agentName.getModuleName()).equals(currentModule)) {
                return moduleTypeInfo.getFunctionalAgent(agentName.getUnqualifiedName());
            }
            
            // Explicitly qualified, imported agent
            else if (agentName.getModuleName() != null) {
                ModuleTypeInfo externalModuleInfo = moduleTypeInfo.getDependeeModuleTypeInfo(resolveModuleName(agentName.getModuleName()));
                if(externalModuleInfo == null) {
                    //  Some sort of compilation problem
                    return null;
                }
                
                return externalModuleInfo.getFunctionalAgent(agentName.getUnqualifiedName());
            }
            
            // Unqualified, non-imported agent
            else if (agentName.getModuleName() == null && moduleTypeInfo.getFunctionalAgent(agentName.getUnqualifiedName()) != null) {
                return moduleTypeInfo.getFunctionalAgent(agentName.getUnqualifiedName());
            }
            
            // Unqualified, imported function
            else {
                ModuleName usingModuleName = moduleTypeInfo.getModuleOfUsingFunctionOrClassMethod(agentName.getUnqualifiedName());
                if (usingModuleName == null) {
                    usingModuleName = moduleTypeInfo.getModuleOfUsingDataConstructor(agentName.getUnqualifiedName());
                }
                
                ModuleTypeInfo externalModuleInfo = moduleTypeInfo.getDependeeModuleTypeInfo(usingModuleName);
                if(externalModuleInfo == null) {
                    // Some sort of compilation problem
                    return null;
                }
                
                return externalModuleInfo.getFunctionalAgent(agentName.getUnqualifiedName());
            }
        }
                
        /** If the provided expression is a Var or DataCons expression, returns the name of the referenced
         * function or datacons.
         * @param expr A Var or DataCons expression
         * @return unqualified name of the function or datacons referred to by the expression
         */
        private Name.Qualifiable getFunctionalAgentName(Expr expr) {
            if(expr instanceof Var) {
                return ((Var)expr).getVarName(); 
            } else if(expr instanceof DataCons) {
                return ((DataCons)expr).getDataConsName();
            } else {
                return null;
            }
        }
    }
    
    /**
     * This is an implementation of SourceModelTraverser that gathers the raw data for the reference-frequency metric.
     * Raw data are collected in a single traversal and stored into the associated EnvEntities.
     * 
     * Creation date: (Mar 24, 2005)
     * @author Jawright
     */
    private static final class ReferenceFrequencyFinder extends MetricVisitor {
        
        /**
         * Map from (dependee, dependent) pair to number of times dependent references dependee
         */
        private final Map<Pair<QualifiedName, QualifiedName>, Integer> dependeeMap = new HashMap<Pair<QualifiedName, QualifiedName>, Integer>();
        
        /**
         * Set of QualifiedNames from imported modules that occur in this module.  
         */
        private final Set<QualifiedName> importedNameOccurrences = new HashSet<QualifiedName>();
        
        /** Filter for deciding whether a function is to be visited */
        private final QualifiedNameFilter functionFilter;
        
        /** When true, functions that are skipped (because they match the excludeFunctionRegexp)
         * will have their names dumped to the system console.  When false, such functions will
         * be skipped silently.
         */
        private final boolean traceSkippedFunctions;
        
        /**
         * Construct a ReferenceFrequencyFinder
         * @param moduleTypeInfo ModuleTypeInfo of module to be scanned
         * @param functionFilter Filter for deciding which functions will be processed. Cannot be null.
         * @param traceSkippedFunctions When true, each skipped function will have its name dumped to the console.
         *                               When false, skipped functions will be skipped silently 
         */
        private ReferenceFrequencyFinder(ModuleTypeInfo moduleTypeInfo, QualifiedNameFilter functionFilter, boolean traceSkippedFunctions) {
            super(moduleTypeInfo);
            if (functionFilter == null) {
                throw new NullPointerException();
            }
            this.functionFilter = functionFilter;
            this.traceSkippedFunctions = traceSkippedFunctions;
        }
        
        /** {@inheritDoc} */
        // Don't visit functions matching the exclusion regexp
        @Override
        public Void visit_FunctionDefn_Algebraic(Algebraic algebraic, Object arg) {
            
            if(!functionFilter.acceptQualifiedName(QualifiedName.make(super.currentModule, algebraic.getName()))) {
                if(traceSkippedFunctions) {
                    System.out.println("Skipping test function " + super.currentModule + "." + algebraic.getName());
                }
                return null;
            }
            
            return super.visit_FunctionDefn_Algebraic(algebraic, arg);
        }
        
        /** {@inheritDoc} */
        // A local pattern match declaration with a data cons pattern counts as a reference to the data constructor.
        @Override
        public Void visit_LocalDefn_PatternMatch_UnpackDataCons(LocalDefn.PatternMatch.UnpackDataCons unpackDataCons, Object arg) {
            Name.DataCons dataName = unpackDataCons.getDataConsName();
            recordDependeeReference(dataName);
            
            return super.visit_LocalDefn_PatternMatch_UnpackDataCons(unpackDataCons, arg);
        }

        /** {@inheritDoc} */
        // A local pattern match declaration with a Cons (:) pattern counts as a reference to Prelude.Cons.
        @Override
        public Void visit_LocalDefn_PatternMatch_UnpackListCons(LocalDefn.PatternMatch.UnpackListCons unpackListCons, Object arg) {
            recordDependeeReference(Name.DataCons.make(CAL_Prelude.DataConstructors.Cons));
            return super.visit_LocalDefn_PatternMatch_UnpackListCons(unpackListCons, arg);
        }

        /** {@inheritDoc} */
        // Using a Cons (:) pattern in a case alternative counts as a reference to Prelude.Cons.
        @Override
        public Void visit_Expr_Case_Alt_UnpackListCons(UnpackListCons cons, Object arg) {
            recordDependeeReference(Name.DataCons.make(CAL_Prelude.DataConstructors.Cons));
            return super.visit_Expr_Case_Alt_UnpackListCons(cons, arg);
        }

        /** {@inheritDoc} */
        // Using a data constructor in a case alternative pattern counts as a reference to the data constructor.
        @Override
        public Void visit_Expr_Case_Alt_UnpackDataCons(UnpackDataCons cons, Object arg) {

            for (int i = 0, nDataConsNames = cons.getNDataConsNames(); i < nDataConsNames; i++) {
                Name.DataCons dataConsName = cons.getNthDataConsName(i);
                recordDependeeReference(dataConsName);
            }
            
            return super.visit_Expr_Case_Alt_UnpackDataCons(cons, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_Case_Alt_UnpackListNil(UnpackListNil nil, Object arg) {
            recordDependeeReference(Name.DataCons.make(CAL_Prelude.DataConstructors.Nil));
            return super.visit_Expr_Case_Alt_UnpackListNil(nil, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_Case_Alt_UnpackUnit(UnpackUnit unit, Object arg) {
            recordDependeeReference(Name.DataCons.make(CAL_Prelude.DataConstructors.Unit));
            return super.visit_Expr_Case_Alt_UnpackUnit(unit, arg);
        }

        /** {@inheritDoc} */
        // Field selection from a dataCons-valued expression counts as a reference to the data constructor.
        @Override
        public Void visit_Expr_SelectDataConsField(SelectDataConsField field, Object arg) {
            Name.DataCons dataName = field.getDataConsName();
            recordDependeeReference(dataName);
            
            return super.visit_Expr_SelectDataConsField(field, arg);
        }

        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_DataCons(DataCons cons, Object arg) {
            recordDependeeReference(cons.getDataConsName());
            return super.visit_Expr_DataCons(cons, arg);
        }        
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_Var(Var var, Object arg) {
            
            if(super.isDependeeReference(var.getVarName())) {
                recordDependeeReference(var.getVarName());
            }
            
            return super.visit_Expr_Var(var, arg);
        }

        // Binary operations are translated to textual form and then
        // recorded as dependees 
        /** {@inheritDoc} */
        @Override
        protected Void visit_Expr_BinaryOp_Helper(BinaryOp binop, Object arg) {
            QualifiedName textualName = OperatorInfo.getTextualName(binop.getOpText());

            // Some operators represent function/method applications, and some (:) represent data constructor applications
            if (textualName.lowercaseFirstChar()) {
                recordDependeeReference(SourceModel.Name.Function.make(textualName));
            } else {
                recordDependeeReference(SourceModel.Name.DataCons.make(textualName));
            }
            return super.visit_Expr_BinaryOp_Helper(binop, arg);
        }
        
        /** {@inheritDoc} */
        // Unary negation is also translated to textual form and then recorded
        // as a dependee
        @Override
        public Void visit_Expr_UnaryOp_Negate(Negate negate, Object arg) {
            recordDependeeReference(SourceModel.Name.Function.make(CAL_Prelude.Functions.negate));
            return super.visit_Expr_UnaryOp_Negate(negate, arg);
        }
        
        /** {@inheritDoc} */
        // The Unit datacons () is translated to textual form and recorded as a dependee
        @Override
        public Void visit_Expr_Unit(Unit unit, Object arg) {
            recordDependeeReference(SourceModel.Name.DataCons.make(CAL_Prelude.DataConstructors.Unit));
            return super.visit_Expr_Unit(unit, arg);
        }
        
        /** {@inheritDoc} */
        // [] is a reference to Prelude.Nil
        @Override
        public Void visit_Expr_List(SourceModel.Expr.List list, Object arg) {
            if(list.getNElements() == 0) {
                recordDependeeReference(SourceModel.Name.DataCons.make(CAL_Prelude.DataConstructors.Nil));
            }
            return super.visit_Expr_List(list, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Import_UsingItem_Function(UsingItem.Function usingItemFunction, Object arg) {

            if(arg == null || !(arg instanceof ModuleName)) {
                throw new IllegalArgumentException("ReferenceFrequencyFinder.visitImportUsingItemFunction expects to be passed a module name as its arg");
            }
            
            ModuleName importedModuleName = (ModuleName)arg;
            String[] usingNames = usingItemFunction.getUsingNames();
            for (final String usingName : usingNames) {
                recordImportedNameOccurrence(QualifiedName.make(importedModuleName, usingName));
            }
            
            return super.visit_Import_UsingItem_Function(usingItemFunction, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Import_UsingItem_DataConstructor(UsingItem.DataConstructor usingItemDataConstructor, Object arg) {

            if(arg == null || !(arg instanceof ModuleName)) {
                throw new IllegalArgumentException("ReferenceFrequencyFinder.visitImportUsingItemDataConstructor expects to be passed a module name as its arg");
            }
            
            ModuleName importedModuleName = (ModuleName)arg;
            String[] usingNames = usingItemDataConstructor.getUsingNames();
            for (final String usingName : usingNames) {
                recordImportedNameOccurrence(QualifiedName.make(importedModuleName, usingName));
            }
            
            return super.visit_Import_UsingItem_DataConstructor(usingItemDataConstructor, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Import_UsingItem_TypeConstructor(UsingItem.TypeConstructor usingItemTypeConstructor, Object arg) {

            if(arg == null || !(arg instanceof ModuleName)) {
                throw new IllegalArgumentException("ReferenceFrequencyFinder.visitImportUsingItemTypeConstructor expects to be passed a module name as its arg");
            }
            
            ModuleName importedModuleName = (ModuleName)arg;
            String[] usingNames = usingItemTypeConstructor.getUsingNames();
            for (final String usingName : usingNames) {
                recordImportedNameOccurrence(QualifiedName.make(importedModuleName, usingName));
            }
            
            return super.visit_Import_UsingItem_TypeConstructor(usingItemTypeConstructor, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Import_UsingItem_TypeClass(UsingItem.TypeClass usingItemTypeClass, Object arg) {

            if(arg == null || !(arg instanceof ModuleName)) {
                throw new IllegalArgumentException("ReferenceFrequencyFinder.visitImportUsingItemTypeClass expects to be passed a module name as its arg");
            }
            
            ModuleName importedModuleName = (ModuleName)arg;
            String[] usingNames = usingItemTypeClass.getUsingNames();
            for (final String usingName : usingNames) {
                recordImportedNameOccurrence(QualifiedName.make(importedModuleName, usingName));
            }
            
            return super.visit_Import_UsingItem_TypeClass(usingItemTypeClass, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Constraint_TypeClass(SourceModel.Constraint.TypeClass typeClass, Object arg) {

            recordImportedNameOccurrence(typeClass.getTypeClassName());
            return super.visit_Constraint_TypeClass(typeClass, arg);
        }

        /** {@inheritDoc} */
        @Override
        public Void visit_TypeExprDefn_Function(TypeExprDefn.Function function, Object arg) {

            recordImportedNameOccurrence(CAL_Prelude.TypeConstructors.Function);
            return super.visit_TypeExprDefn_Function(function, arg);
        }

        /** {@inheritDoc} */
        @Override
        public Void visit_TypeExprDefn_List(TypeExprDefn.List list, Object arg) {

            recordImportedNameOccurrence(CAL_Prelude.TypeConstructors.List);
            return super.visit_TypeExprDefn_List(list, arg);
        }

        /** {@inheritDoc} */
        @Override
        public Void visit_TypeExprDefn_TypeCons(TypeExprDefn.TypeCons cons, Object arg) {
            
            recordImportedNameOccurrence(cons.getTypeConsName());
            return super.visit_TypeExprDefn_TypeCons(cons, arg);
        }

        /** {@inheritDoc} */
        @Override
        public Void visit_TypeExprDefn_Unit(TypeExprDefn.Unit unit, Object arg) {
            
            recordImportedNameOccurrence(CAL_Prelude.TypeConstructors.Unit);
            return super.visit_TypeExprDefn_Unit(unit, arg);
        }

        /**
         * Add an entry to the internal tracking set noting that the gem specified by entityName
         * is referred to by the current function.
         * @param entityName Name of the gem referred to by the current function
         */
        private void recordDependeeReference(Name.Qualifiable entityName) {
            FunctionalAgent dependeeEntity = super.getFunctionalAgent(entityName);

            if(dependeeEntity != null) {
                Function currentEntity = super.moduleTypeInfo.getFunction(getCurrentFunction());
                Pair<QualifiedName, QualifiedName> key = new Pair<QualifiedName, QualifiedName>(dependeeEntity.getName(), currentEntity.getName());
                
                if(dependeeMap.get(key) != null) {
                    Integer oldFrequency = dependeeMap.get(key);
                    dependeeMap.put(key, Integer.valueOf(oldFrequency.intValue() + 1));
                } else {
                    dependeeMap.put(key, Integer.valueOf(1));
                }
            }
        
            recordImportedNameOccurrence(entityName);
        }
        
        /**
         * Add an entry to the importedNameOccurrences Set, but not to the dependeeMap.
         * Does nothing if referenceName is from the current module
         * @param occurrenceName QualifiedName to add to tracking
         */
        private void recordImportedNameOccurrence(QualifiedName occurrenceName) {
            
            if(occurrenceName == null) {
                return;
            }
            
            if(occurrenceName.getModuleName().equals(super.currentModule)) {
                return;
            }
            
            importedNameOccurrences.add(occurrenceName);
        }
        
        /**
         * Add an entry to the importedNameOccurrences Set, but not to the dependeeMap.
         * Does nothing if referenceName is from the current module
         * @param occurrenceName Name to add to tracking
         */
        private void recordImportedNameOccurrence(Name.Qualifiable occurrenceName) {
            
            // Short-circuit return won't catch all the cases, but it'll allow us to avoid
            // doing a "using" lookup in some cases.  The QualifiedName-accepting version that
            // we delegate to will catch any same-module cases that fall through this check.
            if(occurrenceName.getModuleName() != null && super.resolveModuleName(occurrenceName.getModuleName()).equals(super.currentModule)) {
                return;
            }
            
            QualifiedName qualifiedName = getQualifiedName(occurrenceName, super.moduleTypeInfo.getModuleNameResolver());
            recordImportedNameOccurrence(qualifiedName);
        }
        
        /**
         * @return A Map from (dependee, dependent) to number of times dependent references dependee
         */
        Map<Pair<QualifiedName, QualifiedName>, Integer> getDependeeMap() {
            return Collections.unmodifiableMap(dependeeMap);
        }
        
        /**
         * @return The Set of names imported from other modules that occur in this module.
         */
        Set<QualifiedName> getImportedNameOccurrences() {
            return Collections.unmodifiableSet(importedNameOccurrences);
        }
    }
        
    /**
     * A SourceModelTraverser that scans a single module for compositional frequencies.
     * Compositional frequency measures how often the return value from one gem is passed
     * directly as an argument to another gem.
     * 
     * For example, in the following module:
     * 
     *      module Foo;
     *      import Prelude;
     * 
     *      bar = 50;
     *      baz arg = 20 + arg;
     * 
     *      quux = Prelude.add bar (baz 54);
     * 
     *      quuux val = Prelude.add (baz 10) (baz val);
     * 
     * The compositional frequency of (Prelude.add, Foo.bar) is 1 (since the output of bar is
     * passed directly to add once in quux).  
     * The compositional frequency of (Prelude.add, Foo.baz) is 3 (since the output of baz is
     * passed directly to add once in quux and twice in quuux).
     * 
     * Use this class by instantiating it bound to a module and then passing in a SourceModel
     * representing the same module to visitModuleDefn.  After the module has been walked, calling
     * getCompositionalFrequencyMap will return a Map from gem pair to compositional frequency of the
     * pair.
     * 
     * The module to walk and all the modules that it imports is assumed to have already been 
     * successfully compiled.
     * 
     * This class has no external side effects.  It is mutable (its state is different after it has
     * walked a module). 
     * 
     * Creation date: (Jun 27, 2005)
     * @author Jawright
     */
    private static final class CompositionalFrequencyFinder extends MetricVisitor {
        
        /**
         * Map from (consumer, producer) Pair to compositional frequency of the pair.
         */  
        private final Map<Pair<QualifiedName, QualifiedName>, Integer> compositionalFrequencyMap = new HashMap<Pair<QualifiedName, QualifiedName>, Integer>();
        
        /** Filter for deciding whether a function is to be visited */
        private final QualifiedNameFilter functionFilter;
        
        /** When true, functions that are skipped (because they match the excludeFunctionRegexp)
         * will have their names dumped to the system console.  When false, such functions will
         * be skipped silently.
         */
        private final boolean traceSkippedFunctions;
        
        private static final QualifiedName COMPOSE = CAL_Prelude.Functions.compose;
        private static final QualifiedName APPLY = CAL_Prelude.Functions.apply;
        
        /**
         * Construct a CompositionalFrequencyFinder for a specific module.
         * @param moduleTypeInfo ModuleTypeInfo for the module to scan for compositional relationships
         * @param functionFilter Filter for deciding which functions will be processed. Cannot be null.
         * @param traceSkippedFunctions When true, each skipped function will have its name dumped to the console.
         *                               When false, skipped functions will be skipped silently 
         */
        private CompositionalFrequencyFinder(ModuleTypeInfo moduleTypeInfo, QualifiedNameFilter functionFilter, boolean traceSkippedFunctions) {
            super(moduleTypeInfo);
            if (functionFilter == null) {
                throw new NullPointerException();
            }
            this.functionFilter = functionFilter;
            this.traceSkippedFunctions = traceSkippedFunctions;
        }
        
        /**
         * @return Map from (consumer, producer) to the compositional frequency of the pair.
         */
        Map<Pair<QualifiedName, QualifiedName>, Integer> getCompositionalFrequencyMap() {
            return Collections.unmodifiableMap(compositionalFrequencyMap);
        }
        
        /** {@inheritDoc} */
        // Don't visit functions matching the exclusion regexp
        @Override
        public Void visit_FunctionDefn_Algebraic(Algebraic algebraic, Object arg) {
            
            if(!functionFilter.acceptQualifiedName(QualifiedName.make(super.currentModule, algebraic.getName()))) {
                if(traceSkippedFunctions) {
                    System.out.println("Skipping test function " + super.currentModule + "." + algebraic.getName());
                }
                return null;
            }
            
            return super.visit_FunctionDefn_Algebraic(algebraic, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_Application(Application application, Object arg) {

            Expr consumerExpr = application.getNthExpression(0);
            Name.Qualifiable consumerName = getTopLevelConsumerName(consumerExpr);
            FunctionalAgent consumerEntity = null;
            
            if(consumerName != null && consumerExpr instanceof Var) {
                consumerEntity = super.getFunctionalAgent(consumerName);
            }
            
            // Process the standard case where an expression with a statically-identifiable consumer name
            // accepts some arguments
            if(consumerName != null) {
                for(int i = 1; i < application.getNExpressions(); i++) {
                    processArgumentExpression(consumerName, i, application.getNthExpression(i));
                }
            }
            
            // Special cases for apply and compose calls
            if(consumerEntity != null) {
                
                // `apply arg1 arg2` is effectively `arg1 arg2`
                if(consumerEntity.getName().equals(APPLY)) {
                    Expr effectiveConsumer = application.getNthExpression(1);
                    Name.Qualifiable effectiveConsumerName = getTopLevelConsumerName(effectiveConsumer);
                    
                    if(effectiveConsumerName != null) {
                        processArgumentExpression(effectiveConsumerName, 1, application.getNthExpression(2));
                    }
                }
                
                // `compose arg1 arg2 arg3 ... argn` is effectively `arg1 (arg2 arg3 ... argn)`
                if(consumerEntity.getName().equals(COMPOSE)) {
                    Expr effectiveConsumer1 = application.getNthExpression(1);
                    Name.Qualifiable effectiveConsumer1Name = getTopLevelConsumerName(effectiveConsumer1);
                    Expr effectiveConsumer2 = application.getNthExpression(2);
                    Name.Qualifiable effectiveConsumer2Name = getTopLevelConsumerName(effectiveConsumer2);
                    
                    if(effectiveConsumer1Name != null) {
                        processArgumentExpression(effectiveConsumer1Name, 1, effectiveConsumer2);
                    }
                    
                    if(effectiveConsumer2Name != null) {
                        for(int i = 3; i < application.getNExpressions(); i++) {
                            processArgumentExpression(effectiveConsumer2Name, i, application.getNthExpression(i));
                        }
                    }
                }
            }
            
            return super.visit_Expr_Application(application, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_BinaryOp_BackquotedOperator_Var(BackquotedOperator.Var backquotedOperator, Object arg) {
            Expr consumerExpr = backquotedOperator.getOperatorVarExpr();
            Name.Qualifiable consumerName = getTopLevelConsumerName(consumerExpr);

            processArgumentExpression(consumerName, 1, backquotedOperator.getLeftExpr());
            processArgumentExpression(consumerName, 2, backquotedOperator.getRightExpr());
            
            return super.visit_Expr_BinaryOp_BackquotedOperator_Var(backquotedOperator, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_BinaryOp_BackquotedOperator_DataCons(BackquotedOperator.DataCons backquotedOperator, Object arg) {
            Expr consumerExpr = backquotedOperator.getOperatorDataConsExpr();
            Name.Qualifiable consumerName = getTopLevelConsumerName(consumerExpr);

            processArgumentExpression(consumerName, 1, backquotedOperator.getLeftExpr());
            processArgumentExpression(consumerName, 2, backquotedOperator.getRightExpr());
            
            return super.visit_Expr_BinaryOp_BackquotedOperator_DataCons(backquotedOperator, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_BinaryOp_Apply(Apply apply, Object arg) {
            Name.Qualifiable consumerName = getTopLevelConsumerName(apply.getLeftExpr());
            
            // `leftArg $ rightArg` is effectively `leftArg rightArg`
            if(consumerName != null) {
                processArgumentExpression(consumerName, 1, apply.getRightExpr());
            }
            
            return super.visit_Expr_BinaryOp_Apply(apply, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_BinaryOp_Compose(Compose compose, Object arg) {
            Name.Qualifiable consumerName = getTopLevelConsumerName(compose.getLeftExpr());
            
            if(consumerName != null) {
                processArgumentExpression(consumerName, 1, compose.getRightExpr());
            }
            
            return super.visit_Expr_BinaryOp_Compose(compose, arg);
        }
        
        /**
         * Returns the name of the consumer represented by an expression if it is possible to 
         * statically determine this.  So, for DataCons and Var expressions, it just returns
         * the name (if it isn't the name of a bound local variable).  These represent the base
         * cases.
         * 
         * For compose expressions, it is sometimes also possible to statically determine
         * the name of a consumer that will pop out of the expression.
         * 
         * @param arg  An expression which may represent a consumer
         * @return Name of the consumer represented by the expression if statically determinable,
         *          or null otherwise.  
         */
        private Name.Qualifiable getTopLevelConsumerName(Expr arg) {
            
            Expr potentialConsumer;
            //we ignore paren expressions and just consider their contents            
            if (arg instanceof Parenthesized) {
                potentialConsumer = ((Parenthesized)arg).getExpression();
            } else {
                potentialConsumer = arg;
            }
            
            // Standard `foo x` case
            if(potentialConsumer instanceof Var) {
                Name.Function consumerName = ((Var)potentialConsumer).getVarName(); 
                
                if(super.isDependeeReference(consumerName)) {
                    return consumerName;
                } else {
                    return null;
                }

            // Standard `Just x` case    
            } else if(potentialConsumer instanceof DataCons) {
                return ((DataCons)potentialConsumer).getDataConsName();
            
            // Special case for expressions of the form `(compose foo bar) baz`
            } else if(potentialConsumer instanceof Application) {
                Application app = (Application)potentialConsumer;
                Name.Qualifiable consumerName = getTopLevelConsumerName(app.getNthExpression(0));
                
                if(consumerName != null) {
                    FunctionalAgent consumer = super.getFunctionalAgent(consumerName);
                    
                    if(consumer.getName().equals(COMPOSE) && app.getNExpressions() == 3) {
                        return getTopLevelConsumerName(app.getNthExpression(2));
                    }
                }
            
            // Special case for expressions of the form `(foo # bar) baz`
            } else if(potentialConsumer instanceof BinaryOp.Compose) {
                BinaryOp.Compose composeOp = (BinaryOp.Compose)potentialConsumer;
                return getTopLevelConsumerName(composeOp.getRightExpr());
            
            }
            
            return null;
        }
        
        /**
         * Record that the functional agent specified by consumerName consumes the expression
         * argumentExpression as its argumentNumberth parameter.  If either consumerName or 
         * argumentExpression refers to a local function definition, then no action is taken.
         * @param consumerName
         * @param argumentNumber
         * @param argument
         */
        private void processArgumentExpression(Name.Qualifiable consumerName, int argumentNumber, Expr argument) {
            
            FunctionalAgent producer = null;
            FunctionalAgent consumer = super.getFunctionalAgent(consumerName);
            
            Expr argumentExpression;
            //we ignore paren expressions and just consider their contents            
            if (argument instanceof Parenthesized) {
                argumentExpression = ((Parenthesized)argument).getExpression();
            } else {
                argumentExpression = argument;
            }
            
            if(argumentExpression instanceof Var) {
                Var var = (Var)argumentExpression;
                if (super.isDependeeReference(var.getVarName())) {
                    producer = super.getFunctionalAgent(var.getVarName());
                }
            }
            
            else if (argumentExpression instanceof DataCons) {
                DataCons cons = (DataCons)argumentExpression;
                producer = super.getFunctionalAgent(cons.getDataConsName());
            }
            
            else if (argumentExpression instanceof Application) {
                Application app = (Application)argumentExpression;
                processArgumentExpression(consumerName, argumentNumber, app.getNthExpression(0));
                return;
            }
            
            else if (argumentExpression instanceof BinaryOp.Apply) {
                BinaryOp.Apply app = (BinaryOp.Apply)argumentExpression;
                processArgumentExpression(consumerName, argumentNumber, app.getLeftExpr());
                return;
            }
            
            else if (argumentExpression instanceof BinaryOp.Compose) {
                BinaryOp.Compose composeOp = (BinaryOp.Compose)argumentExpression;
                processArgumentExpression(consumerName, argumentNumber, composeOp.getLeftExpr());
                return;
            }
            
            if(producer != null && consumer != null) {
                recordComposition(consumer.getName(), producer.getName());
            }
        }
        
        private void recordComposition(QualifiedName consumer, QualifiedName producer) {
            Pair<QualifiedName, QualifiedName> key = new Pair<QualifiedName, QualifiedName>(consumer, producer);
            Integer frequency = compositionalFrequencyMap.get(key);
            if(frequency != null) {
                compositionalFrequencyMap.put(key, Integer.valueOf(frequency.intValue() + 1));
            } else {
                compositionalFrequencyMap.put(key, Integer.valueOf(1));
            }
        }
    }
    
    /**
     * Represents a warning about a piece of code flagged by the lint process.
     * This is an immutable class.
     * 
     * Creation date: (Jul 8, 2005)
     * @author Jawright
     */
    static final class LintWarning {
        
        /** Typesafe enum representing the type of the warning */
        static final class WarningType {
            
            /** Name of this warning type */
            private final String name;
            
            /** Private constructor for a warning type */
            private WarningType(String name) {
                this.name = name; 
            }
            
            /** {@inheritDoc} */
            @Override
            public String toString() {
                return name;
            }
            
            static final WarningType REDUNDANT_LAMBDA = new WarningType("Possibly redundant lambda");
            static final WarningType UNPLINGED_PRIMITIVE_ARG = new WarningType("Unplinged primitive lexical argument");
            static final WarningType UNUSED_PRIVATE_FUNCTION = new WarningType("Unreferenced private function");
            static final WarningType MISMATCHED_ALIAS_PLINGS = new WarningType("Alias function's arguments do not have same strictness as aliased function/data constructor");
            static final WarningType UNREFERENCED_LET_VARIABLE = new WarningType("Unreferenced let variable");
        }
        
        /** Construct a new LintWarning 
         * 
         * @param warningType Type of problem that we are warning about
         * @param flaggedElement Object representing the SourceElement that caused the warning.  Normally this
         *                        this will just be the SourceElement, but occasionally we may want to custom-print
         *                        the flagged element (eg, for brevity), so a String may also be passed in.
         * @param sourceRange SourceRange of the element that caused the warning
         * @param moduleName Name of the module that the source element causing the warning occurs in
         * @param functionName Name of the top-level function that the source element that caused the warning occurs in. 
         */
        private LintWarning(WarningType warningType, Object flaggedElement, SourceRange sourceRange, 
                    ModuleName moduleName, String functionName) {
            
            if(warningType == null) {
                throw new IllegalArgumentException("LintWarning: warningType must not be null");
            }
            
            if(flaggedElement == null) {
                throw new IllegalArgumentException("LintWarning: flaggedElement must not be null");
            }
            
            if(functionName == null) {
                throw new IllegalArgumentException("LintWarning: functionName must not be null");
            }
            
            if(moduleName == null) {
                throw new IllegalArgumentException("LintWarning: moduleName must not be null");
            }
            
            // (no check for sourcePosition because it is allowed to be null)
            
            this.warningType = warningType;
            this.flaggedElement = flaggedElement;
            this.functionName = QualifiedName.make(moduleName, functionName);
            this.sourceRange = sourceRange;
        }
        
        /** The type of warning */
        private final WarningType warningType;
        
        /** A printable representation of the flagged expression.  This is usually just the 
         * SourceElement that caused the warning, but it will occasionally be a String representing some
         * subset of the element (e.g., the lefthand side of an algebraic function definition). */
        private final Object flaggedElement;
        
        /** The position range of the flagged expression */
        private final SourceRange sourceRange; 
        
        /** The name of the top-level function in which the error occurs */
        private final QualifiedName functionName;
        
        /**
         * @return a string representation of the source element that caused the warning
         */
        String getFlaggedElement() {
            return flaggedElement.toString();
        }

        /**
         * @return the top-level function in which the flagged expression occurs
         */
        QualifiedName getFunctionName() {
            return functionName;
        }
        
        /**
         * @return the start position of the flagged expression, if available, or null if not available
         */
        SourcePosition getSourcePosition() {
            if(sourceRange != null) {
                return sourceRange.getStartSourcePosition();
            } else {
                return null;
            }
        }
        
        /**
         * @return the location of the flagged expression, if available, or null if not available
         */
        SourceRange getSourceRange() {
            return sourceRange;
        }
        
        /**
         * @return the type of the warning
         */
        WarningType getWarningType() {
            return warningType;
        }
    
        /** {@inheritDoc} */
        @Override
        public String toString() {
            if(sourceRange != null) { 
                return warningType + " in " + functionName + " " + sourceRange + ": " + flaggedElement;
            } else {
                return warningType + " in " + functionName + ": " + flaggedElement;
            }
        }
    }
    
    /**
     * A SourceModelTraverser that scans a single module for various style problems that
     * are statically detectable.  Currently scans for the following problems:
     * 
     *   - Redundant lambdas
     *   - Unreferenced private functions
     *   - Lexical arguments of primitive types that are unplinged
     *   - Alias functions whose arguments are plinged incompatibly from the aliased function
     *   - Let variables that are never referenced. 
     * 
     * Warnings are accumulated as a list of LintWarnings, which can be retrieved using the 
     * getWarningList method.
     * 
     * The module to walk and all the modules that it imports is assumed to have already been 
     * successfully compiled.
     * 
     * The let-variable check relies on the assumption that the component SourceElements of the 
     * SourceModel that we walk are not shared (ie, it assumes that the graph is a tree).
     * This assumption can be enforced by copying the incoming tree using the SourceModelCopier;
     * we don't perform that (expensive) step currently because the SourceModelBuilder does 
     * generate trees, and those are the only models that we're currently checking.  
     *  
     * 
     * Creation date: (Jul 4, 2005)
     * @author Jawright
     */
    private static class LintWalker extends MetricVisitor {
        
        /** List of warnings found so far */
        private final List<LintWarning> warningList = new ArrayList<LintWarning>();
        
        /** Filter for deciding which functions will be processed. */
        private final QualifiedNameFilter functionFilter;
        
        /** Set of functions in the module with at least one reference to them within the module. */
        private final Set<QualifiedName> referencedFunctions = new HashSet<QualifiedName>();
        
        /** Map from unqualified function or datacons name to array of parameter strictness */
        private final Map<String, boolean[]> parameterStrictnessMap = new HashMap<String, boolean[]>();
        
        /** Set of SourceElements of bound names whose names have been referenced */
        private final Set<SourceModel.SourceElement> referencedBoundNames = new HashSet<SourceModel.SourceElement>();
        
        /** When true, dump the names of skipped functions to the console */
        private final boolean traceSkippedFunctions;
        
        /** When true, return warnings about unplinged function arguments with primitive types. */
        private final boolean includeUnplingedPrimitiveArgs;
        
        /** When true, return warnings about potentially redundant lambdas */
        private final boolean includeRedundantLambdas;
        
        /** When true, return warnings about unreferenced private functions */
        private final boolean includeUnusedPrivates;
        
        /** When true, return warnings about alias functions whose argument strictness does not exactly match that of the wrapped function. */
        private final boolean includeMismatchedAliasPlings;

        /** When true, return warnings about unused let variables */
        private final boolean includeUnreferencedLetVariables;
        
        /**
         * Construct a LintWalker
         * @param moduleTypeInfo moduleTypeInfo for the module that will be walked
         * @param functionFilter Filter for deciding which functions will be processed. Cannot be null.
         * @param traceSkippedFunctions If true, all functions that are skipped will have their names
         *                               dumped to the console.
         * @param includeUnplingedPrimitiveArgs
         * @param includeRedundantLambdas
         * @param includeUnusedPrivates
         * @param includeMismatchedAliasPlings
         * @param includeUnreferencedLetVariables
         */
        private LintWalker(ModuleTypeInfo moduleTypeInfo, QualifiedNameFilter functionFilter, boolean traceSkippedFunctions,
                           boolean includeUnplingedPrimitiveArgs, boolean includeRedundantLambdas, boolean includeUnusedPrivates, boolean includeMismatchedAliasPlings, boolean includeUnreferencedLetVariables) {
            super(moduleTypeInfo);
            if (functionFilter == null) {
                throw new NullPointerException();
            }
            this.functionFilter = functionFilter;
            this.traceSkippedFunctions = traceSkippedFunctions;
            this.includeUnplingedPrimitiveArgs = includeUnplingedPrimitiveArgs;
            this.includeRedundantLambdas = includeRedundantLambdas;
            this.includeUnusedPrivates = includeUnusedPrivates;
            this.includeMismatchedAliasPlings = includeMismatchedAliasPlings; 
            this.includeUnreferencedLetVariables = includeUnreferencedLetVariables;
            
            if(includeUnusedPrivates) {
                computeReferencedFunctions();
            }
        }
        /** get an expressions without parens */    
        private Expr getExprWithoutParen(Expr expr) {
            if (expr instanceof Parenthesized) {
                return ((Parenthesized)expr).getExpression();
            } else {
                return expr;
            }
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_ModuleDefn(ModuleDefn defn, Object arg) {

            // We do a pretraversal to find out about parameter strictness
            if(includeMismatchedAliasPlings) {
                ParameterStrictnessWalker strictnessWalker = new ParameterStrictnessWalker();
                strictnessWalker.visit_ModuleDefn(defn, null);
                parameterStrictnessMap.putAll(strictnessWalker.getStrictnessMap());
            }
            
            return super.visit_ModuleDefn(defn, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_Expr_Lambda(Lambda lambda, Object arg) {
            boolean isRedundant = false;

            Expr lambdaExp = getExprWithoutParen(lambda.getDefiningExpr());
            
            if(lambdaExp instanceof Application) {
                Application definingExpr = (Application)lambdaExp;
                isRedundant = (definingExpr.getNExpressions() >= lambda.getNParameters() + 1);
                
                // Walk backwards along the parameters so that we catch cases like 
                // (\x -> map add x) 
                int i = lambda.getNParameters() - 1;
                int j = definingExpr.getNExpressions() - 1;
                for(;isRedundant && i >= 0 && j >= 1;i--, j--) {
                    
                    if(!isParameterVar(lambda.getNthParameter(i), definingExpr.getNthExpression(j))) {
                        isRedundant = false;
                    }
                }
                
            } else if(lambdaExp instanceof BinaryOp) {
                BinaryOp definingExpr = (BinaryOp)lambdaExp;
                
                if(lambda.getNParameters() == 2 && 
                   isParameterVar(lambda.getNthParameter(0), definingExpr.getLeftExpr()) &&
                   isParameterVar(lambda.getNthParameter(1), definingExpr.getRightExpr())) {
                    
                    isRedundant = true;
                    
                } else if(lambda.getNParameters() == 1 &&
                   isParameterVar(lambda.getNthParameter(0), definingExpr.getRightExpr()) &&
                   !containsParameterReference(lambda.getNthParameter(0), definingExpr.getLeftExpr())) {
                    
                    isRedundant = true;
                }
            
            } else if(lambdaExp instanceof UnaryOp) {
                UnaryOp definingExpr = (UnaryOp)lambdaExp;
                if(lambda.getNParameters() == 1 &&
                   isParameterVar(lambda.getNthParameter(0), definingExpr.getExpr())) {
                    
                    isRedundant = true;
                }
            }
            
            if(isRedundant && includeRedundantLambdas) {
                warningList.add(new LintWarning(LintWarning.WarningType.REDUNDANT_LAMBDA, lambda, lambda.getSourceRange(), super.currentModule, getCurrentFunction()));
            }
            
            return super.visit_Expr_Lambda(lambda, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_FunctionDefn_Algebraic(Algebraic algebraic, Object arg) {
            
            String functionName = algebraic.getName();
            Function functionEntity = super.moduleTypeInfo.getFunction(functionName);
            
            // Don't process functions that match the exclusion expression
            if(!functionFilter.acceptQualifiedName(QualifiedName.make(super.currentModule, algebraic.getName()))) {
                if(traceSkippedFunctions) {
                    System.out.println("Skipping test function " + super.currentModule + "." + algebraic.getName());
                }
                return null;
            }
            
            if(includeUnplingedPrimitiveArgs) {
                checkForUnplingedPrimitives(algebraic, functionName, functionEntity);
            }
            
            if(includeUnusedPrivates) {
                checkForUnusedPrivates(algebraic, functionName, functionEntity);
            }
            
            if(includeMismatchedAliasPlings) {
                checkForMismatchedAliasPlings(algebraic, functionName, functionEntity);
            }
            
            return super.visit_FunctionDefn_Algebraic(algebraic, arg);
        }
        
        /** {@inheritDoc} */
        @Override
        public Void visit_FunctionDefn_Foreign(FunctionDefn.Foreign foreign, Object arg) {
            
            String functionName = foreign.getName();
            Function functionEntity = super.moduleTypeInfo.getFunction(functionName);
            
            // Don't process functions that match the exclusion expression
            if(!functionFilter.acceptQualifiedName(QualifiedName.make(super.currentModule, foreign.getName()))) {
                if(traceSkippedFunctions) {
                    System.out.println("Skipping test function " + super.currentModule + "." + foreign.getName());
                }
                return null;
            }
            
            if(includeUnusedPrivates) {
                checkForUnusedPrivates(foreign, functionName, functionEntity);
            }
            
            return super.visit_FunctionDefn_Foreign(foreign, arg);
        }
        
        /** {@inheritDoc} */
        /*  Unused let-variable checks
         *  
         *  We first call the super-method, which will visit each subexpression
         *  of the let-expression with bindings fully set up.  Our override of
         *  visitFunctionName adds the referenced SourceElement to the set
         *  referencedBoundNames each time it encounters a reference to a bound
         *  name during this traversal of the subexpressions.
         *  
         *  So, when the super-method returns, we have a set containing the 
         *  SourceElement of each bound name that was referenced in the 
         *  subexpressions.  We iterate over each of our local definitions;
         *  if the local definition is an element of the referencedBoundNames
         *  set, we remove it (since it scopes out after we return).
         *  If the local defninition is NOT contained in referencedBoundNames,
         *  we spit out a warning about an unreferenced let variable.
         */
        @Override
        public Void visit_Expr_Let(Let let, Object arg) {
            final Void ret = super.visit_Expr_Let(let, arg);
            
            final ModuleName currentModule = super.currentModule;
            
            /**
             * Handles the identification of unused let variables.
             * @author Joseph Wong
             */
            class UnusedLetVariablesCollector extends SourceModelTraverser<Void, Void> {

                @Override
                public Void visit_LocalDefn_Function_Definition(final Definition function, final Void arg) {
                    if(referencedBoundNames.contains(function)) {
                        referencedBoundNames.remove(function);
                    
                    } else if(includeUnreferencedLetVariables) {
                        warningList.add(new LintWarning(LintWarning.WarningType.UNREFERENCED_LET_VARIABLE, function.getName(), function.getSourceRange(), currentModule, getCurrentFunction()));
                    }
                    return null;
                }

                @Override
                public Void visit_Pattern_Var(final Pattern.Var var, final Void arg) {
                    if(referencedBoundNames.contains(var)) {
                        referencedBoundNames.remove(var);
                    
                    } else if(includeUnreferencedLetVariables) {
                        warningList.add(new LintWarning(LintWarning.WarningType.UNREFERENCED_LET_VARIABLE, var.getName(), var.getSourceRange(), currentModule, getCurrentFunction()));
                    }
                    return null;
                }

                @Override
                public Void visit_FieldPattern(final FieldPattern fieldPattern, final Void arg) {
                    // H
