package com.example.exampleceim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.exampleceim.initializer.DataInitializer_Parser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class Setup_Inicializacion extends Activity{

	AsyncHttpClient client = new AsyncHttpClient();
//	String url = "http://192.168.2.44/WebApitest/API/Products/GetAllProducts";
//	String urlsinlogin = "http://webapitest2014.azurewebsites.net/API/Products/GetProduct/1";
	
	//url Servicio de inicialización de los datos
	String urlServicioInicializacion = "http://webapitest2014.azurewebsites.net/api/DataClient/GetDataInitialize";
	String urlServicio_Usuarios = "http://webapitest2014.azurewebsites.net/api/DataClient/GetUsuariosInitialize";
	
	static String TAG_APP = "CEIM";
//	private int mProgressStatus = 0;

	ProgressDialog barProgressDialog;
	Handler updateBarHandler;
	
    static Boolean isInternetPresent = false;
     
    static ConnectionDetector cd;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		barProgressDialog = new ProgressDialog(Setup_Inicializacion.this);
		
		pref = getSharedPreferences("testapp", MODE_PRIVATE);
		editor = pref.edit();
		
		String getStatus=pref.getString("register", "nil");
		if(getStatus.equals("true")){
			
			Intent intent = new Intent(getApplicationContext(), Login_Usuario.class);
			startActivity(intent);
			finish();
		
		}
		
		//Mostrar Imagenes (ByteArray) obtenido desde el Servicio de inicialización
//			ImageView imageView = (ImageView) findViewById(R.id.imageView1);
//			String imgByte = "/9j/4AAQSkZJRgABAQEAYABgAAD/4QAiRXhpZgAATU0AKgAAAAgAAQESAAMAAAABAAEAAAAAAAD/7AARRHVja3kAAQAEAAAAUAAA/+EDgGh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8APD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4NCjx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4NCgk8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPg0KCQk8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1wTU06T3JpZ2luYWxEb2N1bWVudElEPSJ4bXAuZGlkOjAxODAxMTc0MDcyMDY4MTFBRkZEOEQ5NkMyNzA1MzdFIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjQ3MjExMTdDOURDRTExRTFCMkEwQUQyMjI4RTkxMDIyIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjQ3MjExMTdCOURDRTExRTFCMkEwQUQyMjI4RTkxMDIyIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzUgTWFjaW50b3NoIj4NCgkJCTx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOkU1NjVENUMyMEMyMDY4MTFBRkZEOEQ5NkMyNzA1MzdFIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjAxODAxMTc0MDcyMDY4MTFBRkZEOEQ5NkMyNzA1MzdF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";
//			byte[] decodedString = Base64.decode(imgByte, Base64.DEFAULT);
//			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
//			imageView.setImageBitmap(decodedByte);
		
		cd = new ConnectionDetector(getApplicationContext());
		
		Button btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View v) {
				
				isInternetPresent = cd.isConnectingToInternet();
				 
                // check for Internet status
                if (isInternetPresent) {
                    // Internet Connection is Present
                    // make HTTP requests
                	
                	
                	
//                    showAlertDialog(Setup_Inicializacion.this, "Internet Connection",
//                            "You have internet connection", true);
				
					barProgressDialog.setTitle("Por favor espere...");
					barProgressDialog.setMessage("Inicialización en progreso...");
					barProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					barProgressDialog.setCancelable(false);
					barProgressDialog.setIndeterminate(true);
	//				barProgressDialog.setProgress(0);
	//				barProgressDialog.setMax(20);
					barProgressDialog.show();	
	
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
	
	//							 Here you should write your time consuming task...
								while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {
	
									Thread.sleep(2000);
	
									updateBarHandler.post(new Runnable() {
	
			                            public void run() {
	//		                            	barProgressDialog.incrementProgressBy(2);
	
			                              }
	
			                          });
	
									if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
	
										barProgressDialog.dismiss();
	
									}
								}
							} catch (Exception e) {
							}
						}
	
					}).start();
					
	//				EditText user = (EditText) findViewById(R.id.txtUser);
	//				EditText pass = (EditText) findViewById(R.id.txtPass);
					
	//				try {
	//					
	//					String passwordEncrypt =	passToSha1(pass.getText().toString(), "sDvsUVEcCuXpIJhvv63YhKGńŃ");
	//					Log.i("Password Encypted : ", passwordEncrypt);
	//					
	//				} catch (Exception e1) {
	//					// TODO Auto-generated catch block
	//					e1.printStackTrace();
	//				}
	
					// Se de define y se pasan los parámetros para la autentificación básica
					// que se utlizará para la inicialización de la aplicación cuando se instala por primera vez.
					client.setBasicAuth("garocas", "123456");
					
					//Para distinguir el centro certificador, se utiliza: CodeCertificatorCenter / 5HwMQ0O4ńc6UzrwAlqYgYLSz=
					client.addHeader("CodeCertificatorCenter", "5HwMQ0O4ńc6UzrwAlqYgYLSz=");
					
					client.get(getApplicationContext(), urlServicioInicializacion, new JsonHttpResponseHandler(){
						@Override
						public void onSuccess(int statusCode, JSONObject response) {
							super.onSuccess(statusCode, response);
							
							Log.i(TAG_APP, "Login Succes");
							//Log.i("Json response", response.toString());
							
							 try {
								
								 DatabaseHandler db = new DatabaseHandler(getApplicationContext());
								 
								DataInitializer_Parser initializer_Parser = new DataInitializer_Parser(getApplicationContext());
								initializer_Parser.guardarDatosInicializacion(response, getApplicationContext(), db);
								
								
	//					            File myFile = new File("/sdcard/usuarios.txt");
	//					            myFile.createNewFile();
	//					            FileOutputStream fOut = new FileOutputStream(myFile);
	//					            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
	//					            myOutWriter.append(response.toString());
	//					            myOutWriter.close();
	//					            fOut.close();
						            
	//					            Log.i(TAG_APP, "Done writing SD");
						        } 
						        catch (Exception e) 
						        {
						            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
						        }
							
							
						}
						
						@Override
						public void onProgress(final int bytesWritten, final int totalSize) {
							super.onProgress(bytesWritten, totalSize);
							
							if( bytesWritten == totalSize){
								
								Log.d("Inicializacion", "Progress Finish");
								
							}
							
							
						}
						
						@Override
						public void onFailure(Throwable e, JSONObject errorResponse) {
							super.onFailure(e, errorResponse);
							
							Log.e(TAG_APP, "Error en la respuesta del servicio desde el servidor.");
							e.printStackTrace();
							
							//Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
						}
						
						@Override
						public void onFinish() {
							
							editor.putString("register","true");
		                	editor.commit();
							
							Intent intent = new Intent(getApplicationContext(), Login_Usuario.class);
							startActivity(intent);
							super.onFinish();
						}
					});
					
				}else {
					
                // Internet connection is not present
                // Ask user to connect to Internet
                showAlertDialog(Setup_Inicializacion.this, "No Internet Connection",
                        "You don't have internet connection.", false);
				}
                
			}
			
		});
	}
	
	@SuppressWarnings("deprecation")
	public static void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
 
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
	
	@SuppressWarnings("deprecation")
	public static void showAlertDialogGuardar(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
 
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.save);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
	
	@SuppressWarnings("deprecation")
	public static void showAlertDialogSalir(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
 
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.fail);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        
        alertDialog.show();
    }
	
	public void launchRingDialog() {
		final ProgressDialog ringProgressDialog = ProgressDialog.show(Setup_Inicializacion.this, "Please wait ...",	"Downloading Image ...", true);
		ringProgressDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Here you should write your time consuming task...
					// Let the progress ring for 10 seconds...
					Thread.sleep(10000);
				} catch (Exception e) {

				}
				ringProgressDialog.dismiss();
			}
		}).start();
	}
	
	public void launchBarDialog(View view) {
		barProgressDialog = new ProgressDialog(Setup_Inicializacion.this);

		barProgressDialog.setTitle("Inicializando...");
		barProgressDialog.setMessage("Inicialización en progreso...");
		barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		barProgressDialog.setCancelable(false);
		barProgressDialog.setIndeterminate(true);
		barProgressDialog.show();	

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					// Here you should write your time consuming task...
					while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {

						Thread.sleep(2000);

						updateBarHandler.post(new Runnable() {

                            public void run() {
                            	barProgressDialog.incrementProgressBy(2);

                              }

                          });

						if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {

							barProgressDialog.dismiss();

						}
					}
				} catch (Exception e) {
				}
			}
		}).start();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(barProgressDialog.isShowing()){
			barProgressDialog.dismiss();
		}
		
	}
	
	
