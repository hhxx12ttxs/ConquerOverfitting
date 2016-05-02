package com.android.qiushi.Control;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class Global {
	private static Global inst=null;

	private static ControlThread mThread = null;

	public static int custId = -1;
	public static int familyId = -1;
	public static String userId = null;
	public static String userPwd = null;
	
	public static Room room = new Room();
	
	public static Room rooms[];
	public static Scene scenes[];
	public static Ea eas[];
	
	public static String strLight[];
	public static int idLight[];
	public static int swLight[];
	
	public static String strDianShi[];
	public static int idDianShi[];
	
	public static String strKongTiao[];
	public static int idKongTiao[];
	
	public static String strFengShan[];
	public static int idFengShan[];
	
	public static String strYinXiang[];
	public static int idYinXiang[];
	
	
	public static int listId=0;
	
	private Global()
	{
		init();
	}
	
	private void init()
	{
		ControlThread.getInstance();
		
	}
	/**
	 * 判断是否连接WiFi
	 * @param inContext
	 * @return 如果连接成功返回true，否则返回false
	 */
	public static boolean isWiFiActive(Context inContext) {
          WifiManager mWifiManager = (WifiManager) inContext
          .getSystemService(Context.WIFI_SERVICE);
          WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
          int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
          if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
          System.out.println("**** WIFI is on");
              return true;
          } else {
             System.out.println("**** WIFI is off");
             return false;   
         }
	}
	
	/**
	 * 判断是否连接3G网络
	 * @param context
	 * @return	如果连接成功返回true，否则返回false
	 */
	public static boolean isNetworkAvailable( Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity == null) {
                      System.out.println("**** newwork is off");
                        return false;
                } else {
                        NetworkInfo info = connectivity.getActiveNetworkInfo();
                        if(info == null){
                              System.out.println("**** newwork is off");
                                return false;
                        }else{
                                if(info.isAvailable()){
                                      System.out.println("**** newwork is on");
                                        return true;
                                }
                              
                        }
                }
                  System.out.println("**** newwork is off");
        return false;
    }
	
	
	 public static Bitmap decodeFile(String path,int w,int h) {
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				options.outWidth = 0;
				options.outHeight = 0;
				options.inSampleSize = 1;
				BitmapFactory.decodeFile(path, options);
				
				if(options.outWidth > 0 && options.outHeight > 0){
			
						
						int widthFactor = (options.outWidth  - 1)
								/ w;
						int heightFactor = (options.outHeight  - 1)
								/ h;
						widthFactor = Math.max(widthFactor, heightFactor);
						widthFactor = Math.max(widthFactor, 1);
						
						if (widthFactor > 1) {
							if ((widthFactor & (widthFactor - 1)) != 0) {
								while ((widthFactor & (widthFactor - 1)) != 0) {
									widthFactor &= widthFactor - 1;
								}

								widthFactor <<= 1;
							}
						}
						options.inSampleSize = widthFactor;
						options.inJustDecodeBounds = false;
						Bitmap bitmap = 
								BitmapFactory.decodeFile(path, options);
						if (bitmap != null) {
							return bitmap;
						}
					}
				
			} catch(Exception e) { }
		
		return null;
	}
	
	
	static public Global getInstance()
	{
		if(inst == null)
			inst = new Global();
		return inst;
	}
	
	static public int initEaList()
	{
		String[] slight = new String[1024];
		int[] ilight = new int[1024];
		int[] swlight = new int[1024];
		int clight = 0;
		
		String[] sdianshi = new String[1024];
		int[] idianshi = new int[1024];
		int cdianshi = 0;

		String[] skongtiao = new String[1024];
		int[] ikongtiao = new int[1024];
		int ckongtiao = 0;

		String[] sfengshan = new String[1024];
		int[] ifengshan = new int[1024];
		int cfengshan = 0;

		String[] syinxiang = new String[1024];
		int[] iyinxiang = new int[1024];
		int cyinxiang = 0;

		int i;
		
		Ea[] ea = room.eaList;
		
		for(i=0; i<ea.length; i++)
		{
			switch(ea[i].tpId)
			{
			case ControlThread.TPID_LIGHT:
				slight[clight] = ea[i].eaNm;
				ilight[clight] = ea[i].idKey;
				swlight[clight] = ea[i].eaSwitch;
				clight++;
				break;
			case ControlThread.TPID_TV:
				sdianshi[cdianshi] = ea[i].eaNm;
				idianshi[cdianshi] = ea[i].idKey;
				cdianshi++;

				break;
			case ControlThread.TPID_AV:
				syinxiang[cyinxiang] = ea[i].eaNm;
				iyinxiang[cyinxiang] = ea[i].idKey;
				cyinxiang++;
				break;
			case ControlThread.TPID_AIR:
				skongtiao[ckongtiao] = ea[i].eaNm;
				ikongtiao[ckongtiao] = ea[i].idKey;
				ckongtiao++;
				break;
			case ControlThread.TPID_FAN:
				sfengshan[cfengshan] = ea[i].eaNm;
				ifengshan[cfengshan] = ea[i].idKey;
				cfengshan++;
				break;			
			}
		}
		
		strLight = new String[clight];
		swLight = new int[clight];
		idLight = new int[clight];
		for(i=0;i<clight;i++)
		{
			strLight[i] = slight[i];
			idLight[i] = ilight[i];
			swLight[i] = swlight[i];
		}

		strDianShi = new String[cdianshi];
		idDianShi = new int[cdianshi];
		for(i=0;i<cdianshi;i++)
		{
			strDianShi[i] = sdianshi[i];
			idDianShi[i] = idianshi[i];
		}

		strYinXiang = new String[cyinxiang];
		idYinXiang = new int[cyinxiang];
		for(i=0;i<cyinxiang;i++)
		{
			strYinXiang[i] = syinxiang[i];
			idYinXiang[i] = iyinxiang[i];
		}

		strKongTiao = new String[ckongtiao];
		idKongTiao = new int[ckongtiao];
		for(i=0;i<ckongtiao;i++)
		{
			strKongTiao[i] = skongtiao[i];
			idKongTiao[i] = ikongtiao[i];
		}

		strFengShan = new String[cfengshan];
		idFengShan = new int[cfengshan];
		for(i=0;i<cfengshan;i++)
		{
			strFengShan[i] = sfengshan[i];
			idFengShan[i] = ifengshan[i];
		}

		return 0;
	}
	
}

