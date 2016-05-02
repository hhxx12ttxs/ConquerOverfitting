package com.wwci.bigdeal.activity;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


import com.wwci.bigdeal.R;
import com.wwci.bigdeal.db.DbAdapter;
import com.wwci.bigdeal.model.Purchase;
import com.wwci.bigdeal.model.PurchaseItem;
import com.wwci.bigdeal.uitl.OrderAdapter;
import com.wwci.bigdeal.uitl.StringTools;
import com.wwci.bigdeal.uitl.ToastUtil;
import com.wwci.bigdeal.BigDealApplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class OrderEditActivity extends ListActivity {
    /** Called when the activity is first created. */
	private static final int PICK_CONTACT = 3;
	
	private TextView TV_lbl_Total;
	private TextView TV_Total;
	private TextView TV_Client;
	private TextView TV_ClientPhone;
	private TextView TV_Date;
	private TextView TV_DueDate;
	private TextView TV_InvoiceNo;
	private TextView TV_InvoiceStatus;
	private EditText ET_Prepayment;
	
	private Button btn_Back;
	private Button btn_Cancel;
	private Button btn_Settle;
	private Button btn_Settle_Email;
	private Button btn_Add;
	private Button btn_pick;
	
	
	protected ArrayList<HashMap<String, String>> m_orders;
	private OrderAdapter m_adapter;

	private DbAdapter mDbHelper;
	protected Cursor cursor;
	private Intent i;
	protected HashMap<String, String> productHash;
	
	private int mYear;
	private int mMonth;
	private int mDay;
	 
    static final int DATE_DIALOG_ID0 = 0;
    static final int DATE_DIALOG_ID1 = 1;
	    
    private SharedPreferences settings;
	
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.purchase_order);
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
//        productHash = (HashMap<String, String>)this.getIntent().getSerializableExtra("item");  
        
        initUI();
        setUIValue();
        setListener();
        
                 
    }
    
    private void initUI(){
    	TV_InvoiceNo = (TextView) findViewById(R.id.no);
    	TV_InvoiceNo.setVisibility(View.VISIBLE);
    	TV_InvoiceStatus = (TextView) findViewById(R.id.status);
    	
    	TV_lbl_Total = (TextView) findViewById(R.id.lbl_total);
    	TV_Total = (TextView) findViewById(R.id.total);
    	TV_Client = (TextView) findViewById(R.id.client);
    	TV_ClientPhone = (TextView) findViewById(R.id.clientPhone);
    	TV_Date = (TextView) findViewById(R.id.purchasedate);
    	TV_DueDate = (TextView) findViewById(R.id.duedate);
    	ET_Prepayment = (EditText) findViewById(R.id.prepayment);
    	
    	btn_Back = (Button) findViewById(R.id.back);
    	btn_Cancel = (Button) findViewById(R.id.cancel);
    	btn_Settle = (Button) findViewById(R.id.settle);
    	btn_Settle_Email = (Button) findViewById(R.id.saveAndEmail);
    	btn_Add = (Button) findViewById(R.id.add);    	
    	btn_pick = (Button) findViewById(R.id.pickSupplier);
    	btn_pick.setText(R.string.client);
    	
    }
   
    private void setUIValue(){    	
    	//get currency symbol
    	settings = getSharedPreferences("Preference", 0);
    	if (StringTools.isNullOrBlank(settings.getString("currency", "")))
    		TV_lbl_Total.setText(getString(R.string.total)+"("+settings.getString("currency", "")+")");
    	 	   
    	 	    
        productHash = (HashMap<String, String>)((BigDealApplication)getApplication()).getPurchase();        
        TV_InvoiceNo.setText(productHash.get("no"));
    	TV_Total.setText(StringTools.toFormatDouble(productHash.get("amount")));
    	TV_Client.setText(productHash.get("client"));
    	TV_ClientPhone.setText(productHash.get("clientphone"));
        TV_Date.setText(productHash.get("orderDate"));
        TV_DueDate.setText(productHash.get("dueDate"));        
        TV_InvoiceStatus.setText(productHash.get("status"));
        ET_Prepayment.setText(productHash.get("paid"));
        
		m_orders = (ArrayList<HashMap<String, String>>)((BigDealApplication)getApplication()).getList().clone();
		double total =0.0;
		if (!m_orders.isEmpty()){
			Collections.reverse(m_orders);			
			HashMap<String, String> productitem = new HashMap<String, String>();
			for (int i=0;i<m_orders.size();i++){
				productitem= m_orders.get(i);
				total+=Double.parseDouble(productitem.get("product_subtotal"));
						
			}
			this.m_adapter = new OrderAdapter(this, R.layout.purchase_order_list_item, m_orders);
	        setListAdapter(this.m_adapter);
		}
		
		TV_Total.setText(StringTools.toFormatDouble(total+""));
    }
    private void setListener(){
    	
    	TV_Date.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 showDialog(DATE_DIALOG_ID0);
			}
		});
		
    	TV_DueDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 showDialog(DATE_DIALOG_ID1);
			}
		});    	
    	btn_Back.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				i = new Intent(OrderEditActivity.this,TabView.class);
