Map.Entry<Object, Double> entry = (Map.Entry)iter.next();
Object key = entry.getKey();
double v = getValue(key);
if (v <= min)
{
min = v;
Object key = entry.getKey();
double v = getValue(key);
if (v <= min)
{
min = v;
obj = entry.getKey();

