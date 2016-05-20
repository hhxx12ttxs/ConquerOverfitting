package trustEvaluation.communication;

 

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.remoting.Client;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.uddi4j.UDDIException;
import org.uddi4j.transport.TransportException;
import org.w3c.dom.Document;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import sun.awt.PlatformFont;
import trustEvaluation.componentEvaluation.ServiceEvaluation;
import trustEvaluation.componentEvaluation.serviceBasicInfo.ServiceBasicInfo;
import trustEvaluation.evaluationProxy.ComponentEvaluationProxy;
import trustEvaluation.evaluationProxy.EvaluationMessageType;
import trustEvaluation.evaluationProxy.EvaluationProxy;
import trustEvaluation.evaluationProxy.EvaluationProxyFactory;
import trustEvaluation.evaluationProxy.component.serviceCollection.CollectedServiceInfo;
import trustEvaluation.evaluationProxy.component.serviceCollection.MyServiceList;
import trustEvaluation.evaluationProxy.component.serviceCollection.ServicesEvaluationResultDoc;
import trustEvaluation.filePathParse.FileNames;
import trustEvaluation.filePathParse.FilePathParser;
import trustEvaluation.systemEvaluation.systemModel.SystemEvaluation;
import trustEvaluation.systemEvaluation.systemModel.TrustSystemModel;

public class ArtemisTrustHandler implements ServerInvocationHandler
{
	@Override
	public void addListener(InvokerCallbackHandler arg0)
	{
	}

	@Override
	public Object invoke(InvocationRequest request) throws Throwable
	{
		InvocationRequest evaRequest = request;
		Thread evaThread = new Thread(new EvaluationRun(evaRequest));
		evaThread.start();
		
		System.out.println("11");
		return true;
	}

	@Override
	public void removeListener(InvokerCallbackHandler arg0)
	{
	}

	@Override
	public void setInvoker(ServerInvoker arg0)
	{
	}

	@Override
	public void setMBeanServer(MBeanServer arg0) 
	{
	}
}

class EvaluationRun implements Runnable
{
	private InvocationRequest evaRequest;
	
	public EvaluationRun(InvocationRequest evaRequest)
	{
		this.evaRequest = evaRequest;
	}
	
	public void run()
	{
		Object[] params = (Object[]) evaRequest.getParameter();
		Object[] result = null;
		
		if (params[0].toString().equals(EvaluationMessageType.COMPONENT_SERVICE_REMOVE))
		{
			ServiceEvaluation componentEvaluation = new ServiceEvaluation();
			result = new Object[2];
			
			if (params.length == 3)
			{
				componentEvaluation.deregisterService(params[1].toString(), params[2].toString());
				result[1] = EvaluationMessageType.COMPONENT_SERVICE_REMOVE_RESULT;
				result[0] = EvaluationMessageType.COMPONENT;
			}
			
			else if (params.length == 4)
			{
				componentEvaluation.deregisterService(params[1].toString(), params[2].toString(), params[3].toString());
				result[1] = EvaluationMessageType.COMPONENT_SERVICE_REMOVE_RESULT;
				result[0] = EvaluationMessageType.COMPONENT;
			}
		}
		
		else if (params[0].toString().equals(EvaluationMessageType.COMPONENT_SERVICE_CHANGE))
		{
			result = new Object[2];
			
			if (params.length == 3)
			{
				ServiceEvaluation componentEvaluation = new ServiceEvaluation();
				String instanceName = params[1].toString();
				String expectationValue = params[2].toString();
				componentEvaluation.serExpecationChange(null, null, instanceName, Double.valueOf(expectationValue));
			
				result[0] = EvaluationMessageType.COMPONENT;
				result[1] = EvaluationMessageType.COMPONENT_SERVICE_CHANGE_RESULT;
			}
		}
		
		else
		{
			ByteArrayInputStream trustStream = new ByteArrayInputStream((byte[]) params[1]);
			XMLParser parser = new XMLParser();
			Document doc = parser.getDocument(trustStream);
			
			if (params[0].toString().equals(EvaluationMessageType.SYSTEM))
			{
				System.out.println("System Evaluation!");
				
				EvaluationProxy evaProxy = EvaluationProxyFactory.getEvaAgentInstance(EvaluationProxyFactory.EvaluationType.System);
				evaProxy.requestParse(doc);
				evaProxy.startEvaluation();
				 
				result = new Object[2];
				result[0] = EvaluationMessageType.SYSTEM;
				result[1] = parser.writeByteArrayDom4j(evaProxy.createDoc());
			}
			
			else if (params[0].toString().equals(EvaluationMessageType.COMPONENT))
			{
				System.out.println("Component Evaluation!");
				
				result = new Object[5];
				result = componentEvaluation(doc);
			}
		}
		
		try 
		{
			returnResult(result);
		} 
		catch (Throwable e)
	    {
			e.printStackTrace();
		}
	}
	
