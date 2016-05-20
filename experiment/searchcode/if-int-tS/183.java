/*
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 only, as published by
 * the Free Software Foundation. Oracle designates this particular file as
 * subject to the "Classpath" exception as provided by Oracle in the LICENSE
 * file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License version 2 for more
 * details (a copy is included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA or
 * visit www.oracle.com if you need additional information or have any
 * questions.
 */

package berlin.com.sun.tools.javac.code;

import static berlin.com.sun.tools.javac.code.Flags.ABSTRACT;
import static berlin.com.sun.tools.javac.code.Flags.ACYCLIC;
import static berlin.com.sun.tools.javac.code.Flags.COMPOUND;
import static berlin.com.sun.tools.javac.code.Flags.FINAL;
import static berlin.com.sun.tools.javac.code.Flags.INTERFACE;
import static berlin.com.sun.tools.javac.code.Flags.PUBLIC;
import static berlin.com.sun.tools.javac.code.Flags.STATIC;
import static berlin.com.sun.tools.javac.code.Flags.SYNTHETIC;
import static berlin.com.sun.tools.javac.code.Type.map;
import static berlin.com.sun.tools.javac.code.TypeTags.ARRAY;
import static berlin.com.sun.tools.javac.code.TypeTags.BOOLEAN;
import static berlin.com.sun.tools.javac.code.TypeTags.BOT;
import static berlin.com.sun.tools.javac.code.TypeTags.BYTE;
import static berlin.com.sun.tools.javac.code.TypeTags.CHAR;
import static berlin.com.sun.tools.javac.code.TypeTags.CLASS;
import static berlin.com.sun.tools.javac.code.TypeTags.DOUBLE;
import static berlin.com.sun.tools.javac.code.TypeTags.ERROR;
import static berlin.com.sun.tools.javac.code.TypeTags.FLOAT;
import static berlin.com.sun.tools.javac.code.TypeTags.FORALL;
import static berlin.com.sun.tools.javac.code.TypeTags.INT;
import static berlin.com.sun.tools.javac.code.TypeTags.LONG;
import static berlin.com.sun.tools.javac.code.TypeTags.METHOD;
import static berlin.com.sun.tools.javac.code.TypeTags.NONE;
import static berlin.com.sun.tools.javac.code.TypeTags.SHORT;
import static berlin.com.sun.tools.javac.code.TypeTags.TYPEVAR;
import static berlin.com.sun.tools.javac.code.TypeTags.UNDETVAR;
import static berlin.com.sun.tools.javac.code.TypeTags.UNKNOWN;
import static berlin.com.sun.tools.javac.code.TypeTags.VOID;
import static berlin.com.sun.tools.javac.code.TypeTags.WILDCARD;
import static berlin.com.sun.tools.javac.code.TypeTags.firstPartialTag;
import static berlin.com.sun.tools.javac.code.TypeTags.lastBaseTag;
import static berlin.com.sun.tools.javac.util.ListBuffer.lb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import berlin.com.sun.tools.javac.code.Symbol.ClassSymbol;
import berlin.com.sun.tools.javac.code.Type.ArrayType;
import berlin.com.sun.tools.javac.code.Type.CapturedType;
import berlin.com.sun.tools.javac.code.Type.ClassType;
import berlin.com.sun.tools.javac.code.Type.ErrorType;
import berlin.com.sun.tools.javac.code.Type.ForAll;
import berlin.com.sun.tools.javac.code.Type.Mapping;
import berlin.com.sun.tools.javac.code.Type.MethodType;
import berlin.com.sun.tools.javac.code.Type.PackageType;
import berlin.com.sun.tools.javac.code.Type.TypeVar;
import berlin.com.sun.tools.javac.code.Type.UndetVar;
import berlin.com.sun.tools.javac.code.Type.WildcardType;
import berlin.com.sun.tools.javac.comp.Check;
import berlin.com.sun.tools.javac.jvm.ClassReader;
import berlin.com.sun.tools.javac.util.Context;
import berlin.com.sun.tools.javac.util.List;
import berlin.com.sun.tools.javac.util.ListBuffer;
import berlin.com.sun.tools.javac.util.Name;
import berlin.com.sun.tools.javac.util.Warner;

/**
 * Utility class containing various operations on types.
 */
public class Types {
    protected static final Context.Key<Types> typesKey = new Context.Key<Types>();

    final Symtab syms;

    final Name.Table names;

    final boolean allowBoxing;

    final ClassReader reader;

    final Source source;

    final Check chk;

    List<Warner> warnStack = List.nil();

    final Name capturedName;

    // <editor-fold defaultstate="collapsed" desc="Instantiating">
    public static Types instance(Context context) {
        Types instance = context.get(typesKey);
        if (instance == null)
            instance = new Types(context);
        return instance;
    }

