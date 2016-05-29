@Override
@SuppressWarnings(&quot;unchecked&quot;)
public T createInstance(T object)
{
if (object instanceof List)
return (T) new ArrayList<Object>();
if (object instanceof Set)
return (T) new HashSet<Object>();
return super.createInstance(object);
}
}

