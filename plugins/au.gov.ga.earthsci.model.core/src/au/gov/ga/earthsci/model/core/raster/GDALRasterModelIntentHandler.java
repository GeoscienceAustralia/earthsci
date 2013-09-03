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
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.URIBuilder;
import au.gov.ga.earthsci.common.util.URIUtil;
import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
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

	public static String GDAL_RASTER_MODEL_URI_SCHEME = "gdalrastermodel"; //$NON-NLS-1$
	public static String GDAL_RASTER_MODEL_URI_HOST = "model"; //$NON-NLS-1$
	public static String SOURCE_URI_PARAM_KEY = "source"; //$NON-NLS-1$

	public static String MODEL_PARAMS_EXTRAS_KEY = "gdalRasterModelParams"; //$NON-NLS-1$
	public static String DATASET_EXTRAS_KEY = "dataset"; //$NON-NLS-1$

	@Inject
	private IIntentManager intentManager;

	@Inject
	private IEclipseContext eclipseContext;

	@Override
	public void handle(Intent intent, IIntentCallback callback)
	{
		if (isModelURI(intent.getURI()))
		{

			Map<String, String> queryParams = URIUtil.getParameterMap(intent.getURI());

			URI source = null;
			GDALRasterModelParameters modelParams = null;
			try
			{
				source = new URI(queryParams.get(SOURCE_URI_PARAM_KEY));
				modelParams = new GDALRasterModelParameters(queryParams);
			}
			catch (Exception e)
			{
				callback.error(e, intent);
				return;
			}

			intent.setURI(source);
			intent.putExtra(MODEL_PARAMS_EXTRAS_KEY, modelParams);
			super.handle(intent, callback);
		}
		else
		{
			super.handle(intent, callback);
		}
	}

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

		// If model params already exist, they were likely loaded from a URL
		// If so, use them here and don't collect them from elsewhere
		if (intent.getExtra(MODEL_PARAMS_EXTRAS_KEY) != null)
		{
			GDALRasterModelParameters params = (GDALRasterModelParameters) intent.getExtra(MODEL_PARAMS_EXTRAS_KEY);
			createModel(ds, params, intent, callback);
			return;
		}

		Intent paramsIntent = new Intent();
		paramsIntent.setRequiredReturnType(GDALRasterModelParameters.class);
		paramsIntent.putExtra(DATASET_EXTRAS_KEY, ds);

		intentManager.start(paramsIntent, new AbstractIntentCallback()
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
				logger.debug("Raster parameter collection canceled"); //$NON-NLS-1$

				callback.canceled(intent);
			}

		}, eclipseContext);

	}

	private void createModel(final Dataset ds, GDALRasterModelParameters parameters, final Intent intent,
			final IIntentCallback callback)
	{
		try
		{
			GDALRasterModel model = GDALRasterModelFactory.createModel(ds, parameters);

			URI newURI = rewriteURI(intent.getURI(), model);
			if (isModelIntent(intent))
			{
				callback.completed(model, intent);
			}
			else if (isLayerIntent(intent))
			{
				intent.setURI(newURI);
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

	private boolean isModelURI(URI source)
	{
		return source.getScheme().equalsIgnoreCase(GDAL_RASTER_MODEL_URI_SCHEME);
	}

	private boolean isModelIntent(Intent intent)
	{
		return intent.getExpectedReturnType().isAssignableFrom(IModel.class);
	}

	private boolean isLayerIntent(Intent intent)
	{
		return intent.getExpectedReturnType().isAssignableFrom(Layer.class);
	}

	/**
	 * Re-write the source URI as a GDAL Raster Model URI with all parameters
	 * encoded.
	 * 
	 * @param sourceURI
	 *            The source URI from which the model was originally created
	 * @param model
	 *            The loaded model
	 * 
	 * @return A model re-written using the
	 *         {@link #GDAL_RASTER_MODEL_URI_SCHEME} scheme
	 */
	private URI rewriteURI(URI sourceURI, GDALRasterModel model) throws Exception
	{
		URIBuilder builder = new URIBuilder();
		builder.setScheme(GDAL_RASTER_MODEL_URI_SCHEME);
		builder.setHost(GDAL_RASTER_MODEL_URI_HOST);
		builder.setParam(SOURCE_URI_PARAM_KEY, sourceURI.toString());

		// Add configuration params
		Map<String, String> modelParams = model.getParameters().asParameterMap();
		for (Entry<String, String> paramEntry : modelParams.entrySet())
		{
			builder.setParam(paramEntry.getKey(), paramEntry.getValue());
		}

		return builder.build();
	}

}
