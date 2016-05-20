package com.davidtpate.speedtest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.davidtpate.jsonxml.R;
import com.davidtpate.speedtest.json.GsonJson;
import com.davidtpate.speedtest.json.GsonJsonAutoPOJO;
import com.davidtpate.speedtest.json.GsonJsonAutoPOJOMinimized;
import com.davidtpate.speedtest.json.GsonJsonMinimized;
import com.davidtpate.speedtest.json.JacksonJson;
import com.davidtpate.speedtest.json.JacksonJsonMinimized;
import com.davidtpate.speedtest.model.PostParent;
import com.davidtpate.speedtest.model.ResultsContainer;
import com.davidtpate.speedtest.xml.AndroidXML;
import com.davidtpate.speedtest.xml.AndroidXMLMinimized;
import com.davidtpate.util.StringUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String DATA_LINE_PADDING = "  ";

    private Button              mRunLocal;
    private Button              mRunRemote;
    private TextView            mTextView;

    private HttpParams          httpParameters;
    private HttpResponse        response;
    private HttpClient          client;

    //@formatter:off
    private final Runnable      mLocalTestTask         = new Runnable() {
       public void run() {
    
           final Map<String, ResultsContainer> results = new HashMap<String, ResultsContainer>();
    
           testImplLocal(new AndroidXML(), results);
           testImplLocal(new AndroidXMLMinimized(), results);
           testImplLocal(new GsonJson(), results);
           testImplLocal(new GsonJsonMinimized(), results);
           testImplLocal(new GsonJsonAutoPOJO(), results);
           testImplLocal(new GsonJsonAutoPOJOMinimized(), results);
           testImplLocal(new JacksonJson(), results);
           testImplLocal(new JacksonJsonMinimized(), results);
    
           runOnUiThread(new Runnable() {
               public void run() {
                   writeToTextView("== Done!");
    
               List<String> keys = new ArrayList<String>(results.keySet());
               Collections.sort(keys);
    
               int minKeyLength = 0;
               for (String key : keys) {
                   int length = String.valueOf(results.get(key).getParserName()).length();
    
                   if (length > minKeyLength) {
                       minKeyLength = length;
                   }
               }
    
               int minValueLength = 0;
               for (String key : keys) {
                   int length = String.valueOf(results.get(key).getDuration()).length();
    
                   if (length > minValueLength) {
                       minValueLength = length;
                   }
               }
    
               ResultsContainer result;
               final String label = "Runs: ";
    
               int runs = -1;
               String perRun;
    
               for (String key : keys) {
                   result = results.get(key);
    
                   if (runs != result.getTestRepeats()) {
                       writeToTextView("\n" + label + result.getTestRepeats());
    
                       runs = result.getTestRepeats();
                   }
    
                   perRun = " (" + result.getDuration() / result.getTestRepeats() + "ms/run)";
    
                   writeToTextView(DATA_LINE_PADDING + StringUtils.padRight(result.getParserName(), minKeyLength) + ": " + StringUtils.padLeft(String.valueOf(result.getDuration()), minValueLength) + "ms" + perRun);
                       }
                   }
               });
    
           }
       };
       //@formatter:on

    //@formatter:off
    private final Runnable      mRemoteTestTask         = new Runnable() {
          public void run() {
       
              final Map<String, ResultsContainer> results = new HashMap<String, ResultsContainer>();
       
              try {
                  testImplRemote(new AndroidXML(), results);
                  testImplRemote(new GsonJson(), results);
                  testImplRemote(new GsonJsonAutoPOJO(), results);
                  testImplRemote(new JacksonJson(), results);
           
                  runOnUiThread(new Runnable() {
                      public void run() {
                          writeToTextView("== Done!");
           
                      List<String> keys = new ArrayList<String>(results.keySet());
                      Collections.sort(keys);
           
                      int minKeyLength = 0;
                      for (String key : keys) {
                          int length = String.valueOf(results.get(key).getParserName()).length();
           
                          if (length > minKeyLength) {
                              minKeyLength = length;
                          }
                      }
           
                      int minValueLength = 0;
                      for (String key : keys) {
                          int length = String.valueOf(results.get(key).getDuration()).length();
           
                          if (length > minValueLength) {
                              minValueLength = length;
                          }
                      }
           
                      ResultsContainer result;
                      final String label = "Runs: ";
           
                      int runs = -1;
                      String perRun;
           
                      for (String key : keys) {
                          result = results.get(key);
           
                          if (runs != result.getTestRepeats()) {
                              writeToTextView("\n" + label + result.getTestRepeats());
           
                              runs = result.getTestRepeats();
                          }
           
                          perRun = " (" + result.getDuration() / result.getTestRepeats() + "ms/run)";
           
                          writeToTextView(DATA_LINE_PADDING + StringUtils.padRight(result.getParserName(), minKeyLength) + ": " + StringUtils.padLeft(String.valueOf(result.getDuration()), minValueLength) + "ms" + perRun);
                              }
                          }
                      });
              } catch (MalformedURLException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
              }
          };
          //@formatter:on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        mRunLocal = (Button) findViewById(R.id.run_local);
        mRunRemote = (Button) findViewById(R.id.run_remote);

        mRunLocal.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTextView.setText("Running tests (API Level: " + android.os.Build.VERSION.SDK_INT + ")...");
                writeToTextView("-----------------");
                new Thread(mLocalTestTask).start();
            }

        });

        mRunRemote.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTextView.setText("Running tests (API Level: " + android.os.Build.VERSION.SDK_INT + ")...");
                writeToTextView("-----------------");
                new Thread(mRemoteTestTask).start();
            }

        });

    }

    private void writeToTextView(String text) {
        mTextView.append("\n");
        mTextView.append(text);
    }

    public InputStream getRemoteStream(String url) throws MalformedURLException, IOException {
        return new URL(url).openStream();
    }

    public String getRemoteContent(String url) {
        try {
            httpParameters = new BasicHttpParams();
            HttpGet request = new HttpGet(url);

            HttpConnectionParams.setSoTimeout(httpParameters, 300);

            client = new DefaultHttpClient(httpParameters);
            response = client.execute(request);

            return response.toString();

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void testImplRemote(final TestParser testParser, Map<String, ResultsContainer> results) throws MalformedURLException, IOException {
        runOnUiThread(new Runnable() {
            public void run() {
                writeToTextView("== Testing '" + testParser.getName() + "'");
            }
        });

        warmUpRemote(testParser);

        int runs = 1;
        long duration = testRemote(testParser, runs);
        results.put(StringUtils.padLeft(runs, 5) + "_" + testParser.getName() + "_remote", new ResultsContainer(testParser.getName(), duration, runs));

        runs = 5;
        duration = testRemote(testParser, runs);
        results.put(StringUtils.padLeft(runs, 5) + "_" + testParser.getName() + "_remote", new ResultsContainer(testParser.getName(), duration, runs));

        runs = 100;
        duration = testRemote(testParser, runs);
        results.put(StringUtils.padLeft(runs, 5) + "_" + testParser.getName() + "_remote", new ResultsContainer(testParser.getName(), duration, runs));
    }

    private long testRemote(final TestParser testParser, int repeats) throws MalformedURLException, IOException {
        InputStream inputStream = getResources().openRawResource(testParser.getResource());

        List<PostParent> result = testParser.parse(inputStream);
        verify(result);

        long duration = 0;

        for (int i = 0; i < repeats; i++) {
            long start = System.currentTimeMillis();
            inputStream = getRemoteStream(testParser.getUrl());
            testParser.parse(inputStream);
            duration += (System.currentTimeMillis() - start);
        }

        return duration;
    }

    private void warmUpRemote(final TestParser testParser) throws MalformedURLException, IOException {
        InputStream inputStream;
        for (int i = 0; i < 5; i++) {
            inputStream = getRemoteStream(testParser.getUrl());
            testParser.parse(inputStream);
        }
    }

    private void testImplLocal(final TestParser testParser, Map<String, ResultsContainer> results) {
        runOnUiThread(new Runnable() {
            public void run() {
                writeToTextView("== Testing '" + testParser.getName() + "'");
            }
        });

        warmUpLocal(testParser);

        int runs = 1;
        long duration = testLocal(testParser, runs);
        results.put(StringUtils.padLeft(runs, 5) + "_" + testParser.getName() + "_local", new ResultsContainer(testParser.getName(), duration, runs));

        runs = 5;
        duration = testLocal(testParser, runs);
        results.put(StringUtils.padLeft(runs, 5) + "_" + testParser.getName() + "_local", new ResultsContainer(testParser.getName(), duration, runs));

        runs = 100;
        duration = testLocal(testParser, runs);
        results.put(StringUtils.padLeft(runs, 5) + "_" + testParser.getName() + "_local", new ResultsContainer(testParser.getName(), duration, runs));
    }

    private long testLocal(final TestParser testParser, int repeats) {
        InputStream inputStream = getResources().openRawResource(testParser.getResource());

        List<PostParent> result = testParser.parse(inputStream);
        verify(result);

        long duration = 0;

        for (int i = 0; i < repeats; i++) {
            inputStream = getResources().openRawResource(testParser.getResource());
            long start = System.currentTimeMillis();
            testParser.parse(inputStream);
            duration += (System.currentTimeMillis() - start);
        }

        return duration;
    }

    private static void verify(List<PostParent> result) {
        if (result.size() != 25) {
            // throw new IllegalStateException("Expected 25 Posts, but was " + result.size());
        }
        for (PostParent post : result) {
            if (post.getPost().getSubreddit() == null || post.getPost().getTitle() == null || post.getPost().getUrl() == null || post.getPost().getUpVotes() == -3333333 || post.getPost().getDownVotes() == -3333333 || post.getPost().getThumbnail() == null)
                throw new IllegalStateException("Missing Data for Post");
        }
    }

    private void warmUpLocal(final TestParser testParser) {
        InputStream inputStream;
        for (int i = 0; i < 5; i++) {
            inputStream = getResources().openRawResource(testParser.getResource());
            testParser.parse(inputStream);
        }
    }

}

