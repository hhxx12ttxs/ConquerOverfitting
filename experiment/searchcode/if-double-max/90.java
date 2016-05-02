<<<<<<< HEAD
/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.xsd;

import jlibs.core.graph.*;
import jlibs.core.graph.navigators.FilteredTreeNavigator;
import jlibs.core.graph.sequences.DuplicateSequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.graph.sequences.RepeatSequence;
import jlibs.core.graph.visitors.ReflectionVisitor;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.core.io.IOUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.OS;
import jlibs.core.util.CollectionUtil;
import jlibs.core.util.RandomUtil;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.display.XSDisplayFilter;
import org.apache.xerces.xs.*;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
public class XSInstance{
    public int minimumElementsGenerated = 2;
    public int maximumElementsGenerated = 2;
    public int minimumListItemsGenerated = 2;
    public int maximumListItemsGenerated = 2;
    public int maximumRecursionDepth = 1;
    public Boolean generateOptionalElements = Boolean.TRUE;
    public Boolean generateOptionalAttributes = Boolean.TRUE;
    public Boolean generateFixedAttributes = Boolean.TRUE;
    public Boolean generateDefaultAttributes = Boolean.TRUE;
    public boolean generateAllChoices = false;
    public boolean showContentModel = true;

    private int generateRepeatCount(int minOccurs, int maxOccurs){
        if(minOccurs==0 && maxOccurs==1) //optional case
            return RandomUtil.randomBoolean(generateOptionalElements) ? 1 : 0;

        if(maxOccurs==-1)
            maxOccurs = Math.max(minOccurs, maximumElementsGenerated);

        int min, max;
        if(minimumElementsGenerated>maxOccurs || maximumElementsGenerated<minOccurs){ // doesn't intersect
            min = minOccurs;
            max = maxOccurs;
        }else { // find intersecting range
            min = Math.max(minOccurs, minimumElementsGenerated);
            max = Math.min(maxOccurs, maximumElementsGenerated);
        }
        return (min == max)
                ? min
                : RandomUtil.random(min, max);
    }

    public void generate(XSModel xsModel, QName rootElement, XMLDocument doc){
        generate(xsModel, rootElement, doc, null, null);
    }

    public void generate(XSModel xsModel, QName rootElement, XMLDocument doc, String xsiSchemaLocation, String xsiNoNamespaceSchemaLocation){
        String namespace = rootElement.getNamespaceURI();
        XSElementDeclaration root = xsModel.getElementDeclaration(rootElement.getLocalPart(), namespace);
        if(root==null)
            throw new IllegalArgumentException("Element "+rootElement+" is not found");

        Navigator navigator = new FilteredTreeNavigator(new XSSampleNavigator(xsModel), new XSDisplayFilter(){
            protected boolean process(XSElementDeclaration elem){
                return !elem.getAbstract();
            }

            protected boolean process(XSTypeDefinition type){
                return type.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE;
            }
        });
        try{
            doc.startDocument();
            doc.declarePrefix(Namespaces.URI_XSI);
            if(rootElement.getPrefix()!=null && !rootElement.getNamespaceURI().isEmpty()){
                if(!showContentModel || !rootElement.getPrefix().isEmpty())
                    doc.declarePrefix(rootElement.getPrefix(), rootElement.getNamespaceURI());
            }
            WalkerUtil.walk(new PreorderWalker(root, navigator), new XSSampleVisitor(doc, xsiSchemaLocation, xsiNoNamespaceSchemaLocation));
            doc.endDocument();
        }catch(SAXException ex){
            throw new ImpossibleException(ex);
        }
    }

    private class XSSampleNavigator extends XSNavigator{
        private XSModel xsModel;
        private XSSampleNavigator(XSModel xsModel){
            this.xsModel = xsModel;
        }

        protected Sequence<XSTerm> process(XSParticle particle){
            XSTerm term = particle.getTerm();
            if(term instanceof XSModelGroup){
                XSModelGroup group = (XSModelGroup)term;
                if(group.getCompositor()==XSModelGroup.COMPOSITOR_CHOICE){
                    XSObjectList particles = group.getParticles();
                    int count = particles.getLength();
                    if(!generateAllChoices && !particle.getMaxOccursUnbounded())
                        count = Math.min(count, particle.getMaxOccurs());
                    List<XSParticle> list = new ArrayList<XSParticle>(particles.getLength());
                    for(int i=0; i<particles.getLength(); i++)
                        list.add((XSParticle)particles.item(i));
                    Collections.shuffle(list);
                    return new IterableSequence(list.subList(0, count));
                }
            }

            int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
            int repeatCount = generateRepeatCount(particle.getMinOccurs(), maxOccurs);
            return new RepeatSequence<XSTerm>(super.process(particle), repeatCount);
        }

        protected Sequence<XSParticle> process(XSModelGroup modelGroup){
            switch(modelGroup.getCompositor()){
                case XSModelGroup.COMPOSITOR_ALL :
                    XSObjectList particles = modelGroup.getParticles();
                    List<XSParticle> list = new ArrayList<XSParticle>(particles.getLength());
                    for(int i=0; i<particles.getLength(); i++)
                        list.add((XSParticle)particles.item(i));
                    Collections.shuffle(list);
                    return new IterableSequence<XSParticle>(list);
                default:
                    return super.process(modelGroup);
            }
        }

        protected Sequence process(XSElementDeclaration elem){
            if(elem.getAbstract()){
                XSObjectList substitutionGroup = xsModel.getSubstitutionGroup(elem);
                int rand = RandomUtil.random(0, substitutionGroup.getLength() - 1);
                return new DuplicateSequence(substitutionGroup.item(rand));
            }
            if(elem.getTypeDefinition() instanceof XSComplexTypeDefinition){
                XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                if(complexType.getAbstract()){
                    List<XSComplexTypeDefinition> subTypes = XSUtil.getSubTypes(xsModel, complexType);
                    int rand = RandomUtil.random(0, subTypes.size() - 1);
                    return new DuplicateSequence<XSTypeDefinition>(subTypes.get(rand));
                }
            }
            return new DuplicateSequence<XSTypeDefinition>(elem.getTypeDefinition());
        }
    }

    private class XSSampleVisitor extends ReflectionVisitor<Object, Processor<Object>>{
        private XMLDocument doc;
        private String xsiSchemaLocation;
        private String xsiNoNamespaceSchemaLocation;

        private XSSampleVisitor(XMLDocument doc, String xsiSchemaLocation, String xsiNoNamespaceSchemaLocation){
            this.doc = doc;
            this.xsiSchemaLocation = xsiSchemaLocation;
            this.xsiNoNamespaceSchemaLocation = xsiNoNamespaceSchemaLocation;
        }

        private void addXSILocations() throws SAXException{
            if(doc.getDepth()==1){
                if(xsiSchemaLocation!=null)
                    doc.addAttribute(Namespaces.URI_XSI, "schemaLocation", xsiSchemaLocation);
                if(xsiNoNamespaceSchemaLocation!=null)
                    doc.addAttribute(Namespaces.URI_XSI, "noNamespaceSchemaLocation", xsiNoNamespaceSchemaLocation);
            }
        }

        @Override
        protected Processor getDefault(Object elem){
            return null;
        }

        protected Processor process(XSElementDeclaration elem){
            return elemProcessor;
        }

        protected Processor process(XSWildcard wildcard){
            return wildcardProcessor;
        }

        protected Processor process(XSComplexTypeDefinition complexType){
            return complexTypeProcessor;
        }

        protected Processor process(XSAttributeUse attr){
            return attrProcessor;
        }

