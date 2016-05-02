import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/*A Schedule represents a sub-collection of courses which a student 
 * can take, out of all possible courses offered by his department in a
 * quarter or semester (term). A valid schedule must satisfy some minimum constraints as well as
 * may satisfy some additional constraints which a student  
 * is looking for. A schedule is modeled by a list of courses taken, along with 
 * two other properties, the highest credit and the lowest credit allowed for the term.
 * Here a credit represents the sum of the credits or units of all the courses in the schedule.
 * A student can take as many courses or as least courses as part of his valid schedule in a term, as 
 * long as the sum of the credits of these courses adds up to a number which falls within the highest 
 * credit and lowest credit range and as long as some temporal constraints are satisfied. 
 */

public class Schedule {

  private List<Course> coursestaken = new ArrayList<Course>(); 
  private static int highestCreditAllowed = 16;
  private static int lowestCreditAllowed = 12;
  
//Populates a course to a schedule.  
  public void addCourse(Course cr) {
	  coursestaken.add(cr); 
  }
  
  //Prints out the constituent courses in a schedule.
  public void print() {
	  for (Course cr : coursestaken) {
		  System.out.print(cr.getCourseName() + " "); 
	  }
  }
  
  
 /*Checks if a schedule is optimal. An optimal schedule is one which satisfies all 
  * the personalized constraints which a student is looking for, in addition to   
  * the basic constraints satisfied by a valid schedule. Thus an optimal schedule is 
  * always a valid schedule, but not the other way round. 
  * For example a schedule is valid if it contains no overlapping (in time) courses, and it is optimal too, if 
  * it doesn't contain courses which are too closely spaced together in a day.   
  */
  public boolean isOptimal(){
 	if(!containsOverlappingCourses() && !containsCloselySpacedCourses() && !containsMoreThanTwoPerDay()
 		&& !containsCoursetakesPlaceDuringLunch())
	    return true;
 	else 
	    return false; 
  }
	  
  /*Checks if a schedule consists of a pair of courses which are too closely  
   * spaced with each other. If course A has a start time within two hours of the  
   * end time of course B, or vice versa, then they are defined to be closely spaced 
    and eliminated as schedule candidates.
   */
  public boolean containsCloselySpacedCourses() {
	  for(int i=0;i<coursestaken.size();i++){
	    	for(int j=i+1;j<coursestaken.size();j++){
	    		List<CourseTiming.DaysofWeek> dwC1 = new ArrayList<CourseTiming.DaysofWeek>();
	    		dwC1 = coursestaken.get(i).getCourseTiming().getDaysOfWeek(); 
	    		List<CourseTiming.DaysofWeek> dwC2 = new ArrayList<CourseTiming.DaysofWeek>();
	    		dwC2 = coursestaken.get(j).getCourseTiming().getDaysOfWeek();
	    		for (CourseTiming.DaysofWeek d1 : dwC1) {
	    			for (CourseTiming.DaysofWeek d2 : dwC2) {
	    				if (d1.equals(d2)) {
	    					if (Math.abs(coursestaken.get(i).getCourseTiming().getStarthours() -
	    							coursestaken.get(j).getCourseTiming().getEndhours()) < 2 ||
	    						Math.abs(coursestaken.get(j).getCourseTiming().getStarthours() -
	    	    					coursestaken.get(i).getCourseTiming().getEndhours()) < 2)	
	    							return true; 
	    				}
	    			}
	    		}
	    			
	    		 	    		
	    	}
	    }
	  
	    return false;
  }
  
 //Returns true or false based on if a schedule contains pair of overlapping courses.  
  public boolean containsOverlappingCourses(){
	 
	    for(int i=0;i<coursestaken.size();i++){
	    	for(int j=i+1;j<coursestaken.size();j++){
	    		if(coursestaken.get(i).overlapsWith(coursestaken.get(j)))
	    			  return true;
	    		 	    		
	    	}
	    }
	  
	    return false;
  }

 //Returns true or false based on if a schecdule contains more than 2 courses per day 
  public boolean containsMoreThanTwoPerDay() {
	 Map<CourseTiming.DaysofWeek, Integer> frequencyPerDay = new HashMap<CourseTiming.DaysofWeek, Integer>(); 
	 for (Course cr : coursestaken) {
		 for (CourseTiming.DaysofWeek dw : cr.getCourseTiming().getDaysOfWeek()) {
			 if (frequencyPerDay.containsKey(dw)) {
				 frequencyPerDay.put(dw, frequencyPerDay.get(dw) + 1);
				 if (frequencyPerDay.get(dw) > 2)
					 return true; 
			 }else {
				 frequencyPerDay.put(dw, 1); 
			 }
			
		 }
	 }
	 return false; 
 }
  
 //Returns true or false based on if a course occurs during lunch 
  public boolean containsCoursetakesPlaceDuringLunch() {
	  for (Course cr : coursestaken) {
		  if (cr.takesPlaceDuringLunch())
			  return true; 
	  }
	  return false;
  }
  
 
  //Getters and Setters for the properties of the Schedule class. 
  
  public static int getHighestCreditAllowed() {
	return highestCreditAllowed;
  }

  public static int getLowestCreditAllowed() {
	return lowestCreditAllowed;
  }


  public List<Course> getCoursestaken() {
	return coursestaken;
  }


  public void setCoursestaken(List<Course> coursestaken) {
	this.coursestaken = coursestaken;
  }

}

