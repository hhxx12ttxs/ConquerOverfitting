public void stepBegin(String descr)
{
if (stepRunning)
stepEnd();

final int       stepId  = step;
final String    message = &quot;<b>&quot; + stepId + &quot;</b>: &quot; + descr;
public void taskEnd(final String comment)
{
if (stepRunning)
stepEnd();

SwingUtilities.invokeLater(new Runnable()

