package com.renren.api.client.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.renren.api.client.RenrenApiInvoker;
import com.renren.api.client.param.Auth;


/**
 * 
 * @author Administrator
 *
 */
public class InvitationsService extends BaseService {
    
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public final static int DOMAIN_RENREN = 0;

    public final static int DOMAIN_KAIXIN = 1;
    
    public InvitationsService(RenrenApiInvoker invoker) {
        super(invoker);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * ???????????????.?????????????????QQ??msn?????????????
     * @param domain ??????????????0????(wwv.renren.com)?1????(wwv.kaixin.com)
     * @param auth Auth???????accessToken?sessionKey???????
     *        <ul>
     *          <li>new AccessToken(accessToken)</li>
     *          <li>new SessionKey(sessionKey)</li>
     *        </ul>
     * @return url
     */
    public String createLink(int domain,Auth auth) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put(auth.getKey(), auth.getValue());
        params.put("method", "invitations.createLink");
        params.put("domain", String.valueOf(domain));
        return this.getResultStringList(params).get(0);
    }
    /**
     * ????????id,?????????????????????????????????????????????????
     * @param inviteeId ???????ID
     * @return ???????
     */
    public JSONObject getInfo(long inviteeId) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("method", "invitations.getInfo");
        params.put("invitee_uid", String.valueOf(inviteeId));
        JSONArray rets = this.getResultJSONArray(params);
        if (rets.size() < 1) return null;
        return (JSONObject) this.getResultJSONArray(params).get(0);
    }
    
    
    /**
     * ????????,?????????????????????????????????????????????????
     * @param begin ??????
     * @param end ??????
     * @param page ?? ?1??
     * @param count ????
     * @return
     */
    public JSONArray getInfo(Date begin, Date end,int page,int count) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("method", "invitations.getInfo");
        params.put("begin_time", dateFormat.format(begin));
        params.put("end_time", dateFormat.format(end));
        params.put("page", String.valueOf(page));
        params.put("count", String.valueOf(count));
        return this.getResultJSONArray(params);
    }
    
    /**
     * ????????id,?????????????????????????????????????????????????
     * @param inviter_id ????id
     * @param page ?? 1??
     * @param count ????
     * @return
     */
    public JSONArray getInfo(long inviter_id,int page,int count){
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("method", "invitations.getInfo");
        params.put("inviter_id",String.valueOf(inviter_id));
        params.put("page", String.valueOf(page));
        params.put("count", String.valueOf(count));
        return this.getResultJSONArray(params);
    }
}

