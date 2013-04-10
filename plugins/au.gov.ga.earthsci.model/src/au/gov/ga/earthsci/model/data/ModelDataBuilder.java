package au.gov.ga.earthsci.model.data;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.UUID;

import org.unitsofmeasurement.unit.Unit;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.common.util.Validate;

/**
 * A builder class that can be used to conveniently create {@link IModelData}
 * instances.
 * <p/>
 * Creates an appropriate {@link IModelData} implementation for the provided
 * backing buffer.
 * <p/>
 * For typed "view" buffers (e.g. {@link FloatBuffer} etc.), buffer type is
 * derived. For all others ({@link ByteBuffer} etc.) a type must be provided.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ModelDataBuilder
{

	private Buffer buffer;
	private BufferType type;

	private String id;
	private String name;
	private String description;

	private Unit<?> units;
	private Object nodata;

	private ModelDataBuilder(Buffer buffer)
	{
		this.buffer = buffer;
	};

	/**
	 * @see IModelData#getSource()
	 */
	public static ModelDataBuilder createFromBuffer(Buffer buffer)
	{
		Validate.notNull(buffer, "A buffer is required"); //$NON-NLS-1$
		return new ModelDataBuilder(buffer);
	}

	/**
	 * @see IModelData#getBufferType()
	 */
	public ModelDataBuilder ofType(BufferType type)
	{
		this.type = type;
		return this;
	}

	/**
	 * @see IModelData#getId()
	 */
	public ModelDataBuilder withId(String id)
	{
		this.id = Util.isEmpty(id) ? UUID.randomUUID().toString() : id;
		return this;
	}

	/**
	 * @see IModelData#getName()
	 */
	public ModelDataBuilder named(String name)
	{
		this.name = name;
		return this;
	}

	/**
	 * @see IModelData#getDescription()
	 */
	public ModelDataBuilder describedAs(String description)
	{
		this.description = description;
		return this;
	}

	/**
	 * @see IModelData#getUnits()
	 */
	public ModelDataBuilder withUnits(Unit<?> units)
	{
		this.units = units;
		return this;
	}

	/**
	 * @see IModelData#getNoDataValue()
	 */
	public ModelDataBuilder withNodata(Object nodata)
	{
		this.nodata = nodata;
		return this;
	}

	public IModelData build()
	{
		if (buffer instanceof FloatBuffer)
		{
			Validate.isTrue(nodata == null || nodata instanceof Float, "NODATA must be of type Float, not " //$NON-NLS-1$
					+ nodata.getClass().getSimpleName());
			return new FloatBufferModelData(id, name, description, (FloatBuffer) buffer, (Float) nodata, units);
		}
		if (buffer instanceof ByteBuffer)
		{
			Validate.notNull(type, "For general buffers a buffer type must be provided"); //$NON-NLS-1$

			return new ByteBufferModelData(id, name, description, (ByteBuffer) buffer, type, nodata, units);
		}

		throw new UnsupportedOperationException(buffer.getClass().getSimpleName()
				+ " backed model data not supported yet"); //$NON-NLS-1$
	}
}
