package org.itx.jbalance.equeue.gwt.client.request;

import org.itx.jbalance.l0.o.Dou;

import com.google.gwt.text.shared.AbstractRenderer;

public class DouRenderer extends AbstractRenderer<Dou> {
	@Override
	public String render(Dou o) {
		if(o==null){
			return "?????";

		}else{
			return o.getName() +" ("+o.getPAddress()+")";
		}
	}
	}
