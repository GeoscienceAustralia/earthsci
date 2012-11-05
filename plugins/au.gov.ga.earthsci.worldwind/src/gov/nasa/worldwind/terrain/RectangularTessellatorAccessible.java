package gov.nasa.worldwind.terrain;

/**
 * An extension of the {@link RectangularTessellator} that gives access to some
 * of the inner workings for use in specialised subclasses.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RectangularTessellatorAccessible extends RectangularTessellator
{
	/**
	 * Helper method that replaces the {@link RenderInfo} on the provided
	 * {@link RectTile} instance with the provided {@link RenderInfo} instance.
	 * 
	 * @param tile
	 *            The tile to set the {@link RenderInfo} on
	 * @param ri
	 *            The {@link RenderInfo} to set
	 */
	public static void setRenderInfo(RectTile tile, RenderInfo ri)
	{
		tile.ri = ri;
	}

	/**
	 * Return the value given by the {@link RenderInfo#getSizeInBytes()}
	 * function.
	 * 
	 * @param ri
	 *            {@link RenderInfo} object to call
	 * @return Result of the getSizeInBytes() function.
	 */
	public static long getSizeInBytes(RenderInfo ri)
	{
		return ri.getSizeInBytes();
	}
}
