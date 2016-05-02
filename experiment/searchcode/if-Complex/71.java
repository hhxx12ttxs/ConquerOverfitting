<<<<<<< HEAD
/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id: XMLInstance2SchemaHandler.java 7996 2008-12-16 08:25:44Z wguttmn $
 */

package org.exolab.castor.xml.schema.util;

import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import org.exolab.castor.xml.Namespaces;
import org.exolab.castor.xml.schema.AttributeDecl;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ContentType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Order;
import org.exolab.castor.xml.schema.Particle;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SchemaException;
import org.exolab.castor.xml.schema.Structure;
import org.exolab.castor.xml.schema.XMLType;
import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



/**
 * A Utility class which will attempt to create an XML Schema
 * Object Model based on a given XML instance document.
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision: 7996 $ $Date: 2006-04-25 15:08:23 -0600 (Tue, 25 Apr 2006) $ 
**/
public final class XMLInstance2SchemaHandler
    implements DocumentHandler, org.xml.sax.ErrorHandler
{


    private static final String XMLNS          = "xmlns";
    private static final String DEFAULT_PREFIX = "xsd";
      //--------------------/
     //- Member Variables -/
    //--------------------/
    
    /**
     * The schema we are creating
    **/
    private Schema _schema = null;

    /**
     * The stack of element declarations
    **/
    private Stack _siStack = null;
    
    private String _nsPrefix = null;
    
    private Order  _defaultGroupOrder = Order.sequence;
    
      //----------------/
     //- Constructors -/
    //----------------/

    /**
     * Creates a new XMLInstance2SchemaHandler
     *
    **/
    public XMLInstance2SchemaHandler() {
        this(null);
    } //-- XMLInstance2SchemaHandler

    /**
     * Creates a new XMLInstance2SchemaHandler
     *
    **/
    public XMLInstance2SchemaHandler(Schema schema) {
        super();
        
        _siStack   = new Stack();
        
        _schema = schema;
        //-- create Schema and initialize
        if (_schema == null) {
            _schema = new Schema();
            _schema.addNamespace(DEFAULT_PREFIX, Schema.DEFAULT_SCHEMA_NS);
            _nsPrefix = DEFAULT_PREFIX;
        }
        //-- find or declare namespace prefix
        else {
            _nsPrefix = null;
            Namespaces namespaces = _schema.getNamespaces();
            Enumeration enumeration = namespaces.getLocalNamespacePrefixes();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                if (namespaces.getNamespaceURI(key).equals(Schema.DEFAULT_SCHEMA_NS)) {
                    _nsPrefix = key;
                    break;
                }
            }
            if (_nsPrefix == null) {
                _schema.addNamespace(DEFAULT_PREFIX, Schema.DEFAULT_SCHEMA_NS);
                _nsPrefix = DEFAULT_PREFIX;
            }
        }
    } //-- XMLInstance2SchemaHandler

      //-----------/
     //- Methods -/
    //-----------/
    
    /**
     * Returns the XML Schema object that is being used by this handler
     *
     * @return the XML Schema object that is being used by this handler
    **/
    public Schema getSchema() {
        return _schema;
    }
    
    /**
     * This method is used to set the default group type. Either
     * "sequence" or "all". The default is "sequence".
     *
     * @param order the default group order to use.
    **/
    protected void setDefaultGroupOrder(Order order) {
        _defaultGroupOrder = order;
    } //-- setDefaultGroupOrder
      
    //---------------------------------------/
    //- org.xml.sax.DocumentHandler methods -/
    //---------------------------------------/
    
    public void characters(char[] ch, int start, int length) 
        throws org.xml.sax.SAXException
    {
        if (_siStack.isEmpty()) return;
        
        StateInfo sInfo = (StateInfo)_siStack.peek();
        
        if (sInfo.buffer == null) {
            sInfo.buffer = new StringBuffer();
        }
        sInfo.buffer.append(ch, start, length);
        
        if (sInfo.complex) {
            sInfo.mixed = true;    
        }
    } //-- characters
    
    public void endDocument()
        throws org.xml.sax.SAXException
    {
        //-- do nothing
        
    } //-- endDocument
    
    public void endElement(String name) 
        throws org.xml.sax.SAXException
    {
        
        //-- strip namespace prefix
        int idx = name.indexOf(':');
        if (idx >= 0) {
            name = name.substring(idx+1);
        }
        
        StateInfo sInfo = (StateInfo) _siStack.pop();
        
        //-- if we don't have a type, it means there are no
        //-- children and therefore the type is a simpleType or
        //-- simpleContent
        if ((sInfo.element.getType() == null) && (sInfo.buffer != null)) {
            
            //-- create SimpleType (guess type)
            String typeName = _nsPrefix + ':' + 
                DatatypeHandler.guessType(sInfo.buffer.toString());
            sInfo.element.setTypeReference(typeName);
            //-- simpleContent
            if (sInfo.attributes.size() > 0) {
                ComplexType cType = new ComplexType(_schema);
                //-- SHOULD CHANGE THIS TO SIMPLE CONTENT WHEN
                //-- SCHEMA WRITER BUGS ARE FIXED
                cType.setContentType(ContentType.mixed);
                sInfo.element.setType(cType);
                Group group = new Group();
                group.setOrder(_defaultGroupOrder);
                //-- add attributes
                try {
                    cType.addGroup(group);
                    for (int i = 0; i < sInfo.attributes.size(); i++) {
                        AttributeDecl attDecl = 
                            (AttributeDecl)sInfo.attributes.elementAt(i);
                        cType.addAttributeDecl(attDecl);
                    }
                }
                catch(SchemaException sx) {
                    throw new SAXException(sx);
                }
            }
        }
        else {
            ComplexType cType = (ComplexType)sInfo.element.getType();
            
            if ((cType == null) && (sInfo.attributes.size() > 0)) {
                cType = new ComplexType(_schema);
                sInfo.element.setType(cType);
                Group group = new Group();
                group.setOrder(_defaultGroupOrder);
                //-- add attributes
                try {
                    cType.addGroup(group);
                }
                catch(SchemaException sx) {
                    throw new SAXException(sx);
                }
            }
            
            if (cType != null) {
                for (int i = 0; i < sInfo.attributes.size(); i++) {
                    AttributeDecl attDecl = 
                        (AttributeDecl)sInfo.attributes.elementAt(i);
                    cType.addAttributeDecl(attDecl);
                }
            }
        }
        
        //-- put element into parent element or as top-level in schema
        if (!_siStack.isEmpty()) {
            StateInfo parentInfo = (StateInfo)_siStack.peek();
            ComplexType type = (ComplexType) parentInfo.element.getType();
            Group group = null;
            if ((type == null) || (type.getParticleCount() == 0)) {
                if (type == null) {
                    parentInfo.complex = true;
                    type = new ComplexType(_schema);
                    parentInfo.element.setType(type);
                }
                group = new Group();
                group.setOrder(_defaultGroupOrder);
                try {
                    type.addGroup(group);
                    //-- add element
                    group.addElementDecl(sInfo.element);
                }
                catch(SchemaException sx) {
                    throw new SAXException(sx);
                }
            }
            else {
                group = (Group) type.getParticle(0);
                //-- check for another element declaration with
                //-- same name ...
                ElementDecl element = group.getElementDecl(name);
                boolean checkGroupType = false;
                if (element != null) {
                    //-- if complex...merge definition
                    if (sInfo.complex) {
                        try {
                            merge(element, sInfo.element);
                        }
                        catch(SchemaException sx) {
                            throw new SAXException(sx);
                        }
                    }
                    element.setMaxOccurs(Particle.UNBOUNDED);
                    checkGroupType = true;
                }
                else {
                    try {
                        group.addElementDecl(sInfo.element);
                    }
                    catch(SchemaException sx) {
                        throw new SAXException(sx);
                    }
                }
                
                //-- change group type if necessary
                if (checkGroupType && (group.getOrder() == Order.sequence)) {
                    //-- make sure element is last item in group,
                    //-- otherwise we need to switch to all
                    boolean found = false;
                    boolean changeType = false;
                    for (int i = 0; i < group.getParticleCount(); i++) {
                        if (found) {
                            changeType = true;
                            break;
                        }
                        if (element == group.getParticle(i)) found = true;
                    }
                    if (changeType) {
                        group.setOrder(Order.all);
                    }
                }
            }
        }
        else {
            try {
                _schema.addElementDecl(sInfo.element);
                
                //-- make complexType top-level also
                //XMLType type = sInfo.element.getType();
                //if ((type != null) && (type.isComplexType())) {
                //    if (type.getName() == null) {
                //        type.setName(sInfo.element.getName() + "Type");
                //        _schema.addComplexType((ComplexType)type);
                //    }
                //}
            }
            catch(SchemaException sx) {
                throw new SAXException(sx);
            }
        }
        
    } //-- endElement


    public void ignorableWhitespace(char[] ch, int start, int length) 
        throws org.xml.sax.SAXException
    {
        //-- do nothing
        
    } //-- ignorableWhitespace

    public void processingInstruction(String target, String data) 
        throws org.xml.sax.SAXException
    {
        //-- do nothing

    } //-- processingInstruction
    
    public void setDocumentLocator(final Locator locator) { }
    
    public void startDocument()
        throws org.xml.sax.SAXException
    {
        //-- do nothing
        
    } //-- startDocument

    
    public void startElement(String name, AttributeList atts) 
        throws org.xml.sax.SAXException
    {
        
        //-- strip namespace prefix
        int idx = name.indexOf(':');
        if (idx >= 0) {
            name = name.substring(idx+1);
        }

        StateInfo sInfo = null;
        
        boolean topLevel = false;
        //-- if we are currently in another element 
        //-- definition...flag as complex content
        if (!_siStack.isEmpty()) {
            sInfo = (StateInfo)_siStack.peek();
            sInfo.complex = true;
        }
        else {
            topLevel = true;
        }
        
        //-- create current holder for stateInformation
        sInfo = new StateInfo();
        sInfo.topLevel = topLevel;
        _siStack.push(sInfo);
        
        //-- create element definition
        sInfo.element = new ElementDecl(_schema, name);
        
        //-- create attributes
        for (int i = 0; i < atts.getLength(); i++) {
            
            String attName = atts.getName(i);
            
            //-- skip namespace declarations
            if (attName.equals(XMLNS)) continue;
            String prefix = "";
            idx = attName.indexOf(':');
            if (idx >= 0) {
                prefix = attName.substring(0, idx);
                attName = attName.substring(idx+1);
            }
            if (prefix.equals(XMLNS)) continue;
            
            AttributeDecl attr = new AttributeDecl(_schema, attName);
            
            //-- guess simple type
            String typeName = _nsPrefix + ':' + 
                DatatypeHandler.guessType(atts.getValue(i));
                
            attr.setSimpleTypeReference(typeName);
            
            sInfo.attributes.addElement(attr);
        }
        
    } //-- startElement
    

    //------------------------------------/
    //- org.xml.sax.ErrorHandler methods -/
    //------------------------------------/
    
    public void error(SAXParseException exception)
        throws org.xml.sax.SAXException
    {
        throw exception;
        
    } //-- error
    
    public void fatalError(SAXParseException exception)
        throws org.xml.sax.SAXException
    {
        throw exception;
        
    } //-- fatalError
    
    
    public void warning(SAXParseException exception)
        throws org.xml.sax.SAXException
    {
        throw exception;
        
    } //-- warning
    
    //-------------------------/
    //- local private methods -/
    //-------------------------/
    
    /**
     * Merges the two element declarations. The resulting
     * merge is placed in ElementDecl e1.
     *
     * @param e1 the main ElementDecl 
     * @param e2 the secondary ElementDecl to merge with e1
    **/
    private void merge(ElementDecl e1, ElementDecl e2) 
        throws SchemaException
    {
        
        XMLType e1Type = e1.getType();
        XMLType e2Type = e2.getType();
         
        //-- Make sure types are not null and if so create them
        if (e1Type == null) {
            if (e2Type == null) return; //-- nothing to merge
			if (e2Type.isSimpleType()) {
			    e1.setType(e2Type);
			}
			else {
			    ComplexType cType = new ComplexType(_schema);
			    Group group = new Group();
			    group.setOrder(_defaultGroupOrder);
			    cType.addGroup(group);
			    e1.setType(cType);
			    e1Type = cType;
			}
        }
        else if (e2Type == null) {
            if (e1Type.isSimpleType()) {
                e2.setType(e1Type);
            }
            else {
                ComplexType cType = new ComplexType(_schema);
                Group group = new Group();
                group.setOrder(_defaultGroupOrder);
                cType.addGroup(group);
                e2.setType(cType);
                e2Type = cType;
            }
        }
        
        //-- both simple types
        if (e1Type.isSimpleType() && e2Type.isSimpleType()) {
            if (!e1Type.getName().equals(e2Type.getName())) {
                String typeName = _nsPrefix + ':' +
                    DatatypeHandler.whichType(e1Type.getName(),
                        e2Type.getName());
                e1.setType(null);
                e1.setTypeReference(typeName);
            }
            return;
        }
        //-- e1 is simple, e2 is complex
        else if (e1Type.isSimpleType()) {
            ComplexType cType = new ComplexType(_schema);
            e1.setType(cType);
            Group group = new Group();
            group.setOrder(_defaultGroupOrder);
            cType.addGroup(group);
            cType.setContentType(ContentType.mixed);
            e1Type = cType;
            //-- do not return here...we need to now treat as both
            //-- were complex
        }
        //-- e2 is simple, e1 is complex
        else if (e2Type.isSimpleType()) {
            ComplexType cType = new ComplexType(_schema);
            e2.setType(cType);
            Group group = new Group();
            group.setOrder(_defaultGroupOrder);
            cType.addGroup(group);
            cType.setContentType(ContentType.mixed);
            e2Type = cType;
            //-- do not return here...we need to now treat as both
            //-- were complex
        }
        
        //-- both complex types
        ComplexType cType1 = (ComplexType)e1Type;
        ComplexType cType2 = (ComplexType)e2Type;
        
        //-- loop through all element/attribute declarations
        //-- of e2 and add them to e1 if they do not already exist
        //-- and mark them as optional
        
        Group e1Group = (Group) cType1.getParticle(0);
        if (e1Group == null) {
            e1Group = new Group();
            e1Group.setOrder(_defaultGroupOrder);
            cType1.addGroup(e1Group);
            
        }
        Group e2Group = (Group) cType2.getParticle(0);
        if (e2Group == null) {
            e2Group = new Group();
            e2Group.setOrder(_defaultGroupOrder);
            cType2.addGroup(e2Group);
            
        }
        
        Enumeration enumeration = e2Group.enumerate();
        while (enumeration.hasMoreElements()) {
            Particle particle = (Particle)enumeration.nextElement();
            if (particle.getStructureType() == Structure.ELEMENT) {
                ElementDecl element = (ElementDecl)particle;
                ElementDecl main = e1Group.getElementDecl(element.getName());
                if (main == null) {
                    e1Group.addElementDecl(element);
                    element.setMinOccurs(0);
                }
                else {
                    merge(main, element);
                }
            }
        }
        //-- add all attributes from type2
        enumeration = cType2.getAttributeDecls();
        
        while (enumeration.hasMoreElements()) {
            //-- check for attribute with same name
            AttributeDecl attNew =  (AttributeDecl)enumeration.nextElement();
                    
            String attName = attNew.getName();
            AttributeDecl attPrev = cType1.getAttributeDecl(attName);
            if (attPrev == null) {
                attNew.setUse(AttributeDecl.USE_OPTIONAL);
                cType1.addAttributeDecl(attNew);
            }
            else {
                String type1 = attPrev.getSimpleType().getName();
                String type2 = attNew.getSimpleType().getName();
                if (!type1.equals(type2)) {
                    String typeName = _nsPrefix + ':' + 
                        DatatypeHandler.whichType(type1, type2);
                    attPrev.setSimpleTypeReference(typeName);                        }
            }
        }
        
        //-- loop through all element/attribute declarations
        //-- of e1 and if they do not exist in e2, simply
        //-- mark them as optional
        enumeration = e1Group.enumerate();
        while (enumeration.hasMoreElements()) {
            Particle particle = (Particle)enumeration.nextElement();
            if (particle.getStructureType() == Structure.ELEMENT) {
                ElementDecl element = (ElementDecl)particle;
                if (e2Group.getElementDecl(element.getName()) == null) {
                    element.setMinOccurs(0);
                }
            }
        }
        
        
    } //-- merge
    
    /**
     * Inner-class to hold state
    **/
    class StateInfo {
        Namespaces   namespaces   = null;
        ElementDecl  element      = null;
        Vector       attributes   = null;
        StringBuffer buffer       = null;
        boolean      mixed        = false;
        boolean      complex      = false;
        boolean      topLevel     = false;
        
        public StateInfo() {
            super();
            attributes = new Vector();
        }
        
    } //-- StateInfo
    
} //--


