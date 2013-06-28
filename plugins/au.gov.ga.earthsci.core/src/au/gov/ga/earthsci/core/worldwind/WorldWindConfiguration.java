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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.core.model.layer.LayerFactory;
import au.gov.ga.earthsci.worldwind.common.terrain.ElevationModelFactory;
import au.gov.ga.earthsci.worldwind.common.terrain.WireframeRectangularTessellator;

/**
 * Helper class for setting up the WorldWind {@link Configuration} properties.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Creatable
@Singleton
public class WorldWindConfiguration
{
	@PostConstruct
	public void setup()
	{
		Configuration.setValue(AVKey.LAYER_FACTORY, LayerFactory.class.getName());
		Configuration.setValue(AVKey.ELEVATION_MODEL_FACTORY, ElevationModelFactory.class.getName());
		Configuration.setValue(AVKey.MODEL_CLASS_NAME, WorldWindModel.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, WorldWindRetrievalService.class.getName());
		Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, WireframeRectangularTessellator.class.getName());
	}

	@PreDestroy
	public void packup()
	{
		WorldWind.getRetrievalService().shutdown(true);
	}
}
