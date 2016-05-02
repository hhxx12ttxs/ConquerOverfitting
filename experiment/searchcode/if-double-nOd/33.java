package timeline1;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
/**
 * Application of a prototype of a character/animation editor, by M@Kgdug.org 
 * @author Mohamad Bo-Hamad.
 * @version 2011-06-30
 * this source code is licensed by the author to you based on the agreement that you always include this licence and this original comment.
 * 
 * development in the Kuwait Game Development User Group (KGDUG), as a productivity tool (categorized as support)
 * 
 * this application is intended as a creativity/productivity tool,
 * or, is for creating characters and their animations,
 * specifically for an API for interactive-applications, i.e. games.
 *
 * Another motivation/goal for this app is to build a bridge between graphical artists and programmers, 
 * in terms of defining an API(or specification and concepts for the non-programmer graphic-artist) 
 * , also, a bridge in terms of, data-structures, and a GUI for the animation API, i.e. this GUI app(character/animation editor).
 *
 * to re-iterate, this app is for animation/graphical artists/designers, (2011-06-30: not really , well, not until this app is fully implemented!)
 * */
public class Main2 implements ListSelectionListener,FocusListener
{/** just get ride of the eclipse warning */
 private static final long serialVersionUID = 1L;
 /**a global reference to the main application */static Main2 main;
	
	/**a file dialog for the user to chose where to save/load a file/path and file-name*/
	JFileChooser fileChooser=new JFileChooser();
	
	/**a file dialog for the user to chose where to save/load a file/path and file-name*/
	JColorChooser colorChooser=new JColorChooser();

 /**short notation for printing to an output, a shortcut to System.out.println(String)*/
 void p(String message){System.out.println(message);}

 /**short notation for adding to a JComponent(or any sub-class) a border with a title*/
 JComponent brdrTtl(JComponent c,String title)
 {c.setBorder(new javax.swing.border.TitledBorder(title));return c;}

// void alert(String message){JOptionPane.showMessageDialog(panelsFrame, message);}

 /**short notation for JOptionPane.showInputDialog, in the fashion of javascript*/
 String prompt(String message,String def){return JOptionPane.showInputDialog(panelsFrame, message,def);}

 /**a utility method that creates a JButton, giving it the parameter txt, and adding an actionListener*/
 JButton btn(String txt,ActionListener al){JButton b=new JButton(txt);b.addActionListener(al);return b;}

////////////////////////////////////////////////////////////////
/**Main Entity/class that represents the highlight of this application(timeline1.Main2), The Character, in an animation*/
 class Char
 {	/**name of the the animation-character, will be used in the future as part of an API, for looking-up the character*/
	String name;

	/**a list/array of the states/animation-sequences of the animation-character*/
	List<State>states=new ArrayList<State>();

	/**constructor , with a name parameter*/
	public Char(String name){this.name=name;}

	/**the setter of the Name property*/
	public void setName(String p){name=p;}

	/**the getter of the Name property*/
	public String getName(){return name;}

	/**returns the getter of the Name property*/
	public String toString(){return name;}

	/**create a new instance of a State reference ,which the instance of the State is an inner object of this Char-object*/
	State newState(String name){State s=new State(name);states.add(s);return s;}

	/**State of the character, which is also an animation-sequence, e.g. the "run" animatio sequence, or, "ideal" :the animation-sequence when the player/ or character is waiting or standing still , or "walk left", or "jumping",,,ect.*/
	class State
	{	/**name of the state-animation-sequence, which will be used in future APIs to lookup the state*/
		String name;

		/**array list of the inner objects sequences frames*/
		List<SqFrame>sqframes=new ArrayList<SqFrame>();

		/**constructor with a name parameter*/
		public State(String name){this.name=name;}

		/**the setter of the Name property*/
		public void setName(String p){name=p;}

		/**the getter of the Name property*/
		public String getName(){return name;}

		/**returns the return value of the getter of the name property. well, this is to satisfy both interfaces(sorta) 1-the getter/setter, and 2-override the java.lang.Object.toString()*/
		@Override public String toString(){return name;}

		/**returns the enclosing Char object*/
		Char getChar(){return Char.this;}

		/**create a new instance of an inner object sequence Frame*/
		SqFrame newSqFrame(){return newSqFrame("-");}
		
		/**create a new instance of an inner object sequence Frame
		 * @param name an unimplemented parameter,i.e. not used until now(2011-06-30) */
		SqFrame newSqFrame(String name){SqFrame s=new SqFrame();sqframes.add(s);return s;}

		/**Animation-Sequence-Frame, an episode in the chain of display/what-is-viewed, or, a moment of the moving graphics*/
		class SqFrame
		{/**an arrayList of the inner shape-objects*/List<Shape>shapes=new ArrayList<Shape>();

			/**the time-duration of this sequence-frame when playing the animation, in other words -actually- how long to wait until displaying the next sequence-frame */
			long duration;

			/**get a reference of the enclosing state object*/
			State getState(){return State.this;}

			/**the order of this sequence frame in the enclosing state object array list*/
			int getIndex(){return sqframes.indexOf(this);}

			/**return as a string the getIndex number*/
			public String toString(){return String.valueOf(getIndex());}

			/**return as a string the duration property*/
			public String dura(){return String.valueOf(duration);}

			/**a getter of the duration property*/
			public long getDuration(){return duration;}

			/**setter of the duration property*/
			public void setDuration(long p){duration=p;}

			/**an entry point for drawing all the shape-objects in this frame*/
			public void paint(Graphics g)
			{for(Shape r:shapes)try{r.paint(g);}catch(Exception ex){ex.printStackTrace();}}

			/**create a new instance of an inner shape-object sub-class, selecting the class based on the type parameter*/
			Shape newShape(String type,String name)
			{Shape r="Img".equals(type)?new Img()
				:"Path".equals(type)?new Path()
				:"Oval".equals(type)?new Oval()
				:new Shape();shapes.add(r);return r;}

			/**create a new inner instance of a rectangle*/
			Shape newRect(String name){Shape r=new Shape();shapes.add(r);return r;}

			/**create a new inner instance of an oval*/
			Shape newOval(){Shape r=new Oval();shapes.add(r);return r;}

			/**create a new inner instance of a rectangle*/
			Shape newRect(){Shape r=new Shape();shapes.add(r);return r;}

			/**create a new inner instance of a Path(User-defined shape, lines and curves)*/
			Shape newPath(){Shape r=new Path();shapes.add(r);return r;}

			/**create a new inner instance of a image*/
			Shape newImg(){Shape r=new Img();shapes.add(r);return r;}

			/**a base-class of a drawable object (shape), this implementation (at this level in the inheritance-hierarchy) would draw a rectangle*/
			class Shape {
				/** the coordinates and dimensions(almost bounding-box) of this shape*/
				int x=10,y=10,w=10,h=10;

				/**optional color, for drawing*/
				Color c;

				/**returns a reference to the enclosing sequence-frame, in other words, the frame which this shape belongs to.*/
				SqFrame getSqFrame(){return SqFrame.this;}

				/**the order of this shape in the list of shapes in the enclosing frame*/
				int getIndex(){return shapes.indexOf(this);}

				/**returns as a string the index of this shape.*/
				public String toString(){return String.valueOf(getIndex());}

				/**entry point for drawing this shape, sub-classes should over-ride this method to draw its custom shape, this implementation draws a rectangle based-on the (x,y,w,h instance-variables), where x,y are the center*/
				public void paint(Graphics g)
				{if(c!=null)g.setColor(c);
				 g.drawRect(x-w/2, y-h/2, w, h);}

			}//class Shape

			/**a Shape which represents/draws an Oval, circular shape which has two radius's ,x-radius and y-radius, the inherited (x,y,w,h) is the position/location and the two-radius's(w,h) or dimensions. in the case when the x-radius(w) equals the y-radius(h) then the shape becomes a circle*/
			class Oval extends Shape{
				/**entry point for drawing this shape, this implementation draws an oval based-on the (inherited x,y,w,h instance-variables), where x,y are the center*/
				public void paint(Graphics g)
				{if(c!=null)g.setColor(c);
				 g.drawOval(x-w/2, y-h/2, w, h);}}

			/**a Shape which represents/draws an Image, the inherited (x,y,w,h) are the position/location and dimensions, where x,y are the center.*/
			class Img extends Shape
			{	/**reference of the actual image data to be drawn*/
				BufferedImage bi;

				/**this is only for the serializer, XmlS.save*/
				File file;

