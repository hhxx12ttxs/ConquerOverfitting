package adhoc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Self-contained test of the speed of accessing several different map implementations looking for the same strings.
 */
public class MapSpeedTest {
    private static long seed = System.nanoTime();
    private static int loop = 100_000_000;
    
    public static void timeAccess(Map<String,String> map) {
        Random rnd = new Random(seed);
        int foundCount = 0;
        
        long start = System.nanoTime();
        
        for(int i = 0; i < loop; i++) {
            String s = map.get(RndString.build(rnd));
            if(s != null)
                foundCount++;
        }
        
        long stop = System.nanoTime() - start;
        
        System.out.println("Found "+foundCount+" strings out of "+loop+" attempts - "+String.format("%.2f",100.0*foundCount/loop)+" success rate.");
        System.out.println(map.getClass().getSimpleName()+" took "+String.format("%.4f", stop/1_000_000_000.0)+" seconds.");
        System.out.println();
    }
    
    public static HashMap<String,String> buildMap() {
        Random rnd = new Random();
        HashMap<String,String> map = new HashMap<>();
        for(int i = 0; i < loop*.01; i++) {
            String str = RndString.build(rnd);
            map.put(str, str);
        }
        return map;
    }
    
    public static void main(String[] args) {
        // Construction / insertion time isn't relevant for this test
        Map<String,String> hash = buildMap();
        Map<String,String> conc = new ConcurrentHashMap<>(hash);
        Map<String,String> immut = ImmutableMap.copyOf(hash);
        
        // Shuffle the maps for good measure, doesn't seem to impact the test
        @SuppressWarnings("unchecked")
        List<Map<String,String>> ls = Lists.newArrayList(hash, conc, immut);
        Collections.shuffle(ls);
        
        for(Map<String,String> map : ls) {
            timeAccess(map);
        }
    }
    
    public static class RndString {
        // You can play the with values here to impact the success rate
        // smaller words and smaller namespaces increase the likelyhood
        // a string will be in the map; conversely larger values
        // mean the map will be more sparse, more often returning null.
        private static final int minLen = 4;
        private static final int maxLen = 5;
        private static final char minChar = 'A';
        private static final char maxChar = minChar+26;
        private static final char[] strBuff = new char[maxLen];
        
        public static String build(Random rnd) {
            // 5 char minimum ensures reasonable likelihood of unique string
            int strLen = rnd.nextInt(maxLen-minLen+1)+minLen;
            for(int i = 0; i < strLen; i++) {
                strBuff[i] = (char)(minChar + rnd.nextInt(maxChar-minChar));
            }
            return new String(strBuff,0,strLen);
        }
    }
}

