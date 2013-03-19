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
package au.gov.ga.earthsci.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An abstract {@link CollectionMap} that uses a {@link TreeMap}, preserving the
 * natural ordering of keys.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class CollectionTreeMap<K, V, C extends Collection<V>> extends TreeMap<K, C> implements
		CollectionSortedMap<K, V, C>
{
	public CollectionTreeMap()
	{
		super();
	}

	public CollectionTreeMap(Comparator<? super K> comparator)
	{
		super(comparator);
	}

	public CollectionTreeMap(Map<? extends K, ? extends C> m)
	{
		super(m);
	}

	public CollectionTreeMap(SortedMap<K, ? extends C> m)
	{
		super(m);
	}

	protected abstract C createCollection(K key);

	@Override
	public void putSingle(K key, V value)
	{
		C values = get(key);
		if (values == null)
		{
			values = createCollection(key);
			put(key, values);
		}
		values.add(value);
	}

	@Override
	public boolean removeSingle(K key, V value)
	{
		Collection<V> values = get(key);
		if (values == null)
		{
			return false;
		}
		return values.remove(value);
	}

	@Override
	public int count(K key)
	{
		Collection<V> values = get(key);
		if (values == null)
		{
			return 0;
		}
		return values.size();
	}

	@Override
	public List<V> flatValues()
	{
		List<V> result = new ArrayList<V>();
		for (Collection<V> values : values())
		{
			result.addAll(values);
		}
		return result;
	}
}
