/*
 Copyright (c) 2007-2009 WebAppShowCase DBA Appcloem (http://www.appcloem.com). All rights reserved.
Author: Gabriel Wong
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL WebAppShowCase
OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


package tools.util;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import java.sql.*;

import java.text.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.naming.*;


/**
 * This class is used for common methods.
 **/
//SharedMethods
public class SharedMethods
{
	/*public static String generateId(){
	 (System.currentTimeMillis()/hashCode()))

	}*/
	public static String smtpUser = null;
	public static String smtpPassword = null;








 public static void runGC ()
    {
        // It helps to call Runtime.gc()
        // using several method calls:
        LogMgr.debug(usedMemory() + " BEGIN GC " + Long.MAX_VALUE);
        try{
        
        for (int r = 0; r < 4; ++ r) _runGC ();
        }catch (Exception e){
        	LogMgr.err(" runGC " + e.toString());
        }
         LogMgr.debug(usedMemory() + " END GC " + Long.MAX_VALUE);

    }
    private static void _runGC () throws Exception
    {
        long usedMem1 = usedMemory (), usedMem2 = Long.MAX_VALUE;
        for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++ i)
        {
            s_runtime.runFinalization ();
            s_runtime.gc ();
            Thread.currentThread ().yield ();
            
            usedMem2 = usedMem1;
            usedMem1 = usedMemory ();
        }
    }
    public static long usedMemory ()
    {
        return s_runtime.totalMemory () - s_runtime.freeMemory ();
    }
    
    private static final Runtime s_runtime = Runtime.getRuntime ();









public		static int getNetBase(String ip){
		int i = ip.lastIndexOf(".");
		return Integer.parseInt(ip.substring(i + 1,ip.length()));

	}

