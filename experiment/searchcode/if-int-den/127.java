package de.dmoeller.MySettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

import de.dmoeller.MySettings.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SaveAppsActivity extends Activity implements View.OnClickListener {
	
	private ProgressDialog AppsSichern;
	private ProgressDialog SelektionSichern;
	private ProgressDialog SelektionLaden;
	
	private String[] allappnames;
	
	private Drawable Icon;
	private HashMap<String, Integer> packageNameById;
	private List<PackageInfo> packs;
	private String SourceDirSelectedApp;
	private String PathSelectedApp;
	private String APKNameSelectedApp;
	private MySettingsDatenbank MysettingsDB;
	private String Global_MySettings_Dir;
	
	private static String UserAppFolder = "/data/app/";
	private static String SystemAppFolder = "/system/app/";
	private static String SaveUserAppFolder = "/Backup-Apps/Backup-UserApps/";
	private static String SaveSystemAppFolder = "/Backup-Apps/Backup-SystemApps/";
	
	public ListView SavelistView = null;
	public Button SaveAppButton = null;
	public Button SaveCheckedPositionsButton = null;
	public Button LoadSaveCheckedPositionsButton = null;
	
	public List<ListViewMod> itemList = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

//Datenbanklasse referenzieren	       
	    MysettingsDB = new MySettingsDatenbank(this);	       

//Globales SD-Kartenverzeichnis aus der Datenbank lesen
	    Global_MySettings_Dir = MysettingsDB.readsdcarddir();
       
// Layout setzen		
		setContentView(R.layout.saveapps_list_view);

		itemList = getItemList();
		SavelistView = (ListView) findViewById(R.id.saveapps_list_view);
		SavelistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

// άber den ListViewModAdapter werden zur Laufzeit die Eintrδge der Liste modifiziert!
		SavelistView.setAdapter(new ListViewModAdapter());
		
		SaveAppButton = (Button) findViewById(R.id.saveapp_button);
		SaveAppButton.setOnClickListener(this);
		
		SaveCheckedPositionsButton = (Button) findViewById(R.id.savecheckeditems_button);
		SaveCheckedPositionsButton.setOnClickListener(this);
		
		LoadSaveCheckedPositionsButton = (Button) findViewById(R.id.loadcheckeditems_button);
		LoadSaveCheckedPositionsButton.setOnClickListener(this);
		
	}
	 
	public List<ListViewMod> getItemList() {
			List<ListViewMod> itemList = new LinkedList<ListViewMod>();
// Auslesen der installierten Apps
// Die Liste der Apps wird in das Array packs geschrieben. 
// Anschlieίend der erste Eintrag (zum Test) in die Variable p vom Typ PackageInfo.
// Daraus wird dann der Name der App gelesen und kann dann weiter verwendet werden		
		packs = getPackageManager().getInstalledPackages(0);

// Grφίe des Arrays ermitteln, damit das StringArray in der richtigen Grφίe erzeugt werden kann.
// Dieses nimmt dann die Apps auf, damit alle angezeigt werden kφnnen, MάSSEN alle Array-Elemente
// gefόllt sein!!!
		int maxpacks = packs.size();				
		allappnames = new String[ maxpacks ];
					
// HashMap erzeugen, damit dem Name der App eine ID zugewiesen werden kann und diese Zuordnung auch
// nach der Sortierung noch erhalten bleibt! Weitere Hashmap fόr das Icon der App zum Aufbau der Liste
		packageNameById = new HashMap<String, Integer>();
	
		for (int i = 0; i < maxpacks; i++) 
		{
			PackageInfo p = packs.get(i);
			String packageName = (p.applicationInfo.loadLabel(getPackageManager()).toString () + " " + i);	
			packageNameById.put(packageName, i);
		}

// String-Array allappnames fόllen mit den Namen der einzelnen Apps, am Ende sortieren.
					
		packageNameById.keySet().toArray(allappnames);
		Arrays.sort(allappnames);
			
		for (int i = 0; i < maxpacks; i++)  
		{
			ListViewMod d = new ListViewMod();
			d.setId(i);
			d.setName(allappnames[i].toString());	
			itemList.add(d);
		}
		return itemList;
	}
		
	public class ListViewModAdapter extends ArrayAdapter<ListViewMod> {
		ListViewModAdapter() {
			super(getApplicationContext(), R.layout.saveapps_list_entry, itemList);
		}
// Mit dem Holen der View werden die einzelnen Zeilen der Liste modifiziert! D.h. hier wird der Text
// der Zeilen angepasst, die Checkbox gesetzt und das App-Icon gesetzt (mit Hilfe der Methode getIcon)

		public View getView(int position, View convertView, ViewGroup parent) {
// Das getView-Event wird jedesmal ausgefόhrt, wenn eine Zeile der ListView angezeigt wird!
// Dadurch wird hier ein eigenes Design dargestellt. Die Icons der Apps werden ausgelesen und
// fόr jede Zeile der Liste wird der App-Name sowie der Check-Button dargestellt. Zusδtzlich auf
// checked gesetzt, wenn eine Zeile ausgewδhlt wird (getcheckedItemPosition)
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.saveapps_list_entry, parent, false);
			}

			TextView label = (TextView) row.findViewById(R.id.label);
			CheckedTextView checkBox = (CheckedTextView) row.findViewById(R.id.checkstate);
			ImageView icon = (ImageView) row.findViewById(R.id.icon);
			
