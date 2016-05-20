package jp.co.geo.logviewer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JFrame;

import jp.co.geo.logviewer.model.AccessLogFormat;
import jp.co.geo.logviewer.model.LogFormat;
import jp.co.geo.logviewer.model.LogItemType;
import jp.co.geo.logviewer.model.LogModel;
import jp.co.geo.logviewer.model.Logs;
import jp.co.geo.logviewer.table.TableSortListener;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;

public class AccessLogViewer {

	protected Shell shell;
	private Table table;
	
	/**
	 * 
	 */
	ResourceBundle rb = ResourceBundle.getBundle("jp.co.geo.logviewer.Location");
	
	/**
	 * HTTPステータスコードの表示メニュー
	 */
	private Menu mntmHttpStatusCode;
	
	/**
	 * HTTPステータスコードの一覧
	 * ファイルからログを読み込んだ際に重複なく一覧に追加する
	 */
	private ArrayList<String> httpStatusCodeList = new ArrayList<String>();
	
	/**
	 * ファイルから読み込んだログの全データを格納するリスト
	 */
	private Logs logList = new Logs();
	
	
	private DateTime fromDateTime;
	
	private DateTime afterDateTime;
	
	private Object setting;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AccessLogViewer window = new AccessLogViewer();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(610, 348);
		shell.setText("Apache Access Log");
		shell.setLayout(new GridLayout(7, false));
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("\u30D5\u30A1\u30A4\u30EB");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmOpen = new MenuItem(menu_1, SWT.NONE);

		mntmOpen.addSelectionListener(new SelectionAdapter() {
			
			@SuppressWarnings("unchecked")
			@Override
			/**
			 * ファイルを開く
			 */
			public void widgetSelected(SelectionEvent e) {
				// ファイルダイアログを開く
				OpenFileDialog openFileDialog = new OpenFileDialog(shell, SWT.CLOSE | SWT.APPLICATION_MODAL);
				ArrayList<StringBuffer> openFile = (ArrayList<StringBuffer>) openFileDialog.open();
				setData(openFile);
				return;
				
			}
		});
		mntmOpen.setText("\u958B\u304F");
		
		MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
		menuItem.setText("\u8868\u793A");
		
		Menu menu_3 = new Menu(menuItem);
		menuItem.setMenu(menu_3);
		
		MenuItem mntmHttp = new MenuItem(menu_3, SWT.CASCADE);
		mntmHttp.setText("HTTP\u30B9\u30C6\u30FC\u30BF\u30B9\u30B3\u30FC\u30C9");
		
		mntmHttpStatusCode = new Menu(mntmHttp);
		mntmHttp.setMenu(mntmHttpStatusCode);
		
		
		MenuItem menuItem_1 = new MenuItem(menu_3, SWT.NONE);
		menuItem_1.setText("\u30D8\u30C3\u30C0");
		
		MenuItem menuItem_3 = new MenuItem(menu_3, SWT.NONE);
		menuItem_3.setText("\u51E6\u7406\u6642\u9593");
		
		MenuItem mntmTool = new MenuItem(menu, SWT.CASCADE);
		mntmTool.setText("\u30C4\u30FC\u30EB");
		
		Menu menu_2 = new Menu(mntmTool);
		mntmTool.setMenu(menu_2);
		
