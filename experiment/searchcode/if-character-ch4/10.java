for (int i = 0; i < password.length(); i++) {

char c = password.charAt(i);

if (Character.isDigit(c)) {
numbers += 1;
} else if (Character.isUpperCase(c)) {
capitalLetter += 1;

