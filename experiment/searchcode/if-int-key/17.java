// FishDot is a Swing-based Java game written by Chi Zhang as
// an assignment from CSCI 470, Summer 2010, NIU
//
// Copyright 2010, Northern Illinois University
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or
// without modification, are permitted provided that the
// following conditions are met:
//
//   1. Redistributions of source code must retain the above
//	copyright notice, this list of conditions and the
//	following disclaimer.
//
//   2. Redistributions in binary form must reproduce the
//	above copyright notice, this list of conditions and
//	the following disclaimer in the documentation and/or
//	other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY NORTHERN ILLINOIS UNIVERSITY
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
// BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
// EVENT SHALL NORTHERN ILLINOIS UNIVERSITY OR CONTRIBUTORS
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
// OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and
// documentation are those of the authors and should not be
// interpreted as representing official policies, either
// expressed or implied, of Northern Illinois University.

import java.awt.event.*;

/**
 * This class contains the default value of all configurable
 * values.
 *
 * @author <a href="mailto:niu.tony.c.zhang@gmail.com">Tony C. Zhang</a>
 * @version 1.0
 */
public abstract class DefaultConfig {

    /**
     * Disable constructor
     */
    private DefaultConfig() {}


    ////////////////////////////////////////////////////////
    // The following constant should not be made
    // configurable, but rather be used to generate
    // other configurable values.
    ////////////////////////////////////////////////////////

    /**
     * Defines the maximum number of AIs (BadDots).
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int NUM_AI_MAX = 4;

    /**
     * Defines the minimum number of AIs (BadDots).
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int NUM_AI_MIN = 0;

    /**
     * Defines the maximum number of PowerPills (at any
     * given time).
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int NUM_POWER_MAX = 5;

    /**
     * Defines the minimum number of PowerPills (at any
     * given time).
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int NUM_POWER_MIN = 1;

    /**
     * Defines the base multiplier of calculating scores.<br />
     * The formula used in "FishDot" is:<br />&nbsp;&nbsp;&nbsp;&nbsp;
     * <code>score_per_power = (SCORE_BASE_MODIFIER + speed * </code>
     * {@link #SCORE_L2_MODIFIER SCORE_L2_MODIFIER})<br />
     * <code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>
     * <code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>
     * <code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</code>
     * <code>* (1 + num_ai * (num_ai + 1) - num_power)</code><br />
     * You can also use {@link #scorePpower(int, int, int) scorePpower()}
     * to calculate this value.<br />
     * Note that the player can get negative scores if
     * they config the game too "easy".
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int SCORE_BASE_MODIFIER = 100;

    /**
     * Defines the level 2 multiplier of calculating scores.<br />
     * The formula is described in {@link #SCORE_BASE_MODIFIER SCORE_BASE_MODIFIER}.
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int SCORE_L2_MODIFIER = 50;

    /**
     * Defines the multiplier for "slow" game speed.
     * <br />All SPEED_* are continuious, which means you
     * can do something like this:<br />
     * <code>for (int i = SPEED_SLOW; i < SPEED_FAST; ++ i) {...}</code>
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int SPEED_SLOW = -1;

    /**
     * Defines the multiplier for "normal" game speed.
     * <br />All SPEED_* are continuious, which means you
     * can do something like this:<br />
     * <code>for (int i = SPEED_SLOW; i < SPEED_FAST; ++ i) {...}</code>
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int SPEED_NORMAL = 0;

    /**
     * Defines the multiplier for "fast" game speed.
     * <br />All SPEED_* are continuious, which means you
     * can do something like this:<br />
     * <code>for (int i = SPEED_SLOW; i < SPEED_FAST; ++ i) {...}</code>
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int SPEED_FAST = 1;

    /**
     * Defines the multiplier for "very fast" game speed.
     * <br />All SPEED_* are continuious, which means you
     * can do something like this:<br />
     * <code>for (int i = SPEED_SLOW; i < SPEED_FAST; ++ i) {...}</code>
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int SPEED_VERY_FAST = 2;

    /**
     * Defines the base time between two moves of the
     * player's "FishDot".
     * <br />Actual time = base time + modifier * speed.
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int TIME_FISH_MV = 180;

    /**
     * Defines the base time between two moves of the
     * AIs' "BadDot"s (regular moving mode).
     * <br />Actual time = base time + modifier * speed.
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int TIME_AI_MV_BASE = 320;

    /**
     * Defines the base time between two moves of the
     * AIs' "BadDot"s (random moving mode).
     * <br />Actual time = base time + modifier * speed.
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int TIME_AI_MV_RANDOM = 250;

    /**
     * Defines the time modifier for player's "FishDot".
     * <br />Actual time = base time + modifier * speed.
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int TIME_FISH_MODIFIER = 40;

    /**
     * Defines the time modifier for the AIs' "BadDot"s.
     * <br />Actual time = base time + modifier * speed.
     * <br /><br /><strong>Note:</strong> This value should
     * not be made configurable, but rather be used to
     * generate other configurable value(s).
     */
    public static final int TIME_AI_MODIFIER = 70;




    ////////////////////////////////////////////////////////
    // Default values for all configurable values
    ////////////////////////////////////////////////////////

    /**
     * Determines whether BadDots (AIs) use random moving
     * strategy (50% v and 50% h on each move), or regular
     * moving strategy (odd indexed one: v-h, even indexed
     * one: h-v).<br />
     * [v: vertical moves, h: horizontal moves]
     */
    public static final boolean FLAG_AI_RANDOM_MV = false;


