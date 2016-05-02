package com.primitive.library.ui.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.primitive.library.common.Comparison;
import com.primitive.library.common.log.Logger;
import com.primitive.library.helper.device.Android;
import com.primitive.library.helper.device.DisplayHelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class CameraView extends RelativeLayout implements SurfaceHolder.Callback {
	private final SurfaceView surfaceView;
	private final SurfaceHolder surfaceHolder;
	private Size previewSize;
	private List<Size> mSupportedPreviewSizes;
	@SuppressWarnings("unused")
	private List<Size> mSupportedPictureSizes;
	private Camera camera;
	private final Display mDisplay;

	@SuppressWarnings("deprecation")
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.surfaceView = new SurfaceView(context);
		this.addView(this.surfaceView);
		this.surfaceHolder = this.surfaceView.getHolder();
		this.surfaceHolder.addCallback(this);
		this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		final WindowManager wmgr = (WindowManager) this.getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		this.mDisplay = wmgr.getDefaultDisplay();
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
		if (this.camera != null) {
			this.mSupportedPreviewSizes = this.camera.getParameters()
					.getSupportedPreviewSizes();
			this.requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(this.getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int height = resolveSize(this.getSuggestedMinimumHeight(),
				heightMeasureSpec);
		this.setMeasuredDimension(width, height);
		if (this.mSupportedPreviewSizes != null) {
			this.previewSize = DisplayHelper.getOptimalPreviewSize(this.mSupportedPreviewSizes, width, height);
			this.mSupportedPictureSizes = DisplayHelper.getOptimalPreviewSizes(this.camera.getParameters().getSupportedPictureSizes(),width, height);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && this.getChildCount() > 0) {
			final View child = this.getChildAt(0);
			final boolean portrait = DisplayHelper.isPortrait(this.getContext());
			final int width = r - l;
			final int height = b - t;
			if (portrait) {
				if (width == this.mDisplay.getWidth()
						&& height == this.mDisplay.getHeight()) {
					child.layout(l, t, r, b);
				} else {
					final Size size = this.camera.getParameters()
							.getPreviewSize();
					final int top = (this.mDisplay.getWidth() - (size.height / 2)) / 2;
					final int buttom = top + height;
					child.layout(0, top, this.mDisplay.getHeight(), buttom);
				}
			} else {
				int previewWidth = width;
				int previewHeight = height;

				if (this.previewSize != null) {
					previewWidth = this.previewSize.width;
					previewHeight = this.previewSize.height;
				}

				// Center the child SurfaceView within the parent.
				if (width * previewHeight > height * previewWidth) {
					final int scaledChildWidth = previewWidth * height / previewHeight;
					child.layout((width - scaledChildWidth) / 2, 0,(width + scaledChildWidth) / 2, height);
				} else {
					final int scaledChildHeight = previewHeight * width
							/ previewWidth;
					child.layout(0, (height - scaledChildHeight) / 2, width,(height + scaledChildHeight) / 2);
				}
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if (this.camera != null) {
				this.camera.setPreviewDisplay(holder);
			}
		} catch (final IOException ex) {
			Logger.err(ex);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (this.camera != null) {
			this.camera.stopPreview();
			this.camera.release();
			this.camera = null;
		}
	}

	private void changeLayout(int width, int height) {
		this.camera.stopPreview();
		final Camera.Parameters parameters = this.camera.getParameters();
		final boolean portrait = DisplayHelper.isPortrait(this.getContext());
		final boolean isVersion = Android.isComparisonVersion(Build.VERSION_CODES.FROYO, Comparison.Less);
		if (isVersion) {
			// 2.1 and before
			if (portrait) {
				parameters.set("orientation", "portrait");
			} else {
				parameters.set("orientation", "landscape");
			}
		} else {
			if (portrait) {
				this.camera.setDisplayOrientation(90);
			} else {
				this.camera.setDisplayOrientation(0);
			}
		}
		final Size pictureSize = DisplayHelper.getOptimalPreviewSize(parameters.getSupportedPictureSizes(), width, height);
		parameters.setPictureSize(pictureSize.width, pictureSize.height);
		this.camera.setParameters(parameters);
		this.camera.startPreview();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.changeLayout(width, height);
	}

	public void switchCamera(Camera camera) {
		this.setCamera(camera);
		try {
			this.camera.setPreviewDisplay(this.surfaceHolder);
		} catch (final IOException ex) {
		}
		if (this.previewSize != null) {
			this.changeLayout(this.previewSize.width, this.previewSize.height);
		}
	}

	@SuppressWarnings("deprecation")
	public void showPictureSizeDialog() {
		final ArrayList<String> resource = new ArrayList<String>();
		List<Size> SupportedPictureSizes = this.camera.getParameters().getSupportedPictureSizes();
		SupportedPictureSizes = DisplayHelper.getOptimalPreviewSizes(
				SupportedPictureSizes,
				this.mDisplay.getWidth(),
				this.mDisplay.getHeight());
		for (final Size size : SupportedPictureSizes) {
			resource.add("" + size.width + " x " + size.height);
		}

		final String[] resources = resource
				.toArray(new String[resource.size()]);
		new AlertDialog.Builder(this.getContext()).setTitle("保存する画像の解像度")
				.setItems(resources, new DialogInterface.OnClickListener() {
					private static final String TAG = "DialogInterface.OnClickListener";

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i(TAG, "onClick");

						List<Size> SupportedPictureSizes = CameraView.this.camera
								.getParameters().getSupportedPictureSizes();
						SupportedPictureSizes = DisplayHelper.getOptimalPreviewSizes(
								SupportedPictureSizes,
								CameraView.this.mDisplay.getWidth(),
								CameraView.this.mDisplay.getHeight()
						);
						if (SupportedPictureSizes.size() > which) {
							final Size size = SupportedPictureSizes.get(which);
							final Parameters params = CameraView.this.camera
									.getParameters();
							params.setPictureSize(size.width, size.height);
							CameraView.this.camera.setParameters(params);
						}
					}
				}).show();
	}
}

