public String formatEstimate(long bytes)
{
String result;

double Gb = bytes / (double) ONE_GIGABYTE;
double Mb = bytes / (double) ONE_MEGABYTE;
double Kb = bytes / (double) ONE_KILOBYTE;
// Size in Giga-bytes.
if (Gb >= 1)
{
result = String.format(&quot;%.2f GB&quot;, Gb);

