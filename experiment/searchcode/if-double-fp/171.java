/** class StationMap: the station map that is drawn on the level choosing panel and in game. */
package fp.s100502514.metroArmy;

import java.awt.*;
import java.util.GregorianCalendar;

public class StationMap {
	
	/** StationMap Constructor and getters/setters */
	private double beginPos=0, endPos=0, currentPos=Double.NaN;
	private int x=0, y=0, width=300, thickness=8;
	static final Station[] station_info = GameSceneData.getStationOrderInfo();
	
	public StationMap(int width, double beginPos, double endPos, double currentPos,
			int thickness, double[] select_range){
		setWidth(width);
		setThickness(thickness);
		setBeginPos(beginPos);
		setEndPos(endPos);
		setCurrentPos(currentPos);
		setSelectRange(select_range);
	}
	public StationMap(int width, double beginPos, double endPos, double currentPos,
			int thickness){
		this(width, beginPos, endPos, currentPos, thickness, null);
	}
	public StationMap(int width, double beginPos, double endPos, double currentPos){
		this(width, beginPos, endPos, currentPos, 8);
	}
	
	public double getBeginPos() {
		return beginPos;
	}
	public void setBeginPos(double beginPos) {
		this.beginPos = beginPos;
	}
	
	public double getEndPos() {
		return endPos;
	}
	public void setEndPos(double endPos) {
		this.endPos = endPos;
	}
	
	public double getLeftPos() {
		return Math.min(beginPos, endPos);
	}
	public double getRightPos() {
		return Math.max(beginPos, endPos);
	}

	
	public double getCurrentPos() {
		return currentPos;
	}
	public void setCurrentPos(double currentPos) {
		this.currentPos = currentPos;
	}
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getThickness() {
		return thickness;
	}
	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	
	/** Chosen range: only used in RangeSelectMenu, which is submenu of main menu. */
	private double[] select_range = null;
	public double[] getSelectRange() {
		return select_range;
	}
	protected void setSelectRange(double[] select_range) {
		this.select_range = select_range;
	}
	
	public void selectRange(double one_bound,
			double station1_ActualWidth, double station2_ActualWidth){
		if(one_bound<beginPos || one_bound>endPos){
			//Ignore any clicks out of bounds.
			return;
		}
		if(select_range == null || select_range.length!=1){
			select_range = new double[]{one_bound};
		}else if(select_range[0] == one_bound){
			select_range = null;
		}else{
			if(select_range[0] > one_bound){
				select_range = new double[]{
						select_range[0]+station1_ActualWidth/2,
						one_bound-station2_ActualWidth/2,
						select_range[0], one_bound};
				//where [0]begin-bound, [1]end-bound, [2]player-start, [3]goal-location
			}else{
				select_range = new double[]{
						select_range[0]-station1_ActualWidth/2,
						one_bound+station2_ActualWidth/2,
						select_range[0], one_bound};
			}
		}
	}
	public boolean isSelectCompleted(){
		if(select_range == null)return false;
		else return select_range.length>=4;/* The options [2],[3] are auto-generated. */
	}
	
	/** Return a point of calculated x, y from the shown distance */
	public Point getPositionCoordinate(double position){
		double deltaPos = (endPos-beginPos);
		double pos;
		if(deltaPos>0) pos = (position-beginPos)*width/deltaPos;
		else pos = (endPos-position)*width/deltaPos;
		return new Point(x+(int)pos, y);
	}
	/** Like a "reverse function" of getPositionCoordinate(double) [NOT Exactly],
	 *  but only used for clicking in the range-select-menu. */
	public double getOriginalPosition(Point p){
		if(p==null)return 0;
		double px = p.x;
		double deltaPos = (endPos-beginPos);
		if(deltaPos>0) return (px-x)*deltaPos/width+beginPos;
		else return (x-px)*deltaPos/width+endPos;
	}
	
	/** Color [private]members and [public]getters/setters */
	private Color color_not_passed = new Color(255, 255, 0);
	private Color color_passed = new Color(204, 191, 153);
	private Color color_inner = new Color(153, 127, 0);
	