		MenuItem mntmGraph = new MenuItem(menu_2, SWT.NONE);
		mntmGraph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphViewer graph = new GraphViewer(logList);
				graph.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			    graph.setBounds(10, 10, 500, 500);
				graph.plot();
			}
		});
		mntmGraph.setText("\u30B0\u30E9\u30D5");
		
		MenuItem menuItem_2 = new MenuItem(menu, SWT.NONE);
		menuItem_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SettingDialog settingDialog = new SettingDialog(shell, SWT.CLOSE | SWT.PRIMARY_MODAL);
				setting = settingDialog.open();
				setTableClumn();
			}
		});
		menuItem_2.setText(rb.getString("menubar.setting"));
		new Label(shell, SWT.NONE);
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setText(rb.getString("label.tofrom"));
		
		fromDateTime = new DateTime(shell, SWT.BORDER);
		
		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setText("\u3000\u3000\u7D42\u4E86\u65E5");
		
		afterDateTime = new DateTime(shell, SWT.BORDER);
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				redraw();
			}
		});
		btnNewButton.setText("\u518D\u63CF\u753B");
		
		Button btnRst = new Button(shell, SWT.NONE);
		btnRst.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnRst.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetTable();
			}
		});
		btnRst.setText("\u30EA\u30BB\u30C3\u30C8");
		
		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 7, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tblclmnAccessTime = new TableColumn(table, SWT.NONE);
		tblclmnAccessTime.addSelectionListener(new TableSortListener(table));
		tblclmnAccessTime.setWidth(72);
		tblclmnAccessTime.setText("\u6642\u523B");
		
		TableColumn tblclmnRequest = new TableColumn(table, SWT.NONE);
		tblclmnRequest.addSelectionListener(new TableSortListener(table));
		tblclmnRequest.setWidth(272);
		tblclmnRequest.setText("\u30EA\u30AF\u30A8\u30B9\u30C8URL");
		
		TableColumn tblclmnHttpStatusCode = new TableColumn(table, SWT.NONE);
		tblclmnHttpStatusCode.addSelectionListener(new TableSortListener(table));
		tblclmnHttpStatusCode.setWidth(87);
		tblclmnHttpStatusCode.setText("\u30B9\u30C6\u30FC\u30BF\u30B9\u30B3\u30FC\u30C9");
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.addSelectionListener(new TableSortListener(table));
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("\u30B5\u30A4\u30BA\uFF08Byte\uFF09");

	}
	
	/**
	 * ログデータから表を作成する
	 * @param dataList
	 */
	private void setData(ArrayList<StringBuffer> dataList){
		if (dataList == null) return;
		
		if (setting == null) return;
		AccessLogFormat format = (AccessLogFormat) setting;
		
		// 読み込んだファイルデータから表を作成する
		for (int i = 0; i < dataList.size(); i++) {
			LogModel log = new LogModel(format);
			Object[] data = log.analyze(dataList.get(i));
			logList.appendLog(log);
			TableItem item = new TableItem(table, SWT.NULL);
			// HTTPステータスコードを見て 50x や 40x ならその行を赤色にする
			String httpStatusCode = log.getHttpStatusCode();
			if (httpStatusCode.contains("50")
					|| httpStatusCode.contains("40")) {
				Color red = new Color(Display.getDefault(), 0xFF, 0x00, 0x00);
				item.setForeground(red);
			}
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			item.setText(0, df.format(log.getDate()));  // 日時
			item.setText(1, log.getURL());  // URL
			item.setText(2, log.getHttpStatusCode()); // HTTPステータスコード
			item.setText(3, log.getProcessingTime()); // レスポンスサイズ

			if (httpStatusCodeList.contains(log.getHttpStatusCode()) == false) {
				httpStatusCodeList.add(log.getHttpStatusCode());
			}
		}
		
		// HTTPステータスコードを昇順に並べる
		Collections.sort(httpStatusCodeList, new Comparator<String>(){
				public int compare(String str1, String str2){
					Integer val1 = new Integer(str1);
					Integer val2 = new Integer(str2);
					return val1.compareTo(val2);
				}
			});
		
		// HTTPステータスコードを表示メニューに追加する
		for (String httpStatusCode : httpStatusCodeList) {
			MenuItem mntmNewItem = new MenuItem(mntmHttpStatusCode, SWT.CHECK);
			mntmNewItem.setText(httpStatusCode);
			mntmNewItem.setSelection(true);
		}
		
		setDateTime();
	}
	
	/**
	 * 再描画を行う
	 */
	private void redraw() {
		table.removeAll();
		Iterator<LogModel> it = logList.iterator();
		while( it.hasNext() ) {
			LogModel log = it.next();
			String httpStatusCode = log.getHttpStatusCode();
			
			if (isDisplay(log)) {
				TableItem item = new TableItem(table, SWT.NULL);
				if (httpStatusCode.contains("50")
						|| httpStatusCode.contains("40")) {
					Color red = new Color(Display.getDefault(), 0xFF, 0x00, 0x00);
					item.setForeground(red);
				}
				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				item.setText(0, df.format(log.getDate()));  // 日時
				item.setText(1, log.getURL());
				item.setText(2, log.getHttpStatusCode());
				item.setText(3, log.getProcessingTime());
			}
		}
	}

	/**
	 * チェックボックスでチェックが行われているかを確認する
	 * @param items チェックボックス
	 * @param target 検索対象文字列
	 * @return
	 */
	private boolean selectedCheckBox(MenuItem[] items, String target) {
		for (MenuItem item : items) {
			if (item.getText().equals(target)) {
				return item.getSelection();
			}
		}
		
		return false;
	}
	
	private void setDateTime() {
		Calendar cal = Calendar.getInstance();
		Date date = logList.getMinDate();
		cal.setTime(date);
		fromDateTime.setYear(cal.get(Calendar.YEAR));
		fromDateTime.setMonth(cal.get(Calendar.MONTH));
		fromDateTime.setDay(cal.get(Calendar.DATE));
		
		date = logList.getMaxDate();
		cal.setTime(date);
		afterDateTime.setYear(cal.get(Calendar.YEAR));
		afterDateTime.setMonth(cal.get(Calendar.MONTH));
		afterDateTime.setDay(cal.get(Calendar.DATE));
	}
	
	/**
	 * ログが表示すべきものかどうか判定
	 * @return
	 */
	private boolean isDisplay(LogModel log) {
		//HTTPステータスコードで判定
		MenuItem menuItems[] = mntmHttpStatusCode.getItems();
		String httpStatusCode = log.getHttpStatusCode();
		if (selectedCheckBox(menuItems, httpStatusCode) == false) {
			return false;
		}
		
		//日付で判定
		Calendar from = Calendar.getInstance(Locale.JAPAN);
		Calendar after = Calendar.getInstance(Locale.JAPAN);
		Calendar logCal = Calendar.getInstance(Locale.JAPAN);
		int fromYear = fromDateTime.getYear();
		int fromMonth = fromDateTime.getMonth();
		int fromDay = fromDateTime.getDay();
		from.set(fromYear, fromMonth, fromDay);
		int afterYear = afterDateTime.getYear();
		int afterMonth = afterDateTime.getMonth();
		int afterDay = afterDateTime.getDay() + 1;
		after.set(afterYear, afterMonth, afterDay);
		
		Date date = log.getDate();
		logCal.setTime(date);
		
		if ( from.after(logCal) || after.before(logCal)){
			return false;
		}
		
		return true;
	}
	
	private void resetTable() {
		table.removeAll();
		logList = new Logs();
		
	}
	
	private void setTableClumn() {
		if (setting instanceof LogFormat) {
			ArrayList<LogItemType> types = ((LogFormat) setting).getTypes();
			TableColumn column[] = table.getColumns();
			for(int i = 0; i < types.size(); i++) {
				LogItemType type = types.get(i);
				if (column.length <= i || column[i] == null) {
					TableColumn tableColumn = new TableColumn(table, SWT.NONE);
					tableColumn.addSelectionListener(new TableSortListener(table));
					tableColumn.setWidth(72);
					tableColumn.setText(type.description());
				}else {
					column[i].setWidth(72);
					column[i].setText(type.description());
				}
			}
			table.redraw();
		}
	}
	
}

