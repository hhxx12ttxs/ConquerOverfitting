<<<<<<< HEAD
/*
 * @(#)Figure.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw.draw;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.event.FigureListener;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import java.io.*;
import org.jhotdraw.geom.*;

/**
 * A <em>figure</em> is a graphical element of a {@link Drawing}. A figure
 * can be only in one drawing at a time.
 * <p>
 * {@code Figure} provides the following functionality:
 * <ul>
 * <li>{@code Figure} knows its bounds and it can draw itself.</li>
 * 
 * <li>Figures can have an open ended set of attributes. An attribute is
 * identified by an {@link AttributeKey}.</li>
 * 
 * <li>A figure can have {@link org.jhotdraw.draw.connector.Connector}s that define how to locate a
 * connection point on the figure.</li>
 * 
 * <li>A figure can create a set of {@link Handle}s which can interactively
 * manipulate aspects of the figure.</li>
 *
 * <li>A figure can return a set of actions associated with a specific
 * point on the figure.</li>
 *
 * <li>A figure can be composed of other figures. If this is the case,
 * the object implementing the {@code Figure} interface usually also
 * implements the {@link CompositeFigure} interface.</li>
 *
 * <li>A figure can create a clone of itself.</li>
 * </ul>
 * 
 *
 * <hr>
 * <b>Design Patterns</b>
 * 
 * <p><em>Framework</em><br>
 * The following interfaces define the contracts of a framework for structured
 * drawing editors:<br>
 * Contract: {@link Drawing}, {@link Figure}, {@link DrawingView},
 * {@link DrawingEditor}, {@link org.jhotdraw.draw.handle.Handle} and
 * {@link org.jhotdraw.draw.tool.Tool}.
 *
 * <p><em>Composite</em><br>
 * Composite figures can be composed of other figures.<br>
 * Component: {@link Figure}; Composite: {@link CompositeFigure}.
 *
 * <p><em>Framework</em><br>
 * Two figures can be connected using a connection figure.  The location of
 * the start or end point of the connection is handled by a connector object
 * at each connected figure.<br>
 * Contract: {@link org.jhotdraw.draw.Figure},
 * {@link ConnectionFigure},
 * {@link org.jhotdraw.draw.connector.Connector}.
 *
 * <p><em>Decorator</em><br>
 * Decorated figures can be adorned with another figure.<br>
 * Component: {@link DecoratedFigure}; Decorator: {@link Figure}.
 * 
 * <p><em>Observer</em><br>
 * State changes of figures can be observed by other objects. Specifically
 * {@code CompositeFigure} observes area invalidations of its child figures. And
 * {@code DrawingView} observers area invalidations of its drawing object.<br>
 * Subject: {@link Figure}; Observer:
 * {@link org.jhotdraw.draw.event.FigureListener}; Event: {@link org.jhotdraw.draw.event.FigureEvent}; Concrete Observer:
 * {@link CompositeFigure}, {@link DrawingView}.
 *
 * <p><em>Prototype</em><br>
 * The creation tool creates new figures by cloning a prototype figure object.
 * That's the reason why {@code Figure} extends the {@code Cloneable} interface.
 * <br>
 * Prototype: {@link Figure}; Client: {@link org.jhotdraw.draw.tool.CreationTool}.
 *
 * <p><em>Strategy</em><br>
 * The location of the start and end points of a connection figure are determined
 * by {@code Connector}s which are owned by the connected figures.<br>
 * Context: {@link Figure}, {@link ConnectionFigure}; 
 * Strategy: {@link org.jhotdraw.draw.connector.Connector}.
 *
 * <p><em>Strategy</em><br>
 * {@code Locator} encapsulates a strategy for locating a point on a
 * {@code Figure}.<br>
 * Strategy: {@link org.jhotdraw.draw.locator.Locator}; Context: {@link Figure}.
 * <hr>
 * 
 * @author Werner Randelshofer
 * @version $Id: Figure.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public interface Figure extends Cloneable, Serializable {
    // PROPERTIES
    /** The name of the "connectable" property. */
    public final static String CONNECTABLE_PROPERTY="connectable";
    /** The name of the "removable" property. */
    public final static String REMOVABLE_PROPERTY="removable";
    /** The name of the "selectable" property. */
    public final static String SELECTABLE_PROPERTY="selectable";
    /** The name of the "transformable" property. */
    public final static String TRANSFORMABLE_PROPERTY="transformable";


    // DRAWING
    /**
     * Draws the figure.
     *
     * @param g The Graphics2D to draw to.
     */
    public void draw(Graphics2D g);

    /**
     * Gets the layer number of the figure.
     * The layer is used to determine the z-ordering of a figure inside of a
     * drawing. Figures with a higher layer number are drawn after figures
     * with a lower number.
     * The z-order of figures within the same layer is determined by the 
     * sequence the figures were added to a drawing. Figures added later to
     * a drawn after figures which have been added before.
     * If a figure changes its layer, it must fire a 
     * <code>FigureListener.figureChanged</code> event to
     * its figure listeners.
     */
    public int getLayer();

    /**
     * A Figure is only drawn by a CompositeFigure, if it is visible.
     * Layouter's should ignore invisible figures too.
     */
    public boolean isVisible();


    // BOUNDS
    /**
     * Sets the logical and untransformed bounds of the figure.
     * <p>
     * This is used by Tool's which create a new Figure and by Tool's which
     * connect a Figure to another Figure.
     * <p>
     * This is a basic operation which does not fire events. Use the following
     * code sequence, if you need event firing:
     * <pre>
     * figure.willChange();
     * figure.setBounds(...);
     * figure.changed();
     * </pre>
     * 
     * 
     * @param start the start point of the bounds
     * @param end the end point of the bounds
     * @see #getBounds
     */
    public void setBounds(Point2D.Double start, Point2D.Double end);

    /**
     * Returns the untransformed logical start point of the bounds.
     * 
     * 
     * 
     * @see #setBounds
     */
    public Point2D.Double getStartPoint();

    /**
     * Returns the untransformed logical end point of the bounds.
     * 
     * 
     * 
     * @see #setBounds
     */
    public Point2D.Double getEndPoint();

    /**
     * Returns the untransformed logical bounds of the figure as a Rectangle.
     * <p>
     * The bounds are used by Handle objects for adjusting the 
     * figure and for aligning the figure on a grid.
     */
    public Rectangle2D.Double getBounds();

    /**
     * Returns the drawing area of the figure as a Rectangle.
     * <p>
     * The drawing area is used to inform {@link DrawingView} about the
     * area that is needed to draw this figure.
     * <p>
     * The drawing area needs to be large enough, to take line width, line caps
     * and other decorations into account that exceed the bounds of the Figure.
     */
    public Rectangle2D.Double getDrawingArea();

    /**
     * The preferred size is used by Layouter to determine the preferred
     * size of a Figure. For most Figure's this is the same as the 
     * dimensions returned by getBounds.
     */
    public Dimension2DDouble getPreferredSize();
    
    /**
     * Checks if a point is contained by the figure.
     * <p>
     * This is used for hit testing by Tool's. 
     */
    public boolean contains(Point2D.Double p);


    // TRANSFORMING
    /**
     * Gets data which can be used to restore the transformation of the figure 
     * without loss of precision, after a transform has been applied to it.
     * 
     * @see #transform(AffineTransform)
     */
    public Object getTransformRestoreData();

    /**
     * Restores the transform of the figure to a previously stored state.
     */
    public void restoreTransformTo(Object restoreData);

    /**
     * Transforms the shape of the Figure. Transformations using double
     * precision arithmethics are inherently lossy operations. Therefore it is 
     * recommended to use getTransformRestoreData() restoreTransformTo() to 
     * provide lossless undo/redo functionality.
     * <p>
     * This is a basic operation which does not fire events. Use the following
     * code sequence, if you need event firing:
     * <pre>
     * figure.willChange();
     * figure.transform(...);
     * figure.changed();
     * </pre>
     * 
     * 
     * @param tx The transformation.
     * @see #getTransformRestoreData
     * @see #restoreTransformTo
     */
    public void transform(AffineTransform tx);

    // ATTRIBUTES
    /**
     * Sets an attribute on the figure and calls {@code attributeChanged}
     * on all registered {@code FigureListener}s if the attribute value
     * has changed.
     * <p>
     * For efficiency reasons, the drawing is not automatically repainted.
     * If you want the drawing to be repainted when the attribute is changed,
     * you can either use {@code key.set(figure, value); } or
     * <pre>
     * figure.willChange();
     * figure.set(...);
     * figure.changed();
     * </pre>
     * 
     * @see AttributeKey#set
     */
    public <T> void set(AttributeKey<T> key, @Nullable T value);

    /**
     * Gets an attribute from the Figure.
     * 
     * @see AttributeKey#get
     *
     * @return Returns the attribute value. If the Figure does not have an
     * attribute with the specified key, returns key.getDefaultValue().
     */
    @Nullable public <T> T get(AttributeKey<T> key);

    /**
     * Returns a view to all attributes of this figure.
     * By convention, an unmodifiable map is returned.
     */
    public Map<AttributeKey, Object> getAttributes();

    /**
     * Gets data which can be used to restore the attributes of the figure 
     * after a set has been applied to it.
     */
    public Object getAttributesRestoreData();

    /**
     * Restores the attributes of the figure to a previously stored state.
     */
    public void restoreAttributesTo(Object restoreData);

    // EDITING
    /**
     * Returns true, if the user may select this figure.
     * If this operation returns false, Tool's should not select this
     * figure on behalf of the user.
     * <p>
     * Please note, that even if this method returns false, the Figure
     * may become part of a selection for other reasons. For example,
     * if the Figure is part of a GroupFigure, then the Figure is 
     * indirectly part of the selection, when the user selects the
     * GroupFigure. 
     */
    public boolean isSelectable();

    /**
     * Returns true, if the user may remove this figure.
     * If this operation returns false, Tool's should not remove this
     * figure on behalf of the user.
     * <p>
     * Please note, that even if this method returns false, the Figure
     * may be removed from the Drawing for other reasons. For example,
     * if the Figure is used to display a warning message, the Figure
     * can be removed from the Drawing, when the warning message is
     * no longer relevant.
     */
    public boolean isRemovable();

    /**
     * Returns true, if the user may transform this figure.
     * If this operation returns false, Tool's should not transform this
     * figure on behalf of the user.
     * <p>
     * Please note, that even if this method returns false, the Figure
     * may be transformed for other reasons. For example, if the Figure takes 
     * part in an animation.
     * 
     * @see #transform
     */
    public boolean isTransformable();

    /**
     * Creates handles used to manipulate the figure.
     *
     * @param detailLevel The detail level of the handles. Usually this is 0 for
     * bounding box handles and 1 for point handles. The value -1 is used 
     * by the SelectAreaTracker and the HandleTracker to highlight figures, over which the mouse
     * pointer is hovering.
     * @return a Collection of handles
     * @see Handle
     */
    public Collection<Handle> createHandles(int detailLevel);

    /**
     * Returns a cursor for the specified location.
     */
    public Cursor getCursor(Point2D.Double p);

    /**
     * Returns a collection of Action's for the specified location on the figure.
     *
     * <p>The collection may contain null entries. These entries are used
     * interpreted as separators in the popup menu.
     * <p>Actions can use the property Figure.ACTION_SUBMENU to specify a 
     * submenu.
     */
    public Collection<Action> getActions(Point2D.Double p);

    /**
     * Returns a specialized tool for the specified location.
     * <p>Returns null, if no specialized tool is available.
     */
    @Nullable public Tool getTool(Point2D.Double p);

    /**
     * Returns a tooltip for the specified location on the figure.
     */
    @Nullable public String getToolTipText(Point2D.Double p);

    // CONNECTING 
    /**
     * Returns true if this Figure can be connected to a {@link ConnectionFigure}.
     */
    public boolean isConnectable();

    /**
     * Gets a connector for this figure at the given location.
     * A figure can have different connectors at different locations.
     *
     * @param p the location of the connector.
     * @param prototype The prototype used to create a connection or null if 
     * unknown. This allows for specific connectors for different 
     * connection figures.
     */
    @Nullable public Connector findConnector(Point2D.Double p, @Nullable ConnectionFigure prototype);

    /**
     * Gets a compatible connector.
     * If the provided connector is part of this figure, return the connector.
     * If the provided connector is part of another figure, return a connector
     * with the same semantics for this figure.
     * Returns null, if no compatible connector is available.
     */
    @Nullable public Connector findCompatibleConnector(Connector c, boolean isStartConnector);

    /**
     * Returns all connectors of this Figure for the specified prototype of
     * a ConnectionFigure.
     * <p>
     * This is used by connection tools and connection handles
     * to visualize the connectors when the user is about to
     * create a ConnectionFigure to this Figure.
     * 
     * @param prototype The prototype used to create a connection or null if 
     * unknown. This allows for specific connectors for different 
     * connection figures.
     */
    public Collection<Connector> getConnectors(@Nullable ConnectionFigure prototype);

    // COMPOSITE FIGURES
    /**
     * Checks whether the given figure is contained in this figure.
     * A figure includes itself.
     */
    public boolean includes(Figure figure);

    /**
     * Finds the innermost figure at the specified location.
     * <p>
     * In case of a {@code CompositeFigure}, this method descends into its
     * children and into its children's children until the innermost figure is
     * found.
     * <p>
     * This functionality is implemented using the <em>Chain of
     * Responsibility</em> design pattern. A figure which is not composed
     * of other figures returns itself if the point is contained by the figure.
     * Composed figures pass the method call down to their children.
     *
     * @param p A location on the drawing.
     * @return Returns the innermost figure at the location, or null if the
     * location is not contained in a figure.
     */
    @Nullable public Figure findFigureInside(Point2D.Double p);

    /**
     * Returns a decompositon of a figure into its parts.
     * A figure is considered as a part of itself.
     */
    public Collection<Figure> getDecomposition();

    // CLONING
    /**
     * Returns a clone of the figure, with clones of all aggregated figures,
     * such as children and decorators. The cloned figure does not clone
     * the list of FigureListeners from its original. 
     */
    public Figure clone();

    /**
     * After cloning a collection of figures, the ConnectionFigures contained
     * in this collection still connect to the original figures instead of
     * to the clones.
     * Using This operation and providing a map, which maps from the original
     * collection of figures to the new collection, connections can be remapped
     * to the new figures.
     */
    public void remap(Map<Figure, Figure> oldToNew, boolean disconnectIfNotInMap);

    // EVENT HANDLING
    /**
     * Informs a figure, that it has been added to a drawing.
     * The figure must inform all FigureListeners that it has been added.
     */
    public void addNotify(Drawing d);

    /**
     * Informs a figure, that it has been removed from a drawing.
     * The figure must inform all FigureListeners that it has been removed.
     */
    public void removeNotify(Drawing d);

    /**
     * Informs that the figure is about to change its visual representation
     * (for example, its shape, or its color).
     * <p>
     * Note: <code>willChange</code> and <code>changed</code> are typically used
     * as pairs before and after invoking one or multiple basic-methods on
     * the Figure.
     *
     * @see #changed
     */
    public void willChange();

    /**
     * Informs that a Figure changed its visual representation and needs to
     * be redrawn.
     * <p>
     * This fires a <code>FigureListener.figureChanged</code>
     * event for the current display bounds of the figure.
     * <p>
     * Note: <code>willChange</code> and <code>changed</code> are typically used
     * as pairs before and after invoking one or multiple basic-methods on
     * the Figure.
     * 
     * @see #willChange
     */
    public void changed();

    /**
     * Fires a <code>FigureListener.figureRequestRemove</code> event.
     */
    public void requestRemove();

    /**
     * Handles a drop.
     * 
     * @param p The location of the mouse event.
     * @param droppedFigures The dropped figures.
     * @param view The drawing view which is the source of the mouse event.
     * @return Returns true, if the figures should snap back to the location
     * they were dragged from.
     */
    public boolean handleDrop(Point2D.Double p, Collection<Figure> droppedFigures, DrawingView view);

    /**
     * Handles a mouse click.
     *
     * @param p The location of the mouse event.
     * @param evt The mouse event.
     * @param view The drawing view which is the source of the mouse event.
     *
     * @return Returns true, if the event was consumed.
     */
    public boolean handleMouseClick(Point2D.Double p, MouseEvent evt, DrawingView view);

    /**
     * Adds a listener for FigureEvent's.
     */
    public void addFigureListener(FigureListener l);

    /**
     * Removes a listener for FigureEvent's.
     */
    public void removeFigureListener(FigureListener l);

    /** Adds a {@code PropertyChangeListener} which can optionally be wrapped
     * into a {@code WeakPropertyChangeListener}.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);
    /** Removes a {@code PropertyChangeListener}. If the listener was added
     * wrapped into a {@code WeakPropertyChangeListener}, the
     * {@code WeakPropertyChangeListener} is removed.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
=======
package org.osgeo.proj4j;

import java.text.DecimalFormat;

/**
 * Stores a the coordinates for a position  
 * defined relative to some {@link CoordinateReferenceSystem}.
 * The coordinate is defined via X, Y, and optional Z ordinates. 
 * Provides utility methods for comparing the ordinates of two positions and
 * for creating positions from Strings/storing positions as strings.
 * <p>
 * The primary use of this class is to represent coordinate
 * values which are to be transformed
 * by a {@link CoordinateTransform}.
 */
