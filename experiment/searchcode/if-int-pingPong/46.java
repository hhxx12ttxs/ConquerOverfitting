package com.pingpong.android.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseActivity;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.db.CityDao;
import com.pingpong.android.db.DataBaseHelper;
import com.pingpong.android.modules.friend.FriendHallFragment;
import com.pingpong.android.modules.friend.FriendHallListActivity;

import java.util.List;

public class CityListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final int MAX_LEVEL = 3;

    private ListView mCityListView;
    private long parentId = 0;
    private int level = 1;

    /**
     * CityListActivity会复用，因此使用static
     */
    public static String toClass;
    public static boolean isSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        initView();
        DataManager.getInstance().addActivityToList(this);
    }

    private void initView() {
        mCityListView = (ListView) findViewById(R.id.lv_city);
        CityAdapter adapter = new CityAdapter();
        mCityListView.setAdapter(adapter);
        mCityListView.setOnItemClickListener(this);
        level = getIntent().getIntExtra("level", 1);
        parentId = getIntent().getLongExtra("parentId", 0);
        String name = getIntent().getStringExtra("name");
        if (TextUtils.isEmpty(name)) {
            name = "请选择";
        }
        mActionBar.setTitle(name);
        adapter.setCities(DataBaseHelper.getCityByParentIdAndLevel(parentId, level));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CityAdapter adapter = (CityAdapter) mCityListView.getAdapter();
        CityDao city = adapter.getCityInPosition(position);
        DataManager.getInstance().addSelectedCityInLevel(city, level);
        if (level < MAX_LEVEL) {
            parentId = city.getId();
            Intent intent = new Intent(this, CityListActivity.class);
            intent.putExtra("level", level + 1);
            intent.putExtra("parentId", parentId);
            intent.putExtra("name", city.name);
            startActivityForResult(intent, 0);
        } else {
            /**
             * 这里的跳转目前有三种情况：
             * 1）分区赛事，跳转到分区赛事界面（FriendAreaGameActivity）。
             * 2）查找球馆，跳转到FriendHallListActivity，选中后跳转到球馆名片页。
             * 3）约球选择球馆页，跳转到FriendHallListActivity，选中后拿到当前选中的球馆返回。
             */
            Intent intent;
            try {
                intent = new Intent(this, Class.forName(toClass));
                if (TextUtils.equals(toClass, FriendHallListActivity.class.getCanonicalName())) {
                    intent.putExtra("hall_type", FriendHallFragment.TYPE_FIND_HALL);
                    intent.putExtra("Choose_Flag", isSelected);
                }
                startActivityForResult(intent, 0);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private class CityAdapter extends BaseAdapter {

        private List<CityDao> cities;

        public void setCities(List<CityDao> cities) {
            this.cities = cities;
        }

        public List<CityDao> getCities() {
            return cities;
        }

        public CityDao getCityInPosition(int position) {
            if (cities == null) return null;
            return cities.get(position);
        }

        @Override
        public int getCount() {
            return cities == null ? 0 : cities.size();
        }

        @Override
        public Object getItem(int position) {
            return cities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(CityListActivity.this).inflate(R.layout.city_item, parent, false);
                holder = new ViewHolder();
                holder.mCityNameView = (TextView) convertView.findViewById(R.id.tv_city_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CityDao city = cities.get(position);
            holder.mCityNameView.setText(city.name);

            return convertView;
        }

        private class ViewHolder {
            TextView mCityNameView;
        }
    }
}