// Der Text der Zeile wird gesetzt
			label.setText(itemList.get(position).toString());
// Das Icon der App wird je Zeile gesetzt
			icon.setImageDrawable(geticon (itemList.get(position).toString()));

// Die selektierten Items werden beim MultiSelect der ListView in ein SparseBooleanArray geschrieben
// Das enthδlt einmal die Keys, die selektiert wurden und die Values, also selektiert=true oder
// nicht selektiert=false. Mit Hilfe des Keys wird geprόft, ob die gerade dargestellte Position
// selektiert ist. Wenn ja, dann wird der Value ausgelesen und entsprechend die Checkbox gesetzt.
// Somit kφnnen die selektierten Items als gewδhlt dargestellt werden. Wird ein Item erneut selektiert,
// δndert sich der Value auf false, so dass auch einmal selektierte Items wieder nicht selektiert
// dargestellt werden kφnnnen!
			SparseBooleanArray CheckedItemPositions = SavelistView.getCheckedItemPositions();
			checkBox.setChecked(false);
			
			for (int i = 0; i < CheckedItemPositions.size(); i++)
            {
				if (CheckedItemPositions.keyAt(i) == position) {				
					if (CheckedItemPositions.valueAt(i) == true) {
						checkBox.setChecked(true);	
					}
				}
            }        

			return (row);
		}
	}

	@Override
	public void onClick(View v) {		
		if (v == SaveAppButton) {			
			// Wenn der Sichern Button betδtigt wird, wird ein Bitte Warten Dialog registiert
			// Dann werden die ausgewδhlten Items mitteles des SparseBooleanArray ausgelesen
			// άber einen neuen Thread im Hintergrund (sonst ist das UI so lange blockiert, und der 
			// Bitte warten Dialog erscheint nicht!) werden dann alle selektierten Apps zum Sichern vorgesehen.
			// Dazu wird anhand des ausgewδhlten Items der Name der App aus dem Adapter ausgelesen und an die Methode
			// saveapp όbergeben.
			AppsSichern = ProgressDialog.show(this, "Bitte warten...",
					"Ausgewδhlte Apps werden gesichert",
					true, // zeitlich unbeschrδnkt
					false); // nicht unterbrechbar

			final SparseBooleanArray CheckedItemPositionsToSave = SavelistView.getCheckedItemPositions();

			new Thread() {

				public void run() {

					for (int i = 0; i < CheckedItemPositionsToSave.size(); i++)
		            {
						if (CheckedItemPositionsToSave.valueAt(i) == true) {
								saveapp(SavelistView.getAdapter().getItem(CheckedItemPositionsToSave.keyAt(i)).toString());
							}
		            }        	
					AppsSichern.dismiss(); // dialog schlieίen
				}

			}.start();
			
		}
		
		if (v == SaveCheckedPositionsButton) {
			SelektionSichern = ProgressDialog.show(this, "Bitte warten...",
					"Selektion wird in der Datenbank gespeichert",
					true, // zeitlich unbeschrδnkt
					false); // nicht unterbrechbar

// Alle bisher als gespeichert markierten Apps aus der Datenbank lφschen
			MysettingsDB.initsavedapps();		
// Die selektierten Apps werden in ein BooleanArray geschrieben und dann Zeile fόr Zeile ausgelesen
// Jede selektierte App wird dann in die Datenbank geschrieben			
			final SparseBooleanArray CheckedItemPositionsToSaveSelektion = SavelistView.getCheckedItemPositions();
			
			new Thread() {

				public void run() {
					int packageId = 0;
					String AppName = "";
					
					for (int i = 0; i < CheckedItemPositionsToSaveSelektion.size(); i++)
		            {
						if (CheckedItemPositionsToSaveSelektion.valueAt(i) == true) {
							AppName = SavelistView.getAdapter().getItem(CheckedItemPositionsToSaveSelektion.keyAt(i)).toString();
							
							// Mit dem Namen aus der HashMap die PackageID holen, damit die korrekten weiteren Daten zur App aus dem
							// PackageManager gelesen werden kφnnen
								if (packageNameById.containsKey(AppName))
								{
									packageId = packageNameById.get(AppName);
								}	
										
							// Zu sichernde Apps in die Datenbank schreiben		
								MysettingsDB.insertsaveapp(packageId, AppName);
							}
		            }        	
					SelektionSichern.dismiss(); // dialog schlieίen
				}

			}.start();
		}
		
		if (v == LoadSaveCheckedPositionsButton) {
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			
			Toast.makeText(context, "Gespeicherte Selektion wurde aus der Datenbank geladen", duration).show();
			
			// Gespeicherte Selektion aus der Datenbank lesen
			final HashMap<String, Integer> CheckedSaveAppNames=MysettingsDB.readsaveapps();

			// selektierte Items in der ListView, welche im ListViewModAdapter verwendet wird, leeren
			for (int i = 0; i < packs.size(); i++)
		    	{				
				SavelistView.setItemChecked(i, false);
		        }
					
			// CheckedPositions in der ListView wieder setzen
			for (int i = 0; i < packs.size(); i++)
				{
					if (CheckedSaveAppNames.containsKey(SavelistView.getAdapter().getItem(i).toString()))
					{				
						SavelistView.setItemChecked(i, true);
					}
				}	
		}
	}
	
	public Drawable geticon (String AppName) {
		int packageId = 0;
// Mit dem Namen der App aus der HashMap die PackageID holen
			if (packageNameById.containsKey(AppName))
			{
				packageId = packageNameById.get(AppName);
			}	

// Anschlieίend mit der ID das Icon aus dem PacketManager holen
			PackageInfo p = packs.get(packageId);
			Icon = p.applicationInfo.loadIcon(getPackageManager());
			
		return Icon;
	}

	public void saveapp (String AppName) { 
		int packageId = 0;
// Mit dem Namen aus der HashMap die PackageID holen, damit die korrekten weiteren Daten zur App aus dem
// PackageManager gelesen werden kφnnen
		if (packageNameById.containsKey(AppName))
		{
			packageId = packageNameById.get(AppName);
		}	
		
// Pfad auslesen der aus dem Menό gewδhlten App	
		PackageInfo p = packs.get((int) packageId);
		SourceDirSelectedApp = p.applicationInfo.sourceDir;
		PathSelectedApp = new IOTools().generatepath(SourceDirSelectedApp);
		
// APK-Name aus dem SourceDir auslesen
		APKNameSelectedApp = new IOTools().generateapkname(SourceDirSelectedApp);
		
// Selektierte App vom SourceDir auf die SD-Karte kopieren
		appkopieren (PathSelectedApp, APKNameSelectedApp);
	}

