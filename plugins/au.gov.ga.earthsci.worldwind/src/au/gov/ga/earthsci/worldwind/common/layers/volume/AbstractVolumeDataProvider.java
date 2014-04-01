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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Position;

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.earthsci.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * Abstract implementation of the {@link VolumeDataProvider} interface. Provides
 * getter methods to the required fields, as well as curtain and surface shape
 * calculation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractVolumeDataProvider extends AbstractDataProvider<VolumeLayer> implements
		VolumeDataProvider
{

	/**
	 * Number of samples in the volume data along the x-axis.
	 */
	protected int xSize;

	/**
	 * Number of samples in the volume data along the y-axis;
	 */
	protected int ySize;

	/**
	 * Number of samples in the volume data along the z-axis;
	 */
	protected int zSize;

	/**
	 * Approximate sector containing the volume data.
	 */
	protected Bounds bounds = null;

	/**
	 * Average top elevation of the volume data (in meters).
	 */
	protected double top;

	/**
	 * Depth (distance between top and bottom slice) of the volume data (in
	 * meters).
	 */
	protected double depth;

	/**
	 * Value in the data that represents NODATA.
	 */
	protected float noDataValue;

	/**
	 * Is the volume data reversed along the x-axis?
	 */
	protected boolean reverseX = false;

	/**
	 * Is the volume data reversed along the y-axis?
	 */
	protected boolean reverseY = false;

	/**
	 * Is the volume data reversed along the z-axis?
	 */
	protected boolean reverseZ = false;

	/**
	 * Contains the positions of the top slice of the volume data, from the
	 * south-west corner to the north-east corner. Longitude (x values) should
	 * increment first.
	 */
	protected List<Position> positions;

	/**
	 * Float array that contains the volume data.
	 */
	protected FloatBuffer data;

	/**
	 * The minimum volume data value.
	 */
	protected float minValue;

	/**
	 * The maximum volume data value.
	 */
	protected float maxValue;

	/**
	 * Whether the volume data is cell-centred
	 */
	protected boolean cellCentred;

	protected FloatBuffer getData()
	{
		return data;
	}

	@Override
	public int getXSize()
	{
		return xSize;
	}

	@Override
	public int getYSize()
	{
		return ySize;
	}

	@Override
	public int getZSize()
	{
		return zSize;
	}

	@Override
	public double getDepth()
	{
		return depth;
	}

	@Override
	public double getTop()
	{
		return top;
	}

	@Override
	public float getValue(int x, int y, int z)
	{
		if (reverseX)
		{
			x = xSize - x - 1;
		}
		if (reverseY)
		{
			y = ySize - y - 1;
		}
		if (reverseZ)
		{
			z = zSize - z - 1;
		}

		if (!cellCentred)
		{
			return data.get(x + y * xSize + z * xSize * ySize);
		}
		else
		{
			// Clamp cell-centred data to vertex coordinates
			int clampedX = Math.min(x, xSize - 2);
			int clampedY = Math.min(y, ySize - 2);
			int clampedZ = Math.min(z, zSize - 2);
			int index = clampedX + clampedY * (xSize - 1) + clampedZ * (xSize - 1) * (ySize - 1);
			return data.get(index);
		}
	}

	@Override
	public boolean isCellCentred()
	{
		return cellCentred;
	}

	@Override
	public float getMinValue()
	{
		return minValue;
	}

	@Override
	public float getMaxValue()
	{
		return maxValue;
	}

	@Override
	public Position getPosition(int x, int y)
	{
		return positions.get(x + y * xSize);
	}

	@Override
	public float getNoDataValue()
	{
		return noDataValue;
	}

	@Override
	public Bounds getBounds()
	{
		return bounds;
	}

	@Override
	public boolean isFollowTerrain()
	{
		return false;
	}

	@Override
	public FastShape createHorizontalSurface(float maxVariance, Rectangle rectangle)
	{
		BinaryTriangleTree btt = new BinaryTriangleTree(positions, xSize, ySize);
		btt.setForceGLTriangles(true);
		btt.setGenerateTextureCoordinates(true);
		return btt.buildMeshFromCenter(maxVariance, rectangle);
	}

	@Override
	public TopBottomFastShape createLongitudeCurtain(int x)
	{
		List<Position> positions = new ArrayList<Position>();
		float[] textureCoordinateBuffer = new float[ySize * 4];
		int i = 0;
		for (int y = 0; y < ySize; y++)
		{
			Position position = getPosition(x, y);
			TopBottomPosition top =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, false);
			TopBottomPosition bottom =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, true);
			positions.add(top);
			positions.add(bottom);
			float u = y / (float) Math.max(1, ySize - 1);
			textureCoordinateBuffer[i++] = u;
			textureCoordinateBuffer[i++] = 0;
			textureCoordinateBuffer[i++] = u;
			textureCoordinateBuffer[i++] = 1;
		}
		TopBottomFastShape shape = new TopBottomFastShape(positions, GL2.GL_TRIANGLE_STRIP);
		shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		return shape;
	}

	@Override
	public TopBottomFastShape createLatitudeCurtain(int y)
	{
		List<Position> positions = new ArrayList<Position>();
		float[] textureCoordinateBuffer = new float[xSize * 4];
		int i = 0;
		for (int x = 0; x < xSize; x++)
		{
			Position position = getPosition(x, y);
			TopBottomPosition top =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, false);
			TopBottomPosition bottom =
					new TopBottomPosition(position.latitude, position.longitude, position.elevation, true);
			positions.add(top);
			positions.add(bottom);
			float u = x / (float) Math.max(1, xSize - 1);
			textureCoordinateBuffer[i++] = u;
			textureCoordinateBuffer[i++] = 0;
			textureCoordinateBuffer[i++] = u;
			textureCoordinateBuffer[i++] = 1;
		}
		TopBottomFastShape shape = new TopBottomFastShape(positions, GL2.GL_TRIANGLE_STRIP);
		shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		return shape;
	}

	@Override
	public FastShape createBoundingBox()
	{
		List<Position> positions = new ArrayList<Position>();
		for (int x = 0; x < getXSize(); x++)
		{
			Position position = getPosition(x, 0);
			positions.add(new TopBottomPosition(position.latitude, position.longitude, position.elevation, false));
		}
		for (int y = 1; y < getYSize() - 1; y++)
		{
			Position position = getPosition(getXSize() - 1, y);
			positions.add(new TopBottomPosition(position.latitude, position.longitude, position.elevation, false));
		}
		for (int x = getXSize() - 1; x >= 0; x--)
		{
			Position position = getPosition(x, getYSize() - 1);
			positions.add(new TopBottomPosition(position.latitude, position.longitude, position.elevation, false));
		}
		for (int y = getYSize() - 2; y >= 1; y--)
		{
			Position position = getPosition(0, y);
			positions.add(new TopBottomPosition(position.latitude, position.longitude, position.elevation, false));
		}

		int size = positions.size();
		for (int i = 0; i < size; i++)
		{
			Position position = positions.get(i);
			positions.add(new TopBottomPosition(position.latitude, position.longitude, position.elevation, true));
		}

		int squareIndexCount = ((getXSize() - 1) + (getYSize() - 1)) * 2;
		int[] indices = new int[2 * (squareIndexCount * 2 + 4)];

		int k = 0;
		for (int i = 0; i < squareIndexCount; i++)
		{
			int j = (i + 1) % squareIndexCount;
			indices[k++] = i;
			indices[k++] = j;
			indices[k++] = i + squareIndexCount;
			indices[k++] = j + squareIndexCount;
		}

		int j = 0;
		for (int i = 0; i < 4; i++)
		{
			indices[k++] = j;
			indices[k++] = j + squareIndexCount;
			j += (i % 2 == 0 ? getXSize() : getYSize()) - 1;
		}

		TopBottomFastShape shape = new TopBottomFastShape(positions, indices, GL2.GL_LINES);
		shape.setBottomElevationOffset(-getDepth());
		shape.setLineWidth(2.0);
		return shape;
	}

	@Override
	public boolean isSingleSliceVolume()
	{
		return xSize <= 1 || ySize <= 1 || zSize <= 1;
	}
}
