package customer.retail;

public class CustomerType {

private int years;

public void withUsFrom(int years) {
this.years = years;
}



public String getCustomerType(double money) {
if(years>=10 &amp;&amp; money >=2000){

