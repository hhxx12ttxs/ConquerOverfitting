package morriscm.ch4;

import javax.swing.JOptionPane;

public class _12Validate2 {

public static void main(String[] args) {
for(int i = 0; i < input.length(); i++) {
char c = input.charAt(i);
if(Character.isDigit(c) == false) {
return false;
}
}
return true;
}
}

