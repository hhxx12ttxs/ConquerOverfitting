package org.imogene.android.app;

import java.util.ArrayList;
import java.util.HashMap;

import org.imogene.android.W;
import org.imogene.android.common.interfaces.Entity;
import org.imogene.android.util.FormatHelper;
import org.imogene.android.util.IamLost;
import org.imogene.android.util.content.IntentUtils;
import org.imogene.android.util.database.DatabaseUtils;
import org.imogene.android.util.dialog.DialogFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;

public abstract class AbstractEntityView extends ScrollingTabActivity implements OnClickListener {
	
	private static final String DES = "descr";
	private static final String VAL = "value";

	private static final int DIALOG_DELETE_ID = 1;
	private static final int DIALOG_INFO_ID = 2;
	private static final int DIALOG_IAMLOST_ID = 3;
	
	protected boolean mCanDelete;
	protected boolean mCanModify;

	private final Handler mHandler = new Handler();

	private final ContentObserver mContentObserver = new ContentObserver(mHandler) {
		public void onChange(boolean selfChange) {
			refresh();
		};
	};

	protected abstract void refresh();

	protected abstract Entity getEntity();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DatabaseUtils.markAs(getContentResolver(), getIntent().getData(), false);
		getContentResolver().registerContentObserver(getIntent().getData(),
				false, mContentObserver);

		IamLost.getInstance().add(getTitle().toString());
	}
	
	@Override
	public final void startActivity(Intent intent) {
		try {
			super.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			IntentUtils.treatException(e, this, intent);
		}
	}
	
	@Override
	public final void startActivityForResult(Intent intent, int requestCode) {
		try {
			super.startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			IntentUtils.treatException(e, this, intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mContentObserver);
		IamLost.getInstance().remove();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(W.menu.menu_entity_view, menu);
		menu.findItem(W.id.menu_edit).setVisible(mCanModify);
		menu.findItem(W.id.menu_delete).setVisible(mCanDelete);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case W.id.menu_edit:
			startActivity(new Intent(Intent.ACTION_EDIT, getIntent().getData()));
			return true;
		case W.id.menu_delete:
			showDialog(DIALOG_DELETE_ID);
			return true;
		case W.id.menu_info:
			showDialog(DIALOG_INFO_ID);
			return true;
		case W.id.menu_iamlost:
			showDialog(DIALOG_IAMLOST_ID);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DELETE_ID:
			return new AlertDialog.Builder(this)
			.setTitle(W.string.delete_confirmation_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(W.string.delete_confirmation)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, this)
			.setCancelable(false)
			.create();
		case DIALOG_INFO_ID:
			final ArrayList<HashMap<String, String>> infos = buildInfo();
			return new AlertDialog.Builder(this)
			.setTitle(W.string.informations)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setAdapter(
				new SimpleAdapter(this, infos,
					W.layout.dialog_list_item, new String[] {
						DES, VAL }, new int[] {
						W.id.dialog_item_title,
						W.id.dialog_item_message }), null)
			.setPositiveButton(android.R.string.ok, null).create();
		case DIALOG_IAMLOST_ID:
			return DialogFactory.createIamLostDialog(this);
		default:
			return super.onCreateDialog(id);
		}
	}
	
	public void onClick(DialogInterface dialog, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			setResult(RESULT_CANCELED);
			finish();
			getContentResolver().unregisterContentObserver(mContentObserver);
			getContentResolver().delete(getIntent().getData(), null, null);
		}
	}

	private ArrayList<HashMap<String, String>> buildInfo() {
		final Entity entity = getEntity();
		final ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

		HashMap<String, String> id = new HashMap<String, String>();
		id.put(DES, getString(W.string.entity_id));
		id.put(VAL, entity.getId());
		result.add(id);

		HashMap<String, String> created = new HashMap<String, String>();
		created.put(DES, getString(W.string.created));
		created.put(VAL, FormatHelper.displayAsDateTime(entity.getCreated()));
		result.add(created);

		HashMap<String, String> createdBy = new HashMap<String, String>();
		createdBy.put(DES, getString(W.string.created_by));
		createdBy.put(VAL, entity.getCreatedBy());
		result.add(createdBy);

		HashMap<String, String> modified = new HashMap<String, String>();
		modified.put(DES, getString(W.string.modified));
		modified.put(VAL, FormatHelper.displayAsDateTime(entity.getModified()));
		result.add(modified);

		HashMap<String, String> modifiedBy = new HashMap<String, String>();
		modifiedBy.put(DES, getString(W.string.modified_by));
		modifiedBy.put(VAL, entity.getModifiedBy());
		result.add(modifiedBy);

		HashMap<String, String> modifiedFrom = new HashMap<String, String>();
		modifiedFrom.put(DES, getString(W.string.modified_from));
		modifiedFrom.put(VAL, entity.getModifiedFrom());
		result.add(modifiedFrom);

		return result;
	}
}

