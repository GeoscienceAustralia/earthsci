package au.gov.ga.earthsci.ant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiMap<K, V> extends HashMap<K, List<V>>
{
	public void putSingle(K key, V value)
	{
		List<V> values = null;
		if (containsKey(key))
		{
			values = get(key);
		}
		else
		{
			values = new ArrayList<V>();
			put(key, values);
		}
		values.add(value);
	}
}