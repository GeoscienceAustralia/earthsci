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
package au.gov.ga.earthsci.worldwind.common.terrain;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.IOUtil;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Elevation model that retrieves its elevation data from elevation tiles stored
 * in a directory in the local file system.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileElevationModel extends BoundedBasicElevationModel
{
	public FileElevationModel(Element domElement, AVList params)
	{
		super(getBasicElevationModelConfigParams(domElement, createURLBuilderParam(params)));
	}

	protected static AVList createURLBuilderParam(AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		params.setValue(AVKey.TILE_URL_BUILDER, new FileURLBuilder(context));

		return params;
	}

	/**
	 * TileUrlBuilder implementation that creates file:// URLs pointing to
	 * elevation tiles stored locally.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class FileURLBuilder implements TileUrlBuilder
	{
		private URL context;

		public FileURLBuilder(URL context)
		{
			this.context = context;
		}

		@Override
		public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
		{
			return Util.getLocalTileURL(tile.getLevel().getService(), tile.getLevel().getDataset(),
										tile.getLevelNumber(), tile.getRow(), tile.getColumn(), context, imageFormat, "bil");
		}
	}

	protected String getImageFormat()
	{
		AVList params = (AVList) getValue(AVKey.CONSTRUCTION_PARAMETERS);
		if (params != null)
		{
			return params.getStringValue(AVKey.IMAGE_FORMAT);
		}
		return null;
	}

	@Override
	protected void requestTile(TileKey key)
	{
		if (WorldWind.getTaskService().isFull())
		{
			return;
		}

		if (this.getLevels().isResourceAbsent(key))
		{
			return;
		}

		RequestTask request = new RequestTask(key, this);
		WorldWind.getTaskService().addTask(request);
	}

	@Override
	protected BufferWrapper readElevations(URL url) throws IOException
	{
		//overridden to handle unzipping the file if required

		if (!URLUtil.isForResourceWithExtension(url, "zip"))
		{
			return super.readElevations(url);
		}

		try
		{
			return IOUtil.readByteBuffer(url, this.getElevationDataType(), this.getElevationDataByteOrder());
		}
		catch (java.io.IOException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE, "ElevationModel.ExceptionReadingElevationFile", url.toString());
			throw e;
		}
	}

	/**
	 * This {@link RequestTask} creates elevation tile requests passing the
	 * image format returned by the getImageFormat() function. It also skips
	 * calling the downloadElevations() function, as the tiles are stored
	 * locally and don't need to be downloaded.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class RequestTask implements Runnable
	{
		protected final FileElevationModel elevationModel;
		protected final TileKey tileKey;

		protected RequestTask(TileKey tileKey, FileElevationModel elevationModel)
		{
			this.elevationModel = elevationModel;
			this.tileKey = tileKey;
		}

		@Override
		public final void run()
		{
			//modified to load tiles directly from ResourceURL instead of checking
			//cache and downloading non-existant tiles

			try
			{
				// check to ensure load is still needed
				if (elevationModel.areElevationsInMemory(tileKey))
				{
					return;
				}

				ElevationTile tile = elevationModel.createTile(tileKey);
				final URL url = tile.getResourceURL(elevationModel.getImageFormat());

				if (url != null && elevationModel.loadElevations(tile, url))
				{
					elevationModel.getLevels().unmarkResourceAbsent(tile);
					elevationModel.firePropertyChange(AVKey.ELEVATION_MODEL, null, this);
				}
				else
				{
					elevationModel.getLevels().markResourceAbsent(tile);
				}
			}
			catch (IOException e)
			{
				String msg = Logging.getMessage("ElevationModel.ExceptionRequestingElevations", tileKey.toString());
				Logging.logger().log(java.util.logging.Level.FINE, msg, e);
			}
		}

		@Override
		public final boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o == null || getClass() != o.getClass())
			{
				return false;
			}

			final RequestTask that = (RequestTask) o;

			if (this.tileKey != null ? !this.tileKey.equals(that.tileKey) : that.tileKey != null)
			{
				return false;
			}

			return true;
		}

		@Override
		public final int hashCode()
		{
			return (this.tileKey != null ? this.tileKey.hashCode() : 0);
		}

		@Override
		public final String toString()
		{
			return this.tileKey.toString();
		}
	}
}
