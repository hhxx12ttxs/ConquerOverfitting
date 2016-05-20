package com.group5.android.fd.activity;

import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.group5.android.fd.FdConfig;
import com.group5.android.fd.Main;
import com.group5.android.fd.R;
import com.group5.android.fd.activity.dialog.QuantityRemoverDialog;
import com.group5.android.fd.adapter.ConfirmAdapter;
import com.group5.android.fd.entity.AbstractEntity;
import com.group5.android.fd.entity.CategoryEntity;
import com.group5.android.fd.entity.ItemEntity;
import com.group5.android.fd.entity.OrderEntity;
import com.group5.android.fd.entity.OrderItemEntity;
import com.group5.android.fd.entity.TableEntity;
import com.group5.android.fd.helper.HttpHelper;
import com.group5.android.fd.helper.ScanHelper;
import com.group5.android.fd.helper.UriStringHelper;

public class NewSessionActivity extends Activity implements OnDismissListener {

	final public static String EXTRA_DATA_NAME_TABLE_OBJ = "tableObj";
	final public static String EXTRA_DATA_NAME_USE_SCANNER = "useScanner";

	final public static int REQUEST_CODE_TABLE = 1;
	final public static int REQUEST_CODE_CATEGORY = 2;
	final public static int REQUEST_CODE_ITEM = 3;
	final public static int REQUEST_CODE_CONFIRM = 4;

	public static final String POST_ORDER_STRING = "Go";
	public static final String CHANGE_ORDER_STRING = "Change";
	public static final int REMOVE_ITEM_MENU = Menu.FIRST;
	public static final String REMOVE_ITEM_MENU_STRING = "Remove";
	protected OrderEntity order = new OrderEntity();
	protected String m_csrfTokenPage = null;
	protected boolean m_useScanner = false;

	// For display confirm View
	protected ConfirmAdapter m_confirmAdapter;
	protected ListView m_vwLisView;
	protected Button confirmButton;
	protected Button changeButton;
	protected TextView tblName;
	protected TextView totalPaid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get intent from Main
		Intent intent = getIntent();
		m_csrfTokenPage = intent
				.getStringExtra(Main.INSTANCE_STATE_KEY_CSRF_TOKEN_PAGE);
		m_useScanner = intent.getBooleanExtra(
				NewSessionActivity.EXTRA_DATA_NAME_USE_SCANNER, false);

		Object tmpObj = intent
				.getSerializableExtra(NewSessionActivity.EXTRA_DATA_NAME_TABLE_OBJ);
		if (tmpObj != null && tmpObj instanceof TableEntity) {
			TableEntity table = (TableEntity) tmpObj;
			order.setTable(table);
		}

		Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
		if (lastNonConfigurationInstance != null
				&& lastNonConfigurationInstance instanceof OrderEntity) {
			// found our long lost order, yay!
			order = (OrderEntity) lastNonConfigurationInstance;

			Log.i(FdConfig.DEBUG_TAG, "OrderEntity has been recovered;");
		}

		initLayout();
		initListeners();

		// this method should take care of the table for us
		startCategoryList();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// we want to preserve our order information when configuration is
		// change, say.. orientation change?
		return order;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		CategoryEntity pendingCategory = null;

		if (resultCode == Activity.RESULT_OK && data != null) {
			switch (requestCode) {
			case REQUEST_CODE_TABLE:
				TableEntity table = (TableEntity) data
						.getSerializableExtra(TableListActivity.ACTIVITY_RESULT_NAME_TABLE_OBJ);
				order.setTable(table);
				startCategoryList();
				break;
			case REQUEST_CODE_CATEGORY:
				pendingCategory = (CategoryEntity) data
						.getSerializableExtra(CategoryListActivity.ACTIVITY_RESULT_NAME_CATEGORY_OBJ);
				startItemList(pendingCategory);
				break;
			case REQUEST_CODE_ITEM:
				OrderItemEntity orderItem = (OrderItemEntity) data
						.getSerializableExtra(ItemListActivity.ACTIVITY_RESULT_NAME_ORDER_ITEM_OBJ);
				order.addOrderItem(orderItem);
				startCategoryList();
				break;
			case IntentIntegrator.REQUEST_CODE:
				new ScanHelper(this, requestCode, resultCode, data,
						new Class[] { ItemEntity.class }) {

					@Override
					protected void onMatched(AbstractEntity entity) {
						order.addItem((ItemEntity) entity);
						startCategoryList();
					}

					@Override
					protected void onMismatched(AbstractEntity entity) {
						// we don't want ti fallback to onInvalid
						// because we want to let user try again :)
						startCategoryList();
					}

					@Override
					protected void onInvalid() {
						m_useScanner = false;
						startCategoryList();
					}
				};
				break;
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			// xu ly khi activity bi huy boi back
			switch (requestCode) {
			case REQUEST_CODE_TABLE:
				finish();
				break;
			case REQUEST_CODE_CATEGORY:
				startTableList();
				break;
			case REQUEST_CODE_ITEM:
				startCategoryList();
				break;
			case IntentIntegrator.REQUEST_CODE:
				m_useScanner = false;
				startConfirmList();
				break;

			}
		} else if (resultCode == CategoryListActivity.RESULT_OK_BEFORE_CONFIRM) {
			startConfirmList();
		}

		/*
		 * if (pendingCategory == null) { // no pending category, yet. Display
		 * the category list startCategoryList(); } else { // a category is
		 * pending, display the item list of that category
		 * startItemList(pendingCategory); }
		 */

	}