public	static String getNetRoot(String ip){
		int i = ip.lastIndexOf(".");
		return ip.substring(0,i + 1);

	}




		public static String getLinuxVendor(){

		//os.version
		if (!linux()) {
			return null;
		}




		if (new File ("/etc/debian_version").exists())
			return "debian";
		if (new File ("/etc/whitebox-release").exists())
			return "whitebox";
		if (new File ("/etc/fedora-release").exists())
			return "fedora";
		if (new File ("/etc//SuSE-release").exists())
			return "suse";

		if (new File ("/etc/redhat-release").exists()){
			String fs = null;
			try{

			fs = FileUtil.getStringFromFile("/etc/redhat-release");
			if (fs.toLowerCase().indexOf("centos") > -1)
				return "centos";
			}catch (Throwable e){
				//tools.util.LogMgr.err("");
			}

			return "redhat";
		}


		return null;
		}

				public static String getOSArch(){

		//os.version
		if (!linux()) return "";
		String os = System.getProperty("os.arch");
		return os;
		}


	public  static String getMacAddress() throws IOException {
		String os = System.getProperty("os.name");

		try {
			if(os.startsWith("Windows")) {
				return windowsParseMacAddress(windowsRunIpConfigCommand());
			} else if(os.startsWith("Linux")) {
				return linuxParseMacAddress(linuxRunIfConfigCommand());
			} else {
				throw new IOException("unknown operating system: " + os);
			}
		} catch(Exception ex) {
			tools.util.LogMgr.err("getMacAddress " + ex.toString());
			return "no-mac-address-found";
			//ex.printStackTrace();
			//throw new IOException(ex.getMessage());
		}
	}


	/*
	 * Linux stuff
	 */
	 	private final static String mymacparse(String ipConfigResponse){
	 		try{
	 			int i =  ipConfigResponse.indexOf("HWaddr");
	 			if (i > -1)
	 			{
	 				ipConfigResponse = ipConfigResponse.substring(i + "HWaddr".length(),ipConfigResponse.length());
	 				i =  ipConfigResponse.indexOf("inet");
	 	 			if (i > -1)
	 			{
	 				ipConfigResponse = ipConfigResponse.substring(0,i).trim();
	 				return ipConfigResponse;


	 			}

	 			}
	 			// 00:07:E9:AD:5D:78
	 		}catch (Throwable e){
	 			tools.util.LogMgr.err("SharedMethods.mymacparse " + e.toString());
	 		}
	 		return "00:00:00:00";
	 	}


	private final static String linuxParseMacAddress(String ipConfigResponse) throws Exception {
		String localHost = null;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch(java.net.UnknownHostException ex) {
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}

		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;

		while(tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			boolean containsLocalHost = line.indexOf(localHost) >= 0;

			// see if line contains IP address
			if(containsLocalHost && lastMacAddress != null) {
				return lastMacAddress;
			}

			// see if line contains MAC address
			int macAddressPosition = line.indexOf("HWaddr");
			if(macAddressPosition <= 0) continue;

			String macAddressCandidate = line.substring(macAddressPosition + 6).trim();
			if(linuxIsMacAddress(macAddressCandidate)) {
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		/*Exception ex = new Exception
			("cannot read MAC address for " + localHost + " from [" + ipConfigResponse + "]");
		ex.printStackTrace();
		throw ex;*/
		return mymacparse(ipConfigResponse);
	}


	private final static boolean linuxIsMacAddress(String macAddressCandidate) {
		// TODO: use a smart regular expression
		if(macAddressCandidate.length() != 17) return false;
		return true;
	}


	private final static String linuxRunIfConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("ifconfig");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

		StringBuffer buffer= new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1) break;
			buffer.append((char)c);
		}
		String outputText = buffer.toString();

		stdoutStream.close();

		return outputText;
	}



	/*
	 * Windows stuff
	 */
	private final static String windowsParseMacAddress(String ipConfigResponse) throws Exception {
		String localHost = null;
		try {
			localHost = InetAddress.getLocalHost().getHostAddress();
		} catch(java.net.UnknownHostException ex) {
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}

		StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
		String lastMacAddress = null;

		while(tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();

			// see if line contains IP address
			if(line.endsWith(localHost) && lastMacAddress != null) {
				return lastMacAddress;
			}

			// see if line contains MAC address
			int macAddressPosition = line.indexOf(":");
			if(macAddressPosition <= 0) continue;

			String macAddressCandidate = line.substring(macAddressPosition + 1).trim();
			if(windowsIsMacAddress(macAddressCandidate)) {
				lastMacAddress = macAddressCandidate;
				continue;
			}
		}

		//Exception ex = new Exception("cannot read MAC address from [" + ipConfigResponse + "]");
		//ex.printStackTrace();
		//throw ex;
		return mymacparse(ipConfigResponse);
	}


	private final static boolean windowsIsMacAddress(String macAddressCandidate) {
		// TODO: use a smart regular expression
		if(macAddressCandidate.length() != 17) return false;

		return true;
	}


	private final static String windowsRunIpConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("ipconfig /all");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

		StringBuffer buffer= new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1) break;
			buffer.append((char)c);
		}
		String outputText = buffer.toString();

		stdoutStream.close();

		return outputText;
	}

























	public static String generateId(Object obj){
	 String id = String.valueOf(System.currentTimeMillis());
	 id = id.substring(id.length() - 6,id.length());
	 /*if (obj != null)
	 	id =  id + obj.hashCode();
		id = StringUtil.replaceSubstring(id,"-","");*/
		return id;
	}

	public static Properties toProperties(Hashtable p){
		Properties properties = new Properties();
		Enumeration en = p.keys();
		while (en.hasMoreElements())
		{
			String k = (String)en.nextElement();
			properties.put(k,p.get(k));

		}
		return properties;
	}

	public static String cpus(){
	return "1";
}







	public static boolean linux(){

			String os = System.getProperty("os.name");
		if (os.toLowerCase().indexOf("linux") > -1)
			return true;

		return false;
	}



	public static boolean vista(){

			String os = System.getProperty("os.name");
		if (os.equals("Windows Vista") || os.equals("Windows Server 2008") || os.equals("Windows 2008"))
			return true;

		return false;
	}

		public static boolean windows(){

			String os = System.getProperty("os.name");
		if (os.toLowerCase().indexOf("windows") > -1)
			return true;

		return false;
	}
	public static String to2Digit(String t)
	{
		int i = t.indexOf(".");
		if (i < 0)
			return t + ".00";
		if (t.substring(i+ 1,t.length()).length() < 2)
			return t + "0";
		if (t.substring(i+ 1,t.length()).length() > 2)
			return t.substring(0,i + 3);
		//return "0.00";
		return t;
	}

	public static int[] toIntArray(String src){
		int l = src.length();
		int[] i = new int[l];
		for (int ct = 0;ct < l;ct++)
			i[ct] = Integer.parseInt(src.substring(ct,ct+1));
		return i;
	}

		    public static String setLink(String wit)
    {
    	 wit = replaceSubstring(wit,"%","THIS_IS_A_PERCENT");

       wit = replaceSubstring(wit," ","%20");
       wit = replaceSubstring(wit,"#","%23");
    	 wit = replaceSubstring(wit,"&","%26");
    	 wit = replaceSubstring(wit,"=","%3D");
    	 wit = replaceSubstring(wit,"\r","%0D");
    	 wit = replaceSubstring(wit,"\n","%0A");
    	 wit = replaceSubstring(wit,"/","%2F");
       return wit;
    }


	private final static  Hashtable m_months = new Hashtable(12);
	static
	{
		m_months.put("Jan","01");
		m_months.put("Feb","02");
		m_months.put("Mar","03");
		m_months.put("Apr","04");
		m_months.put("May","05");
		m_months.put("Jun","06");
		m_months.put("Jul","07");
		m_months.put("Aug","08");
		m_months.put("Sep","09");
		m_months.put("Oct","10");
		m_months.put("Nov","11");
		m_months.put("Dec","12");
	}

	public static int occurrences(String w,String it)
	{
		int ct = 0;
		for (int i = w.indexOf(it); i > -1 ;i = w.indexOf(it))
		{
			ct++;
			w = replaceFirstSubstring(w,it,"$");
		}
		return ct;
	}

	public static void store(Object obj,String filename)throws IOException{
		File f = new File(filename);
		if (!f.exists())f.createNewFile();
		FileOutputStream fout = new FileOutputStream(f);

		try{
			ObjectOutputStream oout = new ObjectOutputStream(fout);
			oout.writeObject(obj);
		}finally{
			fout.close();
			}
	}

	public static Object retrieve(String filename)throws IOException,ClassNotFoundException{
		Object obj = null;
		File f = new File(filename);
		if (!f.exists())return null;
		FileInputStream fout = new FileInputStream(f);

		try{
			ObjectInputStream oout = new ObjectInputStream(fout);
			obj = oout.readObject();
		}finally{
			fout.close();
			}
			return obj;
	}


	public static void mail2(String smtp,String to,String tn,String sub,String from,String name,String txt)throws AddressException,MessagingException,UnsupportedEncodingException{
	Properties mprops = new Properties();
	mprops.put("mail.smtp.host", smtp);
	Session session = Session.getDefaultInstance(mprops, null);
	Message msg = new MimeMessage(session);
	msg.setSentDate(new java.util.Date());
	InternetAddress toAddrs = null;
		//toAddrs = new InternetAddress(to);
		//if (tn != null && tn.length() > 0)
		//	toAddrs.setPersonal(tn);
		//msg.addRecipient(Message.RecipientType.TO, toAddrs);
		addTo(to,tn,msg);
		//("mail2b.TO: " + toAddrs);


	msg.setSubject(sub);
	toAddrs = new InternetAddress(from);
	//(from + " name: " + name);
	if (name != null && name.length() > 0)
		toAddrs.setPersonal(name);
	msg.setFrom(toAddrs);
		msg.setText(txt);
	Transport.send(msg);
	}



	public static void sendmail(String to,String sub,String from,String txt,String cc)throws AddressException,MessagingException,UnsupportedEncodingException{
	sendmail(to,sub,from,txt,cc,null);
}
	public static void sendmail(String to,String sub,String from,String txt,String cc,String sn)throws AddressException,MessagingException,UnsupportedEncodingException{

			SendMailEx2 sc = new SendMailEx2(to,sub,from,txt,cc,sn);
			sc.start();
			for (int ct = 0;ct <40;ct++)
			if (!sc.fin)
			{
				try{
				Thread.currentThread().sleep(1000);
			}catch(Throwable i){}
			}
			else
				break;
			if (!sc.fin)
			{
				sc.interrupt();
				tools.util.LogMgr.err(from + " Sharedmethods.sendmail TIMEOUT " + to);
				throw new MessagingException("SENDMAIL TIMEOUT");
			}

}

   	public static boolean imapAuth(String id,String password)throws AddressException,MessagingException,UnsupportedEncodingException{

		String host = System.getProperty("mail.imap.host");
		if (host == null)host = "localhost";
		return imapAuth(id,password,host);

}

    /*	public static boolean imapAuth(String id,String password,String host)throws AddressException,MessagingException,UnsupportedEncodingException{

     URLName url;
     String protocol = "imap";
     String mbox = "INBOX";


            url = new URLName(protocol, host, -1, mbox,
                          id, password);


	Properties mprops = new Properties();
	mprops.put("mail.imap.host", host);
	Session session = Session.getDefaultInstance(mprops, null);

        Store store = session.getStore(url);
        store.connect();
		store.close();
		return true;

}*/

	public static boolean imapAuth(String id,String password,String host)throws AddressException,MessagingException,UnsupportedEncodingException{
	return 	mailAuth("imap",id,password,host);
}

	public static boolean pop3Auth(String id,String password,String host)throws AddressException,MessagingException,UnsupportedEncodingException{
	return 	mailAuth("pop3",id,password,host);
}
   	public static boolean mailAuth(String protocol, String id,String password,String host)throws AddressException,MessagingException,UnsupportedEncodingException{

     URLName url;
     //String protocol = "imap";
     String mbox = "INBOX";


            url = new URLName(protocol, host, -1, mbox,
                          id, password);


	Properties mprops = new Properties();
	mprops.put("mail.imap.host", host);
	Session session = Session.getDefaultInstance(mprops, null);

        Store store = session.getStore(url);
        store.connect();
		store.close();
		return true;

}


