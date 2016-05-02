package org.imogene.web.gwt.client.ui.field.paginatedList;

import java.util.List;

import org.imogene.web.gwt.common.criteria.ImogJunction;
import org.imogene.web.gwt.common.entity.ImogBean;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Data provider for the list box
 * that displays a big quantity of data
 * @author MEDES-IMPS
 */
public abstract class AbstractImogListBoxDataProvider implements ImogPaginatedListBoxDataProvider{

	protected int startRow = -1;
	private int lastStartRow;
	private int lastMaxRows = -1;
	protected ImogBean[] lastBean;
	protected ImogJunction searchCriterions;
	protected ImogJunction filterParameters = null;
	protected boolean isFiltered = false;
	protected RowDataAcceptor acceptor;
	
	// Total number of rows corresponding to query without pagination
	private int totalNbOfRows = 0;
	

	/*
	 * (non-Javadoc)
	 * @see org.imogene.web.gwt.client.ui.field.paginatedList.ImogenePaginatedListBoxDataProvider#updateRowData(int, int, org.imogene.web.gwt.client.ui.field.paginatedList.ImogenePaginatedListBoxDataProvider.RowDataAcceptor)
	 */
	public void updateRowData(int startRow, int maxRows, RowDataAcceptor acceptor) {
		this.startRow = startRow;
		this.acceptor = acceptor;
		/* Check the simple cache first. */
		if (startRow == lastStartRow) {
			if (maxRows == lastMaxRows) {
				pushResults(acceptor, startRow, lastBean, totalNbOfRows);
				return;
			}
		}	
		getData(startRow, maxRows);
	}
	
	/**
	 * Asynchronous call to the server.
	 * Gets the data that will fill the MedanyPaginatedList.
	 */
	protected abstract void getData(int startRow, int maxRows);
	
	/**
	 * Asynchronous call to the server.
	 * Counts the total number of rows of the query (without pagination)
	 */
	protected abstract void countDataRows();
	
		
	/**
	 * push the result received following the database request
	 * @param acceptor the row acceptor
	 * @param startRow the index of the starting row
	 * @param array the array of data
	 */
	private void pushResults(RowDataAcceptor acceptor, int startRow, ImogBean[] array, int totalNbOfRows) {	
		acceptor.accept(startRow, array, totalNbOfRows);
	}

	/**
	 * Sets criterions for which values have to be temporaly searched
	 * @param criterions ImogJunction including the  criterions 
	 * for which the values have to be searched
	 */
	public void setSearchCriterions(ImogJunction criterions) {
		this.searchCriterions = criterions;
	}
	
	/**
	 * Sets criterions for which values have to be permanently filtered
	 * @param criterions ImogJunction including the  criterions 
	 * for which the values have to be filtered
	 */
	public void setFilterParameters(ImogJunction criterions) {
		this.isFiltered = true;
		filterParameters = criterions;
	}
	
	/**
	 * Sets if the dataprovider is permanently filtered
	 * @param isFiltered true if the dataprovider is permanently filtered
	 */
	public void setIsFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}
	
	/**
	 * Gets if the dataprovider is permanently filtered
	 * @return true if the dataprovider is permanently filtered
	 */
	public boolean isFiltered() {
		return isFiltered;
	}
	
	


	/* ********** Callback classes ********** */

	/**
	 * Callback of the remote call that fills the table
	 */
/*	public class PushResultCallback implements AsyncCallback<List<? extends ImogBean>> {

		public void onFailure(Throwable caught) {
			acceptor.failed(caught);
		}

		public void onSuccess(List<? extends ImogBean> result) {
			
			ImogBean[] beanArray = new ImogBean[result.size()];
			for (int i = 0; i < beanArray.length; i++) {
				beanArray[i] = result.get(i);
			}
			lastBean = beanArray;
			
			 get total nb of rows from service 
			countDataRows();
		}
		
	}*/
	
	/**
	 * Callback of the remote call that counts the total nb
	 * of rows retrieved for query (without pagination)
	 */
	public class CountCallback implements AsyncCallback<Integer> {

		public void onFailure(Throwable caught) {
			acceptor.failed(caught);
		}

		public void onSuccess(Integer result) {
			totalNbOfRows = result.intValue();
			pushResults(acceptor, startRow, lastBean, totalNbOfRows);
		}
	}
}

