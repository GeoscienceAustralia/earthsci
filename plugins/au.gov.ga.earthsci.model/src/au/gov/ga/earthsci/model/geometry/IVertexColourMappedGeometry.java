package au.gov.ga.earthsci.model.geometry;

import au.gov.ga.earthsci.common.color.ColorMap;

/**
 * An interface for geometries that use a {@link ColorMap} to provide colouring
 * information rather than a texture or color map etc.
 * <p/>
 * For alternatives, see {@link IVertexColouredGeometry}.
 * <p/>
 * <b>Events</b>
 * <dl>
 * <dt>{@value #COLOR_MAP_EVENT_NAME}</dt>
 * <dd>Issued when the colour map associated with this geometry change</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public interface IVertexColourMappedGeometry extends IModelGeometry
{

	String COLOR_MAP_KEY = "au.gov.ga.earthsci.model.geometry.colormap"; //$NON-NLS-1$
	String COLOR_MAP_EVENT_NAME = "colorMap"; //$NON-NLS-1$

	/**
	 * @return The ColorMap to use for this geometry
	 */
	ColorMap getColorMap();

	/**
	 * @return Whether a colour map exists on this geometry
	 */
	boolean hasColorMap();

	/**
	 * @return The index of the axis to use for colouring ([x,y,z]->[0,1,2])
	 */
	int getColouredAxis();

	/**
	 * @return <code>true</code> if the X axis is to be used for colouring
	 * 
	 * @see #getColouredAxis()
	 */
	boolean isXColoured();

	/**
	 * @return <code>true</code> if the X axis is to be used for colouring
	 * 
	 * @see #getColouredAxis()
	 */
	boolean isYColoured();

	/**
	 * @return <code>true</code> if the Z axis is to be used for colouring
	 * 
	 * @see #getColouredAxis()
	 */
	boolean isZColoured();

}
