/**
 * The Logic Lab
 * @author jpk
 * May 8, 2008
 */
package com.tll.client.ui.listing;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.tll.client.listing.IRowOptionsDelegate;
import com.tll.client.ui.option.Option;

/**
 * AbstractRowOptions - An adapter for {@link IRowOptionsDelegate} simplifying
 * the implementation.
 * @author jpk
 */
public abstract class AbstractRowOptions implements IRowOptionsDelegate {

	/**
	 * The edit row image bundle.
	 */
	private static final EditRowImageBundle imageBundle = (EditRowImageBundle) GWT.create(EditRowImageBundle.class);

	private Option[] staticOptions;

	/**
	 * @return The prefix text used for the edit option.
	 */
	protected String editOptionPrefix() {
		return "Edit ";
	}

	/**
	 * @return The prefix text used for the delete option.
	 */
	protected String deleteOptionPrefix() {
		return "Delete ";
	}

	/**
	 * Generates an edit option with the given name.
	 * <p>
	 * FORMAT: "Edit {subjectName}"
	 * @param subjectName
	 * @return New Option instance
	 */
	private Option editOption(String subjectName) {
		return new Option(editOptionPrefix() + subjectName, AbstractImagePrototype.create(imageBundle.edit()).createImage());
	}

	/**
	 * Generates a delete option with the given name.
	 * <p>
	 * FORMAT: "Delete {subjectName}"
	 * @param subjectName
	 * @return New Option instance
	 */
	private Option deleteOption(String subjectName) {
		return new Option(deleteOptionPrefix() + subjectName, AbstractImagePrototype.create(imageBundle.delete()).createImage());
	}
	
	/**
	 * Does the option text indicate edit?
	 * @param optionText
	 * @return true/false
	 */
	private boolean isEditOption(String optionText) {
		return optionText == null ? false : optionText.startsWith(editOptionPrefix());
	}

	/**
	 * Does the option text indicate delete?
	 * @param optionText
	 * @return true/false
	 */
	private boolean isDeleteOption(String optionText) {
		return optionText == null ? false : optionText.startsWith(deleteOptionPrefix());
	}

	/**
	 * @return The listing element name.
	 */
	protected abstract String getListingElementName();

	/**
	 * @return <code>true</code> when the row options are constant for all
	 *         elidgible rows.
	 */
	protected boolean isStaticOptions() {
		return true;
	}

	protected boolean isRowEditable(int rowIndex) {
		return true;
	}

	protected boolean isRowDeletable(int rowIndex) {
		return true;
	}

	protected Option[] getCustomRowOps(int rowIndex) {
		return null;
	}

	/**
	 * This method is invoked whtn a row is targeted for editing.
	 * @param rowIndex The row index of the targeted row
	 */
	protected void doEditRow(int rowIndex) {
		// no-op
	}

	/**
	 * This method is invoked when a row is targeted for deletion.
	 * @param rowIndex The row index of the targeted row
	 */
	protected void doDeleteRow(int rowIndex) {
		// no-op
	}

	/**
	 * Performs custom row ops other that edit and delete.
	 * @param optionText The option text of the selection option
	 * @param rowIndex The row index of the targeted row
	 */
	protected void handleRowOp(String optionText, int rowIndex) {
		// no-op
	}

	@Override
	public final Option[] getOptions(int rowIndex) {
		if(isStaticOptions() && staticOptions != null) {
			return staticOptions;
		}
		final List<Option> options = new ArrayList<Option>();
		if(isRowEditable(rowIndex)) options.add(editOption(getListingElementName()));
		if(isRowDeletable(rowIndex)) options.add(deleteOption(getListingElementName()));
		final Option[] customOps = getCustomRowOps(rowIndex);
		if(customOps != null) {
			for(final Option o : customOps) {
				options.add(o);
			}
		}
		final Option[] arr = options.toArray(new Option[options.size()]);
		if(isStaticOptions()) {
			staticOptions = arr;
		}
		return arr;
	}

	@Override
	public final void handleOptionSelection(String optionText, int rowIndex) {
		if(isEditOption(optionText)) {
			doEditRow(rowIndex);
		}
		else if(isDeleteOption(optionText)) {
			doDeleteRow(rowIndex);
		}
		else {
			handleRowOp(optionText, rowIndex);
		}
	}
}
