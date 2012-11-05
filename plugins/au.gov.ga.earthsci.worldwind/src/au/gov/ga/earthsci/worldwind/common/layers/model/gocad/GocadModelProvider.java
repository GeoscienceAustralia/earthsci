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

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import au.gov.ga.earthsci.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.earthsci.worldwind.common.layers.model.ModelLayer;
import au.gov.ga.earthsci.worldwind.common.layers.model.ModelProvider;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;

/**
 * Implementation of a {@link ModelProvider} which reads data from a GOCAD file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadModelProvider extends AbstractDataProvider<ModelLayer> implements ModelProvider
{
	private Sector sector = null;
	private final GocadReaderParameters parameters;

	public GocadModelProvider(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
	}

	@Override
	public Sector getSector()
	{
		return sector;
	}

	@Override
	protected boolean doLoadData(URL url, ModelLayer layer)
	{
		List<FastShape> shapes;
		File file = URLUtil.urlToFile(url);
		if (file.getName().endsWith(".zip"))
		{
			shapes = new ArrayList<FastShape>();

			try
			{
				URL context = new URL("jar:" + url.toExternalForm() + "!/");
				ZipFile zipFile = new ZipFile(file);

				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements())
				{
					ZipEntry entry = entries.nextElement();
					String suffix = WWIO.getSuffix(entry.getName());
					if (suffix == null)
					{
						continue;
					}
					if (GocadFactory.isGocadFileSuffix(suffix))
					{
						List<FastShape> tempList =
								GocadFactory.read(zipFile.getInputStream(entry), context, parameters);
						shapes.addAll(tempList);
					}
				}
				zipFile.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			shapes = GocadFactory.read(file, parameters);
		}
		if (shapes != null && !shapes.isEmpty())
		{
			for (FastShape shape : shapes)
			{
				layer.addShape(shape);
				if(sector == null)
				{
					sector = shape.getSector();
				}
				else
				{
					sector = sector.union(shape.getSector());
				}
			}
			return true;
		}
		return false;
	}
}