//				i.putExtra("activateTab", "2");
//		    	startActivity(i);
		    	finish();
			}
		});
    	
    	btn_Cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!m_orders.isEmpty()){
					cancelAlert();
				}else{
					ToastUtil.getCustomToast(OrderEditActivity.this,getString(R.string.warning_empty_list));
				}
				
			}
		});
    	
    	btn_Add.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((BigDealApplication)getApplication()).setStatus(0);
				i = new Intent(OrderEditActivity.this,OrderProductEditActivity.class);
		    	startActivity(i);
		    	finish();
			}
		});
    	
    	btn_Settle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (vaildate()){
					if (saveOrder()) {
						((BigDealApplication)getApplication()).setList(new ArrayList<HashMap<String, String>>());
						((BigDealApplication)getApplication()).setPosition(0);
						((BigDealApplication)getApplication()).setStatus(0);
						ToastUtil.getCustomToast(OrderEditActivity.this,getString(R.string.info_sucess));
//				    	i = new Intent(OrderEditActivity.this,TabView.class);
//				    	i.putExtra("activateTab", "2");
//				    	startActivity(i);
						finish();
					}
				}
			}
		});
    	
    	btn_Settle_Email.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (vaildate()){
					email();
				}
			}
		});
    	
    	
    	btn_pick.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), PICK_CONTACT);
			}
		});
    }
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);			
		((BigDealApplication)getApplication()).setPosition(m_orders.size()-position-1);
    	((BigDealApplication)getApplication()).setStatus(1);
    	i = new Intent(OrderEditActivity.this,OrderProductEditActivity.class);
    	startActivity(i);
    	finish();
    	
	}
    private void cancelAlert() {
		AlertDialog.Builder dialog=new AlertDialog.Builder(OrderEditActivity.this);
		dialog.setMessage("Are you sure to cancel?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	cancelList(); 
            }
        });
        dialog.setNegativeButton("No",new DialogInterface.OnClickListener() {
 

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
 
			}
		});
        dialog.show();
	}
    private void cancelList(){
    	mDbHelper.cancelResumeOrder(TV_InvoiceNo.getText().toString(),"Canceled");
    	((BigDealApplication)getApplication()).setList(new ArrayList<HashMap<String, String>>());
    	((BigDealApplication)getApplication()).setPosition(-1);
    	((BigDealApplication)getApplication()).setStatus(0);      	
    	i = new Intent(OrderEditActivity.this,OrderActivity.class);
    	startActivity(i);
    	finish();
    	
    }
    
    private boolean vaildate(){
    	if (StringTools.isNullOrBlank(TV_Client.getText().toString())){
    		ToastUtil.getCustomToast(this,getString(R.string.warning_empty_product_name)+" " + getString(R.string.client));
    		return false;
    	}
    	    	    	
        if (((BigDealApplication)getApplication()).getList().size()<=0){
        	ToastUtil.getCustomToast(this,getString(R.string.warning_empty_list));
    		return false;
    	}
        
        if (StringTools.toDate(TV_Date.getText().toString(), "yyyy-MM-dd").toString().compareTo(StringTools.toDate(TV_DueDate.getText().toString(), "yyyy-MM-dd").toString())>0){
        	ToastUtil.getCustomToast(this,getString(R.string.warning_wrong_date_range));
        	return false;
        }
        
        if (!StringTools.isNullOrBlank(ET_Prepayment.getText().toString())){
        	try{
        		double value = Double.parseDouble(ET_Prepayment.getText().toString());        		
        		if (value>Double.parseDouble(TV_Total.getText().toString().replaceAll(",", ""))){
        			ToastUtil.getCustomToast(this,getString(R.string.warning_invaild_prepayment));
            		return false;
        		}
        	}catch(Exception e){
        		ToastUtil.getCustomToast(this,getString(R.string.warning_invaild_prepayment));
        		return false;
        		
        	}
        }
        
        return true;
    }

    private boolean saveOrder(){
    	Purchase purchase = new Purchase();
    	purchase.setPurchaseNo(TV_InvoiceNo.getText().toString());
    	purchase.setAmount(TV_Total.getText().toString().replaceAll(",", ""));
    	purchase.setPaid(ET_Prepayment.getText().toString());
    	purchase.setPurchaseDate(StringTools.toConvertDateFormat(TV_Date.getText().toString(), "MM/dd/yyyy","yyyy-MM-dd"));
    	purchase.setDueDate(StringTools.toConvertDateFormat(TV_DueDate.getText().toString(), "MM/dd/yyyy","yyyy-MM-dd"));
    	purchase.setStatus("In Progress");    	
    	purchase.setClient(TV_Client.getText().toString());
    	purchase.setClientPhone(TV_ClientPhone.getText().toString());
    	
    	if (!StringTools.isNullOrBlank(purchase.getPaid()) && Double.parseDouble(purchase.getPaid())==Double.parseDouble(purchase.getAmount())){
    		purchase.setStatus("Invoiced");
    	}
    	
    	ArrayList<PurchaseItem> items = new ArrayList<PurchaseItem>();
    	PurchaseItem item;
    	HashMap<String, String> productitem = new HashMap<String, String>();
    	ArrayList<HashMap<String, String>> orders = (ArrayList<HashMap<String, String>>)((BigDealApplication)getApplication()).getList();
    	for (int i=0;i<orders.size();i++){
    		productitem = orders.get(i);
    		item = new PurchaseItem();
    		item.setPurchaseNo(TV_InvoiceNo.getText().toString());
    		item.setProductID(productitem.get("product_id"));
    		item.setPrice(productitem.get("product_cost"));
    		item.setQty(productitem.get("product_qty"));
    		item.setSubtotal(productitem.get("product_subtotal"));
    		item.setReceivedDate(productitem.get("product_receivedDate"));
    		items.add(item);
    	}
    	purchase.setPurchaseItem(items);
    	
    	return mDbHelper.updateOrder(purchase);
    	
    }
    
    public void email(){
		 File fileIn;
		 Uri u;
		 String filename;;
		 ArrayList<Uri> uris = new ArrayList<Uri>();
			    	
	     Purchase purchase = new Purchase();
	     purchase.setPurchaseNo(TV_InvoiceNo.getText().toString());
	     purchase.setAmount(TV_Total.getText().toString().replaceAll(",", ""));
	     purchase.setPaid(ET_Prepayment.getText().toString());
	     purchase.setPurchaseDate(StringTools.toConvertDateFormat(TV_Date.getText().toString(), "MM/dd/yyyy","yyyy-MM-dd"));
	     purchase.setDueDate(StringTools.toConvertDateFormat(TV_DueDate.getText().toString(), "MM/dd/yyyy","yyyy-MM-dd"));
	     purchase.setStatus("In Progress");
	     purchase.setClient(TV_Client.getText().toString());
	     purchase.setClientPhone(TV_ClientPhone.getText().toString());
	    
	 	if (!StringTools.isNullOrBlank(purchase.getPaid()) && Double.parseDouble(purchase.getPaid())==Double.parseDouble(purchase.getAmount())){
    		purchase.setStatus("Invoiced");
    	}
	 	
	     StringBuilder content = new StringBuilder();
		 content.append("Dear Mr or Ms ").append(TV_Client.getText().toString()).append(":<br><br>");	     
	     content.append("Summary of order from ").append(TV_Client.getText().toString()).append(" <br>");
	     content.append("Order # ").append(TV_InvoiceNo.getText().toString()).append(" Order Date: ").append(TV_Date.getText().toString()).append(" Due Date: ").append(TV_DueDate.getText().toString()).append("<br><br>");
			
	     
	     ArrayList<PurchaseItem> items = new ArrayList<PurchaseItem>();
	     PurchaseItem item;
	     HashMap<String, String> productitem = new HashMap<String, String>();
	     ArrayList<HashMap<String, String>> orders = (ArrayList<HashMap<String, String>>)((BigDealApplication)getApplication()).getList();
	     double total =0.0;
	     for (int i=0;i<orders.size();i++){
	    	 productitem = orders.get(i);
	    	 item = new PurchaseItem();
	    	 item.setPurchaseNo(TV_InvoiceNo.getText().toString());
	    	 item.setProductID(productitem.get("product_id"));
	    	 item.setPrice(productitem.get("product_cost"));
	    	 item.setQty(productitem.get("product_qty"));
	    	 item.setSubtotal(productitem.get("product_subtotal"));
	    	 item.setReceivedQty(productitem.get("product_receivedQty"));
	    	 item.setReceivedDate(productitem.get("product_receivedDate"));
	    	 items.add(item);
	    	 
	    	 content.append(i+1).append(". ")
		     .append("<b>name:</b> ").append(productitem.get("product_name")).append(" | ")
		     .append("<b>Desc.:</b> ").append(productitem.get("product_desc")).append(" | ")
		     .append("<b>price:</b> ").append(productitem.get("product_cost")).append(" | ")
		     .append("<b>quantity:</b> ").append(productitem.get("product_qty")).append(" | ")
		     .append("<b>subtotal:</b> ").append(productitem.get("product_subtotal"));
		     if (!StringTools.isNullOrBlank(productitem.get("product_path").toString())){
	    		filename = productitem.get("product_path").toString();
	    		fileIn = new File(filename);
		    	u = Uri.fromFile(fileIn);
		    	uris.add(u);
		    	content.append(" | ").append("<b>pic name:</b> ").append(filename.substring(filename.lastIndexOf('/')));
	    	 }
		     content.append("<br>");
		     total = total+Double.parseDouble(productitem.get("product_subtotal").toString());
	     }
	    purchase.setPurchaseItem(items);
	    
	 
	    	
	    if (mDbHelper.updateOrder(purchase)){
			((BigDealApplication)getApplication()).setList(new ArrayList<HashMap<String, String>>());
			((BigDealApplication)getApplication()).setPosition(0);
			((BigDealApplication)getApplication()).setStatus(0);
	    	//need to "send multiple" to get more than one attachment
	   		 final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
//	   		 emailIntent.setType("text/plain");
	   		 emailIntent.setType("text/html");
	   		 //to
	   		 emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});
	   		 //cc
