/** Index into replacement data */
private int replaceIndex = -1;

/** Index into queue data */
private int queueIndex = -1;
public final int read() throws IOException {

if (replaceIndex != -1) {
final int ch = replaceData.charAt(replaceIndex++);

