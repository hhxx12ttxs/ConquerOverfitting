private int blockRow = 0;
private int blockColumn = 0;
private int splits;

@Override
public int run(String[] args) throws Exception {
while(blockColumn < splits &amp;&amp; size-- > 0) {
set.addBlock(blockRow, blockColumn++);
}
if (size == 0) break;
blockColumn = 0;
blockRow++;
}
return set;
}

}

