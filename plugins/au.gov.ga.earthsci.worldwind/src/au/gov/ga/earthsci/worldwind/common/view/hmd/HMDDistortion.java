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

/**
 * Calculates distortion for HMDs. Based on the code by Michael Antonov and
 * Andrew Reisse (see "Util_Render_Stereo.cpp" in the Oculus Rift SDK).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HMDDistortion
{
	//configurable
	private int windowWidth;
	private int windowHeight;
	private IHMDParameters parameters;
	private float distortionFitX = -1f;
	private float distortionFitY = 0f;
	private float interpupillaryDistance;
	private boolean interpupillaryDistanceOverridden = false;
	private float aspectMultiplier = 1f;

	//calculated
	private float distortionScale = 1f;
	private float distortionXCenterOffset = 0f;
	private float aspect;
	private float verticalFOV;
	private float projectionCenterOffset;
	private boolean dirty = true;

	public HMDDistortion(IHMDParameters parameters, int windowWidth, int windowHeight)
	{
		setParameters(parameters);
		setWindowWidth(windowWidth);
		setWindowHeight(windowHeight);
	}

	public int getWindowWidth()
	{
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth)
	{
		this.windowWidth = windowWidth;
		dirty = true;
	}

	public int getWindowHeight()
	{
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight)
	{
		this.windowHeight = windowHeight;
		dirty = true;
	}

	public IHMDParameters getParameters()
	{
		return parameters;
	}

	public void setParameters(IHMDParameters hmd)
	{
		this.parameters = hmd;
		dirty = true;
	}

	public float getInterpupillaryDistance()
	{
		return interpupillaryDistanceOverridden ? interpupillaryDistance : parameters.getInterpupillaryDistance();
	}

	public void setInterpupillaryDistance(float interpupillaryDistance)
	{
		this.interpupillaryDistance = interpupillaryDistance;
		interpupillaryDistanceOverridden = true;
		dirty = true;
	}

	public void resetInterpupillaryDistance()
	{
		interpupillaryDistanceOverridden = false;
		dirty = true;
	}

	public void setDistortionFit(float fitX, float fitY)
	{
		this.distortionFitX = fitX;
		this.distortionFitY = fitY;
		dirty = true;
	}

	public void setDistortionFitPointPixels(float x, float y)
	{
		this.distortionFitX = (4f * x / (float) windowWidth) - 1f;
		this.distortionFitY = (2f * y / (float) windowHeight) - 1f;
		dirty = true;
	}

	public float getAspectMultiplier()
	{
		return aspectMultiplier;
	}

	public void setAspectMultiplier(float aspectMultiplier)
	{
		this.aspectMultiplier = aspectMultiplier;
	}

	public float getDistortionScale()
	{
		return distortionScale;
	}

	public float getDistortionXCenterOffset()
	{
		return distortionXCenterOffset;
	}

	public float getAspect()
	{
		return aspect;
	}

	public float getVerticalFOV()
	{
		return verticalFOV;
	}

	public float getProjectionCenterOffset()
	{
		return projectionCenterOffset;
	}

	public void update()
	{
		if (!dirty)
		{
			return;
		}

		updateComputedState();
		dirty = false;
	}

	protected void updateComputedState()
	{
		// Need to compute all of the following:
		//   - Aspect Ratio
		//   - FOV
		//   - Projection offsets for 3D
		//   - Distortion XCenterOffset
		//   - Update 2D
		//   - Initialize EyeRenderParams

		// Compute aspect ratio. Stereo mode cuts width in half.
		aspect = (float) windowWidth / (float) windowHeight;
		aspect *= 0.5f;
		aspect *= aspectMultiplier;

		updateDistortionOffsetAndScale();

		// Compute Vertical FOV based on distance, distortion, etc.
		// Distance from vertical center to render vertical edge perceived through the lens.
		// This will be larger then normal screen size due to magnification & distortion.
		//
		// This percievedHalfRTDistance equation should hold as long as the render target
		// and display have the same aspect ratios. What we'd like to know is where the edge
		// of the render target will on the perceived screen surface. With NO LENS,
		// the answer would be:
		//
		//  halfRTDistance = (VScreenSize / 2) * aspect *
		//                   DistortionFn_Inverse( DistortionScale / aspect )
		//
		// To model the optical lens we eliminates DistortionFn_Inverse. Aspect ratios
		// cancel out, so we get:
		//
		//  halfRTDistance = (VScreenSize / 2) * DistortionScale
		//
		float percievedHalfRTDistance = (parameters.getVerticalScreenSize() / 2f) * distortionScale;
		verticalFOV = 2f * (float) Math.atan(percievedHalfRTDistance / parameters.getEyeToScreenDistance());

		updateProjectionOffset();
		//updateEyeParams();
	}

	protected void updateDistortionOffsetAndScale()
	{
		// Distortion center shift is stored separately, since it isn't affected
		// by the eye distance.
		float lensOffset = parameters.getLensSeparationDistance() * 0.5f;
		float lensShift = parameters.getHorizontalScreenSize() * 0.25f - lensOffset;
		float lensViewportShift = 4.0f * lensShift / parameters.getHorizontalScreenSize();
		distortionXCenterOffset = lensViewportShift;

		// Compute distortion scale from DistortionFitX & DistortionFitY.
		// Fit value of 0.0 means "no fit".
		if (Math.abs(distortionFitX) < 0.0001f && Math.abs(distortionFitY) < 0.0001f)
		{
			distortionScale = 1.0f;
		}
		else
		{
			// Convert fit value to distortion-centered coordinates before fit radius
			// calculation.
			float stereoAspect = 0.5f * (float) windowWidth / (float) windowHeight;
			float dx = distortionFitX - distortionXCenterOffset;
			float dy = distortionFitY / stereoAspect;
			float fitRadius = (float) Math.sqrt(dx * dx + dy * dy);
			distortionScale = distortionFn(fitRadius) / fitRadius;
		}
	}

	protected void updateProjectionOffset()
	{
		// Post-projection viewport coordinates range from (-1.0, 1.0), with the
		// center of the left viewport falling at (1/4) of horizontal screen size.
		// We need to shift this projection center to match with the lens center;
		// note that we don't use the IPD here due to collimated light property of the lens.
		// We compute this shift in physical units (meters) to
		// correct for different screen sizes and then rescale to viewport coordinates.    
		float viewCenter = parameters.getHorizontalScreenSize() * 0.25f;
		float eyeProjectionShift = viewCenter - parameters.getLensSeparationDistance() * 0.5f;
		projectionCenterOffset = 4.0f * eyeProjectionShift / parameters.getHorizontalScreenSize();
	}

	protected float distortionFn(float r)
	{
		float[] K = parameters.getDistortionCoefficients();
		float rsq = r * r;
		float scale = r * (K[0] + K[1] * rsq + K[2] * rsq * rsq + K[3] * rsq * rsq * rsq);
		return scale;
	}

	/*protected float distortionFnInverse(float r)
	{
		if (r > 10)
		{
			throw new IllegalArgumentException();
		}

		float s, d;
		float delta = r * 0.25f;

		s = r * 0.5f;
		d = Math.abs(r - distortionFn(s));

		for (int i = 0; i < 20; i++)
		{
			float sUp = s + delta;
			float sDown = s - delta;
			float dUp = Math.abs(r - distortionFn(sUp));
			float dDown = Math.abs(r - distortionFn(sDown));

			if (dUp < d)
			{
				s = sUp;
				d = dUp;
			}
			else if (dDown < d)
			{
				s = sDown;
				d = dDown;
			}
			else
			{
				delta *= 0.5f;
			}
		}

		return s;
	}*/
}
