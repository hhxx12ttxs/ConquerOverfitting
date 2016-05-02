package com.grgtvs.dailyxrates;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.grgtvs.dailyxrates.db.Currency;
import com.grgtvs.dailyxrates.db.Rate;
import com.grgtvs.dailyxrates.db.XRatesDataSource;

public class ChartActivity extends SherlockFragmentActivity {
    @SuppressLint("ValidFragment")
    public static class ChartFragment extends SherlockFragment {
        /**
         * Async loader for receiving statistics
         */
        private final class ReceiveStats extends
                AsyncTask<Bundle, Void, JSONObject> {

            @Override
            protected JSONObject doInBackground(Bundle... params) {
                XRatesClient client = new XRatesClient();
                Bundle args = params[0];
                Rate rate = (Rate) args.getSerializable("rate");
                Rate base = (Rate) args.getSerializable("base");
                String type = args.getString("type");

                Bundle requestArgs = new Bundle();
                requestArgs.putString("type", type);
                requestArgs.putString("currency", rate.getCurrency().getCode());
                requestArgs.putString("baseCurrency", base.getCurrency()
                        .getCode());

                JSONObject response = client.getStats(requestArgs);

                return response;
            }

            @Override
            protected void onCancelled() {
                // TODO Auto-generated method stub
                super.onCancelled();
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                try {
                    ChartActivity.ChartFragment.this.updateChart(result);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                super.onPreExecute();
            }
        }

        static ChartFragment newInstance(int num) {
            ChartFragment chart = new ChartFragment();
            Bundle args = new Bundle();
            args.putInt("num", num);
            chart.setArguments(args);
            return chart;
        }

        private int mNum = 1;
        private ProgressDialog mPDialog;
        private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
        private XYMultipleSeriesRenderer mRenderer;
        private XYSeries mCurrentSeries;
        private GraphicalView mChartView;

        private Rate mRate;

        private Rate mBase;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();

            if (args != null) {
                mNum = getArguments().getInt("num");

                mRate = (Rate) args.getSerializable("rate");
                mBase = (Rate) args.getSerializable("base");
                mRenderer = new XYMultipleSeriesRenderer();

                mRenderer.setApplyBackgroundColor(true);
                mRenderer.setBackgroundColor(Color.WHITE);
                mRenderer.setAntialiasing(true);
                mRenderer.setAxisTitleTextSize(20);
                mRenderer.setLabelsTextSize(15);
                mRenderer.setChartTitleTextSize(20);
                mRenderer.setZoomButtonsVisible(true);
                mRenderer.setPointSize(1);
                mRenderer.setGridColor(Color.LTGRAY);
                mRenderer.setShowAxes(true);
                mRenderer.setShowGrid(true);
                mRenderer.setClickEnabled(true);
                mRenderer.setInScroll(true);
                mRenderer.setSelectableBuffer(100);
                mRenderer.setShowLabels(true);
                mRenderer.setShowCustomTextGrid(true);
                mRenderer.setPanEnabled(false);
                mRenderer.setYLabels(10);
                mRenderer.setXLabels(0);

                mCurrentSeries = new XYSeries(mRate.getCurrency().getCode()
                        + " to " + mBase.getCurrency().getCode());

                mDataset.addSeries(mCurrentSeries);
                XYSeriesRenderer mSeriesRenderer = new XYSeriesRenderer();
                mSeriesRenderer.setDisplayChartValues(false);
                mSeriesRenderer.setColor(Color.RED);
                mSeriesRenderer.setPointStyle(PointStyle.CIRCLE);
                mSeriesRenderer.setFillPoints(true);

                mRenderer.addSeriesRenderer(mSeriesRenderer);

                // Receive stats
                new ReceiveStats().execute(args);
            } else {
                mNum = 1;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if (mChartView == null) {
                mChartView = ChartFactory.getLineChartView(
                        container.getContext(), mDataset, mRenderer);
            } else {
                container.removeView(mChartView);
            }

            return mChartView;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private void updateChart(JSONObject data) throws JSONException {
            if (data == null)
                return;
            ArrayList<Date> xLabels = new ArrayList<Date>();
            ArrayList<Double> yLabels = new ArrayList<Double>();

            JSONObject stats = data
                    .getJSONObject(mRate.getCurrency().getCode());
            Iterator<String> keys = stats.keys();
            int cnt = 0;
            int l = (int) Math.ceil(stats.length() / 10);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            do {
                ++cnt;
                String stDate = keys.next();

                Date d;
                try {
                    d = df.parse(stDate);
                    xLabels.add(d);
                    yLabels.add(stats.getDouble(stDate));

                    mCurrentSeries.add(d.getTime(), stats.getDouble(stDate));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } while (keys.hasNext());

            Collections.sort(xLabels);
            Collections.sort(yLabels);

            int dayInMilSec = 24 * 60 * 60 * 1000;

            df = new SimpleDateFormat("yyyy-MM-dd");

            Date xMin = xLabels.get(0);
            Date xMax = xLabels.get(xLabels.size() - 1);

            String alpha = df.format(xMin);
            String omega = df.format(xMax);

            try {
                xMin = df.parse(alpha);
                xMax = df.parse(omega);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            long diff = xMax.getTime() - xMin.getTime();
            int daysDiff = (int) Math.ceil(diff / dayInMilSec);

            df = new SimpleDateFormat("MMM dd");

            Double yMin = yLabels.get(0);
            Double yMax = yLabels.get(yLabels.size() - 1);
            yMin -= yMin * 0.01;
            yMax += yMax * 0.01;
            double[] range = new double[] { xMin.getTime(), xMax.getTime(),
                    yMin, yMax };
            // mRenderer.setPanLimits(range);
            mRenderer.setRange(range);

            df = new SimpleDateFormat("MMM dd");

            // mRenderer.addXTextLabel(xMin.getTime(), df.format(xMin));
            // mRenderer.addXTextLabel(xMax.getTime(), df.format(xMax));
            int maxLabels = 7;
            long step = daysDiff <= maxLabels ? 1 : (long) Math.ceil(daysDiff
                    / maxLabels);
            step = step <= 1 ? 1 : step;

            for (long i = 0; i <= daysDiff; i += step) {
                long a = (dayInMilSec * i);
                Date tmp = new Date(xMin.getTime() + a);
                mRenderer.addXTextLabel(tmp.getTime(), df.format(tmp));
            }

            mRenderer.addYTextLabel(Math.floor(yMin),
                    String.valueOf(Math.ceil(yMax)));
            mRenderer.addYTextLabel(Math.ceil(yMax),
                    String.valueOf(Math.ceil(yMax)));

            if (mChartView != null)
                mChartView.repaint();
        }
    }

    public static class TabManager implements TabHost.OnTabChangeListener {
        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumHeight(0);
                v.setMinimumWidth(0);
                return v;
            }
        }

        static final class TabInfo {
            private final String mTag;
            private final Class<?> mClass;
            private final Bundle mArgs;
            private Fragment mFragment;

            TabInfo(String tag, Class<?> clss, Bundle args) {
                mTag = tag;
                mClass = clss;
                mArgs = args;
            }
        }

        private SherlockFragmentActivity mActivity;
        private TabHost mTabHost;
        private int mContainerId;

        private HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();

        private TabInfo mLastTab = null;

        public TabManager(FragmentActivity activity, TabHost tabHost,
                int containerId) {
            mActivity = (SherlockFragmentActivity) activity;
            mTabHost = tabHost;
            mContainerId = containerId;
        }

        public void setOnTabChangedListener() {
            mTabHost.setOnTabChangedListener(this);
        }

        public void setOnTabChangedListener(TabHost.OnTabChangeListener l) {
            mTabHost.setOnTabChangedListener(this);
        }

        public TabManager addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();
            TabInfo tabInfo = new TabInfo(tag, clss, args);

            tabInfo.mFragment = mActivity.getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (tabInfo.mFragment != null && !tabInfo.mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager()
                        .beginTransaction();
                ft.detach(tabInfo.mFragment);
                ft.commit();
            }

            mTabs.put(tag, tabInfo);
            mTabHost.addTab(tabSpec);
            return this;
        }

        @Override
        public void onTabChanged(String tabId) {
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager()
                        .beginTransaction();
                if (mLastTab != null && mLastTab.mFragment != null) {
                    ft.detach(mLastTab.mFragment);
                }
                if (newTab != null) {
//                    if (newTab.mFragment != null) {
//                        ft.attach(newTab.mFragment);
//                    } else {
                        newTab.mFragment = Fragment.instantiate(mActivity,
                                newTab.mClass.getName(), newTab.mArgs);
                        ft.add(mContainerId, newTab.mFragment, newTab.mTag);
//                    }
                }

                mLastTab = newTab;
                ft.commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }

            if (mLastTab.mTag != null) {
                ((ChartActivity) mActivity).mCurrentTabTag = mLastTab.mTag;
            }
            Log.d("CHARTs", "Selected TabSpec: " + ((ChartActivity) mActivity).mCurrentTabTag);
        }
    }

