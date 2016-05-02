package com.blogspot.the3cube.beefree.logic.cmd;

import com.blogspot.the3cube.beefree.logic.CommandType;
import com.blogspot.the3cube.beefree.storage.Storage;
import com.blogspot.the3cube.beefree.util.Display;
import com.blogspot.the3cube.beefree.util.DisplayCLI;

/**
 * Handle and execute the help command.<br>
 * 
 * @author 		Chin Gui Pei, Chua Jie Sheng
 * @since 		v0.1
 * @version 	v0.2
 * 
 * <p><pre>
 * command syntax:<br>
 * help [commandName]<br><br>
 * </pre></p>
 * <p><pre>
 * example: <br>
 * help<br>
 * this would show an overview of all commands available.<br><br>
 * 
 * help add<br>
 * this would show the help page for the add command.<br><br>
 * 
 * help delete<br>
 * this would show the help page for the delete command.<br><br>
 * </pre></p>
 */
public class HelpCommand implements Command {

	private final static String GENERAL_HELP_MESSAGE = "Beefree is a To-Do manager that is easy to use. To manage your task effectively, you may use any of the following functions:"
			+ "\n"
			+ "- Add\t		= Add tasks!"
			+ "\n"
			+ "- Edit\t		= Edit your tasks"
			+ "\n"
			+ "- Complete\t	= Mark your tasks as completed"
			+ "\n"
			+ "- Delete\t	= Delete unwanted tasks"
			+ "\n"
			+ "- Clear\t		= clear completed tasks or tasks with specific labels"
			+ "\n"
			+ "- Display\t	= Display all tasks or display tasks according to ID or label."
			+ "\n" 
			+ "\t\t\t  You can also search for tasks using this command."
			+ "\n"
			+ "- Blockout\t	= Block out a period which tasks will not be added in!" 
			+ "\n" 
			+ "\t\t\t  You will be reminded if you add a task in the blocked out periods."
			+ "\n"
			+ "- Export\t	= Export your tasks to a textfile or Google Calendar!"
			+ "\n"
			+ "- Undo\t 		= Undo previous commands"
			+ "\n"
			+ "To know more about each command, type help [command]\n"
			+ "where command is the above commands.";
	

	private final static String ADD_HELP_MESSAGE = "To add:"
			+ "\n"
			+ "\n"
			+ "add taskname [-sf date] [-fb date] [-st time] [ft time] [-l label1;label2]\n"
			+ "or\n"
			+ "add taskname [from date/day time to date/day time] [label label1;label2]"
			+ "\n"
			+ "\n"
			+ "Note:"
			+ "\n"
			+ "-sf date = start date of the task"
			+ "\n"
			+ "-fb date = deadline of the task"
			+ "\n"
			+ "\n"
			+ "-st time = start time of the task"
			+ "\n"
			+ "-ft time = end time of the task"
			+ "\n"
			+ "\n"
			+ "-l label1;label2"
			+ "\n"
			+ "To have multiply labels, separate them with a semicolon (;)."
			+ "\n"
			+ "\n"
			+"for the natural language:\n"
			+ "Instead of 'from', you can use 'start', 'at' or '@'\n"
			+ "You can also replace 'to' with 'till', 'until', 'finish' or 'by'";

	private final static String BLOCKOUT_HELP_MESSAGE = "To blockout a period:"
			+ "\n"
			+ "\n"
			+ "blockout name from startDate to endDate"
			+ "\n"
			+ "- where the startDate and endDate is the period which the blockout will be added, named by the name."
			+ "\n"
			+ "OR"
			+ "\n"
			+ "blockout name on date"
			+ "\n"
			+ "- where the date is the day which you want to blockout and named by the name."
			+ "\n"
			+ "OR"
			+ "\n"
			+ "blockout display"
			+ "\n"
			+ "- this will display all the blockout currently in the system."
			+ "\n"
			+ "OR"
			+ "\n"
			+ "blockout remove blockoutId"
			+ "\n"
			+ "- where the blockoutId is the id of the blockout which you want to remove.";

