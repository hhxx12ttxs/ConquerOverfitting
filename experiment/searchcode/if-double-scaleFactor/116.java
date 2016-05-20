package com.robertkcheung.zrox;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.ParseUser;

public class GameActivity extends BaseActivity{
	/** Called when the activity is first created. */
	int lettersPlaced;
	StringBuilder currGuess;
	ImageView iv;
	boolean emptyGuess;
	double scaleFactor;
	Button CLEAR;
	Button PEEK;
	OnTouchListener clrListener;
	OnTouchListener submitListener;
	ParseUser USER;
	int w;
	Drawable mainImg;
    int imageheight = 0 ;
	String uri = "drawable/i";
	TranslateAnimation moveUp;
	CustomTextView leveltext;
	char[] currL;
	int SolutionLen;
	Context ctx;
			
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        w = metrics.widthPixels;
        currGuess = new StringBuilder();
        switch(metrics.densityDpi){
	        case DisplayMetrics.DENSITY_LOW:
	        	scaleFactor = .75;
	            break;
	        case DisplayMetrics.DENSITY_MEDIUM:
				scaleFactor = 1;
	            break;
	        case DisplayMetrics.DENSITY_HIGH:
				scaleFactor = 1.5;
	            break;
	        case DisplayMetrics.DENSITY_XHIGH:
				scaleFactor = 2;
	            break;
        }
        
        //initialize parse
        
        try{
        	//USER = ParseUser.getCurrentUser();
        	//USERNAME = USER.getUsername();

        }
        catch(NullPointerException npe){
        	// do something like show an error box, or go back to log in page
        }
        
        
        //get context
        ctx = this.getApplicationContext();
        //get these buttons
        
        CLEAR = (Button) findViewById(R.id.btnClr);
        PEEK = (Button) findViewById(R.id.btnPeek);
        CLEAR.setClickable(false);
        
        leveltext = (CustomTextView) findViewById(R.id.currentLeveltxt);
        leveltext.setText("LEVEL " + mLevel + " (TEST MODE)");
        
        SolutionLen = Solutions.answer[mLevel].length();
        currL = Solutions.letters[mLevel];
        //reset everything up (Board tiles, blanks, booleans, and anything else)
        resetAll();
        
        //set the mystery image
        //finally animate img
        setImg();

