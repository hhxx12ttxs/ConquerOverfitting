* otherwise it returns a random move
*/
public static int chooseColumn(char[][] board, char cpu){
int ret;

if(empty(board)) return 3;
ret = blockColumn(board, cpu);

if(ret != -1) return ret;
else return new Random().nextInt(7);

