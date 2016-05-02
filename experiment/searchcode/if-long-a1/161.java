package jeliot.mcode;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import jeliot.FeatureNotImplementedException;
import jeliot.avinteraction.AVInteractionEngine;
import jeliot.lang.ArrayInstance;
import jeliot.lang.Class;
import jeliot.lang.ClassInfo;
import jeliot.lang.Instance;
import jeliot.lang.ObjectFrame;
import jeliot.lang.Reference;
import jeliot.lang.StaticVariableNotFoundException;
import jeliot.lang.StringInstance;
import jeliot.lang.Value;
import jeliot.lang.Variable;
import jeliot.lang.VariableInArray;
import jeliot.theater.Actor;
import jeliot.theater.Director;
import jeliot.theater.ExpressionActor;
import jeliot.theater.ValueActor;
import jeliot.util.DebugUtil;
import jeliot.util.ResourceBundles;
import jeliot.util.Util;

/**
 * @author Niko Myller
 */
public class TheaterMCodeInterpreter extends MCodeInterpreter {

    //DOC: document!
    /**
     * 
     */
    protected AVInteractionEngine avInteractionEngine = null;

    /**
     * 
     */
    protected Stack stackOfExprsStacks = new Stack();

    /**
     * 
     */
    protected Stack arrayInitialization = new Stack();

    /**
     *  
     */
    protected Director director = null;

    /**
     *  
     */
    protected boolean invokingMethod = false;

    /**
     * Keeps track of current return value
     */
    protected boolean returned = false;

    /**
     *  
     */
    protected Value returnValue = null;

    /**
     *  
     */
    protected Actor returnActor = null;

    /**
     *  
     */
    protected long returnExpressionCounter = 0;

    /**
     *  
     */
    protected Stack commands = new Stack();

    /**
     *  
     */
    protected Stack exprs = new Stack();

    /**
     *  
     */
    protected Hashtable values = new Hashtable();

    /**
     *  
     */
    protected Hashtable variables = new Hashtable();

    /**
     *  
     */
    protected Hashtable instances = new Hashtable();

    /**
     *  
     */
    protected Stack methodInvocation = new Stack();

    /**
     *  
     */
    protected Hashtable postIncsDecs = new Hashtable();

    /**
     *  
     */
    protected ClassInfo currentClass = null;

    /**
     *  
     */
    protected Hashtable classes = new Hashtable();

    /**
     * currentMethodInvocation keeps track of all the information that is
     * collected during the method invocation.
     * Cells:
     * 0: Method name
     * 1: Class/Object expression
     * 2: Parameter values
     * 3: Parameter types
     * 4: Parameter names
     * 5: Highlight info for invocation
     * 6: Highlight info for declaration
     * 7: Parameter expression references
     * 8: Object reference if method is constructor or object method
     * 9: Class name (for super or this calls) or object value
     */
    protected Object[] currentMethodInvocation = null;

    /**
     *  
     */
    protected Stack objectCreation = new Stack();

    /**
     * Classes containing static variables. Type of the objects in the
     * LinkedList is jeliot.lang.Class
     * 
     * @see jeliot.lang.Class
     */
    protected LinkedList classesWithStaticVariables = new LinkedList();

    private boolean stopBeforeClearingScratch = false;

    /**
     *  
     */
    protected TheaterMCodeInterpreter() {
        super();
        initialize();
    }

    /**
     * @param r
     * @param d
     * @param programCode
     * @param pr
     */
    public TheaterMCodeInterpreter(BufferedReader r, Director d,
            String programCode, PrintWriter pr) {
        super(r);
        //this.mcode = r;
        this.director = d;
        this.programCode = programCode;
        this.input = pr;
        initialize();
    }

    /**
     * Initializes the
     */
    public void initialize() {
        running = true;
        start = true;
        returnActor = null;
        currentMethodInvocation = null;
        currentClass = null;
        classesWithStaticVariables = new LinkedList();
        classes = new Hashtable();
        commands = new Stack();
        exprs = new Stack();
        values = new Hashtable();
        variables = new Hashtable();
        methodInvocation = new Stack();
        returnValue = null;
        postIncsDecs = new Hashtable();
        instances = new Hashtable();
        classes = new Hashtable();
        objectCreation = new Stack();
        constructorCall = false;
        constructorCalls = new Stack();
        superMethods = null;
        superMethodsReading = null;
        superMethodCallNumber = 0;
        arrayInitialization = new Stack();
        //expressionStack = new Stack();
        avInteractionEngine = null;
        stopBeforeClearingScratch = false;

        super.initialize();

        try {
            line = readLine();
            MCodeUtilities.printlnToRegisteredSecondaryMCodeConnections(line);
            //This is for debugging purposes.
            DebugUtil.printDebugInfo(line);
        } catch (Exception e) {
            if (DebugUtil.DEBUGGING) {
                e.printStackTrace();
            }
        }

        if (line == null) {
            line = "" + Code.ERROR + Code.DELIM
                    + messageBundle.getString("unknown.exception") + Code.DELIM
                    + "0" + Code.LOC_DELIM + "0" + Code.LOC_DELIM + "0"
                    + Code.LOC_DELIM + "0";
        }

        StringTokenizer tokenizer = new StringTokenizer(line, Code.DELIM);

        if (Long.parseLong(tokenizer.nextToken()) == Code.ERROR) {
            String message = tokenizer.nextToken();
            Highlight h = MCodeUtilities.makeHighlight(tokenizer.nextToken());

            showErrorMessage(new InterpreterError(message, h));
            running = false;
        } else {
            firstLineRead = true;
        }
    }

