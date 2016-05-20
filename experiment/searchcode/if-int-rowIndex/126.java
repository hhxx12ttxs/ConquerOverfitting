
package gnukhata.views;

import gnukhata.globals;
import gnukhata.controllers.transactionController;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.AccountLockedException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

//import com.sun.org.apache.bcel.internal.generic.Select;
//import com.sun.xml.internal.ws.encoding.SwACodec;

public class AddNewVoucherComposite extends Composite {
	Rectangle grpBounds;
	static Display display;
	boolean pflag=false;
	boolean editAmt = false;
	boolean val = true;
	double oldValue = 0.00;
	boolean msgflag = false;
	int rowcntr;
	String typeFlag;
	String DrCrFlag;
	String searchText = "";
	long searchTexttimeout = 0;
	int startFrom = 0;
	double totalDrAmount = 0.00;
	double totalCrAmount = 0.00;
	List<Combo> CrDrFlags = new ArrayList<Combo>();
	List<Combo> accounts = new ArrayList<Combo>();
	List<Text> DrAmounts = new ArrayList<Text>();
	List<Text> CrAmounts = new ArrayList<Text>();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	List<String> masterQueryParams = new ArrayList<String>();
	List<Object> detailQueryParams = new ArrayList<Object>();
	//List<Button> removeButton = new ArrayList<Button>();
	
	//fdrecord;
	Label lbldate;
	Text txtddate;
	Label dash1;
	Text txtmdate;
	Label dash2;
	Text txtyrdate;
	public static Label lblsavemsg;
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
	
	Combo holdfocuscombo;
	Combo comboselprj;
	Combo comboDr_cr1;
	Combo comboDr_cr2;
	Combo comboDr_CrAccName1;
	Combo comboDr_CrAccName2;
	Combo newCrDrCombo;
	Combo newAccountsCombo;
	Text newTxtDrAmount;
	Text newTxtCrAmount;
	
	Label lblPurchaseOrdNo;
	Label lblPurchaseAmount;
	Label lblEnterPurchaseOrdNo;
	Label lblPurchasedate;
	Text txtPurchaseddate;
	Label dash3;
	Text txtPurchasemdate;
	Label dash4;
	Text txtPurchaseyrdate;
	Button btnpurchaseOdrNo;
	Text txtPurchaseOrderNo;
	Label lblPurchaseOrdAmt;
	Text txtPurchaseOrdAmt;
	
	Group addvoucher;
	/*Button btnRemove1;
	Button btnRemove2;
	Button btnNewRemoveButton;
	*/
	Button btnsave;
	Button btnAddAccount;
	
