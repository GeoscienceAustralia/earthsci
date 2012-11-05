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
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.net.URL;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.FileLockSharer;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.IOUtil;

/**
 * {@link WMSBasicElevationModel} that uses the {@link FileLockSharer} to
 * create/share the fileLock object. This is so that multiple layers can point
 * and write to the same data cache name and synchronize with each other on the
 * same fileLock object. (Note: this has not yet been added to Bulk Download
 * facility).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SharedLockWMSBasicElevationModel extends BoundedWMSBasicElevationModel
{
	protected final Object fileLock;
	protected boolean extractZipEntry = false;

	public SharedLockWMSBasicElevationModel(Element domElement, AVList params)
	{
		this(wmsGetMoreParamsFromDocument(domElement, params));
	}

	public SharedLockWMSBasicElevationModel(AVList params)
	{
		super(params);

		Boolean b = (Boolean) params.getValue(AVKeyMore.EXTRACT_ZIP_ENTRY);
		if (b != null)
			this.setExtractZipEntry(b);

		fileLock = FileLockSharer.getLock(getLevels().getFirstLevel().getCacheName());
	}

	protected static AVList wmsGetMoreParamsFromDocument(Element domElement, AVList params)
	{
		params = wmsGetParamsFromDocument(domElement, params);

		XPath xpath = WWXML.makeXPath();
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.EXTRACT_ZIP_ENTRY, "ExtractZipEntry", xpath);

		return params;
	}

	public boolean isExtractZipEntry()
	{
		return extractZipEntry;
	}

	public void setExtractZipEntry(boolean extractZipEntry)
	{
		this.extractZipEntry = extractZipEntry;
	}

	@Override
	protected void downloadElevations(Tile tile, BasicElevationModel.DownloadPostProcessor postProcessor)
	{
		if (postProcessor == null)
			postProcessor = new DownloadPostProcessor(tile, this);

		super.downloadElevations(tile, postProcessor);
	}

	/**
	 * Extension to superclass' DownloadPostProcessor which returns this class'
	 * fileLock instead of the superclass'.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class DownloadPostProcessor extends BasicElevationModel.DownloadPostProcessor
	{
		private final SharedLockWMSBasicElevationModel em;

		public DownloadPostProcessor(Tile tile, SharedLockWMSBasicElevationModel em)
		{
			super(tile, em);
			this.em = em;
		}

		@Override
		protected Object getFileLock()
		{
			return em.fileLock;
		}
	}

	/* ***************************************************************************************************
	 * Below here is copied from BasicElevationModel, with some modifications to use the shared fileLock *
	 *************************************************************************************************** */

	@Override
	protected BufferWrapper readElevations(URL url) throws IOException
	{
		try
		{
			synchronized (this.fileLock)
			{
				return IOUtil.readByteBuffer(url, getElevationDataType(), getElevationDataByteOrder());
			}
		}
		catch (java.io.IOException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE, "ElevationModel.ExceptionReadingElevationFile",
					url.toString());
			throw e;
		}
	}

	@Override
	protected void writeConfigurationParams(AVList params, FileStore fileStore)
	{
		// Determine what the configuration file name should be based on the configuration parameters. Assume an XML
		// configuration document type, and append the XML file suffix.
		String fileName = DataConfigurationUtils.getDataConfigFilename(params, ".xml");
		if (fileName == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new WWRuntimeException(message);
		}

		// Check if this component needs to write a configuration file. This happens outside of the synchronized block
		// to improve multithreaded performance for the common case: the configuration file already exists, this just
		// need to check that it's there and return. If the file exists but is expired, do not remove it -  this
		// removes the file inside the synchronized block below.
		if (!this.needsConfigurationFile(fileStore, fileName, params, false))
			return;

		synchronized (this.fileLock)
		{
			// Check again if the component needs to write a configuration file, potentially removing any existing file
			// which has expired. This additional check is necessary because the file could have been created by
			// another thread while we were waiting for the lock.
			if (!this.needsConfigurationFile(fileStore, fileName, params, true))
				return;

			this.doWriteConfigurationParams(fileStore, fileName, params);
		}
	}

	@Override
	protected void retrieveRemoteElevations(final Tile tile,
			gov.nasa.worldwind.terrain.BasicElevationModel.DownloadPostProcessor postProcessor)
	{
		if (!this.isNetworkRetrievalEnabled())
		{
			this.getLevels().markResourceAbsent(tile);
			return;
		}

		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		java.net.URL url = null;
		try
		{
			url = tile.getResourceURL();
			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				this.getLevels().markResourceAbsent(tile);
				return;
			}
		}
		catch (java.net.MalformedURLException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					Logging.getMessage("TiledElevationModel.ExceptionCreatingElevationsUrl", url), e);
			return;
		}

		if (postProcessor == null)
			postProcessor = new DownloadPostProcessor(tile, this);
		URLRetriever retriever = new HTTPRetriever(url, postProcessor);
		//BEGIN MODIFICATION
		boolean formatContainsZip = getLevels().getFirstLevel().getFormatSuffix().toLowerCase().contains("zip");
		if (isExtractZipEntry() || !formatContainsZip)
		{
			retriever.setValue(URLRetriever.EXTRACT_ZIP_ENTRY, "true"); // supports legacy elevation models
		}
		//END MODIFICATION
		if (WorldWind.getRetrievalService().contains(retriever))
			return;

		WorldWind.getRetrievalService().runRetriever(retriever, 0d);
	}
}
