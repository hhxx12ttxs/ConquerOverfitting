package org.daniruiz.ufobusters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class UFObusters extends MapActivity implements OnTabChangeListener, OnItemClickListener, OnItemLongClickListener, OnClickListener {
	
	private MapController controller;
    private MapView map;
    public TabHost tabHost;
    private XYPlot mySimpleXYPlot;
    private int actualMonth;
    private int actualYear;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        GregorianCalendar c = new GregorianCalendar();
        actualMonth = c.get(Calendar.MONTH)+1;
		actualYear = c.get(Calendar.YEAR);
		
		Resources res = getResources();
        
        tabHost=(TabHost) findViewById(R.id.tabHost);
        tabHost.setOnTabChangedListener(this);
        tabHost.setup();
        
        initMapView();
		initMyLocation();
        
        TabSpec spec1 = tabHost.newTabSpec("Mapa");
        spec1.setContent(R.id.map);
        spec1.setIndicator("Mapa", res.getDrawable(R.drawable.map));
        
        TabSpec spec2 = tabHost.newTabSpec("Lista");
        spec2.setContent(R.id.list);
        spec2.setIndicator("Lista", res.getDrawable(R.drawable.list));
        
        TabSpec spec3 = tabHost.newTabSpec("Gráfica");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("Gráfica", res.getDrawable(R.drawable.plot));
        
        Button back = (Button) findViewById(R.id.backButton);
        back.setOnClickListener(this);
        
        Button next = (Button) findViewById(R.id.nextButton);
        next.setOnClickListener(this);
        
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        
        generateList();
        generatePlot();
    }
    
    private String DB_NAME = "ufobuster.db";
	private String TABLE = "sights";
	private String ADDR_DB = "address";
	private String COORD_X_DB = "coord_X";
	private String COORD_Y_DB = "coord_Y";
	private String INF_DB = "description";
	private String DAY_DB = "day";
	private String MONTH_DB = "month";
	private String YEAR_DB = "year";

	private void generateList() {
    	SQLiteDatabase myDB = null;
        
        myDB = this.openOrCreateDatabase(DB_NAME, 1, null);
        ArrayList<String> sights = new ArrayList<String>();
        
        myDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" + ADDR_DB + " TEXT NOT NULL PRIMARY KEY, " + COORD_X_DB + " INTEGER NOT NULL, " + COORD_Y_DB + " INTEGER NOT NULL, " + INF_DB + " TEXT, " + DAY_DB + " INTEGER, " + MONTH_DB + " INTEGER, " + YEAR_DB + " INTEGER);");
        
        String[] FROM = { ADDR_DB, DAY_DB, MONTH_DB, YEAR_DB };
        String ORDER_BY = YEAR_DB + " ASC, " + MONTH_DB + " ASC, " + DAY_DB + " ASC, " + ADDR_DB + " ASC";
        
        Cursor c = myDB.query(TABLE, FROM, null, null, null, null, ORDER_BY);
        
        //Obtenemos solo la direcciďż˝n y la fecha
        startManagingCursor(c);
        while(c.moveToNext()) {
        	sights.add(c.getString(0)+" "+String.valueOf(c.getInt(1))+"/"+String.valueOf(c.getInt(2))+"/"+String.valueOf(c.getInt(3)));
        }
        
        c.close();
        
        if(myDB!=null) {
        	myDB.close();
        }
        
        ListView lv = (ListView) findViewById(R.id.list);
        
        ArrayAdapter<String> adapter =  new ArrayAdapter<String>(this, R.layout.list, sights);
  	  	lv.setAdapter(adapter);
  	  	
  	  	lv.setOnItemClickListener(this);
  	  	lv.setOnItemLongClickListener(this);
	}
	
	String meses[] = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre" };
	
	void generatePlot() {
		TextView tv = (TextView) findViewById(R.id.plotViewTitle);
		tv.setText("Avistamientos durante " + meses[actualMonth-1] + " del " + String.valueOf(actualYear));
		
		SQLiteDatabase myDB = null;
        
        myDB = this.openOrCreateDatabase(DB_NAME, 1, null);
        
        myDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" + ADDR_DB + " TEXT NOT NULL PRIMARY KEY, " + COORD_X_DB + " INTEGER NOT NULL, " + COORD_Y_DB + " INTEGER NOT NULL, " + INF_DB + " TEXT, " + DAY_DB + " INTEGER, " + MONTH_DB + " INTEGER, " + YEAR_DB + " INTEGER);");
        
        /*
         * Solo obtendremos los días donde se han visto avistamientos ordenados por menor a mayor.
         */
        String[] FROM = { DAY_DB };
        String ORDER_BY = YEAR_DB + " DESC, " + MONTH_DB + " DESC, " + DAY_DB + " DESC";
        
        Cursor c = myDB.query(TABLE, FROM, MONTH_DB+"="+actualMonth+" AND "+YEAR_DB+"="+actualYear, null, null, null, ORDER_BY);
        
        /*
         * Preparamos el vector de Number dependiendo del mes que tengamos que mostrar
         */
        Number[] series1Numbers;
        int daysOfMonth = 0;
        
        if(actualMonth==1 || actualMonth==3 || actualMonth==5 || actualMonth==7 || actualMonth==8 || actualMonth==10 || actualMonth==12) {
        	daysOfMonth = 32;
        	series1Numbers = new Number[daysOfMonth];
        	for(int i = 0; i<daysOfMonth; ++i) series1Numbers[i] = 0;
        } else if(actualMonth==2) {
        	daysOfMonth = 29;
        	series1Numbers = new Number[daysOfMonth];
        	for(int i = 0; i<daysOfMonth; ++i) series1Numbers[i] = 0;
        } else {
        	daysOfMonth = 31;
        	series1Numbers = new Number[daysOfMonth];
        	for(int i = 0; i<daysOfMonth; ++i) series1Numbers[i] = 0;
        }
        
        int max = 0;	//sirve para establecer el máximo Y valor del gráfico
        int aux = 0;
        int dayAux = 0;
        
        startManagingCursor(c);
        while(c.moveToNext()) {
        	int dayDB = c.getInt(0);
        	if(max<aux) max = aux;
        	if(dayAux==0) {
        		dayAux = dayDB;
        		++aux;
        	} else {
        		if(dayAux==dayDB) ++aux;
        		else {
        			series1Numbers[dayAux] = aux;
        			aux = 1;
        			dayAux = dayDB;
        		}
        	}
        }
        if(dayAux!=0) series1Numbers[dayAux] = aux;
        
        c.close();
        
        if(myDB!=null) {
        	myDB.close();
        }
        
     // Inicializamos el objeto XYPlot búscandolo desde el layout:
        mySimpleXYPlot = (XYPlot) findViewById(R.id.plot);
        mySimpleXYPlot.clear();
        mySimpleXYPlot.setDomainLabel("Días de "+meses[actualMonth-1]);
        mySimpleXYPlot.setRangeLabel("Número de avistamientos");
 
        // Ańadimos Línea Número UNO:
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),  // Array de datos
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, //Valores verticales
                "Número de avistamientos"); // Nombre de la serie
 
        // Modificamos los colores de la primera serie
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(200, 0, 0),                   // Color de la lďż˝nea
                Color.rgb(0, 0, 0),                   // Color del punto
                Color.rgb(190, 150, 150));              // Relleno
 
        // Una vez definida la serie (datos y estilo), la ańadimos al panel
        mySimpleXYPlot.addSeries(series1, series1Format);
        
        if(max==0) ++max;
        
        //Tuneamos el grafico
        mySimpleXYPlot.setDomainValueFormat(new DecimalFormat("#"));
        mySimpleXYPlot.setDomainBoundaries(1, daysOfMonth-1, BoundaryMode.FIXED);
        mySimpleXYPlot.setDomainStepValue(daysOfMonth);
        if(daysOfMonth==32) {
        	mySimpleXYPlot.setTicksPerDomainLabel(2);
        } else if(daysOfMonth==31) {
        	mySimpleXYPlot.setTicksPerDomainLabel(3);
        } else if(daysOfMonth==28) {
        	mySimpleXYPlot.setTicksPerDomainLabel(2);
        }
        mySimpleXYPlot.setRangeUpperBoundary(max+2, BoundaryMode.FIXED);
        mySimpleXYPlot.setRangeStepValue(max+3);
        mySimpleXYPlot.setTicksPerRangeLabel(1);
        mySimpleXYPlot.disableAllMarkup();
        
        /*
         * Generamos la vista de nueva para mostrar la nueva gráfica
         */
        ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
        sv.postInvalidate();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private void initMapView() {
		map = (MapView) findViewById(R.id.map);
		controller = map.getController();
		map.setSatellite(false);
		map.setBuiltInZoomControls(false);
		List<Overlay> capas = map.getOverlays();
		OverlayMap om = new OverlayMap(this);
		capas.add(om);
		map.postInvalidate();
	}
	
	private MyLocationOverlay overlay;
	
	private void initMyLocation() {
		overlay = new MyLocationOverlay(this, map);
		overlay.enableMyLocation();
		overlay.disableCompass();
		overlay.runOnFirstFix(new Runnable() {
			public void run() {
				controller.setZoom(14);
				controller.animateTo(overlay.getMyLocation());
			}
		});
		map.getOverlays().add(overlay);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(!hasFocus) {
			overlay.disableMyLocation();
		} else {
			overlay.enableMyLocation();
		}
	}

	public void onTabChanged(String tabId) {
		if(tabId.equals("Lista")) generateList();
		else if(tabId.equals("Gráfica")) generatePlot();
	}

	public boolean onItemLongClick(AdapterView<?> parent, final View v, int position, long id) {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(150);
		
		new AlertDialog.Builder(this).setTitle(R.string.text_dialog).setItems(R.array.opciones, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				if(i==0) {	//Mostrar informaciďż˝n del avistamiento
					TextView tv2 = (TextView) v;
					String address = tv2.getText().toString();
			        String[] aux = address.split(" ");
			        address = new String();
			        for(int x=0; x<aux.length-1; ++x) {
						if(x==0) address = aux[x];
						else address = address + " " + aux[x];
					}
			        
					SQLiteDatabase myDB = null;
			        
			        myDB = openOrCreateDatabase(DB_NAME, 1, null);
			        
			        myDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" + ADDR_DB + " TEXT NOT NULL PRIMARY KEY, " + COORD_X_DB + " INTEGER NOT NULL, " + COORD_Y_DB + " INTEGER NOT NULL, " + INF_DB + " TEXT, " + DAY_DB + " INTEGER, " + MONTH_DB + " INTEGER, " + YEAR_DB + " INTEGER);");
			        
			        String[] FROM = { ADDR_DB, COORD_X_DB, COORD_Y_DB, INF_DB, DAY_DB, MONTH_DB, YEAR_DB };
			        
			        Cursor c = myDB.query(TABLE, FROM, ADDR_DB+" = '"+address+"'", null, null, null, null);
			        
			        String addr = new String();
			        int x = 0;
			        int y = 0;
			        String inf = new String();
			        int day = 0;
			        int month = 0;
			        int year = 0;
			        
			        //Obtenemos solo la direcciďż˝n y la fecha
			        startManagingCursor(c);
			        if(c.moveToNext()) {
			        	addr = c.getString(0);
			        	x = c.getInt(1);
			        	y = c.getInt(2);
			        	inf = c.getString(3);
			        	day = c.getInt(4);
			        	month = c.getInt(5);
			        	year = c.getInt(6);
			        }
			        
			        c.close();
			        
			        if(myDB!=null) {
			        	myDB.close();
			        }
			        
			        Intent intent = new Intent(getApplicationContext(), viewSight.class);
			        intent.putExtra("dir", addr);
			        intent.putExtra("X", x);
			        intent.putExtra("Y", y);
			        intent.putExtra("inf", inf);
			        intent.putExtra("day", day);
			        intent.putExtra("month", month);
			        intent.putExtra("year", year);
			        startActivity(intent);
			        
				} else if(i==1) {	//Eliminar avistamiento
					TextView tv2 = (TextView) v;
			    	SQLiteDatabase myDB = null;
			        myDB = openOrCreateDatabase(DB_NAME, 1, null);
			        myDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" + ADDR_DB + " TEXT NOT NULL PRIMARY KEY, " + COORD_X_DB + " INTEGER NOT NULL, " + COORD_Y_DB + " INTEGER NOT NULL, " + INF_DB + " TEXT, " + DAY_DB + " INTEGER, " + MONTH_DB + " INTEGER, " + YEAR_DB + " INTEGER);");
			        
			        String address = tv2.getText().toString();
			        String[] aux = address.split(" ");
			        address = new String();
			        for(int x=0; x<aux.length-1; ++x) {
						if(x==0) address = aux[x];
						else address = address + " " + aux[x];
					}
			        			        
			        myDB.delete(TABLE, ADDR_DB+" = '"+address+"'", null);
			        if(myDB!=null) myDB.close();
			        generateList();
				}
			}
		}).show();
		return false;
	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		TextView tv = (TextView) v;
		String address = tv.getText().toString();
		
		String[] aux = address.split(" ");
		address = new String();
		for(int i=0; i<aux.length-1; ++i) {
			if(i==0) address = aux[i];
			else address = address + " " + aux[i];
		}
				
		SQLiteDatabase myDB = null;
        
        myDB = openOrCreateDatabase(DB_NAME, 1, null);
        
        myDB.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" + ADDR_DB + " TEXT NOT NULL PRIMARY KEY, " + COORD_X_DB + " INTEGER NOT NULL, " + COORD_Y_DB + " INTEGER NOT NULL, " + INF_DB + " TEXT, " + DAY_DB + " INTEGER, " + MONTH_DB + " INTEGER, " + YEAR_DB + " INTEGER);");
        
        String[] FROM = { COORD_X_DB, COORD_Y_DB};
        String WHERE = ADDR_DB + " = '"+ address+"'"; 
        
        Cursor c = myDB.query(TABLE, FROM, WHERE, null, null, null, null);
        
        startManagingCursor(c);
        
        GeoPoint point = null;
        if(c.moveToNext()) {
			point = new GeoPoint(c.getInt(0), c.getInt(1));
		}
        
        c.close();
        if(myDB!=null) myDB.close();
        
        tabHost.setCurrentTabByTag("Mapa");
        
        if(point!=null) {
	        controller.animateTo(point);
        }
		while(map.getZoomLevel()!=14) {
			if(map.getZoomLevel()<14) {
				controller.zoomIn();
			} else if(map.getZoomLevel()>14) {
				controller.zoomOut();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.about) {
			Intent intent = new Intent(this, About.class);
			startActivity(intent);
		} else if(item.getItemId()==R.id.changeToSatellite) {
			if(map.isSatellite()) map.setSatellite(false);
			else map.setSatellite(true);
		}
		return true;
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.backButton:
			--actualMonth;
			if(actualMonth==0) {
				actualMonth = 12;
				--actualYear;
			}
			break;
		case R.id.nextButton:
			++actualMonth;
			if(actualMonth==13) {
				actualMonth = 1;
				++actualYear;
			}
			break;
		}
		//Refrescamos la gráfica
		generatePlot();
	}
}
