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

import java.util.List;

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
	 * handler is used. Otherwise the given filter's handler is used.
	 * 
	 * @param intent
	 *            Intent to start
	 * @param filter
	 *            Filter whose handler will handle the intent (unless the intent
	 *            defines it's own handler)
	 * @param callback
	 *            Callback of the intent that is notified of intent completion
	 * @param context
	 *            Eclipse context in which to run the intent
	 */
	void start(Intent intent, IntentFilter filter, IIntentCallback callback, IEclipseContext context);

	/**
	 * Start the given Intent, using the given handler. The intent's own
	 * handler, if defined, is ignored.
	 * 
	 * @param intent
	 *            Intent to start
	 * @param handlerClass
	 *            Handler to handle the intent
	 * @param callback
	 *            Callback of the intent that is notified of intent completion
	 * @param context
	 *            Eclipse context in which to run the intent
	 */
	void start(Intent intent, Class<? extends IIntentHandler> handlerClass, IIntentCallback callback,
			IEclipseContext context);

	/**
	 * Find the best intent filter that matches the given intent. Returns null
	 * if none could be found.
	 * 
	 * @param intent
	 *            Intent to find a filter for
	 * @return Intent filter that matches the given intent
	 * @see IIntentManager#findFilters(Intent)
	 */
	IntentFilter findFilter(Intent intent);

	/**
	 * Find the intent filters that match the given intent. Returns an empty
	 * list if none could be found.
	 * <p/>
	 * The best match is defined as follows:
	 * <ul>
	 * <li>If the intent defines an expected return type, any filters that
	 * define that return type are preferred over those that don't</li>
	 * <li>If the intent defines a content type, the filters that define a
	 * content type closer to the intent's content type are preferred</li>
	 * <li>Otherwise the first matching filter is returned</li>
	 * </ul>
	 * 
	 * @param intent
	 *            Intent to find a filter for
	 * @return Intent filters that match the given intent
	 */
	List<IntentFilter> findFilters(Intent intent);

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
