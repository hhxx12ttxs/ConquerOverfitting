package com.pingpong.android.modules.hall;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.PublishGameModel;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;

public class HallPublishGameActivity extends NetBaseActivity {

    private static final int MAX_LENGTH_GAME_NAME = 128;
    private static final int MAX_LENGTH_GAME_EXPLAIN = 512;
    private static final int MAX_LENGTH_GAME_PLAN = 512;
    private static final int MAX_LENGTH_GAME_AWARD = 512;
    private static final int MAX_LENGTH_GAME_APPLY = 512;

    private EditText mGameName;
    private EditText mGameExplain;
    private EditText mGamePlan;
    private EditText mGameAward;
    private EditText mGameApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_publish_game);
        initView();
    }

    private void initView() {
        mGameName = (EditText) findViewById(R.id.et_game_name);
        mGameExplain = (EditText) findViewById(R.id.et_game_explain);
        mGamePlan = (EditText) findViewById(R.id.et_game_plan);
        mGameAward = (EditText) findViewById(R.id.et_game_award);
        mGameApply = (EditText) findViewById(R.id.et_game_apply);
    }

    private boolean checkData() {
        String name = mGameName.getText().toString();
        String explain = mGameExplain.getText().toString();
        String plan = mGamePlan.getText().toString();
        String award = mGameAward.getText().toString();
        String apply = mGameApply.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "赛事名称不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (name.length() > MAX_LENGTH_GAME_NAME) {
            Toast.makeText(this, "赛事名称不能超过128字！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(explain)) {
            Toast.makeText(this, "赛事说明不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (explain.length() > MAX_LENGTH_GAME_EXPLAIN) {
            Toast.makeText(this, "赛事说明不能超过512字！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(plan)) {
            Toast.makeText(this, "赛事安排不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (plan.length() > MAX_LENGTH_GAME_PLAN) {
            Toast.makeText(this, "赛事安排不能超过512字！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(award)) {
            Toast.makeText(this, "赛事奖励不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (award.length() > MAX_LENGTH_GAME_AWARD) {
            Toast.makeText(this, "赛事奖励不能超过512字！", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (TextUtils.isEmpty(apply)) {
            Toast.makeText(this, "报名方式不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (apply.length() > MAX_LENGTH_GAME_APPLY) {
            Toast.makeText(this, "报名方式不能超过512字！", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void publishGame() {
        sendHttpRequest(Constants.RequestId.ID_PUBLISH_GAME,R.string.common_loading_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hall_publish_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_publish_game:
                if (checkData()) {
                    publishGame();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_PUBLISH_GAME:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_PUBLISH_GAME, PublishGameModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("gameName", mGameName.getText().toString());
                hrp.addParam("gameExplain", mGameExplain.getText().toString());
                hrp.addParam("gamePlan", mGamePlan.getText().toString());
                hrp.addParam("gameAward", mGameAward.getText().toString());
                hrp.addParam("gameApply", mGameApply.getText().toString());
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_PUBLISH_GAME:
                PublishGameModel gameModel = (PublishGameModel) response;
                if (gameModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, "赛事发布成功！", Toast.LENGTH_SHORT).show();
                    this.finish();
                } else {
                    Toast.makeText(this, "赛事发布失败！", Toast.LENGTH_SHORT).show();
                    L.e(response.getResultMessage());
                }
                break;
        }
    }
}

