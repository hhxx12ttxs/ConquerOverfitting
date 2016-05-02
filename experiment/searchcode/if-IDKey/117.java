package com.android.qiushi.Control;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.android.qiushi.MainActivity;
import com.android.qiushi.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.ImageView;


public class ControlThread{
	public static final int CMD_GET_HTTP_DATA = 0x0801;
	public static final int CMD_HTTP_LOGIN = 0x0802;
	public static final int CMD_HTTP_GETROOMS = 0x0803;
	public static final int CMD_HTTP_GETSCENES = 0x0804;
	public static final int CMD_HTTP_GETMACHINES = 0x0805;
	public static final int CMD_HTTP_GETSCENEEA = 0x0806;
	public static final int CMD_HTTP_CONTROL = 0x0807;
	public static final int CMD_HTTP_EACONTROL = 0x0808;
	public static final int CMD_HTTP_SCENECONTROL = 0x0809;
	
	public static final int CMD_HTTP_UPDATEROOMNM = 0x0810;
	public static final int CMD_HTTP_UPDATEROOMIMG = 0x0811;
	public static final int CMD_HTTP_UPDATEROOMSEQ	= 0x0812;
	
	public static final int CMD_HTTP_UPDATESCENENM = 0x0813;
	public static final int CMD_HTTP_UPDATESCENEIMG = 0x0814;
	public static final int CMD_HTTP_UPDATESCENESEQ	= 0x0815;

	public static final int CMD_HTTP_UPDATEEANM = 0x0816;
	public static final int CMD_HTTP_UPDATEEAIMG = 0x0817;
	public static final int CMD_HTTP_UPDATEEASEQ	= 0x0818;

	
	private static ControlThread mThread;
	public static boolean bInit = false;
	public static Handler mHandler;
	public static Handler pUIHandler;
	public static String target = "http://110.80.10.54:8082/QsApp/";
	public static String target_force = "http://110.80.10.54:8082/QsApp/";
	
	
	public static final int STR_RESULT   = 0x1001;
	public static final int STR_CUSTID   = 0x1002;
	public static final int STR_FAMILYID = 0x1003;
	public static final int STR_ROOMID   = 0x1004;
	public static final int STR_ROOMNM  = 0x1005;
	public static final int STR_SCENEDATA= 0x1006;
	public static final int STR_IDKEY    = 0x1007;
	public static final int STR_SCIMG    = 0x1008;
	public static final int STR_SCNM     = 0x1009;
	public static final int STR_EADATA   = 0x1010;
	public static final int STR_EAIMG    = 0x1011;
	public static final int STR_EANM     = 0x1012;
	public static final int STR_EASWITCH = 0x1013;
	public static final int STR_TPID     = 0x1014;
	public static final int STR_ROOMIMG	 = 0x1015;
	public static final int STR_ROOMDATA = 0x1016;
	public static final int STR_SCCODE	 = 0x1017;
	public static final int STR_CUSTOMBTNDATA	 = 0x1018;
	public static final int STR_CUSTOMCODE	= 0x1019;
	public static final int STR_CUSTOMNM = 0x1020;
	public static final int STR_CUSTOMVL = 0x1021;
	
	public static final int TPID_LIGHT = 1;
	public static final int TPID_TV	= 2;
	public static final int TPID_AV = 3;
	public static final int TPID_AIR = 4;
	public static final int TPID_FAN = 5;
	
	public static int opCode = 0;
	
	public static final String RES_FAMILYIMAGES = "resource/upLoadRes/familyImages/";
	public static final String RES_SCENEIMAGES = "resource/upLoadRes/sceneImages/";
	public static final String RES_EAIMAGES = "resource/upLoadRes/eaImages/";
	
	public static final String UPDATE_ROOMNM = "updateMsg!updateRoomNm.do";
	public static final String UPDATE_ROOMIMG = "updateMsg!updateRoomImg.do";
	
	public static final String UPDATE_SCENENM = "updateMsg!updateSceneNm.do";
	public static final String UPDATE_SCENEIMG = "updateMsg!updateSceneImg.do";
	
	public static final String UPDATE_EANM = "updateMsg!updateEaNm.do";
	public static final String UPDATE_EAIMG = "updateMsg!updateEaImg.do";
	
	public static final String UPDATE_ROOMSEQ = "updateMsg!updateRoomSeq.do";
	public static final String UPDATE_SCENESEQ = "updateMsg!updateSceneSeq.do";
	public static final String UPDATE_EASEQ = "updateMsg!updateEaSeq.do";
	
	private static Room pRoom;
	
