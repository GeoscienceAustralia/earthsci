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
package au.gov.ga.earthsci.common.databinding;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;

/**
 * Class that observes a list property of an input, using an
 * {@link IObservableFactory} to create the observer. Also recusively observes
 * the list property of each of the input's list elements, and each of those
 * element's list elements, and so on.
 * <p/>
 * User can register {@link ITreeChangeListener}s to listen for changes to the
 * tree data structure.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ObservableListTreeSupport<E>
{
	private TreeChangeListenerList<E> listeners = new TreeChangeListenerList<E>();
	private final Map<E, IObservableList<E>> elementToObservable = new HashMap<E, IObservableList<E>>();
	private final IObservableFactory<E, IObservableList<E>> observableFactory;
	private E input;

	/**
	 * Constructor.
	 * 
	 * @param observableFactory
	 *            Factory that creates observers on a list property of
	 *            <code>E</code>.
	 */
	public ObservableListTreeSupport(final IObservableFactory<E, IObservableList<E>> observableFactory)
	{
		this.observableFactory = observableFactory;
	}

	/**
	 * Removes all observers from the input. Calls {@link #setInput(Object)}
	 * with a <code>null</code> argument.
	 */
	public void dispose()
	{
		setInput(null);
	}

	/**
	 * @return The root object being observed.
	 */
	public E getInput()
	{
		return input;
	}

	/**
	 * Set the root object to observe.
	 * <p/>
	 * All observers are removed from any previous input and its children, and
	 * the listeners are notified of the removals with
	 * {@link ITreeChangeListener#elementRemoved(Object)}.
	 * <p/>
	 * Observers are then added to the new input and any children, and the
	 * listeners are notified of the adds with
	 * {@link ITreeChangeListener#elementAdded(Object)}.
	 * <p/>
	 * Passing a <code>null</code> input means that all listens are removed from
	 * the previous input.
	 * 
	 * @param input
	 *            Input to observe
	 */
	public void setInput(E input)
	{
		if (this.input == input)
		{
			return;
		}
		if (this.input != null)
		{
			removed(this.input);
		}
		this.input = input;
		if (this.input != null)
		{
			added(this.input);
		}
	}

	/**
	 * Add a listener to listen to structural changes to the observed input
	 * hierarchy.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addListener(ITreeChangeListener<E> listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove an added listener.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	public void removeListener(ITreeChangeListener<E> listener)
	{
		listeners.remove(listener);
	}

	private void added(E input)
	{
		listeners.elementAdded(input);

		IObservableList<E> observable = observableFactory.createObservable(input);
		elementToObservable.put(input, observable);
		observable.addListChangeListener(listChangeListener);
		for (E element : observable)
		{
			added(element);
		}
	}

	private void removed(E input)
	{
		listeners.elementRemoved(input);

		IObservableList<E> observable = elementToObservable.remove(input);
		if (observable != null)
		{
			observable.removeListChangeListener(listChangeListener);
			for (E element : observable)
			{
				removed(element);
			}
			observable.dispose();
		}
	}

	private final IListChangeListener<E> listChangeListener = new IListChangeListener<E>()
	{
		@Override
		public void handleListChange(ListChangeEvent<? extends E> event)
		{
			for (ListDiffEntry<? extends E> entry : event.diff.getDifferences())
			{
				E element = entry.getElement();
				if (entry.isAddition())
				{
					added(element);
				}
				else
				{
					removed(element);
				}
			}
		}
	};
}
