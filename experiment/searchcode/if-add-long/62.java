import java.util.Map;
import java.util.ArrayList;
import com.sleepycat.je.*;
import com.sleepycat.persist.*;
import com.sleepycat.persist.PrimaryIndex;
import java.io.*;
import org.json.simple.parser.ParseException;
import java.util.Scanner;

public class EmbeddedDB
{
    private ServerConnector serverConnector;

    private ArrayList<String> categoryChanges, assignmentChanges, sectionChanges, gradeChanges, gradeLetterChanges;

    private Environment env;

    private StoreConfig storeConfig;

    private EntityStore eStore;

    private int numSS = 0, numStudents = 0, numSections = 0, numAssignments = 0, numGrades = 0, numCategories = 0;

    private PrimaryIndex<Long, Student> studentPIndex;
    private PrimaryIndex<Long, Section> sectionPIndex;
    private PrimaryIndex<Long, Category> categoryPIndex;
    private PrimaryIndex<Long, Grade> gradePIndex;
    private PrimaryIndex<Long, Assignment> assignmentPIndex;
    private PrimaryIndex<Long, Sections_Students> sections_studentsPIndex;

    private File AssignmentList, CategoryList, GradeList;

    private PrintWriter pw;

    private BufferedWriter bw;

    private FileWriter fw;

    //Creates DB and Tables needed
    public EmbeddedDB() throws ConnectionException, LoginException, ParseException, ValidationException, IOException
    {
        try
        {
            EnvironmentConfig config = new EnvironmentConfig();
            config.setAllowCreate(true);
            //config.setLocking(true);
            File home = new File("embeddedDB");
            env = new Environment(home, config);
        }
        catch(DatabaseException e)
        {
            System.out.println("An error occured with creating the offline database.");
            //e.printStackTrace();
        }

        storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        
        eStore = new EntityStore(env, "EStore", storeConfig);

        studentPIndex = eStore.getPrimaryIndex(Long.class, Student.class);
        sectionPIndex = eStore.getPrimaryIndex(Long.class, Section.class);
        assignmentPIndex = eStore.getPrimaryIndex(Long.class, Assignment.class);
        categoryPIndex = eStore.getPrimaryIndex(Long.class, Category.class);
        gradePIndex = eStore.getPrimaryIndex(Long.class, Grade.class);
        sections_studentsPIndex = eStore.getPrimaryIndex(Long.class, Sections_Students.class);

        categoryChanges = new ArrayList<String>();
        assignmentChanges = new ArrayList<String>();
        sectionChanges = new ArrayList<String>();
        gradeChanges = new ArrayList<String>();
        gradeLetterChanges = new ArrayList<String>();

        System.out.println("Success.");

        getNumAssignments();
        getNumCategories();
        getNumGrades();
        
        File assignmentCh = new File("assignmentChanges.txt");
        File categoryCh = new File("categoryChanges.txt");
        File gradeCh = new File("gradeChanges.txt");

        if(!assignmentCh.exists())
        {
            fw = new FileWriter(assignmentCh);
            pw = new PrintWriter(fw);
            bw = new BufferedWriter(pw);
            bw.write("");
            bw.close();
        }

        if(!categoryCh.exists())
        {
            fw = new FileWriter(categoryCh);
            pw = new PrintWriter(fw);
            bw = new BufferedWriter(pw);
            bw.write("");
            bw.close();
        }

        if(!gradeCh.exists())
        {
            fw = new FileWriter(gradeCh);
            pw = new PrintWriter(fw);
            bw = new BufferedWriter(pw);
            bw.write("");
            bw.close();
        }

        Scanner scan = new Scanner(assignmentCh);

        while(scan.hasNext())
        {
            assignmentChanges.add(scan.nextLine());
        }

        scan = new Scanner(categoryCh);

        while(scan.hasNext())
        {
            categoryChanges.add(scan.nextLine());
        }

        scan = new Scanner(gradeCh);

        while(scan.hasNext())
        {
            gradeChanges.add(scan.nextLine());
        }


        //System.out.println("Assignment Changes 1: " + assignmentChanges.get(0));
        //System.out.println("Category Changes 1: " + categoryChanges.get(0));
        //System.out.println("Grade Changes 1: " + gradeChanges.get(0));
        
        //Create Students table, Assignments table, Grades table, Sections table, and Categories table
    }

    public void logout() throws ConnectionException, LoginException, ValidationException, ParseException
    {
        serverConnector.logout();
    }

    public void saveDB() throws ConnectionException, LoginException, ValidationException, ParseException, IOException
    {
        try
        {
           if(eStore != null)
           {
               eStore.close();
           }
        }
        catch(DatabaseException e)
        {
            System.out.println("The offline database store could not be closed.");
        }

        try
        {
           if(env != null)
           {
           env.cleanLog();
           env.close();
           }
        }
        catch(DatabaseException e)
        {
            System.out.println("The offline database could not be closed.");
        }


        try
        {
            EnvironmentConfig config = new EnvironmentConfig();
            config.setAllowCreate(true);
            //config.setLocking(true);
            File home = new File("embeddedDB");
            env = new Environment(home, config);
        }
        catch(DatabaseException e)
        {
            System.out.println("An error occured with creating the offline database.");
            //e.printStackTrace();
        }

        storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);

