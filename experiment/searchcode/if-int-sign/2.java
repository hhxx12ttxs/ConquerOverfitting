int a = Integer.parseInt(args[0]);
int b = Integer.parseInt(args[1]);
int c = a-b;
int sign_a = sign(a);
int sign_b = sign(b);
System.out.println(res+&quot;&quot;);
}

public static int sign(int a){
return flip((a >> 31) &amp; 0x01); // return 1 if a is positive return 0 if a is negative

