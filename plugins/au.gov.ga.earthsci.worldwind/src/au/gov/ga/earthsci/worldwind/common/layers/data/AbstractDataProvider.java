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
package au.gov.ga.earthsci.worldwind.common.layers.data;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.retrieve.AbstractRetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.retriever.PassThroughZipRetriever;

/**
 * Basic implementation of the {@link DataProvider} interface. Handles
 * retrieving the data from the layer's url, and once downloaded, calls an
 * abstract method which loads the data. Also handles caching the data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractDataProvider<L extends DataLayer> implements DataProvider<L>
{
	private final Object readLock = new Object();
	private boolean reading = false;
	private boolean loading = false;
	private boolean loaded = false;
	private FileStore dataFileStore = WorldWind.getDataFileStore();
	private final Object fileLock = new Object();
	private final LoadingListenerList loadingListeners = new LoadingListenerList();

	@Override
	public void requestData(L layer)
	{
		synchronized (readLock)
		{
			if (!loaded && !reading)
			{
				RequestTask task = new RequestTask(this, layer);
				if (!WorldWind.getTaskService().isFull())
				{
					loading = true;
					loadingListeners.notifyListeners(isLoading());
					WorldWind.getTaskService().addTask(task);
				}
			}
		}
	}

	/**
	 * Is the cached file expired (download time is earlier than layer's last
	 * update time)?
	 * 
	 * @param layer
	 * @param fileURL
	 * @param fileStore
	 * @return True if the file has expired
	 */
	protected boolean isFileExpired(L layer, URL fileURL, FileStore fileStore)
	{
		if (!WWIO.isFileOutOfDate(fileURL, layer.getExpiryTime()))
			return false;

		// The file has expired. Delete it.
		fileStore.removeFile(fileURL);
		String message = Logging.getMessage("generic.DataFileExpired", fileURL);
		Logging.logger().fine(message);
		return true;
	}

	public FileStore getDataFileStore()
	{
		return this.dataFileStore;
	}

	public Object getFileLock()
	{
		return fileLock;
	}

	public void setDataFileStore(FileStore fileStore)
	{
		if (fileStore == null)
		{
			String message = Logging.getMessage("nullValue.FileStoreIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		this.dataFileStore = fileStore;
	}

	/**
	 * Downloads the data file for the layer.
	 * 
	 * @param layer
	 * @param postProcessor
	 */
	protected void downloadFile(L layer, RetrievalPostProcessor postProcessor)
	{
		if (!layer.isNetworkRetrievalEnabled())
			return;

		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		URL url;
		try
		{
			url = layer.getUrl();
			if (url == null)
				return;

			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
				return;
		}
		catch (MalformedURLException e)
		{
			String message = "Exception creating data URL";
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return;
		}

		Retriever retriever;

		if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()))
		{
			if (postProcessor == null)
				postProcessor = new DownloadPostProcessor(this, layer);
			retriever = new PassThroughZipRetriever(url, postProcessor);
		}
		else
		{
			Logging.logger().severe(Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", url.toString()));
			return;
		}

		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(layer, AVKey.URL_CONNECT_TIMEOUT);
		if (cto != null && cto > 0)
			retriever.setConnectTimeout(cto);
		Integer cro = AVListImpl.getIntegerValue(layer, AVKey.URL_READ_TIMEOUT);
		if (cro != null && cro > 0)
			retriever.setReadTimeout(cro);
		Integer srl = AVListImpl.getIntegerValue(layer, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
			retriever.setStaleRequestLimit(srl);

		WorldWind.getRetrievalService().runRetriever(retriever);
	}

	/**
	 * Load the data from the file pointed to by the url. Delegates the loading
	 * to the subclass.
	 * 
	 * @param url
	 * @param layer
	 * @return True if the data was loaded successfully
	 */
	protected boolean loadData(URL url, L layer)
	{
		synchronized (readLock)
		{
			reading = true;
		}

		//this is potentially a long operation
		synchronized (getFileLock())
		{
			if (!loaded)
			{
				loaded = doLoadData(url, layer);
			}
		}

		synchronized (readLock)
		{
			loading = false;
			loadingListeners.notifyListeners(isLoading());
			reading = false;
		}

		return loaded;
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		loadingListeners.add(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		loadingListeners.remove(listener);
	}

	/**
	 * Load the data from the file pointed to by the url.
	 * 
	 * @param url
	 * @param layer
	 * @return True if the data was loaded successfully
	 */
	protected abstract boolean doLoadData(URL url, L layer);

	/**
	 * {@link RetrievalPostProcessor} used when downloading the data.
	 */
	protected class DownloadPostProcessor extends AbstractRetrievalPostProcessor
	{
		protected final AbstractDataProvider<L> provider;
		protected final L layer;

		public DownloadPostProcessor(AbstractDataProvider<L> provider, L layer)
		{
			this.provider = provider;
			this.layer = layer;
		}

		@Override
		protected Object getFileLock()
		{
			return provider.getFileLock();
		}

		@Override
		protected File doGetOutputFile()
		{
			return provider.getDataFileStore().newFile(layer.getDataCacheName());
		}
	}

	/**
	 * Task which downloads and/or loads the data.
	 */
	private class RequestTask implements Runnable
	{
		private final AbstractDataProvider<L> provider;
		private final L layer;

		private RequestTask(AbstractDataProvider<L> provider, L layer)
		{
			this.provider = provider;
			this.layer = layer;
		}

		@Override
		public void run()
		{
			String dataCacheName = layer.getDataCacheName();

			//first check if the layer URL is pointing to a local file (has file:// protocol)
			URL fileUrl = null;
			try
			{
				URL url = layer.getUrl();
				if ("file".equalsIgnoreCase(url.getProtocol()))
				{
					fileUrl = url;
				}
			}
			catch (MalformedURLException e)
			{
			}

			if (fileUrl != null)
			{
				//if the layer url is a local file, load the data straight away
				if (provider.loadData(fileUrl, layer))
				{
					layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
			}
			else
			{
				//otherwise, check the cache for the downloaded data, and load or download

				URL url = provider.getDataFileStore().findFile(dataCacheName, false);
				if (url != null && !this.provider.isFileExpired(layer, url, provider.getDataFileStore()))
				{
					if (provider.loadData(url, layer))
					{
						layer.firePropertyChange(AVKey.LAYER, null, this);
						return;
					}
					else
					{
						// Assume that something's wrong with the file and delete it.
						provider.getDataFileStore().removeFile(url);
						String message = Logging.getMessage("generic.DeletedCorruptDataFile", url);
						Logging.logger().info(message);
					}
				}

				provider.downloadFile(layer, null);
			}
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			@SuppressWarnings("unchecked")
			final RequestTask that = (RequestTask) o;

			//assumes each layer only has a single file to request
			return !(layer != null ? !layer.equals(that.layer) : that.layer != null);
		}

		@Override
		public int hashCode()
		{
			return (layer != null ? layer.hashCode() : 0);
		}
	}
}
