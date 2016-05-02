/*
 *   casmi
 *   http://casmi.github.com/
 *   Copyright (C) 2011, Xcoo, Inc.
 *
 *  casmi is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
  
package casmi.graphics.element;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import casmi.graphics.color.Color;
import casmi.graphics.color.ColorSet;
import casmi.graphics.color.RGBColor;
import casmi.matrix.Vertex;

/**
 * Curve class.
 * Wrap JOGL and make it easy to use.
 * 
 * @author Y. Ban
 * 
 */
public class Curve extends Element implements Renderable {

    private float[] points = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    private int detail = 30;
    
    private Color startColor;
    private Color endColor;
    private Color gradationColor = new RGBColor(0.0, 0.0, 0.0);

    public enum XYZ {
        X, Y, Z
    };

    /**
     * Creates a new Curve object using coordinates for the anchor and control points.
     * 
     * @param x1,y1
     *            The coordinates for the first anchor point.
     * @param x2,y2
     *            The coordinates for the first control point.
     * @param x3,y3
     *            The coordinates for the second control point.
     * @param x4,y4
     *            The coordinates for the second ancor point.
     */
    public Curve(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        this.points[0] = x1;
        this.points[1] = y1;
        this.points[2] = 0;
        this.points[3] = x2;
        this.points[4] = y2;
        this.points[5] = 0;
        this.points[6] = x3;
        this.points[7] = y3;
        this.points[8] = 0;
        this.points[9] = x4;
        this.points[10] = y4;
        this.points[11] = 0;
    	set();
    }

    /**
     * Creates a new Curve object using coordinates for the anchor and control points.
     * 
     * @param x1,y1
     *            The coordinates for the first anchor point.
     * @param x2,y2
     *            The coordinates for the first control point.
     * @param x3,y3
     *            The coordinates for the second control point.
     * @param x4,y4
     *            The coordinates for the second ancor point.
     */
    public Curve(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        this.points[0] = (float)x1;
        this.points[1] = (float)y1;
        this.points[2] = 0;
        this.points[3] = (float)x2;
        this.points[4] = (float)y2;
        this.points[5] = 0;
        this.points[6] = (float)x3;
        this.points[7] = (float)y3;
        this.points[8] = 0;
        this.points[9] = (float)x4;
        this.points[10] = (float)y4;
        this.points[11] = 0;
    	set();
    }

    /**
     * Creates a new Curve object using coordinates for the anchor and control points.
     * 
     * @param x1,y1,z1
     *            The coordinates for the first anchor point.
     * @param x2,y2,z2
     *            The coordinates for the first control point.
     * @param x3,y3,z3
     *            The coordinates for the second control point.
     * @param x4,y4,z4
     *            The coordinates for the second ancor point.
     */
    public Curve(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        this.points[0] = x1;
        this.points[1] = y1;
        this.points[2] = 0;
        this.points[3] = x2;
        this.points[4] = y2;
        this.points[5] = 0;
        this.points[6] = x3;
        this.points[7] = y3;
        this.points[8] = 0;
        this.points[9] = x4;
        this.points[10] = y4;
        this.points[11] = 0;
    	set();
    }

    /**
     * Creates a new Curve object using coordinates for the anchor and control points.
     * 
     * @param x1,y1,z1
     *            The coordinates for the first anchor point.
     * @param x2,y2,z2
     *            The coordinates for the first control point.
     * @param x3,y3,z3
     *            The coordinates for the second control point.
     * @param x4,y4,z4
     *            The coordinates for the second ancor point.
     */
    public Curve(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        this.points[0] = (float)x1;
        this.points[1] = (float)y1;
        this.points[2] = (float)z1;
        this.points[3] = (float)x2;
        this.points[4] = (float)y2;
        this.points[5] = (float)z2;
        this.points[6] = (float)x3;
        this.points[7] = (float)y3;
        this.points[8] = (float)z3;
        this.points[9] = (float)x4;
        this.points[10] = (float)y4;
        this.points[11] = (float)z4;
    	set();
    }
    
    /**
     * Creates a new Curve object using coordinates for the anchor and control points.
     * 
     * @param v1
     *            The coordinates for the first anchor point.
     * @param v2
     *            The coordinates for the first control point.
     * @param v3
     *            The coordinates for the second control point.
     * @param v4
     *            The coordinates for the second ancor point.
     */
    public Curve(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        this.points[0]  = (float)v1.getX();
        this.points[1]  = (float)v1.getY();
        this.points[2]  = (float)v1.getZ();
        this.points[3]  = (float)v2.getX();
        this.points[4]  = (float)v2.getY();
        this.points[5]  = (float)v2.getZ();
        this.points[6]  = (float)v3.getX();
        this.points[7]  = (float)v3.getY();
        this.points[8]  = (float)v3.getZ();
        this.points[9]  = (float)v4.getX();
        this.points[10] = (float)v4.getY();
        this.points[11] = (float)v4.getZ();
    	set();
    }
    
