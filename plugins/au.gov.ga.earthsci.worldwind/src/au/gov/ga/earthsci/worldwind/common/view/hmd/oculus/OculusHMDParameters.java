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
package au.gov.ga.earthsci.worldwind.common.view.hmd.oculus;

import au.gov.ga.earthsci.worldwind.common.view.hmd.IHMDParameters;
import de.fruitfly.ovr.HMDInfo;

/**
 * {@link IHMDParameters} implementation that wraps the Oculus' {@link HMDInfo}
 * class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OculusHMDParameters implements IHMDParameters
{
	private final HMDInfo hmd;

	public OculusHMDParameters(HMDInfo hmd)
	{
		this.hmd = hmd;
	}

	@Override
	public int getHorizontalResolution()
	{
		return hmd.HResolution;
	}

	@Override
	public int getVerticalResolution()
	{
		return hmd.VResolution;
	}

	@Override
	public float getHorizontalScreenSize()
	{
		return hmd.HScreenSize;
	}

	@Override
	public float getVerticalScreenSize()
	{
		return hmd.VScreenSize;
	}

	@Override
	public float getVerticalScreenCenter()
	{
		return hmd.VScreenCenter;
	}

	@Override
	public float getEyeToScreenDistance()
	{
		return hmd.EyeToScreenDistance;
	}

	@Override
	public float getLensSeparationDistance()
	{
		return hmd.LensSeparationDistance;
	}

	@Override
	public float getInterpupillaryDistance()
	{
		return hmd.InterpupillaryDistance;
	}

	@Override
	public float[] getDistortionCoefficients()
	{
		return hmd.DistortionK;
	}

	@Override
	public float[] getChromaticAberrationCorrectionCoefficients()
	{
		return hmd.ChromaticAb;
	}
}
