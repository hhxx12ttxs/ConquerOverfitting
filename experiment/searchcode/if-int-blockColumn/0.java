int blockRow=blockPosition.row-num/4;
int blockColumn=blockPosition.column+4-num%4;
if (blockRow<0) return true;
if ( (gameLayer.view[blockRow] &amp; ( 1<<(blockColumn) ) )!=0) return true;
int blockRow=blockPosition.row-num/4;
int blockColumn=blockPosition.column+4-num%4;
if (blockColumn==0) return false;
if (blockRow<0) return false;

