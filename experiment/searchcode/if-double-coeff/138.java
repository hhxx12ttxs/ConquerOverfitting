package com.dummy;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Canvas.VertexMode;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.dummy.element.Coin;
import com.dummy.element.LandingSpot;
import com.dummy.element.Man;
import com.dummy.element.Oscillator;
import com.dummy.element.TemporaryText;
import com.dummy.util.Pool;

class CannonThread extends Thread {
	/*
	 * State-tracking constants
	 */
	public static final int STATE_LOSE = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_READY = 3;
	public static final int STATE_USER_INPUT = 4;
	public static final int STATE_RUNNING = 5;
	public static final int STATE_WIN = 6;

	private Context mContext;

	/** Current Width/height of the surface/canvas. */
	private int mCanvasHeight = 1;
	private int mCanvasWidth = 1;

	float levelWidth, levelHeight;

	/** Message handler used by thread to interact with TextView */
	private Handler mHandler;

	// canon angle
	float angle;
	float maxSpeed;
	// target position
	float btnRadius;
	float windSpeedX;
	float gravity;
	float MaxWindSpeed;
	Man cannonMan;
	Vec2 paraLocalPosition;
	
	ScoreAccountant accountant;

	/** Used to figure out elapsed time between frames */
	private long mLastTime;

	/** Paint to draw the lines on screen. */
	private Paint mPaint;

	/** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
	private int mMode;

	/** Indicate whether the surface has been created & is ready to draw */
	private boolean mRun = false;

	/** Scratch rect object. */
	private RectF mScratchRect;

	/** Handle to the surface manager object we interact with */
	private SurfaceHolder mSurfaceHolder;

	float TimeScale = 1;

	HUDButton fireButton, driveLeft, driveRight;

	World world;
	
	ArrayList<Coin> coins;
	ArrayList<TemporaryText> tempTexts;
	ArrayList<LandingSpot> landingSpots;
	TemporaryText scoreMultiplier;
	
	Vec2 fingerPosition;
	Body man;
	
	final float Gravity = 1;
	StringBuffer stringBuffer;
	DecimalFormat df;
	FieldPosition fp;
	
	final Vec2 UnitX = new Vec2(1, 0);
	final Vec2 UnitNegY = new Vec2(0, -1);
	final Vec2 tmp1 = new Vec2();
	final Vec2 tmp2 = new Vec2();
	final Vec2 tmp3 = new Vec2();
	
	public CannonThread(SurfaceHolder surfaceHolder, Context context,
			Handler handler) {
		// get handles to some important objects
		mSurfaceHolder = surfaceHolder;
		mHandler = handler;
		mContext = context;

		mMode = STATE_READY;

		// Initialize paints for speedometer
		mScratchRect = new RectF(0, 0, 0, 0);
		mPaint = new Paint();
		mPaint.setAntiAlias(false);
		mPaint.setColor(Color.RED);

		AABB worldAABB = new AABB();
		worldAABB.lowerBound.set(new Vec2((float) -100.0, (float) -100.0));
		worldAABB.upperBound.set(new Vec2((float) 100.0, (float) 100.0));

		// Step 2: Create Physics World with Gravity
		Vec2 gravity = new Vec2(0, Gravity);
		boolean doSleep = true;
		world = new World(worldAABB, gravity, doSleep);
		
		coins = new ArrayList<Coin>(50);
		tempTexts = new ArrayList<TemporaryText>(50);
		landingSpots = new ArrayList<LandingSpot>(3);
		
		accountant = new ScoreAccountant();
		
		df = new DecimalFormat("########.#");
		stringBuffer = new StringBuffer();
		fp = new FieldPosition( 0 );
	}

	public void addMan() {
		// Create Dynamic Body
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(levelWidth * 0.1f, levelHeight * 0.9f);
		man = world.createBody(bodyDef);

		// Create Shape with Properties
		PolygonDef poly = new PolygonDef();
		poly.setAsBox(cannonMan.w * 0.5f, cannonMan.h * 0.5f);

		poly.density = 1.0f;
		poly.friction = 0.3f;
		// Assign shape to Body
		man.createShape(poly);
		man.setMassFromShapes();
	}

	public void addParachute() {
		// Create Shape with Properties
		paraLocalPosition = new Vec2(0, cannonMan.h * 1.1f);
	}

