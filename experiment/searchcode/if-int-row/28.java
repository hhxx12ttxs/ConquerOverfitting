public static void main(String[] args) {

System.out.println(findElement(2, 1));

}

public static int findElement(int row, int column) {
if(row < column) {return 0;}
if(row == 0 || column == 0) {return 1;}
int[][] x = new int[row+1][row+1];