=======
package com.nr.test.test_chapter6;

import static com.nr.sf.Hypergeo.hypgeo;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nr.Complex;

public class Test_hypgeo {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=20,M=100;
    double err,errmax,sbeps;
    Complex aa,bb,cc,zz,res1,res2;
    Complex a[]={new Complex(1.0,0.0), new Complex(0.0,1.0),new Complex(1.0,0.0),new Complex(1.0,0.0),
      new Complex(1.0,0.0),new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(1.0,0.0),new Complex(1.0,0.0),
      new Complex(5.0,0.0),new Complex(0.0,3.0),new Complex(0.0,-4.0),new Complex(-5.0,0.0)};
    Complex b[]={new Complex(1.0,0.0),new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(1.0,0.0),
      new Complex(1.0,0.0),new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(0.0,1.0),new Complex(1.0,0.0),
      new Complex(0.0,5.0),new Complex(2.0,0.0),new Complex(0.0,-1.0),new Complex(3.0,0.0)};
    Complex c[]={new Complex(1.0,0.0),new Complex(1.0,0.0),new Complex(1.0,0.0),new Complex(0.0,1.0),
      new Complex(1.0,0.0),new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(2.0,0.0),new Complex(3.0,0.0),new Complex(3.0,0.0),new Complex(2.0,0.0),
      new Complex(1.0,0.0),new Complex(0.0,1.0),new Complex(1.0,0.0),new Complex(0.0,1.0),
      new Complex(0.0,-2.0),new Complex(0.0,-2.0),new Complex(7.0,0.0),new Complex(0.0,7.0)};   
    Complex z[]={new Complex(0.5,0.0),new Complex(0.5,0.0),new Complex(0.5,0.0),new Complex(0.5,0.0),
      new Complex(0.0,1.0),new Complex(0.0,1.0),new Complex(0.0,1.0),new Complex(0.0,0.0),
      new Complex(0.5,0.0),new Complex(0.5,0.0),new Complex(-0.5,0.0),new Complex(-0.5,0.0),
      new Complex(-0.5,0.0),new Complex(0.0,1.0),new Complex(1.0,1.0),new Complex(1.0,1.0),
      new Complex(1.0,1.0),new Complex(1.0,3.0),new Complex(2.0,-3.0),new Complex(5.0,7.0)};
    Complex eexpect[]={new Complex(2.0,0.0),
      new Complex(0.76923890136397212658,0.63896127631363480115),
      new Complex(0.76923890136397212658,0.63896127631363480115),
      new Complex(0.18874993960184887345,-0.73280804956611519935),
      new Complex(0.5,0.5),new Complex(0.0,0.5),new Complex(-0.25,0.25),
      new Complex(1.0,0.0),new Complex(4.0,0.0),new Complex(8.0,0.0),
      new Complex(0.29629629629629629630,0.0),
      new Complex(0.44444444444444444444,0.0),
      new Complex(0.66666666666666666667,0.0),
      new Complex(0.42882900629436784932,-0.15487175246424677819),
      new Complex(0.20787957635076190855,0.0),
      new Complex(2.4639512200927103386,5.0258643859042736965),
      new Complex(0.000782175555099748119,0.065075199065027035764),
      new Complex(-0.31205397840397702583,-0.04344693995132976350),
      new Complex(0.34836454596486382673,0.65394630061667130711),
      new Complex(617.9369000550522997,2595.6638964158808010)};
    Complex[] y=new Complex[N],expect=new Complex[N];
    System.arraycopy(eexpect, 0,expect,0, N);;
    boolean localflag, globalflag=false;

    