				/**entry point for drawing this shape, this implementation draws an image(bi instance-variable) based-on the (inherited x,y,w,h instance-variables), where x,y are the center*/
				public void paint(Graphics g)
				{if(bi!=null)g.drawImage(bi, x-w/2, y-h/2, w, h,null);}

				/***/
				void setImg(File f)
				{try{bi=javax.imageio.ImageIO.read(file=f);
					w=bi.getWidth();h=bi.getHeight();}
					catch(Exception ex){ex.printStackTrace();}}
			}//class Img

		/**a Shape which represents/draws a collection of user-defined vertices, not just polygon or straight lines, but also beizer-curves, still(2011-06-30) not implemeted, in terms of the required instance variables to store the vertices nor in terms of the implementation of the paint method that traverses the vertices, and neither (un-implemented) the options(buttons and radio-buttons) or tools(buttons and radio-buttons and states-of-the-tool-classes) in the ShapeProps class .*/
		class Path extends Shape 
		{AlphaComposite alpha;
		 	 Paint paint;
		 	 AffineTransform trans;
		 	 Stroke stroke;
			 /**static final int WIND_EVEN_ODD
					The winding rule constant for specifying an even-odd rule for determining the interior of a path. The even-odd rule specifies that a point lies inside the path if a ray drawn in any direction from that point to infinity is crossed by path segments an odd number of times.

				static final int WIND_NON_ZERO
					The winding rule constant for specifying a non-zero rule for determining the interior of a path. The non-zero rule specifies that a point lies inside the path if a ray drawn in any direction from that point to infinity is crossed by path segments a different number of times in the counter-clockwise direction than the clockwise direction.
			*/int WindingRule=0;
			Rectangle2D bounds=new Rectangle2D.Float();
			void computeBounds(){Vertex n=head;bounds.setRect(0,0, 0,0);while(n!=null)
			{if(n.a!=null)for(int i=0;i<n.a.length;i+=2)bounds.add(n.a[i], n.a[i+1]);n=n.next;}}

			Vertex head,tail;

			class Vertex
			{Vertex prev,next;boolean relative;

			Vertex link(){unlnk();if(tail==null)head=this;else(prev=tail).next=this;next=null;tail=this;return this;}
			Vertex unlnk(){if(head==this)head=next;if(tail==this)tail=prev;if(prev!=null)prev.next=next;if(next!=null)next.prev=prev;next=prev=null;return this;}
			/**link p after this nod chain*/Vertex link(Vertex p){
				if((p.next=next)!=null)
					next.prev=p;
				p.prev=this;
				next=p;
				if(this==tail)tail=p;
				return p;}

			/**link this before p , in p chain*/Vertex linkBefore(Vertex p)
			{unlnk();if((prev=p.prev)!=null)
					prev.next=this;
				next=p;
				p.prev=this;
				if(head==p)head=this;
				return this;}

		 	/**link this after p , in p chain*/Vertex linkAfter(Vertex p)
		 	{unlnk();if((next=p.next)!=null)
					next.prev=this;
				prev=p;
				p.next=this;
				if(tail==p)tail=this;
				return this;}

				/**static final int SEG_MOVETO
					The segment type constant for a point that specifies the starting location for a new subpath.

				static final int SEG_LINETO
					The segment type constant for a point that specifies the end point of a line to be drawn from the most recently specified point.

				static final int SEG_QUADTO
					The segment type constant for the pair of points that specify a quadratic parametric curve to be drawn 
					from the most recently specified point. The curve is interpolated by solving the parametric control equation
					in the range (t=[0..1]) using the most recently specified (current) point (CP), 
					the first control point (P1), and the final interpolated control point (P2). 
					The parametric control equation for this curve is:
						P(t) = B(2,0)*CP + B(2,1)*P1 + B(2,2)*P2
						0 <= t <= 1

						B(n,m) = mth coefficient of nth degree Bernstein polynomial
						= C(n,m) * t^(m) * (1 - t)^(n-m)
					C(n,m) = Combinations of n things, taken m at a time
               				= n! / (m! * (n-m)!)
 
			static final int SEG_CUBICTO
				The segment type constant for the set of 3 points that specify a cubic parametric curve to be drawn from the most 
				recently specified point. The curve is interpolated by solving the parametric control equation in the range (t=[0..1]) 
				using the most recently specified (current) point (CP), the first control point (P1), the second control point (P2), 
				and the final interpolated control point (P3). The parametric control equation for this curve is:
				          P(t) = B(3,0)*CP + B(3,1)*P1 + B(3,2)*P2 + B(3,3)*P3
				          0 <= t <= 1
				
				        B(n,m) = mth coefficient of nth degree Bernstein polynomial
				               = C(n,m) * t^(m) * (1 - t)^(n-m)
				        C(n,m) = Combinations of n things, taken m at a time
				               = n! / (m! * (n-m)!)
				 
				This form of curve is commonly known as a B?zier curve.

			static final int SEG_CLOSE
				The segment type constant that specifies that the preceding subpath should be closed by appending a line segment back to the point corresponding to the most recent SEG_MOVETO.*/
			int seg;

			float[]a;
			Vertex(int segment,boolean relative,float...p)
			{seg=segment;this.relative=relative;
				if(p.length>0)System.arraycopy
					(p,0,a=new float[p.length], 0, p.length);
				link();computeBounds();}

			Vertex(int segment,float[]p,boolean relative)
			{seg=segment;this.relative=relative;
				if(p!=null&&p.length>0)System.arraycopy
					(p,0,a=new float[p.length], 0, p.length);
				link();computeBounds();}

			void set(float x,float y,int i){bounds.add(a[i]=x, a[i+1]=y);}
			}//class Nod
			Vertex newVertex(int segment,float[]p,boolean relative){return new Vertex(segment,p,relative);}
			Vertex newVertex(int segment,boolean relative,float...p){return new Vertex(segment,p,relative);}

			class Geom implements java.awt.Shape{
			class PI implements PathIterator
			{Vertex n=head;float[]a={0,0};
				@Override public int getWindingRule() {return WindingRule;}
				@Override public boolean isDone() {return n==null;}
				@Override public void next() {n=n.next;}

				void sum(double[]d,float[]f,int i)
				{if(f!=null){a[i]+=f[i];f[i]=a[i];a[i+1]+=f[i+1];f[i+1]=a[i+1];}
					else{d[i]=(a[i]+=d[i]);d[i+1]=(a[i+1]+=d[i+1]);}}

				int curSeg(double[]d,float[]f)
				{System.arraycopy(n.a,0,d==null?f:d, 0, n.a.length);
				 if(n.relative) 
				 {sum(d,f,0);if(n.seg==SEG_QUADTO){sum(d,f,2);if(n.seg==SEG_CUBICTO)sum(d,f,4);}}
				 else{ a[0]=n.a[n.seg==SEG_CUBICTO?4:n.seg==SEG_QUADTO?2:0];
						a[1]=n.a[n.seg==SEG_CUBICTO?5:n.seg==SEG_QUADTO?3:1];}
				 return n.seg;}

				@Override public int currentSegment(float []coords){return curSeg(null,coords);}
				@Override public int currentSegment(double[]coords){return curSeg(coords,null);}
				//PI(){}
				PI(AffineTransform at){}
				PI(AffineTransform at,double flatness){}
			}//class PI

			@Override public PathIterator getPathIterator(AffineTransform at){return new PI(at);}
			@Override public PathIterator getPathIterator(AffineTransform at,double flatness){return new PI(at,flatness);}
			@Override public Rectangle getBounds() {return (Rectangle) bounds;}
			@Override public Rectangle2D getBounds2D() {return bounds;}
			@Override public boolean contains(double x, double y){return bounds.contains(x, y);}
			@Override public boolean contains(Point2D p){return bounds.contains(p);}
			@Override public boolean intersects(double x, double y, double w, double h){return bounds.intersects(x, y, w, h);}
			@Override public boolean intersects(Rectangle2D r){return bounds.intersects(r);}
			@Override public boolean contains(double x, double y, double w, double h){return bounds.contains(x, y, w, h);}
			@Override public boolean contains(Rectangle2D p){return bounds.contains(p);}

			}//class Geom
			}//Path
			//class Group extends Obj{AlphaCompisite}//a layer or grouped shapes, this class is for facilitating for the user the ability to select objects , in order for the user to do operations like copy/paste , or moving,,,ect
			//class Brush {interface IBrush ; class Color implements IBrush; class texture implements IBrush ; class Gradient implements IBrush {radial or linear; (rgba)color-stops; String id };
		}//class SqFrame
	}//class State
 }//class Char
////////////////////////////////////////////////////////////////
//ListSelectionModel;
//ListCellRenderer;
////////////////////////////////////////////////////////////////
 /**an implementation of the javax.swing.ListModel which is customized for this character/animation editor, specifically the instance variable List<Char>a*/
 class CharsListModel implements ListModel
 {	/**list of animation-characters, this instance variable is the most important part of this class, because almost all the methods-in-this-class depend on this instance variable*/
	List<Char>a=new ArrayList<Char>();

