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
package au.gov.ga.earthsci.worldwind.common.view.oculus;

import static com.oculusvr.capi.OvrLibrary.ovrDistortionCaps.ovrDistortionCap_Chromatic;
import static com.oculusvr.capi.OvrLibrary.ovrDistortionCaps.ovrDistortionCap_TimeWarp;
import static com.oculusvr.capi.OvrLibrary.ovrDistortionCaps.ovrDistortionCap_Vignette;
import static com.oculusvr.capi.OvrLibrary.ovrEyeType.ovrEye_Count;
import static com.oculusvr.capi.OvrLibrary.ovrEyeType.ovrEye_Left;
import static com.oculusvr.capi.OvrLibrary.ovrEyeType.ovrEye_Right;
import static com.oculusvr.capi.OvrLibrary.ovrHmdCaps.ovrHmdCap_DynamicPrediction;
import static com.oculusvr.capi.OvrLibrary.ovrHmdCaps.ovrHmdCap_LowPersistence;
import static com.oculusvr.capi.OvrLibrary.ovrHmdType.ovrHmd_DK1;
import static com.oculusvr.capi.OvrLibrary.ovrTrackingCaps.ovrTrackingCap_MagYawCorrection;
import static com.oculusvr.capi.OvrLibrary.ovrTrackingCaps.ovrTrackingCap_Orientation;
import static com.oculusvr.capi.OvrLibrary.ovrTrackingCaps.ovrTrackingCap_Position;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.DrawableSceneController;
import au.gov.ga.earthsci.worldwind.common.render.FrameBuffer;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IViewDelegate;

import com.jogamp.opengl.util.GLBuffers;
import com.oculusvr.capi.DistortionMesh;
import com.oculusvr.capi.DistortionVertex;
import com.oculusvr.capi.EyeRenderDesc;
import com.oculusvr.capi.FovPort;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.OvrMatrix4f;
import com.oculusvr.capi.OvrRecti;
import com.oculusvr.capi.OvrSizei;
import com.oculusvr.capi.OvrVector2f;
import com.oculusvr.capi.OvrVector2i;
import com.oculusvr.capi.OvrVector3f;
import com.oculusvr.capi.Posef;

