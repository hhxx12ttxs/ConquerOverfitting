//            int iHeight = this.blockHeight(iBlock);
//
//            for (int jBlock = 0; jBlock < this.blockColumns; ++index) {
//                if (blockData[index].length != iHeight * this.blockWidth(jBlock)) {
return blockRow == this.blockRows - 1 ? this.rows - blockRow * BLOCK_SIZE : BLOCK_SIZE;
}

private int blockWidth(int blockColumn) {

