package au.gov.ga.earthsci.model.geometry;

import au.gov.ga.earthsci.model.data.IModelData;


/**
 * An {@link IVertexBasedGeometry} that includes an optional per-vertex mask
 * buffer. Alternatively, the geometry may use Z masking where NODATA vertices
 * are encoded using specific Z values.
 * <p/>
 * The mask buffer can be used, for example, to mask NODATA vertices from the
 * rendered output (rather than using transparent colouring etc.)
 * 
 * <b>Events</b>
 * <dl>
 * <dt>{@value #MASK_EVENT_NAME}</dt>
 * <dd>Issued when the mask buffer in this geometry changes</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public interface IMaskedGeometry extends IVertexBasedGeometry
{

	public static final String MASK_KEY = "au.gov.ga.earthsci.model.geometry.mask"; //$NON-NLS-1$
	public static final String MASK_EVENT_NAME = "mask"; //$NON-NLS-1$

	/**
	 * Return the per-vertex mask data for the geometry.
	 * <p/>
	 * The returned data will use byte values, with 0 and 1 representing
	 * excluded/included.
	 * <p/>
	 * The returned data will have a value for each vertex in
	 * {@link #getVertices()}
	 * 
	 * @return The per-vertex mask data for the geometry.
	 */
	IModelData getMask();

	/**
	 * Return whether this geometry has a mask.
	 * 
	 * @return <code>true</code> if a mask exists; <code>false</code> otherwise.
	 */
	boolean hasMask();

	/**
	 * If <code>true</code>, this geometry has mask values encoded in the vertex
	 * data returned by {@link #getVertices()}. An example of this is model data
	 * loaded from 2D grids where particular grid values represent NODATA. In
	 * this case, masking can be determined by inspecting geometry vertex data
	 * and the {@link IModelData#getNoDataValue()} returned from
	 * {@link #getVertices()}
	 * 
	 * @return <code>true</code> if this geometry uses Z masking;
	 *         <code>false</code> otherwise.
	 */
	boolean useZMasking();
}
