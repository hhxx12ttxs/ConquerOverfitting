package il.ac.shenkar.mapmarker;

import java.util.ArrayList;

public class MarkerArray
{
private static MarkerArray array;
private MarkerArray()
{
markersArray = new ArrayList<Marker>();
}

public static MarkerArray getInstance()
{
if (array == null)

