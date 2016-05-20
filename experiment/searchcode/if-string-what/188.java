/**
 * Copyright (C) 2010 Alfredo Morresi
 * 
 * This file is part of SmsForFree project.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * import android.util.Log;

 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */

package it.rainbowbreeze.smsforfree.ui;

import it.rainbowbreeze.libs.common.RainbowResultOperation;
import it.rainbowbreeze.libs.helper.RainbowStringHelper;
import it.rainbowbreeze.smsforfree.R;
import it.rainbowbreeze.smsforfree.common.AppEnv;
import it.rainbowbreeze.smsforfree.common.LogFacility;
import it.rainbowbreeze.smsforfree.common.ResultOperation;
import it.rainbowbreeze.smsforfree.data.AppPreferencesDao;
import it.rainbowbreeze.smsforfree.data.ContactsDao;
import it.rainbowbreeze.smsforfree.data.SmsDao;
import it.rainbowbreeze.smsforfree.domain.ContactPhone;
import it.rainbowbreeze.smsforfree.domain.SmsProvider;
import it.rainbowbreeze.smsforfree.domain.SmsService;
import it.rainbowbreeze.smsforfree.helper.GlobalHelper;
import it.rainbowbreeze.smsforfree.logic.LogicManager;
import it.rainbowbreeze.smsforfree.logic.SendCaptchaThread;
import it.rainbowbreeze.smsforfree.logic.SendMessageThread;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActSendSms
    extends Activity
{
    //---------- Private fields
    protected static final String LOG_HASH = "ActSendSms";
    
    protected static final int DIALOG_PHONES = 10;
    protected static final int DIALOG_CAPTCHA_REQUEST = 11;
    protected static final int DIALOG_SENDING_MESSAGE = 12;
    protected static final int DIALOG_SENDING_CAPTCHA = 13;
    protected static final int DIALOG_STARTUP_INFOBOX = 14;
    protected static final int DIALOG_TEMPLATES = 16;
    protected static final int DIALOG_SENDING_MESSAGE_ERROR = 17;
    protected static final int DIALOG_ASK_CONFIRMATION_FOR_SENDING = 18;
    
    protected static final String BUNDLEKEY_CONTACTPHONES = "ContactPhones";
    protected static final String BUNDLEKEY_CAPTCHASTORAGE = "CaptchaStorage";
    protected static final String BUNDLEKEY_DIALOGERRORMESSAGE = "DialogErrorMessage";

	private final static int WHAT_SENDCAPTCHA = 1003;
	private final static int WHAT_SENDMESSAGE = 1002;

    
    protected Spinner mSpiProviders;
    protected Spinner mSpiSubservices;

    protected SmsProvider mSelectedProvider;
    protected String mSelectedServiceId;
    protected AutoCompleteTextView mTxtDestination;
    protected EditText mTxtBody;
    protected TextView mLblMessageLength;
    protected Button mBtnSend;
    protected ImageButton mBtnPickContact;
    protected ImageButton mBtnGetLastSmsReceivedNumber;
    protected ImageButton mBtnClearDestination;
    protected ImageButton mBtnClearMessage;
    protected TextView mLblProvider;

    protected List<ContactPhone> mPhonesToShowInDialog;
    protected String mCaptchaStorage;
    protected String mErrorSendingMessage;

    protected SendMessageThread mSendMessageThread;
    protected SendCaptchaThread mSendCaptchaThread;
    
    protected AppEnv mAppEnv;
    protected LogFacility mLogFacility;
    protected ActivityHelper mActivityHelper;
    protected AppPreferencesDao mAppPreferencesDao;
    protected LogicManager mLogicManager;
    protected SmsDao mSmsDao;


    
    
    //---------- Public properties




    //---------- Events
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAppEnv = AppEnv.i(getBaseContext());
        mLogFacility = mAppEnv.getLogFacility();
        mLogFacility.logStartOfActivity(LOG_HASH, this.getClass(), savedInstanceState);
        mActivityHelper = mAppEnv.getActivityHelper();
        mAppPreferencesDao = mAppEnv.getAppPreferencesDao();
        mLogicManager = mAppEnv.getLogicManager();
        mSmsDao = mAppEnv.getSmsDao();

        setContentView(R.layout.actsendsms);
        setTitle(String.format(
                getString(R.string.actsendsms_title), mAppEnv.getAppDisplayName()));

        mSpiProviders = (Spinner) findViewById(R.id.actsendsms_spiProviders);
        mSpiSubservices = (Spinner) findViewById(R.id.actsendsms_spiServices);
        mTxtDestination = (AutoCompleteTextView) findViewById(R.id.actsendsms_txtDestination);
        mTxtBody = (EditText) findViewById(R.id.actsendsms_txtMessage);
        mLblMessageLength = (TextView) findViewById(R.id.actsendsms_lblMessageLength);
        mLblProvider = (TextView) findViewById(R.id.actsendsms_lblProvider);
        mBtnSend = (Button) findViewById(R.id.actsendsms_btnSend);
        mBtnPickContact = (ImageButton) findViewById(R.id.actsendsms_btnPickContact);
        mBtnClearDestination = (ImageButton) findViewById(R.id.actsendsms_btnClearDestination);
        mBtnClearMessage = (ImageButton) findViewById(R.id.actsendsms_btnClearMessage);
        mBtnGetLastSmsReceivedNumber = (ImageButton) findViewById(R.id.actsendsms_btnGetLastSmsReceivedNumber);

        /**
        //eventually remove ad view
        if (!mAppEnv.isAdEnables()) {
            AdView adView = (AdView) findViewById(R.id.actsendsms_adview);
            LinearLayout parent = (LinearLayout) adView.getParent();
            parent.removeView(adView);
        }
        **/

        //set listeners
        mSpiProviders.setOnItemSelectedListener(mSpiProvidersSelectedListener);
        mSpiSubservices.setOnItemSelectedListener(mSpiSubservicesSelectedListener);
        mBtnSend.setOnClickListener(mBtnSendClickListener);
        mBtnPickContact.setOnClickListener(mBtnPickContactListener);
        mBtnGetLastSmsReceivedNumber.setOnClickListener(mBtnGetLastSmsReceivedNumberListener);
        mBtnClearDestination.setOnClickListener(mBtnClearDestinationListener);
        mBtnClearMessage.setOnClickListener(mBtnClearMessageListener);
        mTxtBody.addTextChangedListener(mTxtBodyTextChangedListener);

        //set autocomplete
        mTxtDestination.setAdapter(new ContactsPhonesAdapter(this, mAppPreferencesDao));
        
        //populate Spinner with values
        bindProvidersSpinner();
        
        //hide views
        if (!mSmsDao.isInboxSmsProviderAvailable(this)) {
            mBtnGetLastSmsReceivedNumber.setVisibility(View.INVISIBLE);
        }

        //executed when the application first runs
        if (null == savedInstanceState) {
            //load values of view from previous application execution
            restoreLastRunViewValues();
            //check if the application was called as intent action
            processIntentData(getIntent());
            //show info dialog, if needed
            if (mLogicManager.isFirstStartOfAppNewVersion())
                showDialog(DIALOG_STARTUP_INFOBOX);
            mSpiProviders.requestFocus();
        }
        
//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);        
    }
    

    @Override
    protected void onStart() {
        super.onStart();
        
        Object savedThread = getLastNonConfigurationInstance();
        //nothing saved
        if (null == savedThread) return;
    
        //saved object is a SendMessageThread
        if (savedThread instanceof SendMessageThread) {
            mSendMessageThread = (SendMessageThread) savedThread;
            //register new handler
            mSendMessageThread.registerCallerHandler(mActivityHandler);
        
        //saved object is a SendCaptchaThread
        } else {
            mSendCaptchaThread = (SendCaptchaThread) savedThread;
            //register new handler
            mSendCaptchaThread.registerCallerHandler(mActivityHandler);
        }
    }
    
    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            RainbowResultOperation<Void> res = mLogicManager.executeEndTasks(this);
            if (res.hasErrors()) {
                mActivityHelper.reportError(this, res);
            }
            
            //log the end of the application
            mLogFacility.i(LOG_HASH, "App end");
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (null != mSendMessageThread) {
            //unregister handler from background thread
            mSendMessageThread.registerCallerHandler(null);
        } else if (null != mSendCaptchaThread) {
            //unregister handler from background thread
            mSendCaptchaThread.registerCallerHandler(null);
        }
        super.onStop();
    }
        
    @Override
    public Object onRetainNonConfigurationInstance() {
        //save eventually open background thread
        if (null != mSendMessageThread) {
            return mSendMessageThread;
        } else if (null != mSendCaptchaThread) {
            return mSendCaptchaThread;
        }
        return null;
    }
    
    
    @Override
    protected void onPause() {
        super.onPause();
        
        //save current fields value to prefs
        //provider and subservice was already saved in change event of the spinner
        mAppPreferencesDao.setLastUsedDestination(
                TextUtils.isEmpty(mTxtDestination.getText()) ? "" : mTxtDestination.getText().toString());
        mAppPreferencesDao.setLastUsedMessage(
                TextUtils.isEmpty(mTxtBody.getText()) ? "" : mTxtBody.getText().toString());
        
        //and save new selected provider
        if (null != mSelectedProvider) {
            mAppPreferencesDao.setLastUsedProviderId(mSelectedProvider.getId());
        } else {
            mAppPreferencesDao.setLastUsedProviderId("");
        }
                
        mAppPreferencesDao.setLastUsedSubserviceId(mSelectedServiceId);
        mAppPreferencesDao.save();
    }
    
    
    /**
     * Save volatile data (for example, when the activity is
     * rotated
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        String phones = ContactsDao.instance().SerializeContactPhones(mPhonesToShowInDialog);
        outState.putString(BUNDLEKEY_CONTACTPHONES, phones);
        outState.putString(BUNDLEKEY_CAPTCHASTORAGE, mCaptchaStorage);
        outState.putString(BUNDLEKEY_DIALOGERRORMESSAGE, mErrorSendingMessage);
    };

    
    @Override
    /**
     * Load volatile data into inner fields, for example when the
     * activity is rotated
     * 
     * @param savedInstanceState
     */
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //restore volatile values
        mCaptchaStorage = savedInstanceState.getString(BUNDLEKEY_CAPTCHASTORAGE);
        mPhonesToShowInDialog = ContactsDao.instance().deserializeContactPhones(
                savedInstanceState.getString(BUNDLEKEY_CONTACTPHONES));
        mErrorSendingMessage = savedInstanceState.getString(BUNDLEKEY_DIALOGERRORMESSAGE);
        
        //when the activity is rotated, in the onPause event the values of
        //provider, text values and subservice are persisted, so i can rely on this value
        //for reassigning them
        restoreLastRunViewValues();
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!super.onCreateOptionsMenu(menu)) return false;
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actsendsms, menu);
        return true;        
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.actsendsms_mnuSettings) {
            mActivityHelper.openSettingsMain(this);
        } else if (itemId == R.id.actsendsms_mnuAbout) {
            mActivityHelper.openAbout(this);
        } else if (itemId == R.id.actsendsms_mnuSignature) {
            addSignature();
        } else if (itemId == R.id.actsendsms_mnuCompress) {
            String message = mTxtBody.getText().toString();
            mActivityHelper.openCompactMessage(this, message);
        } else if (itemId == R.id.actsendsms_mnuTemplates) {
            showDialog(DIALOG_TEMPLATES);
        }
        return super.onOptionsItemSelected(item);
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        //process cancel action from various activities
        if (RESULT_OK != resultCode) {
            if (ActivityHelper.REQUESTCODE_PICKCONTACT == requestCode ||
                    ActivityHelper.REQUESTCODE_COMPACTMESSAGE == requestCode) {
                return;
            }
        }

        switch (requestCode) {
            case (ActivityHelper.REQUESTCODE_PICKCONTACT):  
                Uri contactData = data.getData();
                assignContactPhone(contactData);
            break;  
            
            case (ActivityHelper.REQUESTCODE_COMPACTMESSAGE):  
                String message = data.getStringExtra(ActivityHelper.INTENTKEY_MESSAGE);
                if (!TextUtils.isEmpty(message))
                    mTxtBody.setText(message);
            break;
            
            case (ActivityHelper.REQUESTCODE_MAINSETTINGS):
                //refresh subservices list if subservices of a provider was edited
                if (mAppEnv.getForceSubserviceRefresh()) {
                    mAppEnv.setForceSubserviceRefresh(false);
                    changeProvider(mSelectedProvider, true);
                }
            break;  
        }  
    }


    /* (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog retDialog = null;
        
        switch (id) {
        case DIALOG_PHONES:
            retDialog = createPhonesDialog();
            break;
            
        case DIALOG_TEMPLATES:
            retDialog = createTemplatesDialog();
            break;
            
        case DIALOG_CAPTCHA_REQUEST:
            retDialog = createCaptchaDialog(mCaptchaStorage);
            break;
            
        case DIALOG_SENDING_MESSAGE:
            retDialog = mActivityHelper.createProgressDialog(this, 0, R.string.actsendsms_msg_sendingMessage);
            break;
            
        case DIALOG_SENDING_CAPTCHA:
            retDialog = mActivityHelper.createProgressDialog(this, 0, R.string.actsendsms_msg_sendingCaptcha);
            break;
            
        case DIALOG_STARTUP_INFOBOX:
            retDialog = mActivityHelper.createInformativeDialog(this,
                    getString(R.string.actsendsms_msg_infobox_title),
                    getString(R.string.actabout_lblDescription) + "\n\n" + this.getString(R.string.actabout_msgChangeslog),
                    getString(R.string.common_btnOk));
            break;
        
        case DIALOG_SENDING_MESSAGE_ERROR:
            retDialog = mActivityHelper.createInformativeDialog(this,
                    getString(R.string.actsendsms_msg_errorSendingMessageTitle),
                    mErrorSendingMessage,
                    getString(R.string.common_btnOk));
            retDialog.setOnDismissListener(new Dialog.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    removeDialog(DIALOG_SENDING_MESSAGE_ERROR);
                }
            });
            break;
        
        default:
            retDialog = super.onCreateDialog(id);
        }
        
        return retDialog;
    }
    
    
    private OnItemSelectedListener mSpiProvidersSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            SmsProvider provider = (SmsProvider) parent.getItemAtPosition(pos);
            changeProvider(provider, false);
        }

        public void onNothingSelected(AdapterView<?> parent) {
          // Do nothing.
        }
    };    
    
    private OnItemSelectedListener mSpiSubservicesSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            SmsService service = (SmsService) parent.getItemAtPosition(pos);
            changeService(service);
        }

        public void onNothingSelected(AdapterView<?> parent) {
          // Do nothing.
        }
    };
    
    private OnClickListener mBtnPickContactListener = new OnClickListener() {
        public void onClick(View v) {
            //launch the pick contact intent
            mActivityHelper.openPickContact(ActSendSms.this);
        }
    };
    
    
    private OnClickListener mBtnGetLastSmsReceivedNumberListener = new OnClickListener() {
        public void onClick(View v) {
            ResultOperation<String> res = mSmsDao.getLastSmsReceivedNumber(ActSendSms.this);
            if (res.hasErrors()) {
                mActivityHelper.reportError(ActSendSms.this, R.string.actsendsms_msg_error_retrieving_inbox_sms);
                return;
            }
            
            if (TextUtils.isEmpty(res.getResult())) {
                mActivityHelper.showInfo(ActSendSms.this, R.string.actsendsms_msg_empty_inbox_sms);
            } else {
                mTxtDestination.setText(res.getResult());
            }
        }
    };
    
    
    private OnClickListener mBtnSendClickListener = new OnClickListener() {
        public void onClick(View v) {
            sendMessage();
        }
    };
    
    
    private OnClickListener mBtnClearDestinationListener = new OnClickListener() {
        public void onClick(View v) {
            mTxtDestination.setText("");
        }
    };
    
    
    private OnClickListener mBtnClearMessageListener = new OnClickListener() {
        public void onClick(View v) {
            mTxtBody.setText("");
        }
    };
    
    
    private TextWatcher mTxtBodyTextChangedListener = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateMessageLength();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void afterTextChanged(Editable s) {
        }
    };


    /**
     * Hander to call when a message is sent or a captcha code is inserted
     */
    private Handler mActivityHandler = new Handler() {
        public void handleMessage(Message msg)
        {
            mLogFacility.i(LOG_HASH, "Returned to ActSendSms from external thread");
            //check if the message is for this handler
            if (msg.what != WHAT_SENDMESSAGE && 
                    msg.what != WHAT_SENDCAPTCHA)
                return;
            
            RainbowResultOperation<String> res;
            switch (msg.what) {
            case WHAT_SENDMESSAGE:
                removeDialog(DIALOG_SENDING_MESSAGE);
                 //may happens that the thread is null (rotation and a call to handler in the same moment?)
                if (null == mSendMessageThread) break;
                //pass data to method
                res = mSendMessageThread.getResult();
                mSendMessageThread = null;
                sendMessageComplete(res);
                break;

            case WHAT_SENDCAPTCHA:
                //dismiss captcha progress dialog
                removeDialog(DIALOG_SENDING_CAPTCHA);
                if (null == mSendCaptchaThread) break;
                //pass data to method
                res = mSendCaptchaThread.getResult();
                mSendCaptchaThread = null;
                displayMessageSendResult(res);
                break;
            }
        };
    };




    //---------- Public methods




    //---------- Private methods
    private void bindProvidersSpinner()
    {
        ArrayAdapter<SmsProvider> adapter = new ArrayAdapter<SmsProvider>(this,
                android.R.layout.simple_spinner_item, mAppEnv.getProviderList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpiProviders.setAdapter(adapter);
    }
    
    private void bindSubservicesSpinner(SmsProvider provider)
    {
        List<SmsService> subservices = provider.getAllSubservices();

        //display a message when subservices are not configured
        if (null == subservices || subservices.size() <= 0)
            mActivityHelper.showInfo(this, String.format(
                    getString(R.string.actsendsms_msg_subservicesNotPresent), mSelectedProvider.getName()));
        
        ArrayAdapter<SmsService> adapter = new ArrayAdapter<SmsService>(this,
                android.R.layout.simple_spinner_item, subservices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpiSubservices.setAdapter(adapter);
    }

    /**
     * Called when the selected provider changes
     * @param provider
     * @param forceRefresh refresh the provider and service list also if
     *        the same provider was selected
     */
    private void changeProvider(SmsProvider provider, boolean forceRefresh)
    {
        //already selected provider
        if (!forceRefresh && provider == mSelectedProvider) return;
        
        //first, check if selected service has subservices
        if (provider.hasSubServices()) {
            mSelectedProvider = provider;
            changeService(null);
            
            //configure subservice spinner
            bindSubservicesSpinner(provider);
            mSpiSubservices.setVisibility(View.VISIBLE);
            mLblProvider.setText(R.string.actsendsms_lblProviderMulti);

        } else {
            mSelectedProvider = provider;
            mSelectedServiceId = provider.getId();

            mSpiSubservices.setVisibility(View.GONE);
            mLblProvider.setText(R.string.actsendsms_lblProviderSingle);
            updateMessageLength();
        }
    }

    /**
     * Called when the selected subservice changes
     * @param service
     */
    private void changeService(SmsService service)
    {
        String newServiceId = null != service ? service.getId() : null;

        //already selected subservice
        if (newServiceId == mSelectedServiceId) return;
        
        mSelectedProvider.setSelectedSubservice(newServiceId);
        mSelectedServiceId = newServiceId;
        updateMessageLength();
    }

    
    private void updateMessageLength(){
        int maxLength = 0;

        if (null != mSelectedProvider)
            maxLength = mSelectedProvider.getMaxMessageLenght();

        //set length text
        mLblMessageLength.setText(mTxtBody.length() + "/" + maxLength);
        //set text color
        boolean tooLong = mTxtBody.length() > maxLength;
        if (tooLong)
            mLblMessageLength.setTextColor(Color.RED);
        else
            mLblMessageLength.setTextColor(Color.GRAY);
    }
    
    
    /**
     * Assign an phone number to destination view
     * @param contactUri 
     */
    private void assignContactPhone(Uri contactUri)
    {
        try{
            //get phone numbers for selected contact
            mPhonesToShowInDialog = ContactsDao.instance().getContactNumbers(this, contactUri);
        } catch (Exception e) {
            mPhonesToShowInDialog = null;
            mActivityHelper.reportError(this, String.format(getText(R.string.common_msg_genericError).toString(), e.getMessage()));
        }
        
        if (null == mPhonesToShowInDialog)
            return;
        
        switch (mPhonesToShowInDialog.size()){

            //no phones for the contact selected
            case 0:
                mActivityHelper.showInfo(this, R.string.actsendsms_msg_noPhoneNumber);
                break;

            //contact has only one phone number
            case 1:
                mTxtDestination.setText(mPhonesToShowInDialog.get(0).getNumber());
                break;
        
            //contact has more than one phone number
            default:
                showDialog(DIALOG_PHONES);
                break;
        }
        
    }
    
    /**
     * Create a list dialog box that shows all the contact phone numbers
     * @return
     */
    private Dialog createPhonesDialog()
    {
        //create the phone numbers selections
        //final CharSequence[] items = {"Red", "Green", "Blue"};
        int i=0;
        CharSequence[] items = new CharSequence[mPhonesToShowInDialog.size()];
        
        for (ContactPhone phone : mPhonesToShowInDialog) {
            items[i++] = getTranslationForPhoneType(phone.getType()) + ": " + phone.getNumber();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getText(R.string.actsendsms_dlgPickNumber));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mTxtDestination.setText(mPhonesToShowInDialog.get(item).getNumber());
                removeDialog(DIALOG_PHONES);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                removeDialog(DIALOG_PHONES);
            }
        });
        AlertDialog alert = builder.create();
        
        return alert;
    }


    /**
     * Create dialog for captcha code
     * 
     * @return
     */
    private Dialog createCaptchaDialog(final String providerReply)
    {
        ResultOperation<Object> res = mSelectedProvider.getCaptchaContentFromProviderReply(providerReply);
        if (res.hasErrors()) {
            //show errors
            mActivityHelper.showInfo(this, res.getResult().toString());
            //and returns with no dialog created
            return null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog;

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(
                R.layout.dlgcaptcha, (ViewGroup) findViewById(R.id.dlgcaptcha_layoutroot));
        
        //load the image
        ImageView mImgCaptcha = (ImageView) layout.findViewById(R.id.dlgcaptcha_imgCaptcha);
        byte[] imageData = (byte[]) res.getResult();
        mLogFacility.i(LOG_HASH, "Captcha lenght: " + imageData.length);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
        mImgCaptcha.setImageBitmap(bitmap);
        
        //
        final EditText mTxtCode = (EditText) layout.findViewById(R.id.dlgcaptcha_txtCode);

        ((Button) layout.findViewById(R.id.dlgcaptcha_btnOk)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                removeDialog(DIALOG_CAPTCHA_REQUEST);
                String code = mTxtCode.getText().toString();
                sendCaptcha(providerReply, code);
            }
        });
        ((Button) layout.findViewById(R.id.dlgcaptcha_btnCancel)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                removeDialog(DIALOG_CAPTCHA_REQUEST);
            }
        });

        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.setTitle(R.string.dlgcaptcha_title);
        return alertDialog;
    }
    

    /**
     * Associates phone type to a label
     * 
     * @param type
     * @return
     */
    private String getTranslationForPhoneType(String type)
    {
        if ("1".equals(type))
            return getString(R.string.actsendsms_phoneType_home);
        else if ("2".equals(type))
            return getString(R.string.actsendsms_phoneType_mobile);
        else if ("3".equals(type))
            return getString(R.string.actsendsms_phoneType_work);
        else if ("4".equals(type))
            return getString(R.string.actsendsms_phoneType_workfax);
        else if ("5".equals(type))
            return getString(R.string.actsendsms_phoneType_homefax);
        else
            return getString(R.string.actsendsms_phoneType_other);
    }
    
    /**
     * Add signature to current message
     */
    private void addSignature() {
        String signature = mAppPreferencesDao.getSignature();
        String message = mTxtBody.getText().toString();

        //check if the signature was already added
        if (!message.endsWith(signature))
            mTxtBody.append(signature);
    }
    
    
    /**
     * Called when activity is first loaded, restore
     * previous status of input views. Called also when the activity is rotated,
     * because not always the text view values are preserver
     */
    private void restoreLastRunViewValues()
    {
        //text and message
        mTxtDestination.setText(mAppPreferencesDao.getLastUsedDestination());
        mTxtBody.setText(mAppPreferencesDao.getLastUsedMessage());

        //reassign spinner values and status of class inner fields
        String providerId = mAppPreferencesDao.getLastUsedProviderId();
        String subserviceId = mAppPreferencesDao.getLastUsedSubserviceId();

        SmsProvider provider = null;
        if (TextUtils.isEmpty(providerId)) {
            mLogFacility.v(LOG_HASH, "Must reassign providerId, because it's empty");
        } else {
            //find provider provider
            provider = GlobalHelper.findProviderInList(mAppEnv.getProviderList(), providerId);
        }
        
        //if provider is null, assign first provider as default value
        if (null == provider) {
            mLogFacility.v(LOG_HASH, "Cannot find provider for providerId " + providerId);
            //get first provider of the list as fallback
            if (!mAppEnv.getProviderList().isEmpty())
                provider = mAppEnv.getProviderList().get(0);
        }
        
        if (null != provider) {
            changeProvider(provider, false);
            //i cannot rely on the call to changeProvider inside the SelectionChangeListener event
            //in the provider spinner, because is execute at the end of this method, but i need it
            //before assign subservice
            int providerPos = GlobalHelper.findProviderPositionInList(mAppEnv.getProviderList(), providerId);
            if (providerPos >= 0) mSpiProviders.setSelection(providerPos);
            
            if (null != provider && provider.hasSubServices() && !TextUtils.isEmpty(subserviceId)){
                int subservicePos = mSelectedProvider.findSubservicePositionInList(subserviceId);
                if (subservicePos >= 0) mSpiSubservices.setSelection(subservicePos);
                //call changeSubservice after this function, but it's ok now
            }
        } else {
            mLogFacility.v(LOG_HASH, "Cannot find a suitable provider for the list");
        }
    }


    /**
     * Send message
     */
    private void sendMessage()
    {
        //checks if can send another SMS
        if (!mLogicManager.checkIfCanSendSms(getBaseContext())) {
            //TODO transform in informative dialog
            mActivityHelper.showInfo(ActSendSms.this, R.string.common_msg_smsLimitReach, Toast.LENGTH_LONG);
            return;
        }
        
        String errorMessage;
        String destination = RainbowStringHelper.cleanPhoneNumber(mTxtDestination.getText().toString()).trim(); 
        String message = mTxtBody.getText().toString();

        //checks for other errors
        switch (mLogicManager.checkMessageValidity(mSelectedProvider, mSelectedServiceId, destination, message)) {
        case CanSend:
            errorMessage = null;
            break;
        case EmptyDestination:
            errorMessage = getString(R.string.actsendsms_msg_noDestination);
            break;
        case EmptyMessage:
            errorMessage = getString(R.string.actsendsms_msg_noMessage);
            break;
        case InvalidProvider:
            errorMessage = getString(R.string.actsendsms_msg_noProviderSelected);
            break;
        case InvalidService:
            errorMessage = getString(R.string.actsendsms_msg_noSubserviceSelected);
            break;
        case MessageTooLong:
            errorMessage = getString(R.string.actsendsms_msg_messageTooLong);
            break;
        case ProviderHasNoParameters:
            errorMessage = String.format(
                    getString(R.string.actsendsms_msg_providerNotConfigured),
                    mSelectedProvider.getName());
            break;
        case ServiceHasNoParameters:
            errorMessage = getString(R.string.actsendsms_msg_subserviceNotConfigured);
            break;
        default:
            errorMessage = null;
            break;
        }
        
        //eventually display a error message and exit
        if (!TextUtils.isEmpty(errorMessage)) {
            mActivityHelper.showInfo(this, errorMessage, Toast.LENGTH_LONG);
            return;
        }
        
        //create new progress dialog
        showDialog(DIALOG_SENDING_MESSAGE);

        //preparing the background task for sending message
        mSendMessageThread = new SendMessageThread(
                this, mActivityHandler, WHAT_SENDMESSAGE,
                mSelectedProvider, mSelectedServiceId,
                destination, message);
        mSendMessageThread.start();
        //at the end of the execution, the activity handler will be called
    }
    
    
    /**
     * Send the captcha code to the server
     * @param providerReply
     * @param captchaCode
     */
    private void sendCaptcha(String providerReply, String captchaCode)
    {
        mLogFacility.i(LOG_HASH, "Captcha code: " + captchaCode);
        if (TextUtils.isEmpty(captchaCode)) {
            mActivityHelper.showInfo(ActSendSms.this, R.string.actsendsms_msg_emptyCaptchaCode);
            return;
        }

        //create new progress dialog
        showDialog(DIALOG_SENDING_CAPTCHA);
        
        //preparing the background task for sending captcha code
        mSendCaptchaThread = new SendCaptchaThread(
                this, mActivityHandler, WHAT_SENDCAPTCHA,
                mSelectedProvider,
                providerReply,
                captchaCode);
        //and execute the command
        mSendCaptchaThread.start();
        //at the end of the execution, a caller to handler will be make
    }
    
    
    /**
     * Reset destination and message body
     */
    private void cleanDataFields(boolean force) {
        if (force || mAppPreferencesDao.getAutoClearMessage()) {
            mTxtDestination.setText("");
            mTxtBody.setText("");
        }
    }
    
    
    /**
     * Called when the background activity has sent the message
     * @param result result of sending message
     */
    private void sendMessageComplete(RainbowResultOperation<String> result)
    {
        //captcha required
        if (ResultOperation.RETURNCODE_SMS_CAPTCHA_REQUEST == result.getReturnCode()) {
            //save captcha data
            mCaptchaStorage = result.getResult();
            mLogFacility.i(LOG_HASH, "Captcha request from provider: " + mCaptchaStorage);
            //launch captcha request
            showDialog(DIALOG_CAPTCHA_REQUEST);
        
        //return with errors or message sent
        } else  {
            displayMessageSendResult(result);
        }
    }

    
    /**
     * Display result of the send message action, eventually processing low level errors
     * 
     * @param result
     */
    private void displayMessageSendResult(RainbowResultOperation<String> result)
    {
        //return with errors
        if (result.hasErrors()) {
            mErrorSendingMessage = mActivityHelper.getErrorMessage(result.getReturnCode(), result.getException());
            mLogFacility.e(LOG_HASH, "Error sending message: " + mErrorSendingMessage);
            showDialog(DIALOG_SENDING_MESSAGE_ERROR);
            return;

        } else {
            mLogFacility.i(LOG_HASH, "Send message result: " + result.getResult());
            //display returning message of the provider
            mActivityHelper.showInfo(ActSendSms.this, result.getResult(), Toast.LENGTH_LONG);

            //update number of messages sent in the day
            mLogicManager.updateSmsCounter(1);
            //insert sms into pim, if required
            insertSmsIntoPim();
            //check if the text should be deleted
            cleanDataFields(false);
        }
    }

    /**
     * Insert sent SMS into PIM of the device
     */
    private void insertSmsIntoPim() {
        //insert SMS into PIM
        if (mAppPreferencesDao.getInsertMessageIntoPim()) {
            String destination = RainbowStringHelper.cleanPhoneNumber(mTxtDestination.getText().toString()).trim();
            ResultOperation<Void> res = mSmsDao.saveSmsInSentFolder(
                    getApplicationContext(),
                    destination,
                    mTxtBody.getText().toString());

            if (res.hasErrors()) {
                mActivityHelper.reportError(getApplicationContext(), R.string.actsendsms_msg_error_saving_sent_sms);
            }
        }
    }
    
    /**
     * Called when the activity first start, process data passed via intent in reply to
     * intent-filter android.intent.action.SENDTO or android.intent.action.SEND 
     * 
     * @param intent 
     * 
     */
    private void processIntentData(Intent intent)
    {
        if (null == intent || null == intent.getExtras()) return;
    
        String destination = intent.getExtras().getString(ActivityHelper.INTENTKEY_MESSAGE_DESTIONATION);
        String text = intent.getExtras().getString(ActivityHelper.INTENTKEY_MESSAGE_TEXT);
        
        if (!TextUtils.isEmpty(destination))
            mTxtDestination.setText(destination);
        if (!TextUtils.isEmpty(text))
            mTxtBody.setText(text);
    }

    
    /**
     * Create a list dialog box that shows all the templates available
     * @return
     */
    private Dialog createTemplatesDialog()
    {
        String[] templates = mAppPreferencesDao.getMessageTemplates();
        CharSequence[] items = new CharSequence[templates.length];
        
        int i=0;
        for (String template : templates) {
            items[i++] = template;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getText(R.string.actsendsms_dlgPickTemplate));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String template = (String) ((AlertDialog) dialog).getListView().getItemAtPosition(item);
                mTxtBody.append(template);
                removeDialog(DIALOG_TEMPLATES);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                removeDialog(DIALOG_TEMPLATES);
            }
        });
        AlertDialog alert = builder.create();
        
        return alert;
    }
}