/*	public static boolean imapAuth(String id,String password)throws AddressException,MessagingException,UnsupportedEncodingException{

		String host = System.getProperty("mail.imap.host");
		if (host == null)host = "localhost";

	Properties mprops = new Properties();
	mprops.put("mail.imap.host", host);
	Session session = Session.getDefaultInstance(mprops, null);

//	if (session.getProperty("mail.smtp.password") != null && session.getProperty("") != null && session.getProperty("mail.smtp.user") != null)
	    	//	{
    		//mprops.put("","true");
    		////("AUTH SMTPT TO " + session.getProperty("mail.smtp.password"));
    		Transport tr = session.getTransport("imap");
			tr.connect(session.getProperty("mail.imap.host"), id, password);
			tr.close();
			return true;

}*/

	public static void sendmail(String to,String sub,String from,String txt,Session session)throws AddressException,MessagingException,UnsupportedEncodingException{
	sendmail(to,sub,from,txt,session,null);
}

	public static void sendmail(String to,String sub,String from,String txt,Session session,String cc)throws AddressException,MessagingException,UnsupportedEncodingException{
	sendmail(to,sub,from,txt,session,cc,null);
}
	public static void sendmail(String to,String sub,String from,String txt,Session session,String cc,String sn)throws AddressException,MessagingException,UnsupportedEncodingException{

	Message msg = new MimeMessage(session);
		msg.setSentDate(new java.util.Date());
		InternetAddress toAddrs = null;
		//toAddrs = new InternetAddress(to);
		//msg.addRecipient(Message.RecipientType.TO, toAddrs);
		addTo(to,msg);
	//InternetAddress ccAddrs = new InternetAddress(from);
	//			msg.addRecipient(Message.RecipientType.CC, ccAddrs);
		if (cc != null && cc.length() > 1)
	{
		//toAddrs = new InternetAddress(cc);
		//msg.addRecipient(Message.RecipientType.CC, toAddrs);
		addCc(cc,msg);
	}
	msg.setSubject(sub);
	toAddrs = new InternetAddress(from);
		if (sn != null && sn.length() > 0)
		toAddrs.setPersonal(sn);
	msg.setFrom(toAddrs);
		msg.setText(txt);
//	if (System.getProperty("mail.smtp.password") != null && System.getProperty("") != null && System.getProperty("mail.smtp.user") != null)

	if (session.getProperty("mail.smtp.password") != null && session.getProperty("mail.smtp.auth") != null && session.getProperty("mail.smtp.user") != null)
	    		{
    		//mprops.put("mail.smtp.auth","true");
    		//("AUTH SMTPT TO " + session.getProperty("mail.smtp.password"));
    		Transport tr = session.getTransport("smtp");
			tr.connect(session.getProperty("mail.smtp.host"), session.getProperty("mail.smtp.user"), session.getProperty("mail.smtp.password"));
			msg.saveChanges();	// don't forget this
			tr.sendMessage(msg, msg.getAllRecipients());
		}
		else
		Transport.send(msg);

}

	static void addTo(String tos,Message msg)throws AddressException,MessagingException{

		addTo(tos,null,msg);

	}

	static void addTo(String tos,String tn,Message msg)throws AddressException,MessagingException{

		addRec(tos,tn,msg,Message.RecipientType.TO);

	}


		static void addRec(String tos,String tns,Message msg,Message.RecipientType rect)throws AddressException,MessagingException{

		InternetAddress toAddrs = null;
		Vector v = toVector(tos,",");
		Vector tnv = new Vector();
		if (StringUtil.isRealString(tns)){
			tnv = 	toVector(tns,",");
		}
		for (int ct = 0;ct < v.size();ct++){
		String to = (String)v.elementAt(ct);
		toAddrs = new InternetAddress(to);
		String tn = null;
		if (tnv.size() >= ct + 1)
			tn = (String)tnv.elementAt(ct);
		else if (tnv.size() > 0)
		tn = (String)tnv.elementAt(0);
		if (tn != null && tn.length() > 0)
		try{
			toAddrs.setPersonal(tn);
		}catch (UnsupportedEncodingException ur){
			tools.util.LogMgr.err("SharedMEthods.addRec " + ur.toString());
		}
		msg.addRecipient(rect, toAddrs);
		}

	}


	static void addCc(String tos,Message msg)throws AddressException,MessagingException{

		addCc(tos,null,msg);

	}

	static void addCc(String tos,String tn,Message msg)throws AddressException,MessagingException{

		addRec(tos,tn,msg,Message.RecipientType.CC);

	}

	public static void sendmail1(String to,String sub,String from,String txt,String cc)throws AddressException,MessagingException,UnsupportedEncodingException{
	sendmail1(to,sub,from,txt,cc,null);
}

	public static void sendmail1(String to,String sub,String from,String txt,String cc,String sn)throws AddressException,MessagingException,UnsupportedEncodingException{
sendmail1(to,sub,from,txt,cc,null);
	}
	public static void sendmail1(String to,String sub,String from,String txt,String cc,String sn,String mo)throws AddressException,MessagingException,UnsupportedEncodingException{

	Session session = null;
	try{
		Context initCtx = new InitialContext();
	Context envCtx = (Context) initCtx.lookup("java:comp/env");
	if (envCtx != null){
	if (mo != null)
	{

		//("TRY SESSION " + mo);
		session = (Session) initCtx.lookup(mo);
	}
		else
	 session = (Session) envCtx.lookup("mail/Session");
	 if (session == null)
	  session = (Session) envCtx.lookup("mail/MailSession");
}
}catch (Throwable e){
		//tools.util.LogMgr.err(mo + " sendmail1.session " + e.toString());
}
	if (session != null){
		tools.util.LogMgr.red("Found Mail Session Object");
		sendmail(to,sub,from,txt,session,cc,sn);
		return;
	}

	Properties mprops = new Properties();
	mprops.put("mail.smtp.host", "localhost");
	//("RT LOOP 12 ");
	session = Session.getDefaultInstance(mprops, null);
	Message msg = new MimeMessage(session);
		msg.setSentDate(new java.util.Date());

	InternetAddress toAddrs = null;
	//	toAddrs = new InternetAddress(to);
	//	msg.addRecipient(Message.RecipientType.TO, toAddrs);
	addTo(to,msg);
	if (cc != null && cc.length() > 1)
	{
		//toAddrs = new InternetAddress(cc);
		//msg.addRecipient(Message.RecipientType.CC, toAddrs);
		addCc(cc,msg);
	}
	msg.setSubject(sub);
	toAddrs = new InternetAddress(from);
	if (sn != null && sn.length() > 0)
		toAddrs.setPersonal(sn);
	msg.setFrom(toAddrs);

		msg.setText(txt);
	if (smtpPassword == null)
	Transport.send(msg);
	    		else{
    		mprops.put("mail.smtp.auth","true");
    		Transport tr = session.getTransport("smtp");
			tr.connect("localhost", smtpUser, smtpPassword);
			msg.saveChanges();	// don't forget this
			tr.sendMessage(msg, msg.getAllRecipients());
		}
	}


	public static void mail2(String smtp,String to,String tn,String sub,String from,String name,String txt,String cc)throws AddressException,MessagingException,UnsupportedEncodingException{
	Properties mprops = new Properties();
	mprops.put("mail.smtp.host", smtp);

		//	Properties props = new Properties();
		//props.put("mail.smtp.host",ElseProperties.getElseProp("smtp.mailhost"));
		//Session msession = Session.getInstance(props, null);



	Session session = Session.getDefaultInstance(mprops, null);
	Message msg = new MimeMessage(session);
		msg.setSentDate(new java.util.Date());

	InternetAddress toAddrs = null;
		//toAddrs = new InternetAddress(to);
		//if (tn != null && tn.length() > 0)
		//	toAddrs.setPersonal(tn);
		//msg.addRecipient(Message.RecipientType.TO, toAddrs);
		addTo(to,tn,msg);
		//("mail2.TO: " + toAddrs);
		//msg.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
		addCc(cc,msg);

	msg.setSubject(sub);
	toAddrs = new InternetAddress(from);
	//(from + " name: " + name);
	if (name != null && name.length() > 0)
		toAddrs.setPersonal(name);
	msg.setFrom(toAddrs);
		msg.setText(txt);

		if (SharedMethods.smtpPassword != null){
		    mprops.put("mail.smtp.auth","true");
    		Transport tr = session.getTransport("smtp");
			tr.connect(smtp, SharedMethods.smtpUser, SharedMethods.smtpPassword);
			msg.saveChanges();	// don't forget this
			tr.sendMessage(msg, msg.getAllRecipients());

	}else
	Transport.send(msg);
	}


	public static void mail(String smtp,Enumeration to,Enumeration bcc,String sub,String from,String name,String txt)throws AddressException,MessagingException,UnsupportedEncodingException{
		mail(smtp,to,bcc,sub,from,name,txt,null);
	}
	public static void mail(String smtp,Enumeration to,Enumeration cc,String sub,String from,String name,String txt,Multipart mp)throws AddressException,MessagingException,UnsupportedEncodingException{
		Properties mprops = new Properties();
		mprops.put("mail.smtp.host", smtp);
		Session session = Session.getDefaultInstance(mprops, null);
		Message msg = new MimeMessage(session);
			msg.setSentDate(new java.util.Date());

		InternetAddress toAddrs = null;
		if (to != null)
		while (to.hasMoreElements())
		{
			toAddrs = new InternetAddress((String)to.nextElement());
			msg.addRecipient(Message.RecipientType.TO, toAddrs);
			//("mail.TO: " + toAddrs);
		}

		if (cc != null)
		while (cc.hasMoreElements())
		{
			toAddrs = new InternetAddress((String)cc.nextElement());
			msg.addRecipient(Message.RecipientType.CC, toAddrs);
			//("CC: " + toAddrs);

		}

		msg.setSubject(sub);
		toAddrs = new InternetAddress(from);
		//(from + " name: " + name);
		if (name != null && name.length() > 0)
			toAddrs.setPersonal(name);
		msg.setFrom(toAddrs);
		if (mp != null)msg.setContent(mp);
		else
			msg.setText(txt);
		Transport.send(msg);



	}



	public static int getAvailablePort (){
	return getAvailablePort("localhost");
}

	public static int getAvailablePort (String host)
	{
		return getAvailablePort(host,1025,59999);
	}

	public static int getAvailablePort (String host,int fr,int tr)

