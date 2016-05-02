package my.parakeet;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class ParakeetActivity extends Activity {
    /** Called when the activity is first created. */

	private boolean CurrentModifierPositive = false;
	
	private TextView txtDisplayText;
	private TextView txtEstimatedSavings;
	private TextView txtRealSavings;
	private TextView txtCurrSpent;
	private TextView txtRemaining;
	private TextView txtDailyAmount;
	private TextView txtEstimatedDaily;
	private TextView txtTodayRecords;
	private TextView txtErrorMessage;
	private TextView btnToggle;
	private TextView btnSave;
	private TextView txtInstanceSpent;
	
	private ArrayList<MoneyEntry> MoneyToday = new ArrayList<MoneyEntry>();
	private ArrayList<MoneyEntry> MoneyMonth = new ArrayList<MoneyEntry>();
	private ArrayList<MoneyEntry> MoneyOld = new ArrayList<MoneyEntry>();
	
	
	private double dailyAmount = 0;
	private double dailyModifier = 0;
	private MoneyEntry currModifier = new MoneyEntry(0);
	private double total = 0;
	private double estDaily = 0;
	private double spentSoFar = 0;
	final private String xmlLocation = "Monies.xml"; //Update to file location
	final private String xmlBase = "/sdcard/Parakeet/";
	
	public final static int VIEW_TODAY = 1;
	public final static int VIEW_MONTH = 2;
	public final static int VIEW_OLD = 3;
	public final static int MENU_EXIT = 4;
	public final static int MENU_CANCEL = 5;
	
	//final private String xmlOld = "test_old"; //Update to file location
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.txtDisplayText = (TextView) findViewById(R.id.txtMainDisplay);
        this.txtEstimatedSavings = (TextView) findViewById(R.id.TextView05);
        this.txtRealSavings = (TextView) findViewById(R.id.TextView04);
        this.txtCurrSpent = (TextView) findViewById(R.id.TextView06);
        this.txtRemaining = (TextView) findViewById(R.id.TextView03);
        this.txtDailyAmount = (TextView) findViewById(R.id.TextView01);
        this.txtEstimatedDaily = (TextView) findViewById(R.id.TextView02);
        this.txtErrorMessage = (TextView) findViewById(R.id.TextView08);
        this.txtInstanceSpent = (TextView) findViewById(R.id.TextView09);
        this.txtTodayRecords = (TextView) findViewById(R.id.TextView07);
        this.txtTodayRecords.setText("");
        
        this.btnToggle = (TextView) findViewById(R.id.btnToggle);
        this.btnSave = (TextView) findViewById(R.id.btnSave);
        
		LoadSampleData();
		//WriteXML();
		
		//ReadXML();    
		LoadData();
		CalculateDailyAmount();
    }
    
    public void LoadSampleData() {
    	MoneyToday.add(new MoneyEntry(1415, "Paycheck1", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(1415, "Paycheck2", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(450, "Rent", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(150, "Rent", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(150, "Rent", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-150, "Gas Bill", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-350, "Car Payment", new Date(100, 0, 1), "00", "14", "00"));
    	MoneyToday.add(new MoneyEntry(-26, "Netflix", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-100, "Bike Insurance", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-350, "Bike Payment", new Date(100, 0, 1), "00", "14", "00"));
    	MoneyToday.add(new MoneyEntry(-75, "Cable", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-200, "Student Loans", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-1200, "Mortgage", new Date(100, 0, 1), "00", "14", "00"));
    	MoneyToday.add(new MoneyEntry(0, "Credit Card", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-86, "Sprint", new Date(100, 0, 1), "00", "14", "00"));
    	MoneyToday.add(new MoneyEntry(-100, "Electric", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-30, "Water", new Date(100, 0, 1), "00", "01", "00"));
    	MoneyToday.add(new MoneyEntry(-12, "Warcraft", new Date(100, 0, 1), "00", "01", "00"));
    	
    	//Daily Replications
    	MoneyToday.add(new MoneyEntry(-1, "", new Date(100, 5, 1), "xx", "xx", "xx"));
    	MoneyToday.add(new MoneyEntry(-25, "", new Date(100, 5, 1), "xx", "xx", "xx"));
    	MoneyToday.add(new MoneyEntry(-3, "", new Date(100, 5, 1), "xx", "xx", "xx"));
    	MoneyToday.add(new MoneyEntry(-2, "", new Date(100, 5, 1), "xx", "xx", "xx"));
    	MoneyToday.add(new MoneyEntry(-5, "", new Date(100, 5, 1), "xx", "xx", "xx"));
    	MoneyToday.add(new MoneyEntry(-1, "", new Date(100, 5, 2), "xx", "xx", "xx"));
    	MoneyToday.add(new MoneyEntry(-4, "", new Date(100, 5, 2), "xx", "xx", "xx"));
    }
    
    public void LoadData() {
    	total = 0;
        estDaily = 0;
        spentSoFar = 0;
        for(int i = 0; i < this.MoneyMonth.size(); i++) {
        	MoneyEntry m = MoneyMonth.get(i);
        	if(!(m.getOccDate() == null || m.getOccDate().equals(new Date(100, 0, 1)))) {
        		spentSoFar += m.Amount;
        	}
        	else {
        		total += m.Amount;
        	}
        }
        
        for(int i = 0; i < this.MoneyToday.size(); i++) {
        	MoneyEntry m = MoneyToday.get(i);
        	this.dailyModifier += m.Amount;
        }
        //this.txtErrorMessage.setText("total: " + total + " estDaily: " + estDaily + " spentSoFar: " + spentSoFar + " dailyModifier: "  + dailyModifier);
    }
    public void CalculateDailyAmount() {
    	Date today = new Date();
    	Calendar cal = new GregorianCalendar(today.getYear(), today.getMonth(), 1);
    	int daysLeft = (cal.getActualMaximum(Calendar.DAY_OF_MONTH) - today.getDate()) + 1;
    	
    	dailyAmount = (total + spentSoFar) / daysLeft;
    	txtEstimatedSavings.setText("Proj: " + CurrencyFormat((spentSoFar + dailyModifier) / today.getDate()) + " -> " + CurrencyFormat((total + (((spentSoFar + dailyModifier) * cal.getActualMaximum(Calendar.DAY_OF_MONTH)) / today.getDate()))));
    	if(daysLeft > 1) {
    		estDaily = (total + spentSoFar + dailyModifier) / (daysLeft - 1);
    		txtRealSavings.setText("Real: " + CurrencyFormat((spentSoFar / today.getDate()) - 1) + " -> " + CurrencyFormat(total + ((spentSoFar * cal.getActualMaximum(Calendar.DAY_OF_MONTH)) / (today.getDate() - 1))));
    	}
    	else {
    		estDaily = (total + spentSoFar + dailyModifier);
    		txtRealSavings.setText("Real: " + CurrencyFormat(spentSoFar / today.getDate()) + " -> " + CurrencyFormat(total + ((spentSoFar * cal.getActualMaximum(Calendar.DAY_OF_MONTH)) / (today.getDate()))));
    	}
    	
    	this.txtCurrSpent.setText("Current Spending: " + CurrencyFormat(dailyModifier));
    	this.txtRemaining.setText("Remaining: " + CurrencyFormat(total + spentSoFar + dailyModifier) + " over " + daysLeft + " days");
    	this.txtDailyAmount.setText("Daily: " + CurrencyFormat(dailyAmount));
    	this.txtEstimatedDaily.setText("Projected Daily: " + CurrencyFormat(estDaily));
    	DisplayAmount();
    }
 
/*
 * Private Sub SaveCurrent(Optional ByVal NewDesc As String = "", Optional ByVal NewAmount As Double = 0)
        If NewDesc.Length > 0 Then
            currModifier.Description = NewDesc
        End If
        currModifier.TimeStamp = Date.Now

        If currModifier.Amount <> 0 Then
            Me.lstToday.Items.Add(currModifier)
            WriteXML()
        End If
        Application.Exit()
    End Sub
 */
    
    public void SaveCurrent() {
    	SaveCurrent("");
    }
    
    public void SaveCurrent(String NewDesc) {
    	if(NewDesc.length() > 0) {
    		currModifier.Description = NewDesc;
    	}
    	currModifier.TimeStamp = new Date();
    	
    	if(currModifier.Amount != 0) {
    		MoneyToday.add(currModifier);
    		WriteXML();
    	}
    	this.finish();
    }
    
    public void ChangeCurrentValue(double change) {
    	this.dailyModifier += change;
    	this.currModifier.Amount += change;
    	setCurrentMsg();
    	if(currModifier.Amount != 0) {
    		btnSave.setText("Save/Exit");
    	}else{
    		btnSave.setText("Exit");
    	}
    	
    	CalculateDailyAmount();
    }
    
    public void DisplayAmount() {
    	this.txtDisplayText.setText(CurrencyFormat(this.dailyAmount + this.dailyModifier));
    }
    
    public String CurrencyFormat(double inValue) {
    	String rtnStr = "";
    	
    	rtnStr = new java.text.DecimalFormat("$0.00").format(inValue);
    	
    	return rtnStr;
    }
    public int TranslateValue(int AbsoluteValue) {
    	int rtnInt;
    	
    	if(CurrentModifierPositive) {
    		rtnInt = AbsoluteValue;
    	}
    	else {
    		rtnInt = -AbsoluteValue;
    	}
    	
    	return rtnInt;
    }
    
    public void ReadXML() {
    	this.MoneyToday.clear();
    	this.MoneyMonth.clear();
    	this.MoneyOld.clear();
    	
    	File f = new File(this.xmlBase + this.xmlLocation);
    	if(f.exists()) {
    		try {
    		
    			FileReader fr = new FileReader(this.xmlBase + this.xmlLocation);
		    	BufferedReader br = new BufferedReader(fr);
		    	
		    	String mainXML = "";
		    	mainXML = br.readLine();
	    	
	    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    		DocumentBuilder db = dbf.newDocumentBuilder();
	    		InputSource is = new InputSource();
		        is.setCharacterStream(new StringReader(mainXML));
	    		Document doc = db.parse(is);
	    		
	            NodeList nodes = doc.getElementsByTagName("money");

	    		for (int i = 0; i < nodes.getLength(); i++) {
	    	           Element element = (Element) nodes.item(i);
	    	           //SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	    	           
	    	           String ThisDate = element.getAttribute("time_stamp");
	    	           if(ThisDate.indexOf("/") == 1){
	    	        	   ThisDate = "0" + ThisDate;
	    	           }
	    	           
	    	           String NewDate = "";
	    	           for(int j = 0; j < ThisDate.split("/").length; j++){
	    	        	   switch(j){
	    	        	   case 0:
	    	        	   case 1:
	    	        		   if(ThisDate.split("/")[j].length() == 1){
	    	        			   NewDate += "0" + ThisDate.split("/")[j] + "/";
	    	        		   }else{
	    	        			   NewDate += ThisDate.split("/")[j] + "/";
	    	        		   }
	    	        		   break;
	    	        	   case 2:
	    	        		   String TempDate = NewDate + ThisDate.split("/")[j];
	    	        		   if(TempDate.split(" ")[0].length() == 8){
	    	        			   NewDate += "20" + ThisDate.split("/")[j];
	    	        		   }else{
	    	        			   NewDate += ThisDate.split("/")[j];
	    	        		   }
	    	        		   break;
	    	        	   }
	    	           }
	    	           
	    	           Date d = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").parse(NewDate);
	    	           MoneyEntry m = new MoneyEntry(Double.valueOf(element.getAttribute("amount")), element.getAttribute("desc"), d, element.getAttribute("occ_month"), element.getAttribute("occ_day"), element.getAttribute("occ_year"));
	    	           
	    	           
	    	           
	    	           boolean added = false;
	    	           if((m.getOccDate().getMonth() == new Date().getMonth() && m.getOccDate().getYear() == new Date().getYear() && m.getOccDate().getDate() != new Date().getDate()) || m.getOccDate() == null || m.getOccDate() == new Date(100, 0, 1)) {
	    	        	   this.MoneyMonth.add(m);
	    	        	   added = true;
	    	        	   //this.txtTodayRecords.setText("M: " + m.ToString() + "\n" + this.txtTodayRecords.getText());
	    	           }
	    	           
	    	           if(m.getOccDate().getMonth() == new Date().getMonth() && m.getOccDate().getYear() == new Date().getYear() && m.getOccDate().getDate() == new Date().getDate()) {
	    	        	   this.MoneyToday.add(m);
	    	        	   added = true;
	    	        	   //this.txtTodayRecords.setText("D: " + m.ToString() + "\n" + this.txtTodayRecords.getText());
	    	           }
	    	           
	    	           if(!added) {
	    	        	   this.MoneyOld.add(m);
	    	        	   //this.txtTodayRecords.setText("O: " + m.ToString() + "\n" + this.txtTodayRecords.getText());
	    	           }
	    	           
	    	        }
	    		DisplayItems(VIEW_TODAY);

	    	} catch (Exception e) {
	    		setError(e.getMessage());
	    	}
    	}
    }
    
    public void DisplayItems(int TimeFrame){
    	this.txtTodayRecords.setText("");
    	String TextHeader = "Today:";
    	ArrayList<MoneyEntry> InMoney = this.MoneyToday;
    	
    	switch(TimeFrame){
    	case VIEW_TODAY:
    		InMoney = this.MoneyToday;
    		TextHeader = "-- Today --";
    		break;
    	case VIEW_MONTH:
    		InMoney = this.MoneyMonth;
    		TextHeader = "-- Month --";
    		break;
    	case VIEW_OLD:
    		InMoney = this.MoneyOld;
    		TextHeader = "-- Older --";
    		break;
    	}
    	
    	for(int i = 0; i < InMoney.size(); i++){
    		MoneyEntry m = InMoney.get(i);
    		this.txtTodayRecords.setText(m.ToString() + "\n" + this.txtTodayRecords.getText());
    	}
    	
    	this.txtTodayRecords.setText(TextHeader + "\n" + this.txtTodayRecords.getText());
    }
    public void WriteXML() {
    	try {
    		FileWriter fw = new FileWriter(this.xmlBase + this.xmlLocation);
        	
        	String xmlDoc = "";
        	
        	xmlDoc = "<xml>";
        	
            for(int i = 0; i < this.MoneyToday.size(); i++) {
            	xmlDoc += MoneyToday.get(i).ToXML();
            }
            for(int i = 0; i < this.MoneyMonth.size(); i++) {
            	xmlDoc += MoneyMonth.get(i).ToXML();
            }
            
            xmlDoc += "</xml>";
        	
        	fw.write(xmlDoc);
        	fw.flush();
        	fw.close();
        	
        	if(this.MoneyOld.size() > 0){
        		String xmlOld = String.valueOf(Calendar.YEAR) + "_";
        		
        		if(String.valueOf(Calendar.MONTH).length() == 1){
        			xmlOld += "0";
        		}
        		xmlOld += String.valueOf(Calendar.MONTH) + "_";
        		
        		if(String.valueOf(Calendar.DAY_OF_MONTH).length() == 1){
        			xmlOld += "0";
        		}
        		xmlOld += String.valueOf(Calendar.DAY_OF_MONTH);
        		
        		fw = new FileWriter(this.xmlBase + xmlOld + ".xml");
        		
        		xmlDoc = "<xml>";
        		for(int i = 0; i < this.MoneyOld.size(); i++) {
                	xmlDoc += MoneyOld.get(i).ToXML();
                }
        		xmlDoc += "</xml>";
            	
            	fw.write(xmlDoc);
            	fw.flush();
            	fw.close();
        	}
        	
    	} catch (Exception e) {
    		setError(e.getMessage());
    	}
    }
    
    public void setCurrentMsg() {
    	if(currModifier.Amount != 0){
    		this.txtInstanceSpent.setText(CurrencyFormat(currModifier.Amount));
    		this.txtInstanceSpent.setVisibility(View.VISIBLE);	
    	}else{
    		this.txtInstanceSpent.setVisibility(View.INVISIBLE);
    	}
    }
    
    public void setError(String errorMsg) {
    	this.txtErrorMessage.setVisibility(View.VISIBLE);
    	if(this.txtErrorMessage.getText().length() > 0) {
    		this.txtErrorMessage.setText(this.txtErrorMessage.getText() + errorMsg);
    	} else {
    		this.txtErrorMessage.setText(errorMsg);
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	SubMenu View = menu.addSubMenu("View");
    	View.add(0, VIEW_TODAY, 0, "Today");
    	View.add(0, VIEW_MONTH, 1, "Month");
    	View.add(0, VIEW_OLD, 2, "Old");
    	View.add(0, MENU_CANCEL, 3, "Cancel");
        menu.add(0, MENU_EXIT, 0, "Exit");
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case VIEW_TODAY:
        case VIEW_MONTH:
        case VIEW_OLD:
        	DisplayItems(item.getItemId());
        	break;
        case MENU_EXIT:
        	this.finish();
        	break;
        case MENU_CANCEL:
        	break;
        }
        return false;
    }
    public void btnToggle_Click(View view) {
    	CurrentModifierPositive = !CurrentModifierPositive;
    	if(CurrentModifierPositive) {
    		btnToggle.setText("+");
    	} else {
    		btnToggle.setText("-");
    	}
    }
    
    public void btnDollarsOne_Click(View view) {
    	ChangeCurrentValue(TranslateValue(1));
    }
    public void btnDollarsFive_Click(View view) {
    	ChangeCurrentValue(TranslateValue(5));
    }
    public void btnDollarsTen_Click(View view) {
    	ChangeCurrentValue(TranslateValue(10));
    }
    public void btnSave_Click(View view) {
	    SaveCurrent();
    }
}

