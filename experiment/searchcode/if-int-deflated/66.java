public DeflatedChunkReader(int clen, String chunkid, boolean checkCrc, long offsetInPng,
DeflatedChunksSet iDatSet) {
this.deflatedChunksSet = iDatSet;
if (chunkid.equals(PngChunkFDAT.ID)) {
skipBytes = true;
skippedBytes = new byte[4];

