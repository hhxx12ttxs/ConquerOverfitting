package gnukhata.views;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import gnukhata.globals;
import gnukhata.controllers.accountController;
import gnukhata.controllers.reportController;
import gnukhata.controllers.transactionController;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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


public class EditVoucherComposite extends Composite
{
	boolean modifyFlag = false;
	
	//Group editVoucher;
	static Display display;
	int counter=0;
	double totalDrAmount = 0.00;
	double totalCrAmount = 0.00;
	
	int startFrom = 0;
	boolean editAmt = false;
	boolean totalRowCalled = false;
	double oldValue = 0.00;
	
	String strOrgName;
	String strFromYear;
	String strToYear;
	
	Label lblLogo;
	Label lblLink ;
	Label lblLine;
	
	Label lblvouchertype;
	
	List<Combo> CrDrFlags = new ArrayList<Combo>();
	List<Combo> accounts = new ArrayList<Combo>();
	List<Text> DrAmounts = new ArrayList<Text>();
	List<Text> CrAmounts = new ArrayList<Text>();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	List<String> masterQueryParams = new ArrayList<String>();
	List<Object> detailQueryParams = new ArrayList<Object>();
	//List<Button> removeButton = new ArrayList<Button>();
	
	String currentCrDr;
	String typeFlag="";
	Label lbldate;
	Text txtddate;
	Label dash1;
	Text txtmdate;
	Label dash2;
	Text txtyrdate;
	Label lblvoucherno;
	Text txtvoucherno;
	

	Label lblDR_CR;
	Label lblAccName;
	Label lblDrAmount;
	Label lblcrAmount;
	Label lblFiller;
	Label lblTotalDrAmt;
	Label lblTotalCrAmt;
	
	
	Text txtDebAmt1;
	Text txtCreAmt1;
	Text txtDebAmt2;
	Text txtCreAmt2;
	Text txttotalDebAmt;
	Text txttotalCrAmt;
	Label lblTotal;
	
	Label lblnarration;
	Text txtnarration;
	Label lblselprj;
	Combo comboselprj;
	
	Combo holdfocuscombo;
	
	Combo comboDr_cr1;
	Combo comboDr_cr2;
	Combo comboDr_CrAccName1;
	Combo comboDr_CrAccName2;
	Combo newCrDrCombo;
	Combo newAccountsCombo;
	Combo dropDownDrCrFlag;
	Combo dropDownAccount;
	
	Text txtDrAmt;
	Text txtCrAmt;
	Text newTxtDrAmount;
	Text newTxtCrAmount;

	//Button btnRemove;
	Button btnConfirm;
	Button btnBack;
	//Button btnRemove1;
	//Button btnRemove2;
	//Button btnNewRemButton;
	Button btnAddAccount;
	
	
	int voucherEditCode = 0;
	int VoucherEditFlag = 0;
	boolean psdrilldown = false;
	String tbType = null;
	boolean narrationFlag = false;
	String AccountName = "";
	String endDate;
	String startDate = null;
	boolean ledgerDrilldown;
	String selectproject ="" ;
	boolean tbdrilldown = false;
	String PN="";
	int crdrleft = 1;
	int crdrright = 13;
	int accountsleft = 13;
	int accountsright = 50;
	int dramountleft = 50;
	int dramountright = 72;
	int cramountleft = 72;
	int cramountright = 94;
	/*int removeleft = 85;
	int removeright = 99;*/
	int currenttop = 1;
	int incrementby = 8;
	int grpVoucherWidth = 0;
	int grpVoucherHeight = 0;
	
	boolean dualflag;
	NumberFormat nf;
	int totalWidth = 0;
	Group grpEditVoucher;
	boolean verifyFlag = false; 
	boolean dualledgerflag;
	
	String oldaccname;
	String oldfromdate;
	String oldenddate;
	String oldselectproject;
	String oldprojectname;
	boolean oldnarration;
	String popupDrCr = "";
		
