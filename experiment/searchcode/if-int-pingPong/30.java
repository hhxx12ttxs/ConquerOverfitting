package com.pingpong.android.modules.friend;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pingpong.android.R;
import com.pingpong.android.base.BaseActivity;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.common.ImageUploadUtils;
import com.pingpong.android.model.Friend;
import com.pingpong.android.modules.common.PreviewImageActivity;
import com.pingpong.android.utils.BitmapCache;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.Utils;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendAlbumActivity extends BaseActivity {

    private static final int REQUEST_CHOOSE_IMAGE = 123;

    private GridView mGridView;

    private Friend mFriend;
    private ImageLoader mImageLoader;

    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_album);
        mFriend = getIntent().getParcelableExtra("friend");
        if (mFriend == null) {
            mFriend = DataManager.getInstance().getLoginFriend();
        }
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (TextUtils.equals(DataManager.getInstance().getLoginUserId() + "", mFriend.getFriendId() + "")) {
            List<String> albums = mFriend.getAlbums();
            int size = albums == null ? 0 : albums.size();
            if (size <= Constants.FRIEND_MAX_IMAGE) {
                getMenuInflater().inflate(R.menu.menu_friend_album, menu);
            }
        }

        MenuItem item = menu.findItem(R.id.action_upload);
        if (item != null) {
            String title = isUploading ? "上传中..." : "上传";
            item.setTitle(title);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                redirectToSelect();
                break;
        }
        return true;
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.grid_view);
        mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(this).getRequestQueue(), BitmapCache.getInstance());
        List<String> albums = mFriend.getAlbums();
        mGridView.setAdapter(new GridAdapter(this, albums));
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = mFriend.getAlbums().get(position);
                Intent intent = new Intent(FriendAlbumActivity.this, PreviewImageActivity.class);
                intent.putExtra("image_url", url);
                startActivity(intent);
            }
        });
    }

    private void redirectToSelect() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
    }


    private void uploadImage(String path) {
        isUploading = true;
        invalidateOptionsMenu();
        ImageUploadUtils.upload(this, path, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                L.i(response.toString());
                try {
                    int rst = response.getInt("resultCode");
                    if (rst == Constants.ResultCode.RESULT_OK) {
                        Toast.makeText(FriendAlbumActivity.this, R.string.upload_image_success, Toast.LENGTH_SHORT).show();
                        uploadImageSuccess(response.getString("imageUrl"));
                    } else if (rst == Constants.ResultCode.RESULT_OVER_THRESHOLD) {
                        Toast.makeText(FriendAlbumActivity.this, R.string.image_over_threshold, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FriendAlbumActivity.this, R.string.upload_image_fail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                isUploading = false;
                invalidateOptionsMenu();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(FriendAlbumActivity.this, R.string.upload_image_fail, Toast.LENGTH_SHORT).show();
                isUploading = false;
                invalidateOptionsMenu();
            }
        });
    }

    private void uploadImageSuccess(String url) {
        List<String> albums = mFriend.getAlbums();
        if (albums == null) {
            albums = new ArrayList<>();
            mFriend.setAlbums(albums);
        }
        albums.add(url);

        albums = DataManager.getInstance().getLoginFriend().getAlbums();
        if (albums == null) {
            albums = new ArrayList<>();
            DataManager.getInstance().getLoginFriend().setAlbums(albums);
        }
        albums.add(url);

        GridAdapter adapter = (GridAdapter) mGridView.getAdapter();
        adapter.notifyDataSetChanged();
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

    private class GridAdapter extends BaseAdapter {

        private Context mContext;
        private List<String> mItems;

        private int size;

        public GridAdapter(Context context, List<String> items) {
            this.mContext = context;
            this.mItems = items;

            int screenWidth = Utils.getScreenWidth(context);
            screenWidth = screenWidth - Utils.dip2px(context, 3) * 3 - Utils.dip2px(context, 10) * 2;
            size = screenWidth / 4;
        }

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_album_grid_item, parent, false);
                convertView.getLayoutParams().width = size;
                convertView.getLayoutParams().height = size;
                viewHolder = new ViewHolder();
                viewHolder.networkImageView = (NetworkImageView) convertView.findViewById(R.id.grid_network_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.networkImageView.setDefaultImageResId(R.drawable.empty_picture);
            viewHolder.networkImageView.setImageUrl(mItems.get(position), mImageLoader);

            return convertView;
        }
    }

    private class ViewHolder {
        public NetworkImageView networkImageView;
    }

}

