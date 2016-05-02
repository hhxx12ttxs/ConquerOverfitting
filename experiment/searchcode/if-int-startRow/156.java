package org.imogene.notif.web.gwt.client;

import java.util.List;
import java.util.Vector;

import org.imogene.notif.web.gwt.remote.NotificationServiceFacade;
import org.imogene.web.gwt.common.criteria.BasicCriteria;
import org.imogene.web.gwt.common.criteria.CriteriaConstants;
import org.imogene.web.gwt.common.criteria.ImogConjunction;
import org.imogene.web.gwt.common.criteria.ImogDisjunction;
import org.imogene.web.gwt.common.criteria.ImogJunction;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import org.imogene.gwt.widgets.client.dynaTable.DynaTableDataProvider;

/**
 * Data provider for dynatable that 
 * displays the notification template list 
 * @author Medes-IMPS
 */
public class NotificationTemplateDataProvider implements DynaTableDataProvider {

	private int lastMaxRows = -1;
	private NotificationTemplate[] lastEntity;
	private int lastStartRow = -1;
	private List<String> rowDataIds = new Vector<String>();
	private RowDataAcceptor acceptor;
	private int startRow;
	private ImogJunction searchCriterions;
	/* Total number of rows corresponding
	to query without pagination*/
	private int totalNbOfRows = 0;

	/**
	 * Constructs the data provider
	 */
	public NotificationTemplateDataProvider() {
	}

	/**
	 * Constructs the data provider
	 */
	public NotificationTemplateDataProvider(ImogJunction searchCriterions) {
		this.searchCriterions = searchCriterions;
	}

	/**
	 * Update data with entities contained in database
	 *
	 * @param startRow first row to update
	 * @param maxRows number of row to update
	 * @param acceptor data acceptor
	 * @param sortProperty bean property used to sort rows
	 * @param sortOrder sort order (true is ascending order)	 	 
	 */
	public void updateRowData(final int startRow, final int maxRows,
			final RowDataAcceptor acceptor, String sortProperty,
			Boolean sortOrder) {
		this.acceptor = acceptor;
		this.startRow = startRow;
		/* Check the simple cache first. */
		if (startRow == lastStartRow) {
			if (maxRows == lastMaxRows) {
				pushResults(acceptor, startRow, lastEntity, totalNbOfRows);
				return;
			}
		}

		/* get data from service */
		if (searchCriterions != null)
			NotificationServiceFacade.getInstance().listNotification(startRow, maxRows,
					searchCriterions, sortProperty, sortOrder,
					new PushResultCallback());
		else {
			NotificationServiceFacade facade = NotificationServiceFacade.getInstance();
			if (facade == null) {
				Window.alert("facade is null ! ");
			} else {
				facade.listNotification(startRow, maxRows, sortProperty, sortOrder,
						new PushResultCallback());
			}

		}
	}

	/**
	 * Update data with entities contained in database
	 *
	 * @param startRow first row to update
	 * @param maxRows number of row to update
	 * @param acceptor data acceptor
	 */
	public void updateRowData(final int startRow, final int maxRows,
			final RowDataAcceptor acceptor) {
		updateRowData(startRow, maxRows, acceptor, null, true);
	}

	/** 
	 * Returns Ids of entities in the list
	 *
	 * @return list of entity ids
	 */
	public List<String> getRowDataIds() {
		return rowDataIds;
	}

	/**
	 * push data to update the table
	 *
	 * @param acceptor data acceptor
	 * @param startRow first row index
	 * @param array entities retrieve from data server
	 */
	private void pushResults(RowDataAcceptor acceptor, int startRow,
			NotificationTemplate[] array, int totalNbRows) {
		Widget[][] rows = new Widget[array.length][];
		rowDataIds.clear();
		for (int i = 0, n = rows.length; i < n; i++) {
			NotificationTemplate card = array[i];

			rows[i] = new Widget[2];

			/* patient  column */

			if (card.getName()!= null)
				rows[i][0] = new HTML(card.getName());

			/* resultDate  column */

			if (card.getTitle() != null) {
				rows[i][1] = new HTML(card.getTitle());
			}

			rowDataIds.add(card.getId());
		}
		acceptor.accept(startRow, rows, toString(rowDataIds.toArray()),
				totalNbRows);
	}

	/**
	 * Configures the search criterions to search
	 * the parameter text among the bean column fields
	 *
	 * @param text string to be searched in the bean column fields
	 */
	public void fullTextSearch(String text) {
		if (text == null || (text != null && text.equals(""))) {
			this.searchCriterions = null;
		} else {
			ImogJunction main = new ImogConjunction();
			ImogJunction junction = new ImogDisjunction();

			BasicCriteria nameCrit = new BasicCriteria();
			nameCrit.setField("name");
			nameCrit.setOperation(CriteriaConstants.STRING_OPERATOR_CONTAINS);
			nameCrit.setValue(text);
			junction.add(nameCrit);

			BasicCriteria titleCrit = new BasicCriteria();
			titleCrit.setField("title");
			titleCrit.setOperation(CriteriaConstants.STRING_OPERATOR_CONTAINS);
			titleCrit.setValue(text);
			junction.add(titleCrit);

			main.add(junction);
			this.searchCriterions = main;
		}
	}

	/**
	 * Converts rowIds list to an array of string.
	 */
	private String[] toString(Object[] object) {
		String[] toString = new String[object.length];
		for (int i = 0; i < object.length; i++) {
			toString[i] = (String) object[i];
		}
		return toString;
	}

	/* ********** Callback classes ********** */

	/**
	 * Callback of the remote call that fills the table
	 */
	private class PushResultCallback implements AsyncCallback<List<NotificationTemplate>> {

		public void onFailure(Throwable caught) {
			acceptor.failed(caught);
			Window.alert(caught.getMessage());
		}

		public void onSuccess(List<NotificationTemplate> result) {
			NotificationTemplate[] entityArray = new NotificationTemplate[result.size()];
			for (int i = 0; i < entityArray.length; i++) {
				entityArray[i] = result.get(i);
			}
			lastEntity = entityArray;

			/* get total nb of rows from service */
			if (searchCriterions != null)
				NotificationServiceFacade.getInstance().countNotification(
						searchCriterions, new CountCallback());
			else
				NotificationServiceFacade.getInstance().countNotification(
						new CountCallback());
		}
	}

	/**
	 * Callback of the remote call that counts the total nb
	 * of rows retrieved for query (without pagination)
	 */
	private class CountCallback implements AsyncCallback<Integer> {

		public void onFailure(Throwable caught) {
			acceptor.failed(caught);
		}

		public void onSuccess(Integer result) {
			totalNbOfRows = result.intValue();
			pushResults(acceptor, startRow, lastEntity, totalNbOfRows);
		}
	}
}

