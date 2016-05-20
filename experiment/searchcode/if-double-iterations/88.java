/*
 * Copyright (C) 2012-2013. Aktive Cortex
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aktivecortex.core.serializer;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aktivecortex.core.axon2backport.saga.Saga;
import org.aktivecortex.core.axon2backport.saga.annotation.AbstractAnnotatedSaga;
import org.aktivecortex.core.axon2backport.saga.repository.JavaSagaSerializer;
import org.aktivecortex.core.axon2backport.saga.repository.SagaSerializer;
import org.aktivecortex.core.axon2backport.saga.repository.XStreamSagaSerializer;
import org.aktivecortex.core.serializer.saga.ProtostuffSagaSerializer;
import org.junit.Before;
import org.junit.Test;

import com.eaio.uuid.UUID;

public class ProtostuffSagaSerializerTest {
	
	private Saga saga;
    private ProtostuffSagaSerializer testSubject;
    private XStreamSagaSerializer xStreamRef;
    private JavaSagaSerializer javaRef;
    private static final int iterations = 5000;
    private static final int MILLIS_TO_NANO = 1000000;
    
 // TODO registrare la IdStrategyFactory nello schema adapter
// 	static {
// 		System.setProperty("protostuff.runtime.id_strategy_factory",
// 				"org.aktivecortex.core.serializer.ProtostuffSagaSerializerTest$IdStrategyFactory");
// 	}

// 	public static class IdStrategyFactory implements IdStrategy.Factory {
//
// 		DefaultIdStrategy strategy = new DefaultIdStrategy();
//
// 		public IdStrategyFactory() {
// 		}
//
// 		public IdStrategy create() {
// 			return strategy;
// 		}
//
// 		public void postCreate() {
// 			strategy.registerCollection(new CollectionSchema.MessageFactory() {
//
// 				@Override
// 				public Collection<AssociationValue> newMessage() {
// 					return new AssociationValuesImpl();
// 				}
//
// 				@Override
// 				public Class<AssociationValues> typeClass() {
// 					return AssociationValues.class;
// 				}
//
// 			});
// 		}
//
// 	}

    @Before
    public void setUp() {
        this.saga = new MyTestSaga("123");
        this.testSubject = new ProtostuffSagaSerializer();
        this.xStreamRef = new XStreamSagaSerializer();
        this.javaRef = new JavaSagaSerializer();
        System.gc();
    }
    
    @Test
    public void testDefaultSettings() {
        Saga result = serializeAndBack(saga, new ProtostuffSagaSerializer());
        assertEquals(saga, result);
    }
    
    @Test
	public void testJavaSerialization() throws IOException {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			byte[] serializedSaga = javaRef.serialize(saga);
		}
		printResult("Java Saga Serialization", System.nanoTime(), start);
		printObject(javaRef.serialize(saga));
	}
    
    @Test
	public void testProtostuffSerialization() {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			byte[] serializedSaga = testSubject.serialize(saga);
		}
		printResult("Protostuff Saga Serialization", System.nanoTime(), start);
		printObject(testSubject.serialize(saga));
	}
    
    @Test
	public void testXStreamSerialization() {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			byte[] serializedSaga = xStreamRef.serialize(saga);
		}
		printResult("XStream Saga Serialization", System.nanoTime(), start);
		printObject(xStreamRef.serialize(saga));
	}
    
    @Test
	public void testJavaDeserialization() throws Exception {
		long start = System.nanoTime();
		byte[] serializedSaga = javaRef.serialize(saga);
		for (int i = 0; i < iterations; i++) {
			Saga saga = javaRef.deserialize(serializedSaga);
		}
		printResult("Java Saga Deserialization", System.nanoTime(), start);
	}
	
	@Test
	public void testProtostuffDeserialization() {
		long start = System.nanoTime();
		byte[] serializedSaga = testSubject.serialize(saga);
		for (int i = 0; i < iterations; i++) {
			Saga saga = testSubject.deserialize(serializedSaga);
		}
		printResult("Protostuff Saga Deserialization", System.nanoTime(), start);
	}
	
	@Test
	public void testXStreamDeserialization() {
		long start = System.nanoTime();
		byte[] serializedSaga = xStreamRef.serialize(saga);
		for (int i = 0; i < iterations; i++) {
			Saga saga = xStreamRef.deserialize(serializedSaga);
		}
		printResult("XStream Saga Deserialization", System.nanoTime(), start);
	}
    
    private Saga serializeAndBack(Saga saga, SagaSerializer serializer) {
        return serializer.deserialize(serializer.serialize(saga));
    }
    
    private void printResult(String testName, long end, long start) {
		StringBuilder sb = new StringBuilder();
		final long duration = end - start;
		final double iterationTime = iterationTime(duration, iterations);
		sb
		.append("---------------------------------------------\n ")
		.append(testName).append(" (").append(iterations).append(" iterations)")
		.append("\n---------------------------------------------\n")
		.append(" Avg: ").append(iterationTime)
		.append(" nanos. Total: ").append(duration)
		.append(" nanos.\n Avg: ").append(
				BigDecimal.valueOf(iterationTime).longValue() / MILLIS_TO_NANO)
		.append(" millis. Total: ").append(
				duration / MILLIS_TO_NANO)
				.append(" millis.\n");
		System.out.println(sb.toString());
	}
    
    protected static double iterationTime(long delta, int iterations) {
		return (double) delta / (double) (iterations);
	}
	
	private void printObject(byte[] serialize) {
		StringBuilder sb = new StringBuilder();
		sb.append(" Object size: ").append(serialize.length).append(" bytes \n");
		System.out.println(sb.toString());
		
	}

	public static class MyTestSaga extends AbstractAnnotatedSaga {

        private static final long serialVersionUID = -1562911263884220240L;
        private int counter;
        private Map<Integer, String> map;

        public MyTestSaga(String identifier) {
            super(identifier);
            this.counter = (int) (100 * Math.random());
            this.map = new ConcurrentHashMap<Integer, String>();
            for (int i = 0; i < counter; i++) {
				map.put(i, new UUID().toString());
			}
        }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + counter;
			result = prime * result + ((map == null) ? 0 : map.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final MyTestSaga other = (MyTestSaga) obj;
			if (counter != other.counter)
				return false;
			if (map == null) {
				if (other.map != null)
					return false;
			} else if (!map.equals(other.map))
				return false;
			return true;
		}

        
    }

}

