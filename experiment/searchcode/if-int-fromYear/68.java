package gnukhata.views;

import gnukhata.globals;
import gnukhata.controllers.reportController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import sun.applet.resources.MsgAppletViewer;

public class viewBalanceSheet extends Composite {
	Color Background;
	Color Foreground;
	Color FocusBackground;
	Color FocusForeground;
	Color BtnFocusForeground;
	static String strOrgName;
	static String strFromYear;
	static String strToYear;
	public static String typeFlag;
	static Display display;
	Text txtddate;
	Text txtmdate;
	Text txtyrdate;
	Label dash1;
	Label dash2;
	Label lblTODt;
	TabFolder tfTransaction;
	Combo drpdwnBalanceSheet;
	Button btnView;
	String searchText = "";
	long searchTexttimeout = 0;
	long wait=0;
    Text FromYear;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	Vector<Object> params;
	protected int[] orgNameList;
	 public viewBalanceSheet(Composite parent, int style) 
	{
		super(parent,style);
		sdf.setLenient(false);
		FormLayout formlayout = new FormLayout();
		this.setLayout(formlayout);
		FormData layout = new FormData();
		MainShell.lblLogo.setVisible(false);
		 MainShell.lblLine.setVisible(false);
		 MainShell.lblOrgDetails.setVisible(false);
		    
		strToYear =  globals.session[3].toString();
		strFromYear=globals.session[2].toString();
		//FromYear.setText(strFromYear.substring(6));
		
		
		Label lblLogo = new Label(this, SWT.None);
		layout = new FormData();
		layout.top = new FormAttachment(1);
		layout.left = new FormAttachment(63);
		layout.right = new FormAttachment(87);
		layout.bottom = new FormAttachment(9);
		//layout.right = new FormAttachment(95);
		//layout.bottom = new FormAttachment(18);
		//lblLogo.setSize(getClientArea().width, getClientArea().height);
		lblLogo.setLocation(getClientArea().width, getClientArea().height);
		lblLogo.setLayoutData(layout);
		//Image img = new Image(display,"finallogo1.png");
		lblLogo.setImage(globals.logo);
		
		Label lblOrgDetails = new Label(this,SWT.NONE);
		lblOrgDetails.setFont( new Font(display,"Times New Roman", 11, SWT.BOLD ) );
		lblOrgDetails.setText(globals.session[1]+"\n"+"For Financial Year "+"From "+globals.session[2]+" To "+globals.session[3] );
		layout = new FormData();
		layout.top = new FormAttachment(2);
		layout.left = new FormAttachment(2);
		//layout.right = new FormAttachment(53);
		//layout.bottom = new FormAttachment(18);
		lblOrgDetails.setLayoutData(layout);

		/*Label lblLink = new Label(this,SWT.None);
		lblLink.setText("www.gnukhata.org");
		lblLink.setFont(new Font(display, "Times New Roman", 11, SWT.ITALIC));
		layout = new FormData();
		layout.top = new FormAttachment(lblLogo,0);
		layout.left = new FormAttachment(65);
		//layout.right = new FormAttachment(33);
		//layout.bottom = new FormAttachment(19);
		lblLink.setLayoutData(layout);*/
		 
		Label lblLine = new Label(this,SWT.NONE);
		lblLine.setText("-------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		lblLine.setFont(new Font(display, "Times New Roman",18, SWT.ITALIC));
		layout = new FormData();
		layout.top = new FormAttachment( lblLogo , 2);
		layout.left = new FormAttachment(2);
		layout.right = new FormAttachment(99);
		layout.bottom = new FormAttachment(22);
		lblLine.setLayoutData(layout);
		

		

		
			
		Label lblviewBalanceSheet = new Label(this, SWT.NONE);
		 if(globals.session[4].equals("profit making"))
		 {
			 lblviewBalanceSheet.setText("View Balance Sheet");
		 }
		 if(globals.session[4].equals("ngo"))
		 {
			 lblviewBalanceSheet.setText("View Statement of Affairs");
		 }
		lblviewBalanceSheet.setFont(new Font(display, "Times New Roman", 16, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,25);
		layout.left = new FormAttachment(39);
		//layout.right = new FormAttachment(65);
		//layout.bottom = new FormAttachment(36);
		lblviewBalanceSheet.setLayoutData(layout);
		

		Label lblperiod = new Label(this,SWT.NONE);
		lblperiod.setFont( new Font(display,"Times New Roman", 12, SWT.BOLD ) );
		lblperiod.setText("Period: ");
		layout = new FormData();
		layout.top = new FormAttachment(36);
		layout.left = new FormAttachment(35);
		//layout.right = new FormAttachment(53);
		//layout.bottom = new FormAttachment(18);
		lblperiod.setLayoutData(layout);
		
		Label lblFromDt=new Label(this, SWT.NONE);
		lblFromDt.setText("&From :");
		lblFromDt.setFont(new Font(display, "Times New Roman", 12, SWT.BOLD));
		layout=new FormData();
		layout.top = new FormAttachment(36);
		layout.left=new FormAttachment(lblperiod,11);		
		lblFromDt.setLayoutData(layout);
		
		Label lblFromDate=new Label(this, SWT.NONE);
		lblFromDate.setText(""+globals.session[2]);
		lblFromDate.setFont(new Font(display, "Times New Roman", 12, SWT.BOLD));
		layout= new FormData();
		layout.top = new FormAttachment(36);
		layout.left= new FormAttachment(44);
		lblFromDate.setLayoutData(layout);
		
		Label lblto =new Label(this , SWT.NONE);
		lblto.setFont(new Font(display,"Times New Roman", 12, SWT.BOLD ));
		lblto.setText("T&o :");
		layout = new FormData();
		layout.top = new FormAttachment(lblperiod,20);
		layout.left = new FormAttachment(41);
		lblto.setLayoutData(layout);
	
		txtddate = new Text(this,SWT.BORDER);
		txtddate.setFocus();
		txtddate.setText(strToYear.substring(0,2));
		txtddate.setTextLimit(2);
		txtddate.setFont(new Font(display, "Times New Roman", 10, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblperiod,20);
		layout.left = new FormAttachment(lblto,10);
		layout.right = new FormAttachment(txtddate,30);
		txtddate.setFocus();
		txtddate.selectAll();
		txtddate.setLayoutData(layout);
		
		
		dash1 = new Label(this,SWT.NONE);
		dash1.setText("-");
		dash1.setFont(new Font(display, "Time New Roman",14,SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblperiod,20);
		layout.left = new FormAttachment(txtddate , 2);
		layout.right = new FormAttachment(dash1 , 5);
		//layout.bottom = new FormAttachment(9);
		dash1.setLayoutData(layout);
		
		txtmdate = new Text(this,SWT.BORDER);
		//txtmdate.setMessage("mm");
		txtmdate.setText(strToYear.substring(3,5));
		txtmdate.setTextLimit(2);
		txtmdate.setFont(new Font(display, "Times New Roman", 10, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblperiod,20);
		layout.left = new FormAttachment(dash1, 10);
		layout.right = new FormAttachment(txtmdate,30);
		//layout.bottom = new FormAttachment(9);
		txtmdate.setLayoutData(layout);
		
		dash2 = new Label(this,SWT.NONE);
		dash2.setText("-");
		dash2.setFont(new Font(display, "Time New Roman",14,SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblperiod,20);
		layout.left = new FormAttachment(txtmdate,2);
		
		layout.right = new FormAttachment(dash2, 5);
		//layout.bottom = new FormAttachment(9);
		dash2.setLayoutData(layout);
		
		txtyrdate = new Text(this,SWT.BORDER);
		/*txtyrdate.setMessage("yyyy");*/
		txtyrdate.setText(strToYear.substring(6));
		txtyrdate.setTextLimit(4);
		txtyrdate.setFont(new Font(display, "Times New Roman", 10, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblperiod,20);
		layout.left = new FormAttachment(dash2 , 10);
		layout.right = new FormAttachment(txtyrdate,50);
		//layout.bottom = new FormAttachment(9);
		txtyrdate.setLayoutData(layout);
	
		Label lblbalsheettype = new Label(this, SWT.NONE);
		if(globals.session[4].equals("profit making"))
		 {
			 lblbalsheettype.setText("View &Balance Sheet:");
		 }
		 if(globals.session[4].equals("ngo"))
		 {
			 lblbalsheettype.setText("View &Statement of Affairs:");
		 }
		lblbalsheettype.setFont(new Font(display, "Times New Roman", 12, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(txtyrdate,15);
		layout.left = new FormAttachment(35);
		lblbalsheettype.setLayoutData(layout);
		
		drpdwnBalanceSheet = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		drpdwnBalanceSheet.setFont(new Font(display,"Times New Roman",11,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(txtyrdate,15);
		layout.left = new FormAttachment(lblbalsheettype,10);
		layout.right = new FormAttachment(drpdwnBalanceSheet,250);
		//layout.bottom = new FormAttachment(14);
		drpdwnBalanceSheet.setLayoutData(layout);
		drpdwnBalanceSheet.add("--Please select--");
		drpdwnBalanceSheet.select(0);
		if(globals.session[4].equals("profit making"))
		 {
			drpdwnBalanceSheet.add("Conventional Balance Sheet");
		 	drpdwnBalanceSheet.add("Sources & Application of Funds");
		 }
		else if(globals.session[4].equals("ngo"))
		 {
			drpdwnBalanceSheet.add("Conventional Statement of Affairs");
		 	drpdwnBalanceSheet.add("Sources & Application of Funds");
		 }


		btnView  =new Button(this,SWT.PUSH);
		btnView.setText("&View");
		btnView.setEnabled(false);
		btnView.setFont(new Font(display, "Times New Roman", 12, SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(txtddate,80);
		layout.left = new FormAttachment(42);
		layout.right=new FormAttachment(48);
		btnView.setLayoutData(layout);
	
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
		txtddate.setForeground(FocusForeground);
		txtddate.setBackground(FocusBackground);

	this.getAccessible();
		this.setEvents();
		this.pack();
	//	this.open();
	//	this.showView();
	}
//the following method sets (registers) all the necesary event listeners on the respective widgets.
//this method will be the last call inside the constructor.

	

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
	
	private void setEvents(){
		
		txtddate.setFocus();
		txtddate.selectAll();
		
		
txtddate.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent arg0) {
				// TODO Auto-generated method stub
//				/*if(verifyFlag== false)
//				{
//					arg0.doit= true;
//					return;
//				}*/
				switch (arg0.keyCode) {
	            case SWT.BS:           // Backspace
	            case SWT.DEL:          // Delete
	            case SWT.HOME:         // Home
	            case SWT.END:          // End
	            case SWT.ARROW_LEFT:   // Left arrow
	            case SWT.ARROW_RIGHT:  // Right arrow
	            case SWT.TAB:
	            case SWT.CR:
	            case SWT.KEYPAD_CR:
	            case SWT.KEYPAD_DECIMAL:
	                return;
	        }
				if(arg0.keyCode==46)
				{
					return;
				}
	        if (!Character.isDigit(arg0.character)) {
	            arg0.doit = false;  // disallow the action
	        }

			}
		});
		
		txtmdate.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent arg0) {
				// TODO Auto-generated method stub
//				/*if(verifyFlag== false)
//				{
//					arg0.doit= true;
//					return;
//				}*/
				switch (arg0.keyCode) {
	            case SWT.BS:           // Backspace
	            case SWT.DEL:          // Delete
	            case SWT.HOME:         // Home
	            case SWT.END:          // End
	            case SWT.ARROW_LEFT:   // Left arrow
	            case SWT.ARROW_RIGHT:  // Right arrow
	            case SWT.TAB:
	            case SWT.CR:
	            case SWT.KEYPAD_CR:
	            case SWT.KEYPAD_DECIMAL:
	                return;
	        }
				if(arg0.keyCode==46)
				{
					return;
				}
	        if (!Character.isDigit(arg0.character)) {
	            arg0.doit = false;  // disallow the action
	        }

			}
		});
		
		
		txtyrdate.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent arg0) {
				// TODO Auto-generated method stub
//				/*if(verifyFlag== false)
//				{
//					arg0.doit= true;
//					return;
//				}*/
				switch (arg0.keyCode) {
	            case SWT.BS:           // Backspace
	            case SWT.DEL:          // Delete
	            case SWT.HOME:         // Home
	            case SWT.END:          // End
	            case SWT.ARROW_LEFT:   // Left arrow
	            case SWT.ARROW_RIGHT:  // Right arrow
	            case SWT.TAB:
	            case SWT.CR:
	            case SWT.KEYPAD_CR:
	            case SWT.KEYPAD_DECIMAL:
	                return;
	        }
				if(arg0.keyCode==46)
				{
					return;
				}
	        if (!Character.isDigit(arg0.character)) {
	            arg0.doit = false;  // disallow the action
	        }

			}
		});
		
		txtddate.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				
				if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
				{
					
					//txtDtDOrg.traverse(SWT.TRAVERSE_TAB_NEXT);
					txtmdate.setFocus();
				}
				

			}
		});
		
		txtddate.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode ==SWT.CR||arg0.keyCode == SWT.KEYPAD_CR)
				{
				if(!txtddate.getText().equals("") && Integer.valueOf ( txtddate.getText())<10 && txtddate.getText().length()< txtddate.getTextLimit())
				{
					txtddate.setText("0"+ txtddate.getText());
					//txtmdate.setFocus();
					txtddate.setFocus();
					return;
					
					
					
				}
				
				}
				if(arg0.keyCode ==SWT.TAB)
				{
					if(txtddate.getText().equals(""))
					{
						txtddate.setText("");
						Display.getCurrent().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								txtddate.setFocus();
							}
						});
						return;
					}
				}
				

			}
		});
		
		
		txtmdate.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode ==SWT.CR||arg0.keyCode == SWT.KEYPAD_CR)
				{
				if(!	txtmdate.getText().equals("") && Integer.valueOf ( 	txtmdate.getText())<10 && 	txtmdate.getText().length()< 	txtmdate.getTextLimit())
				{
					txtmdate.setText("0"+ txtmdate.getText());
					//txtmdate.setFocus();
					
					txtyrdate.setFocus();
					return;
					
					
					
				}
				else
				{
					txtyrdate.setFocus();
				}
				}
				if(arg0.keyCode==SWT.ARROW_UP)
				{
					txtddate.setFocus();
				}
				

			}
		});

		txtyrdate.addKeyListener(new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			//super.keyPressed(arg0);
		
			if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
			{
				
					drpdwnBalanceSheet.setFocus();
				
			}
				
			if(arg0.keyCode==SWT.ARROW_UP)
			{
				txtmdate.setFocus();
			}
		

		}
	});
	

	
		
				
		drpdwnBalanceSheet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				//super.widgetSelected(arg0);
				
			
				if(drpdwnBalanceSheet.getSelectionIndex()<= 0 )
				{
					btnView.setEnabled(false);
					
				}
				else
				{
					btnView.setEnabled(true);
				}
			}
		});
		
			btnView.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.ARROW_UP)
				{
						drpdwnBalanceSheet.setFocus();
				}
			}
		});

		
		
				
		

						btnView.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						
						if(txtddate.getText().trim().equals(""))
						{
							MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msgDayErr.setText("Validation Date Error!");
							msgDayErr.setMessage("Please enter a valid Date.");
							msgDayErr.open();
							txtddate.setFocus();
							
							return;
						}
						if(txtmdate.getText().trim().equals(""))
						{
							MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msgDayErr.setText("Validation Date Error!");
							msgDayErr.setMessage("Please enter a valid Month.");
							msgDayErr.open();
							txtmdate.setFocus();
							
							return;
						}
						
						
						if(txtyrdate.getText().trim().equals(""))
						{
							MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msgDayErr.setText("Validation Date Error!");
							msgDayErr.setMessage("Please enter a valid Year.");
							msgDayErr.open();
							txtyrdate.setFocus();
							
							return;
						}
						if(!txtddate.getText().trim().equals("") && (Integer.valueOf(txtddate.getText())> 31 || Integer.valueOf(txtddate.getText()) <= 0) )
						{
							MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR);
							msgdateErr.setText("Date Validation Error!");
							msgdateErr.setMessage("You have entered an invalid Date");
							txtddate.setText("");
							txtddate.setFocus();
							msgdateErr.open();
							return;
						}
						if(!txtmdate.getText().trim().equals("") && (Integer.valueOf(txtmdate.getText())> 12 || Integer.valueOf(txtmdate.getText()) <= 0) )
						{
							MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR);
							msgdateErr.setText("Month Validation Error!");
							msgdateErr.setMessage("You have entered an invalid month");
							txtmdate.setText("");
							txtmdate.setFocus();
							msgdateErr.open();
							return;
						}
						
						if(!txtyrdate.getText().trim().equals("") && (Integer.valueOf(txtyrdate.getText())> 2100 || Integer.valueOf(txtyrdate.getText()) < 1900) )
						{
							MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR);
							msgdateErr.setText("Year Validation Error!");
							msgdateErr.setMessage("You have entered an invalid Year");
							txtyrdate.setText("");
							txtyrdate.setFocus();
							msgdateErr.open();
							return;
						}
					
						
						try {
							Date voucherDate = sdf.parse(txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText());
							
							} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							MessageBox msg = new MessageBox(new Shell(),SWT.OK | SWT.ICON_ERROR);
							msg.setText("Error!");
							msg.setMessage("Invalid Date");
							txtddate.setFocus();
							msg.open();
							return;
						}
						
						if(txtddate.getText().trim().equals("")&&txtmdate.getText().trim().equals("")&&txtyrdate.getText().trim().equals(""))
						{
							MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msgDayErr.setText("Validation Date Error!");
							msgDayErr.setMessage("Please enter a valid Date.");
							msgDayErr.open();
							txtddate.setFocus();
							
							return;
						}
						
						
					/*	if(txtddate.getText().trim().equals(""))
						{
							MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msgDayErr.setText("Validation Date Error!");
							msgDayErr.setMessage("Please enter a valid Date.");
							msgDayErr.open();
							txtddate.setFocus();
							
							return;
						}
						if(txtmdate.getText().trim().equals(""))
						{
							MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msgDayErr.setText("Validation Date Error!");
							msgDayErr.setMessage("Please enter a valid Date.");
							msgDayErr.open();
							txtmdate.setFocus();
							
							return;
						}
						
						
						if(txtyrdate.getText().trim().equals(""))
						{
							MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
							msgDayErr.setText("Validation Date Error!");
							msgDayErr.setMessage("Please enter a valid Date.");
							msgDayErr.open();
							txtyrdate.setFocus();
							
							return;
						}*/
						if(!txtddate.getText().equals("") && Integer.valueOf ( txtddate.getText())<10 && txtddate.getText().length()< txtddate.getTextLimit())
						{
							txtddate.setText("0"+ txtddate.getText());
						}
						if(!txtmdate.getText().equals("") && Integer.valueOf ( txtmdate.getText())<10 && txtmdate.getText().length()< txtmdate.getTextLimit())
						{
							txtmdate.setText("0"+ txtmdate.getText());
						}
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						try {
							Date voucherDate = sdf.parse(txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText());
							Date fromDate = sdf.parse(globals.session[2].toString().substring(6)+ "-" + globals.session[2].toString().substring(3,5) + "-"+ globals.session[2].toString().substring(0,2));
							Date toDate = sdf.parse(globals.session[3].toString().substring(6)+ "-" + globals.session[3].toString().substring(3,5) + "-"+ globals.session[3].toString().substring(0,2));
							
							if(voucherDate.compareTo(fromDate)< 0 || voucherDate.compareTo(toDate) > 0 )
							{
								MessageBox errMsg = new MessageBox(new Shell(),SWT.ERROR |SWT.OK | SWT.ICON_ERROR );
								errMsg.setText("Validation Date Error!");
								errMsg.setMessage("The Voucher date you entered is not within the Financial Year");
								errMsg.open();
								txtddate.setFocus();
								txtddate.setSelection(0,2);
								return;
							}
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.getMessage();
						}

				
				// TODO Auto-generated method stub
				//super.widgetSelected(arg0);
				//make a call to the reportController.getLedger()
				Composite grandParent = (Composite) btnView.getParent().getParent();
				//MessageBox msg = new MessageBox(new Shell(), SWT.OK);
				//msg.setMessage(grandParent.getText());
				//msg.open();
				//String fromDate = txtddate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText();
				

				
				String bsType = drpdwnBalanceSheet.getItem(drpdwnBalanceSheet.getSelectionIndex());
				String endDate = txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText();
				
				btnView.getParent().dispose();
				gnukhata.controllers.reportController.showBalanceSheet(grandParent, endDate, bsType);
				dispose();
				
				
			}
			
		});
			
						
						txtddate.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent arg0) {
								// TODO Auto-generated method stub
								//super.keyPressed(arg0);
								if((arg0.keyCode>= 48 && arg0.keyCode <= 57) ||  arg0.keyCode== 8 || arg0.keyCode == 13||
										arg0.keyCode == SWT.KEYPAD_0||arg0.keyCode == SWT.KEYPAD_1||arg0.keyCode == SWT.KEYPAD_2||arg0.keyCode == SWT.KEYPAD_3||arg0.keyCode == SWT.KEYPAD_4||
										arg0.keyCode == SWT.KEYPAD_5||arg0.keyCode == SWT.KEYPAD_6||arg0.keyCode == SWT.KEYPAD_7||arg0.keyCode == SWT.KEYPAD_8||arg0.keyCode == SWT.KEYPAD_9
										||arg0.keyCode == SWT.ARROW_RIGHT||arg0.keyCode == SWT.ARROW_LEFT)
								{
									arg0.doit = true;
									
								}
								else
								{
									
									arg0.doit = false;
								}
							}
						});
						
						txtmdate.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent arg0) {
								// TODO Auto-generated method stub
								//super.keyPressed(arg0);
								if((arg0.keyCode>= 48 && arg0.keyCode <= 57) ||  arg0.keyCode== 8 || arg0.keyCode == 13||
										arg0.keyCode == SWT.KEYPAD_0||arg0.keyCode == SWT.KEYPAD_1||arg0.keyCode == SWT.KEYPAD_2||arg0.keyCode == SWT.KEYPAD_3||arg0.keyCode == SWT.KEYPAD_4||
										arg0.keyCode == SWT.KEYPAD_5||arg0.keyCode == SWT.KEYPAD_6||arg0.keyCode == SWT.KEYPAD_7||arg0.keyCode == SWT.KEYPAD_8||arg0.keyCode == SWT.KEYPAD_9||arg0.keyCode == SWT.KEYPAD_CR
										||arg0.keyCode == SWT.ARROW_RIGHT||arg0.keyCode == SWT.ARROW_LEFT)
								{
									arg0.doit = true;
								}
								else
								{
									
									arg0.doit = false;
								}
							}
						});
						
						
						txtyrdate.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent arg0) {
								// TODO Auto-generated method stub
								//super.keyPressed(arg0);
								if((arg0.keyCode>= 48 && arg0.keyCode <= 57) ||  arg0.keyCode== 8 || arg0.keyCode == 13||
										arg0.keyCode == SWT.KEYPAD_0||arg0.keyCode == SWT.KEYPAD_1||arg0.keyCode == SWT.KEYPAD_2||arg0.keyCode == SWT.KEYPAD_3||arg0.keyCode == SWT.KEYPAD_4||
										arg0.keyCode == SWT.KEYPAD_5||arg0.keyCode == SWT.KEYPAD_6||arg0.keyCode == SWT.KEYPAD_7||arg0.keyCode == SWT.KEYPAD_8||arg0.keyCode == SWT.KEYPAD_9||arg0.keyCode == SWT.KEYPAD_CR
										||arg0.keyCode == SWT.ARROW_RIGHT||arg0.keyCode == SWT.ARROW_LEFT)
								{
									arg0.doit = true;
								}
								else
								{
									
									arg0.doit = false;
								}
							}
						});
						
						txtddate.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent arg0) {
								// TODO Auto-generated method stub
								//super.focusLost(arg0);
								
								if(!txtddate.getText().equals("") && Integer.valueOf ( txtddate.getText())<10 && txtddate.getText().length()< txtddate.getTextLimit())
								{
									txtddate.setText("0"+ txtddate.getText());
								}
								
								/*if(!txtddate.getText().equals("") && (Integer.valueOf(txtddate.getText())> 31 || Integer.valueOf(txtddate.getText()) <= 0) )
								{
									MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
									msgdateErr.setText("Validation Date Error!");
									msgdateErr.setMessage("You have entered an Invalid Date");
									msgdateErr.open();
									
									txtddate.setText("");
									Display.getCurrent().asyncExec(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											txtddate.setFocus();
											
										}
									});	
									return;
								}*/
															}
						});
						
						txtmdate.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent arg0) {
								// TODO Auto-generated method stub
								//super.focusLost(arg0);
								if(!txtmdate.getText().equals("") && Integer.valueOf ( txtmdate.getText())<10 && txtmdate.getText().length()< txtmdate.getTextLimit())
								{
									 txtmdate.setText("0"+  txtmdate.getText());
								}
								/*if(!txtmdate.getText().equals("") && (Integer.valueOf(txtmdate.getText())> 12 || Integer.valueOf(txtmdate.getText()) <= 0) )
								{
									MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR | SWT.ICON_ERROR);
									msgdateErr.setText("Validation Month Error!");
									msgdateErr.setMessage("You have entered an Invalid Month");
									msgdateErr.open();
									
									txtmdate.setText("");
									Display.getCurrent().asyncExec(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											txtmdate.setFocus();
											
										}
									});
									return;
								}*/
								
						}
						});
						
						/*txtyrdate.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent arg0) {
								// TODO Auto-generated method stub
								//super.focusLost(arg0);
								
								
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
								try {
									//Date ledgerStart = sdf.parse(txtFromDtYear.getText()+ "-"+ txtFromDtMonth.getText()+"-"+ txtFromDtDay.getText() );
									Date ledgerEnd = sdf.parse(txtyrdate.getText()+ "-"+ txtmdate.getText()+"-"+ txtddate.getText() );
									Date financialStart = sdf.parse(globals.session[2].toString().substring(6) +"-"+globals.session[2].toString().substring(3,5)+"-"+ globals.session[2].toString().substring(0,2));
									Date financialEnd = sdf.parse(globals.session[3].toString().substring(6) +"-"+globals.session[3].toString().substring(3,5)+"-"+ globals.session[3].toString().substring(0,2));
									if((ledgerEnd.compareTo(financialStart)<0 || ledgerEnd.compareTo(ledgerEnd)> 0 ) )
									{
										MessageBox msg = new MessageBox(new Shell(),SWT.ERROR|SWT.OK | SWT.ICON_ERROR);
										msg.setText("Validation Date Error!");
										msg.setMessage("Please enter the date range within the financial year");
										msg.open();
										txtyrdate.setText("");
										Display.getCurrent().asyncExec(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												txtyrdate.setFocus();
											}
										});
										
										return;
									}
													
								} catch(java.text.ParseException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							
														}
						});*/
						drpdwnBalanceSheet.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent arg0) {
								//code here
								if(arg0.keyCode== SWT.CR || arg0.keyCode == SWT.KEYPAD_CR)
								{
									if(drpdwnBalanceSheet.getSelectionIndex()> 0  )
									{
									
									btnView.setFocus();
									return;
									}
								}
								long now = System.currentTimeMillis();
								if (now > searchTexttimeout){
							         searchText = "";
							      }
								searchText += Character.toLowerCase(arg0.character);
								searchTexttimeout = now + 1000;					
								for(int i = 0; i < drpdwnBalanceSheet.getItemCount(); i++ )
								{
									if(drpdwnBalanceSheet.getItem(i).toLowerCase().startsWith(searchText ) ){
										//arg0.doit= false;
										drpdwnBalanceSheet.select(i);
										drpdwnBalanceSheet.notifyListeners(SWT.Selection ,new Event()  );
										break;
									}
								}
							}
						});
						
								
						drpdwnBalanceSheet.addKeyListener(new KeyAdapter() {
							public void keyPressed(KeyEvent arg0) {
								// TODO Auto-generated method stub
								//super.keyPressed(arg0);
								if(arg0.keyCode ==SWT.CR)
								{
									
									if (drpdwnBalanceSheet.getSelectionIndex() == 0)
									{
										MessageBox	 msg = new MessageBox(new Shell(),SWT.OK| SWT.ERROR | SWT.ICON_ERROR);
										msg.setText("Error!");
										msg.setMessage("Please select the  BalanceSheet Type.");
										msg.open();
										drpdwnBalanceSheet.setFocus();
										return;
									}
									//btnView.setFocus();
									btnView.notifyListeners(SWT.Selection ,new Event() );
								}
								if(arg0.keyCode==SWT.ARROW_UP)
								{
									if(drpdwnBalanceSheet.getSelectionIndex()<=1)
									{
									txtyrdate.setFocus();
									}
								}
								
							}
							});
						
						/*txtyrdate.addFocusListener(new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent arg0) {
								// TODO Auto-generated method stub
								//super.focusLost(arg0);
								
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
								try {
									Date voucherDate = sdf.parse(txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText());
									Date fromDate = sdf.parse(globals.session[2].toString().substring(6)+ "-" + globals.session[2].toString().substring(3,5) + "-"+ globals.session[2].toString().substring(0,2));
									Date toDate = sdf.parse(globals.session[3].toString().substring(6)+ "-" + globals.session[3].toString().substring(3,5) + "-"+ globals.session[3].toString().substring(0,2));
									
									if(voucherDate.compareTo(fromDate)< 0 || voucherDate.compareTo(toDate) > 0 )
									{
										MessageBox errMsg = new MessageBox(new Shell(),SWT.ERROR |SWT.OK |SWT.ICON_ERROR);
										errMsg.setText("Validation Date Error!");
										errMsg.setMessage("Please enter the date within the financial year");
										errMsg.open();
										txtyrdate.setText("");
										Display.getCurrent().asyncExec(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												
												txtyrdate.setFocus();
												
											}
										});
										
										return;
									}
								} 
								catch (java.text.ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							
						});*/

						
	}

	

	protected void checkSubclass()
	{
	//this is blank method so will disable the check that prevents subclassing of shells.
	}
	
	
	/*public static void main(String[] args)
	{
		Display d = new Display();
		Shell s = new Shell(d);
		viewTrialBalance vtb = new viewTrialBalance(s, SWT.NONE);
		vtb.setSize(s.getClientArea().width, s.getClientArea().height );
		
		//s.setSize(400, 400);
		s.pack();
		s.open();
		while (!s.isDisposed() ) {
			if (!d.readAndDispatch())
			{
				 d.sleep();
				 if(! s.getMaximized())
				 {
					 s.setMaximized(true);
				 }
			}
	}	}*/
		
	}
	
	
