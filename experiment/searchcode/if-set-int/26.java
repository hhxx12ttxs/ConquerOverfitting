package org.pulpcore.test;

import org.junit.Test;
import pulpcore.animation.Color;
import pulpcore.animation.Easing;
import pulpcore.animation.Fixed;
import pulpcore.animation.event.TimelineEvent;
import pulpcore.animation.Int;
import pulpcore.animation.Timeline;
import pulpcore.math.CoreMath;
import static org.junit.Assert.*;

public class AnimationTest {

    @Test public void bidirectionalBind() {
        Int x = new Int(5);
        Fixed y = new Fixed();
        y.bindWithInverse(x);
        assertEquals("Binding not initially set", x.get(), y.getAsInt());
        y.set(10);
        assertEquals("Bi-directional binding broken on inverse", y.getAsInt(), x.get());
        y.animateTo(20, 100);
        y.update(100);
        assertEquals("Bi-directional binding broken on animation", y.getAsInt(), x.get());
        x.animateTo(30, 100);
        x.update(100);
        assertEquals("Bi-directional binding broken on inverse animation", x.get(), y.getAsInt());
    }

    @Test public void bidirectionalBindMultipleClones() {
        Int x = new Int(5);
        Fixed y = new Fixed();
        Int z = new Int();
        y.bindWithInverse(x);
        z.bindWithInverse(x);
        assertEquals("Binding Y not initially set", x.get(), y.getAsInt());
        assertEquals("Binding Z not initially set", x.get(), z.get());
        y.set(10);
        assertEquals("Binding X not set", y.getAsInt(), x.get());
        assertEquals("Binding Z not set", y.getAsInt(), z.get());
        z.set(20);
        assertEquals("Binding X broken", z.get(), x.get());
        assertEquals("Binding Y broken", z.get(), y.getAsInt());
        for (int i = 0; i < 10000; i++) {
            double r = Math.random();
            if (r < 0.3333333) {
                x.set((int)(Math.random()* 100));
            }
            else if (r < 0.6666667) {
                y.set((int)(Math.random()* 100));
            }
            else {
                z.set((int)(Math.random()* 100));
            }
        }
        assertEquals("Random order - Binding X broken", z.get(), x.get());
        assertEquals("Random order - Binding Y broken", z.get(), y.getAsInt());
    }

    @Test public void bidirectionalBindWhatIf() {
        Int x = new Int(5);
        Fixed y = new Fixed();
        x.bindWithInverse(y);
        y.bindWithInverse(x);
        x.set(60);
        assertEquals("Binding not initially set", x.get(), y.getAsInt());

        Int x2 = new Int(5);
        Fixed y2 = new Fixed();
        x2.bindWithInverse(y2);
        x2.bindWithInverse(y2); // do it twice
        x2.set(40);
        assertEquals("Binding y2 not initially set", x2.get(), y2.getAsInt());
        y2.set(50);
        assertEquals("Binding x2 not initially set", x2.get(), y2.getAsInt());
    }

    @Test public void bidirectionalCircular() {
        Int x = new Int(5);
        Fixed y = new Fixed();
        Int z = new Int();
        y.bindWithInverse(x);
        z.bindWithInverse(y);
        x.bindWithInverse(z);
        x.set(40);
        assertEquals("Random order - Binding X broken", z.get(), x.get());
        assertEquals("Random order - Binding Y broken", z.get(), y.getAsInt());
    }

    @Test public void loopingTimelineReachesEnd() {
        Int property = new Int(0);
        Timeline timeline = new Timeline();
        timeline.loopForever();
        timeline.animate(property, 0, 5, 100);
        timeline.update(100);
        assertEquals("Looping timeline does not reach end.", property.get(), 5);
    }
    
    @Test public void eventTriggersOnFastForward() {
        final int[] executions = { 0 };
        Timeline timeline = new Timeline();
        timeline.add(new TimelineEvent(1000) {
            public void run() {
                executions[0]++;
            }
        });
        timeline.fastForward();
        assertEquals("Event does not trigger once on fast-forward", 1, executions[0]);
    }

