Runtime rt = Runtime.getRuntime();
printMemory(rt);
try {
// Read the sequence file
long tend = System.currentTimeMillis();
System.out.println(&quot;Reading file &quot; + (tend-tstart) + &quot;ms&quot;);
tstart = System.currentTimeMillis();

