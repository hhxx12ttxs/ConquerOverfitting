package com.markduenas.android.apasspigstally.db;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * The Class CommonUtils.
 * 
 * @author markduenas
 */
public class CommonUtils
{

	public static final String CONFIGERROR_MISSINGATTRIBUTESET = "Configuration problem: no items found!";

	public static String getBRSharedPreference(Context context, String key, String defaultValue)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(key, defaultValue);
	}

	public static boolean setBRSharedPreference(Context context, String key, String value)
	{
		Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
		e.putString(key, value);
		e.commit();
		return true;
	}

	public static void hideKeyboard(Activity activity, View v)
	{
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public static void showKeyboard(Activity activity)
	{
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public static boolean isNetworkConnected(Context context)
	{
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		return activeInfo.isConnected();
	}

	/**
	 * Gets the stack trace string.
	 * 
	 * @param e
	 *            the e
	 * @return the stack trace string
	 */
	public static String getStackTraceString(Exception e)
	{
		String stackTrace = "";
		for (StackTraceElement l : e.getStackTrace())
			stackTrace += l.toString();
		return stackTrace;
	}

	/**
	 * Make long toast.
	 * 
	 * @param ctx
	 *            the ctx
	 * @param message
	 *            the message
	 */
	public static void makeLongToast(Context ctx, String message)
	{
		Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
	}

	public static void makeShortToast(Context ctx, String message)
	{
		Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Log stack trace.
	 * 
	 * @param e
	 *            the e
	 */
	public static void logStackTrace(Exception e)
	{
		for (StackTraceElement s : e.getStackTrace())
		{
			Log.e("Stack trace: ", s.toString());
		}
	}

	/**
	 * Given either a Spannable String or a regular String and a token, apply the given CharacterStyle to the span between the tokens, and also remove tokens.
	 * <p>
	 * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##",
	 * new ForegroundColorSpan(0xFFFF0000));} will return a CharSequence {@code "Hello world!"} with {@code world} in red.
	 * 
	 * @param text
	 *            The text, with the tokens, to adjust.
	 * @param token
	 *            The token string; there should be at least two instances of token in text.
	 * @param cs
	 *            The style to apply to the CharSequence. WARNING: You cannot send the same two instances of this parameter, otherwise the second call will remove the original span.
	 * @return A Spannable CharSequence with the new style applied.
	 * 
	 * @see http://developer.android.com/reference/android/text/style/CharacterStyle.html
	 */
	public static CharSequence setSpanBetweenTokens(CharSequence text, String token, CharacterStyle... cs)
	{
		// Start and end refer to the points where the span will apply
		int tokenLen = token.length();
		int start = text.toString().indexOf(token) + tokenLen;
		int end = text.toString().indexOf(token, start);

		if (start > -1 && end > -1)
		{
			// Copy the spannable string to a mutable spannable string
			SpannableStringBuilder ssb = new SpannableStringBuilder(text);
			for (CharacterStyle c : cs)
				ssb.setSpan(c, start, end, 0);

			// Delete the tokens before and after the span
			ssb.delete(end, end + tokenLen);
			ssb.delete(start - tokenLen, start);

			text = ssb;
		}

		return text;
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This method queries the package manager for installed packages that can respond to an intent with the specified action. If no suitable package is found, this method
	 * returns false.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param action
	 *            The Intent action to check for availability.
	 * 
	 * @return True if an Intent with the specified action can be sent and responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action)
	{
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static byte[] my_short_to_bb_le(short myShort)
	{
		return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(myShort).array();
	}

	public static short my_bb_to_short_le(byte[] byteBarray)
	{
		return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	public static byte[] my_int_to_bb_le(int myInteger)
	{
		return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putInt(myInteger).array();
	}

	public static int my_bb_to_int_le(byte[] byteBarray)
	{
		return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	public static byte[] my_int_to_bb_be(int myInteger)
	{
		return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(myInteger).array();
	}

	public static int my_bb_to_int_be(byte[] byteBarray)
	{
		return ByteBuffer.wrap(byteBarray).order(ByteOrder.BIG_ENDIAN).getInt();
	}

	// Given a string representation of a URL, sets up a connection and gets
	// an input stream.
	/**
	 * Download url.
	 * 
	 * @param urlString
	 *            the url string
	 * @return the input stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static InputStream downloadUrl(String urlString) throws IOException
	{
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}

	public static InputStream downloadUrl(String urlString, String headerPropertyName, String headerPropertyValue) throws IOException
	{
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setRequestProperty(headerPropertyName, headerPropertyValue);
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		InputStream stream = conn.getInputStream();
		return stream;
	}

	public static HttpResponse doPost(String mUrl, HashMap<String, String> hm, String jsessionid, DefaultHttpClient httpClient)
	{
		final String LOG_TAG = "CommonUtils.doPost";

		HttpResponse response = null;

		// if (username != null && password != null)
		// {
		//
		// httpClient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
		//
		// new UsernamePasswordCredentials(username, password));
		//
		// }

		HttpPost postMethod = new HttpPost(mUrl);
		postMethod.addHeader("Cookie", "JSESSIONID=" + jsessionid);
		if (hm == null)
			return null;

		try
		{

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			Iterator<String> it = hm.keySet().iterator();
			String k, v;

			while (it.hasNext())
			{
				k = it.next();
				v = hm.get(k);
				nameValuePairs.add(new BasicNameValuePair(k, v));
			}

			postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpClient.execute(postMethod);
			Log.i(LOG_TAG, "STATUS CODE: " + String.valueOf(response.getStatusLine().getStatusCode()));

		}
		catch (Exception e)
		{
			logStackTrace(e);
		}
		finally
		{

		}

		return response;

	}

}

