matrixHashTable = new Hashtable<Integer, Integer>();
}

public int rowDimension(){
return rows;
}

public int columnDimension(){
return cols;
public Integer getWithHash(int row_index, int col_index){
if(row_index > -1 &amp;&amp; col_index > -1 &amp;&amp; row_index < rowDimension()