    protected Types(Context context) {
        context.put(typesKey, this);
        syms = Symtab.instance(context);
        names = Name.Table.instance(context);
        allowBoxing = Source.instance(context).allowBoxing();
        reader = ClassReader.instance(context);
        source = Source.instance(context);
        chk = Check.instance(context);
        capturedName = names.fromString("<captured wildcard>");
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="upperBound">
    /**
     * The "rvalue conversion".<br>
     * The upper bound of most types is the type itself. Wildcards, on the other
     * hand have upper and lower bounds.
     * 
     * @param t
     *            a type
     * @return the upper bound of the given type
     */
    public Type upperBound(Type t) {
        return upperBound.visit(t);
    }

    // where
    private final MapVisitor<Void> upperBound = new MapVisitor<Void>() {

        @Override
        public Type visitWildcardType(WildcardType t, Void ignored) {
            if (t.isSuperBound())
                return t.bound == null ? syms.objectType : t.bound.bound;
            else
                return visit(t.type);
        }

        @Override
        public Type visitCapturedType(CapturedType t, Void ignored) {
            return visit(t.bound);
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="lowerBound">
    /**
     * The "lvalue conversion".<br>
     * The lower bound of most types is the type itself. Wildcards, on the other
     * hand have upper and lower bounds.
     * 
     * @param t
     *            a type
     * @return the lower bound of the given type
     */
    public Type lowerBound(Type t) {
        return lowerBound.visit(t);
    }

    // where
    private final MapVisitor<Void> lowerBound = new MapVisitor<Void>() {

        @Override
        public Type visitWildcardType(WildcardType t, Void ignored) {
            return t.isExtendsBound() ? syms.botType : visit(t.type);
        }

        @Override
        public Type visitCapturedType(CapturedType t, Void ignored) {
            return visit(t.getLowerBound());
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isUnbounded">
    /**
     * Checks that all the arguments to a class are unbounded wildcards or
     * something else that doesn't make any restrictions on the arguments. If a
     * class isUnbounded, a raw super- or subclass can be cast to it without a
     * warning.
     * 
     * @param t
     *            a type
     * @return true iff the given type is unbounded or raw
     */
    public boolean isUnbounded(Type t) {
        return isUnbounded.visit(t);
    }

    // where
    private final UnaryVisitor<Boolean> isUnbounded = new UnaryVisitor<Boolean>() {

        public Boolean visitType(Type t, Void ignored) {
            return true;
        }

        @Override
        public Boolean visitClassType(ClassType t, Void ignored) {
            List<Type> parms = t.tsym.type.allparams();
            List<Type> args = t.allparams();
            while (parms.nonEmpty()) {
                WildcardType unb = new WildcardType(syms.objectType, BoundKind.UNBOUND, syms.boundClass,
                        (TypeVar) parms.head);
                if (!containsType(args.head, unb))
                    return false;
                parms = parms.tail;
                args = args.tail;
            }
            return true;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="asSub">
    /**
     * Return the least specific subtype of t that starts with symbol sym. If
     * none exists, return null. The least specific subtype is determined as
     * follows:
     * 
     * <p>
     * If there is exactly one parameterized instance of sym that is a subtype
     * of t, that parameterized instance is returned.<br>
     * Otherwise, if the plain type or raw type `sym' is a subtype of type t,
     * the type `sym' itself is returned. Otherwise, null is returned.
     */
    public Type asSub(Type t, Symbol sym) {
        return asSub.visit(t, sym);
    }

    // where
    private final SimpleVisitor<Type, Symbol> asSub = new SimpleVisitor<Type, Symbol>() {

        public Type visitType(Type t, Symbol sym) {
            return null;
        }

        @Override
        public Type visitClassType(ClassType t, Symbol sym) {
            if (t.tsym == sym)
                return t;
            Type base = asSuper(sym.type, t.tsym);
            if (base == null)
                return null;
            ListBuffer<Type> from = new ListBuffer<Type>();
            ListBuffer<Type> to = new ListBuffer<Type>();
            try {
                adapt(base, t, from, to);
            } catch (AdaptFailure ex) {
                return null;
            }
            Type res = subst(sym.type, from.toList(), to.toList());
            if (!isSubtype(res, t))
                return null;
            ListBuffer<Type> openVars = new ListBuffer<Type>();
            for (List<Type> l = sym.type.allparams(); l.nonEmpty(); l = l.tail)
                if (res.contains(l.head) && !t.contains(l.head))
                    openVars.append(l.head);
            if (openVars.nonEmpty()) {
                if (t.isRaw()) {
                    // The subtype of a raw type is raw
                    res = erasure(res);
                } else {
                    // Unbound type arguments default to ?
                    List<Type> opens = openVars.toList();
                    ListBuffer<Type> qs = new ListBuffer<Type>();
                    for (List<Type> iter = opens; iter.nonEmpty(); iter = iter.tail) {
                        qs.append(new WildcardType(syms.objectType, BoundKind.UNBOUND, syms.boundClass,
                                (TypeVar) iter.head));
                    }
                    res = subst(res, opens, qs.toList());
                }
            }
            return res;
        }

        @Override
        public Type visitErrorType(ErrorType t, Symbol sym) {
            return t;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isConvertible">
    /**
     * Is t a subtype of or convertiable via boxing/unboxing convertions to s?
     */
    public boolean isConvertible(Type t, Type s, Warner warn) {
        boolean tPrimitive = t.isPrimitive();
        boolean sPrimitive = s.isPrimitive();
        if (tPrimitive == sPrimitive)
            return isSubtypeUnchecked(t, s, warn);
        if (!allowBoxing)
            return false;
        return tPrimitive ? isSubtype(boxedClass(t).type, s) : isSubtype(unboxedType(t), s);
    }

    /**
     * Is t a subtype of or convertiable via boxing/unboxing convertions to s?
     */
    public boolean isConvertible(Type t, Type s) {
        return isConvertible(t, s, Warner.noWarnings);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isSubtype">
    /**
     * Is t an unchecked subtype of s?
     */
    public boolean isSubtypeUnchecked(Type t, Type s) {
        return isSubtypeUnchecked(t, s, Warner.noWarnings);
    }

    /**
     * Is t an unchecked subtype of s?
     */
    public boolean isSubtypeUnchecked(Type t, Type s, Warner warn) {
        if (t.tag == ARRAY && s.tag == ARRAY) {
            return (((ArrayType) t).elemtype.tag <= lastBaseTag) ? isSameType(elemtype(t), elemtype(s))
                    : isSubtypeUnchecked(elemtype(t), elemtype(s), warn);
        } else if (isSubtype(t, s)) {
            return true;
        } else if (!s.isRaw()) {
            Type t2 = asSuper(t, s.tsym);
            if (t2 != null && t2.isRaw()) {
                if (isReifiable(s))
                    warn.silentUnchecked();
                else
                    warn.warnUnchecked();
                return true;
            }
        }
        return false;
    }

    /**
     * Is t a subtype of s?<br>
     * (not defined for Method and ForAll types)
     */
    final public boolean isSubtype(Type t, Type s) {
        return isSubtype(t, s, true);
    }

    final public boolean isSubtypeNoCapture(Type t, Type s) {
        return isSubtype(t, s, false);
    }

    public boolean isSubtype(Type t, Type s, boolean capture) {
        if (t == s)
            return true;

        if (s.tag >= firstPartialTag)
            return isSuperType(s, t);

        Type lower = lowerBound(s);
        if (s != lower)
            return isSubtype(capture ? capture(t) : t, lower, false);

        return isSubtype.visit(capture ? capture(t) : t, s);
    }

    // where
    private TypeRelation isSubtype = new TypeRelation() {
        public Boolean visitType(Type t, Type s) {
            switch (t.tag) {
            case BYTE:
            case CHAR:
                return (t.tag == s.tag || t.tag + 2 <= s.tag && s.tag <= DOUBLE);
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return t.tag <= s.tag && s.tag <= DOUBLE;
            case BOOLEAN:
            case VOID:
                return t.tag == s.tag;
            case TYPEVAR:
                return isSubtypeNoCapture(t.getUpperBound(), s);
            case BOT:
                return s.tag == BOT || s.tag == CLASS || s.tag == ARRAY || s.tag == TYPEVAR;
            case NONE:
                return false;
            default:
                throw new AssertionError("isSubtype " + t.tag);
            }
        }

        private Set<TypePair> cache = new HashSet<TypePair>();

        private boolean containsTypeRecursive(Type t, Type s) {
            TypePair pair = new TypePair(t, s);
            if (cache.add(pair)) {
                try {
                    return containsType(t.getTypeArguments(), s.getTypeArguments());
                } finally {
                    cache.remove(pair);
                }
            } else {
                return containsType(t.getTypeArguments(), rewriteSupers(s).getTypeArguments());
            }
        }

        private Type rewriteSupers(Type t) {
            if (!t.isParameterized())
                return t;
            ListBuffer<Type> from = lb();
            ListBuffer<Type> to = lb();
            adaptSelf(t, from, to);
            if (from.isEmpty())
                return t;
            ListBuffer<Type> rewrite = lb();
            boolean changed = false;
            for (Type orig : to.toList()) {
                Type s = rewriteSupers(orig);
                if (s.isSuperBound() && !s.isExtendsBound()) {
                    s = new WildcardType(syms.objectType, BoundKind.UNBOUND, syms.boundClass);
                    changed = true;
                } else if (s != orig) {
                    s = new WildcardType(upperBound(s), BoundKind.EXTENDS, syms.boundClass);
                    changed = true;
                }
                rewrite.append(s);
            }
            if (changed)
                return subst(t.tsym.type, from.toList(), rewrite.toList());
            else
                return t;
        }

        @Override
        public Boolean visitClassType(ClassType t, Type s) {
            Type sup = asSuper(t, s.tsym);
            return sup != null
                    && sup.tsym == s.tsym
                    // You're not allowed to write
                    // Vector<Object> vec = new Vector<String>();
                    // But with wildcards you can write
                    // Vector<? extends Object> vec = new Vector<String>();
                    // which means that subtype checking must be done
                    // here instead of same-type checking (via containsType).
                    && (!s.isParameterized() || containsTypeRecursive(s, sup))
                    && isSubtypeNoCapture(sup.getEnclosingType(), s.getEnclosingType());
        }

        @Override
        public Boolean visitArrayType(ArrayType t, Type s) {
            if (s.tag == ARRAY) {
                if (t.elemtype.tag <= lastBaseTag)
                    return isSameType(t.elemtype, elemtype(s));
                else
                    return isSubtypeNoCapture(t.elemtype, elemtype(s));
            }

            if (s.tag == CLASS) {
                Name sname = s.tsym.getQualifiedName();
                return sname == names.java_lang_Object || sname == names.java_lang_Cloneable
                        || sname == names.java_io_Serializable;
            }

            return false;
        }

        @Override
        public Boolean visitUndetVar(UndetVar t, Type s) {
            // todo: test against origin needed? or replace with substitution?
            if (t == s || t.qtype == s || s.tag == ERROR || s.tag == UNKNOWN)
                return true;

            if (t.inst != null)
                return isSubtypeNoCapture(t.inst, s); // TODO: ", warn"?

            t.hibounds = t.hibounds.prepend(s);
            return true;
        }

        @Override
        public Boolean visitErrorType(ErrorType t, Type s) {
            return true;
        }
    };

    /**
     * Is t a subtype of every type in given list `ts'?<br>
     * (not defined for Method and ForAll types)<br>
     * Allows unchecked conversions.
     */
    public boolean isSubtypeUnchecked(Type t, List<Type> ts, Warner warn) {
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            if (!isSubtypeUnchecked(t, l.head, warn))
                return false;
        return true;
    }

    /**
     * Are corresponding elements of ts subtypes of ss? If lists are of
     * different length, return false.
     */
    public boolean isSubtypes(List<Type> ts, List<Type> ss) {
        while (ts.tail != null && ss.tail != null
        /* inlined: ts.nonEmpty() && ss.nonEmpty() */&& isSubtype(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.tail == null && ss.tail == null;
        /* inlined: ts.isEmpty() && ss.isEmpty(); */
    }

    /**
     * Are corresponding elements of ts subtypes of ss, allowing unchecked
     * conversions? If lists are of different length, return false.
     **/
    public boolean isSubtypesUnchecked(List<Type> ts, List<Type> ss, Warner warn) {
        while (ts.tail != null && ss.tail != null
        /* inlined: ts.nonEmpty() && ss.nonEmpty() */&& isSubtypeUnchecked(ts.head, ss.head, warn)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.tail == null && ss.tail == null;
        /* inlined: ts.isEmpty() && ss.isEmpty(); */
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isSuperType">
    /**
     * Is t a supertype of s?
     */
    public boolean isSuperType(Type t, Type s) {
        switch (t.tag) {
        case ERROR:
            return true;
        case UNDETVAR: {
            UndetVar undet = (UndetVar) t;
            if (t == s || undet.qtype == s || s.tag == ERROR || s.tag == BOT)
                return true;
            if (undet.inst != null)
                return isSubtype(s, undet.inst);
            undet.lobounds = undet.lobounds.prepend(s);
            return true;
        }
        default:
            return isSubtype(s, t);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isSameType">
    /**
     * Are corresponding elements of the lists the same type? If lists are of
     * different length, return false.
     */
    public boolean isSameTypes(List<Type> ts, List<Type> ss) {
        while (ts.tail != null && ss.tail != null
        /* inlined: ts.nonEmpty() && ss.nonEmpty() */&& isSameType(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.tail == null && ss.tail == null;
        /* inlined: ts.isEmpty() && ss.isEmpty(); */
    }

    /**
     * Is t the same type as s?
     */
    public boolean isSameType(Type t, Type s) {
        return isSameType.visit(t, s);
    }

    // where
    private TypeRelation isSameType = new TypeRelation() {

        public Boolean visitType(Type t, Type s) {
            if (t == s)
                return true;

            if (s.tag >= firstPartialTag)
                return visit(s, t);

            switch (t.tag) {
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
            case VOID:
            case BOT:
            case NONE:
                return t.tag == s.tag;
            case TYPEVAR:
                return s.isSuperBound() && !s.isExtendsBound() && visit(t, upperBound(s));
            default:
                throw new AssertionError("isSameType " + t.tag);
            }
        }

        @Override
        public Boolean visitWildcardType(WildcardType t, Type s) {
            if (s.tag >= firstPartialTag)
                return visit(s, t);
            else
                return false;
        }

        @Override
        public Boolean visitClassType(ClassType t, Type s) {
            if (t == s)
                return true;

            if (s.tag >= firstPartialTag)
                return visit(s, t);

            if (s.isSuperBound() && !s.isExtendsBound())
                return visit(t, upperBound(s)) && visit(t, lowerBound(s));

            if (t.isCompound() && s.isCompound()) {
                if (!visit(supertype(t), supertype(s)))
                    return false;

                HashSet<SingletonType> set = new HashSet<SingletonType>();
                for (Type x : interfaces(t))
                    set.add(new SingletonType(x));
                for (Type x : interfaces(s)) {
                    if (!set.remove(new SingletonType(x)))
                        return false;
                }
                return (set.size() == 0);
            }
            return t.tsym == s.tsym && visit(t.getEnclosingType(), s.getEnclosingType())
                    && containsTypeEquivalent(t.getTypeArguments(), s.getTypeArguments());
        }

        @Override
        public Boolean visitArrayType(ArrayType t, Type s) {
            if (t == s)
                return true;

            if (s.tag >= firstPartialTag)
                return visit(s, t);

            return s.tag == ARRAY && containsTypeEquivalent(t.elemtype, elemtype(s));
        }

        @Override
        public Boolean visitMethodType(MethodType t, Type s) {
            // isSameType for methods does not take thrown
            // exceptions into account!
            return hasSameArgs(t, s) && visit(t.getReturnType(), s.getReturnType());
        }

        @Override
        public Boolean visitPackageType(PackageType t, Type s) {
            return t == s;
        }

        @Override
        public Boolean visitForAll(ForAll t, Type s) {
            if (s.tag != FORALL)
                return false;

            ForAll forAll = (ForAll) s;
            return hasSameBounds(t, forAll) && visit(t.qtype, subst(forAll.qtype, forAll.tvars, t.tvars));
        }

        @Override
        public Boolean visitUndetVar(UndetVar t, Type s) {
            if (s.tag == WILDCARD)
                // FIXME, this might be leftovers from before capture conversion
                return false;

            if (t == s || t.qtype == s || s.tag == ERROR || s.tag == UNKNOWN)
                return true;

            if (t.inst != null)
                return visit(t.inst, s);

            t.inst = fromUnknownFun.apply(s);
            for (List<Type> l = t.lobounds; l.nonEmpty(); l = l.tail) {
                if (!isSubtype(l.head, t.inst))
                    return false;
            }
            for (List<Type> l = t.hibounds; l.nonEmpty(); l = l.tail) {
                if (!isSubtype(t.inst, l.head))
                    return false;
            }
            return true;
        }

        @Override
        public Boolean visitErrorType(ErrorType t, Type s) {
            return true;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="fromUnknownFun">
    /**
     * A mapping that turns all unknown types in this type to fresh unknown
     * variables.
     */
    public Mapping fromUnknownFun = new Mapping("fromUnknownFun") {
        public Type apply(Type t) {
            if (t.tag == UNKNOWN)
                return new UndetVar(t);
            else
                return t.map(this);
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Contains Type">
    public boolean containedBy(Type t, Type s) {
        switch (t.tag) {
        case UNDETVAR:
            if (s.tag == WILDCARD) {
                UndetVar undetvar = (UndetVar) t;

                // Because of wildcard capture, s must be on the left
                // hand side of an assignment. Furthermore, t is an
                // underconstrained type variable, for example, one
                // that is only used in the return type of a method.
                // If the type variable is truly underconstrained, it
                // cannot have any low bounds:
                assert undetvar.lobounds.isEmpty() : undetvar;

                undetvar.inst = glb(upperBound(s), undetvar.inst);
                return true;
            } else {
                return isSameType(t, s);
            }
        case ERROR:
            return true;
        default:
            return containsType(s, t);
        }
    }

    boolean containsType(List<Type> ts, List<Type> ss) {
        while (ts.nonEmpty() && ss.nonEmpty() && containsType(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.isEmpty() && ss.isEmpty();
    }

    /**
     * Check if t contains s.
     * 
     * <p>
     * T contains S if:
     * 
     * <p>
     * {@code L(T) <: L(S) && U(S) <: U(T)}
     * 
     * <p>
     * This relation is only used by ClassType.isSubtype(), that is,
     * 
     * <p>
     * {@code C<S> <: C<T> if T contains S.}
     * 
     * <p>
     * Because of F-bounds, this relation can lead to infinite recursion. Thus
     * we must somehow break that recursion. Notice that containsType() is only
     * called from ClassType.isSubtype(). Since the arguments have already been
     * checked against their bounds, we know:
     * 
     * <p>
     * {@code U(S) <: U(T) if T is "super" bound (U(T) *is* the bound)}
     * 
     * <p>
     * {@code L(T) <: L(S) if T is "extends" bound (L(T) is bottom)}
     * 
     * @param t
     *            a type
     * @param s
     *            a type
     */
    public boolean containsType(Type t, Type s) {
        return containsType.visit(t, s);
    }

    // where
    private TypeRelation containsType = new TypeRelation() {

        private Type U(Type t) {
            while (t.tag == WILDCARD) {
                WildcardType w = (WildcardType) t;
                if (w.isSuperBound())
                    return w.bound == null ? syms.objectType : w.bound.bound;
                else
                    t = w.type;
            }
            return t;
        }

        private Type L(Type t) {
            while (t.tag == WILDCARD) {
                WildcardType w = (WildcardType) t;
                if (w.isExtendsBound())
                    return syms.botType;
                else
                    t = w.type;
            }
            return t;
        }

        public Boolean visitType(Type t, Type s) {
            if (s.tag >= firstPartialTag)
                return containedBy(s, t);
            else
                return isSameType(t, s);
        }

        void debugContainsType(WildcardType t, Type s) {
            System.err.println();
            System.err.format(" does %s contain %s?%n", t, s);
            System.err.format(" %s U(%s) <: U(%s) %s = %s%n", upperBound(s), s, t, U(t), t.isSuperBound()
                    || isSubtypeNoCapture(upperBound(s), U(t)));
            System.err.format(" %s L(%s) <: L(%s) %s = %s%n", L(t), t, s, lowerBound(s), t.isExtendsBound()
                    || isSubtypeNoCapture(L(t), lowerBound(s)));
            System.err.println();
        }

        @Override
        public Boolean visitWildcardType(WildcardType t, Type s) {
            if (s.tag >= firstPartialTag)
                return containedBy(s, t);
            else {
                // debugContainsType(t, s);
                return isSameWildcard(t, s)
                        || isCaptureOf(s, t)
                        || ((t.isExtendsBound() || isSubtypeNoCapture(L(t), lowerBound(s))) && (t.isSuperBound() || isSubtypeNoCapture(
                                upperBound(s), U(t))));
            }
        }

        @Override
        public Boolean visitUndetVar(UndetVar t, Type s) {
            if (s.tag != WILDCARD)
                return isSameType(t, s);
            else
                return false;
        }

        @Override
        public Boolean visitErrorType(ErrorType t, Type s) {
            return true;
        }
    };

    public boolean isCaptureOf(Type s, WildcardType t) {
        if (s.tag != TYPEVAR || !(s instanceof CapturedType))
            return false;
        return isSameWildcard(t, ((CapturedType) s).wildcard);
    }

    public boolean isSameWildcard(WildcardType t, Type s) {
        if (s.tag != WILDCARD)
            return false;
        WildcardType w = (WildcardType) s;
        return w.kind == t.kind && w.type == t.type;
    }

    public boolean containsTypeEquivalent(List<Type> ts, List<Type> ss) {
        while (ts.nonEmpty() && ss.nonEmpty() && containsTypeEquivalent(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.isEmpty() && ss.isEmpty();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isCastable">
    public boolean isCastable(Type t, Type s) {
        return isCastable(t, s, Warner.noWarnings);
    }

    /**
     * Is t is castable to s?<br>
     * s is assumed to be an erased type.<br>
     * (not defined for Method and ForAll types).
     */
    public boolean isCastable(Type t, Type s, Warner warn) {
        if (t == s)
            return true;

        if (t.isPrimitive() != s.isPrimitive())
            return allowBoxing && isConvertible(t, s, warn);

        if (warn != warnStack.head) {
            try {
                warnStack = warnStack.prepend(warn);
                return isCastable.visit(t, s);
            } finally {
                warnStack = warnStack.tail;
            }
        } else {
            return isCastable.visit(t, s);
        }
    }

    // where
    private TypeRelation isCastable = new TypeRelation() {

        public Boolean visitType(Type t, Type s) {
            if (s.tag == ERROR)
                return true;

            switch (t.tag) {
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return s.tag <= DOUBLE;
            case BOOLEAN:
                return s.tag == BOOLEAN;
            case VOID:
                return false;
            case BOT:
                return isSubtype(t, s);
            default:
                throw new AssertionError();
            }
        }

        @Override
        public Boolean visitWildcardType(WildcardType t, Type s) {
            return isCastable(upperBound(t), s, warnStack.head);
        }

        @Override
        public Boolean visitClassType(ClassType t, Type s) {
            if (s.tag == ERROR || s.tag == BOT)
                return true;

            if (s.tag == TYPEVAR) {
                if (isCastable(s.getUpperBound(), t, Warner.noWarnings)) {
                    warnStack.head.warnUnchecked();
                    return true;
                } else {
                    return false;
                }
            }

            if (t.isCompound()) {
                if (!visit(supertype(t), s))
                    return false;
                for (Type intf : interfaces(t)) {
                    if (!visit(intf, s))
                        return false;
                }
                return true;
            }

            if (s.isCompound()) {
                // call recursively to reuse the above code
                return visitClassType((ClassType) s, t);
            }

            if (s.tag == CLASS || s.tag == ARRAY) {
                boolean upcast;
                if ((upcast = isSubtype(erasure(t), erasure(s))) || isSubtype(erasure(s), erasure(t))) {
                    if (!upcast && s.tag == ARRAY) {
                        if (!isReifiable(s))
                            warnStack.head.warnUnchecked();
                        return true;
                    } else if (s.isRaw()) {
                        return true;
                    } else if (t.isRaw()) {
                        if (!isUnbounded(s))
                            warnStack.head.warnUnchecked();
                        return true;
                    }
                    // Assume |a| <: |b|
                    final Type a = upcast ? t : s;
                    final Type b = upcast ? s : t;
                    final boolean HIGH = true;
                    final boolean LOW = false;
                    final boolean DONT_REWRITE_TYPEVARS = false;
                    Type aHigh = rewriteQuantifiers(a, HIGH, DONT_REWRITE_TYPEVARS);
                    Type aLow = rewriteQuantifiers(a, LOW, DONT_REWRITE_TYPEVARS);
                    Type bHigh = rewriteQuantifiers(b, HIGH, DONT_REWRITE_TYPEVARS);
                    Type bLow = rewriteQuantifiers(b, LOW, DONT_REWRITE_TYPEVARS);
                    Type lowSub = asSub(bLow, aLow.tsym);
                    Type highSub = (lowSub == null) ? null : asSub(bHigh, aHigh.tsym);
                    if (highSub == null) {
                        final boolean REWRITE_TYPEVARS = true;
                        aHigh = rewriteQuantifiers(a, HIGH, REWRITE_TYPEVARS);
                        aLow = rewriteQuantifiers(a, LOW, REWRITE_TYPEVARS);
                        bHigh = rewriteQuantifiers(b, HIGH, REWRITE_TYPEVARS);
                        bLow = rewriteQuantifiers(b, LOW, REWRITE_TYPEVARS);
                        lowSub = asSub(bLow, aLow.tsym);
                        highSub = (lowSub == null) ? null : asSub(bHigh, aHigh.tsym);
                    }
                    if (highSub != null) {
                        assert a.tsym == highSub.tsym && a.tsym == lowSub.tsym : a.tsym + " != " + highSub.tsym
                                + " != " + lowSub.tsym;
                        if (!disjointTypes(aHigh.getTypeArguments(), highSub.getTypeArguments())
                                && !disjointTypes(aHigh.getTypeArguments(), lowSub.getTypeArguments())
                                && !disjointTypes(aLow.getTypeArguments(), highSub.getTypeArguments())
                                && !disjointTypes(aLow.getTypeArguments(), lowSub.getTypeArguments())) {
                            if (upcast ? giveWarning(a, highSub) || giveWarning(a, lowSub) : giveWarning(highSub, a)
                                    || giveWarning(lowSub, a))
                                warnStack.head.warnUnchecked();
                            return true;
                        }
                    }
                    if (isReifiable(s))
                        return isSubtypeUnchecked(a, b);
                    else
                        return isSubtypeUnchecked(a, b, warnStack.head);
                }

                // Sidecast
                if (s.tag == CLASS) {
                    if ((s.tsym.flags() & INTERFACE) != 0) {
                        return ((t.tsym.flags() & FINAL) == 0) ? sideCast(t, s, warnStack.head) : sideCastFinal(t, s,
                                warnStack.head);
                    } else if ((t.tsym.flags() & INTERFACE) != 0) {
                        return ((s.tsym.flags() & FINAL) == 0) ? sideCast(t, s, warnStack.head) : sideCastFinal(t, s,
                                warnStack.head);
                    } else {
                        // unrelated class types
                        return false;
                    }
                }
            }
            return false;
        }

        @Override
        public Boolean visitArrayType(ArrayType t, Type s) {
            switch (s.tag) {
            case ERROR:
            case BOT:
                return true;
            case TYPEVAR:
                if (isCastable(s, t, Warner.noWarnings)) {
                    warnStack.head.warnUnchecked();
                    return true;
                } else {
                    return false;
                }
            case CLASS:
                return isSubtype(t, s);
            case ARRAY:
                if (elemtype(t).tag <= lastBaseTag) {
                    return elemtype(t).tag == elemtype(s).tag;
                } else {
                    return visit(elemtype(t), elemtype(s));
                }
            default:
                return false;
            }
        }

        @Override
        public Boolean visitTypeVar(TypeVar t, Type s) {
            switch (s.tag) {
            case ERROR:
            case BOT:
                return true;
            case TYPEVAR:
                if (isSubtype(t, s)) {
                    return true;
                } else if (isCastable(t.bound, s, Warner.noWarnings)) {
                    warnStack.head.warnUnchecked();
                    return true;
                } else {
                    return false;
                }
            default:
                return isCastable(t.bound, s, warnStack.head);
            }
        }

        @Override
        public Boolean visitErrorType(ErrorType t, Type s) {
            return true;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="disjointTypes">
    public boolean disjointTypes(List<Type> ts, List<Type> ss) {
        while (ts.tail != null && ss.tail != null) {
            if (disjointType(ts.head, ss.head))
                return true;
            ts = ts.tail;
            ss = ss.tail;
        }
        return false;
    }

    /**
     * Two types or wildcards are considered disjoint if it can be proven that
     * no type can be contained in both. It is conservative in that it is
     * allowed to say that two types are not disjoint, even though they actually
     * are.
     * 
     * The type C<X> is castable to C<Y> exactly if X and Y are not disjoint.
     */
    public boolean disjointType(Type t, Type s) {
        return disjointType.visit(t, s);
    }

    // where
    private TypeRelation disjointType = new TypeRelation() {

        private Set<TypePair> cache = new HashSet<TypePair>();

        public Boolean visitType(Type t, Type s) {
            if (s.tag == WILDCARD)
                return visit(s, t);
            else
                return notSoftSubtypeRecursive(t, s) || notSoftSubtypeRecursive(s, t);
        }

        private boolean isCastableRecursive(Type t, Type s) {
            TypePair pair = new TypePair(t, s);
            if (cache.add(pair)) {
                try {
                    return Types.this.isCastable(t, s);
                } finally {
                    cache.remove(pair);
                }
            } else {
                return true;
            }
        }

        private boolean notSoftSubtypeRecursive(Type t, Type s) {
            TypePair pair = new TypePair(t, s);
            if (cache.add(pair)) {
                try {
                    return Types.this.notSoftSubtype(t, s);
                } finally {
                    cache.remove(pair);
                }
            } else {
                return false;
            }
        }

        @Override
        public Boolean visitWildcardType(WildcardType t, Type s) {
            if (t.isUnbound())
                return false;

            if (s.tag != WILDCARD) {
                if (t.isExtendsBound())
                    return notSoftSubtypeRecursive(s, t.type);
                else
                    // isSuperBound()
                    return notSoftSubtypeRecursive(t.type, s);
            }

            if (s.isUnbound())
                return false;

            if (t.isExtendsBound()) {
                if (s.isExtendsBound())
                    return !isCastableRecursive(t.type, upperBound(s));
                else if (s.isSuperBound())
                    return notSoftSubtypeRecursive(lowerBound(s), t.type);
            } else if (t.isSuperBound()) {
                if (s.isExtendsBound())
                    return notSoftSubtypeRecursive(t.type, upperBound(s));
            }
            return false;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="lowerBoundArgtypes">
    /**
     * Returns the lower bounds of the formals of a method.
     */
    public List<Type> lowerBoundArgtypes(Type t) {
        return map(t.getParameterTypes(), lowerBoundMapping);
    }

    private final Mapping lowerBoundMapping = new Mapping("lowerBound") {
        public Type apply(Type t) {
            return lowerBound(t);
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="notSoftSubtype">
    /**
     * This relation answers the question: is impossible that something of type
     * `t' can be a subtype of `s'? This is different from the question
     * "is `t' not a subtype of `s'?" when type variables are involved: Integer
     * is not a subtype of T where <T extends Number> but it is not true that
     * Integer cannot possibly be a subtype of T.
     */
    public boolean notSoftSubtype(Type t, Type s) {
        if (t == s)
            return false;
        if (t.tag == TYPEVAR) {
            TypeVar tv = (TypeVar) t;
            if (s.tag == TYPEVAR)
                s = s.getUpperBound();
            return !isCastable(tv.bound, s, Warner.noWarnings);
        }
        if (s.tag != WILDCARD)
            s = upperBound(s);
        if (s.tag == TYPEVAR)
            s = s.getUpperBound();
        return !isSubtype(t, s);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isReifiable">
    public boolean isReifiable(Type t) {
        return isReifiable.visit(t);
    }

    // where
    private UnaryVisitor<Boolean> isReifiable = new UnaryVisitor<Boolean>() {

        public Boolean visitType(Type t, Void ignored) {
            return true;
        }

        @Override
        public Boolean visitClassType(ClassType t, Void ignored) {
            if (!t.isParameterized())
                return true;

            for (Type param : t.allparams()) {
                if (!param.isUnbound())
                    return false;
            }
            return true;
        }

        @Override
        public Boolean visitArrayType(ArrayType t, Void ignored) {
            return visit(t.elemtype);
        }

        @Override
        public Boolean visitTypeVar(TypeVar t, Void ignored) {
            return false;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Array Utils">
    public boolean isArray(Type t) {
        while (t.tag == WILDCARD)
            t = upperBound(t);
        return t.tag == ARRAY;
    }

    /**
     * The element type of an array.
     */
    public Type elemtype(Type t) {
        switch (t.tag) {
        case WILDCARD:
            return elemtype(upperBound(t));
        case ARRAY:
            return ((ArrayType) t).elemtype;
        case FORALL:
            return elemtype(((ForAll) t).qtype);
        case ERROR:
            return t;
        default:
            return null;
        }
    }

    /**
     * Mapping to take element type of an arraytype
     */
    private Mapping elemTypeFun = new Mapping("elemTypeFun") {
        public Type apply(Type t) {
            return elemtype(t);
        }
    };

    /**
     * The number of dimensions of an array type.
     */
    public int dimensions(Type t) {
        int result = 0;
        while (t.tag == ARRAY) {
            result++;
            t = elemtype(t);
        }
        return result;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="asSuper">
    /**
     * Return the (most specific) base type of t that starts with the given
     * symbol. If none exists, return null.
     * 
     * @param t
     *            a type
     * @param sym
     *            a symbol
     */
    public Type asSuper(Type t, Symbol sym) {
        return asSuper.visit(t, sym);
    }

    // where
    private SimpleVisitor<Type, Symbol> asSuper = new SimpleVisitor<Type, Symbol>() {

        public Type visitType(Type t, Symbol sym) {
            return null;
        }

        @Override
        public Type visitClassType(ClassType t, Symbol sym) {
            if (t.tsym == sym)
                return t;

            Type st = supertype(t);
            if (st.tag == CLASS || st.tag == ERROR) {
                Type x = asSuper(st, sym);
                if (x != null)
                    return x;
            }
            if ((sym.flags() & INTERFACE) != 0) {
                for (List<Type> l = interfaces(t); l.nonEmpty(); l = l.tail) {
                    Type x = asSuper(l.head, sym);
                    if (x != null)
                        return x;
                }
            }
            return null;
        }

        @Override
        public Type visitArrayType(ArrayType t, Symbol sym) {
            return isSubtype(t, sym.type) ? sym.type : null;
        }

        @Override
        public Type visitTypeVar(TypeVar t, Symbol sym) {
            return asSuper(t.bound, sym);
        }

        @Override
        public Type visitErrorType(ErrorType t, Symbol sym) {
            return t;
        }
    };

    /**
     * Return the base type of t or any of its outer types that starts with the
     * given symbol. If none exists, return null.
     * 
     * @param t
     *            a type
     * @param sym
     *            a symbol
     */
    public Type asOuterSuper(Type t, Symbol sym) {
        switch (t.tag) {
        case CLASS:
            do {
                Type s = asSuper(t, sym);
                if (s != null)
                    return s;
                t = t.getEnclosingType();
            } while (t.tag == CLASS);
            return null;
        case ARRAY:
            return isSubtype(t, sym.type) ? sym.type : null;
        case TYPEVAR:
            return asSuper(t, sym);
        case ERROR:
            return t;
        default:
            return null;
        }
    }

    /**
     * Return the base type of t or any of its enclosing types that starts with
     * the given symbol. If none exists, return null.
     * 
     * @param t
     *            a type
     * @param sym
     *            a symbol
     */
    public Type asEnclosingSuper(Type t, Symbol sym) {
        switch (t.tag) {
        case CLASS:
            do {
                Type s = asSuper(t, sym);
                if (s != null)
                    return s;
                Type outer = t.getEnclosingType();
                t = (outer.tag == CLASS) ? outer : (t.tsym.owner.enclClass() != null) ? t.tsym.owner.enclClass().type
                        : Type.noType;
            } while (t.tag == CLASS);
            return null;
        case ARRAY:
            return isSubtype(t, sym.type) ? sym.type : null;
        case TYPEVAR:
            return asSuper(t, sym);
        case ERROR:
            return t;
        default:
            return null;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="memberType">
    /**
     * The type of given symbol, seen as a member of t.
     * 
     * @param t
     *            a type
     * @param sym
     *            a symbol
     */
    public Type memberType(Type t, Symbol sym) {
        return (sym.flags() & STATIC) != 0 ? sym.type : memberType.visit(t, sym);
    }

    // where
    private SimpleVisitor<Type, Symbol> memberType = new SimpleVisitor<Type, Symbol>() {

        public Type visitType(Type t, Symbol sym) {
            return sym.type;
        }

        @Override
        public Type visitWildcardType(WildcardType t, Symbol sym) {
            return memberType(upperBound(t), sym);
        }

        @Override
        public Type visitClassType(ClassType t, Symbol sym) {
            Symbol owner = sym.owner;
            long flags = sym.flags();
            if (((flags & STATIC) == 0) && owner.type.isParameterized()) {
                Type base = asOuterSuper(t, owner);
                if (base != null) {
                    List<Type> ownerParams = owner.type.allparams();
                    List<Type> baseParams = base.allparams();
                    if (ownerParams.nonEmpty()) {
                        if (baseParams.isEmpty()) {
                            // then base is a raw type
                            return erasure(sym.type);
                        } else {
                            return subst(sym.type, ownerParams, baseParams);
                        }
                    }
                }
            }
            return sym.type;
        }

        @Override
        public Type visitTypeVar(TypeVar t, Symbol sym) {
            return memberType(t.bound, sym);
        }

        @Override
        public Type visitErrorType(ErrorType t, Symbol sym) {
            return t;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isAssignable">
    public boolean isAssignable(Type t, Type s) {
        return isAssignable(t, s, Warner.noWarnings);
    }

    /**
     * Is t assignable to s?<br>
     * Equivalent to subtype except for constant values and raw types.<br>
     * (not defined for Method and ForAll types)
     */
    public boolean isAssignable(Type t, Type s, Warner warn) {
        if (t.tag == ERROR)
            return true;
        if (t.tag <= INT && t.constValue() != null) {
            int value = ((Number) t.constValue()).intValue();
            switch (s.tag) {
            case BYTE:
                if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
                    return true;
                break;
            case CHAR:
                if (Character.MIN_VALUE <= value && value <= Character.MAX_VALUE)
                    return true;
                break;
            case SHORT:
                if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
                    return true;
                break;
            case INT:
                return true;
            case CLASS:
                switch (unboxedType(s).tag) {
                case BYTE:
                case CHAR:
                case SHORT:
                    return isAssignable(t, unboxedType(s), warn);
                }
                break;
            }
        }
        return isConvertible(t, s, warn);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="erasure">
    /**
     * The erasure of t {@code |t|} -- the type that results when all type
     * parameters in t are deleted.
     */
    public Type erasure(Type t) {
        if (t.tag <= lastBaseTag)
            return t; /* fast special case */
        else
            return erasure.visit(t);
    }

    // where
    private UnaryVisitor<Type> erasure = new UnaryVisitor<Type>() {
        public Type visitType(Type t, Void ignored) {
            if (t.tag <= lastBaseTag)
                return t; /* fast special case */
            else
                return t.map(erasureFun);
        }

        @Override
        public Type visitWildcardType(WildcardType t, Void ignored) {
            return erasure(upperBound(t));
        }

        @Override
        public Type visitClassType(ClassType t, Void ignored) {
            return t.tsym.erasure(Types.this);
        }

        @Override
        public Type visitTypeVar(TypeVar t, Void ignored) {
            return erasure(t.bound);
        }

        @Override
        public Type visitErrorType(ErrorType t, Void ignored) {
            return t;
        }
    };

    private Mapping erasureFun = new Mapping("erasure") {
        public Type apply(Type t) {
            return erasure(t);
        }
    };

    public List<Type> erasure(List<Type> ts) {
        return Type.map(ts, erasureFun);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="makeCompoundType">
    /**
     * Make a compound type from non-empty list of types
     * 
     * @param bounds
     *            the types from which the compound type is formed
     * @param supertype
     *            is objectType if all bounds are interfaces, null otherwise.
     */
    public Type makeCompoundType(List<Type> bounds, Type supertype) {
        ClassSymbol bc = new ClassSymbol(ABSTRACT | PUBLIC | SYNTHETIC | COMPOUND | ACYCLIC, Type.moreInfo ? names
                .fromString(bounds.toString()) : names.empty, syms.noSymbol);
        if (bounds.head.tag == TYPEVAR)
            // error condition, recover
            bc.erasure_field = syms.objectType;
        else
            bc.erasure_field = erasure(bounds.head);
        bc.members_field = new Scope(bc);
        ClassType bt = (ClassType) bc.type;
        bt.allparams_field = List.nil();
        if (supertype != null) {
            bt.supertype_field = supertype;
            bt.interfaces_field = bounds;
        } else {
            bt.supertype_field = bounds.head;
            bt.interfaces_field = bounds.tail;
        }
        assert bt.supertype_field.tsym.completer != null || !bt.supertype_field.isInterface() : bt.supertype_field;
        return bt;
    }

    /**
     * Same as {@link #makeCompoundType(List,Type)}, except that the second
     * parameter is computed directly. Note that this might cause a symbol
     * completion. Hence, this version of makeCompoundType may not be called
     * during a classfile read.
     */
    public Type makeCompoundType(List<Type> bounds) {
        Type supertype = (bounds.head.tsym.flags() & INTERFACE) != 0 ? supertype(bounds.head) : null;
        return makeCompoundType(bounds, supertype);
    }

    /**
     * A convenience wrapper for {@link #makeCompoundType(List)}; the arguments
     * are converted to a list and passed to the other method. Note that this
     * might cause a symbol completion. Hence, this version of makeCompoundType
     * may not be called during a classfile read.
     */
    public Type makeCompoundType(Type bound1, Type bound2) {
        return makeCompoundType(List.of(bound1, bound2));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="supertype">
    public Type supertype(Type t) {
        return supertype.visit(t);
    }

    // where
    private UnaryVisitor<Type> supertype = new UnaryVisitor<Type>() {

        public Type visitType(Type t, Void ignored) {
            // A note on wildcards: there is no good way to
            // determine a supertype for a super bounded wildcard.
            return null;
        }

        @Override
        public Type visitClassType(ClassType t, Void ignored) {
            if (t.supertype_field == null) {
                Type supertype = ((ClassSymbol) t.tsym).getSuperclass();
                // An interface has no superclass; its supertype is Object.
                if (t.isInterface())
                    supertype = ((ClassType) t.tsym.type).supertype_field;
                if (t.supertype_field == null) {
                    List<Type> actuals = classBound(t).allparams();
                    List<Type> formals = t.tsym.type.allparams();
                    if (actuals.isEmpty()) {
                        if (formals.isEmpty())
                            // Should not happen. See comments below in
                            // interfaces
                            t.supertype_field = supertype;
                        else
                            t.supertype_field = erasure(supertype);
                    } else {
                        t.supertype_field = subst(supertype, formals, actuals);
                    }
                }
            }
            return t.supertype_field;
        }

        /**
         * The supertype is always a class type. If the type variable's bounds
         * start with a class type, this is also the supertype. Otherwise, the
         * supertype is java.lang.Object.
         */
        @Override
        public Type visitTypeVar(TypeVar t, Void ignored) {
            if (t.bound.tag == TYPEVAR || (!t.bound.isCompound() && !t.bound.isInterface())) {
                return t.bound;
            } else {
                return supertype(t.bound);
            }
        }

        @Override
        public Type visitArrayType(ArrayType t, Void ignored) {
            if (t.elemtype.isPrimitive() || isSameType(t.elemtype, syms.objectType))
                return arraySuperType();
            else
                return new ArrayType(supertype(t.elemtype), t.tsym);
        }

        @Override
        public Type visitErrorType(ErrorType t, Void ignored) {
            return t;
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="interfaces">
    /**
     * Return the interfaces implemented by this class.
     */
    public List<Type> interfaces(Type t) {
        return interfaces.visit(t);
    }

    // where
    private UnaryVisitor<List<Type>> interfaces = new UnaryVisitor<List<Type>>() {

        public List<Type> visitType(Type t, Void ignored) {
            return List.nil();
        }

        @Override
        public List<Type> visitClassType(ClassType t, Void ignored) {
            if (t.interfaces_field == null) {
                List<Type> interfaces = ((ClassSymbol) t.tsym).getInterfaces();
                if (t.interfaces_field == null) {
                    assert t != t.tsym.type : t.toString();
                    List<Type> actuals = t.allparams();
                    List<Type> formals = t.tsym.type.allparams();
                    if (actuals.isEmpty()) {
                        if (formals.isEmpty()) {
                            // In this case t is not generic (nor raw).
                            // So this should not happen.
                            t.interfaces_field = interfaces;
                        } else {
                            t.interfaces_field = erasure(interfaces);
                        }
                    } else {
                        t.interfaces_field = upperBounds(subst(interfaces, formals, actuals));
                    }
                }
            }
            return t.interfaces_field;
        }

        @Override
        public List<Type> visitTypeVar(TypeVar t, Void ignored) {
            if (t.bound.isCompound())
                return interfaces(t.bound);

            if (t.bound.isInterface())
                return List.of(t.bound);

            return List.nil();
        }
    };

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="isDerivedRaw">
    Map<Type, Boolean> isDerivedRawCache = new HashMap<Type, Boolean>();

    public boolean isDerivedRaw(Type t) {
        Boolean result = isDerivedRawCache.get(t);
        if (result == null) {
            result = isDerivedRawInternal(t);
            isDerivedRawCache.put(t, result);
        }
        return result;
    }

    public boolean isDerivedRawInternal(Type t) {
        if (t.isErroneous())
            return false;
        return t.isRaw() || supertype(t) != null && isDerivedRaw(supertype(t)) || isDerivedRaw(interfaces(t));
    }

    public boolean isDerivedRaw(List<Type> ts) {
        List<Type> l = ts;
        while (l.nonEmpty() && !isDerivedRaw(l.head))
            l = l.tail;
        return l.nonEmpty();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="setBounds">
    /**
     * Set the bounds field of the given type variable to reflect a (possibly
     * multiple) list of bounds.
     * 
     * @param t
     *            a type variable
     * @param bounds
     *            the bounds, must be nonempty
     * @param supertype
     *            is objectType if all bounds are interfaces, null otherwise.
     */
    public void setBounds(TypeVar t, List<Type> bounds, Type supertype) {
        if (bounds.tail.isEmpty())
            t.bound = bounds.head;
        else
            t.bound = makeCompoundType(bounds, supertype);
        t.rank_field = -1;
    }

    /**
     * Same as {@link #setBounds(Type.TypeVar,List,Type)}, except that third
     * parameter is computed directly. Note that this test might cause a symbol
     * completion. Hence, this version of setBounds may not be called during a
     * classfile read.
     */
    public void setBounds(TypeVar t, List<Type> bounds) {
        Type supertype = (bounds.head.tsym.flags() & INTERFACE) != 0 ? supertype(bounds.head) : null;
        setBounds(t, bounds, supertype);
        t.rank_field = -1;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getBounds">
    /**
     * Return list of bounds of the given type variable.
     */
    public List<Type> getBounds(TypeVar t) {
        if (t.bound.isErroneous() || !t.bound.isCompound())
            return List.of(t.bound);
        else if ((erasure(t).tsym.flags() & INTERFACE) == 0)
            return interfaces(t).prepend(supertype(t));
        else
            // No superclass was given in bounds.
            // In this case, supertype is Object, erasure is first interface.
            return interfaces(t);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="classBound">
    /**
     * If the given type is a (possibly selected) type variable, return the
     * bounding class of this type, otherwise return the type itself.
     */
    public Type classBound(Type t) {
        return classBound.visit(t);
    }

    // where
    private UnaryVisitor<Type> classBound = new UnaryVisitor<Type>() {

        public Type visitType(Type t, Void ignored) {
            return t;
        }

        @Override
        public Type visitClassType(ClassType t, Void ignored) {
            Type outer1 = classBound(t.getEnclosingType());
            if (outer1 != t.getEnclosingType())
                return new ClassType(outer1, t.getTypeArgume
