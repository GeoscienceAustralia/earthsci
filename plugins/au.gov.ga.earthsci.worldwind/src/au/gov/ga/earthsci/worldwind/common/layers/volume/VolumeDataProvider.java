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

import au.gov.ga.earthsci.worldwind.common.layers.data.DataProvider;
import au.gov.ga.earthsci.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * {@link DataProvider} that provides data to a {@link VolumeLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface VolumeDataProvider extends DataProvider<VolumeLayer>
{
	/**
	 * @return The size of the volume's x-axis.
	 */
	int getXSize();

	/**
	 * @return The size of the volume's y-axis.
	 */
	int getYSize();

	/**
	 * @return The size of the volume's z-axis.
	 */
	int getZSize();

	/**
	 * @return The depth of the volume, in meters.
	 */
	double getDepth();

	/**
	 * @return The average elevation of the top slice.
	 */
	double getTop();

	/**
	 * Calculate elevation of the given slice as a percentage of the distance
	 * between the top and bottom elevation. For volumes that are linearly
	 * distributed along the z-axis, this is defined as
	 * <code>slice / (ZSize - 1)</code>.
	 * 
	 * @param slice
	 * @return Elevation of the given slice between the top and bottom of the
	 *         volume, in the range 0..1 (0 being the top, 1 being the bottom)
	 */
	double getSliceElevationPercent(double slice);

	/**
	 * Calculate the z-slice for the given elevation percentage that represents
	 * the elevation between the top and bottom volume slice (in range 0..1).
	 * For volumes that are linearly distributed along the z-axis, this is
	 * defined as <code>elevationPercent * (ZSize - 1)</code>.
	 * <p/>
	 * This is the inverse of {@link #getSliceElevationPercent(double)}.
	 * 
	 * @param elevationPercent
	 *            Elevation value in range 0..1
	 * @return Slice for given elevation percentage
	 * @see #getSliceElevationPercent(double)
	 */
	double getElevationPercentSlice(double elevationPercent);

	/**
	 * @return Number of subsamples to use for the z-axis when generating
	 *         curtain (lat/lon) volume textures (1 means no subsampling)
	 */
	int getZSubsamples();

	/**
	 * The value of the volume data at the given (x,y,z) point.
	 * <p/>
	 * Note that the meaning of (x,y,z) will depend on whether the volume is
	 * cell-centred or vertex-centred. If cell-centred, (x,y,z) will index a
	 * cell in the volume. If the volume is vertex-centred the point will index
	 * a vertex in the volume.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 * @param z
	 *            z-coordinate
	 * 
	 * @return The (x,y,z) value of the volume data.
	 * 
	 * @see #isCellCentred()
	 */
	float getValue(int x, int y, int z);

	/**
	 * Returns whether the data in the volume is cell-centred (i.e. data stored
	 * per-cell) or vertex-centred (i.e. data stored per-vertex).
	 * <p/>
	 * If the volume is cell-centred the data array will have dimenions
	 * <code>(XSize-1)*(YSize-1)*(ZSize-1)</code>
	 * 
	 * @return <code>true</code> if the volume data is cell-centred;
	 *         <code>false</code> otherwise.
	 */
	boolean isCellCentred();

	/**
	 * @return The minimum value in the volume data.
	 */
	float getMinValue();

	/**
	 * @return The maximum value in the volume data.
	 */
	float getMaxValue();

	/**
	 * The position of the top (z == 0) volume point at the given (x,y) point.
	 * 
	 * @param x
	 *            x-coordinate
	 * @param y
	 *            y-coordinate
	 * @return The position at the (x,y) point.
	 */
	Position getPosition(int x, int y);

	/**
	 * @return The value that identifies no-data.
	 */
	float getNoDataValue();

	/**
	 * Create a horizontal surface with elevation.
	 * 
	 * @param maxVariance
	 *            BTT variance (see {@link BinaryTriangleTree}).
	 * @param rectangle
	 *            Sub-rectangle with the x and y axes.
	 * @return A {@link FastShape} containing the horizontal surface mesh.
	 */
	FastShape createHorizontalSurface(float maxVariance, Rectangle rectangle);

	/**
	 * Create a curtain along a given x value. The top and bottom of the curtain
	 * follows the volume's elevation at that x.
	 * 
	 * @param x
	 *            x-coordinate of the curtain
	 * @return A {@link TopBottomFastShape} containing a triangle mesh curtain.
	 */
	TopBottomFastShape createXCurtain(int x);

	/**
	 * Create a curtain along a given y value. The top and bottom of the curtain
	 * follows the volume's elevation at that y.
	 * 
	 * @param y
	 *            y-coordinate of the curtain
	 * @return A {@link TopBottomFastShape} containing a triangle mesh curtain.
	 */
	TopBottomFastShape createYCurtain(int y);

	/**
	 * @return Shape that can be used to visualize the bounding box of the
	 *         volume.
	 */
	FastShape createBoundingBox();

	/**
	 * @return Whether this volume is the special case of a single slice in the
	 *         x- y- or z-direction.
	 */
	boolean isSingleSliceVolume();
}