public class ProjCoordinate 
{
  public static String DECIMAL_FORMAT_PATTERN = "0.0###############";
  public static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(DECIMAL_FORMAT_PATTERN);

	/**
	 * The X ordinate for this point. 
	 * <p>
	 * Note: This member variable
	 * can be accessed directly. In the future this direct access should
	 * be replaced with getter and setter methods. This will require 
	 * refactoring of the Proj4J code base.
	 */
	public double x;
	
	/**
	 * The Y ordinate for this point. 
	 * <p>
	 * Note: This member variable
	 * can be accessed directly. In the future this direct access should
	 * be replaced with getter and setter methods. This will require 
	 * refactoring of the Proj4J code base.
	 */
	public double y;
	
	/**
	 * The Z ordinate for this point. 
	 * If this variable has the value <tt>Double.NaN</tt>
	 * then this coordinate does not have a Z value.
	 * <p>
	 * Note: This member variable
	 * can be accessed directly. In the future this direct access should
	 * be replaced with getter and setter methods. This will require 
	 * refactoring of the Proj4J code base.
	 */
	public double z;
	
	/**
	 * Creates a ProjCoordinate with default ordinate values.
	 *
	 */
  public ProjCoordinate()
  {
    this(0.0, 0.0);
  }

	/**
	 * Creates a ProjCoordinate using the provided double parameters.
	 * The first double parameter is the x ordinate (or easting), 
	 * the second double parameter is the y ordinate (or northing), 
	 * and the third double parameter is the z ordinate (elevation or height).
	 * 
	 * Valid values should be passed for all three (3) double parameters. If
	 * you want to create a horizontal-only point without a valid Z value, use
	 * the constructor defined in this class that only accepts two (2) double
	 * parameters.
	 * 
	 * @see #ProjCoordinate(double argX, double argY)
	 */
	public ProjCoordinate(double argX, double argY, double argZ)
	{
		this.x = argX;
		this.y = argY;
		this.z = argZ;
	}
	
