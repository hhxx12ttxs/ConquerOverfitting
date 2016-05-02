/**
 * Date:	2012-03-28
 * Author:	Daoyuan Wu
 * Refer:	ApkCrawler2.3
 */
package edu.polyu.apkcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import java.sql.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.polyu.apkcrawler.constants.Category;
import edu.polyu.apkcrawler.constants.Configuration;
import edu.polyu.apkcrawler.constants.LogFiles;
import edu.polyu.apkcrawler.io.AssetIdFetcher;
import edu.polyu.apkcrawler.io.GetAssetResponseFetcher;
import edu.polyu.apkcrawler.util.MD5Checksum;
import edu.polyu.apkcrawler.util.User;
import edu.polyu.apkcrawler.util.Device;
import edu.polyu.apkcrawler.util.CLI;

import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.App.ExtendedInfo;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.GetAssetResponse;
import com.gc.android.market.api.model.Market.GetAssetResponse.InstallAsset;

public class ApkCrawlerDBMain {
	
	final static String SELFNAME = "ApkCrawlerDB";
	final static String VERSION = "1.2";
	
	int totalCount = 0;
	/**
	 * Starting index for marketplace query.</br>
	 * 以10为单位增加的, startQuery比较好</br>
	 * <b>getAppsByXXX()中需要这个index</b>
	 */
	int startIndex = 0;
	/**
	 * Maximum number the start index can reach</br>
	 * <b>当startIndex或totalCount有改变时，这个值也得重新计算</b>
	 */
//	int maxAppIndex = startIndex + totalCount;	//怀疑这行代码的问题啊
//	下面string的获取也是，得专门给予get 和 set方法
	int maxAppIndex = startIndex + totalCount;
	/**
	 * 注意跟startIndex的区分
	 * 本来是用来统计每个category具体的排名的
	 * <b>现在改为用来统计当前这次runTask中下到多少个了</b></br>
	 * 现在总是从0开始
	 */
	int currentIndex = 0;
	/**
	 * 统计当前这个runTask历史累计下了多少个了
	 */
	int totalIndex = 0;
	
	/**
	 * case 0: return NONE;
	 * case 1: return POPULAR;
	 * case 2: return NEWEST;
	 * case 3: return FEATURED;
	 */
	int orderType = -1;
	
	int whichTask = -1;
	
	/**
	 * Maximum number the start index can reach in one publisher
	 */
	final int maxAppOnePub = 480;
	
	/**
	 * Maximum number of attempts a query can make
	 * from 3 to 1 in 120412
	 */
	final int maxAttempts = 3;
	
	/**
	 * currently support how many accounts
	 * no need *10, could be 14
	 * --> 20, in 120409
	 * --> 23, in 120412
	 * --> 60, in 121218 //mdfd by chenxiong
	 */
	private final int accountsNum = 60;
	/**
     * A list of user(s)
     */
    private User[] users;
    /**
     * A list of device(s)
     */
    private Device[] devices;
    /**
     * used by <code>getNextSession()</code>
     */
    int nextSessionIndex = 0;
    /**
     * current <code>CrawlerSession</code>,
     * changed each time calling <code>getNextSession()</code>
     */
    CrawlerSession crawlerSession;
    /**
     * A list of <code>CrawlerSession</code>
     */
    private CrawlerSession[] allSessions;
	
	/**
	 * which package to be downloaded
	 */
	String pkgName = null;
	String pkgFile;
	
	String pubFile;
	String pubName;
	
	/**
	 * result of parseCategory</br>
	 * e.g. Category.MULTIMEDIA;	"MULTIMEDIA"</br>
	 * category name in market api
	 */
	String categoryInApi;
	/**
	 * result of convertCategory</br>
	 * default: 0_UNKNOWN</br>
	 * category name in hardisk
	 */
	String categoryInDisk = "0_UNKNOWN";
	
	String queryWord = null;
	/**
	 * how many apps fetched by by query keyword
	 * default is 24 * 8 = 192
	 * It coube be specified by user using -qc option
	 * change to 200
	 */
	int queryCount = 200;
	
	
	/**
	 * fetch 5 times, usually equals to 5*10 = 50 apps<br/>
	 * sleep for a big time, then crawlerLogin again
	 * --> * 2 = 10
	 * --> 5 again
	 * --> 3 in version 0.4
	 * --> 10*accountsNum
	 */
	int everyFetchNum = 10*accountsNum;
	/**
	 * every 10 publishers, about 10 ~ 100 apps<br/>
	 * sleep for a big time, then crawlerLogin again
	 * --> 10*accountsNum
	 */
	int everyPubNum = 10*accountsNum;
	/**
	 * every 50 apps<br/>
	 * sleep for a big time, then crawlerLogin again
	 * --> *2 = 100
	 * --> 100*accountsNum
	 */
	int everyAppNum = 100*accountsNum;
	/**
	 * sleep time in milliseconds
	 * 5min = 5 * 60 = 300s
	 * --> * 2 = 600s = 10min
	 * --> * 2 = 1200s = 20min
	 * --> 10min
	 */
	int bigSleep = 60000;//600000 mdfd by chenxiong
	/**
     * 1000ms
     * every app, sleep for a while. In downloadApks()
     * --> *60 = 1min
     * --> *5 = 5min = 300000ms
     * --> 10s
     * --> 300000/accountsNum
     */
	final int defaultSmallSleep = 300000/accountsNum; //300000 mdfd by chenxiong
	/**
	 * = defaultSmallSleep
	 */
	int smallSleep = defaultSmallSleep;
	/**
	 * login wait time
	 * 180000ms = 3min
	 * --> 30s
	 */
	final int loginWaitTime = 30000;//mdfd by chenxiong
	
	final int defaultOneAccountIndex = accountsNum-1;
	int oneAccountIndex = defaultOneAccountIndex;
	
	final int defaultSmallAllCategory = 1;
    int smallAllCategory = defaultSmallAllCategory;
	
	Connection updateConn;
	PreparedStatement updateST;
	
	Connection queryConn;
    // cursor
    Statement queryST;
    ResultSet queryRS;
	// no cursor
    PreparedStatement onefullQueryST;
    ResultSet onefullRS;
	
	boolean isUnLimited = true;
	final static String ROOTCOME = "/home/chenxiong/autoApk/apkCome/";//mdfd by chenxiong
	final static String ROOTTODO = "/home/chenxiong/autoApk/apkTodo/";//mdfd by chenxiong
	
	String sqlFile;
	String sqlCmd = null;
	
	int downloadException = 0;
	final int maxDownloadException = 5;
	
	//add by chenxiong
	//number of accounts at the same time
	private final int accountsSameTime = 3;
	//the start index of current login accounts
	int accountStartIndex = 0;
	//current login sessions
	private CrawlerSession[] currentLoginSessions;
	//max accounts changes, when reaches this, should exit
	private final int maxChangeAccountsNum = 20;//20*3 = 60
	private int changeAccountsNum = 0;
	
	//add by chenxiong
	/*
	 * login the current accounts, from 'accountStartIndex' to
	 * 'accountStartIndex' + 'accountsSameTime'
	 */
	private void loginCurrentAccounts(){
		for(int i=0; i < accountsSameTime; i++){
			int index = (accountStartIndex+i) % accountsNum;
			currentLoginSessions[i] = new CrawlerSession(devices[index],users[index]);
	        
			System.err.println("INFO: login currentLoginSessions["+i+"] --> "+index);
			
	        try {
                System.out.println("wait for a while: "+3000+"ms\n");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.err.print("EXCEPTION: ");
                e.printStackTrace();
            }
		}
		System.err.println();
	}
	
	//add by chenxiong
	/**
	 * when the exceptions happens frequently,
	 * we should change the accounts
	 */
	private void changeAccounts(){
		System.err.println("WARN: changeAccounts, current changeAccountsNum: " + changeAccountsNum);
		accountStartIndex += accountsSameTime;
		accountStartIndex %= accountsNum;
		changeAccountsNum += 1;
		nextSessionIndex = 0;
	}
	
	private void exitClean() {
		writeLogFiles();
		
		try {
			if (updateST != null)
				updateST.close();	System.out.println("==> close updateST");
			
	        if (queryST != null)
	        	queryST.close();	System.out.println("==> close queryST");
	        if (queryRS != null)
	        	queryRS.close();	System.out.println("==> close queryRS");
	        
	        if (onefullQueryST != null)
	        	onefullQueryST.close();	System.out.println("==> close md5QueryST");
	        if (onefullRS != null)
	        	onefullRS.close();	System.out.println("==> close md5RS");
	        
			if (updateConn != null)
				updateConn.close(); System.out.println("==> close updateConn");
			if (queryConn != null)
			    queryConn.close();   System.out.println("==> close queryConn");
			
		} catch (SQLException e) {
			System.err.print("EXCEPTION: ");
    		e.printStackTrace();
    		System.exit(1);
		}
	}
	
	/**
	 * 
	 * @param apkpath must exist
	 * @return
	 */
	private int getApkSize(String apkpath) {
	    File file = new File(apkpath);
	    
	    if (!file.exists() || !file.isFile())
	        return -1;
	    
	    return (int)file.length();
    }
	
	/**
	 * applicable both for <code>loginFirstSession()</code>,
	 * and <code>loginAllSessions()</code>
	 */
	private void initAccounts() {
	    // TODO new的时候可以用变量?
	    // 我试了不管加没加final也行
	    users = new User[accountsNum];
	    // 1-10
	    users[0] = new User("1jacobisabella@gmail.com",    "loveg1@2012", "");
	    users[1] = new User("2ethansophia@gmail.com",      "loveg2@2012", "");
	    users[2] = new User("3michaelemma@gmail.com",      "loveg3@2012", "");
	    users[3] = new User("4jaydenolivia@gmail.com",     "loveg4@2012", "");
	    users[4] = new User("5williamava@gmail.com",       "loveg5@2012", "");
	    users[5] = new User("6alexanderemily@gmail.com",   "loveg6@2012", "");
	    users[6] = new User("7noahabigail@gmail.com",      "loveg7@2012", "");
	    users[7] = new User("8danielmadison@gmail.com",    "loveg8@2012", "");
	    users[8] = new User("9aidenchloe@gmail.com",       "loveg9@2012", "");
	    users[9] = new User("10anthonymia@gmail.com",      "loveg10@2012", "");
	    // 10-20
	    users[10] = new User("11joshuaaddison@gmail.com",   "loveg11@2012", "");
	    users[11] = new User("12masonelizabeth@gmail.com",  "loveg12@2012", "");
	    users[12] = new User("13christopherella@gmail.com", "loveg13@2012", "");
	    users[13] = new User("14andrewnatalie@gmail.com",   "loveg14@2012", "");
	    users[14] = new User("15davidsamantha@gmail.com",   "loveg15@2012", "");
	    users[15] = new User("16matthewalexis@gmail.com",   "loveg16@2012", "");
	    users[16] = new User("17loganlily@gmail.com",      	"loveg17@2012", "");
	    users[17] = new User("18elijahgrace@gmail.com",    	"loveg18@2012", "");
	    users[18] = new User("19jameshailey@gmail.com",     "loveg19@2012", "");
	    users[19] = new User("20josephalyssa@gmail.com",    "loveg20@2012", "");
	    // 20-30
	    users[20] = new User("21gabriellillian@gmail.com",  "loveg21@2012", "");
        users[21] = new User("22benjaminhannah@gmail.com",  "loveg22@2012", "");
        users[22] = new User("23ryanavery@gmail.com",       "loveg23@2012", "");
        
        //add by chenxiong
        users[23] = new User("24samuelleah@gmail.com", 	"loveg24@2012", "");
        users[24] = new User("25jacksonnevaeh@gmail.com",	 "loveg25@2012", "");
        users[25] = new User("26johnsofia@gmail.com", 	"loveg26@2012", "");
        users[26] = new User("27nathanashley@gmail.com",	 "loveg27@2012", "");
        users[27] = new User("28jonathananna@gmail.com", 	"loveg28@2012", "");
        users[28] = new User("29christiansarah@gmail.com",	"loveg29@2012", "");
        users[29] = new User("30liambrianna@gmail.com", 	"loveg30@2012", "");
        users[30] = new User("31dylanzoe@gmail.com",	"loveg31@2012", "");
        users[31] = new User("32landonvictoria@gmail.com", 	"loveg32@2012",	"");
        users[32] = new User("33calebgabriella@gmail.com",	"loveg33@2012",	"");
        users[33] = new User("34tylerbrooklyn@gmail.com",		"loveg34@2012",	"");
        users[34] = new User("35lucaskaylee@gmail.com",	"loveg35@2012",	"");
        users[35] = new User("36evantaylor@gmail.com",	"loveg36@2012",	"");
        users[36] = new User("37nicholaslayla@gmail.com",	"loveg37@2012",	"");
        users[37] = new User("38gavinallison@gmail.com",	"loveg38@2012",	"");
        users[38] = new User("39issacevelyn@gmail.com",	"loveg39@2012",	"");
        users[39] = new User("40braydenriley@gmail.com",	"loveg40@2012",	"");
        users[40] = new User("41lukeamelia@gmail.com",	"loveg41@2012",	"");
        users[41] = new User("42angelkgloe@gmail.com",	"loveg42@2012",	"");
        users[42] = new User("43isaiahmakayla@gmail.com",	"loveg43@2012",	"");
        users[43] = new User("44brandonaubrey@gmail.com",	"loveg44@2012",	"");
        users[44] = new User("45jacksavannah@gmail.com",	"loveg45@2012",	"");
        users[45] = new User("46jordancharlotte@gmail.com",	"loveg46@2012",	"");
        users[46] = new User("47owenzoey@gmail.com",	"loveg47@2012",	"");
        users[47] = new User("48carterbella@gmail.com",	"loveg48@2012",	"");
        users[48] = new User("49connoralexa@gmail.com",	"loveg49@2012",	"");
        users[49] = new User("50justinkayla@gmail.com",	"loveg50@2012",	"");
        users[50] = new User("51jeremiahpeyton@gmail.com",	"loveg51@2012",	"");
        users[51] = new User("52joseaudrey@gmail.com",	"loveg52@2012",	"");
        users[52] = new User("53julianclaire@gmail.com",	"loveg53@2012",	"");
        users[53] = new User("54robertarianna@gmail.com",	"loveg54@2012",	"");
        users[54] = new User("55aaronjulia@gmail.com",	"loveg55@2012",	"");
        users[55] = new User("56adrianaaliyah@gmail.com",	"loveg56@2012",	"");
        users[56] = new User("57wyattkylie@gmail.com",	"loveg57@2012",	"");
        users[57] = new User("58kevinlauren@gmail.com",	"loveg58@2012",	"");
        users[58] = new User("59huntersophie@gmail.com",	"loveg59@2012",	"");
        users[59] = new User("60cameronsydney@gmail.com",	"loveg60@2012",	"");
        
	    
	    devices = new Device[accountsNum];
	    // 1-10
	    devices[0] = new Device(10, "Nexus S", "", "3b8536d11f6b1ec9");
	    devices[1] = new Device(10, "Nexus S", "", "32f988c479be9e8c");
	    devices[2] = new Device(10, "Nexus S", "", "34519d9ef32351cd");
	    devices[3] = new Device(10, "Nexus S", "", "3d847814f19172a7");
	    devices[4] = new Device(10, "Nexus S", "", "38dde9d111c93c2c");
	    devices[5] = new Device(10, "Nexus S", "", "3cf7cc1695909d35");
	    devices[6] = new Device(10, "Nexus S", "", "3a5017336ed5a75b");
	    devices[7] = new Device(10, "Nexus S", "", "3b68cbfd58060344");
	    devices[8] = new Device(10, "Nexus S", "", "335b906ffc59838e");
	    devices[9] = new Device(10, "Nexus S", "", "33b4e1928a53e5a0");
	    // 10-20
	    devices[10] = new Device(10, "Nexus S", "", "361e0dcca39161c0");
        devices[11] = new Device(10, "Nexus S", "", "3a7bdabff63b68ee");
        devices[12] = new Device(10, "Nexus S", "", "3ce7f9b502f0fbd7");
        devices[13] = new Device(10, "Nexus S", "", "3b16d0afbee45244");
        devices[14] = new Device(10, "Nexus S", "", "3f88fc4e7318ea87");
	    devices[15] = new Device(10, "Nexus S", "", "3a1a872f814145b5");
	    devices[16] = new Device(10, "Nexus S", "", "3e096f1086e8613f");
	    devices[17] = new Device(10, "Nexus S", "", "3d81b0006dc298c5");
	    devices[18] = new Device(10, "Nexus S", "", "3c62514094e5a4db");
	    devices[19] = new Device(10, "Nexus S", "", "3288583c39dcd682");
	    // 20-30
	    devices[20] = new Device(10, "Milestone", "", "34097637c7740fc9");
        devices[21] = new Device(10, "Milestone", "", "36824a66319a0f7d");
        devices[22] = new Device(10, "Milestone", "", "33a76efb41930646");
        
        //add by chenxiong
        devices[23] = new Device(10, "Milestone", "", "3326b99d27097a12");
        devices[24] = new Device(10, "Milestone", "", "30f095970299a0f7");
        devices[25] = new Device(10, "Milestone", "", "3867ffdfc41186c5");
        devices[26] = new Device(10, "Milestone", "", "351f483074611224");
        devices[27] = new Device(10, "Milestone", "", "3a910af6d0169498");
        devices[28] = new Device(10, "Milestone", "", "3a982afe12eb40b9");
        devices[29] = new Device(10, "Milestone", "", "3a98465ccbd4a2cb");
        devices[30] = new Device(10, "Milestone", "", "31bf7ba4718477f6");
        devices[31] = new Device(10, "Milestone", "", "3b6698d63be8d423");
        devices[32] = new Device(10, "Milestone", "", "33ce08c9d5199220");
        devices[33] = new Device(10, "Milestone", "", "396a55e583b9fa33");
        devices[34] = new Device(10, "Milestone", "", "3a3fadda6b27dcaa");
        devices[35] = new Device(10, "Milestone", "", "354e914c48730453");
        devices[36] = new Device(10, "Milestone", "", "3cd4a3c253a12e7e");
        devices[37] = new Device(10, "Milestone", "", "3c6e558aa6fc4fb2");
        devices[38] = new Device(10, "Milestone", "", "33997dc3a1e918dd");
        devices[39] = new Device(10, "Milestone", "", "3d2870db1442936a");
        devices[40] = new Device(10, "Milestone", "", "308008c52a821fc3");
        devices[41] = new Device(10, "Milestone", "", "3e26383377fde9c5");
        devices[42] = new Device(10, "Milestone", "", "3520b42bae90c587");
        devices[43] = new Device(10, "Milestone", "", "384098afdd3808ee");
        devices[44] = new Device(10, "Milestone", "", "3e5c128a8103072c");
        devices[45] = new Device(10, "Milestone", "", "32ddb716e90a7825");
        devices[46] = new Device(10, "Milestone", "", "3e3b83366e9efc43");
        devices[47] = new Device(10, "Milestone", "", "3b52a2bb4151b00b");
        devices[48] = new Device(10, "Milestone", "", "328fa356e7814d3d");
        devices[49] = new Device(10, "Milestone", "", "3578f695a15e3d00");
        devices[50] = new Device(10, "Milestone", "", "38aa15d6a6bb6eea");
        devices[51] = new Device(10, "Milestone", "", "3a6821bf10156231");
        devices[52] = new Device(10, "Milestone", "", "331802eb8016745e");
        devices[53] = new Device(10, "Milestone", "", "363c3eb96c3298cf");
        devices[54] = new Device(10, "Milestone", "", "39a8168578772025");
        devices[55] = new Device(10, "Milestone", "", "31f3254d74779549");
        devices[56] = new Device(10, "Milestone", "", "3da51b0c33e6bd7e");
        devices[57] = new Device(10, "Milestone", "", "3e620fbb34cd5ac4");
        devices[58] = new Device(10, "Milestone", "", "3bc01a96a761dcaf");
        devices[59] = new Device(10, "Milestone", "", "305df84da682a3d6");
	    
	    allSessions = new CrawlerSession[accountsNum];
	    
	    //add by chenxiong
	    currentLoginSessions = new CrawlerSession[accountsSameTime];
	    Random random = new Random();
	    accountStartIndex = Math.abs(random.nextInt()) % accountsNum;
	    nextSessionIndex = 0;
	}
	
