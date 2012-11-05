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
package au.gov.ga.earthsci.worldwind.common.layers.volume.btt;

import gov.nasa.worldwind.geom.Position;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A mesh generation helper which uses a grid of positions to generate a mesh.
 * Uses the Binary Triangle Tree mesh simplification algorithm.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BinaryTriangleTree
{
	private final List<Position> positions;
	private final int width;
	private final int height;
	private boolean generateTextureCoordinates = false;
	private boolean forceGLTriangles = false;

	/**
	 * Create a new {@link BinaryTriangleTree} object.
	 * 
	 * @param positions
	 *            Grid of positions in the mesh. Should be ordered in the
	 *            x-axis, then the y-axis.
	 * @param width
	 *            Number of positions in the x-axis
	 * @param height
	 *            Number of positions in the y-axis
	 */
	public BinaryTriangleTree(List<Position> positions, int width, int height)
	{
		Validate.isTrue(positions.size() == width * height, "Positions list count doesn't match provided width/height");

		this.positions = positions;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return Should texture coordinates be generated for the mesh?
	 */
	public boolean isGenerateTextureCoordinates()
	{
		return generateTextureCoordinates;
	}

	/**
	 * Enable/disable texture coordinate generation during mesh creation.
	 * 
	 * @param generateTextureCoordinates
	 */
	public void setGenerateTextureCoordinates(boolean generateTextureCoordinates)
	{
		this.generateTextureCoordinates = generateTextureCoordinates;
	}

	/**
	 * @return Should the mesh be forced to use the {@link GL#GL_TRIANGLES}
	 *         mode? Otherwise a {@link GL#GL_TRIANGLE_STRIP} mesh could
	 *         possibly be generated (if maximum variance is 0).
	 */
	public boolean isForceGLTriangles()
	{
		return forceGLTriangles;
	}

	/**
	 * Force the mesh generated (if max variance is 0) to use
	 * {@link GL#GL_TRIANGLES} instead of {@link GL#GL_TRIANGLE_STRIP}.
	 * 
	 * @param forceGLTriangles
	 */
	public void setForceGLTriangles(boolean forceGLTriangles)
	{
		this.forceGLTriangles = forceGLTriangles;
	}

	/**
	 * Build a mesh from the position grid.
	 * 
	 * @param maxVariance
	 *            Variances between triangle vertices less than maxVariance will
	 *            be simplified. Use 0 to use every vertex in the final mesh.
	 * @return A {@link FastShape} containing the mesh.
	 */
	public FastShape buildMesh(float maxVariance)
	{
		return buildMesh(maxVariance, new Rectangle(0, 0, width, height));
	}

	/**
	 * Build a mesh from the position grid. The simplification algorithm will
	 * start from the center instead of the top-left.
	 * 
	 * @param maxVariance
	 *            Variances between triangle vertices less than maxVariance will
	 *            be simplified. Use 0 to use every vertex in the final mesh.
	 * @return A {@link FastShape} containing the mesh.
	 */
	public FastShape buildMeshFromCenter(float maxVariance)
	{
		return buildMeshFromCenter(maxVariance, new Rectangle(0, 0, width, height));
	}

	/**
	 * Build a mesh from the position grid using positions within the given
	 * rectangle.
	 * 
	 * @param maxVariance
	 *            Variances between triangle vertices less than maxVariance will
	 *            be simplified. Use 0 to use every vertex in the final mesh.
	 * @param rectangle
	 *            Sub-rectangle of positions to use in the mesh.
	 * @return A {@link FastShape} containing the mesh.
	 */
	public FastShape buildMesh(float maxVariance, Rectangle rectangle)
	{
		if (maxVariance <= 0)
		{
			return buildFullMesh(rectangle);
		}

		List<BTTTriangle> triangles = new ArrayList<BTTTriangle>();
		buildMesh(maxVariance, rectangle.x, rectangle.y, rectangle.width, rectangle.height, false, false, triangles);
		return buildFastShape(triangles);
	}

	/**
	 * Build a mesh from the position grid using positions within the given
	 * rectangle. The simplification algorithm will start from the center
	 * instead of the top-left.
	 * 
	 * @param maxVariance
	 *            Variances between triangle vertices less than maxVariance will
	 *            be simplified. Use 0 to use every vertex in the final mesh.
	 * @param rectangle
	 *            Sub-rectangle of positions to use in the mesh.
	 * @return A {@link FastShape} containing the mesh.
	 */
	public FastShape buildMeshFromCenter(float maxVariance, Rectangle rectangle)
	{
		if (maxVariance <= 0)
		{
			return buildFullMesh(rectangle);
		}

		List<BTTTriangle> triangles = new ArrayList<BTTTriangle>();

		int centerWidth = Util.nextLowestPowerOf2Plus1(rectangle.width);
		int centerHeight = Util.nextLowestPowerOf2Plus1(rectangle.height);
		int centerXOffset = (rectangle.width - centerWidth) / 2;
		int centerYOffset = (rectangle.height - centerHeight) / 2;
		int remainingWidth = rectangle.width - centerWidth - centerXOffset;
		int remainingHeight = rectangle.height - centerHeight - centerYOffset;

		buildMesh(maxVariance, rectangle.x + centerXOffset, rectangle.y + centerYOffset, centerWidth, centerHeight,
				false, false, triangles);
		buildMesh(maxVariance, rectangle.x, rectangle.y, centerWidth + centerXOffset, centerYOffset + 1, true, true,
				triangles);
		buildMesh(maxVariance, rectangle.x, rectangle.y + centerYOffset, centerXOffset + 1, rectangle.height
				- centerYOffset, true, false, triangles);
		buildMesh(maxVariance, rectangle.x + centerWidth + centerXOffset - 1, rectangle.y, remainingWidth + 1,
				centerHeight + centerYOffset, false, true, triangles);
		buildMesh(maxVariance, rectangle.x + centerXOffset, rectangle.y + centerHeight + centerYOffset - 1,
				rectangle.width - centerXOffset, remainingHeight + 1, false, false, triangles);

		return buildFastShape(triangles);
	}

	/**
	 * Build a mesh from the position grid using positions within the given
	 * rectangle, using no simplification.
	 * 
	 * @param rect
	 *            Sub-rectangle of positions to use in the mesh.
	 * @return A {@link FastShape} containing the mesh.
	 */
	public FastShape buildFullMesh(Rectangle rect)
	{
		List<Position> positions;
		if (rect.x == 0 && rect.y == 0 && rect.width == width && rect.height == height)
		{
			//if using the entire area, then use the original positions list
			positions = this.positions;
		}
		else
		{
			//otherwise, create a new sub-list
			positions = new ArrayList<Position>(rect.width * rect.height);
			for (int y = rect.y; y < rect.height + rect.y; y++)
			{
				for (int x = rect.x; x < rect.width + rect.x; x++)
				{
					positions.add(this.positions.get(x + y * width));
				}
			}
		}

		int[] indices;
		int i = 0;
		if (forceGLTriangles)
		{
			int indexCount = 6 * (rect.width - 1) * (rect.height - 1);
			indices = new int[indexCount];

			int k = 0;
			for (int y = 0; y < rect.height - 1; y++, k++)
			{
				for (int x = 0; x < rect.width - 1; x++, k++)
				{
					indices[i++] = k;
					indices[i++] = k + 1;
					indices[i++] = k + rect.width;
					indices[i++] = k + 1;
					indices[i++] = k + rect.width + 1;
					indices[i++] = k + rect.width;
				}
			}
		}
		else
		{
			int indexCount = 2 * rect.width * (rect.height - 1);
			indices = new int[indexCount];

			int k = 0;
			for (int y = 0; y < rect.height - 1; y++)
			{
				if (y % 2 == 0) //even
				{
					for (int x = 0; x < rect.width; x++, k++)
					{
						indices[i++] = k;
						indices[i++] = k + rect.width;
					}
				}
				else
				{
					k += rect.width - 1;
					for (int x = 0; x < rect.width; x++, k--)
					{
						indices[i++] = k + rect.width;
						indices[i++] = k;
					}
					k += rect.width + 1;
				}
			}
		}

		FastShape shape = new FastShape(positions, indices, forceGLTriangles ? GL2.GL_TRIANGLES : GL2.GL_TRIANGLE_STRIP);

		if (generateTextureCoordinates)
		{
			i = 0;
			float[] textureCoordinateBuffer = new float[positions.size() * 2];
			for (int y = 0; y < rect.height; y++)
			{
				for (int x = 0; x < rect.width; x++)
				{
					textureCoordinateBuffer[i++] = (x + rect.x) / (float) (width - 1);
					textureCoordinateBuffer[i++] = (y + rect.y) / (float) (height - 1);
				}
			}
			shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		}

		return shape;
	}

	/**
	 * Build a mesh within the given rectangle, adding triangles to the triangle
	 * list. Because the BTT algorithm only supports power-of-2-plus-1 squares,
	 * this function divides the rectangle area into squares and then builds the
	 * tree from the sub-squares.
	 * 
	 * @param maxVariance
	 *            BTT algorithm variance
	 * @param x
	 *            Rectangle x coordinate
	 * @param y
	 *            Rectangle y coordinate
	 * @param width
	 *            Rectangle width
	 * @param height
	 *            Rectangle height
	 * @param reverseX
	 *            Begin the mesh building from the right instead of left?
	 * @param reverseY
	 *            Begin the mesh building from the bottom instead of top?
	 * @param triangles
	 *            Triangle list to add generated triangles to
	 */
	protected void buildMesh(float maxVariance, int x, int y, int width, int height, boolean reverseX,
			boolean reverseY, List<BTTTriangle> triangles)
	{
		//cannot build a mesh between less that 2 rows/columns
		if (width < 2 || height < 2)
			return;

		int yStart = y;
		int remainingHeight = height;
		while (remainingHeight > 1)
		{
			int xStart = x;
			int remainingWidth = width;
			int currentHeight = Util.nextLowestPowerOf2Plus1(Math.min(remainingWidth, remainingHeight));
			while (remainingWidth > 1)
			{
				int currentWidth = Util.nextLowestPowerOf2Plus1(Math.min(remainingWidth, remainingHeight));
				for (int yOffset = 0; yOffset < currentHeight - 1; yOffset += currentWidth - 1)
				{
					int tx = reverseX ? width - xStart - currentWidth + x * 2 : xStart;
					int ty = reverseY ? height - yStart - yOffset - currentWidth + y * 2 : yStart + yOffset;
					buildTree(maxVariance, tx, ty, currentWidth, triangles);
				}
				remainingWidth -= currentWidth - 1;
				xStart += currentWidth - 1;
			}
			remainingHeight -= currentHeight - 1;
			yStart += currentHeight - 1;
		}
	}

	/**
	 * Build a BinaryTriangleTree starting at the x,y coordinate.
	 * 
	 * @param maxVariance
	 *            BTT algorithm variance
	 * @param x
	 *            x coordinate from which to start
	 * @param y
	 *            y coordinate from which to start
	 * @param size
	 *            Size of the square (must be a power of 2 plus 1)
	 * @param triangles
	 *            Triangle list to add generated triangles to
	 */
	protected void buildTree(float maxVariance, int x, int y, int size, List<BTTTriangle> triangles)
	{
		/*
		 *  left
		 *     +---+
		 *     |\  |
		 *     | \ |
		 *     |  \|
		 *     +---+
		 *  apex   right
		 */

		int apex1 = x + y * width, left1 = x + (y + size - 1) * width, right1 = (x + size - 1) + y * width;
		BTTTriangle t1 = new BTTTriangle(apex1, left1, right1);

		int apex2 = (x + size - 1) + (y + size - 1) * width, left2 = (x + size - 1) + y * width, right2 =
				x + (y + size - 1) * width;
		BTTTriangle t2 = new BTTTriangle(apex2, left2, right2);

		t1.bottomNeighbour = t2;
		t2.bottomNeighbour = t1;

		buildFace(maxVariance, t1, x, y, size);
		buildFace(maxVariance, t2, x, y, size);

		addLeavesToTriangleList(t1, triangles);
		addLeavesToTriangleList(t2, triangles);
	}

	/**
	 * Recursively sub-divide triangles within t if the triangle variance is
	 * greater than the maxVariance.
	 * 
	 * @param maxVariance
	 *            BTT algorithm variance
	 * @param t
	 *            Triangle to sub-divide
	 * @param x
	 *            x coordinate from which the area started
	 * @param y
	 *            y coordinate from which the area started
	 * @param size
	 *            Size of the square (must be a power of 2 plus 1)
	 */
	protected void buildFace(float maxVariance, BTTTriangle t, int x, int y, int size)
	{
		if (t.leftChild != null)
		{
			buildFace(maxVariance, t.leftChild, x, y, size);
			buildFace(maxVariance, t.rightChild, x, y, size);
		}
		else
		{
			boolean atLowestLevel =
					Math.abs(t.apexIndex - t.leftIndex) == 1 || Math.abs(t.apexIndex - t.rightIndex) == 1;
			if (!atLowestLevel)
			{
				if (isAnyIndexOnEdge(t.apexIndex, t.leftIndex, t.rightIndex, x, y, size)
						|| calculateVariance(t.apexIndex, t.leftIndex, t.rightIndex) >= maxVariance)
				{
					trySplitFace(t);
					buildFace(maxVariance, t.leftChild, x, y, size);
					buildFace(maxVariance, t.rightChild, x, y, size);
				}
			}
		}
	}

	/**
	 * Test if any of the provided indices are on the edge of the BTT
	 * calculation square.
	 * 
	 * @param apexIndex
	 *            Index of the triangle apex position
	 * @param leftIndex
	 *            Index of the triangle left position
	 * @param rightIndex
	 *            Index of the triangle right position
	 * @param x
	 *            x coordinate from which the area started
	 * @param y
	 *            y coordinate from which the area started
	 * @param size
	 *            Size of the square (must be a power of 2 plus 1)
	 * @return True if the provided indices are on the edge of the area
	 */
	protected boolean isAnyIndexOnEdge(int apexIndex, int leftIndex, int rightIndex, int x, int y, int size)
	{
		return isIndexOnEdge(apexIndex, x, y, size) || isIndexOnEdge(leftIndex, x, y, size)
				|| isIndexOnEdge(rightIndex, x, y, size);
	}

	/**
	 * Test if the provided index is on the edge of the BTT calculation square.
	 * 
	 * @param index
	 *            Position index
	 * @param x
	 *            x coordinate from which the area started
	 * @param y
	 *            y coordinate from which the area started
	 * @param size
	 *            Size of the square (must be a power of 2 plus 1)
	 * @return True if the provided index is on the edge of the area
	 */
	protected boolean isIndexOnEdge(int index, int x, int y, int size)
	{
		int ix = index % width;
		int iy = index / width;
		return ix == x || iy == y || ix == x + size - 1 || iy == y + size - 1;
	}

	/**
	 * Try splitting the given triangle. If the triangle's bottom neighbour
	 * isn't split, it also gets split to ensure there's no gaps in the mesh.
	 * 
	 * @param t
	 *            Triangle to split.
	 */
	protected void trySplitFace(BTTTriangle t)
	{
		if (t.bottomNeighbour != null)
		{
			if (t.bottomNeighbour.bottomNeighbour != t)
			{
				trySplitFace(t.bottomNeighbour);
			}
			splitFace(t);
			splitFace(t.bottomNeighbour);
			t.leftChild.rightNeighbour = t.bottomNeighbour.rightChild;
			t.rightChild.leftNeighbour = t.bottomNeighbour.leftChild;
			t.bottomNeighbour.leftChild.rightNeighbour = t.rightChild;
			t.bottomNeighbour.rightChild.leftNeighbour = t.leftChild;
		}
		else
		{
			splitFace(t);
		}
	}

	/**
	 * Actually split the given triangle.
	 * 
	 * @param t
	 *            Triangle to split.
	 */
	protected void splitFace(BTTTriangle t)
	{
		int midpointIndex = hypotenuseMidpointIndex(t.leftIndex, t.rightIndex);
		t.rightChild = new BTTTriangle(midpointIndex, t.rightIndex, t.apexIndex);
		t.leftChild = new BTTTriangle(midpointIndex, t.apexIndex, t.leftIndex);
		t.leftChild.leftNeighbour = t.rightChild;
		t.rightChild.rightNeighbour = t.leftChild;

		t.leftChild.bottomNeighbour = t.leftNeighbour;
		if (t.leftNeighbour != null)
		{
			if (t.leftNeighbour.bottomNeighbour == t)
			{
				t.leftNeighbour.bottomNeighbour = t.leftChild;
			}
			else if (t.leftNeighbour.leftNeighbour == t)
			{
				t.leftNeighbour.leftNeighbour = t.leftChild;
			}
			else
			{
				t.leftNeighbour.rightNeighbour = t.leftChild;
			}
		}

		t.rightChild.bottomNeighbour = t.rightNeighbour;
		if (t.rightNeighbour != null)
		{
			if (t.rightNeighbour.bottomNeighbour == t)
			{
				t.rightNeighbour.bottomNeighbour = t.rightChild;
			}
			else if (t.rightNeighbour.rightNeighbour == t)
			{
				t.rightNeighbour.rightNeighbour = t.rightChild;
			}
			else
			{
				t.rightNeighbour.leftNeighbour = t.rightChild;
			}
		}
	}

	/**
	 * Calculate the variance of the provided triangle.
	 * 
	 * @param apexIndex
	 *            Index of the triangle apex position.
	 * @param leftIndex
	 *            Index of the triangle left position.
	 * @param rightIndex
	 *            Index of the triangle right position.
	 * @return Variance of the triangle.
	 */
	protected float calculateVariance(int apexIndex, int leftIndex, int rightIndex)
	{
		if (Math.abs(apexIndex - leftIndex) == 1 || Math.abs(apexIndex - rightIndex) == 1)
			return 0;

		int midpointIndex = hypotenuseMidpointIndex(leftIndex, rightIndex);
		double midpointElevation = positions.get(midpointIndex).elevation;
		double interpolatedElevation = (positions.get(leftIndex).elevation + positions.get(rightIndex).elevation) / 2;
		float delta = (float) Math.abs(midpointElevation - interpolatedElevation);
		delta = Math.max(delta, calculateVariance(midpointIndex, rightIndex, apexIndex));
		delta = Math.max(delta, calculateVariance(midpointIndex, apexIndex, leftIndex));
		return delta;
	}

	/**
	 * Index of the midpoint position between the two provided indices.
	 * 
	 * @param leftIndex
	 *            Left position index.
	 * @param rightIndex
	 *            Right position index.
	 * @return Index of the midpoint between leftIndex and rightIndex.
	 */
	protected int hypotenuseMidpointIndex(int leftIndex, int rightIndex)
	{
		int leftX = leftIndex % width;
		int leftY = leftIndex / width;
		int rightX = rightIndex % width;
		int rightY = rightIndex / width;
		return (leftX + rightX) / 2 + ((leftY + rightY) / 2) * width;
	}

	/**
	 * Recursively add all the leaves of the binary triangle tree to the
	 * provided triangle list, beginning at the provided triangle.
	 * 
	 * @param t
	 *            Parent triangle to add leaves from
	 * @param triangles
	 *            Triangle list to add leaves to
	 */
	protected void addLeavesToTriangleList(BTTTriangle t, List<BTTTriangle> triangles)
	{
		if (t.leftChild == null || t.rightChild == null)
		{
			triangles.add(t);
		}
		else
		{
			//recurse through children
			addLeavesToTriangleList(t.leftChild, triangles);
			addLeavesToTriangleList(t.rightChild, triangles);
		}
	}

	/**
	 * Build a {@link FastShape} object from the list of binary triangle tree
	 * leaves.
	 * 
	 * @param triangles
	 *            Triangle list.
	 * @return FastShape containing triangles from the provided triangle list.
	 */
	protected FastShape buildFastShape(List<BTTTriangle> triangles)
	{
		List<Position> positions = new ArrayList<Position>();
		List<Integer> originalIndices = new ArrayList<Integer>();
		Map<Position, Integer> positionIndexMap = new HashMap<Position, Integer>();
		int[] indices = new int[triangles.size() * 3];
		int i = 0;

		for (BTTTriangle triangle : triangles)
		{
			Position apexPosition = this.positions.get(triangle.apexIndex);
			Position leftPosition = this.positions.get(triangle.leftIndex);
			Position rightPosition = this.positions.get(triangle.rightIndex);
			Integer apexIndex = positionIndexMap.get(apexPosition);
			Integer leftIndex = positionIndexMap.get(leftPosition);
			Integer rightIndex = positionIndexMap.get(rightPosition);
			if (apexIndex == null)
			{
				apexIndex = positions.size();
				positionIndexMap.put(apexPosition, apexIndex);
				positions.add(apexPosition);
				originalIndices.add(triangle.apexIndex);
			}
			if (leftIndex == null)
			{
				leftIndex = positions.size();
				positionIndexMap.put(leftPosition, leftIndex);
				positions.add(leftPosition);
				originalIndices.add(triangle.leftIndex);
			}
			if (rightIndex == null)
			{
				rightIndex = positions.size();
				positionIndexMap.put(rightPosition, rightIndex);
				positions.add(rightPosition);
				originalIndices.add(triangle.rightIndex);
			}
			indices[i++] = leftIndex;
			indices[i++] = apexIndex;
			indices[i++] = rightIndex;
		}

		FastShape shape = new FastShape(positions, indices, GL2.GL_TRIANGLES);

		if (generateTextureCoordinates)
		{
			float[] textureCoordinateBuffer = new float[positions.size() * 2];
			i = 0;
			for (Integer index : originalIndices)
			{
				int x = index % width;
				int y = index / width;
				textureCoordinateBuffer[i++] = x / (float) (width - 1);
				textureCoordinateBuffer[i++] = y / (float) (height - 1);
			}
			shape.setTextureCoordinateBuffer(textureCoordinateBuffer);
		}

		return shape;
	}

	/**
	 * Helper class that stores binary triangle tree information.
	 */
	protected class BTTTriangle
	{
		public final int apexIndex;
		public final int leftIndex;
		public final int rightIndex;
		public BTTTriangle leftChild;
		public BTTTriangle rightChild;
		public BTTTriangle leftNeighbour;
		public BTTTriangle rightNeighbour;
		public BTTTriangle bottomNeighbour;

		public BTTTriangle(int apexIndex, int leftIndex, int rightIndex)
		{
			this.apexIndex = apexIndex;
			this.leftIndex = leftIndex;
			this.rightIndex = rightIndex;
		}

		@Override
		public String toString()
		{
			return "Left: " + (leftIndex % BinaryTriangleTree.this.width) + ","
					+ (leftIndex / BinaryTriangleTree.this.width) + ", Apex: "
					+ (apexIndex % BinaryTriangleTree.this.width) + "," + (apexIndex / BinaryTriangleTree.this.width)
					+ ", Right: " + (rightIndex % BinaryTriangleTree.this.width) + ","
					+ (rightIndex / BinaryTriangleTree.this.width);
		}
	}
}