	void Fire() {
		Log.i("cm", "Fire start");
		setState(STATE_RUNNING);
	
		cannonMan.posX = levelWidth * 0.1f;
		cannonMan.posY = levelHeight * 0.9f;
		cannonMan.manAngle = -angle;
		// initial speed
		float speed = 1 * 0.2f;
		cannonMan.velX = speed * (float) Math.cos(angle * 3.14f / 180.0f);
		cannonMan.velY = speed * (float) -Math.sin(angle * 3.14f / 180.0f);

		float rad = (float) (cannonMan.manAngle * Math.PI / 180.0f)
				+ (float) Math.PI * 0.5f;
		man.setXForm(new Vec2(cannonMan.posX, cannonMan.posY), rad);// -
																	// (float)Math.PI
																	// * 0.5f);

		man.applyImpulse(new Vec2(cannonMan.velX, cannonMan.velY), man
				.getPosition());
		cannonMan.closeParachute();
		paraLocalPosition = null;
		
		accountant.init();
		
		mLastTime = System.currentTimeMillis() + 1000;
		//Debug.startMethodTracing();
		Log.i("cm", "Fire done");
	}

	public boolean onTouchEvent(MotionEvent event) {
		synchronized (mSurfaceHolder) {
			if (mCanvasWidth == 0 || mCanvasHeight == 0)
				return false;
			Vec2 coords = tmp3;
			coords.set(event.getX(), event.getY());
			coords.x *= levelWidth / mCanvasWidth;
			coords.y *= levelHeight / mCanvasHeight;

			if (mMode == STATE_READY || mMode == STATE_LOSE
					|| mMode == STATE_WIN) {
				doStart();
				return true;
			} else if (mMode == STATE_USER_INPUT) {
				if (fireButton.IsIn(coords)) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						fireButton.Tag();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						if (fireButton.IsTagged()) {
							fireButton.Untag();
							Fire();
						}
					}
					return true;
				}
				fireButton.Untag();

				fingerPosition = new Vec2(levelWidth * event.getX() / mCanvasWidth, levelHeight * event.getY() / mCanvasHeight);
				angle = (float) (180 * Math.atan2(mCanvasHeight * 0.9f
						- event.getY(), event.getX() - mCanvasWidth * 0.1f) / Math.PI);

				return true;
			} else if (mMode == STATE_RUNNING) {
				if (cannonMan.landed) {
					landingSpots.remove(cannonMan.spot);
					cannonMan.landed = false;
					// go !
				} else {
					if (driveLeft.IsIn(coords)) {
						driveRight.Untag();
						if (event.getAction() == MotionEvent.ACTION_UP)
							driveLeft.Untag();
						else
							driveLeft.Tag();
					} else if (driveRight.IsIn(coords)) {
						driveLeft.Untag();
						if (event.getAction() == MotionEvent.ACTION_UP)
							driveRight.Untag();
						else
							driveRight.Tag();
					} else {
						driveLeft.Untag();
						driveRight.Untag();
	
						if (event.getAction() == MotionEvent.ACTION_UP) {
							if (cannonMan.openParachute()) {
								addParachute();
							} else {
								// paraLocalPosition = null;
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}

	int level;
	/**
	 * Starts the game, setting parameters for the current difficulty.
	 */
	public void doStart() {
		synchronized (mSurfaceHolder) {
			if (mMode == STATE_READY || mMode == STATE_LOSE
					|| mMode == STATE_WIN) {
				coins.clear();
				tempTexts.clear();
				landingSpots.clear();
				
				// Math.random()
				mLastTime = System.currentTimeMillis() + 100;
				setState(STATE_USER_INPUT);

				angle = 40;
				gravity = 50;
				MaxWindSpeed = 1;

				man.setAngularVelocity(0);
				man.setLinearVelocity(new Vec2(0, 0));
				maxSpeed = 0;
				
				level = 1;
				
				initLevelItems(0);
				initLevelItems(1);
				
				windSpeedX = -MaxWindSpeed + (float) Math.random() * 2 * MaxWindSpeed;
			}
		}
	}
	
	void initLevelItems(int offset) {
		float levelH = levelHeight;
		float baseHeight = levelH * offset;
		
		// init coins
		int total = 25;
		
		int bonus = Math.max(1, total - level);
		int malus = total - bonus;
		int deadly = level;
		
		
		for(int i=0; i<bonus; ++i) {
			Vec2 pos = new Vec2((float)Math.random() * levelWidth * 0.6f + levelWidth * 0.2f,
				(float)Math.random() * levelH * 0.90f + baseHeight);
			
			Coin c = new Coin(100, btnRadius * 0.25f, pos, Color.YELLOW);
			c.oscillator = new Oscillator(new Vec2[] { pos, pos.add(new Vec2(c.radius * 0.1f * level, 0))}, 1, (float)Math.random(), (int)Math.round(Math.random()));
			coins.add(c);
		}
		for(int i=0; i<malus; ++i) {
			Vec2 pos = new Vec2((float)Math.random() * levelWidth * 0.6f + levelWidth * 0.2f,
				(float)Math.random() * levelH * 0.90f + baseHeight);
			Coin c = new Coin(-500, btnRadius * 0.25f, pos, Color.RED);
			c.oscillator = new Oscillator(new Vec2[] { pos, pos.add(new Vec2(c.radius * 0.1f * level, 0))}, 1, (float)Math.random(), (int)Math.round(Math.random()));
			coins.add(c);
		}
		for(int i=0; i<deadly; ++i) {
			Vec2 pos = new Vec2((float)Math.random() * levelWidth * 0.6f + levelWidth * 0.2f,
				(float)Math.random() * levelH * 0.90f + baseHeight);
			Coin c = new Coin(Coin.DEADLY, btnRadius * 0.25f, pos, Color.BLACK);
			c.oscillator = new Oscillator(new Vec2[] { pos, pos.add(new Vec2(c.radius * 0.1f * level, 0))}, 1, (float)Math.random(), (int)Math.round(Math.random()));
			coins.add(c);
		}
		
		landingSpots.add(new LandingSpot(new Vec2(levelWidth * 0.05f, levelWidth * 0.01f), 
				new Vec2((float)Math.random() * levelWidth, levelHeight * (1 + offset)), Color.RED));
	}

	/**
	 * Pauses the physics update & animation.
	 */
	public void pause() {
		synchronized (mSurfaceHolder) {
			if (mMode == STATE_RUNNING)
				setState(STATE_PAUSE);
		}
	}

	/**
	 * Restores game state from the indicated Bundle. Typically called when the
	 * Activity is being restored after having been previously destroyed.
	 * 
	 * @param savedState
	 *            Bundle containing the game state
	 */
	public synchronized void restoreState(Bundle savedState) {
		synchronized (mSurfaceHolder) {
			setState(STATE_PAUSE);
		}
	}

	float remainingDt;
	@Override
	public void run() {
		while (mRun) {
			Canvas c = null;
			try {
				c = mSurfaceHolder.lockCanvas(null);
				synchronized (mSurfaceHolder) {
					boolean mustDraw = true;
					if (mMode == STATE_RUNNING) {
						long now = System.currentTimeMillis();
						// Do nothing if mLastTime is in the future.
						// This allows the game-start to delay the start of the physics
						// by 100ms or whatever.
						if (mLastTime > now) {
							// do nothing
							remainingDt = 0;
						}else {
							float dt = (float) ((now - mLastTime) / 1000.0);
							dt *= TimeScale;
							
							final float fixedTimestep = 0.01667f;
							// fixed timestep
							
							dt += remainingDt;
														
							mustDraw = false;
							
							while(dt >= fixedTimestep) {
								update(fixedTimestep);
								dt -= fixedTimestep;
								mustDraw = true;
							}
							remainingDt = dt;
							
							mLastTime = now;
						}
					}
					if (mustDraw)
						doDraw(c);
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
	}
	
	void update(float dt) {
		if (scoreMultiplier != null) {
			scoreMultiplier.update(dt);
			if (scoreMultiplier.progress >= 1) {
				setState(STATE_WIN);
				scoreMultiplier = null;
			}
		} else {
			accountant.update(dt);
			
			// steer force
			if (paraLocalPosition != null) {
				final float driveForce = 0.02f * accountant.multiplier;
				if (driveLeft.IsTagged()) {
					tmp1.set(-driveForce, 0);
					man.applyForce(tmp1, man.getMemberPosition());
				} else if (driveRight.IsTagged()) {
					tmp1.set(driveForce, 0);
					man.applyForce(tmp1, man.getMemberPosition());
				}
			}

			if (!cannonMan.landed) updatePhysics(dt);
			
			checkVictory();
			
			cannonMan.update(dt, man);
			cannonMan.posX = man.getMemberPosition().x;
			cannonMan.posY = man.getMemberPosition().y;
			cannonMan.manAngle = (float) (man.getAngle() * 180 / Math.PI);
		}
		
		// update coins
		for(Coin coin : coins) {
			if (coin == null) continue;
			
			if ((coin.position.y + coin.radius) < (cannonMan.posY - levelHeight*0.5f)) {
				coins.set(coins.indexOf(coin), null);
				continue;
			}
			
			coin.oscillator.update(dt);
			coin.oscillator.current(coin.position);
			for(int i=0; i<4; ++i) {
				if (coin.IsInside(cannonMan.extents[i])) {
					coins.set(coins.indexOf(coin), null);
					if (coin.value == Coin.DEADLY) {
						int score = accountant.endScore();
						setState(STATE_LOSE, "Score : " + Integer.toString(score));
						Debug.stopMethodTracing();
					} else {
						float bonus = accountant.addIncome(coin.value); 
						
						int color;
						Vec2 p = coin.position;
						p.y -= coin.radius;
						char prefix;
						if (coin.value > 0) {
							prefix = '+';
							color = Color.DKGRAY;
						} else {
							prefix = '-';
							color = Color.LTGRAY;
						}
						tempTexts.add(new TemporaryText(prefix + Integer.toString((int)Math.abs(bonus)), p, 0.4f, color, 1.0f));
					}
					break;
				} 
			}
		}
		// update temp text
		for(TemporaryText text : tempTexts) {
			if (text != null) {
				text.update(dt);
				if (text.progress >= 1) tempTexts.set(tempTexts.indexOf(text), null);
			}
		}
		// update landing spot
		for(LandingSpot spot : landingSpots) {
			if (spot != null) {
				if (spot.rect.bottom < cannonMan.posY) landingSpots.set(landingSpots.indexOf(spot), null);
				if (spot.IsInside(cannonMan.posX, cannonMan.maxY())) {
					// -> landed !
					accountant.validateTempScore();
					cannonMan.land(spot);
					man.getLinearVelocity().setZero();
					man.setAngularVelocity(0);
					man.setXForm(man.getMemberPosition(), (float)Math.PI);
				}
			}
		}
		
		// clear null objects
		while(coins.remove(null));
		while(tempTexts.remove(null));
		while(landingSpots.remove(null));
		
		
	}

	/**
	 * Dump game state to the provided Bundle. Typically called when the
	 * Activity is being suspended.
	 * 
	 * @return Bundle with this view's state
	 */
	public Bundle saveState(Bundle map) {
		return map;
		/*
		synchronized (mSurfaceHolder) {
			if (map != null) {
			}
		}
		return map;
		*/
	}

	public void setRunning(boolean b) {
		mRun = b;
	}

	public void setState(int mode) {
		setState(mode, null);
	}

	public void setState(int mode, CharSequence message) {
		synchronized (mSurfaceHolder) {
			mMode = mode;

			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			if (mMode == STATE_RUNNING) {
				b.putString("text", "");
				b.putInt("viz", View.INVISIBLE);
			} else {
				Resources res = mContext.getResources();
				CharSequence str = "";

				switch (mMode) {
				case STATE_LOSE:
					str = res.getText(R.string.lose);
					break;
				case STATE_WIN:
					str = res.getText(R.string.win);
					break;
				case STATE_READY:
					str = res.getText(R.string.ready);
					break;
				case STATE_USER_INPUT:
					str = res.getText(R.string.input);
					break;
				}

				if (message != null) {
					str = message + "\n" + str;
				}
				b.putString("text", str.toString());
				b.putInt("viz", View.VISIBLE);
			}
			msg.setData(b);
			mHandler.sendMessage(msg);
		}
	}

	/* Callback invoked when the surface dimensions change. */
	public void setSurfaceSize(int width, int height) {
		// synchronized to make sure these all change atomically
		synchronized (mSurfaceHolder) {
			mCanvasWidth = width;
			mCanvasHeight = height;

			levelWidth = 10;
			levelHeight = mCanvasHeight * 10 / mCanvasWidth;

			cannonMan = new Man(levelWidth * 0.03f * 0.3f, levelWidth * 0.03f);
			addMan();

			btnRadius = levelWidth * 0.1f;

			fireButton = new HUDButton.HUDCircularButton(Color.GREEN,
					Color.RED, new Vec2(btnRadius * 0.6f, levelHeight
							- btnRadius * 0.6f), btnRadius * 0.5f,
					new HUDButton.HUDText("FIRE !"));
			int dirColor = Color.argb(150, 0, 0, 255);
			int dirColor2 = Color.argb(150, 150, 0, 255);
			driveLeft = new HUDButton.HUDRectangularButton(dirColor, dirColor2,
					new Vec2(levelWidth * 0.05f, levelHeight * 0.4f), new Vec2(
							levelWidth * 0.1f, levelHeight * 0.8f),
					new HUDButton.HUDText("STEER LEFT", 0.6f, -90));
			driveRight = new HUDButton.HUDRectangularButton(dirColor,
					dirColor2,
					new Vec2(levelWidth * 0.95f, levelHeight * 0.4f), new Vec2(
							levelWidth * 0.1f, levelHeight * 0.8f),
					new HUDButton.HUDText("STEER RIGHT", 0.6f, 90));
		}
	}

	/**
	 * Resumes from a pause.
	 */
	public void unpause() {
		// Move the real time clock up to now
		synchronized (mSurfaceHolder) {
			mLastTime = System.currentTimeMillis() + 100;
		}
		setState(STATE_RUNNING);
	}

	void drawViewDependent(Canvas canvas) {
		// draw cannon
		if (mMode == STATE_USER_INPUT) {
			mPaint.setColor(Color.BLACK);
			float w = levelWidth * 0.05f;
			drawRectangle(canvas, levelWidth * 0.1f, levelHeight * 0.9f, w,
					w * 0.3f, -angle, true);
		}
		
		// draw coins
		if (mMode == STATE_USER_INPUT || mMode == STATE_RUNNING || mMode == STATE_LOSE || mMode == STATE_WIN) {
			for(Coin coin : coins)
				if (coin != null)
					coin.Draw(canvas, mPaint);
			for(TemporaryText txt : tempTexts)
				if (txt != null)
					txt.Draw(canvas, mPaint);
		}
		
		// draw cannon man
		if (mMode == STATE_RUNNING || mMode == STATE_LOSE || mMode == STATE_WIN) {
			drawIndicator(canvas);
			cannonMan.draw(canvas, mPaint, man, paraLocalPosition);
			
			for(LandingSpot spot : landingSpots) {
				spot.Draw(canvas, mPaint);
			}
		}
		
		// draw target
		/*if (mMode == STATE_USER_INPUT || mMode == STATE_RUNNING
				|| mMode == STATE_LOSE || mMode == STATE_WIN) {
			float w = levelWidth * 0.05f;
			drawTriangle(canvas, targetX, levelHeight * 0.9f + w,
					w * 0.2f, 0, Color.RED);
			
			mPaint.setColor(Color.BLACK);
			drawRectangle(canvas, targetX, levelHeight * 0.9f, w, w * 0.15f, 0,
					true);
		}*/

		// draw ground
		/*
		mPaint.setColor(Color.LTGRAY);
		mPaint.setAlpha(50);
		drawRectangle(canvas, levelWidth * 0.5f, levelHeight * 0.95f,
				levelWidth, levelHeight * 0.1f, 0, true);
		*/
		/*
		mPaint.setStrokeWidth(mPaint.getStrokeWidth() * 4);
		if ((level % 2) == 0)
			mPaint.setColor(Color.CYAN);
		else
			mPaint.setColor(Color.MAGENTA);
		mPaint.setAlpha(20);
		mPaint.setStyle(Style.FILL);
		canvas.drawRect(0, 0, levelWidth, levelHeight, mPaint);
		mPaint.setStrokeWidth(mPaint.getStrokeWidth() / 4);
		mPaint.setStyle(Style.FILL);
	*/
		if (scoreMultiplier != null) 
			scoreMultiplier.Draw(canvas, mPaint);
	}
	
	void drawViewIndependent(Canvas canvas) {
		if (mMode == STATE_USER_INPUT) {
			if (fingerPosition != null) {
				mPaint.setColor(Color.BLACK);
				float w = mPaint.getStrokeWidth();
				mPaint.setStrokeWidth(w * 2);
				float startX = levelWidth * 0.1f, endX = fingerPosition.x;
				float startY = levelHeight * 0.9f, endY = fingerPosition.y;
				
				Vec2 dir = new Vec2(endX - startX, endY - startY);
				float length = dir.normalize();
				int dotCount = 7;
				float dotLength = length / dotCount;
				
				while(dotLength > levelWidth * 0.05f) {
					dotCount += 2;
					dotLength = length / dotCount;
				}
				
				for(int i=0; i<dotCount; ++i) {
					endX = startX + dir.x * dotLength; endY = startY + dir.y * dotLength;
					
					if ((i % 2) == 0)
						canvas.drawLine(startX, startY, endX, endY, mPaint);
					
					startX = endX; startY = endY;
				}
				canvas.drawLine(startX, startY, endX, endY, mPaint);
				canvas.drawCircle(endX, endY, dotLength, mPaint);
				fingerPosition = null;
				mPaint.setStrokeWidth(w);
			}
		}
		
		// draw fire button
		if (mMode == STATE_USER_INPUT) {
			fireButton.Draw(canvas, mPaint);
		}

		if (mMode == STATE_RUNNING) {
			if (paraLocalPosition != null) {
				driveLeft.Draw(canvas, mPaint);
				driveRight.Draw(canvas, mPaint);
			}
		}
		
		
		if (mMode >= STATE_USER_INPUT) {
			mPaint.setAntiAlias(false);
			mPaint.setARGB(150, 0, 0, 0);
			mPaint.setStyle(Style.STROKE);
			drawRectangle(canvas, levelWidth * 0.2f, levelHeight * 0.1f,
					levelWidth * 0.2f, levelHeight * 0.05f, 0, true);

			mPaint.setStyle(Style.FILL);
			// draw wind
			float w = levelWidth * 0.1f * Math.abs(windSpeedX) / MaxWindSpeed;
			float pos = levelWidth * 0.2f + w * 0.5f * Math.signum(windSpeedX);
			mPaint.setARGB(128, 10, 10, 200);
			drawRectangle(canvas, pos, levelHeight * 0.1f, w,
					levelHeight * 0.05f, 0, true);

			mPaint.setARGB(150, 0, 0, 0);
			canvas.drawLine(levelWidth * 0.2f, levelHeight * 0.075f,
					levelWidth * 0.2f, levelHeight * 0.125f, mPaint);
			mPaint.setAntiAlias(false);
		}
		
		if (mMode == STATE_RUNNING || mMode == STATE_LOSE || mMode == STATE_WIN) {
			drawSpeedoMeter(canvas, levelWidth - btnRadius * 0.6f, levelHeight
					- btnRadius * 0.6f, btnRadius * 0.5f, man
					.getLinearVelocity().y);
			
			// draw score
			mPaint.setTextAlign(Align.CENTER);
			mPaint.setTextSize(0.6f);
			mPaint.setColor(Color.BLACK);
			stringBuffer.setLength(0);
			stringBuffer.append(accountant.currentScore());
			stringBuffer.append("+");
			stringBuffer.append(accountant.tempScore());
			canvas.drawText(stringBuffer.toString(), levelWidth * 0.5f, levelHeight, mPaint);
			
			// draw level
			mPaint.setTextAlign(Align.CENTER);
			mPaint.setTextSize(0.6f);
			mPaint.setColor(Color.BLACK);
			canvas.drawText(Integer.toString(level), levelWidth * 0.1f, levelHeight, mPaint);			
		}
	}
	
	private void doDraw(Canvas canvas) {
		canvas.save();
		canvas.scale(mCanvasWidth / levelWidth, mCanvasHeight / levelHeight);

		// clear
		canvas.drawARGB(255, 255, 255, 255);

		canvas.save();
		if (mMode == STATE_RUNNING) {
			canvas.translate(0, -cannonMan.posY + levelHeight*0.5f);
		}
		drawViewDependent(canvas);
		canvas.restore();
		
		drawViewIndependent(canvas);

		// draw state lose/win
		if (mMode == STATE_LOSE) {
			canvas.drawARGB(128, 255, 0, 0);
		} else if (mMode == STATE_WIN) {
			canvas.drawARGB(128, 0, 255, 0);
		}

		canvas.restore();
	}

	void drawSpeedoMeter(Canvas canvas, float x, float y, float radius,
			float speed) {
		if (true) return;
		mPaint.setColor(Color.DKGRAY);
		mPaint.setAlpha(128);
		canvas.drawCircle(x, y, radius, mPaint);

		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(Color.BLACK);
		canvas.drawCircle(x, y, radius, mPaint);

		mPaint.setAntiAlias(false);
		double step = Math.PI / 21;
		int traitCount = 11 * 3 + 1;
		for (int i = 0; i < traitCount; ++i) {
			float c = (float) Math.cos(i * step);
			float s = (float) -Math.sin(i * step);

			float size = (i % 3 == 0) ? 0.7f : 0.95f;
			canvas.drawLine(x + c * radius, y + s * radius, x + c * radius
					* size, y + s * radius * size, mPaint);
		}

		mPaint.setStrokeWidth(0.05f);

		if (speed < 0)
			speed = 0;
		if (speed > maxSpeed)
			maxSpeed = speed;
		float MaxSpeed = 20;
		float current = (float) (traitCount * step * speed / MaxSpeed);
		mPaint.setColor(Color.DKGRAY);
		canvas.drawLine(x + (float) Math.cos(current) * radius * 0.9f, y
				- (float) Math.sin(current) * radius * 0.9f, x, y, mPaint);

		/*
		current = (float) (traitCount * step * maxSpeed / MaxSpeed);
		mPaint.setColor(Color.RED);
		canvas.drawLine(x + (float) Math.cos(current) * radius * 0.9f, y
				- (float) Math.sin(current) * radius * 0.9f, x, y, mPaint);
		*/
		mPaint.setStrokeWidth(0);
		if (paraLocalPosition == null)
			mPaint.setColor(Color.RED);
		else
			mPaint.setColor(Color.GREEN);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setTextSize(0.3f);
		
		stringBuffer.setLength(0);
		df.format(speed, stringBuffer, fp);
		canvas.drawText(stringBuffer.toString(), x, y, mPaint);

		mPaint.setStyle(Style.FILL);
	}

	void drawIndicator(Canvas canvas) {
		int color = Color.argb(150, 220, 20, 20);

		final float triRadius = levelHeight * 0.015f;
		final float triOffset = 3 * triRadius;
		if (cannonMan.posX > levelWidth - triOffset) {
			if (cannonMan.posY < triOffset) {
				float angle = (float) Math.atan2(-cannonMan.posY,
						cannonMan.posX - levelWidth);
				angle -= (float) Math.PI * 0.5f;
				angle = -angle * 180.0f / (float) Math.PI;

				drawTriangle(canvas, levelWidth - triOffset, triOffset,
						triRadius, angle, color);
			} else {
				drawTriangle(canvas, levelWidth - triOffset, cannonMan.posY,
						triRadius, 90, color);
			}
		} else if (cannonMan.posX < triOffset) {
			if (cannonMan.posY < triOffset) {
				float angle = (float) Math.atan2(-cannonMan.posY,
						cannonMan.posX - levelWidth);
				angle -= (float) Math.PI * 0.5f;
				angle = -angle * 180.0f / (float) Math.PI;
				drawTriangle(canvas, levelWidth - triOffset, triOffset,
						triRadius, angle, color);
			} else {
				drawTriangle(canvas, triOffset, cannonMan.posY, triRadius, -90,
						color);
			}
		} else {
			if (cannonMan.posY < triOffset) {
				drawTriangle(canvas, cannonMan.posX, 3 * triRadius, triRadius,
						0, color);
			}
		}
	}

	void drawRectangle(Canvas canvas, float x, float y, float w, float h,
			float angle, boolean restore) {
		// draw cannon
		canvas.save();
		canvas.translate(x - w * 0.5f, y - h * 0.5f);
		canvas.rotate(angle, w * 0.5f, h * 0.5f);

		this.mScratchRect.bottom = h; // cannon height
		this.mScratchRect.top = 0;
		this.mScratchRect.left = 0;
		this.mScratchRect.right = w; // cannon width
		canvas.drawRect(mScratchRect, mPaint);

		if (restore)
			canvas.restore();
	}

	void drawTriangle(Canvas canvas, float x, float y, float radius,
			float angle, int color) {
		canvas.save();
		canvas.translate(x, y - radius);
		canvas.rotate(angle, radius, radius);

		float[] verts = new float[3 * 2];

		double angle1 = Math.PI * 0.5;
		verts[0] = 0;
		verts[1] = -2 * radius;

		double angle2 = angle1 + Math.PI * 2.0 / 3.0;
		verts[2] = (float) Math.cos(angle2) * 2 * radius;
		verts[3] = -(float) Math.sin(angle2) * 2 * radius;

		verts[4] = -verts[2];
		verts[5] = verts[3];

		int[] colors = new int[3 * 2];
		for (int i = 0; i < 6; ++i)
			colors[i] = color;

		canvas.drawVertices(VertexMode.TRIANGLES, 3 * 2, verts, 0, null, 0,
				colors, 0, null, 0, 0, mPaint);
		canvas.restore();
	}

	/**
	 * Figures the lander state (x, y, fuel, ...) based on the passage of
	 * realtime. Does not invalidate(). Called at the start of draw(). Detects
	 * the end-of-game and sets the UI to the next state.
	 */
	private void updatePhysics(float dt) {
		// { TMP1
		man.getWorldDirectionToOut(UnitX, tmp1);
		// { TMP2
		tmp2.set(windSpeedX, 0);
		float windD = Vec2.dot(tmp1, tmp2);
		// } TMP1, TMP2
		final float WindCoeff = 0.01f; // 0.04f
		
		// { TMP2
		tmp2.set(Math.signum(windSpeedX) * Math.abs(windD * WindCoeff), 0);
		man.applyForce(tmp2, man.getMemberPosition());
		// } TMP2

		if (paraLocalPosition != null) {
			// { TMP1
			tmp1.set(man.getLinearVelocity());
			// { TMP2
			man.getWorldDirectionToOut(UnitNegY, tmp2);
			
			float d = Vec2.dot(tmp1, tmp2);
			// } TMP2
			if (d > 0) {
				tmp1.normalize();
				float coeff = 0.05f / accountant.multiplier;
				tmp1.x *= (-d * d * coeff);
				tmp1.y *= (-d * d * coeff);
				// { TMP2
				man.getWorldLocationToOut(paraLocalPosition, tmp2);
				man.applyForce(tmp1, tmp2);
				// } TMP2
			}
			// } TMP1

			float angle = man.getAngle();
			while (angle > 2 * Math.PI)
				angle -= 2 * Math.PI;
			while (angle < 0)
				angle += 2 * Math.PI;

			float angDiff = angle - (float) Math.PI;

			// current angular velocity
			float angVel = man.getAngularVelocity();
			float timeToRecover = 0.5f;
			float targetVelocity = -angDiff / timeToRecover;
			float diffVel = targetVelocity - angVel;

			float torque = diffVel * man.m_I * 10 * 3;
			float maxTorque = 5f;
			float r = (float) Math.random() * 0.4f + 0.8f;
			man.applyTorque((torque - man.m_torque * 0.3f));
		}
		world.step(dt, 5);
	}
	
	private void checkVictory() {
		float maxY = cannonMan.maxY();
		// check victory/lose conditions
		if (man.getLinearVelocity().y > 0 && maxY >= levelHeight) {
			// change position of everyone :)
			Vec2 p = man.getPosition();
			p.y -= levelHeight;
			man.setXForm(p, man.getAngle());
			
			for(Coin c : coins)
				if (c != null) c.position.y -= levelHeight;
			for(TemporaryText t : tempTexts)
				if (t != null) t.position.y -= levelHeight;
			for(LandingSpot s : landingSpots)
				if (s != null) { s.rect.bottom -= levelHeight; s.rect.top -= levelHeight; }
			
			level++;
			
			// init next level
			initLevelItems(1);
			
			windSpeedX = -MaxWindSpeed + (float) Math.random() * 2 * MaxWindSpeed;
			
			if (true)return;
			
			
			// target hit ?
			// ...
			
			// compute score multiplier
			/*float multiplier = 0;
			float maxDistance = Math.max(targetX, levelWidth - targetX);
			
			float dist = Math.abs(cannonMan.posX - targetX);
			
			if (dist >= maxDistance) {
				multiplier = 0;
			} else {
				float ratio = (1 - dist / maxDistance);
				ratio *= ratio;
				
				// 10 -> 0
				multiplier = ratio * 3; 
			}
			
			scoreMultiplier = new TemporaryText("x" + df.format(multiplier), man.getPosition().add(new Vec2(0, -cannonMan.h * 2)), 0.5f, Color.BLACK, 3);
			score *= multiplier;*/
		}
	}
}

