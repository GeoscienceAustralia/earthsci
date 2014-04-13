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
package au.gov.ga.earthsci.worldwind.common.view.hmd;

import de.fruitfly.ovr.HMDInfo;

/**
 * Wrapper for {@link HMDInfo}, so we can have other (non Oculus)
 * implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IHMDParameters
{
	/**
	 * @return Horizontal resolution (in pixels)
	 */
	int getHorizontalResolution();

	/**
	 * @return Vertical resolution (in pixels)
	 */
	int getVerticalResolution();

	/**
	 * @return Horizontal screen size (in mm)
	 */
	float getHorizontalScreenSize();

	/**
	 * @return Vertical screen size (in mm)
	 */
	float getVerticalScreenSize();

	/**
	 * @return Vertical center of the screen (in mm)
	 */
	float getVerticalScreenCenter();

	/**
	 * @return Distance between the eye and the screen (in mm)
	 */
	float getEyeToScreenDistance();

	/**
	 * @return Distance between the center of the two lenses (in mm)
	 */
	float getLensSeparationDistance();

	/**
	 * @return Distance between the user's eyes
	 */
	float getInterpupillaryDistance();

	/**
	 * @return 4 coefficients used for barrel distortion
	 */
	float[] getDistortionCoefficients();

	/**
	 * @return 4 coefficients used for chromatic aberration correction
	 */
	float[] getChromaticAberrationCorrectionCoefficients();
}
