package net.semanticmetadata.lire.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

public class BitmapUtils {

	
	public static Bitmap decodeFile(File f, int IMAGE_MAX_SIZE){
	    Bitmap b = null;
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;

	        FileInputStream fis = new FileInputStream(f);
	        BitmapFactory.decodeStream(fis, null, o);
	        fis.close();

	        int scale = 1;
	        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
	            scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	        }

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize = scale;
	        fis = new FileInputStream(f);
	        b = BitmapFactory.decodeStream(fis, null, o2);
	        fis.close();
	    } catch (IOException e) {
	    }
	    return b;
	}
	
	public static Bitmap scaleBitmap(Bitmap bitmapOrg, int newWidth,
			int newHeight) {

		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();

		// calculate the scale - in this case = 0.4f
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// rotate the Bitmap
		matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width,
				height, matrix, true);

		return resizedBitmap;
	}

	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}
	
	
	/**
     * Scales down an image into a box of maxSideLenght x maxSideLength.
     *
     * @param image         the image to scale down. It remains untouched.
     * @param maxSideLength the maximum side length of the scaled down instance. Has to be > 0.
     * @return the scaled image, the
     */
    public static Bitmap scaleBitmap(Bitmap image, int maxSideLength) {
        assert (maxSideLength > 0);
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();
        double scaleFactor = 0.0;
        if (originalWidth > originalHeight) {
            scaleFactor = ((double) maxSideLength / originalWidth);
        } else {
            scaleFactor = ((double) maxSideLength / originalHeight);
        }
        // create smaller image
        // fast scale (Java 1.4 & 1.5)
//        Graphics g = img.getGraphics();
        // ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          // createa matrix for the manipulation
     		Matrix matrix = new Matrix();
     		// resize the bit map
     		matrix.postScale((float)(originalWidth * scaleFactor),(float)( originalHeight * scaleFactor));
     		// rotate the Bitmap
     		matrix.postRotate(45);

     		// recreate the new Bitmap
     		Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, (int)(originalWidth * scaleFactor),(int)( originalHeight * scaleFactor),
     				 matrix, true);
    
        return resizedBitmap;
    }
	
	public static int[] pixel2rgb(int pixel){
		int r= Color.red(pixel);
		int g = Color.green(pixel);
		int b = Color.blue(pixel);
		int[] rgbvalue = {r,g,b};
		return rgbvalue;
	}
	
	
	public static class Point2D{
    	public Point2D(double d, double e) {
    		x = d;
    		y = e;
    	}
    	public double getY() {
			// TODO Auto-generated method stub
			return y;
		}
		public double getX() {
			// TODO Auto-generated method stub
			return x;
		}
		double x;
    	double y;
    }   
	
}

