//E-Learning Environment
//search_groups.java 
//Handles activities related to 
//Searching,joining and sending requests to groups and group owners  
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

public class search_groups extends Activity {
	JSONArray jArray;
	String result = null;
	InputStream is = null;
	StringBuilder sb = null;
	String type="";
	String strSName="";
	String strSDescription="";
	String groupID="";
	String CUNAME="";
	String notifier="";
	String keyword="";
	private ArrayList <HashMap<String, Object>> myContent;
	private static final String TITLE = "title";
	private static final String UPLOADER = "uploader";
	private static final String OPERATION = "operation";
	private static final String DESCRIPTION = "description";
	private static final String IDKEY = "id";
	private static final String MODE = "mode";

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
	   	 super.onCreate(savedInstanceState);
	        setContentView(R.layout.search_groups);
	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	        
	        manageGroups("http://10.0.2.2:1000/test/viewAllGroups.php",1,null);
	        
	        Button btnBack = (Button)findViewById(R.id.btnsearchgroupsback);
	        btnBack.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent notificationIntent = new Intent(search_groups.this,FinalNotiActivity.class);
					   startActivity(notificationIntent);
					   finish();//
				}
			});
	        //view all groups click event
	        Button btnViewAll = (Button)findViewById(R.id.btnsearchgroupsviewall);
	        btnViewAll.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					manageGroups("http://10.0.2.2:1000/test/viewAllGroups.php",1,null);
				}
			});
	        
	        //search button click event
	        Button search =  (Button)findViewById(R.id.btnsearchgroupssearch);
	        search.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					TextView tvKeyWord = (TextView)findViewById(R.id.etsearchgroupskeyword);
					keyword = tvKeyWord.getText().toString();
					manageGroups("http://10.0.2.2:1000/test/searchAllGroups.php",2,null);
					
				}
			});
	}
	
	/** Adding the user to the selected group upon user request. */
	 public void AddToGroup(String username,String gid,String gstatus) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("gid", gid));
			nameValuePairs.add(new BasicNameValuePair("status", gstatus));
			// http post
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"http://10.0.2.2:1000/test/addToGroups.php");
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection" + e.toString());
			}
			// convert response to string

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						is, "iso-8859-1"), 8);
				sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");

				String line = "0";
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
			} catch (Exception e) {
				Log.e("log_tag", "Error converting result " + e.toString());
			}
			// paring data
			String status = null;
			try {

				jArray = new JSONArray(result);
				JSONObject json_data = null;
				for (int i = 0; i < jArray.length(); i++) {
					json_data = jArray.getJSONObject(i);
					status = json_data.getString("status");

				}
				if ((status.equals("Error")))
					Toast.makeText(getBaseContext(), "Cannot add to the group",Toast.LENGTH_LONG).show();

				else {
					Toast.makeText(getBaseContext(), "Notification Sent",Toast.LENGTH_LONG).show();
					
				}
			} catch (JSONException e1) {
				Toast.makeText(getBaseContext(), "JError", Toast.LENGTH_LONG).show();
				Log.e("log_tag", e1.toString());

			} catch (ParseException e1) {
				e1.printStackTrace();

			}
		}
	
	 /** Getting the full name of the user. */
	 public void GetName(String url)
	    {
	    	ListView listView;
	        myContent = new ArrayList<HashMap<String,Object>>();
	        HashMap<String, Object> hm;
	        int stat=1;
	        
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        //http post
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
	        try{
	      	 
	              jArray = new JSONArray(result);
	              JSONObject json_data=null;
	              for(int i=0;i<jArray.length();i++){
	                     json_data = jArray.getJSONObject(i);
	                     CUNAME=json_data.getString("name");
	                  
	                 }
	              }
	              catch(JSONException e1)
	              {
	            	  Log.e("log_tag", e1.toString());
	            	  Toast.makeText(getBaseContext(), "No Content Available" ,Toast.LENGTH_LONG).show();
	              } 
	              catch (ParseException e1) 
	              {
	        			e1.printStackTrace();
	        	  }        
	        		
	        }
	    }
	
	 /** Sending a notification as a response. */
	 public void SendNotification(String notifier,String sender,String type,String status,String gid,String gname,String sendername)
	    {
	    	ListView listView;
	        myContent = new ArrayList<HashMap<String,Object>>();
	        HashMap<String, Object> hm;
	        int stat=1;
	        
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        //http post
	    
	      
	        //convert response to string
	        if(stat==1)
	        {
	            nameValuePairs.add(new BasicNameValuePair("notifier",notifier));
	        	nameValuePairs.add(new BasicNameValuePair("sender",GlobalClass.getUsername().toString()));
	        	nameValuePairs.add(new BasicNameValuePair("type",type));
	        	nameValuePairs.add(new BasicNameValuePair("status",status));
	        	nameValuePairs.add(new BasicNameValuePair("gid",gid));
	        	nameValuePairs.add(new BasicNameValuePair("gname",gname));
	        	nameValuePairs.add(new BasicNameValuePair("sendername",CUNAME));
	       	 try{
	                HttpClient httpclient = new DefaultHttpClient();
	                HttpPost httppost = new HttpPost("http://10.0.2.2:1000/test/addNotification.php");
	                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	                HttpResponse response = httpclient.execute(httppost);
	                HttpEntity entity = response.getEntity();
	                is = entity.getContent();
	                }catch(Exception e){
	                    Log.e("log_tag", "Error in http connection"+e.toString());
	               }
	        	
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
	        }
	    }

	 /** Use to leave or remove a group depends on the user is the creator of the group or not. */
	 public void operation(String action,int type,String id)
		{
      final int status = type;
      final String fAction=action;
      final String gID = id;
			AlertDialog.Builder alert = new AlertDialog.Builder(search_groups.this);

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
	        	listView = (ListView)findViewById(R.id.lvsearchgroups);
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
	        String mode;
	        try{
	      	 
	              jArray = new JSONArray(result);
	              JSONObject json_data=null;
	              for(int i=0;i<jArray.length();i++){
	            	     descrip="Createded by : ";
	                     json_data = jArray.getJSONObject(i);
	                     name=json_data.getString("group_name");
	                     type=json_data.getString("creator");
	                     descrip=descrip+json_data.getString("creator");
	                     description=json_data.getString("description");
	                     mode=json_data.getString("type");
	                     
	                     id=json_data.getString("groupID");
	                     hm = new HashMap<String, Object>();
	                     hm.put(TITLE, name);
	                     hm.put(UPLOADER, type);
	                     hm.put(IDKEY, id);
	                     hm.put(DESCRIPTION, description);
	                     hm.put(MODE, mode);
	                     
	                     if(mode.equals("Private"))
	                    	 hm.put(OPERATION, "Send Request");
	                     else
	                    	 hm.put(OPERATION, "Join");
	                    
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
	        
	        else if(stat==2)
	        {
	        	nameValuePairs.add(new BasicNameValuePair("username",GlobalClass.getUsername().toString()));
	        	nameValuePairs.add(new BasicNameValuePair("gname",keyword));
	        	nameValuePairs.add(new BasicNameValuePair("desc",keyword));
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
	        	listView = (ListView)findViewById(R.id.lvsearchgroups);
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
	        String mode;
	        try{
	      	 
	              jArray = new JSONArray(result);
	              JSONObject json_data=null;
	              for(int i=0;i<jArray.length();i++){
	            	     descrip="Created by : ";
	                     json_data = jArray.getJSONObject(i);
	                     name=json_data.getString("group_name");
	                     type=json_data.getString("creator");
	                     descrip=descrip+json_data.getString("creator");
	                     description=json_data.getString("description");
	                     mode=json_data.getString("type");
	                     
	                     id=json_data.getString("groupID");
	                     hm = new HashMap<String, Object>();
	                     hm.put(TITLE, name);
	                     hm.put(UPLOADER, type);
	                     hm.put(IDKEY, id);
	                     hm.put(DESCRIPTION, description);
	                     hm.put(MODE, mode);
	                     
	                     if(mode.equals("Private"))
	                    	 hm.put(OPERATION, "Send Request");
	                     else
	                    	 hm.put(OPERATION, "Join");
	                    
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
		             convertView = mInflater.inflate(R.layout.searchgroups_format, null);
		             // Creates a ViewHolder and store references to the two children views
		             // we want to bind data to.
		             
		             holder = new ViewHolder();
		             holder.titleView = (TextView) convertView.findViewById(R.id.tvsearchgroupsgname);
		             holder.upploaderView = (TextView) convertView.findViewById(R.id.tvsearchgroupscreator);
		             holder.operation = (Button) convertView.findViewById(R.id.btnsearchgroupsoperation);
		             holder.id = (TextView)convertView.findViewById(R.id.tvsearchgroupsid);
		             holder.description=(TextView)convertView.findViewById(R.id.tvsearchgroupsdesc);
		             convertView.setTag(holder);
		             
		                
				 }else {
					 // Get the ViewHolder back to get fast access to the views
					 holder = (ViewHolder) convertView.getTag(); 
				 }
				 	// Bind the data with the holder.
				 
				    String text = (String) Content.get(position).get(TITLE)+" ("+(String) Content.get(position).get(MODE)+")";
				 
					holder.titleView.setText(text);
					
					holder.upploaderView.setText((String) Content.get(position).get(UPLOADER));
					
					holder.operation.setText((String) Content.get(position).get(OPERATION));
					
					holder.id.setText((String) Content.get(position).get(IDKEY));
					
					holder.description.setText((String) Content.get(position).get(DESCRIPTION));
					
					Button operation = (Button)convertView.findViewById(R.id.btnsearchgroupsoperation);
					TextView tvGName = (TextView)convertView.findViewById(R.id.tvsearchgroupsgname);
					TextView tvGId = (TextView)convertView.findViewById(R.id.tvsearchgroupsid);
					TextView tvcreator = (TextView)convertView.findViewById(R.id.tvsearchgroupscreator);
					final String GName = tvGName.getText().toString();
					final String GId = tvGId.getText().toString();
					final String strOperation = operation.getText().toString();
					final String creator  = tvcreator.getText().toString();
					
					operation.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(strOperation.equals("Join"))
							{
								AddToGroup(GlobalClass.getUsername().toString(),GId,"accepted");
								manageGroups("http://10.0.2.2:1000/test/viewAllGroups.php",1,null);
							}
							else
							{
							AddToGroup(GlobalClass.getUsername().toString(),GId,"pending");
							manageGroups("http://10.0.2.2:1000/test/viewAllGroups.php",1,null);
							GetName("http://10.0.2.2:1000/test/getCUserName.php");
				        	SendNotification(creator,GlobalClass.getUsername().toString(),"ureq","1",GId,GName,CUNAME);
							}
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
	 		startActivity(new Intent(search_groups.this,Dashboard.class));
	 		this.finish();//
	 	}

	 	return true;
	 	case 1:
	 		GlobalClass.setUsername("");
	 		startActivity(new Intent(search_groups.this,Login.class));
	 		this.finish();//

	 		return true;

	 	}
	 	return false;
	 }
}

