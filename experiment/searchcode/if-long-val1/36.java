package com.injoit.examplepush.push.storages;

import java.util.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.database.*;
import net.rim.device.api.io.*;
import net.rim.device.api.system.*;
import net.rim.device.api.util.*;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.collection.util.*;

import com.injoit.examplepush.push.models.PushNotificationObject;

public class PushNotificationsStorage
{
    public static final long STORE_KEY = 0x15e4ed0345ea49d8L;
    private static final long GUID = 0xb5ad683906dc4403L;
    
    private static PushNotificationsStorage instance;
    private static BigVector records = new BigVector();
    private static PersistentObject  persist;
    

    static
    {
        persist = PersistentStore.getPersistentObject( STORE_KEY );
        if( persist.getContents() == null )
        {
            persist.setContents(new BigVector());
            persist.commit();
        } 
        records = (BigVector)persist.getContents();
    }

    public static synchronized PushNotificationsStorage getInstance()
    {
        if(instance == null)
        {
            instance = (PushNotificationsStorage)RuntimeStore.getRuntimeStore().get(GUID);
            if(instance == null)
            {
                PushNotificationsStorage newInstance = new PushNotificationsStorage();
                RuntimeStore.getRuntimeStore().put(GUID, newInstance);
                instance = newInstance;
            }
        }      
        return instance;
    }

    public void addPushNotification (PushNotificationObject pn) throws Exception
    {
        if(pn != null)
        {
            synchronized(records)
            {
                records.addElement(pn);
                persist.commit();
            }
        }
    }
    
    public void removePushNotification(PushNotificationObject _pn)  throws Exception
    {
        synchronized(records)
        {
            for(int i=0; i<records.size(); i++)
            {
                PushNotificationObject pn = (PushNotificationObject) records.elementAt(i);
                if(pn.getCreationTime().equals(_pn.getCreationTime()))
                {
                    records.removeElementAt(i);
                    persist.commit();
                    break;
                }
            }
        }
    }
    
    public int getAmountUnreadNotifications()  throws Exception
    {
        synchronized(records)
        {
            int amountUnreadNotifications = 0;
            for(int i=0; i<records.size(); i++)
            {
                PushNotificationObject pn = (PushNotificationObject) records.elementAt(i);
                
                if(pn.getNotificationReadStatus() == false)
                {
                    amountUnreadNotifications += 1;    
                }
                
            }
            return amountUnreadNotifications;
        }
    }
    
    public void Commit() throws Exception
    {
        synchronized(records)
        {
            persist.commit();
        }
    }
    
    public  SimpleSortingVector getPushNotifications() throws Exception
    {
        SimpleSortingVector sortedList = new SimpleSortingVector();
        synchronized(records)
        {
            for(int i=0; i<records.size(); i++)
            {
                PushNotificationObject pno = (PushNotificationObject) records.elementAt(i);
                sortedList.addElement(pno);
            }
            sortedList.setSortComparator(new LongComparator());
            sortedList.setSort(true);
        }
        return sortedList;
    }
    
    private class LongComparator implements Comparator {
           
        public int compare(Object o1, Object o2) {
            PushNotificationObject item1 = (PushNotificationObject) o1;
            PushNotificationObject item2 = (PushNotificationObject) o2;
            
            Long date1 = new Long(item1.getTimeForSort());
            Long date2 = new Long(item2.getTimeForSort());
                        
            long val1 = ((Long) date1).longValue();
            long val2 = ((Long) date2).longValue();
            return val1 > val2 ? -1 : val1 == val2 ? 0 : 1;
        }
    };
} 



