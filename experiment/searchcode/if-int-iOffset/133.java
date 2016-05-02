/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package steidel.przybylska;

/**
 * Given a binary image, this filter performs binary erosion, setting all removed pixels to the given 'new' color.
 */
public class ErodeFilter {

    private int threshold = 1;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    protected int iterations = 1;

    /**
     * Set the threshold - the number of neighbouring pixels for dilation to occur.
     *
     * @param threshold the new threshold
     * @see #getThreshold
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Return the threshold - the number of neighbouring pixels for dilation to occur.
     *
     * @return the current threshold
     * @see #setThreshold
     */
    public int getThreshold() {
        return threshold;
    }

    public byte[] erode(int width, int height, byte[] inPixels) {
        byte[] outPixels = new byte[width * height];

        for (int i = 0; i < iterations; i++) {
            int index = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte pixel = inPixels[y * width + x];
                    if (!isBlack(pixel)) {
                        int neighbours = 0;

                        for (int dy = -1; dy <= 1; dy++) {
                            int iy = y + dy;
                            int ioffset;
                            if (0 <= iy && iy < height) {
                                ioffset = iy * width;
                                for (int dx = -1; dx <= 1; dx++) {
                                    int ix = x + dx;
                                    if (!(dy == 0 && dx == 0) && 0 <= ix && ix < width) {
                                        byte rgb = inPixels[ioffset + ix];
                                        if (isBlack(rgb))
                                            neighbours++;
                                    }
                                }
                            }
                        }

                        if (neighbours >= threshold) {
                            pixel = (byte) 0;
                        }
                    }
                    outPixels[index++] = pixel;
                }
            }
        }

        return outPixels;
    }

    public boolean isBlack(byte bin) {
        return bin == (byte) 0;
    }
}