	public EditVoucherComposite(Composite parent, int style,String voucherType, int voucherCode,int editFlag, boolean tbdrilldown,boolean psdrilldown,String tbType, boolean ledgerDrilldown, String startDate,String oldfromdate1, String endDate,String oldenddate1, String AccountName,String oldaccname1,String ProjectName,String oldprojectname1, boolean narrationFlag,boolean narration,String selectproject,String oldselectproject1,boolean dualledgerflag) 
	{
		super(parent, style);
		typeFlag= voucherType;
		voucherEditCode = voucherCode;
		VoucherEditFlag = editFlag;
		nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		//oldvalues
		this.oldaccname=oldaccname1;
		this.oldfromdate=oldfromdate1;
		this.oldenddate=oldenddate1;
		this.oldselectproject=oldselectproject1;
		this.oldprojectname=oldprojectname1;
		this.oldnarration=narration;
		FormLayout formlayout = new FormLayout();
		this.setLayout(formlayout);
		
	
		strOrgName = globals.session[1].toString();
		strFromYear =  globals.session[2].toString();
		strToYear =  globals.session[3].toString();
		this.ledgerDrilldown= ledgerDrilldown;
		this.tbdrilldown = tbdrilldown;
		this.tbType= tbType;
		this.AccountName = AccountName;
		this.PN=ProjectName;
		this.narrationFlag= narrationFlag;
		this.psdrilldown = psdrilldown;
		this.selectproject= selectproject;
		this.startDate= startDate;
		this.endDate= endDate;
		this.dualledgerflag=dualledgerflag;
		
		FormLayout formLayout= new FormLayout();
		this.setLayout(formLayout);
	    FormData layout=new FormData();
	    
		    
	    Label lblOrgDetails = new Label(this,SWT.NONE);
		lblOrgDetails.setFont( new Font(display,"Times New Roman", 10, SWT.BOLD ) );
		lblOrgDetails.setText(strOrgName+ "\n"+"For Financial Year "+"From "+strFromYear+" To "+strToYear);
		layout.top = new FormAttachment(2);
		layout.left = new FormAttachment(2);
		lblOrgDetails.setLayoutData(layout);
		
		lblLogo = new Label(this, SWT.None);
		layout = new FormData();
		layout = new FormData();
		layout.top = new FormAttachment(1);
		layout.left = new FormAttachment(70);
		layout.right = new FormAttachment(100);
		layout.bottom = new FormAttachment(12);
		lblLogo.setLayoutData(layout);
		//Image img = new Image(display, "finallogo1.png");
		lblLogo.setImage(globals.logo);
		
				
		lblLine = new Label(this, SWT.NONE);
		lblLine.setText("------------------------------------------------------------------------------------------");
		lblLine.setFont(new Font(display, "Times New Roman", 18, SWT.ITALIC));
		layout = new FormData();
		layout.top = new FormAttachment(lblLogo,5);
		layout.left = new FormAttachment(2);
		layout.right = new FormAttachment(99);
		lblLine.setLayoutData(layout);
		
		
		lblvouchertype = new Label(this, SWT.NONE);
		lblvouchertype.setFont( new Font(display,"Times New Roman", 18, SWT.NORMAL) );
		lblvouchertype.setText(typeFlag +"Voucher");
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,2);
		layout.left = new FormAttachment(5);
		lblvouchertype.setLayoutData(layout);
		
		
		lbldate = new Label(this,SWT.NONE);
		lbldate.setText("Date :");
		lbldate.setFont(new Font(display, "Time New Roman",12,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(55);
		lbldate.setLayoutData(layout);
		
		txtddate = new Text(this,SWT.BORDER | SWT.READ_ONLY);
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(lbldate,2);
		txtddate.setLayoutData(layout);
		txtddate.setEditable(true);
		txtddate.setSelection(0, 2);
		txtddate.setTextLimit(2);
		
		dash1 = new Label(this,SWT.NONE);
		dash1.setText("-");
		dash1.setFont(new Font(display, "Time New Roman",14,SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(txtddate,2);
		dash1.setLayoutData(layout);
		
		txtmdate = new Text(this,SWT.BORDER | SWT.READ_ONLY);
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(dash1,2);
		txtmdate.setLayoutData(layout);
		txtmdate.setEditable(true);
		txtddate.setSelection(0, 2);
		txtmdate.setTextLimit(2);
		
		
		dash2 = new Label(this,SWT.NONE);
		dash2.setText("-");
		dash2.setFont(new Font(display, "Time New Roman",14,SWT.BOLD));
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(txtmdate,2);
		dash2.setLayoutData(layout);
		
		txtyrdate = new Text(this,SWT.BORDER | SWT.READ_ONLY);
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(dash2,2);
		txtyrdate.setLayoutData(layout);
		txtyrdate.setEditable(true);
		txtyrdate.setTextLimit(4);
		txtyrdate.setSelection(0, 4);

		
		lblvoucherno = new Label(this,SWT.NONE);
		lblvoucherno.setText("Voucher No *");
		lblvoucherno.setFont(new Font(display, "Time New Roman",12,SWT.NORMAL));
		layout =new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(25);
		lblvoucherno.setLayoutData(layout);
		
				
		if(VoucherEditFlag==1)
		{
			txtvoucherno = new Text(this,SWT.BORDER |SWT.READ_ONLY);
		}
		if(VoucherEditFlag==2)
		{
			txtvoucherno = new Text(this,SWT.BORDER);
		}
		
		layout = new FormData();
		layout.top = new FormAttachment(lblLine,5);
		layout.left = new FormAttachment(lblvoucherno,5);
		layout.right = new FormAttachment(txtvoucherno, 70);
		txtvoucherno.setLayoutData(layout);
				
		//txtvoucherno.setEditable(false);
		/*if(VoucherEditFlag==2)
		{		 
		txtvoucherno.setEditable(true);
		}*/
		
		grpEditVoucher = new Group(this, SWT.BORDER);
		layout = new FormData();
		layout.top = new FormAttachment(lblvouchertype,14);
		layout.left = new FormAttachment(5);
		layout.right = new FormAttachment(81);
		layout.bottom = new FormAttachment(68);
		grpEditVoucher.setLayoutData(layout);
		//grpEditVoucher.setText("Edit"+"" +"Voucher");
		
		this.setInitialVoucher();
		this.makeaccssible(grpEditVoucher);
		
		Object[] projectlist=transactionController.getAllProjects();

		lblselprj = new Label(this,SWT.NONE);
		lblselprj.setText("Select Project :");
		lblselprj.setFont(new Font(display, "Time New Roman",13,SWT.RIGHT));
		layout = new FormData();
		layout.top = new FormAttachment(grpEditVoucher, 40);
		layout.left = new FormAttachment(5);
		lblselprj.setLayoutData(layout);
		
		if (projectlist.length > 0) 
		{
			lblselprj.setVisible(true);
		}
		else 
		{
			lblselprj.setVisible(false);
		}
		
		
		comboselprj = new Combo(this,SWT.READ_ONLY);
		comboselprj.setToolTipText("select your Orgnization");
		layout = new FormData();
		layout.top = new FormAttachment(grpEditVoucher, 40);
		layout.left = new FormAttachment(lblselprj,5);
		layout.right = new FormAttachment(36);
		layout.bottom=new FormAttachment(45);
		comboselprj.setLayoutData(layout);
		if (projectlist.length > 0) 
		{
			comboselprj.setVisible(true);
			comboselprj.add("No Project");
		}
		else 
		{
			comboselprj.setVisible(false);
		}
		
		String[] allProjects = gnukhata.controllers.transactionController.getAllProjects();
		for (int i = 0; i < allProjects.length; i++ )
		{
			comboselprj.add(allProjects[i]);
		}
		comboselprj.select(0);
		

		lblnarration = new Label(this,SWT.NONE);
		lblnarration.setText("Narration       : ");
		lblnarration.setFont(new Font(display, "Time New Roman",14,SWT.RIGHT));
		layout = new FormData();
		layout.top = new FormAttachment(lblselprj, 5);
		layout.left = new FormAttachment(5);
		lblnarration.setLayoutData(layout);
		
		txtnarration = new Text(this, SWT.MULTI | SWT.BORDER|SWT.WRAP);
		layout = new FormData();
		layout.top = new FormAttachment(lblselprj, 9);
		layout.left = new FormAttachment(lblnarration,4);
		layout.right = new FormAttachment(74);
		layout.bottom = new FormAttachment(86);
		txtnarration.setLayoutData(layout);
		
		btnConfirm = new Button(this,SWT.PUSH);
		btnConfirm.setText("&Confirm");
		btnConfirm.setFont(new Font(display, "Time New Roman",13,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(txtnarration,15);
		layout.left = new FormAttachment(33);
		btnConfirm.setLayoutData(layout);
		
		btnBack = new Button(this,SWT.PUSH);
		btnBack.setText("&Back");
		btnBack.setFont(new Font(display, "Time New Roman",13,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(txtnarration,15);
		layout.left = new FormAttachment(btnConfirm,10);
		btnBack.setLayoutData(layout);
		
		btnAddAccount = new Button(this, SWT.PUSH);
		btnAddAccount.setText("A&dd Account");
		btnAddAccount.setFont(new Font(display, "Time New Roman", 14, SWT.RIGHT));
		layout = new FormData();
		layout.top = new FormAttachment(txtnarration,15);
		layout.left = new FormAttachment(btnBack,10);
		btnAddAccount.setLayoutData(layout);
		btnAddAccount.setEnabled(false);
		
		this.getAccessible();
		this.setBounds(this.getDisplay().getPrimaryMonitor().getBounds());
		grpVoucherWidth = grpEditVoucher.getClientArea().width;
		grpVoucherHeight = grpEditVoucher.getClientArea().height;

		
		Object[] masterDetails = transactionController.getVoucherMaster(voucherCode);
		
		Object[] result=transactionController.getVoucherMaster(voucherCode);
		String strprjname=result[4].toString();
		
		typeFlag = masterDetails[2].toString();
		txtvoucherno.setText(masterDetails[0].toString());
		txtddate.setText(masterDetails[1].toString().substring(0,2));
		txtmdate.setText(masterDetails[1].toString().substring(3,5));
		txtyrdate.setText(masterDetails[1].toString().substring(6));
		//editVoucher.setText(masterDetails[2].toString());
		lblvouchertype.setText(masterDetails[2].toString());
		txtnarration.setText(masterDetails[3].toString());
		comboselprj.setText(strprjname);
		
		
		
		Object[] transactionDetails = transactionController.getVoucherDetails(voucherCode);
		for(int rowCounter = 0; rowCounter < transactionDetails.length; rowCounter ++ )
		{
			Object[] transactionRow =(Object[]) transactionDetails[rowCounter];
			//we will make a call to the addRow function here.
			//it must except the transactionRow created in this for loop.

			addRow(transactionRow);
			
		}
		totalCrAmount= 0.00;
		totalDrAmount = 0.00;
		for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
		{
			try {
				totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
		{
			try {
				totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		totalRow();
		grpEditVoucher.pack();
		this.pack();
		
		if(VoucherEditFlag==1)
		{
			txtddate.setFocus();
			txtddate.setSelection(0, 2);
		}
		if(VoucherEditFlag==2)
		{
			txtvoucherno.setFocus();
		}		

		this.setEvents();
	}
	
	
	private void setInitialVoucher()
	{
		
		int grpWidth = grpEditVoucher.getClientArea().width;
		FormLayout	 grpLayout = new FormLayout();
		grpEditVoucher.setLayout(grpLayout);
		
		lblDR_CR = new Label(grpEditVoucher, SWT.BORDER |SWT.CENTER);
		lblDR_CR.setText("   DR/CR   ");
		FormData fd = new FormData();
		fd.top = new FormAttachment(currenttop);
		fd.left= new FormAttachment(crdrleft);
		fd.right= new FormAttachment(crdrright);
		fd.bottom= new FormAttachment(currenttop + incrementby );
		lblDR_CR.setLayoutData(fd);
		
		lblAccName = new Label(grpEditVoucher,SWT.BORDER | SWT.CENTER);
		lblAccName.setText("        	  Account Name 	          ");
		fd = new FormData();
		fd.left = new FormAttachment(accountsleft);
		fd.right =  new FormAttachment(accountsright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblAccName.setLayoutData(fd);
		
		
		lblDrAmount = new Label(grpEditVoucher, SWT.BORDER);
		lblDrAmount.setText("               Debit Amount                  ");
		fd = new FormData();
		fd.left = new FormAttachment(dramountleft);
		fd.right =  new FormAttachment(dramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblDrAmount.setLayoutData(fd);
		
		

		lblcrAmount = new Label(grpEditVoucher, SWT.BORDER);
		lblcrAmount.setText("               Credit Amount                   ");
		fd = new FormData();
		fd.left = new FormAttachment(cramountleft);
		fd.right =  new FormAttachment(cramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblcrAmount.setLayoutData(fd);
		
		
		/*lblFiller =new Label(grpEditVoucher,SWT.BORDER);
		lblFiller.setText("  ");
		lblFiller.setVisible(false);
		fd = new FormData();
		fd.left = new FormAttachment(removeleft);
		fd.right =  new FormAttachment(removeright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblFiller.setLayoutData(fd);
*/		
		currenttop=currenttop+8;
		
		
	}
	
	
	
	
	
	private void addRow(Object[]transactionRow)
	{
		FormData	 fd = new FormData();
		
		totalWidth = grpEditVoucher.getClientArea().width;
		
		dropDownDrCrFlag = new Combo(grpEditVoucher, SWT.READ_ONLY);
		dropDownDrCrFlag.add("Dr");
		dropDownDrCrFlag.add("Cr");
		dropDownDrCrFlag.select(0);
		fd.left = new FormAttachment(crdrleft);
		fd.right = new FormAttachment(crdrright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		dropDownDrCrFlag.setLayoutData(fd);
		

		dropDownAccount = new Combo(grpEditVoucher, SWT.READ_ONLY);
		fd=new FormData();
		fd.left = new FormAttachment(accountsleft);
		fd.right = new FormAttachment(accountsright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		dropDownAccount.setLayoutData(fd);
		
		txtDrAmt= new Text(grpEditVoucher,SWT.RIGHT);
		fd=new FormData();
		fd.left = new FormAttachment(dramountleft);
		fd.right = new FormAttachment(dramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		txtDrAmt.setLayoutData(fd);
		
		 txtCrAmt= new Text(grpEditVoucher,SWT.RIGHT);
		 fd=new FormData();
		 fd.left = new FormAttachment(cramountleft);
		 fd.right = new FormAttachment(cramountright);
		 fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		txtCrAmt.setLayoutData(fd);
		
		
		
		/*btnRemove = new Button(grpEditVoucher, SWT.BORDER);
		btnRemove.setText("Remove");
		btnRemove.setVisible(true);
		fd=new FormData();
		fd.left = new FormAttachment(removeleft);
		fd.right = new FormAttachment(removeright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		btnRemove.setLayoutData(fd);
		*/
		currenttop = currenttop + 8;

		if(transactionRow[1].toString().equals("Dr"))
		{
			dropDownDrCrFlag.select(0);
			txtDrAmt.setText(transactionRow[2].toString() );
			
		totalDrAmount = totalDrAmount + Double.parseDouble(txtDrAmt.getText());
			
			
			if(typeFlag.equals("Contra"))
					{
				dropDownAccount.setItems(transactionController.getContra());
				
					}
			if(typeFlag.equals("Journal")  ){
				dropDownAccount.setItems(transactionController.getJournal());
			}
			if(typeFlag.equals("Payment"))
			{
				dropDownAccount.setItems(transactionController.getPayment("Dr") );
			}
					
			if(typeFlag.equals("Receipt"))
			{
				dropDownAccount.setItems(transactionController.getReceipt("Dr"));
			}
			
			if(typeFlag.equals("Purchase"))
			{
				dropDownAccount.setItems(transactionController.getPurchase("Dr"));
			}
			
			if(typeFlag.equals("PurchaseReturn"))
			{
				dropDownAccount.setItems(transactionController.getPurchaseReturn("Dr"));
			}
			
			if(typeFlag.equals("Sales"))
			{
				dropDownAccount.setItems(transactionController.getSales("Dr"));
			}
			
			if(typeFlag.equals("SalesReturn"))
			{
				dropDownAccount.setItems(transactionController.getSalesReturn("Dr"));
			}
			
			if(typeFlag.equals("DebitNote"))
			{
				dropDownAccount.setItems(transactionController.getDebitNote("Dr"));
			}
			
			if(typeFlag.equals("CreditNote"))
			{
				dropDownAccount.setItems(transactionController.getCreditNote("Dr"));
			}
			txtCrAmt.setEnabled(false);
			txtDrAmt.setEnabled(true);
			dropDownAccount.select(dropDownAccount.indexOf(transactionRow[0].toString() ));
		}
		if(transactionRow[1].toString().equals("Cr"))
		{
			dropDownDrCrFlag.select(1);
			txtCrAmt.setText(transactionRow[2].toString() );
			totalCrAmount = totalCrAmount+ Double.parseDouble(txtCrAmt.getText());
		
			
			dropDownAccount.select(dropDownAccount.indexOf(transactionRow[0].toString() )); 
			if(typeFlag.equals("Contra"))
			{
				dropDownAccount.setItems(transactionController.getContra());
		
			}
			if(typeFlag.equals("Journal")  )
			{
				dropDownAccount.setItems(transactionController.getJournal());
			}
	
			if(typeFlag.equals("Payment"))
			{
				dropDownAccount.setItems(transactionController.getPayment("Cr") );
			}
			
			if(typeFlag.equals("Receipt"))
			{
				dropDownAccount.setItems(transactionController.getReceipt("Cr"));
			}
	
			if(typeFlag.equals("Purchase"))
			{
				dropDownAccount.setItems(transactionController.getPurchase("Cr"));
			}
	
			if(typeFlag.equals("PurchaseReturn"))
			{
				dropDownAccount.setItems(transactionController.getPurchaseReturn("Cr"));
			}
	
			if(typeFlag.equals("Sales"))
			{
				dropDownAccount.setItems(transactionController.getSales("Cr"));
			}
	
			if(typeFlag.equals("SalesReturn"))
			{
				dropDownAccount.setItems(transactionController.getSalesReturn("Cr"));
			}
			
			if(typeFlag.equals("DebitNote"))
			{
				dropDownAccount.setItems(transactionController.getDebitNote("Cr"));
			}
			
			if(typeFlag.equals("CreditNote"))
			{
				dropDownAccount.setItems(transactionController.getCreditNote("Cr"));
			}
			
			
			
		txtCrAmt.setEnabled(true);
		txtDrAmt.setEnabled(false);
		dropDownAccount.select(dropDownAccount.indexOf(transactionRow[0].toString() ));
		
		
	
		}
		
		
	txtnarration.setEditable(true);
	CrDrFlags.add(dropDownDrCrFlag);
	accounts.add(dropDownAccount);
	DrAmounts.add(txtDrAmt);
	CrAmounts.add(txtCrAmt);
	//removeButton.add(btnRemove);
	
	
	/*//remove button displayed
	for(int remcounter=0;remcounter<CrDrFlags.size();remcounter++)
	{
		if(CrDrFlags.size()==2)
		{
			for(int rem=0;rem<removeButton.size();rem++)
			{
				removeButton.get(rem).setVisible(false);
			}
			
		}
		else
		{
			for(int rem=0;rem<removeButton.size();rem++)
			{
			removeButton.get(rem).setVisible(true);
			}
		}
	}
	*/
	
		
	modifyFlag = false;
	grpEditVoucher.layout();
	}
		
	private void addRow(String DrCrParam, double balanceAmount)
	{
		FormData fd=new FormData();
		
		newCrDrCombo = new Combo(grpEditVoucher, SWT.READ_ONLY);
		newCrDrCombo.add("Dr");
		newCrDrCombo.add("Cr");
		fd.left = new FormAttachment(crdrleft);
		fd.right = new FormAttachment(crdrright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newCrDrCombo.setLayoutData(fd);
		

		
		newAccountsCombo = new Combo(grpEditVoucher, SWT.READ_ONLY);
		newAccountsCombo.add("               Select              ");
		fd=new FormData();
		fd.left = new FormAttachment(accountsleft);
		fd.right = new FormAttachment(accountsright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newAccountsCombo.setLayoutData(fd);


		newTxtDrAmount = new Text(grpEditVoucher, SWT.RIGHT);
		fd=new FormData();
		fd.left = new FormAttachment(dramountleft);
		fd.right = new FormAttachment(dramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newTxtDrAmount.setLayoutData(fd);
		
		
		newTxtCrAmount = new Text(grpEditVoucher, SWT.RIGHT);
		fd=new FormData();
		fd.left = new FormAttachment(cramountleft);
		fd.right = new FormAttachment(cramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newTxtCrAmount.setLayoutData(fd);
		
		
		/*btnNewRemButton = new Button(grpEditVoucher, SWT.PUSH);
		btnNewRemButton.setText("Remove");
		fd=new FormData();
		fd.left = new FormAttachment(removeleft);
		fd.right = new FormAttachment(removeright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		
		btnNewRemButton.setLayoutData(fd);
		*/currenttop = currenttop + 8;

		
		// since new widgets are added to a row we will add them to respective
		// array list
		CrDrFlags.add(newCrDrCombo);
		accounts.add(newAccountsCombo);
		DrAmounts.add(newTxtDrAmount);
		CrAmounts.add(newTxtCrAmount);
		//removeButton.add(btnNewRemButton);
	

		if (DrCrParam.equals("Dr")) {
			// newCrDrCombo.select(0);
			CrDrFlags.get(CrDrFlags.size() - 1).select(0);
			DrAmounts.get(DrAmounts.size() -1).setSelection(0,DrAmounts.get(DrAmounts.size() -1).getText().length());
			DrAmounts.get(DrAmounts.size() - 1).setText(nf.format((balanceAmount)));
			CrAmounts.get(DrAmounts.size() - 1).setText("0.00");
			CrAmounts.get(CrAmounts.size() - 1).setEnabled(false);
			//DrAmounts.get(DrAmounts.size() - 1).setText("0.00");
			DrAmounts.get(DrAmounts.size() -1).setEnabled(true);
			if (typeFlag.equals("Payment")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getPayment(DrCrParam));
				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Receipt")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getReceipt(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Credit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getCreditNote(DrCrParam));
				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Debit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getDebitNote(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getSales(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales Return")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getSalesReturn(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Purchase")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getPurchase(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Purchase Return")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getPurchaseReturn(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			if (typeFlag.equals("Contra")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getContra());
				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			if (typeFlag.equals("Journal")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getJournal());

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
		}

		if (DrCrParam.equals("Cr")) {
			// newCrDrCombo.select(1);
			CrDrFlags.get(CrDrFlags.size() - 1).select(1);
			CrAmounts.get(CrAmounts.size() -1).setSelection(0,CrAmounts.get(CrAmounts.size() -1).getText().length());
			CrAmounts.get(CrAmounts.size() - 1).setText(nf.format((balanceAmount)));
			DrAmounts.get(DrAmounts.size() - 1).setText("0.00");
			DrAmounts.get(DrAmounts.size() - 1).setEnabled(false);
		
			
			if (typeFlag.equals("Payment")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getPayment(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));

				}
			}

			if (typeFlag.equals("Receipt")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getReceipt(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Credit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getCreditNote(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Debit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getDebitNote(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getSales(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales Return")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getSalesReturn(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Purchase")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getPurchase(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Purchase Return")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getPurchaseReturn(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			if (typeFlag.equals("Contra")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getContra());
				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			if (typeFlag.equals("Journal")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getJournal());

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			
		}
		/*for(int remcounter=0;remcounter<CrDrFlags.size();remcounter++)
		{	if(CrDrFlags.size()>2)
			{
			
			for(int rem=0;rem<removeButton.size();rem++)
			{
			removeButton.get(rem).setVisible(true);
			}
			}
		}*/
			grpEditVoucher.layout();
			setDynamicRowEvents();
			if(accounts.get(accounts.size()-1).getItemCount()== 0 )
			{
				accounts.get(accounts.size() -1).add("Please Select");
			}
				modifyFlag = false;
	}

	
	
	private void setEvents()
	{
		
			btnAddAccount.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {						
				Shell shell = new Shell();
				AddAccountPopup dialog = new AddAccountPopup(shell);
				System.out.println(dialog.open()); 
				
			if(AddAccountPopup.cancelflag.equals(true))
			{
					shell.dispose();
					holdfocuscombo.setFocus();
			}
			else
			{
				if (typeFlag.equals("Contra")) {
					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getContra());
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

					if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
					{
						holdfocuscombo.select(j);
						holdfocuscombo.setFocus();
					}
					}

				}
				if (typeFlag.equals("Journal")) {
					
					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getJournal());
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

					if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
					{
						holdfocuscombo.select(j);
						holdfocuscombo.setFocus();
					}
					}
				}
				
				if (typeFlag.equals("Payment")) {
					
					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getPayment(popupDrCr));
						
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{
						
						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}
					
				}
				if (typeFlag.equals("Receipt")) {
					
					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getReceipt(popupDrCr));
					
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}
				}
				
				if (typeFlag.equals("Credit Note")) {
					
					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getCreditNote(popupDrCr));
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}
					
				}
				if (typeFlag.equals("Debit Note")) {
					
					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getDebitNote(popupDrCr));
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}
				}
				if (typeFlag.equals("Sales")) {

					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getSales(popupDrCr));
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}
					
				}
				if (typeFlag.equals("Sales Return")) {

					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getSalesReturn(popupDrCr));
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}
					
				}
				if (typeFlag.equals("Purchase")) {
					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getPurchase(popupDrCr));
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}
				}
				if (typeFlag.equals("Purchase Return")) {

					//Code to add the newly added Account to the accounts combo box where the focus is set
					holdfocuscombo.setItems(transactionController.getPurchaseReturn(popupDrCr));
					
					//for loop to display the newly added account in the combo box where the focus is set
					for(int j = 0; j < holdfocuscombo.getItemCount();j++)
					{

						if(AddAccountPopup.newAccount.equals(holdfocuscombo.getItem(j)) )
						{
							holdfocuscombo.select(j);
							holdfocuscombo.setFocus();
						}
					}	
				}
				
			}
			holdfocuscombo.add("Please select",0 );
		}
		});
		
		btnConfirm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				//super.widgetSelected(arg0);
				if(txtyrdate.getText().trim().equals("")&& txtmdate.getText().trim().equals("")&&txtddate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter a date in DD format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtddate.setFocus();
							
						}
					});
					return;
				}
				if(txtddate.getText().trim().equals("")&&!txtmdate.getText().trim().equals("")&&!txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in dd format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtddate.setFocus();
							
						}
					});
					return;
				}
			
				if(!txtddate.getText().trim().equals("")&&!txtmdate.getText().trim().equals("")&&txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter a valid date in yyyy format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtyrdate.setFocus();
							
						}
					});
					return;
				}
				if(!txtddate.getText().trim().equals("")&&txtmdate.getText().trim().equals("")&&!txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in mm format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtmdate.setFocus();
							
						}
					});
					return;
				}
				if(!txtddate.getText().trim().equals("")&&txtmdate.getText().trim().equals("")&&txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in mm format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtmdate.setFocus();
							
						}
					});
					return;
				}
				if(txtddate.getText().trim().equals("")&&txtmdate.getText().trim().equals("")&&!txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in mm format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtddate.setFocus();
							
						}
					});
					return;
				}

				if(VoucherEditFlag==1)
				{		
					masterQueryParams.clear();
					detailQueryParams.clear();
					
					if(totalCrAmount != totalDrAmount) 
					{
						MessageBox errMsg = new MessageBox(new Shell(), SWT.ERROR| SWT.OK);
						errMsg.setMessage("Dr and Cr amounts do not tally, plese review your transaction again.");
						errMsg.open();
						if(DrAmounts.get(0).isEnabled())
						{
							DrAmounts.get(0).setFocus();
							DrAmounts.get(0).setSelection(0, DrAmounts.get(0).getText().length() );
							
							return;
						}
						if(CrAmounts.get(0).isEnabled())
						{
							CrAmounts.get(0).setFocus();
							CrAmounts.get(0).setSelection(0, CrAmounts.get(0).getText().length() );
							
							return;
						}
						
						
					}
					
					if(totalDrAmount == 0 && totalCrAmount == 0)
					{
						MessageBox errMsg = new MessageBox(new Shell(), SWT.ERROR| SWT.OK);
						errMsg.setMessage("Transaction with 0 value can't be save");
						errMsg.open();
						if(DrAmounts.get(0).isEnabled())
						{
							DrAmounts.get(0).setFocus();
							DrAmounts.get(0).setSelection(0, DrAmounts.get(0).getText().length() );
							
							return;
						}
						if(CrAmounts.get(0).isEnabled())
						{
							CrAmounts.get(0).setFocus();
							CrAmounts.get(0).setSelection(0, CrAmounts.get(0).getText().length() );
							
							return;
						}
					}
					
					
					try {
						Date voucherDate = sdf.parse(txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText());
						Date fromDate = sdf.parse(globals.session[2].toString().substring(6)+ "-" + globals.session[2].toString().substring(3,5) + "-"+ globals.session[2].toString().substring(0,2));
						Date toDate = sdf.parse(globals.session[3].toString().substring(6)+ "-" + globals.session[3].toString().substring(3,5) + "-"+ globals.session[3].toString().substring(0,2));
						
						if(voucherDate.compareTo(fromDate)< 0 || voucherDate.compareTo(toDate) > 0 )
						{
							MessageBox errMsg = new MessageBox(new Shell(),SWT.ERROR |SWT.OK );
							errMsg.setMessage("The Voucher date you entered is not within the Financialn Year");
							errMsg.open();
							txtddate.setFocus();
							return;
						}
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.getMessage();
					}
					
					for(int accountvalidation = 0;  accountvalidation < accounts.size(); accountvalidation ++)
					{
						if( accounts.get(accountvalidation).getSelectionIndex() == -1 )
						{
							MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK );
							msgaccerr .setMessage("Please select an account for completing the transaction");
							msgaccerr.open();
							accounts.get(accountvalidation).setFocus();
							editAmt=true;
							return;
							
						}
					}
					
					//all validations ok so now build master and detail query params.
					masterQueryParams.add(Integer.toString(voucherEditCode));
					masterQueryParams.add(txtyrdate.getText()+"-" + txtmdate.getText()+ "-" + txtddate.getText());
					if(comboselprj.getItemCount() >0 && comboselprj.getSelectionIndex() >= 0)
					{
						masterQueryParams.add(comboselprj.getItem(comboselprj.getSelectionIndex()));
					}
					else
					{
						masterQueryParams.add("No Project");
					}
					masterQueryParams.add(txtnarration.getText());
					for( int detailCounter = 0; detailCounter< CrDrFlags.size(); detailCounter ++)
					{
						if(CrDrFlags.get(detailCounter).getSelectionIndex()==0 && (DrAmounts.get(detailCounter).getText().trim().equals("") || Double.parseDouble( DrAmounts.get(detailCounter).getText()) == 0 ) )
						{
							continue;
						}
						if(CrDrFlags.get(detailCounter).getSelectionIndex()==1 && (CrAmounts.get(detailCounter).getText().trim().equals("") || Double.parseDouble( CrAmounts.get(detailCounter).getText()) == 0 ) )
						{
							continue;
						}

							
						String[] detailRow = new String[3];
						detailRow[0] = accounts.get(detailCounter).getItem(accounts.get(detailCounter).getSelectionIndex());
						if(!DrAmounts.get(detailCounter).getText().trim().equals("") && Double.parseDouble(DrAmounts.get(detailCounter).getText()) > 0  )
						{
							
								detailRow[1] =  DrAmounts.get(detailCounter).getText();
	
						}
						else
						{
							detailRow[1] = "0";
						}
						if(!CrAmounts.get(detailCounter).getText().trim().equals("") &&Double.parseDouble(CrAmounts.get(detailCounter).getText()) > 0  )
						{
							
								detailRow[2] = CrAmounts.get(detailCounter).getText();
								// TODO Auto-generated catch block
								//e.printStackTrace();
					
						}
						else
						{
							detailRow[2] = "0";
						}
							detailQueryParams.add(detailRow);
					}
					
					CustomDialog confirm = new CustomDialog(new Shell());
					confirm.SetMessage( "Do you wish to save ?");
					/*MessageBox	 Confirm = new MessageBox(new Shell(),SWT.NO | SWT.YES | SWT.ICON_QUESTION);
					Confirm.setMessage( "Do you wish to save the changes?");
					*/
					int answer = confirm.open();
					if( answer == SWT.YES)
					{
						if(transactionController.editTransaction(masterQueryParams, detailQueryParams))
						{
							if(ledgerDrilldown == true)
							{
								//call ledger from here.
								Composite grandParent = (Composite) btnConfirm.getParent().getParent();
								
								//reportController.showLedger(grandParent, AccountName, startDate, endDate, PN, narrationFlag, tbdrilldown, psdrilldown, tbType, selectproject);
								btnConfirm.getParent().dispose();
								if(dualledgerflag==false)
								{
								reportController.showLedger(grandParent, AccountName, startDate, endDate, PN, narrationFlag, tbdrilldown, psdrilldown, tbType, selectproject);
								}
								if(dualledgerflag==true)
								{
									reportController.showDualLedger(grandParent, AccountName,oldaccname,startDate,oldfromdate, endDate,oldenddate, PN,oldprojectname, narrationFlag,oldnarration, false,false, false,false, "","","", "",true,true);
								}
								//reportController.showLedger(grandParent, AccountName, startDate, endDate, PN, narrationFlag, tbdrilldown, psdrilldown, tbType, selectproject);
								
							
							}
							else
							{
							
							Composite grandParent = (Composite) btnConfirm.getParent().getParent();
							//VoucherTabForm.typeFlag = VoucherTabForm.typeFlag;
						    VoucherTabForm vtf = new VoucherTabForm(grandParent,SWT.NONE );
							btnConfirm.getParent().dispose();
							FindandEditVoucherComposite fdvoucher = new FindandEditVoucherComposite(vtf.tfTransaction, SWT.NONE);
							vtf.tfTransaction.setSelection(1);
							vtf.tifdrecord.setControl(fdvoucher);
							fdvoucher.combosearchRec.setFocus();
							//fdvoucher.combosearchRec.setFocus();
							vtf.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
							}
	
						}
						else
						{
							MessageBox err = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
							err.setMessage("the voucher could not be updated");
							err.open();
						}
					}
					else
					{
						txtddate.setFocus();
						txtddate.setSelection(0,2);
					}
				}	
				
				if(VoucherEditFlag==2)
				{
					masterQueryParams.clear();
					detailQueryParams.clear();
					if(totalCrAmount != totalDrAmount || totalDrAmount	 == 0 || totalCrAmount == 0) 
					{
						MessageBox errMsg = new MessageBox(new Shell(), SWT.ERROR| SWT.OK);
						errMsg.setMessage("Dr and Cr amounts do not tally, plese review your transaction again.");
						errMsg.open();
						if(DrAmounts.get(0).isEnabled())
						{
							DrAmounts.get(0).setFocus();
							return;
						}
						if(CrAmounts.get(0).isEnabled())
						{
							CrAmounts.get(0).setFocus();
							return;
						}
						
						
					}			
					
					try {
						Date voucherDate = sdf.parse(txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText());
						Date fromDate = sdf.parse(globals.session[2].toString().substring(6)+ "-" + globals.session[2].toString().substring(3,5) + "-"+ globals.session[2].toString().substring(0,2));
						Date toDate = sdf.parse(globals.session[3].toString().substring(6)+ "-" + globals.session[3].toString().substring(3,5) + "-"+ globals.session[3].toString().substring(0,2));
						
						if(voucherDate.compareTo(fromDate)< 0 || voucherDate.compareTo(toDate) > 0 )
						{
							MessageBox errMsg = new MessageBox(new Shell(),SWT.ERROR |SWT.OK );
							errMsg.setMessage("The Voucher date you entered is not within the Financialn Year");
							errMsg.open();
							txtddate.setFocus();
							txtddate.setSelection(0,2);
							return;
						}
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.getMessage();
					}
					for(int accountvalidation = 0;  accountvalidation < accounts.size(); accountvalidation ++)
					{
						if( accounts.get(accountvalidation).getSelectionIndex() == -1 )
						{
							MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK );
							msgaccerr .setMessage("Please select an account for completing the transaction");
							msgaccerr.open();
							accounts.get(accountvalidation).setFocus();
							return;
							
						}
					}
					
				//all validations ok so now build master and detail query params.
					masterQueryParams.add(txtvoucherno.getText());
					masterQueryParams.add(sdf.format(new Date()));
					masterQueryParams.add(txtyrdate.getText()+"-" + txtmdate.getText()+ "-" + txtddate.getText());
					masterQueryParams.add(typeFlag);
					if(comboselprj.getItemCount() > 0 && comboselprj.getSelectionIndex() > 0)
					{
						masterQueryParams.add(comboselprj.getItem(comboselprj.getSelectionIndex()));
					}
					else
					{
						masterQueryParams.add("No Project");
					}
					masterQueryParams.add(txtnarration.getText());
					masterQueryParams.add("");
					//masterQueryParams.add(txtPurchaseyrdate.getText()+"-"+txtPurchasemdate.getText()+"-"+txtPurchaseddate.getText());
					//masterQueryParams.add(null); 
					masterQueryParams.add(txtyrdate.getText()+"-" + txtmdate.getText()+ "-" + txtddate.getText());
					masterQueryParams.add("0.00");
					for( int detailCounter = 0; detailCounter< CrDrFlags.size(); detailCounter ++)
					{
						String[] detailRow = new String[3];
						detailRow[0] = CrDrFlags.get(detailCounter).getItem(CrDrFlags.get(detailCounter).getSelectionIndex());
						detailRow[1] = accounts.get(detailCounter).getItem(accounts.get(detailCounter).getSelectionIndex());
						if(detailRow[0].equals("Dr"))
						{
							detailRow[2] = DrAmounts.get(detailCounter).getText();    
						}
						if(detailRow[0].equals("Cr"))
						{
							detailRow[2] = CrAmounts.get(detailCounter).getText();    
						}
						detailQueryParams.add(detailRow);
					}
					
					
					CustomDialog confirm = new CustomDialog(new Shell());
					confirm.SetMessage( "Do you wish to save ?");

					int answer = confirm.open();
					if( answer == SWT.YES)
					{
						if(transactionController.setTransaction(masterQueryParams, detailQueryParams) )
						{
							
							if(ledgerDrilldown == true)
							{
								//call ledger from here.
								Composite grandParent = (Composite) btnConfirm.getParent().getParent();
							
							
								//reportController.showLedger(grandParent, AccountName, startDate, endDate, PN, narrationFlag, tbdrilldown, psdrilldown, tbType, selectproject);
								btnConfirm.getParent().dispose();
								if(dualledgerflag==false)
								{
								reportController.showLedger(grandParent, AccountName, startDate, endDate, PN, narrationFlag, tbdrilldown, psdrilldown, tbType, selectproject);
								}
								if(dualledgerflag==true)
								{
									reportController.showDualLedger(grandParent, AccountName,oldaccname,startDate,oldfromdate, endDate,oldenddate, PN,oldprojectname, narrationFlag,oldnarration, false,false, false,false, "","","", "",true,true);
								}
								//reportController.showLedger(grandParent, AccountName, startDate, endDate, PN, narrationFlag, tbdrilldown, psdrilldown, tbType, selectproject);
								
							}
							else
							{
							
							Composite grandParent = (Composite) btnConfirm.getParent().getParent();
							//VoucherTabForm.typeFlag = VoucherTabForm.typeFlag;
						    VoucherTabForm vtf = new VoucherTabForm(grandParent,SWT.NONE );
							btnConfirm.getParent().dispose();
							FindandEditVoucherComposite fdvoucher = new FindandEditVoucherComposite(vtf.tfTransaction, SWT.NONE);
							vtf.tfTransaction.setSelection(1);
							vtf.tifdrecord.setControl(fdvoucher);
							fdvoucher.combosearchRec.setFocus();
							//fdvoucher.combosearchRec.setFocus();
							vtf.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
							}
						}
						else
						{
							MessageBox err = new MessageBox(new Shell(), SWT.ICON_ERROR | SWT.OK);
							err.setMessage("the voucher could not be updated");
							err.open();
						}
					}
					else
					{
						txtvoucherno.setFocus();
						txtvoucherno.setSelection(0,4);
					}
				}
					
				
			}
		});
		
		btnBack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				//super.widgetSelected(arg0);
				
				
					if(ledgerDrilldown==true)
					{
						//call ledger from here.
						
						Composite grandParent = (Composite) btnBack.getParent().getParent();
						btnBack.getParent().dispose();
						if(dualledgerflag==false)
						{
						reportController.showLedger(grandParent, AccountName, startDate, endDate, PN, narrationFlag, tbdrilldown, psdrilldown, tbType, selectproject);
						}
						if(dualledgerflag==true)
						{
							reportController.showDualLedger(grandParent, AccountName,oldaccname,startDate,oldfromdate, endDate,oldenddate, PN,oldprojectname, narrationFlag,oldnarration, false,false, false,false, "","","", "",true,true);
						}
					
					}
					else
					{
						Composite grandParent = (Composite) btnBack.getParent().getParent();
						//VoucherTabForm.typeFlag = VoucherTabForm.typeFlag;
					    VoucherTabForm vtf = new VoucherTabForm(grandParent,SWT.NONE );
						btnConfirm.getParent().dispose();
						FindandEditVoucherComposite fdvoucher = new FindandEditVoucherComposite(vtf.tfTransaction, SWT.NONE);
						vtf.tfTransaction.setSelection(1);
						vtf.tifdrecord.setControl(fdvoucher);
						fdvoucher.combosearchRec.setFocus();
						//fdvoucher.combosearchRec.setFocus();
						vtf.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
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
					txtmdate.setFocus();
					
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
						txtmdate.setFocus();
						
					}
				
				
				

			}
		});
		


		
		txtmdate.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				
				
				if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
				{
					txtyrdate.setFocus();
					
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
				if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR ||arg0.keyCode==SWT.TAB)
				{
					grpEditVoucher.setFocus();
				}
				if(arg0.keyCode==SWT.ARROW_UP)
				{	
					txtmdate.setFocus();
				}
			}
		});
		
		txtvoucherno.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR ||arg0.keyCode==SWT.TAB)
				{
					txtddate.setFocus();
				}
			}
		});
		
		txtddate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				//super.focusLost(arg0);
				verifyFlag=false;
				if(!txtddate.getText().trim().equals("") && (Integer.valueOf(txtddate.getText())> 31 || Integer.valueOf(txtddate.getText()) <= 0) )
				{
					MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR);
					msgdateErr.setMessage("you have entered an invalid date");
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
				}
				if(!txtddate.getText().equals("") && Integer.valueOf ( txtddate.getText())<10 && txtddate.getText().length()< txtddate.getTextLimit())
				{
					txtddate.setText("0"+ txtddate.getText());
					//txtFromDtMonth.setFocus();
					return;
					
					
					
				}
				
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				verifyFlag=true;
			}
		});
		
		txtmdate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				
				// TODO Auto-generated method stub
				//super.focusLost(arg0);
				verifyFlag=false;
				if(!txtmdate.getText().trim().equals("") && (Integer.valueOf(txtmdate.getText())> 12 || Integer.valueOf(txtmdate.getText()) <= 0))
				{
					MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR);
					msgdateErr.setMessage("you have entered an invalid month, please enter it in MM format.");
					msgdateErr.open();
				
					
					Display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtmdate.setText("");
							txtmdate.setFocus();
							
						}
					});
					return;
					
				}
			
				if(! txtmdate.getText().equals("") && Integer.valueOf ( txtmdate.getText())<10 && txtmdate.getText().length()< txtmdate.getTextLimit())
				{
					txtmdate.setText("0"+ txtmdate.getText());
					return;
				}
				
				
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				verifyFlag=true;
				}
		});
		
		txtyrdate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent args0) {
				verifyFlag=false;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					Date voucherDate = sdf.parse(txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText());
					Date fromDate = sdf.parse(globals.session[2].toString().substring(6)+ "-" + globals.session[2].toString().substring(3,5) + "-"+ globals.session[2].toString().substring(0,2));
					Date toDate = sdf.parse(globals.session[3].toString().substring(6)+ "-" + globals.session[3].toString().substring(3,5) + "-"+ globals.session[3].toString().substring(0,2));
					
					if(voucherDate.compareTo(fromDate)< 0 || voucherDate.compareTo(toDate) > 0 )
					{
						MessageBox errMsg = new MessageBox(new Shell(),SWT.ERROR |SWT.OK );
						errMsg.setMessage("please enter the date within the financial year");
						errMsg.open();
						Display.getCurrent().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								txtyrdate.setText("");
								txtyrdate.setFocus();
								txtyrdate.setSelection(0,4);
							}
						});
						
						return;
					}
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.getMessage();
				}
			}
			@Override
			public void focusGained(FocusEvent arg0) {
			verifyFlag=true;	
			}
		});
		

		txtyrdate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				// TODO Auto-generated method stub
				//super.focusLost(arg0);
				if(!txtyrdate.getText().trim().equals("") && Integer.valueOf(txtyrdate.getText()) < 0000) 
						{
					MessageBox msgbox = new MessageBox(new Shell(), SWT.OK |SWT.ERROR);
					msgbox.setMessage("you have entered an invalid year");
					msgbox.open();
				
					txtyrdate.setText("");
					Display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtyrdate.setFocus();
							txtyrdate.setText("");
							
						}
					});
					
				}
				
				if(txtyrdate.getText().trim().equals("")&& txtmdate.getText().trim().equals("")&&txtddate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter a date in DD format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtddate.setFocus();
							
						}
					});
					return;
				}
				if(txtddate.getText().trim().equals("")&&!txtmdate.getText().trim().equals("")&&!txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in dd format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtddate.setFocus();
							
						}
					});
					return;
				}
			
				if(!txtddate.getText().trim().equals("")&&!txtmdate.getText().trim().equals("")&&txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter a valid date in yyyy format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtyrdate.setFocus();
							
						}
					});
					return;
				}
				if(!txtddate.getText().trim().equals("")&&txtmdate.getText().trim().equals("")&&!txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in mm format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtmdate.setFocus();
							
						}
					});
					return;
				}
				if(!txtddate.getText().trim().equals("")&&txtmdate.getText().trim().equals("")&&txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in mm format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtmdate.setFocus();
							
						}
					});
					return;
				}
				if(txtddate.getText().trim().equals("")&&txtmdate.getText().trim().equals("")&&!txtyrdate.getText().trim().equals(""))
				{
					MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
					msgDayErr.setMessage("Please enter valid date in mm format.");
					msgDayErr.open();
					display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtddate.setFocus();
							
						}
					});
					return;
				}
			
			}
		});



		txtddate.addVerifyListener(new VerifyListener() {
			
			@Override
			
			public void verifyText(VerifyEvent arg0) {
				// TODO Auto-generated method stub
				if(verifyFlag== false)
				{
					arg0.doit= true;
					return;
				}
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
	                return;
				}
		if(arg0.keyCode == 46)
		{
			return;
		}
		if(arg0.keyCode == 45||arg0.keyCode == 62)
		{
			  arg0.doit = false;
			
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
				if(verifyFlag== false)
				{
					arg0.doit= true;
					return;
				}
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
	                return;
				}
		if(arg0.keyCode == 46)
		{
			return;
		}
		if(arg0.keyCode == 45||arg0.keyCode == 62)
		{
			  arg0.doit = false;
			
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
				if(verifyFlag== false)
				{
					arg0.doit= true;
					return;
				}
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
	                return;
				}
		if(arg0.keyCode == 46)
		{
			return;
		}
		if(arg0.keyCode == 45||arg0.keyCode == 62)
		{
			  arg0.doit = false;
			
		}
	        if (!Character.isDigit(arg0.character)) {
	            arg0.doit = false;  // disallow the action
	        }
	        
			}
		});
	
	
		comboselprj.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
			//	super.keyPressed(arg0);
				if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
				{
					txtnarration.setFocus();
					
				}
				if(arg0.keyCode==SWT.ARROW_UP && comboselprj.getSelectionIndex()==0)
				{
					if(DrAmounts.get(DrAmounts.size()-1).getEnabled())
					{
						DrAmounts.get(DrAmounts.size()-1).setFocus();
						DrAmounts.get(DrAmounts.size()-1).setSelection(0, DrAmounts.get(DrAmounts.size()-1).getText().length() );
						
					}
					else
					{
						CrAmounts.get(CrAmounts.size()-1).setFocus();
						CrAmounts.get(CrAmounts.size()-1).setSelection(0, CrAmounts.get(CrAmounts.size()-1).getText().length() );
						
					}
					
				}		
			}
		});
		
		txtnarration.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method{} stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
				{
					btnConfirm.setFocus();
					
				}
				if(arg0.keyCode==SWT.ARROW_UP)
				{
					if(comboselprj.isVisible())
					{
						comboselprj.setFocus();
						
					}
					else
					{
						if(DrAmounts.get(DrAmounts.size()-1).getEnabled())
						{
							DrAmounts.get(DrAmounts.size()-1).setFocus();
						}
						else
						{
							CrAmounts.get(CrAmounts.size()-1).setFocus();
						}
					}
				}
		
			}
		});
		
		btnConfirm.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method{} stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.ARROW_RIGHT)
				{
					btnBack.setFocus();
					
				}
				if(arg0.keyCode==SWT.ARROW_UP)
				{
						txtnarration.setFocus();
				}
					
			}
		});
		
		btnBack.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.ARROW_LEFT)
				{
					btnConfirm.setFocus();
				}
				if(arg0.keyCode==SWT.ARROW_RIGHT)
				{
					btnAddAccount.setFocus();
				}
			}
		});
		
		/*btnAddAccount.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0);
				if(arg0.keyCode==SWT.ARROW_LEFT)
				{
					btnBack.setFocus();
				}
			}
		});*/
		
		setDynamicRowEvents();		
		

	}
	
	private void setDynamicRowEvents()
	{
		for(int rowcounter = startFrom ; rowcounter < CrDrFlags.size(); rowcounter ++ )
		{
			
			CrDrFlags.get(rowcounter).setData("curindex", rowcounter);
			accounts.get(rowcounter).setData("curindex",rowcounter);
			DrAmounts.get(rowcounter).setData("curindex", rowcounter);
			CrAmounts.get(rowcounter).setData("curindex", rowcounter);
			//removeButton.get(rowcounter).setData("curindex", rowcounter);
			System.out.println(CrAmounts.get(rowcounter).getData("curindex").toString() );
			CrDrFlags.get(rowcounter).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					//super.widgetSelected(arg0);
					Combo currentCRDRFlag = (Combo) arg0.widget;
					String DrCrFlag =  currentCRDRFlag.getItem(currentCRDRFlag.getSelectionIndex());

					int rowindex = (Integer) currentCRDRFlag.getData("curindex");
					accounts.get(rowindex).removeAll();
					
				
					CrAmounts.get(rowindex).setSelection(0,CrAmounts.get(rowindex).getText().length());
					
					DrAmounts.get(rowindex).setSelection(0,DrAmounts.get(rowindex).getText().length());
					
					if (DrCrFlag.equals("Dr")&& rowindex ==0 ) {
						CrDrFlags.get(rowindex +1).select(1);
						CrDrFlags.get(rowindex +1).notifyListeners(SWT.Selection, new Event());
						
						DrAmounts.get(rowindex).setEnabled(true);
						CrAmounts.get(rowindex).setEnabled(false);
						CrAmounts.get(rowindex +1).setEnabled(true);
						DrAmounts.get(rowindex +1).setEnabled(false);
						DrAmounts.get(rowindex).setSelection(rowindex,DrAmounts.get(rowindex).getText().length());
						CrAmounts.get(rowindex).setText("0.00");
//						CrAmounts.get(rowindex).setEnabled(false);	
						lblTotalDrAmt.setText("0.00");
						lblTotalCrAmt.setText("0.00");
					}
					if (DrCrFlag.equals("Cr")&& rowindex ==0) {
						CrDrFlags.get(rowindex+1).select(0);
						CrDrFlags.get(rowindex +1).notifyListeners(SWT.Selection, new Event());
						CrAmounts.get(rowindex).setEnabled(true);
						CrAmounts.get(rowindex).setText("0.00");
						DrAmounts.get(rowindex).setEnabled(false);
						CrAmounts.get(rowindex+1).setEnabled(false);
						DrAmounts.get(rowindex+1).setEnabled(true);
						DrAmounts.get(rowindex+1).setText("0.00");
						CrAmounts.get(rowindex).setSelection(rowindex,CrAmounts.get(rowindex).getText().length());
						DrAmounts.get(rowindex).setText("0.00");
						//CrAmounts.get(rowindex+1).setText("0.00");
						//DrAmounts.get(rowindex).setEnabled(false);
						DrAmounts.get(rowindex).setSelection(0, DrAmounts.get(rowindex).getText().length());
						lblTotalDrAmt.setText("0.00");
						lblTotalCrAmt.setText("0.00");
					}
					
					if (DrCrFlag.equals("Dr")&& rowindex > 0 ) {
						
						DrAmounts.get(rowindex).setEnabled(true);
						CrAmounts.get(rowindex).setEnabled(false);
						DrAmounts.get(rowindex).setSelection(rowindex,DrAmounts.get(rowindex).getText().length());
						CrAmounts.get(rowindex).setText("0.00");
		
					}
					if (DrCrFlag.equals("Cr")&& rowindex >0) {
						
						CrAmounts.get(rowindex).setEnabled(true);
						DrAmounts.get(rowindex).setEnabled(false);
						CrAmounts.get(rowindex).setSelection(rowindex,CrAmounts.get(rowindex).getText().length());
						DrAmounts.get(rowindex).setText("0.00");
						DrAmounts.get(rowindex).setSelection(0, DrAmounts.get(rowindex).getText().length());
					}
					
					editAmt=true;
					if (typeFlag.equals("Payment"))
					{
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getPayment(DrCrFlag));
						for (int i = 0; i < finalAccounts.size(); i++) 
						{
							accounts.get(rowindex).add(finalAccounts.get(i));
						}

					}
					
					if (typeFlag.equals("Receipt")) 
					{
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getReceipt(DrCrFlag));
						
						for (int i = 0; i < finalAccounts.size(); i++) 
						{
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}
					if (typeFlag.equals("Debit Note")) {
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getDebitNote(DrCrFlag));

						for (int i = 0; i < finalAccounts.size(); i++) {
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}

					if (typeFlag.equals("Sales")) {
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getSales(DrCrFlag));

						for (int i = 0; i < finalAccounts.size(); i++) {
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}
					if (typeFlag.equals("Sales Return")) {
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getSalesReturn(DrCrFlag));

						for (int i = 0; i < finalAccounts.size(); i++) {
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}

					if (typeFlag.equals("Purchase")) {
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getPurchase(DrCrFlag));

						for (int i = 0; i < finalAccounts.size(); i++) {
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}
					
					if (typeFlag.equals("Purchase Return")) {
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getPurchaseReturn(DrCrFlag));

						for (int i = 0; i < finalAccounts.size(); i++) {
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}
					if (typeFlag.equals("Contra")) {
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getContra());
						for (int i = 0; i < finalAccounts.size(); i++) {
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}
					if (typeFlag.equals("Journal")) {
						List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getJournal());

						for (int i = 0; i < finalAccounts.size(); i++) {
							accounts.get(rowindex).add(finalAccounts.get(i));
						}
					}	
				}
			});
			
			accounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0){

					holdfocuscombo = (Combo) arg0.widget;
					//btnAddAccount.setVisible(true);
					final int rowindex = (Integer) holdfocuscombo.getData("curindex");
					popupDrCr = CrDrFlags.get(rowindex).getItem(CrDrFlags.get(rowindex).getSelectionIndex());
					
					btnAddAccount.setEnabled(true);
				}
			});
			
			DrAmounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0){
					verifyFlag=true;
					btnAddAccount.setEnabled(false);
				}
				@Override
				public void focusLost(FocusEvent arg0){
					verifyFlag=false;
					
				}
				
			});
			
			CrAmounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0){
					verifyFlag=true;
					btnAddAccount.setEnabled(false);
				}
				@Override
				public void focusLost(FocusEvent arg0){
					
					verifyFlag=false;
				}
			});
			
		
			accounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0) {
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					Combo currentAccount = (Combo) arg0.widget;
					int rowindex = (Integer) currentAccount.getData("curindex");
					if(DrAmounts.get(rowindex).getEnabled())
					{
						DrAmounts.get(rowindex).setFocus();
						DrAmounts.get(rowindex).setSelection(0, DrAmounts.get(rowindex).getText().length());
						try {
							DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(CrAmounts.get(rowindex).getEnabled())
					{
						CrAmounts.get(rowindex).setFocus();
						CrAmounts.get(rowindex).setSelection(0, CrAmounts.get(rowindex).getText().length());
						try {
							CrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(CrAmounts.get(rowindex).getText())));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			
			DrAmounts.get(rowcounter).addModifyListener(new ModifyListener() {
	
				@Override
				public void modifyText(ModifyEvent arg0) {
					// TODO Auto-generated method stub
					modifyFlag = true;
		
				}
			} );
			
			DrAmounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				//editAmt flag for editing amt
				@Override
				public void focusGained(FocusEvent arg0) {
					
					Text currentDr = (Text) arg0.widget;
					final int rowindex = (Integer) currentDr.getData("curindex");
					oldValue = Double.parseDouble(currentDr.getText());
					verifyFlag = true;
					
					
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					
					verifyFlag = false;
					Text currentDr = (Text) arg0.widget;
					final int rowindex = (Integer) currentDr.getData("curindex");

					if(DrAmounts.get(rowindex).getText().trim().equals("")  )
					{
						DrAmounts.get(rowindex).setText("0.00");
					}	
					//double newValue = Double.parseDouble(currentDr.getText());
					//if(oldValue != newValue )
					//{
						//editAmt = true;
						DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
						
					//}
					/*if(oldValue == newValue)
					{
						editAmt = false;			//no editing done
					}*/
					
				
		//			if(!editAmt || (rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					if((rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					{
						lblTotal.dispose();
						lblTotalDrAmt.dispose();
						lblTotalCrAmt.dispose();
						//grpEditVoucher.pack();
						totalDrAmount=0.00;
						totalCrAmount=0.00;
						for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
						{
							try {
								totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						
						for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
						{
							try {
								totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						totalRow();
						//grpEditVoucher.pack();
					
						return;
					}
					

					
					if(rowindex == 0 || rowindex < accounts.size()-1)
					{
						DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
						//CrAmounts.get(rowindex +1).setText(DrAmounts.get(rowindex).getText());	
						verifyFlag = true;
						totalDrAmount=Double.parseDouble(DrAmounts.get(rowindex).getText());
						
						if(totalCrAmount>totalDrAmount)
						{
							double balanceAmount=totalCrAmount-totalDrAmount;
							totalDrAmount=totalDrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							//addRow("Dr", balanceAmount);
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex+1).setFocus();
								}
							});
						}
						
						if(totalDrAmount>totalCrAmount)
						{
							
							double balanceAmount=totalDrAmount-totalCrAmount;
							totalCrAmount=totalCrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							//addRow("Cr", balanceAmount);
							//grpEditVoucher.pack();
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex+1).setFocus();
									
								}
							});
						}
						
						if(totalRowCalled)
						{
							
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							totalDrAmount=0.00;
							totalCrAmount=0.00;
							for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
							{
								try {
									totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							
							for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
							{
								try {
									totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							totalRow();
							//grpEditVoucher.pack();	
						}
						else
						{

							totalRow();
							//grpEditVoucher.pack();
						}
						
						
						//CrDrFlags.get(rowindex+1).select(1);
						return;
					}
					
					if(rowindex == accounts.size()-1)
					{
						
						
						verifyFlag = true;
						
						if(DrAmounts.get(rowindex).getText().trim().equals("")  )
						{
							DrAmounts.get(rowindex).setText("0.00");
						}	
						DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
						
						totalDrAmount=0.00;
						totalCrAmount=0.00;
						for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
						{
							try {
								totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}						
						for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
						{
							try {
								totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						if(totalCrAmount==totalDrAmount)
						{
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							totalRow();
							//grpEditVoucher.pack();
							
								
							if(comboselprj.getItemCount()==0)
							{
								display.getCurrent().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
						 				txtnarration.setFocus();
										
									}
								});
							}
							else
							{
								display.getCurrent().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										comboselprj.setFocus();
									}
								});
							}
						}
						
						if(totalCrAmount>totalDrAmount)
						{
							double balanceAmount=totalCrAmount-totalDrAmount;
							totalDrAmount=totalDrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							addRow("Dr", balanceAmount);
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex+1).setFocus();
								}
							});
						}
						
						if(totalDrAmount>totalCrAmount)
						{
							
							double balanceAmount=totalDrAmount-totalCrAmount;
							totalCrAmount=totalCrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							addRow("Cr", balanceAmount);
							//grpEditVoucher.pack();
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex+1).setFocus();
									
								}
							});
						}
					}
				}
			});

			CrAmounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				//editAmt flag for editing
				@Override
				public void focusGained(FocusEvent arg0) {
					Text currentCr = (Text) arg0.widget;
					final int rowindex = (Integer) currentCr.getData("curindex");
					oldValue = Double.parseDouble(currentCr.getText());
					verifyFlag = true;

					
				}
				@Override
				public void focusLost(FocusEvent arg0) {
					verifyFlag = false;
					Text currentCr = (Text) arg0.widget;
					final int rowindex = (Integer) currentCr.getData("curindex");
					
					if(CrAmounts.get(rowindex).getText().trim().equals("")  )
					{
						CrAmounts.get(rowindex).setText("0.00");
							
					}
					//double newValue = Double.parseDouble(currentCr.getText());
					//if(oldValue != newValue )
					//{
						//editAmt = true;
						CrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(CrAmounts.get(rowindex).getText())));
						
						
					//}
					/*if(oldValue == newValue)
					{
						editAmt = false;			//no editing done
					}*/
					
				
					
					//if(!editAmt || (rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					if((rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					{
						lblTotal.dispose();
						lblTotalDrAmt.dispose();
						lblTotalCrAmt.dispose();
						//grpEditVoucher.pack();
						totalDrAmount=0.00;
						totalCrAmount=0.00;
						for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
						{
							try {
								totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						
						for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
						{
							try {
								totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						totalRow();
						//grpEditVoucher.pack();
					
						return;
					}
					
					

					
					if(rowindex == 0 || rowindex < accounts.size()-1)
					{
					
						CrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(CrAmounts.get(rowindex).getText())));
						verifyFlag = true;
						totalCrAmount=Double.parseDouble(CrAmounts.get(rowindex).getText());
						
						if(totalCrAmount>totalDrAmount)
						{
							double balanceAmount=totalCrAmount-totalDrAmount;
							totalDrAmount=totalDrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							//addRow("Dr", balanceAmount);
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex+1).setFocus();
								}
							});
						}
						
						if(totalDrAmount>totalCrAmount)
						{
							double balanceAmount=totalDrAmount-totalCrAmount;
							totalCrAmount=totalCrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							//addRow("Cr",balanceAmount);
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex+1).setFocus();
									
								}
							});
						}	
						
						if(totalRowCalled)
						{
							
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							totalDrAmount=0.00;
							totalCrAmount=0.00;
							for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
							{
								try {
									totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							
							for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
							{
								try {
									totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							totalRow();
							//grpEditVoucher.pack();
							
							
						}
						else
						{
							
							totalRow();
							//grpEditVoucher.pack();
						}
						
					}
					
					if(rowindex == accounts.size()-1)
					{
						
						
						verifyFlag = true;
						
						if(CrAmounts.get(rowindex).getText().trim().equals("")  )
						{
							CrAmounts.get(rowindex).setText("0.00");
								
						}
						CrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(CrAmounts.get(rowindex).getText())));
						
						totalDrAmount=0.00;
						totalCrAmount=0.00;
						for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
						{
							try {
								totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						
						for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
						{
							try {
								totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						if(totalCrAmount==totalDrAmount)
						{
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							totalRow();
							//grpEditVoucher.pack();
							
							if(comboselprj.getItemCount()==0)
							{
								display.getCurrent().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										txtnarration.setFocus();
										
									}
								});
							}
							else
							{
								display.getCurrent().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										comboselprj.setFocus();
									}
								});
							}
						}
						
						if(totalCrAmount>totalDrAmount)
						{
							
							double balanceAmount=totalCrAmount-totalDrAmount;
							totalDrAmount=totalDrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							addRow("Dr", balanceAmount);
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex+1).setFocus();
									
								}
							});
						}
						
						if(totalDrAmount>totalCrAmount)
						{
							double balanceAmount=totalDrAmount-totalCrAmount;
							totalCrAmount=totalCrAmount+balanceAmount;
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpEditVoucher.pack();
							addRow("Cr", balanceAmount);
							totalRow();
							//grpEditVoucher.pack();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrDrFlags.get(rowindex +1).setFocus(); 
									
								}
							});
						}
						//return;
					}
				}
			});
			
			CrDrFlags.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
				//	super.keyPressed(arg0);
					Combo currentCrDr=(Combo) arg0.widget;
					String DrCrFlag=currentCrDr.getItem(currentCrDr.getSelectionIndex());
					int rowindex=(Integer) currentCrDr.getData("curindex");
					//int rowkey = Character.getNumericValue(arg0.character);
					//MessageBox	 msg = new MessageBox(new Shell(), SWT.OK);
					//msg.setMessage(Integer.toString(rowkey ));
					//msg.open();
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'p') && (rowindex >0 ))
					{
						CrDrFlags.get(rowindex -1).setFocus();
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'n') && (rowindex < CrDrFlags.size() -1 ) )
					{
						CrDrFlags.get(rowindex +1).setFocus();
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'f'))
					{
						CrDrFlags.get(0).setFocus();
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'l'))
					{
						CrDrFlags.get(CrDrFlags.size()-1).setFocus();
						return;
					}
					
					if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == '.'))
					{
						
						accounts.get(rowindex).setFocus();
						arg0.doit=false;
						return;
					}

					if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
					{
						//CrDrFlags.get(rowindex).notifyListeners(SWT.Selection, new Event());
						accounts.get(rowindex).setFocus();
						
					}
					if(arg0.keyCode==SWT.ARROW_UP && rowindex==0 && CrDrFlags.get(rowindex).getSelectionIndex()  ==0 )
					{
							txtyrdate.setFocus();
							txtyrdate.setSelection(0,4);
						
					}
					if(arg0.keyCode== SWT.ARROW_UP && rowindex>0 && CrDrFlags.get(rowindex).getSelectionIndex()  ==0)
					{
						if(CrAmounts.get(rowindex -1 ).getEnabled())
						{
							CrAmounts.get(rowindex -1).setFocus();
						}
						else
						{
							DrAmounts.get(rowindex -1).setFocus();
						}
					}
					/*if(arg0.keyCode==SWT.DEL)
					{
						removeButton.get(rowindex).notifyListeners(SWT.Selection, new Event());
					}		*/	
				}			
			});

			
			
			
		
			accounts.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					Combo currentAccount=(Combo) arg0.widget;
				
					int rowindex=(Integer) currentAccount.getData("curindex");
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'p')&& (rowindex > 0))
					{
						accounts.get(rowindex -1).setFocus();
						return;
					}
					/*if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'n') && (rowindex < removeButton.size() -1 ))
					{
						accounts.get(rowindex +1).setFocus();
						return;
					}
					*/
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'f'))
					{
						accounts.get(0).setFocus();
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'l'))
					{
						accounts.get(CrDrFlags.size()-1).setFocus();
						return;
					}
					if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == ','))
					{
						CrDrFlags.get(rowindex).setFocus();
						arg0.doit=false;
						return;
							
					}
					if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == '.'))
					{
					
							
							if(CrAmounts.get(rowindex).getEnabled())
							{
								CrAmounts.get(rowindex).setFocus();
								arg0.doit=false;
								return;
							}
							if(DrAmounts.get(rowindex).getEnabled())
							{
								DrAmounts.get(rowindex).setFocus();
								arg0.doit=false;
								return;
							}		
					}
						

					
					if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
					{
						if(DrAmounts.get(rowindex).getEnabled()==true)
						{	
							DrAmounts.get(rowindex).setFocus();
						}
						if(CrAmounts.get(rowindex).getEnabled()==true)
						{
							CrAmounts.get(rowindex).setFocus();
						}
					}
					if(arg0.keyCode==SWT.ARROW_UP && rowindex==0 && accounts.get(rowindex).getSelectionIndex()==0)
					{	
						CrDrFlags.get(rowindex).setFocus();
					}
					if(arg0.keyCode== SWT.ARROW_UP && rowindex>0 && accounts.get(rowindex).getSelectionIndex()==0)
					{
						CrDrFlags.get(rowindex).setFocus();
					}
					/*if(arg0.keyCode==SWT.DEL)
					{
						removeButton.get(rowindex).notifyListeners(SWT.Selection, new Event());
					}
			*/	}
			});
				
			DrAmounts.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					
					Text currentDr=(Text) arg0.widget;
					int rowindex=(Integer) currentDr.getData("curindex");
					
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'p') && (rowindex >0 ))
					{
						
						if(DrAmounts.get(rowindex -1).getEnabled())
						{
							DrAmounts.get(rowindex -1).setFocus();
						}
						if(CrAmounts.get(rowindex-1).getEnabled())
						{
							CrAmounts.get(rowindex -1).setFocus();
						}
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'n')&& (rowindex < DrAmounts.size() -1 ))
					{
						
						if(DrAmounts.get(rowindex +1).getEnabled())
						{
							DrAmounts.get(rowindex +1).setFocus();
						}
						if(CrAmounts.get(rowindex+1).getEnabled())
						{
							CrAmounts.get(rowindex +1).setFocus();
						}
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'f'))
					{
						
						if(DrAmounts.get(0).getEnabled())
						{
							DrAmounts.get(0).setFocus();
							
						}
						if(CrAmounts.get(0).getEnabled())
						{
							CrAmounts.get(0).setFocus();
							
						}
					
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'l'))
					{
						
						
						if(DrAmounts.get(CrDrFlags.size()-1).getEnabled())
						{
							DrAmounts.get(DrAmounts.size()-1).getEnabled();
							
						}
						if(CrAmounts.get(CrDrFlags.size()-1).getEnabled())
						{
							CrAmounts.get(CrAmounts.size()-1).getEnabled();
							
						}
						return;
					}
					/*if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == '.'))
					{
							
							removeButton.get(rowindex).setFocus();
							arg0.doit=false;
							return;
							
										}*/
					if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == ','))
					{
						
						accounts.get(rowindex).setFocus();
						arg0.doit=false;
						return;
					}
					if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR && rowindex	  == 0)
					{
						
						CrDrFlags.get(rowindex+1).setFocus();
					}
					if(arg0.keyCode== SWT.CR | arg0.keyCode==SWT.KEYPAD_CR && rowindex == CrDrFlags.size()-1)
					{	
						if(comboselprj.isVisible())
						{
							comboselprj.setFocus();
						}
						else
						{
						   	txtnarration.setFocus();
					    }
					}
					
					if(arg0.keyCode== SWT.CR | arg0.keyCode==SWT.KEYPAD_CR && rowindex < CrDrFlags.size()-1)
					{
						if(CrDrFlags.get(rowindex+1).getEnabled()==true)
						{
							CrDrFlags.get(rowindex+1).setFocus();
						}
					}
				
					if(arg0.keyCode==SWT.ARROW_UP)
					{	
						
						accounts.get(rowindex).setFocus();
					}
					/*if(arg0.keyCode==SWT.DEL)
					{
						removeButton.get(rowindex).notifyListeners(SWT.Selection, new Event());
					}*/
				}
			});
			
			CrAmounts.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					Text currentCr=(Text) arg0.widget;
					int rowindex=(Integer) currentCr.getData("curindex");
					
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'p')&& (rowindex > 0))
					{
						
						if(DrAmounts.get(rowindex -1).getEnabled())
						{
							DrAmounts.get(rowindex -1).setFocus();
						}
						if(CrAmounts.get(rowindex-1).getEnabled())
						{
							CrAmounts.get(rowindex -1).setFocus();
						}
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'n')&& (rowindex < CrAmounts.size() -1 ))
					{
						
						if(DrAmounts.get(rowindex +1).getEnabled())
						{
							DrAmounts.get(rowindex +1).setFocus();
						}
						if(CrAmounts.get(rowindex+1).getEnabled())
						{
							CrAmounts.get(rowindex +1).setFocus();
						}
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'f'))
					{
						
						if(DrAmounts.get(0).getEnabled())
						{
							DrAmounts.get(0).setFocus();
						}
						if(CrAmounts.get(0).getEnabled())
						{
							CrAmounts.get(0).setFocus();
						}
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'l'))
					{
						
						
						if(DrAmounts.get(DrAmounts.size()-1).getEnabled())
						{
							DrAmounts.get(DrAmounts.size()-1).getEnabled();
						}
						if(CrAmounts.get(rowindex+1).getEnabled())
						{
							CrAmounts.get(CrAmounts.size()-1).getEnabled();
						}
						return;
					}
					
					//right side
					/*if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == '.'))
					{
						
							removeButton.get(rowindex).setFocus();
							arg0.doit=false;
							return;
					}*/
					//left side
					if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == ','))
					{
						
							accounts.get(rowindex).setFocus();
							arg0.doit=false;
							return;
					}
					
					if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR && rowindex ==0)
					{
						
						CrDrFlags.get(rowindex+1).setFocus();
						System.out.println(Integer.toString(comboselprj.getItemCount()));
					}
					if(arg0.keyCode== SWT.CR | arg0.keyCode==SWT.KEYPAD_CR && rowindex == CrDrFlags.size()-1)
					{	
						if(comboselprj.isVisible())
						{
							comboselprj.setFocus();
						}
						else
						{
						   	txtnarration.setFocus();
					    }
					}
					
					if(arg0.keyCode== SWT.CR | arg0.keyCode==SWT.KEYPAD_CR && rowindex < CrDrFlags.size()-1)
					{
						if(CrDrFlags.get(rowindex+1).getEnabled()==true)
						{
							CrDrFlags.get(rowindex+1).setFocus();
						}
					}

					
					
					if(arg0.keyCode==SWT.ARROW_UP)
					{
						
						accounts.get(rowindex).setFocus();
					}
					/*if(arg0.keyCode==SWT.DEL)
					{
						removeButton.get(rowindex).notifyListeners(SWT.Selection, new Event());
					}
	*/			}
			});

			/*removeButton.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
				//	super.keyPressed(arg0);
					
					Button currentRemove=(Button) arg0.widget;
					//	String DrCrFlag=currentAccount.getItem(current.getSelectionIndex());
						int rowindex=(Integer) currentRemove.getData("curindex");
						
						if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'p')&& (rowindex > 0))
						{
							removeButton.get(rowindex -1).setFocus();
							return;
						}
						if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'n') && (rowindex < removeButton.size() -1 ))
						{
							removeButton.get(rowindex +1).setFocus();
							return;
						}
		
						if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'f'))
						{
							removeButton.get(0).setFocus();
							return;
						}
						if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'l'))
						{
							removeButton.get(CrDrFlags.size()-1).setFocus();
							return;
						}
						if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == ','))
						{
							if(CrAmounts.get(rowindex).getEnabled())
							{
								CrAmounts.get(rowindex).setFocus();
								arg0.doit=false;
								return;
							}
							if(DrAmounts.get(rowindex).getEnabled())
							{
								DrAmounts.get(rowindex).setFocus();
								arg0.doit=false;
								return;
							}
		
				
						}
			}		
			});
*/
	
			DrAmounts.get(rowcounter).addVerifyListener(new VerifyListener() {
				
				@Override
				public void verifyText(VerifyEvent arg0) {
					// TODO Auto-generated method stub
					if(verifyFlag== false)
					{
						arg0.doit= true;
						return;
					}
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
		                return;
					}
			if(arg0.keyCode == 46)
			{
				return;
			}
			if(arg0.keyCode == 45||arg0.keyCode == 62)
			{
				return;
			}
			
		        if (!Character.isDigit(arg0.character)) {
		            arg0.doit = false;  // disallow the action
		        }
		        
				}
			});
			
			CrAmounts.get(rowcounter).addVerifyListener(new VerifyListener() {
				
				@Override
				public void verifyText(VerifyEvent arg0) {
					// TODO Auto-generated method stub
					if(verifyFlag== false)
					{
						arg0.doit= true;
						return;
					}
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
		                return;
		        }
					if(arg0.keyCode==46)
					{
						return;
					}
					if(arg0.keyCode==45||arg0.keyCode == 62)
					{
						arg0.doit= false;
					}
		        if (!Character.isDigit(arg0.character)) {
		            arg0.doit = false;  // disallow the action
		        }

				}
			});

			
		/*removeButton.get(rowcounter).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					//super.widgetSelected(arg0);
					
					if(CrDrFlags.size()<=2)
					{
						return;
					}
					Button btncurrentremove = (Button) arg0.widget;
					int rowindex =(Integer)  btncurrentremove.getData("curindex");
					if(rowindex==0 )
					{
						if(CrDrFlags.size()==1)
						{
							txtvoucherno.setFocus();
						}
						//CrDrFlags.get(rowindex +1).setFocus();
						
					}
					if(rowindex > 0 )
					{
						CrDrFlags.get(rowindex -1).setFocus();
						
					}
				
					CrDrFlags.get(rowindex).dispose();
					CrDrFlags.remove(rowindex);
					accounts.get(rowindex).dispose();
					accounts.remove(rowindex);
					DrAmounts.get(rowindex).dispose();
					DrAmounts.remove(rowindex);
					CrAmounts.get(rowindex).dispose();
					CrAmounts.remove(rowindex);
					removeButton.get(rowindex).dispose();
					removeButton.remove(rowindex);
					
					for(int reset =rowindex; reset < CrDrFlags.size(); reset ++ )
					{
						CrDrFlags.get(reset).setData("curindex", reset );
						accounts.get(reset).setData("curindex", reset );
						DrAmounts.get(reset).setData("curindex", reset );
						CrAmounts.get(reset).setData("curindex", reset );
						//AddRow.get(reset).setData("curindex", reset);
						removeButton.get(reset).setData("curindex", reset );
						
						
					}
					startFrom --;
					
					totalDrAmount=0.00;
					totalCrAmount=0.00;
					for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
					{
						try {
							totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
					{
						try {
							totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					lblTotal.dispose();
					lblTotalDrAmt.dispose();
					lblTotalCrAmt.dispose();
					totalRow();
				//grpEditVoucher.pack();
				if(CrDrFlags.size()==2)
				{
					removeButton.get(0).setVisible(false);
					removeButton.get(1).setVisible(false);
					
				}
			
				}
				
			} );
*/	

			
			
		}
		startFrom	 = CrDrFlags.size();
	}


	private void totalRow() {
		
	FormData tfd = new FormData();
		
		lblTotal= new Label(grpEditVoucher, SWT.CENTER);
		lblTotal.setText("Total");
		lblTotal.setFont(new Font(display, "Times New Roman",14,SWT.CENTER));
		tfd.top = new FormAttachment(currenttop);
		tfd.left = new FormAttachment(crdrleft);
		tfd.right = new FormAttachment(accountsright);
		tfd.bottom = new FormAttachment(currenttop + incrementby);
		lblTotal.setLayoutData(tfd);
		
		
		lblTotalDrAmt = new Label(grpEditVoucher,SWT.BORDER| SWT.RIGHT);
		lblTotalDrAmt.setText( nf.format(totalDrAmount));
		tfd = new FormData();
		tfd.top = new FormAttachment(currenttop);
		tfd.left = new FormAttachment(dramountleft);
		tfd.right = new FormAttachment(dramountright);
		tfd.bottom = new FormAttachment(currenttop + incrementby);
    	lblTotalDrAmt.setLayoutData(tfd);
		
		lblTotalCrAmt = new Label(grpEditVoucher,SWT.BORDER | SWT.RIGHT);
		lblTotalCrAmt.setText( nf.format(totalCrAmount));
		tfd = new FormData();
		//gd.horizontalAlignment= SWT.CENTER;
		tfd.top = new FormAttachment(currenttop);
		tfd.left = new FormAttachment(cramountleft);
		tfd.right = new FormAttachment(cramountright);
		tfd.bottom = new FormAttachment(currenttop + incrementby);
		lblTotalCrAmt.setLayoutData(tfd);
		//grpVoucher.pack();
		grpEditVoucher.layout();
	
		
		totalRowCalled= true;
}

	public void makeaccssible(Control c)
	{
		c.getAccessible();
	}

	private ArrayList<String> getFilteredAccountList(String[] OrigList) {
		ArrayList<String> filterAccounts = new ArrayList<String>();

		for (int i = 0; i < OrigList.length; i++) {
			filterAccounts.add(OrigList[i]);
		}
		for (int drcounter = 0; drcounter < accounts.size(); drcounter++) {
			if (accounts.get(drcounter).getSelectionIndex() >= 0) {
				Iterator<String> itr = filterAccounts.iterator();
				while (itr.hasNext())

				{
					if (itr.next().equals(
							accounts.get(drcounter)
									.getItem(
											accounts.get(drcounter)
													.getSelectionIndex()))) {

						itr.remove();
					}
				}
			}
		}

		return filterAccounts;
	}


}

