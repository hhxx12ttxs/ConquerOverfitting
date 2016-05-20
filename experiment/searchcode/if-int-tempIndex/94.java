/**
  * Copyright 2011 PoQoP
  * Created on 2011-5-20 ??10:00:03
  */
package com.poqop.vanke.detailgroup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import com.poqop.vanke.R;
import com.poqop.vanke.WebBrowserActivity;
import com.poqop.vanke.R.anim;
import com.poqop.vanke.R.drawable;
import com.poqop.vanke.R.id;
import com.poqop.vanke.R.layout;
import com.poqop.vanke.R.string;
import com.poqop.vanke.see360group.See360Activity;
import com.poqop.vanke.utils.MyApplication;
import com.poqop.vanke.utils.WebTransferTool;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * @author huaxin
 * FlipperActivity.java??????????
 */
public class FlipperActivity extends Activity implements OnGestureListener, OnTouchListener{
	private TextView viewOne, viewTwo, viewThree;				//flipper???view
	private ScrollView scrollViewOne, scrollViewTwo, scrollViewThree;	//??????textview?????
	private ViewFlipper mViewFlipper;	//flipper, ??????
	private View dialogView;			//???????????
	private GestureDetector mGestureDetector;   //?????
	private RelativeLayout root;		//?Activity?????
	private View topView, bottomView;	//???????
	private RelativeLayout.LayoutParams lpTop, lpBottom;	//????????????
	
	private Spanned txtHtml;			//???textview???????????
	private String[] urls;				//???????????
	private int startPage;				//????????
	private int urlFlag = 0;			//????????????
	private boolean isBarsVisible;		//?????????
    private static final int FLING_MIN_DISTANCE = 50; 	//???????????????
    private static final int FLING_MIN_VELOCITY = 0; 	//????
//    private String[] srcHtmls;			//?????????????,
    private MyApplication myApp;		//???????
    private ProgressDialog processDialog;		//???
    private TextView tvTitle;					//??????
    private String strTitle;					//????????
//    private Timer timer = new Timer();	//????????????????
//    private Handler handler = new Handler(){   			//??????????????????
//        public void handleMessage(Message msg) {   
//            switch (msg.what) {       
//            case 1:       
//            	if(((MenuGroupActivity)FlipperActivity.this.getParent()).isTopBottomBarShow()){
//    				((MenuGroupActivity)FlipperActivity.this.getParent()).hideTopBottomBar();
//    			} 
//                break;       
//            }       
//            super.handleMessage(msg);   
//        }   
//           
//    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        myApp = (MyApplication)getApplication();
        processDialog = new ProgressDialog(this);
        processDialog.setMessage("??????? . . .");
        
        initMainView();
        setContentView(root);
        
