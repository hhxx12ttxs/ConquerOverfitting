package com.bp.pensionline.letter.producer;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bp.pensionline.letter.constants.Constants;
import com.bp.pensionline.letter.constants.HeaderLine;
import com.bp.pensionline.letter.dao.LetterMemberDao;
import com.bp.pensionline.letter.dao.SignatureXmlDao;
import com.bp.pensionline.letter.util.HeaderXmlDaoUtil;
import com.bp.pensionline.letter.util.PasswordGenerator;

public class WelcomeLetterGenerator
{
	public static final int NUMBERING_LISTDEF_ID_START = 100;
	public static final int NUMBERING_LISTDEF_ID_DEFAULT = 2;
	
	public static final Log log = CmsLog.getLog(org.opencms.jsp.CmsJspLoginBean.class);
	
	public static final String MEMBER_VALUE_PATTERN = "([\\[]{2}[^\\[\\]]*[\\]]{2}){1}";
	public static final String breakPattern = "(&lt;br){1}[ \\t]*(/&gt;){1}|(<br){1}[ \\t]*(/>){1}";
	
	private static Pattern memValuePattern = Pattern.compile(MEMBER_VALUE_PATTERN);
	
	
	private LetterMemberDao memberDao = null;
	
	private String newPassword = null;
	
	public WelcomeLetterGenerator(LetterMemberDao memberDao)
	{
		this.memberDao = memberDao;
		newPassword = PasswordGenerator.generateInitialMemberPassword();
	}
	
	public String processLetterHeader(String header)
	{
		if (header == null)
		{
			return null;
		}
		String updateHeader = header;
		
		Matcher memValueMatcher = memValuePattern.matcher(header);
		
		while (memValueMatcher.find())
		{
			String metaMemValueStr = memValueMatcher.group();
			String memValueKey = metaMemValueStr.substring(2, metaMemValueStr.length() - 2);
			
			String memValue = memberDao.get(memValueKey);
			if (memValue == null)
			{
				memValue = "";
			}			
			updateHeader = updateLetterParagraph(updateHeader, memValueKey, memValue);
			
			
			log.info("Replace member value: " + metaMemValueStr + " with " + memValue);			
		}
		return updateHeader;
	}	
	
	public static String processLetterFooter(String footer, boolean hasSignature, String signatureContent, SignatureXmlDao signatureXmlDao)
	{
		if (footer == null)
		{
			return null;
		}	
		
		String updateFooter = footer;		
		Matcher memValueMatcher = memValuePattern.matcher(footer);
		
		while (memValueMatcher.find())
		{
			String metaMemValueStr = memValueMatcher.group();
			String memValueKey = metaMemValueStr.substring(2, metaMemValueStr.length() - 2);
			
			String memValue = "";
			if (memValueKey.equalsIgnoreCase("signature"))
			{
				if (hasSignature)
				{
					updateFooter = addLetterSignature(footer, memValueKey, signatureContent);
				}
				else
				{
					updateFooter = addEmptySignature(footer, memValueKey, 3);
				}
			}			
			else if (memValueKey.equalsIgnoreCase("name"))
			{
				memValue = signatureXmlDao.getName();
				if (memValue == null)
				{
					memValue = "";
				}				
				updateFooter = updateLetterParagraph(updateFooter, memValueKey, memValue);
				log.info("Replace signature value: " + metaMemValueStr + " with " + memValue);
			}
			else if (memValueKey.equalsIgnoreCase("title"))
			{
				memValue = signatureXmlDao.getTitle();
				if (memValue == null)
				{
					memValue = "";
				}				
				updateFooter = updateLetterParagraph(updateFooter, memValueKey, memValue);
				log.info("Replace signature value: " + metaMemValueStr + " with " + memValue);
			}						
		}
		
		return updateFooter;
	}	
	
	//public static String SIGNATURE_DATA_MATCH_PATERN = "([\\[]{2}[Dd]{1}[Aa]{1}[Tt]{1}[Aa]{1}[\\]]{2}){1}";
	public static String SIGNATURE_DATA_MATCH_PATERN = "[[Data]]";
	//public static String SIGNATURE_FILENAME_MATCH_PATERN = "([\\[]{2}[Ff]{1}[Ii]{1}[Ll]{1}[Ee]{1}[Nn]{1}[Aa]{1}[Mm]{1}[Ee]{1}[\\]]{2}){1}";
	public static String SIGNATURE_FILENAME_MATCH_PATERN = "[[Filename]]";
	
