/**
 *
 * @author dustinchilson
 * @creation_date Feb 17, 2010
 * @description ~~~~~~~~~
 *
 *
 * ~~~~~~~~~~~~~~~~~~~~~~
 */

public class Lab14C {
  public static void main(String[] args) {
    System.out.println("Area of circle = " + area(2.5));
    System.out.println("Area of rectangle = " + area(2 , 5));  // YOUR CALL GOES HERE
  }
  public static double area(double radius) {
    if (radius > 0)
      return Math.PI * radius * radius;
    else
      return 0;
  }

	public static double area(double width, double height) {
		if (width > 0 && height > 0) {
	      return height * width;
	    } else {
	      return 0;
		}
	}


}

