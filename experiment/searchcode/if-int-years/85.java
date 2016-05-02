package org.itx.jbalance.l0;

import java.util.Date;


public class AgeCalculator {
		
	private AgeCalculator(){}
	
	
	
	public static Age calculateAge(Date from, Date to) {
		if(from == null || to == null){
			return null;
		}
		
		if(to.before(from)){
			return new Age(0,0,0);
		}

		
		int years, months, days;
		
		years = to.getYear() 	-	from.getYear();
		months= to.getMonth() 	-	from.getMonth();
		
		if (from.getDate() >  to.getDate())
			months --;
		
		if (months < 0) {
			months += 12;
			years--;
		}
		
		if(from.getDate() > to.getDate()){
			days  = to.getDate();

			days  +=  daysInMonth(from.getYear()+1900,from.getMonth() == 0 ? 11: from.getMonth()-1) - from.getDate();
		} else {
			days  = to.getDate() - from.getDate();
		}
		
		return new Age(years,months,days);
	}
	
	
	public static int daysInMonth(int year, int month){
        boolean leapYear =  (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
        int res=0;
        switch(month){
        case 0:	 res=31;
        case 1:  res= leapYear ?29: 28;
        case 2:  res=31;
        case 3:  res=30;
        case 4:  res=31;
        case 5:  res=30;
        case 6:  res=31;
        case 7:  res=31;
        case 8:  res=30;
        case 9:  res=31;
        case 10: res=30;
        case 11: res=31;
        
        }
        return res;
    }
	
	
	
//	public static Age calculateAge(Date from, Date to) {
//		if(from == null || to == null){
//			return null;
//		}
//		
//		if(to.before(from)){
//			return new Age(0,0,0);
//		}
//		
//		Calendar cFrom = Calendar.getInstance();
//		Calendar cTo   = Calendar.getInstance();
//		
//		cFrom.setTime(from);
//		cTo  .setTime(to);
//		
//		int years, months, days;
//		
//		years = cTo.get(Calendar.YEAR) 			- cFrom.get(Calendar.YEAR);
//		months= cTo.get(Calendar.MONTH) 		- cFrom.get(Calendar.MONTH);
//		
//		if (cFrom.get(Calendar.DAY_OF_MONTH) > cTo.get(Calendar.DAY_OF_MONTH))
//			months --;
//		
//		if (months < 0) {
//			months += 12;
//			years--;
//		}
//		
//		if(cFrom.get(Calendar.DAY_OF_MONTH) > cTo.get(Calendar.DAY_OF_MONTH)){
//			days  = cTo.get(Calendar.DAY_OF_MONTH);
//			cTo.add(Calendar.MONTH, -1);
//			days  +=  cFrom.getMaximum(Calendar.DAY_OF_MONTH) - cFrom.get(Calendar.DAY_OF_MONTH);
//		} else {
//			days  = cTo.get(Calendar.DAY_OF_MONTH) - cFrom.get(Calendar.DAY_OF_MONTH);
//		}
//		
//		return new Age(years,months,days);
//	}
	

}	
	
	

