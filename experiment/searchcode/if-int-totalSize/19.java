package com.example.exampleceim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exampleceim.Agenda.Agenda;
import com.example.exampleceim.Agenda.AgendaParser;
import com.example.exampleceim.pautas_certificacion.PautasParser;
import com.example.exampleceim.usuarios.UsuariosParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class Login_Usuario extends Activity {
	
	private static String urlServiceUsuarios = "http://webapitest2014.azurewebsites.net/api/DataClient/GetUsuariosInitialize";
	private static String urlSincronizarUsuarios = "http://webapitest2014.azurewebsites.net/api/DataClient/GetUsuariosSynchronize?dateLastSynchronize=";
	
	private static String urlInicializacionPauta = "http://webapitest2014.azurewebsites.net/api/DataClient/GetPautasInitialize";
//	private static String urlSincronizarPautas = "http://webapitest2014.azurewebsites.net/api/DataClient/GetPautasSynchronize?dateLastSynchronize=";
	
	public static String urlServicioAgenda = "http://webapitest2014.azurewebsites.net/api/Agenda/GetAgenda?fromDateProgramation=01-04-2014";
	
	static String TAG_APP = Login_Usuario.class.getName() + " LOG_TAG";
	//static String TAG_APP = "CEIM";
	
	private static String LOGTAG = Login_Usuario.class.getName() + " LOGCAT";
	AsyncHttpClient client = new AsyncHttpClient();
	Context mContext;														
	DatabaseHandler db;
	ProgressDialog barProgressDialog;
	Handler updateBarHandler;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login__usuario);
		
		
		mContext = this;
		cargarLogo(mContext);
		
		checkInternetConnection();	
		
		//String currentDateTimeString = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String strDate = sdf.format(c.getTime());
		
		
		TextView textView = (TextView) findViewById(R.id.fecha);
		textView.setText(strDate);
		
		
		final EditText user = (EditText) findViewById(R.id.userTxt);
		final EditText pass = (EditText) findViewById(R.id.txtRut);
		
		Button btnEntrar = (Button) findViewById(R.id.btnEntrar);
		btnEntrar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				checkUserPass(user.getText().toString(), pass.getText().toString());
				
//				try {
//					String password_  = passToSha1(pass.getText().toString(), "sDvsUVEcCuXpIJhvv63YhKGññ");
//					System.out.println("Password con sha1 desde ActivityLogin: " + password_);
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
		});
		
		
		
