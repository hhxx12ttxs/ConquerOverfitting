public void setWeek(Date week) {
this.week = week;

if(todoPlanBean!=null &amp;&amp; todoPlanBean.getWeekPlans()!=null &amp;&amp; todoPlanBean.getWeekPlans().containsKey(week)) {
personDetailBean = new PersonDetailBean();
}
*/

public void setPersonName(String name) {
if(personDetailBean==null) {

