/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package core;

import base.NameValuePair;
import event.EventBean;

/**
 *
 * @author epaln
 */
public class Min extends Aggregate {
    public Min(String attribute, String aggregatedAttribute) {

        _attribute = attribute;
        _aggAttribute = aggregatedAttribute;
    }

    /**
     * compute the aggregated value (the min in this case) of the specified
     * attribute over the specified array of events
     *
     * @param evts
     * @return an Attribute/value pair carrying the aggregated
     * value and its attribute name
     */
    @Override
    protected NameValuePair aggregate(EventBean[] evts) {
        double min, val = 0;
        
        min = Double.parseDouble(evts[0].getValue(_attribute).toString());
        for (EventBean evt : evts) {
            val = Double.parseDouble(evt.getValue(_attribute).toString());
            if (val < min) {
                min = val;
            }
        }
         NameValuePair  res = new NameValuePair(_aggAttribute, min);
        return res;
    }
}

