import us.bridgeses.minder.options.CustomPattern;
import us.bridgeses.minder.options.Option;
import us.bridgeses.minder.provider.OptionsContract;
expandableListView.setAdapter(new SimpleExpandableReminderAdapter(options));
if (options != null &amp;&amp; !options.isClosed()) {
options.close();

