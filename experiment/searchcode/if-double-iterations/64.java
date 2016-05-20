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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.axonframework.saga.AssociationValue;
import org.axonframework.saga.AssociationValues;
import org.axonframework.saga.annotation.AssociationValuesImpl;
import org.axonframework.serializer.XStreamSerializer;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;

import com.dyuproject.protostuff.CollectionSchema;
import com.dyuproject.protostuff.MapSchema;
import com.dyuproject.protostuff.runtime.DefaultIdStrategy;
import com.dyuproject.protostuff.runtime.ExplicitIdStrategy;
import com.dyuproject.protostuff.runtime.IdStrategy;
import com.dyuproject.protostuff.runtime.IncrementalIdStrategy;
import com.dyuproject.protostuff.runtime.ProtostuffRuntimeSchemaAdapter;

public class ProtostuffSerializerTest {

	private static final int MILLIS_TO_NANO = 1000000;
	private XStreamSerializer testReference;
	private ProtostuffSerializer<TestEvent> testSubject;
	private static final String SPECIAL__CHAR__STRING = "Special chars: '\"&;\n\\<>/\n\t";
	private static final int iterations = 5000;
	private TestEvent value;
	private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";



	@Before
	public void setUp() throws Exception {
		this.testReference = new XStreamSerializer();
		this.testSubject = new ProtostuffSerializer<TestEvent>(new ProtostuffRuntimeSchemaAdapter(true));
		this.value = new TestEvent(LOREM_IPSUM);
		System.gc();
	}

	@Test
	public void testSerializeAndDeserializeDomainEventwithRef() {
		final TestEvent testEvent = new TestEvent("Henk");
		byte[] serializedEvent = testReference.serialize(testEvent);
		TestEvent actualResult = (TestEvent) testReference.deserialize(serializedEvent);
		System.out.println("XStream expected iterval:         " + testEvent.getWeek() + " - s: "
				+ testEvent.getWeek().getStartMillis() + " - e: " + testEvent.getWeek().getEndMillis());
		System.out.println("XStream   actual iterval:         " + actualResult.getWeek() + " - s: "
				+ actualResult.getWeek().getStartMillis() + " - e: " + actualResult.getWeek().getEndMillis());
		System.out.println("XStream expected dateMidnight:    " + testEvent.getDate());
		System.out.println("XStream   actual dateMidnight:    " + actualResult.getDate());
		System.out.println("XStream expected date:            " + testEvent.getDateTime());
		System.out.println("XStream   actual date:            " + actualResult.getDateTime());
		assertEquals("Henk", actualResult.getName());
		assertTrue(testEvent.getDate().equals(actualResult.getDate()));
		assertTrue(testEvent.getDateTime().equals(actualResult.getDateTime()));
		assertTrue(testEvent.getWeek().equals(actualResult.getWeek()));
	}

	@Test
	public void testSerializeAndDeserializeDomainEvent() {
		final TestEvent testEvent = new TestEvent("Henk");
		byte[] serializedEvent = testSubject.serialize(testEvent);
		TestEvent actualResult = testSubject.deserialize(serializedEvent);
		System.out.println("Protostuff expected iterval:      " + testEvent.getWeek() + " - s: "
				+ testEvent.getWeek().getStartMillis() + " - e: " + testEvent.getWeek().getEndMillis());
		System.out.println("Protostuff   actual iterval:      " + actualResult.getWeek() + " - s: "
				+ actualResult.getWeek().getStartMillis() + " - e: " + actualResult.getWeek().getEndMillis());
		System.out.println("Protostuff expected dateMidnight: " + testEvent.getDate());
		System.out.println("Protostuff   actual dateMidnight: " + actualResult.getDate());
		System.out.println("Protostuff expected date:         " + testEvent.getDateTime());
		System.out.println("Protostuff   actual date:         " + actualResult.getDateTime());
		assertEquals("Henk", actualResult.getName());
		assertTrue(testEvent.getDate().equals(actualResult.getDate()));
		assertTrue(testEvent.getDateTime().equals(actualResult.getDateTime()));
		assertTrue(testEvent.getWeek().equals(actualResult.getWeek()));
	}

	@Test
	public void testSerializeWithSpecialCharacters() {
		byte[] serializedEvent = testSubject.serialize(new TestEvent(SPECIAL__CHAR__STRING));
		TestEvent actualResult = testSubject.deserialize(serializedEvent);
		assertArrayEquals(SPECIAL__CHAR__STRING.getBytes(), actualResult.getName().getBytes());
	}

