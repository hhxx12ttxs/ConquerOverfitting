/*
 * Copyright 2006 Simon Pepping.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: PenaltyImpl.java 38 2006-06-26 18:39:10Z simon $ */

package cc.creativecomputing.gui.text.linebreaking;

/**
 * A generalized Knuth PenaltyImpl
 */
public class PenaltyImpl implements Penalty {

    private MinOptMax widthBefore;
    private MinOptMax widthAfter;
    private int value;
    private boolean hyphenated = false;
    
    /**
     * Construct a penalty with the default values of 0, 0, 0
     */
    public PenaltyImpl() {
        this.widthBefore = new MinOptMax();
        this.widthAfter = new MinOptMax();
        this.value = 0;
    }

    /**
     * Construct a penalty with the specified penalty value
     * and the default values for the widths
     * @param value the penalty value
     */
    public PenaltyImpl(int value) {
        this.widthBefore = new MinOptMax();
        this.widthAfter = new MinOptMax();
        this.value = value;
    }

    /**
     * Construct a penalty with the specified values
     * for the penalty and the widths
     * @param value the penalty value
     * @param widthBefore the penalty width before the linebreak
     * @param widthAfter the penalty width after the linebreak
     * @param hyphenated true if this penalty is a hyphenated break
     */
    public PenaltyImpl(int value, MinOptMax widthBefore, MinOptMax widthAfter, boolean hyphenated) {
        this.widthBefore = widthBefore;
        this.widthAfter = widthAfter;
        this.value = value;
        this.hyphenated = hyphenated;
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Element#isLBOpp()
     */
    public boolean isLBOpp() {
        return true;
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Element#isSuppressible()
     */
    public boolean isSuppressible() {
        return true;
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Element#isBP()
     */
    public boolean isBP() {
        return false;
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Element#getWidth()
     */
    public MinOptMax getWidth() {
        return new MinOptMax();
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#getWidthBefore()
     */
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#getWidthBefore()
     */
    public MinOptMax getWidthBefore() {
        return widthBefore;
    }
    
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#setWidthAfter(nl.leverkruid.spepping.gkplinebreaking.MinOptMax)
     */
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#setWidthAfter(nl.leverkruid.spepping.gkplinebreaking.MinOptMax)
     */
    public void setWidthAfter(MinOptMax widthAfter) {
        this.widthAfter = widthAfter;
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#getWidthAfter()
     */
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#getWidthAfter()
     */
    public MinOptMax getWidthAfter() {
        return widthAfter;
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#isHyphenated()
     */
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#isHyphenated()
     */
    public boolean isHyphenated() {
        return hyphenated;
    }

    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#toStringBefore()
     */
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#toStringBefore()
     */
    public String toStringBefore() {
        return toString();
    }
    
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#toStringAfter()
     */
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#toStringAfter()
     */
    public String toStringAfter() {
        return toString();
    }
    
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#getValue()
     */
    /* (non-Javadoc)
     * @see nl.leverkruid.spepping.gkplinebreaking.Penalty#getValue()
     */
    public int getValue() {
        return value;
    }

}