	/** Painting station icons */
	public void drawOn(Graphics g){
		if(beginPos == endPos){
			//Do not draw if the two positions are the same.
			return;
		}
		int minPos = (int)getLeftPos();
		int maxPos = (int)getRightPos();
		boolean isRightward = beginPos<endPos;
		
		int rect_x1 = getPositionCoordinate(minPos).x;
		int rect_x2 = getPositionCoordinate(maxPos).x;
		
		//[For range-select-menu use] determine the range (boundaries of selected part).
		double range_x1, range_x2;
		boolean isSelectedToLeft = false;
		if(select_range == null || select_range.length==0){
			range_x1 = beginPos;
			range_x2 = beginPos;
		}else if(select_range.length == 1){
			range_x1 = select_range[0]-10;
			range_x2 = select_range[0]+10;
		}else{
			range_x1 = select_range[0];
			range_x2 = select_range[1];
		}
		if(range_x1 > range_x2){
			//To make sure range_x1 <= range_x2
			double range_tmp = range_x1;
			range_x1 = range_x2;
			range_x2 = range_tmp;
			isSelectedToLeft = true;
		}
		int rect_x_m1 = getPositionCoordinate(range_x1).x;
		int rect_x_m2 = getPositionCoordinate(range_x2).x;
		if(rect_x_m1 < rect_x1){
			rect_x_m1 = rect_x1;
		}
		if(rect_x_m2 > rect_x2){
			rect_x_m2 = rect_x2;
		}
		//End of range determination
		
		//Draw the line
		if(Double.isNaN(currentPos) ||
				currentPos>maxPos && isRightward || currentPos<minPos && !isRightward){
			//The scope is for range-select-menu use to display the line.
			g.setColor(color_passed);
			g.fillRect(rect_x1, y-thickness/2, rect_x_m1-rect_x1, thickness);
			g.setColor(color_not_passed);
			g.fillRect(rect_x_m1, y-thickness/2, rect_x_m2-rect_x_m1, thickness);
			g.setColor(color_passed);
			g.fillRect(rect_x_m2, y-thickness/2, rect_x2-rect_x_m2, thickness);
			
			//TODO TEST
			//System.err.println("{"+rect_x1+", "+rect_x_m1+", "+rect_x_m2+", "+rect_x2+"}");
			
			//Draw the arrow on the line (IN SELECT-MENU)
			drawArrow(g, rect_x_m1, rect_x_m2, isSelectedToLeft);
			
		}else if(currentPos<=maxPos && currentPos>=minPos){
			int rect_x = getPositionCoordinate(currentPos).x;
			g.setColor(isRightward ? color_passed : color_not_passed);
			g.fillRect(rect_x1, y-thickness/2, rect_x-rect_x1, thickness);
			g.setColor(isRightward ? color_not_passed : color_passed);
			g.fillRect(rect_x, y-thickness/2, rect_x2-rect_x, thickness);
			
			//Draw the arrow on the line (IN GAME)
			drawArrow(g, isRightward ? rect_x : rect_x1,
					isRightward ? rect_x2 : rect_x, !isRightward);
			
		}else{
			g.setColor(color_passed);
			g.fillRect(rect_x1, y-thickness/2, rect_x2-rect_x1, thickness);
		}
		
		//Draw the station icons
		for(int i=0; i<station_info.length; i++){
			StationIcon icon = new StationIcon(station_info[i], this);
			Color station_outerColor;
			if(Double.isNaN(currentPos)){
				if(icon.station.x<range_x2 &&
						icon.station.x+icon.station.width>range_x1){
					station_outerColor = color_not_passed;
				}else{
					station_outerColor = color_passed;
				}
			}else if(currentPos<=maxPos && currentPos>=minPos){
				if(!isRightward && icon.station.x>currentPos ||
						isRightward && icon.station.x+icon.station.width>currentPos){
					station_outerColor = (isRightward ? color_not_passed : color_passed);
				}else{
					station_outerColor = (isRightward ? color_passed : color_not_passed);
				}
			}else{
				station_outerColor = color_passed;
			}
			icon.drawOn(g, station_outerColor, color_inner);
		}
	}
	private void drawArrow(Graphics g, int from, int to, boolean isSelectedToLeft){
		int arrowWidth = 10;
		for(int i=from+arrowWidth; i<=to-arrowWidth*3; i+=arrowWidth*2){
			int[] ax, ay;
			int d = arrowWidth, h = thickness/2;//abbriviations
			
			//arrow moving period's parameter t
			int t = (int)((new GregorianCalendar().getTimeInMillis()%500)/500.0*2*d);
			
			if(isSelectedToLeft){
				ax = new int[]{i+2*d-t, i+3*d-t, i+2*d-t, i+3*d-t, i+2*d-t, i+1*d-t};
				ay = new int[]{y-h, y-h, y, y+h, y+h, y};
			}else{
				ax = new int[]{i+0*d+t, i+1*d+t, i+2*d+t, i+1*d+t, i+0*d+t, i+1*d+t};
				ay = new int[]{y-h, y-h, y, y+h, y+h, y};
			}
			g.setColor(color_passed);
			g.fillPolygon(ax, ay, 6);
		}
	}
	
}

