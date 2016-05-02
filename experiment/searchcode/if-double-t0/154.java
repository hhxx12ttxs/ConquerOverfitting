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

import org.joda.time.Interval;

class ProfileUtil {
  /**
   * Compute the index of the last profile entry that is the
   * interval. If the interval doesn't include a data point, default to
   * the first or last index in the data.
   * Note that the endpoints of the interval are rounded to the nearest
   * time point defined by t0, dt, and nSamples.
   * (It might be useful, as an option, to floor instead of round.)
   * 
   * @param interval      time range where a match is desired
   * @param t0            time of start of data, in seconds from midnight
   * @param dt            time step size, in seconds
   * @param nSamples      number of steps of data contained in profile
   * @return index of matching data
   **/
  public static Integer getIndex(
      Interval interval,
      Double t0,
      Double dt,
      Integer nSamples) {
    
    Integer index;

    org.joda.time.DateTime midnight = interval.getStart().withTimeAtStartOfDay(); // DST?
    org.joda.time.DateTime dataStart = midnight.plusSeconds((int)Math.round(t0));
    org.joda.time.DateTime dataEnd = dataStart.plusSeconds((int)Math.round(dt * (nSamples-1)));
    Interval dataInterval = new Interval(dataStart, dataEnd);
    
    Interval overlap = dataInterval.overlap(interval);

    if (overlap == null) {
      if (interval.isBefore(dataInterval)) {
        index = 0;
      }
      else {
        index = nSamples-1;
      }
    }
    else {
      org.joda.time.DateTime dataFound = overlap.getEnd();
      org.joda.time.Duration timeFromStart = 
       (dataInterval.withEnd(dataFound)).toDuration();
      Double steps = timeFromStart.getMillis() / (1000 * dt);
      index = (int)Math.round(steps);
      
      if (index < 0) {
        index = 0;
      }
      else if (index > nSamples-1) {
        index = nSamples-1;
      }
    }
    return index;
  }
}

