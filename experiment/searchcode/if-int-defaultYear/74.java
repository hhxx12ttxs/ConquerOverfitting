public static void initWheelDatePicker(final Activity activity,final TextView mText,final AbWheelView mWheelViewY,final AbWheelView mWheelViewM,final AbWheelView mWheelViewD,
Button okBtn,Button cancelBtn,
int defaultYear,int defaultMonth,int defaultDay,final int startYear,int endYearOffset,boolean initStart){
int month = calendar.get(Calendar.MONTH)+1;
int day = calendar.get(Calendar.DATE);

if(initStart){
defaultYear = year;

