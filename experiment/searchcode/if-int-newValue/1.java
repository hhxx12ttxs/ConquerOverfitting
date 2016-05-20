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
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */

package it.rainbowbreeze.smsforfree.data;

import it.rainbowbreeze.libs.data.RainbowAppPreferencesDao;
import it.rainbowbreeze.libs.helper.RainbowArrayHelper;
import it.rainbowbreeze.smsforfree.common.AppEnv;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferencesDao
	extends RainbowAppPreferencesDao
{
	//---------- Private fields
	
    protected static final String PROP_AUTO_CLEAR_MESSAGE = "clearmessage";
    protected static final String PROP_INSERT_MESSAGE_INTO_PIM = "insertmessageintopim";
    protected static final String PROP_DEFAULT_INTERNATIONAL_PREFIX = "defaultInternationalPrefix";
    protected static final String PROP_SIGNATURE = "signature";
    protected static final String PROP_SMSCOUNTER_DATE = "smsCounterDate";
    protected static final String PROP_SMSCOUNTER_NUMBERFORCURRENTDAY = "smsCounterNumber";
    protected static final String PROP_SMSCOUNTER_TOTAL = "smsCounterTotal";
    protected static final String PROP_MESSAGETEMPLATES = "messageTemplates";
    protected static final String PROP_SHOW_ONLY_MOBILE_NUMBERS = "showOnlyMobileNumbers";
    protected static final String PROP_ASK_CONFIRMATION_FOR_SENDING = "AskConfirmationForSending";
    
    protected static final String PROP_LASTUSED_PROVIDERID = "lastusedProvider";
    protected static final String PROP_LASTUSED_SUBSERVICEID = "lastusedSubservice";
    protected static final String PROP_LASTUSED_DESTINATION = "lastusedDestination";
    protected static final String PROP_LASTUSED_MESSAGE = "lastusedMessage";
    
    protected static final String TEMPLATES_SEPARATOR = "§§§§";
    
    
    

	//---------- Public Properties
	public AppPreferencesDao(Context context, String preferenceKey) {
		super(context, preferenceKey);
	}




	//---------- Public Properties




	//---------- Public Methods

    public boolean getAutoClearMessage()
    { return mSettings.getBoolean(PROP_AUTO_CLEAR_MESSAGE, false); }
    public void setAutoClearMessage(boolean newValue)
    { mEditor.putBoolean(PROP_AUTO_CLEAR_MESSAGE, newValue); }
    
    public boolean getInsertMessageIntoPim()
    { return mSettings.getBoolean(PROP_INSERT_MESSAGE_INTO_PIM, false); }
    public void setInsertMessageIntoPim(boolean newValue)
    { mEditor.putBoolean(PROP_INSERT_MESSAGE_INTO_PIM, newValue); }
    
    public boolean getShowOnlyMobileNumbers()
    { return mSettings.getBoolean(PROP_SHOW_ONLY_MOBILE_NUMBERS, false); }
    public void setShowOnlyMobileNumbers(boolean newValue)
    { mEditor.putBoolean(PROP_SHOW_ONLY_MOBILE_NUMBERS, newValue); }
    
    public String getSignature()
    { return mSettings.getString(PROP_SIGNATURE, ""); }
    public void setSignature(String newValue)
    { mEditor.putString(PROP_SIGNATURE, newValue); }

    public String getDefaultInternationalPrefix()
    { return mSettings.getString(PROP_DEFAULT_INTERNATIONAL_PREFIX, AppEnv.ITALY_INTERNATIONAL_PREFIX); }
    public void setDefaultInternationalPrefix(String newValue)
    { mEditor.putString(PROP_DEFAULT_INTERNATIONAL_PREFIX, newValue); }

    public String getLastUsedProviderId()
    { return mSettings.getString(PROP_LASTUSED_PROVIDERID, ""); }
    public void setLastUsedProviderId(String newValue)
    { mEditor.putString(PROP_LASTUSED_PROVIDERID, newValue); }
    
    public String getLastUsedSubserviceId()
    { return mSettings.getString(PROP_LASTUSED_SUBSERVICEID, ""); }
    public void setLastUsedSubserviceId(String newValue)
    { mEditor.putString(PROP_LASTUSED_SUBSERVICEID, newValue); }
    
    public String getLastUsedDestination()
    { return mSettings.getString(PROP_LASTUSED_DESTINATION, ""); }
    public void setLastUsedDestination(String newValue)
    { mEditor.putString(PROP_LASTUSED_DESTINATION, newValue); }
    
    public String getLastUsedMessage()
    { return mSettings.getString(PROP_LASTUSED_MESSAGE, ""); }
    public void setLastUsedMessage(String newValue)
    { mEditor.putString(PROP_LASTUSED_MESSAGE, newValue); }
    
    public String getSmsCounterDate()
    { return mSettings.getString(PROP_SMSCOUNTER_DATE, ""); }
    public void setSmsCounterDate(String newValue)
    { mEditor.putString(PROP_SMSCOUNTER_DATE, newValue); }
    
    public int getSmsCounterNumberForCurrentDay()
    { return mSettings.getInt(PROP_SMSCOUNTER_NUMBERFORCURRENTDAY, 0); }
    public void setSmsCounterNumberForCurrentDay(int newValue)
    { mEditor.putInt(PROP_SMSCOUNTER_NUMBERFORCURRENTDAY, newValue); }
    
    public int getSmsTotalNumber()
    { return mSettings.getInt(PROP_SMSCOUNTER_TOTAL, 0); }
    public void setSmsTotalNumber(int newValue)
    { mEditor.putInt(PROP_SMSCOUNTER_TOTAL, newValue); }

	public String[] getMessageTemplates()
	{ return mSettings.getString(PROP_MESSAGETEMPLATES, "").split(TEMPLATES_SEPARATOR); }
	public void setMessageTemplates(String[] newValue)
	{ mEditor.putString(PROP_MESSAGETEMPLATES, RainbowArrayHelper.join(newValue, TEMPLATES_SEPARATOR)); }
    
    public boolean getAskConfirmationForSending()
    { return mSettings.getBoolean(PROP_ASK_CONFIRMATION_FOR_SENDING, false); }
    public void setAskConfirmationForSending(boolean newValue)
    { mEditor.putBoolean(PROP_ASK_CONFIRMATION_FOR_SENDING, newValue); }
    

    //---------- Protected Methods

    /* (non-Javadoc)
	 * @see com.angurialab.postino.data.BasePreferencesDao#backupProperties()
	 */
	@Override
	protected void backupProperties(SharedPreferences.Editor editorBackup)
	{
    	editorBackup.putBoolean(PROP_AUTO_CLEAR_MESSAGE, getAutoClearMessage());
    	editorBackup.putBoolean(PROP_INSERT_MESSAGE_INTO_PIM, getInsertMessageIntoPim());
        editorBackup.putBoolean(PROP_SHOW_ONLY_MOBILE_NUMBERS, getShowOnlyMobileNumbers());
    	editorBackup.putString(PROP_SIGNATURE, getSignature());
    	editorBackup.putString(PROP_DEFAULT_INTERNATIONAL_PREFIX, getDefaultInternationalPrefix());
    	editorBackup.putString(PROP_LASTUSED_PROVIDERID, getLastUsedProviderId());
    	editorBackup.putString(PROP_LASTUSED_SUBSERVICEID, getLastUsedSubserviceId());
    	editorBackup.putString(PROP_LASTUSED_DESTINATION, getLastUsedDestination());
    	editorBackup.putString(PROP_LASTUSED_MESSAGE, getLastUsedMessage());
	}

	/* (non-Javadoc)
	 * @see com.angurialab.postino.data.BasePreferencesDao#restoreProperties()
	 */
	@Override
	protected void restoreProperties(SharedPreferences settingsBackup)
	{
    	setAutoClearMessage(settingsBackup.getBoolean(PROP_AUTO_CLEAR_MESSAGE, false));
    	setInsertMessageIntoPim(settingsBackup.getBoolean(PROP_INSERT_MESSAGE_INTO_PIM, false));
    	setSignature(settingsBackup.getString(PROP_SIGNATURE, ""));
    	setDefaultInternationalPrefix(settingsBackup.getString(PROP_DEFAULT_INTERNATIONAL_PREFIX, ""));
    	setLastUsedProviderId(settingsBackup.getString(PROP_LASTUSED_PROVIDERID, ""));
    	setLastUsedSubserviceId(settingsBackup.getString(PROP_LASTUSED_SUBSERVICEID, ""));
    	setLastUsedDestination(settingsBackup.getString(PROP_LASTUSED_DESTINATION, ""));
    	setLastUsedMessage(settingsBackup.getString(PROP_LASTUSED_MESSAGE, ""));
    	setShowOnlyMobileNumbers(settingsBackup.getBoolean(PROP_SHOW_ONLY_MOBILE_NUMBERS, false));
	}
}
