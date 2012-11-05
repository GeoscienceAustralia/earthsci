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
package au.gov.ga.earthsci.worldwind.common.downloader;

import gov.nasa.worldwind.retrieve.Retriever;

/**
 * Extension of the World Wind {@link Retriever} interface which provides
 * additional getter methods required for the {@link Downloader}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ExtendedRetriever extends Retriever
{
	/**
	 * Gets the exception if the attempted download resulted in an error.
	 * Returns null if the download was successful.
	 * 
	 * @return The download error
	 */
	public Exception getError();

	/**
	 * Was a NOT MODIFIED result returned from the server (ie a HTTP 304)? The
	 * not modified since date is read from the modification date of the cached
	 * version of the file.
	 * 
	 * @return True if the server returned a NOT MODIFIED status
	 */
	public boolean isNotModified();
}
