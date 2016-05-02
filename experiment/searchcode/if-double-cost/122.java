package org.kite9.diagram.position;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * This extends the idea of dimension, but allows you to associate a cost with the dimension
 * other than simply the size.
 * 
 * @author robmoffat
 *
 */
@XStreamAlias("costed")
public class CostedDimension extends Dimension2D implements Comparable<CostedDimension> {

	private static final long serialVersionUID = 5835848361276316308L;

	public static final CostedDimension ZERO = new CostedDimension(0,0,0) {

		private static final long serialVersionUID = -5465011727879693143L;

		@Override
		public void increaseX(double x) {
			super.increaseX(x);
		}

		@Override
		public void increaseY(double y) {
			super.increaseY(y);
		}

		@Override
		public void setX(double x) {
			super.setX(x);
		}

		@Override
		public void setY(double y) {
			super.setY(y);
		}

		@Override
		public void setSize(java.awt.geom.Dimension2D arg0) {
			super.setSize(arg0);
		}
		
		
		
	};
	
	public static final CostedDimension NOT_DISPLAYABLE = new CostedDimension(-1,-1,Integer.MAX_VALUE);
	
	public static final CostedDimension UNBOUNDED = new CostedDimension(Integer.MAX_VALUE,Integer.MAX_VALUE,0);
	

	public long cost;

	
	public CostedDimension() {
		super();
	}
	
	public CostedDimension(Dimension2D d) {
		super(d.getWidth(), d.getHeight());
	}

	public CostedDimension(double arg0, double arg1, long cost) {
		super(arg0, arg1);
		this.cost = cost;
	}

	/**
	 * Works out cost based on how well the new {@link CostedDimension} fits into within.
	 */
	public CostedDimension(double width, double height, Dimension2D within) {
		super(width, height);
		double extraHeight = Math.max(height - within.height, 0);
		double extraWidth = Math.max(width - within.width, 0);
		
		cost = (long)( (extraHeight * width) + (extraWidth * height) +  (extraHeight * extraWidth));
	}

	public long getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public static CostedDimension chooseBest(CostedDimension a, CostedDimension b) {
		if (a == CostedDimension.NOT_DISPLAYABLE) {
			if (b == CostedDimension.NOT_DISPLAYABLE) {
				return CostedDimension.NOT_DISPLAYABLE;
			}
			return b;
		} else {
			if (a.cost < b.cost) {
				return a;
			} else if (a.cost > b.cost) {
				return b;
			}
			
			if (a.height< b.height) {
				return a;
			} else if (a.height > b.height) {
				return b;
			}
		
			if (a.width< b.width) {
				return a;
			} else if (a.width > b.width) {
				return b;
			}
	
			return a;
		}
	}

	public int compareTo(CostedDimension o) {
		return ((Long)this.cost).compareTo(o.cost);
	}
}

