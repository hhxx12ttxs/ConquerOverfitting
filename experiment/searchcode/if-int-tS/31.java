public String hash;
public int imsi;
public int cellac;
public long ts;

public PLS(String h, int i, int c, long t){
this.hash = h;
this.imsi = i;
this.cellac = c;
this.ts = t;
}

public int compareTo(PLS o) {

