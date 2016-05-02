/**
 * Copyright 2012 Emmanuel Bourg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pushingpixels.flamingo.api.svg.transcoders;

import org.apache.batik.ext.awt.MultipleGradientPaint;

/**
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public abstract class MultipleGradientPaintTranscoder<P extends MultipleGradientPaint> extends Transcoder<P> {

    protected String transcode(MultipleGradientPaint.CycleMethodEnum cycleMethod) {
        if (cycleMethod == MultipleGradientPaint.NO_CYCLE) {
            return "NO_CYCLE";
        } else if (cycleMethod == MultipleGradientPaint.REFLECT) {
            return "REFLECT";
        } else if (cycleMethod == MultipleGradientPaint.REPEAT) {
            return "REPEAT";
        } else {
            throw new IllegalArgumentException("Unknown cycle method: " + cycleMethod);
        }
    }

    protected String transcode(MultipleGradientPaint.ColorSpaceEnum colorSpace) {
        if (colorSpace == MultipleGradientPaint.SRGB) {
            return "SRGB";
        } else if (colorSpace == MultipleGradientPaint.LINEAR_RGB) {
            return "LINEAR_RGB";
        } else {
            throw new IllegalArgumentException("Unknown color space: " + colorSpace);
        }
    }

    /**
     * Normalizes the specified array such that the values are stricly increasing.
     *
     * @param fractions
     */
    protected float[] normalizeFractions(float[] fractions) {
        float[] values = new float[fractions.length];
        
        float previousFraction = -1;
        for (int i = 0; i < fractions.length; i++) {
            float fraction = fractions[i];
            if (fraction < 0f || fraction > 1f) {
                throw new IllegalArgumentException("Fraction values must be in the range 0 to 1: " + fraction);
            }
            if (i >= 1 && fraction < fractions[i - 1]) {
                throw new IllegalArgumentException("Keyframe fractions must be non-decreasing: " + fraction);
            }
            
            if (fraction == previousFraction) {
                fraction = Math.nextAfter(fraction, Float.POSITIVE_INFINITY);
            }
            
            values[i] = fraction;
            
            previousFraction = fraction;
        }
        
        return values;
    }
}

