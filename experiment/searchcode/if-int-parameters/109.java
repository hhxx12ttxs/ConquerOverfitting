package com.msdpe.pietalk.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.msdpe.pietalk.CameraPreview;
import com.msdpe.pietalk.Constants;
import com.msdpe.pietalk.Constants.CameraUIMode;
import com.msdpe.pietalk.PreferencesHandler;
import com.msdpe.pietalk.R;
import com.msdpe.pietalk.base.BaseActivity;
import com.msdpe.pietalk.util.NetworkUtilities;
import com.msdpe.pietalk.util.PieTalkAlert;
import com.msdpe.pietalk.util.PieTalkLogger;

public class RecordActivity extends BaseActivity implements NumberPicker.OnValueChangeListener {
	
	private final String TAG = "RecordActivity";
	private Camera mCamera;
	private CameraPreview mCameraPreview;
	private VideoView mVideoView;
	private ImageView mImageView;
	private MediaRecorder mMediaRecorder;
	private ImageButton mBtnSwitchCamera;
	private ImageButton mBtnFlash;
	private ImageButton mBtnTakePicture;
	private ImageButton mBtnPies;
	private ImageButton mBtnFriends;
	private ImageButton mBtnSend;
	private ImageButton mBtnDelete;
	private ImageButton mBtnTime;
	private TextView mLblTime;
	private RelativeLayout mLayoutTime;
	private int mCameraNumber;
	private boolean mFlashOn;
	private boolean mTakingVideo;
	private boolean mReviewingPicture;
	private boolean mReviewingVideo;
	private String  mVideoFileName;
	private String  mFileFullPath;
	private File mMediaStorageDir;
	private FrameLayout mFrameLayout;
	private byte[] mPictureData;
	private boolean mIsScrolling;
	private int     mSecondsSelected;
	private boolean mIsReply;
	private String mReplyToUserId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_record);
		
		
		
		mBtnSwitchCamera = (ImageButton) findViewById(R.id.btnSwitchCameras);
		mBtnFlash = (ImageButton) findViewById(R.id.btnFlash);
		mBtnTakePicture = (ImageButton) findViewById(R.id.btnTakePicture);
		mBtnPies = (ImageButton) findViewById(R.id.btnPies);
		mBtnFriends = (ImageButton) findViewById(R.id.btnFriends);
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mImageView = (ImageView) findViewById(R.id.pictureView2);
		mBtnSend = (ImageButton) findViewById(R.id.btnSend);
		mBtnDelete = (ImageButton) findViewById(R.id.btnDelete);
		mBtnTime = (ImageButton) findViewById(R.id.btnTime);
		mLayoutTime = (RelativeLayout) findViewById(R.id.layoutTime);
		
		mLblTime = (TextView) findViewById(R.id.lblTime);
		
		
		mBtnTakePicture.setOnClickListener(takePictureListener);
		mBtnTakePicture.setOnLongClickListener(takeVideoListener);
		mBtnTakePicture.setOnTouchListener(touchListener);
		
		if (NetworkUtilities.isNetworkOnline(mActivity)) {
			mPieTalkService.getFriends();			
			mPieTalkService.getPies();
			mPieTalkService.getPreferences();
		} else {
			PieTalkAlert.showSimpleErrorDialog(mActivity, "You should connect to the internet and rerun PieTalk.");
		}
		
		mTakingVideo = false;
		mReviewingPicture = false;
		mReviewingVideo = false;
		mIsScrolling = false;
		mSecondsSelected = 3;
		
		mLblTime.setText(mSecondsSelected + "");
		
		//TODO: Consider moving to background task
		mPieTalkService.registerForPush();
	}
	
	private OnClickListener takePictureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			PieTalkLogger.i(TAG, "TakePic");
			
			mCamera.takePicture(null, null, mPictureCallback);
			//Hide UI
			setUIMode(Constants.CameraUIMode.UI_MODE_TAKING_PICTURE);
		}	
	};
	
	
	
	private void setUIMode(Constants.CameraUIMode uiMode) {
		switch (uiMode) {
		case UI_MODE_REPLYING:
			mBtnFlash.setVisibility(View.VISIBLE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPies.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.VISIBLE);
			mBtnTakePicture.setVisibility(View.VISIBLE);
			mBtnSend.setVisibility(View.GONE);
			mBtnDelete.setVisibility(View.GONE);
			mLayoutTime.setVisibility(View.GONE);
			
			mCameraPreview.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);
			mImageView.setVisibility(View.GONE);
			break;
		case UI_MODE_PRE_PICTURE:
			mBtnFlash.setVisibility(View.VISIBLE);
			mBtnFriends.setVisibility(View.VISIBLE);
			mBtnPies.setVisibility(View.VISIBLE);
			mBtnSwitchCamera.setVisibility(View.VISIBLE);
			mBtnTakePicture.setVisibility(View.VISIBLE);
			mBtnSend.setVisibility(View.GONE);
			mBtnDelete.setVisibility(View.GONE);
			mLayoutTime.setVisibility(View.GONE);
			
			mCameraPreview.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);
			mImageView.setVisibility(View.GONE);
			break;
		case UI_MODE_REVIEW_PICTURE:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPies.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			mBtnSend.setVisibility(View.VISIBLE);
			mBtnDelete.setVisibility(View.VISIBLE);
			mLayoutTime.setVisibility(View.VISIBLE);
			
			Bitmap myBitmap = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length);
			mImageView.setImageBitmap(myBitmap);
			
			
			mImageView.setVisibility(View.VISIBLE);
			mCameraPreview.setVisibility(View.GONE);
			mVideoView.setVisibility(View.GONE);
						
			break;
		case UI_MODE_REVIEW_VIDEO:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPies.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			mBtnSend.setVisibility(View.VISIBLE);
			mBtnDelete.setVisibility(View.VISIBLE);
			mLayoutTime.setVisibility(View.VISIBLE);
			
			if (mVideoView == null)
				mVideoView = new VideoView(this);
			
			
		
			
			//mVideoView.setVideoPath(mMediaStorageDir.getPath() + File.separator +
