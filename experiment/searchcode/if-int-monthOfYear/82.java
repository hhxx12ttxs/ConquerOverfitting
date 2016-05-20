/*
 * Copyright 2012, Angel Abad
 *
 * This file is part of Once.
 *
 * Once is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Once is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Once.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pastelero.once;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.pastelero.once.service.UpdaterService;
import net.pastelero.once.storage.DataStorage;
import net.pastelero.once.storage.OnceAdapter;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class OnceActivity extends SherlockActivity {

	DataStorage dataStorage;
	OnceAdapter adapter;

	ImageButton addNumberButton;
	ListView listTimeline;

	//Cursor para el adapter
	Cursor cursor;

	//Receiver para UpdaterService
	ChecksReceiver checksReceiver;
	IntentFilter updaterFilter;

	static final int DATE_DIALOG_ID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.once);

		dataStorage = new DataStorage(this);

		// Receiver para UpdaterService
		updaterFilter = new IntentFilter("net.pastelero.once.NEW_CHECK");

		listTimeline = (ListView) findViewById(R.id.listTimeLine);
		listTimeline.setOnItemClickListener(numberDetailsListener);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		cursor = dataStorage.getAllNumbers();
		startManagingCursor(cursor);

		// Crear el adapter
		adapter = new OnceAdapter(this, cursor);
		listTimeline.setAdapter(adapter);

		// Registra ChecksReceiver
		checksReceiver = new ChecksReceiver();
		registerReceiver(checksReceiver, updaterFilter);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// DERregistramos el Checksreceiver
		unregisterReceiver(checksReceiver);
	}

	// Creacion del menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.once, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.btnScanNumber:
			IntentIntegrator integrator = new IntentIntegrator(OnceActivity.this);
			integrator.setTitleByID(R.string.BarcodeTitle);
			integrator.setMessageByID(R.string.BarcodeMessage);
			integrator.setButtonYesByID(R.string.YES);
			integrator.setButtonNoByID(R.string.NO);
			integrator.initiateScan();
			break;
		case R.id.btnAddNumber:
			Intent addNumberIntent = new Intent(getApplicationContext(), AddNumberActivity.class);
			startActivity(addNumberIntent);
			break;
		case R.id.btnSearchDate:
			showDialog(DATE_DIALOG_ID);
			break;
		case R.id.itemPreferences:
			startActivity(new Intent(this, PreferencesActivity.class));
			break;
		case R.id.itemRefresh:
			startService(new Intent(this, UpdaterService.class));
			break;
		}

		return true;
	}

	// Configuramos el lanzamiento de los diferentes dialogos
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DATE_DIALOG_ID:
			// TODO Extraer esto de aqui
			// Obtenemos la fecha actual
			final Calendar c = Calendar.getInstance();
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH);
			int mDay = c.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
		}

		return null;
	}

	// Callback para el DatePicker buscar por fecha
	private DatePickerDialog.OnDateSetListener mDateSetListener =
			new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year,
				int monthOfYear, int dayOfMonth) {
			
			// Ejecuta la tarea asincrona
			CheckByDate checkByDate = new CheckByDate(year, monthOfYear, dayOfMonth);
			checkByDate.execute();
		}
	};

	// Se lanza cuando recibimos los datos de barcodeScanner
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (resultCode != 0) {
			if (scanResult != null) {
				try {
					dataStorage.addBarcodeNumber(scanResult.getContents());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

	}

	// Detalles cuando pinchamos en un numero
	private OnItemClickListener numberDetailsListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2, long id) {

			Intent numberDeatilsIntent = new Intent(v.getContext(), NumberDetailsActivity.class);
			numberDeatilsIntent.putExtra("NUMBER_ID", (int)id);
			startActivity(numberDeatilsIntent);

		}

	};

	// Receiver para UpdaterService
	class ChecksReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			cursor.requery();
			adapter.notifyDataSetChanged();
			Log.d("ChecksReceiver", "onReceived");

		}

	}
	
	// Clase asincrona para buscar la fecha
	private class CheckByDate extends AsyncTask<Void, Void, Integer> {
		ProgressDialog checkDialog;
		
		Integer year;
		Integer monthOfYear;
		Integer dayOfMonth;
		
		public CheckByDate(int myear, int mmonthOfYear, int mdayOfMonth) {
			this.year = myear;
			this.monthOfYear = mmonthOfYear;
			this.dayOfMonth = mdayOfMonth;
		}
		
		@Override
		protected void onPreExecute() {
			checkDialog = ProgressDialog.show(OnceActivity.this,
					getString(R.string.Updating),
					getString(R.string.VerifyingNumber));
		}
		
		// TODO Chequear si hay internet
		@Override
		protected Integer doInBackground(Void... params) {
			Number number = null;
			WebCheck webCheck;
			Integer result;
			
			// Si estamos conectados a internet
			if (Utils.isOnline() == true) {
				// Formateamos la fecha y Chequeamos el numero
				webCheck = new WebCheck();
				try {
					number = webCheck.checkByDate(year, monthOfYear, dayOfMonth);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// Si no hay internet sale y retorna 1
				result = 1;
				return result;
			}

			// Si no hay numero devuelve un toast

			if (number != null) {
				Intent searchByDateIntent = new Intent(getApplicationContext(), SearchByDayActivity.class);
				searchByDateIntent.putExtra("number", number.getNumber());
				searchByDateIntent.putExtra("serie", number.getSerie());
				searchByDateIntent.putExtra("date", number.getShortFormatDate(getApplicationContext()));
				startActivity(searchByDateIntent);
				result = 0;
			} else {
				result = 2;
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			checkDialog.dismiss();
			// TODO Estos toast estan tambien en NumberDetailsActivity
			if (result == 1) {
				Toast.makeText(getApplicationContext(), getString(R.string.NoInternetConnected), Toast.LENGTH_LONG).show();
			} else if (result == 2) {
				Toast.makeText(getApplicationContext(), getString(R.string.NoDataYet), Toast.LENGTH_SHORT).show();
			}
		}
		
		
		
	}
}

