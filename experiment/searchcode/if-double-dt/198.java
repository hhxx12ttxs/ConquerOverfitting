/**
 * Copyright (c) 2012 The Regents of the University of California.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package edu.berkeley.path.model_elements;

import java.util.*;
import org.joda.time.Interval;
import java.util.Map.Entry;

public class SplitRatioSet extends edu.berkeley.path.model_elements_base.SplitRatioSet implements SplitRatioMapProvider {
	
	@Override
	public SplitRatioMap getSplitRatioMap(Interval interval) {
		return slice(interval);
	}
		
  /**
   * Slice off an interval of time and return the matching items as a SplitRatioMap.
   * If, for a given node, in-link, out-link, and vtype, the time interval contains
   * more than one ratio, ignore all but the last. (This method does not change the
   * SplitRatioSet.) If the interval is disjoint from the profile interval, then use the
   * first item (if the interval is earlier) or the last item (if later).
   * If the interval does not exactly match the time coordinates of the data,
   * round to the nearest time point.
   **/
  public SplitRatioMap slice(Interval interval) {
    SplitRatioMap srMap = new SplitRatioMap();
    
    for (Entry<String, SplitRatioProfile> entryForNode : getProfileMap().entrySet()) {
      String nodeId = entryForNode.getKey();
      SplitRatioProfile profile = entryForNode.getValue();
      Double dt = profile.getSampleRate(); // defaults?
      Double t0 = profile.getStartTime(); // defaults?

      Integer nSamples = 0;
      
      Map<String,Map<String,Map<String,List<Double>>>> ratioMap = profile.getRatioMap();
      Map<String,Map<String,List<Double>>> firstMML = ratioMap.values().iterator().next();

      if (firstMML != null) {
        Map<String,List<Double>> firstML = firstMML.values().iterator().next();
        
        if (firstML != null) {
          List<Double> firstL = firstML.values().iterator().next();
          
          nSamples = firstL.size();
          // assume all time series have same size!
        }
      }
      
      if (nSamples == 0) {
        srMap.getRatioMap().put(nodeId, null);
        continue;
      }
      
      Integer index = ProfileUtil.getIndex(interval, t0, dt, nSamples);
      
      for (
        Entry<String,Map<String,Map<String,List<Double>>>>
          entryForInLink : profile.getRatioMap().entrySet()) {

        String inLinkId = entryForInLink.getKey();
        
        for (
          Entry<String,Map<String,List<Double>>>
            entryForOutLink : entryForInLink.getValue().entrySet()) {

          String outLinkId = entryForOutLink.getKey();
        
          for (
            Entry<String,List<Double>>
              entryForVtype : entryForOutLink.getValue().entrySet()) {
          
            String vtype = entryForVtype.getKey();
            
            List<Double> timeSeries = entryForVtype.getValue();

            Double ratioAtTime = timeSeries.get(index);
            // if null?

            Map<String,Map<String,Map<String,Double>>>
              srMapAtNode = srMap.getRatioMap().get(nodeId);

            if (srMapAtNode == null) {
              srMapAtNode = new HashMap<String,Map<String,Map<String,Double>>>();
              srMap.getRatioMap().put(nodeId, srMapAtNode);
            }
            
            Map<String,Map<String,Double>>
              srMapAtNodeAtInLink = srMapAtNode.get(inLinkId);

            if (srMapAtNodeAtInLink == null) {
              srMapAtNodeAtInLink = new HashMap<String,Map<String,Double>>();
              srMapAtNode.put(inLinkId, srMapAtNodeAtInLink);
            }

            Map<String,Double>
              srMapAtNodeAtInLinkAtOutLink = srMapAtNodeAtInLink.get(outLinkId);

            if (srMapAtNodeAtInLinkAtOutLink == null) {
              srMapAtNodeAtInLinkAtOutLink = new HashMap<String,Double>();
              srMapAtNodeAtInLink.put(outLinkId, srMapAtNodeAtInLinkAtOutLink);
            }
            
            srMapAtNodeAtInLinkAtOutLink.put(vtype, ratioAtTime);
          }
        }
      }
    }
    
    return srMap;
  }
  
  /**
   * Get the profile at the specified node.
   * Creates the map if it doesn't exist, returns null if the profile doesn't exist.
   */
  public SplitRatioProfile getSplitRatioProfileAt(Node node) {
    if (null == getProfile()) {
      setProfile(new HashMap<String,edu.berkeley.path.model_elements_base.SplitRatioProfile>());
    }
    
    return (SplitRatioProfile)getProfile().get(node.getId());
  }

  // TODO find a better way to expose Map<> access.
  
  /**
   * Set the profile map. Same as setProfiles(), but works with a map of String to SplitRatioProfile.
   */
  @SuppressWarnings("unchecked")
public void setProfileMap(Map<String,SplitRatioProfile> value) {
    setProfile((Map<String,edu.berkeley.path.model_elements_base.SplitRatioProfile>)(Map<?,?>)value);
  }

  /**
   * Get the profile map. Same as getProfiles(), but works with a map of SplitRatioProfile.
   * Never returns null (creates the map if it doesn't exist).
   */
  @SuppressWarnings("unchecked")
public Map<String,SplitRatioProfile> getProfileMap() {
    if (null == getProfile()) {
      setProfile(new HashMap<String,edu.berkeley.path.model_elements_base.SplitRatioProfile>());
    }
    return (Map<String,SplitRatioProfile>)(Map<?,?>)getProfile();
  }
  
  public Long getLongId() {
    return Long.parseLong(getId().toString());
  }
  
  public void setId(Long id) {
    setId(id.toString());
  }
  
  public Long getLongProjectId() {
    return Long.parseLong(getProjectId().toString());
  }

}

