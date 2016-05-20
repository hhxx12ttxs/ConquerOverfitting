package com.example.mailmanapp;




import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

/**
 * A class for determining the URL's of a MM2.0 list to download based on the
 * kittystore/scripts.py file on the hyperkitty github account. this class can
 * output to console the month-years of a lists archive in order to retrieve the
 * text-archives for each month where there was correspondence on a list.
 * presumably, next we'd retreive the .txt file and parse it for a search key to
 * help the user determine whether the key occurred.
 * 
 * @Author Chris Cargile
 **/
public class Computater {

	static enum MONTHS {
		January, February, March, April, May, June, July, August, September, October, November, December
	};

	private ArrayList<String> answer;
	private String htmlSource;
	private File currentFile = null;
	/**
	 * get a url-specified list and return a list of all the URL's for the
	 * Monthly files associated with it.
	 * 
	 * @param args
	 *            either a single arg or optionally 3 args, where the starting
	 *            year and ending year are provided if desired
	 **/
	public Computater() {
		answer = getArchiverMonths();
	}

	public ArrayList<String> getArchiverMonths() {
		String url = "http://charlestonwebapps.com/pipermail/webapps_charlestonwebapps.com/";
		String url2= "http://lists.csclug.org/pipermail/csclug";
		htmlSource = stringifyHTMLSource(url);
		ArrayList<String> result2 = new ArrayList<String>();	
		/*
		int startYear = 1988;
		final Calendar thisYear = Calendar.getInstance();
		int endYear = thisYear.get(Calendar.YEAR);
		int year = startYear;
		ArrayList<String> result = new ArrayList<String>();
		for (; year >= startYear && year <= endYear; year++) {
			for (Object Month : MONTHS.values()) {
				String month = (String) Month.toString();
				String target = year + "-" + month;
				result.add(target);
			}
		}
		for (String a : result)
			result2.add(new String(a));

		for (int i = 0; i < result.size(); i++) {
			String word = result.get(i);
			if (!htmlSource.contains(word)) {
				result2.remove(word);
			}
		}
		//result2.clear();
		//result2.add("2014-January");
		//result2.add(htmlSource);
		Log.w("getArchiverMonths",result2.toString());
		*/
		result2.add(htmlSource);
		return result2;
	}

	public String stringifyHTMLSource(String url) {
		URL url2;
		HttpURLConnection http;
		InputStream in;
		String htmlSource = "";
		try {
			url2 = new URL(url);
			http = (HttpURLConnection) url2.openConnection();
			//Log.v(Computater.class.toString(),"inside stringify..()");
			http.setRequestMethod("GET");
			String line = http.getInputStream().toString();
			in = new BufferedInputStream(http.getInputStream());
			Scanner rd = new Scanner(in);
			//BufferedReader rd = new Reader(in);
			String line2 = "";//in.toString();//rd.readLine();
			while (rd.hasNextLine()) {
				line2 = rd.nextLine();
				if(line2!=null)
					htmlSource+=line2;
				Log.println(Log.ASSERT, "rd.nextLine",line2);
			}
			Log.println(Log.ASSERT, "htmlSource",htmlSource);

		} catch (MalformedURLException e) {
			Log.e("MalformedURLException",e.getStackTrace().toString());
		} catch (IOException e) {
			StackTraceElement[] arr = e.getStackTrace();
			String trace=null;
			for(StackTraceElement a:arr){
				trace+=a.toString()+"\n";
			}
			Log.e("IOException",e.getStackTrace().toString());
			Log.e("IOException",e.toString()+"="+trace);
			
		}
		return htmlSource;
	}

	public String getMonths() {
		String result="";
		File directory = new File("archives/");
		//directory.mkdir();
		String currentFileContents = "";
		String archiveMonthEmailsURL = "";
		FileWriter writer = null;
		String s = "monthEmailActivityOccur.txt";//"2014-January";
		//archiveMonthEmailsURL = "http://lists.csclug.org/pipermail/csclug/" + s + "/thread.html";
		//currentFileContents = stringifyHTMLSource(archiveMonthEmailsURL);
		//Log.v("getArhiver",currentFileContents);
		//return currentFileContents;}
		for(String s2:new String[2]){//getArchiverMonths()){
			archiveMonthEmailsURL = "http://lists.csclug.org/pipermail/csclug/" + s + "/thread.html";
			currentFileContents = htmlSource;//stringifyHTMLSource(archiveMonthEmailsURL);
			try {
//				directory.mkdir();
				currentFile = new File(directory+s);
				currentFile.createNewFile();
				writer = new FileWriter(currentFile);
				writer.write(currentFileContents);
				writer.flush();
				writer.close();
//				result += getEmailsFromMonthHTMLSource(currentFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;//months;*/
	}
	
	public String getEmailsFromMonthHTMLSource(File htmlSources) {
		if(htmlSources!=null && htmlSources.exists())
			htmlSources = currentFile;
		else{
			Log.println(Log.ASSERT, "getEmailsFromMonthHTMLSource","using String HTML");
			htmlSources = new File("/mnt/sdcard/temp.txt");
			FileWriter writer;
			try {
				htmlSources.createNewFile();
				writer = new FileWriter(htmlSources);
			 	writer.write(htmlSource);
				writer.flush();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		String out="";
		
		try{
			Document doc = Jsoup.parse(htmlSource);
			//Document doc = Jsoup.parse(htmlSources, "UTF-8");
			Elements links = doc.select("a[href]"); // a with href
			/*links.remove(0);
			links.remove(0);
			links.remove(0);
			links.remove(0);
			links.remove(links.size()-1);
			links.remove(links.size()-1);
			links.remove(links.size()-1);
			links.remove(links.size()-1);
			
			*/
			int i=1;
			for(Element e:links){
				out+=i++ +")"+"-"+e.attr("href")+"\n\n"; //+e.toString()
			}
		}
			
		catch(Exception e){
			System.out.println(e);
		}
		return out;
	}
	/*
	public static void main(String[] args) {
		Computater c = new Computater();
		c.getMonths();
	}*/

	
	//	System.out.println(links.toString());
	/*String out="";
	String html = "";
	Scanner scan = new Scanner(htmlSource);
	while(scan.hasNext()){
		html+=scan.nextLine();
	}
	scan.close();
	while(html.contains("--><LI><A HREF=\"")){
		html=html.substring(html.indexOf("<LI><A HREF=\""),
				html.indexOf("</A>"));
		System.out.println(html+"<BR><BR>");
	}*/

}

