package com.blogspot.the3cube.beefree.test;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.blogspot.the3cube.beefree.logic.cmd.DisplayCommand;
import com.blogspot.the3cube.beefree.storage.FileSystem;
import com.blogspot.the3cube.beefree.storage.Storage;
import com.blogspot.the3cube.beefree.storage.TaskContainer;
import com.blogspot.the3cube.beefree.util.Display;
import com.blogspot.the3cube.beefree.util.DisplayCLI;
import com.blogspot.the3cube.beefree.util.Task;
import com.blogspot.the3cube.beefree.util.Variable;

public class DisplayCommandTest {

	private static Storage storage_;
	
	private final static String task01 = "1,brown fox jumped,2/2/2011,28/9/2011,null,null,jump,0";
	private final static String task02 = "2,short,25/2/2011,23/5/2011,,,,-1";
	private final static String task03 = "3,cs2103t,,30/01/2011,15:59:01,23:59:00,school,0";
	private final static String task04 = "4,home,,2/2/2011,,10:30:00,,0";
	private final static String task05 = "5,task test,,,,,task;test,0";
	private final static String task06 = "6,do tutorial,,,,,homework;cs2103t,0";

	private final static String[] newTasks = { task01, task02, task03, task04,
			task05, task06 };

	
	private static String[] output_ = new String[9];

	private final static String DISPLAYCOMMAND01 = "";
	private final static String DISPLAYCOMMAND02 = "-l homework";
	private final static String DISPLAYCOMMAND03 = "-l work";
	private final static String DISPLAYCOMMAND04 = "-t 18";
	private final static String DISPLAYCOMMAND05 = "-t 5";
	private final static String DISPLAYCOMMAND06 = "homework";
	private final static String DISPLAYCOMMAND07 = "cs2103t";
	private final static String DISPLAYCOMMAND08 = "party";
	private final static String DISPLAYCOMMAND09 = "-t ads";
	
	private final static String[] COMMANDS = { DISPLAYCOMMAND01, DISPLAYCOMMAND02,
			DISPLAYCOMMAND03, DISPLAYCOMMAND04, DISPLAYCOMMAND05, DISPLAYCOMMAND06, 
			DISPLAYCOMMAND07, DISPLAYCOMMAND08, DISPLAYCOMMAND09 };

	private final static String RESULT01 = 
			"ID\t\tTask\t\t\t\tStart Date\t\tEnd Date\t\tLabels\n"
				+ "--\t\t----\t\t\t\t----------\t\t--------\t\t-----\n"
				+ "1\tbrown fox jumped\t\t\t2011-02-02\t\t2011-09-28\t\tjump\n2\tshort	" +
				"\t\t\t\t2011-02-25\t\t2011-05-23\t\t\n3\tcs2103t\t\t\t\t\t\t    " +
				"\t\t2011-01-30, 23:59:00\tschool\n4\thome\t\t\t\t\t\t    " +
				"\t\t2011-02-02, 10:30:00\t\n5\ttask test\t\t\t\t\t    " +
				"\t\t	    \t\ttask, test\n6\tdo tutorial\t\t\t\t\t    \t\t\t    " +
				"\t\thomework, cs2103t";
	private final static String RESULT02 = 
			"ID\t\tTask\t\t\t\tStart Date\t\tEnd Date\t\tLabels\n"
				+ "--\t\t----\t\t\t\t----------\t\t--------\t\t-----\n" +
				"6\tdo tutorial\t\t\t\t\t    \t\t\t    " +
				"\t\thomework, cs2103t";
	private final static String RESULT03 = 
			"There is no such label";
	private final static String RESULT04 = 
			"There is no such task";
	private final static String RESULT05 = 
			"ID\t\tTask\t\t\t\tStart Date\t\tEnd Date\t\tLabels\n"
				+ "--\t\t----\t\t\t\t----------\t\t--------\t\t-----\n"
				+ "5\ttask test\t\t\t\t\t    " +
				"\t\t	    \t\ttask, test";
	private final static String RESULT06 = 
			"ID\t\tTask\t\t\t\tStart Date\t\tEnd Date\t\tLabels\n"
				+ "--\t\t----\t\t\t\t----------\t\t--------\t\t-----\n"
				+ "6\tdo tutorial\t\t\t\t\t    \t\t\t    " +
				"\t\thomework, cs2103t";
	private final static String RESULT07 = 
			"ID\t\tTask\t\t\t\tStart Date\t\tEnd Date\t\tLabels\n"
				+ "--\t\t----\t\t\t\t----------\t\t--------\t\t-----\n"
				+ "3\tcs2103t\t\t\t\t\t\t    " +
				"\t\t2011-01-30, 23:59:00\tschool\n"
				+ "6\tdo tutorial\t\t\t\t\t    \t\t\t    " +
				"\t\thomework, cs2103t";
	private final static String RESULT08 = 
			"There is no matching task!";
	private final static String RESULT09 = 
			"Please check your input. Task ID should be numeric.";
	
	private final static String[] RESULT = { RESULT01, RESULT02, RESULT03, RESULT04, 
		RESULT05, RESULT06, RESULT07, RESULT08, RESULT09 };

	/**
	 * this method will perform the required setup.<br>
	 * 
	 * @programmer 	Chin Gui Pei
	 * @start 		30 September 2011
	 * @finish 		4 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	4 October 2011
	 * 
	 * @since 		v0.1
	 */
	@BeforeClass
	public static void setup() {
		FileSystem.clearTextFile(Variable.TEST_BLOCKOUT_FILENAME);
		FileSystem.clearTextFile(Variable.TEST_TASK_FILENAME);

		storage_ = new Storage(Variable.TEST_TASK_FILENAME,
				Variable.TEST_BLOCKOUT_FILENAME);
		storage_.init();

		for (int i = 0; i < newTasks.length; i++) {
			Task newTask = new Task(newTasks[i]);
			assertTrue(storage_.addTask(newTask));
		}

	}

	/**
	 * this method will perform the testing.<br>
	 * 
	 * @programmer 	Chin Gui Pei
	 * @start 		30 September 2011
	 * @finish 		4 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	4 October 2011
	 * 
	 * @since 		v0.1
	 */
	@Test
	public void test() {
		for (int i = 0; i < COMMANDS.length; i++) {
			DisplayCommand command = new DisplayCommand();
			command.setStorage(storage_);
			command.constructCommand(COMMANDS[i]);
			Display display = command.run();
			DisplayCLI cli = null;
			if (display instanceof DisplayCLI)
			cli = (DisplayCLI) display;
			output_[i] = cli.getData_();	
		}
		
		for (int i = 0; i < output_.length; i++) {
			boolean matched = output_[i].equals(RESULT[i].trim());
			if (!matched) {
				System.out.println(i);
				System.out.println(output_[i]);
				System.out.println(RESULT[i]);
			}
			assertTrue(matched);
		}
	}

}