	private final static String CLEAR_HELP_MESSAGE = "To clear:"
			+ "\n"
			+ "\n"
			+ "clear label1; label2\n"
			+ "\n"
			+ "If no label is specified, all completed tasks will be cleared by default.\n"
			+ "input the label to delete tasks that contain the specific labels.\n"
			+ "Separate multiple labels with a semicolon.\n";

	private final static String COMPLETE_HELP_MESSAGE = "To mark task as completed:"
			+ "\n"
			+ "\n"
			+ "complete taskID1; taskID2\n"
			+ "\n"
			+ "input the taskID to mark the specific task as complete.\n"
			+ "to mark more than 1 task, separate them with a semicolon.\n";

	private final static String DISPLAY_HELP_MESSAGE = "To display tasks:"
			+ "\n"
			+ "\n" 
			+ "display [options]\n"
			+ "\n"
			+ "if there's no option specified, all tasks will be displayed by default.\n"
			+ "-t taskID = display specified tasks. You can replace '-t' with 'id'\n"
			+ "-l label  = display tasks with specific label. You can replace '-l' with 'label'.\n"
			+ "-d / asc  = display tasks with finish date in ascending order.\n"
			+ "You can also input any keyword to search task.";


	private final static String DELETE_HELP_MESSAGE = "To delete tasks:"
			+ "\n"
			+ "\n"
			+ "del taskID1; taskID2\n"
			+ "\n"
			+ "input the taskID to delete the specified task.\n"
			+ "To delete more than 1 task, separate them with a semicolon.\n";

	private final static String EXIT_HELP_MESSAGE = "To exit the program:"
			+ "\n"
			+ "\n"
			+ "exit";
	
	private final static String EXPORT_HELP_MESSAGE = "To export to textfile:"
			+ "\n"
			+ "\n"
			+ "export to file filename\n"
			+ "- where filename is the name of the file\n"
			+ "\n\n"
			+ "To export to Google calendar:\n\n"
			+ "export to gcal with username:password\n"
			+ "- username and password of your gmail account\n";
	
	private final static String EDIT_HELP_MESSAGE = "To edit a task:" 
			+ "\n"
			+ "\n"
			+ "edit [-n taskname] [-sf date] [-fb date] [-st time] [ft time] [-l label1;label2] taskId\n"
			+ "or\n"
			+ "edit [name taskname] [from date/day time] [to date/day time] [label label1;label2] taskId"
			+ "\n"
			+ "\n"
			+ "Note:"
			+ "\n"
			+ "-sf date = start date of the task"
			+ "\n"
			+ "-fb date = deadline of the task"
			+ "\n"
			+ "\n"
			+ "-st time = start time of the task"
			+ "\n"
			+ "-ft time = end time of the task"
			+ "\n"
			+ "\n"
			+ "-l label1;label2"
			+ "\n"
			+ "To have multiply labels, separate them with a semicolon (;)."
			+ "\n"
			+ "\n"
			+"for the natural language:\n"
			+ "Instead of 'from', you can use 'start', 'at' or '@'\n"
			+ "You can also replace 'to' with 'till', 'until', 'finish' or 'by'";

	private final static String HELP_HELP_MESSAGE = "To display a help for a command:"
			+ "\n"
			+ "\n" 
			+ "help [commandName]";
	
	private final static String UNDO_HELP_MESSAGE = "To undo the previous command:"
			+ "\n"
			+ "\n"
			+ "undo"
			+ "\n\n"
			+ "it will undo the previous command and print out the changes.";
	
	private Storage storage_;

