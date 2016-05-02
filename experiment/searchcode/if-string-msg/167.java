/*
 * Copyright 2011 DeepDiff Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package deepdiff.app;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import deepdiff.core.ConfigProperty;
import deepdiff.core.Configurable;
import deepdiff.core.DiffPointProcessor;
import deepdiff.core.DiffPointProcessorFactory;
import deepdiff.core.DiffScope;
import deepdiff.core.DiffUnitProcessor;
import deepdiff.core.DiffUnitProcessorFactory;
import deepdiff.core.IllegalConfigException;

/**
 * Parses an XML config file using SAX. Parsing a config file results in new objects available via
 * {@link DiffUnitProcessorFactory}, {@link DiffPointProcessorFactory}, and {@link #getScopes()}.
 */
class ConfigHandler extends DefaultHandler {
    private static final String ELEM_CONFIG = "config";
    private static final String ELEM_POINT_PROCESSOR = "point-processor";
    private static final String ELEM_PROPERTY = "property";
    private static final String ELEM_SCOPE = "scope";
    private static final String ELEM_UNIT_PROCESSOR = "unit-processor";

    private static final String ATTR_CLASS = "class";
    private static final String ATTR_ID = "id";
    private static final String ATTR_LEFT = "left";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_RIGHT = "right";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_VALUE = "value";

    private static final Logger log = Logger.getLogger(ConfigHandler.class);

    /**
     * The objects currently being processed. If processing is underway, the bottom object will be a
     * List of scopes representing the config itself. The top object will be the last contextual
     * object processed. Thus, any configuration elements apply to the top element.
     */
    private Stack<Object> contexts = new Stack<Object>();

    /**
     * The instances of {@link Configurable} that have been processed. This is maintained so that
     * {@link Configurable#validateProperties()} can be called during {@link #endDocument()}.
     */
    private Set<Configurable> configurables = new HashSet<Configurable>();

    /** The instances of {@link DiffScope} that have been configured. */
    private Collection<DiffScope> scopes = new LinkedList<DiffScope>();

    /**
     * Processes the start of an element
     * 
     * @param uri the namespace URI for the element
     * @param localName the local name for the element
     * @param qName the qualified name for the element
     * @param attributes the attributes for the element
     * 
     * @throws SAXException if there's an exception processing the element
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (ELEM_SCOPE.equals(qName)) {
            String leftPath = attributes.getValue(ATTR_LEFT);
            String rightPath = attributes.getValue(ATTR_RIGHT);
            File leftFile = new File(leftPath);
            File rightFile = new File(rightPath);
            DiffScope scope = DiffUnitProcessorFactory.createInitialScope(leftFile, rightFile);
            Object parent = contexts.peek();
            if (!(parent instanceof List<?>)) {
                throw new SAXException("Cannot process a " + ELEM_SCOPE
                        + " element at this point, parent is not a " + ELEM_CONFIG);
            }
            scopes.add(scope);
        } else if (ELEM_UNIT_PROCESSOR.equals(qName)) {
            String className = attributes.getValue(ATTR_CLASS);
            String id = attributes.getValue(ATTR_ID);
            try {
                DiffUnitProcessor unitProcessor = DiffUnitProcessorFactory.instantiate(className,
                        id);
                pushContext(unitProcessor);
            } catch (ClassNotFoundException cnfe) {
                String msg = "Could not load class " + className;
                throw new SAXException(msg, cnfe);
            } catch (InstantiationException ie) {
                String msg = className + " cannot be instantiated";
                throw new SAXException(msg, ie);
            } catch (IllegalAccessException iae) {
                String msg = className + " is not accessible";
                throw new SAXException(msg, iae);
            }
        } else if (ELEM_POINT_PROCESSOR.equals(qName)) {
            String className = attributes.getValue(ATTR_CLASS);
            String id = attributes.getValue(ATTR_ID);
            try {
                DiffPointProcessor pointProcessor = DiffPointProcessorFactory.instantiate(
                        className, id);
                pushContext(pointProcessor);
            } catch (ClassNotFoundException cnfe) {
                String msg = "Could not load class " + className;
                throw new SAXException(msg, cnfe);
            } catch (InstantiationException ie) {
                String msg = className + " cannot be instantiated";
                throw new SAXException(msg, ie);
            } catch (IllegalAccessException iae) {
                String msg = className + " is not accessible";
                throw new SAXException(msg, iae);
            }
        } else if (ELEM_CONFIG.equals(qName)) {
            if (!contexts.isEmpty()) {
                String msg = "The " + ELEM_CONFIG + " element must be the root of the document";
                throw new SAXException(msg);
            }
            pushContext(scopes);
        } else if (ELEM_PROPERTY.equals(qName)) {
            String type = attributes.getValue(ATTR_TYPE);
            String name = attributes.getValue(ATTR_NAME);
            String value = attributes.getValue(ATTR_VALUE);
            ConfigProperty property = new ConfigProperty(type, name, value);
            if (contexts.isEmpty()) {
                throw new SAXException("Cannot process a " + ELEM_PROPERTY
                        + " element at this point, no configurable parent element");
            }
            Object parent = contexts.peek();
            String className = parent.getClass().getName();
            if (parent instanceof Configurable) {
                Configurable configurable = (Configurable) parent;
                try {
                    configurable.addProperty(property);
                } catch (IllegalConfigException ice) {
                    throw new SAXException("Property denied: " + property, ice);
                }
            } else {
                throw new SAXException(className + " is not configurable");
            }
        } else {
            log.warn("Unhandled startElement: " + qName);
        }
    }

    /**
     * Processes the end of an element
     * 
     * @param uri the namespace URI for the element
     * @param localName the local name for the element
     * @param qName the qualified name for the element
     * 
     * @throws SAXException if there's an exception processing the element
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ELEM_PROPERTY.equals(qName) || ELEM_SCOPE.equals(qName)) {
            // Ignore empty elements
        } else if (ELEM_UNIT_PROCESSOR.equals(qName) || ELEM_POINT_PROCESSOR.equals(qName)
                || ELEM_CONFIG.equals(qName)) {
            // End contextual elements
            contexts.pop();
        } else {
            log.warn("Unhandled endElement: " + qName);
        }
    }

    /**
     * Processes the end of a document
     * 
     * @throws SAXException if there's an exception processing the document
     */
    public void endDocument() throws SAXException {
        for (Iterator<Configurable> it = configurables.iterator(); it.hasNext();) {
            Configurable configurable = it.next();
            try {
                configurable.validateProperties();
            } catch (IllegalConfigException ice) {
                throw new SAXException("Invalid component configuration", ice);
            }
        }
    }

    /**
     * Returns a unmodifiable Collection of {@link DiffScope}s.
     * 
     * @return a unmodifiable Collection of {@link DiffScope}s.
     */
    public Collection<DiffScope> getScopes() {
        return Collections.unmodifiableCollection(scopes);
    }

    /**
     * Pushes the specified context on to the context stack. If the object is {@link Configurable},
     * it will also be added to {@link #configurables}.
     * 
     * @param context the context to push on to the context stack
     */
    private void pushContext(Object context) {
        if (context instanceof Configurable) {
            configurables.add((Configurable) context);
        }
        contexts.push(context);
    }
}

