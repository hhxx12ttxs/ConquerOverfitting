package com.hahazql.util.format;


public class LongUtil
{
private static final byte[] minValue = &quot;-9223372036854775808&quot;.getBytes();

public static byte[] toBytes(long i) {
if (i == Long.MIN_VALUE)

