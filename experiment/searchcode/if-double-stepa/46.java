//    public static StatefullMDP staticMdp;
public static double ALPHA = 0.5;
public static double GAMMA = 0.99;
private AbstractEdge nextEdge = null;
public void beforeSimulation() {

if (StatefullMDP.staticMdp == null) {
Set<String> vertices = graph.vertexSet();

