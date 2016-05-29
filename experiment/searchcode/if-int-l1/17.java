

import java.util.*;
import java.io.*;

public class Lab {

public int[][][] l1;

public Lab() throws IOException, FileNotFoundException {
FileReader fr = new FileReader(&quot;lab_1.txt&quot;);
Scanner wczytaj = new Scanner(fr);
l1 = new int[3][232][6];
int i;

