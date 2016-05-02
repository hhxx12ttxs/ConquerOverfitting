package com.pingpong.android.modules.friend;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pingpong.android.R;
import com.pingpong.android.common.CityListActivity;

public class FriendHallFragment extends Fragment {

    private static final int TYPE_TEXT_RES_ID[] = {R.string.my_association_hall,
            R.string.nearby_hall,
            R.string.find_hall,
            R.string.search_hall};

    public static final int TYPE_MY_ASSOCIATE_HALL = 10;
    public static final int TYPE_NEARBY_HALL = 11;
    public static final int TYPE_FIND_HALL = 12;
    public static final int TYPE_SEARCH_HALL = 13;
    private static final int TYPES[] = {TYPE_MY_ASSOCIATE_HALL, TYPE_NEARBY_HALL, TYPE_FIND_HALL, TYPE_SEARCH_HALL};

    private ListView mHallTypeList;
    private boolean mIsChooseHall;

    public FriendHallFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_hall, container, false);
        mHallTypeList = (ListView) view.findViewById(R.id.lv_friend_hall_type_list);
        mHallTypeList.setAdapter(new FriendHallTypeAdapter(getActivity()));
        mHallTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 2) {   //查找球馆
                    Intent intent = new Intent(getActivity(), CityListActivity.class);
                    CityListActivity.toClass = FriendHallListActivity.class.getCanonicalName();
                    CityListActivity.isSelected = mIsChooseHall;
                    getActivity().startActivityForResult(intent, 0);
                } else {  // 我的关联球馆，附近球馆，搜索球馆
                    Intent intent = new Intent(getActivity(), FriendHallListActivity.class);
                    intent.putExtra(FriendHallListActivity.CHOOSE_FLAG, mIsChooseHall);
                    intent.putExtra("hall_type", TYPES[position]);
                    getActivity().startActivityForResult(intent, 0);
                }
            }
        });
        return view;
    }

    public void setChooseHallEnable() {
        mIsChooseHall = true;
    }

    private class FriendHallTypeAdapter extends BaseAdapter {

        private Context mContext;

        public FriendHallTypeAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return TYPE_TEXT_RES_ID.length;
        }

        @Override
        public Object getItem(int position) {
            return TYPE_TEXT_RES_ID[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_hall_type_item, parent, false);
                holder = new ViewHolder();
                holder.typeText = (TextView) convertView.findViewById(R.id.tv_friend_hall_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            int resId = Integer.parseInt(getItem(position).toString());
            String text = mContext.getString(resId);
            holder.typeText.setText(text);
            return convertView;
        }

        private class ViewHolder {
            TextView typeText;
        }
    }
}

