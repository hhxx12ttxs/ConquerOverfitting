millisInterval -= (days*DateUtils.DAY_IN_MILLIS);

if(millisInterval > DateUtils.HOUR_IN_MILLIS)
{
hours = (int) (millisInterval / DateUtils.HOUR_IN_MILLIS);
millisInterval -= (hours*DateUtils.HOUR_IN_MILLIS);

if(millisInterval > DateUtils.MINUTE_IN_MILLIS)
{
minutes = (int) (millisInterval / DateUtils.MINUTE_IN_MILLIS);

