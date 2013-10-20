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
package au.gov.ga.earthsci.common.collection.adapter;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * {@link IAdapter} implementation that links adapted elements together using
 * two maps (one for each direction).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class IdentityHashMapAdapter<E, A> implements IAdapter<E, A>
{
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final Map<E, A> map1 = new IdentityHashMap();
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final Map<A, E> map2 = new IdentityHashMap();

	/**
	 * Adapt this value.
	 * 
	 * @param value
	 * @return Adapted value
	 */
	protected abstract A createTo(E value);

	/**
	 * Adapt this value.
	 * 
	 * @param value
	 * @return Adapted value
	 */
	protected abstract E createFrom(A value);

	@Override
	public final A adaptTo(E e)
	{
		synchronized (map1)
		{
			A a = map1.get(e);
			if (a == null)
			{
				a = createTo(e);
				insert(e, a);
			}
			return a;
		}
	}

	@Override
	public final E adaptFrom(A a)
	{
		synchronized (map1)
		{
			E e = map2.get(a);
			if (e == null)
			{
				e = createFrom(a);
				insert(e, a);
			}
			return e;
		}
	}

	protected void insert(E e, A a)
	{
		map1.put(e, a);
		map2.put(a, e);
	}
}
