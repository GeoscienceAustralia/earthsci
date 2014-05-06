/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.stars;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that converts the ASCII version of the Yale Bright Star Catalog
 * data file to a binary format used by the {@link InfiniteStarsLayer}.
 * <p/>
 * The data file is the <code>bsc5.dat</code> file available <a
 * href="http://tdc-www.harvard.edu/catalogs/bsc5.html">here</a> or <a
 * href="http://cdsarc.u-strasbg.fr/viz-bin/Cat?cat=V%2F50">here</a>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BSC5Converter
{
	public static void main(String[] args) throws IOException
	{
		InputStream is = InfiniteStarsLayer.class.getResourceAsStream("catalog"); //$NON-NLS-1$
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("stars.dat")); //$NON-NLS-1$
		Pattern spectralPattern = Pattern.compile("([A-Z][0-9.]+)(?:[0-9.-]+)?([IV]+)?.*"); //$NON-NLS-1$

		String line;
		while ((line = reader.readLine()) != null)
		{
			try
			{
				float longitude = Float.valueOf(line.substring(90, 96));
				float latitude = Float.valueOf(line.substring(96, 102));
				float magnitude = Float.valueOf(line.substring(102, 107));
				String spectral = line.substring(129, 147);

				Color color = null;
				try
				{
					float bv = Float.valueOf(line.substring(109, 114));
					//float ub = Float.valueOf(line.substring(115, 120));
					color = StarColor.bvToColor(bv);
				}
				catch (Exception e)
				{
					Matcher matcher = spectralPattern.matcher(spectral);
					if (matcher.matches())
					{
						String type = matcher.group(1);
						String classs = matcher.group(2);
						color = StarColor.typeClassToColor(type, classs);
						if (color == null)
						{
							classs = "V"; //$NON-NLS-1$
							color = StarColor.typeClassToColor(type, classs);
						}
						if (color == null)
						{
							classs = "IV"; //$NON-NLS-1$
							color = StarColor.typeClassToColor(type, classs);
						}
						if (color == null)
						{
							classs = "III"; //$NON-NLS-1$
							color = StarColor.typeClassToColor(type, classs);
						}
					}
				}
				if (color == null)
				{
					throw new Exception("Color could not be determined"); //$NON-NLS-1$
				}

				os.writeFloat(longitude);
				os.writeFloat(latitude);
				os.writeFloat(magnitude);
				os.writeInt(color.getRGB());
			}
			catch (Exception e)
			{
				//ignore
			}
		}

		os.flush();
		os.close();
	}
}
