package de.ifcore.hdv.converter.data;

public class LongValue {

public static final LongValue NA = new LongValue(null, &quot;-&quot;);
private Long value;
private String orgValue;

private LongValue(Long value, String orgValue) {

