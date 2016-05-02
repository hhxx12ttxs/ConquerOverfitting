import java.util.ArrayList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import DopeConnection.DopeDBException;
import DopeConnection.DopeResultSetException;
import DopeConnection.OracleFormats;

/**
 * This class calculates the statistics of the data obtained from the objects 
 * the class purpose is to prepare the data for the graph & table
 * @author MRCR
 *
 */
public class Statistics {
	private ListContainer lc = new ListContainer();
	private Provider p;
	public boolean name;
	private ObjectToHoldList last = new ObjectToHoldList();
	/**
	 * This overload of the constructor establish a connection to the database.
	 * This ensures that the application only have 1 instance of the database.
	 * @author MRCR
	 * 
	 */
	public Statistics(){
		try {
			this.p = new Provider();
		} catch (DopeDBException e) {
			e.printStackTrace();
			Main.createErrorMessage("Kunne ikke oprette forbindelse til Databasen! \r\n Kontakt venligst DOPE ansvarlig - Error Code 113Stat");
		}
	}
	public String getNotInDays(DateTime start, DateTime end){
		ArrayList<DateTime> days = p.getOpeningDaysInSelectedPeriod(start, end);
		String notInDays = "";
		DateTime d = new DateTime("2013-01-01");
		start = start.withTimeAtStartOfDay();
		notInDays = notInDays + OracleFormats.convertDate(d.toDate());
		int k  = Days.daysBetween(start, end).getDays();
		for (int i = 0; i <= k; i++) {
			if (!days.contains(start)) {
				notInDays = notInDays +","+ OracleFormats.convertDate(start.toDate());
			}else {
			}
			start = start.plusDays(1);
		}
		return notInDays;
	}
	/**
	 * This method returns the calculated DataList list.
	 * @author MRCR
	 * @param name 
	 * @return averageData
	 */
	public ArrayList<Double>getCallAmountData(DateTime startDate, DateTime endDate, String name, boolean withWeekend){
		QuaterCalculator qc = new QuaterCalculator(0,false);
		qc.clearAll();
		lc.clearAllLists();
		try {
			qc.setTimeFrame(getDaysBetween(startDate, endDate));
			if (getDaysBetween(startDate, endDate) > 1 && getDaysBetween(startDate, endDate) < 9) {
				if (withWeekend) {
					qc.setNumberOfWeekends(startDate, endDate);
				}
				qc.selectTimeFrameView(qc.UGE);
			}else if (getDaysBetween(startDate, endDate) > 10) {
				if (withWeekend) {
					qc.setNumberOfWeekends(startDate, endDate);	
				}
				qc.selectTimeFrameView(qc.MONTH);
			}else {
				qc.selectTimeFrameView(qc.DAY);
			}
			if (!withWeekend) { // false
				getViewWithoutWeekend(name, startDate, endDate,qc);
				return lc.getAverageData();
			}else {
				ArrayList<Integer> tempArray = 	p.phoneQueueWithWeekends(startDate, endDate, name,qc, getNotInDays(startDate, endDate));
				ArrayList<Double> tempTwo = new ArrayList<Double>();
				for (Integer integer : tempArray) {
					tempTwo.add((double)integer);
				}
				lc.setAverageList(tempTwo);
				return lc.getAverageData();
			}
		} catch (DopeDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DopeResultSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lc.setAverageList(lc.getAverageData());
		return lc.getAverageData();
	}
	public ArrayList<Double> getCallPercentageData(DateTime startDate, DateTime endDate, String name, boolean withWeekend, int firstOrSecond){
		QuaterCalculator qc = new QuaterCalculator(0,true);
		qc.clearAll();
		qc.setTimeFrame(getDaysBetween(startDate, endDate));
		if (getDaysBetween(startDate, endDate) > 1 && getDaysBetween(startDate, endDate) < 9) {
			if (withWeekend) {
				qc.setNumberOfWeekends(startDate, endDate);
			}
			qc.selectTimeFrameView(qc.UGE);
		}else if (getDaysBetween(startDate, endDate) > 10) {
			if (withWeekend) {
				qc.setNumberOfWeekends(startDate, endDate);	
			}
			qc.selectTimeFrameView(qc.MONTH);
		}else {
			qc.selectTimeFrameView(qc.DAY);
		}
		try {
			if (withWeekend) {
				ArrayList<Integer> tempArray = 	p.phoneQueueWithWeekends(startDate, endDate, name,qc, getNotInDays(startDate, endDate));
				calculateProcentage(tempArray);
				lc.setBesvaret25Seklist(tempArray);
				return lc.getPercentageList();
			}else {
				getViewWithoutWeekendPercentage(name, startDate, endDate, qc);
				calculateProcentage2(lc.getPercentageList());
				return lc.getPercentageList();
			}
		} catch (DopeDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DopeResultSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lc.getPercentageList();
	}
	private void calculateProcentage(ArrayList<Integer> percentageList) {		
		ArrayList<Double> averageDouble = new ArrayList<Double>();
		for (int i = 0; i < lc.getAverageData().size(); i++) {
			Double averageToAdd = 0.0;
			averageToAdd = percentageList.get(i)/ lc.getAverageData().get(i);
			averageToAdd = averageToAdd * 100;
			//Ret nĺr fejlen i databasen er rettet!!!!
			if (averageToAdd > 100) {
				averageDouble.add(100.0);
			}else {
				averageDouble.add(averageToAdd);

			}
		}
		lc.setPercentage(averageDouble);
	}
	private void calculateProcentage2(ArrayList<Double> percentageList) {		
		ArrayList<Double> averageDouble = new ArrayList<Double>();
		for (int i = 0; i < lc.getAverageData().size(); i++) {
			Double averageToAdd = 0.0;
			averageToAdd = percentageList.get(i)/ lc.getAverageData().get(i);
			averageToAdd = averageToAdd * 100;
			//Ret nĺr fejlen i databasen er rettet!!!!
			if (averageToAdd > 100) {
				averageDouble.add(100.0);
			}else {
				averageDouble.add(averageToAdd);

			}
		}
		lc.setPercentage(averageDouble);
	}
	/**
	 * This method checks how many weekends the month contains and makes the program able to deselect them.
	 * @param name
	 * @param startDate2
	 * @param endDate2
	 * @param qc 
	 */
	private void getViewWithoutWeekend(String name, DateTime startDate2, DateTime endDate2, QuaterCalculator qc) {
		ArrayList<String>notInweekends = new ArrayList<>();
		String notInWeekendsSQL = "";
		DateTime startDate = new DateTime(startDate2);
		DateTime endDate = new DateTime(endDate2);
		int numberOfWeekends = 0;
		while (startDate.getDayOfMonth() != endDate.getDayOfMonth()) {
			startDate = startDate.plusDays(1);
			if (startDate.getDayOfWeek() == DateTimeConstants.SATURDAY || startDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				notInweekends.add(OracleFormats.convertDate(startDate.toDate()));
				numberOfWeekends++;
			}
		}
		qc.setTimeFrame(getDaysBetween(startDate2, endDate2)-numberOfWeekends);
		qc.setNumberOfWeekends(0);
		if (!notInweekends.isEmpty()) {
			notInWeekendsSQL = notInweekends.get(0);	
			for (int i = 1; i < notInweekends.size(); i++) {
				notInWeekendsSQL = notInWeekendsSQL + ", " + notInweekends.get(i);
			}
			try {
				ArrayList<Integer> que =p.phoneQueueWithoutWeekends(startDate2, endDate2, name,notInWeekendsSQL,qc, getNotInDays(startDate2, endDate2));
				ArrayList<Double> tempArray = new ArrayList<Double>();
				for (Integer integer : que) {
					tempArray.add((double)integer);
				}
				lc.setAverageList(tempArray);
			} catch (DopeDBException e) {
				e.printStackTrace();
			} catch (DopeResultSetException e) {
				e.printStackTrace();
			}
		}else {
			//	System.out.println(getDaysBetween(startDate2, endDate2)-numberOfWeekends);
			ArrayList<Integer> que;
			try {
				que = p.phoneQueueWithWeekends(startDate2, endDate2, name, qc, getNotInDays(startDate2, endDate2));
				ArrayList<Double> tempArray = new ArrayList<Double>();
				for (Integer integer : que) {
					tempArray.add((double)integer);
				}
				lc.setAverageList(tempArray);
			} catch (DopeDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DopeResultSetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	private void getViewWithoutWeekendPercentage(String name, DateTime startDate2, DateTime endDate2, QuaterCalculator qc) {
		ArrayList<String>notInweekends = new ArrayList<>();
		String notInWeekendsSQL = "";
		DateTime startDate = new DateTime(startDate2);
		DateTime endDate = new DateTime(endDate2);
		int numberOfWeekends = 0;
		while (startDate.getDayOfMonth() != endDate.getDayOfMonth()) {
			startDate = startDate.plusDays(1);
			if (startDate.getDayOfWeek() == DateTimeConstants.SATURDAY || startDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				notInweekends.add(OracleFormats.convertDate(startDate.toDate()));
				numberOfWeekends++;
			}
		}
		qc.setTimeFrame(getDaysBetween(startDate2, endDate2)-numberOfWeekends);
		qc.setNumberOfWeekends(0);
		if (!notInweekends.isEmpty()) {
			notInWeekendsSQL = notInweekends.get(0);	
			for (int i = 1; i < notInweekends.size(); i++) {
				notInWeekendsSQL = notInWeekendsSQL + ", " + notInweekends.get(i);
			}
			try {
				ArrayList<Integer> que =p.phoneQueueWithoutWeekends(startDate2, endDate2, name,notInWeekendsSQL,qc, getNotInDays(startDate2, endDate2));
				ArrayList<Double> tempArray = new ArrayList<Double>();
				lc.setBesvaret25Seklist(que);
				for (Integer integer : que) {
					tempArray.add((double)integer);
				}
				lc.setPercentage(tempArray);
			} catch (DopeDBException e) {
				e.printStackTrace();
			} catch (DopeResultSetException e) {
				e.printStackTrace();
			}
		}else {
			//	System.out.println(getDaysBetween(startDate2, endDate2)-numberOfWeekends);

			ArrayList<Integer> que;
			try {
				que = p.phoneQueueWithWeekends(startDate2, endDate2, name, qc, getNotInDays(startDate2, endDate2));
				lc.setBesvaret25Seklist(que);
				ArrayList<Double> tempArray = new ArrayList<Double>();
				for (Integer integer : que) {
					tempArray.add((double)integer);
				}
				lc.setPercentage(tempArray);
			} catch (DopeDBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DopeResultSetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * returns the days between to Joda DateTimes
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private int getDaysBetween(DateTime startDate, DateTime endDate){
		ArrayList<DateTime> days = p.getOpeningDaysInSelectedPeriod(startDate, endDate);
		int k = Days.daysBetween(startDate, endDate).getDays();
		startDate = startDate.withTimeAtStartOfDay();
		int daysBetween =0;
		for (int i = 0; i <= k; i++) {	
			if (!days.contains(startDate)) {
				daysBetween--;
			}else {
				daysBetween++;
			}
			startDate = startDate.plusDays(1);
		}
		return  daysBetween;
	}

	/** 
	 * Stores the last known data
	 * @param data
	 */
	public void setLastData(ArrayList<Double> data){
		this.last.setArray(data);
	}
	public ArrayList<Double> getAkkumuleretSvarprocent() {
		ArrayList<Double> akkumuleretSvarprocent = new ArrayList<Double>();
		ArrayList<Double> list1 = new ArrayList<Double>();
		ArrayList<Double> list2 = new ArrayList<Double>();
		double value1 = 0;
		for (int i = 0; i < lc.getAverageData().size(); i++) {
			if (lc.getAverageData().get(i) == 0.0) {
				value1 =0;
			}else {
				value1 = lc.getAverageData().get(i) + value1;	
			}
			list1.add(value1);	
		}
		double value2 = 0;
		for (int i = 0; i < lc.getBesvaret25Seklist().size(); i++) {
			if (lc.getBesvaret25Seklist().get(i) == 0.0) {
				value2 =0;
			}else {
				value2 = (double)lc.getBesvaret25Seklist().get(i)+value2;
			}

			list2.add(value2);
		}

		for (int i = 0; i < list1.size(); i++) {
			double finalValue = list2.get(i) / list1.get(i);
			finalValue = finalValue*100;
			akkumuleretSvarprocent.add(finalValue);
		}

		return akkumuleretSvarprocent;
	}

	public ArrayList<Double> getForecast(DateTime start, DateTime end, boolean withweekend){
		QuaterCalculator qc = new QuaterCalculator(0, false);
		qc.clearAll();
		qc.setTimeFrame(getDaysBetween(start, end));

		if (getDaysBetween(start, end) > 1 && getDaysBetween(start, end) < 9) {
			if (withweekend) {
				qc.setNumberOfWeekends(start, end);
			}
			qc.selectTimeFrameView(qc.UGE);
		}else if (getDaysBetween(start, end) > 10) {
			if (withweekend) {
				qc.setNumberOfWeekends(start, end);	
			}
			qc.selectTimeFrameView(qc.MONTH);
		}else {

			qc.selectTimeFrameView(qc.DAY);
		}
		if (!withweekend) {
			return p.getPrognose(start,end, qc,getForeCastWithoutWeekend(start,end,qc));
		}else {
			return p.getPrognoseWithWeekend(start, end, qc);			
		}


	}
	private String getForeCastWithoutWeekend(DateTime start, DateTime end, QuaterCalculator qc) {
		ArrayList<String>notInweekends = new ArrayList<>();
		String notInWeekendsSQL = "";
		DateTime startDate = new DateTime(start);
		DateTime endDate = new DateTime(end);
		int numberOfWeekends = 0;
		while (startDate.getDayOfMonth() != endDate.getDayOfMonth()) {
			startDate = startDate.plusDays(1);
			if (startDate.getDayOfWeek() == DateTimeConstants.SATURDAY || startDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				notInweekends.add(OracleFormats.convertDate(startDate.toDate()));
				numberOfWeekends++;
			}
		}
		qc.setTimeFrame(getDaysBetween(start, end)-numberOfWeekends);
		qc.setNumberOfWeekends(0);
		if (!notInweekends.isEmpty()) {
			notInWeekendsSQL = notInweekends.get(0);	
			for (int i = 1; i < notInweekends.size(); i++) {
				notInWeekendsSQL = notInWeekendsSQL + ", " + notInweekends.get(i);
			}

		}
		return notInWeekendsSQL;
	}}

