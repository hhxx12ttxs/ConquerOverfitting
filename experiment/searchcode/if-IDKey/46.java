//E-Learning Environment
//ShareView.java 
//Handles activities related to 
//displaying and presenting of uploaded content
package com.els.sliit;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
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


import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** Called when the activity is first created. */
public class ShareView extends Activity {
    	
	EditText name;
	EditText number;
	Button add;
	JSONArray jArray;
	String result = null;
	InputStream is = null;
	StringBuilder sb=null;
	// I use HashMap arraList which takes objects
	private ArrayList <HashMap<String, Object>> myContent;
	private static final String TITLE = "title";
	private static final String FNAME = "fname";
	private static final String UPLOADER = "uploader";
	private static final String FUPLOADER = "fuploader";
	private static final String IMGKEY = "iconfromdrawable";
	private static final String RATINGKEY = "ratings";
	private static final String RATINGKEY1 = "ratings";
	private static final String IDKEY = "id";
	private static final String CATEGORY = "category";
	private Handler progressBarHandler = new Handler();
	String checkCategory="default";
	String switchStatus = "off";
	String description;
	
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_fragment1);
        
        addListenerOnButton();
        addListenerOnAll();
        
    	//Set windows title
		ActionBar acBar = getActionBar();
		acBar.setTitle("Shared Content");
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        loadFiles("http://10.0.2.2:1000/test/test2.php",1,null);
        loadFiles("http://10.0.2.2:1000/test/category.php",2,null);
        TextView textView = (TextView)findViewById(R.id.categorytext);
        textView.setText("All");
        
        Button btnShareContent = (Button) findViewById(R.id.btnShareContent);
        
        btnShareContent.setOnClickListener(new View.OnClickListener() {
 			
 		
 			public void onClick(View v) {
 				
 				//Go to Share View
 				startActivity(new Intent(ShareView.this,UploadContent.class));
 				finish();//
 				
 			}
 		});
        
        
        
    }
    
    /** Handles the click event of switch button to display popular content*/
    public void addListenerOnButton() {
    	 
    	final Switch sw1 = (Switch) findViewById(R.id.switch1);
     
    	sw1.setOnClickListener(new OnClickListener() {
     
    		
    		public void onClick(View v) {
     
    		   if(sw1.isChecked())
    		   {
    			   switchStatus = "on";
    			   if(checkCategory.equals("default"))
    			   loadFiles("http://10.0.2.2:1000/test/test.php",1,null);
    			   else
    			   {
    				   loadFiles("http://10.0.2.2:1000/test/popularcategory.php",3,checkCategory);  
    			   }
    		   }
    		   else
    		   {
    			   switchStatus = "off";
    			   if(checkCategory.equals("default"))
    			   loadFiles("http://10.0.2.2:1000/test/test2.php",1,null);
    			   else
    			   {
    				   loadFiles("http://10.0.2.2:1000/test/recentcategory.php",3,checkCategory);
    			   }
    		   }
     
    		}
     
    	});
     
      }
    
    /** Handle click event of 'All' tag in category to view all content
     * Also compares the status of switch button to display results
     */
    public void addListenerOnAll() {
   	 
    	final TextView sw1 = (TextView) findViewById(R.id.categorytext);
     
    	sw1.setOnClickListener(new OnClickListener() {
     
    		
    		public void onClick(View v) {
    		   	
               checkCategory ="default";
    		  
    		   if(switchStatus.equals("on"))
		        	loadFiles("http://10.0.2.2:1000/test/test.php",1,null);
				else
				    loadFiles("http://10.0.2.2:1000/test/test2.php",1,null);
     
    		   //Toast.makeText(getBaseContext(),result,Toast.LENGTH_LONG).show();
     
    		}
     
    	});
     
      }
    
    /** Load files to ListView in listbox.xml file.
     *  the path of the specific php file is passed to the url.
     *  type of data needed to load identifies using status.
     *  parameters which is needed to send to the php files are handled by para.
     * */
    public void loadFiles(String url,int status,String para)
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
        int rating;
        String type;
        String id;
        String filename;
        String fuploader;
        try{
      	 
              jArray = new JSONArray(result);
              JSONObject json_data=null;
              for(int i=0;i<jArray.length();i++){
            	     descrip="Uploaded by : ";
                     json_data = jArray.getJSONObject(i);
                     name=json_data.getString("title");
                     fuploader=json_data.getString("uploader");
                     filename=json_data.getString("path");
                     type=json_data.getString("type");
                     //category=json_data.getString("category");
                     descrip=descrip+json_data.getString("uploader");
                     rating=json_data.getInt("current_rating");
                     id=json_data.getString("id");
                     hm = new HashMap<String, Object>();
                     hm.put(TITLE, name);
                     hm.put(FNAME, filename);
                     hm.put(FUPLOADER, fuploader);
                     hm.put(UPLOADER, descrip);
                     if((type.equals("doc"))||(type.equals("docx"))||(type.equals("dox")))
                         hm.put(IMGKEY, R.drawable.docx);
                     else if((type.equals("pdf")))
                         hm.put(IMGKEY, R.drawable.pdf);
                     else if((type.equals("ppt"))||(type.equals("pptx"))||(type.equals("ppx")))
                         hm.put(IMGKEY, R.drawable.ppt);
                     else if((type.equals("jpg"))||(type.equals("jpeg"))||(type.equals("png"))||(type.equals("gif"))||(type.equals("svg")))
                         hm.put(IMGKEY, R.drawable.jpg);
                     else
                    	 hm.put(IMGKEY, R.drawable.ic_launcher);
                     hm.put(IDKEY, id);
                     hm.put(RATINGKEY, rating);
                     hm.put(RATINGKEY1, rating);
                     myContent.add(hm);
                     descrip="";
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
        		
        listView.setAdapter(new myListAdapter(myContent,this,1));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        
        
        else if(stat==2)
        {
        	listView = (ListView)findViewById(R.id.listView1);
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
                }catch(Exception e){
                      Log.e("log_tag", "Error converting result "+e.toString());
                }
        //paring data
        
        String category;
        try{
      	 
              jArray = new JSONArray(result);
              JSONObject json_data=null;
              for(int i=0;i<jArray.length();i++){
                     json_data = jArray.getJSONObject(i);
                     category=json_data.getString("category");
                     hm = new HashMap<String, Object>();
                     hm.put(CATEGORY, category);
                     myContent.add(hm);
                 }
              }
              catch(JSONException e1){
              	
            	  Toast.makeText(getBaseContext(), "No Content Available" ,Toast.LENGTH_LONG).show();
            	  Log.e("log_tag", e1.toString());
              } catch (ParseException e1) {
        			e1.printStackTrace();
        	}
         		
        listView.setAdapter(new myListAdapter(myContent,this,2));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); 
        }
        
        if(stat==3)
        {
        	nameValuePairs.add(new BasicNameValuePair("category",para));
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
                }catch(Exception e){
                      Log.e("log_tag", "Error converting result "+e.toString());
                }
        //paring data
        String name;
        description="";
        int rating;
        String id;
        String type;
        String filename;
        String uploader;
        try{
      	 
              jArray = new JSONArray(result);
              JSONObject json_data=null;
              for(int i=0;i<jArray.length();i++){
            	     description="Uploaded by : ";
                     json_data = jArray.getJSONObject(i);
                     name=json_data.getString("title");
                     filename=json_data.getString("path");
                     uploader=json_data.getString("uploader");
                     description=description+json_data.getString("uploader");
                     type=json_data.getString("type");
                     rating=json_data.getInt("current_rating");
                     id=json_data.getString("id");
                     hm = new HashMap<String, Object>();
                     hm.put(TITLE, name);
                     hm.put(FNAME, filename);
                     hm.put(FUPLOADER, uploader);
                     hm.put(UPLOADER, description);
                     if((type.equals("doc"))||(type.equals("docx"))||(type.equals("dox")))
                         hm.put(IMGKEY, R.drawable.docx);
                     else if((type.equals("pdf")))
                         hm.put(IMGKEY, R.drawable.pdf);
                     else if((type.equals("ppt"))||(type.equals("pptx"))||(type.equals("ppx")))
                         hm.put(IMGKEY, R.drawable.ppt);
                     else if((type.equals("jpg"))||(type.equals("jpeg"))||(type.equals("png"))||(type.equals("gif"))||(type.equals("svg")))
                         hm.put(IMGKEY, R.drawable.jpg);
                     else
                    	 hm.put(IMGKEY, R.drawable.ic_launcher);
                     hm.put(IDKEY, id);
                     hm.put(RATINGKEY, rating);
                     hm.put(RATINGKEY1, rating);
                     myContent.add(hm);
                     description="";
                 }
              }
              catch(JSONException e1){
              	
            	  Toast.makeText(getBaseContext(), "No Content Available" ,Toast.LENGTH_LONG).show();
            	  Log.e("log_tag", e1.toString());
              } catch (ParseException e1) {
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
			
			if(status==1)
			{
			// TODO Auto-generated method st
			// A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
			ViewHolder holder;
			
			// When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null
			
			 if (convertView == null) {
	             convertView = mInflater.inflate(R.layout.nlistbox, null);
	             // Creates a ViewHolder and store references to the two children views
	             // we want to bind data to.
	             
	             holder = new ViewHolder();
	             holder.titleView = (TextView) convertView.findViewById(R.id.text1);
	             holder.tvfilename = (TextView) convertView.findViewById(R.id.tvfilename);
	             holder.fuploader = (TextView) convertView.findViewById(R.id.tvfiluploader);
	             holder.upploaderView = (TextView) convertView.findViewById(R.id.text2);
	             holder.icon = (ImageView) convertView.findViewById(R.id.img);
	             holder.contentId = (TextView)convertView.findViewById(R.id.text3);
	             holder.setRate = (RatingBar)convertView.findViewById(R.id.star1);
	             holder.currentRate = (RatingBar)convertView.findViewById(R.id.star);
	             
	             convertView.setTag(holder);
	                
			 }else {
				 // Get the ViewHolder back to get fast access to the views
				 holder = (ViewHolder) convertView.getTag(); 
			 }
			 	// Bind the data with the holder.
			 
				holder.titleView.setText((String) Content.get(position).get(TITLE));
				
				holder.tvfilename.setText((String) Content.get(position).get(FNAME));
				
				holder.fuploader.setText((String) Content.get(position).get(FUPLOADER));
				
				holder.upploaderView.setText((String) Content.get(position).get(UPLOADER));
				
				holder.icon.setImageResource((Integer)Content.get(position).get(IMGKEY));
				
				holder.contentId.setText((String)Content.get(position).get(IDKEY));
				
				holder.currentRate.setRating((Integer)Content.get(position).get(RATINGKEY1));
				
				
				
				convertView.setOnClickListener(new OnClickListener(){
					
			        public void onClick(View v) {
			        				        	
			        }
			        
			        
			    });
				RatingBar ratingBar = (RatingBar)convertView.findViewById(R.id.star1);
				TextView tv = (TextView)convertView.findViewById(R.id.text3);
				TextView tv1 = (TextView)convertView.findViewById(R.id.text1);
				final String ss = (String) tv.getText();
				final String ss1 = (String) tv1.getText();
					//if rating is changed,
					//display the current rating value in the result (textview) automatically
					ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
						public void onRatingChanged(RatingBar ratingBar, float rating,
								boolean fromUser) {
                            
							 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						        nameValuePairs.add(new BasicNameValuePair("numrate",ss));
						        nameValuePairs.add(new BasicNameValuePair("rating",String.valueOf(rating)));
						        //http post
						        try{
						             HttpClient httpclient = new DefaultHttpClient();
						             HttpPost httppost = new HttpPost("http://10.0.2.2:1000/test/test1.php");
						             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
						             HttpResponse response = httpclient.execute(httppost);
						             HttpEntity entity = response.getEntity();
						             is = entity.getContent();
						             }catch(Exception e){
						                 Log.e("log_tag", "Error in http connection"+e.toString());
						            }
							Toast.makeText(getBaseContext(), "you rated "+String.valueOf(rating)+" for "+ss1 ,Toast.LENGTH_LONG).show();

						}
					});
					
					 final TextView tvDocName = (TextView)convertView.findViewById(R.id.tvfilename);
					 final TextView tvDocUploader = (TextView)convertView.findViewById(R.id.tvfiluploader);
					 //Initiating the view button click event
					 Button btnViewContent = (Button)convertView.findViewById(R.id.btnViewDoc);
				        
				        btnViewContent.setOnClickListener(new View.OnClickListener() {
				 			
				 		
				 			public void onClick(View v) {
				 				
				 				 
				 				 String docName = tvDocName.getText().toString();
				 				 Intent intent = new Intent(getBaseContext(),ViewFiles.class);
				 				 
				 				 //Passing document name to the 'ViewFile.java' intent
				                 intent.putExtra("docName",docName);		
				                 startActivity(intent);
				                 finish();//
				 				
				 			}
				 		});
				        
				        //Initiating download button
				        Button download = (Button)convertView.findViewById(R.id.btnDownloadDoc);
				        //download button click event
				        download.setOnClickListener(new OnClickListener() {
				            public void onClick(View v) {
				            	
				                try {
				                	final ProgressDialog progressBar = new ProgressDialog(v.getContext());
				        			progressBar.setCancelable(true);
				        			progressBar.setMessage("File downloading ...");
				        			progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				        			progressBar.setProgress(0);
				        			progressBar.setMax(100);
				        			progressBar.show();
				                	String docName = tvDocName.getText().toString();
				                	String docUploader = tvDocUploader.getText().toString();
				                	URL url  = new URL("http://10.0.2.2:1000/eLearningWebServices/Content/Uploads/"+docUploader+"/"+docName);
				                    URLConnection conexion = url.openConnection();
				                    conexion.connect();
				                    int lenghtOfFile = conexion.getContentLength();
				                    InputStream is = url.openStream();
				                    File testDirectory = 
				                    new File(Environment.getExternalStorageDirectory()+"/Download");
				                    if(!testDirectory.exists()){
				                        testDirectory.mkdir();
				                    }
				                    FileOutputStream fos = new FileOutputStream(testDirectory+"/"+docName);
				                    byte data[] = new byte[1024];
				                    int count = 0;
				                    long total = 0;
				                    int progress = 0;
				                    while ((count=is.read(data)) != -1){
				                    	total += count;
				                        final int progress_temp = (int)total*100/lenghtOfFile;
				                        if(progress_temp%10 == 0 && progress != progress_temp){
				                            progress = progress_temp;
				                        }
				                        progressBarHandler.post(new Runnable() {
				        					public void run() {
				        					  progressBar.setProgress(progress_temp);
				        					}
				        				  });
				        				
				                        fos.write(data, 0, count);
				                    }
				                    is.close();
				                    fos.close();
				                    Toast.makeText(getBaseContext(),"File successfully downloaded to '/sdcard/downloads",Toast.LENGTH_LONG).show();
				                    startActivity(new Intent(ShareView.this, ShareView.class));
				                    finish();//
				                } catch (Exception e) {
				                    Toast.makeText(getBaseContext(),"Error Occured",Toast.LENGTH_LONG).show();
				                    startActivity(new Intent(ShareView.this, ShareView.class));
				                    finish();//
				                }
				            	
				            	
				            }
				        });
			}
			
			else if(status==2)
			{
			// TODO Auto-generated method st
			// A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
			ViewHolder holder;
			
			// When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null
			
			 if (convertView == null) {
	             convertView = mInflater.inflate(R.layout.category, null);
	             // Creates a ViewHolder and store references to the two children views
	             // we want to bind data to.
	             
	             holder = new ViewHolder();
	             holder.displayCategory = (TextView) convertView.findViewById(R.id.categorytext);
	             
	             
	             convertView.setTag(holder);
	                
			 }else {
				 // Get the ViewHolder back to get fast access to the TextView
	             // and the ImageView.
				 holder = (ViewHolder) convertView.getTag(); 
			 }
			 	// Bind the data with the holder.
			 
				holder.displayCategory.setText((String) Content.get(position).get(CATEGORY));
				
					convertView.setOnClickListener(new OnClickListener(){
						
			        public void onClick(View v) {
			        	
			        	TextView tv1 = (TextView)v.findViewById(R.id.categorytext);
						final String ss = (String) tv1.getText();
						checkCategory=ss;
						if(switchStatus.equals("on"))
			        	loadFiles("http://10.0.2.2:1000/test/loadpopularcategoryfiles.php",3,ss);
						else
					    loadFiles("http://10.0.2.2:1000/test/loadcategoryfiles.php",3,ss);
			        	
			            
			        }
					});
			        
				
				
			}
			
				return convertView;
		}
		
		
		
		/** Defining the objects need to be in the ViewHolder*/
		class ViewHolder {
			TextView titleView;
	    	TextView upploaderView;
	    	ImageView icon;
	    	RatingBar setRate;
	    	TextView contentId;
	    	RatingBar currentRate;
	    	TextView displayCategory;
	    	TextView tvfilename;
	    	TextView fuploader;
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
			startActivity(new Intent(ShareView.this,Dashboard.class));
			this.finish();//
		}

		return true;
		case 1:
			GlobalClass.setUsername("");
			startActivity(new Intent(ShareView.this,Login.class));
			this.finish();//

			return true;

		}
		return false;
	}

	
}
