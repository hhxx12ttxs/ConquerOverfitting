double p2_x, double p2_y, double p3_x, double p3_y)
{
double s1_x, s1_y, s2_x, s2_y;
s2_x = p3_x - p2_x;     s2_y = p3_y - p2_y;

double s, t;
s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);

