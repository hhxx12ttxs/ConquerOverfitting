package com.blogspot.the3cube.beefree.logic.cmd;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.io.*;

import com.blogspot.the3cube.beefree.storage.Storage;
import com.blogspot.the3cube.beefree.util.Display;
import com.blogspot.the3cube.beefree.util.DisplayCLI;
import com.blogspot.the3cube.beefree.util.Task;

/**
 * Display the task as specify by the options.<br>
 * 
 * @author The3Cube
 * @since v0.1
 * @version v0.1
 * 
 *          <p>
 * 
 *          <pre>
 * command syntax:<br>
 * display [options]<br><br>
 * </pre>
 * 
 *          </p>
 *          <p>
 * 
 *          <pre>
 * options:<br>
 * -l label		display tasks details according to label<br>
 * -t taskId	display tasks details according to task ID<br><br>
 * 
 * Note: If there is no option specified, all tasks will be displayed.
 * 
 * </pre>
 * 
 *          </p>
 *          <p>
 * 
 *          <pre>
 * example: <br>
 * 
 * display -l school<br>
 * this would display the task with label "school".<br><br>
 * 
 * display -t 1<br>
 * this would display the tasks with task Id 1.<br><br>
 * </pre>
 * 
 *          </p>
 */
public class DisplayCommand implements Command {
	private static final int DISPLAY_ALL = 0;
	private static final int DISPLAY_LABEL = 1;
	private static final int DISPLAY_TASKID = 2;
	private static final int DISPLAY_DATEASC = 3;
	private static final int DISPLAY_SEARCH = 4;
	private String displayCondition_;
	private String[] displayConditionArray_;
	private int displayOption_ = DISPLAY_ALL;
	private boolean search_ = false;
	private boolean invalidInput_ = false;
	private boolean wrongInputFormat_ = false;
	private boolean addedDateTimeLabels_ = false;
	Comparator<Task> DATE_COMPARATOR = new Comparator<Task>() {
		@Override
		public int compare(Task t1, Task t2) {
			if (t1.getFinishBy_() == null && t2.getFinishBy_() == null) {
				return 0;
			} else if (t1.getFinishBy_() == null) {
				return 1;
			} else if (t2.getFinishBy_() == null) {
				return -1;
			} else {
				long n1 = t1.getFinishBy_().getTime();
				long n2 = t2.getFinishBy_().getTime();
				if (n1 > n2)
					return 1;
				else if (n1 < n2)
					return -1;
				else
					return 0;
			}
		}
	};

	private static enum Options {
		NO_MATCHING, LABEL, TASKID, DATE_ASCENDING, SEARCH
	};

	private Storage storage_;

	/**
	 * constructor.<br>
	 */
	public DisplayCommand() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.cmd.Command#setContainers()
	 */
	/**
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3Cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 */
	@Override
	public void constructCommand(String input) {
		String[] parameters = input.split("\\s+");
		parseOptions(parameters);
	}

