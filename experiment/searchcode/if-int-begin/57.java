package ru.yandex.strictweb.scriptjava.base;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.yandex.strictweb.scriptjava.base.util.Console;
import ru.yandex.strictweb.scriptjava.base.util.IntervalIdentifier;
import ru.yandex.strictweb.scriptjava.base.util.TimeoutIdentifier;

public class StrictWeb {
	public static DOMEvent globalEvent;
	/** ???? ????? ????????? strict-web event target */
	public static Node swTarget;
	
	public final static Window window;
	public final static Document document;
	public final static Console console;
	
	static {
		window = getWindow();
		document = getDocument();
		console = getConsole();
	}
	
	public static final VoidDelegate NOP = new VoidDelegate<Object>() {
        public void voidDelegate(Object arg) {
        }
    };
    
    /**
     * this is a magic method for compiler plugin
     * @return method or field name. Example:<br>
     * ajaxName(obj.getLastName()) - becomes "lastName"<br>
     * ajaxName(obj.middleName) - becomes "middleName"<br>
     * ajaxName(MyClass.class) - becomes "myClass"
     */
    @Native
    public final static String ajaxName(Object o) {
        return null;
    }
    
    /**
     * this is a magic method for compiler plugin
     * It makes ajax async call
     */
    @Native
    public final static <T> void ajaxAsyncCall(T a, VoidDelegate<T> callback) {
        return;
    }

    /**
     * this is a magic method for compiler plugin
     * It makes ajax async call
     */
    @Native
    public final static <T> void ajaxAsyncCallWithErrors(T a, VoidDelegate<T> callback, VoidDelegate<Throwable> errorHandler) {
        return;
    }
	
	@NativeCode("{var f = new Function(code); return f();}" +
		";String.prototype.trim = function() {" +
		"return this.replace(/^\\s+/, '').replace(/\\s+$/, '');" +
		"}"
	)
	public static Object evalFunction(String code) {
		return null;
	}
	
    @MayBeExcluded
	@NativeCode("{return str" +
	    ".replace(%toHTML_ampRE%, '&amp;')" +
	    ".replace(%toHTML_ltRE%, '&lt;')" +
		".replace(%toHTML_gtRE%, '&gt;')" +
		".replace(%toHTML_quotRE%, '&quot;');};" +
		"var %toHTML_ampRE%=/&/g;"+
		"var %toHTML_ltRE%=/</g;" +
		"var %toHTML_gtRE%=/>/g;" +
		"var %toHTML_quotRE%=/\\\"/g;")
	public static String toHTML(String str) {
		return null;
	}

    @MayBeExcluded
    @NativeCode("{return str" +
        ".replace(%toJSON_slash%, '\\\\\\\\')" +
        ".replace(%toJSON_quotRE%, '\\\\\"');};" +
        "var %toJSON_slash%=/\\\\/g;"+
        "var %toJSON_quotRE%=/\\\"/g;")
    public static String toJSON(String str) {
        return null;
    }
    
    
    @MayBeExcluded
	@NativeCode(
	    "{if(typeof val != 'number') return '';" +
        "return val.toFixed(2).replace(%round2_regexp%, '$&'+sep);};" +
		"var %round2_regexp%=/\\d{1,3}(?=(\\d{3})+(?!\\d))/g;")
	public static String round2(double val, String sep) {
		return null;
	}
	
    @MayBeExcluded
	@NativeCode("{return document;}")
	public static Document getDocument() {
		return null;
	};

    @MayBeExcluded
	@NativeCode("{return typeof console!='undefined' && typeof console.debug == 'function' ? console : null;}")
	public static Console getConsole() {
		return null;
	};
	
    @MayBeExcluded
	@NativeCode("{var n = document.getElementById(id); return null==n?null:%NodeBuilder%.%wrap%(n);}")
	public static NodeBuilder $(String id) {return null;}

    @MayBeExcluded
	@NativeCode("{return document.getElementById(id);}")
    public static Node $$(String id) {return null;}
	
    @MayBeExcluded
	@NativeCode("{return window;}")
	public static Window getWindow() {
		return null;
	};

	@NativeCode("{if(tagName!=null) return document.createElement(tagName);}")
	final public static <N extends Node> N createNode(String tagName) {
		return null;
	}

	@NativeCode("{return document.createTextNode(s);}")
	final public static Node textNode(String s) {
		return null;
	}
	
    @MayBeExcluded
	final public static NodeBuilder EL(String tagName) {
		return new NodeBuilder(tagName);
	}
	
	@NativeCode("{for(var k in src) if(k!='prototype')dst[k] = src[k];return dst;}")
	public static void extend(Object dst, Object src) {
	}
	
