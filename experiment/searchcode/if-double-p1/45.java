package sdp.vision;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import sdp.gui.MainWindow;

/**
 * 
 * @author Thomas Wallace
 * @author Dale Myers
 * @author Rado
 * @author s0840449
 *
 */
public class Orientation {


	/**
	 *  
	 * @param image
	 * @param centroid
	 * @param robotX
	 * @param robotY
	 * @param grey
	 * @param greenCorners
	 * @param distT
	 * @param distM
	 * @return 
	 * @throws Exception
	 */
	public static double findOrient(Point centroid, ArrayList<Point> robotP, 
			ArrayList <Point> grey, Point[] greenCorners, int distT, int distM, boolean isBlue) throws Exception {
		
		//START CIRCLING CODE-----------------------------------------------------------------------
		/*
		if (robotP.size() == 0) {
            throw new NoAngleException("No T pixels");
        }

        int stdev = 0;
        // Standard deviation 
        for (int i = 0; i < robotP.size(); i++) {
            int x = (int) robotP.get(i).getX();
            int y = (int) robotP.get(i).getY();

            stdev += Math.pow(Math.sqrt(Position.sqrdEuclidDist(x, y, (int) centroid.getX(), (int) centroid.getY())), 2);
        }
        stdev  = (int) Math.sqrt(stdev / robotP.size());
        
        /*
        int k=0;
        while (xPoints.size()>k) {
        	if ((Math.abs(xPoints.get(k)-centroid.getX())>17) && (Math.abs(yPoints.get(k)-centroid.getY())>17)) {
        		xPoints.remove(k);
        		yPoints.remove(k);
        	}
        	else {
        		k=k+1;
        	}
        }
        System.out.println(xPoints.size());

        //Find the position of the front of the T.
        int frontX = 0;
        int frontY = 0;
        int frontCount = 0;
        
        //START ORIGINAL CODE
        
        for (int i = 0; i < robotP.size(); i++) {
            if (stdev > 15) {
                if (Math.abs(robotP.get(i).getX() - centroid.getX()) < stdev && Math.abs(robotP.get(i).getY() - centroid.getY()) < stdev &&
                        Position.sqrdEuclidDist((int) robotP.get(i).getX(), (int) robotP.get(i).getY(), (int) centroid.getX(), (int) centroid.getY()) > Math.pow(15, 2)) {
                    frontCount++;
                    frontX += robotP.get(i).getX();
                    frontY += robotP.get(i).getY();
                }
            } else {
                if (Position.sqrdEuclidDist((int) robotP.get(i).getX(), (int) robotP.get(i).getY(), (int) centroid.getX(), (int) centroid.getY()) > Math.pow(15, 2)) {
                    frontCount++;
                    frontX += robotP.get(i).getX();
                    frontY += robotP.get(i).getY();
                }
            }
        }

        // If no points were found, we'd better bail.
        if (frontCount == 0) {
            throw new NoAngleException("Front of T was not found");
        }
        //END ORIGINAL CODE
        
        //START ITERATION CODE
        
        /*boolean firstIter=true;
        boolean startedTooHigh=true;
        boolean done=false;
        do {
            frontX=0;
            frontY=0;
            frontCount=0;
            for (int i = 0; i < xPoints.size(); i++) {
                if (stdev > 15) {
                    if (Math.abs(xPoints.get(i) - centroid.getX()) < stdev && Math.abs(yPoints.get(i) - centroid.getY()) < stdev &&
                            Position.sqrdEuclidDist(xPoints.get(i), yPoints.get(i), centroid.getX(), centroid.getY()) > Math.pow(radius, 2)) {
                        frontCount++;
                        frontX += xPoints.get(i);
                        frontY += yPoints.get(i);
                        if (!(ts.isBlue_debug()||ts.isYellow_debug())) {image.setRGB(xPoints.get(i),yPoints.get(i),0xFF9900FF);}
                    }
                } else {
                    if (Position.sqrdEuclidDist(xPoints.get(i), yPoints.get(i), centroid.getX(), centroid.getY()) > Math.pow(radius, 2)) {
                        frontCount++;
                        frontX += xPoints.get(i);
                        frontY += yPoints.get(i);
                        if (!(ts.isBlue_debug()||ts.isYellow_debug())) {image.setRGB(xPoints.get(i),yPoints.get(i),0xFF9900FF);}
                    }
                }
            }
            if (firstIter) {
        		firstIter=false;
            	if (frontCount>0) {
            		startedTooHigh=false;
            	}
            	else {
            		startedTooHigh=true;
            	}
            }
            if (frontCount>0) {
            	if (startedTooHigh) {
            		done=true;
            	}
            	else {
            		radius=radius+1;
            	}
            }
            else {
            	if (startedTooHigh) {
            		radius=radius-1;
            		if (radius == 0) {
                        throw new NoAngleException("No points in the T are visible, even when the exclusion radius reaches zero");
                    }
            	}
            	else {
            		radius=radius-1;
            		done=true;
            	}
            }
        } while (!done);

        frontX=0;
        frontY=0;
        frontCount=0;
        for (int i = 0; i < xPoints.size(); i++) {
            if (stdev > 15) {
                if (Math.abs(xPoints.get(i) - centroid.getX()) < stdev && Math.abs(yPoints.get(i) - centroid.getY()) < stdev &&
                        Position.sqrdEuclidDist(xPoints.get(i), yPoints.get(i), centroid.getX(), centroid.getY()) > Math.pow(radius, 2)) {
                    frontCount++;
                    frontX += xPoints.get(i);
                    frontY += yPoints.get(i);
                    if (!(ts.isBlue_debug()||ts.isYellow_debug())) {image.setRGB(xPoints.get(i),yPoints.get(i),0xFF9900FF);}
                }
            } else {(int) backX, (int) backY)
                if (Position.sqrdEuclidDist(xPoints.get(i), yPoints.get(i), centroid.getX(), centroid.getY()) > Math.pow(radius, 2)) {
                    frontCount++;
                    frontX += xPoints.get(i);
                    frontY += yPoints.get(i);
                    if (!(ts.isBlue_debug()||ts.isYellow_debug())) {image.setRGB(xPoints.get(i),yPoints.get(i),0xFF9900FF);}
                }
            }
        }
        //STOP ITERATION CODE

        // Otherwise, get the frontX and Y.
        frontX /= frontCount;
        frontY /= frontCount;

        // In here, calculate the vector between meanX/frontX and
        // meanY/frontY, and then get the angle of that vector. 

        // Calculate the angle from center of the T to the front of the T
        double length = Math.sqrt(Math.pow(frontX - centroid.getX(), 2)
                + Math.pow(frontY - centroid.getY(), 2));
        double ax = (frontX - centroid.getX()) / length;
        double ay = (frontY - centroid.getY()) / length;
        double angle = Math.acos(ax);

        if (Double.isNaN(angle)) {
            throw new NoAngleException("Angle is not number");
        }

        if (frontY < centroid.getY()) {
            angle = -angle;
        }

        ArrayList<Drawable> orients = new ArrayList<Drawable>();
		Point finalPoint = new Point(frontX, frontY);
        orients.add(new DrawableLine(Color.PINK, finalPoint, centroid));*/
		//angle=angle-90;
		//angle = (float) Math.toRadians(angle);
		//END CIRCLING CODE-------------------------------------------------------------------------
		
/*		EDITED OUT FOR NOW - ROUGHLY FINDS THE ORIENTATION, USING 4 'CORNERS' OF THE 'T'
		Position finalPoint = new Position(0, 0);
		if (xPoints.size() != yPoints.size()) {
			throw new NoAngleException("");

		}

		Position[] furthest = findFurthest(centroid, xPoints, yPoints, distT,
				distM);

		
		double[][] distanceMatrix = new double[4][4];
		for (int i = 0; i < distanceMatrix.length; i++)
			for (int j = 0; j < distanceMatrix[0].length; j++) {
				distanceMatrix[i][j] = Position.sqrdEuclidDist(
						furthest[i].getX(), furthest[i].getY(),
						furthest[j].getX(), furthest[j].getY());
			}

		double distance = Double.MAX_VALUE;
		int index1 = 0;
		int index2 = 0;
		int index3 = 0;
		int index4 = 0;

		//find the two 'corners' that are closest together. These should be the tip of the 'T'
		for (int i = 0; i < distanceMatrix.length; i++)
			for (int j = 0; j < distanceMatrix[0].length; j++) {
				if (distanceMatrix[i][j] < distance
						&& distanceMatrix[i][j] != 0) {
					distance = distanceMatrix[i][j];
					index1 = i;
					index2 = j;

				}
			}

		if (index1 + index2 != 3) {
			index3 = 3 - index1;
			index4 = 3 - index2;
		} else {
			if (index1 == 0 || index2 == 0) {
				index3 = 2;
				index4 = 1;
			} else if (index1 == 1 || index2 == 1) {
				index3 = 3;
				index4 = 0;

			}
		}

		Position p1 = furthest[index1];
		Position p2 = furthest[index3];
		Position p3 = furthest[index2];
		Position p4 = furthest[index4];

		if (furthest[index1].getY() < furthest[index2].getY()) {
			if (furthest[index3].getY() < furthest[index4].getY()) {
				p2 = furthest[index3];
				p4 = furthest[index4];
			} else {
				p2 = furthest[index4];
				p4 = furthest[index3];

			}
		} else if (furthest[index1].getY() > furthest[index2].getY()) {
			if (furthest[index3].getY() > furthest[index4].getY()) {
				p2 = furthest[index3];
				p4 = furthest[index4];
			} else {
				p2 = furthest[index4];
				p4 = furthest[index3];
			}

		} else { // the case when the Ys are equal

			if (furthest[index1].getX() < furthest[index2].getX()) {
				if (furthest[index3].getX() < furthest[index4].getX()) {
					p2 = furthest[index3];
					p4 = furthest[index4];
				} else {
					p2 = furthest[index4];
					p4 = furthest[index3];

				}
			} else if (furthest[index1].getX() > furthest[index2].getX()) {
				if (furthest[index3].getX() > furthest[index4].getX()) {
					p2 = furthest[index3];
					p4 = furthest[index4];
				} else {
					p2 = furthest[index4];
					p4 = furthest[index3];

				}

			}

		}

	

		if (p1.getX() == p2.getX() || p3.getX() == p4.getX()) {
			throw new NoAngleException("");
		}
		//image.getGraphics()
		//		.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		//image.getGraphics()
		//		.drawLine(p3.getX(), p3.getY(), p4.getX(), p4.getY());
		//image.getGraphics().drawOval(centroid.getX(), centroid.getY(), 3, 3);

		double m1 = (p1.getY() - p2.getY()) / ((p1.getX() - p2.getX()) * 1.0);
		double b1 = p1.getY() - m1 * p1.getX();

		double m2 = (p3.getY() - p4.getY()) / ((p3.getX() - p4.getX()) * 1.0);
		double b2 = p3.getY() - m2 * p3.getX();

		if (m1 == m2) {
			throw new NoAngleException("");
		}
		int interX = (int) ((b2 - b1) / (m1 - m2));
		int interY = (int) (m1 * interX + b1);

		
		finalPoint.setX(interX);
		finalPoint.setY(interY);
		image.getGraphics().setColor(Color.RED);
		image.getGraphics().drawOval(interX, interY, 3, 3);
		double length = Position.sqrdEuclidDist(centroid.getX(),
				centroid.getY(), finalPoint.getX(), finalPoint.getY());
		length = Math.sqrt(length);
		
		image.getGraphics()
				.drawLine(centroid.getX(), centroid.getY(), finalPoint.getX(), finalPoint.getY());
		

		int xvector = interX - centroid.getX();
		int yvector = interY - centroid.getY();
		float angle = (float) Math.atan2(xvector, yvector);

		angle = (float) Math.toDegrees(angle);
		angle = 180 - angle;
		if (angle == 0)
			angle = (float) 0.001;
			
		
		angle = angle - 90;

		angle = (float) Math.toRadians(angle);
		*/

		//------------------------------------------------------------------------------------
		
		
		//							CUT AND PASTE STARTS HERE  - Thomas

        //Look in a cone in the opposite direction to try to find the grey circle
		/*
        ArrayList<Integer> greyXPoints = new ArrayList<Integer>();
        ArrayList<Integer> greyYPoints = new ArrayList<Integer>();
        int greyCircleTagColour=0xFFFF00FF;
        
        for (int a= -20; a < 20; a++) {
            ax = (float) Math.cos(angle+((a*Math.PI)/180));
            ay = (float) Math.sin(angle+((a*Math.PI)/180));
            for (int i = 13; i < 26; i++) {
                int greyX = centroid.getX() - (int) (ax * i);
                int greyY = centroid.getY() - (int) (ay * i);
                try {
                    Color c = new Color(image.getRGB(greyX, greyY));
                    float hsbvals[] = new float[3];
                    Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                    //if (true) {
                    if (isGrey(c, hsbvals, c.getRed()-c.getGreen(), c.getRed()-c.getBlue(), c.getGreen()-c.getBlue())){
                    	//System.out.println("c.getRed()="+c.getRed()+"; c.getGreen()="+c.getGreen()+"; c.getBlue()="+c.getBlue()+";");
                    	//System.out.println("hsbvals[0]="+hsbvals[0]+"; hsbvals[1]="+hsbvals[1]+"; hsbvals[2]="+hsbvals[2]+";");
                        greyXPoints.add(greyX);
                        greyYPoints.add(greyY);
                        //System.out.println("grey found");
                        image.setRGB(greyX,greyY,greyCircleTagColour);
                        //if (ts.isBlue_debug()||ts.isYellow_debug()) {image.setRGB(greyX,greyY,0xFF00FF00);}
                    }
                    else {
                    	//System.out.println("c.getRed()="+c.getRed()+"; c.getGreen()="+c.getGreen()+"; c.getBlue()="+c.getBlue()+";");
                    	//System.out.println("hsbvals[0]="+hsbvals[0]+"; hsbvals[1]="+hsbvals[1]+"; hsbvals[2]="+hsbvals[2]+";");
                        //if (ts.isBlue_debug()||ts.isYellow_debug()) {image.setRGB(greyX,greyY,0xFFFFFFFF);}
                    	if (!(c.getRGB()==greyCircleTagColour)) {image.setRGB(greyX,greyY,0xFF000000);}
                    }
                }
                catch (Exception e) {
                    System.out.println("("+greyX+","+greyY+") is outside the image? (Whilst checking for the grey circle)");
                    //This happens if part of the search area goes outside the image
                    //This is okay, just ignore and continue
                }
            }
        }*/
        // No grey circle found
        // The angle found is probably wrong, skip this value and return 0

        if (grey.size() < 1) {
            //image.getGraphics().drawOval((int) centroid.getX()-4, (int) centroid.getY()-4, 8, 8);
            throw new NoAngleException("No grey circle found\n");
        }

        // Calculate center of grey circle points
        int totalX = 0;
        int totalY = 0;
        for (int i = 0; i < grey.size(); i++) {
            totalX += grey.get(i).getX();
            totalY += grey.get(i).getY();
        }

        // Center of grey circle
        float backX = totalX / grey.size();
        float backY = totalY / grey.size();

        //image.getGraphics().drawOval((int) backX-4, (int) backY-4, 8, 8);
        
        //Point greyThruCentroid=new Point((int) Math.round(6*centroid.getX()-5*backX), (int) Math.round(6*centroid.getY()-5*backY));
        
        //line from grey circle should bisect green plate
        //Find the bisector of each face of the green plate, choose the one furthest from the grey circle
        Point[] bisectors = new Point[greenCorners.length];
        bisectors[0]=new Point(	(int)Math.round((greenCorners[0].getX()+greenCorners[2].getX())/2),
								(int)Math.round((greenCorners[0].getY()+greenCorners[2].getY())/2));
        bisectors[1]=new Point(	(int)Math.round((greenCorners[0].getX()+greenCorners[3].getX())/2),
								(int)Math.round((greenCorners[0].getY()+greenCorners[3].getY())/2));
        bisectors[2]=new Point(	(int)Math.round((greenCorners[1].getX()+greenCorners[2].getX())/2),
								(int)Math.round((greenCorners[1].getY()+greenCorners[2].getY())/2));
        bisectors[3]=new Point(	(int)Math.round((greenCorners[1].getX()+greenCorners[3].getX())/2),
								(int)Math.round((greenCorners[1].getY()+greenCorners[3].getY())/2));
        
        //choose the bisector furthest from the grey circle
        int furthestIndex=-1;
        double furthestDistance=-1;
        for (int i=0; (i<greenCorners.length); i++) {
        	if ((Math.pow((bisectors[i].getX()-backX),2)+Math.pow((bisectors[i].getY()-backY),2))>furthestDistance) {
        		furthestIndex=i;
        		furthestDistance=(Math.pow((bisectors[i].getX()-backX),2)+Math.pow((bisectors[i].getY()-backY),2));
        	}
        }

    	//image.getGraphics().drawOval((int)bisectors[furthestIndex].getX()-2, (int)bisectors[furthestIndex].getY()-2,4,4);
    	Point greyThruBisector=new Point((int) Math.round(bisectors[furthestIndex].getX()*6-backX*5), (int)Math.round(bisectors[furthestIndex].getY()*6-backY*5)); 
    	Point centroidThruBisector=new Point((int) Math.round(bisectors[furthestIndex].getX()*6-centroid.getX()*5), (int)Math.round(bisectors[furthestIndex].getY()*6-centroid.getY()*5)); 

    	/*g.setColor(new Color(0xFFFF0000));
        g.drawLine((int)(backX), (int)(backY), (int)greyThruBisector.getX(), (int)greyThruBisector.getY());
        g.setColor(new Color(0xFFFFFFFF));
        g.drawLine((int)(backX), (int)(backY), (int)greyThruCentroid.getX(), (int)greyThruCentroid.getY());
        g.setColor(new Color(0xFF000000));
        g.drawLine((int)(centroid.getX()), (int)(centroid.getY()), (int)centroidThruBisector.getX(), (int)centroidThruBisector.getY());*/

    	//Combine the different estimates we have of the point the robot is facing
        
    	//first purge outliers	    	
        //dist=Math.sqrt(Math.pow((point1.getX()-point2.getX()),2)+Math.pow((point1.getY()-point2.getY()),2));
        //dist2=Math.sqrt(Math.pow((point1.getX()-point3.getX()),2)+Math.pow((point1.getY()-point3.getX()),2));
        //dist2=Math.sqrt(Math.pow((point2.getX()-point3.getX()),2)+Math.pow((point2.getY()-point3.getX()),2));
        //dist4=(dist+dist2+dist3)/3;
        
        /*double k=1.4;	//Proportional distance between the suggested points in front of the robot allowed before outlying points are discarded.
        
        if ((dist/dist4)>k) {
        	if (dist2>dist3) {point1.setLocation((point2.getX()+point3.getX())/2, (point2.getY()+point3.getY())/2);} 
        	else {point2.setLocation((point1.getX()+point3.getX())/2, (point1.getY()+point3.getY())/2);}}
        
        if ((dist2/dist4)>k) {
        	if (dist>dist3) {point1.setLocation((point2.getX()+point3.getX())/2, (point2.getY()+point3.getY())/2);} 
        	else {point3.setLocation((point1.getX()+point2.getX())/2, (point1.getY()+point2.getY())/2);}}
        
        if ((dist3/dist4)>k) {
        	if (dist>dist2) {point2.setLocation((point1.getX()+point3.getX())/2, (point1.getY()+point3.getY())/2);} 
        	else {point3.setLocation((point1.getX()+point2.getX())/2, (point1.getY()+point2.getY())/2);}}
        */
        //Outliers purged!
        //image.getGraphics().drawLine((int)(bisector.getX()), (int)(bisector.getY()), (int)backX, (int)backY);
    	
        //take an average of all the different methods for calculating the point the robot is facing.
		
		
		// Calculate new angle using just the center of the T and the grey circle

        double[] angles = new double[3];
        double length;
        double ay;
        double angle;
        
        length = Math.sqrt(Math.pow((centroid.getX()-backX),2)+Math.pow((centroid.getY()-backY),2));
        if (length==0) {throw new Exception("Attempted to divide by zero with 'length'");}
        //ax = ((centroid.getX() - backX) / length);
        ay = ((centroid.getY() - backY) / length);
        angles[0] = -Math.acos(ay);
        if ((centroid.getX() - backX)<0) {angles[0]=-angles[0];}
        angles[0]=angles[0]+Math.PI/2;
        
        length = Math.sqrt(Math.pow((greyThruBisector.getX()-backX),2)+Math.pow((greyThruBisector.getY()-backY),2));
        if (length==0) {throw new Exception("Attempted to divide by zero with 'length'");}
        ay = ((greyThruBisector.getY() - backY) / length);
        angles[1] = -Math.acos(ay);
        if ((greyThruBisector.getX() - backX)<0) {angles[1]=-angles[1];}
        angles[1]=angles[1]+Math.PI/2;
        
        length = Math.sqrt(Math.pow((centroid.getX()-centroidThruBisector.getX()),2)+Math.pow((centroid.getY()-centroidThruBisector.getY()),2));
        if (length==0) {throw new Exception("Attempted to divide by zero with 'length'");}
        ay = ((centroid.getY() - centroidThruBisector.getY()) / length);
        angles[2] = -Math.acos(ay);
        if ((centroid.getX() - centroidThruBisector.getX())<0) {angles[2]=-angles[2];}
        angles[2]=angles[2]-Math.PI/2;

        int x2;
        int y2;

        ArrayList<Drawable> orients = new ArrayList<Drawable>();
    	//g.setColor(new Color(0xFFFF0000));
        x2=(int) (backX+150*Math.cos(angles[0]));
        y2=(int) (backY+150*Math.sin(angles[0]));
        //g.drawLine((int) backX, (int) backY, x2, y2);
        orients.add(new DrawableLine(Color.BLUE, new Point((int) backX, (int) backY), new Point(x2, y2)));
        
        //g.setColor(new Color(0xFF00FF00));
        x2=(int) (centroid.getX()+150*Math.cos(angles[1]));
        y2=(int) (centroid.getY()+150*Math.sin(angles[1]));
        //g.drawLine((int) backX, (int) backY, x2, y2);
        orients.add(new DrawableLine(Color.RED, new Point((int) backX, (int) backY), new Point(x2, y2)));
        
        //g.setColor(new Color(0xFF0000FF));
        x2=(int) (backX+150*Math.cos(angles[2]));
        y2=(int) (backY+150*Math.sin(angles[2]));
        //g.drawLine((int) backX, (int) backY, x2, y2);
        orients.add(new DrawableLine(Color.BLACK, new Point((int) backX, (int) backY), new Point(x2, y2)));
        
        Point averageStart = new Point((int) ((centroid.getX() + backX*2)/3), (int) ((centroid.getY() + backY*2)/3));
        //g.setColor(new Color(0xFFFFFFFF));
        Point averageEnd = new Point((int) ((centroid.getX()*2+greyThruBisector.getX())/3), (int) ((centroid.getY()*2+greyThruBisector.getY())/3));
        //g.drawLine((int) averageStart.getX(), (int) averageStart.getY(), (int) averageEnd.getX(), (int) averageEnd.getY());
        //length = Math.sqrt(Math.pow((centroid.getX()-centroidThruBisector.getX()),2)+Math.pow((centroid.getY()-centroidThruBisector.getY()),2));

		angle=0;
        if (length==0) {throw new Exception("Attempted to divide by zero with 'length'");}
        ay = ((averageStart.getY() - averageEnd.getY()) / length);
        angle = -Math.acos(ay);
        if ((averageStart.getX() - averageEnd.getX())<0) {angle=-angle;}
        angle=angle-Math.PI/2;
        
        if (angle<0) {
        	angle=angle+Math.PI*2;
        }
        angle = Position.angleTo(averageStart, averageEnd);
        
        x2=(int) (averageStart.getX()+50*Math.cos(angle));
        y2=(int) (averageStart.getY()+50*Math.sin(angle));
        //g.drawLine((int) averageStart.getX(), (int) averageStart.getY(), x2, y2);
        orients.add(new DrawableLine(Color.WHITE, new Point((int) averageStart.getX(), (int) averageStart.getY()), new Point(x2, y2)));
        
        if (isBlue) {
          //  MainWindow.addOrUpdateDrawable("blueOrients", orients);
        } else {
           // MainWindow.addOrUpdateDrawable("yellowOrients", orients);
        }

        return angle;
	}

//The old orientation code
	/**
	 * Finds the orientation of a robot, given a list of the points contained
	 * within it's T-shape (in terms of a list of x coordinates and y
	 * coordinates), the mean x and y coordinates, and the image from which it
	 * was taken.
	 * 
	 * @param xpoints
	 *            The x-coordinates of the points contained within the T-shape.
	 * @param ypoints
	 *            The y-coordinates of the points contained within the T-shape.
	 * @param meanX
	 *            The mean x-point of the T.
	 * @param meanY
	 *            The mean y-point of the T.
	 * @param image
	 *            The image from which the points were taken.
	 * @param showImage
	 *            A boolean flag - if true a line will be drawn showing the
	 *            direction of orientation found.
	 * 
	 * @return An orientation from -Pi to Pi degrees.
	 * @throws NoAngleException
	 */
	/*public float findOrientation(ArrayList<Integer> xpoints,
			ArrayList<Integer> ypoints, int meanX, int meanY,
			BufferedImage image, boolean showImage) throws NoAngleException {
		assert (xpoints.size() == ypoints.size()) : "";

		if (xpoints.size() == 0) {
			throw new NoAngleException("");
		}

		int stdev = 0;
		//Standard deviation
		for (int i = 0; i < xpoints.size(); i++) {
			int x = xpoints.get(i);
			int y = ypoints.get(i);

			stdev += Math.pow(
					Math.sqrt(Position.sqrdEuclidDist(x, y, meanX, meanY)), 2);
		}
		stdev = (int) Math.sqrt(stdev / xpoints.size());

		//Find the position of the front of the T.
		int frontX = 0;
		int frontY = 0;
		int frontCount = 0;
		for (int i = 0; i < xpoints.size(); i++) {
			//if (stdev > 15) {
				if (Math.abs(xpoints.get(i) - meanX) < stdev
						&& Math.abs(ypoints.get(i) - meanY) < stdev
						&& Position.sqrdEuclidDist(xpoints.get(i),
								ypoints.get(i), meanX, meanY) > Math.pow(15, 2)) {
					frontCount++;
					frontX += xpoints.get(i);
					frontY += ypoints.get(i);
		//		}
		/*	} else {
				if (Position.sqrdEuclidDist(xpoints.get(i), ypoints.get(i),
						meanX, meanY) > Math.pow(15, 2)) {
					frontCount++;
					frontX += xpoints.get(i);
					frontY += ypoints.get(i);
				}
			}
		}

		//If no points were found, we'd better bail.
		if (frontCount == 0) {
			throw new NoAngleException("");
		}

		//Otherwise, get the frontX and Y.
		frontX /= frontCount;
		frontY /= frontCount;

		
		 //In here, calculate the vector between meanX/frontX and meanY/frontY,
		 //and then get the angle of that vector.
		 

		// Calculate the angle from center of the T to the front of the T
		float length = (float) Math.sqrt(Math.pow(frontX - meanX, 2)
				+ Math.pow(frontY - meanY, 2));
		float ax = (frontX - meanX) / length;
		float ay = (frontY - meanY) / length;
		float angle = (float) Math.acos(ax);

		if (frontY < meanY) {
			angle = angle;
		}

		// Look in a cone in the opposite direction to try to find the grey
		// circle
		ArrayList<Integer> greyXPoints = new ArrayList<Integer>();
		ArrayList<Integer> greyYPoints = new ArrayList<Integer>();

		for (int a = -20; a < 21; a++) {
			ax = (float) Math.cos(angle + ((a * Math.PI) / 180));
			ay = (float) Math.sin(angle + ((a * Math.PI) / 180));
			for (int i = 15; i < 25; i++) {
				int greyX = meanX - (int) (ax * i);
				int greyY = meanY - (int) (ay * i);
				try {
					Color c = new Color(image.getRGB(greyX, greyY));
					float hsbvals[] = new float[3];
					Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(),
							hsbvals);
					if (ts.isYellow_debug()||ts.isBlue_debug()) {
						image.setRGB(greyX, greyY, 0xFFFFFFFF);
					}
					if (isGrey(c, hsbvals)) {
						greyXPoints.add(greyX);
						greyYPoints.add(greyY);
						if (ts.isYellow_debug()||ts.isBlue_debug()) {
							image.setRGB(greyX, greyY, 0xFF000000);
						}
					}
				} catch (Exception e) {
					// This happens if part of the search area goes outside the
					// image
					// This is okay, just ignore and continue
				}
			}
		}
		 //
		 //No grey circle found The angle found is probably wrong, skip this
		 //value and return 0
		 //

		if (greyXPoints.size() < 30) {
			throw new NoAngleException("");
		}

		//Calculate center of grey circle points
		int totalX = 0;
		int totalY = 0;
		for (int i = 0; i < greyXPoints.size(); i++) {
			totalX += greyXPoints.get(i);
			totalY += greyYPoints.get(i);
		}

		//Center of grey circle
		float backX = totalX / greyXPoints.size();
		float backY = totalY / greyXPoints.size();

		
		 //Check that the circle is surrounded by the green plate Currently
		 //checks above and below the circle
		 

		int foundGreen = 0;
		int greenSides = 0;
		//Check if green points are above the grey circle
		for (int x = (int) (backX - 2); x < (int) (backX + 3); x++) {
			for (int y = (int) (backY - 9); y < backY; y++) {
				try {
					Color c = new Color(image.getRGB(x, y));
					float hsbvals[] = new float[3];
					Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(),
							hsbvals);
					if (isGreen(c, hsbvals)) {
						foundGreen++;
						break;
					}
				} catch (Exception e) {
					// Ignore.
				}
			}
		}

		if (foundGreen >= 3) {
			greenSides++;
		}

		//Check if green points are below the grey circle
		foundGreen = 0;
		for (int x = (int) (backX - 2); x < (int) (backX + 3); x++) {
			for (int y = (int) (backY); y < backY + 10; y++) {
				try {
					Color c = new Color(image.getRGB(x, y));
					float hsbvals[] = new float[3];
					Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(),
							hsbvals);
					if (isGreen(c, hsbvals)) {
						foundGreen++;
						break;
					}
				} catch (Exception e) {
					// Ignore.
				}
			}
		}

		if (foundGreen >= 3) {
			greenSides++;
		}

		//Check if green points are left of the grey circle
		foundGreen = 0;
		for (int x = (int) (backX - 9); x < backX; x++) {
			for (int y = (int) (backY - 2); y < backY + 3; y++) {
				try {
					Color c = new Color(image.getRGB(x, y));
					float hsbvals[] = new float[3];
					Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(),
							hsbvals);
					if (isGreen(c, hsbvals)) {
						foundGreen++;
						break;
					}
				} catch (Exception e) {
					// Ignore.
				}
			}
		}

		if (foundGreen >= 3) {
			greenSides++;
		}

		// Check if green points are right of the grey circle 
		foundGreen = 0;
		for (int x = (int) (backX); x < (int) (backX + 10); x++) {
			for (int y = (int) (backY - 2); y < backY + 3; y++) {
				try {
					Color c = new Color(image.getRGB(x, y));
					float hsbvals[] = new float[3];
					Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(),
							hsbvals);
					if (isGreen(c, hsbvals)) {
						foundGreen++;
						break;
					}
				} catch (Exception e) {
					// Ignore.
				}
			}
		}

		if (foundGreen >= 3) {
			greenSides++;
		}

		if (greenSides < 3) {
			// throw new NoAngleException(
			// "Not enough green areas around the grey circle");
			throw new NoAngleException("");
		}

		
		 //At this point, the following is true: Center of the T has been found
		 //Front of the T has been found Grey circle has been found Grey circle
		 //is surrounded by green plate pixels on at least 3 sides The grey
		 //circle, center of the T and front of the T line up roughly with the
		 //same angle
		 

		
		 //Calculate new angle using just the center of the T and the grey
		 //circle
		 
		length = (float) Math.sqrt(Math.pow(meanX - backX, 2)
				+ Math.pow(meanY - backY, 2));
		ax = (meanX - backX) / length;
		ay = (meanY - backY) / length;
		angle = (float) Math.acos(ax);

		if (frontY < meanY) {
			angle = -angle;
		}
		
		 //if (showImage) { image.getGraphics().drawLine((int) backX, (int)
		 //backY, (int) (backX + ax * 70), (int) (backY + ay * 70));
		 //image.getGraphics() .drawOval((int) backX - 4, (int) backY - 4, 8,
		 //8); }
		 
		if (angle == 0) {
			return (float) 0.001;
		}

		return angle;
	}*/
}

