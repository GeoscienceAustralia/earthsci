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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ListMap} implementation that extends {@link HashMap} and uses
 * {@link ArrayList} values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ArrayListHashMap<K, V> extends ListHashMap<K, V>
{
	public ArrayListHashMap()
	{
		super();
	}

	public ArrayListHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public ArrayListHashMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	public ArrayListHashMap(Map<? extends K, ? extends List<V>> m)
	{
		super(m);
	}

	@Override
	protected List<V> createCollection(K key)
	{
		return new ArrayList<V>();
	}
}
