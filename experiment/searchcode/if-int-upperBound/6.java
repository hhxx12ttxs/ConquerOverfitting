
package vjl.core.tools;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import vjl.core.errors.EnforcementError;


public final class Enforcements
		extends Tools
{
	public static final void enforce (final boolean predicate)
			throws EnforcementError
	{
		if (!predicate)
			Enforcements.raiseEnforcementBroken ("Enforcement broken!");
	}
	
	public static final void succeeded (final boolean outcome)
	{
		if (!outcome)
			Enforcements.raiseEnforcementBroken ("The operation failed!");
	}
	
	public static final void identity (final Object first, final Object second)
	{
		if (first != second)
			Enforcements.raiseEnforcementBroken ("The given objects are not identical!");
	}
	
	public static final void enforce (final boolean predicate, final String message)
	{
		if (!predicate)
			Enforcements.raiseEnforcementBroken (message);
	}
	
	public static final File isFile (final File file)
			throws EnforcementError
	{
		if ((file == null) || (!file.isFile ()))
			Enforcements.raiseEnforcementBroken ("The given path is not a file!");
		return (file);
	}
	
	public static final void nul (final Object object)
	{
		if (object != null)
			Enforcements.raiseEnforcementBroken ("Null enforcement broken!");
	}
	
	public static final <_O_ extends Object> _O_ nonNull (final _O_ object)
			throws EnforcementError
	{
		if (object == null)
			Enforcements.raiseEnforcementBroken ("Non null enforcement broken!");
		return (object);
	}
	
	public static final <_O_ extends Object> _O_[] nonNullElements (final _O_[] array)
			throws EnforcementError
	{
		if (array == null)
			Enforcements.raiseEnforcementBroken ("Non null elements enforcement broken!");
		else
			for (final _O_ object : array)
				if (object == null)
					Enforcements.raiseEnforcementBroken ("Non null elements enforcement broken!");
		return (array);
	}
	
	public static final <_O_ extends Object> _O_[] count (final _O_[] array, int length)
			throws EnforcementError
	{
		if ((array == null) || (array.length != length))
			Enforcements.raiseEnforcementBroken ("Count enforcement broken!");
		return (array);
	}
	
	public static final int[] count (final int[] array, int length)
			throws EnforcementError
	{
		if ((array == null) || (array.length != length))
			Enforcements.raiseEnforcementBroken ("Count enforcement broken!");
		return (array);
	}
	
	public static final double[] count (final double[] array, int length)
			throws EnforcementError
	{
		if ((array == null) || (array.length != length))
			Enforcements.raiseEnforcementBroken ("Count enforcement broken!");
		return (array);
	}
	
	public static final byte[] size (final byte[] array, final int size)
			throws EnforcementError
	{
		if ((array == null) || (array.length != size))
			Enforcements.raiseEnforcementBroken ("Size enforcement broken!");
		return (array);
	}
	
	public static final int positive (final int number)
			throws EnforcementError
	{
		if (number <= 0)
			Enforcements.raiseEnforcementBroken ("Positive enforcement broken!");
		return (number);
	}
	
	public static final int positiveOrZero (final int number)
			throws EnforcementError
	{
		if (number < 0)
			Enforcements.raiseEnforcementBroken ("Positive or zero enforcement broken!");
		return (number);
	}
	
	public static final double positive (final double number)
			throws EnforcementError
	{
		if (number <= 0)
			Enforcements.raiseEnforcementBroken ("Positive enforcement broken!");
		return (number);
	}
	
	public static double between (final double value, final double lowerBound, final double upperBound)
			throws EnforcementError
	{
		if ((value < lowerBound) || (value > upperBound))
			Enforcements.raiseEnforcementBroken ("Between enforcement broken!");
		return (value);
	}
	
	public static final <_O_ extends Object> _O_ instance (final Object object, final Class<_O_> type)
			throws EnforcementError
	{
		if ((object == null) || (type == null) || !type.isInstance (object))
			Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		return ((_O_) object);
	}
	
	public static final <_O_ extends Object> _O_ instanceOrNull (final Object object, final Class<_O_> type)
			throws EnforcementError
	{
		if ((type == null) || ((object != null) && !type.isInstance (object)))
			Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		return ((_O_) object);
	}
	
	public static final <_O_ extends Object> _O_[] instances (final _O_[] objects, final Class<? extends _O_> type)
			throws EnforcementError
	{
		if ((objects == null) || (type == null))
			Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		else
			for (final _O_ object : objects)
				if ((object == null) || !type.isInstance (object))
					Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		return (objects);
	}
	
	public static final <_O_ extends Object> _O_[] instances (final _O_[] objects, final Class<? extends _O_>[] types)
			throws EnforcementError
	{
		if ((objects == null) || (types == null))
			Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		else {
			
			final int objectCount = objects.length;
			final int typeCount = types.length;
			
			if (objectCount != typeCount)
				Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
			else
				for (int index = (typeCount - 1); index >= 0; index--)
					if ((objects[index] == null) || (types[index] == null) || !types[index].isInstance (objects[index]))
						Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		}
		return (objects);
	}
	
	public static final <_O_ extends Object> _O_[] instancesOrNull (final _O_[] objects, final Class<? extends _O_>[] types)
			throws EnforcementError
	{
		if ((objects == null) || (types == null))
			Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		else {
			
			final int objectCount = objects.length;
			final int typeCount = types.length;
			
			if (objectCount != typeCount)
				Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
			else
				for (int index = (typeCount - 1); index >= 0; index--)
					if ((objects[index] != null) && ((types[index] == null) || !types[index].isInstance (objects[index])))
						Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		}
		return (objects);
	}
	
	public static final <_I_ extends Object, _O_ extends _I_> Class<_O_> extendsOrNull (final Class<_O_> clas, final Class<_I_> type)
			throws EnforcementError
	{
		if ((type == null) || ((clas != null) && !type.isAssignableFrom (clas)))
			Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		return (clas);
	}
	
	public static final <_I_ extends Object, _O_ extends _I_> Class<_O_> extendz (final Class<_O_> clas, final Class<_I_> type)
			throws EnforcementError
	{
		if ((type == null) || (clas == null) || !type.isAssignableFrom (clas))
			Enforcements.raiseEnforcementBroken ("Class enforcement broken!");
		return (clas);
	}
	
	public static final <_O_ extends Object> _O_ get (final _O_[] array, final int lowerBound, final int upperBound, final int index)
			throws EnforcementError
	{
		Enforcements.index (index, array, lowerBound, upperBound);
		return (array[index]);
	}
	
	public static final <_O_ extends Object> _O_ set (final _O_[] array, final int lowerBound, final int upperBound, final int index, final _O_ newValue)
			throws EnforcementError
	{
		Enforcements.index (index, array, lowerBound, upperBound);
		final _O_ oldValue = array[index];
		array[index] = newValue;
		return (oldValue);
	}
	
	public static final int index (final int index, final int size)
			throws EnforcementError
	{
		if (size <= 0)
			Enforcements.raiseEnforcementBroken ("Size is out of range!");
		if ((index < 0) || (index >= size))
			Enforcements.raiseEnforcementBroken ("Index is out of range!");
		return (index);
	}
	
	public static final int index (final int index, final int size, final int lowerBound, final int upperBound)
			throws EnforcementError
	{
		if (size < 0)
			Enforcements.raiseEnforcementBroken ("Size is out of range!");
		if ((lowerBound < 0) || (lowerBound >= size))
			Enforcements.raiseEnforcementBroken ("Lower bound is out of range!");
		if ((upperBound <= 0) || (upperBound > size))
			Enforcements.raiseEnforcementBroken ("Upper bound is out of range!");
		if (lowerBound >= upperBound)
			Enforcements.raiseEnforcementBroken ("Lower bound is greater than upper bound!");
		if ((index < lowerBound) || (index >= upperBound))
			Enforcements.raiseEnforcementBroken ("Index is out of range");
		return (index);
	}
	
	public static final int index (final int index, final Object[] array)
			throws EnforcementError
	{
		if (array == null)
			Enforcements.raiseEnforcementBroken ("Array is null!");
		else
			Enforcements.index (index, array.length);
		return (index);
	}
	
	public static final int index (final int index, final Object[] array, final int lowerBound, final int upperBound)
			throws EnforcementError
	{
		if (array == null)
			Enforcements.raiseEnforcementBroken ("Array is null!");
		else
			Enforcements.index (index, array.length, lowerBound, upperBound);
		return (index);
	}
	
	public static final byte[] buffer (final byte[] buffer)
			throws EnforcementError
	{
		if (buffer == null)
			Enforcements.raiseEnforcementBroken ("Buffer is null!");
		else
			if (buffer.length == 0)
				Enforcements.raiseEnforcementBroken ("Buffer is empty!");
		return (buffer);
	}
	
	public static final byte[] buffer (final byte[] buffer, final int offset, final int count)
			throws EnforcementError
	{
		if (buffer == null)
			Enforcements.raiseEnforcementBroken ("Buffer is null!");
		else {
			final int size = buffer.length;
			if (size == 0)
				Enforcements.raiseEnforcementBroken ("Buffer is empty!");
			if ((offset < 0) || (offset > size))
				Enforcements.raiseEnforcementBroken ("Offset is out of range!");
			if ((count <= 0) || ((offset + count) > size))
				Enforcements.raiseEnforcementBroken ("Count is out of range!");
		}
		return (buffer);
	}
	
	public static final void raiseEnforcementBroken (final String message)
			throws EnforcementError
	{
		throw (new EnforcementError (message));
	}
	
	public static final void raiseEnforcementBroken ()
			throws EnforcementError
	{
		throw (new EnforcementError ("Unknown enforcement broken!"));
	}
	
	public static final String uuid (final String uuid)
			throws EnforcementError
	{
		if (uuid == null)
			Enforcements.raiseEnforcementBroken ("Uuid is null!");
		try {
			UUID.fromString (uuid);
		} catch (final Throwable error) {
			Enforcements.raiseEnforcementBroken ("Uuid is invalid!");
		}
		return (uuid);
	}
	
	public static final int ipPort (final int port)
	{
		if ((port < 0) || (port > Short.MAX_VALUE))
			Enforcements.raiseEnforcementBroken ("IP port is invalid!");
		return (port);
	}
	
	public static final int ip4Address (final int address)
	{
		return (address);
	}
	
	public static final byte[] ip4Address (final byte[] address)
	{
		if ((address == null) || (address.length != 4))
			Enforcements.raiseEnforcementBroken ("IP4 address is invalid!");
		return (address);
	}
	
	public static final int ip4AddressPart (final int part)
	{
		if ((part < 0) || (part > 255))
			Enforcements.raiseEnforcementBroken ("IP address part is invalid!");
		return (part);
	}
	
	public static final long macAddress (final long address)
	{
		if ((address < 0) || (address > 0xffffffffffffl))
			Enforcements.raiseEnforcementBroken ("MAC address is invalid!");
		return (address);
	}
	
	public static final
			<_Key_ extends Object>
	void excludesKey (
			final Map<_Key_, ?> map,
			final _Key_ key)
	{
		if ((map == null) || map.containsKey (key))
			Enforcements.raiseEnforcementBroken ("Map contains key!");
	}
	
	public static final
			<_Element_ extends Object>
	void excludes (
			final Collection<_Element_> collection,
			final _Element_ element)
	{
		if ((collection == null) || collection.contains (element))
			Enforcements.raiseEnforcementBroken ("Collection contains element!");
	}
}