	/**
	 * Creates a ProjCoordinate using the provided double parameters.
	 * The first double parameter is the x ordinate (or easting), 
	 * the second double parameter is the y ordinate (or northing). 
	 * This constructor is used to create a "2D" point, so the Z ordinate
	 * is automatically set to Double.NaN. 
	 */
	public ProjCoordinate(double argX, double argY)
	{
		this.x = argX;
		this.y = argY;
		this.z = Double.NaN;
	}
	
	/** 
	 * Create a ProjCoordinate by parsing a String in the same format as returned
	 * by the toString method defined by this class.
	 * 
	 * @param argToParse the string to parse
	 */
	public ProjCoordinate(String argToParse)
	{
		// Make sure the String starts with "ProjCoordinate: ".
		boolean startsWith = argToParse.startsWith("ProjCoordinate: ");
		
		if(startsWith == false)
		{
			IllegalArgumentException toThrow = new IllegalArgumentException
			("The input string was not in the proper format.");
			
			throw toThrow;
		}
		
		// 15 characters should cut out "ProjCoordinate: ".
		String chomped = argToParse.substring(16);
		
		// Get rid of the starting and ending square brackets.
		
		String withoutFrontBracket = chomped.substring(1);
		
		// Calc the position of the last bracket.
		int length = withoutFrontBracket.length();
		int positionOfCharBeforeLast = length - 2;
		String withoutBackBracket = withoutFrontBracket.substring(0, 
				positionOfCharBeforeLast);
		
		// We should be left with just the ordinate values as strings, 
		// separated by spaces. Split them into an array of Strings.
		String[] parts = withoutBackBracket.split(" ");
		
		// Get number of elements in Array. There should be two (2) elements
		// or three (3) elements.
		// If we don't have an array with two (2) or three (3) elements,
		// then we need to throw an exception.
		if(parts.length != 2)
		{
			if(parts.length != 3)
			{
				IllegalArgumentException toThrow = new IllegalArgumentException
				("The input string was not in the proper format.");
				
				throw toThrow;
			}
		}
		
		// Convert strings to doubles.
		this.x = Double.parseDouble(parts[0]);
		this.y = Double.parseDouble(parts[0]);
		
		// You might not always have a Z ordinate. If you do, set it.
		if(parts.length == 3)
		{
			this.z = Double.parseDouble(parts[0]);
		}
	}
	
