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
package au.gov.ga.earthsci.core.xml;

import gov.nasa.worldwind.layers.Layer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import au.gov.ga.earthsci.core.Activator;
import au.gov.ga.earthsci.core.model.layer.uri.URILayerFactory;
import au.gov.ga.earthsci.core.model.layer.uri.URILayerFactoryException;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentCaller;
import au.gov.ga.earthsci.intent.IntentHandler;

/**
 * General handler for {@link Intent}s with the
 * <code>org.eclipse.core.runtime.xml</code> content type. Delegates to specific
 * XML loaders.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class XmlIntentHandler implements IntentHandler
{
	@Override
	public void handle(final Intent intent, final IntentCaller caller)
	{
		//caller.completed(intent, "hello");
		
		//TODO replace this with a more general URI -> XML -> Handler implementation
		
		Job job = new Job(intent.getURI().toString())
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					Layer layer = URILayerFactory.createLayer(this, intent.getURI(), monitor);
					caller.completed(intent, layer);
				}
				catch (URILayerFactoryException e)
				{
					caller.error(intent, e);
					Status status = new Status(Status.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
					return status;
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
