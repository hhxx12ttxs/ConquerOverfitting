package com.topcoder;

public class Time {

public static String whatTime(int seconds)
{
String str=&quot;&quot;;
if(seconds>=3600)
{	str+=seconds/3600+&quot;:&quot;;
seconds=seconds%3600;
}
else
str+=0+&quot;:&quot;;

