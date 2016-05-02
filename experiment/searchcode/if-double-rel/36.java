package lrg.memoria.core;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * User: mihai
 * Date: Apr 27, 2005
 * Time: 6:20:09 PM
 */
public class CodeStripe extends ModelElement implements Scopable, Scope {
    // You ca do anything with this
    public static final int NONSTATIC_STRIPE = 0;

    // You can't remove content from this Stripe, but you may be
    // able to move it all together (change it's scope)
    public static final int STATIC_CONTENT = 1;

    // You can't change this Stripe's Scope (move it somwhere else) but you may
    // be able remove it's content
    public static final int STATIC_SCOPE = 2;

    // You can't touch this stripe
    public static final int STATIC_STRIPE = STATIC_CONTENT + STATIC_SCOPE;

    // the standard stripe names
    private static final String EMPTY_NAME = "[empty_stripe]";
    public static final String TEMP = "[temp_code]";
    public static final String NEW = "[new_stmnt]";
    public static final String STATEMENT_BLOCK = "[stmnt_block]";
    public static final String SWITCH = "[switch_code]";
    public static final String CASE = "[case_code]";
    public static final String DEFAULT = "[default_case_code]";
    public static final String FOR = "[for_code]";
    public static final String WHILE = "[while_code]";
    public static final String DO = "[do_while_code]";
    public static final String IF = "[if_code]";
    public static final String ELSE = "[else_code]";
    public static final String TRY = "[try_code]";
    public static final String CATCH = "[catch_code]";
    public static final String FINALLY = "[finally_code]";

    // the default empty stripe
    //public static final CodeStripe EMPTY=new CodeStripe();

    // the stuff this Stripe contains
    private ModelElementList<CodeStripe> stripes;
    private ModelElementList<Call> calls;
    private ModelElementList<Access> accesses;
    private ModelElementList<LocalVariable> localVariables;
    private ModelElementList<Type> innerTypes;

    // the uncomputable metrics
    private int linesOfComment = 0;
    private int numberOfStatements = 0;
    private int bodyExits = 0;
    private int atomicCyclo = 0;

    // some other properties
    private String signature = EMPTY_NAME; // the signature of this Stripe
    private int access = STATIC_STRIPE;
    private Location contentLocation = Location.getUnknownLocation();
    private Location location = Location.getUnknownLocation();
    private Scope scope;
    private String[] source;


    public CodeStripe(Scope scope) {
        this();
        if ((scope instanceof Body) == false)
            scope.addScopedElement(this);
        this.scope = scope;
    }

    private CodeStripe() {
        super();
        accesses = new ModelElementList<Access>();
        calls = new ModelElementList<Call>();
        stripes = new ModelElementList<CodeStripe>();
        localVariables = new ModelElementList<LocalVariable>();
        innerTypes = new ModelElementList<Type>();
        source = null;
        scope = null;
    }

    public void addScopedElement(Scopable element) {
        if (element instanceof CodeStripe)
            stripes.add((CodeStripe) element);

        if (element instanceof Call)
            calls.add((Call) element);

        if (element instanceof Access)
            accesses.add((Access) element);

        if (element instanceof LocalVariable)
            localVariables.add((LocalVariable) element);

        if (element instanceof Type)
            innerTypes.add((Type) element);
    }

    public ModelElementList getScopedElements() {
        ModelElementList scoped = new ModelElementList();
        scoped.addAll(stripes);
        scoped.addAll(localVariables);
        scoped.addAll(innerTypes);
        return scoped;
    }

    public ModelElementList<CodeStripe> getScopedStripes() {
        return stripes;
    }

    /**
     * The list of call-objects, representing the calls that occur in the implementation of
     * this code stripe.
     */
    public ModelElementList<Call> getCallList() {
        return calls;
    }

    /**
     * Adds a call-object, representing a call that occurs in the implementation of this code stripe.
     */
    public void addCall(Call c) {
        calls.add(c);
    }

    /**
     * The list of accesed-objects, representing the variable accesses that
     * occur in the implementation of that code stripe.
     */
    public ModelElementList<Access> getAccessList() {
        return accesses;
    }

    /**
     * Adds an access-object, representing a variable access that
     * occur in the implementation of that code stripe.
     */
    public void addAccess(Access c) {
        accesses.add(c);
    }

