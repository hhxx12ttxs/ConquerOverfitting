package view.components;

import helpers.CalendarHelper;
import helpers.Settings;
import interfaces.Drawable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import model.TaskModel;

import org.joda.time.LocalDate;

import resource.theme.ThemeResources;

public class DayCard implements Drawable{

	private int posX;
	private int posY;
	private double width;
	private double height;
	private String weekDay;
	private LocalDate date;
	
	private int newEventCardPosX;	
	private int newEventCardPosY;
	private int newEventCardWidth;
	private EventCard newEventCard;
	
	public boolean startedToCreateANewEventCard = false;
	public boolean addedTask = false;

	//hold the task that belongs to this day card
	private ArrayList<EventCard> tasks;

	int myDay;
	//public WeekCard weekCard;

	/**
	 * 
	 * @param posX, x-coordinate for the DayCards top-left corner 
	 * @param posY, y-coordinate for the DayCards top-left corner
	 * @param width, the DayCards width
	 * @param height, the DayCards height
	 */

	public DayCard(int posX, int posY, double width, double height, String weekDay,ArrayList<TaskModel> list, LocalDate date,int day){

		this.posX = posX;
		this.posY = posY;
		//this.width = width;
		this.newEventCardWidth = (int)this.width;
		this.height = Settings.getDayCardHeight();
		this.width = Settings.getDayCardWidth();
		
		//this.height = height;
		this.weekDay = weekDay;		
		this.date = date;

		//int myDay = Integer.parseInt(getDayCardDay()); 
		myDay = day;
		tasks = new ArrayList<EventCard> (5);	
		if(list!=null) {
			Iterator<TaskModel> iter = list.iterator();
			while(iter.hasNext()){
				TaskModel tmpTask = iter.next();
				if(myDay == tmpTask.getDay()){
					//System.out.println("There is a task in day card with date:"+date.getDayOfWeek());

					EventCard tmpEventCard = new EventCard(this.posX,this.posY,(int)this.width,100,tmpTask.getIdNumber());
					tmpEventCard.setCategory(tmpTask.getCategory());
					tmpEventCard.setPriority(tmpTask.getPriority());
					tmpEventCard.setContent(tmpTask.getContentTask());
					tmpEventCard.setDay(tmpTask.getDay());
					tmpEventCard.setLocation(tmpTask.getLocation());
					tmpEventCard.setMonth(Integer.parseInt(tmpTask.getMonth()));
					tmpEventCard.setTitle(tmpTask.getTitle());
					tmpEventCard.setWeek(tmpTask.getWeekNum());
					tmpEventCard.setYear(tmpTask.getYear());

					tasks.add(tmpEventCard);
				}
			}
		}


	}
	//denna skall hlla koll p vilka eventcards som tillhr detta daycard, behver inte
	//veta om vilken weekcard/vecka den tillhr
	//

	@Override
	public void draw(Graphics g) {
		Graphics2D gTemp = (Graphics2D)g.create();
		//give it some colours
		gTemp.setColor(ThemeResources.getDayCardColor());

		this.height = Settings.getDayCardHeight();
		this.width = Settings.getDayCardWidth();
		this.posY = Settings.getDayCardYpos();
		
		if(weekDay.equalsIgnoreCase("Tuesday")) {			
			posX = Settings.getDayCardWidth()+Settings.getPaddingBetweenDayCards()+Settings.MONDAY_POS_X;			
		} else if(weekDay.equalsIgnoreCase("Wednesday")) {			
			posX = Settings.getDayCardWidth()*2+Settings.getPaddingBetweenDayCards()+2+Settings.MONDAY_POS_X;			
		} else if(weekDay.equalsIgnoreCase("Thursday")) {			
			posX = Settings.getDayCardWidth()*3+Settings.getPaddingBetweenDayCards()+4+Settings.MONDAY_POS_X;			
		} else if(weekDay.equalsIgnoreCase("Friday")) {			
			posX = Settings.getDayCardWidth()*4+Settings.getPaddingBetweenDayCards()+6+Settings.MONDAY_POS_X;			
		} else if(weekDay.equalsIgnoreCase("Saturday")) {			
			posX = Settings.getDayCardWidth()*5+Settings.getPaddingBetweenDayCards()+8+Settings.MONDAY_POS_X;			
		} else if(weekDay.equalsIgnoreCase("Sunday")) {			
			posX = Settings.getDayCardWidth()*6+Settings.getPaddingBetweenDayCards()+10+Settings.MONDAY_POS_X;			
		}
		
		 //(int)dayCardWidth*2+padding+4)
	
		
		//To remove the jagged corners of the EventCard
		RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		renderHints.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		gTemp.setRenderingHints(renderHints);

		gTemp.fillRoundRect(posX, posY, (int) width, (int) height, 20, 20); 
		//gTemp.setColor(Color.BLACK);
		//gTemp.draw3DRect(posX, posY, width, height, false);

		int lineDraw = posY + 40;
		//int timeLine = posY + 60;
		int timeLine = WeekCard.timeLine-5;
		int nrOfPixelsPerHour = WeekCard.nrOfPixelsPerHour; //(int) ((height - 60) / 24);
		//int lineIterator = (int) ((height+20)/27);//add 20 to height cause I want to count on weekCards height
		int timeHalfLine = timeLine + nrOfPixelsPerHour/2;
		
		int weekDayYpos = lineDraw - 10;

		//gTemp.drawLine(posX, lineDraw, (int) (posX+width), lineDraw);
		gTemp.setColor(Color.BLACK);

		//every whole hour
		for(int i = timeLine; i<height+posY; i=i+nrOfPixelsPerHour) {
			gTemp.drawLine(posX, i, (int) (posX+width), i);
		}

		gTemp.setColor(Color.GRAY);
		//Every half hour
		for(int i = timeHalfLine; i<height+posY; i=i+nrOfPixelsPerHour) {
			gTemp.drawLine(posX, i, (int) (posX+width), i);
		}

		gTemp.setColor(Color.BLACK);
		
		
		
		if(weekDay.equalsIgnoreCase("Monday")) {
			myDay = CalendarHelper.monday;
		} else if(weekDay.equalsIgnoreCase("Tuesday")) {
			myDay = CalendarHelper.tuesday;
		} else if(weekDay.equalsIgnoreCase("Wednesday")) {
			myDay = CalendarHelper.wednesday;
		} else if(weekDay.equalsIgnoreCase("Thursday")) {
			myDay = CalendarHelper.thursday;
		} else if(weekDay.equalsIgnoreCase("Friday")) {
			myDay = CalendarHelper.friday;
		} else if(weekDay.equalsIgnoreCase("Saturday")) {
			myDay = CalendarHelper.saturday;
		} else if(weekDay.equalsIgnoreCase("Sunday")) {
			myDay = CalendarHelper.sunday;
		}
		
		gTemp.drawString(weekDay+"   " +myDay, (int) (posX+(width*0.15)), weekDayYpos);
		/*Iterator<EventCard> iter = tasks.iterator();
		while(iter.hasNext()){
			EventCard tmp = iter.next();
			tmp.draw(gTemp);
		}*/
		gTemp.dispose();

	}


