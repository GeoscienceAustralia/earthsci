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

import java.util.ArrayList;
import java.util.List;

/**
 * List of {@link ITreeChangeListener}s, for internal use by the
 * {@link ObservableListTreeSupport}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
class TreeChangeListenerList<E> implements ITreeChangeListener<E>
{
	private List<ITreeChangeListener<E>> listeners = new ArrayList<ITreeChangeListener<E>>();

	public void add(ITreeChangeListener<E> listener)
	{
		listeners.add(listener);
	}

	public void remove(ITreeChangeListener<E> listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void elementAdded(E element)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).elementAdded(element);
		}
	}

	@Override
	public void elementRemoved(E element)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).elementRemoved(element);
		}
	}
}
