/**
 * Copyright (C) 2010 Alfredo Morresi
 * 
 * This file is part of RainbowLibs project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.rainbowbreeze.libs.ui;

import static it.rainbowbreeze.libs.common.RainbowContractHelper.checkNotNull;

import it.rainbowbreeze.libs.R;
import it.rainbowbreeze.libs.common.IRainbowLogFacility;
import it.rainbowbreeze.libs.common.RainbowLogFacility;
import it.rainbowbreeze.libs.common.RainbowResultOperation;
import it.rainbowbreeze.libs.common.RainbowServiceLocator;
import it.rainbowbreeze.libs.logic.RainbowCrashReporter;
import it.rainbowbreeze.libs.logic.RainbowLogicManager;
import it.rainbowbreeze.libs.logic.RainbowLogicManagerExecuteBeginTasksThread;
import it.rainbowbreeze.libs.logic.RainbowPrepareLogToSendThread;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Splashscreen activity, simply execute application begin tasks
 * 
 * @author Alfredo "Rainbowbreeze" Morresi
 *
 */
public abstract class RainbowSplashScreenActivity extends Activity {
	
	//---------- Private fields
    protected final static String LOG_HASH = RainbowSplashScreenActivity.class.getSimpleName();
	protected final static int DIALOG_EXECUTING_BEGIN_TASKS = 10;
	protected final static int DIALOG_EXECUTING_SENDING_LOGS = 11;
	protected static final int DIALOG_SEND_CRASH_REPORTS = 12;
    protected static final int TASK_EXECUTE_BEGIN_TASK = 10;
    protected static final int TASK_PREPARE_LOG_TO_SEND = 11;
	
	protected IRainbowLogFacility mBaseLogFacility;
	protected RainbowActivityHelper mBaseActivityHelper;
	protected RainbowLogicManager mBaseLogicManager;
	protected RainbowCrashReporter mBaseCrashReporter;
	protected RainbowLogicManagerExecuteBeginTasksThread mExecuteBeginTaskThread;
	protected RainbowPrepareLogToSendThread mPrepareLogThread;

	protected View layShadow;
	protected TextView mLblErrors;
	protected Button mBtnSendLogs;
	protected TextView mLblThankYou;
	protected Button mBtnStartApplication;
	//display start application button after sending logs only if the application initialized correctly */
	protected boolean mStopForBeginTaskErrors;
	protected long mStartWaitTime;
    private boolean mForceDismiss;
	
	
	//---------- Public properties

	
	
	
	//---------- Events
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBaseLogFacility = checkNotNull(RainbowServiceLocator.get(RainbowLogFacility.class), "BaseLogFacility");
		mBaseLogFacility.logStartOfActivity(LOG_HASH, this.getClass(), savedInstanceState);
        mBaseActivityHelper = checkNotNull(RainbowServiceLocator.get(RainbowActivityHelper.class), "BaseActivityHelper");
        mBaseLogicManager = checkNotNull(RainbowServiceLocator.get(RainbowLogicManager.class), "BaseLogicManager");
    	mBaseCrashReporter = checkNotNull(RainbowServiceLocator.get(RainbowCrashReporter.class), "CrashReporter");
		
    	mBaseLogFacility.v(LOG_HASH, "Starting application " + getApplicationInternalName() + " " + getApplicationInternalVersion());
    	mStopForBeginTaskErrors = false;
    	