//	 public String passToSha1(String text, String salt) throws Exception {
//	    	
//		 String generatedPassword = null;
//			 MessageDigest md = MessageDigest.getInstance("SHA-1");
//			 
//		        md.update(salt.getBytes("UTF-8"));
//		        
//		        
//		        byte[] sha1hash = md.digest(text.getBytes("UTF-8"));
//		        
//		        StringBuilder builder = new StringBuilder();
//		        for(int i =0; i < sha1hash.length; i++){
//		        	
//		        	builder.append(String.format("%02x", sha1hash[i]));
//		        	//builder.append(Integer.toString((int) sha1hash[i] & 0xff, 16));
//		        	//builder.append(Integer.toString((sha1hash[i] & 0xff) + 0x100, 16).substring(1));
//		        	
//		        }
//		        
//		        generatedPassword = builder.toString();
//		        
//		        return generatedPassword;
//			 
//	        
//	    }
	    
	 public static byte[] serialize(Object obj) throws IOException {
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    ObjectOutputStream os = new ObjectOutputStream(out);
		    os.writeObject(obj);
		    return out.toByteArray();
		}
	
	public class UserLoginTask extends AsyncTask<String, Void, Boolean> {

	    @Override
	    protected void onPostExecute(final Boolean success) {
//	        if (success == true) {
//
//	        	Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_LONG).show();
//	        	
//	        } else {
//	        	
//	        	Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
//	        	
//	        }
	    }

	    @Override
	    protected Boolean doInBackground(String... login) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpGet httpget = new HttpGet(
	        		urlServicioInicializacion);
	        
	        String username = login[0];
	        String password = login[1];
	        
	        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
	        BasicScheme basicScheme = new BasicScheme();
	        
	        try {
	        	
				Header header = basicScheme.authenticate(credentials, httpget);
				httpget.setHeader(header);
				httpget.setHeader("Enterprise", "CEIM");
				
			} catch (AuthenticationException e1) {
				e1.printStackTrace();
			}
	        
	        String str = null;

//	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//	        nameValuePairs.add(new BasicNameValuePair("username", username));
//	        nameValuePairs.add(new BasicNameValuePair("password", password));
	        
	        
	        
//	        try {
//	        	httpget.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
//	        } catch (UnsupportedEncodingException e1) {
//	            e1.printStackTrace();
//	            return false;
//	        }

	        try {
	            HttpResponse response = httpclient.execute(httpget);
	            str = EntityUtils.toString(response.getEntity());

	        } catch (IOException e) {
	            e.printStackTrace();
	        }


	        if (str.trim().equals("true")) {

	            return true;
	        } else {
	            return false;

	        }
	    }

	 
	}
}