    // Test selected values
    System.out.println("Testing hypgeo");
    errmax=0.0;
    for (i=0;i<N;i++) {
      y[i]=hypgeo(a[i],b[i],c[i],z[i]);
      err=y[i].sub(expect[i]).abs()/expect[i].abs();
//      System.out.printf(y[i] << "  %f\n", expect[i] << "  %f\n", err);
      if (err > errmax) errmax=err;
    }
//    System.out.println("hypgeo: Maximum fractional discrepancy = %f\n", errmax);
    sbeps=1.e-10;
    localflag = errmax > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Incorrect function values");
      
    }

    // Test symmetry of a and b
    sbeps=1.e-15;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(0.5,5.0-0.1*i);
      cc=new Complex(-5.0+0.1*i,1.0);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);

      res1=hypgeo(aa,bb,cc,zz);
      res2=hypgeo(bb,aa,cc,zz);
//      System.out.printf(abs(res1-res2));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Lack of symmetry in first two arguments");
      
    }

    // Test case a=c, b=1 where hypgeo=1/(1-z)
    sbeps=1.e-12;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(1.0,0.0);
      cc=new Complex(-0.5,-5.0+0.1*i);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);

      res1=hypgeo(aa,bb,cc,zz);
      res2=new Complex(1.0).div(new Complex(1.0).sub(zz));
