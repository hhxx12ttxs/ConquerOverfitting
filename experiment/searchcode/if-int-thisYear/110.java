package net.sf.jmoney.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.jmoney.Constants;
import net.sf.jmoney.Currency;
import net.sf.jmoney.VerySimpleDateFormat;
import net.sf.jmoney.model.Account;
import net.sf.jmoney.model.Category;
import net.sf.jmoney.model.DoubleEntry;
import net.sf.jmoney.model.Entry;
import net.sf.jmoney.model.Session;
import net.sf.jmoney.model.SplittedEntry;
import dori.jasper.engine.JRDataSource;
import dori.jasper.engine.JasperFillManager;
import dori.jasper.engine.JasperPrint;
import dori.jasper.engine.data.JRBeanCollectionDataSource;
import dori.jasper.view.JRViewer;

public class IncomeExpenseReportPanel extends JPanel implements Constants {

	public static final int THIS_MONTH = 0;

	public static final int THIS_YEAR = 1;

	public static final int LAST_MONTH = 2;

	public static final int LAST_YEAR = 3;

	public static final int CUSTOM = 4;

	public static final String[] periods =
		{
			LANGUAGE.getString("Panel.Report.IncomeExpense.thisMonth"),
			LANGUAGE.getString("Panel.Report.IncomeExpense.thisYear"),
			LANGUAGE.getString("Panel.Report.IncomeExpense.lastMonth"),
			LANGUAGE.getString("Panel.Report.IncomeExpense.lastYear"),
			LANGUAGE.getString("Panel.Report.IncomeExpense.custom")};

	private JPanel reportPanel;
	private JPanel controlPanel = new JPanel();
	private JButton generateButton = new JButton();
	private JLabel periodLabel = new JLabel();
	private JComboBox periodBox = new JComboBox(periods);
	private JLabel fromLabel = new JLabel();
	private JTextField fromField = new JTextField();
	private JLabel toLabel = new JLabel();
	private JTextField toField = new JTextField();
	private JCheckBox subtotalsCheckBox = new JCheckBox();

	private Session session;
	private VerySimpleDateFormat dateFormat;
	private Date fromDate;
	private Date toDate;

	public IncomeExpenseReportPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setSession(Session aSession) {
		session = aSession;
	}

	public void setDateFormat(String pattern) {
		dateFormat = new VerySimpleDateFormat(pattern);
		updateFromAndTo();
	}

	private void generateReport() {
		if (reportPanel != null) {
			remove(reportPanel);
			updateUI();
		}
		try {
			String reportFile =
				subtotalsCheckBox.isSelected()
					? "resources/IncomeExpenseSubtotals.jasper"
					: "resources/IncomeExpense.jasper";
			URL url = Constants.class.getResource(reportFile);
			InputStream is = url.openStream();

			Map params = new HashMap();
			params.put(
				"Title",
				LANGUAGE.getString("Report.IncomeExpense.Title"));
			params.put(
				"Subtitle",
				dateFormat.format(fromDate)
					+ " - "
					+ dateFormat.format(toDate));
			params.put("Total", LANGUAGE.getString("Report.Total"));
			params.put("Category", LANGUAGE.getString("Entry.category"));
			params.put(
				"Income",
				LANGUAGE.getString("Report.IncomeExpense.Income"));
			params.put(
				"Expense",
				LANGUAGE.getString("Report.IncomeExpense.Expense"));
			params.put("DateToday", dateFormat.format(new Date()));
			params.put("Page", LANGUAGE.getString("Report.Page"));

			Collection items = getItems();
			if (items.isEmpty()) {
				JOptionPane.showMessageDialog(
					this,
					LANGUAGE.getString("Panel.Report.EmptyReport.Message"),
					LANGUAGE.getString("Panel.Report.EmptyReport.Title"),
					JOptionPane.ERROR_MESSAGE);
			} else {
				JRDataSource ds = new JRBeanCollectionDataSource(getItems());
				JasperPrint print =
					JasperFillManager.fillReport(is, params, ds);
				reportPanel = new JRViewer(print);
				add(reportPanel, BorderLayout.CENTER);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		updateUI();
	}

	private Collection getItems() {
		Vector allItems = new Vector();
		HashMap byCurrency = new HashMap();

		Iterator aIt = session.getAccounts().listIterator();
		while (aIt.hasNext()) {
			Account a = (Account) aIt.next();
			String cc = a.getCurrencyCode();
			HashMap items = (HashMap) byCurrency.get(cc);
			if (items == null) {
				items = new HashMap();
				byCurrency.put(cc, items);
			}
			addEntries(allItems, items, cc, a.getEntries());
		}

		Collections.sort(allItems);
		return allItems;
	}

	private void addEntries(
		Vector allItems,
		HashMap items,
		String currencyCode,
		Vector entries) {
		Iterator eIt = entries.listIterator();
		while (eIt.hasNext()) {
			Entry e = (Entry) eIt.next();
			if (!accept(e))
				continue;

			if (e instanceof SplittedEntry) {
				addEntries(
					allItems,
					items,
					currencyCode,
					((SplittedEntry) e).getEntries());
			} else {
				Category c = e.getCategory();
				Item i = (Item) items.get(e.getCategory());
				if (i == null) {
					i = new Item(e.getCategory(), currencyCode, e.getAmount());
					items.put(e.getCategory(), i);
					allItems.add(i);
				} else {
					i.addToSum(e.getAmount());
				}
			}
		}
	}

	private boolean accept(Entry e) {
		if (e instanceof DoubleEntry)
			return false;
		return acceptFrom(e.getDate()) && acceptTo(e.getDate());
	}

	private boolean acceptFrom(Date d) {
		if (fromDate == null)
			return false;
		if (d == null)
			return true;
		return (d.after(fromDate) || d.equals(fromDate));
	}

	private boolean acceptTo(Date d) {
		if (toDate == null)
			return true;
		if (d == null)
			return false;
		return (d.before(toDate) || d.equals(toDate));
	}

	private void updateFromAndTo() {
		int index = periodBox.getSelectedIndex();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		switch (index) {
			case THIS_MONTH :
				cal.set(Calendar.DAY_OF_MONTH, 1);
				fromDate = cal.getTime();

				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.MILLISECOND, -1);
				toDate = cal.getTime();
				break;
			case THIS_YEAR :
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.MONTH, Calendar.JANUARY);
				fromDate = cal.getTime();

				cal.add(Calendar.YEAR, 1);
				cal.add(Calendar.MILLISECOND, -1);
				toDate = cal.getTime();
				break;
			case LAST_MONTH :
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.add(Calendar.MONTH, -1);
				fromDate = cal.getTime();

				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.MILLISECOND, -1);
				toDate = cal.getTime();
				break;
			case LAST_YEAR :
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.MONTH, Calendar.JANUARY);
				cal.add(Calendar.YEAR, -1);
				fromDate = cal.getTime();

