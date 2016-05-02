package test.de.deterministicarts.lib.functional;

import java.util.HashMap;
import java.util.Random;

import de.deterministicarts.lib.functional.Pair;
import de.deterministicarts.lib.functional.Tuple2;
import de.deterministicarts.lib.functional.trees.HashTreeMap;

public class RunHashTreeMap {
	
	public static void main(String[] args) {
		
		final Random rand = new Random();
		final HashMap<Long,String> master = new HashMap<Long,String>();
		HashTreeMap<Long,String> map = HashTreeMap.empty();
		
		for (int k = 0; k < 1000000; ++k) {
			
			final int number = rand.nextInt();
			final Long key = Long.valueOf(number);
			
			if (!master.containsKey(key)) {
			
				final String value = Integer.toString(number);
				master.put(key, value);
				map = map.update(key, value);
			}
		}
		
		System.out.println(map.size());
		System.out.println(master.size());
		
		for (Long key: master.keySet()) {
			
			final String value = master.get(key);
			final String actual = map.get(key);
			
			if (!value.equals(actual)) System.out.println("Ups: " + key + " => " + actual + " (expected " + value + ")");
		}
		
		int seen = 0;
		
		for (Tuple2<Long,String> pair: map) {
			
			if (!master.containsKey(pair.first())) System.out.println("Ups! Found key without master: " + pair);
			else
				++seen;
		}
		
		if (seen != master.size()) System.out.println("Ups: seems, that we haven't seen all items: " + seen);
		
		for (Long key: master.keySet()) {
			
			final String expected = master.get(key);
			final Pair<HashTreeMap<Long,String>,Tuple2<Long,String>> pair = map.removeAssoc(key);
			final Tuple2<Long,String> tuple = pair.second();
			
			if (tuple == null) {

				map.remove(key);
			}
			else if (!expected.equals(tuple.second())) System.out.println("Ups: wrong value: " + tuple);
			
			map = pair.first();
		}
		
		System.out.println(map);
	}
}

