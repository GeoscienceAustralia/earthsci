/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * {@link HashMap} subclass that supports adding multiple values for a single
 * key.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <K>
 *            Key class
 * @param <V>
 *            Value class
 */
public class MultiMap<K, V> extends HashMap<K, List<V>>
{
	/**
	 * Put a single key/value pair into this map.
	 * 
	 * @param key
	 * @param value
	 */
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

	/**
	 * Remove a single key/value pair from this map if it exists.
	 * 
	 * @param key
	 * @param value
	 * @return True if the key/value pair was found and removed, false
	 *         otherwise.
	 */
	public boolean removeSingle(K key, V value)
	{
		if (containsKey(key))
		{
			List<V> values = get(key);
			return values.remove(value);
		}
		return false;
	}
	
	/**
	 * Return the number of entries stored for the given key
	 * 
	 * @param key
	 * 
	 * @return The number of entries stored for the given key
	 */
	public int count(K key)
	{
		if (!containsKey(key))
		{
			return 0;
		}
		return get(key).size();
	}
}