        //????????????????????
        LoadDataAsyn asynTask = new LoadDataAsyn(0);
        this.showLoadingDialog();
        asynTask.execute("");
    }
    
    /**
     * ?????
     */
    private void initMainView(){
    	//???????
        root = new RelativeLayout(this);
        RelativeLayout.LayoutParams rootParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        rootParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rootParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        root.setLayoutParams(rootParams);
        
        urls = this.getIntent().getStringArrayExtra("urls");
        startPage = this.getIntent().getIntExtra("startPage", 0);
        
		myApp.setHouseList(new ArrayList<String>());
		

		//??flipper???
		mViewFlipper = new ViewFlipper(this);
		mViewFlipper.setInAnimation(getApplicationContext(), R.anim.push_left_in);
	    mViewFlipper.setOutAnimation(getApplicationContext(), R.anim.push_right_out);
	    mViewFlipper.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);
	    mViewFlipper.setFlipInterval(500);
	        
	    //???????
	    mGestureDetector = new GestureDetector(this);
	    
	    //???view???
		scrollViewOne = new ScrollView(this);
		scrollViewOne.setBackgroundColor(Color.WHITE);
		viewOne = new TextView(this);
		viewOne.setTextColor(Color.GRAY);
		viewOne.setPadding(2, 0, 2, 0);
		viewOne.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
		scrollViewOne.addView(viewOne);
		 //???view???
		scrollViewTwo = new ScrollView(this);
		scrollViewTwo.setBackgroundColor(Color.WHITE);
		viewTwo = new TextView(this);
		viewTwo.setTextColor(Color.GRAY);
		viewTwo.setPadding(2, 0, 2, 0);
		scrollViewTwo.addView(viewTwo);
		 //???view???
		scrollViewThree = new ScrollView(this);
		scrollViewThree.setBackgroundColor(Color.WHITE);
		viewThree = new TextView(this);
		viewThree.setTextColor(Color.GRAY);
		viewThree.setPadding(2, 0, 2, 0);
		scrollViewThree.addView(viewThree);
		
		//???view??flipper
		mViewFlipper.addView(scrollViewOne);
		mViewFlipper.addView(scrollViewTwo);
		mViewFlipper.addView(scrollViewThree);
		
		//????
		viewOne.setOnTouchListener(this);
        viewThree.setOnTouchListener(this);
        viewTwo.setOnTouchListener(this);
        scrollViewOne.setOnTouchListener(this);
        scrollViewThree.setOnTouchListener(this);
        scrollViewTwo.setOnTouchListener(this);
		mViewFlipper.setOnTouchListener(this);
        mViewFlipper.setLongClickable(true);
		
        //????3?view?????
        urlFlag = startPage;
        try{
	        myApp.getHouseList().add(0, WebTransferTool.getHtml(urls[urlFlag]));
			txtHtml = Html.fromHtml(myApp.getHouseList().get(0), WebTransferTool.imgGetter, null);
			viewOne.setText(txtHtml);
			viewTwo.setText("?????. . .");
			viewThree.setText("?????. . .");
        }catch(Exception e){
        	Log.i("Err", e.toString());
        }
		
        //?flipper????????
        RelativeLayout.LayoutParams webviewParams = new RelativeLayout.LayoutParams(
        		ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        webviewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        root.addView(mViewFlipper, webviewParams);
        
        //??????
        topView = LayoutInflater.from(this).inflate(R.layout.detail_topbar, null);
		lpTop = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 46);
		lpTop.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		//??????
		bottomView = LayoutInflater.from(this).inflate(R.layout.detail_bottombar, null);
		lpBottom = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 54);
		lpBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		attachListener();
    }
    
    //????????????????
    private void attachListener(){
    	//????????????
    	LinearLayout menuBtn, phoneBtn, mapBtn, shareBtn, moreBtn;	//??????
    	final ImageView menuImgv, phoneImgv, mapImgv, shareImgv, moreImgv;//??????????
    	
    	menuBtn=(LinearLayout) bottomView.findViewById(R.id.menuBtn);
		phoneBtn=(LinearLayout) bottomView.findViewById(R.id.phoneBtn);
		mapBtn=(LinearLayout) bottomView.findViewById(R.id.mapBtn);
		shareBtn=(LinearLayout) bottomView.findViewById(R.id.shareBtn);
		moreBtn=(LinearLayout) bottomView.findViewById(R.id.moreBtn);
		
		menuImgv = (ImageView)bottomView.findViewById(R.id.imgv_menu);
		phoneImgv = (ImageView)bottomView.findViewById(R.id.imgv_phone);
		mapImgv = (ImageView)bottomView.findViewById(R.id.imgv_map);
		shareImgv = (ImageView)bottomView.findViewById(R.id.imgv_share);
		moreImgv = (ImageView)bottomView.findViewById(R.id.imgv_more);
		
		menuImgv.setImageResource(R.drawable.none);
		phoneImgv.setImageResource(R.drawable.none);
		mapImgv.setImageResource(R.drawable.none);
		shareImgv.setImageResource(R.drawable.none);
		moreImgv.setImageResource(R.drawable.none);
		
		menuBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				menuImgv.setImageResource(R.drawable.menu);
				Intent intent = new Intent(FlipperActivity.this, DetailMainActivity.class);
				intent.putExtra("flag", 1);
				startActivity(intent);
				finish();
			}
		});
        
        phoneBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				phoneImgv.setImageResource(R.drawable.phone);
				Intent intent = new Intent(FlipperActivity.this, DetailMainActivity.class);
				intent.putExtra("flag", 2);
				startActivity(intent);
				finish();
			}
		});
        
        mapBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mapImgv.setImageResource(R.drawable.map);
				Intent intent = new Intent(FlipperActivity.this, DetailMainActivity.class);
				intent.putExtra("flag", 3);
				startActivity(intent);
				finish();
			}
		});
        
        shareBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				shareImgv.setImageResource(R.drawable.share);
				Intent intent = new Intent(FlipperActivity.this, DetailMainActivity.class);
				intent.putExtra("flag", 4);
				startActivity(intent);
				finish();
			}
		});
        
        moreBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				moreImgv.setImageResource(R.drawable.more);
				Intent intent = new Intent(FlipperActivity.this, DetailMainActivity.class);
				intent.putExtra("flag", 5);
				startActivity(intent);
				finish();
			}
		});
        
        //????????????
        ImageButton itemlistBtn, read360Btn;	//??????
        itemlistBtn = (ImageButton)topView.findViewById(R.id.itemlistBtn);
        itemlistBtn.setImageResource(R.drawable.books_back_button);
		read360Btn = (ImageButton)topView.findViewById(R.id.reading360Btn);
		
		itemlistBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(FlipperActivity.this, WankeMainActivity.class);
