/*
  Cassia: Catalogador SIB sobre Información Ambiental-
  Copyright (C) 2010 Benjamin Rodriguez <benjamin.a.rodriguez@gmail.com>
	
  Este programa es software libre: usted puede redistribuirlo y/o modificarlo 
  bajo los términos de la Licencia Pública General GNU publicada 
  por la Fundación para el Software Libre, ya sea la versión 3 
  de la Licencia, o (a su elección) cualquier versión posterior.

  Este programa se distribuye con la esperanza de que sea útil, pero 
  SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
  MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. 
  Consulte los detalles de la Licencia Pública General GNU para obtener 
  una información más detallada. 

  Debería haber recibido una copia de la Licencia Pública General GNU 
  junto a este programa. 
  
  En caso contrario, consulte <http://www.gnu.org/licenses/>.
  
*/
package org.humboldt.cassia.componentes.metodos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

public class MetodosCliente {
	private final int TIMEOUT = 600000;
    public MetodosCliente() {
    }

    public Collection<Metodo> llamarConjuntoSDBlocking(String URL, String nombreServicio, ArrayList textobuscar){
    	Collection<Metodo> metodos = new ArrayList<Metodo>();
        try {
        	MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        	HttpClient httpClient = new HttpClient(httpConnectionManager);
            EndpointReference targetEPR = new EndpointReference(URL);
            //Crea un OMElemnt con datos de prueba
            OMElement metsd = getConjuntoOMElement(textobuscar);
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(TIMEOUT));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(TIMEOUT));
            options.setAction(nombreServicio);
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);
            options.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
            //Blocking invocation
            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);
            OMElement resultado = sender.sendReceive(metsd);
            Iterator itMetodosXML = resultado.getChildElements();
            while (itMetodosXML.hasNext()) {
            	OMElement objTes = (OMElement)itMetodosXML.next();
            	Metodo metodo = new Metodo();
            	metodo.nombre = objTes.getAttributeValue(new QName("nombre"));
            	metodo.id = objTes.getAttributeValue(new QName("id"));
            	metodo.descripcion = objTes.getAttributeValue(new QName("descripcion"));
            	metodo.url = objTes.getAttributeValue(new QName("url"));
            	//metodo.atributos = llamarConjuntoByIdSDBlocking(URL, "metodosIDSib", metodo.id);
            	metodos.add(metodo);
            }
            try {
				sender.cleanupTransport();
			} catch (Exception e) {
			}
            try {
				sender.cleanup();
			} catch (Exception e) {
			}
			httpConnectionManager.closeIdleConnections(0);
			httpConnectionManager.shutdown();
		} catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        return metodos;
    }
    public static OMElement getConjuntoOMElement(ArrayList textobuscar) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://www.w3.org/2001/XMLSchema", "");
        OMElement root = fac.createOMElement("metsd", omNs);
        for(int i =0; i< textobuscar.size();i++){
            OMElement url = fac.createOMElement("url", omNs);
            url.setText(textobuscar.get(i).toString());
            root.addChild(url);
        }
        return root;
    }
    public ArrayList<String> llamarConjuntoByIdSDBlocking(String URL, String nombreServicio, String id){
    	ArrayList<String> atributos = new ArrayList<String>();
        try {
        	MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        	HttpClient httpClient = new HttpClient(httpConnectionManager);
            EndpointReference targetEPR = new EndpointReference(URL);
            //Crea un OMElemnt con datos de prueba
            OMElement metsd = getConjuntoIdOMElement(id);
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(HTTPConstants.SO_TIMEOUT,new Integer(TIMEOUT));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT,new Integer(TIMEOUT));
            options.setAction(nombreServicio);
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);
            options.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
            //Blocking invocation
            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);
            OMElement resultado = sender.sendReceive(metsd);
            Iterator itMetodosXML = resultado.getChildElements();
            while (itMetodosXML.hasNext()) {
            	OMElement objTes = (OMElement)itMetodosXML.next();
            	if ("atributo".equals(objTes.getLocalName())) {
            		atributos.add(objTes.getAttributeValue(new QName("nombre")));
            	}
            }
            try {
				sender.cleanupTransport();
			} catch (Exception e) {
			}
            try {
				sender.cleanup();
			} catch (Exception e) {
			}
			httpConnectionManager.closeIdleConnections(0);
			httpConnectionManager.shutdown();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        System.out.println("Atributos ID:: " + atributos);
        return atributos;
    }
    public static OMElement getConjuntoIdOMElement(String id) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://www.w3.org/2001/XMLSchema", "");
        OMElement root = fac.createOMElement("metsd", omNs);
        OMElement url = fac.createOMElement("id", omNs);
        url.setText(id);
        root.addChild(url);
        
            
        return root;
    }
    
    
    public static void main(String[] a){
    	
    }
    
}

