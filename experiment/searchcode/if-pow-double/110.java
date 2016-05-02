package ru.nntu.facedetection;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.TermCriteria;
import org.opencv.video.Video;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import ru.nntu.client.FTModel;
import ru.nntu.client.PrefEngine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;


public class FaceView extends CameraPreview {
	private final String TAG = "FaceView";

	// OpneCV vars section
	public final int SUBSAMPLING_FACTOR = 4;
	private final int MAX_CORNERS = 3;

	private CascadeClassifier mFaceClassifier;
	private CascadeClassifier mEyesClassifier;

	// magic numbers section
	private final float MIN_FACE_SIZE = 0.5f;
	private final float MIN_EYE_SIZE = 0.01f;
	private final float MAX_EYE_SIZE = 0.4f;
	private final double MAX_ANGLE = 0.40;
	private final int WINDOW_SIZE = 50;
	// eomns

	private List<Rect> mFaces = null;
	private List<Rect> mEyes = null;

	private Mat mRgbaFrameReturn;
	private Mat mRgbaFrame;
	private Mat mCurrentFrame;
	private Mat mPrevFrame;
	
	
	// Main OpenCv objects
	private List<Point> mStartPoints;
	private List<Point> mEndPoints;

	private boolean isFaceDetected = false;
	private Context mContext;

	private double mInitialAngle;
	private double mEyeDistance;
	private double mNoseLength;
	private int mMaxPointsDist;

	List<Byte> features_found = new LinkedList<Byte>();
	List<Float> feature_errors = new LinkedList<Float>();

	// End of OpenCV vars section
	

	// Sensor vars section
	private static Sensor sensor;
    private static SensorManager sensorManager;
    
    public static float mCurAzimuth;
    public static float mInitialAzimuth;
    public static float pitch;
    public static float roll;
    
    /** indicates whether or not Orientation Sensor is running */
    private static boolean running = false;
    
	// End of Sensor vars section
    
    
	private Handler mHandler;

	
	private boolean isStarted;
	private String mCurrentState = "TAP TO START";

