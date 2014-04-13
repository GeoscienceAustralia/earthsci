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

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import au.gov.ga.earthsci.worldwind.common.view.hmd.HMDDistortion;
import au.gov.ga.earthsci.worldwind.common.view.hmd.HMDView;
import au.gov.ga.earthsci.worldwind.common.view.hmd.IHMDParameters;

/**
 * {@link HMDView} implementation for the Oculus Rift. Uses the JRift library.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OculusView extends HMDView implements IOculusListener, Disposable
{
	private final OculusSingleton oculus = OculusSingleton.getInstance();
	private final HMDDistortion distortion;
	private Matrix headRotation = Matrix.IDENTITY;

	public OculusView()
	{
		if (!oculus.isInitialized())
		{
			distortion = null;
			return;
		}

		IHMDParameters parameters = oculus.getParameters();
		distortion =
				new HMDDistortion(parameters, parameters.getHorizontalResolution(), parameters.getVerticalResolution());
		oculus.addListener(this);
	}

	@Override
	public HMDDistortion getDistortion()
	{
		return distortion;
	}

	@Override
	protected Matrix transformModelView(Matrix modelView)
	{
		return headRotation.multiply(modelView);
	}

	@Override
	public void trackingUpdated(Angle yaw, Angle pitch, Angle roll)
	{
		Matrix z = Matrix.fromRotationZ(roll);
		Matrix xy = Matrix.fromRotationXYZ(pitch, yaw, Angle.ZERO);
		headRotation = z.multiply(xy);
		firePropertyChange(AVKey.VIEW, null, this);
	}

	@Override
	public void dispose()
	{
		oculus.removeListener(this);
	}
}
