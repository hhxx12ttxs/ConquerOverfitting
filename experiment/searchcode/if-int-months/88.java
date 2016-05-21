months.set(m + (ver.nMajor < 5 ? 1 : 0), ObjectFactory.createInteger(1));
}
{
int[] arWeeks = {RepeatRule.FIRST, RepeatRule.SECOND, RepeatRule.THIRD, RepeatRule.FOURTH, RepeatRule.FIFTH};
int nWeeks = rl.getInt(RepeatRule.WEEK_IN_MONTH); 
{
if ( months.get(m).toInt() != 0 )
nMonths |= arMonths[m]; 
//THIS IS BUG in BB < 5.0 : return month -1. January just crash get events
RepeatRule.AUGUST, RepeatRule.SEPTEMBER, RepeatRule.OCTOBER, RepeatRule.NOVEMBER, RepeatRule.DECEMBER};
int nMonths = rl.getInt(RepeatRule.MONTH_IN_YEAR); 
RubyArray months = ObjectFactory.createArray(arMonths.length, ObjectFactory.createInteger(0));
for ( int m = 0; m < arMonths.length; m++ )
{
if ( (nMonths&arMonths[m]) != 0 )

