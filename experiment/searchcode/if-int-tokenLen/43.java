private int discardedColumnNumber,discardedLineNumber;
private int advance,tokenlen;
private int tokenoffset=-1;

/**
* Creates a location tracker.
* @see fr.umlv.tatoo.runtime.buffer.LexerBuffer#unwind(int)
*/
public void bufferUnwind(int count) {
tokenlen+=count;