    /**
     * Sets coordinates for the anchor and control points.
     * 
     * @param x1,y1
     *            The coordinates for the first anchor point.
     * @param x2,y2
     *            The coordinates for the first control point.
     * @param x3,y3
     *            The coordinates for the second control point.
     * @param x4,y4
     *            The coordinates for the second ancor point.
     */
    public void set(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        this.points[0] = x1;
        this.points[1] = y1;
        this.points[2] = 0;
        this.points[3] = x2;
        this.points[4] = y2;
        this.points[5] = 0;
        this.points[6] = x3;
        this.points[7] = y3;
        this.points[8] = 0;
        this.points[9] = x4;
        this.points[10] = y4;
        this.points[11] = 0;
    	set();
    }

    /**
     * Sets coordinates for the anchor and control points.
     * 
     * @param x1,y1
     *            The coordinates for the first anchor point.
     * @param x2,y2
     *            The coordinates for the first control point.
     * @param x3,y3
     *            The coordinates for the second control point.
     * @param x4,y4
     *            The coordinates for the second ancor point.
     */
    public void set(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        this.points[0] = (float)x1;
        this.points[1] = (float)y1;
        this.points[2] = 0;
        this.points[3] = (float)x2;
        this.points[4] = (float)y2;
        this.points[5] = 0;
        this.points[6] = (float)x3;
        this.points[7] = (float)y3;
        this.points[8] = 0;
        this.points[9] = (float)x4;
        this.points[10] = (float)y4;
        this.points[11] = 0;
    	set();

    }

    /**
     * Sets coordinates for the anchor and control points.
     * 
     * @param x1,y1,z1
     *            The coordinates for the first anchor point.
     * @param x2,y2,z2
     *            The coordinates for the first control point.
     * @param x3,y3,z3
     *            The coordinates for the second control point.
     * @param x4,y4,z4
     *            The coordinates for the second ancor point.
     */
    public void set(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        this.points[0] = x1;
        this.points[1] = y1;
        this.points[2] = 0;
        this.points[3] = x2;
        this.points[4] = y2;
        this.points[5] = 0;
        this.points[6] = x3;
        this.points[7] = y3;
        this.points[8] = 0;
        this.points[9] = x4;
        this.points[10] = y4;
        this.points[11] = 0;
    	set();
    }

    /**
     * Sets coordinates for the anchor and control points.
     * 
     * @param x1,y1,z1
     *            The coordinates for the first anchor point.
     * @param x2,y2,z2
     *            The coordinates for the first control point.
     * @param x3,y3,z3
     *            The coordinates for the second control point.
     * @param x4,y4,z4
     *            The coordinates for the second ancor point.
     */
    public void set(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        this.points[0] = (float)x1;
        this.points[1] = (float)y1;
        this.points[2] = (float)z1;
        this.points[3] = (float)x2;
        this.points[4] = (float)y2;
        this.points[5] = (float)z2;
        this.points[6] = (float)x3;
        this.points[7] = (float)y3;
        this.points[8] = (float)z3;
        this.points[9] = (float)x4;
        this.points[10] = (float)y4;
        this.points[11] = (float)z4;
    	set();

    }
    
    /**
     * Sets coordinates for the anchor and control points.
     * 
     * @param v1
     *            The coordinates for the first anchor point.
     * @param v2
     *            The coordinates for the first control point.
     * @param v3
     *            The coordinates for the second control point.
     * @param v4
     *            The coordinates for the second ancor point.
     */
    public void set(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        this.points[0]  = (float)v1.getX();
        this.points[1]  = (float)v1.getY();
        this.points[2]  = (float)v1.getZ();
        this.points[3]  = (float)v2.getX();
        this.points[4]  = (float)v2.getY();
        this.points[5]  = (float)v2.getZ();
        this.points[6]  = (float)v3.getX();
        this.points[7]  = (float)v3.getY();
        this.points[8]  = (float)v3.getZ();
        this.points[9]  = (float)v4.getX();
        this.points[10] = (float)v4.getY();
        this.points[11] = (float)v4.getZ();
    	set();
    }
    
    public void setNode(int number, double x, double y){
    	if(number<=0)
    		number = 0;
    	if(number>=3)
    		number = 3;
    	this.points[number*3] = (float)x;
    	this.points[number*3+1] = (float)y;
    	this.points[number*3+2] = 0;
    	set();
    }
    
    public void setNode(int number, double x, double y, double z){
    	if(number<=0)
    		number = 0;
    	if(number>=3)
    		number = 3;
    	this.points[number*3] = (float)x;
    	this.points[number*3+1] = (float)y;
    	this.points[number*3+2] = (float)z;
    	set();
    }
    
