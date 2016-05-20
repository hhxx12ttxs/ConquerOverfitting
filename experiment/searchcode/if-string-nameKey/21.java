package notesDocViewer;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;


public class Viewer extends Shell {
	
//	private static Viewer shell;
	private NotesFile noteFile;
	private Table table;
	private Combo comboCategories;
	private Combo comboNames;

	/**
	 * Launch the application.
	 * @param args
	 */
//	public static void main(String args[]) {
//		try {
//			Display display = Display.getDefault();
//			shell = new Viewer(display);
//			shell.initUI("C:\\all.log");
//			shell.open();
//			shell.layout();
//			while (!shell.isDisposed()) {
//				if (!display.readAndDispatch()) {
//					display.sleep();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public Viewer(Display display) {
		super(display, SWT.SHELL_TRIM);
		setLayout(new GridLayout(2, false));
		
		Label lblCategory = new Label(this, SWT.NONE);
		lblCategory.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCategory.setText("Category:");
		
		comboCategories = new Combo(this, SWT.READ_ONLY);
		comboCategories.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblName = new Label(this, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name:");
		
		comboNames = new Combo(this, SWT.READ_ONLY);
		comboNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		lblFilter = new Label(this, SWT.NONE);
		lblFilter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFilter.setText("Filter:");
		
		textFilter = new Text(this, SWT.BORDER);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		

		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Configuration Viewer");
		setSize(541, 357);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private ModifyListener categoryListener;
	private ModifyListener nameListener;
	private ModifyListener filterListener;
	private Label lblFilter;
	private Text textFilter;
	public void initUI(String filename) {

//		String filename = "C:\\all.log";
		noteFile = new NotesFile(filename);
		Enumeration<String> e = noteFile.getCategoryKeys();
		while(e.hasMoreElements()) {
			NotesCategory category = noteFile.getCategory(e.nextElement());
			comboCategories.add(category.getCategoryName());
		}
				
		categoryListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				comboNames.removeModifyListener(nameListener);
				comboNames.removeAll();
				
            	String key = comboCategories.getText();
            	NotesCategory category = noteFile.getCategory(key);
            	Enumeration<String> eNames= category.getKeys();
            	while(eNames.hasMoreElements()) {
            		String name = eNames.nextElement();
            		comboNames.add(name);
            	}

        		comboNames.addModifyListener(nameListener);
            	comboNames.select(0);
			}
		};
		
		nameListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				table.removeAll();
            	String categoryKey = comboCategories.getText();
            	String nameKey = comboNames.getText();
            	if (categoryKey.trim().isEmpty() || nameKey.trim().isEmpty())
            		return;
            	NotesCategory category = noteFile.getCategory(categoryKey);
            	ArrayList<NotesDocument> list = category.getDocuments(nameKey);
            	for(NotesDocument doc : list) {
            		Enumeration<String> eKeys = doc.getKeys();
            		while(eKeys.hasMoreElements()) {
            			NotesItem item = doc.getItem(eKeys.nextElement());

                        TableItem item1=new TableItem(table, SWT.NONE);
                        item1.setText(new String[]{item.name,item.type,item.value});
            		}
            	}
			}
		};

		filterListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				table.removeAll();
            	String categoryKey = comboCategories.getText();
            	String nameKey = comboNames.getText();
            	String filterKey = textFilter.getText();
            	if (categoryKey.trim().isEmpty() || nameKey.trim().isEmpty())
            		return;
            	NotesCategory category = noteFile.getCategory(categoryKey);
            	ArrayList<NotesDocument> list = category.getDocuments(nameKey);
            	for(NotesDocument doc : list) {
            		Enumeration<String> eKeys = doc.getKeys();
            		while(eKeys.hasMoreElements()) {
            			String key = eKeys.nextElement();
            			if (!key.toLowerCase().contains(filterKey.toLowerCase()))
            				continue;
            			NotesItem item = doc.getItem(key);
            			
                        TableItem item1=new TableItem(table, SWT.NONE);
                        item1.setText(new String[]{item.name,item.type,item.value});
            		}
            	}
			}
		};
		textFilter.addModifyListener(filterListener);
		comboCategories.addModifyListener(categoryListener);
        
        TableColumn tc1=new TableColumn(table,SWT.CENTER);
        TableColumn tc2=new TableColumn(table,SWT.CENTER);
        TableColumn tc3=new TableColumn(table,SWT.CENTER);
   
        tc1.setText("Name");
        tc2.setText("Type");
        tc3.setText("Value");
        tc1.setWidth(150);
        tc2.setWidth(150);
        tc3.setWidth(200);
        
        comboCategories.pack();
        comboNames.pack();
        comboCategories.select(0);

   }
}

