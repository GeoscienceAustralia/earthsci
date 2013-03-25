package au.gov.ga.earthsci.model;

import java.util.Collection;

import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.IDirtyable;
import au.gov.ga.earthsci.common.util.IIdentifiable;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * Represents a 3D model comprised of one of more geometries represented by
 * {@link IModelGeometry} instances.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModel extends IIdentifiable, INamed, IDescribed, IDirtyable
{
	/**
	 * Return the geometries associated with this model
	 * 
	 * @return The collection of geometries that make up this model
	 */
	Collection<IModelGeometry> getGeometries();
	
	/**
	 * Return the geometry with the given ID, if one exists on this model.
	 * 
	 * @param id The ID of the geometry to return.
	 * 
	 * @return The geometry with the given ID, or <code>null</code> if no geometry
	 * with that ID exists on this model
	 */
	IModelGeometry getGeometry(String id);
}

