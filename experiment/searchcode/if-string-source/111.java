package org.xidea.jsi.impl.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.ScriptLoader;

/**
 * @author jindw
 */
public class DefaultExportorFactory {
	/**
	 * ????????
	 */
	public static final int TYPE_REPORT = 0;
	/**
	 * ?????????????????????
	 */
	public static final int TYPE_SIMPLE = 1;
	/**
	 * ?????????????(??????)
	 */
	public static final int TYPE_EXPORT_RESERVE = 3;
	/**
	 * ?????????????(??????????)
	 */
	public static final int TYPE_EXPORT_CONFUSE = 4;



	private final static String JSI_EXPORTOR_FACTORY_CLASS = "org.jside.jsi.tools.export.JSAExportorFactory";
	private static DefaultExportorFactory exportorFactory = null;

	public static DefaultExportorFactory getInstance() {
		try {
			exportorFactory = (DefaultExportorFactory) Class.forName(
					JSI_EXPORTOR_FACTORY_CLASS).newInstance();

		} catch (Exception e) {
			exportorFactory = new DefaultExportorFactory();
		}
		return exportorFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	public JSIExportor createExplorter(int level,Map<String, String[]> param) {
		switch (level) {
		case TYPE_EXPORT_CONFUSE:
			return createConfuseExplorter(param);
		case TYPE_EXPORT_RESERVE:
			return createReserveExplorter(param);
		case TYPE_SIMPLE:
			return createSimpleExplorter(param);
		case TYPE_REPORT:
			return createReportExplorter(param);

		}
		return null;
	}

	protected JSIExportor createConfuseExplorter(Map<String, String[]> param) {
		return null;// throw new UnsupportedOperationException("???????");
	}

	protected JSIExportor createReserveExplorter(Map<String, String[]> param) {
		return null;// throw new UnsupportedOperationException("???????");
	}

	protected JSIExportor createSimpleExplorter(Map<String, String[]> param) {
		return new SimpleExporter();
	}

	protected JSIExportor createReportExplorter(Map<String, String[]> param) {
		return null;// throw new UnsupportedOperationException("???????");
	}

	protected JSIExportor createXMLExplorter(Map<String, String[]> param) {
		return new XMLExporter();
	}
}

class PreloadExporter implements JSIExportor {
	public String export(JSILoadContext context) {
		List<ScriptLoader> result = context.getScriptList();
		StringBuilder out = new StringBuilder();
		//TODO:???????package ??????
		for (ScriptLoader loader : result) {
			String fileName = loader.getName();
			String packageName = SimpleExporter.getPackage(loader).getName();
			String source = SimpleExporter.getPackage(loader).loadText(loader.getName());
			out.append(JSIText.buildPreloadPerfix(packageName, fileName));
			out.append(source);
			out.append(JSIText.buildPreloadPostfix(source));
		}
		return out.toString();
	}
}

class SimpleExporter implements JSIExportor {
	public String export(JSILoadContext context) {
		StringBuilder result = new StringBuilder();
		List<ScriptLoader> scriptList = context.getScriptList();
		for (ScriptLoader entry : scriptList) {
			result.append(getPackage(entry).loadText(entry.getName()));
			result.append("\r\n;\r\n");
		}
		return result.toString();
	}

	static JSIPackage getPackage(ScriptLoader entry) {
		return ((DefaultScriptLoader)entry).getPackage();
	}

}

class XMLExporter implements JSIExportor {

	public String export(JSILoadContext context) {
		StringBuilder content = new StringBuilder(
				"<properties>\n<entry key='#export'>");
		boolean first = true;
		Map<String, String> exportMap = context.getExportMap();
		List<ScriptLoader> scriptList = context.getScriptList();
		for (String object : exportMap.keySet()) {
			if (first) {
				first = false;
			} else {
				content.append(',');
			}
			content.append(exportMap.get(object));
			content.append(':');
			content.append(object);
		}
		;
		content.append("</entry>\n");
		HashMap<String, Object> packageFileMap = new HashMap<String, Object>();
		for (ScriptLoader entry : scriptList) {
			appendEntry(content, entry.getPath(), SimpleExporter.getPackage(entry).loadText(
					entry.getName()));
			String packageName = SimpleExporter.getPackage(entry).getName();
			if (packageFileMap.get(packageName) == null) {
				packageFileMap.put(packageName, "");
				JSIPackage jsiPackage = SimpleExporter.getPackage(entry);
				String source = jsiPackage
						.loadText(JSIPackage.PACKAGE_FILE_NAME);
				appendEntry(content, jsiPackage.getName().replace('.', '/')
						+ '/' + JSIPackage.PACKAGE_FILE_NAME, source);
			}
		}
		content.append("</properties>\n");
		return content.toString();
	}

	private void appendEntry(StringBuilder content, String path, String source) {
		content.append("<entry key='");
		content.append(path);
		content.append("'>");
		if (source.indexOf("]]>") < 0) {

			content.append("<![CDATA[");
			content.append(source);
			content.append("]]>");
		} else {
			source = source.replaceAll("&", "&amp;").replaceAll(">", "&gt;")
					.replaceAll("<", "&lt;");
			content.append(source);
		}
		content.append("</entry>\n");
	}

}
