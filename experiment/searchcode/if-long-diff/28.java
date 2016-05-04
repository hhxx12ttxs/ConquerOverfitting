/****************************************************************************************
 * Copyright (c) 2009 Andrew Dubya <andrewdubya@gmail.com>                              *
 * Copyright (c) 2009 Nicolas Raoul <nicolas.raoul@gmail.com>                           *
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Daniel Sv??rd <daniel.svard@gmail.com>                             * 
 * Copyright (c) 2010 Norbert Nagold <norbert.nagold@gmail.com>                         *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.anki;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.SQLException;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ichi2.anim.ActivityTransitionAnimation;
import com.ichi2.anki.receiver.SdCardReceiver;
import com.ichi2.async.Connection;
import com.ichi2.async.Connection.OldAnkiDeckFilter;
import com.ichi2.async.Connection.Payload;
import com.ichi2.async.DeckTask;
import com.ichi2.async.DeckTask.TaskData;
import com.ichi2.charts.ChartBuilder;
import com.ichi2.libanki.Collection;
import com.ichi2.libanki.Decks;
import com.ichi2.libanki.Utils;
import com.ichi2.themes.StyledDialog;
import com.ichi2.themes.StyledOpenCollectionDialog;
import com.ichi2.themes.StyledProgressDialog;
import com.ichi2.themes.Themes;
import com.ichi2.widget.WidgetStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class DeckPicker extends FragmentActivity {

    public static final int CRAM_DECK_FRAGMENT = -1;
    /**
     * Dialogs
     */
    private static final int DIALOG_NO_SDCARD = 0;
    private static final int DIALOG_USER_NOT_LOGGED_IN_SYNC = 1;
    private static final int DIALOG_USER_NOT_LOGGED_IN_ADD_SHARED_DECK = 2;
    private static final int DIALOG_NO_CONNECTION = 3;
    private static final int DIALOG_DELETE_DECK = 4;
    private static final int DIALOG_SELECT_STATISTICS_TYPE = 5;
    private static final int DIALOG_CONTEXT_MENU = 9;
    private static final int DIALOG_REPAIR_COLLECTION = 10;
    private static final int DIALOG_NO_SPACE_LEFT = 11;
    private static final int DIALOG_SYNC_CONFLICT_RESOLUTION = 12;
    private static final int DIALOG_CONNECTION_ERROR = 13;
    private static final int DIALOG_SYNC_LOG = 15;
    private static final int DIALOG_SELECT_HELP = 16;
    private static final int DIALOG_BACKUP_NO_SPACE_LEFT = 17;
    private static final int DIALOG_OK = 18;
    private static final int DIALOG_DB_ERROR = 19;
    private static final int DIALOG_ERROR_HANDLING = 20;
    private static final int DIALOG_LOAD_FAILED = 21;
    private static final int DIALOG_RESTORE_BACKUP = 22;
    private static final int DIALOG_SD_CARD_NOT_MOUNTED = 23;
    private static final int DIALOG_NEW_COLLECTION = 24;
    private static final int DIALOG_FULL_SYNC_FROM_SERVER = 25;
    private static final int DIALOG_SYNC_SANITY_ERROR = 26;
    private static final int DIALOG_SYNC_UPGRADE_REQUIRED = 27;
    private static final int DIALOG_IMPORT = 28;
    private static final int DIALOG_IMPORT_LOG = 29;
    private static final int DIALOG_IMPORT_HINT = 30;
    private static final int DIALOG_IMPORT_SELECT = 31;

    private String mDialogMessage;
    private int[] mRepairValues;
    private boolean mLoadFailed;

    private String mImportPath;
    private String[] mImportValues;

    /**
     * Menus
     */
    private static final int MENU_ABOUT = 0;
    private static final int MENU_CREATE_DECK = 1;
    private static final int MENU_ADD_SHARED_DECK = 2;
    private static final int MENU_PREFERENCES = 3;
    private static final int MENU_MY_ACCOUNT = 4;
    private static final int MENU_FEEDBACK = 5;
    private static final int MENU_HELP = 6;
    private static final int CHECK_DATABASE = 7;
    private static final int MENU_SYNC = 8;
    private static final int MENU_ADD_NOTE = 9;
    public static final int MENU_CREATE_DYNAMIC_DECK = 10;
    private static final int MENU_STATISTICS = 12;
    private static final int MENU_CARDBROWSER = 13;
    private static final int MENU_IMPORT = 14;

    /**
     * Context Menus
     */
    private static final int CONTEXT_MENU_COLLAPSE_DECK = 0;
    private static final int CONTEXT_MENU_RENAME_DECK = 1;
    private static final int CONTEXT_MENU_DELETE_DECK = 2;
    private static final int CONTEXT_MENU_DECK_SUMMARY = 3;
    private static final int CONTEXT_MENU_CUSTOM_DICTIONARY = 4;
    private static final int CONTEXT_MENU_RESET_LANGUAGE = 5;

    public static final String EXTRA_START = "start";
    public static final String EXTRA_DECK_ID = "deckId";
    public static final int EXTRA_START_NOTHING = 0;
    public static final int EXTRA_START_REVIEWER = 1;
    public static final int EXTRA_START_DECKPICKER = 2;
    public static final int EXTRA_DB_ERROR = 3;

    public static final int RESULT_MEDIA_EJECTED = 202;
    public static final int RESULT_DB_ERROR = 203;
    public static final int RESULT_RESTART = 204;

    /**
     * Available options performed by other activities
     */
    private static final int PREFERENCES_UPDATE = 0;
    private static final int DOWNLOAD_SHARED_DECK = 3;
    public static final int REPORT_FEEDBACK = 4;
    private static final int LOG_IN_FOR_DOWNLOAD = 5;
    private static final int LOG_IN_FOR_SYNC = 6;
    private static final int STUDYOPTIONS = 7;
    private static final int SHOW_INFO_WELCOME = 8;
    private static final int SHOW_INFO_NEW_VERSION = 9;
    private static final int REPORT_ERROR = 10;
    public static final int SHOW_STUDYOPTIONS = 11;
    private static final int ADD_NOTE = 12;
    private static final int LOG_IN = 13;
    private static final int BROWSE_CARDS = 14;
    private static final int ADD_SHARED_DECKS = 15;
    private static final int LOG_IN_FOR_SHARED_DECK = 16;
    private static final int ADD_CRAM_DECK = 17;
    private static final int SHOW_INFO_UPGRADE_DECKS = 18;
    private static final int REQUEST_REVIEW = 19;

    private StyledProgressDialog mProgressDialog;
    private StyledOpenCollectionDialog mOpenCollectionDialog;
    private StyledOpenCollectionDialog mNotMountedDialog;
    private ImageButton mAddButton;
    private ImageButton mCardsButton;
    private ImageButton mStatsButton;
    private ImageButton mSyncButton;

    private File[] mBackups;

    private SimpleAdapter mDeckListAdapter;
    private ArrayList<HashMap<String, String>> mDeckList;
    private ListView mDeckListView;

    private boolean mDontSaveOnStop = false;

    private BroadcastReceiver mUnmountReceiver = null;

    private String mPrefDeckPath = null;
    private long mLastTimeOpened;
    private long mCurrentDid;
    private int mSyncMediaUsn = 0;

    private EditText mDialogEditText;

    int mStatisticType;

    public boolean mFragmented;
    private boolean mInvalidateMenu;

    boolean mCompletionBarRestrictToActive = false; // set this to true in order to calculate completion bar only for
                                                    // active cards

    private int[] mDictValues;

    private int mContextMenuPosition;

    /** Swipe Detection */
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    private boolean mSwipeEnabled;

    // ----------------------------------------------------------------------------
    // LISTENERS
    // ----------------------------------------------------------------------------

    private AdapterView.OnItemClickListener mDeckSelHandler = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int p, long id) {
            handleDeckSelection(p);
        }
    };

    private DialogInterface.OnClickListener mContextMenuListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int item) {
            Resources res = getResources();

            @SuppressWarnings("unchecked")
            HashMap<String, String> data = (HashMap<String, String>) mDeckListAdapter.getItem(mContextMenuPosition);
            switch (item) {
            	case CONTEXT_MENU_COLLAPSE_DECK:
    				try {
    					JSONObject deck = AnkiDroidApp.getCol().getDecks().get(mCurrentDid);
    					if (AnkiDroidApp.getCol().getDecks().children(mCurrentDid).size() > 0) {
        					deck.put("collapsed", !deck.getBoolean("collapsed"));
        					AnkiDroidApp.getCol().getDecks().save(deck);
                    		loadCounts();
    					}
    				} catch (JSONException e1) {
    					// do nothing
    				}
            		return;
                case CONTEXT_MENU_DELETE_DECK:
                    showDialog(DIALOG_DELETE_DECK);
                    return;
                case CONTEXT_MENU_RESET_LANGUAGE:
                    // resetDeckLanguages(data.get("filepath"));
                    return;
                case CONTEXT_MENU_CUSTOM_DICTIONARY:
                    // String[] dicts = res.getStringArray(R.array.dictionary_labels);
                    // String[] vals = res.getStringArray(R.array.dictionary_values);
                    // int currentSet = MetaDB.getLookupDictionary(DeckPicker.this, data.get("filepath"));
                    //
                    // mCurrentDeckPath = data.get("filepath");
                    // String[] labels = new String[dicts.length + 1];
                    // mDictValues = new int[dicts.length + 1];
                    // int currentChoice = 0;
                    // labels[0] = res.getString(R.string.deckpicker_select_dictionary_default);
                    // mDictValues[0] = -1;
                    // for (int i = 1; i < labels.length; i++) {
                    // labels[i] = dicts[i-1];
                    // mDictValues[i] = Integer.parseInt(vals[i-1]);
                    // if (currentSet == mDictValues[i]) {
                    // currentChoice = i;
                    // }
                    // }
                    // StyledDialog.Builder builder = new StyledDialog.Builder(DeckPicker.this);
                    // builder.setTitle(res.getString(R.string.deckpicker_select_dictionary_title));
                    // builder.setSingleChoiceItems(labels, currentChoice, new DialogInterface.OnClickListener() {
                    // public void onClick(DialogInterface dialog, int item) {
                    // MetaDB.storeLookupDictionary(DeckPicker.this, mCurrentDeckPath, mDictValues[item]);
                    // }
                    // });
                    // StyledDialog alert = builder.create();
                    // alert.show();
                    return;
                case CONTEXT_MENU_RENAME_DECK:
                    StyledDialog.Builder builder2 = new StyledDialog.Builder(DeckPicker.this);
                    builder2.setTitle(res.getString(R.string.contextmenu_deckpicker_rename_deck));

                    mDialogEditText = (EditText) new EditText(DeckPicker.this);
                    mDialogEditText.setSingleLine();
                    mDialogEditText.setText(AnkiDroidApp.getCol().getDecks().name(mCurrentDid));
                    // mDialogEditText.setFilters(new InputFilter[] { mDeckNameFilter });
                    builder2.setView(mDialogEditText, false, false);
                    builder2.setPositiveButton(res.getString(R.string.rename), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newName = mDialogEditText.getText().toString().replaceAll("['\"]", "");
                            Collection col = AnkiDroidApp.getCol();
                            if (col != null) {
                                if (col.getDecks().rename(col.getDecks().get(mCurrentDid), newName)) {
                                    for (HashMap<String, String> d : mDeckList) {
                                        if (d.get("did").equals(Long.toString(mCurrentDid))) {
                                            d.put("name", newName);
                                        }
                                    }
                                    mDeckListAdapter.notifyDataSetChanged();
                                    loadCounts();
                                } else {
                                    try {
                                        Themes.showThemedToast(
                                                DeckPicker.this,
                                                getResources().getString(R.string.rename_error,
                                                		col.getDecks().get(mCurrentDid).get("name")), false);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }                            	
                            }
                        }
                    });
                    builder2.setNegativeButton(res.getString(R.string.cancel), null);
                    builder2.create().show();
                    return;
                case CONTEXT_MENU_DECK_SUMMARY:
                    // mStatisticType = 0;
                    // DeckTask.launchDeckTask(DeckTask.TASK_TYPE_LOAD_STATISTICS, mLoadStatisticsHandler, new
                    // DeckTask.TaskData(DeckPicker.this, new String[]{data.get("filepath")}, mStatisticType, 0));
                    return;
            }
        }
    };

    private Connection.TaskListener mSyncListener = new Connection.TaskListener() {

        String currentMessage;
        long countUp;
        long countDown;


        @Override
        public void onDisconnected() {
            showDialog(DIALOG_NO_CONNECTION);
        }


        @Override
        public void onPreExecute() {
        	mDontSaveOnStop = true;
            countUp = 0;
            countDown = 0;
            if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                mProgressDialog = StyledProgressDialog
                        .show(DeckPicker.this, getResources().getString(R.string.sync_title),
                                getResources().getString(R.string.sync_prepare_syncing) + "\n"
                                        + getResources().getString(R.string.sync_up_down_size, countUp, countDown),
                                true, false);
            }
        }


        @Override
        public void onProgressUpdate(Object... values) {
            Resources res = getResources();
            if (values[0] instanceof Boolean) {
                // This is the part Download missing media of syncing
                int total = ((Integer) values[1]).intValue();
                int done = ((Integer) values[2]).intValue();
                values[0] = ((String) values[3]);
                values[1] = res.getString(R.string.sync_downloading_media, done, total);
            } else if (values[0] instanceof Integer) {
                int id = (Integer) values[0];
                if (id != 0) {
                    currentMessage = res.getString(id);
                }
                if (values.length >= 3) {
                    countUp = (Long) values[1];
                    countDown = (Long) values[2];
                }
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                // mProgressDialog.setTitle((String) values[0]);
                mProgressDialog.setMessage(currentMessage + "\n"
                        + res.getString(R.string.sync_up_down_size, countUp / 1024, countDown / 1024));
            }
        }


        @Override
        public void onPostExecute(Payload data) {
            Log.i(AnkiDroidApp.TAG, "onPostExecute");
            Resources res = DeckPicker.this.getResources();
        	mDontSaveOnStop = false;
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (!data.success) {
                Object[] result = (Object[]) data.result;
                if (result[0] instanceof String) {
                    String resultType = (String) result[0];
                    if (resultType.equals("badAuth")) {
                        // delete old auth information
                        SharedPreferences preferences = AnkiDroidApp.getSharedPrefs(getBaseContext());
                        Editor editor = preferences.edit();
                        editor.putString("username", "");
                        editor.putString("hkey", "");
                        editor.commit();
                        // then show
                        showDialog(DIALOG_USER_NOT_LOGGED_IN_SYNC);
                    } else if (resultType.equals("noChanges")) {
                        mDialogMessage = res.getString(R.string.sync_no_changes_message);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("clockOff")) {
                        long diff = (Long) result[1];
                        if (diff >= 86400) {
                            // The difference if more than a day
                            mDialogMessage = res.getString(R.string.sync_log_clocks_unsynchronized, diff,
                                    res.getString(R.string.sync_log_clocks_unsynchronized_date));
                        } else if (Math.abs((diff % 3600.0) - 1800.0) >= 1500.0) {
                            // The difference would be within limit if we adjusted the time by few hours
                            // It doesn't work for all timezones, but it covers most and it's a guess anyway
                            mDialogMessage = res.getString(R.string.sync_log_clocks_unsynchronized, diff,
                                    res.getString(R.string.sync_log_clocks_unsynchronized_tz));
                        } else {
                            mDialogMessage = res.getString(R.string.sync_log_clocks_unsynchronized, diff, "");
                        }
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("fullSync")) {
                        if (data.data != null && data.data.length >= 1 && data.data[0] instanceof Integer) {
                            mSyncMediaUsn = (Integer) data.data[0];
                        }
                        showDialog(DIALOG_SYNC_CONFLICT_RESOLUTION);
                    } else if (resultType.equals("dbError")) {
                        mDialogMessage = res.getString(R.string.sync_corrupt_database, R.string.repair_deck);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("overwriteError")) {
                        mDialogMessage = res.getString(R.string.sync_overwrite_error);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("remoteDbError")) {
                        mDialogMessage = res.getString(R.string.sync_remote_db_error);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("sdAccessError")) {
                        mDialogMessage = res.getString(R.string.sync_write_access_error);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("finishError")) {
                        mDialogMessage = res.getString(R.string.sync_log_finish_error);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("IOException")) {
                    	handleDbError();
                    } else if (resultType.equals("genericError")) {
                        mDialogMessage = res.getString(R.string.sync_generic_error);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("OutOfMemoryError")) {
                        mDialogMessage = res.getString(R.string.error_insufficient_memory);
                        showDialog(DIALOG_SYNC_LOG);
                    } else if (resultType.equals("upgradeRequired")) {
                        showDialog(DIALOG_SYNC_UPGRADE_REQUIRED);
                    } else if (resultType.equals("sanityCheckError")) {
                        mDialogMessage = res.getString(R.string.sync_log_error_fix, result[1] != null ? (" (" + (String) result[1] + ")") : "");
                        showDialog(DIALOG_SYNC_SANITY_ERROR);
                    } else {
                    	if (result.length > 1 && result[1] instanceof Integer) {
                            int type = (Integer) result[1];
                            switch (type) {
                                case 503:
                                    mDialogMessage = res.getString(R.string.sync_too_busy);
                                    break;
                                default:
                                    mDialogMessage = res.getString(R.string.sync_log_error_specific,
                                            Integer.toString(type), (String) result[2]);
                                    break;
                            }                    		
                    	} else if (result[0] instanceof String) {
                            mDialogMessage = res.getString(R.string.sync_log_error_specific,
                                    -1, (String) result[0]);
                    	} else {
                            mDialogMessage = res.getString(R.string.sync_generic_error);
                    	}
                        showDialog(DIALOG_SYNC_LOG);
                    }
                }
            } else {
                updateDecksList((TreeSet<Object[]>) data.result, (Integer) data.data[2], (Integer) data.data[3]);
                if (data.data[4] != null) {
                    mDialogMessage = (String) data.data[4];
                } else if (data.data.length > 0 && data.data[0] instanceof String && ((String) data.data[0]).length() > 0) {
                    String dataString = (String) data.data[0];
                    if (dataString.equals("upload")) {
                        mDialogMessage = res.getString(R.string.sync_log_uploading_message);
                    } else if (dataString.equals("download")) {
                        mDialogMessage = res.getString(R.string.sync_log_downloading_message);
                        // set downloaded collection as current one
                    } else {
                        mDialogMessage = res.getString(R.string.sync_database_success);
                    }
                } else {
                    mDialogMessage = res.getString(R.string.sync_database_success);
                }

                showDialog(DIALOG_SYNC_LOG);

                // close opening dialog in case it's open
            	if (mOpenCollectionDialog != null && mOpenCollectionDialog.isShowing()) {
            		mOpenCollectionDialog.dismiss();
            	}

                // update StudyOptions too if open
                if (mFragmented) {
                	StudyOptionsFragment frag = getFragment();
                	if (frag != null) {
                		frag.resetAndUpdateValuesFromDeck();
                	}
                }
            }
        }
    };


    DeckTask.TaskListener mOpenCollectionHandler = new DeckTask.TaskListener() {

        @Override
        public void onPostExecute(DeckTask.TaskData result) {
            Collection col = result.getCollection();
            Object[] res = result.getObjArray();
            if (col == null || res == null) {
            	AnkiDatabaseManager.closeDatabase(AnkiDroidApp.getCollectionPath());
                showDialog(DIALOG_LOAD_FAILED);
                return;
            }
            updateDecksList((TreeSet<Object[]>) res[0], (Integer) res[1], (Integer) res[2]);
            // select last loaded deck if any
            if (mFragmented) {
            	long did = col.getDecks().selected();
            	for (int i = 0; i < mDeckList.size(); i++) {
            		if (Long.parseLong(mDeckList.get(i).get("did")) == did) {
            			final int lastPosition = i;
                        mDeckListView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                            	mDeckListView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            	mDeckListView.performItemClick(null, lastPosition, 0);
                            }
                        });
                        break;
            		}
            	}
            }
            if (AnkiDroidApp.colIsOpen() && mImportPath != null) {
            	showDialog(DIALOG_IMPORT);
            }
            if (mOpenCollectionDialog.isShowing()) {
                try {
                	mOpenCollectionDialog.dismiss();
                } catch (Exception e) {
                    Log.e(AnkiDroidApp.TAG, "onPostExecute - Dialog dismiss Exception = " + e.getMessage());
                }
            }
        }


        @Override
        public void onPreExecute() {
        	if (mOpenCollectionDialog == null || !mOpenCollectionDialog.isShowing()) {
        		mOpenCollectionDialog = StyledOpenCollectionDialog.show(DeckPicker.this, getResources().getString(R.string.open_collection), new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        // TODO: close dbs?
                        DeckTask.cancelTask();
                        finishWithAnimation();
                    }
                });
        	}
        }


        @Override
        public void onProgressUpdate(DeckTask.TaskData... values) {
            String message = values[0].getString();
            if (message != null) {
            	mOpenCollectionDialog.setMessage(message);
            }        		
        }
    };

    DeckTask.TaskListener mLoadCountsHandler = new DeckTask.TaskListener() {

        @Override
        public void onPostExecute(DeckTask.TaskData result) {
        	if (result == null) {
        		return;
        	}
            Object[] res = result.getObjArray();
            updateDecksList((TreeSet<Object[]>) res[0], (Integer) res[1], (Integer) res[2]);
        	if (mOpenCollectionDialog != null && mOpenCollectionDialog.isShowing()) {
            	mOpenCollectionDialog.dismiss();        		
        	}
        }


        @Override
        public void onPreExecute() {
        }


        @Override
        public void onProgressUpdate(DeckTask.TaskData... values) {
        }
    };

    DeckTask.TaskListener mCloseCollectionHandler = new DeckTask.TaskListener() {

        @Override
        public void onPostExecute(DeckTask.TaskData result) {
        }


        @Override
        public void onPreExecute() {
        }


        @Override
        public void onProgressUpdate(DeckTask.TaskData... values) {
        }
    };

    DeckTask.TaskListener mLoadStatisticsHandler = new DeckTask.TaskListener() {

        @Override
        public void onPostExecute(DeckTask.TaskData result) {
            if (mProgressDialog.isShowing()) {
                try {
                    mProgressDialog.dismiss();
                } catch (Exception e) {
                    Log.e(AnkiDroidApp.TAG, "onPostExecute - Dialog dismiss Exception = " + e.getMessage());
                }
            }
            if (result.getBoolean()) {
                // if (mStatisticType == Statistics.TYPE_DECK_SUMMARY) {
                // Statistics.showDeckSummary(DeckPicker.this);
                // } else {
                Intent intent = new Intent(DeckPicker.this, com.ichi2.charts.ChartBuilder.class);
                startActivity(intent);
                if (AnkiDroidApp.SDK_VERSION > 4) {
                    ActivityTransitionAnimation.slide(DeckPicker.this, ActivityTransitionAnimation.DOWN);
                }
                // }
            } else {
                // TODO: db error handling
            }
        }


        @Override
        public void onPreExecute() {
            mProgressDialog = StyledProgressDialog.show(DeckPicker.this, "",
                    getResources().getString(R.string.calculating_statistics), true);
        }


        @Override
        public void onProgressUpdate(DeckTask.TaskData... values) {
        }

    };

    DeckTask.TaskListener mRepairDeckHandler = new DeckTask.TaskListener() {

        @Override
        public void onPreExecute() {
            mProgressDialog = StyledProgressDialog.show(DeckPicker.this, "",
                    getResources().getString(R.string.backup_repair_deck_progress), true);
        }


        @Override
        public void onPostExecute(DeckTask.TaskData result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result.getBoolean()) {
                loadCollection();
            } else {
                Themes.showThemedToast(DeckPicker.this, getResources().getString(R.string.deck_repair_error), true);
                showDialog(DIALOG_ERROR_HANDLING);
            }
        }


        @Override
        public void onProgressUpdate(TaskData... values) {
        }

    };


     DeckTask.TaskListener mRestoreDeckHandler = new DeckTask.TaskListener() {
    
     @Override
     public void onPreExecute() {
    	 mProgressDialog = StyledProgressDialog.show(DeckPicker.this, "", getResources().getString(R.string.backup_restore_deck), true);
     }
    
    
     @Override
     public void onPostExecute(DeckTask.TaskData result) {
    	 switch (result.getInt()) {
    	 case BackupManager.RETURN_DECK_RESTORED:
    		 loadCollection();
    		 break;
    	 case BackupManager.RETURN_ERROR:
    		 Themes.showThemedToast(DeckPicker.this, getResources().getString(R.string.backup_restore_error), true);
    		 showDialog(DIALOG_ERROR_HANDLING);
    		 break;
    	 case BackupManager.RETURN_NOT_ENOUGH_SPACE:
    		 showDialog(DIALOG_NO_SPACE_LEFT);
    		 break;
    	 }
    	 if (mProgressDialog != null && mProgressDialog.isShowing()) {
    		 mProgressDialog.dismiss();
    	 }
     }
    
     @Override
     public void onProgressUpdate(TaskData... values) {
     }
    
     };

    // ----------------------------------------------------------------------------
    // ANDROID METHODS
    // ----------------------------------------------------------------------------

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) throws SQLException {   	
        Log.i(AnkiDroidApp.TAG, "DeckPicker - onCreate");
        Intent intent = getIntent();
        if (!isTaskRoot()) {
            Log.i(AnkiDroidApp.TAG,
                    "DeckPicker - onCreate: Detected multiple instance of this activity, closing it and return to root activity");
            Intent reloadIntent = new Intent(DeckPicker.this, DeckPicker.class);
            reloadIntent.setAction(Intent.ACTION_MAIN);
            if (intent != null && intent.getExtras() != null) {
                reloadIntent.putExtras(intent.getExtras());
            }
            if (intent != null && intent.getData() != null) {
                reloadIntent.setData(intent.getData());
            }
            reloadIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            reloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivityIfNeeded(reloadIntent, 0);
        }
        if (intent.getData() != null) {
        	mImportPath = getIntent().getData().getEncodedPath();
        }

        // need to start this here in order to avoid showing deckpicker before splashscreen
        if (AnkiDroidApp.colIsOpen()) {
            setTitle(getResources().getString(R.string.app_name));
        } else {
            setTitle("");
            mOpenCollectionHandler.onPreExecute();        	
        }

        Themes.applyTheme(this);
        super.onCreate(savedInstanceState);

        // mStartedByBigWidget = intent.getIntExtra(EXTRA_START, EXTRA_START_NOTHING);

        SharedPreferences preferences = restorePreferences();

        // activate broadcast messages if first start of a day
        if (mLastTimeOpened < UIUtils.getDayStart()) {
            preferences.edit().putBoolean("showBroadcastMessageToday", true).commit();
        }
        preferences.edit().putLong("lastTimeOpened", System.currentTimeMillis()).commit();

        // if (intent != null && intent.hasExtra(EXTRA_DECK_ID)) {
        // openStudyOptions(intent.getLongExtra(EXTRA_DECK_ID, 1));
        // }

        BroadcastMessages.checkForNewMessages(this);

        View mainView = getLayoutInflater().inflate(R.layout.deck_picker, null);
        setContentView(mainView);

        // check, if tablet layout
        View studyoptionsFrame = findViewById(R.id.studyoptions_fragment);
        mFragmented = studyoptionsFrame != null && studyoptionsFrame.getVisibility() == View.VISIBLE;

        Themes.setContentStyle(mFragmented ? mainView : mainView.findViewById(R.id.deckpicker_view), Themes.CALLER_DECKPICKER);

        registerExternalStorageListener();

        if (!mFragmented) {
            mAddButton = (ImageButton) findViewById(R.id.deckpicker_add);
            mAddButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNote();
                }
            });

            mCardsButton = (ImageButton) findViewById(R.id.deckpicker_card_browser);
            mCardsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCardBrowser();
                }
            });

            mStatsButton = (ImageButton) findViewById(R.id.statistics_all_button);
            mStatsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(DIALOG_SELECT_STATISTICS_TYPE);
                }
            });

            mSyncButton = (ImageButton) findViewById(R.id.sync_all_button);
            mSyncButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sync();
                }
            });
        }

        mInvalidateMenu = false;
        mDeckList = new ArrayList<HashMap<String, String>>();
        mDeckListView = (ListView) findViewById(R.id.files);
        mDeckListAdapter = new SimpleAdapter(this, mDeckList, R.layout.deck_item, new String[] { "name", "new", "lrn",
                "rev", // "complMat", "complAll",
                "sep" }, new int[] { R.id.DeckPickerName, R.id.deckpicker_new, R.id.deckpicker_lrn,
                R.id.deckpicker_rev, // R.id.deckpicker_bar_mat, R.id.deckpicker_bar_all,
                R.id.DeckPickerName });
        mDeckListAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String text) {
                if (view.getId() == R.id.DeckPickerName) {
                    View parent = (View) view.getParent().getParent();
                    if (text.equals("top")) {
                        parent.setBackgroundResource(R.drawable.white_deckpicker_top);
                        return true;
                    } else if (text.equals("bot")) {
                        parent.setBackgroundResource(R.drawable.white_deckpicker_bottom);
                        return true;
                    } else if (text.equals("ful")) {
                        parent.setBackgroundResource(R.drawable.white_deckpicker_full);
                        return true;
                    } else if (text.equals("cen")) {
                        parent.setBackgroundResource(R.drawable.white_deckpicker_center);
                        return true;
                    }
                    return false;
                    // } else if (view.getId() == R.id.deckpicker_bar_mat || view.getId() == R.id.deckpicker_bar_all) {
                    // if (text.length() > 0 && !text.equals("-1.0")) {
                    // View parent = (View)view.getParent().getParent();
                    // if (text.equals("-2")) {
                    // parent.setVisibility(View.GONE);
                    // } else {
                    // Utils.updateProgressBars(view, (int) UIUtils.getDensityAdjustedValue(DeckPicker.this, 3.4f),
                    // (int) (Double.parseDouble(text) * ((View)view.getParent().getParent().getParent()).getHeight()));
                    // if (parent.getVisibility() == View.INVISIBLE) {
                    // parent.setVisibility(View.VISIBLE);
                    // parent.setAnimation(ViewAnimation.fade(ViewAnimation.FADE_IN, 500, 0));
                    // }
                    // }
                    // }
                    // return true;
                    // } else if (view.getVisibility() == View.INVISIBLE) {
                    // if (!text.equals("-1")) {
                    // view.setVisibility(View.VISIBLE);
                    // view.setAnimation(ViewAnimation.fade(ViewAnimation.FADE_IN, 500, 0));
                    // return false;
                    // }
                    // } else if (text.equals("-1")){
                    // view.setVisibility(View.INVISIBLE);
                    // return false;
                }
                return false;
            }
        });
        mDeckListView.setOnItemClickListener(mDeckSelHandler);
        mDeckListView.setAdapter(mDeckListAdapter);

        if (mFragmented) {
            mDeckListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        registerForContextMenu(mDeckListView);

        showStartupScreensAndDialogs(preferences, 0);

        if (mSwipeEnabled) {
            gestureDetector = new GestureDetector(new MyGestureDetector());
            mDeckListView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (gestureDetector.onTouchEvent(event)) {
                        return true;
                    }
                    return false;
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (AnkiDroidApp.getCol() != null) {
            if (Utils.now() > AnkiDroidApp.getCol().getSched().getDayCutoff() && AnkiDroidApp.isSdCardMounted()) {
                loadCounts();
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putLong("mCurrentDid", mCurrentDid);
//      savedInstanceState.putSerializable("mDeckList", mDeckList);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      mCurrentDid = savedInstanceState.getLong("mCurrentDid");
//      mDeckList = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable("mDeckList");
    }

    private void loadCollection() {
    	if (!AnkiDroidApp.isSdCardMounted()) {
    		showDialog(DIALOG_SD_CARD_NOT_MOUNTED);
    		return;
    	}
    	String path = AnkiDroidApp.getCollectionPath();
        Collection col = AnkiDroidApp.getCol();
        if (col == null || !col.getPath().equals(path)) {
            DeckTask.launchDeckTask(DeckTask.TASK_TYPE_OPEN_COLLECTION, mOpenCollectionHandler, new DeckTask.TaskData(path));        	
        } else {
        	loadCounts();
        }
    }


    public void loadCounts() {
        if (AnkiDroidApp.colIsOpen()) {
            DeckTask.launchDeckTask(DeckTask.TASK_TYPE_LOAD_DECK_COUNTS, mLoadCountsHandler, new TaskData(AnkiDroidApp.getCol()));
        }
    }


    private void addNote() {
        Intent intent = new Intent(DeckPicker.this, CardEditor.class);
        intent.putExtra(CardEditor.EXTRA_CALLER, CardEditor.CALLER_DECKPICKER);
        startActivityForResult(intent, ADD_NOTE);
        if (AnkiDroidApp.SDK_VERSION > 4) {
            ActivityTransitionAnimation.slide(DeckPicker.this, ActivityTransitionAnimation.LEFT);
        }
    }


    private void openCardBrowser() {
        Intent cardBrowser = new Intent(DeckPicker.this, CardBrowser.class);
        cardBrowser.putExtra("fromDeckpicker", true);
        startActivityForResult(cardBrowser, BROWSE_CARDS);
        if (AnkiDroidApp.SDK_VERSION > 4) {
            ActivityTransitionAnimation.slide(DeckPicker.this, ActivityTransitionAnimation.LEFT);
        }
    }


    private boolean hasErrorFiles() {
        for (String file : this.fileList()) {
            if (file.endsWith(".stacktrace")) {
                return true;
            }
        }
        return false;
    }


    private boolean upgradeNeeded() {
    	if (!AnkiDroidApp.isSdCardMounted()) {
    		showDialog(DIALOG_SD_CARD_NOT_MOUNTED);
    		return false;
    	}
    	File dir = new File(AnkiDroidApp.getCurrentAnkiDroidDirectory(this));
        if (!dir.isDirectory()) {
        	dir.mkdirs();
        }
        if ((new File(AnkiDroidApp.getCollectionPath())).exists()) {
            // collection file exists
            return false;
        }
        // else check for old files to upgrade
        if (dir.listFiles(new OldAnkiDeckFilter()).length > 0) {
            return true;
        }
        return false;
    }


    private SharedPreferences restorePreferences() {
        SharedPreferences preferences = AnkiDroidApp.getSharedPrefs(getBaseContext());
        mPrefDeckPath = AnkiDroidApp.getCurrentAnkiDroidDirectory(this);
        mLastTimeOpened = preferences.getLong("lastTimeOpened", 0);
        mSwipeEnabled = AnkiDroidApp.initiateGestures(this, preferences);

        // mInvertedColors = preferences.getBoolean("invertedColors", false);
        // mSwap = preferences.getBoolean("swapqa", false);
        // mLocale = preferences.getString("language", "");
        // setLanguage(mLocale);

        return preferences;
    }


    private void showStartupScreensAndDialogs(SharedPreferences preferences, int skip) {
        if (skip < 1 && preferences.getLong("lastTimeOpened", 0) == 0) {
            Intent infoIntent = new Intent(this, Info.class);
            infoIntent.putExtra(Info.TYPE_EXTRA, Info.TYPE_WELCOME);
            startActivityForResult(infoIntent, SHOW_INFO_WELCOME);
            if (skip != 0 && AnkiDroidApp.SDK_VERSION > 4) {
                ActivityTransitionAnimation.slide(this, ActivityTransitionAnimation.LEFT);
            }
        } else if (skip < 2 && !preferences.getString("lastVersion", "").equals(AnkiDroidApp.getPkgVersion())) {
            preferences.edit().putBoolean("showBroadcastMessageToday", true).commit();
            Intent infoIntent = new Intent(this, Info.class);
            infoIntent.putExtra(Info.TYPE_EXTRA, Info.TYPE_NEW_VERSION);
            startActivityForResult(infoIntent, SHOW_INFO_NEW_VERSION);
            if (skip != 0 && AnkiDroidApp.SDK_VERSION > 4) {
                ActivityTransitionAnimation.slide(this, ActivityTransitionAnimation.LEFT);
            }
        } else if (skip < 3 && upgradeNeeded()) {
            Intent upgradeIntent = new Intent(this, Info.class);
            upgradeIntent.putExtra(Info.TYPE_EXTRA, Info.TYPE_UPGRADE_DECKS);
            startActivityForResult(upgradeIntent, SHOW_INFO_UPGRADE_DECKS);
            if (skip != 0 && AnkiDroidApp.SDK_VERSION > 4) {
                ActivityTransitionAnimation.slide(this, ActivityTransitionAnimation.LEFT);
            }
        } else if (skip < 4 && hasErrorFiles()) {
            Intent i = new Intent(this, Feedback.class);
            startActivityForResult(i, REPORT_ERROR);
            if (skip != 0 && AnkiDroidApp.SDK_VERSION > 4) {
                ActivityTransitionAnimation.slide(this, ActivityTransitionAnimation.LEFT);
            }
        } else if (!AnkiDroidApp.isSdCardMounted()) {
            showDialog(DIALOG_SD_CARD_NOT_MOUNTED);
        } else if (!BackupManager.enoughDiscSpace(mPrefDeckPath)) {// && !preferences.getBoolean("dontShowLowMemory",
                                                                   // false)) {
            showDialog(DIALOG_NO_SPACE_LEFT);
        } else if (preferences.getBoolean("noSpaceLeft", false)) {
            showDialog(DIALOG_BACKUP_NO_SPACE_LEFT);
            preferences.edit().putBoolean("noSpaceLeft", false).commit();
        } else if (mImportPath != null && AnkiDroidApp.colIsOpen()) {
        	showDialog(DIALOG_IMPORT);
        } else {
            loadCollection();
        }
    }


    protected void sendKey(int keycode) {
        this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
        this.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keycode));
    }


    @Override
    protected void onPause() {
        Log.i(AnkiDroidApp.TAG, "DeckPicker - onPause");

        super.onPause();
    }


    @Override
    protected void onStop() {
        Log.i(AnkiDroidApp.TAG, "DeckPicker - onStop");
        super.onStop();
        if (!mDontSaveOnStop) {
            if (isFinishing()) {
                DeckTask.launchDeckTask(DeckTask.TASK_TYPE_CLOSE_DECK, mCloseCollectionHandler, new TaskData(AnkiDroidApp.getCol()));
            } else {
            	StudyOptionsFragment frag = getFragment();
            	if (!(frag != null && !frag.dbSaveNecessary())) {
                	UIUtils.saveCollectionInBackground();
            	}
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
        }
        Log.i(AnkiDroidApp.TAG, "DeckPicker - onDestroy()");
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        StyledDialog dialog;
        Resources res = getResources();
        StyledDialog.Builder builder = new StyledDialog.Builder(this);

        switch (id) {
            case DIALOG_OK:
                builder.setPositiveButton(R.string.ok, null);
                dialog = builder.create();
                break;

            case DIALOG_NO_SDCARD:
                builder.setMessage("The SD card could not be read. Please, turn off USB storage.");
                builder.setPositiveButton(R.string.ok, null);
                dialog = builder.create();
                break;

            case DIALOG_SELECT_HELP:
                builder.setTitle(res.getString(R.string.help_title));
                builder.setItems(
                        new String[] { res.getString(R.string.help_tutorial), res.getString(R.string.help_online),
                                res.getString(R.string.help_faq) }, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (arg1 == 0) {
                                    createTutorialDeck();
                                } else {
                                    if (Utils.isIntentAvailable(DeckPicker.this, "android.intent.action.VIEW")) {
                                        Intent intent = new Intent("android.intent.action.VIEW", Uri
                                                .parse(getResources().getString(
                                                        arg1 == 0 ? R.string.link_help : R.string.link_faq)));
                                        startActivity(intent);
                                    } else {
                                        startActivity(new Intent(DeckPicker.this, Info.class));
                                    }
                                }
                            }

                        });
                dialog = builder.create();
                break;

            case DIALOG_CONNECTION_ERROR:
                builder.setTitle(res.getString(R.string.connection_error_title));
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setMessage(res.getString(R.string.connection_error_message));
                builder.setPositiveButton(res.getString(R.string.retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sync();
                    }
                });
                builder.setNegativeButton(res.getString(R.string.cancel), null);
                dialog = builder.create();
                break;

            case DIALOG_SYNC_CONFLICT_RESOLUTION:
                builder.setTitle(res.getString(R.string.sync_conflict_title));
                builder.setIcon(android.R.drawable.ic_input_get);
                builder.setMessage(res.getString(R.string.sync_conflict_message));
                builder.setPositiveButton(res.getString(R.string.sync_conflict_local), mSyncConflictResolutionListener);
                builder.setNeutralButton(res.getString(R.string.sync_conflict_remote), mSyncConflictResolutionListener);
                builder.setNegativeButton(res.getString(R.string.sync_conflict_cancel), mSyncConflictResolutionListener);
                builder.setCancelable(true);
                dialog = builder.create();
                break;

            case DIALOG_LOAD_FAILED:
                builder.setMessage(res.getString(R.string.open_collection_failed_message,
                        BackupManager.BROKEN_DECKS_SUFFIX, res.getString(R.string.repair_deck)));
                builder.setTitle(R.string.open_collection_failed_title);
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setPositiveButton(res.getString(R.string.error_handling_options),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showDialog(DIALOG_ERROR_HANDLING);
                            }
                        });
                builder.setNegativeButton(res.getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishWithAnimation();
                    }
                });
                builder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finishWithAnimation();
                    }
                });
                dialog = builder.create();
                break;

            case DIALOG_DB_ERROR:
                builder.setMessage(R.string.answering_error_message);
                builder.setTitle(R.string.answering_error_title);
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setPositiveButton(res.getString(R.string.error_handling_options),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showDialog(DIALOG_ERROR_HANDLING);
                            }
                        });
                builder.setNeutralButton(res.getString(R.string.answering_error_report),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(DeckPicker.this, Feedback.class);
                                i.putExtra("request", RESULT_DB_ERROR);
                                dialog.dismiss();
                                startActivityForResult(i, REPORT_ERROR);
                                if (AnkiDroidApp.SDK_VERSION > 4) {
                                    ActivityTransitionAnimation.slide(DeckPicker.this,
                                            ActivityTransitionAnimation.RIGHT);
                                }
                            }
                        });
                builder.setNegativeButton(res.getString(R.string.close),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	if (!AnkiDroidApp.colIsOpen()) {
                    		finishWithAnimation();
                    	}
                    }
                });
                builder.setCancelable(true);
                dialog = builder.create();
                break;

            case DIALOG_ERROR_HANDLING:
                builder.setTitle(res.getString(R.string.error_handling_title));
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setSingleChoiceItems(new String[] { "1" }, 0, null);
                builder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mLoadFailed) {
                        	// dialog has been called because collection could not be opened
                            showDialog(DIALOG_LOAD_FAILED);
                        } else {
                        	// dialog has been called because a db error happened
                            showDialog(DIALOG_DB_ERROR);
                        }
                    }
                });
                builder.setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mLoadFailed) {
                        	// dialog has been called because collection could not be opened
                            showDialog(DIALOG_LOAD_FAILED);
                        } else {
                        	// dialog has been called because a db error happened
                            showDialog(DIALOG_DB_ERROR);
                        }
                    }
                });
                dialog = builder.create();
                break;

            case DIALOG_USER_NOT_LOGGED_IN_ADD_SHARED_DECK:
                builder.setTitle(res.getString(R.string.connection_error_title));
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setMessage(res.getString(R.string.no_user_password_error_message));
                builder.setNegativeButton(res.getString(R.string.cancel), null);
                builder.setPositiveButton(res.getString(R.string.log_in), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myAccount = new Intent(DeckPicker.this, MyAccount.class);
                        myAccount.putExtra("notLoggedIn", true);
                        startActivityForResult(myAccount, LOG_IN_FOR_SHARED_DECK);
                        if (AnkiDroidApp.SDK_VERSION > 4) {
                            ActivityTransitionAnimation.slide(DeckPicker.this, ActivityTransitionAnimation.FADE);
                        }
                    }
                });
                dialog = builder.create();
                break;

            case DIALOG_USER_NOT_LOGGED_IN_SYNC:
                builder.setTitle(res.getString(R.string.connection_error_title));
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setMessage(res.getString(R.string.no_user_password_error_message));
                builder.setNegativeButton(res.getString(R.string.cancel), null);
                builder.setPositiveButton(res.getString(R.string.log_in), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myAccount = new Intent(DeckPicker.this, MyAccount.class);
                        myAccount.putExtra("notLoggedIn", true);
                        startActivityForResult(myAccount, LOG_IN_FOR_SYNC);
                        if (AnkiDroidApp.SDK_VERSION > 4) {
                            ActivityTransitionAnimation.slide(DeckPicker.this, ActivityTransitionAnimation.FADE);
                        }
                    }
                });
                dialog = builder.create();
                break;

            // case DIALOG_USER_NOT_LOGGED_IN_DOWNLOAD:
            // if (id == DIALOG_USER_NOT_LOGGED_IN_SYNC) {
            // } else {
            // builder.setPositiveButton(res.getString(R.string.log_in),
            // new DialogInterface.OnClickListener() {
            //
            // @Override
            // public void onClick(DialogInterface dialog, int which) {
            // Intent myAccount = new Intent(DeckPicker.this,
            // MyAccount.class);
            // myAccount.putExtra("notLoggedIn", true);
            // startActivityForResult(myAccount, LOG_IN_FOR_DOWNLOAD);
            // if (UIUtils.getApiLevel() > 4) {
            // ActivityTransitionAnimation.slide(DeckPicker.this, ActivityTransitionAnimation.LEFT);
            // }
            // }
            // });
            // }
            // builder.setNegativeButton(res.getString(R.string.cancel), null);
            // dialog = builder.create();
            // break;

            case DIALOG_NO_CONNECTION:
                builder.setTitle(res.getString(R.string.connection_error_title));
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setMessage(res.getString(R.string.connection_needed));
                builder.setPositiveButton(res.getString(R.string.ok), null);
                dialog = builder.create();
                break;

            case DIALOG_DELETE_DECK:
            	if (!AnkiDroidApp.colIsOpen() || mDeckList == null) {
            		return null;
            	}
                builder.setTitle(res.getString(R.string.delete_deck_title));
                builder.setIcon(R.drawable.ic_dialog_alert);
                builder.setMessage(String.format(res.getString(R.string.delete_deck_message), "\'"
                        + AnkiDroidApp.getCol().getDecks().name(mCurrentDid) + "\'"));
                builder.setPositiveButton(res.getString(R.string.delete_deck_confirm),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (AnkiDroidApp.getCol().getDecks().selected() == mCurrentDid) {
                                    Fragment frag = (Fragment) getSupportFragmentManager().findFragmentById(
                                            R.id.studyoptions_fragment);
                                    if (frag != null && frag instanceof StudyOptionsFragment) {
                                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                        ft.remove(frag);
                                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                        ft.commit();
                                    }
                                }
                                DeckTask.launchDeckTask(DeckTask.TASK_TYPE_DELETE_DECK, new DeckTask.TaskListener() {
                                    @Override
                                    public void onPreExecute() {
                                        mProgressDialog = StyledProgressDialog.show(DeckPicker.this, "", getResources()
                                                .getString(R.string.delete_deck), true);
                                    }


                                    @Override
                                    public void onPostExecute(TaskData result) {
                                        if (result == null) {
                                            return;
                                        }
                                        Object[] res = result.getObjArray();
                                        updateDecksList((TreeSet<Object[]>) res[0], (Integer) res[1], (Integer) res[2]);
                                        if (mProgressDialog.isShowing()) {
                                            try {
                                                mProgressDialog.dismiss();
                                            } catch (Exception e) {
                                                Log.e(AnkiDroidApp.TAG, "onPostExecute - Dialog dismiss Exception = "
                                                        + e.getMessage());
                                            }
                                        }
                                    }


                                    @Override
                                    public void onProgressUpdate(TaskData... values) {
                                    }
                                }, new TaskData(AnkiDroidApp.getCol(), mCurrentDid));
                            }
                        });
                builder.setNegativeButton(res.getString(R.string.cancel), null);
                dialog = builder.create();
                break;

            case DIALOG_SELECT_STATISTICS_TYPE:
                dialog = ChartBuilder.getStatisticsDialog(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean muh = mFragmented ? AnkiDroidApp.getSharedPrefs(
                                AnkiDroidApp.getInstance().getBaseContext()).getBoolean("statsRange", true) : true;
                        DeckTask.launchDeckTask(
                                DeckTask.TASK_TYPE_LOAD_STATISTICS,
                                mLoadStatisticsHandler,
                                new DeckTask.TaskData(AnkiDroidApp.getCol(), which, mFragmented ? AnkiDroidApp.getSharedPrefs(
                                        AnkiDroidApp.getInstance().getBaseContext()).getBoolean("statsRange", true)
                                        : true));
                    }
                }, mFragmented);
                break;

            case DIALOG_CONTEXT_MENU:
                String[] entries = new String[3];
                // entries[CONTEXT_MENU_DECK_SUMMARY] =
                // "XXXsum";//res.getStringArray(R.array.statistics_type_labels)[0];
                // entries[
