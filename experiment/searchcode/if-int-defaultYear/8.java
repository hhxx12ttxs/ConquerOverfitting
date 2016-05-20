package com.demo_task;

import java.util.Calendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.demo_task.TaskDb;

public class TaskCreate extends Activity{
	
	private TaskDb db;
	private EditText content = null;
	private EditText detail = null;
	private TextView time_show;
	int mYear ,mMonth ,mDay ,mHour ,mMinute;
	StringBuilder time,defaultTime;
	String orderRec;
	
	private EditText mContentText;
	private EditText mContentText1;
	
	
	@Override
    public void onCreate(Bundle addTask) {
        super.onCreate(addTask);
        setContentView(R.layout.task_create);
        db = new TaskDb(this);
        db.open();
        setTitle(R.string.addtasktitle);
        content = (EditText)findViewById(R.id.task_content);
        detail = (EditText)findViewById(R.id.task_detail);
        time_show = (TextView)findViewById(R.id.date_show);
        
        Calendar calendar = Calendar.getInstance();
        int defaultYear = calendar.get(Calendar.YEAR);
        int defaultMonth = calendar.get(Calendar.MONTH) + 1;
        int defaultDay = calendar.get(Calendar.DATE);
        int defaultHour = calendar.get(Calendar.HOUR);
        int defaultMinute = calendar.get(Calendar.MINUTE);
        
        defaultTime = new StringBuilder().append(format(defaultYear))
		.append("/").append(format(defaultMonth))
		.append("/").append(format(defaultDay))
		.append("    ")
		.append(format(defaultHour)).append(R.string.hour).append(format(defaultMinute)).append(R.string.min);
        time_show.setText(defaultTime);
        
        Bundle extras = getIntent().getExtras();
        
		if (extras != null) {
			orderRec = extras.getString(TaskDb.ORDER);
			String contentRec = extras.getString(TaskDb.CONTENT);
			String detailRec = extras.getString(TaskDb.DETAIL);
			String dateRec = extras.getString(TaskDb.DATE);
			if (contentRec != null) {
				content.setText(contentRec);
			}
			if (detailRec != null) {
				detail.setText(detailRec);
			}
			if (dateRec != null) {
				time_show.setText(dateRec);
			}
			
		}
        
		mContentText = (EditText)findViewById(R.id.task_content);
		mContentText1 = (EditText)findViewById(R.id.task_detail);
		
		Button confirmButton = (Button) findViewById(R.id.confrirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String content = mContentText.getText().toString();
				String content1 = mContentText1.getText().toString();
				
				String alertString = "";

				if ("".equals(content.trim())) {
					alertString = "Content cannot be empty";
				} else if ("".equals(content1.trim())) {
					alertString = "Detail cannot be empty";
				} 

				if (!"".equals(alertString)) {
					new AlertDialog.Builder(TaskCreate.this).setTitle("Warning")
							.setMessage(alertString).setPositiveButton("Confirm",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialoginterface,
												int i) {
											return;
										}
									}).show();
					return;
				}
				insert();
			}

		});
        
        
    }
	

	

	
    
    
	private String format(int x)
	{
		String s = ""+x;
		if(s.length()==1)
			s = "0" + s;
		return s;
	}
	
	
	private String dateStr,contentStr = null,detaillStr,order; 
	
    public void insert()
    {
    	dateStr = time_show.getText().toString();
		if(dateStr!=null)
		{dateStr = time_show.getText().toString();}
		else
		{dateStr = defaultTime.toString();} 		
		contentStr = content.getText().toString();
		detaillStr = detail.getText().toString();

		if(orderRec != null)
		{
			order = orderRec;
			if(contentStr!=null)
			db.updateById(order, contentStr, dateStr, detaillStr);
			Toast.makeText(this, R.string.edit_sucess, Toast.LENGTH_SHORT).show();
			}
		else
	    	{order = String.valueOf(db.totalData());
    		db.insert(order,contentStr,dateStr,detaillStr);
    	Toast.makeText(this, R.string.add_sucess, Toast.LENGTH_SHORT).show();}
    	finish();
		
    	db.close();
    	setTitle(contentStr);

    }
    


}

