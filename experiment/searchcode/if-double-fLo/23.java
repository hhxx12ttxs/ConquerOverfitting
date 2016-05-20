package de.mns.furian.vis.screen;

import de.mns.furian.level.LevelModel;
import de.mns.furian.level.logic.LevelHandler;
import de.mns.furian.level.object.Segment;
import de.mns.furian.level.object.flo.Bullet;
import de.mns.furian.level.object.flo.FreeLevelObject;
import de.mns.furian.level.object.oos.Coin;
import de.mns.furian.level.object.oos.Monster;
import de.mns.furian.level.object.oos.ObjectOnSegment;
import de.mns.furian.util.Images;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Plain2DRenderer extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	private boolean running;
	private Thread mainThread;

	private Paint screenKillPaint;
	private Paint coordPaint;
	private Paint txtPaint;
	private Paint gapPaint;
	private Paint collisionPaint;
	private Rect screenKillRect;

	private LevelHandler levelHandler;
	private LevelModel levelModel;

	private int width, height;
	private int fps;

	private SurfaceHolder mSurfaceHolder;

	private Drawable drawable_Coin;
	private Drawable drawable_Monster;
	private Drawable drawable_Bullet;

	public Plain2DRenderer(Context context, LevelHandler lh) {
		super(context);

		this.mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		screenKillPaint = new Paint();
		screenKillPaint.setAntiAlias(false);
		screenKillPaint.setARGB(255, 0, 0, 0);

		txtPaint = new Paint();
		txtPaint.setAntiAlias(false);
		txtPaint.setARGB(255, 255, 255, 0);

		coordPaint = new Paint();
		coordPaint.setAntiAlias(false);
		coordPaint.setARGB(255, 0, 255, 0);

		gapPaint = new Paint();
		gapPaint.setAntiAlias(false);
		gapPaint.setARGB(255, 0, 0, 0);

		collisionPaint = new Paint();
		collisionPaint.setAntiAlias(false);
		collisionPaint.setARGB(255, 255, 0, 0);

		this.levelHandler = lh;
		this.levelModel = lh.getLevelModel();

		setFocusable(true);

		drawable_Coin = Images.getInstance().getImg(Images.COIN);
		drawable_Monster = Images.getInstance().getImg(Images.MONSTER);
		drawable_Bullet = Images.getInstance().getImg(Images.BULLET);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		this.width = width;
		this.height = height;
		this.screenKillRect = new Rect(0, 0, this.width, this.height);
	}

	public void surfaceCreated(SurfaceHolder holder) {

		this.startThread();

	}

	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public void startThread() {

		if (!this.running && this.mainThread == null || !this.mainThread.isAlive()) {

			this.running = true;
			this.mainThread = new Thread(this);
			this.mainThread.start();
			this.levelHandler.startLevel();
		}
	}

	public void stopThread() {

		if (this.running && this.mainThread != null && this.mainThread.isAlive()) {

			this.running = false;

		}

	}

	public void run() {

		long tmillis = System.currentTimeMillis();
		int ci = 0;

		this.width = this.getWidth();
		this.height = this.getHeight();

		this.screenKillRect = new Rect(0, 0, this.width, this.height);

		while (this.running) {

			Canvas c = null;
			try {
				c = mSurfaceHolder.lockCanvas(null);
				synchronized (mSurfaceHolder) {

					doDraw(c);
					ci++;
					if (ci > 100) {

						this.fps = (int) (100000.0 / (System.currentTimeMillis() - tmillis));
						ci = 0;
						tmillis = System.currentTimeMillis();
					}

				}
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null) {
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}

		}

		this.mainThread = null;
	}

	private Rect curRect = new Rect();
	private Paint curPaint = new Paint();
	private Segment[] curSegmentLine;
	private Segment curSegment;

	private static final double segmentHight = 20d;
	private static final double segmentWidth = 8d;

	private void doDraw(Canvas canvas) {

		if (canvas == null) return;
		
		canvas.drawRect(this.screenKillRect, this.screenKillPaint);

		// draw coordinate system:
		for (int z = 0; z < 20; z++) {

			canvas.drawLine(10, (int) (height - 60 - (z * segmentHight)), 320, (int) (height - 60 - (z * segmentHight)), coordPaint);
		}

		for (int z = 0; z < levelHandler.horizon; z++) {

			if ((z + levelHandler.cSegment) < levelHandler.levelLength) {

				curSegmentLine = levelModel.segments[levelHandler.cSegment + z];
				for (int x = 0; x < 36; x++) {
					curSegment = curSegmentLine[x];
					if (curSegment != null) {
						curPaint.setARGB(255, (int) curSegment.properties.color1.red, (int) curSegment.properties.color1.green,
						        (int) curSegment.properties.color1.blue);

						curRect.left = 10 + (int) (x * segmentWidth);
						curRect.top = height - 60 + (int) ((levelHandler.cSegmentProgress * segmentHight) - (z * segmentHight));
						curRect.right = curRect.left + (int) segmentWidth - 1;
						curRect.bottom = curRect.top + (int) segmentHight - 1;

						canvas.drawRect(curRect, curPaint);

						if (curSegment.objects != null) {
							for (int o = 0; o < curSegment.objects.length; o++) {
								drawObject(canvas, curRect, curSegment.objects[o]);
							}
						}
					}
				}
			}
		}

		FreeLevelObject tmp_fo;

		// draw FreeLevelObjects:
		for (int i = 0; i < levelHandler.maxFreeObjects; i++) {

			tmp_fo = levelHandler.freeObject[i];

			if (tmp_fo != null) {

				if (tmp_fo instanceof Bullet) {

					this.drawable_Bullet.setBounds(10 + (int) (tmp_fo.segment_x * segmentWidth), (int) (height - 60
					// 1.Vector (segment position:
					        - ((tmp_fo.segment_z - levelHandler.cSegment) * segmentHight)
					        // 2.Vector (bullet @ level progress:
					        + ((1.0d - tmp_fo.segment_z_progress) * segmentHight) + (levelHandler.cSegmentProgress * segmentHight)), 1, 1);

					Rect r = this.drawable_Bullet.getBounds();
					this.drawable_Bullet.setBounds(r.left + 1, r.top - 2, r.left + 5, r.top + 2);

					this.drawable_Bullet.draw(canvas);
				}
			}

		}

		// draw player:
		curRect.left = 10 + (int) (levelHandler.cPlayerPos * segmentWidth + levelHandler.cPlayerJumpProgress * segmentWidth);
		curRect.top = height - 44;
		curRect.right = curRect.left + 7;
		curRect.bottom = curRect.top + 7;
		if (levelHandler.cPlayerJumpProgress != 0) {
			curPaint.setARGB(255, 255, 0, 255);
		} else {
			curPaint.setARGB(255, 255, 255, 255);
		}

		canvas.drawRect(curRect, curPaint);

		canvas.drawText("fps :" + this.fps, 3, 12, this.txtPaint);
		canvas.drawText("lps :" + this.levelHandler.curLps + " @ " + this.levelHandler.curDelayMillis + "msec.", 3, 24, this.txtPaint);

		canvas.drawText("Segment :" + this.levelHandler.cSegment, 3, 36, this.txtPaint);
		canvas.drawText("SegmentProgr:" + this.levelHandler.cSegmentProgress, 3, 48, this.txtPaint);
		canvas.drawText("Collisions:" + this.levelHandler.collision, 3, 60, this.txtPaint);

		// draw gap:
		curRect.left = 10;
		curRect.top = height - 40;
		curRect.right = 320;
		curRect.bottom = curRect.top + 20;

		canvas.drawRect(curRect, gapPaint);

		// canvas.drawText(text, x, y, paint)
		// draw everything...

		// if(levelHandler.collision){
		// curRect.left = 10;
		// curRect.top = height - 40;
		// curRect.right = 50;
		// curRect.bottom = curRect.top + 20 ;
		// canvas.drawRect(curRect, collisionPaint);
		// }

		canvas.restore();

	}

	private Coin curCoin;
	private Monster curMonster;

	private void drawObject(Canvas canvas, Rect curRect2, ObjectOnSegment objectOnSegment) {

		if (objectOnSegment instanceof Coin) {
			curCoin = (Coin) objectOnSegment;
			drawable_Coin.setBounds(curRect2.left, curRect2.top, (int) (curRect2.left + curCoin.curSize * 10), (int) (curRect2.top + curCoin.curSize * 10));
			drawable_Coin.draw(canvas);
		} else if (objectOnSegment instanceof Monster) {
			curMonster = (Monster) objectOnSegment;
			drawable_Monster.setBounds(curRect2.left, curRect2.top, (int) (curRect2.left + 10), (int) (curRect2.top + 10));
			drawable_Monster.draw(canvas);
		}

	}

}

