/**
 * @autor Ekaterina Bobrinskaya
 * 401
 * 054
 */
public class ComplexVector2D {
    private ComplexNumber x, y;

    public ComplexVector2D() {
        this(new ComplexNumber(), new ComplexNumber());
    }

    public ComplexVector2D(ComplexNumber x, ComplexNumber y) {
        this.x = x;
        this.y = y;
    }

    public ComplexNumber getX() {
        return x;
    }

    public void setComplexNumberX(ComplexNumber x) {
        this.x = x;
    }

    public ComplexNumber getY() {
        return y;
    }

    public void setComplexNumberY(ComplexNumber y) {
        this.y = y;
    }

//    @Override
//    public boolean equals(ComplexVector2D complexVector2D1) {
//        if (this.x.equals(complexVector2D1.getX()) && (this.y.equals(complexVector2D1.getY()))) {
//            return true;
//        } else {
//            return false;
//        }
//    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ComplexVector2D)
            return getX().equals(((ComplexVector2D) obj).getX())&& getY().equals(((ComplexVector2D) obj).getY());
        return false;
    }
}