//      System.out.printf(abs(res1-res2));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Failure of special case a=c, b=1");
      
    }
    
    // Test a contiguous relationship for aa (Abromowitz & Stegun, 15.2.13)
    sbeps=1.e-12;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(1.0,0.0);
      cc=new Complex(-0.5,-5.0+0.1*i);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);
      
      //res1=(cc-2.0*aa-(bb-aa)*zz)*hypgeo(aa,bb,cc,zz)
      //  + aa*(1.0-zz)*hypgeo(aa+1.0,bb,cc,zz);
      Complex r = bb.sub(aa).mul(zz);
      r = cc.sub(aa.mul(2.0)).sub(r);
      r = r.mul(hypgeo(aa,bb,cc,zz));
      res1 = r.add( aa.mul(new Complex(1.0).sub(zz)).mul(
          hypgeo(aa.add(new Complex(1.0)),bb,cc,zz)) );
      
      //res2=(cc-aa)*hypgeo(aa-1.0,bb,cc,zz);
      //      System.out.printf(abs(res1-res2));
      r = cc.sub(aa);
      res2 = r.mul(hypgeo(aa.sub(new Complex(1.0)),bb,cc,zz));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
      
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Failure of contiguous relationship for aa");
      
    }
    
    // Test a contiguous relationship for cc (Abromowitz & Stegun, 15.2.27)
    sbeps=1.e-10;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(1.0,0.0);
      cc=new Complex(-0.5,-5.0+0.1*i);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);
      Complex r = cc.mul(2.0).sub(aa).sub(bb).sub(new Complex(1.0));
      r = cc.sub(new Complex(1.0)).sub(r.mul(zz));
      r = cc.mul(r).mul(hypgeo(aa,bb,cc,zz));
      Complex rr =(cc.sub(aa)).mul(cc.sub(bb)).mul(zz).mul(hypgeo(aa,bb,cc.sub(new Complex(1.0)),zz));
      res1 = r.add(rr);
      //res1=cc*(cc-1.0-(2.0*cc-aa-bb-1.0)*zz)*hypgeo(aa,bb,cc,zz)
      //  + (cc-aa)*(cc-bb)*zz*hypgeo(aa,bb,cc+1.0,zz);
      res2 = cc.mul(cc.sub(new Complex(1.0))).mul(new Complex(1.0).sub(zz));
      res2 = res2.mul(hypgeo(aa,bb,cc.sub(new Complex(1.0)),zz));
      // res2=cc*(cc-1.0)*(1.0-zz)*hypgeo(aa,bb,cc-1.0,zz);
      
//      System.out.printf(abs(res1-res2));
      localflag = localflag || res1.sub(res2).abs() > sbeps;
      
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Failure of contiguous relationship for cc");
      
    }
    
    // Test of mirror symmetry
    sbeps=1.e-15;
    localflag=false;
    for (i=0;i<M;i++) {
      aa=new Complex(-0.5,-5.0+0.1*i);
      bb=new Complex(0.5,5.0-0.1*i);
      cc=new Complex(-5.0+0.1*i,1.0);
      zz=new Complex(-5.0+0.1*i,-5.0+0.1*i);

      res1=hypgeo(aa,bb,cc,zz);
      res2=hypgeo(aa.conj(),bb.conj(),cc.conj(),zz.conj());
//      System.out.printf(res1 << " %f\n", res2);
      localflag = localflag || res1.sub(res2.conj()).abs() > sbeps;
    }
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** hypgeo: Function does not follow mirror symmetry rule");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

