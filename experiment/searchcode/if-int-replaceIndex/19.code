public abstract class AbstractTokenFilterReader extends FilterReader {

private String replacement = null;

private int replaceIndex = -1;

public AbstractTokenFilterReader(Reader in) {
super(in);
}

@Override
public int read() throws IOException {

if (replaceIndex != -1) {

