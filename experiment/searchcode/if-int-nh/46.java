private static final int DEFAULT_COMPRESSION = 6;
private final NhFileWriter nhFileWriter;
private Map<String, NVariable> variables;
public void addDimension(String name, int length) throws IOException {
try {
nhFileWriter.getRootGroup().addDimension(name, length);

