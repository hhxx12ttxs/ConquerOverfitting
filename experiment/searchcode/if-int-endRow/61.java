int startColumn=start.getNumber2();
int  endRow=end.getNumber1();
int  endColumn=end.getNumber2();

if(ChessBoard.getFigure(endRow,endColumn)!=null &amp;&amp; ChessBoard.getFigure(endRow,endColumn).getColor()==this.getColor() )
System.out.println(&quot;figure changed his position from [&quot;+row+&quot;,&quot;+column+&quot;] to [&quot;+endRow+&quot;,&quot;+endColumn+&quot;]&quot;);
ChessBoard.resetFigure(row,column);
return true;
}
else if (Math.abs(startRow-endRow)==1 &amp;&amp; Math.abs(startColumn-endColumn)==1)

