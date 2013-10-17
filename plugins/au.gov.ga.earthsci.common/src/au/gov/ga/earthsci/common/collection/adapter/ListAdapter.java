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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Adapter for a list.
 * <p/>
 * Allows a list of one type to be used as a list of another type, using an
 * {@link IAdapter} to adapt between the elements.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ListAdapter<E, A> implements List<E>
{
	private final List<A> list;
	private final IAdapter<E, A> adapter;

	public ListAdapter(List<A> list, IAdapter<E, A> adapter)
	{
		this.list = list;
		this.adapter = adapter;
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			A a = adapter.adaptTo((E) o);
			return list.contains(a);
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (Object o : c)
		{
			if (!contains(o))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public Object[] toArray()
	{
		Object[] array = list.toArray();
		for (int i = 0; i < array.length; i++)
		{
			@SuppressWarnings("unchecked")
			E adapted = adapter.adaptFrom((A) array[i]);
			array[i] = adapted;
		}
		return array;
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		Object[] array = list.toArray();
		if (a.length > array.length)
		{
			a[array.length] = null; //according to method contract
		}
		else if (a.length < array.length)
		{
			@SuppressWarnings("unchecked")
			T[] t = (T[]) Array.newInstance(a.getClass().getComponentType(), array.length);
			a = t;
		}
		for (int i = 0; i < array.length; i++)
		{
			@SuppressWarnings("unchecked")
			T adapted = (T) adapter.adaptFrom((A) array[i]);
			a[i] = adapted;
		}
		return a;
	}

	@Override
	public boolean add(E e)
	{
		A a = adapter.adaptTo(e);
		return list.add(a);
	}

	@Override
	public void add(int index, E element)
	{
		A a = adapter.adaptTo(element);
		list.add(index, a);
	}

	@Override
	public boolean remove(Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			A a = adapter.adaptTo((E) o);
			return list.remove(a);
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	@Override
	public E remove(int index)
	{
		A a = list.remove(index);
		return adapter.adaptFrom(a);
	}

	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		for (E e : c)
		{
			add(e);
		}
		return !c.isEmpty();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		for (E e : c)
		{
			add(index++, e);
		}
		return !c.isEmpty();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean result = true;
		for (Object o : c)
		{
			result |= remove(o);
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		List<Object> retain = new ArrayList<Object>(c.size());
		for (Object o : c)
		{
			try
			{
				@SuppressWarnings("unchecked")
				A a = adapter.adaptTo((E) o);
				retain.add(a);
			}
			catch (ClassCastException e)
			{
				//ignore, won't be retained
			}
		}
		return list.retainAll(retain);
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public E get(int index)
	{
		A a = list.get(index);
		return adapter.adaptFrom(a);
	}

	@Override
	public E set(int index, E element)
	{
		A a = adapter.adaptTo(element);
		a = list.set(index, a);
		return adapter.adaptFrom(a);
	}

	@Override
	public int indexOf(Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			A adapted = adapter.adaptTo((E) o);
			return list.indexOf(adapted);
		}
		catch (ClassCastException e)
		{
			return -1;
		}
	}

	@Override
	public int lastIndexOf(Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			A adapted = adapter.adaptTo((E) o);
			return list.lastIndexOf(adapted);
		}
		catch (ClassCastException e)
		{
			return -1;
		}
	}

	@Override
	public Iterator<E> iterator()
	{
		return new IteratorAdapter<E, A>(list.iterator(), adapter);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return new ListIteratorAdapter<E, A>(list.listIterator(), adapter);
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		return new ListIteratorAdapter<E, A>(list.listIterator(index), adapter);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex)
	{
		List<A> subList = list.subList(fromIndex, toIndex);
		return new ListAdapter<E, A>(subList, adapter);
	}
}
