this.suduko = suduko;
}

public boolean recursionSearch () {
int rowIndex = startPoint().rowIndex;
int columnIndex = startPoint().columnIndex;

if (rowIndex >= 8 &amp;&amp; columnIndex >= 8 &amp;&amp; suduko[rowIndex][columnIndex] != 0) {

