public class Idx_Dlt_Get extends UDF
{
public Double evaluate(Double todayIdx, Double yesterdayIdx, Double N2)
{
try
{
if ((yesterdayIdx == null) &amp;&amp; (todayIdx == null))
return N2;
if (yesterdayIdx == null)

