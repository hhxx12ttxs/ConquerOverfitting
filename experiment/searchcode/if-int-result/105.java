package org.loon.framework.android.game.action.avg.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.resource.Resources;
import org.loon.framework.android.game.utils.StringUtils;
import org.loon.framework.android.game.utils.collection.ArrayMap;

/**
 * Copyright 2008 - 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email ceponline@yahoo.com.cn
 * @version 0.1.2
 */
@SuppressWarnings("unchecked")
public class Command extends Conversion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// ????
	private static Map cacheScript;

	// ????
	private static ArrayMap functions;

	// ????
	private static Map setEnvironmentList;

	// ??????
	private static ArrayMap conditionEnvironmentList;

	// ??????
	private static StringBuffer reader;

	// ?????
	private String cacheCommandName;

	// ?????
	private boolean flaging = false;

	// ?????
	private boolean ifing = false;

	// ?????
	private boolean functioning = false;

	// ????
	private boolean esleflag = false;

	private boolean backIfBool = false;

	private String executeCommand;

	private String nowPosFlagName;

	private boolean addCommand;

	private boolean isInnerCommand;

	private boolean isRead;

	private boolean isCall;

	private boolean isCache;

	private boolean if_bool;

	private boolean elseif_bool;

	private Command innerCommand;

	private String commandString;

	private List temps;

	private List printTags;

	private List randTags;

	private int scriptSize;

	private int offsetPos;

	// ??????
	private List scriptList;

	// ???
	private String scriptName;

	/**
	 * ?????????????
	 * 
	 * @param fileName
	 */
	public Command(String fileName) {
		initCommand();
		formatCommand(fileName);
	}

	/**
	 * ?????????list??
	 * 
	 * @param resource
	 */
	public Command(String fileName, List resource) {
		initCommand();
		formatCommand("function", resource);
	}

	public static void initCommand() {
		if (cacheScript == null) {
			cacheScript = Collections.synchronizedMap(new HashMap(1500));
			functions = new ArrayMap(20);
			setEnvironmentList = Collections.synchronizedMap(new HashMap(20));
			conditionEnvironmentList = new ArrayMap(30);
			reader = new StringBuffer(3000);
		}
	}

	public void formatCommand(String fileName) {
		formatCommand(fileName, Command.includeFile(fileName));
	}

	public void formatCommand(String name, List resource) {
		conditionEnvironmentList.clear();
		setEnvironmentList.put(V_SELECT_KEY, "-1");
		scriptName = name;
		scriptList = resource;
		scriptSize = scriptList.size();
		offsetPos = 0;
		flaging = false;
		ifing = false;
		isCache = true;
		esleflag = false;
		backIfBool = false;
	}

	private boolean setupIF(String commandString, String nowPosFlagName,
			Map setEnvironmentList, Map conditionEnvironmentList) {
		boolean result = false;
		conditionEnvironmentList.put(nowPosFlagName, new Boolean(false));
		try {
			List temps = commandSplit(commandString);
			Object valueA = (String) temps.get(1);
			Object valueB = (String) temps.get(3);
			valueA = setEnvironmentList.get(valueA) == null ? valueA
					: setEnvironmentList.get(valueA);
			valueB = setEnvironmentList.get(valueB) == null ? valueB
					: setEnvironmentList.get(valueB);

			// ????
			if (!isNumber(valueB)) {
				try {
					// ??????????
					valueB = compute.parse(valueB);
				} catch (Exception e) {
				}
			}
			String condition = (String) temps.get(2);

			// ????
			if (valueA == null || valueB == null) {
				conditionEnvironmentList
						.put(nowPosFlagName, new Boolean(false));
			}

			// ??
			if ("==".equals(condition)) {

				conditionEnvironmentList.put(nowPosFlagName, new Boolean(
						result = valueA.toString().equals(valueB.toString())));
				// ??
			} else if ("!=".equals(condition)) {
				conditionEnvironmentList.put(nowPosFlagName, new Boolean(
						result = !valueA.toString().equals(valueB.toString())));
				// ??
			} else if (">".equals(condition)) {
				int numberA = Integer.parseInt(valueA.toString());
				int numberB = Integer.parseInt(valueB.toString());
				conditionEnvironmentList.put(nowPosFlagName, new Boolean(
						result = numberA > numberB));
				// ??
			} else if ("<".equals(condition)) {
				int numberA = Integer.parseInt(valueA.toString());
				int numberB = Integer.parseInt(valueB.toString());
				conditionEnvironmentList.put(nowPosFlagName, new Boolean(
						result = numberA < numberB));

				// ????
			} else if (">=".equals(condition)) {
				int numberA = Integer.parseInt(valueA.toString());
				int numberB = Integer.parseInt(valueB.toString());
				conditionEnvironmentList.put(nowPosFlagName, new Boolean(
						result = numberA >= numberB));
				// ????
			} else if ("<=".equals(condition)) {
				int numberA = Integer.parseInt(valueA.toString());
				int numberB = Integer.parseInt(valueB.toString());
				conditionEnvironmentList.put(nowPosFlagName, new Boolean(
						result = numberA <= numberB));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * ??????
	 * 
	 */
	public void openCache() {
		isCache = true;
	}

	/**
	 * ??????
	 * 
	 */
	public void closeCache() {
		isCache = false;
	}

	/**
	 * ????????
	 * 
	 * @return
	 */
	public String nowCacheOffsetName() {
		if (scriptName == null) {
			return "DELAULT_CACHE";
		}
		return (scriptName + FLAG + offsetPos + FLAG + commandString)
				.toLowerCase();
	}

	/**
	 * ??????
	 * 
	 */
	public static void resetCache() {
		if (cacheScript != null) {
			cacheScript.clear();
		}
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	/**
	 * ???????????
	 * 
	 * @return
	 */
	public synchronized String[] getReads() {
		String result = reader.toString();
		result = result.replaceAll(SELECTS_TAG, "");
		return split(result, FLAG);
	}

	/**
	 * ???????????
	 * 
	 * @param index
	 * @return
	 */
	public synchronized String getRead(int index) {
		try {
			return getReads()[index];
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * ????????????
	 * 
	 * @param messages
	 * @param startString
	 * @param endString
	 * @return
	 */
	public static String getNameTag(String messages, String startString,
			String endString) {
		List results = getNameTags(messages, startString, endString);
		return (results == null || results.size() == 0) ? null
				: (String) results.get(0);
	}

	/**
	 * ?????????list
	 * 
	 * @param messages
	 * @param startString
	 * @param endString
	 * @return
	 */
	public static List getNameTags(String messages, String startString,
			String endString) {
		return Command.getNameTags(messages.toCharArray(), startString
				.toCharArray(), endString.toCharArray());
	}

	/**
	 * ?????????list
	 * 
	 * @param messages
	 * @param startString
	 * @param endString
	 * @return
	 */
	public static List getNameTags(char[] messages, char[] startString,
			char[] endString) {
		int dlength = messages.length;
		int slength = startString.length;
		int elength = endString.length;
		List tagList = new ArrayList(10);
		boolean lookup = false;
		int lookupStartIndex = 0;
		int lookupEndIndex = 0;
		int length;
		StringBuffer sbr = new StringBuffer(100);
		for (int i = 0; i < dlength; i++) {
			char tag = messages[i];
			if (tag == startString[lookupStartIndex]) {
				lookupStartIndex++;
			}
			if (lookupStartIndex == slength) {
				lookupStartIndex = 0;
				lookup = true;
			}
			if (lookup) {
				sbr.append(tag);
			}
			if (tag == endString[lookupEndIndex]) {
				lookupEndIndex++;
			}
			if (lookupEndIndex == elength) {
				lookupEndIndex = 0;
				lookup = false;
				length = sbr.length();
				if (length > 0) {
					tagList.add(sbr.substring(1, sbr.length() - elength));
					sbr.delete(0, length);
				}
			}
		}
		return tagList;
	}

	/**
	 * ??????
	 * 
	 * @param type
	 */
	public void select(int type) {
		if (innerCommand != null) {
			innerCommand.setVariable(V_SELECT_KEY, String.valueOf(type));
		}
		setVariable(V_SELECT_KEY, String.valueOf(type));
	}

	public String getSelect() {
		return (String) getVariable(V_SELECT_KEY);
	}

	/**
	 * ????
	 * 
	 * @param key
	 * @param value
	 */
	public void setVariable(String key, Object value) {
		setEnvironmentList.put(key, value);
	}

	/**
	 * ??????
	 * 
	 * @param vars
	 */
	public void setVariables(Map vars) {
		setEnvironmentList.putAll(vars);
	}

	/**
	 * ??????
	 * 
	 * @return
	 */
	public Map getVariables() {
		return setEnvironmentList;
	}

	public Object getVariable(String key) {
		return setEnvironmentList.get(key);
	}

	/**
	 * ????
	 * 
	 * @param key
	 */
	public void removeVariable(String key) {
		setEnvironmentList.remove(key);
	}

	/**
	 * ????????????
	 * 
	 * @return
	 */
	public boolean next() {
		return (offsetPos < scriptSize);
	}

	/**
	 * ?????????
	 * 
	 * @param offset
	 * @return
	 */
	public boolean gotoIndex(final int offset) {
		boolean result = offset < scriptSize && offset > 0
				&& offset != offsetPos;
		if (result) {
			offsetPos = offset;
		}
		return result;
	}

	/**
	 * ?????????????list??
	 * 
	 * @return
	 */
	public List batchToList() {
		List reslist = new ArrayList(scriptSize);
		for (; next();) {
			String execute = doExecute();
			if (execute != null) {
				reslist.add(execute);
			}
		}
		return reslist;
	}

	/**
	 * ?????????????string??
	 * 
	 * @return
	 */
	public String batchToString() {
		StringBuffer resString = new StringBuffer(scriptSize * 10);
		for (; next();) {
			String execute = doExecute();
			if (execute != null) {
				resString.append(execute);
				resString.append("\n");
			}
		}
		return resString.toString();
	}

	private void setupSET() {
		if (commandString.startsWith(SET_TAG)) {
			List temps = commandSplit(commandString);
			int len = temps.size();
			String result = null;
			if (len == 4) {
				result = temps.get(3).toString();
			} else if (len > 4) {
				StringBuffer sbr = new StringBuffer(len);
				for (int i = 3; i < temps.size(); i++) {
					sbr.append(temps.get(i));
				}
				result = sbr.toString();
			}

			if (result != null) {
				// ????????
				Set set = setEnvironmentList.entrySet();
				for (Iterator it = set.iterator(); it.hasNext();) {
					Entry entry = (Entry) it.next();
					if (!(result.startsWith("\"") && result.endsWith("\""))) {
						result = replaceMatch(result, (String) entry.getKey(),
								entry.getValue().toString());
					}
				}
				// ????????
				if (result.startsWith("\"") && result.endsWith("\"")) {
					setEnvironmentList.put(temps.get(1), result.substring(1,
							result.length() - 1));
				} else if (isChinese(result) || isEnglishAndNumeric(result)) {
					setEnvironmentList.put(temps.get(1), result);
				} else {
					// ????????
					setEnvironmentList.put(temps.get(1), compute.parse(result));

				}
			}
			addCommand = false;
		}

	}

	/**
	 * ?????
	 * 
	 */
	private void setupRandom() {
		// ?????
		if (commandString.indexOf(RAND_TAG) != -1) {
			randTags = Command.getNameTags(commandString, RAND_TAG
					+ BRACKET_LEFT_TAG, BRACKET_RIGHT_TAG);
			if (randTags != null) {
				for (Iterator it = randTags.iterator(); it.hasNext();) {
					String key = (String) it.next();
					Object value = setEnvironmentList.get(key);
					// ?????
					if (value != null) {
						commandString = Command
								.replaceMatch(
										commandString,
										(RAND_TAG + BRACKET_LEFT_TAG + key + BRACKET_RIGHT_TAG)
												.intern(), value.toString());
						// ??????????
					} else if (isNumber(key)) {
						commandString = Command
								.replaceMatch(
										commandString,
										(RAND_TAG + BRACKET_LEFT_TAG + key + BRACKET_RIGHT_TAG)
												.intern(),
										String
												.valueOf(GLOBAL_RAND
														.nextInt(Integer
																.parseInt((String) key))));
						// ???
					} else {
						commandString = Command
								.replaceMatch(
										commandString,
										(RAND_TAG + BRACKET_LEFT_TAG + key + BRACKET_RIGHT_TAG)
												.intern(), String
												.valueOf(GLOBAL_RAND.nextInt()));
					}
				}
			}
		}
	}

	private void innerCallTrue() {
		isCall = true;
		isInnerCommand = true;
	}

	private void innerCallFalse() {
		isCall = false;
		isInnerCommand = false;
		innerCommand = null;
	}

	/**
	 * ????????
	 * 
	 * @return
	 */
	public synchronized String doExecute() {

		executeCommand = null;

		addCommand = true;

		isInnerCommand = (innerCommand != null);

		if_bool = false;

		elseif_bool = false;

		try {
			// ??call??
			if (isInnerCommand && isCall) {
				setVariables(innerCommand.getVariables());
				if (innerCommand.next()) {
					return innerCommand.doExecute();
				} else {
					innerCallFalse();
					return executeCommand;
				}
				// ??????
			} else if (isInnerCommand && !isCall) {
				setVariables(innerCommand.getVariables());
				if (innerCommand.next()) {
					return innerCommand.doExecute();
				} else {
					innerCommand = null;
					isInnerCommand = false;
					return executeCommand;
				}
			}

			nowPosFlagName = String.valueOf(offsetPos);
			int length = conditionEnvironmentList.size();
			if (length > 0) {
				Object ifResult = conditionEnvironmentList.get(length - 1);
				if (ifResult != null) {
					backIfBool = ((Boolean) ifResult).booleanValue();
				}
			}

			// ??????
			commandString = ((String) scriptList.get(offsetPos));
			// ??????
			if (commandString.startsWith(RESET_CACHE_TAG)) {
				resetCache();
				return executeCommand;
			}

			if (isCache) {
				// ????????
				cacheCommandName = nowCacheOffsetName();
				// ???????
				Object cache = cacheScript.get(cacheCommandName);
				if (cache != null) {
					return (String) cache;
				}
			}

			// ???
			if (flaging) {
				flaging = !(commandString.startsWith(FLAG_LS_E_TAG) || commandString
						.endsWith(FLAG_LS_E_TAG));
				return executeCommand;
			}

			if (!flaging) {
				// ????
				if (commandString.startsWith(FLAG_LS_B_TAG)
						&& !commandString.endsWith(FLAG_LS_E_TAG)) {
					flaging = true;
					return executeCommand;
				} else if (commandString.startsWith(FLAG_LS_B_TAG)
						&& commandString.endsWith(FLAG_LS_E_TAG)) {
					return executeCommand;
				}
			}

			// ???????
			setupRandom();

			// ????????
			setupSET();

			// ??????????
			if (commandString.endsWith(END_TAG)) {
				functioning = false;
				return executeCommand;
			}

			// ??????????
			if (commandString.startsWith(BEGIN_TAG)) {
				temps = commandSplit(commandString);
				if (temps.size() == 2) {
					functioning = true;
					functions.put(temps.get(1), new ArrayList(10));
					return executeCommand;
				}
			}

			// ???????
			if (functioning) {
				ArrayList function = (ArrayList) functions
						.get(functions.size() - 1);
				function.add(commandString);
				return executeCommand;
			}

			// ?????????
			if (commandString.startsWith(CALL_TAG) && !isCall) {
				temps = commandSplit(commandString);
				if (temps.size() == 2) {
					String functionName = (String) temps.get(1);
					List funs = (ArrayList) functions.get(functionName);
					if (funs != null) {
						innerCommand = new Command(scriptName + FLAG
								+ functionName, funs);
						innerCommand.closeCache();
						innerCommand.setVariables(getVariables());
						innerCallTrue();
						return null;
					}
				}
			}

			if (!if_bool && !elseif_bool) {
				// ????????
				if_bool = commandString.startsWith(IF_TAG);
				elseif_bool = commandString.startsWith(ELSE_TAG);

			}

			// ????a
			if (if_bool) {
				esleflag = setupIF(commandString, nowPosFlagName,
						setEnvironmentList, conditionEnvironmentList);
				addCommand = false;
				ifing = true;
				// ????b
			} else if (elseif_bool) {
				String[] value = split(commandString, " ");
				if (!backIfBool && !esleflag) {
					// ??if??
					if (value.length > 1 && IF_TAG.equals(value[1])) {
						esleflag = setupIF(commandString.replaceAll(ELSE_TAG,
								"").trim(), nowPosFlagName, setEnvironmentList,
								conditionEnvironmentList);
						addCommand = false;
						// ???else
					} else if (value.length == 1 && ELSE_TAG.equals(value[0])) {
						esleflag = setupIF("if 1==1", nowPosFlagName,
								setEnvironmentList, conditionEnvironmentList);
						addCommand = false;
					}
				} else {
					addCommand = false;
					conditionEnvironmentList.put(nowPosFlagName, new Boolean(
							false));

				}
			}
			// ????
			if (commandString.startsWith(IF_END_TAG)) {
				conditionEnvironmentList.clear();
				backIfBool = false;
				addCommand = false;
				ifing = false;
				if_bool = false;
				elseif_bool = false;
				return null;
			}
			if (backIfBool) {
				// ??????
				if (commandString.startsWith(INCLUDE_TAG)) {
					temps = commandSplit(commandString);
					String fileName = (String) temps.get(1);
					if (fileName != null) {
						innerCommand = new Command(fileName);
						isInnerCommand = true;
						return null;
					}
				}
			} else if (commandString.startsWith(INCLUDE_TAG) && !ifing
					&& !backIfBool && !esleflag) {
				temps = commandSplit(commandString);
				String fileName = (String) temps.get(1);
				if (fileName != null) {
					innerCommand = new Command(fileName);
					isInnerCommand = true;
					return null;
				}
			}
			// ???????
			if (commandString.startsWith(OUT_TAG)) {
				isRead = false;
				addCommand = false;
				executeCommand = (SELECTS_TAG + " " + reader.toString())
						.intern();
			}
			// ?????
			if (isRead) {
				reader.append(commandString);
				reader.append(FLAG);
				addCommand = false;
			}
			// ?????
			if (commandString.startsWith(IN_TAG)) {
				reader.delete(0, reader.length());
				isRead = true;
				return executeCommand;
			}

			// ??????
			if (addCommand && ifing) {
				if (backIfBool && esleflag) {
					executeCommand = commandString;
				}

			} else if (addCommand) {
				executeCommand = commandString;
			}

			// ?????????
			if (executeCommand != null) {
				printTags = Command.getNameTags(executeCommand, PRINT_TAG
						+ BRACKET_LEFT_TAG, BRACKET_RIGHT_TAG);
				if (printTags != null) {
					for (Iterator it = printTags.iterator(); it.hasNext();) {
						String key = (String) it.next();
						Object value = setEnvironmentList.get(key);
						if (value != null) {
							executeCommand = Command
									.replaceMatch(
											executeCommand,
											(PRINT_TAG + BRACKET_LEFT_TAG + key + BRACKET_RIGHT_TAG)
													.intern(), value.toString());
						} else {
							executeCommand = Command
									.replaceMatch(
											executeCommand,
											(PRINT_TAG + BRACKET_LEFT_TAG + key + BRACKET_RIGHT_TAG)
													.intern(), key);
						}

					}

				}

				if (isCache) {
					// ??????
					cacheScript.put(cacheCommandName, executeCommand);
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (!isInnerCommand) {
				offsetPos++;
			}
		}

		return executeCommand;
	}

	/**
	 * ????????
	 * 
	 * @param fileName
	 * @return
	 */
	private static List includeFile(String fileName) {
		InputStream in = null;
		BufferedReader reader = null;
		List result = new ArrayList(1000);
		try {
			in = Resources.openResource(fileName);
			reader = new BufferedReader(new InputStreamReader(in,
					LSystem.encoding));
			String record = null;
			while ((record = reader.readLine()) != null) {
				record = record.trim();
				if (record.length() > 0) {
					if (!(record.startsWith(FLAG_L_TAG)
							|| record.startsWith(FLAG_C_TAG) || record
							.startsWith(FLAG_I_TAG))) {
						result.add(record);
					}
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * ???????????list
	 * 
	 * @param src
	 * @return
	 */
	public static List commandSplit(final String src) {
		String[] cmds;
		String result = src.trim();
		result = result.replaceAll("\r", "");
		result = FLAG + result;
		result = result.replaceAll("\t", FLAG);
		if (StringUtils.charCount(result, '=') == 1) {
			result = result.replaceAll(" ", FLAG);
			result = result.replaceAll("=", (FLAG + "=" + FLAG).intern());
		} else {
			result = result.replaceAll(" ", FLAG);
			result = result.replaceAll("<=", (FLAG + "<=" + FLAG).intern());
			result = result.replaceAll(">=", (FLAG + ">=" + FLAG).intern());
			result = result.replaceAll("==", (FLAG + "==" + FLAG).intern());
			result = result.replaceAll("!=", (FLAG + "!=" + FLAG).intern());
			if (result.indexOf("<=") == -1) {
				result = result.replaceAll("<", (FLAG + "<" + FLAG).intern());
			}
			if (result.indexOf(">=") == -1) {
				result = result.replaceAll(">", (FLAG + ">" + FLAG).intern());
			}
		}
		result = result.replaceAll((FLAG + "{2,}").intern(), FLAG);
		result = result.substring(1);
		cmds = result.split(FLAG);
		return Arrays.asList(cmds);
	}

}
