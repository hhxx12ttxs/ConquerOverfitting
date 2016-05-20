/*
 * Copyright (C) 2008 The Android Open Source Project
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

package android.hardware;

import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.IAudioService;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Camera class is used to set image capture settings, start/stop preview,
 * snap pictures, and retrieve frames for encoding for video.  This class is a
 * client for the Camera service, which manages the actual camera hardware.
 *
 * <p>To access the device camera, you must declare the
 * {@link android.Manifest.permission#CAMERA} permission in your Android
 * Manifest. Also be sure to include the
 * <a href="{@docRoot}guide/topics/manifest/uses-feature-element.html">&lt;uses-feature></a>
 * manifest element to declare camera features used by your application.
 * For example, if you use the camera and auto-focus feature, your Manifest
 * should include the following:</p>
 * <pre> &lt;uses-permission android:name="android.permission.CAMERA" />
 * &lt;uses-feature android:name="android.hardware.camera" />
 * &lt;uses-feature android:name="android.hardware.camera.autofocus" /></pre>
 *
 * <p>To take pictures with this class, use the following steps:</p>
 *
 * <ol>
 * <li>Obtain an instance of Camera from {@link #open(int)}.
 *
 * <li>Get existing (default) settings with {@link #getParameters()}.
 *
 * <li>If necessary, modify the returned {@link Camera.Parameters} object and call
 * {@link #setParameters(Camera.Parameters)}.
 *
 * <li>If desired, call {@link #setDisplayOrientation(int)}.
 *
 * <li><b>Important</b>: Pass a fully initialized {@link SurfaceHolder} to
 * {@link #setPreviewDisplay(SurfaceHolder)}.  Without a surface, the camera
 * will be unable to start the preview.
 *
 * <li><b>Important</b>: Call {@link #startPreview()} to start updating the
 * preview surface.  Preview must be started before you can take a picture.
 *
 * <li>When you want, call {@link #takePicture(Camera.ShutterCallback,
 * Camera.PictureCallback, Camera.PictureCallback, Camera.PictureCallback)} to
 * capture a photo.  Wait for the callbacks to provide the actual image data.
 *
 * <li>After taking a picture, preview display will have stopped.  To take more
 * photos, call {@link #startPreview()} again first.
 *
 * <li>Call {@link #stopPreview()} to stop updating the preview surface.
 *
 * <li><b>Important:</b> Call {@link #release()} to release the camera for
 * use by other applications.  Applications should release the camera
 * immediately in {@link android.app.Activity#onPause()} (and re-{@link #open()}
 * it in {@link android.app.Activity#onResume()}).
 * </ol>
 *
 * <p>To quickly switch to video recording mode, use these steps:</p>
 *
 * <ol>
 * <li>Obtain and initialize a Camera and start preview as described above.
 *
 * <li>Call {@link #unlock()} to allow the media process to access the camera.
 *
 * <li>Pass the camera to {@link android.media.MediaRecorder#setCamera(Camera)}.
 * See {@link android.media.MediaRecorder} information about video recording.
 *
 * <li>When finished recording, call {@link #reconnect()} to re-acquire
 * and re-lock the camera.
 *
 * <li>If desired, restart preview and take more photos or videos.
 *
 * <li>Call {@link #stopPreview()} and {@link #release()} as described above.
 * </ol>
 *
 * <p>This class is not thread-safe, and is meant for use from one event thread.
 * Most long-running operations (preview, focus, photo capture, etc) happen
 * asynchronously and invoke callbacks as necessary.  Callbacks will be invoked
 * on the event thread {@link #open(int)} was called from.  This class's methods
 * must never be called from multiple threads at once.</p>
 *
 * <p class="caution"><strong>Caution:</strong> Different Android-powered devices
 * may have different hardware specifications, such as megapixel ratings and
 * auto-focus capabilities. In order for your application to be compatible with
 * more devices, you should not make assumptions about the device camera
 * specifications.</p>
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 * <p>For more information about using cameras, read the
 * <a href="{@docRoot}guide/topics/media/camera.html">Camera</a> developer guide.</p>
 * </div>
 */
public class Camera {
    private static final String TAG = "Camera";

    // These match the enums in frameworks/base/include/camera/Camera.h
    private static final int CAMERA_MSG_ERROR            = 0x001;
    private static final int CAMERA_MSG_SHUTTER          = 0x002;
    private static final int CAMERA_MSG_FOCUS            = 0x004;
    private static final int CAMERA_MSG_ZOOM             = 0x008;
    private static final int CAMERA_MSG_PREVIEW_FRAME    = 0x010;
    private static final int CAMERA_MSG_VIDEO_FRAME      = 0x020;
    private static final int CAMERA_MSG_POSTVIEW_FRAME   = 0x040;
    private static final int CAMERA_MSG_RAW_IMAGE        = 0x080;
    private static final int CAMERA_MSG_COMPRESSED_IMAGE = 0x100;
    private static final int CAMERA_MSG_RAW_IMAGE_NOTIFY = 0x200;
    private static final int CAMERA_MSG_PREVIEW_METADATA = 0x400;
    private static final int CAMERA_MSG_FOCUS_MOVE       = 0x800;

    private int mNativeContext; // accessed by native methods
    private EventHandler mEventHandler;
    private ShutterCallback mShutterCallback;
    private PictureCallback mRawImageCallback;
    private PictureCallback mJpegCallback;
    private PreviewCallback mPreviewCallback;
    private PictureCallback mPostviewCallback;
    private AutoFocusCallback mAutoFocusCallback;
    private AutoFocusMoveCallback mAutoFocusMoveCallback;
    private OnZoomChangeListener mZoomListener;
    private FaceDetectionListener mFaceListener;
    private ErrorCallback mErrorCallback;
    private boolean mOneShot;
    private boolean mWithBuffer;
    private boolean mFaceDetectionRunning = false;
    private Object mAutoFocusCallbackLock = new Object();

    /**
     * Broadcast Action:  A new picture is taken by the camera, and the entry of
     * the picture has been added to the media store.
     * {@link android.content.Intent#getData} is URI of the picture.
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_NEW_PICTURE = "android.hardware.action.NEW_PICTURE";

    /**
     * Broadcast Action:  A new video is recorded by the camera, and the entry
     * of the video has been added to the media store.
     * {@link android.content.Intent#getData} is URI of the video.
     */
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_NEW_VIDEO = "android.hardware.action.NEW_VIDEO";

    /**
     * Hardware face detection. It does not use much CPU.
     */
    private static final int CAMERA_FACE_DETECTION_HW = 0;

    /**
     * Software face detection. It uses some CPU.
     */
    private static final int CAMERA_FACE_DETECTION_SW = 1;

    /**
     * Returns the number of physical cameras available on this device.
     */
    public native static int getNumberOfCameras();

