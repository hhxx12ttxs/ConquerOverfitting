deflatedBytes[bufferEnd++] = 0; // Flags (FLG)
writeInt(0); // Modification time (MTIME)
deflatedBytes[bufferEnd++] = 0; // Extra flags (XFL)
// Obtain more data from the input stream.
int byteCount = inputStream.read(buffer, 0, buffer.length);
if (byteCount > 0) {