	//private get

	public String getDayCardDay() {
		String stringDate = date.toString();
		String[] tokens = stringDate.split("-");

		return tokens[2];

	}

	public LocalDate getDate() {
		return this.date;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}



	public ArrayList<EventCard> getEventCards() {
		return tasks;
	}

	public void setEventCards(ArrayList<EventCard> tasks) {
		this.tasks = tasks;
	}

	
	public void createTask() {
		if(!addedTask) {
			tasks.add(newEventCard);
			addedTask = true;
		}
	}
/*
	public void mouseDragged(MouseEvent event) {
		
		//System.out.println("startedToCreateANewEventCard:"+startedToCreateANewEventCard);
		
		if(startedToCreateANewEventCard) {	
			
			//System.out.println("size tasklist:"+tasks.size());
			Runnable createEventcard = new Runnable() { 
				public void run() {
					createTask();	
				} 
			};   
			SwingUtilities.invokeLater(createEventcard);
			
			
			newEventCard.changeHeight(event.getY() - newEventCard.getHeight() -newEventCard.getPosY());
			//System.out.println("startedToCreateANewEventCard: height:"+newEventCard.getHeight());
			//System.out.println("width:"+newEventCard.getWidth());
			//System.out.println("posX: "+newEventCard.getPosX());
			//System.out.println("posY: "+newEventCard.getPosX());
			//newEventCardHeight += (event.getY() - newEventCardHeight -newEventCardPosY);
		} else {
			if(tasks.size() > 0 ){
				Iterator<EventCard> iter = tasks.iterator();
				while(iter.hasNext()) {
					EventCard tmpEventCard = iter.next();
					tmpEventCard.mouseDragged(event);
				}
			} 
		}
	}

	public void mouseReleased(MouseEvent event) {
		
		startedToCreateANewEventCard = false;
		addedTask = false;
		//System.out.println("dasfsdaf");
		if(tasks.size() > 0 ){
			Iterator<EventCard> iter = tasks.iterator();
			while(iter.hasNext()) {
				EventCard tmpEventCard = iter.next();
				tmpEventCard.mouseReleased(event);
			}
		} 
	}

	public void mouseMoved(MouseEvent event) {
		if(tasks.size() > 0 ){
			Iterator<EventCard> iter = tasks.iterator();
			while(iter.hasNext()) {
				EventCard tmpEventCard = iter.next();
				tmpEventCard.mouseMoved(event);
			}
		} 
	}*/

	public boolean isInsideDayCard(int mouseX, int mouseY) {
		boolean res = false;
		if(mouseX >= posX && mouseX <= (posX+width-1) && mouseY >= posY && mouseY <= (posY+height-1)) {
			res = true;
		}
		return res;
	}

	public String getWeekDay() {
		return weekDay;
	}

	public void setMyDay(int myDay) {
		this.myDay = myDay;
	}

	
	
	//TODO
	//Br gra varje daycard bredare CHECK
	//Br gra ett streck mellan varje heltimme fr enkelhetens skull (fr anvndaren)CHECK
	//Fixa s att jag kan hmta alla datum fr varje enskild dag

	//fixa en animering (easter egg)
	//fixa scrollpanel


}

