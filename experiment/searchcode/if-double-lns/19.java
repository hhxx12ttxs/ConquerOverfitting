public class DBSCAN
{
double MinLns, Eps;
ArrayList<TrajSegment> tSegments;
public DBSCAN(ArrayList<TrajSegment> tsegments, double eps, double minlns)
{
Eps = eps;
MinLns = minlns;
tSegments = tsegments;