	public FaceView(FaceDetectionActivity context) throws IOException {
		super(context);
		mContext = context;

		InputStream is = context.getResources().openRawResource(
				R.raw.haarcascade_frontalface_alt);
		InputStream is1 = context.getResources().openRawResource(
				R.raw.haarcascade_eye_new);

		File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
		File cascadeFile = new File(cascadeDir,
				"haarcascade_frontalface_alt.xml");
		File cascadeFile1 = new File(cascadeDir, "haarcascade_eye.xml");
		FileOutputStream os = new FileOutputStream(cascadeFile);
		FileOutputStream os1 = new FileOutputStream(cascadeFile1);

		Log.i("DEB", "" + cascadeFile1);
		
		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = is.read(buffer)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		is.close();
		os.close();

		Log.i("DEB", "Start read");
		byte[] buffer1 = new byte[4096];
		int bytesRead1;
		while ((bytesRead1 = is1.read(buffer1)) != -1) {
			Log.i("DEB", "Before write");
			os1.write(buffer1, 0, bytesRead1);
			Log.i("DEB", "Write");
		}
		is1.close();
		os1.close();

		// Load cascades
		mFaceClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
		mEyesClassifier = new CascadeClassifier(cascadeFile1.getAbsolutePath());

		cascadeFile.delete();
		cascadeFile1.delete();
		cascadeDir.delete();

		mStartPoints = new LinkedList<Point>();
		mEndPoints = new LinkedList<Point>();
		
		
		sensorManager = (SensorManager)getContext()
				.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager
				.getSensorList(Sensor.TYPE_ORIENTATION);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
			running = sensorManager.registerListener(sensorEventListener,
					sensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		

		mHandler = new Handler((Callback) context);
		
		FTModel.getMolly().washMolly();
	}

	 /**
     * The listener that listen to events from the orientation listener
     */
    private static SensorEventListener sensorEventListener = 
            new SensorEventListener() {
            
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            
            public void onSensorChanged(SensorEvent event) {
                    mCurAzimuth = event.values[0];      // azimuth
                    pitch = event.values[1];        // pitch
                    roll = event.values[2];         // roll
            }
            
    };
	@Override
	public boolean onTouchEvent(MotionEvent mv) {
		isStarted = true;
		mCurrentState = "DETECTING";
		postInvalidate();
		return true;
	}

	@Override
	protected Bitmap processFrame(VideoCapture capture) {
		Log.i(TAG, "processFrame started");
		
		if (mRgbaFrameReturn == null || mRgbaFrame == null || mCurrentFrame == null) {
			mRgbaFrameReturn = new Mat ();
			mRgbaFrame = new Mat ();
			mCurrentFrame = new Mat ();
		}
		
		Log.i("SENS", "az " + mCurAzimuth + " roll " + roll + " pitch " + pitch);
		
		Log.i(TAG, "retrive frames...");
		capture.retrieve(mRgbaFrameReturn, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
		capture.retrieve(mCurrentFrame, Highgui.CV_CAP_ANDROID_GREY_FRAME);
		if (mMaxPointsDist == 0) {
			mMaxPointsDist = mRgbaFrame.width() / 20;
		}
		
		
		mRgbaFrameReturn.copyTo(mRgbaFrame);
		
		if (mRgbaFrameReturn.empty() || mRgbaFrameReturn.height() == 0 || mRgbaFrameReturn.width() == 0) {
			Log.i(TAG, "retrive frames...NOk");
			
			return null;
		} else {
			Log.i(TAG, "retrive frames...Ok");
		}
		
		Log.i(TAG, "frame size " + mCurrentFrame.width() + "," + mCurrentFrame.height());
		
		//===========================================
		Bitmap bmp = Bitmap.createBitmap(mRgbaFrameReturn.cols(), mRgbaFrameReturn.rows(),
				Bitmap.Config.ARGB_8888);
		//===========================================
		
		if (mFaceClassifier == null && mEyesClassifier == null) {
			return null;
		}

		if (!isFaceDetected) {
			Log.i(TAG, "Detecting faces and eyes...");
			
			int faceSize = Math.round(mCurrentFrame.rows() * MIN_FACE_SIZE);

			if (mFaces == null) {
				mFaces = new LinkedList<Rect>();
			}
			
			Log.i(TAG, "start mFaceClassifier");
			mFaceClassifier.detectMultiScale(mCurrentFrame, mFaces, 1.1, 2, 2, 
					                         new Size(faceSize, faceSize));
			Log.i(TAG, "end mFaceClassifier");
			
			for (Rect r : mFaces)
				Core.rectangle(mRgbaFrameReturn, r.tl(), r.br(), new Scalar(0, 255,
						0, 255), 3);

			Log.i(TAG, "Faces total " + mFaces.size());
			if (mFaces.size() != 1) {
				Log.i(TAG, "Incorrect face count: " + mFaces.size());

				mFaces.clear();

				if (Utils.matToBitmap(mRgbaFrameReturn, bmp))
					return bmp;
				else
					return null;
			}
						
			if (mEyes == null) {
				mEyes = new LinkedList<Rect>();
			}
			
			Mat tmpForEyesFrame = mCurrentFrame.submat(mFaces.get(0));

			int eyeMinSize = Math.round(tmpForEyesFrame.height() * MIN_EYE_SIZE);
			int eyeMaxSize = Math.round(tmpForEyesFrame.height() * MAX_EYE_SIZE);

			Log.i(TAG, "start mEyesClassifier");
			mEyesClassifier.detectMultiScale(tmpForEyesFrame, mEyes, 1.1, 4, 2, 
					                         new Size(eyeMinSize, eyeMinSize),
					                         new Size(eyeMaxSize, eyeMaxSize));
			Log.i(TAG, "end mEyesClassifier");
			
			
			tmpForEyesFrame.release();
			
			for (int i = 0; i < mEyes.size(); i++ ) {
				mEyes.get(i).x = mEyes.get(i).x + mFaces.get(0).x;
				mEyes.get(i).y = mEyes.get(i).y + mFaces.get(0).y;
				Core.rectangle(mRgbaFrameReturn, mEyes.get(i).tl(), mEyes.get(i).br(), new Scalar(0, 0,
						255, 255), 3);
			}

			Log.i(TAG, "Eyes total " + mEyes.size());
			if (mEyes.size() != 2) {
				Log.i(TAG, "Incorrect eyes count: " + mEyes.size());
				
				mEyes.clear();
				mFaces.clear();
				
				if (Utils.matToBitmap(mRgbaFrameReturn, bmp))
					return bmp;
				else
					return null;
			}

			//We have found the face
			FTModel.getMolly().setFaceFind(true);
			
			if (!FTModel.getMolly().isFaceSaved()) {
				
				Bitmap faceBmp = Bitmap.createBitmap(mRgbaFrame.cols(), mRgbaFrame.rows(),
						Bitmap.Config.ARGB_8888);
			
				Utils.matToBitmap(mRgbaFrame, faceBmp);
				File dir = mContext.getDir("tmp", Context.MODE_PRIVATE);
				File filename = new File(dir, "tmp.jpg");
				
				FTModel.getMolly().setFilename(filename.getAbsolutePath());
				FTModel.getMolly().setBitmap(faceBmp);
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(filename);
				} catch (FileNotFoundException e) {
					FTModel.getMolly().setFaceValidated(false);
					return bmp;
				}
				faceBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				Log.i(TAG, "Bitmap was compressed");
				
				out = null;
				
				FTModel.getMolly().setFaceSaved(true);
            }

			
			
			if (!PrefEngine.getInstance().isValidationEnabled()) {
				FTModel.getMolly().setFaceValidated(true);
				notifySuccess();
				
				if (Utils.matToBitmap(mRgbaFrameReturn, bmp))
					return bmp;
				else
					return null;
			}
			
			prepareFaceTracking();
			
		} else {
			// Else validate the face
			
			Log.i(TAG, "Before flow");

			if (mPrevFrame == null || 
				mCurrentFrame == null || 
				mStartPoints == null ||  
				mEndPoints == null) {
				Log.e(TAG, "NULL");
				return null;
			}
			Log.e(TAG, "Previous" + mPrevFrame.width() + mPrevFrame.height());
			Log.e(TAG, "Current" + mCurrentFrame.width() + mCurrentFrame.height());
		
			for (int i = 0; i < MAX_CORNERS; ++i) {
				Log.i(TAG, "start x=" + mStartPoints.get(i).x
						+ "start y=" + mStartPoints.get(i).y);
			}

			
			Video.calcOpticalFlowPyrLK(
					mPrevFrame,
					mCurrentFrame,
					mStartPoints,
					mEndPoints,
					features_found,
					feature_errors,
					new Size(WINDOW_SIZE, WINDOW_SIZE),
					3,
					new TermCriteria(TermCriteria.MAX_ITER | TermCriteria.EPS,
							20, 0.3), 0.0);
	
			for (int i = 0; i < MAX_CORNERS; ++i) {
				Log.i(TAG, "end x=" + mEndPoints.get(i).x + "end y="
						+ mEndPoints.get(i).y);
			}
			Log.i(TAG, "After flow");

			checkPoints(mStartPoints, mEndPoints);
			if (!isFaceDetected) {
				return bmp;
			}

			double curAngle = getAngle(mEndPoints);
			
			float azmDiff = (float) Math.abs(mCurAzimuth - mInitialAzimuth);
			
			if (azmDiff > 200) {
				azmDiff = 360 - azmDiff;
			}
			
			Log.i("ANGLE", "angle = " + curAngle);
			Log.i("ANGLEGRAD", "angle = " + (curAngle* 57.295779513));
			Log.i("AZIMUTH", "azmDiff = " + azmDiff);
			Log.i("DIFF", "diff = " + (azmDiff - (curAngle * 57.295779513)));
			if ((curAngle - mInitialAngle) >= MAX_ANGLE) {
				//if (Math.abs(azmDiff - (curAngle * 57.295779513)) <= 13) {
					notifySuccess();
				//} else {
//					notifyLostPoints();
//				}
				return bmp;
			}

			
			Log.i(TAG, "Start coping points and frames");
			for (int i = 0; i < MAX_CORNERS; i++) {
//				if (features_found[i] == 0 || feature_errors[i] > 1000) {
//					notifyLostPoints();
//					return;
//				}

				Core.line(mRgbaFrameReturn, mStartPoints.get(i), mEndPoints.get(i), new Scalar(0, 255,
						0, 255), 4);
				// Make cur features as previous
				mStartPoints.set(i, mEndPoints.get(i));
				
			}

			mCurrentFrame.copyTo(mPrevFrame);
			Log.i(TAG, "End coping points and frames");
		}
		
		if (Utils.matToBitmap(mRgbaFrameReturn, bmp))
			return bmp;
		else
			return null;
	}

	@Override
	public void run() {
		super.run();

	}


	private void prepareFaceTracking() {
		Log.w(TAG, "Starting checking. Initializing.");
		Log.i(TAG, "----------------------------------");
		Log.i(TAG, "----------------------------------");
		Log.i(TAG, "----------------------------------");


		//Add first eye
		mStartPoints.add(new Point(mEyes.get(0).x + mEyes.get(0).width / 2, 
				                   mEyes.get(0).y + mEyes.get(0).height / 2));
		
		//Add second eye
		mStartPoints.add(new Point(mEyes.get(1).x + mEyes.get(1).width / 2, 
                				   mEyes.get(1).y + mEyes.get(1).height / 2));

		
		// set destination between eyes
		mEyeDistance = _getDst(mStartPoints, mStartPoints, 0, 1);

		if ((mEyeDistance / (double)mFaces.get(0).width) < 0.1) {
			Log.i(TAG, "Eyes distance too small (" + mEyeDistance + ")");
			
			return;
		}

		// noseLen should be initialized before using _getAngle
		mNoseLength = mEyeDistance * 0.6; // nose length (0.6 of distance
											// between eyes)
		double noseHeight = mEyeDistance * 0.56;

		Log.i(TAG, "Eyes distance = " + mEyeDistance);
		Log.i(TAG, "Nose length = " + mNoseLength);
		Log.i(TAG, "Nose height = " + noseHeight);

		int eyeMidPointX = +((mEyes.get(0).x + mEyes.get(0).width / 2) + (mEyes
				.get(1).x + mEyes.get(1).width / 2)) / 2;

		int eyeMidPointY = +((mEyes.get(0).y + mEyes.get(0).height / 2) + (mEyes
				.get(1).y + mEyes.get(1).height / 2)) / 2;

		double eyeLineAngle = Math
				.atan((double) (mEyes.get(1).y - mEyes.get(0).y)
						/ (double) (mEyes.get(1).x - mEyes.get(0).x));

		eyeLineAngle += 0.0001;

		int nosePointX = 0;

		if (mEyes.get(0).x > mEyes.get(1).x) {
			if (mEyes.get(0).y > mEyes.get(1).y) {
				nosePointX = (int) (eyeMidPointX - Math.abs(Math
						.sin(eyeLineAngle) * noseHeight));
			} else {
				nosePointX = (int) (eyeMidPointX + Math.abs(Math
						.sin(eyeLineAngle) * noseHeight));
			}
		} else {
			if (mEyes.get(0).y > mEyes.get(1).y) {
				nosePointX = (int) (eyeMidPointX + Math.abs(Math
						.sin(eyeLineAngle) * noseHeight));
			} else {
				nosePointX = (int) (eyeMidPointX - Math.abs(Math
						.sin(eyeLineAngle) * noseHeight));
			}
		}

		int nosePointY = (int) Math.abs((eyeMidPointY + Math.cos(eyeLineAngle)
				* noseHeight));

		//Add nose point
		mStartPoints.add(new Point (nosePointX, nosePointY));

		Log.i(TAG, "Points were calculated");

		for (Point p : mStartPoints) {
			mEndPoints.add(p);
		}
		
		Log.i(TAG, "End points were copied");

		//Get the initial angle of head
		mInitialAngle = getAngle(mStartPoints);
		Log.i(TAG, "FirstAngle from getAngle() = " + mInitialAngle);

		mInitialAzimuth = mCurAzimuth;
		Log.i(TAG, "FirstAzimuth = " + mInitialAzimuth);
		
		// save current frame
		if (mPrevFrame == null) {

			mPrevFrame = new Mat (mCurrentFrame.rows(),
					mCurrentFrame.cols(), CvType.CV_8UC1);
			
			Log.i(TAG, "Init prev frame");
		}

		mCurrentFrame.copyTo(mPrevFrame);
		
		Log.i(TAG, "Frames were cloned");

		isFaceDetected = true;

		Log.i(TAG, "----------------------------------");
		Log.i(TAG, "----------------------------------");
		Log.i(TAG, "----------------------------------");

		notifyStartTracking();
	}


	private void notifyLostPoints() {
		Log.e(TAG, "notilfyLost");
		isFaceDetected = false;
		isStarted = false;
		mCurrentState = "POINTS LOST. TAP TO START";
		//postInvalidate();
		mHandler.sendMessage(mHandler
				.obtainMessage(FaceDetectionActivity.MSG_LOST));
		FTModel.getMolly().setFaceValidated(false);
	}

	private void notifySuccess() {
		Log.e(TAG, "notilfySuccess");
		mCurrentState = "RECOGNITION SUCCESS";
		
		
		FTModel.getMolly().setFaceValidated(true);
		mHandler.sendMessage(mHandler
				.obtainMessage(FaceDetectionActivity.MSG_SUCCESS));
		
		isStarted = false;

		Log.i(TAG, "width = " + mCurrentFrame.width());
		Log.i(TAG, "height = " + mCurrentFrame.height());
		Log.i(TAG, "finished copying");
	}

	private void notifyStartTracking() {
		Log.e(TAG, "notilfyStartTracking");
		mCurrentState = "TRACKING...";
		//postInvalidate();
		mHandler.sendMessage(mHandler
				.obtainMessage(FaceDetectionActivity.MSG_START));
	}

	private double getAngle(List<Point> facePoints) {
		Log.i(TAG, "start getAngle()");
		double fEyeX, sEyeX, noseX, fEyeY, sEyeY, noseY;
		double eyeMidPointX, eyeMidPointY;
		double noseOrtX, noseOrtY;

		fEyeX = facePoints.get(0).x;
		fEyeY = facePoints.get(0).y;

		sEyeX = facePoints.get(1).x;
		sEyeY = facePoints.get(1).y;

		noseX = facePoints.get(2).x;
		noseY = facePoints.get(2).y;
		
		eyeMidPointX = (fEyeX + sEyeX) / 2;
		eyeMidPointY = (fEyeY + sEyeY) / 2;

		// Calculate coordinates of nose projection on eyes line
		double fDenominator = Math.pow((sEyeX - fEyeX), 2)
				+ Math.pow((sEyeY - fEyeY), 2);

		double t = (noseX * (sEyeX - fEyeX) - (sEyeX - fEyeX) * fEyeX + noseY
				* (sEyeY - fEyeY) - (sEyeY - fEyeY) * fEyeY)
				/ fDenominator;

		noseOrtX = fEyeX + (sEyeX - fEyeX) * t;
		noseOrtY = fEyeY + (sEyeY - fEyeY) * t;

		// distance between middle point between eyes and nose projection
		double ortMidDest = _getDst(noseOrtX, noseOrtY, eyeMidPointX,
				eyeMidPointY);

		double angle = Math.atan(ortMidDest / mNoseLength);

		Log.i(TAG, "end getAngle()");
		return angle;
	}

	// check positions of each point
	private void checkPoints(List<Point> pA, List<Point> pB) {
		Log.i(TAG, "start checkPoints()");
		for (int i = 0; i < MAX_CORNERS; i++) {
			if (pA.get(i).x < 5 || pB.get(i).x < 5
					|| pA.get(i).y < 5 || pB.get(i).y < 5
					|| pA.get(i).x > mCurrentFrame.width()
					|| pB.get(i).x > mCurrentFrame.width()
					|| pA.get(i).y > mCurrentFrame.height()
					|| pB.get(i).y > mCurrentFrame.height()) {

				Log.i(TAG, "checkPoints: Poits are out of borders!!");
				notifyLostPoints();
				return;

			}
		}

		// Check nose position
		if (pB.get(2).y < pB.get(0).y
				|| pB.get(2).y < pB.get(1).y) {
			// Nose is not at the right place
			Log.i(TAG, "checkPoints: Incorrect nose position!!");
			notifyLostPoints();
			return;
		}

		// Check distance between eyes (check size of face)
		double curEyeDst = _getDst(pB, pB, 0, 1);
		if ((curEyeDst / mEyeDistance) < 0.65) {
			// face is too small
			Log.i(TAG, "checkPoints: Face is too small!!");
			notifyLostPoints();
			return;
		}

		// Check distance between current and previous points
		for (int i = 0; i < MAX_CORNERS; i++) {
			if (_getDst(pA, pB, i, i) > mMaxPointsDist) {
				// Point has "jumped" somewhere
				Log.i(TAG, "checkPoints: Some point has \"jumped\"!!");
				notifyLostPoints();
				return;
			}
		}
	
		Log.i(TAG, "end checkPoints()");
		return;
	}

	private double _getDst(List<Point> pA, List<Point> pB, int pNumA,
			int pNumB) {
		double dst = Math
				.sqrt(Math.pow(
						(pA.get(pNumA).x - pB.get(pNumB).x), 2)
						+ Math.pow((pA.get(pNumA).y - pB.get(pNumB).y), 2));

		return dst;
	}

	private double _getDst(double aX, double aY, double bX, double bY) {
		return Math.sqrt(Math.pow((aX - bX), 2) + Math.pow((aY - bY), 2));
	}
	
    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);

        synchronized (this) {
            // initialize Mats before usage
            mCurrentFrame = new Mat();
            mRgbaFrameReturn = new Mat();
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder _holder) {
    	super.surfaceDestroyed(_holder);
		synchronized (this) {
			try {
				if (sensorManager != null && sensorEventListener != null) {
					sensorManager.unregisterListener(sensorEventListener);
				}
			} catch (Exception e) {
			}
		}
	}
    
}

