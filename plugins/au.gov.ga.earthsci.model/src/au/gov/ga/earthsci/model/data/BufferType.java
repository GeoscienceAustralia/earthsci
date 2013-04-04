package au.gov.ga.earthsci.model.data;

/**
 * An enumeration of the types of data stored in native byte buffers
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum BufferType
{
	BYTE(1, Byte.class),
	SHORT(2, Short.class),
	INT(4, Integer.class),
	LONG(8, Long.class),
	FLOAT(4, Float.class),
	DOUBLE(8, Double.class);

	private final int numBytes;
	private final Class<?> typeClass;

	private BufferType(int numBytes, Class<?> typeClass)
	{
		this.numBytes = numBytes;
		this.typeClass = typeClass;
	}

	/**
	 * @return The number of bytes used to represent this type of value in a
	 *         byte buffer/array etc.
	 */
	public int getNumberOfBytes()
	{
		return numBytes;
	}

	/**
	 * @return Whether the given object can be assigned to values of this type
	 */
	public boolean isAssignableFrom(Object testObject)
	{
		return typeClass.isAssignableFrom(testObject.getClass());
	}
}
