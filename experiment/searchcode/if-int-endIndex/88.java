package com.blogspot.the3cube.beefree.logic.cmd;

import java.sql.Date;
import java.util.Vector;

import com.blogspot.the3cube.beefree.logic.DateTimeUtil;
import com.blogspot.the3cube.beefree.storage.Storage;
import com.blogspot.the3cube.beefree.util.Blockout;
import com.blogspot.the3cube.beefree.util.Display;
import com.blogspot.the3cube.beefree.util.DisplayCLI;

/**
 * Handle and execute the blockout command.<br>
 * 
 * @author 		The3Cube
 * @since 		v0.1
 * @version 	v0.2
 * 
 * <p><pre>
 * command syntax:<br>
 * blockout blockoutName from fromDate to toDate<br>
 * OR<br>
 * blockout blockoutName on date<br>
 * OR
 * blockout remove blockoutId
 * OR
 * blockout display<br><br>
 * </pre></p>
 * <p><pre>
 * example: <br>
 * blockout Holiday from 2011-09-01 to 2011-09-10<br>
 * this would add a blockout period with the following:<br>
 * blockout name: 	Holiday<br>
 * from: 			1st Sept 2011<br> 
 * to: 			10th Sept 2011<br><br>
 * 
 * blockout Holiday on 2011-09-20<br>
 * this would add a blockout period with the following:<br>
 * blockout name: 	Holiday<br>
 * on: 			20th Sept 2011<br><br>
 * 
 * blockout delete 1<br>
 * this would delete the blockout with the id 1<br><br>
 * 
 * blockout display<br>
 * this would display all the blockout which is currently in the system<br><br>
 * </pre></p>
 */
public class BlockoutCommand implements Command {
	private static enum Options {
		DISPLAY, DELETE, NO_MATCHING, START_DATE, FINISH_DATE
	};

	private static enum SupportedAction {
		DISPLAY, DELETE, ADD
	};

	private final static String KEY_STRING_ON = " on ";
	private final static String KEY_STRING_FROM = " from ";
	private final static String KEY_STRING_TO = " to ";

	private final static String KEY_STRING_SPACE = " ";

	private Storage storage_;

	private Blockout newBlockout_;
	private SupportedAction action_;
	private Vector<Blockout> toBeDelete_;

	public BlockoutCommand() {
		newBlockout_ = new Blockout();
		newBlockout_.setBlockoutName_("");

		toBeDelete_ = new Vector<Blockout>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.blogspot.the3cube.beefree.cmd.Command#constructCommand(java.lang.
	 * String)
	 */
	@Override
	public void constructCommand(String input) {
		assert input != null;
		
		String[] parameters = input.split("\\s+");

		Options optionType = Options.NO_MATCHING;

		if (input.equalsIgnoreCase("display")) {
			optionType = Options.DISPLAY;
		}

		if (parameters.length > 0) {
			if (parameters[0].equalsIgnoreCase("delete")) {
				optionType = Options.DELETE;
			}
			if (parameters[0].equalsIgnoreCase("remove")) {
				optionType = Options.DELETE;
			}
		}

		if (optionType == Options.DISPLAY) {
			action_ = SupportedAction.DISPLAY;
		} else if (optionType == Options.DELETE) {
			action_ = SupportedAction.DELETE;
			if (parameters.length > 1) {
				removeBlockout(parameters[1]);
			}
		} else {
			action_ = SupportedAction.ADD;
			parseOptions(input);
		}
	}

	/**
	 * this method will set the action to delete and parase the id to delete.<br>
	 * 
	 * @programmer 		Chua Jie Sheng
	 * @start 			01 October 2011
	 * @finish 			02 October 2011
	 * 
	 * @reviewer 		The3Cube
	 * @reviewdate 		04 October 2011
	 * 
	 * @since 			v0.1
	 * 
	 * @param input 	the input, could be one number or seperated by ';'
	 */
	private void removeBlockout(String input) {
		assert input != null;
		
		String[] parameters = input.split(";");
		for (int i = 0; i < parameters.length; i++) {
			int id = -1;
			boolean isNumber = parameters[i].matches("[0-9]*");
			if (isNumber) {
				id = Integer.parseInt(parameters[i]);
			}
			findAndAddBlockout(id);
		}
	}

