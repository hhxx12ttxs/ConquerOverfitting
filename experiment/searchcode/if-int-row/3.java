return rmin;
}
public void set_Rmin_Rmax(int row)
{
if(row > 0 &amp;&amp; row < 9)
{
rmin = row-1;
public void check_First_Row(int row)
{
if(row == 0)
{
rmin = row;
rmax = row + 1;
}
}
public void check_Last_Row(int row)

