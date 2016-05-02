package com.pingpong.android.modules.hall;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pingpong.android.R;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.utils.BitmapCache;
import com.pingpong.android.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangZhenJie on 2015/3/21.
 */
public class HallImageAdapter extends BaseAdapter {

    private HallShowDetailActivity mActivity;
    private ImageLoader mImageLoader;
    private List<String> mImageUrls;

    public HallImageAdapter(HallShowDetailActivity ac) {
        mActivity = ac;
        mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(mActivity).getRequestQueue(), BitmapCache.getInstance());
    }

    @Override
    public int getCount() {
        return mImageUrls == null ? 0 : mImageUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getImageUrl(int position) {
        if (mImageUrls == null || mImageUrls.size() == 0) return "";
        if (position < 0 || position >= mImageUrls.size()) return "";
        return mImageUrls.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.hall_image_layout, parent, false);
            holder = new ViewHolder();
            holder.imageView = (NetworkImageView) convertView.findViewById(R.id.iv_hall_image);
            holder.addImageView = (ImageView) convertView.findViewById(R.id.iv_hall_add_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = mImageUrls.get(position);
        if (url.equals(Constants.URL_LOCAL_ADD_IMAGE)) {
            holder.imageView.setVisibility(View.GONE);
            holder.addImageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.addImageView.setVisibility(View.GONE);
            holder.imageView.setDefaultImageResId(R.drawable.empty_picture);
            holder.imageView.setImageUrl(url, mImageLoader);
        }

        // TODO : 适配图片

        return convertView;
    }

    private class ViewHolder {
        NetworkImageView imageView;
        ImageView addImageView;
    }

    public void setImageUrls(List<String> urls) {
        this.mImageUrls = urls;
        addPlaceholderIfNeeded();
        notifyDataSetChanged();
    }

    public void addImageUrl(String url) {
        if (mImageUrls == null) {
            mImageUrls = new ArrayList<>();
        } else {
            mImageUrls.remove(Constants.URL_LOCAL_ADD_IMAGE);
        }
        mImageUrls.add(url);
        addPlaceholderIfNeeded();
        notifyDataSetChanged();
    }

    private void addPlaceholderIfNeeded() {
        if (mImageUrls == null){
            mImageUrls = new ArrayList<>();
        }
        mImageUrls.remove(Constants.URL_LOCAL_ADD_IMAGE);
        if (mActivity.mIsSelf && mImageUrls.size() < Constants.MAX_IMAGE_COUNT) {
            mImageUrls.add(Constants.URL_LOCAL_ADD_IMAGE);
        }
    }

    public void remove(String url) {
        if (mImageUrls != null) {
            mImageUrls.remove(url);
            notifyDataSetChanged();
        }
    }
}
