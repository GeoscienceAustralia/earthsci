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
package au.gov.ga.earthsci.core.util.collection;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;

/**
 * {@link HashSet} {@link SetAndArray} implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HashSetAndArray<E> extends HashSet<E> implements SetAndArray<E>
{
	private E[] array;

	public HashSetAndArray()
	{
		super();
	}

	public HashSetAndArray(Collection<? extends E> c)
	{
		super(c);
	}

	public HashSetAndArray(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public HashSetAndArray(int initialCapacity)
	{
		super(initialCapacity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E[] getArray(Class<E> type)
	{
		if (array == null)
		{
			array = (E[]) Array.newInstance(type, size());
			int i = 0;
			for (E e : this)
			{
				array[i++] = e;
			}
		}
		return array;
	}

	@Override
	public boolean add(E e)
	{
		boolean added = super.add(e);
		if (added)
		{
			array = null;
		}
		return added;
	}

	@Override
	public boolean remove(Object o)
	{
		boolean removed = super.remove(o);
		if (removed)
		{
			array = null;
		}
		return removed;
	}
}
