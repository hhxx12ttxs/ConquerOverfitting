/**
 *
 * @author Dustin Chilson
 * @creation_date 03-02-2010
 * @description ~~~~~~~~~
 *
 *
 * ~~~~~~~~~~~~~~~~~~~~~~
 */
public class Lab20 {
  public static void main(String[] args) {

	System.out.println("\n----A----");
    	Double d1 = new Double("123.45");
    	Double d2 = new Double(123.45);
		if (d1.equals(d2)) {
      		System.out.println("The values are equal");
    	} else {
      		System.out.println("The values are different");
		}
	System.out.println("\n----B----");
		byte[] data = new byte[1];
    	try {
			System.out.print("Enter a letter, a digit, or a special character: ");
      		System.in.read(data);
      		char key = (char)data[0];
      		System.out.println("You pressed: " + key);
      		if (Character.isLetter(key)) {
				System.out.println("That was a letter");
      		} else if (Character.isDigit(key)) {
        		System.out.println("That was a digit");
      		} else {
        		System.out.println("That was a special character");
      		}
    	} catch (Exception err) {
      		System.out.println("Input Error!");
    	}

	System.out.println("\n----C----");
		byte[] data2 = new byte[20];
	    try {
	     	System.out.print("Enter radius: ");
	      	System.in.read(data2);
	      	String radiusAsString = new String(data2);
	      	float radius = Float.parseFloat(radiusAsString);
	      	if (radius > 0) {
	        	double area = Math.PI * Math.pow(radius, 2);
	        	System.out.println("Area is " + area);
	      	} else{
	        	System.out.println("Invalid radius");
			}
	    } catch (Exception err) {
	      	System.out.println("Input Error!");
	    }

  }
}


