package test;

public class NWD {
public int NWD(int a, int b){
if(a <= 0 || b <= 0){
throw new IllegalArgumentException();
}

int c;

while(b != 0){
c = a % b;
a = b;
b = c;
}

return a;
}
}

