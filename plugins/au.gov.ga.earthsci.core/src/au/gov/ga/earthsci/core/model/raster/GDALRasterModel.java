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
package au.gov.ga.earthsci.core.model.raster;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.gdal.gdal.Dataset;

import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexBasedGeometry;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * An {@link IModel} implementation that has been generated from a GDAL raster
 * band.
 * <p/>
 * Contains optional references to the original GDAL raster and parameters to
 * allow the geometry etc. to be regenerated if needed.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModel implements IModel
{

	private final String id;

	/** The dataset used to create the model */
	private Dataset rasterDataset;

	/** The parameters used to create the model */
	private GDALRasterModelParameters parameters;

	/** The geometry for the model */
	private IVertexBasedGeometry geometry;

	private String name;
	private String description;

	/**
	 * Create a new model with the given ID and geometry
	 */
	public GDALRasterModel(String id, IVertexBasedGeometry geometry)
	{
		this(id, geometry, null, null);
	}

	public GDALRasterModel(String id, IVertexBasedGeometry geometry,
			Dataset dataset, GDALRasterModelParameters parameters)
	{
		this(id, geometry, dataset, parameters, null, null);
	}

	public GDALRasterModel(String id, IVertexBasedGeometry geometry,
			Dataset dataset, GDALRasterModelParameters parameters,
			String name, String description)
	{
		this.id = Util.isBlank(id) ? UUID.randomUUID().toString() : id;
		this.rasterDataset = dataset;
		this.parameters = parameters;
		this.geometry = geometry;
		this.name = name;
		this.description = description;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public Collection<IModelGeometry> getGeometries()
	{
		return Collections.singletonList((IModelGeometry) geometry);
	}

	@Override
	public IModelGeometry getGeometry(String id)
	{
		if (geometry.getId().equals(id))
		{
			return geometry;
		}
		return null;
	}

	/**
	 * @return the GDAL raster dataset used to construct this model, if
	 *         available.
	 */
	public Dataset getRasterDataset()
	{
		return rasterDataset;
	}

	/**
	 * @return Whether this model has a reference to the raster dataset used to
	 *         create it
	 */
	public boolean hasRasterDataset()
	{
		return rasterDataset != null;
	}

	/**
	 * @return the parameters used to construct this model, if available
	 */
	public GDALRasterModelParameters getParameters()
	{
		return parameters;
	}

	/**
	 * @return Whether this model has a reference to the dataset parameters used
	 *         to create it
	 */
	public boolean hasParameters()
	{
		return parameters != null;
	}
}
