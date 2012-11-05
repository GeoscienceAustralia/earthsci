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
package au.gov.ga.earthsci.worldwind.common.util;

import java.util.Iterator;

/**
 * Provides the ability to create an {@link Iterable} of a subclass from an
 * {@link Iterable} of one of its superclasses.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 */
public class IterableProxy<E> implements Iterable<E>
{
	private Iterable<? extends E> source;

	public IterableProxy(Iterable<? extends E> source)
	{
		this.source = source;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new IteratorProxy<E>(source.iterator());
	}

	private class IteratorProxy<O> implements Iterator<O>
	{
		private Iterator<? extends O> iterator;

		public IteratorProxy(Iterator<? extends O> iterator)
		{
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext()
		{
			return iterator.hasNext();
		}

		@Override
		public O next()
		{
			return iterator.next();
		}

		@Override
		public void remove()
		{
			iterator.remove();
		}
	}
}
