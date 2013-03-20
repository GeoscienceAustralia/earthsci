package au.gov.ga.earthsci.model.geometry;

import au.gov.ga.earthsci.model.data.IModelData;

/**
 * An interface for vertex based geometries that provide colour information on a per-vertex basis.
 * <p/>
 * <b>Events</b>
 * <dl>
 * 	<dt>{@value #VERTEX_COLOUR_EVENT_NAME}</dt><dd>Issued when the vertex colours associated with this geometry change</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IVertexColouredGeometry extends IVertexBasedGeometry
{

	String VERTEX_COLOUR_KEY = "au.gov.ga.earthsci.model.geometry.vertexcolour"; //$NON-NLS-1$
	String VERTEX_COLOUR_EVENT_NAME = "vertexColour"; //$NON-NLS-1$
	
	/**
	 * Return the per-vertex colour information for this geometry, if it is available
	 * <p/>
	 * The returned data contains colour components grouped in either groups of three (RGB) or four (RGBA)
	 * depending on the colour type.
	 * <p/>
	 * {@code r,g,b,r,g,b... or r,g,b,a,r,g,b,a...}
	 * 
	 * @return The vertex colour data associated with this geometry
	 * 
	 * @see #getColourType()
	 */
	IModelData getVertexColour();
	
	/**
	 * Return whether or not this geometry has colour information associated with it.
	 * 
	 * @return <code>true</code> if this geometry has colour information associated with it;
	 * <code>false</code> otherwise.
	 * 
	 */
	boolean hasVertexColour();
	
	/**
	 * Return the colour space used by this geometry.
	 * 
	 * @return The colour space used by this geometry
	 */
	ColourType getColourType();
}
