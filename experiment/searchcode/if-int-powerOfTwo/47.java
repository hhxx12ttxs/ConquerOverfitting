//solution 1: 一直除2，直到不能被2整除， 若商为1，则是2的整数次幂，否则不是。
/*    public boolean isPowerOfTwo(int n) {
if(n < 1) return false;
while(n % 2 == 0){
PowerOfTwo pot = new PowerOfTwo();
for(int i = -100; i < 10000; i ++){
if(pot.isPowerOfTwo(i))
System.out.println(i + &quot; is power of two. &quot;);
}
}
}

