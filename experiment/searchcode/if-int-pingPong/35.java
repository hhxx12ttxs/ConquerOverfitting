package com.pingpong.android.modules.hall;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.Moment;
import com.pingpong.android.model.ShowMomentModel;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.Utils;

import java.util.List;

public class HallMomentListActivity extends NetBaseActivity {

    private ListView mMomentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_moment_list);
        initView();
        startRequestData();
    }

    private void initView() {
        mMomentList = (ListView) findViewById(R.id.lv_hall_moment_list);
        mMomentList.setAdapter(new MomentAdapter(this));
    }

    // TODO : 增加本地缓存
    private void startRequestData() {
        sendHttpRequest(Constants.RequestId.ID_SHOW_MOMENT);
    }

    private void updateView(ShowMomentModel momentModel) {
        if (momentModel == null || momentModel.getMoments() == null) return;
        MomentAdapter adapter = (MomentAdapter) mMomentList.getAdapter();
        adapter.setMoments(momentModel.getMoments());
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_MOMENT:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_MOMENT, ShowMomentModel.class);
                hrp.addParam("hallId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("count", 50 + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_MOMENT:
                ShowMomentModel momentModel = (ShowMomentModel) response;
                if (momentModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    L.i(momentModel.getResultMessage() + "\n" + momentModel.getMoments().toString());
                    updateView(momentModel);
                } else {
                    L.e(momentModel.getResultMessage());
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private class MomentAdapter extends BaseAdapter {

        private Context mContext;
        private List<Moment> mMoments;

        public MomentAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mMoments == null ? 0 : mMoments.size();
        }

        @Override
        public Object getItem(int position) {
            return mMoments.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.hall_moment_item, parent, false);
                holder = new ViewHolder();
                holder.mMomentContent = (TextView) convertView.findViewById(R.id.tv_moment_content);
                holder.mMomentTime = (TextView) convertView.findViewById(R.id.tv_moment_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Moment moment = (Moment) getItem(position);
            if (moment != null) {
                holder.mMomentContent.setText(moment.getMomentContent());
                L.d(moment.getMomentTime());
                holder.mMomentTime.setText(Utils.formatTime(moment.getMomentTime()));
            }
            return convertView;
        }

        public void setMoments(List<Moment> moments) {
            this.mMoments = moments;
            notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView mMomentContent;
            TextView mMomentTime;
        }
    }

}

