package de.teamprojekt;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;
import de.teamprojekt.Camera.CameraException;
import de.teamprojekt.viewer.AudioFragment;
import de.teamprojekt.viewer.DescriptionFragment;
import de.teamprojekt.viewer.PhotoFragment;
import de.teamprojekt.viewer.VideoFragment;
import de.teamprojekt.dbconnection.MediaResources;

/**
 * 
 * @author 
 *
 */
@TargetApi(13)
public class CameraPreviewActivity__Other extends Activity
{
	private static final String TAG_  =	"CameraPreviewActivity__Other";
//	public static int THEME = com.actionbarsherlock.R.style.Theme_Sherlock;
	private enum State
	{
		INIT, CALIBRATION, MARKER_DETECTION, SHOW_CONTENTS, HIDE_CONTENTS, CHANGE_CAMERA;
	}
	private State state_ = State.INIT;
	private int currentDeviceId_ = Highgui.CV_CAP_ANDROID;
    private CameraPreviewView	cpView_;
    private OpenGL3DView		openGL3DView_;
    private FrameLayout			mainLayout_;
    
    ActionBar bar;
    
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        Log.i(TAG_, "onCreate");
        
//        setTheme(THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        
        StrictMode.enableDefaults();
        
        openGL3DView_ = new OpenGL3DView( this );
        openGL3DView_.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        cpView_ = new CameraPreviewView( this, openGL3DView_ );
        
        //Use an android relative layout to stack two views on top of each other  
        mainLayout_ = (FrameLayout) findViewById(R.id.mainLayout);
        mainLayout_.addView(openGL3DView_);
        mainLayout_.addView(cpView_);
        
        bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
//        bar.setDisplayShowHomeEnabled(true);
//        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i(TAG_, "onPrepareOptionsMenu");
		menu.clear();
		MenuInflater inflater = getMenuInflater();

		switch(state_) {
		
			case INIT:
				inflater.inflate(R.menu.basic_actions, menu);
				break;
			
			case CALIBRATION:
				inflater.inflate(R.menu.camera_calibration, menu);
				break;
			
			case MARKER_DETECTION:
				inflater.inflate(R.menu.media_contents, menu);
				break;
				
			case SHOW_CONTENTS:
				inflater.inflate(R.menu.no_contents, menu);
				break;
				
			case HIDE_CONTENTS:
				inflater.inflate(R.menu.no_contents, menu);
				break;
				
			case CHANGE_CAMERA:
				inflater.inflate(R.menu.change_camera, menu);
				break;
				
			default:
		}
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
		Log.i(TAG_, "onOptionsItemSelected()");
		
    	List<Integer> deviceIds;
    	List<Size> resolutions;
    	ImageProcessingTask ipt;
        
