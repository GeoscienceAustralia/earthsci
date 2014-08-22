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
package au.gov.ga.earthsci.worldwind.common.input;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.awt.AbstractViewInputHandler;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
import au.gov.ga.earthsci.worldwind.common.view.rotate.FreeRotateOrbitViewInputHandler;

/**
 * {@link ViewInputHandler} that is provided to the
 * {@link IOrbitInputProvider#apply} method.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IProviderOrbitViewInputHandler extends ViewInputHandler
{
	/**
	 * Mark the view as changed, using the {@link View#firePropertyChange}
	 * method.
	 */
	void markViewChanged();

	/**
	 * @see OrbitViewInputHandler#onVerticalTranslate
	 */
	void onVerticalTranslate(double translateChange, double totalTranslateChange, DeviceAttributes deviceAttributes,
			ActionAttributes actionAttributes);

	/**
	 * @see OrbitViewInputHandler#onRotateView
	 */
	void onRotateView(Angle headingChange, Angle pitchChange, ActionAttributes actionAttribs);

	/**
	 * @see FreeRotateOrbitViewInputHandler#onRotateFree
	 */
	void onRotateFree(Angle direction, Angle amount, DeviceAttributes deviceAttributes,
			ActionAttributes actionAttributes);

	/**
	 * @see FreeRotateOrbitViewInputHandler#onAltitudeFree
	 */
	void onAltitudeFree(double amount, DeviceAttributes deviceAttributes,
			ActionAttributes actionAttributes);

	/**
	 * @see OrbitViewInputHandler#getScaleValueRotate
	 */
	double getScaleValueRotate(ActionAttributes actionAttributes);

	/**
	 * @see View#stopAnimations()
	 */
	void stopAnimations();

	/**
	 * @see OrbitViewInputHandler#onResetHeadingPitchRoll
	 */
	void onResetHeadingPitchRoll(ActionAttributes actionAttribs);

	/**
	 * @see AbstractViewInputHandler#getView
	 */
	OrbitView getView();
}
