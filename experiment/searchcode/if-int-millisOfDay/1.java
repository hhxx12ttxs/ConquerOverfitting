public class TimePreference extends Preference {

private int millisOfDay;
private String summary;

public TimePreference(Context context, AttributeSet attrs) {
public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
if (restoreValue) {
int noon = new DateTime().startOfDay().withHourOfDay(12).getMillisOfDay();

