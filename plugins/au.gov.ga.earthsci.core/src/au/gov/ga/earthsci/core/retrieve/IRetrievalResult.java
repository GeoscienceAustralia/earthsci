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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Result of a {@link IRetrieval} after the retrieval is completed.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrievalResult
{
	/**
	 * Create an InputStream for reading the retrieved resource.
	 * <p/>
	 * Can return null if the retrieval was unsuccessful.
	 * 
	 * @return An InputStream for reading the retrieved resource.
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Return a ByteBuffer containing the retrieved resource. Some
	 * implementations create this buffer lazily when this method is called. For
	 * large resources, using {@link #getInputStream()} is sometimes preferred
	 * as the resource is not first loaded into memory.
	 * <p/>
	 * Can return null if the retrieval was unsuccessful.
	 * 
	 * @return A ByteBuffer containing the retrieved resource.
	 */
	ByteBuffer getByteBuffer() throws IOException;

	/**
	 * @return The Exception thrown during resource retrieval if there was an
	 *         error.
	 */
	Exception getError();
}
