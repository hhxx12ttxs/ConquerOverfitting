package org.moca.odk.activities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.moca.R;
import org.moca.odk.database.ClinicAdapter;
import org.moca.odk.openmrs.Constants;
import org.moca.odk.openmrs.Patient;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ObservationChartActivity extends Activity {

	private Patient mPatient;
	private String mObservationFieldName;

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	private GraphicalView mChartView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.observation_chart);
		
		if (!ClinicAdapter.storageReady()) {
			showCustomToast(getString(R.string.error, R.string.storage_error));
			finish();
		}

		// TODO Check for invalid patient IDs
		String patientIdStr = getIntent().getStringExtra(Constants.KEY_PATIENT_ID);
		Integer patientId = Integer.valueOf(patientIdStr);
		mPatient = getPatient(patientId);
		
		mObservationFieldName = getIntent().getStringExtra(Constants.KEY_OBSERVATION_FIELD_NAME);
		
		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.view_patient_detail));
		
		TextView textView = (TextView) findViewById(R.id.title_text);
		if (textView != null) {
			//textView.setText(mObservationFieldName);
			textView.setText("BLOOD PRESSURE");
		}
		textView.setTextColor(getResources().getColor(R.drawable.black));
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setLineWidth(3.0f);
		r.setColor(getResources().getColor(R.color.chart_red));
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		mRenderer.addSeriesRenderer(r);
		
		r = new XYSeriesRenderer();
		r.setLineWidth(3.0f);
		r.setColor(getResources().getColor(R.color.solid_blue));
		r.setPointStyle(PointStyle.DIAMOND);
		r.setFillPoints(true);
		mRenderer.addSeriesRenderer(r);
		
		mRenderer.setDisplayChartValues(true);
		mRenderer.setShowLegend(true);
		//mRenderer.setXTitle("Encounter Date");
		//mRenderer.setAxisTitleTextSize(18.0f);
		mRenderer.setLabelsTextSize(9.0f);
		//mRenderer.setXLabels(10);
		mRenderer.setShowGrid(true);
		mRenderer.setLabelsColor(getResources().getColor(android.R.color.black));
		mRenderer.setXLabels(0);
		
		//TODO Change to a max number varaiable
		mRenderer.setXAxisMin(0.0);
		//mRenderer.setXAxisMax(8.0);
	}
	
	private Patient getPatient(Integer patientId) {

		Patient p = null;
		ClinicAdapter ca = new ClinicAdapter();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		ca.open();
		Cursor c = ca.fetchPatient(patientId);

		if (c != null && c.getCount() > 0) {
			int patientIdIndex = c
					.getColumnIndex(ClinicAdapter.KEY_PATIENT_ID);
			int identifierIndex = c
					.getColumnIndex(ClinicAdapter.KEY_IDENTIFIER);
			int givenNameIndex = c
					.getColumnIndex(ClinicAdapter.KEY_GIVEN_NAME);
			int familyNameIndex = c
					.getColumnIndex(ClinicAdapter.KEY_FAMILY_NAME);
			int middleNameIndex = c
					.getColumnIndex(ClinicAdapter.KEY_MIDDLE_NAME);
			int birthDateIndex = c
					.getColumnIndex(ClinicAdapter.KEY_BIRTH_DATE);
			int genderIndex = c.getColumnIndex(ClinicAdapter.KEY_GENDER);
			
			p = new Patient();
			p.setPatientId(c.getInt(patientIdIndex));
			p.setIdentifier(c.getString(identifierIndex));
			p.setGivenName(c.getString(givenNameIndex));
			p.setFamilyName(c.getString(familyNameIndex));
			p.setMiddleName(c.getString(middleNameIndex));
			try {
				p.setBirthDate(df.parse(c.getString(birthDateIndex)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			p.setGender(c.getString(genderIndex));
		}

		if (c != null) {
			c.close();
		}
		ca.close();

		return p;
	}
	
	private void getObservations(Integer patientId, String fieldName) {
		
		ClinicAdapter ca = new ClinicAdapter();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat ldf = new SimpleDateFormat("MM/dd/yy");
		
		ca.open();
		Cursor c  = ca.fetchPatientObservation(patientId, "SYSTOLIC BLOOD PRESSURE");
		Cursor cd = ca.fetchPatientObservation(patientId, "DIASTOLIC BLOOD PRESSURE");
		
		if (c != null && c.getCount() >= 0 && cd != null && cd.getCount() >= 0 ) {
			
			XYSeries series;
			XYSeries series1;
			if (mDataset.getSeriesCount() > 0) {
				series1 = mDataset.getSeriesAt(1);
				series = mDataset.getSeriesAt(0);
				series1.clear();
				series.clear();
			} else {
				//series = new XYSeries(fieldName);
				series = new XYSeries("Systolic");
				series1 = new XYSeries("Diastolic");
				mDataset.addSeries(series);
				mDataset.addSeries(series1);
			}

			int valueIntIndex = c.getColumnIndex(ClinicAdapter.KEY_VALUE_INT);
			int valueNumericIndex = c.getColumnIndex(ClinicAdapter.KEY_VALUE_NUMERIC);
			int encounterDateIndex = c.getColumnIndex(ClinicAdapter.KEY_ENCOUNTER_DATE);
			int dataTypeIndex = c.getColumnIndex(ClinicAdapter.KEY_DATA_TYPE);
			
			int valueIntIndexD = cd.getColumnIndex(ClinicAdapter.KEY_VALUE_INT);
			int valueNumericIndexD = cd.getColumnIndex(ClinicAdapter.KEY_VALUE_NUMERIC);
			int encounterDateIndexD = cd.getColumnIndex(ClinicAdapter.KEY_ENCOUNTER_DATE);
			int dataTypeIndexD = cd.getColumnIndex(ClinicAdapter.KEY_DATA_TYPE);

			/*
			do {
				try {
					Date encounterDate = df.parse(c.getString(encounterDateIndex));
					int dataType = c.getInt(dataTypeIndex);
					
					double value;
					if (dataType == Constants.TYPE_INT) {
						value = c.getInt(valueIntIndex);
						series.add(encounterDate.getTime(), value);
					} else if (dataType == Constants.TYPE_FLOAT) {
						value = c.getFloat(valueNumericIndex);
						series.add(encounterDate.getTime(), value);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}

			} while(c.moveToNext());
			*/
			//Start
			if(c.getCount() > 7 && cd.getCount() >7) {
				cd.moveToLast();
				c.move(-6);
				c.moveToLast();
				c.move(-6);
				mRenderer.setXAxisMax(8);
			} else {
				mRenderer.setXAxisMax(c.getCount()+1);
			}
			
			double min = 0;
			double max = 0;
			int i = 1;
			do {
				
				try {
					Date encounterDate = df.parse(c.getString(encounterDateIndex));
					int dataType = c.getInt(dataTypeIndex);
					
					double value;
					if (dataType == Constants.TYPE_INT) {
						value = c.getInt(valueIntIndex);
						min = value;
						if(value > max)
							max = value;
						series.add(i, value);
						mRenderer.addTextLabel(i, ldf.format(encounterDate));
					} else if (dataType == Constants.TYPE_FLOAT) {
						value = c.getFloat(valueNumericIndex);
						min = value;
						if(value > max)
							max = value;
						series.add(i, value);
						mRenderer.addTextLabel(i, ldf.format(encounterDate));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				i++;
			} while(c.moveToNext());
			mRenderer.setYAxisMax(max +5);
			
			i = 1;		
			do {
				
				try {
					Date encounterDate = df.parse(cd.getString(encounterDateIndex));
					int dataType = cd.getInt(dataTypeIndex);
					
					double value;
					if (dataType == Constants.TYPE_INT) {
						value = cd.getInt(valueIntIndex);
						if(value < min)
							min = value;
						series1.add(i, value);
						mRenderer.addTextLabel(i, ldf.format(encounterDate));
					} else if (dataType == Constants.TYPE_FLOAT) {
						value = cd.getFloat(valueNumericIndex);
						if(value < min)
							min = value;
						series1.add(i, value);
						mRenderer.addTextLabel(i, ldf.format(encounterDate));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				Log.v("SUBOK", "Counter is: " + i);
				i++;
			} while(cd.moveToNext());
			mRenderer.setYAxisMin(min - 10);
			
			//End
			/*
			List <String> val = getTestData();
			String[] latestVal;
			
			if(val.size() > 7) {
				latestVal = new String[7];
				
				for(int i = 0; i < 7; i++) {
					latestVal[i] = val.get(val.size()-7 + i);
				}
			} else {
				latestVal = new String[val.size()];
				
				for(int i = 0; i < val.size(); i++) {
					latestVal[i] = val.get(i);
				}
			}
			
			for(int i = 0; i < latestVal.length; i++) {
				Double[] pVal = bpVal(latestVal[i]);
				series.add(i+1, pVal[0]);
				mRenderer.addTextLabel(i+1, ldf.format(new Date()));
				series1.add(i+1, pVal[1]);
			}
			
			mRenderer.setXAxisMax(latestVal.length +1);
			*/
		}

		if (c != null) {
			c.close();
		}
		ca.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (mPatient != null && mObservationFieldName != null) {
			getObservations(mPatient.getPatientId(), mObservationFieldName);
		}
		
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			mChartView = ChartFactory.getBarChartView(this, mDataset,
					mRenderer,BarChart.Type.STACKED);
			//mChartView = ChartFactory.getLineChartView(this, mDataset,
			//		mRenderer);
			
			//mChartView = ChartFactory.getTimeChartView(this, mDataset,
			//		mRenderer, null);
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		} else {
			mChartView.repaint();
		}
	}
	
	private void showCustomToast(String message) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.toast_view, null);

		// set the text in the view
		TextView tv = (TextView) view.findViewById(R.id.message);
		tv.setText(message);

		Toast t = new Toast(this);
		t.setView(view);
		t.setDuration(Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	private List<String> getTestData()
	{
		List<String> listing = java.util.Arrays.asList(
								"123/12"
							   ,"140/33"
							   ,"100/60"
							   ,"119/59"
							   ,"120/68"
							   ,"118/71"
							   ,"90/65"
							   ,"97/66"
							   ,"110/69"
							   ,"100/70"
							   ,"178/73"
							   ,"180/66"
								);
		return listing;
	}
	
	private Double[] bpVal(String bpString)
	{
		String[] val = bpString.split("/");
		
		Double[] pVal = new Double[2];
		
		pVal[0] = Double.parseDouble(val[0]);
		pVal[1] = Double.parseDouble(val[1]);
		
		return pVal;
	}
}

