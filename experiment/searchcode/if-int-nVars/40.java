public static int[][] MatrixToGraph(int[][] matrix){
int nVars = matrix[0][0];
int nClausulas = matrix[0][1];
//linhas dos subgrafos com 2 vertices, e a partir de nVars*2+1 até nVars*4 vao ser as linhas dos subgrafos de
//3 vertices.

int[][] graph = new int[nVars*2 + nClausulas*3][nVars*2 + nClausulas*3];