    @Test public void eventTriggersAtEndOfLoopingTimeline() {
        final int[] executions = { 0 };
        Timeline timeline = new Timeline();
        timeline.add(new TimelineEvent(1000) {
            public void run() {
                executions[0]++;
            }
        });
        timeline.loopForever();
        timeline.update(950);
        timeline.update(100);
        assertEquals("Event does not trigger once at end of Timeline", 1, executions[0]);
        timeline.update(950);
        assertEquals("Event does not trigger once at end of Timeline", 2, executions[0]);
        timeline.update(2000);
        assertEquals("Event does not trigger once at end of Timeline", 4, executions[0]);
    }

    @Test public void eventTriggersInEveryLoopIteration() {
        final int[] executions = { 0 };
        Timeline timeline = new Timeline();
        timeline.add(new TimelineEvent(100) {
            public void run() {
                executions[0]++;
            }
        });
        timeline.loopForever();
        timeline.update(1000);
        assertEquals("Event does not trigger in every loop iteration.", 10, executions[0]);
        timeline.update(210);
        assertEquals("Event does not trigger in every loop iteration.", 12, executions[0]);
        timeline.update(125);
        assertEquals("Event does not trigger in every loop iteration.", 13, executions[0]);
        timeline.update(125);
        assertEquals("Event does not trigger in every loop iteration.", 14, executions[0]);
        timeline.update(125);
        assertEquals("Event does not trigger in every loop iteration.", 15, executions[0]);
        timeline.update(125);
        assertEquals("Event does not trigger in every loop iteration.", 17, executions[0]);
    }

    @Test public void subTimeline() {
        Int property = new Int(0);
        Timeline timeline = new Timeline();
        final int moves = 5;
        final int startTime = 50;
        final int moveDur = 100;
        final int d = 10;
        int t = startTime;
        for (int j = 0; j < moves; j++) {
            int x1 = (j+1) * d;
            int x2 = (j+2) * d;
            
            timeline.at(t).animate(property, x1, x2, moveDur);
            t += moveDur;
        }
        timeline.loopForever();
        timeline.update(25);
        assertEquals("Incorrect value.", 0, property.get());
        timeline.update(startTime-25);
        assertEquals("Incorrect value.", d, property.get());
        timeline.update(moveDur);
        assertEquals("Incorrect value.", d*2, property.get());
        timeline.update(moveDur*2);
        assertEquals("Incorrect value.", d*4, property.get());
        timeline.update(moveDur/2);
        assertEquals("Incorrect value.", d*4 + d/2, property.get());
        timeline.update(moveDur/2);
        assertEquals("Incorrect value.", d*5, property.get());
        timeline.update(moveDur-10);
        assertEquals("Incorrect value.", d*5 + d/2, property.get(), d/2);
        timeline.update(10);
        assertEquals("Incorrect value.", d*6, property.get());
        timeline.update(startTime);
        assertEquals("Incorrect value.", d, property.get());
        timeline.update(moveDur*5);
        assertEquals("Incorrect value.", d*6, property.get());
    }

    @Test public void subTimelineEventTriggersOnce() {
        Int property = new Int(0);
        final int[] executions = { 0 };
        Timeline t = new Timeline();
        t.animate(property, 0, 100, 2000);
        t.after().add(new TimelineEvent(100) {
            public void run() {
                executions[0]++;
            }
        });
        t.update(2000);
        assertEquals("Event in subtimeline does not trigger once.", 0, executions[0]);
        t.update(200);
        assertEquals("Event in subtimeline does not trigger once.", 1, executions[0]);
    }
    
    @Test public void subTimelineEventTriggersOncePart2() {
        Int property = new Int(0);
        final int[] executions = { 0 };
        Timeline t1 = new Timeline();
        Timeline t2 = new Timeline();
        t1.animate(property, 50, 0, 2000);
        t2.animate(property, 0, 100, 2000);
        t2.after().addEvent(new TimelineEvent(100) {
            public void run() {
                executions[0]++;
            }
        });
        t1.after().add(t2);
        t1.update(2000);
        assertEquals("Event in subtimeline does not trigger once.", 0, executions[0]);
        t1.update(2000);
        assertEquals("Event in subtimeline does not trigger once.", 0, executions[0]);
        t1.update(200);
        assertEquals("Event in subtimeline does not trigger once.", 1, executions[0]);
    }


