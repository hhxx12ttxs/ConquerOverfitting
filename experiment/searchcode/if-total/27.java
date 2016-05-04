package net.mufly.client.ui;

import net.mufly.client.MuflyConstants;
import net.mufly.client.core.ApplicationParameters;
import net.mufly.domain.TransactionType;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class SummaryPanel extends Composite {

	private HorizontalPanel summaryPanel;

	//private static final String hints[] = {"Income movements in account", "Income movements with transfers", "Expense movements in account", "Expense movements with transfers", "Total movements in account", "Total movements with transfers" };

	private double income;
	private double incomeTransfer;
	private double expense;
	private double expenseTransfer;
	private double total;
	private double totalTransfer;

	private Label lblIncome;
	private Label lblIncomeAmount;
	private Label lblIncomeAmountTransfer;
	private Label lblExpense;
	private Label lblExpenseAmount;
	private Label lblExpenseAmountTransfer;
	private Label lblTotal;
	private Label lblTotalAmount;
	private Label lblTotalAmountTransfer;

	private NumberFormat numberFormater;

	private MuflyConstants constants;

	public SummaryPanel() {
		init();
	}

	protected void init() {
		constants = ApplicationParameters.getInstance().getApplication().getConstants();

		numberFormater = NumberFormat.getFormat("#,#####0.00;-#,#####0.00");
		//numberFormater = NumberFormat.getCurrencyFormat("EUR");

		// Build the transactions summary panel
		summaryPanel = new HorizontalPanel();
		lblIncome = new Label(constants.Income());
		lblIncomeAmount = new Label("0.0");
		lblIncomeAmountTransfer = new Label();
		lblExpense = new Label(constants.Expense());
		lblExpenseAmount = new Label("0.0");
		lblExpenseAmountTransfer = new Label();
		lblTotal = new Label(constants.Total());
		lblTotalAmount = new Label("0.0");
		lblTotalAmountTransfer = new Label();

		lblIncomeAmount.setStyleName("positiveAmount");
		lblIncomeAmountTransfer.setStyleName("positiveAmount");
		lblExpenseAmount.setStyleName("negativeAmount");
		lblExpenseAmountTransfer.setStyleName("negativeAmount");

		summaryPanel.setSpacing(5);
		summaryPanel.add(lblIncome);
		summaryPanel.add(lblIncomeAmount);
		summaryPanel.add(lblIncomeAmountTransfer);
		summaryPanel.add(lblExpense);
		summaryPanel.add(lblExpenseAmount);
		summaryPanel.add(lblExpenseAmountTransfer);
		summaryPanel.add(lblTotal);
		summaryPanel.add(lblTotalAmount);
		summaryPanel.add(lblTotalAmountTransfer);

		lblIncomeAmount.setTitle(constants.hintIncm1());
		lblIncomeAmountTransfer.setTitle(constants.hintIncm2());
		lblExpenseAmount.setTitle(constants.hintExp1());
		lblExpenseAmountTransfer.setTitle(constants.hintExp2());
		lblTotalAmount.setTitle(constants.hintTotal1());
		lblTotalAmountTransfer.setTitle(constants.hintTotal2());

		initWidget(summaryPanel);
	}

	public void updateSummaryLine(Double amount, TransactionType transType) {
		// If the transaction type is Transfer, update the "normal" fields
		if (!transType.equals(TransactionType.Transfer)) {
			// Update the summary line (income, expense, total)
			if (amount >= 0.0) {
				income += amount;
				lblIncomeAmount.setText(numberFormater.format(income));
			} else {
				expense += amount;
				lblExpenseAmount.setText(numberFormater.format(expense));
			}
			total += amount;
			lblTotalAmount.setText(numberFormater.format(total));

			// Apply the style
			if (total >= 0.0) lblTotalAmount.setStyleName("positiveAmount");
			else lblTotalAmount.setStyleName("negativeAmount");
		}

		// We always update the "transfer" fields (income, expense and transfer)
		if (amount >= 0.0) {
			incomeTransfer += amount;
			lblIncomeAmountTransfer.setText("(" + numberFormater.format(incomeTransfer) + ")");
		} else {
			expenseTransfer += amount;
			lblExpenseAmountTransfer.setText("(" + numberFormater.format(expenseTransfer) + ")");
		}
		totalTransfer += amount;
		lblTotalAmountTransfer.setText("(" + numberFormater.format(totalTransfer) + ")");

		// Apply the style
		if (totalTransfer >= 0.0) lblTotalAmountTransfer.setStyleName("positiveAmount");
		else lblTotalAmountTransfer.setStyleName("negativeAmount");
	}

	public void clearSummaryLine() {
		lblIncomeAmount.setText("0.0");
		lblExpenseAmount.setText("0.0");
		lblTotalAmount.setText("0.0");
		lblIncomeAmountTransfer.setText("");
		lblExpenseAmountTransfer.setText("");
		lblTotalAmountTransfer.setText("");

		income = 0.0;
		incomeTransfer = 0.0;
		expense = 0.0;
		expenseTransfer = 0.0;
		total = 0.0;
		totalTransfer = 0.0;
	}
}