	/**
	 * only login to one <code>CrawlerSession</code>
	 */
	private void loginOneSession() {
	    int i = this.oneAccountIndex;
	    allSessions[i] = new CrawlerSession(devices[i], users[i]);
	    System.out.println("CrawlerSession["+i+"], Cookie: "+allSessions[i].getCookie());
        System.out.println("CrawlerSession["+i+"], secureToken: "+allSessions[i].getSecureToken());
	}
	
	/**
     * login allSessions[accountsNum]
     */
	private void loginAllSessions() {
	    for (int i = 0; i < accountsNum; i++) {
	        allSessions[i] = new CrawlerSession(devices[i], users[i]);
	        System.out.println("CrawlerSession["+i+"], Cookie: "+allSessions[i].getCookie());
	        System.out.println("CrawlerSession["+i+"], secureToken: "+allSessions[i].getSecureToken());
	        
	        try {
                System.out.println("wait for a while: "+loginWaitTime+"ms\n");
                Thread.sleep(loginWaitTime);
            } catch (InterruptedException e) {
                System.err.print("EXCEPTION: ");
                e.printStackTrace();
            }
	    }
	}
	
	/**
	 * Only need to be called once.
	 * 
	 * make this.crawlerSession point to one <code>CrawlerSession</code>,
	 * then we can call functions in this.crawlerSession
	 * 
	 * It has nothing to do with <code>nextSessionIndex</code>.
	 */
	private void moveToOneSession() {
	    this.crawlerSession = allSessions[this.oneAccountIndex];
	}
	
	/**
	 * MUST be called before calling any <code>getAppsByXXX()</code>
	 * 
     * make this.crawlerSession point to next <code>CrawlerSession</code>,
     * then we can call functions in this.crawlerSession
     * 
     * <code>nextSessionIndex</code> will loop.
     */
	private void moveToNextSession() {
	    // first time will be allSessions[0]
	    // TODO 应该高级对象也可以直接赋值吧
		
		//deld by chenxiong
	    //this.crawlerSession = allSessions[nextSessionIndex];
		this.crawlerSession = currentLoginSessions[nextSessionIndex++];
	    
		//deld by chenxiong
	    //nextSessionIndex = (++nextSessionIndex) % accountsNum;
		nextSessionIndex = nextSessionIndex % accountsSameTime;
//	    System.out.println("\n==> nextSessionIndex will be "+nextSessionIndex);
    }
	
	/**
	 * 用最笨的switch-case来解决
	 * 还必须得整型。。只能用if了 //必须用else-if
	 */
	private String convertCategoryTitle(String oldCategory) {
        String newCategory = "0_UNKNOWN";
        
        /**
         * "2_DEMO" and "6_LIBRARIES" are the same  // default "2_DEMO"
         * market have: 8(games) + 26(apps) = 34
         * our directory: 26 - 1(11_THEMES) - 
         * our directory = market have + 1, because "LIBRARIES & DEMO" has two dirs
         */
        if ( "Arcade & Action".equalsIgnoreCase(oldCategory) ) {     // "ARCADE"
            newCategory = "1_ARCADE";
            
        } else if ( "LIBRARIES & DEMO".equalsIgnoreCase(oldCategory) ) {
            newCategory = "2_DEMO";
            
        } else if ( "ENTERTAINMENT".equalsIgnoreCase(oldCategory) ) {
            newCategory = "3_ENTERTAINMENT";
            
        } else if ( "FINANCE".equalsIgnoreCase(oldCategory) ) {
            newCategory = "4_FINANCE";
            
        } else if ( "HEALTH & FITNESS".equalsIgnoreCase(oldCategory) ) {
            newCategory = "5_HEALTH";
            
        } else if ( "LIBRARIES & DEMO".equalsIgnoreCase(oldCategory) ) {    // 不会被赋值
            newCategory = "6_LIBRARIES";
            
        } else if ( "LIFESTYLE".equalsIgnoreCase(oldCategory) ) {
            newCategory = "7_LIFESTYLE";
            
        } else if ( "MEDIA & VIDEO".equalsIgnoreCase(oldCategory) ) {
            newCategory = "8_MULTIMEDIA";
            
        } else if ( "News & Magazines".equalsIgnoreCase(oldCategory) ) {
            newCategory = "9_NEWS";
            
        } else if ( "BOOKS & REFERENCE".equalsIgnoreCase(oldCategory) ) {
            newCategory = "10_REFERENCE";
            
        } else if ( "theme".equalsIgnoreCase(oldCategory) ) {    // no accurate.不会被赋值
            newCategory = "11_THEMES";
            
        } else if ( "TRAVEL & LOCAL".equalsIgnoreCase(oldCategory) ) {
            newCategory = "12_TRAVEL";
            
        } else if ( "Brain & Puzzle".equalsIgnoreCase(oldCategory) ) { // "BRAIN"
            newCategory = "13_BRAIN";
            
        } else if ( "Cards & Casino".equalsIgnoreCase(oldCategory) ) {
            newCategory = "14_CARDS";
            
        } else if ( "CASUAL".equalsIgnoreCase(oldCategory) ) {
            newCategory = "15_CASUAL";
            
        } else if ( "COMICS".equalsIgnoreCase(oldCategory) ) {
            newCategory = "16_COMICS";
            
        } else if ( "COMMUNICATION".equalsIgnoreCase(oldCategory) ) {
            newCategory = "17_COMMUNICATION";
            
        } else if ( "PRODUCTIVITY".equalsIgnoreCase(oldCategory) ) {
            newCategory = "18_PRODUCTIVITY";
            
        } else if ( "SHOPPING".equalsIgnoreCase(oldCategory) ) {
            newCategory = "19_SHOPPING";
            
        } else if ( "SOCIAL".equalsIgnoreCase(oldCategory) ) {
            newCategory = "20_SOCIAL";
            
        } else if ( "SPORTS".equalsIgnoreCase(oldCategory) ) {    // sports have two
            newCategory = "21_SPORTS";
            
        } else if ( "TOOLS".equalsIgnoreCase(oldCategory) ) {
            newCategory = "22_TOOLS";
        
        // new added
        } else if ( "BUSINESS".equalsIgnoreCase(oldCategory) ) {
            newCategory = "23_BUSINESS";
            
        } else if ( "PERSONALIZATION".equalsIgnoreCase(oldCategory) ) {
            newCategory = "24_PERSONALIZATION";
        
        // more new added
        } else if ( "APP WALLPAPER".equalsIgnoreCase(oldCategory) ) {
            newCategory = "25_APP_WALLPAPER";
            
        } else if ( "SPORTS GAMES".equalsIgnoreCase(oldCategory) ) {// another sports
            newCategory = "26_SPORTS_GAMES";
            
        } else if ( "GAME WALLPAPER".equalsIgnoreCase(oldCategory) ) {
            newCategory = "27_GAME_WALLPAPER";
            
        } else if ( "RACING".equalsIgnoreCase(oldCategory) ) {
            newCategory = "28_RACING";
            
        } else if ( "GAME WIDGETS".equalsIgnoreCase(oldCategory) ) {
            newCategory = "29_GAME_WIDGETS";
            
        } else if ( "APP WIDGETS".equalsIgnoreCase(oldCategory) ) {
            newCategory = "30_APP_WIDGETS";
            
        } else if ( "EDUCATION".equalsIgnoreCase(oldCategory) ) {
            newCategory = "31_EDUCATION";
            
        } else if ( "MEDICAL".equalsIgnoreCase(oldCategory) ) {
            newCategory = "32_MEDICAL";
            
        } else if ( "MUSIC & AUDIO".equalsIgnoreCase(oldCategory) ) {
            newCategory = "33_MUSIC_AND_AUDIO";
            
        } else if ( "PHOTOGRAPHY".equalsIgnoreCase(oldCategory) ) {
            newCategory = "34_PHOTOGRAPHY";
            
        } else if ( "TRANSPORTATION".equalsIgnoreCase(oldCategory) ) {
            newCategory = "35_TRANSPORTATION";
            
        } else if ( "WEATHER".equalsIgnoreCase(oldCategory) ) {
            newCategory = "36_WEATHER";
        } 
        
        return newCategory;
    }
	