	public static String processSignature(String signatureContent, SignatureXmlDao signatureXmlDao)
	{
		
		String tmpSignature = signatureContent;
		if (tmpSignature != null)
		{
			if (signatureXmlDao != null)
			{
				if (signatureXmlDao.isUseOnlineImage())
				{
					tmpSignature = tmpSignature.replace(SIGNATURE_DATA_MATCH_PATERN, signatureXmlDao.getImageData());
				}
				else
				{
					tmpSignature = tmpSignature.replace(SIGNATURE_FILENAME_MATCH_PATERN, signatureXmlDao.getImageFileName());
					log.info("Replace signature value: [[Filename]] with " + signatureXmlDao.getImageFileName());
				}
			}
		}
		
		return tmpSignature;
	}

	
	public String processLetterBody(String body)
	{
		if (body == null)
		{
			return null;
		}	
		
		String updateBody = body;		
		Matcher memValueMatcher = memValuePattern.matcher(body);
		
		while (memValueMatcher.find())
		{
			String metaMemValueStr = memValueMatcher.group();
			String memValueKey = metaMemValueStr.substring(2, metaMemValueStr.length() - 2);
			
			String memValue = "";
			if (memValueKey.toLowerCase().indexOf("password") >= 0)
			{				
				updateBody = replaceString(updateBody, "[[" + memValueKey + "]]", newPassword);
			}
			else
			{
				memValue = memberDao.get(memValueKey);
				if (memValue == null)
				{
					memValue = "";
				}				
				updateBody = updateLetterParagraph(updateBody, memValueKey, memValue);
			}

			log.info("Replace member value: " + metaMemValueStr + " with " + memValue);			
		}
		
//		String[] emptySectProTags = {"<w:sectPr></w:sectPr>", "<w:sectPr/>"};
//		if (updateBody != null)
//		{
//			updateBody = removeRedundantTag(updateBody, emptySectProTags);	
//		}
			
		return updateBody;
	}
	
	public String removeRedundantTag(String letterPart, String[] tagNames)
	{
		String result = letterPart;
		for (int i = 0; i < tagNames.length; i++)
		{
			result = replaceString(result, tagNames[i], "");
		}
		
		return result;
	}
	
	/**
	 * Replace paragraph contains member value key by a new whole paragraph contains signature image.
	 * @param letterPart
	 * @param memValueKey
	 * @param newParagraph
	 * @return
	 */
	public static String addLetterSignature (String letterPart, String memValueKey, String newParagraph)
	{
		if (letterPart == null)
		{
			return null;
		}
		
		if (newParagraph == null)
		{
			return letterPart;
		}
		
		String para = getCurrentParagraph(letterPart, memValueKey);
		String result = replaceParagraph(letterPart, para, newParagraph);
		
		return result;
	}
	
	/**
	 * Replace paragraph contains member value key by a number of empty paragraphs .
	 * @param letterPart
	 * @param memValueKey
	 * @param newParagraph
	 * @return
	 */
	public static String addEmptySignature (String letterPart, String memValueKey, int numOfBreaks)
	{
		if (letterPart == null)
		{
			return null;
		}
		
		numOfBreaks = (numOfBreaks <= 0) ? 1 : numOfBreaks;
				
		String para = getCurrentParagraph(letterPart, memValueKey);
		StringBuffer updateValue = new StringBuffer();
		for (int i = 0; i < numOfBreaks; i++)
		{
			String tempPara = replaceString(para, "[[" + memValueKey + "]]", "");
			updateValue.append(tempPara);			
		}
		String result = replaceParagraph(letterPart, para, updateValue.toString());
		
		return result;
	}	
	
