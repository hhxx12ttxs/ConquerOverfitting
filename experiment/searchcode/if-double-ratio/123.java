/**
 * @author brinkmwj
 * 
 * Copyright Bo Brinkman, 2012.
 * 
 * This code may be used for any purpose whatsoever, without limitation, EXCEPT that
 * portions of this code are based on http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/graphics/CameraPreview.html
 * 
 * As a result, this code is also bound by the copyright and license listed below.
 */
/*
 * Portions Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bobrinkman.themirror;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class TheMirror extends Activity {
	private Camera mCamera;
	private Preview mSV;

    /** 
     * Called automatically when the activity is first created. 
     * Do NOT put camera initialization code here, though, because
     *  you need to de-init the camera in onPause, and so camera
     *  init should be in onResume.
     *  
     *  Overrides SurfaceView's version, so be sure to use super to call it first.
     * */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSV = new Preview(this);
        setContentView(mSV);
    }
    
    /**
     * Called automatically whenever you app comes to the foreground.
     * 
     * Overrides SurfaceView's version, so be sure to use super to call it first.
     */
    protected void onResume() {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        mCamera = Camera.open();
        mSV.setCamera(mCamera);
    }

    /**
     * Called automatically whenever your app goes to the background. Anything big probably
     * ought to be deallocated here.
     * 
     * TODO: Set b and pixels to null so they can be garbage collected
     */
    protected void onPause() {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mSV.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }
    
	/**
	 * This is the main class that does most of the work.
	 * The SurfaceView class is a View that supports display of camera
	 * previews. Depending on your application you may allow the camera to
	 * directly display the preview on your surface (which is what most of the
	 * online examples show), but in my code I'm actually drawing a bitmap over
	 * top of the preview, so that you can see the image processing I'm doing.
	 * 
	 * @author brinkmwj
	 *
	 */
    public class CameraPreviewView extends SurfaceView {

        protected final Paint rectanglePaint = new Paint();
        
        /**
         * Two member variables to facilitate drawing the preview
         * data on the screen. By allocating these once (not every frame)
         * the app is much faster.
         */
        private Bitmap b;
        private int[] pixels;
        
        public CameraPreviewView(Context context) {
            super(context);
            rectanglePaint.setARGB(255, 200, 0, 0);
            rectanglePaint.setStyle(Paint.Style.FILL);
            rectanglePaint.setStrokeWidth(2);
        }

        /**
         * Overrides the onDraw method of SurfaceView. This function is called
         * automatically by the operating system whenever it determines the surface
         * needs to be redrawn (usually this is because you called invalidate)
         */
        protected void onDraw(Canvas canvas){
        	if(b != null){
        		/*
        		 * Example of how to draw a bitmap on the canvas. Note that you will
        		 * usually want to use the version that takes Rect src and Rect dst
        		 * because the screen size and preview size usually don't match
        		 */
        		canvas.drawBitmap(b, 0.0f, 0.0f,null);
        	}
        	
        	/*
        	 * Example of how to draw simple shapes on the canvas
        	 */
            canvas.drawRect(new Rect(10,10,200,200), rectanglePaint);
            
            /*
             * Example of how to write to the log file. View the log using
             * "adb logcat"
             */
            Log.w(this.getClass().getName(), "On Draw Called");
        }
        
        /**
         * Call this from your onPreviewFrame method to send the preview
         * data to the view. This will also post an invalidate to the view
         * to cause onDraw to be called.
         * 
         * @param frame an array of pixel data in NV21 format
         */
        public void updateBitmap(byte[] frame){
        	if(b != null){
        		for(int i=0;i<pixels.length;i++){
        			//This nonsense is due to Java not having unsigned bytes.
        			// I'm sure there is a better way to do this.
        			int q =frame[i];
        			if(q < 0) q = q+256;
        			
        			//First, copy the color of pixel i into the pixels array
        			//Right now I'm just doing gray-scale, but it is possible
        			// to get color information as well. The pixel format is ARGB,
        			// so the first byte sets the transparency.
        			pixels[i] = (255<<24) | (q<<16) | (q<<8) | (q<<0);
        			
        			//Next, do some simple edge-detection
        			if(i > 0){
        				int qp = pixels[i-1] % 256;
        				qp = (255 + (qp - q))/2;
        				pixels[i-1] = 0xFF000000 | (qp<<16) | (qp<<8) | (qp<<0);; //Edge detection kernel
        			}
        		}
        		
        		//Now that I've updated my array, use it to set the pixels in the Bitmap
        		b.setPixels(pixels,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());
        	}
        	//onDraw only gets called when the Surface is invalidated, so this
        	// will cause onDraw to get called
        	this.invalidate();
        }
        
        /**
         * Call this any time you change the camera preview size to allocate
         *  the Bitmap and the pixel buffer
         *  
         * @param w The width of the camera preview
         * @param h The height of the camera preview
         */
        public void setBitmapSize(int w, int h){
        	b = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        	pixels = new int[w*h];
        }
    }
    
    /**
     * This class manages our main view, but also handles all messages that we care about.
     * We implement Surfaceholder.Callback so that we will get notified if the screen dimensions
     *  change.
     * We implement Camera.PreviewCallback so that we get notified when there is a frame of
     *  camera video to process.
     * @author brinkmwj
     *
     */
    class Preview extends ViewGroup implements SurfaceHolder.Callback, Camera.PreviewCallback {
    	//This is used with logging functions to make it easier to search the log by keyword search
        private final String TAG = "Preview";

        /** The main view */
        CameraPreviewView mSurfaceView;    
        
        /** A utility class that holds a surface */
        SurfaceHolder mHolder;			   
        
        /** The dimensions of the camera preview */
        Size mPreviewSize;			
        
        /** A list of supported sizes. The only reason to make
		 * it a member variable is so that we don't have to
		 * get it every time we need it. */
        List<Size> mSupportedPreviewSizes; 
        /** A reference to the camera, which is passed to us
		 *  from the Activity's onResume method */
        Camera mCamera;	
        /** An array to hold one frame of camera preview data.
		 * We only allocate this once, and then pass it to
		 * the camera to be filled. This avoids having a ton
		 * of garbage collection going on, because the buffer
		 * gets reused repeatedly. */
        byte[] mPF;	

        Preview(Context context) {
            super(context);
            //Create the main view, and add it to our ViewGroup
            mSurfaceView = new CameraPreviewView(context);
            addView(mSurfaceView);
            
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        
        /**
         * Call this method with an already initialized Camera, or with null if there
         * isn't one. Should usually only be called in onPause or onResume.
         * 
         * @param camera A instance of the camera class, or null.
         */
        public void setCamera(Camera camera) {
            mCamera = camera;
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                requestLayout(); //Once we know which camera sizes are supported, we want the
                				 // viewGroup to re-do its layout to match
            }
        }

        /**
         * If you wish to switch from one camera to another (if your device has multiple
         * cameras) you can use this method. It is not currently used.
         * @param camera
         */
        public void switchCamera(Camera camera) {
           setCamera(camera);
           try {
               camera.setPreviewDisplay(mHolder);
           } catch (IOException exception) {
               Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
           }
           Camera.Parameters parameters = camera.getParameters();
           parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
           requestLayout(); //Once we get the new preview size, redo the layout to match

           camera.setParameters(parameters);
        }

       /**
        * This method is called automatically whenever the view is trying to determine
        * how much space we need for our display. You MUST call setMeasuredDimension
        * with the width and height of the thing you want to display. In our case, we want
        * to take up the whole screen.
        * 
        * As a side effect, once we have figured out how much space we have,
        * we go and find the best preview size for our screen, and then allocate a buffer
        * for camera preview.
        */
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because this class acts as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
                //Allocate a buffer to hold the camera preview data
                mPF = new byte[(mPreviewSize.height * mPreviewSize.width * ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat()))/8];
                //Add the buffer to the buffer queue, for use with setPreviewCallbackWithBuffer
                mCamera.addCallbackBuffer(mPF);
            }
        }

        /**
         * This code re-calculated the layout of the viewGroup whenever the screen
         * or camera resolution changes.
         */
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed && getChildCount() > 0) {
                final View child = getChildAt(0);

                final int width = r - l;
                final int height = b - t;

                int previewWidth = width;
                int previewHeight = height;
                if (mPreviewSize != null) {
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                }

                // Center the child SurfaceView within the parent.
                if (width * previewHeight > height * previewWidth) {
                    final int scaledChildWidth = previewWidth * height / previewHeight;
                    child.layout((width - scaledChildWidth) / 2, 0,
                            (width + scaledChildWidth) / 2, height);
                } else {
                    final int scaledChildHeight = previewHeight * width / previewWidth;
                    child.layout(0, (height - scaledChildHeight) / 2,
                            width, (height + scaledChildHeight) / 2);
                }
            }
        }

        /** 
         * Automatically called once the Surface has actually been created by the OS.
         * Note that this comes some time after the constructor, once the surface REALLY
         * exists.
         * The Surface has been created, acquire the camera and tell it where
         * to draw. */
        public void surfaceCreated(SurfaceHolder holder) {
            //This is important magic! Normally, a SurfaceView that is used as a
        	// camera preview does not draw itself, it is drawn by the Camera. Hence,
        	// normally, the onDraw() method never gets called. By setting this to false
        	// we ensure that our over-ridden version of CameraPreviewView.onDraw gets called
        	mSurfaceView.setWillNotDraw(false);
        	
            try {
                if (mCamera != null) {
                	//In some phones, you cannot start the camera preview without a
                	// preview display holder. (In particular, Motorola phones require this,
                	// HTC phones do not.)
                    mCamera.setPreviewDisplay(holder);
                }
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            }
        }

        /**
         * Automatically called by Android when the surface is about to be destroyed.
         */
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
            	//If you don't stopPreview you run the risk of crashes and/or
            	// running down your battery.
                mCamera.stopPreview();
            }
        }

        /**
         * Based on the current screen space available and the supported preview sizes,
         * pick one that is a close match. Because different preview sizes can have different
         * aspect ratios this is non-trivial. This code is mostly based on the demo code, as
         * cited in the copyright notice at the top of the file.
         * 
         * @param sizes List of valid preview sizes, from Camera.Parameters.getSupportedPreviewFormats.
         * The reason we pass this, instead of just calling getSupportedPreviewFormats, is so that the caller
         * can rule out any preview formats that they don't like beforehand.
         * @param w The width of the available space for the preview
         * @param h The height of the available space for the preview
         * @return The dimensions that we suggest for the preview
         */
        private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) w / h;
            if (sizes == null) return null;

            Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            // Try to find an size match aspect ratio and size
            for (Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            // Cannot find the one match the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }
            return optimalSize;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // Now that the size is known, set up the camera parameters and begin
            // the preview.
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            requestLayout();
            mSurfaceView.setBitmapSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
            
            //Note that once we start the preview, it don't do anything until we
            // add a callback.
            mCamera.startPreview();
            //Even once we tell the camera to call us back with preview frames,
            // it won't do so until we add a buffer with addCallbackBuffer
            // By using a single buffer, instead of double-buffering, we ensure
            // that there are no race conditions. If you try to use double-buffering
            // to increase performance, you will need to use synchronization to avoid
            // race conditions.
            mCamera.setPreviewCallbackWithBuffer(this);
        }

        /**
         * Because we set this class to be called back by the camera (using
         * setPerviewCallbackWithBuffer), this will automatically be called by the camera
         * as soon as a frame of video data is available.
         * 
         * @param arg0 An array of bytes representing the picture. Should be in NV21 format
         * @param arg1 The Camera class that took the picture. The main purpose of this is
         * so that we can double-check that the ImageFormat is what we expected before
         * trying to decode the image
         */
		public void onPreviewFrame(byte[] arg0, Camera arg1) {
			Camera.Parameters p = arg1.getParameters();
			if(p.getPreviewFormat() != ImageFormat.NV21){
				Log.e(TAG, "Expected NV21 image format, but got: " + p.getPreviewFormat());
			}
			
			//On each frame of data, first pass the data to the SurfaceView, which will copy
			// it into its own buffer. We want to make a copy because as soon as we pass this buffer
			// back to the camera, it will get overwritten, which can cause screen tearing and
			// other weird effects.
			mSurfaceView.updateBitmap(arg0);
			
			//This check is probably not needed. We should never get a frame of preview data
			// unless it is in our byte array mPF ... but just in case, double check. In other
			// words, arg0 and mPF should always be the same array, so we are using == to check
			// that these two variables are both references to the same array.
			if(arg0 == mPF){
				//Now that we have copied the data out of mPF, we can add it back to the
				// queue of CallbackBuffers, so that the Camera can go get the next frame of
				// video
				mCamera.addCallbackBuffer(mPF);
			}
		}

    }
}
