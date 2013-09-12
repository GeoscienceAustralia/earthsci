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
package au.gov.ga.earthsci.ant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Helper class for storing a list of values for each key in a map.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <K>
 * @param <V>
 */
public class MultiMap<K, V> extends HashMap<K, List<V>>
{
	private static final long serialVersionUID = -4671082204090051141L;

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