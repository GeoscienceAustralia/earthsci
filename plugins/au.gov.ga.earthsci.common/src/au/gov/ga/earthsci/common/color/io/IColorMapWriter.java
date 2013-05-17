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
import java.io.OutputStream;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.INamed;

/**
 * An interface for classes that can write colour maps to a specific output
 * format
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public interface IColorMapWriter extends INamed, IDescribed
{

	/**
	 * Write the given colour map into the given output stream
	 * 
	 * @param map
	 *            The colour map to write
	 * @param stream
	 *            The output stream to write to
	 * @throws IOException
	 *             If the output stream cannot be written to
	 */
	void write(ColorMap map, OutputStream stream) throws IOException;

}
