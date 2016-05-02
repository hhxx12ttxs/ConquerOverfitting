package general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import map.Cell;
import agents.Agent;
import exceptions.NoPathException;

public class Utils {
	
	@Deprecated
	public static List<Coordinate> bresenham(Coordinate c, Coordinate c2) {
		List<Coordinate> result = new ArrayList<Coordinate>();
		int x = c.x;
		int y = c.y;
		int x2 = c2.x;
		int y2 = c2.y;
	    int w = x2 - x;
	    int h = y2 - y;
	    int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
	    if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
	    if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
	    if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
	    int longest = Math.abs(w) ;
	    int shortest = Math.abs(h) ;
	    if (!(longest>shortest)) {
	        longest = Math.abs(h) ;
	        shortest = Math.abs(w) ;
	        if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
	        dx2 = 0 ;            
	    }
	    int numerator = longest >> 1 ;
	    for (int i=0;i<=longest;i++) {
	        result.add(new Coordinate(x, y));
	        numerator += shortest ;
	        if (!(numerator<longest)) {
	            numerator -= longest ;
	            x += dx1 ;
	            y += dy1 ;
	        } else {
	            x += dx2 ;
	            y += dy2 ;
	        }
	    }
	    return result;
	}
	
	public static Coordinate moveRandom(Agent agent, int step) {
		double move = Math.random();
		Coordinate coord = agent.coord;
		
		Coordinate to = null;
		if(move<1./8) {
			to = new Coordinate(coord.x-step, coord.y-step);
		} else if(move < 2./8) {
			to = new Coordinate(coord.x-step, coord.y);
		} else if(move < 3./8) {
			to = new Coordinate(coord.x-step, coord.y+step);
		} else if(move < 4./8) {
			to = new Coordinate(coord.x, coord.y-step);
		} else if(move < 5./8) {
			to = new Coordinate(coord.x, coord.y+step);
		} else if(move < 6./8) {
			to = new Coordinate(coord.x+step, coord.y-step);
		} else if(move < 7./8) {
			to = new Coordinate(coord.x+step, coord.y);
		} else {
			to = new Coordinate(coord.x+step, coord.y+step);
		}
		to.normalize(Agent.map);
		return to;
	}

	public static Coordinate moveRandomTo(Agent agent, double dx, double dy, int focus, int step) {
		Coordinate c = moveRandomTo(agent.coord, dx, dy, focus, step);
		c.normalize(Agent.map);
		return c;
	}
	
	
	public static Coordinate moveRandomTo(Coordinate from, double dx, double dy, int focus, int step) {
		double denum = Math.abs(dx + dy); 
		dx /= denum;
		dy /= denum;

		List<Double> dir = new ArrayList<Double>();
		dir.add((1+dy)/2);
		dir.add((1+(dx+dy)/Math.sqrt(2))/2);
		dir.add((1+dx)/2);
		dir.add((1+(dx-dy)/Math.sqrt(2))/2);
		dir.add((1-dy)/2);
		dir.add((1-(dx+dy)/Math.sqrt(2))/2);
		dir.add((1-dx)/2);
		dir.add((1+(-dx+dy)/Math.sqrt(2))/2);
		
		double sum = 0.;
		for(int i=0; i<dir.size(); i++) {
			dir.set(i, Math.pow(dir.get(i), focus)); 
			sum += dir.get(i);
		}
		
		for(int i=0; i<dir.size(); i++) {
			dir.set(i, dir.get(i)/sum);
		}
		
		
//		System.out.println(dir);
		
		double move = Math.random();
		
		Coordinate to = null;
		double chance = dir.get(0);
		if(move<chance) {
			to = new Coordinate(from.x, from.y+step);
		} else {
			chance += dir.get(1);
			if(move < chance) {
				to = new Coordinate(from.x+step, from.y+step);
			} else {
				chance += dir.get(2);
				if(move < chance) {
					to = new Coordinate(from.x+step, from.y);
				} else {
					chance += dir.get(3);
					if(move < chance) {
						to = new Coordinate(from.x+step, from.y-step);
					} else {
						chance += dir.get(4);
						if(move < chance) {
							to = new Coordinate(from.x, from.y-step);
						} else {
							chance += dir.get(5);
							if(move < chance) {
								to = new Coordinate(from.x-step, from.y-step);
							} else {
								chance += dir.get(6);
								if(move < chance) {
									to = new Coordinate(from.x-step, from.y);
								} else {
									to = new Coordinate(from.x-step, from.y+step);
								}
							}
						}
					}
				}
			}
		}
		return to;
	}
	
