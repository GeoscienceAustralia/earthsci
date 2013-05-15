package au.gov.ga.earthsci.model;

import java.util.List;

import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.IIdentifiable;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.common.util.IPropertyChangeBean;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * Represents a 3D model comprised of one of more geometries represented by
 * {@link IModelGeometry} instances.
 * <p/>
 * <b>Events</b>
 * <dl>
 * <dt>{@value #GEOMETRIES_EVENT_NAME}</dt>
 * <dd>Fired when the geometries list on this model changes (if applicable)</dd>
 * <dt>{@value #OPACITY_EVENT_NAME}</dt>
 * <dd>Fired when the opacity on this model changes (if applicable)</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModel extends IIdentifiable, INamed, IDescribed, IPropertyChangeBean
{
	/** The event name triggered when geometries change on this model */
	String GEOMETRIES_EVENT_NAME = "geometries"; //$NON-NLS-1$

	/** The event name triggered when the opacity changes on this model */
	String OPACITY_EVENT_NAME = "opacity"; //$NON-NLS-1$

	/**
	 * Return the geometries associated with this model
	 * 
	 * @return The collection of geometries that make up this model
	 */
	List<IModelGeometry> getGeometries();

	/**
	 * Return the geometry with the given ID, if one exists on this model.
	 * 
	 * @param id
	 *            The ID of the geometry to return.
	 * 
	 * @return The geometry with the given ID, or <code>null</code> if no
	 *         geometry with that ID exists on this model
	 */
	IModelGeometry getGeometry(String id);

	/**
	 * Set a global opacity on this model.
	 * <p/>
	 * Values should be in the range {@code [0,1]} where 0 = fully transparent
	 * and 1 = fully opaque.
	 * <p/>
	 * This value will be propogated to all constituent geometries.
	 * 
	 * @param opacity
	 *            The opacity to set, in the range {@code [0,1]}
	 */
	void setOpacity(double opacity);

	/**
	 * Return the current opacity value for this model.
	 * <p/>
	 * Value will be in the range {@code [0,1]} where 0 = fully transparent and
	 * 1 = fully opaque.
	 * 
	 * @return The current opacity for this model.
	 */
	double getOpacity();
}
