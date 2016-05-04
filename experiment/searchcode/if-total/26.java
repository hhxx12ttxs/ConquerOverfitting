package com.nimbits.android.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.widget.*;
import com.nimbits.android.CustomDialog;
import com.nimbits.android.DisplayType;
import com.nimbits.android.ImageCursorAdapter;
import com.nimbits.android.R;
import com.nimbits.android.account.OwnerAccountFactory;
import com.nimbits.android.dao.LocalDatabaseDaoFactory;
import com.nimbits.android.database.DatabaseHelperFactory;
import com.nimbits.android.json.GsonFactory;
import com.nimbits.client.model.Const;
import com.nimbits.client.model.category.Category;
import com.nimbits.client.model.category.CategoryName;
import com.nimbits.client.model.common.CommonFactoryLocator;
import com.nimbits.client.model.diagram.Diagram;
import com.nimbits.client.model.point.Point;
import com.nimbits.client.model.point.PointModel;
import com.nimbits.client.model.point.PointName;
import com.nimbits.client.model.value.Value;
import org.apache.http.cookie.Cookie;

import java.io.UnsupportedEncodingException;
import java.util.List;


public class StartActivity extends ListActivity implements OnGestureListener {

    private static final int LOAD_DIALOG = 0;
    private static final int POINT_DIALOG = 1;
    private static final int CHANGE_SERVER_DIALOG = 2;
    private static final int CHOOSE_SERVER_DIALOG = 3;
    private static final int CHECK_SERVER_DIALOG = 4;
    private static final int NO_DATA_DIALOG = 5;

    private PopulateDatabaseThread populateDatabaseThread;
    private LoadPointDataThread loadPointDataThread;
    private AuthenticateThread authenticateThread;

    private ProgressDialog populateDatabaseDialog;
    private ProgressDialog pointDialog;
    private ProgressDialog authenticateDialog;

    private final Handler timerHandler = new Handler();
    private GestureDetector gestureScanner;
    private String baseURL;
    private Cookie authCookie;

    private Cursor listCursor;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static String selectedServer = "";
    private String currentCategory = null;

    private String buildPointDescription(final Point p, final Value v) {
        final StringBuilder b = new StringBuilder();
        if (v != null) {
            b.append(v.getValue());

            if (p.getUnit() != null) {
                b.append(p.getUnit());
            }
            if (v.getNote() != null) {
                b.append(" ").append(v.getNote());
            }
            if (v.getValue() > p.getHighAlarm() && p.isHighAlarmOn()) {
                b.append(" " + Const.MESSAGE_HIGH_ALERT_ON);

            }
            if (v.getValue() < p.getLowAlarm() && p.isLowAlarmOn()) {
                b.append(" " + Const.MESSAGE_LOW_ALERT_ON);

            }
        } else {
            b.append(Const.MESSAGE_NO_DATA);
        }
        return b.toString();

    }

