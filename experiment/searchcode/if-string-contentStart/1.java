/**
 * Copyright 2011 InfoAsset AG
 */
package de.infoasset.platform.template.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import de.infoasset.platform.services.Services;

/**

 */
public class DreamweaverTemplate {

    static String WEBLOG_ADMIN = "infoasset_html_de_weblog-admin.dwt";

    static String WEBLOG_LAYOUT = "infoasset_html_de_weblog-layout.dwt";

    static String BLANK_POPUP = "infoasset_html_de_blank-popup.dwt";

    static String HELP = "infoasset_html_de_help.dwt";

    static String BLANK = "infoasset_html_de_blank.dwt";

    boolean is(String templateName) {
        return this.templateName.equals(templateName);
    }

    static String PROPERTIES = "properties";

    static String CONTENT = "content";

    static String HEAD = "head";

    static String LEFT_CONTENT = "leftContent";

    static String RIGHT_CONTENT = "rightContent";

    static String STYLES = "Styles";

    static String LAYOUT = "Layout";

    public DreamweaverTemplate(ContentExtractor contentExtractor, String fileName, String fileContent) throws StyleException {
        this.contentExtractor = contentExtractor;
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.templateName = getOldStyleTemplateName();
        if (templateName == null) {
            templateName = getNewStyleTemplateName();
        }
        findNewStyleEditables();
        findOldStyleEditables();
        findMyEditables();
        if (templateName == null && editables.size() > 0) {
            throw new StyleException();
        }
    }

    public void convert() throws IOException {
        String originalNameWithoutSource = fileName.substring(contentExtractor.source.toString().length() + 1);
        String newContentFileName = contentExtractor.target.toString() + File.separator + originalNameWithoutSource;
        File newContentFile = new File(newContentFileName);
        newContentFile.getParentFile().mkdirs();
        newContentFile.createNewFile();
        Editable content = editables.get(CONTENT);
        FileUtils.writeStringToFile(new File(newContentFileName), content.content, Services.fileEncoding());

        Editable properties = editables.get(PROPERTIES);
        if (properties != null && "1".equals("2")) {
            if (!(is(BLANK) && standardProperties(properties.content))) {
                File propertiesFile = new File(newContentFileName.substring(0, newContentFileName.length() - 4) + ".js");
                propertiesFile.createNewFile();
                FileUtils.writeStringToFile(new File(propertiesFile.toString()), properties.content, Services.fileEncoding());
            }
        }
    }

    private String getNewStyleTemplateName() {
        Pattern beginPattern = Pattern.compile("<!--[ ]*InstanceBegin[ ]*template=\"/Templates/");
        Matcher beginMatcher = beginPattern.matcher(fileContent);
        if (beginMatcher.find()) {
            String contentStart = fileContent.substring(beginMatcher.end());
            Pattern endPattern = Pattern.compile("\"");
            Matcher endMatcher = endPattern.matcher(contentStart);
            if (endMatcher.find()) {
                return contentStart.substring(0, endMatcher.start());
            } else {
                throw new IllegalStateException();
            }
        }
        return null;
    }

    private String getOldStyleTemplateName() {
        Pattern beginPattern = Pattern.compile("<!--[ ]*#BeginTemplate[ ]*\"/Templates/");
        Matcher beginMatcher = beginPattern.matcher(fileContent);
        if (beginMatcher.find()) {
            String contentStart = fileContent.substring(beginMatcher.end());
            Pattern endPattern = Pattern.compile("\"");
            Matcher endMatcher = endPattern.matcher(contentStart);
            if (endMatcher.find()) {
                return contentStart.substring(0, endMatcher.start());
            } else {
                throw new IllegalStateException();
            }
        }
        return null;
    }

