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
package au.gov.ga.earthsci.worldwind.common.layers.stars;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FloatVBO;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * Layer that renders stars using a point sprite texture. The stars are rendered
 * like a skybox, using a unit sphere, so that they appear at an almost infinite
 * distance; thus the camera cannot move outside the star field sphere.
 * <p/>
 * Stars are read from a file created from the Yale Bright Star Catalog, which
 * contains approximately 9000 stars.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InfiniteStarsLayer extends AbstractLayer
{
	protected boolean inited = false;
	protected final FloatVBO vbo = new FloatVBO(4);
	protected final FloatVBO cbo = new FloatVBO(3);
	protected Texture starTexture;
	protected final InfiniteStarsShader shader = new InfiniteStarsShader();

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!inited)
		{
			init(dc);
			inited = true;
		}
		if (shader.isCreationFailed())
		{
			return;
		}

		GL2 gl = dc.getGL().getGL2();
		OGLStackHandler ogsh = new OGLStackHandler();
		try
		{
			ogsh.pushAttrib(gl, GL2.GL_TEXTURE_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT
					| GL2.GL_POINT_BIT);
			ogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
			ogsh.pushProjection(gl);
			ogsh.pushModelview(gl);

			IDelegateView view = (IDelegateView) dc.getView();
			double near = 0.1;
			double far = near + 1.0;
			double normalizedEyeSize = view.getEyePoint().getLength3() / dc.getGlobe().getRadius();
			double loglog = Math.log(Math.log(normalizedEyeSize) + 1) + 1;
			double fovMultiplier = loglog / 4.0;
			double extraFieldOfView = 20 * fovMultiplier; //as we zoom out/in, change the FOV slightly
			Angle fieldOfView = Angle.fromDegrees(Math.max(view.getFieldOfView().degrees, 70) + extraFieldOfView); //limit the minimum FOV
			Matrix projection = view.computeProjection(fieldOfView, near, far);

			double[] matrixArray = new double[16];
			projection.toArray(matrixArray, 0, false);
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glLoadMatrixd(matrixArray, 0);

			Vec4 up = dc.getView().getUpVector();
			Vec4 f = dc.getView().getForwardVector();
			Vec4 s = f.cross3(up);
			Vec4 u = s.cross3(f);
			Matrix rotation = new Matrix(
					s.x, s.y, s.z, 0.0,
					u.x, u.y, u.z, 0.0,
					-f.x, -f.y, -f.z, 0.0,
					0.0, 0.0, 0.0, 1.0);
			rotation.toArray(matrixArray, 0, false);
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glLoadMatrixd(matrixArray, 0);

			gl.glDepthMask(false);
			gl.glDisable(GL2.GL_DEPTH_TEST);

			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glEnable(GL2.GL_POINT_SPRITE);
			gl.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);

			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

			gl.glActiveTexture(GL2.GL_TEXTURE0);
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);
			starTexture.bind(gl);

			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			vbo.bind(gl);
			gl.glVertexPointer(vbo.getElementStride(), GL2.GL_FLOAT, 0, 0);

			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
			cbo.bind(gl);
			gl.glColorPointer(cbo.getElementStride(), GL2.GL_FLOAT, 0, 0);

			useShader(gl);
			{
				gl.glDrawArrays(GL2.GL_POINTS, 0, vbo.getBuffer().length / vbo.getElementStride());
			}
			unuseShader(gl);

			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		}
		finally
		{
			ogsh.pop(gl);
		}
	}

	protected void useShader(GL2 gl)
	{
		shader.use(gl);
	}

	protected void unuseShader(GL2 gl)
	{
		shader.unuse(gl);
	}

	protected void init(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		if (!shader.create(gl))
		{
			return;
		}

		try
		{
			BufferedImage starImage = ImageIO.read(InfiniteStarsLayer.class.getResourceAsStream("star.png")); //$NON-NLS-1$
			TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), starImage, true);
			starTexture = TextureIO.newTexture(textureData);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			ObjectInputStream is = new ObjectInputStream(InfiniteStarsLayer.class.getResourceAsStream("stars.dat")); //$NON-NLS-1$
			List<Vec4> list = new ArrayList<Vec4>();
			List<Integer> colors = new ArrayList<Integer>();

			while (true)
			{
				try
				{
					float rightAscension = is.readFloat();
					float declination = is.readFloat();
					float magnitude = is.readFloat();
					int color = is.readInt();
					Vec4 cartesian = sphericalToCartesian(declination, rightAscension, 1);
					Vec4 withMagnitude = new Vec4(cartesian.x, cartesian.y, cartesian.z, magnitude);
					list.add(withMagnitude);
					colors.add(color);
				}
				catch (EOFException e)
				{
					break;
				}
			}

			float[] buffer = new float[list.size() * 4];
			int pos = 0;
			for (Vec4 v : list)
			{
				buffer[pos++] = (float) v.x;
				buffer[pos++] = (float) v.y;
				buffer[pos++] = (float) v.z;
				buffer[pos++] = (float) v.w;
			}
			vbo.setBuffer(buffer);

			buffer = new float[colors.size() * 3];
			pos = 0;
			for (Integer color : colors)
			{
				int r = (color >> 16) & 0xff;
				int g = (color >> 8) & 0xff;
				int b = (color) & 0xff;
				buffer[pos++] = r / 255f;
				buffer[pos++] = g / 255f;
				buffer[pos++] = b / 255f;
			}
			cbo.setBuffer(buffer);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected static Vec4 sphericalToCartesian(double declination, double rightAscension, double radius)
	{
		declination *= Math.PI / 180.0f;
		rightAscension *= Math.PI / 180.0f;
		double radCosLat = radius * Math.cos(declination);
		return new Vec4(
				radCosLat * Math.sin(rightAscension),
				radius * Math.sin(declination),
				radCosLat * Math.cos(rightAscension));
	}

	@Override
	public String toString()
	{
		return "Stars";
	}
}