    private void updatePointValues() {
        Cursor c;
        String cat;
        if (currentCategory == null) {
            cat = Const.CONST_HIDDEN_CATEGORY;
        } else {
            cat = currentCategory;
        }
        SQLiteDatabase db1;
        db1 = DatabaseHelperFactory.getInstance(StartActivity.this).getDB(true);
        c = db1.query(Const.ANDROID_TABLE_LEVEL_TWO_DISPLAY, new String[]{"_id", Const.ANDROID_COL_CATEGORY, Const.ANDROID_COL_DESCRIPTION, Const.ANDROID_COL_DISPLAY_TYPE, Const.ANDROID_COL_JSON}, Const.ANDROID_COL_CATEGORY + "='" + cat + "'", null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            PointName pointName = (PointName) CommonFactoryLocator.getInstance().createPointName(c.getString(c.getColumnIndex(Const.ANDROID_COL_CATEGORY)));
            String json = c.getString(c.getColumnIndex(Const.ANDROID_COL_JSON));
            Point p = GsonFactory.getInstance().fromJson(json, Point.class);

            Value v = OwnerAccountFactory.getInstance().getNimbitsClient(StartActivity.this, baseURL).getCurrentRecordedValue(pointName);
            if (v != null) {
                ContentValues u = new ContentValues();
                u.put(Const.ANDROID_COL_DESCRIPTION, buildPointDescription(p, v));
                if (v.getValue() > p.getHighAlarm() && p.isHighAlarmOn()) {
                    u.put(Const.ANDROID_COL_DISPLAY_TYPE, 3);
                } else if (v.getValue() < p.getLowAlarm() && p.isLowAlarmOn()) {
                    u.put(Const.ANDROID_COL_DISPLAY_TYPE, 4);
                } else {
                    u.put(Const.ANDROID_COL_DISPLAY_TYPE, 2);
                }
                db1.update(Const.ANDROID_TABLE_LEVEL_TWO_DISPLAY, u, Const.ANDROID_COL_ID + "=?", new String[]{Long.toString(c.getLong(c.getColumnIndex("_id")))});
                db1.update(Const.ANDROID_TABLE_LEVEL_ONE_DISPLAY, u, Const.ANDROID_COL_NAME + "=?", new String[]{pointName.getValue()});
            }
            c.moveToNext();
        }
        c.close();
        db1.close();
        if (currentCategory == null) {
            loadView();
        } else {
            loadLevelTwoView(currentCategory);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        gestureScanner = new GestureDetector(this);

        super.onCreate(savedInstanceState);
        if (DatabaseHelperFactory.getInstance(StartActivity.this).checkDatabase()) {
            setContentView(R.layout.catagorylayout);
            showDialog(CHECK_SERVER_DIALOG);
        }

    }

    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case LOAD_DIALOG:
                return dialogLoadingMain();
            case POINT_DIALOG:
                return dialogLoadingPoints();
            case CHANGE_SERVER_DIALOG:
                return dialogAddServer();
            case CHOOSE_SERVER_DIALOG:
                return dialogChooseServer();
            case CHECK_SERVER_DIALOG:
                return dialogAuthenticatedResponse();
            case NO_DATA_DIALOG:
                return dialogNoPoints();
            default:
                return null;
        }


    }

    private Dialog dialogNoPoints() {
        final Dialog dialog = new Dialog(StartActivity.this);

        dialog.setContentView(R.layout.welcome_layout);
        dialog.setTitle("Welcome To Nimbits");

        final TextView text1 = (TextView) dialog.findViewById(R.id.text);
        text1.setText("Nimbits is a free, social and open source data logging service built on cloud computing technology." +
                " By creating \"Data Points\" you can feed any values that change over time, such as a changing temperature or stock price, " +
                "into that point for storage in a global infrastructure. That point can then be visualized, charted, " +
                "and shared using many open source software interfaces.   You can configure points by logging into the Nimbits Portal: \n www.nimbits.com.\n " +
                "This Android interface to Nimbits allows you to create and view points, record values, and view charts. " +
                "To get started, click next to create your first data point!");

        final Button d1 = (Button) dialog.findViewById(R.id.Button01);
        d1.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                dismissDialog(NO_DATA_DIALOG);
                removeDialog(NO_DATA_DIALOG);
                createPoint();


            }

        });

        return dialog;
    }

    private Dialog dialogLoadingPoints() {
        pointDialog = new ProgressDialog(StartActivity.this);
        pointDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pointDialog.setMessage("Loading Current Values...");
        loadPointDataThread = new LoadPointDataThread(pointHandler, currentCategory);
        loadPointDataThread.start();
        return pointDialog;
    }

    private Dialog dialogLoadingMain() {
        populateDatabaseDialog = new ProgressDialog(StartActivity.this);
        populateDatabaseDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        populateDatabaseDialog.setMessage("Loading...");
        populateDatabaseThread = new PopulateDatabaseThread(populateDatabaseHandler, StartActivity.this);
        populateDatabaseThread.start();
        return populateDatabaseDialog;
    }

    private Dialog dialogChooseServer() {


        final List<String> servers = LocalDatabaseDaoFactory.getInstance().getServers(StartActivity.this);

        final CharSequence[] items = servers.toArray(new CharSequence[servers.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Nimbits Server");


        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {


            public void onClick(DialogInterface dialog, int item) {


                selectedServer = (String) items[item];

                //Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
                // selection =  (String) items[item];

            }
        });

        builder.setNeutralButton("New", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dismissDialog(CHOOSE_SERVER_DIALOG);
                removeDialog(CHOOSE_SERVER_DIALOG);
                showDialog(CHANGE_SERVER_DIALOG);

            }

        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (selectedServer != null) {
                    SQLiteDatabase db1;
                    db1 = DatabaseHelperFactory.getInstance(StartActivity.this).getDB(true);
                    Log.v("delete", selectedServer);
                    db1.execSQL("delete from Servers where url='" + selectedServer + "'");
                    db1.close();
                    dismissDialog(CHOOSE_SERVER_DIALOG);
                    removeDialog(CHOOSE_SERVER_DIALOG);
                    showDialog(CHOOSE_SERVER_DIALOG);

                }

            }

        });


        builder.setPositiveButton("Switch", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (selectedServer != null) {
                    baseURL = selectedServer;
                    LocalDatabaseDaoFactory.getInstance().updateSetting(StartActivity.this, Const.PARAM_SERVER, baseURL);
                    dismissDialog(CHOOSE_SERVER_DIALOG);
                    removeDialog(CHOOSE_SERVER_DIALOG);

                    refreshData();

                }

            }
        }
        );


        return builder.create();

    }

    private Dialog dialogAddServer() {
        final Dialog dialog1 = new Dialog(StartActivity.this);

        dialog1.setContentView(R.layout.text_prompt);
        dialog1.setTitle("Change Server");
        TextView text = (TextView) dialog1.findViewById(R.id.text);
        text.setText("You can point your android device to another Nimbits Server URL (i.e yourserver.appspot.com)");

        EditText urlText = (EditText) dialog1.findViewById(R.id.new_value);
        urlText.setText(baseURL);
        Button b = (Button) dialog1.findViewById(R.id.textPromptOKButton);
        Button d = (Button) dialog1.findViewById(R.id.textPromptDefaultButton);
        b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditText urlText = (EditText) dialog1.findViewById(R.id.new_value);
                // String authToken = OwnerAccountImpl.getToken(StartActivity.this);
                String u = urlText.getText().toString();
                LocalDatabaseDaoFactory.getInstance().addServer(StartActivity.this, u);
                dialog1.dismiss();
                removeDialog(CHANGE_SERVER_DIALOG);
                showDialog(CHOOSE_SERVER_DIALOG);

//                try {
//                    if (Client.getN().isLoggedIn())
//                    {
//                        baseURL = u;
//
//                        refreshData();
//
//                    }
//                    else
//                    {
//                        Toast.makeText(StartActivity.this, "Could not connect to" + u, Toast.LENGTH_LONG).show();
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                }
//                //showDialog(CHOOSE_SERVER_DIALOG);


            }
        });

        d.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditText urlText = (EditText) dialog1.findViewById(R.id.new_value);
                urlText.setText(Const.PATH_NIMBITS_PUBLIC_SERVER);

            }
        });

        dialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {

            public void onDismiss(DialogInterface dialog) {
            }

        });

        return dialog1;
    }

    private Dialog dialogAuthenticatedResponse() {
        Log.v("NimbitsV", "Authenticating");
        baseURL = LocalDatabaseDaoFactory.getInstance().getSetting(StartActivity.this, Const.PARAM_SERVER);
        Log.v("NimbitsV", "Logging into " + baseURL);
        authenticateDialog = new ProgressDialog(StartActivity.this);
        authenticateDialog = ProgressDialog.show(StartActivity.this, "", "Authenticating to Nimbits Server @ " + baseURL + " using account " + OwnerAccountFactory.getInstance().getEmail(StartActivity.this) + ".  Please wait...", true);

        authenticateThread = new AuthenticateThread(authenticateThreadHandler, StartActivity.this);
        authenticateThread.start();
        return authenticateDialog;
    }

    // Define the Handler that receives messages from the thread and update the progress
    private final Handler populateDatabaseHandler = new Handler() {
        public void handleMessage(Message msg) {
            final int total = msg.getData().getInt(Const.PARAM_TOTAL);
            final int pointCount = msg.getData().getInt(Const.PARAM_POINT_COUNT);

            if (populateDatabaseDialog != null) {
                populateDatabaseDialog.setProgress(total);
                Log.v("handler", "" + total);
                if (total >= 100) {
                    populateDatabaseDialog.setProgress(0);
                    dismissDialog(LOAD_DIALOG);
                    removeDialog(LOAD_DIALOG);
                    populateDatabaseThread.setState(PopulateDatabaseThread.STATE_DONE);
                    populateDatabaseDialog = null;
                    if (pointCount == 0) {
                        showDialog(NO_DATA_DIALOG);
                    } else {
                        loadView();
                    }


                }
            }
        }
    };

    // Define the Handler that receives messages from the thread and update the progress
    private final Handler authenticateThreadHandler = new Handler() {
        public void handleMessage(Message msg) {
            //	int total = msg.getData().getInt("total");
            final boolean isLoggedIn = msg.getData().getBoolean(Const.PARAM_IS_LOGGED_IN);
            Log.v("NimbitsV", "is logged in " + isLoggedIn);
            if (authenticateDialog != null) {
                dismissDialog(CHECK_SERVER_DIALOG);
                removeDialog(CHECK_SERVER_DIALOG);
                authenticateThread.setState(AuthenticateThread.STATE_DONE);
                authenticateDialog = null;

                if (isLoggedIn) {
                    boolean reload = false;
                    final Bundle b = getIntent().getExtras();
                    String category = null;
                    if (b != null) {
                        reload = b.getBoolean(Const.PARAM_RELOAD);
                        category = b.getString(Const.ANDROID_COL_CATEGORY);
                    }
                    if (!reload) {
                        if (category != null) {
                            currentCategory = category;

                            showDialog(POINT_DIALOG);
                        } else {
                            loadView();
                        }
                    } else {
                        showDialog(LOAD_DIALOG);
                    }
                } else {
                    Toast.makeText(StartActivity.this, "Nimbits uses google accounts to authenticate. Please add a google.com (gmail.com) account to this device.", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private final Handler pointHandler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.getData().getInt(Const.PARAM_TOTAL);

            if (pointDialog != null) {
                pointDialog.setProgress(total);

                if (total >= 100) {
                    pointDialog.setProgress(0);
                    dismissDialog(POINT_DIALOG);
                    removeDialog(POINT_DIALOG);
                    loadPointDataThread.setState(LoadPointDataThread.STATE_DONE);
                    pointDialog = null;
                    loadLevelTwoView(currentCategory);


                }
            }
        }
    };

    void loadView() {

        currentCategory = null;


        if (listCursor != null) {
            listCursor.close();
        }


        ListAdapter adapter = LocalDatabaseDaoFactory.getInstance().mainListCursor(StartActivity.this);
        setContentView(R.layout.catagorylayout);
        setListAdapter(adapter);

    }

    private void loadLevelTwoView(final String category) {
        this.setTitle(category);
        currentCategory = category;
        if (listCursor != null) {
            listCursor.close();
        }

        SQLiteDatabase db = DatabaseHelperFactory.getInstance(StartActivity.this).getDB(false);
        listCursor = db.query(Const.ANDROID_TABLE_LEVEL_TWO_DISPLAY, new String[]{
                Const.ANDROID_COL_ID,
                Const.ANDROID_COL_CATEGORY,
                Const.ANDROID_COL_DESCRIPTION,
                Const.ANDROID_COL_DISPLAY_TYPE,
                Const.ANDROID_COL_NAME
        },
                Const.ANDROID_COL_CATEGORY + "='" + category + "'", null, null, null, Const.ANDROID_COL_DISPLAY_TYPE);


        ListAdapter adapter = new ImageCursorAdapter(
                this, // Context.
                R.layout.main_list,  // Specify the row template to use (here, two columns bound to the two retrieved cursor
                listCursor,                                              // Pass in the cursor to bind to.
                new String[]{Const.ANDROID_COL_NAME, Const.ANDROID_COL_DESCRIPTION},           // Array of cursor columns to bind to.

                new int[]{R.id.text1, R.id.text2});  // Parallel array of which template objects to bind to those columns.

        // Bind to our new adapter.
        setListAdapter(adapter);


    }

    private void viewMap() {
        if (this.currentCategory == null) {
            Toast.makeText(StartActivity.this, "Please select a category to or point to view on the map", Toast.LENGTH_LONG).show();

        } else {
            //SQLiteDatabase db1 = getDB(false);
            Cursor c;
            SQLiteDatabase db1;
            db1 = DatabaseHelperFactory.getInstance(StartActivity.this).getDB(false);
            c = db1.query(Const.ANDROID_TABLE_LEVEL_ONE_DISPLAY, new String[]{Const.ANDROID_COL_ID, Const.ANDROID_COL_JSON}, Const.ANDROID_COL_NAME + "='" + currentCategory + "'", null, null, null, null);

            c.moveToFirst();
            String json = c.getString(c.getColumnIndex(Const.ANDROID_COL_JSON));
            c.close();

            Bundle b = new Bundle();
            Intent intent = new Intent();
            b.putString(Const.PARAM_TYPE, Const.ANDROID_COL_CATEGORY);
            b.putString(Const.PARAM_CATEGORY, currentCategory);
            b.putString(Const.PARAM_JSON, json);
            b.putString(Const.PARAM_BASE_URL, baseURL);
            intent.putExtras(b);
            intent.setClass(StartActivity.this, MapViewActivity.class);
            startActivity(intent);
            c.close();
            db1.close();

            finish();
        }
    }

    private void viewChart() {

        String cat;
        Log.v("action", "view chart");
        Log.v("chart", currentCategory);
        if (this.currentCategory == null) {
            cat = Const.CONST_HIDDEN_CATEGORY;
            //Toast.makeText(StartActivity.this,"Please select a catagory to or point to view a chart", Toast.LENGTH_LONG).show();

        } else {
            cat = currentCategory;
        }
        Log.v("chart", cat);

        //SQLiteDatabase db1 = getDB(false);
        Cursor c;
        SQLiteDatabase db1;
        db1 = DatabaseHelperFactory.getInstance(StartActivity.this).getDB(false);
        c = db1.query(Const.ANDROID_TABLE_LEVEL_ONE_DISPLAY, new String[]{Const.ANDROID_COL_ID, Const.ANDROID_COL_JSON}, Const.ANDROID_COL_NAME + "='" + cat + "'", null, null, null, null);

        c.moveToFirst();
        String json = c.getString(c.getColumnIndex(Const.ANDROID_COL_JSON));
        Log.v("chart", json);
        c.close();
        db1.close();

        Bundle b = new Bundle();
        Intent intent = new Intent();
        b.putString(Const.PARAM_TYPE, Const.PARAM_CATEGORY);
        b.putString(Const.PARAM_CATEGORY, cat);
        b.putString(Const.PARAM_JSON, json);
        b.putString(Const.PARAM_BASE_URL, baseURL);

        intent.putExtras(b);
        intent.setClass(StartActivity.this, ChartActivity.class);
        startActivity(intent);

        finish();

    }

    @Override
    public void finish() {
        super.finish();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(mUpdateTimerTask);
        }
    }

    private void createCategory() {

        //setContentView(0);
        try {
            CustomDialog myDialog = new CustomDialog(this, "New Category Name:",
                    new OnNewCategoryListener());
            myDialog.show();
            //   al.add(myDialog.getEntry());

        } catch (Exception e) {

            Log.e("Create category", e.getMessage());
        }


        //dialog.show();

    }

    private void createPoint() {
        try {
            CustomDialog myDialog = new CustomDialog(this, "New Point Name:",
                    new OnCreatePointListener());
            myDialog.show();
        } catch (Exception e) {

            Log.e(Const.TEXT_NEW_CATEGORY, e.getMessage());
        }


    }

    private void refreshData() {

        LocalDatabaseDaoFactory.getInstance().deleteAll(StartActivity.this);
        showDialog(LOAD_DIALOG);
    }

    private void changeServer() {


        showDialog(CHOOSE_SERVER_DIALOG);
    }

    //Event Overrides

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.New_Point_Catagory:
                createCategory();
                return true;
            case R.id.New_Data_Point:
                createPoint();
                return true;
            case R.id.main_menu:
                loadView();
                return true;
            case R.id.refresh:
                refreshData();
                return true;
            case R.id.view_map:
                viewMap();
                return true;
            case R.id.view_chart:
                viewChart();
                return true;
            case R.id.exit:
                this.finish();
                return true;
            case R.id.Servers:
                changeServer();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        super.onListItemClick(l, v, position, id);
        //	Context context = getApplicationContext();

        final ImageView icon = (ImageView) v.findViewById(R.id.icon1);
        final TextView d = (TextView) v.findViewById(R.id.text1);


        if (listCursor != null) {
            listCursor.close();
        }

        if (icon.getTag().toString().equals(Const.PARAM_POINT)) {
            final String json = LocalDatabaseDaoFactory.getInstance().getSelectedChildTableJsonByName(StartActivity.this, (String) d.getText());
            final Bundle b = new Bundle();
            final Intent intent = new Intent();
            b.putString(Const.PARAM_CATEGORY, this.currentCategory);
            b.putString(Const.PARAM_POINT, (String) d.getText());
            b.putString(Const.PARAM_JSON, json);
            b.putString(Const.PARAM_BASE_URL, baseURL);
            intent.putExtras(b);
            intent.setClass(StartActivity.this, PointActivity.class);
            finish();
            startActivity(intent);
        } else if (icon.getTag().toString().equals(Const.PARAM_DIAGRAM)) {
            final String json = LocalDatabaseDaoFactory.getInstance().getSelectedChildTableJsonByName(StartActivity.this, (String) d.getText());
            final Bundle b = new Bundle();
            final Intent intent = new Intent();
            // final String jsonCookie = GsonFactory.getInstance().toJson(authCookie);
            b.putString(Const.PARAM_CATEGORY, this.currentCategory);
            b.putString(Const.PARAM_DIAGRAM, (String) d.getText());
            b.putString(Const.PARAM_JSON, json);
            b.putString(Const.PARAM_BASE_URL, baseURL);

            b.putString(Const.PARAM_COOKIE, authCookie.getName() + "=" + authCookie.getValue() + "; domain=" + authCookie.getDomain());
            intent.putExtras(b);
            intent.setClass(StartActivity.this, DiagramActivity.class);
            finish();
            startActivity(intent);
        } else if (icon.getTag().toString().equals(Const.PARAM_CATEGORY)) {
            currentCategory = (String) d.getText();
            showDialog(POINT_DIALOG);

        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return gestureScanner.onTouchEvent(event);

    }

    @Override
    public boolean onDown(MotionEvent e) {

        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        Log.v("action", "fling");

        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                Log.v("action", "right");
                viewChart();
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {


                Log.v("action", "left");
                if (currentCategory != null) {
                    currentCategory = null;
                    loadView();

                }
            }
        } catch (Exception e) {
            // nothing
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {


    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {


    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        return false;
    }

    private class PopulateDatabaseThread extends Thread {
        final Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        @SuppressWarnings("unused")
        int mState;
        @SuppressWarnings("unused")
        int total;
        final Context context;

        PopulateDatabaseThread(Handler h, Context c) {
            mHandler = h;
            context = c;
        }

        private void update(int c, boolean loggedIn, int count) {
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt(Const.PARAM_TOTAL, c);
            b.putInt(Const.PARAM_POINT_COUNT, count);
            b.putBoolean(Const.PARAM_LOGGED_IN, loggedIn);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }

        public void run() {
            mState = STATE_RUNNING;
            total = 0;
            boolean loggedIn;
            update(7, false, 0);
            loggedIn = true;
            update(10, loggedIn, 0);

            update(20, loggedIn, 0);
            int pointCount = 0;
            int diagramCount = 0;
            if (DatabaseHelperFactory.getInstance(StartActivity.this).isDatabaseEmpty()) {

                pointCount = populateEmptyDatabase(loggedIn, pointCount, diagramCount);
            } else {
                pointCount++;
                diagramCount++;
            }
            //db1.close();
            total = 100;
            update(100, loggedIn, pointCount);
            mState = STATE_DONE;

        }

        private int populateEmptyDatabase(boolean loggedIn, int totalPointCount, int totalDiagramCount) {
            final List<Category> categories = OwnerAccountFactory.getInstance().getNimbitsClient(context, baseURL).getCategories(true, true);
            update(25, loggedIn, totalPointCount);
            Log.v(Const.N, "Populating empty db from: " + baseURL);
            int i = 30;
            if (categories != null) {
                update(i, loggedIn, totalPointCount);
                for (Category category : categories) {

                    update(i++, loggedIn, totalPointCount);
                    ContentValues mainTableValues = new ContentValues();
                    mainTableValues.put(Const.ANDROID_COL_NAME, category.getName().getValue());
                    mainTableValues.put(Const.ANDROID_COL_DISPLAY_TYPE, DisplayType.Category.getCode());
                    mainTableValues.put(Const.ANDROID_COL_JSON, category.getJsonPointCollection());

                    for (final Point p : category.getPoints()) {
                        totalPointCount++;
                        addPointToChildrenTable(category, p);

                    }

                    for (final Diagram d : category.getDiagrams()) {
                        totalDiagramCount++;
                        addDiagramToChildrenTable(category, d);

                    }

                    mainTableValues.put(Const.ANDROID_COL_DESCRIPTION, category.getPoints().size() + " points " +
                            category.getDiagrams().size() + " diagrams");
                    LocalDatabaseDaoFactory.getInstance().insertMain(StartActivity.this, mainTableValues);


                    if (category.getName() != null && category.getName().getValue().equals(Const.CONST_HIDDEN_CATEGORY)) {

                        for (Point p : category.getPoints()) {
                            totalPointCount++;
                            addTopLevelPointToDatabase(p);
                        }

                        for (Diagram d : category.getDiagrams()) {
                            totalDiagramCount++;
                            addTopLevelDiagramToDatabase(d);
                        }


                    }


                }

            }
            return totalPointCount;
        }

        private void addTopLevelPointToDatabase(final Point p) {
            final ContentValues pointTableValues = new ContentValues();
            final ContentValues mainTableValues = new ContentValues();
            final Value v = OwnerAccountFactory.getInstance().getNimbitsClient(StartActivity.this, baseURL).getCurrentRecordedValue(p.getName());
            mainTableValues.put(Const.ANDROID_COL_NAME, p.getName().getValue());
            int displayType;
            if (v != null) {
                if (v.getValue() > p.getHighAlarm() && p.isHighAlarmOn()) {
                    displayType = DisplayType.HighAlarm.getCode();
                } else if (v.getValue() < p.getLowAlarm() && p.isLowAlarmOn()) {
                    displayType = DisplayType.LowAlarm.getCode();
                } else {
                    displayType = DisplayType.Point.getCode();
                }
            } else {
                displayType = DisplayType.Point.getCode();
            }

            mainTableValues.put(Const.ANDROID_COL_DISPLAY_TYPE, displayType);
            mainTableValues.put(Const.ANDROID_COL_DESCRIPTION, buildPointDescription(p, v));
            LocalDatabaseDaoFactory.getInstance().insertMain(StartActivity.this, mainTableValues);


            pointTableValues.put(Const.ANDROID_COL_CATEGORY, p.getName().getValue());
            pointTableValues.put(Const.ANDROID_COL_DESCRIPTION, p.getDescription());
            pointTableValues.put(Const.ANDROID_COL_DISPLAY_TYPE, displayType);
            pointTableValues.put(Const.ANDROID_COL_JSON, GsonFactory.getInstance().toJson(p));
            LocalDatabaseDaoFactory.getInstance().insertPoints(StartActivity.this, pointTableValues);

        }

        private void addTopLevelDiagramToDatabase(final Diagram diagram) {
            final ContentValues pointTableValues = new ContentValues();
            final ContentValues mainTableValues = new ContentValues();
            mainTableValues.put(Const.ANDROID_COL_NAME, diagram.getName().getValue());
            mainTableValues.put(Const.ANDROID_COL_DISPLAY_TYPE, DisplayType.Diagram.getCode());
            mainTableValues.put(Const.ANDROID_COL_DESCRIPTION, "");//TODO diagram description
            LocalDatabaseDaoFactory.getInstance().insertMain(StartActivity.this, mainTableValues);


            pointTableValues.put(Const.ANDROID_COL_CATEGORY, diagram.getName().getValue());
            pointTableValues.put(Const.ANDROID_COL_DESCRIPTION, "");//TODO
            pointTableValues.put(Const.ANDROID_COL_DISPLAY_TYPE, DisplayType.Diagram.getCode());
            pointTableValues.put(Const.ANDROID_COL_JSON, GsonFactory.getInstance().toJson(diagram));
            LocalDatabaseDaoFactory.getInstance().insertPoints(StartActivity.this, pointTableValues);

        }

        private void addPointToChildrenTable(final Category category, final Point p) {
            final ContentValues tableValues = new ContentValues();
            tableValues.put(Const.ANDROID_COL_CATEGORY, category.getName().getValue());
            tableValues.put(Const.ANDROID_COL_NAME, p.getName().getValue());
            tableValues.put(Const.ANDROID_COL_DESCRIPTION, "");
            tableValues.put(Const.ANDROID_COL_DISPLAY_TYPE, DisplayType.Point.getCode());
            tableValues.put(Const.ANDROID_COL_JSON, GsonFactory.getInstance().toJson(p));
            LocalDatabaseDaoFactory.getInstance().insertPoints(StartActivity.this, tableValues);
        }

        private void addDiagramToChildrenTable(final Category category, final Diagram d) {
            final ContentValues tableValues = new ContentValues();
            tableValues.put(Const.ANDROID_COL_CATEGORY, category.getName().getValue());
            tableValues.put(Const.ANDROID_COL_NAME, d.getName().getValue());
            tableValues.put(Const.ANDROID_COL_DESCRIPTION, "");
            tableValues.put(Const.ANDROID_COL_DISPLAY_TYPE, DisplayType.Diagram.getCode());
            tableValues.put(Const.ANDROID_COL_JSON, GsonFactory.getInstance().toJson(d));
            LocalDatabaseDaoFactory.getInstance().insertPoints(StartActivity.this, tableValues);
        }

        /* sets the current state for the thread,
  * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }
    }

    private class LoadPointDataThread extends Thread {
        final Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        @SuppressWarnings("unused")
        int mState;
        @SuppressWarnings("unused")
        int total;
        final String selectedCategory;

        LoadPointDataThread(final Handler h, final String categoryName) {

            selectedCategory = categoryName;
            mHandler = h;
        }

        /* sets the current state for the thread,
  * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }

        private void update(int c) {
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt(Const.PARAM_TOTAL, c);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }

        public void run() {
            mState = STATE_RUNNING;

            Value v;
            PointName pointName;
            String json;


            //SQLiteDatabase db1 = getDB(true);

            final SQLiteDatabase db = DatabaseHelperFactory.getInstance(StartActivity.this).getDB(true);
            final Cursor c = db.query(Const.ANDROID_TABLE_LEVEL_TWO_DISPLAY, new String[]{
                    Const.ANDROID_COL_ID,
                    Const.ANDROID_COL_NAME,
                    Const.ANDROID_COL_DESCRIPTION,
                    Const.ANDROID_COL_DISPLAY_TYPE,
                    Const.ANDROID_COL_JSON},
                    Const.ANDROID_COL_CATEGORY + "='" + selectedCategory + "'",
                    null, null, null,
                    Const.ANDROID_COL_DISPLAY_TYPE);


            int count = c.getCount();

            update(0);
            if (count > 0) {
                int d = 100 / count;
                int progress = 0;

                //update(count);

                c.moveToFirst();
                while (!c.isAfterLast()) {

                    pointName = (PointName) CommonFactoryLocator.getInstance().createPointName(c.getString(c.getColumnIndex(Const.ANDROID_COL_NAME)));
                    json = c.getString(c.getColumnIndex(Const.ANDROID_COL_JSON));
                    Point p = GsonFactory.getInstance().fromJson(json, PointModel.class);
                    v = OwnerAccountFactory.getInstance().getNimbitsClient(StartActivity.this, baseURL).getCurrentRecordedValue(pointName);
                    ContentValues u = new ContentValues();

                    if (v != null) {

                        u.put(Const.ANDROID_COL_DESCRIPTION, buildPointDescription(p, v));
                        if (v.getValue() > p.getHighAlarm() && p.isHighAlarmOn()) {
                            u.put(Const.ANDROID_COL_DISPLAY_TYPE, 3);
                        } else if (v.getValue() < p.getLowAlarm() && p.isLowAlarmOn()) {
                            u.put(Const.ANDROID_COL_DISPLAY_TYPE, 4);
                        } else {
                            u.put(Const.ANDROID_COL_DISPLAY_TYPE, 2);
                        }

                    } else {
                        u.put(Const.ANDROID_COL_DESCRIPTION, Const.MESSAGE_NO_DATA);
                    }
//                    DatabaseHelperFactory.getInstance(StartActivity.this).updatePointName(u, pointName);
                    LocalDatabaseDaoFactory.getInstance().updatePointValuesByName(StartActivity.this, u, pointName);
                    progress += d;
                    update(progress);

                    c.moveToNext();
                }

                c.close();
                db.close();
                update(100);
            } else {
                update(100);
            }
            update(100);


        }

    }

    private class AuthenticateThread extends Thread {
        final Handler m;
        final static int STATE_DONE = 0;
        int mState;
        final Context currentContext;

        AuthenticateThread(Handler h, Context c) {
            m = h;
            currentContext = c;
        }

        /* sets the current state for the thread,
  * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }

        private void update(boolean isLoggedIn) {
            Message msg = m.obtainMessage();
            Bundle b = new Bundle();
            b.putBoolean(Const.PARAM_IS_LOGGED_IN, isLoggedIn);
            msg.setData(b);
            m.sendMessage(msg);
        }

        public void run() {

            try {
                baseURL = LocalDatabaseDaoFactory.getInstance().getSetting(StartActivity.this, Const.PARAM_SERVER);

                // String authToken = OwnerAccountImpl.getToken(currentContext);
                //googleAuth.connectClean(baseURL,authToken);
                boolean isLoggedIn = OwnerAccountFactory.getInstance().getNimbitsClient(StartActivity.this, baseURL).isLoggedIn();
                authCookie = OwnerAccountFactory.getInstance().getNimbitsClient(StartActivity.this, baseURL).getAuthCookie();
                update(isLoggedIn);
            } catch (Exception e) {
                Log.e(Const.N, e.getMessage());
                update(false);
            }


        }

    }

    private final Runnable mUpdateTimerTask = new Runnable() {

        public void run() {
            Log.v("category timer", "tick");
            //			updatePointValues();
            //			if (mHandler != null)
            //			{
            //				mHandler.removeCallbacks(mUpdateTimerTask);
            //				mHandler.postDelayed(mUpdateTimerTask, refreshRate);
            //			}
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (currentCategory != null) {
                currentCategory = null;
                loadView();
                return true;
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    private class OnNewCategoryListener implements CustomDialog.ReadyListener {
        public void ready(String categoryNameResponse) throws UnsupportedEncodingException {
            if (categoryNameResponse != null && categoryNameResponse.trim().length() > 0) {
                CategoryName categoryName = CommonFactoryLocator.getInstance().createCategoryName(categoryNameResponse);

                OwnerAccountFactory.getInstance().getNimbitsClient(StartActivity.this, baseURL).addCategory(categoryName);
                ContentValues values = new ContentValues();
                values.put(Const.ANDROID_COL_CATEGORY, categoryName.getValue());
                values.put(Const.ANDROID_COL_DISPLAY_TYPE, 1);
                values.put(Const.ANDROID_COL_DESCRIPTION, 0 + " points");
                LocalDatabaseDaoFactory.getInstance().insertMain(StartActivity.this, values);

                //db1.insert(DatabaseHelperImpl.ANDROID_TABLE_LEVEL_ONE_DISPLAY, null,values);
                //db1.close();
                Toast.makeText(StartActivity.this, "Added Category " + categoryName + ". Click on the category to add Data Points to it", Toast.LENGTH_LONG).show();
                loadView();

            }


        }
    }

    private class OnCreatePointListener implements CustomDialog.ReadyListener {
        public void ready(String pointNameText) {


            //SQLiteDatabase db1 = getDB(true);
            if (!pointNameText.trim().equals("")) {
                PointName pointName = CommonFactoryLocator.getInstance().createPointName(pointNameText);
                CategoryName categoryName = CommonFactoryLocator.getInstance().createCategoryName(currentCategory);


                Point point = OwnerAccountFactory.getInstance().getNimbitsClient(StartActivity.this, baseURL).addPoint(categoryName, pointName);
                ContentValues pValues = new ContentValues();
                ContentValues cValues = new ContentValues();
                if (currentCategory != null) {
                    pValues.put(Const.ANDROID_COL_CATEGORY, currentCategory);
                } else {
                    pValues.put(Const.ANDROID_COL_CATEGORY, Const.CONST_HIDDEN_CATEGORY);
                    cValues.put(Const.ANDROID_COL_CATEGORY, pointName.getValue());
                    cValues.put(Const.ANDROID_COL_DISPLAY_TYPE, 2);

                    cValues.put(Const.ANDROID_COL_DESCRIPTION, "");

                    LocalDatabaseDaoFactory.getInstance().insertMain(StartActivity.this, cValues);


                }
                pValues.put(Const.ANDROID_COL_CATEGORY, pointName.getValue());
                pValues.put(Const.ANDROID_COL_DESCRIPTION, "");
                pValues.put(Const.ANDROID_COL_DISPLAY_TYPE, 2);
                pValues.put(Const.ANDROID_COL_JSON, GsonFactory.getInstance().toJson(point));
                LocalDatabaseDaoFactory.getInstance().insertPoints(StartActivity.this, pValues);


                //db1.close();
                if (currentCategory != null) {
                    loadLevelTwoView(currentCategory);
                } else {
                    loadView();

                }


                Toast.makeText(StartActivity.this, "Added Point " + pointName.getValue() + ". Go to " + baseURL + " to configure advanced properties", Toast.LENGTH_LONG).show();


            }


        }
    }


}