	NumberFormat nf;
	//int rowIndex = 2;
	Group grpVoucher;
	Group grpLabel;
	boolean totalRowCalled = false;
	int totalWidth = 0;
	boolean verifyFlag = false;
	int crdrleft = 1;
	int crdrright = 13;
	int accountsleft = 13;
	int accountsright = 50;
	int dramountleft = 50;
	int dramountright = 72;
	int cramountleft = 72;
	int cramountright = 94;
	/*int removeleft = 85;
	int removeright = 99;
	*/int currenttop = 0;
	int incrementby = 8;
	int grpVoucherWidth = 0;
	int grpVoucherHeight = 0;
	String popupDrCr = "";
	AddNewVoucherComposite(Composite parent, int Style, String voucherType) {
		
		super(parent, Style);
		//this.setExpandVertical(true);
		typeFlag = voucherType;
		Date today = new Date();
		String strToday = sdf.format(today);
		nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		
		FormLayout formlayout = new FormLayout();
		this.setLayout(formlayout);
		// this.setLayout(new FormLayout());
		 MainShell.lblLogo.setVisible(false);
		 MainShell.lblLine.setVisible(false);
		 MainShell.lblOrgDetails.setVisible(false);
		    

		addvoucher = new Group(this, SWT.BORDER);
		addvoucher.setText("    Add " + typeFlag + " voucher    ");
		addvoucher.setFont(new Font(display, "Times New Roman", 12, SWT.ITALIC));
		addvoucher.setLayout(formlayout);
		FormData layout = new FormData();
		String voucherno = transactionController.getLastReference(typeFlag);
		
		lblvoucherno = new Label(this, SWT.NONE);
		lblvoucherno.setText("&Voucher No *");
		lblvoucherno.setFont(new Font(display, "Time New Roman", 10, SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher,1);
		layout.left = new FormAttachment(6);
		layout.right = new FormAttachment(20);
		/*layout.bottom = new FormAttachment(15);*/
		lblvoucherno.setLayoutData(layout);
		
	
		txtvoucherno = new Text(this, SWT.BORDER);
		txtvoucherno.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher);
		layout.left = new FormAttachment(lblvoucherno);
		layout.right = new FormAttachment(33);
		//layout.bottom = new FormAttachment(12);
		txtvoucherno.setLayoutData(layout);
		txtvoucherno.setEditable(true);
		txtvoucherno.setText(voucherno);
		
		
		
		lbldate = new Label(this, SWT.NONE);
		lbldate.setText("&Date :");
		lbldate.setFont(new Font(display, "Time New Roman", 10, SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher,1);
		layout.left = new FormAttachment(45);
		layout.right = new FormAttachment(50);
		/*layout.bottom = new FormAttachment(12)*/;
		lbldate.setLayoutData(layout);
		String lastDate = transactionController.getLastDate(typeFlag);
		
		
		
		txtddate = new Text(this, SWT.BORDER);
		txtddate.setTextLimit(2);
		txtddate.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher);
		layout.left = new FormAttachment(52);
		//layout.right = new FormAttachment(56);
		//layout.bottom = new FormAttachment(12);
		txtddate.setLayoutData(layout);
		txtddate.setEditable(true);
		txtddate.setText(lastDate.substring(8 ));
		txtddate.setSelection(0, 2);

		dash1 = new Label(this, SWT.NONE);
		dash1.setText("-");
		dash1.setFont(new Font(display, "Time New Roman", 14, SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher);
		layout.left = new FormAttachment(55);
		//layout.right = new FormAttachment(8);
		layout.bottom = new FormAttachment(10);
		dash1.setLayoutData(layout);
		
		txtmdate = new Text(this, SWT.BORDER);
		txtmdate.setTextLimit(2);
		txtmdate.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher);
		layout.left = new FormAttachment(56);
		//layout.right = new FormAttachment(60);
		//layout.bottom = new FormAttachment(12);
		txtmdate.setLayoutData(layout);
		txtmdate.setEditable(true);
		txtmdate.setText(lastDate.substring(5,7));
		txtmdate.setSelection(0, 2);

		dash2 = new Label(this, SWT.NONE);
		dash2.setText("-");
		dash2.setFont(new Font(display, "Time New Roman", 14, SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher);
		layout.left = new FormAttachment(59);
		//layout.right = new FormAttachment(61);
		layout.bottom = new FormAttachment(10);
		dash2.setLayoutData(layout);
		
		txtyrdate = new Text(this, SWT.BORDER);
		txtyrdate.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		layout = new FormData();
		layout.top = new FormAttachment(addvoucher);
		layout.left = new FormAttachment(60);
		//layout.right = new FormAttachment(66);
		//layout.bottom = new FormAttachment(12);
		txtyrdate.setTextLimit(4);
		txtyrdate.setLayoutData(layout);
		txtyrdate.setEditable(true);
		txtyrdate.setText(lastDate.substring(0,4));
		txtyrdate.setSelection(0, 4);
		
		lblsavemsg = new Label(this, SWT.NONE);
		//lblsavemsg.setText("Voucher saved with voucher number " );
		lblsavemsg.setFont(new Font(display, "Time New Roman", 12, SWT.BOLD|SWT.COLOR_RED));
		layout = new FormData();
		layout.top = new FormAttachment(1);
		layout.left = new FormAttachment(30);
		layout.right = new FormAttachment(70);
		layout.bottom = new FormAttachment(6);
		lblsavemsg.setLayoutData(layout);
		
	
		grpVoucher = new Group(this, SWT.BORDER | SWT.V_SCROLL);
		layout = new FormData();
		layout.top = new FormAttachment(lblvoucherno,14);
		layout.left = new FormAttachment(5);
		layout.right = new FormAttachment(95);
		layout.bottom = new FormAttachment(68);
		//voucherTable.setLayoutData(layout);
		
		grpVoucher.setLayoutData(layout);
		//grpVoucher.getVerticalBar().setVisible(true);
		//grpVoucher.pack(); 
		//grpVoucher.setText("add "+ voucherType );
		
			this.makeaccssible(grpVoucher);
		
		
		lblselprj = new Label(this, SWT.NONE);
		lblselprj.setText("S&elect Project : ");
		lblselprj.setFont(new Font(display, "Time New Roman", 10, SWT.RIGHT));
		layout = new FormData();
		layout.top = new FormAttachment(grpVoucher, 27);
		layout.left = new FormAttachment(5);
		//layout.right = new FormAttachment(20);
		//layout.bottom = new FormAttachment(83);
		lblselprj.setLayoutData(layout);
		lblselprj.setVisible(true);
		
		
		comboselprj= new Combo(this, SWT.DROP_DOWN| SWT.READ_ONLY);
		comboselprj.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		layout=new FormData();
		layout.top=new FormAttachment(grpVoucher, 26);
		layout.left=new FormAttachment(lblselprj);
		layout.right=new FormAttachment(40);
		layout.bottom=new FormAttachment(45);
		comboselprj.setLayoutData(layout);
		comboselprj.setVisible(true);
		comboselprj.add("No Project");
		if(comboselprj.getSelectionIndex()==-1)
		{
					comboselprj.setVisible(false);
					lblselprj.setVisible(false);
		}
		
		String[] allProjects = gnukhata.controllers.transactionController.getAllProjects();
		for (int i = 0; i < allProjects.length; i++ )
		{
			comboselprj.setVisible(true);
			lblselprj.setVisible(true);
			comboselprj.add(allProjects[i]);
		}
		comboselprj.select(0);
			
		// Narration
		lblnarration = new Label(this, SWT.NONE);
		lblnarration.setText("Narrat&ion     : ");
		lblnarration.setFont(new Font(display, "Time New Roman", 10, SWT.RIGHT));
		layout = new FormData();
		layout.top = new FormAttachment(80);
		layout.left = new FormAttachment(5);
		//layout.right = new FormAttachment(61);
		//layout.bottom = new FormAttachment(79);
		lblnarration.setLayoutData(layout);
		
		txtnarration = new Text(this, SWT.MULTI | SWT.BORDER|SWT.WRAP);
		layout = new FormData();
		layout.top = new FormAttachment(lblselprj, 9);
		layout.left = new FormAttachment(lblnarration);
		layout.right = new FormAttachment(85);
		layout.bottom = new FormAttachment(89);
		txtnarration.setLayoutData(layout);

		btnsave = new Button(this, SWT.PUSH);
		btnsave.setText("&save");
		btnsave.setFont(new Font(display, "Time New Roman", 10, SWT.RIGHT));
		layout = new FormData();
		layout.top = new FormAttachment(89, 5);
		layout.left = new FormAttachment(36);
		layout.right = new FormAttachment(46);
		layout.bottom = new FormAttachment(97);
		btnsave.setLayoutData(layout);
	
		// txtddate.setFocus();
		
		btnAddAccount = new Button(this, SWT.PUSH);
		btnAddAccount.setText("Add Ac&count");
		btnAddAccount.setFont(new Font(display, "Time New Roman", 10, SWT.RIGHT));
		layout = new FormData();
		layout.top = new FormAttachment(89, 5);
		layout.left = new FormAttachment(btnsave,13);
		//layout.right = new FormAttachment(52);
		//layout.bottom = new FormAttachment(97);
		btnAddAccount.setLayoutData(layout);
		//btnAddAccount.setVisible(false);
		btnAddAccount.setEnabled(false);
		// txtddate.setFocus();

				
		this.getAccessible();
		
		
		this.setBounds(this.getDisplay().getPrimaryMonitor().getBounds());
			grpVoucherWidth = grpVoucher.getClientArea().width;
		grpVoucherHeight = grpVoucher.getClientArea().height;

		//this.pack();
		
		// this.setEvents();
		this.setInitialVoucher();
		this.setEvents();
		
		txtvoucherno.setFocus();
		btnsave.setVisible(false);
		txtvoucherno.setSelection(0,txtvoucherno.getText().length());

	}
	private void setInitialVoucher()
	{
		//add labeles and 2 rows here.xt
		int grpWidth = grpVoucher.getClientArea().width;
		/*MessageBox msgwidth = new MessageBox(new Shell(),SWT.OK);
		msgwidth.setMessage("with of group is " + Integer.toString(grpWidth ));
		msgwidth.open();*/
		//GridLayout gl = new GridLayout();
		//gl.numColumns = 5;
		FormLayout	 grpLayout = new FormLayout();
		
		grpVoucher.setLayout(grpLayout);
		lblDR_CR = new Label(grpVoucher, SWT.BORDER |SWT.CENTER);
		lblDR_CR.setText("   DR/CR   ");
		lblDR_CR.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		FormData fd = new FormData();
		fd.top = new FormAttachment(currenttop);
		fd.left= new FormAttachment(crdrleft);
		fd.right= new FormAttachment(crdrright);
		fd.bottom= new FormAttachment(currenttop + incrementby );
		lblDR_CR.setLayoutData(fd);
			
		//GridData gd = new GridData();
		//gd.widthHint = 10 * grpWidth / 100;
		//gd.grabExcessHorizontalSpace = true;
		//lblDR_CR.setLayoutData(gd);
		
		//lblDR_CR.pack();

		lblAccName = new Label(grpVoucher,SWT.BORDER | SWT.CENTER);
		lblAccName.setText("        	  Account Name 	          ");
		//gd = new GridData();
		//gd.widthHint = 30 * grpWidth / 100;
		//gd.grabExcessHorizontalSpace = true;
		//lblAccName.setLayoutData(gd);
		lblAccName.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		fd = new FormData();
		fd.left = new FormAttachment(accountsleft);
		fd.right =  new FormAttachment(accountsright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblAccName.setLayoutData(fd);
		
		//lblAccName.pack();

		lblDrAmount = new Label(grpVoucher, SWT.BORDER);
		lblDrAmount.setText("                         Debit Amount                  ");
		//gd = new GridData();
		//gd.grabExcessHorizontalSpace = true;
		//gd.widthHint = 20 * grpWidth / 100;
		lblDrAmount.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		fd = new FormData();
		fd.left = new FormAttachment(dramountleft);
		fd.right =  new FormAttachment(dramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblDrAmount.setLayoutData(fd);
		//lblDrAmount.pack();
		
		

		lblcrAmount = new Label(grpVoucher, SWT.BORDER);
		lblcrAmount.setText("                         Credit Amount                   ");
		/*gd = new GridData();
		
		gd.widthHint = 20 * grpWidth / 100;
		gd.grabExcessHorizontalSpace = true;*/
		lblcrAmount.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		fd = new FormData();
		fd.left = new FormAttachment(cramountleft);
		fd.right =  new FormAttachment(cramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblcrAmount.setLayoutData(fd);
		//lblcrAmount.pack();
		
		
		/*lblFiller =new Label(grpVoucher,SWT.BORDER);
		lblFiller.setText("  ");
		lblFiller.setVisible(false);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 20 * grpWidth / 100;
		fd = new FormData();
		fd.left = new FormAttachment(removeleft);
		fd.right =  new FormAttachment(removeright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		lblFiller.setLayoutData(fd);
		*///lblFiller.pack();
		//grpVoucher.pack();

//the first row with Dr as default, remove
//totalWidth = grpVoucher.getClientArea().width;
		currenttop=currenttop+8;
		comboDr_cr1 = new Combo(grpVoucher, SWT.READ_ONLY);
		comboDr_cr1.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		comboDr_cr1.add("Dr");
		comboDr_cr1.add("Cr");
		comboDr_cr1.select(0);
		/*gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 10 * grpWidth / 100;*/
		//gd.horizontalAlignment = SWT.CENTER;
		fd = new FormData();
		fd.left = new FormAttachment(crdrleft);
		fd.right =  new FormAttachment(crdrright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		comboDr_cr1.setLayoutData(fd);
		//comboDr_cr1.pack();
		
		
		comboDr_CrAccName1 = new Combo(grpVoucher, SWT.READ_ONLY);
		/*gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 30 * totalWidth / 100;*/
		//comboDr_CrAccName1.add("Select", 0);
		//comboDr_CrAccName1.select(0);
		comboDr_CrAccName1.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		fd = new FormData();
		fd.left = new FormAttachment(accountsleft);
		fd.right =  new FormAttachment(accountsright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		comboDr_CrAccName1.add("Please Select");
		comboDr_CrAccName1.setLayoutData(fd);
		//comboDr_CrAccName1.pack();
		
		txtDebAmt1 = new Text(grpVoucher, SWT.RIGHT);
		txtDebAmt1.setText("0.00");
		// DrAmounts.get(0).setSelection(0,4 );
		/*gd=new GridData();
		//gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 20 * totalWidth / 100;*/
		txtDebAmt1.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		fd = new FormData();
		fd.left = new FormAttachment(dramountleft);
		fd.right =  new FormAttachment(dramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		txtDebAmt1.setLayoutData(fd);
		//txtDebAmt1.pack();
		
		txtCreAmt1 = new Text(grpVoucher, SWT.RIGHT);
		txtCreAmt1.setText("0.00");
		txtCreAmt1.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		// txtCreAmt1.setSelection(0,txtCreAmt1.getText().length());
		/*gd=new GridData();
		//gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 20 * totalWidth / 100;*/
		fd = new FormData();
		fd.left = new FormAttachment(cramountleft);
		fd.right =  new FormAttachment(cramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		txtCreAmt1.setLayoutData(fd);
		//txtCreAmt1.pack();
		
	/*	btnRemove1 = new Button(grpVoucher, SWT.BORDER);
		btnRemove1.setText("Remove");
		//btnRemove1.setData("curindex", 0);
		btnRemove1.setVisible(true);
		gd=new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 20 * totalWidth / 100;
		fd = new FormData();
		fd.left = new FormAttachment(removeleft);
		fd.right =  new FormAttachment(removeright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		btnRemove1.setLayoutData(fd);
	*/	//btnRemove1.pack();
		//grpVoucher.pack();
		currenttop=currenttop+8;
		comboDr_cr2 = new Combo(grpVoucher, SWT.READ_ONLY);
		comboDr_cr2.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		comboDr_cr2.add("Dr");
		comboDr_cr2.add("Cr");
		comboDr_cr2.select(1);
		/*gd=new GridData();
		//gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 10* totalWidth / 100;*/
		fd = new FormData();
		fd.left = new FormAttachment(crdrleft);
		fd.right =  new FormAttachment(crdrright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		comboDr_cr2.setLayoutData(fd);
		//comboDr_cr2.pack();
		
		comboDr_CrAccName2 = new Combo(grpVoucher, SWT.READ_ONLY);
		//comboDr_CrAccName2.add("			select			");
		//comboDr_CrAccName2.select(0);
		/*gd=new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 30 * totalWidth / 100;*/
		comboDr_CrAccName2.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		fd = new FormData();
		fd.left = new FormAttachment(accountsleft);
		fd.right =  new FormAttachment(accountsright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		//comboDr_cr2.add("Please Select");
		comboDr_CrAccName2.setLayoutData(fd);
		//comboDr_CrAccName2.pack();
		
		txtDebAmt2 = new Text(grpVoucher, SWT.RIGHT);
		txtDebAmt2.setText("0.00");
		txtDebAmt2.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		/*gd=new GridData();
		//gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 20 * totalWidth / 100;*/
		fd = new FormData();
		fd.left = new FormAttachment(dramountleft);
		fd.right =  new FormAttachment(dramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		txtDebAmt2.setLayoutData(fd);
		//txtDebAmt2.pack();
		
		txtCreAmt2 = new Text(grpVoucher, SWT.RIGHT);
		txtCreAmt2.setText("0.00");
		txtCreAmt2.setFont(new Font(display,"Times New Roman",10,SWT.NORMAL));
		/*gd=new GridData();
		//gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 20 * totalWidth / 100;*/
		fd = new FormData();
		fd.left = new FormAttachment(cramountleft);
		fd.right =  new FormAttachment(cramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		txtCreAmt2.setLayoutData(fd);
		//txtCreAmt2.pack();
		
		/*btnRemove2 = new Button(grpVoucher, SWT.BORDER);
		btnRemove2.setText("Remove");
		//btnRemove2.setData("curindex",1);
		btnRemove2.setVisible(true);
		gd=new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint  = 20 * totalWidth / 100;
		fd = new FormData();
		fd.left = new FormAttachment(removeleft);
		fd.right =  new FormAttachment(removeright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom= new FormAttachment(currenttop+incrementby);
		btnRemove2.setLayoutData(fd);
		*///btnRemove2.pack();
		//grpVoucher.pack();
		currenttop = currenttop + 8; 
		
		
		CrDrFlags.add(comboDr_cr1);
		CrDrFlags.add(comboDr_cr2);
		accounts.add(comboDr_CrAccName1);
		accounts.add(comboDr_CrAccName2);
		DrAmounts.add(txtDebAmt1);
		DrAmounts.add(txtDebAmt2);
		CrAmounts.add(txtCreAmt1);
		CrAmounts.add(txtCreAmt2);
		/*removeButton.add(btnRemove1);
		removeButton.add(btnRemove2);
		*///btnRemove1.setData("rowindex", "0");
		//btnRemove2.setData("rowindex","1");
			
		comboDr_CrAccName1.select(0);
		comboDr_CrAccName2.select(0);
		accounts.get(0).add("Please Select", 0);
		accounts.get(0).select(0);
		accounts.get(1).add("Please Select", 0);
		accounts.get(1).select(0);
		
		//grpVoucher.pack();
		//grpVoucher.setSize(grpVoucherWidth  ,grpVoucherHeight );
		grpBounds = grpVoucher.getBounds();
	//then pack the group.
		//currenttop = 40;
		//grpVoucher.pack();
		grpVoucher.layout();
		grpVoucher.pack();




	}

	public void makeaccssible(Control c) {
		c.getAccessible();
	}

	private ArrayList<String> getFilteredAccountList(String[] OrigList) {
		ArrayList<String> filterAccounts = new ArrayList<String>();

		for (int i = 0; i < OrigList.length; i++) {
			filterAccounts.add(OrigList[i]);
		}
		filterAccounts.add(0, "Please Select");
		for (int drcounter = 0; drcounter < accounts.size(); drcounter++) {
			

			if (accounts.get(drcounter).getSelectionIndex() > 0) {
				Iterator<String> itr = filterAccounts.iterator();
				while (itr.hasNext())

				{
					if (itr.next().equals(accounts.get(drcounter).getItem(accounts.get(drcounter).getSelectionIndex()))) 
					{

						itr.remove();
											}
				}
			}
		}

		return filterAccounts;
	}

	private void addRow(String DrCrParam, double balanceAmount) {
		
		
		FormData	 fd = new FormData();
		
		
		
		newCrDrCombo = new Combo(grpVoucher, SWT.READ_ONLY);
		newCrDrCombo.add("Dr");
		newCrDrCombo.add("Cr");
		fd.left = new FormAttachment(crdrleft);
		fd.right = new FormAttachment(crdrright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newCrDrCombo.setLayoutData(fd);
		
		
		
		
		

		// CrDrFlags.get(CrDrFlags.size()-1 ).setFocus();
		newAccountsCombo = new Combo(grpVoucher, SWT.READ_ONLY);
	//	newAccountsCombo.add("               Select              ");
		fd=new FormData();
		fd.left = new FormAttachment(accountsleft);
		fd.right = new FormAttachment(accountsright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newAccountsCombo.setLayoutData(fd);

		newTxtDrAmount = new Text(grpVoucher, SWT.RIGHT);
		fd=new FormData();
		fd.left = new FormAttachment(dramountleft);
		fd.right = new FormAttachment(dramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newTxtDrAmount.setLayoutData(fd);
		
		newTxtCrAmount = new Text(grpVoucher, SWT.RIGHT);
		fd=new FormData();
		fd.left = new FormAttachment(cramountleft);
		fd.right = new FormAttachment(cramountright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		newTxtCrAmount.setLayoutData(fd);
		
		//btnNewRemoveButton = new Button(grpVoucher, SWT.PUSH);
		//btnNewRemoveButton.setData("rowindex",);
		//rowIndex ++;
	/*	btnNewRemoveButton.setText("Remove");
	//tnNewRemoveButton.setVisible(false);
		fd=new FormData();
		fd.left = new FormAttachment(removeleft);
		fd.right = new FormAttachment(removeright);
		fd.top= new FormAttachment(currenttop);
		fd.bottom = new FormAttachment(currenttop + incrementby  );
		btnNewRemoveButton.setLayoutData(fd);
	*/	
		currenttop = currenttop + 8;

		// since new widgets are added to a row we will add them to respective
		// array list
	
		CrDrFlags.add(newCrDrCombo);
		accounts.add(newAccountsCombo);
		DrAmounts.add(newTxtDrAmount);
		CrAmounts.add(newTxtCrAmount);
		//removeButton.add(btnNewRemoveButton);
		
				
		if (DrCrParam.equals("Dr")) {
			// newCrDrCombo.select(0);
			CrDrFlags.get(CrDrFlags.size() - 1).select(0);
			DrAmounts.get(DrAmounts.size() -1).setSelection(0,DrAmounts.get(DrAmounts.size() -1).getText().length());
			DrAmounts.get(DrAmounts.size() - 1).setText(nf.format((balanceAmount)));
			CrAmounts.get(DrAmounts.size() - 1).setText("0.00");
			CrAmounts.get(CrAmounts.size() - 1).setEnabled(false);
			if (typeFlag.equals("Payment")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController
						.getPayment(DrCrParam));
				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Receipt")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getReceipt(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Credit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getCreditNote(DrCrParam));
				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Debit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getDebitNote(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getSales(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales Return")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getSalesReturn(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Purchase")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getPurchase(DrCrParam));

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
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getContra());
				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			if (typeFlag.equals("Journal")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getJournal());

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
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getPayment(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));

				}
			}

			if (typeFlag.equals("Receipt")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getReceipt(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Credit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getCreditNote(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Debit Note")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getDebitNote(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getSales(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Sales Return")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getSalesReturn(DrCrParam));

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}

			if (typeFlag.equals("Purchase")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getPurchase(DrCrParam));

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
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getContra());
            	for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			if (typeFlag.equals("Journal")) {
				List<String> finalAccounts = getFilteredAccountList(gnukhata.controllers.transactionController.getJournal());

				for (int i = 0; i < finalAccounts.size(); i++) {
					accounts.get(accounts.size() - 1).add(finalAccounts.get(i));
				}
			}
			}
		
		/*for (int i = 0; i < removeButton.size(); i++) {
			removeButton.get(i).setVisible(true);
		}
		*/
				//grpVoucher.setSize(grpVoucherWidth, grpVoucherHeight);
		//grpVoucher.setBounds(grpBounds);
				//grpVoucher.pack();
		//grpVoucher.setsi
		grpVoucher.layout();
		//grpVoucher.pack();

				setDynamicRowEvents();
				if(accounts.get(accounts.size()-1).getItemCount()== 0 )
				{
					accounts.get(accounts.size() -1).add("Please Select");
				}
					
					
	}

	private void setEvents() {
		txtvoucherno.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent arg0)
			{
				if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
				{
					btnsave.setVisible(true);
				}
				else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
				{
					btnsave.setVisible(false);
				}
				
				pflag=false;
				if(! lblsavemsg.getText().equals(""))
				{
					Display.getCurrent().asyncExec(new Runnable(){
						public void run()
						{
							long now = System.currentTimeMillis();
							long lblTimeOUt = 0;
							while(lblTimeOUt < (now + 2000))
							{
								lblTimeOUt = System.currentTimeMillis();
							}
		/*					MessageBox endtimer = new MessageBox(new Shell(),SWT.OK);
							endtimer.setText("time up! now will empty the label");
							endtimer.open();
		*/
							lblsavemsg.setText("");

						}
				});
/*					MessageBox msgtime = new MessageBox(new Shell(),SWT.OK);
					msgtime.setMessage("lable is not empty will run timmer");
					msgtime.open();
*/

					
				}

			}
		});
		
		CrAmounts.get(0).setEnabled(false);
		DrAmounts.get(1).setEnabled(false);
		//removeButton.get(0).setVisible(false);
		//removeButton.get(1).setVisible(false);
		if (typeFlag.equals("Contra")) {
			comboDr_CrAccName1.setItems(transactionController.getContra());
			comboDr_CrAccName2.setItems(transactionController.getContra());

		}
		if (typeFlag.equals("Journal")) {
			comboDr_CrAccName1.setItems(transactionController.getJournal());
			comboDr_CrAccName2.setItems(transactionController.getJournal());

		}
		if (typeFlag.equals("Payment")) {
			comboDr_CrAccName1.setItems(transactionController.getPayment("Dr"));
			comboDr_CrAccName2.setItems(transactionController.getPayment("Cr"));

		}
		if (typeFlag.equals("Receipt")) {
			comboDr_CrAccName1.setItems(transactionController.getReceipt("Dr"));
			comboDr_CrAccName2.setItems(transactionController.getReceipt("Cr"));
		}
		if (typeFlag.equals("Credit Note")) {
			comboDr_CrAccName1.setItems(transactionController
					.getCreditNote("Dr"));
			comboDr_CrAccName2.setItems(transactionController
					.getCreditNote("Cr"));
		}
		if (typeFlag.equals("Debit Note")) {
			comboDr_CrAccName1.setItems(transactionController
					.getDebitNote("Dr"));
			comboDr_CrAccName2.setItems(transactionController
					.getDebitNote("Cr"));
		}
		if (typeFlag.equals("Sales")) {
			comboDr_CrAccName1.setItems(transactionController.getSales("Dr"));
			comboDr_CrAccName2.setItems(transactionController.getSales("Cr"));
		}
		if (typeFlag.equals("Sales Return")) {
			comboDr_CrAccName1.setItems(transactionController
					.getSalesReturn("Dr"));
			comboDr_CrAccName2.setItems(transactionController
					.getSalesReturn("Cr"));
		}
		if (typeFlag.equals("Purchase")) {
			comboDr_CrAccName1
					.setItems(transactionController.getPurchase("Dr"));
			comboDr_CrAccName2
					.setItems(transactionController.getPurchase("Cr"));
		}
		if (typeFlag.equals("Purchase Return")) {
			comboDr_CrAccName1.setItems(transactionController
					.getPurchaseReturn("Dr"));
			comboDr_CrAccName2.setItems(transactionController
					.getPurchaseReturn("Cr"));
		
		}
	accounts.get(0).add("Please Select", 0);
	accounts.get(1).add("Please Select",0);
	accounts.get(0).select(0);
	accounts.get(1).select(0);

			this.btnAddAccount.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {						
				Shell shell = new Shell();
				AddAccountPopup dialog = new AddAccountPopup(shell);
				System.out.println(dialog.open()); 
				
			if(AddAccountPopup.cancelflag.equals(true))
			{
					shell.dispose();
					holdfocuscombo.setFocus();
					return;
			}
			else
			{
				try {
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
						else
						{
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
						else
						{
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
							else
							{
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
							else
							{
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
							else
							{
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
							else
							{
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
							else
							{
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
							else
							{
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
							else
							{
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
							else
							{
								holdfocuscombo.setFocus();
							}
						}	
					}
				} catch (NullPointerException e ) {
					// TODO Auto-generated catch block
					holdfocuscombo.setFocus();
					//e.printStackTrace();
				}
				
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
					if(arg0.keyCode==46)
					{
						return;
					}
					if(arg0.keyCode == 45||arg0.keyCode == 62)
					{
						  arg0.doit = false;
						
					}
					switch (arg0.keyCode) {
		            case SWT.BS:           // Backspace
		            case SWT.DEL:          // Delete
		            case SWT.HOME:         // Home
		            case SWT.END:          // End
		            case SWT.ARROW_LEFT:   // Left arrow
		            case SWT.ARROW_RIGHT:  // Right arrow
		                return;
		        }
					if(arg0.keyCode  == 46)
					{
						return;
					}
		        if (!Character.isDigit(arg0.character))
		        {
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
					if(arg0.keyCode==46)
					{
						return;
					}
					if(arg0.keyCode == 45||arg0.keyCode == 62)
					{
						  arg0.doit = false;
						
					}
					switch (arg0.keyCode) {
		            case SWT.BS:           // Backspace
		            case SWT.DEL:          // Delete
		            case SWT.HOME:         // Home
		            case SWT.END:          // End
		            case SWT.ARROW_LEFT:   // Left arrow
		            case SWT.ARROW_RIGHT:  // Right arrow
		                return;
		        }
					if(arg0.keyCode  == 46)
					{
						return;
					}
		        if (!Character.isDigit(arg0.character))
		        {
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
					if(arg0.keyCode==46)
					{
						return;
					}
					if(arg0.keyCode==45||arg0.keyCode == 62)
					{
						arg0.doit= false;
					}
					switch (arg0.keyCode) {
		            case SWT.BS:           // Backspace
		            case SWT.DEL:          // Delete
		            case SWT.HOME:         // Home
		            case SWT.END:          // End
		            case SWT.ARROW_LEFT:   // Left arrow
		            case SWT.ARROW_RIGHT:  // Right arrow
		                return;
		        }
					if(arg0.keyCode  == 46)
					{
						return;
					}
		        if (!Character.isDigit(arg0.character))
		        {
		            arg0.doit = false;  // disallow the action
		        }

					

				}
			});
				 
		this.btnsave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				//super.widgetSelected(e);
				
				masterQueryParams.clear();
				detailQueryParams.clear();
				
				if(!txtyrdate.getText().trim().equals("") && (Integer.valueOf(txtyrdate.getText())< 1900 || Integer.valueOf(txtyrdate.getText()) <= 0))
				{
					MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR);
					msgdateErr.setMessage(" Please enter valid date.");
					msgdateErr.open();
				
					
					Display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtyrdate.setText("");
							txtyrdate.setFocus();
							
						}
					});
					return;
					
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
			
				for(rowcntr=0; rowcntr<CrDrFlags.size(); rowcntr++)
				{
					if(accounts.get(rowcntr).getItemCount()> 1 && accounts.get(rowcntr).getSelectionIndex() == 0||accounts.get(rowcntr).getSelectionIndex() == -1 )
					{
						msgflag=true;
						MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK );
						msgaccerr .setMessage("Please select an account");
						msgaccerr.open();
						display.getCurrent().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								
								accounts.get(rowcntr).setFocus();
							}
						});
						
						return;
						
					}
				}
				
					
				if((totalCrAmount != totalDrAmount || totalDrAmount	 == 0 || totalCrAmount == 0)&& msgflag==false) 
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
						CrAmounts.get(0).setSelection(0,CrAmounts.get(0).getText().length());
						
						return;
					}
					
					
				}				
				
						if(txtvoucherno.getText().trim().equals(""))
						{	
							pflag=true;
							MessageBox errMsg = new MessageBox(new Shell(),SWT.ERROR | SWT.OK);
							errMsg.setMessage("Please enter a voucher Number");
							errMsg.open();
							//txtvoucherno.setText("");
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									txtvoucherno.setFocus();
									
								}
							});
							return;
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
					if( (accounts.get(accountvalidation).getSelectionIndex() == -1 )&& msgflag==false)
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
					
					if(CrDrFlags.get(detailCounter).getSelectionIndex()==0 && (DrAmounts.get(detailCounter).getText().trim().equals("") || Double.parseDouble( DrAmounts.get(detailCounter).getText()) == 0 ) )
					{
						continue;
					}
					if(CrDrFlags.get(detailCounter).getSelectionIndex()==1 && (CrAmounts.get(detailCounter).getText().trim().equals("") || Double.parseDouble( CrAmounts.get(detailCounter).getText()) == 0 ) )
					{
						continue;
					}

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
				
				
				
			if (val==true)
			{	
				CustomDialog confirm = new CustomDialog(new Shell());
				confirm.SetMessage( "Do you wish to save ?");
				
				int answer = confirm.open();
				 
				if( answer == SWT.YES)
				{
					if(transactionController.setTransaction(masterQueryParams, detailQueryParams) )
					{
						
				/*	MessageBox successmsg = new MessageBox(new Shell(),SWT.OK| SWT.ICON_INFORMATION);
					successmsg.setMessage("voucher saved with voucher code  "+ txtvoucherno.getText() );
					successmsg.open();
				*/	
					Composite grandParent = (Composite) btnsave.getParent().getParent().getParent().getParent();
					VoucherTabForm.typeFlag = VoucherTabForm.typeFlag;
					
					//lblsavemsg.setVisible(true);
					
					VoucherTabForm vtf = new VoucherTabForm(grandParent,SWT.NONE );
					
					AddNewVoucherComposite.lblsavemsg.setText("voucher saved with voucher No. " +txtvoucherno.getText());
					AddNewVoucherComposite.lblsavemsg.setVisible(true);
					btnsave.getParent().getParent().getParent().dispose();
					vtf.setSize(grandParent.getClientArea().width, grandParent.getClientArea().height);
					
					}
					else
					{
					MessageBox successmsg = new MessageBox(new Shell(),SWT.OK| SWT.ERROR);
					successmsg.setMessage("voucher can not be saved");
					successmsg.open();
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

		
		txtvoucherno.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				//super.keyPressed(arg0); 65 to 90 caps , 97 122
				if( (arg0.keyCode>= 65 && arg0.keyCode <= 90)||(arg0.keyCode>= 97 && arg0.keyCode <= 122) ||(arg0.keyCode>= 48 && arg0.keyCode <= 57) ||  arg0.keyCode== 8 || arg0.keyCode == 13||arg0.keyCode == SWT.KEYPAD_0||
						arg0.keyCode == SWT.KEYPAD_1||arg0.keyCode == SWT.KEYPAD_2||arg0.keyCode == SWT.KEYPAD_3||arg0.keyCode == SWT.KEYPAD_4||
						arg0.keyCode == SWT.KEYPAD_5||arg0.keyCode == SWT.KEYPAD_6||arg0.keyCode == SWT.KEYPAD_7||arg0.keyCode == SWT.KEYPAD_8||arg0.keyCode == SWT.KEYPAD_9)
				{
					arg0.doit = true;
				}
				else
				{
					
					arg0.doit = false;
				}
				if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
				{
					txtddate.setFocus();
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
					if(arg0.keyCode==SWT.ARROW_UP)
					{	
						txtvoucherno.setFocus();
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
					if(arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR)
					{
						comboDr_cr1.setFocus();
					}
					if(arg0.keyCode==SWT.ARROW_UP)
					{	
						txtmdate.setFocus();
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
					if(arg0.keyCode==SWT.ARROW_UP)
					{
						if(DrAmounts.get(DrAmounts.size()-1).getEnabled()==true)
						{	
							DrAmounts.get(DrAmounts.size()-1).setFocus();
						}
						if(CrAmounts.get(CrAmounts.size()-1).getEnabled()==true)
						{
							CrAmounts.get(CrAmounts.size()-1).setFocus();
						}						
						
					}
					long now = System.currentTimeMillis();
					if (now > searchTexttimeout){
				         searchText = "";
				      }
					searchText += Character.toLowerCase(arg0.character);
					searchTexttimeout = now + 1000;					
					for(int i = 0; i < comboselprj.getItemCount(); i++ )
					{
						if(comboselprj.getItem(i).toLowerCase().startsWith(searchText ) ){
							//arg0.doit= false;
							comboselprj.select(i);
							comboselprj.notifyListeners(SWT.Selection ,new Event()  );
							break;
						}
					}
			
										
				}
			});
			
		
			txtnarration.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method{} stub
					//super.keyPressed(arg0);
					if(arg0.keyCode==SWT.CR|| arg0.keyCode==SWT.TAB||arg0.keyCode == SWT.KEYPAD_CR )
					{
						if(btnsave.getVisible()==true)
						{
							btnsave.setFocus();
							btnsave.notifyListeners(SWT.Selection, new Event());
						}
						
						
					}
					if(arg0.keyCode==SWT.ARROW_UP)
					{
						if(comboselprj.isVisible())
						{
							comboselprj.setFocus();
						}
						else
						{
							if(DrAmounts.get(DrAmounts.size()-1).getEnabled()==true)
							{	
								DrAmounts.get(DrAmounts.size()-1).setFocus();
							}
							if(CrAmounts.get(CrAmounts.size()-1).getEnabled()==true)
							{
								CrAmounts.get(CrAmounts.size()-1).setFocus();
							}						
						
						}
					}
				}
			});
			
			
			btnsave.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
				//	super.keyPressed(arg0);
					if(arg0.keyCode==SWT.ARROW_UP)
					{
						txtnarration.setFocus();
						
					}
					
				}		
			});
			

			btnAddAccount.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
				//	super.keyPressed(arg0);
					if(arg0.keyCode==SWT.ARROW_UP)
					{
						btnsave.setFocus();
						
					}
					
				}		
			});
			
			
			//validations
			//date val
			