	/**
	 * this method will check the flag of the command and display the task
	 * accordingly.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 * 
	 * @param parameters
	 *            the command flags and parameters.
	 */
	private void parseOptions(String[] parameters) {
		String[] displayArray = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Options optionType = checkOptionsType(parameters[i]);

			if (optionType == Options.NO_MATCHING) {

			} else if (optionType == Options.DATE_ASCENDING) {
				displayOption_ = DISPLAY_DATEASC;
				break;
			} else if (optionType == Options.SEARCH) {
				search_ = true;
				displayOption_ = DISPLAY_SEARCH;
				displayArray[i] = parameters[i];
			} else {
				int nextOption = i + 1;
				if (nextOption < parameters.length) {
					setOption(optionType, parameters[nextOption]);
					break;
				}
			}
		}
		displayConditionArray_ = displayArray;
	}

	/**
	 * this method will check what options it is.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 * 
	 * @param input
	 *            the option to be checked.
	 * @return the type of option if valid.
	 */

	private Options checkOptionsType(String input) {
		Options option = Options.NO_MATCHING;

		if (input.equalsIgnoreCase("-l") || input.startsWith("label")) {
			option = Options.LABEL;
		} else if (input.equalsIgnoreCase("-t") || input.equalsIgnoreCase("id")) {
			option = Options.TASKID;
		} else if (input.equalsIgnoreCase("-d") || input.startsWith("asc")) {
			option = Options.DATE_ASCENDING;
		} else if (!input.equalsIgnoreCase("")) {
			option = Options.SEARCH;
		}
		return option;
	}

	/**
	 * this method will set the option for the task given the option type.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 * 
	 * @param optionType
	 *            the type of option to set.
	 * @param value
	 *            the value to be set to the option.
	 */

	private void setOption(Options optionType, String value) {

		switch (optionType) {
		case LABEL:
			displayTaskByLabel(value);
			break;
		case TASKID:
			displayTaskByTaskId(value);
			break;
		}
	}

	/**
	 * this method will set the option for displaying by label.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 * 
	 * @param value
	 *            the value to be set to the option.
	 */
	private void displayTaskByLabel(String value) {
		// TODO Auto-generated method stub
		displayCondition_ = value;
		displayOption_ = DISPLAY_LABEL;
	}

	/**
	 * this method will set the option for displaying by task ID.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3Cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 * 
	 * @param value
	 *            the value to be set to the option.
	 */
	private void displayTaskByTaskId(String value) {
		// TODO Auto-generated method stub
		displayCondition_ = value;
		displayOption_ = DISPLAY_TASKID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.cmd.Command#setContainers()
	 */
	/**
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 */
	@Override
	public Display run() {
		Vector<Task> tasksArray = new Vector<Task>();
		for (Task t : storage_.getTasks_()) {
			tasksArray.add(t.clone());
		}

		assert tasksArray != null;
		Vector<Integer> tasksSearch = new Vector<Integer>(tasksArray.size());
		String taskHeading = "ID\t\tTask\t\t\t\tStart Date\t\tEnd Date\t\tLabels\n"
				+ "--\t\t----\t\t\t\t----------\t\t--------\t\t-----\n";
		String taskFull = "";
		String taskInformation = "";
		DisplayCLI display = new DisplayCLI();

		if (displayOption_ == DISPLAY_DATEASC) {
			Collections.sort(tasksArray, DATE_COMPARATOR);
		}

		if (search_ == true) {
			getMatchingTask(tasksArray, tasksSearch);
		}

		for (int i = 0; i < tasksArray.size(); i++) {
			boolean conditionForDisplay = false;
			Task t = tasksArray.get(i);

			conditionForDisplay = setCondition(t, conditionForDisplay,
					tasksSearch);

			taskInformation = displayTaskByConditions(t, taskInformation,
					conditionForDisplay);
		}

		taskInformation = printInvalidInput(taskInformation);
		if (invalidInput_ == true) {
			taskHeading = "";
		}
		taskFull = taskHeading + taskInformation;
		assert !taskFull.equals("");
		display.setData_(taskFull.trim());
		return display;
	}

	/**
	 * this method will store matching tasks with keyword into a vector. <br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param tasksArray
	 *            the vector containing the task.
	 * @param tasksSearch
	 *            the vector containing matching tasks ID for search function.
	 */
	private void getMatchingTask(Vector<Task> tasksArray,
			Vector<Integer> tasksSearch) {
		for (int i = 0; i < tasksArray.size(); i++) {
			Task t = tasksArray.get(i);
			String[] splitTaskName = t.getTaskName_().split("\\s+");
			String[] splitLabels = new String[5];
			searchMatchingTasksInNameAndLabels(t, tasksSearch, splitTaskName,
					splitLabels);
		}
	}

	/**
	 * this method will set the condition for display.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3Cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 * 
	 * @param t
	 *            Task for validation.
	 * @param conditionForDisplay
	 *            the validation for display.
	 * @param tasksSearch
	 *            the vector containing matching tasks ID for search function.
	 */
	private boolean setCondition(Task t, boolean conditionForDisplay,
			Vector<Integer> tasksSearch) {
		if (displayOption_ == DISPLAY_ALL || displayOption_ == DISPLAY_DATEASC) {
			conditionForDisplay = true;
		} else if (displayOption_ == DISPLAY_LABEL) {
			for (int l = 0; l < t.getLabels_().size(); l++) {
				if (t.getLabels_().get(l).getLabelName_()
						.equalsIgnoreCase(displayCondition_)) {
					conditionForDisplay = true;
				}
			}
		} else if (displayOption_ == DISPLAY_TASKID) {
			try {
				conditionForDisplay = (t.getTaskId_() == Integer
						.parseInt(displayCondition_));
			} catch (NumberFormatException e) {
				wrongInputFormat_ = true;
			}
		} else if (search_ == true) {
			for (int j = 0; j < tasksSearch.size(); j++) {
				if (tasksSearch.get(j) == t.getTaskId_()) {
					conditionForDisplay = true;
				}
			}

		}
		return conditionForDisplay;
	}

	/**
	 * this method will display task according to the user input. <br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param taskInformation
	 *            the message to be displayed for invalid input.
	 * @param conditionForDisplay
	 *            the validation for display.
	 */
	private String displayTaskByConditions(Task t, String taskInformation,
			boolean conditionForDisplay) {
		if (conditionForDisplay) {
			taskInformation += t.getTaskId_() + "\t";

			if (t.getTaskName_().length() < 39) {
				taskInformation = displayShortTaskName(t, taskInformation);
			} else {
				taskInformation = displayLongTaskName(t, taskInformation);
			}
		}
		return taskInformation;
	}

	/**
	 * this method will set the display for invalid input.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.1
	 * 
	 * @param taskInformation
	 *            the message to be displayed for invalid input.
	 */
	private String printInvalidInput(String taskInformation) {
		if (displayOption_ == DISPLAY_LABEL
				&& taskInformation.equalsIgnoreCase("")) {
			taskInformation = "There is no such label";
			invalidInput_ = true;

		} else if (displayOption_ == DISPLAY_TASKID
				&& wrongInputFormat_ == true) {
			taskInformation = "Please check your input. Task ID should be numeric.";
			invalidInput_ = true;

		} else if (displayOption_ == DISPLAY_TASKID
				&& taskInformation.equalsIgnoreCase("")) {
			taskInformation = "There is no such task";
			invalidInput_ = true;

		} else if (displayOption_ == DISPLAY_ALL
				&& taskInformation.equalsIgnoreCase("")) {
			taskInformation = "There is no task!";
			invalidInput_ = true;
		} else if (search_ && taskInformation.equalsIgnoreCase("")) {
			taskInformation = "There is no matching task!";
			invalidInput_ = true;
		}
		return taskInformation;
	}

	/**
	 * this method will search task's task name and label with the keyword.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param tasksSearch
	 *            the vector containing matching task.
	 * @param splitTaskName
	 *            String array that contains separated task name.
	 * @param splitLabels
	 *            String array that contains labels.
	 */
	private void searchMatchingTasksInNameAndLabels(Task t,
			Vector<Integer> tasksSearch, String[] splitTaskName,
			String[] splitLabels) {
		for (int m = 0; m < displayConditionArray_.length; m++) {
			addMatchingTaskToTaskIDVector(t, tasksSearch, splitTaskName, m);

			splitLabels = splitLabelsToArray(t, splitLabels, m);
			if (!tasksSearch.contains(t.getTaskId_())) {
				addMatchingTaskToTaskIDVector(t, tasksSearch, splitLabels, m);
			}

		}
	}

	/**
	 * this method will display task with string length shorter than 39. <br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param taskInformation
	 *            the message to be displayed for invalid input.
	 */
	private String displayShortTaskName(Task t, String taskInformation) {
		taskInformation += t.getTaskName_();

		taskInformation = alignTaskNameColumn(taskInformation, t.getTaskName_());

		taskInformation = displayDateTimeLabels(t, taskInformation);
		return taskInformation;
	}

	/**
	 * this method will display task with string length longer than 39. <br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param taskInformation
	 *            the message to be displayed for invalid input.
	 */
	private String displayLongTaskName(Task t, String taskInformation) {
		String[] splitTaskName = t.getTaskName_().split("\\s+");
		boolean firstLongStringInTaskName = false;
		if (!t.getTaskName_().contains(" ")) {
			splitTaskName[0] = t.getTaskName_();
		}
		String combinedTaskName = "";
		String combinedTaskName2 = "";
		int splitted = 0;

		for (int j = 0; j < splitTaskName.length; j++) {
			String currentSplitTaskName = splitTaskName[j];
			if (splitTaskName.length == 1) {
				combinedTaskName2 += splitTaskName[j];
			}
			if (j + 1 != splitTaskName.length) {
				combinedTaskName2 += splitTaskName[j] + " "
						+ splitTaskName[j + 1];
			}

			if (combinedTaskName2.length() > 39 && splitted == 0) {

				firstLongStringInTaskName = true;
				splitLongStringInTaskName(t, currentSplitTaskName,
						firstLongStringInTaskName);
				splitTaskName[j] = currentSplitTaskName;
				combinedTaskName += splitTaskName[j];
				splitted++;
				taskInformation += combinedTaskName;

				if (addedDateTimeLabels_ == false) {
					taskInformation = alignTaskNameColumn(taskInformation,
							combinedTaskName);

					taskInformation = displayDateTimeLabels(t, taskInformation);
					taskInformation += "\t";
				}
				combinedTaskName = "";
				combinedTaskName2 = "";
			} else if (combinedTaskName2.length() > 39) {

				splitLongStringInTaskName(t, currentSplitTaskName,
						firstLongStringInTaskName);
				splitTaskName[j] = currentSplitTaskName;
				combinedTaskName += splitTaskName[j] + "\n\t";
				taskInformation += combinedTaskName;
				splitted++;
				combinedTaskName = "";
				combinedTaskName2 = "";
			} else {
				combinedTaskName += splitTaskName[j] + " ";
			}
		}
		taskInformation += combinedTaskName + "\n";
		return taskInformation;
	}

	/**
	 * this method will search task name and labels according<br>
	 * to keyword and add the matching task to vector.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param tasksSearch
	 *            the vector containing matching task.
	 * @param splitTaskName
	 *            String array that contains separated task name.
	 * @param m
	 *            the increment for displayConditionArray.
	 */
	private void addMatchingTaskToTaskIDVector(Task t,
			Vector<Integer> tasksSearch, String[] splitTaskName, int m) {
		for (int j = 0; j < splitTaskName.length; j++) {
			if (splitTaskName[j] != null
					&& splitTaskName[j].indexOf(displayConditionArray_[m]) > -1) {
				tasksSearch.add(t.getTaskId_());
			}

		}
	}

	/**
	 * this method parse label vector into an array.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param splitLabels
	 *            String array that contains labels.
	 * @param m
	 *            the increment for displayConditionArray.
	 */
	private String[] splitLabelsToArray(Task t, String[] splitLabels, int m) {
		for (int l = 0; l < t.getLabels_().size(); l++) {
			if (t.getLabels_().get(l).getLabelName_()
					.indexOf(displayConditionArray_[m]) > -1) {
				splitLabels = t.getLabels_().get(l).getLabelName_()
						.split("\\s+");
			}
		}
		return splitLabels;
	}

	/**
	 * this method will align the task name column.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param taskInformation
	 *            the message to be displayed for invalid input.
	 * @param combinedTaskName
	 *            String that contains separated task name.
	 */
	private String alignTaskNameColumn(String taskInformation,
			String combinedTaskName) {
		if (combinedTaskName.length() < 8) {
			taskInformation += "\t\t\t\t\t";
		} else if (combinedTaskName.length() < 16) {
			taskInformation += "\t\t\t\t";
		} else if (combinedTaskName.length() < 24) {
			taskInformation += "\t\t\t";
		} else if (combinedTaskName.length() < 32) {
			taskInformation += "\t\t";
		} else {
			taskInformation += "\t";
		}
		return taskInformation;
	}

	/**
	 * this method will add start date, start time, end date, <br>
	 * end time and labels to the display string.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param taskInformation
	 *            the message to be displayed for invalid input.
	 */
	private String displayDateTimeLabels(Task t, String taskInformation) {

		if (t.getStartFrom_() != null) {
			taskInformation += t.getStartFrom_();
		} else {
			taskInformation += "\t    ";
		}
		if (t.getStartTime_() != null) {
			if (t.getStartFrom_() != null) {
				taskInformation += ", ";
				taskInformation += t.getStartTime_() + "\t";
			} else {
				taskInformation += "\t\t";
			}
		} else {
			taskInformation += "\t\t";
		}
		if (t.getFinishBy_() != null) {
			taskInformation += t.getFinishBy_();
		} else {
			taskInformation += "\t    ";
		}
		if (t.getFinishTime_() != null) {
			if (t.getFinishBy_() != null) {
				taskInformation += ", ";
				taskInformation += t.getFinishTime_() + "\t";
			} else {
				taskInformation += "\t\t";
			}
		} else {
			taskInformation += "\t\t";
		}
		if (t.getLabels_() != null) {

			if (t.getLabels_().size() > 0) {
				for (int l = 0; l < t.getLabels_().size(); l++) {
					if (l > 0) {
						taskInformation += ", ";
					}
					taskInformation += t.getLabels_().get(l);
				}
			}
			taskInformation += "\n";
		} else {
			taskInformation += "\n";
		}
		return taskInformation;
	}
	
	/**
	 * this method will split the first word in Task Name <br>
	 * if it is too long.<br>
	 * 
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3cube
	 * @reviewdate 4 October 2011
	 * 
	 * @since v0.2
	 * 
	 * @param t
	 *            Task for validation.
	 * @param currentSplitTaskName
	 *            A specified task name.
	 * @param firstStringInTaskName
	 *            Verify if the first word in Task Name is very lengthy.
	 */
	private void splitLongStringInTaskName(Task t, String currentSplitTaskName,
			boolean firstStringInTaskName) {

		if (currentSplitTaskName.length() > 39) {
			int noOfLinesForString = currentSplitTaskName.length() / 38;
			StringBuffer longString = new StringBuffer(currentSplitTaskName);
			if (firstStringInTaskName) {
				String dateTimeLabel = "";
				dateTimeLabel = displayDateTimeLabels(t, dateTimeLabel);
				longString.insert(37, "-\t" + dateTimeLabel + "\t");
				addedDateTimeLabels_ = true;
				for (int l = 2; l < noOfLinesForString + 1; l++) {
					longString.insert((37 * l) + (dateTimeLabel.length()),
							"-\n\t");
				}
			} else {
				for (int l = 1; l < noOfLinesForString + 1; l++) {
					longString.insert(37 * (l), "-\n\t");
				}
			}
			currentSplitTaskName = longString.toString();
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.cmd.Command#setContainers()
	 */
	/**
	 * @programmer Chin Gui Pei
	 * @start 21 September 2011
	 * @finish 4 October 2011
	 * 
	 * @reviewer The3Cube
	 * @reviewdate 4 October 2011
	 * 
	 */
	@Override
	public Display undo() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.blogspot.the3cube.beefree.logic.cmd.Command#setStorage(com.blogspot.the3cube.beefree.storage.Storage)
	 */
	@Override
	public void setStorage(Storage storage) {
		// TODO Auto-generated method stub
		this.storage_ = storage;
	}

}

