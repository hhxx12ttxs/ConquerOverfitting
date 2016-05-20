/*
 * Copyright 2012 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.parallel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.gs.collections.api.RichIterable;
import com.gs.collections.api.bag.Bag;
import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.block.procedure.ObjectIntProcedure;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.list.ImmutableList;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.map.MutableMap;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.bag.mutable.HashBag;
import com.gs.collections.impl.block.factory.Functions;
import com.gs.collections.impl.block.factory.HashingStrategies;
import com.gs.collections.impl.block.factory.Predicates;
import com.gs.collections.impl.factory.Lists;
import com.gs.collections.impl.list.Interval;
import com.gs.collections.impl.list.mutable.ArrayListAdapter;
import com.gs.collections.impl.list.mutable.CompositeFastList;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.list.mutable.ListAdapter;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.set.mutable.MultiReaderUnifiedSet;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import com.gs.collections.impl.set.strategy.mutable.UnifiedSetWithHashingStrategy;
import com.gs.collections.impl.test.Verify;
import com.gs.collections.impl.utility.ArrayIterate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParallelIterateTest
{
    private static final Procedure<Integer> EXCEPTION_PROCEDURE = new Procedure<Integer>()
    {
        public void value(Integer value)
        {
            throw new RuntimeException("Thread death on its way!");
        }
    };
    private static final ObjectIntProcedure<Integer> EXCEPTION_OBJECT_INT_PROCEDURE = new ObjectIntProcedure<Integer>()
    {
        public void value(Integer object, int index)
        {
            throw new RuntimeException("Thread death on its way!");
        }
    };

    private static final Function<Integer, Collection<String>> INT_TO_TWO_STRINGS = new Function<Integer, Collection<String>>()
    {
        public Collection<String> valueOf(Integer integer)
        {
            return Lists.fixedSize.of(integer.toString(), integer.toString());
        }
    };

    private int count;
    private final MutableSet<String> threadNames = MultiReaderUnifiedSet.newSet();

    private ImmutableList<RichIterable<Integer>> iterables;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Before
    public void setUp()
    {
        Interval interval = Interval.oneTo(20000);
        this.iterables = Lists.immutable.of(
                interval.toList(),
                interval.toList().asUnmodifiable(),
                interval.toList().asSynchronized(),
                interval.toList().toImmutable(),
                interval.toSet(),
                interval.toSet().asUnmodifiable(),
                interval.toSet().asSynchronized(),
                interval.toSet().toImmutable(),
                interval.toBag(),
                interval.toBag().asUnmodifiable(),
                interval.toBag().asSynchronized(),
                interval.toBag().toImmutable(),
                interval.toSortedSet(),
                interval.toSortedSet().asUnmodifiable(),
                interval.toSortedSet().asSynchronized(),
                interval.toSortedSet().toImmutable(),
                interval.toMap(Functions.<Integer>getPassThru(), Functions.<Integer>getPassThru()),
                interval.toMap(Functions.<Integer>getPassThru(), Functions.<Integer>getPassThru()).asUnmodifiable(),
                interval.toMap(Functions.<Integer>getPassThru(), Functions.<Integer>getPassThru()).asSynchronized(),
                interval.toMap(Functions.<Integer>getPassThru(), Functions.<Integer>getPassThru()).toImmutable(),
                ArrayListAdapter.<Integer>newList().withAll(interval),
                ArrayListAdapter.<Integer>newList().withAll(interval).asUnmodifiable(),
                ArrayListAdapter.<Integer>newList().withAll(interval).asSynchronized(),
                new CompositeFastList<Integer>().withAll(interval.toList()),
                new CompositeFastList<Integer>().withAll(interval.toList()).asUnmodifiable(),
                new CompositeFastList<Integer>().withAll(interval.toList()).asSynchronized(),
                new CompositeFastList<Integer>().withAll(interval.toList()).toImmutable(),
                ListAdapter.<Integer>adapt(new LinkedList<Integer>()).withAll(interval),
                ListAdapter.<Integer>adapt(new LinkedList<Integer>()).withAll(interval).asUnmodifiable(),
                ListAdapter.<Integer>adapt(new LinkedList<Integer>()).withAll(interval).asSynchronized(),
                UnifiedSetWithHashingStrategy.<Integer>newSet(HashingStrategies.defaultStrategy()).withAll(interval),
                UnifiedSetWithHashingStrategy.<Integer>newSet(HashingStrategies.defaultStrategy()).withAll(interval).asUnmodifiable(),
                UnifiedSetWithHashingStrategy.<Integer>newSet(HashingStrategies.defaultStrategy()).withAll(interval).asSynchronized(),
                UnifiedSetWithHashingStrategy.<Integer>newSet(HashingStrategies.defaultStrategy()).withAll(interval).toImmutable()
        );
    }

    @After
    public void tearDown()
    {
        this.executor.shutdown();
    }

    @Test
    public void testOneLevelCall()
    {
        new RecursiveProcedure().value(1);

        synchronized (this)
        {
            Assert.assertEquals("all iterations completed", 20000, this.count);
        }
    }

    @Test
    public void testNestedCall()
    {
        new RecursiveProcedure().value(2);

        synchronized (this)
        {
            Assert.assertEquals("all iterations completed", 419980, this.count);
        }
        Assert.assertTrue("uses multiple threads", this.threadNames.size() > 1);
    }

    @Test
    public void testForEachUsingSet()
    {
        //Tests the default batch size calculations
        IntegerSum sum = new IntegerSum(0);
        MutableSet<Integer> set = Interval.toSet(1, 10000);
        ParallelIterate.forEach(set, new SumProcedure(sum), new SumCombiner(sum));
        Assert.assertEquals(50005000, sum.getSum());

        //Testing batch size 1
        IntegerSum sum2 = new IntegerSum(0);
        UnifiedSet<Integer> set2 = UnifiedSet.newSet(Interval.oneTo(100));
        ParallelIterate.forEach(set2, new SumProcedure(sum2), new SumCombiner(sum2), 1, set2.getBatchCount(set2.size()));
        Assert.assertEquals(5050, sum2.getSum());

        //Testing an uneven batch size
        IntegerSum sum3 = new IntegerSum(0);
        UnifiedSet<Integer> set3 = UnifiedSet.newSet(Interval.oneTo(100));
        ParallelIterate.forEach(set3, new SumProcedure(sum3), new SumCombiner(sum3), 1, set3.getBatchCount(13));
        Assert.assertEquals(5050, sum3.getSum());

        //Testing divideByZero exception by passing 1 as batchSize
        IntegerSum sum4 = new IntegerSum(0);
        UnifiedSet<Integer> set4 = UnifiedSet.newSet(Interval.oneTo(100));
        ParallelIterate.forEach(set4, new SumProcedure(sum4), new SumCombiner(sum4), 1);
        Assert.assertEquals(5050, sum4.getSum());
    }

    @Test
    public void testForEachUsingMap()
    {
        //Test the default batch size calculations
        IntegerSum sum1 = new IntegerSum(0);
        MutableMap<String, Integer> map1 = Interval.fromTo(1, 10000).toMap(Functions.getToString(), Functions.getIntegerPassThru());
        ParallelIterate.forEach(map1, new SumProcedure(sum1), new SumCombiner(sum1));
        Assert.assertEquals(50005000, sum1.getSum());

        //Testing batch size 1
        IntegerSum sum2 = new IntegerSum(0);
        UnifiedMap<String, Integer> map2 = (UnifiedMap<String, Integer>) Interval.fromTo(1, 100).toMap(Functions.getToString(), Functions.getIntegerPassThru());
        ParallelIterate.forEach(map2, new SumProcedure(sum2), new SumCombiner(sum2), 1, map2.getBatchCount(map2.size()));
        Assert.assertEquals(5050, sum2.getSum());

        //Testing an uneven batch size
        IntegerSum sum3 = new IntegerSum(0);
        UnifiedMap<String, Integer> set3 = (UnifiedMap<String, Integer>) Interval.fromTo(1, 100).toMap(Functions.getToString(), Functions.getIntegerPassThru());
        ParallelIterate.forEach(set3, new SumProcedure(sum3), new SumCombiner(sum3), 1, set3.getBatchCount(13));
        Assert.assertEquals(5050, sum3.getSum());
    }

    @Test
    public void testForEach()
    {
        IntegerSum sum1 = new IntegerSum(0);
        List<Integer> list1 = createIntegerList(16);
        ParallelIterate.forEach(list1, new SumProcedure(sum1), new SumCombiner(sum1), 1, list1.size() / 2);
        Assert.assertEquals(16, sum1.getSum());

        IntegerSum sum2 = new IntegerSum(0);
        List<Integer> list2 = createIntegerList(7);
        ParallelIterate.forEach(list2, new SumProcedure(sum2), new SumCombiner(sum2));
        Assert.assertEquals(7, sum2.getSum());

        IntegerSum sum3 = new IntegerSum(0);
        List<Integer> list3 = createIntegerList(15);
        ParallelIterate.forEach(list3, new SumProcedure(sum3), new SumCombiner(sum3), 1, list3.size() / 2);
        Assert.assertEquals(15, sum3.getSum());

        IntegerSum sum4 = new IntegerSum(0);
        List<Integer> list4 = createIntegerList(35);
        ParallelIterate.forEach(list4, new SumProcedure(sum4), new SumCombiner(sum4));
        Assert.assertEquals(35, sum4.getSum());

        IntegerSum sum5 = new IntegerSum(0);
        MutableList<Integer> list5 = FastList.newList(list4);
        ParallelIterate.forEach(list5, new SumProcedure(sum5), new SumCombiner(sum5));
        Assert.assertEquals(35, sum5.getSum());

        IntegerSum sum6 = new IntegerSum(0);
        List<Integer> list6 = createIntegerList(40);
        ParallelIterate.forEach(list6, new SumProcedure(sum6), new SumCombiner(sum6), 1, list6.size() / 2);
        Assert.assertEquals(40, sum6.getSum());

        IntegerSum sum7 = new IntegerSum(0);
        MutableList<Integer> list7 = FastList.newList(list6);
        ParallelIterate.forEach(list7, new SumProcedure(sum7), new SumCombiner(sum7), 1, list6.size() / 2);
        Assert.assertEquals(40, sum7.getSum());
    }

    @Test
    public void testForEachImmutableList()
    {
        IntegerSum sum1 = new IntegerSum(0);
        ImmutableList<Integer> list1 = Lists.immutable.ofAll(createIntegerList(16));
        ParallelIterate.forEach(list1, new SumProcedure(sum1), new SumCombiner(sum1), 1, list1.size() / 2);
        Assert.assertEquals(16, sum1.getSum());

        IntegerSum sum2 = new IntegerSum(0);
        ImmutableList<Integer> list2 = Lists.immutable.ofAll(createIntegerList(7));
        ParallelIterate.forEach(list2, new SumProcedure(sum2), new SumCombiner(sum2));
        Assert.assertEquals(7, sum2.getSum());

        IntegerSum sum3 = new IntegerSum(0);
        ImmutableList<Integer> list3 = Lists.immutable.ofAll(createIntegerList(15));
        ParallelIterate.forEach(list3, new SumProcedure(sum3), new SumCombiner(sum3), 1, list3.size() / 2);
        Assert.assertEquals(15, sum3.getSum());

        IntegerSum sum4 = new IntegerSum(0);
        ImmutableList<Integer> list4 = Lists.immutable.ofAll(createIntegerList(35));
        ParallelIterate.forEach(list4, new SumProcedure(sum4), new SumCombiner(sum4));
        Assert.assertEquals(35, sum4.getSum());

        IntegerSum sum5 = new IntegerSum(0);
        ImmutableList<Integer> list5 = FastList.newList(list4).toImmutable();
        ParallelIterate.forEach(list5, new SumProcedure(sum5), new SumCombiner(sum5));
        Assert.assertEquals(35, sum5.getSum());

        IntegerSum sum6 = new IntegerSum(0);
        ImmutableList<Integer> list6 = Lists.immutable.ofAll(createIntegerList(40));
        ParallelIterate.forEach(list6, new SumProcedure(sum6), new SumCombiner(sum6), 1, list6.size() / 2);
        Assert.assertEquals(40, sum6.getSum());

        IntegerSum sum7 = new IntegerSum(0);
        ImmutableList<Integer> list7 = FastList.newList(list6).toImmutable();
        ParallelIterate.forEach(list7, new SumProcedure(sum7), new SumCombiner(sum7), 1, list6.size() / 2);
        Assert.assertEquals(40, sum7.getSum());
    }

    @Test
    public void testForEachWithException()
    {
        Verify.assertThrows(RuntimeException.class, new Runnable()
        {
            public void run()
            {
                ParallelIterate.forEach(
                        createIntegerList(5),
                        new PassThruProcedureFactory<Procedure<Integer>>(EXCEPTION_PROCEDURE),
                        new PassThruCombiner<Procedure<Integer>>(),
                        1,
                        5);
            }
        });
    }

    @Test
    public void testForEachWithIndexToArrayUsingFastListSerialPath()
    {
        final Integer[] array = new Integer[200];
        FastList<Integer> list = (FastList<Integer>) Interval.oneTo(200).toList();
        Assert.assertTrue(ArrayIterate.allSatisfy(array, Predicates.isNull()));
        ParallelIterate.forEachWithIndex(list, new ObjectIntProcedure<Integer>()
        {
            public void value(Integer each, int index)
            {
                array[index] = each;
            }
        });
        Assert.assertArrayEquals(array, list.toArray(new Integer[]{}));
    }

    @Test
    public void testForEachWithIndexToArrayUsingFastList()
    {
        final Integer[] array = new Integer[200];
        FastList<Integer> list = (FastList<Integer>) Interval.oneTo(200).toList();
        Assert.assertTrue(ArrayIterate.allSatisfy(array, Predicates.isNull()));
        ParallelIterate.forEachWithIndex(list, new ObjectIntProcedure<Integer>()
        {
            public void value(Integer each, int index)
            {
                array[index] = each;
            }
        }, 10, 10);
        Assert.assertArrayEquals(array, list.toArray(new Integer[]{}));
    }

    @Test
    public void testForEachWithIndexToArrayUsingImmutableList()
    {
        final Integer[] array = new Integer[200];
        ImmutableList<Integer> list = Interval.oneTo(200).toList().toImmutable();
        Assert.assertTrue(ArrayIterate.allSatisfy(array, Predicates.isNull()));
        ParallelIterate.forEachWithIndex(list, new ObjectIntProcedure<Integer>()
        {
            public void value(Integer each, int index)
            {
                array[index] = each;
            }
        }, 10, 10);
        Assert.assertArrayEquals(array, list.toArray(new Integer[]{}));
    }

    @Test
    public void testForEachWithIndexToArrayUsingArrayList()
    {
        final Integer[] array = new Integer[200];
        List<Integer> list = new ArrayList<Integer>(Interval.oneTo(200));
        Assert.assertTrue(ArrayIterate.allSatisfy(array, Predicates.isNull()));
        ParallelIterate.forEachWithIndex(list, new ObjectIntProcedure<Integer>()
        {
            public void value(Integer each, int index)
            {
                array[index] = each;
            }
        }, 10, 10);
        Assert.assertArrayEquals(array, list.toArray(new Integer[]{}));
    }

    @Test
    public void testForEachWithIndexToArrayUsingFixedArrayList()
    {
        final Integer[] array = new Integer[10];
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Assert.assertTrue(ArrayIterate.allSatisfy(array, Predicates.isNull()));
        ParallelIterate.forEachWithIndex(list, new ObjectIntProcedure<Integer>()
        {
            public void value(Integer each, int index)
            {
                array[index] = each;
            }
        }, 1, 2);
        Assert.assertArrayEquals(array, list.toArray(new Integer[list.size()]));
    }

    @Test
    public void testForEachWithIndexException()
    {
        Verify.assertThrows(RuntimeException.class, new Runnable()
        {
            public void run()
            {
                ParallelIterate.forEachWithIndex(
                        createIntegerList(5),
                        new PassThruObjectIntProcedureFactory<ObjectIntProcedure<Integer>>(EXCEPTION_OBJECT_INT_PROCEDURE),
                        new PassThruCombiner<ObjectIntProcedure<Integer>>(),
                        1,
                        5);
            }
        });
    }

    @Test
    public void select()
    {
        this.iterables.forEach(new Procedure<RichIterable<Integer>>()
        {
            public void value(RichIterable<Integer> each)
            {
                ParallelIterateTest.this.basicSelect(each);
            }
        });
    }

    private void basicSelect(RichIterable<Integer> iterable)
    {
        Collection<Integer> actual1 = ParallelIterate.select(iterable, Predicates.greaterThan(10000));
        Collection<Integer> actual2 = ParallelIterate.select(iterable, Predicates.greaterThan(10000), HashBag.<Integer>newBag(), 3, this.executor, true);
        Collection<Integer> actual3 = ParallelIterate.select(iterable, Predicates.greaterThan(10000), true);
        RichIterable<Integer> expected = iterable.select(Predicates.greaterThan(10000));
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual1.getClass().getSimpleName(), expected, actual1);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual2.getClass().getSimpleName(), expected.toBag(), actual2);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual3.getClass().getSimpleName(), expected.toBag(), HashBag.newBag(actual3));
    }

    @Test
    public void selectSortedSet()
    {
        RichIterable<Integer> iterable = Interval.oneTo(20000).toSortedSet();
        Collection<Integer> actual1 = ParallelIterate.select(iterable, Predicates.greaterThan(10000));
        Collection<Integer> actual2 = ParallelIterate.select(iterable, Predicates.greaterThan(10000), true);
        RichIterable<Integer> expected = iterable.select(Predicates.greaterThan(10000));
        Assert.assertSame(expected.getClass(), actual1.getClass());
        Assert.assertSame(expected.getClass(), actual2.getClass());
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual1.getClass().getSimpleName(), expected, actual1);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual2.getClass().getSimpleName(), expected, actual2);
    }

    @Test
    public void count()
    {
        this.iterables.forEach(new Procedure<RichIterable<Integer>>()
        {
            public void value(RichIterable<Integer> each)
            {
                ParallelIterateTest.this.basicCount(each);
            }
        });
    }

    private void basicCount(RichIterable<Integer> listIterable)
    {
        int actual1 = ParallelIterate.count(listIterable, Predicates.greaterThan(10000));
        int actual2 = ParallelIterate.count(listIterable, Predicates.greaterThan(10000), 11, this.executor);
        Assert.assertEquals(10000, actual1);
        Assert.assertEquals(10000, actual2);
    }

    @Test
    public void reject()
    {
        this.iterables.forEach(new Procedure<RichIterable<Integer>>()
        {
            public void value(RichIterable<Integer> each)
            {
                ParallelIterateTest.this.basicReject(each);
            }
        });
    }

    private void basicReject(RichIterable<Integer> iterable)
    {
        Collection<Integer> actual1 = ParallelIterate.reject(iterable, Predicates.greaterThan(10000));
        Collection<Integer> actual2 = ParallelIterate.reject(iterable, Predicates.greaterThan(10000), HashBag.<Integer>newBag(), 3, this.executor, true);
        Collection<Integer> actual3 = ParallelIterate.reject(iterable, Predicates.greaterThan(10000), true);
        RichIterable<Integer> expected = iterable.reject(Predicates.greaterThan(10000));
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual1.getClass().getSimpleName(), expected, actual1);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual2.getClass().getSimpleName(), expected.toBag(), actual2);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual3.getClass().getSimpleName(), expected.toBag(), HashBag.newBag(actual3));
    }

    @Test
    public void collect()
    {
        this.iterables.forEach(new Procedure<RichIterable<Integer>>()
        {
            public void value(RichIterable<Integer> each)
            {
                ParallelIterateTest.this.basicCollect(each);
            }
        });
    }

    private void basicCollect(RichIterable<Integer> iterable)
    {
        Collection<String> actual1 = ParallelIterate.collect(iterable, Functions.getToString());
        Collection<String> actual2 = ParallelIterate.collect(iterable, Functions.getToString(), HashBag.<String>newBag(), 3, this.executor, false);
        Collection<String> actual3 = ParallelIterate.collect(iterable, Functions.getToString(), true);
        RichIterable<String> expected = iterable.collect(Functions.getToString());
        Verify.assertSize(20000, actual1);
        Verify.assertContains(String.valueOf(20000), actual1);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual1.getClass().getSimpleName(), expected, actual1);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual2.getClass().getSimpleName(), expected.toBag(), actual2);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual3.getClass().getSimpleName(), expected.toBag(), HashBag.newBag(actual3));
    }

    @Test
    public void collectIf()
    {
        this.iterables.forEach(new Procedure<RichIterable<Integer>>()
        {
            public void value(RichIterable<Integer> each)
            {
                ParallelIterateTest.this.basicCollectIf(each);
            }
        });
    }

    private void basicCollectIf(RichIterable<Integer> collection)
    {
        Predicate<Integer> greaterThan = Predicates.greaterThan(10000);
        Collection<String> actual1 = ParallelIterate.collectIf(collection, greaterThan, Functions.getToString());
        Collection<String> actual2 = ParallelIterate.collectIf(collection, greaterThan, Functions.getToString(), HashBag.<String>newBag(), 3, this.executor, true);
        Collection<String> actual3 = ParallelIterate.collectIf(collection, greaterThan, Functions.getToString(), HashBag.<String>newBag(), 3, this.executor, true);
        Bag<String> expected = collection.collectIf(greaterThan, Functions.getToString()).toBag();
        Verify.assertSize(10000, actual1);
        Verify.assertNotContains(String.valueOf(9000), actual1);
        Verify.assertNotContains(String.valueOf(21000), actual1);
        Verify.assertContains(String.valueOf(15976), actual1);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual1.getClass().getSimpleName(), expected, HashBag.newBag(actual1));
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual2.getClass().getSimpleName(), expected, actual2);
        Assert.assertEquals(expected.getClass().getSimpleName() + '/' + actual3.getClass().getSimpleName(), expected, actual3);
    }

    @Test
    public void flatCollect()
    {
        this.iterables.forEach(new Procedure<RichIterable<Integer>>()
        {
            public void value(RichIterable<Integer> each)
            {
                ParallelIterateTest.this.basicFlatCollect(each);
            }
        });
    }

    private void basicFlatCollect(RichIterable<Integer> iterable)
    {
        Collection<String> actual1 = ParallelIterate.flatCollect(iterable, INT_TO_TWO_STRINGS);
        Collection<String> actual2 = ParallelIterate.flatCollect(iterable, INT_TO_TWO_STRINGS, HashBag.<String>newBag(), 3, this.executor, false);
        Collection<String> actual3 = ParallelIterate.flatCollect(iterable, INT_TO_TWO_STRINGS, true);
        RichIterable<String> expected1 = iterable.flatCollect(INT_TO_TWO_STRINGS);
        RichIterable<String> expected2 = iterable.flatCollect(INT_TO_TWO_STRINGS, HashBag.<String>newBag());
        Verify.assertContains(String.valueOf(20000), actual1);
        Assert.assertEquals(expected1.getClass().getSimpleName() + '/' + actual1.getClass().getSimpleName(), expected1, actual1);
        Assert.assertEquals(expected2.getClass().getSimpleName() + '/' + actual2.getClass().getSimpleName(), expected2, actual2);
        Assert.assertEquals(expected1.getClass().getSimpleName() + '/' + actual3.getClass().getSimpleName(), expected1.toBag(), HashBag.newBag(actual3));
    }

    private static List<Integer> createIntegerList(int size)
    {
        return Collections.nCopies(size, Integer.valueOf(1));
    }

    private class RecursiveProcedure implements Procedure<Integer>
    {
        private static final long serialVersionUID = 1L;
        private final ExecutorService executorService = ParallelIterate.newPooledExecutor("ParallelIterateTest", false);

        public void value(Integer level)
        {
            if (level > 0)
            {
                ParallelIterateTest.this.threadNames.add(Thread.currentThread().getName());
                this.executeParallelIterate(level - 1, this.executorService);
            }
            else
            {
                this.simulateWork();
            }
        }

        private void simulateWork()
        {
            synchronized (ParallelIterateTest.this)
            {
                ParallelIterateTest.this.count++;
            }
        }

        private void executeParallelIterate(int level, ExecutorService executorService)
        {
            MutableList<Integer> items = Lists.mutable.of();
            for (int i = 0; i < 20000; i++)
            {
                items.add(i % 1000 == 0 ? level : 0);
            }
            ParallelIterate.forEach(items, new RecursiveProcedure(), executorService);
        }
    }

    public static final class IntegerSum
    {
        private int sum = 0;

        public IntegerSum(int newSum)
        {
            this.sum = newSum;
        }

        public IntegerSum add(int value)
        {
            this.sum += value;
            return this;
        }

        public int getSum()
        {
            return this.sum;
        }
    }

    public static final class SumProcedure
            implements Procedure<Integer>, Function2<IntegerSum, Integer, IntegerSum>, ProcedureFactory<SumProcedure>
    {
        private static final long serialVersionUID = 1L;

        private final IntegerSum sum;

        public SumProcedure(IntegerSum newSum)
        {
            this.sum = newSum;
        }

        public SumProcedure create()
        {
            return new SumProcedure(new IntegerSum(0));
        }

        public IntegerSum value(IntegerSum s1, Integer s2)
        {
            return s1.add(s2);
        }

        public void value(Integer object)
        {
            this.sum.add(object);
        }

        public int getSum()
        {
            return this.sum.getSum();
        }
    }

    public static final class SumCombiner extends AbstractProcedureCombiner<SumProcedure>
    {
        private static final long serialVersionUID = 1L;
        private final IntegerSum sum;

        public SumCombiner(IntegerSum initialSum)
        {
            super(true);
            this.sum = initialSum;
        }

        public void combineOne(SumProcedure sumProcedure)
        {
            this.sum.add(sumProcedure.getSum());
        }
    }
}

