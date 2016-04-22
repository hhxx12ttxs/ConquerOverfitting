package ntu.asu.rduboveckij.model.external;

/**
 * @author andrus.god
 * @since 16.06.2014
 */
public final class PrimitiveType extends DataType {
    private final PrimitiveEnum primitiveEnum;

    public PrimitiveType(PrimitiveEnum primitiveEnum) {
        super(primitiveEnum.getName());
        this.primitiveEnum = primitiveEnum;
    }

    public PrimitiveEnum getPrimitiveEnum() {
        return primitiveEnum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PrimitiveType that = (PrimitiveType) o;

        return primitiveEnum == that.primitiveEnum;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (primitiveEnum.hashCode());
        return result;
    }
}
