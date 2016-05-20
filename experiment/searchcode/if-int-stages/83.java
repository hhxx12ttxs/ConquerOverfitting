/*
 * Stages.java
 * An application to find age group depending on age
 * Julian Webb
 * ICTP12
 * 07/11/11
 */
import java.util.Scanner; //use package that lets us collect data from user

/*
 * The Stages class finds age group depending on age
 */
public class Stages { //start class definition

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int age; //Variable for user's age
        System.out.print("Enter an age: "); //Ask user for age
        age = input.nextInt(); //Collect age
        if (age <= 0) { //If age is less than or equal to 0
            System.out.print("Not born"); //Display 'Not Born'
        } else if (age <= 5) { //If age is less than or equal to 5
            System.out.print("Toddler"); //Display 'Toddler'
        } else if (age <= 10) { //If age is less than or equal to 10
            System.out.print("Child"); //Display 'Child'
        } else if (age <= 12) { //If age is less than or equal to 12
            System.out.print("Preteen"); //Display 'Preteen'
        } else if (age <= 18) { //If age is less than or equal to 18
            System.out.print("Teen"); //Display 'Teen'
        } else { //If age is anything else
            System.out.print("Adult"); //Display 'Adult'
        } //endif
    } //end class definition
}
