

import java.util.*;
import java.io.*;

public class Lab{

public int[][][] l1;

public Lab() throws IOException,FileNotFoundException{
Scanner wczytaj = new Scanner(fr);
l1 = new int[3][232][6];
int i;

for(i=0;i<232;i++)
{
l1[0][i][0] = wczytaj.nextInt();