	/**
	 * this method will find from the blockout container and add to toBeDelete
	 * vector.<br>
	 * 
	 * @programmer 			Chua Jie Sheng
	 * @start 				01 October 2011
	 * @finish 				02 October 2011
	 * 
	 * @reviewer 			The3Cube
	 * @reviewdate 			04 October 2011
	 * 
	 * @since 				v0.1
	 * 
	 * @param blockoutId 	the blockout id to find.
	 */
	private void findAndAddBlockout(int blockoutId) {
		Blockout blockout = storage_.findBlockout(blockoutId);
		if (blockout != null) {
			toBeDelete_.add(blockout);
		}
	}

	/**
	 * this method will check the various flag of the command and set them
	 * respective.<br>
	 * 
	 * @programmer 	Chua Jie Sheng
	 * @start 		01 October 2011
	 * @finish 		02 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	04 October 2011
	 * 
	 * @since 		v0.1
	 * 
	 * @param cmd 	the parameters split according to white space.
	 */
	private void parseOptions(String cmd) {
		assert cmd != null;
		
		int indexOfOn = cmd.indexOf(KEY_STRING_ON);
		int indexOfFrom = cmd.indexOf(KEY_STRING_FROM);
		int indexOfTo = cmd.indexOf(KEY_STRING_TO);

		if (indexOfOn > -1) {
			int startIndex = indexOfOn + KEY_STRING_ON.length();
			int endIndex = cmd.indexOf(KEY_STRING_SPACE, startIndex);
			if (endIndex < 0) {
				endIndex = cmd.length();
			}
			String data = cmd.substring(startIndex, endIndex);
			Date date = DateTimeUtil.DateParser(data);

			if (date != null) {
				newBlockout_.setStartBlockout_(date);
				newBlockout_.setEndBlockout_(date);
			}
		} else {
			if (indexOfFrom > -1) {
				int startIndex = indexOfFrom + KEY_STRING_FROM.length();
				int endIndex = cmd.indexOf(KEY_STRING_SPACE, startIndex);
				if (endIndex < 0) {
					endIndex = cmd.length();
				}
				String data = cmd.substring(startIndex, endIndex);
				Date date = DateTimeUtil.DateParser(data);

				if (date != null) {
					newBlockout_.setStartBlockout_(date);
				}
			}
			if (indexOfTo > -1) {
				int startIndex = indexOfTo + KEY_STRING_TO.length();
				int endIndex = cmd.indexOf(KEY_STRING_SPACE, startIndex);
				if (endIndex < 0) {
					endIndex = cmd.length();
				}
				String data = cmd.substring(startIndex, endIndex);
				Date date = DateTimeUtil.DateParser(data);

				if (date != null) {
					newBlockout_.setEndBlockout_(date);
				}
			}
		}

		boolean isStartDateNull = newBlockout_.getStartBlockout_() == null;
		boolean isEndDateNull = newBlockout_.getEndBlockout_() == null;

		if (isStartDateNull && isEndDateNull) {
			newBlockout_ = null;
			return;
		} else if (isStartDateNull) {
			newBlockout_.setStartBlockout_(newBlockout_.getEndBlockout_());
		} else if (isEndDateNull) {
			newBlockout_.setEndBlockout_(newBlockout_.getStartBlockout_());
		}

		if (indexOfOn < 0) {
			indexOfOn = cmd.length();
		}
		if (indexOfFrom < 0) {
			indexOfFrom = cmd.length();
		}
		if (indexOfTo < 0) {
			indexOfTo = cmd.length();
		}

		int indexOfFirstKeyWord = cmd.length();
		if (indexOfOn < indexOfFirstKeyWord) {
			indexOfFirstKeyWord = indexOfOn;
		}
		if (indexOfFrom < indexOfFirstKeyWord) {
			indexOfFirstKeyWord = indexOfFrom;
		}
		if (indexOfTo < indexOfFirstKeyWord) {
			indexOfFirstKeyWord = indexOfTo;
		}

		String name = cmd.substring(0, indexOfFirstKeyWord).trim();
		if (newBlockout_ != null) {
			newBlockout_.setBlockoutName_(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.cmd.Command#run()
	 */
	/**
	 * @programmer 	Chua Jie Sheng
	 * @start	 	01 October 2011
	 * @finish 		02 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate	04 October 2011
	 * 
	 * @since 		v0.1
	 */
	@Override
	public Display run() {
		DisplayCLI cli = new DisplayCLI();
		boolean completedSuccessfully = true;

		if (action_ == SupportedAction.ADD) {
			if (newBlockout_ == null) {
				cli.setData_("The command you entered is invalid. " 
							+ "Please refer to \"help blockout\".");
			} else {
				completedSuccessfully = storage_.addBlockout(newBlockout_);
				String data = "";
				if (completedSuccessfully) {
					data = newBlockout_.getBlockoutName_()
							+ " blockout is added.";
				} else {
					data = "We are unable to add " + newBlockout_.getBlockoutName_() 
							+ ", please try again. Contact us if needed.";
				}
				cli.setData_(data);
			}
		} else if (action_ == SupportedAction.DISPLAY) {
			Vector<Blockout> blockouts = storage_.getBlockouts_();
			String data = "";
			if (blockouts.size() > 0) {
				for (int i = 0; i < blockouts.size(); i++) {
					Blockout b = blockouts.get(i);
					data = data + b.getDetailsString() + "\n";
				}
				cli.setData_(data.trim());
			} else {
				cli.setData_("There is currently no blockout in the system.");
			}
			completedSuccessfully = true;
		} else if (action_ == SupportedAction.DELETE) {
			String data = "";
			for (int i = 0; i < toBeDelete_.size(); i++) {
				Blockout b = toBeDelete_.get(i);
				boolean deleted = storage_.deleteBlockout(b);
				if (deleted) {
					data = data + "Blockout by the name of, " + b.getBlockoutName_()
							+ " have been deleted." + "\n";
				} else {
					toBeDelete_.remove(i);
					data = data + "We are unable to delete the blockout of the name, " 
							+ b.getBlockoutName_() 
							+ ", please try again. Contact us if needed."
							+ "\n";
				}
				completedSuccessfully = deleted && completedSuccessfully;
			}
			cli.setData_(data.trim());
		}
		return cli;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.blogspot.the3cube.beefree.cmd.Command#undo()
	 */
	/**
	 * @programmer 	Chua Jie Sheng
	 * @start 		01 October 2011
	 * @finish 		02 October 2011
	 * 
	 * @reviewer 	The3Cube
	 * @reviewdate 	04 October 2011
	 * 
	 * @since 		v0.1
	 */
	@Override
	public Display undo() {
		DisplayCLI cli = new DisplayCLI();
		boolean completedSuccessfully = true;

		if (action_ == SupportedAction.ADD) {
			if (newBlockout_ == null) {
				cli.setData_("We unable to undo this command as a invalid command was entered previously.");
			} else {
				completedSuccessfully = storage_.deleteBlockout(newBlockout_);
				String data = "";
				if (completedSuccessfully) {
					data = "Blockout by the name of, " 
							+ newBlockout_.getBlockoutName_() 
							+ " have been deleted.";
				} else {
					data = "We are unable to delete the blockout of the name, " 
							+ newBlockout_.getBlockoutName_() 
							+ ", please try again. Contact us if needed.";
				}
				cli.setData_(data);
			}
		} else if (action_ == SupportedAction.DISPLAY) {
			cli.setData_("We unable to undo this command as no changes in made.");
		} else if (action_ == SupportedAction.DELETE) {
			for (int i = 0; i < toBeDelete_.size(); i++) {
				Blockout b = toBeDelete_.get(i);
				boolean add = storage_.addBlockout(b);
				if (add) {
					String data = b.getBlockoutName_()
							+ " blockout is added." + "\n" + cli.getData_();
					cli.setData_(data);
				} else {
					toBeDelete_.remove(i);
					String data = "We are unable to add " 
							+ b.getBlockoutName_() 
							+ ", please try again. Contact us if needed."
							+ "\n" + cli.getData_();
					cli.setData_(data);
				}
				completedSuccessfully = add && completedSuccessfully;
			}
		}
		return cli;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.blogspot.the3cube.beefree.logic.cmd.Command#setStorage(com.blogspot
	 * .the3cube.beefree.storage.Storage)
	 */
	/**
	 * @programmer 	Chua Jie Sheng
	 * @start 		11 October 2011
	 * @finish 		11 October 2011
	 * 
	 * @reviewer
	 * @reviewdate
	 * 
	 * @since 		v0.2
	 */
	@Override
	public void setStorage(Storage storage) {
		assert storage != null;
		
		storage_ = storage;
	}

}

