package sim;

import java.util.Arrays;

public class Tag {
public int[] ts;
public Tag(int[] ts) {
@Override
public String toString() {
String s = &quot;&quot;;
for(int i =0;i<ts.length;i++)
{
s+=ts[i];

