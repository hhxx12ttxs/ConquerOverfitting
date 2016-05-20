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
// $Id: FilterParser.java 1162 2009-06-26 14:31:43Z vic $
// $Name:  $

package ru.adv.db.filter;

import ru.adv.db.config.DBConfig;
import ru.adv.db.config.DBConfigException;
import ru.adv.db.config.ObjectAttr;
import ru.adv.util.XmlUtils;
import ru.adv.logger.TLogger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import java.util.*;

/**
 * ?????? ????????.
 *
 * @version $Revision: 1.16 $
 * @see Filter
 */
public class FilterParser {

    private FilterCollections _collections = new FilterCollections();
    private FilterCodes _codes = new FilterCodes();
    private FilterMaps _maps = new FilterMaps();
    public static final String OUTPUT_FILTER = "output-filter";
    public static final String INPUT_FILTER = "input-filter";
    private Node _filtersNode;
    private List _listOfCodes;
    private List _listOfCompoundFilters;
    private List _listOfFilterMaps;
    private Map _elements;
    private Map _mapElements;

    public FilterParser(Node filtersNode) {
        _filtersNode = filtersNode;
        prepareElementMap();
    }

    /**
     * ?????????? ??? ????? ???????? ???????? ? filterNode.
     */
    public FilterMaps parse(DBConfig dbc) throws FilterException {
        try {
            parseCodes();
            parseCollections();
            parseMaps(dbc);
        } catch (DBConfigException e) {
            throw new FilterException(e);
        }
        return _maps;
    }

    private void prepareElementMap() {
        _elements = new HashMap();
        _mapElements = new HashMap();
        _listOfCodes = new ArrayList();
        _listOfCompoundFilters = new ArrayList();
        _listOfFilterMaps = new LinkedList();
        NodeList listOfFilters = _filtersNode.getChildNodes();
        for (int i = 0; i < listOfFilters.getLength(); ++i) {
            Element element = XmlUtils.checkIfElement(listOfFilters.item(i));
            if (element == null || element.getParentNode() != _filtersNode) {
                continue;
            }
            if ("filter".equals(element.getTagName())) {
                String id = element.getAttribute("id");
                if (id.length() == 0) {
                    continue;
                }
                _elements.put(id, element);
                if (element.hasAttribute("code")) {
                    _listOfCodes.add(element);
                }
                else {
                    _listOfCompoundFilters.add(element);
                }
            }
            else if ("filter-map".equals(element.getTagName())) {
                _listOfFilterMaps.add(element);
                _mapElements.put(element.getAttribute("id"), element);
            }
        }
    }

    /**
     * ?????????? ??? ????????? ???????? ???????? filtersNode.
     */
    public FilterCollections parseCollections() throws FilterException {
        _collections = new FilterCollections();
        for (int i = 0; i < _listOfCompoundFilters.size(); i++) {
            parseFilter((Element) _listOfCompoundFilters.get(i));
        }
        return _collections;
    }

    /**
     * ?????????? ??? ???? ???????? ???????? filtersNode.
     */
    public FilterCodes parseCodes() {
        _codes = new FilterCodes();
        Filter filter = null;
        for (int i = 0; i < _listOfCodes.size(); i++) {
            Element filterElement = (Element) _listOfCodes.get(i);
            String id = filterElement.getAttribute("id");
            if (id.length() == 0) {
                continue;
            }
            String code = filterElement.getAttribute("code");
            String formatIn = filterElement.hasAttribute("format-in") ? filterElement.getAttribute("format-in") : null;
            String formatOut = filterElement.hasAttribute("format-out") ? filterElement.getAttribute("format-out") : null;
            try {
                filter = Filter.create(code);
            } catch (FilterException e) {
                TLogger.warning(FilterParser.class, "Cannot load filterElement code '" + code + "'");
                continue;
            }
            _codes.put(id, new FilterCode(id, filter, parseFilterParams(filterElement), formatIn, formatOut));
        }
        return _codes;
    }

    private FilterParams parseFilterParams(Element filterElement) {
        NodeList paramList = filterElement.getChildNodes();
        FilterParams params = new FilterParams();
        for (int j = 0; j < paramList.getLength(); j++) {
            Element param = XmlUtils.checkIfElement(paramList.item(j), "param");
            if (param == null) {
                continue;
            }
            if (param.hasAttribute("name") && param.hasAttribute("value")) {
                params.put(param.getAttribute("name"), param.getAttribute("value"));
            }
        }
        return params;
    }

    /**
     * ?????? ???????? <filter-map> ???????? filterNode.
     */
    private void parseMaps(DBConfig dbc) throws DBConfigException, FilterException {
        for (int i = 0; i < _listOfFilterMaps.size(); i++) {
            Element e = (Element) _listOfFilterMaps.get(i);
            parseMap(e, dbc);
        }
    }

