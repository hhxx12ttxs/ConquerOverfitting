/*
 *      Copyright 2009-2010 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package com.google.code.sagetvaddons.sagealert.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.code.sagetvaddons.sagealert.shared.HandlerService;
import com.google.code.sagetvaddons.sagealert.shared.NotificationServerSettings;
import com.google.code.sagetvaddons.sagealert.shared.SageAlertEvent;
import com.google.code.sagetvaddons.sagealert.shared.SageAlertEventMetadata;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Server side implementation of the HandlerService RPC interface
 * @author dbattams
 * @version $Id: HandlerServiceImpl.java 1022 2010-09-19 01:06:11Z derek@battams.ca $
 */
@SuppressWarnings("serial")
public final class HandlerServiceImpl extends RemoteServiceServlet implements HandlerService {
	
	//static private final Logger LOG = Logger.getLogger(HandlerServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.google.code.sagetvaddons.sagealert.client.HandlerService#getHandlers(com.google.code.sagetvaddons.sagealert.client.SupportedEvent)
	 */
	public List<NotificationServerSettings> getHandlers(String event) {
		return DataStore.getInstance().getHandlers(event);
	}

	/* (non-Javadoc)
	 * @see com.google.code.sagetvaddons.sagealert.client.HandlerService#setHandlers(com.google.code.sagetvaddons.sagealert.client.SupportedEvent, java.util.List)
	 */
	public void setHandlers(String e, List<NotificationServerSettings> handlers) {
		SageAlertEventHandlerManager mgr = SageAlertEventHandlerManager.get();
		mgr.removeAllHandlers(e);
		for(NotificationServerSettings s : handlers) {
			mgr.addHandler(NotificationServerFactory.getInstance(s), e);
		}
	}
	
	public void testServer(NotificationServerSettings settings) {
		SageAlertEventHandlerManager mgr = SageAlertEventHandlerManager.get();
		SageAlertEvent e = new SageAlertEvent() {

			public String getLongDescription() {
				return "This is a test event; if received your SageAlert installation is working properly!";
			}

			public String getMediumDescription() {
				return getLongDescription();
			}

			public String getShortDescription() {
				return getLongDescription();
			}

			public String getSource() {
				return "SageAlert_TestEvent";
			}

			public String getSubject() {
				return "Test event fired!";
			}
			
		};
		SageAlertEventHandler h = NotificationServerFactory.getInstance(settings);
		mgr.addHandler(h, "SageAlert_TestEvent");
		mgr.fire(e);
		mgr.removeHandler(h, "SageAlert_TestEvent");
	}

	public SageAlertEventMetadata getMetadata(String eventId) {
		return SageAlertEventMetadataManager.get().getMetadata(eventId);
	}

	public Collection<SageAlertEventMetadata> getAllMetadata() {
		return Arrays.asList(SageAlertEventMetadataManager.get().getAllMetadata());
	}

	public void saveMetadata(SageAlertEventMetadata data) {
		DataStore ds = DataStore.getInstance();
		String id = data.getEventId();
		ds.setSetting(id + SageAlertEventMetadata.SUBJ_SUFFIX, data.getSubject());
		ds.setSetting(id + SageAlertEventMetadata.SHORT_SUFFIX, data.getShortMsg());
		ds.setSetting(id + SageAlertEventMetadata.MED_SUFFIX, data.getMedMsg());
		ds.setSetting(id + SageAlertEventMetadata.LONG_SUFFIX, data.getLongMsg());
		SageAlertEventMetadataManager.get().putMetadata(data);
	}
}

