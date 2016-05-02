/*
 * _____________________________________________________________________________________________________________________
 *
 * This software is distributed under the terms of the BSD License:
 *
 * <OWNER> = Sebastian Germesin
 * <ORGANIZATION> = DFKI GmbH, Stuhlsatzenhausweg 3, 66123 Saarbruecken, Germany
 * <YEAR> = 2010
 *
 * Copyright <YEAR>, <OWNER>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *   disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * _____________________________________________________________________________________________________________________
 */
package eu.iksproject.interpret.oob_ipat.gam;

import eu.iksproject.interpret.core.api.interpret.IInterpret;
import eu.iksproject.interpret.core.api.ipat.AbstractIPat;
import eu.iksproject.interpret.core.api.ipat.IIPat;
import eu.iksproject.interpret.core.api.ipat.IIPatEvent;
import eu.iksproject.interpret.core.api.ipat.IPatState;
import eu.iksproject.interpret.core.api.user.IUser;
import eu.iksproject.interpret.core.util.Util;
import eu.iksproject.interpret.oob_ipat.gam.event.ContextClosedEvent;
import eu.iksproject.interpret.oob_ipat.gam.event.CreateTagEvent;
import eu.iksproject.interpret.oob_ipat.gam.event.DnDEvent;
import eu.iksproject.interpret.oob_ipat.gam.event.GroundingImageEvent;
import eu.iksproject.interpret.oob_ipat.gam.event.GroundingTextEvent;
import eu.iksproject.interpret.oob_ipat.gam.event.LoginEvent;
import eu.iksproject.interpret.oob_ipat.gam.event.LogoutEvent;
import eu.iksproject.interpret.oob_ipat.gam.event.RightClickEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.NamespaceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODO
 * 
 * @author Sebastian Germesin
 *
 */

@Component(immediate = true, metatype = true)
@Service(IIPat.class)
public class GAMTask extends AbstractIPat {

	enum ContentType {
		Text, Image
	};

	private static Log logger = LogFactory.getLog(GAMTask.class);

	private BundleContext bundleContext;

	private final ExecutorService es = Executors.newCachedThreadPool();

	public GAMTask() {
		super("GAMTask", new Version(0, 0, 1), "TODO");
	}

	protected void activate(ComponentContext ctx) throws IOException, ServletException, NamespaceException {
		bundleContext = ctx.getBundleContext();
	}

	protected void deactivate(ComponentContext ctx) throws IOException, ServletException, NamespaceException {
		bundleContext = null;
	}

	private IInterpret getInterpret() {
		try {
			ServiceReference sr = bundleContext.getServiceReference(IInterpret.class.getName());
			if (sr != null) {
				IInterpret interpret = (IInterpret) bundleContext.getService(sr);
				if (interpret != null) {
					return interpret;
				}
			}
		} catch (Exception e) {
			logger.warn("No INTERPRET Core found!", e);
			return null;
		}
		logger.warn("No INTERPRET Core found!");
		return null;
	}

