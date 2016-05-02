package com.axisapplications.dressme.activity.base;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.axisapplications.dressme.R;
import com.axisapplications.dressme.dao.DatabaseHelper;
import com.axisapplications.dressme.share.ShareHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BaseActivity extends Activity implements SurfaceHolder.Callback {

	public static interface RunnableAfterText {
		public void execute(String text);
	}

	public static interface RunnableAfterTakePhoto {
		void execute(String pathToPhotoImage);
	}
	
	private Boolean debugEnabled	= null; 

	private DatabaseHelper databaseHelper = null;

	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private boolean autofocus;
	private PreviewCallback previewCallback;

	private Handler autoFocusHandler;

	protected void setupCameraPreviewOnSurfaceView(SurfaceView surfaceView,
			PreviewCallback previewCallback, boolean autofocus) {
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		this.autofocus = autofocus;
		this.previewCallback = previewCallback;
	}
	
	protected boolean isDebugEnabled() {
		if (debugEnabled==null) {
			
			
			File debugFile = new File(Environment.getExternalStorageDirectory(),"debug");
			debugEnabled	= Boolean.valueOf(debugFile.exists());
			Log.i("Debug", "Debug [" + debugEnabled + "] To enable debugging create file [" + debugFile.getAbsolutePath() + "]");
		}
		
		return debugEnabled.booleanValue();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (camera == null)
			return;

		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open();
			if (camera != null) {
				camera.setDisplayOrientation(90);

				Camera.Parameters parameters = camera.getParameters();
				// parameters.set("jpeg-quality", 100);
				// parameters.set("orientation", "landscape");
				// parameters.set("rotation", 90);
				parameters.setJpegQuality(85);

				List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
				// TODO that should load from settings

				try {
					camera.setPreviewDisplay(holder);
					camera.setPreviewCallback(previewCallback);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// select closest to 640 pixels width
				Camera.Size selectedSize = null;
				for (Camera.Size size : sizes) {
					if ((selectedSize == null)
							|| (Math.abs(selectedSize.width - 320) > Math
									.abs(size.width - 320))) {
						selectedSize = size;
					}
				}
				parameters.setPictureSize(selectedSize.width,
						selectedSize.height);

				Log.i(this.getClass().getSimpleName(), "Selected size = "
						+ selectedSize.width + "x" + selectedSize.height);

				camera.startPreview();

				if (autofocus) {
					Log.d(this.getClass().getSimpleName(), "Autofocus enabled");

					autoFocusHandler = new Handler();
					final AtomicReference<AutoFocusCallback> autoFocusCallbackReference = new AtomicReference<AutoFocusCallback>();

					final Runnable doAutoFocus = new Runnable() {
						public void run() {
							if (camera != null) {
								camera.autoFocus(autoFocusCallbackReference
										.get());
							}
						}
					};
					autoFocusCallbackReference.set(new AutoFocusCallback() {
						public void onAutoFocus(boolean success, Camera camera) {
							// Log.d("CAMERA","Autofocused");
							autoFocusHandler.postDelayed(doAutoFocus, 1000);
						}
					});

					camera.autoFocus(autoFocusCallbackReference.get());
				}
			}
		} catch (Exception e) {
			Toast.makeText(getApplication(),
					"Camera failed.\n" + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera == null)
			return;

		camera.stopPreview();
		camera.setPreviewCallback(null);
		camera.release();
		camera = null;
	}

	protected void takePhoto(final RunnableAfterTakePhoto runnable) {
		if (camera == null) {
			runnable.execute(null);
			return;
		}

		camera.autoFocus(new AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				camera.takePicture(null, null, new Camera.PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						FileOutputStream outStream = null;
						File tempFile = null;
						try {
							// rotate 90 degrees bitmap
							Bitmap bitmap = BitmapFactory.decodeByteArray(data,
									0, data.length);
							Matrix mat = new Matrix();
							mat.preRotate(90);
							bitmap = Bitmap.createBitmap(bitmap, 0, 0,
									bitmap.getWidth(), bitmap.getHeight(), mat,
									true);

							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							bitmap.compress(Bitmap.CompressFormat.JPEG, 85,
									stream);
							byte[] byteArray = stream.toByteArray();

							tempFile = getTemporaryFileOnExternalStorage(".jpg");

							outStream = new FileOutputStream(tempFile);
							outStream.write(byteArray);
							outStream.close();

							Log.d(this.getClass().getSimpleName(),
									"onPictureTaken - wrote bytes: "
											+ data.length);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								outStream.close();
							} catch (Exception e2) {
							}
						}
						Log.d(this.getClass().getSimpleName(), "onPictureTaken");

						if (tempFile != null) {
							runnable.execute(tempFile.getAbsolutePath());
						} else {
							runnable.execute(null);
						}
					}
				});

			}
		});
	}

	protected boolean isCameraEnabled() {
		return camera != null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	protected DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this,
					DatabaseHelper.class);
		}
		return databaseHelper;
	}

	public void playSound(int soundResourceId) {
		Log.i("SOUND", "play sound " + soundResourceId);
		
		MediaPlayer mp = MediaPlayer.create(this, soundResourceId);
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
			}
		});
		mp.start();
	}

	public void showError(String message) {
		Log.e(this.getClass().getSimpleName(), message);
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.errorDialog_title).setMessage(message)
				.show();
	}

	public void showError(String message, Exception exception) {
		showError(message + "\nError: " + exception.getMessage());
	}

	public void showEditDialog(String title, String message,
			String defaultText, final RunnableAfterText runnable) {
		AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
		alert.setTitle(title);

		// Set an EditText view to get user input
		final EditText input = new EditText(BaseActivity.this);
		input.setText(defaultText);
		alert.setView(input);

		alert.setMessage(message);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String text = input.getEditableText().toString();
				runnable.execute(text);
			}
		});

		alert.setNegativeButton("CANCEL",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						dialog.cancel();
					}
				});

		// End of alert.setNegativeButton
		AlertDialog alertDialog = alert.create();
		alertDialog.show();
	}

	protected File getTemporaryFileOnExternalStorage(String extension)
			throws IOException {
		File outputDir = new File(Environment.getExternalStorageDirectory(),
				getPackageName());
		if (!outputDir.exists()) {
			outputDir.mkdir();
		}

		Log.i(this.getClass().getSimpleName(),
				"outputDir = " + outputDir.getAbsolutePath());

		File file = File.createTempFile("tmp", extension, outputDir);
		file.deleteOnExit();

		return file;
	}
		
	/*
	 * Theoretically it shall be supported to share image AND text, but most
	 * apps crash
	 */
	protected void shareMessage(String subject, String message, String link, String imageFilePath) {
		ShareHelper shareHelper = new ShareHelper(this, subject, message, link, imageFilePath);
		shareHelper.share();
	}

	protected void setupBackButton(int buttonResourceId) {
		final Button backButton = (Button) findViewById(buttonResourceId);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	protected void addToGallery(String imageFilePath) {
		if (imageFilePath != null) {
			File userItemPhotoFile = new File(imageFilePath);
			if (userItemPhotoFile.exists()) {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.fromFile(userItemPhotoFile)));
			}
		}
	}

	public int dpToPx(int dp) {
		return (int) ((60 * getResources().getDisplayMetrics().density) + 0.5f);
	}

	public Bitmap decodeThumbnailBitmap(String imageFilePath, int maxWidth,
			int maxHeight) throws IOException {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageFilePath, options);

		int ratio = Math.min(options.outWidth / maxWidth, options.outHeight
				/ maxHeight);
		int sampleSize = Integer.highestOneBit((int) Math.floor(ratio));
		if (sampleSize == 0) {
			sampleSize = 1;
		}
		Log.d(this.getClass().getSimpleName(), "Sample Size: " + sampleSize);

		options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;

		return BitmapFactory.decodeFile(imageFilePath, options);
	}
}
