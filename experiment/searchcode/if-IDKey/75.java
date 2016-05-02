//E-Learning Environment
//deletesessions.java 
//Handles activities related to 
//deleting session groups
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class deletesessions extends Activity {
	JSONArray jArray;
	String result = null;
	InputStream is = null;
	StringBuilder sb = null;
	String type="";
	String strSName="";
	String strSDescription="";
	String groupID="";
	private ArrayList <HashMap<String, Object>> myContent;
	private static final String TITLE = "title";
	private static final String UPLOADER = "uploader";
	private static final String OPERATION = "operation";
	private static final String DESCRIPTION = "description";
	private static final String IDKEY = "id";
	/** Called when the activity is first created. */
	 public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
         setContentView(R.layout.deletesessions);
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);
         manageGroups("http://10.0.2.2:1000/test/loadGroups.php",1,null);
         
         //Back button click event
         Button btnBack = (Button)findViewById(R.id.btnSessionBack);
         btnBack.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					// Calls leaveCommunity method
					startActivity(new Intent(deletesessions.this, FinalNotiActivity.class));
					finish();//
					
				}
			});
	 }
	 
	 /** Use to leave or remove a group depends on the user is the creator of the group or not. */
	 public void operation(String action,int type,String id)
		{
         final int status = type;
         final String fAction=action;
         final String gID = id;
			AlertDialog.Builder alert = new AlertDialog.Builder(deletesessions.this);

			alert.setTitle(action);
			alert.setMessage("You are about to "+action+" the group.");			
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int whichButton) {
					// Canceled.
				}
			});

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int whichButton) {
					//user is the creator of the group
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
					//user is not the creator of the group
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

	 /** Use to retrieve group details from the database . */
	 public void manageGroups(String url,int status,String para)
	    {
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
	        	listView = (ListView)findViewById(R.id.lvManageSessions);
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
	 
	 /** Adding received data to a holder to display in controls. */ 
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
		             convertView = mInflater.inflate(R.layout.deletesessionformat2, null);
		             // Creates a ViewHolder and store references to the two children views
		             // we want to bind data to.
		             String btnStatus=(String) Content.get(position).get(OPERATION);
		             Button btnAdd = (Button)convertView.findViewById(R.id.btndeletesession2add);
		             holder = new ViewHolder();
		             if(btnStatus.equals("Delete"))
		             {
		             
		             holder.titleView = (TextView) convertView.findViewById(R.id.tvdeletesession2name);
		             holder.upploaderView = (TextView) convertView.findViewById(R.id.tvdeletesessions2creator);
		             holder.operation = (Button) convertView.findViewById(R.id.btndeletesession2operation);
		             holder.id = (TextView)convertView.findViewById(R.id.tvdeletesession2id);
		             holder.description=(TextView)convertView.findViewById(R.id.tvdeletesession2desc);
		             convertView.setTag(holder);
		             }
		             else
		             {
		            	 btnAdd.setVisibility(View.INVISIBLE);
		            	 holder.titleView = (TextView) convertView.findViewById(R.id.tvdeletesession2name);
			             holder.upploaderView = (TextView) convertView.findViewById(R.id.tvdeletesessions2creator);
			             holder.operation = (Button) convertView.findViewById(R.id.btndeletesession2operation);
			             holder.id = (TextView)convertView.findViewById(R.id.tvdeletesession2id);
			             holder.description=(TextView)convertView.findViewById(R.id.tvdeletesession2desc);
			             convertView.setTag(holder);
		             }
		                
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
					
					
					
					
					
					Button btnRemove = (Button)convertView.findViewById(R.id.btndeletesession2operation);
					TextView groupID = (TextView)convertView.findViewById(R.id.tvdeletesession2id);
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
					
					TextView groupIDG = (TextView)convertView.findViewById(R.id.tvdeletesession2id);
					TextView groupName = (TextView)convertView.findViewById(R.id.tvdeletesession2name);
					final String idg = groupIDG.getText().toString();
					final String nameg = groupName.getText().toString();
					Button btnAdd1 = (Button)convertView.findViewById(R.id.btndeletesession2add);
                    btnAdd1.setOnClickListener(new View.OnClickListener() {
                    
						
						public void onClick(View v) {
							
							Intent intent = new Intent(getBaseContext(), addmemberstogroups.class);
							intent.putExtra("gid", idg);
							intent.putExtra("gname",nameg );
							intent.putExtra("type",type);
							startActivity(intent);
							finish();//
						}
					});
					Button btnView = (Button)convertView.findViewById(R.id.btndeletesession2view);
					Button btnRemovel = (Button)convertView.findViewById(R.id.btndeletesession2operation);
					TextView groupIDV = (TextView)convertView.findViewById(R.id.tvdeletesession2id);
					TextView groupNameV = (TextView)convertView.findViewById(R.id.tvdeletesession2name);
					final String idV = groupIDV.getText().toString();
					final String gNameV = groupNameV.getText().toString();
					final String stat = btnRemovel.getText().toString();

					//button click event to delete or leave a group
					btnView.setOnClickListener(new View.OnClickListener() {

						
						public void onClick(View v) {
							// Calls leaveCommunity method
							Intent intent = new Intent(getBaseContext(), viewmembers.class);
							intent.putExtra("gid", idV);
							intent.putExtra("gname", gNameV);
							intent.putExtra("mode", stat);
							startActivity(intent);
							finish();//
						}
					});
						
					Button btnSign = (Button)convertView.findViewById(R.id.btndeletesessionopen);
					btnSign.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							GlobalClass.setCurrentSessionID(idV);
							Intent intent = new Intent(getBaseContext(), ChatTeach.class);
							startActivity(intent);
							finish();//
						}
					});
					
					return convertView;
			}
	    	
			class ViewHolder {
				TextView titleView;
		    	TextView upploaderView;
		    	TextView description;
		    	Button operation;
		    	TextView id;
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
	 		startActivity(new Intent(deletesessions.this,Dashboard.class));
	 		this.finish();//
	 	}

	 	return true;
	 	case 1:
	 		GlobalClass.setUsername("");
	 		startActivity(new Intent(deletesessions.this,Login.class));
	 		this.finish();//

	 		return true;

	 	}
	 	return false;
	 }
}

