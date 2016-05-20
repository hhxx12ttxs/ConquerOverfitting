package gnukhata.views;

/*
 * @authors
 * Amit Chougule <acamit333@gmail.com>,
 * Girish Joshi <girish946@gmail.com>, 
 */


import gnukhata.globals;
import gnukhata.controllers.StartupController;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

//import com.sun.xml.internal.ws.transport.http.DeploymentDescriptorParser;

/*
 * this class is the loginform for the gnukhata.
 */
public class PreferencesForm extends Shell
{
	static Display display;
	String strOrgName;
	String strFromYear;
	String strToYear;
	String strOrgType;
	
	int counter=0;
	
	Button chkbtnProjAcc;
	Label lblInfo;
	Button btnAddNewProj;
	
	Button chkbtnManualAccCode;
	Button btnSave;
	Button btnCreateAcc;
	Button btnQuit;
	int startFrom = 0;
	
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
	public PreferencesForm() {
		super(display);
		strOrgName = globals.session[1].toString();
		strFromYear =  globals.session[2].toString();
		strToYear =  globals.session[3].toString();
		
		nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		FormLayout formlayout = new FormLayout();
		this.setLayout(formlayout);		
		this.setText("Preferences Form ");
		
		FormData layout = new FormData();
		
		Label lblHeadline = new Label(this,SWT.None);
		lblHeadline.setFont(new Font(display, "Times New Roman", 13, SWT.BOLD));
		lblHeadline.setText("GNUKhata: A Free and Open Source Accounting Software");
		layout = new FormData();
		layout.top = new FormAttachment(2);
		layout.left = new FormAttachment(2);
		layout.right = new FormAttachment(51);
		layout.bottom = new FormAttachment(8);
		lblHeadline.setLayoutData(layout);
		Label lblLogo = new Label(this, SWT.None);
		//Image img = new Image(display,"finallogo1.png");
		lblLogo.setImage(globals.logo);
		layout = new FormData();
		layout.top = new FormAttachment(1);
		layout.left = new FormAttachment(70);
		layout.right = new FormAttachment(100);
		layout.bottom = new FormAttachment(12);
		lblLogo.setLayoutData(layout);
				
		Label lblOrgDetails = new Label(this,SWT.NONE);
		lblOrgDetails.setFont( new Font(display,"Times New Roman", 12, SWT.BOLD) );
		lblOrgDetails.setText(strOrgName+"\n"+"For Financial Year "+"From "+strFromYear+" To "+strToYear );
		layout = new FormData();
		layout.top = new FormAttachment(10);
		layout.left = new FormAttachment(2);
		layout.right = new FormAttachment(69);
		layout.bottom = new FormAttachment(18);
		lblOrgDetails.setLayoutData(layout);
		
		Label lblLine = new Label(this,SWT.NONE);
		lblLine.setText("------------------------------------------------------------------------------------------------------------------------------");
		lblLine.setFont(new Font(display, "Times New Roman", 26, SWT.ITALIC));
		layout = new FormData();
		layout.top = new FormAttachment(lblOrgDetails , 1);
		layout.left = new FormAttachment(2);
		layout.right = new FormAttachment(96);
		layout.bottom = new FormAttachment(22);
		lblLine.setLayoutData(layout);
		

		Label lblCreateAcc = new Label(this, SWT.NONE);
		lblCreateAcc.setText("Preferences");
		lblCreateAcc.setFont(new Font(display,"Times New Romen",20,SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(28);
		layout.left = new FormAttachment(40);
		layout.right = new FormAttachment(59);
		//layout.bottom = new FormAttachment(32);
		lblCreateAcc.setLayoutData(layout);
		
		chkbtnProjAcc = new Button(this, SWT.CHECK);
		chkbtnProjAcc.setText("&Projectwise Accounting");
		chkbtnProjAcc.setFont(new Font(display,"Times New Romen",14,SWT.NONE));
		layout = new FormData();
		layout.top = new FormAttachment(35);
		layout.left = new FormAttachment(34);
		layout.right = new FormAttachment(59);
		//layout.bottom = new FormAttachment(35);
		chkbtnProjAcc.setLayoutData(layout);
		
		lblInfo = new Label(this,SWT.NONE);
		lblInfo.setText("Enter Project Name and Its Sanctioned Amount");
		lblInfo.setFont(new Font(display, "Times New Roman", 14, SWT.NONE));
		layout = new FormData();
		layout.top = new FormAttachment(chkbtnProjAcc,5);
		layout.left = new FormAttachment(32);
		layout.right = new FormAttachment(66);
		//layout.bottom = new FormAttachment(47);
		lblInfo.setLayoutData(layout);
	
				
		grpPreferences = new Group(this, SWT.NONE);
		layout = new FormData();
		layout.top = new FormAttachment(lblInfo, 10);
		layout.left = new FormAttachment(28);
		layout.right = new FormAttachment(70);
		layout.bottom = new FormAttachment(72);
		grpPreferences.setLayoutData(layout);
		
		GridData gd = new GridData();
		GridLayout gl = new GridLayout();		
		gl.numColumns = 3;
		grpPreferences.setLayout(gl);

		lblprjname = new Label(grpPreferences, SWT.BORDER);
		lblprjname.setText("Project Name");
		lblprjname.setFont(new Font(display,"Times New Romen",11,SWT.NONE));
		gd=new GridData();
		gd.widthHint=180;
		lblprjname.setLayoutData(gd);	
		
		lblamount = new Label(grpPreferences, SWT.BORDER);
		lblamount.setText("Amount");
		lblamount.setFont(new Font(display,"Times New Romen",11,SWT.NONE));
	    gd = new GridData();
	    gd.widthHint=170;
		lblamount.setLayoutData(gd);
		
		lblRemovebtn = new Label(grpPreferences, SWT.NONE);
		//lblRemovebtn.setText("\t\t Remove \t\t");
		gd=new GridData();
		gd.widthHint=50;
		lblRemovebtn.setLayoutData(gd);
		
		txtprjname = new Text(grpPreferences, SWT.BORDER);
	    gd = new GridData();
	    gd.widthHint=180;
	    txtprjname.setFont(new Font(display,"Times New Romen",11,SWT.NONE));
		txtprjname.setLayoutData(gd);
		txtamount = new Text(grpPreferences, SWT.BORDER | SWT.RIGHT);
	    gd = new GridData();
	    gd.widthHint=170;
	   // txtamount.setText("0.00");
		txtamount.setLayoutData(gd);
			
		
		btnRemove = new Button(grpPreferences, SWT.BORDER);
		btnRemove.setText(" R&emove ");
		btnRemove.setVisible(true);
	    btnRemove.setFont(new Font(display,"Times New Romen",10,SWT.NONE));
		gd = new GridData();
	    gd.widthHint=120;
		btnRemove.setLayoutData(gd);
		
		projectNames.add(txtprjname);
		amounts.add(txtamount);
		Removebtn.add(btnRemove);
		grpPreferences.pack();
		
		
		this.makeaccessible(grpPreferences);
		
		chkbtnManualAccCode= new Button(this, SWT.CHECK);
		chkbtnManualAccCode.setText("Manual Account Codes");
		chkbtnManualAccCode.setFont(new Font(display,"Times New Romen",14,SWT.NONE));
		layout = new FormData();
		layout.top = new FormAttachment(btnAddNewProj,16);
		layout.left = new FormAttachment(34);
		layout.right = new FormAttachment(56);
		layout.bottom = new FormAttachment(84);
		chkbtnManualAccCode.setLayoutData(layout);
		chkbtnManualAccCode.setVisible(false);

		btnAddNewProj = new Button(this,SWT.PUSH);
		btnAddNewProj.setText("Add &New Project");
		btnAddNewProj.setFont(new Font(display, "Times New Roman", 10, SWT.NONE));
		layout = new FormData();
		layout.top = new FormAttachment(chkbtnManualAccCode, 20);
		layout.left = new FormAttachment(25);
		layout.right = new FormAttachment(40);
		layout.bottom = new FormAttachment(91);
		btnAddNewProj.setLayoutData(layout);
		btnAddNewProj.setVisible(false);
		
		btnSave = new Button(this,SWT.PUSH);
		btnSave.setText("&Save");
		btnSave.setFont(new Font(display, "Times New Roman", 10, SWT.NONE));
		layout = new FormData();
		layout.top = new FormAttachment(chkbtnManualAccCode, 20);
		layout.left = new FormAttachment(btnAddNewProj,25);
		layout.right = new FormAttachment(48);
		layout.bottom = new FormAttachment(91);
		btnSave.setLayoutData(layout);
		
		
		btnCreateAcc = new Button(this,SWT.PUSH);
		btnCreateAcc.setText("&Create Account");
		btnCreateAcc.setFont(new Font(display, "Times New Roman", 10, SWT.NONE));
		layout = new FormData();
		layout.top = new FormAttachment(chkbtnManualAccCode, 20);
		layout.left = new FormAttachment(btnSave,25);
		layout.right = new FormAttachment(65);
		layout.bottom = new FormAttachment(91);
		btnCreateAcc.setLayoutData(layout);
		btnCreateAcc.setVisible(false);
	
	
		
		grpPreferences.setVisible(false);
		
		this.setEvents();
		this.getAccessible();
		this.pack();
		this.open();
		this.showView();
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
			Removebtn.get(0).setVisible(false);	
		// TODO Auto-generated method stub
		this.btnCreateAcc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				//super.widgetSelected(arg0);
			}
		});
		this.grpPreferences.addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				//super.focusGained(arg0);
				txtprjname.setFocus();
			}
		});
		this.chkbtnProjAcc.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				//super.widgetSelected(arg0);
				if( chkbtnProjAcc.getSelection()==true)
				{
					
						grpPreferences.setVisible(true);
						projectNames.get(0).setFocus();
						btnAddNewProj.setVisible(true);
						btnSave.setVisible(true);
						btnCreateAcc.setVisible(false);
			/*
					while(!projectNames.isEmpty())
					 {
							projectNames.get(0).dispose();
							projectNames.remove(0);
							amounts.get(0).dispose();
							amounts.remove(0);
							Removebtn.get(0).dispose();
							Removebtn.remove(0); 
					}*/
					 }
					
						
			/*		grpPreferences.setVisible(true);
					projectNames.get(0).setFocus();
					btnAddNewProj.setVisible(true);	
					//btnRemoveProj.setVisible(true);
					
						while(!projectNames.isEmpty())
						{
					projectNames.get(0).dispose();
					projectNames.remove(0);
					amounts.get(0).dispose();
					amounts.remove(0);
					Removebtn.get(0).dispose();
					Removebtn.remove(0);
					//grpPreferences.pack();
						}*/
				//}
				else
				{
					grpPreferences.setVisible(false);
					btnAddNewProj.setVisible(false);
					
					/*while(!projectNames.isEmpty())
					{
					projectNames.get(0).dispose();
					projectNames.remove(0);
					amounts.get(0).dispose();
					amounts.remove(0);
					Removebtn.get(0).dispose();
					Removebtn.remove(0);
					//grpPreferences.pack();
					}*/
					
				}
			}
		});
		this.btnAddNewProj.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				grpPreferences.setEnabled(true);
				btnCreateAcc.setVisible(false);
				btnSave.setVisible(true);
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
		});
		setDynamicRowEvents();
		
		this.btnSave.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			grpPreferences.setEnabled(false);
				
			if(chkbtnProjAcc.getSelection()==true)
			 {
				for(int prjnmvalidation = 0;  prjnmvalidation < projectNames.size(); prjnmvalidation ++)
				{
					if( projectNames.get(prjnmvalidation).getText().trim().equals(""))
					{
						MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK );
						msgaccerr .setMessage("Please Enter Project Name");
						msgaccerr.open();
						grpPreferences.setEnabled(true);
						projectNames.get(prjnmvalidation).setFocus();
						return;	
					}
				}				
				
			}
			
				String accountCodeFlag = "";
				boolean result = true;
				if (chkbtnProjAcc.getSelection())
				{
				HashMap<String , String> projectAmount = new HashMap<String, String>();
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2);
				nf.setGroupingUsed(false);

				for (int i = 0; i < projectNames.size(); i++ )
				{
					projectAmount.put(projectNames.get(i).getText() ,nf.format(Double.valueOf(amounts.get(i).getText())));
					
				}
				
				result = StartupController.setProjects(projectAmount);
				}
				if (chkbtnManualAccCode.getSelection())
				{
					accountCodeFlag = "manually";
					globals.session[5] = accountCodeFlag; 
					
				}
				else
				{
					accountCodeFlag = "automatic";
					globals.session[5] = accountCodeFlag;
				}


				if ( result)
				{
					boolean prefs = StartupController.setPreferences(accountCodeFlag);
					/*MessageBox msg = new MessageBox(new Shell(),SWT.OK);
					msg.setMessage("Data is saved successfully.");
					msg.open();*/
					btnSave.setVisible(false);
						
					
					//lblsavemsg.setVisible(true);
					btnCreateAcc.setVisible(true);
					btnCreateAcc.setFocus();					
				}
				else
				{
					MessageBox msg1 = new MessageBox(new Shell(),SWT.OK);
					msg1.setMessage("Could not save the project, there is an error");
					msg1.open();
				}

			}

			private MessageBox MessageBox(Shell shell, int ok) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		this.btnCreateAcc.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent se)
			{
				dispose();
				gnukhata.controllers.StartupController.showCreateAccount();
			}
			
		});
		
		chkbtnProjAcc.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.CR || arg0.keyCode==SWT.KEYPAD_CR)
				{
					if(chkbtnProjAcc.getSelection()==false)
					{						
						btnSave.setFocus();
						//projectNames.get(0).setFocus();				
					}
						
				}
			}			
		});
		/*chkbtnManualAccCode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.CR || arg0.keyCode==SWT.KEYPAD_CR)
					{					
						btnSave.setFocus();					
					}					
				if(arg0.keyCode==SWT.ARROW_UP)
				{
					if(btnAddNewProj.isVisible()==true)
					{
						btnAddNewProj.setFocus();
					}
					else
					{
						chkbtnProjAcc.setFocus();	
					}
					
				}
			}			
		});*/
				
		btnAddNewProj.addKeyListener(new KeyAdapter() {
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
					grpPreferences.setEnabled(true);
					btnSave.setVisible(true);
					btnCreateAcc.setVisible(false);
				}
			}			
		});
		btnSave.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.ARROW_LEFT)
				{
					if(btnAddNewProj.isVisible()==true)
					{
						btnAddNewProj.setFocus();
					}
					else
					{
						chkbtnProjAcc.setFocus();	
					}
					
				}
				
				if(arg0.keyCode==SWT.KEYPAD_CR)
				{
					grpPreferences.setEnabled(false);
					btnCreateAcc.setFocus();
					
				}
				if(arg0.keyCode==SWT.ARROW_LEFT)
				{
					btnAddNewProj.setFocus();
					
				}
			}			
		});
		btnCreateAcc.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				/*if(arg0.keyCode==SWT.ARROW_UP)
				{
					btnSave.setFocus();
				}*/
				if(arg0.keyCode==SWT.ARROW_LEFT)
				{
					if(btnAddNewProj.isVisible()==true)
					{
						btnAddNewProj.setFocus();
					}
					else
					{
						chkbtnProjAcc.setFocus();	
					}
					
				}
			
			}			
		});				
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
							MessageBox msg=new MessageBox(new Shell(),SWT.ERROR|SWT.OK);				
							msg.setMessage("Please Enter Project Name");
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
							MessageBox msg=new MessageBox(new Shell(),SWT.ERROR|SWT.OK);				
							msg.setMessage("Please Enter Project Name");
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
						chkbtnProjAcc.setFocus();
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
			//focus listenner needed for unique names.
			projectNames.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					Text curprjname = (Text) arg0.widget;
					for(int count = 0; count > projectNames.size(); count ++ )
					{
						if(curprjname.getText().equals(projectNames.get(count).getText() ) )
						{
							MessageBox msg = new MessageBox(new Shell(),SWT.OK);
							msg.setMessage("The project name you entered already exists");
							msg.open();
							curprjname.setText("");
							curprjname.setFocus();
							return;
							
						}
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
						btnAddNewProj.setFocus();
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
						
			/*projectNames.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					
					Text currentPrjnm=(Text) arg0.widget;
					final int rowindex=(Integer) currentPrjnm.getData("curindex");
					if(projectNames.get(rowindex).getText().trim().equals(""))
					{
						MessageBox msg=new MessageBox(new Shell(),SWT.ERROR|SWT.OK);				
						msg.setMessage("Please enter valid Project Name");
						msg.open();
						
						display.getCurrent().asyncExec(new Runnable() {		
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
							projectNames.get(rowindex).setFocus();
							}
						});
						return;
					}
				}
			});*/
			amounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					
					Text currentAmount=(Text) arg0.widget;
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
						e.printStackTrace();
					}
					
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
						btnAddNewProj.setFocus();
											
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
	
	private void showView()
	{
		while(! this.isDisposed())
		{
			if(! this.getDisplay().readAndDispatch())
			{
				this.getDisplay().sleep();
				if ( ! this.getMaximized())
				{
					this.setMaximized(true);
				}
			}
			
		}
		this.dispose();


	}

	
	protected void checkSubclass()
	{
		//this is blank method so will disable the check that prevents subclassing of shells.
	}

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		// display = Display.getDefault();
		PreferencesForm sf = new PreferencesForm();
	}*/
	
}