//	   		 emailIntent.putExtra(android.content.Intent.EXTRA_CC, new String[]{"xxxx@gmail.com"});
	   		 //subject
	   		 emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Order# "+ TV_InvoiceNo.getText().toString() +" Confirmnation");
	           //content
	   		 emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(content.toString()));
	   		 //attachments
	   		 emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);		 
	   		 startActivity(Intent.createChooser(emailIntent, "Send mail..."));

	    }
	    	
	 

		 
   }
    
   public void onActivityResult(int reqCode, int resultCode, Intent data){
        super.onActivityResult(reqCode, resultCode, data);     
        switch(reqCode){
           case (PICK_CONTACT):
             if (resultCode == RESULT_OK){
            	 TV_Client.setText("");
            	 TV_ClientPhone.setText("");        		 
                 Uri contactData = data.getData();
                 Cursor c = managedQuery(contactData, null, null, null,null);
     
                 if (c.moveToFirst()){
                     // other data is available for the Contact.  I have decided to only get the name of the Contact.                                         
                     TV_Client.setText( c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)));
                     String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                     if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    	 Cursor pCur = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                    			 ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
                    			 new String[]{id}, null);
                         if (pCur.moveToFirst()){
                        	 TV_ClientPhone.setText(pCur.getString(pCur.getColumnIndexOrThrow(Phone.NUMBER)));
                        	 
                         }

                         pCur.close();
                     }
                 }
             }
        }
    }
	  // updates the date in the TextView
    private void updateFromDateView() {
    	TV_Date.setText(StringTools.toConvertDateFormat(new StringBuilder()
    	.append(mYear).append("-")
    	.append(mMonth + 1).append("-")
        .append(mDay).toString(),"yyyy-MM-dd","MM/dd/yyyy"));
    }
	  // updates the date in the TextView
    private void updateToDateView() { 	
    	TV_DueDate.setText(StringTools.toConvertDateFormat(new StringBuilder()
    	.append(mYear).append("-")
    	.append(mMonth + 1).append("-")
        .append(mDay).toString(),"yyyy-MM-dd","MM/dd/yyyy"));
    }  
	 @Override
	protected Dialog onCreateDialog(int id) {
		 switch (id) {
		 case DATE_DIALOG_ID0:
		      return new DatePickerDialog(this,fromDateSetListener,mYear, mMonth, mDay);        	              
	     case DATE_DIALOG_ID1:  
	          return new DatePickerDialog(this,toDateSetListener,mYear, mMonth, mDay);    		         
		 }
		 return null;
	}	
    
	// the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener fromDateSetListener = new DatePickerDialog.OnDateSetListener() {
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    		mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateFromDateView();
	    }
    };   
	// the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener toDateSetListener = new DatePickerDialog.OnDateSetListener() {
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    		mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateToDateView();
        }
    };  
	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

}




