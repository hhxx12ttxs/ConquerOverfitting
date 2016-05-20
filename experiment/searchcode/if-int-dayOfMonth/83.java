package com.perfectial.vacationmanager.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.perfectial.vacationmanager.R;
import com.perfectial.vacationmanager.db.VacationContentProvider;
import com.perfectial.vacationmanager.fragment.AlertDialogFragment;
import com.perfectial.vacationmanager.netapi.NetApi;
import com.perfectial.vacationmanager.tools.DialogManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RequestActivity extends FragmentActivity implements DialogManager.DialogCreator, DialogManager.DialogPreparer
{
	private static final String DIALOG_TEXT_TAG = "dialog";
	private static final String EXTRA_DAY_KEY = "dialog.peek_date.EXTRA.day";
	private static final String EXTRA_MONTH_KEY = "dialog.peek_date.EXTRA.month";
	private static final String EXTRA_YEAR_KEY = "dialog.peek_date.EXTRA.year";
	private static final String TAG = "RequestActivity";
	private String mCeoEmail;
	private String mHrEmail;
	private String mThirdEmail;
	
	private Calendar mCalendarFrom = Calendar.getInstance();
	private Calendar mCalendarTo = Calendar.getInstance();
	private Calendar mCurrentCalendar = null;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
	private int mPeriodType = VacationContentProvider.TYPE_VACATION;

	private static String[] sRecipientProjection = { VacationContentProvider.COLUMN_ID, VacationContentProvider.COLUMN_NAME,
		VacationContentProvider.COLUMN_EMAIL, VacationContentProvider.COLUMN_TYPE };

	private DatePickerDialog.OnDateSetListener mDateSetListener =
		    new DatePickerDialog.OnDateSetListener()
	{
		@Override
		public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth )
		{
			if( mCurrentCalendar != null )
			{
				updateCalendar( mCurrentCalendar, year, monthOfYear, dayOfMonth );

				if( mCurrentCalendar == mCalendarFrom && mCalendarTo.before( mCalendarFrom ) )
				{
					updateCalendar( mCalendarTo, year, monthOfYear, dayOfMonth );
				}
				
				updateDateFields();
			}
		}

		private void updateCalendar( Calendar calendar, int year, int monthOfYear, int dayOfMonth )
		{
			calendar.set( Calendar.YEAR, year );
			calendar.set( Calendar.MONTH, monthOfYear );
			calendar.set( Calendar.DAY_OF_MONTH, dayOfMonth );
		}
	};
	private TextView mDateFromText;
	private TextView mDateToText;
	private Button mSelectFromDateButton;
	private Button mSelectToDateButton;
	private View mErrorView;
	private SubmitTask mSubmitTask;
	private View mProgressView;
	private View mMainView;
	private EditText mEditMessage;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_request );
		
		String email = getEmail();
		
		Spinner spn = ( Spinner )findViewById( R.id.spn_recipients );
		
		String where = VacationContentProvider.COLUMN_TYPE + "!=" + VacationContentProvider.TYPE_CEO + " AND " +
				VacationContentProvider.COLUMN_TYPE + "!=" + VacationContentProvider.TYPE_HR + " AND " +
				VacationContentProvider.COLUMN_EMAIL + "!=\'" + email +"\'";
		Cursor c = getContentResolver().query( VacationContentProvider.RECIPIENTS_URI, sRecipientProjection, where, null, VacationContentProvider.COLUMN_EMAIL + " Asc" );
		
		int[] to = { android.R.id.text1 };
		String[] from = { VacationContentProvider.COLUMN_NAME };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter( this, R.layout.recipient_spn, c, from, to,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );
		
		spn.setAdapter( adapter );
		spn.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
		{

			@Override
			public void onItemSelected( AdapterView< ? > parent, View view, int position, long id )
			{
				mThirdEmail = null;
				SimpleCursorAdapter adapter = ( SimpleCursorAdapter )parent.getAdapter();
				Cursor c = adapter.getCursor();
				c.moveToPosition( ( int )position );
				mThirdEmail = c.getString( c.getColumnIndex( VacationContentProvider.COLUMN_EMAIL ) );
				
				Log.d( TAG, "Third emai = " + mThirdEmail );
			}

			@Override
			public void onNothingSelected( AdapterView< ? > parent )
			{
				//mThirdEmail = null;
			}
		} );
		
		spn = ( Spinner )findViewById( R.id.spn_types );
		spn.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
		{

			@Override
			public void onItemSelected( AdapterView< ? > parent, View ciew, int position, long id )
			{
				switch( position )
				{
					case 0:
						mPeriodType = VacationContentProvider.TYPE_VACATION;
						break;

					case 1:
						mPeriodType = VacationContentProvider.TYPE_DAYOFF;
						break;

					case 2:
						mPeriodType = VacationContentProvider.TYPE_ILLNESS;
						break;

					case 3:
						mPeriodType = VacationContentProvider.TYPE_HOMEWORKING;
						break;

					default:
						mPeriodType = VacationContentProvider.TYPE_VACATION;
						break;
				}
			}

			@Override
			public void onNothingSelected( AdapterView< ? > parent )
			{
			}
		} );
		
		setMainRecipients( ( TextView )findViewById( android.R.id.text1 ), ( TextView )findViewById( android.R.id.text2 ) );
		
		mSelectFromDateButton = ( Button )findViewById( R.id.bnt_select_from_date );
		mSelectFromDateButton.setOnClickListener( new View.OnClickListener()
		{
			
			@Override
			public void onClick( View v )
			{
				Bundle args = getDateBundle( mCalendarFrom );
				mCurrentCalendar = mCalendarFrom;
				DialogManager.getInstance().showDialog( R.id.dlg_peek_date, RequestActivity.this, RequestActivity.this,
						getSupportFragmentManager(), DIALOG_TEXT_TAG, args );
			}
		} );

		mSelectToDateButton = ( Button )findViewById( R.id.bnt_select_to_date );
		mSelectToDateButton.setOnClickListener( new View.OnClickListener()
		{
			
			@Override
			public void onClick( View v )
			{
				Bundle args = getDateBundle( mCalendarTo );
				mCurrentCalendar = mCalendarTo;
				DialogManager.getInstance().showDialog( R.id.dlg_peek_date, RequestActivity.this, RequestActivity.this,
						getSupportFragmentManager(), DIALOG_TEXT_TAG, args );
			}
		} );
		
		mErrorView = findViewById( R.id.error_view );
		mDateFromText = ( TextView )findViewById( R.id.date_from );
		mDateToText = ( TextView )findViewById( R.id.date_to );
		
		mCalendarFrom.add( Calendar.DAY_OF_MONTH, 1 );
		mCalendarTo.add( Calendar.DAY_OF_MONTH, 1 );
		
		updateDateFields();
		
		Button btn = ( Button )findViewById( R.id.bnt_submit );
		btn.setOnClickListener( new View.OnClickListener()
		{
			
			@Override
			public void onClick( View v )
			{
				submitData();
			}
		} );
		
		mProgressView = findViewById( android.R.id.progress );
		mMainView = findViewById( R.id.container );

		TextView tv = ( TextView )mProgressView.findViewById( R.id.status_message );
		tv.setText( R.string.msg_sending_request );

		mEditMessage = ( EditText )findViewById( R.id.edit_message );
	}
	
	private Bundle getDateBundle( Calendar calendar )
	{
		Bundle args = new Bundle();
		args.putInt( EXTRA_DAY_KEY, calendar.get( Calendar.DAY_OF_MONTH ) );
		args.putInt( EXTRA_MONTH_KEY, calendar.get( Calendar.MONTH ) );
		args.putInt( EXTRA_YEAR_KEY, calendar.get( Calendar.YEAR ) );
		return args;
	}

	private void setMainRecipients( TextView CEO, TextView HR )
	{
		String where = VacationContentProvider.COLUMN_TYPE + "=" + VacationContentProvider.TYPE_CEO + " OR " +
				VacationContentProvider.COLUMN_TYPE + "=" + VacationContentProvider.TYPE_HR;
		
		Cursor c = getContentResolver().query( VacationContentProvider.RECIPIENTS_URI, sRecipientProjection, where, null, null );
		
		int type = VacationContentProvider.TYPE_NONE;
		String name = null;
		String email = null;
		
		if( c.moveToFirst() )
		{
			do
			{
				type = c.getInt( c.getColumnIndex( VacationContentProvider.COLUMN_TYPE ) );
				name = c.getString( c.getColumnIndex( VacationContentProvider.COLUMN_NAME ) );
				email = c.getString( c.getColumnIndex( VacationContentProvider.COLUMN_EMAIL ) );
				
				if( type == VacationContentProvider.TYPE_CEO )
				{
					CEO.setText( name + "," );
					mCeoEmail = email;
				}
				else
				{
					HR.setText( name + "," );
					mHrEmail = email;
				}
			}while( c.moveToNext() );
		}

		c.close();
	}

	private String getEmail()
	{
		String retval = null;

		String[] projection = { VacationContentProvider.COLUMN_EMAIL };
		Cursor c = getContentResolver().query( VacationContentProvider.SUMMARY_URI, projection, null, null, null );
		
		if( c.moveToFirst() )
		{
			retval = c.getString( c.getColumnIndex( VacationContentProvider.COLUMN_EMAIL ) );
		}
		
		c.close();

		return retval;
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.activity_request, menu );
		return true;
	}

	private void updateDateFields()
	{
		mDateFromText.setText( mDateFormat.format( mCalendarFrom.getTime() ) );
		mDateFromText.invalidate();
		mDateToText.setText( mDateFormat.format( mCalendarTo.getTime() ) );
		mDateToText.invalidate();
		
		mDateToText.setError( null );
		
		if( !validate() )
		{
			mErrorView.setVisibility( View.VISIBLE );
		}
		else
		{
			mErrorView.setVisibility( View.GONE );
		}
	}

	private boolean validate()
	{
		return !mCalendarTo.before( mCalendarFrom );
	}
	
	private void submitData()
	{
		if( validate() && mSubmitTask == null )
		{

			showProgress( true );
			mSubmitTask = new SubmitTask();
			mSubmitTask.execute( mDateFormat.format( mCalendarFrom.getTime() ),
					mDateFormat.format( mCalendarTo.getTime() ),
					Integer.toString( mPeriodType ),
					mEditMessage.getText().toString(),
					mCeoEmail,
					mHrEmail,
					mThirdEmail );
		}
	}
	
	private void showProgress( boolean show )
	{
		mProgressView.setVisibility( show ? View.VISIBLE : View.GONE );
		mMainView.setVisibility( show ? View.GONE : View.VISIBLE );
	}

	private class SubmitTask extends AsyncTask< String, Void, Boolean >
	{

		private static final int TYPE_PARAMS_INDEX = 2;
		private static final int FIXED_PARAMS_COUNT = 4;

		@Override
		protected Boolean doInBackground( String... params )
		{
			Boolean retval = null;

			if( params.length > 5 )
			{
				NetApi.Type type = getNetType( params[ TYPE_PARAMS_INDEX ] );
				String[] recipients = new String[ params.length - FIXED_PARAMS_COUNT ];
				
				for( int i = FIXED_PARAMS_COUNT, c = params.length; i < c; ++i )
				{
					recipients[ i - FIXED_PARAMS_COUNT ] = params[ i ];
				}

				JSONObject json = null;

				try
				{
					json = NetApi.getInstance().request( params[ 0 ], params[ 1 ], type, params[ 3 ], recipients );
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				
				if( json != null )
				{
					try
					{
						retval = (
								!TextUtils.isEmpty( json.getString( NetApi.START_DATE_KEY ) ) &&
								!TextUtils.isEmpty( json.getString( NetApi.END_DATE_KEY ) ) &&
								!TextUtils.isEmpty( json.getString( NetApi.TYPE_KEY ) )
								);
					}
					catch( JSONException e )
					{
						e.printStackTrace();
					}
				}
				else
				{
					retval = false;
				}

			}

			return retval;
		}
		
		private NetApi.Type getNetType( String typeString )
		{
			int typeVal = Integer.parseInt( typeString );
			NetApi.Type type = NetApi.Type.Vacation;
			
			switch( typeVal )
			{	
				case VacationContentProvider.TYPE_VACATION:
					type = NetApi.Type.Vacation;
					break;

				case VacationContentProvider.TYPE_DAYOFF:
					type = NetApi.Type.DayOff;
					break;

				case VacationContentProvider.TYPE_ILLNESS:
					type = NetApi.Type.SickLeave;
					break;

				case VacationContentProvider.TYPE_HOMEWORKING:
					type = NetApi.Type.HomeWorking;
					break;

				default:
					break;
			}

			return type;
		}

		@Override
		protected void onPostExecute( Boolean result )
		{
			showProgress( false );
			mSubmitTask = null;

			if( result != null && result )
			{
				finish();
			}
			else
			{
				Toast.makeText( RequestActivity.this, "Failed to request", Toast.LENGTH_LONG ).show();
			}
		}
	}

	@Override
	public void prepare( int id, DialogFragment dialog, Bundle args )
	{
		switch( id )
		{
			case R.id.dlg_peek_date:
			{
				DatePickerDialog dlg = ( DatePickerDialog )dialog.getDialog();
				
				if( dlg != null )
				{
					int year = args.getInt( EXTRA_YEAR_KEY );
					int monthOfYear = args.getInt( EXTRA_MONTH_KEY );
					int dayOfMonth = args.getInt( EXTRA_DAY_KEY );
					dlg.updateDate( year, monthOfYear, dayOfMonth );
				}
			}
				break;
		}
	}

	@Override
	public DialogFragment create( int id, Bundle args )
	{
		DialogFragment dlg = null;
		
		switch( id )
		{
			case R.id.dlg_peek_date:
			{
				dlg = createDateDialog( args );
			}
				break;

		}
		
		return dlg;
	}
	
	private DialogFragment createDateDialog( Bundle args )
	{
		DialogFragment fragment = new AlertDialogFragment( new AlertDialogFragment.Creator()
		{
			
			@Override
			public AlertDialog create( Bundle args )
			{
				int year = args.getInt( EXTRA_YEAR_KEY );
				int monthOfYear = args.getInt( EXTRA_MONTH_KEY );
				int dayOfMonth = args.getInt( EXTRA_DAY_KEY );
				
				AlertDialog dlg = new DatePickerDialog( RequestActivity.this, mDateSetListener, year, monthOfYear, dayOfMonth );
				
				return dlg;
			}
		}, args );
		
		return fragment;
	}

}

