package com.android.qiushi;


import java.util.ArrayList;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.qiushi.Control.*;
import com.android.qiushi.set.SetActivity;


    
public class MainActivity extends Activity implements 
			AnimationListener,OnClickListener,OnDrawerOpenListener,OnDrawerCloseListener{
	static final String TAG="OctoHome";
    /** Called when the activity is first created. */
	private static final int MENU_SCENE_ID = 1;
	private static final int MENU_ELECTRICAL_ID=2;
	ActionMode mActionMode;
	private ViewPager mPager;
	TabAdapter mTabAdapter;
	View LoginView;
	AlertDialog dlg;
	MenuItem menuCurrent;
	
	ActionBar bar;
	private ProgressDialog mProDialog;
	public static SharedPreferences.Editor mEditor=null;
	public static SharedPreferences mSharedPreferences=null;
	
	private  Handler handler;
    private Runnable startHidingRunnable;
    private Animation hideAnimation;
    private Animation showAnimation;
	private LinearLayout mControlLayout;
    private ImageButton mControlShowHideBtn;
    private boolean isShow=false;
    private PopupWindow popupWindow;
    
    private ImageButton mBtnRoom;
    private ImageButton mBtnOperate;
    private ImageButton mBtnSpeak;
    private ImageButton mBtnSet;
    
    private static SlidingDrawer mSlidingDrawer;
    
    private RelativeLayout mRelativlayout;
    
    public static void CloseSlidingDrawer(){
    	if(mSlidingDrawer!=null && mSlidingDrawer.isOpened()){
    		mSlidingDrawer.close();
    	}
    }
    
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int i;
			switch(msg.what)
			{
			case ControlThread.CMD_HTTP_LOGIN:
			case ControlThread.CMD_HTTP_GETSCENEEA:
				if(msg.arg1 == 0)
				{
					Global.initEaList();
					menuCurrent.setTitle("当前房间："+Global.room.roomNm);
					if(Global.room.roomBitmap!=null){
						System.out.println("no null background");
						mRelativlayout.setBackgroundDrawable(new BitmapDrawable(Global.room.roomBitmap));
					}
					else{
						System.out.println("null background");
						mRelativlayout.setBackgroundResource(R.drawable.qsa_bg);
					}
						
						bar.removeAllTabs();
					mTabAdapter.mTabs.clear();
					mTabAdapter.addTab(bar.newTab().setCustomView(getTextView("场景")),
			                SceneControl.class, null);
					
					
					if(Global.idLight.length>0)
//						mTabAdapter.addTab(bar.newTab().setText("灯光"),
//				                LightControl.class, null);
						mTabAdapter.addTab(bar.newTab().setCustomView(getTextView("灯光")),
				                LightControl.class, null);

					if(Global.idDianShi.length>0)
						mTabAdapter.addTab(bar.newTab().setCustomView(getTextView("电视")),
				                DianShiControl.class, null);
			        
					if(Global.idKongTiao.length>0)
						mTabAdapter.addTab(bar.newTab().setCustomView(getTextView("空调")),
				                KongTiaoControl.class, null);
					//bar.setSelectedNavigationItem(bar.getTabCount()-1);
					if(Global.idYinXiang.length>0)
						mTabAdapter.addTab(bar.newTab().setCustomView(getTextView("音响")),
				                YinXiangControl.class, null);
					
					if(Global.idFengShan.length>0)
						mTabAdapter.addTab(bar.newTab().setCustomView(getTextView("风扇")),
				                FengShanControl.class, null);

			        
			        ///for(i=0; i< Global.room.eaList.length; i++)
//					{
//						Ea ea = Global.room.eaList[i];
//						
//						addUITab(ea.eaNm, ea.tpId, ea.idKey);
//					}
			        
					bar.setSelectedNavigationItem(0);
					ControlThread.getRooms();
			        mProDialog.dismiss();
				}else{
					mProDialog.dismiss();
					//login("登陆失败,请重试!");
					//ShowNoNetDialog();
				}
				break;
			case ControlThread.CMD_HTTP_GETROOMS:
				
				break;
			case ControlThread.CMD_HTTP_CONTROL:
				if(msg.arg1 == 0)
				{
					///the command executed success.
					Log.d(TAG,"Control executed success.");
				}else{
					///show toast that the command is not done.
					Log.d(TAG,"Control executed failed.");
				}
				break;
			default:
				break;
			}
		}
	};
	
	private TextView getTextView(String str){
		TextView tx = new TextView(MainActivity.this);
		tx.setText(str);
		tx.setTextSize(30);
		return tx;
	}
	
	
	public void addUITab(String eanm, int tpid, int key)
	{
		Bundle arg = new Bundle();
		
		arg.putInt("key", key);
		
		switch(tpid)
		{
		case ControlThread.TPID_LIGHT:
	        mTabAdapter.addTab(bar.newTab().setText(eanm),
	                LightControl.class, arg);			
			break;
		case ControlThread.TPID_TV:
	        mTabAdapter.addTab(bar.newTab().setText(eanm),
	                DianShiControl.class, arg);
	        break;
		case ControlThread.TPID_AIR:
	        mTabAdapter.addTab(bar.newTab().setText(eanm),
	                KongTiaoControl.class, arg);
        	break;
		case ControlThread.TPID_AV:
	        mTabAdapter.addTab(bar.newTab().setText(eanm),
	                YinXiangControl.class, arg);
        break;
		case ControlThread.TPID_FAN:
	        mTabAdapter.addTab(bar.newTab().setText(eanm),
	                FengShanControl.class, arg);
		}
	}
	
	
	
	
    /* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(Global.room.roomBitmap!=null){
			mRelativlayout.setBackgroundDrawable(new BitmapDrawable(Global.room.roomBitmap));
			
		}
		super.onResume();
	}
	
	

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.getInstance();
        ControlThread.setUIHandler(mHandler);
        setContentView(R.layout.main);
        
        //ActionBar actionBar = getActionBar();
       // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.setDisplayOptions(1, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        //actionBar.setHomeButtonEnabled(true);
        //actionBar.setLogo(R.drawable.icon_0);
        mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOffscreenPageLimit(0);
		
		    
		
		
		mControlLayout = (LinearLayout)findViewById(R.id.control_layout);
		mControlLayout.setVisibility(View.GONE);
		mControlShowHideBtn = (ImageButton)findViewById(R.id.btn_switch);
		//mControlShowHideBtn.setOnClickListener(this);
		
		mBtnRoom = (ImageButton)findViewById(R.id.btn_room);
		mBtnOperate = (ImageButton)findViewById(R.id.btn_operate);
		mBtnSpeak = (ImageButton)findViewById(R.id.btn_speak);
		mBtnSet = (ImageButton)findViewById(R.id.btn_set);
		mBtnOperate.setOnClickListener(this);
		mBtnRoom.setOnClickListener(this);
		mBtnSpeak.setOnClickListener(this);
		mBtnSet.setOnClickListener(this);
		
		mRelativlayout = (RelativeLayout)findViewById(R.id.layoutBackground);
		
		
		mSlidingDrawer = (SlidingDrawer)findViewById(R.id.SlidingDraw);
		mSlidingDrawer.setOnDrawerOpenListener(this);
		mSlidingDrawer.setOnDrawerCloseListener(this);
		mSlidingDrawer.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
			
				if(false==mSlidingDrawer.isFocusable()){
					mSlidingDrawer.close();
				}
			}
		});
		
		mPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(mSlidingDrawer.isOpened()){
					mSlidingDrawer.close();
				}
				return false;
			}
		});
		
        bar = getActionBar();
        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setLogo(R.drawable.logo);
        //bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
        mTabAdapter = new TabAdapter(this, mPager);
//        mTabAdapter.addTab(bar.newTab().setText("场景"),
//                SceneControl.class, null);
        ///mTabAdapter.addTab(bar.newTab().setText("空调"),
        ///        KongTiaoControl.class, null);
	
        ///mTabAdapter.addTab(bar.newTab().setText("音响"),
        ///        YinXiangControl.class, null);
        ///mTabAdapter.addTab(bar.newTab().setText("风扇"),
        ///        FengShanControl.class, null);
	    
        hideAnimation = AnimationUtils.loadAnimation(this, R.anim.control_btn_out);
        showAnimation = AnimationUtils.loadAnimation(this, R.anim.control_btn_in);
       
        hideAnimation.setAnimationListener(this);
        showAnimation.setAnimationListener(this);
        handler = new Handler();
        startHidingRunnable = new Runnable() {
            public void run() {
              startHiding();
            }
          };
          if(Global.isNetworkAvailable(this) || Global.isWiFiActive(this)){
        	  mProDialog=new ProgressDialog(MainActivity.this);
      		mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      	    mProDialog.setTitle("连接中");
      	    mProDialog.setMessage("正在连接，请稍等...");		
              mProDialog.setCanceledOnTouchOutside(false);
      	    mProDialog.setOnKeyListener(new OnKeyListener() {
      			
      			@Override
      			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
      				// TODO Auto-generated method stub
      				if(keyCode == KeyEvent.KEYCODE_BACK){
      					//finish();
      					return true;
      				}
      				return false;
      			}
      		});
              mSharedPreferences=getSharedPreferences("octohome", 0);
              mEditor=mSharedPreferences.edit();
              
              if(Global.familyId == -1)
              {
              	if(mSharedPreferences.getString("username", "").equals("") 
              			|| mSharedPreferences.getString("userpassword", "").equals("") 
              			|| mSharedPreferences.getString("gw", "").equals(""))
              	{
              		ControlThread.target = mSharedPreferences.getString("gw", "");
              		Global.userId = mSharedPreferences.getString("username", "");
              		Global.userPwd = mSharedPreferences.getString("userpassword", "");
       
              		login("请登陆");
              	}else{
              		ControlThread.target = mSharedPreferences.getString("gw", "");
              		Global.userId = mSharedPreferences.getString("username", "");
              		Global.userPwd = mSharedPreferences.getString("userpassword", "");
              		
      				ControlThread.login(Global.userId, Global.userPwd);
      				
      				///mProDialog=new ProgressDialog(MainActivity.this);
      				///mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      			    ///mProDialog.setTitle("连接中");
      			    ///mProDialog.setMessage("正在连接，请稍等...");
      				mProDialog.show();
              	}
              }else{
      			///mProDialog=new ProgressDialog(MainActivity.this);
      			///mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      		    ///mProDialog.setTitle("连接中");
      		    ///mProDialog.setMessage("正在连接，请稍等...");
      			mProDialog.show();
              	ControlThread.getSceneEa(Global.room.roomId);
              }
             
          }
          else{
        	  
        	  ShowNoNetDialog();
          }
        
    }
    
    public void ShowNoNetDialog(){
    	new AlertDialog.Builder(MainActivity.this).setCancelable(false).setTitle("温馨提示").setMessage("没有可用的网络!是否进行设置").
			setPositiveButton("是", new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
//					Intent intent=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//					ComponentName cName = new ComponentName("com.android.settings","com.android.settings.setting");
//					intent.setComponent(cName);
//					startActivity(intent);
					Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
					startActivity(intent);
					finish();
				}
			}).setNegativeButton("否", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			}).create().show();
    }
    
    protected void initPopuptWindow(int width,boolean IsRoom) {
		// TODO Auto-generated method stub

		// 获取自定义布局文件pop.xml的视图
		View popupWindow_view = getLayoutInflater().inflate(R.layout.show_room, null,
				false);
		// 创建PopupWindow实例,200,150分别是宽度和高度
		
		popupWindow = new PopupWindow(popupWindow_view, width, 168, true);
		if(IsRoom){
			new popShowRoom(popupWindow_view, this,popupWindow,mProDialog);
			
		}else{
			new popShowOperate(popupWindow_view, this,popupWindow,bar);
		}
		System.out.println("is rooooo"+IsRoom);
		//mProDialog
		// 设置动画效果
		//popupWindow.showAtLocation(findViewById(R.id.popBtn), Gravity.CENTER, 100, 100);
		popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
		popupWindow.setAnimationStyle(R.style.AnimationFade);
		//点击其他地方消失		
		popupWindow_view.setOnTouchListener(new OnTouchListener() {			
			
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				if (popupWindow != null && popupWindow.isShowing()) {
					popupWindow.dismiss();
					popupWindow = null;
					}	
				return false;
			}
		});		
    }
    
    private void getPopupWindow(int width,boolean IsRoom) {

//		if (null != popupWindow) {
//			popupWindow.dismiss();
//			return;
//		} else {
//			System.out.println("123456");
			initPopuptWindow(width,IsRoom);
		//}
	}
    
    private void startHideAnimation(View view) {
          view.startAnimation(hideAnimation);
        
      }
    private void startHiding() {
        startHideAnimation(mControlLayout);
        
      }
    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mControlLayout.setAnimation(null);
      }
    private void maybeStartAnimation() {
        cancelHiding();
          handler.postDelayed(startHidingRunnable, 2500);
      }
    
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		System.out.println(mControlLayout.getVisibility()+" 234=="+mControlLayout.getAlpha());
		if(isShow){
			mControlLayout.setVisibility(View.VISIBLE);
			//maybeStartAnimation();
			isShow=false;
		}
		else{
			mControlShowHideBtn.setVisibility(View.VISIBLE);
			mControlLayout.setVisibility(View.GONE);
		}
	}
	
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}  
	
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		   if(mControlLayout.getVisibility()==View.GONE){
			   mControlLayout.setVisibility(View.VISIBLE);
				mControlLayout.setAlpha(1.0f);
		   }
	}    
    
    public void login(String reason)
    {
   		LayoutInflater inflater = LayoutInflater.from(this); 
		LoginView = inflater.inflate(R.layout.login_layout, null); 
		AlertDialog.Builder ad =new AlertDialog.Builder(this);
		
		ad.setView(LoginView);
		dlg= ad.create();
		dlg.setTitle(reason);
		
		EditText eUsername = (EditText)LoginView.findViewById(R.id.editUsername);
		EditText ePassword = (EditText)LoginView.findViewById(R.id.editPassword);
		EditText eGW = (EditText)LoginView.findViewById(R.id.editGW);
		
		if(!Global.userId.equals(""))
		{
			eUsername.setText(Global.userId);
			ePassword.setText(Global.userPwd);
			eGW.setText(ControlThread.target);
		}
		
		dlg.setButton("确定", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				EditText username= (EditText)LoginView.findViewById(R.id.editUsername);
				EditText password =(EditText)LoginView.findViewById(R.id.editPassword);
				EditText editGW = (EditText)LoginView.findViewById(R.id.editGW);
				Global.userPwd = password.getText().toString();
				Global.userId = username.getText().toString();
				ControlThread.target = editGW.getText().toString();
				ControlThread.login(Global.userId, Global.userPwd);
				
				mEditor.putString("username", Global.userId);
				mEditor.putString("userpassword", Global.userPwd);
				mEditor.putString("gw", ControlThread.target);
				mEditor.commit();
				
				///mProDialog=new ProgressDialog(MainActivity.this);
				///mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			    ///mProDialog.setTitle("连接中");
			    ///mProDialog.setMessage("正在连接，请稍等...");
				mProDialog.show();
				
				dlg.dismiss();

			}
		});
	
		dlg.show();
    }
    
    
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
	
//		if(requestCode==0 && resultCode==1){
//			
//			///System.out.println("id=="+data.getStringExtra("roomid"));
//			
//			Global.room.roomId = Integer.valueOf(data.getStringExtra("roomid"));
//			ControlThread.getSceneEa(Global.room.roomId);
//			for(int i=0;i<Global.rooms.length;i++){
//				if(Global.room.roomId==Global.rooms[i].roomId){
//					Global.room.roomNm = Global.rooms[i].roomNm;
//					Global.room.roomImg = Global.rooms[i].roomImg;
//					break;
//				}
//			}
//			this.mProDialog.show();
//		}
		
		if(requestCode==0 && resultCode==1){
			
			///System.out.println("id=="+data.getStringExtra("roomid"));
			
			int roomId = Integer.valueOf(data.getStringExtra("roomid"));

			for(int i=0;i<Global.rooms.length;i++){
				if(roomId==Global.rooms[i].roomId){
					Global.room = Global.rooms[i];
					break;
				}
			}

		}
		//super.onActivityResult(requestCode, resultCode, data);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		//super.onSaveInstanceState(outState);
	}



	public static class TabAdapter extends FragmentPagerAdapter 
    	implements ActionBar.TabListener,ViewPager.OnPageChangeListener{

    	private final Activity mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            
            mTabs.add(info);
            
            mActionBar.addTab(tab);
            
            notifyDataSetChanged();
        }

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			TabInfo info = mTabs.get(position);
            if (info.fragment == null) {
                info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }
            return info.fragment;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mTabs.size();
		}

		
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

		
		public void onTabSelected(Tab tab, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			Object tag = tab.getTag();
            for (int i=0; i<mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
					int id = -1;
					//Intent intent = mContext.getIntent();
					switch(i) {
			            case 0:
//							id = R.id.artisttab;
//							intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/artistalbum");
			                break;
			            case 1:
//							id = R.id.albumtab;
//							intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/album");
			                break;
			            case 2:
//							id = R.id.songtab;
//							intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
			                break;
			            case 3:
//							id = R.id.playlisttab;
//							intent.setDataAndType(Uri.EMPTY, MediaStore.Audio.Playlists.CONTENT_TYPE);
			                break;
			            default:
			                break;
			        }
//			        if (id != R.id.nowplayingtab) {
//			            MusicUtils.setIntPref(mContext, "activetab", id);
//			        }
				
					//mContext.setIntent(intent);
                    mViewPager.setCurrentItem(i);
                }
            }
//            if(!tab.getText().equals(mContext.getString(R.string.app_name))) {
//                ActionMode actionMode = ((MainActivity) mContext).getActionMode();
//                if (actionMode != null) {
//                    actionMode.finish();
//                }
//            }
            if(mSlidingDrawer.isOpened()){
    			mSlidingDrawer.close();
    		}
		}

		
		
		
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub
			
		}

		
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			mActionBar.setSelectedNavigationItem(position);
		}
    }
    public ActionMode getActionMode() {
        return mActionMode;
    }
    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }
	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		
		return super.onContextItemSelected(item);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreatePanelMenu(int, android.view.Menu)
	 */
	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		menuCurrent = menu.add(1, 1, 0, "当前房间:");
		menuCurrent.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menuCurrent.setIcon(R.drawable.imagejpg);
