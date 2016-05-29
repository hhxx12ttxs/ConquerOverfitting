public long FibEven( int n ){
if( n  <= 1 ) return 0;
if( n  == 2 ) return 2;
//sum(Fi) = Fn+2 -1
if( k  == 1 ) return new Tuple(1, 2, 2);
if( k  == 2 ) return new Tuple(2, 3, 3);

long fib = 2, fib_prev = 1, tmp;

