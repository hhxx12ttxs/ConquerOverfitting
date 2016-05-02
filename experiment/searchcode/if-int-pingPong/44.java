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
import com.pingpong.android.model.Game;
import com.pingpong.android.model.ShowGameModel;
import com.pingpong.android.modules.common.GameAdapter;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.Utils;

import java.util.List;

public class HallGameListActivity extends NetBaseActivity {

    private ListView mGameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_game_list);
        initView();
        startRequestData();
    }

    private void initView() {
        mGameList = (ListView) findViewById(R.id.lv_hall_game_list);
        mGameList.setAdapter(new GameAdapter(this));
    }

    // TODO : 增加本地缓存
    private void startRequestData() {
        sendHttpRequest(Constants.RequestId.ID_SHOW_GAME);
    }

    private void updateView(ShowGameModel gameModel) {
        if (gameModel == null || gameModel.getGames() == null) return;
        GameAdapter adapter = (GameAdapter) mGameList.getAdapter();
        adapter.setGames(gameModel.getGames());
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_GAME:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_GAME, ShowGameModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("hallId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("count", 50 + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_GAME:
                ShowGameModel gameModel = (ShowGameModel) response;
                if (gameModel == null) return;
                if (gameModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    L.i(gameModel.getResultMessage() + "\n" + gameModel.getGames().toString());
                    updateView(gameModel);
                } else {
                    L.e(gameModel.getResultMessage());
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

