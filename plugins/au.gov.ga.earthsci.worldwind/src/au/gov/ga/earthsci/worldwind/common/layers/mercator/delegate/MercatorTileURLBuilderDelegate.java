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
package au.gov.ga.earthsci.worldwind.common.layers.mercator.delegate;

import gov.nasa.worldwind.avlist.AVList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileURLBuilderDelegate;

/**
 * {@link ITileURLBuilderDelegate} implementation that builds tile URLs for
 * certain mercator dataset servers. The delegate definition string can define a
 * url that contains placeholders which get replaced by tile parameters such as
 * the service url, dataset, level, row and column.
 * <p>
 * <code><Delegate>MercatorTileURLBuilder(skipLevels,{format})</Delegate></code>
 * <p>
 * <ul>
 * <li><code>skipLevels</code> = number of levels to skip in the source dataset</li>
 * <li>
 * <code>format</code> = URL format string containing tile parameter
 * placeholders. Must be surrounded by braces { }. The following placeholders
 * are supported:
 * <ul>
 * <li><code>%service%</code> = replaced with the service url</li>
 * <li><code>%dataset%</code> = replaced with the dataset name</li>
 * <li><code>%level%</code> = replaced with the tile's level</li>
 * <li><code>%row%</code> = replaced with the tile's row</li>
 * <li><code>%column%</code> = replaced with the tile's column</li>
 * </ul>
 * For example, <code>%service%%level%/%column%/%row%.png</code>, where
 * (service=http://a.tile.openstreetmap.org/, level=2, column=1, row=0), would
 * result in the following URL:
 * <code>http://a.tile.openstreetmap.org/2/1/0.png</code>.</li>
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MercatorTileURLBuilderDelegate implements ITileURLBuilderDelegate
{
	protected final static String DEFINITION_STRING = "MercatorTileURLBuilder";

	protected final static String SERVICE_PLACEHOLDER = "%service%";
	protected final static String DATASET_PLACEHOLDER = "%dataset%";
	protected final static String LEVEL_PLACEHOLDER = "%level%";
	protected final static String ROW_PLACEHOLDER = "%row%";
	protected final static String COLUMN_PLACEHOLDER = "%column%";

	protected final int skipLevels;
	protected final String format;

	@SuppressWarnings("unused")
	private MercatorTileURLBuilderDelegate()
	{
		this(3, "%service%%level%/%column%/%row%.png");
	}

	public MercatorTileURLBuilderDelegate(int skipLevels, String format)
	{
		this.skipLevels = skipLevels;
		this.format = format;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.startsWith(DEFINITION_STRING))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\d+),\\{([^}]+)\\}\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int skipLevels = Integer.parseInt(matcher.group(1));
				String format = matcher.group(2);
				return new MercatorTileURLBuilderDelegate(skipLevels, format);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + skipLevels + "," + format + ")";
	}

	@Override
	public URL getRemoteTileURL(IDelegatorTile tile, String imageFormat) throws MalformedURLException
	{
		int level = tile.getLevelNumber() + skipLevels;
		int column = tile.getColumn();
		int row = (1 << level) - 1 - tile.getRow(); //reverse the row integer (mercator rows are top to bottom)

		String url = format;
		url = url.replaceAll(SERVICE_PLACEHOLDER, tile.getService());
		url = url.replaceAll(DATASET_PLACEHOLDER, tile.getDataset());
		url = url.replaceAll(LEVEL_PLACEHOLDER, level + "");
		url = url.replaceAll(COLUMN_PLACEHOLDER, column + "");
		url = url.replaceAll(ROW_PLACEHOLDER, row + "");
		return new URL(url);
	}
}
