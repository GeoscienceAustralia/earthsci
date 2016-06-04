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

import java.util.LinkedHashMap;
import java.util.Map;

import org.gdal.gdal.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMaps;
import au.gov.ga.earthsci.common.color.io.CompactStringColorMapWriter;
import au.gov.ga.earthsci.common.util.FileUtil;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.core.parameters.IColorMapParameters;
import au.gov.ga.earthsci.model.core.parameters.IInformationParameters;
import au.gov.ga.earthsci.model.core.parameters.ISourceProjectionParameters;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * A simple DTO that contains parameters used for creating {@link IModel}
 * instances from GDAL rasters.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelParameters implements IColorMapParameters, ISourceProjectionParameters,
		IInformationParameters
{

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GDALRasterModelParameters.class);

	public static final String ELEVATION_BAND = "elevationBand"; //$NON-NLS-1$
	public static final String SOURCE_SRS = "sourceSRS"; //$NON-NLS-1$
	public static final String ELEVATION_OFFSET = "offset"; //$NON-NLS-1$
	public static final String ELEVATION_SCALE = "scale"; //$NON-NLS-1$
	public static final String MODEL_NAME = "name"; //$NON-NLS-1$
	public static final String MODEL_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String ELEVATION_SUBSAMPLE = "subsample"; //$NON-NLS-1$
	public static final String COLOR_MAP = "colormap"; //$NON-NLS-1$

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

	/** A colour map used to apply colouring to the loaded model */
	private ColorMap colorMap = ColorMaps.getRGBRainbowMap();

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
	}

	/**
	 * Create a new parameters object from the contents of the given parameters
	 * map.
	 * <p/>
	 * Expected keys are defined as constants in this class
	 * 
	 * @param params
	 *            The parameters to use to create this object with
	 */
	public GDALRasterModelParameters(Map<String, String> params)
	{
		if (params == null)
		{
			return;
		}

		if (params.containsKey(ELEVATION_BAND))
		{
			elevationBandIndex = Integer.parseInt(params.get(ELEVATION_BAND));
		}
		if (params.containsKey(MODEL_NAME))
		{
			modelName = params.get(MODEL_NAME);
		}
		if (params.containsKey(MODEL_DESCRIPTION))
		{
			modelDescription = params.get(MODEL_DESCRIPTION);
		}
		if (params.containsKey(ELEVATION_OFFSET))
		{
			offset = Double.parseDouble(params.get(ELEVATION_OFFSET));
		}
		if (params.containsKey(ELEVATION_SCALE))
		{
			scale = Double.parseDouble(params.get(ELEVATION_SCALE));
		}
		if (params.containsKey(ELEVATION_SUBSAMPLE))
		{
			subsample = Integer.parseInt(params.get(ELEVATION_SUBSAMPLE));
		}
		if (params.containsKey(SOURCE_SRS))
		{
			sourceProjection = params.get(SOURCE_SRS);
		}
		if (params.containsKey(COLOR_MAP))
		{
			colorMap = ColorMaps.readFrom(params.get(COLOR_MAP));
		}
	}

	public int getElevationBandIndex()
	{
		return elevationBandIndex;
	}

	public void setElevationBandIndex(int elevationBandIndex)
	{
		this.elevationBandIndex = elevationBandIndex;
	}

	@Override
	public String getSourceProjection()
	{
		return sourceProjection;
	}

	@Override
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

	@Override
	public String getModelName()
	{
		return modelName;
	}

	@Override
	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}

	@Override
	public String getModelDescription()
	{
		return modelDescription;
	}

	@Override
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

	@Override
	public ColorMap getColorMap()
	{
		return colorMap;
	}

	@Override
	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}

	@Override
	public String getPaintedVariable()
	{
		return null;
	}

	@Override
	public void setPaintedVariable(String paintedVariable)
	{
		//not used by the GDALRasterModel
	}

	/**
	 * Return a map containing these parameters. The map will contain keys that
	 * can be used in the constructor {@link #GDALRasterModelParameters(Map)}
	 */
	public Map<String, String> asParameterMap()
	{
		Map<String, String> result = new LinkedHashMap<String, String>();

		if (!Util.isBlank(modelName))
		{
			result.put(MODEL_NAME, modelName);
		}
		if (!Util.isBlank(modelDescription))
		{
			result.put(MODEL_DESCRIPTION, modelDescription);
		}
		if (!Util.isBlank(sourceProjection))
		{
			result.put(SOURCE_SRS, sourceProjection);
		}
		result.put(ELEVATION_BAND, Integer.toString(elevationBandIndex));
		if (offset != null)
		{
			result.put(ELEVATION_OFFSET, Double.toString(offset));
		}
		if (scale != null)
		{
			result.put(ELEVATION_SCALE, Double.toString(scale));
		}
		if (subsample != null)
		{
			result.put(ELEVATION_SUBSAMPLE, Integer.toString(subsample));
		}
		if (colorMap != null)
		{
			result.put(COLOR_MAP, new CompactStringColorMapWriter().writeToString(colorMap));
		}

		return result;
	}
}