    /**
     * The list of local variables declared in this body.
     */
    public ModelElementList<LocalVariable> getLocalVarList() {
        return localVariables;
    }

    /**
     * Adds a local variable declared in this body.
     */
    public void addLocalVar(LocalVariable var) {
        localVariables.add(var);
    }

    /**
     * The list of inner types declared in this code stripe.
     */
    public ModelElementList<Type> getInnerTypesList() {
        return innerTypes;
    }

    /**
     * Adds an inner type declared in this stripe of code.
     */
    public void addInnerType(Type type) {
        innerTypes.add(type);
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getName() {
        return signature;
    }

    public boolean isEmpty() {
        return EMPTY_NAME.equals(signature);
    }

    public String getFullName() {
        return getScope().getFullName() + "." + signature;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    // WARNING! this is NOT the line/column location in the file, it is a _relative_
    // location to the contentLocation of this stripe's scope (if the scope is a body
    // then it is relative to that)
    // IF you want to find the actual location of this piece of code you would have to
    // iterate up through th scopes adding locations as you go until you reach a scope
    // that is not a Stripe object

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    // WARNING! this is NOT the line/column location in the file, it is a _relative_
    // location to the start location of this stripe
    public Location getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(Location contentLocation) {
        this.contentLocation = contentLocation;
    }


    /**
     * @param absLine the line in the source file
     * @param absCol  the column in the source file
     * @return a location with only startLine & startColumn set to the position
     *         relative to the start of this stripe
     */
    public Location getRelPosOf(int absLine, int absCol) {
        if (getScope() instanceof CodeStripe) {
            Location l = ((CodeStripe) getScope()).getRelPosOf(absLine, absCol);
            return getLocation().relativeOf(l.getStartLine(), l.getStartChar());
        } else {
            return getLocation().relativeOf(absLine, absCol);
        }
    }

    /**
     * @param abs - the location in the source file
     * @return the location relative to this stripe location
     */
    public Location getRelPosOf(Location abs) {
        if (getScope() instanceof CodeStripe) {
            Location l = ((CodeStripe) getScope()).getRelPosOf(abs);
            return getLocation().relativeOf(l);
        } else {
            return getLocation().relativeOf(abs);
        }
    }

    /**
     * @param rel - the location relative to this stripe location
     * @return the location in the source file
     */
    public Location getAbsPosOf(Location rel) {
        if (getScope() instanceof CodeStripe) {
            Location l = ((CodeStripe) getScope()).getAbsPosOf(rel);
            return getLocation().absoluteOf(l);
        } else {
            return getLocation().absoluteOf(rel);
        }
    }

    public boolean hasStaticScope() {
        return ((access & STATIC_SCOPE) != 0);
    }

    public boolean hasStaticContent() {
        return ((access & STATIC_CONTENT) != 0);
    }

    private static boolean checkAndAdd(Location toCheck, Location objLoc, ArrayList<Location> list) {
        if (objLoc.startsAfter(toCheck.getStartLine(), toCheck.getStartChar()))
            list.add(objLoc);
        else if (objLoc.contains(toCheck)) return true;
        return false;
    }

    /**
     * what's content is absorbed into this, what LOOSES much of its contents
     *
     * @param startLine - line where to put _what_ in _this_
     * @param startCol  - column where to put _what_ in _this_
     * @param what      - to be absorbed in this
     * @return - 0 if all was well
     *         non zero if something went wrong
     */
    public int absorbStripe(int startLine, int startCol, CodeStripe what) {
        // startLine,startCol must not be in some _bad_ location

        Location rel = location.relativeOf(startLine, startCol);
        rel.setEndLine(rel.getStartLine());
        rel.setEndChar(rel.getStartChar());
        if (contentLocation.includes(rel) == false) return -1;

        ArrayList<Location> shifted = new ArrayList<Location>();

        for (Access a : accesses)
            for (Location l : a.getInstanceList())
                if (checkAndAdd(rel, l, shifted)) return -1;

        for (Call c : calls)
            for (Location l : c.getInstanceList())
                if (checkAndAdd(rel, l, shifted)) return -1;

        for (LocalVariable lv : localVariables) {
            if (checkAndAdd(rel, lv.getLocation(), shifted)) return -1;
        }

        for (Type t : innerTypes) {
            if (checkAndAdd(rel, ((ExplicitlyDefinedType) t).getLocation(), shifted)) return -1;
        }


        CodeStripe delegated = null;
        // is the location within some of my children?
        for (CodeStripe cs : stripes)
            if (checkAndAdd(rel, cs.getLocation(), shifted)) {
                if (delegated != null)
                    throw new RuntimeException("Some CodeStripes are overlapping! The model might be corrupted!");
                else
                    delegated = cs;
            }

        if (delegated != null) // yes it is!
            return delegated.absorbStripe(rel.getStartLine(), rel.getStartChar(), what);
        // no it's not :(

        // shift objects:
        // bla; bla1;
        // becomes
        // bla;
        //
        //      bla1;
        int lineCount = what.location.getEndLine() - what.location.getStartLine();
        Location.shiftLocations(lineCount + 2, shifted);

        // change source
        String newSource[] = new String[source.length + lineCount + 2];

        int i;

        for (i = 0; i < rel.getStartLine(); i++)
            newSource[i] = source[i];

        newSource[i] = blankChars(source[i], rel.getStartChar(), source[i].length() - 1, ' ');
        i++;

        for (; i <= (rel.getStartLine() + 1 + lineCount); i++)
            newSource[i] = what.source[i - rel.getStartLine() - 1];

        newSource[i] = blankChars(source[i - lineCount - 2], 0, rel.getStartChar() - 1, ' ');
        i++;

        for (; i < newSource.length; i++)
            newSource[i] = source[i - lineCount - 2];

        source = newSource;
        rel.setStartLine(rel.getStartLine() + 1);

        for (Access a : what.accesses) {
            Access x = null;
            for (Access a1 : accesses)
                if (a1.getVariable() == a.getVariable()) {
                    x = a1;
                    break;
                }

            if (x == null) {
                x = new Access(a.getVariable(), this);
                accesses.add(x);
                a.getVariable().addAccess(x);
            }
            for (Location l : a.getReadInstanceList())
                x.addInstance(rel.absoluteOf(l), Access.READ);
            for (Location l : a.getWriteInstanceList())
                x.addInstance(rel.absoluteOf(l), Access.WRITE);
        }

        for (Call c : what.calls) {
            Call x = null;
            for (Call c1 : calls)
                if (c1.getFunction() == c.getFunction()) {
                    x = c1;
                    break;
                }

            if (x == null) {
                x = new Call(c.getFunction(), this);
                calls.add(x);
                c.getFunction().addCall(x);
            }
            for (Location l : c.getInstanceList())
                x.addInstance(rel.absoluteOf(l));
        }

        for (LocalVariable lv : what.localVariables) {
            lv.setScope(this);
            lv.setLocation(rel.absoluteOf(lv.getLocation()));
            localVariables.add(lv);
        }
        what.localVariables.clear(); // clear references to theese local variables

        for (Type t : what.innerTypes) {
            ExplicitlyDefinedType edt = (ExplicitlyDefinedType) t;
            edt.setScope(this);
            edt.setLocation(rel.absoluteOf(edt.getLocation()));
            innerTypes.add(edt);
        }
        what.innerTypes.clear(); // clear refs

        for (CodeStripe cs : what.stripes) {
            cs.setScope(this);
            cs.setLocation(rel.absoluteOf(cs.getLocation()));
            stripes.add(cs);
        }
        what.stripes.clear(); // clear refs


        return 0;
    }

    /**
     * extracts a stripe of code from this stripe, this stripe will be modified!
     *
     * @param range what lines to extract from this stripe, this location is expressed
     *              in the same terms as getLocation() - it is a location relative to the location
     *              of this Stripe's Scope
     * @return this stripe if range contains the whole thing
     *         a different stripe if range was from the content
     *         null if you specified a range that breaks the rules of extraction
     *         CodeStripe.EMPTY if range is not within the bounds of this stripe
     */
    public CodeStripe extractStripe(Location range) {
        if (range.intersects(location) == false) {
            // the range you asked for is not within my juristiction
            return new CodeStripe();
        }

        if (range.contains(location)) {
            // ok so you want the whole thing
            if (hasStaticScope())
            // you can't move the whole stripe if it's glued to it's scope
                return null;
            else {
                // it is your job to unlink it and link it back
                // and set the right location for it
                return this;
            }
        }
        // if you don't want the whole thing you can only get stuff from
        // the thing's contents
        if (hasStaticContent())
        // you can get to the content only if it is allowed
            return null;

        Location relRange = location.relativeOf(range);
        if (contentLocation.contains(relRange) == false)
        // and only if the location you asked for is actually within range
            return null;

        // ok, so you are here. this means that you have access to the content
        // and your location is valid:  congratulations! but you are still not there yet!
        // now we must create a new stripe
        // so it has a unknown body because it does not belong anywhere yet
        CodeStripe newStripe = new CodeStripe(Body.getUnkonwnBody());
        // it is not empty and is not some kind of block
        newStripe.setSignature(TEMP);
        newStripe.setAccess(NONSTATIC_STRIPE);

        CodeStripe delegatedExtraction = null;
        for (CodeStripe cs : stripes) {
            CodeStripe extracted = cs.extractStripe(relRange);
            // at this point extracted can be one of:
            // 1. cs     2. EMPTY    3. null   4. some new CodeStripe
            if (extracted == cs)
                newStripe.addScopedElement(cs); // we record it for later use
            else if (extracted == null)
            // you break the rules of any of my children: you break my rules
                return null;
            else if (extracted.isEmpty() == false) {
                // this means that the range was within the content of one of my children
                // and it did my job for me

                if (delegatedExtraction != null)
                    throw new RuntimeException("Some CodeStripes are overlapping! The model might be corrupted!");
                else
                    delegatedExtraction = extracted;
            }
        }

        // and now for the double check: if all went well 2 cases are possible
        // at this point: either delegatedExtraction==null and es.size()>=0
        //                or delegatedExtraction!=null and es.size()=0
        // it is an error to have delegatedExtraction!=null and es.size()>0
        if (delegatedExtraction != null) {
            if (newStripe.stripes.size() == 0)
                return delegatedExtraction;
            else
                throw new RuntimeException("Some CodeStripes are overlapping! The model might be corrupted!");
            //return null; // error
        }

        // add innerType defs
        for (Iterator<Type> it = innerTypes.iterator(); it.hasNext();) {
            Type t = it.next();
            if ((t instanceof ExplicitlyDefinedType) == false) break;
            // ok so this might smell bad ...
            // todo
            ExplicitlyDefinedType edt = (ExplicitlyDefinedType) t;

            if (relRange.contains(edt.getLocation())) {
                newStripe.addInnerType(edt);
            } else if (relRange.intersects(edt.getLocation()))
                return null; // you can't select only part of a class definition!
        }

        // add local var defs
        for (Iterator<LocalVariable> it = localVariables.iterator(); it.hasNext();) {
            LocalVariable lv = it.next();

            if (relRange.contains(lv.getLocation())) {
                newStripe.addLocalVar(lv);
            } else if (relRange.intersects(lv.getLocation()))
                return null; // you can't select only part of a local var declaration!
        }

        // add calls
        for (Call c : calls) {
            int r = Location.checkLocationsWithin(relRange, c.getInstanceList());

            if (r == -1) return null;
            if (r == 1) newStripe.addCall(c);
        }

        // add accesses
        for (Access a : accesses) {
            int r = Location.checkLocationsWithin(relRange, a.getInstanceList());

            if (r == -1) return null;
            if (r == 1) newStripe.addAccess(a);
        }

        // ok so until here it was all checks, nothing in this was modified,
        // well now all that changes.

        newStripe.setLocation(relRange);
        newStripe.setContentLocation(relRange.relativeOf(relRange)); // looks strange no?
        newStripe.getSourceCodeFromStripe(this); // and now add the source code...

        // add stripes
        stripes.removeAll(newStripe.stripes);
        for (CodeStripe cs : newStripe.stripes) {
            cs.setScope(newStripe);
            cs.setLocation(relRange.relativeOf(cs.getLocation()));
        }

        // add innerType defs
        innerTypes.removeAll(newStripe.innerTypes);
        for (Type t : newStripe.innerTypes) {
            ExplicitlyDefinedType edt = (ExplicitlyDefinedType) t;
            edt.setScope(newStripe);
            edt.setLocation(relRange.relativeOf(edt.getLocation()));
        }

        // add local var defs
        localVariables.removeAll(newStripe.localVariables);
        for (LocalVariable lv : newStripe.localVariables) {
            lv.setScope(newStripe);
            lv.setLocation(relRange.relativeOf(lv.getLocation()));
        }

        // add calls
        ModelElementList<Call> ec = newStripe.calls;
        newStripe.calls = new ModelElementList<Call>();
        for (Call c : ec) {
            Call newCall = new Call(c.getFunction(), newStripe);

            newCall.getInstanceList().addAll(c.extractInstancesWithin(relRange));
            newStripe.addCall(newCall);
            if (c.getInstanceList().size() == 0) calls.remove(c);
        }

        // add accesses
        ModelElementList<Access> ea = newStripe.accesses;
        newStripe.accesses = new ModelElementList<Access>();
        for (Access a : ea) {
            Access newAccess = new Access(a.getVariable(), newStripe);

            newAccess.getReadInstanceList().addAll(a.extractReadInstancesWithin(relRange));
            newAccess.getWriteInstanceList().addAll(a.extractWriteInstancesWithin(relRange));

            newStripe.addAccess(newAccess);
            if (a.getInstanceList().size() == 0) accesses.remove(a);
        }

        return newStripe;
    }

    public Body getParentBody() {
        if (getScope() instanceof CodeStripe) {
            return ((CodeStripe) getScope()).getParentBody();
        } else
            return (Body) getScope();
    }

    ModelElementList<Call> flattenCalls() {
        ModelElementList<Call> allCalls = new ModelElementList<Call>();
        allCalls.addAll(calls);

        for (CodeStripe cs : stripes)
            allCalls.addAll(cs.flattenCalls());

        return allCalls;
    }

    ModelElementList<Access> flattenAccesses() {
        ModelElementList<Access> allAccesses = new ModelElementList<Access>();
        allAccesses.addAll(accesses);

        for (CodeStripe cs : stripes)
            allAccesses.addAll(cs.flattenAccesses());

        return allAccesses;
    }

    ModelElementList<LocalVariable> flattenLocalVariables() {
        ModelElementList<LocalVariable> allLocalVariables = new ModelElementList<LocalVariable>();
        allLocalVariables.addAll(localVariables);

        for (CodeStripe cs : stripes)
            allLocalVariables.addAll(cs.flattenLocalVariables());

        return allLocalVariables;
    }

    ModelElementList<Type> flattenInnerTypes() {
        ModelElementList<Type> allInnerTypes = new ModelElementList<Type>();
        allInnerTypes.addAll(innerTypes);

        for (CodeStripe cs : stripes)
            allInnerTypes.addAll(cs.flattenInnerTypes());

        return allInnerTypes;
    }

    public void getSourceCodeFromStripe(CodeStripe parent) {
        int sl = location.getStartLine(), el = location.getEndLine();
        // if start line = 0 then sc is relative... (also ec)
        // -1 because column counting starts @ 1 not 0
        int sc = location.getStartChar() - 1 + (sl != 0 ? 0 : parent.getLocation().getStartChar()),
                ec = location.getEndChar() - 1 + (el != 0 ? 0 : parent.getLocation().getEndChar());

        source = new String[el - sl + 1];
        if (parent.source.length <= el) {
            throw new IllegalArgumentException("Source code is too short for my specified position & size" +
                    "\nme: start@" + sl + " end@" + el + " src: " + parent.source.length + " lines");
        }
        sc = Math.min(sc, parent.source[sl].length() - 1);

        if (sc >= 0) {
            int fle = (el != sl) ? parent.source[sl].length() - 1 : ec;
            source[0] = blankChars(parent.source[sl], 0, sc - 1, ' ');
            parent.source[sl] = blankChars(parent.source[sl], sc, fle, ' ');
        } else
            source[0] = "";

        for (int i = 1; i < (el - sl); i++) {
            source[i] = parent.source[sl + i];
            parent.source[sl + i] = "";
        }

        //ec=Math.min(ec,parent.source[el].length()-1);
        if (ec >= 0) {
            String lastl = (el != sl) ? parent.source[el] : source[el - sl];

            source[el - sl] = blankChars(lastl, ec + 1, lastl.length() - 1, ' ');
            if (el != sl)
                parent.source[el] = blankChars(parent.source[el], 0, ec, ' ');
        } else
            source[el - sl] = "";

    }

    public void setSourceCode(String sourceCode) {
        source = sourceCode.concat("").split("\n");
        if (source.length < (location.getEndLine() - location.getStartLine() + 1)) {
            throw new IllegalArgumentException("Source code is to short for my specified size\n" +
                    this.getFullName() + "\n" +
                    "me: " + (location.getEndLine() - location.getStartLine() + 1) + " src:" + source.length + "\n");
        }
    }

    public String getSourceCode() {
        String sourceCode = "";
        String lines[] = fill();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null)
                sourceCode += lines[i];
            sourceCode += "\n";
        }
        return sourceCode;
    }

