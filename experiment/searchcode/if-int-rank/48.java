package leoliang.unitpricecompare;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leoliang.unitpricecompare.model.PriceRanker;
import leoliang.unitpricecompare.model.PriceRanker.UncomparableUnitException;
import leoliang.unitpricecompare.model.ShoppingItem;
import leoliang.util.Analytics;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class MainActivity extends BaseActivity {

    public class ItemList extends BaseAdapter {

        private final String[] rankName = { "Best Price", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th" };
        private final LayoutInflater inflater;
        private List<ShoppingItem> items = new ArrayList<ShoppingItem>();
        private PriceRanker ranker = new PriceRanker();
        private NumberFormat ratioNumberFormat;

        public ItemList(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ratioNumberFormat = NumberFormat.getPercentInstance();
            ratioNumberFormat.setMaximumFractionDigits(0);
        }

        public void addItem(ShoppingItem item) {
            setItem(-1, item);
        }

        public void clear() {
            Map<String, String> eventParameters = new HashMap<String, String>();
            eventParameters.put("numberOfItems", String.valueOf(getCount()));
            Analytics.onEvent("allItemsCleared", eventParameters);
            items.clear();
            notifyItemUpdated();
        }

        public void deleteItem(int index) {
            Analytics.onEvent("itemDeleted");
            items.remove(index);
            notifyItemUpdated();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewgroup) {

            // TODO: use convertView to avoid inflate every time
            View view = inflater.inflate(R.layout.item_view, null);

            // TODO: use ViewHolder to avoid findViewById every time
            TextView priceView = (TextView) view.findViewById(R.id.price);
            TextView unitPriceView = (TextView) view.findViewById(R.id.unitPrice);
            TextView rankView = (TextView) view.findViewById(R.id.rank);
            TextView quantityView = (TextView) view.findViewById(R.id.quantity);
            TextView ratioView = (TextView) view.findViewById(R.id.ratio);
            CheckBox enabledBox = (CheckBox) view.findViewById(R.id.enabled);

            final ShoppingItem item = items.get(i);

            NumberFormat format = NumberFormat.getCurrencyInstance();
            priceView.setText(format.format(item.getPrice()));
            quantityView.setText(item.getQuantity().toString());
            enabledBox.setChecked(item.isEnabled());
            unitPriceView.setText(format.format(item.getPricePerUnit()) + "/" + item.getQuantity().getUnitName());

            if (item.isEnabled()) {
                try {
                    int rank = ranker.getRank(item);
                    if ((rank > 0) && (rank <= rankName.length)) {
                        rankView.setText(rankName[rank - 1]);
                        if (rank <= 3) {
                            rankView.setTextColor(Color.rgb(0, 153, 0)); // dark green
                        }
                    }
                    if (rank > 1) {
                        ratioView.setText(" +" + ratioNumberFormat.format(ranker.getRatioToBestPrice(item) - 1) + " ");
                    }
                } catch (UncomparableUnitException e) {
                    rankView.setText(R.string.uncomparable);
                    rankView.setTextColor(Color.RED);
                }
            } else {
                priceView.setEnabled(false);
            }

            enabledBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(@SuppressWarnings("unused") CompoundButton button, boolean isChecked) {
                    item.setEnabled(isChecked);
                    notifyItemUpdated();
                    if (isChecked) {
                        Analytics.onEvent("itemEnabled");
                    } else {
                        Analytics.onEvent("itemDisabled");
                    }
                }
            });

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(@SuppressWarnings("unused") View paramView) {
                    startEditItemActivity(i);
                }
            });

            view.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
                    menu.add(Menu.NONE, i, MENU_DELETE, getString(R.string.delete_item));
                }
            });

            return view;
        }

        public void notifyItemUpdated() {
            ranker.rank(items);
            notifyDataSetChanged();
            updateHint();
        }

        public void setItem(int index, ShoppingItem item) {
            Map<String, String> eventParameters = new HashMap<String, String>();
            eventParameters.put("unitOfItem", item.getQuantity().getUnitName());
            eventParameters.put("numberOfItems", String.valueOf(getCount()));
            if (index < 0) {
                FlurryAgent.onEvent("itemCreated", eventParameters);
                items.add(item);
            } else {
                FlurryAgent.onEvent("itemUpdated", eventParameters);
                items.set(index, item);
            }
            notifyItemUpdated();
        }
    }

    private static final String PREF_SHOPPING_ITEMS = "shoppingItems";

    private static final int ACTION_CREATE = 45341;
    private static final int ACTION_EDIT = 45342;

    private static final int MENU_DELETE = 1;

    private ItemList itemList;

    private void hideHint() {
        TextView hint = (TextView) findViewById(R.id.AddItemsHintText);
        hint.setVisibility(View.GONE);
    }

    private void loadShoppingItems(ItemList itemList) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String json = preferences.getString(PREF_SHOPPING_ITEMS, null);
        if (json == null) {
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                ShoppingItem shoppingItem = new ShoppingItem(jsonArray.getJSONObject(i));
                itemList.addItem(shoppingItem);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to deserialize shopping items", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ShoppingItem item = (ShoppingItem) data.getSerializableExtra(ShoppingItemActivity.EXTRA_SHOPPING_ITEM);
            switch (requestCode) {
            case ACTION_CREATE:
                itemList.addItem(item);
                break;
            case ACTION_EDIT:
                itemList.setItem(data.getIntExtra(ShoppingItemActivity.EXTRA_SHOPPING_ITEM_INDEX, -1), item);
                break;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getOrder()) {
        case MENU_DELETE:
            itemList.deleteItem(item.getItemId());
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        itemList = new ItemList(this);
        loadShoppingItems(itemList);

        ListView listView = (ListView) findViewById(R.id.ShoppingItemList);
        listView.setAdapter(itemList);
        registerForContextMenu(listView);

        Button addButton = (Button) findViewById(R.id.AddItemButton);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@SuppressWarnings("unused") View view) {
                Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
                startActivityForResult(intent, ACTION_CREATE);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.ClearAllItems:
            itemList.clear();
            return true;
        case R.id.help:
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveShoppingItems();
    }

    private void saveShoppingItems() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        JSONArray items = new JSONArray();
        for (int index = 0; index < itemList.getCount(); index++) {
            try {
                items.put(((ShoppingItem) itemList.getItem(index)).toJson());
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to serialize a ShoppingItem to JSON", e);
            }
        }
        preferences.edit().putString(PREF_SHOPPING_ITEMS, items.toString()).commit();
    }

    private void showHint() {
        TextView hint = (TextView) findViewById(R.id.AddItemsHintText);
        hint.setVisibility(View.VISIBLE);
    }
    
    private void startEditItemActivity(int itemIndex) {
        ShoppingItem shoppingItem = (ShoppingItem) itemList.getItem(itemIndex);
        Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
        intent.putExtra(ShoppingItemActivity.EXTRA_SHOPPING_ITEM, shoppingItem);
        intent.putExtra(ShoppingItemActivity.EXTRA_SHOPPING_ITEM_INDEX, itemIndex);
        startActivityForResult(intent, ACTION_EDIT);
    }

    private void updateHint() {
        if (itemList.getCount() < 2) {
            showHint();
        } else {
            hideHint();
        }
    }

}