{

	int porti = -1;
	boolean tf = true;
	int ct = 0;

	while (tf)
	try{
		 porti = tools.util.NumberUtil.getRandomIntBetween(fr,tr);
		//port = 			String.valueOf(porti);
		//checkIfListening(host,port);
		java.net.Socket s = new java.net.Socket(host,porti);
		s.close();

		ct++;
		if (ct > 100)
			return -1;

	}catch (Throwable e){
		//if (isPortAvailable(port))
			tf = false;

	}
			return porti;
}


	//public static



	public static String toLocalFile(String f)
	{
		f = replaceSubstring(f,"/",File.separator);
		f = replaceSubstring(f,"\\",File.separator);
		f = replaceSubstring(f,";",File.pathSeparator);
		f = replaceSubstring(f,":",File.pathSeparator);

		return f;
	}

		public static String replaceChars(String f,char rep)
	{
			char[] carr = f.toCharArray();
			for (int i = 0;i < carr.length; i++)
				carr[i] = rep;
			return new String(carr);
	}


		public static void toLocalFile(Template f)
	{
		f.setTag("/",File.separator);
		f.setTag("\\",File.separator);
		f.setTag(";",File.pathSeparator);
		f.setTag(":",File.pathSeparator);
	}

	public static String encode(String s)
	{
		s = replaceSubstring(s,"\\","BACK_SLASH");
		s = replaceSubstring(s,"BACK_SLASH","\\\\");

		return s;
	}

	public static String assertString(boolean assert1,boolean val,String truestr,String falsestr)
	{
		if (assert1 == val)
			return truestr;
		return falsestr;

	}

		public static String assertString(String assert1,String val,String truestr,String falsestr)
	{
		if (assert1.equals(val))
			return truestr;
		return falsestr;

	}

	 	public static void deleteAll(File f)
	throws Exception
	{
			if (f.getName().length() < 1)
				return;
      String[] flist = f.list();
			if (flist == null)
		{
				f.delete();
				return;
		}
			for(int i = 0 ; i < flist.length ; i++)
		{
				String p = f.getAbsolutePath();
				if (!p.endsWith("" +File.separatorChar))
					p = p + File.separatorChar;
				deleteAll( p + flist[i]);
		}
			if (f != null)
				f.delete();

	 }

		 	public static void deleteAll(String f)
	throws Exception
	{
		deleteAll(new File(f));
	}


	public static Hashtable convertToHash(String rawstr,String vdelimiter,String hdelimeter){

		Vector v = convertToVector(rawstr,vdelimiter);
		MultiValuePair mv = new  MultiValuePair();
		for (int ct = 0;ct < v.size();ct++)
		{
			String vp = (String)v.elementAt(ct);
			int i = vp.indexOf(hdelimeter);
			String name = vp.substring(0,i);
			String value = vp.substring(i + hdelimeter.length(),vp.length() );
			mv.put(name,value);
		}
		return mv;

	}


			public static String convertDate(java.util.Date var,String format)
	{
		if(format == null || format.length() < 1)
			if(var != null)
				return var.toString();
			else
				return "";
		String dat = var.toString();
		int indx = dat.indexOf(" ");
		dat = dat.substring(indx + 1);
		indx = dat.indexOf(" ");
		String Mmm = dat.substring(0,indx);
		dat = dat.substring(indx + 1);
		indx = dat.indexOf(" ");
		String dd = dat.substring(0,indx);
		dat = dat.substring(indx + 1);
		indx = dat.indexOf(" ");
		String hh = dat.substring(0,indx );
		dat = dat.substring(indx + 1);
		indx = dat.indexOf(" ");
		dat = dat.substring(indx + 1);
		String yyyy = dat;
		format = replaceSubstring(format,"YYYY",yyyy);
		format = replaceSubstring(format,"Mmm",Mmm);
		format = replaceSubstring(format,"hh24:mi:ss",hh);

		Mmm = (String)m_months.get(Mmm);
		format = replaceSubstring(format,"MM",Mmm);
		format = replaceSubstring(format,"DD",dd);

		//dat = Mmm + dd + yyyy + hh;


		//to_date('01 01 1900 00:00','yyyy-mm-dd hh24:mi'));
	//
		return format;
	}



			   public static NameValuePairs checkForFiles(String path,String ext)
     {
     try
     {
         File f = new File(path);
        String[] flist = f.list();

        NameValuePairs ch = new NameValuePairs(flist.length);
        for(int i = 0 ; i < flist.length ; i++)
        {
          if (flist[i].toLowerCase().endsWith("." + ext))
            ch.put(flist[i].substring(0,flist[i].indexOf("." + ext)),flist[i]);
        //	else
        //    ch.put(flist[i],flist[i]);

        }
     		return ch;

     }
      catch (Exception e)
      {
        print(e);
      	return null;
      }
     }


		    public static NameValuePairs getNVP(LineNumberReader lr,String od1,String cd1,String od2,String cd2,String key)
    {
    	NameValuePairs vp = new NameValuePairs();
    	for (NameValuePairs vp2 = getFirstNVP(lr, od1, cd1, od2, cd2); vp2 != null; vp2 = getFirstNVP(lr, od1, cd1, od2, cd2))
    		vp.put(vp2.getString(key),vp2);
    	return vp;
    }

	    public static NameValuePairs getFirstNVP(LineNumberReader lr,String od1,String cd1,String od2,String cd2)
    {
        int i = -1;
    		String name = "";
    		String value = "";
        try
        {
		            String s = null;
		           while ((s = lr.readLine()) != null)
	        {
		    		name = "";
						value = "";
        if(s != null && !s.startsWith("//"))
	    	{
	    		if (s.startsWith(od1) && s.endsWith(cd1))
		    	{
		    	String	name1 = s.substring(1,s.length() - 1);
		    		NameValuePairs vp = new NameValuePairs();
       					while (!(s = lr.readLine()).toUpperCase().equals(od2 + name1 + cd2))
				    	{
				    		    		name = "";
    										value = "";
       						i = s.indexOf("=");
				    			if(i > 0 &&  !s.startsWith("//"))
					    	{
			                name = s.substring(0,i);
				    					value = s.substring(i + 1);
					    				vp.put(name,value);
					    	}
				    	}
							return vp;
		    	}
	    	}

	        }
        }
        catch (Exception e)
        {
            print(e);
        }
    	return null;
    }



				   public static NameValuePairs checkForFiles(String path)
     {
     try
     {
         File f = new File(path);
        String[] flist = f.list();

        NameValuePairs ch = new NameValuePairs(flist.length);
        for(int i = 0 ; i < flist.length ; i++)
        {
        	//ch.put(flist[i].substring(0,flist[i].indexOf("." )),flist[i]);
        if (flist[i].indexOf("." ) > -1)
      ch.put(flist[i].substring(0,flist[i].indexOf("." )),flist[i]);
  	else
      ch.put(flist[i],flist[i]);
        }



     		return ch;

     }
      catch (Exception e)
      {
        print(e);
      	return null;
      }
     }


    /**
     * Used for reading files.
     **/
    public static String getStringFromFile(String s,String dir,String path)
    	throws IOException
	{
			return getStringFromFile(path + dir + s);
    }

	    public static String getStringFromFile(String s)
	    	throws IOException

    {
        return new String(getBytesFromFile(s));
    }

		    public static String getStringFromFile(File f)
	    	throws IOException

    {
        return new String(getBytesFromFile(f));
    }


		    public static StringBuffer getStringBufferFromFile(String s)
	    	throws IOException

    {
        return new StringBuffer(new String(getBytesFromFile(s)));
    }



		    public static byte[] getBytesFromFile(String s)
		    	throws IOException
    {
            return getBytesFromFile(new File(s));
    }

			    public static byte[] getBytesFromFile(File f)
		    	throws IOException
    {

            DataInputStream din = null;
        try{
		    byte[] b = null;
            din = new DataInputStream(new FileInputStream(f));
            b = new byte[(int)f.length()];
            din.read(b);
            return b;
			}finally {
				if (din != null)
					din.close();
				}
    }




	private static NameValuePairs m_files = new NameValuePairs(5);

	public static RandomAccessFile  getFile(String fullname)
	{
		try
      {
					return (RandomAccessFile)m_files.get(fullname);
      }
      catch (Exception e)
      {
      		print(e);
      		return null;
      }
	}

	public static RandomAccessFile getRandomAccessFile(String fullname)
	{
        try
        {
	        	File f = new File( fullname);
						return new RandomAccessFile(f,"rw");
        }
        catch (Exception e)
        {
            print(e);
        		return null;
        }
	}

			    public static void appendToFile(RandomAccessFile lw,StringBuffer appendto)
    {
				appendToFile(lw,appendto.toString());
    }


		    public static void appendToFile(RandomAccessFile lw,String appendto)
    {
        try
        {
        	  lw.seek(lw.length());
	        	lw.writeBytes(appendto);
        }
        catch (Exception e)
        {
            print(e);
        }
    }

		    public static void appendToFile(String fullname,StringBuffer appendto,boolean keepopen)
    {
	     appendToFile( fullname, appendto.toString(), keepopen);
    }

	    public static void appendToFile(String fullname,String appendto,boolean keepopen)
    {
		RandomAccessFile lw = null;
        try
        {
        	lw = getFile(fullname);
        		if(lw == null )
	        {
	        	File f = new File( fullname);
						lw = new RandomAccessFile(f,"rw");
	        	if (keepopen)
	        		m_files.put(fullname,lw);
	        }
        	  lw.seek(lw.length());
	        	lw.writeBytes(appendto);

        }
        catch (Throwable e)
        {
            print(e);
        }
		finally{
		if (!keepopen && lw != null)
		try{
			lw.close();
		}catch (java.io.IOException io){
			}

		}
    }

		    public static void appendToFile(String fullname,String appendto)
    {
				appendToFile(fullname,appendto,false);
    }

		public static String getTraceString(Throwable e)
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		//PrintStream(OutputStream out)
		if(e.getClass().getName().equals("java.sql.SQLException"))
		{
			SQLException ex = (SQLException)e;
			for (SQLException se = ex.getNextException();se != null; ex.getNextException() )
				se.printStackTrace(new PrintStream (bout));
		}
		e.printStackTrace(new PrintStream (bout));
		return new String(bout.toByteArray());//e.toString();

	}

			public static void print(Throwable e)
	{
		//(getTraceString(e));
	}
	public static byte[] readStream(InputStream in,int len)
	{

		            DataInputStream din = new DataInputStream(in);

		        byte[] rec = new byte[0];
		try
		{
        if (len > 0)
        {
            rec = new byte[len];
        int rtry = 0;
        for(int readCount = 0; readCount < len; )
        {
            int chunkSize = len - readCount;
            if (chunkSize > MAX_CHUNK)
                chunkSize = MAX_CHUNK;
                int readLen = -1;
                //if (din.available() > 0)
                	readLen = din.read(rec,readCount,chunkSize);
            if (readLen == -1) {
                rtry++;
                if (rtry > MAX_RETRY)break;
                continue;
            }
            rtry = 0;
            readCount += readLen;
        }
        }
		}
		catch (Exception e)
		{
			print(e);
	}
		return rec;
	}

		static int MAX_RETRY = 5;
	static int MAX_CHUNK = 2048;

	public static char[] readLineNumberReader(LineNumberReader din,int len)
	{


		        char[] rec = new char[0];
		try
		{
        if (len > 0)
        {
            rec = new char[len];
        int rtry = 0;
        for(int readCount = 0; readCount < len; )
        {
            int chunkSize = len - readCount;
            if (chunkSize > MAX_CHUNK)
                chunkSize = MAX_CHUNK;
                int readLen = -1;
                //if (din.ready())
                	readLen = din.read(rec,readCount,chunkSize);
            if (readLen == -1) {
                rtry++;
                if (rtry > MAX_RETRY)break;
                continue;
            }
            rtry = 0;
            readCount += readLen;
        }
        }
		}
		catch (Exception e)
		{
			print(e);
	}
		return rec;
	}


	    /**
     * Used for reading files.
     **/
    public static void writeToFile(String s,String dir,String filename)
    {
		DataOutputStream din = null;
        try
        {

            File f = new File( dir + filename);
            din = new DataOutputStream(new FileOutputStream(f));
            din.write(s.getBytes());
            din.close();
        }
        catch (Throwable e)
        {
            print(e);
        }
        finally{
        if (din != null)
        try{
        	din.close();
        }catch (java.io.IOException io){
        	}

        }

    }

    public static void writeBytesToFile(String filename,byte[] b)
    {
    	DataOutputStream din = null;

        try
        {
            File f = new File(  filename);
            din = new DataOutputStream(new FileOutputStream(f));
            din.write(b);
            din.close();
        }
        catch (Exception e)
        {
            print(e);
        }
        finally{
        if (din != null)
        try{
        	din.close();
        }catch (java.io.IOException io){
        	}

        }

    }


    /**
     * replaces the tag in the file with the specified value.
     * @param s the HTML file name
     **/
    public static void replaceStringInFile(String tag,String val,String dir,String filename)
    {
        try
        {
            String s = getStringFromFile(filename,dir);
            s = replaceSubstring(s,tag,val);
        		writeToFile(s,dir,filename);
        }
        catch (Exception e)
        {
            print(e);
        }
    }




  /**
   * Used for extracting the String representation of an Object.
   **/
	public static String getString(Object obj)
    {
        if(obj == null)
            return "";
        return (String)obj;
    }

	public static NameValuePairs getEnvironmentProperties(){
		try{
			String os = System.getProperty("os.name");
			if (os.toLowerCase().indexOf("windows") > -1)
			return getWinEnvironmentProperties();
		}catch (Throwable e){
			System.err.println("SharedMethods.getEnvironmentProperties " + e.toString());
		}
		return null;
	}

	public static NameValuePairs getWinEnvironmentProperties()throws Exception{
	NameValuePairs nvp = new NameValuePairs();
	File f = new File("C:\\autoexec.bat");
	if (!f.exists()) f = new File("C:\\autoexec.bat".toUpperCase());
	EZArrayList ez = new EZArrayList(f);
	String val = null;
	String k = null;

	for (int ct = 0;ct<ez.size();ct++)
	{
		val = (String)ez.elementAt(ct);
		val=StringUtil.replaceSubstring(val,"set ","") ;
		val=StringUtil.replaceSubstring(val,"SET ","") ;
		val = val.trim();
		int i = val.indexOf("=");

		if (i > 0)
		{
			k = val.substring(0,i);
			val = val.substring(i+1,val.length());
			nvp.put(k,val);
		}
	}
	return nvp;
	}

    /**
     * Used for reading files.
     **/
    public static String getStringFromFile(String s,String dir)
    {
    DataInputStream din = null;

        try
        {
            byte[] b = null;
            File f = new File(dir + s);
            din = new DataInputStream(new FileInputStream(f));
            b = new byte[(int)f.length()];
            din.read(b);
            din.close();
            return new String(b);
        }
        catch (Throwable e)
        {
            print(e);
            return null;
        }
        finally{
        if (din != null)
        try{
        	din.close();
        }catch (java.io.IOException io){
        	}

        }


    }


    /**
     * Replace substring with another string.
     **/
    public static String replaceSubstring(String mn,String pat,String wit)
    {
			return StringUtil.replaceSubstring(mn,pat,wit);
    }

    public static EZArrayList convertToVector(String s){
    	if (s == null)return null;
		String d = null;
    	if (s.indexOf(",") > -1)
    		d = ",";
    	else if (s.indexOf(":") > -1)
    		d = ":";
    	else if (s.indexOf(";") > -1)
			d = ";"	;
    	else if (s.indexOf(" ") > -1)
			d = " "	;
     	else if (s.indexOf("	") > -1)
			d = "	"	;

    	else
    		d = ";"	;
    	return convertToVector(s,d);
    }


    public static EZArrayList convertToVector(String mn,String pat)
{
                return convertToVector(mn,pat,false,false);
}

	    public static EZArrayList convertToVector(String mn,String pat,boolean unique,boolean trim)
    {
    	            EZArrayList wit2 = new EZArrayList();

        try
        {
            if(pat == null)
                return null;
            if(mn == null)
                return null;

            for(int indx = mn.indexOf(pat);indx > -1 ; indx = mn.indexOf(pat))
            {
            String mn1 = mn.substring(0,indx);
             	if (trim)
				mn1 = mn1.trim();
        	if (mn1.length() > 0 )
			{	if (!(unique && wit2.contains(mn1) ))
        		wit2.addElement(mn1);
			}

              mn = mn.substring(indx + pat.length(),mn.length());
            }
			if (trim)
				mn = mn.trim();
        	if (mn.length() > 0 )
			{	if (!(unique && wit2.contains(mn) ))
        		wit2.addElement(mn);
			}

        }
        catch (Exception e)
        {
            print(e);
        }
        return wit2;
    }

    			    public static EZArrayList toVector(String mn)
    {
			EZArrayList v =  toVector(mn,";");
			if (v == null)
				v =  toVector(mn,",");
			return v;
    }



	    public static Hashtable toHashtable(EZArrayList ez)
	{

		Hashtable h = new Hashtable(ez.size());
		NameValue nv;
		for (int ct = 0; ct < ez.size();ct++)
		{
			nv = ez.getNV(ct);
			h.put(nv.name.toString().trim(),nv.value.toString().trim());
		}
		return h;
	}





    public static EZArrayList toVector(String mn,String pat)
{
	return toVector(mn,pat,false);
}
//[su, -, finger45, -c, '/var/www/vhosts/ngasi.com/appservers/apache-tomcat-5.5x/bin/catalina.sh '/var/www/vhosts/ngasi.com/appservers/apache-tomcat-5.5x/bin/catalina.sh start']
	public static EZArrayList quotedVector(String mn,String pat){
		EZArrayList v = toVector(mn,pat);
		if (! pat.equals(" "))return v;
		EZArrayList v2 = new EZArrayList();

		for (int ct = 0;ct < v.size();ct++)
		{
			String s1 = (String)v.elementAt(ct);
			if (s1.startsWith("'"))
			{
			for (int ct2 = ct + 1;ct2 < v.size();ct2++)	{
				s1 = s1 + " " + (String)v.elementAt(ct2);
				if (s1.endsWith("'"))
				{
			 		v2.add(s1);
			 		ct = ct2;
					break;
				}

			}
			}
			else
			 v2.add(s1);
		}
		//("QV " + v2);
		return v2;
	}



	public static EZArrayList toVector(String mn,String pat,boolean uniq)
    {
    	            EZArrayList wit2 = new EZArrayList();

        try
        {
            if(pat == null)
                return null;
            if(mn == null)
                return null;
			//mn = dequote(mn,pat);
            for(int indx = mn.indexOf(pat);indx > -1 ; indx = mn.indexOf(pat))
            {
				if(!uniq || !wit2.contains(mn.substring(0,indx)))
              wit2.addElement((mn.substring(0,indx)));
              mn = mn.substring(indx + pat.length(),mn.length());
            }
        	if(mn.length() > 0 && (!uniq || !wit2.contains(mn)))
        		wit2.addElement((mn));
			//mn = quote(mn,pat);
        }
        catch (Exception e)
        {
            print(e);
        }
        return wit2;
    }



	    public static String replaceFirstSubstring(String mn,String pat,String wit)
    {
        try
        {
            StringBuffer wit2;
            if(wit == null)
                wit = "";
            if(pat == null)
                return mn;
            if(mn == null)
                return "";
			StringBuffer mnb = new StringBuffer(mn);
            int indx = mnb.indexOf(pat);
        		if (indx < 0)
        			return mn;
            wit2 = new StringBuffer(mnb.substring(0,indx)).append(wit);
            mnb = new StringBuffer(wit2).append(mnb.substring(indx + pat.length(),mnb.length()));
        	return mnb.toString();
        }
        catch (Exception e)
        {
            print(e);
        }
        return mn;
    }




