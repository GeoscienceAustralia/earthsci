package au.gov.ga.earthsci.model.data;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.unitsofmeasurement.unit.Unit;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.util.Validate;

/**
 * A general-purpose immutable {@link IModelData} implementation backed by a
 * {@link ByteBuffer}
 * <p/>
 * Client code should use the {@link #getBufferType()} method to determine what
 * type of values to read from the buffer.
 * <p/>
 * The {@link #getSource()} method of this implementation will return a
 * duplicate view of the underlying source buffer. This allows multiple threads
 * to modify buffer limits and positions etc. safely. Note, however, that for
 * performance reasons the returned buffers share the same underlying data.
 * <b>No modifications should be made to the data obtained from
 * {@link #getSource()}</b>. To do so is considered programmer error.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ByteBufferModelData implements IModelData
{

	private final ByteBuffer buffer;
	private final BufferType type;
	private final String id;

	private String name;
	private String description;

	private Object nodata;
	private Unit<?> units;

	public ByteBufferModelData(ByteBuffer buffer, BufferType type)
	{
		this(null, null, null, buffer, type, null, null);
	}

	public ByteBufferModelData(String id, String name, String description, ByteBuffer buffer, BufferType type,
			Object nodata, Unit<?> units)
	{
		Validate.notNull(buffer, "A byte buffer is required"); //$NON-NLS-1$
		Validate.notNull(type, "A buffer type is required"); //$NON-NLS-1$
		if (nodata != null)
		{
			Validate.isTrue(type.isAssignableFrom(nodata), "NODATA must be of type " + type.name() //$NON-NLS-1$
					+ ", not " + nodata.getClass().getSimpleName()); //$NON-NLS-1$
		}

		this.id = id == null ? UUID.randomUUID().toString() : id;

		this.name = name;
		this.description = description;

		this.buffer = buffer;
		this.type = type;
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
	public ByteBuffer getSource()
	{
		ByteBuffer result = (ByteBuffer) buffer.duplicate().rewind();
		result.order(buffer.order());
		return result;
	}

	@Override
	public BufferType getBufferType()
	{
		return type;
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