	private ControlThread()
	{
		Log.d("HttpTest","Creating ControlThread.");
		 HandlerThread mHandlerThread = new HandlerThread("WorkerThread");
          mHandlerThread.start();
      
          mHandler = new Handler(mHandlerThread.getLooper()){
        		/* (non-Javadoc)
        		 * @see android.os.Handler#handleMessage(android.os.Message)
        		 */
        		@Override
        		public void handleMessage(Message msg) {
        			String url;
        			param p;
        			int ret = -1;
        			Log.d("HttpTest","ControlThread Handlemessage.");
        			
        			opCode = msg.what;
        			// TODO Auto-generated method stub
        			switch(msg.what){
        			case CMD_HTTP_LOGIN:
        				pRoom = Global.room;
        				url = (String)msg.obj;
        				ret = responseLogin(getResponse(url));
        				break;
        			case CMD_HTTP_GETROOMS:
        				url = (String)msg.obj;
        				ret = responseGetRooms(getResponse(url));
        				break;
        			case CMD_HTTP_GETSCENES:
        				url = (String)msg.obj;
        				ret = responseGetScenes(getResponse(url));
        				break;
        			case CMD_HTTP_GETMACHINES:
        				url = (String)msg.obj;
        				ret = responseGetMachines(getResponse(url));
        				break;
        			case CMD_HTTP_GETSCENEEA:
        				pRoom = Global.room;
        				url = (String)msg.obj;
        				ret = responseGetSceneEa(getResponse(url));
        				break;
        			case CMD_HTTP_CONTROL:
        				url = (String)msg.obj;
        				ret = responseControl(getResponse(url));
        				break;
        				
        			case CMD_HTTP_UPDATEROOMNM: 
        				p = (param)msg.obj;
        				processUpdateRoomNM(p.roomId, p.Name);
        				break;
        			case CMD_HTTP_UPDATEROOMIMG: 
        				p = (param)msg.obj;
        				processUpdateRoomImg(p.roomId, p.bp);
        				break;
        			case CMD_HTTP_UPDATEROOMSEQ: 
        				p = (param)msg.obj;
        				processUpdateRoomSeq(p.ids, p.seq);
        				break;

        			case CMD_HTTP_UPDATESCENENM: 
        				p = (param)msg.obj;
        				processUpdateSceneNM(p.sceneId, p.Name);
        				break;
        			case CMD_HTTP_UPDATESCENEIMG: 
        				p = (param)msg.obj;
        				processUpdateSceneImg(p.sceneId, p.bp);
        				break;
        			case CMD_HTTP_UPDATESCENESEQ: 
        				p = (param)msg.obj;
        				processUpdateSceneSeq(p.ids, p.seq);
        				break;

        			case CMD_HTTP_UPDATEEANM: 
        				p = (param)msg.obj;
        				processUpdateEaNM(p.eaId, p.Name);
        				break;
        			case CMD_HTTP_UPDATEEAIMG: 
        				p = (param)msg.obj;
        				processUpdateEaImg(p.eaId, p.bp);
        				break;
        			case CMD_HTTP_UPDATEEASEQ: 
        				p = (param)msg.obj;
        				processUpdateEaSeq(p.ids, p.seq);
        				break;	
        			default:
        				break;
        				
        			}
        			Message uiMsg = pUIHandler.obtainMessage();
        			uiMsg.what = msg.what;
        			uiMsg.arg1 = ret;
        			uiMsg.sendToTarget();
        			super.handleMessage(msg);
        			
        			if(msg.what == CMD_HTTP_GETROOMS)
        			{
        				int i, j;
        				for(i = 0;i< Global.rooms.length; i++)
        				{
        					Global.rooms[i] = getRoomData(Global.rooms[i].roomId);
        					Global.rooms[i].roomBitmap = getRoomImg(Global.rooms[i].roomImg);
        					
        					if(Global.room.roomId == Global.rooms[i].roomId)
        						Global.room = Global.rooms[i];
        				}
        				for(i = 0;i< Global.rooms.length; i++)
        				{
        					for(j=0; j< Global.rooms[i].sceneList.length; j++)
        					{
        						Global.rooms[i].sceneList[j].scBitmap = getSceneImg(Global.rooms[i].sceneList[j].scImg);
        					}
        					for(j=0; j< Global.rooms[i].eaList.length; j++)
        					{
        						Global.rooms[i].eaList[j].eaBitmap = getEaImg(Global.rooms[i].eaList[j].eaImg);
        					}
        					
        				}
        				
        				
        				bInit = true;
        			}
        		}
        		

        		
        	};
	}

