int columns = getColumns();
rowHeights = new int[rowCount];
rowStartY = new int[rowCount];

if(rowCount < 1)
rowStartY[row + 1] = rowStartY[row] + height;
}
}
}

@Override
protected int getHeightForRow(int row)
{
if(row >= 0 &amp;&amp; row < rowHeights.length)

