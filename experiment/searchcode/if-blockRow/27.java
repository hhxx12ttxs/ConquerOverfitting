public final BufferedImage getNextImageBlock() throws IOException {
if (mImageSegment.getActualBitsPerPixelPerBand() != 1) {
throw new IOException(&quot;Unhandled bilevel image depth:&quot; + mImageSegment.getActualBitsPerPixelPerBand());
readEOL();
if (lineMode2D) {
readScanline2D(blockRow);
} else {