//			        mVideoFileName);
			mVideoView.setVideoPath(mFileFullPath);
			
			
			mCameraPreview.setVisibility(View.GONE);
			mVideoView.setVisibility(View.VISIBLE);
			//mFrameLayout.removeAllViews();
			//Todo add this back in when necessary
//			mFrameLayout.addView(mVideoView);
//			DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
//		    android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mVideoView.getLayoutParams();
//		    params.width =  metrics.widthPixels;
//		    params.height = metrics.heightPixels;
//		    params.rightMargin = 00;
		    //params.gravity = Gravity.CENTER;
//		    mVideoView.setLayoutParams(params);
		    
		    mVideoView.setOnPreparedListener(new OnPreparedListener() {				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
					
					
//					Display display = getWindowManager().getDefaultDisplay(); 
//				    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(display.getWidth(),
//				            MeasureSpec.UNSPECIFIED);
//				    int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(display.getHeight(),
//				            MeasureSpec.UNSPECIFIED);
//				    mVideoView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
//					
//					int videoWidth = mp.getVideoWidth();
//				    int videoHeight = mp.getVideoHeight();
//				    float videoProportion = (float) videoWidth / (float) videoHeight;       
//				    int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//				    int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
//				    float screenProportion = (float) screenWidth / (float) screenHeight;
//				    android.view.ViewGroup.LayoutParams lp = mFrameLayout.getLayoutParams();
//
//				    if (videoProportion > screenProportion) {
//				        lp.width = screenWidth;
//				        lp.height = (int) ((float) screenWidth / videoProportion);
//				    } else {
//				        lp.width = (int) (videoProportion * (float) screenHeight);
//				        lp.height = screenHeight;
//				    }
//				    mFrameLayout.setLayoutParams(lp);
					
					
					
					
					
					
				}
			});
		    
		    
			mVideoView.start();
			
			
			break;
		case UI_MODE_TAKING_PICTURE:
			mBtnFlash.setVisibility(View.GONE);
			mBtnFriends.setVisibility(View.GONE);
			mBtnPies.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);
			mBtnTakePicture.setVisibility(View.GONE);
			mBtnSend.setVisibility(View.GONE);
			break;
		case UI_MODE_TAKING_VIDEO:
			mBtnFlash.setVisibility(View.GONE);
			mBtnSwitchCamera.setVisibility(View.GONE);	
			mBtnSend.setVisibility(View.GONE);
			break;
		}
	}
	
	private OnLongClickListener takeVideoListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			PieTalkLogger.i(TAG, "Video start");			
			mTakingVideo = true;
			if (prepareVideoRecorder()) {
				
				mMediaRecorder.start();
				setUIMode(Constants.CameraUIMode.UI_MODE_TAKING_VIDEO);
			} else {
				//TODO: show an error to the user
				releaseMediaRecorder();
			}
			return true;
		}		
	};
	
	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			
		}
	}
	
	private boolean prepareVideoRecorder() {
		
		mMediaRecorder = new MediaRecorder();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		
		mMediaRecorder.setProfile(CamcorderProfile.get(mCameraNumber, CamcorderProfile.QUALITY_HIGH));
		mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
		mMediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());
		mMediaRecorder.setOrientationHint(90);