			txtmdate.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					if((arg0.keyCode>= 48 && arg0.keyCode <= 57) ||  arg0.keyCode== 8 || arg0.keyCode == 13 || arg0.keyCode == SWT.KEYPAD_0||
							arg0.keyCode == SWT.KEYPAD_1||arg0.keyCode == SWT.KEYPAD_2||arg0.keyCode == SWT.KEYPAD_3||arg0.keyCode == SWT.KEYPAD_4||
							arg0.keyCode == SWT.KEYPAD_5||arg0.keyCode == SWT.KEYPAD_6||arg0.keyCode == SWT.KEYPAD_7||arg0.keyCode == SWT.KEYPAD_8||arg0.keyCode == SWT.KEYPAD_9)
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
					if((arg0.keyCode>= 48 && arg0.keyCode <= 57) ||  arg0.keyCode== 8 || arg0.keyCode == 13||arg0.keyCode == SWT.KEYPAD_0||
						arg0.keyCode == SWT.KEYPAD_1||arg0.keyCode == SWT.KEYPAD_2||arg0.keyCode == SWT.KEYPAD_3||arg0.keyCode == SWT.KEYPAD_4||
						arg0.keyCode == SWT.KEYPAD_5||arg0.keyCode == SWT.KEYPAD_6||arg0.keyCode == SWT.KEYPAD_7||arg0.keyCode == SWT.KEYPAD_8||arg0.keyCode == SWT.KEYPAD_9)
					{
						arg0.doit = true;
					}
					else
					{
						
						arg0.doit = false;
					}
				}
			});
			
			txtddate.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					if((arg0.keyCode>= 48 && arg0.keyCode <= 57) ||  arg0.keyCode== 8 || arg0.keyCode == 13 || arg0.keyCode == SWT.KEYPAD_0||
						arg0.keyCode == SWT.KEYPAD_1||arg0.keyCode == SWT.KEYPAD_2||arg0.keyCode == SWT.KEYPAD_3||arg0.keyCode == SWT.KEYPAD_4||
						arg0.keyCode == SWT.KEYPAD_5||arg0.keyCode == SWT.KEYPAD_6||arg0.keyCode == SWT.KEYPAD_7||arg0.keyCode == SWT.KEYPAD_8||arg0.keyCode == SWT.KEYPAD_9)
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
				public void focusGained(FocusEvent arg0)
				{
					verifyFlag = true;
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
					
				}
				
				
				@Override
				public void focusLost(FocusEvent arg0) {
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					verifyFlag = false;
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
					/*if(txtFromDtDay.getText().length()==2)
					   {
						   txtFromDtMonth.setFocus();
					   }*/
				}
			});
			txtmdate.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0)
				{
					verifyFlag = true;
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
					
				}
				
				@Override
				public void focusLost(FocusEvent arg0) {
					
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					verifyFlag = false;
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
			});
			
			
			
			txtyrdate.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0)
				{
					verifyFlag = true;
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
					
				}
				
				@Override
				public void focusLost(FocusEvent arg0) {
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					verifyFlag = false;
					if(!txtyrdate.getText().trim().equals("") && (Integer.valueOf(txtyrdate.getText())< 1900 || Integer.valueOf(txtyrdate.getText()) <= 0))
					{
						MessageBox msgdateErr = new MessageBox(new Shell(), SWT.OK | SWT.ERROR);
						msgdateErr.setMessage(" Please enter valid date.");
						msgdateErr.open();
					
						
						Display.getCurrent().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								txtyrdate.setText("");
								txtyrdate.setFocus();
								
							}
						});
						return;
						
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
				
					/*if(txtyrdate.getText().trim().equals("")&& Integer.valueOf(txtyrdate.getText())<=0)
					{
						MessageBox msgDayErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
						msgDayErr.setMessage("please enter a Year in yyyy in valid format.");
						msgDayErr.open();
						display.getCurrent().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								txtyrdate.setFocus();
								
							}
						});
						return;
					}*/
					
					/*DateValidate dv = new DateValidate(Integer.valueOf(txtmdate.getText()) ,Integer.valueOf(txtddate.getText()) ,Integer.valueOf(txtyrdate.getText()));
					String validationResult = dv.toString();
					if(validationResult.substring(2,3).equals("0"))
					{
						MessageBox msgErr = new MessageBox(new Shell(),SWT.OK | SWT.ERROR);
						msgErr.setMessage("You have entered invalid Date");
						msgErr.open();
					
					Display.getCurrent().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							txtddate.setFocus();
							
						}
					});
					return;
					}
					*/

					try {
						Date voucherDate = sdf.parse(txtyrdate.getText() + "-" + txtmdate.getText() + "-" + txtddate.getText());
						Date fromDate = sdf.parse(globals.session[2].toString().substring(6)+ "-" + globals.session[2].toString().substring(3,5) + "-"+ globals.session[2].toString().substring(0,2));
						Date toDate = sdf.parse(globals.session[3].toString().substring(6)+ "-" + globals.session[3].toString().substring(3,5) + "-"+ globals.session[3].toString().substring(0,2));
						
						if(voucherDate.compareTo(fromDate)< 0 || voucherDate.compareTo(toDate) > 0 )
						{
							MessageBox errMsg = new MessageBox(new Shell(),SWT.ERROR |SWT.OK );
							errMsg.setMessage("Please enter valid date within the Financialn Year");
							errMsg.open();
							Display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									txtyrdate.setFocus();
									txtyrdate.setText("");
								}
							});
							
							return;
						}
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.getMessage();
					}
				}
				
			});

					
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
					
					
						if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
						{
							btnsave.setVisible(true);
						}
						else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
						{
							btnsave.setVisible(false);
						}
						
					
					
					Combo currentCRDR = (Combo) arg0.widget;
					String DrCrFlag =  currentCRDR.getItem(currentCRDR.getSelectionIndex());

					int rowindex = (Integer) currentCRDR.getData("curindex");
					accounts.get(rowindex).removeAll();
					
				//	CrAmounts.get(rowindex).setText("0.00");
					CrAmounts.get(rowindex).setSelection(0,CrAmounts.get(rowindex).getText().length());
					//DrAmounts.get(rowindex).setText("0.00");
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
					}
					if (DrCrFlag.equals("Cr")&& rowindex ==0) {
						CrDrFlags.get(rowindex +1).select(0);
						CrDrFlags.get(rowindex +1).notifyListeners(SWT.Selection, new Event());
						CrAmounts.get(rowindex).setEnabled(true);
						DrAmounts.get(rowindex).setEnabled(false);
						CrAmounts.get(rowindex+1).setEnabled(false);
						DrAmounts.get(rowindex+1).setEnabled(true);
						CrAmounts.get(rowindex).setSelection(rowindex,CrAmounts.get(rowindex).getText().length());
						DrAmounts.get(rowindex).setText("0.00");
						//DrAmounts.get(rowindex).setEnabled(false);
						DrAmounts.get(rowindex).setSelection(0, DrAmounts.get(rowindex).getText().length());
					}
					
					if (DrCrFlag.equals("Dr") && rowindex > 0 ) {
						
						DrAmounts.get(rowindex).setEnabled(true);
						CrAmounts.get(rowindex).setEnabled(false);
						//CrAmounts.get(rowindex +1).setEnabled(true);
						//DrAmounts.get(rowindex +1).setEnabled(false);
						DrAmounts.get(rowindex).setSelection(rowindex,DrAmounts.get(rowindex).getText().length());
						CrAmounts.get(rowindex).setText("0.00");
//						CrAmounts.get(rowindex).setEnabled(false);				
					}
					if (DrCrFlag.equals("Cr") && rowindex >0) {
						
						CrAmounts.get(rowindex).setEnabled(true);
						DrAmounts.get(rowindex).setEnabled(false);
						//CrAmounts.get(rowindex+1).setEnabled(false);
						//DrAmounts.get(rowindex+1).setEnabled(true);
						CrAmounts.get(rowindex).setSelection(rowindex,CrAmounts.get(rowindex).getText().length());
						DrAmounts.get(rowindex).setText("0.00");
						//DrAmounts.get(rowindex).setEnabled(false);
						DrAmounts.get(rowindex).setSelection(0, DrAmounts.get(rowindex).getText().length());
					}
					
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
				
					if(accounts.get(rowindex).getItemCount()== 0 )
					{
						accounts.get(rowindex).add("Please Select");
					}
					accounts.get(rowindex).select(0);
				}
			});
			CrDrFlags.get(rowcounter).addFocusListener(new FocusAdapter() {
				
				@Override
				public void focusGained(FocusEvent arg0)
				{
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
					
				}
				
				
				@Override
				public void focusLost(FocusEvent arg0)
				{
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
					
				}
			});
			//Code to Enable the Add Account button true only when the focus is on accounts
			accounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent arg0){
					holdfocuscombo = (Combo) arg0.widget;
					final int rowindex = (Integer) holdfocuscombo.getData("curindex");
					popupDrCr = CrDrFlags.get(rowindex).getItem(CrDrFlags.get(rowindex).getSelectionIndex());
					//btnAddAccount.setVisible(true);
					btnAddAccount.setEnabled(true);
					
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
				}
			
			
