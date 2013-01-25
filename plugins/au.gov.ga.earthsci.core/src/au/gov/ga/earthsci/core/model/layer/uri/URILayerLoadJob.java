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
import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.model.layer.Messages;
import au.gov.ga.earthsci.core.util.UTF8URLEncoder;
import au.gov.ga.earthsci.notification.Notification;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationLevel;
import au.gov.ga.earthsci.notification.NotificationManager;

/**
 * Job used for creating a {@link Layer} from a {@link LayerNode}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URILayerLoadJob extends Job
{
	private final LayerNode layerNode;

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
			Layer layer = URILayerFactory.createLayer(layerNode, uri, monitor);
			layerNode.setLayer(layer);
			layerNode.setStatus(ModelStatus.ok());
		}
		catch (URILayerFactoryException e)
		{
			NotificationManager.notify(Notification.create(NotificationLevel.ERROR, 
														   Messages.URILayerLoadJob_FailedLoadNotificationTitle, 
														   Messages.URILayerLoadJob_FailedLoadNotificationDescription + 
														   UTF8URLEncoder.decode(uri.toString()))
														   .inCategory(NotificationCategory.FILE_IO)
														   .requiringAcknowledgement(null)
														   .build());
			Status status = new Status(Status.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
			layerNode.setStatus(ModelStatus.fromIStatus(status));
			return status;
		}
		return Status.OK_STATUS;
	}
}
