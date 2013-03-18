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

import java.util.List;
import java.util.Map;

/**
 * Abstract {@link ListMap} implementation that extends {@link CollectionHashMap}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class ListHashMap<K, V> extends CollectionHashMap<K, V, List<V>> implements ListMap<K, V>
{
	public ListHashMap()
	{
		super();
	}

	public ListHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public ListHashMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	public ListHashMap(Map<? extends K, ? extends List<V>> m)
	{
		super(m);
	}
}