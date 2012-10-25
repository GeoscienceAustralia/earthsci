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
package au.gov.ga.earthsci.core.retrieve;

/**
 * Provides a mechanism for {@link IRetriever} instances to report progress of
 * a retrieval.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IRetrievalMonitor
{

	/**
	 * Retrieve the current status of the retrieval operation
	 * 
	 * @return The current retrieval status
	 */
	RetrievalStatus getRetrievalStatus();
	
	/**
	 * Notify that the retriever has started
	 */
	void notifyStarted();
	
	/**
	 * Notify that the retriever is connecting to the resource
	 */
	void notifyConnecting();
	
	/**
	 * Notify that the retriever has connected to the resource
	 */
	void notifyConnected();
	
	/**
	 * Notify that the retriever has begun reading the resource
	 */
	void notifyReading();
	
	/**
	 * Notify that the retriever has completed 
	 * 
	 * @param success Whether the retrieval was successful
	 */
	void notifyCompleted(boolean success);
}
