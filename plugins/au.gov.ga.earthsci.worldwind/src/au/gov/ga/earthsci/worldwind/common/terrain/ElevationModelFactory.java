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
package au.gov.ga.earthsci.worldwind.common.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.terrain.BasicElevationModelFactory;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.Bounded;

/**
 * Extension to World Wind's {@link BasicElevationModelFactory} which creates
 * {@link ElevationModel}'s that implement the {@link Bounded} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ElevationModelFactory extends BasicElevationModelFactory
{
	//functions copied from superclass, replacing the model objects with our extensions 

	@Override
	protected CompoundElevationModel createCompoundModel(Element[] elements, AVList params)
	{
		BoundedCompoundElevationModel compoundModel = new BoundedCompoundElevationModel();

		if (elements == null || elements.length == 0)
			return compoundModel;

		for (Element element : elements)
		{
			try
			{
				ElevationModel em = this.doCreateFromElement(element, params);
				if (em != null)
					compoundModel.addElevationModel(em);
			}
			catch (Exception e)
			{
				String msg = Logging.getMessage("ElevationModel.ExceptionCreatingElevationModel");
				Logging.logger().log(java.util.logging.Level.WARNING, msg, e);
			}
		}

		return compoundModel;
	}

	@Override
	protected ElevationModel createNonCompoundModel(Element domElement, AVList params)
	{
		ElevationModel em;

		String serviceName = WWXML.getText(domElement, "Service/@serviceName");

		if ("Offline".equals(serviceName))
		{
			em = new SharedLockBasicElevationModel(domElement, params);
		}
		else if ("WWTileService".equals(serviceName))
		{
			em = new SharedLockBasicElevationModel(domElement, params);
		}
		else if (OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
		{
			em = new SharedLockWMSBasicElevationModel(domElement, params);
		}
		else if ("FileTileService".equals(serviceName))
		{
			em = new FileElevationModel(domElement, params);
		}
		else
		{
			String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
			throw new WWUnrecognizedException(msg);
		}

		return em;
	}
}