	/**a collection of the registered LisrDataListeners*/
	List<ListDataListener> listeners;

	/**from the ListModel interface, this method-implementation returns the "a" size, list of characters*/
	@Override public int getSize() {return a.size();}
	
	/**from the ListModel interface, this method-implementation returns the animation-character element in the list of characters "a"*/
	@Override public Object getElementAt(int index) {return a.get(index);}

	/**from the ListModel interface, this method-implementation registers the ListDataListener parameter, adds it in the list "listeners"*/
	@Override public void addListDataListener(ListDataListener l)
	{if(listeners==null)listeners=new ArrayList<ListDataListener>();listeners.add(l);}

	/**from the ListModel interface, this method-implementation unregisters the ListDataListener parameter, removes it from the list "listeners"*/
	@Override public void removeListDataListener(ListDataListener l)
	{if(listeners!=null)listeners.remove(l);}

	/**notifies all the registered ListDataListener in the list "listeners", that an event happened, the parameter is a convenience way to call the overloaded method with an integer*/
	void fireContentsChanged(Char c){int i=a.indexOf(c);fireContentsChanged(i);}

	/**this method-implementation notifies all the registered ListDataListener in the list "listeners", that an event happened, specifically, items in the list change, whether the items string-title or the actual references of the items*/
	void fireContentsChanged(int i)
	{if(listeners!=null)
	{	ListDataEvent e=new 
		ListDataEvent(charactersList,ListDataEvent.CONTENTS_CHANGED,i,i);
		for(ListDataListener l:listeners)try{l.contentsChanged(e);}
		catch(Exception ex){ex.printStackTrace();}}}