        private Processor<XSElementDeclaration> elemProcessor = new Processor<XSElementDeclaration>(){
            private boolean isRecursionDepthCrossed(XSElementDeclaration elem, Path path){
                if(path.getRecursionDepth()>maximumRecursionDepth)
                    return true;

                int typeRecursionDepth = -1;
                while(path!=null){
                    if(path.getElement()==elem.getTypeDefinition())
                        typeRecursionDepth++;
                    path = path.getParentPath();
                }

                return typeRecursionDepth>maximumRecursionDepth;
            };


            @Override
            public boolean preProcess(XSElementDeclaration elem, Path path){
                if(isRecursionDepthCrossed(elem, path))
                    return false;
                try{
                    if(showContentModel && elem.getTypeDefinition() instanceof XSComplexTypeDefinition){
                        XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                        switch(complexType.getContentType()){
                            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                            case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
                                String contentModel = new XSContentModel().toString(complexType, doc);
                                boolean showContentModel = false;
                                for(char ch: "?*+|;[".toCharArray()){
                                    if(contentModel.indexOf(ch)!=-1){
                                        showContentModel = true;
                                        break;
                                    }
                                }
                                if(showContentModel){
                                    int depth = 0;
                                    while(true){
                                        path = path.getParentPath(XSElementDeclaration.class);
                                        if(path!=null)
                                            depth++;
                                        else
                                            break;
                                    }
                                    doc.addText("\n");
                                    for(int i=depth; i>0; i--)
                                        doc.addText("   ");
                                    doc.addComment(contentModel);
                                    doc.addText("\n");
                                    for(int i=depth; i>0; i--)
                                        doc.addText("   ");
                                }
                        }
                    }
                    doc.startElement(elem.getNamespace(), elem.getName());
                    addXSILocations();
                    return true;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSElementDeclaration elem, Path path){
                if(isRecursionDepthCrossed(elem, path))
                    return;
                try{
                    switch(elem.getConstraintType()){
                        case XSConstants.VC_FIXED:
                            doc.addText(elem.getValueConstraintValue().getNormalizedValue());
                            break;
                        case XSConstants.VC_DEFAULT:
                            if(RandomUtil.randomBoolean()){
                                doc.addText(elem.getValueConstraintValue().getNormalizedValue());
                                break;
                            }
                        default:
                            XSSimpleTypeDefinition simpleType = null;
                            if(elem.getTypeDefinition().getTypeCategory()==XSTypeDefinition.SIMPLE_TYPE)
                                simpleType = (XSSimpleTypeDefinition)elem.getTypeDefinition();
                            else{
                                XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                                if(complexType.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)
                                    simpleType = complexType.getSimpleType();
                            }
                            if(simpleType!=null){
                                String sampleValue = null;
                                if(sampleValueGenerator!=null)
                                    sampleValue = sampleValueGenerator.generateSampleValue(elem, simpleType);
                                if(sampleValue==null)
                                    sampleValue = generateSampleValue(simpleType, elem.getName());
                                doc.addText(sampleValue);
                            }
                    }
                    doc.endElement();
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }
        };

        private Processor<XSAttributeUse> attrProcessor = new Processor<XSAttributeUse>(){
            @Override
            public boolean preProcess(XSAttributeUse attr, Path path){
                try{
                    XSAttributeDeclaration decl = attr.getAttrDeclaration();

                    String sampleValue = null;
                    switch(attr.getConstraintType()){
                        case XSConstants.VC_FIXED:
                            if(RandomUtil.randomBoolean(generateFixedAttributes))
                                sampleValue = attr.getValueConstraintValue().getNormalizedValue();
                            break;
                        case XSConstants.VC_DEFAULT:
                            if(RandomUtil.randomBoolean(generateDefaultAttributes))
                                sampleValue = attr.getValueConstraintValue().getNormalizedValue();
                            break;
                        default:
                            if(attr.getRequired() || RandomUtil.randomBoolean(generateOptionalAttributes)){
                                if(sampleValueGenerator!=null)
                                    sampleValue = sampleValueGenerator.generateSampleValue(decl, decl.getTypeDefinition());
                                if(sampleValue==null)
                                    sampleValue = generateSampleValue(decl.getTypeDefinition(), decl.getName());
                            }
                    }
                    if(sampleValue!=null)
                        doc.addAttribute(decl.getNamespace(), decl.getName(), sampleValue);
                    return false;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSAttributeUse elem, Path path){}
        };

        private Processor<XSComplexTypeDefinition> complexTypeProcessor = new Processor<XSComplexTypeDefinition>(){
            @Override
            public boolean preProcess(XSComplexTypeDefinition complexType, Path path){
                try{
                    XSElementDeclaration elem = (XSElementDeclaration)path.getParentPath().getElement();
                    XSComplexTypeDefinition elemType = (XSComplexTypeDefinition)elem.getTypeDefinition();
                    if(elemType.getAbstract())
                        doc.addAttribute(Namespaces.URI_XSI, "type", doc.toQName(complexType.getNamespace(), complexType.getName()));
                    return true;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSComplexTypeDefinition complexType, Path path){}
        };

        private Processor<XSWildcard> wildcardProcessor = new Processor<XSWildcard>(){
            @Override
            public boolean preProcess(XSWildcard wildcard, Path path){
                try{
                    String uri;
                    switch(wildcard.getConstraintType()){
                        case XSWildcard.NSCONSTRAINT_ANY:
                            uri = "anyNS";
                            break;
                        case XSWildcard.NSCONSTRAINT_LIST:
                            StringList list = wildcard.getNsConstraintList();
                            int rand = RandomUtil.random(0, list.getLength()-1);
                            uri = list.item(rand);
                            if(uri==null)
                                uri = ""; // <xs:any namespace="##local"/> returns nsConstraintList with null
                            break;
                        case XSWildcard.NSCONSTRAINT_NOT:
                            list = wildcard.getNsConstraintList();
                            List<String> namespaces = new ArrayList<String>();
                            for(int i=0; i<list.getLength(); i++)
                                namespaces.add(list.item(i));
                            uri = "anyNS";
                            if(namespaces.contains(uri)){
                                for(int i=1;;i++){
                                    if(!namespaces.contains(uri+i)){
                                        uri += i;
                                        break;
                                    }
                                }
                            }
                            break;
                        default:
                            throw new ImpossibleException();
                    }
                    if(isAttribute(wildcard, path))
                        doc.addAttribute(uri, "anyAttr", "anyValue");
                    else{
                        doc.startElement(uri, "anyElement");
                        addXSILocations();
                    }
                    return true;
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            @Override
            public void postProcess(XSWildcard wildcard, Path path){
                try{
                    if(!isAttribute(wildcard, path))
                        doc.endElement();
                }catch(SAXException ex){
                    throw new ImpossibleException(ex);
                }
            }

            private boolean isAttribute(XSWildcard wildcard, Path path){
                if(path.getParentPath().getElement() instanceof XSComplexTypeDefinition){
                    XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)path.getParentPath().getElement();
                    if(complexType.getAttributeWildcard()==wildcard)
                        return true;
                }
                return false;
            }
        };

        private Map<String, Integer> counters = new HashMap<String, Integer>();

        private static final String XSD_DATE_FORMAT = "yyyy-MM-dd";
        private static final String XSD_TIME_FORMAT = "HH:mm:ss";

        private String generateSampleValue(XSSimpleTypeDefinition simpleType, String hint){
            if(simpleType.getBuiltInKind()==XSConstants.LIST_DT){
                XSSimpleTypeDefinition itemType = simpleType.getItemType();

                int len;
                XSFacet facet = getFacet(itemType, XSSimpleTypeDefinition.FACET_LENGTH);
                if(facet!=null)
                    len = Integer.parseInt(facet.getLexicalFacetValue());
                else{
                    int minOccurs = 0;
                    facet = getFacet(itemType, XSSimpleTypeDefinition.FACET_MINLENGTH);
                    if(facet!=null)
                        minOccurs = Integer.parseInt(facet.getLexicalFacetValue());
                    int maxOccurs = -1;
                    facet = getFacet(itemType, XSSimpleTypeDefinition.FACET_MAXLENGTH);
                    if(facet!=null)
                        maxOccurs = Integer.parseInt(facet.getLexicalFacetValue());

                    if(maxOccurs==-1)
                        maxOccurs = Math.max(minOccurs, maximumListItemsGenerated);

                    int min, max;
                    if(minimumListItemsGenerated>maxOccurs || maximumListItemsGenerated<minOccurs){ // doesn't intersect
                        min = minOccurs;
                        max = maxOccurs;
                    }else { // find intersecting range
                        min = Math.max(minOccurs, minimumListItemsGenerated);
                        max = Math.min(maxOccurs, maximumListItemsGenerated);
                    }
                    len = (min == max)
                            ? min
                            : RandomUtil.random(min, max);
                }

                List<String> enums = XSUtil.getEnumeratedValues(itemType);
                if(enums.isEmpty()){
                    StringBuilder buff = new StringBuilder();
                    while(len>0){
                        buff.append(" ");
                        buff.append(generateSampleValue(itemType, hint));
                        len--;
                    }
                    return buff.toString().trim();
                }else{
                    while(enums.size()<len)
                        enums.addAll(new ArrayList<String>(enums));
                    Collections.shuffle(enums);

                    StringBuilder buff = new StringBuilder();
                    while(len>0){
                        buff.append(" ");
                        buff.append(enums.remove(0));
                        len--;
                    }
                    return buff.toString().trim();
                }
            }else if(simpleType.getMemberTypes().getLength()>0){
                XSObjectList members = simpleType.getMemberTypes();
                int rand = RandomUtil.random(0, members.getLength()-1);
                return generateSampleValue((XSSimpleTypeDefinition)members.item(rand), hint);
            }

            List<String> enums = XSUtil.getEnumeratedValues(simpleType);
            if(!enums.isEmpty())
                return enums.get(RandomUtil.random(0, enums.size()-1));

            XSSimpleTypeDefinition builtInType = simpleType;
            while(!Namespaces.URI_XSD.equals(builtInType.getNamespace()))
                builtInType = (XSSimpleTypeDefinition)builtInType.getBaseType();


            String name = builtInType.getName().toLowerCase();
            if("boolean".equals(name))
                return RandomUtil.randomBoolean() ? "true" : "false";

            if("double".equals(name)
                    || "decimal".equals(name)
                    || "float".equals(name)
                    || name.endsWith("integer")
                    || name.endsWith("int")
                    || name.endsWith("long")
                    || name.endsWith("short")
                    || name.endsWith("byte"))
                return new Range(simpleType).randomNumber();

            if("date".equals(name))
                return new SimpleDateFormat(XSD_DATE_FORMAT).format(new Date());
            if("time".equals(name))
                return new SimpleDateFormat(XSD_TIME_FORMAT).format(new Date());
            if("datetime".equals(name)){
                Date date = new Date();
                return new SimpleDateFormat(XSD_DATE_FORMAT).format(date)+'T'+new SimpleDateFormat(XSD_TIME_FORMAT).format(date);
            }else{
                Integer count = counters.get(hint);
                count = count==null ? 1 : ++count;
                counters.put(hint, count);
                String countStr = count.toString();

                XSFacet lengthFacet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_LENGTH);

                XSFacet facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MINLENGTH);
                if(facet==null)
                    facet = lengthFacet;
                if(facet!=null){
                    int len = Integer.parseInt(facet.getLexicalFacetValue());
                    len -= hint.length();
                    len -= countStr.length();
                    if(len>0){
                        char ch[] = new char[len];
                        Arrays.fill(ch, '_');
                        hint += new String(ch);
                    }
                }
                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXLENGTH);
                if(facet==null)
                    facet = lengthFacet;
                if(facet!=null){
                    int maxLen = Integer.parseInt(facet.getLexicalFacetValue());
                    int len = maxLen;
                    len = hint.length() + countStr.length() - len;
                    if(len>0){
                        if(hint.length()>len)
                            hint = hint.substring(0, hint.length()-len);
                        else{
                            hint = hint.substring(0, maxLen);
                            countStr = "";
                        }
                    }
                }
                String value = hint+countStr;

                if("base64binary".equals(name))
                    return DatatypeConverter.printBase64Binary(value.getBytes(IOUtil.UTF_8));
                else
                    return value;
            }
        }

        private XSFacet getFacet(XSSimpleTypeDefinition simpleType, int kind){
            XSObjectList facets = simpleType.getFacets();
            for(int i=0; i<facets.getLength(); i++){
                XSFacet facet = (XSFacet)facets.item(i);
                if(facet.getFacetKind()==kind)
                    return facet;
            }
            return null;
        }

        class Range{
            String minInclusive;
            String minExclusive;
            String maxInclusive;
            String maxExclusive;
            int totalDigits = -1;
            int fractionDigits = -1;

            Range(XSSimpleTypeDefinition simpleType){
                XSFacet facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MININCLUSIVE);
                if(facet!=null)
                    minInclusive = facet.getLexicalFacetValue();
                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MINEXCLUSIVE);
                if(facet!=null)
                    minExclusive = facet.getLexicalFacetValue();

                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXINCLUSIVE);
                if(facet!=null)
                    maxInclusive = facet.getLexicalFacetValue();
                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE);
                if(facet!=null)
                    maxExclusive = facet.getLexicalFacetValue();

                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_TOTALDIGITS);
                if(facet!=null)
                    totalDigits = Integer.parseInt(facet.getLexicalFacetValue());

