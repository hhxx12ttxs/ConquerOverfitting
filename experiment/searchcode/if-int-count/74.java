package com.renren.api.client.services;

import java.util.List;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.renren.api.client.RenrenApiInvoker;
import com.renren.api.client.param.Auth;

/**
 * ??
 * @author DuYang (yang.du@renren-inc.com) 2011-12-14
 *
 */
public class FriendsService extends BaseService {

    public FriendsService(RenrenApiInvoker invoker) {
        super(invoker);
        // TODO Auto-generated constructor stub
    }

    /**
     * ????????????????????????????
     * @param users1 ??????ID???ID???????
     * @param users2    ??????ID???ID???????
     * @param auth Auth???????accessToken?sessionKey???????
     *        <ul>
     *          <li>new AccessToken(accessToken)</li>
     *          <li>new SessionKey(sessionKey)</li>
     *        </ul>
     * @return JSONArray
     */
    public JSONArray areFriends(String users1, String users2, Auth auth) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put(auth.getKey(), auth.getValue());
        params.put("method", "friends.areFriends");
        params.put("uids1", users1);
        params.put("uids2", users2);
        JSONArray friendsInfos = this.getResultJSONArray(params);
        return friendsInfos;
    }

    /**
     * ???????????????????????id???
     * @param page ??
     * @param count ??????
     * @param auth Auth???????accessToken?sessionKey???????
     *        <ul>
     *          <li>new AccessToken(accessToken)</li>
     *          <li>new SessionKey(sessionKey)</li>
     *        </ul>
     * @return ??id??
     */
    public List<Integer> getFriendIds(int page, int count, Auth auth) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put(auth.getKey(), auth.getValue());
        params.put("method", "friends.get");
        params.put("page", String.valueOf(page));
        params.put("count", String.valueOf(count));
        return this.getResultIntList(params);
    }

    /**
     * ???????
     * @param uid1 ??1
     * @param uid2 ??2
     * @param auth Auth???????accessToken?sessionKey???????
     *        <ul>
     *          <li>new AccessToken(accessToken)</li>
     *          <li>new SessionKey(sessionKey)</li>
     *        </ul>
     * @return JSONArray ????
     */
    public JSONObject getSameFriends(long uid1, long uid2, Auth auth) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put(auth.getKey(), auth.getValue());
        params.put("method", "friends.getSameFriends");
        params.put("uid1", String.valueOf(uid1));
        params.put("uid2", String.valueOf(uid2));
        return this.getResultJSONObject(params);
    }

    /**
     * ?????????????
     * @param page ??
     * @param count ??????
     * @param auth Auth???????accessToken?sessionKey???????
     *        <ul>
     *          <li>new AccessToken(accessToken)</li>
     *          <li>new SessionKey(sessionKey)</li>
     *        </ul>
     * @return JSONArray ????????
     */
    public JSONArray getFriends(int page, int count, Auth auth) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put(auth.getKey(), auth.getValue());
        params.put("method", "friends.getFriends");
        params.put("page", String.valueOf(page));
        params.put("count", String.valueOf(count));
        return this.getResultJSONArray(params);
    }

    /**
     * ??App???ID???App???????????????????
     * @param fields ?????????????????????????
     *        ????name?????
     *        tinyurl(???)?
     *        headurl??????
     * @param auth Auth???????accessToken?sessionKey???????
     *        <ul>
     *          <li>new AccessToken(accessToken)</li>
     *          <li>new SessionKey(sessionKey)</li>
     *        </ul>
     * @return JSONArray
     */
    public JSONArray getAppUsers(String fields,Auth auth) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put(auth.getKey(), auth.getValue());
        params.put("method", "friends.getAppFriends");
        params.put("fields", fields);
        JSONArray friends = this.getResultJSONArray(params);
        return friends;
    }

    /**
     * 
     * @param name ?????
     * @param uid ?uid?? 0??
     * @param email ?email??
     * @param page  ??
     * @param count ????
     * @param condition ???? JSON???
     *        name uid email condition ????
     * @param auth Auth???????accessToken?sessionKey???????
     *        <ul>
     *          <li>new AccessToken(accessToken)</li>
     *          <li>new SessionKey(sessionKey)</li>
     *        </ul>
     * @return JSONArray
     */
    public JSONObject searchFriends(String name, long uid, String email, int page, int count,
                                    String condition, Auth auth) {

        /*
         * condition ????
         * */
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put(auth.getKey(), auth.getValue());
        params.put("method", "friends.search");
        params.put("name", name);
        if (uid > 0) {
            params.put("uid", String.valueOf(uid));
        }
        params.put("email", email);
        params.put("page", String.valueOf(page));
        params.put("count", String.valueOf(count));
        params.put("condition", condition);
        JSONObject friends = this.getResultJSONObject(params);
        return friends;
    }

}

