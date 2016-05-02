/**********************************************************************************
* $URL:https://source.sakaiproject.org/svn/osp/trunk/presentation/api/src/java/org/theospi/portfolio/presentation/model/PresentationItemComparator.java $
* $Id:PresentationItemComparator.java 9134 2006-05-08 20:28:42Z chmaurer@iupui.edu $
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/
package org.theospi.portfolio.presentation.model;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.metaobj.shared.model.Id;

public class PresentationItemComparator implements Comparator {
   protected final transient Log logger = LogFactory.getLog(getClass());

   /**
    * Compares its two arguments for order.  Returns a negative integer,
    * zero, or a positive integer as the first argument is less than, equal
    * to, or greater than the second.<p>
    * <p/>
    * The implementor must ensure that <tt>sgn(compare(x, y)) ==
    * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
    * implies that <tt>compare(x, y)</tt> must throw an exception if and only
    * if <tt>compare(y, x)</tt> throws an exception.)<p>
    * <p/>
    * The implementor must also ensure that the relation is transitive:
    * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
    * <tt>compare(x, z)&gt;0</tt>.<p>
    * <p/>
    * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
    * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
    * <tt>z</tt>.<p>
    * <p/>
    * It is generally the case, but <i>not</i> strictly required that
    * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
    * any comparator that violates this condition should clearly indicate
    * this fact.  The recommended language is "Note: this comparator
    * imposes orderings that are inconsistent with equals."
    *
    * @param o1 the first object to be compared.
    * @param o2 the second object to be compared.
    * @return a negative integer, zero, or a positive integer as the
    *         first argument is less than, equal to, or greater than the
    *         second.
    * @throws ClassCastException if the arguments' types prevent them from
    *                            being compared by this Comparator.
    */
   public int compare(Object o1, Object o2) {
      PresentationItemDefinition q1 = (PresentationItemDefinition)o1;
      PresentationItemDefinition q2 = (PresentationItemDefinition)o2;

      if (o1 == null && o2 == null) return 0;
      else if (o1 == null) return -1;
      else if (o2 == null) return 1;

      Id id1 = q1.getId() == null ? q1.getNewId() : q1.getId();
      Id id2 = q2.getId() == null ? q2.getNewId() : q2.getId();
      
      if (id1 == null && id2 != null) return -1;
      else if (id1 != null && id2 == null) return 1;
      else if (id1 != null && id1.equals(id2)) return 0;  //if the ids are the same, should be the same object
      
      long ord1 = q1.getNewSequence();
      long ord2 = q2.getNewSequence();

      if (ord1 < ord2) return -1;
      if (ord1 > ord2) return 1;

      // they are equal, return opposite of order
      ord1 = q1.getSequence();
      ord2 = q2.getSequence();

      if (ord1 < ord2) return 1;
      if (ord1 > ord2) return -1;

      return 0;
   }
}

