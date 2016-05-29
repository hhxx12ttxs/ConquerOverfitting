public class Square
{
public int col;
public int row;

Square(String command)
{
this.row = Integer.parseInt(command.substring(1, 2)) - 1;
Square(int col, int row)
{
if (col < Constants.MIN_COLUMN || col > Constants.MAX_COLUMN || row < Constants.MIN_ROW || row > Constants.MAX_ROW) {

