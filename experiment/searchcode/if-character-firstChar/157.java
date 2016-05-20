/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.tools;

import com.liferay.portal.kernel.io.unsync.UnsyncBufferedReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.ClassUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.TextFormatter;
import com.liferay.portal.kernel.util.Tuple;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.util.FileImpl;
import com.liferay.portal.xml.SAXReaderImpl;
import com.liferay.util.ContentUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;

/**
 * @author Brian Wing Shun Chan
 * @author Igor Spasic
 * @author Wesley Gong
 * @author Hugo Huijser
 */
public class SourceFormatter {

	public static void main(String[] args) {
		try {
			_excludes = StringUtil.split(
				GetterUtil.getString(
					System.getProperty("source.formatter.excludes")));

			_portalSource = _isPortalSource();

			_sourceFormatterHelper = new SourceFormatterHelper(false);

			_sourceFormatterHelper.init();

			Thread thread1 = new Thread () {

				@Override
				public void run() {
					try {
						_formatJSP();
						_formatAntXML();
						_formatDDLStructuresXML();
						_formatFriendlyURLRoutesXML();
						_formatFTL();
						_formatJS();
						_formatPortalProperties();
						_formatPortletXML();
						_formatServiceXML();
						_formatSH();
						_formatSQL();
						_formatStrutsConfigXML();
						_formatTilesDefsXML();
						_formatWebXML();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}

			};

			Thread thread2 = new Thread () {

				@Override
				public void run() {
					try {
						_formatJava();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}

			};

			thread1.start();
			thread2.start();

			thread1.join();
			thread2.join();

			_sourceFormatterHelper.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String stripJavaImports(
			String content, String packageDir, String className)
		throws IOException {

		Matcher matcher = _javaImportPattern.matcher(content);

		if (!matcher.find()) {
			return content;
		}

		String imports = matcher.group();

		Set<String> classes = ClassUtil.getClasses(
			new UnsyncStringReader(content), className);

		StringBundler sb = new StringBundler();

		UnsyncBufferedReader unsyncBufferedReader = new UnsyncBufferedReader(
			new UnsyncStringReader(imports));

		String line = null;

		while ((line = unsyncBufferedReader.readLine()) != null) {
			if (line.contains("import ")) {
				int importX = line.indexOf(" ");
				int importY = line.lastIndexOf(".");

				String importPackage = line.substring(importX + 1, importY);
				String importClass = line.substring(
					importY + 1, line.length() - 1);

				if (!packageDir.equals(importPackage)) {
					if (!importClass.equals("*")) {
						if (classes.contains(importClass)) {
							sb.append(line);
							sb.append("\n");
						}
					}
					else {
						sb.append(line);
						sb.append("\n");
					}
				}
			}
		}

		imports = _formatImports(sb.toString(), 7);

		content =
			content.substring(0, matcher.start()) + imports +
				content.substring(matcher.end());

		// Ensure a blank line exists between the package and the first import

		content = content.replaceFirst(
			"(?m)^[ \t]*(package .*;)\\s*^[ \t]*import", "$1\n\nimport");

		// Ensure a blank line exists between the last import (or package if
		// there are no imports) and the class comment

		content = content.replaceFirst(
			"(?m)^[ \t]*((?:package|import) .*;)\\s*^[ \t]*/\\*\\*",
			"$1\n\n/**");

		return content;
	}

	private static void _addJSPIncludeFileNames(
		String fileName, Set<String> includeFileNames) {

		String content = _jspContents.get(fileName);

		if (Validator.isNull(content)) {
			return;
		}

		for (int x = 0;;) {
			x = content.indexOf("<%@ include file=", x);

			if (x == -1) {
				break;
			}

			x = content.indexOf(StringPool.QUOTE, x);

			if (x == -1) {
				break;
			}

			int y = content.indexOf(StringPool.QUOTE, x + 1);

			if (y == -1) {
				break;
			}

			String includeFileName = content.substring(x + 1, y);

			Matcher matcher = _jspIncludeFilePattern.matcher(includeFileName);

			if (!matcher.find()) {
				throw new RuntimeException(
					"Invalid include " + includeFileName);
			}

			String docrootPath = fileName.substring(
				0, fileName.indexOf("docroot") + 7);

			includeFileName = docrootPath + includeFileName;

			if ((includeFileName.endsWith("jsp") ||
				 includeFileName.endsWith("jspf")) &&
				!includeFileNames.contains(includeFileName) &&
				!includeFileName.contains("html/portlet/init.jsp")) {

				includeFileNames.add(includeFileName);
			}

			x = y;
		}
	}

	private static void _addJSPReferenceFileNames(
		String fileName, Set<String> includeFileNames) {

		for (Map.Entry<String, String> entry : _jspContents.entrySet()) {
			String referenceFileName = entry.getKey();
			String content = entry.getValue();

			if (content.contains("<%@ include file=\"" + fileName) &&
				!includeFileNames.contains(referenceFileName)) {

				includeFileNames.add(referenceFileName);
			}
		}
	}

	private static void _addJSPUnusedImports(
		String fileName, List<String> importLines,
		List<String> unneededImports) {

		for (String importLine : importLines) {
			Set<String> includeFileNames = new HashSet<String>();

			includeFileNames.add(fileName);

			Set<String> checkedFileNames = new HashSet<String>();

			int x = importLine.indexOf(StringPool.QUOTE);
			int y = importLine.indexOf(StringPool.QUOTE, x + 1);

			if ((x == -1) || (y == -1)) {
				continue;
			}

			String className = importLine.substring(x + 1, y);

			className = className.substring(
				className.lastIndexOf(StringPool.PERIOD) + 1);

			if (!_isJSPImportRequired(
					fileName, className, includeFileNames, checkedFileNames)) {

				unneededImports.add(importLine);
			}
		}
	}

	private static List<String> _addParameterTypes(
		String line, List<String> parameterTypes) {

		int x = line.indexOf(StringPool.OPEN_PARENTHESIS);

		if (x != -1) {
			line = line.substring(x + 1);

			if (Validator.isNull(line) ||
				line.startsWith(StringPool.CLOSE_PARENTHESIS)) {

				return parameterTypes;
			}
		}

		for (x = 0;;) {
			x = line.indexOf(StringPool.SPACE);

			if (x == -1) {
				return parameterTypes;
			}

			String parameterType = line.substring(0, x);

			if (parameterType.equals("throws")) {
				return parameterTypes;
			}

			if (parameterType.endsWith("...")) {
				parameterType = StringUtil.replaceLast(
					parameterType, "...", StringPool.BLANK);
			}

			int pos = parameterType.lastIndexOf(StringPool.PERIOD);

			if (pos != -1) {
				parameterType = parameterType.substring(pos + 1);
			}

			parameterTypes.add(parameterType);

			int y = line.indexOf(StringPool.COMMA);
			int z = line.indexOf(StringPool.CLOSE_PARENTHESIS);

			if ((y == -1) || ((z != -1) && (z < y))) {
				return parameterTypes;
			}

			line = line.substring(y + 1);
			line = line.trim();
		}
	}

	private static void _checkIfClause(
		String ifClause, String fileName, int lineCount) {

		int quoteCount = StringUtil.count(ifClause, StringPool.QUOTE);

		if ((quoteCount % 2) == 1) {
			return;
		}

		ifClause = _stripQuotes(ifClause);

		if (ifClause.contains(StringPool.DOUBLE_SLASH) ||
			ifClause.contains("/*") || ifClause.contains("*/")) {

			return;
		}

		ifClause = _stripRedundantParentheses(ifClause);

		ifClause = StringUtil.replace(
			ifClause, new String[] {"'('", "')'"},
			new String[] {StringPool.BLANK, StringPool.BLANK});

		int level = 0;
		int max = StringUtil.count(ifClause, StringPool.OPEN_PARENTHESIS);
		int previousParenthesisPos = -1;

		int[] levels = new int[max];

		for (int i = 0; i < ifClause.length(); i++) {
			char c = ifClause.charAt(i);

			if ((c == CharPool.OPEN_PARENTHESIS) ||
				(c == CharPool.CLOSE_PARENTHESIS)) {

				if (previousParenthesisPos != -1) {
					String s = ifClause.substring(
						previousParenthesisPos + 1, i);

					_checkMissingParentheses(s, fileName, lineCount);
				}

				previousParenthesisPos = i;

				if (c == CharPool.OPEN_PARENTHESIS) {
					levels[level] = i;

					level += 1;
				}
				else {
					int posOpenParenthesis = levels[level - 1];

					if (level > 1) {
						char nextChar = ifClause.charAt(i + 1);
						char previousChar = ifClause.charAt(
							posOpenParenthesis - 1);

						if (!Character.isLetterOrDigit(nextChar) &&
							(nextChar != CharPool.PERIOD) &&
							!Character.isLetterOrDigit(previousChar)) {

							String s = ifClause.substring(
								posOpenParenthesis + 1, i);

							if (Validator.isNotNull(s) &&
								!s.contains(StringPool.SPACE)) {

								_sourceFormatterHelper.printError(
									fileName,
									"redundant parentheses: " + fileName + " " +
										lineCount);
							}
						}

						if ((previousChar == CharPool.OPEN_PARENTHESIS) &&
							(nextChar == CharPool.CLOSE_PARENTHESIS)) {

							_sourceFormatterHelper.printError(
								fileName,
								"redundant parentheses: " + fileName + " " +
									lineCount);
						}
					}

					level -= 1;
				}
			}
		}
	}

	private static void _checkLanguageKeys(
			String fileName, String content, Pattern pattern)
		throws IOException {

		String fileExtension = _fileUtil.getExtension(fileName);

		if (!_portalSource || fileExtension.equals("vm")) {
			return;
		}

		if (_portalLanguageKeysProperties == null) {
			_portalLanguageKeysProperties = new Properties();

			ClassLoader classLoader = SourceFormatter.class.getClassLoader();

			InputStream inputStream = classLoader.getResourceAsStream(
				"content/Language.properties");

			_portalLanguageKeysProperties.load(inputStream);
		}

		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			String[] languageKeys = _getLanguageKeys(matcher);

			for (String languageKey : languageKeys) {
				if (Validator.isNumber(languageKey) ||
					languageKey.endsWith(StringPool.DASH) ||
					languageKey.endsWith(StringPool.PERIOD) ||
					languageKey.endsWith(StringPool.UNDERLINE) ||
					languageKey.startsWith(StringPool.DASH) ||
					languageKey.startsWith(StringPool.PERIOD) ||
					languageKey.startsWith(StringPool.UNDERLINE)) {

					continue;
				}

				if (!_portalLanguageKeysProperties.containsKey(languageKey)) {
					_sourceFormatterHelper.printError(
						fileName,
						"missing language key: " + languageKey +
							StringPool.SPACE + fileName);
				}
			}
		}
	}

	private static void _checkMissingParentheses(
		String s, String fileName, int lineCount) {

		if (Validator.isNull(s)) {
			return;
		}

		boolean containsAndOrOperator = (s.contains("&&") || s.contains("||"));

		boolean containsCompareOperator =
			(s.contains(" == ") || s.contains(" != ") || s.contains(" < ") ||
			 s.contains(" > ") || s.contains(" =< ") || s.contains(" => ") ||
			 s.contains(" <= ") || s.contains(" >= "));

		boolean containsMathOperator =
			(s.contains(" = ") || s.contains(" - ") || s.contains(" + ") ||
			 s.contains(" & ") || s.contains(" % ") || s.contains(" * ") ||
			 s.contains(" / "));

		if (containsCompareOperator &&
			(containsAndOrOperator ||
			 (containsMathOperator && !s.contains(StringPool.OPEN_BRACKET)))) {

			_sourceFormatterHelper.printError(
				fileName, "missing parentheses: " + fileName + " " + lineCount);
		}
	}

	private static boolean _checkTaglibVulnerability(
		String jspContent, String vulnerability) {

		int pos1 = -1;

		do {
			pos1 = jspContent.indexOf(vulnerability, pos1 + 1);

			if (pos1 != -1) {
				int pos2 = jspContent.lastIndexOf(CharPool.LESS_THAN, pos1);

				while ((pos2 > 0) &&
					   (jspContent.charAt(pos2 + 1) == CharPool.PERCENT)) {

					pos2 = jspContent.lastIndexOf(CharPool.LESS_THAN, pos2 - 1);
				}

				String tagContent = jspContent.substring(pos2, pos1);

				if (!tagContent.startsWith("<aui:") &&
					!tagContent.startsWith("<liferay-portlet:") &&
					!tagContent.startsWith("<liferay-util:") &&
					!tagContent.startsWith("<portlet:")) {

					return true;
				}
			}
		}
		while (pos1 != -1);

		return false;
	}

	private static void _checkXSS(String fileName, String jspContent) {
		Matcher matcher = _xssPattern.matcher(jspContent);

		while (matcher.find()) {
			boolean xssVulnerable = false;

			String jspVariable = matcher.group(1);

			String anchorVulnerability = " href=\"<%= " + jspVariable + " %>";

			if (_checkTaglibVulnerability(jspContent, anchorVulnerability)) {
				xssVulnerable = true;
			}

			String inputVulnerability = " value=\"<%= " + jspVariable + " %>";

			if (_checkTaglibVulnerability(jspContent, inputVulnerability)) {
				xssVulnerable = true;
			}

			String inlineStringVulnerability1 = "'<%= " + jspVariable + " %>";

			if (jspContent.contains(inlineStringVulnerability1)) {
				xssVulnerable = true;
			}

			String inlineStringVulnerability2 = "(\"<%= " + jspVariable + " %>";

			if (jspContent.contains(inlineStringVulnerability2)) {
				xssVulnerable = true;
			}

			String inlineStringVulnerability3 = " \"<%= " + jspVariable + " %>";

			if (jspContent.contains(inlineStringVulnerability3)) {
				xssVulnerable = true;
			}

			String documentIdVulnerability = ".<%= " + jspVariable + " %>";

			if (jspContent.contains(documentIdVulnerability)) {
				xssVulnerable = true;
			}

			if (xssVulnerable) {
				_sourceFormatterHelper.printError(
					fileName, "(xss): " + fileName + " (" + jspVariable + ")");
			}
		}
	}

	private static void _compareJavaTermNames(
		String fileName, String previousJavaTermName, String javaTermName,
		int lineCount) {

		if (Validator.isNull(previousJavaTermName) ||
			Validator.isNull(javaTermName)) {

			return;
		}

		if (javaTermName.equals("_log")) {
			_sourceFormatterHelper.printError(
				fileName, "sort: " + fileName + " " + lineCount);

			return;
		}

		if (previousJavaTermName.equals("_instance") ||
			previousJavaTermName.equals("_log")) {

			return;
		}

		if (javaTermName.equals("_instance")) {
			_sourceFormatterHelper.printError(
				fileName, "sort: " + fileName + " " + lineCount);

			return;
		}

		if (previousJavaTermName.compareToIgnoreCase(javaTermName) <= 0) {
			return;
		}

		String javaTermNameLowerCase = javaTermName.toLowerCase();
		String previousJavaTermNameLowerCase =
			previousJavaTermName.toLowerCase();

		if (fileName.contains("persistence") &&
			((previousJavaTermName.startsWith("doCount") &&
			  javaTermName.startsWith("doCount")) ||
			 (previousJavaTermName.startsWith("doFind") &&
			  javaTermName.startsWith("doFind")) ||
			 (previousJavaTermNameLowerCase.startsWith("count") &&
			  javaTermNameLowerCase.startsWith("count")) ||
			 (previousJavaTermNameLowerCase.startsWith("filter") &&
			  javaTermNameLowerCase.startsWith("filter")) ||
			 (previousJavaTermNameLowerCase.startsWith("find") &&
			  javaTermNameLowerCase.startsWith("find")) ||
			 (previousJavaTermNameLowerCase.startsWith("join") &&
			  javaTermNameLowerCase.startsWith("join")))) {

			return;
		}

		_sourceFormatterHelper.printError(
			fileName, "sort: " + fileName + " " + lineCount);
	}

	private static void _compareMethodParameterTypes(
		String fileName, List<String> previousMethodParameterTypes,
		List<String> methodParameterTypes, int lineCount) {

		if (methodParameterTypes.isEmpty()) {
			_sourceFormatterHelper.printError(
				fileName, "sort: " + fileName + " " + lineCount);

			return;
		}

		for (int i = 0; i < previousMethodParameterTypes.size(); i++) {
			if (methodParameterTypes.size() < (i + 1)) {
				_sourceFormatterHelper.printError(
					fileName, "sort: " + fileName + " " + lineCount);

				return;
			}

			String previousParameterType = previousMethodParameterTypes.get(i);

			if (previousParameterType.endsWith("...")) {
				previousParameterType = StringUtil.replaceLast(
					previousParameterType, "...", StringPool.BLANK);
			}

			String parameterType = methodParameterTypes.get(i);

			if (previousParameterType.compareToIgnoreCase(parameterType) < 0) {
				return;
			}

			if (previousParameterType.compareToIgnoreCase(parameterType) > 0) {
				_sourceFormatterHelper.printError(
					fileName, "sort: " + fileName + " " + lineCount);

				return;
			}

			if (previousParameterType.compareTo(parameterType) > 0) {
				return;
			}

			if (previousParameterType.compareTo(parameterType) < 0) {
				_sourceFormatterHelper.printError(
					fileName, "sort: " + fileName + " " + lineCount);

				return;
			}
		}
	}

	private static String _fixAntXMLProjectName(
			String basedir, String fileName, String content)
		throws IOException {

		int x = 0;

		if (fileName.endsWith("-ext/build.xml")) {
			x = fileName.indexOf("ext/");

			if (x == -1) {
				x = 0;
			}
			else {
				x = x + 4;
			}
		}
		else if (fileName.endsWith("-hook/build.xml")) {
			x = fileName.indexOf("hooks/");

			if (x == -1) {
				x = 0;
			}
			else {
				x = x + 6;
			}
		}
		else if (fileName.endsWith("-layouttpl/build.xml")) {
			x = fileName.indexOf("layouttpl/");

			if (x == -1) {
				x = 0;
			}
			else {
				x = x + 10;
			}
		}
		else if (fileName.endsWith("-portlet/build.xml")) {
			x = fileName.indexOf("portlets/");

			if (x == -1) {
				x = 0;
			}
			else {
				x = x + 9;
			}
		}
		else if (fileName.endsWith("-theme/build.xml")) {
			x = fileName.indexOf("themes/");

			if (x == -1) {
				x = 0;
			}
			else {
				x = x + 7;
			}
		}
		else if (fileName.endsWith("-web/build.xml") &&
				 !fileName.endsWith("/ext-web/build.xml")) {

			x = fileName.indexOf("webs/");

			if (x == -1) {
				x = 0;
			}
			else {
				x = x + 5;
			}
		}
		else {
			return content;
		}

		int y = fileName.indexOf("/", x);

		String correctProjectElementText =
			"<project name=\"" + fileName.substring(x, y) + "\"";

		if (!content.contains(correctProjectElementText)) {
			x = content.indexOf("<project name=\"");

			y = content.indexOf("\"", x) + 1;
			y = content.indexOf("\"", y) + 1;

			content =
				content.substring(0, x) + correctProjectElementText +
					content.substring(y);

			_sourceFormatterHelper.printError(
				fileName, fileName + " has an incorrect project name");

			_fileUtil.write(basedir + fileName, content);
		}

		return content;
	}

	private static String _fixDataAccessConnection(
		String className, String content) {

		int x = content.indexOf("package ");

		int y = content.indexOf(CharPool.SEMICOLON, x);

		if ((x == -1) || (y == -1)) {
			return content;
		}

		String packageName = content.substring(x + 8, y);

		if (!packageName.startsWith("com.liferay.portal.kernel.upgrade") &&
			!packageName.startsWith("com.liferay.portal.kernel.verify") &&
			!packageName.startsWith("com.liferay.portal.upgrade") &&
			!packageName.startsWith("com.liferay.portal.verify")) {

			return content;
		}

		content = StringUtil.replace(
			content, "DataAccess.getConnection",
			"DataAccess.getUpgradeOptimizedConnection");

		return content;
	}

	private static String _fixSessionKey(
		String fileName, String content, Pattern pattern) {

		Matcher matcher = pattern.matcher(content);

		if (!matcher.find()) {
			return content;
		}

		String newContent = content;

		do {
			String match = matcher.group();

			int x = -1;

			if (pattern.equals(_sessionKeyPattern)) {
				x = match.indexOf(StringPool.COMMA);
			}
			else if (pattern.equals(_taglibSessionKeyPattern)) {
				x = match.indexOf("key=");
			}

			String substring = match.substring(x + 1).trim();

			String quote = StringPool.BLANK;

			if (substring.startsWith(StringPool.APOSTROPHE)) {
				quote = StringPool.APOSTROPHE;
			}
			else if (substring.startsWith(StringPool.QUOTE)) {
				quote = StringPool.QUOTE;
			}
			else {
				continue;
			}

			int y = match.indexOf(quote, x + 1);
			int z = match.indexOf(quote, y + 1);

			if ((x == -1) || (y == -1) || (z == -1)) {
				continue;
			}

			String prefix = match.substring(0, y + 1);
			String suffix = match.substring(z);
			String oldKey = match.substring(y + 1, z);

			for (char c : oldKey.toCharArray()) {
				if (!Validator.isChar(c) || !Validator.isDigit(c) ||
					(c != CharPool.DASH) || (c != CharPool.UNDERLINE)) {

					continue;
				}
			}

			String newKey = TextFormatter.format(oldKey, TextFormatter.O);

			newKey = TextFormatter.format(newKey, TextFormatter.M);

			if (newKey.equals(oldKey)) {
				continue;
			}

			String oldSub = prefix.concat(oldKey).concat(suffix);
			String newSub = prefix.concat(newKey).concat(suffix);

			newContent = StringUtil.replaceFirst(newContent, oldSub, newSub);
		}
		while (matcher.find());

		return newContent;
	}

	private static void _formatAntXML() throws DocumentException, IOException {
		String basedir = "./";

		DirectoryScanner directoryScanner = new DirectoryScanner();

		directoryScanner.setBasedir(basedir);
		directoryScanner.setIncludes(new String[] {"**\\b*.xml"});
		directoryScanner.setExcludes(new String[] {"**\\tools\\**"});

		List<String> fileNames = _sourceFormatterHelper.scanForFiles(
			directoryScanner);

		for (String fileName : fileNames) {
			File file = new File(basedir + fileName);

			String content = _fileUtil.read(file);

			String newContent = _trimContent(content);

			newContent = _fixAntXMLProjectName(basedir, fileName, newContent);

			Document document = _saxReaderUtil.read(newContent);

			Element rootElement = document.getRootElement();

			String previousName = StringPool.BLANK;

			List<Element> targetElements = rootElement.elements("target");

			for (Element targetElement : targetElements) {
				String name = targetElement.attributeValue("name");

				if (name.equals("Test")) {
					name = name.toLowerCase();
				}

				if (name.compareTo(previousName) < -1) {
					_sourceFormatterHelper.printError(
						fileName,
						fileName + " has an unordered target " + name);

					break;
				}

				previousName = name;
			}

			if ((newContent != null) && !content.equals(newContent)) {
				_fileUtil.write(file, newContent);

				_sourceFormatterHelper.printError(fileName, file);
			}
		}
	}

	private static void _formatDDLStructuresXML()
		throws DocumentException, IOException {

		String basedir =
			"./portal-impl/src/com/liferay/portal/events/dependencies/";

		if (!_fileUtil.exists(basedir)) {
			return;
		}

		DirectoryScanner directoryScanner = new DirectoryScanner();

		directoryScanner.setBasedir(basedir);
		directoryScanner.setIncludes(new String[] {"**\\*structures.xml"});

		List<String> fileNames = _sourceFormatterHelper.scanForFiles(
			directoryScanner);

		for (String fileName : fileNames) {
			File file = new File(basedir + fileName);

			String content = _fileUtil.read(file);

			String newContent = _trimContent(content);

			newContent = _formatDDLStructuresXML(content);

			if ((newContent != null) && !content.equals(newContent)) {
				_fileUtil.write(file, newContent);

				_sourceFormatterHelper.printError(fileName, file);
			}
		}
	}

	private static String _formatDDLStructuresXML(String content)
		throws DocumentException, IOException {

		Document document = _saxReaderUtil.read(content);

		Element rootElement = document.getRootElement();

		rootElement.sortAttributes(true);

		rootElement.sortElementsByChildElement("structure", "name");

		List<Element> structureElements = rootElement.elements("structure");

		for (Element structureElement : structureElements) {
			Element structureRootElement = structureElement.element("root");

			structureRootElement.sortElementsByAttribute(
				"dynamic-element", "name");

			List<Element> dynamicElementElements =
				structureRootElement.elements("dynamic-element");

			for (Element dynamicElementElement : dynamicElementElements) {
				Element metaDataElement = dynamicElementElement.element(
					"meta-data");

				metaDataElement.sortElementsByAttribute("entry", "name");
			}
		}

		return document.formattedString();
	}

	private static void _formatFriendlyURLRoutesXML()
		throws DocumentException, IOException {

		String basedir = "./";

		DirectoryScanner directoryScanner = new DirectoryScanner();

		directoryScanner.setBasedir(basedir);
		directoryScanner.setIncludes(new String[] {"**\\*routes.xml"});
		directoryScanner.setExcludes(
			new String[] {"**\\classes\\**", "**\\bin\\**"});

		List<String> fileNames = _sourceFormatterHelper.scanForFiles(
			directoryScanner);

		for (String fileName : fileNames) {
			File file = new File(basedir + fileName);

			String content = _fileUtil.read(file);

			if (content.contains("<!-- SourceFormatter.Ignore -->")) {
				continue;
			}

			String newContent = _trimContent(content);

			newContent = _formatFriendlyURLRoutesXML(content);

			if ((newContent != null) && !content.equals(newContent)) {
				_fileUtil.write(file, newContent);

				_sourceFormatterHelper.printError(fileName, file);
			}
		}
	}

	private static String _formatFriendlyURLRoutesXML(String content)
		throws DocumentException {

		Document document = _saxReaderUtil.read(content);

		Element rootElement = document.getRootElement();

		List<ComparableRoute> comparableRoutes =
			new ArrayList<ComparableRoute>();

		for (Element routeElement : rootElement.elements("route")) {
			String pattern = routeElement.elementText("pattern");

			ComparableRoute comparableRoute = new ComparableRoute(pattern);

			for (Element generatedParameterElement :
					routeElement.elements("generated-parameter")) {

				String name = generatedParameterElement.attributeValue("name");
				String value = generatedParameterElement.getText();

				comparableRoute.addGeneratedParameter(name, value);
			}

			for (Element ignoredParameterElement :
					routeElement.elements("ignored-parameter")) {

				String name = ignoredParameterElement.attributeValue("name");

				comparableRoute.addIgnoredParameter(name);
			}

			for (Element implicitParameterElement :
					routeElement.elements("implicit-parameter")) {

				String name = implicitParameterElement.attributeValue("name");
				String value = implicitParameterElement.getText();

				comparableRoute.addImplicitParameter(name, value);
			}

			for (Element overriddenParameterElement :
					routeElement.elements("overridden-parameter")) {

				String name = overriddenParameterElement.attributeValue("name");
				String value = overriddenParameterElement.getText();

				comparableRoute.addOverriddenParameter(name, value);
			}

			comparableRoutes.add(comparableRoute);
		}

		Collections.sort(comparableRoutes);

		StringBundler sb = new StringBundler();

		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<!DOCTYPE routes PUBLIC \"-//Liferay//DTD Friendly URL ");
		sb.append("Routes 6.2.0//EN\" \"http://www.liferay.com/dtd/");
		sb.append("liferay-friendly-url-routes_6_2_0.dtd\">\n\n<routes>\n");

		for (ComparableRoute comparableRoute : comparableRoutes) {
			sb.append("\t<route>\n");
			sb.append("\t\t<pattern>");
			sb.append(comparableRoute.getPattern());
			sb.append("</pattern>\n");

			Map<String, String> generatedParameters =
				comparableRoute.getGeneratedParameters();

			for (Map.Entry<String, String> entry :
					generatedParameters.entrySet()) {

				sb.append("\t\t<generated-parameter name=\"");
				sb.append(entry.getKey());
				sb.append("\">");
				sb.append(entry.getValue());
				sb.append("</generated-parameter>\n");
			}

			Set<String> ignoredParameters =
				comparableRoute.getIgnoredParameters();

			for (String entry : ignoredParameters) {
				sb.append("\t\t<ignored-parameter name=\"");
				sb.append(entry);
				sb.append("\" />\n");
			}

			Map<String, String> implicitParameters =
				comparableRoute.getImplicitParameters();

			for (Map.Entry<String, String> entry :
					implicitParameters.entrySet()) {

				sb.append("\t\t<implicit-parameter name=\"");
				sb.append(entry.getKey());
				sb.append("\">");
				sb.append(entry.getValue());
				sb.append("</implicit-parameter>\n");
			}

			Map<String, String> overriddenParameters =
				comparableRoute.getOverriddenParameters();

			for (Map.Entry<String, String> entry :
					overriddenParameters.entrySet()) {

				sb.append("\t\t<overridden-parameter name=\"");
				sb.append(entry.getKey());
				sb.append("\">");
				sb.append(entry.getValue());
				sb.append("</overridden-parameter>\n");
			}

			sb.append("\t</route>\n");
		}

		sb.append("</routes>");

		return sb.toString();
	}

	private static void _formatFTL() throws IOException {
		String basedir = "./";

		DirectoryScanner directoryScanner = new DirectoryScanner();

		directoryScanner.setBasedir(basedir);
		directoryScanner.setIncludes(new String[] {"**\\*.ftl"});
		directoryScanner.setExcludes(
			new String[] {
				"**\\journal\\dependencies\\template.ftl",
				"**\\servicebuilder\\dependencies\\props.ftl"
			});

		List<String> fileNames = _sourceFormatterHelper.scanForFiles(
			directoryScanner);

		for (String fileName : fileNames) {
			File file = new File(basedir + fileName);

			String content = _fileUtil.read(file);

			String newContent = _trimContent(content);

			if ((newContent != null) && !content.equals(newContent)) {
				_fileUtil.write(file, newContent);

				_sourceFormatterHelper.printError(fileName, file);
			}
		}
	}

	private static String _formatImports(String imports, int classStartPos)
		throws IOException {

		if (imports.contains("/*") || imports.contains("*/") ||
			imports.contains("//")) {

			return imports + "\n";
		}

		List<String> importsList = new ArrayList<String>();

		UnsyncBufferedReader unsyncBufferedReader = new UnsyncBufferedReader(
			new UnsyncStringReader(imports));

		String line = null;

		while ((line = unsyncBufferedReader.readLine()) != null) {
			if ((line.contains("import=") || line.contains("import ")) &&
				!importsList.contains(line)) {

				importsList.add(line);
			}
		}

		importsList = ListUtil.sort(importsList);

		StringBundler sb = new StringBundler();

		String temp = null;

		for (int i = 0; i < importsList.size(); i++) {
			String s = importsList.get(i);

			int pos = s.indexOf(".");

			pos = s.indexOf(".", pos + 1);

			if (pos == -1) {
				pos = s.indexOf(".");
			}

			String packageLevel = s.substring(classStartPos, pos);

			if ((i != 0) && !packageLevel.equals(temp)) {
				sb.append("\n");
			}

			temp = packageLevel;

			sb.append(s);
			sb.append("\n");
		}

		return sb.toString();
	}

	private static void _formatJava() throws IOException {
		String copyright = _getCopyright();
		String oldCopyright = _getOldCopyright();

		boolean portalJavaFiles = true;

		Collection<String> fileNames = null;

		if (_portalSource) {
			fileNames = _getPortalJavaFiles();

			_javaTermAlphabetizeExclusionsProperties =
				_getPortalExclusionsProperties(
					"source_formatter_javaterm_alphabetize_exclusions." +
						"properties");
			_lineLengthExclusionsProperties = _getPortalExclusionsProperties(
				"source_formatter_line_length_exclusions.properties");
		}
		else {
			portalJavaFiles = false;

			fileNames = _getPluginJavaFiles();

			_javaTermAlphabetizeExclusionsProperties =
				_getPluginExclusionsProperties(
					"source_formatter_javaterm_alphabetize_exclusions." +
						"properties");
			_lineLengthExclusionsProperties = _getPluginExclusionsProperties(
				"source_formatter_line_length_exclusions.properties");
		}

		for (String fileName : fileNames) {
			File file = new File(fileName);

			String content = _fileUtil.read(file);

			if (_isGenerated(content)) {
				continue;
			}

			String className = file.getName();

			className = className.substring(0, className.length() - 5);

			String packagePath = fileName;

			int packagePathX = packagePath.indexOf(
				File.separator + "src" + File.separator);
			int packagePathY = packagePath.lastIndexOf(File.separator);

			if ((packagePathX + 5) >= packagePathY) {
				packagePath = StringPool.BLANK;
			}
			else {
				packagePath = packagePath.substring(
					packagePathX + 5, packagePathY);
			}

			packagePath = StringUtil.replace(
				packagePath, File.separator, StringPool.PERIOD);

			if (packagePath.endsWith(".model")) {
				if (content.contains("extends " + className + "Model")) {
					continue;
				}
			}

			String newContent = content;

			if (newContent.contains("$\n */")) {
				_sourceFormatterHelper.printError(fileName, "*: " + fileName);

				newContent = StringUtil.replace(
					newContent, "$\n */", "$\n *\n */");
			}

			if ((oldCopyright != null) && newContent.contains(oldCopyright)) {
				newContent = StringUtil.replace(
					newContent, oldCopyright, copyright);

				_sourceFormatterHelper.printError(
					fileName, "old (c): " + fileName);
			}

			if (!newContent.contains(copyright)) {
				String customCopyright = _getCustomCopyright(file);

				if (Validator.isNull(customCopyright) ||
					!newContent.contains(customCopyright)) {

					_sourceFormatterHelper.printError(
						fileName, "(c): " + fileName);
				}
			}

			if (newContent.contains(className + ".java.html")) {
				_sourceFormatterHelper.printError(
					fileName, "Java2HTML: " + fileName);
			}

			if (newContent.contains(" * @author Raymond Aug") &&
				!newContent.contains(" * @author Raymond Aug\u00e9")) {

				newContent = newContent.replaceFirst(
					"Raymond Aug.++", "Raymond Aug\u00e9");

				_sourceFormatterHelper.printError(
					fileName, "UTF-8: " + fileName);
			}

			newContent = _fixDataAccessConnection(className, newContent);
			newContent = _fixSessionKey(
				fileName, newContent, _sessionKeyPattern);

			newContent = StringUtil.replace(
				newContent,
				new String[] {
					"com.liferay.portal.PortalException",
					"com.liferay.portal.SystemException",
					"com.liferay.util.LocalizationUtil",
					"private static final Log _log"
				},
				new String[] {
					"com.liferay.portal.kernel.exception.PortalException",
					"com.liferay.portal.kernel.exception.SystemException",
					"com.liferay.portal.kernel.util.LocalizationUtil",
					"private static Log _log"
				});

			newContent = stripJavaImports(newContent, packagePath, className);

			newContent = StringUtil.replace(
				newContent,
				new String[] {
					";\n/**", "\t/*\n\t *", "else{", "if(", "for(", "while(",
					"List <", "){\n", "]{\n", "\n\n\n"
				},
				new String[] {
					";\n\n/**", "\t/**\n\t *", "else {", "if (", "for (",
					"while (", "List<", ") {\n", "] {\n", "\n\n"
				});

			if (newContent.contains("*/\npackage ")) {
				_sourceFormatterHelper.printError(
					fileName, "package: " + fileName);
			}

			if (!newContent.endsWith("\n\n}") && !newContent.endsWith("{\n}")) {
				_sourceFormatterHelper.printError(fileName, "}: " + fileName);
			}

			if (portalJavaFiles && !className.equals("BaseServiceImpl") &&
				className.endsWith("ServiceImpl") &&
				newContent.contains("ServiceUtil.")) {

				_sourceFormatterHelper.printError(
					fileName, "ServiceUtil: " + fileName);
			}

			if (!className.equals("DeepNamedValueScanner") &&
				!className.equals("ProxyUtil") &&
				newContent.contains("import java.lang.reflect.Proxy;")) {

				_sourceFormatterHelper.printError(
					fileName, "Proxy: " + fileName);
			}

			// LPS-28266

			for (int pos1 = -1;;) {
				pos1 = newContent.indexOf(StringPool.TAB + "try {", pos1 + 1);

				if (pos1 == -1) {
					break;
				}

				int pos2 = newContent.indexOf(
					StringPool.TAB + "try {", pos1 + 1);
				int pos3 = newContent.indexOf("\"select count(", pos1);

				if ((pos2 != -1) && (pos3 != -1) && (pos2 < pos3)) {
					continue;
				}

				int pos4 = newContent.indexOf("rs.getLong(1)", pos1);
				int pos5 = newContent.indexOf(
					StringPool.TAB + "finally {", pos1);

				if ((pos3 == -1) || (pos4 == -1) || (pos5 == -1)) {
					break;
				}

				if ((pos3 < pos4) && (pos4 < pos5)) {
					_sourceFormatterHelper.printError(
						fileName, "Use getInt(1) for count: " + fileName);
				}
			}

			_checkLanguageKeys(fileName, newContent, _languageKeyPattern);

			String oldContent = newContent;

			for (;;) {
				newContent = _formatJava(fileName, oldContent);

				if (oldContent.equals(newContent)) {
					break;
				}

				oldContent = newContent;
			}

			if ((newContent != null) && !content.equals(newContent)) {
				_fileUtil.write(file, newContent);

				_sourceFormatterHelper.printError(fileName, file);
			}
		}
	}

	private static String _formatJava(String fileName, String content)
		throws IOException {

		StringBundler sb = new StringBundler();

		UnsyncBufferedReader unsyncBufferedReader = new UnsyncBufferedReader(
			new UnsyncStringReader(content));

		int lineCount = 0;

		String line = null;

		String previousLine = StringPool.BLANK;

		int lineToSkipIfEmpty = 0;

		String javaTermName = null;
		int javaTermType = 0;

		String previousJavaTermName = null;
		int previousJavaTermType = 0;

		List<String> parameterTypes = new ArrayList<String>();
		List<String> previousParameterTypes = null;

		boolean hasSameConstructorOrMethodName = false;
		boolean readParameterTypes = false;

		String ifClause = StringPool.BLANK;

		String packageName = StringPool.BLANK;

		while ((line = unsyncBufferedReader.readLine()) != null) {
			lineCount++;

			line = _trimLine(line);

			line = StringUtil.replace(
				line,
				new String[] {
					"* Copyright (c) 2000-2011 Liferay, Inc."
				},
				new String[] {
					"* Copyright (c) 2000-2012 Liferay, Inc."
				});

			if (line.startsWith("package ")) {
				packageName = line.substring(8, line.length() - 1);
			}

			if (line.startsWith("import ")) {
				int pos = line.lastIndexOf(StringPool.PERIOD);

				if (pos != -1) {
					String importPackageName = line.substring(7, pos);

					if (importPackageName.equals(packageName)) {
						continue;
					}
				}
			}

			if (line.contains(StringPool.TAB + "for (") && line.contains(":") &&
				!line.contains(" :")) {

				line = StringUtil.replace(line, ":" , " :");
			}

			line = _replacePrimitiveWrapperInstantiation(
				fileName, line, lineCount);

			String trimmedLine = StringUtil.trimLeading(line);

			if (trimmedLine.startsWith(StringPool.EQUAL)) {
				_sourceFormatterHelper.printError(
					fileName, "equal: " + fileName + " " + lineCount);
			}

			if (!trimmedLine.equals("{") && line.endsWith("{") &&
				!line.endsWith(" {")) {

				line = StringUtil.replaceLast(line, "{", " {");
			}

			line = _sortExceptions(line);

			if (trimmedLine.startsWith("if (") ||
				trimmedLine.startsWith("else if (") ||
				trimmedLine.startsWith("while (") ||
				Validator.isNotNull(ifClause)) {

				if (Validator.isNull(ifClause) ||
					ifClause.endsWith(StringPool.OPEN_PARENTHESIS)) {

					ifClause = ifClause + trimmedLine;
				}
				else {
					ifClause = ifClause + StringPool.SPACE + trimmedLine;
				}

				if (ifClause.endsWith(") {")) {
					_checkIfClause(ifClause, fileName, lineCount);

					ifClause = StringPool.BLANK;
				}
			}

			String excluded = null;

			if (_javaTermAlphabetizeExclusionsProperties != null) {
				excluded = _javaTermAlphabetizeExclusionsProperties.getProperty(
					StringUtil.replace(
						fileName, "\\", "/") + StringPool.AT + lineCount);

				if (excluded == null) {
					excluded =
						_javaTermAlphabetizeExclusionsProperties.getProperty(
							StringUtil.replace(fileName, "\\", "/"));
				}
			}

			if (line.startsWith(StringPool.TAB + "private ") ||
				line.startsWith(StringPool.TAB + "protected ") ||
				line.startsWith(StringPool.TAB + "public ")) {

				hasSameConstructorOrMethodName = false;

				Tuple tuple = _getJavaTermTuple(line);

				if (tuple != null) {
					javaTermName = (String)tuple.getObject(0);

					if (Validator.isNotNull(javaTermName)) {
						javaTermType = (Integer)tuple.getObject(1);

						boolean isConstructorOrMethod =
							_isInJavaTermTypeGroup(
								javaTermType, _TYPE_CONSTRUCTOR) ||
							_isInJavaTermTypeGroup(javaTermType, _TYPE_METHOD);

						if (isConstructorOrMethod) {
							readParameterTypes = true;
						}

						if (excluded == null) {
							if (_isInJavaTermTypeGroup(
									javaTermType, _TYPE_VARIABLE_NOT_FINAL)) {

								char firstChar = javaTermName.charAt(0);

								if (firstChar == CharPool.UNDERLINE) {
									firstChar = javaTermName.charAt(1);
								}

								if (Character.isUpperCase(firstChar)) {
									_sourceFormatterHelper.printError(
										fileName,
										"final: " + fileName + " " + lineCount);
								}
							}

							if (Validator.isNotNull(previousJavaTermName)) {
								if (previousJavaTermType > javaTermType) {
									_sourceFormatterHelper.printError(
										fileName,
										"order: " + fileName + " " + lineCount);
								}
								else if (previousJavaTermType == javaTermType) {
									if (isConstructorOrMethod &&
										previousJavaTermName.equals(
											javaTermName)) {

										hasSameConstructorOrMethodName = true;
									}
									else {
										_compareJavaTermNames(
											fileName, previousJavaTermName,
											javaTermName, lineCount);
									}
								}
							}
						}

						previousJavaTermName = javaTermName;
						previousJavaTermType = javaTermType;
					}
				}
			}

			if (readParameterTypes) {
				parameterTypes = _addParameterTypes(
					trimmedLine, parameterTypes);

				if (trimmedLine.contains(StringPool.CLOSE_PARENTHESIS)) {
					if (hasSameConstructorOrMethodName) {
						_compareMethodParameterTypes(
							fileName, previousParameterTypes, parameterTypes,
							lineCount);
					}

					readParameterTypes = false;

					previousParameterTypes = ListUtil.copy(parameterTypes);

					parameterTypes.clear();
				}
			}

			if (!trimmedLine.contains(StringPool.DOUBLE_SLASH) &&
				!trimmedLine.startsWith(StringPool.STAR)) {

				while (trimmedLine.contains(StringPool.TAB)) {
					line = StringUtil.replaceLast(
						line, StringPool.TAB, StringPool.SPACE);

					trimmedLine = StringUtil.replaceLast(
						trimmedLine, StringPool.TAB, StringPool.SPACE);
				}

				if (line.contains(StringPool.TAB + StringPool.SPACE) &&
					!previousLine.endsWith("&&") &&
					!previousLine.endsWith("||") &&
					!previousLine.contains(StringPool.TAB + "((") &&
					!previousLine.contains(StringPool.TAB + StringPool.SPACE) &&
					!previousLine.contains(StringPool.TAB + "implements ") &&
					!previousLine.contains(StringPool.TAB + "throws ")) {

					line = StringUtil.replace(
						line, StringPool.TAB + StringPool.SPACE,
						StringPool.TAB);
				}

				while (trimmedLine.contains(StringPool.DOUBLE_SPACE) &&
					   !trimmedLine.contains(
						   StringPool.QUOTE + StringPool.DOUBLE_SPACE) &&
					   !fileName.contains("Test")) {

					line = StringUtil.replaceLast(
						line, StringPool.DOUBLE_SPACE, StringPool.SPACE);

					trimmedLine = StringUtil.replaceLast(
						trimmedLine, StringPool.DOUBLE_SPACE, StringPool.SPACE);
				}

				if (!line.contains(StringPool.QUOTE)) {
					if ((trimmedLine.startsWith("private ") ||
						 trimmedLine.startsWith("protected ") ||
						 trimmedLine.startsWith("public ")) &&
						line.contains(" (")) {

						line = StringUtil.replace(line, " (", "(");
					}

					if (line.contains(" [")) {
						line = StringUtil.replace(line, " [", "[");
					}

					for (int x = -1;;) {
						int posComma = line.indexOf(StringPool.COMMA, x + 1);
						int posSemicolon = line.indexOf(
							StringPool.SEMICOLON, x + 1);

						if ((posComma == -1) && (posSemicolon == -1)) {
							break;
						}

						x = Math.min(posComma, posSemicolon);

						if (x == -1) {
							x = Math.max(posComma, posSemicolon);
						}

						if (line.length() > (x + 1)) {
							char nextChar = line.charAt(x + 1);

							if ((nextChar != CharPool.APOSTROPHE) &&
								(nextChar != CharPool.CLOSE_PARENTHESIS) &&
								(nextChar != CharPool.SPACE) &&
								(nextChar != CharPool.STAR)) {

								line = StringUtil.insert(
									line, StringPool.SPACE, x + 1);
							}
						}

						if (x > 0) {
							char previousChar = line.charAt(x - 1);

							if (previousChar == CharPool.SPACE) {
								line = line.substring(0, x - 1).concat(
									line.substring(x));
							}
						}
					}
				}
			}

			if ((line.contains(" && ") || line.contains(" || ")) &&
				line.endsWith(StringPool.OPEN_PARENTHESIS)) {

				_sourceFormatterHelper.printError(
					fileName, "line break: " + fileName + " " + lineCount);
			}

			if (line.contains(StringPool.COMMA) &&
				!line.contains(StringPool.CLOSE_PARENTHESIS) &&
				!line.contains(StringPool.GREATER_THAN) &&
				!line.contains(StringPool.QUOTE) &&
				line.endsWith(StringPool.OPEN_PARENTHESIS)) {

				_sourceFormatterHelper.printError(
					fileName, "line break: " + fileName + " " + lineCount);
			}

			if (line.endsWith(" +") || line.endsWith(" -") ||
				line.endsWith(" *") || line.endsWith(" /")) {

				int x = line.indexOf(" = ");

				if (x != -1) {
					int y = line.indexOf(StringPool.QUOTE);

					if ((y == -1) || (x < y)) {
						_sourceFormatterHelper.printError(
							fileName,
							"line break: " + fileName + " " + lineCount);
					}
				}
			}

			if (line.contains("    ") && !line.matches("\\s*\\*.*")) {
				if (!fileName.endsWith("StringPool.java")) {
					_sourceFormatterHelper.printError(
						fileName, "tab: " + fileName + " " + lineCount);
				}
			}

			if (line.contains("  {") && !line.matches("\\s*\\*.*")) {
				_sourceFormatterHelper.printError(
					fileName, "{:" + fileName + " " + lineCount);
			}

			excluded = null;

			if (_lineLengthExclusionsProperties != null) {
				excluded = _lineLengthExclusionsProperties.getProperty(
					StringUtil.replace(
						fileName, "\\", "/") + StringPool.AT + lineCount);

				if (excluded == null) {
					excluded = _lineLengthExclusionsProperties.getProperty(
						StringUtil.replace(fileName, "\\", "/"));
				}
			}

			Tuple combinedLines = null;
			int lineLength = _getLineLength(line);

			if ((excluded == null) &&
				!line.startsWith("import ") && !line.startsWith("package ") &&
				!line.matches("\\s*\\*.*")) {

				if (fileName.endsWith("Table.java") &&
					line.contains("String TABLE_SQL_CREATE = ")) {
				}
				else if (fileName.endsWith("Table.java") &&
						 line.contains("String TABLE_SQL_DROP = ")) {
				}
				else if (fileName.endsWith("Table.java") &&
						 line.contains(" index IX_")) {
				}
				else {
					if (lineLength > 80) {
						_sourceFormatterHelper.printError(
							fileName, "> 80: " + fileName + " " + lineCount);
					}
					else {
						int lineTabCount = StringUtil.count(
							line, StringPool.TAB);
						int previousLineTabCount = StringUtil.count(
							previousLine, StringPool.TAB);

						if (previousLine.endsWith(StringPool.COMMA) &&
							previousLine.contains(
								StringPool.OPEN_PARENTHESIS) &&
							!previousLine.contains("for (") &&
							(lineTabCount > previousLineTabCount)) {

							_sourceFormatterHelper.printError(
								fileName,
								"line break: " + fileName + " " + lineCount);
						}

						if (Validator.isNotNull(trimmedLine) &&
							((previousLine.endsWith(StringPool.COLON) &&
							  previousLine.contains(StringPool.TAB + "for ")) ||
							 (previousLine.endsWith(
								 StringPool.OPEN_PARENTHESIS) &&
							  previousLine.contains(StringPool.TAB + "if "))) &&
							((previousLineTabCount + 2) != lineTabCount)) {

							_sourceFormatterHelper.printError(
								fileName,
								"line break: " + fileName + " " + lineCount);
						}

						if (previousLine.endsWith(StringPool.PERIOD)) {
							int x = trimmedLine.indexOf(
								StringPool.OPEN_PARENTHESIS);

							if ((x != -1) &&
								((_getLineLength(previousLine) + x) < 80) &&
								(trimmedLine.endsWith(
									StringPool.OPEN_PARENTHESIS) ||
								 (trimmedLine.charAt(x + 1) !=
									 CharPool.CLOSE_PARENTHESIS))) {

								_sourceFormatterHelper.printError(
									fileName,
									"line break: " + fileName + " " +
										lineCount);
							}
						}

						combinedLines = _getCombinedLines(
							trimmedLine, previousLine, lineTabCount,
							previousLineTabCount);
					}
				}
			}

			if (combinedLines != null) {
				previousLine = (String)combinedLines.getObject(0);

				if (combinedLines.getSize() > 1) {
					String linePart = (String)combinedLines.getObject(1);
					boolean addToPreviousLine =
						(Boolean)combinedLines.getObject(2);

					if (addToPreviousLine) {
						previousLine = previousLine + linePart;
						line = StringUtil.replaceFirst(
							line, linePart, StringPool.BLANK);
					}
					else {
						if (((linePart.length() + lineLength) <= 80) &&
							(line.endsWith(StringPool.OPEN_CURLY_BRACE) ||
							 line.endsWith(StringPool.SEMICOLON))) {

							previousLine = StringUtil.replaceLast(
								previousLine, linePart, StringPool.BLANK);

							line = StringUtil.replaceLast(
								line, StringPool.TAB,
								StringPool.TAB + linePart);
						}
						else {
							_sourceFormatterHelper.printError(
								fileName,
								"line break: " + fileName + " " + lineCount);
						}
					}

					sb.append(previousLine);
					sb.append("\n");

					previousLine = line;
				}
				else if (line.endsWith(StringPool.OPEN_CURLY_BRACE) &&
						 !previousLine.contains(" class ")) {

					lineToSkipIfEmpty = lineCount + 1;
				}
			}
			else {
				if ((lineCount > 1) &&
					(Validator.isNotNull(previousLine) ||
					 (lineToSkipIfEmpty != (lineCount - 1)))) {

					sb.append(previousLine);

					if (Validator.isNotNull(previousLine) &&
						Validator.isNotNull(trimmedLine) &&
						!previousLine.contains("/*")) {

						String trimmedPreviousLine = StringUtil.trimLeading(
							previousLine);

						if ((trimmedPreviousLine.startsWith("// ") &&
							 !trimmedLine.startsWith("// ")) ||
							(!trimmedPreviousLine.startsWith("// ") &&
							 trimmedLine.startsWith("// "))) {

							sb.append("\n");
						}
						else if (previousLine.endsWith(
									StringPool.TAB +
										StringPool.CLOSE_CURLY_BRACE) &&
								 !trimmedLine.startsWith(
									 StringPool.CLOSE_CURLY_BRACE) &&
								 !trimmedLine.startsWith(
									 StringPool.CLOSE_PARENTHESIS) &&
								 !trimmedLine.startsWith(
									 StringPool.DOUBLE_SLASH) &&
								 !trimmedLine.startsWith("catch ") &&
								 !trimmedLine.startsWith("else ") &&
								 !trimmedLine.startsWith("finally ") &&
								 !trimmedLine.startsWith("while ")) {

							sb.append("\n");
						}
					}

					sb.append("\n");
				}

				previousLine = line;
			}
		}

		sb.append(previousLine);

		unsyncBufferedReader.close();

		String newContent = sb.toString();

		if (newContent.endsWith("\n")) {
			newContent = newContent.substring(0, newContent.length() - 1);
		}

		return newContent;
	}

	private static void _formatJS() throws IOException {
		String basedir = "./";

		DirectoryScanner directoryScanner = new DirectoryScanner();

		directoryScanner.setBasedir(basedir);

		String[] excludes = {
			"**\\js\\aui\\**", "**\\js\\editor\\**", "**\\js\\misc\\**",
			"**\\tools\\**", "**\\VAADIN\\**"
		};

		excludes = ArrayUtil.append(excludes, _excludes);

		directoryScanner.setExcludes(excludes);

		directoryScanner.setIncludes(new String[] {"**\\*.js"});

		List<String> fileNames = _sourceFormatterHelper.scanForFiles(
			directoryScanner);

		for (String fileName : fileNames) {
			File file = new File(basedir + fileName);

			String content = _fileUtil.read(file);

			String newContent = _trimContent(content);

			newContent = StringUtil.replace(
				newContent,
				new String[] {
					"else{", "for(", "function (", "if(", "while(", "){\n",
					"= new Array();", "= new Object();"
				},
				new String[] {
					"else {", "for (", "function(", "if (", "while (", ") {\n",
					"= [];", "= {};"
				});

			Pattern pattern = Pattern.compile("\t+var \\w+\\, ");

			for (;;) {
				Matcher matcher = pattern.matcher(newContent);

				if (!matcher.find()) {
					break;
				}

				String match = newContent.substring(
					matcher.start(), matcher.end());

				int pos = match.indexOf("var ");

				StringBundler sb = new StringBundler(4);

				sb.append(match.substring(0, match.length() - 2));
				sb.append(StringPool.SEMICOLON);
				sb.append("\n");
				sb.append(match.substring(0, pos + 4));

				newContent = StringUtil.replace(
					newContent, match, sb.toString());
			}

			if (newContent.endsWith("\n")) {
				newContent = newContent.substring(0, newContent.length() - 1);
			}

			_checkLanguageKeys(fileName, newContent, _languageKeyPattern);

			if ((newContent != null) && !content.equals(newContent)) {
				_fileUtil.write(file, newContent);

				_sourceFormatterHelper.printError(fileName, file);
			}
		}
	}

	private static void _formatJSP() throws IOException {
		String basedir = "./";

		String copyright = _getCopyright();
		String oldCopyright = _getOldCopyright();

		DirectoryScanner directoryScanner = new DirectoryScanner();

		directoryScanner.setBasedir(basedir);

		String[] excludes = {
			"**\\portal\\aui\\**", "**\\bin\\**", "**\\null.jsp", "**\\tmp\\**",
			"**\\tools\\**"
		};

		excludes = ArrayUtil.append(excludes, _excludes);

		directoryScanner.setExcludes(excludes);

		directoryScanner.setIncludes(
			new String[] {"**\\*.jsp", "**\\*.jspf", "**\\*.vm"});

		List<String> fileNames = _sourceFormatterHelper.scanForFiles(
			directoryScanner);

		for (String fileName : fileNames) {
			File file = new File(basedir + fileName);

			String content = _fileUtil.read(file);

			fileName = fileName.replace(
				CharPool.BACK_SLASH, CharPool.FORWARD_SLASH);

			_jspContents.put(fileName, content);
		}

		boolean stripJSPImports = true;

		for (String fileName : fileNames) {
			File file = new File(basedir + fileName);

			String content = _fileUtil.read(file);

			String oldContent = content;
			String newContent = StringPool.BLANK;

			for (;;) {
				newContent = _formatJSP(fileName, oldContent);

				if (oldContent.equals(newContent)) {
					break;
				}

				oldContent = newContent;
			}

			newContent = StringUtil.replace(
				newContent,
				new String[] {
					"<br/>", "\"/>", "\" >", "@page import", "\"%>", ")%>",
					"javascript: "
				},
				new String[] {
					"<br />", "\" />", "\">", "@ page import", "\" %>", ") %>",
					"javascript:"
				});

			if (stripJSPImports) {
				try {
					newContent = _stripJSPImports(fileName, newContent);
				}
				catch (RuntimeException re) {
					stripJSPImports = false;
				}
			}

			newContent = StringUtil.replace(
				newContent,
				new String[] {
					"* Copyright (c) 2000-2011 Liferay, Inc."
				},
				new String[] {
					"* Copyright (c) 2000-2012 Liferay, Inc."
				});

			if (fileName.endsWith(".jsp") || fileName.endsWith(".jspf")) {
				if ((oldCopyright != null) &&
					newContent.contains(oldCopyright)) {

					newContent = StringUtil.replace(
						newContent, oldCopyright, copyright);

					_sourceFormatterHelper.printError(
						fileName, "old (c): " + fileName);
				}

				if (!newContent.contains(copyright)) {
					String customCopyright = _getCustomCopyright(file);

					if (Validator.isNull(customCopyright) ||
						!newContent.contains(customCopyright)) {

						_sourceFormatterHelper.printError(
							fileName, "(c): " + fileName);
					}
					else {
						newContent = StringUtil.replace(
							newContent, "<%\n" + customCopyright + "\n%>",
							"<%--\n" + customCopyright + "\n--%>");
					}
				}
				else {
					newContent = StringUtil.replace(
						newContent, "<%\n" + copyright + "\n%>",
						"<%--\n" + copyright + "\n--%>");
				}
			}

			newContent = StringUtil.replace(
				newContent,
				new String[] {
					"alert('<%= LanguageUtil.",
					"alert(\"<%= LanguageUtil.", "confirm('<%= LanguageUtil.",
					"confirm(\"<%= LanguageUtil."
				},
				new String[] {
					"alert('<%= UnicodeLanguageUtil.",
					"alert(\"<%= UnicodeLanguageUtil.",
					"confirm('<%= UnicodeLanguageUtil.",
					"confirm(\"<%= UnicodeLanguageUtil."
				});

			if (newContent.contains("    ")) {
				if (!fileName.matches(".*template.*\\.vm$")) {
					_sourceFormatterHelper.printError(
						fileName, "tab: " + fileName);
				}
			}

			if (fileName.endsWith("init.jsp")) {
				int x = newContent.indexOf("<%@ page import=");

				int y = newContent.lastIndexOf("<%@ page import=");

				y = newContent.indexOf("%>", y);

				if ((x != -1) && (y != -1) && (y > x)) {

					// Set compressImports to false to decompress imports

					boolean compressImports = true;

					if (compressImports) {
						String imports = newContent.substring(x, y);

						imports = StringUtil.replace(
							imports, new String[] {"%>\r\n<%@ ", "%>\n<%@ "},
							new String[] {"%><%@\r\n", "%><%@\n"});

						newContent =
							newContent.substring(0, x) + imports +
								newContent.substring(y);
					}
				}
			}

			newContent = _fixSessionKey(
				fileName, newContent, _sessionKeyPattern);
			newContent = _fixSessionKey(
				fileName, newContent, _taglibSessionKeyPattern);

			_checkLanguageKeys(fileName, newContent, _languageKeyPattern);
			_checkLanguageKeys(fileName, newContent, _taglibLanguageKeyPattern);
			_checkXSS(fileName, newContent);

			if ((newContent != null) && !content.equals(newContent)) {
				_fileUtil.write(file, newContent);

				_sourceFormatterHelper.printError(fileName, file);
			}
		}
	}

	private static String _formatJSP(String fileName, String content)
		throws IOException {

		StringBundler sb = new StringBundler();

		UnsyncBufferedReader unsyncBufferedReader = new UnsyncBufferedReader(
			new UnsyncStringReader(content));

		int lineCount = 0;

		String line = null;

		String previousLine = StringPool.BLANK;

		String currentAttributeAndValue = null;
		String previousAttribute = null;
		String previousAttributeAndValue = null;

		boolean readAttributes = false;

		String currentException = null;
		String previousException = null;

		boolean hasUnsortedExceptions = false;

		while ((line = unsyncBufferedReader.readLine()) != null) {
			lineCount++;

			if (!fileName.contains("jsonw") ||
				!fileName.endsWith("action.jsp")) {

				line = _trimLine(line);
			}

			if (line.contains("<aui:button ") &&
				line.contains("type=\"button\"")) {

				_sourceFormatterHelper.printError(
					fileName, "aui:button " + fileName + " " + lineCount);
			}

			String trimmedLine = StringUtil.trimLeading(line);
			String trimmedPreviousLine = StringUtil.trimLeading(previousLine);

			if (!trimmedLine.equals("%>") && line.contains("%>") &&
				!line.contains("--%>") && !line.contains(" %>")) {

				line = StringUtil.replace(line, "%>", " %>");
			}

			if (line.contains("<%=") && !line.contains("<%= ")) {
				line = StringUtil.replace(line, "<%=", "<%= ");
			}

			if (trimmedPreviousLine.equals("%>") && Validator.isNotNull(line) &&
				!trimmedLine.equals("-->")) {

				sb.append("\n");
			}
			else if (Validator.isNotNull(previousLine) &&
					 !trimmedPreviousLine.equals("<!--") &&
					 trimmedLine.equals("<%")) {

				sb.append("\n");
			}
			else if (trimmedPreviousLine.equals("<%") &&
					 Validator.isNull(line)) {

				continue;
			}
			else if (trimmedPreviousLine.equals("<%") &&
					 trimmedLine.startsWith("//")) {

				sb.append("\n");
			}
			else if (Validator.isNull(previousLine) &&
					 trimmedLine.equals("%>") && (sb.index() > 2)) {

				String lineBeforePreviousLine = sb.stringAt(sb.index() - 3);

				if (!lineBeforePreviousLine.startsWith("//")) {
					sb.setIndex(sb.index() - 1);
				}
			}

			if ((trimmedLine.startsWith("if (") ||
				 trimmedLine.startsWith("else if (") ||
				 trimmedLine.startsWith("while (")) &&
				trimmedLine.endsWith(") {")) {

				_checkIfClause(trimmedLine, fileName, lineCount);
			}

			if (readAttributes) {
				if (!trimmedLine.startsWith(StringPool.FORWARD_SLASH) &&
					!trimmedLine.startsWith(StringPool.GREATER_THAN)) {

					int pos = trimmedLine.indexOf(StringPool.EQUAL);

					if (pos != -1) {
						String attribute = trimmedLine.substring(0, pos);

						if (!trimmedLine.endsWith(StringPool.QUOTE) &&
							!trimmedLine.endsWith(StringPool.APOSTROPHE)) {

							_sourceFormatterHelper.printError(
								fileName,
								"attribute: " + fileName + " " + lineCount);

							readAttributes = false;
						}
						else if (Validator.isNotNull(previousAttribute)) {
							if (!_isJSPAttributName(attribute)) {
								_sourceFormatterHelper.printError(
									fileName,
									"attribute: " + fileName + " " + lineCount);

								readAttributes = false;
							}
							else if (Validator.isNull(
										previousAttributeAndValue) &&
									 (previousAttribute.compareTo(
										 attribute) > 0)) {

								previousAttributeAndValue = previousLine;
								currentAttributeAndValue = line;
							}
						}

						if (!readAttributes) {
							previousAttribute = null;
							previousAttributeAndValue = null;
						}
						else {
							previousAttribute = attribute;
						}
					}
				}
				else {
					previousAttribute = null;

					readAttributes = false;
				}
			}

			if (!hasUnsortedExceptions) {
				int i = line.indexOf("<liferay-ui:error exception=\"<%=");

				if (i != -1) {
					currentException = line.substring(i + 33);

					if (Validator.isNotNull(previousException) &&
						(previousException.compareTo(currentException) > 0)) {

						hasUnsortedExceptions = true;
					}
				}

				if (!hasUnsortedExceptions) {
					previousException = currentException;
					currentException = null;
				}
			}

			if (trimmedLine.startsWith(StringPool.LESS_THAN) &&
				!trimmedLine.startsWith("<%") &&
				!trimmedLine.startsWith("<!")) {
