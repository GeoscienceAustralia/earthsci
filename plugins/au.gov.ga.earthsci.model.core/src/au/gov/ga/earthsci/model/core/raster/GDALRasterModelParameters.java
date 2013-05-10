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
package au.gov.ga.earthsci.model.core.raster;

import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.common.util.FileUtil;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * A simple DTO that contains parameters used for creating {@link IModel}
 * instances from GDAL rasters.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelParameters
{

	/** The raster band to use for elevation values */
	private int elevationBandIndex = 1;

	/** The source coordinate system / projection */
	private String sourceProjection;

	/** The offset to apply to the 'elevation' values */
	private Double offset;

	/** The scale factor to apply to the 'elevation' values */
	private Double scale;

	/** A (localised) name to attach to the model on creation */
	private String modelName;

	/** A (localised) description to attach to the model on creation */
	private String modelDescription;

	/** An (optional) subsample rate to apply to raster data on creation */
	// TODO: Support better subsampling methods (downscaling and filtering etc.)
	private Integer subsample;

	/**
	 * Create a new parameters object, populated with any sensible defaults
	 * obtainable from the provided dataset
	 * 
	 * @param ds
	 *            The GDAL dataset to obtain defaults from
	 */
	public GDALRasterModelParameters(Dataset ds)
	{
		if (ds == null)
		{
			return;
		}

		String datasetProjection = ds.GetProjection();
		if (!Util.isBlank(datasetProjection))
		{
			sourceProjection = datasetProjection;
		}

		modelName = FileUtil.isLikelyFilePath(ds.GetDescription()) ?
				FileUtil.getFileName(ds.GetDescription()) :
				ds.GetDescription();
		modelDescription = ds.GetDescription();
	}

	/**
	 * Create a new, empty parameters object.
	 */
	public GDALRasterModelParameters()
	{
		this(null);
	}

	public int getElevationBandIndex()
	{
		return elevationBandIndex;
	}

	public void setElevationBandIndex(int elevationBandIndex)
	{
		this.elevationBandIndex = elevationBandIndex;
	}

	public String getSourceProjection()
	{
		return sourceProjection;
	}

	public void setSourceProjection(String sourceProjection)
	{
		this.sourceProjection = sourceProjection;
	}

	public Double getScaleFactor()
	{
		return scale;
	}

	public void setScaleFactor(Double scale)
	{
		this.scale = scale;
	}

	public Double getOffset()
	{
		return this.offset;
	}

	public void setOffset(Double offset)
	{
		this.offset = offset;
	}

	public String getModelName()
	{
		return modelName;
	}

	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}

	public String getModelDescription()
	{
		return modelDescription;
	}

	public void setModelDescription(String modelDescription)
	{
		this.modelDescription = modelDescription;
	}

	public void setSubsample(Integer subsample)
	{
		this.subsample = subsample;
	}

	public Integer getSubsample()
	{
		return subsample;
	}

	/**
	 * @return A normalised subsample value to use that will never return a null
	 *         or non-positive number.
	 */
	public int getNormalisedSubsample()
	{
		return subsample == null ? 1 : Math.max(1, subsample);
	}
}
