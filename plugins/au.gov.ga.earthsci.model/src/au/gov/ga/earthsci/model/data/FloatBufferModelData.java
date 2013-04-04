package au.gov.ga.earthsci.model.data;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.UUID;

import org.unitsofmeasurement.unit.Unit;

import au.gov.ga.earthsci.common.util.Validate;

/**
 * An implementation of the {@link IModelData} interface backed by a
 * {@link FloatBuffer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class FloatBufferModelData implements IModelData
{
	private final FloatBuffer buffer;
	private final String id;
	private String name;
	private String description;

	private Float nodata;
	private Unit<?> units;

	public FloatBufferModelData(FloatBuffer buffer)
	{
		this(null, null, null, buffer, null, null);
	}

	public FloatBufferModelData(String id, String name, String description, FloatBuffer buffer, Float nodata,
			Unit<?> units)
	{
		Validate.notNull(buffer, "A float buffer is required"); //$NON-NLS-1$

		this.id = id == null ? UUID.randomUUID().toString() : id;

		this.name = name;
		this.description = description;

		this.buffer = buffer;

		this.nodata = nodata;
		this.units = units;
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
	public Object getNoDataValue()
	{
		return nodata;
	}

	@Override
	public Buffer getSource()
	{
		return buffer;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.FLOAT;
	}

	@Override
	public Unit<?> getUnits()
	{
		return units;
	}

	@Override
	public boolean hasUnits()
	{
		return units != null;
	}
}
