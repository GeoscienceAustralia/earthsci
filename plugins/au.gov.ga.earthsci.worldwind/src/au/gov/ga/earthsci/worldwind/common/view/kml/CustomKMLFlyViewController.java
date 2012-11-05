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
package au.gov.ga.earthsci.worldwind.common.view.kml;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwindx.examples.kml.KMLFlyViewController;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension to the {@link KMLFlyViewController} that uses the
 * {@link CustomKMLUtil} to calculate a {@link KMLPlacemark} positions, to add
 * support for flying to {@link KMLModel}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CustomKMLFlyViewController extends KMLFlyViewController
{
	protected CustomKMLFlyViewController(WorldWindow wwd)
	{
		super(wwd);
	}

	@Override
	protected void goToDefaultPlacemarkView(KMLPlacemark placemark)
	{
		View view = this.wwd.getView();
		List<Position> positions = new ArrayList<Position>();

		// Find all the points in the placemark. We want to bring the entire placemark into view.
		KMLAbstractGeometry geometry = placemark.getGeometry();
		CustomKMLUtil.getPositions(view.getGlobe(), geometry, positions);

		this.goToDefaultView(positions);
	}
}
