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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * An implementation of the {@link ListTreeMap} that uses an {@link ArrayList}
 * to store values for each key.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ArrayListTreeMap<K, V> extends ListTreeMap<K, V>
{
	public ArrayListTreeMap()
	{
		super();
	}

	public ArrayListTreeMap(Comparator<? super K> comparator)
	{
		super(comparator);
	}

	public ArrayListTreeMap(Map<? extends K, ? extends List<V>> m)
	{
		super(m);
	}

	public ArrayListTreeMap(SortedMap<K, ? extends List<V>> m)
	{
		super(m);
	}

	@Override
	protected ArrayList<V> createCollection(K key)
	{
		return new ArrayList<V>();
	}
}
