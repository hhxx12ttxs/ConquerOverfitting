package jcookie;

import jcookie.DomainMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import com.gargoylesoftware.htmlunit.util.Cookie;

public class CookieSorter {
	
	/*
	 * cookie_map is a hash with key = String domain 
	 * and a Set<Cookie> of cookies
	 */
	HashMap<String, ArrayList<JCookie>> cookie_map;
	Integer numCookies = 0;
	Integer numThirdPartyCookies = 0;
	String requestDomain;
	String[] results;
	
	/*
	 * constructor expects set of Cookies and the
	 * domain-name for which the request was made
	 */
	public CookieSorter(Set<Cookie> cookies, String requestDomain) {
		this.requestDomain = requestDomain.trim();
		this.cookie_map = new HashMap<String, ArrayList<JCookie>>();
		
		Iterator<Cookie> iter = cookies.iterator();
		String domain_part = "";
		String tld = "";
		String dom = "";
		String key = "";
		
		while (iter.hasNext()) { 
			this.numCookies ++;
			JCookie cookie = new JCookie(iter.next()); 
			domain_part = cookie.getDomain();
			
			System.out.println("CookieSorter: CHECKING: " + domain_part);
			
			StringTokenizer st = new StringTokenizer(domain_part,".");   
			
			int tokens = st.countTokens();
			String _toCompare = "";
			
			/*
			 * create an array to store the tokens in: e.g. 'de','ivwbox'
			 * we need this to create the match against our list of cookie-domains
			 * 
			 * BUG: why there is an index-out-of-bounds if the array-size is according to th tokens-size.
			 * allthough everay last element in array is 'null' ?
			 */
			String[] list = new String[tokens+1];
			
			for(int i=0; i <= tokens-1; i++) {
				if (st.hasMoreTokens()) {
					String _tmp = st.nextToken();
					if  ( (_tmp != null) && (_tmp.length() > 1) ) {
						list[i] = _tmp;
					} else {
						i--;  // reset count i
					}
				}
			}
			
			/**
			for (int i = 0; i <= tokens; i++) {
				System.out.println("ARRAY[" + i + "]: " + list[i]);
			}
			**/
			
			_toCompare = list[list.length -3] + "." + list[list.length -2];
			
			System.out.println("COMPARE: " + _toCompare);
			
			if (DomainMatcher.includes(_toCompare.toLowerCase())) {
				System.out.println("CookieSorter SETTING INFO for: " + _toCompare.toLowerCase());
				cookie.setInfo(DomainMatcher.get(_toCompare));
			}
			
			if (domain_part != null) {
				
				this.results = domain_part.split("\\.");
			
				tld = this.results[this.results.length-1];
				dom = this.results[this.results.length-2];
				key = dom.concat(".").concat(tld);
				
				addCookie(key, cookie);
				
			} else {
				addCookie("n.n.", cookie);
			}
		}
	}
	
	public ArrayList<JCookie> getThirdPartyCookies() {
		
		ArrayList<JCookie> cookies =  new ArrayList<JCookie>();
		Set<String> keys = this.cookie_map.keySet();
		String _tmp;
		
		Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			_tmp = i.next();
			if ( ! _tmp.equalsIgnoreCase(this.requestDomain)) {
				
				cookies.addAll(this.cookie_map.get(_tmp));
			}
		}
		return cookies;
	}
	
	
	public ArrayList<JCookie> getDomainCookies() {
		
		ArrayList<JCookie> cookies =  new ArrayList<JCookie>();
		Set<String> keys = this.cookie_map.keySet();
		String _tmp;
		
		Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			_tmp = i.next();
			if ( _tmp.equalsIgnoreCase(this.requestDomain)) {
				System.out.println("\n FOOBAR - is domain-cookie: " + _tmp);
				cookies.addAll(this.cookie_map.get(_tmp));
			}
		}
		return cookies;
	}
	
	public String[] getStringArray() {
		return this.results;
	}
	
	/*
	 * returns computed HashMap with
	 * key -> domain
	 * and 
	 * ArrayList of Cookies
	 */
	public HashMap<String,ArrayList<JCookie>> getCookieMap() {
		return this.cookie_map;
	}
	
	
	/*
	 * returns absolute number of cookies available
	 */
	public Integer cookiesLength()  {
		return this.numCookies;
	}
	
	/*
	 * returns number of 3rd party cookies
	 */
	public Integer thirdPartyCookiesLength() {
		return this.numThirdPartyCookies;
	}
	
	/*
	 * inserts Cookie cookie to HashMap - 
	 * if HashMap has key - adds to ArrayList
	 * if not - creates key 'domain' - and adds new ArrayList
	 * with given cookie
	 */
	private void addCookie(String domain, JCookie cookie) {
		
		if  (! this.requestDomain.equalsIgnoreCase(domain)){
			this.numThirdPartyCookies ++;
		}
		
		if (this.cookie_map.containsKey(domain)) {
			this.cookie_map.get(domain).add(cookie);
		} else {
			ArrayList<JCookie> list = new ArrayList<JCookie>();
			list.add(cookie);
			this.cookie_map.put(domain,list);
		}
		
	}
	

}