	/**
	 * This method converts the {@link String} input into the {@link ITaskEvent} object.
	 * For this specific task, the input needs to be in EVML (EVent Markup Language), which
	 * is specified as follows:
	 * 
	 * &lt;evml&gt; <br>
	 *   &lt;event name="[NAME]"&gt; <br>
	 *     &lt;params&gt; <br>
	 *       &lt;uri&gt; ... &lt;/uri&gt; <br>
	 *     &lt;/params&gt; <br>
	 *   &lt;/event&gt; <br>
	 * &lt;/evml&gt; <br>
	 * 
	 * Where [NAME] corresponds to the output of the <pre>getName()</pre> method from the
	 * events, defined in {@link eu.iksproject.interpret.gam_task.gam.event}.
	 * 
	 * @param userId The id of the user
	 * @param input The input message, serialized as a {@link java.lang.String} object.
	 * 
	 * @return The converted {@link ITaskEvent} or <pre>null</pre> if no conversion could be found.
	 */
	@Override
	public IIPatEvent convertInput(UUID userId, String input) {
		try {
			logger.info("Converting the following input: '" + input + "'");
			IUser user = getInterpret().getUser(userId);
			XPath xpath = XPathFactory.newInstance().newXPath();
			Document doc = Util.stringToXml(input);

			Element element = (Element) xpath.evaluate("/evml/event", doc, XPathConstants.NODE);

			if (element != null) {
				String name = element.getAttribute("name");
				logger.info("Found EVML input with the name: '" + name + "'");

				if (name.equalsIgnoreCase(GroundingTextEvent.getName())) {
					logger.info("EVML is an event of type " + GroundingTextEvent.class.getName());
					Element idElement = (Element) xpath.evaluate("/evml/event/i", doc, XPathConstants.NODE);
					Element textElement = (Element) xpath.evaluate("/evml/event/txt", doc, XPathConstants.NODE);
					return new GroundingTextEvent(idElement.getTextContent(), textElement.getTextContent());
				} else if (name.equalsIgnoreCase(GroundingImageEvent.getName())) {
					logger.info("EVML is an event of type " + GroundingImageEvent.class.getName());
					Element idElement = (Element) xpath.evaluate("/evml/event/i", doc, XPathConstants.NODE);
					return new GroundingImageEvent(idElement.getTextContent());
				} else if (name.equalsIgnoreCase(LoginEvent.getName())) {
					logger.info("EVML is an event of type " + LoginEvent.class.getName());
					return new LoginEvent();
				} else if (name.equalsIgnoreCase(LogoutEvent.getName())) {
					logger.info("EVML is an event of type " + LogoutEvent.class.getName());
					return new LogoutEvent();
				} else if (name.equalsIgnoreCase(RightClickEvent.getName())) {
					logger.info("EVML is an event of type " + RightClickEvent.class.getName());
					Element idElement = (Element) xpath.evaluate("/evml/event/i", doc, XPathConstants.NODE);
					Element xElement = (Element) xpath.evaluate("/evml/event/x", doc, XPathConstants.NODE);
					Element yElement = (Element) xpath.evaluate("/evml/event/y", doc, XPathConstants.NODE);
					RightClickEvent rce = new RightClickEvent(idElement.getTextContent().trim(), xElement
							.getTextContent().trim(), yElement.getTextContent().trim());
					return rce;
				} else if (name.equalsIgnoreCase(CreateTagEvent.getName())) {
					logger.info("EVML is an event of type " + CreateTagEvent.class.getName());
					Element idElement = (Element) xpath.evaluate("/evml/event/i", doc, XPathConstants.NODE);
					Element tagElement = (Element) xpath.evaluate("/evml/event/ta", doc, XPathConstants.NODE);
					CreateTagEvent cte = new CreateTagEvent(idElement.getTextContent().trim(), tagElement
							.getTextContent().trim());
					return cte;
				} else if (name.equalsIgnoreCase(ContextClosedEvent.getName())) {
					logger.info("EVML is an event of type " + ContextClosedEvent.class.getName());
					ContextClosedEvent lce = new ContextClosedEvent();
					return lce;
				} else if (name.equalsIgnoreCase(DnDEvent.getName())) {
					logger.info("EVML is an event of type " + DnDEvent.class.getName());
					Element srcElement = (Element) xpath.evaluate("/evml/event/src", doc, XPathConstants.NODE);
					Element tarElement = (Element) xpath.evaluate("/evml/event/tar", doc, XPathConstants.NODE);
					DnDEvent dnde = new DnDEvent(srcElement.getAttribute("id").trim(), tarElement.getAttribute("id")
							.trim());
					return dnde;
				} else {
					logger.warn("Sorry, cannot convert input of type:'" + name + "'");
					return null;
				}
			} else {
				logger.warn("This is not a valid EVML format!");
				return null;
			}
		} catch (Exception e) {
			logger.warn("Please check for correct XML project settings!", e);
			return null;
		}
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, GroundingImageEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");
		IUser user = getInterpret().getUser(userId);
		IPatState currentState = getCurrentState(userId);
		if (currentState == INIT_STATE) {
			user.getContext().addObject("content(" + event.getId() + ").type", "image");
			logger.info("Adding (" + "content(" + event.getId() + ").type , 'image' to context(" + userId + ")!");
			return new String[] { "addDropTarget", event.getId() };
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, GroundingTextEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");
		IUser user = getInterpret().getUser(userId);
		IPatState currentState = getCurrentState(userId);
		if (currentState == INIT_STATE) {
			user.getContext().addObject("content(" + event.getId() + ").type", "text");
			user.getContext().addObject("content(" + event.getId() + ").text", event.getText());
			logger.info("Adding (" + "content(" + event.getId() + ").type , 'text' to context(" + userId + ")!");
			logger.info("Adding (" + "content(" + event.getId() + ").text , '" + event.getText() + "' to context("
					+ userId + ")!");
			return new String[] { "addDropTarget", event.getId() };
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, LoginEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");
		IUser user = getInterpret().getUser(userId);
		IPatState currentState = getCurrentState(userId);
		if (currentState == INIT_STATE) {
			setCurrentState(userId, IPatState.getInstance("loggedIn"));
			user.getContext().addObject("loggedin", true);
			logger.info("Adding (" + "loggedin" + "," + true + ") to context(" + userId + ")!");
			return true;
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, LogoutEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");
		IPatState currentState = getCurrentState(userId);
		if (currentState != INIT_STATE) {
			setCurrentState(userId, INIT_STATE);
			IUser user = getInterpret().getUser(userId);
			user.getContext().addObject("loggedin", false);
			logger.info("Adding (" + "loggedin" + "," + false + ") to context(" + userId + ")!");
			long now = System.currentTimeMillis();
			user.getContext().addObject("lastlogin.time", now);
			logger.info("Adding (" + "lastlogin.time" + "," + now + ") to context(" + userId + ")!");
			return true;
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, RightClickEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");

		IPatState currentState = getCurrentState(userId);
		if (currentState == IPatState.getInstance("loggedIn")) {
			IUser user = getInterpret().getUser(userId);
			// if (src.isTagged()) -> 
			if (user.getContext().containsKey("content(" + event.getId() + ").tag")) {
				String tag = (String) user.getContext().getObject("content(" + event.getId() + ").tag");
				String type = (String) user.getContext().getObject("content(" + event.getId() + ").type");
				String text = (String) user.getContext().getObject("content(" + event.getId() + ").text");

				String textToShow = "";
				if (tag.equalsIgnoreCase("person")) {
					textToShow = "Search in Facebook for '<b>" + text + "</b>'!";
				} else if (tag.equalsIgnoreCase("organization")) {
					textToShow = "Search in Business.com for '<b>" + text + "</b>!";
				} else if (tag.equalsIgnoreCase("university")) {
					textToShow = "Search in Bing.com for '<b>" + text + "</b>!";
				}

				if (type.equalsIgnoreCase("image")) {
					//// if (src.isImage()) -> showImageContextMenu() TODO
					return new String[] { "showSelectionMenu1", event.getId(), event.getX(), event.getY(), textToShow };
				} else {
					return new String[] { "showSelectionMenu2", event.getId(), event.getX(), event.getY(), textToShow,
							"Use Google-Search instead!" };
				}
			}
			// else -> CREATE_TAG -> send context menu to let user choose
			else {
				String type = (String) user.getContext().getObject("content(" + event.getId() + ").type");
				if (type != null) {
					logger.info("This is an untagged element of type '" + type
							+ "'. Please show context menu to let user manually tag the item!");
					if (type.equalsIgnoreCase("text")) {
						String text = (String) user.getContext().getObject("content(" + event.getId() + ").text");
						logger.info("TEXT: '" + text + "'");
						return new String[] { "showTagCreationMenu", event.getId(), event.getX(), event.getY(), type,
								text };
					} else {
						return new String[] { "showTagCreationMenu", event.getId(), event.getX(), event.getY(), type,
								"dummy" };
					}
				}
				return false;
			}
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, ContextClosedEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");

		IPatState currentState = getCurrentState(userId);
		if (currentState == IPatState.getInstance("loggedIn")) {
			logger.info("User left context menu!");
			return null;
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, CreateTagEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");
		IPatState currentState = getCurrentState(userId);
		if (currentState == IPatState.getInstance("loggedIn")) {
			// save in context: id <-> tag
			// send: src is draggable/droppable	
			IUser user = getInterpret().getUser(userId);
			user.getContext().addObject("content(" + event.getId() + ").tag", event.getTag());
			logger.info("Adding (" + "content(" + event.getId() + ").tag" + "," + event.getTag() + ") to context("
					+ userId + ")!");
			String functName = "TagContent";
			Version functVersion = new Version(0, 0, 1);
			logger.info("Calling IKS Functionality (" + functName + ";" + functVersion + ";" + event.getId() + ";"
					+ event.getTag() + ") in context (" + userId + ")!");
			getInterpret().executeFunctionality(functName, functVersion, event.getId(), event.getTag());
			String type = (String) user.getContext().getObject("content(" + event.getId() + ").type");
			return new String[] { "markAsTagged", event.getId(), type };
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, DnDEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");

		IPatState currentState = getCurrentState(userId);
		if (currentState == IPatState.getInstance("loggedIn")) {
			IUser user = getInterpret().getUser(userId);
			// if (src.isTagged()) -> 
			if (user.getContext().containsKey("content(" + event.getSourceId() + ").tag")) {
				String srcTag = (String) user.getContext().getObject("content(" + event.getSourceId() + ").tag");
				// if (tar.isTagged()) -> 
				if (user.getContext().containsKey("content(" + event.getTargetId() + ").tag")) {
					String tarTag = (String) user.getContext().getObject("content(" + event.getTargetId() + ").tag");

					if (srcTag.equalsIgnoreCase("person") && tarTag.equalsIgnoreCase("organization")) {
						String srcText = (String) user.getContext().getObject(
								"content(" + event.getSourceId() + ").text");
						String tarText = (String) user.getContext().getObject(
								"content(" + event.getTargetId() + ").text");
						String functName = "LinkContent";
						Version functVersion = new Version(0, 0, 1);
						logger.info("Calling IKS Functionality (" + functName + ";" + functVersion + ";"
								+ event.getSourceId() + ";" + event.getTargetId() + ") in context (" + userId + ")!");
						getInterpret().executeFunctionality(functName, functVersion, event.getSourceId(),
								event.getTargetId());
						return new String[] { "showLinkingResponse",
								"<b>" + srcText + "</b> is marked as employer at <b>" + tarText + "</b>!" };
					} else if (srcTag.equalsIgnoreCase("person") && tarTag.equalsIgnoreCase("university")) {
						String srcText = (String) user.getContext().getObject(
								"content(" + event.getSourceId() + ").text");
						String tarText = (String) user.getContext().getObject(
								"content(" + event.getTargetId() + ").text");
						String functName = "LinkContent";
						Version functVersion = new Version(0, 0, 1);
						logger.info("Calling IKS Functionality (" + functName + ";" + functVersion + ";"
								+ event.getSourceId() + ";" + event.getTargetId() + ") in context (" + userId + ")!");
						getInterpret().executeFunctionality(functName, functVersion, event.getSourceId(),
								event.getTargetId());
						return new String[] { "showLinkingResponse",
								"<b>" + srcText + "</b> is marked as student at <b>" + tarText + "</b>!" };
					} else if (tarTag.equalsIgnoreCase("person") && srcTag.equalsIgnoreCase("organization")) {
						String tarText = (String) user.getContext().getObject(
								"content(" + event.getSourceId() + ").text");
						String srcText = (String) user.getContext().getObject(
								"content(" + event.getTargetId() + ").text");
						String functName = "LinkContent";
						Version functVersion = new Version(0, 0, 1);
						logger.info("Calling IKS Functionality (" + functName + ";" + functVersion + ";"
								+ event.getSourceId() + ";" + event.getTargetId() + ") in context (" + userId + ")!");
						getInterpret().executeFunctionality(functName, functVersion, event.getSourceId(),
								event.getTargetId());
						return new String[] { "showLinkingResponse",
								"<b>" + srcText + "</b> is marked as employer at <b>" + tarText + "</b>!" };
					} else if (tarTag.equalsIgnoreCase("person") && srcTag.equalsIgnoreCase("university")) {
						String tarText = (String) user.getContext().getObject(
								"content(" + event.getSourceId() + ").text");
						String srcText = (String) user.getContext().getObject(
								"content(" + event.getTargetId() + ").text");
						String functName = "LinkContent";
						Version functVersion = new Version(0, 0, 1);
						logger.info("Calling IKS Functionality (" + functName + ";" + functVersion + ";"
								+ event.getSourceId() + ";" + event.getTargetId() + ") in context (" + userId + ")!");
						getInterpret().executeFunctionality(functName, functVersion, event.getSourceId(),
								event.getTargetId());
						return new String[] { "showLinkingResponse",
								"<b>" + srcText + "</b> is marked as student at <b>" + tarText + "</b>!" };
					}
				}

				user.getContext().addObject("content(" + event.getTargetId() + ").tag", srcTag);
				logger.info("Adding (" + "content(" + event.getTargetId() + ").tag" + "," + srcTag + ") to context("
						+ userId + ")!");
			}
			return null;
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@Override
	public Object dispatchEvent(UUID userId, IIPatEvent event) throws Exception {
		try {
			logger.info("Dispatching event (" + event.getClass().getName() + ") for (" + userId + ") and return this");
			Method m = this.getClass().getDeclaredMethod("handleEvent", userId.getClass(), event.getClass());

			Object o = m.invoke(this, userId, event);
			logger.info("Successfully dispatched event (" + event.getClass().getName() + ") for (" + userId + ")");
			return o;
		} catch (Exception e) {
			logger.warn("Sorry, could not dispatch event (" + event.getClass().getName() + ") for (" + userId + ")", e);
			throw new Exception();
		}
	}
}