    private String[] fill() {
        // next line added by RADUM!!!!!!
        if (source == null) return new String[0];

        String[] src = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null)
                src[i] = new String(source[i]);
            else
                src[i] = null;
        }

        for (CodeStripe cs : stripes) {
            String[] csrc = cs.fill();
            int sl = cs.getLocation().getStartLine(), el = cs.getLocation().getEndLine();
            // if start line = 0 then sc is relative... (also ec)
            // -1 because column counting starts @ 1 not 0
            int sc = cs.getLocation().getStartChar() - 1 + (sl != 0 ? 0 : getLocation().getStartChar()),
                    ec = cs.getLocation().getEndChar() - 1 + (el != 0 ? 0 : getLocation().getEndChar());

            src[sl] = fillBlanks(src[sl], csrc[0],
                    sc,
                    (sl != el) ? csrc[0].length() - 1 : ec);

            for (int i = 1; i < (csrc.length - 1); i++) {
                src[sl + i] = csrc[i];
            }
            if (sl != el)
                src[sl + csrc.length - 1] =
                        fillBlanks(src[sl + csrc.length - 1], csrc[csrc.length - 1], 0, ec);
        }
        return src;
    }

    private static String blankChars(String s, int begin, int end, char c) {
        if (end < begin || end < 0 || begin < 0) return s;
        String sb = (begin > 0) ? s.substring(0, begin) : "",
                se = (end < (s.length() - 1)) ? s.substring(end + 1) : "";
        for (int i = begin; i <= end; i++) sb += c;
        return sb + se;
    }

    private static String fillBlanks(String dest, String src, int begin, int end) {
        if (end < begin || end < 0 || begin < 0) return dest;

        String t = (begin > 0) ? begin < dest.length() ? dest.substring(0, begin) : dest : "";

        String t1 = (end < (src.length() - 1)) ? src.substring(begin, end + 1) :
                ((begin <= (src.length() - 1)) ? src.substring(begin) : "");

        String t2 = (end < (dest.length() - 1)) ? dest.substring(end + 1) : "";

        return t + t1 + t2;
    }

    public void accept(ModelVisitor v) {
        v.visitCodeStripe(this);
    }

    /*-------------------------------------------------------------*/

    private int getAtomicCyclo() {
        if (
        // it's a hack I know, but case is fully nonMuvable (aka static)
        //  only when it has no body (it is empty)
                (CASE.equals(signature) && access != STATIC_STRIPE) ||
                DO.equals(signature) ||
                FOR.equals(signature) ||
                IF.equals(signature) ||
                WHILE.equals(signature)
        )
            return atomicCyclo + 1;
        else
            return atomicCyclo;
    }

    public void addAtomicCyclo(int cn) {
        atomicCyclo += cn;
    }

    private int getAtomicExceptions() {
        return CATCH.equals(signature) ? 1 : 0;
    }

    private int getAtomicNestingLevel() {
        if (
                (CASE.equals(signature) && access != STATIC_STRIPE) ||
                CATCH.equals(signature) ||
                DO.equals(signature) ||
                FOR.equals(signature) ||
                IF.equals(signature) ||
                SWITCH.equals(signature) ||
                TRY.equals(signature) ||
                WHILE.equals(signature)
        )
            return 1;
        else
            return 0;
    }


    public void addBodyExitPoints(int ep) {
        bodyExits += ep;
    }

    private int getAtomicLoops() {
        if (
                DO.equals(signature) ||
                FOR.equals(signature) ||
                WHILE.equals(signature)
        )
            return 1;
        else
            return 0;
    }

    private int getAtomicDecisions() {
        if (
                (CASE.equals(signature) && access != STATIC_STRIPE) ||
                DO.equals(signature) ||
                FOR.equals(signature) ||
                IF.equals(signature) ||
                WHILE.equals(signature)
        )
            return 1;
        else
            return 0;
    }

    public void addAtomicCommentLines(int n) {
        linesOfComment += n;
    }

    public void addAtomicStatements(int n) {
        numberOfStatements += n;
    }

    private int getAtomicStatements() {
        if (
                (ELSE.equals(signature) && access != STATIC_STRIPE) ||
                DO.equals(signature) ||
                FOR.equals(signature) ||
                IF.equals(signature) ||
                WHILE.equals(signature)
        )
            return numberOfStatements + 1;
        else
            return numberOfStatements;
    }


    /**
     * Is the McCabe cyclomatic complexity number of the function.
     * <P>
     * Obs:
     * <br>
     * We know that compared to the rest of the metrics defined for
     * this class this one is not atomic, but because it is important
     * for us, and because its definition still has to be studied we
     * put it here.
     */
    public int getCyclomaticNumber() {
        int cyclo = getAtomicCyclo();

        for (CodeStripe cs : stripes)
            cyclo += cs.getCyclomaticNumber();

        return cyclo;
    }

    /**
     * Is the maximum nesting level for control structures in the CodeStripe.
     */
    public int getMaxNestingLevel() {
        int nl = getAtomicNestingLevel(), mnl = nl;

        for (CodeStripe cs : stripes) {
            int cnl = cs.getMaxNestingLevel() + nl;
            if (cnl > mnl) mnl = cnl;
        }

        return mnl;
    }

    /**
     * Is the number of "catch" blocks in a body.
     * Concerning "numberofExceptions" we have to consider also about
     * the possibility to count the number of "try-catch" blocks in a function.
     */
    public int getNumberOfExceptions() {
        int ne = getAtomicExceptions();

        for (CodeStripe cs : stripes) {
            ne += cs.getNumberOfExceptions();
        }

        return ne;
    }

    /**
     * Is the number of statements associated with an explicit exit from this body.(return, exit)
     */
    public int getNumberOfExits() {
        int ne = bodyExits;

        for (CodeStripe cs : stripes) {
            ne += cs.getNumberOfExits();
        }

        return ne;
    }

    /**
     * Is the number of statements with a decision (if, switch).
     */
    public int getNumberOfDecisions() {
        int nd = getAtomicDecisions();

        for (CodeStripe cs : stripes) {
            nd += cs.getNumberOfDecisions();
        }

        return nd;
    }

    /**
     * Is the number of loop statements (pre- and post- tested loops).
     */
    public int getNumberOfLoops() {
        int nl = getAtomicLoops();

        for (CodeStripe cs : stripes) {
            nl += cs.getNumberOfLoops();
        }

        return nl;
    }

    /**
     * Returns the total number of lines in a body.
     */
    public int getNumberOfLines() {
        return getLocation().getEndLine() - getLocation().getStartLine() + 1;
    }

    /**
     * Returns the number of commented lines in a body.
     */
    public int getNumberOfComments() {
        int nc = linesOfComment;

        for (CodeStripe cs : stripes) {
            nc += cs.getNumberOfComments();
        }

        return nc;
    }

    /**
     * Returns the number of statements of a method.
     * Following statements are counted:
     * - Control statements.
     * - Statements followed by ";"
     */
    public int getNumberOfStatements() {
        int ns = getAtomicStatements();

        for (CodeStripe cs : stripes) {
            ns += cs.getNumberOfStatements();
        }

        return ns;
    }

    /*-------------------------------------------------------------*/

    boolean restore() {
        if (super.restore()) {
            if (stripes != null)
                stripes.restore();
            if (accesses != null)
                accesses.restore();
            if (calls != null)
                calls.restore();
            if (innerTypes != null)
                innerTypes.restore();
            if (localVariables != null)
                localVariables.restore();
            return true;
        }
        return false;
    }
}

