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
package au.gov.ga.earthsci.core.raster;

import static org.gdal.gdalconst.gdalconstConstants.*;

import org.gdal.gdal.Band;

import au.gov.ga.earthsci.common.buffer.BufferType;

/**
 * Utility methods for working with GDAL rasters
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterUtil
{

	/**
	 * Retrieve the {@link BufferType} that matches the data type of the given
	 * GDAL raster {@link Band}.
	 * 
	 * @param band
	 *            The GDAL band to get the buffer type for
	 * 
	 * @return The buffer type for the given GDAL band
	 */
	public static BufferType getBufferType(Band band)
	{
		int dataType = band.getDataType();
		if (dataType == GDT_Byte)
		{
			return BufferType.BYTE;
		}
		else if (dataType == GDT_Int16)
		{
			return BufferType.SHORT;
		}
		else if (dataType == GDT_UInt16)
		{
			return BufferType.UNSIGNED_SHORT;
		}
		else if (dataType == GDT_Int32)
		{
			return BufferType.INT;
		}
		else if (dataType == GDT_UInt32)
		{
			return BufferType.UNSIGNED_INT;
		}
		else if (dataType == GDT_Float32 || dataType == GDT_CFloat32)
		{
			return BufferType.FLOAT;
		}
		else if (dataType == GDT_Float64 || dataType == GDT_CFloat64)
		{
			return BufferType.DOUBLE;
		}

		throw new UnsupportedOperationException("Band type " + dataType + " not supported"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
