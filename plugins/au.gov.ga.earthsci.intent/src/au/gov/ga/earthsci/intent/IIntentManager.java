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
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 *
 */
public interface IIntentManager
{
	/**
	 * Start the given Intent.
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
	 * Find an intent filter that best matches the given intent, or null if none
	 * could be found.
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
	 * @return Intent filter that matches the given intent
	 */
	IntentFilter findFilter(Intent intent);

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