                facet = getFacet(simpleType, XSSimpleTypeDefinition.FACET_FRACTIONDIGITS);
                if(facet!=null)
                    fractionDigits = Integer.parseInt(facet.getLexicalFacetValue());
            }

            private String applyDigits(Object obj){
                String str = applyExponent(String.valueOf(obj));
                String number, fraction;
                int dot = str.indexOf(".");
                if(dot==-1){
                    number = str;
                    fraction = "";
                }else{
                    number = str.substring(0, dot);
                    fraction = str.substring(dot+1);
                }
                boolean negative = false;
                if(number.startsWith("-")){
                    negative = true;
                    number = number.substring(1);
                }
                if(totalDigits>=0){
                    if(number.length()>totalDigits)
                        number = number.substring(0, totalDigits);
                }
                if(fractionDigits>=0){
                    if(fraction.length()>fractionDigits)
                        fraction = fraction.substring(0, fractionDigits);
                }

                str = negative ? "-" : "";
                str += number;
                if(fraction.length()>0)
                    str += '.' + fraction;
                return str;
            }

            private String applyExponent(String str){
                int index = str.indexOf('E');
                if(index==-1)
                    return str;

                int exponent = Integer.parseInt(str.substring(index+(str.charAt(index+1)=='+'?2:1)));
                str = str.substring(0, index);

                boolean negative = false;
                if(str.charAt(0)=='-'){
                    negative = true;
                    str = str.substring(1);
                }

                if(exponent!=0){
                    int dot = str.indexOf('.');
                    String beforeDot, afterDot;
                    if(dot==-1){
                        beforeDot = str;
                        afterDot = "";
                    }else{
                        beforeDot = str.substring(0, dot);
                        afterDot = str.substring(dot+1);
                    }

                    if(exponent<0){
                        while(exponent!=0){
                            if(beforeDot.length()==1)
                                beforeDot = "0"+beforeDot;
                            afterDot = beforeDot.substring(beforeDot.length()-1)+afterDot;
                            beforeDot = beforeDot.substring(0, beforeDot.length()-1);
                            exponent++;
                        }
                    }else{
                        while(exponent!=0){
                            if(afterDot.isEmpty())
                                afterDot = "0";
                            beforeDot = beforeDot+afterDot.substring(0, 1);
                            afterDot = afterDot.substring(1);
                            exponent--;
                        }
                    }
                    str = afterDot.isEmpty() ? beforeDot : beforeDot+"."+afterDot;
                }
                if(negative)
                    str = "-"+str;

                return str;
            }

            public String randomNumber(){
                if(fractionDigits==0){
                    // NOTE: min/max facets can have fractional part
                    //       even though fractionDigits is zero
                    long min = Long.MIN_VALUE;
                    if(minInclusive!=null)
                        min = (long)Double.parseDouble(minInclusive);
                    if(minExclusive!=null)
                        min = (long)Double.parseDouble(minExclusive)+1;

                    long max = Long.MAX_VALUE;
                    if(maxInclusive!=null)
                        max = (long)Double.parseDouble(maxInclusive);
                    if(maxExclusive!=null)
                        max = (long)Double.parseDouble(maxExclusive)-1;

                    return applyDigits(RandomUtil.random(min, max));
                }else{
                    double min = Double.MIN_VALUE;
                    if(minInclusive!=null)
                        min = Double.parseDouble(minInclusive);
                    if(minExclusive!=null)
                        min = Double.parseDouble(minExclusive)+1;

                    double max = Double.MAX_VALUE;
                    if(maxInclusive!=null)
                        max = Double.parseDouble(maxInclusive);
                    if(maxExclusive!=null)
                        max = Double.parseDouble(maxExclusive)-1;

                    return applyDigits(RandomUtil.random(min, max));
                }
            }
        }
    }

    public void loadOptions(Properties options){
        String value = options.getProperty("minimumElementsGenerated");
        if(value!=null)
            minimumElementsGenerated = Integer.parseInt(value);
        value = options.getProperty("maximumElementsGenerated");
        if(value!=null)
            maximumElementsGenerated = Integer.parseInt(value);
        value = options.getProperty("minimumElementsGenerated");
        if(value!=null)
            minimumListItemsGenerated = Integer.parseInt(value);
        value = options.getProperty("maximumListItemsGenerated");
        if(value!=null)
            maximumListItemsGenerated = Integer.parseInt(value);
        value = options.getProperty("maximumRecursionDepth");
        if(value!=null)
            maximumRecursionDepth = Integer.parseInt(value);

        value = options.getProperty("generateOptionalElements");
        if(value!=null)
            generateOptionalElements = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateOptionalAttributes");
        if(value!=null)
            generateOptionalAttributes = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateFixedAttributes");
        if(value!=null)
            generateFixedAttributes = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateOptionalElements");
        if(value!=null)
            generateOptionalElements = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateDefaultAttributes");
        if(value!=null)
            generateDefaultAttributes = "always".equals(value) ? Boolean.TRUE : ("never".equals(value) ? Boolean.FALSE : null);
        value = options.getProperty("generateAllChoices");
        if(value!=null)
            generateDefaultAttributes = Boolean.parseBoolean(value);
        value = options.getProperty("showContentModel");
        if(value!=null)
            showContentModel = Boolean.parseBoolean(value);
    }

    public SampleValueGenerator sampleValueGenerator;

    public static interface SampleValueGenerator{
        public String generateSampleValue(XSElementDeclaration element, XSSimpleTypeDefinition simpleType);
        public String generateSampleValue(XSAttributeDeclaration attribute, XSSimpleTypeDefinition simpleType);
    }

    public static void main(String[] args) throws Exception{
        if(args.length==0){
            System.err.println("Usage:");
            System.err.println("\txsd-instance."+(OS.get().isWindows()?"bat":"sh")+" <xsd-file> [root-element]");
            System.err.println("Example:");
            System.err.println("\txsd-instance."+(OS.get().isWindows()?"bat":"sh")+" purchase-order.xsd {http://jlibs.org}PurchaseOrder");
            System.exit(1);
        }

        XSModel xsModel = new XSParser().parse(args[0]);
        QName rootElement = null;
        if(args.length>1)
            rootElement = QName.valueOf(args[1]);
        else{
            List<XSElementDeclaration> elements = XSUtil.guessRootElements(xsModel);
            if(elements.size()==0){
                System.err.println("no elements found in given xml schema");
                System.exit(1);
            }else if(elements.size()==1){
                XSElementDeclaration elem = elements.get(0);
                rootElement = XSUtil.getQName(elem);
            }else{
                int i = 1;
                for(XSElementDeclaration elem: elements)
                    System.err.println(i++ +": "+XSUtil.getQName(elem));
                System.err.print("Select Root Element: ");
                String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
                XSElementDeclaration elem = elements.get(Integer.parseInt(line)-1);
                rootElement = XSUtil.getQName(elem);
            }
        }

        XSInstance xsInstance = new XSInstance();
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("xsd-instance.properties");
        if(is!=null)
            xsInstance.loadOptions(CollectionUtil.readProperties(is, null));
        xsInstance.generate(xsModel, rootElement, new XMLDocument(new StreamResult(System.out), true, 4, null));
        System.out.println();
=======
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnlab.trixy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.filechooser.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import kuhnlab.coordinates.KPoint2D;
import kuhnlab.gui.GenericInputDialog;
import kuhnlab.gui.GenericOptionsDialog;
import kuhnlab.gui.GenericOptionsPanel;
import kuhnlab.gui.TableOutputDialog;
import kuhnlab.math.StatTools;
import kuhnlab.trixy.data.io.CommaFileHandler;
import kuhnlab.trixy.data.io.PtiFileHandler;
import kuhnlab.trixy.data.io.SerializedFileHandler;
import kuhnlab.trixy.data.Series;
import kuhnlab.trixy.data.io.SeriesFileFilter;
import kuhnlab.trixy.data.io.SeriesFileHandler;
import kuhnlab.trixy.data.SeriesList;
import kuhnlab.trixy.data.io.KinsimFileHandler;
import kuhnlab.trixy.data.io.SoftmaxFileHandler;
import kuhnlab.trixy.data.io.TabbedFileHandler;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author jrkuhn
 */
public class TrixyApp extends SingleFrameApplication implements ClipboardOwner,
        ListDataListener {

    protected TrixyAboutBox aboutBox;
    public SeriesList data;
    public File currentFile;
    public FileFilter currentFileType;
    public File currentPath;
    public boolean modified = false;
    protected JList seriesJList;
    protected JPanel blankPanel;
    protected JScrollPane seriesScrollPane;
    protected JSplitPane splitPane;
    protected ChartPanel mainChartPanel;
    protected JFreeChart mainChart;
    protected XYItemRenderer mainChartRenderer;
    protected String appName;
    protected FrameView view;
    public SeriesList undoBuffer;
    public SeriesJList dataListComponent;
    protected boolean replace = false;
    protected boolean rename = true;
    protected ChartPanelMouseAdapter markListener;
    protected boolean fileOpen = false;
    protected boolean zoomMode = false;
    protected boolean legendVisible = false;
    protected boolean pasteAvailable = false;
    protected boolean undoAvailable = false;
    protected SeriesFileHandler[] seriesReaders = {
        new CommaFileHandler(), new TabbedFileHandler(), new SoftmaxFileHandler(),
        new PtiFileHandler(), new SerializedFileHandler()
    };
    protected SeriesFileHandler[] seriesWriters = {
        new CommaFileHandler(), new TabbedFileHandler(),
        new PtiFileHandler(), new KinsimFileHandler(), new SerializedFileHandler()
    };

    public boolean isFileOpen() {
        return fileOpen;
    }

    public void setFileOpen(boolean fileOpen) {
        boolean old = this.fileOpen;
        this.fileOpen = fileOpen;
        firePropertyChange("fileOpen", old, this.fileOpen);
    }

    public boolean isZoomMode() {
        return zoomMode;
    }

    public void setZoomMode(boolean zoomMode) {
        boolean old = this.zoomMode;
        this.zoomMode = zoomMode;
        firePropertyChange("zoomMode", old, this.zoomMode);
    }

    public boolean isLegendVisible() {
        return legendVisible;
    }

    public void setLegendVisible(boolean legendVisible) {
        boolean old = this.legendVisible;
        this.legendVisible = legendVisible;
        firePropertyChange("legendVisible", old, this.legendVisible);
    }

    public boolean isPasteAvailable() {
        return pasteAvailable;
    }

    public void setPasteAvailable(boolean pasteAvailable) {
        boolean old = this.pasteAvailable;
        this.pasteAvailable = pasteAvailable;
        firePropertyChange("pasteAvailable", old, this.pasteAvailable);
    }

    public boolean isUndoAvailable() {
        return undoAvailable;
    }

    public void setUndoAvailable(boolean undoAvailable) {
        boolean old = this.undoAvailable;
        this.undoAvailable = undoAvailable;
        firePropertyChange("undoAvailable", old, this.undoAvailable);
    }

    @Override
    protected void startup() {
        currentFile = null;
        currentFileType = null;
        data = null;
        undoBuffer = null;
        mainChart = null;
        mainChartRenderer = null;
        mainChartPanel = null;
        modified = false;

        final PlatformType platform = AppHelper.getPlatform();
        if (PlatformType.OS_X.equals(platform)) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.brushMetalLook", "true");
            //System.setProperty("apple.awt.fileDialogForDirectories", "true");
        }

        appName = getResourceString("Application.title");
        currentPath = new File(System.getProperty("user.home"));
        view = new FrameView(this);
        initView(view);
        this.addExitListener(new Application.ExitListener() {
            public boolean canExit(EventObject event) {
                return canExitApplication();
            }

            public void willExit(EventObject event) {
            }
        });

        show(view);
    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return the instance of TrixyApp
     */
    public static TrixyApp getApplication() {
        return Application.getInstance(TrixyApp.class);
    }

    public String getResourceString(String name) {
        return getContext().getResourceMap().getString(name);
    }

    /**
     * Create a menu from a list of strings. Allows for sub-sub menus.
     *
     * @param items
     * @return
     */
    protected JMenu createMenu(String menuName) {
        JMenu menu = new JMenu();
        menu.setName(menuName);
        String menuItemsString = getResourceString(menuName + "_items");
        String[] menuItems = menuItemsString.split("\\s");
        for (String item : menuItems) {
            if (item.startsWith(">")) {
                menu.add(createMenu(item.substring(1)));
            } else if (item.startsWith("---")) {
                menu.add(new JSeparator());
            } else {
                boolean showIcon;
                if (item.startsWith("@")) {
                    showIcon = true;
                    item = item.substring(1);
                } else {
                    showIcon = false;
                }
                ApplicationAction action = getAction(item);
                boolean isSelectable = action.getValue(javax.swing.Action.SELECTED_KEY) != null;
                JMenuItem menuItem = isSelectable ? new JCheckBoxMenuItem(action) : new JMenuItem(action);
                if (!showIcon) {
                    menuItem.setIcon(null);
                }
                menu.add(menuItem);
            }
        }
        return menu;
    }

    protected JMenuBar createMenuBar() {
        String menusString = getResourceString("mainMenu");
        String[] menus = menusString.split("\\s");
        JMenuBar menuBar = new JMenuBar();
        for (String menu : menus) {
            menuBar.add(createMenu(menu));
        }
        return menuBar;
    }

    protected JToolBar createToolBar() {
        String itemsString = getResourceString("mainToolBar");
        String[] items = itemsString.split("\\s");
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        for (String item : items) {
            if (item.startsWith("---")) {
                toolBar.addSeparator();
            } else {
                ApplicationAction action = getAction(item);
                boolean isSelectable = action.getValue(javax.swing.Action.SELECTED_KEY) != null;
                AbstractButton button = isSelectable ? new JToggleButton(action) : new JButton(action);
                button.setText(null);
                button.setFocusable(false);
                toolBar.add(button);
            }
        }
        return toolBar;
    }

    protected ApplicationAction getAction(String actionName) {
        ApplicationActionMap map = getContext().getActionMap();
        return (ApplicationAction) map.get(actionName);
    }

    protected void initView(FrameView view) {
        splitPane = new javax.swing.JSplitPane();
        seriesScrollPane = new javax.swing.JScrollPane();
        seriesJList = new javax.swing.JList();
        blankPanel = new javax.swing.JPanel();

        splitPane.setDividerLocation(120);
        seriesJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        seriesScrollPane.setViewportView(seriesJList);
        dataListComponent = new SeriesJList();
        seriesScrollPane.setViewportView(dataListComponent);

        splitPane.setLeftComponent(seriesScrollPane);

        blankPanel.setLayout(new java.awt.BorderLayout());

        blankPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.disabledForeground"));
        blankPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        blankPanel.setPreferredSize(new java.awt.Dimension(400, 400));
        splitPane.setRightComponent(blankPanel);

        view.setComponent(splitPane);

        JMenuBar menuBar = createMenuBar();
        view.setMenuBar(menuBar);

        JToolBar toolBar = createToolBar();
        view.setToolBar(toolBar);
    }

    //=======================================================================
    // Helper Methods
    //=======================================================================
    public SeriesList readSeriesFile(File file, FileFilter filter) {
        SeriesFileHandler mainHandler = null;
        List<SeriesFileHandler> possibleHandlers = new ArrayList<SeriesFileHandler>();
        // search for a single handler or a list of possible handlers
        for (SeriesFileHandler handler : seriesReaders) {
            if (filter == handler.getFilter()) {
                // the user specifically requested this file type
                mainHandler = handler;
                break;
            } else if (handler.isFileExtension(file)) {
                possibleHandlers.add(handler);
            }
        }

        if (mainHandler == null) {
            if (possibleHandlers.size() == 1) {
                // only one possible handler
                mainHandler = possibleHandlers.get(0);
            } else if (possibleHandlers.size() > 0) {
                // could not find a single handler for this file extension
                // guess handler based on file signature
                for (SeriesFileHandler handler : possibleHandlers) {
                    if (handler.isFileSignature(file)) {
                        mainHandler = handler;
                        break;
                    }
                }
            }
        }

        if (mainHandler == null) {
            // could not find any handlers
            return null;
        }

        // Allow the handler to prompt for options
        GenericOptionsPanel op = mainHandler.getOptionsPanel(false);
        if (op != null) {
            GenericOptionsDialog gd = new GenericOptionsDialog(view.getFrame(), "Read file options");
            gd.addOptionsTab(op.getName(), op);
            if (!gd.showDialog()) {
                return null;
            }
        }

        return mainHandler.readFile(file);
    }

    public void openData(File file, FileFilter filter) {
        currentPath = new File(file.getParent());
        SeriesList newdata = readSeriesFile(file, filter);
        if (newdata == null) {
            JOptionPane.showMessageDialog(view.getFrame(), "Unable to open file " + file.getName(), appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        closeData();
        currentFile = file;
        currentFileType = filter;
        data = newdata;
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        mainChart = ChartFactory.createXYLineChart(null, "X", "Y", data, PlotOrientation.VERTICAL, false, true, false);
        mainChartRenderer = mainChart.getXYPlot().getRenderer();

        mainChartPanel = new ChartPanel(mainChart);
        if (markListener == null) {
            markListener = new ChartPanelMouseAdapter();
        }
        mainChartPanel.addMouseListener(markListener);
        mainChartPanel.addMouseMotionListener(markListener);

        splitPane.setRightComponent(mainChartPanel);

        dataListComponent.setSeriesList(data, mainChartRenderer);
        dataListComponent.addListDataListener(this);
        splitPane.setDividerLocation(120);

        modified = false;
        updateTitle();
        setFileOpen(true);

        setZoomMode(false);
        updateZoomMode();
        setLegendVisible(false);
        updateLegendVisible();

        // add X and Y axes
        XYPlot plot = mainChart.getXYPlot();
        java.awt.Stroke stroke = new java.awt.BasicStroke(0.5f);
        //plot.addAnnotation(new XYAxisAnnotation(0, XYAxisAnnotation.DOMAIN_AXIS, stroke, Color.GRAY));
        //plot.addAnnotation(new XYAxisAnnotation(0, XYAxisAnnotation.RANGE_AXIS, stroke, Color.GRAY));
        mainChart.fireChartChanged();
        updateColors();
        updateTitle();
        dataListComponent.clearSelection();
        dataListComponent.repaint();

        splitPane.validate();
    }

    public void closeData() {
        currentFile = null;
        currentFileType = null;
        data = null;
        undoBuffer = null;
        mainChart = null;
        mainChartRenderer = null;
        if (markListener != null && mainChartPanel != null) {
            mainChartPanel.removeMouseListener(markListener);
            mainChartPanel.removeMouseMotionListener(markListener);
        }
        mainChartPanel = null;

        splitPane.setRightComponent(blankPanel);

        dataListComponent.removeListDataListener(this);
        dataListComponent.clearSeriesList();

        modified = false;
        updateTitle();
        setFileOpen(false);
        view.getFrame().validate();
    }

    public boolean writeSeriesFile(SeriesList data, File file, FileFilter filter) {
        // search for a single handler or a list of possible handlers
        SeriesFileHandler mainHandler = null;
        List<SeriesFileHandler> possibleHandlers = new ArrayList<SeriesFileHandler>();
        for (SeriesFileHandler handler : seriesWriters) {
            if (filter == handler.getFilter()) {
                // the user specifically requested this file type
                mainHandler = handler;
                break;
            } else if (handler.isFileExtension(file)) {
                possibleHandlers.add(handler);
            }
        }

        if (mainHandler == null && possibleHandlers.size() > 0) {
            // could not find a single handler. Just use the first file
            // type that matches this file extension
            mainHandler = possibleHandlers.get(0);
        }

        if (mainHandler == null) {
            // could not find any handlers
            return false;
        }

        GenericOptionsPanel op = mainHandler.getOptionsPanel(true);
        if (op != null) {
            GenericOptionsDialog gd = new GenericOptionsDialog(view.getFrame(), "Save file options");
            gd.addOptionsTab(op.getName(), op);
            if (!gd.showDialog()) {
                return false;
            }
        }
        return mainHandler.writeFile(data, file);
    }

    public boolean saveData(File newfile, FileFilter filter) {
        boolean ret = writeSeriesFile(data, newfile, filter);
        if (!ret) {
            JOptionPane.showMessageDialog(view.getFrame(), "Unable to save file\n" + newfile.getName(), appName, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        currentFile = newfile;
        currentFileType = filter;
        currentPath = new File(newfile.getParent());
        modified = false;
        updateTitle();
        view.getFrame().validate();
        return true;
    }

    public boolean canExitApplication() {
        if (modified) {
            JOptionPane op = new JOptionPane();
            int choice = JOptionPane.showConfirmDialog(null, "The file was not saved. Exit anyway?", appName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        return true;
    }

    public SeriesList getSelectedSeries() {
        return data.subList(dataListComponent.getSelectedIndices());
    }

    public int getLastSelectedIndex() {
        int[] indices = dataListComponent.getSelectedIndices();
        if (indices != null && indices.length > 0) {
            return indices[indices.length - 1];
        } else {
            return -1;
        }
    }

    public void setSelectedSeries(SeriesList sublist) {
        if (sublist == null) {
            dataListComponent.clearSelection();
        }
        int nSeries = data.getSeriesCount();
        List<Integer> indexList = new ArrayList<Integer>();
        for (int index = 0; index < nSeries; index++) {
            if (sublist.getSeries().contains(data.getSeries(index))) {
                indexList.add(new Integer(index));
            }
        }
        if (indexList.size() > 0) {
            // convert List to array
            int nSelected = indexList.size();
            int[] indices = new int[nSelected];
            for (int i = 0; i < nSelected; i++) {
                indices[i] = indexList.get(i);
            }
            dataListComponent.setSelectedIndices(indices);
        } else {
            dataListComponent.clearSelection();
        }
    }

    public void updateColors() {
        // Attempt to obtain colors of each of the plot lines
        AbstractRenderer renderer;
        if (mainChartRenderer instanceof AbstractRenderer) {
            renderer = (AbstractRenderer) mainChartRenderer;
        } else {
            return;
        }
        for (int i = 0; i < data.getSeriesCount(); i++) {
            Paint paint = renderer.lookupSeriesPaint(i);
            if (paint instanceof Color) {
                data.getSeries(i).setColor((Color) paint);
            }
        }
    }

    public void updateTitle() {
        if (currentFile != null) {
            getMainFrame().setTitle(appName + " - " + currentFile.getName() + (modified ? " *" : ""));
        } else {
            getMainFrame().setTitle(appName);
        }
    }

    public void startModify() {
        data.saveVisibility(mainChartRenderer);
        undoBuffer = (SeriesList) data.clone();
    }

    public void endModify() {
        modified = true;
        data.restoreVisibility(mainChartRenderer, false);
        mainChart.fireChartChanged();
        setUndoAvailable(undoBuffer != null);
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        boolean canPaste = data.canImport(clip.getAvailableDataFlavors());
        setPasteAvailable(canPaste);
        updateColors();
        updateTitle();
        dataListComponent.repaint();
    }

    void updateZoomMode() {
        if (mainChartPanel == null) {
            return;
        }
        mainChartPanel.setDomainZoomable(zoomMode);
        mainChartPanel.setRangeZoomable(zoomMode);
    }

    void updateLegendVisible() {
        if (mainChart != null) {
            if (legendVisible) {
                LegendTitle legend = new LegendTitle(mainChart.getXYPlot());
                legend.setMargin(1.0, 1.0, 1.0, 1.0);
                legend.setFrame(new BlockBorder());
                legend.setBackgroundPaint(Color.white);
                legend.setPosition(RectangleEdge.RIGHT);
                mainChart.addLegend(legend);
            } else {
                mainChart.removeLegend();
            }
        }
    }

    void markXRange(double xmin, double xmax) {
        if (mainChart == null) {
            return;
        }
        IntervalMarker mark = new IntervalMarker(xmin, xmax);
        mark.setPaint(new Color(240, 240, 255));
        XYPlot plot = mainChart.getXYPlot();
        plot.clearDomainMarkers();
        plot.addDomainMarker(mark, org.jfree.ui.Layer.BACKGROUND);
    }

    void clearMarkedXRange() {
        if (mainChart == null) {
            return;
        }
        XYPlot plot = mainChart.getXYPlot();
        plot.clearDomainMarkers();
    }

    double[] getMarkedXRange() {
        if (mainChart == null) {
            return null;
        }
        XYPlot plot = mainChart.getXYPlot();
        Collection<Marker> markers = plot.getDomainMarkers(org.jfree.ui.Layer.BACKGROUND);
        if (markers == null) {
            return null;
        }
        for (Marker mark : markers) {
            if (mark instanceof IntervalMarker) {
                IntervalMarker imark = (IntervalMarker) mark;
                double[] range = {imark.getStartValue(), imark.getEndValue()};
                return range;
            }
        }
        return null;
    }

    //=======================================================================
    // ListDataListener implementation
    //=======================================================================
    public void intervalAdded(ListDataEvent e) {
        modified = true;
        mainChart.fireChartChanged();
        updateColors();
        dataListComponent.repaint();
    }

    public void intervalRemoved(ListDataEvent e) {
        modified = true;
        mainChart.fireChartChanged();
        updateColors();
        dataListComponent.repaint();
    }

    public void contentsChanged(ListDataEvent e) {
        modified = true;
        mainChart.fireChartChanged();
        updateColors();
        dataListComponent.repaint();
    }

    //=======================================================================
    // ClipboardOwner implementation
    //=======================================================================
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        boolean canImport = data.canImport(clipboard.getAvailableDataFlavors());
        setPasteAvailable(canImport);
    }

    //=======================================================================
    // MouseListener utility class to handle chart selection
    //=======================================================================
    public class ChartPanelMouseAdapter implements MouseListener, MouseMotionListener {

        Rectangle2D markArea = null;
        Point markStart = null;

        protected Point constrainPoint(int x, int y, Rectangle2D area) {
            int xmin = (int) Math.floor(area.getMinX()), xmax = (int) Math.ceil(area.getMaxX());
            int ymin = (int) Math.floor(area.getMinY()), ymax = (int) Math.ceil(area.getMaxY());
            x = (int) Math.max(xmin, Math.min(x, xmax));
            y = (int) Math.max(ymin, Math.min(y, ymax));
            return new Point(x, y);
        }

        public void mouseClicked(MouseEvent e) {
            clearMarkedXRange();
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            Object src = e.getSource();
            if (!(src instanceof ChartPanel) || zoomMode) {
                return;
            }
            ChartPanel chart = (ChartPanel) src;

            if (markArea == null) {
                Rectangle2D screenDataArea = chart.getScreenDataArea(e.getX(), e.getY());
                if (screenDataArea != null) {
                    markStart = constrainPoint(e.getX(), e.getY(), screenDataArea);
                } else {
                    markStart = null;
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            Object src = e.getSource();
            if (!(src instanceof ChartPanel) || zoomMode || markStart == null) {
                return;
            }
            ChartPanel chart = (ChartPanel) src;

            Graphics2D g2 = (Graphics2D) chart.getGraphics();
            g2.setXORMode(java.awt.Color.YELLOW);

            if (this.markArea != null) {
                g2.draw(markArea);
            }

            Rectangle2D plotArea = chart.getScreenDataArea(
                    (int) markStart.getX(), (int) markStart.getY());

            double xright = Math.min(e.getX(), plotArea.getMaxX());
            markArea = new Rectangle2D.Double(
                    markStart.getX(), plotArea.getMinY(),
                    xright - markStart.getX(), plotArea.getHeight());

            if (markArea != null) {
                g2.draw(markArea);
            }
            g2.dispose();

        }

        public void mouseReleased(MouseEvent e) {
            Object src = e.getSource();
            if (zoomMode) {
                setZoomMode(false);
                updateZoomMode();
                return;
            }
            if (!(src instanceof ChartPanel) || markArea == null) {
                return;
            }
            ChartPanel chart = (ChartPanel) src;

            Graphics2D g2 = (Graphics2D) chart.getGraphics();
            g2.setXORMode(java.awt.Color.YELLOW);

            if (markArea != null) {
                g2.draw(markArea);
            }

            Rectangle2D plotArea = chart.getScreenDataArea(
                    (int) markStart.getX(), (int) markStart.getY());

            double xright = Math.min(e.getX(), plotArea.getMaxX());
            markArea = new Rectangle2D.Double(
                    markStart.getX(), plotArea.getMinY(),
                    xright - markStart.getX(), plotArea.getHeight());

            XYPlot plot = chart.getChart().getXYPlot();
            ValueAxis xaxis = plot.getDomainAxis();
            double xminfrac = (markArea.getMinX() - plotArea.getMinX()) / plotArea.getWidth();
            double xmaxfrac = (markArea.getMaxX() - plotArea.getMinX()) / plotArea.getWidth();
            double xmin = xminfrac * (xaxis.getUpperBound() - xaxis.getLowerBound()) + xaxis.getLowerBound();
            double xmax = xmaxfrac * (xaxis.getUpperBound() - xaxis.getLowerBound()) + xaxis.getLowerBound();

            markXRange(xmin, xmax);
            markStart = null;
            markArea = null;
        }
    }

    //=======================================================================
    // FILE MENU Action Handlers
    //=======================================================================
    @Action
    public void newFile() {
        setFileOpen(true);
    }

    @Action
    public void openFile() {
        if (modified) {
            JOptionPane op = new JOptionPane();
            int choice = JOptionPane.showConfirmDialog(null, "The file was not saved. Replace anyway?", appName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        JFileChooser fc = new JFileChooser(currentPath);
        fc.setAcceptAllFileFilterUsed(true);
        for (SeriesFileHandler handler : seriesReaders) {
            fc.addChoosableFileFilter(handler.getFilter());
        }
        fc.setFileFilter(currentFileType != null ? currentFileType : fc.getAcceptAllFileFilter());

        //fc.setFileHidingEnabled(false);
        fc.setFileHidingEnabled(true);
        if (fc.showOpenDialog(view.getFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        openData(fc.getSelectedFile(), fc.getFileFilter());
    }

    @Action(enabledProperty = "fileOpen")
    public void saveFile() {
        if (data == null) {
            return;
        }
        if (currentFile == null) {
            return;
        }
        saveData(currentFile, currentFileType);
    }

    @Action(enabledProperty = "fileOpen")
    public void saveFileAs() {
        if (data == null) {
            return;
        }
        JFileChooser fc;
        fc = new JFileChooser();
        if (currentPath != null) {
            fc.setCurrentDirectory(currentPath);
        }
        fc.setAcceptAllFileFilterUsed(false);
        for (SeriesFileHandler handler : seriesWriters) {
            fc.addChoosableFileFilter(handler.getFilter());
        }
        fc.setFileFilter(currentFileType);
        fc.setFileHidingEnabled(false);
        fc.setSelectedFile(SeriesFileFilter.removeExtension(currentFile));
        if (fc.showSaveDialog(view.getFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        FileFilter filter = fc.getFileFilter();
        if (filter instanceof SeriesFileFilter) {
            file = ((SeriesFileFilter) filter).forceExtension(file);
        }
        saveData(file, filter);
    }

    @Action(enabledProperty = "fileOpen")
    public void closeFile() {
        if (modified) {
            JOptionPane op = new JOptionPane();
            int choice = JOptionPane.showConfirmDialog(null, "The file was not saved. Close anyway?", appName, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        closeData();
    }

    @Action
    public void exitFile() {
        exit();
    }

    //=======================================================================
    // EDIT MENU Action Handlers
    //=======================================================================
    @Action(enabledProperty = "undoAvailable")
    public void undo() {
        data.removeAllSeries();
        for (Series ser : undoBuffer.getSeries()) {
            data.addSeries(ser);
        }
        undoBuffer = null;
        mainChart.fireChartChanged();
        setUndoAvailable(false);
        updateTitle();
        dataListComponent.repaint();
    }

    @Action
    public void redo() {
    }

    @Action(enabledProperty = "fileOpen")
    public void cut() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(selection, this);
        setPasteAvailable(true);
        data.removeSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void copy() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        clip.setContents(selection, this);
        setPasteAvailable(true);
    }

    @Action(enabledProperty = "pasteAvailable")
    public void paste() {
        if (data == null) {
            return;
        }
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transfer = clip.getContents(this);
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        startModify();
        try {
            int oldCount = data.getSeriesCount();
            dataListComponent.clearSelection();
            data.insertTransferData(insertIndex + 1, transfer);
            int newCount = data.getSeriesCount();
            dataListComponent.setSelectionInterval(insertIndex + 1, insertIndex + newCount - oldCount);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        }
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void delete() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        data.removeSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void insertFile() {
        // TODO: Update insertFile with common fileOpen method
        if (data == null) {
            return;
        }

        JFileChooser fc = new JFileChooser(currentPath);
        for (SeriesFileHandler handler : seriesReaders) {
            fc.addChoosableFileFilter(handler.getFilter());
        }
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileHidingEnabled(false);
        if (fc.showOpenDialog(view.getFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File newfile = fc.getSelectedFile();
        SeriesList newdata = readSeriesFile(newfile, fc.getFileFilter());

        if (newdata == null) {
            JOptionPane.showMessageDialog(view.getFrame(), "Unable to open file\n" + newfile.getName(), appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        startModify();
        int oldCount = data.getSeriesCount();
        dataListComponent.clearSelection();
        insertIndex++;
        for (int i = 0; i < newdata.getSeriesCount(); i++) {
            data.addSeries(insertIndex++, newdata.getSeries(i));
        }
        int newCount = data.getSeriesCount();
        dataListComponent.setSelectionInterval(insertIndex + 1, insertIndex + newCount - oldCount);
        endModify();
        currentPath = new File(newfile.getParent());
    }

    @Action(enabledProperty = "fileOpen")
    public void addSeparator() {
        if (data == null) {
            return;
        }
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        startModify();
        data.addSeries(insertIndex + 1, new Series("", 0));
        endModify();
    }
    String lastFind = "";
    boolean regularExpression = false;
    boolean searchSelection = false;

    @Action(enabledProperty = "fileOpen")
    public void findNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        boolean hasSelection = selection.getSeriesCount() > 0;
        String[] findS = {lastFind};
        Boolean[] reB = {new Boolean(regularExpression)};
        Boolean[] inselB = {new Boolean(searchSelection)};

        GenericInputDialog gd = new GenericInputDialog(this, null, "Find");
        gd.addString("Find what:", findS, 30);
        gd.addBoolean("Regular expressions:", reB, false);
        if (hasSelection) {
            gd.addBoolean("Search selection:", inselB, false);
        }
        if (gd.showDialog()) {
            lastFind = findS[0];
            regularExpression = reB[0];
            searchSelection = inselB[0];
            SeriesList toSearch = (hasSelection && inselB[0]) ? selection : data;
            SeriesList found = new SeriesList();
            Pattern p = reB[0] ? Pattern.compile(findS[0]) : null;
            for (Series ser : toSearch.getSeries()) {
                if (reB[0]) {
                    if (p.matcher(ser.getName()).find()) {
                        found.addSeries(ser);
                    }
                } else {
                    if (ser.getName().contains(findS[0])) {
                        found.addSeries(ser);
                    }
                }
            }
            setSelectedSeries(found);
        }
    }
    String lastReplace = "";

    @Action(enabledProperty = "fileOpen")
    public void replaceNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        boolean hasSelection = selection.getSeriesCount() > 0;
        String[] findS = {lastFind};
        String[] replS = {lastReplace};
        Boolean[] reB = {new Boolean(regularExpression)};
        Boolean[] inselB = {new Boolean(searchSelection)};

        GenericInputDialog gd = new GenericInputDialog(this, null, "Replace");
        gd.addString("Find what:", findS, 30);
        gd.addString("Replace with:", replS, 30);
        gd.addBoolean("Regular expressions:", reB, false);
        if (hasSelection) {
            gd.addBoolean("Search selection:", inselB, false);
        }
        if (gd.showDialog()) {
            startModify();
            lastFind = findS[0];
            lastReplace = replS[0];
            regularExpression = reB[0];
            searchSelection = inselB[0];
            SeriesList toSearch = (hasSelection && inselB[0]) ? selection : data;
            SeriesList found = new SeriesList();
            Pattern p = reB[0] ? Pattern.compile(findS[0]) : null;
            String oldName, newName;
            for (Series ser : toSearch.getSeries()) {
                oldName = ser.getName();
                if (reB[0]) {
                    newName = oldName.replaceAll(findS[0], replS[0]);
                } else {
                    newName = oldName.replace(findS[0], replS[0]);
                }
                if (!newName.equals(oldName)) {
                    ser.setName(newName);
                    found.addSeries(ser);
                }
            }
            endModify();
            setSelectedSeries(found);
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void renameSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() != 1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select one series to rename", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        Series ser = selection.getSeries(0);
        String[] aname = {ser.getName()};
        GenericInputDialog gd = new GenericInputDialog(this, null, "Rename series");
        gd.addString("New name:", aname, 15);
        if (gd.showDialog()) {
            startModify();
            ser.setName(aname[0]);
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void prefixNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        String[] prefix = {"Prefix-"};
        GenericInputDialog gd = new GenericInputDialog(this, "Prepend to series names", appName);
        gd.addString("Prefix:", prefix, 10);
        if (gd.showDialog()) {
            startModify();
            for (Series ser : selection.getSeries()) {
                ser.setName(prefix[0] + ser.getName());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void suffixNames() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        String[] suffix = {"-Suffix"};
        GenericInputDialog gd = new GenericInputDialog(this, "Append to series names", appName);
        gd.addString("Suffix:", suffix, 10);
        if (gd.showDialog()) {
            startModify();
            for (Series ser : selection.getSeries()) {
                ser.setName(ser.getName() + suffix[0]);
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void selectAllSeries() {
        dataListComponent.setSelectionInterval(0, data.getSeriesCount() - 1);
    }

    @Action(enabledProperty = "fileOpen")
    public void sortSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        data.removeSeries(selection.getSeries());
        selection.sortByName();
        data.addAllSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void sortSeriesValue() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        startModify();
        data.removeSeries(selection.getSeries());
        selection.sortByNamesValue();
        data.addAllSeries(selection.getSeries());
        dataListComponent.clearSelection();
        endModify();
    }

    @Action
    public void preferences() {
    }

    //=======================================================================
    // VIEW MENU Action Handlers
    //=======================================================================
    @Action(enabledProperty = "fileOpen")
    public void toggleSeries() {
        int[] aiSelected = dataListComponent.getSelectedIndices();
        for (int index : aiSelected) {
            boolean visible = mainChartRenderer.isSeriesVisible(index);
            Boolean bv = new Boolean(!visible);
            mainChartRenderer.setSeriesVisible(index, bv);
        }
        dataListComponent.repaint();
    }

    @Action(enabledProperty = "fileOpen")
    public void setXAxis() {
        if (data == null || mainChart == null) {
            return;
        }
        ValueAxis axis = mainChart.getXYPlot().getDomainAxis();
        Double[] minD = {new Double(Math.rint(axis.getLowerBound() * 10) / 10)};
        Double[] maxD = {new Double(Math.rint(axis.getUpperBound() * 10) / 10)};
        GenericInputDialog gd = new GenericInputDialog(this, "X range to display", appName);
        gd.addDouble("Min x:", minD, 8);
        gd.addDouble("Max x:", maxD, 8);
        if (gd.showDialog()) {
            axis.setRange(minD[0], maxD[0]);
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void setYAxis() {
        if (data == null || mainChart == null) {
            return;
        }
        ValueAxis axis = mainChart.getXYPlot().getRangeAxis();
        Double[] minD = {new Double(Math.rint(axis.getLowerBound() * 10) / 10)};
        Double[] maxD = {new Double(Math.rint(axis.getUpperBound() * 10) / 10)};
        GenericInputDialog gd = new GenericInputDialog(this, "Y range to display", appName);
        gd.addDouble("Min y:", minD, 8);
        gd.addDouble("Max y:", maxD, 8);
        if (gd.showDialog()) {
            axis.setRange(minD[0], maxD[0]);
        }
    }

    @Action(enabledProperty = "fileOpen", selectedProperty = "zoomMode")
    public void zoom() {
        updateZoomMode();
    }

    @Action(enabledProperty = "fileOpen")
    public void zoomIn() {
        if (mainChartPanel == null) {
            return;
        }
        Rectangle2D plotArea = mainChartPanel.getScreenDataArea();
        mainChartPanel.setZoomInFactor(0.8);
        mainChartPanel.zoomInBoth(plotArea.getCenterX(), plotArea.getCenterY());
    }

    @Action(enabledProperty = "fileOpen")
    public void zoomOut() {
        if (mainChartPanel == null) {
            return;
        }
        Rectangle2D plotArea = mainChartPanel.getScreenDataArea();
        mainChartPanel.setZoomOutFactor(1 / 0.8);
        mainChartPanel.zoomOutBoth(plotArea.getCenterX(), plotArea.getCenterY());
    }

    @Action(enabledProperty = "fileOpen")
    public void zoomExtents() {
        if (mainChartPanel == null) {
            return;
        }
        mainChartPanel.restoreAutoBounds();
    }

    @Action(enabledProperty = "fileOpen")
    public void selectRange() {
        if (data == null) {
            return;
        }
        double[] range = getMarkedXRange();
        if (range == null) {
            range = data.getXRange();
        }
        if (range[1] <= range[0]) {
            clearMarkedXRange();
        }
        Double[] xminD = {new Double(Math.rint(range[0] * 10) / 10)};
        Double[] xmaxD = {new Double(Math.rint(range[1] * 10) / 10)};
        GenericInputDialog gd = new GenericInputDialog(this, "Select range", appName);
        gd.addDouble("Min x:", xminD, 8);
        gd.addDouble("Max x:", xmaxD, 8);
        if (gd.showDialog()) {
            markXRange(xminD[0], xmaxD[0]);
        }
    }

    @Action(enabledProperty = "fileOpen", selectedProperty = "legendVisible")
    public void optShowLegend() {
        updateLegendVisible();
    }

    @Action(enabledProperty = "fileOpen")
    public void info() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        double[] xrange = selection.getXRange();
        double[] yrange = selection.getYRange();
        double delx = selection.getAverageDeltaX();
        int nPoints = 0;
        for (Series ser : selection.getSeries()) {
            nPoints += ser.getSize();
        }
        Object[][] table = {
            {"Description", "Minimum value", "Maximum value"},
            {"Series Count", new Integer(selection.getSeriesCount()), null},
            {"X range", new Double(xrange[0]), new Double(xrange[1])},
            {"Y range", new Double(yrange[0]), new Double(yrange[1])},
            {"Avg delta-x", new Double(delx), null},
            {"Total points", new Integer(nPoints), null}};
        new TableOutputDialog(view.getFrame(), "Series information", true, table).setVisible(true);
    }

    @Action(enabledProperty = "fileOpen")
    public void statistics() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int nSelected = selection.getSeriesCount();
        if (nSelected < 1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least one series", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ADD a series containing ALL of the selected points
        Series allData = new Series("ALL");
        for (Series ser : selection.getSeries()) {
            double[] range = getMarkedXRange();
            if (range == null) {
                range = ser.getXRange();
            }
            allData.addAll(ser.subSeries(range[0], range[1]).getPoints());
        }
        selection.addSeries(allData);

        List<Object> header = new ArrayList<Object>(nSelected + 2);
        List<StatTools.Stats> selectedStats = new ArrayList<StatTools.Stats>(nSelected + 1);
        header.add("Statistics");
        Map<String, List<Object>> dataRows = new HashMap<String, List<Object>>();
        for (String desc : StatTools.Stats.DESCS) {
            List<Object> row = new ArrayList<Object>(nSelected + 1);
            row.add(desc);
            dataRows.put(desc, row);
        }
        for (Series ser : selection.getSeries()) {
            header.add(ser.getName());
            Series subSeries;
            if (ser == allData) {
                subSeries = ser;
            } else {
                double[] range = getMarkedXRange();
                if (range == null) {
                    range = ser.getXRange();
                }
                subSeries = ser.subSeries(range[0], range[1]);
            }
            subSeries.removeNaN();

            if (subSeries.getSize() < 3) {
                // not enough points for statistics. fill rows with blanks
                for (String desc : StatTools.Stats.DESCS) {
                    dataRows.get(desc).add("-");
                }
                selectedStats.add(null);
            } else {
                StatTools.Stats stats = StatTools.calcStats(subSeries.getYArray());
                //System.out.println(stats.toString());
                dataRows.get(stats.DESC_DOF).add(new Integer(stats.DOF));
                dataRows.get(stats.DESC_sum).add(new Double(stats.sum));
                dataRows.get(stats.DESC_sumSq).add(new Double(stats.sumSq));
                dataRows.get(stats.DESC_min).add(new Double(stats.min));
                dataRows.get(stats.DESC_max).add(new Double(stats.max));
                dataRows.get(stats.DESC_avg).add(new Double(stats.avg));
                dataRows.get(stats.DESC_avgDev).add(new Double(stats.avgDev));
                dataRows.get(stats.DESC_stdDev).add(new Double(stats.stdDev));
                dataRows.get(stats.DESC_var).add(new Double(stats.var));
                dataRows.get(stats.DESC_skew).add(new Double(stats.skew));
                dataRows.get(stats.DESC_kurt).add(new Double(stats.kurt));
                selectedStats.add(stats);
            }
        }

        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add(header);
        for (String key : StatTools.Stats.DESCS) {
            List<Object> statRow = dataRows.get(key);
            data.add(statRow);
        }

        List<XYLineAnnotation> annotations = new ArrayList<XYLineAnnotation>();
        XYPlot plot = null;
        if (mainChart != null) {
            BasicStroke thinStroke = new BasicStroke(0.5f);
            BasicStroke dashStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{20f, 10f}, 0.0f);
            plot = mainChart.getXYPlot();
            ValueAxis xaxis = plot.getDomainAxis();
            for (int i = 0; i < nSelected + 1; i++) {
                StatTools.Stats stats = selectedStats.get(i);
                if (stats != null) {
                    java.awt.Stroke stroke;
                    java.awt.Color color;
                    Series ser = selection.getSeries(i);
                    if (ser == allData) {
                        color = java.awt.Color.BLACK;
                        stroke = dashStroke;
                    } else {
                        color = ser.getColor();
                        stroke = thinStroke;
                    }
                    XYLineAnnotation line = new XYLineAnnotation(
                            xaxis.getLowerBound(), stats.avg,
                            xaxis.getUpperBound(), stats.avg,
                            stroke, color);
                    plot.addAnnotation(line);
                    annotations.add(line);
                }
            }
        }
        new TableOutputDialog(view.getFrame(), "Series Statistics", true, data).setVisible(true);
        if (mainChart != null) {
            for (XYLineAnnotation line : annotations) {
                plot.removeAnnotation(line);
            }
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void linearFit() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int nSelected = selection.getSeriesCount();
        if (nSelected < 1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least one series", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ADD a series containing ALL of the selected points
        Series allData = new Series("ALL");
        for (Series ser : selection.getSeries()) {
            double[] range = getMarkedXRange();
            if (range == null) {
                range = ser.getXRange();
            }
            allData.addAll(ser.subSeries(range[0], range[1]).getPoints());
        }
        selection.addSeries(allData);

        List<Object> header = new ArrayList<Object>(nSelected + 2);
        List<StatTools.LinFit> selectedFits = new ArrayList<StatTools.LinFit>(nSelected + 1);
        header.add("Linear Fit");
        Map<String, List<Object>> dataRows = new HashMap<String, List<Object>>();
        for (String desc : StatTools.LinFit.DESCS) {
            List<Object> row = new ArrayList<Object>(nSelected + 2);
            row.add(desc);
            dataRows.put(desc, row);
        }
        for (Series ser : selection.getSeries()) {
            header.add(ser.getName());
            Series subSeries;
            if (ser == allData) {
                subSeries = ser;
            } else {
                double[] range = getMarkedXRange();
                if (range == null) {
                    range = ser.getXRange();
                }
                subSeries = ser.subSeries(range[0], range[1]);
            }
            subSeries.removeNaN();

            if (subSeries.getSize() < 2) {
                // not enough points for statistics. fill rows with blanks
                for (String desc : StatTools.LinFit.DESCS) {
                    dataRows.get(desc).add("-");
                }
                selectedFits.add(null);
            } else {
                double[] xvals = subSeries.getXArray();
                double[] yvals = subSeries.getYArray();
                StatTools.LinFit fit = StatTools.calcLinFit(xvals, yvals, null, false);
                //System.out.println(fit.toString());
                dataRows.get(fit.DESC_DOF).add(new Integer(fit.DOF));
                dataRows.get(fit.DESC_inter).add(new Double(fit.inter));
                dataRows.get(fit.DESC_interStdDev).add(new Double(fit.interStdDev));
                dataRows.get(fit.DESC_interTStat).add(new Double(fit.interTStat));
                dataRows.get(fit.DESC_slope).add(new Double(fit.slope));
                dataRows.get(fit.DESC_slopeStdDev).add(new Double(fit.slopeStdDev));
                dataRows.get(fit.DESC_slopeTStat).add(new Double(fit.slopeTStat));
                dataRows.get(fit.DESC_RSq).add(new Double(fit.RSq));
                dataRows.get(fit.DESC_ChiSq).add(new Double(fit.ChiSq));
                dataRows.get(fit.DESC_stdErrEst).add(new Double(fit.stdErrEst));
                dataRows.get(fit.DESC_Q).add(new Double(fit.Q));
                dataRows.get(fit.DESC_nvar).add(new Double(fit.nvar));
                dataRows.get(fit.DESC_sumX).add(new Double(fit.sumX));
                dataRows.get(fit.DESC_sumXSq).add(new Double(fit.sumXSq));
                selectedFits.add(fit);
            }
        }

        List<List<Object>> data = new ArrayList<List<Object>>();
        data.add(header);
        for (String key : StatTools.LinFit.DESCS) {
            List<Object> statRow = dataRows.get(key);
            data.add(statRow);
        }

        List<XYLineAnnotation> annotations = new ArrayList<XYLineAnnotation>();
        XYPlot plot = null;
        if (mainChart != null) {
            BasicStroke thinStroke = new BasicStroke(0.5f);
            BasicStroke dashStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{20f, 10f}, 0.0f);
            plot = mainChart.getXYPlot();
            ValueAxis xaxis = plot.getDomainAxis();
            for (int i = 0; i < nSelected + 1; i++) {
                StatTools.LinFit fit = selectedFits.get(i);
                if (fit != null) {
                    java.awt.Stroke stroke;
                    java.awt.Color color;
                    Series ser = selection.getSeries(i);
                    if (ser == allData) {
                        color = java.awt.Color.BLACK;
                        stroke = dashStroke;
                    } else {
                        color = ser.getColor();
                        stroke = thinStroke;
                    }
                    double xmin = xaxis.getLowerBound();
                    double xmax = xaxis.getUpperBound();

                    XYLineAnnotation line = new XYLineAnnotation(
                            xmin, xmin * fit.slope + fit.inter,
                            xmax, xmax * fit.slope + fit.inter,
                            stroke, color);

                    plot.addAnnotation(line);
                    annotations.add(line);
                }
            }
        }
        new TableOutputDialog(view.getFrame(), "Series Linear Fit", true, data).setVisible(true);
        if (mainChart != null) {
            for (XYLineAnnotation line : annotations) {
                plot.removeAnnotation(line);
            }
        }
    }
    //=======================================================================
    // TRANSFORM MENU Action Handlers
    //=======================================================================
    double keepRangeMin = 1000;
    double keepRangeMax = 300000;

    @Action(enabledProperty = "fileOpen")
    public void keepYRange() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] yminD = {new Double(keepRangeMin)};
        Double[] ymaxD = {new Double(keepRangeMax)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Y range to keep", appName);
        gd.addDouble("Min y:", yminD, 10);
        gd.addDouble("Max y:", ymaxD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            keepRangeMin = yminD[0];
            keepRangeMax = ymaxD[0];
            replace = replaceB[0];
            rename = renameB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.keepYRange(keepRangeMin, keepRangeMax, true);
                if (rename) {
                    ser.setName("yrange(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    int smoothPoints = 81;
    int smoothOrder = 2;
    boolean leftBias = false;

    @Action(enabledProperty = "fileOpen")
    public void smooth() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }

        Integer[] npointsI = {new Integer(smoothPoints)};
        Integer[] orderI = {new Integer(smoothOrder)};
        Boolean[] leftBiasB = {new Boolean(leftBias)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Savitzky-Golay Smoothing", appName);
        gd.addInteger("number of points:", npointsI, 4);
        gd.addInteger("polynomial order:", orderI, 2);
        gd.addBoolean("Bias to left", leftBiasB, true);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            smoothPoints = npointsI[0];
            leftBias = leftBiasB[0];
            int smoothL, smoothR;
            if (leftBias) {
                smoothL = 0;
                smoothR = 2 * (smoothPoints / 2);
            } else {
                smoothL = smoothPoints / 2;
                smoothR = smoothPoints / 2;
            }
            smoothPoints = smoothL + 1 + smoothR;
            smoothOrder = orderI[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.smoothSavitzkyGolay(smoothL, smoothR, smoothOrder, 0);
                if (rename) {
                    ser.setName("smooth(" + smoothPoints + ";" + smoothOrder + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    int derivOrder = 4;

    @Action(enabledProperty = "fileOpen")
    public void derivative() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }

        Integer[] npointsI = {new Integer(smoothPoints)};
        Integer[] orderI = {new Integer(derivOrder)};
        Boolean[] leftBiasB = {new Boolean(leftBias)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Savitzky-Golay Derivative", appName);
        gd.addInteger("number of points:", npointsI, 4);
        gd.addInteger("polynomial order:", orderI, 2);
        gd.addBoolean("Bias to left", leftBiasB, true);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            smoothPoints = npointsI[0];
            leftBias = leftBiasB[0];
            int smoothL, smoothR;
            if (leftBias) {
                smoothL = 0;
                smoothR = 2 * (smoothPoints / 2);
            } else {
                smoothL = smoothPoints / 2;
                smoothR = smoothPoints / 2;
            }
            smoothPoints = smoothL + 1 + smoothR;
            derivOrder = orderI[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.smoothSavitzkyGolay(smoothL, smoothR, derivOrder, 1);
                if (rename) {
                    ser.setName("d/dx(" + smoothPoints + ";" + derivOrder + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double scaleYFactor = 2;

    @Action(enabledProperty = "fileOpen")
    public void scaleY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] scaleD = {new Double(scaleYFactor)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Scale Y values", appName);
        gd.addDouble("Scale factor:", scaleD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            scaleYFactor = scaleD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.scaleY(scaleYFactor);
                if (rename) {
                    ser.setName("" + scaleYFactor + "*(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double offsetY = 1000;

    @Action(enabledProperty = "fileOpen")
    public void offsetY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] offsetD = {new Double(offsetY)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Offset Y values", appName);
        gd.addDouble("Offset:", offsetD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            offsetY = offsetD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            String soff = offsetY < 0 ? "" + offsetY : "+" + offsetY;
            for (Series ser : slist.getSeries()) {
                ser.addY(offsetY);
                if (rename) {
                    ser.setName(ser.getName() + soff);
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double newYIntercept = 0;

    @Action(enabledProperty = "fileOpen")
    public void adjustYIntercept() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] newYInterceptD = {new Double(newYIntercept)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Adjust Y intercept", appName);
        gd.addDouble("New Intercept:", newYInterceptD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            newYIntercept = newYInterceptD[0];
            double[] range = getMarkedXRange();
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                Series subSer = ser.subSeries(range[0], range[1]);
                subSer.removeNaN();
                if (subSer.getSize() < 2) {
                    // can't fit if too few points
                    continue;
                }
                StatTools.LinFit fit = StatTools.calcLinFit(subSer.getXArray(), subSer.getYArray(), null, false);
                double offset = newYIntercept - fit.inter;
                ser.addY(offset);
                String soff = offset < 0 ? "" + offset : "+" + offset;
                if (rename) {
                    ser.setName(ser.getName() + soff);
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void invertY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Invert Y values", appName);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.invertY();
                if (rename) {
                    ser.setName("1/(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double scaleXFactor = 2;
    double logYBase = 10;

    @Action(enabledProperty = "fileOpen")
    public void logY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] baseD = {new Double(logYBase)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Take logN(Y)", appName);
        gd.addDouble("Base (N):", baseD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            logYBase = baseD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.logY(logYBase);
                if (rename) {
                    ser.setName("log" + logYBase + "(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double antilogYBase = 10;

    @Action(enabledProperty = "fileOpen")
    public void antilogY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] baseD = {new Double(antilogYBase)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Take Base^Y", appName);
        gd.addDouble("Base (N):", baseD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            antilogYBase = baseD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.antilogY(antilogYBase);
                if (rename) {
                    ser.setName("" + antilogYBase + "^(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double powYFactor = 10;

    @Action(enabledProperty = "fileOpen")
    public void powY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] factD = {new Double(powYFactor)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Take Y^factor", appName);
        gd.addDouble("Factor:", factD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            powYFactor = factD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.powY(powYFactor);
                if (rename) {
                    ser.setName("(" + ser.getName() + ")^" + powYFactor);
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void atanY() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Arctan (Y)", appName);
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.atanY();
                if (rename) {
                    ser.setName("atan(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void scaleX() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] scaleD = {new Double(scaleXFactor)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Scale X values", appName);
        gd.addDouble("Scale factor:", scaleD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            scaleXFactor = scaleD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.scaleX(scaleXFactor);
                if (rename) {
                    ser.setName("(x*" + scaleXFactor + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    double offsetX = 100;

    @Action(enabledProperty = "fileOpen")
    public void offsetX() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        Double[] offsetD = {new Double(offsetX)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Offset X values", appName);
        gd.addDouble("Offset:", offsetD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            offsetX = offsetD[0];
            String soff = offsetX < 0 ? "" + offsetX : "+" + offsetX;
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                ser.addX(offsetX);
                if (rename) {
                    ser.setName("(x" + soff + ";" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }
    boolean keepXVals = false;

    @Action(enabledProperty = "fileOpen")
    public void resampleX() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double olddelx = delx;
        Double[] xminD = {new Double(Math.floor(xmin))};
        Double[] xmaxD = {new Double(Math.ceil(xmax))};
        Double[] delxD = {new Double(Math.rint(delx * 10) / 10)};
        Boolean[] keepXB = {new Boolean(keepXVals)};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Resample", appName);
        gd.addDouble("Min x:", xminD, 10);
        gd.addDouble("Max x:", xmaxD, 10);
        gd.addDouble("Delta-x:", delxD, 8);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        gd.addSeparator("Special Option");
        gd.addBoolean("Keep existing X's", keepXB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            xmin = xminD[0];
            xmax = xmaxD[0];
            delx = delxD[0];
            keepXVals = keepXB[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                if (keepXVals) {
                    Series newser = (Series) ser.clone();
                    newser.resample(xmin, xmax, delx);
                    double eps = olddelx / 10;
                    for (KPoint2D ptser : ser.getPoints()) {
                        double y = Double.NaN;
                        for (KPoint2D ptnew : newser.getPoints()) {
                            if (Math.abs(ptnew.x - ptser.x) < eps) {
                                y = ptnew.y;
                            }
                        }
                        ptser.y = y;
                    }
                } else {
                    ser.resample(xmin, xmax, delx);
                }
                if (rename) {
                    ser.setName("resampled(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void removeXRange() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        if (selection.getSeriesCount() == 0) {
            return;
        }
        double[] xrange = getMarkedXRange();
        if (xrange == null) {
            xrange = selection.getXRange();
        }
        double xmin = xrange[0], xmax = xrange[1];
        Double[] xminD = {new Double(Math.floor(xmin))};
        Double[] xmaxD = {new Double(Math.ceil(xmax))};
        Boolean[] replaceB = {new Boolean(replace)};
        Boolean[] renameB = {new Boolean(rename)};
        GenericInputDialog gd = new GenericInputDialog(this, "Remove points in range", appName);
        gd.addDouble("Min x:", xminD, 10);
        gd.addDouble("Max x:", xmaxD, 10);
        gd.addSeparator("Options");
        gd.addBoolean("Replace values:", replaceB, false);
        gd.addBoolean("Rename series:", renameB, false);
        if (gd.showDialog()) {
            startModify();
            replace = replaceB[0];
            rename = renameB[0];
            xmin = xminD[0];
            xmax = xmaxD[0];
            SeriesList slist = replace ? selection : (SeriesList) selection.clone();
            for (Series ser : slist.getSeries()) {
                Series left = ser.subSeries(Double.MIN_VALUE, xmin);
                Series right = ser.subSeries(xmax, Double.MAX_VALUE);
                ser.clearPoints();
                if (left != null) {
                    ser.addAll(left.getPoints());
                }
                if (right != null) {
                    ser.addAll(right.getPoints());
                }
                if (rename) {
                    ser.setName("xrange(" + ser.getName() + ")");
                }
            }
            if (!replace) {
                data.addAllSeries(slist.getSeries());
            }
            endModify();
        }
    }

    @Action(enabledProperty = "fileOpen")
    public void addSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries == 0) {
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += "+";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = 0;
            for (int i = 0; i < nSeries; i++) {
                y += selection.getSeries(i).interpolateY(x);
            }
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void subtractSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries != 2) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select exactly two series to subtract.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        Series sA = selection.getSeries(0);
        Series sB = selection.getSeries(1);
        String name = "(" + sA.getName() + "-" + sB.getName() + ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = sA.interpolateY(x) - sB.interpolateY(x);
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void multiplySeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries == 0) {
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += "*";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = 1;
            for (int i = 0; i < nSeries; i++) {
                y *= selection.getSeries(i).interpolateY(x);
            }
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        this.dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void divideSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries != 2) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select exactly two series to divide.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        Series sA = selection.getSeries(0);
        Series sB = selection.getSeries(1);
        String name = "(" + sA.getName() + "/" + sB.getName() + ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double N = (xmax - xmin) / delx + 1;
        int nPoints = (int) N;
        if (Double.isNaN(N) || N < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double y = sA.interpolateY(x) / sB.interpolateY(x);
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        this.dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void averageSereies() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries == 0) {
            return;
        }
        String name = "avg(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += ";";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double M = (xmax - xmin) / delx + 1;
        int nPoints = (int) M;
        if (Double.isNaN(M) || M < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> points = new ArrayList<KPoint2D>(nPoints);
        for (double x = xmin; x <= xmax; x += delx) {
            double sum = 0;
            for (int i = 0; i < nSeries; i++) {
                sum += selection.getSeries(i).interpolateY(x);
            }
            double y = sum / nSeries;
            points.add(new KPoint2D(x, y));
        }
        Series dest = new Series(name, points);
        dest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, dest);
        this.dataListComponent.setSelectedIndex(insertIndex + 1);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void avgSDSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries < 3) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least 3 series for standard deviation.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += ";";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double M = (xmax - xmin) / delx + 1;
        int nPoints = (int) M;
        if (Double.isNaN(M) || M < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> avgpoints = new ArrayList<KPoint2D>(nPoints);
        List<KPoint2D> sdpoints = new ArrayList<KPoint2D>(nPoints);
        double[] yvals = new double[nSeries];
        StatTools.Stats stats;
        for (double x = xmin; x <= xmax; x += delx) {
            for (int i = 0; i < nSeries; i++) {
                yvals[i] = selection.getSeries(i).interpolateY(x);
            }
            stats = StatTools.calcStats(yvals);
            avgpoints.add(new KPoint2D(x, stats.avg));
            sdpoints.add(new KPoint2D(x, stats.stdDev));
        }
        Series avgdest = new Series("avg" + name, avgpoints);
        Series sddest = new Series("sd" + name, sdpoints);
        avgdest.removeNaN();
        sddest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, avgdest);
        data.addSeries(insertIndex + 2, sddest);
        int[] selind = {insertIndex + 1, insertIndex + 2};
        this.dataListComponent.setSelectedIndices(selind);
        endModify();
    }

    @Action(enabledProperty = "fileOpen")
    public void avgPlusSDSeries() {
        if (data == null) {
            return;
        }
        SeriesList selection = getSelectedSeries();
        int insertIndex = getLastSelectedIndex();
        if (insertIndex == -1) {
            insertIndex = data.getSeriesCount() - 1;
        }
        int nSeries = selection.getSeriesCount();
        if (nSeries < 3) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select at least 3 series for standard deviation.", appName, JOptionPane.ERROR_MESSAGE);
            return;
        }
        String name = "(";
        for (int i = 0; i < nSeries; i++) {
            name += selection.getSeries(i).getName();
            if (i < nSeries - 1) {
                name += ";";
            }
        }
        name += ")";
        double[] xrange = selection.getXRange();
        double xmin = xrange[0], xmax = xrange[1];
        double delx = selection.getAverageDeltaX();
        double M = (xmax - xmin) / delx + 1;
        int nPoints = (int) M;
        if (Double.isNaN(M) || M < 0 || nPoints == 0) {
            return;
        }
        List<KPoint2D> avgminussdpoints = new ArrayList<KPoint2D>(nPoints);
        List<KPoint2D> avgpoints = new ArrayList<KPoint2D>(nPoints);
        List<KPoint2D> avgplussdpoints = new ArrayList<KPoint2D>(nPoints);
        double[] yvals = new double[nSeries];
        StatTools.Stats stats;
        for (double x = xmin; x <= xmax; x += delx) {
            for (int i = 0; i < nSeries; i++) {
                yvals[i] = selection.getSeries(i).interpolateY(x);
            }
            stats = StatTools.calcStats(yvals);
            avgminussdpoints.add(new KPoint2D(x, stats.avg - stats.stdDev));
            avgpoints.add(new KPoint2D(x, stats.avg));
            avgplussdpoints.add(new KPoint2D(x, stats.avg + stats.stdDev));
        }
        Series avgminussddest = new Series("avg-sd" + name, avgminussdpoints);
        Series avgdest = new Series("avg" + name, avgpoints);
        Series avgplussddest = new Series("avg+sd" + name, avgplussdpoints);
        avgminussddest.removeNaN();
        avgdest.removeNaN();
        avgplussddest.removeNaN();
        startModify();
        data.addSeries(insertIndex + 1, avgplussddest);
        data.addSeries(insertIndex + 2, avgdest);
        data.addSeries(insertIndex + 3, avgminussddest);
        int[] selind = {insertIndex + 1, insertIndex + 2, insertIndex + 3};
        this.dataListComponent.setSelectedIndices(selind);
        endModify();
    }

    //      adjustYIntercept invertY --- scaleX offsetX resampleX removeXRange \
    //      seriesMathMenu.items = addSeries subtractSeries multiplySeries divideSeries \
    //      averageSereies avgSDSeries avgPlusSDSeries
    //=======================================================================
    // HELP MENU Action Handlers
    //=======================================================================
    @Action
    public void about() {
        if (aboutBox == null) {
            JFrame mainFrame = view.getFrame();
            aboutBox = new TrixyAboutBox(mainFrame, false);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        show(aboutBox);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        SplashScreen splash = SplashScreen.getSplashScreen();
//        if (splash != null) {
//            if (false) {
//                Graphics2D g = (Graphics2D) splash.createGraphics();
//                Dimension dim = splash.getSize();
//                // Simulate loading
//                final int STEPS = 3;
//                final int SLEEP = 300;
//                final int YPOS = 250;
//                final int HEIGHT = 5;
//                for (int i = 0; i <= STEPS; i++) {
//                    g.setColor(Color.LIGHT_GRAY);
//                    g.fillRect(0, YPOS, dim.width, HEIGHT);
//                    g.setColor(Color.BLACK);
//                    g.fillRect(0, YPOS, i*dim.width/STEPS, HEIGHT);
//                    g.drawRect(0, YPOS, dim.width, HEIGHT);
//                    splash.update();
//                    try {
//                        Thread.sleep(SLEEP);
//                    } catch (InterruptedException e) {
//                    }
//                }
//            }
//        }
        launch(TrixyApp.class, args);
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

