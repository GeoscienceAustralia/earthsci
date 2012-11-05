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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.transform.URLTransformer;

/**
 * Extension of {@link BasicTiledImageLayer} which provides the default image
 * format as a parameter (F) to the texture URLs and also uses the
 * {@link URLTransformer} to transform the URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLTransformerBasicTiledImageLayer extends BasicTiledImageLayer
{
	public URLTransformerBasicTiledImageLayer(AVList params)
	{
		this(setupParams(params), false);
	}

	public URLTransformerBasicTiledImageLayer(Element domElement, AVList params)
	{
		this(domElement, setupParams(params), false);
	}

	/* The private constructors below are analogous to those above, except that
	 * params is never null (as the result from setupParams() is passed to the
	 * private constructor). This means that the constructor can pull out params
	 * set by the superclass from the params variable. */

	private URLTransformerBasicTiledImageLayer(AVList params, boolean ignore)
	{
		super(params);
		initBuilder(params);
	}

	private URLTransformerBasicTiledImageLayer(Element domElement, AVList params, boolean ignore)
	{
		super(domElement, params);
		initBuilder(params);
	}

	protected static AVList setupParams(AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		if (params.getValue(AVKey.TILE_URL_BUILDER) == null)
			params.setValue(AVKey.TILE_URL_BUILDER, createURLBuilder(params));

		return params;
	}

	protected void initBuilder(AVList params)
	{
		TileUrlBuilder builder = (TileUrlBuilder) params.getValue(AVKey.TILE_URL_BUILDER);
		if (builder != null && builder instanceof ExtendedUrlBuilder)
		{
			String imageFormat = (String) params.getValue(AVKey.IMAGE_FORMAT);
			((ExtendedUrlBuilder) builder).overrideFormat(imageFormat);
		}
	}

	protected static TileUrlBuilder createURLBuilder(AVList params)
	{
		return new ExtendedUrlBuilder();
	}

	protected static class ExtendedUrlBuilder implements TileUrlBuilder
	{
		private String imageFormat;

		public void overrideFormat(String imageFormat)
		{
			this.imageFormat = imageFormat;
		}

		@Override
		public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
		{
			String service = tile.getLevel().getService();
			if (service == null || service.length() < 1)
				return null;

			service = URLTransformer.transform(service);

			StringBuffer sb = new StringBuffer(service);
			if (sb.lastIndexOf("?") < 0)
				sb.append("?");
			else
				sb.append("&");

			sb.append("T=");
			sb.append(tile.getLevel().getDataset());
			sb.append("&L=");
			sb.append(tile.getLevel().getLevelName());
			sb.append("&X=");
			sb.append(tile.getColumn());
			sb.append("&Y=");
			sb.append(tile.getRow());

			String format = imageFormat != null ? imageFormat : this.imageFormat;
			if (format != null)
				sb.append("&F=" + format);

			return new URL(sb.toString());
		}
	}
}
