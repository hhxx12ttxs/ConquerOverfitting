private TextField keyorder;
@FXML
private ComboBox<Double> hispeed;

private static final String[] PLAYSIDE = { &quot;POP&quot;, &quot;BM&quot; };
protected void drawNote(GraphicsContext gc, double x, double y,
double width, double height, double scale, int lane, Note note) {
if (note instanceof NormalNote) {

