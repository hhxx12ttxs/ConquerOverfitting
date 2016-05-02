package a4.s100502028;
import java.util.Scanner; // Using Scanner in the java.util package
public class A42 {
	public static void main(String[] args){
		Scanner input = new Scanner(System.in); // Create an object of the Scanner type
		
		// Prompt the user for six numbers as three points of the triangle
		System.out.print("Please enter three points of the new triangle: ");
		double point1x = input.nextDouble();
		double point1y = input.nextDouble();
		double point2x = input.nextDouble();
		double point2y = input.nextDouble();
		double point3x = input.nextDouble();
		double point3y = input.nextDouble();
		
		System.out.println("\n1.Get the area of the new triangle\n" +
				"2.Get the perimeter of the new triangle\n" +
				"3.Check the new triangle is in the original triangle or not\n" +
				"4.Enter a point and check the point is in the original triangle or not\n" + 
				"5.Exit");
		
		while(true){
			System.out.print("\nChoose the function you want to use: ");
			int choose = input.nextInt();
			if (choose == 5) { // If statement for exit ability
				System.out.println("Thanks for your using!!!");
				break;
			} // end if
			
			// Create an object of Triangle2D and pass the points
			Triangle2D test = new Triangle2D(point1x,point1y,point2x,point2y,point3x,point3y);
			
			switch(choose){ // Switch statement for several cases
				case 1: // Case to calculate area
					System.out.println("The area of the new triangle is " + test.getArea()); // Call method getPerimeter()
					break;
					
				case 2: // Case to calculate perimeter
					System.out.println("The perimeter of the new triangle is " + test.getPerimeter()); // Call method getArea()
					break;
					
				case 3: // Case to check whether the new triangle is in the original triangle or not
					
					// Prompt the user for a new triangle
					System.out.println("Input three points to check whether the new triangle is in the original triangle or not: ");
					double newpoint1x = input.nextDouble();
					double newpoint1y = input.nextDouble();
					double newpoint2x = input.nextDouble();
					double newpoint2y = input.nextDouble();
					double newpoint3x = input.nextDouble();
					double newpoint3y = input.nextDouble();
					
					// Create a new object of Triangle2D and pass new points
					Triangle2D t = new Triangle2D(newpoint1x,newpoint1y,newpoint2x,newpoint2y,newpoint3x,newpoint3y);
					if(test.contains(t) == true) // Call the method contains(Triangle2D t)
						System.out.println("The new triangle is in the original triangle !!!");
					else
						System.out.println("The new triangle is NOT in the original triangle !!!");
					break;
					
				case 4: // Case to check whether the point is in the original triangle or not 
					
					// Prompt the user for a new point
					System.out.println("Input a point to check whether the point is in the original triangle or not: ");
					double checkPx = input.nextDouble();
					double checkPy = input.nextDouble();
					
					// Create a new object of MyPoint and pass new points
					MyPoint p = new MyPoint(checkPx,checkPy);
					if(test.contains(p)==true) // Call the method contains(MyPoint p)
						System.out.println("The new point is in the original triangle (0,0), (17,6), (10, 15)!!!");
					else
						System.out.println("The new point is NOT in the original triangle (0,0), (17,6), (10, 15)!!!");
					break;
					
				default: // Catch all other characters
					System.out.println("Errors!!!Invalid input of choose!!!");
					System.exit(0);
			} // End switch
		} // End while loop
	} // End main method
}  // End class A42