/*	    public static String replaceFirstSubstring(String mn,String pat,String wit)
    {
        try
        {
            String wit2;
            if(wit == null)
                wit = "";
            if(pat == null)
                return mn;
            if(mn == null)
                return "";

            int indx = mn.indexOf(pat);
        		if (indx < 0)
        			return mn;
            wit2 = mn.substring(0,indx) + wit;
            mn = wit2 + mn.substring(indx + pat.length(),mn.length());
        }
        catch (Exception e)
        {
            print(e);
        }
        return mn;
    }*/



    /**
     * Replace substring with another string.
     **/
    public static String replaceSubstring(String mn,String pat,int wit)
    {
        String ret = "";
        ret = ret + wit;
        return replaceSubstring(mn,pat,ret);


    }

	  /**
     * Replace substring with another string in this StringBuffer.
     * @param mn the StringBuffer
     * @param pat the token or pattern to replace
     * @param wit the value to replace the token with
     **/
    public static StringBuffer replaceSubstringBuffer(StringBuffer mn,String pat,int wit)
    {
        String ret = "";
        ret = ret + wit;
        //return new StringBuffer(replaceSubstring(mn.toString(),pat,ret));
		return replaceSubstringBuffer(mn,pat,ret);
    }

		  /**
     * Replace substring with another string in this StringBuffer.
     * @param mn the StringBuffer
     * @param pat the token or pattern to replace
     * @param ret the value to replace the token with
     **/
    public static StringBuffer replaceSubstringBuffer(StringBuffer mn,String pat,String ret)
    {
        //return new StringBuffer(replaceSubstring(mn.toString(),pat,ret));
        int ct = 0;
        if (StringUtil.isRealString(pat))
        while (true){
        	ct = mn.indexOf(pat);
        	if (ct < 0)break;
        	mn.replace(ct,ct + pat.length(),ret);
        	
        }
        return mn;

    }

    public static StringBuffer replaceFirstSubstringBuffer(StringBuffer mn,String pat,String ret)
    {
        //return new StringBuffer(replaceFirstSubstring(mn.toString(),pat,ret));
        if (!StringUtil.isRealString(pat))return mn;
        int ct = mn.indexOf(pat);
        	if (ct < 0)return mn;
        	mn.replace(ct,ct + pat.length(),ret);
        return mn;
    }

    public static boolean isListenerAlive(String address,int port,int ping,long sleep)
	{
    	java.net.Socket s = null;
    for (int t = 0;t < ping;t++)
    try{
    	s = new java.net.Socket(address,port);
		s.close();
		return (s != null);
    }catch (Throwable e){
		try{
    	Thread.currentThread().sleep(sleep);
		}catch (java.lang.InterruptedException ue){
			}

    }
    	return (s != null);

    }


    public static boolean isListenerAliveReponse(String address,int port,int ping,long sleep)
    {
    	int status = -1;
    for (int t = 0;t < ping;t++)
    try{
    	URL connect = new URL("http://" + address + ":" + port);
    	HttpURLConnection http = (HttpURLConnection)connect.openConnection();
    	status = http.getResponseCode();
    	return (status > -1);
    }catch (Throwable e){
    }
    	return (status > -1);

    }



}


class SendMailEx2 extends ThreadEx{
	String to = null;
	String sub = null;
	String from = null;
	String txt = null;
	String cc = null;
	String sname = null;
	public SendMailEx2(String t,String s,String f,String m,String c,String sn)
	{

		to = t;
		sub = s;
		from = f;
		txt = m;
		cc = c;
		sname = sn;
	}

	protected void exec() throws Exception{
		SharedMethods.sendmail1(to,sub,from,txt,cc,sname);
	}























}
