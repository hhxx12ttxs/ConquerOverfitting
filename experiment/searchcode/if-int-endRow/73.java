public List<Integer> spiralOrder(int[][] matrix) {

List<Integer> result = new ArrayList();

if(matrix == null || matrix.length == 0 || matrix[0].length == 0){
return result;
}

int beginRow = 0;
int endRow = matrix.length - 1;
int beginCol = 0;