  /**
   * Sets the value of this coordinate to 
   * be equal to the given coordinate's ordinates.
   * 
   * @param p the coordinate to copy
   */
  public void setValue(ProjCoordinate p)
  {
    this.x = p.x;
    this.y = p.y;
    this.z = p.z;
  }
  
	/**
	 * Returns a boolean indicating if the X ordinate value of the 
	 * ProjCoordinate provided as an ordinate is equal to the X ordinate
	 * value of this ProjCoordinate. Because we are working with floating
	 * point numbers the ordinates are considered equal if the difference
	 * between them is less than the specified tolerance.
	 */	
	public boolean areXOrdinatesEqual(ProjCoordinate argToCompare, 
			double argTolerance)
	{
		// Subtract the x ordinate values and then see if the difference
		// between them is less than the specified tolerance. If the difference
		// is less, return true.
		double difference = argToCompare.x - this.x;
		
		if(difference > argTolerance)
		{
			return false;
		}
		
		else
		{
			return true;
		}
	}
	
	/**
	 * Returns a boolean indicating if the Y ordinate value of the 
	 * ProjCoordinate provided as an ordinate is equal to the Y ordinate
	 * value of this ProjCoordinate. Because we are working with floating
	 * point numbers the ordinates are considered equal if the difference
	 * between them is less than the specified tolerance.
	 */
	public boolean areYOrdinatesEqual(ProjCoordinate argToCompare,
			double argTolerance)
	{
		// Subtract the y ordinate values and then see if the difference
		// between them is less than the specified tolerance. If the difference
		// is less, return true.
		double difference = argToCompare.y - this.y;
		
		if(difference > argTolerance)
		{
			return false;
		}
		
		else
		{
			return true;
		}
	}
	