    /**
     * TODO
     * @param appsResponse is not null && appsResponse.getAppCount() != 0
     */
    private void newDownloadApks(AppsResponse appsResponse, int nAppCount) {
    	int nInstallAssetCount = 0;
        int appIndex;
        int installSize;
        String assetId;
        String pkgName;
        String rating;
        int ratingsCount;
        String apkName;
        String prefix;
        String oldCategory;
        String blobUrl;
        String downloadAuthCookieName;
        String downloadAuthCookieValue;
        String versionName;
        String creator;
        String title;
        String downloadsCountText;
        int versionCode;
        InstallAsset installAsset;
        App app;
        ExtendedInfo extendedInfo;
        
        // DONE: transmit from appsResponse.getAppCount() to int appCount
        for (appIndex = 0; appIndex < nAppCount; appIndex++, currentIndex++, totalIndex++) {
        	app = appsResponse.getApp(appIndex);
        	/**
        	 * get app info
        	 */
            assetId = app.getId();
            System.out.print("getId(): " + assetId + "\n");
            pkgName = app.getPackageName();
            System.out.print("getPackageName(): " + pkgName + "\n");
            creator = app.getCreator();
            System.out.print("getCreator(): " + creator + "\n");
            rating = app.getRating();
            System.out.print("getRating(): " + rating + "\n");
            ratingsCount = app.getRatingsCount();
            System.out.print("getRatingsCount(): " + ratingsCount + "\n");
            versionCode = app.getVersionCode();
            System.out.print("getVersionCode(): " + versionCode + "\n");
            versionName = app.getVersion();
            System.out.print("getVersion(): " + versionName + "\n");
            title = app.getTitle();
            System.out.print("getTitle(): " + title + "\n");
            /**
             * extendedInfo
             */
            extendedInfo = app.getExtendedInfo();
            installSize = extendedInfo.getInstallSize();
            System.out.print("getInstallSize(): " + installSize + "\n");
            oldCategory = extendedInfo.getCategory();
            System.out.print("getCategory(): " + oldCategory + "\n");
            downloadsCountText = extendedInfo.getDownloadsCountText();
            System.out.print("getDownloadsCountText(): " + downloadsCountText + "\n");
            /**
             * other
             */
            System.out.print("getDownloadsCount(): " + extendedInfo.getDownloadsCount() + "\n");
            
            apkName = totalIndex + "-" + currentIndex + "-" + pkgName + ".apk";
            if (whichTask == 3) {
            	// "-*-"
                apkName = totalIndex + "-*-" + pkgName + ".apk";
            }
            if (whichTask == 4) {
                // "-+-"
                apkName = totalIndex + "-+-" + pkgName + ".apk";
            }
            if (whichTask == 6) {
            	// "-#-"
                apkName = totalIndex + "-#-" + pkgName + ".apk";
            }
            // get category name
            if (whichTask == 4 || whichTask == 6) {
            	System.out.print("oldCategory from getCategory(): " + oldCategory + "\n");
                categoryInDisk = convertCategoryTitle(oldCategory);
                System.out.print("newCategory from convertCategory(): " + categoryInDisk + "\n");
            }
            prefix = "../" + categoryInDisk + "/";
            
            /**
             * TODO: dao
             * find real download url, i.e. blobUrl
             */
            GetAssetResponseFetcher getassetFetcher = new GetAssetResponseFetcher();
            GetAssetResponse getassetResponse = getassetFetcher.getGetAssetResponseById(
                    this.crawlerSession.getMarketSession(), assetId);
            
            if (getassetResponse != null) {
            	// TODO: test
                System.out.println(getassetResponse.toString());
                
                nInstallAssetCount = getassetResponse.getInstallAssetCount();
                if (nInstallAssetCount != 0) {
                    // only get first InstallAsset
                    installAsset = getassetResponse.getInstallAsset(0);
                    
                    blobUrl = installAsset.getBlobUrl();
                    downloadAuthCookieName = installAsset.getDownloadAuthCookieName();
                    downloadAuthCookieValue = installAsset.getDownloadAuthCookieValue();
                    
                } else {    // 0
                    System.err.print("ERROR: " + pkgName + ": nInstallAssetCount is 0\n\n");
                    return;
                }
                
            } else {    // null
                System.err.print("ERROR: " +pkgName + ": getassetResponse is null\n\n");
                return;
            }
         
            
            /**
             * TODO: dao
             * new save apk file
             */
            try {
                /*
                 * Send download request
                 */
                URL url = new URL(blobUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // For GET only
                conn.setRequestMethod("GET");
                // TODO: dao
                conn.setRequestProperty("User-agent", "AndroidDownloadManager");
                conn.setRequestProperty("Cookie", this.crawlerSession.getCookie()); // 靠.还必须得加这句
                conn.setRequestProperty(downloadAuthCookieName, downloadAuthCookieValue);
                
                if ( whichTask == 0 ) {
                    BufferedWriter ratingWriter = new BufferedWriter(
                            new FileWriter( LogFiles.ratingLog, true ) );
                    // write指针也会自动往后
                    ratingWriter.write( totalIndex + "-" + currentIndex + "-" + pkgName + ".apk" + "\t" + rating );
                    ratingWriter.write( "\n");
                    ratingWriter.close();
                } else if (whichTask == 3 || whichTask == 4 || whichTask == 6) {
                    String ratingFile = prefix + LogFiles.ratingLog;        
                    BufferedWriter ratingWriter = new BufferedWriter(
                            new FileWriter( ratingFile, true ) );
                    // write指针也会自动往后
                    ratingWriter.write( apkName + "\t" + rating );
                    ratingWriter.write( "\n");
                    ratingWriter.close();
                }

                /*
                 * Read response and save file...
                 */
                InputStream inputStream = conn.getInputStream();
                BufferedOutputStream bufferStream;
                if (whichTask == 3 || whichTask == 4 || whichTask == 6) {
                    bufferStream = new BufferedOutputStream( new FileOutputStream(
                            prefix + apkName ) );
                } else {
                    bufferStream = new BufferedOutputStream( new FileOutputStream(
                            apkName ) );
                }
                byte apkData[] = new byte[1024];
                int bytesRead;
                // java读写会自动调整指针??yes!! C也会吧
                while ( ( bytesRead = inputStream.read(apkData) ) != -1 )
                    bufferStream.write(apkData, 0, bytesRead);
                
                inputStream.close();
                // 何时flush，跟write的关系呢
                // The close method of FilterOutputStream calls its flush method,
                // and then calls the close method of its underlying output stream.
                bufferStream.close();
                
                System.out.println(">> " + apkName);
                // IOException的时候这边也会加吗??
                // 也就是异常之后程序还会在那边执行下去吗
//              currentIndex++;
//              totalIndex++;
                
            }
            catch (FileNotFoundException e) {
                System.err.println("ERROR: Bad url address!");
            }
            catch (UnsupportedEncodingException e) {
                System.err.println(e);
            }
            catch (MalformedURLException e) {
                System.err.println(e);
            }
            catch (IOException e) {
                if( e.toString().contains("HTTP response code: 403") ) {
                    // 1.9，把currentIndex改用totalIndex
                    System.err.println( "ERROR: " + this.totalIndex + ": " + "Forbidden response received: " + pkgName );
                    if ( whichTask == 0 || whichTask == 3 || whichTask == 4 || whichTask == 6) {
                        try {
                            FileWriter errorApkWriter = new FileWriter( LogFiles.errorApks, true );
                            // 输入一行怎么输
                            // TODO write 跟 append的区别
                            errorApkWriter.write( apkName );
                            errorApkWriter.write("\n");
                            errorApkWriter.close();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
                System.err.println(e);
                e.printStackTrace();
            }
            catch (Exception e) {
                System.err.println(e);
            } finally {
                try {
                    System.out.print("sleep for a while: " + smallSleep + "ms\n\n");
                    Thread.sleep(smallSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
        }   // end of "for"
    }
    
    /**
     * 
     * @param apkpath must ensure directory exist </br>
     * 			18_PRODUCTIVITY/1-=-com.pkg.apk
     * @param assetId
     * @return -1 for ERROR
     */
    private int downloadApk(String apkpath, String assetId) {
    	int result = 0;
    	int nInstallAssetCount = 0;
    	InstallAsset installAsset;
        String blobUrl;
        String downloadAuthCookieName;
        String downloadAuthCookieValue;
        String apkfullpath = ROOTCOME + apkpath;
        GetAssetResponseFetcher getassetFetcher;
        GetAssetResponse getassetResponse = null;
    	
        /**
         * find real download url, i.e. blobUrl
         */
        getassetFetcher = new GetAssetResponseFetcher();
        /**
         * @since 120417 in version 0.9
         */
        if (this.whichTask != 0) {
            System.out.println("INFO: (download)current SessionIndex is "+nextSessionIndex);//mdfd by chenxiong
            moveToNextSession();
            System.out.println("INFO: (download)nextSessionIndex will be "+nextSessionIndex);//mdfd by chenxiong
        }
        
        /**
         * for each getAppsByXXX(), then sleep
         */
        try {
            getassetResponse = getassetFetcher.getGetAssetResponseById(
                    this.crawlerSession.getMarketSession(),
                    assetId);
        } catch (Exception e) {
            System.err.print("EXCEPTION: "+apkpath+": ");
            e.printStackTrace();
            
            downloadException++;
            System.err.print("WARN: Current downloadException is: "+
            downloadException+"\n");
            if (downloadException >= 3) {
                // exit when has reached max exception
                System.out.print("\n==> Have reached max " +
                "downloadException, going to exit!\n");
                exitClean();
                System.exit(1);
            }
            
            return -1;
        }
        try {
            System.out.println("sleep for a while: "+smallSleep+"ms");
            Thread.sleep(smallSleep);
        } catch (InterruptedException e) {
            System.err.print("EXCEPTION: ");
            e.printStackTrace();
        }
        
        if (getassetResponse != null) {
        	// TODO: test
//          System.out.println(getassetResponse.toString());
            
            nInstallAssetCount = getassetResponse.getInstallAssetCount();
            if (nInstallAssetCount != 0) {
                // only get first InstallAsset
                installAsset = getassetResponse.getInstallAsset(0);
                
                blobUrl = installAsset.getBlobUrl();
                downloadAuthCookieName = installAsset.getDownloadAuthCookieName();
                downloadAuthCookieValue = installAsset.getDownloadAuthCookieValue();
                
            } else {    // 0
                System.err.print("ERROR: "+apkpath+": nInstallAssetCount is 0\n\n");
                return -1;
            }
            
        } else {    	// null
            System.err.print("ERROR: " +apkpath+": getassetResponse is null\n\n");
            return -1;
        }
     
        
        /**
         * save apk file
         */
        try {
            /**
             * Send download request
             */
            URL url = new URL(blobUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");	// For GET only
            conn.setRequestProperty("User-agent", "AndroidDownloadManager");
            conn.setRequestProperty("Cookie", this.crawlerSession.getCookie()); // 靠.还必须得加这句
            conn.setRequestProperty(downloadAuthCookieName, downloadAuthCookieValue);

            /**
             * @since 1.1-11
             */
            // getReadTimeout: 0ms
            // System.out.println("getReadTimeout: "+conn.getReadTimeout()+"ms");
            conn.setReadTimeout(Configuration.SOCKTIMEOUT);
            
            /**
             * Read response and save file...
             */
            InputStream inputStream = conn.getInputStream();    // TODO 502
            BufferedOutputStream bufferStream;
            bufferStream = new BufferedOutputStream(new FileOutputStream(apkfullpath));
            byte apkData[] = new byte[1024];
            int bytesRead;
            while ( (bytesRead = inputStream.read(apkData)) != -1 )
                bufferStream.write(apkData, 0, bytesRead);
            
            inputStream.close();
            bufferStream.close();
            
            System.out.println("Download: " + apkpath);
            downloadException = 0;  //成功一次就clear as 0
            
        } catch (Exception e) {
        	System.err.print("EXCEPTION: "+apkpath+": ");
    		e.printStackTrace();
    		System.err.println("WARN: downloadAuthCookieName: "+downloadAuthCookieName);
    		System.err.println("WARN: downloadAuthCookieValue: "+downloadAuthCookieValue);
    		System.err.println("WARN: installAsset: "+installAsset);
            
            downloadException++;
            System.err.print("WARN: Current downloadException is: "+
            downloadException+"\n");
            if (downloadException >= maxDownloadException) {
                // exit when has reached max exception
                System.out.print("\n==> Have reached max " +
                "downloadException, going to exit!\n");
                exitClean();
                System.exit(1);
            }
            
    		result = -1;
    		
        }
    	
    	return result;
    }
	
	private void writeLogFiles() {
		System.out.println("==> writeLogFiles");
		try {
			BufferedWriter crawlerWriter = new BufferedWriter(
					new FileWriter( LogFiles.crawlerLog, true ) );
			Date endDate = new Date();
			SimpleDateFormat endDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
			String endTime = endDateFormat.format(endDate);
			crawlerWriter.write("End Start Index: "+startIndex+"\n");
			crawlerWriter.write("End Current Index: "+currentIndex+"\n");
			crawlerWriter.write("Next Start Index: (oldStartIndex + newCurrentIndex), usually\n");
			crawlerWriter.write("Next Total Index: "+totalIndex+"\n");
			crawlerWriter.write("End Time: "+endTime+"\n");
			crawlerWriter.write("\n\n");
			crawlerWriter.close();
			
		} catch (IOException e) {
			System.err.println("EXCEPTION: " + e);
		}
	}
	
	/**
	 * write start time & start Index
	 */
	private void readLogFiles() {
		System.out.println("==> readLogFiles");
		try {
			BufferedWriter crawlerWriter = new BufferedWriter(
					new FileWriter( LogFiles.crawlerLog, true ) );
			Date startDate = new Date();
			SimpleDateFormat startDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
			String startTime = startDateFormat.format(startDate);
			
			crawlerWriter.write(SELFNAME+" Version: "+VERSION+"\n" );
			crawlerWriter.write( "Start Time: "+startTime+"\n" );
			crawlerWriter.write( "Which Task: "+whichTask+"\n" );
			if (this.queryWord != null)
				crawlerWriter.write( "Query Word: " + this.queryWord + "\n" );
			if (this.sqlCmd != null)
				crawlerWriter.write( "Sql Cmd: " + this.sqlCmd + "\n" );
			if (this.categoryInDisk != "0_UNKNOWN")
				crawlerWriter.write( "Category: "+categoryInDisk+"\n" );
			if (this.pkgName != null)
			    crawlerWriter.write("Pkg name: "+this.pkgName+"\n");
			if (this.orderType != -1) {
			    crawlerWriter.write( "OrderType: "+orderType+"\n" );
			}
			if (this.totalCount != 0) {
			    crawlerWriter.write( "TotalCount: "+totalCount+"\n" );
			}
			if (this.smallSleep != this.defaultSmallSleep) {
				crawlerWriter.write( "SmallTime: "+smallSleep+"\n" );
			}
			// TODO 其他只用loginLastSession()的也需要及时添加
			if (this.whichTask == 0) {
			    crawlerWriter.write("oneAccountIndex: ["+oneAccountIndex+"]\n");
			}
			crawlerWriter.write("Current Start Index: "+startIndex+"\n");
			crawlerWriter.write("Current currentIndex: "+currentIndex+"\n");
			crawlerWriter.write("Current Total Index: "+totalIndex+"\n");
			crawlerWriter.close();
			
		} catch (IOException e) {
			System.err.println("EXCEPTION: " + e);
			System.exit(1);
		}
	}
	
	/**
	 * case 0
	 * always download this apk, then compare md5 with db
	 */
	private void executeByOnePkg() {
		/**
		 * store results from getAppCount()
		 */
		int nAppCount = 0;
		int result = 0;
		App app = null;
		ExtendedInfo extendedInfo = null;
		String assetId, pkgName, oldCategory, apkName, apkPath;
		
		readLogFiles();
		/**
		 * @deprecated
		 * @since version 0.6
		 */
//		testCrawlerLogin();
		/**
		 * @since version 0.6
		 */
		loginOneSession();
		moveToOneSession();
		
		AssetIdFetcher pkgFetcher = new AssetIdFetcher();
		AppsResponse pkgResponse = pkgFetcher.getAppsByPackageName(
				crawlerSession.getMarketSession(),
				1,     //entriesCount
				this.pkgName);
		
		if (pkgResponse != null) {
			// TODO: for testing
			System.out.println(pkgResponse.toString());
			
			nAppCount = pkgResponse.getAppCount();
			if (nAppCount != 0) {
				/**
				 * get app info & extendedInfo
				 */
				app = pkgResponse.getApp(0);
				pkgName = app.getPackageName();
				assetId = app.getId();
				extendedInfo = app.getExtendedInfo();
				oldCategory = extendedInfo.getCategory();
				if (oldCategory != "") {
					categoryInDisk = convertCategoryTitle(oldCategory);
				}
				
				apkName = totalIndex + "-@-" + pkgName + ".apk";
				apkPath = categoryInDisk + "/" + apkName;
				
				result = downloadApk(apkPath, assetId);
				if (result == -1) {
					System.out.print("\n==> Didn't crawl pkg: "+this.pkgName+"\n");
			        exitClean();
					System.exit(1);
				}
				
				currentIndex++;	totalIndex++;
			    
			} else {	// 0
				System.err.print("WARN: "+this.pkgName+": nAppCount is 0\n");
			}
			
		} else {		// null
			System.err.print("WARN: "+this.pkgName+": pkgResponse is null\n");
		}
		
		System.out.print("\n==> Have done for pkg: "+this.pkgName+"\n");
        exitClean();
	}
	
	/**
	 * case 1
	 * TODO: didn't finish
	 * 
	 * always download apk, then compare md5 with db
	 * format: com.snda.youni;17_COMMUNICATION;
	 */
	private void executeByPkgFile() {
		int nAppCount = 0;
		
		readLogFiles();
		/**
         * @deprecated
         * @since version 0.6
         */
//		oldCrawlerLogin();
		/**
         * @since version 0.6
         */
		loginAllSessions();
		
		BufferedReader pkgfileReader;
		try {
			pkgfileReader = new BufferedReader( new FileReader(pkgFile) );
			String line = null;
			AssetIdFetcher pkgsFetcher = new AssetIdFetcher();
			
			while ( (line = pkgfileReader.readLine()) != null ) {
				//one error
//				com.bankcomm.mobile
//				4_FINANCE;
//				Bad url address!
//				String[] twoStrs = line.split(";", 2);
				String[] twoStrs = line.split(";", 3);
				pkgName = twoStrs[0];
				categoryInDisk = twoStrs[1];
				
				/**
		         * @since version 0.6
		         */
				System.out.println("\n==> current SessionIndex is "+nextSessionIndex);
                moveToNextSession();
                System.out.println("==> nextSessionIndex will be "+nextSessionIndex);
				AppsResponse pkgsResponse = pkgsFetcher.getAppsByPackageName(
						crawlerSession.getMarketSession(),
						1,     //entriesCount
						pkgName);
				
				if (pkgsResponse != null) {
					/**
					 * added in 2.3
					 */
//					System.out.println(pkgsResponse.toString());
					
					nAppCount = pkgsResponse.getAppCount();
					
					if (nAppCount != 0) {
					    // TODO: dao
//						downloadApks(pkgsResponse, nAppCount);
						newDownloadApks(pkgsResponse, nAppCount);
					} else {	// =0
//					    FileWriter errorApkWriter;
					    FileWriter errorApkWriter = null;
						try {
							System.err.print("Error: " + line + "\t0\n");
							errorApkWriter = new FileWriter( LogFiles.errorApks, true );
							errorApkWriter.write( line + "\t0\n" );
//							errorApkWriter.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						} finally {
						    if (errorApkWriter != null) {
                                errorApkWriter.close();
                            }
						}
					}
					
				} else {	// null
				    FileWriter errorApkWriter = null;
					try {
						System.err.print("Error: " + line + "\tNULL\n");
						errorApkWriter = new FileWriter( LogFiles.errorApks, true );
						errorApkWriter.write( line + "\tNULL\n" );
//						errorApkWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					} finally {
					    if (errorApkWriter != null) {
					        errorApkWriter.close();
					    }
					}
				}
				
			}	// end of "for"
			
			pkgfileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// java中出现异常，跟python中出现异常后
			// try里的分别还会执行不 //不会!
		}
		
		exitClean();
	}
	
	/**
	 * case 2
	 * not always download apk, will first compare with db
	 * only setEntriesCount as 10
	 * format: Kaixin001.com Inc.
	 * TODO
	 */
	private void executeByPubFile() {
		/**
		 * store results from getAppCount()
		 */
		int nAppCount = 0;
		/**
		 * app index for one pub
		 */
		int onePubIndex;
        /**
         * 用来统计是否到pubCountOut了
         * 可以一直计数下去，因为是取余数
         */
        int nPubNum = 0;
        /**
         * 用于遍历每次的pkgsResponse
         */
        int appIndex = 0;
        /**
         * getAppsByPublisher出现java.lang.RuntimeException的次数
         */
        int nException = 0;
        String line = null;
        BufferedReader pubfileReader;
        AssetIdFetcher pkgsFetcher;
        
        int result = 0;
		App app = null;
		ExtendedInfo extendedInfo = null;
		String assetId, pkgName, oldCategory, apkName, apkPath;
        
        readLogFiles();
        
        try {
            pubfileReader = new BufferedReader(new FileReader(pubFile));
            pkgsFetcher = new AssetIdFetcher();
            
            // readLine()是不包括'\n'的
            while ( (line = pubfileReader.readLine()) != null ) {
                //
                // 1. judge to sleep for a big time
                //
                if (nPubNum != 0 && nPubNum%everyPubNum == 0) {
                    try {
                    	System.out.print("==> sleep for a big time: "+bigSleep+"ms\n\n");
                        Thread.sleep(bigSleep);
                        
                    } catch (InterruptedException e) {
                    	System.err.print("EXCEPTION: ");
                		e.printStackTrace();
                    }
                }
            	
                //
                // 2. judge whether now is pubCountOut or not
                //
                if (nPubNum % everyPubNum == 0) {
                    System.out.print("==> crawlerLogin\n\n");
                    /**
                     * @deprecated
                     * @since version 0.6
                     */
//                  oldCrawlerLogin();
                    /**
                     * @since version 0.6
                     */
                    loginAllSessions();
                }
                
                //
                // 3. fetch one pub
                //
                onePubIndex = 0;
                pubName = line;
                System.out.print("--> Pub " + nPubNum + ": " + pubName + "\n\n");
                
                AppsResponse pkgsResponse = null;
                while (onePubIndex < maxAppOnePub) {
                    // 因为每次只query 10个
                	try {
                	    /**
                         * @since version 0.6
                         */
                	    System.out.println("\n==> current SessionIndex is "+nextSessionIndex);
                        moveToNextSession();
                        System.out.println("==> nextSessionIndex will be "+nextSessionIndex);
                    	
                    	/**
                         * for each getAppsByXXX(), then sleep
                         */
                        pkgsResponse = pkgsFetcher.getAppsByPublisher(
                                crawlerSession.getMarketSession(),
                                pubName,
                                onePubIndex,
                                10,     // entriesCount 
                                1);     // free
                        try {
                            System.out.println("sleep for a while: "+smallSleep+"ms");
                            Thread.sleep(smallSleep);
                        } catch (InterruptedException e) {
                            System.err.print("EXCEPTION: ");
                            e.printStackTrace();
                        }
                    	
                    	// 跑到这边来的话，就说明上面的正常。reset as 0
                    	nException = 0;
                    	
                        if (pkgsResponse != null) {
                        	// TODO: for testing
        					System.out.println(pkgsResponse.toString());
        					
                        	nAppCount = pkgsResponse.getAppCount();
                        	if (nAppCount == 0) {
                        		// TODO: write to file
                        		System.err.print("WARN: pkgsResponse is 0\n\n");
                        		break;
                        	}
                        	
                        	// 遍历pkgsResponse
                    		for (appIndex = 0; appIndex < nAppCount; appIndex++) {
                    			/**
                				 * get app info & extendedInfo
                				 */
                				app = pkgsResponse.getApp(appIndex);
                				pkgName = app.getPackageName();
                				assetId = app.getId();
                				extendedInfo = app.getExtendedInfo();
                				oldCategory = extendedInfo.getCategory();
                				if (oldCategory != "") {
                					categoryInDisk = convertCategoryTitle(oldCategory);
                				}
                				
                				apkName = totalIndex + "-@-" + pkgName + ".apk";
                				apkPath = categoryInDisk + "/" + apkName;
                				
                				/**
                				 * compare app info with db
                				 */
                	            
                	            /**
                				 * download one app
                				 */
                				result = downloadApk(apkPath, assetId);
                				if (result == -1) {
                					continue;
                				}
                				
                				/**
                				 * compare md5 with db
                				 */
                				
                				currentIndex++;	totalIndex++;
                    		}
                        	
                    		// next getAppsByPublisher
                        	if (nAppCount >= 10) {	// 照理我就query了10个，应该不会 > 的                        		
                        		onePubIndex += nAppCount;	// 应该就是 +10 的了
                        	} else if (nAppCount > 0) {	// 0 < nAppCount < 10
                        		break;
                        	}
                        	
                        // end for "if"
                        } else {
                        	// TODO: write to file
                        	// TODO: 这种情况应该是要重试
                        	System.err.print("WARN: pkgsResponse is null\n\n");
							break;
						}
                        
                	} catch (RuntimeException e) {	// 针对getAppsByPublisher的
                									// also will catch newDownloadApks()
                		System.err.print("EXCEPTION for "+pubName+": ");
                		e.printStackTrace();
                		
                		FileWriter errorPubWriter = null;
                        try {
                            errorPubWriter = new FileWriter( LogFiles.errorPubs, true );
                            errorPubWriter.write( pubName + "\n" );
                        } catch (IOException e1) {
                        	System.err.print("EXCEPTION: ");
                    		e1.printStackTrace();
                        } finally {
                            if (errorPubWriter != null) {
                                errorPubWriter.close();
                            }
                        }
                		
                		// 不好连续异常3次。
                		nException++;
                		System.err.print("WARN: Current exception number is: " + nException + "\n\n");
                		if (nException > 2) {
                			// exit when has reached 3rd exception
                			System.out.print("==> We are going to exit!\n");
                			exitClean();
                			System.exit(1);
                		}
                		
                		// go to next publisher
                		break;
                	}
                    
                }   // end of this "while"
                
                //
                // 4. next pub
                //
                nPubNum++;
                
            }   // end of outside "while"
            
            System.out.print("==> Have done all for " + pubFile + "\n");
            pubfileReader.close();
            
        } catch (Exception e) {
        	System.err.print("EXCEPTION: ");
    		e.printStackTrace();
        } finally {
        }
        
        exitClean();
	}
	
	/**
	 * case 3
	 * automatically download by query keyword
	 * 
	 * TODO 还用的newDownloadApks呢，category也需要convertCategory()来转化
	 * TODO currentIndex和totalIndex都没有啊
	 */
	private void executeByQueryWord() {
		int nAppCount = 0;
        /**
         * getAppsByQuery出现java.lang.RuntimeException的次数
         */
        int nException = 0;
        /**
         * 成功fetch的次数
         * 用来统计是否到everyFetchNum了
         * 可以一直计数下去，因为是取余数
         */
        int nFetchNum = 0;
        
        readLogFiles();
        
        AssetIdFetcher pkgsFetcher = new AssetIdFetcher();
        AppsResponse pkgsResponse = null;
        
        while (startIndex < maxAppIndex) {
            //
            // 1. judge to sleep for a big time
            //
            if (nFetchNum != 0 && nFetchNum%everyFetchNum == 0) {
                try {
                    System.out.print("==> sleep for a big time: "+bigSleep+"\n\n");
                    Thread.sleep(bigSleep);
                    
                } catch (InterruptedException e) {
                	System.err.println("EXCEPTION: " + e);
                }
            }
            
            //
            // 2. judge whether now is everyFetchNum or not
            //
            if (nFetchNum % everyFetchNum == 0) {
                System.out.print("==> crawlerLogin\n\n");
                /**
                 * @deprecated
                 * @since version 0.6
                 */
//              oldCrawlerLogin();
                /**
                 * @since version 0.6
                 */
                loginAllSessions();
            }
            
            //
            // 3. fetch
            //
        	try {
        	    /**
                 * @since version 0.6
                 */
        	    System.out.println("\n==> current SessionIndex is "+nextSessionIndex);
                moveToNextSession();
                System.out.println("==> nextSessionIndex will be "+nextSessionIndex);
        		pkgsResponse = pkgsFetcher.getAppsByQueryWord(
    					crawlerSession.getMarketSession(),
    					this.queryWord,
    					this.startIndex,
    					1);	// free
    			
    			// 跑到这边来的话，就说明上面的正常。reset as 0
            	nException = 0;
    			
    			if ( pkgsResponse != null ) {
    				/**
    				 * added in 2.3
    				 */
//    				System.out.println(pkgsResponse.toString());
    				
    				nAppCount = pkgsResponse.getAppCount();
    				
    				if (nAppCount != 0) {
    					newDownloadApks(pkgsResponse, nAppCount);

    					startIndex += nAppCount;
    					
    				} else { // nAppCount is 0
    					System.err.print("WARN: nAppCount is 0\n\n");
    					break;
    				}
    				
    			} else { // pkgsResponse is null
                	// TODO: 这种情况应该是要重试
                	System.err.print("WARN: pkgsResponse is null\n\n");
    				break;
    			}
    			
        	} catch (RuntimeException e) {	// 针对getAppsByPublisher的
				// also will catch newDownloadApks()
				System.err.print("ERROR: RuntimeException for " + this.currentIndex + "\n");
				e.printStackTrace();
				
				// 不好连续异常3次。
				nException++;
				System.err.print("WARN: Current exception number is: " + nException + "\n\n");
				if (nException > 2) {
					// exit when has reached 3rd exception
					System.out.print("==> We are going to exit!\n");
					System.exit(1);
				}
			}
        	
        	//
        	// 4. next fetch
			//
            nFetchNum++;
		
        } // end of "while"
        
        System.out.print("\n==> Have done all for queryword: " + queryWord + "\n");
        exitClean();
	}
	
	/**
	 * case 4</br>
	 * format of sqlfile must be:</br>
	 * SELECT pkgName, category, curVersionCode, curVersionName FROM GMarketInfo ORDER BY lastCheckTime</br>
	 * 最后不要加分号</br>
	 * 跟executeByOnePkg, executeByPkgFile区别是：它只能爬db里有的pkgName
	 */
	private void executeBySqlPkgFile() {
		/**
		 * store results from getAppCount()
		 */
		int nAppCount = 0;
		/**
		 * numbers of calling getAppsByPackageName(), start from 0
		 * --> numbers of queryRS, start from 1
		 * --> from nAppNum to nAppRow
		 */
		int nAppRow = 0;
		/**
         * getAppsByPackageName出现java.lang.RuntimeException的次数
         */
        int nException = 0;
		int result = 0;
		App app = null;
		AssetIdFetcher pkgFetcher = new AssetIdFetcher();
		AppsResponse pkgResponse = null;
		String assetId, apkName, apkPath, apkComePath, apkTodoPath;
		String curVersionName, hisVersionName;
		String creator, title, rating;
		int curVersionCode, hisVersionCode, ratingsCount;
		/**
		 * -1: ERROR
		 * 0:  no
		 * 1:  yes
		 */
		int isToCrawl;

		String updateCmd;
		int updateRows;
		String md5Cmd = "SELECT md5 FROM ApkBin WHERE md5 = ?";
		
		boolean justUpdateTime;
		
		readLogFiles();
		
		try {
		    // TODO queryRS拿到的数据集不会变吗，不会受下面更新的影响吗
		    // TODO 是因为每次都调最旧的来吗
		    // TODO 它会单独创建个mirror吗
			queryST = queryConn.createStatement();
			// turn on cursor: set each cache size
			queryST.setFetchSize(50);
			
			queryRS = queryST.executeQuery(sqlCmd);
			while (queryRS.next()) {
				nAppRow++;
				
				// judge whether to exit
				// TODO not yet testing
				if (nAppRow >= this.totalCount) {
				    System.out.println("\n==> Have reached totalCount for pkg query: "+totalCount);
				    break;
				}
				
	            // judge to sleep for a big time
	            if (nAppRow != 1 && nAppRow%everyAppNum == 0) {
	                try {
	                    System.out.print("\n==> sleep for a big time: "+bigSleep+"ms\n");
	                    Thread.sleep(bigSleep);
	                    
	                } catch (InterruptedException e) {
	                	System.err.print("EXCEPTION: ");
	        			e.printStackTrace();
	                }
	            }
	            
	            // judge whether now is everyAppNum or not
	            if (nAppRow == 1 || nAppRow % everyAppNum == 0) {
	                System.out.print("\n==> crawlerLogin\n");
	                /**
                     * @deprecated
                     * @since version 0.6
                     */
//                  oldCrawlerLogin();
                    /**
                     * @since version 0.6
                     */
                    loginAllSessions();
	            }
	            
	            isToCrawl = -1;
	            updateRows = -1;
				this.pkgName = queryRS.getString(1);
				this.categoryInDisk = queryRS.getString(2);	// '2_DEMO'
				curVersionCode = queryRS.getInt(3);		// 29
				curVersionName = queryRS.getString(4);	// '1.3.1 Free' OR '@string/app_version'
				System.out.println("\n--> "+nAppRow+" Row: "+this.pkgName+" "+
				this.categoryInDisk+" "+curVersionCode+" "+curVersionName);
				
				try {
				    /**
	                 * @since version 0.6
	                 */
				    System.out.println("\n==> current SessionIndex is "+nextSessionIndex);
                    moveToNextSession();
                    System.out.println("==> nextSessionIndex will be "+nextSessionIndex);
					
					/**
			         * for each getAppsByXXX(), then sleep
			         */
                    pkgResponse = pkgFetcher.getAppsByPackageName(
                            crawlerSession.getMarketSession(),
                            1,     //entriesCount
                            this.pkgName);
			        try {
			            System.out.println("sleep for a while: "+smallSleep+"ms");
			            Thread.sleep(smallSleep);
			        } catch (InterruptedException e) {
			            System.err.print("EXCEPTION: ");
			            e.printStackTrace();
			        }
				} catch (RuntimeException e) {    // 针对getAppsByPackageName的
		            System.err.print("EXCEPTION: "+this.pkgName+": ");
		            e.printStackTrace();
		            
		            // 不好连续异常3次。
		            nException++;
		            System.err.print("WARN: Current exception number is: " + nException + "\n\n");
		            if (nException >= 3) {
		                // exit when has reached 3rd exception
		                System.out.print("==> Have reached max exception, going to exit!\n");
		                exitClean();
		                System.exit(1);
		            }
		            
		            continue;
		        }
				// 跑到这边来的话，就说明上面的正常。reset as 0
            	nException = 0;
            	justUpdateTime = false;
            	
				if (pkgResponse != null) {
					// TODO: for testing
//					System.out.println(pkgResponse.toString());
					
					nAppCount = pkgResponse.getAppCount();
					if (nAppCount != 0) {
						// get app info & extendedInfo
						app = pkgResponse.getApp(0);
						assetId = app.getId();
						hisVersionCode = app.getVersionCode();
						hisVersionName = app.getVersion();
						creator = app.getCreator();
						title = app.getTitle();
						rating = app.getRating();
						ratingsCount = app.getRatingsCount();
						
						// judge whether to crawl or not
						if (curVersionCode < hisVersionCode) {
							isToCrawl = 1;
							System.out.println("INFO: curVersionCode("+curVersionCode+
							") is smaller than market versionCode("+hisVersionCode+")");
							
						} else if (curVersionCode == hisVersionCode) {
							System.out.println("INFO: curVersionCode is the same");
							if (curVersionName.contains(hisVersionName)) {
								isToCrawl = 0;
								System.out.println("INFO: curVersionName("+curVersionName+
								") contains hisVersionName("+hisVersionName+")");
							} else {
								isToCrawl = 1;
								System.out.println("WARN: curVersionName("+curVersionName+
								") doesnt contain hisVersionName("+hisVersionName+")");
							}
							
						} else {
							// impossible to be larger, unless author rollback
							System.err.println("ERROR: curVersionCode("+curVersionCode+
							") is bigger than market versionCode("+hisVersionCode+")");
							isToCrawl = 1;
						}
						
						System.out.println("isToCrawl is "+isToCrawl);
						if (isToCrawl == 1) {
							/**
							 * yes to crawl, also need to insert or update
							 */
							apkName = totalIndex + "-@-" + this.pkgName + ".apk";
							apkPath = categoryInDisk + "/" + apkName;
							apkComePath = ROOTCOME + apkPath;
							apkTodoPath = ROOTTODO + apkPath;
							
							result = downloadApk(apkPath, assetId);
							if (result == -1) {
								System.out.print("\nDidn't successfully crawl pkg: "+this.pkgName+"\n");
								/**
								 * still update:
								 * creator, lastCheckTime, title, rating, ratingsCount
								 */
								updateCmd = "UPDATE GMarketInfo SET creator = ?, " +
                                "lastCheckTime = statement_timestamp(), " +
                                "title = ?, rating = ?, " +
                                "ratingsCount = ? WHERE pkgName = ?";
                                updateST = updateConn.prepareStatement(updateCmd);
                                updateST.setString(1, creator);
                                updateST.setString(2, title);
                                updateST.setString(3, rating);
                                updateST.setInt(4, ratingsCount);
                                updateST.setString(5, this.pkgName);
                                // for print
                                updateCmd = "UPDATE GMarketInfo SET creator = "+creator+", " +
                                        "lastCheckTime = statement_timestamp(), " +
                                        "title = "+title+", rating = "+rating+", " +
                                        "ratingsCount = "+ratingsCount+" WHERE pkgName = "+this.pkgName+"";
                                System.out.println(updateCmd);
                                // check result
                                updateRows = updateST.executeUpdate();
                                if (updateRows != 1) {
                                    System.err.println("ERROR: "+this.pkgName+
                                    ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                    exitClean();
                                    System.exit(1);
                                }
                                updateConn.commit();
                                System.out.println("INFO: UPDATE ok!");
                                
                                // next pkg
								continue;
							}
							
							// calculate md5
							String md5 = "";
							try {
                                md5 = MD5Checksum.getMD5Checksum(apkComePath);
                                System.out.println("INFO: md5: "+md5);
                            } catch (Exception e) {
                                System.err.print("EXCEPTION: ");
                                e.printStackTrace();
                                // must succeed, otherwise exit
                                exitClean();
                                System.exit(1);
                            }
                            
                            // get file size
                            int apksize = 0;
                            apksize = getApkSize(apkComePath);
                            if (apksize == -1) {
                                System.err.println("ERROR: "+apkComePath+" path is wrong!");
                                exitClean();
                                System.exit(1);
                            } else if (apksize == 0) {
                                System.err.println("ERROR: "+apkComePath+" size is 0");
                            }
                            System.out.println("INFO: apksize: "+apksize);
                            
                            // query this md5
                            onefullQueryST = updateConn.prepareStatement(md5Cmd);
                            onefullQueryST.setString(1, md5);
                            onefullRS = onefullQueryST.executeQuery();
                            if (onefullRS.next()) {
                                /**
                                 * already exists, should seldom happen
                                 */
                                System.err.println("WARN: pkg("+this.pkgName+")'s md5("+
                                md5+") already in db, just keep "+apkComePath);
                                
                            } else {
                            	System.out.println("INFO: No this md5("+md5+") in db");
                                /**
                                 * no this md5 in db, first insert ApkBin
                                 */
                                String mvCmd = "mv "+apkComePath+" "+apkTodoPath;// must ensure dir exists
                                System.out.println(mvCmd);
                                Process p = Runtime.getRuntime().exec(mvCmd);
                                int exitCode = p.waitFor();
                                if (exitCode != 0) {
                                	System.err.println("ERROR: mv "+apkComePath+"failed, exiCode is "+exitCode);
                                	exitClean();
                                    System.exit(1);
                                }
                                System.out.println("INFO: mv ok!");
                                
                                String apkPathinDB = "apkTodo/"+apkPath;
                                // TODO 当前仅仅新的apk才更新assetId
                                // 这样也保证了assetId肯定是跟md5对应的
                                updateCmd = "INSERT INTO ApkBin (md5, pkgName, apkPath, " +
                                "apkSize, versionCode, versionName, assetId) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
                                updateST = updateConn.prepareStatement(updateCmd);
                                updateST.setString(1, md5);
                                updateST.setString(2, this.pkgName);
                                updateST.setString(3, apkPathinDB);
                                updateST.setInt(4, apksize);
                                updateST.setInt(5, hisVersionCode);
                                updateST.setString(6, hisVersionName);
                                updateST.setString(7, assetId);
                                // for print
                                updateCmd = "INSERT INTO ApkBin (md5, pkgName, apkPath, " +
                                "apkSize, versionCode, versionName, assetId) " +
                                "VALUES ("+md5+", "+this.pkgName+", "+apkPathinDB+", "+apksize+
                                ", "+hisVersionCode+", "+hisVersionName+", "+assetId+")";
                                System.out.println(updateCmd);
                                // check result
                                updateRows = updateST.executeUpdate();
                                if (updateRows != 1) {
                                    System.err.println("ERROR: "+this.pkgName+
                                    ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                    exitClean();
                                    System.exit(1);
                                }
                                
                                // only insert will count
                                // TODO: how to roll back this
                                currentIndex++; totalIndex++;
                                System.out.println("INFO: Next currentIndex: "+currentIndex+", totalIndex: "+totalIndex);
                            }
                            
                            /**
                             * both should update:
                             * md5, creator, lastCheckTime, lastUpdateTime, curApkSize,
                             * curVersionCode, curVersionName, title, rating, ratingsCount
                             * 
                             * transaction
                             */
                            updateCmd = "UPDATE GMarketInfo SET " +
                            "md5 = ?, creator = ?, " +
                            "lastCheckTime = statement_timestamp(), " +
                            "lastUpdateTime = statement_timestamp(), " +
                            "curApkSize = ?, curVersionCode = ?, " +
                            "curVersionName = ?, title = ?, rating = ?, " +
                            "ratingsCount = ? WHERE pkgName = ?";
                            updateST = updateConn.prepareStatement(updateCmd);
                            updateST.setString(1, md5);
                            updateST.setString(2, creator);
                            updateST.setInt(3, apksize);
                            updateST.setInt(4, hisVersionCode);
                            updateST.setString(5, hisVersionName);
                            updateST.setString(6, title);
                            updateST.setString(7, rating);
                            updateST.setInt(8, ratingsCount);
                            updateST.setString(9, this.pkgName);
                            // for print
                            updateCmd = "UPDATE GMarketInfo SET " +
                            "md5 = "+md5+", creator = "+creator+", " +
                            "lastCheckTime = statement_timestamp(), " +
                            "lastUpdateTime = statement_timestamp(), " +
                            "curApkSize = "+apksize+", curVersionCode = "+hisVersionCode+", " +
                            "curVersionName = "+hisVersionName+", title = "+title+", rating = "+rating+", " +
                            "ratingsCount = "+ratingsCount+" WHERE pkgName = "+this.pkgName+"";
                            System.out.println(updateCmd);
                            // check result
                            updateRows = updateST.executeUpdate();
                            if (updateRows != 1) {
                                System.err.println("ERROR: "+this.pkgName+
                                ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                exitClean();
                                System.exit(1);
                            }
                            // final commit for two statements
                            updateConn.commit();
                            System.out.println("INFO: insert and update ok");
							
						} else {
							/**
							 * isToCrawl == 0
							 * no to crawl, just update:
							 * creator, lastCheckTime, curVersionName,
							 * title, rating, ratingsCount
							 */
							updateCmd = "UPDATE GMarketInfo SET creator = ?, " +
							"lastCheckTime = statement_timestamp(), " +
							"curVersionName = ?, title = ?, rating = ?, " +
							"ratingsCount = ? WHERE pkgName = ?";
							updateST = updateConn.prepareStatement(updateCmd);
							updateST.setString(1, creator);
							updateST.setString(2, hisVersionName);
							updateST.setString(3, title);
							updateST.setString(4, rating);
							updateST.setInt(5, ratingsCount);
							updateST.setString(6, this.pkgName);
							// for print
                            updateCmd = "UPDATE GMarketInfo SET creator = "+creator+", " +
							"lastCheckTime = statement_timestamp(), " +
							"curVersionName = "+hisVersionName+", title = "+title+", rating = "+rating+", " +
							"ratingsCount = "+ratingsCount+" WHERE pkgName = "+this.pkgName+"";
                            System.out.println(updateCmd);
							// check result
							updateRows = updateST.executeUpdate();
							if (updateRows != 1) {
								System.err.println("ERROR: "+this.pkgName+
								": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
								exitClean();
								System.exit(1);
							}
							updateConn.commit();
							System.out.println("INFO: update ok");
						}
					    
					} else {	// pkgResponse is 0
						System.err.print("WARN: "+this.pkgName+": nAppCount is 0\n");
						justUpdateTime = true;
					}
					
				} else {		// pkgResponse is null
					System.err.print("WARN: "+this.pkgName+": pkgResponse is null\n");
					justUpdateTime = true;
				}
				
				if (justUpdateTime) {
					System.out.println("justUpdateTime is "+justUpdateTime);
					
					updateCmd = "UPDATE GMarketInfo SET " +
					"lastCheckTime = statement_timestamp() WHERE pkgName = ?";
					updateST = updateConn.prepareStatement(updateCmd);
					updateST.setString(1, this.pkgName);
					
					// for print
					updateCmd = "UPDATE GMarketInfo SET " +
					"lastCheckTime = statement_timestamp() WHERE pkgName = "+this.pkgName;
                    System.out.println(updateCmd);
					// check result
					updateRows = updateST.executeUpdate();
					if (updateRows != 1) {
						System.err.println("ERROR: "+this.pkgName+
						": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
						exitClean();
						System.exit(1);
					}
					updateConn.commit();
					System.out.println("INFO: update ok");
				}
				
				// TODO: test for only one time
				// break;
			}
			// end of while
			System.out.println("\n==> Have done all pkgs");
			
		} catch (Exception e) {
        	System.err.print("EXCEPTION: "+this.pkgName+": ");
            e.printStackTrace();
            exitClean();
            System.exit(1);
        }
		
		exitClean();
	}

    /**
     * case 5</br>
     * format of sqlfile must be:</br>
     * <code>SELECT creator FROM GPublisher WHERE insertNum = 3 ORDER BY lastCheckTime</code></br>
     * <code>SELECT creator FROM GPublisher ORDER BY lastCheckTime</code></br>
     * 最后不要加分号</br>
     * 校验也拿executeByOneCat()来校验下
     * 
     * @see <code>newexecuteByPubFile()</code>, <code>executeByPubFile()</code>,
     *      <code>executeBySqlPkgFile</code>, <code>executeByOneCat()最新的, 更像，因为取db里已有的app信息出来比较时。最多就是还要update GPublisher</code>
     * @return errorCode
     * 0: ok
     * 1: have reached max attempts, appsResponse == null OR nAppCount == 0
     * 2: max RuntimeException
     * 3: get md5 or file size error, mv apkComePath error
     * 4: sql exception
     * 5: unknow error
     * 6: category error
     */
	private int executeBySqlPubFile() {
	    /**
         * store results from getAppCount()
         */
        int nAppCount = 0;
        /**
         * app index for one pub, 在一个pkgsResponse的总index
         */
        int appIndexInOnePub;
        /**
         * 用于遍历每次的pkgsResponse, 一次只有10个
         */
        int appIndex = 0;
        /**
         * 用来统计是否到everyPubNum了
         * 可以一直计数下去，因为是取余数
         * 
         * --> nPubNum to nPubRow
         * @see nAppRow in executeBySqlPkgFile()
         */
        int nPubRow = 0;
        /**
         * "\n--> "
         */
        int nRow = 0;
        /**
         * getAppsByPublisher出现java.lang.RuntimeException的次数
         */
        int nException = 0;
        
        AssetIdFetcher pkgsFetcher;
        
        int result = 0;
        App app = null;
        ExtendedInfo extendedInfo = null;
        String ourAssertId, hisAssetId, pkgName, oldCategory, apkName, apkPath;
        String hisVersionName;
        String creator, title, rating;
        String downloadsCountText, newCategory;
        int hisVersionCode, ratingsCount, hisApkSize;
        int ourVersionCode, ourApkSize;
        String apkComePath, apkTodoPath;
        /**
         * -1: ERROR
         * 0:  no
         * 1:  yes
         */
        int isToCrawl;
        boolean isNewtoDB;
        
        String queryCmd = "SELECT assetId, curVersionCode, curApkSize " +
        "from ApkBin, GMarketInfo " +
        "WHERE GMarketInfo.pkgName = ? AND ApkBin.md5 = GMarketInfo.md5";
        String updateCmd;
        int updateRows;
        String md5Cmd = "SELECT md5 FROM ApkBin WHERE md5 = ?";
        
        readLogFiles();
        pkgsFetcher = new AssetIdFetcher();
        
        try {
            queryST = queryConn.createStatement();
            queryST.setFetchSize(50);
            
            queryRS = queryST.executeQuery(sqlCmd);
            while (queryRS.next()) {
                // judge whether to exit
                // TODO not yet testing
                if (nPubRow >= this.totalCount) {
                    System.out.println("\n==> Have reached totalCount for pub query: "+totalCount);
                    break;
                }
                
                nPubRow++;
                
                // judge to sleep for a big time
                if (nPubRow != 1 && nPubRow%everyPubNum == 0) {
                    try {
                        System.out.print("==> sleep for a big time: "+bigSleep+"ms\n");
                        Thread.sleep(bigSleep);
                        
                    } catch (InterruptedException e) {
                        System.err.print("EXCEPTION: ");
                        e.printStackTrace();
                    }
                }
                
                // judge whether now is everyPubNum or not
                if (nPubRow == 1 || nPubRow % everyPubNum == 0) {
                    System.out.print("\n==> crawlerLogin\n");
                    loginAllSessions();
                }
                
                // fetch one pub
                pubName = queryRS.getString(1);
                System.out.println("\n==> "+nPubRow+" Pub: "+pubName);
                appIndexInOnePub = 0;   // TODO needed?
                
                /**
                 * update this publisher
                 */
                updateCmd = "UPDATE GPublisher SET " +
                "lastCheckTime = statement_timestamp() WHERE creator = ?";
                updateST = updateConn.prepareStatement(updateCmd);
                updateST.setString(1, pubName);
                // for print
                updateCmd = "UPDATE GPublisher SET " +
                "lastCheckTime = statement_timestamp() WHERE creator = "+pubName;
                System.out.println(updateCmd);
                // check result
                updateRows = updateST.executeUpdate();
                if (updateRows != 1) {
                    System.err.println("ERROR: "+pubName+
                    ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                    return 4;
                }
                // final commit for two statements
                updateConn.commit();
                System.out.println("INFO: update GPublisher ok");
                
                /**
                 * use new session to crawl
                 */
                AppsResponse pkgsResponse = null;
                while (appIndexInOnePub < maxAppOnePub) {// 因为每次只query 10个
                    /**
                     * get pkgsResponse
                     */
                    try {
                        System.out.println("\n==> current SessionIndex is "+nextSessionIndex);
                        moveToNextSession();
                        System.out.println("==> nextSessionIndex will be "+nextSessionIndex);
                        
                        // for each getAppsByXXX(), then sleep
                        pkgsResponse = pkgsFetcher.getAppsByPublisher(
                                crawlerSession.getMarketSession(),
                                pubName,
                                appIndexInOnePub,
                                10,     // entriesCount 
                                1);     // free
                        try {
                            System.out.println("sleep for a while: "+smallSleep+"ms");
                            Thread.sleep(smallSleep);
                        } catch (InterruptedException e) {
                            System.err.print("EXCEPTION: ");
                            e.printStackTrace();
                        }
                        
                    } catch (RuntimeException e) {    // 针对getAppsByPublisher的
                        System.err.print("EXCEPTION for "+pubName+": ");
                        e.printStackTrace();
                        
                        // 不好连续异常3次。
                        nException++;
                        System.err.print("WARN: Current exception number is: " + nException + "\n\n");
                        if (nException >= 3) {
                            // exit when has reached 3rd exception
                            System.out.print("==> Have reached max exception, going to exit!\n");
                            return 2;
                        }
                        
                        // go to next publisher
                        break;
                    }
                    // 跑到这边来的话，就说明上面的正常。reset as 0
                    nException = 0;
                    
                    /**
                     * analyze pkgsResponse
                     */
                    if (pkgsResponse != null) {
                        // TODO: for testing
                        //System.out.print(pkgsResponse.toString());
                        
                        // exclude nAppCount == 0
                        nAppCount = pkgsResponse.getAppCount();
                        if (nAppCount == 0) {
                            System.err.print("WARN: "+this.pubName+": nAppCount is 0\n\n");
                            break;
                        }
                        
                        /**
                         * 遍历pkgsResponse
                         */
                        for (appIndex = 0; appIndex < nAppCount; appIndex++) {
                            nRow++;
                            // TODO 还不是很准
                            currentIndex++;
                            
                            /**
                             * set default value
                             */
                            isToCrawl = -1;
                            isNewtoDB = false;
                            ourAssertId = null;
                            ourVersionCode = -1;
                            ourApkSize = -1;
                            
                            /**
                             * get app info & extendedInfo
                             */
                            app = pkgsResponse.getApp(appIndex);
                            pkgName = app.getPackageName();
                            hisAssetId = app.getId();
                            hisVersionCode = app.getVersionCode();
                            hisVersionName = app.getVersion();
                            creator = app.getCreator();
                            title = app.getTitle();
                            rating = app.getRating();
                            ratingsCount = app.getRatingsCount();
                            
                            extendedInfo = app.getExtendedInfo();
                            hisApkSize = extendedInfo.getInstallSize();
                            downloadsCountText = extendedInfo.getDownloadsCountText();
                            oldCategory = extendedInfo.getCategory();
                            
                            newCategory = convertCategoryTitle(oldCategory);
                            if (newCategory.equals("0_UNKNOWN")) {
                                System.err.println("ERROR: convert failed for newCategory: "+newCategory);
                                return 6;
                            }
                            
                            apkName = totalIndex + "-@-" + pkgName + ".apk";
                            apkPath = newCategory + "/" + apkName;
                            
                            // print rows
                            System.out.println("\n--> "+nRow+": "+pkgName+", "+hisVersionCode+", "+newCategory);
                            
                            /**
                             * compare app info with db
                             * 基本就跟<code>executeByOneCat()</code>一样的吧
                             */
                            onefullQueryST = queryConn.prepareStatement(queryCmd);
                            onefullQueryST.setString(1, pkgName);
                            onefullRS = onefullQueryST.executeQuery();
                            if (onefullRS.next()) {
                                /**
                                 * db里有这个pkgname
                                 * 为啥我没用curVersionName，就是因为当时脚本解析manifest，得到的那个versionName不一定是精确的
                                 */
                                System.out.println("INFO: "+pkgName+" exists in current db");
                                ourAssertId = onefullRS.getString(1);
                                ourVersionCode = onefullRS.getInt(2);
                                ourApkSize = onefullRS.getInt(3);
                                if (ourAssertId != null && !ourAssertId.equals("")) {
                                    if (ourAssertId.equals(hisAssetId)) {
                                        isToCrawl = 0;
                                        System.out.println("INFO: assetId is the same: "+hisAssetId);
                                    } else {
                                        isToCrawl = 1;
                                        System.out.println("INFO: our assetId is: "+ourAssertId+
                                        ", his assetId is: "+hisAssetId);
                                    }
                                } else {
                                    if (ourVersionCode != 0) {
                                        if (ourVersionCode != hisVersionCode) {
                                            isToCrawl = 1;
                                            System.out.println("INFO: our versionCode is: "+ourVersionCode+
                                            ", his versionCode is: "+hisVersionCode);
                                        } else {
                                            System.out.println("INFO: versionCode is the same: "+ourVersionCode);
                                            if (ourApkSize != 0) {
                                                if (ourApkSize == hisApkSize) {
                                                    isToCrawl = 0;
                                                    System.out.println("INFO: ApkSize is the same: "+ourApkSize);
                                                } else {
                                                    isToCrawl = 1;
                                                    System.out.println("INFO: ourApkSize is: "+ourApkSize+
                                                    ", hisApkSize is: "+hisApkSize);
                                                }
                                            } else {
                                                isToCrawl = 1;
                                                System.err.println("WARN: ourApkSize is 0, pkgName: "+pkgName);
                                            }
                                        }
                                    } else {
                                        isToCrawl = 1;
                                        System.err.println("WARN: our versionCode is 0, pkgName: "+pkgName);
                                    }
                                }
                                
                            } else {
                                /**
                                 * db里没有这个pkgName
                                 */
                                isToCrawl = 1;
                                isNewtoDB = true;
                                System.out.println("INFO: "+pkgName+" is new");
                            }
                            System.out.println("INFO: isToCrawl is "+isToCrawl);
                            
                            /**
                             * download one app
                             * !!!
                             */
                            if (isToCrawl == 1) {
                                // yes to crawl, also need to insert or update basic new md5
                                apkComePath = ROOTCOME + apkPath;
                                apkTodoPath = ROOTTODO + apkPath;
                                
                                result = downloadApk(apkPath, hisAssetId);
                                if (result == -1) {
                                    System.err.print("ERROR: Didn't successfully crawl: "+apkPath+"\n");
                                    continue;
                                }
                                
                                // calculate md5
                                String md5 = "";
                                try {
                                    md5 = MD5Checksum.getMD5Checksum(apkComePath);
                                    System.out.println("INFO: md5: "+md5);
                                } catch (Exception e) {
                                    System.err.print("EXCEPTION: ");
                                    e.printStackTrace();
                                    return 3;
                                }
                                
                                // get file size
                                int apksize = 0;
                                apksize = getApkSize(apkComePath);
                                if (apksize == -1) {
                                    System.err.println("ERROR: "+apkComePath+" path is wrong!");
                                    return 3;
                                } else if (apksize == 0) {
                                    System.err.println("ERROR: "+apkComePath+" size is 0");
                                }
                                System.out.println("INFO: apksize: "+apksize);
                                
                                // query this md5，虽然认为不可能有，但还是得query下
                                onefullQueryST = queryConn.prepareStatement(md5Cmd);
                                onefullQueryST.setString(1, md5);
                                onefullRS = onefullQueryST.executeQuery();
                                if (onefullRS.next()) {
                                    // already exists, should seldom happen
                                    System.err.println("ERROR: md5("+md5+") already in the db");
                                    
                                } else {
                                    System.out.println("INFO: No this md5("+md5+") in db");
                                    // no this md5 in db, first insert ApkBin
                                    String mvCmd = "mv "+apkComePath+" "+apkTodoPath;// must ensure dir exists
                                    System.out.println(mvCmd);
                                    Process p = Runtime.getRuntime().exec(mvCmd);
                                    int exitCode = p.waitFor();
                                    if (exitCode != 0) {
                                        System.err.println("ERROR: mv "+apkComePath+"failed, exiCode is "+exitCode);
                                        return 3;
                                    }
                                    System.out.println("INFO: mv ok!");
                                    
                                    String apkPathinDB = "apkTodo/"+apkPath;
                                    // TODO 当前仅仅新的apk才更新assetId
                                    // 这样也保证了assetId肯定是跟md5对应的
                                    updateCmd = "INSERT INTO ApkBin (md5, pkgName, apkPath, " +
                                    "apkSize, versionCode, versionName, assetId) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
                                    updateST = updateConn.prepareStatement(updateCmd);
                                    updateST.setString(1, md5);
                                    updateST.setString(2, pkgName);
                                    updateST.setString(3, apkPathinDB);
                                    updateST.setInt(4, apksize);
                                    updateST.setInt(5, hisVersionCode);
                                    updateST.setString(6, hisVersionName);
                                    updateST.setString(7, hisAssetId);
                                    // for print
                                    updateCmd = "INSERT INTO ApkBin (md5, pkgName, apkPath, " +
                                    "apkSize, versionCode, versionName, assetId) " +
                                    "VALUES ("+md5+", "+pkgName+", "+apkPathinDB+", "+apksize+
                                    ", "+hisVersionCode+", "+hisVersionName+", "+hisAssetId+")";
                                    System.out.println(updateCmd);
                                    // check result
                                    updateRows = updateST.executeUpdate();
                                    if (updateRows != 1) {
                                        System.err.println("ERROR: "+pkgName+
                                        ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                        return 4;
                                    }
                                    
                                    if (isNewtoDB) {
                                        // insert new pkg(thus new md5)
                                        // 还是增加rating和ratingsCount吧
                                        // TODO no need to differentiate new and popular any more
                                        updateCmd = "INSERT INTO GMarketInfo (pkgName, md5, creator, " +
                                        "insertTime, lastCheckTime, lastUpdateTime, curApkSize, " +
                                        "curVersionCode, curVersionName, category, title, " +
                                        "rating, ratingsCount, downloadsCountText) " +
                                        "VALUES(?, ?, ?, statement_timestamp(), statement_timestamp(), " +
                                        "statement_timestamp(), ?, ?, ?, ?, ?, ?, ?, ?)";
                                        updateST = updateConn.prepareStatement(updateCmd);
                                        updateST.setString(1, pkgName);
                                        updateST.setString(2, md5);
                                        updateST.setString(3, creator);
                                        updateST.setInt(4, apksize);
                                        updateST.setInt(5, hisVersionCode);
                                        updateST.setString(6, hisVersionName);
                                        updateST.setString(7, newCategory);
                                        updateST.setString(8, title);
                                        updateST.setString(9, rating);
                                        updateST.setInt(10, ratingsCount);
                                        updateST.setString(11, downloadsCountText);
                                        // for print
                                        updateCmd = "INSERT INTO GMarketInfo (pkgName, md5, creator, " +
                                        "insertTime, lastCheckTime, lastUpdateTime, curApkSize, " +
                                        "curVersionCode, curVersionName, category, title, " +
                                        "rating, ratingsCount, downloadsCountText) " +
                                        "VALUES("+pkgName+", "+md5+", "+creator+", statement_timestamp(), statement_timestamp(), " +
                                        "statement_timestamp(), "+apksize+", "+hisVersionCode+", "+hisVersionName+", "+
                                        newCategory+", "+title+", "+rating+", "+ratingsCount+", "+downloadsCountText+")";
                                        System.out.println(updateCmd);
                                        // check result
                                        updateRows = updateST.executeUpdate();
                                        if (updateRows != 1) {
                                            System.err.println("ERROR: insert "+pkgName+
                                            ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                            return 4;
                                        }
                                        
                                    } else {
                                        /**
                                         * update new md5(old pkg):
                                         * md5, creator, lastCheckTime, lastUpdateTime, curApkSize,
                                         * curVersionCode, curVersionName, title, rating, ratingsCount,
                                         * 
                                         * 去掉rating, ratingsCount //还是加上
                                         * 增加downloadsCountText
                                         */
                                        updateCmd = "UPDATE GMarketInfo SET " +
                                        "md5 = ?, creator = ?, " +
                                        "lastCheckTime = statement_timestamp(), " +
                                        "lastUpdateTime = statement_timestamp(), " +
                                        "curApkSize = ?, curVersionCode = ?, " +
                                        "curVersionName = ?, title = ?, rating = ?, " +
                                        "ratingsCount = ?, downloadsCountText = ? " +
                                        "WHERE pkgName = ?";
                                        updateST = updateConn.prepareStatement(updateCmd);
                                        updateST.setString(1, md5);
                                        updateST.setString(2, creator);
                                        updateST.setInt(3, apksize);
                                        updateST.setInt(4, hisVersionCode);
                                        updateST.setString(5, hisVersionName);
                                        updateST.setString(6, title);
                                        updateST.setString(7, rating);
                                        updateST.setInt(8, ratingsCount);
                                        updateST.setString(9, downloadsCountText);
                                        updateST.setString(10, pkgName);
                                        // for print
                                        updateCmd = "UPDATE GMarketInfo SET " +
                                        "md5 = "+md5+", creator = "+creator+", " +
                                        "lastCheckTime = statement_timestamp(), " +
                                        "lastUpdateTime = statement_timestamp(), " +
                                        "curApkSize = "+apksize+", curVersionCode = "+hisVersionCode+", " +
                                        "curVersionName = "+hisVersionName+", title = "+title+", rating = "+rating+", " +
                                        "ratingsCount = "+ratingsCount+", downloadsCountText = "+downloadsCountText+" " +
                                        "WHERE pkgName = "+pkgName;
                                        System.out.println(updateCmd);
                                        // check result
                                        updateRows = updateST.executeUpdate();
                                        if (updateRows != 1) {
                                            System.err.println("ERROR: "+pkgName+
                                            ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                            return 4;
                                        }
                                        
                                    }
                                    
                                    // final commit for two statements
                                    updateConn.commit();
                                    System.out.println("INFO: insert and update/insert ok");
                                    
                                    totalIndex++;
                                    System.out.println("INFO: Next totalIndex: "+totalIndex);
                                }
                                
                            } else {
                                /**
                                 * isToCrawl == 0 肯定至少db里是有的 且肯定versionCode是一样的吧?
                                 * no to crawl, just update:
                                 * creator, lastCheckTime, curVersionName,
                                 * title, rating, ratingsCount
                                 * 
                                 * 去掉rating, ratingsCount //还是加上
                                 * 增加downloadsCountText
                                 * 
                                 * 不用区分是new还是popular的了吧，根据publisher来爬，应该rating, ratingsCount, downloadsCountText都有吧
                                 */
                                updateCmd = "UPDATE GMarketInfo SET creator = ?, " +
                                "lastCheckTime = statement_timestamp(), " +
                                "curVersionName = ?, title = ?, rating = ?, " +
                                "ratingsCount = ?, downloadsCountText = ? " +
                                "WHERE pkgName = ?";
                                updateST = updateConn.prepareStatement(updateCmd);
                                updateST.setString(1, creator);
                                updateST.setString(2, hisVersionName);
                                updateST.setString(3, title);
                                updateST.setString(4, rating);
                                updateST.setInt(5, ratingsCount);
                                updateST.setString(6, downloadsCountText);
                                updateST.setString(7, pkgName);
                                // for print
                                updateCmd = "UPDATE GMarketInfo SET creator = "+creator+", " +
                                "lastCheckTime = statement_timestamp(), " +
                                "curVersionName = "+hisVersionName+", title = "+title+", rating = "+rating+", " +
                                "ratingsCount = "+ratingsCount+", downloadsCountText = "+downloadsCountText+" " +
                                "WHERE pkgName = "+pkgName;
                                System.out.println(updateCmd);
                                // check result
                                updateRows = updateST.executeUpdate();
                                if (updateRows != 1) {
                                    System.err.println("ERROR: "+pkgName+
                                    ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                    return 4;
                                }
                                updateConn.commit();
                                System.out.println("INFO: update ok");
                            }
                        }//--end of 'for'
                        
                        /**
                         * next getAppsByPublisher()
                         */
                        if (nAppCount >= 10) {  // 照理我就query了10个，应该不会 > 的                               
                            appIndexInOnePub += nAppCount;   // 应该就是 +10 的了
                        } else if (nAppCount > 0) { // 0 < nAppCount < 10
                            break;
                        }
                        
                    //--end of 'pkgsResponse != null'
                    } else {
                        System.err.print("WARN: "+this.pubName+": pkgsResponse is null\n");
                        break;
                    }
                    
                }//-- end of inside "while"
                
                // TODO: test for only one time
                //break;
                
            }//--end of outside "while"
            
        } catch (Exception e) {
            System.err.print("EXCEPTION: "+this.pubName+": ");
            e.printStackTrace();
            return 5;
        }
        
        return 0;
	}
	
	/**
	 * case 6
	 * current according to POPULAR
	 * refer to executeByPubFile()
	 * 
	 * @return errorCode
	 * 0: ok
	 * 1: have reached max attempts, appsResponse == null OR nAppCount == 0
	 * 2: max RuntimeException
	 * 3: get md5 or file size error, mv apkComePath error
	 * 4: sql exception
	 * 5: unknow error
	 * 6: category error
	 */
	private int executeByOneCat() {
		/**
		 * store results from getAppCount()
		 */
		int nAppCount = 0;
		/**
		 * Number of attempts the current query has made so far
		 */
		int attempts = 0;
		/**
         * getAppsByCategory出现java.lang.RuntimeException的次数
         */
        int nException = 0;
        /**
         * 用于遍历每次的pkgsResponse
         */
        int appIndex = 0;
        /**
		 * store results from downloadApk()
		 */
        int result = 0;
        /**
         * 成功fetch getAppsByCategory的次数
         * 用来统计是否到everyFetchNum了
         * 可以一直计数下去，因为是取余数
         */
        int nFetchNum = 0;
        /**
         * "\n--> "
         */
        int nRow = 0;
        /**
		 * -1: ERROR
		 * 0:  no
		 * 1:  yes
		 */
		int isToCrawl;
		boolean isNewtoDB;
        
        App app = null;
		ExtendedInfo extendedInfo = null;
		String pkgName, apkName, apkPath, hisVersionName;
		
		String queryCmd = "SELECT assetId, curVersionCode, curApkSize " +
		"from ApkBin, GMarketInfo " +
		"WHERE GMarketInfo.pkgName = ? AND ApkBin.md5 = GMarketInfo.md5";
		String updateCmd;
		int updateRows;
		String md5Cmd = "SELECT md5 FROM ApkBin WHERE md5 = ?";
		
		AssetIdFetcher assetIdFetcher = new AssetIdFetcher();
		AppsResponse appsResponse = null;
		String creator, title, apkComePath, apkTodoPath;
		String ourAssertId, hisAssetId;
		String rating, downloadsCountText, oldCategory, newCategory;
		int ourVersionCode, hisVersionCode, ourApkSize, hisApkSize, ratingsCount;
		
		readLogFiles();
		
		try {
			while (startIndex < maxAppIndex) {
				
				//add by chenxiong
				if(changeAccountsNum >= maxChangeAccountsNum){
					System.err.println("ERROR: have reached maxChangeAccountsNum!\n exit!");
					return 1;
				}
				
				if (attempts >= maxAttempts) {
					//deld by chenxiong
					/*System.err.println("ERROR: have reached max attempts: "+
					maxAttempts+"! going to exit");*/
//					exitClean();
//					System.exit(1);
					//deld by chenxiong
					//return 1;
					
					//add by chenxiong
					/*
					 * when the exception reached maxAttempts,
					 * change the accounts
					 */
					System.err.println("WARN: have reached max attempts: " + maxAttempts + "! going to change accounts");
					attempts = 0;
					changeAccounts();
					loginCurrentAccounts();
				}
				
				// judge to sleep for a big time
	            if (nFetchNum != 0 && nFetchNum%everyFetchNum == 0) {
	                try {
	                    System.out.print("\n==> sleep for a big time: "+bigSleep+"\n\n");
	                    Thread.sleep(bigSleep);
	                    
	                } catch (InterruptedException e) {
	                	System.err.println("EXCEPTION: " + e);
	                }
	            }
	            
	            // judge whether now is everyFetchNum or not
	            if (nFetchNum % everyFetchNum == 0) {
	                System.out.print("\n==> crawlerLogin\n\n");
	                /**
                     * @deprecated
                     * @since version 0.6
                     */
//                  oldCrawlerLogin();
                    /**
                     * @since version 0.6
                     */
	                //deld by chenxiong
                    //loginAllSessions();
	            }
				
				try {
				    nFetchNum++;    // changed in 0.4
				    /**
	                 * @since version 0.6
	                 */
				    System.out.println("\n==> current SessionIndex is "+nextSessionIndex);
	                moveToNextSession();
	                System.out.println("==> nextSessionIndex will be "+nextSessionIndex);
	                
	                /**
                     * for each getAppsByXXX(), then sleep
                     */
					appsResponse = assetIdFetcher.getAppsByCategory(
							crawlerSession.getMarketSession(),
							categoryInApi,
							startIndex,
							orderType,	// POPULAR
							1);			// free
                    try {
                        System.out.println("sleep for a while: "+smallSleep+"ms");
                        Thread.sleep(smallSleep);
                    } catch (InterruptedException e) {
                        System.err.print("EXCEPTION: ");
                        e.printStackTrace();
                    }
				} catch (RuntimeException e) {
		            System.err.print("EXCEPTION: ");
		            e.printStackTrace();
		            
		            nException++;
		            System.err.print("WARN: Current exception number is: "+nException+"\n\n");
		            //add by chenxiong
		            System.err.println("going to change accounts!");
		            changeAccounts();
		            loginCurrentAccounts();
		            
		            if (nException >= 3) {
		                // exit when has reached 3rd exception
		            	//deld by chenxiong
		                System.err.print("==> Have reached max exception, going to change accounts!\n");//mfd by chenxiong
//		                exitClean();
//		                System.exit(1);
		                //deld by chenxiong
		                //return 2;
		                
		                //add by chenxiong
			            System.err.println("going to change accounts!");
			            changeAccounts();
			            loginCurrentAccounts();
			            nException = 0;
		            }
		            
		            continue;
		        }
				// 跑到这边来的话，就说明上面的正常。reset as 0
	        	nException = 0;
	        	
	        	if (appsResponse == null) {
	        		System.err.println("WARN: appsResponse is null");
					attempts++;
					System.err.println("INFO: current attempt is: "+attempts);
					continue;
	        	}
	        	
	        	// TODO for testing
//				System.out.println(appsResponse.toString());
				nAppCount = appsResponse.getAppCount();
				if (nAppCount == 0) {
					System.err.println("WARN: nAppCount is 0");
					attempts++;
					System.out.println("INFO: current attempt is: "+attempts);
					continue;
				}
				
				/**
				 * now appsResponse is valid
				 */
				attempts = 0;
	    		for (appIndex = 0; appIndex < nAppCount; appIndex++) {
	    		    nRow++;
	    		    // TODO 还不是很准
                    currentIndex++;
	    			/**
	    			 * set default value
	    			 */
	    			isToCrawl = -1;
	    			isNewtoDB = false;
	    			ourAssertId = null;
	    			ourVersionCode = -1;
	    			ourApkSize = -1;
	    			
	    			/**
					 * get app info & extendedInfo
					 */
					app = appsResponse.getApp(appIndex);
					pkgName = app.getPackageName();
					hisAssetId = app.getId();
					hisVersionCode = app.getVersionCode();
					hisVersionName = app.getVersion();
					creator = app.getCreator();
					title = app.getTitle();
					rating = app.getRating();
                    ratingsCount = app.getRatingsCount();
					
					extendedInfo = app.getExtendedInfo();
					hisApkSize = extendedInfo.getInstallSize();
					downloadsCountText = extendedInfo.getDownloadsCountText();
					oldCategory = extendedInfo.getCategory();
					
                    newCategory = convertCategoryTitle(oldCategory);
                    if (newCategory.equals("0_UNKNOWN")) {
                        System.err.println("ERROR: convert failed for newCategory: "+newCategory);
                        return 6;
                    }
                    if (!categoryInDisk.equals("25_APP_WALLPAPER") && !categoryInDisk.equals("27_GAME_WALLPAPER")
                            && !categoryInDisk.equals("29_GAME_WIDGETS") && !categoryInDisk.equals("30_APP_WIDGETS")) {
                        if (!newCategory.equals(categoryInDisk))
                            System.err.println("WARN: unequal for newCategory: "+newCategory+", categoryInDisk: "+categoryInDisk);
                    }
					
					apkName = totalIndex + "-@-" + pkgName + ".apk";
					apkPath = newCategory + "/" + apkName;
					
					// print rows
					System.out.println("\n--> "+nRow+": "+pkgName+", "+hisVersionCode);
					
					/**
					 * compare app info with db
					 */
	                onefullQueryST = queryConn.prepareStatement(queryCmd);
	                onefullQueryST.setString(1, pkgName);
	                onefullRS = onefullQueryST.executeQuery();
	                if (onefullRS.next()) {
	                	/**
	                	 * db里有这个pkgname
	                	 */
	                    System.out.println("INFO: "+pkgName+" exists in current db");
	                	ourAssertId = onefullRS.getString(1);
	                	ourVersionCode = onefullRS.getInt(2);
	                	ourApkSize = onefullRS.getInt(3);
	                	if (ourAssertId != null && !ourAssertId.equals("")) {
	                		if (ourAssertId.equals(hisAssetId)) {
	                			isToCrawl = 0;
	                			System.out.println("INFO: assetId is the same: "+hisAssetId);
	                		} else {
	                			isToCrawl = 1;
	                			System.out.println("INFO: our assetId is: "+ourAssertId+
	                			", his assetId is: "+hisAssetId);
	                		}
	                	} else {
	                		if (ourVersionCode != 0) {
	                			if (ourVersionCode != hisVersionCode) {
	                				isToCrawl = 1;
	                				System.out.println("INFO: our versionCode is: "+ourVersionCode+
	        	                	", his versionCode is: "+hisVersionCode);
	                			} else {
	                				System.out.println("INFO: versionCode is the same: "+ourVersionCode);
	                				if (ourApkSize != 0) {
	                					if (ourApkSize == hisApkSize) {
	                						isToCrawl = 0;
	                						System.out.println("INFO: ApkSize is the same: "+ourApkSize);
	                					} else {
	                						isToCrawl = 1;
	                						System.out.println("INFO: ourApkSize is: "+ourApkSize+
	            	        	            ", hisApkSize is: "+hisApkSize);
	                					}
	                				} else {
	                					isToCrawl = 1;
	    	                			System.err.println("WARN: ourApkSize is 0, pkgName: "+pkgName);
	                				}
	                			}
	                		} else {
	                			isToCrawl = 1;
	                			System.err.println("WARN: our versionCode is 0, pkgName: "+pkgName);
	                		}
	                	}
	                	
	                } else {
	                	/**
	                	 * db里没有这个pkgName
	                	 */
	                	isToCrawl = 1;
	                	isNewtoDB = true;
	                	System.out.println("INFO: "+pkgName+" is new");
	                }
					System.out.println("INFO: isToCrawl is "+isToCrawl);
		            
					/**
					 * download apps
					 * TODO 基本都是重复的了，跟executeBySqlPkgFile
					 * 不过还是有些不一样的
					 */
					if (isToCrawl == 1) {
						/**
						 * yes to crawl, also need to insert or update
						 * basic new md5
						 */
						apkComePath = ROOTCOME + apkPath;
						apkTodoPath = ROOTTODO + apkPath;
						
						result = downloadApk(apkPath, hisAssetId);
						if (result == -1) {
							System.err.print("ERROR: Didn't successfully crawl: "+apkPath+"\n");
							continue;
						}
						
						// calculate md5
						String md5 = "";
						try {
                            md5 = MD5Checksum.getMD5Checksum(apkComePath);
                            System.out.println("INFO: md5: "+md5);
                        } catch (Exception e) {
                            System.err.print("EXCEPTION: ");
                            e.printStackTrace();
                            // must succeed, otherwise exit
//                          exitClean();
//                          System.exit(1);
                            return 3;
                        }
                        
                        // get file size
                        int apksize = 0;
                        apksize = getApkSize(apkComePath);
                        if (apksize == -1) {
                            System.err.println("ERROR: "+apkComePath+" path is wrong!");
//                          exitClean();
//                          System.exit(1);
                            return 3;
                        } else if (apksize == 0) {
                            System.err.println("ERROR: "+apkComePath+" size is 0");
                        }
                        System.out.println("INFO: apksize: "+apksize);
                        
                        // query this md5，虽然认为不可能有，但还是得query下
                        onefullQueryST = queryConn.prepareStatement(md5Cmd);
                        onefullQueryST.setString(1, md5);
                        onefullRS = onefullQueryST.executeQuery();
                        if (onefullRS.next()) {
                            /**
                             * already exists, should seldom happen
                             */
                            System.err.println("ERROR: md5("+md5+") already in the db");
                            
                        } else {
                        	System.out.println("INFO: No this md5("+md5+") in db");
                            /**
                             * no this md5 in db, first insert ApkBin
                             */
                            String mvCmd = "mv "+apkComePath+" "+apkTodoPath;// must ensure dir exists
                            System.out.println(mvCmd);
                            Process p = Runtime.getRuntime().exec(mvCmd);
                            int exitCode = p.waitFor();
                            if (exitCode != 0) {
                            	System.err.println("ERROR: mv "+apkComePath+"failed, exiCode is "+exitCode);
//                            	exitClean();
//                              System.exit(1);
                            	return 3;
                            }
                            System.out.println("INFO: mv ok!");
                            
                            String apkPathinDB = "apkTodo/"+apkPath;
                            // TODO 当前仅仅新的apk才更新assetId
                            // 这样也保证了assetId肯定是跟md5对应的
                            updateCmd = "INSERT INTO ApkBin (md5, pkgName, apkPath, " +
                            "apkSize, versionCode, versionName, assetId) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                            updateST = updateConn.prepareStatement(updateCmd);
                            updateST.setString(1, md5);
                            updateST.setString(2, pkgName);
                            updateST.setString(3, apkPathinDB);
                            updateST.setInt(4, apksize);
                            updateST.setInt(5, hisVersionCode);
                            updateST.setString(6, hisVersionName);
                            updateST.setString(7, hisAssetId);
                            // for print
                            updateCmd = "INSERT INTO ApkBin (md5, pkgName, apkPath, " +
                            "apkSize, versionCode, versionName, assetId) " +
                            "VALUES ("+md5+", "+pkgName+", "+apkPathinDB+", "+apksize+
                            ", "+hisVersionCode+", "+hisVersionName+", "+hisAssetId+")";
                            System.out.println(updateCmd);
                            // check result
                            updateRows = updateST.executeUpdate();
                            if (updateRows != 1) {
                                System.err.println("ERROR: "+pkgName+
                                ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
//                              exitClean();
//                              System.exit(1);
                                return 4;
                            }
                            
                            
                            if (isNewtoDB) {
                                /**
                                 * insert new pkg(thus new md5)
                                 * 还是增加rating和ratingsCount吧
                                 */
                                if (this.orderType == 2) {  // new
                                    updateCmd = "INSERT INTO GMarketInfo (pkgName, md5, creator, " +
                                    "insertTime, lastCheckTime, lastUpdateTime, curApkSize, " +
                                    "curVersionCode, curVersionName, category, title, " +
                                    "downloadsCountText) " +
                                    "VALUES(?, ?, ?, statement_timestamp(), statement_timestamp(), " +
                                    "statement_timestamp(), ?, ?, ?, ?, ?, ?)";
                                    updateST = updateConn.prepareStatement(updateCmd);
                                    updateST.setString(1, pkgName);
                                    updateST.setString(2, md5);
                                    updateST.setString(3, creator);
                                    updateST.setInt(4, apksize);
                                    updateST.setInt(5, hisVersionCode);
                                    updateST.setString(6, hisVersionName);
                                    updateST.setString(7, newCategory);
                                    updateST.setString(8, title);
                                    updateST.setString(9, downloadsCountText);
                                    // for print
                                    updateCmd = "INSERT INTO GMarketInfo (pkgName, md5, creator, " +
                                    "insertTime, lastCheckTime, lastUpdateTime, curApkSize, " +
                                    "curVersionCode, curVersionName, category, title, " +
                                    "downloadsCountText) " +
                                    "VALUES("+pkgName+", "+md5+", "+creator+", statement_timestamp(), statement_timestamp(), " +
                                    "statement_timestamp(), "+apksize+", "+hisVersionCode+", "+hisVersionName+", "+
                                    newCategory+", "+title+", "+downloadsCountText+")";
                                    System.out.println(updateCmd);
                                    // check result
                                    updateRows = updateST.executeUpdate();
                                    if (updateRows != 1) {
                                        System.err.println("ERROR: insert "+pkgName+
                                        ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                        return 4;
                                    }
                                    
                                } else {
                                    updateCmd = "INSERT INTO GMarketInfo (pkgName, md5, creator, " +
                                    "insertTime, lastCheckTime, lastUpdateTime, curApkSize, " +
                                    "curVersionCode, curVersionName, category, title, " +
                                    "rating, ratingsCount, downloadsCountText) " +
                                    "VALUES(?, ?, ?, statement_timestamp(), statement_timestamp(), " +
                                    "statement_timestamp(), ?, ?, ?, ?, ?, ?, ?, ?)";
                                    updateST = updateConn.prepareStatement(updateCmd);
                                    updateST.setString(1, pkgName);
                                    updateST.setString(2, md5);
                                    updateST.setString(3, creator);
                                    updateST.setInt(4, apksize);
                                    updateST.setInt(5, hisVersionCode);
                                    updateST.setString(6, hisVersionName);
                                    updateST.setString(7, newCategory);
                                    updateST.setString(8, title);
                                    updateST.setString(9, rating);
                                    updateST.setInt(10, ratingsCount);
                                    updateST.setString(11, downloadsCountText);
                                    // for print
                                    updateCmd = "INSERT INTO GMarketInfo (pkgName, md5, creator, " +
                                    "insertTime, lastCheckTime, lastUpdateTime, curApkSize, " +
                                    "curVersionCode, curVersionName, category, title, " +
                                    "rating, ratingsCount, downloadsCountText) " +
                                    "VALUES("+pkgName+", "+md5+", "+creator+", statement_timestamp(), statement_timestamp(), " +
                                    "statement_timestamp(), "+apksize+", "+hisVersionCode+", "+hisVersionName+", "+
                                    newCategory+", "+title+", "+rating+", "+ratingsCount+", "+downloadsCountText+")";
                                    System.out.println(updateCmd);
                                    // check result
                                    updateRows = updateST.executeUpdate();
                                    if (updateRows != 1) {
                                        System.err.println("ERROR: insert "+pkgName+
                                        ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
//                                      exitClean();
//                                      System.exit(1);
                                        return 4;
                                    }
                                    
                                }
                                
                            } else {
                                /**
                                 * update new md5(old pkg):
                                 * md5, creator, lastCheckTime, lastUpdateTime, curApkSize,
                                 * curVersionCode, curVersionName, title, rating, ratingsCount,
                                 * 
                                 * 去掉rating, ratingsCount //还是加上
                                 * 增加downloadsCountText
                                 */
                                if (this.orderType == 2) {  // new
                                    updateCmd = "UPDATE GMarketInfo SET " +
                                    "md5 = ?, creator = ?, " +
                                    "lastCheckTime = statement_timestamp(), " +
                                    "lastUpdateTime = statement_timestamp(), " +
                                    "curApkSize = ?, curVersionCode = ?, " +
                                    "curVersionName = ?, title = ?, " +
                                    "downloadsCountText = ? " +
                                    "WHERE pkgName = ?";
                                    updateST = updateConn.prepareStatement(updateCmd);
                                    updateST.setString(1, md5);
                                    updateST.setString(2, creator);
                                    updateST.setInt(3, apksize);
                                    updateST.setInt(4, hisVersionCode);
                                    updateST.setString(5, hisVersionName);
                                    updateST.setString(6, title);
                                    updateST.setString(7, downloadsCountText);
                                    updateST.setString(8, pkgName);
                                    // for print
                                    updateCmd = "UPDATE GMarketInfo SET " +
                                    "md5 = "+md5+", creator = "+creator+", " +
                                    "lastCheckTime = statement_timestamp(), " +
                                    "lastUpdateTime = statement_timestamp(), " +
                                    "curApkSize = "+apksize+", curVersionCode = "+hisVersionCode+", " +
                                    "curVersionName = "+hisVersionName+", title = "+title+", " +
                                    "downloadsCountText = "+downloadsCountText+" " +
                                    "WHERE pkgName = "+pkgName;
                                    System.out.println(updateCmd);
                                    // check result
                                    updateRows = updateST.executeUpdate();
                                    if (updateRows != 1) {
                                        System.err.println("ERROR: "+pkgName+
                                        ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                        return 4;
                                    }
                                    
                                } else {
                                    updateCmd = "UPDATE GMarketInfo SET " +
                                    "md5 = ?, creator = ?, " +
                                    "lastCheckTime = statement_timestamp(), " +
                                    "lastUpdateTime = statement_timestamp(), " +
                                    "curApkSize = ?, curVersionCode = ?, " +
                                    "curVersionName = ?, title = ?, rating = ?, " +
                                    "ratingsCount = ?, downloadsCountText = ? " +
                                    "WHERE pkgName = ?";
                                    updateST = updateConn.prepareStatement(updateCmd);
                                    updateST.setString(1, md5);
                                    updateST.setString(2, creator);
                                    updateST.setInt(3, apksize);
                                    updateST.setInt(4, hisVersionCode);
                                    updateST.setString(5, hisVersionName);
                                    updateST.setString(6, title);
                                    updateST.setString(7, rating);
                                    updateST.setInt(8, ratingsCount);
                                    updateST.setString(9, downloadsCountText);
                                    updateST.setString(10, pkgName);
                                    // for print
                                    updateCmd = "UPDATE GMarketInfo SET " +
                                    "md5 = "+md5+", creator = "+creator+", " +
                                    "lastCheckTime = statement_timestamp(), " +
                                    "lastUpdateTime = statement_timestamp(), " +
                                    "curApkSize = "+apksize+", curVersionCode = "+hisVersionCode+", " +
                                    "curVersionName = "+hisVersionName+", title = "+title+", rating = "+rating+", " +
                                    "ratingsCount = "+ratingsCount+", downloadsCountText = "+downloadsCountText+" " +
                                    "WHERE pkgName = "+pkgName;
                                    System.out.println(updateCmd);
                                    // check result
                                    updateRows = updateST.executeUpdate();
                                    if (updateRows != 1) {
                                        System.err.println("ERROR: "+pkgName+
                                        ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
//                                      exitClean();
//                                      System.exit(1);
                                        return 4;
                                    }
                                    
                                }
                                
                            }
                            
                            // final commit for two statements
                            updateConn.commit();
                            System.out.println("INFO: insert and update/insert ok");
                            
                            // only insert and update will count
//                          currentIndex++;   //放在这里是不准的了
                            totalIndex++;
//                          System.out.println("INFO: Next currentIndex: "+currentIndex+", totalIndex: "+totalIndex);
                            System.out.println("INFO: Next totalIndex: "+totalIndex);
                        }
						
					} else {
						/**
						 * isToCrawl == 0 肯定至少db里是有的 且肯定versionCode是一样的吧?
						 * no to crawl, just update:
						 * creator, lastCheckTime, curVersionName,
						 * title, rating, ratingsCount
						 * 
						 * 去掉rating, ratingsCount //还是加上
                         * 增加downloadsCountText
						 */
					    if (this.orderType == 2) {  // new
					        updateCmd = "UPDATE GMarketInfo SET creator = ?, " +
                            "lastCheckTime = statement_timestamp(), " +
                            "curVersionName = ?, title = ?, " +
                            "downloadsCountText = ? " +
                            "WHERE pkgName = ?";
                            updateST = updateConn.prepareStatement(updateCmd);
                            updateST.setString(1, creator);
                            updateST.setString(2, hisVersionName);
                            updateST.setString(3, title);
                            updateST.setString(4, downloadsCountText);
                            updateST.setString(5, pkgName);
                            // for print
                            updateCmd = "UPDATE GMarketInfo SET creator = "+creator+", " +
                            "lastCheckTime = statement_timestamp(), " +
                            "curVersionName = "+hisVersionName+", title = "+title+", " +
                            "downloadsCountText = "+downloadsCountText+" " +
                            "WHERE pkgName = "+pkgName;
                            System.out.println(updateCmd);
                            // check result
                            updateRows = updateST.executeUpdate();
                            if (updateRows != 1) {
                                System.err.println("ERROR: "+pkgName+
                                ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
                                return 4;
                            }
                            updateConn.commit();
                            System.out.println("INFO: update ok");
                            
					    } else {
					        updateCmd = "UPDATE GMarketInfo SET creator = ?, " +
	                        "lastCheckTime = statement_timestamp(), " +
	                        "curVersionName = ?, title = ?, rating = ?, " +
	                        "ratingsCount = ?, downloadsCountText = ? " +
	                        "WHERE pkgName = ?";
	                        updateST = updateConn.prepareStatement(updateCmd);
	                        updateST.setString(1, creator);
	                        updateST.setString(2, hisVersionName);
	                        updateST.setString(3, title);
	                        updateST.setString(4, rating);
	                        updateST.setInt(5, ratingsCount);
	                        updateST.setString(6, downloadsCountText);
	                        updateST.setString(7, pkgName);
	                        // for print
	                        updateCmd = "UPDATE GMarketInfo SET creator = "+creator+", " +
	                        "lastCheckTime = statement_timestamp(), " +
	                        "curVersionName = "+hisVersionName+", title = "+title+", rating = "+rating+", " +
	                        "ratingsCount = "+ratingsCount+", downloadsCountText = "+downloadsCountText+" " +
	                        "WHERE pkgName = "+pkgName;
	                        System.out.println(updateCmd);
	                        // check result
	                        updateRows = updateST.executeUpdate();
	                        if (updateRows != 1) {
	                            System.err.println("ERROR: "+pkgName+
	                            ": updateRows is "+updateRows+". updateCmd is: "+updateCmd);
//	                            exitClean();
//	                            System.exit(1);
	                            return 4;
	                        }
	                        updateConn.commit();
	                        System.out.println("INFO: update ok");
	                        
					    }
						
					}
					
				// end of "for" 
	    		}	
				startIndex += nAppCount;
				// TODO 移到上面去吧
				// 不管是成功fetch，还是失败fetch，都++
//				nFetchNum++;	//成功fetch的次数+1
				
				// TODO: only for one time testing
//				break;
				
			// end of "while"
			}
			
		} catch (Exception e) {
        	System.err.print("EXCEPTION: ");
            e.printStackTrace();
//          exitClean();
//          System.exit(1);
            return 5;
        }
		
//		exitClean();
		return 0;
	}
	
	/**
	 * case 7
	 * automatically download all category
	 * according to NEWEST | FEATURED | POPULAR
	 * 
	 * TODO 这样startIndex就不好控制了。。。
	 * 不过可以直接循环调用executeByOneCat()
	 */
	private void executeByAllCat() {
		/**
		 * bycategory
		 */
		int bc;
		int errorCode = -1;
		
		//add by chenxiong
		loginCurrentAccounts();
		
		for (bc = this.smallAllCategory; bc <= 36; bc++) {
			if (bc == 6 || bc == 11) {
				System.out.println("\n==> skip bc: "+bc+"\n");
				continue;
			}
			
			// 类似resolveArgs()一样来解析参数
			// TODO totalIndex应该没问题吧?
			this.categoryInApi = parseToCategoryApi(bc);
			if (this.categoryInApi.equals("ERROR")) {
				System.err.println("ERROR: parse bycategory: "+bc);
				System.exit(1);
			}
			this.categoryInDisk = parseToCategoryDisk(bc);
			if (this.categoryInDisk.equals("0_UNKNOWN")) {
				System.err.println("ERROR: parse bycategory: "+bc);
				System.exit(1);
			}
			this.startIndex = 0;
			this.currentIndex = 0;	//因为这个值会被改，所以每次得重新赋值
			this.maxAppIndex  = this.startIndex + this.totalCount;
			
			// crawling one category
			connectToDB();
			errorCode = executeByOneCat();
			if (errorCode == 0 || errorCode == 1) {
				// continue
				exitClean();
			} else {
				// game over
				exitClean();
				System.exit(errorCode);
			}
			
			// take a rest
			try {
                System.out.print("\n==> wait for next category: "+bigSleep+"\n\n");
                Thread.sleep(bigSleep);
                
            } catch (InterruptedException e) {
            	System.err.println("EXCEPTION: " + e);
            }
		}
	}
	
	/**
	 * 类就开始做预先定义类时就要做的工作了
	 */
	private void execute() {
		int errorCode = -1;
		
		if (whichTask == 4 || whichTask ==5) {
			BufferedReader fileReader = null;
			try {
				fileReader = new BufferedReader(new FileReader(sqlFile));
				sqlCmd = fileReader.readLine();
				if (sqlCmd == null) {
					exitClean();
		    		System.exit(1);
				}
				
			} catch (Exception e) {
				System.err.print("EXCEPTION: ");
	    		e.printStackTrace();
	    		exitClean();
	    		System.exit(1);
	    		
			} finally {
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						System.err.print("EXCEPTION: ");
			    		e.printStackTrace();
			    		exitClean();
			    		System.exit(1);
					}
				}
			}
		}
		
		switch (this.whichTask) {
		case 0:
			connectToDB();
			/**
			 * only download a package
			 * also output _ERRORAPKS, _CRAWLERLOG
			 */
			executeByOnePkg();
			break;
			
		case 1:
			connectToDB();
			/**
			 * download pkgs from a PKGTOCRAWL
			 */
			executeByPkgFile();
			break;
			
		case 2:
			connectToDB();
			/**
	         * download pkgs from a Pub File
	         */
			executeByPubFile();
            break;
            
		case 3:
			connectToDB();
			/**
	         * automatically download by query keyword
	         */
			executeByQueryWord();
			break;
			
		case 4:
			connectToDB();
			executeBySqlPkgFile();
			break;
			
		case 5:
			connectToDB();
			errorCode = executeBySqlPubFile();
			if (errorCode == 0) {
                exitClean();
            } else {
                exitClean();
                System.exit(errorCode);
            }
			break;
			
		case 6:
			connectToDB();
			/**
			 * automatically download a category
			 */
			errorCode = executeByOneCat();
			if (errorCode == 0) {
				exitClean();
			} else {
				exitClean();
				System.exit(errorCode);
			}
			break;
		
		case 7:	
			/**
		     * TODO
	         * automatically download all category
	         */
			executeByAllCat();
			break;
			
		default:
			System.err.println("ERROR: No this task, it's over!");
		}
		
	}

	private void connectToDB() {
		try {
			String url = "jdbc:postgresql://localhost/apkdb-test";
			String user = "chenxiong";
			String pwd = "chenxiong123";
			updateConn = DriverManager.getConnection(url, user, pwd);
			queryConn = DriverManager.getConnection(url, user, pwd);
			
			// make sure autocommit is off
			updateConn.setAutoCommit(false);
			queryConn.setAutoCommit(false);
			
		} catch (SQLException e) {
			System.err.println("EXCEPTION: " + e);
		}
	}
	
	private void resolveArgs(Options options, String args[]) {	
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine line = parser.parse(options, args);
			/**
			 * general option
			 */
			// totalIndex
			if (line.hasOption("ti")) {
				// TODO: default use md5 as name, currently not
				// TODO md5得保存下来后才能计算吧。。。
//				this.isMd5ForName = false;
				try {
					this.totalIndex = Integer.parseInt(line.getOptionValue("ti"));
					
				} catch (NumberFormatException e) {
					System.err.println("EXCEPTION: " + e);
					System.exit(1);
				}
			}
			// TODO totalCount
			if (line.hasOption("tc")) {
				// TODO
				this.isUnLimited = false;
				try {
					this.totalCount = Integer.parseInt(line.getOptionValue("tc"));
					
				} catch (NumberFormatException e) {
					System.err.println("EXCEPTION: " + e);
					System.exit(1);
				}
			}
			// startIndex
			if (line.hasOption("si")) {
				try {
					this.startIndex = Integer.parseInt(line.getOptionValue("si"));
					// 现在总是从0开始
//					this.currentIndex = startIndex;
					
				} catch (NumberFormatException e) {
					System.err.println("EXCEPTION: " + e);
					System.exit(1);
				}
			}
			// orderType
			if (line.hasOption("ot")) {
				try {
					this.orderType = Integer.parseInt(line.getOptionValue("ot"));
					if (orderType < 1 || orderType > 3) {
						System.err.println("ERROR: unexpected orderType: "+orderType);
						System.exit(1);
					}
					
				} catch (NumberFormatException e) {
					System.err.println("EXCEPTION: " + e);
					System.exit(1);
				}
			}
			// smallTime
			if (line.hasOption("st")) {
				try {
					this.smallSleep = Integer.parseInt(line.getOptionValue("st"));
					
				} catch (NumberFormatException e) {
					System.err.println("EXCEPTION: " + e);
					this.smallSleep = this.defaultSmallSleep;
				}
			}
			// oneAccountIndex
			if (line.hasOption("oai")) {
                try {
                    this.oneAccountIndex = Integer.parseInt(line.getOptionValue("oai"));
                    if (oneAccountIndex < 0 || oneAccountIndex >= accountsNum) {
                        System.err.println("WARN: oneAccountIndex should >= 0, while <= accountsNum-1. " +
                        "So just set default: "+defaultOneAccountIndex);
                        this.oneAccountIndex = this.defaultOneAccountIndex;
                    }
                    
                } catch (NumberFormatException e) {
                    System.err.println("EXCEPTION: " + e);
                    this.oneAccountIndex = this.defaultOneAccountIndex;
                }
            }
			// smallAllCategory
			if (line.hasOption("sac")) {
                try {
                    this.smallAllCategory = Integer.parseInt(line.getOptionValue("sac"));
                    if (smallAllCategory < 1 || smallAllCategory > 36) {
                        System.err.println("WARN: smallAllCategory should >= 1, while <= 36. " +
                        "So just set default: "+defaultSmallAllCategory);
                        this.smallAllCategory = this.defaultSmallAllCategory;
                    }
                    
                } catch (NumberFormatException e) {
                    System.err.println("EXCEPTION: " + e);
                    this.smallAllCategory = this.defaultSmallAllCategory;
                }
            }
			
			/**
			 * choose which task
			 */
			if (line.hasOption("pkg") && line.hasOption("ti")) {// TODO: add category
				this.pkgName = line.getOptionValue("pkg");
				this.whichTask = 0;
				
			} else if (line.hasOption("pkf") && line.hasOption("ti")) {
				this.pkgFile = line.getOptionValue("pkf");
				this.whichTask = 1;
				
			} else if (line.hasOption("puf") && line.hasOption("ti")) {
				this.pubFile = line.getOptionValue("puf");
				this.whichTask = 2;
			//3 for qw
				
			} else if (line.hasOption("sqlpkf") && line.hasOption("ti") && line.hasOption("tc")) {
				this.sqlFile = line.getOptionValue("sqlpkf");
				this.whichTask = 4;
				
			} else if (line.hasOption("sqlpuf") && line.hasOption("ti") && line.hasOption("tc")) {
				this.sqlFile = line.getOptionValue("sqlpuf");
				this.whichTask = 5;
				
			} else if (line.hasOption("bc") && line.hasOption("si") && 
						line.hasOption("ti") && line.hasOption("tc") && line.hasOption("ot")) {
				int bc = -1;
				try {
					bc = Integer.parseInt(line.getOptionValue("bc"));
					// TODO 下面parseCategory已经有过滤
					if (bc < 1 || bc > 36 || bc == 6 || bc == 11) {
						System.err.println("ERROR: unexpected bycategory: "+bc);
						System.exit(1);
					}
					
				} catch (NumberFormatException e) {
					System.err.println("EXCEPTION: " + e);
					System.exit(1);
				}
				
				this.categoryInApi = parseToCategoryApi(bc);
				if (this.categoryInApi.equals("ERROR")) {
					System.err.println("ERROR: parse bycategory: "+bc);
					System.exit(1);
				}
				// exp33_120409 in ByApkDB
//				this.categoryInDisk = bc+"_"+this.categoryInApi;
				this.categoryInDisk = parseToCategoryDisk(bc);
				if (this.categoryInDisk.equals("0_UNKNOWN")) {
					System.err.println("ERROR: parse bycategory: "+bc);
					System.exit(1);
				}
				
				this.maxAppIndex  = this.startIndex + this.totalCount;
				this.whichTask = 6;
				
			} else if (line.hasOption("ac") && line.hasOption("ti") 
						&& line.hasOption("ot") && line.hasOption("tc")) {
			    // 这里的totalCount是指每个category爬多少个
			    this.whichTask = 7;
			    
			}
			
//					else if (line.hasOption("qw") && line.hasOption("ti") && line.hasOption('s') ) {
//				    /**
//				     * case 6
//				     * by Query Keyword
//				     * '1-#-1-....apk'
//				     */
//				    this.queryWord = line.getOptionValue("qw");
//                    try {
//                        this.startIndex   = Integer.parseInt( line.getOptionValue('s') );
//                        this.totalIndex = Integer.parseInt( line.getOptionValue("ti") );
//                        this.currentIndex = startIndex;
//                        
//                        if ( line.hasOption("qc") ) {
//    				    	this.queryCount = Integer.parseInt( line.getOptionValue("qc") );
//    				    }
//                        this.maxAppIndex  = this.startIndex + this.queryCount;
//                        
//                    } catch (NumberFormatException e) {
//                        System.err.println(e);
//                        System.exit(1);
//                    }
//                    this.whichTask = 6;
//				}

		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		
		if ( whichTask == -1 ) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(SELFNAME, options);
			
		} else {
		    initAccounts();
			execute();
		}
	}

	// static函数不能直接用主类里的东东，只能用主类的static成员
	public static void main(String[] args) {
		/**
		 * load postgresql driver for jdbc
		 */
		try {
			Class.forName("org.postgresql.Driver");
			
		} catch (ClassNotFoundException e) {
			System.err.println("EXCEPTION: " + e);
			System.exit(1);
		}
		
		Options options = new CLI().getOptions();
		
		if (args.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			System.out.println(SELFNAME+": "+VERSION);
			formatter.printHelp(SELFNAME, options);
		} else {
			ApkCrawlerDBMain crawlerMain = new ApkCrawlerDBMain();
			crawlerMain.resolveArgs(options, args);
		}

	}
	
	private String parseToCategoryApi (int categoryValue) {
		switch (categoryValue) {
		case 1:
			return Category.ARCADE;
		case 2:
			return Category.DEMO;
		case 3:
			return Category.ENTERTAINMENT;
		case 4:
			return Category.FINANCE;
		case 5:
			return Category.HEALTH;
		case 6:
			return Category.LIBRARIES;
		case 7:
			return Category.LIFESTYLE;
		case 8:
			return Category.MULTIMEDIA;
		case 9:
			return Category.NEWS;
		case 10:
			return Category.REFERENCE;
		case 11:
			return Category.THEMES;
		case 12:
			return Category.TRAVEL;
		case 13:
			return Category.BRAIN;
		case 14:
			return Category.CARDS;
		case 15:
			return Category.CASUAL;
		case 16:
			return Category.COMICS;
		case 17:
			return Category.COMMUNICATION;
		case 18:
			return Category.PRODUCTIVITY;
		case 19:
			return Category.SHOPPING;
		case 20:
			return Category.SOCIAL;
		case 21:
			return Category.SPORTS;
		case 22:
			return Category.TOOLS;
		case 23:
            return Category.BUSINESS;
		case 24:
            return Category.PERSONALIZATION;
		case 25:
            return Category.APP_WALLPAPER;
		case 26:
            return Category.SPORTS_GAMES;
		case 27:
            return Category.GAME_WALLPAPER;
		case 28:
            return Category.RACING;
		case 29:
            return Category.GAME_WIDGETS;
		case 30:
            return Category.APP_WIDGETS;
		case 31:
            return Category.EDUCATION;
		case 32:
            return Category.MEDICAL;
		case 33:
            return Category.MUSIC_AND_AUDIO;
		case 34:
            return Category.PHOTOGRAPHY;
		case 35:
            return Category.TRANSPORTATION;
		case 36:
            return Category.WEATHER;
		default:
			return "ERROR";
		}
	}
	
	private String parseToCategoryDisk (int categoryValue) {
		switch (categoryValue) {
		case 1:
			return "1_ARCADE";
		case 2:
			return "2_DEMO";
		case 3:
			return "3_ENTERTAINMENT";
		case 4:
			return "4_FINANCE";
		case 5:
			return "5_HEALTH";
		case 6:
			// error
			return "0_UNKNOWN";
		case 7:
			return "7_LIFESTYLE";
		case 8:
			return "8_MULTIMEDIA";
		case 9:
			return "9_NEWS";
		case 10:
			return "10_REFERENCE";
		case 11:
			// error
			return "0_UNKNOWN";
		case 12:
			return "12_TRAVEL";
		case 13:
			return "13_BRAIN";
		case 14:
			return "14_CARDS";
		case 15:
			return "15_CASUAL";
		case 16:
			return "16_COMICS";
		case 17:
			return "17_COMMUNICATION";
		case 18:
			return "18_PRODUCTIVITY";
		case 19:
			return "19_SHOPPING";
		case 20:
			return "20_SOCIAL";
		case 21:
			return "21_SPORTS";
		case 22:
			return "22_TOOLS";
		case 23:
            return "23_BUSINESS";
		case 24:
            return "24_PERSONALIZATION";
		case 25:
            return "25_APP_WALLPAPER";
		case 26:
            return "26_SPORTS_GAMES";
		case 27:
            return "27_GAME_WALLPAPER";
		case 28:
            return "28_RACING";
		case 29:
            return "29_GAME_WIDGETS";
		case 30:
            return "30_APP_WIDGETS";
		case 31:
            return "31_EDUCATION";
		case 32:
            return "32_MEDICAL";
		case 33:
            return "33_MUSIC_AND_AUDIO";
		case 34:
            return "34_PHOTOGRAPHY";
		case 35:
            return "35_TRANSPORTATION";
		case 36:
            return "36_WEATHER";
		default:
			return "0_UNKNOWN";
		}
	}
	
	/**
	 * case 2
	 * not always download apk, will first compare with db
	 * change setEntriesCount to maxAppOnePub 不知道会不会异常
	 * TODO 果然不让一下子设为480的，见exp1_120329
	 * format: Kaixin001.com Inc.
	 * @deprecated
	 */
	private void newexecuteByPubFile() {
		/**
		 * store results from getAppCount()
		 */
		int nAppCount = 0;
		/**
		 * app index for one pub
		 */
		int appIndexinOnePub;
        /**
         * getAppsByPublisher出现java.lang.RuntimeException的次数
         */
        int nException = 0;
        /**
         * 用来统计是否到everyCount了
         * 可以一直计数下去，因为是取余数
         */
        int nFetchNum = 0;
        BufferedReader pubfileReader;
        
        AssetIdFetcher pkgsFetcher = null;
        AppsResponse pkgsResponse = null;
    	App app = null;
    	ExtendedInfo extendedInfo = null;
		String assetId, pkgName, oldCategory, apkName, apkPath;
		int result = 0;
        
        readLogFiles();
        
        try {
            pubfileReader = new BufferedReader(new FileReader(pubFile));
            String line = null;
            pkgsFetcher = new AssetIdFetcher();
            
            System.out.print("==> crawlerLogin\n\n");
            /**
             * @deprecated
             * @since version 0.6
             */
//          oldCrawlerLogin();
            /**
             * @since version 0.6
             */
            loginAllSessions();
            
            // readLine()是不包括'\n'的
            while ( (line = pubfileReader.readLine()) != null ) {
            	pubName = line;
                System.out.print("--> Pub: " + pubName + "\n\n");
                
            	try {
            	    /**
                     * @since version 0.6
                     */
            	    System.out.println("\n==> current SessionIndex is "+nextSessionIndex);
                    moveToNextSession();
                    System.out.println("==> nextSessionIndex will be "+nextSessionIndex);
                	
                	/**
                     * for each getAppsByXXX(), then sleep
                     */
                    pkgsResponse = pkgsFetcher.getAppsByPublisher(
                            crawlerSession.getMarketSession(),
                            pubName,
                            0,
                            maxAppOnePub,   // entriesCount 
                            1);             // free
                    try {
                        System.out.println("sleep for a while: "+smallSleep+"ms");
                        Thread.sleep(smallSleep);
                    } catch (InterruptedException e) {
                        System.err.print("EXCEPTION: ");
                        e.printStackTrace();
                    }
                	
                	// 跑到这边来的话，就说明上面的正常。reset as 0
                	nException = 0;
                	
                    if (pkgsResponse != null) {
                    	// TODO: for testing
    					System.out.println(pkgsResponse.toString());
    					
                    	nAppCount = pkgsResponse.getAppCount();
                    	if (nAppCount != 0) {
                    		// 遍历pkgsResponse
                    		for (appIndexinOnePub = 0; appIndexinOnePub < nAppCount; appIndexinOnePub++) {            	    			
                    			/**
                				 * get app info & extendedInfo
                				 */
                				app = pkgsResponse.getApp(appIndexinOnePub);
                				pkgName = app.getPackageName();
                				assetId = app.getId();
                				extendedInfo = app.getExtendedInfo();
                				oldCategory = extendedInfo.getCategory();
                				if (oldCategory != "") {
                					categoryInDisk = convertCategoryTitle(oldCategory);
                				}
                				
                				apkName = totalIndex + "-@-" + pkgName + ".apk";
                				apkPath = categoryInDisk + "/" + apkName;
                				
                				/**
                				 * compare app info with db
                				 */
                				
                	            /**
                	             * judge whether now is everyFetchNum or not
                	             */
                	            if (nFetchNum != 0 && nFetchNum%everyFetchNum == 0) {
                	                try {
                	                    System.out.print("==> sleep for a big time: "+bigSleep+"\n\n");
                	                    Thread.sleep(bigSleep);
                	                    
                	                    System.out.print("==> crawlerLogin\n\n");
                	                    /**
                	                     * @deprecated
                	                     * @since version 0.6
                	                     */
//                	                    oldCrawlerLogin();
                	                    /**
                	                     * @since version 0.6
                	                     */
                	                    loginAllSessions();
                	                    
                	                } catch (InterruptedException e) {
                	                	System.err.println("EXCEPTION: " + e);
                	                }
                	            }
                	            
                	            /**
                				 * download one app
                				 */
                				result = downloadApk(apkPath, assetId);
                				if (result == -1) {
                					continue;
                				}
                				
                				/**
                				 * compare md5 with db
                				 */
                				
                				currentIndex++;	totalIndex++;
                    		}
                    		
                    	} else {	// 0
                    		// TODO: write to file
                    		System.err.print("WARN: pkgsResponse is 0\n\n");
                    		break;
                    	}
                    	
                    } else {		// null
                    	// TODO: write to file
                    	// TODO: 这种情况应该是要重试
                    	System.err.print("WARN: pkgsResponse is null\n\n");
						break;
					}
                    
            	} catch (RuntimeException e) {	// 针对getAppsByPublisher的
            									// also will catch newDownloadApks()
            		System.err.print("EXCEPTION for "+pubName+": ");
            		e.printStackTrace();
            		
            		FileWriter errorPubWriter = null;
                    try {
                        errorPubWriter = new FileWriter( LogFiles.errorPubs, true );
                        errorPubWriter.write( pubName + "\n" );
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } finally {
                        if (errorPubWriter != null) {
                            errorPubWriter.close();
                        }
                    }
            		
            		// 不好连续异常3次。
            		nException++;
            		System.err.print("WARN: Current exception number is: " + nException + "\n\n");
            		if (nException > 2) {
            			// exit when has reached 3rd exception
            			System.out.print("==> We are going to exit!\n");
            			exitClean();
            			System.exit(1);
            		}
            		
            		// go to next publisher
            	}
                
            }   // end of outside "while"
            
            System.out.print("==> Have done all for " + pubFile + "\n");
            pubfileReader.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // TODO: java中出现异常，跟python中出现异常后
            // try里的分别还会执行不 //应该都不会了吧
        }
        
        exitClean();
	}
}

