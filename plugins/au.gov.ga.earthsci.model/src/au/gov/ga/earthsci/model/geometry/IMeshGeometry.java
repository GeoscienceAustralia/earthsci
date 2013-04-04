package au.gov.ga.earthsci.model.geometry;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.model.data.IModelData;

/**
 * An interface for geometry types that use vertices, edges and faces.
 * <p/>
 * <b>Events</b>
 * <dl>
 * 	<dt>{@value #FACE_TYPE_EVENT_NAME}</dt><dd>Issued when the face type used in this geometry changes</dd>
 * 	<dt>{@value #EDGE_INDICES_EVENT_NAME}</dt><dd>Issued when the edge indices in this geometry change</dd>
 *  <dt>{@value #NORMALS_EVENT_NAME}</dt><dd>Issued when the normals in this geometry change</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IMeshGeometry extends IVertexBasedGeometry
{

	String EDGE_INDICES_KEY = "au.gov.ga.earthsci.model.geometry.edges"; //$NON-NLS-1$
	String EDGE_INDICES_EVENT_NAME = "edgeIndices"; //$NON-NLS-1$
	
	String NORMALS_KEY = "au.gov.ga.earthsci.model.geometry.normals"; //$NON-NLS-1$
	String NORMALS_EVENT_NAME = "normals"; //$NON-NLS-1$
	
	String FACE_TYPE_EVENT_NAME = "faceType"; //$NON-NLS-1$
	
	/**
	 * Return the face type used by this geometry. This is an indication of how the data returned in 
	 * {@link #getEdgeIndices()} should be interpreted, and is defined to reflect the OpenGL standard
	 * primitive types.
	 * 
	 * @return The face type used by this geometry
	 */
	FaceType getFaceType();
	
	/**
	 * Return the edge indices used to form faces of the mesh. Indices are interpreted
	 * according to the face type when forming faces.
	 * <p/>
	 * Indices will be {@link BufferType#BYTE}, {@link BufferType#SHORT}, or {@link BufferType#INT} 
	 * values that should be interpreted as an integer index into the associated {@link #getVertices()}
	 * data. 
	 * 
	 * @return The edge index data for this geometry
	 * 
	 * @see #getFaceType()
	 */
	IModelData getEdgeIndices();
	
	/**
	 * Return whether there are any edge indices associated with this geometry. Note that
	 * edge indices are required if the geometry is to be used as a mesh.
	 * 
	 * @return <code>true</code> if edge indices are available; <code>false</code> otherwise.
	 */
	boolean hasEdgeIndices();
	
	/**
	 * Return the normals for this geometry, if they exist.
	 * <p/>
	 * The returned normals, if they exist, will contain a normal per vertex for the geometry. Normals
	 * are grouped as 3 values (u,v,w).
	 * 
	 * @return The normals for this geometry, if they exist, or <code>null</code> if none exist.
	 */
	IModelData getNormals();
	
	/**
	 * Return whether this geometry has normals data associated
	 * 
	 * @return <code>true</code> if this geometry has normals data associated with it; 
	 * <code>false</code> otherwise.
	 */
	boolean hasNormals();
}
