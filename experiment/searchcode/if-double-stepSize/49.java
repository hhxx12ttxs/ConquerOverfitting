/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.ngt.jopenmetaverse.shared.sim.rendering.mesh.primmesher;

import java.util.LinkedList;
import java.util.List;

public class AngleList
	    {
	        private float iX, iY; // intersection point

	        public List<Angle> angles;
	        public List<Coord> normals;
	        
	        private static Angle[] angles3 =
	        {
	            new Angle(0.0f, 1.0f, 0.0f),
	            new Angle(0.33333333333333333f, -0.5f, 0.86602540378443871f),
	            new Angle(0.66666666666666667f, -0.5f, -0.86602540378443837f),
	            new Angle(1.0f, 1.0f, 0.0f)
	        };

	        private static Coord[] normals3 =
	        {
	            new Coord(0.25f, 0.4330127019f, 0.0f).Normalize(),
	            new Coord(-0.5f, 0.0f, 0.0f).Normalize(),
	            new Coord(0.25f, -0.4330127019f, 0.0f).Normalize(),
	            new Coord(0.25f, 0.4330127019f, 0.0f).Normalize()
	        };

	        private static Angle[] angles4 =
	        {
	            new Angle(0.0f, 1.0f, 0.0f),
	            new Angle(0.25f, 0.0f, 1.0f),
	            new Angle(0.5f, -1.0f, 0.0f),
	            new Angle(0.75f, 0.0f, -1.0f),
	            new Angle(1.0f, 1.0f, 0.0f)
	        };

	        private static Coord[] normals4 = 
	        {
	            new Coord(0.5f, 0.5f, 0.0f).Normalize(),
	            new Coord(-0.5f, 0.5f, 0.0f).Normalize(),
	            new Coord(-0.5f, -0.5f, 0.0f).Normalize(),
	            new Coord(0.5f, -0.5f, 0.0f).Normalize(),
	            new Coord(0.5f, 0.5f, 0.0f).Normalize()
	        };

	        private static Angle[] angles24 =
	        {
	            new Angle(0.0f, 1.0f, 0.0f),
	            new Angle(0.041666666666666664f, 0.96592582628906831f, 0.25881904510252074f),
	            new Angle(0.083333333333333329f, 0.86602540378443871f, 0.5f),
	            new Angle(0.125f, 0.70710678118654757f, 0.70710678118654746f),
	            new Angle(0.16666666666666667f, 0.5f, 0.8660254037844386f),
	            new Angle(0.20833333333333331f, 0.25881904510252096f, 0.9659258262890682f),
	            new Angle(0.25f, 0.0f, 1.0f),
	            new Angle(0.29166666666666663f, -0.25881904510252063f, 0.96592582628906831f),
	            new Angle(0.33333333333333333f, -0.5f, 0.86602540378443871f),
	            new Angle(0.375f, -0.70710678118654746f, 0.70710678118654757f),
	            new Angle(0.41666666666666663f, -0.86602540378443849f, 0.5f),
	            new Angle(0.45833333333333331f, -0.9659258262890682f, 0.25881904510252102f),
	            new Angle(0.5f, -1.0f, 0.0f),
	            new Angle(0.54166666666666663f, -0.96592582628906842f, -0.25881904510252035f),
	            new Angle(0.58333333333333326f, -0.86602540378443882f, -0.5f),
	            new Angle(0.62499999999999989f, -0.70710678118654791f, -0.70710678118654713f),
	            new Angle(0.66666666666666667f, -0.5f, -0.86602540378443837f),
	            new Angle(0.70833333333333326f, -0.25881904510252152f, -0.96592582628906809f),
	            new Angle(0.75f, 0.0f, -1.0f),
	            new Angle(0.79166666666666663f, 0.2588190451025203f, -0.96592582628906842f),
	            new Angle(0.83333333333333326f, 0.5f, -0.86602540378443904f),
	            new Angle(0.875f, 0.70710678118654735f, -0.70710678118654768f),
	            new Angle(0.91666666666666663f, 0.86602540378443837f, -0.5f),
	            new Angle(0.95833333333333326f, 0.96592582628906809f, -0.25881904510252157f),
	            new Angle(1.0f, 1.0f, 0.0f)
	        };
	        
	        public AngleList() {
				super();
			}
	        
			public AngleList(float iX, float iY) {
				super();
				this.iX = iX;
				this.iY = iY;
	            angles = new LinkedList<Angle>();
	            normals = new LinkedList<Coord>();
			}

			public AngleList(AngleList angleList) 
			{
				this(angleList.iX, angleList.iY);
				for(Angle a: angleList.angles)
					angles.add(new Angle(a));
				for(Coord c: angleList.normals)
					normals.add(new Coord(c));
			}

			private Angle interpolatePoints(float newPoint, Angle p1, Angle p2)
	        {
	            float m = (newPoint - p1.angle) / (p2.angle - p1.angle);
	            return new Angle(newPoint, p1.X + m * (p2.X - p1.X), p1.Y + m * (p2.Y - p1.Y));
	        }

	        private void intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	        { // ref: http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
	            double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
	            double uaNumerator = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);

	            if (denom != 0.0)
	            {
	                double ua = uaNumerator / denom;
	                iX = (float)(x1 + ua * (x2 - x1));
	                iY = (float)(y1 + ua * (y2 - y1));
	            }
	        }

	        public void makeAngles(int sides, float startAngle, float stopAngle) throws Exception
	        {
	            angles = new LinkedList<Angle>();
	            normals = new LinkedList<Coord>();

	            double twoPi = Math.PI * 2.0;
	            float twoPiInv = 1.0f / (float)twoPi;

	            if (sides < 1)
	                throw new Exception("number of sides not greater than zero");
	            if (stopAngle <= startAngle)
	                throw new Exception("stopAngle not greater than startAngle");

	            if ((sides == 3 || sides == 4 || sides == 24))
	            {
	                startAngle *= twoPiInv;
	                stopAngle *= twoPiInv;

	                Angle[] sourceAngles;
	                if (sides == 3)
	                    sourceAngles = angles3;
	                else if (sides == 4)
	                    sourceAngles = angles4;
	                else sourceAngles = angles24;

	                int startAngleIndex = (int)(startAngle * sides);
	                int endAngleIndex = sourceAngles.length - 1;
	                if (stopAngle < 1.0f)
	                    endAngleIndex = (int)(stopAngle * sides) + 1;
	                if (endAngleIndex == startAngleIndex)
	                    endAngleIndex++;

//		            System.out.println(String.format("StartAngle %f stopAngle %f sides %d startAngleIndex %d endAngleIndex %d", 
//		            		startAngle, stopAngle, sides, startAngleIndex, endAngleIndex));
	                
	                for (int angleIndex = startAngleIndex; angleIndex < endAngleIndex + 1; angleIndex++)
	                {
	                    angles.add(new Angle(sourceAngles[angleIndex]));
	                    if (sides == 3)
	                        normals.add(new Coord(normals3[angleIndex]));
	                    else if (sides == 4)
	                        normals.add(new Coord(normals4[angleIndex]));
	                }

	                if (startAngle > 0.0f)
	                    angles.set(0, interpolatePoints(startAngle, angles.get(0), angles.get(1)));

	                if (stopAngle < 1.0f)
	                {
	                    int lastAngleIndex = angles.size() - 1;
	                    angles.set(lastAngleIndex, interpolatePoints(stopAngle, angles.get(lastAngleIndex - 1), angles.get(lastAngleIndex)));
	                }
	            }
	            else
	            {
	                double stepSize = twoPi / sides;

	                int startStep = (int)(startAngle / stepSize);
	                double angle = stepSize * startStep;
	                int step = startStep;
	                double stopAngleTest = stopAngle;
	                if (stopAngle < twoPi)
	                {
	                    stopAngleTest = stepSize * ((int)(stopAngle / stepSize) + 1);
	                    if (stopAngleTest < stopAngle)
	                        stopAngleTest += stepSize;
	                    if (stopAngleTest > twoPi)
	                        stopAngleTest = twoPi;
	                }

	                while (angle <= stopAngleTest)
	                {
	                    Angle newAngle = new Angle();
	                    newAngle.angle = (float)angle;
	                    newAngle.X = (float)Math.cos(angle);
	                    newAngle.Y = (float)Math.sin(angle);
	                    angles.add(newAngle);
	                    step += 1;
	                    angle = stepSize * step;
	                }

	                if (startAngle > angles.get(0).angle)
	                {
	                    Angle newAngle = new Angle();
	                    intersection(angles.get(0).X, angles.get(0).Y, angles.get(1).X, angles.get(1).Y, 0.0f, 0.0f, (float)Math.cos(startAngle), (float)Math.sin(startAngle));
	                    newAngle.angle = startAngle;
	                    newAngle.X = iX;
	                    newAngle.Y = iY;
	                    angles.set(0,  newAngle);
	                }

	                int index = angles.size() - 1;
	                if (stopAngle < angles.get(index).angle)
	                {
	                    Angle newAngle = new Angle();
	                    intersection(angles.get(index - 1).X, angles.get(index - 1).Y, angles.get(index).X, angles.get(index).Y, 0.0f, 0.0f, (float)Math.cos(stopAngle), (float)Math.sin(stopAngle));
	                    newAngle.angle = stopAngle;
	                    newAngle.X = iX;
	                    newAngle.Y = iY;
	                    angles.set(index, newAngle);
	                }
	            }
	        }
	    }

	    /// <summary>
	    /// generates a profile for extrusion
	    /// </summary>
	    
