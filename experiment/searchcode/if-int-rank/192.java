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
package com.wls.pt.client.models.pojo;

import java.util.Date;

import com.google.gwt.event.shared.SimpleEventBus;
import com.wls.pt.client.ModelHolder;
import com.wls.pt.client.models.IStep;
import com.wls.pt.client.models.json.ModelJso;
import com.wls.pt.client.presenters.StepTreeItemPresenter;
import com.wls.pt.client.presenters.TreeItemPresenter;
import com.wls.pt.client.presenters.TreeItemPresenter.TreeItemDisplay;
import com.wls.pt.client.views.StepTreeItemView;

public abstract class Step extends Model implements IStep {

	private String parentPk;
	private int rank;

	public Step(ModelJso mp) {
		super(mp);
		this.parentPk = mp.getParentPk();
		this.rank = mp.getRank();
	}

	public Step(String pk, boolean abandoned, Date created, boolean deleted,
			boolean editable, Date modified, String name, String parentPk, int rank,
			String information) {
		super(pk, abandoned, created, deleted, editable, modified, name,
				information);
		this.parentPk = parentPk;
		this.rank = rank;
	}

	@Override
	public TreeItemPresenter asTreeItemPresenter(SimpleEventBus eventBus,
			ModelHolder models) {
		return new StepTreeItemPresenter(getTreeItemView(), eventBus, this, models);
	}

	@Override
	public String getParentPk() {
		return parentPk;
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public void setParentPk(String parentPk) {
		this.parentPk = parentPk;
	}

	@Override
	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public void update(Model model) {
		super.update(model);
		this.parentPk = ((IStep) model).getParentPk();
		this.rank = ((IStep) model).getRank();
	}

	protected TreeItemDisplay getTreeItemView() {
		return new StepTreeItemView();
	}

}

