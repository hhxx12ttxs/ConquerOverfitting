public boolean legalMove (int startRow, int startColumn, int desRow, int desColumn, int[][] playerMatrix)
{

boolean axis = true;

if (startRow == desRow ^ startColumn == desColumn) //XOR If ONE of these conditions match (if both true or false then false is returned)

