this.y = y;
}

//    @Override
//    public boolean equals(ComplexVector2D complexVector2D1) {
//        if (this.x.equals(complexVector2D1.getX()) &amp;&amp; (this.y.equals(complexVector2D1.getY()))) {
@Override
public boolean equals(Object obj) {
if(obj instanceof ComplexVector2D)
return getX().equals(((ComplexVector2D) obj).getX())&amp;&amp; getY().equals(((ComplexVector2D) obj).getY());