    	if (isFullscreenActivity()) {
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.actsplashscreen);
    	} else {
			setContentView(R.layout.actsplashscreen);
	        setTitle(String.format(getString(R.string.splashscreen_lblTitle), getString(R.string.common_appName)));
    	}

		mLblErrors = (TextView) findViewById(R.id.actsplashscreen_lblErrors);
		mLblThankYou = (TextView) findViewById(R.id.actsplashscreen_lblThankYouForLogs);
		mBtnSendLogs = (Button) findViewById(R.id.actsplashscreen_btnSendLogs);
		mBtnStartApplication = (Button) findViewById(R.id.actsplashscreen_btnStartApplication);
		mBtnSendLogs.setOnClickListener(mBtnSendLogsOnClickListener);
		mBtnStartApplication.setOnClickListener(mBtnStartApplicationOnClickListener);
		
        additionalInitialization(savedInstanceState);
        
        //executed when the application first runs
        if (null == savedInstanceState) {
	    	//checks for previous crash reports
        	mBaseLogFacility.v(LOG_HASH, "Checking for previous crash reports");
	    	if (mBaseCrashReporter.isCrashReportPresent(this)) {
	    		showDialog(DIALOG_SEND_CRASH_REPORTS);
	    		//when the dialog close, starts begin tasks
	    	} else {
	    		//no error log to send, so starts begin tasks immediately
		    	startBeginTasks();
	    	}
        }
	}

    /* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
		super.onStart();
		
		Object[] threads = (Object[]) getLastNonConfigurationInstance();
		
		if (null != threads) {
			mExecuteBeginTaskThread = (RainbowLogicManagerExecuteBeginTasksThread) threads[0];
			if (null != mExecuteBeginTaskThread) {
				//register new handler
				mExecuteBeginTaskThread.registerCallerHandler(mActivityHandler);
			}
			mPrepareLogThread = (RainbowPrepareLogToSendThread) threads[1];
			if (null != mPrepareLogThread) {
				//register new handler
				mPrepareLogThread.registerCallerHandler(mActivityHandler);
			}
		}
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onPause() {
		if (null != mExecuteBeginTaskThread) {
			//unregister handler from background thread
			mExecuteBeginTaskThread.unregisterCallerHandler();
		}
		if (null != mPrepareLogThread) {
			//unregister handler from background thread
			mPrepareLogThread.unregisterCallerHandler();
		}
		super.onPause();
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
    	Object[] threads = new Object[] {mExecuteBeginTaskThread, mPrepareLogThread };
    	return threads;
    }    

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog retDialog = null;
    	
    	switch (id) {
    	case DIALOG_EXECUTING_BEGIN_TASKS:
    		retDialog = mBaseActivityHelper.createProgressDialog(this, 0, R.string.splashscreen_msgExecutingBeginTasks);
    		break;

		case DIALOG_EXECUTING_SENDING_LOGS:
			retDialog = mBaseActivityHelper.createAndShowProgressDialog(this, 0, R.string.common_msgGatheringLogs);
			break;
    		
    	case DIALOG_SEND_CRASH_REPORTS:
    		retDialog = mBaseActivityHelper.createYesNoDialog(
    				this,
    				R.string.common_msgAskForCrashReportEmailTitle,
    				R.string.common_msgAskForCrashReportEmail,
    				//yes button
    				new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int which) {
    						startSendingLogs();
    						//after the log was sent, continue with app initialization
    					}
    				},
    				//no button
					new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int which) {
    						//delete all previous crash error files
    						mBaseCrashReporter.deleteCrashFiles(RainbowSplashScreenActivity.this);
    						//continue with app initialization
    						startBeginTasks();
//    						dialog.cancel();							
    					}
    				}
			);
    		break;
    		
		default:
			retDialog = super.onCreateDialog(id);
    	}
    	
    	return retDialog;
    }
   
	/* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mBaseLogFacility.e(LOG_HASH, "Returning to activity " + this.getClass().getSimpleName() + ". requestCode: " + requestCode + ", resultCode: " + resultCode);
		super.onActivityResult(requestCode, resultCode, data);
    }

	/* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
    	if (isFinishing()) {
    		//can do something here
    	}
    	super.onDestroy();
    }	    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //when the user touch the activity, it is dismissed
            mForceDismiss = true;
        }
        return super.onTouchEvent(event);
    }
    
    
    
	/**
	 * Handler called by threads
	 */
	private Handler mActivityHandler = new Handler() {
        public void handleMessage(Message msg)
        {
            //check if the message is for this handler
			if (msg.what != TASK_EXECUTE_BEGIN_TASK && 
					msg.what != TASK_PREPARE_LOG_TO_SEND)
				return;
			
			switch (msg.what) {
			case TASK_EXECUTE_BEGIN_TASK:
				//dismisses progress dialog
				dismissDialog(DIALOG_EXECUTING_BEGIN_TASKS);
				if (null == mExecuteBeginTaskThread) {
				    waitForMinimumTimeAndComplete(null);
				} else {
    				RainbowResultOperation<Void> resBeginTask = mExecuteBeginTaskThread.getResult();
    				//free the thread
    				mExecuteBeginTaskThread = null;
    				if (resBeginTask.hasErrors()) {
    					showErrorViews();
    					beginTaskFailed(resBeginTask);
    				} else {
    				    waitForMinimumTimeAndComplete(resBeginTask);
    				}
				}
				break;
		
			case TASK_PREPARE_LOG_TO_SEND:
				//dismisses progress dialog
				dismissDialog(DIALOG_EXECUTING_SENDING_LOGS);
				RainbowResultOperation<String> resPrepareLog = mPrepareLogThread.getResult();
				//free the thread
				mPrepareLogThread = null;
				sendinLogsCompleted(resPrepareLog);
				break;
			}
		}

	};

	private OnClickListener mBtnSendLogsOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			startSendingLogs();
		}
	};

	private OnClickListener mBtnStartApplicationOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			startBeginTasks();
		}
	};

	

	
	//---------- Public methods
	
	
	

	//---------- Private methods
	
	/**
	 * Starts begin tasks
	 * Called when the activity starts, if there are no error repost to send or after the
	 * the user choosed to send or not the log
	 */
	protected void startBeginTasks() {
	    mStartWaitTime = System.currentTimeMillis();
	    mForceDismiss = false;
	    
	    //hide background shadow
        layShadow = findViewById(R.id.actsplashscreen_layShadowOverlay);
        if (!isBackgroundGrayed()) {
            layShadow.setVisibility(View.GONE);
        }
	    
		showDialog(DIALOG_EXECUTING_BEGIN_TASKS);
        //execute begin tasks and other initialization
		//preparing the background thread for executing service command
		mExecuteBeginTaskThread = new RainbowLogicManagerExecuteBeginTasksThread(
				this.getApplicationContext(),
				mActivityHandler,
				TASK_EXECUTE_BEGIN_TASK,
				mBaseLogicManager);
		mExecuteBeginTaskThread.start();
		
		//at the end, completeBeginTasks() is executed
	}

	
	/**
	 * Waits the minimum time that the splashscreen must be shown, then close it and
	 * executes {@link RainbowSplashScreenActivity#beginTasksCompleted(RainbowResultOperation)}
	 * 
	 * @param resBeginTask task result
	 */
    protected void waitForMinimumTimeAndComplete(final RainbowResultOperation<Void> resBeginTask) {
        
        //first of all, block the rotation of the activity
        if (mBaseActivityHelper.isPortrait(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //then, wait for some time
        Thread waitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //after initialization, checks how much time remains
                try {
                    long elapsedTime = System.currentTimeMillis() - mStartWaitTime;
                    //and wait
                    while(!mForceDismiss && (elapsedTime < getMinimumWaiTime())) {
                        Thread.sleep(100);
                        elapsedTime = System.currentTimeMillis() - mStartWaitTime;
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                    beginTasksCompleted(resBeginTask);
                }            
            }
        });
        waitThread.start();
    }

    /**
	 * Execute when the user press the button for sending logs
	 */
	protected void startSendingLogs() {
		//create new progress dialog
		showDialog(DIALOG_EXECUTING_SENDING_LOGS);

		//preparing the background thread for executing service command
		mPrepareLogThread = new RainbowPrepareLogToSendThread(
				this.getApplicationContext(),
				mActivityHandler,
				TASK_PREPARE_LOG_TO_SEND,
				mBaseLogFacility,
				mBaseCrashReporter,
				getLogTag());
		mPrepareLogThread.start();
	}

	protected void showThankYouViews() {
		mLblErrors.setVisibility(View.GONE);
		mBtnSendLogs.setVisibility(View.GONE);
		mLblThankYou.setVisibility(View.VISIBLE);
		if (mStopForBeginTaskErrors)
			mBtnStartApplication.setVisibility(View.GONE);
		else
			mBtnStartApplication.setVisibility(View.VISIBLE);
	}

	protected void showErrorViews() {
		mLblErrors.setVisibility(View.VISIBLE);
		mBtnSendLogs.setVisibility(View.VISIBLE);
		mLblThankYou.setVisibility(View.GONE);
		mBtnStartApplication.setVisibility(View.GONE);
		mStopForBeginTaskErrors = true;
	};
	
	/**
	 * Executed when the sending logs task ended</p>
	 * </p>
	 * At the end, executed the start logic tasks task
	 * 
	 * @param resPrepareLog
	 */
	protected void sendinLogsCompleted(
			RainbowResultOperation<String> resPrepareLog) {
		if (resPrepareLog.hasErrors()) {
			//some errors
			mBaseActivityHelper.reportError(RainbowSplashScreenActivity.this, resPrepareLog);
			startBeginTasks();
		} else {
			//send email with log
			mBaseActivityHelper.sendEmail(
					RainbowSplashScreenActivity.this,
					getEmailForLog(),
					String.format(getString(R.string.common_sendlogSubject), getApplicationInternalName() + " " + getApplicationInternalVersion()),
					String.format(getString(R.string.common_sendlogBody), resPrepareLog.getResult()));
			//if i run this dialog and immediately after open
			//main app activity, the send mail dialog is covered
			//by main app activity :(
			//the workaround is to ask to the user to start
			//main app activity
			showThankYouViews();
		}
	}
	
	
    /**
     * Performs additional initialization. Put here additional logic
     * before starting {@link RainbowLogicManager} begin tasks
     */
    protected void additionalInitialization(Bundle savedInstanceState) {
    }

	/**
	 * Show the activity in fullscreen
	 * @return
	 */
	protected boolean isFullscreenActivity() {
	    return false;
    }

	/**
	 * Put a shadow over the background
	 * 
	 * @return
	 */
    protected boolean isBackgroundGrayed() {
        return true;
    }
    
    /**
     * The minimum wait time of the splashscreen, expressed in milliseconds
     * @return
     */
    protected long getMinimumWaiTime() {
        return 2000;
    }

	/**
	 * Execute when the call to {@link RainbowLogicManagerExecuteBeginTasksThread} returns with success
	 */
	protected abstract void beginTasksCompleted(RainbowResultOperation<Void> result);
	
    /**
	 * Execute when the call to {@link RainbowLogicManagerExecuteBeginTasksThread} returns with errors
	 */
	protected abstract void beginTaskFailed(RainbowResultOperation<Void> result);
	
	protected abstract String getApplicationInternalName();
	
	protected abstract String getApplicationInternalVersion();
	
	protected abstract String getEmailForLog();
	
	protected abstract String getLogTag();

}