    /**
     * ?????? ??????? <filter-map> ???????? e.
     */
    private void parseMap(Element e, DBConfig dbc) throws DBConfigException, FilterException {
        String id = e.getAttribute("id");
        checkFilterMapId(id);

        FilterMap fm = extendFilterMap(e, dbc, id);

        NodeList list = e.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Element ae = XmlUtils.checkIfElement(list.item(i), "attr");
	    if (ae == null) {
	        continue;
	    }

            FilterCollection input = getFilterCollection(ae, INPUT_FILTER);
            FilterCollection output = getFilterCollection(ae, OUTPUT_FILTER);

            Collection matchedAttributes = null;
            try {
                matchedAttributes = dbc.matchAttributes(getMatch(ae));
            } catch (XPathExpressionException e1) {
                throw new FilterException(FilterException.FILTER_ERROR, e1);
            }
            for (Iterator ai = matchedAttributes.iterator(); ai.hasNext();) {
                ObjectAttr a = (ObjectAttr) ai.next();
                if (input != null) {
                    fm.setInputFilterCollection(a.getObjectName(), a.getName(), input);
                }
                if (output != null) {
                    fm.setOutputFilterCollection(a.getObjectName(), a.getName(), output);
                }
            }
        }
        _maps.put(id, fm);
    }

    private FilterCollection getFilterCollection(Element ae, String what) throws FilterException {
        FilterCollection result = null;
        if (ae.hasAttribute(what)) {
            String filterId = ae.getAttribute(what);
            if (filterId.length() > 0) {
                if (_collections.containsKey(filterId)) {
                    result = _collections.get(filterId);
                } else if (_codes.containsKey(filterId)) {
                    FilterCode code = _codes.get(filterId);
                    result = new FilterCollection(filterId, code.getFormatIn(), code.getFormatOut());
                    result.add(code);
                } else {
                    throw new FilterException(FilterException.FILTER_ERROR, "Filter '" + filterId + "' not found");
                }
            } else {
                throw new FilterException(FilterException.FILTER_ERROR, "Value of '" + what + "' attribute cannot be empty");
            }
        }
        return result;
    }

    private String getMatch(Element ae) throws FilterException {
        String match = ae.getAttribute("match");
        if (match.length() == 0) {
            throw new FilterException(FilterException.FILTER_ERROR, "Atrribute match of filter-map '" + (((Element) ae.getParentNode()).getAttribute("id")) + "' cannot be empty");
        }
        return match;
    }

    private FilterMap extendFilterMap(Element e, DBConfig dbc, String id) throws DBConfigException, FilterException {
        String ext = e.getAttribute("extends");
        FilterMap extmap = null;
        if (ext.length() > 0) {
            if (ext.equals(FilterMap.DEFAULT)) {
                _maps.put(FilterMap.DEFAULT, dbc.getFilterMap());
            } else {
                if (_maps.get(ext) == null) {
                    Element exte = (Element) _mapElements.get(ext);
                    if (exte != null) {
                        parseMap(exte, dbc);
                    } else {
                        throw new FilterException(FilterException.FILTER_ERROR, "Filter map '" + ext + "' not found");
                    }
                }
            }
            extmap = _maps.get(ext);
        }
        FilterMap fm = null;
        if (extmap == null) {
            fm = new FilterMap(id);
        } else {
            fm = extmap.copy();
            fm.setId(id);
        }
        return fm;
    }

    private void checkFilterMapId(String id) throws FilterException {
        if (id.length() == 0) {
            throw new FilterException(FilterException.FILTER_ERROR, "Element filter-map cannot contain empty id");
        }
        if (_maps.get(id) != null) {
            throw new FilterException(FilterException.FILTER_ERROR, "Element filter-map with id='" + id + "' already exists");
        }
    }

    /**
     * ???? ???????? ?????? ? filtersNode ? ?????? ???.
     */
    private void findAndParseFilter(String id) throws FilterException {
        Element e = (Element) _elements.get(id);
        if (e != null) {
            parseFilter(e);
        }
    }

    /**
     * ???????????? ??????? <filter> ???????? n.
     */
    private void processFilter(Element n, FilterCollection fc) throws FilterException {
        String id = n.getAttribute("id");
        if (_codes.containsKey(id)) {
            fc.add(_codes.get(id));
        } else {
            if (!_collections.containsKey(id)) {
                findAndParseFilter(id);
            }
            if (_collections.containsKey(id)) {
                fc.addAll(_collections.get(id));
            } else {
                throw new FilterException(FilterException.FILTER_ERROR, "Filter '" + id + "' not found");
            }
        }
    }

    /**
     * ?????? ??????? <filter> ???????? filter.
     */
    private void parseFilter(Element filter) throws FilterException {
        String id = filter.getAttribute("id");
        if (_collections.get(id) != null) {
            return;
        }
        String formatIn = filter.hasAttribute("format-in") ? filter.getAttribute("format-in") : null;
        String formatOut = filter.hasAttribute("format-out") ? filter.getAttribute("format-out") : null;
        FilterCollection fc = new FilterCollection(id, formatIn, formatOut);
        NodeList list = filter.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Element e = XmlUtils.checkIfElement(list.item(i), "filter");
            if (e == null) {
                continue;
            }
            processFilter(e, fc);
        }
        if (fc.size() == 0) {
            throw new FilterException(FilterException.FILTER_ERROR, "Ambiguous filter declaration: " + XmlUtils.elementToString(filter));
        }
        _collections.put(id, fc);
    }

}

