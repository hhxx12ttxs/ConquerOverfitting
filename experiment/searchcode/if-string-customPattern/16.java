/**
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.mobicents.ssf.flow.engine.builder.xml;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.mobicents.ssf.flow.engine.builder.template.OperatorTemplate;
import org.mobicents.ssf.flow.engine.builder.template.PatternListTemplate;
import org.mobicents.ssf.flow.engine.builder.template.PatternTemplate;
import org.mobicents.ssf.flow.engine.builder.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XmlPatternTemplateBuilder implements TemplateBuilder {
    private DocumentLoader loader = new DocumentLoader();
    private Logger logger = LoggerFactory.getLogger(XmlPatternTemplateBuilder.class);
    
    public enum OpeCode {
        AND, OR, NOT, EQUAL, CONTAINS, EXISTS, SUBDOMAIN, PATTERN_REF, CUSTOM, UNDEFINIED
    }
    
    private Document document;
    
    private Resource resource;
    
    private PatternListTemplate patternList;
    
    private static final String[] OPERATORS = 
        new String[]{"pattern-ref", "customPattern", "and", "contains", "equal", "exists", "not", "or", "subdomain"};
    
    public XmlPatternTemplateBuilder(Resource resource) {
        this.resource = resource;
    }

    public void init() {
        try {
            // resource???????
            this.document = loader.loadDocument(resource);
        } catch (IOException e) {
            logger.error("IO error", e);
        } catch (ParserConfigurationException e) {
            logger.error("Parse error", e);
        } catch (SAXException e) {
            logger.error("SAX error", e);
        }        
    }

    public void buildTemplate() {
        // patternTempalte???
        if(this.document == null)
            return;
        
        Element element = this.document.getDocumentElement();
        
        this.patternList = parsePatternList(element);
    }

    public Template getTemplate() {
        if(this.patternList == null) {
            buildTemplate();
        }
            
        return this.patternList;
    }

    @SuppressWarnings("unchecked")
	private PatternListTemplate parsePatternList(Element element) {
        // TODO ??????
        PatternListTemplate template = new PatternListTemplate();
        
        List<Element> elements = DomUtils.getChildElementsByTagName(element, OPERATORS);
        
        for(Element patternElement:elements) {
            PatternTemplate pattern = parsePatternTemplate(patternElement);
            template.addPattern(pattern);
        }
        
        return template;
    }
    
    private PatternTemplate parsePatternTemplate(Element element) {
        PatternTemplate pattern = new PatternTemplate();
        pattern.setOperator(parseOperatorTemplate(element, null));
        return pattern;
    }
    
    private OperatorTemplate parseOperatorTemplate(Element node, OperatorTemplate parent) {
        NodeList list = node.getChildNodes();
        int size = list.getLength();
        for(int i=0; i<size; i++) {
            Node n = list.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)n;
                OperatorTemplate operator = new OperatorTemplate();
                
                String nodeName = element.getNodeName();
                
                //OpeCode code = toOpeCode(nodeName);
                
                operator.setType(nodeName);
                String var = DomUtils.getTextValue(DomUtils.getChildElementByTagName(element, "var"));
                String value = DomUtils.getTextValue(DomUtils.getChildElementByTagName(element, "value"));
                operator.setVar(var);
                operator.setVar(value);
                
                operator.setId(element.getAttribute("id"));
                operator.setName(element.getAttribute("name"));
                
                if(nodeName.equals("custom")) {
                    operator.setClassName(element.getAttribute("class"));
                }
                String attr = element.getAttribute("ignore-case");
                if(attr != null && !attr.equals("")) {
                    // ignoreCase????????
                    operator.setIgnoreCase(Boolean.valueOf(attr.trim()).booleanValue());
                }
                
                if(parent != null) {
                    parent.addChild(operator);
                } else {
                    parent = operator;
                }
                parseOperatorTemplate(element, operator);
            }
        }
        return parent;
    }
    @SuppressWarnings("unused")
	private OpeCode toOpeCode(String nodeName) {
        if("and".equalsIgnoreCase(nodeName)) {
            return OpeCode.AND;
        } else if("or".equalsIgnoreCase(nodeName)) {
            return OpeCode.OR;
        } else if("not".equalsIgnoreCase(nodeName)) {
            return OpeCode.NOT;
        } else if("equal".equalsIgnoreCase(nodeName)) {
            return OpeCode.EQUAL;
        } else if("exists".equalsIgnoreCase(nodeName)) {
            return OpeCode.EXISTS;
        } else if("contains".equalsIgnoreCase(nodeName)) {
            return OpeCode.CONTAINS;
        } else if("subdomain".equalsIgnoreCase(nodeName)) {
            return OpeCode.SUBDOMAIN;
        } else if("pattern-ref".equalsIgnoreCase(nodeName)) {
            return OpeCode.PATTERN_REF;
        } else if("custom".equalsIgnoreCase(nodeName)) {
            return OpeCode.CUSTOM;
        }
        return OpeCode.UNDEFINIED;
    }
}

