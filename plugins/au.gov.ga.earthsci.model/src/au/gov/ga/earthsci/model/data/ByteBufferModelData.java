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

	private final String name;
	private final String description;

	private final Object nodata;
	private final Unit<?> units;

	private final int numValues;
	private final int groupSize;
	private final int numGroups;

	/**
	 * Create a new model data instance with group size of 1 and no name,
	 * description or units etc.
	 * 
	 * @param buffer
	 *            The buffer containing the data (required)
	 * @param type
	 *            The type of value contained in the data (required)
	 */
	public ByteBufferModelData(ByteBuffer buffer, BufferType type)
	{
		this(null, null, null, buffer, type, null, null);
	}

	/**
	 * Create a new model data instance with a group size of 1.
	 * 
	 * @param id
	 *            The ID to assign the data (if <code>null</code> will
	 *            auto-generate a unique ID)
	 * @param name
	 *            The (localised) human-readable name for the data (optional but
	 *            recommended)
	 * @param description
	 *            The (localised) human-readable description of the data
	 *            (optional)
	 * @param buffer
	 *            The buffer containing the data (required)
	 * @param type
	 *            The type of value contained in the buffer (required)
	 * @param nodata
	 *            The nodata value for this data (optional)
	 * @param units
	 *            Units associated with this data (optional)
	 */
	public ByteBufferModelData(String id, String name, String description,
			ByteBuffer buffer, BufferType type,
			Object nodata, Unit<?> units)
	{
		this(id, name, description, buffer, type, 1, nodata, units);
	}

	/**
	 * Create a new fully configured model data instance
	 * 
	 * @param id
	 *            The ID to assign the data (if <code>null</code> will
	 *            auto-generate a unique ID)
	 * @param name
	 *            The (localised) human-readable name for the data (optional but
	 *            recommended)
	 * @param description
	 *            The (localised) human-readable description of the data
	 *            (optional)
	 * @param buffer
	 *            The buffer containing the data (required)
	 * @param type
	 *            The type of value contained in the buffer (required)
	 * @param groupSize
	 *            The (positive integer) size of value groups contained in the
	 *            buffer. A size of 1 indicates no grouping. (required)
	 * @param nodata
	 *            The nodata value for this data (optional)
	 * @param units
	 *            Units associated with this data (optional)
	 */
	public ByteBufferModelData(String id, String name, String description,
			ByteBuffer buffer, BufferType type, int groupSize,
			Object nodata, Unit<?> units)
	{
		Validate.notNull(buffer, "A byte buffer is required"); //$NON-NLS-1$
		Validate.notNull(type, "A buffer type is required"); //$NON-NLS-1$
		Validate.isTrue(groupSize > 0, "Group size must be a positive integer"); //$NON-NLS-1$
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

		// Pre-compute values to avoid computation during render loops etc. 
		// Done here rather than lazily to allow use of final variables.
		this.numValues = buffer.limit() / type.getNumberOfBytes();
		this.groupSize = groupSize;
		this.numGroups = numValues / groupSize;
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

	@Override
	public int getNumberOfValues()
	{
		return numValues;
	}

	@Override
	public int getGroupSize()
	{
		return groupSize;
	}

	@Override
	public int getNumberOfGroups()
	{
		return numGroups;
	}

	private String stringRepresentation;

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		if (stringRepresentation != null)
		{
			return stringRepresentation;
		}

		StringBuffer result = new StringBuffer();
		result.append("ByteBufferModelData [").append('\n');
		result.append("   ").append("ID: ").append(id).append('\n');
		result.append("   ").append("Name: ").append(name).append('\n');
		result.append("   ").append("Description: ").append(description).append('\n');
		result.append("   ").append("Type: ").append(type).append('\n');
		result.append("   ").append("NumValues: ").append(numValues).append('\n');
		result.append("   ").append("NumGroups: ").append(numGroups).append('\n');
		result.append("   ").append("GroupSize: ").append(groupSize).append('\n');
		result.append("   ").append("NODATA: ").append(nodata).append('\n');
		result.append("]");
		stringRepresentation = result.toString();
		return stringRepresentation;
	}
}
