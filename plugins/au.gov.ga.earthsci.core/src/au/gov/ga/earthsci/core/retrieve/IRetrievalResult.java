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

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Represents the result of a resource retrieval.
 * <p/>
 * Contains methods for accessing the result in a variety of useful
 * ways.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IRetrievalResult
{

	/**
	 * Returns whether there is any data available in this result.
	 * <p/>
	 * Typically there will always be data available if {@link #isSuccessful()} returns <code>true</code>, 
	 * but this is not guaranteed and client code should be able to handle that scenario. 
	 * 
	 * @return <code>true</code> if data is available in this result.
	 */
	boolean hasData();
	
	/**
	 * Get the result content as a byte buffer.
	 * 
	 * @return The result content as a byte buffer
	 */
	ByteBuffer getAsBuffer();
	
	/**
	 * Get the result content as a String.
	 * 
	 * @return The result content as a String
	 */
	String getAsString();
	
	/**
	 * Get the result content as an input stream.
	 * 
	 * @return The result content as an input stream
	 */
	InputStream getAsInputStream();
	
	/**
	 * Return whether this result represents a successful retrieval or not
	 * 
	 * @return <code>true</code> if this result represents a successful retrieval; <code>false</code> otherwise.
	 */
	boolean isSuccessful();
	
	/**
	 * Get any exception associated with this result.
	 * <p/>
	 * Generally <code>null</code> if {@link #isSuccessful()} returns <code>true</code>.
	 * 
	 * @return Any exception associated with this result.
	 */
	Exception getException();

	/**
	 * Return a user friendly (human readable) error message in the case of an unsuccessful result.
	 * 
	 * @return A user friendly message to describe any problems with the result
	 */
	String getMessage();
}