	protected void startTableList() {
		Intent tableIntent = new Intent(this, TableListActivity.class);
		startActivityForResult(tableIntent,
				NewSessionActivity.REQUEST_CODE_TABLE);
	}

	protected void startCategoryList() {
		if (order.getTableId() == 0) {
			// before display the category list
			// we should have a valid table set
			startTableList();
		} else if (m_useScanner) {
			startScanner();
		} else {
			Intent categoryIntent = new Intent(this, CategoryListActivity.class);
			startActivityForResult(categoryIntent,
					NewSessionActivity.REQUEST_CODE_CATEGORY);
		}
	}

	protected void startItemList(CategoryEntity category) {
		Intent itemIntent = new Intent(this, ItemListActivity.class);
		itemIntent.putExtra(ItemListActivity.EXTRA_DATA_NAME_CATEGORY_ID,
				category.categoryId);
		startActivityForResult(itemIntent, NewSessionActivity.REQUEST_CODE_ITEM);
	}

	protected void startScanner() {
		IntentIntegrator.initiateScan(this);
	}

	protected void startConfirmList() {
		m_confirmAdapter.notifyDataSetChanged();
		confirmButton.setText(NewSessionActivity.POST_ORDER_STRING);
		changeButton.setText(NewSessionActivity.CHANGE_ORDER_STRING);
		tblName.setText(order.getTableName());
		totalPaid.setText(String.format("%s", order.getPriceTotal()));
		registerForContextMenu(m_vwLisView);
		m_vwLisView.setOnItemLongClickListener(m_confirmAdapter);
	}

	/*
	 * Cai dat danh cho confirm list Bao gom cac thiet lap lay out, listener va
	 * ham post du lieu order toi server
	 */
	public void initLayout() {
		setContentView(R.layout.activity_confirm);
		m_vwLisView = (ListView) findViewById(R.id.m_vwListView);
		confirmButton = (Button) findViewById(R.id.confirmButton);
		changeButton = (Button) findViewById(R.id.changeButton);
		tblName = (TextView) findViewById(R.id.tblName);
		totalPaid = (TextView) findViewById(R.id.totalPaid);

		m_confirmAdapter = new ConfirmAdapter(this, order);
		m_vwLisView.setAdapter(m_confirmAdapter);

	}

	public void initListeners() {
		confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				postOrder();
				NewSessionActivity.this.finish();
			}
		});

		changeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startCategoryList();
			}
		});

		/*
		 * m_vwLisView.setOnLongClickListener(new OnLongClickListener() {
		 * 
		 * @Override public boolean onLongClick(View v) { if (v instanceof
		 * ConfirmView) {
		 * 
		 * showDialog(ItemListActivity.DIALOG_QUANTITY_SELECTOR); } return true;
		 * } });
		 */
	}

	public void postOrder() {
		new AsyncTask<Void, Void, JSONObject>() {
			@Override
			protected JSONObject doInBackground(Void... Void) {
				String orderUrl = UriStringHelper.buildUriString("new-order");
				List<NameValuePair> params = order.getOrderAsParams();
				JSONObject response = HttpHelper.post(NewSessionActivity.this,
						orderUrl, m_csrfTokenPage, params);
				return response;
			}

			@Override
			protected void onPostExecute(JSONObject jsonObject) {
				// TODO
			}
		}.execute();
	}

	/*
	 * thuc hien khi nut Back duoc nhan chuyen tro ve CategoryList de tiep tuc
	 * chon
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			startCategoryList();
			return true;
		}
		return false;
	}

	/*
	 * Tao menu xoa di order trong confirmList
	 */
	/*
	 * @Override public void onCreateContextMenu(ContextMenu menu, View v,
	 * ContextMenuInfo menuInfo) { MenuItem removeMenu = menu.add(Menu.NONE,
	 * NewSessionActivity.REMOVE_ITEM_MENU, Menu.NONE,
	 * NewSessionActivity.REMOVE_ITEM_MENU_STRING);
	 * removeMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
	 * 
	 * @Override public boolean onMenuItemClick(MenuItem item) { int
	 * selectedPosition = m_confirmAdapter.getSelectedPosition();
	 * order.removeOrderItem(selectedPosition, 1);
	 * m_confirmAdapter.notifyDataSetChanged(); return true;
	 * 
	 * } }); }
	 */

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		showDialog(ItemListActivity.DIALOG_QUANTITY_SELECTOR);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case ItemListActivity.DIALOG_QUANTITY_SELECTOR:
			dialog = new QuantityRemoverDialog(this);
			dialog.setOnDismissListener(this);
			break;
		}

		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case ItemListActivity.DIALOG_QUANTITY_SELECTOR:

			((QuantityRemoverDialog) dialog)
					.setDialogText("Quantity to change");
			break;
		}
	}

	@Override
	public void onDismiss(DialogInterface arg0) {
		if (arg0 instanceof QuantityRemoverDialog) {
			int selectedPosition = m_confirmAdapter.getSelectedPosition();
			order.removeOrderItem(selectedPosition,
					((QuantityRemoverDialog) arg0).getQuantity());
			m_confirmAdapter.notifyDataSetChanged();

		}
	}

}