    public String readLine() {
        String readLine = null;
        if (readNew()) {
            try {
                readLine = mcode.readLine();
            } catch (Exception e) {
            }
        } else {
            if (!superMethods.isEmpty()) {
                readLine = (String) superMethods.remove(0);
            } else {
                constructorCalls.pop();
                if (!constructorCalls.empty()) {
                    superMethods = (Vector) constructorCalls.peek();
                }
                return readLine();
            }
        }
        //System.out.println(values.size());
        if (readLine == null) {
            readLine = "" + Code.ERROR + Code.DELIM
                    + messageBundle.getString("unknown.exception") + Code.DELIM
                    + "0" + Code.LOC_DELIM + "0" + Code.LOC_DELIM + "0"
                    + Code.LOC_DELIM + "0";
        }
        DebugUtil.printDebugInfo(readLine);

        return readLine;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jeliot.mcode.MCodeInterpreter#beforeInterpretation(java.lang.String)
     */
    protected void beforeInterpretation(String line) {
        MCodeUtilities.printlnToRegisteredSecondaryMCodeConnections(line);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jeliot.mcode.MCodeInterpreter#showErrorMessage(jeliot.mcode.InterpreterError)
     */
    public void showErrorMessage(InterpreterError error) {
        director.showErrorMessage(error);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jeliot.mcode.MCodeInterpreter#openScratch()
     */
    public void openScratch() {
        director.openScratch();
    }

    /*
     * (non-Javadoc)
     * 
     * @see jeliot.mcode.MCodeInterpreter#closeScratch()
     */
    public void closeScratch() {
        director.closeScratch();
    }

    /*
     * (non-Javadoc)
     * 
     * @see jeliot.mcode.MCodeInterpreter#cleanEvaluationArea(int)
     */
    public void cleanEvaluationArea(int token) {
        if (exprs.empty() && !invokingMethod && arrayInitialization.empty()
                && token != Code.WHI && token != Code.FOR && token != Code.DO
                && token != Code.IFT && token != Code.IFTE
                && token != Code.SWIBF && token != Code.SWITCHB
                && token != Code.SWITCH && token != Code.VD
                && token != Code.OUTPUT && token != Code.INPUT
                && token != Code.INPUTTED && token != Code.OMC
                && token != Code.SMC && token != Code.SA) {
            //Hack to get a stop before return value is erased when it is not used.
            if (this.stopBeforeClearingScratch) {
                this.director.highlightForMessage(new Highlight(0, 0, 0, 0));
            }
            closeScratch();
            openScratch();
            checkInstancesForRemoval(false);
        }
        this.stopBeforeClearingScratch = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jeliot.mcode.MCodeInterpreter#endRunning()
     */
    protected void endRunning() {
        //if (ResourceBundles.getJeliotUserProperties().getBooleanProperty("CG")) {
        removeClasses();
        removeInstances();
        //}

        //TODO fix this hack!
        if (avInteractionEngine != null) {
            this.director.getTheatre().requestFocus();
            avInteractionEngine.showResults(director.getTheatre());
        }
    }

    /**
     * @param expressionCounter
     * @param leftExpressionReference
     * @param rightExpressionReference
     * @param value
     * @param type
     * @param h
     * @param operator
     */
    protected void handleCodeLQE(long expressionCounter,
            long leftExpressionReference, long rightExpressionReference,
            String value, String type, Highlight h) {
        handleBinaryExpression(expressionCounter, leftExpressionReference,
                rightExpressionReference, value, type, h, MCodeUtilities
                        .resolveBinOperator(Code.LQE));
    }

    /**
     * @param expressionCounter
     * @param leftExpressionReference
     * @param rightExpressionReference
     * @param value
     * @param type
     * @param h
     * @param operator
     */
    protected void handleCodeLE(long expressionCounter,
            long leftExpressionReference, long rightExpressionReference,
            String value, String type, Highlight h) {
        handleBinaryExpression(expressionCounter, leftExpressionReference,
                rightExpressionReference, value, type, h, MCodeUtilities
                        .resolveBinOperator(Code.LE));
    }

    /**
     * @param expressionCounter
     * @param leftExpressionReference
     * @param rightExpressionReference
     * @param value
     * @param type
     * @param h
     * @param operator
     */
    protected void handleCodeNE(long expressionCounter,
            long leftExpressionReference, long rightExpressionReference,
            String value, String type, Highlight h) {
        handleBinaryExpression(expressionCounter, leftExpressionReference,
                rightExpressionReference, value, type, h, MCodeUtilities
                        .resolveBinOperator(Code.NE));
    }

    /**
     * @param message
     * @param h
     */
    protected void handleCodeERROR(String message, Highlight h) {
        director.showErrorMessage(new InterpreterError(message, h));
        running = false;
    }

    /**
     * @param name
     * @param type
     * @param modifiers
     * @param value
     * @param h
     */
    protected void handleCodeFIELD(String name, String type, int modifiers,
            String value, String h) {
        if (value.equals(Code.UNKNOWN)) {
            value = MCodeUtilities.getDefaultValue(type);
        }
        currentClass.declareField(name, "" + modifiers + Code.DELIM + type
                + Code.DELIM + value + Code.DELIM + h);

        //We create static variable if it doesn't exist already
        /*
        if (Modifier.isStatic(modifiers) && name.indexOf("$") < 0) {
            //find class and add the static field
            String className = currentClass.getName();
            
            ListIterator li = classesWithStaticVariables.listIterator();
            jeliot.lang.Class ca = null;
            while (li.hasNext()) {
                jeliot.lang.Class c = (jeliot.lang.Class) li.next();
                if (c.getName().equals(className)) {
                    ca = c;
                    break;
                }
            }
            if (ca == null) {
                ca = new jeliot.lang.Class(className);
                classesWithStaticVariables.addLast(ca);
                director.showClassCreation(ca);
            }
            Variable v = null;

            try {
                v = ca.getVariable(name);
            } catch (Exception e) {
            }

            if (v == null) {
                Value val = new Value(value, type);
                Variable var = new Variable(name, type);
                var.assign(val);
                var.setModifierCodes(modifiers);
                ca.declareVariable(var);
                var.setLocationInCode(MCodeUtilities.makeHighlight(h));
                director.declareClassVariable(ca, var, val);
            }
        }
        */
    }

    /**
     * @param name
     * @param returnType
     * @param modifiers
     * @param listOfParameters
     */
    protected void handleCodeMETHOD(String name, String returnType,
            int modifiers, String listOfParameters) {
        currentClass.declareMethod(name + Code.DELIM + listOfParameters, ""
                + modifiers + Code.DELIM + returnType);
    }

    /**
     * @param listOfParameters
     */
    protected void handleCodeCONSTRUCTOR(String listOfParameters) {
        currentClass.declareConstructor(currentClass.getName() + Code.DELIM
                + listOfParameters, "");
    }

    /**
     *  
     */
    protected void handleCodeEND_CLASS() {
        if (currentClass != null) {
            classes.put(currentClass.getName(), currentClass);
        }
        currentClass = null;
    }

    /**
     * @param name
     * @param extendedClass
     */
    protected void handleCodeCLASS(String name, String extendedClass) {
        currentClass = new ClassInfo(name);
        ClassInfo ci = (ClassInfo) classes.get(extendedClass);

        //if extended class is user defined class
        if (ci != null) {
            currentClass.extendClass(ci);
        }
    }

    /**
     * @param expressionCounter
     * @param arrayCounter
     * @param name
     * @param value
     * @param type
     * @param highlight
     */

    protected void handleCodeAL(long expressionCounter, long arrayCounter,
            String name, String value, String type, Highlight highlight) {
        Reference ref = (Reference) values.remove(new Long(arrayCounter));
        ArrayInstance array = (ArrayInstance) ref.getInstance();

        Value length = new Value(value, type);
        director.introduceArrayLength(length, array);

        handleExpression(length, expressionCounter);
        exprs.pop();
    }

    /**
     * @param expressionCounter
     * @param expressionReference
     * @param dims
     * @param cellNumberReferences
     * @param cellNumbers
     * @param value
     * @param type
     * @param h
     */
    protected void handleCodeAAC(long expressionCounter,
            long expressionReference, int dims, String cellNumberReferences,
            String cellNumbers, String value, String type, Highlight h) {

        StringTokenizer st = new StringTokenizer(cellNumberReferences, ",");

        long[] cellNumberReference = new long[dims];

        for (int i = 0; st.hasMoreTokens(); i++) {
            cellNumberReference[i] = Long.parseLong(st.nextToken());
        }

        st = new StringTokenizer(cellNumbers, ",");
        int[] cellNumber = new int[dims];

        for (int i = 0; st.hasMoreTokens(); i++) {
            cellNumber[i] = Integer.parseInt(st.nextToken());
        }

        //Finding the VariableInArray
        Variable variable = (Variable) variables.remove(new Long(
                expressionReference));
        Reference varRef = null;
        if (variable != null) {
            varRef = (Reference) variable.getValue();
            values.remove(new Long(expressionReference));
        } else {
            varRef = (Reference) values.remove(new Long(expressionReference));
        }
        ArrayInstance ainst = (ArrayInstance) varRef.getInstance();
        int n = cellNumber.length;
        VariableInArray[] vars = new VariableInArray[n];
        for (int i = 0; i < n; i++) {
            vars[i] = ainst.getVariableAt(cellNumber[i]);
            if (i != n - 1) {
                ainst = (ArrayInstance) ((Reference) vars[i].getValue())
                        .getInstance();
            }
        }

        //Getting the right Values that point to the cell in
        // the array
        Value[] cellNumberValues = new Value[dims];
        for (int i = 0; i < dims; i++) {
            cellNumberValues[i] = (Value) values.remove(new Long(
                    cellNumberReference[i]));
        }

        //Actual value in the array in pointed cell
        Value val = null;
        if (MCodeUtilities.isPrimitive(type)) {
            val = new Value(value, type);
        } else {
            if (value.equals("null")) {
                val = new Reference(type);
            } else {
                Instance inst = (Instance) instances.get(MCodeUtilities
                        .getHashCode(value));
                if (inst != null) {
                    val = new Reference(inst);
                    ((Reference) val).makeReference();
                } else {
                    val = new Reference(type);
                }
            }
        }

        director.showArrayAccess(vars, cellNumberValues, val, h);

        exprs.pop();

        //command that waits for this expression
        int command = -1;
        int oper = -1;
        int size = commands.size();

        //We find the command
        for (int i = size - 1; i >= 0; i--) {
            StringTokenizer commandTokenizer = new StringTokenizer(
                    (String) commands.elementAt(i), Code.DELIM);
            int comm = Integer.parseInt(commandTokenizer.nextToken());
            long cid = Long.parseLong(commandTokenizer.nextToken());

            if (expressionCounter == cid) {
                command = comm;
                commands.removeElementAt(i);
                break;
            }
        }

        /*
         * Look from the expression stack what expression should be shown next
         */
        expressionReference = 0;
        Highlight highlight = null;
        if (!exprs.empty()) {
            StringTokenizer expressionTokenizer = new StringTokenizer(
                    (String) exprs.peek(), Code.DELIM);

            oper = Integer.parseInt(expressionTokenizer.nextToken());

            expressionReference = Long.parseLong(expressionTokenizer
                    .nextToken());

            //Make the location information for the location
            // token
            highlight = MCodeUtilities.makeHighlight(expressionTokenizer
                    .nextToken());
        }

        /*
         * Do different kind of things depending on in what expression the
         * variable is used.
         */

        //If operator is assignment we just store the value
        if (oper == Code.A) {
            if (command == Code.TO) {
                variables.put(new Long(expressionCounter), vars[n - 1]);
            } else {
                values.put(new Long(expressionCounter), val);
            }

            //If oper is other binary operator we will show it
            //on the screen with operator
        } else if (MCodeUtilities.isBinary(oper)) {

            int operator = MCodeUtilities.resolveBinOperator(oper);

            if (command == Code.LEFT) {
                //This is for compound assignments
                if (vars[n - 1] != null) {
                    variables.put(new Long(expressionCounter), vars[n - 1]);
                }

                director.beginBinaryExpression(val, operator,
                        expressionReference, highlight);

            } else if (command == Code.RIGHT) {

                ExpressionActor ea = director.getCurrentScratch().findActor(
                        expressionReference);
                if (ea != null) {

                    director.rightBinaryExpression(val, ea, highlight);

                } else {
                    values.put(new Long(expressionCounter), val);
                }
            } else {
                values.put(new Long(expressionCounter), val);
            }

            //If oper is a unary operator we will show it
            //on the screen with operator
        } else if (MCodeUtilities.isUnary(oper)) {

            if (oper == Code.PRIE || oper == Code.PRDE) {

                variables.put(new Long(expressionCounter), vars[n - 1]);
                values.put(new Long(expressionCounter), val);

            } else if (oper == Code.PIE || oper == Code.PDE) {

                if (exprs.size() > 1) {

                    StringTokenizer expressionTokenizer = new StringTokenizer(
                            (String) exprs.get(exprs.size() - 2), Code.DELIM);

                    int oper2 = Integer.parseInt(expressionTokenizer
                            .nextToken());
                    long expressionReference2 = Long
                            .parseLong(expressionTokenizer.nextToken());

                    if (MCodeUtilities.isBinary(oper2)) {
                        int command2 = 0;
                        int j = 0;
                        for (int i = commands.size() - 1; i >= 0; i--) {
                            StringTokenizer commandTokenizer = new StringTokenizer(
                                    (String) commands.elementAt(i), Code.DELIM);
                            int comm = Integer.parseInt(commandTokenizer
                                    .nextToken());
                            long cid = Long.parseLong(commandTokenizer
                                    .nextToken());

                            if (expressionReference == cid) {
                                command2 = comm;
                                j = i;
                                break;
                            }
                        }
                        if (command2 == Code.LEFT) {

                            //commands.removeElementAt(j);
                            int operator2 = MCodeUtilities
                                    .resolveBinOperator(oper2);
                            //This is for compound assignments
                            if (vars[n - 1] != null) {
                                variables.put(new Long(expressionCounter),
                                        vars[n - 1]);
                            }
                            director.beginBinaryExpression(val, operator2,
                                    expressionReference2, highlight);
                        }
                    }
                }

                variables.put(new Long(expressionCounter), vars[n - 1]);
                values.put(new Long(expressionReference), val);
                values.put(new Long(expressionCounter), val);

            } else {

                values.put(new Long(expressionCounter), val);
                int operator = MCodeUtilities.resolveUnOperator(oper);
                if (command == Code.RIGHT) {
                    director.beginUnaryExpression(operator, val,
                            expressionReference, highlight);
                }
            }

            //If it is something else we will store it for
            // later use.
        } else {

            values.put(new Long(expressionCounter), val);
            variables.put(new Long(expressionCounter), vars[n - 1]);

        }

        for (int i = 0; i < dims; i++) {
            Object[] postIncDec = (Object[]) postIncsDecs.remove(new Long(
                    cellNumberReference[i]));

            if (postIncDec != null) {
                doPostIncDec(postIncDec);
            }
        }
    }

    /**
     * @param expressionReference
     * @param hashCode
     * @param compType
     * @param dims
     * @param dimensionReferences
     * @param dimensionSizes
     * @param h
     */
    protected void handleCodeAA(long expressionReference, String hashCode,
            String compType, int dims, String dimensionReferences,
            String dimensionSizes, int actualDimension,
            String subArraysHashCodes, Highlight h) {

        //            String dimensionSizes, int actualDimension, Highlight h) {

        StringTokenizer st = new StringTokenizer(dimensionReferences,
                Code.LOC_DELIM);

        long[] dimensionReference = new long[dims];

        for (int i = 0; st.hasMoreTokens(); i++) {
            dimensionReference[i] = Long.parseLong(st.nextToken());
        }

        st = new StringTokenizer(dimensionSizes, Code.LOC_DELIM);
        int[] dimensionSize = new int[dims];

        for (int i = 0; st.hasMoreTokens(); i++) {
            dimensionSize[i] = Integer.parseInt(st.nextToken());
        }

        Value[] dimensionValues = new Value[dims];

        for (int i = 0; i < dims; i++) {
            dimensionValues[i] = (Value) values.remove(new Long(
                    dimensionReference[i]));

        }

        if (dimensionSize.length > 3) {
            throw new FeatureNotImplementedException(
                    "More than 3-dimensional arrays are not supported.");
        }

        ArrayInstance ai = new ArrayInstance(hashCode, compType,
                dimensionSize.length, actualDimension, dimensionSize[0]);

        Reference ref = new Reference(ai);

        //Handling of subArrayHascodes.
        // Sring format for a[2][2] is a1,a2,a11,a12,a21,a22 
        st = new StringTokenizer(subArraysHashCodes, Code.LOC_DELIM);
        Vector hashCodes = new Vector();

        while (st.hasMoreTokens()) {
            hashCodes.add(st.nextToken());
        }

        ArrayInstance[] level1 = null;
        Iterator itHashCodes = hashCodes.iterator();
        if (dimensionSize.length > 1) {
            level1 = new ArrayInstance[dimensionSize[0]];
            for (int i = 0; i < dimensionSize[0]; i++) {
                level1[i] = new ArrayInstance((String) itHashCodes.next(),
                        compType, dimensionSize.length - 1,
                        actualDimension - 1, dimensionSize[1]);
            }
        }

        ArrayInstance[][] level2 = null;
        if (dimensionSize.length > 2) {
            level2 = new ArrayInstance[dimensionSize[0]][dimensionSize[1]];
            for (int i = 0; i < dimensionSize[0]; i++) {
                for (int j = 0; j < dimensionSize[1]; j++) {
                    level2[i][j] = new ArrayInstance((String) itHashCodes
                            .next(), compType, dimensionSize.length - 2,
                            actualDimension - 2, dimensionSize[2]);
                }
            }
        }

        if (start && h.getBeginLine() < 2 && h.getEndLine() < 2) {
            h = null;
        }
        director.showArrayCreation(ai, ref, level1, level2, dimensionValues,
                expressionReference, actualDimension, h);

        //director.arrayCreation(dimensionSize, h);

        instances.put(hashCode, ai);
        //Now we also put the sub array hashcodes
        itHashCodes = hashCodes.iterator();
        if (dimensionSize.length > 1) {
            for (int i = 0; i < dimensionSize[0]; i++) {
                instances.put(itHashCodes.next(), level1[i]);
            }
        }
        if (dimensionSize.length > 2) {
            for (int i = 0; i < dimensionSize[0]; i++) {
                for (int j = 0; j < dimensionSize[1]; j++) {
                    instances.put(itHashCodes.next(), level2[i][j]);
                }
            }
        }

        ref.makeReference();

        values.put(new Long(expressionReference), ref);

        for (int i = 0; i < dims; i++) {
            Object[] postIncDec = (Object[]) postIncsDecs.remove(new Long(
                    dimensionReference[i]));

            if (postIncDec != null) {
                doPostIncDec(postIncDec);
            }
        }

        /*
         StringTokenizer st = new StringTokenizer(dimensionReferences, ",");

         long[] dimensionReference = new long[dims];

         for (int i = 0; st.hasMoreTokens(); i++) {
         dimensionReference[i] = Long.parseLong(st.nextToken());
         }

         //int values of the dimension sizes
         st = new StringTokenizer(dimensionSizes, ",");
         int[] dimensionSize = new int[dims];

         for (int i = 0; st.hasMoreTokens(); i++) {
         dimensionSize[i] = Integer.parseInt(st.nextToken());
         }

         Value[] dimensionValues = new Value[dims];

         for (int i = 0; i < dims; i++) {
         dimensionValues[i] = (Value) values.remove(new Long(
         dimensionReference[i]));

         }

         ArrayInstance ai = new ArrayInstance(hashCode, compType, dimensionSize);

         Reference ref = new Reference(ai);

         director.showArrayCreation(ai, ref, dimensionValues,
         expressionReference, h);

         //director.arrayCreation(dimensionSize, h);

         instances.put(hashCode, ai);

         ref.makeReference();

         values.put(new Long(expressionReference), ref);

         for (int i = 0; i < dims; i++) {
         Object[] postIncDec = (Object[]) postIncsDecs.remove(new Long(
         dimensionReference[i]));

         if (postIncDec != null) {
         doPostIncDec(postIncDec);
         }
         }
         */
    }

    /**
     * @param scope
     */
    protected void handleCodeSCOPE(int scope) {

        //Open the scope
        if (scope == 1) {

            director.openScope();
            director.closeScratch();
            director.openScratch();
            openNewExpressionStack();

            //Close the scope
        } else if (scope == 0) {

            director.closeScope();
            director.closeScratch();
            director.openScratch();
            closeExpressionStack();
        }
    }

    /**
     * @param expressionCounter
     * @param value
     * @param type
     * @param h
     */
    protected void handleCodeINPUTTED(long expressionCounter, String value,
            String type, Highlight h) {

        Value in = (Value) values.remove(new Long(expressionCounter));

        if (in == null) {
            in = new Value(value, type);
        }

        if (Util.visualizeStringsAsObjects()
                && MCodeUtilities.resolveType(in.getType()) == MCodeUtilities.STRING) {
            String[] values = MCodeUtilities.getStringValues(value);
            ((Reference) in).getInstance().setHashCode(values[1]);
            this.instances.remove(INPUT_STRING_HASHCODE);
            this.instances.put(((Reference) in).getInstance().getHashCode(),
                    ((Reference) in).getInstance());
        }

        handleExpression(in, expressionCounter);
    }

    /**
     * @param expressionCounter
     * @param type
     * @param h
     */
    protected void handleCodeINPUT(long expressionCounter, String className,
            String methodName, String type, String prompt, Highlight h) {
        Value in = director.animateInputHandling(type, prompt, h);

        if (Util.visualizeStringsAsObjects()
                && in instanceof Reference
                && MCodeUtilities.resolveType(in.getType()) == MCodeUtilities.STRING) {
            input.println(((StringInstance) ((Reference) in).getInstance())
                    .getStringValue().getValue());
        } else {
            input.println(in.getValue());
        }
        values.put(new Long(expressionCounter), in);
    }

    /**
     * @param expressionReference
     * @param value
     * @param type
     * @param breakLine
     * @param highlight
     */
    protected void handleCodeOUTPUT(long expressionReference, String className,
            String methodName, String value, String type, boolean breakLine,
            Highlight highlight) {
        Value output = (Value) values.remove(new Long(expressionReference));
        type = "java.lang.Object".equals(type) ? String.class.getName() : type;
        if (output == null) {
            if (Util.visualizeStringsAsObjects()
                    && MCodeUtilities.resolveType(type) == MCodeUtilities.STRING) {
                output = createStringReference(value, type);
                director.introduceLiteral(output, highlight);
            } else {
                output = new Value(value, type);
                director.introduceLiteral(output);
            }
        }

        if (Util.visualizeStringsAsObjects()
                && MCodeUtilities.resolveType(type) == MCodeUtilities.STRING) {
            output.setValue(MCodeUtilities.getValue(value, type));
        }

        if (breakLine) {
            output.setValue(output.getValue() + "\\n");
        }

        //Value output = new Value(value, type);

        director.output(output, highlight);

        // To pop the OUTPUT statement on top of the expression Stack if it is
        // there
        if (!exprs.empty()) {
            StringTokenizer expressionTokenizer = new StringTokenizer(
                    (String) exprs.peek(), Code.DELIM);
            if (Integer.parseInt(expressionTokenizer.nextToken()) == (Code.OUTPUT)) {
                exprs.pop();
            }
        }

        // To handle the post increments and decrements if there are any.
        Object[] postIncDec = (Object[]) postIncsDecs.remove(new Long(
                expressionReference));
        if (postIncDec != null) {
            doPostIncDec(postIncDec);
        }
    }

    /**
     * @param statementName
     * @param h
     */
    protected void handleCodeCONT(int statementName, Highlight h) {
        String stmt = "";

        if (statementName == Code.WHI) {
            stmt = propertiesBundle.getStringProperty("statement_name.while");
        } else if (statementName == Code.FOR) {
            stmt = propertiesBundle.getStringProperty("statement_name.for");
        } else if (statementName == Code.DO) {
            stmt = propertiesBundle
                    .getStringProperty("statement_name.do_while");
        }

        director.continueLoop(stmt, h);

        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param statementName
     * @param h
     */
    protected void handleCodeBR(int statementName, Highlight h) {
        String stmt = "";

        if (statementName == Code.WHI) {
            stmt = propertiesBundle.getStringProperty("statement_name.while");
            director.breakLoop(stmt, h);
        } else if (statementName == Code.FOR) {
            stmt = propertiesBundle.getStringProperty("statement_name.for");
            director.breakLoop(stmt, h);
        } else if (statementName == Code.DO) {
            stmt = propertiesBundle
                    .getStringProperty("statement_name.do_while");
            director.breakLoop(stmt, h);
        } else if (statementName == Code.SWITCH) {
            director.breakSwitch(h);
        }

        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param h
     */
    protected void handleCodeSWITCH(Highlight h) {
        director.closeSwitch(h);
        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param selectorReference
     * @param switchBlockReference
     * @param h
     */
    protected void handleCodeSWIBF(long selectorReference,
            long switchBlockReference, Highlight h) {
        if (switchBlockReference != -1) {
            Value selector = (Value) values.remove(new Long(selectorReference));
            Value switchBlock = (Value) values.remove(new Long(
                    switchBlockReference));
            Value result = new Value("true", "boolean");

            director.animateBinaryExpression(MCodeUtilities
                    .resolveBinOperator(Code.EE), selector, switchBlock,
                    result, -3, h);
            director.switchSelected(h);
        } else {
            director.switchDefault(h);
        }
    }

    /**
     * @param h
     */
    protected void handleCodeSWITCHB(Highlight h) {
        director.openSwitch(h);
        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param expressionReference
     * @param value
     * @param round
     * @param h
     */
    protected void handleCodeDO(long expressionReference, String value,
            long round, Highlight h) {
        Value result = (Value) values.remove(new Long(expressionReference));

        if (round == 0) {
            director.enterLoop(propertiesBundle
                    .getStringProperty("statement_name.do_while"), h);
        } else {
            if (value.equals(Boolean.TRUE.toString())) {
                director.continueLoop(propertiesBundle
                        .getStringProperty("statement_name.do_while"), result,
                        h);
            } else {
                director.exitLoop(propertiesBundle
                        .getStringProperty("statement_name.do_while"), result);
            }
        }
        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param expressionReference
     * @param value
     * @param round
     * @param h
     */
    protected void handleCodeFOR(long expressionReference, String value,
            long round, Highlight h) {
        Value result = (Value) values.remove(new Long(expressionReference));

        if (round == 0) {
            if (value.equals(Boolean.TRUE.toString())) {
                director.enterLoop(propertiesBundle
                        .getStringProperty("statement_name.for"), result, h);
            } else {
                director.skipLoop(propertiesBundle
                        .getStringProperty("statement_name.for"), result);
            }
        } else {
            if (value.equals(Boolean.TRUE.toString())) {
                director.continueLoop(propertiesBundle
                        .getStringProperty("statement_name.for"), result, h);
            } else {
                director.exitLoop(propertiesBundle
                        .getStringProperty("statement_name.for"), result);
            }
        }
        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param expressionReference
     * @param value
     * @param round
     * @param h
     */
    protected void handleCodeWHI(long expressionReference, String value,
            int round, Highlight h) {

        Value result = (Value) values.remove(new Long(expressionReference));

        if (round == 0) {

            if (value.equals(Boolean.TRUE.toString())) {
                director.enterLoop(propertiesBundle
                        .getStringProperty("statement_name.while"), result, h);
            } else {
                director.skipLoop(propertiesBundle
                        .getStringProperty("statement_name.while"), result);
            }

        } else {

            if (value.equals(Boolean.TRUE.toString())) {
                director.continueLoop(propertiesBundle
                        .getStringProperty("statement_name.while"), result, h);
            } else {
                director.exitLoop(propertiesBundle
                        .getStringProperty("statement_name.while"), result);
            }

        }

        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param expressionReference
     * @param value
     * @param h
     */
    protected void handleCodeIFTE(long expressionReference, String value,
            Highlight h) {

        Value result = (Value) values.remove(new Long(expressionReference));

        if (value.equals(Boolean.TRUE.toString())) {
            director.branchThen(result, h);
        } else {
            director.branchElse(result, h);
        }

        director.closeScratch();
        director.openScratch();
    }

    /**
     * @param expressionReference
     * @param value
     * @param h
     */
    protected void handleCodeIFT(long expressionReference, String value,
            Highlight h) {
        Value result = (Value) values.remove(new Long(expressionReference));

        if (value.equals(Boolean.TRUE.toString())) {
            director.branchThen(result, h);
        } else {
            director.skipIf(result, h);
        }

        director.closeScratch();
        director.openScratch();
    }

    /**
     *  
     */
    protected void handleCodeSMCC() {
        if (!returned) {

            director.finishMethod(null, 0);
            closeExpressionStack();

        } else {

            Value rv = null;

            if (returnValue instanceof Reference) {
                rv = (Value) ((Reference) returnValue).clone();
                //((Reference) rv).makeReference();
            } else {
                rv = (Value) returnValue.clone();
            }

            ValueActor va = director.finishMethod(returnActor,
                    returnExpressionCounter);
            closeExpressionStack();
            rv.setActor(va);
            handleExpression(rv, returnExpressionCounter);
        }

        returned = false;

        if (currentMethodInvocation != null) {
            invokingMethod = true;
        }

    }

    /**
     * @param expressionCounter
     * @param expressionReference
     * @param value
     * @param type
     * @param h
     */
    protected void handleCodeR(long expressionCounter,
            long expressionReference, String value, String type, Highlight h) {
        if (type.equals(Void.TYPE.getName()) || type.equals("void")) {

            //director.finishMethod(null, expressionCounter);
            returned = false;

        } else {

            Value ret = (Value) values.remove(new Long(expressionReference));

            Value casted = null;

            if (MCodeUtilities.isPrimitive(type)) {
                casted = new Value(value, type);
            } else {
                Instance inst = null;
                if (Util.visualizeStringsAsObjects()
                        && MCodeUtilities.resolveType(type) == MCodeUtilities.STRING) {
                    String[] strs = MCodeUtilities.getStringValues(value);
                    if (strs != null) {
                        inst = (Instance) instances.get(MCodeUtilities
                                .getHashCode(strs[1]));
                    }
                }
                if (inst == null) {
                    inst = (Instance) instances.get(MCodeUtilities
                            .getHashCode(value));
                }
                if (inst != null) {
                    casted = new Reference(inst);
                    ((Reference) casted).makeReference();
                } else {
                    casted = new Reference(type);
                }
            }

            returnActor = director.animateReturn(ret, casted, h);
            if (casted instanceof Reference) {
                returnValue = (Value) ((Reference) casted).clone();
                ((Reference) returnValue).makeReference();
            } else {
                returnValue = (Value) casted.clone();
            }
            returnExpressionCounter = expressionCounter;
            returned = true;
        }
        exprs.pop();
    }

    /**
     * @param parameters
     */
    protected void handleCodePARAMETERS(String parameters) {
        if (currentMethodInvocation != null) {
            String[] parameterNames = (String[]) currentMethodInvocation[4];
            StringTokenizer names = new StringTokenizer(parameters, ",");
            for (int i = 0; i < parameterNames.length; i++) {
                parameterNames[i] = names.nextToken();
            }
        }
    }

    /**
     * @param h
     */
    protected void handleCodeMD(Highlight h) {
        if (currentMethodInvocation != null) {

            //Make the location information for the location token
            currentMethodInvocation[6] = h;

            //Object method call or constructor
            if (currentMethodInvocation.length >= 9) {

                if (start && "main".equals(currentMethodInvocation[0])) {
                    start = false;
                    currentMethodInvocation[5] = null;
                }

                Value[] args = null;

                if (currentMethodInvocation[1] != null
                        && ((String) currentMethodInvocation[1]).equals("")) {
                    //System.out.println("CI: " + "new " +
                    //((String) currentMethodInvocation[0]));
                    args = director.animateConstructorInvocation(
                            (String) currentMethodInvocation[0],
                            (Value[]) currentMethodInvocation[2],
                            (Highlight) currentMethodInvocation[5]);

                    //This works for non-primitive classes.
                    //There needs to be a check whether invoked
                    //class is primitive or not.
                    if (!MCodeUtilities
                            .isPrimitive(((ClassInfo) currentMethodInvocation[8])
                                    .getName())) {
                        ObjectFrame of = createNewInstance(
                                (ClassInfo) currentMethodInvocation[8],
                                (Highlight) currentMethodInvocation[5]);

                        Reference ref = new Reference(of);
                        ref.makeReference();
                        currentMethodInvocation[8] = ref;
                        objectCreation.push(new Reference(of));
                    } else {
                        //TODO: Make the constructor call work for
                        // semi-primitive types (wrappers and String)
                    }

                } else {
                    //System.out.println("OMI: " + "." + ((String)
                    // currentMethodInvocation[0]));
                    args = director.animateOMInvocation("."
                            + ((String) currentMethodInvocation[0]),
                            (Value[]) currentMethodInvocation[2],
                            (Highlight) currentMethodInvocation[5],
                            (Value) currentMethodInvocation[8]);
                }

                String call;

                if (currentMethodInvocation[1] != null
                        && ((String) currentMethodInvocation[1]).equals("")) {
                    call = (String) currentMethodInvocation[0];
                } else if (((String) currentMethodInvocation[0])
                        .equals("super")
                        || ((String) currentMethodInvocation[0]).equals("this")) {
                    call = /*"this." + */(String) currentMethodInvocation[9];
                } else if (currentMethodInvocation[1] == null) {
                    call = ((Value) currentMethodInvocation[8]).getValue()
                            + "." + (String) currentMethodInvocation[0];
                } else {
                    call = (String) currentMethodInvocation[1] + "."
                            + (String) currentMethodInvocation[0];
                    //System.out.println("METHOD: " + call);
                }

                director.setUpMethod(call, args,
                        (String[]) currentMethodInvocation[4],
                        (String[]) currentMethodInvocation[3],
                        (Highlight) currentMethodInvocation[6],
                        (Value) currentMethodInvocation[8]);

                //Static method invocation
            } else {
                Value[] args = null;
                if (start) {
                    args = director.animateSMInvocation(
                            ((String) currentMethodInvocation[1]) + "."
                                    + ((String) currentMethodInvocation[0]),
                            (Value[]) currentMethodInvocation[2], null);
                    start = false;
                } else {
                    args = director.animateSMInvocation(
                            ((String) currentMethodInvocation[1]) + "."
                                    + ((String) currentMethodInvocation[0]),
                            (Value[]) currentMethodInvocation[2],
                            (Highlight) currentMethodInvocation[5]);
                }

                director.setUpMethod(((String) currentMethodInvocation[1])
                        + "." + ((String) currentMethodInvocation[0]), args,
                        (String[]) currentMethodInvocation[4],
                        (String[]) currentMethodInvocation[3],
                        (Highlight) currentMethodInvocation[6]);
            }

            Long[] parameterExpressionReferences = (Long[]) currentMethodInvocation[7];

            if (parameterExpressionReferences != null) {
                int i = 0;
                while (i < parameterExpressionReferences.length) {
                    Object[] postIncDec = (Object[]) postIncsDecs
                            .remove(parameterExpressionReferences[i]);
                    if (postIncDec != null) {
                        doPostIncDec(postIncDec);
                    }
                    i++;
                }
            }

            if (!methodInvocation.empty()) {
                currentMethodInvocation = (Object[]) methodInvocation.pop();
            } else {
                currentMethodInvocation = null;
            }

            invokingMethod = false;

            openNewExpressionStack();
        }
    }

    /**
     * @param expressionReference
     * @param argType
     */
    protected void handleCodeP(long expressionReference, String value,
            String argType) {

        Value[] parameterValues = (Value[]) currentMethodInvocation[2];
        String[] parameterTypes = (String[]) currentMethodInvocation[3];
        Long[] parameterExpressionReferences = (Long[]) currentMethodInvocation[7];

        Value parameterValue = (Value) values.remove(new Long(
                expressionReference));
        Value castedParameterValue = null;
        if (MCodeUtilities.isPrimitive(parameterValue.getType())) {
            if (MCodeUtilities.resolveType(parameterValue.getType()) != MCodeUtilities
                    .resolveType(argType)) {
                castedParameterValue = new Value(value, argType);
                director.animateCastExpression(parameterValue,
                        castedParameterValue);
            }
        }

        //if (parameterValue == null) {
        //  System.out.println("Mistake");
        //}

        int i = 0;
        while (parameterValues[i] != null) {
            i++;
        }
        if (castedParameterValue != null) {
            parameterValues[i] = castedParameterValue;
        } else {
            parameterValues[i] = parameterValue;
        }
        parameterTypes[i] = argType;
        parameterExpressionReferences[i] = new Long(expressionReference);

        exprs.pop();
    }

    /**
     * @param methodName
     * @param className
     * @param parameterCount
     * @param h
     */
    protected void handleCodeSMC(String methodName, String className,
            int parameterCount, Highlight h) {

        invokingMethod = true;

        if (currentMethodInvocation != null) {
            methodInvocation.push(currentMethodInvocation);
        }
        currentMethodInvocation = new Object[8];

        for (int i = 0; i < currentMethodInvocation.length; i++) {
            currentMethodInvocation[i] = null;
        }

        currentMethodInvocation[0] = methodName;
        currentMethodInvocation[1] = className;

        Value[] parameterValues = new Value[parameterCount];
        String[] parameterTypes = new String[parameterCount];
        String[] parameterNames = new String[parameterCount];
        Long[] parameterExpressionReferences = new Long[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            parameterValues[i] = null;
            parameterTypes[i] = null;
            parameterNames[i] = null;
            parameterExpressionReferences[i] = null;
        }

        currentMethodInvocation[2] = parameterValues;
        currentMethodInvocation[3] = parameterTypes;
        currentMethodInvocation[4] = parameterNames;
        currentMethodInvocation[5] = h;
        currentMethodInvocation[7] = parameterExpressionReferences;
    }

    /**
     *  
     */
    protected void handleCodeOMCC() {
        if (!returned) {
            director.finishMethod(null, 0);
            closeExpressionStack();
        } else {
            Value rv = null;

            if (returnValue instanceof Reference) {
                rv = (Value) ((Reference) returnValue).clone();
                ((Reference) rv).makeReference();
            } else {
                rv = (Value) returnValue.clone();
            }

            ValueActor va = director.finishMethod(returnActor,
                    returnExpressionCounter);
            closeExpressionStack();
            rv.setActor(va);
            handleExpression(rv, returnExpressionCounter);
        }
        if (currentMethodInvocation != null) {
            invokingMethod = true;
        }
        returned = false;
    }

    /**
     * @param methodName
     * @param parameterCount
     * @param objectCounter
     * @param highlight
     */
    protected void handleCodeOMC(String methodName, int parameterCount,
            long objectCounter, String objectValueOrClassName,
            Highlight highlight) {

        //See EvaluationVisitor.visit(SuperMethodCall node) for this hack.
        //TODO: This should be changed to separate call in next versions!
        int index = methodName.indexOf(",");
        boolean superMethod = false;
        if (index >= 0) {
            methodName = methodName.substring(index + 1);
            superMethod = true;
        }

        Value val = (Value) values.remove(new Long(objectCounter));
        Variable var = (Variable) variables.remove(new Long(objectCounter));

        boolean objCreationInProgress = false;
        if (val == null && !objectCreation.empty()) {
            val = (Reference) objectCreation.peek();
            objCreationInProgress = true;
        } else if (val != null && val.getValue().equals("null")
                && !objectCreation.empty()) {
            val = (Reference) objectCreation.peek();
            objCreationInProgress = true;
        }

        // fixed by rku: check if instance is really an ObjectFrame
        // e.g. char [] c = new char[2]; S.o.p (c); (failed here with type cast exception)
        if (val instanceof Reference
                && ((Reference) val).getInstance() instanceof ObjectFrame) {
            ObjectFrame obj = (ObjectFrame) ((Reference) val).getInstance();

            if (obj == null && !objectCreation.empty()) {
                val = (Reference) objectCreation.peek();
                obj = (ObjectFrame) ((Reference) val).getInstance();
                objCreationInProgress = true;
            }
        }
        if (val instanceof Reference) {
            ((Reference) val).makeReference();
        }
        invokingMethod = true;

        if (currentMethodInvocation != null) {
            methodInvocation.push(currentMethodInvocation);
        }
        currentMethodInvocation = new Object[10];

        /*
         * int n = currentMethodInvocation.length;
         *
         * for (int i = 0; i < n; i++) { currentMethodInvocation[i] = null; }
         */

        currentMethodInvocation[0] = methodName;

        if (superMethod) {
            currentMethodInvocation[1] = "super";
        } else if (var != null) {
            currentMethodInvocation[1] = var.getName();
        } else {
            if (val instanceof Reference) {
                if (objCreationInProgress) {
                    currentMethodInvocation[1] = "new "
                            + ((Reference) val).getInstance().getType();
                } else {
                    currentMethodInvocation[1] = ((Reference) val)
                            .getInstance().getType();
                }
            } else {
                currentMethodInvocation[1] = val.getValue();
            }
        }

        Value[] parameterValues = new Value[parameterCount];
        String[] parameterTypes = new String[parameterCount];
        String[] parameterNames = new String[parameterCount];
        Long[] parameterExpressionReferences = new Long[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            parameterValues[i] = null;
            parameterTypes[i] = null;
            parameterNames[i] = null;
            parameterExpressionReferences[i] = null;
        }

        currentMethodInvocation[2] = parameterValues;
        currentMethodInvocation[3] = parameterTypes;
        currentMethodInvocation[4] = parameterNames;
        currentMethodInvocation[5] = highlight;
        currentMethodInvocation[7] = parameterExpressionReferences;
        currentMethodInvocation[8] = val;
        currentMethodInvocation[9] = objectValueOrClassName;
    }

    /**
     * @param expressionCounter
     * @param objectCounter
     * @param variableName
     * @param value
     * @param type
     * @param modifiers
     * @param h
     */
    protected void handleCodeOFA(long expressionCounter, long objectCounter,
            String variableName, String value, String type, int modifiers,
            Highlight h) {

        Reference objVal = (Reference) values.remove(new Long(objectCounter));

        if (objVal == null && !objectCreation.empty()) {
            objVal = (Reference) objectCreation.peek();
        }

        ObjectFrame obj = (ObjectFrame) objVal.getInstance();

        /*
         * This is here because if the object is just under creation
         * (i.e. were are currently in constructor) it is possiple
         * there the reference in values don't have any instance as
         * it's reference point and then the ObjectFrame obj is null
         * after previous line.
         */
        if (obj == null && !objectCreation.empty()) {
            objVal = (Reference) objectCreation.peek();
            obj = (ObjectFrame) objVal.getInstance();
        }

        Variable var = null;
        if (obj != null) {
            var = obj.getVariable(variableName);
        }
        /*
         * This is a static field access if there isn't such variable in the object.
         * If the code is like this:
         * class Ware {
         *   int i = 0;
         *   static int z = 0;
         *   Ware (int i) {
         *       this.i  = i;
         *       z++;                
         *   }           
         * }
         * access to z in (z++) is a object field access in Dynamic Java and thus
         * it is necessary to transfer those actions to static field accesses in
         * Jeliot side.    
         */
        if (var == null) {
            String classType = "";
            if (obj != null) {
                classType = obj.getType();
            } else {
                classType = objVal.getType();
            }
            handleCodeSFA(expressionCounter, classType, variableName, value,
                    type, modifiers, h);

            /* Different version of this procedure.
             String className = obj.getType();
             boolean notFound = true;
             while (notFound) {
             try {
             handleCodeSFA(expressionCounter, className, variableName, value,
             type, h);
             notFound = false;
             } catch (StaticVariableNotFoundException e) {
             notFound = true;
             ClassInfo ci = (ClassInfo) classes.get(className);
             if (ci != null) {
             className = ci.getExtendedClassName();
             } else {
             throw new RuntimeException(e);
             }
             }
             }
             */
            return;
        }

        //command that waits for this expression
        int command = -1;
        int oper = -1;
        int size = commands.size();

        //We find the command
        for (int i = size - 1; i >= 0; i--) {
            StringTokenizer commandTokenizer = new StringTokenizer(
                    (String) commands.elementAt(i), Code.DELIM);
            int comm = Integer.parseInt(commandTokenizer.nextToken());
            long cid = Long.parseLong(commandTokenizer.nextToken());

            if (expressionCounter == cid) {
                command = comm;
                commands.removeElementAt(i);
                break;
            }
        }

        /*
         * Look from the expression stack what expression should be shown next
         */
        long expressionReference = 0;
        Highlight highlight = null;
        if (!exprs.empty()) {
            StringTokenizer expressionTokenizer = new StringTokenizer(
                    (String) exprs.peek(), Code.DELIM);

            oper = Integer.parseInt(expressionTokenizer.nextToken());

            expressionReference = Long.parseLong(expressionTokenizer
                    .nextToken());

            //Make the location information for the location
            // token
            highlight = MCodeUtilities.makeHighlight(expressionTokenizer
                    .nextToken());
        }

        Value val = null;
        if (MCodeUtilities.isPrimitive(type)) {
            val = new Value(value, type);
            ValueActor va = var.getActor().getValue();
            val.setActor(va);
        } else {
            if (value.equals("null")) {
                val = new Reference(type);
            } else {
                Instance inst = (Instance) instances.get(MCodeUtilities
                        .getHashCode(value));
                if (inst != null) {
                    val = new Reference(inst);
                } else {
                    val = new Reference(type);
                }
            }
            val.setActor(var.getActor().getValue());
            variables.put(new Long(expressionCounter), var);
        }

        /*
         * Do different kind of things depending on in what expression the
         * variable is used.
         */

        //If operator is assignment we just store the value
        if (oper == Code.A) {

            if (command == Code.TO) {

                variables.put(new Long(expressionCounter), var);

            } else {

                values.put(new Long(expressionCounter), val);
            }

            //If oper is other binary operator we will show it
            //on the screen with operator
        } else if (MCodeUtilities.isBinary(oper)) {

            int operator = MCodeUtilities.resolveBinOperator(oper);

            if (command == Code.LEFT) {
                //This is for compound assignments
                if (var != null) {
                    variables.put(new Long(expressionCounter), var);
                }

                director.beginBinaryExpression(val, operator,
                        expressionReference, highlight);

            } else if (command == Code.RIGHT) {

                ExpressionActor ea = director.getCurrentScratch().findActor(
                        expressionReference);
                if (ea != null) {

                    director.rightBinaryExpression(val, ea, highlight);

                } else {
                    values.put(new Long(expressionCounter), val);
                }
            } else {
                values.put(new Long(expressionCounter), val);
            }

            //If oper is a unary operator we will show it
            //on the screen with operator
        } else if (MCodeUtilities.isUnary(oper)) {

            if (oper == Code.PRIE || oper == Code.PRDE) {

                variables.put(new Long(expressionCounter), var);
                values.put(new Long(expressionCounter), val);

            } else if (oper == Code.PIE || oper == Code.PDE) {
                if (exprs.size() > 1) {
                    StringTokenizer expressionTokenizer = new StringTokenizer(
                            (String) exprs.get(exprs.size() - 2), Code.DELIM);

                    int oper2 = Integer.parseInt(expressionTokenizer
                            .nextToken());
                    long expressionReference2 = Long
                            .parseLong(expressionTokenizer.nextToken());

                    if (MCodeUtilities.isBinary(oper2)) {
                        int command2 = 0;
                        int j = 0;
                        for (int i = commands.size() - 1; i >= 0; i--) {
                            StringTokenizer commandTokenizer = new StringTokenizer(
                                    (String) commands.elementAt(i), Code.DELIM);
                            int comm = Integer.parseInt(commandTokenizer
                                    .nextToken());
                            long cid = Long.parseLong(commandTokenizer
                                    .nextToken());

                            if (expressionRe