	/**this method-implementation notifies all the registered ListDataListener (in the list "listeners" instance variable), that an event happened, specifically, all the items have been replaced*/
	void fireAllContentsChanged()
	{if(listeners!=null){
		ListDataEvent e=new ListDataEvent(
			charactersList,ListDataEvent.CONTENTS_CHANGED,0,a==null?0:a.size()-1);
		for(ListDataListener l:listeners)
			try{l.contentsChanged(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**not-implementation */void fireIntervalAdded(){}
	/**not-implementation */void fireIntervalRemoved(){}

	/**create a new Character, this a high-level method call, in terms of that this method calls other lower leveled calls*/
	Char newChar(String name){Char c=new Char(name);a.add(c);fireContentsChanged(c);return c;}

 }//class CharsListModel

////////////////////////////////////////////////////////////////
 /**an implementation of the javax.swing.ListModel which is customized for this character/animation editor, this ListModel sub-class is tricky(similar to master/detail with the CharListModel), in that it would reflect a list of the states of the currently selected Char. so changing the reference of (what is current) would require notifying that this list changed.*/
 class StatesListModel implements ListModel
 {	/**convenience method for fetching what is the current Char, and then, getting the list, this method is cautious to check(highly likely NullPointerException) if the current Char would return null*/
	List<Char.State>states(){Char c=getCurrentChar();return c==null?null:c.states;}

	/**a collection of the registered LisrDataListeners*/
	List<ListDataListener> listeners;

	/**from the ListModel interface, this method-implementation returns the "a" size, list of characters*/
	@Override public int getSize() {List l=states();return l==null?0:l.size();}
	
	/**from the ListModel interface, this method-implementation returns the animation-character element in the list of characters "a"*/
	@Override public Object getElementAt(int index){List l=states();return l==null||index<0||index>=l.size()?null:l.get(index);}

	/**from the ListModel interface, this method-implementation registers the ListDataListener parameter, adds it in the list "listeners"*/
	@Override public void addListDataListener(ListDataListener l)
	{if(listeners==null)listeners=new ArrayList<ListDataListener>();listeners.add(l);}

	/**from the ListModel interface, this method-implementation unregisters the ListDataListener parameter, removes it from the list "listeners"*/
	@Override public void removeListDataListener(ListDataListener l)
	{if(listeners!=null)listeners.remove(l);}

	/**notifies all the registered ListDataListener in the list "listeners", that an event happened, the parameter is a convenience way to call the overloaded method with an integer*/
	void fireContentsChanged(Char.State c){List l=states();int i=l==null?-1:l.indexOf(c);fireContentsChanged(i);}

	/**this method-implementation notifies all the registered ListDataListener in the list "listeners", that an event happened, specifically, items in the list change, whether the items string-title or the actual references of the items*/
	void fireContentsChanged(int i)
	{if(listeners!=null){
		ListDataEvent e=new ListDataEvent(
			statesList,ListDataEvent.CONTENTS_CHANGED,i,i);
		for(ListDataListener l:listeners)
			try{l.contentsChanged(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**this method-implementation notifies all the registered ListDataListener (in the list "listeners" instance variable), that an event happened, specifically, all the items have been replaced*/
	void fireAllContentsChanged()
	{if(listeners!=null){List L=states();
		ListDataEvent e=new ListDataEvent(
			statesList,ListDataEvent.CONTENTS_CHANGED,0,L==null?0:L.size()-1);
		for(ListDataListener l:listeners)
			try{l.contentsChanged(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**not-implementation */void fireIntervalAdded(){}
	/**not-implementation */void fireIntervalRemoved(){}

	/**create a new Character-State, this a high-level method call, in terms of that this method calls other lower leveled calls*/
	Char.State newState(String name)
	{Char.State c=getCurrentChar().newState(name);fireContentsChanged(c);return c;}

}//class StatesListModel

////////////////////////////////////////////////////////////////
 /**an implementation of the javax.swing.ListModel which is customized for this character/animation editor, this ListModel sub-class is tricky(similar to master/detail with the StatesListModel), in that it would reflect a list of the SqFrame of the currently selected Char.State. so changing the reference of (what is current) would require notifying that this list changed.*/
 class SqFramesListModel implements ListModel
 {	/**convenience method for fetching what is the current state, and then, getting the list of sqFrames, this method is cautious to check(highly likely NullPointerException) if the current State would return null*/
	List<Char.State.SqFrame>sqframes(){Char.State s=getCurrentState();return s==null?null:s.sqframes;}

	/**from the ListModel interface, this method-implementation returns the "a" size, list of characters*/
	@Override public int getSize() {List l=sqframes();return l==null?0:l.size();}
	/**from the ListModel interface, this method-implementation returns the animation-character element in the list of characters "a"*/
	@Override public Object getElementAt(int index) {List l=sqframes();return l==null||index<0||index>=l.size()?null:l.get(index);}
	
	/**a collection of the registered LisrDataListeners*/
	List<ListDataListener> listeners;

	/**from the ListModel interface, this method-implementation registers the ListDataListener parameter, adds it in the list "listeners"*/
	@Override public void addListDataListener(ListDataListener l)
	{if(listeners==null)listeners=new ArrayList<ListDataListener>();listeners.add(l);}

	/**from the ListModel interface, this method-implementation unregisters the ListDataListener parameter, removes it from the list "listeners"*/
	@Override public void removeListDataListener(ListDataListener l)
	{if(listeners!=null)listeners.remove(l);}

	/**notifies all the registered ListDataListener in the list "listeners", that an event happened, the parameter is a convenience way to call the overloaded method with an integer*/
	void fireContentsChanged(Char.State.SqFrame c){List l=sqframes();int i=l==null?-1:l.indexOf(c);fireContentsChanged(i);}

	/**this method-implementation notifies all the registered ListDataListener in the list "listeners", that an event happened, specifically, items in the list change, whether the items string-title or the actual references of the items*/
	void fireContentsChanged(int i)
	{if(listeners!=null){
		ListDataEvent e=new ListDataEvent(
			sqFramesList,ListDataEvent.CONTENTS_CHANGED,i,i);
		for(ListDataListener l:listeners)
			try{l.contentsChanged(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**this method-implementation notifies all the registered ListDataListener (in the list "listeners" instance variable), that an event happened, specifically, all the items have been replaced*/
	void fireAllContentsChanged()
	{if(listeners!=null){List L=sqframes();
		ListDataEvent e=new ListDataEvent(
			sqFramesList,ListDataEvent.CONTENTS_CHANGED,0,L==null?0:L.size()-1);
		for(ListDataListener l:listeners)
			try{l.contentsChanged(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**not-implementation */void fireIntervalAdded(){}
	/**not-implementation */void fireIntervalRemoved(){}

	/**create a new sequence frame, this a high-level method call, in terms of that this method calls other lower leveled calls*/
	Char.State.SqFrame newSqFrame(String name)
	{Char.State.SqFrame c=getCurrentState() .newSqFrame(name);fireContentsChanged(c);return c;}

 }//class SqFramesListModel
////////////////////////////////////////////////////////////////
 /**an implementation of the javax.swing.ListModel which is customized for this character/animation editor, this ListModel sub-class is tricky(similar to master/detail with the SqFramesListModel), in that it would reflect a list of the SqFrame of the currently selected SqFrame . so changing the reference of (what is current) would require notifying that this list changed.*/
 class ShapesListModel implements ListModel
 {	/**convenience method for fetching what is the current sqFrame, and then, getting the list of shapes, this method is cautious to check(highly likely NullPointerException) if the current sqFrame would return null*/
	List<Char.State.SqFrame.Shape>shps(){Char.State.SqFrame s=getCurrentSqFrame();return s==null?null:s.shapes;}

	/**from the ListModel interface, this method-implementation returns the "a" size, list of characters*/
	@Override public int getSize() {List l=shps();return l==null?0:l.size();}

	/**from the ListModel interface, this method-implementation returns the animation-character element in the list of characters "a"*/
	@Override public Object getElementAt(int index) {List l=shps();return l==null||index<0||index>=l.size()?null:l.get(index);}
	
	/**a collection of the registered LisrDataListeners*/
	List<ListDataListener> listeners;

	/**from the ListModel interface, this method-implementation registers the ListDataListener parameter, adds it in the list "listeners"*/
	@Override public void addListDataListener(ListDataListener l)
	{if(listeners==null)listeners=new ArrayList<ListDataListener>();listeners.add(l);}

	/**from the ListModel interface, this method-implementation unregisters the ListDataListener parameter, removes it from the list "listeners"*/
	@Override public void removeListDataListener(ListDataListener l)
	{if(listeners!=null)listeners.remove(l);}

	/**notifies all the registered ListDataListener in the list "listeners", that an event happened, the parameter is a convenience way to call the overloaded method with an integer*/
	void fireContentsChanged(Char.State.SqFrame.Shape c)
	{List l=shps();int i=l==null?-1:l.indexOf(c);fireContentsChanged(i);}

	/**this method-implementation notifies all the registered ListDataListener in the list "listeners", that an event happened, specifically, items in the list change, whether the items string-title or the actual references of the items*/
	void fireContentsChanged(int i)
	{if(listeners!=null){
		ListDataEvent e=new ListDataEvent(
			shapesList,ListDataEvent.CONTENTS_CHANGED,i,i);
		for(ListDataListener l:listeners)
			try{l.contentsChanged(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**this method-implementation notifies all the registered ListDataListener (in the list "listeners" instance variable), that an event happened, specifically, all the items have been replaced*/
	void fireAllContentsChanged()
	{if(listeners!=null){List L=shps();
		ListDataEvent e=new ListDataEvent(
			shapesList,ListDataEvent.CONTENTS_CHANGED,0,L==null?0:L.size()-1);
		for(ListDataListener l:listeners)
			try{l.contentsChanged(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**this method-implementation notifies all the registered ListDataListener (in the list "listeners" instance variable), that an event happened, specifically, an item has been added, the parameter is a convenience way to call the overloaded method with an integer*/
	void fireIntervalAdded(Char.State.SqFrame.Shape c)
	{List l=shps();int i=l==null?-1:l.indexOf(c);fireIntervalAdded(i);}

	/**this method-implementation notifies all the registered ListDataListener (in the list "listeners" instance variable), that an event happened, specifically, an item has been added*/
	void fireIntervalAdded(int i)
	{if(listeners!=null){
		ListDataEvent e=new ListDataEvent(
			shapesList,ListDataEvent.INTERVAL_ADDED,i,i);
		for(ListDataListener l:listeners)
			try{l.intervalAdded(e);}
			catch(Exception ex){ex.printStackTrace();}}}

	/**not-implementation */void fireIntervalRemoved(){}

	/**create a new Rectangle shape, this a high-level method call, in terms of that this method calls other lower leveled calls*/
	Char.State.SqFrame.Shape newRect(String name)
	{Char.State.SqFrame.Shape c=getCurrentSqFrame().newRect(name);fireContentsChanged(c);return c;}

	/**create a new oval shape, this a high-level method call, in terms of that this method calls other lower leveled calls*/
	Char.State.SqFrame.Shape newOval(String name)
	{Char.State.SqFrame.Shape c=getCurrentSqFrame().newOval();fireContentsChanged(c);return c;}

	/**create a new user-defined shape, this a high-level method call, in terms of that this method calls other lower leveled calls*/
	Char.State.SqFrame.Shape newPath(String name)
	{Char.State.SqFrame.Shape c=getCurrentSqFrame().newPath();fireContentsChanged(c);return c;}

	/**create a new image shape, this a high-level method call, in terms of that this method calls other lower leveled calls*/
	Char.State.SqFrame.Shape newImg(String name)
	{Char.State.SqFrame.Shape c=getCurrentSqFrame().newImg();fireContentsChanged(c);return c;}

}//class ShapesListModel
////////////////////////////////////////////////////////////////
 /**a gui panel that facilitates for the user operations on the currently select Char*/
 class CharProps extends JPanel
 {	/**a reference of "what is currently selected char", until now(2011-06-30), the two source-code-locations that change this reference are 1-Main2.valueChanged  and 2-Main2.focusGained*/
	Char current;
	
 	JButton
 	/**a button /gui for the user to create a new char , the Main2.prompt method is used to get a name for it*/
 	newBtn
 	
 	/**a button /gui for the user to change the name of the currently selected char using the Main2.prompt method*/
 	,nameBtn
 
 	,/**a button /gui for the user to do the save operation*/
 	saveBtn
 	
 	,/**a button /gui for the user to do the load operation*/
 	loadBtn
 	;
 
	/**constructor to initialize the gui, and register action listeners on the buttons, and the actual ActionListener actionPerformed implementation */
 	CharProps()
	{	ActionListener al=new ActionListener()
		{@Override public void actionPerformed(ActionEvent e) 
			{Object src=e.getSource();Char car=getCurrentChar();
				if(src==newBtn)
				{	p("add");
					String v=prompt("Enter name for new Character","defaultChar");
					if(v!=null)
						setCurrent(charsListModel.newChar(v));
	 			}else if(src==nameBtn)
				{	p("name:"+car);
					String v=prompt("Enter name for highlighted Character",car.getName());
					if(v!=null){car.setName(v);nameBtn.setText(v);charsListModel.fireContentsChanged(car);}
		 		}else if(src==saveBtn)
				{	p("CharProps.act:save");
					try{if(fileChooser.showSaveDialog(panelsFrame)==fileChooser.APPROVE_OPTION)
						XmlS.save(fileChooser.getSelectedFile().getCanonicalPath(), charsListModel.a);}
					catch(Exception ex){ex.printStackTrace();}}
			 	else if(src==loadBtn)
				{p("CharProps.act:load");
				 try{if(fileChooser.showOpenDialog(panelsFrame)==fileChooser.APPROVE_OPTION)
				  {charsListModel.a.clear();
					XmlS.load(fileChooser.getSelectedFile().getCanonicalPath(), charsListModel.a);
					charsListModel.fireAllContentsChanged();
					statesListModel.fireAllContentsChanged();
					sqFramesListModel.fireAllContentsChanged();
					shapesListModel.fireAllContentsChanged();
				}}
				catch(Exception ex){ex.printStackTrace();}}
				else p("CharProps.act:"+car);
				
		}	};
		brdrTtl(this,"Character props");
		add(loadBtn=btn("Load",al));
		add(saveBtn=btn("Save as",al));
		add(newBtn=btn("new Character",al));
		add(brdrTtl(nameBtn=btn("",al),"Name"));
	}

	/**a high leveled method, that sets/changes "what is the current char", and updates the gui accordingly to the new selection to display(on the gui) the properties, and update all the other guis, and tell JVM to set focus */
 	void setCurrent(Char car)
	{	current=car;
		if(car!=null)nameBtn.setText(car.getName());
		propsSplit.setRightComponent(this);
		statesListModel.fireAllContentsChanged();
		sqFramesListModel.fireAllContentsChanged();
		shapesListModel.fireAllContentsChanged();
		charactersList.grabFocus();
	}

 }//class CharProps
////////////////////////////////////////////////////////////////
 /**a gui panel that facilitates for the user operations on the currently select state*/
 class StateProps extends JPanel
 {	/**a reference of "what is the currently selected state", until now(2011-06-30), the two source-code-locations that change this reference are 1-Main2.valueChanged  and 2-Main2.focusGained*/
	Char.State current;

	JButton
 	/**a button /gui for the user to create a new char , the Main2.prompt method is used to get a name for it*/
 	newBtn 

	,/**a button /gui for the user to change the name of the currently selected char using the Main2.prompt method*/
 	nameBtn
 
 	/*,*a button /gui for the user to start play/view/running the animation (2011-06-30 not implemented)* /
 	play*/;

	/**constructor to initialize the gui, and register action listeners on the buttons, and the actual ActionListener actionPerformed implementation */
 	StateProps()
	{	ActionListener al=new ActionListener() 
		{@Override public void actionPerformed(ActionEvent e) 
		{Object src=e.getSource();Char.State stt=getCurrentState();
			if(src==newBtn)
			{	p("new");
				String v=prompt("Enter name for new State","Ideal");
				if(v!=null)
					setCurrent(statesListModel.newState(v));
			}else if(src==nameBtn)
			{	p("name");
				String v=prompt("Enter Name for highlighted State",stt.getName());
				if(v!=null){stt.setName(v);nameBtn.setText(v);statesListModel.fireContentsChanged(stt);}
			}else p("stateProps.btn:"+stt+":"+e);
		}	};
		brdrTtl(this,"State props");
		add(newBtn=btn("new State",al));
		add(brdrTtl(nameBtn=btn("",al),"Name"));
		//add(upButton=btn("up",al));add(downButton=btn("down",al));add(duplicateButton=btn("duplicate",al));add(play=btn("play",al));
	}

 	/**a high leveled method, that sets/changes "what is the current state", and updates the gui accordingly to the new selection to display(on the gui) the properties, and update all the other guis, and tell JVM to set focus */
 	void setCurrent(Char.State stt)
	{	current=stt;
		if(stt!=null)nameBtn.setText(stt.getName());
		propsSplit.setRightComponent(this);
		sqFramesListModel.fireAllContentsChanged();
		shapesListModel.fireAllContentsChanged();
		statesList.grabFocus();
	}

 }//class StateProps
////////////////////////////////////////////////////////////////
 /**a gui panel for the user to do operations and view the properties of the currently select sqFrame*/
 class SqFrameProps extends JPanel
 {	/**a reference of "what is the currently selected sqFrame", until now(2011-06-30), the two source-code-locations that change this reference are 1-Main2.valueChanged  and 2-Main2.focusGained*/
	Char.State.SqFrame current;
	JButton newBtn
	,durationBtn
	/*,*a button /gui for the user to change the order of the currently selected char within the charactersListModel list, (2011-06-30 not implemented)* /
 	upButton
 	
 	,/**a button /gui for the user to change the order of the currently selected char within the charactersListModel list, (2011-06-30 not implemented)* /
 	downButton
	
 	,/**a button /gui for the user to make a copy of the currently selected sqFrame along with all the inner objects, (2011-06-30 not implemented)* /
 	duplicateButton*/;

	/**constructor to initialize the gui, and register action listeners on the buttons, and the actual ActionListener actionPerformed implementation */
 	SqFrameProps()
	{	ActionListener al=new ActionListener() 
		{@Override public void actionPerformed(ActionEvent e) 
		{Object src=e.getSource();Char.State.SqFrame sqfrm=getCurrentSqFrame();
			if(src==newBtn)
			{	p("new");
				setCurrent(sqFramesListModel.newSqFrame(""));
			}else if(src==durationBtn)
			{	p("name");
			String v=prompt("Enter duration in miliseconds",sqfrm.dura());
			if(v!=null){sqfrm.setDuration(Long.parseLong(v));durationBtn.setText(v);}
		}else p("sqFrameProps.act:"+sqfrm);
		}	};
		brdrTtl(this,"SqFrame props");
		add(newBtn=btn("new SqFrame",al));
		add(brdrTtl(durationBtn=btn("1000",al),"Duration"));
		//add(upButton=btn("up",al));add(downButton=btn("down",al));add(duplicateButton=btn("duplicate",al));
	}

 	/**a high leveled method, that sets/changes "what is the current sqFrame", and updates the gui accordingly to the new selection to display(on the gui) the properties, and update all the other guis, and tell JVM to set focus */
 	void setCurrent(Char.State.SqFrame sqfrm)
	{	current=sqfrm;
		if(sqfrm!=null)durationBtn.setText(sqfrm.dura());
		propsSplit.setRightComponent(this);
		shapesListModel.fireAllContentsChanged();
		sqFramesList.grabFocus();
	}

 }//class SqFrameProps
////////////////////////////////////////////////////////////////
 /**a gui panel that facilitates for the user operations on the currently select shape*/
 class ShapeProps extends JPanel
 {	/**a reference of "what is the currently selected sqFrame", until now(2011-06-30), the two source-code-locations that change this reference are 1-Main2.valueChanged  and 2-Main2.focusGained*/
	Char.State.SqFrame.Shape current;

	JButton 
	newRectBtn,newOvalBtn,newPathBtn,newImgBtn
 	/*,*a button /gui for the user to change the order of the currently selected char within the charactersListModel list, (2011-06-30 not implemented)* /
 	upButton

 	,/**a button /gui for the user to change the order of the currently selected char within the charactersListModel list, (2011-06-30 not implemented)* /
 	downButton

 	,/**a button /gui for the user to make a copy of the currently selected char along with all the inner objects, (2011-06-30 not implemented)* /
 	duplicateButton*/
	//,convRect,convOval,convPath,convImg
	//,outline,fill
	,colorBtn
	,positionBtn
	,dimBtn
	,imgBtn
	;
	JPanel imgPanel;//,pathPanel;
	
	//JRadioButton pick,move;//,resize,rotate,moveOrig
/*drop downs / modes
 * fill (none,solid,pattern,gradient(linear,radial),texture) (alpha)
 * outline(none,solid,pattern,gradient(linear,radial),texture) (alpha) (width)
 * tool(
 * 		moveOrig , move , scale , rotate , zoom 
 * 		, selectionAShapes(rubberBandBox(include / exclude))
 * 		, brush (addNew(copy) , list-saved , select/activate / paste)
 * 		, pathTool(drawLines,deleteVertix
 * 			, selectionVertices(include/exclude,rubberBand Include/exclude)
 * 			, addVertex, move(Vertex/selectionVertices)
 * 		) 
 *		, workspace-config(orig,zoomScale,viewOffset,rulers/guideLines,showGrid,unionPeels)
 * )
 * save/load selection (mu-ex groups)
 * 		Abstract-Shapes / brush
 * 		Vertices
 * inter-frame interpolation(move or resize or scale) 
 * 		each(frame-delta value or function by frame(time) (frame/time)-table-based-(values/deltas) 
 * 		,and , inter-frame selection-AShapes(references across frames of the same AShapes)
 * inter-frame color-anim / picking
 * 
 */
 ShapeProps()
 {	ActionListener al=new ActionListener() 
	{@Override public void actionPerformed(ActionEvent e) 
	{	Object src=e.getSource();
		Char.State.SqFrame f=getCurrentSqFrame();
		Char.State.SqFrame.Shape shaip=getCurrentShape();
		if(src==newRectBtn){p("ShapeProps:act:newRect:"+f);shaip=f.newRect();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
		else if(src==newOvalBtn){p("ShapeProps:act:newOval:"+f);shaip=f.newOval();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
		else if(src==newPathBtn){p("ShapeProps:act:newPath:"+f);shaip=f.newPath();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
		else if(src==newImgBtn){p("ShapeProps:act:newImg:"+f);shaip=f.newImg ();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
		else if(src==positionBtn)
		{	p("ShapeProps:act:pos:"+f);
			String v=prompt("set position( x , y )",String.valueOf(shaip.x)+" , "+shaip.y);
			if(v!=null){int i=v.indexOf(",");
				shaip.x=Integer.parseInt(v.substring(0, i).trim());
				shaip.y=Integer.parseInt(v.substring( i+1).trim());
				frame.repaint();}}
		else if(src==dimBtn)
		{	p("ShapeProps:act:dim:"+f);
			String v=prompt("set dimension( w , h )",String.valueOf(shaip.w)+" , "+shaip.h);
			if(v!=null){int i=v.indexOf(",");
				shaip.w=Integer.parseInt(v.substring(0, i).trim());
				shaip.h=Integer.parseInt(v.substring( i+1).trim());
				frame.repaint();}}
		else if(src==imgBtn&&shaip instanceof Char.State.SqFrame.Img)
		{	Char.State.SqFrame.Img m=(Char.State.SqFrame.Img)shaip;
			p("ShapeProps:act:img:" +f+":"+m);
			try{if(fileChooser.showOpenDialog(panelsFrame)==JFileChooser.APPROVE_OPTION)
				m.setImg(fileChooser.getSelectedFile());frame.repaint();
			}catch(Exception ex){ex.printStackTrace();}}
		else if(src==colorBtn){p("ShapeProps:act:color:" +f);
			Color c=colorChooser.showDialog(panelsFrame, "choose a color", shaip.c);
			if(c!=null)shaip.c=c;frame.repaint();}
		else p("ShapeProps:act:"+shaip);
	}};
	brdrTtl(this,"Shape props");
	//add(upButton=btn("up",al));add(downButton=btn("down",al));add(duplicateButton=btn("duplicate",al));
	add(newRectBtn=btn("newRect",al));
	add(newOvalBtn=btn("newOval",al));
	add(newPathBtn=btn("newPath",al));
	add(newImgBtn=btn("newImg",al));
	add(colorBtn=btn("color",al));
	add(positionBtn=btn("position",al));
	add(dimBtn=btn("resize",al));
	add(imgBtn=btn("image",al));


	//add(brdrTtl(toolsPanel=new JPanel(),"Tool"));toolsPanel.add(move=new JRadioButton("move"));toolsButtonGroup.add(move);move.addActionListener(al);toolsPanel.add(pick=new JRadioButton("pick"));toolsButtonGroup.add(pick);pick.addActionListener(al);
 }//constructor ShapeProps

	void setCurrent(Char.State.SqFrame.Shape r)
	{	current=r;
		propsSplit.setRightComponent(this);
		//shapesListModel.fireAllContentsChanged();
	}
 class PathProps extends JPanel
 {
	JButton 
	newMoveToBtn
	,newLineToBtn
	,newQuadToBtn
	,newCubicToBtn
	,newCloseBtn
	,deleteBtn
	,alphaBtn
	,paintColorBtn
	,paintLinearGradientBtn
	,paintRadialGradientBtn
	,paintNoneBtn
	,paintTxtrBtn

	//,transformBtn
	,strokeWidthBtn
	,strokeColorBtn
	/*,*a button /gui for the user to change the order of the currently selected char within the charactersListModel list, (2011-06-30 not implemented)* /
	upButton

 	,/**a button /gui for the user to change the order of the currently selected char within the charactersListModel list, (2011-06-30 not implemented)* /
	downButton

	,/**a button /gui for the user to make a copy of the currently selected char along with all the inner objects, (2011-06-30 not implemented)* /
	duplicateButton*/

	,positionBtn

	;JList verticesList;

	 PathProps()
	 {	ActionListener al=new ActionListener() 
		{@Override public void actionPerformed(ActionEvent e) 
		{	Object src=e.getSource();
			Char.State.SqFrame f=getCurrentSqFrame();
			Char.State.SqFrame.Shape shaip=getCurrentShape();
			if(src==newRectBtn){p("ShapeProps:act:newRect:"+f);shaip=f.newRect();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
			else if(src==newOvalBtn){p("ShapeProps:act:newOval:"+f);shaip=f.newOval();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
			else if(src==newPathBtn){p("ShapeProps:act:newPath:"+f);shaip=f.newPath();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
			else if(src==newImgBtn){p("ShapeProps:act:newImg:"+f);shaip=f.newImg ();shapesListModel.fireIntervalAdded(shaip);frame.repaint();}
			else if(src==positionBtn)
			{	p("ShapeProps:act:pos:"+f);
				String v=prompt("set position( x , y )",String.valueOf(shaip.x)+" , "+shaip.y);
				if(v!=null){int i=v.indexOf(",");
					shaip.x=Integer.parseInt(v.substring(0, i).trim());
					shaip.y=Integer.parseInt(v.substring( i+1).trim());
					frame.repaint();}}
			else if(src==dimBtn)
			{	p("ShapeProps:act:dim:"+f);
				String v=prompt("set dimension( w , h )",String.valueOf(shaip.w)+" , "+shaip.h);
				if(v!=null){int i=v.indexOf(",");
					shaip.w=Integer.parseInt(v.substring(0, i).trim());
					shaip.h=Integer.parseInt(v.substring( i+1).trim());
					frame.repaint();}}
			else if(src==imgBtn&&shaip instanceof Char.State.SqFrame.Img)
			{	Char.State.SqFrame.Img m=(Char.State.SqFrame.Img)shaip;
				p("ShapeProps:act:img:" +f+":"+m);
				try{if(fileChooser.showOpenDialog(panelsFrame)==JFileChooser.APPROVE_OPTION)
					m.setImg(fileChooser.getSelectedFile());frame.repaint();
				}catch(Exception ex){ex.printStackTrace();}}
			else if(src==colorBtn){p("ShapeProps:act:color:" +f);
				Color c=colorChooser.showDialog(panelsFrame, "choose a color", shaip.c);
				if(c!=null)shaip.c=c;frame.repaint();}
			else p("ShapeProps:act:"+shaip);
		}};
		brdrTtl(this,"Shape props");
		//add(upButton=btn("up",al));add(downButton=btn("down",al));add(duplicateButton=btn("duplicate",al));
		add(newRectBtn=btn("newRect",al));
		add(newOvalBtn=btn("newOval",al));
		add(newPathBtn=btn("newPath",al));
		add(newImgBtn=btn("newImg",al));
		add(colorBtn=btn("color",al));
		add(positionBtn=btn("position",al));
		add(dimBtn=btn("resize",al));
		add(imgBtn=btn("image",al));


		//add(brdrTtl(toolsPanel=new JPanel(),"Tool"));toolsPanel.add(move=new JRadioButton("move"));toolsButtonGroup.add(move);move.addActionListener(al);toolsPanel.add(pick=new JRadioButton("pick"));toolsButtonGroup.add(pick);pick.addActionListener(al);
	 }//constructor PathProps
 }//PathProps
}//class ShapeProps
////////////////////////////////////////////////////////////////
class MyPanel extends JPanel implements MouseListener,MouseMotionListener
{public void paint(Graphics g)
 {Char.State.SqFrame sf=getCurrentSqFrame();
	if(sf!=null)sf.paint(g);
	if(over!=null){//g.drawRect(over.x-over.w/2+2, over.y-over.h/2+2, over.w-4, over.h-4);
		overb.paintBorder(this, g, over.x-over.w/2+2, over.y-over.h/2+2, over.w-4, over.h-4);}
	if(selctd!=null){//g.drawRect(selctd.x-selctd.w/2, selctd.y-selctd.h/2, selctd.w, selctd.h);
		selb.paintBorder(this, g, selctd.x-selctd.w/2, selctd.y-selctd.h/2, selctd.w, selctd.h);
		paintBorder(g, selctd);
	}
 }
	Char.State.SqFrame.Shape over,selctd;
	int ox,oy,ox2,oy2,ox3,oy3,corner,cornerRadius=7;
	Border selb=BorderFactory.createRaisedBevelBorder()
	,overb=BorderFactory.createLoweredBevelBorder();

	MyPanel(){addMouseListener(this);addMouseMotionListener(this);}

 boolean isOver(Char.State.SqFrame.Shape p,int px,int py)
 {return p.x-p.w/2<=px && px<=p.x+p.w/2 && p.y-p.h/2<=py && py<=p.y+p.h/2;}
 

 int getCornerX(Char.State.SqFrame.Shape p,int corner)
 {int w2=p.w/2,d2=cornerRadius/2;
  return corner==1 || corner==4 || corner==6
		?p.x-w2-d2
	: corner==2 || corner==7
		?p.x-d2
	://corner==3 || corner==5 || corner==8?
		p.x+w2-d2 ;}

 int getCornerY(Char.State.SqFrame.Shape p,int corner)
 {int h2=p.h/2,d2=cornerRadius/2;
  return corner<4?p.y-h2-d2:corner<6?p.y-d2:p.y-d2+h2;}

 int getCorner(Char.State.SqFrame.Shape p,int mx,int my)
 {for(int corner=8;corner>=1;corner--)
  {int x=getCornerX(p,corner),y=getCornerY(p,corner);
	if( x<=mx && mx<=x+cornerRadius && y<=my && my<=y+
		cornerRadius)return corner;}return 0;}

void paintBorder(Graphics g,Char.State.SqFrame.Shape p)
{//Color c=null;
	for(int corner=1;corner<=8;corner++)
		if(corner==this.corner)g.fillOval(getCornerX(p, corner), getCornerY(p, corner), cornerRadius+5, cornerRadius+5);else
		//g.fill3DRect(getCornerX(p, corner), getCornerY(p, corner), cornerRadius, cornerRadius, raised);
		//g.fillRoundRect(getCornerX(p, corner), getCornerY(p, corner), cornerRadius, cornerRadius, arcWidth, arcHeight);
		g.fillRect(getCornerX(p, corner), getCornerY(p, corner), cornerRadius, cornerRadius);
/*approach2
	int w2=p.w/2,h2=p.h/2,d=cornerRadius,d2=d/2;
	g.fillRect(p.x-w2-d2, p.y-h2-d2, d, d);
	g.fillRect(p.x-d2, p.y-h2-d2, d, d);
	g.fillRect(p.x+w2-d2, p.y-h2-d2, d, d);

	g.fillRect(p.x-w2-d2, p.y-d2, d, d);
	g.fillRect(p.x+w2-d2, p.y-d2, d, d);

	g.fillRect(p.x-w2-d2, p.y+h2-d2, d, d);
	g.fillRect(p.x-d2, p.y+h2-d2, d, d);
	g.fillRect(p.x+w2-d2, p.y+h2-d2, d, d);
*approach1
	g.fillRect(s.x-s.w/2, s.y-s.h/2, 4, 4);
	g.fillRect(s.x-2, s.y-s.h/2, 4, 4);
	g.fillRect(s.x+s.w/2-4, s.y-s.h/2, 4, 4);

	g.fillRect(s.x-s.w/2, s.y-2, 4, 4);
	g.fillRect(s.x+s.w/2-4, s.y-2, 4, 4);

	g.fillRect(s.x-s.w/2, s.y+s.h/2-4, 4, 4);
	g.fillRect(s.x-2, s.y+s.h/2-4, 4, 4);
	g.fillRect(s.x+s.w/2-4, s.y+s.h/2-4, 4, 4);*/
}

 @Override public void mouseMoved(MouseEvent e) 
 {Char.State.SqFrame sf=getCurrentSqFrame();
  if(sf!=null)
  {	Char.State.SqFrame.Shape s=null,x;
	List<Char.State.SqFrame.Shape>a=sf.shapes;
	int n=a.size(),i=-1,mx=e.getX(),my=e.getY(),oc=corner;
	if(selctd!=null)
		corner=getCorner(selctd,mx,my);
	boolean needRepaint=false;
	while(s==null&& ++i <n){x=a.get(i);if(isOver(x, mx,my))s=x;}
	if(oc!=corner){p("mouseMoved:corner="+corner);needRepaint=true;}
	if(s!=over){over=s;p("mouseMoved:over="+over);needRepaint=true;}
	if(needRepaint)frame.repaint();
 }}

 @Override public void mousePressed(MouseEvent e) 
 {	int mx=e.getX(),my=e.getY();
	Char.State.SqFrame sf=getCurrentSqFrame();
	boolean needRepaint=false;
	if(sf!=null)
	{Char.State.SqFrame.Shape s=null,x;
		List<Char.State.SqFrame.Shape>a=sf.shapes;
		int n=a.size(),i=-1,oc=corner;
		while(s==null&& ++i <n)
		{	x=a.get(i);
			if(isOver(x, mx,my))
				s=x;
		}
		if(s!=selctd){selctd=s;p("mousePressed:selctd="+selctd);needRepaint=true;}
		oc=corner;if(selctd!=null)
			corner=getCorner(selctd,mx,my);
		if(oc!=corner){p("mousePressed:corner="+corner);needRepaint=true;}
	}
	if(selctd!=null)
	{	corner=getCorner(selctd,mx,my);
		if(corner==0){ox=selctd.x-mx;oy=selctd.y-my;needRepaint=true;}
		else{ox=mx;//getCornerX(selctd, corner)-mx;
			oy=my;//getCornerY(selctd, corner)-my;
			ox2=selctd.x;oy2=selctd.y;ox3=selctd.w;oy3=selctd.h;
			needRepaint=true;}
	}if(needRepaint)frame.repaint();}

 @Override public void mouseDragged(MouseEvent e) 
 {if(selctd!=null){int mx=e.getX(),my=e.getY(),dx=mx-ox,dy=my-oy;
	if(corner==8)
	{selctd.x=ox2+dx/2;
	 selctd.y=oy2+dy/2;
	 selctd.w=ox3+dx;
	 selctd.h=oy3+dy;}
	else //if(corner==0)
	{selctd.x=mx+ox;selctd.y=my+oy;}
	frame.repaint();}}

 @Override public void mouseReleased(MouseEvent e) 
 {if(selctd!=null){
	if(corner==0){selctd.x=e.getX()+ox;selctd.y=e.getY()+oy;}
	else{}
	frame.repaint();}}

 @Override public void mouseClicked(MouseEvent e){}//{if(over!=selctd){selctd=over;p("mouseClicked:selctd="+selctd);frame.repaint();}}

 @Override public void mouseEntered(MouseEvent e) {}

 @Override public void mouseExited(MouseEvent e) {}

}//class MyPanel

////////////////////////////////////////////////////////////////

JFrame frame,panelsFrame;
JSplitPane propsSplit;
JList charactersList,statesList,sqFramesList,shapesList;
CharsListModel charsListModel;
StatesListModel statesListModel;
SqFramesListModel sqFramesListModel;
ShapesListModel shapesListModel;
CharProps charProps=new CharProps();
StateProps stateProps=new StateProps();
SqFrameProps sqFrameProps=new SqFrameProps();
ShapeProps shapeProps=new ShapeProps();
MyPanel myPanel;

 /**keep track(reference) of the current focus (List)*/
 Object current;

 /**call-back method for a change of selection in a JList*/
 @Override public void valueChanged(ListSelectionEvent e)
 {	Object src=e.getSource();current=src;
	if(src==charactersList)
	{	p("ListSelectionListener.valueChanged:char:"+e);
		charProps.setCurrent((Char)charactersList.getSelectedValue());
	}
	else if(src==statesList)
	{	p("ListSelectionListener.valueChanged:char:"+e);
		stateProps.setCurrent((Char.State)statesList.getSelectedValue());
	}
	else if(src==charactersList)
	{	p("ListSelectionListener.valueChanged:sqFrame:"+e);
		sqFrameProps.setCurrent((Char.State.SqFrame)sqFramesList.getSelectedValue());
	}
	else if(src==shapesList)
	{	p("ListSelectionListener.valueChanged:shape:"+e);
		shapeProps.setCurrent((Char.State.SqFrame.Shape)shapesList.getSelectedValue());
	}
	else p("ListSelectionListener.valueChanged:selection:"+e);frame.repaint();
 }//valueChanged(ListSelectionEvent e)

 Char getCurrentChar(){return charProps.current;}
 Char.State getCurrentState(){return stateProps.current;}
 
 /** has a bug, when the char or state change, this method would still return the old Char.State.SqFrame */
 Char.State.SqFrame getCurrentSqFrame()
 {return sqFrameProps.current;}

 Char.State.SqFrame.Shape getCurrentShape(){return shapeProps.current;}

 JScrollPane listPanel(final JList c,String title)
 {	c.setBorder(new javax.swing.border.TitledBorder(title));
	c.addFocusListener(this);
	c.addListSelectionListener(this);
	return new JScrollPane(c);}

 @Override public void focusGained(FocusEvent e) 
 {	Object src=e.getSource();//p("focusGained:"+src);
	if(src==charactersList){Char c=(Char)charactersList.getSelectedValue();p("focusGained:char:"+c);charProps.setCurrent(c);}
	if(src==statesList){Char.State c=(Char.State)statesList.getSelectedValue();p("focusGained:stt:"+c);stateProps.setCurrent(c);}
	if(src==sqFramesList){Char.State.SqFrame c=(Char.State.SqFrame)sqFramesList.getSelectedValue();p("focusGained:sqFrame:"+c);sqFrameProps.setCurrent(c);}
	if(src==shapesList){Char.State.SqFrame.Shape c=(Char.State.SqFrame.Shape)
		shapesList.getSelectedValue();p("focusGained:shape:"+c);shapeProps.setCurrent(c);}
	frame.repaint();}

@Override public void focusLost(FocusEvent e) {p("focusLost:"+e);}

 public Main2()
 {	main=this;JFrame f=panelsFrame=new JFrame(
		"Anim/Character Editor, by M@Kgdug.org");
	frame=new JFrame("2:Anim/Character Editor, by M@Kgdug.org");
	frame.setContentPane(myPanel=new MyPanel());
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setBounds(0,0, 800, 300);
	frame.setBounds(0,300, 800, 600);
	JPanel c;
	f.setContentPane(propsSplit=new JSplitPane
		(JSplitPane.VERTICAL_SPLIT,true, 
		c=new JPanel(), charProps));//propsPanel=new JPanel()
	propsSplit.setOneTouchExpandable(true);
	c.setLayout(new GridLayout(1,4));
	c.add(listPanel(charactersList=new JList(charsListModel=new CharsListModel()),"Characters"));
	c.add(listPanel(statesList=new JList(statesListModel=new StatesListModel()),"States"));
	c.add(listPanel(sqFramesList=new JList(sqFramesListModel=new SqFramesListModel()),"SqFrames"));
	c.add(listPanel(shapesList=new JList(shapesListModel=new ShapesListModel()),"Shapes:Rects/Ovals/Polygons/Imgs"));
	f.setVisible(true);
	frame.setVisible(true);
	charactersList.addListSelectionListener(this);
	statesList.addListSelectionListener(this);
	sqFramesList.addListSelectionListener(this);
	shapesList.addListSelectionListener(this);
	try{String path="/home/moh/Desktop/kgdug/char.xml";XmlS.load(path);p("Main2.<init>:XmlS.load:"+path);
		charactersList.setSelectedIndex(0);
		statesList.setSelectedIndex(0);
		sqFramesList.setSelectedIndex(0);
		shapesList.setSelectedIndex(0);
		frame.repaint();
		shapesList.grabFocus();
		p("Main2.<init>:successfully initialized.");
	}catch(Exception ex){ex.printStackTrace();}
 }

 /** main program entry point. call constructor/instantiate Main2*/
 public static void main(String[] args) {new Main2();}
//////////////////////////////////////////////////////////////////////////////////
 static class XmlS
 {static void save(String fileName,List<Char>l)throws IOException
	{File f=new File(fileName);
	 Writer w=new FileWriter(f);
	 w(w,"",l);
	 w.close();
	}

	static void w(Writer w,String ind,List<Char>l)throws IOException{w.write("<Characters>\n");for(Char c:l)w(w,"\t",c);w.write("</Characters>\n");}
	static void w(Writer w,String ind,Char c)throws IOException
	{w.write(ind);w.write("<Character name=\"");w.write(c.getName());w.write("\">\n");
		for(Char.State s:c.states)w(w,ind+"\t",s);w.write(ind);w.write("</Character>\n");}
	static void w(Writer w,String ind,Char.State c)throws IOException
	{w.write(ind);w.write("<State name=\"");w.write(c.getName());w.write("\">\n");
		for(Char.State.SqFrame s:c.sqframes)w(w,ind+"\t",s);w.write(ind);w.write("</State>\n");}	
	static void w(Writer w,String ind,Char.State.SqFrame c)throws IOException
	{w.write(ind);w.write("<Frame>\n");
		for(Char.State.SqFrame.Shape s:c.shapes)w(w,ind+"\t",s);w.write(ind);w.write("</Frame>\n");}	

	static void w(Writer w,String ind,Char.State.SqFrame.Shape c,String n)throws IOException{w(w,ind,c,n,2);}
	static void w(Writer w,String ind,Char.State.SqFrame.Shape c,String n,int tag)throws IOException
	{w.write(ind);
		w.write("<");w.write(n);w.write(" x=\"");w.write(String.valueOf(c.x ));
		w.write("\" y=\"");w.write(String.valueOf( c.y));
		w.write("\" w=\"");w.write(String.valueOf( c.w));
		w.write("\" h=\"");w.write(String.valueOf( c.h));
		java.awt.Color o=c.c;
		if(o!=null)
		{int r=o.getRed(),g=o.getGreen(),b=o.getBlue(),a=o.getAlpha();
			w.write("\" c=\"#");
			if(a<16)w.write('0');
			w.write(Integer.toHexString(a));
			if(r<16)w.write('0');
			w.write(Integer.toHexString(r));
			if(g<16)w.write('0');
			w.write(Integer.toHexString(g));
			if(b<16)w.write('0');
			w.write(Integer.toHexString(b));
		}if(tag==2)w.write("\" />\n");
		else if(tag==1)w.write("\" >\n");
		else w.write("\" ");}

	static void w(Writer w,String ind,Char.State.SqFrame.Shape
		c)throws IOException{w(w,ind,c,"Shape");}

	static void w(Writer w,String ind,Char.State.SqFrame.Oval 
			c)throws IOException{w(w,ind,c,"Oval");}

	//should implement a separate method for the vertices
	static void w(Writer w,String ind,Char.State.SqFrame.Path 
			c)throws IOException{w(w,ind,c,"Path");
	w.write("path=\"");w.write(pathToString(c));w.write("\"/>\n");}

	static String pathToString(Char.State.SqFrame.Path p)
	{Char.State.SqFrame.Path.Vertex n=p.head;StringBuilder b=new StringBuilder();
		while(n!=null)
		{	if(n.seg==PathIterator.SEG_CLOSE)b.append("z;");
			else if(n.seg==PathIterator.SEG_MOVETO )b.append(n.relative?'m':'M').append(n.a[0]).append(",").append(n.a[1]).append(";");
			else if(n.seg==PathIterator.SEG_LINETO )b.append(n.relative?'l':'L').append(n.a[0]).append(",").append(n.a[1]).append(";");
			else if(n.seg==PathIterator.SEG_QUADTO )b.append(n.relative?'q':'Q').append(n.a[0]).append(",").append(n.a[1]).append(n.a[2]).append(",").append(n.a[3]).append(";");
			else if(n.seg==PathIterator.SEG_CUBICTO)b.append(n.relative?'c':'C').append(n.a[0]).append(",").append(n.a[1]).append(n.a[2]).append(",").append(n.a[3]).append(n.a[4]).append(",").append(n.a[5]).append(";");
			n=n.next;}
		return b.toString();}//"path-data place-holder"

	//should implement a separate method for the base64 of the image
	static void w(Writer w,String ind,Char.State.SqFrame.Img 
			c)throws IOException{w(w,ind,c,"Img",2);
		w.write("file=\"");w.write(c.file.getCanonicalPath());w.write("\"/>\n");}
/*should implement methods for the following:
 * fill
 	*(none,solid,pattern,gradient(linear,radial),texture) (alpha) 
 * outline(same as fill in terms of attribtes/modes)
 * transform(translation,rotation,scaling)
 * transformVertives
 * clipping(shape)
 * orig-point
 * duration
 * cell dimensions
 * zoom (factor , offset)
 * selectionAShapes
 * brush (list,active)
 * selectionVertices
 * workspace-config(orig,zoomScale,viewOffset,rulers/guideLines,showGrid,unionPeels)
 * )
 * save/load selection (mu-ex groups)
 * 		Abstract-Shapes / brush
 * 		Vertices
 * inter-frame interpolation(move or resize or scale) 
 * 		each(frame-delta value or function by frame(time) (frame/time)-table-based-(values/deltas) 
 * 		,and , inter-frame selection-AShapes(references across frames of the same AShapes)
 * inter-frame color-anim / picking
 * 
 * */
	static List<Char>load(String fileName)throws IOException
	{List<Char> l=new ArrayList<Char>();load(fileName,l);return l;}

	static List<Char>load(String fileName,List<Char>l)
	{try{DocumentBuilderFactory docBuilderFactory=DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder=docBuilderFactory.newDocumentBuilder();
		Document doc=docBuilder.parse(new File(fileName));
		doc.getDocumentElement().normalize();
		Element root=doc.getDocumentElement();
		Node n=root.getFirstChild();
		if(l==null)l=new ArrayList<Char>();
		while(n!=null)
			try{if(n instanceof Element)
				l.add(car((Element)n));
			n=n.getNextSibling();}
		catch(Exception ex){ex.printStackTrace();}
	}catch (Throwable t) {t.printStackTrace ();}return l;}

	static Char car(Element tag)
	{Char c=main.new Char(tag.getAttribute("name"));
	 Node n=tag.getFirstChild();
	 while(n!=null)
		 try{if(n instanceof Element)
			 state((Element)n,c);
		 n=n.getNextSibling();}
	 catch(Exception ex){ex.printStackTrace();}
	 return c;}

	static Char.State state(Element tag,Char car)
	{Char.State c=car.newState(tag.getAttribute("name"));
	 Node n=tag.getFirstChild();
	 while(n!=null)
		 try{if(n instanceof Element)
			 sqFrame((Element)n,c);
		 n=n.getNextSibling();}
	 catch(Exception ex){ex.printStackTrace();}
	 return c;}

	stat
