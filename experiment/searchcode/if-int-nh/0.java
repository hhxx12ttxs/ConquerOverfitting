public final static String  [] ruleName = {&quot;AND&quot;, &quot;OR&quot;, &quot;XOR&quot;, &quot;NOT&quot; , &quot;INV&quot;, &quot;MIN&quot;, &quot;MAX&quot; , &quot;SET&quot;, &quot;INC&quot;, &quot;DEC&quot;, &quot;SWAP&quot; , &quot;ROR&quot;, &quot;ROL&quot;, &quot;ROU&quot;, &quot;ROD&quot;, &quot;NOP&quot;};
protected byte [][] rules;
protected int nOS; //Number of states
protected int [] nH = new int [5]; // Neighborhood
public IBARuleSett(int nOS){
this.nOS = nOS;
}
public int preformRules(int[] bs){
this.nH = bs;
//		printNH();