	static ControlThread getInstance()
	{
		if(mThread == null)
			mThread = new ControlThread();
		return mThread;
	}
	public void run() {

		// TODO Auto-generated method stub
	    while (true) {
	        try {
	        
		        Thread.sleep(1000);//¾€³Ì•ºÍ£10Ãë£¬†ÎÎ»ºÁÃë
	        
	        } catch (InterruptedException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	        }
	    }

	}
	public static void setUIHandler(Handler p)
	{
		pUIHandler = p;
	}
	public static String getResponse(String url)
	{
			try{
	    		/*
		    	URL url = new URL(target+action+"?id="+id+"&pwd="+pwd);
		    	//Ê¹ÓÃHttpURLConnection´ò¿ªÁ¬½Ó 
		        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();  
		        //µÃµ½¶ÁÈ¡µÄÄÚÈÝ(Á÷) 
		        InputStreamReader in = new InputStreamReader(urlConn.getInputStream());  
		        //ÎªÊä³ö´´½¨BufferedReader 
		        BufferedReader buffer = new BufferedReader(in);  
		        String inputLine = null;  
		        //Ê¹ÓÃÑ­»·À´¶ÁÈ¡»ñµÃµÄÊý¾Ý 
		        while (((inputLine = buffer.readLine()) != null))  
		        {  
		        	//ÎÒÃÇÔÚÃ¿Ò»ÐÐºóÃæ¼ÓÉÏÒ»¸ö"\n"À´»»ÐÐ 
		            resultData += inputLine + "\n";  
		        }           
		        //¹Ø±ÕInputStreamReader 
		        in.close();  
		        //¹Ø±ÕhttpÁ¬½Ó 
		        urlConn.disconnect();*/
	    		//HttpGetÁ¬½Ó¶ÔÏó 
	    		///String httpUrl = target+action+"?id="+id+"&pwd="+pwd;
	    		Log.d("Test",url);
	            HttpGet httpRequest = new HttpGet(url);  
	            //È¡µÃHttpClient¶ÔÏó 
	            HttpClient httpclient = new DefaultHttpClient();  
	            //ÇëÇóHttpClient£¬È¡µÃHttpResponse 
	            HttpResponse httpResponse = httpclient.execute(httpRequest);  
	            //ÇëÇó³É¹¦ 
	            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)  
	            {  
	            	//È¡µÃ·µ»ØµÄ×Ö·û´® 
	                String strResult = EntityUtils.toString(httpResponse.getEntity()); 
	                Log.d("OctoHome",strResult);
	                return strResult;
	                
	            }else{  
	                  
	            }  
	            
	    	}catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    	return null;
	}
	
	public static int sendCommand(int type,String cmd)
	{	
        try {
        	
	        Message message=mHandler.obtainMessage();
	        message.what=type;
	        message.obj = cmd;
	        message.sendToTarget();//°lËÍÏûÏ¢
	        return 0;
        } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
        }
        return -1;
	}
	public static int sendCommand(int type,param cmd)
	{	
        try {
        	
	        Message message=mHandler.obtainMessage();
	        message.what=type;
	        message.obj = cmd;
	        message.sendToTarget();//°lËÍÏûÏ¢
	        return 0;
        } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
        }
        return -1;
	}

	
	public static int login(String id, String pwd)
	{
		String action = "login";
		String resultData = null;
		
		String httpUrl = target+action+"?id="+id+"&pwd="+pwd;
		ControlThread.sendCommand(CMD_HTTP_LOGIN,httpUrl);
		return 0;
	}
	public static int getRooms()
	{
		String action = "getrooms";
		
		String httpUrl = target+action+"?familyId="+Global.familyId;
		ControlThread.sendCommand(CMD_HTTP_GETROOMS, httpUrl);
		return 0;
	}
	public static int getScenes()
	{
		String action = "getscene";
		
		String httpUrl = target+action+"?familyId="+Global.familyId;
		ControlThread.sendCommand(CMD_HTTP_GETSCENES, httpUrl);
		return 0;
	}
	public static int getMachines()
	{
		String action = "getmachine";
		
		String httpUrl = target+action+"?familyId="+Global.familyId;
		ControlThread.sendCommand(CMD_HTTP_GETMACHINES, httpUrl);
		return 0;
	}
	public static int getSceneEa(int roomId)
	{
		String action = "getsceneea";
		
		String httpUrl = target+action+"?roomId="+roomId;
		ControlThread.sendCommand(CMD_HTTP_GETSCENEEA, httpUrl);
		return 0;
	}
	public static int getCurrentSceneEa()
	{
		getSceneEa(Global.room.roomId);
		return 0;
	}
	public static int responseLogin(String rep)
	{
		if(rep == null)return -1;
		
		byte[] sRep = rep.getBytes();
		int i;
		int login_result = 0;
		
		if(sRep[0] == '{')
		{
			rep = rep.substring(1);
			sRep = rep.getBytes();
			while(sRep[0]!='}'&&sRep.length!=0)
			{
				i = getString(sRep);
				if(i<=0)return -1;
				
				String cmd = rep.substring(1,i);
				if(sRep[i+1]!=':')return -1;
				rep = rep.substring(i+2);
				sRep = rep.getBytes();
				int index = getStringIndex(cmd);
				i = 0;
				switch(index){
				case STR_RESULT   : 
					i = rep.indexOf(",");
					if(i<0)
						i = rep.indexOf("}");
					if(i>0)
						login_result = Integer.parseInt(rep.substring(0,i));
					if(login_result != 4)
						return login_result;
					break;
				case STR_CUSTID   : 
					i = rep.indexOf(",");
					Global.custId = Integer.parseInt(rep.substring(0,i));
					
					break;
				case STR_FAMILYID : 
					i = rep.indexOf(",");
					Global.familyId = Integer.parseInt(rep.substring(0,i));

					break;
				case STR_ROOMID   : 
					i = rep.indexOf(",");
					pRoom.roomId = Integer.parseInt(rep.substring(0,i));

					break;
				case STR_ROOMNM  : 
					i = rep.indexOf(",");
					///login_result = Integer.parseInt(rep.substring(0,i-1));
					pRoom.roomNm = rep.substring(1,i-1);
					rep = rep.substring(i);
					sRep=rep.getBytes();
					i = 0;
					break;
				case STR_ROOMIMG  : 
					i = rep.indexOf(",");
					///login_result = Integer.parseInt(rep.substring(0,i-1));
					pRoom.roomImg = rep.substring(1,i-1);
					rep = rep.substring(i);
					sRep=rep.getBytes();
					i = 0;
					break;
				
				case STR_SCENEDATA: 
					rep = getSceneData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				case STR_EADATA   : 
					rep = getEaData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				case STR_CUSTOMBTNDATA:
					rep = getCustomBtnData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				default:
					return login_result;
				}
				if(sRep[i] == ',')
				{
					rep = rep.substring(i+1);
					sRep=rep.getBytes();				
				}
			}
			
		}
		return 0;
		
	}
	public static int responseGetRooms(String rep)
	{
		if(rep == null)return -1;
		
		byte[] sRep = rep.getBytes();
		int i;
		int login_result = 0;
		
		if(sRep[0] == '{')
		{
			rep = rep.substring(1);
			sRep = rep.getBytes();
			while(sRep[0]!='}'&&sRep.length!=0)
			{
				i = getString(sRep);
				if(i<=0)return -1;
				
				String cmd = rep.substring(1,i);
				if(sRep[i+1]!=':')return -1;
				rep = rep.substring(i+2);
				sRep = rep.getBytes();
				int index = getStringIndex(cmd);
				i = 0;
				switch(index){
				case STR_RESULT   : 
					i = rep.indexOf(",");
					if(i<0)
						i = rep.indexOf("}");
					if(i>0)
						login_result = Integer.parseInt(rep.substring(0,i));
					
					if(login_result != 3)
						return login_result;
					break;
				case STR_ROOMDATA: 
					rep = getRoomData(rep,Global.rooms);
					sRep = rep.getBytes();
					break;
				default:
					return -1;
				}
				if(sRep[i] == ',')
				{
					rep = rep.substring(i+1);
					sRep=rep.getBytes();				
				}				
			}
			
		}
		return 0;
		
	}
	public static int responseGetScenes(String rep)
	{
		if(rep == null)return -1;
		
		byte[] sRep = rep.getBytes();
		int i;
		int login_result = 0;
		
		if(sRep[0] == '{')
		{
			rep = rep.substring(1);
			sRep = rep.getBytes();
			while(sRep[0]!='}'&&sRep.length!=0)
			{
				i = getString(sRep);
				if(i<=0)return -1;
				
				String cmd = rep.substring(1,i);
				if(sRep[i+1]!=':')return -1;
				rep = rep.substring(i+2);
				sRep = rep.getBytes();
				int index = getStringIndex(cmd);
				i = 0;
				switch(index){
				case STR_RESULT   : 
					i = rep.indexOf(",");
					if(i < 0)
						i = rep.indexOf("}");
					if(i>0)
						login_result = Integer.parseInt(rep.substring(0,i));
					if(login_result != 3)
						return login_result;
					break;
				case STR_SCENEDATA: 
					rep = getSceneData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				case STR_EADATA   : 
					rep = getEaData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				default:
					return -1;
				}
				if(sRep[i] == ',')
				{
					rep = rep.substring(i+1);
					sRep=rep.getBytes();				
				}				
			}
			
		}
		return 0;
		
	}
	public static int responseGetMachines(String rep)
	{
		if(rep == null)return -1;
		
		byte[] sRep = rep.getBytes();
		int i;
		int login_result = 0;
		
		if(sRep[0] == '{')
		{
			rep = rep.substring(1);
			sRep = rep.getBytes();
			while(sRep[0]!='}'&&sRep.length!=0)
			{
				i = getString(sRep);
				if(i<=0)return -1;
				
				String cmd = rep.substring(1,i);
				if(sRep[i+1]!=':')return -1;
				rep = rep.substring(i+2);
				sRep = rep.getBytes();
				int index = getStringIndex(cmd);
				i = 0;
				switch(index){
				case STR_RESULT   : 
					i = rep.indexOf(",");
					if(i < 0)
						i = rep.indexOf("}");
					if(i>0)
						login_result = Integer.parseInt(rep.substring(0,i));
					if(login_result != 3)
						return login_result;
					break;
				case STR_EADATA   : 
					rep = getEaData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				default:
					return -1;
				}
				if(sRep[i] == ',')
				{
					rep = rep.substring(i+1);
					sRep=rep.getBytes();				
				}
			}
			
		}
		return 0;
		
	}
	public static int responseGetSceneEa(String rep)
	{
		if(rep == null)return -1;
		
		byte[] sRep = rep.getBytes();
		int i;
		int login_result = 0;
		
		if(sRep[0] == '{')
		{
			rep = rep.substring(1);
			sRep = rep.getBytes();
			while(sRep[0]!='}'&&sRep.length!=0)
			{
				i = getString(sRep);
				if(i<=0)return -1;
				
				String cmd = rep.substring(1,i);
				if(sRep[i+1]!=':')return -1;
				rep = rep.substring(i+2);
				sRep = rep.getBytes();
				int index = getStringIndex(cmd);
				i = 0;
				switch(index){
				case STR_RESULT   : 
					i = rep.indexOf(",");
					if(i < 0)
						i = rep.indexOf("}");
					if(i>0)
						login_result = Integer.parseInt(rep.substring(0,i));
					if(login_result != 3)
						return login_result;
					break;
				case STR_ROOMID   : 
					i = rep.indexOf(",");
					pRoom.roomId = Integer.parseInt(rep.substring(0,i));
					break;
				case STR_ROOMNM  : 
					i = rep.indexOf(",");
					///login_result = Integer.parseInt(rep.substring(0,i-1));
					pRoom.roomNm = rep.substring(1,i-1);
					rep = rep.substring(i);
					sRep=rep.getBytes();
					i = 0;
					break;
				case STR_ROOMIMG  : 
					i = rep.indexOf(",");
					///login_result = Integer.parseInt(rep.substring(0,i-1));
					pRoom.roomImg = rep.substring(1,i-1);
					rep = rep.substring(i);
					sRep=rep.getBytes();
					i = 0;
					break;
				case STR_SCENEDATA: 
					rep = getSceneData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				case STR_EADATA   : 
					rep = getEaData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				case STR_CUSTOMBTNDATA:
					rep = getCustomBtnData(rep,pRoom);
					sRep = rep.getBytes();
					break;
				default:
					return -1;
				}
				if(sRep[i] == ',')
				{
					rep = rep.substring(i+1);
					sRep=rep.getBytes();				
				}
			}
			
		}
		return 0;
		
	}
	
	public static int responseControl(String rep)
	{
		if(rep == null)return -1;
		
		byte[] sRep = rep.getBytes();
		int i;
		int login_result = 0;
		
		if(sRep[0] == '{')
		{
			rep = rep.substring(1);
			sRep = rep.getBytes();
			while(sRep[0]!='}'&&sRep.length!=0)
			{
				i = getString(sRep);
				if(i<=0)return -1;
				
				String cmd = rep.substring(1,i);
				if(sRep[i+1]!=':')return -1;
				rep = rep.substring(i+2);
				sRep = rep.getBytes();
				int index = getStringIndex(cmd);
				i = 0;
				switch(index){
				case STR_RESULT   : 
					i = rep.indexOf("}");
					if(i>0)
						login_result = Integer.parseInt(rep.substring(0,i));
					if(login_result != 2)
						return login_result;
					break;
				default:
					return -1;
				}
				if(sRep[i] == ',')
				{
					rep = rep.substring(i+1);
					sRep=rep.getBytes();				
				}
			}
			
		}
		return 0;
		
	}
	
	public static int getString(byte[] sRep)
	{
		int i=0;
		if(sRep[0] == '"'||sRep[0] == '\'')
		{
			for(i=1;i<sRep.length;i++)
			{
				if(sRep[i] == '"'||sRep[i] == '\'')
					break;
			}
		}
		return i;
	}
	public static int getStringIndex(String str)
	{
		if(str.toUpperCase().compareTo("RESULT"   ) == 0) return STR_RESULT   ;
		if(str.toUpperCase().compareTo("CUSTID"   ) == 0) return STR_CUSTID   ;
		if(str.toUpperCase().compareTo("FAMILYID" ) == 0) return STR_FAMILYID ;
		if(str.toUpperCase().compareTo("ROOMID"   ) == 0) return STR_ROOMID   ;
		if(str.toUpperCase().compareTo("ROOMNM"   ) == 0) return STR_ROOMNM   ;
		if(str.toUpperCase().compareTo("SCENEDATA") == 0) return STR_SCENEDATA;
		if(str.toUpperCase().compareTo("IDKEY"    ) == 0) return STR_IDKEY    ;
		if(str.toUpperCase().compareTo("SCIMG"    ) == 0) return STR_SCIMG    ;
		if(str.toUpperCase().compareTo("SCNM"     ) == 0) return STR_SCNM     ;
		if(str.toUpperCase().compareTo("EADATA"   ) == 0) return STR_EADATA   ;
		if(str.toUpperCase().compareTo("EAIMG"    ) == 0) return STR_EAIMG    ;
		if(str.toUpperCase().compareTo("EANM"     ) == 0) return STR_EANM     ;
		if(str.toUpperCase().compareTo("EASWITCH" ) == 0) return STR_EASWITCH ;
		if(str.toUpperCase().compareTo("TPID"     ) == 0) return STR_TPID     ;
		if(str.toUpperCase().compareTo("ROOMIMG"  ) == 0) return STR_ROOMIMG  ;
		if(str.toUpperCase().compareTo("ROOMDATA" ) == 0) return STR_ROOMDATA ;
		if(str.toUpperCase().compareTo("SCCODE"   ) == 0) return STR_SCCODE   ;
		if(str.toUpperCase().compareTo("CUSTOMBTNDATA"   ) == 0) return STR_CUSTOMBTNDATA   ;
		if(str.toUpperCase().compareTo("CUSTOMCODE"   ) == 0) return STR_CUSTOMCODE   ;
		if(str.toUpperCase().compareTo("CUSTOMNM"   ) == 0) return STR_CUSTOMNM   ;
		if(str.toUpperCase().compareTo("CUSTOMVL"   ) == 0) return STR_CUSTOMVL   ;
	
		return -1;
	}
	public static String getSceneData(String rep, Room room)
	{
		int i=0;
		int j=0;
		byte[] sRep = rep.getBytes();
		Scene[] lScene = new Scene[1024];
		Scene  pScene;
		Scene[] scene;
		int cScene = 0;
		
		if((sRep[0]=='{')&&(sRep[1] == '['))
		{
			i = 2;
			rep = rep.substring(i);
			sRep = rep.getBytes();
			j+=i;
			
			while((sRep[0] != ']') || (sRep[1] != '}'))
			{
				if(sRep[0] != '{')
				{
					break;
				}
				
				rep = rep.substring(1);
				sRep = rep.getBytes();
				j+=1;
				
				pScene = lScene[cScene++] = new Scene();

				while(sRep[0] != '}')
				{
					i = getString(sRep);
					if(i<=0)return null;
					
					String cmd = rep.substring(1,i);
					if(sRep[i+1]!=':')return null;
					
					rep = rep.substring(i+2);
					sRep = rep.getBytes();
					j+=i+2;
					
					int index = getStringIndex(cmd);
					switch(index){
					case STR_IDKEY    : 
						i = rep.indexOf(",");
						pScene.idKey = Integer.parseInt(rep.substring(0,i));
						break;
					case STR_SCIMG    :
						i = rep.indexOf(",");
						pScene.scImg = rep.substring(1,i-1);
						break;
					case STR_SCNM     : 
						i = rep.indexOf("}");
						pScene.scNm = rep.substring(1,i-1);
						i--;
						break;
					case STR_SCCODE   : 
						i = rep.indexOf(",");
						pScene.scCode = rep.substring(1,i-1);
						break;
					default:
						i = rep.indexOf(",");
						break;
					}
					
					rep = rep.substring(i+1);
					sRep=rep.getBytes();
					j+=i+1;
				}
				if(sRep[0] == '}' && sRep[1] ==',')
				{
					rep = rep.substring(2);
					sRep = rep.getBytes();
					j+=2;
				}else if(sRep[0] == '}'){
					rep = rep.substring(1);
					sRep = rep.getBytes();
					j+=1;
				}
			}
		}
		
		if((sRep[0] == ']') && (sRep[1] == '}'))
		{
			rep = rep.substring(2);
			j+=2;
		}

		if(opCode == CMD_HTTP_GETSCENES)
		{
			Global.scenes = new Scene[cScene];
			scene = Global.scenes;
		}else{
			room.sceneList = new Scene[cScene];
			scene = room.sceneList;
		}
		
		for(i=0 ; i< cScene; i++)
			scene[i] = lScene[i];
		
		return rep;
		
	}
	
	public static String getCustomBtnData(String rep, Room room )
	{
		int i=0;
		int j=0;
		byte[] sRep = rep.getBytes();
		CustomBtnData[] lScene = new CustomBtnData[1024];
		CustomBtnData  pScene;
		int cScene = 0;
		CustomBtnData[] scene;
		
		if((sRep[0]=='{')&&(sRep[1] == '['))
		{
			i = 2;
			rep = rep.substring(i);
			sRep = rep.getBytes();
			j+=i;
			
			while((sRep[0] != ']') || (sRep[1] != '}'))
			{
				if(sRep[0] != '{')
				{
					break;
				}
				
				rep = rep.substring(1);
				sRep = rep.getBytes();
				j+=1;
				
				pScene = lScene[cScene++] = new CustomBtnData();

				while(sRep[0] != '}')
				{
					i = getString(sRep);
					if(i<=0)return null;
					
					String cmd = rep.substring(1,i);
					if(sRep[i+1]!=':')return null;
					
					rep = rep.substring(i+2);
					sRep = rep.getBytes();
					j+=i+2;
					
					int index = getStringIndex(cmd);
					switch(index){
					case STR_IDKEY    : 
						i = rep.indexOf("}");
						pScene.idKey = Integer.parseInt(rep.substring(0,i));
						i--;
						break;
					case STR_CUSTOMNM     : 
						i = rep.indexOf(",");
						pScene.customNm = rep.substring(1,i-1);
						break;
					case STR_CUSTOMCODE   : 
						i = rep.indexOf(",");
						pScene.customCode = rep.substring(1,i-1);
						break;
					case STR_CUSTOMVL:
						i = rep.indexOf(",");
						pScene.customVl = rep.substring(1,i-1);
						break;
					default:
						i = rep.indexOf(",");
						break;
					}
					
					rep = rep.substring(i+1);
					sRep=rep.getBytes();
					j+=i+1;
				}
				if(sRep[0] == '}' && sRep[1] ==',')
				{
					rep = rep.substring(2);
					sRep = rep.getBytes();
					j+=2;
				}else if(sRep[0] == '}'){
					rep = rep.substring(1);
					sRep = rep.getBytes();
					j+=1;
				}
			}
		}
		
		if((sRep[0] == ']') && (sRep[1] == '}'))
		{
			rep = rep.substring(2);
			j+=2;
		}

		///if(opCode == CMD_HTTP_GETSCENES)
		///{
		///	Global.scenes = new CustomBtnData[cScene];
		///	scene = Global.scenes;
		///}else{
			room.customBtnDataList = new CustomBtnData[cScene];
			scene = room.customBtnDataList;
		///}
		
		for(i=0 ; i< cScene; i++)
			scene[i] = lScene[i];
		
		return rep;
		
	}
	
	public static String getEaData(String rep, Room room)
	{
		int i=0;
		int j=0;
		byte[] sRep = rep.getBytes();
		Ea[] lEa = new Ea[1024];
		Ea  pEa;
		int cEa = 0;
		Ea[] ea;
		
		if((sRep[0]=='{')&&(sRep[1] == '['))
		{
			i = 2;
			rep = rep.substring(i);
			sRep = rep.getBytes();

			while((sRep[0] != ']') || (sRep[1] != '}'))
			{
				if(sRep[0] != '{')
				{
					break;
				}
				rep = rep.substring(1);
				sRep = rep.getBytes();

				pEa = lEa[cEa++] = new Ea();

				while(sRep[0] != '}')
				{
					i = getString(sRep);
					if(i<=0)return null;
					
					String cmd = rep.substring(1,i);
					if(sRep[i+1]!=':')return null;
					rep = rep.substring(i+2);
					sRep = rep.getBytes();
					int index = getStringIndex(cmd);
					switch(index){
					case STR_IDKEY    : 
						if(opCode == CMD_HTTP_GETMACHINES)
							i = rep.indexOf("}");
						else
							i = rep.indexOf(",");
						if(i>0)
							pEa.idKey = Integer.parseInt(rep.substring(0,i));
						
						if(opCode == CMD_HTTP_GETMACHINES)
							i--;
						break;
					case STR_EAIMG    :
						i = rep.indexOf(",");
						pEa.eaImg = rep.substring(1,i-1);
						break;
					case STR_EANM     : 
						i = rep.indexOf(",");
						pEa.eaNm = rep.substring(1,i-1);
						break;
					case STR_EASWITCH:
						i = rep.indexOf(",");
						if(i>2)
							pEa.eaSwitch = Integer.parseInt(rep.substring(1,i-1));
						break;
					case STR_TPID:
						i = rep.indexOf("}");
						if(i>2)
							pEa.tpId = Integer.parseInt(rep.substring(1,i-1));
						i--;
						break;
					default:
						i = rep.indexOf(",");
						break;
					}
					rep = rep.substring(i+1);
					sRep=rep.getBytes();
				}
				if(sRep[0] == '}' && sRep[1] ==',')
				{
					rep = rep.substring(2);
					sRep = rep.getBytes();
				}else if(sRep[0] == '}'){
					rep = rep.substring(1);
					sRep = rep.getBytes();
					
				}
			}
		}
		
		if((sRep[0] == ']') && (sRep[1] == '}'))
		{
			rep = rep.substring(2);
		}
		
		if(opCode == CMD_HTTP_GETMACHINES)
		{
			Global.eas = new Ea[cEa];
			ea = Global.eas;
		}else{
			room.eaList = new Ea[cEa];
			ea = room.eaList;
		}
		
		for(i=0;i<cEa;i++)
			ea[i] = lEa[i];
		
		return rep;
		
	}	
	public static String getRoomData(String rep, Room[] room)
	{
		int i=0;
		int j=0;
		byte[] sRep = rep.getBytes();
		Room[] lRoom = new Room[1024];
		Room  pRoom;
		int cRoom = 0;
		
		if((sRep[0]=='{')&&(sRep[1] == '['))
		{
			i = 2;
			rep = rep.substring(i);
			sRep = rep.getBytes();

			while((sRep[0] != ']') || (sRep[1] != '}'))
			{
				if(sRep[0] != '{')
				{
					break;
				}
				rep = rep.substring(1);
				sRep = rep.getBytes();

				pRoom = lRoom[cRoom++] = new Room();

				while(sRep[0] != '}')
				{
					i = getString(sRep);
					if(i<=0)return null;
					
					String cmd = rep.substring(1,i);
					if(sRep[i+1]!=':')return null;
					rep = rep.substring(i+2);
					sRep = rep.getBytes();
					int index = getStringIndex(cmd);
					switch(index){
					case STR_IDKEY    : 
						i = rep.indexOf(",");
						if(i>0)
							pRoom.roomId = Integer.parseInt(rep.substring(0,i));
						break;
					case STR_ROOMIMG    :
						i = rep.indexOf(",");
						pRoom.roomImg = rep.substring(1,i-1);
						break;
					case STR_ROOMNM     : 
						i = rep.indexOf("}");
						pRoom.roomNm = rep.substring(1,i-1);
						i--;
						break;
					default:
						i = rep.indexOf(",");
						break;
					}
					rep = rep.substring(i+1);
					sRep=rep.getBytes();
				}
				if(sRep[0] == '}' && sRep[1] ==',')
				{
					rep = rep.substring(2);
					sRep = rep.getBytes();
				}else if(sRep[0] == '}'){
					rep = rep.substring(1);
					sRep = rep.getBytes();
					
				}
			}
		}
		
		if((sRep[0] == ']') && (sRep[1] == '}'))
		{
			rep = rep.substring(2);
		}
		
		Global.rooms = new Room[cRoom];
		for(i=0;i<cRoom;i++)
			Global.rooms[i] = lRoom[i];
		
		return rep;
		
	}
	
	public static int ControlScene(String cmd)
	{
		String action = "getbutton";
		
		String httpUrl = target+action+"?custId="+Global.custId
				+"&roomId="+Global.room.roomId
				+"&familyId="+Global.familyId
				+"&comTp=1"
				+"&comCd="+cmd;
		
		ControlThread.sendCommand(CMD_HTTP_CONTROL, httpUrl);
		return 0;
	}
	public static int ControlEa(int eaId, String cmd, String button)
	{
		MainActivity.CloseSlidingDrawer();
		String action = "getbutton";
		
		String httpUrl = target+action+"?custId="+Global.custId
				+"&roomId="+Global.room.roomId
				+"&familyId="+Global.familyId
				+"&comTp=4"
				+"&eaId="+eaId
				+"&comCd="+cmd
				+"&cmdVl="+button;
		
		ControlThread.sendCommand(CMD_HTTP_CONTROL, httpUrl);
		return 0;
	}
	public static int ControlOnOff(int eaId, String cmd)
	{
		String action = "getbutton";
		
		String httpUrl = target+action+"?custId="+Global.custId
				+"&roomId="+Global.room.roomId
				+"&familyId="+Global.familyId
				+"&comTp=2"
				+"&eaId="+eaId
				+"&eaSwitch="+cmd;
		
		ControlThread.sendCommand(CMD_HTTP_CONTROL, httpUrl);
		return 0;
	}
	
	public static String getStringFromId(int id)
	{
		switch(id)
		{
		case R.id.airBtnDel :return"airBtnDel";
		case R.id.airBtnFeng:return"airBtnFeng";
		case R.id.airBtnMode:return"airBtnMode";
		case R.id.airBtnPlus:return"airBtnPlus";
		case R.id.airBtnPower :return"airBtnPower";
		case R.id.avBtnPower:return"avBtnPower";
		case R.id.avBtnVolumeDown :return"avBtnVolumeDown";
		case R.id.avBtnVolumeUp :return"avBtnVolumeUp";
		case R.id.avTextPower :return"avTextPower";
		case R.id.avTextVolume:return"avTextVolume";
		case R.id.boxBtnPower :return"boxBtnPower";
		case R.id.btn_operate :return"btn_operate";
		case R.id.btn_room:return"btn_room";
		case R.id.btn_set :return"btn_set";
		case R.id.btn_speak :return"btn_speak";
		case R.id.btn_switch:return"btn_switch";
		case R.id.control_layout:return"control_layout";
		case R.id.dianshi_0 :return"dianshi_0";
		case R.id.dianshi_1 :return"dianshi_1";
		case R.id.dianshi_2 :return"dianshi_2";
		case R.id.dianshi_3 :return"dianshi_3";
		case R.id.dianshi_4 :return"dianshi_4";
		case R.id.dianshi_5 :return"dianshi_5";
		case R.id.dianshi_6 :return"dianshi_6";
		case R.id.dianshi_7 :return"dianshi_7";
		case R.id.dianshi_8 :return"dianshi_8";
		case R.id.dianshi_9 :return"dianshi_9";
		case R.id.dianshi_back:return"dianshi_back";
		case R.id.dianshi_clear :return"dianshi_clear";
		case R.id.dianshi_exit:return"dianshi_exit";
		case R.id.dianshi_list:return"dianshi_list";
		case R.id.dianshi_menu:return"dianshi_menu";
		case R.id.dianshi_switch:return"dianshi_switch";
		case R.id.dvdBtnNext:return"dvdBtnNext";
		case R.id.dvdBtnPause :return"dvdBtnPause";
		case R.id.dvdBtnPower :return"dvdBtnPower";
		case R.id.dvdBtnPrevios :return"dvdBtnPrevios";
		case R.id.dvdBtnQuickNext :return"dvdBtnQuickNext";
		case R.id.dvdBtnQuickPrevios:return"dvdBtnQuickPrevios";
		case R.id.dvdBtnStop:return"dvdBtnStop";
		case R.id.dvdBtnVolumeDown:return"dvdBtnVolumeDown";
		case R.id.dvdBtnVolumeUp:return"dvdBtnVolumeUp";
		case R.id.dvdTextPower:return"dvdTextPower";
		case R.id.dvdTextVolume :return"dvdTextVolume";
		case R.id.editGW:return"editGW";
		case R.id.editPassword:return"editPassword";
		case R.id.editUsername:return"editUsername";
		case R.id.fenBtnMode:return"fenBtnMode";
		case R.id.fenBtnMove:return"fenBtnMove";
		case R.id.fenBtnPower :return"fenBtnPower";
		case R.id.fenBtnSpeed :return"fenBtnSpeed";
		case R.id.fenBtnTime:return"fenBtnTime";
		case R.id.gridimage :return"gridimage";
		case R.id.gridtext:return"gridtext";
		case R.id.pager :return"pager";
		case R.id.projects:return"projects";
		case R.id.roomImg :return"roomImg";
		case R.id.roomName:return"roomName";
		case R.id.showRoomView:return"showRoomView";
		case R.id.spinnerDianShi:return"spinnerDianShi";
		case R.id.spinnerFengShan :return"spinnerFengShan";
		case R.id.spinnerKongTiao :return"spinnerKongTiao";
		case R.id.spinnerYinxiang :return"spinnerYinxiang";
		case R.id.textGW:return"textGW";
		case R.id.textPassword:return"textPassword";
		case R.id.textUsername:return"textUsername";
		case R.id.lightSwitch :return"toggleButton1";
		case R.id.tvBtnBack :return"tvBtnBack";
		case R.id.tvBtnChannelNext:return"tvBtnChannelNext";
		case R.id.tvBtnCurrent:return"tvBtnCurrent";
		case R.id.tvBtnMute :return"tvBtnMute";
		case R.id.tvBtnNextPage :return"tvBtnNextPage";
		case R.id.tvBtnOK :return"tvBtnOK";
		case R.id.tvBtnPower:return"tvBtnPower";
		case R.id.tvBtnSoundDown:return"tvBtnSoundDown";
		case R.id.tvBtnSoundUp:return"tvBtnSoundUp";
		case R.id.tvBtnStop :return"tvBtnStop";
		case R.id.tvBtnUpPage :return"tvBtnUpPage";
		case R.id.tvChannelUp :return"tvChannelUp";
		
		}
		return null;
	}
	
	public Bitmap getRoomImg(String img)
	{
		if(img == null || img.length() == 0)
			return null;
		String url = target+RES_FAMILYIMAGES + img;
		return getBitmap(url);
	}

	public Bitmap getSceneImg(String img)
	{
		if(img == null || img.length() == 0)
			return null;		
		String url = target+RES_SCENEIMAGES + img;
		return getBitmap(url);
	}
	public Bitmap getEaImg(String img)
	{
		if(img == null || img.length() == 0)
			return null;
		String url = target+RES_EAIMAGES + img;
		return getBitmap(url);
	}

	
	public Bitmap getBitmap(String url) {   
		URL myFileUrl = null;   
		Bitmap bitmap = null;   
		
		try {   
			myFileUrl = new URL(url);   
		} catch (MalformedURLException e) {   
			e.printStackTrace();   
		}   
		try {   
			HttpURLConnection conn = (HttpURLConnection) 
			myFileUrl.openConnection();   
			conn.setDoInput(true);   
			conn.connect();   
			InputStream is = conn.getInputStream();   
			bitmap = BitmapFactory.decodeStream(is);   
			is.close();   
		} catch (IOException e) {   
			e.printStackTrace();   
		}
		
		return bitmap;   
	}
	public static Room getRoomData(int roomId)
	{
		String action = "getsceneea";
		String url = target+action+"?roomId="+roomId;
		
		pRoom = new Room();
		int ret = responseGetSceneEa(getResponse(url));
		
		return pRoom;
	}
	
	public static void processUpdateRoomNM(int roomId, String Name)
	{
		String url = target+UPDATE_ROOMNM+"?roomId="+roomId+"&roomNm="+Name;
		
		String ret = getResponse(url);
		return;
	}

	public static void processUpdateSceneNM(int sceneId, String Name)
	{
		String url = target+UPDATE_SCENENM+"?sceneId="+sceneId+"&sceneNm="+Name;
		
		String ret = getResponse(url);
		return;
	}

	public static void processUpdateEaNM(int eaId, String Name)
	{
		String url = target+UPDATE_EANM+"?eaId="+eaId+"&eaNm="+Name;
		
		String ret = getResponse(url);
		return;
	}
	
	public static void processUpdateRoomImg(int roomId, Bitmap bp)
	{
		String url = target+UPDATE_ROOMIMG+"?roomId="+roomId;
		uploadFile(url, "upFile", bp);
	}

	public static void processUpdateSceneImg(int sceneId, Bitmap bp)
	{
		String url = target+UPDATE_SCENEIMG+"?sceneId="+sceneId;
		uploadFile(url, "upFile", bp);
	}
	public static void processUpdateEaImg(int eaId, Bitmap bp)
	{
		String url = target+UPDATE_EAIMG+"?eaId="+eaId;
		uploadFile(url, "upFile", bp);
	}
	public static void uploadFile(String actionUrl, String fileId, Bitmap bp)
    {
      String end ="\r\n";
      String twoHyphens ="--";
      String boundary ="*****";
      try
      {
        URL url =new URL(actionUrl);
        HttpURLConnection con=(HttpURLConnection)url.openConnection();
        /* ÔÊÐíInput¡¢Output£¬²»Ê¹ÓÃCache */
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        /* ÉèÖÃ´«ËÍµÄmethod=POST */
        con.setRequestMethod("POST");
        /* setRequestProperty */
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type",
                           "multipart/form-data;boundary="+boundary);
        /* ÉèÖÃDataOutputStream */
        DataOutputStream ds =
          new DataOutputStream(con.getOutputStream());
        ds.writeBytes(twoHyphens + boundary + end);
        ds.writeBytes("Content-Disposition: form-data; "+
                      "name=\""+fileId+"\";filename=\""+
                      fileId +".jpg\""+ end);
        ds.writeBytes(end);  
        if(false){
	        /* È¡µÃÎÄ¼þµÄFileInputStream */
	        FileInputStream fStream =new FileInputStream(fileId);
	        /* ÉèÖÃÃ¿´ÎÐ´Èë1024bytes */
	        int bufferSize =1024;
	        byte[] buffer =new byte[bufferSize];
	        int length =-1;
	        /* ´ÓÎÄ¼þ¶ÁÈ¡Êý¾ÝÖÁ»º³åÇø */
	        while((length = fStream.read(buffer)) !=-1)
	        {
	          /* ½«×ÊÁÏÐ´ÈëDataOutputStreamÖÐ */
	          ds.write(buffer, 0, length);
	        }
	        fStream.close();
        }
        byte[] buff = Bitmap2Bytes(bp);
        ds.write(buff, 0, buff.length);
        
        ds.writeBytes(end);
        ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
        /* close streams */
        
        ds.flush();
        /* È¡µÃResponseÄÚÈÝ */
        InputStream is = con.getInputStream();
        int ch;
        StringBuffer b =new StringBuffer();
        while( ( ch = is.read() ) !=-1 )
        {
          b.append( (char)ch );
        }
        /* ½«ResponseÏÔÊ¾ÓÚDialog */
        
        /* ¹Ø±ÕDataOutputStream */
        ds.close();
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    public static byte[] Bitmap2Bytes(Bitmap bm){   
        ByteArrayOutputStream baos = new ByteArrayOutputStream();     
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);     
        return baos.toByteArray();   
    }
    public static int processUpdateRoomSeq(int ids[], int seq[])
    {
    	int count = ids.length;
    	int i;
    	String url = target+UPDATE_ROOMSEQ;
    	
    	for(i=0;i<count;i++)
    	{
    		if(i==0)
    			url+="?roomId="+ids[i]+"&seq="+seq[i];
    		else
    			url+="&roomId="+ids[i]+"&seq="+seq[i];
    	}
    	
    	String ret = getResponse(url);
    	
    	return 0;
    }

    public static int processUpdateSceneSeq(int ids[], int seq[])
    {
    	int count = ids.length;
    	int i;
    	String url = target+UPDATE_SCENESEQ;
    	
    	for(i=0;i<count;i++)
    	{
    		if(i==0)
    			url+="?sceneId="+ids[i]+"&seq="+seq[i];
    		else
    			url+="&sceneId="+ids[i]+"&seq="+seq[i];
    	}
    	
    	String ret = getResponse(url);
    	
    	return 0;
    }
    public static int processUpdateEaSeq(int ids[], int seq[])
    {
    	int count = ids.length;
    	int i;
    	String url = target+UPDATE_EASEQ;
    	
    	for(i=0;i<count;i++)
    	{
    		if(i==0)
    			url+="?eaId="+ids[i]+"&seq="+seq[i];
    		else
    			url+="&eaId="+ids[i]+"&seq="+seq[i];
    	}
    	
    	String ret = getResponse(url);
    	
    	return 0;
    }
	
    
    public static void updateRoomNM(int roomId, String Name)
	{
		param p = new param();
		p.roomId = roomId;
		p.Name = Name;
		
		sendCommand(CMD_HTTP_UPDATEROOMNM,p);
	}

	public static void updateSceneNM(int sceneId, String Name)
	{
		param p = new param();
		p.sceneId = sceneId;
		p.Name = Name;
		
		sendCommand(CMD_HTTP_UPDATESCENENM,p);
	}

	public static void updateEaNM(int eaId, String Name)
	{
		param p = new param();
		p.eaId = eaId;
		p.Name = Name;
		
		sendCommand(CMD_HTTP_UPDATEEANM,p);
	}
	
	public static void updateRoomImg(int roomId, Bitmap bp)
	{
		param p = new param();
		p.roomId = roomId;
		p.bp = bp;
		
		sendCommand(CMD_HTTP_UPDATEROOMIMG,p);
	}

	public static void updateSceneImg(int sceneId, Bitmap bp)
	{
		param p = new param();
		p.sceneId = sceneId;
		p.bp = bp;
		
		sendCommand(CMD_HTTP_UPDATESCENEIMG,p);

	}
	public static void updateEaImg(int eaId, Bitmap bp)
	{
		param p = new param();
		p.eaId = eaId;
		p.bp = bp;
		
		sendCommand(CMD_HTTP_UPDATEEAIMG,p);

	}
	public static void updateRoomSeq(int ids[], int seq[])
    {
		param p = new param();
		p.ids = ids;
		p.seq = seq;
		
		sendCommand(CMD_HTTP_UPDATEROOMSEQ,p);
    	

    }

    public static void updateSceneSeq(int ids[], int seq[])
    {
		param p = new param();

		p.ids = ids;
		p.seq = seq;
		
		sendCommand(CMD_HTTP_UPDATESCENESEQ,p);

    }
    public static void updateEaSeq(int ids[], int seq[])
    {
		param p = new param();
		p.ids = ids;
		p.seq = seq;
		
		sendCommand(CMD_HTTP_UPDATEEASEQ,p);

    }    

}
class param{
    int roomId;
    int sceneId;
    int eaId;
    int ids[];
    int seq[];
    String Name;
    Bitmap bp;
}

