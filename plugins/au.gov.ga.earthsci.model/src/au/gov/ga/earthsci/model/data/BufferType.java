package au.gov.ga.earthsci.model.data;

/**
 * An enumeration of the types of data stored in native byte buffers
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum BufferType
{
	BYTE(1),
	SHORT(2),
	INT(4),
	LONG(8),
	FLOAT(4),
	DOUBLE(8);
	
	private final int numBytes;
	
	private BufferType(int numBytes)
	{
		this.numBytes = numBytes;
	}
	
	public int getNumberOfBytes()
	{
		return numBytes;
	}
}