    /**
     * Determines whether the scoreboard should be shown
     * (automatically) after a game was ended.
     */
    public static final boolean FLAG_SHOW_SCORE = true;


    /**
     * Determines whether the sound is enabled.
     */
    public static final boolean FLAG_SOUND_ON = true;


    /**
     * Defines the key (code) to perform moving up.
     */
    public static final int KEY_UP = KeyEvent.VK_UP;

    /**
     * Defines the key (code) to perform moving down.
     */
    public static final int KEY_DOWN = KeyEvent.VK_DOWN;

    /**
     * Defines the key (code) to perform moving left.
     */
    public static final int KEY_LEFT = KeyEvent.VK_LEFT;

    /**
     * Defines the key (code) to perform moving right.
     */
    public static final int KEY_RIGHT = KeyEvent.VK_RIGHT;

    /**
     * Defines the key (code) to perform pause/resume.
     */
    public static final int KEY_PAUSE = KeyEvent.VK_SPACE;

    /**
     * Defines the key (code) to perform mute/unmute.
     */
    public static final int KEY_MUTE = KeyEvent.VK_M;


    /**
     * Defines the number of AIs (BadDots).
     */
    public static final int NUM_AI = 2;

    /**
     * Defines the number of PowerPills (at any given time).
     */
    public static final int NUM_POWER = 2;


    /**
     * Defines the speed of the game. Used as a multiplier
     * of {@link #TIME_FISH_MODIFIER TIME_FISH_MODIFIER}
     * and {@link #TIME_AI_MODIFIER TIME_AI_MODIFIER}.
     */
    public static final int SPEED = SPEED_NORMAL;


    /**
     * Defines the preparing time before a new game started.
     */
    public static final int TIME_PREP = 2000;




    ////////////////////////////////////////////////////////
    // Static Methods that help doing calculations or
    // validating configuations
    ////////////////////////////////////////////////////////


    /**
     * Determines if the given number is in the range
     * determined by the given key.
     *
     * @param num an <code>int</code> value that needed
     *	      to be validated.
     * @param key an <code>String</code> value indicates
     *	      the type of the number. Possible choices
     *	      are: "numAI", "numPower", "speed"
     * @return a <code>boolean</code> value indicates
     *	       whether the given number is in range.
     */
    public static boolean isNumInRange(int num, String key) {
	return (
	    (key == "numAI")?
	    (( (num >= NUM_AI_MIN) && (num <= NUM_AI_MAX) )?
		true : false) :
	    (key == "numPower")?
	    (( (num >= NUM_POWER_MIN) && (num <= NUM_POWER_MAX) )?
		true : false) :
	    (key == "speed")?
	    (( (num >= SPEED_SLOW) && (num <= SPEED_VERY_FAST) )?
		true : false) :
	    (false)
		);
    }


    /**
     * Determines if the given key is usable or not.
     *
     * @param key an <code>int</code> value that needed
     *	      to be validated.
     * @return a <code>boolean</code> value indicates
     *	       whether the given key is usable.
     */
    public static boolean isUsableKey(int key) {
	return (
		// a sorted list of all key code can be found
		// in "/doc/sorted.key.codes.pdf"
		// the "usable" ones defined here is highlighted
		// in that document
		((key >= 37) && (key <= 111)) ||
		((key >= 222) && (key <= 227)) ||
		(key == KeyEvent.VK_BACK_SPACE) ||
		(key == KeyEvent.VK_ENTER) ||
		(key == KeyEvent.VK_SPACE)
		);
    }


    /**
     * Calculate the score earned per PowerPill.
     *
     * @param speed an <code>int</code> value representing
     *	      the speed of the game.
     * @param numAI an <code>int</code> value representing
     *	      the number of AIs.
     * @param numPower an <code>int</code> value representing
     *	      the number of PowerPills.
     * @return an <code>int</code> value representing the
     *	       score per PowerPill.
     */
    public static int scorePpower(int speed, int numAI, int numPower) {
	return
	    (SCORE_BASE_MODIFIER + speed * SCORE_L2_MODIFIER) *
	    (1 + numAI * (numAI + 1) - numPower);

    };


    /**
     * Calculate the number of PowerPills needed to gain
     * 1 extra life.
     *
     * @param speed an <code>int</code> value representing
     *	      the speed of the game.
     * @param numAI an <code>int</code> value representing
     *	      the number of AIs.
     * @param numPower an <code>int</code> value representing
     *	      the number of PowerPills.
     * @return an <code>int</code> value representing the
     *	       transfer limit.
     */
    public static int transferLimit(int speed, int numAI, int numPower) {
	// the following formula comes from linear
	// programming analysis of:
	//     (MaxAi + 1 - DefAi) * a + DefPower * p >= 2 
	//     (MaxAi + 1 - DefAi) * a + DefPower * p <	 3 
	//     (MaxAi + 1 - MaxAi) * a + MinPower * p - MaxSpeed * s = 1 
	//     a >= 1/2
	//     p >= 1/2
	// - the first two inequalities are chosen to map (2,2) to 2;
	// - the third one is chosen to guarantee transferLimit >= 1;
	// - the last two are chosen to grant numAI and numPower
	//   at least 33.3% weight while leaving some weight to speed
	//   (since from first two we have:  1 <= a + p < 1.5 )
	// 
	// a chart lists all permutations can be found in
	// "/doc/transfer.limit.calc.pdf"
	return
	    ( (NUM_AI_MAX + 1 - numAI) * 25
	      + numPower * 37
	      - speed * 6
	    ) / 50;

    };
}
