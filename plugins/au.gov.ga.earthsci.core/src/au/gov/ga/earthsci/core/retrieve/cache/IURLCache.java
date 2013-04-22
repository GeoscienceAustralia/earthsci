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
package au.gov.ga.earthsci.core.retrieve.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Cache used by the retrieval system.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IURLCache
{
	/**
	 * Has the given URL been retrieved partially?
	 * 
	 * @param url
	 *            URL to test
	 * @return True if the given URL has been partially retrieved.
	 */
	boolean isPartial(URL url);

	/**
	 * Length of the partial retrieval of the given URL. Returns 0L for unknown
	 * URLs.
	 * 
	 * @param url
	 *            URL to test
	 * @return Length of the partially retrieved resource.
	 */
	long getPartialLength(URL url);

	/**
	 * Last modification time of the partial retrieval. Returns 0L for unknown
	 * URLs.
	 * 
	 * @param url
	 *            URL to test
	 * @return Last modification of the partially retrieved resource.
	 */
	long getPartialLastModified(URL url);

	/**
	 * Create an OutputStream for writing a resource that is currently being
	 * retrieved. If the retrieval is a resumption of a partially retrieved
	 * resource, the offset defines the start point.
	 * <p/>
	 * The caller <b>must</b> close the OutputStream when it has finished using
	 * it.
	 * <p/>
	 * When the full resource has been retrieved,
	 * {@link #writeComplete(URL, long, String)} must be called to change the
	 * state of the resource from partially retrieved to complete. This must be
	 * called after the OutputStream is closed.
	 * 
	 * @param url
	 *            URL being retrieved
	 * @param offset
	 *            Offset within the resource at which to begin writing
	 * @return OutputStream for the caller to begin writing the resource to
	 * @throws IOException
	 *             If an error occurred while creating the OutputStream
	 */
	OutputStream writePartial(URL url, long offset) throws IOException;

	/**
	 * Mark the URL as complete. Must be called after calling
	 * {@link #writePartial(URL, long)}, after the retrieval is complete and the
	 * OutputStream has been closed.
	 * <p/>
	 * Returns true if the cache was updated with the completed retrieval data.
	 * A returned value of false means the cached version of the data is exactly
	 * the same as the retrieved version, and therefore the cached version was
	 * not updated.
	 * 
	 * @param url
	 *            URL completed
	 * @param lastModified
	 *            Value to use when setting the last modified property of the
	 *            cached resource
	 * @param Value
	 *            to use when setting the content-type property of the cached
	 *            resource
	 * @return True if the cache was updated with the completed retrieval data
	 */
	boolean writeComplete(URL url, long lastModified, String contentType);

	/**
	 * Does this cache contain a complete version of resource pointed to by the
	 * given URL?
	 * 
	 * @param url
	 *            URL to test
	 * @return True if this cache contains a completed resource for the URL
	 */
	boolean isComplete(URL url);

	/**
	 * Length of the completely retrieved resource. Returns 0L for URLs that
	 * aren't complete or don't exist in this cache.
	 * 
	 * @param url
	 *            URL to test
	 * @return Length of the retrieved resource
	 */
	long getLength(URL url);

	/**
	 * Last modified time of the completely retrieved resource. Returns 0L for
	 * URLs that aren't complete or don't exist in this cache.
	 * 
	 * @param url
	 *            URL to test
	 * @return Last modified time of the retrieved resource
	 */
	long getLastModified(URL url);

	/**
	 * Content type of the completely retrieved resource, specified in the
	 * {@link #writeComplete(URL, long, String)} function.
	 * 
	 * @param url
	 *            URL to test
	 * @return The content type of the retrieved resource
	 */
	String getContentType(URL url);

	/**
	 * Create an InputStream to read the completely retrieved resource.
	 * <p/>
	 * The caller <b>must</b> close the InputStream when it has finished using
	 * it.
	 * 
	 * @param url
	 *            URL to get the InputStream for
	 * @return InputStream used to read the resource
	 * @throws IOException
	 *             If an error occurs when creating the InputStream
	 */
	InputStream read(URL url) throws IOException;

	/**
	 * Create a File object that points to the completed retrieved resource.
	 * <p/>
	 * It is generally preferred to use the {@link #read(URL)} method over this
	 * one. Use this if a file is explicitly required; for example, to pass to
	 * an external library that requires File input.
	 * 
	 * @param url
	 *            URL to get the File for
	 * @return File containing the retrieved resource
	 */
	File getFile(URL url);
}
