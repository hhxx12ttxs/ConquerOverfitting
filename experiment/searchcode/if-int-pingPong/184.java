package com.pingpong.android.base;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.UIUtils;

public class HttpRequest {

    private Context mContext;
    private int mRequestId;
    private IHttpCallBacker mCallBacker;
    private HttpRequestParam mRequestParams;
    private HttpStringRequest mRequest;

    public HttpRequest(Context context, int requestId, IHttpCallBacker callBacker) {
        mContext = context;
        mRequestId = requestId;
        mCallBacker = callBacker;
        mRequestParams = callBacker.makeParam(requestId);
        if (mRequestParams == null) {
            throw new IllegalArgumentException("HttpRequestParams can not null");
        }
        mRequest = new HttpStringRequest(Request.Method.POST, mRequestParams.getUrl(), mListener, mErrorListener);
        mRequest.setParams(mRequestParams.getAllParams());
    }

    public HttpRequest(Context context, int requestId, int method, IHttpCallBacker callBacker) {
        mContext = context;
        mRequestId = requestId;
        mCallBacker = callBacker;
        mRequestParams = callBacker.makeParam(requestId);
        if (mRequestParams == null) {
            throw new IllegalArgumentException("HttpRequestParams can not null");
        }
        mRequest = new HttpStringRequest(method, mRequestParams.getUrl(), mListener, mErrorListener);
        mRequest.setParams(mRequestParams.getAllParams());
    }


    private Response.Listener<String> mListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            L.d("Response --> " + mRequestId + " : " + response);
            mCallBacker.dataReceived(mRequestId, parseResult(response, mRequestParams.getResultModel()));
            UIUtils.dismissProgressDialog();
        }
    };

    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error == null) {
                L.e("error null");
                return;
            }
            L.d("Response --> " + mRequestId + " : " + error.getCause());
            if (mCallBacker != null) {
                if (error.networkResponse != null) {
                    mCallBacker.processHttpException(error.networkResponse.statusCode);
                }else {
                    mCallBacker.processHttpException(-1);  //-1表示无法连接到服务器
                }
            }
            UIUtils.dismissProgressDialog();
        }
    };

    private BaseModel parseResult(String response, Class clazz) {
        Gson gson = new Gson();
        return (BaseModel)gson.fromJson(response, clazz);
    }

    public void doRequest() {
        SingletonRequestQueue.getInstance(mContext).addToRequestQueue(mRequest);
    }
}

