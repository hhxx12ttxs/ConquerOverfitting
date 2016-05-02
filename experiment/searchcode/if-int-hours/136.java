package com.jitcaforwin.extended.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jitcaforwin.extended.api.track.Duration;

public class DurationTest {

	@Test
	public void testStringRepresentation1() {
		int days = 5;
		int hours = 14;
		int minutes = 1;
		int seconds = 30;

		Duration duration = new Duration(this.calculateSeconds(days, hours,
				minutes, seconds));
		assertTrue(duration.toString().equals("5:14:01:30"));
	}
	
	@Test
	public void testStringRepresentation2() {
		int days = 35;
		int hours = 0;
		int minutes = 41;
		int seconds = 1;

		Duration duration = new Duration(this.calculateSeconds(days, hours,
				minutes, seconds));
		assertTrue(duration.toString().equals("35:00:41:01"));
	}
	
	@Test
	public void testDays() {
		int days = 35;
		int hours = 12;
		int minutes = 41;
		int seconds = 1;

		Duration duration = new Duration(this.calculateSeconds(days, hours,
				minutes, seconds));
		assertTrue(equal(duration.getDays(),35.5f));
	}
	
	@Test
	public void testHours() {
		int days = 0;
		int hours = 12;
		int minutes = 41;
		int seconds = 1;

		Duration duration = new Duration(this.calculateSeconds(days, hours,
				minutes, seconds));
		assertTrue(equal(duration.getHours(), 12.7f));
	}
	

	
	@Test
	public void testMinutes() {
		int days = 0;
		int hours = 0;
		int minutes = 41;
		int seconds = 45;

		Duration duration = new Duration(this.calculateSeconds(days, hours,
				minutes, seconds));
		assertTrue(equal(duration.getMinutes(),41.75f));
	}
	
	private static boolean equal(float a, float b){
		if (a < b){
			float help = a;
			a = b;
			b = help;
		}
		return (a - b) < 0.0001f;
	}

	private long calculateSeconds(int days, int hours, int minutes, int seconds) {
		return ((((days * 24) + hours) * 60) + minutes) * 60 + seconds;
	}
}

