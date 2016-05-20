package com.example.wallt.views;

import java.util.Calendar;

import com.example.wallt.R;
import com.example.wallt.R.id;
import com.example.wallt.R.layout;
import com.example.wallt.R.menu;
import com.example.wallt.R.string;
import com.example.wallt.presenters.GestureListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.content.Context;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


@SuppressLint("ValidFragment")
public class ReportsActivity extends Fragment {

    private ListView listView;
    private ListAdapter listAdapter;
    private View fragmentView;
    private Activity parentActivity;

    private int dateRow;
    private int reportRow;
    private int rowCount;

    private TextView fromMonthView;
    private TextView fromDayView;
    private TextView fromYearView;
    private TextView toMonthView;
    private TextView toDayView;
    private TextView toYearView;
    private Spinner spinner;

    private int fromYear;
    private int fromMonth;
    private int fromDay;
    private int toYear;
    private int toMonth;
    private int toDay;

    private int reportSelected;

    private Calendar calendar;

    public static int income = 0;
    public static int spending = 1;
    public static int accounts = 2;
    public static int cashflow = 3;
    public static int transactions = 4;

    private String[] months = {"January",
    		"February", "March", "April",
    		"May", "June", "July", "August",
    		"September", "October", "November",
    		"December"};


    @Override
	public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        rowCount = 0;
        dateRow = rowCount++;
        reportRow = rowCount++;
    }

    @Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
    	fragmentView = inflater.inflate(R.layout.
    			activity_reports, container, false);
    	parentActivity = getActivity();
    	calendar = Calendar.getInstance();
    	toYear = calendar.get(Calendar.YEAR);
    	toMonth = calendar.get(Calendar.MONTH);
    	toDay = calendar.get(Calendar.DATE);
    	fromYear = calendar.get(Calendar.YEAR) - 1;
    	fromMonth = calendar.get(Calendar.MONTH);
    	fromDay = calendar.get(Calendar.DATE);
    	listAdapter = new ListAdapter(parentActivity);
    	listView = (ListView) fragmentView.findViewById(R.id.listView);
    	Button generateReport = (Button) fragmentView.
    			findViewById(R.id.button1);
    	listView.setAdapter(listAdapter);

        listView.setOnTouchListener(new GestureListener() {
            public void onSwipeRight() {
                ((MainActivity) parentActivity).finishFragment();
            }
        });
        generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

            	Bundle i = new Bundle();
            	i.putInt("FROMMONTH", fromMonth);
			    i.putInt("FROMDAY", fromDay);
			    i.putInt("FROMYEAR", fromYear);
			    i.putInt("TOMONTH", toMonth);
			    i.putInt("TODAY", toDay);
			    i.putInt("TOYEAR", toYear);
			    i.putInt("TYPE", reportSelected);
			    GeneratedActivity pie = new GeneratedActivity();
			    pie.putBundle(i);
			    Intent pieIntent = pie.getIntent(getActivity());
			    startActivity(pieIntent);
            }
        });

        return fragmentView;
    }

	@Override
	public final void onCreateOptionsMenu(final Menu menu,
			final MenuInflater inflater) {
	    super.onCreateOptionsMenu(menu, inflater);
	    parentActivity.getActionBar().setDisplayHomeAsUpEnabled(true);
	    parentActivity.setTitle(getString(R.string.Reports));
	    inflater.inflate(R.menu.reports, menu);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (getFragmentManager().getBackStackEntryCount() > 1) {
			   ((MainActivity) parentActivity).finishFragment();
	        }
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

    private class ListAdapter extends BaseAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(final int i) {
            return i == dateRow || i == reportRow;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(final int i) {
            return null;
        }

        @Override
        public long getItemId(final int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(final int i, View view,
        		final ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
            	LayoutInflater li = (LayoutInflater) mContext.
            		getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	view = li.inflate(R.layout.reports_date_layout,
            			viewGroup, false);
                fromMonthView = (TextView) view.findViewById(R.id.frommonth);
                fromYearView = (TextView) view.findViewById(R.id.fromyear);
                fromDayView = (TextView) view.findViewById(R.id.fromday);
                toMonthView = (TextView) view.findViewById(R.id.tomonth);
                toYearView = (TextView) view.findViewById(R.id.toyear);
                toDayView = (TextView) view.findViewById(R.id.today);
                if (i == dateRow) {
                    fromMonthView.setText(months[fromMonth]);
                    toMonthView.setText(months[toMonth]);
                    fromDayView.setText(Integer.toString(fromDay));
                    toDayView.setText(Integer.toString(toDay));
                    fromYearView.setText(Integer.toString(fromYear));
                    toYearView.setText(Integer.toString(toYear));
                }
                final LinearLayout fromHolder = (LinearLayout) view.
                		findViewById(R.id.fromLayout);
                final LinearLayout toHolder = (LinearLayout) view.
                		findViewById(R.id.toLayout);
                fromHolder.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						DialogFragment newFragment =
						new FromDatePickerFragment();
	        		    newFragment.show(getFragmentManager(),
	        		    		"datepicker");
					}
                });
                toHolder.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						DialogFragment newFragment =
						new ToDatePickerFragment();
	        		    newFragment.show(getFragmentManager(),
	        		    		"datepicker");
					}
                });
            } else if (type == 1) {
            	LayoutInflater li = (LayoutInflater) mContext.
            		getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	view = li.inflate(R.layout.reports_report_layout,
            			viewGroup, false);
            	spinner = (Spinner) view.findViewById(R.id.spinner1);
            	String[] items = {getString(R.string.income_report),
            			getString(R.string.spending_report),
            			getString(R.string.accountlisting_report),
            			getString(R.string.cashflow_report)};
                if (i == reportRow) {
                	ArrayAdapter<String> adapter =
                			new ArrayAdapter<String>(parentActivity,
                			R.layout.spinner_item, items);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(
                    		new OnItemSelectedListener() {
            			@Override
            			public void onItemSelected(
            					final AdapterView<?> parent,
            					final View view, 
            					final int position,
            					final long id) {
            				reportSelected = position;
            			}

            			@Override
            			public void onNothingSelected(
            					final AdapterView<?> parent) {
            				// TODO Auto-generated method stub
            			}
                	});
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(final int i) {
            if (i == dateRow) {
                return 0;
            } else if (i == reportRow) {
            	return 1;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

	@SuppressLint("ValidFragment")
	private class FromDatePickerFragment extends DialogFragment
		implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			int year = fromYear;
			int month = fromMonth;
			int day = fromDay;
			return new DatePickerDialog(getActivity(),
					this, year, month, day);
		}

		@Override
		public void onDateSet(final DatePicker view, final int year,
					final int month, final int day) {
			fromYear = year;
			fromMonth = month;
			fromDay = day;
			fromMonthView.setText(months[fromMonth ]);
            fromDayView.setText(Integer.toString(fromDay));
            fromYearView.setText(Integer.toString(fromYear));
		}
	}

	private class ToDatePickerFragment extends DialogFragment
		implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			int year = toYear;
			int month = toMonth;
			int day = toDay;
			return new DatePickerDialog(getActivity(),
					this, year, month, day);
		}

		@Override
		public void onDateSet(final DatePicker view,
			   final int year, final int month, final int day) {
			toYear = year;
			toMonth = month;
			toDay = day;
            toMonthView.setText(months[toMonth]);
            toDayView.setText(Integer.toString(toDay));
            toYearView.setText(Integer.toString(toYear));
		}
	}
}

