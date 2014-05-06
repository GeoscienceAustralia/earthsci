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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.DrawableSceneController;
import au.gov.ga.earthsci.worldwind.common.render.FrameBuffer;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;
import au.gov.ga.earthsci.worldwind.common.view.stereo.IStereoViewDelegate.Eye;

/**
 * Abstract OrbitView implementation of {@link IHMDViewDelegate}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class HMDViewDelegate implements IHMDViewDelegate
{
	private Matrix pretransformedModelView = Matrix.IDENTITY;

	private boolean renderEyes = false;
	private Eye eye = Eye.LEFT;

	private FrameBuffer leftBuffer = new FrameBuffer();
	private FrameBuffer rightBuffer = new FrameBuffer();
	private BarrelDistortionShader shader = new BarrelDistortionShader();

	protected boolean shouldRenderForHMD()
	{
		return getDistortion() != null && renderEyes;
	}

	@Override
	public void beforeComputeMatrices(IDelegateView view)
	{
		if (shouldRenderForHMD())
		{
			HMDDistortion distortion = getDistortion();
			distortion.update();

			//field of view is calculated from the HMD distortion
			float verticalFOV = distortion.getVerticalFOV();
			float aspect = distortion.getAspect();
			double horizontalFOV = 2d * Math.atan(Math.tan(verticalFOV / 2d) * aspect);
			view.setFieldOfView(Angle.fromRadians(horizontalFOV));

			System.out.println(Angle.fromRadians(horizontalFOV));
		}
	}

	@Override
	public Matrix computeModelView(IDelegateView view)
	{
		Matrix modelView = view.computeModelView();
		if (shouldRenderForHMD())
		{
			double multiplier = dynamicEyeSeparationMultiplier(view);
			modelView = Matrix.fromTranslation(
					(eye == Eye.LEFT ? -1 : 1) * -getDistortion().getInterpupillaryDistance() * 0.5
							* multiplier, 0, 0).multiply(modelView);
		}
		pretransformedModelView = modelView;
		return transformModelView(pretransformedModelView);
	}

	private double dynamicEyeSeparationMultiplier(IDelegateView view)
	{
		Vec4 eyePoint = view.getCurrentEyePoint();
		double distanceFromOrigin = eyePoint.getLength3();

		Globe globe = view.getGlobe();
		double radius = globe.getRadiusAt(globe.computePositionFromPoint(eyePoint));
		double amount = Util.percentDouble(distanceFromOrigin, radius, radius * 5d);

		Vec4 centerPoint = view.getCenterPoint();
		double distanceToCenter;
		if (centerPoint != null)
		{
			distanceToCenter = eyePoint.distanceTo3(centerPoint);
		}
		else
		{
			distanceToCenter = view.getHorizonDistance();
		}

		//limit the distance to center relative to the eye distance from the ellipsoid surface
		double maxDistanceToCenter = (distanceFromOrigin - radius) * 10;
		distanceToCenter = Math.min(maxDistanceToCenter, distanceToCenter);

		//calculate focal length as distance to center when zoomed in, and distance from origin when zoomed out
		double focalLength = Util.mixDouble(amount, distanceToCenter, distanceFromOrigin);

		//exaggerate separation more when zoomed out
		//double separationExaggeration = Util.mixDouble(amount, separationExaggeration, separationExaggeration * 4);

		//move focal length closer as view is pitched
		amount = Util.percentDouble(view.getPitch().degrees, 0d, 90d);
		focalLength = Util.mixDouble(amount, focalLength, focalLength / 3d);

		//calculate eye separation linearly relative to focal length
		//double eyeSeparation = separationExaggeration * focalLength / 100d;

		return focalLength;
	}

	/**
	 * Transform the model view matrix for any head tracking/rotation of the
	 * HMD.
	 * 
	 * @param modelView
	 * @return Transformed model view matrix
	 */
	protected abstract Matrix transformModelView(Matrix modelView);

	@Override
	public Matrix getPretransformedModelView(IDelegateView view)
	{
		return pretransformedModelView;
	}

	@Override
	public Matrix computeProjection(IDelegateView view, Angle horizontalFieldOfView, double nearDistance,
			double farDistance)
	{
		if (!shouldRenderForHMD())
		{
			return view.computeProjection(horizontalFieldOfView, nearDistance, farDistance);
		}

		HMDDistortion distortion = getDistortion();
		boolean left = eye == Eye.LEFT;
		float projectionCenterOffset = distortion.getProjectionCenterOffset();
		float verticalFOV = distortion.getVerticalFOV();
		float aspect = distortion.getAspect();

		Matrix m = perspective(verticalFOV, aspect, nearDistance, farDistance);
		Matrix t = Matrix.fromTranslation(left ? projectionCenterOffset : -projectionCenterOffset, 0, 0);
		return t.multiply(m);
	}

	private static Matrix perspective(double verticalFov, double aspectRatio, double nearDistance, double farDistance)
	{
		//verticalFov is in radians
		double oneOnTanHalfFov = 1 / Math.tan(0.5 * verticalFov);
		double zRange = nearDistance - farDistance;

		//from man pages of gluPerspective:
		//return new Matrix(oneOnTanHalfFov / aspectRatio, 0, 0, 0,
		//		0, oneOnTanHalfFov, 0, 0,
		//		0, 0, (farDistance + nearDistance) / zRange, (2 * farDistance * nearDistance) / zRange,
		//		0, 0, -1, 0);

		//from Oculus Rift SDK documentation:
		return new Matrix(oneOnTanHalfFov / aspectRatio, 0, 0, 0,
				0, oneOnTanHalfFov, 0, 0,
				0, 0, farDistance / zRange, farDistance * nearDistance / zRange,
				0, 0, -1, 0);
	}

	@Override
	public void draw(IDelegateView view, DrawContext dc, DrawableSceneController sc)
	{
		HMDDistortion distortion = getDistortion();
		if (distortion == null)
		{
			sc.draw(dc);
			return;
		}

		int width = distortion.getWindowWidth(), height = distortion.getWindowHeight();
		int widthd2 = width / 2;

		GL2 gl = dc.getGL().getGL2();
		Dimension bufferDimension = new Dimension(width, height * 2);
		leftBuffer.resize(gl, bufferDimension);
		rightBuffer.resize(gl, bufferDimension);
		shader.createIfRequired(gl);

		renderEyes = true;
		{
			//render left eye:
			leftBuffer.bind(gl);
			{
				sc.clearFrame(dc);

				gl.glViewport(0, 0, bufferDimension.width, bufferDimension.height);
				eye = Eye.LEFT;
				sc.applyView(dc);
				sc.draw(dc);
			}
			leftBuffer.unbind(gl);

			//render right eye:
			rightBuffer.bind(gl);
			{
				sc.clearFrame(dc);

				gl.glViewport(0, 0, bufferDimension.width, bufferDimension.height);
				eye = Eye.RIGHT;
				sc.applyView(dc);
				sc.draw(dc);
			}
			rightBuffer.unbind(gl);
		}
		renderEyes = false;

		//reset view:
		gl.glViewport(0, 0, width, height);
		sc.applyView(dc);

		shader.use(dc, distortion, 0, 0, widthd2, height, true);
		renderTexturedQuad(gl, leftBuffer.getTexture().getId(), true);
		shader.use(dc, distortion, widthd2, 0, widthd2, height, false);
		renderTexturedQuad(gl, rightBuffer.getTexture().getId(), false);
		shader.unuse(gl);
	}

	public static void renderTexturedQuad(GL2 gl, int textureId, boolean left)
	{
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glPushAttrib(GL2.GL_ENABLE_BIT);

		try
		{
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glActiveTexture(GL2.GL_TEXTURE0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, textureId);

			gl.glBegin(GL2.GL_TRIANGLE_STRIP);
			if (left)
			{
				gl.glTexCoord2f(0.0f, 0.0f);
				gl.glVertex2f(-1.0f, -1.0f);
				gl.glTexCoord2f(0.5f, 0.0f);
				gl.glVertex2f(0.0f, -1.0f);
				gl.glTexCoord2f(0.0f, 1.0f);
				gl.glVertex2f(-1.0f, 1.0f);
				gl.glTexCoord2f(0.5f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			else
			{
				gl.glTexCoord2f(0.5f, 0.0f);
				gl.glVertex2f(0.0f, -1.0f);
				gl.glTexCoord2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, -1.0f);
				gl.glTexCoord2f(0.5f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
				gl.glTexCoord2f(1.0f, 1.0f);
				gl.glVertex2f(1.0f, 1.0f);
			}
			gl.glEnd();
		}
		finally
		{
			gl.glPopMatrix();
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glPopMatrix();
			gl.glPopAttrib();
		}
	}
}