//		menu.add(0, 2, 0, "对讲监控").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//		menu.add(0, 3, 0, "常用设置").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(0, 4, 0, "连接设置").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(0, 5, 0, "内外网切换").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(0,6,0,"注销").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case 1:
			//startActivity(new Intent(this,ShowRoom.class));
			if(Global.rooms!=null){
				//startActivityForResult(new Intent(this,ShowRoom.class), 0);
				getPopupWindow(900,true);
				popupWindow.showAtLocation(mPager, Gravity.CENTER|Gravity.TOP, 0, 55);
				
			}else{
				Toast.makeText(this, "正在初始化，请稍候...", Toast.LENGTH_SHORT).show();
			}
			break;
		case 4:
			login("更改连接设置");
			break;
		case 6:
			break;
		}
		
		if(mSlidingDrawer.isOpened()){
			mSlidingDrawer.close();
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.btn_switch:
//			if(mControlLayout.getVisibility()==View.GONE){
//				mControlLayout.setAnimation(null);
//				mControlLayout.startAnimation(showAnimation);
//				isShow=true;
//				mControlShowHideBtn.setVisibility(View.INVISIBLE);
//				
//			}
//			else{
//				
//				mControlLayout.setAnimation(null);
//				mControlLayout.startAnimation(hideAnimation);
//				isShow=false;
//			}
			break;
		case R.id.btn_room:
			if(Global.rooms!=null){
				//startActivityForResult(new Intent(this,ShowRoom.class), 0);
				getPopupWindow(900,true);
				
				popupWindow.showAtLocation(mPager, Gravity.CENTER|Gravity.TOP, 0, 55);
				
			}else{
				Toast.makeText(this, "正在初始化，请稍候...", Toast.LENGTH_SHORT).show();
			}
			
		//	maybeStartAnimation();
			break;
		case R.id.btn_operate:
			getPopupWindow(600, false);
			popupWindow.showAtLocation(mPager, Gravity.CENTER|Gravity.TOP, 0, 55);
			//maybeStartAnimation();
			break;
		case R.id.btn_speak:
			startActivity(new Intent(this,SpeakMovie.class));
			
			break;
		case R.id.btn_set:
			if(Global.rooms!=null){
				//startActivityForResult(new Intent(this,ShowRoom.class), 0);
				
				 
				startActivity(new Intent(this,SetActivity.class));
				
			}else{
				Toast.makeText(this, "正在初始化，请稍候...", Toast.LENGTH_SHORT).show();
			}
			break;
		}
		mSlidingDrawer.close();
	}


	@Override
	public void onDrawerClosed() {
		// TODO Auto-generated method stub
		mControlShowHideBtn.setImageResource(R.drawable.switchbtn);
	}


	@Override
	public void onDrawerOpened() {
		// TODO Auto-generated method stub
		mControlShowHideBtn.setImageResource(R.drawable.switchclose);
	}
    
    
    
}
