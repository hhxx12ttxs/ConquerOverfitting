public abstract class OnNoMultiClickListener implements View.OnClickListener {

private long prevMillis;
@Override
public void onClick(View v) {
long thisMillis = System.currentTimeMillis();
if (thisMillis - prevMillis >= duration) {