//				intent.putExtra("flag", 3);
//				intent.putExtra("SecondFlag", true);
//				startActivity(intent);
				finish();
			}
        });
        
        read360Btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String strUrl = "http://vanke.home3d.cn/mobile.php?do=community360&id=25142";
				Intent intent = new Intent(FlipperActivity.this, WebBrowserActivity.class);
				intent.putExtra("url", strUrl);
				startActivity(intent);
			}
        });
        
        //??????
        tvTitle = (TextView)topView.findViewById(R.id.title);
        strTitle = this.getIntent().getStringExtra("bookName");
        strTitle = (strTitle != null) ? strTitle : getString(R.string.detail_books_title);
        tvTitle.setText(strTitle + "(?" + (urlFlag + 1) + "?)");
    }

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)??????????
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX()-e2.getX() > FLING_MIN_DISTANCE    
                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {	//????????
			if(urlFlag == urls.length - 1){
				return true;	//??????view?????
			}
			mViewFlipper.setInAnimation(getApplicationContext(), R.anim.push_right_in);
	        mViewFlipper.setOutAnimation(getApplicationContext(), R.anim.push_left_out);
			mViewFlipper.showNext();
			urlFlag = (urlFlag + 1) % urls.length;
//			reflushViews();
			LoadDataAsyn taskB = new LoadDataAsyn(2);
			showLoadingDialog();
			taskB.execute("");
			tvTitle.setText(strTitle + "(?" + (urlFlag + 1) + "?)");
		}else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE    
                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {	//??????
			if(urlFlag == 0){
				return true;	//?1?view?????0?view??
			}
			mViewFlipper.setInAnimation(getApplicationContext(), R.anim.push_left_in);
            mViewFlipper.setOutAnimation(getApplicationContext(), R.anim.push_right_out);
			mViewFlipper.showPrevious();
			urlFlag = (urlFlag - 1 + urls.length) % urls.length;
//			reflushViews();
			LoadDataAsyn taskB = new LoadDataAsyn(2);
			showLoadingDialog();
			taskB.execute("");
			tvTitle.setText(strTitle + "(?" + (urlFlag + 1) + "?)");
		}else{
        	return false;
        }
		return true;
	}
	
	/**
	 * ???????????????
	 */
	private synchronized void reflushViews(){
		if(mViewFlipper.getCurrentView() == scrollViewOne){
			txtHtml = Html.fromHtml(myApp.getHouseList().get((urlFlag + 1) % urls.length), WebTransferTool.imgGetter, null);
			viewTwo.setText(txtHtml);
			txtHtml = Html.fromHtml(myApp.getHouseList().get((urlFlag - 1 + urls.length) % urls.length), WebTransferTool.imgGetter, null);
			viewThree.setText(txtHtml);
		}else if(mViewFlipper.getCurrentView() == scrollViewTwo){
			txtHtml = Html.fromHtml(myApp.getHouseList().get((urlFlag + 1) % urls.length), WebTransferTool.imgGetter, null);
			viewThree.setText(txtHtml);
			txtHtml = Html.fromHtml(myApp.getHouseList().get((urlFlag - 1 + urls.length) % urls.length), WebTransferTool.imgGetter, null);
			viewOne.setText(txtHtml);
		}else if(mViewFlipper.getCurrentView() == scrollViewThree){
			txtHtml = Html.fromHtml(myApp.getHouseList().get((urlFlag + 1) % urls.length), WebTransferTool.imgGetter, null);
			viewOne.setText(txtHtml);
			txtHtml = Html.fromHtml(myApp.getHouseList().get((urlFlag - 1 + urls.length) % urls.length), WebTransferTool.imgGetter, null);
			viewTwo.setText(txtHtml);
		}
	}
	
	private synchronized void loadFirst(){
		try {//????????????
			int tempIndex = (urlFlag + 1 >= urls.length) ? (urlFlag - 2) : (urlFlag + 1);
			myApp.getHouseList().add(1, WebTransferTool.getHtml(urls[tempIndex]));
			txtHtml = Html.fromHtml(myApp.getHouseList().get(1), WebTransferTool.imgGetter, null);
			viewTwo.setText(txtHtml);
			tempIndex = (urlFlag - 1 < 0) ? (urlFlag + 2 < urls.length ? (urlFlag + 2) : 0) : (urlFlag - 1);
			myApp.getHouseList().add(2, WebTransferTool.getHtml(urls[tempIndex]));
			txtHtml = Html.fromHtml(myApp.getHouseList().get(2), WebTransferTool.imgGetter, null);
			viewThree.setText(txtHtml);
			mViewFlipper.invalidate();
		} catch (Exception e) {
			Log.i("Err", e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if ((distanceX > 0 && urlFlag < urls.length - 1)// ?????????
				|| (distanceX < 0 && urlFlag > 0 && mViewFlipper.getScrollX() > 0)) {// ??????????
//			mViewFlipper.scrollBy((int)distanceX, 0);
//			if(distanceX > 160){
////				mViewFlipper.scrollBy((int)distanceX * (-1), 0);
//				mViewFlipper.showNext();
//				urlFlag = (urlFlag + 1) % urls.length;
////				reflushViews();
//				LoadDataAsyn taskB = new LoadDataAsyn(2);
//				showLoadingDialog();
//				taskB.execute("");
//			}else if(distanceX < -160){
////				mViewFlipper.scrollBy((int)distanceX * (-1), 0);
//				mViewFlipper.showPrevious();
//				urlFlag = (urlFlag - 1 + urls.length) % urls.length;
////				reflushViews();
//				LoadDataAsyn taskB = new LoadDataAsyn(2);
//				showLoadingDialog();
//				taskB.execute("");
//			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc), ?touch down???????onScroll????????onLongPress????Touchup????
	 * ??????????
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		if(isBarsVisible){
			removeTopBottomBar();
		}else{
			showTopBottomBar();
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return mGestureDetector.onTouchEvent(event);
	}
	
	/**
	 * @author huaxin
	 * LoadDataAsyn class, ?????
	 */
	class LoadDataAsyn extends AsyncTask<Object,Object, String>{
		int typeOfTask;
		public LoadDataAsyn(int type){
			this.typeOfTask = type;
		}

		/* (non-Javadoc)???????
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(Object... arg0) {
			switch(this.typeOfTask){
			case 0:			//???????html??????string
				try{
					//load the data of the first time
					loadFirst();
					
					for(int i=0; i<urls.length; i++){
						if(i >= myApp.getHouseList().size()){
							myApp.getHouseList().add(WebTransferTool.getHtml(urls[i]));
						}else{
							myApp.getHouseList().set(i, WebTransferTool.getHtml(urls[i]));
						}
					}
				}catch(Exception e){
					Log.i("Err", e.toString());
				}
				break;
			case 1:			//???????????
				
				break;
			case 2:			//??????
				try{
					reflushViews();
				}catch(Exception ex){
					Log.i("ERR", ex.toString());
				}
				break;
			default:
				break;
			}
			return "";
		}
		
		/**
		 * ????????????????????
		 * param result, doInBackground???????
		 */
		@Override
		protected void onPostExecute(final String result){
			removeLoadingDialog();
		}
	}
	
	/**
	 * ???????????
	 */
	private void showTopBottomBar(){
		root.addView(topView, lpTop);
		tvTitle.setText(strTitle + "(?" + (urlFlag + 1) + "?)");
		Animation topFlowDown = AnimationUtils.loadAnimation(this, R.anim.push_up_in);
//		topView.startAnimation(topFlowDown);
		
		root.addView(bottomView, lpBottom);
		Animation bottomFlowUp = AnimationUtils.loadAnimation(this, R.anim.push_down_in);
		topView.startAnimation(topFlowDown);
		bottomView.startAnimation(bottomFlowUp);
		
		isBarsVisible = true;
	}
	
	/**
	 * ???????????
	 */
	private void removeTopBottomBar(){
		if(!isBarsVisible){
			return;
		}
		Animation topFlowUp = AnimationUtils.loadAnimation(this, R.anim.push_up_out);
		topView.startAnimation(topFlowUp);
		
		Animation bottomFlowDown = AnimationUtils.loadAnimation(this, R.anim.push_down_out);
		bottomView.startAnimation(bottomFlowDown);
		
		root.removeView(topView);
		root.removeView(bottomView);
		
		isBarsVisible = false;
	}
	
	/**
	 * ?????
	 */
	public void showLoadingDialog(){
		processDialog.show();
	}
	
	/**
	 * ?????
	 */
	public void removeLoadingDialog(){
		if(processDialog != null && processDialog.isShowing()){
			processDialog.dismiss();
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	protected void onPause(){
//		timer.cancel();
		super.onPause();
	}
	
	protected void onDestroy(){
//		System.gc();
		myApp.clearHouseList();
		super.onDestroy();
	}
}
