/*******************************************************************************
 * Copyright 2012 Korey Robert Peters
 * 
 * This file is part of ProcessTrain.
 * 
 * ProcessTrain is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * ProcessTrain is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public 
 * License along with ProcessTrain. If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package com.wls.pt.client.presenters;

import java.util.Date;
import java.util.UUID;

import junit.framework.TestCase;

import com.google.gwt.event.shared.SimpleEventBus;
import com.wls.pt.client.MockEventBus;
import com.wls.pt.client.models.pojo.ClientUsers;
import com.wls.pt.client.models.pojo.LiveProcess;
import com.wls.pt.client.models.pojo.LiveTaskStep;
import com.wls.pt.client.models.pojo.MasterLive;
import com.wls.pt.client.models.pojo.MasterTemplates;
import com.wls.pt.client.models.pojo.MockClient;
import com.wls.pt.client.models.pojo.MockLiveProcess;
import com.wls.pt.client.models.pojo.MockLiveTaskStep;
import com.wls.pt.client.models.pojo.MockMasterLive;
import com.wls.pt.client.models.pojo.MockMasterTemplates;
import com.wls.pt.client.models.pojo.MockTemplateProcessInstance;
import com.wls.pt.client.models.pojo.MockTemplateTaskStep;
import com.wls.pt.client.models.pojo.Model;
import com.wls.pt.client.models.pojo.Process;
import com.wls.pt.client.models.pojo.ProcessTreeUser;
import com.wls.pt.client.models.pojo.RankedParentedProcess;
import com.wls.pt.client.models.pojo.RankedProcess;
import com.wls.pt.client.models.pojo.Step;
import com.wls.pt.client.models.pojo.TemplateProcess;
import com.wls.pt.client.models.pojo.TemplateProcessInstance;
import com.wls.pt.client.models.pojo.TemplateTaskStep;
import com.wls.pt.client.views.MockLiveProcessView;
import com.wls.pt.client.views.MockLiveTaskStepView;

public abstract class PtTestCase extends TestCase {

	protected RankedProcess client;
	protected Step clientUsers;
	protected MockEventBus eventBus = new MockEventBus();
	protected LiveProcess liveProcess1;
	protected LiveProcess liveProcess2;
	protected LiveTaskStep liveTaskStep1;
	protected LiveTaskStep liveTaskStep10;
	protected LiveTaskStep liveTaskStep2;
	protected LiveTaskStep liveTaskStep3;
	protected LiveTaskStep liveTaskStep4;
	protected LiveTaskStep liveTaskStep5;
	protected LiveTaskStep liveTaskStep6;
	protected LiveTaskStep liveTaskStep7;
	protected LiveTaskStep liveTaskStep8;
	protected LiveTaskStep liveTaskStep9;
	protected MasterLive masterLive;
	protected MasterTemplates masterTemplates;
	protected MasterLive personalLive;
	protected MasterTemplates personalTemplates;
	protected TemplateProcess templateProcess1;
	protected TemplateTaskStep templateTaskStep1;
	protected TemplateTaskStep templateTaskStep2;
	protected TemplateTaskStep templateTaskStep3;
	protected TemplateTaskStep templateTaskStep4;
	protected TemplateTaskStep templateTaskStep5;
	protected TemplateProcessInstance tpi1;
	protected RankedParentedProcess user;

	public PtTestCase() {
		super();
	}

	public PtTestCase(String name) {
		super(name);
	}

	public RankedProcess createClientProcess(String name) {
		return new MockClient(getRandomPk(), false, getDateNow(), false,
				getDateNow(), name, "");
	}

	public Step createClientUsers(RankedProcess parent) {
		return new ClientUsers(getRandomPk(), false, getDateNow(), false,
				getDateNow(), "ClientUsers", parent.getPk(), 2, "");
	}

	public LiveProcess createLiveProcess(Model parent, String name, int rank,
			String processTemplatePk) {
		return createLiveProcess(getRandomPk(), parent, name, rank,
				processTemplatePk, null, false, false, null);
	}

	public LiveProcess createLiveProcess(String pk, Model parent, String name,
			int rank, String processTemplatePk, Date completedOn, boolean isCurrent,
			boolean isComplete, Date startedOn) {
		return new MockLiveProcess(pk, false, getDateNow(), false, true,
				getDateNow(), name, parent.getPk(), rank, "", processTemplatePk,
				completedOn, isCurrent, isComplete, startedOn);
	}

	public LiveProcessPresenter createLiveProcessPresenter(Process process,
			SimpleEventBus eventBus) {
		return new LiveProcessPresenter(process, new MockLiveProcessView(),
				eventBus);
	}

	public LiveTaskStep createLiveTaskStep(Process parent, String name, int rank) {
		return createLiveTaskStep(getRandomPk(), parent, name, rank);
	}

	public LiveTaskStep createLiveTaskStep(String pk, Process parent,
			String name, int rank) {
		return new MockLiveTaskStep(pk, false, getDateNow(), false, true,
				getDateNow(), name, parent.getPk(), rank, "", getDateNow(), false,
				getDateNow(), false);
	}

	public LiveTaskStepPresenter createLiveTaskStepPresenter(Step step,
			SimpleEventBus eventBus) {
		return new LiveTaskStepPresenter(step, new MockLiveTaskStepView(), eventBus);
	}

	public MasterLive createMasterLive(RankedProcess client) {
		return new MockMasterLive(getRandomPk(), false, getDateNow(), false,
				getDateNow(), "MasterLive", client.getPk(), 0, "");
	}

	public MasterTemplates createMasterTemplates(RankedParentedProcess user) {
		return new MasterTemplates(getRandomPk(), false, getDateNow(), false,
				getDateNow(), "MasterTemplates", user.getPk(), 1, "");
	}

	public MasterTemplates createMasterTemplates(RankedProcess client) {
		return new MockMasterTemplates(getRandomPk(), false, getDateNow(), false,
				getDateNow(), "MasterTemplates", client.getPk(), 1, "");
	}

	public RankedParentedProcess createProcessTreeUser(Step parent,
			RankedProcess client, String name) {
		return new ProcessTreeUser(getRandomPk(), false, getDateNow(), false,
				getDateNow(), name, parent.getPk(), 0, "", client.getPk(), 4);
	}

	private MasterLive createMasterLive(RankedParentedProcess user) {
		return new MasterLive(getRandomPk(), false, getDateNow(), false,
				getDateNow(), "MasterLive", user.getPk(), 1, "");
	}

	private TemplateProcess createTemplateProcess(String name, int rank) {
		return new TemplateProcess(getRandomPk(), false, getDateNow(), false, true,
				getDateNow(), name, rank, "");
	}

	private TemplateProcessInstance createTemplateProcessInstance(
			MasterTemplates parent, TemplateProcess template, int rank) {
		return new MockTemplateProcessInstance(getRandomPk(), false, getDateNow(),
				false, true, getDateNow(), template.getName(), parent.getPk(), rank,
				template.getPk(), "");
	}

	private TemplateTaskStep createTemplateTaskStep(TemplateProcess parent,
			String name, int rank) {
		return new MockTemplateTaskStep(getRandomPk(), false, getDateNow(), false,
				true, getDateNow(), name, parent.getPk(), rank, "");
	}

	private Date getDateNow() {
		return new Date();
	}

	protected String getRandomPk() {
		return UUID.randomUUID().toString();
	}

	@Override
	protected void setUp() {

		client = createClientProcess("Client1");
		clientUsers = createClientUsers(client);
		user = createProcessTreeUser(clientUsers, client, "User1");
		masterLive = createMasterLive(client);
		masterTemplates = createMasterTemplates(client);
		templateProcess1 = createTemplateProcess("TemplateProcess1", 0);
		templateTaskStep1 = createTemplateTaskStep(templateProcess1,
				"TemplateTaskStep1", 0);
		templateTaskStep2 = createTemplateTaskStep(templateProcess1,
				"TemplateTaskStep2", 1);
		templateTaskStep3 = createTemplateTaskStep(templateProcess1,
				"TemplateTaskStep3", 2);
		templateTaskStep4 = createTemplateTaskStep(templateProcess1,
				"TemplateTaskStep4", 3);
		templateTaskStep5 = createTemplateTaskStep(templateProcess1,
				"TemplateTaskStep5", 4);
		tpi1 = createTemplateProcessInstance(masterTemplates, templateProcess1, 0);
		liveProcess1 = createLiveProcess(masterLive, "LiveProcess1", 0, null);
		liveProcess2 = createLiveProcess(masterLive, "LiveProcess2", 1, null);
		liveTaskStep1 = createLiveTaskStep(liveProcess1, "LiveTaskStep1", 0);
		liveTaskStep2 = createLiveTaskStep(liveProcess1, "LiveTaskStep2", 1);
		liveTaskStep3 = createLiveTaskStep(liveProcess1, "LiveTaskStep3", 2);
		liveTaskStep4 = createLiveTaskStep(liveProcess1, "LiveTaskStep4", 3);
		liveTaskStep5 = createLiveTaskStep(liveProcess1, "LiveTaskStep5", 4);
		liveTaskStep6 = createLiveTaskStep(liveProcess2, "LiveTaskStep6", 0);
		liveTaskStep7 = createLiveTaskStep(liveProcess2, "LiveTaskStep7", 1);
		liveTaskStep8 = createLiveTaskStep(liveProcess2, "LiveTaskStep8", 2);
		liveTaskStep9 = createLiveTaskStep(liveProcess2, "LiveTaskStep9", 3);
		liveTaskStep10 = createLiveTaskStep(liveProcess2, "LiveTaskStep10", 4);
		personalLive = createMasterLive(user);
		personalTemplates = createMasterTemplates(user);
	}
}

