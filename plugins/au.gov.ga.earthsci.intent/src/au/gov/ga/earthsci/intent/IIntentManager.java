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
package au.gov.ga.earthsci.intent;

import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * Intent manager, responsible for maintaining a list of intent filters. Used to
 * start intents, or to find filter(s) that can handle intents.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IIntentManager
{
	/**
	 * Marks this intent manager as ready for execution. Any intent's started
	 * before calling this are queued. This will start all queued intents.
	 * <p/>
	 * This should be called after the application has started, so that no
	 * intents are started during application startup.
	 */
	void beginExecution();

	/**
	 * Start the given Intent. If the intent defines it's own handler, that
	 * handler is used. Otherwise the best matching filter's handler is used. If
	 * no filter is found, an exception is passed to the callback's
	 * {@link IIntentCallback#error(Exception, Intent)} method.
	 * 
	 * @param intent
	 *            Intent to start
	 * @param callback
	 *            Callback of the intent that is notified of intent completion
	 * @param context
	 *            Eclipse context in which to run the intent
	 */
	void start(Intent intent, IIntentCallback callback, IEclipseContext context);

	/**
	 * Start the given Intent. If the intent defines it's own handler, that
	 * handler is used. Otherwise the best matching filter's handler is used. If
	 * no filter is found, an exception is passed to the callback's
	 * {@link IIntentCallback#error(Exception, Intent)} method.
	 * 
	 * @param intent
	 *            Intent to start
	 * @param selectionPolicy
	 *            Policy to use for filter selection (null if none)
	 * @param showProgress
	 *            Whether to show a dismissable progress dialog while
	 *            determining the applicable intent filters
	 * @param callback
	 *            Callback of the intent that is notified of intent completion
	 * @param context
	 *            Eclipse context in which to run the intent
	 */
	void start(Intent intent, IIntentFilterSelectionPolicy selectionPolicy, boolean showProgress,
			IIntentCallback callback, IEclipseContext context);

	/**
	 * Add an intent filter.
	 * 
	 * @param filter
	 */
	void addFilter(IntentFilter filter);

	/**
	 * Remove an intent filter.
	 * 
	 * @param filter
	 */
	void removeFilter(IntentFilter filter);
}
