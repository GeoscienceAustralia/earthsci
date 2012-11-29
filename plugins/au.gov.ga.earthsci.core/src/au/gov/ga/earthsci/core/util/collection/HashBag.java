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

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple Bag implementation backed by a HashMap.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HashBag<E> extends AbstractCollection<E> implements Bag<E>
{
	private final Map<E, Integer> map = new HashMap<E, Integer>();
	private int size = 0;

	@Override
	public boolean add(E e)
	{
		Integer count = map.get(e);
		if (count == null)
		{
			count = 0;
		}
		map.put(e, count + 1);
		size++;
		return true;
	}

	@Override
	public boolean remove(Object o)
	{
		Integer count = map.get(o);
		if (count == null)
		{
			return false;
		}

		//safe because it exists in the map:
		@SuppressWarnings("unchecked")
		E e = (E) o;

		if (count > 1)
		{
			map.put(e, count - 1);
		}
		else
		{
			map.remove(o);
		}
		size--;
		return true;
	}

	@Override
	public boolean contains(Object o)
	{
		return count(o) > 0;
	}

	@Override
	public int count(Object o)
	{
		Integer count = map.get(o);
		if (count == null)
		{
			return 0;
		}
		return count;
	}

	@Override
	public int countUnique()
	{
		return map.size();
	}

	@Override
	public Iterator<E> iterator()
	{
		return new HashBagIterator();
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public String toString()
	{
		return map.keySet().toString();
	}

	private class HashBagIterator implements Iterator<E>
	{
		private Iterator<E> keys;
		private E currentKey;
		private int index = 0;

		public HashBagIterator()
		{
			//have to create a copy to prevent ConcurrentModificationException when removing elements
			keys = new HashSet<E>(map.keySet()).iterator();
			if (keys.hasNext())
			{
				currentKey = keys.next();
			}
		}

		@Override
		public boolean hasNext()
		{
			return keys.hasNext() || index < count(currentKey);
		}

		@Override
		public E next()
		{
			while (index++ >= count(currentKey))
			{
				currentKey = keys.next();
				index = 0;
			}
			return currentKey;
		}

		@Override
		public void remove()
		{
			HashBag.this.remove(currentKey);
			index--;
		}
	}
}
