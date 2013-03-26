/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.common.collection;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Abstract {@link ListMap} implementation that extends
 * {@link CollectionTreeMap}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class ListTreeMap<K, V> extends CollectionTreeMap<K, V, List<V>> implements ListSortedMap<K, V>
{
	public ListTreeMap()
	{
		super();
	}

	public ListTreeMap(Comparator<? super K> comparator)
	{
		super(comparator);
	}

	public ListTreeMap(Map<? extends K, ? extends List<V>> m)
	{
		super(m);
	}

	public ListTreeMap(SortedMap<K, ? extends List<V>> m)
	{
		super(m);
	}
}
