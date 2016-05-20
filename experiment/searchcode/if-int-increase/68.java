/*
 * Copyright (c) 2010-2012 AdroitLogic Private Ltd. All Rights Reserved.
 */

package org.adroitlogic.ultraesb.json;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author asankha
 */
@SuppressWarnings({"deprecation"})
public class JsonTimeoutLoadTestCase extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(JsonTimeoutLoadTestCase.class);

    private String proxyUrl;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static HttpClient client = null;
    private static volatile int value1 = 1;
    private static volatile int value2 = 1;


    static {
        HttpParams params = new BasicHttpParams();
        // Increase max total connection to 200
        ConnManagerParams.setMaxTotalConnections(params, 200);
        // Increase default max connection per route to 20
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);
        // Increase max connections for localhost:8280 to 50
        HttpHost localhost = new HttpHost("locahost", 8280);
        connPerRoute.setMaxForRoute(new HttpRoute(localhost), 50);
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        client = new DefaultHttpClient(cm, params);
    }

    public JsonTimeoutLoadTestCase(String name) throws Exception {
        super(name);
        if ("testStreaming".equals(name)) {
            proxyUrl = "http://localhost:8280/service/stream-and-aggregate-proxy";
        } else if ("testMerging".equals(name)) {
            proxyUrl = "http://localhost:8280/service/merge-and-aggregate-proxy";
        }
    }

    public void testStreaming() throws Exception {

        int v1 = value1++;
        int v2 = value2++;

        HashMap<String, Object> untyped = new HashMap<String, Object>();
        untyped.put("message", "hello world, I am client N!");
        untyped.put("v1", v1);
        untyped.put("v2", v2);
        String msg = mapper.writeValueAsString(untyped);

        HttpPost httppost = new HttpPost(proxyUrl);
        StringEntity entity = new StringEntity(msg, "application/json", "UTF-8");
        httppost.setEntity(entity);

        HttpResponse response = client.execute(httppost);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        String responseStr = baos.toString();
        //System.out.println("Result : " + responseStr);

        untyped = mapper.readValue(responseStr, HashMap.class);
        List<Map> results = (List<Map>) untyped.get("merged");
        //Assert.assertEquals(3, results.size());

        int count = 0;
        if (results != null) {
            for (Map m : results) {
                if (m.size() > 0) {
                    count++;
                    int result = (Integer) m.get("result");
                    Assert.assertEquals(result, v1 * v2);
                }
            }
        }
        //logger.info("Received : {} results : {}", count, responseStr);
    }

    public void testMerging() throws Exception {

        int v1 = value1++;
        int v2 = value2++;

        HashMap<String, Object> untyped = new HashMap<String, Object>();
        untyped.put("message", "hello world, I am client N!");
        untyped.put("v1", v1);
        untyped.put("v2", v2);
        String msg = mapper.writeValueAsString(untyped);

        HttpPost httppost = new HttpPost(proxyUrl);
        StringEntity entity = new StringEntity(msg, "application/json", "UTF-8");
        httppost.setEntity(entity);

        HttpResponse response = client.execute(httppost);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.getEntity().writeTo(baos);
        String responseStr = baos.toString();
        //System.out.println("Result : " + responseStr);

        untyped = mapper.readValue(responseStr, HashMap.class);
        List<Map> results = (List<Map>) untyped.get("merged");
        //Assert.assertEquals(3, results.size());

        int count = 0;
        if (results != null) {
            for (Map m : results) {
                if (m.size() > 0) {
                    count++;
                    int result = (Integer) m.get("result");
                    Assert.assertEquals(result, v1 * v2);
                }
            }
        }
        //logger.info("Received : {} results : {}", count, responseStr);
    }
}