/*			accounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0){

					holdfocuscombo = (Combo) arg0.widget;
					//btnAddAccount.setVisible(false);
					btnAddAccount.setEnabled(false);
				}
			});*/
			
			
			
			
/*			DrAmounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0){

					btnAddAccount.setVisible(false);
				}
			});
			
			CrAmounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent arg0){

					btnAddAccount.setVisible(false);
				}
			});*/
					
			//now the event lost focus for accounts dropdown.
			
				@Override
				public void focusLost(FocusEvent arg0) {
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					Combo currentAccount = (Combo) arg0.widget;
					final int rowindex = (Integer) currentAccount.getData("curindex");
					
					/*	if( accounts.get(rowindex).getSelectionIndex() == 0||accounts.get(rowindex).getSelectionIndex() == -1 )
						{
							
							MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK );
							msgaccerr .setMessage("Please select an account");
							msgaccerr.open();
							display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									
									accounts.get(rowindex).setFocus();
								}
							});
							
							return;
							
						}
					*/
					
					
					if(DrAmounts.get(rowindex).getEnabled())
					{
						DrAmounts.get(rowindex).setFocus();
						DrAmounts.get(rowindex).setSelection(0, DrAmounts.get(rowindex).getText().length());
						DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
					}
					if(CrAmounts.get(rowindex).getEnabled())
					{
						CrAmounts.get(rowindex).setFocus();
						CrAmounts.get(rowindex).setSelection(0, CrAmounts.get(rowindex).getText().length());
						CrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(CrAmounts.get(rowindex).getText())));
					}
				}
			});
			
			DrAmounts.get(rowcounter).addFocusListener(new FocusAdapter() {
				//editAmt flag for editing
				@Override
				public void focusGained(FocusEvent arg0) {
					btnAddAccount.setEnabled(false);
					
					/*final int rowindex = (Integer) currentDr.getData("curindex");*/
					verifyFlag = true;
							
			
					
					if(totalCrAmount==totalDrAmount && totalDrAmount!=0.00)
					{
						btnsave.setVisible(true);
					}
				
					else if(totalCrAmount!=totalDrAmount || totalDrAmount==0.00)	
					{
						btnsave.setVisible(false);
					}
					}
				
				
				public void focusLost(FocusEvent arg0) {
					verifyFlag = false;
					
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00 )
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
					
					
					Text currentDr = (Text) arg0.widget;
					final int rowindex = (Integer) currentDr.getData("curindex");
					if(DrAmounts.get(rowindex).getText().trim().equals("")  )
					{
						DrAmounts.get(rowindex).setText("0.00");
					}

					/*double newValue = Double.parseDouble(currentDr.getText());
					if(oldValue != newValue ||newValue == 0)
					{
						editAmt = true;*/
						try {
							DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
						} catch (NumberFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							MessageBox error = new MessageBox(new Shell(),SWT.OK);
							error.setMessage("Enter a valid amount");
							error.open();
							Display.getCurrent().asyncExec(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									DrAmounts.get(rowindex).setText("");;
									DrAmounts.get(rowindex).setFocus();
								}
							});
							return;
						}
						
					//}
					/*if(oldValue == newValue)
					{
						editAmt = false;			//no editing done
					}*/
					
					//if(!editAmt || (rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					if((rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					{
						if(totalRowCalled)
						{
							
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpVoucher.pack();
							
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
							//grpVoucher.pack();
							
						}
						else
						{
							
							
							totalRow();
							//grpVoucher.pack();
							//grpVoucher.pack();
						}
						return;
					}
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					
					if(rowindex== 0 || rowindex < accounts.size()-1)
					{
						
						DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
						CrAmounts.get(rowindex +1).setText(DrAmounts.get(rowindex).getText());
						verifyFlag = true;
						totalDrAmount=Double.parseDouble(DrAmounts.get(rowindex).getText());
						totalCrAmount=totalDrAmount;
						
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
							//grpVoucher.pack();
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
							//grpVoucher.pack();
							
						}
						else
						{
							
							
							totalRow();
							//grpVoucher.pack();
							//grpVoucher.pack();
						}
						
						CrDrFlags.get(rowindex+1).select(1);
						return;
					}
					
					if(rowindex> 0)
					{
						verifyFlag = true;
						if(DrAmounts.get(rowindex).getText().trim().equals(""))
						{
							DrAmounts.get(rowindex).setText("0.00");
						}
						DrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(DrAmounts.get(rowindex).getText())));
						
						totalDrAmount=0.00;
						totalCrAmount=0.00;
						for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
						{
							totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
							
						}						
						for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
						{
							totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
							
						}
						if(totalCrAmount==totalDrAmount && totalCrAmount != 0.00 && totalDrAmount != 0.00)
						{
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpVoucher.pack();
							totalRow();
							//grpVoucher.pack();
							
								
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
							//grpVoucher.pack();
							addRow("Dr", balanceAmount);
							//grpVoucher.pack();
							totalRow();
							//grpVoucher.pack();
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
							//grpVoucher.pack();
							addRow("Cr", balanceAmount);
							//grpVoucher.pack();
							totalRow();
							//grpVoucher.pack();
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
				@Override
				public void focusGained(FocusEvent arg0) {
					try {
						Text currentCr = (Text) arg0.widget;
						final int rowindex = (Integer) currentCr.getData("curindex");
						oldValue = Double.parseDouble(currentCr.getText());
						verifyFlag = true;
						btnAddAccount.setEnabled(false);
						double newValue = Double.parseDouble(currentCr.getText());
						boolean edit;
						if(oldValue==newValue || newValue!= 0.00)
								{
							edit=true;
								}
						else
						{
							edit=false;
						}
						
						if(edit=true)
						{
						if( totalCrAmount==totalDrAmount && totalCrAmount!=0.00)
						{
							btnsave.setVisible(true);
						}
						
						else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00)	
						{
							btnsave.setVisible(false);
						}
						}
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
				@Override
				public void focusLost(FocusEvent arg0) {
					verifyFlag = false;
					
					if(totalCrAmount==totalDrAmount && totalCrAmount!=0.00 && totalDrAmount!=0.00)
					{
						btnsave.setVisible(true);
					}
					else if(totalCrAmount!=totalDrAmount || totalCrAmount==0.00 || totalDrAmount==0.00)
					{
						btnsave.setVisible(false);
					}
					
					// TODO Auto-generated method stub
					//super.focusLost(arg0);
					//System.out.println(CrAmounts.get(rowIndex).getData("curindex").toString() );
					Text currentCr = (Text) arg0.widget;
					final int rowindex = (Integer) currentCr.getData("curindex");
					if(CrAmounts.get(rowindex).getText().trim().equals("")  )
					{
						CrAmounts.get(rowindex).setText("0.00");
							
					}
					/*double newValue = Double.parseDouble(currentCr.getText());
					
					if(oldValue != newValue||newValue == 0 )
					{
						editAmt = true;*/
						try {
							CrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(CrAmounts.get(rowindex).getText())));
						} catch (NumberFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							MessageBox error = new MessageBox(new Shell(),SWT.OK);
							error.setMessage("Enter a valid amount");
							error.open();
							Display.getCurrent().asyncExec(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									CrAmounts.get(rowindex).setText("");
									CrAmounts.get(rowindex).setFocus();
								}
							});
							return;
						}
						
					/*}
					if(oldValue == newValue)
					{
						editAmt = false;			//no editing done
					}*/
					
					//if(!editAmt || (rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					if((rowindex < CrDrFlags.size()-1 && CrDrFlags.get(rowindex).getSelectionIndex()== CrDrFlags.get(rowindex +1).getSelectionIndex()) )
					{
						
						
						if(totalRowCalled)
						{
							
							lblTotal.dispose();
							lblTotalDrAmt.dispose();
							lblTotalCrAmt.dispose();
							//grpVoucher.pack();
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
							//grpVoucher.pack();
							
						}
						else
						{
							
							
							totalRow();
							//grpVoucher.pack();
							//grpVoucher.pack();
						}

						return;
					}
					
					
					
					
					if(rowindex== 0 || rowindex < accounts.size()-1)
					{
						
						CrAmounts.get(rowindex).setText(nf.format(Double.parseDouble(CrAmounts.get(rowindex).getText())));
						DrAmounts.get(rowindex+1).setText(CrAmounts.get(rowindex).getText());	
						verifyFlag = true;
						totalCrAmount=Double.parseDouble(CrAmounts.get(rowindex).getText());
						totalDrAmount=totalCrAmount;
						
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
							//grpVoucher.pack();
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
							//grpVoucher.pack();
							
							
						}
						else
						{
							
							
							totalRow();
							//grpVoucher.pack();3
						}
						
						CrDrFlags.get(rowindex+1).select(0);
					}
					if(rowindex> 0)
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
							totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
							
						}
						
						for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
						{
							totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
							
						}
						
							if(totalCrAmount==totalDrAmount && totalCrAmount != 0.00 && totalDrAmount != 0.00)
							{
								lblTotal.dispose();
								lblTotalDrAmt.dispose();
								lblTotalCrAmt.dispose();
								//grpVoucher.pack();
								totalRow();
								//grpVoucher.pack();
								
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
							//grpVoucher.pack();
							addRow("Dr", balanceAmount);
							//grpVoucher.pack();
							totalRow();
							//grpVoucher.pack();
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
							//grpVoucher.pack();
							addRow("Cr", balanceAmount);
							//grpVoucher.pack();
							totalRow();
							//grpVoucher.pack();
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
					}			
