package net.quadratum.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

//References: http://www.javaspecialists.eu/archive/Issue010.html
//            http://download.oracle.com/javase/tutorial/uiswing/layout/custom.html

public class LineLayout implements LayoutManager2 {
	public static final int HORIZONTAL = 1, VERTICAL = 0;
	public static final int FORWARD = 2, REVERSE = 0;
	
	public static final int LEFT_TO_RIGHT = HORIZONTAL + FORWARD;
	public static final int RIGHT_TO_LEFT = HORIZONTAL + REVERSE;
	public static final int TOP_TO_BOTTOM = VERTICAL + FORWARD;
	public static final int BOTTOM_TO_TOP = VERTICAL + REVERSE;
	
	private final boolean _horizontal, _forward;
	
	private java.util.List<WeightedComponent> _comps;  //Components with weights

	private Double _weightSum;  //Cached total weight for visible components.  May be null if no value has been cached
	private Dimension _min, _pref, _max;  //Cached Dimensions for minimum, preferred, and maximum size.  May be null if no value has been cached
	
	public LineLayout(int dir) {
		_comps = new ArrayList<WeightedComponent>();
		
		_weightSum = new Double(0.0);
		_min = new Dimension(0, 0);
		_pref = new Dimension(0, 0);
		_max = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		_horizontal = (dir & 1) != 0;
		_forward = (dir & 2) != 0;
	}
	
	public void addLayoutComponent(String name, Component comp) {
		addLayoutComponent(comp, new LineConstraints());
	}
	
	public void addLayoutComponent(Component comp, Object constraints) {
		if(constraints==null)
			constraints = new LineConstraints();  //If constraints are null, use default constraints
			
		if(constraints instanceof LineConstraints) {
			LineConstraints lineConstraints = (LineConstraints)constraints;
			
			if(comp!=null) {
				//Get the index mod the number of possible indices
				int index = lineConstraints.index % (_comps.size()+1);
				if(index<0)
					index += _comps.size()+1;
				
				if(lineConstraints.weight<=0)
					throw new IllegalArgumentException("Could not add layout component due to invalid weight: "+lineConstraints.weight);
				
				_comps.add(index, new WeightedComponent(comp, lineConstraints.weight));
			}
		} else
			throw new IllegalArgumentException("Could not add layout component due to invalid constraint object type: "+constraints.getClass());
	}
	
	public void removeLayoutComponent(Component comp) {
		for(Iterator<WeightedComponent> i = _comps.iterator(); i.hasNext();) {
			if(i.next().component == comp) {
				i.remove();
				return;
			}
		}
	}
	
	private double getTotalWeight() {
		if(_weightSum==null) {
			_weightSum = 0.0;
			
			for(WeightedComponent c:_comps)
				if(c.component.isVisible())
					_weightSum += c.weight;
		}
		
		return _weightSum;
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		if(_min==null) {
			double totalWeight = getTotalWeight();
			
			_min = new Dimension(0, 0);
			
			for(WeightedComponent w : _comps) {
				if(w.component.isVisible()) {
					Dimension minD;
					if(_horizontal)
						minD = new Dimension((int)(Math.ceil(w.component.getMinimumSize().width*totalWeight/w.weight)), w.component.getMinimumSize().height);
					else
						minD = new Dimension(w.component.getMinimumSize().width, (int)(Math.ceil(w.component.getMinimumSize().height*totalWeight/w.weight)));
					_min = getMaxDimension(_min, minD);
				}
			}
			
			addInsets(_min, parent.getInsets());
			
			_min = getMaxDimension(_min, parent.getMinimumSize());
		}
		
		return _min;
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		if(_pref==null) {
			double totalWeight = getTotalWeight();
			
			_pref = new Dimension(0, 0);
			
			for(WeightedComponent w : _comps) {
				if(w.component.isVisible()) {
					Dimension prefD;
					if(_horizontal)
						prefD = new Dimension((int)(Math.ceil(w.component.getPreferredSize().width*totalWeight/w.weight)), w.component.getPreferredSize().height);
					else
						prefD = new Dimension(w.component.getPreferredSize().width, (int)(Math.ceil(w.component.getPreferredSize().height*totalWeight/w.weight)));
					_pref = getMaxDimension(_pref, prefD);
				}
			}
			
			addInsets(_pref, parent.getInsets());
		}
		
		return _pref;
	}
	
