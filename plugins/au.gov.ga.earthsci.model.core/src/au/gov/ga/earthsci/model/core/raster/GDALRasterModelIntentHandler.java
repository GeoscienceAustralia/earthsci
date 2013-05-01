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
package au.gov.ga.earthsci.model.core.raster;

import gov.nasa.worldwind.layers.Layer;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentManager;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.core.worldwind.BasicModelLayer;
import au.gov.ga.earthsci.model.core.worldwind.IModelLayer;

/**
 * An intent handler that responds to intents that match GDAL-supported raster
 * formats and generates an IModel
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelIntentHandler extends AbstractRetrieveIntentHandler
{

	private static final Logger logger = LoggerFactory.getLogger(GDALRasterModelIntentHandler.class);

	@Inject
	private IIntentManager intentManager;

	@Inject
	private IEclipseContext eclipseContext;

	@Override
	protected void handle(IRetrievalData data, URL url, Intent intent, IIntentCallback callback)
	{
		try
		{
			File source = data.getFile();

			/* Create the model from the source file
			 *  
			 * Implementation passes the original callback through for use in (possibly) 
			 * asynchronous operations
			 *
			 * Processing chain is as follows:
			 * 
			 * Open dataset -> Get model parameters -> Create model
			 * 
			 */

			openDataset(source, intent, callback);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	@Override
	protected IRetrievalProperties getRetrievalProperties()
	{
		RetrievalProperties result = new RetrievalProperties();
		result.setFileRequired(true);
		return result;
	}

	private void openDataset(File source, Intent intent, IIntentCallback callback)
	{
		logger.debug("Creating model from dataset {}", source.getAbsoluteFile()); //$NON-NLS-1$

		Dataset ds = gdal.Open(source.getAbsolutePath());
		if (ds == null)
		{
			logger.debug("Unable to open dataset {}", source.getAbsoluteFile()); //$NON-NLS-1$
			callback.error(new IllegalArgumentException(gdal.GetLastErrorMsg()), intent);
			return;
		}

		obtainParameters(ds, intent, callback);
	}

	private void obtainParameters(final Dataset ds, final Intent intent, final IIntentCallback callback)
	{

		Intent paramsIntent = new Intent();
		paramsIntent.setExpectedReturnType(GDALRasterModelParameters.class);
		paramsIntent.putExtra("dataset", ds);

		intentManager.start(paramsIntent, new IIntentCallback()
		{
			@Override
			public void error(Exception e, Intent paramsIntent)
			{
				logger.debug("Error signaled during raster parameter collection"); //$NON-NLS-1$

				callback.error(e, intent);
			}

			@Override
			public void completed(Object result, Intent paramsIntent)
			{
				logger.debug("Raster parameters completed"); //$NON-NLS-1$

				GDALRasterModelParameters parameters = (GDALRasterModelParameters) result;
				if (parameters == null)
				{
					callback.completed(null, intent);
					return;
				}

				createModel(ds, parameters, intent, callback);
			}

			@Override
			public void aborted(Intent paramsIntent)
			{
				logger.debug("Raster parameter collection aborted"); //$NON-NLS-1$

				callback.aborted(intent);
			}

			@Override
			public void canceled(Intent paramsIntent)
			{
				logger.debug("Raster parameter collection cancelled"); //$NON-NLS-1$

				callback.aborted(intent);
			}

		}, eclipseContext);

	}

	private void createModel(final Dataset ds, GDALRasterModelParameters parameters, final Intent intent,
			final IIntentCallback callback)
	{
		try
		{
			IModel model = GDALRasterModelFactory.createModel(ds, parameters);

			if (isModelIntent(intent))
			{
				callback.completed(model, intent);
			}
			else if (isLayerIntent(intent))
			{
				callback.completed(createModelLayer(model), intent);
			}
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	/**
	 * Create a new {@link IModelLayer} that contains the provided model.
	 * 
	 * @param m
	 *            The model to wrap with a layer
	 * 
	 * @return A new {@link IModelLayer} that contains the provided model.
	 */
	private IModelLayer createModelLayer(IModel m) throws Exception
	{
		return new BasicModelLayer(m.getName(), m);
	}

	private boolean isModelIntent(Intent intent)
	{
		return intent.getExpectedReturnType().isAssignableFrom(IModel.class);
	}

	private boolean isLayerIntent(Intent intent)
	{
		return intent.getExpectedReturnType().isAssignableFrom(Layer.class);
	}

}
