public char shiftChar(char ch){
int int_version = (int) ch;
int new_intversion = int_version + shift_amount;

char new_char = (char) new_intversion;

return new_char;


}
public char shiftCharBack(char ch){
int int_version = (int) ch;