	public Object[] componentEvaluation(Document expectationDoc)
	{
		Object[] results = new Object[5];
		results[0] = EvaluationMessageType.COMPONENT;
		EvaluationProxy evaProxy = EvaluationProxyFactory.getEvaAgentInstance(EvaluationProxyFactory.EvaluationType.Component);
		evaProxy.requestParse(expectationDoc);
		String componentId = ((ComponentEvaluationProxy)evaProxy).getServiceInfo().getInstanceName();
		results[1] = componentId;
		results[2] = evaProxy.startEvaluation();
		System.out.println(results[1]);
		System.out.println(results[2]);
		
		XMLParser parser = new XMLParser();
		MyServiceList services = new MyServiceList();
		services.setComponentId(componentId);
		List<ServiceBasicInfo> serviceInfos = ((ComponentEvaluationProxy)evaProxy).getServiceExpectation().getSameSerInfos();
		
		if (serviceInfos != null)
		{
			for (int index = 0; index < serviceInfos.size(); index++)
			{
				CollectedServiceInfo serviceInfo = new CollectedServiceInfo();
				serviceInfo.setProviderName(serviceInfos.get(index).getServiceProvider());
				serviceInfo.setServiceName(serviceInfos.get(index).getServiceName());
				serviceInfo.setServiceURL(serviceInfos.get(index).getServiceURL());
				serviceInfo.setInstanceName(serviceInfos.get(index).getInstanceName());
				services.add(serviceInfo);
			}
		}
		
		if (services.getServiceList() != null)
		{	
			servicesEvaluation(services, evaProxy, expectationDoc);
			ServicesEvaluationResultDoc resultXml = new ServicesEvaluationResultDoc(services);
			
			resultXml.createDom4jDco();			
			results[3] = parser.writeByteArrayDom4j(resultXml.getDom4jDoc());
		}
		
		org.dom4j.Document tempDoc = evaProxy.createDoc();
		if (tempDoc == null)
		{
			results[4] = "NULL!";
		}
		else results[4] = parser.writeByteArrayDom4j(evaProxy.createDoc());
		
		return results;
	}
	
	public void servicesEvaluation(MyServiceList serviceList, EvaluationProxy evaAgent, Document expectationDoc)
	{
		List<CollectedServiceInfo> services = new LinkedList<CollectedServiceInfo>();
		services = serviceList.getServiceList();
		
		int tempIndex = -1;
		
		for (int index = 0; index < services.size(); index++)
		{
			ServiceBasicInfo tempService = new ServiceBasicInfo();
			
			if (!(services.get(index).getServiceURL().equals(((ComponentEvaluationProxy)evaAgent).
					getServiceInfo().getServiceURL())))
			{
				tempService.setServiceURL(services.get(index).getServiceURL());
				tempService.setServiceName(services.get(index).getServiceName());
				tempService.setServiceProvider(services.get(index).getProviderName());
				tempService.setInstanceName(services.get(index).getInstanceName());
				
				EvaluationProxy tempEvaAgent = EvaluationProxyFactory.getEvaAgentInstance(
						                   EvaluationProxyFactory.EvaluationType.Component);
				((ComponentEvaluationProxy)tempEvaAgent).setServiceInfo(tempService);
				tempEvaAgent.requestParse(expectationDoc);
				services.get(index).setEvaluationResult(tempEvaAgent.startEvaluation());
				
				System.out.println(services.get(index).getServiceURL());
			}
			
			else 
			{
				tempIndex = index;
			}
		}
		
		serviceList.setServiceList(services);
		
		if (tempIndex != -1)
		{
			serviceList.getServiceList().remove(tempIndex);
		}
	}
	
	public void returnResult(Object[] params) throws Throwable 
	{
		String locatorFilePath = new String();
		FilePathParser filePathParser = new FilePathParser();
		
		if (filePathParser.findFilePath(FileNames.COMMUNICATION_LOCATOR))
		{
			locatorFilePath = filePathParser.getFilePath();
		}
		
		Properties props = new Properties();
//		FileInputStream in = new FileInputStream("E:\\workspace\\artemis.trust.evaluation\\resource\\trustViewLocator\\locator.properties");
		FileInputStream in = new FileInputStream(locatorFilePath);
		props.load(in);
		in.close();
		
		String locatorURI = "socket://" + props.getProperty("TrustViewLocatorURI");
		InvokerLocator locator = new InvokerLocator(locatorURI);

		System.out.println("Calling on remoting server with locator uri of: " + locatorURI);

		final Client remotingClient = new Client(locator, ArtemisConnectorConstants.RUNTIME_TRUST_SUB_SYSTEM);

		try 
		{
			remotingClient.connect();
			remotingClient.invoke(params);
		} 
		catch (Throwable e)
		{
			throw e;
		} 
		finally 
		{
			if (remotingClient != null)
			{
				remotingClient.disconnect();
			}
		}
	}
}


