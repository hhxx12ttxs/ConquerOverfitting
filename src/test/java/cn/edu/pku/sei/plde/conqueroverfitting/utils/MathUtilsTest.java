package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.Interval;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yjxxtd on 4/23/16.
 */
public class MathUtilsTest {
    @Test
    public void testMergetDoubleInterval(){
        ArrayList<Interval> intervalList = new ArrayList<Interval>();
        List<Interval> intervalMergeList = new ArrayList<Interval>();

        intervalList.add(new Interval(1, 1, true, true));
        intervalList.add(new Interval(2, 2, true, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 2);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 1.0, true, true)));
        assertTrue(intervalMergeList.contains(new Interval(2.0, 2.0, true, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(1, 2, true, false));
        intervalList.add(new Interval(2, 3, false, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 2);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 2.0, true, false)));
        assertTrue(intervalMergeList.contains(new Interval(2.0, 3.0, false, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(17.0, 17.0, true, true));
        intervalList.add(new Interval(17.0, 18.0, true, true));
        intervalList.add(new Interval(18.0, 20.0, true, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(17.0, 20.0, true, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(17.0, 17.0, true, true));
        intervalList.add(new Interval(17.0, 18.0, true, true));
        intervalList.add(new Interval(18.0, 20.0, true, true));
        intervalMergeList = MathUtils.mergetDoubleInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(17.0, 20.0, true, true)));
    }

    @Test
    public void testMergetIntInterval(){
        ArrayList<Interval> intervalList = new ArrayList<Interval>();
        List<Interval> intervalMergeList = new ArrayList<Interval>();

        intervalList.add(new Interval(1, 1, true, true));
        intervalList.add(new Interval(2, 2, true, true));
        intervalMergeList = MathUtils.mergetIntInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 2.0, true, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(1, 1, true, false));
        intervalList.add(new Interval(3, 3, false, true));
        intervalMergeList = MathUtils.mergetIntInterval(intervalList);
        assertTrue(intervalMergeList.size() == 2);
        assertTrue(intervalMergeList.contains(new Interval(1.0, 1.0, true, false)));
        assertTrue(intervalMergeList.contains(new Interval(3.0, 3.0, false, true)));

        intervalList.clear();
        intervalMergeList.clear();
        intervalList.add(new Interval(17.0, 17.0, true, true));
        intervalList.add(new Interval(18.0, 18.0, true, true));
        intervalList.add(new Interval(18.0, 20.0, true, true));
        intervalMergeList = MathUtils.mergetIntInterval(intervalList);
        assertTrue(intervalMergeList.size() == 1);
        assertTrue(intervalMergeList.contains(new Interval(17.0, 20.0, true, true)));
    }
}
