// Copyright (c) 2007 Sun Microsystems
//    
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//    
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//    
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

package net.sf.hulp.test;

import net.java.hulp.measure.Group;
import net.java.hulp.measure.Probe;
import net.sf.hulp.profiler.Profiler;

import javax.management.openmbean.TabularData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Tests the behavior of Probe when it is loaded in a self-first delegating classloader
 * 
 * @author fkieviet
 */
public class SelfFirstCLTest extends TestCase {
    public static class SelfFirst extends ClassLoader {
        Pattern mPattern;
        public SelfFirst(ClassLoader parent) {
            super(parent);
            mPattern = Pattern.compile(".*");
        }
        
        public SelfFirst(String pattern, ClassLoader parent) {
            super(parent);
            mPattern = Pattern.compile(pattern);
        }
        
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.startsWith("java")) {
                return super.loadClass(name);
            }
            
            Matcher m = mPattern.matcher(name);
            System.out.println(name + " matches: " + m.matches());
            if (!m.matches()) {
                return super.loadClass(name);
            }
            
            System.out.println("loadClass(" + name + ")");
            String file = name.replace('.', '/');
            file += ".class";
            InputStream in = getResourceAsStream(file);
            try {
                byte[] buf = new byte[20000];
                int len = in.read(buf);
                in.close();
                return defineClass(name, buf, 0, len);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ClassNotFoundException();
            }
        }
        
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }
    
    public static int sTestCounter;

    public static class YT {
        public void test() throws Throwable {
            sTestCounter++;
            System.out.println(this.getClass().getClassLoader());
        }
    }
    
    public static class XT {
        public void test() throws Throwable {
            new YT().test();
        }
    }
    
    public static class YM {
        public void test() throws Throwable {
            Probe m1 = Probe.fine(null, "m1");
            m1.setSubtopic(null);
            m1.end();

            m1 = Probe.info(null, "m1", null);
            m1.setTopic("m1");
            m1.end();
            System.out.println(this.getClass().getClassLoader());
        }
    }
    
    public static class XM {
        public void test() throws Throwable {
            new YM().test();
        }

        public void test2() {
            Group g = Group.createGroup();
            g.addPattern("m1", null);
            TabularData data = g.fetchData();
            assertTrue(data != null);
            assertTrue(data.values().size() == 1);
            Integer n = (Integer) data.get(new Object[] {"", "m1", ""}).get(Profiler.H_N);
            assertTrue(n == 2);
            
            g.clearData();
            data = g.fetchData();
            assertTrue(data == null);
        }
        
        public void testIdle() {
            Probe p1 = Probe.fine(this.getClass(), "x");
            p1.end();

            Probe p2 = Probe.fine(this.getClass(), "x");
            p2.end();
            
            assertTrue(p1 == p2);
        }
    }
    
    /**
     * Test self first classloader
     * 
     * @throws Throwable
     */
    public void test1() throws Throwable {
        sTestCounter = 0;
        
        XT xt = new XT();
        xt.test();
        assertTrue(sTestCounter == 1);
        
        Class c = Class.forName(XT.class.getName());
        xt = (XT) c.newInstance();
        xt.test();
        assertTrue(sTestCounter == 2);
        
        c = this.getClass().getClassLoader().loadClass(XT.class.getName());
        xt = (XT) c.newInstance();
        xt.test();
        assertTrue(sTestCounter == 3);
        
        // Static should be invisible now
        SelfFirst s = new SelfFirst(this.getClass().getClassLoader());
        c = s.loadClass(XT.class.getName());
        Object o = c.newInstance();
        o.getClass().getMethod("test").invoke(o);
        assertTrue(sTestCounter == 3);
    }
    
    /**
     * Test aggregation in self first classloader
     * 
     * @throws Throwable
     */
    public void testSelfFirstCLMeasIdle() throws Throwable {
//        // Test with one measurement in current CL
//        Profiler.get().clear();
//        {
//            new XM().testIdle();
//        }
    
        // Test another measurement in self-first classloader
        Profiler.get().clear();
        {
            SelfFirst s = new SelfFirst(this.getClass().getClassLoader());
            Class c = s.loadClass(XM.class.getName());
            Object o = c.newInstance();
            o.getClass().getMethod("testIdle").invoke(o);

            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(m1result == null);
        }
    }
    
    /**
     * Test aggregation in self first classloader
     * 
     * @throws Throwable
     */
    public void testSelfFirstCLMeas() throws Throwable {
        // Test with one measurement in current CL
        System.setProperty("net.sf.hulp.profiler", "1");
        Profiler.get().clear();
        {
            new XM().test();
            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(((Integer) m1result.get(Profiler.H_N)).intValue() == 2);
        }
    
        // Test another measurement in self-first classloader
        Profiler.get().clear();
        {
            SelfFirst s = new SelfFirst(this.getClass().getClassLoader());
            Class c = s.loadClass(XM.class.getName());
            Object o = c.newInstance();
            o.getClass().getMethod("test").invoke(o);

            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(((Integer) m1result.get(Profiler.H_N)).intValue() == 2);
        }
    }
    
    /**
     * Test aggregation in self first classloader; itf jar is in self-delegating CL,
     * impl is in parent CL
     * 
     * @throws Throwable
     */
    public void testSelfFirstCLMeas2() throws Throwable {
        // Test with one measurement in current CL
        System.setProperty("net.sf.hulp.profiler", "1");
        Profiler.get().clear();
        {
            new XM().test();
            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(((Integer) m1result.get(Profiler.H_N)).intValue() == 2);
        }
    
        // Test another measurement in self-first classloader
        Profiler.get().clear();
        {
            SelfFirst s = new SelfFirst(".*(java|test).*", this.getClass().getClassLoader());
            Class c = s.loadClass(XM.class.getName());
            Object o = c.newInstance();
            o.getClass().getMethod("test").invoke(o);

            Map m = Profiler.get().dump("::");
            Map m1result = (Map) m.get("m1" + "::" + null);
            assertTrue(((Integer) m1result.get(Profiler.H_N)).intValue() == 2);
        }
    }
    
    public void testGetData() throws Throwable {
        // Test with one measurement
        System.setProperty("net.sf.hulp.profiler", "1");
        Profiler.get().clear();
        {
            new XM().test();
            Group g = Group.createGroup();
            g.addPattern(".*", ".*");
            TabularData data = g.fetchData();
            assertTrue(data != null);
            assertTrue(data.values().size() == 1);
            Integer n = (Integer) data.get(new Object[] { "", "m1", ""}).get(Profiler.H_N);
            assertTrue(n == 2);

            g.clearData();
            data = g.fetchData();
            assertTrue(data == null);
        }
        
        // Test another measurement in self-first classloader
        Profiler.get().clear();
        {
            SelfFirst s = new SelfFirst(this.getClass().getClassLoader());
            Class c = s.loadClass(XM.class.getName());
            Object o = c.newInstance();
            o.getClass().getMethod("test").invoke(o);
            Group g = Group.createGroup();
            g.addPattern("m1", null);
            TabularData data = g.fetchData();
            assertTrue(data != null);
            assertTrue(data.values().size() == 1);
            Integer n = (Integer) data.get(new Object[] { "", "m1", ""}).get(Profiler.H_N);
            assertTrue(n == 2);

            o.getClass().getMethod("test2").invoke(o);
        }
    }
}

