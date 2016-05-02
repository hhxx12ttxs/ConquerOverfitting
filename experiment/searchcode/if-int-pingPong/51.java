package com.pingpong.android.modules.friend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class FriendGameActivity extends NetBaseActivity {

    private ListView mGameListView;
    private TextView mAreaGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_game);
        initView();
        sendHttpRequest(Constants.RequestId.ID_SHOW_GAME, R.string.common_loading_progress);
    }

    private void initView() {
        mGameListView = (ListView) findViewById(R.id.lv_game);
        mGameListView.setAdapter(new GameAdapter(this));
        mAreaGameView = (TextView) findViewById(R.id.tv_area_game);
        mAreaGameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendGameActivity.this, CityListActivity.class);
                CityListActivity.toClass = FriendAreaGameActivity.class.getCanonicalName();
                startActivity(intent);
            }
        });
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
                }else{
                    Toast.makeText(this,R.string.request_data_fail,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

