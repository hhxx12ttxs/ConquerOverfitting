public double lowerBound;
public double upperBound;

public ARangeD() {

}

public ARangeD( double lowerBound, double upperBound ) {
this.upperBound = upper;
}

public boolean isIn( double value ) {
if( lowerBound > value )
return false;
if( upperBound <= value )