    private SimpleAdapter mAdapter = null;

    public String mCurrentTabTag = "30D";

    private boolean mFirstCreate = true;

    private SimpleAdapter.ViewBinder mAdapterViewBinder = new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object data,
                String textRepresentation) {

            switch (view.getId()) {
                case android.R.id.text1:
                    TextView txt = (TextView) view;
                    String[] item = (String[]) data;
                    Currency a = new Currency(item[0]);
                    Currency b = new Currency(item[1]);
                    txt.setText(item[0] + "/" + item[1]);
                    txt.setCompoundDrawablesWithIntrinsicBounds(
                            a.getFlagDrawable(), null, b.getFlagDrawable(),
                            null);
                    txt.setCompoundDrawablePadding(App.getContext()
                            .getResources()
                            .getDimensionPixelSize(R.dimen.padding_small));
                    return true;
                default:
                    break;
            }

            return false;
        }
    };

    private ActionBar.OnNavigationListener callback = new ActionBar.OnNavigationListener() {
        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            @SuppressWarnings("unchecked")
            HashMap<String, String[]> selected = (HashMap<String, String[]>) mAdapter
                    .getItem(itemPosition);
            String[] c = selected.get("item");
            Rate r = new Rate();
            r.setCurrency(c[0]);
            Rate bs = new Rate();
            bs.setCurrency(c[1]);
            setUpTabs(r, bs);

            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle extras = getIntent().getBundleExtra("bundle");

        Rate rate = (Rate) extras.getSerializable("rate");
        Rate base = (Rate) extras.getSerializable("base");

        XRatesDataSource db = new XRatesDataSource();
        Currency[] c = db.getCurrencies(true);

        ArrayList<HashMap<String, String[]>> lst = new ArrayList<HashMap<String, String[]>>();

        HashMap<String, String[]> item = new HashMap<String, String[]>();
        item.put("item", new String[] { rate.getCurrency().getCode(),
                base.getCurrency().getCode() });
        lst.add(item);

        for (int i = 0; i < c.length; i++) {
            if (rate.getCurrency().getCode().equals(c[i].getCode())
                    || base.getCurrency().getCode().equals(c[i].getCode()))
                continue;
            item = new HashMap<String, String[]>();
            item.put("item", new String[] { c[i].getCode(),
                    base.getCurrency().getCode() });
            lst.add(item);
        }

        mAdapter = new SimpleAdapter(this, lst,
                android.R.layout.simple_list_item_1, new String[] { "item" },
                new int[] { android.R.id.text1 });
        mAdapter.setViewBinder(mAdapterViewBinder);

        getSupportActionBar().setListNavigationCallbacks(mAdapter, callback);
    }

    @Override
    public boolean onOptionsItemSelected(
            com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpTabs(Rate rate, Rate base) {
        TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);

        if (tabs.getTabWidget() != null && tabs.getTabWidget().getTabCount() > 0 ) {
            tabs.clearAllTabs();
        }

        tabs.setup();

        TabManager mTabManger = new TabManager(this, tabs,
                android.R.id.tabcontent);
        Bundle args = new Bundle();

        args.putSerializable("rate", rate);
        args.putSerializable("base", base);

        args.putString("type", "daily");
        mTabManger.addTab(tabs.newTabSpec("24H").setIndicator("24H"),
                ChartActivity.ChartFragment.class, args);
        args = new Bundle(args);
        args.putString("type", "weekly");
        mTabManger.addTab(tabs.newTabSpec("7D").setIndicator("7D"),
                ChartActivity.ChartFragment.class, args);
        args = new Bundle(args);
        args.putString("type", "monthly");
        mTabManger.addTab(tabs.newTabSpec("30D").setIndicator("30D"),
                ChartActivity.ChartFragment.class, args);
        args = new Bundle(args);
        args.putString("type", "last3months");
        mTabManger.addTab(tabs.newTabSpec("3M").setIndicator("3M"),
                ChartActivity.ChartFragment.class, args);
        args = new Bundle(args);
        args.putString("type", "yearly");
        mTabManger.addTab(tabs.newTabSpec("1Y").setIndicator("1Y"),
                ChartActivity.ChartFragment.class, args);

        Log.d("CHARTs", "Set selected Tab: " + mCurrentTabTag);

        tabs.setOnTabChangedListener(mTabManger);
        tabs.setCurrentTabByTag(mCurrentTabTag);

        tabs.forceLayout();
    }
}

