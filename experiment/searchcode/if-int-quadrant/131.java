/*
 * Copyright (c) 2013, Christopher Cox, Sebastian Funk, Jingyi Luo,
 * Leonhard Markert, Vlad Tataranu and Ranna Zhou.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package uk.ac.cam.cl.gproj.alpha.mid;

import org.junit.Test;
import uk.ac.cam.cl.gproj.alpha.interfaces.GraphicsPrinter;
import uk.ac.cam.cl.gproj.alpha.interfaces.Sheet;
import uk.ac.cam.cl.gproj.alpha.interfaces.SheetContext;

import java.awt.*;
import java.awt.geom.Line2D;

public class FingerJointTest {

    /**
     * Everything drawn in this test happens within the universe. Used for debugging purposes only so shouldn't matter
     * for correctness
     */
    static final Rectangle universe = new Rectangle(-5000,-5000, 10000, 10000);

    /**
     * Draws finger joints to join two sheets in the rectangles specified
     *
     * @param Av See JointGenerator
     * @param Ad See JointGenerator
     * @param Bv See JointGenerator
     * @param Bd See JointGenerator
     * @param overlap Amount of overlap between fingers (see FingerJointGenerator)
     * @param graphicsA Graphics2D to write to for A
     * @param graphicsB Graphics2D to write to for B
     */
    private void drawFingerJoint(Line2D Av, boolean Ad,
                                 Line2D Bv, boolean Bd,
                                 int height,
                                 int overlap,
                                 Graphics2D graphicsA, Graphics2D graphicsB) {
        // Set up 2 graphics printers to test the shapes individually without requiring laying out
        GraphicsPrinter gpA = new GraphicsPrinter();
        Sheet sheetA = gpA.getNewSheet();
        SheetContext sheetAContext = sheetA.getNewContext();

        GraphicsPrinter gpB = new GraphicsPrinter();
        Sheet sheetB = gpB.getNewSheet();
        sheetB.getNewContext();
        SheetContext sheetBContext = sheetB.getNewContext();

        // Finger join them
        FingerJointGenerator fingerJointGenerator = new FingerJointGenerator(overlap);
        fingerJointGenerator.drawJoint(
                sheetAContext, Av, Ad,
                sheetBContext, Bv, Bd,
                height
        );

        // Draw to the output
        try {
            gpA.print(graphicsA);
            gpB.print(graphicsB);
        } catch (AssertionError e) {
            // Print debug SVG
            MidTestUtilities.OutputDebugSVG(
                    new GraphicsPrinter[]{ gpA, gpB },
                    new Sheet[]{ sheetA, sheetB },
                    new SheetContext[]{ sheetAContext, sheetBContext },
                    universe,
                    "FingerJointTest.drawFingerJoint-fail-");

            // Let the exception on its way
            throw e;
        }
    }

    /**
     * Test a simple finger joint with 4 fingers along lines starting from the x axis and going counterclockwise in 45
     * degree increments. Only tests joints on the negative (false) side of the lines.
     */
    @Test
    public void simple4FingerJoint() {
        final int height = 300;
        final int overlap = 30;
        final boolean side = false;

        // Store the lines along which we join for sheet A and B alternatively
        Line2D lines[] = {
                // Positive X axis
                new Line2D.Double(0, 0, 1200, 0),
                new Line2D.Double(0, 0, 1200, 0),

                // Quadrant 1 (+x, +y)
                new Line2D.Double(0, 0, 900, 900),
                new Line2D.Double(0, 0, 900, 900),

                // Positive Y axis
                new Line2D.Double(0, 0, 0, 1200),
                new Line2D.Double(0, 0, 0, 1200),

                // Quadrant 2 (-x, +y)
                new Line2D.Double(0, 0, -900, 900),
                new Line2D.Double(0, 0, -900, 900),

                // Negative X axis
                new Line2D.Double(0, 0, -1200, 0),
                new Line2D.Double(0, 0, -1200, 0),

                // Quadrant 3 (-x, -y)
                new Line2D.Double(0, 0, -900, -900),
                new Line2D.Double(0, 0, -900, -900),

                // Negative Y axis
                new Line2D.Double(0, 0, 0, -1200),
                new Line2D.Double(0, 0, 0, -1200),

                // Quadrant 4 (x, -y)
                new Line2D.Double(0, 0, 900, -900),
                new Line2D.Double(0, 0, 900, -900)
        };

        // Construct the expecting graphics for sheet A and B alternatively
        LineExpectingGraphics2D graphics[] = {
                // Positive X axis
                new LineExpectingGraphics2D("-X: Sheet A", new int[] {
                        0, 0,
                        0, 300,
                        330, 300,
                        330, 0,
                        600, 0,
                        600, 300,
                        930, 300,
                        930, 0,
                        1200, 0
                }),
                new LineExpectingGraphics2D("-X: Sheet B", new int[] {
                        0, 0,
                        270, 0,
                        270, 300,
                        600, 300,
                        600, 0,
                        870, 0,
                        870, 300,
                        1200, 300,
                        1200, 0
                }),

                // Quadrant 1 (+x, +y)
                new LineExpectingGraphics2D("Q1: Sheet A", new int[] {
                        0, 0,
                        -212, 212,
                        34, 458,
                        246, 246,
                        450, 450,
                        238, 662,
                        484, 908,
                        696, 696,
                        900, 900
                }),
                new LineExpectingGraphics2D("Q1: Sheet B", new int[] {
                        0, 0,
                        204, 204,
                        -8, 416,
                        238, 662,
                        450, 450,
                        654, 654,
                        442, 866,
                        688, 1112,
                        900, 900
                }),

                // Positive Y axis
                new LineExpectingGraphics2D("+Y: Sheet A", new int[] {
                        0, 0,
                        -300, 0,
                        -300, 330,
                        0, 330,
                        0, 600,
                        -300, 600,
                        -300, 930,
                        0, 930,
                        0, 1200
                }),
                new LineExpectingGraphics2D("+Y: Sheet B", new int[] {
                        0, 0,
                        0, 270,
                        -300, 270,
                        -300, 600,
                        0, 600,
                        0, 870,
                        -300, 870,
                        -300, 1200,
                        0, 1200
                }),

                // Quadrant 2 (-x, +y)
                new LineExpectingGraphics2D("Q2: Sheet A", new int[] {
                        0, 0,
                        -212, -212,
                        -458, 34,
                        -246, 246,
                        -450, 450,
                        -662, 238,
                        -908, 484,
                        -696, 696,
                        -900, 900
                }),
                new LineExpectingGraphics2D("Q2: Sheet B", new int[] {
                        0, 0,
                        -204, 204,
                        -416, -8,
                        -662, 238,
                        -450, 450,
                        -654, 654,
                        -866, 442,
                        -1112, 688,
                        -900, 900
                }),

                // Negative X axis
                new LineExpectingGraphics2D("-X: Sheet A", new int[] {
                        0, 0,
                        0, -300,
                        -330, -300,
                        -330, 0,
                        -600, 0,
                        -600, -300,
                        -930, -300,
                        -930, 0,
                        -1200, 0
                }),
                new LineExpectingGraphics2D("-X: Sheet B", new int[] {
                        0, 0,
                        -270, 0,
                        -270, -300,
                        -600, -300,
                        -600, 0,
                        -870, 0,
                        -870, -300,
                        -1200, -300,
                        -1200, 0
                }),

                // Quadrant 3 (-x, -y)
                new LineExpectingGraphics2D("Q3: Sheet A", new int[] {
                        0, 0,
                        212, -212,
                        -34, -458,
                        -246, -246,
                        -450, -450,
                        -238, -662,
                        -484, -908,
                        -696, -696,
                        -900, -900
                }),
                new LineExpectingGraphics2D("Q3: Sheet B", new int[] {
                        0, 0,
                        -204, -204,
                        8, -416,
                        -238, -662,
                        -450, -450,
                        -654, -654,
                        -442, -866,
                        -688, -1112,
                        -900, -900
                }),

                // Negative Y axis
                new LineExpectingGraphics2D("-Y: Sheet A", new int[] {
                        0, 0,
                        300, 0,
                        300, -330,
                        0, -330,
                        0, -600,
                        300, -600,
                        300, -930,
                        0, -930,
                        0, -1200
                }),
                new LineExpectingGraphics2D("-Y: Sheet B", new int[] {
                        0, 0,
                        0, -270,
                        300, -270,
                        300, -600,
                        0, -600,
                        0, -870,
                        300, -870,
                        300, -1200,
                        0, -1200
                }),

                // Quadrant 4 (x, -y)
                new LineExpectingGraphics2D("Q4: Sheet A", new int[] {
                        0, 0,
                        212, 212,
                        458, -34,
                        246, -246,
                        450, -450,
                        662, -238,
                        908, -484,
                        696, -696,
                        900, -900
                }),
                new LineExpectingGraphics2D("Q4: Sheet B", new int[] {
                        0, 0,
                        204, -204,
                        416, 8,
                        662, -238,
                        450, -450,
                        654, -654,
                        866, -442,
                        1112, -688,
                        900, -900
                })
        };

        for (int i = 0; i < lines.length; i += 2) {
            drawFingerJoint(
                    lines[i], side,
                    lines[i+1], side,
                    height,
                    overlap,
                    graphics[i],
                    graphics[i+1]
            );

            // Expect all the lines to have been drawn
            graphics[i].expectDone();
            graphics[i+1].expectDone();
        }
    }

}

