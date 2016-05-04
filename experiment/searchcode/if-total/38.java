package net.mufly.client.ui.summary.account;

import java.util.List;

import net.mufly.client.MuflyConstants;
import net.mufly.client.core.ApplicationParameters;
import net.mufly.client.core.DefaultCallback;
import net.mufly.client.core.MuflyUtils;
import net.mufly.client.services.TransactionRemote;
import net.mufly.domain.Account;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TotalAccount extends Composite {

	private VerticalPanel mainPanel;
	private FlexTable accountTable;

	private MuflyConstants constants;

	private int tmpColourRow = 0;

	public TotalAccount() {
		init();
	}

	private void init() {
		constants = ApplicationParameters.getInstance().getApplication().getConstants();

		mainPanel = new VerticalPanel();

		accountTable = new FlexTable();
		accountTable.setStyleName("summaryTable-AccountTable");

		// Header
		accountTable.setText(0, 1, constants.total());
		accountTable.getRowFormatter().setStyleName(0, "summaryTable-Header");

		// Get the account list
		ApplicationParameters.getInstance().getApplication().getAccountManager().addValueChangeHandler(new ValueChangeHandler<List<Account>>() {
			public void onValueChange(
					ValueChangeEvent<List<Account>> event) {
				updateAccountList(event.getValue());
			}
		});

		mainPanel.add(accountTable);

		initWidget(mainPanel);
	}

	public void update() {
		TransactionRemote.Util.getInstance().getTotalByAccount(
				new DefaultCallback() {

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(Object result) {
						updateAccountTotal((List<AccountDTO>) result);
					}
				});
	}

	private void updateAccountList(List<Account> listAccount) {
		MuflyUtils.removeRows(accountTable);

		for (int i=0; i<listAccount.size(); i++) {
			accountTable.setText(i+1, 0, listAccount.get(i).getAccountName());
			accountTable.getCellFormatter().setStyleName(i+1, 0, "summaryTable-List");
		}
		accountTable.setText(listAccount.size()+1, 0, constants.total());
		accountTable.getCellFormatter().setStyleName(listAccount.size()+1, 0, "summaryTable-List");
	}

	private void updateAccountTotal(List<AccountDTO> listAccountTotal) {
		double total = 0.0;

		// Initialize the accounts total
		initializeAccounts();

		for (int i=0; i<listAccountTotal.size(); i++) {
			// Get the values of the current account
			AccountDTO oneAccount = listAccountTotal.get(i);
			double totalAccount = oneAccount.getTotal();
			String accountName = oneAccount.getAccount().getAccountName();

			// Find the account position in the accountTable
			int accountRow = lookupAccount(accountName);

			// Only if we find the account in the accountTable we update its values. The account can't be
			// found if it has been hidden but it has money.
			if (accountRow != -1) {
				total += totalAccount;
				accountTable.setText(accountRow, 1, MuflyUtils.numberFormater.format(totalAccount));

				// Only if the ammount is <0.0 we apply the style. Otherwise it
				// has been applied in the initialization
				if (totalAccount < 0.0) {
					if (accountRow % 2 == tmpColourRow) {
						accountTable.getCellFormatter().setStyleName(accountRow, 1, "summaryTable-Negative-Odd");
					} else {
						accountTable.getCellFormatter().setStyleName(accountRow, 1, "negativeAmount");
					}
				}
			}
		}

		// Footer: TOTAL
		if (total >= 0.0) accountTable.getCellFormatter().setStyleName(accountTable.getRowCount()-1, 1, "summaryTable-Positive-Odd");
		else accountTable.getCellFormatter().setStyleName(accountTable.getRowCount()-1, 1, "summaryTable-Negative-Odd");

		accountTable.setText(accountTable.getRowCount()-1, 1, MuflyUtils.numberFormater.format(total));

	}

	private int lookupAccount(String accountName) {
		boolean accountFound = false;
		int rows = accountTable.getRowCount();
		int i = 0;
		int rowAccount = -1;

		while (i<rows-1 && !accountFound) {
			if (accountTable.getText(i+1, 0).equals(accountName)) {
				rowAccount = i+1;
				accountFound = true;
			}
			i++;
		}
		return rowAccount;
	}

	private void initializeAccounts() {
		int rows = accountTable.getRowCount();
		
		if ((rows-1) % 2 == 0) tmpColourRow = 0;
		else tmpColourRow = 1;
		
		for (int i=1; i<rows; i++) {
			// Set the default value to 0.0 and the style to positive
			accountTable.setText(i, 1, MuflyUtils.numberFormater.format(0.0));

			if (i % 2 == tmpColourRow) {
				accountTable.getCellFormatter().setStyleName(i, 1, "summaryTable-Positive-Odd");
			} else {
				accountTable.getCellFormatter().setStyleName(i, 1, "positiveAmount");
			}
		}
	}
}