	/**
	 * Returns a boolean indicating if the Z ordinate value of the 
	 * ProjCoordinate provided as an ordinate is equal to the Z ordinate
	 * value of this ProjCoordinate. Because we are working with floating
	 * point numbers the ordinates are considered equal if the difference
	 * between them is less than the specified tolerance.
	 * 
	 * If both Z ordinate values are Double.NaN this method will return
	 * true. If one Z ordinate value is a valid double value and one is
	 * Double.Nan, this method will return false.
	 */
	public boolean areZOrdinatesEqual(ProjCoordinate argToCompare,
			double argTolerance)
	{
		// We have to handle Double.NaN values here, because not every
		// ProjCoordinate will have a valid Z Value.
		if(Double.isNaN(z))
		{
			if(Double.isNaN(argToCompare.z))
			{
				// Both the z ordinate values are Double.Nan. Return true.
				return true;
			}
			
			else
			{
				// We've got one z ordinate with a valid value and one with
				// a Double.NaN value. Return false.
				return false;
			}
		}
		
		// We have a valid z ordinate value in this ProjCoordinate object.
		else
		{
			if(Double.isNaN(argToCompare.z))
			{
				// We've got one z ordinate with a valid value and one with
				// a Double.NaN value. Return false.
				return false;
			}

			// If we get to this point in the method execution, we have to
			// z ordinates with valid values, and we need to do a regular 
			// comparison. This is done in the remainder of the method.
		}
		
		// Subtract the z ordinate values and then see if the difference
		// between them is less than the specified tolerance. If the difference
		// is less, return true.
		double difference = argToCompare.z - this.z;
		
		if(difference > argTolerance)
		{
			return false;
		}
		
		else
		{
			return true;
		}
	}
	
