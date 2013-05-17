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

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Writes a {@link ColorMap} to a compact string format:
 * <p/>
 * <code>
 * name|&lt;description&gt;|&lt;mode&gt;|percentage_based ? 1:0|[val,rgb]*
 * </code>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class CompactStringColorMapWriter implements IColorMapWriter
{

	@Override
	public String getName()
	{
		return Messages.CompactStringColorMapWriter_WriterName;
	}

	@Override
	public String getDescription()
	{
		return Messages.CompactStringColorMapWriter_WriterDescription;
	}

	@Override
	public void write(ColorMap map, OutputStream stream) throws IOException
	{
		Validate.notNull(map, "A ColorMap is required"); //$NON-NLS-1$
		Validate.notNull(stream, "An output stream is required"); //$NON-NLS-1$

		StringBuilder builder = new StringBuilder();

		builder.append(map.getName());
		builder.append('|');
		if (!Util.isBlank(map.getDescription()))
		{
			builder.append(map.getDescription());
		}
		builder.append('|');
		builder.append(map.getMode().name());
		builder.append('|');
		builder.append(map.isPercentageBased() ? 1 : 0);
		builder.append('|');
		if (map.getNodataColour() != null)
		{
			builder.append(map.getNodataColour().getRGB());
		}
		builder.append('|');
		int count = 0;
		for (Entry<Double, Color> entry : map.getEntries().entrySet())
		{
			if (count > 0)
			{
				builder.append(',');
			}
			builder.append(entry.getKey()).append(',').append(entry.getValue().getRGB());
			count++;
		}

		stream.write(builder.toString().getBytes());
	}

}
