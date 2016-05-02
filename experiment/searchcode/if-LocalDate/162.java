package org.grill.fatwhacker.data;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.grill.fatwhacker.FatWhacker;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import sun.tools.tree.ThisExpression;

public class WeekSet implements Iterable<FoodWeek>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<FoodWeek> weeks = new ArrayList<FoodWeek>();


	public FoodWeek getFoodWeek(LocalDate date) {
		int dayOfWeek = date.getDayOfWeek();
		if (dayOfWeek != FatWhacker.weekStart)
			date = date.plusDays( -1*((7+dayOfWeek - FatWhacker.weekStart)%7));
		
		for (FoodWeek week : weeks) {
			if (week.getStartDay().equals(date))
				return week;
		}
		
		FoodWeek week = new FoodWeek(date);
		weeks.add(week);
		Collections.sort(weeks, new Comparator<FoodWeek>() {
		
			public int compare(FoodWeek o1, FoodWeek o2) {
				return o1.getStartDay().compareTo(o2.getStartDay());
			}
		
		});
		return week;
	}
	
	public static void main(String[] args) {
		LocalDate date = new LocalDate();
		
		WeekSet set = new WeekSet();
		set.getFoodWeek(date);
		date = date.plusWeeks(1);
		set.getFoodWeek(date);
		date = date.plusDays( 4);
		set.getFoodWeek(date);
		set.dump();
	}
	
	public void dump() {
		for (FoodWeek week : weeks) {
			System.out.println("Week: " + week.getStartDay());
			for (DailyRecord day : week.getDays()) {
				System.out.println("\tDay: " + day.getDate());
			}
		}
	}

	public Iterator<FoodWeek> iterator() {
		return weeks.iterator();
	}

	public void initialize() {
		LocalDate localDate = new LocalDate();
		getFoodWeek(localDate.plusWeeks(-1));
		getFoodWeek(localDate);
		getFoodWeek(localDate.plusWeeks(1));
	}

	public void save() {
		save("data.fwbin");
		save("data_" + new LocalDateTime().toString() + ".fwbin");
	}

	private void save(String string) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(FatWhacker.getDataFolder() + "/" + string);
			ObjectOutputStream encoder = new ObjectOutputStream(fos);
			encoder.writeObject(this);
			encoder.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static WeekSet restore() {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(FatWhacker.getDataFolder() + "/data.fwbin");
			return (WeekSet)new ObjectInputStream(fis).readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
}