//		mMediaRecorder.setVideoSize(720,480);
//		mMediaRecorder.setVideoSize(480, 720);
//		mMediaRecorder.setVideoSize(1184,768);
//		mMediaRecorder.setVideoSize(768, 1184);
		
		
		
		
		DisplayMetrics metrics = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		PieTalkLogger.d(TAG, "Width: " + metrics.widthPixels);
		PieTalkLogger.d(TAG, "Height: " + metrics.heightPixels);
//	    params.width =  metrics.widthPixels;
//	    params.height = metrics.heightPixels;
		
		mMediaRecorder.setOnErrorListener(new OnErrorListener() {							
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				PieTalkLogger.e(TAG, "MediaRecorder error");
				
			}
		});
		mMediaRecorder.setOnInfoListener(new OnInfoListener() {
			
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				PieTalkLogger.i(TAG, "MediaRecorer info");
			}
		});
		
		
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException ex) {
			PieTalkLogger.e(TAG, "IllegalStateException preparing MediaRecorder: " + ex.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException ex) {
			PieTalkLogger.e(TAG, "IOException preparing MediaRecorder: " + ex.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
	
	private OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_DOWN) {
	            //PieTalkLogger.i(TAG, "Down");
	        } else if (event.getAction() == MotionEvent.ACTION_UP) {
	        		
	        		if (mTakingVideo) {
	        			PieTalkLogger.i(TAG, "Finished video");
	        			mTakingVideo = false;
	        			mReviewingVideo = true;
	        			try {
		        			mMediaRecorder.stop();
		        			releaseMediaRecorder();	        			
		        			setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
	        			} catch (RuntimeException ex) {
	        				PieTalkLogger.e(TAG, "Error stopping media recorder");
	        				PieTalkAlert.showToast(mActivity, R.string.video_recording_failed, true, true);
	        				setUIMode(Constants.CameraUIMode.UI_MODE_PRE_PICTURE);
	        			}
	        		}
	        }
			return false;
		}		
	};
	
	private PictureCallback mPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			PieTalkLogger.i(TAG, "pic taken");
			if (mTakingVideo) {
				mReviewingVideo = true;
				setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
			}
			else {
				mReviewingPicture = true;
				mPictureData = data;
				setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_PICTURE);
				
				File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					PieTalkLogger.d(TAG, "Error creating media file, check storage permissions");
				}
				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException ex) {
					PieTalkLogger.d(TAG, "File not found: " + ex.getMessage());
				} catch (IOException ex) {
					PieTalkLogger.d(TAG, "Error accessing file: " + ex.getMessage());
				}
			}
		}
	};
	
	//This method will be fired if RecordActivity is opened from an intent
	//This helps becuase it will be called if it's opened a second time from
	//intent which is the case when the user double taps a pie to reply
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mIsReply = intent.getBooleanExtra("isReply", false);
		if (mIsReply) {
			mReplyToUserId = intent.getStringExtra("replyToUserId"); 
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
		}
	};
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		PieTalkLogger.i(TAG, "onResume");
		PieTalkLogger.i(TAG, "Full filepath: " + mFileFullPath);				
		
		int numberOfCams = Camera.getNumberOfCameras();
		mCameraNumber = 0;		
		if (numberOfCams <2 )
			mBtnSwitchCamera.setVisibility(View.GONE);
		else 
			mCameraNumber = PreferencesHandler.GetCameraPreference(getApplicationContext());
		
				
		mCamera = getCameraInstance(mCameraNumber);
		
		mCameraPreview = new CameraPreview(this, mCamera);
		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		//mFrameLayout.removeAllViews();
		mFrameLayout.addView(mCameraPreview);
		
		mFlashOn = PreferencesHandler.GetFlashPreference(getApplicationContext());
		Camera.Parameters params = mCamera.getParameters();
		List<String> flashModes = params.getSupportedFlashModes();
		params.setRotation(90);
		
