Double x = Math.pow(N, M);
return fib(x.longValue());
}

public Long fib(Long x){

Long result = map.get(x);

if(result != null) {
return result;
}

if(x < 0){
result = 0L;
} else if(x == 0){

