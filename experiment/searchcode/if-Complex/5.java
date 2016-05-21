        return complex;
    }
@Aspect
public class ComplexCachingAspect {
    private Map<String, Complex> cache;
    public void setCache(Map<String, Complex> cache) {
        this.cache = Collections.synchronizedMap(cache);
    @Around(\"call(public Complex.new(int, int)) && args(a,b)\")
    public Object cacheAround(ProceedingJoinPoint joinPoint, int a, int b)
        Complex complex = cache.get(key);
        if (complex == null) {
            System.out.println(\"Cache MISS for (\" + key + \")\");
        }

