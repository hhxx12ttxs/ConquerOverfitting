int[] compressedBlock = new int[(MAX_POSSIBLE_COMPRESSED_BITSIZE>>>5)];
int[] expPos = new int[_batchSize];
private int compressExpPosBasic(int[] compBlock, int offset, int[] expPos, int expSize)
{
// hy: currently no compression for pos
if(offset%32 !=0)

