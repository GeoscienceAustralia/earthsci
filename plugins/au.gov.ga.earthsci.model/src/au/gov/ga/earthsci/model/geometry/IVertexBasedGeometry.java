package au.gov.ga.earthsci.model.geometry;

import javax.swing.text.Position;

import au.gov.ga.earthsci.model.data.IModelData;

/**
 * An interface for geometry types that are based on vertices (e.g. point,
 * line, mesh)
 * <p/>
 * <b>Events</b>
 * <dl>
 * 	<dt>{@value #VERTICES_EVENT_NAME}</dt><dd>Issued when the vertices associated with this geometry change</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IVertexBasedGeometry extends IModelGeometry
{

	String VERTICES_KEY = "au.gov.ga.earthsci.model.geometry.vertices"; //$NON-NLS-1$
	String VERTICES_EVENT_NAME = "vertices"; //$NON-NLS-1$
	
	/**
	 * Return the vertices in this geometry
	 * <p/>
	 * Vertices are returned in geographic coordinates, in WGS84 Lat/Lon
	 * 
	 * @return The vertices in this geometry
	 */
	IModelData<Position> getVertices();

	/**
	 * Return whether this geometry has vertices associated with it.
	 * 
	 * @return <code>true</code> if there are vertices; <code>false</code> otherwise.
	 */
	boolean hasVertices();
}
