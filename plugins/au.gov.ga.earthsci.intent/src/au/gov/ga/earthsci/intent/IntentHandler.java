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

/**
 * Represents a handler for an intent. This is an implementation that actually
 * performs the {@link Intent}'s action. Usually associated with an
 * {@link IntentFilter} for lookup, but can also be associated directly with an
 * {@link Intent}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IntentHandler
{
	/**
	 * Handle this intent. Must notify the caller when completed (or failed).
	 * 
	 * @param intent
	 *            Intent to handle.
	 * @param caller
	 *            Caller to notify of completion.
	 */
	void handle(Intent intent, IntentCaller caller);
}
