class A1C02T08 {
public static void main(String[] a){
System.out.println(new A().max(5, 3));
}
}

class A {
public int max(int a, int b) {
int max;
max = 0;
if (a < b)
max = b;
else
max = a;
return max;
}
}

