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
 * The caller of the {@link Intent}, used to get a result from the handling of
 * an Intent. This called by an {@link IntentHandler} to notify the caller of
 * the result.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IntentCaller
{
	/**
	 * Called by the Intent handler when it has completed successfully. If the
	 * handler didn't produce a result, result will be null.
	 * <p/>
	 * If the result is of an unknown type, it should be handled gracefully,
	 * such as passing it to the dispatch system.
	 * 
	 * @param intent
	 *            Intent that completed
	 * @param result
	 *            Result of the Intent
	 */
	void completed(Intent intent, Object result);

	/**
	 * Called when the Intent handler failed with an error.
	 * 
	 * @param intent
	 *            Intent that failed
	 * @param e
	 *            Error generated from handling the Intent
	 */
	void error(Intent intent, Exception e);
}
