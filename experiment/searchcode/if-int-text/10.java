import java.util.*;

public class Coder{

public static int[] prefix_(String text){

int[] prefix = new int[text.length()];
int len = text.length();
int k = 0;

for(int i=1;i<len;i++){

