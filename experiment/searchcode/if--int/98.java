/*
 *      Copyright 2008-2010 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package com.google.code.sagetvaddons.sjq.server;
import gkusnick.sagetv.api.AiringAPI;
import gkusnick.sagetv.api.ChannelAPI;
import gkusnick.sagetv.api.MediaFileAPI;
import gkusnick.sagetv.api.ShowAPI;
import gkusnick.sagetv.api.SystemMessageAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.util.StringUtils;

import com.google.code.sagetvaddons.sjq.server.tests.Field;
import com.google.code.sagetvaddons.sjq.server.tests.FieldBoolean;
import com.google.code.sagetvaddons.sjq.server.tests.FieldNumber;
import com.google.code.sagetvaddons.sjq.server.tests.FieldString;
import com.google.code.sagetvaddons.sjq.server.utils.RangeInterpreter;
import com.google.code.sagetvaddons.sjq.server.utils.SystemMessageUtils;


final class RulesParser {	
	static private final Logger LOG = Logger.getLogger(RulesParser.class);
	static final String METHOD_REGEX = "((?:Is|Get|Has)[A-Za-z]+)\\{(.*)\\}";
	static final String OBJ_REFLECT_REGEX = "\\$\\." + METHOD_REGEX;
	static private final String FIELD_REGEX = OBJ_REFLECT_REGEX + "|[A-Z][A-Za-z]*|(?:[A-Za-z\\d]+_)+[A-Za-z\\d]+\\{([\\d\\w, ])*\\}";
	static private final class StringComparator extends FieldString {		
		public StringComparator(String src, String op, String input) {
			super(src, op, input);
		}

		@Override
		public boolean run() {
			return run(getObject().toString(), getInput());
		}
	}
	final class Evaluator {
		private boolean result;
		private String conditional;
		private String field;
		private String op;
		private String input;
		private boolean expectConditional;
		
		public Evaluator(boolean isSubTest, boolean skipSubtest) {
			expectConditional = false;
			resetForField();
			result = false;
			conditional = new String();
			try {
				if(!isSubTest || !skipSubtest) {
					if(tokenizer.nextToken() == '(') {
						result = new Evaluator(true, skipSubtest).getResult();
						expectConditional = true;
					}
					else
						tokenizer.pushBack();

					if(!expectConditional && nextTokenIsField())
						result = evalTest(true);
					else if(!expectConditional)
						throw new RuntimeException("Expected field name after but saw '" + tokenizer.nextToken() + "' on line " + tokenizer.lineno());
					while(nextTokenIsConditional()) {
						conditional = readConditionalOperator();
						if(conditional.equals("&&"))
							result = evalTest(result) && result;
						else if(conditional.equals("||"))
							result = evalTest(!result) || result;
					}
				} else {
					int nextTok;
					while((nextTok = tokenizer.nextToken()) != ')' && nextTok != StreamTokenizer.TT_EOL && nextTok != StreamTokenizer.TT_EOF) {
						if(nextTok == '(') {
							new Evaluator(true, skipSubtest);
						}
					}
					tokenizer.pushBack();
				}
				
				int nextTok;
				if(isSubTest && (nextTok = tokenizer.nextToken()) != ')')
					throw new RuntimeException("Missing matching ')' on line " + tokenizer.lineno() + "; found '" + nextTok + "' [" + tokenizer.sval + "]");
			} catch(IOException e) {
				throw new RuntimeException(e.getLocalizedMessage());
			}
		}

		public Evaluator() {
			this(false, false);
		}
		
		public boolean getResult() {
			return result;
		}
		
		private boolean evalTest(boolean doCalculation) {
			try {
				if(tokenizer.nextToken() == '(')
					return new Evaluator(true, !doCalculation).getResult();
				tokenizer.pushBack();
			} catch(IOException e) {
				throw new RuntimeException(e.getLocalizedMessage());
			}
			field = readField();
			op = readBinaryOperator();
			input = readInput();
			LOG.trace("FIELD: " + field + " :: OP: " + op + " :: INPUT: " + input);
			
			if(doCalculation) {
				boolean ret = runTest(field);
				if(logDetails)
					LOG.warn("\tLine " + tokenizer.lineno() + ": Evaluating '" + field + " " + op + " \"" + input + "\"' == " + Boolean.toString(ret));
				return ret;
			} else {
				LOG.trace("Skipping calculation of '" + field + "' on line " + tokenizer.lineno() + "; short circuit opportunity detected");
				return false;
			}
		}
		
		private boolean runTest(String field) {
			if(field.startsWith("$."))
				return reflect(field);
			else if(field.contains("_"))
				return externalReflect(field);
			return internalTest(field);
		}
		
		private boolean externalReflect(String field) {
			String clsCall = field.substring(0, field.lastIndexOf('_'));
			clsCall = StringUtils.replace(clsCall, "_", ".");
			
			Class<?> cls = null;
			try {
				cls = Class.forName(clsCall);
				StringBuilder msg = new StringBuilder("The following public methods are available in class '" + clsCall + "':\n");
				for(Method m : cls.getMethods())
					msg.append(m.toString() + "\n");
				LOG.debug(msg);
			} catch(ClassNotFoundException e) {
				LOG.error("Class '" + clsCall + "' does not exist; returning FALSE for test!");
				return false;
			}
			
			String methodCall = field.substring(field.lastIndexOf('_') + 1, field.indexOf('{'));
			String[] strArgs;
			String argsVal = field.substring(field.indexOf('{') + 1, field.lastIndexOf('}'));
			if(argsVal == null || argsVal.trim().length() == 0)
				strArgs = new String[0];
			else
				strArgs = argsVal.trim().split(",");
			Class<?>[] argTypes = new Class<?>[strArgs.length];
			for(int i = 0; i < argTypes.length; ++i)
				argTypes[i] = Object.class;
			Method method = null;
			
			Object[] finalArgs = new Object[1 + strArgs.length];
			finalArgs[0] = obj;
			for(int i = 0; i < strArgs.length; ++i)
				finalArgs[i + 1] = strArgs[i];
			
			Class<?>[] sigTypes;
			try {
				sigTypes = new Class<?>[argTypes.length + 1];
				sigTypes[0] = obj.getClass();
				if(sigTypes.length > 1)
					Arrays.fill(sigTypes, 1, sigTypes.length, Object.class);
				LOG.debug("Searching for typed method with " + sigTypes.length + " arguments: " + Arrays.toString(sigTypes));
				method = cls.getMethod(methodCall, sigTypes);
			} catch(NoSuchMethodException e) {
				sigTypes = new Class<?>[argTypes.length];
				if(sigTypes.length > 0)
					Arrays.fill(sigTypes, 0, sigTypes.length, Object.class);
				LOG.debug("Now searching for typed method with " + sigTypes.length + " arguments: " + Arrays.toString(sigTypes));
				try {
					method = cls.getMethod(methodCall, sigTypes);
				} catch (NoSuchMethodException e1) {
					LOG.warn("Unable to find a suitable method named '" + methodCall + "' in class '" + clsCall + "'; returning FALSE for test!");
					return false;
				}
				finalArgs = ArrayUtils.remove(finalArgs, 0);
			}
			
			try {
				LOG.info("Invoking method '" + methodCall + "' from class '" + clsCall + "' on line " + tokenizer.lineno());
				Object target = null;
				if(!Modifier.isStatic(method.getModifiers()))
					target = cls.newInstance();
				return testInput(method.invoke(target, finalArgs), method.getReturnType());
			} catch (IllegalArgumentException e) {
				LOG.error("Illegal argument", e);
				return false;
			} catch (IllegalAccessException e) {
				LOG.error("Illegal access", e);
				return false;
			} catch (InvocationTargetException e) {
				LOG.error("Target threw exception", e);
				return false;
			} catch (InstantiationException e) {
				LOG.error("Instantiation exception", e);
				return false;
			}
		}
		
		private boolean reflect(String field) {
			Pattern p = Pattern.compile(FIELD_REGEX);
			Matcher m = p.matcher(field);
			String methName, arg;
			Class<?>[][] argList = new Class<?>[][] {{String.class}, {long.class}, {int.class}, null};
						
			if(m.matches()) {
				methName = m.group(1);
				arg = m.group(2);
				LOG.debug("Want to run: '" + methName + "' with arg: '" + arg + "'");
			} else {
				LOG.error("Field does NOT match regex!");
				return false;
			}
			
			Object[] objs;
			Method method = null;
			if(obj instanceof MediaFileAPI.MediaFile) {
				AiringAPI.Airing airing = ((MediaFileAPI.MediaFile)obj).GetMediaFileAiring();
				ShowAPI.Show show = null;
				ChannelAPI.Channel channel = null;
				if(airing != null) {
					show = airing.GetShow();
					channel = airing.GetChannel();
				}
				objs = new Object[] {obj, airing, show, channel};
			} else if(obj instanceof SystemMessageAPI.SystemMessage) {
				objs = new Object[] {obj};
			} else {
				LOG.error("Unsupported object type; returning false! [" + obj.getClass().getCanonicalName() + "]");
				return false;
			}
			
			for(Object o : objs) {
				if(o == null)
					continue;
				for(Class<?>[] type : argList) {
					try {
						method = o.getClass().getDeclaredMethod(methName, type);
						Class<?> retType = method.getReturnType();
						LOG.debug("Invoking '" + methName + "' on type '" + o.getClass().getCanonicalName() + "' with return type: '" + retType.getName() + "'");
						return testInput(method.invoke(o, type != null ? MediaVariables.convert(arg, type[0]) : null), retType);
					} catch(NoSuchMethodException e) {
						method = null;
					} catch(InvocationTargetException e) {
						LOG.error("Invocation target exception", e);
						method = null;
					} catch(IllegalAccessException e) {
						LOG.error("Illegal access exception", e);
						method = null;
					}
				}
			}
			LOG.warn("Unable to invoke method '" + methName + "'; return false for test!");
			return false;
		}
		
		private boolean testInput(Object retVal, Class<?> retType) {
			if(boolean.class.equals(retType))
				return new FieldBoolean(obj, op, input).run((Boolean)retVal);
			if(int.class.equals(retType) || long.class.equals(retType) || double.class.equals(retType) || float.class.equals(retType))
				return new FieldNumber(obj, op, input).run(Double.parseDouble(retVal.toString()));
			return new StringComparator(retVal.toString(), op, input).run();
		}
		
		private boolean internalTest(String field) {
			Field f = null;
			try {
				Class<?> cls = Class.forName("com.google.code.sagetvaddons.sjq.server.tests.Field" + field);
				Constructor<?> ctor = cls.getDeclaredConstructor(new Class[] {Object.class, String.class, String.class});
				f = (Field)ctor.newInstance(new Object[] {obj, op, input});
			} catch(ClassNotFoundException e) {
				throw new RuntimeException("Invalid field name, '" + field + "', on line " + tokenizer.lineno());
			} catch(NoSuchMethodException e) {
				throw new RuntimeException("FATAL SJQ ERROR: Unexpected NoSuchMethodException [" + field + "]");
			} catch(InstantiationException e) {
				throw new RuntimeException("Invalid field name, '" + field + "', on line " + tokenizer.lineno());
			} catch(IllegalAccessException e) {
				throw new RuntimeException("FATAL SJQ ERROR: Unexpected IllegalAccessException");
			} catch(InvocationTargetException e) {
				throw new RuntimeException("FATAL SJQ ERROR: Unexpected InvocationTargetException");
			}
			return f.run();
		}
	}
	
	private StreamTokenizer tokenizer;
	private Object obj;
	private boolean logDetails;
	
	public RulesParser(Reader r, Object obj, boolean logDetails) {
		tokenizer = new StreamTokenizer(r);
		this.obj = obj;
		this.logDetails = logDetails;
		loadQ();
	}
	
	public RulesParser(File f, MediaFileAPI.MediaFile mf, boolean logDetails) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(f)), mf, logDetails);
	}
	
	public RulesParser(String file, MediaFileAPI.MediaFile mf, boolean logDetails) throws FileNotFoundException {
		this(new File(file), mf, logDetails);
	}
	
	public RulesParser(Reader r, MediaFileAPI.MediaFile mf) {
		this(r, mf, false);
	}
	
	public RulesParser(File f, MediaFileAPI.MediaFile mf) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(f)), mf, false);
	}
	
	public RulesParser(String file, MediaFileAPI.MediaFile mf) throws FileNotFoundException {
		this(new File(file), mf, false);
	}

	private void loadQ() {
		try {
			while(shellChecker()) {
				Evaluator e = new Evaluator();
				postShellChecker(e.getResult());
			}
		}catch(IOException e) {
			LOG.error("IOException", e);
		}		
	}
		
	private void resetBaseSyntax() {
		StreamTokenizer t = tokenizer;
		t.resetSyntax();
		t.slashSlashComments(true);
		t.slashStarComments(true);
		t.commentChar('#');
		t.whitespaceChars('\u0000', '\u0020');
		t.lowerCaseMode(false);
		t.parseNumbers();
		t.eolIsSignificant(true);
		return;
	}
	
	private boolean shellChecker() throws IOException {
		StreamTokenizer t = tokenizer;
		resetBaseSyntax();
		t.wordChars('i', 'i');
		t.wordChars('f', 'f');
		t.wordChars('[', ']');
		
		while(t.nextToken() == StreamTokenizer.TT_EOL);
		t.pushBack();

		if(t.nextToken() == StreamTokenizer.TT_EOF)
			return false;
		t.pushBack();
		
		if(t.nextToken() != StreamTokenizer.TT_WORD || !t.sval.equals("if"))
			throw new RuntimeException("Expected \"if\" to start line " + t.lineno());
		if(t.nextToken() != StreamTokenizer.TT_WORD || !t.sval.equals("["))
			throw new RuntimeException("Expected \"[\" after \"if\" on line " + t.lineno() + "'" + t.sval + "'");
		return true;
	}
	
	private void postShellChecker(boolean runTasks) throws IOException {
		StreamTokenizer t = tokenizer;
		resetForPostShell();

		while(t.nextToken() == StreamTokenizer.TT_EOL);
		t.pushBack();
		
		int tok = t.nextToken();
		if((tok != StreamTokenizer.TT_WORD || !t.sval.equals("]")) && tok != ']')
			throw new RuntimeException("Expected ']' missing on line " + t.lineno());
		
		while(t.nextToken() == StreamTokenizer.TT_EOL);
		t.pushBack();

		tok = t.nextToken();
		if((tok != StreamTokenizer.TT_WORD || !t.sval.equals("{")) && tok != '{')
			throw new RuntimeException("Expected '{' missing on line " + t.lineno());
		
		while(t.nextToken() == StreamTokenizer.TT_EOL);
		t.pushBack();

		Task[] tasks = readJob();
		if(runTasks)
			for(Task task : tasks)
				if(!isTaskBlackedOut(task))
					TaskQueue.getInstance().push(task);
		
		while(t.nextToken() == StreamTokenizer.TT_EOL);
		t.pushBack();

		tok = t.nextToken();
		if((tok != StreamTokenizer.TT_WORD || !t.sval.equals("}")) && tok != '}')
			throw new RuntimeException("Expected '}' missing on line " + t.lineno());
		return;
	}
	
	private Task[] readJob() throws IOException {
		resetForInput();
		StreamTokenizer t = tokenizer;
		
		Map<String, String> opts = new HashMap<String, String>();
		List<Task> tasks = new ArrayList<Task>();
		double priority = Task.DEFAULT_PRIORITY;
		int tok;
		while((tok = t.nextToken()) != '}') {
			if(tok == ':') {
				if(t.nextToken() != StreamTokenizer.TT_WORD)
					throw new RuntimeException("Expected option token on line " + t.lineno() + " after ':'");
				if(t.sval.equals("PRIORITY")) {
					if(t.nextToken() != StreamTokenizer.TT_NUMBER)
						throw new RuntimeException("Expecting integer following PRIORITY option on line " + t.lineno());
					priority = (int)t.nval;
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following integer on line " + t.lineno());
				} else if(t.sval.equals("ADDUSERCATOPTS") || t.sval.equals("DELUSERCATOPTS")) {
					String optName = t.sval;
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted, comma separated list of user categories following " + t.sval + " option on line " + t.lineno());
					if(optName.equals("ADDUSERCATOPTS"))
						optName = Task.OPT_SET_USER_CAT;
					else
						optName = Task.OPT_DEL_USER_CAT;
					opts.put(optName, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("TRANSCODEOPTS")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted format followed by double quoted boolean following TRANSCODEOPTS option on line " + t.lineno());
					opts.put(Task.OPT_TRANSCODE_FMT, t.sval);
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted format followed by double quoted boolean following TRANSCODEOPTS option on line " + t.lineno());
					opts.put(Task.OPT_TRANSCODE_KEEP, t.sval);
					String lastParam;
					if(t.nextToken() == '"') {
						opts.put(Task.OPT_TRANSCODE_DEST, t.sval);
						lastParam  = opts.get(Task.OPT_TRANSCODE_DEST);
					}
					else {
						t.pushBack();
						lastParam = opts.get(Task.OPT_TRANSCODE_KEEP);
					}
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + lastParam + "' on line " + t.lineno());
				} else if(t.sval.equals("OFFDAY")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted range setting following OFFDAY option on line " + t.lineno());
					opts.put(Task.OPT_OFF_DAY, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("OFFHOUR")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted range setting following OFFHOUR option on line " + t.lineno());
					opts.put(Task.OPT_OFF_HOUR, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("MOVERECOPTS")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string of options following MOVERECOPTS on line " + t.lineno());
					opts.put(Task.OPT_MOVE_REC, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("RESCHEDOPTS")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted option list following RESCHEDOPTS option on line " + t.lineno());
					opts.put(Task.OPT_RESCHED, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("SCANMEDIA")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string of TRUE or FALSE following SCANMEDIA option on line " + t.lineno());
					opts.put(Task.OPT_SCAN_MEDIA, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("RUNQLOADER")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string of TRUE or FALSE following RUNQLOADER option on line " + t.lineno());
					opts.put(Task.OPT_RUN_Q_LOADER, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());					
				} else if(t.sval.equals("FAILALERT")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string of TRUE or FALSE following FAILALERT option on line " + t.lineno());
					opts.put(Task.OPT_ALERT_FAIL, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expceting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("DONEALERT")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string of TRUE or FALSE following DONEALERT option on line " + t.lineno());
					opts.put(Task.OPT_ALERT_DONE, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expceting end of line following '" + t.sval + "' on line " + t.lineno());					
				} else if(t.sval.equals("DONEALERTTXT")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string following DONEALERTTXT option on line " + t.lineno());
					opts.put(Task.OPT_ALERT_DONE_TXT, MediaVariables.expand(t.sval, obj, null, false));
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expceting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("FAILALERTTXT")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string of following FAILALERTTXT option on line " + t.lineno());
					opts.put(Task.OPT_ALERT_FAIL_TXT, MediaVariables.expand(t.sval, obj, null, false));
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expceting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("ALERTSUBJ")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string following ALERTSUBJ option on line " + t.lineno());
					opts.put(Task.OPT_ALERT_SUBJ, MediaVariables.expand(t.sval, obj, null, false));
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("ALERTTXT")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string following ALERTTXT option on line " + t.lineno());
					opts.put(Task.OPT_ALERT_TXT, MediaVariables.expand(t.sval, obj, null, false));
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("ALERTLVL")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting double quoted string following ALERTLVL option on line " + t.lineno());
					opts.put(Task.OPT_ALERT_LVL, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());
				} else if(t.sval.equals("RELINKOPTS")) {
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting two double quoted strings following RELINKOPTS on line " + t.lineno());
					opts.put(Task.OPT_RELINK_AIRING, t.sval);
					if(t.nextToken() != '"')
						throw new RuntimeException("Expecting two double quoted strings following RELINKOPTS on line " + t.lineno());
					opts.put(Task.OPT_RELINK_MEDIA, t.sval);
					if(t.nextToken() != StreamTokenizer.TT_EOL)
						throw new RuntimeException("Expecting end of line following '" + t.sval + "' on line " + t.lineno());	
				} else {
					LOG.warn("Skipping unrecognized option '" + t.sval + "'");
					while((tok = t.nextToken()) != StreamTokenizer.TT_WORD && tok != ':');
					t.pushBack();
				}
			}
			else if(tok == StreamTokenizer.TT_WORD || (tok >= 'A' && tok <= 'Z') || (tok >= 'a' && tok <= 'z') || tok == '_') {
				String tName;
				if(tok != StreamTokenizer.TT_WORD) {
					tName = new String("" + (char)tok);
					if(t.nextToken() == StreamTokenizer.TT_WORD)
						tName = tName.concat(t.sval);
					else
						t.pushBack();
				}
				else
					tName = t.sval;
				
				String key;
				String type;
				if(obj instanceof MediaFileAPI.MediaFile) {
					key = Integer.toString(((MediaFileAPI.MediaFile)obj).GetMediaFileID());
					type = "media";
				}
				else if(obj instanceof SystemMessageAPI.SystemMessage) {
					key = SystemMessageUtils.genKey((SystemMessageAPI.SystemMessage)obj);
					type = "sysmsg";
				} else {
					key = "0";
					type = "unknown";
				}
				tasks.add(new Task(tName, key, type, (int)priority, opts));
			}
			else if(tok == StreamTokenizer.TT_EOL)
				continue;
			else
				throw new RuntimeException("Unexpected '" + (char)tok + "' on line " + t.lineno());
		}
		t.pushBack();
		
		if(tasks.size() == 0)
			throw new RuntimeException("Job must define at least one task on line " + t.lineno());
		return tasks.toArray(new Task[tasks.size()]);
	}
	
	private boolean nextTokenIsField() {
		resetForField();
		StreamTokenizer t = tokenizer;
		try {
			int next = t.nextToken();
			return (next == StreamTokenizer.TT_WORD || next == '"') && t.sval.matches(FIELD_REGEX);
		} catch(IOException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		} finally {
			t.pushBack();
		}
	}
		
	private boolean nextTokenIsConditional() {
		resetForConditionalOperator();
		StreamTokenizer t= tokenizer;
		int tok = -99;
		try {
			tok = t.nextToken();
			return tok == StreamTokenizer.TT_WORD || tok == '|' || tok == '&';
		} catch(IOException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		} finally {
			t.pushBack();
		}
	}
	
	private void resetForPostShell() {
		StreamTokenizer t = tokenizer;
		resetBaseSyntax();
		t.wordChars(']', ']');
		t.wordChars('{', '{');
		t.wordChars('}', '}');
		return;
	}
	
	private void resetForField() {
		StreamTokenizer t = tokenizer;
		resetBaseSyntax();
		// Everything but the double quote
		t.wordChars(33, 39);
		t.wordChars(42, 126);
		t.quoteChar('"');
		return;
	}
	
	private void resetForBinaryOperator() {
		StreamTokenizer t = tokenizer;
		resetBaseSyntax();
		t.wordChars('<', '>'); // Includes '='
		t.wordChars('!', '!');
		t.wordChars('$', '%');
		t.wordChars('*', '*');
		t.wordChars('^', '^');
		return;
	}
	
	private void resetForConditionalOperator() {
		StreamTokenizer t = tokenizer;
		resetBaseSyntax();
		t.wordChars('&', '&');
		t.wordChars('|', '|');
		return;
	}
	
	private void resetForInput() {
		StreamTokenizer t = tokenizer;
		resetBaseSyntax();
		t.wordChars('A', 'Z');
		t.wordChars('a', 'z');
		t.wordChars('_', '_');
		t.wordChars('\u00A0', '\u00FF');
		t.quoteChar('"');
		return;
	}
		
	private String readField() {
		resetForField();
		int tok;
		String err;
		String partialField = new String();
		try { 
			while((tok = tokenizer.nextToken()) != StreamTokenizer.TT_WORD && tok != StreamTokenizer.TT_EOF && tok != StreamTokenizer.TT_EOL && (tok > 0 || tok == StreamTokenizer.TT_NUMBER)) {
				if(tok > 0)
					partialField = partialField.concat((char)tok + "");
				else
					partialField = partialField.concat(".");
			}
		} catch(IOException e) { 
			throw new RuntimeException(e.getLocalizedMessage()); 
		}
		tokenizer.pushBack();
		try { tok = tokenizer.nextToken(); } catch(IOException e) { throw new RuntimeException(e.getLocalizedMessage()); }
		if(tokenizer.ttype != StreamTokenizer.TT_WORD && tok != '"') {
			if(tokenizer.ttype == StreamTokenizer.TT_EOF)
				err = new String("Unexpected end of file reached");
			else
				err = new String("Unexpected value \"" + (char)tok + "\" on line " + tokenizer.lineno());
			err = err.concat("; expecting a valid field identifier");
			throw new RuntimeException(err);
		}
		tokenizer.sval = partialField + tokenizer.sval;
		if(!tokenizer.sval.matches(FIELD_REGEX)) {
			err = new String("Invalid field name encountered on line " + tokenizer.lineno() + "; fields must start with a captial letter and only contain letters or be a supported object method call [" + tokenizer.sval + "]");
			throw new RuntimeException(err);
		}
		return tokenizer.sval;
	}
	
	private String readBinaryOperator() {
		resetForBinaryOperator();
		int tok;
		String err;
		try { tok = tokenizer.nextToken(); } catch(IOException e) { throw new RuntimeException(e.getLocalizedMessage()); }
		if(tokenizer.ttype != StreamTokenizer.TT_WORD) {
			if(tokenizer.ttype == StreamTokenizer.TT_EOF)
				err = new String("Unexpected end of file reached");
			else
				err = new String("Unexpected value \"" + (char)tok + "\" on line " + tokenizer.lineno());
			err = err.concat("; expecting a binary operator");
			throw new RuntimeException(err);
		}
		return tokenizer.sval;
	}
	
	private String readConditionalOperator() {
		resetForConditionalOperator();
		int tok;
		try { 
			tok = tokenizer.nextToken();
			if (tok == '&' || tok == '|') {
				tokenizer.nextToken();
				tokenizer.sval = (char)tok + tokenizer.sval;
				tokenizer.ttype = StreamTokenizer.TT_WORD;
				tok = tokenizer.ttype;
			}
		} catch(IOException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		if(tok != StreamTokenizer.TT_WORD || (!tokenizer.sval.equals("&&") && !tokenizer.sval.equals("||")))
			throw new RuntimeException("Expected \"&&\" or \"||\" on line " + tokenizer.lineno());
		return tokenizer.sval;
	}
	
	private String readInput() {
		resetForInput();
		String input;
		try {
			int tok = tokenizer.nextToken();
			if(tok == StreamTokenizer.TT_NUMBER)
				input = Double.toString(tokenizer.nval);
			else if((tok == StreamTokenizer.TT_WORD || tok == '"') && tokenizer.sval != null)
				input = tokenizer.sval;
			else
				throw new RuntimeException("Unexpected '" + (char)tok + "' on line " + tokenizer.lineno() + " (are your double quotes balanced?)");
		} catch(IOException e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return input;
	}
	
	private boolean isTaskBlackedOut(Task t) {
		String offDay = t.getOption(Task.OPT_OFF_DAY);
		String offHour = t.getOption(Task.OPT_OFF_HOUR);

		if(offDay != null && offDay.length() > 0 && RangeInterpreter.inRange(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), 1, 7, offDay)) {
			LOG.info("Task '" + t.getObjType() + "/" + t.getObjId() + "/" + t.getTaskId() + "' is currently blacked out by OFFDAY: '" + offDay + "'");
			return true;
		}

		if(offHour != null && offHour.length() > 0)
			if(RangeInterpreter.inRange(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), 0, 23, offHour)) {
				LOG.info("Task '" + t.getObjType() + "/" + t.getObjId() + "/" + t.getTaskId() + "' is currently blacked out by OFFHOUR: '" + offHour + "'");
				return true;
			}
		return false;
	}
}

