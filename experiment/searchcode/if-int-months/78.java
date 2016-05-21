    (235 * ((year - 1) / 19))           // Months in complete cycles so far.
    + (7 * ((year - 1) % 19) + 1) / 19; // Leap months this cycle
  int PartsElapsed = 204 + 793 * (MonthsElapsed % 1080);
  int HoursElapsed =
  {
    + PartsElapsed / 1080;
  int ConjunctionDay = 1 + 29 * MonthsElapsed + HoursElapsed / 24;
  int ConjunctionParts = 1080 * (HoursElapsed % 24) + PartsElapsed % 1080;
  for (int m = month - 1;  m > 0; m--) // days in prior months this year
    int DayInYear = day; // Days so far this month.
    if (month < 7) { // Before Tishri, so add days in prior months
                     // this year before and after Nisan.
  int N = day;           // days this month
  
  int MonthsElapsed =

