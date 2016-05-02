package com.gwt.swf.client.upload;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * 
 * @author sharad.gaur
 * 
 */
public class SWFUpload {
    private JavaScriptObject jsObj;
    private SWFSettings settings;

    public static enum QUEUE_ERROR {
	QUEUE_LIMIT_EXCEEDED(-100), FILE_EXCEEDS_SIZE_LIMIT(-110), ZERO_BYTE_FILE(
		-120), INVALID_FILETYPE(-130);
	private final int value;

	public int getValue() {
	    return value;
	}

	QUEUE_ERROR(int code) {
	    this.value = code;
	}

    };

    public static enum UPLOAD_ERROR {
	HTTP_ERROR(-200), MISSING_UPLOAD_URL(-210), IO_ERROR(-220), SECURITY_ERROR(
		-230), UPLOAD_LIMIT_EXCEEDED(-240), UPLOAD_FAILED(-250), SPECIFIED_FILE_ID_NOT_FOUND(
		-260), FILE_VALIDATION_FAILED(-270), FILE_CANCELLED(-280), UPLOAD_STOPPED(
		-290);

	private final int value;

	public int getValue() {
	    return value;

	}

	UPLOAD_ERROR(int value) {
	    this.value = value;
	}
    };

    public static enum FILE_STATUS {
	QUEUED(-1), IN_PROGRESS(-2), ERROR(-3), COMPLETE(-4), CANCELLED(-5);

	private final int value;

	public int getValue() {
	    return value;

	}

	FILE_STATUS(int value) {
	    this.value = value;
	}

    };

    public static enum BUTTON_ACTION {
	SELECT_FILE(-100), SELECT_FILES(-110), START_UPLOAD(-120);

	private final int value;

	public int getValue() {
	    return value;

	}

	BUTTON_ACTION(int value) {
	    this.value = value;
	}
    };

    public static enum CURSOR {
	ARROW(1), HAND(-2);

	private final int value;

	public int getValue() {
	    return value;

	}

	CURSOR(int value) {
	    this.value = value;
	}

    };

    public static enum WINDOW_MODE {
	WINDOW("window"), TRANSPARENT("transparent"), OPAQUE("opaque");

	private final String value;

	public String getValue() {
	    return value;

	}

	WINDOW_MODE(String value) {
	    this.value = value;
	}
    };

    public SWFUpload(SWFSettings settings) throws Exception {
	this.settings = settings;
	try {
	    this.jsObj = getSWFObject(settings);
	} catch (Exception e) {
	    throw e;
	}
    }

    public JavaScriptObject getJsObj() {
	return jsObj;
    }

    public void setJsObj(JavaScriptObject jsObj) {
	this.jsObj = jsObj;
    }

    public SWFSettings getSettings() {
	return settings;
    }

    public void setSettings(SWFSettings settings) {
	this.settings = settings;
    }

    public native void startUpload()/*-{
                            			try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.startUpload();
                            			}catch(e){
                            				throw e;
                            			}
                            		}-*/;

    public native void startUpload(int file_id)/*-{
                            		try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.startUpload(file_id);
                            			}catch(e){
                            				throw e;
                            			}
                            		}-*/;

    public native void cancelUpload(String file_id)/*-{
                            		try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.cancelUpload(file_id);
                            			}catch(e){
                            				throw e;
                            			}
                            		
                            		}-*/;

    public native void stopUpload()/*-{
                            			try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.stopUpload();
                            			}catch(e){
                            				throw e;
                            			}
                            		
                            		}-*/;

    public native void setButtonDisabled(boolean isDisabled)/*-{
                            			try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.setButtonDisabled(isDisabled);
                            			}catch(e){
                            				throw e;
                            			}
                            	
                            		}-*/;

    public native void setButtonAction(int buttonAction)/*-{
                            			try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.setButtonAction(buttonAction);
                            			}catch(e){
                            				throw e;
                            			}
                            		
                            		}-*/;

    public native void setButtonCursor(int buttonCursor)/*-{
                            		try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.setButtonCursor(buttonCursor);
                            			}catch(e){
                            				throw e;
                            			}
                            		}-*/;

    public native void setButtonText(String text)/*-{
                            		try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.setButtonText(text);
                            			}catch(e){
                            				throw e;
                            			}
                            		}-*/;

    public native void setButtonTextStyle(String css_style_text)/*-{
                            		try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.setButtonTextStyle(css_style_text);
                            			}catch(e){
                            				throw e;
                            			}
                            		
                            		}-*/;

    public native void setButtonImageURL(String url)/*-{
                            		try{
                            				var swfobj = this.@com.gwt.swf.client.upload.SWFUpload::getJsObj()();
                            				swfobj.setButtonImageURL(url);
                            			}catch(e){
                            				throw e;
                            			}
                            		
                            		}-*/;

    private native JavaScriptObject getSWFObject(SWFSettings settings) /*-{
                      	var suo;
                      	try
                      	{
                      		if (typeof($wnd.SWFUpload) === "undefined") {
                      	 		
                      				throw "undefined";

                      				return;
                      		}
                      			suo = new $wnd.SWFUpload(settings);


                      		}catch(e){
                      			throw e;
                      		}
                      		return suo;
                      }-*/;

}

