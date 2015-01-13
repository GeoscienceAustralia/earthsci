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

import java.util.ArrayList;

/**
 * Represents any object that loads data. Used to display loading indicators to
 * the user.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Loader
{
	/**
	 * @return Is this object loading its data?
	 */
	boolean isLoading();

	/**
	 * Add a listener to listen for load events.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	void addLoadingListener(LoadingListener listener);

	/**
	 * Remove a loading listener.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	void removeLoadingListener(LoadingListener listener);

	/**
	 * The listener interface for receiving load events.
	 */
	public static interface LoadingListener
	{
		void loadingStateChanged(Loader loader, boolean isLoading);
	}

	/**
	 * Simple helper class for creating lists of listeners and notifying them.
	 */
	public static class LoadingListenerList extends ArrayList<LoadingListener>
	{
		public void notifyListeners(Loader loader, boolean isLoading)
		{
			for (int i = size() - 1; i >= 0; i--)
				get(i).loadingStateChanged(loader, isLoading);
		}
	}
}
