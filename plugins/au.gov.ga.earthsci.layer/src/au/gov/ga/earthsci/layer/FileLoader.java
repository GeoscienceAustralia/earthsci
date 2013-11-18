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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.BasicDataRasterReaderFactory;
import gov.nasa.worldwind.data.DataRasterReader;
import gov.nasa.worldwind.data.DataRasterReaderFactory;
import gov.nasa.worldwind.data.DataStoreProducer;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.data.WWDotNetLayerSetConverter;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwindx.applications.worldwindow.features.DataImportUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class used to load (non layer definition) files as layers. Examples of
 * files that could be loaded are a GeoTIFF, a GOCAD geometry file, or a KML
 * file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileLoader
{
	/**
	 * Listener interface for {@link FileLoader} events.
	 */
	public interface FileLoadListener
	{
		void loaded(Element element, URL sourceUrl);

		void error(Exception e);

		void cancelled();
	}

	/**
	 * Attempt to load the given file. Runs the load operation asynchronously on
	 * a separate thread.
	 * 
	 * @param file
	 *            File to load
	 * @param listener
	 *            Listener for load events
	 * @param shell
	 *            Parent shell to show any loading progress bars as children of
	 * @param fileStore
	 *            File store to save a local tileset to (for georeferenced
	 *            images such as GeoTIFFs)
	 */
	public static void loadFile(final File file, final FileLoadListener listener, final Shell parent,
			final FileStore fileStore)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Document dataConfig = null;

				try
				{
					//first attempt loading the file from the WorldWindInstalled cache
					dataConfig = loadCachedData(file, fileStore);
					if (dataConfig == null)
					{
						// Import the file into a form usable by World Wind components.
						dataConfig = importDataFromFile(parent, file, fileStore);
					}

					if (dataConfig == null)
					{
						listener.cancelled();
					}
					else
					{
						URL sourceUrl = file.toURI().toURL();
						listener.loaded(dataConfig.getDocumentElement(), sourceUrl);
					}
				}
				catch (Exception e)
				{
					listener.error(e);
					e.printStackTrace();
				}
			}
		});
		thread.setDaemon(true);
		thread.setName("File loader");
		thread.start();
	}

	protected static Document loadCachedData(File file, FileStore fileStore)
	{
		//TODO
		return null;
	}

	//FROM ImportingImagesAndElevationsDemo.java, modified for SWT/JFace

	protected static Document importDataFromFile(Shell parent, final File file, final FileStore fileStore)
			throws Exception
	{
		// Create a DataStoreProducer which is capable of processing the file.
		final DataStoreProducer producer = createDataStoreProducerFromFile(file);
		if (producer == null)
		{
			throw new IllegalArgumentException("Unrecognized file type");
		}

		final AtomicInteger progress = new AtomicInteger(0);
		PropertyChangeListener progressListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getPropertyName().equals(AVKey.PROGRESS))
				{
					progress.set((int) (100 * (Double) evt.getNewValue()));
				}
			}
		};
		producer.addPropertyChangeListener(progressListener);

		final boolean[] closeDialog = new boolean[] { false };
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(parent);
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					dialog.run(true, true, new IRunnableWithProgress()
					{
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException
						{
							monitor.beginTask("Importing " + file.getName(), 100);
							while (true)
							{
								if (closeDialog[0])
								{
									break;
								}
								if (monitor.isCanceled())
								{
									producer.stopProduction();
									break;
								}

								int monitorProgress = 0;
								int currentProgress = progress.get();
								int diff = currentProgress - monitorProgress;
								if (diff > 0)
								{
									monitor.worked(diff);
								}
								Thread.sleep(100);
							}
						}
					});
				}
				catch (InvocationTargetException e)
				{
				}
				catch (InterruptedException e)
				{
					producer.stopProduction();
				}
			}
		});

		Document doc = null;
		try
		{
			// Import the file into the specified FileStore.
			doc = createDataStoreFromFile(file, fileStore, producer);

			// The user clicked the ProgressMonitor's "Cancel" button. Revert any change made during production, and
			// discard the returned DataConfiguration reference.
			if (dialog.getReturnCode() == Dialog.CANCEL)
			{
				doc = null;
				producer.removeProductionState();
			}
		}
		finally
		{
			// Remove the progress event listener from the DataStoreProducer. stop the progress timer, and signify to the
			// ProgressMonitor that we're done.
			closeDialog[0] = true;
			producer.removePropertyChangeListener(progressListener);
		}

		return doc;
	}

	protected static Document createDataStoreFromFile(File file, FileStore fileStore, DataStoreProducer producer)
			throws Exception
	{
		File importLocation = DataImportUtil.getDefaultImportLocation(fileStore);
		if (importLocation == null)
		{
			String message = Logging.getMessage("generic.NoDefaultImportLocation");
			Logging.logger().severe(message);
			return null;
		}

		// Create the production parameters. These parameters instruct the DataStoreProducer where to import the cached
		// data, and what name to put in the data configuration document.
		AVList params = new AVListImpl();
		params.setValue(AVKey.DATASET_NAME, file.getName());
		params.setValue(AVKey.DATA_CACHE_NAME, file.getName());
		params.setValue(AVKey.FILE_STORE_LOCATION, importLocation.getAbsolutePath());
		producer.setStoreParameters(params);

		// Use the specified file as the the production data source.
		producer.offerDataSource(file, params);

		try
		{
			// Convert the file to a form usable by World Wind components, according to the specified DataStoreProducer.
			// This throws an exception if production fails for any reason.
			producer.startProduction();
		}
		catch (Exception e)
		{
			// Exception attempting to convert the file. Revert any change made during production.
			producer.removeProductionState();
			throw e;
		}

		// Return the DataConfiguration from the production results. Since production sucessfully completed, the
		// DataStoreProducer should contain a DataConfiguration in the production results. We test the production
		// results anyway.
		Iterable<?> results = producer.getProductionResults();
		if (results != null && results.iterator() != null && results.iterator().hasNext())
		{
			Object o = results.iterator().next();
			if (o != null && o instanceof Document)
			{
				return (Document) o;
			}
		}

		return null;
	}

	//**************************************************************//
	//********************  Utility Methods  ***********************//
	//**************************************************************//

	protected static DataStoreProducer createDataStoreProducerFromFile(File file)
	{
		if (file == null)
		{
			return null;
		}

		DataStoreProducer producer = null;

		DataRasterReaderFactory readerFactory = new BasicDataRasterReaderFactory();
		AVListImpl params = new AVListImpl();
		DataRasterReader reader = readerFactory.findReaderFor(file, params);

		if (reader != null && reader.isElevationsRaster(file, params))
		{
			producer = new TiledElevationProducer();
		}
		else if (reader != null && reader.isImageryRaster(file, params))
		{
			producer = new TiledImageProducer();
		}
		else if (DataImportUtil.isWWDotNetLayerSet(file))
		{
			producer = new WWDotNetLayerSetConverter();
		}

		return producer;
	}
}
