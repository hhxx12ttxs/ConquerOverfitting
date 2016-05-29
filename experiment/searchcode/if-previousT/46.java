return monday;
}


public static Date previoustMonday(Date thisMonday, int nrOfWeeks) {
//System.out.println(&quot;Entering previousMonday&quot;);
Calendar now = Calendar.getInstance();

List<String> mondays = new ArrayList<String>();

Date currentMonday = previoustMonday(thisMonday(), numberOfWeeks/2);

