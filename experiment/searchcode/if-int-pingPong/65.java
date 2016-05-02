package com.pingpong.android.modules.hall;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.common.SettingActivity;
import com.umeng.update.UmengUpdateAgent;

public class HallMainActivity extends NetBaseActivity {

    private TextView mPublishMoment;
    private TextView mPublishGame;
    private TextView mShowDetail;
    private TextView mModifyDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_main);
        reLoginIfNeeded();
        DataManager.getInstance().setMainActivity(this);
        initView();

        UmengUpdateAgent.update(this);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
    }

    private void initView() {
        mPublishMoment = (TextView) findViewById(R.id.tv_publish_moment);
        mPublishGame = (TextView) findViewById(R.id.tv_publish_game);
        mShowDetail = (TextView) findViewById(R.id.tv_show_detail);
        mModifyDetail = (TextView) findViewById(R.id.tv_modify_detail);
    }

    public void viewClicked(View view) {
        Intent intent = new Intent();
        Class clazz = null;
        switch (view.getId()) {
            case R.id.tv_publish_game:
                clazz = HallPublishGameActivity.class;
                break;
            case R.id.tv_publish_moment:
                clazz = HallPublishMomentActivity.class;
                break;
            case R.id.tv_show_detail:
                clazz = HallShowDetailActivity.class;
                break;
            case R.id.tv_modify_detail:
                clazz = HallModifyDetailActivity.class;
                break;
        }
        if (clazz != null) {
            intent.setClass(this, clazz);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hall_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        return null;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {

    }
}

