package org.coode.oae.ui;

import java.util.HashSet;
import java.util.Set;

import org.coode.oae.ui.VariableListModel.VariableListItem;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;

import uk.ac.manchester.mae.evaluation.BindingModel;

public class BindingViewer_modalwindows extends MList implements
		VerifiedInputEditor {
	private static final long serialVersionUID = -6626947893443460240L;

	@Override
	protected void handleAdd() {
		this.bindingEditor.setBindingModel(null);
		addBinding();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void handleDelete() {
		this.bindingModels
				.remove(((VariableListItem<BindingModel>) getSelectedValue())
						.getItem());
		handleChange();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void handleEdit() {
		this.bindingEditor
				.setBindingModel(((VariableListItem<BindingModel>) getSelectedValue())
						.getItem());
		addBinding();
	}

	protected final Set<BindingModel> bindingModels = new HashSet<BindingModel>();
	private final VariableListModel<BindingModel> bindingViewModel = new VariableListModel<BindingModel>(
			this.bindingModels, "Bindings");
	protected BindingEditor_modalwindows bindingEditor;

	public BindingViewer_modalwindows(OWLEditorKit kit) {
		this.bindingEditor = new BindingEditor_modalwindows(kit);
		setBorder(ComponentFactory.createTitledBorder("Bindings"));
		setModel(this.bindingViewModel);
		setCellRenderer(new RenderableObjectCellRenderer(kit));
		this.bindingViewModel.init();
	}

	public void clear() {
		this.bindingModels.clear();
		handleChange();
	}

	void addBinding() {
		BindingModel b = this.bindingEditor.getBindingModel();
		if (b != null) {
			this.bindingModels.remove(b);
			this.bindingModels.add(b);
			handleChange();
			for (InputVerificationStatusChangedListener i : this.listeners) {
				i.verifiedStatusChanged(true);
			}
		}
	}

	public Set<BindingModel> getBindingModels() {
		return this.bindingModels;
	}

	public void handleChange() {
		this.bindingViewModel.init();
	}

	private Set<InputVerificationStatusChangedListener> listeners = new HashSet<InputVerificationStatusChangedListener>();

	public void addStatusChangedListener(
			InputVerificationStatusChangedListener listener) {
		this.listeners.add(listener);
	}

	public void removeStatusChangedListener(
			InputVerificationStatusChangedListener listener) {
		this.listeners.remove(listener);
	}
}