	/**
	 * Update the member value to the key with consideration of paragraph in WordML
	 * Replace all WordML non-understandable in member value such as <br/>
	 * @param memValueKey
	 * @param memValue
	 * @return
	 */
	public static String updateLetterParagraph (String letterPart, String memValueKey, String memberValue)
	{
		if (letterPart == null)
		{
			return null;
		}
		String result = letterPart;
		// replace & by &amp;
		String[] valueParts = memberValue.split(breakPattern);
		
		if (valueParts.length > 1)
		{
			// find the paragraph that contain the memberValueKey
//			String paraPatternStr = "(<w:p>){1}[^\\[\\]]*(\\[\\[" + memValueKey + "\\]\\])+[^\\[\\]]*(</w:p>){1}";
//			Pattern paraPattern = Pattern.compile(paraPatternStr);
			// find the current para
			String para = getCurrentParagraph(letterPart, memValueKey);
			if (para != null)
			{
				// update with new multiple paragraphs
				StringBuffer updateValue = new StringBuffer();
				for (int i = 0; i < valueParts.length; i++)
				{
					if (!valueParts[i].trim().equals(""))
					{
						String tmpValuePart = valueParts[i];
						if (tmpValuePart.indexOf("&") >= 0)
						{
							tmpValuePart = tmpValuePart.replaceAll("&", "&amp;");
						}
												
						String tempPara = replaceString(para, "[[" + memValueKey + "]]", tmpValuePart);
						updateValue.append(tempPara);
					}
				}
				
				result = replaceParagraph(letterPart, para, updateValue.toString());
			}
		}
		else
		{
			String tmpMemberValue = memberValue.replaceAll("&", "&amp;");			
			result = replaceString(result, "[[" + memValueKey + "]]", tmpMemberValue);
		}
		
		return result;
	}
	
	public static String getCurrentParagraph (String letterPart, String memValueKey)
	{
		int keyIndex = letterPart.indexOf("[[" + memValueKey + "]]");
		if (keyIndex > 0)
		{
			String fisrtPart = letterPart.substring(0, keyIndex);
			String secondPart = letterPart.substring(keyIndex);
			
			String paraFirstPart = fisrtPart.substring(fisrtPart.lastIndexOf("<w:p>"));
			String paraSecondPart = secondPart.substring(0, secondPart.indexOf("</w:p>")) + "</w:p>";
			
			return paraFirstPart + paraSecondPart;
		}
		
		return null;
	}
	
	public static String getParagraph(String letterPart, String memValueKey)
	{
		int keyIndex = letterPart.indexOf("((--" + memValueKey + "--))");
		if (keyIndex > 0)
		{
			String fisrtPart = letterPart.substring(0, keyIndex);
			String secondPart = letterPart.substring(keyIndex);
			
			String paraFirstPart = fisrtPart.substring(fisrtPart.lastIndexOf("<w:p>"));
			String paraSecondPart = secondPart.substring(0, secondPart.indexOf("</w:p>")) + "</w:p>";
			
			return paraFirstPart + paraSecondPart;
		}
		
		return null;
	}
	
	/*
	 * Utility method for processing signature
	 */
	public static String[] getSignatureParagraph(String letterPart, String memValueKey) {
		int keyIndex = letterPart.indexOf("((--" + memValueKey + ":");
		if (keyIndex > 0) {
			String fisrtPart = letterPart.substring(0, keyIndex);
			String secondPart = letterPart.substring(keyIndex);
			int s = secondPart.indexOf("--))");
			String signKey = secondPart.substring(secondPart.indexOf(':')+1,s);
			String paraFirstPart = fisrtPart.substring(fisrtPart.lastIndexOf("<w:p>"));
			String paraSecondPart = secondPart.substring(0, secondPart.indexOf("</w:p>")) + "</w:p>";
			
			String pg = paraFirstPart + paraSecondPart;
			return new String[] {pg,signKey};
		}
		
		return null;
	}
	
	public static String replaceParagraph (String letterPart, String oldPara, String newPara)
	{
		int index = letterPart.indexOf(oldPara);
		if (index == -1) return letterPart;
		StringBuffer result = new StringBuffer();
		// append first part
		result.append(letterPart.substring(0, index));
		// append new para
		result.append(newPara);
		// append last part
		result.append(letterPart.substring(index + oldPara.length()));
		
		return result.toString();
	}
	/**
	 * Update listId for numbering list. Find the string <w:ilfo w:val="2">
	 * @return
	 */
	public String updateListForMember(String letterPart, int listId)
	{
		String oldListTagStr = "<w:ilfo w:val=\"2\">";
		String updateListTagStr = "<w:ilfo w:val=\"" + listId + "\">";
		String result = replaceString(letterPart, oldListTagStr, updateListTagStr);
		
		return result;
	}
	
