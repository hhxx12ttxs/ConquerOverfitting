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

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.HasWidgets;
import com.wls.pt.client.ModelHolder;
import com.wls.pt.client.events.ModelsLoadedEvent;
import com.wls.pt.client.events.ModelsLoadedEventHandler;
import com.wls.pt.client.events.ShowProcessEvent;
import com.wls.pt.client.events.ShowProcessEventHandler;
import com.wls.pt.client.events.ShowStepEvent;
import com.wls.pt.client.events.ShowStepEventHandler;
import com.wls.pt.client.events.TreeItemSelectedEvent;
import com.wls.pt.client.models.pojo.LiveTaskStep;
import com.wls.pt.client.models.pojo.Model;
import com.wls.pt.client.models.pojo.RankedProcess;
import com.wls.pt.client.models.pojo.Step;
import com.wls.pt.client.models.pojo.TemplateProcessInstance;
import com.wls.pt.client.views.Display;

public abstract class TreeItemPresenter implements Presenter {

	public interface TreeItemDisplay extends Display {
		int _getChildCount();

		TreeItemDisplay _getTreeItemAt(int rank);

		void addChild(TreeItemDisplay view, int index);

		String getName();

		String getPk();

		int getRank();

		String getTemplatePk();

		TreeItemDisplay getTreeItemDisplay();

		TreeItemDisplay getTreeItemWidget();

		String getType();

		void setComplete(boolean complete);

		void setCurrent(boolean current);

		void setDeleted(boolean deleted);

		void setName(String name);

		void setPk(String pk);

		void setProcessTemplatePk(String pk);

		void setRank(int rank);

		void setSelected(boolean selected);

		void setType(String type);

		void sortChildren();
	}

	class GetCheckBoxClickHandler implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			eventBus.fireEvent(new TreeItemSelectedEvent(model.getPk()));
		}
	}

	private final HashMap<String, TreeItemPresenter> children;

	private final SimpleEventBus eventBus;
	protected final Model model;
	final TreeItemDisplay display;

	public TreeItemPresenter(final TreeItemDisplay display,
			final SimpleEventBus eventBus, final Model model, ModelHolder models) {
		this.children = new HashMap<String, TreeItemPresenter>();
		this.display = display;
		this.eventBus = eventBus;
		this.model = model;

		addAllApplicableChildren(models);

		eventBus.addHandler(ModelsLoadedEvent.TYPE, new ModelsLoadedEventHandler() {

			@Override
			public void onModelsLoaded(ModelsLoadedEvent event) {
				addAndSortChildren(event.getModels());
			}
		});

		eventBus.addHandler(ShowProcessEvent.TYPE, new ShowProcessEventHandler() {

			@Override
			public void onShowProcess(ShowProcessEvent event) {
				if (event.getPk().equals(model.getPk())) {
					display.setSelected(true);
				} else if (model.getModelType().equals("TemplateProcessInstance")
						&& event.getPk().equals(
								((TemplateProcessInstance) model).getProcessTemplatePk())) {
					display.setSelected(true);
				} else {
					display.setSelected(false);
				}
			}

		});

		eventBus.addHandler(ShowStepEvent.TYPE, new ShowStepEventHandler() {

			@Override
			public void onShowStep(ShowStepEvent event) {
				if (event.getPk().equals(model.getPk())) {
					display.setSelected(true);
				} else {
					display.setSelected(false);
				}
			}
		});

		updateWidget(model);
	}

	public void addChild(TreeItemPresenter child) {
		int rank;
		rank = getRankFromTreeItemPresenter(child);
		display.addChild(child.display.getTreeItemWidget(), rank);
	}

	@Override
	public void bind() {

	}

	public String getPkForAddingChildren() {
		if (model instanceof TemplateProcessInstance) {
			return ((TemplateProcessInstance) model).getProcessTemplatePk();
		} else {
			return model.getPk();
		}
	}

	@Override
	public void go(HasWidgets container) {
		// Unused
	}

	@Override
	public String toString() {
		return model.getModelType();
	}

	private void addAllApplicableChildren(ModelHolder models) {
		if (models != null) {
			addAndSortChildren(models);
		}
	}

	private int getRankFromModel(Model model) {
		int rank;
		if (model instanceof Step) {
			rank = ((Step) model).getRank();
		} else {
			rank = ((RankedProcess) model).getRank();
		}
		return rank;
	}

	private int getRankFromTreeItemPresenter(TreeItemPresenter child) {
		return getRankFromModel(child.model);
	}

	protected void addAndSortChildren(ModelHolder models) {
		HashMap<String, Model> newChildren = models.getChildren(model
				.getPkForAddingChildren());
		for (Model newChild : newChildren.values()) {
			TreeItemPresenter existingChild = children.get(newChild.getPk());
			if (existingChild == null) {
				TreeItemPresenter p;
				if (newChild instanceof Step) {
					p = ((Step) newChild).asTreeItemPresenter(eventBus, models);
				} else if (newChild instanceof RankedProcess) {
					p = ((RankedProcess) newChild).asTreeItemPresenter(eventBus, models);
				} else {
					continue;
				}
				children.put(newChild.getPk(), p);
				display.addChild(p.display, getRankFromModel(newChild));
			} else {
				existingChild.model.update(newChild);
				existingChild.updateWidget(newChild);
			}
		}
		display.sortChildren();
	}

	protected void updateWidget(Model newModel) {
		display.setPk(newModel.getPk());
		display.setRank(getRankFromModel(newModel));
		display.setName(newModel.getName());
		display.setType(newModel.getModelType());
		display.setDeleted(newModel.isDeleted());
		if (newModel.getModelType().equals("TemplateProcessInstance")) {
			display.setProcessTemplatePk(((TemplateProcessInstance) newModel)
					.getProcessTemplatePk());
		} else if (newModel.getModelType().equals("LiveTaskStep")) {
			display.setComplete(((LiveTaskStep) newModel).isComplete());
			display.setCurrent(((LiveTaskStep) newModel).isCurrent());
		}
	}
}

