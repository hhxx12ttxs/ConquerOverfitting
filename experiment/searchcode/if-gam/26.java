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
package eu.iksproject.interpret.gam_task.gam;

import eu.iksproject.interpret.core.api.interpret.IInterpret;
import eu.iksproject.interpret.core.api.task.AbstractTask;
import eu.iksproject.interpret.core.api.task.ITask;
import eu.iksproject.interpret.core.api.task.ITaskEvent;
import eu.iksproject.interpret.core.api.task.TaskState;
import eu.iksproject.interpret.core.api.user.IUser;
import eu.iksproject.interpret.core.util.Util;
import eu.iksproject.interpret.gam_task.gam.event.ContextClosedEvent;
import eu.iksproject.interpret.gam_task.gam.event.CreateTagEvent;
import eu.iksproject.interpret.gam_task.gam.event.DnDEvent;
import eu.iksproject.interpret.gam_task.gam.event.LoginEvent;
import eu.iksproject.interpret.gam_task.gam.event.LogoutEvent;
import eu.iksproject.interpret.gam_task.gam.event.RightClickEvent;

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
@Service(ITask.class)
public class GAMTask extends AbstractTask {

	enum ContentType {
		Text, Image
	};

	enum ContentTag {
		Organization, Person
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
	public ITaskEvent convertInput(UUID userId, String input) {
		try {
			logger.info("Converting the following input: '" + input + "'");
			IUser user = getInterpret().getUser(userId);
			XPath xpath = XPathFactory.newInstance().newXPath();
			Document doc = Util.stringToXml(input);

			Element element = (Element) xpath.evaluate("/evml/event", doc, XPathConstants.NODE);

			if (element != null) {
				String name = element.getAttribute("name");
				logger.info("Found EVML input with the name: '" + name + "'");

				if (name.equalsIgnoreCase(LoginEvent.getName())) {
					logger.info("EVML is an event of type " + LoginEvent.class.getName());
					return new LoginEvent();
				} else if (name.equalsIgnoreCase(LogoutEvent.getName())) {
					logger.info("EVML is an event of type " + LogoutEvent.class.getName());
					return new LogoutEvent();
				} else if (name.equalsIgnoreCase(CreateTagEvent.getName())) {
					logger.info("EVML is an event of type " + CreateTagEvent.class.getName());
					Element idElement = (Element) xpath.evaluate("/evml/event/i", doc, XPathConstants.NODE);
					Element tagElement = (Element) xpath.evaluate("/evml/event/ta", doc, XPathConstants.NODE);
					Element typeElement = (Element) xpath.evaluate("/evml/event/ty", doc, XPathConstants.NODE);
					Element textElement = (typeElement.getTextContent().trim().equalsIgnoreCase("text")) ? (Element) xpath
							.evaluate("/evml/event/text", doc, XPathConstants.NODE) : null;
					CreateTagEvent cte = (textElement == null) ? new CreateTagEvent(idElement.getTextContent().trim(),
							tagElement.getTextContent().trim(), typeElement.getTextContent().trim())
							: new CreateTagEvent(idElement.getTextContent().trim(), tagElement.getTextContent().trim(),
									typeElement.getTextContent().trim(), textElement.getTextContent().trim());
					return cte;
				} else if (name.equalsIgnoreCase(ContextClosedEvent.getName())) {
					logger.info("EVML is an event of type " + ContextClosedEvent.class.getName());
					ContextClosedEvent lce = new ContextClosedEvent();
					return lce;
				} else if (name.equalsIgnoreCase(RightClickEvent.getName())) {
					logger.info("EVML is an event of type " + RightClickEvent.class.getName());
					Element idElement = (Element) xpath.evaluate("/evml/event/i", doc, XPathConstants.NODE);
					Element typeElement = (Element) xpath.evaluate("/evml/event/ty", doc, XPathConstants.NODE);
					Element xElement = (Element) xpath.evaluate("/evml/event/x", doc, XPathConstants.NODE);
					Element yElement = (Element) xpath.evaluate("/evml/event/y", doc, XPathConstants.NODE);
					RightClickEvent rce = new RightClickEvent(idElement.getTextContent().trim(), typeElement
							.getTextContent().trim(), xElement.getTextContent().trim(), yElement.getTextContent()
							.trim());
					return rce;
				} else if (name.equalsIgnoreCase(DnDEvent.getName())) {
					logger.info("EVML is an event of type " + DnDEvent.class.getName());
					Element srcElement = (Element) xpath.evaluate("/evml/event/src", doc, XPathConstants.NODE);
					Element tarElement = (Element) xpath.evaluate("/evml/event/tar", doc, XPathConstants.NODE);
					DnDEvent dnde = new DnDEvent(srcElement.getAttribute("id").trim(), tarElement.getAttribute("id")
							.trim(), tarElement.getAttribute("type").trim());
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
	private Object handleEvent(UUID userId, LoginEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");
		IUser user = getInterpret().getUser(userId);
		TaskState currentState = getCurrentState(userId);
		if (currentState == INIT_STATE) {
			setCurrentState(userId, TaskState.getInstance("loggedIn"));
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
		TaskState currentState = getCurrentState(userId);
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
	private Object handleEvent(UUID userId, CreateTagEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");
		TaskState currentState = getCurrentState(userId);
		if (currentState == TaskState.getInstance("contextualClick")) {
			// save in context: id <-> tag
			// send: src is draggable/droppable	
			setCurrentState(userId, TaskState.getInstance("creatingTag"));
			IUser user = getInterpret().getUser(userId);
			user.getContext().addObject("content(" + event.getId() + ").type", event.getType());
			user.getContext().addObject("content(" + event.getId() + ").tag", event.getTag());
			logger.info("Adding (" + "content(" + event.getId() + ").type" + "," + event.getType() + ") to context("
					+ userId + ")!");
			logger.info("Adding (" + "content(" + event.getId() + ").tag" + "," + event.getTag() + ") to context("
					+ userId + ")!");
			if (event.getType().equalsIgnoreCase("text")) {
				user.getContext().addObject("content(" + event.getId() + ").text", event.getText());
				logger.info("Adding (" + "content(" + event.getId() + ").text" + "," + event.getText()
						+ ") to context(" + userId + ")!");
			}
			setCurrentState(userId, TaskState.getInstance("loggedIn"));
			return new String[] { "markAsTagged", event.getId(), event.getType(), event.getTag() };
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, ContextClosedEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");

		TaskState currentState = getCurrentState(userId);
		if (currentState == TaskState.getInstance("contextualClick")) {
			logger.info("User left context menu!");
			setCurrentState(userId, TaskState.getInstance("loggedIn"));
			return null;
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, RightClickEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");

		TaskState currentState = getCurrentState(userId);
		if (currentState == TaskState.getInstance("loggedIn")) {
			setCurrentState(userId, TaskState.getInstance("contextualClick"));
			IUser user = getInterpret().getUser(userId);
			// if (src.isTagged()) -> 
			if (user.getContext().containsKey("content(" + event.getId() + ").tag")) {
				String tag = (String) user.getContext().getObject("content(" + event.getId() + ").tag");

				String text = "";
				if (tag.equalsIgnoreCase("person")) {
					text = "Search in Facebook for this person!";
				} else {
					text = "Search in Business.com for this organization!";
				}

				if (event.getType().equalsIgnoreCase("image")) {
					//// if (src.isImage()) -> showImageContextMenu() TODO
					return new String[] { "showSelectionMenu1", event.getId(), event.getX(), event.getY(), text };
				} else {
					return new String[] { "showSelectionMenu2", event.getId(), event.getX(), event.getY(), text,
							"Use Google-Search instead!" };
				}
			}
			// else -> CREATE_TAG -> send context menu to let user choose
			else {
				logger.info("This is an untagged element. Please show context menu to let user manually tag the item!");
				return new String[] { "showTagCreationMenu", event.getId(), event.getX(), event.getY(), event.getType() };
			}
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@SuppressWarnings("unused")
	private Object handleEvent(UUID userId, DnDEvent event) throws Exception {
		logger.info("Handling event (" + event + ") for (" + userId + ") and return this");

		TaskState currentState = getCurrentState(userId);
		if (currentState == TaskState.getInstance("loggedIn")) {
			setCurrentState(userId, TaskState.getInstance("dragNDropHappened"));
			IUser user = getInterpret().getUser(userId);
			// if (src.isTagged()) -> 
			if (user.getContext().containsKey("content(" + event.getSourceId() + ").tag")) {
				String tag = (String) user.getContext().getObject("content(" + event.getSourceId() + ").tag");

				user.getContext().addObject("content(" + event.getTargetId() + ").type", event.getTargetType());
				user.getContext().addObject("content(" + event.getTargetId() + ").tag", tag);
				logger.info("Adding (" + "content(" + event.getTargetId() + ").type" + "," + event.getTargetType()
						+ ") to context(" + userId + ")!");
				logger.info("Adding (" + "content(" + event.getTargetId() + ").tag" + "," + tag + ") to context("
						+ userId + ")!");
			}
			setCurrentState(userId, TaskState.getInstance("loggedIn"));
		}
		logger.info("Could not handle event (" + event + ") for (" + userId
				+ ") as we are currently in the wrong state (" + currentState + ")!");
		throw new Exception();
	}

	@Override
	public Object dispatchEvent(UUID userId, ITaskEvent event) throws Exception {
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
