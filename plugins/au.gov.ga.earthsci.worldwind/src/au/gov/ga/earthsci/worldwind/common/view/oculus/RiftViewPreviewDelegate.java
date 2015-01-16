/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.view.oculus;

import static com.oculusvr.capi.OvrLibrary.ovrEyeType.ovrEye_Count;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.DrawableSceneController;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;

import com.oculusvr.capi.Posef;

/**
 * Subclass of {@link RiftViewDistortionDelegate} that displays a single eye of
 * the Rift's view, without any distortion. Useful for previewing what the Rift
 * sees on a secondary monitor.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RiftViewPreviewDelegate extends RiftViewDistortionDelegate
{
	@Override
	protected void initDistortion(GL2 gl)
	{
	}

	@Override
	public void draw(IDelegateView view, DrawContext dc, DrawableSceneController sc)
	{
		GL2 gl = dc.getGL().getGL2();
		init(gl);

		hmd.beginFrameTiming(++frameCount);

		Posef[] eyePoses = hmd.getEyePoses(frameCount, eyeOffsets);
		for (int i = 0; i < ovrEye_Count; i++)
		{
			int eye = hmd.EyeRenderOrder[i];
			Posef pose = eyePoses[eye];
			this.eyePoses[eye].Orientation = pose.Orientation;
			this.eyePoses[eye].Position = pose.Position;
		}

		sc.applyView(dc);
		sc.draw(dc);

		hmd.endFrameTiming();

		view.firePropertyChange(AVKey.VIEW, null, view); //make the view draw repeatedly for oculus rotation
	}
}
