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
 * Callback of an {@link Intent}, used to get a result from the handling of an
 * Intent. This called by an {@link IIntentHandler} to notify the caller of the
 * result.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IIntentCallback
{
	/**
	 * Called by the Intent handler when it has completed successfully. If the
	 * handler didn't produce a result, result will be null.
	 * <p/>
	 * If the result is of an unknown type, it should be handled gracefully,
	 * such as passing it to the dispatch system.
	 * <p/>
	 * It is possible for this to be called twice. For example, if the Intent
	 * handler loads a result from a cache, and then the cache is refreshed by
	 * retrieving a newer version, this will also be called with the newer
	 * result.
	 * 
	 * @param result
	 *            Result of the Intent
	 * @param intent
	 *            Intent that completed
	 */
	void completed(Object result, Intent intent);

	/**
	 * Called when the Intent handler failed with an error.
	 * 
	 * @param e
	 *            Error generated from handling the Intent
	 * @param intent
	 *            Intent that failed
	 */
	void error(Exception e, Intent intent);

	/**
	 * Called by the Intent handler when it is canceled.
	 * <p/>
	 * For example, could mean that a retrieval required to complete the intent
	 * was canceled.
	 * 
	 * @param intent
	 *            Intent whose handler was canceled
	 */
	void canceled(Intent intent);

	/**
	 * Called by the Intent handler when it is aborted.
	 * <p/>
	 * Generally means that the user canceled an interactive intent handler.
	 * This method should clean up any model changed, as if the Intent never
	 * occurred.
	 * 
	 * @param intent
	 *            Intent whose handler was aborted
	 */
	void aborted(Intent intent);
}
