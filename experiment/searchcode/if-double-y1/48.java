/* 
 * Copyright 2011 Cologne University of Applied Sciences Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package amcgala.framework.shape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amcgala.framework.camera.Camera;
import amcgala.framework.math.Matrix;
import amcgala.framework.math.Vector3d;
import amcgala.framework.renderer.Pixel;
import amcgala.framework.renderer.Renderer;

/**
 * Ein 3d Kreis, der mithilfe des Bresenham Algorithmus gezeichnet wird.
 *
 * @author Steffen Tröster
 */
public class Circle3d extends Shape {

    private double x, y;
    private double radius;
    private Vector3d pos;
    private final double z;

    /**
     * Ein Kreis im 3d-Raum mit dem Mittelpunkt an der Position (x,y,z).
     * @param x die x-Position des Mittelpunkts
     * @param y die y-Position des Mittelpunkts
     * @param z die z-Position des Mittelpunkts
     * @param radius der Radius
     */
    public Circle3d(double x, double y, double z, double radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        pos = new Vector3d(x, y, z);
    }
    
    /**
     * Setzt den Radius des Kreises auf den übergebenen Wert.
     * @param r
     */
    public void setRadius(double r) {
    	this.radius = r;
    }
    
    /**
     * Gibt den Radius des Kreises zurück.
     * @return
     */
    public double getRadius() {
    	return this.radius;
    }
    
    /**
     * Setzt den Kreis auf die übergebene Position.
     */
    public void setPosition(double x, double y) {
    	this.x = x;
    	this.y = y;
    }
    
    /**
     * Gibt die Position in Form eines Vektors zurück.
     * @return
     */
    public Vector3d getPosition() {
    	return new Vector3d(this.x, this.y, this.z);
    }

    /**
     * 
     */
    @Override
    public void render(Matrix transformation, Camera camera, Renderer renderer) {
    	/*
    	 * Einbeziehen der Transformationsgruppen. Um Animationen zu
    	 * beruecksichtigen, die auf die einzelnen Felder zugegriffen
    	 * haben, wird der pos Vektor aktualisiert, bevor er mit
    	 * der Transformationsmatrix multipliziert wird.
    	 */
        pos = new Vector3d(x, y, z).transform(transformation);
        x = pos.x;
        y = pos.y;

        double f = 1 - radius;
        double ddF_x = 0;
        double ddF_y = -2 * radius;
        double x1 = 0;
        double y1 = radius;

        // Eckpunkte zeichnen
        renderer.putPixel(new Pixel(x, y + radius), this.color);
        renderer.putPixel(new Pixel(x, y - radius), this.color);
        renderer.putPixel(new Pixel(x + radius, y), this.color);
        renderer.putPixel(new Pixel(x - radius, y), this.color);

        while (x1 < y1) {
            if (f >= 0) {
                y1--;
                ddF_y += 2;
                f += ddF_y;
            }
            x1++;
            ddF_x += 2;
            f += ddF_x + 1;

            // Zeichne jeweiligen Randsegmente
            renderer.putPixel(new Pixel(this.x + x1, this.y + y1), this.color);
            renderer.putPixel(new Pixel(this.x - x1, this.y + y1), this.color);
            renderer.putPixel(new Pixel(this.x + x1, this.y - y1), this.color);
            renderer.putPixel(new Pixel(this.x - x1, this.y - y1), this.color);
            renderer.putPixel(new Pixel(this.x + y1, this.y + x1), this.color);
            renderer.putPixel(new Pixel(this.x - y1, this.y + x1), this.color);
            renderer.putPixel(new Pixel(this.x + y1, this.y - x1), this.color);
            renderer.putPixel(new Pixel(this.x - y1, this.y - x1), this.color);
        }

        /*
         * Ende Bresenham Algorithmus
         */
    }

    /**
     * 
     */
    @Override
    public String toString() {
        return "Circle3d{" + "x=" + x + ", y=" + y + ", radius=" + radius
               + ", pos=" + pos + '}';
    }

    private static final Logger log = LoggerFactory.getLogger(Circle3d.class);
}
