package org.itx.jbalance.equeue.gwt.client.request;

import org.itx.jbalance.l0.h.RecordColor;

import com.google.gwt.text.shared.AbstractRenderer;

public class RecordColorRenderer extends AbstractRenderer<RecordColor> {
	
	private String nullString="--";

	
	public RecordColorRenderer() {
		super();
	}



	@Override
	public String render(RecordColor o) {
		if(o==null){
			return nullString;
		}else {
			return o.getColorName();
		}
	}
	}
