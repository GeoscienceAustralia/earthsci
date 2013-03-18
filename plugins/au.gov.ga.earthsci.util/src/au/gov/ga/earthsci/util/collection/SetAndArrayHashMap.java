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

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract {@link SetAndArrayMap} implementation that extends {@link HashMap}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class SetAndArrayHashMap<K, V> extends CollectionHashMap<K, V, SetAndArray<V>> implements
		SetAndArrayMap<K, V>
{
	public SetAndArrayHashMap()
	{
		super();
	}

	public SetAndArrayHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public SetAndArrayHashMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	public SetAndArrayHashMap(Map<? extends K, ? extends SetAndArray<V>> m)
	{
		super(m);
	}
}