	@Test
	public void testJavaSerialization() throws IOException {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			byte[] serializedEvent = serialize(value);
		}
		printResult("Java Serialization", System.nanoTime(), start);
		printObject(serialize(value));
	}

	@Test
	public void testProtostuffSerialization() {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			byte[] serializedEvent = testSubject.serialize(value);
		}
		printResult("Protostuff Serialization", System.nanoTime(), start);
		printObject(testSubject.serialize(value));
	}

	@Test
	public void testXStreamSerialization() {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			byte[] serializedEvent = testReference.serialize(value);
		}
		printResult("XStream Serialization", System.nanoTime(), start);
		printObject(testReference.serialize(value));
	}

	@Test
	public void testJavaDeserialization() throws Exception {
		long start = System.nanoTime();
		byte[] serializedEvent = serialize(value);
		for (int i = 0; i < iterations; i++) {
			TestEvent event = deserialize(serializedEvent, TestEvent.class);
		}
		printResult("Java Deserialization", System.nanoTime(), start);
	}

	@Test
	public void testProtostuffDeserialization() {
		long start = System.nanoTime();
		byte[] serializedEvent = testSubject.serialize(value);
		for (int i = 0; i < iterations; i++) {
			TestEvent event = testSubject.deserialize(serializedEvent);
		}
		printResult("Protostuff Deserialization", System.nanoTime(), start);
	}

	@Test
	public void testXStreamDeserialization() {
		long start = System.nanoTime();
		byte[] serializedEvent = testReference.serialize(value);
		for (int i = 0; i < iterations; i++) {
			Object event = testReference.deserialize(serializedEvent);
		}
		printResult("XStream Deserialization", System.nanoTime(), start);
	}

	protected static double iterationTime(long delta, int iterations) {
		return (double) delta / (double) (iterations);
	}

	private byte[] serialize(Object instance) throws IOException {
		if (!Serializable.class.isInstance(instance)) {
			throw new IllegalArgumentException("Object must implements Serializable");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		try {
			oos.writeObject(instance);
		} finally {
			oos.close();
		}
		return baos.toByteArray();
	}

	private <T> T deserialize(byte[] serializedInstance, Class<T> expectedType) throws Exception {
		ObjectInputStream ois;
		ois = new ObjectInputStream(new ByteArrayInputStream(serializedInstance));
		return expectedType.cast(ois.readObject());
	}

	public static final class TestEvent implements Serializable {

		private static final long serialVersionUID = 1657550542124835062L;
		private String name;
		private DateMidnight date;
		private DateTime dateTime;
		private Period period;
		private Interval week;

		/**
		 * 
		 */
		protected TestEvent() {
			super();
		}

		public TestEvent(String name) {
			this.name = name;
			this.date = new DateMidnight();
			this.dateTime = new DateTime();
			this.period = new Period(100);
			this.week = new Interval(this.dateTime, this.dateTime.plus(Days.SEVEN));
		}

		public String getName() {
			return name;
		}

		public static long getSerialVersionUID() {
			return serialVersionUID;
		}

		public DateMidnight getDate() {
			return date;
		}

		public DateTime getDateTime() {
			return dateTime;
		}

		public Period getPeriod() {
			return period;
		}

		public Interval getWeek() {
			return week;
		}

	}

	private void printResult(String testName, long end, long start) {
		StringBuilder sb = new StringBuilder();
		final long duration = end - start;
		final double iterationTime = iterationTime(duration, iterations);
		sb.append("---------------------------------------------\n ").append(testName).append(" (").append(iterations)
				.append(" iterations)").append("\n---------------------------------------------\n").append(" Avg: ")
				.append(iterationTime).append(" nanos. Total: ").append(duration).append(" nanos.\n Avg: ")
				.append(BigDecimal.valueOf(iterationTime).longValue() / ProtostuffSerializerTest.MILLIS_TO_NANO)
				.append(" millis. Total: ").append(duration / ProtostuffSerializerTest.MILLIS_TO_NANO)
				.append(" millis.\n");
		System.out.println(sb.toString());
	}

	private void printObject(byte[] serialize) {
		StringBuilder sb = new StringBuilder();
		sb.append(" Object size: ").append(serialize.length).append(" bytes \n");
		System.out.println(sb.toString());

	}

}

