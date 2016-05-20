/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// -*- java -*-
// $Id: StrToXML.java 1106 2009-06-03 07:32:17Z vic $
// $Name:  $
//

package ru.adv.db.filter;

import org.w3c.dom.Attr;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ru.adv.util.HtmlEntities;
import ru.adv.util.XmlUtils;
import ru.adv.xml.parser.Parser;
import ru.adv.xml.parser.ParserException;
import ru.adv.xml.parser.XmlDoc;

/**
 * ?????? ??? ?????????????? ????? ? XML.<p>
 * in - String<br>
 * out - DocumentFragment<p>
 * ????????? ???????? <command>namespace</command>,
 * ?????????? ?????? ??????????? ?? ????????? ??????? ???????.
 * ???????????? ????? ?????? XML. ??????? ?????? ?????? ????????? ????????
 * XML, ????? ? ??? ???????? ????????. ????? ??? ??? ??????? ??????
 * ??????????? ??????? ??????? ? ?????????? namespace.
 * <PRE> new Parser().parse(&quot;&lt;root &quot;+namespace+&quot;&gt;&quot;+value.toString()+&quot;&lt;/root&gt;&quot;); </PRE>
 * @version $Revision: 1.14 $
 * @see Filter
 */
public class StrToXML extends Filter {

    public Object perform(Object value, FilterConfig config) throws FilterException {

        if (isNull(value))
            return value;

        // ?????? namespace ???????????
        String namespace = config.getParams().get("namespace");
        namespace = namespace == null ? "" : namespace;

        XmlDoc xml;
        try {
            try {
                xml = new Parser().parse("<x " + namespace + ">\n" + HtmlEntities.replaceHtmlEntitiesToDecEntities(value.toString()) + "\n</x>");
            } catch (ParserException e) {
                xml = new Parser().parse("<x " + namespace + ">\n" + prepareToXML(value.toString()) + "\n</x>");
            }
        } catch (ParserException e) {
            // ????????
            try {
                xml = new Parser().parse("<x " + namespace + ">\n" + "<![CDATA[" + value.toString() + "]]>\n" + "</x>");
            } catch (ParserException e1) {
                adjustLineNumber(e1);
                throw new FilterException(FilterException.FILTER_CANNOT_PARSE_XML, e1);
            }
        }

        // ???????? ???????? ? namspaces
        NamedNodeMap nameAttrs = xml.getDocument().getDocumentElement().getAttributes();

        // ??????? DocFragment (?????? <root>)
        DocumentFragment dNode = xml.getDocument().createDocumentFragment();
        Node[] children = XmlUtils.childrenArray(xml.getDocument().getDocumentElement());
        for (int i = 0; i < children.length; i++) {
            Node child = dNode.appendChild(children[i]);
            // ??? ???????? ????????? ???????? ???????? ? namespace
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                for (int k = 0; k < nameAttrs.getLength(); k++) {
                    ((Element) child).setAttribute(getName(nameAttrs, k), getValue(nameAttrs, k));
                }
            }
        }
        return dNode;
    }

    /**
     * 1) All '&' replace to &lg;&lt;&amp; to '&amp;'
     * 2) Characters for replacing haven't to be inside <![CDATA[...]]>
     * @param text
     * @return
     */
    protected String prepareToXML(String text) {
        final String E_AMP = "&amp;";
        final String E_LT = "&lt;";
        final String E_GT = "&gt;";
        final String START_CDATA = "<![CDATA[";
        final String END_CDATA = "]]>";
        final int STATE_PLANE    = 0;
        final int STATE_IN_CDATA = 1;
        int state = STATE_PLANE;
        StringBuffer escaped = new StringBuffer(text.length());
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (state==STATE_PLANE && c=='&') {
                if (isNext(text,i,E_AMP)||isNext(text,i,E_GT)||isNext(text,i,E_LT)) {
                    escaped.append(c);
                }else{
                    escaped.append(E_AMP);
                }
            }else if (state==STATE_PLANE && c=='<' && isNext(text,i,START_CDATA)) {
                state=STATE_IN_CDATA;
                escaped.append(c);
            }else if (state==STATE_IN_CDATA && c==']' && isNext(text,i,END_CDATA)){
                state=STATE_PLANE;
                escaped.append(c);
            }else{
                escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private boolean isNext(String text, int pos, String waitStr) {
        try {
            return text.substring(pos,pos+waitStr.length()).equals(waitStr);
        }catch(IndexOutOfBoundsException e){
            return false;
        }
    }

    private void adjustLineNumber(ParserException e) {
        if (e.getAttr("line") != null) {
            try {
                int line = Integer.parseInt(e.getAttr("line").toString());
                if (line > 0) {
                    e.setAttr("line", Integer.toString(line - 1));
                }
            } catch (NumberFormatException e1) {
                // just ignore
            }
        }
    }

    private String getValue(NamedNodeMap nameAttrs, int k) {
        return ((Attr) nameAttrs.item(k)).getValue();
    }

    private String getName(NamedNodeMap nameAttrs, int k) {
        return ((Attr) nameAttrs.item(k)).getName();
    }

}