	public Dimension maximumLayoutSize(Container parent) {
		if(_max==null) {
			double totalWeight = getTotalWeight();
			
			_max = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
			
			for(WeightedComponent w : _comps) {
				if(w.component.isVisible()) {
					Dimension maxD;
					if(_horizontal)
						maxD = new Dimension((int)(Math.ceil(w.component.getMaximumSize().width*totalWeight/w.weight)), w.component.getMaximumSize().height);
					else
						maxD = new Dimension(w.component.getMaximumSize().width, (int)(Math.ceil(w.component.getMaximumSize().height*totalWeight/w.weight)));
					_max = getMinDimension(_max, maxD);
				}
			}
			
			addInsets(_max, parent.getInsets());
			
			_max = getMinDimension(_max, parent.getMaximumSize());
		}
		
		return _max;
	}
	
	private static Dimension getMaxDimension(Dimension d1, Dimension d2) {
		return new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
	}
	
	private static Dimension getMinDimension(Dimension d1, Dimension d2) {
		return new Dimension(Math.min(d1.width, d2.width), Math.min(d1.height, d2.height));
	}
	
	private static void addInsets(Dimension d, Insets insets) {
		int width = d.width, height = d.height;
		width += insets.left + insets.right;
		height += insets.top + insets.bottom;
		d.setSize(width, height);
	}
	
	public float getLayoutAlignmentX(Container target) {
		if(_horizontal) {
			if(_forward)
				return 0f;
			else
				return 1f;
		} else {
			return 0.5f;
		}
	}
	public float getLayoutAlignmentY(Container target) {
		if(_horizontal)
			return 0.5f;
		else {
			if(_forward)
				return 0f;
			else
				return 1f;
		}
	}
	
	public void invalidateLayout(Container target) {
		_weightSum = null;
		_min = null;
		_pref = null;
		_max = null;
	}
	
	public void layoutContainer(Container target) {
		Insets insets = target.getInsets();
		Dimension size = target.getSize();
		int minx = insets.left, miny = insets.top, maxx = size.width-insets.right, maxy = size.height-insets.bottom;
		int width = maxx-minx, height = maxy-miny;
		width = Math.max(width, 0);
		height = Math.max(height, 0);
		
		int prev = (_horizontal ? minx : miny);
		double curWeight = 0.0;
		double totalWeight = getTotalWeight();
		
		for(Iterator<WeightedComponent> i = new Literator<WeightedComponent>(_comps, _forward); i.hasNext();) {
			WeightedComponent w = i.next();
			if(w.component.isVisible()) {
				curWeight += w.weight;
				int quran;
				if(_horizontal) {
					quran = minx + StaticMethods.round(width*curWeight/totalWeight);
					w.component.setBounds(prev, miny, quran-prev, height);
				} else {
					quran = miny + StaticMethods.round(height*curWeight/totalWeight);
					w.component.setBounds(minx, prev, width, quran-prev);
				}
				prev = quran;
			}
		}
	}
}

//A class that adapts a list iterator into a regular iterator that iterates either normally or in reverse over the list
class Literator<E> implements Iterator<E> {
	private ListIterator<E> _it;
	private boolean _forward;
	public Literator(java.util.List<E> list, boolean forward) {
		_forward = forward;
		
		if(_forward)
			_it = list.listIterator();
		else
			_it = list.listIterator(list.size());
	}
	
	public boolean hasNext() {
		if(_forward)
			return _it.hasNext();
		else
			return _it.hasPrevious();
	}
	
	public E next() {
		if(_forward)
			return _it.next();
		else
			return _it.previous();
	}
	
	public void remove() {
		_it.remove();
	}
}

//A simple class for storing a component and a positive double weight value
class WeightedComponent {
	public final double weight;
	public final Component component;
	
	public WeightedComponent(Component c, double w) {
		if(c==null)
			throw new NullPointerException();
		if(w<=0)
			throw new IllegalArgumentException("Weight must be a nonnegative value.");
		
		component = c;
		weight = w;
	}
}