//		
	}
	
	private void checkInternetConnection() {
		
		Setup_Inicializacion.cd = new ConnectionDetector(getApplicationContext());
		Setup_Inicializacion.isInternetPresent = Setup_Inicializacion.cd.isConnectingToInternet();
		 
        if (Setup_Inicializacion.isInternetPresent) {
        	
        	final ProgressDialog ringProgressDialog = ProgressDialog.show(this, "Por favor espere...",	"Sinronizando Datos ...", true);
    		ringProgressDialog.setCancelable(false);
    		new Thread(new Runnable() {
    			@Override
    			public void run() {
    				try {
    					// Here you should write your time consuming task...
    					// Let the progress ring for 10 seconds...
    					Thread.sleep(5000);
    				} catch (Exception e) {

    				}
    				ringProgressDialog.dismiss();
    			}
    		}).start();
        
        	obtenerUsuarios();
        	
        }else{
        	
        	Setup_Inicializacion.showAlertDialog(Login_Usuario.this, "Sin conexiñn a Internet",
                    "Se necesita conexiñn a Internet para sincronizaro los datos ...", false);
        	
        }
		
		
		
	}

	private void checkUserPass(final String user, final String pass) {
		
		db = new DatabaseHandler(mContext);
		db.open();
		boolean resultado = db.validarUsuario(user, pass);
		
		if(resultado == true){
			
			 if (Setup_Inicializacion.isInternetPresent) {
		        	
				 	client.setBasicAuth(user, pass);
					//Para distinguir el centro certificador, se utiliza: CodeCertificatorCenter / 5HwMQ0O4ñc6UzrwAlqYgYLSz=
					client.addHeader("CodeCertificatorCenter", "5HwMQ0O4ñc6UzrwAlqYgYLSz=");
					client.get(getApplicationContext(), urlServicioAgenda, new JsonHttpResponseHandler(){
						@Override
						public void onSuccess(int statusCode, JSONObject response) {
							super.onSuccess(statusCode, response);
							
							Log.i(TAG_APP, "Login Succes");
							//Log.i("Json response", response.toString());
							
							 try {
								 
								 db = new DatabaseHandler(getApplicationContext());
									AgendaParser agendaParser = new AgendaParser();
									agendaParser.guardarDatosAgenda(response, mContext, db);
								
//							            File myFile = new File("/sdcard/AgendaJson.txt");
//							            myFile.createNewFile();
//							            FileOutputStream fOut = new FileOutputStream(myFile);
//							            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
//							            myOutWriter.append(response.toString());
//							            myOutWriter.close();
//							            fOut.close();
//							            Log.i(TAG_APP, "Done writing SD");
						        } 
						        catch (Exception e) 
						        {
						            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
						        }
							
							
						}
						
						@Override
						public void onProgress(final int bytesWritten, final int totalSize) {
							super.onProgress(bytesWritten, totalSize);
							
							
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
							Intent intent = new Intent(getApplicationContext(), Agenda.class);
							intent.putExtra("user", user);
							intent.putExtra("pass", pass);
							startActivity(intent);
							super.onFinish();
						}
					});
					
		        	
		        }else{
		        	
		        	Setup_Inicializacion.showAlertDialog(Login_Usuario.this, "Sin conexiñn a Internet",
		                    "", false);
		        	
		        	Intent intent = new Intent(getApplicationContext(), Agenda.class);
					intent.putExtra("user", user);
					intent.putExtra("pass", pass);
					startActivity(intent);
		        	
		        }
			
		}else{
			
			alertDialog();
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login__usuario, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId())
		{
			case R.id.action_sincronizar:
				
//				Calendar c = Calendar.getInstance();
//				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
//				String strDate = sdf.format(c.getTime());
				
		        sincronizarUsuarios( checkFechaSync("Usuario") );    
				
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
		
	}
		
	public void obtenerUsuarios(){
		
		client.setBasicAuth("garocas", "123456");
		client.addHeader("CodeCertificatorCenter", "5HwMQ0O4ñc6UzrwAlqYgYLSz=");
		client.get(getApplicationContext(), urlServiceUsuarios, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				super.onSuccess(statusCode, response);
				
				Log.i(LOGTAG, "Descargando Usuarios");
				//Log.i("Json response", response.toString());
				
				 try {
					 
					db = new DatabaseHandler(getApplicationContext());
					UsuariosParser usuariosParser = new UsuariosParser();
					usuariosParser.guardarDatosUsuario(response, mContext, db);
					
					
					
					
//			            File myFile = new File("/sdcard/usuarios.txt");
//			            myFile.createNewFile();
//			            FileOutputStream fOut = new FileOutputStream(myFile);
//			            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
//			            myOutWriter.append(response.toString());
//			            myOutWriter.close();
//			            fOut.close();
			            
//			            Log.i(TAG_APP, "Done writing SD");
			        } 
			        catch (Exception e) 
			        {
			            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
			        }
				
				
			}
			
			@Override
			public void onProgress(final int bytesWritten, final int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
			
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				super.onFailure(e, errorResponse);
				
				Log.e(LOGTAG, "Error en la respuesta del servicio desde el servidor, Sincronizaciñn Usuarios");
				e.printStackTrace();
				
				//Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				
				obtenerPautas();
				
			}
			
			
		});
		
		
	}
	
	public void sincronizarUsuarios(String strDate){
		
		String url = urlSincronizarUsuarios + strDate;
		
		Log.i(LOGTAG, "URL para sincronizaciñn de usuarios: " + url);
		
		client.setBasicAuth("garocas", "123456");
		client.addHeader("CodeCertificatorCenter", "5HwMQ0O4ñc6UzrwAlqYgYLSz=");
		client.get(getApplicationContext(), url, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				super.onSuccess(statusCode, response);
				
				Log.i(LOGTAG, "Sincronizando Usuarios");
				
				 try {
					 
						db = new DatabaseHandler(getApplicationContext());
						UsuariosParser usuariosParser = new UsuariosParser();
						usuariosParser.guardarDatosUsuario(response, mContext, db);
					 
					 JSONArray jValues = response.getJSONArray("$values");
					 
					 if(jValues.length() <= 0){
						 
						 alertDialog();
						 
					 }
					 
//			            File myFile = new File("/sdcard/usuarios.txt");
//			            myFile.createNewFile();
//			            FileOutputStream fOut = new FileOutputStream(myFile);
//			            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
//			            myOutWriter.append(response.toString());
//			            myOutWriter.close();
//			            fOut.close();
//			            Log.i("JSON Sincronizacion de Usuarios", "Done writing SD");
//			            
			        } 
			        catch (Exception e) 
			        {
			            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_SHORT).show();
			        }
				
				
			}
			
			@Override
			public void onProgress(final int bytesWritten, final int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
			
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				super.onFailure(e, errorResponse);
				
				Log.e(LOGTAG, "Error en la respuesta del servicio desde el servidor.");
				e.printStackTrace();
				
				//Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onFinish() {
				super.onFinish();
				
			}
			
			
		});
	}
	
	public void obtenerPautas(){
		
		client.setBasicAuth("garocas", "123456");
		client.addHeader("CodeCertificatorCenter", "5HwMQ0O4ñc6UzrwAlqYgYLSz=");
		client.get(getApplicationContext(), urlInicializacionPauta, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				super.onSuccess(statusCode, response);
				
				 try {
					Log.i(LOGTAG, "Sincronizando Pautas");
					
				     File myFile = new File("/sdcard/PautasCertificacionInicializacion.txt");
			            myFile.createNewFile();
			            FileOutputStream fOut = new FileOutputStream(myFile);
			            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
			            myOutWriter.append(response.toString());
			            myOutWriter.close();
			            fOut.close();
					
					db = new DatabaseHandler(getApplicationContext());
					PautasParser pautasParser = new PautasParser();
					pautasParser.guardarPautaCertificacion(response, mContext, db);
					

				 }catch (Exception e){
			     
					 Log.e(LOGTAG, "Error al obtener las Pautas desde el Servidor.");
					 e.printStackTrace();
				 }
				
				
			}
			
			@Override
			public void onProgress(final int bytesWritten, final int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
			
			}
			
			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				super.onFailure(e, errorResponse);
				
				Log.e(LOGTAG, "Error en la respuesta del servicio desde el servidor, Sincronizaciñn Pautas");
				e.printStackTrace();
				
				//Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			}
			
			
		});
		
		
	}
	
	public void cargarLogo(Context context){
		
		db = new DatabaseHandler(context);
		db.open();
		
		Resources res = getResources();
		BitmapDrawable icon = new BitmapDrawable(res, db.getImgLogo());
		getActionBar().setIcon(icon);
		
//		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
//		byte[] decodedString = Base64.decode(imgByte, Base64.DEFAULT);
//		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
//		imageView.setImageBitmap(db.getImgLogo());
		db.close();
		
	}
	
	public String checkFechaSync(String tipoSync){
		
		db = new DatabaseHandler(mContext);
		db.open();
		
		String fecha =  db.getFecha(tipoSync);
		if(fecha == null || fecha.equals("")){
			
		}else{
			Log.d(LOGTAG, "Fecha para sincronizaciñn: "+ fecha);
		}
			
		
		db.close();
		
		return fecha;
		
	}
	
	public void alertDialog(){
		
		final Dialog dialog = new Dialog(mContext);

        dialog.setContentView(R.layout.error_login);
        dialog.setTitle("Alerta");

        final Button btnAceptar = (Button) dialog.findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(new View.OnClickListener() {

    	    @Override
    	    public void onClick(View v) {
    	     dialog.dismiss();
    	     
    	    }
    	   });
        
         dialog.show();
		
	}
	
	public String passToSha1(String password, String salt) throws Exception {
    	
		        
		        StringBuilder sb = new StringBuilder();
					byte[] passwordByte = password.getBytes("UTF-8");
					byte[] saltByte = salt.getBytes("UTF-8");
		        
		        Mac localMac =  Mac.getInstance("HmacSHA1");
		        localMac.init(new SecretKeySpec(saltByte, localMac.getAlgorithm()));        
		        byte[] sha1hash = localMac.doFinal(passwordByte);
			    	        
				for (byte b : sha1hash) {
					sb.append(String.format("%02x", b));
				}
				
				Log.i(LOGTAG, sb.toString());
				
				return sb.toString();

		        
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
			 
	        
	    }

}

