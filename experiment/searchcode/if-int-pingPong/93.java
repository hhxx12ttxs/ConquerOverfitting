package com.pingpong.android.modules.friend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pingpong.android.R;
import com.pingpong.android.model.Hall;
import com.pingpong.android.utils.Utils;

import java.util.List;

/**
 * Created by JiangZhenJie on 2015/3/23.
 */
public class FriendHallAdapter extends BaseAdapter {

    private Context mContext;
    private List<Hall> mHalls;
    private int mHallType;

    public FriendHallAdapter(Context context) {
        mContext = context;
    }

    public void setHallType(int type){
        this.mHallType = type;
    }

    public Hall getHallInPosition(int position){
        if(mHalls == null) return null;
        return mHalls.get(position);
    }

    @Override
    public int getCount() {
        return mHalls == null ? 0 : mHalls.size();
    }

    @Override
    public Object getItem(int position) {
        return mHalls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_hall_item, parent, false);
            holder = new ViewHolder();
            holder.hallName = (TextView) convertView.findViewById(R.id.tv_hall_name);
            holder.hallAddress = (TextView) convertView.findViewById(R.id.tv_hall_address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Hall hall = (Hall) getItem(position);
        holder.hallName.setText(hall.getHallName());
        if(mHallType == FriendHallFragment.TYPE_NEARBY_HALL){
            holder.hallAddress.setText(Utils.formatDistance(hall.getHallDistance()));
        }else{
            holder.hallAddress.setText(hall.getHallAddress());
        }

        return convertView;
    }

    public void setHalls(List<Hall> halls) {
        this.mHalls = halls;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView hallName;
        TextView hallAddress;
    }


}

