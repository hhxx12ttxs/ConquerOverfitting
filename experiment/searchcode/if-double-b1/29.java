public class SimpleMatchingSimilartiy implements BooleanSimilarityMeasure
{
@Override
public Double similarity(Boolean[] b1, Boolean[] b2)
{
if (b1.length == 0 || b1.length != b2.length)
len++;
if (b1[i] == b2[i])
eq++;
}
if (len == 0)
return null;
double d = eq / (double) len;

