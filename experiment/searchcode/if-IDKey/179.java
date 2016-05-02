package cc.clearcode.nudanachacie.facebook;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import cc.clearcode.nudanachacie.activities.MainActivity;
import cc.clearcode.nudanachacie.facebook.objects.Event;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.Util;
import cc.clearcode.nudanachacie.facebook.listeners.*;
import cc.clearcode.nudanachacie.facebook.parameters.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

public class FBConnection {

	public static final String TAG = "FACEBOOK CONNECT";

	private static Facebook mFacebook;
	private static AsyncFacebookRunner mAsyncFacebookRunner;
	private Context context;

	public FBConnection(Context context) {
		this.context = context;
	}

	public String permissions[] = { "user_about_me", "user_activities",
			"user_birthday", "user_checkins", "user_education_history",
			"user_events", "create_event",
			"user_religion_politics", "user_status", "user_videos", "email",

			"read_friendlists", "read_mailbox", "read_requests",
            "create_event", "manage_friendlists", "manage_notifications",
			"offline_access", "publish_stream" };

	public void initFacebookSession(String APP_ID) {
		if (APP_ID == null || APP_ID.equals("")) {
			Util.showAlert(context, "Warning",
					"Facebook Applicaton ID must be "
							+ "specified before running");
		} else {
			mFacebook = new Facebook(APP_ID);
			mAsyncFacebookRunner = new AsyncFacebookRunner(mFacebook);
		}
	}

	public void login(Activity activity, String[] userPermissions,
			LoginDialogListener dialogListener) {
		if (!mFacebook.isSessionValid()) {
			if (userPermissions == null || userPermissions.equals("")) {
				mFacebook.authorize(activity, permissions,
						Facebook.FORCE_DIALOG_AUTH, dialogListener);
			} else {
				mFacebook.authorize(activity, userPermissions,
						Facebook.FORCE_DIALOG_AUTH, dialogListener);
			}
		}
	}

	public void refreshAccessToken() {
		mFacebook.extendAccessTokenIfNeeded(context, null);
	}

	public void logout(Context c, LogoutRequestListener requestListener) {
		if (mFacebook.isSessionValid()) {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFacebook);
			asyncRunner.logout(c, requestListener);
		} else {
			Toast.makeText(context, "There is no valid session",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void getUserData(UserDataRequestListener requestListener) {
		mAsyncFacebookRunner.request("me", requestListener);
	}

	public void getFriendsList(FriendsListRequestListener requestListener) {
		mAsyncFacebookRunner.request("me/friends", requestListener);
	}

    public void getMyEvents(GetEventsRequestListener requestListener) {
        mAsyncFacebookRunner.request("me/events", requestListener);
    }

    public Event getFriendEvent(String friendId) throws IOException, JSONException {
        String response =  mFacebook.request(friendId + "/events");
        JSONObject jsonObject =  Util.parseJson(response);
        Event event = new Event(jsonObject.getString(Event.idKey), jsonObject.getString(Event.ownerKey), jsonObject.getString(Event.nameKey), jsonObject.getString(Event.descriptionKey), jsonObject.getString(Event.startTimeKey), jsonObject.getString(Event.endTimeKey), jsonObject.getString(Event.locationKey), jsonObject.getString(Event.pictureKey));
        return event;
    }

    public void createEvent(CreateEventDialogListener requestListener, CreateEventParameters params) {
        mAsyncFacebookRunner.request("me/events", params.getBundle(), "POST", requestListener, null);
    }

	public void postOnWallDialog(Context context, PostDialogParameters params,
			PostOnWallDialogListener dialogListener) {
		if (params == null) {
			Bundle bundle = new Bundle();
			mFacebook.dialog(context, "feed", bundle, dialogListener);
		} else {
			mFacebook.dialog(context, "feed", params.getBundle(),
					dialogListener);
		}
	}
    /**
     *
     * @return
     */
    public boolean isLogged()
    {
        return mFacebook.isSessionValid();
    }
}

