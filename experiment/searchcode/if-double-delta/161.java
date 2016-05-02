package br.com.andreikeda.x_maps.widget;

import java.util.ArrayList;

import br.com.andreikeda.x_maps.R;
import br.com.andreikeda.x_maps.helper.MenuHelper;
import br.com.andreikeda.x_maps.widget.interfaces.SpecialMenuCallBack;
import br.com.andreikeda.x_maps.widget.model.SpecialMenuModel;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Visibility;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class SpecialMenu extends AbsoluteLayout implements OnTouchListener,
GestureDetector.OnGestureListener {

	private final double arcLength = (3.14 * 2 / 360);
	private Context mcontext;
	public SpecialMenuCallBack mSpecialmenuCallBack;
	// the GestureDector for checking user behavour like onClick,Long Press
	private GestureDetector mTapDetector;

	// main menuButton
	private ImageButton menuButton;
	private final int menuButtonLength = 83;					// Menu Button
	private final int submenuButtonLength = 101;					// SubMenu Button
	private int menuButtonX = 0;								// Menu Button X coordinate
	private int menuButtonY = 0;								// Menu Button Y coordinate
	//
	private ArrayList<TextView> subMenuButtonAL;				
	private ArrayList<SpecialMenuModel> submenuModel;			// submenu model store the information of each submenu
	private int Direction;										// position of the submenu
	private double degreegap = 20;

//	private int actionid = -1;
	private MenuHelper actionid;
	private boolean openFlag = false;
	private int layoutSize;
	private int differentX;
	private int differentY;
	
	private final String logName = "SpecialMenu";
	
	


	public SpecialMenu(Context context ,int layoutSize, int position , ArrayList<SpecialMenuModel> _model ) {
		super(context);
		// TODO Auto-generated constructor stub
		
		
		mcontext = context;
		submenuModel = _model;
		Direction = position;
		Log.d(logName , "Direction : " + Direction);			// which side ?
		
		mTapDetector = new GestureDetector(this);				// use Gesture Detector for touch event
		
		degreegap = 120 / this.submenuModel.size();
		
		Log.d(logName , "new Degree : " + degreegap);
		this.layoutSize = layoutSize;
		
		onBuildComponent();
	}

	public void setSpecialMenuCallBack(SpecialMenuCallBack v){
		this.mSpecialmenuCallBack = v;
	}

	public void onBuildComponent() {
		// Build the main menu Button
		menuButton = new ImageButton(mcontext);
		menuButton.setBackgroundResource(R.drawable.menu_fechado);
		menuButton.setOnTouchListener(this);

		menuButtonX = 0;
		menuButtonY = 0;
		switch(this.Direction) {
		case 0:
			menuButtonX = 0;
			menuButtonY = 0;
			break;
		case 1:
			menuButtonX = layoutSize - menuButtonLength;
			menuButtonY = 0;
			break;
		case 2:
			menuButtonX = layoutSize - menuButtonLength;
			menuButtonY = layoutSize - menuButtonLength;
			break;
		case 3:
			menuButtonX = 0;
			menuButtonY = layoutSize - menuButtonLength;
			break;
		}
		this.addView(menuButton, new AbsoluteLayout.LayoutParams(menuButtonLength, menuButtonLength, menuButtonX, menuButtonY));

		subMenuButtonAL = new ArrayList<TextView>();
		onBuildSubMenuComponent();
	}

	private void getLengthDifferent()
	{
		switch(this.Direction)
		{
		case 0:
			differentX = this.menuButtonLength / 2;
			differentY = this.menuButtonLength / 2;
			break;
		case 1:
			differentX = 0;
			differentY = this.menuButtonLength / 2;
			break;
		case 2:
			differentX = 0;
			differentY = 0;
			break;
		case 3:
			differentX = this.menuButtonLength / 2;
			differentY = 0;
			break;
		}
	}
	private void onBuildSubMenuComponent() {
		for (int i = 0; i < this.submenuModel.size(); i++) {
			TextView ib = new TextView(mcontext);
			ib.setBackgroundResource(this.submenuModel.get(i).Rid);
			ib.setTag(this.submenuModel.get(i).tag);
			ib.setOnTouchListener(this);
			ib.setVisibility(INVISIBLE);
			
			getLengthDifferent();
			int x = (int) getCircleX(menuButtonX + differentX,
					arcLength * (degreegap * i + Direction*90 - 5), 270);
			int y = (int) getCircleY(menuButtonY  + differentY,
					arcLength * (degreegap * i + Direction*90 - 10), 300);
			this.addView(ib, new AbsoluteLayout.LayoutParams(submenuButtonLength, submenuButtonLength, x, y));
			subMenuButtonAL.add(ib);
		}
		menuButton.bringToFront();
	}

	// move StartMenu
	private void startSubMenuMove() {
		openFlag = true;
		int time = 500;
		for (int i = 0; i < this.submenuModel.size(); i++) {
			subMenuButtonAL.get(i).setVisibility(VISIBLE);
			
			getLengthDifferent();
			int x = (int) getCircleX(menuButtonX  + differentX,
					arcLength * (degreegap * i + Direction*90), 270);
			int y = (int) getCircleY(menuButtonY + differentY,
					arcLength * (degreegap * i + Direction*90), 300);
			Log.i("TEST", "x: " + x + " y: " + y);
				subMenuButtonAL.get(i).startAnimation(
						AnimationSetMoveOut(-(x - menuButtonX ) + 5,0,-(y- menuButtonY) + 5,0,time));
				time -= 50;

		}
	}

	private void removeSubMenuMove() {
		openFlag = false;
		int time = 300;
		for (int i = 0; i < this.submenuModel.size(); i++) {
			subMenuButtonAL.get(i).clearAnimation();
			getLengthDifferent();
			int x = (int) getCircleX(menuButtonX  + differentX,
					arcLength * (degreegap * i + Direction*90), 270);
			int y = (int) getCircleY(menuButtonY  + differentY,
					arcLength * (degreegap * i + Direction*90), 300);
			Log.i("TEST", "x: " + x + " y: " + y);
			subMenuButtonAL.get(i).startAnimation(AnimationSetMoveIn(0,-(x - menuButtonX) + 5,0,-(y- menuButtonY) + 5,time));
			subMenuButtonAL.get(i).setVisibility(INVISIBLE);
			time -= 50;
		}
	}

	// Animation Set for Menu
	private AnimationSet AnimationSetA() {
		AnimationSet rootSet = new AnimationSet(true);
		rootSet.setInterpolator(new AccelerateInterpolator());
		rootSet.setRepeatMode(Animation.ABSOLUTE);

		RotateAnimation rotateA = new RotateAnimation(0, 180,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateA.setRepeatCount(0);
		rotateA.setDuration(250);

		rootSet.addAnimation(rotateA);
		return rootSet;
	}
	// Animation Set for Menu
	private AnimationSet AnimationSetA2() {
		AnimationSet rootSet = new AnimationSet(true);
		rootSet.setInterpolator(new AccelerateInterpolator());
		rootSet.setRepeatMode(Animation.ABSOLUTE);

		RotateAnimation rotateA = new RotateAnimation(180, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateA.setRepeatCount(0);
		rotateA.setDuration(250);

		rootSet.addAnimation(rotateA);
		return rootSet;
	}
	// Move out Animation
	private AnimationSet AnimationSetMoveOut(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta , int timer)
	{
		AnimationSet rootSet = new AnimationSet(true);
		rootSet.setInterpolator(new AccelerateInterpolator());
		rootSet.setRepeatMode(Animation.ABSOLUTE);
		TranslateAnimation translateA = new TranslateAnimation(fromXDelta,toXDelta,fromYDelta,toYDelta);
		translateA.setDuration(timer);
		rootSet.addAnimation(translateA);
		return rootSet;
	}
	// Move back Animation
	private AnimationSet AnimationSetMoveIn(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta , int timer)
	{
		AnimationSet rootSet = new AnimationSet(true);
		rootSet.setInterpolator(new AccelerateInterpolator());
		rootSet.setRepeatMode(Animation.ABSOLUTE);
		TranslateAnimation translateA = new TranslateAnimation(fromXDelta,toXDelta,fromYDelta,toYDelta);
		translateA.setDuration(timer);
		rootSet.addAnimation(translateA);
		return rootSet;
	}
	
	// geting the Circle Point X
	private double getCircleX(int centerX, double delta, int radius) {
		Log.i("TEST", "Center X : " + centerX + " delta: " + delta + " radius"
				+ radius);
		return radius * Math.cos(delta) + centerX;
	}

	// geting the Circle Point Y
	private double getCircleY(int centerY, double delta, int radius) {
		Log.i("TEST", "Center X : " + centerY + " delta: " + delta + " radius"
				+ radius);
		return radius * Math.sin(delta) + centerY;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (v.equals(this.menuButton))
//			actionid = 100;
			actionid = MenuHelper.None;
		else {
			for (int i = 0; i < this.submenuModel.size(); i++) {
				if (v.equals(this.subMenuButtonAL.get(i)))
//					actionid = i;
					actionid = (MenuHelper) this.subMenuButtonAL.get(i).getTag();
			}
		}
		return mTapDetector.onTouchEvent(event);
		// return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.i("TEST", "onDown");  

		switch(actionid)
		{
//		case 100:
		case None:
			Log.i("TEST", "menuButton pressed");  

			if(!openFlag){
				menuButton.startAnimation(AnimationSetA());
				startSubMenuMove();
			}else{
				menuButton.startAnimation(AnimationSetA2());
				this.removeSubMenuMove();
			}
			break;
		default:
			menuButton.startAnimation(AnimationSetA2());
			this.removeSubMenuMove();
			this.mSpecialmenuCallBack.subMenuEvent(actionid);
			Log.i("TEST", "sunMenuButton pressed : action id is : " + actionid);
		}

		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		Log.i("TEST", "onFling");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.i("TEST", "onLongPress");

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		Log.i("TEST", "onScroll");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.i("TEST", "onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.i("TEST", "onSingleTapUp");
//		actionid = -1;
		actionid = null;
		return false;
	}



}

