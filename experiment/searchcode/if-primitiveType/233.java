package ntu.asu.rduboveckij.model.external;

import ntu.asu.rduboveckij.util.CommonUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author andrus.god
 * @since 16.06.2014
 */
public abstract class DataType extends Name {
    public DataType(String name) {
        super(name);
    }

    public boolean isPrimitive() {
        return this instanceof PrimitiveType;
    }

    public boolean isComplex() {
        return this instanceof ComplexType;
    }

    public PrimitiveType asPrimitive() {
        return (PrimitiveType) this;
    }

    public ComplexType asComplex() {
        return (ComplexType) this;
    }

    public static DataType valueOf(String name) {
        Optional<PrimitiveEnum> type = PrimitiveEnum.of(CommonUtils.requireNotEmpty(name));
        if (type.isPresent()) return new PrimitiveType(type.get());
        return new ComplexType(name);
    }

    public static boolean isCast(PrimitiveType source, PrimitiveType target) {
        return Objects.requireNonNull(source).getPrimitiveEnum()
                .isCast(Objects.requireNonNull(target).getPrimitiveEnum());
    }
}
