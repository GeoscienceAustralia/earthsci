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
package au.gov.ga.earthsci.core.model.layer.uri;

import gov.nasa.worldwind.layers.Layer;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import au.gov.ga.earthsci.core.Activator;
import au.gov.ga.earthsci.core.model.layer.LayerNode;

/**
 * Job used for creating a {@link Layer} from a {@link LayerNode}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URILayerLoadJob extends Job
{
	private final LayerNode layerNode;
	private Layer layer;

	public URILayerLoadJob(LayerNode layerNode)
	{
		super(layerNode.getName());
		this.layerNode = layerNode;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		URI uri = layerNode.getURI();
		try
		{
			layer = URILayerFactory.createLayer(uri, monitor);
			layerNode.setLayer(layer);
		}
		catch (URILayerFactoryException e)
		{
			return new Status(Status.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
		}
		return Status.OK_STATUS;
	}

	public Layer getLayer()
	{
		return layer;
	}
}