    /**
     * Returns the information about a particular camera.
     * If {@link #getNumberOfCameras()} returns N, the valid id is 0 to N-1.
     */
    public static void getCameraInfo(int cameraId, CameraInfo cameraInfo) {
        _getCameraInfo(cameraId, cameraInfo);
        IBinder b = ServiceManager.getService(Context.AUDIO_SERVICE);
        IAudioService audioService = IAudioService.Stub.asInterface(b);
        try {
            if (audioService.isCameraSoundForced()) {
                // Only set this when sound is forced; otherwise let native code
                // decide.
                cameraInfo.canDisableShutterSound = false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Audio service is unavailable for queries");
        }
    }
    private native static void _getCameraInfo(int cameraId, CameraInfo cameraInfo);

    /**
     * Information about a camera
     */
    public static class CameraInfo {
        /**
         * The facing of the camera is opposite to that of the screen.
         */
        public static final int CAMERA_FACING_BACK = 0;

        /**
         * The facing of the camera is the same as that of the screen.
         */
        public static final int CAMERA_FACING_FRONT = 1;

        /**
         * The direction that the camera faces. It should be
         * CAMERA_FACING_BACK or CAMERA_FACING_FRONT.
         */
        public int facing;

        /**
         * <p>The orientation of the camera image. The value is the angle that the
         * camera image needs to be rotated clockwise so it shows correctly on
         * the display in its natural orientation. It should be 0, 90, 180, or 270.</p>
         *
         * <p>For example, suppose a device has a naturally tall screen. The
         * back-facing camera sensor is mounted in landscape. You are looking at
         * the screen. If the top side of the camera sensor is aligned with the
         * right edge of the screen in natural orientation, the value should be
         * 90. If the top side of a front-facing camera sensor is aligned with
         * the right of the screen, the value should be 270.</p>
         *
         * @see #setDisplayOrientation(int)
         * @see Parameters#setRotation(int)
         * @see Parameters#setPreviewSize(int, int)
         * @see Parameters#setPictureSize(int, int)
         * @see Parameters#setJpegThumbnailSize(int, int)
         */
        public int orientation;

        /**
         * <p>Whether the shutter sound can be disabled.</p>
         *
         * <p>On some devices, the camera shutter sound cannot be turned off
         * through {@link #enableShutterSound enableShutterSound}. This field
         * can be used to determine whether a call to disable the shutter sound
         * will succeed.</p>
         *
         * <p>If this field is set to true, then a call of
         * {@code enableShutterSound(false)} will be successful. If set to
         * false, then that call will fail, and the shutter sound will be played
         * when {@link Camera#takePicture takePicture} is called.</p>
         */
        public boolean canDisableShutterSound;
    };

    /**
     * Creates a new Camera object to access a particular hardware camera. If
     * the same camera is opened by other applications, this will throw a
     * RuntimeException.
     *
     * <p>You must call {@link #release()} when you are done using the camera,
     * otherwise it will remain locked and be unavailable to other applications.
     *
     * <p>Your application should only have one Camera object active at a time
     * for a particular hardware camera.
     *
     * <p>Callbacks from other methods are delivered to the event loop of the
     * thread which called open().  If this thread has no event loop, then
     * callbacks are delivered to the main application event loop.  If there
     * is no main application event loop, callbacks are not delivered.
     *
     * <p class="caution"><b>Caution:</b> On some devices, this method may
     * take a long time to complete.  It is best to call this method from a
     * worker thread (possibly using {@link android.os.AsyncTask}) to avoid
     * blocking the main application UI thread.
     *
     * @param cameraId the hardware camera to access, between 0 and
     *     {@link #getNumberOfCameras()}-1.
     * @return a new Camera object, connected, locked and ready for use.
     * @throws RuntimeException if opening the camera fails (for example, if the
     *     camera is in use by another process or device policy manager has
     *     disabled the camera).
     * @see android.app.admin.DevicePolicyManager#getCameraDisabled(android.content.ComponentName)
     */
    public static Camera open(int cameraId) {
        return new Camera(cameraId);
    }

    /**
     * Creates a new Camera object to access the first back-facing camera on the
     * device. If the device does not have a back-facing camera, this returns
     * null.
     * @see #open(int)
     */
    public static Camera open() {
        int numberOfCameras = getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                return new Camera(i);
            }
        }
        return null;
    }

    Camera(int cameraId) {
        mShutterCallback = null;
        mRawImageCallback = null;
        mJpegCallback = null;
        mPreviewCallback = null;
        mPostviewCallback = null;
        mZoomListener = null;

        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        native_setup(new WeakReference<Camera>(this), cameraId);
    }

    /**
     * An empty Camera for testing purpose.
     */
    Camera() {
    }

    protected void finalize() {
        release();
    }

    private native final void native_setup(Object camera_this, int cameraId);
    private native final void native_release();


    /**
     * Disconnects and releases the Camera object resources.
     *
     * <p>You must call this as soon as you're done with the Camera object.</p>
     */
    public final void release() {
        native_release();
        mFaceDetectionRunning = false;
    }

    /**
     * Unlocks the camera to allow another process to access it.
     * Normally, the camera is locked to the process with an active Camera
     * object until {@link #release()} is called.  To allow rapid handoff
     * between processes, you can call this method to release the camera
     * temporarily for another process to use; once the other process is done
     * you can call {@link #reconnect()} to reclaim the camera.
     *
     * <p>This must be done before calling
     * {@link android.media.MediaRecorder#setCamera(Camera)}. This cannot be
     * called after recording starts.
     *
     * <p>If you are not recording video, you probably do not need this method.
     *
     * @throws RuntimeException if the camera cannot be unlocked.
     */
    public native final void unlock();

    /**
     * Re-locks the camera to prevent other processes from accessing it.
     * Camera objects are locked by default unless {@link #unlock()} is
     * called.  Normally {@link #reconnect()} is used instead.
     *
     * <p>Since API level 14, camera is automatically locked for applications in
     * {@link android.media.MediaRecorder#start()}. Applications can use the
     * camera (ex: zoom) after recording starts. There is no need to call this
     * after recording starts or stops.
     *
     * <p>If you are not recording video, you probably do not need this method.
     *
     * @throws RuntimeException if the camera cannot be re-locked (for
     *     example, if the camera is still in use by another process).
     */
    public native final void lock();

    /**
     * Reconnects to the camera service after another process used it.
     * After {@link #unlock()} is called, another process may use the
     * camera; when the process is done, you must reconnect to the camera,
     * which will re-acquire the lock and allow you to continue using the
     * camera.
     *
     * <p>Since API level 14, camera is automatically locked for applications in
     * {@link android.media.MediaRecorder#start()}. Applications can use the
     * camera (ex: zoom) after recording starts. There is no need to call this
     * after recording starts or stops.
     *
     * <p>If you are not recording video, you probably do not need this method.
     *
     * @throws IOException if a connection cannot be re-established (for
     *     example, if the camera is still in use by another process).
     */
    public native final void reconnect() throws IOException;

    /**
     * Sets the {@link Surface} to be used for live preview.
     * Either a surface or surface texture is necessary for preview, and
     * preview is necessary to take pictures.  The same surface can be re-set
     * without harm.  Setting a preview surface will un-set any preview surface
     * texture that was set via {@link #setPreviewTexture}.
     *
     * <p>The {@link SurfaceHolder} must already contain a surface when this
     * method is called.  If you are using {@link android.view.SurfaceView},
     * you will need to register a {@link SurfaceHolder.Callback} with
     * {@link SurfaceHolder#addCallback(SurfaceHolder.Callback)} and wait for
     * {@link SurfaceHolder.Callback#surfaceCreated(SurfaceHolder)} before
     * calling setPreviewDisplay() or starting preview.
     *
     * <p>This method must be called before {@link #startPreview()}.  The
     * one exception is that if the preview surface is not set (or set to null)
     * before startPreview() is called, then this method may be called once
     * with a non-null parameter to set the preview surface.  (This allows
     * camera setup and surface creation to happen in parallel, saving time.)
     * The preview surface may not otherwise change while preview is running.
     *
     * @param holder containing the Surface on which to place the preview,
     *     or null to remove the preview surface
     * @throws IOException if the method fails (for example, if the surface
     *     is unavailable or unsuitable).
     */
    public final void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        if (holder != null) {
            setPreviewDisplay(holder.getSurface());
        } else {
            setPreviewDisplay((Surface)null);
        }
    }

    private native final void setPreviewDisplay(Surface surface) throws IOException;

    /**
     * Sets the {@link SurfaceTexture} to be used for live preview.
     * Either a surface or surface texture is necessary for preview, and
     * preview is necessary to take pictures.  The same surface texture can be
     * re-set without harm.  Setting a preview surface texture will un-set any
     * preview surface that was set via {@link #setPreviewDisplay}.
     *
     * <p>This method must be called before {@link #startPreview()}.  The
     * one exception is that if the preview surface texture is not set (or set
     * to null) before startPreview() is called, then this method may be called
     * once with a non-null parameter to set the preview surface.  (This allows
     * camera setup and surface creation to happen in parallel, saving time.)
     * The preview surface texture may not otherwise change while preview is
     * running.
     *
     * <p>The timestamps provided by {@link SurfaceTexture#getTimestamp()} for a
     * SurfaceTexture set as the preview texture have an unspecified zero point,
     * and cannot be directly compared between different cameras or different
     * instances of the same camera, or across multiple runs of the same
     * program.
     *
     * <p>If you are using the preview data to create video or still images,
     * strongly consider using {@link android.media.MediaActionSound} to
     * properly indicate image capture or recording start/stop to the user.</p>
     *
     * @see android.media.MediaActionSound
     * @see android.graphics.SurfaceTexture
     * @see android.view.TextureView
     * @param surfaceTexture the {@link SurfaceTexture} to which the preview
     *     images are to be sent or null to remove the current preview surface
     *     texture
     * @throws IOException if the method fails (for example, if the surface
     *     texture is unavailable or unsuitable).
     */
    public native final void setPreviewTexture(SurfaceTexture surfaceTexture) throws IOException;

    /**
     * Callback interface used to deliver copies of preview frames as
     * they are displayed.
     *
     * @see #setPreviewCallback(Camera.PreviewCallback)
     * @see #setOneShotPreviewCallback(Camera.PreviewCallback)
     * @see #setPreviewCallbackWithBuffer(Camera.PreviewCallback)
     * @see #startPreview()
     */
    public interface PreviewCallback
    {
        /**
         * Called as preview frames are displayed.  This callback is invoked
         * on the event thread {@link #open(int)} was called from.
         *
         * <p>If using the {@link android.graphics.ImageFormat#YV12} format,
         * refer to the equations in {@link Camera.Parameters#setPreviewFormat}
         * for the arrangement of the pixel data in the preview callback
         * buffers.
         *
         * @param data the contents of the preview frame in the format defined
         *  by {@link android.graphics.ImageFormat}, which can be queried
         *  with {@link android.hardware.Camera.Parameters#getPreviewFormat()}.
         *  If {@link android.hardware.Camera.Parameters#setPreviewFormat(int)}
         *             is never called, the default will be the YCbCr_420_SP
         *             (NV21) format.
         * @param camera the Camera service object.
         */
        void onPreviewFrame(byte[] data, Camera camera);
    };

    /**
     * Starts capturing and drawing preview frames to the screen.
     * Preview will not actually start until a surface is supplied
     * with {@link #setPreviewDisplay(SurfaceHolder)} or
     * {@link #setPreviewTexture(SurfaceTexture)}.
     *
     * <p>If {@link #setPreviewCallback(Camera.PreviewCallback)},
     * {@link #setOneShotPreviewCallback(Camera.PreviewCallback)}, or
     * {@link #setPreviewCallbackWithBuffer(Camera.PreviewCallback)} were
     * called, {@link Camera.PreviewCallback#onPreviewFrame(byte[], Camera)}
     * will be called when preview data becomes available.
     */
    public native final void startPreview();

    /**
     * Stops capturing and drawing preview frames to the surface, and
     * resets the camera for a future call to {@link #startPreview()}.
     */
    public final void stopPreview() {
        _stopPreview();
        mFaceDetectionRunning = false;

        mShutterCallback = null;
        mRawImageCallback = null;
        mPostviewCallback = null;
        mJpegCallback = null;
        synchronized (mAutoFocusCallbackLock) {
            mAutoFocusCallback = null;
        }
        mAutoFocusMoveCallback = null;
    }

    private native final void _stopPreview();

    /**
     * Return current preview state.
     *
     * FIXME: Unhide before release
     * @hide
     */
    public native final boolean previewEnabled();

    /**
     * <p>Installs a callback to be invoked for every preview frame in addition
     * to displaying them on the screen.  The callback will be repeatedly called
     * for as long as preview is active.  This method can be called at any time,
     * even while preview is live.  Any other preview callbacks are
     * overridden.</p>
     *
     * <p>If you are using the preview data to create video or still images,
     * strongly consider using {@link android.media.MediaActionSound} to
     * properly indicate image capture or recording start/stop to the user.</p>
     *
     * @param cb a callback object that receives a copy of each preview frame,
     *     or null to stop receiving callbacks.
     * @see android.media.MediaActionSound
     */
    public final void setPreviewCallback(PreviewCallback cb) {
        mPreviewCallback = cb;
        mOneShot = false;
        mWithBuffer = false;
        // Always use one-shot mode. We fake camera preview mode by
        // doing one-shot preview continuously.
        setHasPreviewCallback(cb != null, false);
    }

    /**
     * <p>Installs a callback to be invoked for the next preview frame in
     * addition to displaying it on the screen.  After one invocation, the
     * callback is cleared. This method can be called any time, even when
     * preview is live.  Any other preview callbacks are overridden.</p>
     *
     * <p>If you are using the preview data to create video or still images,
     * strongly consider using {@link android.media.MediaActionSound} to
     * properly indicate image capture or recording start/stop to the user.</p>
     *
     * @param cb a callback object that receives a copy of the next preview frame,
     *     or null to stop receiving callbacks.
     * @see android.media.MediaActionSound
     */
    public final void setOneShotPreviewCallback(PreviewCallback cb) {
        mPreviewCallback = cb;
        mOneShot = true;
        mWithBuffer = false;
        setHasPreviewCallback(cb != null, false);
    }

    private native final void setHasPreviewCallback(boolean installed, boolean manualBuffer);

    /**
     * <p>Installs a callback to be invoked for every preview frame, using
     * buffers supplied with {@link #addCallbackBuffer(byte[])}, in addition to
     * displaying them on the screen.  The callback will be repeatedly called
     * for as long as preview is active and buffers are available.  Any other
     * preview callbacks are overridden.</p>
     *
     * <p>The purpose of this method is to improve preview efficiency and frame
     * rate by allowing preview frame memory reuse.  You must call
     * {@link #addCallbackBuffer(byte[])} at some point -- before or after
     * calling this method -- or no callbacks will received.</p>
     *
     * <p>The buffer queue will be cleared if this method is called with a null
     * callback, {@link #setPreviewCallback(Camera.PreviewCallback)} is called,
     * or {@link #setOneShotPreviewCallback(Camera.PreviewCallback)} is
     * called.</p>
     *
     * <p>If you are using the preview data to create video or still images,
     * strongly consider using {@link android.media.MediaActionSound} to
     * properly indicate image capture or recording start/stop to the user.</p>
     *
     * @param cb a callback object that receives a copy of the preview frame,
     *     or null to stop receiving callbacks and clear the buffer queue.
     * @see #addCallbackBuffer(byte[])
     * @see android.media.MediaActionSound
     */
    public final void setPreviewCallbackWithBuffer(PreviewCallback cb) {
        mPreviewCallback = cb;
        mOneShot = false;
        mWithBuffer = true;
        setHasPreviewCallback(cb != null, true);
    }

    /**
     * Adds a pre-allocated buffer to the preview callback buffer queue.
     * Applications can add one or more buffers to the queue. When a preview
     * frame arrives and there is still at least one available buffer, the
     * buffer will be used and removed from the queue. Then preview callback is
     * invoked with the buffer. If a frame arrives and there is no buffer left,
     * the frame is discarded. Applications should add buffers back when they
     * finish processing the data in them.
     *
     * <p>For formats besides YV12, the size of the buffer is determined by
     * multiplying the preview image width, height, and bytes per pixel. The
     * width and height can be read from
     * {@link Camera.Parameters#getPreviewSize()}. Bytes per pixel can be
     * computed from {@link android.graphics.ImageFormat#getBitsPerPixel(int)} /
     * 8, using the image format from
     * {@link Camera.Parameters#getPreviewFormat()}.
     *
     * <p>If using the {@link android.graphics.ImageFormat#YV12} format, the
     * size can be calculated using the equations listed in
     * {@link Camera.Parameters#setPreviewFormat}.
     *
     * <p>This method is only necessary when
     * {@link #setPreviewCallbackWithBuffer(PreviewCallback)} is used. When
     * {@link #setPreviewCallback(PreviewCallback)} or
     * {@link #setOneShotPreviewCallback(PreviewCallback)} are used, buffers
     * are automatically allocated. When a supplied buffer is too small to
     * hold the preview frame data, preview callback will return null and
     * the buffer will be removed from the buffer queue.
     *
     * @param callbackBuffer the buffer to add to the queue. The size of the
     *   buffer must match the values described above.
     * @see #setPreviewCallbackWithBuffer(PreviewCallback)
     */
    public final void addCallbackBuffer(byte[] callbackBuffer)
    {
        _addCallbackBuffer(callbackBuffer, CAMERA_MSG_PREVIEW_FRAME);
    }

    /**
     * Adds a pre-allocated buffer to the raw image callback buffer queue.
     * Applications can add one or more buffers to the queue. When a raw image
     * frame arrives and there is still at least one available buffer, the
     * buffer will be used to hold the raw image data and removed from the
     * queue. Then raw image callback is invoked with the buffer. If a raw
     * image frame arrives but there is no buffer left, the frame is
     * discarded. Applications should add buffers back when they finish
     * processing the data in them by calling this method again in order
     * to avoid running out of raw image callback buffers.
     *
     * <p>The size of the buffer is determined by multiplying the raw image
     * width, height, and bytes per pixel. The width and height can be
     * read from {@link Camera.Parameters#getPictureSize()}. Bytes per pixel
     * can be computed from
     * {@link android.graphics.ImageFormat#getBitsPerPixel(int)} / 8,
     * using the image format from {@link Camera.Parameters#getPreviewFormat()}.
     *
     * <p>This method is only necessary when the PictureCallbck for raw image
     * is used while calling {@link #takePicture(Camera.ShutterCallback,
     * Camera.PictureCallback, Camera.PictureCallback, Camera.PictureCallback)}.
     *
     * <p>Please note that by calling this method, the mode for
     * application-managed callback buffers is triggered. If this method has
     * never been called, null will be returned by the raw image callback since
     * there is no image callback buffer available. Furthermore, When a supplied
     * buffer is too small to hold the raw image data, raw image callback will
     * return null and the buffer will be removed from the buffer queue.
     *
     * @param callbackBuffer the buffer to add to the raw image callback buffer
     *     queue. The size should be width * height * (bits per pixel) / 8. An
     *     null callbackBuffer will be ignored and won't be added to the queue.
     *
     * @see #takePicture(Camera.ShutterCallback,
     * Camera.PictureCallback, Camera.PictureCallback, Camera.PictureCallback)}.
     *
     * {@hide}
     */
    public final void addRawImageCallbackBuffer(byte[] callbackBuffer)
    {
        addCallbackBuffer(callbackBuffer, CAMERA_MSG_RAW_IMAGE);
    }

    private final void addCallbackBuffer(byte[] callbackBuffer, int msgType)
    {
        // CAMERA_MSG_VIDEO_FRAME may be allowed in the future.
        if (msgType != CAMERA_MSG_PREVIEW_FRAME &&
            msgType != CAMERA_MSG_RAW_IMAGE) {
            throw new IllegalArgumentException(
                            "Unsupported message type: " + msgType);
        }

        _addCallbackBuffer(callbackBuffer, msgType);
    }

    private native final void _addCallbackBuffer(
                                byte[] callbackBuffer, int msgType);

    private class EventHandler extends Handler
    {
        private Camera mCamera;

        public EventHandler(Camera c, Looper looper) {
            super(looper);
            mCamera = c;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case CAMERA_MSG_SHUTTER:
                if (mShutterCallback != null) {
                    mShutterCallback.onShutter();
                }
                return;

            case CAMERA_MSG_RAW_IMAGE:
                if (mRawImageCallback != null) {
                    mRawImageCallback.onPictureTaken((byte[])msg.obj, mCamera);
                }
                return;

            case CAMERA_MSG_COMPRESSED_IMAGE:
                if (mJpegCallback != null) {
                    mJpegCallback.onPictureTaken((byte[])msg.obj, mCamera);
                }
                return;

            case CAMERA_MSG_PREVIEW_FRAME:
                PreviewCallback pCb = mPreviewCallback;
                if (pCb != null) {
                    if (mOneShot) {
                        // Clear the callback variable before the callback
                        // in case the app calls setPreviewCallback from
                        // the callback function
                        mPreviewCallback = null;
                    } else if (!mWithBuffer) {
                        // We're faking the camera preview mode to prevent
                        // the app from being flooded with preview frames.
                        // Set to oneshot mode again.
                        setHasPreviewCallback(true, false);
                    }
                    pCb.onPreviewFrame((byte[])msg.obj, mCamera);
                }
                return;

            case CAMERA_MSG_POSTVIEW_FRAME:
                if (mPostviewCallback != null) {
                    mPostviewCallback.onPictureTaken((byte[])msg.obj, mCamera);
                }
                return;

            case CAMERA_MSG_FOCUS:
                AutoFocusCallback cb = null;
                synchronized (mAutoFocusCallbackLock) {
                    cb = mAutoFocusCallback;
                }
                if (cb != null) {
                    boolean success = msg.arg1 == 0 ? false : true;
                    cb.onAutoFocus(success, mCamera);
                }
                return;

            case CAMERA_MSG_ZOOM:
                if (mZoomListener != null) {
                    mZoomListener.onZoomChange(msg.arg1, msg.arg2 != 0, mCamera);
                }
                return;

            case CAMERA_MSG_PREVIEW_METADATA:
                if (mFaceListener != null) {
                    mFaceListener.onFaceDetection((Face[])msg.obj, mCamera);
                }
                return;

            case CAMERA_MSG_ERROR :
                Log.e(TAG, "Error " + msg.arg1);
                if (mErrorCallback != null) {
                    mErrorCallback.onError(msg.arg1, mCamera);
                }
                return;

            case CAMERA_MSG_FOCUS_MOVE:
                if (mAutoFocusMoveCallback != null) {
                    mAutoFocusMoveCallback.onAutoFocusMoving(msg.arg1 == 0 ? false : true, mCamera);
                }
                return;

            default:
                Log.e(TAG, "Unknown message type " + msg.what);
                return;
            }
        }
    }

    private static void postEventFromNative(Object camera_ref,
                                            int what, int arg1, int arg2, Object obj)
    {
        Camera c = (Camera)((WeakReference)camera_ref).get();
        if (c == null)
            return;

        if (c.mEventHandler != null) {
            Message m = c.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            c.mEventHandler.sendMessage(m);
        }
    }

    /**
     * Callback interface used to notify on completion of camera auto focus.
     *
     * <p>Devices that do not support auto-focus will receive a "fake"
     * callback to this interface. If your application needs auto-focus and
     * should not be installed on devices <em>without</em> auto-focus, you must
     * declare that your app uses the
     * {@code android.hardware.camera.autofocus} feature, in the
     * <a href="{@docRoot}guide/topics/manifest/uses-feature-element.html">&lt;uses-feature></a>
     * manifest element.</p>
     *
     * @see #autoFocus(AutoFocusCallback)
     */
    public interface AutoFocusCallback
    {
        /**
         * Called when the camera auto focus completes.  If the camera
         * does not support auto-focus and autoFocus is called,
         * onAutoFocus will be called immediately with a fake value of
         * <code>success</code> set to <code>true</code>.
         *
         * The auto-focus routine does not lock auto-exposure and auto-white
         * balance after it completes.
         *
         * @param success true if focus was successful, false if otherwise
         * @param camera  the Camera service object
         * @see android.hardware.Camera.Parameters#setAutoExposureLock(boolean)
         * @see android.hardware.Camera.Parameters#setAutoWhiteBalanceLock(boolean)
         */
        void onAutoFocus(boolean success, Camera camera);
    }

    /**
     * Starts camera auto-focus and registers a callback function to run when
     * the camera is focused.  This method is only valid when preview is active
     * (between {@link #startPreview()} and before {@link #stopPreview()}).
     *
     * <p>Callers should check
     * {@link android.hardware.Camera.Parameters#getFocusMode()} to determine if
     * this method should be called. If the camera does not support auto-focus,
     * it is a no-op and {@link AutoFocusCallback#onAutoFocus(boolean, Camera)}
     * callback will be called immediately.
     *
     * <p>If your application should not be installed
     * on devices without auto-focus, you must declare that your application
     * uses auto-focus with the
     * <a href="{@docRoot}guide/topics/manifest/uses-feature-element.html">&lt;uses-feature></a>
     * manifest element.</p>
     *
     * <p>If the current flash mode is not
     * {@link android.hardware.Camera.Parameters#FLASH_MODE_OFF}, flash may be
     * fired during auto-focus, depending on the driver and camera hardware.<p>
     *
     * <p>Auto-exposure lock {@link android.hardware.Camera.Parameters#getAutoExposureLock()}
     * and auto-white balance locks {@link android.hardware.Camera.Parameters#getAutoWhiteBalanceLock()}
     * do not change during and after autofocus. But auto-focus routine may stop
     * auto-exposure and auto-white balance transiently during focusing.
     *
     * <p>Stopping preview with {@link #stopPreview()}, or triggering still
     * image capture with {@link #takePicture(Camera.ShutterCallback,
     * Camera.PictureCallback, Camera.PictureCallback)}, will not change the
     * the focus position. Applications must call cancelAutoFocus to reset the
     * focus.</p>
     *
     * <p>If autofocus is successful, consider using
     * {@link android.media.MediaActionSound} to properly play back an autofocus
     * success sound to the user.</p>
     *
     * @param cb the callback to run
     * @see #cancelAutoFocus()
     * @see android.hardware.Camera.Parameters#setAutoExposureLock(boolean)
     * @see android.hardware.Camera.Parameters#setAutoWhiteBalanceLock(boolean)
     * @see android.media.MediaActionSound
     */
    public final void autoFocus(AutoFocusCallback cb)
    {
        synchronized (mAutoFocusCallbackLock) {
            mAutoFocusCallback = cb;
        }
        native_autoFocus();
    }
    private native final void native_autoFocus();

    /**
     * Cancels any auto-focus function in progress.
     * Whether or not auto-focus is currently in progress,
     * this function will return the focus position to the default.
     * If the camera does not support auto-focus, this is a no-op.
     *
     * @see #autoFocus(Camera.AutoFocusCallback)
     */
    public final void cancelAutoFocus()
    {
        synchronized (mAutoFocusCallbackLock) {
            mAutoFocusCallback = null;
        }
        native_cancelAutoFocus();
        // CAMERA_MSG_FOCUS should be removed here because the following
        // scenario can happen:
        // - An application uses the same thread for autoFocus, cancelAutoFocus
        //   and looper thread.
        // - The application calls autoFocus.
        // - HAL sends CAMERA_MSG_FOCUS, which enters the looper message queue.
        //   Before event handler's handleMessage() is invoked, the application
        //   calls cancelAutoFocus and autoFocus.
        // - The application gets the old CAMERA_MSG_FOCUS and thinks autofocus
        //   has been completed. But in fact it is not.
        //
        // As documented in the beginning of the file, apps should not use
        // multiple threads to call autoFocus and cancelAutoFocus at the same
        // time. It is HAL's responsibility not to send a CAMERA_MSG_FOCUS
        // message after native_cancelAutoFocus is called.
        mEventHandler.removeMessages(CAMERA_MSG_FOCUS);
    }
    private native final void native_cancelAutoFocus();

    /**
     * Callback interface used to notify on auto focus start and stop.
     *
     * <p>This is only supported in continuous autofocus modes -- {@link
     * Parameters#FOCUS_MODE_CONTINUOUS_VIDEO} and {@link
     * Parameters#FOCUS_MODE_CONTINUOUS_PICTURE}. Applications can show
     * autofocus animation based on this.</p>
     */
    public interface AutoFocusMoveCallback
    {
        /**
         * Called when the camera auto focus starts or stops.
         *
         * @param start true if focus starts to move, false if focus stops to move
         * @param camera the Camera service object
         */
        void onAutoFocusMoving(boolean start, Camera camera);
    }

    /**
     * Sets camera auto-focus move callback.
     *
     * @param cb the callback to run
     */
    public void setAutoFocusMoveCallback(AutoFocusMoveCallback cb) {
        mAutoFocusMoveCallback = cb;
        enableFocusMoveCallback((mAutoFocusMoveCallback != null) ? 1 : 0);
    }

    private native void enableFocusMoveCallback(int enable);

    /**
     * Callback interface used to signal the moment of actual image capture.
     *
     * @see #takePicture(ShutterCallback, PictureCallback, PictureCallback, PictureCallback)
     */
    public interface ShutterCallback
    {
        /**
         * Called as near as possible to the moment when a photo is captured
         * from the sensor.  This is a good opportunity to play a shutter sound
         * or give other feedback of camera operation.  This may be some time
         * after the photo was triggered, but some time before the actual data
         * is available.
         */
        void onShutter();
    }

    /**
     * Callback interface used to supply image data from a photo capture.
     *
     * @see #takePicture(ShutterCallback, PictureCallback, PictureCallback, PictureCallback)
     */
    public interface PictureCallback {
        /**
         * Called when image data is available after a picture is taken.
         * The format of the data depends on the context of the callback
         * and {@link Camera.Parameters} settings.
         *
         * @param data   a byte array of the picture data
         * @param camera the Camera service object
         */
        void onPictureTaken(byte[] data, Camera camera);
    };

    /**
     * Equivalent to takePicture(shutter, raw, null, jpeg).
     *
     * @see #takePicture(ShutterCallback, PictureCallback, PictureCallback, PictureCallback)
     */
    public final void takePicture(ShutterCallback shutter, PictureCallback raw,
            PictureCallback jpeg) {
        takePicture(shutter, raw, null, jpeg);
    }
    private native final void native_takePicture(int msgType);

    /**
     * Triggers an asynchronous image capture. The camera service will initiate
     * a series of callbacks to the application as the image capture progresses.
     * The shutter callback occurs after the image is captured. This can be used
     * to trigger a sound to let the user know that image has been captured. The
     * raw callback occurs when the raw image data is available (NOTE: the data
     * will be null if there is no raw image callback buffer available or the
     * raw image callback buffer is not large enough to hold the raw image).
     * The postview callback occurs when a scaled, fully processed postview
     * image is available (NOTE: not all hardware supports this). The jpeg
     * callback occurs when the compressed image is available. If the
     * application does not need a particular callback, a null can be passed
     * instead of a callback method.
     *
     * <p>This method is only valid when preview is active (after
     * {@link #startPreview()}).  Preview will be stopped after the image is
     * taken; callers must call {@link #startPreview()} again if they want to
     * re-start preview or take more pictures. This should not be called between
     * {@link android.media.MediaRecorder#start()} and
     * {@link android.media.MediaRecorder#stop()}.
     *
     * <p>After calling this method, you must not call {@link #startPreview()}
     * or take another picture until the JPEG callback has returned.
     *
     * @param shutter   the callback for image capture moment, or null
     * @param raw       the callback for raw (uncompressed) image data, or null
     * @param postview  callback with postview image data, may be null
     * @param jpeg      the callback for JPEG image data, or null
     */
    public final void takePicture(ShutterCallback shutter, PictureCallback raw,
            PictureCallback postview, PictureCallback jpeg) {
        mShutterCallback = shutter;
        mRawImageCallback = raw;
        mPostviewCallback = postview;
        mJpegCallback = jpeg;

        // If callback is not set, do not send me callbacks.
        int msgType = 0;
        if (mShutterCallback != null) {
            msgType |= CAMERA_MSG_SHUTTER;
        }
        if (mRawImageCallback != null) {
            msgType |= CAMERA_MSG_RAW_IMAGE;
        }
        if (mPostviewCallback != null) {
            msgType |= CAMERA_MSG_POSTVIEW_FRAME;
        }
        if (mJpegCallback != null) {
            msgType |= CAMERA_MSG_COMPRESSED_IMAGE;
        }

        native_takePicture(msgType);
        mFaceDetectionRunning = false;
    }

    /**
     * Zooms to the requested value smoothly. The driver will notify {@link
     * OnZoomChangeListener} of the zoom value and whether zoom is stopped at
     * the time. For example, suppose the current zoom is 0 and startSmoothZoom
     * is called with value 3. The
     * {@link Camera.OnZoomChangeListener#onZoomChange(int, boolean, Camera)}
     * method will be called three times with zoom values 1, 2, and 3.
     * Applications can call {@link #stopSmoothZoom} to stop the zoom earlier.
     * Applications should not call startSmoothZoom again or change the zoom
     * value before zoom stops. If the supplied zoom value equals to the current
     * zoom value, no zoom callback will be generated. This method is supported
     * if {@link android.hardware.Camera.Parameters#isSmoothZoomSupported}
     * returns true.
     *
     * @param value zoom value. The valid range is 0 to {@link
     *              android.hardware.Camera.Parameters#getMaxZoom}.
     * @throws IllegalArgumentException if the zoom value is invalid.
     * @throws RuntimeException if the method fails.
     * @see #setZoomChangeListener(OnZoomChangeListener)
     */
    public native final void startSmoothZoom(int value);

    /**
     * Stops the smooth zoom. Applications should wait for the {@link
     * OnZoomChangeListener} to know when the zoom is actually stopped. This
     * method is supported if {@link
     * android.hardware.Camera.Parameters#isSmoothZoomSupported} is true.
     *
     * @throws RuntimeException if the method fails.
     */
    public native final void stopSmoothZoom();

    /**
     * Set the clockwise rotation of preview display in degrees. This affects
     * the preview frames and the picture displayed after snapshot. This method
     * is useful for portrait mode applications. Note that preview display of
     * front-facing cameras is flipped horizontally before the rotation, that
     * is, the image is reflected along the central vertical axis of the camera
     * sensor. So the users can see themselves as looking into a mirror.
     *
     * <p>This does not affect the order of byte array passed in {@link
     * PreviewCallback#onPreviewFrame}, JPEG pictures, or recorded videos. This
     * method is not allowed to be called during preview.
     *
     * <p>If you want to make the camera image show in the same orientation as
     * the display, you can use the following code.
     * <pre>
     * public static void setCameraDisplayOrientation(Activity activity,
     *         int cameraId, android.hardware.Camera camera) {
     *     android.hardware.Camera.CameraInfo info =
     *             new android.hardware.Camera.CameraInfo();
     *     android.hardware.Camera.getCameraInfo(cameraId, info);
     *     int rotation = activity.getWindowManager().getDefaultDisplay()
     *             .getRotation();
     *     int degrees = 0;
     *     switch (rotation) {
     *         case Surface.ROTATION_0: degrees = 0; break;
     *         case Surface.ROTATION_90: degrees = 90; break;
     *         case Surface.ROTATION_180: degrees = 180; break;
     *         case Surface.ROTATION_270: degrees = 270; break;
     *     }
     *
     *     int result;
     *     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
     *         result = (info.orientation + degrees) % 360;
     *         result = (360 - result) % 360;  // compensate the mirror
     *     } else {  // back-facing
     *         result = (info.orientation - degrees + 360) % 360;
     *     }
     *     camera.setDisplayOrientation(result);
     * }
     * </pre>
     *
     * <p>Starting from API level 14, this method can be called when preview is
     * active.
     *
     * @param degrees the angle that the picture will be rotated clockwise.
     *                Valid values are 0, 90, 180, and 270. The starting
     *                position is 0 (landscape).
     * @see #setPreviewDisplay(SurfaceHolder)
     */
    public native final void setDisplayOrientation(int degrees);

    /**
     * <p>Enable or disable the default shutter sound when taking a picture.</p>
     *
     * <p>By default, the camera plays the system-defined camera shutter sound
     * when {@link #takePicture} is called. Using this method, the shutter sound
     * can be disabled. It is strongly recommended that an alternative shutter
     * sound is played in the {@link ShutterCallback} when the system shutter
     * sound is disabled.</p>
     *
     * <p>Note that devices may not always allow disabling the camera shutter
     * sound. If the shutter sound state cannot be set to the desired value,
     * this method will return false. {@link CameraInfo#canDisableShutterSound}
     * can be used to determine whether the device will allow the shutter sound
     * to be disabled.</p>
     *
     * @param enabled whether the camera should play the system shutter sound
     *                when {@link #takePicture takePicture} is called.
     * @return {@code true} if the shutter sound state was successfully
     *         changed. {@code false} if the shutter sound state could not be
     *         changed. {@code true} is also returned if shutter sound playback
     *         is already set to the requested state.
     * @see #takePicture
     * @see CameraInfo#canDisableShutterSound
     * @see ShutterCallback
     */
    public final boolean enableShutterSound(boolean enabled) {
        if (!enabled) {
            IBinder b = ServiceManager.getService(Context.AUDIO_SERVICE);
            IAudioService audioService = IAudioService.Stub.asInterface(b);
            try {
                if (audioService.isCameraSoundForced()) return false;
            } catch (RemoteException e) {
                Log.e(TAG, "Audio service is unavailable for queries");
            }
        }
        return _enableShutterSound(enabled);
    }

    private native final boolean _enableShutterSound(boolean enabled);

    /**
     * Callback interface for zoom changes during a smooth zoom operation.
     *
     * @see #setZoomChangeListener(OnZoomChangeListener)
     * @see #startSmoothZoom(int)
     */
    public interface OnZoomChangeListener
    {
        /**
         * Called when the zoom value has changed during a smooth zoom.
         *
         * @param zoomValue the current zoom value. In smooth zoom mode, camera
         *                  calls this for every new zoom value.
         * @param stopped whether smooth zoom is stopped. If the value is true,
         *                this is the last zoom update for the application.
         * @param camera  the Camera service object
         */
        void onZoomChange(int zoomValue, boolean stopped, Camera camera);
    };

    /**
     * Registers a listener to be notified when the zoom value is updated by the
     * camera driver during smooth zoom.
     *
     * @param listener the listener to notify
     * @see #startSmoothZoom(int)
     */
    public final void setZoomChangeListener(OnZoomChangeListener listener)
    {
        mZoomListener = listener;
    }

    /**
     * Callback interface for face detected in the preview frame.
     *
     */
    public interface FaceDetectionListener
    {
        /**
         * Notify the listener of the detected faces in the preview frame.
         *
         * @param faces The detected faces in a list
         * @param camera  The {@link Camera} service object
         */
        void onFaceDetection(Face[] faces, Camera camera);
    }

    /**
     * Registers a listener to be notified about the faces detected in the
     * preview frame.
     *
     * @param listener the listener to notify
     * @see #startFaceDetection()
     */
    public final void setFaceDetectionListener(FaceDetectionListener listener)
    {
        mFaceListener = listener;
    }

    /**
     * Starts the face detection. This should be called after preview is started.
     * The camera will notify {@link FaceDetectionListener} of the detected
     * faces in the preview frame. The detected faces may be the same as the
     * previous ones. Applications should call {@link #stopFaceDetection} to
     * stop the face detection. This method is supported if {@link
     * Parameters#getMaxNumDetectedFaces()} returns a number larger than 0.
     * If the face detection has started, apps should not call this again.
     *
     * <p>When the face detection is running, {@link Parameters#setWhiteBalance(String)},
     * {@link Parameters#setFocusAreas(List)}, and {@link Parameters#setMeteringAreas(List)}
     * have no effect. The camera uses the detected faces to do auto-white balance,
     * auto exposure, and autofocus.
     *
     * <p>If the apps call {@link #autoFocus(AutoFocusCallback)}, the camera
     * will stop sending face callbacks. The last face callback indicates the
     * areas used to do autofocus. After focus completes, face detection will
     * resume sending face callbacks. If the apps call {@link
     * #cancelAutoFocus()}, the face callbacks will also resume.</p>
     *
     * <p>After calling {@link #takePicture(Camera.ShutterCallback, Camera.PictureCallback,
     * Camera.PictureCallback)} or {@link #stopPreview()}, and then resuming
     * preview with {@link #startPreview()}, the apps should call this method
     * again to resume face detection.</p>
     *
     * @throws IllegalArgumentException if the face detection is unsupported.
     * @throws RuntimeException if the method fails or the face detection is
     *         already running.
     * @see FaceDetectionListener
     * @see #stopFaceDetection()
     * @see Parameters#getMaxNumDetectedFaces()
     */
    public final void startFaceDetection() {
        if (mFaceDetectionRunning) {
            throw new RuntimeException("Face detection is already running");
        }
        _startFaceDetection(CAMERA_FACE_DETECTION_HW);
        mFaceDetectionRunning = true;
    }

    /**
     * Stops the face detection.
     *
     * @see #startFaceDetection()
     */
    public final void stopFaceDetection() {
        _stopFaceDetection();
        mFaceDetectionRunning = false;
    }

    private native final void _startFaceDetection(int type);
    private native final void _stopFaceDetection();

    /**
     * Information about a face identified through camera face detection.
     *
     * <p>When face detection is used with a camera, the {@link FaceDetectionListener} returns a
     * list of face objects for use in focusing and metering.</p>
     *
     * @see FaceDetectionListener
     */
    public static class Face {
        /**
         * Create an empty face.
         */
        public Face() {
        }

        /**
         * Bounds of the face. (-1000, -1000) represents the top-left of the
         * camera field of view, and (1000, 1000) represents the bottom-right of
         * the field of view. For example, suppose the size of the viewfinder UI
         * is 800x480. The rect passed from the driver is (-1000, -1000, 0, 0).
         * The corresponding viewfinder rect should be (0, 0, 400, 240). It is
         * guaranteed left < right and top < bottom. The coordinates can be
         * smaller than -1000 or bigger than 1000. But at least one vertex will
         * be within (-1000, -1000) and (1000, 1000).
         *
         * <p>The direction is relative to the sensor orientation, that is, what
         * the sensor sees. The direction is not affected by the rotation or
         * mirroring of {@link #setDisplayOrientation(int)}. The face bounding
         * rectangle does not provide any information about face orientation.</p>
         *
         * <p>Here is the matrix to convert driver coordinates to View coordinates
         * in pixels.</p>
         * <pre>
         * Matrix matrix = new Matrix();
         * CameraInfo info = CameraHolder.instance().getCameraInfo()[cameraId];
         * // Need mirror for front camera.
         * boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
         * matrix.setScale(mirror ? -1 : 1, 1);
         * // This is the value for android.hardware.Camera.setDisplayOrientation.
         * matrix.postRotate(displayOrientation);
         * // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
         * // UI coordinates range from (0, 0) to (width, height).
         * matrix.postScale(view.getWidth() / 2000f, view.getHeight() / 2000f);
         * matrix.postTranslate(view.getWidth() / 2f, view.getHeight() / 2f);
         * </pre>
         *
         * @see #startFaceDetection()
         */
        public Rect rect;

        /**
         * <p>The confidence level for the detection of the face. The range is 1 to
         * 100. 100 is the highest confidence.</p>
         *
         * <p>Depending on the device, even very low-confidence faces may be
         * listed, so applications should filter out faces with low confidence,
         * depending on the use case. For a typical point-and-shoot camera
         * application that wishes to display rectangles around detected faces,
         * filtering out faces with confidence less than 50 is recommended.</p>
         *
         * @see #startFaceDetection()
         */
        public int score;

        /**
         * An unique id per face while the face is visible to the tracker. If
         * the face leaves the field-of-view and comes back, it will get a new
         * id. This is an optional field, may not be supported on all devices.
         * If not supported, id will always be set to -1. The optional fields
         * are supported as a set. Either they are all valid, or none of them
         * are.
         */
        public int id = -1;

        /**
         * The coordinates of the center of the left eye. The coordinates are in
         * the same space as the ones for {@link #rect}. This is an optional
         * field, may not be supported on all devices. If not supported, the
         * value will always be set to null. The optional fields are supported
         * as a set. Either they are all valid, or none of them are.
         */
        public Point leftEye = null;

        /**
         * The coordinates of the center of the right eye. The coordinates are
         * in the same space as the ones for {@link #rect}.This is an optional
         * field, may not be supported on all devices. If not supported, the
         * value will always be set to null. The optional fields are supported
         * as a set. Either they are all valid, or none of them are.
         */
        public Point rightEye = null;

        /**
         * The coordinates of the center of the mouth.  The coordinates are in
         * the same space as the ones for {@link #rect}. This is an optional
         * field, may not be supported on all devices. If not supported, the
         * value will always be set to null. The optional fields are supported
         * as a set. Either they are all valid, or none of them are.
         */
        public Point mouth = null;
    }

    // Error codes match the enum in include/ui/Camera.h

    /**
     * Unspecified camera error.
     * @see Camera.ErrorCallback
     */
    public static final int CAMERA_ERROR_UNKNOWN = 1;

    /**
     * Media server died. In this case, the application must release the
     * Camera object and instantiate a new one.
     * @see Camera.ErrorCallback
     */
    public static final int CAMERA_ERROR_SERVER_DIED = 100;

    /**
     * Callback interface for camera error notification.
     *
     * @see #setErrorCallback(ErrorCallback)
     */
    public interface ErrorCallback
    {
        /**
         * Callback for camera errors.
         * @param error   error code:
         * <ul>
         * <li>{@link #CAMERA_ERROR_UNKNOWN}
         * <li>{@link #CAMERA_ERROR_SERVER_DIED}
         * </ul>
         * @param camera  the Camera service object
         */
        void onError(int error, Camera camera);
    };

    /**
     * Registers a callback to be invoked when an error occurs.
     * @param cb The callback to run
     */
    public final void setErrorCallback(ErrorCallback cb)
    {
        mErrorCallback = cb;
    }

    private native final void native_setParameters(String params);
    private native final String native_getParameters();

    /**
     * Changes the settings for this Camera service.
     *
     * @param params the Parameters to use for this Camera service
     * @throws RuntimeException if any parameter is invalid or not supported.
     * @see #getParameters()
     */
    public void setParameters(Parameters params) {
        native_setParameters(params.flatten());
    }

    /**
     * Returns the current settings for this Camera service.
     * If modifications are made to the returned Parameters, they must be passed
     * to {@link #setParameters(Camera.Parameters)} to take effect.
     *
     * @see #setParameters(Camera.Parameters)
     */
    public Parameters getParameters() {
        Parameters p = new Parameters();
        String s = native_getParameters();
        p.unflatten(s);
        return p;
    }

    /**
     * Returns an empty {@link Parameters} for testing purpose.
     *
     * @return a Parameter object.
     *
     * @hide
     */
    public static Parameters getEmptyParameters() {
        Camera camera = new Camera();
        return camera.new Parameters();
    }

    /**
     * Image size (width and height dimensions).
     */
    public class Size {
        /**
         * Sets the dimensions for pictures.
         *
         * @param w the photo width (pixels)
         * @param h the photo height (pixels)
         */
        public Size(int w, int h) {
            width = w;
            height = h;
        }
        /**
         * Compares {@code obj} to this size.
         *
         * @param obj the object to compare this size with.
         * @return {@code true} if the width and height of {@code obj} is the
         *         same as those of this size. {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Size)) {
                return false;
            }
            Size s = (Size) obj;
            return width == s.width && height == s.height;
        }
        @Override
        public int hashCode() {
            return width * 32713 + height;
        }
  