				cal.add(Calendar.YEAR, 1);
				cal.add(Calendar.MILLISECOND, -1);
				toDate = cal.getTime();
				break;
			case CUSTOM :
			default :
				}

		fromField.setText(dateFormat.format(fromDate));
		fromField.setEnabled(index == CUSTOM);
		toField.setText(dateFormat.format(toDate));
		toField.setEnabled(index == CUSTOM);
	}

	private void updateFrom() {
		fromDate = dateFormat.parse(fromField.getText());
		fromField.setText(dateFormat.format(fromDate));
	}

	private void updateTo() {
		toDate = dateFormat.parse(toField.getText());
		toField.setText(dateFormat.format(toDate));
	}

	private void jbInit() throws Exception {
		setLayout(new BorderLayout());
		periodLabel.setText(
			LANGUAGE.getString("Panel.Report.IncomeExpense.Period"));
		periodBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFromAndTo();
			}
		});
		fromLabel.setText(
			LANGUAGE.getString("Panel.Report.IncomeExpense.From"));
		fromField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFrom();
			}
		});
		fromField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				updateFrom();
			}
		});
		toLabel.setText(LANGUAGE.getString("Panel.Report.IncomeExpense.To"));
		toField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTo();
			}
		});
		fromField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				updateTo();
			}
		});
		generateButton.setText(LANGUAGE.getString("Panel.Report.Generate"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateReport();
			}
		});

		subtotalsCheckBox.setText(
			LANGUAGE.getString("Panel.Report.IncomeExpense.ShowSubtotals"));

		controlPanel.setBorder(BorderFactory.createEtchedBorder());
		controlPanel.setLayout(new GridBagLayout());
		controlPanel.add(
			periodLabel,
			new GridBagConstraints(
				0,
				0,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(6, 6, 0, 0),
				0,
				0));
		controlPanel.add(
			periodBox,
			new GridBagConstraints(
				1,
				0,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(6, 6, 0, 5),
				0,
				0));
		controlPanel.add(
			fromLabel,
			new GridBagConstraints(
				2,
				0,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(6, 6, 0, 0),
				0,
				0));
		controlPanel.add(
			fromField,
			new GridBagConstraints(
				3,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL,
				new Insets(6, 6, 0, 5),
				0,
				0));
		controlPanel.add(
			toLabel,
			new GridBagConstraints(
				4,
				0,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(6, 6, 0, 0),
				0,
				0));
		controlPanel.add(
			toField,
			new GridBagConstraints(
				5,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL,
				new Insets(6, 6, 0, 5),
				0,
				0));
		controlPanel.add(
			generateButton,
			new GridBagConstraints(
				6,
				0,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.NONE,
				new Insets(6, 12, 0, 4),
				0,
				0));
		controlPanel.add(
			subtotalsCheckBox,
			new GridBagConstraints(
				0,
				1,
				6,
				1,
				0.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 6, 0, 0),
				0,
				0));
		add(controlPanel, BorderLayout.SOUTH);
	}

	public class Item implements Comparable {

		private Category category;
		private String currencyCode;
		private long sum;

		public Item(Category aCategory, String aCurrencyCode, long aSum) {
			category = aCategory;
			currencyCode = aCurrencyCode;
			sum = aSum;
		}

		public String getCurrencyCode() {
			return currencyCode;
		}

		public String getBaseCategory() {
			if (category == null)
				return LANGUAGE.getString("Report.IncomeExpense.NoCategory");
			Object[] path = category.getCategoryNode().getUserObjectPath();
			return path.length > 1
				? path[1].toString()
				: category.getCategoryName();
		}

		public String getCategory() {
			return category == null
				? LANGUAGE.getString("Report.IncomeExpense.NoCategory")
				: category.getFullCategoryName();
		}

		public Long getIncome() {
			return sum >= 0 ? new Long(sum) : null;
		}

		public String getIncomeString() {
			return Currency.getCurrencyForCode(currencyCode).format(
				getIncome());
		}

		public Long getExpense() {
			return sum < 0 ? new Long(-sum) : null;
		}

		public String getExpenseString() {
			return Currency.getCurrencyForCode(currencyCode).format(
				getExpense());
		}

		public void addToSum(long amount) {
			sum += amount;
		}

		public boolean noCategory() {
			return category == null;
		}

		public int compareTo(Object o) {
			Item other = (Item) o;
			if (noCategory() && other.noCategory())
				return 0;
			else if (noCategory())
				return 1;
			else if (other.noCategory())
				return -1;
			else
				return getCategory().compareTo(other.getCategory());
		}
	}
}