    @Test public void backForthTimeline() {
        Int property = new Int(0);
        int dur = 100;
        int startValue = 0;
        int endValue = 50;
        Timeline timeline = new Timeline();
        timeline.animate(property, startValue, endValue, dur/2, null, 0);
        timeline.animate(property, endValue, startValue, dur/2, null, dur/2);
        timeline.loopForever();
        timeline.update(dur-1);
        timeline.update(1);
        assertEquals("Incorrect value.", startValue, property.get());

        // Same thing, only reversed order of adding animations to timeline
        property = new Int(0);
        timeline = new Timeline();
        timeline.animate(property, endValue, startValue, dur/2, null, dur/2);
        timeline.animate(property, startValue, endValue, dur/2, null, 0);
        timeline.loopForever();
        timeline.update(dur-1);
        timeline.update(1);
        assertEquals("Incorrect value.", startValue, property.get());
    }
    
    @Test public void propertyUpdatesOnGracefullStop() {
        Int property = new Int(0);
        property.animate(0, 1234, 1000);
        property.stopAnimation(true);
        assertTrue("Property does not update on stopAnimation(true)", property.get() == 1234);
    }
    
    @Test public void propertyDoesNotUpdateOnStop() {
        Int property = new Int(0);
        property.animate(0, 1234, 1000);
        property.stopAnimation(false);
        assertTrue("Property incorrectly updates on stopAnimation(false)", property.get() == 0);
    }
    
    @Test public void propertyValueUpdatesOnSetBehavior() {
        Int property = new Int(0);
        property.animate(1234, 5678, 1000);
        assertTrue("Property does not update on setBehavior()", property.get() == 1234);
    }
    
    @Test public void propertyValueDoesNotUpdatesOnDelayedBehavior() {
        Int property = new Int(0);
        property.animate(1234, 5678, 1000, Easing.NONE, 500);
        assertTrue("Property incorrectly updates on delayed setBehavior()", property.get() == 0);
    }

    @Test public void moveToInt() {
        Int property = new Int(0);
        Timeline t = new Timeline();
        t.animate(property, 100, 200, 1000);
        t.animateTo(property, 300, 1000, null, 1000);

        t.update(1);
        assertEquals(100, property.get());
        t.update(999);
        assertEquals(200, property.get());
        t.update(1);
        assertEquals(200, property.get());
        t.update(999);
        assertEquals(300, property.get());
        t.update(100);
        assertEquals(300, property.get());
    }

    @Test public void moveToFixedAsInt() {
        Fixed property = new Fixed(0);
        Timeline t = new Timeline();
        t.animate(property, 100, 200, 1000);
        t.animateTo(property, 300, 1000, null, 1000);

        t.update(1);
        assertEquals(100, property.get(), 1);
        t.update(999);
        assertEquals(200, property.get(), 0);
        t.update(1);
        assertEquals(200, property.get(), 1);
        t.update(999);
        assertEquals(300, property.get(), 0);
        t.update(100);
        assertEquals(300, property.get(), 0);
    }

    @Test public void moveToFixed() {
        Fixed property = new Fixed(0);
        Timeline t = new Timeline();
        t.animateAsFixed(property, CoreMath.toFixed(100), CoreMath.toFixed(200), 1000);
        t.animateToFixed(property, CoreMath.toFixed(300), 1000, null, 1000);

        t.update(1);
        assertEquals(100, property.get(), 1);
        t.update(999);
        assertEquals(200, property.get(), 0);
        t.update(1);
        assertEquals(200, property.get(), 1);
        t.update(999);
        assertEquals(300, property.get(), 0);
        t.update(100);
        assertEquals(300, property.get(), 0);
    }

    @Test public void moveToColor() {
        Color property = new Color(0);
        Timeline t = new Timeline();
        t.animate(property, 0xff000033, 0xff000066, 1000);
        t.animateTo(property, 0xff000099, 1000, null, 1000);

        t.update(1);
        assertEquals(0xff000033, property.get());
        t.update(999);
        assertEquals(0xff000066, property.get());
        t.update(1);
        assertEquals(0xff000066, property.get());
        t.update(999);
        assertEquals(0xff000099, property.get());
        t.update(100);
        assertEquals(0xff000099, property.get());
    }
}

