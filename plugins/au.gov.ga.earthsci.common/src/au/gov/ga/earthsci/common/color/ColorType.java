package au.gov.ga.earthsci.common.color;

/**
 * An enumeration of supported colour spaces
 */
public enum ColorType
{
	/**
	 * 3 components per colour: Red, Green, Blue
	 */
	RGB(3),
	
	/**
	 * 4 components per colour: Red, Green, Blue, Alpha
	 */
	RGBA(4);
	
	private int numComponents;
	
	private ColorType(int n)
	{
		numComponents = n;
	}
	
	public int getNumComponents()
	{
		return numComponents;
	}
}
