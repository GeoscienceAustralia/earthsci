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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * Simple implementation of {@link ModelLayer} that renders a list of
 * {@link FastShape}s provided in the class constructor.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LocalModelLayer extends AbstractModelLayer implements ModelLayer
{
	public LocalModelLayer(List<FastShape> shapes)
	{
		super();

		for (FastShape shape : shapes)
		{
			addShape(shape);
		}
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return null;
	}

	@Override
	public String getDataCacheName()
	{
		return null;
	}

	@Override
	public boolean isLoading()
	{
		return false;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
	}

	@Override
	protected void requestData()
	{
	}
}
