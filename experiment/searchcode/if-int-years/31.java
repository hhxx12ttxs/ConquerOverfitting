private static final int YEARS_SERVICE = 10;


public Lawyer(String name, int caseNums) {
super(name);
this.setCaseNums(caseNums);
System.out.println(this.getName() + &quot; takes notes &quot;);

}


@Override
public void setYearsInService(int years) {
if(years >= YEARS_SERVICE){

