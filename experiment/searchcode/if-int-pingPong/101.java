package com.pingpong.android.base;

import android.app.Activity;
import android.content.Context;

import com.pingpong.android.db.CityDao;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.Hall;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangZhenJie on 2015/2/24.
 */
public class DataManager {

    private static DataManager mDataManager;

    private static PingPongApp mPingPongApp;
    private Context mContext;

    /**
     * 当前登录的球类用户，不能作为登录依据，可能为空值。
     */
    private Friend mLoginFriend;
    /**
     * 当前登录的球馆用户，不能作为登录依据，可能为空值。
     */
    private Hall mLoginHall;

    /**
     * 主Activity，用于退出登录时使用
     */
    private Activity mMainActivity;

    /**
     * 在屏幕前面的聊天界面中的对方的ID(单个用户的ID或者多用户的组ID)
     */
    private String mFrontChat;

    /**
     * MainActivity是否在前台标志
     */
    private boolean isMainActivityOnForeground;

    /**
     * 选择城市中，记录Activity,以便后面全部取消
     */
    private List<Activity> mActivityList;

    /**
     * 记录选中的城市列表
     */
    private List<CityDao> mSelectedCity;

    private DataManager() {

    }

    private DataManager(Context context) {
        this.mContext = context;
        if (context != null) {
            mPingPongApp = (PingPongApp) context.getApplicationContext();
        }
    }

    public static DataManager getInstance() {

        if (mDataManager == null) {
            synchronized (DataManager.class) {
                if (mDataManager == null) {
                    mDataManager = new DataManager();
                }
            }
        }

        return mDataManager;
    }

    public static DataManager getInstance(Context context) {
        if (mDataManager == null) {
            synchronized (DataManager.class) {
                if (mDataManager == null) {
                    mDataManager = new DataManager(context);
                }
            }
        }

        return mDataManager;
    }

    public Context getContext() {
        return mContext;
    }

    public void setLoginUserId(long userId) {
        if (mPingPongApp != null) {
            mPingPongApp.setLoginUserId(userId);
        }
    }

    public long getLoginUserId() {
        if (mPingPongApp == null) {
            return 0;
        }
        return mPingPongApp.getLoginUserId();
    }

    public int getLoginType() {
        if (mPingPongApp == null) {
            return -1;
        }
        return mPingPongApp.getLoginType();
    }

    public void setLoginType(int type) {
        if (mPingPongApp != null) {
            mPingPongApp.setLoginType(type);
        }
    }

    public Friend getLoginFriend() {
        return mLoginFriend == null ? new Friend() : mLoginFriend;
    }

    public void setLoginFriend(Friend loginFriend) {
        mLoginFriend = loginFriend;
    }

    public Hall getLoginHall() {
        return mLoginHall == null ? new Hall() : mLoginHall;
    }

    public void setLoginHall(Hall hall) {
        mLoginHall = hall;
    }

    public void setMainActivity(Activity ac) {
        mMainActivity = ac;
    }

    public Activity getMainActivity() {
        return mMainActivity;
    }

    public void setFrontChat(String id) {
        mFrontChat = id;
    }

    public String getFrontChat() {
        return mFrontChat;
    }

    public boolean isMainActivityOnForeground() {
        return isMainActivityOnForeground;
    }

    public void setIsMainActivityOnForeground(boolean b) {
        isMainActivityOnForeground = b;
    }

    public void addActivityToList(Activity ac) {
        if (mActivityList == null) {
            mActivityList = new ArrayList<>();
        }
        mActivityList.add(ac);
    }

    public void finishAllActivityInList() {
        if (mActivityList == null) return;
        for (Activity ac : mActivityList) {
            ac.finish();
        }
    }

    public void addSelectedCityInLevel(CityDao city, int level) {
        if (mSelectedCity == null || level == 1) {
            mSelectedCity = new ArrayList<>();
        }
        if (level - 1 >= mSelectedCity.size()) {
            mSelectedCity.add(city);
        } else {
            mSelectedCity.remove(level - 1);
            mSelectedCity.add(level - 1, city);
        }
    }

    public List<CityDao> getSelectedCity() {
        return mSelectedCity;
    }

    public String getSelectedCityString() {
        if (mSelectedCity == null) return "";
        StringBuilder sb = new StringBuilder();
        for (CityDao city : mSelectedCity) {
            sb.append(city.name);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}