	private CommandType helpOnCommand_ = CommandType.INVALID;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.blogspot.the3cube.beefree.logic.cmd.Command#constructCommand(java
	 * .lang.String)
	 */
	/** 
	 * @programmer 	Chua Jie Sheng
	 * @start 		05 October 2011
	 * @finish 		05 October 2011
	 * 
	 * @reviewer 	
	 * @reviewdate 	
	 * 
	 * @since 		v0.1
	 */
	@Override
	public void constructCommand(String input) {
		if (input.startsWith("a")) {
			helpOnCommand_ = CommandType.ADD;
		} else if (input.startsWith("b")) {
			helpOnCommand_ = CommandType.BLOCKOUT;
		} else if (input.startsWith("co")) {
			helpOnCommand_ = CommandType.COMPLETE;
		} else if (input.startsWith("c")) {
			helpOnCommand_ = CommandType.CLEAR;
		} else if (input.startsWith("done")) {
			helpOnCommand_ = CommandType.COMPLETE;
		} else if (input.startsWith("di")) {
			helpOnCommand_ = CommandType.DISPLAY;
		} else if (input.startsWith("do")) {
			helpOnCommand_ = CommandType.ADD;
		} else if (input.startsWith("d")) {
			helpOnCommand_ = CommandType.DELETE;
		} else if (input.startsWith("ex")) {
			helpOnCommand_ = CommandType.EXPORT;
		} else if (input.startsWith("e")) {
			helpOnCommand_ = CommandType.EDIT;
		} else if (input.startsWith("f")) {
			helpOnCommand_ = CommandType.DISPLAY;
		} else if (input.startsWith("ls")) {
			helpOnCommand_ = CommandType.DISPLAY;
		} else if (input.startsWith("rm")) {
			helpOnCommand_ = CommandType.DELETE;
		} else if (input.startsWith("h")) {
			helpOnCommand_ = CommandType.HELP;
		} else if (input.startsWith("s")) {
			helpOnCommand_ = CommandType.DISPLAY;
		} else if (input.startsWith("u")) {
			helpOnCommand_ = CommandType.UNDO;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.logic.cmd.Command#run()
	 */
	/** 
	 * @programmer 	Chua Jie Sheng
	 * @start 		05 October 2011
	 * @finish 		05 October 2011
	 * 
	 * @reviewer 	
	 * @reviewdate 	
	 * 
	 * @since 		v0.1
	 */
	@Override
	public Display run() {
		DisplayCLI cli = new DisplayCLI(GENERAL_HELP_MESSAGE);

		switch (helpOnCommand_) {
		case ADD:
			cli = new DisplayCLI(ADD_HELP_MESSAGE);
			break;
		case BLOCKOUT:
			cli = new DisplayCLI(BLOCKOUT_HELP_MESSAGE);
			break;
		case CLEAR:
			cli = new DisplayCLI(CLEAR_HELP_MESSAGE);
			break;
		case COMPLETE:
			cli = new DisplayCLI(COMPLETE_HELP_MESSAGE);
			break;
		case DISPLAY:
			cli = new DisplayCLI(DISPLAY_HELP_MESSAGE);
			break;
		case DELETE:
			cli = new DisplayCLI(DELETE_HELP_MESSAGE);
			break;
		case EXIT:
			cli = new DisplayCLI(EXIT_HELP_MESSAGE);
			break;
		case EXPORT:
			cli = new DisplayCLI(EXPORT_HELP_MESSAGE);
			break;
		case EDIT:
			cli = new DisplayCLI(EDIT_HELP_MESSAGE);
			break;
		case HELP:
			cli = new DisplayCLI(HELP_HELP_MESSAGE);
			break;
		case UNDO:
			cli = new DisplayCLI(UNDO_HELP_MESSAGE);
			break;
		}

		return cli;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.logic.cmd.Command#undo()
	 */
	/** 
	 * @programmer 	Chua Jie Sheng
	 * @start 		05 October 2011
	 * @finish 		05 October 2011
	 * 
	 * @reviewer 	Chin Gui Pei
	 * @reviewdate 	03 November 2011
	 * 
	 * @since 		v0.1
	 */
	@Override
	public Display undo() {
		return new DisplayCLI("there is nothing to undo for this command");
	}

	/* (non-Javadoc)
	 * @see com.blogspot.the3cube.beefree.logic.cmd.Command#setStorage(com.blogspot.the3cube.beefree.storage.Storage)
	 */
	/** 
	 * @programmer 	Chua Jie Sheng
	 * @start 		11 October 2011
	 * @finish 		11 October 2011
	 * 
	 * @reviewer 	Chin Gui Pei
	 * @reviewdate 	03 November 2011
	 * 
	 * @since 		v0.2
	 */
	@Override
	public void setStorage(Storage storage) {
		storage_ = storage;
	}

}

