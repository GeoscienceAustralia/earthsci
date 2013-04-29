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

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;
import au.gov.ga.earthsci.intent.IIntentCallback;
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

	@Override
	protected void handle(IRetrievalData data, URL url, Intent intent, IIntentCallback callback)
	{
		try
		{
			File source = data.getFile();
			IModel model = createModel(source);

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

	@Override
	protected IRetrievalProperties getRetrievalProperties()
	{
		RetrievalProperties result = new RetrievalProperties();
		result.setFileRequired(true);
		return result;
	}

	/**
	 * Create an {@link IModel} instance from the GDAL raster referenced by the
	 * provided file
	 * 
	 * @param source
	 *            The source raster to load
	 * 
	 * @return A created {@link IModel} instance, or <code>null</code> if one
	 *         could not be created
	 * 
	 * @throws Exception
	 *             If something goes wrong during creation
	 */
	private IModel createModel(File source) throws Exception
	{
		logger.debug("Creating model from dataset {}", source.getAbsoluteFile()); //$NON-NLS-1$

		Dataset ds = gdal.Open(source.getAbsolutePath());
		if (ds == null)
		{
			logger.debug("Unable to open dataset {}", source.getAbsoluteFile()); //$NON-NLS-1$

			throw new IllegalArgumentException(gdal.GetLastErrorMsg());
		}
		return createModel(ds);
	}

	/**
	 * Create an {@link IModel} instance from the GDAL raster referenced by the
	 * provided dataset.
	 * 
	 * @param ds
	 *            The GDAL dataset to load the model from
	 * 
	 * @return A created {@link IModel} instance, or <code>null</code> if one
	 *         could not be created
	 * 
	 * @throws Exception
	 *             If something goes wrong during creation
	 */
	private IModel createModel(Dataset ds) throws Exception
	{
		GDALRasterModelParameters parameters = getParameters(ds);

		return GDALRasterModelFactory.createModel(ds, parameters);

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

	/**
	 * Get parameters to use for creating a model instance from the provided
	 * dataset
	 */
	private GDALRasterModelParameters getParameters(Dataset ds)
	{
		return new GDALRasterModelParameters(ds);
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
