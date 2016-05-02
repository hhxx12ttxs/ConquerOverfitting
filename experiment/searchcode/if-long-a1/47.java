/*
 * This class keeps all the methods needed to calculate soft constraints 
 * this class is our fwert
 * It uses the value of it's parent's partial schedule and the assign that is being adding
 * this allows it to not check things multiple times as we only need to know how the new assign will effect the solution
 *
 */
package CPSC433;

import CPSC433.Environment;
import PredicateObjects.Assign;
import PredicateObjects.Lecture;
import PredicateObjects.Session;
import PredicateObjects.Student;
import java.util.*;

/**
 *
 * @author JohnLaptop
 */
public class SoftConstraintChecker
{

    private static final int PENALTY_S1 = 100;
    private static final int PENALTY_S2 = 20;
    private static final int PENALTY_S3 = 50;
    private static final int PENALTY_S4 = 50;
    private static final int PENALTY_S5 = 50;
    private static final int PENALTY_S6 = 20;
    private static final int PENALTY_S7 = 5;
    //keeps track of how many times we violate each constraint
    private int violationCountS1;
    private int violationCountS2;
    private int violationCountS3;
    private int violationCountS4;
    private int violationCountS5;
    private int violationCountS6;
    private int violationCountS7;
    public int totalUtility;

    public SoftConstraintChecker()
    {
        violationCountS1 = 0;
        violationCountS2 = 0;
        violationCountS3 = 0;
        violationCountS4 = 0;
        violationCountS5 = 0;
        violationCountS6 = 0;
        violationCountS7 = 0;
        totalUtility = 0;
    }

    public void getUtility(Set<Assign> workingSet)
    {
        this.totalUtility = soft12367(workingSet) + soft4(workingSet) + soft5(workingSet);
    }

    public static int getStaticUtility(Set<Assign> workingSet, Assign newItem)
    {
        return staticUtility(workingSet, newItem);
    }

    //S1: No student writes more than one exam in a timeslot (no direct conflict)
    //S2: No instructor invigulates in more than one room at the same time (no direct conflict)
    //S3: Every lecture for the same course should have the same exam timeslot
    //S6: All the exams taking place in a particular session should have the same length 
    //S7: Every exam in a session should take up the full time of the sesssion 
    private int soft12367(Set<Assign> workingSet)
    {
        List<Assign> theList = new ArrayList<>(workingSet);
        int badness = 0;

        //checks pairs of assign statements
        for (int outer = 0; outer < theList.size(); outer++)
        {
            Assign a1 = theList.get(outer);
            //S7
            if (a1.getSession().getLength() != a1.getLecture().getExamLength())
            {
                badness += PENALTY_S7;
                this.violationCountS7++;
            }

            for (int inner = outer + 1; inner < theList.size(); inner++)
            {
                Assign a2 = theList.get(inner);
                if (sessionOverlap(a1.getSession(), a2.getSession()))
                {
                    //s1
                    Set<Student> intersection = new HashSet<>(a1.getLecture().getStudents());
                    intersection.retainAll(a2.getLecture().getStudents());
                    badness += intersection.size() * PENALTY_S1;
                    this.violationCountS1 += intersection.size();
                    //s2
                    if (a1.getLecture().getInstructor() != null && a1.getLecture().getInstructor() == a2.getLecture().getInstructor())
                    {
                        badness += PENALTY_S2;
                        this.violationCountS2++;
                    }
                    //s3
                    if (a1.getCourse() == a2.getCourse() && a1.getSession() != a2.getSession())
                    {
                        badness += PENALTY_S3;
                        this.violationCountS3++;
                    }
                    //s6
                    if ((a1.getSession() == a2.getSession()) && (a1.getLecture().getExamLength() != a1.getLecture().getExamLength()))
                    {
                        if (a1 != null && a2 != null)
                        {
                            badness += PENALTY_S6;
                            this.violationCountS6++;
                        }
                    }
                }
            }
        }
        return badness;
    }

