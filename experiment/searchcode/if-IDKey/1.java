package org.apache.commons.lang3.builder;


final class IDKey
{

public IDKey(Object obj)
{
id = System.identityHashCode(obj);
IDKey idkey;
if(obj instanceof IDKey)
if(id == (idkey = (IDKey)obj).id &amp;&amp; value == idkey.value)

