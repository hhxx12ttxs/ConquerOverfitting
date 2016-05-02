package de.winteger.piap.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.winteger.piap.R;
import de.winteger.piap.core.AppListItem;
import de.winteger.piap.data.DataSource;
import de.winteger.piap.listadapter.ArrayAdapterWhiteList;
/**
 * Activity to select/deselect the apps in which logging is enabled 
 * @author sarah
 *
 */
public class SelectAppsActivity extends Activity {
	protected static final String TAG = "Logger";
	private PackageManager p; // used to get app (names)
	private DataSource datasource; // DB access object
	private ArrayList<AppListItem> appList; // used to store the apps
	private ArrayAdapterWhiteList appArrayAdapter; // notifies the appList view
	private ListView appListView; // displays the apps
	
	// listens for clicks on the listview
	private OnItemClickListener defaultListOnItemOnClickHandler = new OnItemClickListener() {

		//@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			Log.i(TAG, "Clicked on pos(" + pos + ") id(" + id + ")");
			AppListItem clickedItem = appList.get(pos);
			
			// toggle the logging-status for the clicked app
			if (clickedItem.isLogEnabled()){
				clickedItem.setLogEnabled(false);			
				// delete DB entry
				datasource.deleteWhiteListItem(clickedItem);
			} else {
				clickedItem.setLogEnabled(true);	
				// Insert in DB
				datasource.createWhiteListItem(clickedItem.getAppName(), clickedItem.getAppPkg());
			}
			// notify the view
			appArrayAdapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_apps);

		appListView = (ListView) findViewById(R.id.lvDB);
		appList = new ArrayList<AppListItem>();

		appArrayAdapter = new ArrayAdapterWhiteList(this, getLayoutInflater(), R.id.lvDB, appList);

		// only for debugging reasons, add input app by default
		datasource = new DataSource(this);
		datasource.openWrite();

		appListView.setAdapter(appArrayAdapter);
		appListView.setOnItemClickListener(defaultListOnItemOnClickHandler);

		// retrieve installed apps and populate the list view
		loadApps();
		sortAppsByName();
		sortAppsByLogStatus();

	}

	/**
	 * Sorts the apps by name, notifies the view
	 */
	private void sortAppsByName() {
		//sorts names ascending
		Collections.sort(appList, new Comparator<AppListItem>() {
			public int compare(AppListItem a0, AppListItem a1) {
				// an integer < 0 if lhs is less than rhs
				// 0 if they are equal
				// and > 0 if lhs is greater than rhs.
				return a0.getAppName().compareTo(a1.getAppName());
			}
		});
		appArrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Sorts the apps by log status, notifies the view
	 */
	private void sortAppsByLogStatus() {
		//sorts apps descending by checked status
		Collections.sort(appList, new Comparator<AppListItem>() {
			public int compare(AppListItem a0, AppListItem a1) {
				// an integer < 0 if lhs is less than rhs
				// 0 if they are equal
				// and > 0 if lhs is greater than rhs.
				if (a0.isLogEnabled() == a1.isLogEnabled()) {
					return 0;
				} else if (a0.isLogEnabled()) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		appArrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Retrieves all installed apps that show in the launcher
	 * Checks in the DB if logging is enabled
	 * Stores the result in app list
	 */
	private void loadApps() {
		
		// get all activities that show up in the launcher
		p = getPackageManager();
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		List<ResolveInfo> resInfo = p.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);

		for (int i = 0; i < resInfo.size(); i++) {
			AppListItem tempItem = new AppListItem();

			ResolveInfo pkgInfo = resInfo.get(i);
			if (pkgInfo.activityInfo != null) {
				// get detailed app-info
				ApplicationInfo appinfo = resInfo.get(i).activityInfo.applicationInfo;

				// retrieve app-name and app-package
				String appName = getApplicationName(p, appinfo);
				String pkgName = resInfo.get(i).activityInfo.packageName;
				// Log.i(TAG, "name = " + appName + ", pkgname = " + pkgName);
				// check if logging for this app is enabled
				boolean isLogged = datasource.isLoggingEnabled(pkgName, appName);
				
				// create list item and add it to the list
				tempItem.setAppName(appName);
				tempItem.setAppPkg(pkgName);
				tempItem.setLogEnabled(isLogged);

				if (!appList.contains(tempItem))
					appList.add(tempItem);
			}
		}
	}

	/**
	 * Return a human-readable application name for the given appinfo
	 * 
	 * @param p the PackageManger
	 * @param appinfo the ApplicationInfo
	 * @return human-readable application name
	 */
	private String getApplicationName(PackageManager p, ApplicationInfo appinfo) {
		if (appinfo != null)
			return (String) p.getApplicationLabel(appinfo);
		return "[not found]";
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Make sure to close the cursor
		datasource.close();
	}
}

