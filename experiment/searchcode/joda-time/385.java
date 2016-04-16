package de.fzi.hiwitool.controllers.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.fzi.hiwitool.db.hibernateImp.tables.contract.Contract;
import de.fzi.hiwitool.util.DateUtil;

public class Test {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		Date b = DateUtil.getDateFromSimpleDateFormat("15.12.2000");
		Date end = DateUtil.getDateFromDateString("14.1.2001");
		List<Date> waMonths = calculateWorkMonth(b, end);
		int hours = 40;
		
		for(int i = 0; i < waMonths.size(); i++){
			
			double freetime;
			// the first month of contract
			if(i == 0){
				Date d = waMonths.get(i);
				String bd = DateUtil.getDayMonthYear(d)[0];
				// contract begins at 15th
				if(bd.equalsIgnoreCase("15")){
					freetime = calculateFreeTime(hours, true);
				}
				// contract begings at 1st but contains only one month
				else if(waMonths.size() == 1){
					String ed = DateUtil.getDayMonthYear(end)[0];
					// contract ends at 14th
					if(ed.equalsIgnoreCase("14")){
						freetime = calculateFreeTime(hours, true);
					}
					// contract ends at end of month
					else{
						freetime = calculateFreeTime(hours, false);
					}
				}
				// contract begins at 1st and contains many month
				else{
					freetime = calculateFreeTime(hours, false);
				}
			}
			// the last month of contract
			else if(i == (waMonths.size()-1)){
				String ed = DateUtil.getDayMonthYear(end)[0];
				// contract ends at 14th
				if(ed.equalsIgnoreCase("14")){
					freetime = calculateFreeTime(hours, true);
				}
				// contract ends at end of month
				else{
					freetime = calculateFreeTime(hours, false);
				}
			}
			// other months of contract
			else{
				freetime = calculateFreeTime(hours, false);
			}
			
			System.out.println(waMonths.get(i)+" free hours: "+freetime);
		}
	}
	
	static public int sortAndGetContract(List<Contract> contracts, Date date){
		
		if(contracts != null){
			// sort contracts according to end day of contracts in descending way
			Collections.sort(contracts, new Comparator<Contract>(){
				public int compare(Contract o1, Contract o2) {
					return o2.getEnd().compareTo(o1.getEnd());
				}
			});
			
			if(date != null){
				// calculate index of the contract
				for (int i = 0; i < contracts.size(); i++) {
					Contract contract = contracts.get(i);
						
					if(date.compareTo(contract.getBegin()) >= 0 && date.compareTo(contract.getEnd()) <= 0){
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	static private double calculateFreeTime(int hours, boolean half){
		double x = (((hours*20)*3.95)/85)/12;
		
		if(!half)
			return Math.round(x * 2) / 2.0;
		else 
			return Math.round(x) / 2.0;
	}
	
	private static int checkDate(List<Contract> contracts, Date c, Date b, Date e){
		// TODO working time > 3 months???
		if(b.after(e))
			return 1;
		
		int conIndex = sortAndGetContract(contracts, c);
		for(int i = conIndex; i >= 0; i--){
			Contract con = contracts.get(i);
			if(i == 0){
				if(b.before(con.getEnd()))
					return 2;
			}
			else{
				Contract con2 = contracts.get(i-1);
				if(b.before(con.getEnd()))
					return 2;
				else if(b.before(con2.getBegin())){
					if(e.after(con2.getBegin()))
						return 3;
					else
						return 0;
				}
				else
					continue;
			}
		}
		return 0;
	}
	
	protected static List<Date> calculateWorkMonth(Date begin, Date end){
		
		int bm = Integer.valueOf(DateUtil.getDayMonthYear(begin)[1]);
		int em = Integer.valueOf(DateUtil.getDayMonthYear(end)[1]);
		int by = Integer.valueOf(DateUtil.getDayMonthYear(begin)[2]);
		int ey = Integer.valueOf(DateUtil.getDayMonthYear(end)[2]);
		
		List<Date> ret = new ArrayList<Date>();
		ret.add(begin);
		if((++bm) == 13){
			bm = 1;
			by++;
		}
		
		if(by <= ey){
			try {
				while(by < ey){
					for(; bm < 13; bm++){
						Date d = DateUtil.getDateFromSimpleDateFormat("01."+DateUtil.months[bm-1]+"."+by);
						ret.add(d);
					}
					bm = 1;
					by++;
				}
				
				while(bm <= em){
					Date d = DateUtil.getDateFromSimpleDateFormat("01."+DateUtil.months[bm-1]+"."+by);
					ret.add(d);
					bm++;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
}

