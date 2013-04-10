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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Provides access to a retrieved result's data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrievalData
{
	/**
	 * @return The length of the content; or the value read from the
	 *         content-length response header. -1 if unknown.
	 */
	long getContentLength();

	/**
	 * @return The value read from the content-type response header, or null if
	 *         this is unavailable.
	 */
	String getContentType();

	/**
	 * Create an InputStream for reading the retrieved resource. The caller must
	 * close the returned InputStream once reading is complete.
	 * 
	 * @return An InputStream for reading the retrieved resource.
	 * @throws IOException
	 *             If an IO error occurs during resource reading.
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Return a ByteBuffer containing the retrieved resource. Some
	 * implementations create this buffer lazily when this method is called.
	 * <p/>
	 * For large resources, using {@link #getInputStream()} is preferred as the
	 * resource is not first loaded into memory.
	 * 
	 * @return A ByteBuffer containing the retrieved resource.
	 * @throws IOException
	 *             If an IO error occurs during resource reading.
	 */
	ByteBuffer getByteBuffer() throws IOException;

	/**
	 * Create a File object pointing at the retrieved resource.
	 * <p/>
	 * Using {@link #getInputStream()} is preferred. If you need a file from the
	 * retrieved resource, set the {@link IRetrievalProperties#isFileRequired()}
	 * property to true in the properties object passed to the
	 * {@link IRetrievalService#retrieve(Object, java.net.URL, IRetrievalProperties)}
	 * method.
	 * 
	 * @see IRetrievalProperties#isFileRequired()
	 * 
	 * @return File pointing at the retrieved resource.
	 */
	File getFile();
}
