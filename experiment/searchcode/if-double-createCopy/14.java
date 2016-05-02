package nl.tudelft.lime.xml;

import nl.tudelft.lime.constants.SystemSettings;
import nl.tudelft.lime.model.LimeElement;
import nl.tudelft.lime.model.LimeSubpart;
import nl.tudelft.lime.model.codefragment.LimeCodeFragment;
import nl.tudelft.lime.model.component.LimeComponent;
import nl.tudelft.lime.model.component.LimeDiagram;
import nl.tudelft.lime.model.connection.Arc;
import nl.tudelft.lime.model.connection.Association;
import nl.tudelft.lime.model.port.InputPort;
import nl.tudelft.lime.model.port.InputPortTrap;
import nl.tudelft.lime.model.port.OutputPort;
import nl.tudelft.lime.model.port.Port;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultElement;

import org.dom4j.util.XMLErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * XML factory used to create XML document from the diagram
 *
 * @author mazaninfardi
 *
 */
public class XMLFactory {
    /**
     * <b>Note</b> <br>
     * The assumption is that all the elements inside each component have unique
     * names in that component, with this meaning that they have distinguishable
     * parentChildPath values, which holds for their IDs (e.g. if we have a
     * port1 in Component1, we have only one port with name port1 in Component1
     * 'Component1:port1')
     * <p>
     * Creating the diagram from the XML file is a two step process,
     * <ul>
     * <li>Create the diagram without arcs. Because references for the arcs can
     * be to any item in the diagram, and cannot be created before creation of
     * all nodes. In this phase a reference to all elements which are involved
     * in connections should be kept</li>
     * <li>In the second phase, arcs can be drawn based on the &lt;endpoint&gt;
     * elements. This is almost a straightforward process, if you keep the map
     * between the parentChildPath to the object references</li>
     * </ul>
     *
     * @param is
     * @throws DocumentException
     * @throws IOException
     * @throws XMLFormatNotCorrectException
     */
    public static LimeDiagram getDiagramForXML(InputStream is)
        throws DocumentException, IOException, XMLFormatNotCorrectException {
        SAXReader xmlReader = new SAXReader();
        xmlReader.setEntityResolver(new CustomEntityResolver());

        // add error handler which turns any errors into XML
        XMLErrorHandler errorHandler = new XMLErrorHandler();
        xmlReader.setErrorHandler(errorHandler);

        Document doc = xmlReader.read(is);

        // if the document is not correct, it should be handled by the caller
        // class
        if ((doc.getRootElement() == null) ||
                !SystemSettings.GXF_TAG.equals(doc.getRootElement().getName())) {
            throw new XMLFormatNotCorrectException();
        }

        // two phase process of creating the diagram,
        // first create all nodes, and all references needed, usually it is a
        // map from parentChildPath to the object
        // reference
        // but in the same phase keep track of all the associations in the diagram,
        // e.g. : Component1::port1 -> Port1@2345EA35
        LimeDiagram diagram = (LimeDiagram) getDiagram(doc.getRootElement());
        //it can be only made when all the nodes are created
        addAssociations(doc.getRootElement(), diagram);

        // LimeDiagram diagram = new LimeDiagram();
        // second, add the arcs to the diagram, this is a faster process
        // TODO: this can be called full of side effects, since it works with
        // references
        // it is wise to enhance it if needed
        return diagram;
    }

    /**
     * Saves the XML version of the diagram.
     *
     * @param os
     *            output stream
     * @throws IOException
     *             if output stream cannot be created
     */
    public static void writeXMLDiagramToOutputStream(OutputStream os,
        LimeDiagram limeDiagram) throws IOException {
        // lets write to a file
        // XMLWriter writer = new XMLWriter(new FileWriter(path));

        // Pretty print the document to System.out
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(os, format);
        writer.write(XMLFactory.getXMLForDiagram(limeDiagram));

        writer.close();
    }

