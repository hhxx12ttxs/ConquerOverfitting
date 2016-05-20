package gnukhata.views;

import gnukhata.globals;
import gnukhata.controllers.StartupController;
import gnukhata.controllers.accountController;
import gnukhata.controllers.reportmodels.ProjectList;
import gnukhata.controllers.reportmodels.accountReport;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddMoreProjects extends Composite
{
	Color Background;
	Color Foreground;
	Color FocusBackground;
	Color FocusForeground;
	Color BtnFocusForeground;
	
	static Display display;
	String strOrgName;
	String strFromYear;
	String strToYear;
	String strOrgType;
	
	int counter=0;
	
	//Button chkbtnProjAcc;
	Label lblInfo;
	//Button btnAddNewProj;
	
//	Button chkbtnManualAccCode;
	Button btnSave;
	Button btnCreateAcc;
	Button btnQuit;
	int startFrom = 0;
	
	Label lblLogo;
	Label lblOrgDetails;
	Label lblLine;
	Label lblheadline;
	Label lblpreference;
	Label lblprojectlist;
	
	TableViewer tblProjectList;
	
	Label lblprjname;
	Text txtprjname;
	Label lblamount;
	Text txtamount;
	Button btnRemove;
	Label lblRemovebtn;
	Text newtxtprjname;
	Text newtxtamount;
	Button newbtnRemove;
	Vector<Text> projectNames = new Vector<Text>();
	Vector<Text> amounts = new Vector<Text>();
	Vector<Button> Removebtn = new Vector<Button>();
	
	//Vector<Object> deployParams;
	Group grpPreferences;
	NumberFormat nf;
	
	int shellwidth = 0;
	int finshellwidth;

	public AddMoreProjects(Composite parent, int style, ArrayList<ProjectList> prjdata) 
	{
		super(parent, style);
		// TODO Auto-generated constructor stub
		strOrgName = globals.session[1].toString();
		strFromYear =  globals.session[2].toString();
		strToYear =  globals.session[3].toString();
		
		MainShell.lblLogo.setVisible(false);
		MainShell.lblLine.setVisible(false);
		MainShell.lblOrgDetails.setVisible(false);
		
		nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		FormLayout formlayout = new FormLayout();
		this.setLayout(formlayout);	
		FormData layout =new FormData();
		
		lblLogo = new Label(this, SWT.None);
		layout = new FormData();
		layout.top = new FormAttachment(1);
		layout.left = new FormAttachment(60);
		//layout.right = new FormAttachment(95);
		//layout.bottom = new FormAttachment(18);
		lblLogo.setLayoutData(layout);
		//Image img = new Image(display,"finallogo1.png");
		lblLogo.setImage(globals.logo);
		
		lblOrgDetails = new Label(this,SWT.NONE);
		lblOrgDetails.setFont( new Font(display,"Times New Roman", 11, SWT.BOLD ) );
		lblOrgDetails.setText(globals.session[1]+"\n"+"For Financial Year "+"From "+globals.session[2]+" To "+globals.session[3] );
		layout = new FormData();
		layout.top = new FormAttachment(1);
		layout.left = new FormAttachment(2);
		//layout.right = new FormAttachment(53);
		//layout.bottom = new FormAttachment(18);
		lblOrgDetails.setLayoutData(layout);

		
		lblLine = new Label(this,SWT.NONE);
		lblLine.setText("-------------------------------------------------------------------------------------------------------");
		lblLine.setFont(new Font(display, "Times New Roman", 26, SWT.ITALIC));
		layout = new FormData();
		layout.top = new FormAttachment(lblLogo,1);
		layout.left = new FormAttachment(2);
		layout.right = new FormAttachment(98);
		layout.bottom = new FormAttachment(14);
		lblLine.setLayoutData(layout);
		
		lblpreference=new Label(this, SWT.NONE);
		lblpreference.setText("Add More Projects");
		lblpreference.setFont(new Font(display, "Times New Roman", 14, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(65);
		layout.left = new FormAttachment(10);
		lblpreference.setLayoutData(layout);
		
		lblprojectlist=new Label(this, SWT.NONE);
		lblprojectlist.setText("Project List");
		lblprojectlist.setFont(new Font(display, "Times New Roman", 14, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,1);
		layout.left = new FormAttachment(42);
		lblprojectlist.setLayoutData(layout);
		
	
		
		tblProjectList=new TableViewer(this, SWT.MULTI|SWT.BORDER|SWT.FULL_SELECTION|SWT.LINE_SOLID);
		tblProjectList.getTable().setLinesVisible (true);
		tblProjectList.getTable().setHeaderVisible (true);
		layout = new FormData();
		layout.top = new FormAttachment(lblprojectlist, 10);
		layout.left = new FormAttachment(10);
		layout.right = new FormAttachment(90);
		layout.bottom = new FormAttachment(60);
		tblProjectList.getTable().setLayoutData(layout);
		
		
		grpPreferences = new Group(this, SWT.BORDER);
		FormData grp = new FormData();
		grp.top = new FormAttachment(lblpreference, 5);
		grp.left = new FormAttachment(10);
		grp.right = new FormAttachment(45);
		grp.bottom = new FormAttachment(85);
		grpPreferences.setLayoutData(grp);
		grpPreferences.setLayout(new FormLayout());
		
		/*GridData gd = new GridData();
		GridLayout gl = new GridLayout();		
		gl.numColumns = 3;
		grpPreferences.setLayout(gl);*/
		

		lblprjname = new Label(grpPreferences, SWT.BORDER );
		lblprjname.setText("Project Name");
		layout = new FormData();
		layout.top = new FormAttachment(10);
		layout.left = new FormAttachment(5);
		
		lblprjname.setLayoutData(layout);
		

		txtprjname = new Text(grpPreferences, SWT.BORDER);
		layout = new FormData();
		layout.top = new FormAttachment(lblprjname, 10);
		layout.left = new FormAttachment(5);
		layout.right = new FormAttachment(35);
	    /*gd = new GridData();
	    gd.widthHint=280;*/
		txtprjname.setLayoutData(layout);
		
		/*gd=new GridData();
		gd.widthHint=180;
		lblprjname.setLayoutData(gd);*/	
		
		lblamount = new Label(grpPreferences, SWT.BORDER );
		lblamount.setText("Sanctioned Amount");
		layout = new FormData();
		layout.top = new FormAttachment(10);
		layout.left = new FormAttachment(lblprjname, 73);
	    /*gd = new GridData();
	    gd.widthHint=170;*/
		lblamount.setLayoutData(layout);
		

		txtamount = new Text(grpPreferences, SWT.BORDER| SWT.RIGHT);
		layout = new FormData();
		layout.top = new FormAttachment(lblamount, 10);
		layout.left = new FormAttachment(txtprjname, 38);
		layout.right = new FormAttachment(70);
	    /*gd = new GridData();
	    gd.widthHint=170;*/
	    
	   // txtamount.setText("0.00");
		txtamount.setLayoutData(layout);
			
		
		lblRemovebtn = new Label(grpPreferences, SWT.BORDER);
		//lblRemovebtn.setText("\t\t Remove \t\t");
		/*gd=new GridData();
		gd.widthHint=50;*/
		layout = new FormData();
		layout.top = new FormAttachment(10);
		layout.left = new FormAttachment(lblamount, 10);
		lblRemovebtn.setLayoutData(layout);
		lblRemovebtn.setVisible(false);
		
		
		
		btnRemove = new Button(grpPreferences, SWT.BORDER);
		btnRemove.setText(" R&emove ");
		btnRemove.setVisible(false);
		layout = new FormData();
		layout.top = new FormAttachment(lblRemovebtn, 10);
		layout.left = new FormAttachment(txtamount, 10);
	    /*gd = new GridData();
	    gd.widthHint=120;*/
		btnRemove.setLayoutData(layout);
		
		projectNames.add(txtprjname);
		amounts.add(txtamount);
		Removebtn.add(btnRemove);
		grpPreferences.pack();
		
		
		
		
		/*chkbtnManualAccCode= new Button(this, SWT.CHECK);
		chkbtnManualAccCode.setText("Manual Account Codes");
		chkbtnManualAccCode.setFont(new Font(display,"Times New Romen",14,SWT.NONE));
		layout = new FormData();
		layout.top = new FormAttachment(btnAddNewProj,16);
		layout.left = new FormAttachment(34);
		layout.right = new FormAttachment(56);
		layout.bottom = new FormAttachment(84);
		chkbtnManualAccCode.setLayoutData(layout);
		//chkbtnManualAccCode.setVisible(false);
*/
		/*btnAddNewProj = new Button(this,SWT.PUSH);
		btnAddNewProj.setText("Add &New Project");
		btnAddNewProj.setFont(new Font(display, "Times New Roman", 14, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(grpPreferences, 20);
		layout.left = new FormAttachment(15);
		//layout.right = new FormAttachment(40);
		//layout.bot tom = new FormAttachment(65);
		btnAddNewProj.setLayoutData(layout);
		btnAddNewProj.setEnabled(false);*/
		
		btnSave = new Button(this,SWT.PUSH);
		btnSave.setText("&Save");
		btnSave.setFont(new Font(display, "Times New Roman", 14, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(grpPreferences, 20);
		layout.left = new FormAttachment(29);
		/*layout.right = new FormAttachment(48);
		layout.bottom = new FormAttachment(91);*/
		btnSave.setLayoutData(layout);
		
		this.makeaccessible(grpPreferences);
		
		this.getAccessible();
		this.setBounds(this.getDisplay().getPrimaryMonitor().getBounds());
		shellwidth = this.getClientArea().width;
		finshellwidth = shellwidth-(shellwidth*2/100);
		this.setEvents();
		this.setReport(prjdata);
		Background =  new Color(this.getDisplay() ,220 , 224, 227);
		Foreground = new Color(this.getDisplay() ,0, 0,0 );
		FocusBackground  = new Color(this.getDisplay(),78,97,114 );
		FocusForeground = new Color(this.getDisplay(),255,255,255);
		BtnFocusForeground=new Color(this.getDisplay(), 0, 0, 255);

		globals.setThemeColor(this, Background, Foreground);
		globals.SetButtonColoredFocusEvents(this, FocusBackground, BtnFocusForeground, Background, Foreground);
		globals.SetComboColoredFocusEvents(this, FocusBackground, FocusForeground, Background, Foreground);
		globals.SetTableColoredFocusEvents(this, FocusBackground, FocusForeground, Background, Foreground); 
		globals.SetTextColoredFocusEvents(this, FocusBackground, FocusForeground, Background, Foreground);

		
		this.pack();
	}
	
	public void makeaccessible(Control c)
	{
		/*
		 * getAccessible() method is the method of class Controlwhich is the
		 * parent class of all the UI components of SWT including Shell.so when
		 * the shell is made accessible all the controls which are contained by
		 * that shell are made accessible automatically.
		 */
		c.getAccessible();
	}
	
	private void setEvents() 
	{
			
		// TODO Auto-generated method stub 
		Removebtn.get(0).setVisible(false);	
		grpPreferences.setFocus();
		projectNames.get(0).setFocus();
		this.grpPreferences.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				//super.focusGained(arg0);
				txtprjname.setFocus();
			}
		});
	
		/*this.btnAddNewProj.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				GridData gd = new GridData();
				
				newtxtprjname = new Text(grpPreferences, SWT.BORDER);
				gd=new GridData();
				gd.widthHint=180;
				newtxtprjname.setLayoutData(gd);
				
				newtxtamount = new Text(grpPreferences, SWT.BORDER | SWT.RIGHT);
				gd=new GridData();
				gd.widthHint=170;
				newtxtamount.setLayoutData(gd);
				
				newbtnRemove = new Button(grpPreferences, SWT.BORDER);
				newbtnRemove.setText(" Remove ");
				
				
				gd=new GridData();
				gd.widthHint=120;
				newbtnRemove.setLayoutData(gd);
				
				projectNames.add(newtxtprjname);
				amounts.add(newtxtamount);
				Removebtn.add(newbtnRemove);
				
				projectNames.get(projectNames.size()-1).setFocus();
				for (int i = 0; i < Removebtn.size(); i++) {
					Removebtn.get(i).setVisible(true);
				}
				
				setDynamicRowEvents();
				grpPreferences.pack();
			}
		});*/
		setDynamicRowEvents();
		
		this.btnSave.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				for(int prjnmvalidation = 0;  prjnmvalidation < projectNames.size(); prjnmvalidation ++)
				{
					if( projectNames.get(prjnmvalidation).getText().trim().equals(""))
					{
						MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK | SWT.ICON_ERROR);
						msgaccerr .setMessage("Please Enter Project Name");
						msgaccerr.setText("Error!");
						msgaccerr.open();
						projectNames.get(prjnmvalidation).setFocus();
						return;	
					}
				}				
				
			
				String accountCodeFlag = "";
				boolean result = true;
				
				HashMap<String , String> projectAmount = new HashMap<String, String>();
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2);
				nf.setGroupingUsed(false);

				for (int i = 0; i < projectNames.size(); i++ )
				{
					for(int j = 0; j < tblProjectList.getTable().getItemCount(); j++)
					{
						ProjectList P = (ProjectList)tblProjectList.getElementAt(j);
						if(projectNames.get(i).getText().toString().equalsIgnoreCase(P.getProjectName()))
						{
							MessageBox	 msg = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msg.setMessage("The Project Name you entered already exists");
							msg.setText("Error!");
							
							
							msg.open();
							projectNames.get(0).setFocus();
							projectNames.get(0).setText("");
							return;
						}
					}
					
					
					
					if(amounts.get(i).getText().toString().equals(""))
					{
						MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK | SWT.ICON_ERROR);
						msgaccerr .setMessage("Please Enter an Amount");
						msgaccerr.setText("Error!");
						msgaccerr.open();
						amounts.get(i).setFocus();
						return;
					}
					else
					{
						try {
							projectAmount.put(projectNames.get(i).getText() ,nf.format(Double.valueOf(amounts.get(i).getText())));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							MessageBox	 msg = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msg.setMessage("Please enter proper amount");
							msg.setText("Error!");
							msg.open();
							e.printStackTrace();
						}
					}
					
				}
				
				
				
				result = StartupController.setProjects(projectAmount);
			
				if ( result)
				{
					boolean prefs = StartupController.setPreferences(accountCodeFlag);
					/*MessageBox msg = new MessageBox(new Shell(),SWT.OK);
					msg.setMessage("Data is saved successfully.");
					msg.open();*/
					//btnSave.setEnabled(false);
					Composite grandparent=(Composite) btnSave.getParent().getParent();
					btnSave.getParent().dispose();
					/*AddMoreProjects amp=new AddMoreProjects(grandparent, SWT.None,null);
					amp.setSize(grandparent.getClientArea().width, grandparent.getClientArea().height);*/
					gnukhata.controllers.StartupController.getProjectList(grandparent);
					
					
				}
				else
				{
					MessageBox msg1 = new MessageBox(new Shell(),SWT.OK | SWT.ICON_ERROR);
					msg1.setMessage("Could not save the project, there is an error");
					msg1.setText("Error!");
					msg1.open();
				}

			}

			private MessageBox MessageBox(Shell shell, int ok) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		
		/*btnAddNewProj.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.ARROW_UP)
				{
					if(Removebtn.get(Removebtn.size()-1).isVisible())
					{
						Removebtn.get(Removebtn.size()-1).setFocus();
					}
					else
					{
						amounts.get(amounts.size()-1).setFocus();
					}
				}
				if(arg0.keyCode==SWT.ARROW_RIGHT)
				{
					btnSave.setFocus();
				}
				if(arg0.keyCode==SWT.KEYPAD_CR)
				{
					btnSave.setVisible(true);
				}
			}			
		});*/
		
		btnSave.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.ARROW_LEFT)
				{
					amounts.get(0).setFocus();
				}
			}			
		});

		
	}
	
	private void setReport(ArrayList<ProjectList> prjdata)
	{
		TableViewerColumn colSrNo = new TableViewerColumn(tblProjectList,SWT.None);
		colSrNo.getColumn().setText("  Sr.No.");
		colSrNo.getColumn().setAlignment(SWT.LEFT);
		colSrNo.getColumn().setWidth(5 * finshellwidth /100);
		colSrNo.setLabelProvider(new ColumnLabelProvider()
		{
		@Override
		public String getText(Object element) {
			// TODO Auto-generated method stub
		gnukhata.controllers.reportmodels.ProjectList srNo = (ProjectList) element;
		return srNo.getSrNo();
		}	
		});
		
		TableViewerColumn colprjname = new TableViewerColumn(tblProjectList,SWT.None);
		colprjname.getColumn().setText("                          Project Name");
		colprjname.getColumn().setAlignment(SWT.LEFT);
		colprjname.getColumn().setWidth(60 * finshellwidth /100);
		colprjname.setLabelProvider(new ColumnLabelProvider()
		{
		@Override
		public String getText(Object element) {
			// TODO Auto-generated method stub
		gnukhata.controllers.reportmodels.ProjectList prjnm = (ProjectList) element;
		return prjnm.getProjectName();
		}	
		});
		
		TableViewerColumn colprjamount = new TableViewerColumn(tblProjectList, SWT.None);
		colprjamount.getColumn().setText("Sanctioned Amount         ");
		colprjamount.getColumn().setAlignment(SWT.RIGHT);
		colprjamount.getColumn().setWidth(10 * finshellwidth /100);
		colprjamount.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element) {
				// TODO Auto-generated method stub
				gnukhata.controllers.reportmodels.ProjectList prjamnt = (ProjectList) element;
				return prjamnt.getProjectAmount();
				//return super.getText(element);
			}
		}
		);
		
		tblProjectList.setContentProvider(new ArrayContentProvider());
		tblProjectList.setInput(prjdata);
		tblProjectList.getTable().pack();
		
		/*AccountList.setContentProvider(new ArrayContentProvider());
		AccountList.setInput(accdata);
		AccountList.getTable().pack();
		AccountList.getTable().setFocus();*/
	}
	
	
	
	private void setDynamicRowEvents() {
		
		for(int rowcounter=startFrom; rowcounter<projectNames.size();rowcounter++)
		{
			projectNames.get(rowcounter).setData("curindex", rowcounter);
			amounts.get(rowcounter).setData("curindex",rowcounter);
			amounts.get(rowcounter).setText("0.00");
			Removebtn.get(rowcounter).setData("curindex",rowcounter);
			//Removebtn.get(0).setVisible(false);
			
			projectNames.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					Text currentPrjnm=(Text) arg0.widget;
					final int rowindex=(Integer) currentPrjnm.getData("curindex");
					if(arg0.keyCode==SWT.CR || arg0.keyCode==SWT.KEYPAD_CR)
					{	
						//projectNames.get(rowindex).notifyListeners(SWT.FocusOut, new Event());
						if(projectNames.get(rowindex).getText().equals(""))
						{
							MessageBox msg=new MessageBox(new Shell(),SWT.ERROR|SWT.OK | SWT.ICON_ERROR);				
							msg.setMessage("Please Enter Project Name");
							msg.setText("Error!");
							msg.open();
							projectNames.get(rowindex).setFocus();
						}
						else
						{
							amounts.get(rowindex).setFocus();
						}
					}
					
					if(arg0.keyCode==SWT.ARROW_DOWN && rowindex>=0)
					{
						if(projectNames.get(rowindex).getText().equals(""))
						{
							MessageBox msg=new MessageBox(new Shell(),SWT.ERROR|SWT.OK | SWT.ICON_ERROR);				
							msg.setMessage("Please Enter Project Name");
							msg.setText("Error!");
							msg.open();
							projectNames.get(rowindex).setFocus();
						}
						else
						{
							amounts.get(rowindex).setFocus();
						}
						//projectNames.get(rowindex).setFocus();
					}
					
					if(arg0.keyCode==SWT.ARROW_UP && rowindex==0)
					{
						//chkbtnProjAcc.setFocus();
					}
					if(arg0.keyCode==SWT.ARROW_UP && rowindex==1)
					{
						amounts.get(rowindex-1).setFocus();
					}
					
					if(arg0.keyCode==SWT.ARROW_UP && rowindex>0)
					{
						Removebtn.get(rowindex -1).setFocus();
						
					}
						
				
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='p') && rowindex > 0)
					{
						projectNames.get(rowindex-1).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='n')&& (rowindex < projectNames.size()-1))
					{
						projectNames.get(rowindex+1).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='f'))
					{
						projectNames.get(0).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='l'))
					{
						projectNames.get(projectNames.size()-1).setFocus();
					}
					if(((arg0.stateMask & SWT.SHIFT)==SWT.SHIFT)&&(arg0.keyCode=='.'))
					{
						amounts.get(rowindex).setFocus();
						arg0.doit=false;
						return;
					}
					
					if(arg0.keyCode==SWT.DEL)
					{
						Removebtn.get(rowindex).notifyListeners(SWT.Selection, new Event());
						
					}
				}			
			});
			
			amounts.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);					
					Text currentAmount=(Text) arg0.widget;
					int rowindex=(Integer) currentAmount.getData("curindex");
					
					if(arg0.keyCode==SWT.CR || arg0.keyCode==SWT.KEYPAD_CR)
					{		
						btnSave.setFocus();
					}
					if(arg0.keyCode==SWT.ARROW_DOWN && rowindex ==0 && (rowindex<amounts.size()-1))
					{
						projectNames.get(rowindex+1).setFocus();
					}
					if(arg0.keyCode==SWT.ARROW_DOWN && rowindex>0 )
					{
						Removebtn.get(rowindex).setFocus();
					}
					
					if(arg0.keyCode==SWT.ARROW_UP)
					{
						projectNames.get(rowindex).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='p')&& rowindex > 0)
					{
						amounts.get(rowindex-1).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='n') && (rowindex<amounts.size()-1))
					{
						amounts.get(rowindex+1).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='f'))
					{
						amounts.get(0).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='l'))
					{
						amounts.get(projectNames.size()-1).setFocus();
					}
					if(((arg0.stateMask & SWT.SHIFT)==SWT.SHIFT)&&(arg0.keyCode=='.'))
					{
						Removebtn.get(rowindex).setFocus();
						arg0.doit=false;
						return;
					}
					if(((arg0.stateMask & SWT.SHIFT)==SWT.SHIFT)&&(arg0.keyCode==','))
					{
						projectNames.get(rowindex).setFocus();
						arg0.doit=false;
						return;
					}
					if(arg0.keyCode==SWT.DEL)
					{
						Removebtn.get(rowindex).notifyListeners(SWT.Selection, new Event());
					}

				}			
			});
		
			projectNames.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) 
				{
					
					Text currentProjectName=(Text) arg0.widget;
					currentProjectName.setBackground(Background);
					currentProjectName.setForeground(Foreground);
					final int rowindex=(Integer) currentProjectName.getData("curindex");
					
					if(!projectNames.get(rowindex).getText().trim().equals(""))
					{
						//projectNames.get(rowindex).setText(Character.toUpperCase(projectNames.get(rowindex).getText().charAt(0)) + projectNames.get(rowindex).getText().substring(1));
						String result = accountController.duplicateProject(projectNames.get(rowindex).getText());
						if(result.equals("1"))
						{
							MessageBox	 msg = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msg.setMessage("The Project Name you entered already exists");
							msg.setText("Error!");
							msg.open();
				
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									projectNames.get(rowindex).setText("");
									projectNames.get(rowindex).setFocus();
									
								}
							});			
						}		
					}
					if(!projectNames.get(rowindex).getText().trim().equals(""))
					{
						String prjname= projectNames.get(rowindex).getText();
						String strtitlecaseproj = toTitleCase(prjname);
						projectNames.get(rowindex).setText(strtitlecaseproj);
					}
					
				}
				@Override
				public void focusGained(FocusEvent arg0) {
					Text currentProjectName=(Text) arg0.widget;
					currentProjectName.setBackground(FocusBackground);
					currentProjectName.setForeground(FocusForeground);
					
					// TODO Auto-generated method stub
				//	super.focusGained(arg0);
				}
			});
			
			amounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					
					Text currentAmount=(Text) arg0.widget;
					currentAmount.setBackground(Background);
					currentAmount.setForeground(Foreground);
					final int rowindex=(Integer) currentAmount.getData("curindex");
					/*if(amounts.get(rowindex).getText().trim().equals("")||Double.parseDouble(amounts.get(rowindex).getText())==0)
					{
						MessageBox msg=new MessageBox(new Shell(),SWT.ERROR|SWT.OK);
						msg.setMessage("Please Enter Amount");
						msg.open();
						display.getCurrent().asyncExec(new Runnable() {							
							@Override
							public void run() {
								// TODO Auto-generated method stub
							amounts.get(rowindex).setFocus();
							amounts.get(rowindex).setSelection(0,amounts.get(rowindex).getText().length());
							}
						});
						return;
					}*/
					try {
						amounts.get(rowindex).setText(nf.format(Double.parseDouble(amounts.get(rowindex).getText())));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						amounts.get(rowindex).setText("");
					}
					
				}
				@Override
				public void focusGained(FocusEvent arg0) {
					Text currentAmount=(Text) arg0.widget;
					currentAmount.setBackground(FocusBackground);
					currentAmount.setForeground(FocusForeground);
					
					// TODO Auto-generated method stub
				//	super.focusGained(arg0);
				}
			});
			
				amounts.get(rowcounter).addVerifyListener(new VerifyListener() {
				
				@Override
				public void verifyText(VerifyEvent arg0) {
					// TODO Auto-generated method stub
					switch (arg0.keyCode) {
		            case SWT.BS:           // Backspace
		            case SWT.DEL:          // Delete
		            case SWT.HOME:         // Home
		            case SWT.END:          // End
		            case SWT.ARROW_DOWN:
		            case SWT.ARROW_UP:
		            case SWT.ARROW_LEFT:   // Left arrow
		            case SWT.ARROW_RIGHT:  // Right arrow
		            case SWT.TAB:
		            case SWT.CR:
		            case SWT.KEYPAD_CR:
		                return;
					}
			if(arg0.keyCode == 46)
			{
				return;
			}
		        if (!Character.isDigit(arg0.character)) {
		            arg0.doit = false;  // disallow the action
		        }
		        
				}
			});
			
			Removebtn.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					Button btncurrentremove = (Button) arg0.widget;
					int rowindex =(Integer)  btncurrentremove.getData("curindex");
					if(arg0.keyCode==SWT.ARROW_DOWN && (rowindex < Removebtn.size()-1))
					{
						projectNames.get(rowindex+1).setFocus();											
					}
					if(arg0.keyCode==SWT.ARROW_UP)
					{
						amounts.get(rowindex).setFocus();											
					}
											
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='p')&& rowindex > 0)
					{
						Removebtn.get(rowindex-1).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='n')&& (rowindex < Removebtn.size()-1))
					{
						Removebtn.get(rowindex+1).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='f'))
					{
						Removebtn.get(0).setFocus();
					}
					if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='l'))
					{
						Removebtn.get(projectNames.size()-1).setFocus();
					}
					/*if(((arg0.stateMask & SWT.CTRL)==SWT.CTRL)&&(arg0.keyCode=='>'))
					{
						amounts.get(rowindex).setFocus();
					}*/
					if(((arg0.stateMask & SWT.SHIFT)==SWT.SHIFT)&&(arg0.keyCode==','))
					{
						amounts.get(rowindex).setFocus();
						arg0.doit=false;
						return;
					}
					if(arg0.keyCode==SWT.DEL)
					{
						Removebtn.get(rowindex).notifyListeners(SWT.Selection, new Event());
						
					}
					
				}			
			});
			Removebtn.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0) {
					Button btncurrentremove = (Button) arg0.widget;
					btncurrentremove.setBackground(FocusBackground);
					btncurrentremove.setForeground(BtnFocusForeground);
					// TODO Auto-generated method stub
				//	super.focusGained(arg0);
				}
				@Override
				public void focusLost(FocusEvent arg0) {
					Button btncurrentremove = (Button) arg0.widget;
					btncurrentremove.setBackground(Background);
					btncurrentremove.setForeground(Foreground);
					
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
				}
			});
			
				Removebtn.get(rowcounter).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					//super.widgetSelected(arg0);
					if(Removebtn.size()<=1)
					{
						return;
					}
					Button btncurrentremove = (Button) arg0.widget;
					int rowindex =(Integer)  btncurrentremove.getData("curindex");
					if(rowindex==0 )
					{						
						btnSave.setFocus();
											
					}
					if(rowindex > 0 )
					{
						projectNames.get(rowindex -1).setFocus();
					}					
					projectNames.get(rowindex).dispose();
					projectNames.remove(rowindex);
					amounts.get(rowindex).dispose();
					amounts.remove(rowindex);
					Removebtn.get(rowindex).dispose();
					Removebtn.remove(rowindex);
					
					
					for(int reset =rowindex; reset < projectNames.size(); reset ++ )
					{
						projectNames.get(reset).setData("curindex", reset );
						amounts.get(reset).setData("curindex", reset );
						Removebtn.get(reset).setData("curindex", reset );					
						
					}
					startFrom --;
					grpPreferences.pack();
					
					if(projectNames.size()==1)
					{
						Removebtn.get(0).setVisible(false);
						//removeButton.get(1).setVisible(false);
						
					}

		    	}
	    	});
			
		}
		startFrom	 = projectNames.size();
		
	}
	
	public static String toTitleCase(String input) 
	{
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;
	    char second_char=0;
	    String[] seperated_by_space =input.split(" "); 
	    try {
			for(int i= 0; i<seperated_by_space.length; i++)
			{
				if (seperated_by_space[i].length() > 1)
					{
						second_char = seperated_by_space[i].charAt(1);
					} 
					else if (seperated_by_space[i].length() == 1)
					{
						second_char = seperated_by_space[i].charAt(0);
					}
					if (Character.isDigit(second_char))
					{
						for (char c : seperated_by_space[i].toCharArray())
						{

							titleCase.append(c);
						}	
					}
					if (!Character.isAlphabetic(second_char) &&!Character.isDigit(second_char) )
					{
						for (char c : seperated_by_space[i].toCharArray())
						{

							titleCase.append(c);
						}
					}
					if (Character.isLowerCase(second_char))
					{
						for (char c : seperated_by_space[i].toCharArray())
						{
							if (nextTitleCase) {
								c = Character.toTitleCase(c);
								nextTitleCase = false;
							}

							titleCase.append(c);
						}
					} 
					else if (Character.isUpperCase(second_char)) 
					{
						for (char c : seperated_by_space[i].toCharArray())
						{

							titleCase.append(c);
						}
					}
					
				
				
				if (i != seperated_by_space.length - 1) 
				{
					titleCase.append(" ");
					nextTitleCase = true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return titleCase.toString();
	}

	
	protected void checkSubclass()
	{
		//this is blank method so will disable the check that prevents subclassing of shells.
	}
	

}

