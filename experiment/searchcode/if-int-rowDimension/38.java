public FastMatrixParameter(String id, int rowDimension, int colDimension) {
super(id);
singleParameter = new Parameter.Default(rowDimension * colDimension);
public Parameter getParameter(int index) {
if (proxyList == null) {
proxyList = new ArrayList<ParameterProxy>(colDimension);

