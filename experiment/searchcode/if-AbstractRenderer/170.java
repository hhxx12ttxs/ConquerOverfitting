package org.itx.jbalance.equeue.gwt.client.request;


import org.itx.jbalance.l2_api.dto.DouDTO;

import com.google.gwt.text.shared.AbstractRenderer;

public class DouRenderer extends AbstractRenderer<DouDTO> {
	@Override
	public String render(DouDTO o) {
		if(o==null){
			return "?????";

		}else{
			return o.getName();// +" ("+o.getPAddress()+")";
		}
	}
	}
