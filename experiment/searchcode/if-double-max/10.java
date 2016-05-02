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
public class Max  extends Aggregate {
    
     public Max(String attribute, String aggregatedAttribute) {

        _attribute = attribute;
        _aggAttribute = aggregatedAttribute;
    }

     /**
     * compute the aggregated value (the max in this case) of the specified
     * attribute over the specified array of events
     *
     * @param evts
     * @return an Attribute/value pair carrying the aggregated
     * value and its attribute name
     */
    @Override
    protected NameValuePair aggregate(EventBean[] evts) {
         double max = 0, val = 0; 
        for (EventBean evt : evts) {
            val= Double.parseDouble(evt.getValue(_attribute).toString());
            if(val>max){
                max= val;
            }
        }
        NameValuePair  res = new NameValuePair(_aggAttribute, max);
        return res;
    }
    
}