//		List<Size> sizes = params.getSupportedVideoSizes();
//		for (Size size : sizes) {
//			PieTalkLogger.i(TAG, "Wid: " + size.width + "  Hei: " + size.height);
//		}
		
		if (mFlashOn)
			mBtnFlash.setImageResource(R.drawable.device_access_flash_on);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_on));
		else
			mBtnFlash.setImageResource(R.drawable.device_access_flash_off);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_off));
		if (flashModes == null || flashModes.size() == 0) {
			mBtnFlash.setVisibility(View.GONE);
		} else {
			setCameraFlash(params);
		}
		
		if (mReviewingPicture) {			
			//TODO: Set preview to show picture from file path
			PieTalkLogger.i(TAG, "Path: " + mFileFullPath);
//			File imgFile = new File(mFileFullPath);
//			if (imgFile.exists()) {
//				Bitmap myBitmap = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length);
//				mImageView.setImageBitmap(myBitmap);
//			}
//			
//			mImageView.setVisibility(View.VISIBLE);
//			mCameraPreview.setVisibility(View.GONE);
//			mVideoView.setVisibility(View.GONE);
			setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_PICTURE);
		} else if (mReviewingVideo) {
			//TODO: Set video to play from file path
			mVideoView.setVideoPath(mFileFullPath);
			mVideoView.start();
			mVideoView.setVisibility(View.VISIBLE);
			mCameraPreview.setVisibility(View.GONE);
			setUIMode(Constants.CameraUIMode.UI_MODE_REVIEW_VIDEO);
		} else if (mIsReply){
			setUIMode(Constants.CameraUIMode.UI_MODE_REPLYING);
		} else {
			setUIMode(Constants.CameraUIMode.UI_MODE_PRE_PICTURE);
		}
		
		IntentFilter filter = new IntentFilter();
		//filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		filter.addAction(Constants.BROADCAST_PIES_UPDATED);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		PieTalkLogger.i(TAG, "onPause");
		mCameraPreview.getHolder().removeCallback(mCameraPreview);
		mCamera.release();
		unregisterReceiver(receiver);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			if (intent.getAction().equals(Constants.BROADCAST_PIES_UPDATED)) {
				boolean wasSuccess = intent.getBooleanExtra(Constants.PIES_UPDATE_STATUS, false);
				if (wasSuccess) {
					int count = mPieTalkService.getLocalPies().size();
					
				} else {
					PieTalkAlert.showToast(mActivity, R.string.error_getting_pies, true, true);
				}				
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}
	
	public Camera getCameraInstance(int cameraNumber) {
		Camera c = null;
		try { 
			c = Camera.open(cameraNumber);
		} catch (Exception ex) {
			PieTalkLogger.e(TAG, ex.getMessage());
		}
		return c;
	}
	
	public void tappedFlash(View view) {
		mFlashOn = !mFlashOn;
		PreferencesHandler.SaveFlashPreference(getApplicationContext(), mFlashOn);
		
		if (mFlashOn)
			mBtnFlash.setImageResource(R.drawable.device_access_flash_on);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_on));			
		else
			mBtnFlash.setImageResource(R.drawable.device_access_flash_off);
			//mBtnFlash.setBackgroundDrawable(getResources().getDrawable(R.drawable.device_access_flash_off));
		
		Camera.Parameters params = mCamera.getParameters();
		setCameraFlash(params);
	}
	
	public void tappedSwitchCamera(View view) {
		mCamera.stopPreview();
		mCamera.release();

		if (mCameraNumber == 0)
			mCameraNumber = 1;
		else
			mCameraNumber = 0;
		mCamera = getCameraInstance(mCameraNumber);
		PreferencesHandler.SaveCameraPreference(getApplicationContext(), mCameraNumber);
		mCameraPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.removeAllViews();
		preview.addView(mCameraPreview);
		
		Camera.Parameters params = mCamera.getParameters();
		List<String> flashModes = params.getSupportedFlashModes();
		if (flashModes == null || flashModes.size() == 0) {
			mBtnFlash.setVisibility(View.GONE);
		} else {
			mBtnFlash.setVisibility(View.VISIBLE);
			setCameraFlash(params);
		}
	}
	
	private void setCameraFlash(Camera.Parameters parameters) {
		if (mFlashOn) {
			parameters.setFlashMode(parameters.FLASH_MODE_ON);
			mCamera.setParameters(parameters);
		} else {
			parameters.setFlashMode(parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
		}			
	}
	
	public void tappedPies(View view) {
		startActivity(new Intent(getApplicationContext(), PiesListActivity.class));		
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}
	
	public void tappedFriendsList(View view) {
		startActivity(new Intent(getApplicationContext(), FriendsListActivity.class));		
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}
	
	public void tappedSend(View view) {
		Intent intent = new Intent(mActivity, SendToFriendsActivity.class);
		intent.putExtra("filePath", mFileFullPath);
		intent.putExtra("isPicture", mReviewingPicture);
		intent.putExtra("isVideo", mReviewingVideo);
		intent.putExtra("timeToLive", mSecondsSelected);
		intent.putExtra("isReply", mIsReply);
		intent.putExtra("replyToUserId", mReplyToUserId);
		//startActivity(intent);
		startActivityForResult(intent, Constants.REQUEST_CODE_SEND_TO_FRIENDS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_CODE_SEND_TO_FRIENDS){

			if (resultCode == Constants.RESULT_CODE_PIE_SENT) {
				setUIMode(CameraUIMode.UI_MODE_PRE_PICTURE);
				//startActivity(new Intent(getApplicationContext(), PiesListActivity.class));
				Intent intent = new Intent(mActivity, PiesListActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public void tappedDelete(View view) {
		returnToCameraPreview();
	}
	
	public void tappedTime(View view) {
		DialogFragment newFragment = new NumberPickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}
	
	public void setIsScrolling(boolean isScrolling) {
		mIsScrolling = isScrolling;
	}
	
	public boolean getIsScrolling() {
		return mIsScrolling;
	}
	
	
	
	public static class NumberPickerFragment extends DialogFragment {
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			PieTalkLogger.i("TEST", "Configchanged");
			super.onConfigurationChanged(newConfig);
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			RecordActivity activity = (RecordActivity) getActivity();
			NumberPicker picker = new NumberPicker(activity);
			picker.setMinValue(1);
			picker.setMaxValue(10);
			picker.setWrapSelectorWheel(false);
			picker.setValue(activity.getSelectedSeconds());
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(activity);
			builder.setView(picker);

			final AlertDialog dialog = builder.create();
			//Place the dialog at the bottom of the screen
			Window window = dialog.getWindow();			
			WindowManager.LayoutParams wlp = window.getAttributes();
			wlp.gravity = Gravity.BOTTOM;
			window.setAttributes(wlp);
			
			
			//Record if we're scrolling for key down detection logic
			picker.setOnScrollListener(new OnScrollListener() {				
				@Override
				public void onScrollStateChange(NumberPicker view, int scrollState) {
					RecordActivity activity = (RecordActivity) getActivity();
					switch (scrollState) {
					case OnScrollListener.SCROLL_STATE_FLING:
						activity.setIsScrolling(true);
						break;
					case OnScrollListener.SCROLL_STATE_IDLE:
						activity.setIsScrolling(false);
						break;
					case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
						activity.setIsScrolling(true);
						break;
					}					
				}
			});
			picker.setOnValueChangedListener((RecordActivity) getActivity());
			picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

			picker.setDisplayedValues(getResources().getStringArray(R.array.share_seconds_list));
			
			
			picker.setOnTouchListener(new OnTouchListener() {				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					RecordActivity activity = (RecordActivity) getActivity();
					if (event.getAction() == MotionEvent.ACTION_UP) {
						if (!activity.getIsScrolling()) {
							//Check to see if they have clicked into the 
							//area specific to the middle row
							//This is a little hacky but NumberPicker's
							//onClickListener doesn't fire as expected
							float rx = event.getX();
							float ry = event.getY();
							if (ry > 128 && ry < 256) {
								dialog.dismiss();
							}							
						}
					} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
						PieTalkLogger.i("TEST", "down");
					}
						
					return false;
				}
			});
			return dialog;			
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mReviewingPicture || mReviewingVideo) {
			//releaseMediaRecorder();
			returnToCameraPreview();
			
		} else {
			PieTalkLogger.i(TAG, "back");
			if (mIsReply) {
				PieTalkLogger.i(TAG, "back-reply");
				Intent intent = new Intent(mActivity, PiesListActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);				
		        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
			} else {			
				PieTalkLogger.i(TAG, "back-finish");
				finish();
			}
		}
	}
	
	private void returnToCameraPreview() {
		if (mReviewingVideo) {
			mVideoView.stopPlayback();
			mCameraPreview.setVisibility(View.VISIBLE);
			mVideoView.setVisibility(View.GONE);				
		}
		File file = new File(mFileFullPath);
		mFileFullPath = "";
		if (!file.delete()) {
			PieTalkLogger.e(TAG, "Unable to delete file");
		}
		
		//Ensure friends won't be checked when we return to them
		mPieTalkService.uncheckFriends();
		//TODO: Make sure commenting this doesn't break anything
		//mCamera.startPreview();
		mReviewingPicture = false;
		mReviewingVideo = false;
		if (mIsReply)
			setUIMode(Constants.CameraUIMode.UI_MODE_REPLYING);
		else
			setUIMode(Constants.CameraUIMode.UI_MODE_PRE_PICTURE);
	}
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	private  Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private  File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		
	    mMediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mMediaStorageDir.exists()){
	        if (! mMediaStorageDir.mkdirs()){
	            PieTalkLogger.d(TAG, "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	    		String imageFileName = "IMG_"+ timeStamp + ".jpg";
	    		mFileFullPath = mMediaStorageDir.getPath() + File.separator +
	    		        imageFileName;
	        mediaFile = new File(mFileFullPath);
	    } else if(type == MEDIA_TYPE_VIDEO) {
	    		mVideoFileName = "VID_"+ timeStamp + ".mp4";
	    		mFileFullPath = mMediaStorageDir.getPath() + File.separator +
	    		        mVideoFileName;
	        mediaFile = new File(mFileFullPath);
	        
	    } else {
	        return null;
	    }

	    return mediaFile;
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		PieTalkLogger.i(TAG, "New value: " + newVal);
		mSecondsSelected = newVal;
		mLblTime.setText(newVal + "");
	}
	
	
	public int getSelectedSeconds() {
		return mSecondsSelected;
	}

}