/**
 * {@link IViewDelegate} for the Oculus Rift.
 * <p/>
 * Based on example from <a
 * href="https://github.com/elect86/Joglus/">Joglus</a>.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RiftViewDistortionDelegate implements IViewDelegate
{
	private enum DistortionObjects
	{
		vbo,
		ibo,
		count
	}

	protected Hmd hmd;
	protected final OvrRecti[] eyeRenderViewport = (OvrRecti[]) new OvrRecti().toArray(2);
	protected final OvrVector2f[][] uvScaleOffset = new OvrVector2f[2][2];
	protected final EyeRenderDesc[] eyeRenderDescs = (EyeRenderDesc[]) new EyeRenderDesc().toArray(2);
	protected final OvrVector3f[] eyeOffsets = (OvrVector3f[]) new OvrVector3f().toArray(2);
	protected final FovPort[] eyeFov = (FovPort[]) new FovPort().toArray(2);
	protected final Posef[] eyePoses = (Posef[]) new Posef().toArray(2);
	protected int[][] distortionObjects;
	protected int indicesCount;

	protected final FrameBuffer frameBuffer = new FrameBuffer();
	protected final DistortionShader distortionShader = new DistortionShader();

	protected int frameCount = -1;
	protected boolean inited = false;
	protected boolean renderEyes = false;
	protected int eye = 0; //0 == left, 1 == right
	protected Matrix pretransformedModelView = Matrix.IDENTITY;
	protected boolean disableHeadTransform = false;

	protected boolean shouldRenderForHMD()
	{
		return renderEyes;
	}

	protected static Hmd openFirstHmd()
	{
		Hmd hmd = Hmd.create(0);
		if (null == hmd)
		{
			hmd = Hmd.createDebug(ovrHmd_DK1);
		}
		return hmd;
	}

	@Override
	public void installed(IDelegateView view)
	{
		Hmd.initialize();

		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
		}

		hmd = openFirstHmd();
		if (hmd == null)
		{
			throw new IllegalStateException("Unable to initialize HMD");
		}
		if (hmd.configureTracking(ovrTrackingCap_Orientation | ovrTrackingCap_Position, 0) == 0)
		{
			throw new IllegalStateException("Unable to start the sensor");
		}
		//hmd.enableHswDisplay(false);
	}

	@Override
	public void uninstalled(IDelegateView view)
	{
		hmd.destroy();
		Hmd.shutdown();
	}

	@Override
	public void beforeComputeMatrices(IDelegateView view)
	{
		if (shouldRenderForHMD())
		{
			//double hfovRadians = Math.atan(eyeFov[eye].LeftTan) + Math.atan(eyeFov[eye].RightTan);
			//double vfovRadians = Math.atan(eyeFov[eye].UpTan) + Math.atan(eyeFov[eye].DownTan);
			//view.setFieldOfView(Angle.fromRadians(hfovRadians));

			//TODO hmmm, something about the reported Oculus FOV above and the World Wind's View FOV
			//don't mix very well, so set it manually to a high FOV to ensure all tiles are rendered:
			view.setFieldOfView(Angle.fromDegrees(130));
		}
	}

	@Override
	public Matrix computeModelView(IDelegateView view)
	{
		pretransformedModelView = view.computeModelView();
		return transformModelView(pretransformedModelView, view);
	}

	protected Matrix transformModelView(Matrix modelView, IDelegateView view)
	{
		if (disableHeadTransform)
		{
			return modelView;
		}

		Vec4 translation = RiftUtils.toVec4(eyePoses[eye].Position);
		double translationScale = getHeadTranslationScale(view);
		Quaternion rotation = RiftUtils.toQuaternion(eyePoses[eye].Orientation);

		Matrix translationM = Matrix.fromTranslation(translation.multiply3(-translationScale));
		Matrix rotationM = Matrix.fromQuaternion(rotation.getInverse());

		return rotationM.multiply(translationM.multiply(modelView));
	}

	protected double getHeadTranslationScale(IDelegateView view)
	{
		Position eyePosition = view.getEyePosition();
		DrawContext dc = view.getDC();
		if (dc == null)
		{
			return Math.abs(eyePosition.elevation);
		}
		double altitude = ViewUtil.computeElevationAboveSurface(dc, eyePosition);
		return Math.abs(altitude);
	}

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
		return RiftUtils.toMatrix(Hmd.getPerspectiveProjection(eyeFov[eye], (float) nearDistance, (float) farDistance,
				true));
	}

	protected void init(GL2 gl)
	{
		if (inited)
		{
			return;
		}
		inited = true;

		initOculus(gl);
	}

	protected void initOculus(GL2 gl)
	{
		eyeFov[0] = hmd.DefaultEyeFov[0];
		eyeFov[1] = hmd.DefaultEyeFov[1];

		initDistortion(gl);

		hmd.setEnabledCaps(ovrHmdCap_LowPersistence | ovrHmdCap_DynamicPrediction);
		hmd.configureTracking(ovrTrackingCap_Orientation | ovrTrackingCap_MagYawCorrection | ovrTrackingCap_Position, 0);
		hmd.recenterPose();
	}

	protected void initDistortion(GL2 gl)
	{
		//Configure Stereo settings.
		OvrSizei recommendedTex0Size = hmd.getFovTextureSize(ovrEye_Left, hmd.DefaultEyeFov[0], 1f);
		OvrSizei recommendedTex1Size = hmd.getFovTextureSize(ovrEye_Right, hmd.DefaultEyeFov[1], 1f);
		int x = recommendedTex0Size.w + recommendedTex1Size.w;
		int y = Math.max(recommendedTex0Size.h, recommendedTex1Size.h);
		OvrSizei renderTargetSize = new OvrSizei(x, y);
		frameBuffer.resize(gl, new Dimension(x, y));
		// Initialize eye rendering information.
		eyeRenderViewport[0].Pos = new OvrVector2i(0, 0);
		eyeRenderViewport[0].Size = new OvrSizei(renderTargetSize.w / 2, renderTargetSize.h);
		eyeRenderViewport[1].Pos = new OvrVector2i((renderTargetSize.w + 1) / 2, 0);
		eyeRenderViewport[1].Size = eyeRenderViewport[0].Size;
		distortionShader.create(gl);
		distortionObjects = new int[ovrEye_Count][DistortionObjects.count.ordinal()];
		for (int eyeNum = 0; eyeNum < ovrEye_Count; eyeNum++)
		{
			int distortionCaps = ovrDistortionCap_Chromatic | ovrDistortionCap_TimeWarp | ovrDistortionCap_Vignette;
			DistortionMesh meshData = hmd.createDistortionMesh(eyeNum, eyeFov[eyeNum], distortionCaps);
			DistortionVertex[] distortionVertices = new DistortionVertex[meshData.VertexCount];
			meshData.pVertexData.toArray(distortionVertices);
			{
				initDistortionVBOs(gl, eyeNum, distortionVertices);
			}
			short[] indicesData = meshData.pIndexData.getPointer().getShortArray(0, meshData.IndexCount);
			indicesCount = indicesData.length;
			{
				initDistortionIBO(gl, eyeNum, indicesData);
			}
			eyeRenderDescs[eyeNum] = hmd.getRenderDesc(eyeNum, eyeFov[eyeNum]);
			uvScaleOffset[eyeNum] = Hmd.getRenderScaleAndOffset(eyeFov[eyeNum], renderTargetSize,
					eyeRenderViewport[eyeNum]);
			eyeOffsets[eyeNum].x = eyeRenderDescs[eyeNum].HmdToEyeViewOffset.x;
			eyeOffsets[eyeNum].y = eyeRenderDescs[eyeNum].HmdToEyeViewOffset.y;
			eyeOffsets[eyeNum].z = eyeRenderDescs[eyeNum].HmdToEyeViewOffset.z;
		}
	}

	protected void initDistortionVBOs(GL2 gl, int eyeNum, DistortionVertex[] structures)
	{
		int vertexSize = 2 + 1 + 1 + 2 + 2 + 2;
		float[] vertexData = new float[vertexSize * structures.length];
		for (int v = 0; v < structures.length; v++)
		{
			vertexData[v * vertexSize + 0] = structures[v].ScreenPosNDC.x;
			vertexData[v * vertexSize + 1] = structures[v].ScreenPosNDC.y;
			vertexData[v * vertexSize + 2] = structures[v].TimeWarpFactor;
			vertexData[v * vertexSize + 3] = structures[v].VignetteFactor;
			vertexData[v * vertexSize + 4] = structures[v].TanEyeAnglesR.x;
			vertexData[v * vertexSize + 5] = structures[v].TanEyeAnglesR.y;
			vertexData[v * vertexSize + 6] = structures[v].TanEyeAnglesG.x;
			vertexData[v * vertexSize + 7] = structures[v].TanEyeAnglesG.y;
			vertexData[v * vertexSize + 8] = structures[v].TanEyeAnglesB.x;
			vertexData[v * vertexSize + 9] = structures[v].TanEyeAnglesB.y;
		}
		gl.glGenBuffers(1, distortionObjects[eyeNum], DistortionObjects.vbo.ordinal());
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, distortionObjects[eyeNum][DistortionObjects.vbo.ordinal()]);
		{
			FloatBuffer fb = GLBuffers.newDirectFloatBuffer(vertexData);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertexData.length * 4, fb, GL2.GL_STATIC_DRAW);
		}
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}

	protected void initDistortionIBO(GL2 gl, int eyeNum, short[] shortIndicesData)
	{
		int[] intIndicesData = new int[shortIndicesData.length];
		for (int i = 0; i < shortIndicesData.length; i++)
		{
			intIndicesData[i] = shortIndicesData[i];
		}
		gl.glGenBuffers(1, distortionObjects[eyeNum], DistortionObjects.ibo.ordinal());
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, distortionObjects[eyeNum][DistortionObjects.ibo.ordinal()]);
		{
			IntBuffer intBuffer = GLBuffers.newDirectIntBuffer(intIndicesData);
			gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, intIndicesData.length * 4, intBuffer, GL2.GL_STATIC_DRAW);
		}
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void pick(IDelegateView view, DrawContext dc, DrawableSceneController sc)
	{
		view.pick(dc, sc);
		sc.clearFrame(dc);
		fixViewportCenterPosition(dc, sc);
	}

	@Override
	public void draw(IDelegateView view, DrawContext dc, DrawableSceneController sc)
	{
		GL2 gl = dc.getGL().getGL2();
		init(gl);

		if (distortionShader.isCreationFailed())
		{
			view.draw(dc, sc);
			return;
		}

		Rectangle oldViewport = view.getViewport();

		hmd.beginFrameTiming(++frameCount);
		{
			Posef[] eyePoses = hmd.getEyePoses(frameCount, eyeOffsets);
			//RiftLogger.logPose(eyePoses);

			renderEyes = true;
			frameBuffer.bind(gl);
			{
				sc.clearFrame(dc);

				for (int i = 0; i < ovrEye_Count; i++)
				{
					int eye = hmd.EyeRenderOrder[i];
					Posef pose = eyePoses[eye];
					this.eyePoses[eye].Orientation = pose.Orientation;
					this.eyePoses[eye].Position = pose.Position;

					this.eye = eye;

					gl.glViewport(eyeRenderViewport[eye].Pos.x, eyeRenderViewport[eye].Pos.y,
							eyeRenderViewport[eye].Size.w, eyeRenderViewport[eye].Size.h);

					sc.applyView(dc);
					sc.draw(dc);
				}
			}
			frameBuffer.unbind(gl);
			renderEyes = false;

			OGLStackHandler oglsh = new OGLStackHandler();
			oglsh.pushAttrib(gl, GL2.GL_ENABLE_BIT);
			oglsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
			try
			{
				gl.glViewport(0, 0, hmd.Resolution.w, hmd.Resolution.h);
				gl.glDisable(GL2.GL_DEPTH_TEST);

				gl.glEnable(GL2.GL_TEXTURE_2D);
				gl.glActiveTexture(GL2.GL_TEXTURE0);
				gl.glBindTexture(GL2.GL_TEXTURE_2D, frameBuffer.getTexture().getId());
				for (int eyeNum = 0; eyeNum < ovrEye_Count; eyeNum++)
				{
					OvrMatrix4f[] timeWarpMatricesRowMajor = new OvrMatrix4f[2];
					hmd.getEyeTimewarpMatrices(eyeNum, eyePoses[eyeNum], timeWarpMatricesRowMajor);
					distortionShader.use(gl, uvScaleOffset[eyeNum][0].x, -uvScaleOffset[eyeNum][0].y,
							uvScaleOffset[eyeNum][1].x, 1 - uvScaleOffset[eyeNum][1].y, timeWarpMatricesRowMajor[0].M,
							timeWarpMatricesRowMajor[1].M);

					gl.glClientActiveTexture(GL2.GL_TEXTURE0);
					gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
					gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
					gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

					gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, distortionObjects[eyeNum][DistortionObjects.vbo.ordinal()]);
					{
						int stride = 10 * 4;
						gl.glVertexPointer(4, GL2.GL_FLOAT, stride, 0);
						gl.glTexCoordPointer(2, GL2.GL_FLOAT, stride, 4 * 4);
						gl.glColorPointer(4, GL2.GL_FLOAT, stride, 6 * 4);

						gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER,
								distortionObjects[eyeNum][DistortionObjects.ibo.ordinal()]);
						{
							gl.glDrawElements(GL2.GL_TRIANGLES, indicesCount, GL2.GL_UNSIGNED_INT, 0);
						}
						gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
					}
					gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

					distortionShader.unuse(gl);
				}
			}
			finally
			{
				oglsh.pop(gl);
			}
		}
		hmd.endFrameTiming();

		//apply the old viewport, and ensure that the view is updated for the next picking round
		gl.glViewport(oldViewport.x, oldViewport.y, oldViewport.width, oldViewport.height);
		sc.applyView(dc);

		view.firePropertyChange(AVKey.VIEW, null, view); //make the view draw repeatedly for oculus rotation
	}

	@Override
	public boolean isTranslateAbsAllowed()
	{
		return false;
	}

	/**
	 * Transforming the modelview matrix with the Rift's head rotation causes
	 * the {@link OrbitView#focusOnViewportCenter()} method to focus on the
	 * center of the viewport, which means the view jumps around depending on
	 * the head rotation. This method calls the focus method using an
	 * untransformed modelview matrix, keeping the center rotation point more
	 * consistent.
	 * 
	 * @param dc
	 */
	protected void fixViewportCenterPosition(DrawContext dc, DrawableSceneController sc)
	{
		dc.setViewportCenterPosition(null);
		Point vpc = dc.getViewportCenterScreenPoint();
		if (vpc == null)
		{
			return;
		}

		try
		{
			disableHeadTransform = true;
			sc.applyView(dc);
			dc.enablePickingMode();

			List<Point> points = Arrays.asList(new Point[] { vpc });
			List<PickedObject> pickedObjects = dc.getSurfaceGeometry().pick(dc, points);

			if (pickedObjects == null || pickedObjects.size() == 0)
			{
				return;
			}

			dc.setViewportCenterPosition((Position) pickedObjects.get(0).getObject());
		}
		finally
		{
			disableHeadTransform = false;
			sc.applyView(dc);
			dc.disablePickingMode();
		}
	}
}
