package fractals.util;
public class MiscUtils
{
public static String formatTime(int millis)
int secs = millis%60;
millis/=60;
int mins = millis%60;
millis/=60;
int hours = millis;
if(hours > 0)