    	switch(item.getItemId())
        {
//        	case android.R.id.home:
//        		Log.i(TAG_, "app icon selected, navigate up");
//
//        		// app icon in action bar clicked; go home            
//        		Intent intent = new Intent(this, CameraPreviewActivity__Other.class);            
//        		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);            
//        		startActivity(intent);            
//        		break;        
        	
        	case R.id.marker_detection:
        		Log.i(TAG_, "goto marker detection selected.");
        		state_ = State.MARKER_DETECTION;
        		
        		deviceIds = new ArrayList<Integer>();
    	        deviceIds.add(Highgui.CV_CAP_ANDROID);
    	        currentDeviceId_ = Highgui.CV_CAP_ANDROID;
    	        resolutions = new ArrayList<Size>();
    	        resolutions.add(new Size(720, 408));
    	        ipt = new MarkerDetectionTask(true, 0.0, deviceIds, resolutions);
    	        cpView_.switchEndlessTask(ipt);
    	        break;
    	        
        	case R.id.calibration:
        		Log.i(TAG_, "goto calibration selected.");
        		state_ = State.CALIBRATION;
        		
        		deviceIds = new ArrayList<Integer>();
    	        deviceIds.add( currentDeviceId_ );
    	        resolutions = new ArrayList<Size>();
    	        resolutions.add(new Size(720, 408));
    	        ipt = new LiveImageTask(deviceIds, resolutions);        		
        		cpView_.switchEndlessTask(ipt);
        		break;
        		
        	case R.id.image_shooting:
        		Log.i(TAG_, "shoot image selected.");
        		deviceIds = new ArrayList<Integer>();
    	        deviceIds.add(currentDeviceId_);
    	        resolutions = new ArrayList<Size>();
    	        resolutions.add(new Size(720, 408));
    	        ipt = new AddCalibrationImageTask(deviceIds, resolutions);    
    	        cpView_.addOneTimeTask(ipt);
    	        break;
    	        
        	case R.id.camera_calibration:
        		Log.i(TAG_, "calibrate camera selected.");
				try {
					CameraHandler.getInstance().getDevice(currentDeviceId_).calibrate();
				} catch (CameraException e) {
					Log.e(TAG_, "Camera calibration failed with exception " + e); 
				}
				break;
        	
        	case R.id.camera_choosing:
        		Log.i(TAG_, "choose camera selected.");        		
        		state_ = State.CHANGE_CAMERA;
        		
        		break;
        		
        	case R.id.front_camera:
        		if (!item.isChecked()) {
        			Log.i(TAG_, "Front camera selected.");
	        		currentDeviceId_ = Highgui.CV_CAP_ANDROID + 1;
	        		state_ = State.CALIBRATION;

	        		deviceIds = new ArrayList<Integer>();
	    	        deviceIds.add( currentDeviceId_ );
	    	        resolutions = new ArrayList<Size>();
	    	        resolutions.add( new Size(720, 408) );
	    	        ipt = new LiveImageTask(deviceIds, resolutions);  
	    	        cpView_.switchEndlessTask(ipt);
	    	        item.setChecked(true);
        		}
        		break;
        		
        	case R.id.main_camera:
        		if (!item.isChecked()) {
        			Log.i(TAG_, "Main camera selected.");
	        		currentDeviceId_ = Highgui.CV_CAP_ANDROID;
	        		state_ = State.CALIBRATION;
	        		
	        		deviceIds = new ArrayList<Integer>();
	    	        deviceIds.add( currentDeviceId_ );
	    	        resolutions = new ArrayList<Size>();
	    	        resolutions.add(new Size(720, 408));
	    	        ipt = new LiveImageTask(deviceIds, resolutions);  
	    	        cpView_.switchEndlessTask(ipt);
	    	        item.setChecked(true);
        		}
        		break;
        	
        	case R.id.media_contents:
        		Log.i(TAG_, "3D-Object belonged content chosen");
        		state_ = State.SHOW_CONTENTS;
//        		startActivity(new Intent(this, FragmentTabs.class));
        		bar.addTab(bar.newTab()
                        .setText("Description")
                        .setIcon(getResources().getDrawable(R.drawable.icon_photos_tab))
                        .setTabListener(new TabListener<DescriptionFragment>(
                                this, "doc", DescriptionFragment.class)));
                bar.addTab(bar.newTab()
                        .setText("Photos")
                        .setIcon(getResources().getDrawable(R.drawable.icon_photos_tab))
                        .setTabListener(new TabListener<PhotoFragment>(
                                this, "img", PhotoFragment.class)));
                bar.addTab(bar.newTab()
                        .setText("Audios")
                        .setIcon(getResources().getDrawable(R.drawable.icon_songs_tab))
                        .setTabListener(new TabListener<AudioFragment>(
                                this, "aud", AudioFragment.class)));
                bar.addTab(bar.newTab()
                        .setText("Videos")
                        .setIcon(getResources().getDrawable(R.drawable.icon_videos_tab))
                        .setTabListener(new TabListener<VideoFragment>(
                                this, "vid", VideoFragment.class)));
        		break;
        	
        	case R.id.hide_contents:
        		state_ = State.MARKER_DETECTION;
        		getActionBar().removeAllTabs();
        		break;
        	
        	case R.id.sync_contents:
        		//TODO: Bitte hier noch die markerID eines Markers, der in diesem Moment erkannt wurde, einfügen.
        		//Beim Druck auf den Button werden dann automatisch alle Dateien des erkannten Markers nacheinander heruntergeladen.
        		//Es wäre schön, wenn dazu auf dem Tablet der Text "Download läuft" und "Download abgeschlossen" erscheint.
        		
        		//************************************************
        		//Hier die 1 mit der momentanen MarkerID ersetzen
        		//************************************************
        		int markerID = 1;
        		//************************************************
        		
        		//==================================================================================
        		//Download der Dateien
        		//==================================================================================
        		String[][] filesOfMarker = MediaResources.getMedia(markerID);
        		
        		int i = 0;
        		int dateiID = 0;
        		while (filesOfMarker[i][0] != null){
        			dateiID = Integer.parseInt(filesOfMarker[i][0]);
        			MediaResources.downloadMedia(dateiID);
        			i = i+1;
        		}
        		//==================================================================================
        		
        		break;
        		
        	default:
        		Log.i(TAG_, "This should not happen.");
        		return super.onOptionsItemSelected(item);
        }
    	
    	invalidateOptionsMenu();
    	return true;
    }
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }
    
//	@Override
//	protected void onPause() {
//		Log.i(TAG_, "my onPause()");
//		super.onPause();
//	}
//
//	@Override
//	protected void onResume() {
//		Log.i(TAG_, "my onResume()");
//		super.onResume();
//		List<Integer> deviceIds;
//    	List<Size> resolutions;
//    	ImageProcessingTask ipt;
//    	deviceIds = new ArrayList<Integer>();
//        deviceIds.add(Highgui.CV_CAP_ANDROID);
//        currentDeviceId_ = Highgui.CV_CAP_ANDROID;
//        resolutions = new ArrayList<Size>();
//        resolutions.add(new Size(720, 408));
//        ipt = new MarkerDetectionTask(true, 0.0, deviceIds, resolutions);
//        cpView_.switchEndlessTask(ipt);
//	}

	@Override
    protected void onStop()
    {
    	Log.i(TAG_, "my onStop()");
    	CameraHandler.getInstance().getDevice(Highgui.CV_CAP_ANDROID).release();
    	CameraHandler.getInstance().getDevice(Highgui.CV_CAP_ANDROID + 1 ).release();
    	super.onStop();
    }

//	@Override
//	protected void onRestart() {
//		Log.i(TAG_, "my onReStart()");
//		super.onRestart();
//	}
//
//	protected void onDestroy()
//    {
//    	Log.i(TAG_, "my onDestroy()");
//
//    	CameraHandler.getInstance().getDevice(Highgui.CV_CAP_ANDROID).release();
//    	CameraHandler.getInstance().getDevice(Highgui.CV_CAP_ANDROID + 1 ).release();
//    	super.onDestroy();
//    }
//    
//    
//    
//    
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//    	super.onSaveInstanceState(outState);
//    }
//    
//    @Override 
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//    	super.onRestoreInstanceState(savedInstanceState);
//    }
    
}