    @MayBeExcluded
	@NativeCode("{e = %StrictWeb%.%globalEvent%;if(!e) return;" +
//			"try{if(document.dispatchEvent)document.dispatchEvent(e);}catch(qq){}" +
			"e.stopPropagation?e.stopPropagation():e.cancelBubble=true;" +
			"return e;}")
	public static DOMEvent stopEvent() {
		return null;
	}

    @MayBeExcluded
	@NativeCode("{if(cb==null){obj[name]=null;return;};obj[name] = typeof cb=='function'?cb:function(ev, nullNode) {%StrictWeb%.%swTarget%=null;%StrictWeb%.%globalEvent%=ev||window.event;if(cb&&cb.%delegate%)return cb.%delegate%(nullNode ? null : this);return false;}}")
	public static void setDOMEventCallback(Object obj, String name, CommonDelegate<Boolean, Node> cb) {
	}
	
    @MayBeExcluded
	@NativeCode("{if(cb==null){obj[name]=null;return;};obj[name] = typeof cb=='function'?cb:function(ev) {if(cb&&cb.%voidDelegate%) cb.%voidDelegate%(obj);return false;}}")
	public static void setVoidEventCallback(Object obj, String name, VoidDelegate<?> cb) {
	}

	/**
	 * https://developer.mozilla.org/en/DOM/element.removeEventListener
	 * @return the function, that can be used to call
	 */
    @MayBeExcluded
	@NativeCode("{obj.removeEventListener(event, func, useCapture);}")
	public static void removeEventListener(Object obj, String event, JavaScriptFunction func, boolean useCapture) {
	}
	
	/**
	 * https://developer.mozilla.org/en/DOM/element.addEventListener
	 * @return the function, that can be used to call
	 */
    @MayBeExcluded
	@NativeCode("{if(cb==null)return null;var f = function(ev, nullNode) {%StrictWeb%.%swTarget%=null;%StrictWeb%.%globalEvent%=ev||window.event;return cb.%delegate%(nullNode ? null : this);};obj.addEventListener(event, f, useCapture); return f;}")
	public static JavaScriptFunction addEventListener(Object obj, String event, CommonDelegate<Boolean, Node> cb, boolean useCapture) {
		return null;
	}
	
    @MayBeExcluded
	public static void onLoadWindow(VoidDelegate<Window> cb) {
		setVoidEventCallback(window, "onload", cb);
	}
	
	@NativeCode("{return new RegExp(base, flags);}")
	public static String regExp(String base, String flags) {
		return null;
	}
	
	/**
	 * Compare dates
	 * @return result eq 0, if dates are equal. <br> 
	 *   &lt;0 if d1&lt;d2 <br>
	 *   &gt;0 if d1&gt;d2
	 */
    @MayBeExcluded
	public static int compareDates(Date d1, Date d2) {
		int t1 = d1.getYear(), t2 = d2.getYear();
		if(t1<t2) return -1; if(t1>t2) return 1;
		t1 = d1.getMonth(); t2 = d2.getMonth();
		if(t1<t2) return -1; if(t1>t2) return 1;
		t1 = d1.getDate(); t2 = d2.getDate();
		if(t1<t2) return -1; if(t1>t2) return 1;

		return 0;
	}

    @MayBeExcluded
	@NativeCode("{if(d1<d2)return -1;if(d1>d2)return 1;return 0;}")
	public static int compareTimes(Date d1, Date d2) {
		return 0;
	}
	
    @MayBeExcluded
	@NativeCode("{if(str1<str2)return -1;if(str1>str2)return 1;return 0;}")
	public static int compareStrings(String str1, String str2) {
		return 0;
	}
	
    @MayBeExcluded
	@NativeCode("{if(n1<n2)return -1;if(n1>n2)return 1;return 0;}")
	public static int compareNumbers(Object n1, Object n2) {
		return 0;
	}
	
    @MayBeExcluded
	@NativeCode("{return list.sort(comparator.compare);}")
	public static <T> List<T> jsSortList(List<T> list, Comparator<T> comparator) {
		return null;
	}
	
    @MayBeExcluded
    @NativeCode("{return o[property];}")
	public static Object jsGetObjectProperty(Object o, String property) {
	    return null;
	}
	
    @MayBeExcluded
    @NativeCode("{return o[property]=value;}")
    public static Object jsSetObjectProperty(Object o, String property, Object value) {
        return null;
    }
    
    @MayBeExcluded
    @NativeCode("{delete o[property];}")
    public static void jsDelObjectProperty(Object o, Object property) {
    }
    
    @MayBeExcluded
	@NativeCode("{return isNaN(o);}")
	public static boolean isNaN(Object o) {
		return false;
	}
	
	/**
	 * removes an obj from the list
	 * @return new list
	 */
    @MayBeExcluded
	public static <T> List<T> arrayRemove(List<T> list, T obj) {
		List<T> res = jsNewList();
		for(T o : list) if(o!=obj) res.add(o);
		return res;
	}
	
