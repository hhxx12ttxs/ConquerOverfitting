package iabcd.com;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class Objeto
{
	private Bitmap bitmap;
	private float x;
	private float y;
	private float invX;
	private float invY;
	private float difX;
	private float difY;
	private float canvasHeight;
	private int resize = 8;
	private boolean isPicked;
	private boolean playPickupAnimation;
	private SoundPool soundPool;
	private int soundid;
	
	private static final String TAG = Objeto.class.getSimpleName();
	
	Objeto(Bitmap bitmap, int x, int y)
	{
		this.bitmap = bitmap;
		this.x = x;
		this.y = y;
		isPicked = false;
		playPickupAnimation = false;
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		
	}
	
	void draw(Canvas canvas)
	{
		canvasHeight = canvas.getHeight();
		
		canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect((int)x - bitmap.getWidth()/resize,(int) y - bitmap.getHeight()/resize,(int) x + bitmap.getWidth()/resize,(int) y + bitmap.getHeight()/resize), null);
		
		if(playPickupAnimation)
		{
			pickupAnimation(difX, difY);
		}
	}
	
	void onTouch(int numItens, float itemWidth, float itemHeight)
	{
		if(!isPicked)
		{
			playPickupAnimation = true;
			isPicked = true;
			
			invX = ( (numItens - 1) * itemWidth) + itemWidth/2 ;
			invY = itemHeight/2 + canvasHeight - 68;
			difX = (float)(invX - x) / 30.0f;
			difY = (float)(invY - y) / 30.0f;
		}
		
		else
		{
			
			
		}
		
		Log.d(TAG,"invX="+invX+" InvY="+invY+" difX="+difX+" difY="+difY);
	}
	
	void pickupAnimation(float difX, float difY)
	{
		if( Math.abs(invX - x) <= 1 && Math.abs(invY - y) <= 1 )
			playPickupAnimation = false;
		
		x += difX;
		y += difY;
		
		Log.d(TAG,"invX="+invX+" InvY="+invY+" X="+x+" Y="+y);
	}
	
	public boolean isTouch(int x, int y)
	{
		
		if(      (x <= this.x + this.getBitmap().getWidth()/2) &&
				 (y <= this.y + this.getBitmap().getHeight()/2) &&
				 (x >= this.x - this.getBitmap().getWidth()/2) &&
				 (y >= this.y - this.getBitmap().getHeight()/2)   
		  )
			return true;
		
		return false;
		
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public float getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getResize() {
		return resize;
	}

	public void setResize(int resize) {
		this.resize = resize;
	}

	public boolean isPicked() {
		return isPicked;
	}

	public void setPicked(boolean isPicked) {
		this.isPicked = isPicked;
	}
}

