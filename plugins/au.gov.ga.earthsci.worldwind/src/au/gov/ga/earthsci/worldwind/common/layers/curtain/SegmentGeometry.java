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
package au.gov.ga.earthsci.worldwind.common.layers.curtain;

import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.nio.FloatBuffer;

import javax.media.opengl.GL2;

/**
 * A {@link Renderable} piece of geometry that draws a segment (or section) of a
 * curtain/path for the {@link TiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SegmentGeometry implements Renderable
{
	private final int vertexCount;
	private final FloatBuffer vertices;
	private final FloatBuffer texCoords;
	private Vec4 referenceCenter;
	private long time;

	protected Object vboCacheKey = new Object();

	public SegmentGeometry(DrawContext dc, FloatBuffer vertices, FloatBuffer texCoords, Vec4 referenceCenter)
	{
		this.vertexCount = vertices.limit() / 3;
		this.vertices = vertices;
		this.texCoords = texCoords;
		this.referenceCenter = referenceCenter;
		time = System.currentTimeMillis();

		if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
		{
			fillVerticesVBO(dc);
		}
	}

	@Override
	public void render(DrawContext dc)
	{
		render(dc, 1);
	}

	public void render(DrawContext dc, int numTextureUnits)
	{
		dc.getView().pushReferenceCenter(dc, referenceCenter);

		GL2 gl = dc.getGL();
		OGLStackHandler ogsh = new OGLStackHandler();

		try
		{
			ogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

			if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
			{
				//Use VBOs
				int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.vboCacheKey);
				if (vboIds == null)
				{
					vboIds = fillVerticesVBO(dc);
				}
				
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboIds[0]);
				gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);

				for (int i = 0; i < numTextureUnits; i++)
				{
					gl.glClientActiveTexture(GL2.GL_TEXTURE0 + i);
					gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

					gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboIds[1]);
					gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, 0);
				}

				gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, vertexCount);
			}
			else
			{
				//Use Vertex Arrays
				gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vertices.rewind());

				for (int i = 0; i < numTextureUnits; i++)
				{
					gl.glClientActiveTexture(GL2.GL_TEXTURE0 + i);
					gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
					gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, texCoords.rewind());
				}

				gl.glDrawArrays(GL2.GL_TRIANGLE_STRIP, 0, vertexCount);
			}
		}
		finally
		{
			ogsh.pop(gl);
		}
		dc.getView().popReferenceCenter(dc);
	}

	public Vec4 getReferenceCenter()
	{
		return referenceCenter;
	}

	public FloatBuffer getVertices()
	{
		return vertices;
	}

	public FloatBuffer getTexCoords()
	{
		return texCoords;
	}

	public long getTime()
	{
		return time;
	}

	public long getSizeInBytes()
	{
		return 5 * vertexCount * Float.SIZE / 8;
	}
	
	protected void update(DrawContext dc, Vec4 referenceCenter)
    {
        this.time = System.currentTimeMillis();
        this.referenceCenter = referenceCenter;

        if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
        {
            this.fillVerticesVBO(dc);
        }
    }

	protected int[] fillVerticesVBO(DrawContext dc)
	{
		GL2 gl = dc.getGL();

		int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.vboCacheKey);
		if (vboIds == null)
		{
			vboIds = new int[2];
			gl.glGenBuffers(vboIds.length, vboIds, 0);
			int size = (vertices.limit() + texCoords.limit()) * 4;
			dc.getGpuResourceCache().put(this.vboCacheKey, vboIds, GpuResourceCache.VBO_BUFFERS, size);
		}

		try
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboIds[0]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.limit() * 4, vertices.rewind(), GL2.GL_STATIC_DRAW);
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboIds[1]);
			gl.glBufferData(GL2.GL_ARRAY_BUFFER, texCoords.limit() * 4, texCoords.rewind(), GL2.GL_STATIC_DRAW);
		}
		finally
		{
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		}

		return vboIds;
	}
}
