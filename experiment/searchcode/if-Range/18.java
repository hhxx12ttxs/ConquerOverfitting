return range;
}

public static int[][] split(int[] tr, int[] r){
int[][] ranges = new int[2][2];

if(Range.start(r) > Range.start(tr) &amp;&amp; Range.start(r) < Range.end(tr) &amp;&amp; Range.end(r) > Range.start(tr) &amp;&amp; Range.end(r) < Range.end(tr)){ // r completamente contenido en this
ranges[0]  = Range.newRange(Range.end(r), Range.end(tr));
} else if(Range.start(r) < Range.end(tr) &amp;&amp; Range.start(r) > Range.start(tr)){ // r esta a la derecha

