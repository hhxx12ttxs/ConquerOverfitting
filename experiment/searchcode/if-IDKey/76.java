/*
 * DeleteGroups
 *
 * Version 1.0
 * E-Learning Environment
 * DeleteGroups.java 
 * Handles activities related to deletion of groups
 */

package com.els.sliit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class DeleteGroups extends Activity {
	
	JSONArray jArray;
	String result = null;
	InputStream is = null;
	StringBuilder sb=null;
	// I use HashMap arraList which takes objects
	private ArrayList <HashMap<String, Object>> myContent;
	private static final String TITLE = "title";
	private static final String UPLOADER = "uploader";
	private static final String OPERATION = "operation";
	private static final String DESCRIPTION = "description";
	private static final String IDKEY = "id";
	String description;
	
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_groups);
        
        
        //addListenerOnAll();
        
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        manageGroups("http://10.0.2.2:1000/test/loadGroups.php",1,null);
       
            
        
    }
    
    
    /** load the groups which the particular user subscribed in.
     *  there user can leave or delete groups
     * */
    public void manageGroups(String url,int status,String para)
    {
    	description="";
    	ListView listView;
        myContent = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> hm;
        int stat=status;
        
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        //http post
        try{
             HttpClient httpclient = new DefaultHttpClient();
             HttpPost httppost = new HttpPost(url);
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
             HttpResponse response = httpclient.execute(httppost);
             HttpEntity entity = response.getEntity();
             is = entity.getContent();
             }catch(Exception e){
                 Log.e("log_tag", "Error in http connection"+e.toString());
            }
        //convert response to string
        if(stat==1)
        {
        	nameValuePairs.add(new BasicNameValuePair("username",GlobalClass.getUsername().toString()));
       	 try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
                }catch(Exception e){
                    Log.e("log_tag", "Error in http connection"+e.toString());
               }
        	listView = (ListView)findViewById(R.id.list);
         try{
              BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
               sb = new StringBuilder();
               sb.append(reader.readLine() + "\n");

               String line="0";
               while ((line = reader.readLine()) != null) {
                              sb.append(line + "\n");
                }
                is.close();
                result=sb.toString();
                }
                catch(Exception e){
                      Log.e("log_tag", "Error converting result "+e.toString());
                }
        //paring data
        String name;
        String descrip;
        String type;
        String id;
        String description;
        try{
      	 
              jArray = new JSONArray(result);
              JSONObject json_data=null;
              for(int i=0;i<jArray.length();i++){
            	     descrip="Uploaded by : ";
                     json_data = jArray.getJSONObject(i);
                     name=json_data.getString("group_name");
                     type=json_data.getString("creator");
                     descrip=descrip+json_data.getString("creator");
                     description=json_data.getString("description");
                     
                     id=json_data.getString("groupID");
                     hm = new HashMap<String, Object>();
                     hm.put(TITLE, name);
                     hm.put(UPLOADER, descrip);
                     hm.put(IDKEY, id);
                     hm.put(DESCRIPTION, description);
                     
                     if(type.equals(GlobalClass.getUsername().toString()))
                    	 hm.put(OPERATION, "Delete");
                     else
                    	 hm.put(OPERATION, "Leave");
                    
                     myContent.add(hm);
                     descrip="";
                 }
              }
              catch(JSONException e1)
              {
            	  Log.e("log_tag", e1.toString());
            	  Toast.makeText(getBaseContext(), "No Groups Available" ,Toast.LENGTH_LONG).show();
              } 
              catch (ParseException e1) 
              {
        			e1.printStackTrace();
        	  }        
        		
        listView.setAdapter(new myListAdapter(myContent,this,1));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        
        
       
               
    }
      
    /** Placing items in ListView according to a specific pattern 
     * by using holders. First, place a unit of data in a holder and then
     * the holder is passing to the ListView as a single ListView item 
     */
    private class myListAdapter extends BaseAdapter{
    	
    	
    	private ArrayList<HashMap<String, Object>> Content; 
    	private LayoutInflater mInflater;
    	int status;
    	
    	
		public myListAdapter(ArrayList<HashMap<String, Object>> content, Context context,int stat){
			status=stat;
			Content = content;
			mInflater = LayoutInflater.from(context);
		}
    	
    	
    	public int getCount() {
			// TODO Auto-generated method stub
			return Content.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return Content.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			
			
			// TODO Auto-generated method st
			// A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
			ViewHolder holder;
			
			// When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null
			
			 if (convertView == null) {
	             convertView = mInflater.inflate(R.layout.delete_listbox, null);
	             // Creates a ViewHolder and store references to the two children views
	             // we want to bind data to.
	             
	             holder = new ViewHolder();
	             holder.titleView = (TextView) convertView.findViewById(R.id.text1);
	             holder.upploaderView = (TextView) convertView.findViewById(R.id.text2);
	             holder.operation = (Button) convertView.findViewById(R.id.btnRemove);
	             holder.id = (TextView)convertView.findViewById(R.id.text3);
	             holder.description=(TextView)convertView.findViewById(R.id.text4);
	            
	             
	             
	             convertView.setTag(holder);
	                
			 }else {
				 // Get the ViewHolder back to get fast access to the views
				 holder = (ViewHolder) convertView.getTag(); 
			 }
			 	// Bind the data with the holder.
			 
				holder.titleView.setText((String) Content.get(position).get(TITLE));
				
				holder.upploaderView.setText((String) Content.get(position).get(UPLOADER));
				
				holder.operation.setText((String) Content.get(position).get(OPERATION));
				
				holder.id.setText((String) Content.get(position).get(IDKEY));
				
				holder.description.setText((String) Content.get(position).get(DESCRIPTION));
				
				
				Button btnRemove = (Button)convertView.findViewById(R.id.btnRemove);
				TextView groupID = (TextView)convertView.findViewById(R.id.text3);
				final String sss = btnRemove.getText().toString();
				final String id = groupID.getText().toString();

				//button click event to delete or leave a group
				btnRemove.setOnClickListener(new View.OnClickListener() {

					
					public void onClick(View v) {
						// Calls leaveCommunity method
						if(sss.equals("Delete"))
						{
							operation(sss,1,id);
						    
						}
						else
							operation(sss,2,id);
					}
				});
						
			
				return convertView;
		}
		
		/** This method remove or delete a group according to the action variable
		 *  
		 * */
		public void operation(String action,int type,String id)
		{
            final int status = type;
            final String fAction=action;
            final String gID = id;
			AlertDialog.Builder alert = new AlertDialog.Builder(DeleteGroups.this);

			alert.setTitle(action);
			alert.setMessage("You are about to "+action+" the group.");			
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int whichButton) {
					// Canceled.
				}
			});

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int whichButton) {
					if(status==1)
					{
						ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						//http post
						nameValuePairs.add(new BasicNameValuePair("groupID",gID));
						try{
							HttpClient httpclient = new DefaultHttpClient();
							HttpPost httppost = new HttpPost("http://10.0.2.2:1000/test/deleteGroups.php");
							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
							HttpResponse response = httpclient.execute(httppost);
							HttpEntity entity = response.getEntity();
							is = entity.getContent();
							Toast.makeText(getBaseContext(), "Successfully "+fAction ,Toast.LENGTH_LONG).show();
							manageGroups("http://10.0.2.2:1000/test/loadGroups.php",1,null);
						}catch(Exception e){
							Toast.makeText(getBaseContext(), "Error Occured" ,Toast.LENGTH_LONG).show();
							Log.e("log_tag", "Error in http connection"+e.toString());
							
						}
					}
					
					else if(status==2)
					{
						ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						//http post
						nameValuePairs.add(new BasicNameValuePair("groupID",gID));
						nameValuePairs.add(new BasicNameValuePair("username",GlobalClass.getUsername().toString()));
						try{
							HttpClient httpclient = new DefaultHttpClient();
							HttpPost httppost = new HttpPost("http://10.0.2.2:1000/test/leaveGroups.php");
							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
							HttpResponse response = httpclient.execute(httppost);
							HttpEntity entity = response.getEntity();
							is = entity.getContent();
							Toast.makeText(getBaseContext(), "Successfully "+fAction ,Toast.LENGTH_LONG).show();
							manageGroups("http://10.0.2.2:1000/test/loadGroups.php",1,null);
						}catch(Exception e){
							Toast.makeText(getBaseContext(), "Error Occured" ,Toast.LENGTH_LONG).show();
							Log.e("log_tag", "Error in http connection"+e.toString());
							
						}
					}
					
				}

			});

			alert.show();
		}
		
		
		
		/** Defining the objects need to be in the ViewHolder*/
		class ViewHolder {
			TextView titleView;
	    	TextView upploaderView;
	    	TextView description;
	    	Button operation;
	    	TextView id;
        }
    	
    }
    
    /** Add dashboard menu item to action bar. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}

	/** Create the menu */
	private void CreateMenu(Menu menu)
	{
		MenuItem mnu1 = menu.add(0, 0, 0, "Dashboard");
		{

			mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
					MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			mnu1.setIcon(R.drawable.ic_menu_largetiles);
			mnu1.setTitle("Dashboard");
		}

		MenuItem mnu2 = menu.add(0, 1, 1, "Logout");
		{
			mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
					MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			mnu2.setIcon(R.drawable.ic_menu_exit);
			mnu2.setTitle("Logout");
		}

	}

	/** Menu item click event */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return MenuChoice(item);
	}

	/** Excute tasks for click events */
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 0:
		{
			startActivity(new Intent(DeleteGroups.this,Dashboard.class));
			this.finish();//
		}

		return true;
		case 1:
			GlobalClass.setUsername("");
			startActivity(new Intent(DeleteGroups.this,Login.class));
			this.finish();//
			return true;

		}
		return false;
	}
}



