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
package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import java.net.URL;
import java.util.regex.Matcher;

/**
 * A {@link GocadReader} that reads header information from a Gocad group and returns
 * {@link GocadReaderParameters} that are updated to include the information from the group.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GocadGroupReader implements GocadReader<GocadReaderParameters>
{

	public static final String HEADER_REGEX = ".* HomogeneousGroup .*";
	public static final String END_REGEX = "BEGIN_MEMBERS";
	
	private GocadReaderParameters originalParams;
	private GocadReaderParameters newParams;
	
	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.originalParams = parameters;
		this.newParams = new GocadReaderParameters(parameters);
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher;
		if (!originalParams.isColorInformationAvailable())
		{
			matcher = solidColorPattern.matcher(line);
			if (matcher.matches())
			{
				newParams.setColor(GocadColor.gocadLineToColor(line));
				return;
			}
		}
	}

	@Override
	public GocadReaderParameters end(URL context)
	{
		return newParams;
	}

}
