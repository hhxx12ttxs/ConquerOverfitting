package ca.szc.keratin.bot.util;

public abstract class CachedValue<T>
{
private long lastUpdateMillis;

private long expiryMillis = expiryMillis();
public synchronized T getValue()
{
if ( cachedValue == null || System.currentTimeMillis() >= lastUpdateMillis + expiryMillis )

