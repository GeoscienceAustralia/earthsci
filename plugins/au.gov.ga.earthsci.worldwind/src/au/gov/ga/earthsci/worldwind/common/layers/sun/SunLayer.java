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
package au.gov.ga.earthsci.worldwind.common.layers.sun;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.layers.atmosphere.Atmosphere;
import au.gov.ga.earthsci.worldwind.common.render.FrameBufferStack;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * Layer that renders a sun, with lens flare, glow, halo, and dirty lens
 * effects.
 * <p/>
 * Based on the code by Michal Belanec <a
 * href="http://www.belanecbn.sk/3dtutorials/index.php?id=7">here</a>.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SunLayer extends AbstractLayer
{
	protected final float sunSize = 20;
	protected Vec4 lightDirection = new Vec4(0, 0, 1000).normalize3();

	protected boolean inited = false;
	protected Dimension size;
	protected Dimension stageTextureSize;
	protected Texture circleTexture;
	protected Texture dirtTexture;
	protected final int[] depthTexture = new int[1];
	protected final int[] stageTextures = new int[3];
	protected final int[] fbo = new int[1];
	protected final SunDepthTestShader sunDepthTestShader = new SunDepthTestShader();
	protected final SunRaysLensFlareHaloShader sunRaysLensFlareHaloShader = new SunRaysLensFlareHaloShader();
	protected final BlurHorizontalShader blurHorizontalShader = new BlurHorizontalShader();
	protected final BlurVerticalShader blurVerticalShader = new BlurVerticalShader();

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!inited)
		{
			init(dc);
			inited = true;
		}
		resize(dc);
		renderSun(dc);
	}

	protected void init(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		//load the textures
		try
		{
			BufferedImage circleImage = ImageIO.read(SunLayer.class.getResourceAsStream("circle.png")); //$NON-NLS-1$
			TextureData td = AWTTextureIO.newTextureData(gl.getGLProfile(), circleImage, true);
			circleTexture = TextureIO.newTexture(td);

			BufferedImage dirtImage = ImageIO.read(SunLayer.class.getResourceAsStream("dirtylens.jpg")); //$NON-NLS-1$
			td = AWTTextureIO.newTextureData(gl.getGLProfile(), dirtImage, false);
			dirtTexture = TextureIO.newTexture(td);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		//generate textures and a frame buffer object
		gl.glGenTextures(depthTexture.length, depthTexture, 0);
		gl.glGenTextures(stageTextures.length, stageTextures, 0);
		gl.glGenFramebuffers(fbo.length, fbo, 0);

		//compile the shaders
		sunDepthTestShader.create(gl);
		sunRaysLensFlareHaloShader.create(gl);
		blurHorizontalShader.create(gl);
		blurVerticalShader.create(gl);
	}

	protected void resize(DrawContext dc)
	{
		View view = dc.getView();
		Rectangle viewport = view.getViewport();
		Dimension size = viewport.getSize();

		if (size.equals(this.size))
		{
			return;
		}
		this.size = size;

		GL2 gl = dc.getGL().getGL2();

		//generate a depth texture for copying the current scene's depth buffer to
		gl.glBindTexture(GL2.GL_TEXTURE_2D, depthTexture[0]);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT24, size.width, size.height, 0,
				GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, null);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

		//generate the sun staging textures at half-res
		stageTextureSize = new Dimension(size.width / 2, size.height / 2);
		for (int i = 0; i < stageTextures.length; i++)
		{
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[i]);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, stageTextureSize.width, stageTextureSize.height, 0,
					GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		}
	}

	protected void renderSun(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		//calculate view rotation
		Vec4 up = dc.getView().getUpVector();
		Vec4 f = dc.getView().getForwardVector();
		Vec4 s = f.cross3(up);
		Vec4 u = s.cross3(f);
		Matrix viewRotation = new Matrix(
				s.x, s.y, s.z, 0.0,
				u.x, u.y, u.z, 0.0,
				-f.x, -f.y, -f.z, 0.0,
				0.0, 0.0, 0.0, 1.0);

		//compute modelview matrix by combining view rotation and light direction rotation
		Angle lightAngle = Vec4.UNIT_Z.angleBetween3(lightDirection);
		Vec4 lightAxis = Vec4.UNIT_Z.cross3(lightDirection);
		Matrix lightRotation = Matrix.fromAxisAngle(lightAngle, lightAxis);
		Matrix modelview = viewRotation.multiply(lightRotation);

		//compute the view's projection matrix
		IDelegateView view = (IDelegateView) dc.getView();
		Matrix projection = view.computeProjection(0.1, 1.5);

		//project the sun's direction onto the view plane using gluProject
		Rectangle viewport = view.getViewport();
		double[] modelviewArray = new double[16];
		double[] projectionArray = new double[16];
		modelview.toArray(modelviewArray, 0, false);
		projection.toArray(projectionArray, 0, false);
		int[] viewportArray = new int[] { viewport.x, viewport.y, viewport.width, viewport.height };
		double[] result = new double[3];
		dc.getGLU().gluProject(0.0, 0.0, 1.0, modelviewArray, 0, projectionArray, 0, viewportArray, 0, result, 0);

		//calculate sun screen position in screen coordinates
		double projectedSunPosX = (viewport.x + result[0]) / viewport.width;
		double projectedSunPosY = (viewport.y + result[1]) / viewport.height;
		double sunWidth = 0.5 * sunSize / viewport.width;
		double sunHeight = 0.5 * sunSize / viewport.height;
		double x1 = projectedSunPosX - sunWidth;
		double x2 = projectedSunPosX + sunWidth;
		double y1 = projectedSunPosY - sunHeight;
		double y2 = projectedSunPosY + sunHeight;

		if (!(0 <= x2 && x1 <= 1 && 0 <= y2 && y1 <= 1 && 0 <= result[2] && result[2] <= 1))
		{
			//sun not within viewport, so don't render
			return;
		}

		double directionScale = dc.getGlobe().getRadius() * dc.getGlobe().getRadius();
		Color sunColor = Atmosphere.getSpaceObjectColor(dc, lightDirection.multiply3(directionScale));

		OGLStackHandler ogsh = new OGLStackHandler();
		try
		{
			//switch to ortho mode
			ogsh.pushModelviewIdentity(gl);
			ogsh.pushProjectionIdentity(gl);
			ogsh.pushAttrib(gl, GL2.GL_TEXTURE_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT
					| GL2.GL_VIEWPORT_BIT);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glOrthof(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);

			//copy depth buffer to texture
			gl.glActiveTexture(GL2.GL_TEXTURE0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, depthTexture[0]);
			gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, size.width, size.height);

			//render the sun circle texture
			gl.glViewport(0, 0, stageTextureSize.width, stageTextureSize.height);
			FrameBufferStack.push(gl, fbo[0]);
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D,
					stageTextures[0], 0);
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D, 0, 0);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT); //clear the frame buffer
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glColor3d(sunColor.getRed() / 255.0, sunColor.getGreen() / 255.0, sunColor.getBlue() / 255.0);
			circleTexture.bind(gl);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glTexCoord2f(0, 0);
				gl.glVertex3d(x1, y1, -1);
				gl.glTexCoord2f(1, 0);
				gl.glVertex3d(x2, y1, -1);
				gl.glTexCoord2f(1, 1);
				gl.glVertex3d(x2, y2, -1);
				gl.glTexCoord2f(0, 1);
				gl.glVertex3d(x1, y2, -1);
			}
			gl.glEnd();
			gl.glColor3d(1.0, 1.0, 1.0);

			//test if sun sphere is behind scene geometry
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D,
					stageTextures[1], 0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[0]);
			gl.glActiveTexture(GL2.GL_TEXTURE1);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, depthTexture[0]);
			sunDepthTestShader.use(gl);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glVertex2f(0.0f, 0.0f);
				gl.glVertex2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			gl.glEnd();
			sunDepthTestShader.unuse(gl);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE0);

			//blur sun sphere horizontally (low)
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D,
					stageTextures[0], 0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[1]);
			blurHorizontalShader.use(gl, 1, 1.0f / stageTextureSize.width);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glVertex2f(0.0f, 0.0f);
				gl.glVertex2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			gl.glEnd();
			blurHorizontalShader.unuse(gl);

			//blur sun sphere vertically (low)
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D,
					stageTextures[2], 0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[0]);
			blurVerticalShader.use(gl, 1, 1.0f / stageTextureSize.height);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glVertex2f(0.0f, 0.0f);
				gl.glVertex2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			gl.glEnd();
			blurVerticalShader.unuse(gl);

			//blur sun sphere horizontally (high)
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D,
					stageTextures[0], 0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[1]);
			blurHorizontalShader.use(gl, 10, 1.0f / stageTextureSize.width);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glVertex2f(0.0f, 0.0f);
				gl.glVertex2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			gl.glEnd();
			blurHorizontalShader.unuse(gl);

			//blur sun sphere vertically (high)
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D,
					stageTextures[1], 0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[0]);
			blurVerticalShader.use(gl, 10, 1.0f / stageTextureSize.height);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glVertex2f(0.0f, 0.0f);
				gl.glVertex2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			gl.glEnd();
			blurVerticalShader.unuse(gl);

			//blur sun sphere radially and calculate lens flare and halo and apply dirt texture
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_TEXTURE_2D,
					stageTextures[0], 0);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[2]);
			gl.glActiveTexture(GL2.GL_TEXTURE1);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[1]);
			gl.glActiveTexture(GL2.GL_TEXTURE2);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, dirtTexture.getTextureObject(gl));
			sunRaysLensFlareHaloShader.use(gl, (float) projectedSunPosX, (float) projectedSunPosY);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glVertex2f(0.0f, 0.0f);
				gl.glVertex2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			gl.glEnd();
			sunRaysLensFlareHaloShader.unuse(gl);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE1);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
			gl.glActiveTexture(GL2.GL_TEXTURE0);

			//unbind frame buffer and reset viewport
			FrameBufferStack.pop(gl);
			gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);

			//render the final composited sun texture to the screen
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, stageTextures[0]);
			gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_COLOR);
			gl.glEnable(GL2.GL_BLEND);
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glTexCoord2f(0.0f, 0.0f);
				gl.glVertex2f(0.0f, 0.0f);
				gl.glTexCoord2f(1.0f, 0.0f);
				gl.glVertex2f(1.0f, 0.0f);
				gl.glTexCoord2f(1.0f, 1.0f);
				gl.glVertex2f(1.0f, 1.0f);
				gl.glTexCoord2f(0.0f, 1.0f);
				gl.glVertex2f(0.0f, 1.0f);
			}
			gl.glEnd();
			gl.glDisable(GL2.GL_BLEND);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
			gl.glDisable(GL2.GL_TEXTURE_2D);
		}
		finally
		{
			ogsh.pop(gl);
		}
	}
}
