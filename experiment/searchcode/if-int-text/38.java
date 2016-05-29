package SRM147;

public class CCipher {

public String decode(String cipherText, int shift) {
int intValue = chars[i] - shift;
if (intValue < A) {
intValue = intValue + 26;
}
cipherText += (char) intValue;

