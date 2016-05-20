package org.safermobile.clear.micro.apps.views;


import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

import org.j4me.ui.*;
import org.j4me.ui.components.*;
import org.safermobile.clear.micro.L10nConstants;
import org.safermobile.clear.micro.L10nResources;
import org.safermobile.clear.micro.apps.LocaleManager;
import org.safermobile.clear.micro.apps.ITCMainMIDlet;
import org.safermobile.clear.micro.apps.ITCConstants;
import org.safermobile.clear.micro.apps.controllers.ShoutController;
import org.safermobile.clear.micro.ui.ErrorAlert;
import org.safermobile.micro.utils.Logger;
import org.safermobile.micro.utils.Preferences;

/**
 * Example of a <code>TextBox</code> component.
 */
public class ShoutManualForm
        extends Dialog implements Runnable, OnClickListener
{
        /**
         * The previous screen.
         */
        private ITCMainMIDlet _midlet;
        
        /**
         * The number box used by this example for entering phone numbers.
         */
        private TextBox tbPhoneNumber;        
        private TextBox tbMessage;
        
    	private Label _label = new Label();

    	L10nResources l10n = LocaleManager.getResources();

    	private Preferences _prefs;
    	
        /**
         * Constructs a screen that shows a <code>TextBox</code> component in action.
         * 
         * @param previous is the screen to return to once this done.
         */
        public ShoutManualForm (ITCMainMIDlet midlet)
        {
               _midlet = midlet;
                
                try
        		{
        		 _prefs = new Preferences (ITCConstants.PANIC_PREFS_DB);
        		} catch (RecordStoreException e) {
        			
        			Logger.error(ITCConstants.TAG, "a problem saving the prefs: " + e, e);
        		}
                
                setupUI();
        }
        
        public void setupUI ()
        {
                setTitle( "Send Emergency Message" );
               // setMenuText( "Cancel" ,  l10n.getString(L10nConstants.keys.MENU_SEND) );

             // Center the text.
        		_label.setHorizontalAlignment( Graphics.LEFT );

        		_label.setLabel("Emergency Message");
        		
        		// Add the label to this screen.
        		append( _label );
        		
                // Add the phone number box.
        		tbPhoneNumber = new TextBox();
        		tbPhoneNumber.setLabel( l10n.getString(L10nConstants.keys.SMS_TEST_LBL_PHONE) );
                tbPhoneNumber.setForPhoneNumber();
                tbPhoneNumber.setMaxSize( 20 );
                append( tbPhoneNumber );
                
                // Add the phone number box.
        		tbMessage = new TextBox();
        		tbMessage.setLabel(l10n.getString(L10nConstants.keys.SMS_TEST_LBL_MSG));        		
                append( tbMessage );

                Button btn = new Button();
        		btn.setOnClickListener(this);
        		btn.setLabel(l10n.getString(L10nConstants.keys.MENU_SEND));
        		append (btn);

                load();
               
        }

        public boolean hasMenuBar ()
        {
        	return false;
        }
        
		public void onClick(Component c) 
		{
			new Thread(this).start();
			
		}
		
		protected void declineNotify ()
		{
			_midlet.showMainForm();
		}
		
      
        public void run ()
        {
        	sendShoutMessage();

        }
        
        private void load ()
        {
        	if (_prefs.get(ITCConstants.PREFS_KEY_RECIPIENT) != null)
        	{
        		tbPhoneNumber.setString(_prefs.get(ITCConstants.PREFS_KEY_RECIPIENT));
        		tbMessage.setString(_prefs.get(ITCConstants.PREFS_KEY_MESSAGE));
        	}
        }
        
        private void sendShoutMessage ()
        {
        	String userName = "";
        	
        	String recip = tbPhoneNumber.getString();
        	String msg =  tbMessage.getString();
        	
        	ShoutController sc = new ShoutController();
        	
        	String data = sc.buildDataMessage(userName);
        	
        	try {
				sc.sendSMSShout(recip, msg, data);
				ErrorAlert eAlert = new ErrorAlert ("Success!", "Your Shout! was sent.", null, this);
				eAlert.show();
				
			} catch (Exception e) {
				
				ErrorAlert eAlert = new ErrorAlert ("Error!", "Unable to send Shout! message. Try again.", null, this);
				eAlert.show();
				e.printStackTrace();
			}
        }
        
}

