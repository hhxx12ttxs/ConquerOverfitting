package net.mufly.client.ui.summary.month;

import java.util.List;

import net.mufly.client.MuflyConstants;
import net.mufly.client.core.ApplicationParameters;
import net.mufly.client.core.DefaultCallback;
import net.mufly.client.core.MuflyUtils;
import net.mufly.client.services.TransactionRemote;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TotalMonth extends Composite {

	private VerticalPanel mainPanel;
	private Grid yearPanel;

	private ListBox cmbYear;

	private Grid monthTable;


	private MuflyConstants constants;

	public TotalMonth() {
		init();
	}

	private void init() {
		constants = ApplicationParameters.getInstance().getApplication().getConstants();

		mainPanel = new VerticalPanel();
		yearPanel = new Grid(1,2);
		yearPanel.setStyleName("summaryTable-YearCombo");

		cmbYear = new ListBox();

		monthTable = new Grid(14,3);

		// Header
		monthTable.setText(0, 1, constants.month());
		monthTable.setText(0, 2, constants.accumulated());

		// Months
		monthTable.setText(1, 0, constants.january());
		monthTable.setText(2, 0, constants.february());
		monthTable.setText(3, 0, constants.march());
		monthTable.setText(4, 0, constants.april());
		monthTable.setText(5, 0, constants.may());
		monthTable.setText(6, 0, constants.june());
		monthTable.setText(7, 0, constants.july());
		monthTable.setText(8, 0, constants.august());
		monthTable.setText(9, 0, constants.september());
		monthTable.setText(10, 0, constants.october());
		monthTable.setText(11, 0, constants.november());
		monthTable.setText(12, 0, constants.december());
		monthTable.setText(13, 0, constants.total());
		for (int i=1; i<14; i++) {
			monthTable.getCellFormatter().setStyleName(i, 0, "summaryTable-List");
		}

		monthTable.getCellFormatter().setStyleName(0, 1, "summaryTable-Header");
		monthTable.getCellFormatter().setStyleName(0, 2, "summaryTable-Header");

		// Load the years of the transactions in the combo
		TransactionRemote.Util.getInstance().getYears(new DefaultCallback() {
			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object result) {
				List<Integer> list = (List<Integer>)result;
				for (Integer year : list) {
					cmbYear.addItem(String.valueOf(year));
				}
			}
		});

		cmbYear.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int year = Integer.parseInt(cmbYear.getItemText(cmbYear.getSelectedIndex()));

				TransactionRemote.Util.getInstance().getTotalByMonth(year, new DefaultCallback() {
					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(Object result) {
						updateTable((List<MonthDTO>)result);
					}
				});
			}
		});

		yearPanel.setText(0, 0, constants.year());
		yearPanel.setWidget(0, 1, cmbYear);

		mainPanel.add(yearPanel);
		mainPanel.add(monthTable);

		initWidget(mainPanel);
	}


	public void update() {
		// Just fire the event to get the data...
		cmbYear.fireEvent(new ChangeEvent() {
		});
	}

	private void updateTable(List<MonthDTO> listMonth) {
		double total = 0.0;

		for (int i=1; i<=12; i++) {
			monthTable.setText(i, 1, MuflyUtils.numberFormater.format(0.0));
			monthTable.setText(i, 2, MuflyUtils.numberFormater.format(0.0));

			if (i % 2 == 1) {
				monthTable.getCellFormatter().setStyleName(i, 1, "summaryTable-Positive-Odd");
				monthTable.getCellFormatter().setStyleName(i, 2, "summaryTable-Positive-Odd");
			} else {
				monthTable.getCellFormatter().setStyleName(i, 1, "positiveAmount");
				monthTable.getCellFormatter().setStyleName(i, 2, "positiveAmount");
			}
		}
		monthTable.getCellFormatter().setStyleName(13, 2, "summaryTable-Positive-Odd");

		int i=1;
		for (MonthDTO monthData : listMonth) {
			int month = monthData.getMonth();
			double totalMonth = monthData.getTotal();

			total += totalMonth;

			if (month % 2 == 1) {
				if (monthData.getTotal() < 0.0) {
					monthTable.getCellFormatter().setStyleName(month, 1, "summaryTable-Negative-Odd");
				}
				if (total < 0.0) {
					monthTable.getCellFormatter().setStyleName(month, 2, "summaryTable-Negative-Odd");
				}
			} else {
				if (monthData.getTotal() < 0.0) {
					monthTable.getCellFormatter().setStyleName(month, 1, "negativeAmount");
				}
				if (total < 0.0) {
					monthTable.getCellFormatter().setStyleName(month, 2, "negativeAmount");
				}
			}
			monthTable.setText(monthData.getMonth(), 1, MuflyUtils.numberFormater.format(totalMonth));
			monthTable.setText(monthData.getMonth(), 2, MuflyUtils.numberFormater.format(total));
			i++;
		}

		if (total < 0.0) monthTable.getCellFormatter().setStyleName(13, 2, "summaryTable-Negative-Odd");
		monthTable.setText(13, 2, MuflyUtils.numberFormater.format(total));
	}

}



