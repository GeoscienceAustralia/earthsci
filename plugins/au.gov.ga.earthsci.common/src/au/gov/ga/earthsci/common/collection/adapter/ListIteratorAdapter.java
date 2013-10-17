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

import java.util.ListIterator;

/**
 * Adapter for a {@link ListIterator}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ListIteratorAdapter<E, A> implements ListIterator<E>
{
	private final ListIterator<A> iterator;
	private final IAdapter<E, A> adapter;

	public ListIteratorAdapter(ListIterator<A> iterator, IAdapter<E, A> adapter)
	{
		this.iterator = iterator;
		this.adapter = adapter;
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public E next()
	{
		A a = iterator.next();
		return adapter.adaptFrom(a);
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}

	@Override
	public boolean hasPrevious()
	{
		return iterator.hasPrevious();
	}

	@Override
	public E previous()
	{
		A a = iterator.previous();
		return adapter.adaptFrom(a);
	}

	@Override
	public int nextIndex()
	{
		return iterator.nextIndex();
	}

	@Override
	public int previousIndex()
	{
		return iterator.previousIndex();
	}

	@Override
	public void set(E e)
	{
		A a = adapter.adaptTo(e);
		iterator.set(a);
	}

	@Override
	public void add(E e)
	{
		A a = adapter.adaptTo(e);
		iterator.add(a);
	}
}
