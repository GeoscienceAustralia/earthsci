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

import java.util.Collection;
import java.util.HashSet;

/**
 * {@link HashSet} {@link SetAndArray} implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HashSetAndArray<E> extends HashSet<E> implements SetAndArray<E>
{
	private Object[] array = new Object[0];

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
	public E[] getArray()
	{
		return (E[]) array;
	}

	@Override
	public boolean add(E e)
	{
		try
		{
			return super.add(e);
		}
		finally
		{
			array = toArray();
		}
	}

	@Override
	public boolean remove(Object o)
	{
		try
		{
			return super.remove(o);
		}
		finally
		{
			array = toArray();
		}
	}
}
