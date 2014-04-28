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
package au.gov.ga.earthsci.worldwind.common.view.stereo;

/**
 * Parameters for {@link IStereoViewDelegate}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface StereoViewParameters
{
	/**
	 * @return Is stereo enabled?
	 */
	boolean isStereoEnabled();

	/**
	 * Toggle stereo on/off.
	 * 
	 * @param stereoEnabled
	 */
	void setStereoEnabled(boolean stereoEnabled);

	/**
	 * @return Asymmetric frustum focal length for this view.
	 */
	double getFocalLength();

	/**
	 * Set the asymmetric frustum focal length for this view; ignored if dynamic
	 * stereo is enabled.
	 * 
	 * @param focalLength
	 */
	void setFocalLength(double focalLength);

	/**
	 * @return Eye separation for this view.
	 */
	double getEyeSeparation();

	/**
	 * Set the eye separation for this view; ignored if dynamic stereo is
	 * enabled.
	 * 
	 * @param eyeSeparation
	 */
	void setEyeSeparation(double eyeSeparation);

	/**
	 * @return Eye separation multiplier applied to the eye separation in the
	 *         dynamic stereo mode.
	 */
	double getEyeSeparationMultiplier();

	/**
	 * Set the eye separation multiplier applied to the eye separation in the
	 * dynamic stereo mode.
	 * 
	 * @param eyeSeparationMultiplier
	 */
	void setEyeSeparationMultiplier(double eyeSeparationMultiplier);

	/**
	 * @return Is this view calculating stereo parameters (focal length and eye
	 *         separation) dynamically according to zoom and pitch?
	 */
	boolean isDynamicStereo();

	/**
	 * Enable/disable dynamic calculation of stereo parameters (focal length and
	 * eye separation) according to zoom and pitch.
	 * 
	 * @param dynamicStereo
	 */
	void setDynamicStereo(boolean dynamicStereo);

	/**
	 * @return Mode to use for stereo rendering.
	 */
	StereoMode getStereoMode();

	/**
	 * Set the mode to use for stereo rendering.
	 * 
	 * @param stereoMode
	 */
	void setStereoMode(StereoMode stereoMode);

	/**
	 * @return Should the eyes be swapped?
	 */
	boolean isSwapEyes();

	/**
	 * Turn stereo eye swapping on/off.
	 */
	void setSwapEyes(boolean swapEyes);
}
