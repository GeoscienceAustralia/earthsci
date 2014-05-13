/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.atmosphere;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Color;

import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Atmospheric scattering parameters.
 * <p/>
 * Based on Sean O'Neil's algorithm described in GPU Gems 2 <a
 * href="http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter16.html"
 * >chapter 16</a>.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Atmosphere
{
	//atmosphere constants
	final static float ATMOSPHERE_SCALE = 1.025f;
	final static float RAYLEIGH_SCATTERING = 0.0025f;
	final static float MIE_SCATTERING = 0.0015f;
	final static float SUN_BRIGHTNESS = 18.0f;
	final static float MIE_PHASE_ASYMMETRY = -0.990f;
	final static float WAVELENGTH[] = new float[] {
			0.731f,
			0.612f,
			0.455f
	};
	final static float SCALE_DEPTH = 0.25f;
	final static float EXPOSURE = 2.0f;

	//derived constants
	final static float INVWAVELENGTH4[] = new float[] {
			1f / (float) Math.pow(WAVELENGTH[0], 4),
			1f / (float) Math.pow(WAVELENGTH[1], 4),
			1f / (float) Math.pow(WAVELENGTH[2], 4)
	};

	/**
	 * Get the color of an object at a point in space, using the atmospheric
	 * scattering parameters defined by this layer.
	 * 
	 * @param dc
	 *            Current draw context, from which to get the current globe
	 *            radius and eye point
	 * @param point
	 *            Point to determine the color of
	 * @return Color of the object at the given point
	 */
	public static Color getSpaceObjectColor(DrawContext dc, Vec4 point)
	{
		double innerRadius = dc.getGlobe().getRadius();
		double outerRadius = innerRadius * Atmosphere.ATMOSPHERE_SCALE;
		Vec4 eyePoint = dc.getView().getEyePoint();

		double cameraHeight = eyePoint.getLength3();
		double cameraHeight2 = cameraHeight * cameraHeight;

		double outerRadius2 = outerRadius * outerRadius;
		Vec4 ray = point.subtract3(eyePoint);
		double far = ray.getLength3();
		ray = ray.divide3(far);

		double B = 2.0 * eyePoint.dot3(ray);
		double C = cameraHeight2 - outerRadius2;
		double det = Math.max(0.0, B * B - 4.0 * C);
		double near = 0.5 * (-B - Math.sqrt(det));
		Vec4 start = eyePoint;

		if (cameraHeight > outerRadius)
		{
			start = start.add3(ray.multiply3(near));
		}

		double scaleOverScaleDepth = (1f / (outerRadius - innerRadius)) / Atmosphere.SCALE_DEPTH;
		double height = start.getLength3();
		double depth = Math.exp(scaleOverScaleDepth * (innerRadius - cameraHeight));
		double angle = ray.dot3(start) / height;
		double scatter = depth * scale(angle, Atmosphere.SCALE_DEPTH);

		double fKr4PI = Atmosphere.RAYLEIGH_SCATTERING * 4.0 * Math.PI;
		double fKm4PI = Atmosphere.MIE_PHASE_ASYMMETRY * 4.0 * Math.PI;
		double r = Util.clamp(Math.exp(-scatter * (Atmosphere.INVWAVELENGTH4[0] * fKr4PI + fKm4PI)), 0, 1);
		double g = Util.clamp(Math.exp(-scatter * (Atmosphere.INVWAVELENGTH4[1] * fKr4PI + fKm4PI)), 0, 1);
		double b = Util.clamp(Math.exp(-scatter * (Atmosphere.INVWAVELENGTH4[2] * fKr4PI + fKm4PI)), 0, 1);
		return new Color((float) r, (float) g, (float) b);
	}

	protected static double scale(double cos, double scaleDepth)
	{
		double x = 1.0 - cos;
		return scaleDepth * Math.exp(-0.00287 + x * (0.459 + x * (3.83 + x * (-6.80 + x * 5.25))));
	}
}
