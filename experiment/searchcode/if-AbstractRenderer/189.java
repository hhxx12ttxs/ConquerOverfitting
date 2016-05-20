package org.itx.jbalance.equeue.gwt.client.request;

import com.google.gwt.text.shared.AbstractRenderer;

public class BooleanRenderer extends AbstractRenderer<Boolean> {
	
	private String nullString="null";
	private String trueString="true";
	private String falseString="false";
	
	
	
	
	public BooleanRenderer() {
		super();
	}




	public BooleanRenderer(String nullString, String trueString,
			String falseString) {
		super();
		this.nullString = nullString;
		this.trueString = trueString;
		this.falseString = falseString;
	}




	@Override
	public String render(Boolean o) {
		if(o==null){
			return nullString;
		}else if (o==true){
			return trueString;
		}else{
			return falseString;
		}
	}
	}
