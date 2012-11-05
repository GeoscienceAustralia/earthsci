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
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwindx.examples.kml.KMLViewController;

/**
 * Factory that creates the custom view controllers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CustomKMLViewControllerFactory
{
	/**
	 * Convenience method to create a new view controller appropriate for the
	 * <code>WorldWindow</code>'s current <code>View</code>. Accepted view types
	 * are as follows:
	 * <ul>
	 * <li>{@link gov.nasa.worldwind.view.orbit.OrbitView}</li>
	 * <li>{@link gov.nasa.worldwind.view.firstperson.BasicFlyView}</li>
	 * </ul>
	 * . If the <code>View</code> is not one of the recognized types, this
	 * returns <code>null</code> and logs a warning.
	 * 
	 * @param wwd
	 *            the <code>WorldWindow</code> to create a view controller for.
	 * 
	 * @return A new view controller, or <code>null</code> if the
	 *         <code>WorldWindow</code>'s <code>View</code> type is not one of
	 *         the recognized types.
	 */
	public static KMLViewController create(WorldWindow wwd)
	{
		if (wwd == null)
		{
			String message = Logging.getMessage("nullValue.WorldWindow");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		View view = wwd.getView();

		if (view instanceof OrbitView)
			return new CustomKMLOrbitViewController(wwd);
		else if (view instanceof BasicFlyView)
			return new CustomKMLFlyViewController(wwd);
		else
		{
			Logging.logger().warning(Logging.getMessage("generic.UnrecognizedView", view));
			return null; // Unknown view
		}
	}
}