	/**
	 * Update listId for numbering list. Find the string <w:ilfo w:val="2">
	 * @return
	 */
	public static String replaceString(String letterPart, String oldString, String newString)
	{
		int index = letterPart.indexOf(oldString);
		StringBuffer result = new StringBuffer();
		String tempStr = letterPart;
		while (index >= 0)
		{			
			// append first part
			result.append(tempStr.substring(0, index));
			// append new para
			result.append(newString);
			// append last part
			tempStr = tempStr.substring(index + oldString.length());
			index = tempStr.indexOf(oldString);			
		}		
		
		result.append(tempStr);

		return result.toString();
		
	}	
	
	
	/**
	 * Add new numbering list definition to the before header part. XML processing
	 * 	<w:listDef w:listDefId="2">
			<w:lsid w:val="324A730D"/>
			<w:plt w:val="HybridMultilevel"/>
			<w:tmpl w:val="8C8C5D62"/>
			<w:lvl w:ilvl="0" w:tplc="0409000F">
				<w:start w:val="1"/>
				<w:lvlText w:val="%1."/>
				<w:lvlJc w:val="left"/>
				<w:pPr>
					<w:tabs>
						<w:tab w:val="list" w:pos="576"/>
					</w:tabs>
					<w:ind w:left="576" w:hanging="360"/>
				</w:pPr>
			</w:lvl>
		</w:listDef>
		<w:list w:ilfo="2">
			<w:ilst w:val="2"/>
		</w:list>
	 * @param beforeHeader
	 * @param numList
	 * @return
	 */
	public static String addNewNumberingList (String beforeHeader, int numList)
	{
		StringBuffer resultBuf = new StringBuffer();
		// new squence of w:list buffer
		StringBuffer newListPointerStrBuf = new StringBuffer();
		
		String listBeginTagStr = "<w:lists";
		String listEndTagStr = "</w:lists>";
		
		if (beforeHeader == null || numList == 0)
		{
			return beforeHeader;
		}
		// get the w:lists string contains all list definitions
		int listNodeBeginIndex = beforeHeader.indexOf(listBeginTagStr);
		int listNodeEndIndex = beforeHeader.indexOf(listEndTagStr);
		
		if (listNodeBeginIndex < 0 || listNodeEndIndex < 0)
		{
			return beforeHeader;
		}
		
		// append the head
		resultBuf.append(beforeHeader.substring(0, listNodeBeginIndex));
		
		String listXMLString = beforeHeader.substring(listNodeBeginIndex, listNodeEndIndex + listEndTagStr.length());
		
		// parsing the w:lists XML string to get the numbering list definition node.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		try
		{
			builder = factory.newDocumentBuilder();

			ByteArrayInputStream bais = new ByteArrayInputStream(listXMLString.getBytes("UTF-8"));

			document = builder.parse(bais);
			
			Element root = document.getDocumentElement();
			NodeList lists = root.getChildNodes();
			Node numberListDefNode = null;
			boolean numberListFound = false;

			for (int i = 0; i < lists.getLength(); i++)
			{
				if (numberListFound)
				{	
					break;
				}
				
				// get the list definition with Id = NUMBERING_LISTDEF_ID_DEFAULT
				Node listNode = lists.item(i);
				if (listNode.getNodeType() == Node.ELEMENT_NODE && listNode.getNodeName().equals("w:listDef"))
				{
					NamedNodeMap listDefAtts = listNode.getAttributes();
					for (int j = 0; j < listDefAtts.getLength(); j++)
					{
						Node listDefAtt = listDefAtts.item(j);
						if (listDefAtt.getNodeName().equals("w:listDefId") && 
								listDefAtt.getNodeValue().equals("" + NUMBERING_LISTDEF_ID_DEFAULT))
						{
							numberListDefNode = listNode;
							numberListFound = true;
							break;
						}
					}
				}
			}
			
			// copy numberListDefNode to numList times to the root node			
			if (numberListDefNode != null)
			{				
				for (int i = 0; i < numList; i++)
				{					
					// compose list pointer node to append to the w:lists
					Node newNumberListDefNode = cloneListDefNode(numberListDefNode, NUMBERING_LISTDEF_ID_START + i);
					root.insertBefore(newNumberListDefNode, numberListDefNode);
					
					String newListPointerStr = 	"<w:list w:ilfo=\"" + (NUMBERING_LISTDEF_ID_START + i) + "\">" +
													"<w:ilst w:val=\"" + (NUMBERING_LISTDEF_ID_START + i) +"\"/>" +
												"</w:list>";
					
					newListPointerStrBuf.append(newListPointerStr);
				}
			}
			
			// update the new list definitions to result
			resultBuf.append(LetterTransformer.getNodeAsRawText(root));			
			
			// insert the list pointers to results
			int newListNodeEndIndex = resultBuf.indexOf(listEndTagStr);
			resultBuf.insert(newListNodeEndIndex, newListPointerStrBuf.toString());
			
			//System.out.println("resultBuf: " + resultBuf.toString());
			bais.close();
		}
		catch (Exception ex)
		{
			System.out.println("Error in parsing w:lists node: " + ex.toString());
			return beforeHeader;
		}
		
		// append the tail
		resultBuf.append(beforeHeader.substring(listNodeEndIndex + listEndTagStr.length()));
		
		return resultBuf.toString();
	}
	
