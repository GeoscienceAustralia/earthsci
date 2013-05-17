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
package au.gov.ga.earthsci.common.color.io;

import java.io.IOException;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.INamed;

/**
 * An interface for classes that are able to read {@link ColorMap} instances
 * from an source object.
 * <p/>
 * Implementations may read from different source formats.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IColorMapReader extends INamed, IDescribed
{

	/**
	 * Return whether or not this reader supports the given source object
	 * 
	 * @param source
	 *            The source object to read colour maps from
	 * 
	 * @return <code>true</code> if this reader supports the given source;
	 *         <code>false</code> otherwise
	 */
	boolean supports(Object source);

	/**
	 * Read the colour map from the provided source
	 * 
	 * @param source
	 *            The source to read the colour map from
	 * 
	 * @return The new colour map read from the source
	 * 
	 * @throws IOException
	 *             If the source cannot be read from
	 */
	ColorMap read(Object source) throws IOException;

}
