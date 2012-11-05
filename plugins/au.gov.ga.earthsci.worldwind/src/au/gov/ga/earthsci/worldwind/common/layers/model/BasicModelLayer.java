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
package au.gov.ga.earthsci.worldwind.common.layers.model;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Basic implementation of a {@link ModelLayer}. Renders data provided by a
 * {@link ModelProvider}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicModelLayer extends AbstractModelLayer implements ModelLayer
{
	private ModelProvider provider;

	private String url;
	private URL context;
	private String dataCacheName;

	public BasicModelLayer(AVList params)
	{
		super();

		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		provider = (ModelProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		color = (Color) params.getValue(AVKeyMore.COLOR);
		lineWidth = (Double) params.getValue(AVKeyMore.LINE_WIDTH);
		pointSize = (Double) params.getValue(AVKeyMore.POINT_SIZE);

		Boolean b = (Boolean) params.getValue(AVKeyMore.POINT_SPRITE);
		if (b != null)
			pointSprite = b;

		b = (Boolean) params.getValue(AVKeyMore.REVERSE_NORMALS);
		if (b != null)
			reverseNormals = b;

		b = (Boolean) params.getValue(AVKeyMore.ORDERED_RENDERING);
		if (b != null)
			useOrderedRendering = b;

		pointMinSize = (Double) params.getValue(AVKeyMore.POINT_MIN_SIZE);
		pointMaxSize = (Double) params.getValue(AVKeyMore.POINT_MAX_SIZE);
		pointConstantAttenuation = (Double) params.getValue(AVKeyMore.POINT_CONSTANT_ATTENUATION);
		pointLinearAttenuation = (Double) params.getValue(AVKeyMore.POINT_LINEAR_ATTENUATION);
		pointQuadraticAttenuation = (Double) params.getValue(AVKeyMore.POINT_QUADRATIC_ATTENUATION);

		minimumDistance = (Double) params.getValue(AVKeyMore.MINIMUM_DISTANCE);

		Validate.notBlank(url, "Model data url not set");
		Validate.notBlank(dataCacheName, "Model data cache name not set");

		Validate.notNull(provider, "Model data provider is null");
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return new URL(context, url);
	}

	@Override
	public String getDataCacheName()
	{
		return dataCacheName;
	}

	@Override
	public boolean isLoading()
	{
		return provider.isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		provider.addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		provider.removeLoadingListener(listener);
	}

	@Override
	protected void requestData()
	{
		provider.requestData(this);
	}
}
