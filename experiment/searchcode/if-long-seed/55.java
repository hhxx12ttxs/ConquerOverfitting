public String getSeedString()
{
return seed;
}
public long getSeed()
{
long lSeed = 0;

if (seed.length() != 0)
{
try
{
long l1 = Long.parseLong(seed);

if (l1 != 0L)