	/**
	 * create a new list definition node with new defId
	 * @param listDefNode
	 * @return
	 */
	private static Node cloneListDefNode (Node listDefNode, int newListDefId)
	{
		if (listDefNode == null)
		{
			return null;
		}
		// compose list pointer node to append to the w:lists
		Node newListDefNode = listDefNode.cloneNode(true);
		
		// update w:listDefId attribute
		NamedNodeMap listDefAtts = newListDefNode.getAttributes();
		for (int i = 0; i < listDefAtts.getLength(); i++)
		{
			Node listDefAtt = listDefAtts.item(i);
			if (listDefAtt.getNodeName().equals("w:listDefId"))
			{
				listDefAtt.setNodeValue("" + newListDefId);
				break;
			}
		}
		
		// update attribute w:val of child <w:lsid>
		NodeList childListDefNodes = newListDefNode.getChildNodes();
		for (int i = 0; i < childListDefNodes.getLength(); i++)
		{
			Node childListDefNode = childListDefNodes.item(i);
			if (childListDefNode.getNodeType() == Node.ELEMENT_NODE && childListDefNode.getNodeName().equals("w:lsid"))
			{
				NamedNodeMap listIdDefAtts = childListDefNode.getAttributes();
				for (int j = 0; j < listIdDefAtts.getLength(); j++)
				{
					Node listDefIdAtt = listIdDefAtts.item(j);
					if (listDefIdAtt.getNodeName().equals("w:val"))
					{
						String oldValueStr = listDefIdAtt.getNodeValue();	// Hexa string -> convert to integer
						String newValueStr = "";
						try
						{
							int oldValueInt = Integer.parseInt(oldValueStr, 16);
							newValueStr = Integer.toHexString(oldValueInt + newListDefId).toUpperCase();
						}
						catch (NumberFormatException nfe)
						{
							newValueStr = oldValueStr + newListDefId;
						}
						listDefIdAtt.setNodeValue(newValueStr);
						break;
					}
				}
				break;
			}
		}
		
		return newListDefNode;
	}	
	
	public String getNewPassword()
	{
		return newPassword;
	}

	public static String preProcessHeader(String header) {
		try {
			String headerParagraph = getParagraph(header, "Header");
			int startIndex = headerParagraph.indexOf("Header")-4;
			int endIndex = startIndex + 14;
			String leftString = headerParagraph.substring(0, startIndex);
			String rightString = headerParagraph.substring(endIndex);
			HeaderXmlDaoUtil util = new HeaderXmlDaoUtil(Constants.HEADER.XML_VFS_PATH);
			List<HeaderLine> headerLines = util.getHeaderLines();
			String newParagraphs = "";
			for (int i=0; i<headerLines.size(); i++) {
				HeaderLine line = headerLines.get(i);
				String value = line.getValue();
				String newLine = "";
				if (value.trim().length() > 0) {
					String[] headers = value.split(",");
					String newKey = "";
					for (int j=0; j<headers.length; j++) {
						newKey += " [["+headers[j]+"]]";
					}
					newLine = leftString + newKey.substring(1) + rightString;
					newParagraphs += newLine;
				}
			}
			return replaceParagraph(header, headerParagraph, newParagraphs);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("EXCEPTION while preprocessing Header: "+e.toString());
			return header;
		}
	}
}