        eStore = new EntityStore(env, "EStore", storeConfig);

        studentPIndex = eStore.getPrimaryIndex(Long.class, Student.class);
        sectionPIndex = eStore.getPrimaryIndex(Long.class, Section.class);
        assignmentPIndex = eStore.getPrimaryIndex(Long.class, Assignment.class);
        categoryPIndex = eStore.getPrimaryIndex(Long.class, Category.class);
        gradePIndex = eStore.getPrimaryIndex(Long.class, Grade.class);
        sections_studentsPIndex = eStore.getPrimaryIndex(Long.class, Sections_Students.class);

        getNumAssignments();
        getNumCategories();
        getNumGrades();

        fw = new FileWriter(new File("assignmentChanges.txt"));
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);

        for(int i = 0; i < assignmentChanges.size(); i++)
        {
            bw.write(assignmentChanges.get(i) + "\n");
        }

        bw.close();

        fw = new FileWriter(new File("categoryChanges.txt"));
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);

        for(int i = 0; i < categoryChanges.size(); i++)
        {
            bw.write(categoryChanges.get(i) + "\n");
        }

        bw.close();

        fw = new FileWriter(new File("gradeChanges.txt"));
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);

        for(int i = 0; i < gradeChanges.size(); i++)
        {
            bw.write(gradeChanges.get(i) + "\n");
        }

        bw.close();

    }

    public void close() throws ConnectionException, LoginException
    {
        try
        {
            serverConnector.logout();
        }
        catch(NullPointerException exc)
        {
            System.out.println("Connection was not open and logout was unnecessary.");
        }

        try
        {
           if(eStore != null)
           {
           eStore.close();
           }
        }
        catch(DatabaseException e)
        {
            System.out.println("The offline database store could not be closed.");
        }

        try
        {
           if(env != null)
           {
           env.cleanLog();
           env.close();
           }
        }
        catch(DatabaseException e)
        {
            System.out.println("The offline database could not be closed.");
        }
    }

    //Sets the connection class for the DB - Passed in as parameter
    public void setConnection(String s)
    {
        serverConnector = ServerConnector.sharedInstance(s);
    }

    public ServerConnector getConnector()
    {
        return serverConnector;
    }

    public void setConnector(ServerConnector sc)
    {
        serverConnector = sc;
    }

    //Logs in using the connection class
    public void login(String name, String password) throws ConnectionException, LoginException, ParseException, ValidationException
    {
        serverConnector.login(name, password);

        //download();

        System.out.println("Data downloaded.");

        //close();

        //System.out.println("Close successful.");
    }
    
    //Calls all of the methods for getting all of the data from
    //the connection class to fill the tables with
    public void download() throws ConnectionException, LoginException, ParseException, ValidationException, IOException
    {
        numStudents = 0;
        numAssignments = 0;
        numGrades = 0;
        numCategories = 0;
        numSections = 0;
        numSS = 0;

        emptyDB();
        
        downloadAssignments();
        downloadGrades();
        downloadCategories();
        downloadSections();

        //downloadStudents();
        //downloadSectionStudents();
        
        getNumAssignments();
        getNumCategories();
        getNumGrades();

        assignmentChanges = new ArrayList<String>();
        categoryChanges = new ArrayList<String>();
        gradeChanges = new ArrayList<String>();

        File assignmentCh = new File("assignmentChanges.txt");
        File categoryCh = new File("categoryChanges.txt");
        File gradeCh = new File("gradeChanges.txt");

        fw = new FileWriter(assignmentCh);
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);
        bw.write("");
        bw.close();

        fw = new FileWriter(categoryCh);
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);
        bw.write("");
        bw.close();

        fw = new FileWriter(gradeCh);
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);
        bw.write("");
        bw.close();
    }

    //Obtains the Map[] of all students in a given section and stores them in the DB
    /*public void downloadStudents() throws ConnectionException, LoginException, ParseException
    {
        Map[] students = (Map[])serverConnector.getStudents();

        for(int i = 0; i < students.length; i++)
        {
            insertStudent(students[i]);
        }
    }*/

    //Obtains the Map[] of all assignments in a given section and stores them in the DB
    public void downloadAssignments() throws ConnectionException, LoginException, ParseException
    {
        Map[] assignments = (Map[])serverConnector.getAssignments();

        for(int i = 0; i < assignments.length; i++)
        {
            insertAssignment(assignments[i]);
            //System.out.println("Assignment " + assignments[i].get("section_id") + ", " + assignments[i].get("id") + " downloaded.");
        }
    }

    //Obtains the Map[] of all grades in a given section and stores them in the DB
    public void downloadGrades() throws ConnectionException, LoginException, ParseException
    {
        Map[] grades = (Map[])serverConnector.getGrades();

        for(int i = 0; i < grades.length; i++)
        {
            insertGrade(grades[i]);
            //System.out.println("Grade downloaded: " + grades[i].get("id"));
        }

        boolean gradeExists = false;

        //Get all sections
        Section[] sections = getSections();
        //Get all students
        for(int i = 0; i < sections.length; i++)
        {
            Student[] students = getStudents(sections[i].getID());

            for(int j = 0; j < students.length; j++)
            {
                Category[] categories = getCategories(sections[i].getID());

                for(int k = 0; k < categories.length; k++)
                {
                    Assignment[] assignments = getAssignments(categories[k].getID());

                    for(int l = 0; l < assignments.length; l++)
                    {
                        Grade[] tempGrades = getGrades(assignments[l].getID());

                        for(int m = 0; m < tempGrades.length; m++)
                        {
                            if(tempGrades[m].getStudentID() == students[j].getID())
                            {
                                gradeExists = true;
                            }
                        }

                        if(!gradeExists)
                        {
                            long numGrades = getNumGrades();
                            gradePIndex.put(new Grade(numGrades, sections[i].getID(), assignments[l].getID(), students[j].getID(), -1));
                            gradeChanges.add(new String("I" + numGrades + "\n"));
                        }

                        gradeExists = false;
                    }
                }
            }
        //Get all categories
        //Get all assignments
        //Get all grades

        }

        //for each student in each section, for each category for each assignment,
        //loop through the grades to see if that student has a grade for that assignment
        //if not, create a new grade with the student's id, the assignment's id, and a null points_earned value
        //if the grade does exist, do nothing
    }

    //Obtains the Map[]s of all categories for each section and stores them in the DB
    //First obtains the section ids from the Sections table, then proceeds in a loop
    //Will first drop all of the data from the categories table
    //Then for each section, pass the id to the connection class
    //Connection class returns a Map[]
    //The Map[] is used in a loop with the insertCategory method to inssert the data into the table
    public void downloadCategories() throws ConnectionException, LoginException, ParseException
    {
        Map[] categories = (Map[])serverConnector.getCategories();

        for(int i = 0; i < categories.length; i++)
        {
            insertCategory(categories[i]);
        }
    }

    public void downloadSections() throws ConnectionException, LoginException, ParseException, ValidationException
    {
        Map[] sections = (Map[])serverConnector.getSections();

        for(int i = 0; i < sections.length; i++)
        {
            //System.out.println("Section: " + i);
            insertSection(sections[i]);

            Map[] sections_students = (Map[])serverConnector.getSectionStudents((Long)sections[i].get("id"));

            for(int j = 0; j < sections_students.length; j++)
            {
                insertSections_Students((Long)sections[i].get("id"), (Long)sections_students[j].get("id"));
                //System.out.println("Inserting SS:-----\n SectionID: " + sections[i].get("id") + ", StudentID: " + (Long)sections_students[j].get("id"));
                Student s = new Student(((Long)sections_students[j].get("id")), sections_students[j].get("first_name")+" "+sections_students[j].get("last_name"));
                studentPIndex.put(s);
                numStudents++;
                //System.out.println(((Long)sections_students[j].get("id")) + " " + sections_students[j].get("first_name")+" "+sections_students[j].get("last_name"));
            }
        }
    }

    /*public void downloadSections_Students() throws ConnectionException, LoginException, ParseException
    {

        Map[] sections_students = (Map[])serverConnector.getSectionStudents();

        for(int i = 0; i < sections_students.length; i++)
        {
            insertSections_Students(sections_students[i]);
        }
    }*/

    //Sends all of the data in the tables to the connection class using the
    //methods for each object type/table
    public void upload() throws ConnectionException, LoginException, ParseException, ValidationException, IOException
    {
        uploadCategories();
        //uploadSections();
        uploadAssignments();
        uploadGrades();
        
        assignmentChanges = new ArrayList<String>();
        categoryChanges = new ArrayList<String>();
        gradeChanges = new ArrayList<String>();

        File assignmentCh = new File("assignmentChanges.txt");
        File categoryCh = new File("categoryChanges.txt");
        File gradeCh = new File("gradeChanges.txt");

        fw = new FileWriter(assignmentCh);
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);
        bw.write("");
        bw.close();

        fw = new FileWriter(categoryCh);
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);
        bw.write("");
        bw.close();

        fw = new FileWriter(gradeCh);
        pw = new PrintWriter(fw);
        bw = new BufferedWriter(pw);
        bw.write("");
        bw.close();
    }

    //Fix this later
    public void uploadSections() throws ConnectionException, LoginException, ParseException, ValidationException
    {
        /*
        for(int i = 0; i < sectionChanges.size(); i++)
        {
            if(sectionChanges.get(i).substring(0, 1) == "U")
            {
                update
            }
            else if(sectionChanges.get(i).substring(0, 1) == "D")
            {
                delete
            }
            else
            {
                create
            }
        }
        */
    }

    public void uploadAssignments() throws ConnectionException, LoginException, ParseException, ValidationException
    {
        for(int i = 0; i < assignmentChanges.size(); i++)
        {
            String c = assignmentChanges.get(i).substring(0, 1);

            if(!c.equals("D"))
            {
                Assignment temp = assignmentPIndex.get(Long.parseLong(assignmentChanges.get(i).substring(1)));
                long id = temp.getID();
                long sectionID = temp.getSectionID();
                long categoryID = temp.getCategoryID();
                String name = temp.getName();
                long strategy = temp.getStrategy();
                double pointsValue = temp.getPoints();
                System.out.println("Points Value: " + pointsValue);
                double pointsCurve = 0;
            
                if(c.equals("U"))
                {
                    serverConnector.updateAssignment(sectionID, id, categoryID, name, strategy, (int)pointsValue, (int)pointsCurve);
                }
                else if(c.equals("I"))
                {
                    serverConnector.createAssignment(sectionID, categoryID, name, strategy, (int)pointsValue, (int)pointsCurve);

                    Assignment[] tempAssignments = getAssignments(categoryID);

                    emptyAssignments();
                    downloadAssignments();

                    ArrayList<Assignment> tempAssign2 = new ArrayList<Assignment>();
                    Assignment[] tempAssign3 = getAssignments(categoryID);

                    for(int p = 0; p < tempAssign3.length; p++)
                    {
                        tempAssign2.add(tempAssign3[p]);
                    }

                    ArrayList<Assignment> tempAssignOld = new ArrayList<Assignment>();

                    for(int p = 0; p < tempAssignments.length; p++)
                    {
                        tempAssignOld.add(tempAssignments[p]);
                    }

                    for(int v = 0; v < tempAssignOld.size(); v++)
                    {
                        for(int u = 0; u < tempAssign2.size(); u++)
                        {
                            System.out.println("OLD: " + tempAssignOld.get(v).getID());
                            System.out.println("NEW: " + tempAssign2.get(u).getID());

                            if(tempAssignOld.get(v).getID() == tempAssign2.get(u).getID())
                            {
                                tempAssign2.remove(u);
                                if(tempAssign2.size() > 1)
                                {
                                    u--;
                                }
                            }
                        }
                    }

                    if(tempAssign2.size() >= 1)
                    {

                    EntityCursor<Grade> cursor2 = gradePIndex.entities();

                    for(int z = 0; z < assignmentChanges.size(); z++)
                    {
                        System.out.println("Checking assignment changes at " + z);

                        System.out.println("CURRENT: " + assignmentChanges.get(z));
                        System.out.println("NEW: " + tempAssign2.get(0).getID());

                        System.out.println("OLD ID IN CHANGES: " + assignmentChanges.get(z).substring(1, assignmentChanges.get(z).length()));
                        System.out.println("OLD ID: " + id);

                        if(assignmentChanges.get(z).substring(1, assignmentChanges.get(z).length()).equals("" + id))
                        {
                            String s = assignmentChanges.get(z).substring(0, 1);

                            System.out.println("Assignment Change Type: " + s);

                            assignmentChanges.remove(z);

                            System.out.println("Assignment change at z = " + z  + " removed");

                            assignmentChanges.add(z, "" + s + "" + tempAssign2.get(0).getID());

                            System.out.println("Assignment Changes made. " + s + tempAssign2.get(0).getID()+ " is the new change.");
                        }
                }


                    for(Grade g : cursor2)
                    {
                        if(g.getAssignmentID() == id)
                        {
                            Grade tempGrade = g;
                            tempGrade.setAssignmentID(tempAssign2.get(0).getID());
                            gradePIndex.put(tempGrade);
                        }
                    }

                    cursor2.close();
                    }
                }
            }
            else if(c.equals("D"))
            {
                int x = 0;

                for(int w = 0; w < assignmentChanges.get(i).length(); w++)
                {
                    if(assignmentChanges.get(i).charAt(w) == '-')
                    {
                        x = w+1;
                    }
                }
                
                serverConnector.destroyAssignment(Long.parseLong(assignmentChanges.get(i).substring(1, x-1)), Long.parseLong(assignmentChanges.get(i).substring(x)));
            }

            assignmentChanges.remove(i);
            i--;
        }

        for(int i = 0; i < assignmentChanges.size(); i++)
        {
            assignmentChanges.remove(i);
        }
    }

    public void uploadGrades() throws ConnectionException, LoginException, ParseException, ValidationException
    {
        for(int i = 0; i < gradeChanges.size(); i++)
        {
            String c = gradeChanges.get(i).substring(0, 1);
            Grade temp = gradePIndex.get(Long.parseLong(gradeChanges.get(i).substring(1)));
            long sectionID = temp.getSectionID();
            long assignmentID = temp.getAssignmentID();
            long studentID = temp.getStudentID();
            double pointsEarned = temp.getPointsEarned();

            System.out.println("Attempting Grade Upload- SID:" + sectionID + ", AID: " + assignmentID + ", STID: " + studentID + ", " + pointsEarned);
            
            if(c.equals("U"))
            {
                if(pointsEarned >= 0)
                {
                    serverConnector.updateGrade(sectionID, assignmentID, studentID, (int)pointsEarned);
                }
                if(pointsEarned < 0)
                {
                    serverConnector.destroyGrade(sectionID, assignmentID, studentID);
                }
            }
            else if(c.equals("D"))
            {
                    serverConnector.destroyGrade(sectionID, assignmentID, studentID);
            }
            else
            {
                if(pointsEarned >= 0)
                {
                    System.out.println("Creating Grade in Server.");
                    serverConnector.createGrade(sectionID, assignmentID, studentID, (int)pointsEarned);
                }
            }
        }

        for(int i = 0; i < gradeChanges.size(); i++)
        {
            Grade tempGrade = gradePIndex.get(Long.parseLong(gradeChanges.get(i).substring(1)));

            if(tempGrade.getPointsEarned() >= 0)
            {
                gradeChanges.remove(i);
                i--;
            }
        }
    }

    public void uploadCategories() throws ConnectionException, LoginException, ParseException, ValidationException
    {
        for(int i = 0; i < categoryChanges.size(); i++)
        {
            String c = categoryChanges.get(i).substring(0, 1);
            
            if(!c.equals("D"))
            {
                Category temp = categoryPIndex.get(Long.parseLong(categoryChanges.get(i).substring(1, categoryChanges.get(i).length())));
                System.out.println("Category to Upload: " + temp);
                System.out.println("CID: " + Long.parseLong(categoryChanges.get(i).substring(1, categoryChanges.get(i).length())));
                long sectionID = temp.getSectionID();
                long id = temp.getID();
                String name = temp.getName();
                double weight = temp.getWeight();

                System.out.println("SectionID: " + sectionID + "\nName: " + name + "\nWeight: " + weight);
                System.out.println("Connector: " + serverConnector);
            

                if(c.equals("U"))
                {
                    serverConnector.updateCategory(sectionID, id, name, (int)weight);
                    System.out.println(name + " Updated.");
                }
                else if(c.equals("I"))
                {
                    Category tempCat = temp;
                    serverConnector.createCategory(sectionID, name, (int)weight);
                    System.out.println(name + " Created.");

                    Category[] tempCategories = getCategories(sectionID);

                    emptyCategories();
                    downloadCategories();

                    ArrayList<Category> tempCats2 = new ArrayList<Category>();
                    Category[] tempCats3 = getCategories(sectionID);

                    for(int p = 0; p < tempCats3.length; p++)
                    {
                        tempCats2.add(tempCats3[p]);
                    }

                    ArrayList<Category> tempCatsOld = new ArrayList<Category>();

                    for(int p = 0; p < tempCategories.length; p++)
                    {
                        tempCatsOld.add(tempCategories[p]);
                    }

                    for(int v = 0; v < tempCatsOld.size(); v++)
                    {
                        for(int u = 0; u < tempCats2.size(); u++)
                        {
                            System.out.println("OLD: " + tempCatsOld.get(v).getID());
                            System.out.println("NEW: " + tempCats2.get(u).getID());
                        
                            if(tempCatsOld.get(v).getID() == tempCats2.get(u).getID())
                            {
                                tempCats2.remove(u);
                                
                                if(tempCats2.size() > 1)
                                {
                                    u--;
                                }
                            }
                        }
                    }

                    EntityCursor<Assignment> cursor2 = assignmentPIndex.entities();


                    for(int z = 0; z < categoryChanges.size(); z++)
                    {
                        System.out.println("Checking category changes at " + z);

                        System.out.println("CURRENT: " + categoryChanges.get(z));
                        System.out.println("NEW: " + tempCats2.get(0).getID());

                        System.out.println("OLD ID IN CHANGES: " + categoryChanges.get(z).substring(1, categoryChanges.get(z).length()));
                        System.out.println("OLD ID: " + id);

                        if(categoryChanges.get(z).substring(1, categoryChanges.get(z).length()).equals("" + id))
                        {
                            String s = categoryChanges.get(z).substring(0, 1);

                            System.out.println("Category Change Type: " + s);

                            categoryChanges.remove(z);

                            System.out.println("Category change at z = " + z  + "removed");

                            categoryChanges.add(z, "" + s + "" + tempCats2.get(0).getID());

                            System.out.println("Category Changes made. " + s + tempCats2.get(0).getID()+ " is the new change.");
                        }
                    }


                    for(Assignment a : cursor2)
                    {
                        if(a.getCategoryID() == id)
                        {
                            Assignment tempAssignment = a;
                            tempAssignment.setCategoryID(tempCats2.get(0).getID());
                            assignmentPIndex.put(tempAssignment);
                        }
                    }

                    cursor2.close();
                }
            }
            else if(c.equals("D"))
            {
                int x = 0;

                for(int w = 0; w < categoryChanges.get(i).length(); w++)
                {
                    if(categoryChanges.get(i).charAt(w) == '-')
                    {
                        x = w+1;
                    }
                }

                serverConnector.destroyCategory(Long.parseLong(categoryChanges.get(i).substring(1, x-1)), Long.parseLong(categoryChanges.get(i).substring(x)));
                System.out.println("Category Destroyed.");
            }

            categoryChanges.remove(i);
            i--;
        }

        for(int i = 0; i < categoryChanges.size(); i++)
        {
            categoryChanges.remove(i);
        }
    }

    //Empties the entire contents of the DB, drops all data, drops tables and recreates them
    public void emptyDB()
    {
        emptyCategories();
        emptyGrades();
        emptyAssignments();
        emptyStudents();
        emptySections();
        emptySections_Students();
    }

    //Drops Categories table and recreates it
    public void emptyCategories()
    {
        EntityCursor<Category> cursor = categoryPIndex.entities();
        //cursor.first();
        //Iterator<Category> i = cursor.iterator();

        //cursor.last();

        for(Category c : cursor)
        {
            cursor.delete();
            //cursor.prev();
        }

        cursor.close();
    }

    //Drops Students table and recreates it
    public void emptyStudents()
    {
        EntityCursor<Student> cursor = studentPIndex.entities();
        //cursor.first();
        //Iterator<Student> i = cursor.iterator();

        //cursor.last();

        for(Student s : cursor)
        {
            cursor.delete();
            //cursor.prev();
        }

        cursor.close();
    }

    //Drops Assignments table and recreates it
    public void emptyAssignments()
    {
        EntityCursor<Assignment> cursor = assignmentPIndex.entities();
        //cursor.first();
        //Iterator<Assignment> i = cursor.iterator();

        //cursor.last();

        for(Assignment a: cursor)
        {
            cursor.delete();
            //cursor.prev();
        }

        cursor.close();
    }

    //Drops Grades table and recreates it
    public void emptyGrades()
    {
        EntityCursor<Grade> cursor = gradePIndex.entities();
        //cursor.first();
        //Iterator<Grade> i = cursor.iterator();

        //cursor.last();

        for(Grade g : cursor)
        {
            cursor.delete();
           // cursor.prev();
        }

        cursor.close();
    }

    //Drops Sections table and recreates it
    public void emptySections()
    {
        EntityCursor<Section> cursor = sectionPIndex.entities();
        //cursor.first();
        //Iterator<Section> i = cursor.iterator();

        //cursor.last();

        for(Section s : cursor)
        {
            cursor.delete();
            //cursor.prev();
        }

        cursor.close();
    }

    public void emptySections_Students()
    {
        EntityCursor<Sections_Students> cursor = sections_studentsPIndex.entities();
        //cursor.first();
        //Iterator<Sections_Students> i = cursor.iterator();

        //cursor.last();

        for(Sections_Students s : cursor)
        {
            cursor.delete();
            //cursor.prev();
        }

        cursor.close();
    }

    public Student getStudent(long StudentID)
    {
        EntityCursor<Student> cursor = studentPIndex.entities();
        //cursor.first();

        Student temp = null;

        for (Student g: cursor)
        {
            if(g.getID() == StudentID)
            {
                temp = g;
            }
        }

        cursor.close();

        return temp;
    }

    public Student[] getStudents(long SectionID)
    {
        Student[] students = new Student[10000];

        int num = 0;

        EntityCursor<Sections_Students> cursor2 = sections_studentsPIndex.entities();
        //cursor2.first();

        boolean add = true;

        for (Sections_Students s: cursor2)
        {
            if(s.getSectionID() == SectionID)
            {
                for(int i = 0; i < num; i++)
                {
                    if(students[i] == studentPIndex.get(s.getStudentID()))
                    {
                        add = false;
                    }
                }

                if(add)
                {
                    students[num] = studentPIndex.get(s.getStudentID());
                    //System.out.println(students[num]);
                    num++;
                }

                add = true;
            }
        }
        
        cursor2.close();

        Student[] temp = new Student[num];

        for(int i = 0; i < num; i++)
        {
            temp[i] = students[i];
            //System.out.println("Adding student: " + temp[i]);
        }

        students = temp;

        return students;
    }

    public void insertStudent(Map m)
    {
        Map a = (Map)m;

        Student s = new Student(((Long)a.get(new String("id"))), a.get(new String("first_name"))+" "+a.get(new String("last_name")));

        studentPIndex.put(s);

        numStudents++;
    }

    public void insertStudent(Student s)
    {
        studentPIndex.put(s);
        numStudents++;
    }

    public void insertSections_Students(Map m)
    {
        Map a = (Map)m;

        Sections_Students s = new Sections_Students(numSS, ((Long)(a.get(new String("section_id")))), ((Long)(a.get(new String("student_id")))));

        sections_studentsPIndex.put(s);

        numSS++;
    }

    public void insertSections_Students(long sectionID, long studentID)
    {
        Sections_Students s = new Sections_Students(numSS, sectionID, studentID);

        sections_studentsPIndex.put(s);

        numSS++;
    }

    public Section getSection(long SectionID)
    {
        EntityCursor<Section> cursor = sectionPIndex.entities();
        //cursor.first();

        Section temp = null;

        for (Section g: cursor)
        {
            if(g.getID() == SectionID)
            {
                temp = g;
            }
        }

        cursor.close();

        return temp;
    }

    public Section[] getSections()
    {
        Section[] sections = new Section[100];

        int num = 0;

        EntityCursor<Section> cursor = sectionPIndex.entities();
        //cursor.first();

        for (Section g: cursor)
        {
            sections[num] = g;
            num++;
            //System.out.println(num);
        }

        cursor.close();

        Section[] temp = new Section[num];

        for(int i = 0; i < num; i++)
        {
            temp[i] = sections[i];
        }

        sections = temp;

        return sections;
    }

    public Assignment getAssignment(long AssignmentID)
    {
        EntityCursor<Assignment> cursor = assignmentPIndex.entities();
        //cursor.first();

        Assignment temp = null;

        for (Assignment g: cursor)
        {
            if(g.getID() == AssignmentID)
            {
                temp = g;
            }
        }

        cursor.close();

        return temp;
    }

    public Assignment[] getAssignments(long CategoryID)
    {
        Assignment[] assignments = new Assignment[10000];

        int num = 0;

        EntityCursor<Assignment> cursor = assignmentPIndex.entities();
        //cursor.first();

        for (Assignment g: cursor)
        {
            if(g.getCategoryID() == CategoryID)
            {
                assignments[num] = g;
                num++;
            }
        }

        cursor.close();

        Assignment[] temp = new Assignment[num];

        for(int i = 0; i < num; i++)
        {
            temp[i] = assignments[i];
        }

        assignments = temp;

        return assignments;
    }

    public void updateAssignment(Assignment a)
    {
        assignmentPIndex.put(a);
        assignmentChanges.add("U" + a.getID());
    }

    public void updateAssignments(Assignment[] a)
    {
        for(int i = 0; i < a.length; i++)
        {
            assignmentPIndex.put(a[i]);
            assignmentChanges.add("U" + a[i].getID());
        }
    }

    public void deleteAssignment(Assignment a)
    {
        assignmentChanges.add("D" + a.getSectionID() + "-" + a.getID());

        EntityCursor<Assignment> cursor = assignmentPIndex.entities();

        for(Assignment assign : cursor)
        {
            if(assign.getID() == a.getID())
            {
                cursor.delete();
            }
        }

        cursor.close();
    }

    public void deleteAssignments(Assignment[] a)
    {
        for(int i = 0; i < a.length; i++)
        {
            assignmentChanges.add("D" + a[i].getID());
        }


    }

    public void insertAssignment(Assignment a)
    {
        assignmentPIndex.put(a);
        assignmentChanges.add("I" + a.getID());
        numAssignments++;
    }

    public void insertAssignment(Map m)
    {
        Map a = (Map)m;

        Assignment s = new Assignment((Long)a.get(new String("id")), ((String)a.get(new String("name"))), ((Double)(a.get(new String("points_value")))), ((Long)(a.get(new String("strategy_id")))), ((Long)(a.get(new String("category_id")))), ((Long)(a.get(new String("section_id")))));

        assignmentPIndex.put(s);
        
        numAssignments++;
    }

    public void insertAssignments(Assignment[] a)
    {
        for(int i = 0; i < a.length; i++)
        {
            assignmentPIndex.put(a[i]);
            assignmentChanges.add("I" + a[i].getID());
            numAssignments++;
        }
    }

    public Category getCategory(long CategoryID)
    {
        EntityCursor<Category> cursor = categoryPIndex.entities();
        //cursor.first();

        Category temp = null;

        for (Category g: cursor)
        {
            if(g.getID() == CategoryID)
            {
                temp = g;
            }
        }

        cursor.close();

        return temp;
    }

    public Category[] getCategories(long SectionID)
    {
        Category[] categories = new Category[10000];

        int num = 0;

        EntityCursor<Category> cursor = categoryPIndex.entities();
        //cursor.first();

        for (Category g: cursor)
        {
            if(g.getSectionID() == SectionID)
            {
                categories[num] = g;
                num++;
            }
        }

        cursor.close();

        Category[] temp = new Category[num];

        for(int i = 0; i < num; i++)
        {
            temp[i] = categories[i];
        }

        categories = temp;

        return categories;
    }

    public void updateCategory(Category c)
    {
        categoryPIndex.put(c);
        categoryChanges.add("U" + c.getID());
    }

    public void updateCategories(Category[] c)
    {
        for(int i = 0; i < c.length; i++)
        {
            categoryPIndex.put(c[i]);
            categoryChanges.add("U" + c[i].getID());
        }
    }

    public void deleteCategory(Category c)
    {
        categoryChanges.add("D" + c.getSectionID() + "-" + c.getID());

        EntityCursor<Category> cursor = categoryPIndex.entities();

        for(Category cat : cursor)
        {
            if(cat.getID() == c.getID())
            {
                cursor.delete();
            }
        }

        cursor.close();
    }

    public void deleteCategories(Category[] c)
    {
        for(int i = 0; i < c.length; i++)
        {
            categoryChanges.add("D" + c[i].getID());
        }
    }

    public void insertCategory(Category c)
    {
        categoryPIndex.put(c);
        categoryChanges.add("I" + c.getID());
        System.out.println("Category: " + c + " added to Embedded DB.");
        numCategories++;
    }

    public void insertCategory(Map m)
    {
        Map a = (Map)m;

        long id = ((Long)a.get("id"));
        long sectionID = ((Long)a.get("section_id"));
        String name = (String)a.get("name");
        double weight = (Double)(a.get("weight"));

        Category s = new Category(id, sectionID, name, weight);

        categoryPIndex.put(s);

        numCategories++;
    }

    public void insertCategories(Category[] c)
    {
        for(int i = 0; i < c.length; i++)
        {
            categoryPIndex.put(c[i]);
            categoryChanges.add("I" + c[i].getID());
            numCategories++;
        }
    }

    public Grade getGrade(long AssignmentID, long StudentID)
    {
        EntityCursor<Grade> cursor = gradePIndex.entities();
        //cursor.first();

        Grade temp = null;

        for (Grade g: cursor)
        {
            if(g.getAssignmentID() == AssignmentID && g.getStudentID() == StudentID)
            {
                temp = g;
            }
        }

        cursor.close();

        return temp;
    }

    public Grade[] getGrades(long AssignmentID)
    {
        Grade[] grades = new Grade[10000];

        int num = 0;

        EntityCursor<Grade> cursor = gradePIndex.entities();
        //cursor.first();

        for (Grade g: cursor)
        {
            //System.out.println("Grade: " + g + ", AID: " + g.getAssignmentID() + ", SID: " + g.getStudentID());

            if(g.getAssignmentID() == AssignmentID)
            {
                grades[num] = g;
                num++;
            }
        }

        cursor.close();

        Grade[] temp = new Grade[num];

        for(int i = 0; i < num; i++)
        {
            temp[i] = grades[i];
        }

        grades = temp;

        return grades;
    }

    public void updateGrade(Grade g)
    {
        gradePIndex.put(g);
        gradeChanges.add("U" + g.getID());
    }

    public void updateGrades(Grade[] g)
    {
        for(int i = 0; i < g.length; i++)
        {
            gradePIndex.put(g[i]);
            gradeChanges.add("U" + g[i].getID());
        }
    }

    private void deleteGrade(Grade g)
    {
        gradeChanges.add("D" + g.getID());
    }

    private void deleteGrades(Grade[] g)
    {
        for(int i = 0; i < g.length; i++)
        {
            gradeChanges.add("D" + g[i].getID());
        }
    }

    public void insertGrade(Grade g)
    {
        gradePIndex.put(g);
        gradeChanges.add("I" + g.getID());
        numGrades++;
    }

    public void replaceGrade(Grade g)
    {
        System.out.println("Grade Replaced ID: " + g.getID() + " Points: " + g.getPointsEarned());
        gradePIndex.put(g);
        gradeChanges.add("U" + g.getID());
        System.out.println("Grade after replacement: ID: " + g.getID() + " Points: " + g.getPointsEarned());
    }

    public void insertGrade(Map m)
    {
        Map a = (Map)m;

        long assignmentID = (Long)(a.get("assignment_id"));
        long studentID = (Long)(a.get("student_id"));
        long sectionID = (Long)(a.get("id"));
        double points_earned = (Double)(a.get("points_earned"));

        Grade s = new Grade(sectionID, assignmentPIndex.get(assignmentID).getSectionID(), assignmentID, studentID, points_earned);

        //System.out.println("Inserted Grade: " + s);

        gradePIndex.put(s);

        numGrades++;
    }

    public void insertGrades(Grade[] g)
    {
        for(int i = 0; i < g.length; i++)
        {
            gradePIndex.put(g[i]);
            gradeChanges.add("I" + g[i].getID());
            numGrades++;
        }
    }

    public void insertSection(Map m) throws ConnectionException, LoginException, ParseException, ValidationException
    {
        Map a = (Map)m;

        long courseID = (Long)(a.get("course_id"));

        String name = "";

        Map[] courses = (Map[])(serverConnector.getCourses());
        
        for(int i = 0; i < courses.length; i++)
        {
            if((Long)(courses[i].get("id")) == courseID)
            {
                name = (String)courses[i].get("prefix") + " " + (String)courses[i].get("code") + "/" + a.get("sequence");
            }
        }

        long id = (Long)(a.get("id"));

        //System.out.println("SectionID: " + id + " added");

        Section s = new Section(id, name);

        sectionPIndex.put(s);

        numSections++;
    }

    public int getNumGrades()
    {
        numGrades = 0;
        long tempNum = 0;

        EntityCursor<Grade> cursor = gradePIndex.entities();
        //cursor.first();

        for (Grade g: cursor)
        {
            if(g.getID() > tempNum)
            {
                tempNum = g.getID();
                numGrades = (int)tempNum+1;
            }
        }

        cursor.close();

        return numGrades;
    }

    public int getNumCategories()
    {
        numCategories = 0;
        long tempNum = 0;

        EntityCursor<Category> cursor = categoryPIndex.entities();
        //cursor.first();

        for (Category g: cursor)
        {
            if(g.getID() > tempNum)
            {
                tempNum = g.getID();
                numCategories = (int)tempNum+1;
            }
        }

        cursor.close();

        return numCategories;
    }

    public int getNumAssignments()
    {
        numAssignments = 0;
        long tempNum = 0;

        EntityCursor<Assignment> cursor = assignmentPIndex.entities();
        //cursor.first();

        for (Assignment g: cursor)
        {
            if(g.getID() > tempNum)
            {
                tempNum = g.getID();
                numAssignments = (int)tempNum+1;
            }
        }

        cursor.close();

        return numAssignments;
    }

    public ArrayList<String> getGradeChanges()
    {
        return gradeChanges;
    }

    public ArrayList<String> getAssignmentChanges()
    {
        return assignmentChanges;
    }

    public ArrayList<String> getCategoryChanges()
    {
        return categoryChanges;
    }

    public double getGradePoints(long assignment_id, long student_id)
    {
        EntityCursor<Grade> cursor = gradePIndex.entities();
        //cursor.first();

        double pe = -1;

        for (Grade g: cursor)
        {
            if(g.getAssignmentID() == assignment_id && g.getStudentID() == student_id)
            {
                pe = g.getPointsEarned();
            }
        }

        cursor.close();

        return pe;
    }
}
