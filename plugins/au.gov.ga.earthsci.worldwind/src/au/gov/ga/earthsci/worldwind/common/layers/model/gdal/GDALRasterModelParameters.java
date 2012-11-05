package au.gov.ga.earthsci.worldwind.common.layers.model.gdal;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.Color;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;

/**
 * Parameters used to control how a GDAL-supported raster is converted to a model
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelParameters
{

	/** The raster band to use for model generation */
	private int band = 1;
	
	/** The maximum variance used for mesh simplification */
	private float maxVariance = 0;
	
	/** The default color to use if no color map is provided */
	private Color defaultColor = Color.GRAY;
	
	/** The color map to apply to the model data */
	private ColorMap colorMap;
	
	/** A scale factor to use to scale Z values as appropriate */
	private Double scaleFactor;
	
	/** An offset applied to Z values as appropriate */
	private Double offset;
	
	/** 
	 * The coordinate system of the raster. 
	 * Used to provide a coordinate system for raster formats that do not store it (ASCII grids etc.) 
	 */
	private String coordinateSystem;
	
	public GDALRasterModelParameters()
	{
		this(null);
	}

	/**
	 * Construct a new instance of this class, using the params to initialise values
	 * 
	 * @param params Default parameters
	 */
	public GDALRasterModelParameters(AVList params)
	{
		if (params == null)
		{
			return;
		}
		
		ColorMap cm = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		if (cm != null)
		{
			setColorMap(cm);
		}

		Double d = (Double) params.getValue(AVKeyMore.MAX_VARIANCE);
		if (d != null)
		{
			setMaxVariance(d.floatValue());
		}
		
		Integer i = (Integer)params.getValue(AVKeyMore.TARGET_BAND);
		if (i != null)
		{
			setBand(i);
		}
		
		String s = (String)params.getValue(AVKeyMore.COORDINATE_SYSTEM);
		if (s != null)
		{
			setCoordinateSystem(s);
		}
		
		d = (Double) params.getValue(AVKeyMore.SCALE);
		if (d != null)
		{
			setScaleFactor(d);
		}
		
		d = (Double) params.getValue(AVKeyMore.OFFSET);
		if (d != null)
		{
			setOffset(d);
		}
				
	}

	/**
	 * @return The raster band to use for the model (defaults to 1)
	 */
	public int getBand()
	{
		return band;
	}

	/**
	 * @param band the band to set
	 */
	public void setBand(int band)
	{
		this.band = band;
	}
	
	/**
	 * @return The max variance to use for mesh simplification (defaults to 0)
	 */
	public float getMaxVariance()
	{
		return maxVariance;
	}

	/**
	 * @param maxVariance the maxVariance to set
	 */
	public void setMaxVariance(float maxVariance)
	{
		this.maxVariance = maxVariance;
	}
	
	/**
	 * @return the colour map to apply to the loaded data
	 */
	public ColorMap getColorMap()
	{
		return colorMap;
	}
	
	/**
	 * @param colorMap the colorMap to set
	 */
	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}
	
	/**
	 * @return the default color to use if no color map is provided
	 */
	public Color getDefaultColor()
	{
		return defaultColor;
	}
	
	/**
	 * @param defaultColor the defaultColor to set
	 */
	public void setDefaultColor(Color defaultColor)
	{
		this.defaultColor = defaultColor;
	}
	
	/**
	 * @param coordinateSystem the coordinateSystem to set
	 */
	public void setCoordinateSystem(String coordinateSystem)
	{
		this.coordinateSystem = coordinateSystem;
	}
	
	/**
	 * @return the coordinateSystem
	 */
	public String getCoordinateSystem()
	{
		return coordinateSystem;
	}

	/**
	 * @return the scaleFactor
	 */
	public Double getScaleFactor()
	{
		return scaleFactor;
	}

	/**
	 * @param scaleFactor the scaleFactor to set
	 */
	public void setScaleFactor(Double scaleFactor)
	{
		this.scaleFactor = scaleFactor;
	}
	
	/**
	 * @return the offset
	 */
	public Double getOffset()
	{
		return offset;
	}
	
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(Double offset)
	{
		this.offset = offset;
	}
}
