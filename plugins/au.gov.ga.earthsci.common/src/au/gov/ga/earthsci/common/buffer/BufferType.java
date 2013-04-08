package au.gov.ga.earthsci.common.buffer;

/**
 * An enumeration of the types of data stored in native byte buffers
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum BufferType
{
	/** 8 bit value */
	BYTE(1, Byte.class),

	/** 16 bit signed integer */
	SHORT(2, Short.class),

	/** 16 bit unsigned integer (uses Java Integer) */
	UNSIGNED_SHORT(2, Integer.class),

	/** 32 bit signed integer */
	INT(4, Integer.class),

	/** 32 bit unsigned integer (uses Java Long) */
	UNSIGNED_INT(4, Long.class),

	/** 64 bit signed integer */
	LONG(8, Long.class),

	/** 32 bit IEE floating point */
	FLOAT(4, Float.class),

	/** 64 bit IEE floating point */
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

	/**
	 * Return the Java type that will be used to store the buffer values.
	 */
	public Class<?> getContainerClass()
	{
		return typeClass;
	}
}
