package com.baidu.hsb.util;

/**
*
*
* @author xiongzhao@baidu.com
* @version $Id: LongUtil.java, v 0.1 2013年12月31日 下午1:38:20 HI:brucest0078 Exp $
private static final byte[] minValue = &quot;-9223372036854775808&quot;.getBytes();

public static byte[] toBytes(long i) {
if (i == Long.MIN_VALUE)

