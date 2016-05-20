package jp.ne.voqn.calcurator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <div>
 * <p>
 * like function of Python "range()".<br/>
 * <code>&gt&gt&gt range(10)</code> <br/>
 * returns <code>[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]</code> <br/>
 * (not contains <code>10</code>)<br/>
 * </p>
 * this class method "range" returns a Iterable Object
 * </div>
 *
 * <h3>USASE</h3>
 * <pre>
 * import static jp.ne.voqn.labs.Range.*;
 *
 * public class AnimalSounds{
 *      public static void main(String[] args){
 *          String[] animals = { "cat", "dog", "big" };
 *          String[] actionExp = {"is mewing", "is barking", "is saying"};
 *          String[] sounds = {"\"meow\"", "\"bowwow\"", "\"oink, oink\""};
 *          for(int i : range(animals.length){
 *              System.out.println(animals[i] + " " + actionExp[i] + " " + sounds[i] + ".");
 *          }
 *      }
 * }
 * </pre>
 *
 * @author VoQn
 */
public abstract class Range implements Iterable<Integer> {

    private static final String STEP_ARGUMENT_ERROR_MSG;

    static{
        STEP_ARGUMENT_ERROR_MSG = "range() step argument must not be 0";
    }

    private final int start, stop, step;
    

    private Range(int start, int stop, int step) {
        if(step == 0){
            throw new IllegalArgumentException(STEP_ARGUMENT_ERROR_MSG);
        }
        this.stop = stop;
        this.step = step;
        this.start = start;
    }

    private static int autoSign(final int start, final int stop){
        return stop < start ? -1 : 1;
    }

    public static Range of(int stop) {
        return new Range(0, stop, autoSign(0, stop)){};
    }

    public static Range of(int start, int stop) {
        return new Range(start, stop, autoSign(start, stop)){};
    }

    public static Range of(int start, int stop, int step) {
        return new Range(start, stop, step){};
    }

    @Override
    public Iterator<Integer> iterator() {

        return new Iterator<Integer>() {
            private int current = start;

            @Override
            public boolean hasNext() {
                return step < 0 ? current > stop : current < stop;
            }

            @Override
            public Integer next() {
                final int value = current;
                current += step;
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public List<Integer> list(){
        List<Integer> list = new ArrayList<Integer>();
        for(Integer i : this){
            list.add(i);
        }
        return list;
    }

    @Override
    public String toString(){
        return this.list().toString();
    }

    public static <E> Range of(E[] elements){
        return of(elements.length);
    }

    public static <E> Range of(Collection<E> collection){
        return of(collection.size());
    }

    public static Range of(byte[] nums){
        return of(nums.length);
    }

    public static Range of(short[] nums){
        return of(nums.length);
    }

    public static Range of(int[] nums){
        return of(nums.length);
    }

    public static Range of(long[] nums){
        return of(nums.length);
    }

    public static Range of(float[] nums){
        return of(nums.length);
    }

    public static Range of(double[] nums){
        return of(nums.length);
    }

    public static Range of(boolean[] nums){
        return of(nums.length);
    }

    public static Range of(char[] nums){
        return of(nums.length);
    }
}

