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

import java.util.Iterator;

/**
 * Adapter for an {@link Iterator}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IteratorAdapter<E, A> implements Iterator<E>
{
	private final Iterator<A> iterator;
	private final IAdapter<E, A> adapter;

	public IteratorAdapter(Iterator<A> iterator, IAdapter<E, A> adapter)
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
}