*/				}			
			});
		
			accounts.get(rowcounter).addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					//super.keyPressed(arg0);
					Combo currentAccount=(Combo) arg0.widget;
				
					 final int rowindex=(Integer) currentAccount.getData("curindex");
					long now = System.currentTimeMillis();
					if (now > searchTexttimeout){
				         searchText = "";
				      }
					searchText += Character.toLowerCase(arg0.character);
					searchTexttimeout = now + 1000;					
					for(int i = 0; i < accounts.get(rowindex).getItemCount(); i++ )
					{
						if(accounts.get(rowindex).getItem(i).toLowerCase().startsWith(searchText ) ){
							//arg0.doit= false;
							accounts.get(rowindex).select(i);
							accounts.get(rowindex).notifyListeners(SWT.Selection ,new Event()  );
							break;
						}
					}
			
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'p')&& (rowindex > 0))
					{
						accounts.get(rowindex -1).setFocus();
						return;
					}
					if(((arg0.stateMask & SWT.CTRL) == SWT.CTRL) && (arg0.keyCode == 'n') && (rowindex < accounts.size() -1 ))
					{
						accounts.get(rowindex +1).setFocus();
						return;
					}
					
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
						
						if(accounts.get(rowindex).getItemCount() >1 && accounts.get(rowindex).getSelectionIndex() == 0||accounts.get(rowindex).getSelectionIndex() == -1 )
						{
							
							MessageBox msgaccerr = new MessageBox(new Shell(), SWT.ERROR |SWT.OK );
							msgaccerr .setMessage("Please select an account");
							msgaccerr.open();
							display.getCurrent().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									
									accounts.get(rowindex).setFocus();
								}
							});
							
							return;
							
						}
					
						
						
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
				/*	if(arg0.keyCode==SWT.DEL)
					{
						removeButton.get(rowindex).notifyListeners(SWT.Selection, new Event());
					}
		*/		}
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
						
						
						if(DrAmounts.get(DrAmounts.size()-1).getEnabled())
						{
							DrAmounts.get(DrAmounts.size()-1).setFocus();
							
						}
						if(CrAmounts.get(CrAmounts.size()-1).getEnabled())
						{
							CrAmounts.get(CrAmounts.size()-1).setFocus();
							
						}
						return;
					}
					if(editAmt==true)
					{
					if(totalCrAmount==totalDrAmount && DrAmounts.get(rowindex).getText()!="0.00")
					{
					
							btnsave.setVisible(true);
					}
					}
					else if(editAmt==false)
					{
						if(totalCrAmount!=totalDrAmount || DrAmounts.get(rowindex).getText()=="0.00")
						{
							btnsave.setVisible(false);
						}
					}
					/*if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == '.'))
					{
						
							removeButton.get(rowindex).setFocus();
							arg0.doit=false;
							return;
							
					}
					*/if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == ','))
					{
						
						accounts.get(rowindex).setFocus();
						arg0.doit=false;
						return;
					}
					if((arg0.keyCode==SWT.CR | arg0.keyCode==SWT.KEYPAD_CR) && rowindex	  == 0)
					{
						
						//DrAmounts.get(rowindex).notifyListeners(SWT.FocusOut, new Event());
						CrDrFlags.get(rowindex+1).setFocus();
					}
					if((arg0.keyCode== SWT.CR| arg0.keyCode==SWT.KEYPAD_CR)&& rowindex == CrDrFlags.size()-1)
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
					if((arg0.keyCode== SWT.CR| arg0.keyCode==SWT.KEYPAD_CR) && rowindex < CrDrFlags.size()-1)
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
					
					if(totalCrAmount==totalDrAmount && CrAmounts.get(rowindex).getText()=="0.00")
					{
					
							btnsave.setVisible(true);
						}
						else if(totalCrAmount!=totalDrAmount || CrAmounts.get(rowindex).getText()=="0.00")
						{
							btnsave.setVisible(false);
						}
					
					
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
							DrAmounts.get(DrAmounts.size()-1).setFocus();
						}
						if(CrAmounts.get(rowindex+1).getEnabled())
						{
							CrAmounts.get(CrAmounts.size()-1).setFocus();
						}
						return;
					}
					
					//right side
					/*if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == '.'))
					{
							
							removeButton.get(rowindex).setFocus();
							arg0.doit=false;
							return;
					}
					*///left side
					if(((arg0.stateMask & SWT.SHIFT) == SWT.SHIFT) && (arg0.keyCode == ','))
					{
							
							accounts.get(rowindex).setFocus();
							arg0.doit=false;
							return;
					}
					
					
					if((arg0.keyCode==SWT.CR|arg0.keyCode==SWT.KEYPAD_CR) && rowindex ==0)
					{
						
						//CrAmounts.get(1).notifyListeners(SWT.FocusOut, new Event());
						CrDrFlags.get(rowindex+1).setFocus();
						System.out.println(Integer.toString(comboselprj.getItemCount()));
					}
					if((arg0.keyCode== SWT.CR| arg0.keyCode==SWT.KEYPAD_CR) && rowindex == CrDrFlags.size()-1)
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
					if((arg0.keyCode== SWT.CR | arg0.keyCode==SWT.KEYPAD_CR) && rowindex < CrDrFlags.size()-1)
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
		            case SWT.KEYPAD_DECIMAL:
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
		            case SWT.KEYPAD_DECIMAL:
		                return;
		        }
					if(arg0.keyCode==46)
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
					
			
				/*removeButton.get(rowcounter).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(CrDrFlags.size()<= 2)
					{
						return;
					}

					// TODO Auto-generated method stub
					//super.widgetSelected(arg0);
					Button btncurrentremove = (Button) arg0.widget;
					int rowindex =(Integer)  btncurrentremove.getData("curindex");
					if(rowindex==0 )
					{
						if(CrDrFlags.size()== 1 )
						{
							//txtvoucherno.setFocus();
							//CrDrFlags.get(rowindex+1).setFocus();
						}
						CrDrFlags.get(rowindex+1).setFocus();
						
					}
					if(rowindex > 0 )
					{
						CrDrFlags.get(rowindex -1).setFocus();
						
					}
					
			
			//voucherTable.remove(rowindex);
			//voucherTable.pack();
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
						removeButton.get(reset).setData("curindex", reset );
						
						
					}
					startFrom --;
					totalDrAmount=0.00;
					totalCrAmount=0.00;
					for(int drcounter=0; drcounter<DrAmounts.size();drcounter++)
					{
						totalDrAmount=totalDrAmount+Double.parseDouble(DrAmounts.get(drcounter).getText());
						
					}
					
					for(int crcounter=0; crcounter<CrAmounts.size();crcounter++)
					{
						totalCrAmount=totalCrAmount+Double.parseDouble(CrAmounts.get(crcounter).getText());
						
					}
					lblTotal.dispose();
					lblTotalDrAmt.dispose();
					lblTotalCrAmt.dispose();
					//totalRow();
					grpVoucher.pack();
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
			//voucherTable.remove(voucherTable.getItemCount() - 2);
			//GridData gd=new GridData();
		FormData tfd = new FormData();
			
			/*gd.horizontalSpan = 2;
			gd.widthHint = 28 * totalWidth / 100;*/
			lblTotal= new Label(grpVoucher, SWT.CENTER);
			lblTotal.setText("Total");
			lblTotal.setFont(new Font(display, "Times New Roman",16,SWT.CENTER));
			tfd.top = new FormAttachment(currenttop);
			tfd.left = new FormAttachment(crdrleft);
			tfd.right = new FormAttachment(accountsright);
			tfd.bottom = new FormAttachment(currenttop + incrementby);
			//lblTotal.setFont(new Font(display,15));
			
			
			lblTotal.setLayoutData(tfd);
			
			
			lblTotalDrAmt = new Label(grpVoucher,SWT.BORDER| SWT.RIGHT);
			lblTotalDrAmt.setText( nf.format(totalDrAmount));
			tfd = new FormData();
			tfd.top = new FormAttachment(currenttop);
			tfd.left = new FormAttachment(dramountleft);
			tfd.right = new FormAttachment(dramountright);
			tfd.bottom = new FormAttachment(currenttop + incrementby);
        	lblTotalDrAmt.setLayoutData(tfd);
			
			lblTotalCrAmt = new Label(grpVoucher,SWT.BORDER | SWT.RIGHT);
			lblTotalCrAmt.setText( nf.format(totalCrAmount));
			tfd = new FormData();
			//gd.horizontalAlignment= SWT.CENTER;
			tfd.top = new FormAttachment(currenttop);
			tfd.left = new FormAttachment(cramountleft);
			tfd.right = new FormAttachment(cramountright);
			tfd.bottom = new FormAttachment(currenttop + incrementby);
			lblTotalCrAmt.setLayoutData(tfd);
			//grpVoucher.pack();
			grpVoucher.layout();
		
			
			totalRowCalled= true;
	}

	public static void main(String[] args) {
		Display d = new Display();
		Shell s = new Shell(d);
		AddNewVoucherComposite anvc = new AddNewVoucherComposite(s, SWT.NONE,
				"Contra");

		anvc.setSize(s.getClientArea().width, s.getClientArea().height);

		// s.setSize(400, 400);
		s.pack();
		s.open();
		while (!s.isDisposed()) {
			if (!d.readAndDispatch()) {
				d.sleep();
			}
		}

	}
}