	private static double euclidian(Coordinate a, Coordinate b) {
		return Math.sqrt(a.getSquareDistanceFrom(b));
	}
	
	public static List<Coordinate> aStar(map.Map map, Coordinate start, Coordinate goal) throws NoPathException {
	     Set<Coordinate> closedset = new HashSet<Coordinate>();    // The set of nodes already evaluated.
	     Set<Coordinate> openset = new HashSet<Coordinate>();
	     openset.add(start); // The set of tentative nodes to be evaluated, initially containing the start node
	     Map<Coordinate, Coordinate> cameFrom = new HashMap<Coordinate, Coordinate>();//the empty map    // The map of navigated nodes.
	 
	     Map<Coordinate, Double> gScore = new HashMap<Coordinate, Double>();
	     gScore.put(start, 0.); // Cost from start along best known path.
	     
	     final Map<Coordinate, Double> fScore = new HashMap<Coordinate, Double>();
	     // Estimated total cost from start to goal through y.
	     fScore.put(start, gScore.get(start) + Utils.euclidian(start, goal));
	 
	     Coordinate current;
	     while(!openset.isEmpty()) {
	    	 
	    	 Comparator<Coordinate> fScoreComp = new Comparator<Coordinate>() { 
				@Override
				public int compare(Coordinate arg0, Coordinate arg1) {
					if(fScore.get(arg0).equals(fScore.get(arg1))) {
						return 0;
					} else if(fScore.get(arg0)<(fScore.get(arg1))) {
						return -1;
					} else {
						return 1;
					}
				}
	    	 };
	    	 
	    	 List<Coordinate> sorted = new ArrayList<Coordinate>(openset);
	    	 Collections.sort(sorted, fScoreComp);
	    	 
	    	 current = sorted.get(0); // the node in openset having the lowest f_score[] value
	    	 
	         if(current.equals(goal)) {
	        	 List<Coordinate> result = new ArrayList<Coordinate>();
	             reconstructPath(cameFrom, goal, result);
	             Collections.reverse(result);
	             return result.subList(1, result.size());
	         }
	 
	         openset.remove(current);
	         closedset.add(current);
	         
	         
	         Double tentativeGScore = Double.MAX_VALUE;
	         for(Cell neighbor : map.getSquareAround(current, 1)) {
	        	 if(closedset.contains(neighbor.coord)) {
	        		 continue;
	        	 }
	        	 tentativeGScore = gScore.get(current) + Utils.cost(map, current,neighbor.coord);
	        	 
	        	 if(gScore.get(neighbor.coord) == null) {
	        		 gScore.put(neighbor.coord, Double.MAX_VALUE);
	        	 }
	        	 if(openset.contains(neighbor.coord) || tentativeGScore <= gScore.get(neighbor.coord)) {
	        		 cameFrom.put(neighbor.coord, current);
	        		 gScore.put(neighbor.coord, tentativeGScore);
	        		 fScore.put(neighbor.coord, gScore.get(neighbor.coord) + Utils.euclidian(neighbor.coord, goal));
	        		 if(!openset.contains(neighbor.coord)) {
	        			 openset.add(neighbor.coord);
	        		 }
	        	 }
	         }
	     }
	     throw new NoPathException();
	}
	
	private static Double cost(map.Map map, Coordinate current, Coordinate coord) {
		if(!map.getCell(coord).isVisitable()) {
			return Double.MAX_VALUE;
		} else {
			return 1.;
		}
	}

	private static void reconstructPath(Map<Coordinate, Coordinate> cameFrom, Coordinate currentNode, List<Coordinate> result) {
	     if(cameFrom.containsKey(currentNode)) {
	    	 result.add(currentNode);
	         reconstructPath(cameFrom, cameFrom.get(currentNode), result);
	     } else {
	         result.add(currentNode);
	     }
	 }
	
	
}