	/**
	 * Returns a string representing the ProjPoint in the format:
	 * <tt>ProjCoordinate[X Y Z]</tt>.
	 * <p>
	 * Example: 
	 * <pre>
	 *    ProjCoordinate[6241.11 5218.25 12.3]
	 * </pre>
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ProjCoordinate[");
		builder.append(this.x);
		builder.append(" ");
		builder.append(this.y);
		builder.append(" ");
		builder.append(this.z);
		builder.append("]");
		
		return builder.toString();
	}

	/**
	 * Returns a string representing the ProjPoint in the format:
	 * <tt>[X Y]</tt> 
	 * or <tt>[X, Y, Z]</tt>.
	 * Z is not displayed if it is NaN.
	 * <p>
	 * Example: 
	 * <pre>
	 * 		[6241.11, 5218.25, 12.3]
	 * </pre>
	 */
	public String toShortString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(DECIMAL_FORMAT.format(x));
		builder.append(", ");
		builder.append(DECIMAL_FORMAT.format(y));
		if (! Double.isNaN(z)) {
			builder.append(", ");
			builder.append(this.z);
		}
		builder.append("]");
		
		return builder.toString();
	}
	
	public boolean hasValidZOrdinate()
	{
		if(Double.isNaN(this.z))
		{
			return false;
		}
		
		else
		{
			return true;
		}
	}
	
	/**
	 * Indicates if this ProjCoordinate has valid X ordinate and Y ordinate
	 * values. Values are considered invalid if they are Double.NaN or 
	 * positive/negative infinity.
	 */
	public boolean hasValidXandYOrdinates()
	{
		if(Double.isNaN(x))
		{
			return false;
		}
		
		else if(Double.isInfinite(this.x) == true)
		{
			return false;
		}
		
		if(Double.isNaN(y))
		{
			return false;
		}
		
		else if(Double.isInfinite(this.y) == true)
		{
			return false;
		}
		
		else
		{
			return true;
		}
	}

	public void clearZ() {
		z = Double.NaN;
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

