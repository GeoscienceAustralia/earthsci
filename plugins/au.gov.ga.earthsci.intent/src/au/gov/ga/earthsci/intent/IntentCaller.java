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
	 * Called when the Intent handler has completed successfully. If the intent
	 * didn't produce a result, null should be passed.
	 * 
	 * @param result
	 *            Result of the Intent
	 */
	void completed(Object result);

	/**
	 * Called when the Intent handler failed with an error.
	 * 
	 * @param e
	 *            Error generated from handling the Intent
	 */
	void error(Exception e);
}
