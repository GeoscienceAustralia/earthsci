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
 * Basic implementation of the {@link StereoViewParameters} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicStereoViewParameters implements StereoViewParameters
{
	private boolean stereoEnabled = false;
	private double focalLength = 100;
	private double eyeSeparation = 1;
	private double eyeSeparationMultiplier = 1;
	private boolean dynamicStereo = true;
	private StereoMode stereoMode = StereoMode.RC_ANAGLYPH;
	private boolean swapEyes = false;

	@Override
	public boolean isStereoEnabled()
	{
		return stereoEnabled;
	}

	@Override
	public void setStereoEnabled(boolean stereoEnabled)
	{
		this.stereoEnabled = stereoEnabled;
	}

	@Override
	public double getFocalLength()
	{
		return focalLength;
	}

	@Override
	public void setFocalLength(double focalLength)
	{
		this.focalLength = focalLength;
	}

	@Override
	public double getEyeSeparation()
	{
		return eyeSeparation;
	}

	@Override
	public void setEyeSeparation(double eyeSeparation)
	{
		this.eyeSeparation = eyeSeparation;
	}

	@Override
	public double getEyeSeparationMultiplier()
	{
		return eyeSeparationMultiplier;
	}

	@Override
	public void setEyeSeparationMultiplier(double eyeSeparationMultiplier)
	{
		this.eyeSeparationMultiplier = eyeSeparationMultiplier;
	}

	@Override
	public boolean isDynamicStereo()
	{
		return dynamicStereo;
	}

	@Override
	public void setDynamicStereo(boolean dynamicStereo)
	{
		this.dynamicStereo = dynamicStereo;
	}

	@Override
	public StereoMode getStereoMode()
	{
		return stereoMode;
	}

	@Override
	public void setStereoMode(StereoMode stereoMode)
	{
		this.stereoMode = stereoMode;
	}

	@Override
	public boolean isSwapEyes()
	{
		return swapEyes;
	}

	@Override
	public void setSwapEyes(boolean swapEyes)
	{
		this.swapEyes = swapEyes;
	}
}
