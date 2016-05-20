/*
 * Copyright 2009-2010 MBTE Sweden AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbte.groovypp.compiler;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;

public class CompilerStack implements Opcodes {
    // state flag
    private boolean clear=true;
    // current scope
    private VariableScope scope;
    // current label for continue
    private Label continueLabel;
    // current label for break
    private Label breakLabel;
    // available variables on stack
    private Map<String, Register> stackVariables = new HashMap<String, Register>();
    // index of the last variable on stack
    private int currentVariableIndex = 1;
    // index for the next variable on stack
    private int nextVariableIndex = 1;
    // currently temporary variables in use
    private final LinkedList<Register> temporaryVariables = new LinkedList<Register>();
    // overall used variables for a method/constructor
    private final LinkedList<Register> usedVariables = new LinkedList<Register>();
    // map containing named labels of parenting blocks
    private Map<String, Label> superBlockNamedLabels = new HashMap<String, Label>();
    // map containing named labels of current block
    private Map<String, Label> currentBlockNamedLabels = new HashMap<String, Label>();
    // list containing runnables representing a finally block
    // such a block is created by synchronized or finally and
    // must be called for break/continue/return
    private LinkedList finallyBlocks = new LinkedList();
    // a list of blocks already visiting.
    private final List<Runnable> visitedBlocks = new LinkedList<Runnable>();

    private Label thisStartLabel, thisEndLabel;

    private MethodVisitor mv;
    private BytecodeHelper helper;

    // helper to handle different stack based variables
    private final LinkedList<StateStackElement> stateStack = new LinkedList<StateStackElement>();

    // defines the first variable index useable after
    // all parameters of a method
    private int localVariableOffset;
    // this is used to store the goals for a "break foo" call
    // in a loop where foo is a label.
	private final Map<String, Label> namedLoopBreakLabel = new HashMap<String, Label>();
	//this is used to store the goals for a "continue foo" call
    // in a loop where foo is a label.
	private final Map<String, Label> namedLoopContinueLabel = new HashMap<String, Label>();
    private String className;

    CompilerStack parent;
    public static final ClassNode inferenced_TYPE = ClassHelper.make("TYPE INFERENCE");

    static {
        inferenced_TYPE.setRedirect(ClassHelper.OBJECT_TYPE);
    }

    public CompilerStack(CompilerStack compileStack) {
        parent = compileStack;
    }

    private class StateStackElement {
        final VariableScope scope;
        final Label continueLabel;
        final Label breakLabel;
        Label finallyLabel;
        final int lastVariableIndex;
        final int nextVariableIndex;
        final Map<String, Register> stackVariables;
        List<Register> temporaryVariables = new LinkedList<Register>();
        List usedVariables = new LinkedList();
        final Map<String, Label> superBlockNamedLabels;
        final Map<String, Label> currentBlockNamedLabels;
        final LinkedList finallyBlocks;

        StateStackElement() {
            scope = CompilerStack.this.scope;
            continueLabel = CompilerStack.this.continueLabel;
            breakLabel = CompilerStack.this.breakLabel;
            lastVariableIndex = CompilerStack.this.currentVariableIndex;
            stackVariables = CompilerStack.this.stackVariables;
            temporaryVariables = CompilerStack.this.temporaryVariables;
            nextVariableIndex = CompilerStack.this.nextVariableIndex;
            superBlockNamedLabels = CompilerStack.this.superBlockNamedLabels;
            currentBlockNamedLabels = CompilerStack.this.currentBlockNamedLabels;
            finallyBlocks = CompilerStack.this.finallyBlocks;
        }
    }

    protected void pushState() {
        stateStack.add(new StateStackElement());
        stackVariables = new HashMap<String, Register>(stackVariables);
        finallyBlocks = new LinkedList(finallyBlocks);
    }

    private void popState() {
        if (stateStack.size()==0) {
             throw new GroovyBugError("Tried to do a pop on the compiler stack without push.");
        }
        StateStackElement element = stateStack.removeLast();
        scope = element.scope;
        continueLabel = element.continueLabel;
        breakLabel = element.breakLabel;
        currentVariableIndex = element.lastVariableIndex;
        stackVariables = element.stackVariables;
        nextVariableIndex = element.nextVariableIndex;
        finallyBlocks = element.finallyBlocks;
    }

    public Label getContinueLabel() {
        return continueLabel;
    }

    public Label getBreakLabel() {
        return breakLabel;
    }

    public void removeVar(int tempIndex) {
        final Register head = temporaryVariables.removeFirst();
        if (head.getIndex() != tempIndex)
            throw new GroovyBugError("CompileStack#removeVar: tried to remove a temporary variable in wrong order");

        currentVariableIndex = head.getPrevIndex ();
        nextVariableIndex = tempIndex;
    }

    private void setEndLabels(){
        Label endLabel = new Label();
        mv.visitLabel(endLabel);
        for (Iterator<Register> iter = stackVariables.values().iterator(); iter.hasNext();) {
            Register var = iter.next();
            var.setEndLabel(endLabel);
        }
        thisEndLabel = endLabel;
    }

    public void pop() {
        setEndLabels();
        popState();
    }

    public VariableScope getScope() {
        return scope;
    }

    /**
     * Returns a normal variable.
     * <p/>
     * If <code>mustExist</code> is true and the normal variable doesn't exist,
     * then this method will throw a GroovyBugError. It is not the intention of
     * this method to let this happen! And the exception should not be used for
     * flow control - it is just acting as an assertion. If the exception is thrown
     * then it indicates a bug in the class using CompileStack.
     * This method can also not be used to return a temporary variable.
     * Temporary variables are not normal variables.
     *
     * @param variableName name of the variable
     * @param mustExist    throw exception if variable does not exist
     * @return the normal variable or null if not found (and <code>mustExist</code> not true)
     */
    public Register getRegister(String variableName, boolean mustExist) {
        if (variableName.equals("this")) return Register.THIS_VARIABLE;
        if (variableName.equals("super")) return Register.SUPER_VARIABLE;
        Register v = stackVariables.get(variableName);
        if (v == null && mustExist) {
            throw new GroovyBugError("tried to get a variable with the name " + variableName + " as stack variable, but a variable with this name was not created");
        }
        return v;
    }

    /**
     * creates a temporary variable.
     *
     * @param name defines the name
     * @param node defines the node
     * @param store defines if the toplevel argument of the stack should be stored
     * @return the index used for this temporary variable
     */
    public int defineTemporaryVariable(String name, ClassNode node, boolean store) {
        Register answer = defineVar(name,node,false);
        temporaryVariables.addFirst(answer); // TRICK: we add at the beginning so when we find for remove or get we always have the last one
        usedVariables.removeLast();

        if (store) doStore(node);

        return answer.getIndex();
    }

    private void resetVariableIndex(boolean isStatic) {
        if (!isStatic) {
            currentVariableIndex=1;
            nextVariableIndex=1;
        } else {
            currentVariableIndex=0;
            nextVariableIndex=0;
        }
    }

    /**
     * Clears the state of the class. This method should be called
     * after a MethodNode is visited. Note that a call to init will
     * fail if clear is not called before
     */
    public void clear() {
        if (stateStack.size()>1) {
            int size = stateStack.size()-1;
            throw new GroovyBugError("the compiler stack contains "+size+" more push instruction"+(size==1?"":"s")+" than pops.");
        }
        clear = true;
        // br experiment with local var table so debuggers can retrieve variable names
        if (true) {//AsmClassGenerator.CREATE_DEBUG_INFO) {
            if (thisEndLabel==null) setEndLabels();

            if (!scope.isInStaticContext()) {
                // write "this"
                mv.visitLocalVariable("this", className, null, thisStartLabel, thisEndLabel, 0);
            }

            for (Iterator<Register> iterator = usedVariables.iterator(); iterator.hasNext();) {
                Register v = iterator.next();
                String type = BytecodeHelper.getTypeDescription(v.getType());
                Label start = v.getStartLabel();
                Label end = v.getEndLabel();
                mv.visitLocalVariable(v.getName(), type, null, start, end, v.getIndex());
            }
        }
        pop();
        stackVariables.clear();
        usedVariables.clear();
        scope = null;
        mv=null;
        resetVariableIndex(false);
        superBlockNamedLabels.clear();
        currentBlockNamedLabels.clear();
        namedLoopBreakLabel.clear();
        namedLoopContinueLabel.clear();
        continueLabel=null;
        breakLabel=null;
        helper = null;
        thisStartLabel=null;
        thisEndLabel=null;
    }

    /**
     * initializes this class for a MethodNode. This method will
     * automatically define varibales for the method parameters
     * and will create references if needed. the created variables
     * can be get by getVariable
     *
     */
    public void init(VariableScope el, Parameter[] parameters, MethodVisitor mv, ClassNode cn) {
        if (!clear) throw new GroovyBugError("CompileStack#init called without calling clear before");
        clear=false;
        pushVariableScope(el);
        this.mv = mv;
        this.helper = new BytecodeHelper(mv);
        defineMethodVariables(parameters,el.isInStaticContext());
        this.className = BytecodeHelper.getTypeDescription(cn);
    }

    /**
     * Causes the statestack to add an element and sets
     * the given scope as new current variable scope. Creates
     * a element for the state stack so pop has to be called later
     */
    protected void pushVariableScope(VariableScope el) {
        pushState();
        scope = el;
        superBlockNamedLabels = new HashMap<String, Label>(superBlockNamedLabels);
        superBlockNamedLabels.putAll(currentBlockNamedLabels);
        currentBlockNamedLabels = new HashMap<String, Label>();
    }

    /**
     * Should be called when decending into a loop that defines
     * also a scope. Calls pushVariableScope and prepares labels
     * for a loop structure. Creates a element for the state stack
     * so pop has to be called later
     */
    protected void pushLoop(VariableScope el, String labelName) {
        pushVariableScope(el);
        initLoopLabels(labelName);
    }

    private void initLoopLabels(String labelName) {
        continueLabel = new Label();
        breakLabel = new Label();
        if (labelName!=null) {
        	namedLoopBreakLabel.put(labelName,breakLabel);
        	namedLoopContinueLabel.put(labelName,continueLabel);
        }
    }

    /**
     * Should be called when descending into a loop that does
     * not define a scope. Creates a element for the state stack
     * so pop has to be called later
     */
    protected void pushLoop(String labelName) {
        pushState();
        initLoopLabels(labelName);
    }

    protected Label getNamedBreakLabel(String name) {
        if (name!=null) return namedLoopBreakLabel.get(name);
        return getBreakLabel();
    }

    protected Label getNamedContinueLabel(String name) {
        if (name!=null) return namedLoopContinueLabel.get(name);
        return getContinueLabel();
    }

    /**
     * Creates a new break label and a element for the state stack
     * so pop has to be called later
     */
    protected Label pushSwitch(){
        pushState();
        breakLabel = new Label();
        return breakLabel;
    }

    /**
     * because a boolean Expression may not be evaluated completly
     * it is important to keep the registers clean
     */
    protected void pushBooleanExpression(){
        pushState();
    }

    private Register defineVar(String name, ClassNode type, boolean methodParameterUsedInClosure) {
        makeNextVariableID(type);
        int index = currentVariableIndex;
        if (methodParameterUsedInClosure) {
            index = localVariableOffset++;
            type = TypeUtil.wrapSafely(type);
        }
        Register answer = new Register(index, type, name);
        usedVariables.add(answer);
        return answer;
    }

    private Register defineTypeInferenceVar(String name) {
        makeNextVariableID(ClassHelper.long_TYPE); // we want to allocate 2 slots
        int index = currentVariableIndex;
        Register answer = new Register(index, inferenced_TYPE, name);
        usedVariables.add(answer);
        return answer;
    }

    private void makeLocalVariablesOffset(Parameter[] paras,boolean isInStaticContext) {
        resetVariableIndex(isInStaticContext);

        for (int i = 0; i < paras.length; i++) {
            makeNextVariableID(paras[i].getType());
        }
        localVariableOffset = nextVariableIndex;

        resetVariableIndex(isInStaticContext);
    }

    private void defineMethodVariables(Parameter[] paras,boolean isInStaticContext) {
        Label startLabel  = new Label();
        thisStartLabel = startLabel;
        mv.visitLabel(startLabel);

        makeLocalVariablesOffset(paras,isInStaticContext);

        boolean hasHolder = false;
        for (int i = 0; i < paras.length; i++) {
            String name = paras[i].getName();
            Register answer;
            ClassNode type = paras[i].getType();
            answer = defineVar(name,type,false);
            answer.setStartLabel(startLabel);
            stackVariables.put(name, answer);
        }

        if (hasHolder) {
            nextVariableIndex = localVariableOffset;
        }
    }

    /**
     * Defines a new Register using an AST variable.
     * @param initFromStack if true the last element of the
     *                      stack will be used to initilize
     *                      the new variable. If false null
     *                      will be used.
     */
    public Register defineVariable(Variable v, boolean initFromStack) {
        String name = v.getName();
        final ClassNode type = v.getType();
        Register answer = defineVar(name, type, false);
        stackVariables.put(name, answer);

        Label startLabel  = new VarStartLabel();
        answer.setStartLabel(startLabel);

        if (!initFromStack) {
            if (ClassHelper.isPrimitiveType(type)) {
                if (type == ClassHelper.long_TYPE)
                    mv.visitInsn(LCONST_0);
                else
                if (type == ClassHelper.float_TYPE)
                    mv.visitInsn(FCONST_0);
                else
                if (type == ClassHelper.double_TYPE)
                    mv.visitInsn(DCONST_0);
                else
                    mv.visitInsn(ICONST_0);
            }
            else
                mv.visitInsn(ACONST_NULL);
        }

        doStore(type);

        mv.visitLabel(startLabel);
        return answer;
    }

    public Register defineTypeInferenceVariable(Variable v, ClassNode initType) {
        String name = v.getName();
        Register answer = defineTypeInferenceVar(name);
        stackVariables.put(name, answer);

        Label startLabel  = new VarStartLabel();
        answer.setStartLabel(startLabel);

        doStore(initType);

        mv.visitLabel(startLabel);
        return answer;
    }

    private void doStore(ClassNode type) {
        if (ClassHelper.isPrimitiveType(type)) {
            if (type == ClassHelper.long_TYPE)
                mv.visitVarInsn(LSTORE, currentVariableIndex);
            else
            if (type == ClassHelper.float_TYPE)
                mv.visitVarInsn(FSTORE, currentVariableIndex);
            else
            if (type == ClassHelper.double_TYPE)
                mv.visitVarInsn(DSTORE, currentVariableIndex);
            else
                mv.visitVarInsn(ISTORE, currentVariableIndex);
        }
        else
            mv.visitVarInsn(ASTORE, currentVariableIndex);
    }

    /**
     * @param name the name of the variable of interest
     * @return true if a variable is already defined
     */
    public boolean containsVariable(String name) {
        return stackVariables.containsKey(name);
    }

    /**
     * Calculates the index of the next free register stores ir
     * and sets the current variable index to the old value
     */
    private void makeNextVariableID(ClassNode type) {
        currentVariableIndex = nextVariableIndex;
        if (type==ClassHelper.long_TYPE || type==ClassHelper.double_TYPE) {
            nextVariableIndex++;
        }
        nextVariableIndex++;
    }

    /**
     * Returns the label for the given name
     */
    public Label getLabel(String name) {
        if (name==null) return null;
        Label l = superBlockNamedLabels.get(name);
        if (l==null) l = createLocalLabel(name);
        return l;
    }

    /**
     * creates a new named label
     */
    public Label createLocalLabel(String name) {
        Label l = currentBlockNamedLabels.get(name);
        if (l==null) {
            l = new Label();
            currentBlockNamedLabels.put(name,l);
        }
        return l;
    }

    public void applyFinallyBlocks(Label label, boolean isBreakLabel) {
        // first find the state defining the label. That is the state
        // directly after the state not knowing this label. If no state
        // in the list knows that label, then the defining state is the
        // current state.
        StateStackElement result = null;
        for (ListIterator<StateStackElement> iter = stateStack.listIterator(stateStack.size()); iter.hasPrevious();) {
            StateStackElement element = iter.previous();
            if (!element.currentBlockNamedLabels.values().contains(label)) {
                if (isBreakLabel && element.breakLabel != label) {
                    result = element;
                    break;
                }
                if (!isBreakLabel && element.continueLabel != label) {
                    result = element;
                    break;
                }
            }
        }

        List blocksToRemove;
        if (result==null) {
            // all Blocks do know the label, so use all finally blocks
            blocksToRemove = Collections.EMPTY_LIST;
        } else {
            blocksToRemove = result.finallyBlocks;
        }

        ArrayList blocks = new ArrayList(finallyBlocks);
        blocks.removeAll(blocksToRemove);
        applyFinallyBlocks(blocks);
    }

    private void applyFinallyBlocks(List blocks) {
        for (Iterator iter = blocks.iterator(); iter.hasNext();) {
            Runnable block = (Runnable) iter.next();
            if (visitedBlocks.contains(block)) continue;
            block.run();
        }
    }

    public void applyFinallyBlocks() {
        applyFinallyBlocks(finallyBlocks);
    }

    public boolean hasFinallyBlocks() {
        return !finallyBlocks.isEmpty();
    }

    public void pushFinallyBlock(Runnable block) {
        finallyBlocks.addFirst(block);
        pushState();
    }

    public void popFinallyBlock() {
        popState();
        finallyBlocks.removeFirst();
    }

    public void pushFinallyBlockVisit(Runnable block) {
        visitedBlocks.add(block);
    }

    public void popFinallyBlockVisit(Runnable block) {
        visitedBlocks.remove(block);
    }

    public static class VarStartLabel extends Label {
    }
}

