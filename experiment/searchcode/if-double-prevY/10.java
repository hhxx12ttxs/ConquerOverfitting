if (prev.isKing() &amp;&amp; prev.isFire()){
if (prevX +2 ==x &amp;&amp; prevY +2 ==y &amp;&amp; pieceAt(prevX +1,prevY +1) != null &amp;&amp; !pieceAt(prevX +1,prevY +1).isFire()){
return true;
} else if (prevX -2 ==x &amp;&amp; prevY +2 ==y &amp;&amp; pieceAt(prevX -1,prevY +1) != null &amp;&amp; !pieceAt(prevX -1,prevY +1).isFire()){