    public void setNode(int number, Vertex v) {
    	if (number <= 0) {
    		number = 0;
    	} else if (3 <= number) {
    		number = 3;
    	}
    	this.points[number*3]   = (float)v.getX();
    	this.points[number*3+1] = (float)v.getY();
    	this.points[number*3+2] = (float)v.getZ();
    	set();
    }

    @Override
    public void render(GL gl, GLU glu, int width, int height) {
    	if (this.fillColor.getAlpha() < 0.001 || this.strokeColor.getAlpha() < 0.001 || this.isDepthTest()==false) {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }

        gl.glPushMatrix();
        this.setTweenParameter(gl);
        gl.glTranslated(-this.points[0],-this.points[1], -this.points[2]);
        

        if (this.fill) {
            getSceneFillColor().setup(gl);
            gl.glBegin(GL.GL_TRIANGLE_STRIP);
            for (int i = 0; i < detail; i++) {
                if (i == 0 && isGradation() && startColor != null)
                    getSceneColor(this.startColor).setup(gl);
                if (i == detail - 1 && isGradation() && endColor != null)
                    getSceneColor(this.endColor).setup(gl);
                if (i != 0 && i != (detail - 1) && isGradation() && endColor != null && startColor != null) {
                    gradationColor = RGBColor.lerpColor(this.startColor, this.endColor, (i / (double)(detail - 1)));
                    getSceneColor(this.gradationColor).setup(gl);
                }
                gl.glVertex2d(
                    catmullRom(points[0], points[3], points[6], points[9], ((float)((i + 1) / (float)detail))),
                    catmullRom(points[1], points[4], points[7], points[10], ((float)((i + 1) / (float)detail)))
                    );
            }
            gl.glEnd();
        }
        
        if (this.stroke) {
        	getSceneStrokeColor().setup(gl);
            gl.glLineWidth(this.strokeWidth);
            gl.glBegin(GL.GL_LINE_STRIP);
            for (int i = 0; i < detail; i++) {
                if (i == 0 && isGradation() && startColor != null)
                    getSceneColor(this.startColor).setup(gl);
                if (i == detail - 1 && isGradation() && endColor != null)
                    getSceneColor(this.endColor).setup(gl);
                if (i != 0 && i != (detail - 1) && isGradation() && endColor != null && startColor != null) {
                    gradationColor = RGBColor.lerpColor(this.startColor, this.endColor, (i / (double)(detail - 1)));
                    getSceneColor(this.gradationColor).setup(gl);
                }
                gl.glVertex2d(
                    catmullRom(points[0], points[3], points[6], points[9], ((float)((i + 1) / (float)detail))),
                    catmullRom(points[1], points[4], points[7], points[10], ((float)((i + 1) / (float)detail)))
                    );
            }
            gl.glEnd();
        }
        
        gl.glPopMatrix();
        
        if (this.fillColor.getAlpha() < 0.001 || this.strokeColor.getAlpha() < 0.001 || this.isDepthTest()==false)
            gl.glEnable(GL.GL_DEPTH_TEST);
    }

    private final double catmullRom(float p0, float p1, float p2, float p3, float t) {
        double v0 = (p2 - p0) * 0.5;
        double v1 = (p2 - p0) * 0.5;
        return (2 * p1 - 2 * p2 + v0 + v1) * t * t * t +
            (-3 * p1 + 3 * p2 - 2 * v0 - v1) * t * t + v0 * t + p1;
    }

    /**
     * Evaluates the curve at point t. 
     * 
     * @param vec
     *           The coordinate to get the location of a curve at t.
     * @param t
     *           value between 0 and 1
     */
    public double curvePoint(XYZ vec, float t) {
        double tmp = 0;
        switch (vec) {
        case X:
            tmp = catmullRom(points[0], points[3], points[6], points[9], t);
            break;
        case Y:
            tmp = catmullRom(points[1], points[4], points[7], points[10], t);
            break;
        default:
            break;
        }
        return tmp;
    }

    /**
     * Returns the detail of this Curve.
     */
    public int getDetail() {
    	return detail;
    }
    
    /**
     * Set the detail of this Curve.
     * 
     * @param detail
     *             The detail of the Curve.
     */
    public void setDetail(int d) {
        detail = d;
    }
    
    private final void set() {
    	x = this.points[0];
    	y = this.points[1];
    	z = this.points[2];
    }
    
    public void setAnchorColor(int index, Color color) {
        if (index == 0) {
            if (startColor == null) {
                startColor = new RGBColor(0.0, 0.0, 0.0);
            }
            setGradation(true);
            this.startColor = color;
        } else if (index == 1) {
            if (endColor == null) {
                endColor = new RGBColor(0.0, 0.0, 0.0);
            }
            setGradation(true);
            this.endColor = color;
        }
    }
    
    public void setAnchorColor(int index, ColorSet colorSet) {
        setAnchorColor(index, new RGBColor(colorSet));
    }
}