// Methode zum Schreiben auf den externen Speicher.
	public void appkopieren (String SourceDir, String SourceAPK) {      
		String DestPath = Global_MySettings_Dir;
	
		if (SourceDir.equals(UserAppFolder)) {
			DestPath = Global_MySettings_Dir + SaveUserAppFolder;
		}
		
		if (SourceDir.equals(SystemAppFolder)) {
			DestPath = Global_MySettings_Dir + SaveSystemAppFolder;
		}
		
		boolean mExternalStorageWriteable = new IOTools().checkexternalstorage (); 
		if (mExternalStorageWriteable) {
			copyfile (SourceDir, SourceAPK, DestPath, SourceAPK);
		}
	}
	
	public void copyfile(String SourcePath, String SourceFile, String DestPath, String DestFile) {
		// Create a path where we will place our picture in the user's
	    // public pictures directory.  Note that you should be careful about
	    // what you place here, since the user often manages these files.  For
	    // pictures and other media owned by the application, consider
	    // Context.getExternalMediaDir().
		
		// Als DestPath wird nur der Pfad ohne die SDCard angegeben.
		// Hier wird aus dem Betriebssystem der Pfad zur Speicherkarte ausgelesen und der όbergebene
		// Pfad um den SD-Kartenpfad ergδnzt.
		
		File path=Environment.getExternalStoragePublicDirectory(DestPath);
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(DestPath).toString();
		
		File SFile = new File(SourcePath, SourceFile);
		File DFile = new File(SdCardDestPath, DestFile);

	    try {
			// Make sure the Destination Path directory exists.
			path.mkdirs();

	        InputStream is = new FileInputStream (SFile);
	        OutputStream os = new FileOutputStream(DFile);
	        byte[] data = new byte[is.available()];
	        is.read(data);
	        os.write(data);
	        is.close();
	        os.close();
	        } 
	    catch (IOException e) {
	        // Unable to create file, likely because external storage is
	        // not currently mounted.
	        Log.w("ExternalStorage", "Error writing " + DFile, e);
	    }
	}

	public void deletefile() {
	    // Create a path where we will place our picture in the user's
	    // public pictures directory and delete the file.  If external
	    // storage is not currently mounted this will fail.
//	    File path = Environment.getExternalStoragePublicDirectory(
//	            Environment.DIRECTORY_PICTURES);
//	    File file = new File(path, "DemoPicture.jpg");
//	    file.delete();
	}
}