    private void findNewStyleEditables() {
        Pattern beginPattern = Pattern.compile("<!--[ ]*InstanceBeginEditable[ ]*name=\"");
        Matcher beginMatcher = beginPattern.matcher(fileContent);
        while (beginMatcher.find()) {
            String contentStart = fileContent.substring(beginMatcher.end());
            Pattern endPattern = Pattern.compile("\"[ ]*-->");
            Matcher endMatcher = endPattern.matcher(contentStart);
            if (endMatcher.find()) {
                String editableName = contentStart.substring(0, endMatcher.start());
                String editableContent = findNewStyleEditableContent(contentStart.substring(endMatcher.end()));
                editables.put(editableName, new Editable(editableName, editableContent));
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private String findNewStyleEditableContent(String contentStart) {
        Pattern beginPattern = Pattern.compile("<!--[ ]*InstanceEndEditable[ ]*-->");
        Matcher beginMatcher = beginPattern.matcher(contentStart);
        if (beginMatcher.find()) {
            return contentStart.substring(0, beginMatcher.start());
        } else {
            throw new IllegalStateException();
        }
    }

    private void findMyEditables() {
        Pattern beginPattern = Pattern.compile("<!--[ ]*myEditable[ ]*\"");
        Matcher beginMatcher = beginPattern.matcher(fileContent);
        while (beginMatcher.find()) {
            String contentStart = fileContent.substring(beginMatcher.end());
            Pattern endPattern = Pattern.compile("\"[ ]*-->");
            Matcher endMatcher = endPattern.matcher(contentStart);
            if (endMatcher.find()) {
                String editableName = contentStart.substring(0, endMatcher.start());
                String editableContent = findMyEditableContent(contentStart.substring(endMatcher.end()));
                editables.put(editableName, new Editable(editableName, editableContent));
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private String findMyEditableContent(String contentStart) {
        Pattern beginPattern = Pattern.compile("<!--[ ]*myEditableEnd[ ]*-->");
        Matcher beginMatcher = beginPattern.matcher(contentStart);
        if (beginMatcher.find()) {
            return contentStart.substring(0, beginMatcher.start());
        } else {
            throw new IllegalStateException();
        }
    }

    private void findOldStyleEditables() {
        Pattern beginPattern = Pattern.compile("<!--[ ]*#BeginEditable[ ]*\"");
        Matcher beginMatcher = beginPattern.matcher(fileContent);
        while (beginMatcher.find()) {
            String contentStart = fileContent.substring(beginMatcher.end());
            Pattern endPattern = Pattern.compile("\"[ ]*-->");
            Matcher endMatcher = endPattern.matcher(contentStart);
            if (endMatcher.find()) {
                String editableName = contentStart.substring(0, endMatcher.start());
                String editableContent = findOldStyleEditableContent(contentStart.substring(endMatcher.end()));
                editables.put(editableName, new Editable(editableName, editableContent));
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private String findOldStyleEditableContent(String contentStart) {
        Pattern beginPattern = Pattern.compile("<!--[ ]*#EndEditable[ ]*-->");
        Matcher beginMatcher = beginPattern.matcher(contentStart);
        if (beginMatcher.find()) {
            return contentStart.substring(0, beginMatcher.start());
        } else {
            throw new IllegalStateException();
        }
    }

    String fileName;

    ContentExtractor contentExtractor;

    String templateName;

    String fileContent;

    Map<String, Editable> editables = new HashMap<String, Editable>();

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("dwt: ");
        result.append(ContentExtractor.getFormatedFile(fileName));
        // result.append(" template: " + templateName);
        if (is(BLANK_POPUP)) {
            for (Editable editable : editables.values()) {
                result.append(" | " + editable.name);
                if (editable.name.equals(PROPERTIES)) {
                    if (!standardProperties(editable.content)) {
                        result.append(" nonstandard properties: %%" + editable.content + "%%");
                    }
                }
            }
        }
        return result.toString();
    }

    private boolean standardProperties(String properties) {
        Pattern pattern = Pattern.compile(PROPERTIES_PATTERN);
        Matcher matcher = pattern.matcher(properties);
        return matcher.find();
    }

    static String START_1 = "[\n\r ]*<script[ ]*language=\"JavaScript\">[\n\r\t ]*";

    static String START_2 = "function[ ]*Document\\(\\)[ ]*\\{[\n\r\t ]*";

    static String PROPERTIES_PATTERN_3 = "this\\[\"ID\"\\][ ]*=[ ]*\"[^\"]*\";[\n\r\t ]*";

    static String PROPERTIES_PATTERN_4 = "this\\[\"area\"\\][ ]*=[ ]*\"[^\"]*\";[\n\r\t ]*";

    static String CLIENT_STATE = "\\$clientState\\$[\n\r\t ]*";

    static String END_1 = "return[ ]*this;\\}[\n\r\t ]*";

    static String END_2 = "</script>[\n\r\t ]*";

    static String PROPERTIES_PATTERN = START_1 + START_2 + "(" + PROPERTIES_PATTERN_3 + "|" + PROPERTIES_PATTERN_4 + ")*" + CLIENT_STATE + END_1 + END_2;

    static String PROPERTIES_PATTERN_2 = START_1 + START_2 + PROPERTIES_PATTERN_3 + PROPERTIES_PATTERN_4 + CLIENT_STATE + END_1 + END_2;
}

