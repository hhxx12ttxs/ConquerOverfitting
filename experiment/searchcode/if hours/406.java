
public class TotalWages
{
  public double totalWages(double hours, double rate)
  {
    double wages;
    if (hours > 40)
    {
      wages = ((40 * rate) + (hours - 40) * 1.5 * rate);
    }
    else
      wages = hours * rate;
    return wages;
  }

}
