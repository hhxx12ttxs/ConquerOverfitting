/*
 *      Copyright 2008,2009 Battams, Derek
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
import gkusnick.sagetv.api.MediaFileAPI;
import gkusnick.sagetv.api.ShowAPI;
import gkusnick.sagetv.api.SystemMessageAPI;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public final class MediaVariables {
	static private final Logger LOG = Logger.getLogger(MediaVariables.class);
	
	static public String expand(String s, Object obj, Map<String, File> dirMaps, boolean logDetails) {
		String debugMsg = "Expanding '" + s + "'\n";
		if(s == null)
			LOG.error(debugMsg);
		else
			LOG.debug(debugMsg);
		
		String expanded = s;
		
		Map<String, String> values;
		if(obj instanceof MediaFileAPI.MediaFile) {
			MediaFileAPI.MediaFile mf = (MediaFileAPI.MediaFile)obj;
			if(mf.GetNumberOfSegments() > 0)
				values = getVars(mf.GetFileForSegment(0), logDetails);
			else
				values = new HashMap<String, String>();
		} else if(obj instanceof File)
			values = getVars((File)obj, logDetails);
		else
			values = new HashMap<String, String>();
		Iterator<String> vars = values.keySet().iterator();
		while(vars.hasNext()) {
			expanded = ""; 
			String var = vars.next();
			int index;
			while((index = s.indexOf(var)) != -1) {
				expanded = s.substring(0, index);
				String expandedVar = values.get(var);
				if(dirMaps != null)
					for(String srvDir : dirMaps.keySet())
						expandedVar = expandedVar.replace(srvDir, dirMaps.get(srvDir).getAbsolutePath());
				expanded = expanded.concat(expandedVar);
				s = s.substring(index + var.length());
			}
			if(s.length() > 0)
				expanded = expanded.concat(s);
			s = expanded;
		}
		
		Pattern p = Pattern.compile(".*(" + RulesParser.OBJ_REFLECT_REGEX + ").*");
		Matcher m = p.matcher(expanded);
		while(m.matches()) {
			expanded = expanded.replace(m.group(1), reflect(m.group(1), obj));
			m = p.matcher(expanded);
		}
		LOG.debug("Final expansion: '" + expanded + "'");
		return expanded;		
	}
	
	static public String expand(String s, File f, boolean logDetails) {
		return expand(s, f, null, logDetails);
	}
	
	static private Map<String, String> getVars(File f, boolean logResults) {
		Map<String, String> vars = new HashMap<String, String>();
		
		// %f% => Full file name without leading directory
		vars.put("%f%", f.getName());
		
		// %d% => Full path name, no file
		File parentPath = f.getParentFile();
		if(parentPath != null)
			vars.put("%d%", parentPath.getAbsolutePath());
		else
			vars.put("%d%", "");
		
		// %e% => File name's extension WITHOUT the period
		// %p% => File name's prefix (i.e. file name without file extension and WITHOUT period)
		String fName = f.getName();
		if(fName.lastIndexOf('.') != -1) {
			vars.put("%e%", fName.substring(fName.lastIndexOf('.') + 1));
			vars.put("%p%", fName.substring(0, fName.lastIndexOf('.')));
		}
		else {
			vars.put("%e%", "");
			vars.put("%p%", fName);
		}
		
		// %c% => Complete, absoulte path of the file
		vars.put("%c%", f.getAbsolutePath());
	
		if(logResults)
			for(String key : vars.keySet())
				LOG.warn("\tMediaVar '" + key + "' == '" + vars.get(key) + "'");
		return vars;
	}
	
	static private String reflect(String field, Object obj) {
		Pattern p = Pattern.compile(".*" + RulesParser.OBJ_REFLECT_REGEX + ".*");
		Matcher m = p.matcher(field);
		String methName, arg;
		Class<?>[][] argList = new Class<?>[][] {{String.class}, {long.class}, {int.class}, null};
		
		if(m.matches()) {
			methName = m.group(1);
			arg = m.group(2);
			LOG.debug("Want to run: '" + methName + "' with arg: '" + arg + "'");
		} else {
			LOG.error("Field does NOT match regex! [" + field + "]");
			return "";
		}
		
		Object[] objs;
		Method method = null;
		if(obj instanceof MediaFileAPI.MediaFile || obj instanceof File) {
			if(obj instanceof File)
				obj = Butler.SageApi.mediaFileAPI.GetMediaFileForFilePath((File)obj);
			if(obj != null) {
				AiringAPI.Airing airing = ((MediaFileAPI.MediaFile)obj).GetMediaFileAiring();
				ShowAPI.Show show = null;
				if(airing != null)
					show = airing.GetShow();
				objs = new Object[] {obj, airing, show};
			} else
				objs = new Object[0];
		} else if(obj instanceof SystemMessageAPI.SystemMessage) {
			objs = new Object[] {obj};
		} else
			objs = new Object[0];
		
		for(Object o : objs) {
			if(o == null)
				continue;
			for(Class<?>[] type : argList) {
				try {
					method = o.getClass().getDeclaredMethod(methName, type);
					Class<?> retType = method.getReturnType();
					LOG.debug("Invoking '" + methName + "' on type '" + o.getClass().getCanonicalName() + "' with return type: '" + retType.getName() + "'");
					return method.invoke(o, type != null ? convert(arg, type[0]) : null).toString();
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
		LOG.warn("Unable to invoke method '" + methName + "'; returning empty string!");
		return "";
	}
	
	static Object[] convert(String arg, Class<?> type) {
		if(type == null)
			return null;
		Object[] args = new Object[1];
		if(type.equals(String.class))
			args[0] = arg;
		else if(long.class.equals(type))
			args[0] = Long.parseLong(arg);
		else if(int.class.equals(type))
			args[0] = Integer.parseInt(arg);
		else
			args[0] = Double.parseDouble(arg);
		return args;
	}

	private MediaVariables() {};
}

