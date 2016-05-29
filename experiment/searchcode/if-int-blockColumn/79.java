public class PageRankPrep {

private long blockSize;
private long numNodes;

public PageRankPrep(int blockSize, long numNodes) {
this.blockSize = blockSize;
this.numNodes = numNodes;
}

public int run(String matrixInput, String normalizedMatrixOutput, String danglingNodeVectorOutput, String initialRankVectorOutput) throws Exception {

