package com.pingpong.android.modules.hall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.common.ImageUploadUtils;
import com.pingpong.android.model.DeleteImageModel;
import com.pingpong.android.model.Game;
import com.pingpong.android.model.Hall;
import com.pingpong.android.model.Moment;
import com.pingpong.android.model.ShowGameModel;
import com.pingpong.android.model.ShowHallModel;
import com.pingpong.android.model.ShowMomentModel;
import com.pingpong.android.modules.common.PreviewImageActivity;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.Utils;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import static android.view.View.OnClickListener;

public class HallShowDetailActivity extends NetBaseActivity implements OnClickListener {

    private static final int REQUEST_CHOOSE_IMAGE = 123;

    private TextView mHallFans;
    private TextView mHallAddress;
    private LinearLayout mChargeLayout;
    private LinearLayout mParkLayout;
    private LinearLayout mHallPhoneLayout;
    private LinearLayout mNewestGameLayout;
    private LinearLayout mNewesetMomentLayout;
    private TextView mNewestGame;
    private TextView mNewestMoment;
    private GridView mHallImageGrid;

    private Hall mHall;
    private Moment mMoment;
    private Game mGame;
    public boolean mIsSelf = false;

    private String mLongClickImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_show_detail);
//        reLoginIfNeeded();
        initView();
        getExtra();
        startRequestData();
    }

    /**
     * 得到其他Activity传进的数据<br/>
     * 优先级为：<br/>
     * 1. Hall实例 <br/>
     * 2. HallId <br/>
     * 3. 无 <br/>
     */
    private void getExtra() {
        Hall hall = getIntent().getParcelableExtra("hall");
        if (hall == null) {
            int hallId = getIntent().getIntExtra("hall_id", -1);
            if (hallId == -1) {
                updateView(DataManager.getInstance().getLoginHall());  // 显示登录球馆
            } else {
                mHall = new Hall();
                mHall.setHallId(hallId);
                sendHttpRequest(Constants.RequestId.ID_SHOW_HALL, R.string.common_loading_progress);
            }
        } else {
            updateView(hall);
        }
    }

    private void initView() {
        mHallFans = (TextView) findViewById(R.id.tv_hall_fans);
        mHallAddress = (TextView) findViewById(R.id.tv_hall_address);
        mChargeLayout = (LinearLayout) findViewById(R.id.ll_charge_layout);
        mParkLayout = (LinearLayout) findViewById(R.id.ll_park_layout);
        mHallPhoneLayout = (LinearLayout) findViewById(R.id.ll_hall_phone_layout);
        mNewesetMomentLayout = (LinearLayout) findViewById(R.id.ll_newest_moment_layout);
        mNewesetMomentLayout.setOnClickListener(this);
        mNewestGameLayout = (LinearLayout) findViewById(R.id.ll_newest_game_layout);
        mNewestGameLayout.setOnClickListener(this);
        mNewestGame = (TextView) findViewById(R.id.tv_newest_game);
        mNewestMoment = (TextView) findViewById(R.id.tv_newest_moment);
        mHallImageGrid = (GridView) findViewById(R.id.gv_hall_images);
        mHallImageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HallImageAdapter adapter = (HallImageAdapter) mHallImageGrid.getAdapter();
                String url = adapter.getImageUrl(position);
                if (url.equals(Constants.URL_LOCAL_ADD_IMAGE)) {
                    redirectToSelect();
                } else {
                    Intent intent = new Intent(HallShowDetailActivity.this, PreviewImageActivity.class);
                    intent.putExtra("image_url", url);
                    startActivity(intent);
                }
            }
        });

        mHallImageGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HallImageAdapter adapter = (HallImageAdapter) mHallImageGrid.getAdapter();
                String url = adapter.getImageUrl(position);
                if (!url.equals(Constants.URL_LOCAL_ADD_IMAGE)) {
                    mLongClickImageUrl = url;
                    showDeleteConfirmDialog();
                    return true;
                }
                return false;
            }
        });

        mHallImageGrid.setAdapter(new HallImageAdapter(this));
    }

    private void startRequestData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendHttpRequest(Constants.RequestId.ID_SHOW_MOMENT);
                sendHttpRequest(Constants.RequestId.ID_SHOW_GAME);
            }
        }).start();
    }

    private void updateView(Hall hall) {
        if (hall == null) return;
        mHall = hall;
        mIsSelf = DataManager.getInstance().getLoginType() == Constants.TYPE_HALL &&
                mHall.getHallId() == DataManager.getInstance().getLoginUserId();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mHall.getHallName() + " （ID： " + mHall.getHallId() + "）");
        mHallFans.setText(mHall.getHallFans() + "");
        mHallAddress.setText(mHall.getHallAddress());
        HallImageAdapter adapter = (HallImageAdapter) mHallImageGrid.getAdapter();
        adapter.setImageUrls(mHall.getHallImages());
    }

    private void updateView(ShowMomentModel model) {
        if (model == null || model.getMoments() == null || model.getMoments().size() == 0) {
            mNewestMoment.setText(R.string.newest_moment_null);
            return;
        }
        mMoment = model.getMoments().get(0);
        if (TextUtils.isEmpty(mMoment.getMomentContent())) {
            mNewestMoment.setText(R.string.newest_moment_null);
        } else {
            mNewestMoment.setText(mMoment.getMomentContent());
        }
    }

    private void updateView(ShowGameModel model) {
        if (model == null || model.getGames() == null || model.getGames().size() == 0) {
            mNewestGame.setText(R.string.newest_game_null);
            return;
        }
        mGame = model.getGames().get(0);
        if (TextUtils.isEmpty(mGame.getGameName())) {
            mNewestGame.setText(R.string.newest_game_null);
        } else {
            mNewestGame.setText(mGame.getGameName());
        }
    }


    public void viewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_charge_layout:
                showChargeMessage(mHall.getHallCharges());
                break;
            case R.id.ll_park_layout:
                showParkMessage(mHall.getHallPark());
                break;
            case R.id.ll_hall_phone_layout:
                showPhoneMessage(mHall.getHallPhone());
                break;
        }
    }

    private void showChargeMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.charge_way);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void showPhoneMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.hall_phone);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.call, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mHall.getHallPhone()));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void showParkMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.park_information);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void redirectToSelect() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
    }

    private void uploadImage(String path) {
        ImageUploadUtils.upload(this, path, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                L.i(response.toString());
                try {
                    int rst = response.getInt("resultCode");
                    if (rst == Constants.ResultCode.RESULT_OK) {
                        Toast.makeText(HallShowDetailActivity.this, R.string.upload_image_success, Toast.LENGTH_SHORT).show();
                        uploadImageSuccess(response.getString("imageUrl"));
                    } else {
                        Toast.makeText(HallShowDetailActivity.this, R.string.upload_image_fail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(HallShowDetailActivity.this, R.string.upload_image_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageSuccess(String url) {
        HallImageAdapter adapter = (HallImageAdapter) mHallImageGrid.getAdapter();
        adapter.addImageUrl(url);
    }

    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_confirm);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendHttpRequest(Constants.RequestId.ID_DELETE_IMAGE, R.string.common_loading_progress);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hall_show_detail, menu);
        if (mIsSelf) {
            menu.removeItem(R.id.action_associate);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_associate:
                sendHttpRequest(Constants.RequestId.ID_ASSOCIATE, R.string.common_loading_progress);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        /*userId 是指登录用户ID，hallId是指球馆ID，两者可能会不同，注意区分*/
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_HALL:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_HALL, ShowHallModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("hallId", mHall.getHallId() + "");
                break;
            case Constants.RequestId.ID_SHOW_MOMENT:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_MOMENT, ShowMomentModel.class);
                hrp.addParam("hallId", mHall.getHallId() + "");
                hrp.addParam("count", 1 + "");
                break;
            case Constants.RequestId.ID_SHOW_GAME:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_GAME, ShowGameModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("hallId", mHall.getHallId() + "");
                break;
            case Constants.RequestId.ID_DELETE_IMAGE:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_DELETE_IMAGE, DeleteImageModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("imageUrl", mLongClickImageUrl);
                break;
            case Constants.RequestId.ID_ASSOCIATE:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_ASSOCIATE, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("hallId", mHall.getHallId() + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_HALL:
                ShowHallModel hallModel = (ShowHallModel) response;
                if (hallModel != null && hallModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    if (hallModel.getHalls() != null && hallModel.getHalls().size() > 0) {
                        updateView(hallModel.getHalls().get(0));
                    }
                } else {
                    L.e("get hall info fail");
                    Toast.makeText(this, R.string.fail_to_update_hall, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_SHOW_MOMENT:
                ShowMomentModel momentModel = (ShowMomentModel) response;
                if (momentModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    L.i(momentModel.toString() + "\n" + momentModel.getMoments().toString());
                    updateView(momentModel);
                } else {
                    L.e(momentModel.getResultMessage());
                    Toast.makeText(this, R.string.fail_to_update_moment, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_SHOW_GAME:
                ShowGameModel gameModel = (ShowGameModel) response;
                if (gameModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    L.i(gameModel.toString() + "\n" + gameModel.getGames().toString());
                    updateView(gameModel);
                } else {
                    L.e(gameModel.getResultMessage());
                    Toast.makeText(this, R.string.fail_to_update_game, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_DELETE_IMAGE:
                DeleteImageModel model = (DeleteImageModel) response;

                if (model != null && model.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    HallImageAdapter adapter = (HallImageAdapter) mHallImageGrid.getAdapter();
                    adapter.remove(model.getImageUrl());
                } else {
                    Toast.makeText(this, R.string.delete_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_ASSOCIATE:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.associate_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.associate_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_newest_moment_layout:
                Intent intent = new Intent(this, HallMomentListActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_newest_game_layout:
                startActivity(new Intent(this, HallGameListActivity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CHOOSE_IMAGE:

                    Uri uri = data.getData();
                    if (uri != null) {
                        String path = Utils.getFilePathByContentResolver(this, uri);
                        if (!TextUtils.isEmpty(path)) {
                            uploadImage(path);
                        } else {
                            Toast.makeText(this, R.string.selected_image_not_found, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }
    }
}

