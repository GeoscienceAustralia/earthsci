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
package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;

import java.awt.Color;
import java.nio.ByteOrder;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;
import au.gov.ga.earthsci.worldwind.common.util.CoordinateTransformationUtil;

/**
 * Provides the ability to configure the {@link GocadReader}. An instance of
 * this class is provided to the {@link GocadFactory} when reading GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadReaderParameters
{
	private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	private int subsamplingU = 1;
	private int subsamplingV = 1;
	private int subsamplingW = 1;
	private boolean dynamicSubsampling = false;
	private int dynamicSubsamplingSamplesPerAxis = 50;
	private boolean bilinearMinification = false;
	private CoordinateTransformation coordinateTransformation = null;
	private Color color = null; // To use it no colormap found
	private ColorMap colorMap = null;
	private float maxVariance = 0;
	private String paintedVariable;

	private Double pointSize;
	private Double pointMinSize;
	private Double pointMaxSize;
	private Double pointConstantAttenuation;
	private Double pointLinearAttenuation;
	private Double pointQuadraticAttenuation;
	
	public GocadReaderParameters()
	{
		//use defaults
	}

	/**
	 * Copy constructor
	 */
	public GocadReaderParameters(GocadReaderParameters other)
	{
		this.byteOrder = other.byteOrder;
		this.subsamplingU = other.subsamplingU;
		this.subsamplingV = other.subsamplingV;
		this.subsamplingW = other.subsamplingW;
		this.dynamicSubsampling = other.dynamicSubsampling;
		this.dynamicSubsamplingSamplesPerAxis = other.dynamicSubsamplingSamplesPerAxis;
		this.bilinearMinification = other.bilinearMinification;
		this.coordinateTransformation = other.coordinateTransformation;
		this.color = other.color;
		this.colorMap = other.colorMap;
		this.maxVariance = other.maxVariance;
		this.paintedVariable = other.paintedVariable;
		this.pointSize = other.pointSize;
		this.pointMinSize = other.pointMinSize;
		this.pointMaxSize = other.pointMaxSize;
		this.pointConstantAttenuation = other.pointConstantAttenuation;
		this.pointLinearAttenuation = other.pointLinearAttenuation;
		this.pointQuadraticAttenuation = other.pointQuadraticAttenuation;
	}
	
	/**
	 * Construct a new instance of this class, using the params to setup any
	 * default values.
	 * 
	 * @param params
	 *            Default parameters
	 */
	public GocadReaderParameters(AVList params)
	{
		ByteOrder bo = (ByteOrder) params.getValue(AVKey.BYTE_ORDER);
		if (bo != null)
		{
			setByteOrder(bo);
		}

		Integer i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_U);
		if (i != null)
		{
			setSubsamplingU(i);
		}

		i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_V);
		if (i != null)
		{
			setSubsamplingV(i);
		}

		i = (Integer) params.getValue(AVKeyMore.SUBSAMPLING_W);
		if (i != null)
		{
			setSubsamplingW(i);
		}

		Boolean b = (Boolean) params.getValue(AVKeyMore.DYNAMIC_SUBSAMPLING);
		if (b != null)
		{
			setDynamicSubsampling(b);
		}

		i = (Integer) params.getValue(AVKeyMore.DYNAMIC_SUBSAMPLING_SAMPLES_PER_AXIS);
		if (i != null)
		{
			setDynamicSubsamplingSamplesPerAxis(i);
		}

		b = (Boolean) params.getValue(AVKeyMore.BILINEAR_MINIFICATION);
		if (b != null)
		{
			setBilinearMinification(b);
		}

		String s = (String) params.getValue(AVKey.COORDINATE_SYSTEM);
		if (s != null)
		{
			setCoordinateTransformation(CoordinateTransformationUtil.getTransformationToWGS84(s));
		}

		ColorMap cm = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		if (cm != null)
		{
			setColorMap(cm);
		}

		Color c = (Color) params.getValue(AVKeyMore.COLOR);
		if (c != null)
		{
			setColor(c);
		}
		
		Double d = (Double) params.getValue(AVKeyMore.MAX_VARIANCE);
		if (d != null)
		{
			setMaxVariance(d.floatValue());
		}

		setPaintedVariable((String) params.getValue(AVKeyMore.PAINTED_VARIABLE));
	}

	/**
	 * @return The amount of subsampling to use in the u-axis when reading GOCAD
	 *         voxets. Defaults to 1 (no subsampling).
	 */
	public int getSubsamplingU()
	{
		return subsamplingU;
	}

	/**
	 * Sets the amount of subsampling to use in the u-axis when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetSubsamplingU
	 */
	public void setSubsamplingU(int voxetSubsamplingU)
	{
		this.subsamplingU = voxetSubsamplingU;
	}

	/**
	 * @return The amount of subsampling to use in the v-axis when reading GOCAD
	 *         voxets. Defaults to 1 (no subsampling).
	 */
	public int getSubsamplingV()
	{
		return subsamplingV;
	}

	/**
	 * Sets the amount of subsampling to use in the v-axis when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetSubsamplingV
	 */
	public void setSubsamplingV(int voxetSubsamplingV)
	{
		this.subsamplingV = voxetSubsamplingV;
	}

	/**
	 * @return The amount of subsampling to use in the w-axis when reading GOCAD
	 *         voxets. Defaults to 1 (no subsampling).
	 */
	public int getSubsamplingW()
	{
		return subsamplingW;
	}

	/**
	 * Sets the amount of subsampling to use in the w-axis when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetSubsamplingW
	 */
	public void setSubsamplingW(int voxetSubsamplingW)
	{
		this.subsamplingW = voxetSubsamplingW;
	}

	/**
	 * @return Should the reader use dynamic subsampling when reading GOCAD
	 *         voxets? Dynamic subsampling attempts to subsample the voxet
	 *         automatically to ensure a certain resolution (number of samples)
	 *         in each axis. Defaults to true.
	 */
	public boolean isDynamicSubsampling()
	{
		return dynamicSubsampling;
	}

	/**
	 * Set whether the reader should use dynamic subsampling when reading GOCAD
	 * voxets.
	 * 
	 * @param voxetDynamicSubsampling
	 */
	public void setDynamicSubsampling(boolean voxetDynamicSubsampling)
	{
		this.dynamicSubsampling = voxetDynamicSubsampling;
	}

	/**
	 * @return The number of samples to attempt to subsample to per axis when
	 *         dynamic subsampling is enabled. Defaults to 50.
	 */
	public int getDynamicSubsamplingSamplesPerAxis()
	{
		return dynamicSubsamplingSamplesPerAxis;
	}

	/**
	 * Set the number of samples to subsample to per axis when using dynamic
	 * subsampling.
	 * 
	 * @param voxetDynamicSubsamplingSamplesPerAxis
	 */
	public void setDynamicSubsamplingSamplesPerAxis(int voxetDynamicSubsamplingSamplesPerAxis)
	{
		this.dynamicSubsamplingSamplesPerAxis = voxetDynamicSubsamplingSamplesPerAxis;
	}

	/**
	 * @return Whether the reader should use bilinear minification when
	 *         subsampling GOCAD voxets. If false, a nearest neighbour approach
	 *         is used. Defaults to true.
	 */
	public boolean isBilinearMinification()
	{
		return bilinearMinification;
	}

	/**
	 * Set whether the reader should use bilinear minification when subsampling
	 * GOCAD voxets.
	 * 
	 * @param voxetBilinearMinification
	 */
	public void setBilinearMinification(boolean voxetBilinearMinification)
	{
		this.bilinearMinification = voxetBilinearMinification;
	}

	/**
	 * @return {@link ByteOrder} to use when reading binary GOCAD data (eg from
	 *         voxets). Defaults to {@link ByteOrder#LITTLE_ENDIAN}.
	 */
	public ByteOrder getByteOrder()
	{
		return byteOrder;
	}

	/**
	 * Set the {@link ByteOrder} to use when reading binary GOCAD data.
	 * 
	 * @param byteOrder
	 */
	public void setByteOrder(ByteOrder byteOrder)
	{
		this.byteOrder = byteOrder;
	}

	/**
	 * @return Map reprojection to use when reading GOCAD vertices (null for no
	 *         reprojection).
	 */
	public CoordinateTransformation getCoordinateTransformation()
	{
		return coordinateTransformation;
	}

	/**
	 * Set the map reprojection to use when reading GOCAD vertices.
	 * 
	 * @param coordinateTransformation
	 */
	public void setCoordinateTransformation(CoordinateTransformation coordinateTransformation)
	{
		this.coordinateTransformation = coordinateTransformation;
	}

	/**
	 * @return Colour map to use when assigning colours to GOCAD vertices.
	 */
	public ColorMap getColorMap()
	{
		return colorMap;
	}

	/**
	 * Set the colour map to use when assigning colours to GOCAD vertices.
	 * 
	 * @param colorMap
	 */
	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}

	/**
	 * @return The maximum variance to use when generating a triangle mesh from
	 *         a gridded elevation dataset (ie a GSurf).
	 * @see BinaryTriangleTree
	 */
	public float getMaxVariance()
	{
		return maxVariance;
	}

	/**
	 * Set the maximum variance to use when generating a triangle mesh from a
	 * gridded elevatoin dataset. Defaults to 0 (no simplification).
	 * 
	 * @param maxVariance
	 */
	public void setMaxVariance(float maxVariance)
	{
		this.maxVariance = maxVariance;
	}

	/**
	 * @return Property used to calculate vertex colors. This will override the
	 *         '*painted*variable' parameter in the GOCAD file.
	 */
	public String getPaintedVariable()
	{
		return paintedVariable;
	}

	/**
	 * Set the property used to calculate vertex colors. This can be used to
	 * override the '*painted*variable' parameter in the GOCAD file.
	 * 
	 * @param paintedVariable
	 */
	public void setPaintedVariable(String paintedVariable)
	{
		this.paintedVariable = paintedVariable;
	}
	
	/**
	 * @return the color to use if no colour map is found
	 */
	public Color getColor()
	{
		return color;
	}
	
	/**
	 * @param color the color to set
	 */
	public void setColor(Color color)
	{
		this.color = color;
	}
	
	/**
	 * @return Whether colour information is available in these parameters
	 */
	public boolean isColorInformationAvailable()
	{
		return this.colorMap != null || this.color != null;
	}

	/**
	 * @return the pointSize
	 */
	public Double getPointSize()
	{
		return pointSize;
	}

	/**
	 * @param pointSize the pointSize to set
	 */
	public void setPointSize(Double pointSize)
	{
		this.pointSize = pointSize;
	}

	/**
	 * @return the pointMinSize
	 */
	public Double getPointMinSize()
	{
		return pointMinSize;
	}

	/**
	 * @param pointMinSize the pointMinSize to set
	 */
	public void setPointMinSize(Double pointMinSize)
	{
		this.pointMinSize = pointMinSize;
	}

	/**
	 * @return the pointMaxSize
	 */
	public Double getPointMaxSize()
	{
		return pointMaxSize;
	}

	/**
	 * @param pointMaxSize the pointMaxSize to set
	 */
	public void setPointMaxSize(Double pointMaxSize)
	{
		this.pointMaxSize = pointMaxSize;
	}

	/**
	 * @return the pointConstantAttenuation
	 */
	public Double getPointConstantAttenuation()
	{
		return pointConstantAttenuation;
	}

	/**
	 * @param pointConstantAttenuation the pointConstantAttenuation to set
	 */
	public void setPointConstantAttenuation(Double pointConstantAttenuation)
	{
		this.pointConstantAttenuation = pointConstantAttenuation;
	}

	/**
	 * @return the pointLinearAttenuation
	 */
	public Double getPointLinearAttenuation()
	{
		return pointLinearAttenuation;
	}

	/**
	 * @param pointLinearAttenuation the pointLinearAttenuation to set
	 */
	public void setPointLinearAttenuation(Double pointLinearAttenuation)
	{
		this.pointLinearAttenuation = pointLinearAttenuation;
	}

	/**
	 * @return the pointQuadraticAttenuation
	 */
	public Double getPointQuadraticAttenuation()
	{
		return pointQuadraticAttenuation;
	}

	/**
	 * @param pointQuadraticAttenuation the pointQuadraticAttenuation to set
	 */
	public void setPointQuadraticAttenuation(Double pointQuadraticAttenuation)
	{
		this.pointQuadraticAttenuation = pointQuadraticAttenuation;
	}
}
