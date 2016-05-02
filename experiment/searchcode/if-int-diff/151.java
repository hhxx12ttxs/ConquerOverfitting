package org.linuxuser.android.iconic;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class GameLines extends Activity {
	private static final String TAG = "Iconic";

	private void alert(String s) {
    	Toast.makeText(GameLines.this, s, Toast.LENGTH_SHORT).show();
    }
 
    /* 
     * ****************** 
     * Our Playfield View 
     * ****************** 
     */
    public class PlayField extends View {
    	// Variables 
		private ShapeDrawable[] mDrawables = new ShapeDrawable[60];
		private ShapeDrawable[] mProgressBar = new ShapeDrawable[2];
		
		private int mode = 0; // 0=Training, 1=Medium Survival, 2=Hard Survival
		public final int MODE_EASY = 0;
		public final int MODE_MEDIUM= 1;
		public final int MODE_HARD = 2;

		private int status = 0; //
		public final int STATUS_STOPPED = 0;
		public final int STATUS_PLAYING = 1;
		public final int STATUS_PAUSED = 2;
		
		private int count = 0;

		public int COLOR1 = 0xff74AC23;
		public int COLOR2 = 0xfffafade;
		
		private int lastItem = -1;
		private long startTime;
		
		private int level = 0;
		
		// Preset Levels, currently not used
		private String[] levels = {
				// "000000000000000000000000000000000000000000111111111111",
				"000000000000000000001100001100000000000000000000000000",
				"100000010000001000000100001000010000000000000100000000",
				"000000001100010010010010001100000000000000000100010000",
				"010100010100010100101010010101101010010100010100010100",
				"110000011000001100000110000011000110001100011000110000",
				"010100010100010101010101010101010101010101010001010001",
				"001100011110011110001100001100001100011110110011100001",
				"000000111111100001101101101101100001111111000000000000",
				"111100000100000100000100000100000100000100000100000111",
				"011110001100011110111111011110001100001100011110111111"
		};
		
		// Colors are taken randomly out of this array
		private int[] colors = {
				0xff74AC23,
				0xff4D70CF,
				0x00000000,
				0xffEF0E2C,
				0xff5F6F45,
				0xffFF5FCF,
				0x00000000,
				0xff4A475F
		};
		
		// Pattern Bitfields (0, 1)
		private String pattern = "";
		private String pattern_target = "";

		// Points & Resets
		public int totalPoints = 0;
		private int resets = 3;
		
		// Default Preview Delay in ms
		public int previewDelay = 3000;

		// Time Left Infos
		public int timePerRound = 100; // Default Time
		private int timeLeftWidth = 300; // Max width of progress bar 

		private int timeLeft = 0;	  // Updated by game 

		// Layout Options
		public int width = 320;
		public int rows = 7;
		public int columns = 5;
		
		// Drawing if set to true if a user can actually touch and play
		private boolean drawing = false;
		
		// Handler for different update Threads
		private Handler mHandler2;
		private Handler mHandler3;
		private Handler mHandler4; // Time Left Info
		
		private boolean isPreview = false;

		
		// Random Number Generator
		private Random randGen = new Random(System.currentTimeMillis());
		private Vibrator vibrator;
		
		private Context c;
		
	    public PlayField(Context context) {
	        super(context);
	        c = context;
	        int w = (width / columns) - 6; //50;
	        int h = w;

	        int left = 4; //(width - ((w*columns)) / 2;

	        int x = left;
	        int y = 4;

	        count = 0;

	        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	        
	        // Create Playfield
	        for (int i=0; i<rows; i++) {
	        	for (int j=0; j<columns;j++) {
	       			ShapeDrawable mDrawable = new ShapeDrawable(new OvalShape());
	       	        mDrawable.getPaint().setColor(colors[2]);
	       	        mDrawable.setBounds(x, y, x + w, y + h);
	       	        mDrawables[count] = mDrawable;	        		
	        		x += w + 6;
	        		count++;
	        	}
	        	y += h + 6;
	        	x = left;
	        }

	        mProgressBar[0] = new ShapeDrawable(new RectShape());
	        mProgressBar[0].getPaint().setColor(COLOR1);
	        mProgressBar[0].setBounds(10, 465, 10 + timeLeftWidth, 466);
	        
	        mHandler2 = new Handler();
	        mHandler3 = new Handler();
	        mHandler4 = new Handler();

	        //mp = MediaPlayer.create(context, R.raw.cheer1);
	        //mpClick = MediaPlayer.create(context, R.raw.push);
	        
	        COLOR1 = colors[3];
	        
	        Intent intent = getIntent();
	        Bundle extras = intent.getExtras();
	        mode = Integer.valueOf(extras.get("org.linuxuser.android.iconic.mode").toString());
	        loadMode(mode);
	        
	        pattern_target = getPattern(-1); //level); 
	        //loadPattern(pattern_target);
	    }

	    protected void start() {
	        mHandler2.post(previewLevel);	        
	    }
	    
	    protected void onDraw(Canvas canvas) {
	    	for (int i=0; i<count; i++) {
	    		mDrawables[i].draw(canvas);
	    	}
	    	mProgressBar[0].draw(canvas);
	    }
	    
	    private void loadMode(int mode) {
	    	// Load mode specific settings
	    	// Log.v(TAG, "mode: " + mode);
	    	if (mode == 0) {
	    		// Training (Easy)
	    		COLOR1 = colors[1]; // blue
	    		previewDelay = 3400;
	    		
	    	} else if (mode == 1) {
	    		// Survival (Medium)
	    		COLOR1 = colors[0]; // green
	    		
	    	} else if (mode == 2) {
	    		// Survival (Hard)
	    		COLOR1 = colors[2]; // black
	    		previewDelay = 2600;
	    		timePerRound = 8;
	    	}
	        mProgressBar[0].getPaint().setColor(COLOR1);
	    }
	    
	    public void fieldUpdate() {
	    	if (!drawing) {
	    		return ;
	    	}
	    	
	    	int first = mDrawables[0].getPaint().getColor();	    	
	    	for (int i=0; i<this.count; i++) {
	    		if (i == this.count-1) {
	    			mDrawables[i].getPaint().setColor(first);
	    		} else {
	    			mDrawables[i].getPaint().setColor(mDrawables[i+1].getPaint().getColor()+ i * 256);
	    		}
	    	}
    	}

	    public boolean inCircle(float center_x, float center_y, float radius, float x, float y) {
	    	float square_dist = ((center_x-x)*(center_x-x)) + ((center_y-y)*(center_y-y));
	    	return square_dist <= (radius * radius);
	    }

	    public void fieldUpdatePos(float x, float y) {
	    	if (drawing == false) { return ;}
	    	
	    	float radius = 27;
	    	
	    	for (int i=0; i<this.count; i++) {
	    		Rect r = mDrawables[i].getBounds();
	    		if (i != lastItem) {
	    			if (inCircle(x, y, radius, r.centerX(), r.centerY())) {
	    				if (mDrawables[i].getPaint().getColor() == COLOR1) {
	    					mDrawables[i].getPaint().setColor(COLOR2);
		    				if (i < pattern.length()) {
		    					pattern = pattern.substring(0, i) + "1" + pattern.substring(i+1, pattern.length());
		    				} else {
		    					pattern = pattern.substring(0, i) + "1";
		    				}
	    				} else {
	    					mDrawables[i].getPaint().setColor(COLOR1);	    				
		    				if (i < pattern.length()) {
		    					pattern = pattern.substring(0, i) + "0" + pattern.substring(i+1, pattern.length());
		    				} else {
		    					pattern = pattern.substring(0, i) + "0";
		    				}
	    				}

	    				// mpClick.start();
	    				
	    				lastItem = i;
	    				mHandler3.removeCallbacks(releaseLastItem);
	    				mHandler3.postDelayed(releaseLastItem, 650);
	    			}
	    		}
	    	}

	    	checkPattern();
	    }
	    
	    public void reset() {
	    	pattern = "";
	    	for (int i=0; i<this.count;i++) {
	    		mDrawables[i].getPaint().setColor(COLOR1);
	    		pattern += "0";
	    	}
    		//postInvalidate();
	    }

	    public void checkPattern() {
	    	if (!drawing) { return ;}	    	
	        //postInvalidate();
	    	if (pattern.compareTo(pattern_target) == 0) {
		        drawing = false;
	    		pattern = "";

	    		float diff = System.currentTimeMillis() - startTime;
	    		float s = (float) (diff / 1000.0);
	    		int timeDiff = 5 - (int) s;
	    		if (timeDiff < 0) {
	    			timeDiff = 0;
	    		}
	    		timeDiff *= 3.5;
	    		
	    		// Log.v(TAG, "Time Diff: " + timeDiff);
	    		
	    		level += 1;  
	    		
	    		int points = (int) (level + timeDiff);
	    		if (points < 0) {
	    			points = 0;
	    		}
	    		
	    		if (mode == MODE_MEDIUM) {
	    			if (level < 12) {
	    				points *= 0.7;
	    			}
	    		} else if (mode == MODE_EASY) {
	    			if (level < 10) {
	    				points *= 0.5;	    				
	    			}
	    		}
	    		
	    		totalPoints += points;

	    		if (level % 5 == 4) {
	    			if (timePerRound > 7) { 
	    				timePerRound -= 1;
	    			}
	    		}

    			if (mode == MODE_HARD) {
    	    		if (level < 58) {
    	    			previewDelay -= 45;
    	    		} else if (level < 60) {
    	    			previewDelay -= 5;
    	    		}
    			} else {
    				if (level < 72) {
    					previewDelay -= 40;
    				} else if (level < 80) {
    					previewDelay -= 5;
    				}
    			}	    			
	    		
		        COLOR1 = colors[randGen.nextInt(colors.length)];
		        if (mode == MODE_HARD) {
		        	if (COLOR1 != colors[2]) {
		        		if (randGen.nextBoolean()) {
		        			COLOR1 = colors[randGen.nextInt(colors.length)];
		        		}
		        	}
		        }
		        mHandler2.removeCallbacks(previewLevel);
		        mHandler2.postDelayed(previewLevel, 3000);
		        
		        mHandler4.removeCallbacks(updateTimeLeft);
		        mHandler4.postDelayed(updatePattern, 300);

		        if ((mode == 0) && (level == 1)) {
		        	alert("Great!");
		        } else if ((mode == 0) && (level == 10)) {
		        	alert("Really good... Keep on!");
		        } else if ((mode == 0) && (level == 20)) {
		        	alert("Awesome!");
		        } else if ((mode == 0) && (level == 50)) {
		        	alert("Brilliant!");
		        } else if ((mode == 0) && (level == 80)) {
		        	alert("You are a nerd!");
		        } else if ((mode == 0) && (level == 100)) {
		        	alert("You're probably a genious!");
		        } else {
		        	alert("Total Points: " + totalPoints);
		        }
		        vibrator.vibrate(100);

		        // mp.start();
	    	}
	    }
	    
	    public void logPattern() {
	    	String pattern = "";
	    	for (int i=0; i<this.count; i++) {
	    		if (mDrawables[i].getPaint().getColor() == COLOR2) {
		    		pattern += "1";	    			
	    		} else {
	    			pattern += "0";
	    		}
	    	}
        	// Log.v(TAG, "export: " + pattern);
	    }
	    
	    private void loadPattern(String pattern2) {
	    	//reset();
	    	for (int i=0; i<pattern2.length(); i++) {
	    		if (pattern2.charAt(i) == '1') {
	    			mDrawables[i].getPaint().setColor(COLOR2);
	    		} else {
	    			mDrawables[i].getPaint().setColor(COLOR1);	    			
	    		}	
	    	}
	    }
	    
	    private int getNeighborPoints(String pattern, int pos) {
	    	int points = 0;

	    	boolean isTop = pos < columns;
	    	boolean isBottom = pos >= this.count - columns;
	    	boolean isLeft = pos % columns == 0;
	    	boolean isRight =  pos % columns == columns - 1;
	    	
	    	int add2 = 2;
	    	if (mode == MODE_HARD) {
	    		if (level > 70) {
	    			add2 = 0;
	    		} else if (level > 60) {
	    			add2 = 1;
	    		}
	    	} else if (mode == MODE_MEDIUM) {
	    		if (level > 80) {
	    			add2 = 0;
	    		} else if (level > 90) {
	    			add2 = 1;
	    		}
	    	}
	    	
	    	// Check 1 Above
	    	if ((isTop) || (pattern.charAt(pos-columns) == '1')) {
	    		points += add2;
	    	}
	    	
	    	// Check 1 Left
	    	if ((isLeft) || (pattern.charAt(pos-1) == '1')) {
	    		points += 2;
    			if ((!isLeft) && ((pos-2>=0) && (pattern.charAt(pos-2)) == '1')) {
    				points += add2;
    			}
	    	}
	    	
	    	// Check 1 Right
	    	if (pos < this.count) {
	    		if ((isRight) || (pattern.charAt(pos+1) == '1')) {
	    			points += 2;
	    			if ((!isRight) && ((pos+2<this.count) && (pattern.charAt(pos+2) == '1'))) {
	    				points += add2;
	    			}
	    		}
	    	} else {
	    		points += add2;
	    	}
	    	
	    	// Check 1 Below
	    	if ((isBottom) || (pattern.charAt(pos+columns) == '1')) {
	    		points += add2;
	    	}
	    	
	    	return points;
	    }
	    
	    private int getLengthPoints(String p, int level) {
	    	int points = 0;
	    	int count1 = 0;
	    		    	
	    	for (int i=0; i<p.length(); i++) {
	    		if (p.charAt(i) == '1') {
	    			count1 += 1;
	    		}
	    	}
	    	
	    	int target1 = 4 + (level / 4);	 
	    	if (mode == MODE_HARD) {
	    		target1 += 1;
	    	}

	    	if (target1 > 12) {
	    		target1 = 12;
	    	}
	    	
	    	int diff = count1 - target1;
	    	
	    	if (diff > 0) {
	    		diff *= -1;
	    	}
	    	
	    	if (diff == 0) {
	    		points += 30;
	    	} else if (diff == -1) {
	    		points += 15;
	    	}
	    	points += diff*2;

	    	if (points > 10) {
		    	// Log.v(TAG, "length points: count1:" + count1 + ", target: " + target1 + " points: " + String.valueOf(points) + ", diff: " + diff);	    		
	    	}
	    	
	    	return points;
	    }
	    
	    private String getPattern(int l) {
	    	if (l == -1) {
	    		// Generate Random Pattern	    		
	    		Random generator = new Random(System.currentTimeMillis());
	    		
	    		String[] newpatterns = new String[100];
	    		
	    		int whites;
	    		// Generate Random Levels
	    		for (int x=0; x<100; x++) {	    			
	    			String p = "";
	    			whites = 0;
	    			for (int i=0; i<this.count; i++) {
	    				if (generator.nextInt(12) == 0) {
	    					p += "1";
	    					whites += 1;
	    				} else {
	    					p += "0";
	    				}
	    			}
	    			newpatterns[x] = p;
	    			// Dismiss blank ones
	    			
	    			if ((whites < 2) || (whites > 15)) {
	    				x -= 1;
	    				//Log.v(TAG, "forfeit w: " + whites);
	    			}
	    		}

	    		// Evaluate Levels for Neighbors
	    		int maxpoints = 0;
	    		int maxpointsid = 0;
	    		int tmpoints = 0;
	    		
	    		for (int x=0; x<newpatterns.length; x++) {
	    			tmpoints = 0;
	    			for (int j=0;j<newpatterns[x].length(); j++) {
	    				if (newpatterns[x].charAt(j) == '1') {
	    					tmpoints += getNeighborPoints(newpatterns[x], j); 
	    				}
	    			}
	    			
	    			int t2 = getLengthPoints(newpatterns[x], level);
	    			tmpoints += t2;
//	    			Log.v(TAG, "p-neib: " + tmpoints + " p-len: " + t2 + " -- w: ");
	    			if (tmpoints >= maxpoints) {
	    				maxpoints = tmpoints;
	    				maxpointsid = x;
	    			}
	    			
	    	    	if (tmpoints > 0) {
		    			Log.v(TAG, "p-neib: " + tmpoints + " p-len: " + t2 + " -- w: ");
	    	    	}

	    		}

	    		// Log.v(TAG, "max points: " + String.valueOf(maxpoints));
	    		
	    		return newpatterns[maxpointsid];

	    	} else {
	    		return levels[l];
	    	}
	    }

	    // This Event starts a new round after the preview
	    private Runnable startGame = new Runnable() {
	    	public void run() {
	    		if ((mode == 0) && (level == 0)) {
	    			alert("Where are they?!");
	    		}
	    		isPreview = false;
	    		drawing = true;
	    		startTime = System.currentTimeMillis();
	    		
	    		mHandler4.removeCallbacks(updatePattern);
	    		mHandler4.removeCallbacks(updateTimeLeft);
	    		mHandler4.postDelayed(updateTimeLeft, 1000);
	    		
	    		if (status == STATUS_PAUSED) {
		    		status = STATUS_PLAYING;	    		
		    		
		    		for (int i=0; i<level; i++) {
			    		if (mode == MODE_HARD) {
			    			previewDelay -= 45;
			    		} else {
				    		previewDelay -= 40;	    			
			    		}
			    		if (level % 5 == 4) {
			    			if (timePerRound > 7) { 
			    				timePerRound -= 1;
			    			}
			    		}		    			
		    		}
		    		if (timeLeft <= 0) {
		    			timeLeft = timePerRound;
		    		}
	    		} else {
		    		status = STATUS_PLAYING;	    			
		    		timeLeft = timePerRound;
		    		reset();
	    		}
	    	}
	    };

	    // Updates the Timer and checks if it ran out 
	    private Runnable updateTimeLeft = new Runnable() {
	    	public void run() {
	    		if (drawing) {
	    	        timeLeft -= 1;
	    			int w = (timeLeftWidth / timePerRound) * timeLeft;
	    			if (w < 0) {
	    				// Time Out
	    				if (resets > 0) {
	    					// Reset if resets left!
	    	        		resets -= 1;
	    	        		drawing = false;
	    	        		
	    	        		String s = "You have " + (resets+1) + " tries left.";
	    					if (resets == 0) {
	    	        			s = "That is your last try!";
	    	        		} else if (resets == 1) {
	    	        			s = "Two tries left...";
	    	        		}
	    	        		
	    					Builder a = new AlertDialog.Builder(GameLines.this).setMessage("The time ran out!\n\n" + s)
	    						.setPositiveButton("Try Again", new DialogInterface.OnClickListener(){
	    							public void onClick(DialogInterface dialog, int which) {
	    		    	        		mCustomDrawableView.reset();
	    		    	        		mHandler2.removeCallbacks(mCustomDrawableView.previewLevel);
	    		    	        		mHandler2.post(mCustomDrawableView.previewLevel);	    					
	    								dialog.dismiss();
	    							}
	    						});
	    					a.setOnCancelListener(new DialogInterface.OnCancelListener() {
								public void onCancel(DialogInterface arg0) {
    		    	        		mCustomDrawableView.reset();
    		    	        		mHandler2.removeCallbacks(mCustomDrawableView.previewLevel);
    		    	        		mHandler2.post(mCustomDrawableView.previewLevel);	    					
								}
	    					});
	    					a.show();
	    				} else {
	    					status = STATUS_STOPPED;
	    					// Game Over
	    					Intent intent = new Intent();
	    					intent.setClassName("org.linuxuser.android.iconic", "org.linuxuser.android.iconic.Finish");
	    					intent.putExtra("org.linuxuser.android.iconic.level", level+1);
	    					intent.putExtra("org.linuxuser.android.iconic.points", totalPoints);
	    					intent.putExtra("org.linuxuser.android.iconic.mode", mode);
	    					startActivity(intent);
	    					finish();
	    				}
	    			} else {
	    				mProgressBar[0].setBounds(10, 465, 10 + w, 466);
	    				mHandler4.postDelayed(updateTimeLeft, 1000);
	    			}
	    			postInvalidate();
	    		}
	    	}
	    };

	    // Preview the Level
	    private Runnable previewLevel = new Runnable() {
	    	public void run() {
	    		isPreview = true;
	    		if (mode == 0) {
	    			if (level == 0) {
	    				alert("See the white spots...?");
	    			} else if (level == 1) {
	    				alert("Faster => More Points!");	
	    			} else if (level == 3) {
	    				alert("On the bottom is the timeline...");
	    			} else if (level == 5) {
	    				alert("Preview is slightly shorter each level");
	    			} else {
		    			alert("Level " + (level+1));	    				
	    			}
	    		} else {
	    			alert("Level " + (level+1));
	    		}

		        mProgressBar[0].getPaint().setColor(COLOR1);
				mProgressBar[0].setBounds(10, 465, 10 + timeLeftWidth, 466);
	            loadPattern(pattern_target);
	            postInvalidate();
	            
	            mHandler2.removeCallbacks(startGame);
	            mHandler2.postDelayed(startGame, previewDelay);
	    	}
	    };

	    // Release Swap Lock on the last selected Item (after a given time)
	    private Runnable releaseLastItem= new Runnable() {
	    	public void run() {
	    		lastItem = -1;
	    	}
	    };

	    // Update the Pattern in the Background
	    private Runnable updatePattern = new Runnable() {
	    	public void run() {
	    		pattern_target = getPattern(-1); //level);
	    	}
	    };
    }

    
    /*
     * Main "Iconic Memory" Activity Below
     */
    
	PlayField mCustomDrawableView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request Window Features 
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // getWindow().setBackgroundDrawable();
    }
    
    @Override
    protected void onPause() { 
    	super.onPause();
    	mCustomDrawableView.mHandler2.removeCallbacks(mCustomDrawableView.startGame);
    	mCustomDrawableView.mHandler2.removeCallbacks(mCustomDrawableView.previewLevel);
    	mCustomDrawableView.mHandler3.removeCallbacks(mCustomDrawableView.releaseLastItem);
    	mCustomDrawableView.mHandler4.removeCallbacks(mCustomDrawableView.updateTimeLeft);

		if (mCustomDrawableView.status == mCustomDrawableView.STATUS_PLAYING) {
			mCustomDrawableView.status = mCustomDrawableView.STATUS_PAUSED;
		}

        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putInt("status", mCustomDrawableView.status);
        editor.putInt("level", mCustomDrawableView.level);
        editor.putInt("points", mCustomDrawableView.totalPoints);
        editor.putInt("mode", mCustomDrawableView.mode);
        editor.putInt("resets", mCustomDrawableView.resets);
        editor.putInt("timeLeft", mCustomDrawableView.timeLeft);
        editor.putInt("COLOR1", mCustomDrawableView.COLOR1);
        
    	String p = "";
        if (mCustomDrawableView.isPreview) {
        	for (int i=0; i<mCustomDrawableView.count; i++) {
        		p += "0";
        	}
        } else {
            p = mCustomDrawableView.pattern;        	
        }
        if (p.length() == 0) {
        	for (int i=0; i<mCustomDrawableView.count; i++) {
        		p += "0";
        	}
        }
        editor.putString("pattern", p);
        editor.putString("pattern_target", mCustomDrawableView.pattern_target);
        
        editor.commit();
        // Log.v(TAG, "pause: tl " + mCustomDrawableView.timeLeft);
    }
    

    @Override
    protected void onResume() {
    	super.onResume();
    	
        // Set the View
        mCustomDrawableView = new PlayField(this);
        setContentView(mCustomDrawableView);
        
        SharedPreferences prefs = getPreferences(0); 
    	mCustomDrawableView.status = prefs.getInt("status", 0);
    	
    	if ((mCustomDrawableView.status == mCustomDrawableView.STATUS_PAUSED)) {

    		// Restore last game settings!
        	mCustomDrawableView.mode = prefs.getInt("mode", 0);
        	mCustomDrawableView.level = prefs.getInt("level", 0);
        	mCustomDrawableView.totalPoints = prefs.getInt("points", 0);
        	mCustomDrawableView.resets = prefs.getInt("resets", 0);

        	mCustomDrawableView.loadMode(mCustomDrawableView.mode);
        	mCustomDrawableView.COLOR1 = prefs.getInt("COLOR1", mCustomDrawableView.COLOR1);
        	// mCustomDrawableView.COLOR2 = prefs.getInt("COLOR2", mCustomDrawableView.COLOR2);        	
        	
        	mCustomDrawableView.pattern = prefs.getString("pattern", "");
        	mCustomDrawableView.loadPattern(mCustomDrawableView.pattern);

        	mCustomDrawableView.pattern_target = prefs.getString("pattern_target", mCustomDrawableView.getPattern(-1));

			int w = (mCustomDrawableView.timeLeftWidth / mCustomDrawableView.timePerRound) * prefs.getInt("timeLeft", 0);
			mCustomDrawableView.mProgressBar[0].setBounds(10, 465, 10 + w, 466);
			mCustomDrawableView.mProgressBar[0].getPaint().setColor(mCustomDrawableView.COLOR1);

			// Log.v(TAG, "color1: " + mCustomDrawableView.COLOR1);
        	mCustomDrawableView.postInvalidate();

        	String m[] = {"Easy", "Medium", "Hard" };
        	Builder a = new AlertDialog.Builder(GameLines.this).setMessage("Game is paused!\n\n" + "* Mode: " + m[mCustomDrawableView.mode] + "\n* Level: " + (mCustomDrawableView.level+1) + "\n* Points: " + mCustomDrawableView.totalPoints)
			.setPositiveButton("Continue", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
			        SharedPreferences prefs = getPreferences(0); 
		        	mCustomDrawableView.timeLeft = prefs.getInt("timeLeft", -1);
					mCustomDrawableView.mHandler2.post(mCustomDrawableView.startGame);
				}
			})
			.setNegativeButton("New Game", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
			        mCustomDrawableView = new PlayField(GameLines.this);
			        mCustomDrawableView.drawing = false;
			        setContentView(mCustomDrawableView);
			        mCustomDrawableView.start();
				}
			});
			a.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
	        		mCustomDrawableView.reset();
	        		finish();
	        		
				}
			});
			a.show();    		

    	} else {
            mCustomDrawableView.start();
    	}
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();       
        switch (eventaction ) {
        	case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on a ball
        		mCustomDrawableView.fieldUpdatePos(event.getX(), event.getY());
                mCustomDrawableView.postInvalidate();
        		break;

        	case MotionEvent.ACTION_MOVE:   // touch drag with the ball
        		mCustomDrawableView.fieldUpdatePos(event.getX(), event.getY());
                mCustomDrawableView.postInvalidate();
        		break;

        	case MotionEvent.ACTION_UP:
                mCustomDrawableView.postInvalidate();                
        		break;
        }
        return true;     
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Pause");
        menu.add(0, 1, 1, "Quit");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int i = item.getItemId();
        if (i == 0) {
        	this.onPause();
        	this.onResume();
        } else if (i == 1) {
        	mCustomDrawableView.status = mCustomDrawableView.STATUS_STOPPED;
        	finish();
        }

        return false;
    }
}
