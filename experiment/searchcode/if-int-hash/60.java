package hash;

public class HashFunction {
public static int simpleHash(String key, int tablesize){
int hashVal = 0;

//把字符串中字符的ASCII码(或Unicode码)值加起来
for(int i=0; i<key.length(); i++){

