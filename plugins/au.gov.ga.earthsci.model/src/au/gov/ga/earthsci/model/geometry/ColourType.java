package au.gov.ga.earthsci.model.geometry;

/**
 * An enumeration of supported colour spaces
 */
public enum ColourType
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
	
	private ColourType(int n)
	{
		numComponents = n;
	}
	
	public int getNumComponents()
	{
		return numComponents;
	}
}