    @MayBeExcluded
	public static String getCookie(String name) {
	    String dc = document.cookie;
	    String prefix = name + "=";
	    int begin = dc.indexOf("; " + prefix);
	    if (begin == -1) {
	        begin = dc.indexOf(prefix);
	        if (begin != 0) return null;
	    } else {
	        begin += 2;
	    }
	    int end = dc.indexOf(";", begin);
	    if (end == -1) {
	        end = dc.length();
	    }
	    return window.unescape(dc.substring(begin + prefix.length(), end));
	}
	
    @MayBeExcluded
	public static void setCookie(String name, String value) {
		setCookieFull(name, value, null, null, null, false);
	}
	
    @MayBeExcluded
	public static void setCookieFull(String name, String value, Date expires, String path, String domain, boolean secure) {
		if(null==expires) {
			expires = new Date();
			expires.setYear(expires.getYear() + 10);
		}
//		alertWindow(getDocument().cookie);
		document.cookie = name + "=" + window.escape(value) +
			("; expires=" + expires.toGMTString()) +
			(path!=null ? "; path=" + path : "") +
			(domain!=null ? "; domain=" + domain : "") +
			(secure ? "; secure" : "");

	}
	
    @MayBeExcluded
	@NativeCode("{var iid=setInterval(function(){cb.%voidDelegate%(iid);}, millis);return iid;};")
	public static IntervalIdentifier setInterval(VoidDelegate<IntervalIdentifier> cb, int millis) {
		return null;
	}

    @MayBeExcluded
	@NativeCode("{var iid=setTimeout(function(){cb.%voidDelegate%(iid);}, millis);return iid;};")
	public static TimeoutIdentifier setTimeout(VoidDelegate<TimeoutIdentifier> cb, int millis) {
		return null;
	}

    @MayBeExcluded
    @NativeCode("{var iid=setInterval(f, millis);return iid;};")
    public static IntervalIdentifier setIntervalFunc(JavaScriptFunction f, int millis) {
        return null;
    }
    
    @MayBeExcluded
    @NativeCode("{var iid=setTimeout(f, millis);return iid;};")
    public static TimeoutIdentifier setTimeoutFunc(JavaScriptFunction f, int millis) {
        return null;
    }	
	
    @MayBeExcluded
	public static void clearInterval(IntervalIdentifier intervalId) {
		window.clearInterval(intervalId);
	}

    @MayBeExcluded
	public static void clearTimeout(TimeoutIdentifier timeoutId) {
		window.clearTimeout(timeoutId);
	}
	
    @MayBeExcluded
	@NativeCode("{return typeof obj;}")
	public static String jsTypeOf(Object obj) {
		return null;
	}

    @MayBeExcluded
	@NativeCode("{return obj._isEnum;}")
	public static boolean isEnum(Object obj) {
		return false;
	}

    @MayBeExcluded
	@NativeCode("{return obj instanceof Date;}")
	public static boolean isInstanceOfDate(Object obj) {
		return false;
	}

    @MayBeExcluded
	@NativeCode("{return obj instanceof Date || (typeof obj.length != 'undefined' && typeof obj['0'] != 'undefined');}")
	public static boolean isInstanceOfArray(Object obj) {
		return false;
	}
	
    @MayBeExcluded
	@NativeCode("{return obj.tagName;}")
	public static boolean isInstanceOfNode(Object obj) {
		return false;
	}
	
    @MayBeExcluded
	public static String dateToString(Date d) {
		if(null==d) return "";
		return (d.getDate()<10?"0":"") + d.getDate() + '.' + (d.getMonth()<9?"0":"") + (d.getMonth()+1) +'.' + d.getYear();
	}

    @MayBeExcluded
	public static String dateTimeToString(Date d) {
		if(null==d) return "";
		return dateToString(d) + " " + (d.getHours()<10?"0":"") + d.getHours()+":"+(d.getMinutes()<10?"0":"")+d.getMinutes();
	}
	
    @MayBeExcluded
	public static String dateToStringSmart(Date d) {
		if(null==d) return "";
		return d.getHours()==0 && d.getMinutes()==0 ? 
			dateToString(d) : dateTimeToString(d);
	}

    @MayBeExcluded
	@NativeCode("{return list.join(sep);}")
	public static String jsJoinList(Object list, String sep) {
		return null;
	}

    @MayBeExcluded
	public static void scrollToVisible(Node n) {
	    int top=0;
        for(; n!=null;n = n.offsetParent) {
            top  += n.offsetTop;
        }
        window.scroll(0, top);
	}
	
    @MayBeExcluded
	@NativeCode("{return {};}")
	public static <K, V> Map<K, V> jsNewMap() {return null;}
	
    @MayBeExcluded
    @NativeCode("{return {};}")
    public static <V> Set<V> jsNewSet() {return null;}

    @MayBeExcluded
    @NativeCode("{return [];}")
    public static <V> List<V> jsNewList() {return null;}
}

