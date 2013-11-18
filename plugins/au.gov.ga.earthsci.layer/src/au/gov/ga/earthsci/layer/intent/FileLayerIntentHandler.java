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
package au.gov.ga.earthsci.layer.intent;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.layer.FileLoader;
import au.gov.ga.earthsci.layer.FileLoader.FileLoadListener;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Intent handler that creates tiled imagery layers from georeferenced datasets
 * such as GeoTIFFs, using the {@link FileLoader}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileLayerIntentHandler extends AbstractRetrieveIntentHandler
{
	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Override
	protected IRetrievalProperties getRetrievalProperties()
	{
		RetrievalProperties properties = new RetrievalProperties();
		properties.setFileRequired(true);
		return properties;
	}

	@Override
	protected void handle(IRetrievalData data, URL url, final Intent intent, final IIntentCallback callback)
	{
		FileLoadListener listener = new FileLoadListener()
		{
			@Override
			public void loaded(Element element, URL sourceUrl)
			{
				AVList params = new AVListImpl();
				params.setValue(AVKeyMore.CONTEXT_URL, sourceUrl);
				try
				{
					Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
					Object result = factory.createFromConfigSource(element, params);
					callback.completed(result, intent);
				}
				catch (Exception e)
				{
					callback.error(e, intent);
				}
			}

			@Override
			public void error(Exception e)
			{
				callback.error(e, intent);
			}

			@Override
			public void cancelled()
			{
				callback.aborted(intent);
			}
		};

		File file = data.getFile();
		FileLoader.loadFile(file, listener, shell, WorldWind.getDataFileStore());
	}
}
