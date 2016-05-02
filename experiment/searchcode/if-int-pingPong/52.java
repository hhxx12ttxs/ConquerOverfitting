package com.pingpong.android.modules.friend;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.common.CityListActivity;
import com.pingpong.android.model.Game;
import com.pingpong.android.model.ShowGameModel;
import com.pingpong.android.modules.common.GameAdapter;
import com.pingpong.android.utils.Constants;

import java.util.List;

public class FriendAreaGameActivity extends NetBaseActivity {

    private ListView mGameListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_area_game);
        DataManager.getInstance().finishAllActivityInList();
        initView();
        sendHttpRequest(Constants.RequestId.ID_SHOW_GAME, R.string.common_loading_progress);
    }

    private void initView() {
        mGameListView = (ListView) findViewById(R.id.lv_game);
        mGameListView.setAdapter(new GameAdapter(this));
        String title = DataManager.getInstance().getSelectedCityString().replace(",","");
        mActionBar.setTitle(title+"赛事");
    }

    private void updateUI(List<Game> games){
        if (games == null) return;
        GameAdapter adapter = (GameAdapter) mGameListView.getAdapter();
        adapter.setGames(games);
        adapter.notifyDataSetChanged();
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_GAME:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_GAME, ShowGameModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId()+"");
                hrp.addParam("area",DataManager.getInstance().getSelectedCityString());
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_GAME:
                ShowGameModel model = (ShowGameModel) response;
                if (model != null && model.getResultCode() == Constants.ResultCode.RESULT_OK){
                    updateUI(model.getGames());
                }
                break;
        }
    }
}

