package com.github.isteed.coffeerl.util;
import java.awt.Point;
import java.util.ArrayList;

public class DungeonTools {
	private DungeonTools() {
		
	}
	public static ArrayList<Point> getLine(int x1, int y1, int x2, int y2) {
		ArrayList<Point> ret = new ArrayList<Point>();
		
		int w = x2 - x1;
	    int h = y2 - y1;
	    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
	    if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
	    if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
	    int longest = Math.abs(w);
	    int shortest = Math.abs(h);
	    if (!(longest>shortest)) {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
	        dx2 = 0 ;            
	    }
	    int numerator = longest >> 1 ;
	    for (int i=0;i<=longest;i++) {
	        ret.add(new Point(x1,y1));
	        numerator += shortest ;
	        if (!(numerator<longest)) {
	            numerator -= longest ;
	            x1 += dx1 ;
	            y1 += dy1 ;
	        } else {
	            x1 += dx2 ;
	            y1 += dy2 ;
	        }
	    }
		
	    return ret;
	}
	
	public static ArrayList<Point> getLine(Point c1, Point c2) {
		return getLine(c1.x, c1.y, c2.x, c2.y);
	}
	
	public static ArrayList<Point> getCircle(Point point,int radius) {
		ArrayList<Point> coords = new ArrayList<Point>();
		ArrayList<Point> ret = new ArrayList<Point>();
		int f = 1 - radius;
		int ddF_x = 1;
		int ddF_y = -2 * radius;
		int x = 0;
		int y = radius;
		int x1 = point.x;
		int y1 = point.y;
		
		
		coords.add(new Point(x1, y1 + radius));
		coords.add(new Point(x1, y1 - radius));
		coords.add(new Point(x1 + radius, y1));
		coords.add(new Point(x1 - radius, y1));

		while(x < y) {
			if(f >= 0) {
				y--;
				ddF_y += 2;
				f += ddF_y;
			}
			x++;
			ddF_x += 2;
			f += ddF_x;    
			coords.add(new Point(x1 + x, y1 + y));
			coords.add(new Point(x1 - x, y1 + y));
			coords.add(new Point(x1 + x, y1 - y));
			coords.add(new Point(x1 - x, y1 - y));
			coords.add(new Point(x1 + y, y1 + x));
			coords.add(new Point(x1 - y, y1 + x));
			coords.add(new Point(x1 + y, y1 - x));
			coords.add(new Point(x1 - y, y1 - x));
		}
		for (int i = 0; i < coords.size(); i++) {
			if (!(ret.contains(coords.get(i)))){
				//not in there, addit.
				//System.out.println("dungeon tools adding a circle coord " + coords.get(i));
				ret.add(coords.get(i));
			}
		}
		return ret;
	}
}

