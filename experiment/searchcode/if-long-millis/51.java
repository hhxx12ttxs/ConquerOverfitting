
public class Duration implements Comparable<Duration>
{
public long duration_in_millis=0;
public int compareTo(Duration that)
{
if(this.duration_in_millis>that.duration_in_millis)
{
return 1;
}
else if(this.duration_in_millis<that.duration_in_millis)

