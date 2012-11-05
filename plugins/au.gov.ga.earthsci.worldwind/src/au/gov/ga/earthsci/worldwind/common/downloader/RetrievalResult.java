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

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Represents a result from a download.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface RetrievalResult
{
	/**
	 * Source URL from which this result was downloaded.
	 * 
	 * @return Source URL
	 */
	public URL getSourceURL();

	/**
	 * Does this download result have any data (ie was it successful)?
	 * 
	 * @return true if this result has data
	 */
	public boolean hasData();

	/**
	 * Get the downloaded data as a ByteBuffer.
	 * 
	 * @return ByteBuffer containing the downloaded data
	 */
	public ByteBuffer getAsBuffer();

	/**
	 * Get the downloaded data as a String.
	 * 
	 * @return String representation of the downloaded data
	 */
	public String getAsString();

	/**
	 * Get the downloaded data as an InputStream.
	 * 
	 * @return Downloaded data wrapped in an InputStream
	 */
	public InputStream getAsInputStream();

	/**
	 * Was this download result retrieved from the cache?
	 * 
	 * @return True if this result was from the cache
	 */
	public boolean isFromCache();

	/**
	 * Was a NOT MODIFIED result returned from the server (ie a HTTP 304)? The
	 * not modified since date is read from the modification date of the cached
	 * version of the file.
	 * 
	 * @return True if the server returned a NOT MODIFIED status
	 */
	public boolean isNotModified();

	/**
	 * Get the content type of the downloaded data.
	 * 
	 * @return The content type of the downloaded data returned by the server,
	 *         or null if the file was not retrieved from a http url.
	 */
	public String getContentType();

	/**
	 * Gets the exception if the attempted download resulted in an error.
	 * Returns null if the download was successful.
	 * 
	 * @return The download error
	 */
	public Exception getError();
}