    private static int staticUtility(Set<Assign> set, Assign item)
    {
        Map<String, Long> students = new HashMap<>();
        int badness = 0;

        for (Student s : item.getLecture().getStudents())
        {
            students.put(s.getName(), new Long(0));
        }
        //S7: Every exam in a session should take up the full time of the sesssion 
        if (item.getSession().getLength() != item.getLecture().getExamLength())
        {
            badness += PENALTY_S7;
        }

        for (Assign assign : set)
        {
            if (sessionOverlap(item.getSession(), assign.getSession()))
            {
                //S1: No student writes more than one exam in a timeslot (no direct conflict)
                Set<Student> intersection = new HashSet<>(item.getLecture().getStudents());
                intersection.retainAll(assign.getLecture().getStudents());
                badness += intersection.size() * PENALTY_S1;
                //S2: No instructor invigulates in more than one room at the same time (no direct conflict)
                if (item.getLecture().getInstructor() != null && item.getLecture().getInstructor() == assign.getLecture().getInstructor())
                {
                    badness += PENALTY_S2;
                }
                //S3: Every lecture for the same course should have the same exam timeslot
                if (item.getCourse() == assign.getCourse() && item.getSession() != assign.getSession())
                {
                    badness += PENALTY_S3;
                }
                //S6: All the exams taking place in a particular session should have the same length 
                if ((item.getSession() == assign.getSession()) && (item.getLecture().getExamLength() != assign.getLecture().getExamLength()))
                {
                    if (item != null && assign != null)
                    {
                        badness += PENALTY_S6;
                    }
                }
            }
            //S4
            if (sessionOverlapDay(item.getSession(), assign.getSession()))
            {
                for (Student s : assign.getLecture().getStudents())
                {
                    if(students.containsKey(s.getName()))
                    {
                        students.put(s.getName(), students.get(s.getName()) + assign.getLecture().getExamLength());
                    }
                }
            }
        }
        
        for(Long l : students.values())
        {
            if(l > 5)
            {
                badness += PENALTY_S7;
            }
        }
        
        return badness;
    }
    
    //No student writes for longer than 5 hours in a single day 
    private int soft4(Set<Assign> workingSet)
    {
        int badness = 0;
        Map<String, List<Lecture>> sesByDay = new HashMap<>();

        for (Assign ass : workingSet)
        {
            String key = ass.getSession().getDay().getName();
            if (!sesByDay.containsKey(key))
            {
                sesByDay.put(key, new ArrayList<Lecture>());
            }

            sesByDay.get(key).add(ass.getLecture());
        }

        for (List<Lecture> lecList : sesByDay.values())
        {
            Map<String, Long> studentHrs = new HashMap<>();
            for (Lecture lec : lecList)
            {
                for (Student stu : new ArrayList<Student>(lec.getStudents()))
                {
                    if (!studentHrs.containsKey(stu.getName()))
                    {
                        studentHrs.put(stu.getName(), new Long(0));
                    }
                    Long thingy = studentHrs.get(stu.getName());
                    thingy += lec.getExamLength();
                    studentHrs.put(stu.getName(), thingy);
                }
            }
            for (Long check : studentHrs.values())
            {
                if (check > 5)
                {
                    badness += PENALTY_S4;
                    this.violationCountS4++;
                }
            }
        }
        return badness;
    }
    //No student should write exams with no break between them 
    // we decided that there will be no breaks for those ungreatfull bastards.

    private static int soft5(Set<Assign> workingSet)
    {
        return 0;
    }

    public int getTotalUtility()
    {
        return totalUtility;
    }

    public int getViolationCountS1()
    {
        return violationCountS1;
    }

    public int getViolationCountS2()
    {
        return violationCountS2;
    }

    public int getViolationCountS3()
    {
        return violationCountS3;
    }

    public int getViolationCountS4()
    {
        return violationCountS4;
    }

    public int getViolationCountS5()
    {
        return violationCountS5;
    }

    public int getViolationCountS6()
    {
        return violationCountS6;
    }

    public int getViolationCountS7()
    {
        return violationCountS7;
    }

    private static boolean sessionOverlap(Session s1, Session s2)
    {
        return (s1.getDay() == s2.getDay()) && (s1.getTime() - s2.getTime() >= 0 && s1.getTime() - s2.getTime() < s1.getLength());
    }

    private static boolean sessionOverlapDay(Session s1, Session s2)
    {
        return (s1.getDay() == s2.getDay());
    }
}

