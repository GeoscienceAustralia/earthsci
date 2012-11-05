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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.AbstractDelegateFactory;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.elevationreader.ColorMapElevationImageReaderDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.elevationreader.ShadedElevationImageReaderDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.nearestneighbor.NearestNeighborTextureTileFactoryDelegate;

/**
 * Factory which creates delegates for the {@link DelegatorTiledImageLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImageDelegateFactory extends AbstractDelegateFactory
{
	private static ImageDelegateFactory instance = new ImageDelegateFactory();

	public static ImageDelegateFactory get()
	{
		return instance;
	}

	private ImageDelegateFactory()
	{
		super();

		//register the specific delegates applicable to this Image factory
		registerDelegate(ImageURLRequesterDelegate.class);
		registerDelegate(ImageLocalRequesterDelegate.class);

		registerDelegate(TextureTileFactoryDelegate.class);
		registerDelegate(NearestNeighborTextureTileFactoryDelegate.class);

		registerDelegate(ShadedElevationImageReaderDelegate.class);
		registerDelegate(ColorMapElevationImageReaderDelegate.class);
	}
}