    /**
     * In this methods arcs should be made between ports in the diagram. It
     * starts with all the &lt;edge&gt; elements and using their 'xlink:from'
     * and 'xlink:to' attribute and the ids they store, finds the appropriate
     * ports from the portDictionary and then adds the arc to them
     *
     * @param Element
     * @param arcsPortDictionary it should only contain ports which are in the lime components inside the element
     */
    @SuppressWarnings("unchecked")
    private static void addArcs(Element element,
        final Map<String, Port> arcsPortDictionary) {
        // configure component specific fields
        // if it is the root element or it is a node, then it might have some
        // edge elements
        if (SystemSettings.GXF_TAG.equals(element.getName()) ||
                SystemSettings.NODE_TAG.equals(element.getName())) {
            // for these elements first create all the arcs inside them, and
            // then
            // call the same for their children
            Element edges = element.element(SystemSettings.EDGES_TAG);

            if (edges != null) {
                List<Element> arcs = (List<Element>) edges.elements(SystemSettings.EDGE_TAG);

                for (Element arcElement : arcs) {
                    try {
                        Arc arc = new Arc();
                        String fromOutput = "";
                        String toInput = "";
                        arc.setArcType(arcElement.element(
                                SystemSettings.TYPE_TAG).getText());

                        // use QName to resolve xlink attributes, but it means
                        // defining a
                        // variable for the namespace, to keep it simple we keep it
                        // as below

                        //TODO: the convention should be kept in one place...
                        //now the convention is to use ${node's name}:${port's name}
                        Element fromElement = arcElement.element(SystemSettings.FROM_NODE_TAG);

                        if (fromElement != null) {
                            fromOutput = fromElement.elementText(SystemSettings.ID_TAG) +
                                ":" +
                                fromElement.elementText(SystemSettings.PORT_ID_TAG);
                        } else {
                            fromElement = arcElement.element(SystemSettings.FROM_TAG);
                            fromOutput = fromElement.element(SystemSettings.NODE_TAG)
                                                    .elementText(SystemSettings.ID_TAG) +
                                ":" +
                                fromElement.element(SystemSettings.NODE_TAG)
                                           .element(SystemSettings.PORT_TAG)
                                           .elementText(SystemSettings.ID_TAG);
                        }

                        if ((fromOutput == null) || fromOutput.equals("")) {
                            throw new XMLFormatNotCorrectException();
                        }

                        // this is a non-safe way to map, but since the XML is
                        // not well-formatted for this part
                        // we have to continue doing this
                        // there are two double-dot columns we have to ignore in
                        // our indexing
                        //fromOutput = fromOutput.substring(2);

                        // if the attribute is empty get edge -> node -> port ->
                        // id instead
                        Element toElement = arcElement.element(SystemSettings.TO_NODE_TAG);

                        if (toElement != null) {
                            toInput = toElement.elementText(SystemSettings.ID_TAG) +
                                ":" +
                                toElement.elementText(SystemSettings.PORT_ID_TAG);
                        } else {
                            toElement = arcElement.element(SystemSettings.TO_TAG);
                            toInput = toElement.element(SystemSettings.NODE_TAG)
                                               .elementText(SystemSettings.ID_TAG) +
                                ":" +
                                toElement.element(SystemSettings.NODE_TAG)
                                         .element(SystemSettings.PORT_TAG)
                                         .elementText(SystemSettings.ID_TAG);
                        }

                        if ((toInput == null) || toInput.equals("")) {
                            throw new XMLFormatNotCorrectException();
                        }

                        Port port1 = (Port) arcsPortDictionary.get(fromOutput);
                        Port port2 = (Port) arcsPortDictionary.get(toInput);

                        if ((port1 != null) && (port2 != null)) {
                            arc.setSource(port1);
                            arc.setTarget(port2);
                            arc.setTargetTerminal(Port.CONNECTION_TERMINAL);
                            arc.setSourceTerminal(Port.CONNECTION_TERMINAL);
                            arc.attachSource();
                            arc.attachTarget();
                        }
                    } catch (Exception e) {
                        // ignored one arc
                        System.err.println(arcElement.asXML());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * In this method associations are created between ports in the diagram. It
     * starts with all the &lt;bound-to&gt; elements and using their 'xlink:from'
     * and 'xlink:to' attribute and the ids they store, finds the appropriate
     * ports from the portDictionary and then adds the arc to them
     * TODO: this should be optimized... on one hand keeping a dictionary for the associations doesn't seem to be a good solution
     * on the other hand this ways is not the best way
     *
     * @param Element
     * @param arcsPortDictionary it should only contain ports which are in the lime components inside the element
     */
    @SuppressWarnings("unchecked")
    private static void addAssociations(Element element, LimeDiagram diagram) {
        List<Element> associations = (List<Element>) element.elements(
                "bound-to");

        //2. now for the associations do the same procedure
        for (Element arcElement : associations) {
            try {
                Association association = new Association();
                String fromPort = arcElement.attribute(SystemSettings.XLINK_FROM_ATTRIBUTE)
                                            .getValue();
                String toPort = arcElement.attribute(SystemSettings.XLINK_TO_ATTRIBUTE)
                                          .getValue();

                // use QName to resolve xlink attributes, but it means
                // defining a
                // variable for the namespace, to keep it simple we keep it
                // as below
                Port port1 = findPort(diagram, fromPort);
                Port port2 = findPort(diagram, toPort);

                if ((port1 != null) && (port2 != null)) {
                    association.createAssociation(port1, port2);
                }
            } catch (Exception e) {
                System.err.println(arcElement.asXML());
                e.printStackTrace();
            }
        }

        //do it for its children
        List<Element> nodes = (List<Element>) element.elements(SystemSettings.NODE_TAG);

        for (Element node : nodes) {
            addAssociations(node, diagram);
        }
    }

    /**
     * Find the port based on the portParentChildPath it receives in a diagram
     * @param diagram
     * @param portParentChildPath
     * @return
     */
    private static Port findPort(LimeDiagram diagram, String portParentChildPath) {
        List<LimeElement> children = diagram.getChildren();
        boolean lastStep = false;
        String lookfor = portParentChildPath;

        if (portParentChildPath.indexOf(":") == -1) {
            lastStep = true;
        } else {
            lookfor = portParentChildPath.substring(0,
                    portParentChildPath.indexOf(":"));
        }

        for (LimeElement child : children) {
            //if it is lastStep it should look for the port
            if (lastStep && (child instanceof Port) &&
                    ((Port) child).getName().equals(lookfor)) {
                return (Port) child;
            }

            if (!lastStep && (child instanceof LimeComponent) &&
                    ((LimeComponent) child).getName().equals(lookfor)) {
                return findPort((LimeDiagram) child,
                    portParentChildPath.substring(portParentChildPath.indexOf(
                            ":") + 1));
            }

            //else ignore other children
        }

        //the worst case!
        return null;
    }

    /**
    * Using
    * {@link XMLFactory#createXML(nl.tudelft.lime.model.LimeElement, String)}
    * creates equivalent XML document for the diagram
    *
    * @return XML document
    */
    public static Document getXMLForDiagram(LimeDiagram limeDiagram) {
        Document xmlDocument = DocumentHelper.createDocument();
        // QName rootName = DocumentFactory.getInstance().createQName("gxf", "",
        // "http://www.w3.org/1999/xlink");
        // Element root = DocumentFactory.getInstance().createElement(rootName);
        // xmlDocument.add(root);
        xmlDocument.addDocType("gxf", "", SystemSettings.GXF_URL);

        try {
            xmlDocument.add(getXML(limeDiagram));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlDocument;
    }

    /**
     * Creates a diagram based on the XML elements it receives, and adds its corresponding
     * arcs and assocations
     * <br>
     * Steps:
     * <ul>
     * <li>Create all the elements (ports and nodes), recursively</li>
     * <li>add arcs and associations for the node</li>
     * </ul>
     *
     * @param element
     * @param associationsFlatDictionary this is a dictioanry used to draw associations between ports at the end
     * it is called flat since its saved with flat ids node1:node2:port1
     * @return XML representation of element
     */
    @SuppressWarnings("unchecked")
    public static LimeSubpart getDiagram(Element element)
        throws IllegalArgumentException {
        //port dictionary holds the information for the visible ports for this node
        //visible ports are the ones which edges can use them to connect components together

        //--------------------------------------------------------------------------------
        //1. Create the elements
        //--------------------------------------------------------------------------------
        LimeSubpart subpart = createLimeSubpart(element);

        if (subpart != null) {
            // if it is a port or a node then configure the default fields
            if (subpart instanceof LimeComponent || subpart instanceof Port) {
                createDefaultFromXML(subpart, element);
            }

            //then if it is a port tag
            // configure port specific fields
            if (subpart instanceof Port) {
                Port port = (Port) subpart;
                createPortFromXML(port, element);

                return port;

                //portDictionary.put(portId, port);
            }

            // configure component specific fields
            if (subpart instanceof LimeDiagram) {
                //portdictionary should contain port information of this component's children
                //then it can draw edges between them
                //so first empy portDictionary for its own usage
                if (subpart instanceof LimeComponent) {
                    createComponentFromXML((LimeComponent) subpart, element);
                }

                // only get nodes - nodes are the ones which should be traversed
                List<Element> children = (List<Element>) element.elements(SystemSettings.NODE_TAG);

                for (Element child : children) {
                    // add parent child relationship to the items
                    LimeSubpart childSubpart = getDiagram(child);
                    childSubpart.setParent((LimeDiagram) subpart);

                    boolean isComponentInstance = (childSubpart instanceof LimeComponent) &&
                        ((LimeComponent) childSubpart).isInstance();
                    ((LimeDiagram) subpart).addChild(childSubpart,
                        isComponentInstance);

                    if ((subpart instanceof LimeComponent) &&
                            ((LimeComponent) subpart).isInstance() &&
                            (childSubpart instanceof LimeComponent)) {
                        ((LimeComponent) childSubpart).setInstance(true);
                    }

                    if (isComponentInstance) {
                        //this is needed to refresh
                        LimeComponent component = (LimeComponent) childSubpart;
                        component.setComponentClass(component.getComponentClass());
                    }
                }

                //for port children just add them
                List<Element> portChildren = (List<Element>) element.elements(SystemSettings.PORT_TAG);

                for (Element child : portChildren) {
                    //it is obviously getting a port and obviously it doesn't need to fill in associations
                    LimeSubpart childSubpart = getDiagram(child);
                    //now set the port's parent
                    childSubpart.setParent((LimeDiagram) subpart);

                    //and add it to the component
                    ((LimeDiagram) subpart).addChild(childSubpart);
                }

                //when every child of me is created, then add arcs and associations between them but before that create a 
                //dictionary holding String->Port values for ports in my component children

                //now build the dictionary
                Map<String, Port> portDictionary = buildArcDictionary(subpart);
                addArcs(element, portDictionary);
                //then create the arcs
                portDictionary.clear();
            }
        }

        //--------------------------------------------------------------------------------
        //2. Add associations and arcs
        //--------------------------------------------------------------------------------
        return subpart;
    }

    /**
     * Create port dictionary which is indeed
     * @return
     */
    private static Map<String, Port> buildArcDictionary(LimeSubpart subpart) {
        Map<String, Port> portDictionary = new HashMap<String, Port>();

        for (LimeElement myComponents : ((LimeDiagram) subpart).getChildren()) {
            //if the element is a limeComponet get all its ports and add them to the dictionary
            if (myComponents instanceof LimeComponent) {
                for (LimeElement componentElement : ((LimeComponent) myComponents).getChildren()) {
                    if (componentElement instanceof Port) {
                        Port port = (Port) componentElement;

                        //both the port and all its instances should be put on portDictionary
                        List<LimeSubpart> instances = port.getInstances();

                        for (LimeSubpart instance : instances) {
                            Port portInstance = (Port) instance;
                            String portId = portInstance.getParent().getName() +
                                ":" + portInstance.getName();
                            portDictionary.put(portId, portInstance);
                        }

                        String portId = port.getParent().getName() + ":" +
                            port.getName();
                        portDictionary.put(portId, port);
                    }
                }
            }
        }

        return portDictionary;
    }

    /**
     * Creates default element XML for nodes
     * @param component a LIME component
     * @param element
     */
    @SuppressWarnings("unchecked")
    private static void createComponentFromXML(LimeComponent limeComponent,
        Element element) {
        for (Element subElement : (List<Element>) element.elements()) {
            if (subElement.getName().equals(SystemSettings.TYPE_TAG)) {
                limeComponent.setType(subElement.getText());
            } else if (subElement.getName().equals(SystemSettings.RET_TAG)) {
                limeComponent.setComponentRet(subElement.getText());
            } else if (subElement.getName().equals(SystemSettings.STEREOTYPE_TAG)) {
                limeComponent.setInstance(true);
                //do not set component here, still it is not added to its parent
                //we need its parent to find the diagram and find corresponding elemnt
                limeComponent.setComponentClassText(subElement.getText());
            }
        }
    }

    /**
     * Creates default element XML for ports and nodes
     * @param subpart either a port or a component
     * @param element
     */
    @SuppressWarnings("unchecked")
    private static void createDefaultFromXML(LimeSubpart subpart,
        Element element) {
        subpart.setName(element.element(SystemSettings.ID_TAG).getText());

        List<Element> metaElements = element.elements(SystemSettings.META_TAG);

        //for meta elements only two elements should be treated in different way
        //1. meta tag with id = "visual" : which determines size and location of a LimeElement
        //2. meta tag with id = "passthrough" : which would be the code part
        for (Element metaElement : metaElements) {
            //if it is the first case
            if (((metaElement.attribute(SystemSettings.ID_ATTRIBUTE) != null) &&
                    metaElement.attributeValue(SystemSettings.ID_ATTRIBUTE)
                                   .equals(SystemSettings.META_TAG_VISUAL_ID)) ||
                    ((metaElement.element(SystemSettings.ID_TAG) != null) &&
                    metaElement.elementText(SystemSettings.ID_TAG)
                                   .equals(SystemSettings.META_TAG_VISUAL_ID))) {
                subpart.setSizeByMap(getDimensionsListMap(metaElement.element(
                            SystemSettings.SIZE_TAG)));
                subpart.setLocationByMap(getDimensionsListMap(
                        metaElement.element(SystemSettings.LOCATION_TAG)));
            }
            //if it is a code fragment
            else if ((subpart instanceof LimeComponent &&
                    ((metaElement.attribute(SystemSettings.ID_ATTRIBUTE) != null) &&
                    metaElement.attributeValue(SystemSettings.ID_ATTRIBUTE)
                                   .equals(SystemSettings.META_TAG_CODE_ID))) ||
                    ((metaElement.element(SystemSettings.ID_TAG) != null) &&
                    metaElement.elementText(SystemSettings.ID_TAG)
                                   .equals(SystemSettings.META_TAG_CODE_ID))) {
                //create a label and make it as a child of this component
                List<Element> codeElements = metaElement.elements(SystemSettings.CODE_TAG);

                for (Element codeElement : codeElements) {
                    LimeCodeFragment codeFragment = new LimeCodeFragment();
                    codeFragment.setLabelContents((String) codeElement.getData());

                    if (codeElement.attribute(SystemSettings.CONTEXT_TAG) != null) {
                        codeFragment.setContext((String) codeElement.attributeValue(
                                SystemSettings.CONTEXT_TAG));
                    } else {
                        codeFragment.setContext((String) codeElement.elementText(
                                SystemSettings.CONTEXT_TAG));
                    }

                    ((LimeComponent) subpart).addChild(codeFragment);

                    //if it has size and location read it
                    Element contextSizeAndLocation = codeElement.element(SystemSettings.META_TAG);

                    if ((contextSizeAndLocation != null) &&
                            (contextSizeAndLocation.attribute(
                                SystemSettings.ID_ATTRIBUTE) != null) &&
                            contextSizeAndLocation.attributeValue(
                                SystemSettings.ID_ATTRIBUTE)
                                                      .equals(SystemSettings.META_TAG_VISUAL_ID)) {
                        codeFragment.setSizeByMap(getDimensionsListMap(
                                contextSizeAndLocation.element(
                                    SystemSettings.SIZE_TAG)));
                        codeFragment.setLocationByMap(getDimensionsListMap(
                                contextSizeAndLocation.element(
                                    SystemSettings.LOCATION_TAG)));
                    }

                    //in both cases read location and size
                }
            }
            //if it is an unknown tag
            else {
                subpart.getPreservedXMLElements().add(metaElement.createCopy());
            }
        }
    }

    /**
     * Creates port related XML
     * @param port
     * @param element
     * @return portId
     */
    @SuppressWarnings("unchecked")
    private static String createPortFromXML(Port port, Element element) {
        port.setPortSize("");

        for (Element subElement : (List<Element>) element.elements()) {
            if (subElement.getName().equals(SystemSettings.TYPE_TAG)) {
                //two cases happen here based on the definition of LIME
                //1. if the element is like this <type id="something"><id>something_else<id></type>
                //2. there is only a text value for type <type>text</type> like

                // in the first case add type to embedded XMLs
                if (subElement.attribute(SystemSettings.ID_ATTRIBUTE) != null) {
                    port.getPreservedXMLElements().add(subElement.createCopy());
                }
                //otherwise keep it as a property of the port and show it in the visual editor in the 'properties' view
                else {
                    port.setType(subElement.getStringValue());
                }
            } else if (subElement.getName().equals(SystemSettings.CONSTP_TAG)) {
                port.setPortConstp(Port.TRUE);
            } else if (subElement.getName().equals(SystemSettings.RESTRICT_TAG)) {
                port.setPortRestrict(Port.TRUE);
            } else if (subElement.getName().equals(SystemSettings.STATIC_TAG)) {
                port.setPortStatic(Port.TRUE);
            }
            //if size is not defined it is set as default
            else if (subElement.getName().equals(SystemSettings.SIZE_TAG)) {
                String elementSize = "[" + subElement.getText() + "]";
                //append the text to the other elements
                port.setPortSize(port.getPortSize() + elementSize);
            } else if (subElement.getName().equals(SystemSettings.VOLATILE_TAG)) {
                port.setPortVolatile(Port.TRUE);
            } else if (subElement.getName().equals(SystemSettings.VOLATILEP_TAG)) {
                port.setPortVolatilep(Port.TRUE);
            } else if (!isNormalPortTag(subElement.getName())) {
                port.getPreservedXMLElements().add(subElement.createCopy());
            }
        }

        //previously portId could be obtained from id attribute 
        String portId = element.attribute(SystemSettings.ID_ATTRIBUTE).getValue();

        //TODO:but now it is should be created from the hierarchy of the port
        //???
        return portId;

        // to the map ports should be added
    }

    private static boolean isNormalPortTag(String name) {
        return name.equals(SystemSettings.LOCATION_TAG) ||
        name.equals(SystemSettings.SIZE_TAG) ||
        name.equals(SystemSettings.META_TAG) ||
        name.equals(SystemSettings.DUMMY_INTRA_ARCS_TAG);
    }

    /**
    * Creates a dimension map from (id, value) pairs of inner nodes of an XML
    * node which have the tage of dimension
    *
    * @param element
    * @return
    */
    @SuppressWarnings("unchecked")
    private static Map<String, Integer> getDimensionsListMap(Element element) {
        Map<String, Integer> dimensionMap = new HashMap<String, Integer>();

        if (element == null) {
            return dimensionMap;
        }

        List<Element> elements = (List<Element>) element.elements(SystemSettings.DIMENSION_TAG);

        for (Element dimension : elements) {
            dimensionMap.put(dimension.attribute(SystemSettings.ID_ATTRIBUTE)
                                      .getText(),
                Integer.valueOf(dimension.attribute(
                        SystemSettings.VALUE_ATTRIBUTE).getText()));
        }

        return dimensionMap;
    }

    private static LimeSubpart createLimeSubpart(Element element) {
        String name = element.getName();

        if (SystemSettings.GXF_TAG.equals(name)) {
            return new LimeDiagram();
        }

        if (SystemSettings.NODE_TAG.equals(name)) {
            return new LimeComponent();
        }

        if (SystemSettings.PORT_TAG.equals(name)) {
            // if it has 'const' modifier then it is an input port
            if (element.element("const") != null) {
                return new InputPort();
            }
            // otherwise it is an output port
            else {
                return new OutputPort();
            }
        }

        return null;
    }

    /**
     * @param part
     * @return equivalent string to the {@link LimeElement}
     */
    private static String getPartName(LimeElement part) {
        if (part instanceof LimeComponent) {
            return SystemSettings.NODE_TAG;
        } else if (part instanceof Arc) {
            return SystemSettings.EDGE_TAG;
        } else if (part instanceof Port) {
            return SystemSettings.PORT_TAG;
        } else if (part instanceof LimeDiagram) {
            return SystemSettings.GXF_TAG;
        }

        return "";
    }

    /**
     * to create each XML file, first add the default attributes, the returned
     * XML element is a root element containing elements which are needed to be
     * included in the resulting XML file, therefore, the topmost element should
     * be ignored in writing to file
     *
     * <b>DummyArcElements</b>are used to pull up arc hierarchy to the appropriate element.
     * Normally in the diagram arcs are a part of a port, but in the XML file, they should be
     * saved in either a port's parent or a port's grandparent
     * @param limePart
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Element getXML(LimeElement part) {
        LimeSubpart limeSubpart = (LimeSubpart) part;
        Element root = new DefaultElement(getPartName(limeSubpart));

        // if it is the diagram, we don't need to add anything special
        if (limeSubpart.getClass() != LimeDiagram.class) {
            addDefaultAttributes(root, limeSubpart);
        } else {
            root.add(new DefaultAttribute("xmlns:xlink",
                    "http://www.w3.org/1999/xlink"));
            root.add(new DefaultAttribute(SystemSettings.XLINK_TYPE_ATTRIBUTE,
                    "extended"));
        }

        // to be compatible with the example XML, we have to stick to a
        // rule,
        // we have to keep track of the items connected together, but
        // the problem arises when we have to keep the parent path for
        // if (limeSubpart.getConnections().size() != 0)
        // add component specific attributes
        if (limeSubpart instanceof LimeComponent) {
            addComponentAttributes(root, (LimeComponent) limeSubpart);
        } else if (limeSubpart instanceof Port) {
            addPortAttributes(root, (Port) limeSubpart);
        } // if it is a component or the whole diagram

        // add component/diagram embodied elements
        if (limeSubpart instanceof LimeDiagram) {
            // get the component or diagram
            LimeDiagram diagram = (LimeDiagram) limeSubpart;
            List<LimeElement> children = diagram.getChildren();

            //this variable is used to keep arcs and associations and add them when all the
            //child elements are added
            Element rootArcs = new DefaultElement(SystemSettings.DUMMY_ROOT_ARCS_TAG);
            Element rootAssociations = new DefaultElement(SystemSettings.DUMMY_ROOT_ASSOCIATIONS_TAG);
            Element rootArcsToBePulledUp = new DefaultElement(SystemSettings.DUMMY_INTRA_ARCS_TAG);

            for (LimeElement element : children) {
                //if it is a code fragment (label) ignore it,
                //it is earlier handled as a meta tag
                if (element instanceof LimeCodeFragment) {
                    continue;
                }

                Element item = getXML(element);
                //FURTHER WHEN arcs are created if it is an instance remove its children
                root.add(item);

                //if the interior ports had arcs, their component is responsible to handle them
                //
                //if both source and target of an arc are in a same component hierarchy, 
                //the arc should be added to source's parent
                //the exception is when both components are the in the same level 
                //like ports which are used for state variables of a node
                if (item.elements(SystemSettings.DUMMY_INTRA_ARCS_TAG).size() != 0) {
                    final Element dummyArc = (Element) (item.elements(SystemSettings.DUMMY_INTRA_ARCS_TAG)).get(0);

                    // in fact arc information is related to a port not to a
                    // container directly,
                    // but based on the XML examples, we have to keep it as this
                    // way
                    for (Element arc : (List<Element>) dummyArc.elements())
                        rootArcs.add(arc.createCopy());

                    item.remove(dummyArc);
                }

                //otherwise it should be added to its grand parent
                //(it is more readable to rewrite the above if statement for 
                // intercomponent arcs, otherwise it is not well readable)
                //to make it easy, we set inter_arcs as intra_arcs of the current element,
                //it causes it to be pulled up by its parent automatically
                if (item.elements(SystemSettings.DUMMY_INTER_ARCS_TAG).size() != 0) {
                    final Element dummyInterComponentArc = (Element) (item.elements(SystemSettings.DUMMY_INTER_ARCS_TAG)).get(0);

                    //pull up the contents one level more up if it is an inter-component arc
                    //(an arc between two component which are not in the same hierarchy)
                    for (Element arcElement : (List<Element>) dummyInterComponentArc.elements()) {
                        rootArcsToBePulledUp.add(arcElement.createCopy());
                    }

                    // in fact arc information is related to a port not to a
                    // container directly,
                    // but based on the XML examples, we have to keep it as this
                    // way
                    item.remove(dummyInterComponentArc);
                }

                if (item.elements(SystemSettings.DUMMY_ASSOCIATIONS_TAG).size() != 0) {
                    final Element dummyAssociation = (Element) (item.elements(SystemSettings.DUMMY_ASSOCIATIONS_TAG)).get(0);

                    // in fact arc information is related to a port in the object model not to a
                    // container directly,
                    // but based on the XML samples of LIME, we have to treat them in this
                    // way
                    for (Element arc : (List<Element>) dummyAssociation.elements())
                        rootAssociations.add(arc.createCopy());

                    item.remove(dummyAssociation);
                }

                //if it was an instance then clear its contents
                if ((part instanceof LimeComponent) &&
                        ((LimeComponent) part).isInstance()) {
                    root.remove(item);
                }
            }

            //edges and associations should be added after all the child nodes are added
            //first the associations - add the associations which correspond to this component
            if (rootAssociations.elements().size() != 0) {
                for (Element element : (List<Element>) rootAssociations.elements()) {
                    root.add(element.createCopy());
                }

                rootAssociations.clearContent();
            }

            //second the edges - add the arcs which correspond to this component
            if (rootArcs.elements().size() != 0) {
                Element edgesElement = new DefaultElement(SystemSettings.EDGES_TAG);

                for (Element element : (List<Element>) rootArcs.elements()) {
                    edgesElement.add(element.createCopy());
                }

                root.add(edgesElement);
                rootArcs.clearContent();
            }

            if (rootArcsToBePulledUp.elements().size() != 0) {
                root.add(rootArcsToBePulledUp);
            }
        }

        // add arcs if there are any
        Vector<Arc> intraComponentArcs = limeSubpart.getIntraComponentSourceArcs();
        Vector<Arc> interComponentArcs = limeSubpart.getInterComponentSourceArcs();

        // if there are any arcs from this element consider it
        // we keep the rule that we only write outgoing arcs,
        // so there are no duplicate entries
        if ((intraComponentArcs != null) && (intraComponentArcs.size() != 0)) {
            DefaultElement DUMMY_INTRA_ARC = new DefaultElement(SystemSettings.DUMMY_INTRA_ARCS_TAG);
            root.add(getArcsXML(DUMMY_INTRA_ARC, intraComponentArcs));
        }

        if ((interComponentArcs != null) && (interComponentArcs.size() != 0)) {
            DefaultElement DUMMY_INTER_ARC = new DefaultElement(SystemSettings.DUMMY_INTER_ARCS_TAG);
            root.add(getArcsXML(DUMMY_INTER_ARC, interComponentArcs));
        }

        // after adding arc to the XML it is the time to add associations
        Vector<Association> associations = limeSubpart.getSourceAssociations();

        // if there are any arcs from this element consider it
        // we keep the rule that we only write outgoing arcs,
        // so there are no duplicate entries
        if ((associations != null) && (associations.size() != 0)) {
            //if it is an instance or a port with parent as an instance
            boolean isInstanceComponent = (limeSubpart instanceof LimeComponent) &&
                ((LimeComponent) limeSubpart).isInstance();
            boolean hasInstanceParent = (limeSubpart instanceof Port) &&
                ((LimeComponent) limeSubpart.getParent()).isInstance();

            //dont create the association
            if (!isInstanceComponent && !hasInstanceParent) {
                root.add(getAssociationsXML(associations));
            }
        }

        //add extra information which are kept as embeddedXML for an element
        List<Element> embeddedElements = limeSubpart.getPreservedXMLElements()
                                                    .elements();

        for (Element element : embeddedElements)
            root.add(element.createCopy());

        //TODO:this is not a wise way to do so, but it is a simple way otherwise the other elements would be added each time the file is being added 
        limeSubpart.getPreservedXMLElements().clearContent();

        return root;
    }

    /**
     * creates corresponding nodes for an outgoing edge and returns the result
     *
     * @param root
     * @param arcs
     * @return an element containing all the arcs
     */
    private static Element getArcsXML(DefaultElement root, Vector<Arc> arcs) {
        // for each from connection, create its endpoints,
        // then create the edge (that's a bit funny convention)
        // 1. create end points for all from connections
        for (Arc arc : arcs) {
            Element arcElement = new DefaultElement(SystemSettings.ENDPOINT_TAG);
            //define source endpoint 
            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_TYPE_ATTRIBUTE, "locator"));
            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_LABEL_ATTRIBUTE,
                    getParentChildPath(arc.getSource())));
            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_HREF_ATTRIBUTE,
                    "#" + getParentChildPath(arc.getSource())));
            root.add(arcElement);
            //define target endpoint
            arcElement = new DefaultElement(SystemSettings.ENDPOINT_TAG);

            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_TYPE_ATTRIBUTE, "locator"));
            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_LABEL_ATTRIBUTE,
                    getParentChildPath(arc.getTarget())));
            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_HREF_ATTRIBUTE,
                    "#" + getParentChildPath(arc.getTarget())));
            root.add(arcElement);
        }

        // 2. for each arc add an edge element
        for (Arc arc : arcs) {
            Element arcElement = new DefaultElement(getPartName(arc));

            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_TYPE_ATTRIBUTE, "arc"));

            LimeSubpart source = arc.getSource();
            LimeSubpart target = arc.getTarget();
            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_FROM_ATTRIBUTE,
                    getParentChildPath(source)));
            arcElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_TO_ATTRIBUTE,
                    getParentChildPath(target)));

            DefaultElement typeElement = new DefaultElement(SystemSettings.TYPE_TAG);
            typeElement.setText(arc.getArcTypeName());

            arcElement.add(typeElement);

            // adding from and port embedded nodes
            // I. adding 'from' node
            DefaultElement fromElement = new DefaultElement(SystemSettings.FROM_NODE_TAG);
            DefaultElement fromNodeIdElement = new DefaultElement(SystemSettings.ID_TAG);
            DefaultElement fromPortIdElement = new DefaultElement(SystemSettings.PORT_ID_TAG);

            String parentName = source.getParent().getName();

            fromPortIdElement.setText(source.getRevisedName());
            fromNodeIdElement.setText(parentName);

            fromElement.add(fromPortIdElement);
            fromElement.add(fromNodeIdElement);

            // II. adding 'to' node
            DefaultElement toElement = new DefaultElement(SystemSettings.TO_NODE_TAG);
            DefaultElement toNodeIdElement = new DefaultElement(SystemSettings.ID_TAG);
            DefaultElement toPortIdElement = new DefaultElement(SystemSettings.PORT_ID_TAG);

            parentName = target.getParent().getName();

            toPortIdElement.setText(target.getRevisedName());
            toNodeIdElement.setText(parentName);

            toElement.add(toPortIdElement);
            toElement.add(toNodeIdElement);

            arcElement.add(fromElement);
            arcElement.add(toElement);
            // it should be added to the parent element
            root.add(arcElement);
        }

        return root;
    }

    /**
     * creates corresponding nodes for an outgoing edge and returns the result
     *
     * @param root
     * @param association
     * @return an element containing all the arcs
     */
    private static Element getAssociationsXML(Vector<Association> associations) {
        DefaultElement root = new DefaultElement(SystemSettings.DUMMY_ASSOCIATIONS_TAG);

        // for each from connection, create its endpoints,
        // then create the edge (that's a bit funny convention)
        // 1. create end points for all from connections
        // <bound-to xlink:type='arc' xlink:from='w11a_r_source:in'
        // xlink:to='w11a_r_source:process:in'></bound-to>
        for (Association association : associations) {
            Element associationElement = new DefaultElement(SystemSettings.BOUND_TO_TAG);

            associationElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_TYPE_ATTRIBUTE, "arc"));

            LimeSubpart source = association.getSource();
            LimeSubpart target = association.getTarget();
            associationElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_FROM_ATTRIBUTE,
                    getParentChildPath(source)));
            associationElement.add(new DefaultAttribute(
                    SystemSettings.XLINK_TO_ATTRIBUTE,
                    getParentChildPath(target)));
            root.add(associationElement);
        }

        return root;
    }

    private static String getParentChildPath(LimeSubpart source) {
        LimeSubpart traverser = source;
        String parentChildPath = traverser.getName();

        while ((traverser.getParent() != null) &&
                !traverser.getParent().getClass().equals(LimeDiagram.class)) {
            traverser = traverser.getParent();
            parentChildPath = traverser.getName() + ":" + parentChildPath;
        }

        return parentChildPath;
    }

    /**
     * add specific nodes which should be embedded inside a port definition
     *
     * @param root
     * @param port
     */
    private static void addPortAttributes(Element root, Port port) {
        if (port instanceof InputPort || port instanceof InputPortTrap) {
            root.add(createTextElement("const", ""));
        }

        // making portsize acceptable for LIME compiler
        String portSizeText = port.getPortSize();

        //ignore the first bracket otherwise it would be treated wrong
        String[] sizes = portSizeText.substring(1).split("[\\[|\\]]+");

        for (String size : sizes) {
            root.add(createTextElement(SystemSettings.SIZE_TAG, size));
        }

        //        if (portSizeText.length() != 0) {
        //            if (portSizeText.charAt(0) == ' ') {
        //                portSizeText = portSizeText.substring(1);
        //            }
        //
        //            if (portSizeText.charAt(portSizeText.length() - 1) == ' ') {
        //                portSizeText = portSizeText.substring(0,
        //                        portSizeText.length() - 1);
        //            }
        //
        //            if (portSizeText.indexOf(" ") != -1) {
        //                portSizeText = "(" + portSizeText + ")";
        //            }
        //        }
        if (port.getPortConstp()) {
            root.add(createTextElement(SystemSettings.CONSTP_TAG, ""));
        }

        if (port.getPortRestrict()) {
            root.add(createTextElement(SystemSettings.RESTRICT_TAG, ""));
        }

        if (port.getPortStatic()) {
            root.add(createTextElement(SystemSettings.STATIC_TAG, ""));
        }

        if (port.getPortVolatile()) {
            root.add(createTextElement(SystemSettings.VOLATILE_TAG, ""));
        }

        if (port.getPortVolatilep()) {
            root.add(createTextElement(SystemSettings.VOLATILEP_TAG, ""));
        }
    }

    /**
     * creates a node in the XML element with a text identified by
     *
     * @param value
     *            as the embedded text
     * @param tag
     * @param value
     * @return
     */
    private static DefaultElement createTextElement(String tag, String value) {
        if (value == null) {
            return null;
        }

        DefaultElement element = new DefaultElement(tag);
        element.addText(value);

        return element;
    }

    /**
     * creates default component XML equivalent interior elements
     * @param root
     * @param component
     */
    private static void addComponentAttributes(Element root,
        LimeComponent component) {
        //if it is an instance component just create simple node infos
        //<node xlink:type="resource" id="copy1" xlink:label="copy1"><id>copy1</id>
        //<stereo-type>copy</stereo-type>
        //</node>
        if (component.isInstance()) {
            root.add(createTextElement(SystemSettings.STEREOTYPE_TAG,
                    component.getComponentClass()));
        } else {
            //adding code fragment
            List<LimeCodeFragment> codeFragments = component.getCodeFragments();

            if (codeFragments.size() != 0) {
                for (LimeCodeFragment codeFragment : codeFragments) {
                    DefaultElement metaCode = new DefaultElement(SystemSettings.META_TAG);
                    metaCode.add(new DefaultAttribute(
                            SystemSettings.ID_ATTRIBUTE,
                            SystemSettings.META_TAG_CODE_ID));

                    DefaultElement codeFragmentElement = new DefaultElement(SystemSettings.CODE_TAG);
                    metaCode.add(codeFragmentElement);

                    codeFragmentElement.add(new DefaultCDATA(
                            codeFragment.getLabelContents()));
                    codeFragmentElement.add(new DefaultAttribute(
                            SystemSettings.CONTEXT_TAG,
                            codeFragment.getContext()));

                    //add location and size
                    DefaultElement metaVisual = new DefaultElement(SystemSettings.META_TAG);
                    metaVisual.add(new DefaultAttribute(
                            SystemSettings.ID_ATTRIBUTE,
                            SystemSettings.META_TAG_VISUAL_ID));
                    metaVisual.add(createDimensionList(
                            SystemSettings.SIZE_TAG, codeFragment.getSizeMap()));
                    metaVisual.add(createDimensionList(
                            SystemSettings.LOCATION_TAG,
                            codeFragment.getLocationMap()));

                    codeFragmentElement.add(metaVisual);

                    root.add(metaCode);
                }
            }

            //adding component return type
            DefaultElement componentRet = createTextElement(SystemSettings.RET_TAG,
                    component.getComponentRet());

            if (componentRet != null) {
                root.add(componentRet);
            }
        }
    }

    /**
     * adds default attributes to the element
     *
     * @param root
     * @param limeSubpart
     * @param elementRevisedName
     */
    private static void addDefaultAttributes(Element root,
        LimeSubpart limeSubpart) {
        DefaultElement metaVisual = new DefaultElement(SystemSettings.META_TAG);
        metaVisual.add(new DefaultAttribute(SystemSettings.ID_ATTRIBUTE,
                SystemSettings.META_TAG_VISUAL_ID));
        metaVisual.add(createDimensionList(SystemSettings.SIZE_TAG,
                limeSubpart.getSizeMap()));
        metaVisual.add(createDimensionList(SystemSettings.LOCATION_TAG,
                limeSubpart.getLocationMap()));

        root.add(metaVisual);

        DefaultElement partIDElement = new DefaultElement(SystemSettings.ID_TAG);
        partIDElement.setText(limeSubpart.getRevisedName());
        root.add(partIDElement);

        DefaultElement partType = new DefaultElement(SystemSettings.TYPE_TAG);
        partType.setText(limeSubpart.getType());
        root.add(partType);

        root.add(new DefaultAttribute(SystemSettings.ID_ATTRIBUTE,
                getParentChildPath(limeSubpart)));

        root.add(new DefaultAttribute(SystemSettings.XLINK_TYPE_ATTRIBUTE,
                "resource"));

        root.add(new DefaultAttribute(SystemSettings.XLINK_LABEL_ATTRIBUTE,
                getParentChildPath(limeSubpart)));

        // if it goes beyond two-dimension we should still be able to define it
    }

    /**
     * creates a dimension element based on t he node name it receives and a map
     * of dimensions
     *
     * @param nodeName
     * @param dimensions
     * @return
     */
    private static Element createDimensionList(String nodeName,
        Map<String, Integer> dimensions) {
        Element element = new DefaultElement(nodeName);

        for (String item : dimensions.keySet()) {
            Element dimensionElement = new DefaultElement(SystemSettings.DIMENSION_TAG);
            dimensionElement.addAttribute(SystemSettings.ID_ATTRIBUTE, item);
            dimensionElement.addAttribute(SystemSettings.VALUE_ATTRIBUTE,
                String.valueOf(dimensions.get(item)));
            element.add(dimensionElement);
        }

        return element;
    }
}