        imageheight = iv.getDrawable().getIntrinsicHeight();
        float imagepos = iv.getTop();
        float animationPadding = imageheight*.1f;
        float moveToHere = imagepos-imageheight;
        System.out.println("HEIGHT: " +  imageheight + " POS: " + imagepos);
    	moveUp = new TranslateAnimation(0, 0, imagepos+animationPadding, moveToHere-animationPadding);
        moveUp.setDuration(3500);
        moveUp.setRepeatCount(TranslateAnimation.INFINITE);
        moveUp.setRepeatMode(TranslateAnimation.RESTART);
        iv.startAnimation(moveUp);
        /*CLEAR.setOnClickListener(new OnClickListener(){
		@Override
			public void onClick(View v){
				resetAll();
			}
		});*/

	}
	public void onClearClick(View v){
		resetAll();
	}
	
	public void onPeekClick(View v){
		DialogFragment df = new CustomDialogFragment();
		df.show(getSupportFragmentManager(), "peekin");
		
	}
	
	public void setImg(){

		int imgRes = getResources().getIdentifier(uri + mLevel, null, getPackageName());
		//System.out.println("mLevel: " + mLevel);
        mainImg = getResources().getDrawable(imgRes);
		iv = (ImageView) findViewById(R.id.gameImg);
        iv.setImageDrawable(mainImg);
        Log.d("LEVELLLLLLLLLL", mLevel + "");
	}
	
	private void setBoard(){
		setLetters();
		setBlanks();
	}
	
	private void setLetters(){
		int res[] = {R.id.letter1,R.id.letter2,R.id.letter3,R.id.letter4,R.id.letter5,R.id.letter6,R.id.letter7,R.id.letter8,R.id.letter9,R.id.letter10,R.id.letter11,R.id.letter12};
		
		for(int i=0; i<12; i++){
			//for each letter in bank
			ImageView curriv = (ImageView) findViewById(res[i]);
			int letterNum = currL[i] - 96; //get letter number from ascii number ie a=1, b=2, etc.
			int imgRes = getResources().getIdentifier("drawable/l" + letterNum, null, getPackageName());
	        Drawable letterd = getResources().getDrawable(imgRes); 
			curriv.setImageDrawable(letterd);
			curriv.setTag("letter" + i);
			
			CustomClickListener listener = new CustomClickListener(curriv, currL[i]) { //create custom clicklistener with iv and current letter
	            public void onClick(View v) {

	            	if(clickable){ //only clickable if letter is still in bank
		            	Drawable currletter = this.iv.getDrawable();
		            	
		            	LinearLayout ll = (LinearLayout) findViewById(R.id.blankHolder);
		        		try{
		        			if(emptyGuess){ //if guess is currently empty. ie. this is the first letter to be pressed
		        				emptyGuess = false;
		        				CLEAR.setClickable(true);
		        				CLEAR.setTextColor(ctx.getResources().getColor(R.color.box_text_color));
		        				//TODO:clearable now
		        			}
		        			int indexOfExclaimation = currGuess.indexOf("!");
		        			int nextBlank = indexOfExclaimation==-1? lettersPlaced:indexOfExclaimation;
		        			
		        			ImageView currblank = (ImageView) ll.findViewWithTag("blank" + nextBlank);
		        			
		        			currblank.setImageDrawable(currletter);
		        			currGuess.insert(nextBlank,this.let);
		        			lettersPlaced++;
		        			iv.setImageDrawable(getResources().getDrawable(R.drawable.blank));
		        			if(lettersPlaced==SolutionLen){
		        				evaluateAns();
		        			}
		        			
		        		}
		        		catch(NullPointerException e){
		        			//evaluateAns(mLevel);
		        		}
		        		clickable = false;
		            }
	            }
	        };

			curriv.setOnClickListener(listener);
		}
	}
	public void evaluateAns(){
		String finalGuess = currGuess.toString();
		if(finalGuess.equalsIgnoreCase(Solutions.answer[mLevel])){
			//Toast t = Toast.makeText(getApplicationContext(), "Yiss", Toast.LENGTH_SHORT);
			//t.show();
			congratulateUser();
		}
		else{
			Toast t = Toast.makeText(getApplicationContext(), "INCORRECT, BOOOO!", Toast.LENGTH_SHORT);
			t.show();
			resetAll();
		}
	}
	private void setBlanks(){
		LinearLayout ll = (LinearLayout) findViewById(R.id.blankHolder);
		ll.removeAllViews();
		
		int pix = (int) (SolutionLen * 44 * scaleFactor); 
		boolean needToScale = (pix>=(w-(int)(20*scaleFactor + .5)));
		for(int i=0; i<SolutionLen; i++){
			ImageView iv = new ImageView(this);
			iv.setBackgroundResource(R.drawable.blank);
			LinearLayout.LayoutParams lp; 
			if(needToScale){
				int height = (int)(((w - (20 * scaleFactor))/SolutionLen) - 4*scaleFactor);
				lp = new LinearLayout.LayoutParams((height), (height));
				lp.setMargins((int)(2*scaleFactor), 0, (int)(2*scaleFactor), 0);
			}
			else{
				lp = new LinearLayout.LayoutParams((int)(40*scaleFactor), (int)(40*scaleFactor));
				lp.setMargins((int)(2*scaleFactor),0,(int)(2*scaleFactor),0);
			}

			iv.setLayoutParams(lp);
			iv.setTag("blank" + i);
			ll.addView(iv);
			CustomClickListener listener = new CustomClickListener(iv, '!') { //create custom clicklistener with iv and current letter
	            public void onClick(View v) {

	            	if(clickable){ //only clickable if letter is still in bank
		            	Drawable currletter = this.iv.getDrawable();
		            	LinearLayout ll = (LinearLayout) findViewById(R.id.blankHolder);
		            	
		        		try{
		        			if(emptyGuess){ //if guess is currently empty. ie. this is the first letter to be pressed
		        				emptyGuess = false;
		        				CLEAR.setClickable(true);
		        				//TODO:clearable now
		        			}
		        			int indexOfExclaimation = currGuess.indexOf("!");
		        			int nextBlank = indexOfExclaimation==-1? lettersPlaced:indexOfExclaimation;
		        			
		        			ImageView currblank = (ImageView) ll.findViewWithTag("blank" + nextBlank);
		        			
		        			currblank.setImageDrawable(currletter);
		        			currGuess.insert(nextBlank,this.let);
		        			lettersPlaced++;
		        			iv.setImageDrawable(getResources().getDrawable(R.drawable.blank));
		        			if(lettersPlaced==Solutions.answer[mLevel].length()){
		        				evaluateAns();
		        			}
		        			
		        		}
		        		catch(NullPointerException e){
		        			//evaluateAns(mLevel);
		        		}
		        		clickable = false;
		            }
	            }
	        };

			//iv.setOnClickListener(listener);
			
		}
	}
	
	public void congratulateUser(){
		Intent i = new Intent(GameActivity.this, CongratulateActivity.class);
		startActivityForResult(i, 500);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
		
	}
	
	
	public void resetAll(){
		setBoard();
		setBlanks();
		currGuess.delete(0, currGuess.length());
		lettersPlaced = 0;
		CLEAR.setClickable(false);
		CLEAR.setTextColor(ctx.getResources().getColor(R.color.inactive_clear_color));
		emptyGuess=true;

		
	}
}

