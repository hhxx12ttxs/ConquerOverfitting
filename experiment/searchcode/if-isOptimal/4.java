import java.util.ArrayList;
import java.util.List;


/*This class generates all possible optimal schedules which a student can take in 
 * a term, where each optimal schedule satisfies all his personalized constraints too, 
 * in addition to the minimum constraints satisfied by a valid schedule. 
 */
public class ScheduleGenerator {
	
/*This represents the underlying domain of all courses offered for the term, from which
 * a student can pick his optimal schedule (set of courses satisfying all his constraints)
 * from.  	
 */
	private List<Course> allcourses; 
	
/*The maximum credit (where credit is the sum of the credits of all the courses in a schedule) 
and minimum credit allowed for the term. 
*/	 
	private int minCredit;
	private int maxCredit;
	
/* finalScheduleSet models the list of valid schedules, where any valid schedule in that list  
 * can be taken by a student if he doesn't care of any personalized constraints (like for example he doesn't 
 * care if the courses are too closely spaced.)
 * finalOptimalScheduleSet is a list of optimal schedules where any schedule in that list satisfies 
 *  all the constraints (including the personalized ones) required by a student. 
 *  Thus an optimal schedule is always a valid schedule, but not the other way round.    
 */ 
	private List<Schedule> finalScheduleSet = new ArrayList<Schedule>();  
	private List<Schedule> finalOptimalScheduleSet = new ArrayList<Schedule>();
	
	public ScheduleGenerator(List<Course> allcourses, int minCredit, int maxCredit) {
		this.minCredit = minCredit; 
		this.maxCredit = maxCredit;
		this.allcourses = allcourses; 
	}
	
/* The following method generates all possible valid schedules from the underlying domain of 
 * all courses offered for the term. Or in other words, this method generates  
 * 	the finalScheduleSet. I apply a recursive approach in generating all possible schedules. 
 * While I perform a left to right scan of the "allcourses" list representing the underlying domain, 
 * at each stage of recursion, I make a choice either to add the course encountered at the 
 * current index in the "allcourses" list to the schedule which I am composing, or not to add it. Thus  
 * each round of recursion gives rise to a binary tree, each branch represents a decision. The 
 * recursion reaches its base case, when either I am finished traversing the whole "allcourses" list
 * or if the sum of the credits of the yet so far added courses to the schedule which I am composing, exceeds 
 * or becomes equal to the maximum allowable credit limit for the term. Once the base case is reached,
 *  I check if the sum of the credits of the courses added to my composed falls within the minimum and maximum 
 *  allowable credit range. If it is so, I consider it as a valid schedule and add it to the finalScheduleSet
 *  as a member. Otherwise I don't add it, and return from the current recursive call.  
 *  
 *  */
	
	private void generatesAllSchedules(List<Course> output, int index, int sum) {
		if (index == allcourses.size() || sum >= maxCredit) {
			if (sum >= minCredit && sum < maxCredit) {
				Schedule schedule = new Schedule(); 
				schedule.setCoursestaken(output);
				finalScheduleSet.add(schedule); 
			//	for (Course crse : output)
			//		System.out.print("  "+crse.getCourseName());
			//	System.out.println(); 
			//	output.clear(); 
			}  	
			
			return; 
		}
		
		List<Course> output1 = new ArrayList<Course>(output.size());
		for (Course cr : output)
			output1.add(cr); 
		output.add(allcourses.get(index));
		generatesAllSchedules(output, index+1, 
							 sum + allcourses.get(index).getCredit());
		generatesAllSchedules(output1, index+1, sum); 
	}

/* The following method generates all possible optimal schedules. First it invokes  
 * "generatesAllSchedules" method to generate all valid schedules and then, selects from them, the 
 * ones which are optimal, based on the definition of the isOPtimal method.  
 */
	
	public void generateAllOptimalSchedules() {
		List<Course> output = new ArrayList<Course>();
		generatesAllSchedules(output, 0, 0); 
		for (Schedule schedule : finalScheduleSet) {
			if (schedule.isOptimal()) {
				finalOptimalScheduleSet.add(schedule);
			}
		}
		
	}
	
//Displays all optimal schedules on the console.	
	public void printSchedules() {
		System.out.println("Printing allowable schedules ");
		for (Schedule lcr : finalOptimalScheduleSet) {
			for (Course cr : lcr.getCoursestaken()) {
				System.out.print(" "+cr.getCourseName()); 
			}
			System.out.println(); 
		}
	}


}

