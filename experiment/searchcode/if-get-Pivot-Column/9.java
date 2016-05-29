String tmp_pivot_value=this.PivotTable[i][j].split(&quot; &quot;)[0];
if(tryParseFloat(tmp_pivot_value)){
if(highlight.get(0).semanticValue.contains(tmp_pivot_value)) colortable[i][j]=highlight.get(0).mincolor;
else if(highlight.get(1).semanticValue.contains(tmp_pivot_value)) colortable[i][j]=highlight.get(1).maxcolor;

