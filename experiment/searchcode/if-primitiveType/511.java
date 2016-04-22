/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modelinglab.ocl.core.ast.types;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import org.modelinglab.ocl.core.ast.utils.ClassifierVisitor;
import org.modelinglab.ocl.core.ast.utils.OclVisitor;

/**
 *
 * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
 */
@Immutable
public class PrimitiveType extends DataType {

    private static final long serialVersionUID = 1L;
    public static final PrimitiveType BOOLEAN = new PrimitiveType(PrimitiveKind.BOOLEAN);
    public static final PrimitiveType INTEGER = new PrimitiveType(PrimitiveKind.INTEGER);
    public static final PrimitiveType REAL = new PrimitiveType(PrimitiveKind.REAL);
    public static final PrimitiveType STRING = new PrimitiveType(PrimitiveKind.STRING);
    public static final PrimitiveType UNLIMITED_NATURAL = new PrimitiveType(PrimitiveKind.UNLIMITED_NATURAL);
    private final PrimitiveKind kind;
    private final ClassifierType classifierType;

    private PrimitiveType(PrimitiveKind kind) {
        this.kind = kind;
        classifierType = new ClassifierType(this);
    }

    /**
     * @param primitiveKind
     * @return
     * @deprecated you should use the public final static attributes like {@link #BOOLEAN}, {@link #STRING}, etc.
     */
    @Deprecated
    public static PrimitiveType getInstance(PrimitiveKind primitiveKind) {
        switch (primitiveKind) {
            case BOOLEAN:
                return BOOLEAN;
            case INTEGER:
                return INTEGER;
            case REAL:
                return REAL;
            case STRING:
                return STRING;
            case UNLIMITED_NATURAL:
                return UNLIMITED_NATURAL;
            default:
                throw new AssertionError(primitiveKind + " not expected");
        }
    }

    @Override
    public ClassifierType getClassifierType() {
        return classifierType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrimitiveType other = (PrimitiveType) obj;
        if (this.kind != other.kind) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        return hash;
    }

    @Override
    protected boolean conformsToProtected(Classifier conformsTarget, TemplateRestrictions instanciatedTemplates) {
        if (!(conformsTarget instanceof PrimitiveType)) {
            return false;
        }
        final PrimitiveType otherPrimitiveType = (PrimitiveType) conformsTarget;
        if (this.getPrimitiveKind() == otherPrimitiveType.getPrimitiveKind()) {
            return true;
        }
        if (this.getPrimitiveKind() == PrimitiveKind.INTEGER && otherPrimitiveType.getPrimitiveKind() == PrimitiveKind.REAL) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return kind.toString();
    }

    public PrimitiveKind getPrimitiveKind() {
        return kind;
    }

    @Override
    public <Result, Arg> Result accept(ClassifierVisitor<Result, Arg> visitor, Arg arguments) {
        return visitor.visit(this, arguments);
    }

    @Override
    public <Result, Arg> Result accept(OclVisitor<Result, Arg> visitor, Arg arguments) {
        return visitor.visit(this, arguments);
    }

    @Override
    public List<Classifier> getSuperClassifiers() {
        List<Classifier> result = new ArrayList<Classifier>(2);

        if (kind == PrimitiveKind.INTEGER) {
            result.add(PrimitiveType.getInstance(PrimitiveKind.REAL));
        }

        result.add(AnyType.getInstance());
        
        return result;
    }

    @Override
    public PrimitiveType clone() {
        return this;
    }

    public enum PrimitiveKind {

        BOOLEAN, STRING, INTEGER, UNLIMITED_NATURAL, REAL;

        @Override
        public String toString() {
            switch (this) {
                case BOOLEAN:
                    return "Boolean";
                case STRING:
                    return "String";
                case INTEGER:
                    return "Integer";
                case UNLIMITED_NATURAL:
                    return "UnlimitedNatural";
                case REAL:
                    return "Real";
                default:
                    throw new AssertionError();
            }
        }
    }
}

