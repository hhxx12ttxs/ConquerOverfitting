package com.humaorie.dollar;

/*
 * Dollar, http://bitbucket.org/dfa/dollar
 * (c) 2010, 2011 Davide Angelocola <davide.angelocola@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.humaorie.dollar.hamcrest.InOrder.*;
import static com.humaorie.dollar.hamcrest.Only.*;

// SUT deps
import static com.humaorie.dollar.Dollar.*;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class DollarTest {

    @Test
    public void forBoxed() {
        Integer x = 0;

        for (Integer i : $(10)) {
            assertThat(i, is(x++));
        }
    }

    @Test
    public void forUnboxed() {
        int x = 0;

        for (int i : $(10)) {
            assertThat(i, is(x++));
        }
    }

    @Test
    public void forEachString() {
        assertThat($("cccc"), only('c'));
    }

    @Test
    public void intList() {
        int x = 0;

        for (int i : $(5).toList()) {
            assertThat(i, is(x++));
        }
    }

    @Test
    public void buildMap() {
        Map<String, Integer> map = $(new HashMap<String, Integer>()).
                threadSafe().
                add("foo", 10).
                add("bar", 20).
                toMap();
        assertThat(map, notNullValue());
    }

    @Test
    public void toList() {
        List<Integer> list = $(10, 100, 10).toList();
        assertThat(list, notNullValue());
        assertThat(list.size(), is(9));
        assertThat(list.get(0), is(10));
        assertThat(list.get(1), is(20));
    }

    static class CustomList<T> extends AbstractList<T> {

        @Override
        public T get(int index) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void toCustomList() {
        $(10, 100, 10).toList(CustomList.class);
    }

    @Test
    public void stringToList() {
        assertThat($("hello world!").sort().join(), is(" !dehllloorw"));
    }

    @Test
    public void toSet() {
        Set<Integer> set = $(10, 100, 5).toSet();
        assertThat(set, notNullValue());
        assertThat(set.size(), is(18));
    }

    static class CustomSet<T> extends AbstractSet<T> {

        @Override
        public Iterator<T> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void toCustomSet() {
        $(10, 100, 5).toSet(CustomSet.class);
    }

    @Test
    public void stringToSet() { // eliminate the double 'l' and double 'o'
        assertThat($("hello world!").toSet().toString(), is("[w, d,  , !, e, r, o, l, h]"));
    }

    @Test
    public void sortStrings() {
        String[] array = new String[]{"4", "2", "1", "3"};
        assertThat($(array).sort(), inOrder("1", "2", "3", "4"));
    }

    // Q: how to quickly generate a string of 10 '-'?
    @Test
    public void stringFiller() {
        assertThat($($(10).join()).fill('-').join(), is("----------"));
    }

    // Q: ugly... could you improve it?
    @Test
    public void improvedStringFiller() {
        assertThat($("-").repeat(10).join(), is("----------"));
    }

    @Test
    public void join() {
        List<Integer> list = Arrays.asList(10, 20, 30);
        assertThat(list, hasItems(10, 20, 30));
    }

    @Test
    public void joinArray() {
        String[] ary = {"1", "2", "3"};
        assertThat($(ary).join("-"), is("1-2-3"));
    }

    @Test
    public void joinWithArrow() {
        assertThat($(5).reverse().join("->"), is("4->3->2->1->0"));
    }

    // TODO: join(null) is a little bit weird
    @Test
    public void joinWithNull() {
        assertThat($(2).join(null), is("0null1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotConvertAListOfStringToPrimitiveArray() {
        List<String> list = Arrays.asList("hello", "world");
        $(list).convert();
    }

    // http://stackoverflow.com/questions/2131997
    private boolean arePermutationsOfSameString(String s1, String s2) {
        s1 = $(s1).sort().join();
        s2 = $(s2).sort().join();
        return s1.equals(s2);
    }

    @Test
    public void stringPermutationCheck() {
        // true cases
        assertThat(arePermutationsOfSameString("abc", "acb"), is(true));
        assertThat(arePermutationsOfSameString("bac", "bca"), is(true));
        assertThat(arePermutationsOfSameString("cab", "cba"), is(true));

        // false cases
        assertThat(arePermutationsOfSameString("cab", "acba"), is(false));
        assertThat(arePermutationsOfSameString("cab", "acbb"), is(false));

        // corner cases
        assertThat(arePermutationsOfSameString("", ""), is(true));
        assertThat(arePermutationsOfSameString("", null), is(true));
        assertThat(arePermutationsOfSameString(null, ""), is(true));
        assertThat(arePermutationsOfSameString(null, null), is(true));
    }

    // http://stackoverflow.com/questions/1979767
    // http://stackoverflow.com/questions/754294
    @Test
    public void longPrimitiveArrayToList() {
        long[] longArray = {42L};
        List<Long> longList = $(longArray).toList();
        assertThat(longList.get(0), is(42L));
    }

    // http://stackoverflow.com/questions/1738068
    @Test
    public void concatCollection() {
        int sum = 0;
        // [0, 1, 2, 3, 4, -5, -4, -3, -2, -1]
        for (int i : $(new LinkedList<Integer>()).addAll($(5)).addAll($(-5))) {
            sum += i;
        }

        assertThat(sum, is(-5));
    }

    // http://stackoverflow.com/questions/2176459
    @Test
    public void concatIterables() {
        int sum = 0;

        for (int i : $(10).concat($(10))) {
            sum += i;
        }

        assertThat(sum, is(90));
    }

    // http://stackoverflow.com/questions/1235179
    @Test
    public void repeatString() {
        String string = "abc";
        assertThat($(string).repeat(3).join(""), is("abcabcabc"));
    }

    @Test
    public void repeatStringWithSpaces() {
        String string = "abc";
        assertThat($(string).repeat(3).join(" "), is("abc abc abc"));
    }

    // http://stackoverflow.com/questions/1900197
    @Test
    public void generatePermutations() {
        String digits = $('0', '9').join();

        for (int i : $(10)) {
            String p = $(digits).shuffle().slice(4).join();
            assertThat(p.length(), is(4));
        }
    }

    // http://stackoverflow.com/questions/2478275/
    @Test
    public void generateRandomString() {
        String string = $('a', 'z').shuffle().slice(3).join()
                + $('0', '9').shuffle().slice(3).join();
        assertThat(string.length(), is(6));
    }

    // http://stackoverflow.com/questions/1549090
    @Test
    public void arrayToString() {
        String[] array = new String[3];
        array[0] = "Ap";
        array[1] = "p";
        array[2] = "le";
        assertThat($(array).join(), is("Apple"));
    }
    // http://stackoverflow.com/questions/41107
    final static String validCharacters = $('0', '9').join() + $('A', 'Z').join();

    private static String randomString(int length) {
        return $(validCharacters).shuffle().slice(length).toString();
    }

    @Test
    public void randomStrings() {
        for (int i : $(5)) {
            assertThat(randomString(12).length(), is(12));
        }
    }

    // http://stackoverflow.com/questions/2626835/
    @Test
    public void randomString() {
        String randomString = $('a', 'z').repeat(10).shuffle().slice(6).join();
        assertThat(randomString.length(), is(6));
    }

    // http://stackoverflow.com/questions/718554
    @Test
    public void convertListToPrimitiveArray() {
        List<Integer> list = $(5).toList();
        int[] array = $(list).convert().toIntArray();
        int[] expected = new int[]{0, 1, 2, 3, 4};
        assertArrayEquals(expected, array);
    }

    // http://stackoverflow.com/questions/1802915
    @Test
    public void filler() {
        assertThat($("=").repeat(10).join(), is("=========="));
    }

    // TODO: we are still weak in this test
    // http://stackoverflow.com/questions/2145853
    @Test
    public void treeMapSortedByValue() {
        Map<Integer, Double> map = $(new TreeMap<Integer, Double>()).
                add(1, Math.PI).
                add(2, Math.E).
                add(3, 42.0).
                toMap();

        Set<Map.Entry<Integer, Double>> sortedEntries = new TreeSet<Map.Entry<Integer, Double>>(new DoubleComparator());
        sortedEntries.addAll(map.entrySet());

        List<Double> doubles = new LinkedList<Double>();

        for (Map.Entry<Integer, Double> entry : sortedEntries) {
            doubles.add(entry.getValue());
        }
    }

    static class DoubleComparator implements Comparator<Map.Entry<Integer, Double>> {

        @Override
        public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
            return Double.compare(o1.getValue(), o2.getValue());
        }
    }

    // http://stackoverflow.com/questions/3176222
    @Test
    public void listOfByteToPrimitiveArray() {
        List<Byte> list = Arrays.asList((byte) 42, (byte) 84);
        byte[] array = $(list).convert().toByteArray();
        assertThat(array, not(nullValue()));
        assertThat(array.length, is(2));
        assertThat(array[0], is((byte) 42));
        assertThat(array[1], is((byte) 84));
    }

    // wiki examples
    @Test
    public void primitiveArraySortRevertToList() {
        int[] array = new int[]{-42, 1, 4, 2, -5};
        List<Integer> list = $(array).sort().reverse().toList();
        assertThat(list, inOrder(4, 2, 1, -5, -42));
    }

    // random
    @Test
    public void randomFloats() {
        float max = 10.0F;
        List<Float> randomFloats = $(new Random(), max).samples(100).toList();

        for (Float x : randomFloats) {
            assertThat(x < max, is(true));
        }
    }

    @Test
    public void randomDoubles() {
        double max = 10.0;
        List<Double> randomDoubles = $(new Random(), max).samples(5).toList();

        for (Double x : randomDoubles) {
            assertThat(x < max, is(true));
        }
    }

    @Test
    public void randomIntegers() {
        int max = 10;
        List<Integer> randomIntegers = $(new Random(), max).samples(5).toList();

        for (Integer x : randomIntegers) {
            assertThat(x < max, is(true));
        }
    }

    @Test
    public void sortedRandomIntegers() {
        int max = 100;
        List<Integer> randomIntegers = $(new Random(), max).samples(50).sort().toList();
        int prev = -1;

        for (int x : randomIntegers) {
            assertThat(x < max, is(true));
            assertThat(x >= prev, is(true));
            prev = x;
        }
    }

    @Test
    public void reversedSortedRandomIntegers() {
        int max = 100;
        List<Integer> randomIntegers = $(new Random(), max).samples(50).sort().reverse().toList();
        int prev = max;

        for (int x : randomIntegers) {
            assertThat(x < max, is(true));
            assertThat(x <= prev, is(true));
            prev = x;
        }
    }

    @Test
    public void randomLongs() {
        long max = 10L;
        List<Long> randomLongs = $(new Random(), 10L).samples(5).toList();

        for (Long x : randomLongs) {
            assertThat(x < max, is(true));
        }
    }

    @Test
    public void randomBooleans() {
        List<Boolean> randomBooleans = $(new Random()).samples(5).toList();
        assertThat(randomBooleans.size(), is(5));
    }

    @Test
    public void mapToString() {
        Wrapper<Integer> integers = $(10, 15);
        Wrapper<String> strings = integers.map(new Dollar.Function<String, Integer>() {

            @Override
            public String call(Integer object) {
                return object.toString();
            }
        });

        assertThat(strings.size(), is(integers.size()));
        assertThat(strings, hasItems("10", "11", "12", "13", "14"));
    }

    @Test
    public void filterEven() {
        Wrapper<Integer> integers = $(10, 15);
        Wrapper<Integer> evens = integers.filter(new Dollar.Function<Boolean, Integer>() {

            @Override
            public Boolean call(Integer object) {
                return object % 2 == 0;
            }
        });

        assertThat(evens, hasItems(10, 12, 14));
        assertThat(evens, not(hasItems(11, 13)));
    }

    // instanceof tests; for backward compatibility
    @Test
    public void charSequenceWrapper() {
        assertThat($(""), is(CharSequenceWrapper.class));
        assertThat($(new StringBuilder()), is(CharSequenceWrapper.class));
        assertThat($(new StringBuffer()), is(CharSequenceWrapper.class));
    }

    @Test
    public void range1Wrapper() {
        assertThat($(1), is(RangeWrapper.class));
    }

    @Test
    public void range2Wrapper() {
        assertThat($(1, 2), is(RangeWrapper.class));
    }

    @Test
    public void range3Wrapper() {
        assertThat($(1, 10, 2), is(RangeWrapper.class));
    }

    @Test
    public void charRangeWrapper() {
        assertThat($('a', 'z'), is(CharRangeWrapper.class));
    }

    @Test
    public void randomBooleanWrapper() {
        assertThat($(new Random()), is(RandomBooleanWrapper.class));
    }

    @Test
    public void randomIntegerWrapper() {
        assertThat($(new Random(), 10), is(RandomIntegerWrapper.class));
    }

    @Test
    public void randomLongWrapper() {
        assertThat($(new Random(), 10L), is(RandomLongWrapper.class));
    }

    @Test
    public void randomFloatWrapper() {
        assertThat($(new Random(), 10.0F), is(RandomFloatWrapper.class));
    }

    @Test
    public void randomDoubleWrapper() {
        assertThat($(new Random(), 10.0), is(RandomDoubleWrapper.class));
    }

    @Test
    public void arrayWrappers() {
        assertThat($(new boolean[0]), is(ArrayWrapper.class));
        assertThat($(new char[0]), is(ArrayWrapper.class));
        assertThat($(new byte[0]), is(ArrayWrapper.class));
        assertThat($(new short[0]), is(ArrayWrapper.class));
        assertThat($(new int[0]), is(ArrayWrapper.class));
        assertThat($(new long[0]), is(ArrayWrapper.class));
        assertThat($(new float[0]), is(ArrayWrapper.class));
        assertThat($(new double[0]), is(ArrayWrapper.class));
        assertThat($(new Object[0]), is(ArrayWrapper.class));
        assertThat($(new Boolean[0]), is(ArrayWrapper.class));
        assertThat($(new Character[0]), is(ArrayWrapper.class));
        assertThat($(new Byte[0]), is(ArrayWrapper.class));
        assertThat($(new Short[0]), is(ArrayWrapper.class));
        assertThat($(new Integer[0]), is(ArrayWrapper.class));
        assertThat($(new Long[0]), is(ArrayWrapper.class));
        assertThat($(new Float[0]), is(ArrayWrapper.class));
        assertThat($(new Double[0]), is(ArrayWrapper.class));
    }

    @Test
    public void dateWrappers() {
        assertThat($(new Date(), new Date()), is(DateRangeWrapper.class));
    }

    @Test
    public void listWrapper() {
        assertThat($(new LinkedList<Object>()), is(ListWrapper.class));
    }

    @Test
    public void setWrapper() {
        assertThat($(new HashSet<Object>()), is(SetWrapper.class));
    }

    @Test
    public void mapWrapper() {
        assertThat($(new HashMap<Object, Object>()), is(MapWrapper.class));
    }

    @Test
    public void dollarCharIsDollarInteger() {
        // the purpose of this test is only to certify the backward compatibility
        // (should be CharRangeWrapper?)
        assertThat($('a'), is(RangeWrapper.class));
    }
}

